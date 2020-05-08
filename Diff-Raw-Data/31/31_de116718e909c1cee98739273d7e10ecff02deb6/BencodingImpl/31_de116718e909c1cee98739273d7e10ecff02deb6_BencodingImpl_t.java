 /*
  * Created on Jul 5, 2004
  */
 package net.sf.torrentsniffer.bencoding;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Stack;
 
 //import org.apache.log4j.Logger;
 
 /**
  * Implementation of bencoding.
  * 
  * See http://wiki.theory.org/index.php/BitTorrentSpecification for information
  * about bencoding.
  * 
  * 
  * @author Larry Williams
  *  
  */
 public class BencodingImpl implements Bencoding {
 
     public static final String BYTE_ENCODING = "ISO-8859-1";
 
     /*private final static Logger log = Logger.getLogger(BencodingImpl.class
             .getName());*/
 
     /*
      * (non-Javadoc)
      * 
      * @see net.sf.torrentsniff.bencoding.Bencoding#decode(java.lang.String)
      */
     public Dictionary decode(String string) throws FileNotFoundException {
        // log.debug("Decoding file '" + string + "'");
         FileInputStream input = new FileInputStream(string);
         return decode(input);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see net.sf.torrentsniff.bencoding.Bencoding#decode(java.io.InputStream)
      */
     public Dictionary decode(InputStream input) {
 
         Stack stack = new Stack();
         //stack.push(root);
 
         try {
             process(input, stack);
 
         } catch (IOException ex) {
             throw new BencodingException(ex);
         }
         Dictionary root = (Dictionary) stack.peek();
 
         return root;
     }
 
     /**
      * 
      * @todo Support for proxies could be implemented 
      * @todo Decode from a URL should be able to handle GZIP encoded content.
      * @todo Use Apache HTTP Client.
      * 
      * @see net.sf.torrentsniff.bencoding.Bencoding#decode(java.net.URL)
      */
     public Dictionary decode(URL url, int timeOutInSecs) {
         try {
         	URLConnection connection; 
         	//connection.setConnectTimeout(1);
         	connection = url.openConnection();
         	connection.setConnectTimeout(1000 * timeOutInSecs);
             return decode(connection.getInputStream());
         } catch (IOException ex) {
             throw new BencodingException(ex);
         }
     }
 
     /**
      * @param reader
      * @param c
      * @param stack
      * @throws IOException
      */
     private void process(InputStream input, Stack stack) throws IOException {
 
         BufferedInputStream stream = new BufferedInputStream(input);
 
         try {
 
             int c;
 
             while ((c = stream.read()) != -1) {
 
                 if (c == 'd') {
                     Dictionary dictionary = readDictionary(stream, stack);
                     addObject(stack, dictionary);
                     stack.push(dictionary);
                 } else if (c == 'l') {
                     List list = readList(stream, stack);
                     addObject(stack, list);
                     stack.push(list);
                 } else if (c == 'i') {
                     Integer integer = readInteger(stream, stack);
                     addObject(stack, integer);
                 } else if (Character.isDigit((char) c)) {
                     byte bytes[] = readString(c, stream, stack);
                     addObject(stack, bytes);
                 } else if (c == 'e') {
                     Object object = stack.peek();
                     if (object instanceof Dictionary) {
                         //log.debug("End of Dictionary");
                         // Have to close dictionary to know if it is valid
                         Dictionary dictionary = (Dictionary) object;
                         dictionary.close();
                     } else if (object instanceof List) {
                         //log.debug("End of List");
                     } else {
                         throw new BencodingException("Unknown '"
                                 + object.getClass() + "' collection end");
                     }
                     // Remove List or Dictionary from stack
                     stack.pop();
                 } else {
                 	// error, print first char
                 	throw new BencodingException("Unknown object '" + (char) c+ "'");
                 }
             }
 
             // We should have only one root object
             if (stack.size() > 1) {
                 throw new BencodingException(
                         "Stack size is "
                                 + stack.size()
                                 + " it should be 1. Most probably some "
                                 + "List or Dictionary was not closed. Check that all Lists and Dictionaries "
                                 + "are closed with the character e.");
             }
         } finally {
             stream.close();
         }
     }
 
     /**
      * @param firstChar
      * @param reader
      * @param stack
      * @throws IOException
      */
     private byte[] readString(int firstChar, InputStream stream, Stack stack)
             throws IOException {
         int numeric = Character.getNumericValue((char) firstChar);
         String value = numeric + "";
 
         int next;
 
         // Read while next character is numeric
         while (true) {
 
             next = stream.read();
 
             if (next == -1) {
                 throw new BencodingException(
                         "Unexpected end of torrent. Expected a numeric character that specifies the length of a string.");
             } else if ((char) next == ':') {
                 break;
             } else if (Character.isDigit((char) next)) {
                 value = value + Character.getNumericValue((char) next);
             } else {
                 throw new BencodingException(
                         "Expected a colon or a numeric character. Got character '"
                                 + (char) next + "'");
             }
         }
 
         int length = Integer.parseInt(value);
        // log.debug("New string found. Length is " + length);
 
         byte[] bytes = new byte[length];
 
         int totalCharsRead = 0;
 
         // Need to read everything even if blocking
         while (totalCharsRead < length) {
             int charsRead = stream.read(bytes, totalCharsRead, length
                     - totalCharsRead);
 
             if (charsRead == -1) {
                 break;
             }
             totalCharsRead += charsRead;
         }
 
         // Check that the whole String was read.
         if (totalCharsRead != length) {
             throw new RuntimeException("Read " + totalCharsRead
                     + " characters. Should have read " + length + " characters");
         }
         return bytes;
     }
 
     /**
      * @param reader
      * @param stack
      * @throws IOException
      */
     private Integer readInteger(InputStream stream, Stack stack)
             throws IOException {
         //log.debug("New integer found");
 
         int character = stream.read();
         String value;
 
         if (character == '-') {
             value = "-";
         } else {
             int numeric = Character.getNumericValue((char) character);
             value = numeric + "";
         }
 
         int next;
 
         // Read while next character is numeric
         while (true) {
 
             next = stream.read();
 
             if (next == -1) {
                 throw new BencodingException(
                         "Unexpected end of torrent. Expected a numeric character.");
             } else if ((char) next == 'e') {
                 break;
             } else if (Character.isDigit((char) next)) {
                 value = value + Character.getNumericValue((char) next);
             } else {
                 throw new BencodingException(
                         "Expected an e or a numeric character. Got character '"
                                 + (char) next + "'");
             }
         }
 
        // log.debug("Integer value " + value);
 
         Integer integer = new Integer(value);
        
         return integer;
     }
 
     /**
      * @param reader
      * @param stack
      */
     private List readList(InputStream stream, Stack stack) {
         //log.debug("New list found");
         List list = new ArrayList();
         return list;
     }
 
     /**
      * Dictionary keys are strings.
      * 
      * @param reader
      * @param stack
      */
     private Dictionary readDictionary(InputStream stream, Stack stack) {
        // log.debug("New dictionary found");
         Dictionary dictionary = new Dictionary();
         return dictionary;
     }
 
     /**
      * Inserts an object into the current List or Dictionary.
      * 
      * @param stack
      */
     private void addObject(Stack stack, Object value) {
         // The object that we are adding to
         if (stack.size() == 0) {
             if (!(value instanceof Dictionary)) {
                 throw new BencodingException("Root object is not a Dictionary "
                         + value.getClass());
             }
 
             stack.push(value);
         } else {
             Object object = stack.peek();
             if (object instanceof Dictionary) {
                // log.debug("Adding to Dictionary");
                 Dictionary dictionary = (Dictionary) object;
                 dictionary.addValue(value);
             } else if (object instanceof List) {
                // log.debug("Adding to List");
                 List list = (List) object;
                 list.add(value);
             } else {
                 throw new RuntimeException("Not a valid collection "
                         + object.getClass());
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see net.sf.torrentsniffer.bencoding.Bencoding#encode(java.lang.Object)
      */
     public void encode(OutputStream out, Object object) {
 
         try {
             if (object instanceof byte[]) {
                 byte bytes[] = (byte[]) object;
                 String string = new String(bytes, BencodingImpl.BYTE_ENCODING);
                 //log.debug("Encoding String '" + string + "'");
                 out.write(String.valueOf(string.length()).getBytes(
                         BYTE_ENCODING));
                 out.write(":".getBytes(BYTE_ENCODING));
                 out.write(bytes);
             } else if (object instanceof Integer) {
                 Integer integer = (Integer) object;
                // log.debug("Encoding Integer " + integer.intValue());
                 out.write("i".getBytes(BYTE_ENCODING));
                 out.write(String.valueOf(integer.intValue()).getBytes(
                         BYTE_ENCODING));
                 out.write("e".getBytes(BYTE_ENCODING));
 
             } else if (object instanceof List) {
                // log.debug("Encoding List");
                 List list = (List) object;
 
                 out.write("l".getBytes(BYTE_ENCODING));
                 for (int i = 0; i < list.size(); i++) {
                     encode(out, list.get(i));
                 }
                 out.write("e".getBytes(BYTE_ENCODING));
 
             } else if (object instanceof Dictionary) {
 
                // log.debug("Encoding Dictionary");
                 Dictionary dictionary = (Dictionary) object;
                 out.write("d".getBytes(BYTE_ENCODING));
                 String keys[] = dictionary.getKeys();
                 for (int i = 0; i < keys.length; i++) {
                     // Encode key
                     encode(out, keys[i].getBytes(BencodingImpl.BYTE_ENCODING));
                     // Encode value
                     encode(out, dictionary.get(keys[i]));
                 }
                 out.write("e".getBytes(BYTE_ENCODING));
 
             } else {
                 throw new BencodingException(
                         "Unable to encode unknown object "
                                 + object.getClass()
                                 + " only byte[] (String), Integer, List and Dictionary are supported.");
             }
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see net.sf.torrentsniffer.bencoding.Bencoding#match(java.util.List,
      *      java.util.List)
      */
     public boolean match(Dictionary bencoded1, Dictionary bencoded2) {
         ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
         ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
 
         encode(stream1, bencoded1);
         encode(stream2, bencoded2);
 
         boolean same = Arrays.equals(stream1.toByteArray(), stream2
                 .toByteArray());
 
       /*  if (!same && log.isDebugEnabled()) {
           //  log.debug("Array 1: " + new String(stream1.toByteArray()));
            // log.debug("Array 2: " + new String(stream2.toByteArray()));
         }*/
 
         return same;
     }
 }
