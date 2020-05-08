 /*
  * This file is part of GreatmancodeTools.
  *
  * Copyright (c) 2013-2013, Greatman <http://github.com/greatman/>
  *
  * GreatmancodeTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GreatmancodeTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GreatmancodeTools.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.greatmancode.tools.interfaces;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
import java.util.logging.Logger;
 
 import com.greatmancode.tools.ServerType;
 import com.greatmancode.tools.caller.canary.CanaryServerCaller;
 import com.greatmancode.tools.events.EventManager;
 
 import net.canarymod.plugin.Plugin;
 
 public class CanaryLoader extends Plugin implements Loader {
 	private Common common;
 	private EventManager eventManager;
 
 	@Override
 	public void onEnable() {
 		//Not used
 	}
 
 	@Override
 	public void onDisable() {
 		//Not used
 	}
 
 	@Override
 	public ServerType getServerType() {
 		return ServerType.CANARY;
 	}
 
 	@Override
 	public EventManager getEventManager() {
 		return eventManager;
 	}
 
 	@Override
 	public Common getCommon() {
 		return common;
 	}
 
 	@Override
 	public boolean enable() {
 
 		CanaryServerCaller canaryCaller = new CanaryServerCaller(this);
 		eventManager = new EventManager(canaryCaller);
 		InputStream is = this.getClass().getResourceAsStream("/loader.yml");
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		try {
 			String mainClass = br.readLine();
 			mainClass = mainClass.split("main-class:")[1].trim();
 
 			Class<?> clazz = Class.forName(mainClass);
 
 			if (Common.class.isAssignableFrom(clazz)) {
 				common = (Common) clazz.newInstance();
				common.onEnable(canaryCaller, Logger.getLogger(getName()));
 				return true;
 			} else {
				this.getLogman().logSevere("The class " + mainClass + " is invalid!");
 				return false;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IllegalAccessException e) {
 			return false;
 		}
 		return false;
 	}
 
 	@Override
 	public void disable() {
 
 	}
 }
