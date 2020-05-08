 
 package org.ss.shop;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.material.MaterialData;
 
 public class ShopEntry
 		implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	public String unit_name;
 	public byte unit_data;
 	public HashMap< Integer, Integer > unit_enchantments = new HashMap< Integer, Integer >();;
 	public short unit_durability;
 
 	public int units_in_stock;
 	public int units_wanted;
 	public double cost_to_buy_unit = -1;
 	public double cost_to_sell_unit = -1;
 
 	public ShopEntry( ItemStack stack ) {
 		SpoutItemStack sis = new SpoutItemStack( stack );
 
 		unit_name = sis.getMaterial().getNotchianName();
 		unit_durability = stack.getDurability();
 		unit_data = stack.getData().getData();
 
 		Map< Enchantment, Integer > enchantments = stack.getEnchantments();
 		for ( Enchantment encha : enchantments.keySet() ) {
 			unit_enchantments.put( encha.getId(), enchantments.get( encha ) );
 		}
 
 		units_in_stock = stack.getAmount();
 	}
 
 	public SpoutItemStack createItemStack() {
 		SpoutItemStack sis = new SpoutItemStack( MaterialData.getMaterial( unit_name ) );
 		sis.setDurability( unit_durability );
 
 		for ( Integer eid : unit_enchantments.keySet() ) {
 			sis.addEnchantment( Enchantment.getById( eid ), unit_enchantments.get( eid ) );
 		}
 
 		return sis;
 	}
 
 	public boolean matchesString( String criteria ) {
 		if ( criteria == null )
 			return false;
 
 		return unit_name.equalsIgnoreCase( criteria );
 	}
 
 	public boolean hasInfiniteStock() {
 		return units_in_stock == -1;
 	}
 
 	public boolean hasInfiniteDemand() {
 		return units_wanted == -1;
 	}
 
 	public boolean equals( Object o ) {
 		if ( o == null )
 			return false;
 
 		if ( o == this )
 			return true;
 
 		if ( o instanceof ItemStack ) {
 			SpoutItemStack sis = new SpoutItemStack( ( ItemStack ) o );
 
			if ( unit_name.equals( sis.getMaterial().getNotchianName() ) )
 				return false;
 
 			if ( unit_durability != sis.getDurability() )
 				return false;
 
 			if ( unit_data != sis.getData().getData() )
 				return false;
 
 			Map< Enchantment, Integer > sis_enc = sis.getEnchantments();
 
 			if ( unit_enchantments.size() != sis_enc.size() )
 				return false;
 
 			if ( sis_enc.size() != 0 ) {
 				for ( Enchantment enc : sis_enc.keySet() ) {
 					if ( !unit_enchantments.containsKey( enc ) )
 						return false;
 
 					if ( unit_enchantments.get( enc ) != sis_enc.get( enc ) )
 						return false;
 				}
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 }
