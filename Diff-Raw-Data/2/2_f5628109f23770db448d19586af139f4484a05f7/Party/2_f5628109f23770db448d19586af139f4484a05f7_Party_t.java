 /*
  * This file is part of mmoMinecraft (http://code.google.com/p/mmo-minecraft/).
  * 
  * mmoMinecraft is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package mmo.Party;
 
 import mmo.Core.ArrayListString;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import mmo.Core.GenericLivingEntity;
 import mmo.Core.mmo;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.util.config.Configuration;
 import org.getspout.spoutapi.gui.Container;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.Widget;
 
 public class Party {
 
 	protected static mmo mmo;
 	private static Server server;
 	/**
 	 * All the active parties, use "party.remove()" where needed.
 	 */
 	private static final ArrayList<Party> parties = new ArrayList<Party>();
 	/**
 	 * The names of all members of this party.
 	 */
 	private final ArrayListString members = new ArrayListString();
 	/**
 	 * The names of all players invited to this party.
 	 */
 	private final ArrayListString invites = new ArrayListString();
 	/**
 	 * The name of the party leader.
 	 */
 	protected String leader;
 	/**
 	 * A map of player containers, each container is their party bar
 	 */
 	protected static HashMap<Player, GenericContainer> containers = new HashMap<Player, GenericContainer>();
 
 	/**
 	 * Constructor.
 	 */
 	public Party() {
 		this("", "", "");
 	}
 
 	/**
 	 * Constructor.
 	 * @param names The player names to add
 	 */
 	public Party(String leader) {
 		this(leader, leader, "");
 	}
 
 	/**
 	 * Constructor.
 	 * @param names The player names to add
 	 */
 	public Party(String leader, String names) {
 		this(leader, names, "");
 	}
 
 	/**
 	 * Constructor.
 	 * @param names The player names to add
 	 * @param invites The player names to invite
 	 */
 	public Party(String leader, String names, String invite) {
 		server = mmo.server;
 		parties.add(this); // Make sure we can store the new party
 		this.leader = leader;
 		if (!names.equals("")) {
 			members.addAll(Arrays.asList(names.split(",")));
 		}
 		if (!invite.equals("")) {
 			invites.addAll(Arrays.asList(invite.split(",")));
 		}
 	}
 
 	/**
 	 * Load all parties and invites.
 	 */
 	protected static void load() {
 		Configuration cfg = new Configuration(new File(mmo.plugin.getDataFolder(), "parties.yml"));
 		cfg.load();
 		for (String leader : cfg.getKeys()) {
 			new Party(leader, cfg.getString(leader + ".members", ""), cfg.getString(leader + ".invites", ""));
 		}
 	}
 
 	/**
 	 * Save all parties and invites.
 	 */
 	protected static void save() {
 		Configuration cfg = new Configuration(new File(mmo.plugin.getDataFolder(), "parties.yml"));
 		cfg.setHeader("#mmoParty list of party members + invites");
 		synchronized (parties) {
 			for (Party party : parties) {
 				if (party.isParty() || !party.invites.isEmpty()) {
 					cfg.setProperty(party.leader + ".members", party.getMemberNames());
 					cfg.setProperty(party.leader + ".invites", party.getInviteNames());
 				}
 			}
 		}
 		cfg.save();
 	}
 
 	/**
 	 * Delete a Party from the global list.
 	 * @param party The Party to remove
 	 */
 	public static void delete(Party party) {
 		if (party != null && parties.contains(party)) {
 			parties.remove(parties.indexOf(party));
 		}
 	}
 
 	/**
 	 * Clear all saved parties - only meant for onDisable.
 	 */
 	protected static void clear() {
 		parties.clear();
 	}
 
 	/**
 	 * Find a Party via a Player.
 	 * @param player The player who's party we are trying to find
 	 * @return The Party they are a member of, or null
 	 */
 	public static Party find(Player player) {
 		return find(player.getName());
 	}
 
 	/**
 	 * Find a Party via a player name.
 	 * @param player The player who's party we are trying to find
 	 * @return The Party they are a member of, or null
 	 */
 	public static Party find(String player) {
 		for (Party party : parties) {
 			if (party.members.contains(player)) {
 				return party;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks whether two players are in the same party
 	 * @param a First player
 	 * @param b Second player
 	 * @return If they're both in the same party
 	 */
 	public static boolean isSameParty(Player a, Player b) {
 		if (a != null && b != null && find(a) == find(b)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns whether this is a real party, or a single player with invites.
 	 * @return Whether there are party members other than the leader
 	 */
 	public boolean isParty() {
 		return (members.size() > 1);
 	}
 
 	/**
 	 * Returns whether this party has any invites.
 	 * @return Whether there are invites outstanding
 	 */
 	public boolean hasInvites() {
 		return !invites.isEmpty();
 	}
 
 	/**
 	 * Find all outstanding invites for a Player.
 	 * @param player The Player we're interested in
 	 * @return A List of parties that have invited player
 	 */
 	public static List<Party> findInvites(Player player) {
 		ArrayList<Party> list = new ArrayList<Party>();
 		for (Party party : parties) {
 			if (party.invites.contains(player.getName())) {
 				list.add(party);
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * Remove all outstanding invites for a Player.
 	 * @param player The Player to un-invite
 	 */
 	public static void declineInvites(Player player) {
 		for (Party party : findInvites(player)) {
 			if (party.invites.contains(player.getName())) {
 				party.invites.remove(party.invites.indexOf(player.getName()));
 			}
 		}
 		Party.save();
 	}
 
 	/**
 	 * Get online players in a party, exclude a single player from the list.
 	 * @param name The player to exclude from the list
 	 * @return A list of all online players
 	 */
 	public List<Player> getMembers(String name) {
 		return getMembers(server.getPlayer(name));
 	}
 
 	/**
 	 * Get online players in a party, exclude a single player from the list.
 	 * @param name The player to exclude from the list
 	 * @return A list of all online players
 	 */
 	public List<Player> getMembers(Player player) {
 		List<Player> players = getMembers();
 		if (player != null && players.contains(player)) {
 			players.remove(player);
 		}
 		return players;
 	}
 
 	/**
 	 * Get online players in a party.
 	 * @return A list of all online players
 	 */
 	public List<Player> getMembers() {
 		ArrayList<Player> players = new ArrayList<Player>();
 		for (String name : members) {
 			Player player = server.getPlayer(name);
 			if (player != null && player.isOnline()) {
 				players.add(player);
 			}
 		}
 		return players;
 	}
 
 	/**
 	 * Get all players in a party.
 	 * @return A list of player names separated by commas
 	 */
 	public String getMemberNames() {
 		String names = "";
 		boolean first = true;
 		for (String name : members) {
 			names += (first ? "" : ",") + name;
 			first = false;
 		}
 		return names;
 	}
 
 	/**
 	 * Get all players invites to a party.
 	 * @return A list of player names separated by commas
 	 */
 	public String getInviteNames() {
 		String names = "";
 		boolean first = true;
 		for (String name : invites) {
 			names += (first ? "" : ",") + name;
 			first = false;
 		}
 		return names;
 	}
 
 	/**
 	 * Get the number of members in this Party.
 	 * @return The number of members
 	 */
 	public int size() {
 		return members.size();
 	}
 
 	/**
 	 * Adds an invited online Player to this Party.
 	 * @param player The player to add
 	 * @return If they have been successfully added
 	 */
 	public boolean accept(Player player) {
 		if (player == null) {
 			return false;
 		}
 		Party party = Party.find(player);
 		if (party != null && party.isParty()) {
 			mmo.sendMessage(player, "You are already in a party.");
 			return false;
 		}
 		if (!invites.contains(player.getName())) {
 			mmo.sendMessage(player, "You haven't been invited.");
 			return false;
 		}
 		decline(player);
 		if (members.size() >= mmo.cfg.getInt("max_party_size", 6)) {
 			mmo.sendMessage(player, "There isn't any space for you.");
 			return false;
 		}
 		declineInvites(player); // Make sure they have no outstanding invites from anywhere else
 		if (party != null) { // Only if they're the only member - and were sending out invites
 			Party.delete(party);
 		}
 		// Note the order - send to everyone in the party so the new member gets a custom msg
 		mmo.sendMessage(getMembers(), "%s has joined the party.", mmo.name(player.getName()));
 		mmo.notify(getMembers(), "%s joined", mmo.name(player.getName()));
 		mmo.sendMessage(player, "You have joined a party.");
 		mmo.notify(player, "Joined %s", mmo.name(leader));
 		members.add(player.getName());
 		update();
 		Party.save();
 		return true;
 	}
 
 	/**
 	 * Removes an invite for a Player.
 	 * @param player The Player to un-invite
 	 * @return Whether they had a valid invite or not
 	 */
 	public boolean decline(Player player) {
 		if (player != null && invites.contains(player.getName())) {
 			invites.remove(invites.indexOf(player.getName()));
 			mmo.sendMessage(player, "Declined invitation from %s.", mmo.name(leader));
 			Party.save();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Delete a Player from a Party.
 	 * This removes the Player from this party,
 	 * and will delete the party if it is now empty.
 	 * @param player The Player to remove from this party
 	 * @return Whether they have been removed or not found
 	 */
 	public boolean remove(String name) {
 		if (members.contains(name)) {
 			members.remove(members.indexOf(name));
 			if (members.isEmpty()) {
 				Party.delete(this);
 			} else {
 				update();
 			}
 			new Party(name);
 			update(name);
 			Party.save();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Promote a Player to party leader.
 	 * @param leader The person attempting it
 	 * @param name The player to promote
 	 * @return If they have been promoted
 	 */
 	public boolean promote(Player leader, String name) {
 		if (!isLeader(leader)) {
 			mmo.sendMessage(leader, "You are not the party leader.");
 			return false;
 		}
 		if (!isParty()) {
 			mmo.sendMessage(leader, "You are not in a party.");
 			return false;
 		}
 		if (!members.contains(name)) {
 			mmo.sendMessage(leader, "%s is not in your party.", mmo.name(name));
 			return false;
 		}
 		if (isLeader(name)) {
 			mmo.sendMessage(leader, "You are already the party leader.");
 			return false;
 		}
 		this.leader = members.get(name);
 		mmo.notify(name, "Promoted to leader");
 		mmo.notify(getMembers(name), "%s promoted", mmo.name(this.leader));
 		mmo.sendMessage(leader, "Promoted %s to leader.", mmo.name(this.leader));
 		mmo.sendMessage(name, "You have been promoted to leader.");
 		update();
 		Party.save();
 		return true;
 	}
 
 	/**
 	 * Leave a party (in a friendly way).
 	 * @param player The Player leaving
 	 * @return Whether they left this party
 	 */
 	public boolean leave(Player player) {
 		if (remove(player.getName())) {
 			mmo.sendMessage(player, "You have left your party.");
 			mmo.sendMessage(getMembers(), "%s has left the party.", mmo.name(player.getName()));
 			mmo.notify(getMembers(), "%s left", mmo.name(player.getName()));
 			if (isLeader(player)) {
 				leader = members.get(0);
 				mmo.sendMessage(leader, "You are now the party leader");
 				mmo.notify(leader, "Promoted to leader");
 				mmo.notify(getMembers(leader), "%s is now leader", mmo.name(leader));
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Leave a party (in an unfriendly way).
 	 * @param leader The Player attempting to do this
 	 * @param player The Player leaving
 	 * @return Whether they left this party
 	 */
 	public boolean kick(Player leader, String name) {
 		if (!isLeader(leader)) {
 			mmo.sendMessage(leader, "You are not the party leader.");
 			return false;
 		}
 		if (isLeader(name)) {
 			mmo.sendMessage(leader, "You cannot kick yourself.");
 			return false;
 		}
 		if (!members.contains(name)) {
 			mmo.sendMessage(leader, "%s is not in your party.", mmo.name(name));
 			return false;
 		}
 		name = members.get(name);
 		if (!remove(name)) {
 			mmo.sendMessage(leader, "Unable to remove them...");
 			return false;
 		}
 		mmo.sendMessage(getMembers(), "%s has been kicked out of the party.", mmo.name(name));
 		mmo.sendMessage(name, "You have been kicked from the party.");
 		mmo.notify(getMembers(), "%s kicked", mmo.name(name));
 		return true;
 	}
 
 	/**
 	 * Determines if the Player is able to invite / kick etc
 	 * @param player The player to check
 	 * @return If they are the party leader or not
 	 */
 	public boolean isLeader(Player player) {
 		return isLeader(player.getName());
 	}
 
 	/**
 	 * Determines if the Player is able to invite / kick etc
 	 * @param name The player to check
 	 * @return If they are the party leader or not
 	 */
 	public boolean isLeader(String name) {
 		return name != null && leader.equalsIgnoreCase(name);
 	}
 
 	/**
 	 * Get the party leader.
 	 * @return Party leader name
 	 */
 	public String getLeader() {
 		return leader;
 	}
 
 	/**
 	 * Determines if the Player is part of this party
 	 * @param player The player to check
 	 * @return If they are a member or not
 	 */
 	public boolean isMember(Player player) {
 		if (player != null && members.contains(player.getName())) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Invite a player to the party.
 	 * @param leader The Player attempting to do this
 	 * @param name The player to invite
 	 * @return Whether the invitation was successful
 	 */
 	public boolean invite(Player leader, String name) {
 		if (!isLeader(leader)) {
 			mmo.sendMessage(leader, "You are not the party leader.");
 			return false;
 		}
 		Player player = server.getPlayer(name);
 		if (player == null) {
 			mmo.sendMessage(leader, "%s isn't online, is it spelt correctly?", mmo.name(name));
 			return false;
 		}
 		if (player.equals(leader)) {
 			mmo.sendMessage(leader, "You cannot invite yourself.");
 			return false;
 		}
 		if (members.size() >= mmo.cfg.getInt("max_party_size", 6)) {
 			mmo.sendMessage(leader, "You don't have space in your party.");
 			return false;
 		}
 		Party party = find(player);
 		if (party != null && party.size() > 1) {
 			if (this == party) {
 				mmo.sendMessage(leader, "They are already in your party.");
 			} else {
 				mmo.sendMessage(leader, "They are already in a party.");
 			}
 			return false;
 		}
 		if (invites.contains(player.getName())) {
 			mmo.sendMessage(leader, "They have already been invited.");
 			return false;
 		}
 		invites.add(player.getName());
 		mmo.sendMessage(player, "You have been invited to a join party by %s\nTo accept type: /party accept %s", mmo.name(this.leader), this.leader);
 		mmo.sendMessage(leader, "You have invited %s", mmo.name(player.getName()));
 		mmo.notify(player, "Invite from %s", mmo.name(leader.getName()));
 		Party.save();
 		return true;
 	}
 
 	/**
 	 * Update all party members in Player's party.
 	 */
 	public static void update(String name) {
 		update(server.getPlayer(name));
 	}
 
 	/**
 	 * Update all party members in Player's party.
 	 * @param player The Player to update
 	 */
 	public static void update(Player player) {
 		if (player != null) {
 			Party party = Party.find(player);
 			if (party == null) {
 				new Party(player.getName());
 			}
 			party.update();
 		}
 	}
 
 	/**
 	 * Update all parties.
 	 */
 	public static void updateAll() {
 		for (Party party : parties) {
 			party.update();
 		}
 	}
 
 	/**
 	 * Update all party members.
 	 */
 	public void update() {
 		if (mmo.hasSpout && members.size() > 1 || mmo.cfg.getBoolean("always_show", true)) {
 			boolean show_pets = mmo.cfg.getBoolean("show_pets", true);
 
 			for (Player player : getMembers()) {
 				Container container = containers.get(player);
 
 				if (container != null) {
 					int index = 0;
 					Widget[] bars = container.getChildren();
 					for (String name : members.meFirst(player.getName())) {
 						GenericLivingEntity bar;
 						if (index >= bars.length) {
 							container.addChild(bar = new GenericLivingEntity());
 						} else {
 							bar = (GenericLivingEntity)bars[index];
 						}
 						bar.setEntity(name, isLeader(name) ? ChatColor.GREEN + "@" : "");
 						bar.setTargets(show_pets ? mmo.getPets(server.getPlayer(name)) : null);
 						index++;
 					}
 					while (index < bars.length) {
						container.removeChild(bars[index++]);
 					}
 					container.updateLayout();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get an array of member_name:status strings, used for UI and /party status
 	 * @return 
 	 */
 	private HashMap<String, String> getStatus() {
 		HashMap<String, String> status = new HashMap<String, String>();
 		Map<String, Tameable> pets = new HashMap<String, Tameable>();
 		String output = "";
 
 		if (mmo.cfg.getBoolean("show_pets", true)) {
 			for (World world : server.getWorlds()) {
 				for (LivingEntity entity : world.getLivingEntities()) {
 					if (entity instanceof Tameable && ((Tameable) entity).isTamed() && ((Tameable) entity).getOwner() instanceof Player) {
 						String name = ((Player) ((Tameable) entity).getOwner()).getName();
 						if (members.contains(name)) {
 							pets.put(name, (Tameable) entity);
 						}
 					}
 				}
 			}
 		}
 		for (String member : members) {
 			Player player = server.getPlayer(member);
 			if (player == null) {
 				output = mmo.makeBar(ChatColor.BLACK, 0) + mmo.makeBar(ChatColor.BLACK, 0) + ChatColor.DARK_GRAY + member;
 			} else {
 				output = mmo.makeBar(ChatColor.RED, mmo.getHealth(player)) + mmo.makeBar(ChatColor.WHITE, mmo.getArmor(player));
 				output += (isLeader(member) ? ChatColor.GREEN + "@" : "") + mmo.name(player.getName());
 				if (pets.containsKey(member)) {
 					Tameable pet = pets.get(member);
 					if (player.getName().equals(((Player) pet.getOwner()).getName())) {
 						output += "\n" + mmo.makeBar(ChatColor.RED, mmo.getHealth((Entity) pet)) + mmo.makeBar(ChatColor.BLACK, 0);
 						output += ChatColor.WHITE + "+ " + ChatColor.AQUA + " " + mmo.getSimpleName((LivingEntity) pet, false);
 					}
 				}
 			}
 			status.put(member, output + "\n");
 		}
 		return status;
 	}
 
 	/**
 	 * Print current party status.
 	 * @param name Who we send it to
 	 */
 	public void status(Player player) {
 		if (player != null) {
 			HashMap<String, String> status = getStatus();
 			String name = player.getName();
 
 			String output = status.get(name);
 			for (String tmp : members) {
 				if (!name.equals(tmp)) {
 					output += status.get(tmp);
 				}
 			}
 			mmo.sendMessage(player, "Status:");
 			mmo.sendMessage(player, output);
 		}
 	}
 }
