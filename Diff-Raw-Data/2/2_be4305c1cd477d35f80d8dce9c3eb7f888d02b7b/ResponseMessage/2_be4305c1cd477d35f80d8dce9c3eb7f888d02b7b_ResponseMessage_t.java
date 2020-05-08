 import java.io.UnsupportedEncodingException;
 
 /** Response message; represents a response to an RPC.
  *
  * Wire format: (<offset> <size in bytes> <field name>)
  * - 0x00       1       Message type.
  * - 0x01       1       Response code (unsigned int)
  * - 0x02       ?       Additional data, specific to RPC or response code
  */
 public class ResponseMessage extends FileNameMessage {
   private int errorCode;
 
   public ResponseMessage() {
     this(MessageWireType.Response);
   }
 
   protected ResponseMessage(MessageWireType wireId) {
    this(wireId, "", 0);
   }
 
   protected ResponseMessage(MessageWireType wireId, String fileName, int errorCode) {
     super(wireId, fileName);
     this.errorCode = errorCode;
   }    
 
   public ResponseMessage(String fileName, int errorCode) {
     this(MessageWireType.Response, fileName, errorCode);
   }
 
   @Override
   public boolean fromByteStream(byte[] msg) {
     int codeStart = unpackFileNameHeader(msg);
     if (codeStart < 0)
       return false;
 
     if (msg.length - codeStart < 1)
       return false;
 
     errorCode = BinaryUtils.byteToUint(msg, codeStart);
 
     return true;
   }
 
   protected byte[] packHeader(int numAdditionalBytes) {
     byte[] packed = new byte[1 + numAdditionalBytes];
     BinaryUtils.uintToByte(errorCode, packed, 0);
     return createPackedHeader(packed);
   }
 
   @Override
   public byte[] pack() {
     return packHeader(0);
   }
 
   public int getCode() {
     return errorCode;
   }
 
   @Override
   public String toString() {
     return "Response, code = " + Integer.toString(errorCode);
   }
 }
