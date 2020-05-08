 package org.rsbot.util;
 
 import org.rsbot.Configuration;
 import org.rsbot.gui.BotGUI;
 import org.rsbot.util.io.HttpClient;
 
 import javax.swing.*;
 import java.io.*;
 import java.net.URL;
 import java.util.logging.Logger;
 
 /**
  * @author Paris
  */
 public final class UpdateChecker {
 	private static final Logger log = Logger.getLogger(UpdateChecker.class.getName());
 	private static int latest = -1;
 
 	public static void notify(final BotGUI instance) {
 		if (new File(Configuration.Paths.ROOT, ".git").exists() && findGit() != null) {
 			log.info("You can check for an update by clicking \"Update Bot\" in the About tab.");
 		} else {
 			if (Configuration.getVersion() >= getLatestVersion()) {
 				return;
 			}
 			log.info("New version available");
 			final int update = JOptionPane.showConfirmDialog(instance, "A newer version is available. Do you wish to update?", "Update Found", JOptionPane.YES_NO_OPTION);
 			if (update != 0) {
 				return;
 			}
 			try {
 				update(instance);
 			} catch (final Exception e) {
 				log.warning("Unable to apply update");
 			}
 		}
 	}
 
 	public static int getLatestVersion() {
 		if (latest != -1) {
 			return latest;
 		}
 		final File cache = new File(Configuration.Paths.getCacheDirectory(), "version-latest.txt");
 		BufferedReader reader = null;
 		try {
 			HttpClient.download(new URL(Configuration.Paths.URLs.VERSION), cache);
 			reader = new BufferedReader(new FileReader(cache));
 			final String s = reader.readLine().trim();
 			reader.close();
 			latest = Integer.parseInt(s);
 			return latest;
 		} catch (final Exception ignored) {
 		} finally {
 			try {
 				if (reader != null) {
 					reader.close();
 				}
 			} catch (final IOException ignored) {
 			}
 		}
 		log.warning("Unable to obtain latest version information");
 		return -1;
 	}
 
 	private static void update(final BotGUI instance) throws IOException {
 		log.info("Downloading update...");
 		final File jarNew = new File(Configuration.NAME + "-" + getLatestVersion() + ".jar");
 		HttpClient.download(new URL(Configuration.Paths.URLs.DOWNLOAD), jarNew);
 		final String jarOld = Configuration.Paths.getRunningJarPath();
 		Runtime.getRuntime().exec("java -jar \"" + jarNew + "\" --delete \"" + jarOld + "\"");
 		instance.cleanExit(true);
 	}
 
 	public static void internalDeveloperUpdate(final BotGUI instance) {
 		if (new File(Configuration.Paths.ROOT, ".git").exists()) {
 			final String git = findGit();
 			if (git == null) {
 				log.severe("You need git installed to perform this action.");
 				return;
 			}
 			final int update = JOptionPane.showConfirmDialog(instance, "Are you sure you want to update?  This might take a while to complete.", "Developers", JOptionPane.YES_NO_OPTION);
 			if (update != 0) {
 				return;
 			}
 			try {
 				log.fine("Saving your stash.");
 				Process p = Runtime.getRuntime().exec(new String[]{git, "stash", "clear"});
 				p.waitFor();
 				p = Runtime.getRuntime().exec(new String[]{git, "stash", "save"});
 				p.waitFor();
 				log.fine("Adding an update remote.");
 				p = Runtime.getRuntime().exec(new String[]{git, "remote", "add", "rsbot_internal_update", "git://github.com/powerbot/RSBot.git"});
 				p.waitFor();
 				log.fine("Fetching the update remote.");
 				p = Runtime.getRuntime().exec(new String[]{git, "fetch", "rsbot_internal_update"});
 				p.waitFor();
 				log.fine("Merging it with our branch, let's hope no errors.");
 				p = Runtime.getRuntime().exec(new String[]{git, "merge", "rsbot_internal_update/master"});
 				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
 				String line;
 				boolean doNotPop = false;
 				while ((line = in.readLine()) != null) {
 					if (line.contains("Already") && line.contains("date")) {
 						log.info("Bot is already up to date, replaying changes.");
 						Runtime.getRuntime().exec(new String[]{git, "stash", "pop"});
 						return;
 					} else if (line.contains("CONFLICT")) {
 						doNotPop = true;
 					}
 				}
 				log.fine("Removing the update remote.");
 				p = Runtime.getRuntime().exec(new String[]{git, "remote", "rm", "rsbot_internal_update"});
 				p.waitFor();
 				log.fine("Running make.bat.");
 				p = Runtime.getRuntime().exec("make.bat");
 				p.waitFor();
 				if (!doNotPop) {
 					log.fine("Poping the change from stash, let's hope there's no conflicts.");
 					Runtime.getRuntime().exec(new String[]{git, "stash", "pop"});
 				} else {
 					log.fine("We didn't pop changes because of conflicts, resolve then pop yourself!");
 				}
 				log.fine("Starting new bot.");
 				Runtime.getRuntime().exec(new String[]{"java", "-jar", "\"RSBot.jar\""});
 				log.fine("Closing old bot.");
 				instance.cleanExit(true);
 			} catch (InterruptedException ie) {
 				log.severe("Could not update via Git...");
 				return;
 			} catch (IOException ioe) {
 				log.severe("Could not update via Git...");
 				return;
 			}
 		} else {
 			log.severe("The JAR is not running from a git directory.");
 		}
 	}
 
	private static String findGit() {
 		final File x64 = new File("C:\\Program Files (x86)\\Git\\bin\\git.exe");
 		final File x32 = new File("C:\\Program Files\\Git\\bin\\git.exe");
 		if (x64.exists()) {
 			return x64.getAbsolutePath();
 		} else if (x32.exists()) {
 			return x32.getAbsolutePath();
 		}
 		return null;
 	}
 }
