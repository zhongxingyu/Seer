 /*
  * This file is part of MineQuest, The ultimate MMORPG plugin!.
  * MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MineQuest;
 
 import java.io.File;
 
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Utils.PropertiesFile;
 
 public enum I18NMessage {
 	
 	Cmd_INVALIDARGS("Invalid arguments. See the help menu for arguments."),
 	Cmd_NOPARTY("Not in party!"),
 	Cmd_NOPERMISSION("You do not have sufficient permissions."),
 	Cmd_NOSUCHPLAYER("No such player!"),
 	Cmd_NOSUCHQUEST("No such quest!"),
 	Cmd_NOTLEADER("Not leader!"),
 	Cmd_Party_ACCEPT("Joined the party!"),
 	Cmd_Party_CREATE("Created and joined a new party!"),
 	Cmd_Party_DISCARD("Discarding party invite..."),
 	Cmd_Party_HELP("Party Help"),
 	Cmd_Party_HELPCREATE("Create a new party."),
 	Cmd_Party_HELPINVITE("Accept pending invite."),
 	Cmd_Party_HELPINVITETARGET("Invite a player to the party."),
 	Cmd_Party_HELPKICK("Kick a player."),
 	Cmd_Party_HELPLEAVE("Leave the party."),
 	Cmd_Party_HELPLIST("List members and statistics."),
 	Cmd_Party_HELPPROMOTE("Give leader position to someone else."),
 	Cmd_Party_INPARTY("Already in party!"),
 	Cmd_Party_KICK("Kicked player: "),
 	Cmd_Party_KICKTARGET("Kicked off the party!"),
 	Cmd_Party_LEAVE("Departed the party!"),
 	Cmd_Party_LIST("Party List"),
 	Cmd_Party_NOINVITE("No pending invites..."),
 	Cmd_Party_PROMOTE("Promoted player to leader! You are no longer leader!"),
 	Cmd_Party_PROMOTETARGET("You are now leader!"),
 	Cmd_Party_TARGETINPARTY("Target is already in a party!"),
 	Cmd_Party_TARGETINVITESENT("Invite sent to target!"),
 	Cmd_Party_TARGETNOPARTY("Target not in {any|your} party!"),
 	Cmd_Party_TARGETPENDING("Target has a pending invite!"),
 	Cmd_Quest_ACCEPTED("Accepted (Pending) Quests"),
 	Cmd_Quest_ALREADYACTIVE("Already on an active quest!"),
 	Cmd_Quest_ALREADYDONE("The quest has already finished!"),
 	Cmd_Quest_AVAILABLE("Available Quests"),
 	Cmd_Quest_DROP("The Quest was dropped!"),
 	Cmd_Quest_EXITUNFINISHED("Quest unfinished - to stop now, use abandon."),
 	Cmd_Quest_HELP("Quest Help"),
 	Cmd_Quest_HELPABANDON("Abandon active quest."),
 	Cmd_Quest_HELPACCEPT("Accept a Quest."),
 	Cmd_Quest_HELPACTIVE("View Active Quest."),
 	Cmd_Quest_HELPAVAILABLE("View Available Quests."),
 	Cmd_Quest_HELPDROP("Drop a given quest."),
 	Cmd_Quest_HELPENTER("Enter active quest."),
 	Cmd_Quest_HELPEXIT("Exit active quest."),
 	Cmd_Quest_HELPGIVEN("View Pending Quests."),
 	Cmd_Quest_HELPINFO("View information about a Quest."),
 	Cmd_Quest_HELPMAIN("View Main World Quests."),
 	Cmd_Quest_HELPSTART("Initiate an active quest."),
 	Cmd_Quest_HELPSTARTNOPARTY("Create a party and initiate an active quest."),
 	Cmd_Quest_INQUEST("Already inside the quest!"),
 	Cmd_Quest_JOINPARTY("Join a party to see all available options!"),
 	Cmd_Quest_MAINWORLD("This is a main world quest!"),
 	Cmd_Quest_MWACTIVE("Main World Quests"),
 	Cmd_Quest_NOACTIVE("No active quest!"),
 	Cmd_Quest_NOTHAVEQUEST("You don't have this quest!"),
 	Cmd_Quest_NOTINQUEST("Not inside the quest!"),
 	Cmd_Quest_RELOAD("Toggled a reload. Check the server log for more details."),
 	Cmd_Quest_UNAVAILABLE("The quest seems to be unavailable at this time."),
 	Cmd_SQLEXCEPTION("An internal server error occured; blame the admins..."),
 	Quest_ABORT("Quest aborted!"),
 	Quest_ACCEPT("Quest accepted!"),
 	Quest_COMPLETE("You've successfully completed the quest!"),
 	Quest_NODESC("No description available..."),
	Quest_NOEDIT("Nice try, but you can't edit this part of the world."),
	Top_Cat_MEOW("Meow!");
 	
 	private static final String LOCATION = Managers.getActivePlugin().getDataFolder().getAbsolutePath() + File.separator + "locales";
 	
 	static {
 		File f = new File(LOCATION);
 		if (!f.exists() || !f.isDirectory()) {
 			if (f.exists())
 				f.delete();
 			f.mkdirs();
 		}
 	}
 	
 	private static String getLocale() {
 		return MineQuest.configuration.mainConfig.getString("locale", "en_US");
 	}
 	
 	private final String description;
 	
 	private I18NMessage(String d) {
 		description = d;
 	}
 	
 	public String getValue() {
 		PropertiesFile localefile = new PropertiesFile(LOCATION + File.separator + getLocale() + ".dict");
 		return localefile.getChatString(name(), description);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Enum#toString()
 	 */
 	@Override
 	public String toString() {
 		return getValue();
 	}
 	
 }
