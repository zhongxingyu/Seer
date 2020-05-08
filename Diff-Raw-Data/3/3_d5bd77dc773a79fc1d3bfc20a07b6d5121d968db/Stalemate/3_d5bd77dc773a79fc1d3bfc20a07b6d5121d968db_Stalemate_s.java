 package plugin;
 
 import java.io.*;
 import static util.ColorParser.parseColors;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import net.minecraft.server.v1_5_R3.Block;
 import net.minecraft.server.v1_5_R3.Item;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import org.w3c.dom.NodeList;
 
 import amendedclasses.LoggerOutputStream;
 
 import controllers.RoundController;
 import defense.PlayerAI;
 import defense.Team;
 
 import serial.MapArea;
 import serial.RoundConfiguration;
 import serial.SerializableItemStack;
 import soldier.SoldierClass;
 import java.util.*;
 public class Stalemate extends JavaPlugin implements Listener {
 	private Map<String, List<ItemStack>> itempacks = new ConcurrentHashMap<String, List<ItemStack>>();
 	public Map<String, String> settings = new ConcurrentHashMap<String, String>();
 	private static Stalemate instance;
 	public final Map<String, PlayerAI> aiMap = new ConcurrentHashMap<String, PlayerAI>();
 	private List<RoundController> rounds = new Vector<RoundController>();
 	private Map<String, CommandCallback> cmdMap = new ConcurrentHashMap<String, CommandCallback>();
 	private Map<String, String> helpMap = new ConcurrentHashMap<String, String>();
 	private Map<String, TrapCallback> trapMap = new ConcurrentHashMap<String, TrapCallback>();
 	public final Map<Location, String> placedTraps = new ConcurrentHashMap<Location, String>();
 	public String getSetting(String key, String def)
 	{
 		String val = settings.get(key);
 		if (val == null) val = def;
 		return val;
 	}
 	private static TrapCallback createCallbackFromXML(Element e)
 	{
 		final List<TrapCallback> tasks = new Vector<TrapCallback>(); 
 		NodeList stuff = e.getChildNodes();
 		for (int i = 0; i < stuff.getLength(); i++)
 		{
 			Node n = stuff.item(i);
 			if (!(n instanceof Element))
 				continue;
 			Element task = (Element) n;
 			String name = task.getNodeName();
 			switch (name.toLowerCase()) {
 				case "explosion":
 					tasks.add(new ExplosiveTrapCallback(task));
 					break;
 				case "kill":
 					tasks.add(new KillPlayerCallback(task));
 					break;
 				case "sleep":
 					tasks.add(new SleepTrapCallback(task));
 					break;
 				case "command":
 					tasks.add(new CommandTrapCallback(task));
 					break;
 				default:
 					System.out.println("Trap Callbacks: Unrecognized tag: "+name);
 			}
 		}
 		String reusables = e.getAttribute("reusable");
 		boolean reusable = true;
 		if (!reusables.equals("true"))
 		{
 			reusable = false;
 		}
 		final boolean reusableF = reusable;
 		return new TrapCallback() {
 
 			@Override
 			public void onTriggered(Player p, Location loc, RoundController rnd) {
 				for (TrapCallback c : tasks)
 					try {
 						c.onTriggered(p, loc, rnd);
 					} catch (Throwable e) {
 						RuntimeException e1 = new RuntimeException(Stalemate.getInstance().getSetting("trap_exc_msg", "Exception when triggering trap."));
 						e1.initCause(e);
 						throw e1;
 					}
 			}
 
 			@Override
 			public boolean isReusable() {
 				return reusableF;
 			}
 		};
 	}
 	public static Stalemate getInstance()
 	{
 		return instance;
 	}
 	private static class AsyncTask {
 		public long timeToWait;
 		public long lastTimeUpdated;
 		public final Callable<?> call;
 		public AsyncTask(long t, Callable<?> c) {
 			timeToWait = t;
 			call = c;
 			lastTimeUpdated = System.nanoTime()/1000000;
 		}
 	}
 	public Map<String, TrapCallback> getTrapCallbacks()
 	{
 		return trapMap;
 	}
 	private List<AsyncTask> tasks = new Vector<AsyncTask>();
 	private Thread asyncUpdateThread = new Thread() {
 		public void run() {
 			while (Stalemate.getInstance().isEnabled())
 			{
 				for (AsyncTask t : tasks)
 				{
 					t.timeToWait += -(t.lastTimeUpdated-(t.lastTimeUpdated = System.nanoTime()/1000000));
 					if (t.timeToWait <= 0)
 					{
 						tasks.remove(t);
 						try {
 							t.call.call();
 						} catch (Exception e) {
 							throw new RuntimeException(e);
 						}
 					}
 				}
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	};
 	
 	public void setTimeout(long timeToWait, Callable<?> callback)
 	{
 		tasks.add(new AsyncTask(timeToWait, callback));
 	} 
 	
 	public Stalemate()
 	{
 		instance = this;
 	}
 	
 	private void join(Team t, Player p)
 	{
 		for (Team tt : Team.list())
 		{
 			if (Arrays.asList(tt.getPlayers()).contains(p.getName().toUpperCase()))
 				tt.removePlayer(p.getName().toUpperCase());
 		}
 		t.addPlayer(p.getName());
 		for (RoundController rc : rounds)
 		{
 			if (rc.getConfig().getParticipatingTeams().contains(t))
 			{
 				p.teleport(rc.getConfig().getArea().randomLoc());
 			}
 		}
 	}
 	
 	private void initRound(RoundController rnd)
 	{
 		rounds.add(rnd);
 		rnd.startRound();
 	}
 	
 	private void placeTrap(String type, Location loc)
 	{
 		for (RoundController rc : rounds)
 		{
 			MapArea a = rc.getConfig().getArea();
 			if (a.contains(loc))
 			{
 				rc.placeTrap(type, loc);
 				break;
 			}
 		}
 	}
 	private Set<String> noChange = new HashSet<String>();
 	private Map<String, List<ItemStack>> itemsAccountableMap = new ConcurrentHashMap<String, List<ItemStack>>();
 	public void onEnable()
 	{
 		// TODO: Implement all commands
 		registerCommand(getSetting("help_cmd", "help"), getSetting("help_help", "Shows the help page."), new CommandCallback() {
 
 			@Override
 			public void onCommand(CommandSender sender, String[] args) {
 				for (String key : helpMap.keySet())
 				{
 					sender.sendMessage(synColor(ChatColor.LIGHT_PURPLE)+"/stalemate "+synColor(ChatColor.RESET)+key+" - "+synColor(ChatColor.AQUA)+helpMap.get(key));
 				}
 			}
 			
 		});
 		registerCommand(getSetting("remove_trap_cmd", "rtrap"), getSetting("remove_trap_help", "Defuses a trap. Has a chance to fail."), new CommandCallback() {
 
 			@Override
 			public void onCommand(CommandSender sender, String[] args) {
 				if (!(sender instanceof Player))
 				{
 					sender.sendMessage(parseColors(getSetting("players_only_msg", "Only players may use this command.")));
 					return;
 				}
 				// TODO: Finish
 			}
 			
 		});
 		registerCommand(getSetting("join_war_cmd", "joinwar"), getSetting("join_war_help", "Joins a war"), new CommandCallback() {
 
 			@Override
 			public void onCommand(CommandSender sender, String[] args) {
 				if (!sender.hasPermission(getSetting("join_war_perm", "stalemate.joinwar")))
 				{
 					sender.sendMessage(parseColors(getSetting("no_perm_msg", "<xml><font color=\"Red\">You do not have permission to do that.</font></xml>")));
 					return;
 				}
 				if (args.length < 2)
 				{
 					sender.sendMessage(parseColors(getSetting("join_war_syntax", "Syntax: /"+args[0]+" &lt;teamname&gt;")));
 					return;
 				}
 				Team t = Team.getTeam(args[1]);
 				Team yours = null;
 				for (Team te : Team.list())
 				{
 					if (te.containsPlayer(sender.getName().toUpperCase()))
 					{
 						yours = te;
 					}
 				}
 				if (yours == null)
 				{
 					sender.sendMessage(parseColors(getSetting("err_no_team_msg", "<root><font color=\"Red\">Join a team first.</font></root>")));
 					return;
 				}
 				if (!sender.getName().equalsIgnoreCase(yours.getOwner()))
 				{
 					sender.sendMessage(parseColors(getSetting("no_perm_msg", "<xml><font color=\"Red\">You do not have permission to do that!</font></xml>")));
 					return;
 				}
 				for (RoundController rc : rounds)
 				{
 					if (rc.getConfig().getParticipatingTeams().contains(t) && rc.isJoinable())
 					{
 						rc.getConfig().addTeam(yours);
 					}
 				}
 			}
 			
 		});
 		registerCommand(getSetting("war_cmd", "war"), getSetting("war_help", "Starts a new war"),new CommandCallback() {
 
 			@Override
 			public void onCommand(final CommandSender sender, String[] args) {
 				if (!sender.hasPermission(getSetting("war_perm", "stalemate.start")))
 				{
 					sender.sendMessage(getSetting("no_perm_msg", "<xml><font color=\"Red\"> You do not have permission to do that.</font></xml>"));
 					return;
 				}
 				RoundController rnd = new RoundController(new RoundConfiguration((Location) null, null), new Callable<Object>(){
 
 					@Override
 					public Object call() throws Exception {
 						for (RoundController r : rounds) {
 							if (r.getPlayers().contains(sender.getName().toUpperCase()))
 							{
 								for (String p : r.getPlayers())
 								{
 									List<ItemStack> acct = Stalemate.this.itemsAccountableMap.get(p.toUpperCase());
 									Player pl = Stalemate.this.getServer().getPlayer(p);
 									Map<Integer, ItemStack> failed = pl.getInventory().removeItem(acct.toArray(new ItemStack[0]));
 									acct.clear();
 									acct.addAll(failed.values());
 									noChange.remove(p.toUpperCase());
 								}
 								break;
 							}
 						}
 						return null;
 					}});
 				initRound(rnd);
 				Team yours = null;
 				for (Team t : Team.list())
 				{
 					if (t.containsPlayer(sender.getName()))
 					{
 						yours = t;
 						break;
 					}
 				}
 				if (!yours.getOwner().equalsIgnoreCase(sender.getName()))
 				{
 					sender.sendMessage(parseColors(getSetting("no_perm_msg", "<xml><font color=\"Red\">You do not have permission to do that!</font></xml>")));
 					return;
 				}
 				rnd.getConfig().addTeam(yours);
 			}
 			
 		});
 		registerCommand(getSetting("place_trap_cmd", "ptrap"), getSetting("place_trap_help", "Places a new trap or removes one if already here."), new CommandCallback() {
 
 			@Override
 			public void onCommand(CommandSender sender, String[] args) {
 				assert(args.length > 0);
 				if (!sender.hasPermission(getSetting("place_trap_perm", "stalemate.ptrap")))
 				{
 					sender.sendMessage(parseColors(getSetting("no_perm_msg", "<xml><font color=\"Red\">You do not have permission to do that!</font></xml>")));
 					return;
 				}
 				if (!(sender instanceof Player))
 				{
 					if (args.length < 6)
 					{
 						sender.sendMessage(getSetting("syntax_trap_console_msg", "Syntax: "+args[0]+" <world> <x> <y> <z>"));
 						return;
 					}
 					World w;
 					int x, y, z;
 					String trapName;
 					try {
 						w = getServer().getWorld(args[1]);
 						if (w == null) {
 							sender.sendMessage(getSetting("invworld_trap_console_msg", "Invalid World."));
 							return;
 						}
 						x = Integer.parseInt(args[2]);
 						y = Integer.parseInt(args[3]);
 						z = Integer.parseInt(args[4]);
 						trapName = args[5];
 					} catch (NumberFormatException e) {
 						sender.sendMessage(getSetting("integer_err_console", "Please provide integers for x, y, and z."));
 						return;
 					}
 					placeTrap(trapName, new Location(w, x, y, z));
 					sender.sendMessage(getSetting("trap_place_success_msg_console", "Trap Placed."));
 				} else {
 					if (args.length < 2)
 					{
 						sender.sendMessage(parseColors(getSetting("syntax_trap_player_msg", "Syntax: /"+args[0]+" <trap_type>")));
 						return;
 					}
 					Player p = (Player) sender;
 					String trapName = args[1];
 					placeTrap(trapName, p.getLocation());
 					sender.sendMessage(parseColors(getSetting("trap_place_success_msg", "Trap Placed.")));
 				}
 			}
 			
 		});
 		registerCommand(getSetting("join_cmd", "join"), getSetting("join_help", "Joins the team of the specified player."), new CommandCallback() {
 
 			@Override
 			public void onCommand(CommandSender sender, String[] args) {
 				if (!sender.hasPermission(getSetting("join_perm", "stalemate.join")))
 				{
 					sender.sendMessage(parseColors(getSetting("no_perm_msg", "<xml><font color=\"Red\">You do not have permission to do that!</font></xml>")));
 					return;
 				}
 				if (args.length < 2) {
 					sender.sendMessage(parseColors(getSetting("syntax_join_msg", "/stalemate "+args[0]+" <player> or to create a team: /stalemate "+args[0]+" <teamname>")));
 					Team yours = null;
 					for (Team tt : Team.list())
 					{
 						if (tt.containsPlayer(sender.getName()))
 						{
 							yours = tt;
 							break;
 						}
 					}
 					sender.sendMessage("Your current team is: "+yours.getName());
 					return;
 				}
 				Player target = getServer().getPlayer(args[1]);
 				if (!(sender instanceof Player))
 				{
 					sender.sendMessage(getSetting("players_only_msg", "Only players may execute this command."));
 					return;
 				}
 				for (Team t : Team.list())
 				{
 					// TODO: Use linear search or hashing
 					if (Arrays.binarySearch(t.getPlayers(), target.getName().toUpperCase()) > 0)
 					{
 						join(t, (Player) sender);
 						sender.sendMessage(parseColors(getSetting("join_success_msg", "Success.")));
 						return;
 					}
 				}
 				Team x = new Team(args[1]);
 				join(x, (Player) sender);
 			}
 			
 		});
 		registerCommand(getSetting("leave_cmd", "leave"), getSetting("leave_help", "Leaves the current round."), new CommandCallback() {
 
 			@Override
 			public void onCommand(CommandSender sender, String[] args) {
 				if (!sender.hasPermission(getSetting("leave_perm", "stalemate.leave")))
 				{
 					sender.sendMessage(parseColors(getSetting("no_perm_msg", "<xml><font color=\"Red\">You do not have permission to do that!</font></xml>")));
 					return;
 				}
 				if (!(sender instanceof Player))
 				{
 					sender.sendMessage(getSetting("players_only_msg", "Only players may use this feature."));
 					return;
 				}
 				for (RoundController rc : rounds)
 				{
 					if (rc.getPlayers().contains(sender.getName().toUpperCase()))
 					{
 						List<Team> teams = rc.getConfig().getParticipatingTeams();
 						for (Team t : teams)
 							if (t.removePlayer(sender.getName())) break;
 					}
 				}
 			}
 			
 		});
 		registerCommand(getSetting("class_cmd", "class"), getSetting("class_help", "Changes your class (Gives you a class's items)"), new CommandCallback() {
 
 			@Override
 			public void onCommand(CommandSender sender, String[] args) {
 				if (!(sender instanceof Player))
 				{
 					sender.sendMessage(getSetting("players_only_msg", "Only players may use this command."));
 					return;
 				}
 				
 				if (!sender.hasPermission(getSetting("class_perm", "stalemate.changeclass")))
 				{
 					sender.sendMessage(parseColors(getSetting("no_perm_msg", "<xml><font color=\"Red\">You do not have permission to do that!</font></xml>")));
 					return;
 				}
 				
 				if (noChange.contains(sender.getName().toUpperCase()) && !sender.hasPermission("stalemate.changeclass.multiple"))
 				{
 					sender.sendMessage(parseColors(getSetting("class_nochange", "You may not change your class once you have selected it.")));
 					return;
 				}
 				if (args.length < 2)
 				{
 					sender.sendMessage(parseColors(getSetting("class_syntax", "Syntax: /"+args[0]+" <classname>")));
 					return;
 				}
 				SoldierClass s = SoldierClass.fromName(args[1]);
 				if (!sender.hasPermission(s.getPermission()))
 				{
 					sender.sendMessage(parseColors(getSetting("no_perm_msg", "You do not have permission to change to the requested class.")));
 					return;
 				}
 				ItemStack[] give = s.getItems();
 				List<ItemStack> account = (account=itemsAccountableMap.get(sender.getName().toUpperCase())) == null ? account == itemsAccountableMap.put(sender.getName().toUpperCase(), account = new Vector<ItemStack>()) ? account : account : account;
 				for (ItemStack i : give)
 					account.add(i.clone());
 				Map<Integer, ItemStack> failed = ((Player) sender).getInventory().addItem(give);
 				account.removeAll(failed.values());
 			}
 			
 		});
 		try {
 			onEnable0();
 		} catch (Throwable e) {
 			getLogger().log(Level.SEVERE, "Unexpected exception while loading Stalemate.");
 			e.printStackTrace();
 		}
 		asyncUpdateThread.start();
 		getLogger().info("Wars? Battles? Skirmishes? Fistfights? Air punching? Stalemate loaded!");
 	}
 	
 	protected static boolean isInRound(Player sender) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	public CommandCallback registerCommand(String name, String desc, CommandCallback onCommand)
 	{
 		CommandCallback c = cmdMap.put(name.toUpperCase(), onCommand);
 		helpMap.put(name.toUpperCase(), desc);
 		return c;
 	}
 	
 	public void onDisable()
 	{
 		getLogger().info("Peace! AAAAH!");
 		Map<String, List<SerializableItemStack>> saveAccount = new HashMap<String, List<SerializableItemStack>>();
 		for (String s : itemsAccountableMap.keySet()) {
 			List<SerializableItemStack> l = SerializableItemStack.fromList(itemsAccountableMap.get(s));
 			saveAccount.put(s, l);
 		}
 		try {
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(getDataFolder(), "items.dat")));
 			oos.writeObject(saveAccount);
 			oos.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String synColor(ChatColor c)
 	{
 		return ChatColor.COLOR_CHAR+""+c.getChar();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String args[])
 	{
 		if (!cmd.getName().equalsIgnoreCase("stalemate")) return false;
 		if (!sender.hasPermission("stalemate.basic"))
 		{
 			sender.sendMessage(parseColors(getSetting("no_perm_msg", "<xml><font color=\"Red\">You do not have permission.</font></xml>")));
 			return true;
 		}
 		if (args.length == 0)
 		{
 			args = new String[] {"help"};
 		}
 		String name = args[0];
 		CommandCallback cb = cmdMap.get(name);
 		if (cb == null)
 		{
 			sender.sendMessage(parseColors(getSetting("invalid_cmd_msg", "Invalid Command. Please type /stalemate "+getSetting("help_cmd", "help")+" for help.")));
 			return false;
 		}
 		cb.onCommand(sender, Arrays.asList(name, args).toArray(new String[0]));
 		return true;
 	}
 	
 	private void generateConfig(File f) throws IOException
 	{
 		// Writing lines to translate CRLF -> LF and LF -> CRLF
 		PrintWriter writer = new PrintWriter(new FileOutputStream(f));
 		InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("config.xml");
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		String ln;
 		while ((ln = br.readLine()) != null)
 			writer.println(ln);
 		writer.close();
 		br.close();
 	}
 	
 	public void onEnable0() throws Throwable
 	{
 		File config = new File(this.getDataFolder(), "config.xml");
 		if (!config.exists())
 		{
 			generateConfig(config);
 		}
 		if (!config.isFile())
 		{
 			boolean success = config.delete();
 			if (!success)
 			{
 				config.deleteOnExit();
 				throw new RuntimeException("Failed to create config.");
 			} else {
 				generateConfig(config);
 			}
 		}
 		FileInputStream stream = new FileInputStream(config);
 		// Buffer the file in memory
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		byte[] buf = new byte[1024];
 		int bytesRead;
 		while ((bytesRead = stream.read(buf)) != -1)
 		{
 			b.write(buf, 0, bytesRead);
 		}
 		stream.close();
 		buf = null;
 		byte[] bytes = b.toByteArray();
 		b.close();
 		// Begin parsing
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = dbf.newDocumentBuilder();
 		Document doc;
 		try {
 			doc = builder.parse(new ByteArrayInputStream(bytes));
 		} catch (Throwable t)
 		{
 			Throwable exc = new RuntimeException("Config Error: Invalid XML File: config.xml");
 			exc.initCause(t);
 			throw exc;
 		}
 		Element root = doc.getDocumentElement();
 		NodeList classes = root.getElementsByTagName("soldiers");
 		for (int i = 0; i < classes.getLength(); i++)
 		{
 			Node nn = classes.item(i);
 			if (!(nn instanceof Element)) continue;
 			Element section = (Element) nn;
 			// Load item packs
 			NodeList nodes = section.getElementsByTagName("itempack");
 			for (int j = 0; j < nodes.getLength(); j++)
 			{
 				Node nnn = nodes.item(j);
 				if (!(nnn instanceof Element)) continue;
 				Element pack = (Element) nnn;
 				NodeList items = pack.getElementsByTagName("item");
 				List<ItemStack> packItems = new Vector<ItemStack>();
 				for (int k = 0; k < items.getLength(); k++)
 				{
 					Node nnnn = items.item(k);
 					if (!(nnnn instanceof Element)) continue;
 					Element item = (Element) nnnn;
 					String idStr = item.getAttribute("id");
 					int id = -1;
 					if (idStr.equals(""))
 					{
 						// Fetch according to name attribute
 						String name = item.getAttribute("name");
 						for (Block block : Block.byId)
 						{
 							if (block.getName().equalsIgnoreCase(name))
 							{
 								id = block.id;
 								break;
 							}
 						}
 						if (id == -1) {
 							for (Item mcItem : Item.byId)
 							{
 								if (mcItem.getName().equalsIgnoreCase(name))
 								{
 									id = mcItem.id;
 									break;
 								}
 							}
 							if (id == -1)
 								throw new RuntimeException("Config Error: Non-existent name: "+name);
 						}
 					} else {
 						String name = item.getAttribute("name");
 						if (!name.equals("")) throw new RuntimeException("Both name and ID specified. Specify one or the other. Name: "+name+" ID: "+idStr);
 						try {
 							id = Integer.parseInt(idStr);
 						} catch (NumberFormatException e) {
 							throw new RuntimeException("Config Error: Expected a number for item ID. Got: "+idStr);
 						}
 					}
 					String dmgStr = item.getAttribute("dmg");
 					int dmg;
 					if (dmgStr.equals("")) {
 						dmg = 0;
 					} else {
 						try {
 							dmg = Integer.parseInt(dmgStr);
 						} catch (NumberFormatException e) {
 							throw new RuntimeException("Config Error: Expected an integer (-2147483648 -> 2147483647) for item damage. Got: "+dmgStr);
 						}
 					}
 					int num;
 					String numStr = item.getAttribute("num");
 					if (numStr.equals("")) {
 						num = 1;
 					} else {
 						try {
 							num = Integer.parseInt(numStr);
 							if (num <= 0) throw new NumberFormatException("break");
 						} catch (NumberFormatException e) {
 							throw new RuntimeException("Config Error: Expected a positive integer for item number. Got: "+numStr);
 						}
 					}
 					ItemStack stack = new ItemStack(id, num, (short) dmg);
 					packItems.add(stack);
 				}
 				if (pack.getAttribute("name").equals("")) throw new RuntimeException("Config Error: Item packs require a name attribute.");
 				itempacks.put(pack.getAttribute("name").toUpperCase(), packItems);
 			}
 			NodeList classList = section.getElementsByTagName("sclass");
 			for (int j = 0; j < classList.getLength(); j++)
 			{
 				Element classElement = (Element) classList.item(j);
 				String name = classElement.getAttribute("name");
 				String permission = classElement.getAttribute("permission");
 				NodeList itemList = classElement.getElementsByTagName("item");
 				List<ItemStack> classItems = new Vector<ItemStack>();
 				for (int k = 0; k < itemList.getLength(); k++)
 				{
 					Element item = (Element) itemList.item(k);
 					String n = item.getAttribute("name");
 					String isPackStr = item.getAttribute("isPack").toLowerCase();
 					boolean isPack;
 					if (isPackStr.equals(""))
 					{
 						isPack = false;
 					} else {
 						try {
 							isPack = Boolean.parseBoolean(isPackStr);
 						} catch (RuntimeException e) {
 							throw new RuntimeException("Config Error: Expected a true/false value for attribute isPack. Got: "+isPackStr);
 						}
 					}
 					if (n.equals("") || !isPack)
 					{
 						// Normal Item Processing
 						String idStr = item.getAttribute("id");
 						int id = -1;
 						if (!n.equals(""))
 						{
 							if (!idStr.equals(""))
 								throw new RuntimeException("Config Error: Name and ID specified. Please specify one or the other. Name: "+n+" ID: "+idStr);
 							for (Block b1 : Block.byId)
 							{
 								if (b1.getName().equalsIgnoreCase(n))
 								{
 									id = b1.id;
 									break;
 								}
 							}
 							if (id == -1)
 								throw new RuntimeException("Config Error: Non-existent name: "+n);
 						} else {
 							try {
 								id = Integer.parseInt(idStr);
 							} catch (NumberFormatException e) {
 								throw new RuntimeException("Config Error: ID must be a valid integer. Got: "+idStr);
 							}
 						}
 						int num;
 						String numStr = item.getAttribute("num");
 						if (numStr.equals(""))
 						{
 							num = 1;
 						} else {
 							try {
 								num = Integer.parseInt(numStr);
 							} catch (NumberFormatException e) {
 								throw new RuntimeException("Config Error: Expected an integer for item amount. Got: "+numStr);
 							}
 						}
 						int dmg;
 						String dmgStr = item.getAttribute("dmg");
 						if (dmgStr.equals(""))
 						{
 							dmg = 0;
 						} else {
 							try {
 								dmg = Integer.parseInt(dmgStr);
 							} catch (NumberFormatException e) {
 								throw new RuntimeException("Config Error: Expected an integer (-32768 -> 32767) for item damage. Got: "+dmgStr);
 							}
 						}
 						ItemStack stack = new ItemStack(id, num, (short) dmg);
 						classItems.add(stack);
 					} else {
 						// Fetch item pack and add in.
 						if (!itempacks.containsKey(n.toUpperCase()))
 							throw new RuntimeException("Config Error: Non-existent item pack: "+n);
 						classItems.addAll(itempacks.get(n.toUpperCase()));
 					}
 				}
 				new SoldierClass(name, classItems.toArray(new ItemStack[0]), permission);
 			}
 		}
 		// Load Settings
 		NodeList settings = root.getElementsByTagName("settings");
 		for (int i = 0; i < settings.getLength(); i++)
 		{
 			Node nnnn = settings.item(i);
 			Element section = (Element) nnnn;
 			NodeList tags = section.getElementsByTagName("setting");
 			for (int j = 0; j < tags.getLength(); j++)
 			{
 				Element setting = (Element) tags.item(j);
 				String name = setting.getAttribute("name");
 				String value = setting.getAttribute("value");
 				if (name.equals(""))
 					throw new RuntimeException("Please include a name attribute for all setting tags in config.xml");
 				this.settings.put(name, value);
 			}
 		}
 		// Load Traps
 		NodeList traps = root.getElementsByTagName("traps");
 		for (int i = 0; i < traps.getLength(); i++)
 		{
 			Element section = (Element) settings.item(i);
 			NodeList trapList = section.getElementsByTagName("trap");
 			for (int j = 0; j < trapList.getLength(); j++)
 			{
 				Element trap = (Element) trapList.item(j);
 				String name = trap.getAttribute("name");
 				if (name.equals("")) throw new RuntimeException("All traps must have a name.");
 				TrapCallback call = createCallbackFromXML(trap);
 				trapMap.put(name.toUpperCase(), call);
 			}
 		}
 		// Load accounts
 		File accountsFile = new File(getDataFolder(), "items.dat");
 		if (accountsFile.isDirectory())
 			accountsFile.delete();
 		if (!accountsFile.exists())
 		{
 			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(accountsFile));
 			out.writeObject(new HashMap<String, List<SerializableItemStack>>());
 			out.close();
 		}
 		try {
 			// TODO: Fix race condition, so that checking and opening is atomic
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream(accountsFile));
 			@SuppressWarnings("unchecked")
 			Map<String, List<SerializableItemStack>> read = (Map<String, List<SerializableItemStack>>) in.readObject();
 			in.close();
 			// Begin translation
 			for (String s : read.keySet())
 			{
 				itemsAccountableMap.put(s, SerializableItemStack.toList(read.get(s)));
 			}
 		} catch (Throwable t) {
 			accountsFile.delete();
 			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(accountsFile));
 			out.writeObject(new HashMap<String, List<SerializableItemStack>>());
 			out.close();
 			System.err.println("Please restart the server. A data file has been corrupted.");
 			throw new RuntimeException(t);
 		}
 	}
 
 }
