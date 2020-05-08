 package mel.fencing.server;
 
 public class Game
 {
     //TODO RFI consider using powers of two so one bit each for color, canAdvance/Attack, canRetreat, and canParry 
     public static final int COLOR_NONE = -10;
     public static final int COLOR_WHITE = 0;
     public static final int COLOR_BLACK = 10;
     public static final int TURN_MOVE = 0;
     public static final int TURN_PARRY = 1;
     public static final int TURN_PARRY_OR_RETREAT = 2;
     public static final int TURN_BLACK_MOVE =               COLOR_BLACK+TURN_MOVE;
     public static final int TURN_BLACK_PARRY =              COLOR_BLACK+TURN_PARRY;
     public static final int TURN_BLACK_PARRY_OR_RETREAT =   COLOR_BLACK+TURN_PARRY_OR_RETREAT; 
     public static final int TURN_WHITE_MOVE =               COLOR_WHITE+TURN_MOVE;
     public static final int TURN_WHITE_PARRY =              COLOR_WHITE+TURN_PARRY;
     public static final int TURN_WHITE_PARRY_OR_RETREAT =   COLOR_WHITE+TURN_PARRY_OR_RETREAT; 
     public static final int TURN_GAME_OVER = -1;
     
     UserSession black;
     UserSession white;
     Deck deck;
     Hand whiteHand = new Hand();
     Hand blackHand = new Hand();
     int blackHP = 5;
     int whiteHP = 5;
     int blackpos = 23;
     int whitepos = 1;
     int turn = TURN_WHITE_MOVE;
     int parryVal = -1;
     int parryCount = -1;
     boolean finalParry = false;
     
     private Game(UserSession challenger, UserSession target)
     {
         black = challenger;
         white = target;
         deck = new Deck();
         deck.shuffle();
         black.setGame(this);
         black.setColor(COLOR_BLACK);
         white.setGame(this);
         white.setColor(COLOR_WHITE);
         sendNames();
         blackHand.fill(deck);
         sendBlackHand();
         whiteHand.fill(deck);
         sendWhiteHand();
     }
     
     public static Game newGame(UserSession challenger, UserSession target)
     {
         return new Game(challenger, target);
     }
     
     private void sendWhiteHand() { white.send(whiteHand.toString()); }
     private void sendBlackHand() { black.send(blackHand.toString()); }
     
     private void sendNames()
     {
         white.send("w"+black.getUsername());
         black.send("b"+white.getUsername());
     }
     
     private final int playerColor(UserSession player)
     {
         if(player == white) return COLOR_WHITE;
         if(player == black) return COLOR_BLACK;
         return COLOR_NONE;
     }
     
     synchronized void jumpAttack(UserSession player, String values)
     {
         if(values.length() != 3) send(player, "ESyntax error in attack:"+values);
         int distance = parseDigit(values.charAt(0));
         int value = parseDigit(values.charAt(1));
         int count = parseDigit(values.charAt(2));
         if(value<0 || count<1) send(player, "ESyntax error in attack:"+values);
         
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_MOVE) 
         {
             if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before attacking");
             else send(player, "ENot your turn to attack");
             return;
         }
         
         Hand hand = handOf(color);
         if(!hand.hasCardWithCards(distance, value, count))
         {
             send(player, "EYou don't have the cards to jump-attack ("+distance+":"+value+","+count+")");
             return;
         }
         
         if(blackpos - whitepos != (distance+value))
         {
             send(player, "EYou are the wrong distance to jump-attack with a "+distance+" and "+value);
             return;
         }
         
         parryVal = value;
         parryCount = count; 
         movePosOf(color, distance);        
         hand.removeByValue(distance);
         hand.removeByValue(value, count);
         hand.fill(deck);
         if(deck.isEmpty()) { finalParry = true; notifyFinalParry(); }
         turn = otherColor(color)+TURN_PARRY_OR_RETREAT;
         
         notifyPositions();
         notifyHand(player, hand);
         notifyAttack(value, count, distance);
         notifyTurn();
     }
     
     private void notifyAttack(int value, int count, int distance)
     {
         sendAll("a"+value+""+count+""+distance);
     }
     
     private void notifyFinalParry()
     {
         sendAll("f");
     }
 
     synchronized void standingAttack(UserSession player, String values)
     {
         if(values.length() != 2) send(player, "ESyntax error in attack:"+values);
         int value = parseDigit(values.charAt(0));
         int count = parseDigit(values.charAt(1));
         if(value<0 || count<1) send(player, "ESyntax error in attack:"+values);
         
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_MOVE) 
         {
             if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before attacking");
             else send(player, "ENot your turn to attack");
             return;
         }
 
         Hand hand = handOf(color);
         if(!hand.hasCards(value, count))
         {
             send(player, "EYou don't have the cards to attack ("+value+","+count+")");
             return;
         }
         
         if(blackpos - whitepos != value)
         {
             send(player, "EYou are the wrong distance to attack with a "+value);
             return;
         }
         
         parryVal = value;
         parryCount = count;
         hand.removeByValue(value, count);
         hand.fill(deck);
         
         if(checkmate(color)) { notifyCannotParry(color); turn = TURN_GAME_OVER; }
         else 
         {
             if(deck.isEmpty()) { finalParry = true; notifyFinalParry(); }
             turn = otherColor(color)+TURN_PARRY;
         }
         
         notifyHand(player, hand);
         notifyAttack(value, count, 0);
         notifyTurn();        
     }
     
     private void notifyCannotParry(int color)
     {
         sendAll(color == Game.COLOR_WHITE ? "A1" : "B1");
     }
 
     private boolean checkmate(int color)
     {
         Hand hand = handOf(otherColor(color));
        return !hand.hasCards(parryVal, parryCount);
     }
 
     synchronized void move(UserSession player, String values)
     {
         if(values.length() != 1) send(player, "ESyntax error in advance:"+values);
         int distance = parseDigit(values.charAt(0));
         if(distance < 0) send(player, "ESyntax error in advance:"+values);
         
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_MOVE) 
         {
             if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before advancing");
             else send(player, "ENot your turn to advance");
             return;
         }
  
         if(blackpos-whitepos<=distance)
         {
             send(player, "EMay not move through other fencer");
             return;
         }
         
         Hand hand = handOf(color);
         if(!hand.hasCard(distance))
         {
             // hacked or buggy client
             send(player, "EYou don't have the advance card "+distance);
             return;
         }
 
         movePosOf(color, distance);        
         hand.removeByValue(distance);
         hand.fill(deck);
         if(deck.isEmpty()) { endGame(); return; }
         
         turn = otherColor(color)+TURN_MOVE;
         notifyPositions();
         notifyHand(player, hand);
         notifyMove(distance);
         notifyTurn();
     }
 
     private void notifyMove(int distance)
     {
         sendAll("m"+distance);
     }
     
     private void notifyRetreat(int distance)
     {
         sendAll("r"+distance);
     }
     
     synchronized void retreat(UserSession player, String values)
     {
         if(values.length() != 1) send(player, "ESyntax error in retreat:"+values);
         int distance = parseDigit(values.charAt(0));
         if(distance < 0) send(player, "ESyntax error in retreat:"+values);
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_PARRY_OR_RETREAT && turn != color+TURN_MOVE)
         {
             if(turn == color+TURN_PARRY) send(player, "EYou cannot retreat from a standing attack");
             else send(player, "ENot your turn to retreat");
             return;
         }
         
         Hand hand = handOf(color);
         if(!hand.hasCard(distance))
         {
             // hacked or buggy client
             send(player, "EYou don't have the advance card "+distance);
             return;
         }
         
         movePosOf(color, -distance);
         hand.removeByValue(distance);
         hand.fill(deck);
         if(deck.isEmpty() || fencerOffStrip()) { endGame(); return; }
         
         turn = otherColor(color)+TURN_MOVE;
         if(finalParry) endGame();
         notifyPositions();
         notifyHand(player, hand);
         notifyRetreat(distance);
         notifyTurn();
     }
     
     synchronized void parry(UserSession player)
     {
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_PARRY_OR_RETREAT && turn != color+TURN_PARRY)
         {
             if(turn == color+TURN_MOVE) send(player, "EThere is no attack to parry");
             else send(player, "ENot your turn to parry");
             return;
         }
         
         Hand hand = handOf(color);
         if(!hand.hasCards(parryVal, parryCount))
         {
             send(player, "EYou don't have the cards to parry ("+parryVal+","+parryCount+")");
             return;
         }
         
         
         hand.removeByValue(parryVal, parryCount);
         turn = color+TURN_MOVE;
         sendAll("q"); // notify parry occured
         if(finalParry) endGame();
         notifyTurn();
     }
     
     private void endGame()
     {
         turn = TURN_GAME_OVER;
         if(whitepos < 1)  { sendAll("B0"); return; }
         if(blackpos > 23) { sendAll("A0"); return; }
         
         int finalDistance = blackpos - whitepos;
         int whiteCount = whiteHand.countCards(finalDistance);
         int blackCount = blackHand.countCards(finalDistance);
         if(whiteCount > blackCount) { sendAll("A2"); return; }
         if(blackCount > whiteCount) { sendAll("B2"); return; }
         if(12-whitepos > blackpos-12) { sendAll("B3"); return; }
         if(12-whitepos < blackpos-12) { sendAll("A3"); return; }
         sendAll("X");
         Server.lobby.removeFromGame(black);
         Server.lobby.removeFromGame(white);
     }
     
     private final boolean fencerOffStrip()
     {
         return whitepos < 1 || blackpos > 23;
     }
     
     private void send(UserSession who, String what)
     {
         if(who != null) who.send(what);
     }
     
     private void sendAll(String what)
     {
         send(white, what);
         send(black, what);
     }
     
     static private final int parseDigit(char in)
     {
         if(in<'0' || in > '9') return -1;
         return in-'0';
     }
     
     private void notifyPositions()
     {
         StringBuilder sb = new StringBuilder(3);
         sb.append("x");
         sb.append((char)('a'+whitepos-1));
         sb.append((char)('a'+blackpos-1));
         sendAll(sb.toString());
     }
     
     private final void notifyHand(UserSession player, Hand hand)
     {
         send(player, hand.toString());
     }
     
     private void notifyTurn()
     {
         sendAll("t"+turn);
     }
     
     private final int otherColor(int color)
     {
         if(color == COLOR_WHITE) return COLOR_BLACK;
         if(color == COLOR_BLACK) return COLOR_WHITE;
         return COLOR_NONE;
     }
     
     private final Hand handOf(int color)
     {
         if(color == COLOR_WHITE) return whiteHand;
         if(color == COLOR_BLACK) return blackHand;
         return null;
     }
     
     private final void movePosOf(int color, int offset)
     {
         if(color == COLOR_WHITE) whitepos += offset;
         if(color == COLOR_BLACK) blackpos -= offset;
     }
 }
