 package uk.co.CyniCode.CyniChat.Chatting;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.libs.com.google.gson.annotations.Expose;
 import org.bukkit.entity.Player;
 
 import uk.co.CyniCode.CyniChat.CyniChat;
 import uk.co.CyniCode.CyniChat.DataManager;
 import uk.co.CyniCode.CyniChat.PermissionManager;
 import uk.co.CyniCode.CyniChat.Channel.Channel;
 
 /**
  * UserDetails class and object which stores all of the relevant information for each and every player
  * @author Matthew Ball
  *
  */
 public class UserDetails {
 	
<<<<<<< HEAD
	private int ID;
=======
<<<<<<< Updated upstream
=======
 	private int ID = 0;
>>>>>>> Stashed changes
>>>>>>> localfix2
 	private Player player;
 	@Expose
 	private String CurrentChannel = CyniChat.def_chan;
 	private String LastMessage = null;
 	private Boolean afk = false;
 	@Expose
 	private Boolean Silenced = false;
 	@Expose
 	private Boolean CanIgnore = true;
 	@Expose
 	private List<String> JoinedChannels = new ArrayList<String>();
 	@Expose
 	private List<String> BannedFrom = new ArrayList<String>();
 	@Expose
 	private List<String> MutedIn = new ArrayList<String>();
 	@Expose
 	private List<String> Ignoring = new ArrayList<String>();
 	
 	public UserDetails() {
 		this.JoinedChannels.add( CyniChat.def_chan );
 	}
 	
 	/**
 	 * Adds an ignored player to the list
 	 * @param Ignorer : The player we're going to ignore 
 	 * @return true when complete or false with insufficient perms.
 	 */
 	public boolean addIgnore( String Ignorer ) {
 		if ( PermissionManager.checkPerm( player, "cynichat.basic.ignore") ) {
 			
 			if ( DataManager.getDetails(Ignorer.toLowerCase()).canIgnore() ) {
 				if ( !Ignoring.contains( Ignorer.toLowerCase() ) ) {
 					Ignoring.add( Ignorer.toLowerCase() );
 					player.sendMessage("You have muted the player");
 					return true;
 				} else {
 					player.sendMessage("You are already ignoring this player");
 					return true;
 				}
 			}
 			player.sendMessage("You cannot ignore this player.");
 			return true;
 		}
 		player.sendMessage("You lack the permissions necessary to ignore a person. Poor you...");
 		return false;
 	}
 	
 	/**
 	 * Remove a player which is being ignored
 	 * @param Ignorer : this is the player we're going to try and listen to again
 	 * @return true when complete or false with insufficient perms
 	 */
 	public boolean remIgnore( String Ignorer ) {
 		if ( PermissionManager.checkPerm( player, "cynichat.basic.ignore") ) {
 			if ( Ignoring.contains( Ignorer.toLowerCase() ) ) {
 				Ignoring.remove( Ignorer.toLowerCase() );
 				player.sendMessage("You have unmuted the player");
 				return true;
 			} else {
 				player.sendMessage("You are not ignoring this player");
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Toggle the afk status
 	 * @return true when complete
 	 */
 	public boolean changeAfk() {
 		if ( afk == true ) {
 			this.afk = false;
 		} else {
 			this.afk = true;
 		} return true;
 	}
 	
 	/**
 	 * Change the latest sender of a msg
 	 * @param newPerson : This is the person who can now be /r'd
 	 * @return true when complete
 	 */
 	public boolean changeLatest( String newPerson ) {
 		this.LastMessage = newPerson;
 		return true;
 	}
 	
 	/**
 	 * Send a new /msg and alter the internals accordingly
 	 * @param receiver : This is the person receiving the message
 	 * @return true when complete
 	 */
 	public boolean newMsg( UserDetails receiver ) {
 		this.LastMessage = receiver.getName().toLowerCase();
 		receiver.changeLatest(this.getName());
 		return true;
 	}
 	
 	/**
 	 * Send a new /r and alter the other players Latest, just in case
 	 * @param receiver : The person to receive the message
 	 * @return true when complete
 	 */
 	public boolean newR( Player receiver ) {
 		if ( this.LastMessage != null ) {
 			LastMessage = receiver.getName().toLowerCase();
 			return true;
 		}
 		return true;
 	}
 	
 	/**
 	 * Add a muted channel to a player
 	 * @param muter : This is the person trying to mute the player
 	 * @param channel : This is the channel the player is muted in
 	 * @return true when complete or false if no permission
 	 */
 	public boolean addMute( CommandSender muter, Channel channel ) {
 		if ( PermissionManager.checkPerm( (Player) muter, "cynichat.mod.mute."+channel.getName()) ) {
 			if ( !MutedIn.contains( channel.getName() ) ) {
 				MutedIn.add( channel.getName() );
 				muter.sendMessage("Player has been muted");
 				return true;
 			}
 			muter.sendMessage("Player was already muted");
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes a muted channel from a player
 	 * @param unmuter : this is the player that is unmuting someone
 	 * @param channel : This is the channel the player can now talk in again
 	 * @return true when complete or false with lack of perms
 	 */
 	public boolean remMute( CommandSender unmuter, Channel channel ) {
 		if ( PermissionManager.checkPerm( (Player) unmuter, "cynichat.mod.mute."+channel.getName()) ) {
 			if ( MutedIn.contains( channel.getName() ) ) {
 				MutedIn.remove( channel.getName() );
 				unmuter.sendMessage("Player has been unmuted");
 				return true;
 			}
 			unmuter.sendMessage("Player was already unmuted");
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Add a new ban to the player as long as the banner has the correct permission
 	 * @param banner : This is the person trying to enforce the ban
 	 * @param channel : This is the channel the player is being banned from
 	 * @return true when completed or false with insufficient permissions
 	 */
 	public boolean newBan( CommandSender banner, Channel channel ) {
 		if ( PermissionManager.checkPerm( (Player) banner, "cynichat.mod.ban."+channel.getName().toLowerCase() ) ) {
 			if ( !BannedFrom.contains( channel.getName() ) ) {
 				if ( JoinedChannels.contains( channel.getName() ) ) {
 					JoinedChannels.remove( channel.getName() );
 					if ( CurrentChannel.equals(channel.getName()) ) {
 						CurrentChannel = JoinedChannels.get(0);
 					}
 				}
 				BannedFrom.add( channel.getName() );
 				banner.sendMessage("Player has been banned.");
 				return true;
 			}
 			banner.sendMessage("Player is already banned.");
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Remove a ban from a player
 	 * @param unbanner : This is the player trying to unban someone
 	 * @param channel : This is the channel that they're being unbanned in
 	 * @return true when complete or false when permissions are not granted
 	 */
 	public boolean remBan( CommandSender unbanner, Channel channel ) {
 		if ( PermissionManager.checkPerm( (Player) unbanner, "cynichat.mod.ban."+channel.getName().toLowerCase() ) ) {
 			if ( BannedFrom.contains(channel.getName().toLowerCase() ) ) {
 				BannedFrom.remove( channel.getName().toLowerCase() );
 				unbanner.sendMessage("The player has been unbanned.");
 			} else {
 				unbanner.sendMessage("This player was not banned.");
 			}
 			return true;
 		} else {
 			unbanner.sendMessage("You do not have the sufficient perms for this action.");
 			return true;
 		}
 	}
 	
 	/**
 	 * This kicks the player from the channel
 	 * @param kicker : The kicker of the player
 	 * @param channel : The channel the player is being kicked from
 	 * @return true when complete, false if the person doesn't have perms
 	 */
 	public boolean Kick( CommandSender kicker, Channel channel ) {
 		if ( PermissionManager.checkPerm( (Player) kicker, "cynichat.mod.kick."+channel.getName().toLowerCase() ) ) {
 			if ( JoinedChannels.contains( channel.getName().toLowerCase() ) ) {
 				JoinedChannels.remove( channel.getName().toLowerCase() );
 				if ( CurrentChannel.equals(channel.getName().toLowerCase() ) ) {
 					this.CurrentChannel = JoinedChannels.get(0);
 				}
 				kicker.sendMessage("Player has been kicked");
 				return true;
 			}
 			kicker.sendMessage("Player was not in the channel");
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Globally silences a player
 	 * @param silencer : this is the person silencing the player
 	 * @return true when complete, false with insufficient perms
 	 */
 	public boolean Silence( CommandSender silencer ) {
 		if ( PermissionManager.checkPerm( (Player) silencer, "cynichat.mod.silence") ) {
 			if ( this.Silenced == false ) {
 				this.Silenced = true;
 				silencer.sendMessage("Player has been muted");
 				return true;
 			}
 			silencer.sendMessage("Player is already muted");
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Globally unmutes a player
 	 * @param listener : This is the person who unmuted the player
 	 * @return true when complete, false with insufficient perms
 	 */
 	public boolean Listen( CommandSender listener ) {
 		if ( PermissionManager.checkPerm( (Player) listener, "cynichat.mod.listener") ) {
 			if ( this.Silenced == false ) {
 				this.Silenced = false;
 				listener.sendMessage("Player has been unmuted");
 				return true;
 			}
 			listener.sendMessage("Player is already unmuted");
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * bind a player object to the details (used when a player joins)
 	 * @param player
 	 * @return
 	 */
 	public void bindPlayer( Player player ) {
 		this.player = player;
 	}
 	
 	/**
 	 * Unbinds the player;
 	 */
 	public void unbindPlayer(){
 		this.player = null;
 	}
 		
 	/**
 	 * Iterate through all the values we store in here and return them as debug
 	 * @return ALL THE DEBUG
 	 */
 	public boolean printAll() {
 		//CyniChat.printDebug("Name: "+ this.getName() );
 		CyniChat.printDebug("Silenced: "+ this.getSilenced().toString() );
 		CyniChat.printDebug("Can Ignore: "+ this.canIgnore().toString() );
 		CyniChat.printDebug("Current Channel: "+this.getCurrentChannel() );
 		CyniChat.printDebug("All Channels: "+ this.getAllVerboseChannels() );
 		CyniChat.printDebug("Banned Channels: "+ this.getBannedVerboseChannels() );
 		CyniChat.printDebug("Muted Channels: "+ this.getMutedVerboseChannels() );
 		CyniChat.printDebug("Muted Players: "+ this.getVerboseIgnoring() );
 		return true;
 	}
 	
 	/**
 	 * When a player joins a channel, this is being used
 	 * @param newChan : This is the new channel that we're joining
 	 * @param pass : This is either "" or the password sent by the user, to be compared to the channel object.
 	 * @return false if the channel is protected and the user doesn't have the perm... actually, just read the code, it's simple enough.
 	 */
 	public boolean joinChannel( Channel newChan, String pass ) {
 		if ( newChan.isProtected() ) {
 			if ( PermissionManager.checkPerm( player, "cynichat.basic.join.all") || 
 					( PermissionManager.checkPerm( player, "cynichat.basic.join."+newChan.getName().toLowerCase() ) 
 							&& newChan.equalsPass( pass ) ) ) {
 				if ( !this.JoinedChannels.contains( newChan.getName() ) ) {
 					this.JoinedChannels.add( newChan.getName() );
 				}
 				this.CurrentChannel = newChan.getName();
 				player.sendMessage("You are now talking in "+ newChan.getName() );
 				return true;
 			}
 			return false;
 		}
 		if ( newChan.equalsPass( pass ) ) {
 			if ( !this.JoinedChannels.contains( newChan.getName() ) ) {
 				this.JoinedChannels.add( newChan.getName() );
 			}
 			this.CurrentChannel = newChan.getName();
 			player.sendMessage("You are now talking in "+ newChan.getName() );
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * When a player leaves a channel, strike the channel from all the lists
 	 * @param chan : This is the channel they're leaving
 	 * @return true for when all the actions have been carried out.
 	 */
 	public boolean leaveChannel( String chan ) {
 		if ( this.JoinedChannels.contains( chan )) {
 			if ( PermissionManager.checkPerm( player, "cynichat.basic.leave."+chan) ) {
 				this.JoinedChannels.remove( chan );
 				player.sendMessage("You have left "+ chan);
 				if ( this.CurrentChannel.equals(chan) ) {
 					this.CurrentChannel = JoinedChannels.get(0);
 					player.sendMessage("You are now in " + JoinedChannels.get(0));
 				}
 				return true;
 			} else {
 				player.sendMessage("You cannot leave "+ chan);
 				return true;
 			}
 		} else {
 			player.sendMessage("You are not in "+ chan);
 			return true;
 		}
 	}
 	
 	/**
 	 * Return the current channel the player is in
 	 * @return CurrentChannel
 	 */
 	public String getCurrentChannel() {
 		return CurrentChannel;
 	}
 	
 	/**
 	 * Return the name of the player
 	 * @return player name
 	 */
 	public String getName() {
 		return player.getName().toLowerCase();
 	}
 	
 	/**
 	 * Return the player object
 	 * @return player
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 	
 	/**
 	 * Return whether they are silenced or not
 	 * @return Silenced
 	 */
 	public Boolean getSilenced() {
 		return Silenced;
 	}
 	
 	/**
 	 * Return whether the player is afk or not
 	 * @return afk
 	 */
 	public Boolean getAfk() {
 		return afk;
 	}
 	
 	/**
 	 * Return all the channels that the player is in
 	 * @return JoinedChannels
 	 */
 	public List<String> getAllChannels() {
 		return JoinedChannels;
 	}
 	
 	/**
 	 * Turn the JoinedChannels array into a string
 	 * @return JoinedChannels Verbose
 	 */
 	public String getAllVerboseChannels() {
 		String AllChan = "";
 		String c = "";
 		if ( JoinedChannels.isEmpty() ) {
 			return "-";
 		}
 		for ( int i=0; i<JoinedChannels.size(); i++ ) {
 			AllChan += c + JoinedChannels.get(i);
 			c = ", ";
 		}
 		return AllChan;
 	}
 	
 	/**
 	 * Return all the channels a player is banned from
 	 * @return BannedFrom
 	 */
 	public List<String> getBannedChannels() {
 		return BannedFrom;
 	}
 	
 	/**
 	 * Return the string of all the channels that a player is banned from
 	 * @return BannedFrom Verbose
 	 */
 	public String getBannedVerboseChannels() {
 		String BanChan = "";
 		String c = "";
 		if ( BannedFrom.isEmpty() ) {
 			return "-";
 		}
 		for ( int i=0; i<BannedFrom.size(); i++ ) {
 			BanChan += c + BannedFrom.get(i);
 			c = ", ";
 		}
 		return BanChan;
 	}
 	
 	/**
 	 * Return the channels that a player is muted in
 	 * @return MutedIn
 	 */
 	public List<String> getMutedChannels() {
 		return MutedIn;
 	}
 	
 	/**
 	 * Return the strings of the MutedIn array
 	 * @return MutedIn Verbose
 	 */
 	public String getMutedVerboseChannels() {
 		String MuteChan = "";
 		String c = "";
 		if ( MutedIn.isEmpty() ) {
 			return "-";
 		}
 		for ( int i=0; i<MutedIn.size(); i++ ) {
 			MuteChan += c + MutedIn.get(i);
 			c = ", ";
 		}
 		return MuteChan;
 	}
 	
 	public Boolean canIgnore() {
 		return CanIgnore;
 	}
 	
 	/**
 	 * Return the players that are being ignored
 	 * @return Ignoring
 	 */
 	public List<String> getIgnoring() {
 		return Ignoring;
 	}
 	
 	/**
 	 * Return the strings of the Ignoring array
 	 * @return Ignoring Verbose
 	 */
 	public String getVerboseIgnoring() {
 		String ignore = "";
 		String c = "";
 		if ( Ignoring.isEmpty() ) {
 			return "-";
 		}
 		for ( int i=0; i<Ignoring.size(); i++ ) {
 			ignore += c + Ignoring.get(i);
 			c = ", ";
 		}
 		return ignore;
 	}
 
 	public String getLatest() {
 		return this.LastMessage;
 	}
<<<<<<< HEAD
=======
<<<<<<< Updated upstream
=======
>>>>>>> localfix2
 
 	public int getID() {
 		return this.ID;
 	}
 
<<<<<<< HEAD
=======
 	public boolean setId( int newId ) {
 		this.ID = newId;
 		return true;
 	}
 
>>>>>>> localfix2
 	public Boolean loadData( int id, String active, Boolean silence, Boolean canIgnore, List<String> Joined, List<String> Muted, List<String> Banned, List<String> Ignoring ) {
 		this.ID = id;
 		this.player = null;
 		this.CurrentChannel = active;
 		this.Silenced = silence;
 		this.CanIgnore = canIgnore;
 		this.JoinedChannels = Joined;
 		this.MutedIn = Muted;
 		this.BannedFrom = Banned;
 		this.Ignoring = Ignoring;
 		return true;
 	}
<<<<<<< HEAD
=======
>>>>>>> Stashed changes
>>>>>>> localfix2
 }
