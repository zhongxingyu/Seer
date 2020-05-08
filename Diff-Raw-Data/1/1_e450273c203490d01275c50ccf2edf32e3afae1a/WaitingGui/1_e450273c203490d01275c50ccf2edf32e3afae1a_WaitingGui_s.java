 package org.dyndns.pamelloes.SpoutCasino.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.dyndns.pamelloes.SpoutCasino.SpoutCasino;
 import org.getspout.spoutapi.event.screen.ScreenCloseEvent;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.Container;
 import org.getspout.spoutapi.gui.ContainerType;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class WaitingGui extends TableGui {
 	public static final List<SpoutPlayer> waiting = new ArrayList<SpoutPlayer>();
 	
 	public WaitingGui(SpoutPlayer player, final Runnable onCancelled) {
 		super(player);
 		
 		waiting.add(player);
 		
 		SpoutCasino.casino.getServer().getPluginManager().registerEvents(new Listener() {
 			@SuppressWarnings("unused")
 			@EventHandler
 			public void onScreenClose(ScreenCloseEvent e) {
 				if(!e.getScreen().equals(WaitingGui.this)) return;
 				if(waiting.contains(e.getPlayer())) {
 					onCancelled.run();
 					waiting.remove(e.getPlayer());
 				}
 				HandlerList.unregisterAll(this);
 			}
 		}, SpoutCasino.casino);
 		
 		makeGui();
 	}
 
 	protected void makeGui() {
 		setSize(200,100);
 
 		Container master = new GenericContainer().setLayout(ContainerType.VERTICAL);
 		GenericLabel label = new GenericLabel("The table is full at the moment.");
 		label.setPriority(RenderPriority.Lowest);
 		label.setTextColor(new Color(1.0f,0.3f,0.3f));
 		label.setMargin(10,20);
 		master.addChild(label);
 		
 		label = new GenericLabel("You are currently in line for the");
 		label.setPriority(RenderPriority.Lowest);
 		label.setMargin(10,15,0,15);
 		master.addChild(label);
 		label = new GenericLabel("next open slot.");
 		label.setPriority(RenderPriority.Lowest);
 		label.setMargin(0,15,10,15);
 		master.addChild(label);
 		
 		label = new GenericLabel("Press Escape to stop waiting.");
 		label.setPriority(RenderPriority.Lowest);
 		label.setMargin(10,15);
 		master.addChild(label);
 		container.addChild(master);
 	}
 
 }
