 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Collections;
 import java.util.Map;
 import java.util.HashMap;
 
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class TransactionMessage extends DFSMessage{
 
   private Flags txFlags;
   private TransactionId txId;
   private HashMap<DFSFilename,FileVersion> changedFiles;
 
   public TransactionMessage() {
     super(MessageWireType.Transaction);
   }
 
   public TransactionMessage(TransactionId txId, 
                             Flags flags, 
                             Map<DFSMessage,FileVersion> changedFiles) {
     super(MessageWireType.Transaction);
     this.txId = txId;
     txFlags = new Flags(flags);
     if(changedFiles != null)
       changedFiles = new HashMap<DFSMessage,FileVersion>(changedFiles);
   }
 
   public void setTxId(TransactionId txId) {
     this.txId = txId;
   }
   //(color) diff = auto:
   public TransactionId getTxId() {
     return txId;
   }
 
   public byte[] pack()  {
     byte[] flagPacked = txFlags.pack();
 
     ByteArrayOutputStream bytes = new ByteArrayOutputStream();
     ObjectOutputStream output;
 
     try {
       output = new ObjectOutputStream(bytes);
       output.writeObject(changedFiles);
       output.close();
     } catch (IOException e) {
       //blah
     }
     
     byte[] map = bytes.toByteArray();
     
     try {
       bytes.close();
     } catch (IOException e) {
       //lol fuck you
     }
    byte [] packed = new byte[3 + flagPacked.length + map.length];
    writeMessageType(packed);
    BinaryUtils.uintToByte(txId.getClientId(), packed, 1);
    BinaryUtils.uintToByte(txId.getTxId(), packed, 2);
    System.arraycopy(flagPacked, 0, packed, 3, flagPacked.length);
    System.arraycopy(map, 0, packed, 3 + flagPacked.length, map.length);
    return packed;
   }
 
   @Override
   protected boolean fromByteStream(byte[] msg) {
     
    if (msg.length < (3 + Flags.kDefaultEncodedLength))
       return false;
 
     int clientId = BinaryUtils.byteToUint(msg, 1);
     int txId = BinaryUtils.byteToUint(msg, 2);
     this.txId = new TransactionId(clientId, txId);
    this.txFlags = Flags.unpack(msg, 3, Flags.kDefaultEncodedLength);
 
    ByteArrayInputStream bytes = new ByteArrayInputStream(msg, 5, msg.length - 5);
     try {
       ObjectInputStream ois = new ObjectInputStream(bytes);
       changedFiles = (HashMap<DFSFilename, FileVersion>) ois.readObject();
     } catch (IOException e) {
       // TODO Auto-generated catch block
     } catch (ClassNotFoundException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     
     // TODO(jimmy): Serialize/deserialize map
     //the trick is to find where each key, value pair begins, 
     //we know DFSFileName.kPrefixString marks the beginning of each key value pair
     /*
     changedFiles = new HashMap<DFSFilename, FileVersion>();
     
     try {
       String map = new String(msg, 4, msg.length - 4, "UTF-8");
       String[] entries= map.split(DFSFilename.kPrefixString);
       
       //we don't know the length of the file name, but we can work it backwards
       for (int i = 0; i< entries.length; i++) {
         String str = entries[i];
         byte[] keyValuePair = Utility.stringToByteArray(str);
         
         int cid = BinaryUtils.byteToUint(keyValuePair, keyValuePair.length-12);
         //int 
       }
       
     } catch (UnsupportedEncodingException e) {
       System.out.println("well shit");
     } */
     
     return true;
   }
 
   public Map<DFSFilename, FileVersion> getChangedFiles() {
     return Collections.unmodifiableMap(changedFiles);
   }
 
   public Flags getFlags() {
     return txFlags;
   }
 }
 
 // TODO(jimmy): Serialize/deserialize map
 /*
 byte[][] mapSerialize = new byte[changedFiles.keySet().size()][64]; //this is an assumption, CAREFUL
 int i = 0;
 for (DFSFilename n : changedFiles.keySet()) {
   try {
     String path = n.getPath();
     byte[] pathByteArr = path.getBytes("UTF-8");
 
     int node = n.getOwningServer();
     byte[] nodeByte = new byte[1];
     BinaryUtils.uintToByte(node, nodeByte, 0);
 
     FileVersion v = changedFiles.get(n);
     byte[] packedFileVersion = v.pack();
 
     byte[] holeyshit = new byte[pathByteArr.length + nodeByte.length + packedFileVersion.length];
     
     System.arraycopy(pathByteArr, 0, holeyshit, 0, pathByteArr.length);
     System.arraycopy(nodeByte, 0, holeyshit, pathByteArr.length, nodeByte.length);
     System.arraycopy(packedFileVersion, 0, holeyshit, pathByteArr.length + nodeByte.length, packedFileVersion.length);
     
     mapSerialize[i] = holeyshit;
     i++;
   } catch (UnsupportedEncodingException e) {
     System.out.println("well shit");
   } 
 }
 
 byte[] packedMap = new byte[64 * mapSerialize.length];
 int current = 0;
 for (i=0; i<mapSerialize.length; i++) {
   System.arraycopy(mapSerialize[i], 0, packedMap, current, mapSerialize[i].length);
   current = current + mapSerialize[i].length;
 } */
