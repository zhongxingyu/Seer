 package testfiles;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 
 import antlr.RecognitionException;
 
 import fire.ForrestFireException;
 import forrest.main.Forrest;
 
 /**
  * Class used for running all tests
  *
  */
 public class TestForrest {
 	
 	//Setting Debug to true will generate more information
 	private boolean debug = false;
 	//Class used to run the grammar Forrest
 	private Forrest forrest;
 	//String used for the location of the test files
 	private static String fileLocation = "src/testfiles/";
 
 	//TestFiles
 	private HashMap<String,String[]> fileToOutputs = new HashMap<String,String[]>();
 	
 	/**
 	 * Constructor for this class
 	 */
 	public TestForrest(){
 		
 	}
 	
 	/**
 	 * Method for initializing the tests
 	 */
 	public void init(){
 		this.fillFileToOutput();
 		this.forrest = new Forrest();
 	}
 	
 	/**
 	 * Method to run all tests
 	 */
 	public void runAllTests(){
 		int totalFailed = 0;
 		totalFailed += runParserTests();
 //		totalFailed += runCheckerTests();
 //		totalFailed += runEncoderTests();
 		System.out.println("All tests ended with " + totalFailed + " errors");
 	}
 	
 	/**
 	 * Method to run all ParserTests
 	 */
 	private int runParserTests(){
 		int failed = 0;
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		PrintStream ps = new PrintStream(baos);
 		for(String file : fileToOutputs.keySet()){
 			String expected = fileToOutputs.get(file)[0];
 			String returned = null;
 			try{
				this.forrest.runForrest(file, ps, true, false, false, false);
 				returned = baos.toString();
 			} catch(ForrestFireException e){
 				returned = e.getMessage();
 				
 			} catch(RecognitionException e){
 				returned = e.getMessage();
 			
 			} catch(Exception e){
 				returned = e.getMessage();
 //				System.exit(0);
 			}
 			
 			if(!returned.equals(expected)){
 				System.out.println("Test Failed: "+ file + " : \n" + returned + "\n" + expected);
 				failed++;
 			} else {
 				print("Test Succesful: " + file);
 			}
 			baos.reset();
 		}
 		ps.close();
 		try {
 			baos.close();
 		} catch (IOException e) {
 			System.out.println("Somehting wrong with closing ByteArrayOutputStream");
 		}
 		System.out.println("Finished tests with " + failed + " errors");
 		return failed;
 	}
 	
 	/**
 	 * Method to print output to the System.out, will only print if debug is set to true
 	 * @param tekst - String: de tekt om uit te printen
 	 */
 	private void print(String tekst){
 		if(debug){
 			System.out.println(tekst);
 		}
 	}
 	
 	/**
 	 * Method to make all tests
 	 */
 	private void fillFileToOutput(){
 		fileToOutputs.put(fileLocation+"DeclaratieTest1.forrest", new String[]
 				{"(PROGRAM (var lex bool) (var stijn char) (var RoNalD int) (= lex true))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"DeclaratieTest2a.forrest", new String[]
 				{"(PROGRAM stijn (var <missing IDENTIFIER> char) (= lex true))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"DeclaratieTest2b.forrest", new String[]
 				{"rule program_lines",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"DeclaratieTest3.forrest", new String[]
 				{"(PROGRAM <error: var stijn boolean> <error: var stijn character> <error: var stijn integer> (= lex true))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"BinaryOpTest1.forrest", new String[]
 				{"(PROGRAM (var stijn char) (var lex bool) (= lex (== lex lex)) (= lex (!= lex lex)) (= lex (|| (&& lex lex) (== lex lex))) (= stijn c) (= lex (== stijn c)))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"BinaryOpTest2.forrest", new String[]
 				{"(PROGRAM (var stijn char) (var lex bool) (= lex (|| stijn <unexpected: [@22,88:88=';',<38>,4:14], resync=;>)))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"BinaryOpTest3.forrest", new String[]
 				{"(PROGRAM (var stijn char) (var lex bool) (var lex2 bool) (= lex lex))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"BinaryOpTest4.forrest", new String[]
 				{"(PROGRAM (var stijn char) (var lex bool) (var lex2 bool) (= lex (|| (&& lex <unexpected: [@30,115:116='||',<24>,5:13], resync=||>) lex2)))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"CommentTest1.forrest", new String[]
 				{"(PROGRAM <mismatched token: [@15,94:94='<EOF>',<-1>,4:20], resync=var stijn char;\r\nvar lex bool;\r\n>)\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"CommentTest2.forrest", new String[]
 				{"(PROGRAM (var stijn char) (= lex true))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"Correct1.forrest", new String[]
 				{"(PROGRAM (var b1 bool) (var i1 int) (var c1 char) (var b2 bool) (var b3 bool) (= (= b1 b2) true) (= b3 (! b1)) (print (|| b1 (&& b2 b3))) (print (!= (== b1 b2) b3)))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"UnaryOpTest1.forrest", new String[]
 				{"(PROGRAM (var stijn char) (var lex bool) (var lex2 bool) (= lex lex) (! lex2))\r\n",
 				"output2",
 				"output3"
 				});
 		fileToOutputs.put(fileLocation+"IdentifierTest1.forrest", new String[]
 				{"(PROGRAM <mismatched token: [@22,61:61='<EOF>',<-1>,4:12], resync=var x bool;\r\nvar y1 bool;\r\nvar 1y bool;>)\r\n",
 				"output2",
 				"output3"
 				});
 	}
 	
 	/**
 	 * Main
 	 * @param args - not used
 	 */
 	public static void main(String[] args){
 		TestForrest tester = new TestForrest();
 		tester.init();
 		tester.runAllTests();
 		
 	}
 }
