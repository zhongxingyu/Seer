 /*
  * This file is part of Common Implementation + API for MineQuest, This provides the common classes for the MineQuest official implementation of the API..
  * Common Implementation + API for MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) The MineQuest Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.common;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.logging.Level;
 
 import com.theminequest.api.Managers;
 import com.theminequest.common.quest.v1.V1EventManager;
 
 public class Common {
 	
 	private static Common COMMON = null;
 	
 	public static Common getCommon() {
 		if (COMMON == null)
 			resetCommon();
 		return COMMON;
 	}
 	
 	public static void resetCommon() {
		if (COMMON != null)
			COMMON.stopCommon();
 		COMMON = new Common();
 	}
 	
 	private Properties attributes;
 	private V1EventManager events;
 	
 	public Common() {
 		attributes = new Properties();
 		
 		InputStream resource = null;
 		try {
 			resource = Common.class.getResourceAsStream("common.properties");
 			if (resource != null)
 				attributes.load(resource);
 		} catch (IOException e) {
 			Managers.log(Level.WARNING, "[Common] Unable to load resources.");
 		} finally {
 			if (resource != null)
 				try { resource.close(); } catch (IOException e) {}
 		}
 		
 		events = new V1EventManager();
 	}
 	
 	public V1EventManager getV1EventManager() {
 		return events;
 	}
 	
 	public String getVersion() {
 		return attributes.getProperty("common.version", "unknown");
 	}
 	
 	public void stopCommon() {
 		events.dismantleRunnable();
 	}
 	
 }
