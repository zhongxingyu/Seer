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
 		
		Fork[] forks = new Fork[np-1];
		for(int f = 0; f < np; f++){
			forks[f] = new Fork();
		}
 		
 		for(int i = 0; i < np; i++){
			Philosopher p = new Philosopher(i,forks[i], forks[(np+i-1)%np],true,nt,tm,em);
			Phil.add(p);
 		}
 		
		for(Philosopher p : Phil){
			p.run();
		}
 	}
 }
