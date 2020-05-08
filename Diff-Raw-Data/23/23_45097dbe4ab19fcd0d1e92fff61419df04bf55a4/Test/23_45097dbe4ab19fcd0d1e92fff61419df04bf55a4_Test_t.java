 package com.awesomesauce.testapi;
 
 public class Test {
     private net.minecraft.workbench.WorkbenchImpl api;
     private net.minecraft.workbench.server.Server server;
     private boolean running;
     public Test()
     {
         api = new TestAPI(this);
         server = api.getServer();
        running = true;
     }
     public void importPlugins() {
         api.registerPlugin(new TestPlugin());
     }
     public void run() {
 	while (running)
 	{
 	    api.tickPlugins();
 	}
     }
 }
