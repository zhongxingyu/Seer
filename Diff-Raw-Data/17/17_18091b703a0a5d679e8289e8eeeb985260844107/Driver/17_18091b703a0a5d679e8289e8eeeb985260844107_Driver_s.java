 /**
  * Class to run the dining philosophers
  * 
  * @author Harris Borawski
  *
  */
 import java.util.ArrayList;
 public class Driver {
 	
 	public static void main(String[] args){
 		int np = Integer.parseInt(args[0]);
 		int nt = Integer.parseInt(args[1]);
 		int tm = Integer.parseInt(args[2]);
 		int em = Integer.parseInt(args[3]);
 		
 		ArrayList<Philosopher> Phil = new ArrayList<Philosopher>();
 		
		Fork LFork = new Fork();
		Fork RFork = new Fork();
		
 		
 		for(int i = 0; i < np; i++){
			Philosopher p = new Philosopher(i,LFork, RFork,true,nt,tm,em);
 		}
 		
		Philosopher p = new Philosopher(1,new Fork(), new Fork(),true,2,(long)500, (long)500);
		p.run();
 	}
 }
