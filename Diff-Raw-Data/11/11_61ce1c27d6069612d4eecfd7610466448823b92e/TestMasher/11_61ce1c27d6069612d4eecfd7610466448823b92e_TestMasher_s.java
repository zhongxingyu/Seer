 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.Vector;
 
 /**
  * TODO Put here a description of what this class does.
  *
 * @author tim.
 *         Created Sep 6, 2012.
  */
 public class TestMasher extends Thread {
 	String[] arguments;
 	int timeout = 1000;
 	
 	@SuppressWarnings("deprecation")
 	public static void main(String [] args) {
 		int startGame = 0;
 		int games = 0;
 		String port = "5032";
 		String server = "dd2380.csc.kth.se"; 
 		
 		Vector<TestMasher> allThreads = new Vector<TestMasher>();
 				
 		int timeout = 1;
 		String opponent="";
 		try{
 			startGame = Integer.parseInt(args[0]);
 			games = Integer.parseInt(args[1]);
 			timeout = Integer.parseInt(args[2]);
 		}catch(Exception e){
 			System.out.println("invalid use of the tester!");
 			System.out.println("testrunner <number of games> <opponent>");
 		}
 		
 		System.out.println("--TESTMASHER--");
 		
		for(int game = startGame; game < games; game++){
 			TestMasher thread = new TestMasher(new String[] {server, port, ""+game }, timeout);
 			allThreads.add(thread);
 		}
 		
 		PrintStream originalOut = System.out; // To get it back later
 		int wins = 0;
 
		System.out.println("Running " + games + " boards, starting with " + startGame);		
 		for(int t = 0; t < allThreads.size(); t++) {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			PrintStream newOut = new PrintStream(baos);
 			System.setOut(newOut);
 			String tmp;
 			
 			long start = System.currentTimeMillis();
 			TestMasher current = allThreads.get(t);
 			current.start();
 			while(current.isAlive() && System.currentTimeMillis() - start < current.timeout*1000) {
 				try {
 					Thread.sleep(10);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 				}
 			}
 			if(current.isAlive()) {
 				try {
 					current.stop();
 				} catch(Throwable e) {
 					originalOut.println("aborted");
 				}
 			}
 			long stop = System.currentTimeMillis();
 			System.setOut(originalOut);
 			tmp = baos.toString();
 			System.out.print("Board #" + (startGame + t) + "\t");
 			
 			if(tmp.contains("CORRECT SOLUTION")){
 				System.out.println("Solved in " + (double)(stop-start)/1000 + " seconds");
 				wins++;
 			}
 			else
 				System.out.println("-");
 			
 			baos.reset();
 			System.setOut(newOut);
 		}
 		
 		System.out.flush();
 
 		System.setOut(originalOut); // So you can print again
		    System.out.println("\nPlaying against: " + opponent);
 		    System.out.println("Number of solved boards: " + wins + " of " + games + " boards");
 	}
 	
 	public TestMasher(String[] args, int timeout) {
 		arguments = args;
 		this.timeout = timeout;
 	}
 	
 	public void run() {
 		Client.main(arguments);
 	}
 
 }
