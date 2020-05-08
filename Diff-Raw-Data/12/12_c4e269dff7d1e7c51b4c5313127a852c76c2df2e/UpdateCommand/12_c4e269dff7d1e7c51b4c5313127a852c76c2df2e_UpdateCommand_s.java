 package com.titankingdoms.nodinchan.titanchat.command.commands;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.command.Command;
 import com.titankingdoms.nodinchan.titanchat.command.info.CommandID;
 import com.titankingdoms.nodinchan.titanchat.command.info.CommandInfo;
 
 public class UpdateCommand extends Command {
 	
 	@CommandID(name = "Update", aliases = "update")
 	@CommandInfo(description = "Checks for an update of the library", usage = "update")
 	public void update(Player player, String[] args) {
 		try {
			File destination = new File(getDataFolder().getParentFile().getParentFile(), "lib");
 			destination.mkdirs();
 			
 			File lib = new File(destination, "NC-BukkitLib.jar");
 			
 			if (!lib.exists()) {
 				System.out.println("Missing NC-Bukkit lib");
 				
 			} else {
 				JarFile jarFile = new JarFile(lib);
 				
 				double version = 0;
 				
 				if (jarFile.getEntry("version.yml") != null) {
 					JarEntry element = jarFile.getJarEntry("version.yml");
 					BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
 					version = Double.parseDouble(reader.readLine().substring(9).trim());
 					
 				} else {
 					System.out.println("Missing version.yml");
 				}
 				
 				if (version == 0) {
 					System.out.println("NC-Bukkit lib outdated");
 					
 				} else {
 					HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://www.nodinchan.com/NC-BukkitLib/version.yml").openConnection();
 					BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
 					
 					if (Double.parseDouble(reader.readLine().replace("NC-BukkitLib Version ", "").trim()) > version) {
 						System.out.println("NC-Bukkit lib outdated");
 					}
 				}
 			}
 			
 			System.out.println("Downloading NC-Bukkit lib...");
 			URL libURL = new URL("http://www.nodinchan.com/NC-BukkitLib/NC-BukkitLib.jar");
 			ReadableByteChannel rbc = Channels.newChannel(libURL.openStream());
 			FileOutputStream output = new FileOutputStream(lib);
 			output.getChannel().transferFrom(rbc, 0, 1 << 24);
 			System.out.println("Downloaded NC-Bukkit lib");
 			
 			URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
 			
 			URL[] urls = sysLoader.getURLs();
 			
 			for (int url = 0; url < urls.length; url++) {
 				if (urls[url].sameFile(lib.toURI().toURL()))
 					sysLoader.getURLs()[url] = lib.toURI().toURL();
 			}
 			
 			try {
 				Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
 				method.setAccessible(true);
 				method.invoke(sysLoader, lib.toURI().toURL());
 				
 			} catch (Exception e) { return; }
 			
 			return;
 			
 		} catch (Exception e) { e.printStackTrace(); }
 		
 		return;
 	}
 }
