 package ucbang.network;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.LinkedList;
 import java.util.Random;
 
 import java.util.Iterator;
 
 import ucbang.core.*;
 import ucbang.core.Deck.CardName;
 import ucbang.gui.CardDisplayer;
 import ucbang.gui.ClientGUI;
 import ucbang.gui.Field;
 public class Client extends Thread {
 	String name = "";
 	int id;
 	public int numPlayers = 0;// should be deprecated soon in favor of
 	// players.size()
 	Socket socket = null;
 	Random r = new Random();
 	int port = 12345;
 	String host = "127.0.0.1";
 	boolean connected = false;
 	public LinkedList<String> outMsgs = new LinkedList<String>();
 	ClientGUI gui;
 	public Player player;
 	public LinkedList<String> players = new LinkedList<String>();
 	public Field field;
 	ClientThread t;
 	int turn;
         
         public boolean prompting;
         public boolean forceDecision;
 
 	public Client(String host, boolean guiEnabled) {
 		this.host = host;
 		if (guiEnabled)
 			gui = new ClientGUI(numPlayers, this);
 		field = new Field(new CardDisplayer(), this);
 		gui.addMouseListener(field); //TODO: does this need to be here?
                gui.addMouseMotionListener(field);
 		//Begin testing card field stuffs
 		CardName[] cards=CardName.values();
 		int x = 70;
 		int y = 30;
 		for(int i=0;i<cards.length;i++){
 			field.add(new Card(cards[i]), x,y);
 			x+=60;
 			if(x>750){
 				y+=90;
 				x=70;
 			}
		}
                
                /*Deck.Characters[] chars= Deck.Characters.values();
		x = 70;
		y = 30;
		for(int i=0;i<chars.length;i++){
			field.add(new Card(chars[i]), x,y);
			x+=60;
			if(x>750){
				y+=90;
				x=70;
			}
		}*/
                 
                 
 		promptName();
 		this.start();
                 player = new Player(id,"name");   //check if this is right
 	}
 
 	public Client(String host, boolean guiEnabled, String name) {
 		this.host = host;
 		this.name = name;
 		if (guiEnabled)
 			gui = new ClientGUI(numPlayers++, this);
 		field = new Field(new CardDisplayer(), this);
 		gui.addMouseListener(field);
 		//Begin testing card field stuffs
 		CardName[] cards=CardName.values();
 		int x = 70;
 		int y = 30;
 		for(int i=0;i<cards.length;i++){
 			field.add(new Card(cards[i]), x,y);
 			x+=60;
 			if(x>750){
 				y+=90;
 				x=70;
 			}
 		}
 		this.start();
                 player = new Player(id,"name");   //check if this is right
 	}
 
 	public static void main(String[] Args) {
 		if (Args.length == 0)
 			new Client("127.0.0.1", true);
 		else if (Args.length == 1)
 			new Client(Args[0], true);
 		else if (Args.length == 2)
 			if (Args[1].equals("Dummy"))
 				new Client(Args[0], false);
 			else
 				new Client(Args[0], true, Args[1]);
 	}
 
 	public String getPlayerName() {
 		return name;
 	}
 
 	void promptName() {
 		System.out.println("Choosing a new name");
 		name = gui.promptChooseName();
 		synchronized (name) {
 			name.notifyAll();
 		}
 		System.out.println("New name is " + name);
 	}
 
 	public void run() {
 		try {
 			socket = new Socket(host, port);
 		} catch (Exception e) {
 			System.err.println(e + "\nServer Socket Error!");
 		}
 		t = new ClientThread(socket, this);
 		while (true) {
 			gui.update();
 			try {
 				sleep(45);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	void print(Object stuff) {
 		if (gui != null)
 			gui.appendText("Client:" + stuff);
 		else
 			System.out.println("Client:" + stuff);
 	}
 
 	void addMsg(String msg) {
 		synchronized (outMsgs) {
 			outMsgs.add(msg);
 		}
 	}
 
 	public void addChat(String chat) {
 		addMsg("Chat:" + chat);
 	}
 
 }
 
 class ClientThread extends Thread {
 	Socket server;
 	BufferedReader in;
 	BufferedWriter out;
 	Client c;
 	String buffer;
 	boolean namesent = false;
 
 	// int response = -2; //-1 is cancel
 
 	public ClientThread(Socket theServer, Client c) {
 		server = theServer;
 		this.c = c;
 		try {
 			out = new BufferedWriter(new OutputStreamWriter(server
 					.getOutputStream()));
 			in = new BufferedReader(new InputStreamReader(server
 					.getInputStream()));
 		} catch (Exception e1) {
 			try {
 				if(server!=null) //is it closing too soon some times?
                                     server.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return;
 		}
 		this.start();
 	}
 
 	public void run() {
 		while (!server.isClosed()) {
 			// System.out.println("Loop looping");
 			try {
 				if (c.name != null && out != null && !c.connected && !namesent) {
 					out.write("Name:" + c.name);
 					out.newLine();
 					out.flush();
 					namesent = true;
 				}
 				synchronized (c.outMsgs) {
 					if (!c.outMsgs.isEmpty()) {
 						Iterator<String> iter = c.outMsgs.iterator();
 						while (iter.hasNext()) {
 							out.write(iter.next());
 							out.newLine();
 							iter.remove();
 						}
 					}
 				}
 				out.flush();
 				if (in.ready()) {
 					buffer = (String) in.readLine();
 					String[] temp = buffer.split(":", 2);
 					if (temp[0].equals("Connection")) {
 						System.out.println(temp[1]);
 						if (!c.connected
 								&& temp[1].equals("Successfully connected.")) {
 							c.connected = true;
 							c.gui.setTitle("UCBang - "+c.name+" - Connected to server on "+
 									server.getInetAddress());
 						} else if (!c.connected
 								&& temp[1].equals("Name taken!")) {
 							System.out
 									.println(this
 											+ ": Connection refused because name was taken");
 							namesent = false;
 							c.promptName();
 						}
 					} else if (temp[0].equals("Chat")) {
 						c.gui.appendText(temp[1]);
 					} else if (temp[0].equals("Players")) {
 						String[] ppl = temp[1].split(",");
 						for (int i = 0; i < ppl.length; i++) {
 							if (ppl[i] != null && !ppl[i].isEmpty()) {
 								c.players.add(ppl[i]);
 							}
 						}
 					} else if (temp[0].equals("PlayerJoin")) {
 						c.players.add(temp[1]);
 					} else if (temp[0].equals("PlayerLeave")) {
 						c.players.remove(temp[1]);
 					} else if (temp[0].equals("Prompt")) {
 						// received a prompt from host
 						if (temp[1].equals("Start")) { // will waiting for
 							// response here cause
 							// client to desync with
 							// server?
 							c.outMsgs.add("Prompt:"+ c.gui.promptYesNo("Host has sent a request to start game","Start game?"));
 							c.gui.appendText("Host has requested the game be started");
 						}
 						else if (temp[1].equals("PlayCard")) { //play one card
                                                     c.gui.promptChooseCard(c.player.hand,"","",true);
                                                     //c.outMsgs.add("Prompt:"+a);
                                                     //System.out.println("PLAYING CARD"+c.player.hand.get(a).name);
 						}
                                                 else if (temp[1].equals("PlayCardUnforced")) { //play one card
                                                     c.gui.promptChooseCard(c.player.hand,"","",false);
                                                     //c.outMsgs.add("Prompt:"+a);
                                                     //if(a>=0)
                                                     //    System.out.println("PLAYING CARD"+c.player.hand.get(a).name);
                                                 }
                                                 else if (temp[1].equals("ChooseCharacter")) { //play one card
                                                     c.gui.promptChooseCard(c.player.hand,"","",true);
                                                 }
 					} 
                                         else if (temp[0].equals("Draw")) {
                                                 String[] temp1 = temp[1].split(":");
                                                 int n = temp1.length;
                                                 for(int m = 1; m<n; m++){
                                                     if(temp1[0].equals("Character")){
                                                         Card card = new Card(Deck.Characters.valueOf(temp1[m]));
                                                         c.player.hand.add(card);
                                                         c.field.add(card,80+(int)(400*Math.random()), 80+(int)(400*Math.random()));
                                                     }
                                                     else{
                                                         Card card = new Card(Deck.CardName.valueOf(temp1[m]));
                                                         c.player.hand.add(card);    
                                                         c.field.add(card,80+(int)(400*Math.random()), 80+(int)(400*Math.random()));
                                                     }
                                                 }
                                                 c.outMsgs.add("Ready");
                                         }
                                         else if (temp[0].equals("GetInfo")) {
 						// get information about hand and stuff
 						// how many parameters are needed?
 						String[] temp1 = buffer.split(":", 2);
                                                 c.outMsgs.add("Ready");
                                         } else if (temp[0].equals("SetInfo")) { // note: a bit of a
 						// misnomer for
 						// lifepoints, just
 						// adds or subtracts
 						// that amount
 						// set information about hand and stuff
 						String[] temp1 = temp[1].split(":");
 						if (temp1[0].equals("newPlayer")){
 							c.player = new Player(Integer.valueOf(temp1[1]), c.name);
                                                         c.numPlayers = Integer.valueOf(temp1[2]);
 						}
 						else if (temp1[0].equals("role")) {
                                                         c.field.clear();
 							c.player.role = Deck.Role.values()[Integer.valueOf(temp1[1])];
 						}
 						else if (temp1[0].equals("maxHP")) {
                                                         c.field.clear();
 							c.player.maxLifePoints += Integer.valueOf(temp1[1]);
 						}
                                                 else if (temp1[0].equals("turn")) {
                                                         c.turn = Integer.valueOf(temp1[1]);
                                                         if(c.turn%c.numPlayers==0){
                                                             c.gui.appendText("hay guyz, iz my turn");
                                                         }
                                                 }
                                                 else if (temp1[0].equals("discard")) {
                                                         c.field.cards.remove(c.player.hand.get((int)Integer.valueOf(temp1[1])));
                                                         System.out.println("MOVED TO DISCARD:"+c.player.hand.remove((int)Integer.valueOf(temp1[1])).name);
                                                 }
                                                 else{
                                                     System.out.println("WTF do i do with "+temp1[0]+":"+temp1[1]);
                                                 }
 					    c.outMsgs.add("Ready");
 					}
 				}
 			} catch (Exception e) {
 				if (e != null && e.getMessage() != null
 						&& e.getMessage().equals("Connection reset")) {
 					print("Connection to server lost");
 					try {
 						finalize();
 					} catch (Throwable t) {
 						t.printStackTrace();
 					}
 				} 
 				e.printStackTrace();
 			}
 			try {
 				sleep(45);
 			} catch (InterruptedException e) {
 			}
 		}
 		System.out.println("Server connection closed");
 	}
 
 	protected void finalize() throws Throwable {
 		try {
 			in.close();
 			out.close();
 			server.close();
 		} catch (Exception e) {
 		}
 	}
 
 	void print(Object stuff) {
 		if (c.gui != null)
 			c.gui.appendText("ClientThread:" + stuff);
 		else
 			System.out.println("ClientThread:" + stuff);
 	}
 }
