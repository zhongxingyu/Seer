 /*
  * Created on Nov 11, 2005
  */
 package uk.org.ponder.streamutil.read;
 
 import uk.org.ponder.stringutil.CharWrap;
 
 public class PushbackRIS implements ReadInputStream {
   private CharWrap pushback = new CharWrap(8);
   private ReadInputStream wrapped;
   
   public PushbackRIS(ReadInputStream wrapped) {
     this.wrapped = wrapped;
   }
 
   public char get() {
     if (pushback.size > 0) {
       return pushback.charAt(--pushback.size);
     }
     else return wrapped.get();
   }
 
   public void unread(char c) {
    if (c != EOF) {
      pushback.append(c);
    }
   }
   
   public int read(char[] target, int start, int length) {
     int read = 0;
     while (pushback.size > 0 && read < length) {
       target[start + read] = get(); // may as well do these slowly, there will not be very many
       ++read;
     }
     read += wrapped.read(target, start + read, length - read);
     return read;
   }
 
   public boolean EOF() {
     return pushback.size == 0 && wrapped.EOF();
   }
 
   public void close() {
     wrapped.close();
   }
   
 }
