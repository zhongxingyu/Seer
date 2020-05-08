 package com.ftwinston.KillerMinecraft.Configuration;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 import com.ftwinston.KillerMinecraft.Option;
 
 public class ToggleOption extends Option
 {
 	public ToggleOption(String name, boolean enabledByDefault)
 	{
 		super(name);
 		setEnabled(enabledByDefault);
 	}
 
 	@Override
 	protected Material getDisplayMaterial() { return enabled ? Material.LAVA_BUCKET : Material.BUCKET; }
 	
 	@Override
 	protected String[] getDescription()
 	{
		return new String[] { isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled", "<option descriptions not yet implemented>" };
 	}
 	
 	@Override
 	protected boolean trySetValue(String value)
 	{
 		setEnabled(Boolean.parseBoolean(value));
 		return true;
 	}
 	
 	@Override
 	protected String getValueString()
 	{
 		return isEnabled() ? "1" : "0";
 	}
 	
 	@Override
 	protected ItemStack[] optionClicked() { toggle(); return null; }
 	
 	private boolean enabled;
 	public boolean isEnabled() { return enabled; }
 	protected void setEnabled(boolean enabled) { this.enabled = enabled; setSelectedIndex(enabled ? 1 : 0); }
 	
 	public void toggle()
 	{
 		if ( dontDisableIfTheseDisabled != null && isEnabled() )
 		{
 			boolean anyEnabled = false;
 			for ( ToggleOption option : dontDisableIfTheseDisabled )
 				if ( option.isEnabled() )
 				{
 					anyEnabled = true;
 					break;
 				}
 			
 			if (!anyEnabled)
 				return;
 		}
 		
 		setEnabled(!enabled);
 		
 		if (disableWhenThisEnabled != null && isEnabled())
 			for ( ToggleOption option : disableWhenThisEnabled )
 				option.setEnabled(false);
 	}
 		
 	private ArrayList<ToggleOption> disableWhenThisEnabled;
 	private ArrayList<ToggleOption> dontDisableIfTheseDisabled;
 					
 	public static void ensureOnlyOneEnabled(ToggleOption... options)
 	{
 		for ( ToggleOption option : options ) 
 		{
 			option.disableWhenThisEnabled = new ArrayList<ToggleOption>();
 			
 			for ( ToggleOption other : options )
 				if ( other != option )
 					option.disableWhenThisEnabled.add(other);
 		}
 		
 		ensureAtLeastOneEnabled(options);
 	}
 	
 	public static void ensureAtLeastOneEnabled(ToggleOption... options)
 	{
 		for ( ToggleOption option : options ) 
 		{
 			option.dontDisableIfTheseDisabled = new ArrayList<ToggleOption>();
 			
 			for ( ToggleOption other : options )
 				if ( other != option )
 					option.dontDisableIfTheseDisabled.add(other);
 		}		
 	}
 }
