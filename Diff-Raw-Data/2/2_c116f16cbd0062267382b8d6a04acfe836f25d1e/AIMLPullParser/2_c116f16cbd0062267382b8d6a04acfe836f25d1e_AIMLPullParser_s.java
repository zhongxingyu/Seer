 package aiml.parser;
 
 /**
  * <p>Title: AIML Pull Parser</p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2006</p>
  * @author Kim Sullivan
  * @version 1.0
  */
 import java.io.*;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.*;
 
 import junit.framework.*;
 import junit.textui.TestRunner;
 
 import org.xmlpull.v1.*;
 
 public class AIMLPullParser implements XmlPullParser {
   private Reader in;
   private String encoding;
   char ch; //the current character in the input
   private HashMap<String, String> entityReplacementText = new HashMap<String, String>();
 
   class Attribute {
     String name;
     String value;
     boolean isdefault = false;
     String type = "CDATA";
     Attribute(String name, String value) {
       assert (name != null && !name.equals("")) : "Name must not be empty";
       assert (value != null) : "Value must not be null";
       this.name = name;
       this.value = value;
     }
     String getNamespace() {
       return "";
     }
     String getName() {
       return name;
     }
     String getPrefix() {
       return null;
     }
     String getType() {
       return type;
     }
     boolean isDefault() {
       return isdefault;
     }
     String getValue() {
       return value;
     }
   }
   private HashMap<String, Attribute> attributeMap = new HashMap<String, Attribute>();
   private ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
 
   private boolean readCR;
   private int lineNumber;
   private int colNumber;
   private int depth;
   private int eventType;
   private boolean isEmptyElemTag;
   private boolean isWhitespace;
   private boolean processNamespaces;
   private String name;
   private String refName;
   private String text;
   private Boolean isStandalone;
   private String encodingDeclared;
   private boolean xmlDeclParsed;
   
   private enum InternalState{
     DOCUMENT_START,
     PROLOG,
     CONTENT,
     EPILOG,
     DOCUMENT_END;
   }
   private InternalState internalState;
   private int markedLineNumber;
   private int markedColNumber;
   private boolean isMarked;
   private char markedCharacter;
   private boolean markedReadCR;
   private boolean readDocdecl;
 
   public static final char EOF = '\uFFFF';
   public static final char CR = '\r';
   public static final char LF = '\n';
   public static final char QUOT = '"';
   public static final char APOS = '\'';
   public static final char AMP = '&';
   public static final char HASH = '#';
   public static final char X = 'x';
   public static final char LT = '<';
   public static final char GT = '>';
   public static final char EXCL = '!';
   public static final char QUES = '?';
   public static final char EQ = '=';
   public static final char SEMICOLON = ';';
   public static final char DASH = '-';
   public static final char RAB = ']';
   public static final char LAB = '[';
   public static final char SLASH = '/';
 
   public AIMLPullParser() {
     resetState();
   }
 
   public void setFeature(String name, boolean state) throws XmlPullParserException {
     if (eventType != START_DOCUMENT)
       throw new XmlPullParserException("Features can only be set before the first call to next or nextToken");
     if (name.equals(FEATURE_PROCESS_NAMESPACES))
       processNamespaces=state;
     else if(state)
       throw new XmlPullParserException("Feature " + name + " can't be activated");
   }
   public boolean getFeature(java.lang.String name) {
     if (name.equals(FEATURE_PROCESS_NAMESPACES))
       return processNamespaces;
     return false;
   }
   public void setProperty(String name, Object value) throws XmlPullParserException {
     if (name == null)
       throw new IllegalArgumentException("Property name cannot be null");
     throw new XmlPullParserException("Property " + name + " not supported");
   }
   public Object getProperty(String name) {
     if (name.equals("http://xmlpull.org/v1/doc/properties.html#xmldecl-version") && xmlDeclParsed) {
       return "1.0";
     }
     if (name.equals("http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone")) {
       return isStandalone;
     }
     
     return null;
   }
   public void setInput(java.io.Reader in) {
     resetState();
     if (in!=null)
       this.in = new BufferedReader(in);
     else
       this.in=null;
   }
   public void setInput(java.io.InputStream inputStream, java.lang.String inputEncoding) throws XmlPullParserException {
     resetState();
     if (inputStream==null)
       throw new IllegalArgumentException("Input stream must not be null");
     try {
       InputStreamReader isr;
       if (inputEncoding != null) {
         isr = new InputStreamReader(inputStream,inputEncoding);
         encoding = inputEncoding; //isr.getEncoding();
       } else {
         isr = new InputStreamReader(inputStream);
       }
       in = new BufferedReader(isr);
     } catch (UnsupportedEncodingException e) {
       throw new XmlPullParserException("Unsupported encoding",null,e);
     }
   }
   public String getInputEncoding() {
     return encoding;
   }
   public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
     if (entityReplacementText.containsKey(entityName))
       throw new XmlPullParserException("Cannot redefine entity replacement text");
     entityReplacementText.put(entityName,replacementText);
   }
   public int getNamespaceCount(int depth) throws XmlPullParserException {
     return 0;
   }
   public String getNamespacePrefix(int pos) throws XmlPullParserException {
     return null;
   }
   public String getNamespaceUri(int pos) throws XmlPullParserException {
     return null;
   }
   public String getNamespace(String prefix) {
     switch (eventType) {
       case START_TAG:
       case END_TAG:
         return NO_NAMESPACE;
       default:
         return null;
     }
   }
   public int getDepth() {
     return depth;
   }
   public String getPositionDescription() {
     return "@" + getLineNumber() + ":" + getColumnNumber();
   }
   public int getLineNumber() {
     return lineNumber;
   }
   public int getColumnNumber() {
     return colNumber;
   }
   public boolean isWhitespace() throws XmlPullParserException {
     switch(eventType) {
       case IGNORABLE_WHITESPACE:
         return true;
       case TEXT:
       case CDSECT:
         return isWhitespace;
       default:
         throw new XmlPullParserException("Whitespace can only be queried for ignorable whitespace, text and cdata sections");
     }
     
   }
   public String getText() {
     switch (eventType) {
       case START_DOCUMENT:
       case END_DOCUMENT:
       case START_TAG:
       case END_TAG:
       case DOCDECL:
         return null;
       default:
         return text;
     }
   }
   public char[] getTextCharacters(int[] holderForStartAndLength) {
     switch (eventType) {
       case START_DOCUMENT:
       case END_DOCUMENT:
       case START_TAG:
       case END_TAG:
       case DOCDECL:
         return null;
       case ENTITY_REF:
        return getTextCharacters(name,holderForStartAndLength);
       default:
         return getTextCharacters(text,holderForStartAndLength);
     }
   }
   public String getNamespace() {
     switch (eventType) {
       case START_TAG:
       case END_TAG:
         return "";
       default:
         return null;
     }
   }
   public String getName() {
     switch (eventType) {
       case START_TAG:
       case END_TAG:
         return name;
       case ENTITY_REF:
         return refName;
       default:
         return null;
     }
   }
   public String getPrefix() {
     return null;
   }
   public boolean isEmptyElementTag() throws XmlPullParserException {
     if (eventType == START_TAG)
       return isEmptyElemTag;
     else
       throw new XmlPullParserException("The function isEmptyElementTag() can only be called for start-tags");
   }
   public int getAttributeCount() {
     if (eventType == START_TAG)
       return attributeList.size();
     else
       return -1;
   }
   public String getAttributeNamespace(int index) {
     if (eventType != START_TAG)
       throw new IndexOutOfBoundsException();
     return attributeList.get(index).getNamespace();
   }
   public String getAttributeName(int index) {
     if (eventType != START_TAG)
       throw new IndexOutOfBoundsException();
     return attributeList.get(index).getName();
   }
   public String getAttributePrefix(int index) {
     if (eventType != START_TAG)
       throw new IndexOutOfBoundsException();
     return attributeList.get(index).getPrefix();
   }
   public String getAttributeType(int index) {
     return attributeList.get(index).getType();
   }
   public boolean isAttributeDefault(int index) {
     return attributeList.get(index).isDefault();
   }
   public String getAttributeValue(int index) {
     if (eventType != START_TAG)
       throw new IndexOutOfBoundsException();
     return attributeList.get(index).getValue();
   }
   public String getAttributeValue(String namespace, String name) {
     assert (namespace == null) : "Namespaces not supported";
     if (eventType != START_TAG)
       throw new IndexOutOfBoundsException();
     if (attributeMap.containsKey(name))
       return attributeMap.get(name).getValue();
     else
       return null;
   }
   public int getEventType() throws XmlPullParserException {
     return eventType;
   }
   public int next() throws IOException, XmlPullParserException {
     if (in==null)
       throw new XmlPullParserException("Input must not be null");
     StringBuffer textBuffer = new StringBuffer();
 TextLoop:
     do {
       switch (internalState) {
         case DOCUMENT_START:
           nextChar();
           tryXmlDecl();
           internalState=InternalState.PROLOG;
         case PROLOG:  
           if (CharacterClasses.isS(ch)) {
             nextS();
             eventType=IGNORABLE_WHITESPACE;
             internalState=InternalState.PROLOG;
             continue TextLoop;
           } else if (ch=='<') {
             nextChar();
             nextMarkupContent();
             switch (eventType) {
               case CDSECT:
               case END_TAG:
                 throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document prolog",this,null);
               case START_TAG:
                 internalState=InternalState.CONTENT;
                 return eventType;
               case DOCDECL:
               case PROCESSING_INSTRUCTION:
               case COMMENT:
                 continue TextLoop;
               default:
                 throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document prolog",this,null);
             }
           } else if(ch==EOF){
             throw new EOFException("Unexpected end of file inside XML prolog");
           } else { 
             throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document prolog",this,null);
           }
         case CONTENT:
           if (eventType==START_TAG && isEmptyElemTag) { //special handling for empty elements
             eventType=END_TAG;
             return eventType;
           }
           if (eventType==END_TAG) depth--;
           switch (ch) {
             case '<':
               markInput(2);
               nextChar();
               switch (ch) {
                 case '!':
                 case '?':
                   unmarkInput();
                   eventType=TEXT;
                   nextMarkupContent();
                   if (eventType==CDSECT) {
                     textBuffer.append(text);                    
                   }
                   continue TextLoop;
                 default:
                   if (textBuffer.length()>0) {
                     resetInput();
                     eventType=TEXT;
                     text=textBuffer.toString();
                     return eventType;
                   }
                   nextMarkupContent();
                   if (eventType==END_TAG && depth==1) internalState=InternalState.EPILOG;
                   return eventType;
               }
             
             case '&':
               text=nextReference();
               if (text==null)
                 throw new XmlPullParserException("Unknown reference '" + refName + "' encountered",this,null);
               else
                 textBuffer.append(text);
               eventType=TEXT;
               continue TextLoop;
             case EOF:
               if (depth==0) {
                 internalState=InternalState.DOCUMENT_END;
                 eventType=END_DOCUMENT;
                 return eventType;
               }
               throw new EOFException("Unexpected end of file inside ROOT element");
             default:
               textBuffer.append(nextCharData());
               eventType=TEXT;
               continue TextLoop;
           }
         case EPILOG:
           if (eventType==END_TAG) depth--;
           if (CharacterClasses.isS(ch)) {
             text=nextS();
             eventType=IGNORABLE_WHITESPACE;
             continue TextLoop;
           } else if (ch=='<') {
             nextChar();
             if (eventType!=TEXT) {
               nextMarkupContent();
               switch (eventType) {
                 case CDSECT:
                 case END_TAG:
                 case START_TAG:  
                 case DOCDECL:
                   throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document epilog",this,null);
                 case PROCESSING_INSTRUCTION:
                 case COMMENT:
                   continue TextLoop;
                 default:
                   throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document epilog",this,null);
               }
             }
           } else if(ch==EOF){
             internalState=InternalState.DOCUMENT_END;
             eventType=END_DOCUMENT;
             return eventType;
           } else { 
             throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document prolog",this,null);
           }
         case DOCUMENT_END:
           return END_DOCUMENT;
         default:
           throw new XmlPullParserException("Inconsistent parser state, please reset input");
       }
     } while (true);
   }
   public int nextToken() throws IOException, XmlPullParserException {
     if (in==null)
       throw new XmlPullParserException("Input must not be null");
     switch (internalState) {
       case DOCUMENT_START:
         nextChar();
         tryXmlDecl();
         internalState=InternalState.PROLOG;
       case PROLOG:  
         if (CharacterClasses.isS(ch)) {
           text=nextS();
           eventType=IGNORABLE_WHITESPACE;
           internalState=InternalState.PROLOG;
           return eventType;
         } else if (ch=='<') {
           nextChar();
           nextMarkupContent();
           switch (eventType) {
             case CDSECT:
             case END_TAG:
               throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document prolog",this,null);
             case START_TAG:
               internalState=InternalState.CONTENT;
               return eventType;
             case DOCDECL:
             case PROCESSING_INSTRUCTION:
             case COMMENT:
               return eventType;
             default:
               throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document prolog",this,null);
           }
         } else if(ch==EOF){
           throw new EOFException("Unexpected end of file inside XML prolog");
         } else { 
           throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document prolog",this,null);
         }
       case CONTENT:
         if (eventType==START_TAG && isEmptyElemTag) { //special handling for empty elements
           eventType=END_TAG;
           return eventType;
         }
         if (eventType==END_TAG) depth--;
         switch (ch) {
           case '<':
             nextChar();
             nextMarkupContent();
             if (eventType==END_TAG && depth==1) internalState=InternalState.EPILOG;
             return eventType;
           case '&':
             text=nextReference();
             eventType=ENTITY_REF;
             return eventType;
           case EOF:
             if (depth==0) {
               internalState=InternalState.DOCUMENT_END;
               eventType=END_DOCUMENT;
               return eventType;
             }
             throw new EOFException("Unexpected end of file inside ROOT element");
           default:
             text=nextCharData();
             eventType=TEXT;
             return eventType;
         }
       case EPILOG:
         if (eventType==END_TAG) depth--;
         if (CharacterClasses.isS(ch)) {
           text=nextS();
           eventType=IGNORABLE_WHITESPACE;
           return eventType;
         } else if (ch=='<') {
           nextChar();
           nextMarkupContent();
           switch (eventType) {
             case CDSECT:
             case END_TAG:
             case START_TAG:  
             case DOCDECL:
               throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document epilog",this,null);
             case PROCESSING_INSTRUCTION:
             case COMMENT:
               return eventType;
             default:
               throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document epilog",this,null);
           }
         } else if(ch==EOF){
           internalState=InternalState.DOCUMENT_END;
           eventType=END_DOCUMENT;
           return eventType;
         } else { 
           throw new XmlPullParserException("Syntax error, only comments, processing instructions and whitespace allowed in document prolog",this,null);
         }
       case DOCUMENT_END:
         return END_DOCUMENT;
       default:
         throw new XmlPullParserException("Inconsistent parser state, please reset input");
     }
   }
   public void require(int type, String namespace, String name) throws IOException, XmlPullParserException {
     if (type != getEventType()
         || (namespace != null &&  !namespace.equals( getNamespace () ) )
         || (name != null &&  !name.equals( getName() ) ) )
            throw new XmlPullParserException( "expected "+ TYPES[ type ]+getPositionDescription());
   }
   public String nextText() throws IOException, XmlPullParserException {
     if(getEventType() != START_TAG) {
       throw new XmlPullParserException(
         "parser must be on START_TAG to read next text", this, null);
    }
    next();
    if(eventType == TEXT) {
       String result = getText();
       next();
       if(eventType != END_TAG) {
         throw new XmlPullParserException(
            "event TEXT must be immediately followed by END_TAG", this, null);
        }
        return result;
    } else if(eventType == END_TAG) {
       return "";
    } else {
       throw new XmlPullParserException(
         "parser must be on START_TAG or TEXT to read text", this, null);
    }  }
   public int nextTag() throws IOException, XmlPullParserException {
     next();
     if(eventType == TEXT &&  isWhitespace()) {   // skip whitespace
        next();
     }
     if (eventType != START_TAG &&  eventType != END_TAG) {
        throw new XmlPullParserException("expected start or end tag", this, null);
     }
     return eventType;
   }
 
   private static char[] getTextCharacters(String s, int[] holderForStartAndLength) {
     holderForStartAndLength[0] = 0;
     holderForStartAndLength[1] = s.length();
     char[] result = new char[s.length()];
     s.getChars(0,s.length(),result,0);
     return result;
   }
 
   private char getChar() {
     return ch;
   }
   
   private void markInput(int readAheadLimit) throws IOException {
     assert(in.markSupported()) : "Mark operation must be supported";
     in.mark(readAheadLimit);
     markedLineNumber=lineNumber;
     markedColNumber=colNumber;
     markedReadCR=readCR;
     isMarked=true;
     markedCharacter=ch;
   }
   
   private void resetInput() throws IOException {
     assert(isMarked) : "Cannot reset unmarked input";
     in.reset();
     lineNumber=markedLineNumber;
     colNumber=markedColNumber;
     ch=markedCharacter;
     isMarked=false;
     readCR=markedReadCR;
   }
   private void unmarkInput() {
     isMarked=false;
   }
   
   private void resetState() {
     lineNumber = 1;
     colNumber = 0;
     depth = 0;
     readCR = false;
     setDefaultEntityReplacementText();
     attributeMap.clear();
     attributeList.clear();
     in = null;
     encoding = null;
     eventType = START_DOCUMENT;
     isStandalone=null;
     encodingDeclared=null;
     xmlDeclParsed=false;
     name=null;
     text=null;
     refName=null;
     internalState=InternalState.DOCUMENT_START;
     isWhitespace=false;
     processNamespaces=false;
     isMarked=false;
     readDocdecl=false;
   }
   private void setDefaultEntityReplacementText() {
     entityReplacementText.clear();
     try {
       defineEntityReplacementText("amp","&");
       defineEntityReplacementText("lt","<");
       defineEntityReplacementText("gt",">");
       defineEntityReplacementText("quot","\"");
       defineEntityReplacementText("apos","'");
     } catch (XmlPullParserException e) {
     };
   }
 
   private void requireChar(char what, String failMessage) throws XmlPullParserException, IOException {
     if (ch != what)
       throw new XmlPullParserException(failMessage,this,null);
     nextChar();
   }
   private void requireString(String what, String failMessage) throws XmlPullParserException, IOException {
     for (int i = 0; i < what.length(); i++) {
       if (ch != what.charAt(i))
         throw new XmlPullParserException(failMessage,this,null);
       nextChar();
     }
   }
 
   private void skipS() throws XmlPullParserException, IOException {
     while (CharacterClasses.isS(ch))
       nextChar();
   }
 
   private char nextChar() throws IOException {
     ch = (char) in.read();
     colNumber++;
     switch (ch) { //normalize end of line markers and count the position
       case LF:
         if (readCR) { // Processing CRLF, so silently skip the LF
           ch = (char) in.read();
           if (ch==CR) {
             ch = LF;
             lineNumber++;
             colNumber = 0;            
           } else
             readCR=false;
         } else {
           lineNumber++;
           colNumber = 0;
           readCR = false;
         }
         break;
       case CR:
         ch = LF;
         lineNumber++;
         colNumber = 0;
         readCR = true;
         break;
       default:
         readCR = false;
     }
 
     return ch;
   }
   private String nextS() throws XmlPullParserException, IOException {
     //[3]   	S	   ::=   	(#x20 | #x9 | #xD | #xA)+
     StringBuffer result = new StringBuffer();
     if (!CharacterClasses.isS(ch))
       throw new XmlPullParserException("Syntax error, expecting whitespace",this,null);
     do {
       result.append(ch);
       nextChar();
     } while (CharacterClasses.isS(ch));
     return result.toString();
   }
 
   private String nextName() throws XmlPullParserException, IOException {
     //[5]   	Name	   ::=   	(Letter | '_' | ':') (NameChar)*
     if (!CharacterClasses.isNameFirst(ch))
       throw new XmlPullParserException("Syntax error, expecting production\n[5]   	Name	   ::=   	(Letter | '_' | ':') (NameChar)*",this,null);
     StringBuffer result = new StringBuffer();
     result.append(ch);
     while ((nextChar() != EOF) && CharacterClasses.isNameChar(ch))
       result.append(ch);
     return result.toString();
   }
   private void nextEq() throws XmlPullParserException, IOException {
     // [25]   	Eq	   ::=   	S? '=' S?
     if (!CharacterClasses.isS(ch) && ch != EQ)
       throw new XmlPullParserException("Syntax error, expecting production\n[25]   	Eq	   ::=   	S? '=' S?",this,null);
     skipS();
     requireChar(EQ,"Syntax error, expecting production\n[25]   	Eq	   ::=   	S? '=' S?");
     skipS();
   }
   private String nextReference() throws XmlPullParserException, IOException {
     //[67]   	Reference	   ::=   	EntityRef | CharRef
     requireChar(AMP,"Syntax error, production [67] Referencee must start with &");
     StringBuffer result = new StringBuffer();
     String name;
     if (CharacterClasses.isNameFirst(ch)) { //[68]   	EntityRef	   ::=   	'&' Name ';'
       name = nextName();
       if (entityReplacementText.containsKey(name))
         result.append(entityReplacementText.get(name));
       else
         result=null;
     } else if (ch == HASH) {//[66]   	CharRef	   ::=   	'&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
       nextChar();
       int radix;
       StringBuffer codepointBuffer = new StringBuffer();
       if (ch == X) {//[66]   	CharRef	   ::=   	'&#x' [0-9a-fA-F]+ ';'
         radix = 16;
         name = "#x";
         nextChar();
         do {
           if (CharacterClasses.isHexDigit(ch))
             codepointBuffer.append(ch);
           else
             throw new XmlPullParserException("Syntax error, invalid hexadecimal digit '" + ch + "' in character reference",this,null);
           nextChar();
         } while (CharacterClasses.isHexDigit(ch));
       } else {//[66]   	CharRef	   ::=   	'&#' [0-9]+ ';'
         radix = 10;
         name = "#";
         do {
           if (CharacterClasses.isDecDigit(ch))
             codepointBuffer.append(ch);
           else
             throw new XmlPullParserException("Syntax error, invalid decimal digit '" + ch + "' in character reference",this,null);
           nextChar();
         } while (CharacterClasses.isDecDigit(ch));
       }
       int codepoint;
       try {
         name = name + codepointBuffer.toString();
         codepoint = Integer.parseInt(codepointBuffer.toString(),radix);
       } catch (NumberFormatException e) {
         throw new XmlPullParserException("Syntax error, bad character reference '" + codepointBuffer + "'",this,null);
       }
       result.appendCodePoint(codepoint);
     } else {
       throw new XmlPullParserException("Syntax error, bad entity reference",this,null);
     }
 
     requireChar(SEMICOLON,"Syntax error, production [67] Reference must end with ';'");
     refName=name;
     if (result==null)
       return null;
     else
       return result.toString();
   }
 
   private String nextAttValue() throws XmlPullParserException, IOException {
     //[10]   	AttValue	   ::=   	'"' ([^<&"] | Reference)* '"' |  "'" ([^<&'] | Reference)* "'"
     if ((ch != QUOT) && (ch != APOS)) {
       //System.out.println("((["+ch+"]!=["+QUOT+"]) || ([["+ch+"]!=["+APOS+"]))");
       throw new XmlPullParserException("Syntax error, attribute value must begin with quote or apostrophe",this,null);
     }
     char delim = ch;
     StringBuffer result = new StringBuffer();
     nextChar();
     do {
 
       if (ch == delim) {
         nextChar();
         return result.toString();
       }
       if (CharacterClasses.isS(ch)) {
         result.append('\u0020');
         nextChar();
         continue;
       }
       switch (ch) {
         case LT:
           throw new XmlPullParserException("Syntax error, character '<' not allowed in attribute value",this,null);
         case AMP:
           String replacement = nextReference();
           
           /* Weeelll... the specification states that
            * "The replacement text of any entity referred to directly or 
            * indirectly in an attribute value MUST NOT contain a <"
            * http://www.w3.org/TR/REC-xml/#CleanAttrVals
            * (and it's been like that since the second revision).
            * BUT, the test cases assume that the character < can be included 
            * in attribute values via references... who am I to argue?
            */
           /*
           if (replacement.contains("<"))
             throw new XmlPullParserException("Syntax error, character '<' not allowed in attribute value",this,null);
           */  
           result.append(replacement);
           continue;
         default:
           result.append(ch);
       }
       nextChar();
     } while (true);
 
   }
 
   private void nextAttribute() throws XmlPullParserException, IOException {
     String name = nextName();
     nextEq();
     String value = nextAttValue();
     if (attributeMap.containsKey(name))
       throw new XmlPullParserException("Violation of WFC: Unique Att Spec (An attribute name MUST NOT appear more than once in the same start-tag or empty-element tag.)",this,null);
     Attribute a = new Attribute(name,value);
     attributeMap.put(name,a);
     attributeList.add(a);
   }
 
   private String nextPIContent() throws XmlPullParserException, IOException {
     //[16]   	PI	   ::=   	'<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'
     //assumes we already have parsed '<?'PITarget and are on the character after that
     if (CharacterClasses.isS(ch)) {
       //(S (Char* - (Char* '?>' Char*)))
       /*This is a bit tricky. The notation says we're looking for strings that begin with
        * whitespace, and DON'T contain the '?>' marker. What we have to do is the opposite:
        * actually LOOK for the marker. The translational grammar for it looks like this:
        * S ::= '?' A | '>' S {out('>')} | C S {out(C)}
        * A ::= '?' A {out('?')} | '>' {break} | C S {out('?') out (C)}
        * C ::= Char - ('>' | '?')
        */
       StringBuffer result = new StringBuffer();
       boolean seenQ = false;
 PIContent:
       do {
         if (!CharacterClasses.isChar(ch)) {
           if (ch == EOF)
             throw new EOFException("Unexpected end of input while parsing PI");
           else
             throw new XmlPullParserException("Syntax error, invalid character while parsing PI",this,null);
         }
         if (!seenQ) {
           //S ::= '?' A | '>' S {out('>')} | C S {out(C)}
           if (ch == QUES)
             seenQ = true;
           else
             result.append(ch);
         } else {
           //A ::= '?' A {out('?')} | '>' {break} | C S {out('?') out (C)}
           switch (ch) {
             case QUES:
               result.append('?');
               break; //what we're outputting here is not this '?' but the one before that
             case GT:
               break PIContent; //a simple break would just terminate the switch, not the do {} while block.
             default:
               result.append('?').append(ch);
               seenQ = false;
           }
         }
         nextChar();
       } while (true);
       nextChar();
       return result.toString();
     } else {
       //'?>'
       requireChar(QUES,"Syntax error, in production [16] PI: PITarget must be followed by whitespace, or immediately terminated with '?>'");
       requireChar(GT,"Syntax error, in production [16] PI: PITarget must be followed by whitespace, or immediately terminated with '?>'");
       return "";
     }
 
   }
 
   private String nextCommentContent() throws IOException, XmlPullParserException {
     //[15]   	Comment	   ::=   	'<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
     //Assumes we already read '<!--'
     //As with PI's we're actually looking for the terminating '--'
     //(notice that the gramar doesn't allow the string '--' except as the terminating '-->'
     /*
      * S ::= '-' A | C S {out(C)}
      * A ::= '-' {break} | C S {out('-') out (C)}
      * C ::= Char - ('-')
      */
     StringBuffer result = new StringBuffer();
     boolean seenDash = false;
     do {
       if (!CharacterClasses.isChar(ch)) {
         if (ch == EOF)
           throw new EOFException("Unexpected end of input while parsing Comment");
         else
           throw new XmlPullParserException("Syntax error, invalid character while parsing Comment",this,null);
       }
       if (!seenDash) {
         if (ch == DASH)
           seenDash = true;
         else
           result.append(ch);
       } else {
         if (ch == DASH)
           break;
         else {
           result.append('-').append(ch);
           seenDash = false;
         }
       }
       nextChar();
     } while (true);
     nextChar();
     requireChar(GT,"Syntax error, comment must be terminated with '-->'");
     return result.toString();
   }
 
   private String nextCDataContent() throws IOException, XmlPullParserException {
     //[20]   	CData	   ::=   	(Char* - (Char* ']]>' Char*))
     //[21]   	CDEnd	   ::=   	']]>'
     //Assumes we already read '<![CDATA['
     //So, we're looking for the terminating ']]>'
     /* S::= ']' A | '>' S {out('>')} | C S {out(C)}
      * A::= ']' B | '>' S {out(']') out('>')} | C S {out(']') out(C)}
      * B::= ']' B {out(']')} | '>' S {break} | C S {out(']') out(']') out(C)}
      */
     StringBuffer result = new StringBuffer();
     int seenRAB = 0;
     isWhitespace=true;
 CDContent:
     do {
       if (!CharacterClasses.isChar(ch)) {
         if (ch == EOF)
           throw new EOFException("Unexpected end of input while parsing Comment");
         else
           throw new XmlPullParserException("Syntax error, invalid character while parsing Comment",this,null);
       }
       switch (seenRAB) {
         case 0:
           if (ch == RAB) {
             seenRAB = 1;
           } else {
             if (isWhitespace && !CharacterClasses.isS(ch))
               isWhitespace=false;
             result.append(ch);
           }
           break;
         case 1:
           if (ch == RAB) {
             seenRAB = 2;
           } else {
             seenRAB = 0;
             if (isWhitespace)
               isWhitespace=false;
             result.append(']').append(ch);
           }
           break;
         case 2:
           switch (ch) {
             case RAB:
               if (isWhitespace)
                 isWhitespace=false;
               result.append(']');
               break;
             case GT:
               break CDContent;
             default:
               seenRAB = 0;
               if (isWhitespace)
                 isWhitespace=false;
               result.append(']').append(']').append(ch);
           }
           break;
       }
       nextChar();
     } while (true);
     nextChar();
     return result.toString();
   }
 
   private String nextCharData() throws IOException, XmlPullParserException {
     //[14]   	CharData	   ::=   	[^<&]* - ([^<&]* ']]>' [^<&]*)
     //It is interesting to note, that, while the characters '<' and '&' do not
     //belong into this production they only signal the end of it, while the
     //CDATA-section-close delimiter ']]>' actually signals a syntax error.
     /* S::= ']' A | '>' S {out('>')} | C S {out(C)} | '&' {break} | '<' {break}
      * A::= ']' B | '>' S {out(']') out('>')} | C S {out(']') out(C)} | '&' {out(']') break} | '<' {out(']') break}
      * B::= ']' B {out(']')} | '>' S {error} | C S {out(']') out(']') out(C)} | '&' {out(']') out(']') break} | '<' {out(']') out(']') break}
      */
     StringBuffer result = new StringBuffer();
     int seenRAB = 0;
     isWhitespace=true;
 CharData:
     do {      
       if (!CharacterClasses.isChar(ch)) {
         if (ch == EOF)
           throw new EOFException("Unexpected end of input while parsing Character data");
         else
           throw new XmlPullParserException("Syntax error, invalid character while parsing Character data",this,null);
       }
       switch (seenRAB) {
         case 0:
           switch (ch) {
             case (RAB):
               seenRAB = 1;
               break;
             case (AMP):
             case (LT):
               break CharData;
             default:
               if (isWhitespace && !CharacterClasses.isS(ch))
                 isWhitespace=false;
               result.append(ch);
           }
           break;
         case 1:
           switch (ch) {
             case (RAB):
               seenRAB = 2;
               break;
             case (AMP):
             case (LT):
               if (isWhitespace)
                 isWhitespace=false;
               result.append(']');
               break CharData;
             default:
               if (isWhitespace)
                 isWhitespace=false;
               seenRAB = 0;
               result.append(']').append(ch);
           }
           break;
         case 2:
           switch (ch) {
             case RAB:
               if (isWhitespace)
                 isWhitespace=false;
               result.append(']');
               break;
             case GT:
               throw new XmlPullParserException("Syntax error, the CDATA-sesction-close delimiter ']]>' must not occur in Character data",this,null);
             case (AMP):
             case (LT):
               if (isWhitespace)
                 isWhitespace=false;
               result.append(']').append(']');
               break CharData;
             default:
               seenRAB = 0;
               if (isWhitespace)
                 isWhitespace=false;
               result.append(']').append(']').append(ch);
           }
           break;
       }
       nextChar();
     } while (true);
     return result.toString();
   }
 
   private void nextMarkupContent() throws XmlPullParserException, IOException {
     //Distinguishes between
     //[40]    STag          ::=  '<' Name (S Attribute)* S? '>'
     //[44]    EmptyElemTag  ::=  '<' Name (S Attribute)* S? '/>'
     //[42]    ETag          ::=  '</' Name S? '>'
     //[15]    Comment       ::=  '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
     //[19]    CDStart       ::=  '<![CDATA['
     //[16]    PI            ::=  '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'
     //[23]    XMLDecl       ::=  '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
     //assumes we already read the initial '<'
     switch (ch) {
       case QUES:
         eventType = PROCESSING_INSTRUCTION;
         nextChar();
         name = nextName();
         if (name.equalsIgnoreCase("xml"))
           throw new XmlPullParserException("The target '" + name + "' is not allowed for a processing instruction");
         text = name + nextPIContent();
         break;
       case EXCL:
         nextChar();
         switch (ch) {
           case DASH:
             eventType = COMMENT;
             nextChar();
             requireChar(DASH,"Syntax error, comments must begin with '<!--'");
             text=nextCommentContent();
             break;
           case LAB:
             eventType = CDSECT;
             nextChar();
             requireString("CDATA[","Syntax error, only CDATA marked sections are supported in XML");
             text = nextCDataContent();
             break;
           case 'D':
             eventType = DOCDECL;
             nextChar();
             requireString("OCTYPE","Syntax error, invalid characters after '<!'");
             if (readDocdecl)
               throw new XmlPullParserException("There can be only one doctype declaration in a document");
             skipDoctypeContent();
             //throw new XmlPullParserException("This implementation doesn't support DOCTYPE declarations");
             break;
           default:
             throw new XmlPullParserException("Syntax error, invalid characters after '<!'");
         }
         break;
       case SLASH:
         eventType = END_TAG;
         nextChar();
         name = nextName();
         skipS();
         requireChar(GT,"Syntax error, end-tag must end with '>'");
         break;
       default://Actually case:NAME_START_CHAR
         eventType = START_TAG;
         name = nextName();
         nextStartTagContent();
     }
   }
 
   private void skipDoctypeContent() throws IOException {
     int bracketLevel=0;
     do {
       nextChar();
       if(ch == '[') ++bracketLevel;
       if(ch == ']') --bracketLevel;
       if(ch == '>' && bracketLevel == 0) break;
       if(ch==EOF)
         throw new EOFException();
     } while (true);
     nextChar();
     readDocdecl=true;
   }
 
   private void nextStartTagContent() throws XmlPullParserException, IOException {
     //reads the following part from productions [40] & [44]:
     //(S Attribute)* S? '/'?'>'
     //Assumes we already read '<' Name
     //The state machine looks like this:
     // 0::= S 1 | '/' | '>'
     // 1::= Attribute 0 | '/' | '>'
     int state = 0;
     isEmptyElemTag = false;
     attributeList.clear();
     attributeMap.clear();
 AttList:
     do {
       switch (state) {
         case 0:
           if (CharacterClasses.isS(ch)) {
             skipS();
             state = 1;
             continue AttList;
           }
           switch (ch) {
             case SLASH:
               isEmptyElemTag = true;
               nextChar();
             case GT:
               break AttList;
             default:
               throw new XmlPullParserException("Syntax error, unexpected character '" + ch + "' while parsing attribute list");
           }
         case 1:
           switch (ch) {
             case SLASH:
               isEmptyElemTag = true;
               nextChar();
             case GT:
               break AttList;
             default:
               nextAttribute();
               state = 0;
               break;
           }
       }
     } while (true);
     requireChar(GT,"Syntax error while parsing " + (isEmptyElemTag ? "EmptyElemTag" : "STag") + ": unexpected terminal character, must be '>'");
     eventType = START_TAG;
     depth++;
   }
   private String nextNcoding() throws IOException, XmlPullParserException {
     // 'ncoding' Eq ('"' EncName '"' | "'" EncName "'" )
     nextChar();
     requireString("ncoding","Syntax error while parsing XML declaration, 'encoding' expected");
     nextEq();
     if (ch!=QUOT && ch!=APOS)
       throw new XmlPullParserException("Syntax error, encoding name must be encolsed in quotes or apostrophes",this,null);
     char delim=ch;
     nextChar();
     if (!CharacterClasses.isEncNameFirst(ch))
       throw new XmlPullParserException("Syntax error, encoding name must begin with [A-Za-z]");
     StringBuffer result = new StringBuffer();
     result.append(ch);
     do { //[81]     EncName    ::=    [A-Za-z] ([A-Za-z0-9._] | '-')*
       nextChar();
       if (ch == delim) {
         nextChar();
         return result.toString();
       } else if (CharacterClasses.isEncName(ch)) {
         result.append(ch);
       } else {
         throw new XmlPullParserException("Syntax error, encoding name must contain only with [A-Za-z0-9._] or the character '-'");
       }
     } while (true);
 
   }
   
   private boolean nextTandalone() throws IOException, XmlPullParserException{
     // 'tandalone' Eq (("'" ('yes' | 'no') "'") | ('"' ('yes' | 'no') '"'))
     nextChar();
     requireString("tandalone","Syntax error while parsing XML declaration, 'standalone' expected");
     nextEq();
     if (ch!=QUOT && ch!=APOS)
       throw new XmlPullParserException("Syntax error, standalone value must be encolsed in quotes or apostrophes",this,null);
     char delim=ch;
     nextChar();
     boolean result;
     switch (ch) {
       case 'y':
         nextChar();
         requireString("es","Syntax error, standalone value must be either 'yes' or 'no'");
         result=true;
         break;
       case 'n':
         nextChar();
         requireChar('o',"Syntax error, standalone value must be either 'yes' or 'no'");
         result=false;
         break;
       default:
         throw new XmlPullParserException("Syntax error, standalone value must be either 'yes' or 'no'",this,null);
     }
     requireChar(delim,"Syntax error, encoding name must be encolsed in quotes or apostrophes");
     return result;
 
   }
   private boolean tryXmlDecl() throws IOException, XmlPullParserException {
     //[23]    XMLDecl    ::=    '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
     markInput(6);
     try {
       requireString("<?xml","No Xml declaration present");
     } catch (XmlPullParserException e) {
       resetInput();
       return false;
     }
     if (CharacterClasses.isNameChar(ch)) { //something that only looks like an XML declaration
       resetInput();
       return false;
     }
     unmarkInput();
     nextS();
     requireString("version","Syntax error in XML declaration, 'version' expected");
     nextEq();
     if (!nextAttValue().equals("1.0"))
       throw new XmlPullParserException("XML version MUST be '1.0'");
     
     /* now for the tricky part
        EncodingDecl? SDDecl? S? '?>'
        which translates to
        (S 'encoding' Eq ('"' EncName '"' | "'" EncName "'" ))?   (S 'standalone' Eq (("'" ('yes' | 'no') "'") | ('"' ('yes' | 'no') '"')))? S? '?>'
        (S 'e' Ncoding)? (S 's' Tandalone)? S? '?>'
        The grammar for this is:
        0 ::= S 1 | '?'
        1 ::= 'e' Ncoding 2 | 's' Tandalone 4 | '?'   
        2 ::= S 3 | '?'
        3 ::= 's' Tandalone 4 | '?'
        4 ::= S '?' | '?'
     */
     
     int state=0;
 XMLDeclContent:    
     do {
       switch (state) {
         case 0:
           if (CharacterClasses.isS(ch)) {
             skipS();
             state=1;
           } else if (ch==QUES) {
             nextChar();
             break XMLDeclContent;
           } else {
             throw new XmlPullParserException("Syntax error parsing XML declaration, expecting whitespace or '?>' after version");
           }
           break;
         case 1:
           switch (ch) {
             case 'e':
               encodingDeclared=nextNcoding();
               state=2;
               break;
             case 's':
               isStandalone=nextTandalone();
               state=4;
               break;
             case '?':
               nextChar();
               break XMLDeclContent;
             default:
               throw new XmlPullParserException("Syntax error parsing XML declaration, expecting encoding, standalone or '?>'");
           }
           break;
         case 2:
           if (CharacterClasses.isS(ch)) {
             skipS();
             state=3;
           } else if (ch==QUES) {
             nextChar();
             break XMLDeclContent;
           } else {
             throw new XmlPullParserException("Syntax error parsing XML declaration, expecting whitespace or '?>' after encoding");
           }
           break;
         case 3:
           switch (ch) {
             case 's':
               isStandalone=nextTandalone();
               state=4;
               break;
             case '?':
               nextChar();
               break XMLDeclContent;
             default:
               throw new XmlPullParserException("Syntax error parsing XML declaration, expecting standalone or '?>'");
           }
           break;
         case 4:
           skipS();          
           requireChar('?',"Syntax error, XML declaration must end with '?>'");
           break XMLDeclContent;
         default:
           break;
       }
     } while(true);
     requireChar('>',"Syntax error, XML declaration must end with '?>'");
     xmlDeclParsed=true;
     return true;
   }
   
   public class LexerTest extends TestCase {
     public LexerTest(String s) {
       super(s);
     }
     public void testNextReference() throws IOException, XmlPullParserException {
       setInput(new StringReader("&fooBar;&#64;&lt;&amp;"));
       nextChar();
       assertEquals(nextReference(),null);
       assertEquals(nextReference(),new String(Character.toChars(64)));
       assertEquals(nextReference(),"<");
       assertEquals(nextReference(),"&");
     }
     private void assertAttribute(int i, String name, String value) {
       assertEquals(getAttributeName(i),name);
       assertEquals(getAttributeValue(i),value);
     }
     public void testNextAttribute() throws IOException, XmlPullParserException {
       setInput(new StringReader("ap:kf  =   \n \r\n \"foo\r\n\n\r&amp;'xxx\"foofoo='wtf'"));
       nextChar();
       eventType = START_TAG;
       nextAttribute();
       nextAttribute();
       assertEquals(getAttributeCount(),2);
       assertAttribute(0,"ap:kf","foo   &'xxx");
       assertAttribute(1,"foofoo","wtf");
     }
     public void testPIContent() throws IOException, XmlPullParserException {
       setInput(new StringReader("?> bla??? >>>>??? ? > ?hblah?>ffrrfraaafhr-->-->-aasdfasdf-asdfsad-asdfa->-asfd-->adasdf--asdf-->asdfasdf--->"));
       nextChar();
       assertEquals(nextPIContent(),"");
       assertEquals(nextPIContent()," bla??? >>>>??? ? > ?hblah");
       try {
         nextPIContent();
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }
     }
     public void testCommentContent() throws IOException, XmlPullParserException {
       setInput(new StringReader("ffrrfraaafhr-->-->-aasdfasdf-asdfsad-asdfa->-asfd-->adasdf--asdf-->asdfasdf--->"));
       nextChar();
       assertEquals(nextCommentContent(),"ffrrfraaafhr");
       assertEquals(nextCommentContent(),"");
       assertEquals(nextCommentContent(),"-aasdfasdf-asdfsad-asdfa->-asfd");
       try {
         nextCommentContent();
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }
       assertEquals(nextCommentContent(),"asdf");
       try {
         System.out.println(nextCommentContent());
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }
     }
     public void testCDataContent() throws IOException, XmlPullParserException {
       setInput(new StringReader("]]12]3]4]]]]5]]6]]7]]] >]]]8]>9012>>>>>]>]>]>]]b]]>"));
       nextChar();
       assertEquals(nextCDataContent(),"]]12]3]4]]]]5]]6]]7]]] >]]]8]>9012>>>>>]>]>]>]]b");
     }
     public void testCharData() throws IOException, XmlPullParserException {
       setInput(new StringReader("asdfasdfjh<skdjfhaskdjfh&askjfh<]]12]3]4]]]]5]]6]]7]]] >]]]8]>9012>>>>>]>]>]>]]b<]]12]3]4]]]]5]]6]]7]]] >]]]8]>9012>>>>>]>]>]>]]b]]>asdf]]>asdf"));
       assertEquals(nextChar(),'a');
       assertEquals(nextCharData(),"asdfasdfjh");
       assertEquals(getChar(),'<');
 
       assertEquals(nextChar(),'s');
       assertEquals(nextCharData(),"skdjfhaskdjfh");
       assertEquals(getChar(),'&');
 
       assertEquals(nextChar(),'a');
       assertEquals(nextCharData(),"askjfh");
       assertEquals(getChar(),'<');
 
       assertEquals(nextChar(),']');
       assertEquals(nextCharData(),"]]12]3]4]]]]5]]6]]7]]] >]]]8]>9012>>>>>]>]>]>]]b");
       assertEquals(getChar(),'<');
 
       assertEquals(nextChar(),']');
       try {
         nextCharData();
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }
       assertEquals(getChar(),'>');
 
       assertEquals(nextChar(),'a');
       try {
         nextCharData();
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }
     }
     public void testStartTagContent() throws IOException, XmlPullParserException {
       setInput(new StringReader(" foo='bar' bar='foo'> a='b' c='d'   > u='1' v='2'/>"));
       assertEquals(nextChar(),' ');
       nextStartTagContent();
       assertEquals(getAttributeCount(),2);
       assertEquals(isEmptyElementTag(),false);
       assertAttribute(0,"foo","bar");
       assertAttribute(1,"bar","foo");
       try {
         getAttributeName(2);
         fail("Expcected IndexOutOfBoundsException");
       } catch (IndexOutOfBoundsException e) {
         assertTrue(true);
       }
       assertEquals(getChar(),' ');
 
       nextStartTagContent();
       assertEquals(getAttributeCount(),2);
       assertEquals(isEmptyElementTag(),false);
       assertAttribute(0,"a","b");
       assertAttribute(1,"c","d");
       try {
         getAttributeValue(2);
         fail("Expcected IndexOutOfBoundsException");
       } catch (IndexOutOfBoundsException e) {
         assertTrue(true);
       }
       assertEquals(getChar(),' ');
 
       nextStartTagContent();
       assertEquals(getAttributeCount(),2);
       assertEquals(isEmptyElementTag(),true);
       assertAttribute(0,"u","1");
       assertAttribute(1,"v","2");
       try {
         getAttributeValue(2);
         fail("Expcected IndexOutOfBoundsException");
       } catch (IndexOutOfBoundsException e) {
         assertTrue(true);
       }
       assertEquals(getChar(),EOF);
 
     }
     public void testMarkupContentComment() throws IOException, XmlPullParserException {
       setInput(new StringReader("!--foobar-->"));
       assertEquals(START_DOCUMENT,getEventType());
       assertEquals('!',nextChar());
       nextMarkupContent();
       assertEquals(COMMENT,eventType);
       assertEquals("foobar",getText());      
       assertEquals(EOF,getChar());
     }
     public void testMarkupContentCommentEmpty() throws IOException, XmlPullParserException {
       setInput(new StringReader("!---->"));
       assertEquals(START_DOCUMENT,getEventType());
       assertEquals('!',nextChar());
       nextMarkupContent();
       assertEquals(COMMENT,eventType);
       assertEquals("",getText());      
       assertEquals(EOF,getChar());      
     }
     public void testMarkupContentCommentError() throws IOException, XmlPullParserException {
       setInput(new StringReader("!-foo-->"));
       assertEquals(START_DOCUMENT,getEventType());
       assertEquals('!',nextChar());
       try {
         nextMarkupContent();
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }      
     }
     public void testMarkupContentPI() throws Exception {
       setInput(new StringReader("?php echo('j00 fail')?>"));
       assertEquals('?',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       nextMarkupContent();
       assertEquals(PROCESSING_INSTRUCTION,getEventType());
       assertEquals("php echo('j00 fail')",getText());
       assertEquals(EOF,getChar());
     }
     public void testMarkupContentPIXmlDecl() throws Exception {
       setInput(new StringReader("?xml version='1.0'?>"));
       assertEquals('?',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       try {
         nextMarkupContent();
         fail("Expected XmlPullParserException");
       } catch(XmlPullParserException e) {
         
       }
         
     }
     public void testMarkupContentCDSect() throws Exception {
       setInput(new StringReader("![CDATA[<this> will be &ignored;]]<><!---->]]>"));
       assertEquals('!',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       nextMarkupContent();
       assertEquals(CDSECT,getEventType());
       assertEquals("<this> will be &ignored;]]<><!---->",getText());
       assertEquals(EOF,getChar());
     }
     public void testMarkupContentDoctype() throws Exception {
       setInput(new StringReader("!DOCTYPE [<!ELEMENT ]>"));
       assertEquals('!',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       nextMarkupContent();
       assertEquals(DOCDECL,getEventType());
     }
     public void testMarkupContentMarkedSectionError() throws Exception {
       setInput(new StringReader("![RCDSECT[some RCDATA]]>"));
       assertEquals('!',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       try {
         nextMarkupContent();
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }
     }
     public void testMarkupContentInvalidCharAfterExcl() throws Exception {
       setInput(new StringReader("!]something"));
       assertEquals('!',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       try {
         nextMarkupContent();
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }
     }
     public void testMarkupContentEndTag() throws Exception {
       setInput(new StringReader("/endtag>"));
       assertEquals('/',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       nextMarkupContent();
       assertEquals(END_TAG,getEventType());
       assertEquals(null,getText());
       assertEquals("endtag",AIMLPullParser.this.getName());
       assertEquals(EOF,getChar());      
     }
     public void testMarkupContentEndTagWithSpaces() throws Exception {
       setInput(new StringReader("/endtag   >"));
       assertEquals('/',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       nextMarkupContent();
       assertEquals(END_TAG,getEventType());
       assertEquals(null,getText());
       assertEquals("endtag",AIMLPullParser.this.getName());
       assertEquals(EOF,getChar());      
     }
     public void testMarkupContentEndTagMalformed() throws Exception {
       setInput(new StringReader("/ endtag   >"));
       assertEquals('/',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       try {
         nextMarkupContent();
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }
       
       setInput(new StringReader("/endtag s>"));
       assertEquals('/',nextChar());
       assertEquals(START_DOCUMENT,getEventType());
       try {
         nextMarkupContent();
         fail("Expected XmlPullParserException");
       } catch (XmlPullParserException e) {
         assertTrue(true);
       }
       
     }
     public void testXmlDeclVersion() throws Exception {
       setInput(new StringReader("<?xml version='1.0' encoding='windows-1250' standalone='yes'?>"));
       assertEquals('<',nextChar());
       tryXmlDecl();
       assertTrue("xmlDeclParsed",xmlDeclParsed);
       assertTrue("isStandalone",isStandalone);
       assertEquals("windows-1250",encodingDeclared);
     }
     public void testNextToken() throws Exception {
       setInput(new StringReader("<foo>some mixed content<bar>foo&amp;bar</bar></foo>"));
       assertEquals(START_DOCUMENT,getEventType());
       assertEquals("depth",0,getDepth());
       assertEquals("internal state",internalState,InternalState.DOCUMENT_START);
            
       assertEquals("start tag nt",START_TAG,nextToken());
       assertEquals("start tag et",START_TAG,getEventType());
       assertEquals("depth 1",1,getDepth());
       assertEquals("foo",AIMLPullParser.this.getName());
       assertEquals("internal state",internalState,InternalState.CONTENT);
       
       assertEquals("text nt",TEXT,nextToken());
       assertEquals("text et",TEXT,getEventType());
       assertEquals("depth 1",1,getDepth());
       assertEquals("some mixed content",getText());
       assertEquals("internal state",internalState,InternalState.CONTENT);
 
       assertEquals("start tag nt",START_TAG,nextToken());
       assertEquals("start tag et",START_TAG,getEventType());
       assertEquals("depth 2",2,getDepth());
       assertEquals("bar",AIMLPullParser.this.getName());
       assertEquals("internal state",internalState,InternalState.CONTENT);
 
       assertEquals("text nt",TEXT,nextToken());
       assertEquals("text et",TEXT,getEventType());
       assertEquals("depth 2",2,getDepth());
       assertEquals("foo",getText());
       assertEquals("internal state",internalState,InternalState.CONTENT);
       
       assertEquals("entity nt",ENTITY_REF,nextToken());
       assertEquals("entity et",ENTITY_REF,getEventType());
       assertEquals("depth 2",2,getDepth());
       assertEquals("&",getText());
       assertEquals("internal state",internalState,InternalState.CONTENT);
 
       assertEquals("text nt",TEXT,nextToken());
       assertEquals("text et",TEXT,getEventType());
       assertEquals("depth 2",2,getDepth());
       assertEquals("bar",getText());
       assertEquals("internal state",internalState,InternalState.CONTENT);
       
       assertEquals("end tag nt",END_TAG,nextToken());
       assertEquals("end tag et",END_TAG,getEventType());
       assertEquals("depth 2",2,getDepth());
       assertEquals("bar",AIMLPullParser.this.getName());
       assertEquals("internal state",internalState,InternalState.CONTENT);
 
       assertEquals("end tag nt",END_TAG,nextToken());
       assertEquals("end tag et",END_TAG,getEventType());
       assertEquals("depth 1",1,getDepth());
       assertEquals("foo",AIMLPullParser.this.getName());
       assertEquals("internal state",internalState,InternalState.EPILOG);
 
       assertEquals("end tag nt",END_DOCUMENT,nextToken());
       assertEquals("end tag et",END_DOCUMENT,getEventType());
       assertEquals("depth 0",0,getDepth());
       assertEquals("internal state",internalState,InternalState.DOCUMENT_END);
 
       assertEquals("end tag nt",END_DOCUMENT,nextToken());
       assertEquals("end tag et",END_DOCUMENT,getEventType());
       assertEquals("depth 0",0,getDepth());
       assertEquals("internal state",internalState,InternalState.DOCUMENT_END);
       
     }  
     public void testChardataWhitespace() throws Exception {
       setInput(new StringReader("    <   s  <   ><"));
       eventType=TEXT;
 
       nextChar();
       nextCharData();
       assertTrue(isWhitespace());      
 
       nextChar();
       nextCharData();
       assertFalse(isWhitespace());      
 
       nextChar();
       nextCharData();
       assertFalse(isWhitespace());      
     }
     public void testCDataWhitespace() throws Exception {
       setInput(new StringReader("    ]]>   ] ]]>   ]] ]]>"));
       eventType=CDSECT;
 
       nextChar();
       nextCDataContent();
       assertTrue(isWhitespace());      
 
       nextChar();
       nextCDataContent();
       assertFalse(isWhitespace());      
 
       nextChar();
       nextCDataContent();
       assertFalse(isWhitespace());      
     }
   } 
  
   private Test suite() {
     TestSuite t = new TestSuite();
     Method[] methods = LexerTest.class.getMethods();
     for (int i = 0; i < methods.length; i++) {
       if (methods[i].getName().startsWith("test") && Modifier.isPublic(methods[i].getModifiers())) {
         t.addTest(new LexerTest(methods[i].getName()));
       }
     }
     return t;
   }
 
   public static void main(String[] args) throws Exception {
     AIMLPullParser pp = new AIMLPullParser();
     TestRunner.run(pp.suite());
     /*
      pp.tests().testNextReference();
      pp.tests().testNextAttribute();   
      pp.tests().testPIContent();
      pp.tests().testCommentContent();    
      pp.tests().testCDataContent();
      pp.tests().testCharData();
      pp.tests().testStartTagContent();
      */
   }
 }
