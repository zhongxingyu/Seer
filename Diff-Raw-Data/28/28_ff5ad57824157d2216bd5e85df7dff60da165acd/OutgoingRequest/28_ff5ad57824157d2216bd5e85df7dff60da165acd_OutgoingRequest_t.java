 package ch.nksa.pu.robotics.libs;
 
 import ch.nksa.pu.robotics.libs.Util;
 
 public class OutgoingRequest extends Request{
 	protected boolean hasBeenSent = false;
 	protected Thread sentTest;
 	
 	public OutgoingRequest(String sender, String nick, String subject, byte[][] data){
 		super(sender, nick, subject, data);
 		initializeRequest(sender, nick, subject, data);
 	}
 	
 	public OutgoingRequest(String sender, String subject, byte[][] data){
 		super(subject, "default", subject, data);
 		initializeRequest(sender, nick, subject, data);
 	}
 
 	
 	private OutgoingRequest(RequestMode mode, Request reference, 
 							String sender, String nick, String subject, byte[][] data){
 		super(mode, sender, nick, subject, data);
 		this.mode = mode;
 		this.reference = reference;
 		initializeRequest(sender, nick, subject, data);
 	}
 	
 	private void initializeRequest(String sender, String nick, String subject, byte[][] data){
 		this.sender = sender;
 		this.nick = nick;
 		this.subject = subject;
 		this.data = data;
 		sentTest = new Thread(){
 			public void run(){
 				while(!isSent()){}
 				System.out.println("Request " + id + " has been sent.");
 			}
 		};
 		sentTest.start();
 		Uplink.getInstance().registerRequest(this);
 	}
 	
 	public OutgoingRequest followUp(byte[][] data){
 		return new OutgoingRequest(RequestMode.FOLLOW_UP, this, sender, nick, subject, data);
 	}
 	
 	public OutgoingRequest followUp(String new_subject, byte[][] data){
 		return new OutgoingRequest(RequestMode.FOLLOW_UP, this, sender, nick, new_subject, data);
 	}
 	
 	protected Thread getSendCheckThread(){
 		return sentTest;
 	}
 	
 	public boolean isSent(){
 		return hasBeenSent;
 	}
 	
 
 	
 	public Thread getWaitingThread(){
 		Thread temp = new Thread(){
 			public void run(){
 				//TODO implement waitForreply
 			}
 		};
 		temp.start();
 		return temp;
 	}
 	
 	public void waitForReply() throws InterruptedException{
 		getWaitingThread().join();
 		return;
 	}
 	
 	public byte[][] getHeader(){
 		//id
 		//RequestMode
 		//ReferenceId
 		//Sender
 		//Nick
 		//Subject
		byte[][] header = new byte[6][];
 		header[0] = Util.intToBytes(id);
 		header[1] = Util.stringToBytes(mode.name());
		header[2] = Util.intToBytes(getReferenceId());
 		
 		header[3] = Util.stringToBytes(this.sender);
 
 		header[4] = Util.stringToBytes(nick);
 		
 		header[5] = Util.stringToBytes(subject);
		return header;
 	}
 }
