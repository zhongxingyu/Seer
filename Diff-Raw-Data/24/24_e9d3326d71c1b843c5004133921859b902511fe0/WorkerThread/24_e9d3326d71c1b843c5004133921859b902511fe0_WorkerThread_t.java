 package edu.cs305.paintproject.server;
 
 import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Vector;
 
 import javax.swing.ImageIcon;
 
 import edu.cs305.paintproject.Constants;
 import edu.cs305.paintproject.DrawCommands;
 import edu.cs305.paintproject.util.Logger;
 
 public class WorkerThread extends Thread {
 	
 	boolean available;
 	ObjectInputStream inFromClient;
 	private ObjectOutputStream outToClient;
 	ArrayList<WorkerThread> sessionMembers;
 	PaintServer server;
 	private String currentSession;
 	BufferedImage image;
 
 	public WorkerThread(ObjectInputStream inFromClient, ObjectOutputStream outToClient, PaintServer server){
 		this.inFromClient = inFromClient;
 		this.outToClient = outToClient;
 		this.server = server;
 		
 		sessionMembers = new ArrayList<WorkerThread>();
 		available = true;
 		currentSession = "lobby";
 		image = null;
 	}
 	
 	public synchronized boolean send(Object data, String sessionOrigin){
 		if(data instanceof Integer && (Integer)data == Constants.LOGOUT){
 			try{
 				outToClient.close();
 				available = false;
 			}
 			catch(IOException ioe){
 				Logger.log(ioe,"while closing output stream...");
 			}
 		}
 		
 		if(!available || !sessionOrigin.equals(currentSession))
 			return false;
 		
 		try{
 			outToClient.writeObject(data);
 		}
 		catch(IOException ioe){
 			Logger.log(ioe,"IOException while sending to client...");
 		}
 		
 		return true;
 	}
 	
 	private synchronized void setCurrentSession(String currentSession){
 		this.currentSession = currentSession;
 	}
 	
 	public void run(){
 		try{
 			
 			while(true){
 				Object data = inFromClient.readObject();
 				
 				if(data instanceof PictureRequest){
 					PictureRequest request = (PictureRequest)data;
 					try{
 						setCurrentSession(request.pictureName);
 						image = server.exitLobbyTo(this, currentSession);
 						ImageIcon i = new ImageIcon(image);
 						
 						
 						outToClient.writeObject(i);
 						outToClient.flush();
 					}
 					catch(IOException ioe){
 						Logger.log(ioe,"IOException while accessing picture file...");
 					}
 				}
 				else if(data instanceof Integer){
 					int message = (Integer)data;
 					
 					if(message == Constants.LOGOUT){
 						if(!currentSession.equals("lobby")){
 							server.returnToLobbyFrom(this, currentSession, image);
 							setCurrentSession("lobby");
 						}
 						send(data, currentSession);
 						break;
 					}
 					else if(message == Constants.REQUEST_PICTURE_NAMES){
 						Logger.log(currentSession);
 						send(getFiles(new File("Server/Pictures")), currentSession);
 					}
 					else if(message == Constants.EXIT_TO_LOBBY){
 						server.returnToLobbyFrom(this, currentSession, image);
 						setCurrentSession("lobby");
 					}
 				}
 				else{	//some type of draw command
 					DrawCommands.drawCommand(data, image.getGraphics());
 					for(int i=0; i<sessionMembers.size(); i++){
 						WorkerThread thread = sessionMembers.get(i);
 						
 						if(!thread.send(data, currentSession))
 							sessionMembers.set(i, null);
 					}
 					
 					while(sessionMembers.remove(null)){}
 				}
 				
 			}
 		}
 		catch(IOException ioe){
 			Logger.log(ioe,"IOException in thread...");
 		}
 		catch(ClassNotFoundException cnfe){
 			Logger.log(cnfe,"ClassNotFoundException in thread...");
 		}
 		Logger.log("Exiting Thread...");
 	}
 	
 	public Vector<String> getFiles(File folder){
 		folder.mkdirs();
 		Vector<String> fileNames = new Vector<String>();
 		for(File file: folder.listFiles()){
 			if(!file.isDirectory())
 				fileNames.add(file.getName());
 		}
 		return fileNames;
 	}
 }
