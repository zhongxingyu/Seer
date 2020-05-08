 package team.GunsPlus;
 
 import org.getspout.spoutapi.event.input.KeyBindingEvent;
 import org.getspout.spoutapi.gui.ScreenType;
 import org.getspout.spoutapi.keyboard.BindingExecutionDelegate;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import team.GunsPlus.Enum.KeyType;
 import team.GunsPlus.Util.GunUtils;
 import team.GunsPlus.Util.PlayerUtils;
 
 public class ZoomBinding implements BindingExecutionDelegate{
 
	@SuppressWarnings("unused")
 	private GunsPlus plugin;
	@SuppressWarnings("unused")
 	private KeyType keytype;
 	
 	public ZoomBinding(GunsPlus p, KeyType kt){
 		plugin = p;
 		keytype = kt;
 		SpoutManager.getKeyBindingManager().registerBinding("Zoom", kt.getKey(), "If pressed guns will zoom.", this, plugin);
 	}
 	
 	@Override
 	public void keyPressed(KeyBindingEvent ev) {
 		if(ev.getScreenType().equals(ScreenType.GAME_SCREEN)){
 			SpoutPlayer sp = ev.getPlayer();
 			if(GunUtils.holdsGun(sp)){
 				PlayerUtils.getPlayerBySpoutPlayer(sp).zoom(GunUtils.getGunInHand(sp));
 			}
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyBindingEvent ev) {
 		if(ev.getScreenType().equals(ScreenType.GAME_SCREEN)&&keytype.isHoldKey()){
 			SpoutPlayer sp = ev.getPlayer();
 			if(GunUtils.holdsGun(sp)){
 				PlayerUtils.getPlayerBySpoutPlayer(sp).zoom(GunUtils.getGunInHand(sp));
 			}
 		}
 	}
 }
