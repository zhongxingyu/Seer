 package team.GunsPlus;
 
 import org.getspout.spoutapi.event.input.KeyBindingEvent;
 import org.getspout.spoutapi.gui.ScreenType;
 import org.getspout.spoutapi.keyboard.BindingExecutionDelegate;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import team.GunsPlus.Enum.KeyType;
 import team.GunsPlus.Util.GunUtils;
 import team.GunsPlus.Util.PlayerUtils;
 
 public class ReloadBinding implements BindingExecutionDelegate{
 
	@SuppressWarnings("unused")
 	private GunsPlus plugin;
 	
 	public ReloadBinding(GunsPlus p, KeyType kt){
 		plugin = p;
 		SpoutManager.getKeyBindingManager().registerBinding("Reload", kt.getKey(), "If pressed guns will reload.", this, plugin);
 	}
 	
 	@Override
 	public void keyPressed(KeyBindingEvent ev) {
 		if(ev.getScreenType().equals(ScreenType.GAME_SCREEN)){
 			SpoutPlayer sp = ev.getPlayer();
 			if(GunUtils.holdsGun(sp)){
 				PlayerUtils.getPlayerBySpoutPlayer(sp).reload(GunUtils.getGunInHand(sp));
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyBindingEvent ev) {
 		//no effect
 	}
 	
 }
