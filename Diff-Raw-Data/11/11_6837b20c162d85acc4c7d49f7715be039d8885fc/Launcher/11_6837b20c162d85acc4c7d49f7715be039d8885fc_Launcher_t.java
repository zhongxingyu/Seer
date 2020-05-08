 package org.whired.ghostclient.updater;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.logging.Level;
 import javax.swing.SwingUtilities;
 import org.whired.ghost.Vars;
 import org.whired.ghostclient.updater.io.HttpClient;
 import org.whired.ghostclient.updater.ui.UpdaterForm;
 
 /**
  * Downloads and launches the newest version of GHOST
  *
  * @author Whired
  */
 public class Launcher
 {
 
 	/** Where the packages are hosted */
 	//private static final String REMOTE_CODEBASE = "https://raw.github.com/Whired/jghost-portable/master/dist/"; // Stable branch
 	private static final String REMOTE_CODEBASE = "https://raw.github.com/Whired/jghost-portable/master/dist/dev/"; // Dev branch
 	/** Where the packages are save */
 	private static final String LOCAL_CODEBASE = System.getProperty("user.home") + "/.ghost/";
 	/** The first version of this program */
	private static final String FIRST_PACKAGE = "ghostclient0-0-0.jar";
 
 	public static void main(String[] args) throws URISyntaxException, IOException
 	{
 		SwingUtilities.invokeLater(new Runnable()
 		{
 
 			public void run()
 			{
 				final UpdaterForm form = new UpdaterForm();
 				new Thread(new Runnable()
 				{
 
 					@Override
 					public void run()
 					{
 						// Find the latest local codebase, if any
 						File base = new File(LOCAL_CODEBASE);
 						String newestLocPkg = null;
 						if (!base.exists())
 						{
 							base.mkdirs();
 						}
 						File[] locPkgs = base.listFiles();
 						if (locPkgs != null && locPkgs.length > 0)
 						{
 							newestLocPkg = findNewest(locPkgs);
 						}
 
 						String pathToJar = LOCAL_CODEBASE + (newestLocPkg != null ? newestLocPkg : FIRST_PACKAGE);
 						System.out.println(pathToJar);
 
						final String pkgName = pathToJar.substring(pathToJar.lastIndexOf("/ghostclient") + 1, pathToJar.length());
 						String newPkg = null;
 						form.log("Checking for update..");
 						try
 						{
 							// Check for update
 							if (!doesExist(pkgName))
 							{
 								form.log("Update found for " + pkgName + ".");
 
 								newPkg = getNextPackage(pkgName);
 								while (!doesExist(newPkg))
 								{
 									form.log(newPkg + " not found.");
 									newPkg = getNextPackage(newPkg);
 								}
 								form.log("New package found: " + newPkg);
 								form.log("Downloading..");
 								HttpClient.saveToDisk(LOCAL_CODEBASE + newPkg, REMOTE_CODEBASE + newPkg);
 								form.log("Download complete.");
 								pathToJar = pathToJar.replace(pkgName, newPkg);
 							}
 							else
 							{
 								form.log("Current package is up-to-date.");
 							}
 						}
 						catch (IOException e)
 						{
 							form.log("Error while updating: " + e.toString());
 							if (Vars.getLogger().getLevel().intValue() <= Level.FINE.intValue())
 							{
 								e.printStackTrace();
 							}
 						}
 
 						form.log("Launching "+(newPkg == null ? pkgName : newPkg).replace(".jar", "")+"..");
 
 						// Run
 						try
 						{
 							ProcessBuilder pb = new ProcessBuilder("java", "-classpath", pathToJar, "org.whired.ghostclient.Main");
 							pb.start();
 							form.log("Exiting..");
 							try
 							{
 								Thread.sleep(4000);
 							}
 							catch (InterruptedException ex)
 							{}
 							System.exit(0);
 						}
 						catch (IOException e)
 						{
 							form.log("Unable to launch GHOST: " + e.toString());
 						}
 					}
 				}).start();
 			}
 		});
 	}
 
 	/**
 	 * Gets the next package name based on the name of the current package
 	 *
 	 * @param curPkg the name of the current package
 	 */
 	private static String getNextPackage(String curPkg)
 	{
 		// The build numbers
 		int major, minor, rev;
 
 		String[] parts = getVersionParts(curPkg);
 
 		major = Integer.parseInt(parts[0]);
 		minor = Integer.parseInt(parts[1]);
 		rev = Integer.parseInt(parts[2]);
 
 		if (rev + 1 < 10)
 		{
 			rev++;
 		}
 		else
 		{
 			rev = 0;
 			if (minor + 1 < 10)
 			{
 				minor++;
 			}
 			else
 			{
 				minor = 0;
 				major++;
 			}
 		}
		return "ghostclient" + major + "-" + minor + "-" + rev + ".jar";
 	}
 
 	/**
 	 * Checks whether or not a package is being hosted
 	 * @param pkgName the package to check
 	 * @return {@code true} if the package was found, otherwise {@code false}
 	 * @throws IOException if the server can't be reached or if an invalid status code is returned
 	 */
 	private static boolean doesExist(String pkgName) throws IOException
 	{
 		int stat = -1;
 		stat = HttpClient.getStatus(REMOTE_CODEBASE + pkgName);
 		if (stat == 404)
 		{
 			return false;
 		}
 		else if (stat == 200)
 		{
 			return true;
 		}
 		else
 		{
 			throw new IOException("Unexpected status: " + stat);
 		}
 	}
 
 	/**
 	 * Finds the newest package out of a given collection of packages
 	 *
 	 * @param bases the collection to compare
 	 * @return the name of the newest local package
 	 */
 	private static String findNewest(File[] bases)
 	{
 		File newest = null;
 		for (File f : bases)
 		{
 			if (newest == null)
 			{
 				newest = f;
 			}
 			else
 			{
 				newest = getNewest(newest, f);
 			}
 		}
 		return newest.getName();
 	}
 
 	/**
 	 * Compares two files to find the newest version
 	 *
 	 * @param base1
 	 * @param base2
 	 * @return the file with the newest version
 	 */
 	private static File getNewest(File base1, File base2)
 	{
 		int b1Maj, b1Min, b1Rev;
 		int b2Maj, b2Min, b2Rev;
 
 		String[] b1Parts = getVersionParts(base1.getName());
 		String[] b2Parts = getVersionParts(base2.getName());
 
 		b1Maj = Integer.parseInt(b1Parts[0]);
 		b1Min = Integer.parseInt(b1Parts[1]);
 		b1Rev = Integer.parseInt(b1Parts[2]);
 
 		b2Maj = Integer.parseInt(b2Parts[0]);
 		b2Min = Integer.parseInt(b2Parts[1]);
 		b2Rev = Integer.parseInt(b2Parts[2]);
 
 		int b1Ver = b1Maj * 100 + b1Min * 10 + b1Rev;
 		int b2Ver = b2Maj * 100 + b2Min * 10 + b2Rev;
 
 		if (b1Ver >= b2Ver)
 		{
 			return base1;
 		}
 		else
 		{
 			return base2;
 		}
 	}
 
 	/**
 	 * Strips information away from a package name and parses the version numbers
 	 *
 	 * @param pkgName the name of the package to parse
 	 * @return the version numbers (major, minor, revision)
 	 */
 	private static String[] getVersionParts(String pkgName)
 	{
		return pkgName.replace("ghostclient", "").replace(".jar", "").split("-");
 	}
 }
