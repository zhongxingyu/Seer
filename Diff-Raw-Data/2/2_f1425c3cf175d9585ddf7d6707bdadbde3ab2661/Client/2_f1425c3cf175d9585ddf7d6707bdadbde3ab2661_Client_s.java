 package ucbang.network;
 
 import java.awt.Color;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 import java.net.Socket;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
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
     public int id;
     public int numPlayers = 0; // should be deprecated soon in favor of
     // players.size()
     Socket socket = null;
     Random r = new Random();
    int port = 80;
     String host = "127.0.0.1";
     boolean connected = false;
     public LinkedList<String> outMsgs = new LinkedList<String>();
     ClientGUI gui;
     public Player player;
     public Deque<Player> players = new ArrayDeque<Player>();
     public Field field;
     ClientThread t;
     int turn;
     public boolean running;
     public boolean prompting;
     public boolean forceDecision;
 
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
             field.add(new Card(cards[i]), x, y);
             x += 60;
             if (x > 750) {
                 y += 90;
                 x = 70;
             }
         }
 
         promptName();
         this.start();
         player = new Player(id, "name"); // check if this is right
     }
 
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
             field.add(new Card(cards[i]), x, y);
             x += 60;
             if (x > 750) {
                 y += 90;
                 x = 70;
             }
         }
         this.start();
         player = new Player(id, "name"); // check if this is right
     }
 
     public static void main(String[] Args) {
         if (Args.length == 0)
             new Client("75.37.42.104", true);
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
             out = 
 new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
             in = 
  new BufferedReader(new InputStreamReader(server.getInputStream()));
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
                     if (temp[0].equals("Connection")) {
                         System.out.println(temp[1]);
                         if (!c.connected && 
                             temp[1].equals("Successfully connected.")) {
                             c.connected = true;
                             c.gui.setTitle("UCBang - " + c.name + 
                                            " - Connected to server on " + 
                                            server.getInetAddress());
                         } else if (!c.connected && 
                                    temp[1].equals("Name taken!")) {
                             System.out.println(this + 
                                                ": Connection refused because name was taken");
                             namesent = false;
                             c.promptName();
                         }
                     } else if (temp[0].equals("Chat")) {
                         c.gui.appendText(temp[1]);
                     } else if (temp[0].equals("Players")) {
                         String[] ppl = temp[1].split(",");
                         for (int i = 0; i < ppl.length; i++) {
                             if (ppl[i] != null && !ppl[i].isEmpty()) {
                                 c.players.add(new Player(c.players.size(), 
                                                          ppl[i]));
                             }
                         }
                     } else if (temp[0].equals("PlayerJoin")) {
                         c.players.add(new Player(c.players.size(), temp[1]));
                     } else if (temp[0].equals("PlayerLeave")) {
                         c.players.remove(temp[1]);
                     } else if (temp[0].equals("Prompt")) {
                         // received a prompt from host
                         if (temp[1].equals("Start")) { // will waiting for
                             // response here cause
                             // client to desync with
                             // server?
                             c.outMsgs.add("Prompt:" + 
                                           c.gui.promptYesNo("Host has sent a request to start game", 
                                                             "Start game?"));
                             c.gui.appendText("Host has requested the game be started", 
                                              Color.BLUE);
                         } else if (temp[1].equals("PlayCard")) {
                             c.gui.promptChooseCard(c.player.hand, "", "", 
                                                    true);
                             // c.outMsgs.add("Prompt:"+a);
                             //System.out.println("PLAYING CARD"+c.player.hand.get
                             // (a).name);
                         } else if (temp[1].equals("PlayCardUnforced")) {
                             c.gui.promptChooseCard(c.player.hand, "", "", 
                                                    false);
                             // c.outMsgs.add("Prompt:"+a);
                             // if(a>=0)
                             //System.out.println("PLAYING CARD"+c.player.hand.get
                             // (a).name);
                         } else if (temp[1].equals("ChooseCharacter")) {
                             c.gui.promptChooseCard(c.player.hand, "", "", 
                                                    true);
                         } else if (temp[1].equals("PickTarget")) {
                             System.out.println("I am player " + c.id + 
                                                ", prompting = " + c.prompting);
                             c.outMsgs.add("Prompt:" + (1 - c.id));
                         } else {
                             System.out.println("WTF do i do with " + temp[1]);
                         }
 
                     } else if (temp[0].equals("Draw")) {
                         String[] temp1 = temp[1].split(":");
                         int n = temp1.length;
                         if (Integer.valueOf(temp1[0]) == c.id) {
                             for (int m = 2; m < n; m++) {
                                 if (temp1[1].equals("Character")) {
                                     Card card = 
                                         new Card(Deck.Characters.valueOf(temp1[m]));
                                     c.player.hand.add(card);
                                     c.field.add(card, 
                                                 80 + (int)(400 * Math.random()), 
                                                 80 + 
                                                 (int)(400 * Math.random()));
                                 } else {
                                     Card card = 
                                         new Card(Deck.CardName.valueOf(temp1[m]));
                                     c.player.hand.add(card);
                                     c.field.add(card, 
                                                 80 + (int)(400 * Math.random()), 
                                                 80 + 
                                                 (int)(400 * Math.random()));
                                 }
                             }
                         } else {
                             c.gui.appendText("Player " + temp1[0] + " drew " + 
                                              temp1[1] + "cards.", Color.GREEN);
                         }
                         c.outMsgs.add("Ready");
                     } else if (temp[0].equals("GetInfo")) {
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
                         if (temp1[0].equals("newPlayer")) {
                             c.player = 
                                     new Player(Integer.valueOf(temp1[1]), c.name);
                             c.id = Integer.valueOf(temp1[1]);
                             c.numPlayers = Integer.valueOf(temp1[2]);
                         } else if (temp1[0].equals("role")) {
                             if (Integer.valueOf(temp1[1]) == c.id) {
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
                         } else if (temp1[0].equals("maxHP")) {
                             c.field.clear();
                             if (c.id == Integer.valueOf(temp1[1])) {
                                 c.player.maxLifePoints += 
                                         Integer.valueOf(temp1[2]);
                                 c.player.lifePoints = c.player.maxLifePoints;
                             } else {
                                 c.gui.appendText("Player " + temp1[1] + 
                                                  " has a maxHP of " + temp1[2], 
                                                  Color.RED);
                             }
                         } else if (temp1[0].equals("HP")) {
                             if (c.id == Integer.valueOf(temp1[1])) {
                                 c.player.lifePoints += 
                                         Integer.valueOf(temp1[2]);
                             } else {
                                 c.gui.appendText("Player " + temp1[1] + 
                                                  " life points changed by " + 
                                                  temp1[2], Color.RED);
                             }
                         } else if (temp1[0].equals("turn")) {
                             c.turn = Integer.valueOf(temp1[1]);
                             if (c.turn % c.numPlayers == c.id) {
                                 c.gui.appendText("It's your move!!!!!! Time to d-d-d-d-d-duel!", Color.CYAN);
                             }
                         } else if (temp1[0].equals("discard")) {
                             c.field.cards.remove(c.player.hand.get((int)Integer.valueOf(temp1[1])));
                             System.out.println("MOVED TO DISCARD:" + c.player.hand.remove((int)Integer.valueOf(temp1[1])).name);
                         } else if (temp1[0].equals("CardPlayed")) {
                             String s = "";
                             s = "Player " + temp1[1] + " played " + temp1[2] + (temp1.length == 4 ? " at player " + temp1[3] : "");
                             c.gui.appendText(s);
                         } else if (temp1[0].equals("id")) {
                             c.id = Integer.valueOf(temp1[1]);
 
                         } else if (temp1[0].equals("character")) {
                             if (Integer.valueOf(temp1[1]) == c.id){
                                 c.player.character = Integer.valueOf(temp1[2]);
                                 System.out.println("asdf");
                             }
                             else {
                                 c.gui.appendText("Player " + temp1[1] + " chose " + Deck.Characters.values()[Integer.valueOf(temp1[2])], Color.YELLOW);
                             }
                         } else {
                             System.out.println("WTF do i do with " + temp1[0] + ":" + temp1[1]);
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
