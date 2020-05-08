 package me.KeybordPiano459.Newspaper;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.Proxy;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 import java.util.UUID;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.scheduler.BukkitTask;
 
 public class BukkitMetrics {
     
     private final static int REVISION = 6;
     private static final String BASE_URL = "http://mcstats.org";
     private static final String REPORT_URL = "/report/%s";
     private static final String CUSTOM_DATA_SEPARATOR = "~~";
     private static final int PING_INTERVAL = 10;
     private final Plugin plugin;
     private final Set<Graph> graphs = Collections.synchronizedSet(new HashSet<Graph>());
     private final Graph defaultGraph = new Graph("Default");
     private final YamlConfiguration configuration;
     private final File configurationFile;
     private final String guid;
     private final boolean debug;
     private final Object optOutLock = new Object();
     private volatile BukkitTask task = null;
 
     public BukkitMetrics(final Plugin plugin) throws IOException {
         if (plugin == null) {
             throw new IllegalArgumentException("Plugin cannot be null");
         }
 
         this.plugin = plugin;
 
         configurationFile = getConfigFile();
         configuration = YamlConfiguration.loadConfiguration(configurationFile);
 
         configuration.addDefault("opt-out", false);
         configuration.addDefault("guid", UUID.randomUUID().toString());
         configuration.addDefault("debug", false);
 
         if (configuration.get("guid", null) == null) {
             configuration.options().header("http://mcstats.org").copyDefaults(true);
             configuration.save(configurationFile);
         }
 
         guid = configuration.getString("guid");
         debug = configuration.getBoolean("debug", false);
     }
 
     public Graph createGraph(final String name) {
         if (name == null) {
             throw new IllegalArgumentException("Graph name cannot be null");
         }
 
         final Graph graph = new Graph(name);
         graphs.add(graph);
         return graph;
     }
 
     public void addGraph(final Graph graph) {
         if (graph == null) {
             throw new IllegalArgumentException("Graph cannot be null");
         }
 
         graphs.add(graph);
     }
 
     public void addCustomData(final Plotter plotter) {
         if (plotter == null) {
             throw new IllegalArgumentException("Plotter cannot be null");
         }
 
         defaultGraph.addPlotter(plotter);
         graphs.add(defaultGraph);
     }
 
     public boolean start() {
         synchronized (optOutLock) {
             if (isOptOut()) {
                 return false;
             }
 
             if (task != null) {
                 return true;
             }
 
             task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
 
                 private boolean firstPost = true;
 
                 @Override
                 public void run() {
                     try {
                         synchronized (optOutLock) {
                             if (isOptOut() && task != null) {
                                 task.cancel();
                                 task = null;
                                 for (Graph graph : graphs) {
                                     graph.onOptOut();
                                 }
                             }
                         }
 
                         postPlugin(!firstPost);
                         firstPost = false;
                     } catch (IOException e) {
                         if (debug) {
                             Bukkit.getLogger().log(Level.INFO, "[Metrics] {0}", e.getMessage());
                         }
                     }
                 }
             }, 0, PING_INTERVAL * 1200);
 
             return true;
         }
     }
 
     public boolean isOptOut() {
         synchronized (optOutLock) {
             try {
                 configuration.load(getConfigFile());
            } catch (IOException e) {
                if (debug) Bukkit.getLogger().log(Level.INFO, "[Metrics] {0}", e.getMessage());
                return true;
            } catch (InvalidConfigurationException e) {
                if (debug) Bukkit.getLogger().log(Level.INFO, "[Metrics] {0}", e.getMessage());
                 return true;
             }
             return configuration.getBoolean("opt-out", false);
         }
     }
 
     public void enable() throws IOException {
         synchronized (optOutLock) {
             if (isOptOut()) {
                 configuration.set("opt-out", false);
                 configuration.save(configurationFile);
             }
 
             if (task == null) {
                 start();
             }
         }
     }
 
     public void disable() throws IOException {
         synchronized (optOutLock) {
             if (!isOptOut()) {
                 configuration.set("opt-out", true);
                 configuration.save(configurationFile);
             }
 
             if (task != null) {
                 task.cancel();
                 task = null;
             }
         }
     }
 
     private File getConfigFile() {
         File pluginsFolder = plugin.getDataFolder().getParentFile();
         return new File(new File(pluginsFolder, "PluginMetrics"), "config.yml");
     }
 
     private void postPlugin(final boolean isPing) throws IOException {
         PluginDescriptionFile description = plugin.getDescription();
         String pluginName = description.getName();
         boolean onlineMode = Bukkit.getServer().getOnlineMode();
         String pluginVersion = description.getVersion();
         String serverVersion = Bukkit.getVersion();
         int playersOnline = Bukkit.getServer().getOnlinePlayers().length;
 
         final StringBuilder data = new StringBuilder();
         data.append(encode("guid")).append('=').append(encode(guid));
         encodeDataPair(data, "version", pluginVersion);
         encodeDataPair(data, "server", serverVersion);
         encodeDataPair(data, "players", Integer.toString(playersOnline));
         encodeDataPair(data, "revision", String.valueOf(REVISION));
 
         String osname = System.getProperty("os.name");
         String osarch = System.getProperty("os.arch");
         String osversion = System.getProperty("os.version");
         String java_version = System.getProperty("java.version");
         int coreCount = Runtime.getRuntime().availableProcessors();
 
         if (osarch.equals("amd64")) {
             osarch = "x86_64";
         }
 
         encodeDataPair(data, "osname", osname);
         encodeDataPair(data, "osarch", osarch);
         encodeDataPair(data, "osversion", osversion);
         encodeDataPair(data, "cores", Integer.toString(coreCount));
         encodeDataPair(data, "online-mode", Boolean.toString(onlineMode));
         encodeDataPair(data, "java_version", java_version);
 
         if (isPing) {
             encodeDataPair(data, "ping", "true");
         }
         
         synchronized (graphs) {
             final Iterator<Graph> iter = graphs.iterator();
 
             while (iter.hasNext()) {
                 final Graph graph = iter.next();
 
                 for (Plotter plotter : graph.getPlotters()) {
                     final String key = String.format("C%s%s%s%s", CUSTOM_DATA_SEPARATOR, graph.getName(), CUSTOM_DATA_SEPARATOR, plotter.getColumnName());
                     final String value = Integer.toString(plotter.getValue());
                     encodeDataPair(data, key, value);
                 }
             }
         }
 
         URL url = new URL(BASE_URL + String.format(REPORT_URL, encode(pluginName)));
         URLConnection connection;
         if (isMineshafterPresent()) {
             connection = url.openConnection(Proxy.NO_PROXY);
         } else {
             connection = url.openConnection();
         }
 
         connection.setDoOutput(true);
         final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
         writer.write(data.toString());
         writer.flush();
 
         final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         final String response = reader.readLine();
 
         writer.close();
         reader.close();
 
         if (response == null || response.startsWith("ERR")) {
             throw new IOException(response);
         } else {
             if (response.contains("OK This is your first update this hour")) {
                 synchronized (graphs) {
                     final Iterator<Graph> iter = graphs.iterator();
 
                     while (iter.hasNext()) {
                         final Graph graph = iter.next();
 
                         for (Plotter plotter : graph.getPlotters()) {
                             plotter.reset();
                         }
                     }
                 }
             }
         }
     }
 
     private boolean isMineshafterPresent() {
         try {
             Class.forName("mineshafter.MineServer");
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
     private static void encodeDataPair(final StringBuilder buffer, final String key, final String value) throws UnsupportedEncodingException {
         buffer.append('&').append(encode(key)).append('=').append(encode(value));
     }
 
     private static String encode(final String text) throws UnsupportedEncodingException {
         return URLEncoder.encode(text, "UTF-8");
     }
 
     public static class Graph {
         
         private final String name;
         private final Set<Plotter> plotters = new LinkedHashSet<Plotter>();
 
         private Graph(final String name) {
             this.name = name;
         }
 
         public String getName() {
             return name;
         }
 
         public void addPlotter(final Plotter plotter) {
             plotters.add(plotter);
         }
 
         public void removePlotter(final Plotter plotter) {
             plotters.remove(plotter);
         }
 
         public Set<Plotter> getPlotters() {
             return Collections.unmodifiableSet(plotters);
         }
 
         @Override
         public int hashCode() {
             return name.hashCode();
         }
 
         @Override
         public boolean equals(final Object object) {
             if (!(object instanceof Graph)) {
                 return false;
             }
 
             final Graph graph = (Graph) object;
             return graph.name.equals(name);
         }
 
         protected void onOptOut() {
         }
     }
 
     public static abstract class Plotter {
         
         private final String name;
 
         public Plotter() {
             this("Default");
         }
 
         public Plotter(final String name) {
             this.name = name;
         }
 
         public abstract int getValue();
 
         public String getColumnName() {
             return name;
         }
 
         public void reset() {
         }
 
         @Override
         public int hashCode() {
             return getColumnName().hashCode();
         }
 
         @Override
         public boolean equals(final Object object) {
             if (!(object instanceof Plotter)) {
                 return false;
             }
 
             final Plotter plotter = (Plotter) object;
             return plotter.name.equals(name) && plotter.getValue() == getValue();
         }
     }
}
