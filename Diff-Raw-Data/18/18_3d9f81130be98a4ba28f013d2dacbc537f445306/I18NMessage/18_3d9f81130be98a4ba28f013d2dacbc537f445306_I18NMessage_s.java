 /*
  * This file is part of MineQuest, The ultimate MMORPG plugin!.
  * MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
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
 package com.theminequest.MineQuest;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.h2.util.IOUtils;
 
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Utils.PropertiesFile;
 
 public class I18NMessage {
 	
 	private static final String LOCATION = Managers.getActivePlugin().getDataFolder().getAbsolutePath() + File.separator + "locales";
 	private static final Locale LOCALE = new Locale(MineQuest.configuration.mainConfig.getString("locale", "en_US"));
 	private static final String CUSTOM = LOCATION + File.separator + "custom.dict";
 	
	static {
		File f = new File(LOCATION);
		if (!f.exists() || !f.isDirectory()) {
			if (f.exists())
				f.delete();
			f.mkdirs();
		}
	}
	
 	public synchronized static Locale getLocale() {
 		return LOCALE;
 	}
 	
 	public static final class Locale {
 		
 		private PropertiesFile localeprops;
 		private PropertiesFile customprops;
 		
 		private Locale(String localename) {
			//localefile = new PropertiesFile(LOCATION + File.separator + getLocale() + ".dict");
 			File localefile = new File(LOCATION + File.separator + localename + ".dict");
 			if (!localefile.exists())
 				copyFromJar(localefile,localename);
 			else {
 				localeprops = new PropertiesFile(localefile.getAbsolutePath());
 				if (!localeprops.getString("lastversion", "0").equals(MineQuest.getVersion())) {
 					localeprops = null;
 					copyFromJar(localefile,localename);
 				}
 			}
 			localeprops = new PropertiesFile(localefile.getAbsolutePath());
 			localeprops.setString("lastversion", MineQuest.getVersion());
 			customprops = new PropertiesFile(CUSTOM);
 			if (!customprops.getString("lastversion", MineQuest.getVersion()).equals(MineQuest.getVersion())) {
 				Managers.log(Level.WARNING, "[i18n] Custom translations may be out of date!");
 			}
 			customprops.setString("lastversion", MineQuest.getVersion());
 		}
 		
 		public synchronized String getString(String key) {
 			String touse = localeprops.getChatString(key);
 			if (customprops.containsKey(key))
 				touse = customprops.getChatString(key);
 			return touse;
 		}
 		
 		private void copyFromJar(File localefile, String localename) {
 			JarFile file = null;
 			try {
 				file = new JarFile(MineQuest.getFileReference());
 				JarEntry jarentry = file.getJarEntry("i18n/" + localename + ".dict");
 				if (jarentry == null) {
 					Managers.log(Level.SEVERE, "[i18n] Can't find locale " + localename + "; using en_US");
 					jarentry = file.getJarEntry("i18n/en_US.dict");
 				}
 				IOUtils.copyAndClose(file.getInputStream(jarentry), new FileOutputStream(localefile));
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			} finally {
 				if (file != null) {
 					try {
 						file.close();
 					} catch (IOException e) {
 						Managers.log(Level.SEVERE, "Resource Leak! i18n");
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		
 	}
 	
 }
