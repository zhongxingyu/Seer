 package org.GreenTeaScript.DShell;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.EmptyStackException;
 import java.util.Stack;
 import java.util.regex.Pattern;
 
 import org.GreenTeaScript.DShellGrammar;
 import org.GreenTeaScript.LibGreenTea;
 
 public class DShellProcess {
 	// option flag
 	private static final int returnable  = 1 << 0;
 	private static final int printable   = 1 << 1;
 	private static final int throwable   = 1 << 2;
 	private static final int background  = 1 << 3;
 	private static final int enableTrace = 1 << 4;
 
 	// return type
 	private static final int VoidType    = 0;
 	private static final int BooleanType = 1;
 	private static final int StringType  = 2;
 	
 	// trace tyep
 	private static boolean traceRequirement = false;
 
 	private String Result = "";
 	private long ReturnValue = -1;
 	public final int CommandFlag;
 	private final PseudoProcess LastProcess;
 	public final PseudoProcess[] Processes;
 	public long timeout;
 	private ProcMonitor procMonitor;
 	
 	public DShellProcess(int option, String[][] cmds, long timeout) {
 		// init process
 		this.Processes = createProcs(cmds, is(option, enableTrace));
 		int ProcessSize = this.Processes.length;
 		ErrorStreamHandler Handler = new ErrorStreamHandler(this.Processes);
 
 		// start process
 		int lastIndex = ProcessSize - 1;
 		this.Processes[0].start();
 		for(int i = 1; i < ProcessSize; i++) {
 			this.Processes[i].start();
 			this.Processes[i].pipe(this.Processes[i - 1]);
 		}
 		Handler.showErrorMessage();
 		this.CommandFlag = option;
 		this.LastProcess = this.Processes[lastIndex];
 		this.timeout = timeout;
 	}
 	private boolean IsBackGroundProcess() {
 		return is(this.CommandFlag, background);
 	}
 	private DShellProcess Detach() {
 		this.LastProcess.showResult();
 		this.procMonitor = new ProcMonitor(this, true);
 		this.procMonitor.start();
 		return null;
 	}
 	private Object GetResult(int ReturnType) throws Exception {
 		this.procMonitor = new ProcMonitor(this, false);
 		this.procMonitor.start();
 		this.waitResult();
 		// raise exception
 		if(this.timeout <= 0) {
 			ShellExceptionRaiser raiser = new ShellExceptionRaiser(is(CommandFlag, throwable));
 			raiser.setProcesses(this.Processes);
 			raiser.raiseException();
 		}
 		this.Result = LastProcess.getStdout();
 		this.ReturnValue = LastProcess.getRet();
 		// get result value
 		if(is(this.CommandFlag, returnable)) {
 			if(ReturnType == StringType) {
 				return this.Result;
 			}
 			else if(ReturnType == BooleanType) {
 				return new Boolean(this.ReturnValue == 0);
 			}
 		}
 		return null;
 	}
 	private void waitResult() {
 		this.LastProcess.waitResult(is(this.CommandFlag, printable));
 	}
 	public void join() {
 		try {
 			this.procMonitor.join();
 			((SubProc)this.LastProcess).getMessageStreamHandler().join();
 		} 
 		catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	public String getResult() {
 		return this.LastProcess.getStdout();
 	}
 	
 	// initialization
 	public static void initDShellProcess() {
 		traceRequirement = checkTraceRequirements();
 	}
 
 	// called by JavaByteCodeGenerator.VisitCommandNode 
 	public static void ExecCommandVoid(String[]... cmds) throws Exception {
 		int option = printable | throwable | enableTrace;
 		runCommands(cmds, option, VoidType);
 	}
 	public static String ExecCommandString(String[]... cmds) throws Exception {
 		int option = returnable | throwable | enableTrace;
 		return (String) runCommands(cmds, option, StringType);
 	}
 	public static boolean ExecCommandBool(String[]... cmds) throws Exception {
 		int option = returnable | printable;
 		return ((Boolean) runCommands(cmds, option, BooleanType)).booleanValue();
 	}
 
 	// file system roll back function
 	// TargetLV: vg_name/lv_name
 	public static boolean CreateSnapshot(String SnapshotLabel, String TargetLV) throws Exception {
 		if(System.getProperty("os.name").equals("Linux") && new File("/sbin/lvm").canExecute()) {
 			String[] cmds = {"sudo", "lvcreate", "-s", "-n", SnapshotLabel, TargetLV};
 			return ExecCommandBool(cmds);
 		}
 		return false;
 	}
 
 	public static boolean RevertSnapshot(String SnapshotLabel, String TargetLV) throws Exception {
 		if(System.getProperty("os.name").equals("Linux") && new File("/sbin/lvm").canExecute()) {
 			String[] volNames = TargetLV.split("/");
 			String VG_Name = volNames[0];
 			String LV_Name = volNames[1];
 			String mountableId = VG_Name + "-" + LV_Name;
 			
 			// get target LV mount point
 			int option = returnable | throwable;
 			String[][] mountPoint_cmds = {{"mount"}, {"grep", mountableId}};
 			String mountPoint;
 			try {
 				String[] results = ((String) runCommands(mountPoint_cmds, option, StringType)).split(" ");
 				mountPoint = results[2];
 			} catch (Exception e) {
 				return false;
 			}
 			
 			// umount target LV
 			String[] umount_cmds = {"sudo", "umount", mountPoint};
 			if(!ExecCommandBool(umount_cmds)) {
 				return false;
 			}
 			
 			// remove current LV
 			String[] lvremove_cmds = {"sudo", "lvremove", TargetLV};
 			if(!ExecCommandBool(lvremove_cmds)) {
 				return false;
 			}
 			
 			// rename snapshot
 			String[] lvrename_cmds = {"sudo", "lvrename", VG_Name + "/" + SnapshotLabel, LV_Name};
 			if(!ExecCommandBool(lvrename_cmds)) {
 				return false;
 			}
 			
 			// mount LV
 			String[] mount_cmds = {"sudo", "mount", "/dev/" + TargetLV, mountPoint};
 			return ExecCommandBool(mount_cmds);
 		}
 		return false;
 	}
 	
 	// change directory
 	public static boolean ChangeDirectory(String path) {
 		if(LibGreenTea.EqualsString(path, "")) {
 			return CLibraryWrapper.INSTANCE.chdir(System.getenv("HOME")) == 0;
 		}
 		return CLibraryWrapper.INSTANCE.chdir(path) == 0;
 	}
 
 	//---------------------------------------------
 
 	private static boolean checkTraceRequirements() {
 		if(System.getProperty("os.name").equals("Linux")) {
 			boolean flag = DShellGrammar.IsUnixCommand("strace+") && 
 					DShellGrammar.IsUnixCommand("pretty_print_strace_out.py");
 			if(flag) {
 				SubProc.traceBackendType = SubProc.traceBackend_strace_plus;
 				return true;
 			}
 			else {
 				SubProc.traceBackendType = SubProc.traceBackend_strace;
 				return DShellGrammar.IsUnixCommand("strace");
 			}
 		}
 		System.err.println("Systemcall Trace is Not Supported");
 		return false;
 	}
 
 	private static boolean is(int option, int flag) {
 		option &= flag;
 		return option == flag;
 	}
 
 	private static int setFlag(int option, int flag, boolean set) {
 		if(set && !is(option, flag)) {
 			return option | flag;
 		}
 		else if(!set && is(option, flag)) {
 			return option & ~flag;
 		}
 		return option;
 	}
 
 	private static PseudoProcess[] createProcs(String[][] cmds, boolean enableSyscallTrace) {
 		ArrayList<PseudoProcess> procBuffer = new ArrayList<PseudoProcess>();
 		int cmdsNum = cmds.length;
 		for(int i = 0; i < cmdsNum; i++) {
 			String[] currentCmd = cmds[i];
 			String cmdSymbol = currentCmd[0];
 			SubProc prevProc = null;
 			int size = procBuffer.size();
 			if(size > 0) {
 				prevProc = (SubProc)procBuffer.get(size - 1);
 			}
 			
 			if(LibGreenTea.EqualsString(cmdSymbol, "<")) {
 				prevProc.setInputRedirect(currentCmd[1]);
 			}
 			else if(LibGreenTea.EqualsString(cmdSymbol, "1>") || LibGreenTea.EqualsString(cmdSymbol, ">")) {
 				prevProc.setOutputRedirect(SubProc.STDOUT_FILENO, currentCmd[1], false);
 			}
 			else if(LibGreenTea.EqualsString(cmdSymbol, "1>>") || LibGreenTea.EqualsString(cmdSymbol, ">>")) {
 				prevProc.setOutputRedirect(SubProc.STDOUT_FILENO, currentCmd[1], true);
 			}
 			else if(LibGreenTea.EqualsString(cmdSymbol, "2>")) {
 				prevProc.setOutputRedirect(SubProc.STDERR_FILENO, currentCmd[1], false);
 			}
 			else if(LibGreenTea.EqualsString(cmdSymbol, "2>>")) {
 				prevProc.setOutputRedirect(SubProc.STDERR_FILENO, currentCmd[1], true);
 			}
 			else if(LibGreenTea.EqualsString(cmdSymbol, "&>") || LibGreenTea.EqualsString(cmdSymbol, ">&")) {
 				prevProc.setOutputRedirect(SubProc.STDOUT_FILENO, currentCmd[1], false);
 				prevProc.setMergeType(SubProc.mergeErrorToOut);
 			}
 			else if(LibGreenTea.EqualsString(cmdSymbol, "&>>")) {
 				prevProc.setOutputRedirect(SubProc.STDOUT_FILENO, currentCmd[1], true);
 				prevProc.setMergeType(SubProc.mergeErrorToOut);
 			}
 			else if(LibGreenTea.EqualsString(cmdSymbol, ">&1") || 
 					LibGreenTea.EqualsString(cmdSymbol, "1>&1") || LibGreenTea.EqualsString(cmdSymbol, "2>&2")) {
 				// do nothing
 			}
 			else if(LibGreenTea.EqualsString(cmdSymbol, "1>&2")) {
 				prevProc.setMergeType(SubProc.mergeOutToError);
 			}
 			else if(LibGreenTea.EqualsString(cmdSymbol, "2>&1")) {
 				prevProc.setMergeType(SubProc.mergeErrorToOut);
 			}
 			else {
 				SubProc proc = new SubProc(enableSyscallTrace);
 				proc.setArgument(currentCmd);
 				procBuffer.add(proc);
 			}
 		}
 		
 		int bufferSize = procBuffer.size();
 		PseudoProcess[] procs = new PseudoProcess[bufferSize];
 		for(int i = 0; i < bufferSize; i++) {
 			procs[i] = procBuffer.get(i);
 		}
 		return procs;
 	}
 
 	private static Object runCommands(String[][] cmds, int option, int retType) throws Exception {
 		// prepare shell option
 		long timeout = -1;
 		ArrayList<String[]> newCmdsBuffer = new ArrayList<String[]>();
 		for(int i = 0; i < cmds.length; i++) {	// check background
 			String[] currentCmd = cmds[i];
 //			if(LibGreenTea.EqualsString(currentCmd[0], "set")) {
 //				if(currentCmd.length < 2) {
 //					continue;
 //				}
 //				String subOption = currentCmd[1];
 //				if(LibGreenTea.EqualsString(subOption, "trace=on")) {
 //					option = setFlag(option, enableTrace, true);
 //				}
 //				else if(LibGreenTea.EqualsString(subOption, "trace=off")) {
 //					option = setFlag(option, enableTrace, false);
 //				}
 //			}
 //			else 
 			if(LibGreenTea.EqualsString(currentCmd[0], "&")) {
 				option = setFlag(option, background, !is(option, returnable));
 			}
 			else {
 				newCmdsBuffer.add(currentCmd);
 			}
 		}
 		int bufferSize = newCmdsBuffer.size();
 		for(int i = 0; i < bufferSize; i++) {	// check internal option
 			String[] currentCmd = newCmdsBuffer.get(i);
 			if(LibGreenTea.EqualsString(currentCmd[0], "timeout")) {
 				StringBuilder numBuilder = new StringBuilder();
 				StringBuilder unitBuilder = new StringBuilder();
 				int len = currentCmd[1].length();
 				for(int j = 0; j < len; j++) {
 					char ch = currentCmd[1].charAt(j);
 					if(Character.isDigit(ch)) {
 						numBuilder.append(ch);
 					} 
 					else {
 						unitBuilder.append(ch);
 					}
 				}
 				long num = Integer.parseInt(numBuilder.toString());
 				String unit = unitBuilder.toString();
 				if(LibGreenTea.EqualsString(unit, "s")) {
 					num = num * 1000;
 				}
 				if(num >= 0) {
 					timeout = num;
 				}
 				String[] newCmd = new String[currentCmd.length - 2];
 				for(int j = 2; j < currentCmd.length; j++) {
 					newCmd[j - 2] = currentCmd[j];
 				}
 				newCmdsBuffer.set(i, newCmd);
 			}
 		}
 		
 		String[][] newCmds = newCmdsBuffer.toArray(new String[newCmdsBuffer.size()][]);
 		
 		if(is(option, enableTrace)) {
 			option = setFlag(option, enableTrace, traceRequirement);
 		}
 		
 		// run command
 		DShellProcess Process = new DShellProcess(option, newCmds, timeout);
 		if(Process.IsBackGroundProcess()) {
 			return Process.Detach();
 		}
 		return Process.GetResult(retType);
 	}
 }
 
 interface CLibraryWrapper extends com.sun.jna.Library {
 	CLibraryWrapper INSTANCE = (CLibraryWrapper) com.sun.jna.Native.loadLibrary("c", CLibraryWrapper.class);
 	
 	int chdir(String path);
 	int seteuid(int uid);
 	int getuid();
 }
 
 class PseudoProcess {
 	public final static int mergeErrorToOut = 0;
 	public final static int mergeOutToError = 1;
 
 	protected PseudoProcess pipedPrevProc;
 
 	protected OutputStream stdin = null;
 	protected InputStream stdout = null;
 	protected InputStream stderr = null;
 
 	protected StringBuilder cmdNameBuilder;
 	protected ArrayList<String> commandList;
 
 	protected boolean stdoutIsDirty = false;
 	protected boolean stderrIsDirty = false;
 
 	protected int mergeType = -1;
 	protected int retValue = 0;
 
 	public PseudoProcess() {
 		this.cmdNameBuilder = new StringBuilder();
 		this.commandList = new ArrayList<String>();
 	}
 
 	public void setArgument(String Arg) {
 		this.cmdNameBuilder.append(Arg + " ");
 		this.commandList.add(Arg);
 	}
 
 	public void setArgument(String[] Args) {
 		for(int i = 0; i < Args.length; i++) {
 			this.setArgument(Args[i]);
 		}
 	}
 
 	public void setMergeType(int mergeType) {
 		this.mergeType = mergeType;
 	}
 
 	public void start() {
 	}
 
 	public void pipe(PseudoProcess srcProc) {
 		new PipeStreamHandler(srcProc.accessOutStream(), this.stdin, true).start();
 	}
 
 	public void kill() {
 	}
 
 	public void waitFor() {
 	}
 
 	public void waitResult(boolean isPrintable) {
 	}
 	
 	public void showResult() {
 	}
 
 	public String getStdout() {
 		return "";
 	}
 
 	public String getStderr() {
 		return "";
 	}
 
 	public InputStream accessOutStream() {
 		if(!this.stdoutIsDirty) {
 			this.stdoutIsDirty = true;
 			return this.stdout;
 		}
 		return null;
 	}
 
 	public InputStream accessErrorStream() {
 		if(!this.stderrIsDirty) {
 			this.stderrIsDirty = true;
 			return this.stderr;
 		}
 		return null;
 	}
 
 	public int getRet() {
 		return this.retValue;
 	}
 
 	public String getCmdName() {
 		return this.cmdNameBuilder.toString();
 	}
 
 	public boolean isTraced() {
 		return false;
 	}
 }
 
 class SubProc extends PseudoProcess {
 	public final static int traceBackend_strace = 0;
 	public final static int traceBackend_strace_plus = 1;
 	public static int traceBackendType = traceBackend_strace;
 
 	private final static String logdirPath = "/tmp/strace-log";
 	private static int logId = 0;
 	
 	public final static int STDOUT_FILENO = 1;
 	public final static int STDERR_FILENO = 2;
 
 	private Process proc;
 	private boolean enableSyscallTrace = false;
 	public boolean isKilled = false;
 	public String logFilePath = null;
 	
 	private FileInputStream inFileStream = null;
 	private FileOutputStream outFileStream = null;
 	private FileOutputStream errFileStream = null;
 	
 	private ByteArrayOutputStream messageBuffer;
 	private PipeStreamHandler messageStreamHandler = null;
 
 	private static String createLogDirectory() {
 		Calendar cal = Calendar.getInstance();
 		StringBuilder pathBuilder = new StringBuilder();
 		
 		pathBuilder.append(logdirPath + "/");
 		pathBuilder.append(cal.get(Calendar.YEAR) + "-");
 		pathBuilder.append((cal.get(Calendar.MONTH) + 1) + "-");
 		pathBuilder.append(cal.get(Calendar.DATE));
 		
 		String subdirPath = pathBuilder.toString();
 		File subdir = new File(subdirPath);
 		subdir.mkdirs();
 		
 		return subdirPath;
 	}
 
 	private static String createLogNameHeader() {
 		Calendar cal = Calendar.getInstance();
 		StringBuilder logNameHeader = new StringBuilder();
 		
 		logNameHeader.append(cal.get((Calendar.HOUR) + 1) + ":");
 		logNameHeader.append(cal.get(Calendar.MINUTE) + "-");
 		logNameHeader.append(cal.get(Calendar.MILLISECOND));
 		logNameHeader.append("-" + logId++);
 
 		return logNameHeader.toString();
 	}
 
 	public static void deleteLogFile(String logFilePath) {
 		new File(logFilePath).delete();
 	}
 
 	public SubProc(boolean enableSyscallTrace) {
 		super();
 		this.enableSyscallTrace = enableSyscallTrace;
 		initTrace();
 	}
 
 	private void initTrace() {
 		if(this.enableSyscallTrace) {
 			String currentLogdirPath = createLogDirectory();
 			String logNameHeader = createLogNameHeader();
 			logFilePath = new String(currentLogdirPath + "/" + logNameHeader + ".log");
 
 			String[] traceCmd;
 			if(traceBackendType == traceBackend_strace) {
 				String[] backend_strace = {"strace", "-t", "-f", "-F", "-o", logFilePath};
 				traceCmd = backend_strace;
 			}
 			else if(traceBackendType == traceBackend_strace_plus) {
 				String[] backend_strace_plus = {"strace+", "-k", "-t", "-f", "-F", "-o", logFilePath};
 				traceCmd = backend_strace_plus;
 			}
 			else {
 				throw new RuntimeException("invalid trace backend type");
 			}
 			
 			for(int i = 0; i < traceCmd.length; i++) {
 				this.commandList.add(traceCmd[i]);
 			}
 		}
 	}
 
 	@Override public void setArgument(String[] Args) {
 		String arg = Args[0];
 		this.cmdNameBuilder.append(arg + " ");
 		if(LibGreenTea.EqualsString(arg, "sudo")) {
 			int size = this.commandList.size();
 			ArrayList<String> newCommandList = new ArrayList<String>();
 			newCommandList.add(arg);
 			for(int i = 0; i < size; i++) {
 				newCommandList.add(this.commandList.get(i));
 			}
 			this.commandList = newCommandList;
 		}
 		else {
 			this.commandList.add(arg);
 		}
 		
 		for(int i = 1; i < Args.length; i++) {
 			this.setArgument(Args[i]);
 		}
 	}
 
 	@Override public void start() {
 		int size = this.commandList.size();
 		String[] cmd = new String[size];
 		for(int i = 0; i < size; i++) {
 			cmd[i] = this.commandList.get(i);
 		}
 
 		try {
 			ProcessBuilder procBuilder = new ProcessBuilder(cmd);
 			if(this.mergeType == mergeErrorToOut || this.mergeType == mergeOutToError) {
 				procBuilder.redirectErrorStream(true);
 			}
 			this.proc = procBuilder.start();
 			this.stdin = this.proc.getOutputStream();
 			if(this.mergeType == mergeOutToError) {
 				this.stdout = this.proc.getErrorStream();
 				this.stderr = this.proc.getInputStream();
 			}
 			else {
 				this.stdout = this.proc.getInputStream();
 				this.stderr = this.proc.getErrorStream();
 			}
 			
 			// input & output redirect
 			readFile();
 			writeFile(STDOUT_FILENO);
 			writeFile(STDERR_FILENO);
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void setInputRedirect(String readFileName) {
 		try {
 			this.inFileStream = new FileInputStream(readFileName);
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void readFile() {
 		if(this.inFileStream == null) {
 			return;
 		}
 		InputStream srcStream = new BufferedInputStream(inFileStream);
 		OutputStream destStream = this.stdin;
 		new PipeStreamHandler(srcStream, destStream, true).start();
 	}
 
 	public void setOutputRedirect(int fd, String writeFileName, boolean append) {
 		try {
 			if(fd == STDOUT_FILENO) {
 				this.outFileStream = new FileOutputStream(writeFileName, append);
 			} 
 			else if(fd == STDERR_FILENO) {
 				this.errFileStream = new FileOutputStream(writeFileName, append);
 			}
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void writeFile(int fd) {
 		InputStream srcStream;
 		OutputStream destStream;
 		if(fd == STDOUT_FILENO) {
 			if(this.outFileStream == null) {
 				return;
 			}
 			srcStream = this.accessOutStream();
 			destStream = new BufferedOutputStream(this.outFileStream);
 		}
 		else if(fd == STDERR_FILENO) {
 			if(this.errFileStream == null) {
 				return;
 			}
 			srcStream = this.accessErrorStream();
 			destStream = new BufferedOutputStream(this.errFileStream);
 		}
 		else {
 			throw new RuntimeException("invalid file descriptor");
 		}
 		new PipeStreamHandler(srcStream, destStream, true).start();
 	}
 
 	@Override public void waitResult(boolean isPrintable) {
 		InputStream inStream = this.accessOutStream();
 		OutputStream outStream;
 		boolean closeStream;
 		if(isPrintable) {
 			outStream = System.out;
 			closeStream = false;
 		}
 		else {
 			messageBuffer = new ByteArrayOutputStream();
 			outStream = messageBuffer;
 			closeStream = true;
 		}
 		
 		PipeStreamHandler stdoutHandler = new PipeStreamHandler(inStream, outStream, closeStream);
 		stdoutHandler.start();
 		try {
 			stdoutHandler.join();
 		}
 		catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Override public void showResult() {
 		this.messageBuffer = new ByteArrayOutputStream();
 		OutputStream[] outStreams = {System.out, this.messageBuffer}; 
 		boolean[] closeOutputs = {false, true};
 		this.messageStreamHandler = new PipeStreamHandler(this.accessOutStream(), outStreams, false, closeOutputs);
 		this.messageStreamHandler.start();
 	}
 
 	@Override public String getStdout() {
 		return this.messageBuffer == null ? "" : this.messageBuffer.toString();
 	}
 
 	@Override public void waitFor() {
 		try {
 			this.retValue = this.proc.waitFor();
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override public void kill() {
 		if(System.getProperty("os.name").startsWith("Windows")) {
 			this.proc.destroy();
 			return;
 		} 
 		 
 		try {
 			// get target pid
 			Field pidField = this.proc.getClass().getDeclaredField("pid");
 			pidField.setAccessible(true);
 			int pid = pidField.getInt(this.proc);
 			
 			// kill process
 			String[] cmds = {"kill", "-9", Integer.toString(pid)};
 			Process procKiller = new ProcessBuilder(cmds).start();
 			procKiller.waitFor();
 			this.isKilled = true;
 			//LibGreenTea.print("[killed]: " + this.getCmdName());
 		} 
 		catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} 
 		catch (SecurityException e) {
 			e.printStackTrace();
 		} 
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} 
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 		} 
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Process getInternalProc() {
 		return this.proc;
 	}
 
 	public String getLogFilePath() {
 		return this.logFilePath;
 	}
 
 	@Override public boolean isTraced() {
 		return this.enableSyscallTrace;
 	}
 	
 	public PipeStreamHandler getMessageStreamHandler() {
 		return this.messageStreamHandler;
 	}
 }
 
 class ProcMonitor extends Thread {	// TODO: support exit handler
 	private DShellProcess dShellProc;
 	private boolean isBackground;
 	
 	public ProcMonitor(DShellProcess dShellProc, boolean isBackground) {
 		this.dShellProc = dShellProc;
 		this.isBackground = isBackground;
 	}
 	
 	@Override public void run() {
 		int size = this.dShellProc.Processes.length;
 		if(this.dShellProc.timeout > 0) { // timeout
 			try {
 				StringBuilder msgBuilder = new StringBuilder();
 				msgBuilder.append("timeout processes: ");
 				Thread.sleep(this.dShellProc.timeout);	// ms
 				for(int i = 0; i < size; i++) {
 					this.dShellProc.Processes[i].kill();
 					if(i != 0) {
 						msgBuilder.append("| ");
 					}
 					msgBuilder.append(this.dShellProc.Processes[i].getCmdName());
 				}
 				System.err.println(msgBuilder.toString());
 				// run exit handler
 			} 
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			return;
 		}
 		
 		// check process status
 		while(this.isBackground) {
 			int count = 0;
 			for(int i = 0; i < size; i++) {
 				SubProc subProc = (SubProc)this.dShellProc.Processes[i];
 				try {
 					subProc.getInternalProc().exitValue();
 					count++;
 				}
 				catch(IllegalThreadStateException e) {
 					// process has not terminated yet. do nothing
 				}
 			}
 			if(count == size) {
 				StringBuilder msgBuilder = new StringBuilder();
 				msgBuilder.append("exit processes: ");
 				for(int i = 0; i < size; i++) {
 					if(i != 0) {
 						msgBuilder.append("| ");
 					}
 					msgBuilder.append(this.dShellProc.Processes[i].getCmdName());
 				}
 				System.err.println(msgBuilder.toString());
 				// run exit handler
 				return;
 			}
 			try {
 				Thread.sleep(100); // sleep thread
 			}
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
 
 class ErrorStreamHandler {
 	private PseudoProcess[] targetProcs;
 	
 	public ErrorStreamHandler(PseudoProcess[] targetProcs) {
 		this.targetProcs = targetProcs;
 	}
 	
 	public void showErrorMessage() {
 		for(int i = 0; i < targetProcs.length; i++) {
 			new PipeStreamHandler(this.targetProcs[i].accessErrorStream(), System.err, false).start();
 		}
 	}
 }
 
 // copied from http://blog.art-of-coding.eu/piping-between-processes/
 class PipeStreamHandler extends Thread {
 	private InputStream input;
 	private OutputStream[] outputs;
 	private boolean closeInput;
 	private boolean[] closeOutputs;
 
 	public PipeStreamHandler(InputStream input, OutputStream output, boolean closeStream) {
 		this.input = input;
 		this.outputs = new OutputStream[1];
 		this.outputs[0] = output;
 		if(output == null) {
 			this.outputs[0] = new NullStream();
 		}
 		this.closeInput = closeStream;
 		this.closeOutputs = new boolean[1];
 		this.closeOutputs[0] = closeStream;
 	}
 	
 	public PipeStreamHandler(InputStream input, 
 			OutputStream[] outputs, boolean closeInput, boolean[] closeOutputs) {
 		this.input = input;
 		this.outputs = new OutputStream[outputs.length];
 		this.closeInput = closeInput;
 		this.closeOutputs = closeOutputs;
 		for(int i = 0; i < this.outputs.length; i++) {
 			this.outputs[i] = outputs[i] == null ? new NullStream() : outputs[i];
 		}
 	}
 
 	@Override public void run() {
 		if(this.input == null) {
 			return;
 		}
 		try {
 			byte[] buffer = new byte[512];
 			int read = 0;
 			while(read > -1) {
 				read = this.input.read(buffer, 0, buffer.length);
 				if(read > -1) {
 					for(int i = 0; i < this.outputs.length; i++) {
 						this.outputs[i].write(buffer, 0, read);
 					}
 				}
 			}
 			if(this.closeInput) {
 				this.input.close();
 			}
 			for(int i = 0; i < this.outputs.length; i++) {
 				if(this.closeOutputs[i]) {
 					this.outputs[i].close();
 				}
 			}
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	class NullStream extends OutputStream {
 		@Override public void write(int b) throws IOException {
 			// do nothing
 		}
 	}
 }
 
 class CauseInferencer {
 	// syscall filter
 	private static final Pattern syscallFilter = Pattern.compile("^[1-9][0-9]* .+(.+) *= *.+");
 	private static final Pattern failedSyscallFilter = Pattern.compile("^[1-9][0-9]* .+(.+) *= *-[1-9].+");
 	private static final Pattern localeFilter = Pattern.compile("^.+(.+/locale.+).+");
 	private static final Pattern gconvFilter = Pattern.compile("^.+(.+/usr/lib64/gconv.+).+");
 	
 	// function filter
 	private static final Pattern functionFilter = Pattern.compile("^  > .+");
 	private static final Pattern dcigettextFilter = Pattern.compile("^  > __dcigettext().+");
 	private static final Pattern exitFilter = Pattern.compile("^  > .+exit.*().+");
 	private static final Pattern libcStartMainFilter = Pattern.compile("^  > __libc_start_main().+");
 	
 	private int traceBackendType;
 
 	public CauseInferencer(int traceBackendType) {
 		this.traceBackendType = traceBackendType;
 	}
 
 	private boolean applyFilter(Pattern filter, String line) {
 		return filter.matcher(line).find();
 	}
 	
 	private boolean applyFilterToGroup(Pattern filter, int startIndex, ArrayList<String> group) {
 		int size = group.size();
 		for(int i = startIndex; i < size; i++) {
 			if(applyFilter(filter, group.get(i))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private Stack<String[]> filterStraceLog(String logFilePath) {
 		try {
 			Stack<String[]> parsedSyscallStack = new Stack<String[]>();
 			BufferedReader br = new BufferedReader(new FileReader(logFilePath));
 			String line;
 			while((line = br.readLine()) != null) {
 				if(applyFilter(failedSyscallFilter, line) && 
 						!applyFilter(localeFilter, line) && !applyFilter(gconvFilter, line)) {
 					parsedSyscallStack.push(parseLine(line));
 				}
 			}
 			br.close();
 			return parsedSyscallStack;
 		} 
 		catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} 
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private String getFullExcutablePath(String executableFile) {
 		String[] path = System.getenv("PATH").split(":");
 		int i = 0;
 		while(i < path.length) {
 			String fullPath = path[i] + "/" + executableFile;
 			if(new File(fullPath).exists()) {
 				return fullPath;
 			}
 			i = i + 1;
 		}
 		return null;
 	}
 
 	private String applyPostProcess(String logPath) {
 		StringBuilder cmdBuilder = new StringBuilder();
 		String shapedLogPath = logPath + "-shaped.log";
 		String scriptPath = getFullExcutablePath("pretty_print_strace_out.py");
 		
 		cmdBuilder.append("python");
 		cmdBuilder.append(" " + scriptPath + " " + logPath + " --trace > ");
 		cmdBuilder.append(shapedLogPath);
 		String[] cmds = {"bash", "-c", cmdBuilder.toString()};
 		try {
 			Process launcher = new ProcessBuilder(cmds).start();
 			launcher.waitFor();
 		} 
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		} 
 		catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 		return shapedLogPath;
 	}
 
 	private Stack<String[]> filterStracePlusLog(String logFilePath) {
 		try {
 			String newLogFilePath = applyPostProcess(logFilePath);
 			Stack<String[]> parsedSyscallStack = new Stack<String[]>();
 			ArrayList<String> syscallGroup = null;
 			ArrayList<ArrayList<String>> syscallGroupList = new ArrayList<ArrayList<String>>();
 			BufferedReader br = new BufferedReader(new FileReader(newLogFilePath));
 			String line;
 			while((line = br.readLine()) != null) {
 				if(applyFilter(syscallFilter, line)) {
 					if(syscallGroup != null) {
 						syscallGroupList.add(syscallGroup);
 						syscallGroup = null;
 					}
 					syscallGroup = new ArrayList<String>();
 					syscallGroup.add(line);
 				}
 				else if(applyFilter(functionFilter, line)) {
 					syscallGroup.add(line);
 				}
 			}
 			if(syscallGroup != null) {
 				syscallGroupList.add(syscallGroup);
 				syscallGroup = null;
 			}
 			br.close();
 			SubProc.deleteLogFile(newLogFilePath);
 			
 			int size = syscallGroupList.size();
 			for(int i = 0; i < size; i++) {
 				ArrayList<String> group = syscallGroupList.get(i);
 				String syscall = group.get(0);
 				if(!applyFilter(failedSyscallFilter, syscall)) {
 					continue;
 				}
 				if(applyFilter(gconvFilter, syscall)) {
 					continue;
 				}
 				if(applyFilter(localeFilter, syscall)) {
 					continue;
 				}
 				if(!applyFilterToGroup(libcStartMainFilter, 1, group)) {
 					continue;
 				}
 				if(applyFilterToGroup(exitFilter, 1, group)) {
 					continue;
 				}
 				if(applyFilterToGroup(dcigettextFilter, 1, group)) {
 					continue;
 				}
 				parsedSyscallStack.push(parseLine(syscall));
 			}
 			return parsedSyscallStack;
 		} 
 		catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} 
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private Stack<String[]> filterTraceLog(String logFilePath) {
 		if(traceBackendType == SubProc.traceBackend_strace) {
 			return filterStraceLog(logFilePath);
 		}
 		else if(traceBackendType == SubProc.traceBackend_strace_plus) {
 			return filterStracePlusLog(logFilePath);
 		}
 		else {
 			throw new RuntimeException("invalid trace backend type");
 		}
 	}
 
 	private String[] parseLine(String syscallLine) {
 		int index = 0;
 		int whiteSpaceCount = 0;
 		int openBracketCount = 0;
 		int closeBracketCount = 0;
 		String[] parsedSyscall = new String[3];
 		String[] parsedSyscallTemp = new String[4];
 		StringBuilder sBuilder= new StringBuilder();
 
 		for(int i = 0; i < syscallLine.length(); i++) {
 			char token = syscallLine.charAt(i);
 			switch(token) {
 			case '(':
 				if(openBracketCount++ == 0) {
 					parsedSyscallTemp[index++] = new String(sBuilder.toString());
 					sBuilder = new StringBuilder();
 				}
 				break;
 			case ')':
 				if(openBracketCount == ++closeBracketCount) {
 					parsedSyscallTemp[index++] = new String(sBuilder.toString());
 					sBuilder = new StringBuilder();
 					openBracketCount = closeBracketCount = 0;
 				}
 				break;
 			default:
 				if(whiteSpaceCount < 2 && token == ' ') {
 					if(i + 1 < syscallLine.length() && syscallLine.charAt(i + 1) != ' ') {
 						whiteSpaceCount++;
 					}
 				} 
 				else {
 					sBuilder.append(token);
 				}
 				break;
 			}
 		}
 		String[] splitStrings = parsedSyscallTemp[2].trim().split(" ");
 		
 		parsedSyscall[0] = parsedSyscallTemp[0];
 		parsedSyscall[1] = parsedSyscallTemp[1];
 		parsedSyscall[2] = splitStrings[2];
 
 		return parsedSyscall;
 	}
 
 	public String[] doInference(String traceLogPath) {
 		Stack<String[]> syscallStack = this.filterTraceLog(traceLogPath);
 		try {
 			return syscallStack.peek();
 		}
 		catch(EmptyStackException e) {
 			return null;
 		}
 	}
 }
 
 class ShellExceptionRaiser {
 	private PseudoProcess[] procs;
 	private boolean enableException;
 
 	public ShellExceptionRaiser(boolean enableException) {
 		this.enableException = enableException;
 	}
 
 	public void setProcesses(PseudoProcess[] kprocs) {
 		this.procs = kprocs;
 	}
 
 	public void raiseException() throws Exception {
 		for(int i = 0; i < this.procs.length; i++) {
 			PseudoProcess targetProc = this.procs[i];
 			targetProc.waitFor();
 			
 			if(!this.enableException) {
 				continue;
 			}
 			String message = targetProc.getCmdName();
 			if(targetProc.isTraced() && targetProc instanceof SubProc) {
 				String logFilePath = ((SubProc)targetProc).getLogFilePath();
 				if(targetProc.getRet() != 0) {
 					CauseInferencer inferencer = new CauseInferencer(SubProc.traceBackendType);
 					String[] inferedSyscall = inferencer.doInference(logFilePath);
 					SubProc.deleteLogFile(logFilePath);
 					throw createException(message, inferedSyscall);
 				}
 				SubProc.deleteLogFile(logFilePath);
 			}
 			else {
 				if(targetProc.getRet() != 0) {
 					throw new DShellException(message);
 				}
 			}
 		}
 	}
 
 	private Exception createException(String message, String[] syscall) throws Exception {
 		// syscall: syscallName: 0, param: 1, errno: 2
 		Class<?>[] types = {String.class, String.class, String[].class};
 		Object[] args = {message, message, syscall};
 		try {
 			if(syscall == null) {
 				return new NotRelatedSyscallException(message);
 			}
 			Class<?> exceptionClass = ErrorToException.valueOf(syscall[2]).toException();
 			if(exceptionClass == null) {
 				return new DShellException(syscall[2] + " has not implemented yet!!");
 			}
 			else {
 				Constructor<?> constructor = exceptionClass.getConstructor(types);
 				return (RelatedSyscallException) constructor.newInstance(args);
 			}
 		}
 		catch (IllegalArgumentException e) {
 			return new Exception((syscall[2] + " is not syscall!!"));
 		}
 	}
 }
 
 enum Syscall {
 	open, openat, connect,
 }
 
 enum ErrorToException {
 	E2BIG {
 		public Class<?> toException() {
 			return TooManyArgsException.class;
 		}
 	}, 
 	EACCES {
 		public Class<?> toException() {
 			return NotPermittedException.class;
 		}
 	}, 
 	EADDRINUSE, 
 	EADDRNOTAVAIL, 
 	EAFNOSUPPORT,
 	EAGAIN {
 		public Class<?> toException() {
 			return TemporaryUnavailableException.class;
 		}
 	}, 
 	EALREADY, 
 	EBADE, 
 	EBADF {
 		public Class<?> toException() {
 			return BadFileDescriptorException.class;
 		}
 	}, 
 	EBADFD {
 		public Class<?> toException() {
 			return BadStateFileDescriptorException.class;
 		}
 	}, 
 	EBADMSG {
 		public Class<?> toException() {
 			return BadMessageException.class;
 		}
 	}, 
 	EBADR, 
 	EBADRQC, 
 	EBADSLT, 
 	EBUSY, 
 	ECANCELED, 
 	ECHILD {
 		public Class<?> toException() {
 			return NoChildException.class;
 		}
 	}, 
 	ECHRNG, 
 	ECOMM, 
 	ECONNABORTED,
 	ECONNREFUSED {
 		public Class<?> toException() {
 			return ConnectionRefusedException.class;
 		}
 	}, 
 	ECONNRESET, 
 	EDEADLK, 
 	EDEADLOCK, 
 	EDESTADDRREQ, 
 	EDOM,
 	EDQUOT, 
 	EEXIST {
 		public Class<?> toException() {
 			return FileExistException.class;
 		}
 	}, 
 	EFAULT, 
 	EFBIG {
 		public Class<?> toException() {
 			return TooLargeFileException.class;
 		}
 	}, 
 	EHOSTDOWN, 
 	EHOSTUNREACH {
 		public Class<?> toException() {
 			return UnreachableHostException.class;
 		}
 	}, 
 	EIDRM, 
 	EILSEQ,
 	EINPROGRESS, 
 	EINTR {
 		public Class<?> toException() {
 			return InterruptedBySignalException.class;
 		}
 	}, 
 	EINVAL {
 		public Class<?> toException() {
 			return InvalidArgumentException.class;
 		}
 	}, 
 	EIO {
 		public Class<?> toException() {
 			return org.GreenTeaScript.DShell.IOException.class;
 		}
 	}, 
 	EISCONN, 
 	EISDIR {
 		public Class<?> toException() {
 			return IsDirectoryException.class;
 		}
 	}, 
 	EISNAM, 
 	EKEYEXPIRED,
 	EKEYREJECTED, 
 	EKEYREVOKED, 
 	EL2HLT, 
 	EL2NSYNC, 
 	EL3HLT, 
 	EL3RST, 
 	ELIBACC, 
 	ELIBBAD, 
 	ELIBMAX, 
 	ELIBSCN, 
 	ELIBEXEC, 
 	ELOOP {
 		public Class<?> toException() {
 			return TooManyLinkException.class;
 		}
 	}, 
 	EMEDIUMTYPE, 
 	EMFILE {
 		public Class<?> toException() {
 			return TooManyFileOpenException.class;
 		}
 	}, 
 	EMLINK, 
 	EMSGSIZE {
 		public Class<?> toException() {
 			return TooLongMessageException.class;
 		}
 	}, 
 	EMULTIHOP, 
 	ENAMETOOLONG {
 		public Class<?> toException() {
 			return TooLongNameException.class;
 		}
 	}, 
 	ENETDOWN, 
 	ENETRESET, 
 	ENETUNREACH {
 		public Class<?> toException() {
 			return UnreachableNetworkException.class;
 		}
 	}, 
 	ENFILE {
 		public Class<?> toException() {
 			return FileTableOverflowException.class;
 		}
 	},
 	ENOBUFS {
 		public Class<?> toException() {
 			return NoBufferSpaceException.class;
 		}
 	}, 
 	ENODATA, 
 	ENODEV {
 		public Class<?> toException() {
 			return DeviceNotFoundException.class;
 		}
 	}, 
 	ENOENT {
 		public Class<?> toException() {
 			return org.GreenTeaScript.DShell.FileNotFoundException.class;
 		}
 	}, 
 	ENOEXEC, 
 	ENOKEY, 
 	ENOLCK, 
 	ENOLINK, 
 	ENOMEDIUM, 
 	ENOMEM {
 		public Class<?> toException() {
 			return NoFreeMemoryException.class;
 		}
 	},
 	ENOMSG, 
 	ENONET, 
 	ENOPKG, 
 	ENOPROTOOPT, 
 	ENOSPC {
 		public Class<?> toException() {
 			return NoFreeSpaceException.class;
 		}
 	}, 
 	ENOSR, 
 	ENOSTR,
 	ENOSYS, 
 	ENOTBLK, 
 	ENOTCONN, 
 	ENOTDIR {
 		public Class<?> toException() {
 			return NotDirectoryException.class;
 		}
 	}, 
 	ENOTEMPTY {
 		public Class<?> toException() {
 			return NotEmptyDirectoryException.class;
 		}
 	}, 
 	ENOTSOCK {
 		public Class<?> toException() {
 			return NotSocketException.class;
 		}
 	}, 
 	ENOTSUP, 
 	ENOTTY {
 		public Class<?> toException() {
 			return InappropriateOperateException.class;
 		}
 	}, 
 	ENOTUNIQ, 
 	ENXIO, 
 	EOPNOTSUPP, 
 	EOVERFLOW, 
 	EPERM{
 		public Class<?> toException() {
 			return NotPermittedOperateException.class;
 		}
 	}, 
 	EPFNOSUPPORT, 
 	EPIPE {
 		public Class<?> toException() {
 			return BrokenPipeException.class;
 		}
 	}, 
 	EPROTO, 
 	EPROTONOSUPPORT, 
 	EPROTOTYPE, 
 	ERANGE, 
 	EREMCHG, 
 	EREMOTE, 
 	EREMOTEIO {
 		public Class<?> toException() {
 			return RemoteIOException.class;
 		}
 	},
 	ERESTART, 
 	EROFS {
 		public Class<?> toException() {
 			return ReadOnlyException.class;
 		}
 	}, 
 	ESHUTDOWN, 
 	ESPIPE {
 		public Class<?> toException() {
 			return IllegalSeekException.class;
 		}
 	}, 
 	ESOCKTNOSUPPORT, 
 	ESRCH, 
 	ESTALE, 
 	ESTRPIPE, 
 	ETIME, 
 	ETIMEDOUT {
 		public Class<?> toException() {
 			return ConnectionTimeoutException.class;
 		}
 	}, 
 	ETXTBSY, 
 	EUCLEAN, 
 	EUNATCH, 
 	EUSERS {
 		public Class<?> toException() {
 			return TooManyUsersException.class;
 		}
 	}, 
 	EWOULDBLOCK {
 		public Class<?> toException() {
 			return EAGAIN.toException();
 		}
 	}, 
 	EXDEV, 
 	EXFULL;
 
 	public Class<?> toException() {
 		return null;
 	}
 }
