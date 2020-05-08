 package fghjconner.DonationMeter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class DonationMeter extends JavaPlugin
 {
 	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	private final DMBlockListener BlockListener = new DMBlockListener(this);
 	private final DMEntityListener EntityListener = new DMEntityListener(this);
 	private final DMServerListener ServerListener = new DMServerListener(this);
 	private final DMPlayerListener PlayerListener = new DMPlayerListener(this);
 	public static PermissionHandler permissionHandler;
 	private Logger log = Logger.getLogger("Minecraft");
 	public ArrayList<Meter> meterList;
 	public HashMap<String, Short> notificationList;
 	public ArrayList<String> vips;
 	public static DonationMeter plugin;
 	public static short requiredDonations, currentDonations;
 	public static String currency, vipName;
 	public boolean showTime, explosionVulnerable, opPermissions;
 	public static Server server;
 
 	//file IO stuff
 	static String mainDirectory = "plugins/DonationMeter"; //sets the main directory for easy reference
 	static File Meters = new File(mainDirectory + File.separator + "Meters.dat");
 	static File Donations = new File(mainDirectory + File.separator + "Donations.dat");
 	static File Notifications = new File(mainDirectory + File.separator + "Notifications.dat");
 	static Properties prop = new Properties(); //creates a new properties file
 	
 	public void onDisable()
 	{
 		saveAll();
 		plugin = null;
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled!" );
 	}
 
 	public void onEnable()
 	{
 		plugin = this;
 		server = getServer();
 		setupPermissions();
 		PluginManager pm = this.getServer().getPluginManager();
 
 		//registers main events
		pm.registerEvent(Event.Type.SIGN_CHANGE, BlockListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, BlockListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.BLOCK_BURN, BlockListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, ServerListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, PlayerListener, Event.Priority.Monitor, this);
 
 		//registers command
 		PluginCommand command = getCommand("DonationMeter");
 
 		command.setExecutor(new DonationsCommands(this));
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled!" );
 
 
 		//loads properties
 		new File(mainDirectory).mkdir();
 		if(!Meters.exists())
 			createMetersFile();
 
 		if(!Donations.exists())
 			createDonationsFile();
 		
 		if(!Notifications.exists())
 			createNotificationsFile();
 		
 		loadMeters();
 		loadNotifications();
 		loadDonations();
 		
 		//registers entity explode if explosion vulnerable is true
 		if (explosionVulnerable)
 			pm.registerEvent(Event.Type.ENTITY_EXPLODE, EntityListener, Event.Priority.Normal, this);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadMeters()
 	{
 		try
 		{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mainDirectory + File.separator + "Meters.dat"));
 			Object result = ois.readObject();
 			meterList = (ArrayList<Meter>)result;
 			ois.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void loadNotifications()
 	{
 		try
 		{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mainDirectory + File.separator + "Notifications.dat"));
 			Object result = ois.readObject();
 			notificationList = (HashMap<String, Short>)result;
 			ois.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void loadDonations()
 	{
 		
 		try
 		{
 			FileInputStream in = new FileInputStream(Donations);
 			prop.load(in);
 			requiredDonations = Short.parseShort(prop.getProperty("requiredDonations","0"));
 			currentDonations = Short.parseShort(prop.getProperty("currentDonations","0"));
 			currency = prop.getProperty("currency","dollars");
 			vipName = prop.getProperty("VIPname","VIP");
 			explosionVulnerable = prop.getProperty("explosionVulnerable","true").equals("true");
 			showTime = prop.getProperty("displayTime","false").equals("true");
 			loadVIPs(prop.getProperty("VIPs",""));
 			in.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void loadVIPs(String vipString)
 	{
 		vips = new ArrayList<String>();
 		Scanner vipList = new Scanner(vipString);
 		vipList.useDelimiter(",");
 		while (vipList.hasNext())
 		{
 			vips.add(vipList.next());
 		}
 	}
 
 	private void createNotificationsFile()
 	{
 		try
 		{
 			Notifications.createNewFile();
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mainDirectory + File.separator + "Notifications.dat"));
 			oos.writeObject(new HashMap<String,Short>());
 			oos.flush();
 			oos.close();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void createMetersFile()
 	{
 		try
 		{
 			Meters.createNewFile();
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mainDirectory + File.separator + "Meters.dat"));
 			oos.writeObject(new ArrayList<Meter>());
 			oos.flush();
 			oos.close();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void createDonationsFile()
 	{
 		try
 		{
 			Donations.createNewFile();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void saveDonationsFile()
 	{
 		try
 		{
 			FileOutputStream out = new FileOutputStream(Donations);
 			prop.setProperty("requiredDonations", Short.toString(requiredDonations));
 			prop.setProperty("currentDonations", Short.toString(currentDonations));
 			prop.setProperty("currency", currency);
 			prop.setProperty("displayTime", Boolean.toString(showTime));
 			prop.setProperty("VIPname", vipName);
 			prop.setProperty("VIPs", vipsToString());
 			prop.setProperty("explosionVulnerable", Boolean.toString(explosionVulnerable));
 			prop.store(out, "DonationMeters Config");
 			out.flush();
 			out.close();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private String vipsToString()
 	{
 		String out = "";
 		for (String name:vips)
 		{
 			out+=","+name;
 		}
 		return out;
 	}
 
 	private void saveMetersFile()
 	{
 		try{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mainDirectory + File.separator + "Meters.dat"));
 			oos.writeObject(meterList);
 			oos.flush();
 			oos.close();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	private void saveNotificationsFile()
 	{
 		try{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mainDirectory + File.separator + "Notifications.dat"));
 			oos.writeObject(notificationList);
 			oos.flush();
 			oos.close();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	public boolean isDebugging(final Player player)
 	{
 		if (debugees.containsKey(player)) {
 			return debugees.get(player);
 		} else {
 			return false;
 		}
 	}
 
 	public void setDebugging(final Player player, final boolean value)
 	{
 		debugees.put(player, value);
 	}
 
 	private void setupPermissions()
 	{
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 		if (DonationMeter.permissionHandler == null) {
 			if (permissionsPlugin != null)
 			{
 				DonationMeter.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 				opPermissions = true;
 			} else
 			{
 				log.info("Permission system not detected, defaulting to OP");
 				opPermissions = true;
 			}
 		}
 	}
 
 	//updates meters
 	public void updateMeters()
 	{
 		for (int i=0;i<meterList.size();i++)
 		{
 			if (!meterList.get(i).update())
 				i--;
 		}
 		saveMetersFile();
 	}
 
 	public void saveAll()
 	{
 		saveDonationsFile();
 		saveMetersFile();
 		saveNotificationsFile();
 	}
 
 	public void addNotification(short amount,String player)
 	{
 		if (notificationList.containsKey(player))
 			notificationList.put(player, (short)(notificationList.get(player)+amount));
 		else
 			notificationList.put(player, amount);
 	}
 }
