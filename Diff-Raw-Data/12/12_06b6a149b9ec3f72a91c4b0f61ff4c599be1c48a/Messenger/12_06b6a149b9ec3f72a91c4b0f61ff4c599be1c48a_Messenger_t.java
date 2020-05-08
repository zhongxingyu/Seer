 package de.piratenpartei.id;
 
 import java.io.*;
 
 import org.json.simple.*;
 
 public class Messenger {
 	private PrintWriter outputStream;
 	private Account author;
 	
 	public Messenger(Account author){
 		this.author = author;
 		this.outputStream = new PrintWriter(System.out);
 	}
 	
 	public Messenger(Account author, String outputPath) throws FileNotFoundException{
 		this.author = author;
 		this.outputStream = new PrintWriter(outputPath);
 	}
 	
 	public void sendMessage(String s) throws IOException, IllegalFormatException, KeyException, VerificationException{
 		Message m = new Message(this.author);
 		m.setMessage(s);
 		m.send(this.outputStream);
		this.outputStream.flush();
 	}
 
 	public void sendVote(int topicID, Vote vote, String type) throws IOException, IllegalFormatException, KeyException, VerificationException{
 		TextStore ts1 = new TextStore();
 		TextStore ts2 = new TextStore();
 		ts1.put("type",type);
 		ts1.put("vote", vote);
 		ts1.put("topic", topicID);
 		ts2.put("vote", ts1);
 		this.sendMessage(ts2.toString());
 	}
 	public void sendMessageToUser(String userName, String message) throws IOException, IllegalFormatException, KeyException, VerificationException{
 		TextStore ts1 = new TextStore();
 		TextStore ts2 = new TextStore();
 		ts1.put("userName",userName);
 		ts1.put("message",message);
 		ts2.put("userMsg",ts1);
 		this.sendMessage(ts2.toString());
 	}
 	public void sendNickchange(String newNick) throws IOException, IllegalFormatException, KeyException, VerificationException{
 		JSONObject jo1 = new JSONObject();
 		JSONObject jo2 = new JSONObject();
 		jo1.put("newNick", newNick);
 		jo2.put("nickChange", jo1);
 		this.sendMessage(jo2.toString());
 	}
 }
