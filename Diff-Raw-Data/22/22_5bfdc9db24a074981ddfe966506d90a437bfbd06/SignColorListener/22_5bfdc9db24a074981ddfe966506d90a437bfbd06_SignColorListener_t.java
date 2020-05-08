 package com.github.Sabersamus.Basic.Listeners;
 
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 
 import com.github.Sabersamus.Basic.Basic;
 
 public class SignColorListener implements Listener
 {
 	public static Basic plugin;
 	public SignColorListener(Basic instance){
 		plugin = instance;
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	public void onSignChange(SignChangeEvent event){
 	BlockState state = event.getBlock().getState();
 	if(state instanceof Sign) {
 	Sign sign = (Sign) state;
 	String line0 = event.getLine(0);
 	String nline0 = line0.replaceAll("(&([a-fk0-9]))", "\u00A7$2");
 	String line1 = event.getLine(1);
 	String nline1 = line1.replaceAll("(&([a-fk0-9]))", "\u00A7$2");
 	String line2 = event.getLine(2);
 	String nline2 = line2.replaceAll("(&([a-fk0-9]))", "\u00A7$2");
 	String line3 = event.getLine(3);
 	String nline3 = line3.replaceAll("(&([a-fk0-9]))", "\u00A7$2");
 	event.setLine(0, nline0);
 	event.setLine(1, nline1);
 	event.setLine(2, nline2);
 	event.setLine(3, nline3);
 	sign.update(true);
 		}
 	}
 }
