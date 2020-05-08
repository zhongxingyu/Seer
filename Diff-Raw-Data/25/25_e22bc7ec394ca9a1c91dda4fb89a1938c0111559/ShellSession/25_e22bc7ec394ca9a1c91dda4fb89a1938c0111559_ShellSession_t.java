 package com.buglabs.bug.base;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Writer;
 
 import com.buglabs.bug.base.pub.ICommandResponseHandler;
 import com.buglabs.bug.base.pub.IShellSession;
 import com.buglabs.util.StringUtil;
 
 /**
  * Default implementation of ShellSession.
  * 
 * @deprecated This has moved to com.buglabs.util.shell.
  * @author kgilmer
  */
 public class ShellSession implements IShellSession {
 	private volatile boolean interrupt = false;
 	private boolean disposed = false;
 	/**
 	 * This terminator string is used to determine when content from the output
 	 * stream is completed for a given command.
 	 */
 	public static final String TERMINATOR = "234o987dsfkcqiuwey18837032843259d";
 	public static final String LT = System.getProperty("line.separator");
 
 	public static String getFilePath(String file) throws IOException {
 		File f = new File(file);
 
 		if (!f.exists() || f.isDirectory()) {
 			throw new IOException("Path passed is not a file: " + file);
 		}
 
 		StringBuffer sb = new StringBuffer();
 
 		String elems[] = StringUtil.split(file, File.separator);
 
 		for (int i = 0; i < elems.length - 1; ++i) {
 			sb.append(elems[i]);
 			sb.append(File.separator);
 		}
 
 		return sb.toString();
 	}
 
 	private Process process;
 
 	private OutputStream pos = null;
 	private static final String SHELL_PATH = "/bin/sh";
 	private final File root;
 	private final Writer out;
 
 	public ShellSession(File root, Writer out) throws IOException {
 		this.root = root;
 
 		if (out == null) {
 			this.out = new NullWriter();
 		} else {
 			this.out = out;
 		}
 
 		initializeShell();
 	}
 
 	private void initializeShell() throws IOException {
 		if (!disposed) {
 			process = Runtime.getRuntime().exec(SHELL_PATH);
 			pos = process.getOutputStream();
 
 			if (root != null) {
 				out.write(execute("cd " + root.getAbsolutePath()));
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see shellservice.IShellService#execute(java.lang.String)
 	 */
 	synchronized public String execute(String command) throws IOException {
 		if (disposed) {
 			throw new IOException("This shell session has been disposed.");
 		}
 		
 		String errorMessage = null;
 		interrupt = false;
 		out.write(command);
 
 		sendToProcessAndTerminate(command);
 
 		if (process.getErrorStream().available() > 0) {
 			byte[] msg = new byte[process.getErrorStream().available()];
 
 			process.getErrorStream().read(msg, 0, msg.length);
 			out.write(new String(msg));
 			errorMessage = "Error while executing: " + command + LT + new String(msg);
 		}
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
 
 		StringBuffer sb = new StringBuffer();
 		String line = null;
 
 		while (((line = br.readLine()) != null) && !line.equals(TERMINATOR) && !interrupt) {
 			sb.append(line);
 			sb.append(LT);
 			out.write(line);
 			out.write(LT);
 		}
 
 		if (interrupt) {
 			process.destroy();
 			initializeShell();
 			interrupt = false;
 		}
 
 		if (errorMessage != null) {
 			throw new IOException(errorMessage);
 		}
 
 		return sb.toString();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see shellservice.IShellService#execute(java.lang.String,
 	 * shell.pub.ICommandResponseHandler)
 	 */
 	synchronized public void execute(String command, ICommandResponseHandler handler) throws IOException {
 		execute(command, TERMINATOR, handler);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see shellservice.IShellService#execute(java.lang.String,
 	 * java.lang.String, shell.pub.ICommandResponseHandler)
 	 */
 	synchronized public void execute(String command, String terminator, ICommandResponseHandler handler) throws IOException {
 		if (disposed) {
 			throw new IOException("This shell session has been disposed.");
 		}
 		
 		interrupt = false;
 		sendToProcessAndTerminate(command);
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
 		InputStream errIs = process.getErrorStream();
 		String std = null;
 
 		do {
 			if (errIs.available() > 0) {
 				byte[] msg = new byte[errIs.available()];
 
 				errIs.read(msg, 0, msg.length);
 				out.write(new String(msg));
 				handler.response(new String(msg), true);
 			}
 
 			std = br.readLine();
 
 			if (std != null && !std.equals(terminator)) {
 				out.write(std);
 				handler.response(std, false);
 			}
 
 		} while (std != null && !std.equals(terminator) && !interrupt);
 
 		if (interrupt) {
 			process.destroy();
 			initializeShell();
 			interrupt = false;
 		}
 	}
 
 	/**
 	 * Send command string to shell process and add special terminator string so
 	 * reader knows when output is complete.
 	 * 
 	 * @param command
 	 * @throws IOException
 	 */
 	private void sendToProcessAndTerminate(String command) throws IOException {
 		pos.write(command.getBytes());
 		pos.write(LT.getBytes());
 		pos.write("echo ".getBytes());
 		pos.write(TERMINATOR.getBytes());
 		pos.write(LT.getBytes());
 		pos.flush();
 	}
 
 	public void interrupt() {
 		interrupt = true;
 	}
 
 	public void dispose() {
 		this.disposed = true;
 		interrupt();
 	}
 
 	/**
 	 * A Writer that does nothing.
 	 * 
 	 * @author kgilmer
 	 */
 	private class NullWriter extends Writer {
 		public void close() throws IOException {
 		}
 
 		public void flush() throws IOException {
 		}
 
 		public void write(char[] cbuf, int off, int len) throws IOException {
 		}
 	}
 }
