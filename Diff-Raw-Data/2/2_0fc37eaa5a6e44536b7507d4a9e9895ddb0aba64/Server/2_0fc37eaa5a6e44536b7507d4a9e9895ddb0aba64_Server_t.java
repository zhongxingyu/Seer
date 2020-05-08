 package netbang.network;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 
 import javax.swing.JOptionPane;
 
 import netbang.core.Bang;
 import netbang.core.Choice;
 import netbang.core.Player;
 
 
 public class Server extends Thread {
     public static void main(String Args[]) {
         new Server(12345, false);
     }
     protected LinkedHashMap<String, LinkedList<String>> messages = new LinkedHashMap<String, LinkedList<String>>();
     int numPlayers;
     ServerSocket me;
     /**
      * 1 = attempting to start game, 2 = game started for realz lawl
      */
     public int gameInProgress;
     /**
      * flag for whether people are still being prompted
      * for something. 0 = no, 1 = prompting with no
      * unchecked updates, 2 = unchecked prompt
      */
     public int prompting;
     /**
      * Stores player choices as received by the server.
      * The format is int[m][n], where m is player and n is option
      * 
      * <p>Actually, I'm not sure what that meant but it seems like it's not
      * really true. The 2D array stores  information on who was prompted and
      * what their response was. m is the player number in the order that they
      * were prompted, and array[m][0] gives the player number that the rest of
      * the game understands. array[m][1] gives their response status.
      * </p>
      */
     public ArrayList<Choice[]> choice;
 
     public int[][] ready;
     Bang game; // just insert game stuff here
     public LinkedList<String> names = new LinkedList<String>();
     ServerListAdder adder;
     long listLastUpdated;
 
     public boolean running;
     public ArrayList<Player> players = new ArrayList<Player>();
     public boolean lan = false;
 
     public Server(int port, boolean lan){
         this.lan = lan;
         running = true;
         if(!lan)
             adder = new ServerListAdder(JOptionPane.showInputDialog(null, "Input server name"));
         try {
             me = new ServerSocket(port);
             me.setSoTimeout(1000);
         } catch (IOException e) {
             System.err.println("Server Socket Error!\n" + e);
             e.printStackTrace();
             running = false;
         }
         print("Game server is listening to port " + port);
 
         this.start();    }
 
     /**
      * Sends a chat message to all players.
      * @param string The message to be sent to all players
      */
     void addChat(String string) {
         Iterator<String> keyter = messages.keySet().iterator();
         while (keyter.hasNext()) {
             messages.get(keyter.next()).add("Chat:" + string);
         }
     }
 
     /**
      * Notifies all clients of a new player joining the game
      * @param player The name of the new player
      */
     void playerJoin(String player) {
         names.add(player);
         Iterator<String> keyter = messages.keySet().iterator();
         while (keyter.hasNext()) {
             messages.get(keyter.next()).add("PlayerJoin:" + player);
         }
     }
 
     /**
      * Notifies all clients of a player leaving the game
      * @param player The name of the player who left
      */
     void playerLeave(String player) {
         names.remove(player);
         messages.remove(player);
         Iterator<String> keyter = messages.keySet().iterator();
         while (keyter.hasNext()) {
             messages.get(keyter.next()).add("PlayerLeave:" + player);
         }
     }
 
     void print(Object stuff) {
         System.out.println("Server:" + stuff);
     }
 
     /**
      * Sends a prompt to the specified player
      * <p>The valid prompts are</p>
      * <ul><li>PlayCard</li>
      * <li>PlayCardUnforced</li>
      * <li>Start</li>
      * <li>PickCardTarget</li>
      * <li>GeneralStore</li>
      * <li>ChooseCharacter</li>
      * <li>PickTarget</li>
      * </ul>
      * @param player The player to send the prompt to
      * @param s The prompt message
      */
     public void prompt(int player, String s) {
         if (prompting == 0) {
             prompting = 1;
         }
         System.out.println("Sending prompt to player "+player+" : "+s);
         messages.get(names.get(player)).add("Prompt:" + s);
     }
 
     /**
      * Sends the same prompt to all players
      * @param s The prompt message
      */
     public void promptAll(String s) {
         prompting = 1;
         choice.add(new Choice[numPlayers]);
         for (int n = 0; n < numPlayers; n++) {
             choice.get(choice.size() - 1)[n] = new Choice(n, -2);
         }
         for (int n = 0; n < numPlayers; n++) {
             prompt(n, s);
         }
     }
 
     /**
      * Prompt the players in the given int array.
      * @param p The players to prompt
      * @param s The prompt message
      */
     public void promptPlayers(int[] p, String s) {
         prompting = 1;
         choice.add(new Choice[p.length]);
         for (int n = 0; n < p.length; n++) {
             choice.get(choice.size() - 1)[n] = new Choice(p[n], -2);
         }
         for (int n:p) {
             prompt(n, s);
         }
     }
     /**
      * Prompt the player specified.
      * @param p The player to prompt
      * @param s The prompt message
      */
     public void promptPlayer(int p, String s) {
         prompting = 1;
         choice.add(new Choice[] { new Choice( p, -2 ) });
         prompt(p, s);
     }
     public void run() {
         while (running) {
             if (!lan&&System.currentTimeMillis() - listLastUpdated > 60000) {
                 listLastUpdated = System.currentTimeMillis();
                 adder.addToServerList();
             }
             if (gameInProgress == 0) {
                 try {
                     Socket client = me.accept();
                     if (client != null) {
                         new ServerThread(client, this);
                         numPlayers++;
                         if(!lan){
                             adder.setPlayers(numPlayers);
                             adder.addToServerList();
                         }
                     }
                 } catch (SocketTimeoutException e) {}
                 catch(SocketException e){
                     if(e.getMessage().equals("socket closed")){
 
                     }else{
                         e.printStackTrace();
                     }
                 }
                 catch (Exception e) {e.printStackTrace();
                 }
             } else {
                 // System.out.println("Has it been updated? "+prompting);
                 if (prompting == 2) {
                     boolean flag = true;
                     // System.out.println(choice.length + " " +
                     // choice[0].length);
                     for (int n = 0; n < choice.get(choice.size() - 1).length; n++) {
                         if (choice.get(choice.size() - 1)[n].choice == -2) {
                             flag = false;
                         }
                     }
                     System.out.println("Are we ready to move on? " + flag);
                     if (flag) {
                         prompting = 0;
                         // received all choices, send this to bang.java or w/e
                         if (gameInProgress == 1) {
                             choice.remove(choice.size() - 1);
                             gameInProgress++;
 
                             // check order of names
                             /*
                              * for(String s: names){ System.out.println(s); }
                              */
                             ready = new int[numPlayers][2];
 
                             // create player objects
                             for (int n = 0; n < names.size(); n++) {
                                 sendInfo(n, "SetInfo:newPlayer:" + n + ":"
                                         + numPlayers);
                             }
                             for (int n = 0; n < ready.length; n++) {
                                 ready[n][0] = n;
                                 ready[n][1] = 0;
                             }
                             game = new Bang(numPlayers, this);// FLAG: game
                             game.process();
                             if (!lan) {
                                 adder.setStarted(true);
                                 adder.addToServerList();
                             }
                         } else if (gameInProgress == 2) { // game has started
                             game.process(); // less bleh
                             gameInProgress++;
                         } else if (gameInProgress == 3) {
                             game.process();
                         }
                     } else {
                         // still prompting
                         prompting = 1;
                     }
                 }
             }
         }
         if(!lan)
             adder.RemoveFromServerList();
         System.out.println("Server shutting down");
         System.exit(0);
     }
 
     /**
      * Sends some character or game information to a player
      * @param player The player to send information to
      * @param info The information to send
      */
     public void sendInfo(int player, String info) { // info can be sent to
         // multiple people at the
         // same time, unlike prompts
         if (ready != null) {
             while (ready[player][1] > 0) {
             } // wait
             ready[player][1]++;
         } else {
             for (int n = 0; n < ready.length; n++) {
                 ready[n][0] = n;
                 if (n != player) {
                     ready[n][1] = 0;
                 } else {
                     ready[n][1] = 1;
                 }
             }
         }
         messages.get(names.get(player)).add(info);
     }
     /**
      * Sends some character or game information to all players
      * @param info The information to send
      */
     public void sendInfo(String info) {
         for (int n = 0; n < numPlayers; n++) {
             sendInfo(n, info);
         }
     }
     void startGame(int host, String name) {
         gameInProgress = 1;
         try {
             me.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
         prompting = 1;
         choice = new ArrayList<Choice[]>();
         choice.add(new Choice[numPlayers - 1]);
         for (int n = 0, m = 0; m < numPlayers - 1; n++, m++) {// this prompt
             // goes out to everyone except host
             if (n != host) {
                 choice.get(choice.size() - 1)[m] = new Choice(n, -2);
             } else
                 m--;
         }
         Iterator<String> keyter = messages.keySet().iterator();
         while (keyter.hasNext()) {
             String s = keyter.next();
             if (s != name)
                 messages.get(s).add("Prompt:Start");
         }
     }
 }
 
 class ServerThread extends Thread {
     // sends HashMap of stuff to clients, gets client's updated positions
     Socket client;
     BufferedReader in;
     BufferedWriter out;
     Server server;
     String name = "";
     int id;
     String buffer;
     boolean connected = false;
     LinkedList<String> newMsgs = new LinkedList<String>();
 
     public ServerThread(Socket theClient, Server myServer) {
         client = theClient;
         this.server = myServer;
         id = server.numPlayers;
         System.out.println("This is client id " + id);
         try {
             in = new BufferedReader(new InputStreamReader(client
                     .getInputStream()));
             out = new BufferedWriter(new OutputStreamWriter(client
                     .getOutputStream()));
         } catch (Exception e1) {
 
             e1.printStackTrace();
             try {
                 client.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         try {
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         this.start();
     }
 
     protected void finalize() throws Throwable {
         print(name + "(" + client.getInetAddress() + ") has left the game.");
         server.playerLeave(name);
         try {
             in.close();
             out.close();
             client.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     void print(Object stuff) {
         System.out.println("Server:" + stuff);
     }
 
     public synchronized void run() {
         while (!client.isClosed()&&server.running) {
             try {
                 if (in.ready()) {
                     buffer = (String) in.readLine();
                     String[] msgfields = buffer.split(":", 2);
                     String msgtype = msgfields[0];
                    if (msgtype.equals("Name")) {
                         processNameRequest(msgfields);
                     } else if(msgtype.equals("/quit")){
                         processQuitter();
                     } else if (msgtype.equals("Chat")) {
                         if (msgfields[1].charAt(0) == '/') {
                             parseSlashCommands(msgfields);
                         } else
                             server.addChat(name + ": " + msgfields[1]);
                     } else if (msgtype.equals("Prompt")) {
                         processPrompt(msgfields);
                     } else if (msgtype.equals("Ready")) {
                         if (server.ready != null) {
                             server.ready[id][1]--;
                         } else {
                             System.out.println("ERROR: Ready for what?");
                         }
                     } else {
                         System.out.println("Error: Junk String received:"
                                 + buffer);
                     }
                 }
                 if (!newMsgs.isEmpty()) {
                     Iterator<String> iter = ((LinkedList<String>) newMsgs
                             .clone()).iterator();
                     while (iter.hasNext()) {
                         out.write(iter.next());
                         out.newLine();
                         iter.remove();
                     }
                     newMsgs.clear(); // will this still produce CME?
                 }
                 out.flush();
                 sleep(10); // is this needed?
             } catch (Exception e) {
                 if (e != null && e.getMessage() != null
                         && e.getMessage().equals("Connection reset"))
                     try {
                         finalize();
                     } catch (Throwable t) {
                         t.printStackTrace();
                     }
                     else
                         e.printStackTrace();
             }
         }
         System.out.println("Tried to quit");
         try {
             this.finalize();
         } catch (Throwable e) {
             e.printStackTrace();
         }
     }
 
 	/**
 	 * @param msgfields
 	 */
 	private void processPrompt(String[] msgfields) {
 		if (server.prompting >= 1) {
 		    int n;
 		    // if(id>server.choice.length)
 		    for (n = 0; server.choice.get(server.choice.size() - 1)[n].playerid != id || 
 		    	(server.choice.get(server.choice.size() - 1)[n].playerid == id && 
 		    	server.choice.get(server.choice.size() - 1)[n].choice>-1); n++) {
 		    }//TODO: HUH?! Why is this dead loop here? O.o I split it across 3 lines for readability
 		    server.choice.get(server.choice.size() - 1)[n].choice = Integer.valueOf(msgfields[1]);
 		    System.out.println("Player "+n+" returned "+msgfields[1]);
 		    server.prompting = 2;
 		} else {
 		    System.out.println("Received prompt from player when not prompted!");
 		}
 	}
 
 	/**
 	 * Processes someone leaving
 	 */
 	private void processQuitter() {
 		if(isClientLocalhost()){
 		    server.running=false;
 		    System.out.println("Server shutting down");
 		}
 		else{
 		    server.sendInfo(("PlayerLeave:"+name));
 		}
 	}
 
 	/**
 	 * This method is an abstraction to check whether the client is at localhost.
 	 * @return Returns whether the client attached to this thread is on localhost
 	 */
 	private boolean isClientLocalhost() {
 		return client.getInetAddress().isLoopbackAddress();
 	}
 
 	/**
 	 * @param msgfields
 	 * @throws IOException
 	 */
 	private void processNameRequest(String[] msgfields) throws IOException {
 		if (!connected) {// player was never connected
 		    if (server.messages.containsKey(msgfields[1])) {
 		        out.write("Connection:Name taken!");
 		        out.newLine();
 		        out.flush();
 		        print(client.getInetAddress()
 		                + " Attempting joining with taken name.");
 		    } else {
 		        name = msgfields[1];
 		        print(name + "(" + client.getInetAddress()
 		                + ") has joined the game.");
 		        server.playerJoin(name);
 		        server.messages.put(name, newMsgs);
 		        out.write("Connection:Successfully connected.");
 		        out.newLine();
 		        out.flush();
 		        Object[] players = server.messages.keySet().toArray();
 		        out.write("Players:");
 		        String wr=(String)players[0];
 		        for(int n = 1; n<players.length; n++) {// give player list
 		            wr+=","+(String)players[n];
 		        }
 		        out.write(wr);
 		        System.out.println("PLAYERS LIST IS NOW "+ wr);
 		        out.newLine();
 		        out.flush();
 		    }
 		}
 	}
 
 	/**
 	 * @param msgfields
 	 * @throws IOException
 	 */
 	private void parseSlashCommands(String[] msgfields) throws IOException {
 		// TODO: Send commands
 		if (msgfields[1].equals("/start")&&(id==0 || isClientLocalhost()) &&
 				server.gameInProgress == 0){
 		    server.startGame(id, name);
 		}
 		else if (msgfields[1].startsWith("/rename")) {
 		    if (server.gameInProgress == 2) {
 		        System.out.println("I'm sorry, Dave.");
 		    } else if (msgfields[1].length() > 7
 		            && msgfields[1].charAt(7) == ' ') {
 		        String temp1 = msgfields[1].split(" ", 2)[1];
 		        if (server.messages.containsKey(temp1)) {
 		            out.write("Connection:Name taken!");
 		            out.newLine();
 		            out.flush();
 		            print(name + "(" + client.getInetAddress()
 		                    + ") Attempting renaming to taken name.");
 		        } else {
 		            print(name + "(" + client.getInetAddress()
 		                    + ") is now known as " + temp1);
 		            server.messages.remove(name);
 		            server.messages.put(temp1, newMsgs);
 		            server.playerLeave(name);
 		            server.playerJoin(temp1);
 		            System.out.println("hi");
 		            name = temp1;
 		            out.write("Connection:Successfully renamed.");
 		            out.newLine();
 		            out.flush();
 		        }
 		    } else {
 		        // TODO: (Optional) create /help RENAME
 		        // TODO: Create /help commands
 		    }
 		} else if (msgfields[1].startsWith("/prompting")) {
 		    System.out.println("Prompting is "
 		            + server.prompting + " "
 		            + server.gameInProgress);
 		}
 	}
 }
