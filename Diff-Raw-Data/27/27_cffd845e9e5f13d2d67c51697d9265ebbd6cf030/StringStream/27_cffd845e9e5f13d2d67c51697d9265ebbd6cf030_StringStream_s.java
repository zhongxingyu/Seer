 package mi.stream;
 
 /**
  * User: goldolphin
  * Time: 2013-06-01 11:21
  */
 public class StringStream implements ICharStream {
     private String source;
     private int pos;
 
     public StringStream(String source) {
         this.source = source;
         pos = 0;
     }
 
     public void reset() {
         pos = 0;
     }
 
     public int getPos() {
         return pos;
     }
 
     @Override
     public char peek() {
         if (pos < 0 || pos >= source.length()) {
             return EOF;
         }
         return source.charAt(pos);
     }
 
     @Override
     public char poll() {
         char c = peek();
         pos ++;
         return c;
     }
 
     @Override
     public void retract() {
         pos --;
     }
 }
