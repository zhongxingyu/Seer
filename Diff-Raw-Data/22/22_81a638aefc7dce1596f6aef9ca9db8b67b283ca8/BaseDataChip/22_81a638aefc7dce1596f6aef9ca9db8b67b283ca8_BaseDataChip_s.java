 package com.grover.mingebag.ic;
 
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseIC;
 import com.bukkit.gemo.FalseBook.IC.ICs.Lever;
 import com.bukkit.gemo.utils.ICUtils;
 import com.bukkit.gemo.utils.SignUtils;
 
 import org.bukkit.Location;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.SignChangeEvent;
 
 
 
 public class BaseDataChip extends BaseIC
 {
 	
     public void checkCreation(SignChangeEvent event) {
 
         String line = event.getLine(1);
         Integer user = -1;
        Integer pulse = 1;
         
         if (line.length() > 0) {
             try {
                 user = Integer.parseInt(line);
                 pulse = user;
             } catch (Exception e) {
             }
         }
         
        if (pulse > 600 || pulse < 1) {
            pulse = 1;
         }
 
         if (pulse != user) {
             event.setLine(1, pulse.toString());
         }
     }
    
 	public void outputData(BaseData data, final Sign signBlock, final int distance) {
 		outputData(data, signBlock, distance, Lever.BACK);
 	}
 	
 	public void outputDataLeft(BaseData data, DataTypes type, final Sign signBlock, final int distance) {
 		outputData(data, signBlock, distance, Lever.LEFT);
 	}
 	
 	public void outputDataRight(BaseData data, DataTypes type, final Sign signBlock, final int distance) {
 		outputData(data, signBlock, distance, Lever.RIGHT);
 	}
 	
 	private void outputData(BaseData data, final Sign signBlock, final int distance, final Lever lever) {
 		
 		// send datatype
 		DataTypeManager manager = this.core.getFactory().getDataTypeManager();
 		manager.addDataType(ICUtils.getLeverPos(signBlock, distance), new DataType(data));
 		switchLever(lever, signBlock, true, distance);
 		
 		// pulse
 		Integer pulse = 2;
 		if(signBlock.getLine(1).length() > 0) {
 			try {
 				pulse = Integer.parseInt(signBlock.getLine(1));
 			} catch (Exception e) {
 			}
 		}
 	
 		if(pulse > 600 || pulse < 0) {
			pulse = 1;
 		}
 		
 		if(pulse > 0) {
 			this.core.getServer().getScheduler().scheduleSyncDelayedTask(this.core, new Runnable() {
 			    public void run() {
 			        switchLever(lever, signBlock, false, distance);
 			    }
 			}, pulse);
 		}
 		
 		// end datatype at lever
 		if(lever == Lever.BACK)
 			manager.endDataType(ICUtils.getLeverPos(signBlock, distance));
 			
 		if(lever == Lever.LEFT)
 			manager.endDataType(ICUtils.getLeverPosLeft(signBlock, distance));
 		
 		if(lever == Lever.RIGHT)
 			manager.endDataType(ICUtils.getLeverPosRight(signBlock, distance));
 	}
 	
 	public Location getDataLocation(Location location, int direction, int orientation) {
 		// Orientation 0 = foward, 1 = left, 2 = right
 		Location loc = location.clone();
 		
 		if (orientation == 0) {
 			if (direction == 1)
 				loc.add(0,0,1);
 			
 			if (direction == 2) 
 				loc.add(1,0,0);
 
 			if (direction == 3) 
 				loc.subtract(0,0,1);
 
 			if (direction == 4)
 				loc.subtract(1,0,0);
 		}
 		
 		if (orientation == 1) {
 
 			if (direction == 1) 
 				loc.subtract(1,0,0);
 
 			if (direction == 2)
 				loc.add(0,0,1);
 
 			if (direction == 3)
 				loc.add(1,0,0);
 
 			if (direction == 4)
 				loc.subtract(0,0,1);
 
 		}
 		
 		if (orientation == 2) {
 			
 			if (direction == 1)
 				loc.add(1,0,0);
 			
 			if (direction == 2)
 				loc.subtract(0,0,1);
 
 			if (direction == 3)
				loc.subtract(0,0,1);
 
 			if (direction == 4)
 				loc.add(0,0,1);
 		}
 
 
 		return loc;
 	}
 	
 	public BaseData getData(Sign signBlock) {
 		return getData(signBlock, 0);
 	}
 	
 	public BaseData getDataLeft(Sign signBlock) {
 		return getData(signBlock, 1);
 	}
 	
 	public BaseData getDataRight(Sign signBlock) {
 		return getData(signBlock, 2);
 	}
 	
 	
 	public BaseData getData(Sign signBlock, int orientation) {
 		DataType type = null;
 		
 		Location loc = getDataLocation(signBlock.getLocation(), SignUtils.getDirection(signBlock), orientation);
 		
 		type = core.getFactory().getDataTypeManager().getDataType(loc);
 		
 		if (type == null) {
 			BlockState block = signBlock.getWorld().getBlockAt(loc).getState();
 			if (block instanceof Sign) {
 				Sign argSign = (Sign) block;
 				String argType = argSign.getLine(0);
 				
 				if (argType.length() == 0)
 					return null;
 				
 				if ( !argType.equals("string") && !argType.equals("int") )
 					return null;
 
 				if (argType.equals("string")) {
 					return new StringData(argSign.getLine(1)
 							+ argSign.getLine(2)
 							+ argSign.getLine(3));
 				}
 				
 				if (argType.equals("int")) {
 					try {
 						return new NumberData(Integer.parseInt(argSign.getLine(1)));
 					} catch (Exception e) {
 						return null;
 					}
 				}
 			}
 			return null;
 		}
 			
 		
 		return type.getData();
 	}
 	
 
 	
 }
