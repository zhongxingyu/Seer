 package ds.lab.messagepasser;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicIntegerArray;
 
 import ds.lab.bean.NodeBean;
 import ds.lab.bean.RuleBean;
 import ds.lab.message.Message;
 import ds.lab.message.MessageAction;
 import ds.lab.message.MessageKind;
 
 public class MessagePasser implements MessagePasserApi {
 	/** multithreading, node management */
 	private int MAX_THREAD;
 	private ServerSocket listenSocket;
 	private HashMap<String, NodeBean> nodeList;// name-user
 	private HashMap<String, ObjectOutputStream> outStreamMap;// name-socket
 	/** message */
 	private BlockingQueue<Message> inputQueue; // input queue
 	private BlockingQueue<Message> outputQueue; // output queue
 	private BlockingQueue<Message> delayInputQueue; // input queue
 	private BlockingQueue<Message> delayOutputQueue; // output queue
 	/** other local information */
 	private String localName;
 	private HashMap<String, String> ipNameMap;	//for reverse lookup by ip, <ip, name>
 	private AtomicIntegerArray sendNthTracker;
 	private AtomicIntegerArray rcvNthTracker;
 	private AtomicInteger lastId;
 	private final int NUM_ACTION = 3;// DROP, DUPLICATE, DELAY
 	private Config config;
 
 	/**
 	 * Constructor read yaml formatted configure file, parse "configuration"
 	 * section into nodeList as NodeBean;parse sendRule and rcvRule
 	 * 
 	 * @param configurationFile
 	 * @param localName
 	 *            global unique name to identify this node
 	 * @throws IOException
 	 */
 	public MessagePasser(String configurationFile, String localName) throws IOException {
 		config = new Config(configurationFile, localName);
 		MAX_THREAD = Config.NUM_NODE;
 		nodeList = Config.NODELIST;
 		/* build my listening socket */
 		NodeBean me = nodeList.get(localName);
 		if (me == null) {
 			throw new IllegalArgumentException("Error local name");
 		}
 		this.localName = localName;
 		lastId = new AtomicInteger(-1);
 		ipNameMap = new HashMap<String, String>();
 		for (NodeBean n : nodeList.values())
 			ipNameMap.put(n.getIp(), n.getName());
 
 		/* queues and trackers */
 		inputQueue = new LinkedBlockingDeque<Message>();
 		outputQueue = new LinkedBlockingDeque<Message>();
 		delayInputQueue = new LinkedBlockingDeque<Message>();
 		delayOutputQueue = new LinkedBlockingDeque<Message>();
 		sendNthTracker = new AtomicIntegerArray(NUM_ACTION);
 		rcvNthTracker = new AtomicIntegerArray(NUM_ACTION);
 		/* listener */
 		outStreamMap = new HashMap<String, ObjectOutputStream>();
 		listenSocket = new ServerSocket(me.getPort());
 		ListenThread listener = new ListenThread();
 		// TODO for loop MAX_THREAD
 		new Thread(listener).start();
 		UserThread userIf = new UserThread();
 		new Thread(userIf).start();
 	}
 
 	/**
 	 * 
 	 * @param argv
 	 *            local name of this node
 	 */
 	public static void main(String argv[]) {
 		if (argv.length < 2) {
 			System.out.println("Usage: configFile localName");
 			System.exit(0);
 		}
 
 		try {
 			new MessagePasser(argv[0], argv[1].toLowerCase());
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void send(Message message) {
 		// TODO sync message id
 		message.setId(lastId.incrementAndGet());
 		RuleBean theRule = getMatchedSendRule(message);
		System.err.println(theRule);
 		if (theRule == null) {
 			System.err.println("Messager> Error: no rule matches for current pair. Return");
 			return;
 		}
 		MessageAction action = theRule.getAction();
 		if (action != MessageAction.DEFAULT)
 			action = checkSendAction(theRule);
 		System.out.println(action);
 		try {
 			Message dup = null;
 			switch (action) {
 			case DROP:// ignore message
 				message = null;
 				break;
 			case DELAY:
 				
 				delayOutputQueue.add(message);// don't send
 				break;
 			case DUPLICATE:
 				sendNthTracker.incrementAndGet(1);
 				dup = message.clone();
 				dup.setId(message.getId());
 			case DEFAULT:
 				outputQueue.add(message);
 				// now there should be two identical messages. Send delayed after normal
 				/*
 				 * The dup should be added to outputqueue by
 				 * outputqueue.add(dup) isn't it ?? ----this needs count for sending the 2 message. if another thread adds one msg between them, you don't know which two should send...my opinion
 				 */
 				connectAndSend(outputQueue.remove());
 				if (dup != null)
 					connectAndSend(dup);
 				synchronized (delayOutputQueue) {
 					while (!delayOutputQueue.isEmpty())
 						connectAndSend(delayOutputQueue.remove());
 				}
 				
 			}
 		} catch (UnknownHostException e) {
 			System.err.println("Messager> " + e.getMessage());
 		} catch (SocketException e) {
 			System.err.println("Message cannot be delivered: " + message.getDest() + " is offline");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (CloneNotSupportedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private RuleBean getMatchedSendRule(Message message) {
 		ArrayList<RuleBean> rules = config.getSendRules();
 		RuleBean theRule = null;
 		for (RuleBean r : rules) {
 			if (r.isMatch(message)) {
 				theRule = r;
 				break;
 			}
 		}
 		return theRule;
 	}
 
 	private MessageAction checkSendAction(RuleBean r) {
 		if (r.hasNoRestriction())// if no specify nth or everyNth, ignore action
 									// field..
 			return r.getAction();
 		int now = sendNthTracker.incrementAndGet(r.getActionIndex());// counter++
 		if ((now == r.getNth()) || (r.getEveryNth() > 0 && (now % r.getEveryNth()) == 0))
 			return r.getAction();
 		return MessageAction.DEFAULT;
 	}
 
 	@Override
 	public ArrayList<Message> receive() {
 		ArrayList<Message> incoming = null;
 		if (!inputQueue.isEmpty()) {
 			Message message = inputQueue.remove();// examine the 1st one
 			RuleBean theRule = getMatchedReceiveRule(message);
 			System.err.println(theRule);
 			if (theRule == null) {
 				System.err.println("Messager> Error: no rule matches for current pair. Return");
 				return null;
 			}
			MessageAction action = checkReceiveAction(theRule);
 			System.out.println(action);
 			try {
 				Message dup = null;
 				switch (action) {
 				case DROP:// drop message TODO lastId???
 					break;
 				case DELAY:// move the message to delayQUeue
 					delayInputQueue.add(message);
 					break;
 				case DUPLICATE:
 					rcvNthTracker.incrementAndGet(1);
 					dup = message.clone();// add to list later
 					dup.setId(dup.getId());
 					// inputQueue.add(dup);
 				case DEFAULT:
 					// now there should be two identical messages to deliver.
 					// deliver delayed messages after the new one(s)
 					incoming = new ArrayList<Message>();
 					incoming.add(message);
 					if (dup != null)
 						incoming.add(dup);
 					synchronized (delayInputQueue) {
 						while (!delayInputQueue.isEmpty())
 							incoming.add(delayInputQueue.remove());
 					}
 				}
 
 			} catch (CloneNotSupportedException e) {
 				e.printStackTrace();
 			}
 		}
 		return incoming;
 	}
 
 	private RuleBean getMatchedReceiveRule(Message message) {
 		ArrayList<RuleBean> rules = config.getRcvRules();
 		RuleBean theRule = null;
 		for (RuleBean r : rules) {
 			if (r.isMatch(message)) {
 				theRule = r;
 				break;
 			}
 		}
 		return theRule;
 	}
 
 	private MessageAction checkReceiveAction(RuleBean r) {
 		if (r.hasNoRestriction())// if no specify nth or everyNth, ignore action
 									// field..
			return MessageAction.DEFAULT;
 		int now = rcvNthTracker.incrementAndGet(r.getActionIndex());// TODO
 																	// caution
 		if ((now == r.getNth()) || (r.getEveryNth() > 0 && (now % r.getEveryNth()) == 0))
 			return r.getAction();
 		return MessageAction.DEFAULT;
 	}
 
 	private void connectAndSend(Message message) throws UnknownHostException, SocketException, IOException {
 		String dest = message.getDest();
 		ObjectOutputStream out = outStreamMap.get(dest);
 		if (out == null) {// no socket connection yet, create it TODO if port change...
 			NodeBean n = nodeList.get(dest);
 			if (n == null)
 				throw new UnknownHostException("Message Send fails: unknown host " + dest);
 			Socket sendSocket = new Socket(n.getIp(), n.getPort());
 			// synchronized (WorkerThread.getSockMap()) {
 			// WorkerThread.getSockMap().put(InetAddress.getByName(n.getIp()).getHostAddress(),
 			// sendSocket);
 			// }
 			out = new ObjectOutputStream(sendSocket.getOutputStream());
 			outStreamMap.put(dest, out);
 		}
 		out.writeObject(message);
 		out.flush();
 		System.err.println("sent>>>>>>>>>msg"+message.getId());
 
 	}
 
 	private class ListenThread implements Runnable {
 		@Override
 		public void run() {
 			System.err.println("Listener> I'm " + localName);
 			/**
 			 * when a new socket connected, create a new thread to handle the
 			 * request, who's responsible for reading message from the socket
 			 * and add to input queue
 			 */
 			try {
 				while (true) {
 					Socket connection = listenSocket.accept();
 //					 connection.setKeepAlive(true);
 					assert connection.isConnected();
 					String remote = connection.getInetAddress().getHostAddress();
					System.err.println("Listener> Received: " + remote);
 
 					new WorkerThread(connection, inputQueue, ipNameMap.get(remote));
 				}
 			} catch (EOFException e) {//someone offline
 				String remote = e.getMessage();
 				System.err.println("Messager> " + remote + " went offline");
 				try {
 					outStreamMap.get(remote).close();
 					outStreamMap.remove(remote);
 				} catch (IOException e1) {
 					e.printStackTrace();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Responsible for interating with user: send or receive
 	 *
 	 * @author dmei
 	 * 
 	 */
 	private class UserThread implements Runnable {
 		@Override
 		public void run() {
			Scanner sc = new Scanner(System.in);
 			try {
 				while (true) {
 					System.err.println("Messager> Choose: 0. Send(S)\t1. Receive(R)");
 					String input = sc.nextLine().toLowerCase();
 					if (input.equals("0") || input.equals("send") || input.equals("s")) {
 						System.err.print("Messager> TO: ");
 						for (String name : nodeList.keySet()) {
 							if (name.equals(localName))// skip self
 								continue;
 							System.err.print(name + "\t");
 						}
 						String dest = sc.nextLine().toLowerCase();
 						System.err.println("\nMessager> Input Message in 144 chars:");
 						String outMessage = sc.nextLine();
 						System.out.println("\n Enter kind of the message: " + "\n '0' for Lookup" + "\n '1' for Ack"
 								+ "\n '2' for None");
 						int kind = sc.nextInt();
 						MessageKind mk = Message.getMessageKind(kind);
 
 						sendMessage(dest, mk, outMessage);
 
 					} else if (input.equals("1") || input.equals("receive") || input.equals("r")) {
 						ArrayList<Message> incoming = receive();
 						if (incoming != null) {
 							for (Message m : incoming) {
 								synchronized (lastId) {
 									if (lastId.get() < m.getId())
 										lastId.set(m.getId());
 								}
 								System.out.println(m.getSrc() + "> msg" + m.getId()+" "+m.getData());
 							}
							System.out.println("lastId: "+lastId);
 						} else
 							System.err.println("Messager> no message");
 					} else {
 						System.err.println("Messager> Invalid input");
 					}
 				}
 			} catch (NoSuchElementException e) {
 				System.err.println("Messager> User press CTRL+C. Bye!");
 			} finally {
 				sc.close();
 			}
 		}
 
 		private void sendMessage(String dest, MessageKind kind, Object data) {
 			send(new Message(localName, dest, kind, data));
 		}
 	}
 }
