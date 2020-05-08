 package at.yawk.yxml;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Lexer {
     private final Reader    input;
     
     public Lexer(Reader input) {
         this.input = input;
     }
     
     public Lexer(InputStream input, String charset) throws UnsupportedEncodingException {
         this(new InputStreamReader(input, charset));
     }
     
     public Lexer(InputStream input) throws UnsupportedEncodingException {
         this(input, Charset.defaultCharset().name());
     }
     
     public Lexer(URLConnection connection) throws IOException {
        this(connection.getInputStream());
     }
     
     public Lexer(URL url) throws IOException {
         this(url.openConnection());
     }
     
     private boolean             streamInTag        = false;
     private boolean             isEOF              = false;
     
     private String              currentElementData = null;
     
     private Map<String, String> attributes         = null;
     private String              tagName            = null;
     
     public boolean getNext() throws IOException {
         if(isEOF)
             return false;
         final StringBuilder builder = new StringBuilder();
         while(true) {
             char c;
             try {
                 c = readNextCharacter();
             } catch(EOFException e) {
                 c = '\0';
                 isEOF = true;
             }
             if(isEOF) {
                 break;
             }
             if(streamInTag) {
                 if(c == '>') {
                     streamInTag = false;
                     attributes = null;
                     break;
                 }
             } else {
                 if(c == '<') {
                     streamInTag = true;
                     break;
                 }
             }
             builder.append(c);
         }
         currentElementData = builder.toString();
         return true;
     }
     
     public String getCurrentElementContent() {
         return currentElementData;
     }
     
     public String getCurrentElement() {
         return isTag() ? '<' + currentElementData + '>' : currentElementData;
     }
     
     public boolean isEmpty() {
         return !isTag() && currentElementData.trim().length() == 0;
     }
     
     public boolean isEndTagOnly() {
         return isTag() && currentElementData.length() > 0 && currentElementData.charAt(0) == '/';
     }
     
     public boolean isCompactTag() {
         return isTag() && currentElementData.length() > 0 && currentElementData.charAt(currentElementData.length() - 1) == '/';
     }
     
     public boolean isTag() {
         return !streamInTag;
     }
     
     public String getLowercaseTagName() {
         return getTagName().toLowerCase();
     }
     
     public String getTagName() {
         if(!isTag())
             throw new IllegalStateException("Not a tag");
         parseTag();
         return tagName;
     }
     
     public Map<String, String> getAttributes() {
         if(!isTag())
             throw new IllegalStateException("Not a tag");
         parseTag();
         return attributes;
     }
     
     private void parseTag() {
         if(attributes == null) {
             attributes = new HashMap<String, String>();
             final char[] characters = currentElementData.toCharArray();
             boolean isCompact = characters.length > 0 && characters[characters.length - 1] == '/';
             char usingQuotes = 0;
             boolean escaping = false;
             boolean inName = true;
             boolean inKey = true;
             StringBuilder tname = new StringBuilder();
             StringBuilder key = new StringBuilder();
             StringBuilder value = null;
             for(int i = 0; i < characters.length; i++) {
                 final char c = characters[i];
                 if(inName) {
                     if(c == ' ') {
                         inName = false;
                     } else {
                         tname.append(c);
                     }
                 } else {
                     if(inKey) {
                         if(c == ' ') {
                             if(key.length() != 0) {
                                 attributes.put(key.toString(), null);
                                 key = new StringBuilder();
                             }
                         } else if(c == '=') {
                             inKey = false;
                             value = new StringBuilder();
                             if(i < characters.length - 1) {
                                 final char d = characters[++i];
                                 if(d == '"' || d == '\'') {
                                     usingQuotes = d;
                                 } else if(d == ' ') {
                                     attributes.put(key.toString(), "");
                                     key = new StringBuilder();
                                     inKey = true;
                                 } else {
                                     value.append(d);
                                 }
                             }
                         } else {
                             key.append(c);
                         }
                     } else {
                         if(usingQuotes != 0) {
                             if(c == usingQuotes) {
                                 if(escaping) {
                                     escaping = false;
                                     value.append(c);
                                 } else {
                                     attributes.put(key.toString(), value.toString());
                                     key = new StringBuilder();
                                     value = new StringBuilder();
                                     inKey = true;
                                 }
                             } else if(c == '\\') {
                                 if(escaping) {
                                     escaping = false;
                                     value.append("\\\\");
                                 } else {
                                     escaping = true;
                                 }
                             } else {
                                 if(escaping) {
                                     escaping = false;
                                     value.append('\\');
                                 }
                                 value.append(c);
                             }
                         } else {
                             if(c == ' ') {
                                 attributes.put(key.toString(), value.toString());
                                 key = new StringBuilder();
                                 value = new StringBuilder();
                                 inKey = true;
                             } else {
                                 value.append(c);
                             }
                         }
                     }
                 }
             }
             if(key.length() != 0 && !isCompact)
                 attributes.put(key.toString(), value == null ? null : value.toString());
             this.tagName = tname.toString();
         }
     }
     
     private char readNextCharacter() throws IOException {
         if(input.read(singleCharacterArray) <= 0)
             throw new EOFException();
         return singleCharacterArray[0];
     }
     
     private final char[] singleCharacterArray = new char[1];
 
     public String getAttribute(String key) {
         return getAttributes().get(key);
     }
 }
