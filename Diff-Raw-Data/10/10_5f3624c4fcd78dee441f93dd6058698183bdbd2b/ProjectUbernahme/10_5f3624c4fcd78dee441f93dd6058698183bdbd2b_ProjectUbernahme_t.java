 package projectubernahme;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
 
 import projectubernahme.interface2D.Interface2D;
 import projectubernahme.simulator.MainSimulator;
 
 /*
  * Copyright 2010 Project Ubernahme Team
  * Copyright 2010 Martin Ueding <dev@martin-ueding.de>
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 /** the main game class */
 public class ProjectUbernahme {
 	/** main player */
 	static Player player;
 
 	/** simulator backbone that simulates all the nonplayer interactions */
 	static MainSimulator sim;
 
 	private static Properties config;
 	
 	public static DecimalFormat f;
 
 	public static int verboseLevel;
 
 	public static String getConfigValue (String key) {
 		return (String) config.get(key);
 	}
 
 	public static void main (String[] args) {
		logMessages = new CopyOnWriteArrayList<LogMessage>();
 		f = new DecimalFormat();
 		f.applyPattern("0.00E0");
 		
 		config = new Properties();
 		try {
 			config.load(ClassLoader.getSystemResourceAsStream("projectubernahme/config.properties"));
 		} catch (IOException e) {
 			ProjectUbernahme.log(Localizer.get("could not open main config file"), MessageTypes.ERROR);
 			e.printStackTrace();
 		}		
 		
 		verboseLevel = Integer.parseInt(ProjectUbernahme.getConfigValue("verboseLevel"));
 		
 		ProjectUbernahme.log(Localizer.get("Welcome to Project Ubernahme"));
 		
 		sim = new MainSimulator();
 
 		player = new Player(sim);
 		
 		sim.addPlayer(player);
 		
 		/* creates a new interface which lets the physical player take control over the player object */
 		@SuppressWarnings("unused")
 		Interface2D interface2d = new Interface2D(sim, player);
 	}
 	
	private static CopyOnWriteArrayList<LogMessage> logMessages;
 	
 	/* prints out a log message */
 	public static void log (String s, Enum<MessageTypes> type) {
 		/* check if it is already in the queue */
 		boolean isInQueue = false;
 		for (LogMessage message : getLogMessages()) {
 			if (message.msg.equals(s)) {
 				isInQueue = true;
 			}
 		}
 		
 		if (!isInQueue) {
 			getLogMessages().add(new LogMessage(s, type));
 
 			while (getLogMessages().size() > Integer.decode(ProjectUbernahme.getConfigValue("maxLogLines"))) {
 				getLogMessages().remove(0);
 			}
 		}
 		
 	}
 	
 	public static void log (String s) {
 		log(s, MessageTypes.UNKNOWN);
 	}
 
	public static CopyOnWriteArrayList<LogMessage> getLogMessages() {
 		return logMessages;
 	}
 }
