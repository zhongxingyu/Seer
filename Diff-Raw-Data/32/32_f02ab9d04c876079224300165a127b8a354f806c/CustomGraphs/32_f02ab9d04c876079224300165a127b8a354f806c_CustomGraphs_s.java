 package com.carlgo11.preventip.mcstats;
 
 import com.carlgo11.preventip.Main;
 
 public class CustomGraphs {
 
     public static void graphs(Metrics metrics, Main main)
     {
         graph0(metrics, main);
         graph1(metrics, main);
         metrics.start();
     }
 
     static void graph0(Metrics metrics, Main main)
     {
         Metrics.Graph graph = metrics.createGraph("auto-update");
         boolean au = main.getConfig().getBoolean("auto-update");
         if (au) {
             graph.addPlotter(new SimplePlotter("enabled"));
         } else {
             graph.addPlotter(new SimplePlotter("disabled"));
         }
         main.debug("graph0 sent");
     }
     
     static void graph1(Metrics metrics, Main main){
         Metrics.Graph graph = metrics.createGraph("action");
         String ac = main.getConfig().getString("action");
         if (ac.equalsIgnoreCase("warn")) {
             graph.addPlotter(new SimplePlotter("Warn"));
         } else if(ac.equalsIgnoreCase("kick")){
             graph.addPlotter(new SimplePlotter("Kick"));
         }else if(ac.equalsIgnoreCase("ban")){
             graph.addPlotter(new SimplePlotter("Ban"));
         }else if(ac.equalsIgnoreCase("custom")){
             graph.addPlotter(new SimplePlotter("Custom"));
         }else{
             graph.addPlotter(new SimplePlotter("Other"));
         }
         main.debug("graph1 sent");
     }
     static void graph2(Metrics metrics, Main main)
     {
         Metrics.Graph graph = metrics.createGraph("block-ip");
         boolean au = main.getConfig().getBoolean("block-ip");
         if (au) {
             graph.addPlotter(new SimplePlotter("enabled"));
         } else {
             graph.addPlotter(new SimplePlotter("disabled"));
         }
        main.debug("graph0 sent");
     }
     
     static void graph3(Metrics metrics, Main main)
     {
         Metrics.Graph graph = metrics.createGraph("block-hostname");
         boolean au = main.getConfig().getBoolean("block-ip");
         if (au) {
             graph.addPlotter(new SimplePlotter("enabled"));
         } else {
             graph.addPlotter(new SimplePlotter("disabled"));
         }
        main.debug("graph0 sent");
     }
     
 }
