 package com.ftwinston.KillerMinecraft.Configuration;
 
 import java.util.Arrays;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import com.ftwinston.KillerMinecraft.Option;
 
 public class NumericOption extends Option
 {
 	public NumericOption(String name, int min, int max, Material icon, int defaultVal)
 	{
 		super(name);
 		
 		if ( max < min )
 		{
 			this.min = max;
 			this.max = min;
 		}
 		else
 		{
 			this.min = min;
 			this.max = max;
 		}
 
 		int val = Math.min(max, Math.max(min, defaultVal));
		setSelectedIndex(val-min);
			
 		this.icon = icon;
 	}
 	
 	private int min, max, value;
 	private Material icon;
 	
 	protected void setValue(int newVal)
 	{
 		setSelectedIndex(value-min);
 	}
 	
 	@Override
 	protected void setSelectedIndex(int index)
 	{
 		super.setSelectedIndex(index);
 		value = index + min;
 	}
 	
 	@Override
 	protected Material getDisplayMaterial() { return icon; }
 
 	@Override
 	protected String[] getDescription() {
		return new String[] { ChatColor.YELLOW + "Current value: " + getValue(), "<no description available>" };
 	}
 
 	@Override
 	protected boolean trySetValue(String value)
 	{
 		int val = Integer.parseInt(value);
 		
 		if ( val >= min && val <= max )
 		{
 			setValue(val);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	protected String getValueString()
 	{
 		return Integer.toString(getValue());
 	}
 	
 	public int getValue()
 	{
 		return value;
 	}
 	
 	final int maxNumItems = 34; 
 	
     @Override
     public ItemStack[] optionClicked()
     {
     	int numItems = Math.min(max - min + 1, maxNumItems);
     	ItemStack[] items = new ItemStack[numItems];
     	
     	for ( int i=0; i<numItems; i++ )
     	{
     		ItemStack item = new ItemStack(Material.REDSTONE_TORCH_ON);
     		
     		ItemMeta meta = item.getItemMeta();
     		
     		meta.setDisplayName(ChatColor.RESET + "" + (i+min));
     		
     		if ( i == getSelectedIndex() )
     			meta.setLore(Arrays.asList("" + ChatColor.YELLOW + ChatColor.ITALIC + "Current value", "<value descriptions not yet implemented>"));
     		else
     			meta.setLore(Arrays.asList("<value descriptions not yet implemented>"));
     		
     		item.setItemMeta(meta);
     		
     		items[i] = item;
     	}
     	
     	return items;
     }
 }
