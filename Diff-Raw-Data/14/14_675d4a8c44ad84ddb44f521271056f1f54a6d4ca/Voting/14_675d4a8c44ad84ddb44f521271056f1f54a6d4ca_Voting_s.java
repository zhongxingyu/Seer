 package to.joe.manager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import to.joe.J2;
 import to.joe.util.Flag;
 
 public class Voting {
     private final J2 j2;
     private ArrayList<String> currentOptions;
     private String currentQuestion;
     private boolean voteInProgress;
     private HashMap<String, Integer> votes;
     private int tallyTaskNumber;
     private final Object votesSync = new Object();
     private final Object optionsSync = new Object();
 
     public Voting(J2 j2) {
         this.j2 = j2;
     }
 
     /**
      * Tell player the usage.
      * 
      * @param player
      */
     private void usageVoteAdmin(Player player) {
         player.sendMessage(ChatColor.RED + "Usage: /voteadmin start|cancel");
     }
 
     /**
      * Called when a player says /vote Record to this.votes String = playername
      * Integer = option they voted for
      * 
      * @param player
      * @param args
      */
     public void voteCommand(Player player, String[] args) {
         final String name = player.getName();
         if (args.length == 0) {
             player.sendMessage(ChatColor.RED + "/vote #");
             return;
         }
         final boolean adminStart = args[0].startsWith("\"");
         if (adminStart || args[0].startsWith("cancel")) {
             if (this.j2.hasFlag(name, Flag.ADMIN)) {
                 if (adminStart) {
                     if (this.voteInProgress) {
                         player.sendMessage(ChatColor.RED + "Vote already in progress");
                     }
                    String combined = this.j2.combineSplit(1, args, " ");
                     if (!combined.startsWith("\"") || !combined.endsWith("\"")) {
                         this.usageVoteAdmin(player);
                         return;
                     }
 
                     combined = combined.substring(1, combined.length() - 1);
                     String[] bits = combined.split("\" \"");
 
                     if (bits.length > 6) {
                         player.sendMessage(ChatColor.RED + "Too many options");
                         return;
                     }
                     if (bits.length < 2) {
                         final String q = bits[0];
                         bits = new String[3];
                         bits[0] = q;
                         bits[1] = "Yes";
                         bits[2] = "No";
                     }
                     this.voteInProgress = true;
                     this.currentQuestion = bits[0];
                     this.j2.chat.muteAll = true;
                     this.j2.chat.messageByFlag(Flag.ADMIN, ChatColor.AQUA + name + " has started a vote.");
                     this.j2.chat.messageByFlagless(Flag.ADMIN, ChatColor.AQUA + "Admin has started a vote.");
                     this.j2.chat.messageAll(ChatColor.DARK_AQUA + "Question: " + ChatColor.AQUA + bits[0]);
                     synchronized (this.optionsSync) {
                         this.currentOptions = new ArrayList<String>();
                         for (int x = 1; x < bits.length; x++) {
                             this.currentOptions.add(bits[x]);
                             this.j2.chat.messageAll(ChatColor.DARK_AQUA.toString() + x + ". " + ChatColor.AQUA + bits[x]);
                         }
                     }
                     this.j2.chat.messageAll(ChatColor.AQUA + "Say " + ChatColor.DARK_AQUA + "/vote x" + ChatColor.AQUA + " where x is the answer #");
                     this.j2.chat.muteAll = false;
                     this.votes = new HashMap<String, Integer>();
                     // Run the run() method of VoteTally in 30 seconds
                     this.tallyTaskNumber = this.j2.getServer().getScheduler().scheduleAsyncDelayedTask(this.j2, new VoteTally(this.j2), 600L);
                 } else {
                     if (this.voteInProgress) {
                         this.j2.getServer().getScheduler().cancelTask(this.tallyTaskNumber);
                         this.tallyTaskNumber = -1;
                         this.j2.sendAdminPlusLog(ChatColor.AQUA + name + " canceled the vote.");
                         this.j2.chat.messageByFlagless(Flag.ADMIN, ChatColor.RED + "Admin canceled the voting.");
                         this.cleanUp();
                     } else {
                         player.sendMessage(ChatColor.RED + "You derp there isn't any vote");
                     }
                 }
             } else {
                 player.sendMessage(ChatColor.RED + "That's not a number!");
                 return;
             }
         }
         int option;
         try {
             option = Integer.parseInt(args[0]);
         } catch (final NumberFormatException e) {
             player.sendMessage(ChatColor.RED + "That's not a number!");
             return;
         }
         if (!this.voteInProgress) {
             player.sendMessage(ChatColor.RED + "There is no voting in session.");
         } else if ((args.length != 1) || (option > this.currentOptions.size())) {
             player.sendMessage(ChatColor.RED + "Usage: /vote # - # must be a valid option.");
         } else {
             synchronized (this.votesSync) {
                 if (this.votes.containsKey(name)) {
                     player.sendMessage(ChatColor.AQUA + "Vote changed.");
                 } else {
                     player.sendMessage(ChatColor.AQUA + "Thanks for voting.");
                 }
                 synchronized (this.optionsSync) {
                     player.sendMessage(ChatColor.DARK_AQUA + "Your choice: " + ChatColor.AQUA + this.currentOptions.get(option - 1));
                 }
                 this.votes.put(name, option);
             }
         }
     }
 
     /**
      * @return the current question
      */
     private String getCurrentQuestion() {
         return this.currentQuestion;
     }
 
     /**
      * @return The current options
      */
     private ArrayList<String> getCurrentOptions() {
         synchronized (this.optionsSync) {
             return new ArrayList<String>(this.currentOptions);
         }
     }
 
     /**
      * @return the current votes.
      */
     private HashMap<String, Integer> getVotes() {
         return new HashMap<String, Integer>(this.votes);
     }
 
     /**
      * vote done! :)
      */
     private void cleanUp() {
         this.voteInProgress = false;
     }
 
     private class VoteTally implements Runnable {
         private final J2 j2;
 
         public VoteTally(J2 j2) {
             this.j2 = j2;
         }
 
         @Override
         public void run() {
             this.j2.chat.muteAll = true;
             this.j2.chat.messageAll(ChatColor.AQUA + "Vote over! Tallying results...");
             final Collection<Integer> votes = this.j2.voting.getVotes().values();
             final ArrayList<String> options = this.j2.voting.getCurrentOptions();
             int totalVotes = 0;
             final int[] tally = new int[options.size()];
             System.out.println(options.size() + " options");
             for (int x = 0; x < tally.length; x++) {
                 tally[x] = 0;
             }
             final Iterator<Integer> voterator = votes.iterator();
             while (voterator.hasNext()) {
                 totalVotes++;
                 tally[voterator.next() - 1]++;
             }
             int winner = 0;
             int highest = tally[0];
             boolean tie = false;
             for (int x = 1; x < tally.length; x++) {
                 if (tally[x] > highest) {
                     tie = false;
                     winner = x;
                     highest = tally[x];
                 } else if (tally[x] == highest) {
                     tie = true;
                 }
             }
             if (!tie) {
                 final int percent = (100 * highest) / totalVotes;
                 this.j2.chat.messageAll(ChatColor.DARK_AQUA + "Result of \"" + ChatColor.AQUA + this.j2.voting.getCurrentQuestion() + ChatColor.DARK_AQUA + "\" is:");
                 this.j2.chat.messageAll(ChatColor.DARK_AQUA + "(" + percent + "% of " + totalVotes + "): " + ChatColor.AQUA + options.get(winner));
             } else {
                 this.j2.chat.messageAll(ChatColor.DARK_AQUA + "FAILURE! " + ChatColor.AQUA + "Votes tied, we all lose!");
             }
             // In here goes tallying.
             // Access via this.voting.getCurrentQuestion() and
             // this.voting.getCurrentOptions()
             // and this.voting.getVotes()
             // In here is where you announce winners
 
             // j2.chat.messageAll("A WINNAR IS OPTION X");
 
             // Lastly, vote is no longer in progress
             this.j2.voting.cleanUp();
             this.j2.chat.muteAll = false;
         }
     }
 }
