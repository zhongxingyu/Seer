 package daemon;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import constant.Application;
 
 public class BuildRunner implements Runnable{
 	public String branchName = "";
 	@Override
 	public void run() {
 		try {
 			Runtime runTime = Runtime.getRuntime();
 			String commands = new String();
 			commands = "/usr/local/share/run.sh";
 		
 			Process proc = runTime.exec(commands);
 			
 			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
 			System.out.println(br.read());
 			/*TailBuffer tailBuffer = new TailBuffer();
 			tailBuffer.setFILEPATH(Application._TAILFILE);
 			Thread thread = new Thread(tailBuffer);
 			thread.start();*/
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	public static void main ( String args[]){
 		BuildRunner runner = new BuildRunner();
 		Thread thread = new Thread(runner);
 		thread.start();
 	}
 }
