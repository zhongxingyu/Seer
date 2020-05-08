 package sat.radio.message;
 
 import sat.file.FileFormat;
 
 public class MessageFile extends Message {
 	private int fileSize;
 	private FileFormat format;
 	private byte[] hash;
 	private int packetNumber;
 	private byte[] payload;
 	
 	public MessageFile() {
 		super();
 	}
 	
	public getPayload() {
 		return this.payload;
 	}
 }
