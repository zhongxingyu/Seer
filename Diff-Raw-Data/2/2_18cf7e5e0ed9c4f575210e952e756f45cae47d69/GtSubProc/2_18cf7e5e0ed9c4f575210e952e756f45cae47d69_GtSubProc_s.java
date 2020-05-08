 package org.GreenTeaScript.JVM;
 
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
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.GreenTeaScript.LibGreenTea;
 
 public class GtSubProc {
 	// option index
 	private static final int isExpr = 0;
 	private static final int isThrowable = 1;
 	private static final int isBackGround = 2;
 	private static final int enableTrace = 3;
 	
 	private static final int optionSize = 4;
 	
 	
 	// called by VisitCommandNode at JavaByteCodeGenerator 
 	public static String ExecCommandString(String[]... cmds) throws Exception {
 		boolean[] option = {true, true, false, true};
 		return createSubProc(cmds, option).str;
 	}
 
 	public static boolean ExecCommandBool(String[]... cmds) throws Exception {
 		boolean[] option = {true, false, false, false};
 		return createSubProc(cmds, option).bool;
 	}
 
 	public static void ExecCommandVoid(String[]... cmds) throws Exception {
 		boolean[] option = {false, true, false, true};
 		createSubProc(cmds, option);
 	}
 	//---------------------------------------------
 
 	private static boolean checkTraceRequirements() {
 		if(System.getProperty("os.name").equals("Linux")) {
 			return LibGreenTea.IsUnixCommand("strace");
 		}
 		return false;
 	}
 	
 	private static RetPair createSubProc(String[][] cmds, boolean[] option) throws Exception {
 		int size = cmds.length;
 		String stdout = "";
 		boolean ret = false;
 		
 		if(option[enableTrace]) {
 			option[enableTrace] = checkTraceRequirements();
 		}
 		
 		ProcessMonitor monitor = new ProcessMonitor(option[isThrowable]);
 		SubProc[] subProcs = new SubProc[size];
 		for(int i = 0; i < size; i++) {
 			subProcs[i] = new SubProc(option[enableTrace]);
 			subProcs[i].setArgument(cmds[i]);
 			monitor.setProcess(subProcs[i]);
 		}
 		
 		for(int i = 0; i < size; i++) { 
 			subProcs[i].start();
 			if(i == 0) {
 				// TODO: input redirect
 			}
 			else {
				subProcs[i - i].pipe(subProcs[i]);
 			}
 			
 			if(i == size - 1) {
 				// TODO: output redirect
 				if(option[isExpr]) {
 					subProcs[i].waitResult();
 				}
 				else {
 					subProcs[i].console();
 				}
 			}
 		}
 		monitor.throwException();
 		int lastIndex = size - 1;
 		if(option[isExpr]) {
 			stdout = subProcs[lastIndex].getStdout();
 			ret = (subProcs[lastIndex].getRet() == 0);
 		}
 		return new RetPair(stdout, ret);
 	}
 }
 
 class RetPair {
 	public String str;
 	public boolean bool;
 
 	public RetPair(String str, boolean bool) {
 		this.str = str;
 		this.bool = bool;
 	}
 }
 
 class SubProc {
 	private static int logId = 0;
 	private Process proc;
 
 	private OutputStream stdin = null;
 	private InputStream stdout = null;
 	private InputStream stderr = null;
 
 	private StringBuilder cmdNameBuilder;
 	private ArrayList<String> commandList;
 	private boolean enableSyscallTrace = false;
 	public boolean isKilled = false;
 	private final String logdirPath = "/tmp/strace-log";
 	public String logFilePath = null;
 	private int retValue = -1;
 
 	private boolean stdoutIsRedireted = false;
 	private boolean stderrIsRedireted = false;
 	private boolean streamIsLocked = false;
 
 	private ByteArrayOutputStream outBuf;
 	private ByteArrayOutputStream errBuf;
 
 	public SubProc() {
 		this.cmdNameBuilder = new StringBuilder();
 		this.commandList = new ArrayList<String>();
 		
 		initTrace();
 	}
 
 	public SubProc(boolean enableSyscallTrace) {
 		this.cmdNameBuilder = new StringBuilder();
 		this.commandList = new ArrayList<String>();
 		this.enableSyscallTrace = enableSyscallTrace;
 		
 		initTrace();
 	}
 
 	private void initTrace() {
 		if(this.enableSyscallTrace) {
 			String currentLogdirPath = createLogDirectory();
 			String logNameHeader = createLogNameHeader();
 			logFilePath = new String(currentLogdirPath + "/" + logNameHeader + ".log");
 			
 			String[] straceCmd = {"strace", "-t", "-f", "-F", "-o", logFilePath};
 			for(int i = 0; i < straceCmd.length; i++) {
 				this.commandList.add(straceCmd[i]);
 			}
 		}
 	}
 
 	private String createLogDirectory() {
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
 
 	private String createLogNameHeader() {
 		Calendar cal = Calendar.getInstance();
 		StringBuilder logNameHeader = new StringBuilder();
 		
 		logNameHeader.append(cal.get((Calendar.HOUR) + 1) + ":");
 		logNameHeader.append(cal.get(Calendar.MINUTE) + "-");
 		logNameHeader.append(cal.get(Calendar.MILLISECOND));
 		logNameHeader.append("-" + logId++);
 
 		return logNameHeader.toString();
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
 
 	public void start() {
 		int size = this.commandList.size();
 		String[] cmd = new String[size];
 		for(int i = 0; i < size; i++) {
 			cmd[i] = this.commandList.get(i);
 		}
 
 		try {
 			this.proc = new ProcessBuilder(cmd).start();
 			this.stdin = this.proc.getOutputStream();
 			this.stdout = this.proc.getInputStream();
 			this.stderr = this.proc.getErrorStream();
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void pipe(SubProc destProc) {
 		new PipeStreamHandler(this.stdout, destProc.stdin, true).start();
 	}
 
 	public void readFromFile(String fileName) {
 		try {
 			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
 			new PipeStreamHandler(bis, this.stdin, true).start();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void writeToFile(String fileName) {
 		try {
 			this.stdoutIsRedireted = true;
 			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
 			new PipeStreamHandler(this.stdout, bos, true).start();
 		} 
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void handleOutputStream(OutputStream out, OutputStream err, boolean closeStream) {
 		if(streamIsLocked) {
 			return;
 		}
 		streamIsLocked = true;
 		
 		PipeStreamHandler stdoutHandler = null;
 		PipeStreamHandler stderrHandler = null;
 		if(!stdoutIsRedireted) {
 			stdoutHandler = new PipeStreamHandler(stdout, out, closeStream);
 			stdoutHandler.start();
 		}
 		
 		if(!stderrIsRedireted) {
 			stderrHandler = new PipeStreamHandler(stderr, err, closeStream);
 			stderrHandler.start();
 		}
 
 		try {
 			if(stdoutHandler != null) {
 				stdoutHandler.join();
 			}
 			if(stderrHandler != null) {
 				stderrHandler.join();
 			}
 		}
 		catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void waitResult() {
 		outBuf = new ByteArrayOutputStream();
 		errBuf = new ByteArrayOutputStream();
 		
 		handleOutputStream(outBuf, errBuf, true);
 	}
 
 	public String getStdout() {
 		return this.stdoutIsRedireted ? "" : this.outBuf.toString();
 	}
 
 	public String getStderr() {
 		return this.stderrIsRedireted ? "" : this.errBuf.toString();
 	}
 
 	public void console() {
 		handleOutputStream(System.out, System.err, false);
 	}
 
 	public void waitFor() {
 		try {
 			this.retValue = this.proc.waitFor();
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void waitFor(long timeout) {
 		try {
 			this.proc.exitValue();
 		}
 		catch (IllegalThreadStateException e) {
 			try {
 				Thread.sleep(timeout);
 			}
 			catch (InterruptedException e1) {
 				e1.printStackTrace();
 			} 
 			finally {
 				this.kill();
 			}
 		}
 	}
 
 	public int getRet() {
 		return this.retValue;
 	}
 
 	public void kill() {
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
 
 	public String getLogFilePath() {
 		return this.logFilePath;
 	}
 
 	public String getCmdName() {
 		return this.cmdNameBuilder.toString();
 	}
 	
 	public boolean isTraced() {
 		return this.enableSyscallTrace;
 	}
 }
 
 // copied from http://blog.art-of-coding.eu/piping-between-processes/
 class PipeStreamHandler extends Thread {
 	private InputStream input;
 	private OutputStream output;
 	private boolean closeStream;
 
 	public PipeStreamHandler(InputStream input, OutputStream output, boolean closeStream) {
 		this.input = input;
 		this.output = output;
 		this.closeStream = closeStream;
 	}
 
 	@Override public void run() {
 		try {
 			byte[] buffer = new byte[512];
 			int read = 0;
 			while(read > -1) {
 				read = this.input.read(buffer, 0, buffer.length);
 				if(read > -1) {
 					this.output.write(buffer, 0, read);
 				}
 			}
 			if(closeStream) {
 				this.input.close();
 				this.output.close();
 			}
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
 
 class ProcessMonitor {
 	private ArrayList<SubProc> procList;
 	private boolean enableException;
 	
 	public ProcessMonitor(boolean enableException) {
 		this.enableException = enableException;
 		this.procList = new ArrayList<SubProc>();
 	}
 
 	public void setProcess(SubProc kproc) {
 		this.procList.add(kproc);
 	}
 
 	public void throwException() throws Exception {
 		int size = procList.size();
 		for(int i = 0; i < size; i++) {
 			SubProc targetProc = procList.get(i);
 			targetProc.waitFor();
 			
 			if(!this.enableException) {
 				continue;
 			}
 			
 			// throw exception
 			String message = targetProc.getCmdName();
 			if(targetProc.isTraced()) {	// infer systemcall error
 				String logFilePath = targetProc.getLogFilePath();
 				if(targetProc.getRet() != 0) {
 					Stack<String[]> syscallStack = parseTraceLog(logFilePath);
 					deleteLogFile(logFilePath);
 					throw createException(message, syscallStack.peek());
 				}
 				deleteLogFile(logFilePath);
 			}
 			else {
 				if(targetProc.getRet() != 0) {
 					throw new Exception(message);
 				}
 			}
 		}
 	}
 
 	private void deleteLogFile(String logFilePath) {
 		new File(logFilePath).delete();
 	}
 
 	private Stack<String[]> parseTraceLog(String logFilePath) {
 		try {
 			Stack<String[]> syscallStack = new Stack<String[]>();
 			BufferedReader br = new BufferedReader(new FileReader(logFilePath));
 			
 			String regex1 = "^[1-9][0-9]* .+(.+) *= *-[1-9].+";
 			Pattern p1 = Pattern.compile(regex1);
 			
 			String regex2 = "^.+(.+/locale.+).+";
 			Pattern p2 = Pattern.compile(regex2);
 			
 			String regex3 = "(^[1-9][0-9]*)( *)([0-9][0-9]:[0-9][0-9]:[0-9][0-9])( *)(.+)";
 			Pattern p3 = Pattern.compile(regex3);
 			
 			Matcher m1, m2, m3;
 			
 			String line;
 			while((line = br.readLine()) != null) {
 				m1 = p1.matcher(line);
 				m2 = p2.matcher(line);
 				if(m1.find() && !m2.find()) {
 					m3 = p3.matcher(line);
 					syscallStack.push(parseLine(m3.replaceAll("$5")));
 				}
 			}
 			br.close();
 			
 			return syscallStack;
 		} 
 		catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} 
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private String[] parseLine(String syscallLine) {
 		int p = 0;
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
 					parsedSyscallTemp[p++] = new String(sBuilder.toString());
 					sBuilder = new StringBuilder();
 				}
 				break;
 			case ')':
 				if(openBracketCount == ++closeBracketCount) {
 					parsedSyscallTemp[p++] = new String(sBuilder.toString());
 					sBuilder = new StringBuilder();
 					openBracketCount = closeBracketCount = 0;
 				}
 				break;
 			default:
 				sBuilder.append(token);
 				break;
 			}
 		}
 		String[] splitStrings = parsedSyscallTemp[2].trim().split(" ");
 		
 		parsedSyscall[0] = parsedSyscallTemp[0];
 		parsedSyscall[1] = parsedSyscallTemp[1];
 		parsedSyscall[2] = splitStrings[2];
 		
 		// print 
 //		System.out.print(syscallLine + ": ");
 //		for(int i = 0; i < parsedSyscall.length; i++) {
 //			System.out.print(parsedSyscall[i] + " ");
 //		}
 //		System.out.println();
 		return parsedSyscall;
 	}
 
 	private Exception createException(String message, String[] syscall) throws Exception {
 		try {
 			return ErrNo.valueOf(syscall[2]).toException(message, syscall[0], syscall[1]);
 		}
 		catch (IllegalArgumentException e) {
 			return new Exception((syscall[2] + " is not syscall!!"));
 		}
 	}
 }
 
 // shell Exception definition
 class NotPermittedException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public NotPermittedException(String message) {
 		super(message);
 	}
 }
 
 class TooManyLinkException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public TooManyLinkException(String message) {
 		super(message);
 	}
 }
 
 class TooLongNameException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public TooLongNameException(String message) {
 		super(message);
 	}
 }
 
 class NotFoundException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public NotFoundException(String message) {
 		super(message);
 	}
 }
 
 class NetworkTimeoutException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public NetworkTimeoutException(String message) {
 		super(message);
 	}
 }
 
 class InterruptedBySignalException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public InterruptedBySignalException(String message) {
 		super(message);
 	}
 }
 
 class UnreachableException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public UnreachableException(String message) {
 		super(message);
 	}
 }
 
 class ConnectRefusedException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public ConnectRefusedException(String message) {
 		super(message);
 	}
 }
 
 class NoFreeSpaceException	extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public NoFreeSpaceException(String message) {
 		super(message);
 	}
 }
 
 class ReadOnlyException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public ReadOnlyException(String message) {
 		super(message);
 	}
 }
 
 class NoFreeMemoryException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public NoFreeMemoryException(String message) {
 		super(message);
 	}
 }
 
 class IllegalSeekException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public IllegalSeekException(String message) {
 		super(message);
 	}
 }
 
 class NotPermittedOprateException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public NotPermittedOprateException(String message) {
 		super(message);
 	}
 }
 
 class NotDirectoryException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public NotDirectoryException(String message) {
 		super(message);
 	}
 }
 
 enum Syscall {
 	open, openat, connect,
 }
 
 enum ErrNo {
 	E2BIG, 
 	EACCES {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NotPermittedException(message);
 		}
 	}, 
 	EADDRINUSE, 
 	EADDRNOTAVAIL, 
 	EAFNOSUPPORT,
 	EAGAIN, 
 	EALREADY, 
 	EBADE, 
 	EBADF, 
 	EBADFD, 
 	EBADMSG, 
 	EBADR, 
 	EBADRQC, 
 	EBADSLT, 
 	EBUSY, 
 	ECANCELED, 
 	ECHILD, 
 	ECHRNG, 
 	ECOMM, 
 	ECONNABORTED,
 	ECONNREFUSED {
 		public Exception toException(String message, String syscallName, String param) {
 			return new ConnectRefusedException(message);
 		}
 	}, 
 	ECONNRESET, 
 	EDEADLK, 
 	EDEADLOCK, 
 	EDESTADDRREQ, 
 	EDOM,
 	EDQUOT, 
 	EEXIST, 
 	EFAULT, 
 	EFBIG, 
 	EHOSTDOWN, 
 	EHOSTUNREACH, 
 	EIDRM, 
 	EILSEQ,
 	EINPROGRESS, 
 	EINTR {
 		public Exception toException(String message, String syscallName, String param) {
 			return new InterruptedBySignalException(message);
 		}
 	}, 
 	EINVAL, 
 	EIO, 
 	EISCONN, 
 	EISDIR, 
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
 		public Exception toException(String message, String syscallName, String param) {
 			return new TooManyLinkException(message);
 		}
 	}, 
 	EMEDIUMTYPE, 
 	EMFILE, 
 	EMLINK, 
 	EMSGSIZE, 
 	EMULTIHOP, 
 	ENAMETOOLONG {
 		public Exception toException(String message, String syscallName, String param) {
 			return new TooLongNameException(message);
 		}
 	}, 
 	ENETDOWN, 
 	ENETRESET, 
 	ENETUNREACH {
 		public Exception toException(String message, String syscallName, String param) {
 			return new UnreachableException(message);
 		}
 	}, 
 	ENFILE,
 	ENOBUFS, 
 	ENODATA, 
 	ENODEV, 
 	ENOENT {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NotFoundException(message);
 		}
 	}, 
 	ENOEXEC, 
 	ENOKEY, 
 	ENOLCK, 
 	ENOLINK, 
 	ENOMEDIUM, 
 	ENOMEM {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NoFreeMemoryException(message);
 		}
 	},
 	ENOMSG, 
 	ENONET, 
 	ENOPKG, 
 	ENOPROTOOPT, 
 	ENOSPC {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NoFreeSpaceException(message);
 		}
 	}, 
 	ENOSR, 
 	ENOSTR,
 	ENOSYS, 
 	ENOTBLK, 
 	ENOTCONN, 
 	ENOTDIR{
 		public Exception toException(String message, String syscallName, String param) {
 			return new NotDirectoryException(message);
 		}
 	}, 
 	ENOTEMPTY, 
 	ENOTSOCK, 
 	ENOTSUP, 
 	ENOTTY, 
 	ENOTUNIQ, 
 	ENXIO, 
 	EOPNOTSUPP, 
 	EOVERFLOW, 
 	EPERM{
 		public Exception toException(String message, String syscallName, String param) {
 			return new NotPermittedOprateException(message);
 		}
 	}, 
 	EPFNOSUPPORT, 
 	EPIPE, 
 	EPROTO, 
 	EPROTONOSUPPORT, 
 	EPROTOTYPE, 
 	ERANGE, 
 	EREMCHG, 
 	EREMOTE, 
 	EREMOTEIO,
 	ERESTART, 
 	EROFS {
 		public Exception toException(String message, String syscallName, String param) {
 			return new ReadOnlyException(message);
 		}
 	}, 
 	ESHUTDOWN, 
 	ESPIPE {
 		public Exception toException(String message, String syscallName, String param) {
 			return new IllegalSeekException(message);
 		}
 	}, 
 	ESOCKTNOSUPPORT, 
 	ESRCH, 
 	ESTALE, 
 	ESTRPIPE, 
 	ETIME, 
 	ETIMEDOUT {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NetworkTimeoutException(message);
 		}
 	}, 
 	ETXTBSY, 
 	EUCLEAN, 
 	EUNATCH, 
 	EUSERS, 
 	EWOULDBLOCK, 
 	EXDEV, 
 	EXFULL;
 
 	public Exception toException(String message, String syscallName, String param) {
 		return new Exception(this.toString() + " is not yet implemented!!");
 	}
 }
