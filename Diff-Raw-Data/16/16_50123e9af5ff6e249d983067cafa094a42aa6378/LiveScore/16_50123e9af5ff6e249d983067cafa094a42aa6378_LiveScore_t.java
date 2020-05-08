 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav "Gussoh" Sohtell
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package silvertrout.plugins.livescore;
 
 import java.util.ArrayList;
 import silvertrout.Channel;
 import silvertrout.User;
 import silvertrout.commons.Color;
 
 /**
  *
  * @see silvertrout.Plugin
  * @see silvertrout.plugins
  */
 public class LiveScore extends silvertrout.Plugin {
     String channelName = "#sportit";
     ArrayList<FootballGame> games;
     ArrayList<Follower> followers;
     public LiveScore(){
         games = new ArrayList<FootballGame>();
         followers = new ArrayList<Follower>();
     }
     public ArrayList<FootballEvent> getNewEvents(FootballGame newgame, FootballGame oldgame) {
         ArrayList<FootballEvent> oldEvents = oldgame.events;
         ArrayList<FootballEvent> newEvents = newgame.events;
         ArrayList<FootballEvent> updatedEvents = new ArrayList<FootballEvent>();
         if (newgame.gametime.contains("FT") && !oldgame.gametime.contains("FT")) {
             if (newgame.events.isEmpty()) {
                 updatedEvents.add(new FootballEvent("Full time", "", "", ""));
             } else {
                 updatedEvents = newgame.events;
                 updatedEvents.add(new FootballEvent("Full time", "", "", ""));
             }
         } else if (newgame.gametime.contains("HT") && !oldgame.gametime.contains("HT")) {
             if (newgame.events.isEmpty()) {
                 updatedEvents.add(new FootballEvent("Halftime", "", "", ""));
             } else {
                 updatedEvents = newgame.events;
                 updatedEvents.add(new FootballEvent("Half time", "", "", ""));
             }
         } else if (newgame.gametime.contains("'") && oldgame.gametime.contains("HT")) {
             if (newgame.events.isEmpty()) {
                 updatedEvents.add(new FootballEvent("Second Half", "", "", ""));
             } else {
                 updatedEvents = newgame.events;
                 updatedEvents.add(new FootballEvent("Second Half", "", "", ""));
             }
         } else if (newgame.gametime.contains("'") && !oldgame.gametime.contains("'")) {
             if (newgame.events.isEmpty()) {
                 updatedEvents.add(new FootballEvent("First Half", "", "", ""));
             } else {
                 updatedEvents = newgame.events;
                 updatedEvents.add(new FootballEvent("First Half", "", "", ""));
             }
         }
        //for (FootballEvent newEvent : newEvents) {
        //    if (!oldEvents.contains(newEvent)) {
        //        updatedEvents.add(newEvent);
        //    }
        //}
        else{for (int i = 0; i < newEvents.size(); i++){
            if (!oldEvents.contains(newEvents.get(i))) {
                updatedEvents.add(newEvents.get(i));
             }
        }}
        System.out.println(updatedEvents.size());
            
         return updatedEvents;
         
     }
 
     public void onTick(int ticks) {
         if (ticks % 60 == 0) {
             System.out.println("STARTSPAM");
             LiveScoreParser p = new LiveScoreParser();
             ArrayList<FootballGame> newGames = p.getGames();
             ArrayList<FootballGame> updatedGames = new ArrayList<FootballGame>();
             ArrayList<FootballGame> addedGames = new ArrayList<FootballGame>();
             boolean addedGame = true;
             for (FootballGame newGame : newGames) {
                 addedGame = true;
                 for (FootballGame oldGame : games) {
                     if (oldGame.hometeam.equals(newGame.hometeam) && oldGame.league.equals(newGame.league)) {
                         addedGame = false;
                         ArrayList<FootballEvent> events = getNewEvents(newGame, oldGame);
                         if (!events.isEmpty()) {
                             updatedGames.add(new FootballGame(newGame.country, newGame.league, newGame.hometeam, newGame.awayteam, newGame.gametime, events, newGame.result));
                         } else if (!oldGame.result.equals(newGame.result)){
                             updatedGames.add(new FootballGame(newGame.country, newGame.league, newGame.hometeam, newGame.awayteam, newGame.gametime, events, newGame.result));
                         }
                     }
                 }
                 if (addedGame){
                     addedGames.add(newGame);
                 }
             }
             for (FootballGame f : addedGames){
                 for (Follower follower : followers) {
                     ArrayList<String> watchlist = follower.getWatchList();
                     for (String following : watchlist) {
                         if (f.hometeam.contains(following) || f.awayteam.contains(following) || f.league.contains(following) || f.country.contains(following)) {
                             String message = follower.getName() + ": ";//print out changes to follower
                             message += f.gametime + " " + f.hometeam + " - " + f.awayteam;
                             System.out.println(message);
                             follower.getChannel().sendPrivmsg(message);
                         }
                     }
                 }
             }
             
             games = newGames;
             
             for (FootballGame updatedGame : updatedGames) {
                 for (Follower follower : followers) {
                     ArrayList<String> watchlist = follower.getWatchList();
                     for (String following : watchlist) {
                         if (updatedGame.hometeam.contains(following) || updatedGame.awayteam.contains(following) || updatedGame.league.contains(following) || updatedGame.country.contains(following)) {
                             String message = follower.getName() + ": ";//print out changes to follower
                             message += updatedGame.gametime + " " + updatedGame.hometeam + " - " + updatedGame.awayteam + " " + updatedGame.result;
                             follower.getChannel().sendPrivmsg(message);
                             ArrayList<FootballEvent> events = updatedGame.events;
                             for (FootballEvent event : events) {
                                 message = event.matchtime + " " + Color.green(event.score);
                                 if (event.yellowcard) {
                                     message += Color.yellow(" YELLOW CARD " + event.playername);
                                 } else if (event.redcard) {
                                     message += Color.red(" RED CARD" + event.playername);
                                 } else if (event.goal) {
                                     message += Color.green(" GOAL " + event.playername);
                                 }
                                 follower.getChannel().sendPrivmsg(message);
                             }
                         }
                     }
                 }
             }
             System.out.println("ENDSPAM");
         }
     }
 
     @Override
     public void onPrivmsg(User user, Channel channel,
             String message) {
 
         if (channel != null) {
 
             if (message.startsWith("!watchlist")) {
                 String[] splitmess = message.split(" ");
                 if (splitmess.length < 2) {
                     channel.sendPrivmsg("Gief parameters!");
                     return;
                 }
                 if (splitmess[1].equals("add")) {
                     if (splitmess.length < 3) {
                         channel.sendPrivmsg(user.getNickname() + ": Nothing to add?");
                         return;
                     }
                     ArrayList<String> watchlist = new ArrayList<String>();
                     for (int i = 2; i < splitmess.length; i++){
                         watchlist.add(splitmess[i]);
                     }
                     int index = followers.indexOf(user.getNickname());
                     if (index > -1) {
                         Follower f = followers.get(index);
                         f.addToWatchlist(watchlist);
                     } else {
                         followers.add(new Follower(user.getNickname(),channel,watchlist));
                     }
                     for (FootballGame f : games){
                         for (String following : watchlist){
                         if (f.hometeam.contains(following) || f.awayteam.contains(following) || f.league.contains(following) || f.country.contains(following)) {
                            channel.sendPrivmsg(user.getNickname() + ": " + f.gametime + " " + f.hometeam + " - " + f.awayteam + " " + f.result);  
                         }}
                     }
                 } else if (splitmess[1].equals("remove")) {
                     if (splitmess.length < 3) {
                         channel.sendPrivmsg(user.getNickname() + ": Nothing to remove?");
                         return;
                     }
                     ArrayList<String> watchlist = new ArrayList<String>();
                     for (int i = 2; i < splitmess.length; i++){
                         watchlist.add(splitmess[i]);
                     }
                     int index = followers.indexOf(user.getNickname());
                     if (index > -1) {
                         Follower f = followers.get(index);
                         f.removeFromWatchlist(watchlist);
                     } else {
                         channel.sendPrivmsg(user.getNickname() + ": You have no watchlist");
                     }
                 }
 
 
             }
         }
     }
     public void onConnected() {
         // Join channel:
         if(!getNetwork().isInChannel(channelName)) {
             getNetwork().getConnection().join(channelName);
         }
     }
 }
