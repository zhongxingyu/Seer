 
 /** Response message; represents a response to an RPC.
  *
  * Wire format: (<offset> <size in bytes> <field name>)
  * - 0x00       1       Message type.
  * - 0x01       1       Response code (unsigned int)       
  * - 0x02       ?       Additional data, specific to RPC or response code
  */
 public class ResponseMessage extends DFSMessage{
   private int errorCode;
   private ArraySegment data;
 
   public ResponseMessage() {
     super(MessageWireType.Response);
   }
 
   public ResponseMessage(int errorCode) {
     this(errorCode, null);
   }
 
   public ResponseMessage(int errorCode, ArraySegment data) {
     this();
     this.errorCode = errorCode;
     this.data = data;
   }
 
   @Override
   public boolean fromByteStream(byte[] msg) {
     if (msg.length < 2)
       return false;
 
     errorCode = BinaryUtils.byteToUint(msg, 1);
     
     if (msg.length == 2)
       return true;
 
    data = new ArraySegment(msg, 2);
     return true;
   }
 
   @Override
   public byte[] pack() {
     int packedSizeBytes = 2;
     if (data != null)
       packedSizeBytes += data.getLength();
 
     byte[] packed = new byte[packedSizeBytes];
     writeMessageType(packed);
     BinaryUtils.uintToByte(errorCode, packed, 1);
     
     if (data != null)
       data.copy(packed, 2);
 
     return packed;
   }
 
   public int getCode() {
     return errorCode;
   }
 
   public ArraySegment getData() {
     return data;
   }
 }
