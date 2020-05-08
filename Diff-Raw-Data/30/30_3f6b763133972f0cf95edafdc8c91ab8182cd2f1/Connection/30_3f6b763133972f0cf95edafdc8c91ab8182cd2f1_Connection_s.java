 package irc;
 
 import java.net.Socket;
 import java.io.PrintWriter;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import java.util.concurrent.PriorityBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 import java.util.Iterator;
 import java.util.List;
 
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 /**
  */
 public class Connection {
 
 	public enum State {
 		DISCONNECTED(0),
 		CONNECTED(1),
 		REGISTERED(2);
 
 		private int code;
 
 		State(int code) {
 			this.code = code;
 		}
 	}
 
 	//minimum time between pings = 30 seconds...
 	private static final int PING_TIMEOUT = 30000;
 
 	//max idle of 1.5 minutes...
 	private static final int MAX_IDLE = 1000*60*3;
 
 
 	/**
 	 * Server for the IRC connection
 	 */
 	private String host;
 
 	/**
 	 * Port of the IRCd on host.
 	 */
 	private int port;
 
 	//nickname, username, realname.
 	private String nick,user,real;
 
 	//queue of messages to send.
 	private BlockingQueue<OutgoingMessage> sendQ;
 	
 	//queue of messages received.
 	private BlockingQueue<Message> recvQ;
 
 	//Irc connection
 	private IrcConnection conn;
 
 	//message handlers (subscribers)
 	private List<IrcMessageSubscription> handlers = new util.LinkedList<IrcMessageSubscription>();
 	
 	//whether or not registration phase of connection is complete
 	//(no non-registration commands can be sent until this is done)
 	private State state;
 
 	//how the server identifies itself in the 001
 	private String hostname = "none";
 
 	private long last_tx = 0;
 
 	private long last_rx = 0;
 
 	private long last_ping = 0;
 
 
 	public Connection(String host, int port, String nick) {
 		this(host,port,nick,nick);
 	}
 
 	public Connection(String host, int port, String nick, String user) {
 		this(host,port,nick,user,nick);
 	}
 
 	public Connection(String host, int port, String nick, String user, String real) {
 		this.user = user;
 		this.host = host;
 		this.port = port;
 		this.nick = nick;
 		this.user = user;
 		this.real = real;
 
 		state = State.DISCONNECTED;
 	}
 
 	private void setState(State ns) {
 		state = ns;
 
 		synchronized(this) {
 			notifyAll();
 		}
 	}
 
 	//attempt to connect
 	public void connect() throws java.io.IOException {
 
 		//will replace these with own implementation of queue/prioirty queue later.
 		sendQ = new PriorityBlockingQueue<OutgoingMessage>();
 		recvQ = new PriorityBlockingQueue<Message>();
 
 
 		//connect
 		//(blocks until connected)
 		conn = new IrcConnection();
 
 		//handle messages in a separate thread
 		//so the socket I/O never pauses
 		//(This can be important in the case of a flood of messages which require
 		//some potentially long, synchonous operation to handle. In this case,
 		//a PING could be left unread which would cause a pint timeout.
 		//)
 		(new Thread( new Worker(), "Message Handler" )).start();
 	
 		addMessageHandler(this.internalHandler)
 			.addType( MessageType.PING )
 			.or()
 			.addCode( MessageCode.RPL_WELCOME )

 		;
 
 		//@TODO
 		if ( state != State.CONNECTED )
 			throw new RuntimeException("Connection is not connected!");
 
 		//initiatite registration
 		register();
 
 		try {
 
 			//wait until this changes its state..
 			synchronized(this) {
 				this.wait(30000); 
 			}
 
 		} catch (InterruptedException e) {
 		}
 
 		if ( state != state.REGISTERED )
 			throw new RuntimeException("REGISTER timeout after 30 seconds");
 
 		//the connection is registered, so the client is now in a usable state and can execute commands.
 	}
 
 	//initiate registration
 	private void register() {
 		sendRaw("NICK " +nick);
 		sendRaw("USER " + user + " 0 * : " + real);
 	}
 
 	private void ping() {
 
 		if ( System.currentTimeMillis() - last_ping <= PING_TIMEOUT )
 			return;
 
 		last_ping = System.currentTimeMillis();
 			
 		send("PING", hostname, Priority.CRITICAL);
 	}
 
 	/**
 	 * Issue the nick command...
 	 */
 	public void nick(String nick) {
 		//send the nick command...	
 		send("NICK", nick);
 
 		this.nick = nick;
 
 		//@TODO monitor for failed nick changes.
 		//only set the nick on a successful reply...
 	}
 
 
 
 	public void part(String chan, String msg) {
 		send("PART " + chan, msg);
 	}
 
 	public void part(String chan) {
 		part(chan,"");
 	}
 
 	public void join(String chan) {
 		send("JOIN", chan);
 	}
 
 	public void msg(String target, String msg) {
 		msg(target, msg, Priority.MEDIUM);
 	}
 
 	public void msg(String target, String msg, Priority p) {
 		send("PRIVMSG " + target, msg, p);
 	}
 
 	public void notice(String target, String msg) {
 		notice(target, msg, Priority.MEDIUM);
 	}
 
 	public void notice(String target, String msg, Priority p) {
 		notice("PRIVMSG " + target, msg, p);
 	}
 
 
 	public String nick() {
 		return this.nick;
 	}
 
 	public void quit() {
 		quit("Client exited.");
 	}
 
 	public void quit(String msg) {
 		send("QUIT", msg);
		conn.close();
		conn = null;
 	}
 
 	//handle a raw received message
 	private void handleRaw(String raw) {
 		if (raw.length() == 0) return;
 	
 		recvQ.offer( MessageParser.parse( raw ) );
 
 	}
 
 	//public send
 	public void send(String cmd, String msg) {
 		send(cmd,msg,Priority.MEDIUM);
 	}
 
 	public void send(String cmd, String msg, Priority p) {
 		sendRaw(cmd + " :" + msg, p);
 	}
 
 	private void sendRaw(String cmd) {
 		sendRaw(cmd, Priority.MEDIUM);
 	}
 
 	private void sendRaw(String cmd, Priority p) {
 
 		//@TODO error checking
 		//offer returns bool.
 		if (!sendQ.offer( new OutgoingMessage(cmd,p) ))
 			throw new RuntimeException("Failed to queue message: " + cmd);
 
 	}
 
 
 
 	/**
 	 * Provides a fluent interface...
 	 */
 	public IrcMessageSubscription addMessageHandler(MessageHandler handler) {
 		return (new IrcMessageSubscription(handler)).register(); 
 	}
 
 	//a subscription to irc messages
 	public class IrcMessageSubscription {
 			
 		private Set<MessageType> types = null;
 		private Set<MessageCode> codes = null;
 
 		private List<String> cmds = null;
 		private List<Pattern> patterns = null;
 
 		private MessageHandler handler;
 			
 		private IrcMessageSubscription(MessageHandler handler) {
 			this.handler = handler;
 		}
 
 		/**
 		 * Add a type to the subscription
 		 * @return provides a fluent interface
 		 */
 		public IrcMessageSubscription addType(MessageType type) {
 			
 			if  ( types == null ) 
 				types = new TreeSet<MessageType>();
 
 			types.add(type);
 
 			return this;
 		}
 
 		/**
 		 * Add a code to the subscription
 		 * @return provides a fluent interface
 		 */
 		public IrcMessageSubscription addCode(MessageCode code) {
 
 			if  ( codes == null ) 
 				codes = new TreeSet<MessageCode>();
 
 			codes.add(code);
 
 			return this;
 		}
 
 		public IrcMessageSubscription addCommand(String cmd) {
 			
 			if ( cmds == null ) 
 				cmds = new util.LinkedList<String>();
 
 			cmds.add(cmd);
 
 			return this;
 		}
 
 		public IrcMessageSubscription addPattern(Pattern p) {
 			
 			if ( patterns == null )
 				patterns = new util.LinkedList<Pattern>();
 
 			patterns.add(p);
 
 			return this;
 		}
 
 		private IrcMessageSubscription register() {
 			handlers.add(this);
 			return this;
 		}
 
 		public IrcMessageSubscription or() {
 			return (new IrcMessageSubscription(this.handler)).register();
 		}
 
 		//tests if this subscription matches, calls the handlers handle if it does.
 		private void handle(Message msg) {
 
 			//Type must ALWAYS match...
 			//msg.getType() should NEVER return null.
 			if ( this.types != null && !types.contains( msg.getType() ) )
 				return;
 
 			//code or pattern must match...
 			boolean codeMatches = true,
 					cmdMatches = true;
 
 			if ( this.codes != null && (msg.getCode() == null || !codes.contains( msg.getCode() )) )
 				codeMatches = false;
 
 		
 			//if the code DOESN'T match and there are commands to match.
 			if ( !codeMatches && this.cmds != null ) {
 				cmdMatches = false;
 
 				for (String cmd : cmds) {
 					if ( cmd.equals(msg.getCommand()) ) {
 						cmdMatches = true;
 						break;
 					}
 				}
 			}
 
 			//if we didn't find a command or code match...
 			if ( !(cmdMatches || codeMatches) ) 
 				return;
 
 			//Patterns must always match....
 			if ( this.patterns != null ) {
 				boolean found = false;
 
 				for (Pattern p : patterns) {
 					if ( p.matcher( msg.getMessage() ).matches() ) {
 						found = true;
 						break;
 					}
 				}
 
 				if (!found) return;
 			}
 
 			//if we haven't returne by this point, the message must be a match...
 			handler.handle(msg);		
 		}
 	}
 	
 	//handler for some internal stuff.
 	private MessageHandler internalHandler = new MessageHandler() {
 
 		public void handle(Message msg) {
 
 			if ( msg.getCode() == MessageCode.RPL_WELCOME ) {
 				setState(State.REGISTERED);
 
 				hostname = msg.getSource().getHost();
 			} else if ( msg.getType() == MessageType.PING )
 				//preempt!
 				send( "PONG", msg.getMessage(), Priority.CRITICAL );
 		}
 	};
 
 	//message handler thread
 	private class Worker implements Runnable {
 
 		private Worker() {}
 
 		public void run() {
 
 			while(true) {
 
 				Message msg = null;
 
 				try {
 					msg = recvQ.poll(PING_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS);
 				} catch (InterruptedException e) {
 					System.out.println("INTERRUPT");
 				}
 
 				//nothing to do
 				if (msg == null) continue;
 
 				Iterator<IrcMessageSubscription> it = handlers.iterator();
 					
 				while (it.hasNext()) try {
 					it.next().handle( msg );
 				} catch (Exception e) {
 					//TODO need a way to handle these...
 					e.printStackTrace();
 				}
 
 				//We do this after the loop...
 				//don't bother checkuing until a message been sent...
 				if ( ((System.currentTimeMillis() - last_tx > MAX_IDLE/2) || (System.currentTimeMillis() - last_rx) > MAX_IDLE/2) ) ping();
 			}
 		}
 	}
 
 	private static class OutgoingMessage implements Comparable<OutgoingMessage> {
 		
 		Priority priority;
 		String msg;
 
 		private OutgoingMessage(String msg) {
 			this(msg,Priority.MEDIUM);
 		}
 
 		private OutgoingMessage(String msg, Priority priority) {
 			this.priority = priority;
 			this.msg = msg;// + "\r\n";
 		}
 
 		public String getMessage() {
 			return msg;
 		}
 
 		public int compareTo( OutgoingMessage msg ) {
 			return this.priority.getValue() - msg.priority.getValue();
 		}
 	}
 	//connection thread
 	private class IrcConnection {
 
 		//per RFC, max size of irc message...
 		private static final int MSG_SIZE = 512;
 
 		private Socket conn = null;
 		private PrintWriter out;
 		private BufferedReader in;
 
 		private IrcConnection() throws java.io.IOException {
 
 			conn = new java.net.Socket(host,port);
 
 			out = new PrintWriter( conn.getOutputStream(), false );
 			in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
 
 			last_tx = last_rx = System.currentTimeMillis();
 
 			//setting state = connected starts all the fun loops..
 			setState(state.CONNECTED);
 			
 			(new Thread( readerThread, "Socket Read" )).start();
 			(new Thread( writerThread, "Socket Write" )).start();
 		}
 
 		private Runnable readerThread = new Runnable() {
 
 			/**
 			 * @TODO exception handling
 			 */
 			public void run() {
 			
 				if ( state == State.DISCONNECTED ) 
 					throw new RuntimeException("Trying to loop over non connection???");
 	
 				while ( state != State.DISCONNECTED ) try {	
 					
 					/**
 					 * NOTE: this *should* work.  
 					 * Not 100% sure in the case of CTCPS
 					 * I believe CR/LF characters must be quoted in in a CTCP (by the server, presumably),
 					 * but in any case, this should be changed to a character-by-character read for two reasons:
 					 *
 					 * (1) enforce the MSG_SIZE
 					 * (2) An unquoted embedded CR\LF inside a CTCP would cause the message to be split into two
 					 * 	thus allowing message injection. Probably not a good idea to rely on the server to do the quoting...
 					 */
 					recv( in.readLine() );
 		
 				} catch (Exception e) {
 					//@TODO
 					e.printStackTrace();
 				}
 			}
 		};
 
 		private Runnable writerThread = new Runnable() {
 
 			/**
 			 * @TODO exception handling
 			 */
 			public void run() {
 			
 				if ( state == State.DISCONNECTED ) 
 					throw new RuntimeException("Trying to loop over non connection???");
 	
 				while( state != State.DISCONNECTED ) try {	
 
 					sendMsg( sendQ.poll(10, java.util.concurrent.TimeUnit.SECONDS) );
 		
 				} catch (Exception e) {
 					//@TODO
 					e.printStackTrace();
 				}
 			}
 		};
 
 		private void sendMsg(OutgoingMessage msg) {
 
 			if (msg == null) return;
 
 			out.print( msg.getMessage() );
 			out.print( "\r\n" );
 			out.flush();
 		}
 
 		/**
 		 * Receives/buffers any data on the channel
 		 */
 		private void recv(String msg) {
 			if (msg == null) return;
 
 			handleRaw(msg);
 		}
 
 		public void close() {

 			//stop everything from looping
 			setState(State.DISCONNECTED);
 
 			try {
				//should socket be set to block first?
 				conn.close();
 			} catch (Exception e) {
				//@TODO
				e.printStackTrace();
 			}
 		}
 
 		protected void finalize() {
			if ( state == State.REGISTERED ) {
				quit();
			}
			else 
				close();
 		}
 	}
 }
