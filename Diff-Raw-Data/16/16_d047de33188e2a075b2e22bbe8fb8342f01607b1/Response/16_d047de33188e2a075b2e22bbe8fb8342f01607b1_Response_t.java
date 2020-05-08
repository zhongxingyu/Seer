 package webserver;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Response {
 	public Map<String,String> headers;
	public byte[] code = null;
 	
 	public static final int BUFFER_SIZE = 8192; //8Kb
 	
 	static final byte[] NEW_LINE = {13,10};
 	
 	private boolean headersSent;
 	private Client client;
 	public int httpMajor;
 	public int httpMinor;
 	
 	public Response(Client client){
 		headers = new HashMap<String,String>();
 		headersSent = false;
 		httpMajor=0;
 		httpMinor=0;
 		this.client = client;
 	}
 
 	public void sendHeaders() {
 		if(!headersSent){
 			headersSent = true;
 			
 			ByteBuffer[] bb = new ByteBuffer[headers.size()+3];
 			
 			if(code == null){
 				code = STATUS_405; //noone handled the request, return method not allowed.
 			}
 			
 			bb[0] = ByteBuffer.wrap(("HTTP/"+httpMajor+"."+httpMinor+" ").getBytes());
			bb[1] = ByteBuffer.wrap(code);
 			
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
 		
 	private static final int chunkSize = 1048576; //1 MB
 	public void sendFile(FileChannel file, long size){
 		try {
 			sendHeaders();
 			long position = 0;
 			while(position < size){
 				position += file.transferTo(position, size, client.ch);
 			}
 		} catch (IOException e) {
 			System.err.println("Error: could not send file, closing connection");
 			e.printStackTrace();
 			client.close();
 		}
 		end();
 	}
 
 	public void end() {
 		client.requestFinished();
 		client.close();//TODO: persistent connection
 	}
 	
	public static final byte[] STATUS_200 = "200 OK\r\n".getBytes(),
			STATUS_404 = "404 Not Found\r\n".getBytes(),
			STATUS_405 = "405 Method Not Allowed\r\n".getBytes(),
			STATUS_500 = "500 Internal Server Error\r\n".getBytes(),
			STATUS_505 = "505 HTTP Version Not Supported\r\n".getBytes();
 	
 }
