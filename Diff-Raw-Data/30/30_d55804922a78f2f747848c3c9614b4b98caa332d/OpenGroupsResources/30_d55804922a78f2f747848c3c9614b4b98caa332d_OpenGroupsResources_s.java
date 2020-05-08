 /*******************************************************************************
  * Copyright 2011 Adrian Cristian Ionescu
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package ro.zg.open_groups.resources;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Locale;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 
 import com.vaadin.terminal.ThemeResource;
 
 import ro.zg.open_groups.gui.constants.LunaBlueIconsSet;
 import ro.zg.open_groups.gui.constants.OpenGroupsIconsSet;
 
 public class OpenGroupsResources {
     // private static ResourceBundle messagesBundle = ResourceBundle.getBundle("OpenGroupsMessages",new Locale("ro",
     // "RO"));
     private static ResourceBundle messagesBundle;
     private static OpenGroupsIconsSet iconsSet=new LunaBlueIconsSet("img/luna-blue-icons/png");
     static {
 	try {
 	    String fileName = "OpenGroupsMessages_" + new Locale("ro", "RO") + ".properties";
 	    InputStream in = (Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
 	    messagesBundle = new PropertyResourceBundle(new InputStreamReader(in, "UTF-8"));
 	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	} catch (IOException e) {
	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
     }
     
     public static String getMessage(String key) {
 	try {
 	    return messagesBundle.getString(key);
 	} catch (Exception e) {
 //	    e.printStackTrace();
 	}
 	return null;
     }
 
     public static ResourceBundle getBundle() {
 	return messagesBundle;
     }
 
     public static ThemeResource getIcon(String name,String size) {
	return iconsSet.getIcon(name, size);
     }
     
     public static ThemeResource getIcon(String name) {
 	return new ThemeResource("img/"+name);
     }
 }
