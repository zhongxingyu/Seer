 package co.networkery.uvbeenzaned.SnowBrawl;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class Arena {
 
 	private String sender;
 	private String name;
 	private String description;
 	private List<String> authors;
 	private Location cyanspawn;
 	private Location limespawn;
 
 	public Arena(Player player, String name, String description,
 			List<String> authors) {
 		if (player != null) {
 			setSender(player);
 		}
 		setName(name);
 		setDescription(description);
 		setAuthors(authors);
 	}
 
 	public Arena() {
 
 	}
 
 	public static Arena getInstanceFromConfig(String name) {
 		if (Configurations.getArenasconfig().contains(name)) {
 			Arena a = new Arena();
 			a.setName(name);
 			a.setDescription(Configurations.getArenasconfig().getString(
 					name + ".description"));
 			a.setAuthors(Configurations.getArenasconfig().getStringList(
 					name + ".authors"));
 			a.setCyanSide(LocationSerializer.str2loc(Configurations
 					.getArenasconfig().getString(name + ".cyanspawn")));
 			a.setLimeSide(LocationSerializer.str2loc(Configurations
 					.getArenasconfig().getString(name + ".limespawn")));
 			return a;
 		}
 		return null;
 	}
 
 	public void setSender(Player p) {
 		sender = p.getName();
 	}
 
 	public Player getSender() {
 		return Bukkit.getServer().getPlayer(sender);
 	}
 
 	public void setName(String n) {
 		name = n;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setDescription(String d) {
 		description = d;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setAuthors(List<String> a) {
 		authors = a;
 	}
 
 	public void addAuthor(String a) {
 		authors.add(a);
 	}
 
 	public List<String> getAuthors() {
 		return authors;
 	}
 
 	public String getAuthorsString() {
 		String authorstring = "";
 		boolean first = true;
 		for (String author : getAuthors()) {
 			if (!first) {
 				authorstring = authorstring + ", " + author;
 			} else {
 				authorstring = authorstring + author;
 				first = false;
 			}
 		}
 		return authorstring.trim();
 	}
 
 	public Location getCyanSide() {
		//cyanspawn.setY(cyanspawn.getY() + 2);
 		return cyanspawn;
 	}
 
 	public void setCyanSide(Location l) {
 		cyanspawn = l;
 	}
 
 	public Location getLimeSide() {
		//limespawn.setY(limespawn.getY() + 2);
 		return limespawn;
 	}
 
 	public void setLimeSide(Location l) {
 		limespawn = l;
 	}
 
 	private boolean tryPass() {
 		if (cyanspawn != null) {
 			if (limespawn != null) {
 				if (name != "") {
 					if (description != "") {
 						if (!authors.isEmpty()) {
 							return true;
 						} else {
 							Chat.sendPPM("Missing at least one author name!",
 									getSender());
 						}
 					} else {
 						Chat.sendPPM("Missing an arena description!",
 								getSender());
 					}
 				} else {
 					Chat.sendPPM("Missing an arena name!", getSender());
 				}
 			} else {
 				Chat.sendPPM("Missing a lime side!", getSender());
 			}
 		} else {
 			Chat.sendPPM("Missing a cyan side!", getSender());
 		}
 		return false;
 	}
 
 	public void save() {
 		if (tryPass()) {
 			Configurations.getArenasconfig().createSection(name);
 			Configurations.getArenasconfig().set(name + ".cyanspawn",
 					LocationSerializer.loc2str(cyanspawn));
 			Configurations.getArenasconfig().set(name + ".limespawn",
 					LocationSerializer.loc2str(limespawn));
 			Configurations.getArenasconfig().set(name + ".description",
 					description);
 			Configurations.getArenasconfig().set(name + ".authors", authors);
 			Configurations.saveArenasConfig();
 		}
 	}
 
 	public void delete() {
 		Configurations.getArenasconfig().set(name, null);
 		Configurations.saveArenasConfig();
 	}
 }
