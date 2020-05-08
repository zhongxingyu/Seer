 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.io.ItemMap;
 
 /**
  * Material list for items
  * 
  * @author turt2live
  */
 public class ASMaterialList{
 
 	public static class ASMaterial{
 		public int id = 0;
 		public short data = -1; // -1 = any
 		public String name = "Unknown";
 	}
 
 	private Map<Integer, List<ASMaterial>> listing = new HashMap<Integer, List<ASMaterial>>();
 
 	/**
 	 * Creates a new material list
 	 * 
 	 * @param strings the list of strings
 	 */
 	public ASMaterialList(List<?> strings){
 		if(strings == null){
 			throw new IllegalArgumentException("Null arguments are not allowed");
 		}
 		AntiShare p = AntiShare.p;
 		for(Object o : strings){
 			if(!(o instanceof String)){
 				continue;
 			}
 			String s = (String) o;
 			s = s.trim();
 			String testString = s.toLowerCase().replace(" ", "");
 			boolean negate = s.startsWith("-");
 			s = negate ? (s.replaceFirst("-", "").trim()) : s;
 			if(s.equalsIgnoreCase("all")){
 				for(Material m : Material.values()){
 					ASMaterial asm = new ASMaterial();
 					asm.id = m.getId();
 					asm.data = -1;
 					asm.name = m.name();
 					add(asm, negate);
 				}
 				continue;
 			}else if(s.equalsIgnoreCase("none")){
 				listing.clear();
 				continue;
 			}else if(testString.equalsIgnoreCase("furnace") || testString.equalsIgnoreCase("burningfurnace")
 					|| testString.equalsIgnoreCase(String.valueOf(Material.FURNACE.getId()))
 					|| testString.equalsIgnoreCase(String.valueOf(Material.BURNING_FURNACE.getId()))){
 				add(Material.FURNACE, negate);
 				add(Material.BURNING_FURNACE, negate);
 				continue;
 			}else if(testString.equalsIgnoreCase("sign") || testString.equalsIgnoreCase("wallsign") || testString.equalsIgnoreCase("signpost")
 					|| testString.equalsIgnoreCase(String.valueOf(Material.SIGN.getId()))
 					|| testString.equalsIgnoreCase(String.valueOf(Material.WALL_SIGN.getId()))
 					|| testString.equalsIgnoreCase(String.valueOf(Material.SIGN_POST.getId()))){
 				add(Material.SIGN, negate);
 				add(Material.WALL_SIGN, negate);
 				add(Material.SIGN_POST, negate);
 				continue;
 			}else if(testString.equalsIgnoreCase("brewingstand") || testString.equalsIgnoreCase("brewingstanditem")
 					|| testString.equalsIgnoreCase(String.valueOf(Material.BREWING_STAND.getId()))
 					|| testString.equalsIgnoreCase(String.valueOf(Material.BREWING_STAND_ITEM.getId()))){
 				add(Material.BREWING_STAND, negate);
 				add(Material.BREWING_STAND_ITEM, negate);
 				continue;
			}else if(testString.equalsIgnoreCase("enderportal") || testString.equalsIgnoreCase("enderportalframe")
 					|| testString.equalsIgnoreCase(String.valueOf(Material.ENDER_PORTAL.getId()))
 					|| testString.equalsIgnoreCase(String.valueOf(Material.ENDER_PORTAL_FRAME.getId()))){
 				add(Material.ENDER_PORTAL, negate);
 				add(Material.ENDER_PORTAL_FRAME, negate);
 				continue;
 			}else if(testString.equalsIgnoreCase("skull") || testString.equalsIgnoreCase("skullitem") || testString.equalsIgnoreCase("mobskull")
 					|| testString.equalsIgnoreCase(String.valueOf(Material.SKULL.getId()))
 					|| testString.equalsIgnoreCase(String.valueOf(Material.SKULL_ITEM.getId()))){
 				add(Material.SKULL, negate);
 				add(Material.SKULL_ITEM, negate);
 				continue;
 			}
 			ASMaterial asm = ItemMap.get(s);
 			if(asm == null){
 				p.getLogger().warning(p.getMessages().getMessage("unknown-material", s));
 				continue;
 			}
 			add(asm, negate);
 		}
 	}
 
 	void add(ASMaterial m, boolean negate){
 		List<ASMaterial> materials = new ArrayList<ASMaterial>();
 		if(negate && m.data < 0){
 			listing.remove(m.id);
 			return;
 		}
 		if(!negate){
 			materials.add(m);
 		}
 		if(listing.containsKey(m.id)){
 			if(negate){
 				for(ASMaterial m2 : listing.get(m.id)){
 					if(m.data != m2.data){
 						materials.add(m2);
 					}
 				}
 			}else{
 				materials.addAll(listing.get(m.id));
 			}
 		}
 		listing.put(m.id, materials);
 	}
 
 	void add(Material m, boolean negate){
 		ASMaterial asm = new ASMaterial();
 		asm.id = m.getId();
 		asm.data = -1;
 		asm.name = m.name();
 		add(asm, negate);
 	}
 
 	/**
 	 * Determines if this list has the material. Use {@link #has(Block)} or {@link #has(ItemStack)} where possible as this does not check the data value
 	 * 
 	 * @param material the material
 	 * @return true if found
 	 */
 	public boolean has(Material material){
 		if(material == null){
 			return false;
 		}
 		return listing.containsKey(material.getId());
 	}
 
 	/**
 	 * Determines if a block is contained in this list (item ID and data)
 	 * 
 	 * @param block the block
 	 * @return true if found
 	 */
 	public boolean has(Block block){
 		if(block == null){
 			return false;
 		}
 		Material material = block.getType();
 		short data = block.getData();
 		return find(material, data);
 	}
 
 	/**
 	 * Determines if a item is contained in this list (item ID and data)
 	 * 
 	 * @param item the item
 	 * @return true if found
 	 */
 	public boolean has(ItemStack item){
 		if(item == null){
 			return false;
 		}
 		Material material = item.getType();
 		short data = item.getDurability();
 		return find(material, data);
 	}
 
 	private boolean find(Material material, short data){
 		List<ASMaterial> asMaterials = listing.get(material.getId());
 		if(asMaterials == null){
 			return false;
 		}
 		for(ASMaterial m : asMaterials){
 			if(m.id == material.getId() && (m.data == data || m.data < 0)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
