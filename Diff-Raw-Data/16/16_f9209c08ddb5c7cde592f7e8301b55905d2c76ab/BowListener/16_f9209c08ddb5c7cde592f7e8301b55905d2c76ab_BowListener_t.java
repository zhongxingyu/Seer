 package shadowlauch.advancedbow.main;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 
 public class BowListener extends PlayerListener{
     public static AdvancedBow plugin;
     protected static List<Object> disabled=new ArrayList<Object>();
     protected Timer timer= new Timer();
 
     public BowListener(AdvancedBow instance) {
         plugin = instance;
     }
 
     public void onPlayerInteract(PlayerInteractEvent e) {
    	int type;
    	if(e.getItem()!=null){
    		type=e.getItem().getTypeId();
    	}
    	else return;
     	String act=e.getAction().toString();
     	Boolean arrows=e.getPlayer().getInventory().contains(262);
     	if(type==261 && arrows && (act=="RIGHT_CLICK_BLOCK" || act=="RIGHT_CLICK_AIR")){
     		if(disabled.contains(e.getPlayer())){
     			e.getPlayer().sendMessage(ChatColor.RED + "" + plugin.config.temp_cd.replaceAll("\\+sec", getLeftTime(e)));
     			e.setCancelled(true);
     			e.getPlayer().updateInventory();
     		}
     		else{
     			if(!plugin.hasPerm(e.getPlayer(),"advancedbow.ignorecooldown")){
 	    			disabled.add(e.getPlayer());
 	    			disabled.add(System.currentTimeMillis()+plugin.config.cdm);
 	    			timer.schedule(new BowCooldownTimerTask(e.getPlayer()), plugin.config.cdm);
     			}
     		}
     	}
     }
     
     private String getLeftTime(PlayerInteractEvent e){
     	long ret=(Long) disabled.get(disabled.indexOf(e.getPlayer())+1);
     	double erg=(ret-System.currentTimeMillis());
     	DecimalFormat df = new DecimalFormat( "0.00" );
     	return df.format( erg/1000 );
     }	
 
 }
