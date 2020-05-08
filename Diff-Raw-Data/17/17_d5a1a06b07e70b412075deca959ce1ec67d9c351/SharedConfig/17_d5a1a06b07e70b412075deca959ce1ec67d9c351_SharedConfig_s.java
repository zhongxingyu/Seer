 /*
  * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.daboross.bungeedev.ncommon.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import net.md_5.bungee.api.plugin.Plugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 public class SharedConfig {
 
     private final JSONObject config;
     public final File configFile;
 
     public SharedConfig(Plugin p) throws IOException {
         configFile = new File(p.getDataFolder(), "shared-config.json");
        if (configFile == null) {
             config = new JSONObject();
         } else {
             try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
                 config = new JSONObject(new JSONTokener(fileInputStream));
             }
         }
     }
 
     public void save() throws IOException {
         try (FileOutputStream fos = new FileOutputStream(configFile)) {
             try (OutputStreamWriter writer = new OutputStreamWriter(fos, Charset.forName("UTF-8"))) {
                 config.write(writer);
             }
         }
     }
 
     public String getString(String key) {
         return getString(key, null);
     }
 
     public String getString(String key, String def) {
         try {
             return config.getString(key);
         } catch (JSONException ex) {
             return def;
         }
     }
 
     public int getInt(String key) {
         return getInt(key, 0);
     }
 
     public int getInt(String key, int def) {
         try {
             return config.getInt(key);
         } catch (JSONException ex) {
             return def;
         }
     }
 
     public double getDouble(String key) {
         return getDouble(key, 0);
     }
 
     public double getDouble(String key, double def) {
         try {
             return config.getDouble(key);
         } catch (JSONException ex) {
             return def;
         }
     }
 
     public boolean getBoolean(String key) {
         return getBoolean(key, false);
     }
 
     public boolean getBoolean(String key, boolean def) {
         try {
             return config.getBoolean(key);
         } catch (JSONException ex) {
             return def;
         }
     }
 
     public JSONArray getList(String key) {
         try {
             return config.getJSONArray(key);
         } catch (JSONException ex) {
             JSONArray array = new JSONArray();
             config.put(key, array);
             return array;
         }
     }
 
     public JSONObject getConfig() {
         return config;
     }
 }
