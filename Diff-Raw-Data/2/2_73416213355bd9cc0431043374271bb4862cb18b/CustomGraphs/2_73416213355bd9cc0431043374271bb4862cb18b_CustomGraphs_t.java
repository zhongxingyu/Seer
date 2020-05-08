 package com.carlgo11.simpleautomessage.metrics;
 
 import com.carlgo11.simpleautomessage.Main;
 
 public class CustomGraphs {
 
     public static void graphs(Metrics metrics, Main Main)
     { // Custom Graphs. Sends data to mcstats.org
         try {
             //Graph1
            Metrics.Graph graph1 = metrics.createGraph("Messages"); //Sends data about how many msg strings the server has.
             int o = 0;
             for (int i = 1; Main.getConfig().contains("msg" + i); i++) {
                 o = i;
             }
             graph1.addPlotter(new SimplePlotter("" + o));
 
             //graph2
             Metrics.Graph graph2 = metrics.createGraph("auto-update"); //Sends auto-update data. if auto-update: is true it returns 'enabled'.
             if (Main.getConfig().getBoolean("auto-update") == true) {
                 graph2.addPlotter(new SimplePlotter("enabled"));
             } else {
                 graph2.addPlotter(new SimplePlotter("disabled"));
             }
 
             //Graph3
             Metrics.Graph graph3 = metrics.createGraph("language");
             if (Main.getConfig().getString("language").equalsIgnoreCase("EN") || Main.getConfig().getString("language").isEmpty()) {
                 graph3.addPlotter(new SimplePlotter("English"));
             }
             if (Main.getConfig().getString("language").equalsIgnoreCase("FR")) {
                 graph3.addPlotter(new SimplePlotter("French"));
             }
             if (Main.getConfig().getString("language").equalsIgnoreCase("NL")) {
                 graph3.addPlotter(new SimplePlotter("Dutch"));
             }
             if (Main.getConfig().getString("language").equalsIgnoreCase("SE")) {
                 graph3.addPlotter(new SimplePlotter("Swedish"));
             }
             if (!Main.getConfig().getString("language").equalsIgnoreCase("EN") && !Main.getConfig().getString("language").equalsIgnoreCase("FR") && !Main.getConfig().getString("language").equalsIgnoreCase("NL") && !Main.getConfig().getString("language").equalsIgnoreCase("SE")) {
                 graph3.addPlotter(new SimplePlotter("Other"));
             }
 
             //Graph4
             Metrics.Graph graph4 = metrics.createGraph("min-players");
             graph4.addPlotter(new SimplePlotter("" + Main.getConfig().getInt("min-players")));
 
             //Graph5
             Metrics.Graph graph5 = metrics.createGraph("random");
             if (Main.getConfig().getBoolean("random")) {
                 graph5.addPlotter(new SimplePlotter("enabled"));
             } else {
                 graph5.addPlotter(new SimplePlotter("disabled"));
             }
 
             //Graph6
             Metrics.Graph graph6 = metrics.createGraph("warn-update");
             if (!Main.getConfig().getBoolean("auto-update")) {
                 if (Main.getConfig().getString("warn-update0").equalsIgnoreCase("op")) {
                     graph6.addPlotter(new SimplePlotter("op"));
                 } else if (Main.getConfig().getString("warn-update").equalsIgnoreCase("perm")) {
                     graph6.addPlotter(new SimplePlotter("perm"));
                 } else {
                     graph6.addPlotter(new SimplePlotter("none"));
                 }
             }
             Main.debug("Sending metrics data...");
             metrics.start();
         } catch (Exception e) {
             Main.logger.warning(e.getMessage());
         }
     }
 }
