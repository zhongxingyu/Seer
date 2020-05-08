 /*
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2010 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis;
 
 import java.awt.*;
 import java.io.*;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.vecmath.Color3f;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.XMLConfiguration;
 
 import com.xith3d.scenegraph.PolygonAttributes;
 
 import org.nees.rpi.vis.loaders.Loader;
 
 
 /**
  * A singleton class that stores the application settings.
  *
  * Application settings that could cause an application error
  * if tweaked by the user are left as instance variables. The rest
  * is stored to an xml configuration file in the user's home directory.
  */
 public class AppSettings extends XMLConfiguration
 {
 	private static final Logger logger = Logger.getLogger("org.nees.rpi.vis");
 	
 	private static AppSettings instance;
 
     private float minOpenGLVersion = 1.2f;
 
     private String version = "Version 1.6.0dev";
 
 	private String m3dvFileChooserDescriptor = "3DDV Model Files (*.m3dv, *.3ddv)";
 
 	private int defaultPolygonFill = PolygonAttributes.POLYGON_FILL;
 	private Color defaultShapeFillColor = Color.decode("#888888");
 	private Color defaultShapeBorderColor = Color.decode("#000000");
 	private float defaultShapeTransparency = 0.5f;
 
 	private Color defaultBackgroundColor = Color.WHITE;
 	private Color defaultDialogBackgroundColor = Color.decode("#F8F4D4");
 	private Color fadeColor = Color.decode("#AAAAAA");
 	private Color defaultTextColor = Color.decode("#333333");
 	private Font defaultFont = new Font("Tahoma", Font.PLAIN, 13);
 	private Font metaFont = new Font("Tahoma", Font.PLAIN, 12);
 
 	private float defaultRotX;
 	private float defaultRotY;
 
 	private String applicationTitle;
 
 	private Collection<Loader> loaders;
 
 	/**
 	 * Before the private constructor is called, ensure that the settings
 	 * file exists. If it doesn't exist the default file is copied to the
 	 * user's home directory.
 	 */
 	static
 	{
 		instance = null;
 		try
 		{
 			AppSettings.createSettingsFile();
 			instance = new AppSettings();
 		} catch (FileNotFoundException e) {
 			logger.log(Level.SEVERE, "Could not find configuration file", e);
 		} catch(IOException e) {
 			logger.log(Level.SEVERE, "Could not load configuration file", e);
 		} catch(ConfigurationException e) {
 			logger.log(Level.SEVERE, "Could not process configuration file", e);
 		}
 	}
 	
 	private static String getSettingsFilePath()
 	{
 		String xmlName = "3ddv.xml";
 		String path = System.getProperty("user.home") + "/" + xmlName;
 		
 		// HACK: Create an invisible XML file on Linux/BSD, or put it in
 		// ~/Library/Preferences on OS X, because I'm tired of having the
 		// config file show up in the home folder. I don't really care if
 		// it puts it there on Windows, though.
 		if (System.getProperty("os.name").equals("Linux") || System.getProperty("os.name").equals("FreeBSD")) {
 			path = System.getProperty("user.home") + "/." + xmlName;
 		} else if (System.getProperty("os.name").equals("Mac OS X")) {
 			path = System.getProperty("user.home") + "/Library/Preferences/" + xmlName;
 		}
 		
 		return path;
 	}
 	
 	private static void createSettingsFile() throws FileNotFoundException, IOException
 	{
 		String oldPath = System.getProperty("user.home") + "/3ddv.xml";
 		String newPath = getSettingsFilePath();
 		File oldFile = new File(oldPath);
 		File newFile = new File(newPath);
 		
 		// Migrate the old config file to the new file system location
 		// (if it exists)
 		if (!newPath.equals(oldPath) && oldFile.exists()) {
 			logger.info("Migrating old config file to new location");
 			
 			InputStream in = new FileInputStream(oldFile);
 			OutputStream out = new FileOutputStream(newFile);
 		
 			byte[] buf = new byte[1024];
 			int n = 0;
 			while ((n = in.read(buf)) != -1) {
 				out.write(buf, 0, n);
 			}
 			
 			in.close();
 			out.close();
 			oldFile.delete();
 		}
 		
 		// Create a new file config file based on the default config options,
 		// if such a file does not exist.
 		if (!newFile.exists()) {
 			logger.info("Creating default configuration file");
 			
 			InputStream in = AppSettings.class.getResource("/3ddv.default.xml").openStream();
 			OutputStream out = new FileOutputStream(newFile);
 		
 			byte[] buf = new byte[1024];
 			int n = 0;
 			while ((n = in.read(buf)) != -1) {
 				out.write(buf, 0, n);
 			}
 			
 			in.close();
 			out.close();
 		}
 	}
 
 	private AppSettings() throws ConfigurationException
 	{
 		super(AppSettings.getSettingsFilePath());
 
 		//Removing the auto-save option for now. The way the resource
 		//is stored with Webstart makes it uneditable from within the
 		//jar, need to find an alternate solution - Hassan 7/25
 		setAutoSave(true);
 
 		initLoadersInfo();
 
 		applicationTitle = getString("application.title");
 
 		defaultRotX = 0.58f;
 		defaultRotY = 0.79f;
 	}
 
 	public static AppSettings getInstance()
 	{
 		return instance;
 	}
 
 	public static void initFiles()
 	{
 
 	}
 
 
 	private void initLoadersInfo()
 	{
 		loaders = new ArrayList<Loader>();
 
 		String className, displayText;
 		int counter = 0;
 		do
 		{
 			displayText = getString("loaders.loader(" + counter + ").display-text");
 			className = getString("loaders.loader(" + counter + ").classname");
 			if (className != null)
 			{
 				try
 				{
 					Class loaderClass = Class.forName(className);
 					Loader loader = (Loader) loaderClass.newInstance();
 					if (displayText != null)
 						loader.setDisplayText(displayText);
 					else
 						loader.setDisplayText(className);
 					loaders.add(loader);
 				}
 				catch (ClassNotFoundException e)
 				{
 					//TODO proper error handling
 					e.printStackTrace();	//To change body of catch statement use File | Settings | File Templates.
 				}
 				catch (IllegalAccessException e)
 				{
 					//TODO proper error handling
 					e.printStackTrace();	//To change body of catch statement use File | Settings | File Templates.
 				}
 				catch (InstantiationException e)
 				{
 					//TODO proper error handling
 					e.printStackTrace();	//To change body of catch statement use File | Settings | File Templates.
 				}
 			}
 			counter++;
 		} while (className != null);
 	}
 
 	public String getApplicationTitle()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return applicationTitle;
 	}
 
 	public String getApplicationVersion()
 	{
 		return version;
 	}
 
 	public int getDefaultPolygonFill()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultPolygonFill;
 	}
 
 	public float getDefaultRotX()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultRotX;
 	}
 
 	public float getDefaultRotY()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultRotY;
 	}
 
     public float getMinOpenGLVersion()
     {
         return minOpenGLVersion;
     }
 
     public Color getDefaultBackgroundColor()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultBackgroundColor;
 	}
 
     public Color getDefaultDialogBackgroundColor()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultDialogBackgroundColor;
 	}
 
 	public Color getFadeColor()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return fadeColor;
 	}
 
 	public Color getDefaultTextColor()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultTextColor;
 	}
 
 	public Font getDefaultFont()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultFont;
 	}
 
 	public String getM3dvFileChooserDescriptor()
 	{
 		return m3dvFileChooserDescriptor;
 	}
 
 	public Font getMetaFont()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return metaFont;
 	}
 
 	public Color getDefaultShapeFillColor()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultShapeFillColor;
 	}
 
 	public Color getDefaultShapeBorderColor()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultShapeBorderColor;
 	}
 
 	public float getDefaultShapeTransparency()
 	{
 		//TODO refactor to use default XMLConfiguration methods
 		return defaultShapeTransparency;
 	}
 
 	public Collection getLoaders()
 	{
 		return loaders;
 	}
 }
