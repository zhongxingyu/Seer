 import java.io.*;
 import java.util.*;
 
 /**
  * @author Sebastian Olsson
  * @version 2013-10-27
  */
 public class MasterThread extends Thread {
 
     private Set<ServerThread> connections;
     private Set<Game> games;
 
     /**
      * Skapar en tråd för ändring av bankens välkomstbanner.
      */
     public MasterThread() {
         connections = new HashSet<ServerThread>();
         games = new HashSet<Game>();
     }
 
     /**
      * Handles the communication between clients.
      */
     public void run(){
         int clientCount = -1;
         while(true){
             Iterator<ServerThread> it = connections.iterator();
             while(it.hasNext()){
                 ServerThread conn = it.next();
                 if(!conn.isAlive()){
                     it.remove();
                     continue;
                 }
                 if(conn.hasRequest()){
                     processRequest(conn);
                 }
             }
             if(clientCount != connections.size()){
                 clientCount = connections.size();
                 System.out.println("There are currently "+clientCount+" clients connected.");
             }
             int millisecondDelay = 50;
             try{Thread.sleep(millisecondDelay);}catch(InterruptedException e){}
         }
     }
 
     /**
      * Process the oldest request from the server thread.
      */
     private void processRequest(final ServerThread conn){
         String request = conn.pollRequest();
         String[] tokens = request.split("\\s+");
         if(tokens.length == 1){
             if(tokens[0].equals("list")){
                 String listResponse = listResponse();
                 conn.sendReg(listResponse);
                 conn.printComm("sendReg",listResponse);
             }else if(tokens[0].equals("exit")){
                 tryQuittingGame(conn.clientName);
             }
         }else if(tokens.length == 2){
             if(tokens[0].equals("setname")){
                 String proposedName = tokens[1];
                 if(playerNameIsAvailable(proposedName)){
                     conn.clientName = proposedName;
                 }else{
                     conn.sendReg("error That name is unavailable.");
                     conn.printComm("sendReg","error That name is unavailable.");
                 }
             }else if(tokens[0].equals("host")){
                 String proposedName = tokens[1];
                 if(gameNameIsAvailable(proposedName)){
                     Game game = new Game(proposedName,conn);
                     games.add(game);
                     conn.sendReg("ok");
                     conn.printComm("sendReg","ok");
                 }else{
                     conn.sendReg("error That name is unavailable.");
                     conn.printComm("sendReg","error That name is unavailable.");
                 }
             }else if(tokens[0].equals("join")){
                 String proposedName = tokens[1];
                 if(!gameNameIsAvailable(proposedName)){
                     Game joinedGame = null;
                     for(Game game : games){
                         if(game.gameName.equalsIgnoreCase(proposedName)){
                             joinedGame = game;
                             break;
                         }
                     }
                     if(joinedGame == null){
                         String msg = "error There is no game with that name.";
                         conn.sendReg(msg);
                         conn.printComm("sendReg",msg);
                     }else{
                         joinedGame.join(conn);
                         conn.sendReg("ok");
                         conn.printComm("sendReg","ok");
                     }
                 }
             }else if(tokens[0].equals("move")){
                 //TODO
                 
                 //Check if user is in a game. If no: do nothing.
                 //If yes, parse the parameters to an integer vector.
                 Vector<Integer> positions = new Vector<Integer>();
                 int player;
                 //player should be either 1 or 10 (Constants.CELL_RED or
                 //Constants.CELL_WHITE) depending on what player requested the move.
                 try {
 		            for (int i = 1;i<tokens.length;i++) {
 		            	positions.add(Integer.parseInt(tokens[i]));
 		            }
                 } catch (Exception e) {
                 	//Probably because the arguments were not valid integers.
                 	//Notify player somehow
                 }
                 
                 //Find the correct game and call:
                 //correctGame.makeMove(player, positions);
                                 
             }else if(tokens[0].equals("chat")){
                 //TODO
                 
                 //Check if user is in a game .
                 //If yes, send the message to the correct opponent.
                 //If not in a game: do nothing for now.
                 //Might implement lobby chat in the future, though.
                 
                
                
                
             }else{
                 throw new IllegalArgumentException("Request to master thread was not recognized.");
             }
         }else{
             System.err.println("Could not parse request!");
         }
         wake(conn);
         System.out.println("exiting processReq");
     }
 
     /**
      * Make the player identified by the parameter quit a game, if s/he is part of one.
      */
     private void tryQuittingGame(String clientName){
         Iterator<Game> it = games.iterator();
         while(it.hasNext()){
             Game game = it.next();
             if(game.hosterName().equals(clientName)){
                 wake(game.hoster);
                 game.hoster.sendReg("ok");
                 it.remove();
                 break;
             }else if(game.joinerName().equals(clientName)){
                it.remove();
                game.joiner.sendReg("ok");
                 wake(game.joiner);
                 break;
             }
         }
     }
 
     /**
      * @return A response representing a list of all available hosted games.
      */
     private String listResponse(){
         int numSessions = games.size();
         String list = "sessions "+numSessions;
         for(Game game : games){
             list += " "+game.gameName+" "+game.hosterName()+" "+game.joinerName();
         }
         return list;
     }
 
     /**
      * Wake the thread up.
      */
     private void wake(ServerThread conn){
         synchronized(conn){
             conn.notify();
         }
     }
 
     /**
      * @return true if no other client uses that name.
      */
     private boolean playerNameIsAvailable(String name){
         for(ServerThread conn : connections){
             if(conn.clientName.equalsIgnoreCase(name)){
                 return false;
             }
         }
         return true;
     }
 
     /**
      * @return true if no other game session uses that name.
      */
     private boolean gameNameIsAvailable(String name){
         for(Game game : games){
             if(game.gameName.equalsIgnoreCase(name)){
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Starts a connection and keeps track of it.
      */
     public void addConnection(ServerThread connection){
         connection.start();
         connections.add(connection);
     }
 }
