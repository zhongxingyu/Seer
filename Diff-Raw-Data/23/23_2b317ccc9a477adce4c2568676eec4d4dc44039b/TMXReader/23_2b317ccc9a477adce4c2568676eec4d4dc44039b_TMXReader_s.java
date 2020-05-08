 package com.spartansoftwareinc.otter;
 
 import java.io.Reader;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.List;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Characters;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartElement;
 
 import net.sundell.snax.DefaultElementHandler;
 import net.sundell.snax.ElementSelector;
 import net.sundell.snax.NodeModel;
 import net.sundell.snax.NodeModelBuilder;
 import net.sundell.snax.SNAXParser;
 import net.sundell.snax.SNAXUserException;
 
 import static com.spartansoftwareinc.otter.TMXConstants.*;
 import static com.spartansoftwareinc.otter.TMXEventType.*;
 import static com.spartansoftwareinc.otter.Util.*;
 
 public class TMXReader {
     
     public static TMXReader createTMXEventReader(Reader r) {
         return new TMXReader(r);
     }
 
     private TMXReader(Reader r) {
         XMLInputFactory factory = XMLInputFactory.newFactory();
         factory.setProperty(XMLInputFactory.IS_COALESCING, true);
         try {
             parser = SNAXParser.createParser(factory, buildModel());
             parser.startParsing(r, new SegmentBuilder(this));
         }
         catch (XMLStreamException e) {
             errorHandler.xmlError(e);
         }
     }
     
     private ErrorHandler errorHandler = new DefaultErrorHandler();
     private SNAXParser<SegmentBuilder> parser;
     private List<TMXEvent> events = new ArrayList<TMXEvent>();
     private Deque<TUVContentSink> contentStack = new ArrayDeque<TUVContentSink>();
     private Header header = null;
     private int nextSequence = 0;
     private boolean currentTuIsInError = false;
     
     /**
      * Flag the current TU as being in error.
      * @param e
      */
     void reportTuError(OtterException e) {
         currentTuIsInError = true;
         errorHandler.tuError(nextSequence, e);
     }
     
     /**
      * Set the current {@link ErrorHandler}.
      * @param handler ErrorHandler to install
      */
     public void setErrorHandler(ErrorHandler handler) {
         this.errorHandler = handler;
     }
     
     /**
      * Check to see if a {@link TMXEvent} is available.  This may 
      * involve the parser consuming some portion of the source XML.
      * Errors encountered during parsing are reported via the 
      * {@link ErrorHandler} interface.  The default ErrorHandler will
      * throw {@link OtterException} for any XML errors encountered
      * during parsing.
      * 
      * @return true if any events are available, false if the end of 
      *         document has been reached.
      */
     public boolean hasNext() {
         try {
             if (events.size() > 0) {
                 return true;
             }
             while (parser.hasMoreEvents()) {
                 parser.processEvent();
                 if (events.size() > 0) {
                     return true;
                 }
             }
             return false;
         }
         catch (XMLStreamException e) {
             errorHandler.xmlError(e);
             return false;
         }
     }
     
     /**
      * Return the next available event.  
      * @return the next available {@link TMXEvent}, or null if the end
      *         of the document has been reached.
      */
     public TMXEvent nextEvent() {
         if (hasNext()) {
             return events.remove(0);
         }
         return null;
     }
     
     protected void addEvent(TMXEvent event) {
         events.add(event);
     }
     
     protected void addTUVContent(TUVContent content) {
         contentStack.peek().addContent(content);
     }
     protected void addTextContent(String text) {
         addTUVContent(new TextContent(text));
     }
     protected void addCodeContent(String codes) {
         addTUVContent(new CodeContent(codes));
     }
     
     private NodeModel<SegmentBuilder> buildModel() {
         return new NodeModelBuilder<SegmentBuilder>() {
             ElementSelector<SegmentBuilder> phAnchor, bptAnchor, eptAnchor, itAnchor;
             {
                 elements("tmx").attach(new TMXHandler());
                 elements("tmx", "header").attach(new HeaderHandler());
                 elements("tmx", "header", "prop").attach(new HeaderPropertyHandler());
                 elements("tmx", "header", "note").attach(new HeaderNoteHandler());
                 elements("tmx", "body").attach(new BodyHandler());
                 elements("tmx", "body", "tu").attach(new TuHandler());
                 elements("tmx", "body", "tu", "prop").attach(new TuPropertyHandler());
                 elements("tmx", "body", "tu", "note").attach(new TuNoteHandler());
                 ElementSelector<SegmentBuilder> tuvAnchor = elements("tmx", "body", "tu", "tuv");
                 ElementSelector<SegmentBuilder> segAnchor = tuvAnchor.element("seg");
                 tuvAnchor.attach(new TuvHandler());
                 segAnchor.attach(new SegHandler());
                 tuvAnchor.element("prop").attach(new TuvPropertyHandler());
                 tuvAnchor.element("note").attach(new TuvNoteHandler());
                 
                 phAnchor = segAnchor.element("ph");
                 bptAnchor = segAnchor.element("bpt");
                 eptAnchor = segAnchor.element("ept");
                 itAnchor = segAnchor.element("it");
     
                 phAnchor.attach(new SegPhHandler());
                 bptAnchor.attach(new SegBptHandler());
                 eptAnchor.attach(new SegEptHandler());
                 itAnchor.attach(new SegItHandler());
     
                 // Link each type of subflow back to the correct anchor
                 attachSubflowStates(phAnchor);
                 attachSubflowStates(bptAnchor);
                 attachSubflowStates(eptAnchor);
                 attachSubflowStates(itAnchor);
                 
                 // Snax currently doesn't support anything like conditional handlers based
                 // on which incoming transition to a state was used.  Rather than add them,
                 // I'm going to make the state machine larger, so that the <hi>-descendant
                 // state loop is separate from the overall content state loop.
                 ElementSelector<SegmentBuilder> hiAnchor = segAnchor.element("hi");
                 hiAnchor.attach(new SegHiHandler());
                 hiAnchor.addTransition("hi", hiAnchor); // Nested case
                 hiAnchor.element("ph").attach(new SegPhHandler());
                 hiAnchor.element("bpt").attach(new SegBptHandler());
                 hiAnchor.element("ept").attach(new SegEptHandler());
                 hiAnchor.element("it").attach(new SegItHandler());
             }
             private void attachSubflowStates(ElementSelector<SegmentBuilder> e) {
                 ElementSelector<SegmentBuilder> sub = e.element("sub");
                 sub.attach(new SegSubHandler());
                 sub.addTransition("ph", phAnchor);
                 sub.addTransition("bpt", bptAnchor);
                 sub.addTransition("ept", eptAnchor);
                 sub.addTransition("it", itAnchor);
             }
         }.build();
     }
     
     class TMXHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             String version = requireAttrVal(element, VERSION, errorHandler);
             if (!TMX_VERSION_1_4.equals(version)) {
                 errorHandler.error(new OtterException("Unsupported TMX version: " +
                                    version, element.getLocation()));
             }
             addEvent(new TMXEvent(START_TMX));
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             addEvent(new TMXEvent(END_TMX));
         }
     }
     class HeaderHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             header = new Header();
             header.setAdminLang(attrVal(element, ADMINLANG));
             header.setChangeDate(attrValAsDate(element, CHANGEDATE, errorHandler));
             header.setChangeId(attrVal(element, CHANGEID));
             header.setCreationDate(attrValAsDate(element, CREATIONDATE, errorHandler));
             header.setCreationId(attrVal(element, CREATIONID));
             header.setCreationTool(attrVal(element, CREATIONTOOL));
             header.setCreationToolVersion(attrVal(element, CREATIONTOOLVERSION));
             header.setDataType(attrVal(element, DATATYPE));
             header.setEncoding(attrVal(element, ENCODING));
             header.setSegType(attrVal(element, SEGTYPE));
             header.setSrcLang(attrVal(element, SRCLANG));
             header.setTmf(attrVal(element, TMF));
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             addEvent(new TMXEvent(TMXEventType.HEADER, header));
             header = null;
         }
     }
     abstract class PropertyHandler extends DefaultElementHandler<SegmentBuilder> {
         private StringBuilder value = new StringBuilder();
         private String type = null;
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             value.setLength(0);
             type = attrVal(element, TYPE);
         }
         @Override
         public void characters(StartElement parent, Characters characters,
                 SegmentBuilder data) throws SNAXUserException {
             value.append(characters.getData());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             require(type != null, element.getLocation(), "Property type was not set");
             handleProperty(data, new Property(type, value.toString()));
         }
         abstract void handleProperty(SegmentBuilder data, Property property);
     }
     class HeaderPropertyHandler extends PropertyHandler {
         @Override
         void handleProperty(SegmentBuilder data, Property property) {
             header.addProperty(property);
         }
     }
     abstract class NoteHandler extends DefaultElementHandler<SegmentBuilder> {
         private StringBuilder value = new StringBuilder();
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             value.setLength(0);
         }
         @Override
         public void characters(StartElement parent, Characters characters,
                 SegmentBuilder data) throws SNAXUserException {
             value.append(characters.getData());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             handleNote(data, new Note(value.toString()));
         }
         abstract void handleNote(SegmentBuilder data, Note note);
     }
     class HeaderNoteHandler extends NoteHandler {
         @Override
         void handleNote(SegmentBuilder data, Note note) {
             header.addNote(note);
         }
     }
     class BodyHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             addEvent(new TMXEvent(START_BODY));
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             addEvent(new TMXEvent(END_BODY));
         }
     }
     class TuHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             currentTuIsInError = false;
             data.startTu(element);
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             if (!currentTuIsInError) {
                 TUEvent e = new TUEvent(data.getTu());
                 e.setSequence(nextSequence);
                 addEvent(e);
             }
             // Regardless of outcome, we always increment the sequence id.
             nextSequence++;
         }
     }
     class TuPropertyHandler extends PropertyHandler {
         @Override
         void handleProperty(SegmentBuilder data, Property property) {
             data.getTu().addProperty(property);
         }
     }
     class TuNoteHandler extends NoteHandler {
         @Override
         void handleNote(SegmentBuilder data, Note note) {
             data.getTu().addNote(note);
         }
     }
 
     class TuvHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             data.startTuv(element);
             contentStack.push(data.getCurrentTuv());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             data.endTuv();
             contentStack.pop();
         }
     }
     class TuvPropertyHandler extends PropertyHandler {
         @Override
         void handleProperty(SegmentBuilder data, Property property) {
             data.getCurrentTuv().addProperty(property);
         }
     }
     class TuvNoteHandler extends NoteHandler {
         @Override
         void handleNote(SegmentBuilder data, Note note) {
             data.getCurrentTuv().addNote(note);
         }
     }
     class SegHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void characters(StartElement parent, Characters characters,
                 SegmentBuilder data) throws SNAXUserException {
             addTextContent(characters.getData());
         }
     }
     class SegPhHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             PlaceholderTag ph = new PlaceholderTag();
             Integer x = attrValAsInteger(element, X);
             if (x != null) {
                 ph.setX(x);
             }
             ph.setType(attrVal(element, TYPE));
             ph.setAssoc(attrVal(element, ASSOC));
             addTUVContent(ph);
             contentStack.push(ph);
         }
         @Override
         public void characters(StartElement parent, Characters characters,
                 SegmentBuilder data) throws SNAXUserException {
             addCodeContent(characters.getData());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             contentStack.pop();
         }
     }
     class SegBptHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
            BeginTag bpt = new BeginTag(requireAttrValAsInteger(element, I, errorHandler));
             Integer x = attrValAsInteger(element, X);
             if (x != null) {
                 bpt.setX(x);
             }
             String type = attrVal(element, TYPE);
             if (type != null) {
                 bpt.setType(type);
             }
             data.startPair(bpt, element);
             addTUVContent(bpt);
             contentStack.push(bpt);
         }
         @Override
         public void characters(StartElement parent, Characters characters,
                 SegmentBuilder data) throws SNAXUserException {
             addCodeContent(characters.getData());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             contentStack.pop();
         }
     }
     class SegEptHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             EndTag ept = new EndTag(requireAttrValAsInteger(element, I, errorHandler));
             data.endPair(ept, element);
             addTUVContent(ept);
             contentStack.push(ept);
         }
         @Override
         public void characters(StartElement parent, Characters characters,
                 SegmentBuilder data) throws SNAXUserException {
             addCodeContent(characters.getData());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             contentStack.pop();
         }
     }
     class SegItHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             IsolatedTag it = new IsolatedTag();
             Integer x = attrValAsInteger(element, X);
             if (x != null) {
                 it.setX(x);
             }
             it.setType(attrVal(element, TYPE));
             String v = attrVal(element, POS);
             if (v == null) {
                 reportTuError(new OtterException("<it> element missing 'pos' attribute", 
                               element.getLocation()));
             }
             else {
                 IsolatedTag.Pos pos = IsolatedTag.Pos.byAttrValue(v);
                 if (pos == null) {
                     reportTuError(new OtterException("Invalid value for 'pos' attribute: " + v, 
                             element.getLocation()));
                 }
                 it.setPos(pos);
             }
             addTUVContent(it);
             contentStack.push(it);
         }
         @Override
         public void characters(StartElement parent, Characters characters,
                 SegmentBuilder data) throws SNAXUserException {
             addCodeContent(characters.getData());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             contentStack.pop();
         }
     }
     class SegHiHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             HighlightTag hi = new HighlightTag();
             Integer x = attrValAsInteger(element, X);
             if (x != null) {
                 hi.setX(x);
             }
             String type = attrVal(element, TYPE);
             if (type != null) {
                 hi.setType(type);
             }
             // Add it to the current container, then push it onto 
             // the stack to catch content up to </hi>
             addTUVContent(hi);
             contentStack.push(hi);
         }
         @Override
         public void characters(StartElement parent, Characters characters,
                 SegmentBuilder data) throws SNAXUserException {
             addTextContent(characters.getData());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             contentStack.pop();
         }
     }
     class SegSubHandler extends DefaultElementHandler<SegmentBuilder> {
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             Subflow subflow = new Subflow();
             String type = attrVal(element, TYPE);
             if (type != null) {
                 subflow.setType(type);
             }
             String dataType = attrVal(element, DATATYPE);
             if (dataType != null) {
                 subflow.setDatatype(dataType);
             }
             addTUVContent(subflow);
             contentStack.push(subflow);
         }
         @Override
         public void characters(StartElement parent, Characters contents,
                 SegmentBuilder data) throws SNAXUserException {
             addTextContent(contents.getData());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             contentStack.pop();
         }
     }
 }
