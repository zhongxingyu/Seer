 package com.zeoldcraft.dev.functions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.laytonsmith.abstraction.*;
 import com.laytonsmith.abstraction.blocks.MCBlockFace;
 import com.laytonsmith.abstraction.blocks.MCFallingBlock;
 import com.laytonsmith.abstraction.entities.MCEnderman;
 import com.laytonsmith.abstraction.entities.MCOcelot;
 import com.laytonsmith.abstraction.entities.MCSheep;
 import com.laytonsmith.abstraction.entities.MCWolf;
 import com.laytonsmith.abstraction.enums.MCDyeColor;
 import com.laytonsmith.abstraction.enums.MCOcelotType;
 import com.laytonsmith.annotations.api;
 import com.laytonsmith.core.ObjectGenerator;
 import com.laytonsmith.core.Static;
 import com.laytonsmith.core.constructs.*;
 import com.laytonsmith.core.environments.Environment;
 import com.laytonsmith.core.exceptions.ConfigRuntimeException;
 import com.laytonsmith.core.functions.Exceptions;
 import com.laytonsmith.core.functions.Exceptions.ExceptionType;
 import com.laytonsmith.core.functions.FunctionBase;
 import com.laytonsmith.core.functions.FunctionList;
 import com.sk89q.worldguard.protection.flags.WGBukkit;
 import com.zeoldcraft.dev.CHDev.DevFunction;
 
 public class Entities {
 	
 	public String docs() {
 		return "Function testing for EntityManagement class.";
 	}
 	
 	@api
 	public static class get_entity_spec extends DevFunction {
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
 		}
 
 		public Construct exec(Target t, Environment environment,
 				Construct... args) throws ConfigRuntimeException {
 			MCEntity ent = Static.getEntity(Static.getInt32(args[0], t), t);
 			CArray ret = new CArray(t);
 			if (ent instanceof MCItem) {
 				ret.set("itemstack", ObjectGenerator.GetGenerator().item(((MCItem) ent).getItemStack(), t), t);
 				ret.set("pickupdelay", new CInt(((MCItem) ent).getPickupDelay(), t), t);
 			}
 			if (ent instanceof MCFallingBlock) {
 				ret.set("dropitem", new CBoolean(((MCFallingBlock) ent).getDropItem(), t), t);
 				ret.set("type", new CInt(((MCFallingBlock) ent).getBlockId(), t), t);
 				ret.set("data", new CInt(((MCFallingBlock) ent).getBlockData(), t), t);
 			}
 			if (ent instanceof MCExperienceOrb) {
 				ret.set("exp", new CInt(((MCExperienceOrb) ent).getExperience(), t), t);
 			}
 			if (ent instanceof MCLightningStrike) {
 				ret.set("iseffect", new CBoolean(((MCLightningStrike) ent).isEffect(), t), t);
 			}
 			if (ent instanceof MCTNT) {
 				
 			}
 			if (ent instanceof MCFireball) {
 				ret.set("direction", ObjectGenerator.GetGenerator().velocity(((MCFireball) ent).getDirection(), t), t);
 			}
 			if (ent instanceof MCProjectile) {
 				ret.set("shooter", new CInt(((MCProjectile) ent).getShooter().getEntityId(), t), t);
 				ret.set("bounce", new CBoolean(((MCProjectile) ent).doesBounce(), t), t);
 			}
 			if (ent instanceof MCHanging) {
 				ret.set("facing", new CString(((MCHanging) ent).getFacing().name(), t), t);
 			}
 			if (ent instanceof MCWolf) {
 				ret.set("color", new CString(((MCWolf) ent).getCollarColor().name(), t), t);
 				ret.set("angry", new CBoolean(((MCWolf) ent).isAngry(), t), t);
 				ret.set("sitting", new CBoolean(((MCWolf) ent).isSitting(), t), t);
 			}
 			if (ent instanceof MCOcelot) {
 				ret.set("cat", new CString(((MCOcelot) ent).getCatType().name(), t), t);
 				ret.set("sitting", new CBoolean(((MCOcelot) ent).isSitting(), t), t);
 			}
 			if (ent instanceof MCEnderman) {
 				ret.set("type", new CInt(((MCEnderman) ent).getCarriedType(), t), t);
 				ret.set("data", new CInt(((MCEnderman) ent).getCarriedData(), t), t);
 			}
 			if (ent instanceof MCSheep) {
 				ret.set("color", new CString(((MCSheep) ent).getColor().name(), t), t);
 			}
 			return ret;
 		}
 
 		public String getName() {
 			return "get_entity_spec";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1};
 		}
 
 		public String docs() {
 			return "specsArray {entityID} Returns an array of information specific to this entitytype.";
 		}
 	}
 	
 	@api
 	public static class set_entity_spec extends DevFunction {
 
 		public ExceptionType[] thrown() {
 			// TODO Auto-generated method stub
 			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.BadEntityException};
 		}
 
 		public Construct exec(Target t, Environment environment,
 				Construct... args) throws ConfigRuntimeException {
 			MCEntity ent = Static.getEntity(Static.getInt32(args[0], t), t);
 			if (!(args[1] instanceof CArray)) {
 				throw new Exceptions.FormatException("Arg 2 was expected to be an array", t);
 			}
 			CArray ca = (CArray) args[1];
 			if (ent instanceof MCItem) {
 				((MCItem) ent).setItemStack(ObjectGenerator.GetGenerator().item(ca.get("itemstack", t), t));
 				((MCItem) ent).setPickupDelay(Static.getInt32(ca.get("pickupdelay", t), t));
 			}
 			if (ent instanceof MCFallingBlock) {
 				((MCFallingBlock) ent).setDropItem(Static.getBoolean(ca.get("dropitem", t)));
 			}
 			if (ent instanceof MCExperienceOrb) {
 				((MCExperienceOrb) ent).setExperience(Static.getInt32(ca.get("exp"), t));
 			}
 			if (ent instanceof MCLightningStrike) {
 				
 			}
 			if (ent instanceof MCTNT) {
 				
 			}
 			if (ent instanceof MCFireball) {
 				((MCFireball) ent).setDirection(ObjectGenerator.GetGenerator().velocity(ca.get("direction", t), t));
 			}
 			if (ent instanceof MCProjectile) {
 				((MCProjectile) ent).setShooter(Static.getLivingEntity(Static.getInt32(ca.get("shooter", t), t), t));
 				((MCProjectile) ent).setBounce(Static.getBoolean(ca.get("bounce", t)));
 			}
 			if (ent instanceof MCHanging) {
 				try {
 					((MCHanging) ent).setFacingDirection(MCBlockFace.valueOf(ca.get("facing", t).val()), true);
 				} catch (IllegalArgumentException iae) {
 					throw new Exceptions.FormatException("Not a valid blockface.", t);
 				}
 			}
 			if (ent instanceof MCWolf) {
 				try {
 					((MCWolf) ent).setCollarColor(MCDyeColor.valueOf(ca.get("color", t).val()));
 				} catch (IllegalArgumentException iae) {
 					throw new Exceptions.FormatException("Not a valid color.", t);
 				}
 				((MCWolf) ent).setAngry(Static.getBoolean(ca.get("angry", t)));
 				((MCWolf) ent).setSitting(Static.getBoolean(ca.get("sitting", t)));
 			}
 			if (ent instanceof MCOcelot) {
 				try {
 					((MCOcelot) ent).setCatType(MCOcelotType.valueOf(ca.get("cat", t).val()));
 				} catch (IllegalArgumentException iae) {
 					throw new Exceptions.FormatException("Not a valid cat type.", t);
 				}
 				((MCOcelot) ent).setSitting(Static.getBoolean(ca.get("sitting", t)));
 			}
 			if (ent instanceof MCEnderman) {
 				((MCEnderman) ent).setCarriedMaterial(Static.getInt32(ca.get("type", t), t), 
 						Static.getInt8(ca.get("data", t), t));
 			}
 			if (ent instanceof MCSheep) {
 				try {
 					((MCSheep) ent).setColor(MCDyeColor.valueOf(ca.get("color", t).val()));
 				} catch (IllegalArgumentException iae) {
 					throw new Exceptions.FormatException("Not a valid color.", t);
 				}
 			}
 			return new CVoid(t);
 		}
 
 		public String getName() {
 			return "set_entity_spec";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public String docs() {
 			return "void {entityID, specsArray} Sets properties on an entity. Nothing is optional atm.";
 		}
 	}
 	
 	@api
 	public static class sk_can_build extends DevFunction {
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PlayerOfflineException,
 					ExceptionType.FormatException, ExceptionType.InvalidWorldException};
 		}
 
 		public Construct exec(Target t, Environment environment,
 				Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("WorldGuard", t);
 			MCPlayer p = Static.GetPlayer(args[0], t);
 			MCLocation loc = ObjectGenerator.GetGenerator().location(args[1], p.getWorld(), t);
 			return new CBoolean(WGBukkit.getPlugin().canBuild((Player) p.getHandle(), (Location) loc.getHandle()), t);
 		}
 
 		public String getName() {
 			return "sk_can_build";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public String docs() {
 			return "boolean {player, locationArray} Returns whether or not player can build at the location,"
 					+ " according to WorldGuard.";
 		}
 	}
 	
 	private static Map<String,List<String>> funcs = new HashMap<String,List<String>>();
 	
	private static void initf() {
 		for (FunctionBase f : FunctionList.getFunctionList(api.Platforms.INTERPRETER_JAVA)) {
			String[] pack = f.getClass().getEnclosingClass().getName().split("\\.");
			String clazz = pack[pack.length - 1];
			if (!funcs.containsKey(clazz)) {
				funcs.put(clazz, new ArrayList<String>());
 			}
			funcs.get(clazz).add(f.getName());
 		}
 	}
 	
 	@api
 	public static class get_functions extends DevFunction {
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{};
 		}
 
 		public Construct exec(Target t, Environment environment,
 				Construct... args) throws ConfigRuntimeException {
 			CArray ret = CArray.GetAssociativeArray(t);
			if (funcs.keySet().size() < 10) {
				initf();
			}
 			for (String cname : funcs.keySet()) {
 				CArray fnames = new CArray(t);
 				for (String fname : funcs.get(cname)) {
 					fnames.push(new CString(fname, t));
 				}
 				ret.set(new CString(cname, t), fnames, t);
 			}
 			return ret;
 		}
 
 		public String getName() {
 			return "get_functions";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{0};
 		}
 
 		public String docs() {
 			return "array {} Returns an associative array of all loaded functions. The keys of this array are the"
 					+ " names of the classes containing the functions (which you know as the sections of the API page),"
 					+ " and the values are arrays of the names of the functions within those classes.";
 		}
 	}
 }
