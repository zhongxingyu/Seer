 package p2pChat;
 
 import java.io.*;
 import java.net.*;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class Peer {
	public static final boolean debug = true;
 	protected static boolean quit = false;
 	protected static boolean prevDone = false;
 	protected static boolean hold = false;
 	protected static LinkedBlockingQueue<Socket> socketQueue = 
 		new LinkedBlockingQueue<Socket>();
 	protected static LinkedBlockingQueue<String> chatQueue = 
 		new LinkedBlockingQueue<String>();
 	protected static LinkedBlockingQueue<String> reconnectQueue = 
 		new LinkedBlockingQueue<String>();
 	protected static Socket prev;
 	protected static BufferedReader prevIn;
 	protected static PrintStream prevOut;
 	protected static Socket next;
 	protected static BufferedReader nextIn;
 	protected static PrintStream nextOut;
 	protected static String myIP;
 	protected static int serverPort = 4242;
 	protected static ServerSocket ss;
 
 	/*
 	 * synchronized getter and setter for hold
 	 */
 	protected static synchronized void setHold(boolean value) {
 		hold = value; 
 	}
 	protected static synchronized boolean getHold() {
 		return hold;
 	}
 	
 	/*
 	 * synchronized getter and setter for quit
 	 */
 	protected static synchronized void setQuit(boolean value) {
 		quit = value; 
 	}
 	protected static synchronized boolean getQuit() {
 		return quit;
 	}
 
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unused")
 	public static void main(String[] args) throws Exception {
 		//initialize class variables
 		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
 		
     	try {
 			myIP = InetAddress.getLocalHost().getHostAddress();
 		} catch (UnknownHostException e) {
 			//can't get own IP for some reason???
 			System.out.println("Something unexpected went wrong. Please try" +
 					" again. (UnknownHostException)");
 			System.exit(1);
 		}
 		
 		ss = new ServerSocket(serverPort);
 		
 
 		System.out.println("***********************************************");
 		System.out.println("*            Welcome to P2P Chat!             *");
 		System.out.println("*                                             *");
 		System.out.println("* by Jacob Williams, Michael McCormick, Chad  *");
 		System.out.println("*         Ellsworth and Joshua Conner         *");
 		System.out.println("*                                             *");
 		System.out.println("*       CS 499/565: Distributed Systems       *");
 		System.out.println("*                 Fall 2011                   *");
 		System.out.println("***********************************************\n");
 		
 		//if no arguments, ask for IP to connect to from stdIn
 		String input = null; 
 		if (args.length  == 0) {
 			System.out.print("Enter the IP address to connect to, or press " +
 					"<Enter> to start a new chat node: ");
 		
 			if ((input = stdIn.readLine()) == null) 
 			{
 				next = new Socket(myIP, serverPort);
 			}
 			else
 			{
 				next = new Socket(input, serverPort);
 			}
 		//if there is an argument, attempt to connect to chat node at that IP
 		} else if (args.length  == 1) {
 			next = new Socket(InetAddress.getByName(args[0]), serverPort);
 		//error if too many args
 		} else {
 			System.out.println("You entered the too many arguments." +
 					" Please try again!");
 			System.out.println("Usage: peer [ipAddress]");
 			System.exit(0);
 		}
 		
 		//set up original sockets and their streams
 		prev = ss.accept();
 		nextIn = new BufferedReader(new InputStreamReader(next.getInputStream()));
 		nextOut = new PrintStream(next.getOutputStream(), true);
 		prevIn = new BufferedReader(new InputStreamReader(prev.getInputStream()));
 		prevOut = new PrintStream(prev.getOutputStream(), true);
 		
 		//set up Threads to handle each stream, and also new connections
 		(new Thread(new NextInput(), "NextIn")).start();
 		(new Thread(new NextOutput(), "NextOut")).start();
 		(new Thread(new PrevOutput(), "PrevOut")).start();
 		(new Thread(new ConnectionHandler(), "ConnectionHandler")).start();
 		(new Thread(new PrevInput(), "PrevIn")).start();
 	
 		
 		//display system message notifying you that you are listening or
 		//connected to a chat node, whichever is the case
 		if(input != null && (input.length() == 0 && args.length == 0)) 
 		{
 			System.out.println("    [Now listening at IP " + myIP + "]");
 		}
 		else
 		{
 			System.out.println("    [Now connected to chat node at IP " + 
 					next.getInetAddress().getHostAddress() + ".]");
 			chatQueue.add("    [" + myIP + " has joined the chat.]");
 		}
 		
 		/*
 		 * begin main execution loop
 		 */
 		String userInput;
 		try {
 
 			MAINLOOP:while (!getQuit()) {
 				if ((userInput = stdIn.readLine()) != null) {
 				    //if "quit" start quit process
 					if (userInput.toLowerCase().equals("quit"))
 				    {
 					    	setQuit(true);
 					    	break MAINLOOP;
 				    }
 					//debug code
 				    else if (Peer.debug && userInput.toLowerCase().equals("debug"))
 				    {
 				    		System.out.println("PREV: " + prev.toString());
 				    		System.out.println("NEXT: " + next.toString());
 				    		System.out.println("SocketQueue empty? " + socketQueue.isEmpty());
 				    		System.out.println("ChatQueue empty? " + chatQueue.isEmpty());
 				    		System.out.println("reconnectQueue empty? " + reconnectQueue.isEmpty());
 				    		System.out.println("Hold = " + getHold() + ", prevDone = " + prevDone + ", quit = " + getQuit());
 				    }
 					//more debug code; allows you to send messages backwards
 				    else if (Peer.debug && userInput.toLowerCase().startsWith("housekeeping"))
 				    {
 				    	if(Peer.debug)
 				    	System.out.println("Sending housekeeping: " + userInput.substring(13));	
 				    	Peer.prevOut.println(userInput.substring(13));
 				    }
 					//else add message to chat queue
 				    else
 				    {
 				        Peer.chatQueue.add(Peer.myIP + ": " + userInput);
 				    }
 				}
 			}
 		} catch (IOException e) {
 			// something went wrong with stdIn.readLine() or socket streams
 			System.out.println("Something unexpected went wrong. Please try" +
 					"again.");
 			System.exit(1);
 		}
 		
 		//i.e. if you're not the only node left, do the following
 		if(!next.getInetAddress().getHostAddress().equals(myIP) && 
 				!prev.getInetAddress().getHostAddress().equals(myIP))
 		{
 			//send chat-leave message
 			Peer.chatQueue.add("    [" + myIP + " has left the chat.]");
 			
 			//empty the socket queue
 			while(!socketQueue.isEmpty())
 			{
 			    Socket s = socketQueue.remove();
 			    PrevOutput.sendReconnect(s);
 			}
 			
 			/*
 			 * Final reconnect; connects your prev and your next
 			 */
 	    	prevOut.println("Hold");
 		    
 		    while(!chatQueue.isEmpty())
 		    {
 		    	nextOut.println(chatQueue.remove());
 		    }
 		    
 		    prevOut.println(next.getInetAddress().getHostAddress());
 		}
 		
 		//cleanup
 	    prevOut.close();
 	    prevIn.close();
 	    prev.close();
 
 	    nextOut.close();
 	    nextIn.close();
 	    next.close();  
 	    
 	    ss.close();
 	}
 }
