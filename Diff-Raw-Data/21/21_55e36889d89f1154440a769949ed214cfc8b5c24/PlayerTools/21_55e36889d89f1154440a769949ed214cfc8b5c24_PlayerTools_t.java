 /*
  * WorldTools
  * Copyright (C) 2012
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.File;
 import java.util.ArrayList;
 
 /**
  * @author Spenk & Glacksy
  * @category Player Listener
  * @version 1.0
  * 
  * @description
  * tools for players
  * and admins
  *
  */
 public class PlayerTools extends PluginListener{
 	
 	ArrayList<String> god = new ArrayList<String>();
 	ArrayList<String> frozen = new ArrayList<String>();
 	
 	private static Location exactSpawn = null;
 	
 	public boolean onCommand(Player Player, String[] split) {
 			   if (split[0].equalsIgnoreCase("/drain")){
 				   if ((Player.canUseCommand("/worldtools"))||Player.canUseCommand("/drain")){
 			       int dist = 0;
 			       if (split.length == 2) try { dist = Integer.parseInt(split[1]); } catch (Throwable localThrowable) {
 			         } if (dist == 0) {
 			         Player.sendMessage("9[6-8-6-8-6-9Drain Help6-8-6-8-6-9]");
 			         Player.sendMessage("a/drain <radius> - Drain water/lava");
 			         Player.sendMessage("a/drainwater <radius> - Drain water");
 			         Player.sendMessage("a/drainlava <radius>  - Drain lava");
 			         Player.sendMessage("a/ext <radius> - Remove fire"); return true;
 			       }
 			         int radius = 0;
 			       try {radius = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /drain <radius>");return true;}
 			       int xmin = (int)Player.getX()-radius;
 			         int xmax = (int)Player.getX()+radius;
 			         int ymin = (int)Player.getY()-radius;
 			         int ymax = (int)Player.getY()+radius;
 			         int zmin = (int)Player.getZ()-radius;
 			         int zmax = (int)Player.getZ()+radius;
 			        
 			         for (int x = xmin; x <= xmax; x++) {
 			                 for (int y = ymin; y <= ymax; y++) {
 			                         for (int z = zmin; z <= zmax; z++) {    
 			                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 8){Player.getWorld().setBlockAt(95, x, y, z);}
 			                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(9, x, y, z);}
 			                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 10){Player.getWorld().setBlockAt(95, x, y, z);}
 			                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(11, x, y, z);}
 			                         }
 			                       }
 			                     }
 			         Player.sendMessage("aLava & Water Successfully Drained!");
 			         return true;
 			                   }
 			   Player.notify("You cant use this command");return true;
 	}
 
 
 		     if (split[0].equalsIgnoreCase("/drainlava")){
		    	 if ((Player.canUseCommand("/worldtools")) || (Player.canUseCommand("/drainlava"))){
 		       int dist = 0;
 		       if (split.length == 2) try { dist = Integer.parseInt(split[1]); } catch (Throwable localThrowable1) {
 		         } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /drainlava <radius>"); return true; }
 		         int radius = 0;
 		         try {radius = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /drainlava <radius>");return true;}
 		         int xmin = (int)Player.getX()-radius;
 		         int xmax = (int)Player.getX()+radius;
 		         int ymin = (int)Player.getY()-radius;
 		         int ymax = (int)Player.getY()+radius;
 		         int zmin = (int)Player.getZ()-radius;
 		         int zmax = (int)Player.getZ()+radius;
 		        
 		         for (int x = xmin; x <= xmax; x++) {
 		                 for (int y = ymin; y <= ymax; y++) {
 		                         for (int z = zmin; z <= zmax; z++) {    
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 11){Player.getWorld().setBlockAt(95, x, y, z);}
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(10, x, y, z);}
 		                         }
 		                       }
 		                     }
 		       Player.sendMessage("aLava Successfully Removed!");
 		       return true;
 		     }
 		     Player.notify("You cant use this command");return true;
 	}
 
 		     if (split[0].equalsIgnoreCase("/drainwater")) {
		    	 if ((Player.canUseCommand("/worldtools")) || (Player.canUseCommand("/drainwater"))){
 		       int dist = 0;
 		       if (split.length == 2) try { dist = Integer.parseInt(split[1]); } catch (Throwable localThrowable2) {
 		         } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /drainwater <radius>"); return true; }
 		         int radius = 0;
 		         try {radius = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /drainwater <radius>");return true;}
 		         int xmin = (int)Player.getX()-radius;
 		         int xmax = (int)Player.getX()+radius;
 		         int ymin = (int)Player.getY()-radius;
 		         int ymax = (int)Player.getY()+radius;
 		         int zmin = (int)Player.getZ()-radius;
 		         int zmax = (int)Player.getZ()+radius;
 		        
 		         for (int x = xmin; x <= xmax; x++) {
 		                 for (int y = ymin; y <= ymax; y++) {
 		                         for (int z = zmin; z <= zmax; z++) {    
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 9){Player.getWorld().setBlockAt(95, x, y, z);}
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(8, x, y, z);}
 		                         }
 		                       }
 		                     }
 		       Player.sendMessage("aWater Successfully Removed!");
 		       return true;
 		     }
 		    	 Player.notify("You cant use this command");return true;
 		     }
 
 		     if (split[0].equalsIgnoreCase("/ext")) {
		    	 if ((Player.canUseCommand("/worldtools")) || (Player.canUseCommand("/ext"))){
 		       int dist = 0;
 		       if (split.length == 2) try { dist = Integer.parseInt(split[1]); } catch (Throwable localThrowable3) {
 		         } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /ext <radius>"); return true; }
 		         int radius = 0;
 		         try {radius = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /ext <radius>");return true;}
 		         int xmin = (int)Player.getX()-radius;
 		         int xmax = (int)Player.getX()+radius;
 		         int ymin = (int)Player.getY()-radius;
 		         int ymax = (int)Player.getY()+radius;
 		         int zmin = (int)Player.getZ()-radius;
 		         int zmax = (int)Player.getZ()+radius;
 		        
 		         for (int x = xmin; x <= xmax; x++) {
 		                 for (int y = ymin; y <= ymax; y++) {
 		                         for (int z = zmin; z <= zmax; z++) {    
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 51){Player.getWorld().setBlockAt(0, x, y, z);}
 		                         }
 		                       }
 		                     }
 		       Player.sendMessage("aFire Successfully Extinguished");
 		       return true;
 		     }
 		     Player.notify("You cant use this command");return true;
 	}
 
 		     if (split[0].equalsIgnoreCase("/melt")) {
 		    	 if ((Player.canUseCommand("/worldtools")) || (Player.canUseCommand("/melt"))){
 		       int dist = 0;
 		       if (split.length == 2) try { dist = Integer.parseInt(split[1]); } catch (Throwable localThrowable4) {
 		         } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /melt <radius>"); return true; }
 		         int radius = 0;
 		         try {radius = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /melt <radius>");return true;}
 		         int xmin = (int)Player.getX()-radius;
 		         int xmax = (int)Player.getX()+radius;
 		         int ymin = (int)Player.getY()-radius;
 		         int ymax = (int)Player.getY()+radius;
 		         int zmin = (int)Player.getZ()-radius;
 		         int zmax = (int)Player.getZ()+radius;
 		        
 		         for (int x = xmin; x <= xmax; x++) {
 		                 for (int y = ymin; y <= ymax; y++) {
 		                         for (int z = zmin; z <= zmax; z++) {    
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 78){Player.getWorld().setBlockAt(0, x, y, z);}
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 79){Player.getWorld().setBlockAt(8, x, y, z);}
 		                         }
 		                       }
 		                     }
 		       Player.sendMessage("aThe Snow & Ice has been successfully melted!");
 		       return true;
 		     }
 		    	 Player.notify("You cant use this command");return true;
 		     }
 
 		     if (split[0].equalsIgnoreCase("/snow")) {
 		    	 if ((Player.canUseCommand("/worldtools")) || (Player.canUseCommand("/snow"))){
 		       int dist = 0;
 		       if (split.length == 2) try { dist = Integer.parseInt(split[1]); } catch (Throwable localThrowable5) {
 		         } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /snow <radius>"); return true; }
 		         int radius = 0;
 		         try {radius = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /snow <radius>");return true;}
 		         int xmin = (int)Player.getX()-radius;
 		         int xmax = (int)Player.getX()+radius;
 		         int ymin = (int)Player.getY()-radius;
 		         int ymax = (int)Player.getY()+radius;
 		         int zmin = (int)Player.getZ()-radius;
 		         int zmax = (int)Player.getZ()+radius;
 		        
 		         for (int x = xmin; x <= xmax; x++) {
 		                 for (int y = ymin; y <= ymax; y++) {
 		                         for (int z = zmin; z <= zmax; z++) {    
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 0){Player.getWorld().setBlockAt(78, x, y, z);}
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 8){Player.getWorld().setBlockAt(79, x, y, z);}
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 9){Player.getWorld().setBlockAt(79, x, y, z);}
 		                         }
 		                       }
 		                     }
 		       Player.sendMessage("a Snow placed & water frozen!");
 		       return true;
 		     }
 		     Player.notify("You cant use this command");return true;
 	}
 
		     if (split[0].equalsIgnoreCase("/waterfix")) {
		    	 if ((Player.canUseCommand("/worldtools")) && (Player.canUseCommand("/waterfix"))){
 		       int dist = 0;
 		       if (split.length == 2) try { dist = Integer.parseInt(split[1]); } catch (Throwable localThrowable6) {
 		         } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /waterfix <radius>"); return true; }
 		         int radius = 0;
 		         try {radius = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /waterfix <radius>");return true;}
 		         int xmin = (int)Player.getX()-radius;
 		         int xmax = (int)Player.getX()+radius;
 		         int ymin = (int)Player.getY()-radius;
 		         int ymax = (int)Player.getY()+radius;
 		         int zmin = (int)Player.getZ()-radius;
 		         int zmax = (int)Player.getZ()+radius;
 		        
 		         for (int x = xmin; x <= xmax; x++) {
 		                 for (int y = ymin; y <= ymax; y++) {
 		                         for (int z = zmin; z <= zmax; z++) {    
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 8){Player.getWorld().setBlockAt(95, x, y, z);}
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 9){Player.getWorld().setBlockAt(95, x, y, z);}
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(9, x, y, z);}
 		                         }
 		                       }
 		                     }
 		       Player.sendMessage("a Water Successfully Fixed!");
 		       return true;
 		     }
		    	 Player.notify("You cant use this command");return true;
		     }
 
 		     if (split[0].equalsIgnoreCase("/lavafix")) {
 		    	 if ((Player.canUseCommand("/worldtool")) || (Player.canUseCommand("/lavafix"))){
 		       int dist = 0;
 		       if (split.length == 2) try { dist = Integer.parseInt(split[1]); } catch (Throwable localThrowable7) {
 		         } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /lavafix <radius>"); return true; }
 		         int radius = 0;
 		         try {radius = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is /lavafix <radius>");return true;}
 		         int xmin = (int)Player.getX()-radius;
 		         int xmax = (int)Player.getX()+radius;
 		         int ymin = (int)Player.getY()-radius;
 		         int ymax = (int)Player.getY()+radius;
 		         int zmin = (int)Player.getZ()-radius;
 		         int zmax = (int)Player.getZ()+radius;
 		        
 		         for (int x = xmin; x <= xmax; x++) {
 		                 for (int y = ymin; y <= ymax; y++) {
 		                         for (int z = zmin; z <= zmax; z++) {    
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 10){Player.getWorld().setBlockAt(95, x, y, z);}
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 11){Player.getWorld().setBlockAt(95, x, y, z);}
 		                    if (Player.getWorld().getBlockAt(x, y, z).getType() == 95){Player.getWorld().setBlockAt(9, x, y, z);}
 		                         }//TODO
 		                       }
 		                     }
 		       Player.sendMessage("a Lava Successfully Fixed!");
 		       return true;
 		     }
 		    	 Player.notify("You cant use this command");return true;
 		     }
 		     
 		     if (split[0].equalsIgnoreCase("/lighter")){ 
 		    	 if(Player.canUseCommand("/lighter") || (Player.canUseCommand("/worldtools"))) {
 		       int dist = 0;
 		       if (split.length == 0) try { dist = Integer.parseInt(split[0]); } catch (Throwable localThrowable8) {
 		         } if (dist == 0) { Player.sendMessage("cWrong syntax! Usage: /lighter"); return true;
 		       }
 		       Player.giveItem(260, 1);
 		       Player.sendMessage("a No smoke without 6fire!"); 
 		       return true;
 		     }
 		    	 Player.notify("You cant use this command");return true;
 		     }
 		     
 		     if (split[0].equalsIgnoreCase("/cmob")){
 		    	 if(Player.canUseCommand("/cmob") || (Player.canUseCommand("/worldtools"))) {
 		         try {
 		           int r = Integer.valueOf(split[1]).intValue();
 		           WorldToolsVoids.cMob(r);
 		           Player.sendMessage("aCleared mobs");
 		           return true;
 		         } catch (Exception e) {
 		           Player.sendMessage("cWrong syntax! Usage: /cmob <radius>");
 		           return true;
 		         }
 		       }
 		    	 Player.notify("You cant use this command");return true;
 		     }
 		     
 		     /**
 		      * killmobs
 		      */
 		     if (split[0].equalsIgnoreCase("/killmobs")){
 		    		 if (Player.canUseCommand("/killmobs") || (Player.canUseCommand("/worldtools"))) {
 		         int mobcount = Player.getWorld().getMobList().size();
 		         for (int i = 0; i < mobcount; i++) {
 		           ((Mob)Player.getWorld().getMobList().get(i)).setHealth(0);
 		         }
 		         Player.sendMessage("aYou Killed " + mobcount + " Mobs.");
 		         return true;
 		       }
 		    		 Player.notify("You cant use this command");return true;
 		     }
 		     
 		     /**
 		      * replace feature
 		      * 
 		      */ 
 		     if (split[0].equalsIgnoreCase("/wreplace")){
 		    	 if ((Player.canUseCommand("/wreplace") || (Player.canUseCommand("/worldtools")))){
 		         if (split.length <4 || split.length >4){
 		                 Player.notify("The correct usage is '/wreplace fromid toid radius'");
 		                 return true;
 		         } 
 		         Integer.parseInt(split[1]); Integer.parseInt(split[2]);Integer.parseInt(split[3]);
 		     int fromid = 0;
 		     try{fromid = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is '/wreplace fromid toid radius");return true;}
 		     int toid = 0;
 		     try{fromid = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is '/wreplace fromid toid radius");return true;}
 		     int radius = 0;
 		     try{fromid = Integer.parseInt(split[1]);}catch(NumberFormatException nfe){Player.notify("The correct usage is '/wreplace fromid toid radius");return true;}
 		         WorldToolsVoids.replace(Player,fromid,toid,radius);
 		         
 		         Player.sendMessage("aBlocks Replaced.");
 		         return true;
 		    	 }
 		    	 Player.notify("You cant use this command");return true;
 		 }
 		     
 		     if ((split[0].equalsIgnoreCase("/worldtools")) && (Player.canUseCommand("/worldtools"))) {
 		    	 if (split.length >1 || split.length <1){Player.notify("The correct usage is '/worldtools'");return true;}
 		           Player.sendMessage("6 WorldTools " + WorldTools.version + " by Glacksy 8&6 Spenk");
 		           return true;
 		         }
 		     
 		     if ((split[0].equalsIgnoreCase("/suicide"))){
 		    	 if ((Player.canUseCommand("/suicide") || (Player.canUseCommand("/worldtool")))){
 		    		 if (split.length >1 || split.length <1){Player.notify("The correct usage is '/suicide'");return true;}
 		           Player.setHealth(0);
 		           Player.sendMessage("cYou committed suicide");
 		           return true;
 		    	 }
 		    	 Player.notify("You cant use this command");return true;
 		     }
 		     
 		     /**
 		      * kill code
 		      * @author spenk
 		      */
 		           if ((split[0].equalsIgnoreCase("/kill"))){    
 		        	   if ((Player.canUseCommand("/worldtools")) || (Player.canUseCommand("/kill"))){
 		        		   if (split.length <2 || split.length >2){
 		        		   Player.notify("The correct usage is /kill <player>");
 		        		   return true;
 		        	   }else{
 		        		   Player player = etc.getServer().matchPlayer(split[1]);
 		        		   if (player == null){
 		        			   Player.notify("cThis Player Doesnt Exist or is currently not online!");
 		        			   return true;
 		        		   }
 		        		   player.sendMessage("4You got killed by 2"+Player.getName());
 		        		   player.dropInventory(player.getLocation());
 		        		   player.setHealth(0);
 		        		   Player.sendMessage("2Player sucsessfully killed!");
 		        		   return true;
 		        	   }
 		        	   }
 		        		   Player.sendMessage("cYou cant use this command");
 		        		   return true;
 		        	   }
 		           
 		           if ((split[0].equalsIgnoreCase("/heal"))){           
 		        	   if ((Player.canUseCommand("/worldtools")) || (Player.canUseCommand("/heal"))){
 		    			   if (split.length <2 || split.length >2){
 		            		   Player.sendMessage("cThe correct usage is /heal <player>");
 		            		   return true; 
 		    			   }else{
 		        		   Player player = etc.getServer().matchPlayer(split[1]);
 		        		   if (player == null){
 		        			   Player.sendMessage("cThis Player Doesnt Exist or is currently not online!");
 		        			   return true;
 		        		   }
 		        		   player.sendMessage("4You were healed by 2"+Player.getName());
 		        		   player.setHealth(20);
 		   				   player.setFoodLevel(20);
 		        		   Player.sendMessage("2Player sucsessfully healed!");
 		        		   return true;
 		    			   }
 		        	   }
 		        		   Player.notify("You cant use this command");return true;
 		           }
 		           
 		           if (split[0].equalsIgnoreCase("/save-inv")){
 		        	   if (Player.canUseCommand("/worldtools") || (Player.canUseCommand("/save-inv"))){
 		        		   if (split.length <1 || split.length >1){Player.notify("The correct usage is '/save-inv'");return true;}
 		        	   etc.getServer().saveInventories();
 		        	   Player.sendMessage("aInventories saved");
 		               return true;
 		           }
 		        	   Player.notify("You cant use this command");return true;
 		           }
 		           
 		           if ((split[0].equalsIgnoreCase("/godmode"))) {
		        	   if ((Player.canUseCommand("/godmode")) || (Player.canUseCommand("/worldtools"))){
 		        		   if (split.length > 2){Player.notify("The correct usage is /godmode (player)");return true;
 		        	   }
 		        		   if (split.length == 1){
 				        	      if (!god.contains(Player.getName())) {
 					        	        god.add(Player.getName());
 					        	        Player.sendMessage("3Godmode have been enabled");
 					        	        return true;
 					        	      }else{
 					        	      god.remove(Player.getName());
 					        	      Player.sendMessage("3Godmode have been disabled");
 					        	      return true;
 					        	      }
 		        		   }
 		        		   if (split.length == 2){
 		        			   Player player2 = etc.getServer().matchPlayer(split[1]);
 		        			   if (player2 == null){Player.notify("This player does not exist or is not logged in!");return true;}
 		        	      if (!god.contains(player2.getName())) {
 		        	        god.add(player2.getName());
 		        	        Player.sendMessage("3Godmode have been enabled");
 		        	        player2.sendMessage("3Godmode have been enabled");
 		        	        return true;
 		        	      }else{
 		        	      god.remove(player2.getName());
 		        	      Player.sendMessage("3Godmode have been disabled");
 		        	      player2.sendMessage("3Godmode have been disabled");
 		        	      return true;
 		        	      }
 		        	    }
 		        	   Player.notify("You cant use this command");return true;
 		           }
 		           }
 		           if (split[0].equalsIgnoreCase("/feed")){
		        	   if (Player.canUseCommand("/worldtools") || (Player.canUseCommand("/feed"))){
 		        	   if (split.length <2 || split.length >2){
 		        		   Player.notify("The correct usage is /feed player");
 		        		   return true;
 		        	   }
 		        	   Player player2 = etc.getServer().matchPlayer(split[1]);
 		        	   if (player2 == null) { Player.notify("This Player does not exist or is currently not logged in!"); return true;}
 		        	   player2.setFoodLevel(20);
 		        	   Player.sendMessage("2"+player2.getName()+"'s foodlevel is restored!");
 		        	   player2.sendMessage("2"+Player.getName()+" Restored your foodlevel!");
 		        	   return true;
 		           }
 		        	   Player.notify("You cant use this command");return true;
 		           }
 		           if (split[0].equalsIgnoreCase("/getip")){
 		        	   if (Player.canUseCommand("/worldtools") || (Player.canUseCommand("/getip"))){
 		        	   if (split.length <2 || split.length >2){
 		        		   Player.notify("The correct usage is /getip <player>");
 		        		   return true;
 		        	   }
 		        	   Player player2 = etc.getServer().matchPlayer(split[1]);
 		        	   if (player2 == null){Player.notify("This player doesnt exist or is not logged in");return true;}
 		        	   Player.sendMessage("4"+player2.getName()+"2 His IP is 4"+player2.getIP());
 		        	   return true;
 		           }
 		           if (split[0].equalsIgnoreCase("/forcewarp")){
 		        	   if (Player.canUseCommand("/worldtools") || (Player.canUseCommand("/forcewarp"))){
 		        	   if (split.length <3 || split.length >3){
 		        		   Player.notify("The correct usage is /forcewarp player warpname");
 		        		   return true;
 		        	   }
 		        	   Player player2 = etc.getServer().matchPlayer(split[1]);
 		        	   if (player2 == null) { Player.notify("This Player does not exist or is currently not logged in!"); return true;}
 		        	   Warp warp = etc.getDataSource().getWarp(split[2]);
 		        	   if (warp == null){Player.notify("This warp doesnt exist!");return true;}
 		        	   player2.teleportTo(warp.Location);
 		        	   Player.sendMessage("2"+player2.getName()+" is warped!");
 		        	   player2.sendMessage("2"+Player.getName()+" warped you!");
 		        	   return true;
 		           }
 		           Player.notify("You cant use this command");return true;
 		           }
 		           
 		           if (split[0].equalsIgnoreCase("/switchworlds")){
 		        	   if (Player.canUseCommand("/worldtools") || (Player.canUseCommand("/switchworlds"))){
 		        	   if (split.length <3 || split.length >3){
 		        		   Player.notify("The correct usage is /swichworlds player worldname");
 		        		   Player.notify("-1 = nether, 0 = normal world , 1 = end");
 		        		   return true;
 		        	   }
 		        	   Player player2 = etc.getServer().matchPlayer(split[1]);
 		        	   if (player2 == null) { Player.notify("This Player does not exist or is currently not logged in!"); return true;}
 		        	   
 		        	   File f = new File(split[2]);
 		        	   if (!f.exists()){
 		        		   Player.notify("This World does not exist!");
 		        		   return true;
 		        	   }
 		        	   
 		        	   if (!etc.getServer().isWorldLoaded(split[2])){
 		        		   Player.notify("This world isnt loaded! please load it before you try to teleport!");
 		        		   return true;
 		        	   }
 		        	   
 		        	   World[] w = etc.getServer().getWorld(split[2]);
 		        	   player2.switchWorlds(w[0]);
 		        	   Player.sendMessage("2"+player2.getName()+" Has swiched to world "+w[0].getName());
 		        	   player2.sendMessage("2"+Player.getName()+" Has swiched you to world "+w[0].getName());
 		        	   return true;
 		        	   }
 		        	   Player.notify("You cant use this command");return true;
 		           }
 		           
 		             if (split[0].equalsIgnoreCase("/freeze")) {
 		        	   if (Player.canUseCommand("/freeze") || (Player.canUseCommand("/worldtools"))){
 		        		   if (split.length > 2 || split.length < 2){Player.notify("The correct usage is '/freeze <player>'");return true;}
 		               Player offender = etc.getServer().matchPlayer(split[1]);
 		               if (offender == null){
 		            	   Player.notify("This Player does not exist or is currently not logged in!");
 		            	   return true;
 		               }
 		               
 		               if ((!frozen.contains(offender.getName())) && (split[1] != null)) {
 		                 offender.sendMessage("2You have been frozen by 3" + Player.getName() + "e!");
 		                 Player.sendMessage("2You froze 3" + offender.getName() + "2!");
 		                 frozen.add(offender.getName());
 		                 return true;
 		               }
 		               offender.sendMessage("2You have been thawed!");
 		               Player.sendMessage("2You unfroze 3" + Player.getName() + "2!");
 		               frozen.remove(offender.getName());
 		               return true;
 		             }
 		        	   Player.notify("You cant use this command");return true;
 		           }
 		           
 		           if (split[0].equalsIgnoreCase("/setspawn")){
 		        	   if (Player.canUseCommand("/setspawn")||Player.canUseCommand("/worldtools")){
 		        		   if (split.length > 1 || split.length < 1){Player.notify("The correct usage is '/setspawn'");return true;}
 		               exactSpawn = new Location(Player.getX(), Player.getY(), Player.getZ(), Player.getRotation(), Player.getPitch());
 		               PropertiesFile props = new PropertiesFile("worldtools.properties");
 		               props.setString("exact-spawn", exactSpawn.x + "," + exactSpawn.y + "," + exactSpawn.z + "," + exactSpawn.rotX + "," + exactSpawn.rotY);
 		               return true;
 		        	   }
 		        	   Player.notify("You cant use this command");return true;
 		             }
 		           
 		           if (split[0].equalsIgnoreCase("/spawn") && exactSpawn != null) {
 		        	   if ((Player.canUseCommand("/spawn")) || Player.canUseCommand("/worldtools")){
 		               Player.teleportTo(exactSpawn);
 		               return true;
 		             }
 		        	   Player.notify("You cant use this command");return true;
 		           }
 		           
 		             if (split[0].equalsIgnoreCase("/locate")){
 		            	 if (Player.canUseCommand("/locate")|| Player.canUseCommand("/worldtools")){
 		            		 if (split.length > 2 || split.length < 2){
 		            			 Player.notify("The correct usage is '/locate <player>'");return true;
 		            		 }
 		            		 Player player2 = etc.getServer().matchPlayer(split[1]);
 		            		 if (player2 == null){Player.notify("cTis player doesnt exist or is currently not logged in!");return true;}
 		            		 Player.sendMessage(player2.getName()+"Is located in World:"+player2.getWorld().getName()+" in Dimension:"+player2.getLocation().dimension +" at location X:"+player2.getX() +" Y:"+player2.getY() +" Z:"+player2.getZ());
 		            		 return true;
 		            	 }
 		            	 Player.notify("You cant use this command");return true;
 		             }
 		             }
 		           
 		     return false;
 		   }
 public void onPlayerMove(Player player, Location from, Location to) {
     if (frozen.contains(player.getName()))
       player.teleportTo(from);
   }
 public void onLogin(Player player) {
     if (god.contains(player.getName()))
     if (god.contains(player.getName()))
       god.remove(player.getName());
       god.remove(player.getName());
   }
 
 public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount)
 {
 if ((defender.isPlayer()) && 
 	      (god.contains(defender.getPlayer().getName()))) {
 	      Player localplayer = defender.getPlayer();
 
 	      if ((localplayer.canUseCommand("/godmode")) && 
 	        (type.equals(PluginLoader.DamageType.ENTITY)) && 
 	        (type.equals(PluginLoader.DamageType.CACTUS)) && 
 	        (type.equals(PluginLoader.DamageType.FALL)) && 
 	        (type.equals(PluginLoader.DamageType.FIRE)) && 
 	        (type.equals(PluginLoader.DamageType.LAVA)) && 
 	        (type.equals(PluginLoader.DamageType.SUFFOCATION)) && 
 	        (type.equals(PluginLoader.DamageType.FIRE_TICK)) &&
 	        (type.equals(PluginLoader.DamageType.CREEPER_EXPLOSION)) &&
 	        (type.equals(PluginLoader.DamageType.EXPLOSION)) &&
 	        (type.equals(PluginLoader.DamageType.POTION)) &&
 	        (type.equals(PluginLoader.DamageType.STARVATION)) &&
 	        (type.equals(PluginLoader.DamageType.WATER)) &&
 	        (type.equals(PluginLoader.DamageType.LIGHTNING)))
 	      {
 	        return true;
 	      }
 	    }
 return false;
 }
 }
 
 /**
  * @commands
  * /drain
  * /drainlava
  * /drainwater
  * /ext
  * /melt
  * /snow
  * /lavafix
  * /waterfix
  * /lighter
  * /cmob
  * /wreplace
  * /killmobs
  * /suicide
  * /kill
  * /heal
  * /save-inv
  * /freeze
  * /godmode
  * /food
  * /swichworlds
  * /getip
  */
 // end of class
