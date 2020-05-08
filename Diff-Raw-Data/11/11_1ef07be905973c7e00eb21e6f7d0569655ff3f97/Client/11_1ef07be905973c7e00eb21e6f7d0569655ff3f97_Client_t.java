 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 
 import java.io.PrintWriter;
 import java.io.BufferedReader;
 
 import java.net.Socket;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Scanner;
 
 // http://java.sun.com/j2se/1.5.0/docs/api/
 
 public class Client implements Runnable {
 
 	
 	private Socket s;
 	private OutputStream os;
 	private InputStream is;
 	private PrintWriter out;
 	private BufferedReader in;
 	private String serv_msg;
 	private String our_msg;
 	public String users;
 	public String messages="";
 	public boolean state=false;
 	public boolean userschanged=false;
 	public boolean messageschanged=false;
 	Scanner inscan;
 	public synchronized  void connect() {
 		
 		try {
			s = new Socket("192.168.1.102", 1234);
 			os = s.getOutputStream();
 			is = s.getInputStream();
 			inscan = new Scanner(is);
 			out = new PrintWriter(new OutputStreamWriter(os));
 			in = new BufferedReader(new InputStreamReader(is));
 			String hello_msg = in.readLine();
 			if (hello_msg != null) {
 				 messages+=hello_msg+"\r\n";
 				 messageschanged=true;
 			}
 			state=true;	
 		} 
 		catch (UnknownHostException e) {
 			
 			e.printStackTrace();
 			
 		} 
 		catch (IOException e) {
 			
 			e.printStackTrace();
 			
 		}
 
 	}
 	public synchronized void disconnect(){
 		try {	
 			our_msg = "bye";
 			out.println(our_msg);
 			out.flush();			
 			serv_msg=in.readLine();
 			s.close();
 			state=false;			
 		}catch (UnknownHostException e) {
 			
 			e.printStackTrace();
 			
 		} 
 		catch (IOException e) {
 			
 			e.printStackTrace();
 		}
 		
 	}
 	public void Send(String st){
 		out.println(st);
 		out.flush();
 	}
 	@Override
 	public  void run() {
 		
 		while (state){
 			while (inscan.hasNext()) {
 				  String line = inscan.nextLine();
 				  
 				  if(line.startsWith("200"))
 				  {	  
					  if(line.contains("200 ok users"))
 					  {	users="";
 						  String[] userList = line.split(" ");
						  for(int i=3; i<userList.length; i++) {
 							  users+=userList[i]+"\n";
 						  }
 						 userschanged = true ;
 					  }
 					  if(line.contains("registered")){
 					  char[] MsgList = line.toCharArray();
 					  for(int i=3; i<MsgList.length; i++) {
 						  messages+=MsgList[i];
 					  }
 					  }
 					  else{
 						  System.out.println(line);
 					  }
 					  messages+="\r\n";
 					 messageschanged = true ;
 				  }
 				  else if(line.startsWith("400"))
 				  {	
 					  our_msg="list";
 					  out.println(our_msg);
 					  out.flush();
 					  }
 				  else if(line.startsWith("300"))
 				  {	
 					  System.out.println(line);
 					  }
 				  else{
 					  if(line.startsWith("100")){
 					  messages+=line+"\n";
 					  messageschanged = true ;
 					  }
 					  else{
					  messages+=line+"\n";
 					  messageschanged = true ;
 						  
 					  }
 				  } 
 					  
 				  }
 				
 				 
 			}
 		}
 			
 	}
 	
 	
 
