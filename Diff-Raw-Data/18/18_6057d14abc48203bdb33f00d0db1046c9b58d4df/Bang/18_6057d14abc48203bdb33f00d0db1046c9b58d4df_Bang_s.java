 package ucbang.core;
 
 import java.util.ArrayList;
 
 import ucbang.network.Server;
 
 public class Bang {
     Server server;
 
     public Player[] players;
     public int numPlayers;
     public int turn;
     public Deck deck;
 
     public int sheriff;
 
     public Bang(int p, Server s) {
         server = s;
         numPlayers = p;
         turn = -2;
     }
 
     /**
      * Helper method to remove some conditions from server and put them here,
      * where it makes more sense
      */
     public void process() {
         if (turn == -2) {
             start();
             turn++;
         } else if (turn == -1) {
             start2();
         } else {
             System.out.println("PROCESS");
             if (server.choice.size() >= 1) {
                 if (server.choice.size() == 1) {
                     System.out.println("You played " + 
                                        server.choice.get(0)[0][1] + 
                                        ". You have " + 
                                        (players[server.choice.get(0)[0][0]].hand.size() - 
                                         1) + " cards left in your hand.");
                 }
                 if (server.choice.get(0)[0][1] != -1) {
                     if (getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).target == 
                         2 && 
                         (getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).type == 
                          3? 
                          getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).location ==
                           1:true)) {
                         if (server.choice.size() == 1) {
                             {
                                 if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.STEAL.ordinal() ||
                                  getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DISCARD.ordinal()){
                                     server.prompt(turn % numPlayers, "PickCardTarget", true);
                                 }
                                 else
                                     server.prompt(turn % numPlayers, "PickTarget", true);
                                 System.out.println("pick target....");
                                 return;
                             }
                         } else if (server.choice.size() == 2) {
                             if (server.choice.get(1)[0][1] == -1) {
                                 System.out.println("Cancelled");
                                 server.choice.remove(server.choice.size() - 1);
                             } else if (isCardLegal(getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]), players[server.choice.get(0)[0][0]], players[server.choice.get(1)[0][1]])) {
                                 if (getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).discardToPlay == true) {
                                     if (players[server.choice.get(0)[0][0]].hand.size() > 1) {
                                         if (server.choice.get(1).length == 1) { //has not been asked to discard yet
                                             System.out.println("THIS CARD REQUIRES A DISCARD TO PLAY AS WELL");
                                             server.choice.set(1, new int[][] { server.choice.get(1)[0], { server.choice.get(0)[0][0], -2 } }); //TODO: obsolete?
                                             server.prompt(server.choice.get(0)[0][0], 
                                                           "PlayCard", 
                                                           false); //TODO: play card from hand only
                                             System.out.println(server.choice.size() + 
                                                                " " + 
                                                                server.choice.get(1).length);
                                             return;
                                         }
                                         if (server.choice.get(1).length ==  2) { //has  been asked to discard
                                             if (server.choice.get(1)[1][1] == 
                                                 server.choice.get(0)[0][1]) {
                                                 System.out.println("Cannot discard the card you are playing");
                                                 server.choice.set(1, 
                                                                   new int[][] { server.choice.get(1)[0], 
                                                                                 { server.choice.get(0)[0][0], 
                                                                                   -2 } });
                                                 server.prompt(server.choice.get(0)[0][0], 
                                                               "PlayCard", 
                                                               false); //TODO: play card from hand only
                                                 return;
                                             } else {
                                                 System.out.println("DISCARD OK.");
                                                 //may need to reorder the cards;
                                                 if (server.choice.get(0)[0][1] > 
                                                     server.choice.get(1)[1][1]) {
                                                     server.choice.get(0)[0][1]--;
                                                 }
                                                 playerDiscardCard(server.choice.get(0)[0][0], 
                                                                   server.choice.get(1)[1][1], true);
                                             }
                                         }
                                     } else {
                                         server.choice.remove(server.choice.size() - 
                                                              1);
                                         server.prompt(turn % numPlayers, 
                                                       "PlayCardUnforced", 
                                                       true);
                                         System.out.println("Cannot play that card: you need another card to discard");
                                         return;
                                     }
                                 }
                                 System.out.println("Player " + 
                                                    server.choice.get(1)[0][0] + 
                                                    " is targetting " + 
                                                    server.choice.get(1)[0][1]);
                                 if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.STEAL.ordinal() ||
                                     getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DISCARD.ordinal()){
                                         server.prompt(turn % numPlayers, 
                                                       "PickCardTarget", 
                                                       true);
                                         return;
                                 }
                                 server.sendInfo("SetInfo:CardPlayed:" + server.choice.get(0)[0][0] + ":" + getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).name + ":" + server.choice.get(1)[0][1]);
                                 if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.JAIL.ordinal()){
                                     server.sendInfo("SetInfo:PutInField:"+server.choice.get(1)[0][1]+":"+
                                                 getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).ordinal);
                                     players[server.choice.get(1)[0][1]].field.add(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1])); //give player the jail
                                     playerDiscardCard(server.choice.get(0)[0][0], 
                                                        server.choice.get(0)[0][1], false);
                                     server.choice.remove(server.choice.size()-1);
                                     server.choice.remove(server.choice.size()-1);
                                     server.prompt(turn % numPlayers, "PlayCardUnforced", true);
                                     return;
                                 } else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DAMAGE.ordinal() ||
                                     getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DUEL.ordinal() ) {
                                     server.prompt(server.choice.get(1)[0][1], 
                                                   "PlayCardUnforced", 
                                                   true); //unforce b/c do not have to miss
                                     return;
                                 } else {
                                     System.out.println("NOT A BANG, SO TARGET DOES NOT CHOOSE (Unimplemented targetting card)");
                                     playerDiscardCard(server.choice.get(0)[0][0], 
                                                       server.choice.get(0)[0][1], true);
                                 }
                             }
                             else{
                                 server.choice.remove(server.choice.size() - 1);
                             }
                         } else if (server.choice.size() >= 3) {
                             if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == 
                                 Card.play.DAMAGE.ordinal() && 
                                 getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special != 
                                 2) {
                                 if (server.choice.get(2)[0][1] == 
                                     -1) { //no miss played
                                     server.sendInfo("SetInfo:CardPlayed:" + 
                                                     server.choice.get(1)[0][1] + 
                                                     ":no miss");
                                     changeLifePoints(server.choice.get(1)[0][1], 
                                                      -1);
                                     if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect2 == 
                                         Card.play.DRAW.ordinal()) {
                                         playerDrawCard(server.choice.get(1)[0][0], 
                                                        1);
                                     }
                                     server.choice.remove(server.choice.size() - 
                                                          1);
                                     server.choice.remove(server.choice.size() - 
                                                          1);
                                     playerDiscardCard(server.choice.get(0)[0][0], 
                                                       server.choice.get(0)[0][1], true);
                                 } else if (getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).effect == Card.play.MISS.ordinal() && 
                                     (getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).type == 3?getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).location>0:true)) {
                                     server.sendInfo("SetInfo:CardPlayed:" + server.choice.get(1)[0][1] + ":" + getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).name);
                                     if (getCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1]).effect2 == 
                                         Card.play.DRAW.ordinal()) {
                                         playerDrawCard(server.choice.get(1)[0][1], 
                                                        1);
                                     }
                                     if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect2 == 
                                         Card.play.DRAW.ordinal()) {
                                         playerDrawCard(server.choice.get(1)[0][0], 1);
                                     }
                                     playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                                     playerDiscardCard(server.choice.get(1)[0][1], server.choice.get(2)[0][1], true);
                                     server.choice.remove(server.choice.size() - 1);
                                     server.choice.remove(server.choice.size() - 1);
                                 } else { //not a miss card!
                                     System.out.println("May only play a miss!");
                                     server.choice.remove(server.choice.size() - 1);
                                     server.prompt(server.choice.get(1)[0][1], "PlayCardUnforced", true);
                                     return;
                                 }
                             }
                             else if(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DUEL.ordinal()){
                                 if(server.choice.get(server.choice.size()-1)[0][1]==-1){
                                     changeLifePoints(server.choice.get(server.choice.size()-1)[0][0], -1);
                                     playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                                     server.choice.remove(server.choice.size() - 1);
                                     server.choice.remove(server.choice.size() - 1);
                                     if(server.choice.size()==4){
                                         server.choice.remove(server.choice.size() - 1);
                                     }
                                 }
                                 else if(getCard(server.choice.get(server.choice.size()-1)[0][0], server.choice.get(server.choice.size()-1)[0][1]).effect == Card.play.DAMAGE.ordinal() && getCard(server.choice.get(server.choice.size()-1)[0][0], server.choice.get(server.choice.size()-1)[0][1]).special==1){
                                     if(server.choice.size() == 4){
                                         playerDiscardCard(server.choice.get(server.choice.size()-1)[0][0], server.choice.get(server.choice.size()-1)[0][1], true);
                                         if(server.choice.get(server.choice.size()-1)[0][1]<server.choice.get(0)[0][1])
                                             server.choice.get(0)[0][1]--;
                                         server.choice.remove(server.choice.size() - 1);
                                         server.choice.remove(server.choice.size() - 1);
                                         server.prompt(server.choice.get(1)[0][1], "PlayCardUnforced", true);
                                         return;
                                     }
                                     else if(server.choice.size()==3){
                                         playerDiscardCard(server.choice.get(server.choice.size()-1)[0][0], server.choice.get(server.choice.size()-1)[0][1], true);
                                         server.prompt(server.choice.get(1)[0][0], "PlayCardUnforced", true);
                                         return;
                                     }
                                     else{
                                         System.out.println("AWEJFAWKFJ@K~$!@#%!!@#RFQFAWEFAWEFA@@#%!&$*#%*#@!$@#%!@#$#YTRQRGDDDZVCX");
                                         return;
                                     }
                                 } else{
                                     System.out.println("ILLEGAL CARD");
                                     int i = server.choice.get(server.choice.size() - 1)[0][0];
                                     server.choice.remove(server.choice.size() - 1);
                                     server.prompt(i, "PlayCardUnforced", true);
                                 }
                             }
                             else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.STEAL.ordinal() ||
                                 getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DISCARD.ordinal()){
                                     if(server.choice.get(1)[0][1] == server.choice.get(1)[0][0]){
                                         server.choice.remove(server.choice.size() - 1);
                                         server.choice.remove(server.choice.size() - 1);
                                         server.prompt(turn % numPlayers, "PickCardTarget", true);
                                         System.out.println("Cannot panic yourself");
                                         return;
                                     }
                                     server.sendInfo("SetInfo:CardPlayed:" + server.choice.get(0)[0][0] + ":" + getCard(server.choice.get(0)[0][0],server.choice.get(0)[0][1]).name + ":" + server.choice.get(1)[0][1]);
                                     int temp = server.choice.get(2)[0][1];
                                     if(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.DISCARD.ordinal()){
                                         if(temp>-1){
                                             playerDiscardCard(server.choice.get(1)[0][1], temp, true);
                                              }
                                         else{
                                             temp = (-temp)-3;
                                             System.out.println("LOOKING FOR A CARD IN FIELD");
                                             playerFieldDiscardCard(server.choice.get(1)[0][1], temp, true);
                                         }
                                     }
                                     else{
                                         if(temp>-1){///askdfjlasjflajfa;lsjkafsf REPLACE THIS WITH GETCARD
                                             players[turn%numPlayers].hand.add(players[(server.choice.get(1)[0][1])].hand.get(temp));
                                             server.sendInfo(turn%numPlayers,"Draw:" + turn%numPlayers + ":Game:"+players[(server.choice.get(1)[0][1])].hand.get(temp).name);
                                             playerDiscardCard(server.choice.get(1)[0][1], temp, false);
                                         }
                                         else{
                                             temp = (-temp)-3;
                                             players[turn%numPlayers].hand.add(players[(server.choice.get(1)[0][1])].field.get(temp));
                                             server.sendInfo(turn%numPlayers,"Draw:" + turn%numPlayers + ":Game:"+players[(server.choice.get(1)[0][1])].field.get(temp).name);
                                             playerFieldDiscardCard(server.choice.get(1)[0][1], temp, false);
                                         }
                                     }
                                     server.choice.remove(server.choice.size() - 1);
                                     server.choice.remove(server.choice.size() - 1);
                                     playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                                     //server.prompt(turn%numPlayers, "PlayCardUnforced", true);
                                     //return;
                             }
                         }                        
                     } else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).target == 
                                3 &&
                                (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).type == 
                                 3? 
                                 getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).location ==
                                  1:true)) {
                         if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == Card.play.HEAL.ordinal()) {
                             for (int n = 0; n < numPlayers; n++) {
                                 changeLifePoints(n, 1);
                             }
                             playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                         } else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == 
                                    Card.play.DRAW.ordinal()) {
                             for (int n = 0; n < numPlayers; n++) {
                                 playerDrawCard(n, 1);
                             }
                             playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true); //TODO: fix general store
                         } else {                        
                             server.sendInfo("SetInfo:PutInField:"+server.choice.get(0)[0][0]+":"+
                                     getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).name+":"+
                                     server.choice.get(0)[0][1]);
                         }
 
                     } else if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).target == 
                                4 && 
                                 (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).type == 
                                  3? 
                                  getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).location ==
                                   1:true)) {
                         if (server.choice.size() == 1) {
                             int[] p = new int[numPlayers - 1];
                             for (int n = 0, m = 0; n < numPlayers - 1; n++, m++) {
                                 if (m == turn % numPlayers) {
                                     m++;
                                 }
                                 p[n] = m;
                             }
                             server.promptPlayers(p, "PlayCardUnforced"); //this is for damage cards only!!! need to implement BRAWL here.
                             return;
                         } else if (server.choice.size() == 2) {
                             ArrayList<Integer> al = new ArrayList<Integer>();
                             for (int[] n: server.choice.get(1)) {
                                 if (n[1] == -1) {
                                     changeLifePoints(n[0], -1);
                                 }
                                 else if ((getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special == 0 && 
                                            getCard(n[0], n[1]).effect == Card.play.MISS.ordinal() && 
                                             getCard(n[0], n[1]).type != 3) || 
                                          (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).special == 2 && 
                                            getCard(n[0], n[1]).effect == Card.play.DAMAGE.ordinal() && 
                                             getCard(n[0], n[1]).special == 1)) {
                                     if (getCard(n[0], n[1]).effect2 == 
                                         Card.play.DRAW.ordinal()) {
                                         playerDrawCard(n[0], 1);
                                     }
                                     server.sendInfo("SetInfo:CardPlayed:" + n[0] + ":" + getCard(n[0], n[1]).name + ":" + n[1]);
                                     playerDiscardCard(n[0], n[1], true);
                                 } else {
                                     System.out.println("SOMEONE DID NOT PLAY A LEGAL CARD");
                                     al.add(n[0]);
                                 }
                             }
                             if (al.size() == 0){
                                 playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                                 server.choice.remove(server.choice.size() - 1);
                             }
                             else {
                                 int[] pp = new int[al.size()];
                                 for (int n = 0; n < pp.length; n++) {
                                     pp[n] = al.get(n);
                                 }
                                 server.choice.remove(server.choice.size() - 1);
                                 server.promptPlayers(pp, "PlayCardUnforced");
                                 return;
                             }
                         }
                     } 
                     else if((getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).type == 3 || getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).type == 5) && getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).location == 0){
                         server.sendInfo("SetInfo:PutInField:"+server.choice.get(0)[0][0]+":"+
                                     getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).name+":"+
                                     server.choice.get(0)[0][1]);
                         getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).location = 2;
                         players[server.choice.get(0)[0][0]].field.add(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]));
                         players[server.choice.get(0)[0][0]].hand.remove(server.choice.get(0)[0][1]);
                         //server.choice.remove(server.choice.size() - 1);
                         //server.prompt(turn % numPlayers, "PlayCardUnforced", true);
                     } else { //self targetting
                         if (isCardLegal(getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]), 
                                         players[server.choice.get(0)[0][0]], 
                                         null)) {
                             server.sendInfo("SetInfo:CardPlayed:" + 
                                             server.choice.get(0)[0][0] + ":" + 
                                             getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).name);
                             if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == 
                                 Card.play.DRAW.ordinal()) {
                                 playerDrawCard(server.choice.get(0)[0][0], 
                                                getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).range);
                             }
                             if (getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).effect == 
                                 Card.play.HEAL.ordinal()) {
                                 changeLifePoints(server.choice.get(0)[0][0], 
                                                  getCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1]).range);
                             }
                             playerDiscardCard(server.choice.get(0)[0][0], server.choice.get(0)[0][1], true);
                         }
                     }
                     server.choice.remove(server.choice.size() - 1);
                     server.prompt(turn % numPlayers, "PlayCardUnforced", true);
                 }
                 if (server.choice.get(0)[0][1] == -1 || 
                     players[server.choice.get(0)[0][0]].hand.size() <= 
                     0) { //TODO: add check for cards that can be played on field as well
                     server.choice.remove(server.choice.size() - 1);
                     nextTurn();
                 }
             }
         }
     }
 
     /**
      * 
      */
     public boolean isCardLegal(Card c, Player p1, 
                                Player p2) {
         if(c.type == 3 && c.location != 1){
             System.out.println("YOU JUST PLAYED THAT GREEN CARD");
             return false;
         }
         if(c.type == 5 && c.location >= 1){
             System.out.println("YOU CAN'T DO ANYTHING WITH THAT");
             return false;
         }
         if (c.target != 2 && p2 != null) {
             System.out.println("NON-TARGETING CARD HAS TARGET");
             return false;
         } else if (c.target == 2 && p2 == null) {
             System.out.println("TARGETING CARD DOES NOT HAVE TARGET");
             return false;
         }
         
         //the rules
         if (true) { //no idea why i had a condition here
             if (c.type == 3 && players[server.choice.get(0)[0][0]].field.contains(c)) { //replace false with some indicator of whether the card is on field
                     System.out.println("CLICKED ON A GREEN CARD ON FIELD, not implemented yet");
             }
             if (c.type == 2) {
                 if (c.effect == Card.play.JAIL.ordinal()) {
                     if(p2.role.ordinal()==0){
                         return false; //cannot jail sheriff
                     } else if(p1==p2){
                         return false; //cannot jail self
                     }
                     for(Card card :p2.field){
                         if(card.effect == Card.play.JAIL.ordinal()) //TODO: this sort of check should be for all field cards
                             return false; //TODO: techinically, this should be allowed, and the old jail should be discarded
                     }
                 } else if (c.effect == Card.play.HEAL.ordinal()) {
                     if (c.target == 1 && 
                         p1.lifePoints == p1.maxLifePoints) { //can't beer self with max hp
                         System.out.println("ILLEGAL CARD: you are already at maxhp");
                         return false;
                     } else if (c.target == 2 && 
                                p2.lifePoints == p2.maxLifePoints) {
                         System.out.println("ILLEGAL CARD: target is already at maxhp");
                         return false;
                     }
                 } else if (c.effect == Card.play.DAMAGE.ordinal() && 
                            p1 == p2) {
                     System.out.println("ILLEGAL CARD: why would you shoot yourself?");
                     return false;
                 }
             } else if (c.type == 4 && 
                        c.effect == Card.play.MISS.ordinal()) { //can't play miss
                 System.out.println("ILLEGAL CARD: can't play miss on turn");
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Gets the distance between p1 and p2. Includes horses and hideouts, but not guns
      * @param Player 1, Player 2
      * @return int of how far they are apart
      */
     public int getRangeBetweenPlayers(Player p1, Player p2) {
         int naturalRange = Math.min((numPlayers-p1.id+p2.id)%numPlayers, (numPlayers-p2.id+p1.id)%numPlayers);//seating order
         int distance = naturalRange;
         distance+=p1.hasFieldEffect(Card.field.HORSE_CHASE)+p2.hasFieldEffect(Card.field.HORSE_RUN);
         return distance;
     }
 
     /**
      * Create p players.
      * Create a draw pile.
      * Show everyone their roles.
      * Give them a choice between two character cards.
      * Give sheriff the first turn.
      * Draw cards equal to the number of life points.
      * Sheriff gets an additional card.
      */
     public void start() {
         //Assign roles
         ArrayList<Enum> roles = new ArrayList<Enum>();
         players = new Player[numPlayers];
         for (int n = 0; n < numPlayers; n++) {
             players[n] = new Player(n, server.names.get(n));
         }
 
         switch (numPlayers) {
         case 2: //DEBUG MODE
             roles.add(Deck.Role.SHERIFF);
             roles.add(Deck.Role.OUTLAW);
             break;
         case 4:
             roles.add(Deck.Role.SHERIFF);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.RENEGADE);
             break;
         case 5:
             roles.add(Deck.Role.SHERIFF);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.RENEGADE);
             roles.add(Deck.Role.DEPUTY);
             break;
         case 6:
             roles.add(Deck.Role.SHERIFF);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.RENEGADE);
             roles.add(Deck.Role.DEPUTY);
             roles.add(Deck.Role.OUTLAW);
             break;
         case 7:
             roles.add(Deck.Role.SHERIFF);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.RENEGADE);
             roles.add(Deck.Role.DEPUTY);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.DEPUTY);
             break;
         case 8:
             roles.add(Deck.Role.SHERIFF);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.RENEGADE);
             roles.add(Deck.Role.DEPUTY);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.OUTLAW);
             roles.add(Deck.Role.RENEGADE);
             break;
         default:
             System.out.print("Bad number of players!");
             System.exit(0);
             break;
         }
         for (int n = 0; n < numPlayers; n++) {
             int role = 
                 roles.remove((int)(Math.random() * roles.size())).ordinal();
             if (role == 0) {
                 sheriff = n;
                 server.sendInfo("SetInfo:role:" + n + ":" + role);
             } else
                 server.sendInfo(n, "SetInfo:role:" + n + ":" + role);
             players[n].role = Deck.Role.values()[Integer.valueOf(role)];
         }
 
         deck = new Deck();
         deck.fillCharacterCards(numPlayers);
 
         for (int n = 0; n < numPlayers; n++) {
             playerDrawCard(n, 5);
         }
 
         server.promptAll("ChooseCharacter");
     }
 
 
     /**
      * After characters have been chosen, continue starting the game
      */
     public void start2() {
         for (int n = 0; n < server.choice.get(server.choice.size() - 1).length; n++) {
             players[n].character = players[n].hand.get(server.choice.get(server.choice.size() - 1)[n][1]).ordinal;
             server.sendInfo("SetInfo:character:"+n+":"+ players[n].hand.get(server.choice.get(server.choice.size() - 1)[n][1]).ordinal);
         }
         for (int n = 0; n < server.choice.get(server.choice.size() - 1).length; n++) {
             changeMaxLifePoints(n, players[n].hand.get(server.choice.get(server.choice.size() - 1)[n][1]).special + (n == sheriff ? 1 : 0));
         }
 
         deck.fillGameCards();
 
         //draw cards equal to lifepoints
         for (Player p1: players) {
             p1.hand.clear();
             playerDrawCard(p1.id, p1.lifePoints);
         }
 
         //Give Sheriff the first turn (turn 0)
         for (int n = 0; n < numPlayers; n++) {
             System.out.println("Player " + n + " is a " + players[n].role);
             if (players[n].role == Deck.Role.SHERIFF) {
                 turn = n - 1;
                 break;
             }
         }
         server.choice.clear();
         nextTurn();
     }
 
     //returns false if game is over
 
     public void nextTurn() {
         turn++;
 
         if (server.choice.size() > 1) {
             System.out.println("ERROR: NEW TURN CALLED TOO EARLY");
             turn--;
             return;
         }
 
         //check if player is dead
         int oldturn = turn;
         while (players[turn % numPlayers].lifePoints == 0 && 
                turn - oldturn < numPlayers) {
             turn++;
         }
 
         server.sendInfo("SetInfo:turn:" + turn);
         
         for(int n = 0; n<players[turn % numPlayers].field.size(); n++){
             Card c = players[turn % numPlayers].field.get(n);
             if(c.effect == Card.play.JAIL.ordinal()){
                 if(c.type==2){
                     playerFieldDiscardCard(turn%numPlayers, n, true);
                     nextTurn();
                     return;
                 } else{ //dynamite
                     if(Math.random()<.5){ //BOOM
                         playerFieldDiscardCard(turn%numPlayers, n, true); 
                         System.out.println("ASPLODEDEDEDEDEDEDEDEDEDEDED!11111!!$!@#$!@#$");
                         //something to end the guy's turn if he's dead
                     }
                     else{
                         int a = turn+1;
                         while (players[a  % numPlayers].lifePoints == 0 && 
                                a - turn < numPlayers) {
                             a++;
                         }
                         server.sendInfo("SetInfo:PutInField:"+a%numPlayers+":"+
                                     players[turn%numPlayers].field.get(n).ordinal);
                         players[a%numPlayers].field.add(players[turn%numPlayers].field.get(n));
                        //playerFieldDiscardCard(turn%numPlayers, n, true);
                     }
                 }
             }
         }
         
         for(Card c : players[turn % numPlayers].field){ //set all green cards played the turn before to 1
             c.location = 1;
         }
         
         //draw two cards
         if (players[turn % numPlayers].specialDraw == 
             0) { //TODO: get rid of specialDraw, move to a direct reference to character cards
             playerDrawCard(turn % numPlayers, 2);
         } else {
             //Yuck, there's alot of characters with this ability
         }
         System.out.println("It is turn " + turn % numPlayers);
         server.prompt(turn % numPlayers, "PlayCardUnforced", true);
     }
 
     /**
      * If written, this method would replace the return value above
      */
     public boolean isGameWon() {
         int deputies = 0;
         int sheriff = 0;
         int outlaws = 0;
         int renegades = 0;
         for (Player p: players) {
             if (p.lifePoints > 0) {
                 switch ((Deck.Role)p.role) {
                 case DEPUTY:
                     deputies++;
                 case SHERIFF:
                     sheriff++;
                     break;
                 case OUTLAW:
                     outlaws++;
                     break;
                 case RENEGADE:
                     renegades++;
                     break;
                 }
             }
         }
         if (sheriff == 0) {
             if (outlaws == 0 && deputies == 0) {
                 System.out.println("Renegades win!");
                 return true;
             } else {
                 System.out.println("Outlaws win!");
                 return true;
             }
         }
         if (sheriff >= 0) {
             if (outlaws == 0 && deputies == 0) {
                 System.out.println("Sheiff+Deputies win!");
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Plays a card. This is one of the functions used to connect the GUI to the game.
      * @param c
      * @return Whether that is a legal move (boolean) //TODO: this is stupid
      */
 
     /**public boolean playCardFromHand(Player p, Card c){
         if(c.type == 4){
             System.out.println("A miss card cannot be played.");
             return false;
         }
         p.hand.remove(c);
         if(c.type == 3){
             //put it on the field
         }
         if(c.type == 2){
             int[] targets;
             if(c.target == 2){
                 targets = new int[]{1};//gui[p.id].promptChooseTargetPlayer()};//TODO: Actually prompt player to choose targets
             }
             else if(c.target == 4){
                 targets = new int[numPlayers-1];
                 int m = 0;
                 for(int n = 0; n<targets.length; n++, m++)
                     if(m!=p.id)
                         targets[n] = m;
                     else
                         n--;
             }
             else if(c.target ==3){
                 targets = new int[numPlayers];
                 for(int n = 0; n<targets.length; n++)
                     targets[n] = n;
             }
             else{
                 targets = new int[1]; //serves no purpose but to initialize value
             }
         
             //damage
             if(c.effect == Card.play.DAMAGE.ordinal()){
                 for(int target: targets){
                     int miss = -2; //or bang for indians
                     while(miss != -1 || (miss>=0 && miss<players[target].hand.size() && players[target].hand.get(miss).special==(c.name==Deck.CardName.INDIANS.name()?1:0) && players[target].hand.get(miss).effect==(c.name==Deck.CardName.INDIANS.name()?Card.play.DAMAGE.ordinal():Card.play.DAMAGE.ordinal()))){
                         miss = 1;//gui[target].promptChooseCard(players[target].hand, "Dodge!", "Play a miss?", false); //TODO: FIX
                     }
                     if(miss == -1){ //change this to a flag checking barrels/if target want to play a miss, etc.
                         while(players[target].lifePoints<=0){
                             System.out.println("Invalid Target!");
                             //gui[p.id].promptChooseTargetPlayer();//TODO: FIX
                         }
                         players[target].lifePoints--;
                         System.out.println(target+"'s hp: "+players[target].lifePoints);
                         if(players[target].lifePoints <= 0){
                             //gui[p.id].appendText("You killed player "+target+"! \nPlayer "+target+" was a(n) "+players[target].role.name()+" ("+players[target].role.ordinal()+")");//TODO:FIX
                             if(players[target].role.ordinal()==2){ //if he was an outlaw, claim bounty
                                 //gui[p.id].appendText("Draw 3 cards!");//TODO:FIX
                                 playerDrawCard(p.id, 3);
                             }
                         }
                     }
                 }
             }
             
             //heal
             if(c.effect == Card.play.HEAL.ordinal()){
                 for(int target: targets){
                     if(c.range != 1) //shiskey
                         players[target].lifePoints++;
                     else
                         players[target].lifePoints += 2;
                     if(players[target].lifePoints>players[target].maxLifePoints)
                         players[target].lifePoints = players[target].maxLifePoints;
                     System.out.println(target+"'s hp: "+players[target].lifePoints);
                 }
             }
             //draw
             if(c.effect == Card.play.DRAW.ordinal())
                 playerDrawCard(p.id, c.range);
             if(c.effect2 != 0 && c.effect2 == Card.play.DRAW.ordinal())
                 playerDrawCard(p.id, 1);
             deck.discardPile.add(c);
         }
         //TODO: currently only removes the card from hand and sets it into discard
         return true;
     }**/
     public void playerDrawCard(int p, int n) {
         if (n <= 0)
             return; //must draw at least 1
         Card c = drawCard();
         String s = 
             "Draw:" + p + ":" + (c.type == 1 ? "Character:" : "Game:") + c.name + 
             ":"; //need this to get the card type
         players[p].hand.add(c);
         for (int m = 1; m < n; m++) {
             c = drawCard();
             s = s + c.name + ":";
             players[p].hand.add(c);
         }
         server.sendInfo(p, s);
         if (c.type != 1) {
             for (int m = 0; m < numPlayers; m++) {
                 if (m != p) {
                     server.sendInfo(m, "Draw:" + p + ":" + n);
                 }
             }
         }
     }
     
     /**
      * Discards Player p's hand
      */
     public void playerDiscardHand(Player p) {
         for (int n = p.hand.size() - 1; n >= 0; n--)
             playerDiscardCard(p.id, n, true);
     }
 
     /**
      * Discards card n in Player p's hand
      */
     public void playerDiscardCard(int p, int n, boolean dp) {
         if(n<-2){
             playerFieldDiscardCard(p, -n-3, dp);
             return;
         }
         Card c = players[p].hand.get(n);
         //is card a character card
         players[p].hand.remove(c);
         if (c.type == 1) {
             //players[p].hand.remove(n);
         } else {
             server.sendInfo(p, "SetInfo:discard:"+p+":"+n);
             if(dp){
                 deck.discardPile.add(c);
                 for (int m = 0; m < numPlayers; m++) {
                     if (m != p) {
                         server.sendInfo(m, "SetInfo:discard:"+p+":"+n+":"+ c.name);
                     }
                 }
             }
             else{
                 for (int m = 0; m < numPlayers; m++) {
                     if (m != p) {
                         server.sendInfo(m, "SetInfo:discard:"+p+":"+n);
                     }
                 }
             }
         }
     }
     /**
      * Discards card n in Player p's field
      */
     public void playerFieldDiscardCard(int p, int n, boolean dp) {
         Card c = players[p].field.get(n);
         //is card a character card
         players[p].field.remove(c);
         if (c.type == 1) {
             //players[p].hand.remove(n);
         } else {
             server.sendInfo(p, "SetInfo:fieldDiscard:"+p+":"+n);
             if(dp)
                 deck.discardPile.add(c);
             for (int m = 0; m < numPlayers; m++) {
                 if (m != p) {
                     server.sendInfo(m, "SetInfo:fieldDiscard:"+p+":"+n+":"+ c.name);
                 }
             }
         }
         }
     
     /**
      * Moves a card from p's hand to his field
      */
     public void playerMoveToField(int p, int n) {
         /*Card c = players[p].hand.get(n);
         //is card a character card
         players[p].hand.remove(c);
         server.sendInfo("SetInfo:moveToField:"+p+":"+c.name);
         deck.discardPile.add(c);*/
     }
 
     /**
      * Flips the top card of the drawPile. This card is then put in the discard 
      * pile. Used for barrels and other effects.
      * @return
      */
     public Card flipCard() {
         Card c = drawCard();
         deck.discardPile.add(drawCard());
         return c;
     }
 
     /**
      * Draws one card. This card is either returned to the flipCard method or
      * the playerDrawCard method.
      * @param
      * @return Card
      */
     public Card drawCard() {
         if (deck.drawPile.size() == 0) {
             shuffleDeck();
         }
         System.out.println(deck.drawPile.peekLast().name);
         return deck.drawPile.removeLast();
     }
 
     /**
      * Shuffles the discard pile back into the deck.
      * Only used when draw pile is empty
      */
     public void shuffleDeck() {
         if (deck.drawPile.size() > 0) {
             System.out.println("Error: did not need to shuffleDeck()");
             return;
         }
         while (deck.discardPile.size() > 0) {
             deck.drawPile.add(deck.discardPile.remove((int)Math.random() * 
                                                       deck.discardPile.size()));
         }
     }
 
     void changeMaxLifePoints(int p, int n) {
         server.sendInfo("SetInfo:maxHP:" + p + ":" + n);
         players[p].maxLifePoints += n;
         players[p].lifePoints = players[p].maxLifePoints;
     }
 
     void changeLifePoints(int p, int n) {
         if (players[p].lifePoints + n > players[p].maxLifePoints) {
             System.out.println("Player " + p + " is now at max hp.");
             n = players[p].maxLifePoints - players[p].lifePoints;
             server.sendInfo("SetInfo:HP:" + p +":"+ n);
             players[p].lifePoints += n;
         } else {
             server.sendInfo("SetInfo:HP:" + p + ":" + n);
             players[p].lifePoints += n;
         }
     }
     
     Card getCard(int p, int n){
         if(n>-1){
             return players[p].hand.get(n);
         }
         if(n<-2){
             return players[p].field.get(-n-3);
         }
         System.out.println("ERRORERRORERROR12345");
         return null;
     }
 }
