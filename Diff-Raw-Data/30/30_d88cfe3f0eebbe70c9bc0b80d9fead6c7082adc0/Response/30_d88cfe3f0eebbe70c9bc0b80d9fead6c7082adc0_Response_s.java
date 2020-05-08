 package webserver;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Response {
 	public Map<String,String> headers;
 	public ByteBuffer code = null;
 	
 	public static final int BUFFER_SIZE = 8192; //8Kb
 	
 	static final byte[] NEW_LINE = {13,10};
 	
 	private boolean headersSent;
 	private Client client;
 	public int httpMajor;
 	public int httpMinor;
 	
 	public Response(Client client){
 		headers = new HashMap<String,String>();
 		headersSent = false;
 		this.client = client;
 	}
 
 	public void sendHeaders() {
 		if(!headersSent){
 			headersSent = true;
 			
 			ByteBuffer[] bb = new ByteBuffer[headers.size()+3];
 			
 			if(code == null){
 				code = STATUS_405; //noone handled the request, return method not allowed.
 			}
 			
			bb[0] = ByteBuffer.wrap(("HTTP/"+httpMajor+"."+httpMinor).getBytes());
 			bb[1] = code;
 			
 			int i = 2;
 			for(Entry<String, String> header: headers.entrySet()){
 				bb[i] =ByteBuffer.wrap((header.getKey() + ": " + header.getValue() + "\r\n").getBytes());  //TODO: make more efficient.
 				i++;
 			}
 			
 			bb[i] = ByteBuffer.wrap(NEW_LINE);
 			try {
 				client.ch.write(bb); //TODO: wait for key.isWritable?
 			} catch (IOException e) {
 				System.err.println("Could not write to client");
 				e.printStackTrace();
 				client.close();
 			}
 		}
 	}
 		
 	public void sendFile(FileChannel file){
 		try {
 			sendHeaders();
 			file.transferTo(0, Long.MAX_VALUE, client.ch); //TODO while loop
 		} catch (IOException e) {
 			System.err.println("Error: could not send file, closing connection");
 			e.printStackTrace();
 			client.close();
 		}
 		end();
 	}
 
 	public void end() {
 		client.requestFinished();
 	}
 	
 	public static final ByteBuffer STATUS_200 = ByteBuffer.wrap("200 OK\r\n".getBytes()),
 			STATUS_404 = ByteBuffer.wrap("404 Not Found\r\n".getBytes()),
 			STATUS_405 = ByteBuffer.wrap("405 Method Not Allowed\r\n".getBytes()),
 			STATUS_500 = ByteBuffer.wrap("500 Internal Server Error\r\n".getBytes()),
 			STATUS_505 = ByteBuffer.wrap("505 HTTP Version Not Supported\r\n".getBytes());
 	
 }
