 /*
 	BukkitUtil
 	Copyright (C) 2011 Klezst
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package bukkitutil.util;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Provides convenience functions for logging.
  * 
  * @author Klezst
  */
 public class Logging {
     private static final Logger logger = Logger.getLogger("Minecraft");
     
     /**
      * Returns the logger used for logging.
      * 
      * @returns the logger used for logging.
      * 
      * @author Klezst
      */
     public static Logger getLogger() {
 	return logger;
     }
     
     /**
      * Logs messages.
      * 
      * @param level
      *            The Level of the message.
      * @param messages
      *            The messages to log.
      * 
      * @author Klezst
      */
     public static void log(final Level level, final String message) {
 	prefixLog(level, "", message);
     }
     
     /**
      * Logs messages. Each line will have PREFIX added before it.
      * 
      * @param level
      *            The Level of the message.
      * @param messages
      *            The messages to log.
      * 
      * @author Klezst
      */
     public static void prefixLog(final Level level, final String prefix, final String... messages) {
 	for (String message : messages) {
 	    for (String line : message.split("\n")) {
		logger.log(level, prefix + line);
 	    }
 	}
     }
 }
