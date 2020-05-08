 package team.GrenadesPlus.Manager;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import org.bukkit.block.Block;
 import team.GrenadesPlus.GrenadesPlus;
 import team.GrenadesPlus.Block.Placeable;
 import team.GrenadesPlus.Trigger.ExplosiveTriggerType;
 import team.GrenadesPlus.Trigger.ExplosivesTrigger;
 import team.GrenadesPlus.Trigger.TriggerActivationType;
 import team.GrenadesPlus.Trigger.TriggerListener;
 import team.GrenadesPlus.Util.ExplosiveUtils;
 import team.GrenadesPlus.Util.PlaceableData;
 import digi.tools.Db;
 
 public class DBManager {
 
 	private static Db db;
 	public static HashMap<Block, PlaceableData> blocks = new HashMap<Block, PlaceableData>();
 	
 	public static void loadAll() throws SQLException {
 		ResultSet re = db.query("SELECT * FROM `placeables`");
 		while(re.next()){
 			int id = re.getRow();
 			String name = re.getString(2);
 			String world = re.getString(3);
 			int x = re.getInt(4);
 			int y = re.getInt(5);
 			int z = re.getInt(6);
 			boolean i = re.getBoolean(7);
 			PlaceableData pd = new PlaceableData(id, name, x, y, z, world, i);
 			blocks.put(pd.getLoc().getBlock(), pd);
 			startTriggers(pd);
 		}
 	}
 	
	public static void init() throws SQLException {System.out.print("intialising db");
 		db = new Db(GrenadesPlus.plugin, GrenadesPlus.plugin.getDataFolder() + "/data.db");
 
 		db.query("CREATE TABLE IF NOT EXISTS `placeables` (`ID` INT AUTO_INCREMENT PRIMARY KEY, `P` TEXT , `W` TEXT, `X` INT, `Y` INT, `Z` INT, `I` BOOL)");
 		loadAll();
 	}
 	
 	public static void insertBlock(String name, Block b, boolean interacted) throws SQLException {
 		db.query("INSERT INTO `placeables` (`P`, `W`, `X`, `Y`, `Z`, `I`)" +
 				" VALUES('"+name+"', '"+b.getWorld().getName()+"', "+b.getX()+", "+b.getY()+", "+b.getZ()+", "+(interacted?"1":"0")+")");
 		ResultSet re = db.query("SELECT `ID` FROM `placeables`" +
 				" WHERE `W`='"+b.getWorld().getName()+
 				"' AND `X`="+b.getX()+
 				" AND `Y`="+b.getY()+
 				" AND `Z`="+b.getZ());
 		re.next();
 		blocks.put(b, new PlaceableData(re.getRow(), name, b.getX(), b.getY(), b.getZ(), b.getWorld().getName(), interacted));
 		re.close();
 	}
 	
 	public static void updateBlock(PlaceableData pd, boolean insert) throws SQLException{
 		updateBlock(pd.getOwner(), pd.getLoc().getBlock(), pd.isInteracted(), insert);
 	}
 	
 	public static void updateBlock(String name, Block b, boolean interacted, boolean insert) throws SQLException{
 		if(!blocks.containsKey(b)&&insert){
 			insertBlock(name, b, interacted);
 		}
 		db.query("UPDATE `placeables` SET" +
 				" `P`='"+name+"'," +
 				" `I`="+(interacted?1:0)+"" +
 				" WHERE "+
 				" `W`='"+b.getWorld().getName()+"' AND " +
 				" `X`="+b.getX()+" AND " +
 				" `Y`="+b.getY()+" AND " +
 				" `Z`="+b.getZ());
 	}
 	
 	public static void deleteBlock(Block b){
 		if(!blocks.containsKey(b)) return;
 		db.query("DELETE FROM `placeables` WHERE" +
 				" `W`='"+b.getWorld().getName()+"' AND " +
 				" `X`="+b.getX()+" AND " +
 				" `Y`="+b.getY()+" AND " +
 				" `Z`="+b.getZ());
 		blocks.remove(b);
 	}
 //	TODO Get the primary key working
 //	public static PlaceableData getDataOf(Block b) throws SQLException {
 //		ResultSet re = db.query("SELECT * FROM `placeables` WHERE `ID`="+blocks.get(b));
 //		re.next();
 //		PlaceableData p = new PlaceableData(re.getRow(), re.getString(2), re.getInt(4), re.getInt(5), re.getInt(6), re.getString(3), re.getBoolean(7));
 //		re.close();
 //		return p;
 //	}
 //	
 //	public static PlaceableData getDataOf(int id) throws SQLException {
 //		ResultSet re = db.query("SELECT * FROM `placeables` WHERE `ID`="+id);
 //		re.next();
 //		PlaceableData p = new PlaceableData(re.getRow(), re.getString(2), re.getInt(4), re.getInt(5), re.getInt(6), re.getString(3), re.getBoolean(7));
 //		re.close();
 //		return p;
 //	}
 	
 	@Deprecated
 	public static int getID(Block b) {
 		return blocks.get(b).getId();
 	}
 	
 	public static PlaceableData getData(Block b) {
 		return blocks.get(b);
 	}
 	
 	private static void startTriggers(PlaceableData pd) {
 	   Block block = pd.getLoc().getBlock();
        if(ExplosiveUtils.isPlaceable(block)){
    		System.out.print("check 1");
     	   Placeable p = ExplosiveUtils.getPlaceable(block);
     	    @SuppressWarnings("unchecked")
 			List<ExplosivesTrigger> triggers = ((ArrayList<ExplosivesTrigger>)p.getProperty("TRIGGERS"));
        		for(ExplosivesTrigger t : triggers){
        			ExplosiveTriggerType et = (ExplosiveTriggerType)t.getTriggerType();
        			if(et.getTriggerActivationType().equals(TriggerActivationType.ONPLACE)||et.getTriggerActivationType().equals(TriggerActivationType.ONINTERACT)&&pd.isInteracted()){
        				System.out.print("check 3");
        				switch(et){
        					case DETONATOR:
        						break;
        					case TIME:
        						TriggerListener.registerDelayedTrigger(block, t.activate(p, null, null, block));
        						break;
        					default:
        						t.activate(p, null, block);
        						break;
        				}
        			}
        		}
        }
 	}
 }
