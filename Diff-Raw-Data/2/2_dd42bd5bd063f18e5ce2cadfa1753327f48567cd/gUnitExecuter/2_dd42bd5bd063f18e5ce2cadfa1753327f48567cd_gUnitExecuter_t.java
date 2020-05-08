 /*
  [The "BSD licence"]
  Copyright (c) 2007 Leon, Jen-Yuan Su
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:
  1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
  3. The name of the author may not be used to endorse or promote products
     derived from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package org.antlr.gunit;
 
 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 
 import java.io.*;
 import java.lang.reflect.*;
 
 public class gUnitExecuter {
 	public Interp interpreter;
 	
 	/** If error during execution, store stderr here */
 	protected String stderr;
 	protected String stdout;
 
 	protected boolean invalidInput;		// valid input if current index of tokens = size of tokens - 1
 
 	private int numOfTest;
 
 	private int numOfSuccess;
 
 	private int numOfFailure;
 
 	private String title;
 
 	private int numOfInvalidInput;
 
 	private StringBuffer bufInvalid;
 
 	private StringBuffer bufResult;
 
 	private String parserName;
 
 	private String lexerName;
 	
 	public gUnitExecuter(Interp interpreter) {
 		this.interpreter = interpreter;
 		numOfTest = 0;
 		numOfSuccess = 0;
 		numOfFailure = 0;
 		numOfInvalidInput = 0;
 		bufInvalid = new StringBuffer();
 		bufResult = new StringBuffer();
 	}
 	
 	public void execTest() throws IOException{
 		try {
 			/** Set up appropriate path for parser/lexer if using package */
 			if (interpreter.header!=null ) {
 				parserName = interpreter.header+"."+interpreter.grammarName+"Parser";
 				lexerName = interpreter.header+"."+interpreter.grammarName+"Lexer";
 			}
 			else {
 				parserName = interpreter.grammarName+"Parser";
 				lexerName = interpreter.grammarName+"Lexer";
 			}
 			
 			/*** Start Unit/Functional Testing ***/
 			if ( interpreter.treeGrammarName!=null ) {	// Execute unit test of for tree grammar
 				title = "executing testsuite for tree grammar:"+interpreter.treeGrammarName+" walks "+parserName;
 				executeTreeTests();
 			}
 			else {	// Execute unit test of for grammar
 				title = "executing testsuite for grammar:"+interpreter.grammarName;
 				executeGrammarTests();
 			}	// End of exection of unit testing
 
 			interpreter.unitTestResult.append("--------------------------------------------------------------------------------\n");
 			interpreter.unitTestResult.append(title+" with "+numOfTest+" tests\n");
 			interpreter.unitTestResult.append("--------------------------------------------------------------------------------\n");
 			if( numOfFailure>0 ) {
 				interpreter.unitTestResult.append(numOfFailure+" failures found:\n");
 				interpreter.unitTestResult.append(bufResult.toString()+"\n");
 			}
 			if( numOfInvalidInput>0 ) {
 				interpreter.unitTestResult.append(numOfInvalidInput+" invalid inputs found:\n");
 				interpreter.unitTestResult.append(bufInvalid.toString()+"\n");
 			}
 			interpreter.unitTestResult.append("Tests run: "+numOfTest+", "+"Failures: "+numOfFailure+"\n\n");
 		}
 		catch (Exception e) {
             e.printStackTrace();
             System.exit(1);
         }
 	}
 	
 	private void reportTestHeader(StringBuffer buffer, String rule, String treeRule) {
 		buffer.append("test" + numOfTest + " (");
 		if (treeRule != null)
 			buffer.append(treeRule+" walks ");
 		buffer.append(rule + ")" + " - " + "\n");
 	}
 
 	private void executeGrammarTests() throws Exception {
 		for ( gUnitTestSuite ts: interpreter.ruleTestSuites ) {
 			String rule = ts.rule;
 			String treeRule = null;
 			for ( gUnitTestInput input: ts.testSuites.keySet() ) {	// each rule may contain multiple tests
 				numOfTest++;
 				// Run parser, and get the return value or stdout or stderr if there is
 				Object result = runParser(parserName, lexerName, ts.rule, input);
 				if ( invalidInput==true ) {
 					numOfInvalidInput++;
 					reportTestHeader(bufInvalid, rule, treeRule);
 					bufInvalid.append("invalid input: "+input.testInput+"\n\n");
 				}
 				if ( ts.testSuites.get(input).getType()==27 ) {	// expected Token: OK
 					if ( this.stderr==null ) {
 						numOfSuccess++;
 					}
 					else {
 						numOfFailure++;
 						reportTestHeader(bufResult, rule, treeRule);
 						bufResult.append("expected: OK"+"\n");
 						bufResult.append("actual: FAIL"+"\n\n");
 					}
 				}
 				else if ( ts.testSuites.get(input).getType()==28 ) {	// expected Token: FAIL
 					if ( this.stderr!=null ) {
 						numOfSuccess++;
 					}
 					else {
 						numOfFailure++;
 						reportTestHeader(bufResult, rule, treeRule);
 						bufResult.append("expected: FAIL"+"\n");
 						bufResult.append("actual: OK"+"\n\n");
 					}
 				}
 				else if ( result==null ) {	// prevent comparing null return
 					numOfFailure++;
 					reportTestHeader(bufResult, rule, treeRule);
 					bufResult.append("expected: "+ts.testSuites.get(input).getText()+"\n");
 					bufResult.append("actual: null\n\n");
 				}
 				else if ( ts.testSuites.get(input).getType()==7 ) {	// expected Token: RETURN
 					/** Interpreter only compares the return value as String */
 					String stringResult = String.valueOf(result);
 					String expect = ts.testSuites.get(input).getText();
 					if ( expect.charAt(0)=='"' && expect.charAt(expect.length()-1)=='"' ) {
 						expect = expect.substring(1, expect.length()-1);
 					}
 					if( stringResult.equals(expect) ) {
 						numOfSuccess++;
 					}
 					else {
 						numOfFailure++;
 						reportTestHeader(bufResult, rule, treeRule);
 						bufResult.append("expected: "+expect+"\n");
 						bufResult.append("actual: "+result+"\n\n");
 					}
 				}
 				else if ( ts.testSuites.get(input).getType()==6 ) {	// expected Token: ACTION
 					numOfFailure++;
 					reportTestHeader(bufResult, rule, treeRule);
 					bufResult.append("\t"+"{ACTION} is not supported in the interpreter yet...\n\n");
 				}
 				else {
 					if( result.equals(ts.testSuites.get(input).getText()) ) {
 						numOfSuccess++;
 					}
 					else {
 						numOfFailure++;
 						reportTestHeader(bufResult, rule, treeRule);
 						bufResult.append("expected: "+ts.testSuites.get(input).getText()+"\n");
 						bufResult.append("actual: "+result+"\n\n");
 					}
 				}
 			}
 		}
 	}
 
 	private void executeTreeTests() throws Exception {
 		for ( gUnitTestSuite ts: interpreter.ruleTestSuites ) {
 			String rule = ts.rule;
 			String treeRule = ts.treeRule;
 			for ( gUnitTestInput input: ts.testSuites.keySet() ) {	// each rule may contain multiple tests
 				numOfTest++;
 				// Run tree parser, and get the return value or stdout or stderr if there is
 				Object result = runTreeParser(parserName, lexerName, ts.rule, ts.treeRule, input);
 				if ( invalidInput==true ) {
 					numOfInvalidInput++;
 					reportTestHeader(bufInvalid, rule, treeRule);
 					bufInvalid.append("invalid input: "+input.testInput+"\n\n");
 				}
 				if ( ts.testSuites.get(input).getType()==27 ) {	// expected Token: OK
 					if ( this.stderr==null ) {
 						numOfSuccess++;
 					}
 					else {
 						numOfFailure++;
 						reportTestHeader(bufResult, rule, treeRule);
 						bufResult.append("expected: OK"+"\n");
 						bufResult.append("actual: FAIL"+"\n\n");
 					}
 				}
 				else if ( ts.testSuites.get(input).getType()==28 ) {	// expected Token: FAIL
 					if ( this.stderr!=null ) {
 						numOfSuccess++;
 					}
 					else {
 						numOfFailure++;
 						reportTestHeader(bufResult, rule, treeRule);
 						bufResult.append("expected: FAIL"+"\n");
 						bufResult.append("actual: OK"+"\n\n");
 					}
 				}
 				else if ( result==null ) {	// prevent comparing null return
 					numOfFailure++;
 					reportTestHeader(bufResult, rule, treeRule);
 					bufResult.append("expected: "+ts.testSuites.get(input).getText()+"\n");
 					bufResult.append("actual: null\n\n");
 				}
 				else if ( ts.testSuites.get(input).getType()==7 ) {	// expected Token: RETVAL
 					/** Interpreter only compares the return value as String */
 					String stringResult = String.valueOf(result);
 					String expect = ts.testSuites.get(input).getText();
 					if ( expect.charAt(0)=='"' && expect.charAt(expect.length()-1)=='"' ) {
						expect = expect.substring(1, expect.length()-1);
 					}
 					if( stringResult.equals(expect) ) {
 						numOfSuccess++;
 					}
 					else {
 						numOfFailure++;
 						reportTestHeader(bufResult, rule, treeRule);
 						bufResult.append("expected: "+expect+"\n");
 						bufResult.append("actual: "+result+"\n\n");
 					}
 				}
 				else if ( ts.testSuites.get(input).getType()==6 ) {	// expected Token: ACTION
 					numOfFailure++;
 					reportTestHeader(bufResult, rule, treeRule);
 					bufResult.append("\t"+"{ACTION} is not supported in the interpreter yet...\n\n");
 				}
 				else {
 					if( result.equals(ts.testSuites.get(input).getText()) ) {
 						numOfSuccess++;
 					}
 					else {
 						numOfFailure++;
 						reportTestHeader(bufResult, rule, treeRule);
 						bufResult.append("expected: "+ts.testSuites.get(input).getText()+"\n");
 						bufResult.append("actual: "+result+"\n\n");
 					}
 				}
 			}
 		}
 	}
 	
 	protected Object runParser(String parserName, String lexerName, String testRuleName, gUnitTestInput testInput) throws Exception {
 		CharStream input;
 		/** Set up ANTLR input stream based on input source, file or String */
 		if ( testInput.inputIsFile==true ) {
 			input = new ANTLRFileStream(testInput.testInput);
 		}
 		else {
 			input = new ANTLRStringStream(testInput.testInput);
 		}
 		Class lexer = null;
 		Class parser = null;
         try {
             /** Use Reflection to create instances of lexer and parser */
         	lexer = Class.forName(lexerName);
             Class[] lexArgTypes = new Class[]{CharStream.class};				// assign type to lexer's args
             Constructor lexConstructor = lexer.getConstructor(lexArgTypes);        
             Object[] lexArgs = new Object[]{input};								// assign value to lexer's args   
             Object lexObj = lexConstructor.newInstance(lexArgs);				// makes new instance of lexer    
             
             CommonTokenStream tokens = new CommonTokenStream((Lexer) lexObj);
             
             parser = Class.forName(parserName);
             Class[] parArgTypes = new Class[]{TokenStream.class};				// assign type to parser's args
             Constructor parConstructor = parser.getConstructor(parArgTypes);
             Object[] parArgs = new Object[]{tokens};							// assign value to parser's args  
             Object parObj = parConstructor.newInstance(parArgs);				// makes new instance of parser      
             
             Method ruleName = parser.getMethod(testRuleName);
             
             /** Start of I/O Redirecting */
             PipedInputStream pipedIn = new PipedInputStream();
             PipedOutputStream pipedOut = new PipedOutputStream();
             PipedInputStream pipedErrIn = new PipedInputStream();
             PipedOutputStream pipedErrOut = new PipedOutputStream();
             try {
             	pipedOut.connect(pipedIn);
             	pipedErrOut.connect(pipedErrIn);
             }
             catch(IOException e) {
             	System.err.println("connection failed...");
             	System.exit(1);
             }
             PrintStream console = System.out;
             PrintStream consoleErr = System.err;
             PrintStream ps = new PrintStream(pipedOut);
             PrintStream ps2 = new PrintStream(pipedErrOut);
             System.setOut(ps);
             System.setErr(ps2);
             /** End of redirecting */
 
             /** Invoke grammar rule, and store if there is a return value */
             Object ruleReturn = ruleName.invoke(parObj);
             String astString = null;
             /** If rule has return value, determine if it's an AST */
             if ( ruleReturn!=null ) {
             	/** If return object is instanceof AST, get the toStringTree */
                 if ( ruleReturn.toString().indexOf(testRuleName+"_return")>0 ) {
                 	try {	// NullPointerException may happen here...
                 		Class _return = Class.forName(parserName+"$"+testRuleName+"_return");
                 		Method[] methods = _return.getDeclaredMethods();
                 		for(Method method : methods) {
 			                if ( method.getName().equals("getTree") ) {
 			                	Method returnName = _return.getMethod("getTree");
 		                    	CommonTree tree = (CommonTree) returnName.invoke(ruleReturn);
 		                    	astString = tree.toStringTree();
 			                }
 			            }
                 	}
                 	catch(Exception e) {
                 		System.err.println(e);
                 	}
                 }
             }
             
             /** Invalid input */
             if ( tokens.index()!=tokens.size() ) {
             	this.invalidInput = true;
             }
             else {
             	this.invalidInput = false;
             }
             
             StreamVacuum stdoutVacuum = new StreamVacuum(pipedIn);
 			StreamVacuum stderrVacuum = new StreamVacuum(pipedErrIn);
 			ps.close();
 			ps2.close();
 			System.setOut(console);			// Reset standard output
 			System.setErr(consoleErr);		// Reset standard err out
 			this.stdout = null;
 			this.stderr = null;
 			stdoutVacuum.start();
 			stderrVacuum.start();			
 			stdoutVacuum.join();
 			stderrVacuum.join();
 			if ( stderrVacuum.toString().length()>0 ) {
 				this.stderr = stderrVacuum.toString();
 				return this.stderr;
 			}
 			if ( stdoutVacuum.toString().length()>0 ) {
 				this.stdout = stdoutVacuum.toString();
 			}
 			if ( astString!=null ) {	// Return toStringTree of AST
 				return astString;
 			}
 			if ( ruleReturn!=null ) {
 				return ruleReturn;
 			}
 			if ( stderrVacuum.toString().length()==0 && stdoutVacuum.toString().length()==0 ) {
 				return null;
 			}
         } catch (ClassNotFoundException e) {
             e.printStackTrace(); System.exit(1);
         } catch (SecurityException e) {
             e.printStackTrace(); System.exit(1);
         } catch (NoSuchMethodException e) {
             e.printStackTrace(); System.exit(1);
         } catch (IllegalArgumentException e) {
             e.printStackTrace(); System.exit(1);
         } catch (InstantiationException e) {
             e.printStackTrace(); System.exit(1);
         } catch (IllegalAccessException e) {
             e.printStackTrace(); System.exit(1);
         } catch (InvocationTargetException e) {
             e.printStackTrace(); System.exit(1);
         } catch (InterruptedException e) {
 			e.printStackTrace(); System.exit(1);
 		}
         return stdout;
 	}
 	
 	protected Object runTreeParser(String parserName, String lexerName, String testRuleName, String testTreeRuleName, gUnitTestInput testInput) throws Exception {
 		CharStream input;
 		String treeParserPath;
 		/** Set up ANTLR input stream based on input source, file or String */
 		if ( testInput.inputIsFile==true ) {
 			input = new ANTLRFileStream(testInput.testInput);
 		}
 		else {
 			input = new ANTLRStringStream(testInput.testInput);
 		}
 		/** Set up appropriate path for tree parser if using package */
 		if ( interpreter.header!=null ) {
 			treeParserPath = interpreter.header+"."+interpreter.treeGrammarName;
 		}
 		else {
 			treeParserPath = interpreter.treeGrammarName;
 		}
 		Class lexer = null;
 		Class parser = null;
 		Class treeParser = null;
         try {
             /** Use Reflection to create instances of lexer and parser */
         	lexer = Class.forName(lexerName);
             Class[] lexArgTypes = new Class[]{CharStream.class};				// assign type to lexer's args
             Constructor lexConstructor = lexer.getConstructor(lexArgTypes);        
             Object[] lexArgs = new Object[]{input};								// assign value to lexer's args   
             Object lexObj = lexConstructor.newInstance(lexArgs);				// makes new instance of lexer    
             
             CommonTokenStream tokens = new CommonTokenStream((Lexer) lexObj);
             
             parser = Class.forName(parserName);
             Class[] parArgTypes = new Class[]{TokenStream.class};				// assign type to parser's args
             Constructor parConstructor = parser.getConstructor(parArgTypes);
             Object[] parArgs = new Object[]{tokens};							// assign value to parser's args  
             Object parObj = parConstructor.newInstance(parArgs);				// makes new instance of parser      
             
             Method ruleName = parser.getMethod(testRuleName);
 
             /** Start of I/O Redirecting */
             PipedInputStream pipedIn = new PipedInputStream();
             PipedOutputStream pipedOut = new PipedOutputStream();
             PipedInputStream pipedErrIn = new PipedInputStream();
             PipedOutputStream pipedErrOut = new PipedOutputStream();
             try {
             	pipedOut.connect(pipedIn);
             	pipedErrOut.connect(pipedErrIn);
             }
             catch(IOException e) {
             	System.err.println("connection failed...");
             	System.exit(1);
             }
             PrintStream console = System.out;
             PrintStream consoleErr = System.err;
             PrintStream ps = new PrintStream(pipedOut);
             PrintStream ps2 = new PrintStream(pipedErrOut);
             System.setOut(ps);
             System.setErr(ps2);
             /** End of redirecting */
 
             /** Invoke grammar rule, and get the return value */
             Object ruleReturn = ruleName.invoke(parObj);
             
             Class _return = Class.forName(parserName+"$"+testRuleName+"_return");            	
         	Method returnName = _return.getMethod("getTree");
         	CommonTree tree = (CommonTree) returnName.invoke(ruleReturn);
 
         	// Walk resulting tree; create tree nodes stream first
         	CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
         	// AST nodes have payload that point into token stream
         	nodes.setTokenStream(tokens);
         	// Create a tree walker attached to the nodes stream
         	treeParser = Class.forName(treeParserPath);
             Class[] treeParArgTypes = new Class[]{TreeNodeStream.class};		// assign type to tree parser's args
             Constructor treeParConstructor = treeParser.getConstructor(treeParArgTypes);
             Object[] treeParArgs = new Object[]{nodes};							// assign value to tree parser's args  
             Object treeParObj = treeParConstructor.newInstance(treeParArgs);	// makes new instance of tree parser      
         	// Invoke the tree rule, and store the return value if there is
             Method treeRuleName = treeParser.getMethod(testTreeRuleName);
             Object treeRuleReturn = treeRuleName.invoke(treeParObj);
 
             String astString = null;
             /** If tree rule has return value, determine if it's an AST */
             if ( treeRuleReturn!=null ) {
             	/** If return object is instanceof AST, get the toStringTree */
                 if ( treeRuleReturn.toString().indexOf(testTreeRuleName+"_return")>0 ) {
                 	try {	// NullPointerException may happen here...
                 		Class _treeReturn = Class.forName(treeParserPath+"$"+testTreeRuleName+"_return");
                 		Method[] methods = _treeReturn.getDeclaredMethods();
 			            for(Method method : methods) {
 			                if ( method.getName().equals("getTree") ) {
 			                	Method treeReturnName = _treeReturn.getMethod("getTree");
 		                    	CommonTree returnTree = (CommonTree) treeReturnName.invoke(treeRuleReturn);
 		                        astString = returnTree.toStringTree();
 			                }
 			            }
                 	}
                 	catch(Exception e) {
                 		System.err.println(e);
                 	}
                 }
             }
           
             /** Invalid input */
             if ( tokens.index()!=tokens.size() ) {
             	this.invalidInput = true;
             }
             else {
             	this.invalidInput = false;
             }
             
             StreamVacuum stdoutVacuum = new StreamVacuum(pipedIn);
 			StreamVacuum stderrVacuum = new StreamVacuum(pipedErrIn);
 			ps.close();
 			ps2.close();
 			System.setOut(console);			// Reset standard output
 			System.setErr(consoleErr);		// Reset standard err out
 			this.stdout = null;
 			this.stderr = null;
 			stdoutVacuum.start();
 			stderrVacuum.start();			
 			stdoutVacuum.join();
 			stderrVacuum.join();
 			if ( stderrVacuum.toString().length()>0 ) {
 				this.stderr = stderrVacuum.toString();
 				return this.stderr;
 			}
 			if ( stdoutVacuum.toString().length()>0 ) {
 				this.stdout = stdoutVacuum.toString();
 			}
 			if ( astString!=null ) {	// Return toStringTree of AST
 				return astString;
 			}
 			if ( treeRuleReturn!=null ) {
 				return treeRuleReturn;
 			}
 			if ( stderrVacuum.toString().length()==0 && stdoutVacuum.toString().length()==0 ) {
 				return null;
 			}
         } catch (ClassNotFoundException e) {
             e.printStackTrace(); System.exit(1);
         } catch (SecurityException e) {
             e.printStackTrace(); System.exit(1);
         } catch (NoSuchMethodException e) {
             e.printStackTrace(); System.exit(1);
         } catch (IllegalArgumentException e) {
             e.printStackTrace(); System.exit(1);
         } catch (InstantiationException e) {
             e.printStackTrace(); System.exit(1);
         } catch (IllegalAccessException e) {
             e.printStackTrace(); System.exit(1);
         } catch (InvocationTargetException e) {
             e.printStackTrace(); System.exit(1);
         } catch (InterruptedException e) {
 			e.printStackTrace(); System.exit(1);
 		}
         return stdout;
 	}
 
 	public static class StreamVacuum implements Runnable {
 		StringBuffer buf = new StringBuffer();
 		BufferedReader in;
 		Thread sucker;
 		public StreamVacuum(InputStream in) {
 			this.in = new BufferedReader( new InputStreamReader(in) );
 		}
 		public void start() {
 			sucker = new Thread(this);
 			sucker.start();
 		}
 		public void run() {
 			try {
 				String line = in.readLine();
 				while (line!=null) {
 					buf.append(line);
 					buf.append('\n');
 					line = in.readLine();
 				}
 			}
 			catch (IOException ioe) {
 				System.err.println("can't read output from standard (error) output");
 			}
 		}
 		/** wait for the thread to finish */
 		public void join() throws InterruptedException {
 			sucker.join();
 		}
 		public String toString() {
 			return buf.toString();
 		}
 	}
 }
