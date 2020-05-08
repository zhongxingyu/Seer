 package com.niccholaspage.nICs.ics;
 
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockRedstoneEvent;
 
 import com.niccholaspage.nICs.IC;
 import com.niccholaspage.nICs.nICs;
 
 public class N1002 implements IC {
 
 	public Boolean run(final nICs plugin, BlockRedstoneEvent event){
 		if (event.getNewCurrent() < 1) return null;
 		final Sign sign = (Sign)event.getBlock().getState();
 		final int times = Integer.parseInt(sign.getLine(2));
 		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
 			public void run(){
 				for (int i = 0; i < times; i++){
 					plugin.setLever(sign, true);
 					try {
						Thread.sleep(500);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					plugin.setLever(sign, false);
 					
 				}
 			}
 		});
 		return null;
 	}
 	
 	public String getName(){
 		return "POWER REPEATER";
 	}
 	
 	public String canPlace(String[] lines){
 		if (!nICs.isInt(lines[2])){
 			return "Line 3 must be a integer.";
 		}else {
 			return null;
 		}
 	}
 
 }
