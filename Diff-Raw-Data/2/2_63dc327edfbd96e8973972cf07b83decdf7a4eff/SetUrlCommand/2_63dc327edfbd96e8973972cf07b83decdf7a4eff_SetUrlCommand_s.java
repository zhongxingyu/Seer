 /*-------------------------------------------------------------------------
 svninfo: $Id: PkillCommand.java 994 2005-10-23 10:19:20Z maartenl $
 Maarten's Mud, WWW-based MUD using MYSQL
 Copyright (C) 1998  Maarten van Leunen
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 Maarten van Leunen
 Appelhof 27
 5345 KA Oss
 Nederland
 Europe
 maarten_l@yahoo.com
 -------------------------------------------------------------------------*/
 package mmud.commands.guilds;
 
 import java.util.logging.Logger;
 
 import mmud.MudException;
 import mmud.Attribute;
 import mmud.characters.User;
 import mmud.characters.Person;
 import mmud.characters.Persons;
 import mmud.characters.GuildFactory;
 import mmud.characters.Guild;
 import mmud.database.Database;
 import mmud.commands.NormalCommand;
 import mmud.commands.Command;
 
 /**
  * Makes you, as guildmaster, set the description of the guild.
  * Command syntax something like : <TT>guilddescription &lt;description&gt;</TT>
  */
 public class SetUrlCommand extends GuildMasterCommand
 {
 
 	public SetUrlCommand(String aRegExpr)
 	{
 		super(aRegExpr);
 	}
 
 	public boolean run(User aUser)
 	throws MudException
 	{
 		Logger.getLogger("mmud").finer("");
 		if (!super.run(aUser))
 		{
 			return false;
 		}
 		String[] myParsed = getParsedCommand();
 		aUser.getGuild().setGuildUrl(
 			getCommand().substring(myParsed[0].length() + 1)
 			);
 		Database.writeLog(aUser.getName(), " set guildtitle of " + 
 			" guild " + aUser.getGuild().getName());
		aUser.writeMessage("You have set a new title for your guild.<BR>\r\n");
 		return true;
 	}
 
 	public Command createCommand()
 	{
 		return new SetUrlCommand(getRegExpr());
 	}
 	
 }
