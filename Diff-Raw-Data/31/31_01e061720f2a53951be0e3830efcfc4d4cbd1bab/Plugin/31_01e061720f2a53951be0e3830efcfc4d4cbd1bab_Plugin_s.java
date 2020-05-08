 package no.runsafe.UserControl;
 
 import no.runsafe.UserControl.command.*;
 import no.runsafe.UserControl.database.PlayerDatabase;
 import no.runsafe.UserControl.database.PlayerKickLog;
 import no.runsafe.UserControl.database.PlayerSessionLog;
 import no.runsafe.UserControl.events.Login;
 import no.runsafe.framework.RunsafeConfigurablePlugin;
 
 public class Plugin extends RunsafeConfigurablePlugin
 {
 	@Override
 	protected void PluginSetup()
 	{
 		addComponent(PlayerHandler.class);
 		addComponent(PlayerDatabase.class);
 		addComponent(PlayerSessionLog.class);
 		addComponent(PlayerKickLog.class);
 		addComponent(SessionLogger.class);
 		addComponent(Ban.class);
 		addComponent(BanIP.class);
 		addComponent(Kick.class);
 		addComponent(KickAll.class);
 		addComponent(SuDo.class);
 		addComponent(TempBan.class);
 		addComponent(UnBan.class);
 		addComponent(UnBanIP.class);
 		addComponent(BanEnforcer.class);
 		addComponent(Seen.class);
 		addComponent(Whois.class);
 		addComponent(Op.class);
 		addComponent(DeOp.class);
 		addComponent(OpController.class);
 		addComponent(LoginRedirectManager.class);
 		addComponent(SetRedirectLocation.class);
 		addComponent(RemoveRedirectLocation.class);
 
 		// Events
		addComponent(Kick.class);
 		addComponent(Login.class);
 	}
 }
