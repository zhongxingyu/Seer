 /* 
  * This file is part of PS2YT
  *
  * Copyright (C) 2013 Frédéric Bertolus (Niavok)
  * 
  * PS2YT is free software: you can redistribute it and/or modify
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
 package com.niavok;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 public class Config {
 
	private static final String CACHE_USER_CONF = ClassLoader.getSystemClassLoader().getResource(".").getPath()+"/cache/user.conf";
 	static Properties configFile;
 	static Properties userConfigFile;
 	
 	static {
 		configFile = new Properties();
 		userConfigFile = new Properties();
 		
 
 		
 		try {
 			configFile.load(new FileInputStream(new File(getJarPath()+"/ps2yt.conf")));
			userConfigFile.load(new FileInputStream(new File(CACHE_USER_CONF)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
 		System.out.println("getJarPath() "+getJarPath());
 	}
 	
 	public static String getJarPath() {
		String absolutePath = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()).getAbsolutePath();
 		return absolutePath;
 	}
 	
 	
 	public static String getCachePath(String file) {
 		return new File(new File(getJarPath(), "cache"),file).getAbsolutePath();
 	}
 	
 	public static String getYoutubeDeveloperKey() {
 		return configFile.getProperty("DEVELOPER_KEY");
 	}
 
 	public static String getYoutubeClientId() {
 		return configFile.getProperty("CLIENT_ID");
 	}
 
 	public static String getYoutubeClientSecret() {
 		return configFile.getProperty("CLIENT_SECRET");
 	}
 
 	public static String getYoutubeAccessToken() {
 		return userConfigFile.getProperty("ACCESS_TOKEN");
 	}
 
 	public static String getYoutubeRefreshToken() {
 		return userConfigFile.getProperty("REFRESH_TOKEN");
 	}
 
 	public static void setAccessToken(String accessToken) {
 		if(accessToken == null) {
 			userConfigFile.setProperty("ACCESS_TOKEN", "");
 		} else {
 			userConfigFile.setProperty("ACCESS_TOKEN", accessToken);
 		}
 		save();
 	}
 
 	public static void setRefreshToken(String refreshToken) {
 		if(refreshToken == null) {
 			userConfigFile.setProperty("REFRESH_TOKEN", "");
 		} else {
 			userConfigFile.setProperty("REFRESH_TOKEN", refreshToken);
 		}
 		save();
 	}
 	
 	private static void save() {
 		try {
 			userConfigFile.store(new FileOutputStream(new File(CACHE_USER_CONF)), "");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static int getMaxConcurrentEncode() {
 		return 2;
 	}
 
 	public static int getMaxConcurrentUpload() {
 		return 1;
 	}
 
 	public static int getMaxConcurrentDownload() {
 		return 1;
 	}
 
 
 	public static String getPodcastUrl() {
 		return configFile.getProperty("PODCAST_URL");
 	}
 
 
 	public static int getDefaultPodcast() {
 		String property = userConfigFile.getProperty("DEFAULT_PODCAST");
 		
 		int defaultPodcast = 0;
 		
 		if(property != null && !property.isEmpty()) {
 			defaultPodcast = Integer.parseInt(property);
 		}
 		
 		return defaultPodcast;
 	}
 
 
 	public static void setDefaultPodcast(int defaultPodcast) {
 		userConfigFile.setProperty("DEFAULT_PODCAST", Integer.toString(defaultPodcast));
 		save();		
 	}
 
 	
 
 }
