 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.logging.Logger;
 
 public class CMCPCP extends Plugin {
 
 	public final static String name = "CMCPCP";
	private final static String version = "2.0_1";
 	protected static String currver = version;
 	public final String author = "DarkDiplomat";
 	public final Logger log = Logger.getLogger("Minecraft");
 	private CMCPCPListener listener = new CMCPCPListener();
 	
 	public void initialize() {
 		etc.getLoader().addListener( PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
 	}
 	public synchronized void disable() {
 		log.info("[CMCPCP] Throwing out old coffee!");
		listener.killTask();
 		log.info(name + " version " + version + " is disabled!");
 	}
 	public void enable() {
 		log.info( name + " version " + version + " by " + author + " is enabled!" );
 	}
 	
 	public static boolean isLatest(){
         try{
             BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://visualillusionsent.net/cmod_plugins/versions.php?plugin="+name).openStream()));
             String inputLine;
             if ((inputLine = in.readLine()) != null) {
                 currver = inputLine;
             }
             in.close();
             return Float.valueOf(version.replace("_", "")) >= Float.valueOf(currver.replace("_", ""));
         } 
         catch (Exception E) {
         }
         return true;
     }
 }
 
 /*******************************************************************************\
 * CanaryMod Coffee Pot Control Protocol v2.x                                    *
 * Copyright (C) 2012 Visual Illusions Entertainment                             *
 * @author darkdiplomat <darkdiplomat@visualillusionsent.net>                    *
 *                                                                               *
 * This file is part of CanaryMod Coffee Pot Control Protocol                    *
 *                                                                               *
 * This program is free software: you can redistribute it and/or modify          *
 * it under the terms of the GNU General Public License as published by          *
 * the Free Software Foundation, either version 3 of the License, or             *
 * (at your option) any later version.                                           *
 *                                                                               *
 * This program is distributed in the hope that it will be useful,               *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                          *
 *                                                                               *
 * You should have received a copy of the GNU General Public License             *
 * along with this program.  If not, see http://www.gnu.org/licenses/gpl.html    *
 *                                                                               *
 \*******************************************************************************/
