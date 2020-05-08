 package com.nexus.webserver;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import com.nexus.logging.NexusFormattedLogger;
 import com.nexus.main.EnumShutdownCause;
 import com.nexus.main.ShutdownManager;
 
 
 public class WebServer implements Runnable{
 	private boolean IsRunning = false;
 	
 	private final InetSocketAddress Address;
 	
 	public static Logger Log = NexusFormattedLogger.getLogger("WebServer");
 
 	private ServerSocket Socket;
 	private ServerSocketChannel SocketChannel;
 	private Selector SocketSelector;
 	
 	private Thread WebserverThread;
 	
 	public WebServer(InetSocketAddress Address){
 		this.Address = Address;
 	}
 
 	//@SuppressWarnings("resource")
 	@Override
 	public void run(){
 		WebServer.Log.info("Server starting...");
 		try{
 			SocketChannel = ServerSocketChannel.open();
 			SocketChannel.configureBlocking(false);
 			Socket = this.SocketChannel.socket();
 			Socket.bind(this.Address);
 			SocketSelector = Selector.open();
 			SocketChannel.register(SocketSelector, SocketChannel.validOps());
 		}catch (IOException e){
 		    WebServer.Log.severe("Could not listen on port " + this.Address.getPort() + "!");
 		    return;
 		}
 		WebServer.Log.info("Server started!");
 		
 		while(IsRunning){
 			SelectionKey Key = null;
 			try{
 				SocketSelector.select();
 				Set<SelectionKey> keys = SocketSelector.selectedKeys();
 				Iterator<SelectionKey> i = keys.iterator();
 				
 				while(i.hasNext()){
 					Key = i.next();
 					i.remove();
 					
 					if(!Key.isValid()){
 						continue;
 					}
 					try{
 						if(Key.isAcceptable()){
 							SocketChannel Channel = SocketChannel.accept();
 	                        Channel.configureBlocking(false);
 	                        Channel.register(SocketSelector, SelectionKey.OP_READ);
 	                    }else if(Key.isReadable()){
 							/*SocketChannel Channel = (SocketChannel) Key.channel();
 	                        WebServerSession Session = (WebServerSession) Key.attachment();
 	                        if(Session == null){
 	                        	Session = new WebServerSession(Channel);
 	                            Key.attach(Session);
 	                        }
 	                        Session.readData();
 							
 	                        WebServer.Log.fine("Connected by " + Channel.getRemoteAddress().toString().substring(1).split(":")[0]  + ".");
 							
 							WebServerClientThread.LaunchNewThread(Session);*/
 	                    	SocketChannel Channel = (SocketChannel) Key.channel();
 	                        WebServerSession Session = (WebServerSession) Key.attachment();
 	                        if(Session == null){
 	                        	Session = new WebServerSession(Channel);
 	                            Key.attach(Session);
 	                        }
	                        WebServer.Log.finest("Connected by " + Channel.socket().getInetAddress().toString().split("/")[1]  + ".");
 							
 	                        Session.readData();
                             while(Session.readLine() != null){}
 	                        
 							WebServerClientThread.LaunchNewThread(Session);
 	                    }
 					}catch(Exception e){
                         if(Key.attachment() instanceof WebServerSession){
                             ((WebServerSession) Key.attachment()).Close();
                         }
 					}
 				}
 			}catch(IOException e){
 				WebServer.Log.severe("Error while accepting connections.");
 				e.printStackTrace();
 				this.IsRunning = false;
 				ShutdownManager.ShutdownServer(EnumShutdownCause.CRASH);
 			}
 		}
 		try{
 			this.Socket.close();
 			this.SocketSelector.close();
 			this.SocketChannel.close();
 		}catch(IOException e){}
 	}
 	
 	public void Start(){
 		this.WebserverThread = new Thread(this);
 		this.WebserverThread.setName("WebServerListener");
 		this.IsRunning = true;
 		this.WebserverThread.start();
 	}
 	
 	public void Stop(){
 		this.IsRunning = false;
 		
 		try{
 			this.Socket.close();
 			this.SocketSelector.close();
 			this.SocketChannel.close();
 		}catch(IOException e){
 
 		}
 	}
 }
