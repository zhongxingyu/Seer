 package com.wolvencraft.prison.mines.flags;
 
 import java.util.logging.Level;
 
 import com.wolvencraft.prison.util.Message;
 
 public enum FlagHandler {
 	
 	SurfaceOreFlag(SurfaceOreFlag.class, "surfaceore"),
 	EffectFlag(EffectFlag.class, "effect");
 	
 	FlagHandler(Class<?> clazz, String alias) {
 		this.clazz = clazz;
 		this.alias = alias;
 	}
 	
 	Class<?> clazz;
 	String alias;
 	
 	public BaseFlag dispatch() {
 		try {
 			return (BaseFlag) clazz.newInstance();
 		} catch (InstantiationException e) {
 			Message.log(Level.SEVERE, "Error instantiating a new flag! [InstantiationException]");
 			return null;
 		} catch (IllegalAccessException e) {
 			Message.log(Level.SEVERE, "Error instantiating a new flag! [IllegalAccessException]");
 			return null;
		} catch (NullPointerException npe) {
			Message.log(Level.SEVERE, "Error instantiating a new flag!  [NullPointerException]");
			return null;
		} catch (Exception ex) {
			Message.log(Level.SEVERE, "Error instantiating a new flag!  [Exception]");
			return null;
 		}
 	}
 	
 	public String getAlias() { return alias; }
 	
 	public static boolean exists(String alias) {
 		for(FlagHandler handler : values()) {
			Message.debug(handler.getAlias() + " =?= " + alias);
			if(handler.getAlias().equalsIgnoreCase(alias)) return true;
 		}
		Message.debug(alias + " does not match any handlers");
 		return false;
 	}
 	
 	public static FlagHandler get(String alias) {
 		for(FlagHandler handler : values()) {
			if(handler.getAlias().equalsIgnoreCase(alias)) return handler;
 		}
 		return null;
 	}
 }
