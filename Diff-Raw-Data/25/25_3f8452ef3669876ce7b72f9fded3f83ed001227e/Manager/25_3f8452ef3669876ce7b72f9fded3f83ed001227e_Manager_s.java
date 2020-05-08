 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ConnectException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.io.FileOutputStream;
 import javax.swing.JOptionPane;
 
 public class Manager implements Runnable{
 	private InputStream input;
 	private ObjectOutputStream ooutput;
 	private ObjectInputStream oinput;
 	private ServerSocket server;
 	private String location, ip, type, dlto, id;
 	private Socket socket;
 	private int progress = 0;
 	
 	public Manager(String location){
 		this.location = location;
 	}
 	
 	public Manager(String location, String type, String ip, String dlto, String id){
 		System.out.println(type);
 		
 		this.location = location;
 		this.ip = ip;
 		this.type = type;
 		this.dlto = dlto;
 		this.id = id;
 	}
 	
 	public void run(){		
 		try{
 			if(type.equals("distributor")){
 				while(true){
					System.out.println(id);
					server = new ServerSocket(666, 69);
 					
 					socket = server.accept();
 					
 					oinput = new ObjectInputStream(socket.getInputStream());
					
 					Object obj = oinput.readObject();
 					
					while(!(obj instanceof Command)){
						System.out.println(obj.toString());
						obj = oinput.readObject();
					}
 					
 					Command command = (Command) obj;
 					
 					server.close();
 					
 					Provider provider = new Provider(command);
 					
 					Thread thread = new Thread(provider);
 					thread.start();
 					
 					socket.close();
 					oinput.close();
 				}
 			}
 			else if(type.equals("client")){
 				socket = new Socket(this.ip, Integer.parseInt(id));
 				
 				ooutput = new ObjectOutputStream(socket.getOutputStream());
 				
 				Command command = new Command("GET", location, Main.status.getIP(), id);
 				
 				ooutput.writeObject(command);
 				ooutput.flush();
 				
				socket.close();
				ooutput.close();
				
 				server = new ServerSocket(Integer.parseInt(id), 69);
 				
 				socket = server.accept();
 				
 				oinput = new ObjectInputStream(socket.getInputStream());
 				input = socket.getInputStream();
 				
 				FileOutputStream foutput = new FileOutputStream(dlto);
 				byte[] buffer = new byte[32 * 1024];
 				
 				int read = 0, total = 0, size = 0;
 				
 				size = (Integer) oinput.readObject();
 				
 				while((read = input.read(buffer)) != -1){
 					total += read;
 					foutput.write(buffer, 0, read);
 
 					progress = (int) ((double) total / size * 100);
 					
 					Main.frame.getHub().updateProgress(dlto, progress);
 					
 					if(total / size == 1){
 						break;
 					}
 				}
 				
 				foutput.flush();
 				foutput.close();
 				ooutput.close();
 				oinput.close();
 				input.close();
 				socket.close();
 				server.close();
 				
 				JOptionPane.showMessageDialog(null, "Download finished.");
 			}
 		}
 		catch(ConnectException e){
 			e.printStackTrace();
 		}
 		catch(IOException e){
 			e.printStackTrace();		
 		}
 		catch(ClassCastException e){
 			e.printStackTrace();
 		}
 		catch(ClassNotFoundException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public double getProgress(){
 		return progress;
 	}
 }
