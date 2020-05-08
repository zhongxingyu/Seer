 import java.io.UnsupportedEncodingException;
 
 public class FileMessage extends FileNameMessage {
   private String data;
   private byte[] encodedData;
   
 
   /** Do not call - used for decoding messages. */
   protected FileMessage(MessageWireType wireId) {
     super(wireId);
   }
 
   protected FileMessage(MessageWireType wireId, String fileName, String data) {
     super(wireId, fileName);
 
    if (data != null) {
      getEncodedData();
 
      if (encodedData.length > DFSMessage.kMaxFileSizeBytes)
        throw new IllegalArgumentException("Data buffer too large!");
     }
 
     this.data = data;
   }
   
   protected byte[] getEncodedData() {
     if (encodedData == null) {
       try {
        if (data == null) {
          encodedData = new byte[0];
        } else {
          byte[] encodedDataBytes = data.getBytes(DFSMessage.kStringCharset);
          encodedData = new byte[encodedDataBytes.length + 1];
          System.arraycopy(encodedDataBytes, 0, encodedData, 1, encodedDataBytes.length);
        }
       } catch (UnsupportedEncodingException e) {
         assert false : "should not get here";
         throw new RuntimeException("Unexpectedly could not serialize to UTF-8!");
       }
     }
 
     return encodedData;
   } 
 
   @Override
   public byte[] pack() {
     if (data == null)
       return null;
 
     byte[] msg = createPackedHeader(getEncodedData());
 
     return msg;
   }
 
   protected boolean deserializeDataTrailerIfPresent(byte[] msg, int start) {
     if (start < 0 || start > msg.length)
       return false;
 
     if (start == msg.length) {
       data = null;
       return true;
     }
 
    int dataLength = msg.length - start - 1;
     if (dataLength > DFSMessage.kMaxFileSizeBytes)
       return false;
 
     try {
      this.data = new String(msg, start + 1, dataLength, DFSMessage.kStringCharset);
     } catch (UnsupportedEncodingException e) {
       assert false : "should not get here";
       return false;
     }
 
     return true;
   }
     
 
   @Override
   protected boolean fromByteStream(byte[] msg) {
     int fileContentStart = unpackFileNameHeader(msg);
 
     if (fileContentStart < 0)
       return false;
 
     return deserializeDataTrailerIfPresent(msg, fileContentStart);
   }
 
   public String getData() {
     return data;
   }
 
   @Override
   public String toString() {
     return super.toString() + (data != null ? ", data = <" + data.length() + " chars>" : ", no data");
   }
 }
