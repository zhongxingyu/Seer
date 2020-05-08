 /*
 Ziah_'s Client
 Copyright (C) 2013  Ziah Jyothi
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
 
 package com.oneofthesevenbillion.ziah.ZiahsClient;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import net.minecraft.src.BaseMod;
 import net.minecraft.src.GuiButton;
 import net.minecraft.src.GuiSmallButton;
 import net.minecraft.src.KeyBinding;
 import net.minecraft.src.Minecraft;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.NetClientHandler;
 import net.minecraft.src.Packet250CustomPayload;
 import net.minecraft.src.ResourceLocation;
 
 import org.lwjgl.opengl.Display;
 
 import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventAddRenderers;
 import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventBus;
 import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventChat;
 import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventClientConnect;
 import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventClientDisconnect;
 import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventCustomPayload;
 import com.oneofthesevenbillion.ziah.ZiahsClient.event.EventLoad;
 import com.oneofthesevenbillion.ziah.ZiahsClient.gui.GuiModList;
 import com.oneofthesevenbillion.ziah.ZiahsClient.network.PacketManager;
 import com.oneofthesevenbillion.ziah.ZiahsClient.network.PacketRegistry;
 
 public class ZiahsClient {
     private static ZiahsClient instance;
     private final String modAltName = "ZiahsClient";
     private String msgPrefix;
     private File dataDir;
     private Logger logger;
     private File logFile;
     private FileHandler logFileHandler;
     private Formatter logFormatter;
     private Config config;
     private final BaseMod modClass;
     private EventBus eventBus;
     private GuiButton menuButton;
     private Map<KeyBinding, Object> keybindings = new HashMap<KeyBinding, Object>();
     private Map<KeyBinding, Boolean> lastKeybindings = new HashMap<KeyBinding, Boolean>();
     private String runningMCVersion;
     private List<GuiButton> menuButtons = new ArrayList<GuiButton>();
     private Map<Integer, Method> menuButtonIdToMethod = new HashMap<Integer, Method>();
     private Map<Integer, Object> menuButtonIdToClickMethodObject = new HashMap<Integer, Object>();
     private int nextMenuId = 1;
     private String version;
     private String mcversion;
 
     public ZiahsClient(BaseMod modClass) {
     	// Sets the static instance variable to this
         ZiahsClient.instance = this;
 
         this.modClass = modClass;
 
         // Instantiate the event bus
         this.eventBus = new EventBus();
 
         // Instantiate the PacketRegistry to register all the packets
         new PacketRegistry();
     }
 
     public void load() {
     	// Reads the version from the version file
     	InputStream versionFileIn = this.getClass().getClassLoader().getResourceAsStream("version.txt");
     	if (versionFileIn != null) {
         	BufferedReader versionFileReader = new BufferedReader(new InputStreamReader(versionFileIn));
     		try {
 				this.version = versionFileReader.readLine();
 	    		this.mcversion = versionFileReader.readLine();
 			} catch (IOException e) {
 				System.err.println("[ZiahsClient] ERROR: Exception when reading version file!");
 			}
     	}
 
     	// Gets the running version of Minecraft and quits if it's the wrong one
         this.runningMCVersion = ModLoader.VERSION.split(" ")[1];
         if (!this.runningMCVersion.equalsIgnoreCase(this.getMinecraftVersion())) {
             System.err.println("INCORRECT MINECRAFT VERSION " + this.runningMCVersion + " EXPECTED " + this.getMinecraftVersion() + "!!!");
             return;
         }
 
         // Sets the window title
         Display.setTitle(Display.getTitle() + " with Ziah_'s Client v" + this.getVersion());
 
         // Finds the data directory or creates it
         if ((new File("options.txt")).exists()) {
             this.dataDir = new File("." + File.separator + "ZiahsClient" + File.separator);
         }else{
             this.dataDir = new File(Minecraft.getMinecraft().mcDataDir, "ZiahsClient");
         }
         if (!this.dataDir.exists()) {
             if (!this.dataDir.mkdirs() && (!this.dataDir.exists() || !this.dataDir.isDirectory())) {
                 System.err.println("[ZiahsClient] ERROR: Unable to create data directory, unloading!");
                 return;
             }
         }
 
         // Sets up the logger
         this.logger = Logger.getLogger(this.modAltName);
         try {
             this.logFile = new File(this.dataDir, "ziahsclient.log");
             this.logFileHandler = new FileHandler(this.logFile.getAbsolutePath(), true);
             this.logger.addHandler(this.logFileHandler);
             this.logger.setLevel(Level.ALL);
             this.logFormatter = new SimpleFormatter();
             this.logFileHandler.setFormatter(this.logFormatter);
         } catch(IOException e) {
             this.logger.log(Level.SEVERE, "IOException when setting up the logger.", e);
         }
 
         // Instantiates the config
         try {
             this.config = new Config(this.getName() + " Configuration", new File(this.dataDir, "ziahsclient.cfg"), new URL("http://magi-craft.net/defaultconfigs/ziahsclient.cfg"));
         } catch(IOException e) {
             this.logger.log(Level.SEVERE, "IOException when setting up the config.", e);
         }
 
         // Load Ziah_'s Client language files (module language files are loaded later)
         try {
             List<ResourceLocation> langFiles = new ArrayList<ResourceLocation>();
             BufferedReader in = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().func_110442_L().func_110536_a(new ResourceLocation("ziahsclient/lang/languages.list")).func_110527_b()));
 
             while (true) {
                 try {
                     String line = in.readLine();
                     if (line == null) break;
                     if (!line.isEmpty() && !line.startsWith("#")) {
                         langFiles.add(new ResourceLocation("ziahsclient/lang/" + line + ".lang"));
                     }
                 } catch(IOException e) {
                     this.logger.log(Level.SEVERE, "Failed to load language!", e);
                 }
             }
             Locale.loadLanguages(langFiles);
         } catch(IOException e) {
             this.logger.log(Level.SEVERE, "Failed to load language list!", e);
         }
 
         // Add the sound for the scary maze game easter egg
         Minecraft.getMinecraft().sndManager.addSound("ziahsclient/scarymazegame.ogg");
 
         // Register our event handler
         this.eventBus.registerEventHandler(null, new EventHandler());
 
         // Start the tick thread to call tick events
         new Thread(new ThreadTick()).start();
 
         // If we aren't running the correct Minecraft version exit (prints errors above)
         if (!this.runningMCVersion.equalsIgnoreCase(this.getMinecraftVersion())) return;
 
         // Instantiate the menu button
         this.menuButton = new GuiButton(9001, 0, 0, Locale.localize("ziahsclient.gui.menu"));
 
         // Register the modules button for the menu
         try {
             this.registerMenuButton(new GuiSmallButton(0, 0, 0, Locale.localize("ziahsclient.gui.modules")), this.getClass().getDeclaredMethod("onModuleButtonClicked"), this);
         } catch(Exception e) {}
 
         // Add the back button to the menu but don't call the register function because we don't want to increment the button ids or anything
         this.menuButtons.add(0, new GuiSmallButton(0, 0, 0, Locale.localize("ziahsclient.gui.back")));
 
         // Instantiate the ModuleManager to load the modules
         new ModuleManager();
 
         // Print how many modules were loaded and how many were not and a total
         this.logger.log(Level.INFO, "Found " + (ModuleManager.getInstance().getUnloadedModules().size() + ModuleManager.getInstance().getLoadedModules().size()) + " modules, " + ModuleManager.getInstance().getUnloadedModules().size() + " modules not loaded, " + ModuleManager.getInstance().getLoadedModules().size() + " modules loaded.");
 
         // Load languages from modules
         Map<String, InputStream> mlangFiles = new HashMap<String, InputStream>();
         for (Module module : ModuleManager.getInstance().getLoadedModules().keySet()) {
         	InputStream langListIn = ModuleManager.getInstance().getModuleClassLoader().getResourceAsStream(module.moduleId() + "/lang/languages.list");
         	if (langListIn != null) {
 	            BufferedReader in = new BufferedReader(new InputStreamReader(langListIn));
 	
 	            while (true) {
 	                try {
 	                    String line = in.readLine();
 	                    if (line == null) break;
 	                    if (!line.isEmpty() && !line.startsWith("#")) {
 	                    	InputStream langFileIn = ModuleManager.getInstance().getModuleClassLoader().getResourceAsStream(module.moduleId() + "/lang/" + line + ".lang");
 	                        if (langFileIn != null) mlangFiles.put(line, langFileIn);
 	                    }
 	                } catch(IOException e) {
 	                    this.logger.log(Level.SEVERE, "Failed to load language!", e);
 	                }
 	            }
        	}else{
                    langListIn = this.getClass().getClassLoader().getResourceAsStream(module.moduleId() + "/lang/languages.list");
                    if (langListIn != null) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(langListIn));
            
                        while (true) {
                            try {
                                String line = in.readLine();
                                if (line == null) break;
                                if (!line.isEmpty() && !line.startsWith("#")) {
                                    InputStream langFileIn = this.getClass().getClassLoader().getResourceAsStream(module.moduleId() + "/lang/" + line + ".lang");
                                    if (langFileIn != null) mlangFiles.put(line, langFileIn);
                                }
                            } catch(IOException e) {
                                this.logger.log(Level.SEVERE, "Failed to load language!", e);
                            }
                        }
                    }
         	}
         }
         Locale.loadLanguages(mlangFiles);
 
         // Set the message prefix
         this.msgPrefix = "§7[§6" + this.getName() + "§7]§r ";
 
         // Print a starting message and a message telling where the data is going to be stored
         this.logger.log(Level.INFO, this.getName() + " starting...");
         this.logger.log(Level.INFO, "Storing data in " + this.dataDir.getAbsolutePath());
 
         // Register the Ziah_'s Client packet channel
         ModLoader.registerPacketChannel(this.modClass, this.modAltName.toLowerCase());
 
         // Call the load event
         this.eventBus.callEvent(new EventLoad());
     }
 
     /*
      * Registers a key for Ziah_'s Client
      */
     public void registerKey(Object module, KeyBinding key) {
         key.keyDescription = Locale.localize(key.keyDescription);
         this.keybindings.put(key, module);
         ModLoader.registerKey(this.modClass, key, false);
     }
 
     /*
      * Called by the Ziah_'s Client BaseMod class to call the add renderers event
      */
     public void addRenderer(Map map) {
         this.eventBus.callEvent(new EventAddRenderers(map));
     }
 
     /*
      * Called by the Ziah_'s Client BaseMod class to call the chat event
      */
     public void clientChat(String message) {
         this.eventBus.callEvent(new EventChat(message));
     }
 
     /*
      * Called by the Ziah_'s Client BaseMod class to call the custom payload event
      */
     public void clientCustomPayload(NetClientHandler clientHandler, Packet250CustomPayload payload) {
         this.eventBus.callEvent(new EventCustomPayload(clientHandler, payload));
 
         if (payload.channel.equals(this.modAltName.toLowerCase())) PacketManager.onPacketData(clientHandler, payload);
     }
 
     /*
      * Called by the Ziah_'s Client BaseMod class to call the client connect event
      */
     public void clientConnect(NetClientHandler clientHandler) {
         this.eventBus.callEvent(new EventClientConnect(clientHandler.getNetManager().getSocketAddress().toString(), clientHandler));
 
         if (!this.config.getData().containsKey("hasPlayed") || this.config.getData().containsKey("hasPlayed") && this.config.getData().getProperty("hasPlayed") == "false") {
             this.config.getData().setProperty("hasPlayed", "true");
             try {
                 this.config.save();
             } catch(IOException e) {
                 this.logger.log(Level.WARNING, "Exception when saving config.", e);
             }
         }
         this.logger.log(Level.INFO, "Client joined " + clientHandler.getNetManager().getSocketAddress().toString());
     }
 
     /*
      * Called by the Ziah_'s Client BaseMod class to call the client disconnect event
      */
     public void clientDisconnect(NetClientHandler clientHandler) {
         this.eventBus.callEvent(new EventClientDisconnect(clientHandler.getNetManager().getSocketAddress().toString(), clientHandler));
     }
 
     /*
      * Returns the log file
      */
     public File getLogFile() {
         return this.logFile;
     }
 
     /*
      * Returns the data directory
      */
     public File getDataDir() {
         return this.dataDir;
     }
 
     /*
      * Returns the config
      */
     public Config getConfig() {
         return this.config;
     }
 
     /*
      * Returns the menu button
      */
     public GuiButton getMenuButton() {
         return this.menuButton;
     }
 
     /*
      * Returns the instance of this class
      */
     public static ZiahsClient getInstance() {
         return instance;
     }
 
     /*
      * Returns the event bus
      */
     public EventBus getEventBus() {
         return this.eventBus;
     }
 
     /*
      * Returns the logger
      */
     public Logger getLogger() {
         return this.logger;
     }
 
     /*
      * Returns the BaseMod class
      */
     public BaseMod getModClass() {
         return this.modClass;
     }
 
     /*
      * Returns the name of Ziah_'s Client
      */
     public String getName() {
         return Locale.localize("ziahsclient.ziahsclient");
     }
 
     /*
      * Returns the version of Ziah_'s Client
      */
     public String getVersion() {
         return this.version;
     }
 
     /*
      * Returns the version of Minecraft that this version of Ziah_'s Client is compatible with
      */
     public String getMinecraftVersion() {
         return this.mcversion;
     }
 
     /*
      * Returns the running Minecraft version
      */
     public String getRunningMinecraftVersion() {
         return this.runningMCVersion;
     }
 
     /*
      * Returns all the registered key bindings
      */
     public Map<KeyBinding, Object> getKeybindings() {
         return this.keybindings;
     }
 
     /*
      * Returns all the registered key bindings and last values
      */
     public Map<KeyBinding, Boolean> getKeybindingAndLastValues() {
         return this.lastKeybindings;
     }
 
     /*
      * Unregisters keys for the specified module
      */
     public void unregisterKeys(Class modClass) {
         for (KeyBinding key : new ArrayList<KeyBinding>(this.keybindings.keySet())) {
             if (this.keybindings.get(key).getClass().equals(modClass)) {
                 this.keybindings.remove(key);
                 this.lastKeybindings.remove(key);
             }
         }
     }
 
     /*
      * Registers a button in the menu
      */
     public void registerMenuButton(GuiButton button, Method method, Object obj) {
         button.id = this.nextMenuId;
         this.nextMenuId++;
         this.menuButtons.add(button);
         this.menuButtonIdToMethod.put(button.id, method);
         this.menuButtonIdToClickMethodObject.put(button.id, obj);
     }
 
     /*
      * Gets the registered menu buttons
      */
     public List<GuiButton> getMenuButtons() {
         return this.menuButtons;
     }
 
     /*
      * Callback for the module button
      */
     public void onModuleButtonClicked() {
         Minecraft.getMinecraft().displayGuiScreen(new GuiModList(Minecraft.getMinecraft().currentScreen));
     }
 
     /*
      * Returns the menu button id to method map
      */
     public Map<Integer, Method> getMenuButtonIdToMethod() {
         return this.menuButtonIdToMethod;
     }
 
     /*
      * Returns the menu button id to callback method object map
      */
     public Map<Integer, Object> getMenuButtonIdToClickMethodObject() {
         return this.menuButtonIdToClickMethodObject;
     }
 }
