import java.math.BigInteger;

 public class HashResult {
     private int distance;
     private byte[] buf;
 
     public HashResult(int distance,
             byte[] buf) {
         this.distance = distance;
         this.buf = buf;
     }
 
     public int getDistance() {
         return this.distance;
     }
 
     public byte[] getBuf() {
         return this.buf;
     }
 
     public String toString() {
        String result = new BigInteger(buf).abs().toString(16);
        return "HashResult{distance="+distance+ " result=\"" + result+"\"}";
     }
 }
