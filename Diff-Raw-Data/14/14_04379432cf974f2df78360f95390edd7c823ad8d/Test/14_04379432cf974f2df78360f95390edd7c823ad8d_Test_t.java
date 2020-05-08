 package test;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 public abstract class Test {
 	
 	private String testName;
 	File dataFile;
 	PrintWriter dataStream;
 	int counter, n, i = 0;
 	boolean end = false;
 	
 	public Test(int n) throws FileNotFoundException {
 		counter = counter();
 		dataFile = new File(testName + "_" + counter + ".test");		
 		dataStream = new PrintWriter(dataFile);
 		this.n = n;
 	}
 	
 	public Test(String testName, int n) throws FileNotFoundException {
 		this.testName = testName;
 		counter = counter();
 		dataFile = new File(testName + "_" + counter + ".test");		
 		dataStream = new PrintWriter(dataFile);
 		this.n = n;
 	}
 	
 	private int counter() throws FileNotFoundException {
 		File counterSource = new File(testName + "_counter");
 		try {
 			counterSource.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		Scanner counterR = new Scanner(counterSource);
 		if(counterR.hasNextInt())	counter = counterR.nextInt();
 		else						counter = 0;
 		counterR.reset();
 		counterR.close();
 		
 		PrintWriter counterW = new PrintWriter(counterSource);
 		counterR.reset();
 		counterW.println(++counter);
 		counterW.flush();
 		counterW.close();
 		return counter;
 	}
 	
	protected Double target() {
 		System.err.println("Idiot.  I told you to override target().  --Joseph McGee");
 		return null;
 	}
	protected Double result() {
 		System.err.println("Idiot.  I told you to override result().  --Joseph McGee");
 		return null;
 	}
 	
 	public void main() {
		dataStream.println("target" + "\t" + "result");
 		while(!endTest()) {
 			doTestCall();
 		}
		dataStream.flush();
		dataStream.close();
 	}
 	
 	protected void doTestCall(){
 		System.err.println("You should probably override doTestCall().  --Joseph McGee");
 		doTest();
 	}
 	protected void doTest() {
 		if(i<n){
			dataStream.println("" + target() + '\t' + result());
 			i++;
 		}
 	}
 	protected boolean endTest(){
 		if(i>=n){
 			return end = true;
 		}
 		return end;
 	}
 
 }
