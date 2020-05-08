 package com.awesomesauce.testapi;
 
 import net.minecraft.workbench.Workbench;
 import net.minecraft.workbench.plugin.Plugin;
 
 public class TestPlugin extends Plugin {
 
     @Override
     public String getName() {
 	return "TestPlugin";
     }
     public void tick()
     {
 	Workbench.getServer().setBanList(new TestBanList());
 	Workbench.getServer().getBanList().add(new TestBanEntry("TestPlayer"));
     }
 
 }
