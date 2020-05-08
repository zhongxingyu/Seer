 package me.eccentric_nz.plugins.secretary;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import org.apache.commons.lang.WordUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Constants extends JavaPlugin {
 
 	public static String MY_PLUGIN_NAME;
 	public static String CONFIG_FILE_NAME = "config.yml";
 	public static String SECRETARIES_FILE_NAME = "secretaries.yml";
 	public static String TODO_FILE_NAME = "todo.yml";
 	public static String REMINDERS_FILE_NAME = "reminders.yml";
 	// messages
 	public static final String INSTRUCTIONS = "Secretaries must be selected to perform commands on them.\nRight-click the secretary with a FEATHER to toggle selection on and off.\nRight-click the secretary with PAPER to view your todo list.\nRight-click the secretary with an INK_SACK to view your reminders.\nType " + ChatColor.GOLD + "/secretary help" + ChatColor.RESET + " in chat to see more instructions.";
 	public static final String COMMANDS = "Type " + ChatColor.GOLD + "/secretary help <command>" + ChatColor.RESET + " to see more details about a command.\nCommands\n" + ChatColor.GOLD + "/secretary create" + ChatColor.RESET + " - makes a new secretary.\n" + ChatColor.GOLD + "/secretary todo [add|mark|delete|list]" + ChatColor.RESET + " - manipulates todo list\n" + ChatColor.GOLD + "/secretary remind [add|list] - adds and views reminders.\n" + ChatColor.GOLD + "/secretary delete" + ChatColor.RESET + " - remove a secretary.\n" + ChatColor.GOLD + "/secretary setsound" + ChatColor.RESET + " - sets the alarm sound for a secretary.\n" + ChatColor.GOLD + "/secretary name" + ChatColor.RESET + " - view a secretary's name.";
 
 	public enum CMDS {
 
 		CREATE, DELETE, TODO, REMIND, REPEAT, SETSOUND, NAME, ADMIN;
 	}
 	public static final String COMMAND_CREATE = "Look at the block where you want the secretary to stand, then type\nthe command " + ChatColor.GOLD + "/secretary create" + ChatColor.RESET + ".\nIn SURVIVAL mode, you will need 8 fences, and 3 wood pressure plates.";
 	public static final String COMMAND_DELETE = "Select the secretary you want to delete\nby right-clicking it with a FEATHER, then type the command " + ChatColor.GOLD + "/secretary delete" + ChatColor.RESET + ".\n" + ChatColor.RED + "WARNING:" + ChatColor.RESET + " You will lose any todos and reminders that have been set!";
 	public static final String COMMAND_TODO = "Select the secretary you want to add a todo to\nby right-clicking it with a FEATHER\nTo add a todo, type " + ChatColor.GOLD + "/secretary todo add [the thing you need to do]" + ChatColor.RESET + "\nTo list your todos, type " + ChatColor.GOLD + "/secretary todo list" + ChatColor.RESET + " (or right-click with PAPER).\nTo mark a todo as DONE, list the todos to get the todo's number,\nthen type " + ChatColor.GOLD + "/secretary todo mark [x]" + ChatColor.RESET + ", where [x] is a number.\nTo delete a todo, list the todos to get the todo's number,\nthen type " + ChatColor.GOLD + "/secretary todo delete [x]" + ChatColor.RESET + ", where [x] is a number.";
 	public static final String COMMAND_REMIND = "Select the secretary you want to add a reminder to\nby right-clicking it with a FEATHER\nTo add a reminder, type " + ChatColor.GOLD + "/secretary remind add [the thing you need to do] [minutes until alarm]\n" + ChatColor.RESET + "eg. /secretary remind add Get more coal 15\nTo list your reminders, type " + ChatColor.GOLD + "/secretary remind list" + ChatColor.RESET + " (or right-click with an INK_SACK).\nOnce the secretary has jogged your memory, the item will be removed from the reminder list.";
 	public static final String COMMAND_REPEAT = "Select the secretary you want to add a repeating reminder to\nby right-clicking it with a FEATHER\nTo add a repeating reminder, type " + ChatColor.GOLD + "/secretary repeat add [the thing you need to do] [minutes until alarm]\n" + ChatColor.RESET + "eg. /secretary repeat add Harvest wheat crops 45\nTo toggle repeating reminders off and on, first list the reminders to get the reminder's number (or right-click with an INK_SACK),\n then type " + ChatColor.GOLD + "/secretary repeat set [x]" + ChatColor.RESET + " where [x] is the reminder number.\nIf the reminder is already a repeating one, it will be toggled OFF and will be removed after the next alarm.";
 	public static final String COMMAND_SOUND = "Select the secretary you want to set the reminder sound for\nby right-clicking it with a FEATHER\nTo change the reminder alarm sound,\ntype " + ChatColor.GOLD + "/secretary setsound [sound effect]" + ChatColor.RESET + "\neg. /secretary setsound BLAZE_SHOOT\nYou can choose from: BLAZE_SHOOT, BOW_FIRE, CLICK1, DOOR_TOGGLE, EXTINGUISH, GHAST_SHOOT, GHAST_SHRIEK, STEP_SOUND, ZOMBIE_CHEW_IRON_DOOR, ZOMBIE_CHEW_WOODEN_DOOR, ZOMBIE_DESTROY_DOOR";
 	public static final String COMMAND_NAME = "Select the secretary whose name you want to see\nby right-clicking it with a FEATHER\nTo view the name, type " + ChatColor.GOLD + "/secretary name";
 	public static final String COMMAND_ADMIN = "Arguments\n" + ChatColor.GOLD + "/secretary admin s_limit [x]" + ChatColor.RESET + " - set the number of secretaries allowed per player.\n" + ChatColor.GOLD + "/secretary admin t_limit [x]" + ChatColor.RESET + " - set the number of todo items allowed per secretary.\n" + ChatColor.GOLD + "/secretary admin r_limit [x]" + ChatColor.RESET + " - set the number of reminders allowed per secretary.\n" + ChatColor.GOLD + "/secretary admin use_inv [true|false]" + ChatColor.RESET + " - set whether a player must have the required fences and pressure plates in their inventory. SURVIVAL mode only.\n" + ChatColor.GOLD + "/secretary admin damage [x]" + ChatColor.RESET + " - set the amount of time (in minutes) secretaries take no damage.";
 	public static final String NO_PERMS_MESSAGE = "You do not have permission to do that!";
 	public static final String NOT_OWNER = "That is not your secretary!";
	public static final String NO_TODOS = "You have not added any todos yet.";
 	public static final String NO_REMINDS = "You have not added any reminders yet.";
 	public static final String WRONG_MATERIAL = "That is not the correct material to get a list from the secretary!";
 	public static final String CMD_MESSAGE_ON = "Secretary selected, and is at your command!";
 	public static final String CMD_MESSAGE_OFF = "Secretary deselected, carry on as normal...";
 	public static String PROFESSION_ENABLED = "You have the Profession plugin installed!";
 	private static final List<String> firstnames = Arrays.asList("authentic", "inspirational", "reactionary", "absurd", "conservative", "ironic", "realistic", "commonplace", "controversial", "liberal", "recondite", "heretical", "credible", "melodramatic", "romantic", "improbable", "cultural", "mystical", "satiric", "incredible", "didactic", "naturalistic", "scholarly", "insignificant", "dramatic", "objective", "significant", "intolerant", "esoteric", "orthodox", "spiritual", "pedantic", "expressionistic", "philosophic", "subjective", "prejudiced", "fanciful", "plausible", "symbolic", "shallow", "humanistic", "pragmatic", "utilitarian", "superficial", "humorous", "profound", "trivial", "impressionistic", "radical", "unscholarly", "active", "exquisite", "pretty", "awkward", "hideous", "ugly", "adept", "fair", "ravishing", "bizarre", "homely", "ungainly", "adroit", "fascinating", "robust", "cadaverous", "horrible", "unkempt", "agile", "good-looking", "shapely", "clumsy", "incongruous", "unmanly", "attractive", "graceful", "skillful", "coarse", "invidious", "unwomanly", "beautiful", "handsome", "spirited", "decrepit", "loathsome", "weak", "brawny", "hardy", "spruce", "effeminate", "odious", "charming", "immaculate", "stalwart", "emaciated", "repellent", "comely", "lively", "strapping", "feeble", "repugnant", "dainty", "lovely", "strong", "frail", "repulsive", "dapper", "manly", "sturdy", "gawky", "sickly", "delicate", "muscular", "virile", "ghastly", "slovenly", "dexterous", "neat", "vivacious", "graceless", "spare", "elegant", "nimble", "winsome", "grotesque", "thin", "apt", "learned", "bigoted", "stupid", "astute", "observant", "crass", "ungifted", "capable", "precocious", "dull", "unintellectual", "clever", "prudent", "fatuous", "unintelligent", "competent", "rational", "foolish", "unlettered", "crafty", "reasonable", "ignorant", "unschooled", "cunning", "sage", "illiterate", "vacuous", "educated", "scholarly", "inane", "erudite", "sensible", "irrational", "gifted", "shrewd", "narrow-minded", "ingenious", "subtle", "obtuse", "intellectual", "talented", "puerile", "intelligent", "wily", "shallow", "inventive", "wise", "simple", "abstemious", "righteous", "base", "iniquitous", "austere", "straightforward", "corrupt", "intemperate", "chaste", "temperate", "deceitful", "notorious", "decent", "trustworthy", "degenerate", "reprobate", "exemplary", "truthful", "depraved", "ribald", "faultless", "undefiled", "dishonest", "sensual", "guileless", "upright", "dishonorable", "unprincipled", "honorable", "virtuous", "dissolute", "unscrupulous", "idealistic", "foul", "vicious", "innocent", "immoral", "vile", "pure", "incorrigible", "vulgar", "puritanical", "indecent", "wicked", "respectable", "infamous", "angelic", "agnostic", "materialistic", "devout", "atheistic", "mundane", "faithful", "blasphemous", "profane", "godlike", "carnal", "sacrilegious", "holy", "diabolic", "skeptical", "pious", "fiend", "like", "unregenerate", "regenerate", "godless", "religious", "impious", "reverent", "irrelevant", "saintly", "irreligious", "affable", "acrimonious", "irascible", "amiable", "antagonistic", "malevolent", "amicable", "anti-social", "misanthropic", "cheerful", "boorish", "obsequious", "civil", "brusque", "peevish", "congenial", "captious", "perverse", "convivial", "caustic", "petulant", "cooperative", "churlish", "provincial", "cordial", "contentious", "quarrelsome", "courteous", "crabbed", "rustic", "debonair", "critical", "shrewish", "elegant", "crusty", "sniveling", "genial", "cynical", "sulky", "gracious", "discourteous", "sullen", "hospitable", "fawning", "sycophantic", "jolly", "fractious", "uncivil", "jovial", "grumpy", "unctuous", "polite", "ill-bred", "ungracious", "politic", "ill-mannered", "unpolished", "sociable", "implacable", "unrefined", "suave", "impolite", "unsociable", "tactful", "imprudent", "waspish", "urbane", "insolent", "admirable", "gentle", "phlegmatic", "apathetic", "indiscreet", "rebellious", "altruistic", "gullible", "plucky", "arrogant", "inefficient", "recalcitrant", "ambitious", "humane", "punctual", "artificial", "insensitive", "reckless", "aristocratic", "humble", "radical", "avaricious", "insidious", "refractory", "artless", "illustrious", "reactionary", "boastful", "insignificant", "remiss", "assiduous", "imperturbable", "refined", "brutish", "intolerant", "reprehensible", "audacious", "imposing", "reserved", "bumptious", "irresolute", "ruthless", "benevolent", "impressive", "resolute", "bungling", "irresponsible", "sanctimonious", "candid", "indifferent", "resourceful", "callous", "lackadaisical", "scurrilous", "cautious", "indomitable", "responsive", "capricious", "lazy", "self-centered", "charitable", "indulgent", "reticent", "complacent", "lethargic", "self-indulgent", "circumspect", "industrious", "saturnine", "conceited", "listless", "silly", "compassionate", "influential", "saucy", "contemptible", "malicious", "slothful", "confident", "ingenious", "scrupulous", "contemptuous", "malignant", "smug", "conscientious", "intrepid", "sedate", "cowardly", "mediocre", "squeamish", "conservative", "kindly", "self-impassive", "craven", "mercenary", "stingy", "considerate", "laconic", "self-reliant", "cruel", "mischievous", "stubborn", "courageous", "liberal", "sensitive", "dilatory", "mulish", "timorous", "coy", "long-suffering", "serious", "disdainful", "niggardly", "traitorous", "cultured", "magnanimous", "shy", "dogmatic", "obdurate", "treacherous", "demure", "meek", "sober", "domineering", "obnoxious", "truculent", "determined", "melancholic", "solemn", "eccentric", "obstinate", "unambitious", "diffident", "merciful", "staid", "egotistical", "odd", "unreliable", "diligent", "moody", "stoical", "envious", "oppressive", "unruly", "discreet", "munificent", "strong-willed", "erratic", "ordinary", "unstable", "distinguished", "nave", "sympathetic", "fastidious", "overconfident", "vain", "earnest", "natural", "taciturn", "fickle", "parasitic", "venal", "efficient", "noble", "thrifty", "frivolous", "parsimonious", "vindictive", "eloquent", "nonchalant", "timid", "gluttonous", "perfidious", "voracious", "eminent", "patient", "tolerant", "haughty", "petty", "wearisome", "enthusiastic", "pensive", "unaffected", "headstrong", "pharisaical", "willful", "flippant", "persevering", "uncompromising", "hypocritical", "pompous", "worthless", "forbearing", "persistent", "valorous", "imperious", "prejudiced", "frugal", "persuasive", "wary", "impetuous", "prolix", "garrulous", "pert", "well-bred", "imprudent", "proud", "generous", "philanthropic", "whimsical", "impulsive", "quixotic", "genteel", "philosophical", "witty", "incompetent", "rash", "zealous");
 	private static final int fn_count = firstnames.size();
 	private static final List<String> lastnames = Arrays.asList("accountant", "accounting", "accounts", "accruals", "ads", "advertise", "affordable", "agenda", "agreement", "arbitration", "benefits", "bill of lading", "board", "board of directors", "bond", "bonus", "bookkeeping", "borrow", "boss", "bottom line", "break even", "briefcase", "budget", "business", "business card", "buy", "buyer", "calculate", "capital", "capitalist", "career", "cargo", "chairman", "chairwoman", "charge", "clause", "client", "close", "collateral", "cold call", "commerce", "commercial", "commission", "commodity", "company", "competition", "compromise", "consumer", "contract", "copyright", "corporate", "corporation", "cost", "corner office", "coupon", "credit", "credit card", "cubicle", "currency", "customer", "database", "deadline", "deal", "debit", "deflation", "demand", "department", "discount", "director", "discount", "dismiss", "distribution", "diversify", "dividend", "download", "down payment", "duty", "duties", "economical", "economics", "economy of scale", "efficiency", "employ", "employee", "employer", "employment", "entrepreneur", "equipment", "estimate", "executive", "expenses", "export", "facility", "factory", "fax", "figures", "finance", "financial", "fire", "foreman", "framework", "freight", "fund", "goods", "graph", "gross", "growth", "guidebook", "headhunter", "headquarters", "high", "hire", "hours", "import", "incentive", "income", "income tax", "inflation", "insurance", "intern", "interest rate", "interview", "inventory", "invest", "investment", "invoice", "job", "labor", "laborer", "laptop", "lead", "lease", "leave", "letterhead", "liability", "loan", "log-in", "loss", "low", "lucrative", "mailbox", "mainframe", "manage", "manager", "management", "market", "marketing", "meeting", "memo", "merchandise", "merchant", "money", "monopoly", "motherboard", "mouse pad", "negotiate", "negotiation", "net", "network", "net worth", "niche", "notebook", "notice", "no-win", "occupation", "offer", "office", "offline", "opportunity", "open", "order", "organization", "online", "outgoing", "owner", "overdraft", "overhead", "packing list", "paperweight", "partner", "password", "pay", "payment", "perk", "personnel", "plan", "policy", "portfolio", "position", "presentation", "president", "price", "prime rate", "principal", "product", "production", "profit", "profitable", "promotion", "proposal", "prospects", "proxy", "purchase order", "purchasing", "quarter", "quit", "rank", "receipt", "recruit", "recruiter", "refund", "resign", "resume", "retail", "retailer", "retire", "risk", "salary", "sale", "salesman", "sales tax", "saleswoman", "secretary", "sell", "seller", "service", "shareholder", "ship", "shipment", "shipping", "shop", "sick leave", "sign", "signature", "spreadsheet", "staff", "statement", "stock", "stockholder", "strike", "success", "superintendent", "supervisor", "supply", "target", "tariff", "tax", "temp", "terms", "trade", "trade-off", "trainee", "transaction", "treasurer", "treasury", "trend", "typeface", "typewriter", "upgrade", "upload", "unemployment", "union", "unit cost", "username", "vacancy", "vacation time", "venture", "vice-president", "video conference", "volume", "warranty", "wastebasket", "waybill", "wholesale", "wholesaler", "win-win", "withdraw", "work", "worker", "workroom", "workspace", "yield");
 	private static final int ln_count = lastnames.size();
 
 	// function to print out todos and reminders
 	public static void list(FileConfiguration c, String configPath, Player p, String s) {
 		if (c.isSet(configPath)) {
 			Set<String> thelist = c.getConfigurationSection(configPath).getKeys(false);
 			ChatColor colour;
 			if (s.equals("todos")) {
 				colour = ChatColor.YELLOW;
 			} else {
 				colour = ChatColor.BLUE;
 			}
 			if (!thelist.isEmpty()) {
 				int i = 1;
 				p.sendMessage(colour + "Your " + s + ":");
 				for (String str : thelist) {
 					String actualPath = c.getConfigurationSection(configPath + "." + str).getName();
 					if (s.equals("todos")) {
 						int m = c.getInt(configPath + "." + actualPath + ".status");
 						if (m == 1) {
 							str += " - DONE";
 						}
 					} else {
 						long now = System.currentTimeMillis();
 						long alarm = c.getLong(configPath + "." + actualPath + ".alarm");
 						long diff = alarm - now;
 						String remaining = String.format("%d min, %d sec",
 								TimeUnit.MILLISECONDS.toMinutes(diff),
 								TimeUnit.MILLISECONDS.toSeconds(diff)
 								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)));
 						str += " - " + remaining + " remaining";
 						if (c.isSet(configPath + "." + actualPath + ".repeat")) {
 							str += ": REPEATING";
 						}
 					}
 					p.sendMessage(colour + "" + i + ". " + str);
 					i++;
 				}
 			} else {
 				p.sendMessage(colour + "There are no " + s + "!");
 			}
 		} else {
 			p.sendMessage(ChatColor.GREEN + "There are no " + s + "!");
 		}
 	}
 
 	public static String name() {
 		String sec_name;
 		Random fn_rand = new Random();
 		Random ln_rand = new Random();
 		while (true) {
 			int fn = fn_rand.nextInt(fn_count);
 			int ln = ln_rand.nextInt(ln_count);
 			sec_name = firstnames.get(fn) + " " + lastnames.get(ln);
 			return WordUtils.capitalize(sec_name);
 		}
 	}
 
 	public static void setBlock(World w, int x, int y, int z, float yaw, float min, float max, String compare) {
 		Block b = w.getBlockAt(x, y, z);
 		if (b.getType().equals(Material.AIR) || b.getType().equals(Material.LONG_GRASS)) {
 			b.setTypeId(85);
 		}
 		if (compare.equals("AND")) {
 			if (yaw >= min && yaw < max) {
 				Block d = w.getBlockAt(x, y + 1, z);
 				if (d.getType().equals(Material.AIR)) {
 					d.setTypeId(72);
 				}
 			}
 		} else {
 			if (yaw >= min || yaw < max) {
 				Block d = w.getBlockAt(x, y + 1, z);
 				if (d.getType().equals(Material.AIR)) {
 					d.setTypeId(72);
 				}
 			}
 		}
 	}
 	public static String[] EFFECT_TYPES = {
 		"BLAZE_SHOOT",
 		"BOW_FIRE",
 		"CLICK1",
 		"CLICK2",
 		"DOOR_TOGGLE",
 		"EXTINGUISH",
 		"GHAST_SHOOT",
 		"GHAST_SHRIEK",
 		"STEP_SOUND",
 		"ZOMBIE_CHEW_IRON_DOOR",
 		"ZOMBIE_CHEW_WOODEN_DOOR",
 		"ZOMBIE_DESTROY_DOOR"
 	};
 
 	public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
 		if (c != null && string != null) {
 			try {
 				return Enum.valueOf(c, string.trim().toUpperCase());
 			} catch (IllegalArgumentException ex) {
 			}
 		}
 		return null;
 	}
 
 	public static CMDS fromString(String name) {
 		return getEnumFromString(CMDS.class, name);
 	}
 }
