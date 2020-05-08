 package no.antares.kickstart;
 
 
 /**
  * mvn exec:java -Dexec.mainClass="no.antares.kickstart.ProcessControl"
 http://stackoverflow.com/questions/830641/is-it-possible-to-have-interprocess-communication-in-java
 http://publib.boulder.ibm.com/infocenter/iseries/v7r1m0/index.jsp?topic=%2Frzaha%2Fsockets.htm
 
 http://www.coderanch.com/t/328888/java/java/Killing-process-spawned-Runtime-exec
  * @author Tommy Skodje
  */
 public class ProcessControl {
 
 	/**	Start a program / process */
 	public static Runnable startProcess() throws Exception {
 		String execStr = "C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE";  
 		final Process proc = Runtime.getRuntime().exec(execStr);  
 		System.out.println("proc: " + proc);
 		return new Runnable() {
			@Override public void run() {
 				proc.destroy();  
 				System.out.println("destroyed");  
 			}
 		};
 	}
 
 	/**	Start and stop a program / process */
 	public static void main(String[] args) throws Exception { 
 		final Runnable killer	= startProcess();
 		Runtime.getRuntime().addShutdownHook( new Thread( killer ) );
 		Thread.sleep(10000);
 	}  
 
 }
