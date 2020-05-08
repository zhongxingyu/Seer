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
 
 package eu.tomylobo.abstraction.platform.spout;
 
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
 
 public class SpoutUtils {
 	private static final Map<org.spout.api.geo.World, SpoutWorld> wrappedWorlds = new IdentityHashMap<org.spout.api.geo.World, SpoutWorld>();
 	private static final Map<org.spout.api.player.Player, SpoutPlayer> wrappedPlayers;
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
 
 	public static SpoutWorld wrap(org.spout.api.geo.World backend) {
 		if (backend == null)
 			return null;
 
 		SpoutWorld spoutWorld = wrappedWorlds.get(backend);
 		if (spoutWorld == null)
 			wrappedWorlds.put(backend, spoutWorld = new SpoutWorld(backend));
 
 		return spoutWorld;
 	}
 
 	public static org.spout.api.geo.World unwrap(World world) {
 		if (world == null)
 			return null;
 
 		return ((SpoutWorld) world).backend;
 	}
 
 
 	public static Vector wrap(org.spout.api.math.Vector3 vector) {
 		return new Vector(vector.getX(), vector.getY(), vector.getZ());
 	}
 
 	public static org.spout.api.math.Vector3 unwrap(Vector vector) {
		return new org.spout.api.math.Vector3(vector.getX(), vector.getY(), vector.getZ());
 	}
 
 
 	public static Location wrap(org.spout.api.geo.discrete.Point location) {
 		return new Location(
 				wrap(location.getWorld()),
 				wrap((org.spout.api.math.Vector3) location),
 				0, 0
 		);
 	}
 	public static Location wrap(org.spout.api.entity.Position location) {
 		return new Location(
 				wrap(location.getPosition().getWorld()),
 				wrap((org.spout.api.math.Vector3) location.getPosition()),
 				location.getYaw(), location.getPitch()
 		);
 	}
 
 	public static org.spout.api.entity.Position unwrap(Location location) {
 		return new org.spout.api.entity.Position(
 				unwrapPoint(location),
 				location.getPitch(), location.getYaw(), 0
 		);
 	}		
 	public static org.spout.api.geo.discrete.Point unwrapPoint(Location location) {
		return new org.spout.api.geo.discrete.Point(
 				unwrap(location.getPosition()),
 				unwrap(location.getWorld())
 		);
 	}
 
 
 	public static SpoutPlayer wrap(org.spout.api.player.Player backend) {
 		if (backend == null)
 			return null;
 
 		SpoutPlayer spoutPlayer = wrappedPlayers.get(backend);
 		if (spoutPlayer == null) {
 			wrappedPlayers.put(backend, spoutPlayer = new SpoutPlayer(backend));
 		}
 
 		return spoutPlayer;
 	}
 
 	public static org.spout.api.player.Player unwrap(Player player) {
 		if (player == null)
 			return null;
 
 		return ((SpoutPlayer) player).backend;
 	}
 
 
 	public static SpoutEntity wrap(org.spout.api.entity.Entity backend) {
 		if (backend == null)
 			return null;
 
 		if (backend instanceof org.spout.api.player.Player)
 			return wrap((org.spout.api.player.Player) backend);
 
 		return new SpoutEntity(backend);
 	}
 
 	public static org.spout.api.entity.Entity unwrap(Entity entity) {
 		if (entity == null)
 			return null;
 
 		return ((SpoutEntity) entity).backend;
 	}
 
 
 	public static CommandSender wrap(org.spout.api.command.CommandSource backend) {
 		if (backend == null)
 			return null;
 
 		if (backend instanceof org.spout.api.player.Player)
 			return wrap((org.spout.api.player.Player) backend);
 
 		return new SpoutCommandSender(backend);
 	}
 
 	public static org.spout.api.command.CommandSource unwrap(CommandSender sender) {
 		if (sender == null)
 			return null;
 
 		if (sender instanceof Player)
 			return unwrap((Player) sender);
 		
 		return ((SpoutCommandSender) sender).backend;
 	}
 }
