 //----------------------------------------------------------------------------
 // $Id$
 //----------------------------------------------------------------------------
 
 package hexgui.htp;
 
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.lang.Thread;
 import java.io.PrintStream;
 
 //----------------------------------------------------------------------------
 
 /** Connects a HTP compatible program to a socket. */
 public class HtpServer
 { 
     public HtpServer(String program, int port) throws Exception
     {
 	System.out.println("program = \"" + program + "\", port = " + port);
 
 	ServerSocket server = new ServerSocket(port);
 	System.out.println("Waiting for connection...");
 	Socket socket = server.accept();
 	System.out.println("htpserver: connected to " 
 			   + socket.getInetAddress());
 
 	System.out.println("Starting program...");
 	Process proc = Runtime.getRuntime().exec(program);
 	System.out.println("Running.");
 
 	Thread here = new Thread(new StreamCopy(proc.getInputStream(),
 						socket.getOutputStream()));
  	Thread there = new Thread(new StreamCopy(socket.getInputStream(), 
  						 proc.getOutputStream()));
  	there.start();
  	here.start();
  	there.join();
  	here.join();
 	server.close();
     }
 
     public static void main(String[] args)
     {
 	if (args.length == 0) {
 	    printUsage();
 	    System.exit(0);
 	}
 
 	try {
 	    new HtpServer(args[0], 20000);
 	}
 
 	catch (Exception e) {
 	    System.out.println("htpserver: error: " + e.getMessage());
 	    System.exit(1);
 	}
     }
 
     public static void printUsage()
     {
 	System.out.println("htpserver");
 	System.out.println("usage:");
 	System.out.println("    java HtpServer program [options]");
     }
 }
 
 //----------------------------------------------------------------------------
