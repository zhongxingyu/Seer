 package com.teammetallurgy.agriculture.nei;
 
 import codechicken.nei.api.API;
 import codechicken.nei.api.IConfigureNEI;
 
 public class NEIConfig implements IConfigureNEI
 {
 
     @Override
     public void loadConfig()
     {
        API.registerUsageHandler(new ProcessorHandler());
        API.registerRecipeHandler(new ProcessorHandler()); 
        
        API.registerUsageHandler(new CounterHandler());
        API.registerRecipeHandler(new CounterHandler()); 
     }
 
     @Override
     public String getName()
     {
         return "agriculture_nei_plugin";
     }
 
     @Override
     public String getVersion()
     {
         return "v1.0";
     }
 
 }
