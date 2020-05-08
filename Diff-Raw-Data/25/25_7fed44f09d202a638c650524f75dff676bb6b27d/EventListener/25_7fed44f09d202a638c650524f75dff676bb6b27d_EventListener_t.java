 package net.mcforge.verification;
 
 import net.mcforge.API.EventHandler;
 import net.mcforge.API.Listener;
 import net.mcforge.API.player.PlayerBlockChangeEvent;
 import net.mcforge.API.player.PlayerCommandEvent;
 import net.mcforge.API.player.PlayerConnectEvent;
 import net.mcforge.API.player.PlayerDisconnectEvent;
 import net.mcforge.iomodel.Player;
 
 public class EventListener implements Listener {
 
 	@EventHandler
 	public void onPlayerCommand(PlayerCommandEvent e) {
 		Player who = e.getPlayer();
 		if (who.getGroup().permissionlevel < AdminVerification.identificationPerm) { return; }
 		if (Verifier.isVerified(who) || !Verifier.verifies(who)) { return; }
 
 		if (!e.getCommand().equalsIgnoreCase("pass") && !e.getCommand().equalsIgnoreCase("setpass") && !e.getCommand().equalsIgnoreCase("help")) {
 			who.sendMessage("&cPlease verify yourself by using &a/pass <password> &cfirst!");
 			e.setCancel(true);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerBlockChange(PlayerBlockChangeEvent e) {
 		Player who = e.getPlayer();
 		if (who.getGroup().permissionlevel < AdminVerification.identificationPerm) { return; }
 		if (Verifier.isVerified(who) || !Verifier.verifies(who)) { return; }
 
 		who.sendMessage("&cPlease verify yourself by using &a/pass <password> &cfirst!");
 		e.setCancel(true);
 	}
 
 	@EventHandler
 	public void onPlayerConnect(PlayerConnectEvent e) {
 		Player who = e.getPlayer();
 		if (who.getGroup().permissionlevel < AdminVerification.identificationPerm) { return; }
		if (AdminVerification.identificationToggle) {
			if (!who.hasAttribute("mcf_verifies")) {
				Verifier.setVerifies(who, true);
			}
		} 
 		
 		Verifier.setVerified(who, false);
 		Verifier.setTries(who, 0);
 		if (!Verifier.verifies(who)) { return; }
 		
 		if (Verifier.getPassword(who) == null) {
 			who.sendMessage("&ePlease set your password using &b/setpass <password>");
 		}
 		else {
 			who.sendMessage("&cPlease verify yourself by using &a/pass <password>");
 		}
 	}
 
 	@EventHandler
 	public void onPlayerDisconnect(PlayerDisconnectEvent e) {
 		Player who = e.getPlayer();
 		if (who.getGroup().permissionlevel < AdminVerification.identificationPerm) { return; }
 
 		Verifier.setVerified(who, false);
 		Verifier.setTries(who, 0);
 	}
 }
