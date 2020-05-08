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
 
 /**
  * Abstract plugin class. All new plugins should inherit this class and 
  * overload the functions they want. As default the on* handlers do nothing.
  *
 * @see silvertrout.Network#loadPlugin
 * @see silvertrout.Network#unloadPlugin
  *
  *
  */
 public abstract class Plugin {
 
     /**
      * Network that loaded the plugin.
      */
     private Network network;
 
     /**
      * Set Network that loaded the plugin
      */
     protected void setNetwork(Network network) {
         this.network = network;
     }
 
     /**
      * Return Network that loaded the plugin
      */
     public Network getNetwork() {
         return network;
     }
 
     /**
      * This is the onPart handling function. The function gets called whenever a
      * user parts in one of the channels that are joined.
      *
      * @param  user         User that parted
      * @param  channel      Channel the user parted from
      * @param  partMessage  Part message
      */
     public void onPart(User user, Channel channel, String partMessage) {
     }
 
     /**
      * This is the onJoin handling function. The function gets called whenever a
      * user joins a channel.
      *
      * @param  user         User that joined
      * @param  channel      Channel the user joined
      */
     public void onJoin(User user, Channel channel) {
     }
 
     /**
      * This is the onQuit handling function. The function gets called whenever a
      * user quits from IRC.
      *
      * @param  user         User that quit
      * @param  quitMessage  Quit message
      */
     public void onQuit(User user, String quitMessage) {
     }
 
     // TODO:
     public void onInvite(User nick, String channelName) {
     }
     /* public void onBan(...) { } */
 
     /**
      * This is the onKick handling function. The function gets called whenever a
      * operator kicks someone from a channel.
      *
      * @param  user         User that initiated the kick
      * @param  channel      Channel the user was kicked from
      * @param  kicked       User that where kicked
      * @param  kickReason   Reason for the kick / comment
      */
     public void onKick(User user, Channel channel, User kicked,
             String kickReason) {
     }
 
     /**
      * This is the onPrivmsg handling function. The function gets called whenever
      * a private message is sent to you privetly or to a channel you have joined.
      * <p>
      * NOTE: If channel is NULL then it is a private message directly to you. If
      * not then the message was written in a channel.
      *
      * @param  user         User that wrote the message
      * @param  channel      Channel the message was written in (if any)
      * @param  message      Message
      */
     public void onPrivmsg(User user, Channel channel, String message) {
     }
 
     /**
      * This is the onNotice handling function. The function gets called whenever
      * a user notifies a channel or user.
      * <p>
      * NOTE: The IRC protocol clearly states that "automatic replies MUST NEVER
      * be sent in response to a NOTICE message", so don't do that.
      *
      * @param  user         User that parted
      * @param  channel      Channel the user pareted from
      * @param  message      Message
      */
     public void onNotice(User user, Channel channel, String message) {
     }
 
     /**
      * This is the onTick handling function. The function gets called every
      * second.
      *
      * @param  ticks  The number of seconds since start
      */
     public void onTick(int ticks) {
     }
 
     /**
      * This is the onTopic handling function. The function gets called every
      * time a topic is changed in a channel.
      * <p>
      * NOTE: You can get the new topic by calling channel.getTopic().
      *
      * @param  user      User that change the topic
      * @param  channel   Channel whose topic was changed
      * @param  oldTopic  The old topic
      */
     public void onTopic(User user, Channel channel, String oldTopic) {
     }
 
     /**
      * This is the onNick handling function. The function gets called every
      * time a user change its nick.
      * <p>
      * NOTE: You can get the new nick by calling user.getNick().
      *
      * @param  user         User that change its nick
      * @param  oldNickname  The old nickname
      */
     public void onNick(User user, String oldNickname) {
     }
 
     /*
     TODO: onMode
      */
     // TODO: change from string to User
     /**
      * This is the onPing handling function. The function gets called every
      * time you are pinged.
      *
      * @param  from  The user who sent the ping
      */
     public void onPing(String id) {
     }
 
     /**
      * This is the onLoad handling function. The function gets called every time
      * the plugin is loaded.
      */
     public void onLoad() {
     }
 
     /**
      * This is the onUnload handling function. The function gets called every
      * time the plugin is unloaded.
      */
     public void onUnload() {
     }
 
     /**
      * This is the onConnected handling function. The function gets called every
      * time we get connected.
      */
     public void onConnected() {
     }
 
     /**
      * This is the onDisconnected handling function. The function gets called
      * every time we get disconnected.
      */
     public void onDisconnected() {
     }
 }
