 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class CSData {
 	private ChestSync cs;
 	private CSStorage CSS;
 	private HashMap<String, String[]> LinkOwners = new HashMap<String, String[]>();
 	private HashMap<String, Inventory[]> Inventories = new HashMap<String, Inventory[]>();
 	private ArrayList<Inventory> openInv = new ArrayList<Inventory>();
 	
 	private String InvInfo = "Chest6 %s 7= X:6 %s 7Y: 6%s 7Z:6 %s 7World:6 %s";
 	private String PropsLoc = "plugins/config/ChestSync/ChestSync.ini";
 	
 	private PropertiesFile Props;
 	
 	protected boolean MySQL = false, CMySQL = false;
 	protected String DataBase = "jdbc:mysql://localhost:3306/minecraft", UserName = "root", Password = "root", Drive = "com.mysql.jdbc.Driver";
 	
 	public CSData(ChestSync cs){
 		this.cs = cs;
 		File Dir = new File("plugin/config/ChestSync/");
 		File PropFile = new File(PropsLoc);
 		
 		if(!Dir.isDirectory()){
 			Dir.mkdirs();
 		}
 		
 		if(!PropFile.exists()){
 			Props = new PropertiesFile(PropsLoc);
 			Props.setBoolean("Use-MySQL", MySQL);
 			Props.setBoolean("Use-CanaryMySQLConn", CMySQL);
 			Props.setString("MySQLDataBase", DataBase);
 			Props.setString("MySQLUserName", UserName);
 			Props.setString("MySQLPassword", Password);
 			Props.setString("MySQLDriver", Drive);
 		}
 		
 		if(Props == null){
 			Props = new PropertiesFile(PropsLoc);
 		}
 		
 		loadProperties();
 		
 		if(CMySQL || MySQL){
 			CSS = new CSMySQL(this, this.cs);
 		}
 		else{
 			CSS = new CSFlat(this, this.cs);
 		}
 		
 		CSS.loaddata();
 	}
 	
 	private void loadProperties(){
 		if(Props.containsKey("Use-MySQL")){
 			MySQL = Props.getBoolean("Use-MySQL");
 		}
 		if(Props.containsKey("Use-CanaryMySQLConn")){
 			CMySQL = Props.getBoolean("Use-CanaryMySQLConn");
 		}
 		if(!CMySQL && MySQL){
 			if(Props.containsKey("MySQLDriver")){
 				Drive = Props.getString("MySQLDriver").trim();
 				if(Drive.equals("") || Drive.equals(" ")){
 					cs.log.warning("[ChestSync] - Unable to set MySQL Driver! Disabling MySQL");
 					MySQL = false;
 					return;
 				}
 				Class<?> driver = null;
 				try{
 					driver = Class.forName(Drive);
 				}catch(ClassNotFoundException CNFE){
 					cs.log.warning("[ChestSync] - Unable to set MySQL Driver! Disabling MySQL");
 					MySQL = false;
 					return;
 				}
 				if(driver != null){
 					if(Props.containsKey("MySQLDataBase")){
 						DataBase = Props.getString("MySQLDataBase").trim();
 					}
 					if(DataBase.equals("") || DataBase.equals(" ")){
 						cs.log.warning("[ChestSync] - Unable to set MySQL DataBase! Disabling MySQL");
 						MySQL = false;
 						return;
 					}
 					if(Props.containsKey("MySQLUserName")){
 						UserName = Props.getString("MySQLUserName").trim();
 					}
 					if(UserName.equals("") || UserName.equals(" ")){
 						cs.log.warning("[ChestSync] - Unable to set MySQL UserName! Disabling MySQL");
 						MySQL = false;
 						return;
 					}
 					if(Props.containsKey("MySQLPassword")){
						Password = Props.getString("MySQLPassword").trim();
 					}
 					if(Password.equals("") || Password.equals(" ")){
 						cs.log.warning("[ChestSync] - Unable to set MySQL Password! Disabling MySQL");
 						MySQL = false;
 						return;
 					}
 					try{
 						Connection conn = DriverManager.getConnection(DataBase, UserName, Password);
 						if(conn != null){
 							conn.close();
 						}
 					}catch(SQLException SQLE){
 						cs.log.warning("[ChestSync] - Unable to set MySQL Connection! Disabling MySQL");
 						MySQL = false;
 						return;
 					}
 				}
 			}
 		}
 	}
 	
 	public void callSave(ChestSync cs){
 		CSS.savedata();
 	}
 	
 	public void addLink(String LinkName, String[] owners){
 		LinkOwners.put(LinkName, owners);
 	}
 	
 	public void addInvs(String LinkName, Inventory[] invs){
 		Inventories.put(LinkName, invs);
 	}
 	
 	public HashMap<String, String[]> getLink(){
 		return LinkOwners;
 	}
 	
 	public HashMap<String, Inventory[]> getInvs(){
 		return Inventories;
 	}
 	
 	private Inventory getInvA(Inventory inv){
 		Inventory get = null;
 		for(String key : Inventories.keySet()){
 			Inventory[] invcheck = Inventories.get(key);
 			if(invcheck[1] != null){
 				if(invcheck[1].hashCode() == inv.hashCode()){
 					get = invcheck[0];
 					break;
 				}
 			}
 		}
 		return get;
 	}
 	
 	private Inventory getInvB(Inventory inv){
 		Inventory get = null;
 		for(String key : Inventories.keySet()){
 			Inventory[] invcheck = Inventories.get(key);
 			if(invcheck[0] != null){
 				if(invcheck[0].hashCode() == inv.hashCode()){
 					get = invcheck[1];
 					break;
 				}
 			}
 		}
 		return get;
 	}
 	
 	private boolean isInvA(Inventory inv){
 		for(String key : Inventories.keySet()){
 			Inventory[] invcheck = Inventories.get(key);
 			if(invcheck[0] != null){
 				if(invcheck[0].hashCode() == inv.hashCode()){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public String getLinkName(Inventory inv){
 		String LinkName = "";
 		for(String key : Inventories.keySet()){
 			Inventory[] invcheck = Inventories.get(key);
 			if(invcheck[0] != null){
 				if(invcheck[0].hashCode() == inv.hashCode()){
 					LinkName = key;
 				}
 			}
 			if(invcheck[1] != null){
 				if(invcheck[1].hashCode() == inv.hashCode()){
 					LinkName = key;
 				}
 			}
 		}
 		return LinkName;
 	}
 	
 	public boolean isChestSyncInv(Inventory inv){
 		for(String key : Inventories.keySet()){
 			Inventory[] invcheck = Inventories.get(key);
 			if(invcheck[0] != null){
 				if(invcheck[0].hashCode() == inv.hashCode()){
 					return true;
 				}
 			}
 			if(invcheck[1] != null){
 				if(invcheck[1].hashCode() == inv.hashCode()){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean isOwner(Player player, Inventory inv){
 		String LinkName = null;
 		if(player.canUseCommand("/csadmin")){
 			return true;
 		}
 		for(String key : Inventories.keySet()){
 			Inventory[] invs = Inventories.get(key);
 			if(invs[0].hashCode() == inv.hashCode() || invs[1].hashCode() == inv.hashCode()){
 				LinkName = key;
 				break;
 			}
 		}
 		if(LinkOwners.containsKey(LinkName)){
 			String[] owners = LinkOwners.get(LinkName);
 			for(String own : owners){
 				if(own.equals(player.getName())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean isOwner(Player player, String LinkName){
 		if(player.canUseCommand("/csadmin")){
 			return true;
 		}
 		if(LinkOwners.containsKey(LinkName)){
 			String[] owners = LinkOwners.get(LinkName);
 			for(String own : owners){
 				if(own.equals(player.getName())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean isOpen(Inventory inv){
 		if(openInv.contains(inv)){
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isOtherOpen(Inventory inv){
 		Inventory invb = getInvA(inv);
 		if(invb != null){
 			return openInv.contains(invb);
 		}
 		invb = getInvB(inv);
 		if(invb != null){
 			return openInv.contains(invb);
 		}
 		return false;
 	}
 	
 	public void addOpenInv(Inventory inv){
 		openInv.add(inv);
 	}
 	
 	public void removeOpenInv(Inventory inv){
 		if(openInv.contains(inv)){
 			openInv.remove(inv);
 		}
 	}
 	
 	public void SyncChests(Inventory inv){
 		Inventory inv2;
 		if(isInvA(inv)){
 			inv2 = getInvB(inv);
 		}
 		else{
 			inv2 = getInvA(inv);
 		}
 		if(inv2 != null){
 			Chest chest = (Chest)inv2;
 			loadChunk(chest.getBlock());
 			inv2.clearContents();
 			inv2.setContents(inv.getContents());
 		}
 	}
 	
 	public boolean LinkInUse(String LinkName){
 		return LinkOwners.containsKey(LinkName);
 	}
 	
 	public boolean LinkComplete(String LinkName){
 		if(Inventories.containsKey(LinkName)){
 			Inventory[] invs = Inventories.get(LinkName);
 			if(invs[0] != null && invs[1] != null){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void createLink(Player player, Inventory inv, String LinkName){
 		LinkOwners.put(LinkName, new String[]{player.getName()});
 		Inventories.put(LinkName, new Inventory[]{inv, null});
 	}
 	
 	public void addInv(Player player, Inventory inv, String LinkName){
 		if(Inventories.containsKey(LinkName)){
 			Inventory[] invs = Inventories.get(LinkName);
 			if(invs[0] == null){
 				invs[0] = inv;
 				inv.setContents(invs[1].getContents());
 			}
 			else{
 				invs[1] = inv;
 				inv.setContents(invs[0].getContents());
 			}
 			
 			Inventories.put(LinkName, invs);
 		}
 	}
 	
 	public void clearInv(Player player, Inventory inv){
 		String link = getLinkName(inv);
 		Inventory[] invs;
 		if(isInvA(inv)){
 			Inventory invb = getInvB(inv);
 			if(invb != null){
 				inv.clearContents();
 				invs = Inventories.get(link);
 				invs[0] = null;
 				Inventories.put(link, invs);
 			}
 			else{
 				Inventories.remove(link);
 				LinkOwners.remove(link);
 			}
 		}
 		else{
 			Inventory inva = getInvA(inv);
 			if(inva != null){
 				inv.clearContents();
 				invs = Inventories.get(link);
 				invs[1] = null;
 				Inventories.put(link, invs);
 			}
 			else{
 				Inventories.remove(link);
 				LinkOwners.remove(link);
 			}
 		}
 	}
 	
 	private void loadChunk(Block block){
 		block.getWorld().loadChunk(block);
 	}
 	
 	public void getInfo(Player player, Inventory inv){
 		int Ax, Ay, Az, Bx, By, Bz;
 		String Aworld, Bworld;
 		Chest chestA = null, chestB = null;
 		if(isInvA(inv)){
 			chestA = (Chest)inv;
 			chestB = (Chest)getInvB(inv);
 		}
 		else{
 			chestA = (Chest)getInvA(inv);
 			chestB = (Chest)inv;
 		}
 		
 		if(chestA != null){
 			Ax = chestA.getX();
 			Ay = chestA.getY();
 			Az = chestA.getZ();
 			Aworld = chestA.getWorld().getType().name();
 			player.sendMessage("b"+String.format(InvInfo, "A", String.valueOf(Ax), String.valueOf(Ay), String.valueOf(Az), Aworld));
 		}
 		else{
 			player.sendMessage("bChest 6A b= cNot Set");
 		}
 		
 		if(chestB != null){
 			Bx = chestB.getX();
 			By = chestB.getY();
 			Bz = chestB.getZ();
 			Bworld = chestB.getWorld().getType().name();
 			player.sendMessage("b"+String.format(InvInfo, "B", String.valueOf(Bx), String.valueOf(By), String.valueOf(Bz), Bworld));
 		}
 		else{
 			player.sendMessage("bChest 6B b= cNot Set");
 		}
 	}
 }
