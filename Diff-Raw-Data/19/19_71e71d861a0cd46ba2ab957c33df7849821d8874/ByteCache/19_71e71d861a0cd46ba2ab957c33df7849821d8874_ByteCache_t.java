 package luoyong.toolbox.json.me;
 
 /**
  *
  * @author Luo Yong &lt; luo.yong.name@gmail.com &gt;
  */
 public class ByteCache implements ByteHolder {
 
    private int byteArrayLength = 0;
    private byte[] bytes = null;
    private int nextPosition = 0;
 
    private int beginCachePosition = 0;
    private int endCachePosition = 0;
 
    public ByteCache(byte[] bytes) {
 
       if (bytes != null) {
          this.bytes = bytes;
          this.byteArrayLength = this.bytes.length;
       }
    }
 
    public byte getNextByte() throws EOFException {
       if (nextPosition < byteArrayLength) {
          // Point cursor to next byte.
          // Position of current byte is (nextPosition - 1).
          nextPosition++;
          // Return current byte.
          return bytes[nextPosition - 1];
       }else {
          throw new EOFException();
       }
    }
 
    public byte getCurrentByte() throws EOFException {
      // The next position cursor may be out of range.
      if (nextPosition <= byteArrayLength) {
          // Return current byte.
          return bytes[nextPosition - 1];
       }else {
          throw new EOFException();
       }
    }
 
    public void beginCache() {
       this.beginCachePosition = this.nextPosition;
       this.endCachePosition = 0;
    }
    
    public void beginCacheAndCacheCurrentByte() {
       this.beginCachePosition = this.nextPosition - 1;
    }
 
    public void endCache() {
       this.endCachePosition = this.nextPosition;
    }
 
    public byte[] getCachedBytes() {
       throw new UnsupportedOperationException("Not supported yet.");
    }
 
    public String getCachedBytesAsString(String charset) {
 
       if (this.endCachePosition > this.beginCachePosition) {
          return new String(bytes, this.beginCachePosition,
                  (this.endCachePosition - this.beginCachePosition));
       }else if (this.endCachePosition == this.beginCachePosition) {
          return "";
       }else {
          return null;
       }
    }
 
    public String getCachedBytesAsStringWithoutTrailing(String charset) {
 
       if ((this.endCachePosition - 1) > this.beginCachePosition) {
          return new String(bytes, this.beginCachePosition,
                  (this.endCachePosition - 1 - this.beginCachePosition));
       }else if ((this.endCachePosition - 1)  == this.beginCachePosition) {
          return "";
       }else {
          return null;
       }
    }
 }
