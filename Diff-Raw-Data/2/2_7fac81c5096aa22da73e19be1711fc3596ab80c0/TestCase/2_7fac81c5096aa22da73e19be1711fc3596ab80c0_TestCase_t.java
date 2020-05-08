 package org.blackcoffee;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecuteResultHandler;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.ExecuteException;
 import org.apache.commons.exec.ExecuteWatchdog;
 import org.apache.commons.exec.PumpStreamHandler;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.blackcoffee.command.Command;
 import org.blackcoffee.command.TcoffeeCommand;
 import org.blackcoffee.commons.utils.Duration;
 import org.blackcoffee.exception.BlackCoffeeException;
 import org.blackcoffee.exception.ExitFailed;
 import org.blackcoffee.utils.PathUtils;
 import org.blackcoffee.utils.QuoteStringTokenizer;
 import org.blackcoffee.utils.VarHolder;
 
 /**
  * Defines a test case i.e. a test rule, plus a list of assertion 
  *
  */
 public class TestCase { 
 	
 	public String label;
 	public Duration timeout = Duration.parse("60s");
 	public List<String> exports = new ArrayList<String>();
 	public Command command; 
 	public List<TestAssertion> assertions = new ArrayList<TestAssertion>();
 	public VarHolder variables = new VarHolder();
 	public List<String> input = null;
 	public List<String> output = null;
 	public List<String> tags = null;
 	
 	/** Condition to be sodisified to execute the test */
 	public TestCondition condition;
 	
 	/** The folder which contain the input data */
 	public File inputPath;		
 	
 	/** The path that will contains the result files */
 	public File sandboxPath;
 
 	/** The file from which the test has loaded */
 	public File testFile; 
 
 	/** The folder where the test is executed */
 	public File runPath;
 	
 	/** The exptected exit code */
 	public Integer exit = 0;
 
 	
 	public List<Command> before;
 	public List<Command> after;
 	public boolean disabled;
 	
 	TestResult result;
 	
 	String valgrindCmd;
 
 	
 	/** Progressive index number starting from 1 */
 	public int index;
 	
 	/** The line count where the test is defined in the file */
 	public int line;
 	
 	public TestCase( String test ) { 
 		if( test != null ) { 
 			this.command = test.startsWith("t_coffee") 
 		             ? new TcoffeeCommand(test.substring(8).trim())
 					 : new Command(test);
 		}
 		else { 
 			this.command = new Command(null);
 		}
 	}
 	
 	public void addAssertion( String cond, int line, VarHolder variables ) { 
 		
 		TestAssertion item = new TestAssertion();
 		item.line = line;
 		item.variables = new VarHolder(variables);
 	
 		int p = cond.indexOf("#");
 		if( p != -1 ) { 
 			item. message = cond.substring(p+1) .replaceAll("^\\s*", "");
 			item.declaration = cond.substring(0,p);
 		}
 		else { 
 			item.declaration = cond;
 		}
 		
 		assertions.add(item);
 	}
 	
 	public String toString() { 
 		return new StringBuilder() 
 			.append("TestCase[ \n") 
 			.append("  env: " ) .append(exports) . append(",\n")
 			.append("  test: " ) .append(command) .append(",\n")
 			.append("  valgrind: " ) .append(valgrindCmd) .append(",\n")
 			.append("  before: " ) .append(before) .append(",\n")
 			.append("  after: " ) .append(after) .append(",\n")
 			.append("  if: " ) .append(condition != null ? condition : "") .append(",\n")
 			.append("  assert: ") .append(assertions) .append(",\n")
 			.append("  variables: ") .append(variables) .append("\n")
 			.append("]") 
 			.toString();
 	}
 
 	/**
 	 * Compile the assertions predicates in this test case 
 	 */
 	public void compileAssertions() {
 		Class<?> lastResultType = null;
 		for( TestAssertion assertion : assertions ) { 
 			assertion.compile( lastResultType );
 			lastResultType  = assertion.predicate.lastResultType;
 		}
 	}
 
 	public void configure( File testFile, Config config) { 
 		this.testFile = testFile;
 		
 		this.variables .putAll( config.vars );
 		
 		this.sandboxPath = config.sandboxPath;
 		
 		if( config.valgrind ) { 
 			this.valgrindCmd = "valgrind --log-file=.valgrind.log"; 
 			if( StringUtils.isNotBlank(config.valgrindOptions) ) { 
 				this.valgrindCmd += " " + config.valgrindOptions;
 			}
 		}
 		
 		/*
 		 *  set the test input path: 
 		 *  if the attribute has been specified on the command line this has an higher priority,
 		 *  use this valie to define the test 'inputPath'
 		 */
 		if( config.inputPath != null ) { 
 			this.inputPath = config.inputPath;
 		}
 		
 		/*
 		 * otherwise the input file could has been defined in the configuration file 
 		 * if no, the folder that holds the file is expected to contain the input files as well
 		 */
 
 		if( this.inputPath == null ) { 
 			this.inputPath = testFile.getParentFile().getAbsoluteFile();
 		}
 		
 		
 		// 2. configure the 'test' command
 		command.configure(this);
 
 		// 3. configure the 'before' commands
 		if( before != null ) for( Command cmd : before ) { 
 			cmd.configure(this);
 		}
 
 		// 4. configure the after commands 
 		if( after != null ) for( Command cmd : after ) { 
 			cmd.configure(this);
 		}
 		
 	}
 	
 	
 	/**
 	 * Verify that all assertions are satisfied 
 	 * 
 	 * @param path directory that define the execution context for this test i.e. usually the directory 
 	 * that contains all the files required/produced by the command under tests 
 	 */
 	public void verify() {
 		
 		Object previousAssertResult=null;
 		for( TestAssertion assertion : assertions ) { 
 			
 			/* 
 			 * verify the assertion 
 			 */
 			Object value = assertion.verify(runPath, previousAssertResult);
 			
 			/* 
 			 * save the assertion result value in the context, 
 			 * so that can be reused if required 
 			 */
 			if( !assertion.predicate.root.isContinuation() ) { 
 				previousAssertResult = value;
 			}
 		}
 	}
 
 	/**
 	 * Invoke to execute the test 
 	 * 
 	 * @throws ExecuteException
 	 * @throws IOException
 	 * @throws TimeoutException
 	 */
 	public void run() throws ExecuteException, IOException, TimeoutException {
 
 		long start = System.currentTimeMillis();
 		try { 
 			/* execute the test command */
 			result.exitCode = runTest();
 
 			/* verify the returned exit code match the exptectd one */
 			if( exit != null && exit != result.exitCode ) { 
 				String msg = String.format("Test terminated with with exit code: %s", result.exitCode);
 				if( exit != 0 ) { 
 					msg += String.format("; was expected %s.", exit);
 				}
 				throw new ExitFailed(msg);
 			}
 		}
 		finally { 
 			result.elapsed = System.currentTimeMillis() - start;
 		}
 
 	}
 	
 	int runTest() throws ExecuteException, IOException, TimeoutException {
 
 		/*
 		 * define the srdout / stderr streams to save the program output 
 		 */
 		OutputStream stdout = new BufferedOutputStream ( new FileOutputStream(new File(runPath,".stdout")) );
 		OutputStream stderr = new BufferedOutputStream ( new FileOutputStream(new File(runPath,".stderr")) );
 		
 		try  {
 			DefaultExecutor executor = new DefaultExecutor();
 			executor.setWorkingDirectory(runPath);
 			executor.setStreamHandler(new PumpStreamHandler(stdout, stderr));
 			executor.setExitValue(this.exit);
 			
 			if( this.timeout != null ) { 
 				executor.setWatchdog(new ExecuteWatchdog(this.timeout.millis()));
 			}
 			
 			/* 
 			 * define the environment / script file 
 			 */
 			
 			PrintWriter script = new PrintWriter(new FileWriter( new File(runPath, ".run") ));
 			if( this.exports != null ) for( String key : this.exports ) { 
 
 				String val = variables .value(key);	
 				
 				script.append("export ") 
 					.append(key)
 					.append( "=") 
 					.append("\"") .append(val) .println("\"");
 		
 			}
 			
 			script.println();
 			
 			/* the before commands */
 			if( before != null ) for( Command cmd : before ){ 
 				script.println(cmd.toString());
 			}
 
 			/* prepemnding teh command with valgring options if requested */
 			String theCommand = command.toString();
 			if( StringUtils.isNotBlank(valgrindCmd) ) { 
 				theCommand = valgrindCmd.trim() + " " + theCommand;
 			}
 			
 			/* put out the command */
 			script.println( theCommand );
 
 			/* after command declaration */
 			if( after != null ) for( Command cmd : after ){ 
 				script.println(cmd.toString());
 			}
 	
 			IOUtils.closeQuietly(script);
 			
 			
 			CommandLine cmd = CommandLine.parse("bash .run");
 			int result = Integer.MAX_VALUE;
 			try { 
 				DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
 				executor.execute(cmd,resultHandler);		
 				try {
 					resultHandler.waitFor();
 				} catch (InterruptedException e) {
 					System.err.printf("Warning: InterruptedException on test running");
 				}
 				result = resultHandler.getExitValue();
 			}
 			catch( ExecuteException e ) { 
 				result = e.getExitValue();
 			}
 			finally { 
 				// save the exitcode 
 				FileUtils.writeStringToFile(new File(runPath, ".exitcode"), String.valueOf(result));
 			}
 			
 			// check if is terminated by a timeout 
 			if( executor.isFailure(result) && executor.getWatchdog().killedProcess() ) { 
 				throw new TimeoutException();
 			}
 
 			return result;
 		}
 		finally { 
 			IOUtils.closeQuietly(stdout);
 			IOUtils.closeQuietly(stderr);
 		}
 	}	
 	
 	public void addInputFile( String fileName ) { 
 		if( fileName == null ) return;
 		
 		if( input == null ) { 
 			input = new ArrayList<String>();
 		}
 
 		QuoteStringTokenizer values = new QuoteStringTokenizer(fileName, ' ', ',');
 		for( String str : values ) { 
 			if( !input.contains(str) ) { 
 				input.add(str);
 			}
 		}
 		
 	}
 	
 	public void addTag( String sTags ) { 
 		if( sTags == null ) { return; }
 		
 		if( this.tags == null ) { 
 			this.tags= new ArrayList<String>();
 		}
 
 		QuoteStringTokenizer values = new QuoteStringTokenizer(sTags, ' ', ',', ';');
 		for( String str : values ) { 
 			if( !this.tags.contains(str) ) { 
 				this.tags.add(str);
 			}
 		}
 	}
 	
 	public void addBeforeCommand(String cmdline) { 
 		if( before == null ) { 
 			before = new ArrayList<Command>();
 		}
 		
 		before.add( new Command(cmdline) );
 	}
 	
 	public void addAfterCommand( String cmdline ) { 
 		if( after == null ) { 
 			after = new ArrayList<Command>();
 		}
 
 		after.add( new Command(cmdline) );
 	}
 	
 	public TestResult result() {
 		
 		if( result == null ) { 
 			result = new TestResult();
 			result.test = this;
 		}
 		
 		return result;
 	}
 	
 	
 	/*
 	 * copy everything to the target directory where the test will run 
 	 */
 	public void prepareData() throws IOException {
 
 		result = result();
 
 		/* 
 		 * define the run path 
 		 */
 		this.runPath = getUniquePath();
 
 		/* 
 		 * add the run path to the variables
 		 */
 		variables .put("run.path", runPath.getAbsolutePath());
 		for( TestAssertion _assert : assertions ) { 
 			_assert.variables .put("run.path", runPath.getAbsolutePath()); 
 		}
 		
 		/*
		 * when the 'in' attributes is defined only the declared files are copied 
 		 */
 		PathUtils path = new PathUtils() .current(inputPath);
 	
 		if( input != null && input.size()>0) { 
 
 			for( String sItem : input ) { 
 				copyFile( 
 						path.absolute(sItem), 	// <-- convert to absolute path using the 'inputPath' as current directory
 						runPath);				// copy to the current path
 			}
 			return;
 		}
 		
 		/*
 		 * otherwise if is a file copy just that file 
 		 */
 		if( inputPath.isFile() ) { 
 			copyFile(inputPath, runPath);
 			return;
 		}
 		
 		/* 
 		 * otherwise it is a directory (i hope ..) copy all the content 
 		 */
 		File[] all = inputPath.listFiles();
 		if(all!=null) for( File item : all ) { 
 			if( item.getName().endsWith(".testcase") ) continue;
 			if( item.getName().endsWith(".testsuite") ) continue;
 
 			copyFile(item, runPath);
 		}
 		
 	}
 	
 	/*
 	 * copy a single file to the destination folder 
 	 */
 	void copyFile( File item, File targetPath ) throws IOException { 
 
 		if( !item.exists() ) { 
 			result.text.printf("~ Warning: missing input file '%s' \n", item);
 			return;
 		}
 		
 		String cmd = String.format("ln -s %s %s", item.getAbsolutePath(), item.getName());
 		try {
 			Runtime.getRuntime().exec(cmd, null, targetPath).waitFor();
 		} catch (InterruptedException e) {
 			System.err.println("Warning: Interrupted exception waiting for file creation");
 		}
 	}
 	
 
 	/**
 	 * Creare a unique path 
 	 * 
 	 * @return
 	 */
 	File getUniquePath() { 
 		
 		File path;
 		do { 
 			Double d = Math.random();
 			path = new File(this.sandboxPath, Integer.toHexString(d.hashCode()));
 			if( path.exists() ) { 
 				// try again ..
 				continue;
 			}
 			
 			if( !path.mkdirs() ) { 
 				throw new BlackCoffeeException("Unable to create path: %s", path);
 			}
 			
 			break;
 		}
 		while(true);
 		
 		return path;
 	}
 
 }
 
 
 
 
