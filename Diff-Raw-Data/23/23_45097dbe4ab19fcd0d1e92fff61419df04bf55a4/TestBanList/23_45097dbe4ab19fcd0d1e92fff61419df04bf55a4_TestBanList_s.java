 package com.awesomesauce.testapi;
 import net.minecraft.workbench.server.players.*;
 import java.util.Map;
 class TestBanList implements BanList
 {
	private boolean enabled;
	private Map<String, BanEntry> entries;
     public boolean isEnabled()
     {
     	return enabled;
     }
     public void setEnabled(boolean aenabled)
     {
     	enabled = aenabled;
     }
     public Map<String, BanEntry> getEntries()
     {
     	return entries;
     }
     public boolean isBanned(String name)
     {
     	if (entries.containsKey(name))
     	{
     		if (!(entries.get(name).hasExpired()))
     			return true;
     	}
     	return false;
     }
     public void add(BanEntry entry)
     {
     	entries.put(entry.getName(), entry);
     }
     public BanEntry remove(String name)
     {
     	BanEntry entry = entries.get(name);
     	entries.remove(name);
     	return entry;
     }
 }
