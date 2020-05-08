 /*
  * Created on Oct 21, 2005
  */
 package uk.org.ponder.rsf.template;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.xmlpull.mxp1.MXParser;
 import org.xmlpull.v1.XmlPullParser;
 
 import uk.org.ponder.arrayutil.ArrayUtil;
 import uk.org.ponder.arrayutil.ListUtil;
 import uk.org.ponder.rsf.renderer.ViewRender;
 import uk.org.ponder.rsf.util.SplitID;
 import uk.org.ponder.rsf.view.ViewTemplate;
 import uk.org.ponder.rsf.view.ViewTemplateParser;
 import uk.org.ponder.stringutil.CharWrap;
 import uk.org.ponder.util.UniversalRuntimeException;
 import uk.org.ponder.xml.XMLUtil;
 
 /**
  * The parser for the IKAT view template, implemented using the XPP3 "rapid" XML
  * pull parser. Returns an XMLViewTemplate, which can be recognised by the
  * {@link ViewRender}, embodying the IKAT algorithm.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class XMLViewTemplateParser implements ViewTemplateParser {
   private XMLViewTemplate t;
 
   public int INITIAL_LUMP_SIZE = 1000;
   private CharWrap buffer;
 
   private List parseinterceptors;
 
   public void setTemplateParseInterceptors(List parseinterceptors) {
     this.parseinterceptors = parseinterceptors;
   }
 
   private void writeToken(int token, XmlPullParser parser, CharWrap w) {
 
     char[] chars = parser.getTextCharacters(limits);
     switch (token) {
     case XmlPullParser.COMMENT:
       w.append("<!--");
       break;
     case XmlPullParser.ENTITY_REF:
       w.append("&");
       break;
     case XmlPullParser.CDSECT:
       w.append("<![CDATA[");
       break;
     case XmlPullParser.PROCESSING_INSTRUCTION:
       w.append("<?");
       break;
     case XmlPullParser.DOCDECL:
       w.append("<!DOCTYPE");
       break;
 
     }
     w.append(chars, limits[0], limits[1]);
     switch (token) {
     case XmlPullParser.COMMENT:
       w.append("-->");
       break;
     case XmlPullParser.ENTITY_REF:
       w.append(";");
       break;
     case XmlPullParser.CDSECT:
       w.append("]]>");
       break;
     case XmlPullParser.PROCESSING_INSTRUCTION:
       w.append("?>");
       break;
     case XmlPullParser.DOCDECL:
       w.append(">");
       break;
     }
   }
 
   private void setLumpChars(XMLLump lump, char[] chars, int start, int length) {
     lump.start = buffer.size;
     lump.length = length;
     if (length > 0) {
       buffer.append(chars, start, length);
     }
   }
 
   private void setLumpString(XMLLump lump, String string) {
     lump.start = buffer.size;
     lump.length = string.length();
     buffer.append(string);
   }
 
   private void simpleTagText(XmlPullParser parser) {
     justended = false;
     char[] chars = parser.getTextCharacters(limits);
     XMLLump newlump = newLump(parser);
     setLumpChars(newlump, chars, limits[0], limits[1]);
     // String text = new String(chars, limits[0], limits[1]);
     // lumps[lumpindex - 1].text = text;
   }
 
   private void processDefaultTag(int token, XmlPullParser parser) {
     justended = false;
     CharWrap w = new CharWrap();
     writeToken(token, parser, w);
     XMLLump newlump = newLump(parser);
     setLumpChars(newlump, w.storage, 0, w.size);
     // lumps[lumpindex - 1].text = w.toString();
   }
 
   private void checkContribute(String id, XMLLump headlump) {
     if (id.startsWith(XMLLump.SCR_CONTRIBUTE_PREFIX)) {
       String scr = id.substring(XMLLump.SCR_CONTRIBUTE_PREFIX.length());
       t.collectmap.addLump(scr, headlump);
     }
   }
 
   private void processTagStart(XmlPullParser parser, boolean isempty) {
     if (justended) {
       // avoid the pathological case where we have for example
       // <td class="tmiblock1" rsf:id="tmiblock:"></td><td> which makes it
       // hard to spot run ends on the basis of recursion uncession.
       justended = false;
       XMLLump backlump = newLump(parser);
       backlump.nestingdepth--;
       setLumpChars(backlump, null, 0, 0);
     }
     XMLLump headlump = newLump(parser);
     XMLLump stacktop = getStackTop();
     headlump.uplump = stacktop;
     
     if (t.roottagindex == -1)
       t.roottagindex = headlump.lumpindex;
     String tagname = parser.getName();
     // standard text of |<tagname | to allow easy identification.
     setLumpString(headlump, XMLLump.tagToText(tagname));
     // HashMap forwardmap = new HashMap();
     // headlump.forwardmap = forwardmap;
     // current policy - every open tag gets a forwardmap, and separate lumps.
     // eventually we only want a lump where there is an rsf:id.
     int attrs = parser.getAttributeCount();
     headlump.attributemap = new HashMap(attrs < 3 ? (attrs + 1) * 2
         : attrs * 2);
 
     for (int i = 0; i < attrs; ++i) {
       String attrname = parser.getAttributeName(i);
       String attrvalue = parser.getAttributeValue(i);
       headlump.attributemap.put(attrname, attrvalue);
     }
     try {
     if (parseinterceptors != null) {
       for (int i = 0; i < parseinterceptors.size(); ++i) {
         TemplateParseInterceptor parseinterceptor = (TemplateParseInterceptor) parseinterceptors
             .get(i);
         parseinterceptor.adjustAttributes(tagname, headlump.attributemap);
       }
     }
     }
     catch (Exception e) {
       throw UniversalRuntimeException.accumulate(e, "Error processing tag " + headlump);
     }
     attrs = headlump.attributemap.size(); // TPI may have changed it
     if (headlump.attributemap.isEmpty()) {
       headlump.attributemap = null;
     }
     else {
       boolean firstattr = true;
       for (Iterator keyit = headlump.attributemap.keySet().iterator(); keyit
           .hasNext();) {
         String attrname = (String) keyit.next();
         String attrvalue = (String) headlump.attributemap.get(attrname);
 
         if (attrname.equals(XMLLump.ID_ATTRIBUTE)) {
           --attrs; // reduce count which is kept for close tag accounting below
           String ID = attrvalue;
           if (ID.startsWith(XMLLump.FORID_PREFIX)
               && ID.endsWith(XMLLump.FORID_SUFFIX)) {
             ID = ID.substring(0, ID.length() - XMLLump.FORID_SUFFIX.length());
           }
           checkContribute(ID, headlump);
           headlump.rsfID = ID;
 
           XMLLump downreg = findTopContainer(ID);
           if (downreg.downmap == null) {
             downreg.downmap = new XMLLumpMMap(); // to handle payload-component case
           }
           downreg.downmap.addLump(ID, headlump);
       
           t.globalmap.addLump(ID, headlump);
 
           SplitID split = new SplitID(ID);
 
           if (split.prefix.equals(XMLLump.FORID_PREFIX)) {
             // no special note, just prevent suffix logic.
           }
           // we need really to be able to locate 3 levels of id -
           // for-message:message:to
           // ideally we would also like to be able to locate repetition
           // constructs too, hopefully the standard suffix-based computation
           // will allow this. However we previously never allowed BOTH
           // repetitious and non-repetitious constructs to share the same
           // prefix, so revisit this to solve.
           // }
           else if (split.suffix != null) {
             // a repetitive tag is found.
             // Repetitions within a SCOPE should be UNIQUE and CONTIGUOUS.
             //XMLLump prevlast = stacktop.getFinal(split.prefix);
             stacktop.setFinal(split.prefix, headlump);
           }
         }
         else { // is not rsf:id attribute
           XMLLump frontlump = newLump(parser);
           CharWrap lumpac = new CharWrap();
           if (!firstattr) {
             lumpac.append("\" ");
           }
           firstattr = false;
           lumpac.append(attrname).append("=\"");
           setLumpChars(frontlump, lumpac.storage, 0, lumpac.size);
           // frontlump holds |" name="|
           // valuelump just holds the value.
 
           XMLLump valuelump = newLump(parser);
           setLumpString(valuelump, XMLUtil.encode(attrvalue));
         }
       } // end for each attribute
     }
     XMLLump finallump = newLump(parser);
 
     String closetext = attrs == 0 ? (isempty ? "/>"
         : ">")
         : (isempty ? "\"/>"
             : "\">");
     setLumpString(finallump, closetext);
     headlump.open_end = finallump;
 
     tagstack.add(nestingdepth, headlump);
     if (isempty) {
       processTagEnd(parser);
     }
   }
 
   boolean justended;
 
   private void processTagEnd(XmlPullParser parser) {
     // String tagname = parser.getName();
     XMLLump oldtop = tagstack.lumpAt(nestingdepth);
 
     oldtop.close_tag = t.lumps[lumpindex - 1];
     tagstack.remove(nestingdepth);
     justended = true;
   }
   
   private XMLLump getStackTop() {
     XMLLump togo = (XMLLump) ListUtil.peek(tagstack);
     return togo == null? t.rootlump : togo;
   }
   
   private XMLLump findTopContainer(String id) {
     for (int i = tagstack.size() - 1; i >= 0; --i) {
       XMLLump lump = tagstack.lumpAt(i);
      if (lump.rsfID != null && SplitID.isSplit(lump.rsfID))
         return lump;
     }
     return t.rootlump;
   }
 
   // temporary array for getCharacterText
   private int[] limits = new int[2];
   private int lumpindex = 0;
   private int nestingdepth = 0;
   // only stores repetitive tags.
   private XMLLumpList tagstack = new XMLLumpList();
 
   private XMLLump newLump(XmlPullParser parser) {
     if (lumpindex == t.lumps.length) {
       t.lumps = (XMLLump[]) ArrayUtil.expand(t.lumps, 2.0);
     }
     XMLLump togo = new XMLLump(lumpindex, nestingdepth);
     togo.line = parser.getLineNumber();
     togo.column = parser.getColumnNumber();
     togo.parent = t;
     t.lumps[lumpindex] = togo;
 
     ++lumpindex;
     return togo;
   }
 
   // XPP tag depths:
   // <!-- outside --> 0
   // <root> 1
   // sometext 1
   // <foobar> 2
   // </foobar> 2
   // </root> 1
   // <!-- outside --> 0
 
   public void init() {
     t = new XMLViewTemplate();
     t.lumps = new XMLLump[INITIAL_LUMP_SIZE];
     buffer = new CharWrap(INITIAL_LUMP_SIZE * 10);
     lumpindex = 0;
     tagstack.clear();
     t.rootlump = new XMLLump();
     t.rootlump.downmap = new XMLLumpMMap();
     t.rootlump.nestingdepth = -1;
     t.rootlump.parent = t;
     t.roottagindex = -1;
     t.collectmap = new XMLLumpMMap();
     justended = false;
   }
 
   public ViewTemplate parse(InputStream xmlstream) {
     // long time = System.currentTimeMillis();
     init();
     XmlPullParser parser = new MXParser();
     try {
       // parser.setFeature(FEATURE_XML_ROUNDTRIP, true);
       parser.setInput(xmlstream, "UTF-8");
       while (true) {
         int token = parser.nextToken();
         if (token == XmlPullParser.END_DOCUMENT)
           break;
         // currently 1 lump for each token - an optimisation would collapse
         // provable irrelevant lumps. but watch out for end tags! Some might
         // be fused, some not.
         nestingdepth = parser.getDepth() - 1;
 
         switch (token) {
         case XmlPullParser.START_TAG:
           boolean isempty = parser.isEmptyElementTag();
           processTagStart(parser, isempty);
           if (isempty) {
             parser.next();
           }
           break;
         case XmlPullParser.END_TAG:
           simpleTagText(parser);
           processTagEnd(parser);
           break;
         default:
           processDefaultTag(token, parser);
         }
       }
 
     }
     catch (Throwable t) {
       throw UniversalRuntimeException.accumulate(t, "Error parsing template");
     }
     endParse();
     return t;
     // Logger.log.info("Template parsed in " + (System.currentTimeMillis() -
     // time) + "ms");
   }
 
   private void endParse() {
     t.lumps = (XMLLump[]) ArrayUtil.trim(t.lumps, lumpindex);
     tagstack.clear();
     char[] compacted = new char[buffer.size];
     System.arraycopy(buffer.storage, 0, compacted, 0, buffer.size);
     t.buffer = compacted;
     buffer = null;
   }
 
 }
