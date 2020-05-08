 package com.nick.Paxos.Network.Leader;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.Vector;
 
 import com.nick.Paxos.Command;
 import com.nick.Paxos.Paxos;
 import com.nick.Paxos.UpdateLog;
 import com.nick.Paxos.Messages.CatchUpRequestMessage;
 import com.nick.Paxos.Network.SerializationUtil;
 
 public class LeaderListener extends Thread { 
 	
 	private DatagramSocket listenSocket;
 	private DatagramPacket packet;
 	private byte[] buf = new byte[512];
 	public boolean running = false;
 	
 	public LeaderListener(int port) {
 		try {
 			listenSocket = new DatagramSocket(port);
 			running = true;
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void run() {
 		while(running) {
 			packet = new DatagramPacket(buf,buf.length);
 			try {
 				listenSocket.receive(packet);
 				Object obj = SerializationUtil.deSerialize(buf);
 				if(obj instanceof CatchUpRequestMessage){
					System.out.println("New catch up request.");
 					CatchUpRequestMessage CUR = (CatchUpRequestMessage) obj;
 					Vector<Integer> seqNumbers = new Vector<Integer>();
 					HashMap<Integer,Command> commands = new HashMap<Integer,Command>();
 					
 					if(CUR.getLatestRound() != 0){
 						for(int i=Paxos.node.data.SeqNumbersProcessed.indexOf(CUR.getLatestRound()); i<Paxos.node.data.SeqNumbersProcessed.size(); i++){
 							seqNumbers.add(Paxos.node.data.SeqNumbersProcessed.elementAt(i));
 							commands.put(Paxos.node.data.SeqNumbersProcessed.elementAt(i), Paxos.node.data.commandsProcessed.get(Paxos.node.data.SeqNumbersProcessed.elementAt(i)));
 						}
 					} else {
 						for(int i=0; i<Paxos.node.data.SeqNumbersProcessed.size(); i++){
 							seqNumbers.add(Paxos.node.data.SeqNumbersProcessed.elementAt(i));
 							commands.put(Paxos.node.data.SeqNumbersProcessed.elementAt(i), Paxos.node.data.commandsProcessed.get(Paxos.node.data.SeqNumbersProcessed.elementAt(i)));
 						}
 					}
 					
 					UpdateLog log = new UpdateLog(seqNumbers,commands);
					System.out.println(seqNumbers.size());
 					byte buf[] = SerializationUtil.serialize(log);
 					DatagramPacket dp = new DatagramPacket(buf, buf.length, packet.getAddress(),1237);
 					DatagramSocket ds = new DatagramSocket();
 					ds.send(dp);
 					
 				} else if(obj instanceof Command) {
 					System.out.println("Received new command");
 					Command cmd = (Command) obj;
 					if(isReadOperation(cmd)) {
 						Paxos.node.leaderProc.resultReply(Paxos.node.data.data.get(cmd.getVariable()), cmd.getFromAddress());
 					} else {
 					Paxos.node.leaderProc.enqueueCommand(cmd);
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		
 	}
 	
 	private boolean isReadOperation(Command cmd) {
 		return cmd.getOperation().equalsIgnoreCase("READ");
 	}
 }
