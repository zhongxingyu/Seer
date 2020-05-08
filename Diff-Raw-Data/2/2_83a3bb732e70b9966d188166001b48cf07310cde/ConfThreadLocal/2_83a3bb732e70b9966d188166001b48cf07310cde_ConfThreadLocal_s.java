 // cinnamon - the Open Enterprise CMS project
 // Copyright (C) 2007-2013 Texolution GmbH (http://texolution.eu)
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 // (or visit: http://www.gnu.org/licenses/lgpl.html)
 
 package cinnamon.global;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import cinnamon.exceptions.CinnamonConfigurationException;
 
 /**
 * The Conf class loads the configuration from the XML file "config/config.xml". It follows a Singleton pattern by
  * storing the Conf object in a ThreadLocal variable to prevent repeated parsing of the configuration file.   
  *
  */
 public class ConfThreadLocal extends Conf {
 
     static long sessionExpirationTime = 0;
 
 	@SuppressWarnings("unused")
 	private transient Logger log = LoggerFactory.getLogger("cinnamon.global.ConfThreadLocal");
 
 	static ThreadLocal<ConfThreadLocal> config = new ThreadLocal<ConfThreadLocal>(){
 		@Override
 		protected ConfThreadLocal initialValue() {
 			ConfThreadLocal c;
 			try {
 				c = new ConfThreadLocal();
 			} catch (Exception e) {
 				LoggerFactory.getLogger(ConfThreadLocal.class).debug("", e);
 				throw new CinnamonConfigurationException("Could not initialize Config: "+e.getLocalizedMessage());
 			}
 			return c;
 		}
 	};
 	
 	private ConfThreadLocal(){
 		super();
 	}
 
 	public static ThreadLocal<ConfThreadLocal> getConfig() {
 		return config;
 	}
 
 	public static void setConfig(ThreadLocal<ConfThreadLocal> config) {
 		ConfThreadLocal.config = config;
 	}
 	
 	/**
 	 * Use getConf to retrieve the single Conf object.
 	 * @return Conf - the Conf singleton
 	 */
 	public static ConfThreadLocal getConf(){
 		return config.get();
 	}
 
 	/**
 	 * Reads the string-value of //repository[name='%s']/sessionExpirationTime.
 	 * @param name The Name of the repository
 	 * @return the time it takes for a session to expire (in milliseconds)
 	 */
 	public Long getSessionExpirationTime(String name){
         if(sessionExpirationTime == 0){
 		    String xpath = String.format("repository[name='%s']/sessionExpirationTime", name);
 		    String time = getField(xpath, "3600000");
 		    sessionExpirationTime =  Long.parseLong(time);
         }
         return sessionExpirationTime;
     }
 	
 }
