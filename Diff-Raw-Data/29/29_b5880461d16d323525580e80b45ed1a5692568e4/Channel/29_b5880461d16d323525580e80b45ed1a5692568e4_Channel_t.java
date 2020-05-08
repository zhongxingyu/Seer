/*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav Sothell
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
 
 package silvertrout;
 
 import java.util.*;
 
 /**
  * 
  *
  */
 public class Channel {
 
     /**
      * Channel name
      */
     private String name;
     /**
      * Channel topic
      */
     private String topic;
     /**
      * Channel modes
      */
     private Modes modes;
     /**
      * Channel users and their modes
      */
     private Map<User, Modes> users;
     /**
      * Network the channel is in
      */
     private Network network;
 
     /**
      * Creates a channel on the specified network
      *
      * @param  name     Name of the channel
      * @param  network  Network the channel is in
      */
     Channel(String name, Network network) {
         this.name = name;
         this.network = network;
         this.topic = "";
 
         this.users = new HashMap<User, Modes>();
     }
 
     /**
      * Add a user with the specified modes to the channel
      *
      * @param  user  User to add
      * @param  mode  Modes to give user in the channel
      */
     protected void addUser(User user, Modes mode) {
         users.put(user, mode);
     }
 
     /**
      * Remove a user from the channel
      *
      * @param  user  User to remove
      */
     protected void delUser(User user) {
         users.remove(user);
     }
 
     /**
      * Returns the name of the channel
      *
      * @return  The name of the channel
      */
     public String getName() {
         return this.name;
     }
 
     /**
      * Returns all user in the channel and their modes
      *
      * @return  The user and their modes
      */
     public Map<User, Modes> getUsers() {
         return this.users;
     }
 
     /**
      * Sets the topic of the channel
      *
      * @param  topic  The new topic
      */
     protected void setTopic(String topic) {
         this.topic = topic;
     }
 
     /**
      * Returns the current topic
      *
      * @return  The current topic
      */
     public String getTopic() {
         return this.topic;
     }
 
     /**
      * Change the topic of the current channel. This commands sends a topic
      * change request to the IRC server.
      *
      * @param   topic  The topic to change to
      * @return  TODO
      */
     public boolean changeTopic(String topic) {
         network.sendRaw("TOPIC " + this.name + " " + topic);
         return true;
     }
 
     /**
      * Send a private message to this channel.
      *
      * @param  message  Message to send
      */
     public void sendPrivmsg(String message) {
         network.sendRaw("PRIVMSG " + this.name + " :" + message);
     }
 
     /**
      * Send an action to this channel. This is a special form of private mesasge
      * that only adds special characters. Works like /me in many IRC programs.
      *
      * @param  message  Action to send
      */
     public void sendAction(String action) {
         sendPrivmsg("ACTION " + action + "");
     }
 
     // TODO: boolean and fail if no operator?
     /**
      * Gives voice to an user in the channel. You need to be a channel operator
      * for this to have any effect.
      *
      * @param  user  The user to give voice to
      */
     public void giveVoice(User user) {
         if (getUsers().containsKey(user)) {
             network.sendRaw("MODE " + getName() + " +v " + user.getNickname());
         }
     }
 
     /**
      * Take voice from an user in the channel. You need to be a channel operator
      * for this to have any effect.
      *
      * @param  user  The user to take voice from
      */
     public void deVoice(User user) {
         if (getUsers().containsKey(user)) {
             network.sendRaw("MODE " + getName() + " -v " + user.getNickname());
         }
     }
 
     /**
      * Gives operator status to an user in the channel. You need to be a channel
      * operator for this to have any effect.
      *
      * @param  user  The user to give operator status to
      */
     public void giveOp(User user) {
         if (getUsers().containsKey(user)) {
             network.sendRaw("MODE " + getName() + " +o " + user.getNickname());
         }
     }
 
     /**
      * Takes operator status from an user in the channel. You need to be a
      * channel operator for this to have any effect.
      *
      * @param  user  The user to take operator status from
      */
     public void deOp(User user) {
         if (getUsers().containsKey(user)) {
             network.sendRaw("MODE " + getName() + " -o " + user.getNickname());
         }
     }
 
     /**
      * Kick a user from the channel. You need to be a channel operator for this
      * to have any effect.
      *
      * @param  user    The user to kick
      * @param  reason  Reason for the kick / Message
      */
     public void kick(User user, String reason) {
         if (getUsers().containsKey(user)) {
             network.sendRaw("KICK " + getName() + " " + user.getNickname() + " :" + reason);
         }
     }
 
     /**
      * Part from (leave) the channel.
      */
     public void part() {
         network.sendRaw("PART " + getName());
     }
 
     /**
      * Take half-op status from an user in the channel.
      * You need to be a channel operator for this to have any effect.
      *
      * @param  user  The user to take half-op from
      */
     public void deHalfOp(User user) {
         if (getUsers().containsKey(user)) {
             network.sendRaw("MODE " + getName() + " -h " + user.getNickname());
         }
     }
 
     /**
      * Give half-op to an user in the channel. You need to be a channel operator
      * for this to have any effect.
      *
      * @param  user  The user to give half-op status
      */
     public void giveHalfOp(User user) {
         if (getUsers().containsKey(user)) {
             network.sendRaw("MODE " + getName() + " +h " + user.getNickname());
         }
     }
 }
