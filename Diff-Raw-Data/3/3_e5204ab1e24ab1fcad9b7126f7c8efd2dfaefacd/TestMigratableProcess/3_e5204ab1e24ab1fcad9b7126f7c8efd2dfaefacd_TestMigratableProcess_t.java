 package Classes;
 
 import java.io.*;
 
 public class TestMigratableProcess implements MigratableProcess{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1241500530790605595L;
 	private String inputFile;
 	private String outputFile = "out.txt";
 	private volatile boolean suspending = false;
 	private boolean finished = false;
 	
 	/* transactionalIO */
 	private TransactionalFileInputStream inStream;
 	private TransactionalFileOutputStream outStream;
 	
 	private int i = 0;
 	
 	public TestMigratableProcess() {
 		
 	}
 	
 	public TestMigratableProcess(String[] args) {
 		this.inputFile = args[0];
 		this.outputFile = args[1];
 		
 		inStream = new TransactionalFileInputStream(inputFile);
 		outStream = new TransactionalFileOutputStream(outputFile);
 	}
 	
 	public String toString() {
 		return "";
 	}
 	
 	public void run() {
 		suspending = false;
 		DataOutputStream out = null;
 	    out = new DataOutputStream(outStream);
 	    
 		while(!suspending && !finished) {
 			
 			if (i > 100) {
 				finished = true;
 				break;
 			}
 			
 			try {
 				out.writeBytes("" + ++i + "\n");
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			System.out.println(++i);
 			
 			try {
 				Thread.sleep(600);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
		outStream.setMigrated(true);
 		suspending = false;
 		
 	}
 	
 	public void suspend() {
 		suspending = true;
 		while (suspending && !finished);
 	}
 	
 	public boolean getFinished() {
 		return this.finished;
 	}
 
 }
