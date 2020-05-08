 package net.immortalcraft.races.cmd;
 
 import com.massivecraft.mcore.cmd.HelpCommand;
 import com.massivecraft.mcore.cmd.VersionCommand;
 import net.immortalcraft.races.ConfServer;
 import net.immortalcraft.races.Races;
 import net.immortalcraft.races.RPerm;
 
 	public class CmdBase extends RCommand
 	{
 		public CmdRacesBe cmdRacesBe = new CmdRacesBe();
 		public CmdRacesInfo cmdRacesInfo = new CmdRacesInfo();
	    	public VersionCommand cmdVersion = new VersionCommand(Races.get(), RPerm.VERSION.node, "v", "version");
 
 		public CmdBase()
 		{
 			this.addAliases(ConfServer.baseCommandAliases);
 
 			this.addSubCommand(HelpCommand.get());
 			this.addSubCommand(cmdRacesBe);
 			this.addSubCommand(cmdRacesInfo);
 			this.addSubCommand(cmdVersion);
 
 			this.setDesc("The races base command");
 		}
 
 		@Override
 		public void perform()
 		{
 			this.getCommandChain().add(this);
 			HelpCommand.getInstance().execute(this.sender, this.args, this.commandChain);
 		}
 	}
