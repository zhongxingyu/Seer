 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.brothercraft.chdangerous.functions;
 
import com.avaje.ebean.validation.AssertFalse;
 import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCEntity;
 import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
 import com.laytonsmith.annotations.api;
 import com.laytonsmith.core.CHVersion;
 import com.laytonsmith.core.Static;
 import com.laytonsmith.core.constructs.CInt;
 import com.laytonsmith.core.constructs.CNull;
 import com.laytonsmith.core.constructs.Construct;
 import com.laytonsmith.core.constructs.Target;
 import com.laytonsmith.core.environments.CommandHelperEnvironment;
 import com.laytonsmith.core.environments.Environment;
 import com.laytonsmith.core.exceptions.ConfigRuntimeException;
 import com.laytonsmith.core.functions.AbstractFunction;
 import com.laytonsmith.core.functions.Exceptions.ExceptionType;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 /**
  *
  * @author cgallarno
  */
 public class CHdPlayer {
     @api
     public static class chd_ptarget extends AbstractFunction {
 
 	public ExceptionType[] thrown() {
 	    throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	public boolean isRestricted() {
 	    return false;
 	}
 
 	public Boolean runAsync() {
 	    return false;
 	}
 
 	public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 	    MCPlayer p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
 	    if (args.length == 1) {
 		p = Static.GetPlayer(args[0], t);
 	    }
 	    Static.AssertPlayerNonNull(p, t);
 	    Player player = ((Player) p.getHandle());
 	    Entity target = null;
 	    double targertDistanceSquared = 0;
 	    final double radiusSquared = 1;
 	    final Vector l = player.getEyeLocation().toVector(), n = player.getLocation().getDirection().normalize();
 	    final double cos45 = Math.cos(Math.PI / 4);
 	    for (final LivingEntity other : player.getWorld().getEntitiesByClass(LivingEntity.class)) {
 		if (other == player)
 		    continue;
 		if (target == null || targertDistanceSquared > other.getLocation().distanceSquared(player.getLocation())) {
 		    final Vector v = other.getLocation().add(0, 1, 0).toVector().subtract(l);
 		    if (n.clone().crossProduct(v).lengthSquared() < radiusSquared && v.normalize().dot(n) >= cos45) {
 			target = other;
 			targertDistanceSquared = target.getLocation().distanceSquared(player.getLocation());
 		    }
 		}
 	    }
 	    if (target == null) {
 		return new CNull(t);
 	    } else {
 		return new CInt(target.getEntityId(), t);
 	    }
 	}
 
 	public String getName() {
 	    return "chd_ptarget";
 	}
 
 	public Integer[] numArgs() {
	    return new Integer[]{1};
 	}
 
 	public String docs() {
	    return "gets the entity id of the entity the player is looking at";
 	}
 
 	public Version since() {
 	    return CHVersion.V3_3_1;
 	}
 	
     }
 }
