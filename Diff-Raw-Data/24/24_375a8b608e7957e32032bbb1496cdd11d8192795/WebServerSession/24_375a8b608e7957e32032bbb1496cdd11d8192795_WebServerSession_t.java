 package com.nexus.webserver;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 
 import com.nexus.NexusServer;
 
 public class WebServerSession{

	public final SocketChannel Channel;
	public final SelectionKey Key;
 	
 	private final ByteBuffer buffer = ByteBuffer.allocate(2048);
 	public StringBuilder readLines = new StringBuilder();
 	private int mark = 0;
 	
 	private final Charset charset = Charset.forName("UTF-8");
 	private final CharsetEncoder encoder = charset.newEncoder();
 	
 	private boolean SupportsGZIP = false;
 	
	public WebServerSession(SocketChannel Channel, SelectionKey Key){
 		this.Channel = Channel;
		this.Key = Key;
 	}
 	
 	public void SetSupportsGZIP(boolean support){
 		this.SupportsGZIP = support;
 	}
 	
 	public void reset(){
 		this.mark = 0;
 		this.buffer.clear();
 		this.readLines = new StringBuilder();
 	}
 	
 	public String readLine() throws IOException{
 		StringBuilder sb = new StringBuilder();
 		int l = -1;
 		while (buffer.hasRemaining()){
 			char c = (char) buffer.get();
 			sb.append(c);
 			if(c == '\n' && l == '\r'){
 				mark = buffer.position();
 				readLines.append(sb);
 				return sb.substring(0, sb.length() - 2);
 			}
 			l = c;
 		}
 		if(!sb.toString().isEmpty()){
 			readLines.append(sb);
 			return sb.toString();
 		}
 		return null;
 	}
 	
 	public void readData() throws IOException{
 		buffer.limit(buffer.capacity());
 		int read = Channel.read(buffer);
 		if(read == -1){
 			throw new IOException("End of stream");
 		}
 		buffer.flip();
 		buffer.position(mark);
 	}
 	
 	public void write(String line) throws IOException{
 		NexusServer.Instance.Profiler.StartUplinkProfiling();
 		Channel.write(encoder.encode(CharBuffer.wrap(line)));
 		NexusServer.Instance.Profiler.EndUplinkProfiling(line.length());
 	}
 	
 	public OutputStream getOutputStream() throws IOException{
 		return this.Channel.socket().getOutputStream();
 	}
 	
 	public void writeLine(String line) throws IOException{
 		this.write(line + "\r\n");
 	}
 	
 	public void Close(){
 		try{
 			Channel.close();
 			buffer.clear();
			Key.cancel();
 			this.reset();
 		}catch(IOException ex){
 		}
 	}
 	
 	public boolean IsSupportingGZIP(){
 		return this.SupportsGZIP;
 	}
 }
