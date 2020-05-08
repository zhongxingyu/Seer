 package shell;
 
 import gui.Console;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedList;
 
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
 
 	/**
 	 * Classic Constructor.
 	 * 
 	 * @param tmpPID
 	 * @param stdERR
 	 */
 	public Shell(Integer tmpPID, Pipe stdERR) {
 		super(tmpPID, stdERR);
 		this.currentDirectory = new File("").getAbsolutePath();
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
 		this.currentDirectory = currentDirectory;
 	}
 
 
 	public void setLast(){
 		this.imLast = true;
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
 				// TODO opravit pripad jako """""""""""""""
 				/** Testing TODO remove me **/
 
 				String bem = "job.";
 				if (parsedProcess.getNameOfProcess().equalsIgnoreCase("shell"))
 					bem = "shell.";
 				/** Testing TODO remove me **/
 
 				this.tmpPID = this.createJob(
 						bem + parsedProcess.getNameOfProcess(), this.jobErr);
 				if (!this.isAsync)
 					this.activeJopPID = this.tmpPID;
 
 				if (i > 0) {
 					connect = new Pipe(null, null);
 
 					os.Init.setPipes(
 							// aktualni job
 							this.tmpPID,
 							(parsedProcess.getStdIn().length() < 1) ? connect : new Pipe(new FileReader(parsedProcess.getStdIn()), null),
							(parsedProcess.getStdOut().length() < 1) ? this.jobOut : new Pipe(null, new FileWriter(parsedProcess.getStdOut())),
							this.jobErr,
							parsedProcess.getArguments());
 
 					os.Init.setOutputPipe(
 							processesInCreation.getLast(), (pa[i - 1].getStdOut().length() < 1) ? connect : new Pipe(null, new FileWriter(pa[i - 1].getStdOut()))); // predchozi job
 
 				} else if (i == 0) {
 					os.Init.setPipes(
 							this.tmpPID,
							(parsedProcess.getStdIn().length() < 1) ? null : new Pipe(new FileReader(parsedProcess.getStdIn()), null),
							(parsedProcess.getStdOut().length() < 1) ? this.jobOut : new Pipe(null, new FileWriter(parsedProcess.getStdOut())),
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
 			// TODO Auto-generated catch block
 			e.printStackTrace();
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
 		StringBuilder str = new StringBuilder();
 		int i = 0;
 		int j = 0;
 
 		try {
 
 			while (i > -1) {
 				i = getData(chars);
 				if (i == -1) {
 					runJob(str.toString());
 					str.setLength(0);
 				} else {
 					str.append(chars, 0, i);
 					j = 0;
 					//if((j = str.indexOf("\n")) != -1){
 					while((j = str.indexOf("\n")) != -1){
 						runJob(str.substring(0, j));
 						//str.setLength(0);
 						str.delete(0, j + 1);
 					}
 					//this.unlockConsole();
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
 	private void runJob(String str) throws InterruptedException {
 		try {
 			str = str.replace("\uFEFF", "");//remove BOM character
 			str = str.trim();
 			if (str.length() > 0) {
 				doYourJob(str);
 				if (!this.isAsync) {
 					joinJob(this.activeJopPID);
 
 					this.activeJopPID = null;
 
 					/** In case someone took console. **/
 					if (this.console.getShell() != this)
 						this.console.setShell(this);
 
 					this.unlockConsole();
 
 					// System.out.println("job: " + this.tmpPID + " is done.");
 				} else {
 					this.unlockConsole();
 				}
 			} else {
 				this.unlockConsole();
 			}
 		} catch (ClassNotFoundException e) {
 			pushError("Job not found!");
 			this.unlockConsole();
 		}
 	}
 
 	/**
 	 * Simple test of regularity of commands. (Test if the command is not few spaces).
 	 * @param str Input string
 	 * @return true for regular data
 	 */
 	private boolean testRegular(String str) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * FIXME make it cool!
 	 * 
 	 * @throws InterruptedException 
 	 */
 	private void unlockConsole() throws InterruptedException{
 
 		getStdErr().waitForEmpty();
 		getStdOut().waitForEmpty();
 		this.console.setUnrestricted();
 
 	}
 
 	/**
 	 * For child shell to take control over console.
 	 * 
 	 * @param childShell
 	 * @throws InterruptedException
 	 */
 	public void takeControl(Shell childShell) throws InterruptedException {
 		if(childShell.getStdErr().equals(jobErr))//if childShell got classic job pipe give him direct pipe
 			childShell.setStdErr(getStdErr());
 		
 		boolean needConsole = false;
 		if(childShell.getStdIn() == null){
 			childShell.setStdIn(getStdIn());
 		}
 		else{
 			needConsole = true;
 		}
 		if(childShell.getStdOut() == null)
 			childShell.setStdOut(getStdOut());
 		
 		childShell.setConsole(this.console);
 		this.console.setShell(childShell);
 		if(needConsole)
 			this.unlockConsole();
 	}
 
 	@Override
 	public void childHasEnded(Integer childPID) {
 		// FIXME CONDITION RUN!!
 		// this.runningProcesses.remove(childPID);
 	}
 }
