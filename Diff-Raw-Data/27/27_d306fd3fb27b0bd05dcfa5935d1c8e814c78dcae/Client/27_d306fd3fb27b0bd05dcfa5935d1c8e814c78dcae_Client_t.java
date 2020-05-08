 package node;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Scanner;
 import java.util.UUID;
 import java.util.logging.Level;
 
 import message.ClientOpMsg;
 import message.SiteCrashMsg;
 import common.Common;
 import common.Common.SiteCrashMsgType;
 import common.Common.State;
 import common.MessageWrapper;
 import common.Triplet;
 import common.Tuple;
 
 public class Client extends Node implements Runnable{
 	private String paxosLeaderOneId;
 	private String paxosLeaderTwoId;
 
 	public Client(String nodeId,String paxosLeaderOneId,String paxosLeaderTwoId) throws IOException {		
 		super(nodeId, "");
 		this.paxosLeaderOneId=paxosLeaderOneId;
 		this.paxosLeaderTwoId=paxosLeaderTwoId;
 		ArrayList<Triplet<String, String, Boolean>> exchanges = new ArrayList<Triplet<String, String, Boolean>>();
 		exchanges.add(new Triplet(Common.DirectMessageExchange, Common.directExchangeType, true));
 		this.DeclareExchanges(exchanges);
 		this.InitializeConsumer();
 	}
 
 	public void ProcessInput() throws IOException, InterruptedException
 	{
 		String requesttype,request;
 		int destid;
 		String sitecrashid;
 		int sitecrashflag=0;
 		ClientOpMsg msg;
 
 		while(true)
 		{
 			Scanner in = new Scanner(System.in);			 
 			System.out.print("\nEnter a reqeust type (Read/Append/Crash) - ");
 			requesttype = in.nextLine();
 
 			if(requesttype.equals("Read"))
 			{
 				System.out.print(" Enter a destination id (1 or 2) - ");
 				
 				destid = in.nextInt();
 				UUID uid = java.util.UUID.randomUUID();
 
 				msg= new ClientOpMsg(this.nodeId, Common.ClientOPMsgType.READ, "READ", uid);
 				this.sendClientOpMsg(msg, (destid==1)?this.paxosLeaderOneId:this.paxosLeaderTwoId);
 				System.out.println(" Created new request with uid - " + uid);
 				this.run();
 			}
 			else if (requesttype.equals("Append"))
 			{
 				System.out.print(" Enter data to be inserted. (':' separated) - ");
 				request = in.nextLine();				
 				String[] data = request.split(":");
 
 				UUID uid = java.util.UUID.randomUUID();
 				ClientOpMsg msg1 = new ClientOpMsg(this.nodeId, Common.ClientOPMsgType.APPEND, data[0], uid);
 				ClientOpMsg msg2 = new ClientOpMsg(this.nodeId, Common.ClientOPMsgType.APPEND, data[1], uid);
 
 				this.sendClientOpMsg(msg1, this.paxosLeaderOneId);				
 				this.sendClientOpMsg(msg2, this.paxosLeaderTwoId);
 				System.out.println(" Created new request with uid - " + uid);
 				this.run();	
 			}
 			else if(requesttype.equals("Crash"))
 			{
 				System.out.print(" Enter a destination id - ");
 				sitecrashid = in.nextLine();
 				System.out.print(" Crash(1) or Recover(2) - ");
 				sitecrashflag = in.nextInt();
 				if(sitecrashflag==1)
 				{
 					SiteCrashMsg sitecrashmsg=new SiteCrashMsg(this.nodeId, SiteCrashMsgType.CRASH);
					
					StringBuffer sb = new StringBuffer();
					sb.append("Node - "+sitecrashid);
					sb.append("Sent - "+ sitecrashmsg);
					this.AddLogEntry(sb.toString(), Level.INFO);
					
 					sendSiteCrashMsg(sitecrashmsg, sitecrashid);
 				}
 				else if(sitecrashflag ==2)
 				{
 					SiteCrashMsg sitecrashmsg=new SiteCrashMsg(this.nodeId, SiteCrashMsgType.RECOVER);
 					
					StringBuffer sb = new StringBuffer();
					sb.append("Node - "+sitecrashid);
					sb.append("Sent - "+ sitecrashmsg);
					this.AddLogEntry(sb.toString(), Level.INFO);
					
 					sendSiteCrashMsg(sitecrashmsg, sitecrashid);
 				}				
 			}			
 
 			Thread.sleep(600);
 		}
 	}
 
 	public void sendClientOpMsg(ClientOpMsg msg, String destid) throws IOException
 	{		
 		this.AddLogEntry("Sent " + msg, Level.INFO);
 		
 		messageController.SendMessage(Common.CreateMessageWrapper(msg), Common.DirectMessageExchange, destid);		
 	}
 
 	public void sendSiteCrashMsg(SiteCrashMsg msg, String destid) throws IOException
	{	
 		messageController.SendMessage(Common.CreateMessageWrapper(msg), Common.DirectMessageExchange, destid);
 	}
 
 	public void run(){
 
 		System.out.println("\n Receiving..");
 
 		Timestamp startTime=new Timestamp(new Date().getTime());
 		Timestamp curtime;
 
 		while (true) 
 		{
 			MessageWrapper msgwrap;
 
 			try {
 				msgwrap = messageController.ReceiveMessage();
 				if (msgwrap != null ) {
 					
 					if(msgwrap.getmessageclass() == ClientOpMsg.class)
 					{
 						ClientOpMsg msg = (ClientOpMsg) msgwrap.getDeSerializedInnerMessage();
 						ProcessClientResponseData(msg);
 						break;
 					}
 				}
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			curtime=new Timestamp(new Date().getTime());
 			if(curtime.after(Common.getUpdatedTimestamp(startTime, Common.commitabort_timeout+2)))
 			{
 				System.out.println("Transaction aborted. Timed out. ");
				this.AddLogEntry("Transaction aborted. Timed out", Level.INFO);
				
 				break;
 			}
 		}		
 	}
 
 
 	public void ProcessClientResponseData(ClientOpMsg msg)
 	{
 		System.out.println(" Received " + msg);		
 		this.AddLogEntry("Received " + msg, Level.INFO);
 	}
 
 }
