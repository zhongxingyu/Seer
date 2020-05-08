 package utils;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 
 
public class Protocol {
	
	public static final int CREATE = 0;
 	
 	protected void sendSignedMessage(Socket s, byte[] message, PrivateKey key) throws IOException {
 		byte[] signature = SignUtils.signData(key, message);
 		sendMessage(s, signature);
 		sendMessage(s, message);
 	}
 	
 	protected void sendMessage(Socket s, byte[] message) throws IOException {
 		DataOutputStream out = new DataOutputStream(s.getOutputStream());
 		out.writeInt(message.length);
 		out.write(message);
 	}
 	
 	//TODO: make this return a String
 	protected byte[] readSignedMessage(Socket s, PublicKey key) throws IOException {
 		DataInputStream in = new DataInputStream(s.getInputStream());
 		
 		int sigLen = in.readInt();
 		byte[] signature = new byte[sigLen];
 		in.read(signature, 0, sigLen);
 		
 		int messageLen = in.readInt();
 		byte[] message = new byte[messageLen];
 		in.read(message, 0, messageLen);
 		
 		boolean success = SignUtils.verifyData(key, signature, message);
 		if (success) {
 			return message;
 		} else {
 			return null;
 		}
 	}
 	
 	protected byte[] readMessage(Socket s) throws IOException {
 		byte[] output = null;
 		DataInputStream in = new DataInputStream(s.getInputStream());
 		int len = in.readInt();
 		output = new byte[len];
 		in.read(output, 0, len);
 		return output;
 	}
 }
