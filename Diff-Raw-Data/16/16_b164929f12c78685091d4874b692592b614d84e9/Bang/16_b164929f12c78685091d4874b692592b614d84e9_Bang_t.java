 package netbang.core;
 
 import java.util.ArrayList;
 
 import netbang.core.Card.Targets;
 import netbang.network.Server;
 
 public class Bang {
     Server server;
 
     public Player[] players;
     public int numPlayers;
     public int turn;
         public int offturn = -1;
     public Deck deck;
     ArrayList<Card> store;
         int storeIndex;
 
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
             if(offturn>-1){
                 if (server.choice.size() == 0) {
                     server.promptPlayer(offturn, "PlayCard");
                 }
                 else if (server.choice.size() >= 1) {
                     offturn = -1;
                 }
             }
             
             if (server.choice.size() >= 1) {
                 Choice[] firstchoice = server.choice.get(0);
 				if (server.choice.size() == 1) {
                     System.out.println("You played " + firstchoice[0].choice + ". You have " +
                     		(players[firstchoice[0].playerid].hand.size() - 1) + " cards left in your hand.");
                 }
                 if (firstchoice[0].choice != -1) {
                     int who = turn % numPlayers;
                     Choice[] secondchoice = null;
                     if(server.choice.size()>1)
                     	secondchoice = server.choice.get(1);
 					if (getCard(firstchoice[0].playerid,firstchoice[0].choice).target == 
                         Targets.ONE && 
                         (getCard(firstchoice[0].playerid,firstchoice[0].choice).type == 
                          3? 
                          getCard(firstchoice[0].playerid,firstchoice[0].choice).location ==
                           1:true)) {
                         if (server.choice.size() == 1) {
                             {
                                 if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.STEAL.ordinal() ||
                                  getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.DISCARD.ordinal()){
                                     server.promptPlayer(who, "PickCardTarget");
                                 }
                                 else{   
                                     server.promptPlayer(who, "PickTarget");
                                 }
                                 server.sendInfo(who, "InfoMsg:Pick Target:0");    
                                 return;
                             }
                         } else if (server.choice.size() == 2) {
                             if (secondchoice[0].choice == -1) {
                                 System.out.println("Cancelled");
                                 server.choice.remove(server.choice.size() - 1);
                             } else if (isCardLegal(getCard(firstchoice[0].playerid,firstchoice[0].choice), players[firstchoice[0].playerid], players[secondchoice[0].choice])) {
                                 if (getCard(firstchoice[0].playerid,firstchoice[0].choice).discardToPlay == true) {
                                     if (players[firstchoice[0].playerid].hand.size() > 1) {
                                         if (secondchoice.length == 1) { //has not been asked to discard yet
                                             System.out.println("THIS CARD REQUIRES A DISCARD TO PLAY AS WELL");
                                             server.choice.set(1, new Choice[] { secondchoice[0], new Choice( firstchoice[0].playerid, -2 ) }); //TODO: obsolete?
                                             server.prompt(firstchoice[0].playerid,
                                                           "PlayCard"); //TODO: play card from hand only
                                             /*TODO: Why didn't this have true for prompting one player?
                                              * It *was* prompting only one player. I guess the reasoning
                                              * was that the line above set the array manually, but it also
                                              * says something about obsolete?
                                              */
                                             
                                             System.out.println(server.choice.size() + 
                                                                " " + 
                                                                secondchoice.length);
                                             return;
                                         }
                                         if (secondchoice.length ==  2) { //has  been asked to discard
                                             if (secondchoice[1].choice == firstchoice[0].choice) {
                                                 System.out.println("Cannot discard the card you are playing");
                                                 server.choice.set(1, new Choice[] { secondchoice[0], 
                                                                                 new Choice( firstchoice[0].playerid, -2 ) });
                                                 server.prompt(firstchoice[0].playerid, "PlayCard"); //TODO: play card from hand only
                                                 return;
                                             } else {
                                                 System.out.println("DISCARD OK.");
                                                 //may need to reorder the cards;
                                                 if (firstchoice[0].choice > 
                                                     secondchoice[1].choice) {
                                                     firstchoice[0].choice--;
                                                 }
                                                 playerDiscardCard(firstchoice[0].playerid, 
                                                                   secondchoice[1].choice, true);
                                             }
                                         }
                                     } else {
                                         server.choice.remove(server.choice.size() - 1);
                                         server.promptPlayer(who, "PlayCardUnforced");
                                         System.out.println("Cannot play that card: you need another card to discard");
                                         return;
                                     }
                                 }
                                 System.out.println("Player " + 
                                                    secondchoice[0].playerid + 
                                                    " is targeting " + 
                                                    secondchoice[0].choice);
                                 if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.STEAL.ordinal() ||
                                     getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.DISCARD.ordinal()){
                                         server.promptPlayer(who, "PickCardTarget");
                                         return;
                                 }
                                 server.sendInfo("SetInfo:CardPlayed:" + firstchoice[0].playerid + ":" + getCard(firstchoice[0].playerid,firstchoice[0].choice).name + ":" + secondchoice[0].choice);
                                 if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.JAIL.ordinal()){
                                     server.sendInfo("SetInfo:PutInField:"+secondchoice[0].choice+":"+
                                                 getCard(firstchoice[0].playerid, firstchoice[0].choice).ordinal);
                                     players[secondchoice[0].choice].field.add(getCard(firstchoice[0].playerid, firstchoice[0].choice)); //give player the jail
                                     playerDiscardCard(firstchoice[0].playerid, 
                                                        firstchoice[0].choice, false);
                                     server.choice.remove(server.choice.size()-1);
                                     server.choice.remove(server.choice.size()-1);
                                     server.promptPlayer(who, "PlayCardUnforced");
                                     return;
                                 } else if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.DAMAGE.ordinal() ||
                                  getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.DUEL.ordinal() ) {
                                     if(players[who].bangs>0 && getCard(firstchoice[0].playerid, firstchoice[0].choice).special == 1 &&
                                      (playerHasFieldEffect(who, Card.field.GUN)==-1||players[who].field.get(playerHasFieldEffect(who, Card.field.GUN)).special!=1)){
                                         server.choice.remove(server.choice.size() - 1);
                                         server.sendInfo(who, "InfoMsg:Stop banging!:1");    
                                     }                                    
                                     else if(playerHasFieldEffect(secondchoice[0].choice, Card.field.BARREL)>-1&&Math.random()<.25 ||
                                      (players[secondchoice[0].choice].character==Deck.Characters.JOURDONNAIS.ordinal()?Math.random()<.25:false)){
                                         server.sendInfo("InfoMsg:Player "+secondchoice[0].choice+" did a barrel roll!:0");
                                         server.choice.remove(server.choice.size() - 1);
                                         playerDiscardCard(firstchoice[0].playerid, 
                                                           firstchoice[0].choice, true);
                                     }
                                     else{
                                         //System.out.println(players[turn%numPlayers].bangs+"~!@#!@#!@#!@#@!#!#!@#!@#!#@!@#!@#!#!#!#!@#!#!@#12");
                                         server.promptPlayer(secondchoice[0].choice, 
                                                       "PlayCardUnforced"); //unforce b/c do not have to miss
                                         return;
                                     }
                                 } else {
                                     System.out.println("NOT A BANG, SO TARGET DOES NOT CHOOSE (Unimplemented targetting card)");
                                     playerDiscardCard(firstchoice[0].playerid, 
                                                       firstchoice[0].choice, true);
                                 }
                             }
                             else{
                                 server.choice.remove(server.choice.size() - 1);
                             }
                         } else if (server.choice.size() >= 3) {
                             Choice[] thirdchoice = server.choice.get(2);
 							if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == 
                                 Card.play.DAMAGE.ordinal() && 
                                 getCard(firstchoice[0].playerid, firstchoice[0].choice).special != 
                                 2) {
                                 if (thirdchoice[0].choice == -1) { //no miss played
                                     server.sendInfo("SetInfo:CardPlayed:" + 
                                                     secondchoice[0].choice + 
                                                     ":no miss");
                                     changeLifePoints(secondchoice[0].choice, 
                                                      -1);
                                     if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect2 == 
                                         Card.play.DRAW.ordinal()) {
                                         playerDrawCard(secondchoice[0].playerid, 
                                                        1);
                                     }
                                     if(getCard(firstchoice[0].playerid, firstchoice[0].choice).special == 1 && 
                                      players[who].character!=Deck.Characters.WILLY_THE_KID.ordinal())//Willy the Kid
                                         players[who].bangs++;
                                     server.choice.remove(server.choice.size() - 
                                                          1);
                                     server.choice.remove(server.choice.size() - 
                                                          1);
                                     playerDiscardCard(firstchoice[0].playerid, 
                                                       firstchoice[0].choice, true);
                                 } else if (getCard(secondchoice[0].choice, thirdchoice[0].choice).effect == Card.play.MISS.ordinal() && 
                                     (getCard(secondchoice[0].choice, thirdchoice[0].choice).type == 3?getCard(secondchoice[0].choice, thirdchoice[0].choice).location>0:true)) {
                                     server.sendInfo("SetInfo:CardPlayed:" + secondchoice[0].choice + ":" + getCard(secondchoice[0].choice, thirdchoice[0].choice).name);
                                     if (getCard(secondchoice[0].choice, thirdchoice[0].choice).effect2 == 
                                         Card.play.DRAW.ordinal()) {
                                         playerDrawCard(secondchoice[0].choice, 
                                                        1);
                                     }
                                     if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect2 == 
                                         Card.play.DRAW.ordinal()) {
                                         playerDrawCard(secondchoice[0].playerid, 1);
                                     }
                                     if(getCard(firstchoice[0].playerid, firstchoice[0].choice).special == 1 &&
                                      players[who].character!=Deck.Characters.WILLY_THE_KID.ordinal())
                                         players[who].bangs++;
                                     playerDiscardCard(firstchoice[0].playerid, firstchoice[0].choice, true);
                                     playerDiscardCard(secondchoice[0].choice, thirdchoice[0].choice, true);
                                     server.choice.remove(server.choice.size() - 1);
                                     server.choice.remove(server.choice.size() - 1);
                                 } else { //not a miss card!
                                     System.out.println("May only play a miss!");
                                     server.choice.remove(server.choice.size() - 1);
                                     server.promptPlayer(secondchoice[0].choice, "PlayCardUnforced");
                                     return;
                                 }
                             }
                             else if(getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.DUEL.ordinal()){
                                 Choice[] lastchoice = server.choice.get(server.choice.size()-1);
 								if(lastchoice[0].choice==-1){
                                     changeLifePoints(lastchoice[0].playerid, -1);
                                     playerDiscardCard(firstchoice[0].playerid, firstchoice[0].choice, true);
                                     server.choice.remove(server.choice.size() - 1);
                                     server.choice.remove(server.choice.size() - 1);
                                     if(server.choice.size()==4){
                                         server.choice.remove(server.choice.size() - 1);
                                     }
                                 }
                                 else if(getCard(lastchoice[0].playerid, lastchoice[0].choice).effect == Card.play.DAMAGE.ordinal() && getCard(lastchoice[0].playerid, lastchoice[0].choice).special==1){
                                     if(server.choice.size() == 4){
                                         playerDiscardCard(lastchoice[0].playerid, lastchoice[0].choice, true);
                                         if(lastchoice[0].choice<firstchoice[0].choice)
                                             firstchoice[0].choice--;
                                         server.choice.remove(server.choice.size() - 1);
                                         server.choice.remove(server.choice.size() - 1);
                                         server.promptPlayer(secondchoice[0].choice, "PlayCardUnforced");
                                         return;
                                     }
                                     else if(server.choice.size()==3){
                                         playerDiscardCard(lastchoice[0].playerid, lastchoice[0].choice, true);
                                         server.promptPlayer(secondchoice[0].playerid, "PlayCardUnforced");
                                         return;
                                     }
                                     else{
                                         System.out.println("AWEJFAWKFJ@K~$!@#%!!@#RFQFAWEFAWEFA@@#%!&$*#%*#@!$@#%!@#$#YTRQRGDDDZVCX");
                                         return;
                                     }
                                 } else{
                                     System.out.println("ILLEGAL CARD");
                                     int i = lastchoice[0].playerid;
                                     server.choice.remove(server.choice.size() - 1);
                                     server.promptPlayer(i, "PlayCardUnforced");
                                 }
                             }
                             else if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.STEAL.ordinal() ||
                                 getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.DISCARD.ordinal()){
                                     if(secondchoice[0].choice == secondchoice[0].playerid){
                                         server.choice.remove(server.choice.size() - 1);
                                         server.choice.remove(server.choice.size() - 1);
                                         server.promptPlayer(who, "PickCardTarget");
                                         System.out.println("Cannot panic yourself");
                                         return;
                                     }
                                     server.sendInfo("SetInfo:CardPlayed:" + firstchoice[0].playerid + ":" + getCard(firstchoice[0].playerid,firstchoice[0].choice).name + ":" + secondchoice[0].choice);
                                     int temp = thirdchoice[0].choice;
                                     if(getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.DISCARD.ordinal()){
                                         if(temp>-1){
                                             playerDiscardCard(secondchoice[0].choice, temp, true);
                                              }
                                         else{
                                             temp = (-temp)-3;
                                             System.out.println("LOOKING FOR A CARD IN FIELD");
                                             playerFieldDiscardCard(secondchoice[0].choice, temp, true);
                                         }
                                     }
                                     else{
                                         if(temp>-1){//TODO:askdfjlasjflajfa;lsjkafsf REPLACE THIS WITH GETCARD
                                             players[who].hand.add(players[(secondchoice[0].choice)].hand.get(temp));
                                             server.sendInfo(who,"Draw:" + who + ":Game:"+players[(secondchoice[0].choice)].hand.get(temp).name);
                                             playerDiscardCard(secondchoice[0].choice, temp, false);
                                         }
                                         else{
                                             temp = (-temp)-3;
                                             players[who].hand.add(players[(secondchoice[0].choice)].field.get(temp));
                                             server.sendInfo(who,"Draw:" + who + ":Game:"+players[(secondchoice[0].choice)].field.get(temp).name);
                                             playerFieldDiscardCard(secondchoice[0].choice, temp, false);
                                         }
                                     }
                                     server.choice.remove(server.choice.size() - 1);
                                     server.choice.remove(server.choice.size() - 1);
                                     playerDiscardCard(firstchoice[0].playerid, firstchoice[0].choice, true);
                                     //server.prompt(turn%numPlayers, "PlayCardUnforced", true);
                                     //return;
                             }
                         }                        
                     } else if (getCard(firstchoice[0].playerid, firstchoice[0].choice).target == 
                                Targets.ALL &&
                                (getCard(firstchoice[0].playerid, firstchoice[0].choice).type == 
                                 3? 
                                 getCard(firstchoice[0].playerid, firstchoice[0].choice).location ==
                                  1:true)) {
                         if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == Card.play.HEAL.ordinal()) {
                             for (int n = 0; n < numPlayers; n++) {
                                 changeLifePoints(n, 1);
                             }
                             playerDiscardCard(firstchoice[0].playerid, firstchoice[0].choice, true);
                         } else if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == 
                                    Card.play.DRAW.ordinal()) {
                                                 if(store==null){
                                                     store = new ArrayList<Card>();
                                                     for(int n = 0; n < numPlayers; n++){ //only check living players
                                                         if(players[n].lifePoints>0)
                                                             store.add(drawCard());
                                                     }
                                                     storeIndex = who;
                                                     
                                                     String s = "";
                                                     for(Card c:store)
                                                         s += ":"+c.name;
                                                     server.sendInfo("SetInfo:GeneralStore:-1"+s);
                                                     server.promptPlayer(storeIndex, "GeneralStore");
                                                     return;
                                                 }
                                                 else{
                                                         Card card = store.remove(secondchoice[0].choice);
                                                         server.sendInfo(storeIndex, "Draw:"+storeIndex+":Game:" + card.name);
                                                         for(int m = 0; m != storeIndex; m++)
                                                             server.sendInfo(m, "Draw:" + storeIndex + ":" + 1);
                                                         players[storeIndex].hand.add(card);
                                                         
                                                         server.choice.remove(server.choice.size()-1);
                                                         
                                                         storeIndex++;
                                                         if(storeIndex == numPlayers)
                                                             storeIndex = 0;
                                                         
                                                     if(store.size()>0){
                                                         String s = "";
                                                         for(Card c:store)
                                                             s += ":"+c.name;
                                                         server.sendInfo("SetInfo:GeneralStore:-1"+s);
                                                         server.promptPlayer(storeIndex, "GeneralStore");
                                                         return;
                                                     }
                                                     else{
                                                         for(int n = 0; n<numPlayers; n++){
                                                             server.sendInfo(n, "SetInfo:GeneralStore:-1");
                                                         }
                                                         store = null;
                                                     }
                                                 }
                         } else {                        
                             server.sendInfo("SetInfo:PutInField:"+firstchoice[0].playerid+":"+
                                     getCard(firstchoice[0].playerid, firstchoice[0].choice).name+":"+
                                     firstchoice[0].choice);
                         }
 
                     } else if (getCard(firstchoice[0].playerid, firstchoice[0].choice).target == 
                                Targets.OTHERS && 
                                 (getCard(firstchoice[0].playerid, firstchoice[0].choice).type == 
                                  3? 
                                  getCard(firstchoice[0].playerid, firstchoice[0].choice).location ==
                                   1:true)) {
                         if (server.choice.size() == 1) {
                             int[] p = new int[numPlayers - 1];
                             for (int n = 0, m = 0; n < numPlayers - 1; n++, m++) {
                                 if (m == who) {
                                     m++;
                                 }
                                 p[n] = m;
                             }
                             server.promptPlayers(p, "PlayCardUnforced"); //this is for damage cards only!!! need to implement BRAWL here.
                             return;
                         } else if (server.choice.size() == 2) {
                             ArrayList<Integer> al = new ArrayList<Integer>();
                             for (Choice n: secondchoice) {
                                 if (n.choice == -1) {
                                     changeLifePoints(n.playerid, -1);
                                 }
                                 else if ((getCard(firstchoice[0].playerid, firstchoice[0].choice).special == 0 && 
                                            getCard(n.playerid, n.choice).effect == Card.play.MISS.ordinal() && 
                                             getCard(n.playerid, n.choice).type != 3) || 
                                          (getCard(firstchoice[0].playerid, firstchoice[0].choice).special == 2 && 
                                            getCard(n.playerid, n.choice).effect == Card.play.DAMAGE.ordinal() && 
                                             getCard(n.playerid, n.choice).special == 1)) {
                                     if (getCard(n.playerid, n.choice).effect2 == 
                                         Card.play.DRAW.ordinal()) {
                                         playerDrawCard(n.playerid, 1);
                                     }
                                     server.sendInfo("SetInfo:CardPlayed:" + n.playerid + ":" + getCard(n.playerid, n.choice).name + ":" + n.choice);
                                     playerDiscardCard(n.playerid, n.choice, true);
                                 } else {
                                     System.out.println("SOMEONE DID NOT PLAY A LEGAL CARD");
                                     al.add(n.playerid);
                                 }
                             }
                             if (al.size() == 0){
                                 playerDiscardCard(firstchoice[0].playerid, firstchoice[0].choice, true);
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
                     else if((getCard(firstchoice[0].playerid, firstchoice[0].choice).type == 3 || getCard(firstchoice[0].playerid, firstchoice[0].choice).type == 5) && getCard(firstchoice[0].playerid, firstchoice[0].choice).location == 0){
                         isCardLegal(getCard(firstchoice[0].playerid, firstchoice[0].choice), players[firstchoice[0].playerid], null);
                         server.sendInfo("SetInfo:PutInField:"+firstchoice[0].playerid+":"+
                                     getCard(firstchoice[0].playerid, firstchoice[0].choice).name+":"+
                                     firstchoice[0].choice);
                         if(getCard(firstchoice[0].playerid, firstchoice[0].choice).name=="DYNAMITE"){
                             //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                              getCard(firstchoice[0].playerid, firstchoice[0].choice).effect2 = who;
                         }
                         getCard(firstchoice[0].playerid, firstchoice[0].choice).location = 2;
                         players[firstchoice[0].playerid].field.add(getCard(firstchoice[0].playerid, firstchoice[0].choice));
                         players[firstchoice[0].playerid].hand.remove(firstchoice[0].choice);
                         //server.choice.remove(server.choice.size() - 1);
                         //server.prompt(turn % numPlayers, "PlayCardUnforced", true);
                     } else { //self targetting
                         if (isCardLegal(getCard(firstchoice[0].playerid, firstchoice[0].choice), 
                                         players[firstchoice[0].playerid], 
                                         null)) {
                             server.sendInfo("SetInfo:CardPlayed:" + 
                                             firstchoice[0].playerid + ":" + 
                                             getCard(firstchoice[0].playerid, firstchoice[0].choice).name);
                             if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == 
                                 Card.play.DRAW.ordinal()) {
                                 playerDrawCard(firstchoice[0].playerid, 
                                                getCard(firstchoice[0].playerid, firstchoice[0].choice).range);
                             }
                             if (getCard(firstchoice[0].playerid, firstchoice[0].choice).effect == 
                                 Card.play.HEAL.ordinal()) {
                                 changeLifePoints(firstchoice[0].playerid, 
                                                  getCard(firstchoice[0].playerid, firstchoice[0].choice).range +
                                                  (getCard(firstchoice[0].playerid, firstchoice[0].choice).special==1 && isCharacter(firstchoice[0].playerid,Deck.Characters.TEQUILA_JOE)?1:0));
                             }
                             playerDiscardCard(firstchoice[0].playerid, firstchoice[0].choice, true);
                         }
                     }
                     server.choice.remove(server.choice.size() - 1);
                     server.promptPlayer(who, "PlayCardUnforced");
                 }
                 if (firstchoice[0].choice == -1 || 
                     players[firstchoice[0].playerid].hand.size() <= 
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
         if (c.target != Targets.ONE && p2 != null) {
             System.out.println("NON-TARGETING CARD HAS TARGET");
             return false;
         } else if (c.target == Targets.ONE && p2 == null) {
             System.out.println("TARGETING CARD DOES NOT HAVE TARGET");
             return false;
         }
         //the rules
         if (true) { //no idea why i had a condition here
             Choice[] firstchoice = server.choice.get(0);
 			if ((c.type == 3 || c.type == 5) && players[firstchoice[0].playerid].hand.contains(c)) {
                 if(c.type == 5)
                     if(playerHasFieldEffect(firstchoice[0].playerid, Card.field.GUN)>-1)
                         playerFieldDiscardCard(firstchoice[0].playerid, playerHasFieldEffect(firstchoice[0].playerid, Card.field.GUN), true);
             }
             if (c.type == 2 ||(c.type == 3 && players[firstchoice[0].playerid].field.contains(c))) {
                 if (c.effect == Card.play.JAIL.ordinal()) {
                     if(p2.role.ordinal()== Constants.Role.OUTLAW.ordinal()){
                         return false; //cannot jail sheriff
                     } else if(p1==p2){
                         return false; //cannot jail self
                     }
                     for(Card card :p2.field){
                         if(card.effect == Card.play.JAIL.ordinal()) //TODO: this sort of check should be for all field cards
                             return false; //TODO: technically, this should be allowed, and the old jail should be discarded
                     }
                 } else if (c.effect == Card.play.HEAL.ordinal()) {
                     if (c.target == Targets.SELF && 
                         p1.lifePoints == p1.maxLifePoints) { //can't beer self with max hp
                         System.out.println("ILLEGAL CARD: you are already at maxhp");
                         return false;
                     } else if (c.target == Targets.ONE && 
                                p2.lifePoints == p2.maxLifePoints) {
                         System.out.println("ILLEGAL CARD: target is already at maxhp");
                         return false;
                     }
                 } else if(c.effect == Card.play.DAMAGE.ordinal()){ 
                     if(p1 == p2) {
                         System.out.println("ILLEGAL CARD: why would you shoot yourself?");
                         return false;
                     }
                 }
             } else if (c.type == 4 && 
                        c.effect == Card.play.MISS.ordinal()) { //can't play miss
                 System.out.println("ILLEGAL CARD: can't play miss on turn");
                 return false;
             }
             if(c.target == Targets.ONE && c.range != -1){
                 if(c.effect == Card.play.DAMAGE.ordinal()){ 
                     if(c.range == 0){
                         int gun = (playerHasFieldEffect(p1.id, Card.field.GUN)!=-1?p1.field.get(playerHasFieldEffect(p1.id, Card.field.GUN)).range:1);
                         if(getRangeBetweenPlayers(p1, p2)>gun){
                             //System.out.println("YOU RANGE: "+gun+". Target distance: "+ getRangeBetweenPlayers(p1, p2));
                             server.sendInfo(p1.id, "InfoMsg:Player "+server.choice.get(1)[0].choice+" out of range. "+gun+"/"+ getRangeBetweenPlayers(p1, p2)+":1");
                             return false;
                         }
                     }
                     else if(c.range == 1 && getRangeBetweenPlayers(p1, p2)>1)
                         return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * Gets the distance between p1 and p2. Includes horses and hideouts.
      * @param Player 1, Player 2
      * @return int of how far they are apart
      */
     public int getRangeBetweenPlayers(Player p1, Player p2) {
         int naturalRange = Math.min((numPlayers-p1.id+p2.id)%numPlayers, (numPlayers-p2.id+p1.id)%numPlayers);//seating order
         int distance = naturalRange;
         distance += playerHasFieldEffect(p1.id, Card.field.HORSE_CHASE)>-1?-1:0+playerHasFieldEffect(p2.id, Card.field.HORSE_RUN)>-1?1:0
             + (isCharacter(p1.id, Deck.Characters.SUZY_LAFAYETTE)?-1:0) + (isCharacter(p2.id, Deck.Characters.PAUL_REGRET)?1:0);
         return distance;
     }
     
     boolean isCharacter(int player, Deck.Characters e){
         if(players[player].character==e.ordinal())
             return true;
         return false;
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
         ArrayList<Constants.Role> roles = new ArrayList<Constants.Role>();
         players = new Player[numPlayers];
         for (int n = 0; n < numPlayers; n++) {
             players[n] = new Player(n, server.names.get(n));
         }
 
         switch (numPlayers) {
         case 2: //DEBUG MODE
             roles.add(Constants.Role.SHERIFF);
             roles.add(Constants.Role.OUTLAW);
             break;
         case 4:
             roles.add(Constants.Role.SHERIFF);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.RENEGADE);
             break;
         case 5:
             roles.add(Constants.Role.SHERIFF);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.RENEGADE);
             roles.add(Constants.Role.DEPUTY);
             break;
         case 6:
             roles.add(Constants.Role.SHERIFF);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.RENEGADE);
             roles.add(Constants.Role.DEPUTY);
             roles.add(Constants.Role.OUTLAW);
             break;
         case 7:
             roles.add(Constants.Role.SHERIFF);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.RENEGADE);
             roles.add(Constants.Role.DEPUTY);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.DEPUTY);
             break;
         case 8:
             roles.add(Constants.Role.SHERIFF);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.RENEGADE);
             roles.add(Constants.Role.DEPUTY);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.OUTLAW);
             roles.add(Constants.Role.RENEGADE);
             break;
         default:
             System.out.print("Bad number of players!");
             System.exit(0);
             break;
         }

         for (int n = 0; n < numPlayers; n++) {
            int role = roles.remove((int)(Math.random() * roles.size())).ordinal();
             if (role == 0) {
                 sheriff = n;
                 server.sendInfo("SetInfo:role:" + n + ":" + role);
             } else
                 server.sendInfo(n, "SetInfo:role:" + n + ":" + role);
             players[n].role = Constants.Role.values()[Integer.valueOf(role)];
         }
 
         deck = new Deck();
         deck.fillCharacterCards(numPlayers);
 
         for (int n = 0; n < numPlayers; n++) {
             playerDrawCard(n, 2);
         }
 
         server.promptAll("ChooseCharacter");
     }
 
 
     /**
      * After characters have been chosen, continue starting the game
      */
     public void start2() {
         Choice[] lastchoice = server.choice.get(server.choice.size() - 1);
 		for (int n = 0; n < lastchoice.length; n++) {
             players[n].characterCard = players[n].hand.get(lastchoice[n].choice);
             players[n].character = players[n].hand.get(lastchoice[n].choice).ordinal;
             server.sendInfo("SetInfo:character:"+n+":"+ players[n].hand.get(lastchoice[n].choice).ordinal);
         }
         for (int n = 0; n < lastchoice.length; n++) {
             changeMaxLifePoints(n, players[n].hand.get(lastchoice[n].choice).special + (n == sheriff ? 1 : 0));
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
             if (players[n].role == Constants.Role.SHERIFF) {
                 turn = n - 1;
                 break;
             }
         }
         server.choice.clear();
         nextTurn();
     }
 
     public void nextTurn() {
         turn++;
 
         if (server.choice.size() > 1) {
             System.out.println("ERROR: NEW TURN CALLED TOO EARLY");
             turn--;
             return;
         }
 
         //check if player is dead
         int oldturn = turn;
         int who = turn % numPlayers;
         while (players[who].lifePoints == 0 && 
                turn - oldturn < numPlayers) {
             turn++;
         }
 
         server.sendInfo("SetInfo:turn:" + turn);
         
         for(int n = 0; n<players[who].field.size(); n++){
             Card c = players[who].field.get(n);
             if(c.effect == Card.play.JAIL.ordinal()){
                 if(c.type==2){
                     playerFieldDiscardCard(who, n, true);
                      if(Math.random()<.75){
                         nextTurn();
                         return;
                     }
                 } 
                 else{ //dynamite
                     if(Math.random()<0){ //BOOM
                         playerFieldDiscardCard(who, n, true); 
                         System.out.println("ASPLODEDEDEDEDEDEDEDEDEDEDED!11111!!$!@#$!@#$");
                         //something to end the guy's turn if he's dead
                     }
                     else{
                         int a = turn+1;
                         while (players[a  % numPlayers].lifePoints == 0 && 
                                a - turn < numPlayers) {
                             a++;
                         }
                         if(players[who].field.get(n).effect2 != a%numPlayers){
                             server.sendInfo("SetInfo:PutInField:"+a%numPlayers+":"+
                                         players[who].field.get(n).ordinal);
                             players[a%numPlayers].field.add(players[who].field.get(n));
                         }
                         playerFieldDiscardCard(who, n, true);
                     }
                 }
             }
         }
         
         for(Card c : players[who].field){ //set all green cards played the turn before to 1
             c.location = 1;
         }
         
         players[who].bangs = 0;
         
         //draw two cards
         if (players[who].characterCard.effect != 1) { //TODO: get rid of specialDraw, move to a direct reference to character cards
             playerDrawCard(who, 2);
         } else {
             switch((Deck.Characters)players[who].characterCard.e){
                 case BLACK_JACK: //TODO: reveal second card drawn.
                     playerDrawCard(who, (Math.random()<.5?3:2));
                     break;
                 case JESSE_JONES:
                 playerDrawCard(who, 2);
                     break;
                 case KIT_CARLSON:
                 playerDrawCard(who, 2);
                     break;
                 case PEDRO_RAMIREZ:
                 playerDrawCard(who, 2);
                     break;
                 case BILL_NOFACE:
                 playerDrawCard(who, 2);
                     break;
                 case PAT_BRENNAN:
                 playerDrawCard(who, 2);
                     break;
                 case PIXIE_PETE:
                     playerDrawCard(who, 4);
                     break;
                 default: 
                     break;
                 //
             }
         }
         System.out.println("It is turn " + who);
         server.promptPlayer(who, "PlayCardUnforced");
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
                 switch ((Constants.Role)p.role) {
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
 
     /**
      * @param p The player whose max lifepoints to change
      * @param n The amount by which to change the max lifepoints
      */
     void changeMaxLifePoints(int p, int n) {
         server.sendInfo("SetInfo:maxHP:" + p + ":" + n);
         players[p].maxLifePoints += n;
         players[p].lifePoints = players[p].maxLifePoints;
     }
 
     /**
      * @param p The player whose lifepoints to change
      * @param n The amount by which to change the lifepoints
      */
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
     
     /**
      * @param p The player from whom to retrieve the card
      * @param n The card number to retrieve
      * @return the specified card
      */
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
     
     /**
      * Returns the index in field of the card in player p's field granting the effect e.
      * If p does not have the effect, returns -1.
      * @param p
      * @param e
      * @return
      */
     public int playerHasFieldEffect(int p, Card.field e){ //TODO: this
         for(int n = 0; n<players[p].field.size(); n++){
             Card c = players[p].field.get(n);
             if(e.ordinal()==c.effect){
                 return n;
             }
         }
         return -1;
     }
 }
