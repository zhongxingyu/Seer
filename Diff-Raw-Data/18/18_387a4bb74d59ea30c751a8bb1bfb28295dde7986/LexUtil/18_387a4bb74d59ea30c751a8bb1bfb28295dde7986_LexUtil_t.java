 /*
  * Created on Feb 11, 2004
  */
 package uk.org.ponder.streamutil.read;
 
 import java.io.IOException;
 
 import uk.org.ponder.stringutil.CharWrap;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * The class
  * 
  * @author Bosmon
  */
 public class LexUtil {
   public static void expect(PushbackRIS lr, String toexpect) {
     for (int i = 0; i < toexpect.length(); ++i) {
       char c = lr.get();
       if (c != toexpect.charAt(i))
         throw new UniversalRuntimeException("Expected text " + toexpect
             + " not found");
     }
   }
 
   public static void skipWhite(PushbackRIS lr) {
     while (!lr.EOF()) {
       char c = lr.get();
       if (!Character.isWhitespace(c)) {
         lr.unread(c);
         break;
       }
     }
   }
 
   public static String readString(PushbackRIS pbr, String delimiters) {
     CharWrap togo = new CharWrap();
     while (!pbr.EOF()) {
       char c = pbr.get();
       if (delimiters.indexOf(c) != -1) {
         pbr.unread(c);
         break;
       }
       togo.append(c);
     }
     return togo.toString();
   }
 
   public static int readInt(PushbackRIS pbr) {
     StringBuffer sb = new StringBuffer();
     char c = pbr.get();
     while (Character.isDigit(c)) {
       sb.append(c);
       c = pbr.get();
     }
    if (c != ReadInputStream.EOF) {
      pbr.unread(c);
    }
     int togo = 0;
     try {
       togo = Integer.parseInt(sb.toString());
     }
     catch (Exception e) {
       throw UniversalRuntimeException.accumulate(e, "Error reading integer: ");
     }
     return togo;
   }
 
   public static double readDouble(PushbackRIS lr) {
     StringBuffer sb = new StringBuffer();
     char c = lr.get();
     while (Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '+'
         || c == '-') {
       sb.append(c);
       c = lr.get();
     }
    if (c != ReadInputStream.EOF) {
      lr.unread(c);
    }
     return Double.parseDouble(sb.toString());
   }
 
   /**
    * @param lr
    */
   public static void expectEmpty(PushbackRIS lr) {
     skipWhite(lr);
     if (!lr.EOF()) {
       CharWrap unexpected = new CharWrap();
       // TODO: this is rubbish! We cannot expect EOF until we actually read the
       // char.
       while (!lr.EOF()) {
         char c = lr.get();
         if (unexpected.size() < 32) {
           unexpected.append(c);
         }
       }
       throw new UniversalRuntimeException("Unexpected trailing data "
           + unexpected);
     }
   }
 }
