 /**
  * Copyright (C) 2013 Colorado School of Mines
  *
  * This file is part of the Interface Software Development Kit (SDK).
  *
  * The InterfaceSDK is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * The InterfaceSDK is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the InterfaceSDK.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.mines.acmX.exhibit.input_services.hardware.drivers.openni;
 
 import org.OpenNI.Context;
 import org.OpenNI.GeneralException;
 import org.OpenNI.OutArg;
 import org.OpenNI.ScriptNode;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 /**
  * A singleton for the OpenNI Context that manages communication between
  * the openni library and the kinect device.
  * 
  * @author Aakash Shah
  * @author Ryan Stauffer
  */
 public class OpenNIContextSingleton {
 	public static final Logger log = LogManager.getLogger(OpenNIContextSingleton.class);
     private static Context context = null;
     private static OutArg<ScriptNode> scriptNode;
     private static String xml_file = null;
 
     public static void setConfigurationFile(String str) {
     	xml_file = str;
     }
     
     public static Context getContext()
     		throws GeneralException, OpenNIConfigurationException {
     	if (xml_file == null) {
     		throw new OpenNIConfigurationException("No configuration file provided!");
     	}
     	
     	if (context == null) {
     		scriptNode = new OutArg<ScriptNode>();
     		context = Context.createFromXmlFile(xml_file, scriptNode);
     	}
         return context;
     }
     
     public static void destroy() {
     	context = null;
     }
 }
