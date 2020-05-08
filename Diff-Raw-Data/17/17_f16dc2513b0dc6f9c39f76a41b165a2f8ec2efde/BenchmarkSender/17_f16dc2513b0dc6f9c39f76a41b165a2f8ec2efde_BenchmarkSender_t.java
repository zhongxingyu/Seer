 package edu.berkeley.cs.cs162;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class BenchmarkSender extends AbstractChatClient {
 	private Map<Integer,Long> benchmarkTimes = new HashMap<Integer,Long>();
 	private Lock sendLock = new ReentrantLock();
 	
 	@Override
 	protected void send(String dest, int sqn, String msg){
 		if(!connected || !isLoggedIn)
 			return;
 		TransportObject toSend = new TransportObject(Command.send,dest,sqn,msg);
 		try {
 			isWaiting = true;
 			reply = Command.send;
 			benchmarkTimes.put(sqn,System.currentTimeMillis());
			sendLock.lock();
 			sent.writeObject(toSend);
			sendLock.unlock();
 			this.wait();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void sendRTT(double rtt){
 		TransportObject rttObject = new TransportObject(Command.rtt,rtt);
 		try {
			sendLock.lock();
 			sent.writeObject(rttObject);
			sendLock.unlock();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	protected void benchmark(TransportObject object){
 		if(!benchmarkTimes.containsKey(object.getSQN()))
 			return;
 				
 		long sentTime = benchmarkTimes.get(object.getSQN());
 		benchmarkTimes.remove(object.getSQN());
 		long receivedTime = System.currentTimeMillis();
 		double rrt = (receivedTime - sentTime);
 		sendRTT(rrt);
 	}
 	
 	public static void main(String[] args) throws UnknownHostException{
 		new BenchmarkSender();
 	}
 	
 }
