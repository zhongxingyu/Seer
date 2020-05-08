 package ucbang.network;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 
 import ucbang.core.Card;
 import ucbang.core.Deck;
 import ucbang.core.Player;
 import ucbang.core.Deck.CardName;
 import ucbang.gui.CardDisplayer;
 import ucbang.gui.ClientGUI;
 import ucbang.gui.Field;
 
 public class Client extends Thread {
     String name = "";
     public int id;
     public int numPlayers = 0; // should be deprecated soon in favor of
     Socket socket = null;
     Random r = new Random();
     int port = 12345;
     String host = "127.0.0.1";
     boolean connected = false;
     public LinkedList<String> outMsgs = new LinkedList<String>();
     public ClientGUI gui;
     public ArrayList<Player> players = new ArrayList<Player>();
     public Player player;
     public Field field;
     ClientThread t;
     int turn;
     public boolean running;
     public boolean prompting;
     public boolean forceDecision;
     public boolean targetingPlayer;
     public int nextPrompt = -2; //this value will be returned the next time the client is prompted to do something
 
     /**
      * Constructs a client to the Bang server on the specified host.
      * <p>Note: guiEnabled is intended for bots and the like. As such there is no
      * use of it yet, but there will be eventually.</p>
      * @param host the host to connect to
      * @param guiEnabled whether the GUI is enabled
      */
     public Client(String host, boolean guiEnabled) {
         running = true;
         this.host = host;
         if (guiEnabled)
             gui = new ClientGUI(numPlayers, this);
         field = new Field(new CardDisplayer(), this);
         gui.addMouseListener(field); // TODO: does this need to be here?
         gui.addMouseMotionListener(field);
         // Begin testing card field stuffs
         CardName[] cards = CardName.values();
         int x = 70;
         int y = 30;
         for (int i = 0; i < cards.length; i++) {
             field.add(new Card(cards[i]), x, y, 0, false);
             x += 60;
             if (x > 750) {
                 y += 90;
                 x = 70;
             }
         }
         promptName();
         player = new Player(id, name);
         this.start();
     }
     /**
      * Constructs a client to the Bang server on the specified host, with the specified name.
      * <p>Note: guiEnabled is intended for bots and the like. As such there is no
      * use of it yet, but there will be eventually.</p>
      * @param host the host to connect to
      * @param guiEnabled whether the GUI is enabled
      * @param name the name of the client
      */
     public Client(String host, boolean guiEnabled, String name) {
         running = true;
         this.host = host;
         this.name = name;
         if (guiEnabled)
             gui = new ClientGUI(numPlayers++, this);
         field = new Field(new CardDisplayer(), this);
         gui.addMouseListener(field);
         gui.addMouseMotionListener(field);
         // Begin testing card field stuffs
         CardName[] cards = CardName.values();
         int x = 70;
         int y = 30;
         for (int i = 0; i < cards.length; i++) {
             field.add(new Card(cards[i]), x, y, 0, false);
             x += 60;
             if (x > 750) {
                 y += 90;
                 x = 70;
             }
         }
         player = new Player(id, name);
         this.start();
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
 
     /**
      * Gives the name of the local client
      * @return the name of the client
      */
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
         while (running) {
             gui.update();
             try {
                 sleep(45);
             } catch (InterruptedException e) {
             }
         }
         gui.dispose();
         gui = null;
         System.out.println("Exiting");
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
 
     /**
      * Sends the specified chat message to the server
      * @param chat the chat message
      */
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
             out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
             in =  new BufferedReader(new InputStreamReader(server.getInputStream()));
         } catch (Exception e1) {
             try {
                 if (server != null) // is it closing too soon some times?
                     server.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
             return;
         }
         this.start();
     }
 
     public void run() {
         while (!server.isClosed() && c.running) {
             // System.out.println("Loop looping");
             try {
                 if (c.name != null && out != null && !c.connected && 
                     !namesent) {
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
                     buffer = (String)in.readLine();
                     String[] temp = buffer.split(":", 2);
                     String messagetype = temp[0];
 					String messagevalue = temp[1];
 					if (messagetype.equals("Connection")) {
                         System.out.println(messagevalue);
                         if (!c.connected && 
                             messagevalue.equals("Successfully connected.")) {
                             c.connected = true;
                             c.gui.setTitle("UCBang - " + c.name + 
                                            " - Connected to server on " + 
                                            server.getInetAddress());
                         } else if (!c.connected && 
                                    messagevalue.equals("Name taken!")) {
                             System.out.println(this + 
                                                ": Connection refused because name was taken");
                             namesent = false;
                             c.promptName();
                         }
                     } else if (messagetype.equals("Chat")) {
                         c.gui.appendText(messagevalue);
                     } else if (messagetype.equals("InfoMsg")) {
                         String[] temp1 = messagevalue.split(":");
                         c.gui.appendText(temp1[0], (Integer.valueOf(temp1[1])==0)?Color.BLUE:Color.RED);
                     } else if (messagetype.equals("Players")) {
                         String[] ppl = messagevalue.split(",");
                         for (int i = 0; i < ppl.length; i++) {
                             if (ppl[i] != null && !ppl[i].isEmpty()) {
                                 c.players.add(new Player(i, ppl[i]));
                             }
                         }
                     } else if (messagetype.equals("PlayerJoin")) {
                         c.players.add(new Player(c.players.size(), messagevalue));
                     } else if (messagetype.equals("PlayerLeave")) {
                         c.players.remove(messagevalue);
                     } else if (messagetype.equals("Prompt")) {
                         if(c.nextPrompt!=-2){
                             c.outMsgs.add("Prompt:" + c.nextPrompt);
                             c.nextPrompt = -2;
                         }
                         // received a prompt from host
                         else if (messagevalue.equals("Start")) { // will waiting for
                             // response here cause
                             // client to desync with
                             // server?
                             c.outMsgs.add("Prompt:" + 
                                           c.gui.promptYesNo("Host has sent a request to start game", 
                                                             "Start game?"));
                             c.gui.appendText("Host has requested the game be started", 
                                              Color.BLUE);
                         } else if (messagevalue.equals("PlayCard")) {
                             c.gui.promptChooseCard(c.player.hand, "", "", 
                                                    true);
                         } else if (messagevalue.equals("PlayCardUnforced")) {
                             c.gui.promptChooseCard(c.player.hand, "", "", 
                                                    false);
                         } else if (messagevalue.equals("PickCardTarget")) {
                             c.gui.promptTargetCard("", "", //null should be ALL cards.
                                                    false);
                             c.nextPrompt = -1;
                         } else if (messagevalue.equals("ChooseCharacter")) {
                             c.gui.promptChooseCard(c.player.hand, "", "", 
                                                    true);
                         } else if (messagevalue.equals("PickTarget")) {
                             //System.out.println("I am player " + c.id + ", prompting = " + c.prompting);
                             //c.outMsgs.add("Prompt:" + (1 - c.id));
                             c.gui.promptChooseCard(null, "", "", false);
                             c.targetingPlayer = true;
                         } else {
                             System.out.println("WTF do i do with " + messagevalue);
                         }
 
                     } else if (messagetype.equals("Draw")) {
                         String[] temp1 = messagevalue.split(":");
                         int n = temp1.length;
                         if (Integer.valueOf(temp1[0]) == c.id) {
                             for (int m = 2; m < n; m++) {
                                 if (temp1[1].equals("Character")) {
                                     Card card = 
                                         new Card(Deck.Characters.valueOf(temp1[m]));
                                     c.field.add(card, 150+80*m, 200, c.id, false);
                                     c.player.hand.add(card);
                                 } else {
                                     Card card = 
                                         new Card(Deck.CardName.valueOf(temp1[m]));
                                     c.field.add(card, c.id, false);
                                     c.player.hand.add(card);
                                 }
                             }
                         } else {
                             c.gui.appendText("Player " + temp1[0] + " drew " + 
                                              temp1[1] + "cards.", Color.GREEN);
                             for(int i=0;i<Integer.valueOf(temp1[1]);i++){
                                 Card card = new Card(Deck.CardName.BACK);
                             	c.field.add(card, Integer.valueOf(temp1[0]), false);
                             	c.players.get(Integer.valueOf(temp1[0])).hand.add(card);
                             }
                         }
                         c.outMsgs.add("Ready");
                     } else if (messagetype.equals("GetInfo")) {
                         String[] temp1 = buffer.split(":", 2);
                         c.outMsgs.add("Ready");
                     } else if (messagetype.equals("SetInfo")) { // note: a bit of a
                         // misnomer for lifepoints, just adds or subtracts that amount
                         // set information about hand and stuff
                         String[] temp1 = messagevalue.split(":");
                     	int tid = Integer.valueOf(temp1[1]);
                         String infotype = temp1[0];
                         Player ptemp = null;
                         if (infotype.equals("newPlayer")) {
                             c.id = tid;
                             c.player = new Player(tid, c.name); 
                             c.numPlayers = Integer.valueOf(temp1[2]);
                             c.players.set(c.id, c.player);
                         } else{
                             if(c.id == tid){
                                     ptemp = c.player;
                             }else if(tid<c.players.size()){
                                     ptemp = c.players.get(tid);
                             }
                         }
                         if (infotype.equals("role")) {
                             if (tid == c.id) {
                                 c.field.clear();
                                 c.player.role = 
                                         Deck.Role.values()[Integer.valueOf(temp1[2])];
                                 c.gui.appendText("You are a " + 
                                                  Deck.Role.values()[Integer.valueOf(temp1[2])].name(), 
                                                  Color.YELLOW);
                             } else {
                                 if (Integer.valueOf(temp1[2]) == 0)
                                     c.gui.appendText("Player " + temp1[1] + 
                                                      " is the " + 
                                                      Deck.Role.values()[Integer.valueOf(temp1[2])].name(), 
                                                      Color.YELLOW);
                                 else //only shown when player is killed
                                     c.gui.appendText("Player " + temp1[1] + 
                                                      " was a " + 
                                                      Deck.Role.values()[Integer.valueOf(temp1[2])].name(), 
                                                      Color.YELLOW);
                             }
                         } else if (infotype.equals("maxHP")) {
                             c.gui.appendText("Player " + temp1[1] + 
                                              " has a maxHP of " + temp1[2], 
                                              Color.RED);
                             ptemp.maxLifePoints=Integer.valueOf(temp1[2]);
                             ptemp.lifePoints=Integer.valueOf(temp1[2]);
                                 //this should match the above block
                             if(tid+1==c.numPlayers)
                             	c.field.start2();
                         } else if (infotype.equals("HP")) {
                             c.gui.appendText("Player " + temp1[1] + 
                                              " life points changed by " + 
                                              temp1[2], Color.RED);
                             ptemp.lifePoints+=Integer.valueOf(temp1[2]);
                             c.field.setHP(tid,ptemp.lifePoints);
                         } else if (infotype.equals("PutInField")) {
                                 c.gui.appendText("Player "+temp1[1]+" added "+temp1[2]+" to the field.");
                                 Card card;
                                 if(tid==c.id){
                                     if(temp1.length==4){
                                         card = c.player.hand.get(Integer.valueOf(temp1[3]));
                                         card.location = 1;
                                         c.field.clickies.remove(card);
                                         c.player.hand.remove(card);
                                     } else{
                                         card = new Card(Deck.CardName.values()[Integer.valueOf(temp1[2])]);
                                     }
                                 }
                                 else{
                                     if(temp1.length==4){
                                         card = new Card(CardName.valueOf(temp1[2]));
                                         card.location = 1;
                                         c.field.clickies.remove(c.players.get(tid).hand.get((int)Integer.valueOf(temp1[3])));
                                         c.players.get(tid).hand.remove((int)Integer.valueOf(temp1[3]));
                                     }
                                     else{
                                         card = new Card(Deck.CardName.values()[Integer.valueOf(temp1[2])]);
                                         card.location = 1;
                                     }
                                 }
                                 c.players.get(tid).field.add(card);
                                 c.field.add(card, tid, true);
                         } else if (infotype.equals("turn")) {
                             c.turn = tid;
                             if (c.turn % c.numPlayers == c.id) {
                                 c.gui.appendText("It's your move!!!!!! Time to d-d-d-d-d-duel!", Color.CYAN);
                             }
                         } else if (infotype.equals("discard")) {
                             if(tid==c.id){
                                 c.field.clickies.remove(c.player.hand.get(Integer.valueOf(temp1[2])));
                                 c.gui.appendText("You discarded:" + c.player.hand.remove(Integer.valueOf(temp1[2]).intValue()).name);
                             }
                             else{
                                 c.field.clickies.remove(c.players.get(tid).hand.get(Integer.valueOf(temp1[2])));
                                 c.players.get(tid).hand.remove(Integer.valueOf(temp1[2]).intValue());
                                 c.gui.appendText("Player "+tid+" discarded:" + (temp1.length==4?temp1[3]:"card #"+temp1[2]));
                             }
                         } else if (infotype.equals("fieldDiscard")) {
                             if(tid==c.id){
                                 c.gui.appendText("REMOVING:" + Integer.valueOf(temp1[2]).intValue()+ " "+c.player.field.get(Integer.valueOf(temp1[2]).intValue())+" "+c.player.field.size());
                                 c.field.clickies.remove(c.player.field.get(Integer.valueOf(temp1[2]).intValue()));
                                 c.gui.appendText("You discarded:" + c.player.field.remove(Integer.valueOf(temp1[2]).intValue()).name);
                             }
                             else{
                                 System.out.println("ASDFASDFASDFASDFASDF"+temp[1]);
                                 c.field.clickies.remove(c.players.get(tid).field.get(Integer.valueOf(temp1[2])));
                                 c.players.get(tid).field.remove(Integer.valueOf(temp1[2]).intValue());
                                 c.gui.appendText("Player "+tid+" discarded:" + temp1[3]);
                             }
                         }
                         else if (infotype.equals("CardPlayed")) {
                             String s = "";
                             s = "Player " + temp1[1] + " played " + temp1[2] + (temp1.length == 4 ? " at player " + temp1[3] : "");
                             c.gui.appendText(s);
                             //if(tid!=c.id && (temp1.length==4?!temp1[3].equals("no miss"):true)) //client would have already removed it
                                 //c.field.removeLast(tid);
                         } else if (infotype.equals("id")) { //TODO: remove safely?
                             c.id = tid;
                         } else if (infotype.equals("character")) {
                             if (tid == c.id){
                                 c.player.character = Integer.valueOf(temp1[2]);
                                 c.players.get(tid).character=Integer.valueOf(temp1[2]);
                             }
                             else {
                                 c.gui.appendText("Player " + temp1[1] + " chose " + Deck.Characters.values()[Integer.valueOf(temp1[2])], Color.YELLOW);
                                 c.players.get(tid).character=Integer.valueOf(temp1[2]);
                                 c.field.add(new Card(Deck.Characters.values()[Integer.valueOf(temp1[2])]), tid, false);
                             }
                         } else {
                             System.out.println("WTF do i do with " + infotype + ":" + temp1[1]);
                         }
                         c.outMsgs.add("Ready");
                     }
                 }
             } catch (Exception e) {
                 if (e != null && e.getMessage() != null && 
                     e.getMessage().equals("Connection reset")) {
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
         try {
             this.finalize();
         } catch (Throwable e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         System.out.println("Server connection closed");
     }
 
     protected void finalize() throws Throwable {
         try {
             if (c.id == 0) {
                 out.write("/shutdown");
                 out.flush();
             }
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
