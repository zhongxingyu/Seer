 package asl.network;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 public class SocketTransport implements ITransport {
 	private Socket socket;
 	
 	BufferedInputStream inputStream;
 	BufferedOutputStream outputStream; 
 	
 	public SocketTransport(Socket socket) throws IOException{
 		this.socket = socket;	
 		
 		inputStream = new BufferedInputStream(socket.getInputStream());
 		outputStream = new BufferedOutputStream(socket.getOutputStream());
 	}
 	
 	public void Send(String data) throws IOException{
 		outputStream.write(data.getBytes("UTF-8"));
 		outputStream.write(0); // request terminator
 		outputStream.flush();
 	}
 	
 	public String Read() throws IOException{
 		StringBuilder builder = new StringBuilder();
 		int bufferSize = 65536; // 64KiB
 		byte[] buffer = new byte[bufferSize];
 		int readBytes = 0;
 		
 		while((readBytes = inputStream.read(buffer)) > 0){
 			
 			if(buffer[readBytes-1] == 0){ // EoF token
 				builder.append(new String(buffer, 0, readBytes-1, "UTF-8"));
 				break;
 			}
 			else
 				builder.append(new String(buffer, "UTF-8"));
 		}
 		
 		return builder.toString();
 	}
 	
 	public String SendAndGetResponse(String data) throws IOException{
 		Send(data);
 		return Read();
 	}
 
 	public Socket getSocket() {
 		return socket;
 	}
 
 	@Override
 	public void disconnect() throws IOException {
 		socket.close();
 	}
 }
