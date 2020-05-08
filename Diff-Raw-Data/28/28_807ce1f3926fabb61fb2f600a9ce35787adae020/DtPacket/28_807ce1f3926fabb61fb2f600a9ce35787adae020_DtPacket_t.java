 package distributedServices;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.UUID;
 
 public class DtPacket {
 	
 	public DtMessageType MessageType;
 	public UUID TransactionId;
 	public String Data;
 	
 	public DtPacket(){
 		
 	}
 	
 	public DtPacket(DtMessageType messageType, UUID transactionId, String data){
 		MessageType = messageType;
 		TransactionId = transactionId;
 		Data = data;
 	}
 	
 	public static DtPacket Abort(UUID transactionId){
 		return new DtPacket(DtMessageType.Abort, transactionId, "");
 	}
 	
 	public static DtPacket Commit(UUID transactionId){
 		return new DtPacket(DtMessageType.Commit, transactionId, "");
 	}
 	
 	public static DtPacket VoteRequest(UUID transactionId){
 		return new DtPacket(DtMessageType.VoteRequest, transactionId, "");
 	}
 	
 	public static DtPacket VoteResponse(UUID transactionId, String response){
		return new DtPacket(DtMessageType.VoteResponse, transactionId, response);
 	}
 	
 	public static DtPacket Enlist(UUID transactionId){
 		return new DtPacket(DtMessageType.Enlist, transactionId, "");
 	}
 	
 	public byte[] pack() {
 		try {
 			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(byteStream);
 
 			out.writeInt(MessageType.ordinal());
 			out.writeUTF(TransactionId.toString());
 			out.writeUTF(Data);
 
 			out.flush();
 			out.close();
 			return byteStream.toByteArray();
 		} catch (IOException e) {
 			return null;
 		}
 	}
 	
 	public static DtPacket unpack(byte[] iodata) {
 		try {
 			DataInputStream in = new DataInputStream(new ByteArrayInputStream(iodata));
 
 			DtPacket packet = new DtPacket();
 			int msgType = in.readInt();
 			packet.MessageType = DtMessageType.values()[msgType];
 			packet.TransactionId = UUID.fromString(in.readUTF());
 			packet.Data = in.readUTF();
 			return packet;
 			
 		} catch (IllegalArgumentException e) {
 			// will return null
 		} catch(IOException e) {
 			// will return null
 		}
 		return null;
 	}
 }
