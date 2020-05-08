 package net.mctitan.infraction;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * Represents a players data, includign infractions received/given and the
  * name of the player in the plugin
  * 
  * @author Colin
  */
 public class PlayerData implements Serializable {
     /** infractions received from the moderators+ */
     private LinkedList<Infraction> infractions;
     
     /** infractions given out to players */
     private LinkedList<Infraction> moderations;
     
     /** donations player used to pardon/delete infractions */
     private HashMap<String, Integer> donations;
     
     /** name of this player, should be gotten from Player.getName() */
     public String name;
     
     /** generic output given to moderator+ players on player join, if they are good */
     private static transient final String PLAYER_JOIN_GOOD =
             InfractionRegex.COLOR_PLAYER_REGEX+InfractionRegex.PLAYER_REGEX+" "+InfractionRegex.COLOR_NORMAL_REGEX
             +"has no infractions :)";
     
     /** generic output given to moderator+ players on player join, if they are bad */
     private static transient final String PLAYER_JOIN_BAD =
             InfractionRegex.COLOR_PLAYER_REGEX+InfractionRegex.PLAYER_REGEX+" "+InfractionRegex.COLOR_NORMAL_REGEX
             +"has "+InfractionRegex.COLOR_AMOUNT_REGEX+InfractionRegex.AMOUNT_REGEX+" "+InfractionRegex.COLOR_NORMAL_REGEX
             +"infraction(s)";
     
     /** generic header for infractions given out by the player */
     private static transient final String OUTPUT_PLAYER_HEADER =
             InfractionRegex.COLOR_PLAYER_REGEX+InfractionRegex.PLAYER_REGEX+"'s "+InfractionRegex.COLOR_NORMAL_REGEX
             +"Infractions (Page "+InfractionRegex.COLOR_PAGE_REGEX+InfractionRegex.PAGE_REGEX+InfractionRegex.COLOR_NORMAL_REGEX
             +"/"+InfractionRegex.COLOR_PAGE_REGEX+InfractionRegex.PAGES_REGEX+InfractionRegex.COLOR_NORMAL_REGEX+")";
     
    /** generic header for ifnractions received by the players */
     private static transient final String OUTPUT_MODERATOR_HEADER =
             InfractionRegex.COLOR_ISSUER_REGEX+InfractionRegex.PLAYER_REGEX+"'s "+InfractionRegex.COLOR_NORMAL_REGEX
             +"Given Infractions (Page "+InfractionRegex.COLOR_PAGE_REGEX+InfractionRegex.PAGE_REGEX+InfractionRegex.COLOR_NORMAL_REGEX
             +"/"+InfractionRegex.COLOR_PAGE_REGEX+InfractionRegex.PAGES_REGEX+InfractionRegex.COLOR_NORMAL_REGEX+")";
     
     /** generic output line of a single infraction */
     private static transient final String OUTPUT_LINE =
             InfractionRegex.COLOR_ID_REGEX+InfractionRegex.ID_REGEX+") "+InfractionRegex.INFRACTION_REGEX;
     
     /** number of infractions to display in fast mode */
     private static transient final int FAST_INFRACTIONS_PER_PAGE = 10;
     
     /** numnber of infractions to display in full mode */
     private static transient final int FULL_INFRACTIONS_PER_PAGE = 5;
     
     /**
      * Constructs a player data, only needs their name
      * @param name player name should be gotten from Player.getName()
      */
     public PlayerData(String name) {
         this.name = name;
         infractions = new LinkedList<Infraction>();
         moderations = new LinkedList<Infraction>();
         donations = new HashMap<String, Integer>();
     }
     
     /**
      * Tests to see if the player that is represented by this data is banned or not
      * 
      * @return true if banned, false otherwise
      */
     public boolean isBanned() {
         //test to see if there is an infraction
         if(infractions.isEmpty())
             return false;
         
         //get the top infraction from the player
         Infraction infract = infractions.getFirst();
         
         //if it isn't a ban, they are not banned
         if(!infract.type.equals(InfractionType.BAN))
             return false;
         
         //if there is a pardon, there are not banned
         if(infract.infract != null)
             return false;
         
         //all else fails, they are banned
         return true;
     }
     
     /**
      * Gets the player color, either good or bad
      * 
      * @return the color of the player
      */
     public InfractionChatColor getColor() {
         //if the player has any infractions, pardoned or not, return bad
         if(!infractions.isEmpty())
             return InfractionChatColor.COLOR_BAD_PLAYER;
         
         //failing that, a good player
         return InfractionChatColor.COLOR_GOOD_PLAYER;
     }
     
     /**
      * gets the player join message that is sent to other moderators
      * 
      * @param name name of player the message is being sent to
      * @return message to be sent, tailord for the person receiving it
      */
     public String getPlayerJoin(String name) {
         String msg = (getNumberInfractions()==0?PLAYER_JOIN_GOOD:PLAYER_JOIN_BAD);
         String pname = (this.name.equals(name)?"You":this.name);
         
         //replace data in output
         msg = msg.replace(InfractionRegex.PLAYER_REGEX, pname);
         msg = msg.replace(InfractionRegex.AMOUNT_REGEX, ""+getNumberInfractions());
         
         //replace color data in output
         msg = InfractionChatColor.replaceColor(msg, getColor());
         msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COlOR_NORMAL);
         msg = InfractionChatColor.replaceColor(msg, InfractionChatColor.COLOR_AMOUNT);
         
         return msg;
     }
     
     /**
      * gets the full infraction output for the player represented by this data
      * @param name player name to receive this data
      * @param page page of infractions to look at, first page is 1
      * @param moderator whether we are looking at the received or sent infractions
      * @return message to be sent, tailored for the person receiving it
      */
     public LinkedList<String> getInfractionOutput(String name, int page, boolean moderator, boolean fast) {
         LinkedList<String> output = new LinkedList<String>();
         LinkedList<Infraction> infracts = (moderator?moderations:infractions);
         int infractsPerPage = (fast?FAST_INFRACTIONS_PER_PAGE:FULL_INFRACTIONS_PER_PAGE);
         int pages = 1+(infracts.size()-1)/infractsPerPage;
         
         //check current page to make sure it isn't out of bounds
         if(page > pages)
             page = pages;
         
         //get the header
         String header = getHeader(name, moderator, page, pages);
         output.add(header);
         
         //change page into 0 order
         --page;
         
         int start = infractsPerPage*page;
         Iterator<Infraction> iter = infracts.listIterator(start);
         Infraction infract;
         String line;
         for(int i = 1; i <= infractsPerPage; ++i) {
             if(!iter.hasNext())
                 break;
             infract = iter.next();
             line = OUTPUT_LINE;
             
             //change line data
             line = line.replace(InfractionRegex.ID_REGEX,""+(start+i));
             line = line.replace(InfractionRegex.INFRACTION_REGEX,(fast?infract.getOnFlyOutput(name):infract.getFullOutput(name)));
             
             //change line color data
             line = InfractionChatColor.replaceColor(line,InfractionChatColor.COLOR_ID);
             
             //add line to output
             output.add(line);
         }
         
         return output;
     }
     
     /**
      * Used to get the header for the 2 infraction outpus (both fast and full)
      * 
      * @param name player name receiving the header
      * @param moderator whether this is infraction or moderator output
      * @param page page number looking at
      * @param pages number of pages
      * @return 
      */
     private String getHeader(String name, boolean moderator, int page, int pages) {
         //get the correct generi header
         String header = (moderator?OUTPUT_MODERATOR_HEADER:OUTPUT_PLAYER_HEADER);
         
         //if names are a match, replace {player}'s with Your
         if(this.name.equals(name))
             header = header.replace(InfractionRegex.PLAYER_REGEX+"'s","Your");
         
         //replace data in the header
         header = header.replace(InfractionRegex.PLAYER_REGEX, this.name);
         header = header.replace(InfractionRegex.PAGE_REGEX, ""+page);
         header = header.replace(InfractionRegex.PAGES_REGEX, ""+pages);
         
         //place the color data in the header
         header = InfractionChatColor.replaceColor(header, getColor());
         header = InfractionChatColor.replaceColor(header, InfractionChatColor.COLOR_ISSUER);
         header = InfractionChatColor.replaceColor(header, InfractionChatColor.COlOR_NORMAL);
         header = InfractionChatColor.replaceColor(header, InfractionChatColor.COLOR_PAGE);
         
         return header;
     }
     
     /**
      * adds a received infraction to the player
      * 
      * @param infract infraction to be added
      */
     public void addPlayerInfraction(Infraction infract) {
         infractions.addFirst(infract);
     }
     
     /**
      * adds a sent infraction from the player
      * 
      * @param infract infraction to be added
      */
     public void addModeratorInfraction(Infraction infract) {
         moderations.addFirst(infract);
     }
     
     /**
      * removes the infraction from the infraction list
      * 
      * @param infract infraction to remove
      */
     public void rmInfraction(Infraction infract) {
         infractions.remove(infract);
     }
     
     /**
      * removes the infraction from the moderation list
      * 
      * @param infract infraction to remove
      */
     public void rmModeration(Infraction infract) {
         moderations.remove(infract);
     }
     
     /**
      * gets number of infractions reveived by player
      * 
      * @return number of received infractions
      */
     public int getNumberInfractions() {
         return infractions.size();
     }
     
     /**
      * gets number of infractions sent by player
      * @return number of sent infractions
      */
     public int getNumberModerations() {
         return moderations.size();
     }
     
     /**
      * gets a player's infraction by id number
      * 
      * @param id id of the infraction to get
      * @return the infraction represented by the id, or null if it doesn't exist
      */
     public Infraction getInfraction(int id) {
         //check to see if the id is out of range
         if((id < 0) || (id >= getNumberInfractions()))
             return null; //illegal returns null
         
         //return the appropriate infraction
         return infractions.get(id);
     }
     
     /**
      * gets a player's moderation by id number
      * 
      * @param id id of the moderation to get
      * @return the moderation represented by the id, or null if it doesn't exist
      */
     public Infraction getModeration(int id) {
         
         //check to see if the id is out of range
         if((id < 0) || (id >= getNumberModerations()))
             return null; //illegal returns null
         
         //return the appropriate infraction
         return moderations.get(id);
     }
     
     public int getDonations(String donation) {
         Integer i = donations.get(donation);
         if(i == null) i = 0;
         return i;
     }
     
     public void addDonation(String donation) {
         Integer i = donations.get(donation);
         if(i == null) i = 0;
         ++i;
         donations.put(donation,i);
     }
     
     /**
      * Used to test this class without a server running
      * @param args not used
      */
     public static void main(String [] args) {
         PlayerData player = new PlayerData("HerbieVersmells");
         PlayerData issuer = new PlayerData("mindless728");
         PlayerData other = new PlayerData("other");
         int numberInfractions = 1;
         
         //test the player join message with no infractions
         System.out.println("Player join with no infractions");
         System.out.println(player.getPlayerJoin(issuer.name));
         System.out.println();
         
         //create infractions
         for(int i = 0; i < numberInfractions; ++i)
             new Infraction(player,issuer,InfractionType.WARN,"being a dick - "+(i+1));
         
         //test the player join message with infractions
         System.out.println("Player join with infractions");
         System.out.println(player.getPlayerJoin(issuer.name));
         System.out.println();
         
         //test player fast output
         System.out.println("Player Fast Output");
         System.out.println(player.getInfractionOutput(player.name, 1, false, true));
         System.out.println(player.getInfractionOutput(issuer.name, 1, false, true));
         System.out.println(player.getInfractionOutput(other.name, 1, false, true));
         System.out.println();
         
         //test player full output
         System.out.println("Player Full Output");
         System.out.println(player.getInfractionOutput(player.name, 1, false, false));
         System.out.println(player.getInfractionOutput(issuer.name, 1, false, false));
         System.out.println(player.getInfractionOutput(other.name, 1, false, false));
         System.out.println();
         
         //test moderator fast output
         System.out.println("Moderator Fast Output");
         System.out.println(issuer.getInfractionOutput(player.name, 1, true, true));
         System.out.println(issuer.getInfractionOutput(issuer.name, 1, true, true));
         System.out.println(issuer.getInfractionOutput(other.name, 1, true, true));
         System.out.println();
         
         //test moderator full output
         System.out.println("Player Fast Output");
         System.out.println(issuer.getInfractionOutput(player.name, 1, true, false));
         System.out.println(issuer.getInfractionOutput(issuer.name, 1, true, false));
         System.out.println(issuer.getInfractionOutput(other.name, 1, true, false));
         System.out.println();
     }
 }
