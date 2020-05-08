 package com.rperce.compactstuff;
 
 import static com.rperce.compactstuff.CompactStuff.comCobArmorMaterial;
 import static com.rperce.compactstuff.CompactStuff.dioriteToolMaterial;
 import static com.rperce.compactstuff.CompactStuff.heatCarbArmorMaterial;
 import static com.rperce.compactstuff.CompactStuff.metCarbToolMaterial;
 import static com.rperce.compactstuff.CompactStuff.paxelMaterial;
 import static com.rperce.compactstuff.CompactStuff.pureCarbArmorMaterial;
 import static com.rperce.compactstuff.CompactStuff.steelToolMaterial;
 import static com.rperce.compactstuff.CompactStuff.wovnCarbArmorMaterial;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 
 import com.rperce.compactstuff.client.CSIcons;
 import com.rperce.compactstuff.tools.CompactAxe;
 import com.rperce.compactstuff.tools.CompactHoe;
 import com.rperce.compactstuff.tools.CompactPick;
 import com.rperce.compactstuff.tools.CompactSpade;
 import com.rperce.compactstuff.tools.CompactSword;
 import com.rperce.compactstuff.tools.Paxel;
 
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public enum Ref {
 	DIORITE_SWORD(9339, dioriteToolMaterial, "dioriteSword",	CompactSword.class),
 	DIORITE_PICK( 9340, dioriteToolMaterial, "dioritePick", 	CompactPick.class),
 	DIORITE_HOE(  9341, dioriteToolMaterial, "dioriteHoe",	 	CompactHoe.class),
 	DIORITE_AXE(  9342, dioriteToolMaterial, "dioriteAxe",		CompactAxe.class),
 	DIORITE_SPADE(9343, dioriteToolMaterial, "dioriteSpade",	CompactSpade.class),
 	
 	METCARB_SWORD(9344, metCarbToolMaterial, "metcarbSword",	CompactSword.class),
 	METCARB_PICK( 9345, metCarbToolMaterial, "metcarbPick",		CompactPick.class),
 	METCARB_HOE(  9346, metCarbToolMaterial, "metcarbHoe",		CompactHoe.class),
 	METCARB_AXE(  9347, metCarbToolMaterial, "metcarbAxe",		CompactAxe.class),
 	METCARB_SPADE(9348, metCarbToolMaterial, "metcarbSpade",	CompactSpade.class),
 	
 	STEEL_SWORD(9368, steelToolMaterial, "steelSword",		CompactSword.class),
 	STEEL_PICK( 9369, steelToolMaterial, "steelPick",		CompactPick.class),
 	STEEL_HOE(  9370, steelToolMaterial, "steelHoe",		CompactHoe.class),
 	STEEL_AXE(  9371, steelToolMaterial, "steelAxe",		CompactAxe.class),
 	STEEL_SPADE(9372, steelToolMaterial, "steelSpade",		CompactSpade.class),
 	
 	DIORITE_HELM( 9349, comCobArmorMaterial, "Diorite Armor", 0, "dioriteHelm",  ItemCompactArmor.class),
 	DIORITE_PLATE(9350, comCobArmorMaterial, "Diorite Armor", 1, "dioritePlate", ItemCompactArmor.class),
 	DIORITE_PANTS(9351, comCobArmorMaterial, "Diorite Armor", 2, "dioritePants", ItemCompactArmor.class),
 	DIORITE_BOOTS(9352, comCobArmorMaterial, "Diorite Armor", 3, "dioriteBoots", ItemCompactArmor.class),
 	
 	METCARB_HELM( 9353, heatCarbArmorMaterial, "Metcarb Armor", 0, "metcarbHelm",  ItemCompactArmor.class),
 	METCARB_PLATE(9354, heatCarbArmorMaterial, "Metcarb Armor", 1, "metcarbPlate", ItemCompactArmor.class),
 	METCARB_PANTS(9355, heatCarbArmorMaterial, "Metcarb Armor", 2, "metcarbPants", ItemCompactArmor.class),
 	METCARB_BOOTS(9356, heatCarbArmorMaterial, "Metcarb Armor", 3, "metcarbBoots", ItemCompactArmor.class),
 	
 	WOVEN_HELM( 9357, wovnCarbArmorMaterial, "Woven Carbon Armor", 0, "wovncarbHelm",  ItemCompactArmor.class),
 	WOVEN_PLATE(9358, wovnCarbArmorMaterial, "Woven Carbon Armor", 1, "wovncarbPlate", ItemCompactArmor.class),
 	WOVEN_PANTS(9359, wovnCarbArmorMaterial, "Woven Carbon Armor", 2, "wovncarbPants", ItemCompactArmor.class),
 	WOVEN_BOOTS(9360, wovnCarbArmorMaterial, "Woven Carbon Armor", 3, "wovncarbBoots", ItemCompactArmor.class),
 	
 	ADV_HELM( 9361, pureCarbArmorMaterial, "Advanced Carbon Armor", 0, "advcarbHelm",  ItemCompactArmor.class),
 	ADV_PLATE(9362, pureCarbArmorMaterial, "Advanced Carbon Armor", 1, "advcarbPlate", ItemCompactArmor.class),
 	ADV_PANTS(9363, pureCarbArmorMaterial, "Advanced Carbon Armor", 2, "advcarbPants", ItemCompactArmor.class),
 	ADV_BOOTS(9364, pureCarbArmorMaterial, "Advanced Carbon Armor", 3, "advcarbBoots", ItemCompactArmor.class),
 	
 	PAXEL(9365, paxelMaterial, CSIcons.PAXEL, Paxel.class);
 	
 	int id;
 	Item item;
 	Block block;
 	String name;
 	Constructor<?> ctor;
 	
 	private Object material;
 	private int aID;
 	private int rID;
 	private byte type;
 	private byte TOOL = 0, ARMOR = 1, BLOCK=4;
 	private HashMap<String, Integer> rIDs = new HashMap<String, Integer>();
 	
 	Ref(int id, EnumToolMaterial toolMat, String ident, Class<?> type) {
 		basic(id, ident);
 		try {
 			this.type = this.TOOL;
 			this.material = toolMat;
 			this.ctor = type.getConstructor(Integer.TYPE, EnumToolMaterial.class, String.class);
 		} catch(NoSuchMethodException nsme) {
 			System.out.println("ERROR: could not get constructor for id " + id);
 		}
 	}
 	
 	Ref(int id, EnumArmorMaterial armorMat, String render, int armorType, String ident, Class<?> type) {
 		basic(id, ident);
 		try {
 			this.type = this.ARMOR;
 			this.material = armorMat;
 			this.aID = armorType;
			if(this.rIDs.containsKey(render)) {
				this.rID = this.rIDs.get(render);
			} else {
				this.rID = RenderingRegistry.addNewArmourRendererPrefix(render);
				this.rIDs.put(render, this.rID);
			}
 			this.ctor = type.getConstructor(Integer.TYPE, EnumArmorMaterial.class, Integer.TYPE, Integer.TYPE, String.class);
 		} catch(NoSuchMethodException nsme) {
 			System.out.println("ERROR: could not get constructor for id " + id);
 		}
 	}
 	
 	private void basic(int id, String ident) {
 		this.id = id;
 		this.name = ident;
 	}
 	public void resolve(Configuration c) {
 		this.id = c.getItem(this.name, this.id).getInt();
 		
 		try {
 			if(this.type==this.TOOL)
 				this.item = ((Item)this.ctor.newInstance(this.id, this.material, this.name)).setUnlocalizedName(this.name);
 			else if(this.type==this.ARMOR)
 				this.item = ((Item)this.ctor.newInstance(this.id, this.material, this.rID, this.aID, this.name)).setUnlocalizedName(this.name);
 				
 			if(this.type==this.TOOL || this.type==this.ARMOR)
 				GameRegistry.registerItem(this.item, this.item.getUnlocalizedName());
 		} catch(InvocationTargetException e) {
 			System.out.println("ERROR: non-instantiatable class for id " + this.id);
 		} catch(IllegalAccessException e) {
 			System.out.println("ERROR: not allowed to instantiate item with id " + this.id);
 		} catch(InstantiationException e) {
 			System.out.println("ERROR: could not instantiate item with id " + this.id);
 		}
 	}
 	
 	public int id() { return this.id; }
 	public Item getItem() { return this.item; }
 	public Block getBlock() { return this.block; }
 	public String unlocal() { 
 		if(this.type==this.BLOCK)
 			return this.block.getUnlocalizedName();
 		return this.item.getUnlocalizedName();
 	}
 	public ItemStack stack() {
 		if(this.type==this.BLOCK)
 			return new ItemStack(this.block);
 		return new ItemStack(this.item);
 	}
 	
 	public static boolean matches(String name, Ref... check) {
 		for(Ref r : check) {
 			if(name.equals(r.unlocal()))
 				return true;
 		}
 		return false;
 	}
 	
 	public static boolean matches(Item item, Ref... check) {
 		return matches(item.getUnlocalizedName(), check);
 	}
 	
 	public static boolean matches(ItemStack stack, Ref... check) {
 		return matches(stack.getItem(), check);
 	}
 }
