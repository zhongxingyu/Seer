 package controllers.server;
 
 import channels.messages.ChannelMessage;
 import messages.*;
 import model.Player;
 import model.Roles;
 
 import java.util.*;
 
 public class Game {
     private List<Player> players;
 
     private Game (List<Player> players) {
         this.players = players;
     }
 
     public void assignRoles () {
         Random randomGenerator = new Random ();
         int assignedMafia = 0;
         setDefaultRole ();
         int mafiaCount = players.size() / 3;
         while (assignedMafia != mafiaCount) {
             int playerId = randomGenerator.nextInt (players.size ());
             if (!players.get (playerId).isMafia ()) {
                 assignedMafia++;
                 players.get (playerId).setRole (Roles.MAFIA);
             }
         }
         informRoles ();
     }
 
     private void setDefaultRole () {
         for (Player aPlayer : players) {
             aPlayer.setRole (Roles.VILLAGER);
         }
     }
 
     public String[][] getPlayersChart () {
         String[][] playersData = new String[players.size ()][2];
         for (int i = 0; i < players.size (); i++) {
             playersData[i][0] = players.get (i).getName ();
             playersData[i][1] = players.get (i).getRole ().name ();
         }
         return playersData;
     }
 
     private void informRoles () {
         for (Player player : players) {
             player.sendRoleMessage ();
         }
     }
 
     private void sendMafiaListToMafias () {
         List<String> mafiasList = new ArrayList<String> ();
         for (Player player : players) {
             if (player.isMafia ()) mafiasList.add (player.getName ());
         }
         MafiaMessage message = new MafiaMessage ();
         message.setMafiasName (mafiasList.toArray (new String[mafiasList.size ()]));
         sendMessageToMafia (message);
     }
 
     private void sendVillagersListToMafia () {
         List<String> villagersList = getVillagersName ();
         VillagersListMessage message = new VillagersListMessage ();
         message.setVillagersName (villagersList.toArray (new String[villagersList.size ()]));
         sendMessageToMafia (message);
     }
 
     private List<String> getVillagersName () {
         List<String> villagersList = new ArrayList<String> ();
         for (Player player : players) {
             villagersList.add (player.getName ());
         }
         return villagersList;
     }
 
     private void sendMessageToVillager (ChannelMessage message) {
         for (Player player : players) {
             player.send (message);
         }
     }
 
     private void sendMessageToMafia (ChannelMessage message) {
         for (Player player : players) {
             if (player.isMafia ()) player.send (message);
         }
     }
 
     public void sendPlayersListToMafias () {
         sendMafiaListToMafias ();
         new Timer ().schedule (new TimerTask () {
             @Override
             public void run () {
                 sendVillagersListToMafia ();
             }
         }, 1000);
     }
 
     public static Game createGame (List<Player> players) {
         if (players == null) throw new IllegalArgumentException ("Cannot create game of players : " + players);
         return new Game (players);
     }
 
     public void calculateDayVotes (String name, String voteTo) {
         for (Player player : players) {
             player.initializeVotes ();
         }
         for (Player player : players) {
             for (Player player1 : players) {
                 if (player1.getVoteTo ().equals (player.getName ()))
                     player.incrementVote ();
             }
         }
         sendVillagerVoteInfo (name, voteTo);
     }
 
     public void sendNightVotesInfoToMafias (String name, String voteTo) {
         for (Player player : players) {
             player.initializeVotes ();
         }
         for (Player player : players) {
             for (Player player1 : players) {
                 if (player1.isMafia () && player1.getVoteTo ().equals (player.getName ()))
                     player.incrementVote ();
             }
         }
         sendMafiaVoteInfo (name, voteTo);
     }
 
     private String[] voteDetails () {
         List<String> voteList = new ArrayList<String> ();
         for (Player player : players) {
             voteList.add ("Name : " + player.getName () + " Votes : " + player.getVotes ());
         }
         return voteList.toArray (new String[voteList.size ()]);
     }
 
 
     private void sendMafiaVoteInfo (String name, String voteTo) {
         String[] voteList = voteDetails ();
         VoteInformationMessage voteInfo = new VoteInformationMessage ();
         voteInfo.setVoteInformation(name + " Voted To " + voteTo);
         voteInfo.setVoteList(voteList);
         sendMessageToMafia (voteInfo);
 
     }
 
     private void sendVillagerVoteInfo (String name, String voteTo) {
         String[] voteList = voteDetails ();
         VoteInformationMessage message = new VoteInformationMessage ();
         message.setVoteInformation (name + " Voted To " + voteTo);
         message.setVoteList (voteList);
         sendMessageToVillager (message);
     }
 
     private Player getEliminatedPlayer () {
         Player outPlayer = players.get (0);
         for (int i = 1; i < players.size (); i++) {
             if (players.get (i).getVotes () > outPlayer.getVotes ()) outPlayer = players.get (i);
         }
         return outPlayer;
     }
 
     public void sendEliminatedPlayerInfo () {
         final Player outPlayer = getEliminatedPlayer ();
 
         final EliminationMessage eliminatedPlayer = new EliminationMessage ();
         eliminatedPlayer.setEliminatedPlayerInfo (outPlayer.getName ());
         new Timer ().schedule (new TimerTask () {
             @Override
             public void run () {
                 sendMessageToVillager (eliminatedPlayer);
             }
         }, 1000);
         sendDeadMessageToEliminatedPlayer(outPlayer);
         players.remove (outPlayer);
     }
 
     private void sendDeadMessageToEliminatedPlayer (Player outPlayer) {
         DeadMessage message = new DeadMessage();
         outPlayer.send (message);
     }
 
     public boolean calculateRatio () {
         int noOfMafias = 0;
         int noOfVillagers = 0;
         for (Player player : players) {
             if (player.isMafia ()) noOfMafias++;
             else noOfVillagers++;
         }
         if (noOfMafias == 0) {
             sendResultMessage ("Villagers Won !!!");
             return true;
         }
         if (noOfMafias >= noOfVillagers) {
             sendResultMessage ("Mafias Won !!!");
             return true;
         }
         return false;
     }
 
     public void sendDayArrivedMessage () {
         DayArrivedMessage startDayTime = new DayArrivedMessage ();
         sendMessageToVillager (startDayTime);
     }
 
     private void sendResultMessage (String resultOfGame) {
         ResultMessage result = new ResultMessage ();
         result.setResultOfGame (resultOfGame);
         sendMessageToVillager (result);
     }
 
     public void sendNightArrivedMessage () {
         NightArrivedMessage night = new NightArrivedMessage ();
         for (Player player : players) {
             player.send (night);
         }
     }
 
     public void sendPlayersListToVillagers () {
         new Timer ().schedule (new TimerTask () {
             @Override
             public void run () {
                 sendVillagersListToVillagers ();
             }
         }, 1000);
     }
 
     private void sendVillagersListToVillagers () {
         List<String> allVillagersName = getVillagersName ();
         VillagersListMessage message = new VillagersListMessage ();
         message.setVillagersName (allVillagersName.toArray (new String[allVillagersName.size ()]));
         sendMessageToVillager (message);
     }
 }
