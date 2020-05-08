 package com.cloudcontrol.doozer4j.support;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 
import org.apache.log4j.Logger;

 import com.cloudcontrol.doozer4j.exception.BadPathException;
 import com.cloudcontrol.doozer4j.exception.DoozerException;
 import com.cloudcontrol.doozer4j.exception.IsDirException;
 import com.cloudcontrol.doozer4j.exception.MissingArgumentException;
 import com.cloudcontrol.doozer4j.exception.NoEntryException;
 import com.cloudcontrol.doozer4j.exception.NotDirException;
 import com.cloudcontrol.doozer4j.exception.RangeException;
 import com.cloudcontrol.doozer4j.exception.ReadOnlyException;
 import com.cloudcontrol.doozer4j.exception.RevMismatchException;
 import com.cloudcontrol.doozer4j.exception.TooLateException;
 import com.cloudcontrol.doozer4j.exception.UnknownVerbException;
 import com.cloudcontrol.doozer4j.msg.Msg.Request;
 import com.cloudcontrol.doozer4j.msg.Msg.Request.Builder;
 import com.cloudcontrol.doozer4j.msg.Msg.Response;
 
 /**
  * This class provides simple doozer IO operations
  * 
  * @author Denis Neuling (dn@cloudcontrol.de)
  * 
  */
 public abstract class AbstractSynchroneousDoozerClient {
	private final Logger log = Logger.getLogger(this.getClass());
 	
 	private InetAddress inetAddress;
 	private int port;
 	
 	private Socket socket;
 	private BufferedOutputStream outputStream;
 	private BufferedInputStream inputStream;
 	
 	public abstract int getTimeout();
 	public abstract int getAttempts();
 	
 	public int getPort(){
 		return port;
 	}
 	public void setPort(int port){
 		this.port = port;
 	}
 	
 	public InetAddress getInetAddress(){
 		return inetAddress;
 	}
 	public void setInetAddress(InetAddress address){
 		this.inetAddress = address;
 	}
 	
 	public Socket getSocket(){
 		return socket;
 	}
 	public void setSocket(Socket socket){
 		this.socket = socket;
 	}
 	
 	public boolean isConnected(){
 		boolean connected = (this.socket!=null?this.socket.isConnected():false);
 		return connected;
 	}
 	
 	/**
 	 * Opens the connection to the channel
 	 * and provides input output operations from it
 	 * 
 	 * @throws IOException
 	 */
 	private void connect() throws IOException{
 		if(!isConnected()){
 			
 			InetAddress address = getInetAddress();
 			int port = getPort();
 			SocketAddress socketAddress = new InetSocketAddress(address, port);
 			
 			for(int i = 0; i < getAttempts() ; i ++){
 				try{
 				    socket = new Socket();
 				    socket.setSoTimeout(1000);
 				    int timeout = getTimeout();
 				    socket.connect(socketAddress, timeout);
 				    openInputStream();
 					openOutputStream();
 				    break;
 				}catch(IOException ioe){
					if(i>=getAttempts()-1){
						socket.close();
						socket = null;
						
						throw ioe;
					}else{
						log.info(ioe.getMessage());
					}
 				}
 			}
 		}
 	}
 	
 	private void openOutputStream() throws IOException{
 		connect();
 		outputStream = new BufferedOutputStream(socket.getOutputStream());
 	}
 	
 	private void openInputStream() throws IOException{
 		connect();
 		inputStream = new BufferedInputStream(socket.getInputStream());
 	}
 	
 	private void closeInputStream() throws IOException{
 		inputStream.close();
 	}
 
 	protected Response buildResponse(com.cloudcontrol.doozer4j.msg.Msg.Response.Builder builder){
 		return builder.build();
 	}
 	
 	@SuppressWarnings("unused")
 	protected byte[] receive() throws IOException{
 		byte[] lenght = new byte[4];
 		int responseSize = inputStream.read(lenght);
 		
 		byte[] responseBuffer = new byte[parseToInt32(lenght)];
 		int received = inputStream.read(responseBuffer);
 		
 		return responseBuffer;
 	}
 	
 	protected void write(Request request) throws IOException{
 		int size = request.getSerializedSize();
 		
 		openOutputStream();
 		outputStream.write(parseFromInt32(size));
 		outputStream.write(request.toByteArray());
 		outputStream.flush();
 	}
 	
 	protected com.cloudcontrol.doozer4j.msg.Msg.Response.Builder send(Builder builder) throws DoozerException, IOException{
 		Request request = builder.build();
 		write(request);
 		
 		try{
 			byte[] responseBuffer = receive();
 			
 			com.cloudcontrol.doozer4j.msg.Msg.Response.Builder responseBuilder = com.cloudcontrol.doozer4j.msg.Msg.Response.newBuilder();
 			responseBuilder.mergeFrom(responseBuffer);
 			
 			wrapError(responseBuilder);
 			
 			closeInputStream();
 			disconnect();
 			
 			return responseBuilder;
 		}catch(DoozerException dze){
 			disconnect();
 			throw dze;
 		}catch(IOException ioe){
 			disconnect();
 			throw ioe;
 		}
 	}
 	
 	/**
 	 * relieves all network resources
 	 * 
 	 * @throws IOException
 	 */
 	private void disconnect() throws IOException{
 		try{
 			if(outputStream!=null){
 				outputStream.close();
 				outputStream = null;
 				
 				/* (non-Javadoc)
 				 * 
 				 * outputstream.close() releases all associated ressources
 				 * so socket is affected too
 				 */
 			}
 			if(inputStream!=null){
 				inputStream.close();
 				inputStream = null;
 			}
 		}catch(IOException ioe){
 		}finally{
 			if(isConnected()){
 				
 				/* (non-Javadoc)
 				 * 
 				 * nevertheless we try to close the socket
 				 */
 				socket.close();
 				socket = null;
 			}
 		}
 	}
 
     /**
 	 * What do we do with the error codes?
 	 * 
 	 * OTHER        = 127;
 	 * TAG_IN_USE   = 1;
 	 * UNKNOWN_VERB = 2;
 	 * READONLY     = 3;
 	 * TOO_LATE     = 4;
 	 * REV_MISMATCH = 5;
 	 * BAD_PATH     = 6;
 	 * MISSING_ARG  = 7;
 	 * RANGE        = 8;
 	 * NOTDIR       = 20;
 	 * ISDIR        = 21;
 	 * NOENT        = 22;
 	 * 
 	 * @param responseBuilder the bytecoded response
 	 * @return String the error detail 
 	 * @throws DoozerException
 	 */
 	public String wrapError(com.cloudcontrol.doozer4j.msg.Msg.Response.Builder responseBuilder) throws DoozerException{
 		Response response = responseBuilder.build();
 		
 		String message = response.getErrDetail();
 		int code = response.getErrCode().getNumber();
 		
 		switch(code){
 		case 127 :
 			
 			/* (non-Javadoc)
 			 * 
 			 * that is mostly not an error
 			 */
 			break;
 		case 1 :
 			
 			/* (non-Javadoc)
 			 * 
 			 * that is currently not an error
 			 */
 			break;
 		case 2 :
 			throw new UnknownVerbException(message, code);
 		case 3 :
 			throw new ReadOnlyException(message, code);
 		case 4 :
 			throw new TooLateException(message, code);
 		case 5 :
 			throw new RevMismatchException(message, code);
 		case 6 :
 			throw new BadPathException(message, code);
 		case 7 :
 			throw new MissingArgumentException(message, code);
 		case 8 :
 			throw new RangeException(message, code);
 		case 20 :
 			throw new NotDirException(message, code);
 		case 21 :
 			throw new IsDirException(message, code);
 		case 22 :
 			throw new NoEntryException(message, code);
 		}
 		return message;
 	}
 	
 	public static int parseToInt32(byte[] bytes) {
         return ( bytes[0] <<  24)
              + ((bytes[1] & 0xFF) << 16)
              + ((bytes[2] & 0xFF) << 8)
              + ( bytes[3] & 0xFF)
         ;
     }
 
     public static byte[] parseFromInt32(int value) {
         byte[] bytes = new byte[4];
         bytes[3] = (byte)value;
         bytes[2] = (byte)(value >>> 8);
         bytes[1] = (byte)(value >>> 16);
         bytes[0] = (byte)(value >>> 24);
         return bytes;
     }
     
 }
