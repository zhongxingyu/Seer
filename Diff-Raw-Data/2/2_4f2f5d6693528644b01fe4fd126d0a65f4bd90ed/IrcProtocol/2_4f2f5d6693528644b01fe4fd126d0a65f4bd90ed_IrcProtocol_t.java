 package irc;
 
 import irc.events.IrcEvent;
 import irc.listeners.InputListener;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 
 public abstract class IrcProtocol {
 	
 	private BufferedReader in;
 	private PrintWriter out;
	private List<InputListener> inputEventListeners = new CopyOnWriteArrayList<InputListener>();
 	
 	public void privMsg(String output, String loc) {
 		write("PRIVMSG " + loc + " :" + output);
 	}
 	
 	public void notice(String output, String loc) {
 		write("NOTICE " + loc + " :" + output);
 	}
 	
 	public void join(String loc) {
 		write("JOIN " + loc);
 	}
 	
 	public void write(String output) {
 		out.write(output + "\r\n");
 		out.flush();
 	}
 	
 	private void connect(String host, int ip) throws IOException {
 		Socket s = new Socket(host, ip);
 		in =  new BufferedReader(new InputStreamReader(s.getInputStream()));
 		out = new PrintWriter(s.getOutputStream(), true);
 	}
 	
 	public void invokeListeners(String input) {
 		if (inputEventListeners.size() == 0) return;
 		IrcEvent event = new IrcEvent(new IrcMessage(input));
 		String command = event.getSource().getCommand();
 		if (event.getSource().getUserCommand() != null) {
 			for (InputListener i : inputEventListeners) {
 				i.handleUserCommand(event);
 			}
 		} else if (command.equalsIgnoreCase("privmsg")) {
 			for (InputListener i : inputEventListeners) {
 				i.handlePrivMsg(event);
 			}
 		} else if (command.equalsIgnoreCase("ping")) {
 			for (InputListener i : inputEventListeners) {
 				i.handlePing(event);
 			}
 		} else if (command.equalsIgnoreCase("invite")) {
 			System.out.println("invite");
 			for (InputListener i : inputEventListeners) {
 				i.handleInvite(event);
 			}
 		} else if (command.equalsIgnoreCase("kick")) {
 			for (InputListener i : inputEventListeners) {
 				i.handleKick(event);
 			}
 		} else if (command.equalsIgnoreCase("join")) {
 			for (InputListener i : inputEventListeners) {
 				i.handleJoin(event);
 			}
 		} else if (command.equalsIgnoreCase("part")) {
 			for (InputListener i : inputEventListeners) {
 				i.handlePart(event);
 			}
 		} else if (command.equalsIgnoreCase("mode")) {
 			for (InputListener i : inputEventListeners) {
 				i.handleMode(event);
 			}
 		} else if (command.equalsIgnoreCase("topic")) {
 			for (InputListener i : inputEventListeners) {
 				i.handleTopic(event);
 			}
 		} else if (command.equalsIgnoreCase("notice")) {
 			for (InputListener i : inputEventListeners) {
 				i.handleNotice(event);
 			}
 		}
 	}
 	
 	public IrcProtocol(String host, int ip, String nick, String user, String pass) throws IOException {
 		if (host == null || ip <= 0 || nick == null || user == null) {
 			return;
 		}
 		connect(host, ip);
 		write("NICK "+ nick);
 		write("USER " + user + " * * :" + nick);
 		if (pass != null)
 			write("PRVMSG NICKSERV IDENTIFY " + pass);
 	}
 	
 	public List<InputListener> getInputEventListeners() {
 		return inputEventListeners;
 	}
 	
 	public void addInputEventListener(InputListener listener) {
 		inputEventListeners.add(listener);
 	}
 	
 	public BufferedReader getIn() {
 		return in;
 	}
 	
 }
