 package server;
 
 import java.awt.Point;
 
 import card.Deck;
 import client.Player;
 
 class FragServer {
    final static int port = 9876;
    final static int maxPlayers = 2;
    static int currentPlayers = 0;
    static int currentPlayer = 0;
    static Player[] players = new Player[maxPlayers];
    static boolean displayPlayers = true;
    static int[] dice = new int[maxPlayers];
    static int numDice = 0;
    
    static Deck weapon = produceWeaponCards(34);
    static Deck gadget = produceGadgetCards(56);
    static Deck frag = produceFragCards(22);
    
    public static void main(String args[]) throws Exception {
       weapon.shuffle();
       gadget.shuffle();
       frag.shuffle();
 
       ServerConnection clients = new ServerConnection(port);
       while(true) {
          String message = clients.receivePacket();
          message = message.trim();
          //name health speed accuracy x-coordinate y-coordinate
          if(message.matches("^\\w+ \\d \\d \\d \\d+ \\d+")) {
             System.out.println(message);
             if(currentPlayers < maxPlayers) {
                String[] playerArr = message.split("\\s");
                
                boolean isFound = false;
                for(int i=0; i<currentPlayers; i++) {
                   if(playerArr[0].equals(players[i].getName())) {
                      isFound = true;
                   }
                }
                
                if(!isFound) {
                   players[currentPlayers] = new Player(playerArr[0],
                      Integer.parseInt(playerArr[1]),
                      Integer.parseInt(playerArr[2]),
                      Integer.parseInt(playerArr[3]));
                   Point p = new Point(Integer.parseInt(playerArr[4]), 
                      Integer.parseInt(playerArr[5]));
                   players[currentPlayers].setLocation(p);
                
                   players[currentPlayers].setIP(clients.getLastClientIP());
                
                   currentPlayers++;
                }
             }
             
             /* This block sends the movement to all other players */
             String[] playerArr = message.split("\\s");
             
             int playerIndex;
             for(int i=0; i<currentPlayers; i++) {
                if(playerArr[0].equals(players[i].getName())) {
                   playerIndex = i;
                }
             }
             
             for(int i=0; i<currentPlayers; i++) {
                if(!playerArr[0].equals(players[i].getName())) {
                   System.out.println(playerArr[0] + " => " + 
                         players[i].getName());
 
                   clients.sendPacket(players[i].getIP(), message);
                }
             }
          }
          /* Read in the end of the turn */
          else if(message.matches("^\\w+ end of turn")) {
             if(currentPlayer == maxPlayers-1) {
                currentPlayer = 0;
             }
             else {
                currentPlayer++;
             }
             
             System.out.println(players[currentPlayer].getName() + " begin turn");
             clients.sendPacket(players[currentPlayer].getIP(),
                   players[currentPlayer].getName() + " begin turn");
          }
          /* Read in roll amount */ 
          else if(message.matches("^\\w+ rolled \\d+")) {
             System.out.println(message);
             String[] rolledArr = message.split("\\s");
             
             for(int i=0; i < currentPlayers; i++) {
                if(rolledArr[0].equals(players[i].getName())) {
                   dice[i] = Integer.parseInt(rolledArr[2]);
                   numDice++;
                }
             }
             
             if(numDice == maxPlayers) {
                int biggestIndex = 0;
                int maxRoll = 0;
                for(int i=0; i < maxPlayers; i++) {
                   System.out.println(dice[i] + " > " + maxRoll);
                   if(dice[i] > maxRoll) {
                      biggestIndex = i;
                      maxRoll = dice[i];
                   }
                }
                
                System.out.println(players[biggestIndex].getName() + 
                      " begin turn");
                clients.sendPacket(players[biggestIndex].getIP(),
                      players[biggestIndex].getName() + " begin turn");
                currentPlayer = biggestIndex;
                numDice = 0;
             }
          }
          else if(message.matches("^\\w+ request gadget")) {
             String[] playerArr = message.split("\\s");
             
             int index = -1;
             for(int i=0; i<currentPlayers; i++) {
                if(playerArr[0].equals(players[i].getName())) {
                   index = i;
                   break;
                }
             }
             
            int gadgetCard = gadget.drawCard();
            System.out.println( players[index].getName() + " gadget " 
                  + gadgetCard);
            
             clients.sendPacket(players[index].getIP(),
                  players[index].getName() + " gadget " + gadgetCard);
          }
          else if(message.matches("^\\w+ request weapon")) {
             String[] playerArr = message.split("\\s");
             
             int index = -1;
             for(int i=0; i<currentPlayers; i++) {
                if(playerArr[0].equals(players[i].getName())) {
                   index = i;
                   break;
                }
             }
             
            int weaponCard = gadget.drawCard();
            System.out.println( players[index].getName() + " weapon " 
                  + weaponCard);
            
             clients.sendPacket(players[index].getIP(),
                  players[index].getName() + " weapon " + weaponCard);
          }
          else if(message.matches("^\\w+ request frag")) {
             String[] playerArr = message.split("\\s");
             
             int index = -1;
             for(int i=0; i<currentPlayers; i++) {
                if(playerArr[0].equals(players[i].getName())) {
                   index = i;
                   break;
                }
             }
            
            int fragCard = gadget.drawCard();
            System.out.println( players[index].getName() + " frag " 
                  + fragCard);
            
             clients.sendPacket(players[index].getIP(),
                  players[index].getName() + " frag " + fragCard);
          }
       }
    }
    
    
    public static Deck produceGadgetCards (int length) {
       Deck gadget = new Deck(length);
 
       gadget.cards[0] = 1;
       gadget.cards[1] = 1;
       gadget.cards[2] = 1;
       gadget.cards[3] = 1;
       gadget.cards[4] = 1;
       gadget.cards[5] = 1;
       gadget.cards[6] = 1;
       gadget.cards[7] = 1;
       gadget.cards[8] = 1;
       gadget.cards[9] = 1;
       gadget.cards[10] = 1;
       gadget.cards[11] = 2;
       gadget.cards[12] = 2;
       gadget.cards[13] = 2;
       gadget.cards[14] = 2;
       gadget.cards[15] = 2;
       gadget.cards[16] = 2;
       gadget.cards[17] = 3;
       gadget.cards[18] = 3;
       gadget.cards[19] = 3;
       gadget.cards[20] = 3;
       gadget.cards[21] = 3;
       gadget.cards[22] = 4;
       gadget.cards[23] = 4;
       gadget.cards[24] = 4;
       gadget.cards[25] = 4;
       gadget.cards[26] = 4;
       gadget.cards[27] = 5;
       gadget.cards[28] = 5;
       gadget.cards[29] = 5;
       gadget.cards[30] = 5;
       gadget.cards[31] = 5;
       gadget.cards[32] = 6;
       gadget.cards[33] = 6;
       gadget.cards[34] = 6;
       gadget.cards[35] = 7;
       gadget.cards[36] = 7;
       gadget.cards[37] = 7;
       gadget.cards[38] = 8;
       gadget.cards[39] = 8;
       gadget.cards[40] = 8;
       gadget.cards[41] = 9;
       gadget.cards[42] = 9;
       gadget.cards[43] = 10;
       gadget.cards[44] = 10;
       gadget.cards[45] = 11;
       gadget.cards[46] = 11;
       gadget.cards[47] = 12;
       gadget.cards[48] = 12;
       gadget.cards[49] = 13;
       gadget.cards[50] = 14;
       gadget.cards[51] = 15;
       gadget.cards[52] = 16;
       gadget.cards[53] = 17;
       gadget.cards[54] = 18;
       gadget.cards[55] = 19;
       
       return gadget;
    }
    
    public static Deck produceWeaponCards (int length) {
       Deck weapon = new Deck(length);
       
       weapon.cards[0] = 20;
       weapon.cards[1] = 20;
       weapon.cards[2] = 20;
       weapon.cards[3] = 20;
       weapon.cards[4] = 20;
       weapon.cards[5] = 21;
       weapon.cards[6] = 21;
       weapon.cards[7] = 21;
       weapon.cards[8] = 21;
       weapon.cards[9] = 22;
       weapon.cards[10] = 22;
       weapon.cards[11] = 22;
       weapon.cards[12] = 22;
       weapon.cards[13] = 23;
       weapon.cards[14] = 23;
       weapon.cards[15] = 23;
       weapon.cards[16] = 23;
       weapon.cards[17] = 24;
       weapon.cards[18] = 24;
       weapon.cards[19] = 24;
       weapon.cards[20] = 25;
       weapon.cards[21] = 25;
       weapon.cards[22] = 25;
       weapon.cards[23] = 26;
       weapon.cards[24] = 26;
       weapon.cards[25] = 27;
       weapon.cards[26] = 27;
       weapon.cards[27] = 28;
       weapon.cards[28] = 28;
       weapon.cards[29] = 29;
       weapon.cards[30] = 29;
       weapon.cards[31] = 30;
       weapon.cards[32] = 31;
       weapon.cards[33] = 32;
       
       return weapon;
    }
    
    public static Deck produceFragCards (int length) {
       Deck frag = new Deck(length);
       
       frag.cards[0] = 33;
       frag.cards[1] = 33;
       frag.cards[2] = 34;
       frag.cards[3] = 34;
       frag.cards[4] = 35;
       frag.cards[5] = 35;
       frag.cards[6] = 36;
       frag.cards[7] = 36;
       frag.cards[8] = 37;
       frag.cards[9] = 37;
       frag.cards[10] = 38;
       frag.cards[11] = 39;
       frag.cards[12] = 40;
       frag.cards[13] = 41;
       frag.cards[14] = 42;
       frag.cards[15] = 43;
       frag.cards[16] = 44;
       frag.cards[17] = 45;
       frag.cards[18] = 46;
       frag.cards[19] = 47;
       frag.cards[20] = 48;
       frag.cards[21] = 49;
       
       return frag;
    }
 }
