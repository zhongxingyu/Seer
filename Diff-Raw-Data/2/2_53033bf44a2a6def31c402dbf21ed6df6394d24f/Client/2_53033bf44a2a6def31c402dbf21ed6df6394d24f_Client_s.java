 // Copyright (C) 2012 jOVAL.org.  All rights reserved.
 // This software is licensed under the AGPL 3.0 license available at http://www.joval.org/agpl_v3.txt
 
 package jwsmv.winrs;
 
 import java.io.BufferedReader;
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.PasswordAuthentication;
 import java.net.URL;
 import java.util.ArrayList;
 import javax.security.auth.login.FailedLoginException;
 
 import jline.ConsoleReader;
 import jline.Terminal;
 
 import jwsmv.Constants;
 import jwsmv.Shell;
 import jwsmv.wsman.Port;
 
 /**
  * A winrs client.  To use it, you must first do this on the target machine:
  *   winrm quickconfig
  *
  * @author David A. Solin
  * @version %I% %G%
  */
 public class Client implements Constants {
     public static void main(String[] argv) {
 	String host = null;
 	String dir = "%WINDIR%";
 	String user = null;
 	String pass = null;
 	String debugFile = null;
 	boolean encrypt = true;
 	boolean compress = false;
 	boolean echo = true;
 	ArrayList<String> env = null;
 
 	int index = 0;
 	for (; index < argv.length; index++) {
 	    if (!argv[index].startsWith("-") && !argv[index].startsWith("/")) {
 		break;
 	    }
 	    String arg = argv[index].substring(1);
 	    int ptr = arg.indexOf(":");
 	    if (arg.equals("?")) {
 		usage();
 		System.exit(0);
 	    } else if (arg.startsWith("r:") || arg.startsWith("remote:")) {
 		host = arg.substring(ptr+1);
 	    } else if (arg.equals("un") || arg.equals("unencrypted")) {
 		encrypt = false;
 	    } else if (arg.equals("comp") || arg.equals("unencrypted")) {
 		compress = true;
 	    } else if (arg.equals("noe") || arg.equals("noecho")) {
 		echo = false;
 	    } else if (arg.startsWith("u:") || arg.startsWith("username:")) {
 		user = arg.substring(ptr+1);
 	    } else if (arg.startsWith("p:") || arg.startsWith("password:")) {
 		pass = arg.substring(ptr+1);
 	    } else if (arg.startsWith("d:") || arg.startsWith("directory:")) {
 		dir = arg.substring(ptr+1);
 	    } else if (arg.equals("debug") && (index+1) < argv.length) {
 		debugFile = argv[++index];
 	    } else if (arg.startsWith("env:") || arg.startsWith("environment:")) {
 		if (env == null) {
 		    env = new ArrayList<String>();
 		}
 		env.add(arg.substring(ptr+1));
 	    }
 	}
 	StringBuffer sb = new StringBuffer();
 	for (; index < argv.length; index++) {
 	    if (sb.length() > 0) {
 		sb.append(" ");
 	    }
 	    sb.append(argv[index]);
 	}
 	String command = sb.toString();
 
 	if (host == null) {
 	    System.out.println("Target host is missing");
 	    usage();
 	    System.exit(1);
 	} else if (user == null) {
 	    System.out.println("Username is missing");
 	    usage();
 	    System.exit(1);
 	} else if (pass == null) {
 	    System.out.println("Password is missing");
 	    usage();
 	    System.exit(1);
 	} else if (command.length() == 0) {
 	    System.out.println("Command is missing");
 	    usage();
 	    System.exit(1);
 	} else {
 	    String url = null;
 	    if (host.startsWith("http://") || host.startsWith("https://")) {
 		url = host;
 	    } else {
 		sb = new StringBuffer("http://");
 		sb.append(host);
 		if (host.indexOf(":") == -1) {
 		    sb.append(":");
 		    sb.append(Integer.toString(HTTP_PORT));
 		}
 		sb.append("/");
 		sb.append(URL_PREFIX);
 		url = sb.toString();
 	    }
 	    OutputStream debug = null;
 	    try {
		Port port = new Port(url, null, new PasswordAuthentication(user, pass.toCharArray()));
 		if (debugFile != null) {
 		    debug = new FileOutputStream(debugFile);
 		    port.setDebug(debug);
 		}
 		port.setEncryption(encrypt);
 		String[] environment = null;
 		if (env != null) {
 		    environment = env.toArray(new String[env.size()]);
 		}
 
 		Shell shell = new Shell(port, compress, false, null, environment, dir);
 		Process p = shell.exec(command);
 
 		InputStream in = p.getInputStream();
 		StreamCopier errorThread = new StreamCopier(p.getErrorStream(), System.err);
 		errorThread.start();
 
 		ConsoleReader reader = new ConsoleReader();
 		if (echo) {
 		    reader.getTerminal().enableEcho();
 		} else {
 		    reader.getTerminal().disableEcho();
 		}
 		TerminalInput inputThread = new TerminalInput(reader, p.getOutputStream());
 		inputThread.start();
 
 		int ch;
 		while ((ch = in.read()) != -1) {
 		    System.out.write((byte)(ch & 0xFF));
 		    System.out.flush();
 		}
 
 		int exitValue = p.exitValue();
 		errorThread.close();
 		inputThread.close();
 		shell.dispose();
 		System.exit(exitValue);
 	    } catch (FailedLoginException e) {
 		System.out.println("Authentication failed for user " + user);
 		System.exit(0x4DC); // ERROR_NOT_AUTHENTICATED
 	    } catch (Exception e) {
 		e.printStackTrace();
 		System.exit(1);
 	    } finally {
 		if(debug != null) {
 		    try {
 			debug.close();
 		    } catch (IOException e) {
 		    }
 		}
 	    }
 	}
     }
 
     static void usage() {
 	System.out.println("Usage: winrs [-/SWITCH[:VALUE]] COMMAND");
 	System.out.println("  COMMAND - Any string that can be executed as a command in the cmd.exe shell");
 	System.out.println("  SWITCHES:");
 	System.out.println("    -r[emote]:ENDPOINT           - The target endpoint using a DNS name or URL");
 	System.out.println("    -comp[ress]                  - Turn on compression.");
 	System.out.println("    -un[encrypted]               - Specify that messages sent to the target");
 	System.out.println("                                   should not be encrypted");
 	System.out.println("    -u[sername]:USERNAME         - The username for connecting to the target");
 	System.out.println("    -p[assword]:PASSWORD         - The password for the user");
 	System.out.println("    -d[irectory]:PATH            - Set the working directory for the command");
 	System.out.println("    -env[ironment]:STRING=VALUE  - Set an environment variable. Use multiple");
 	System.out.println("                                   times to set multiple variables.");
 	System.out.println("    -noe[cho]                    - Disable echo");
 	System.out.println("    -debug FILENAME              - Write SOAP messages to the specified file.");
 	System.out.println("    -?                           - Print this help message");
     }
 
     /**
      * A simple stream copier that polls the input for available data.
      */
     static class StreamCopier implements Runnable {
 	Thread thread;
 	InputStream in;
 	OutputStream out;
 	boolean open;
 
 	StreamCopier(InputStream in, OutputStream out) {
 	    this.in = in;
 	    this.out = out;
 	    thread = new Thread(this);
 	    open = false;
 	}
 
 	void start() {
 	    open = true;
 	    thread.start();
 	}
 
 	void close() throws IOException {
 	    open = false;
 	}
 
 	public void run() {
 	    try {
 		while(open) {
 		    int len = in.available();
 		    if (len > 0) {
 			byte[] buff = new byte[len];
 			in.read(buff);
 			out.write(buff);
 			out.flush();
 		    } else {
 			Thread.sleep(250);
 		    }
 		}
 	    } catch (InterruptedException e) {
 		e.printStackTrace();
 	    } catch (IOException e) {
 		e.printStackTrace();
 	    }
 	}
     }
 
     /**
      * Uses a JLine ConsoleReader.
      */
     static class TerminalInput implements Runnable {
 	Thread thread;
 	ConsoleReader reader;
 	OutputStream out;
 
 	TerminalInput(ConsoleReader reader, OutputStream out) {
 	    this.reader = reader;
 	    this.out = out;
 	    thread = new Thread(this);
 	}
 
 	void start() {
 	    thread.start();
 	}
 
 	void close() throws IOException {
 	    thread.interrupt();
 	}
 
 	public void run() {
 	    try {
 		String line = null;
 		while((line = reader.readLine()) != null) {
 		    out.write(line.getBytes());
 		    out.write("\r\n".getBytes());
 		    out.flush();
 		}
 	    } catch (IOException e) {
 		e.printStackTrace();
 	    }
 	}
     }
 }
