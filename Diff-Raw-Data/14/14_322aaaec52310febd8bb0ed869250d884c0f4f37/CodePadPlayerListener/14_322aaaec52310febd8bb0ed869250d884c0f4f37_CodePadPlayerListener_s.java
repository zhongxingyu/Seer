 package de.davboecki.signcodepad;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import de.davboecki.signcodepad.event.CalSaver;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 
 public class CodePadPlayerListener extends PlayerListener {
     SignCodePad plugin;
 
     CodePadPlayerListener(SignCodePad pplugin) {
         plugin = pplugin;
     }
 
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         
         if (!plugin.CalibrationSettings.CalibrationList.containsKey(
                     player.getName()) && plugin.hasPermission(event.getPlayer(), "SignCodePad.use")) {
             new CalNoteThread(player).start();
         }
     }
 
     private double positivgrad(double grad) {
         if (grad < 0) {
             grad += 360;
         }
 
         return grad;
     }
 
     private double getXpos(Player player, Sign sign, Location signloc) {
         double Yaw = positivgrad(player.getLocation().getYaw());
         Location playerloc = player.getLocation();
         double x = -1;
         double z = -1;
 
         switch ((int) sign.getRawData()) {
         //Westen
         case 2:
             x = signloc.getZ() - playerloc.getZ() + 1;
             z = signloc.getX() - playerloc.getX() + 1;
 
             break;
 
         //Osten
         case 3:
             x = playerloc.getZ() - signloc.getZ();
             z = playerloc.getX() - signloc.getX();
 
             break;
 
         //Sden
         case 4:
             x = signloc.getX() - playerloc.getX() + 1;
             z = playerloc.getZ() - signloc.getZ();
 
             break;
 
         //Norden
         case 5:
             x = playerloc.getX() - signloc.getX();
             z = signloc.getZ() - playerloc.getZ() + 1;
 
             break;
         }
 
         Yaw = getXposSuba(Yaw, (int) sign.getRawData());
         x -= 0.12;
 
         //player.sendMessage("X:"+x+"\nZ:"+z);
         return (Math.tan(Math.toRadians(Yaw)) * x) + z;
     }
 
     private double getXposSuba(double Yaw, int value) {
         switch (value) {
         //Westen
         case 2:
             break;
 
         //Osten
         case 3:
             Yaw -= 180;
 
             break;
 
         //Sden
         case 4:
             Yaw -= 270;
 
             break;
 
         //Norden
         case 5:
             Yaw -= 90;
 
             break;
         }
 
         Yaw = positivgrad(Yaw);
 
         return Yaw;
     }
 
     private double getYpos(Player player, Sign sign, Location signloc) {
         double Yaw = positivgrad(player.getLocation().getYaw());
         double Pitch = positivgrad(player.getLocation().getPitch());
         Location playerloc = player.getLocation();
         double x = -1;
         double y = -1;
         double z = -1;
 
         switch ((int) sign.getRawData()) {
         //Westen
         case 2:
             x = signloc.getZ() - playerloc.getZ() + 1;
             y = playerloc.getY() - signloc.getY();
             z = signloc.getX() - playerloc.getX() + 1;
 
             break;
 
         //Osten
         case 3:
             x = playerloc.getZ() - signloc.getZ();
             y = playerloc.getY() - signloc.getY();
             z = playerloc.getX() - signloc.getX();
 
             break;
 
         //Sden
         case 4:
             x = signloc.getX() - playerloc.getX() + 1;
             y = playerloc.getY() - signloc.getY();
             z = playerloc.getZ() - signloc.getZ();
 
             break;
 
         //Norden
         case 5:
             x = playerloc.getX() - signloc.getX();
             y = playerloc.getY() - signloc.getY();
             z = signloc.getZ() - playerloc.getZ() + 1;
 
             break;
         }
 
         Pitch = positivgrad(Pitch);
         Yaw = getXposSuba(Yaw, (int) sign.getRawData());
         z -= getXpos(player, sign, signloc);
         y += player.getEyeHeight();
         x -= 0.15;
 
         return ((Math.tan(Math.toRadians(Pitch)) * (Math.sqrt((x * x) +
             (z * z)))) - y + 0.3) * -2;
     }
 
     private void Sternchen(String Code, Sign sign) {
         int Anzahl = Code.length();
 
         StringBuilder Line = new StringBuilder();
         Line.append("4 5 6 | ");
 
         for (int i = 0; i < Anzahl; i++) {
             Line.append("*");
         }
 
         for (int i = Anzahl; i < 4; i++) {
             Line.append("-");
         }
 
         if (Anzahl > 2) {
             Line.append(" ");
         }
 
         //sign.setLine(0, "1 2 3 |       ");
         sign.setLine(1, Line.toString());
         sign.setLine(2, "7 8 9 |  <<- ");
         sign.setLine(3, "* 0 # |  OK  ");
         sign.update();
     }
 
     private void setError(Sign sign, Player player, String Type) {
         sign.setLine(0, "1 2 3 |  cErr ");
         sign.update();
         new ErrorReset(sign, player).start();
 
         if (Type == "WrongCode") {
             if (((Location) plugin.getSetting(sign.getBlock().getLocation(),"Error-Location")).getY() >= 0) {
                 int Count = 0;
 
                 if (plugin.ErrorCount.containsKey(sign.getBlock().getLocation())) {
                     Count = plugin.ErrorCount.get(sign.getBlock().getLocation());
                 }
 
                 Count++;
 
                 int ErrorCount = 0;
 
                 try {
                     ErrorCount = Integer.parseInt((String) plugin.getSetting(sign.getBlock().getLocation(), "Error-Count"));
                 } catch (ClassCastException e) {
                     try {
                         ErrorCount = (int) (((Double) plugin.getSetting(sign.getBlock().getLocation(),"Error-Count")) * 1);
                     } catch (ClassCastException ex) {
                         ErrorCount = (Integer) plugin.getSetting(sign.getBlock().getLocation(),"Error-Count");
                     }
                 }
 
                 if (Count > ErrorCount) {
                     Block block = sign.getWorld().getBlockAt((Location) plugin.getSetting(sign.getBlock().getLocation(), "Error-Location"));
             		if(block.getTypeId() == Material.TORCH.getId()){
             			block.setTypeId(Material.REDSTONE_TORCH_ON.getId());
             		} else {
             			player.sendMessage("No torch to change.");
             		}
             		new RedstoneTorchReset(block,(int) (Double.parseDouble((String) plugin.getSetting(sign.getBlock().getLocation(), "Error-Delay")) * 1000),sign, player).start();
             		Count = 0;
                 }
                 plugin.ErrorCount.put(sign.getBlock().getLocation(), Count);
             }
         }
     }
 
     public void onPlayerInteract(PlayerInteractEvent event) {
     	if(event.getClickedBlock() == null) return;
         if (event.getClickedBlock().getTypeId() == Material.WALL_SIGN.getId()) {
             if (plugin.CalLoc.containsKey(event.getPlayer().getName())) {
             	if(plugin.CalType.containsKey(event.getPlayer().getName())){
             		if(plugin.CalType.get(event.getPlayer().getName()) == CalTypes.Normal){
                     	handleCalSignNormal(event);
             		} else if(plugin.CalType.get(event.getPlayer().getName()) == CalTypes.Advanced){
             			handleCalSignAdvanced(event);
             		} else {
             			handleCalSignNormal(event);
             		}
             	} else {
             		handleCalSignNormal(event);
             	}
             }
             if (plugin.hasSetting(event.getClickedBlock().getLocation())) {
             	handleCodeEnter(event);
             }
         }
     }
     
     private char[] SignCharList = {'S','B','1','2','3','4','5','6','7','8','9','r','R','*','0','#','k','K','X'};
     private HashMap<String,SignLoc> r = new HashMap<String,SignLoc>();
     private HashMap<String,SignLoc> k = new HashMap<String,SignLoc>();
     private HashMap<String,SignLoc> s = new HashMap<String,SignLoc>();
     private HashMap<String,Double> d = new HashMap<String,Double>();
     
     private ArrayList<CalibrationPosition> CreateCalList(CalSaver Calsave,PlayerInteractEvent event){
     	String Name = event.getPlayer().getName();
     	ArrayList<CalibrationPosition> CalList = new ArrayList<CalibrationPosition>();
     	for(SignLoc part: Calsave.CalPos.values()){
     		if(part.world.equals("r")) {
     			r.put(Name, part);
     		} else if(part.world.equals("k")) {
     			k.put(Name, part);
     		} else if(part.world.equals("S")) {
     			//Nichts
     		} else if(part.world.equals("B")) {
     			//Nichts
     		} else {
     			double x = part.x;
     			double y = part.y;
                 double x_size = 0.035;
     			if(d.containsKey(event.getPlayer().getName())){
     				double v = d.get(event.getPlayer().getName());
     				x_size *= v;
     			}
     			double x1 = x-x_size;
     			double x2 = x+x_size;
     			double y1 = y-0.08;
     			double y2 = y+0.08;
     			if(part.world.equals("R") && r.containsKey(Name)){
     				SignLoc partb = r.get(Name);
         			x = partb.x;
         			y = partb.y;
         			x1 = x-0.035;
         			y1 = y-0.08;
     			}
     			if(part.world.equals("K") && k.containsKey(Name)){
     				SignLoc partb = k.get(Name);
         			x = partb.x;
         			y = partb.y;
         			x1 = x-0.035;
         			y1 = y-0.08;
     			}
     			char Lapping = 'X';
 	    		if(new Calibration(CalList).getCharAt(x1, y1) != 'X' && Lapping == 'X'){
 	    			Lapping = new Calibration(CalList).getCharAt(x1, y1);
 	    		}
 	    		if(new Calibration(CalList).getCharAt(x1, y2) != 'X' && Lapping == 'X'){
 	    			Lapping = new Calibration(CalList).getCharAt(x1, y2);
 	    		}
 	    		if(new Calibration(CalList).getCharAt(x2, y1) != 'X' && Lapping == 'X'){
 	    			Lapping = new Calibration(CalList).getCharAt(x2, y1);
 	    		}
 	    		if(new Calibration(CalList).getCharAt(x2, y2) != 'X' && Lapping == 'X'){
 	    			Lapping = new Calibration(CalList).getCharAt(x2, y2);
 	    		}
     			if(SignCharList[Calsave.CalNumber] == 'K' || SignCharList[Calsave.CalNumber] == 'R' || Lapping == 'X'){
     				CalList.add(new CalibrationPosition(part.world.charAt(0),x1,y1,x2,y2));
     			} else {
     				return null;
     			}
     		}
     	}
     	return CalList;
     }
     
     private void handleCalSignAdvanced(PlayerInteractEvent event){
     	if(!plugin.hasPermission(event.getPlayer(), "SignCodePad.use")){
     		event.getPlayer().sendMessage("You do not have Permission to do that.");
     		return;
     	}
     	if (plugin.CalLoc.get(event.getPlayer().getName()).equals(new SignLoc(event.getClickedBlock().getLocation()))) {
     		CalSaver Calsave = plugin.CalSaverList.get(event.getPlayer().getName());
             double x = getXpos(event.getPlayer(), (Sign) event.getClickedBlock().getState(), event.getClickedBlock().getLocation());
             double y = getYpos(event.getPlayer(), (Sign) event.getClickedBlock().getState(), event.getClickedBlock().getLocation());
             // Using World As Char Variable
             double x_size = 0.035;
 			if(d.containsKey(event.getPlayer().getName())){
 				double v = d.get(event.getPlayer().getName());
 				x_size *= v;
 			}
 			double x1 = x-x_size;
 			double x2 = x+x_size;
 			double y1 = y-0.08;
 			double y2 = y+0.08;
 			ArrayList<CalibrationPosition> CalList = CreateCalList(Calsave,event);
 			if(CalList != null){
 	        	char Lapping = 'X';
 	    		if(new Calibration(CalList).getCharAt(x1, y1) != 'X' && Lapping == 'X'){
 	    			Lapping = new Calibration(CalList).getCharAt(x1, y1);
 	    		}
 	    		if(new Calibration(CalList).getCharAt(x1, y2) != 'X' && Lapping == 'X'){
 	    			Lapping = new Calibration(CalList).getCharAt(x1, y2);
 	    		}
 	    		if(new Calibration(CalList).getCharAt(x2, y1) != 'X' && Lapping == 'X'){
 	    			Lapping = new Calibration(CalList).getCharAt(x2, y1);
 	    		}
 	    		if(new Calibration(CalList).getCharAt(x2, y2) != 'X' && Lapping == 'X'){
 	    			Lapping = new Calibration(CalList).getCharAt(x2, y2);
 	    		}
 				if(SignCharList[Calsave.CalNumber] == 'S' || SignCharList[Calsave.CalNumber] == 'B' || SignCharList[Calsave.CalNumber] == 'K' || SignCharList[Calsave.CalNumber] == 'R' || Lapping == 'X'){
 					if(SignCharList[Calsave.CalNumber] != 'S' && SignCharList[Calsave.CalNumber] != 'B'){
 		            	Calsave.CalPos.put(Calsave.CalNumber,new SignLoc(""+SignCharList[Calsave.CalNumber], x, y, (double) 0));
 					} else {
 						 if(SignCharList[Calsave.CalNumber] == 'S') {
 				    		s.put(event.getPlayer().getName(), new SignLoc(""+SignCharList[Calsave.CalNumber], x, y, (double) 0));
 				    	} else if(SignCharList[Calsave.CalNumber] == 'B') {
 				    		SignLoc parta = s.get(event.getPlayer().getName());
 				    		d.put(event.getPlayer().getName(), ((x - parta.x)/0.065));
 				    		event.getPlayer().sendMessage("D: "+((x - parta.x)/0.065));
 				    	}
 					}
 		            Calsave.CalNumber++;
 				} else {
 	           		event.getPlayer().sendMessage("Error: Overlapping.");
 					Calsave.CalPos = new HashMap<Integer,SignLoc>();
 					Calsave.CalNumber = 0;
 				}
 			} else {
 	           	event.getPlayer().sendMessage("Error: Overlapping.");
 				Calsave.CalPos = new HashMap<Integer,SignLoc>();
 				Calsave.CalNumber = 0;
 			}
             Sign sign = (Sign) event.getClickedBlock().getState();
             if(SignCharList[Calsave.CalNumber] == 'X'){
             	CalList = CreateCalList(Calsave,event);
             	if(CalList == null){
             		event.getPlayer().sendMessage("Error: Overlapping.");
     				Calsave.CalNumber = 0;
             	} else {
             		plugin.CalibrationSettings.CalibrationList.put(event.getPlayer().getName(), new Calibration(CalList));
             		event.getPlayer().sendMessage("Done.");
             		plugin.save();
             	}
            }
            if(SignCharList[Calsave.CalNumber] == 'X'){
                 sign.getBlock().setType(Material.AIR);
                 sign.getBlock().getLocation().getWorld().dropItem(sign.getBlock().getLocation(),new ItemStack(Material.SIGN, 1));
                 plugin.CalSaverList.remove(event.getPlayer().getName());
                 plugin.CalLoc.remove(event.getPlayer().getName());
                 plugin.CalType.remove(event.getPlayer().getName());
                 r.remove(event.getPlayer().getName());
                 k.remove(event.getPlayer().getName());
                 s.remove(event.getPlayer().getName());
                 d.remove(event.getPlayer().getName());
             } else {
             	sign.setLine(0, "1 2 3 |       ");
             	sign.setLine(1, "4 5 6 | ----");
             	sign.setLine(2, "7 8 9 |  <<- ");
             	sign.setLine(3, "* 0 # |  OK  ");
             	switch(SignCharList[Calsave.CalNumber]){
             		case 'S':
                         sign.setLine(0, "Press the");
                         sign.setLine(1, "crosses");
                         sign.setLine(2, "Start:");
                         sign.setLine(3, "+-");
                         break;
             		case 'B':
                         sign.setLine(0, "Press the");
                         sign.setLine(1, "crosses");
                         sign.setLine(2, "");
                         sign.setLine(3, "-+");
                         break;
             		case '1':sign.setLine(0, "+ 2 3 |       ");break;
             		case '2':sign.setLine(0, "1 + 3 |       ");break;
             		case '3':sign.setLine(0, "1 2 + |       ");break;
             		case '4':sign.setLine(1, "+ 5 6 | ----");break;
             		case '5':sign.setLine(1, "4 + 6 | ----");break;
             		case '6':sign.setLine(1, "4 5 + | ----");break;
             		case '7':sign.setLine(2, "+ 8 9 |  <<- ");break;
             		case '8':sign.setLine(2, "7 + 9 |  <<- ");break;
             		case '9':sign.setLine(2, "7 8 + |  <<- ");break;
             		case 'r':sign.setLine(2, "7 8 9 |  +<- ");break;
             		case 'R':sign.setLine(2, "7 8 9 |  <<+ ");break;
             		case '*':sign.setLine(3, "+ 0 # |  OK  ");break;
             		case '0':sign.setLine(3, "* + # |  OK  ");break;
             		case '#':sign.setLine(3, "* 0 + |  OK  ");break;
             		case 'k':sign.setLine(3, "* 0 # |  +K  ");break;
             		case 'K':sign.setLine(3, "* 0 # |  O+  ");break;
             	}
             	sign.update();
                 plugin.CalSaverList.put(event.getPlayer().getName(),Calsave);
             }
     		return;
     	}
     }
 	
     private void handleCalSignNormal(PlayerInteractEvent event){
 		if(!plugin.hasPermission(event.getPlayer(), "SignCodePad.use")){
     		event.getPlayer().sendMessage("You do not have Permission to do that.");
     		return;
     	}
         if (plugin.CalLoc.get(event.getPlayer().getName()).equals(new SignLoc(event.getClickedBlock().getLocation()))) {
             CalSaver Calsave = plugin.CalSaverList.get(event.getPlayer().getName());
             double x = getXpos(event.getPlayer(), (Sign) event.getClickedBlock().getState(), event.getClickedBlock().getLocation());
             double y = getYpos(event.getPlayer(), (Sign) event.getClickedBlock().getState(), event.getClickedBlock().getLocation());
             Calsave.CalPos.put(Calsave.CalNumber,new SignLoc("", x, y, (double) 0));
             Calsave.CalNumber++;
             Sign sign = (Sign) event.getClickedBlock().getState();
             if (Calsave.CalNumber >= 4) {
             	double x_abstand_a = plugin.CalSaverList.get(event.getPlayer().getName()).CalPos.get(1).x - plugin.CalSaverList.get(event.getPlayer().getName()).CalPos.get(0).x;
             	double x_abstand_b = plugin.CalSaverList.get(event.getPlayer().getName()).CalPos.get(3).x - plugin.CalSaverList.get(event.getPlayer().getName()).CalPos.get(2).x;
             	double x_abstand = ((x_abstand_a + x_abstand_b) / 2) *1.6;
             	double y_abstand_a = plugin.CalSaverList.get(event.getPlayer().getName()).CalPos.get(0).y - plugin.CalSaverList.get(event.getPlayer().getName()).CalPos.get(2).y;
             	double y_abstand_b = plugin.CalSaverList.get(event.getPlayer().getName()).CalPos.get(1).y - plugin.CalSaverList.get(event.getPlayer().getName()).CalPos.get(3).y;
             	double y_abstand = ((y_abstand_a + y_abstand_b) / 2) *1.6;
             	if(x_abstand_a < x_abstand_b*0.8 || x_abstand_a > x_abstand_b*1.2 || y_abstand_a < y_abstand_b*0.8 || y_abstand_a > y_abstand_b*1.2){
             		event.getPlayer().sendMessage("Error: Too much diverence.");
             		Calsave.CalNumber = 0;
             	} else {
             		plugin.CalibrationSettings.CalibrationList.put(event.getPlayer().getName(), new Calibration(x_abstand,y_abstand));
             		event.getPlayer().sendMessage("Done.");
             		plugin.save();
             	}
             }
             if (Calsave.CalNumber >= 4) {
                 sign.getBlock().setType(Material.AIR);
                 sign.getBlock().getLocation().getWorld().dropItem(sign.getBlock().getLocation(),new ItemStack(Material.SIGN, 1));
                 plugin.CalSaverList.remove(event.getPlayer().getName());
                 plugin.CalLoc.remove(event.getPlayer().getName());
                 plugin.CalType.remove(event.getPlayer().getName());
             } else {
                 sign.setLine(0, "");
                 sign.setLine(1, "Press the");
                 sign.setLine(2, "cross");
                 sign.setLine(3, "");
                 switch (Calsave.CalNumber) {
                 case 0:
                     sign.setLine(0, "+              ");
                     break;
                 case 1:
                     sign.setLine(0, "              +");
                     break;
                 case 2:
                     sign.setLine(3, "+              ");
                     break;
                 case 3:
                     sign.setLine(3, "              +");
                     break;
                 }
                 sign.update();
                 plugin.CalSaverList.put(event.getPlayer().getName(),Calsave);
             }
             return;
         }
 	}
 	
     private void handleCodeEnter(PlayerInteractEvent event){
     	if(!plugin.hasPermission(event.getPlayer(), "SignCodePad.use")){
     		event.getPlayer().sendMessage("You do not have Permission to do that.");
     		return;
     	}
         /* if(!plugin.CalibrationSettings.CalibrationList.containsKey(event.getPlayer().getName())){
         	event.getPlayer().sendMessage("Please Calibrate First.");
         	return;
         } */
         double x = getXpos(event.getPlayer(), (Sign) event.getClickedBlock().getState(), event.getClickedBlock().getLocation());
         double y = getYpos(event.getPlayer(), (Sign) event.getClickedBlock().getState(), event.getClickedBlock().getLocation());
         char Result = 'X';
         if(plugin.CalibrationSettings.CalibrationList.containsKey(event.getPlayer().getName())){
         	Result = plugin.CalibrationSettings.CalibrationList.get(event.getPlayer().getName()).getCharAt(x, y);
         } else {
         	Result = new Calibration(1,1).getCharAt(x, y);
         	event.getPlayer().sendMessage("Please Calibrate.");
         }
 
         if ((Result != 'X') && (Result != 'R') && (Result != 'K')) {
             if (plugin.CodeEnter.containsKey(event.getClickedBlock().getLocation())) {
                 if (plugin.CodeEnter.get(event.getClickedBlock().getLocation()).length() > 3) {
                     event.getPlayer().sendMessage("Overflow.");
                     setError((Sign) event.getClickedBlock().getState(), event.getPlayer(), "Overflow");
                     plugin.CodeEnter.put(event.getClickedBlock().getLocation(), "");
                     Sternchen("",(Sign) event.getClickedBlock().getState());
                 } else {
                     plugin.CodeEnter.put(event.getClickedBlock().getLocation(),plugin.CodeEnter.get(event.getClickedBlock().getLocation()) + Result);
                 }
             } else {
                 plugin.CodeEnter.put(event.getClickedBlock().getLocation(),""+Result);
             }
             Sternchen(plugin.CodeEnter.get(event.getClickedBlock().getLocation()), (Sign) event.getClickedBlock().getState());
         } else if (Result == 'R') {
             if (plugin.CodeEnter.containsKey(event.getClickedBlock().getLocation())) {
             	String New = "";
             	for(int i=0;i<plugin.CodeEnter.get(event.getClickedBlock().getLocation()).length()-1;i++){
             		New += plugin.CodeEnter.get(event.getClickedBlock().getLocation()).charAt(i);
             	}
                 plugin.CodeEnter.put(event.getClickedBlock().getLocation(),New);
                 Sternchen(New,(Sign) event.getClickedBlock().getState());
             }
         } else if (Result == 'K') {
             if (plugin.CodeEnter.containsKey(event.getClickedBlock().getLocation())) {
                 MD5 md5 = new MD5(plugin.CodeEnter.get(event.getClickedBlock().getLocation()));
 
                 if (!md5.isGen()) {
                     event.getPlayer().sendMessage("Internal Error (MD5)");
                     setError((Sign) event.getClickedBlock().getState(),event.getPlayer(), "MD5");
                     return;
                 }
                 
                 MD5 md5b = new MD5(md5.getValue());
 
                 if (!md5b.isGen()) {
                     event.getPlayer().sendMessage("Internal Error (MD5)");
                     setError((Sign) event.getClickedBlock().getState(),event.getPlayer(), "MD5");
                     return;
                 }
                 if (((String) plugin.getSetting(event.getClickedBlock().getLocation(), "MD5")).equalsIgnoreCase(md5.getValue()) || ((String) plugin.getSetting(event.getClickedBlock().getLocation(), "MD5")).equalsIgnoreCase(md5b.getValue())) {
                     Sign sign = (Sign) event.getClickedBlock().getState();
                     Block block = event.getClickedBlock().getWorld().getBlockAt((Location) plugin.getSetting(event.getClickedBlock().getLocation(),"OK-Location"));
                     
                     if(block.getTypeId() == Material.TORCH.getId()){
                     	block.setTypeId(Material.REDSTONE_TORCH_ON.getId());
 
                     } else {
                     	event.getPlayer().sendMessage("No torch to change.");
                     }
                     
                     sign.setLine(0, "1 2 3 |  aOK  ");
                     
                 	try {
                         new RedstoneTorchReset(block, (int) (Double.parseDouble((String) plugin.getSetting(event.getClickedBlock().getLocation(),"OK-Delay")) * 1000), sign, event.getPlayer()).start();
                     } catch (ClassCastException e) {
                         try {
                             new RedstoneTorchReset(block,(int) (((Double) plugin.getSetting(event.getClickedBlock().getLocation(),"OK-Delay")) * 1000), sign, event.getPlayer()).start();
                         } catch (ClassCastException ex) {
                             new RedstoneTorchReset(block,(int) (((Integer) plugin.getSetting(event.getClickedBlock().getLocation(),"OK-Delay")) * 1000), sign, event.getPlayer()).start();
                         }
                     }
                 } else {
                     setError((Sign) event.getClickedBlock().getState(), event.getPlayer(), "WrongCode");
                 }
             } else {
                 setError((Sign) event.getClickedBlock().getState(),event.getPlayer(), "WrongCode");
             }
             plugin.CodeEnter.put(event.getClickedBlock().getLocation(),"");
             Sternchen("", (Sign) event.getClickedBlock().getState());
         }
     }
 }
