 package ch.bfh.univoteverifier.common;
 
import ch.bfh.univote.election.ElectionBoardServiceFault;
 import ch.bfh.univoteverifier.utils.ElectionProxy;
 
 
 
 
 
 
 
 public class MainTemp {
 
 	/**
 	 * @param args
 	 * @throws InterruptedException 
 	 */
	public static void main(String[] args) throws InterruptedException, ElectionBoardServiceFault {
 		// TODO Auto-generated method stub
 		//System.out.println(Config.p.toString());
 		//System.out.println(Config.q.toString());
 		//System.out.println(Config.g.toString());
 	
		ElectionProxy ep = new ElectionProxy("vsbfh-2013");
 		ep.getElectionData();
 
		ep.getElectionData();
 		
 		
 	}
 
 }
