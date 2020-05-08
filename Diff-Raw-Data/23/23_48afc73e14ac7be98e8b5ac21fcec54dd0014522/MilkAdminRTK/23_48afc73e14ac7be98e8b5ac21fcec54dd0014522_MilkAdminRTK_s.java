 package com.bukkit.sharkiller.milkAdminRTK;
 
//these must be imported

import com.bukkit.sharkiller.milkAdminRTK.Config.*;
import com.bukkit.sharkiller.milkAdminRTK.RTK.*;
 import com.drdanick.McRKit.module.Module;
 import com.drdanick.McRKit.module.ModuleLoader;
 import com.drdanick.McRKit.module.ModuleMetadata;
 import com.drdanick.McRKit.ToolkitEvent;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 //This class defines a skeleton module that will be enabled when the server is stopped, and disabled when the server is started.
 @SuppressWarnings("unused")
 public class MilkAdminRTK extends Module implements RTKListener{
 	private static final Logger LOG = Logger.getLogger("milkAdmin");
 	private ModuleMetadata meta;
 	private ModuleLoader moduleLoader;
 	private ClassLoader cLoader;
 	RTKInterface api = null;
 	String PluginDir = "plugins/milkAdmin";
 	String userRTK, passRTK;
 	int portRTK;
 	Configuration Settings = new Configuration(new File(PluginDir+"/settings.yml"));
 	private WebServer server = null;
 	public MilkAdminRTK(ModuleMetadata meta, ModuleLoader moduleLoader, ClassLoader cLoader){
 		super(meta,moduleLoader,cLoader,ToolkitEvent.ON_SERVER_HOLD,ToolkitEvent.ON_SERVER_RESTART);
 		//the last two parameters define the events where the plugin is enabled and disabled respectively.
 		
 		//fix formatting on logger
 		Logger rootlog = Logger.getLogger("");
 		for (Handler h : rootlog.getHandlers()){ //remove all handlers
 			h.setFormatter(new McSodFormatter());
 		}
 		
 		try{
 			Settings.load();
             userRTK = Settings.getString("RTK.Username", "user");
             passRTK = Settings.getString("RTK.Password", "pass");
             portRTK = Settings.getInt("RTK.Port", 25561);
         	api = RTKInterface.createRTKInterface(portRTK,"localhost",userRTK,passRTK);
 		}catch(RTKInterfaceException e){
 			e.printStackTrace();
 		}catch(Exception e){
 			LOG.log(Level.SEVERE, "[milkAdminRTK] Error instantiating milkAdmin Module", e);
 		}
 		
 	}
 	protected void onEnable(){
 		LOG.info("[milkAdminRTK] Module enabled!");
         api.registerRTKListener(this);
 		server = new WebServer(this, LOG);
 	}
 
 	public void onRTKStringReceived(String s){
 		if(s.equals("RTK_TIMEOUT")){
 			LOG.info("[milkAdminRTK] RTK not response to the user '"+userRTK+"' in the port '"+portRTK+"' bad configuration?");
 		}else
 			LOG.info("[milkAdminRTK] From RTK: "+s);
 	}
 	
 	protected void onDisable(){
 		LOG.info("[milkAdminRTK] Module disabled!");
 		try{
 			server.stopServer();
 		}catch(IOException e){
 			LOG.severe("[milkAdminRTK] Error closing milkAdmin Server");
 			e.printStackTrace();
 		}
 	}
 }
 
 class McSodFormatter extends Formatter {
 	SimpleDateFormat dformat;
 
 	public McSodFormatter(){
 		dformat = new SimpleDateFormat("HH:mm:ss");
 	}
 
 	@Override
 	public String format(LogRecord record) {
 		StringBuffer buf = new StringBuffer();
 		buf.append(dformat.format(new Date(record.getMillis())))
 		.append(" [").append(record.getLevel().getName()).append("] ")
 		.append(this.formatMessage(record)).append('\n');
 		if (record.getThrown() != null){
 			buf.append('\t').append(record.getThrown().toString()).append('\n');
 		}
 		return buf.toString();
 	}
 
 }
