 /*
  * Copyright (C) 2006-2010 Thomas Chemineau
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package net.aepik.casl.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.jar.*;
 
 /**
  * Un objet PluginsManager gère l'ensemble des plugins de l'application.
  * Ce manager permet d'éxécuter les plugins, de les lister, de les instancier
  * en mémoire.
  */
 
 public class PluginManager
 {
 
 	/*
 	 * Le nom de l'interface de plugin dans le paquetage.
 	 */
 	public final static String PLUGIN_INTERFACE_NAME = "net.aepik.casl.core.Plugin";
 
 	/**
 	 * Le manager de l'application
 	 */
 	private Manager manager;
 
 	/**
 	 * Le chemin du répertoire contenant tous les plugins
 	 */
 	private String path;
 
 	/**
 	 * L'ensemble des plugins
 	 */
 	private Plugin[] plugins;
 
 	/**
 	 * Build a new PluginManager object.
 	 * @param m A Manager object.
 	 * @param path A path.
 	 */
 	public PluginManager (Manager m, String path)
 	{
 		this.manager = m;
 		this.path = path ;
 		this.plugins = new Plugin[0];
 	}
 
 	/**
 	 * Build a Plugin instance.
 	 * @param file A jar file
 	 * @param filename The class to instanciate
 	 * @return Plugin A Plugin object.
 	 */
 	public static Plugin createPluginInstance (File file, String filename) throws IllegalAccessException, InstantiationException
 	{
 		if (filename.length() <= 6 || !filename.substring(filename.length()-6).equals(".class"))
 		{
 			return null;
 		}
 		URLClassLoader loader = null;
 		try
 		{
 			loader = new URLClassLoader(new URL[]{
 				file.toURI().toURL()
 			});
 		}
 		catch (MalformedURLException e1)
 		{
 			e1.printStackTrace();
 			return null;
 		}
 		Class class1 = null;
 		try
 		{
 			class1 = Class.forName(
 				filename.substring(0, filename.length()-6).replace('/', '.'),
 				false,
 				loader
 			);
 		}
 		catch (ClassNotFoundException e2)
 		{
 			e2.printStackTrace();
 			return null;
 		}
 		boolean instanciate = false;
 		Class[] interfaces = ((Class<?>) class1.getSuperclass()).getInterfaces();
 		for (int i = 0; i < interfaces.length && !instanciate; i++)
 		{
 			if (interfaces[i].getName().equals(PLUGIN_INTERFACE_NAME))
 			{
 				instanciate = true;
 			}
 		}
 		if (instanciate)
 		{
 			return (Plugin) class1.newInstance();
 		}
 		return null;
 	}
 
 	/**
 	 * Retourne le manager de l'application.
 	 * @return Manager Le manager de l'application.
 	 */
 	public Manager getManager ()
 	{
 		return manager;
 	}
 
 	/**
 	 * Retourne l'ensemble des plugins.
 	 * @return String[] L'ensemble des noms de plugins.
 	 */
 	public Plugin[] getPlugins ()
 	{
		return plugins;
 	}
 
 	/**
 	 * List jar files.
 	 * @param file A jar file.
 	 * @return String[] Entry name into the jar file.
 	 */
 	private static String[] listJarEntries (File file)
 	{
 		String filename = file.getName();
 		if (filename.length() <= 4 || !filename.substring(filename.length()-4).equals(".jar"))
 		{
			return new String[0];
 		}
 		Enumeration<JarEntry> entries = null;
 		try
 		{
 			entries = (new JarFile(file)).entries();
 		}
 		catch (IOException e1)
 		{
 			e1.printStackTrace();
 			return new String[0];
 		}
 		JarEntry[] files = Collections.list(entries).toArray(new JarEntry[0]);
 		String[] filenames = new String[files.length];
 		for (int i = 0; i < files.length; i++)
 		{
 			filenames[i] = files[i].getName();
 		}
 		return filenames;
 	}
 
 	/**
 	 * Trouve les plugins dans le répertoire des plugins.
 	 * Pour accéder aux plugins, il faut appeler la méthode getPlugins().
 	 * @return boolean True si le chargement des plugins a réussi, false sinon.
 	 */
 	public boolean loadPlugins ()
 	{
 		File pluginsDir = new File(path);
 		if (!pluginsDir.exists())
 		{
 			return false;
 		}
 		Vector<Plugin> pluginsVector = new Vector<Plugin>();
 		for (File file : pluginsDir.listFiles())
 		{
 			if (file.isDirectory())
 			{
 				continue;
 			}
 			Plugin plugin = null;
 			try
 			{
 				String[] files = listJarEntries(file);
 				for (int i = 0; i < files.length && plugin == null; i++)
 				{
 					plugin = createPluginInstance(file, files[i]);
 				}
 			}
 			catch (IllegalAccessException e2)
 			{
 				e2.printStackTrace();
 				continue;
 			}
 			catch (InstantiationException e3)
 			{
 				e3.printStackTrace();
 				continue;
 			}
 			if (plugin != null)
 			{
 				pluginsVector.add(plugin);
 			}
 		}
 		if (pluginsVector.size() == 0)
 		{
 			return false;
 		}
 		this.plugins = pluginsVector.toArray(new Plugin[0]);
 		Arrays.sort(this.plugins);
 		return true;
 	}
 
 }
