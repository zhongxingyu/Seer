 package raddar.controllers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.text.DateFormat;
 
 import raddar.enums.MessageType;
 import raddar.models.Message;
 import raddar.models.TextMessage;
 import android.util.Log;
 
 public class Reciver implements Runnable {
 
 	private Thread thread = new Thread(this);
 	private BufferedReader in;
 	private Socket so;
 	private ReciveController rc;
 
 	public Reciver(Socket so,ReciveController rc) {
 		this.so = so;
 		this.rc = rc;
 		thread.start();
 	}
 
 	public void run() {
 		try {
 
 			in = new BufferedReader(new InputStreamReader(so.getInputStream()));
 			String msgType = in.readLine();
 			String[] parts = msgType.split(" ");
 			String t = parts[1];
 			MessageType type = MessageType.convert(t);
 			Message m = null;
 			String prio = in.readLine().split(" ")[1];
 			String srcUser = in.readLine().split(" ")[1];
 			String toUser = in.readLine().split(" ")[1];
 			in.readLine();
 		//	String[] dateParts = in.readLine().split(" ");
 		//	DateFormat df = DateFormat.getDateTimeInstance();
 		//	Date  date = new Date(Date.parse(in.readLine()));
			String subject = in.readLine();
			subject = extractValue(subject);
 			switch (type){
 			case TEXT:
 				m = new TextMessage(type,srcUser,toUser);
 			//	m.setDate(date);
 				m.setSubject(subject);
 				in.readLine();
 				String data = "";
 				while (in.ready())
 					data +=in.readLine();
 				m.setData(data);
 				break;
 			case IMAGE:
 
 				break;			
 			}			
 			
 			so.close();
 			rc.addToInbox(m);
 			
 		} catch (IOException ie) {
 			ie.printStackTrace();
 		}
 		catch(ArrayIndexOutOfBoundsException e){
 			Log.d("Undersk","ArrayIndexOutOfBounds i reciver");
 		}
 
 	}
	
	private String extractValue(String str) {
		int index = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ' ') { 
				index = i;
				break;
			}
		}
		return str.substring(index);
	}
 }
