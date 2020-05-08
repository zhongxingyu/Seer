 import java.util.*;
 
 public class RandomGrid{
 	
 	public static void main(String[] args){
 		if (args.length == 0){
 			System.out.println("Use command line to enter N. \nLeaving.");
 			return;
 		}
        	    	  
 		int n = 0; 
        	    	  
 		try{
 			n = (int)Integer.parseInt(args[0]);
 		}
 		catch(NumberFormatException e){
 			System.out.println("Invalid argument: " + args[0]);
 			return;
 		}
 		
 		RandomBag<Connection> bag = Generate(n);
 		System.out.println("Pairs: " + bag.Size());		
 		
 		for (Connection conn : bag)
 			System.out.println(conn);
 	}
 	
 	public static RandomBag<Connection> Generate(int n){
 		RandomBag<Connection> bag = new RandomBag<Connection>();
 		System.out.printf("Generating for n = %d... ", n);
 		
		
		for (int i = 0; i != n; i++){
			for (int j = 0; j != n; j++){
				if ((i + 1) != n)
					bag.Add(new Connection(j * n + i, j * n + i + 1));
				
				if ((j + 1) != n)
					bag.Add(new Connection(j * n + i, (j + 1) * n + i));				
 			}
 		}
 
 		return bag;		
 	}
 	
 	//nested
 	
 	private static class Connection{
 		int p, q;
 		
 		public Connection(int p, int q){
 			this.p = p;
 			this.q = q;
 		}
 		
 		@Override
 		public String toString(){
 			return String.format("pair: p=%d q=%d", p, q);
 		}		
 	}
 }
