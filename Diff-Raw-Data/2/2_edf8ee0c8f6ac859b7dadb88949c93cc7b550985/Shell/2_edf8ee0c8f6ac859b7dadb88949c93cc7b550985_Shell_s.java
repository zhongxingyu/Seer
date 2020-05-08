 package shell;
 
 import gui.Console;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import job.Job;
 import os.Init;
 import os.PermPipe;
 import os.Pipe;
 import shell.SyntaxAnalyzer.ProcessAnalysis;
 
 /**
  * shell/Shell.java <br>
  * <br>
  * 
  * Process commands, create processes and connect them with pipes.
  * 
  * 
  * @author Lukáš Hain
  * @version 0.1
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška radekp25@students.zcu.cz
  * 
  */
 public class Shell extends Job {
 
 	private String currentDirectory;
 
 	private Integer activeJopPID = null;
 	private Integer tmpPID;
 	boolean isAsync;
 
 	private Console console;
 
 	// private PermPipe jobIn;
 	/**
 	 * StdOut pipe for jobs.
 	 */
 	private PermPipe jobOut;
 	/**
 	 * StdErr pipe for jobs.
 	 */
 	private PermPipe jobErr;
 
 	private Thread TstdIn;
 	private Thread TstdOut;
 	private Thread TstdErr;
 
 	/** Map of proccesses: to know which one is where */
 
 	private HashSet<Integer> runningProcesses;
 
 	private boolean imLast = false;
 	private boolean imFromFile = false;
 	
 	private Integer waitingPID = null;
 	
 	public void setWaitingPID(Integer PID) {
 		this.waitingPID = PID;
 	}
 	
 	StringBuilder str;
 	
 	public String getCommandString(){
 		return this.str.toString();
 	}
 	
 	public void resetCommandString(){
 		this.str.replace(0, this.str.length(), "");
 	}
 	
 
 	/**
 	 * Classic Constructor.
 	 * 
 	 * @param tmpPID
 	 * @param stdERR
 	 */
 	public Shell(Integer tmpPID, Pipe stdERR) {
 		super(tmpPID, stdERR);
 		//this.currentDirectory = new File("").getAbsolutePath();
 		this.currentDirectory = Init.home;
 		// Console c = new Console(this);
 
 		this.jobOut = new PermPipe(null, null);
 		this.jobErr = new PermPipe(null, null);
 		this.runningProcesses = new HashSet<Integer>();
 
 		this.TstdErr = new Thread() {
 
 			@SuppressWarnings("synthetic-access")
 			@Override
 			public void run() {
 				super.run();
 				Shell.this.stdErrThread();
 			}
 		};
 
 		this.TstdIn = new Thread() {
 
 			@SuppressWarnings("synthetic-access")
 			@Override
 			public void run() {
 				super.run();
 				Shell.this.stdInThread();
 			}
 		};
 
 		this.TstdOut = new Thread() {
 
 			@SuppressWarnings("synthetic-access")
 			@Override
 			public void run() {
 				super.run();
 				Shell.this.stdOutThread();
 			}
 		};
 
 	}
 
 	public String getCurrentDirectory() {
 		return this.currentDirectory;
 	}
 
 	public void setCurrentDirectory(String currentDirectory) {
 		System.out.println("Current directory has been changed to: " + currentDirectory);
 		this.currentDirectory = currentDirectory;
 	}
 
 	/**
 	 * Set the shell as last. Shell will be closing console.
 	 */
 	public void setLast(){
 		this.imLast = true;
 	}
 
 	/**
 	 * Set the shell as "commands from file". Shell will not call unlockConsole()
 	 */
 	public void setFromFile(){
 		this.imFromFile = true;
 	}
 
 	/*
 	 * public Shell() { this.currentDirectory = new File("").getAbsolutePath();
 	 * new Console(this); }
 	 */
 	// cat param1 >>out.txt param2 –arg1 | sort > err param1 param2 < input
 	// cat "viceslovny parametr" >>out.txt param2 –arg1 | sort > err param1
 	// param2 < input
 
 	/**
 	 * Uses SyntaxAnalyzer method to parse command and then send it for
 	 * processes creation.
 	 * 
 	 * @param command
 	 *            command to be process
 	 * @throws ClassNotFoundException
 	 * @throws InterruptedException
 	 */
 	public void doYourJob(String command) throws ClassNotFoundException, InterruptedException {
 		SyntaxAnalyzer syn = new SyntaxAnalyzer();
 		CommandAnalysis ca = syn.processCommand(command, this.currentDirectory);
 
 		if(ca != null && ca.getProcessAnalysis() != null && ca.getProcessAnalysis().length > 0){
 			runProcessesAndCreatePipes(ca);
 		} else if(ca != null && ca.getErrorMessage() != null && ca.getErrorMessage().length() > 0) {
 			this.isAsync = true; //little trick to continue
 			pushError(ca.getErrorMessage());
 		} else {
 			this.isAsync = true; //little trick to continue
 			pushError("Syntax analysis error.");
 		}
 	}
 
 	public void setConsole(Console console) {
 		this.console = console;
 	}
 
 	/**
 	 * Creates processes and connect them with pipes. After this it starts them.
 	 * 
 	 * @param analysis
 	 *            analyzed processes from SyntaxAnalyzer
 	 * @throws ClassNotFoundException
 	 * @throws InterruptedException
 	 */
 	@SuppressWarnings("resource")
 	private void runProcessesAndCreatePipes(CommandAnalysis analysis)
 			throws ClassNotFoundException, InterruptedException {
 		LinkedList<Integer> processesInCreation = new LinkedList<Integer>();
 
 		ProcessAnalysis[] pa = analysis.getProcessAnalysis();
 		this.isAsync = analysis.isAsync();
 
 		ProcessAnalysis parsedProcess = null;
 
 		Pipe connect;
 
 		for (int i = 0; i < pa.length; i++) {
 			try {
 
 				parsedProcess = pa[i];
 				/** Testing TODO remove me **/
 
 				String bem = "job.";
 				if (parsedProcess.getNameOfProcess().equalsIgnoreCase("shell"))
 					bem = "shell.";
 				/** Testing TODO remove me **/
 
 				this.tmpPID = this.createJob(
 						bem + parsedProcess.getNameOfProcess(), this.jobErr);
 				if (!this.isAsync)
 					this.activeJopPID = this.tmpPID;
 
 
 				String in = "";
 				String out = "";
 
 				if(parsedProcess.getStdIn() != null && parsedProcess.getStdIn().length() > 0){
 					in = getFilePath(getCurrentDirectory(), parsedProcess.getStdIn());
 				}
 
 				if(parsedProcess.getStdOut() != null && parsedProcess.getStdOut().length() > 0){
 					out = getFilePath(getCurrentDirectory(), parsedProcess.getStdOut());
 				}
 
 				System.out.println("IN:  " + in);
 				System.out.println("OUT: " + out);
 
 				if (i > 0) {
 					connect = new Pipe(null, null);
 
 
 
 					os.Init.setPipes(
 							// aktualni job
 							this.tmpPID,
 							(parsedProcess.getStdIn().length() < 1) ? connect : new Pipe(new FileReader(in), null),
 									(parsedProcess.getStdOut().length() < 1) ? this.jobOut : new Pipe(null, new FileWriter(out)),
 											this.jobErr,
 											parsedProcess.getArguments());
 
 					os.Init.setOutputPipe(
 							processesInCreation.getLast(), (pa[i - 1].getStdOut().length() < 1) ? connect : new Pipe(null, new FileWriter(getFilePath(this.currentDirectory, pa[i - 1].getStdOut())))); // predchozi job
 
 				} else if (i == 0) {
 					os.Init.setPipes(
 							this.tmpPID,
 							(parsedProcess.getStdIn().length() < 1) ? null : new Pipe(new FileReader(in), null),
 									(parsedProcess.getStdOut().length() < 1) ? this.jobOut : new Pipe(null, new FileWriter(out)),
 											this.jobErr,
 											parsedProcess.getArguments());
 				}
 
 				processesInCreation.add(this.tmpPID);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		for (int i = 0; i < processesInCreation.size(); i++) {
 			try {
 
 				if(!this.isAsync)
 					this.runningProcesses.add(processesInCreation.get(i));
 
 				os.Init.startJob(processesInCreation.get(i));
 				System.out.println("tmpPID: " + processesInCreation.get(i)
 						+ " started"
 						+ (this.isAsync ? " on background" : " on foreground"));
 
 
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static String getFilePath(String currentDirectory2, String file) {	
 		System.out.println("Current directory: " + currentDirectory2);
 		if(file.compareTo(".") == 0){
 			return currentDirectory2;
 		}
 
 		File newFile;
 
 		if(file.compareTo("..") == 0){
 			File oldFile = new File(currentDirectory2);
 			newFile = oldFile.getParentFile();
 
 		} else if (isAbsolute(file)) {
 			System.out.println("ABSOLUTE");
 			newFile = new File(file);
 
 		} else {
 			System.out.println("RELATIVE");
 			newFile = new File(((currentDirectory2.endsWith(Init.FS) ? currentDirectory2 : currentDirectory2 + Init.FS)) + file);
 		}
 
 		return newFile.getAbsolutePath();
 
 	}
 
 	private static boolean isAbsolute(String file) {
 		if(Init.OsName.contains("Win")){			
 			Pattern pattern = Pattern.compile("^([A-Za-z]:)"); //zacina na PISMENO: např.  C: nebo f: atd.
 			Matcher matcher = pattern.matcher(file);
 
 			if (matcher.find()){
 				return true;
 			}
 		} else{
 			if(file.startsWith(Init.FS)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public String getManual() {
 		// TODO Make it better
 		return "I am not your mom.";
 	}
 
 	@Override
 	protected void getJobDone() throws InterruptedException {
 
 		if (this.console == null) {
 			// This shell has been started from another shell.
 			try {
 				Shell parent = (Shell) Init.getJob(this.parentPID);
 				parent.takeControl(this);
 			} catch (ClassCastException e) {
 				pushError("My parent is not shell.");
 			} catch (NullPointerException e) {
 				pushError("My parent doesn't exist.");
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			// Starts the Threads
 			this.TstdErr.start();
 			this.TstdIn.start();
 			this.TstdOut.start();
 
 			// And waits for them.
 			this.TstdErr.join();
 			this.TstdIn.join();
 			this.TstdOut.join();
 
 		} catch (InterruptedException e) {
 			// This shell got SIG_KILL while working normally.
 			this.closeShell();
 			// After all throw Interruption ahed of us.
 			currentThread().interrupt();
 			return;
 		}
 
 		System.out.println("SHELL " + this.PID + " ENDED NORMALLY.");
 
 	}
 
 	/**
 	 * Closes the Shell.
 	 * 
 	 */
 	public void closeShell() {
 
 		this.TstdErr.interrupt();
 		this.TstdIn.interrupt();
 		this.TstdOut.interrupt();
 
 		if(this.imLast)
 			this.console.closeTheConsole(false);
 
 		System.out.println("Closing SHELL.");
 
 		try {
 			this.jobOut.closeForReal();
 			this.jobErr.closeForReal();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// do nothing
 			//e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Read jobErr and write stdOut.
 	 */
 	private void stdErrThread() {
 
 		char[] chars = new char[BUF_SIZE];
 		int i = 0;
 		Integer[] pid = new Integer[1];
 		StringBuilder str = new StringBuilder();
 
 		try {
 
 			while (i > -1) {
 
 				i = Shell.this.jobErr.getData(chars, pid);
 
 				if (!this.runningProcesses.contains(pid[0])) {
 					str.setLength(0);
 					str.append("[" + pid[0] + "]");
 					pushData(str.toString().toCharArray(), 0, str.length());
 				}
 
 				pushData(chars, 0, i);
 
 			}
 
 		} catch (IOException e) {
 			// TODO KILL SHELL WHEN ONE THREAD GOT BROKEN
 			e.printStackTrace();
 			return;
 		} catch (InterruptedException e) {
 			// TODO called when ending shell
 			// e.printStackTrace();
 			return;
 		}
 	}
 
 	/**
 	 * Read jobIn and write stdOut.
 	 * 
 	 */
 	private void stdOutThread() {
 
 		char[] chars = new char[BUF_SIZE];
 		int i = 0;
 		Integer[] pid = new Integer[1];
 		StringBuilder str = new StringBuilder();
 
 		try {
 			while (i > -1) {
 				i = Shell.this.jobOut.getData(chars, pid);
 				//Boolean isOn =  this.runningProcesses.contains(pid[0]);
 
 				if (!this.runningProcesses.contains(pid[0])) {
 					str.setLength(0);
 					str.append("[" + pid[0] + "]");
 					pushData(str.toString().toCharArray(), 0, str.length());
 				}
 
 				pushData(chars, 0, i);
 			}
 
 		} catch (IOException e) {
 			// TODO KILL SHELL WHEN ONE THREAD GOT BROKEN
 			e.printStackTrace();
 			return;
 		} catch (InterruptedException e) {
 			// TODO called when ending shell
 			// e.printStackTrace();
 			return;
 		}
 	}
 
 	/**
 	 * Read stdIn.
 	 * 
 	 */
 	private void stdInThread() {
 		char[] chars = new char[BUF_SIZE];
 		str = new StringBuilder();
 		StringBuilder subStr = new StringBuilder();
 		int i = 0;
 		int j = 0;
 		int k = 0;
 		boolean openQuotes = false;
 		try {
 
 			while (i > -1) {
 				if(openQuotes)
 					unlockConsole(false);
 
 				i = getData(chars);
 				if (i == -1) {
 					runJob(str.toString());
 					str.setLength(0);
 				} else {
 					str.append(chars, 0, i);
 					j = 0;
 
 
 					while((j = str.indexOf("\n", j)) != -1){
 						subStr.setLength(0);
 						subStr.append(str.substring(0, j));
 						k = 0;
 						openQuotes = false;
 						while((k = subStr.indexOf("\"", k)) != -1){
 							openQuotes = !openQuotes;
 							k++;
 						}
 						if(!openQuotes){
 							runJob(str.substring(0, j));
 							str.delete(0, j + 1);
 							j = 0;
 						}
 						else
 							j++;
 					}
 				}
 
 			}
 
 			/** Proper end. (Finished file read) **/
 			this.TstdErr.interrupt();
 			this.TstdOut.interrupt();
 
 
 		} catch (IOException e) {
 			// TODO KILL SHELL WHEN ONE THREAD GOT BROKEN
 			e.printStackTrace();
 			return;
 		} catch (InterruptedException e) {
 			// TODO called when ending shell
 			// e.printStackTrace();
 			return;
 		}
 	}
 
 	/**
 	 * SigKill actual job.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public void callSigKill() throws InterruptedException {
 		if(getCommandString().length() > 0){
 			resetCommandString();
 			unlockConsole(true);
 		}
 		
 		if (this.activeJopPID == null) {
 			// nothing to kill
 			return;
 		}
 		Init.killJob(this.activeJopPID);
 	}
 
 	/**
 	 * Starts a job and waiting at the end.
 	 * 
 	 * @param str
 	 *            job name
 	 * @throws InterruptedException
 	 */
 	private void runJob(String job) throws InterruptedException {
 		String str = job;
 		try {
 			str = str.replace("\uFEFF", "");//remove BOM character
 			str = str.trim();
 			if (str.length() > 0) {
 				this.console.addToHistory(str);
 				doYourJob(str);
 				if (!this.isAsync) {
 					joinJob(this.activeJopPID);
 
 					System.out.println("PID: " + this.activeJopPID + " ended.");
 					if(this.waitingPID != null){
 						System.out.println("Somebody was waiting to be pushed to fg.");
 						this.activeJopPID = this.waitingPID;
 						this.runningProcesses.add(this.activeJopPID);
 						this.waitingPID = null;
 						System.out.println("Waiting for new fg job to finish.");
 						joinJob(this.activeJopPID);
 						System.out.println("New fg job finished.");
 					}
 					
 					this.activeJopPID = null;
 
 					/** In case someone took console. **/
 					if (this.console.getShell() != this)
 						this.console.setShell(this);
 
 					if(!this.imFromFile)
 						this.unlockConsole(true);
 					
 					this.runningProcesses.clear();//after join there are no running processes
 
 					// System.out.println("job: " + this.tmpPID + " is done.");
 				} else {
 					if(!this.imFromFile)
 						this.unlockConsole(true);
 					
 				}
 			} else {
 				if(!this.imFromFile){
 					this.unlockConsole(true);
 				}
 			}
 		} catch (ClassNotFoundException e) {
 			pushError("Job not found!");
 			if(!this.imFromFile)
 				this.unlockConsole(true);
 		}
 	}
 
 	/**
 	 * Simple test of regularity of commands. (Test if the command is not few spaces).
 	 * @param str Input string
 	 * @return true for regular data
 	 */
 	private static boolean testRegular(String str) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * FIXME make it cool!
 	 * 
 	 * @param Set true if you want a new line
 	 * @throws InterruptedException 
 	 */
 	private void unlockConsole(boolean newLine) throws InterruptedException{
 
 		getStdErr().waitForEmpty();
 		getStdOut().waitForEmpty();
 		this.console.setUnrestricted(newLine);
 	}
 
 	/**
 	 * For child shell to take control over console.
 	 * 
 	 * @param childShell
 	 * @throws InterruptedException
 	 */
 	public void takeControl(Shell childShell) throws InterruptedException {
 		if(childShell.getStdErr().equals(this.jobErr))//if childShell got classic job pipe give him direct pipe
 			childShell.setStdErr(getStdErr());
 
 		boolean fromFile = false;
 		if(childShell.getStdIn() == null){
 			childShell.setStdIn(getStdIn());
 		}
 		else{
 			fromFile = true;
 		}
 
 		if(childShell.getStdOut() == null)
 			childShell.setStdOut(getStdOut());
 
 		childShell.setConsole(this.console);
 		this.console.setShell(childShell);
 		if(!fromFile){
 			this.unlockConsole(true);
 		}
 		else{
 			childShell.setFromFile();
 		}
 
 
 	}
 
 	@Override
 	public void childHasEnded(Integer childPID) {
 		// FIXME CONDITION RUN!!
 		// this.runningProcesses.remove(childPID);
 	}
 }
