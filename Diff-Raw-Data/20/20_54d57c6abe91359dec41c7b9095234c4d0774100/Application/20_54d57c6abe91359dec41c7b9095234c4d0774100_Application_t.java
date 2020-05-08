 package org.rsbot;
 
 import org.rsbot.bot.Bot;
 import org.rsbot.gui.BotGUI;
 import org.rsbot.log.LogOutputStream;
 import org.rsbot.log.SystemConsoleHandler;
 import org.rsbot.security.RestrictedSecurityManager;
 import org.rsbot.util.io.IOHelper;
 
 import java.awt.*;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
import javax.swing.JOptionPane;

 public class Application {
 	private static BotGUI gui;
 
 	public static void main(final String[] args) {
 		try {
 			bootstrap();
 			extractResources();
 			commands(args);
 			System.setSecurityManager(new RestrictedSecurityManager());
 			System.setProperty("java.io.tmpdir", Configuration.Paths.getGarbageDirectory());
 			gui = new BotGUI();
 			gui.setVisible(true);
 		} catch (final Exception e) {
 			e.printStackTrace();
 			System.out.print(e.getMessage());
			try {
				if (gui != null) {
					gui.setVisible(false);
				}
			} catch (final Exception ignored) {
			}
			final String msg = "Error: " + e.toString() + "\nUnable to start.";
			try {
				JOptionPane.showMessageDialog(null, msg, Configuration.NAME, JOptionPane.ERROR_MESSAGE);
			} catch (final HeadlessException ignored) {
			}
			try {
				if (gui != null) {
					gui.cleanExit(false);
				}
			} catch (final Exception ignored) {
			}
 			System.exit(1);
 		}
 	}
 
 	private static void commands(final String[] args) {
 		if (args.length > 1) {
 			if (args[0].toLowerCase().endsWith("delete")) {
 				final File jarOld = new File(args[1]);
 				if (jarOld.exists()) {
 					if (!jarOld.delete()) {
 						jarOld.deleteOnExit();
 					}
 				}
 			}
 		}
 	}
 
 	public static void extractResources() {
 		final ArrayList<String> extract = new ArrayList<String>(2);
 		if (Configuration.getCurrentOperatingSystem() == Configuration.OperatingSystem.WINDOWS) {
 			extract.add(Configuration.Paths.COMPILE_SCRIPTS_BAT);
 			extract.add(Configuration.Paths.COMPILE_FIND_JDK);
 		} else {
 			extract.add(Configuration.Paths.COMPILE_SCRIPTS_SH);
 		}
 		for (final String item : extract) {
 			final String path = Configuration.Paths.Resources.ROOT + "/" + item;
 			final InputStream in;
 			try {
 				in = Configuration.getResourceURL(path).openStream();
 			} catch (IOException ignored) {
 				continue;
 			}
 			final File output = new File(Configuration.Paths.getHomeDirectory(), item);
 			IOHelper.write(in, output);
 		}
 	}
 
 	/**
 	 * Returns the Bot for any object loaded in its client. For internal use
 	 * only (not useful for script writers).
 	 *
 	 * @param o Any object from within the client.
 	 * @return The Bot for the client.
 	 */
 	public static Bot getBot(final Object o) {
 		return gui.getBot(o);
 	}
 
 	/**
 	 * Returns the size of the panel that clients should be drawn into. For
 	 * internal use.
 	 *
 	 * @return The client panel size.
 	 */
 	public static Dimension getPanelSize() {
 		return gui.getPanel().getSize();
 	}
 
 	private static void bootstrap() {
 		Logger.getLogger("").setLevel(Level.ALL);
 		Logger.getLogger("").addHandler(new SystemConsoleHandler());
 		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
 			private final Logger log = Logger.getLogger("EXCEPTION");
 
 			public void uncaughtException(final Thread t, final Throwable e) {
 				log.logp(Level.SEVERE, "EXCEPTION", "", "Unhandled exception in thread " + t.getName() + ": ", e);
 			}
 		});
 		System.setErr(new PrintStream(new LogOutputStream(Logger.getLogger("STDERR"), Level.SEVERE), true));
 	}
 }
