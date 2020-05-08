 package com.turt2live.antishare.tekkitcompat;
 
 import org.bukkit.entity.Entity;
 
 public class EntityLayer {
 
 	public static boolean isEntity(Entity entity, String name){
		return entity.getClass().getName().equals("org.bukkit.entity." + name);
 	}
 
 }
