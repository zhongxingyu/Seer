 /*
  * Copyright (C) 2012 TomyLobo
  *
  * This file is part of Routes.
  *
  * Routes is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package eu.tomylobo.abstraction.bukkit;
 
 import java.lang.reflect.Method;
 import java.util.IdentityHashMap;
 import java.util.Map;
 
 import com.google.common.base.Equivalence;
 import com.google.common.base.Equivalences;
 import com.google.common.collect.MapMaker;
 
 import eu.tomylobo.abstraction.CommandSender;
 import eu.tomylobo.abstraction.World;
 import eu.tomylobo.abstraction.entity.Entity;
 import eu.tomylobo.abstraction.entity.Player;
 import eu.tomylobo.math.Location;
 import eu.tomylobo.math.Vector;
 
 public class BukkitUtils {
 	private static final Map<org.bukkit.World, BukkitWorld> wrappedWorlds = new IdentityHashMap<org.bukkit.World, BukkitWorld>();
 	private static final Map<org.bukkit.entity.Player, BukkitPlayer> wrappedPlayers;
 	static {
 		try {
 			final MapMaker mapMaker = new MapMaker()
 				.weakKeys()
 				.weakValues()
 				.concurrencyLevel(1);
 
 			Method keyEquivalence = MapMaker.class.getDeclaredMethod("keyEquivalence", Equivalence.class);
 			keyEquivalence.setAccessible(true);
 			keyEquivalence.invoke(mapMaker, Equivalences.identity());
 			wrappedPlayers = mapMaker.makeMap();
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static BukkitWorld wrap(org.bukkit.World backend) {
 		BukkitWorld bukkitWorld = wrappedWorlds.get(backend);
 		if (bukkitWorld == null)
 			wrappedWorlds.put(backend, bukkitWorld = new BukkitWorld(backend));
 
 		return bukkitWorld;
 	}
 
 	public static org.bukkit.World unwrap(World world) {
 		return ((BukkitWorld) world).backend;
 	}
 
 
 	public static Vector wrap(org.bukkit.util.Vector vector) {
 		return new Vector(vector.getX(), vector.getY(), vector.getZ());
 	}
 
 	public static org.bukkit.util.Vector unwrap(Vector vector) {
 		return new org.bukkit.util.Vector(vector.getX(), vector.getY(), vector.getZ());
 	}
 
 
 	public static Location wrap(org.bukkit.Location location) {
 		return new Location(
 				wrap(location.getWorld()),
 				wrap(location.toVector()),
 				location.getYaw(), location.getPitch()
 		);
 	}
 
 	public static org.bukkit.Location unwrap(Location location) {
 		return unwrap(location.getPosition()).toLocation(
 				unwrap(location.getWorld()),
 				location.getYaw(), location.getPitch()
 		);
 	}
 
 
 	public static BukkitPlayer wrap(org.bukkit.entity.Player backend) {
 		BukkitPlayer bukkitPlayer = wrappedPlayers.get(backend);
 		if (bukkitPlayer == null) {
 			wrappedPlayers.put(backend, bukkitPlayer = new BukkitPlayer(backend));
 		}
 
 		return bukkitPlayer;
 	}
 
 	public static org.bukkit.entity.Player unwrap(Player player) {
 		return (org.bukkit.entity.Player) ((BukkitPlayer) player).backend;
 	}
 
 
 	public static BukkitEntity wrap(org.bukkit.entity.Entity backend) {
 		if (backend instanceof org.bukkit.entity.Player)
 			return wrap((org.bukkit.entity.Player) backend);
 
 		return new BukkitEntity(backend);
 	}
 
 	public static org.bukkit.entity.Entity unwrap(Entity entity) {
 		return ((BukkitEntity) entity).backend;
 	}
 
 
 	public static CommandSender wrap(org.bukkit.command.CommandSender backend) {
 		if (backend instanceof org.bukkit.entity.Player)
 			return wrap((org.bukkit.entity.Player) backend);
 
 		return new BukkitCommandSender(backend);
 	}
 
 	public static org.bukkit.command.CommandSender unwrap(CommandSender sender) {
		if (sender instanceof Player)
			return unwrap((Player) sender);

 		return ((BukkitCommandSender) sender).backend;
 	}
 }
