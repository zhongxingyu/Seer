 package webserver;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Response {
 	
 	public Map<String, String> headers;
 	public byte[] code = null;
 
 	public static final int BUFFER_SIZE = 8192; // 8Kb
 
 	static final byte[] NEW_LINE = { 13, 10 };
 
 	private boolean headersSent;
 	private Client client;
 	public int httpMajor;
 	public int httpMinor;
 
 	public boolean closeAfterEnd;
 
 	public Response(Client client) {
 		headers = new HashMap<String, String>();
 		headersSent = false;
 		httpMajor = 0;
 		httpMinor = 0;
 		closeAfterEnd = true;
 		this.client = client;
 	}
 	
 	protected FileChannel fileToSend;
 	private long fileSize = 0;
 
 	public void sendFile(FileChannel file, long size) {
 		fileToSend = file;
 		fileSize = size;
 	}
 
 	public void end() {
 		client.requestFinished();
 
 		if (closeAfterEnd) {
 			client.close();
 		}
 	}
 	
 	private long filePosition = 0;
 	private ByteBuffer[] hb;
 	
 	//returns true if everything written, false if more to write.
 	public boolean write() {
 		
 		if(hb == null){
 			hb = new ByteBuffer[headers.size() + 3];
 
 			if (code == null) {
 				code = STATUS_405; // noone handled the request, return method
 									// not allowed.
 			}
 
 			hb[0] = ByteBuffer
 					.wrap(("HTTP/" + httpMajor + "." + httpMinor + " ")
 							.getBytes());
 			hb[1] = ByteBuffer.wrap(code);
 
 			int i = 2;
 			for (Entry<String, String> header : headers.entrySet()) {
 				hb[i] = ByteBuffer.wrap((header.getKey() + ": "
 						+ header.getValue() + "\r\n").getBytes());
 				i++;
 			}
 
 			hb[i] = ByteBuffer.wrap(NEW_LINE);
 		}
 		
 		if (!headersSent) {
 			try {
 				client.ch.write(hb);
 				if(hb[hb.length-1].remaining() == 0){
 					headersSent = true;
 				}else{
 					return false;
 				}
 			} catch (IOException e) {
 				System.err.println("Could not write to client");
 				client.close();
 			}
 		}
 		
 		if(fileToSend != null){
 			try {
 				filePosition += fileToSend.transferTo(filePosition, fileSize, client.ch);
 				if(filePosition != fileSize){
 					return false; //write again
				}else{
					fileToSend.close();
 				}
 			} catch (IOException e) {
 				System.err.println("Error: could not send file, closing connection");
 				client.close();
 			}
 		}
 		
 		//if we get here, the request is finished! 
 		end();
 		return true;
 	}
 	
 	public static final byte[] STATUS_200 = "200 OK\r\n".getBytes(),
 			STATUS_404 = "404 Not Found\r\n".getBytes(),
 			STATUS_405 = "405 Method Not Allowed\r\n".getBytes(),
 			STATUS_500 = "500 Internal Server Error\r\n".getBytes(),
 			STATUS_505 = "505 HTTP Version Not Supported\r\n".getBytes(), 
 			STATUS_304 = "304 Not Modified\r\n".getBytes();
 }
