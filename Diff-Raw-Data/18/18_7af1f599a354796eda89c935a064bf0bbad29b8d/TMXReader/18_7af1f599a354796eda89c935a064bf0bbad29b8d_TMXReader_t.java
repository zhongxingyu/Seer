 package com.spartansoftwareinc.otter;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.List;
 
 import javax.xml.stream.XMLEventReader;
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
 
 /**
  * A class to read Translation Memory Exchange (TMX) 1.4b documents.  TMX
  * documents are parsed and presented to the user as a series of
  * {@link TMXEvent} objects.  Errors, if encountered, will be
  * reported via the {@link ErrorHandler} interface.  Note that the
  * default behavior for most non-fatal errors is to ignore them.  To
  * change this behavior, use {@link #setErrorHandler(ErrorHandler)} to
  * specify a different {@link ErrorHandler} implementation.  Note that
  * while <code>TMXReadr</code> attempts to detect and report errors in
  * the TMX structure, it does not validate the document against a DTD. 
  * <p>
  * A <code>TMXReader</code> expects to be provided XML data via a
  * {@link Reader}.  A Unicode byte-order mark (BOM), if present,
  * will be stripped automatically.
  * <p>
  * <code>TMXReader</code> uses the {@link XMLEventReader} mechanism
  * to parse the underlying XML.  As a result, it is stream-based and
  * does not need to hold the entire document in memory.
  * 
  * @see <a href="http://www.gala-global.org/oscarStandards/tmx/">http://www.gala-global.org/oscarStandards/tmx/</a>
  */
 public class TMXReader {
     
     /**
      * Create a new <code>TMXReader</code> to parse TMX from the provided
      * <code>Reader</code>.
      * @param reader reader containing character content to be parsed as TMX
      * @return new TMXReader
      */
     public static TMXReader createTMXEventReader(Reader reader) throws IOException {
         return new TMXReader(stripBOM(reader));
     }
 
    
    private XMLInputFactory getInputFactory() {
        // XMLInputFactory is thread-safe as long as you're not setting
        // properties, so I could re-use this.  But unfortunately,
        //   https://bugs.openjdk.java.net/browse/JDK-8028111
        // will eventually cause that to crash under long-running
        // (ie, server-based) circumstances in which > 64k documents
        // are read.  So instead, create a new factory every time.
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
         inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
         // Disable DTD loading - this is necessary to prevent
         // Woodstox (if present) from bombing out when it can't find
         // the TMX DTD.
         inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return inputFactory;
     }
     
     private TMXReader(Reader r) {
         try {
            parser = SNAXParser.createParser(getInputFactory(), buildModel());
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
     private String srcLang = null;
     
     /**
      * Return the source language for the current TMX, as defined
      * in the header.
      */
     String getSrcLang() {
     	return srcLang;
     }
     
     /**
      * Flag the current TU as being in error.
      * @param e
      */
     void reportTuError(OtterInputException e) {
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
      * throw {@link OtterInputException} for any XML errors encountered
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
     
     private void addEvent(TMXEvent event) {
         events.add(event);
     }
     
     private void addTUVContent(TUVContent content) {
         contentStack.peek().addContent(content);
     }
     private void addTextContent(String text) {
         addTUVContent(new TextContent(text));
     }
     private void addCodeContent(String codes) {
         addTUVContent(new CodeContent(codes));
     }
     
     private NodeModel<SegmentBuilder> buildModel() {
         return new NodeModelBuilder<SegmentBuilder>() {
             ElementSelector<SegmentBuilder> phAnchor, bptAnchor, eptAnchor, itAnchor;
             {
                 elements("tmx").attach(new TMXHandler());
                 ElementSelector<SegmentBuilder> header = elements("tmx", "header"); 
                 header.attach(new HeaderHandler());
                 header.element("prop").attach(new HeaderPropertyHandler());
                 header.element("note").attach(new HeaderNoteHandler());
 
                 // <body> is currently a no-op for producing user data
                 ElementSelector<SegmentBuilder> body = elements("tmx", "body"); 
                 ElementSelector<SegmentBuilder> tu = body.element("tu");
                 tu.attach(new TuHandler());
                 tu.element("prop").attach(new TuPropertyHandler());
                 tu.element("note").attach(new TuNoteHandler());
                 ElementSelector<SegmentBuilder> tuvAnchor = tu.element("tuv");
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
                 errorHandler.error(new OtterInputException("Unsupported TMX version: " +
                                    version, element.getLocation()));
             }
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
             srcLang = header.getSrcLang();
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             addEvent(new TMXEvent(TMXEventType.START_TMX, header));
             header = null;
         }
     }
     abstract class PropertyHandler extends DefaultElementHandler<SegmentBuilder> {
         private StringBuilder value = new StringBuilder();
         private String type = null, encoding = null, lang = null;
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             value.setLength(0);
             type = attrVal(element, TYPE);
             encoding = attrVal(element, ENCODING);
             lang = attrVal(element, XMLLANG);
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
             handleProperty(data, new Property(type, value.toString())
                                         .setEncoding(encoding).setLang(lang));
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
         private String encoding = null, lang = null;
         @Override
         public void startElement(StartElement element, SegmentBuilder data)
                 throws SNAXUserException {
             value.setLength(0);
             encoding = attrVal(element, ENCODING);
             lang = attrVal(element, XMLLANG);
         }
         @Override
         public void characters(StartElement parent, Characters characters,
                 SegmentBuilder data) throws SNAXUserException {
             value.append(characters.getData());
         }
         @Override
         public void endElement(EndElement element, SegmentBuilder data)
                 throws SNAXUserException {
             handleNote(data, new Note(value.toString())
                                     .setEncoding(encoding).setLang(lang));
         }
         abstract void handleNote(SegmentBuilder data, Note note);
     }
     class HeaderNoteHandler extends NoteHandler {
         @Override
         void handleNote(SegmentBuilder data, Note note) {
             header.addNote(note);
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
         	data.endTu(element);
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
             Integer i = attrValAsInteger(element, I);
             if (i == null) {
                 reportTuError(new OtterInputException("<bpt> missing 'i' attribute", 
                               element.getLocation()));
                 i = BeginTag.NO_VALUE;
             }
             BeginTag bpt = new BeginTag(i);
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
             Integer i = attrValAsInteger(element, I);
             if (i == null) {
                 reportTuError(new OtterInputException("<ept> missing 'i' attribute",
                               element.getLocation()));
                 i = EndTag.NO_VALUE;
             }
             EndTag ept = new EndTag(i);
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
                 reportTuError(new OtterInputException("<it> element missing 'pos' attribute", 
                               element.getLocation()));
             }
             else {
                 IsolatedTag.Pos pos = IsolatedTag.Pos.byAttrValue(v);
                 if (pos == null) {
                     reportTuError(new OtterInputException("Invalid value for 'pos' attribute: " + v, 
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
