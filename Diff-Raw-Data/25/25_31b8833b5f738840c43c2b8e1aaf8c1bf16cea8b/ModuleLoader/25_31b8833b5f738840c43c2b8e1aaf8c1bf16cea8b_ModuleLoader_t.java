 /**
  * bFundamentals 1.2-SNAPSHOT
  * Copyright (C) 2013  CodingBadgers <plugins@mcbadgercraft.com>
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
 package uk.codingbadgers.bFundamentals.module;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 
 import uk.codingbadgers.bFundamentals.bFundamentals;
 import uk.codingbadgers.bFundamentals.error.ExceptionHandler;
 import uk.codingbadgers.bFundamentals.module.loader.Loader;
 
 /**
  * The ModuleLoader.
  */
 public class ModuleLoader {
 	
 	/** The List of modules. */
 	private final List<Module> m_modules;
 	private Loader m_loader;
 
 	/**
 	 * Instantiates a new module loader.
 	 */
 	public ModuleLoader() {
 		m_modules = new LinkedList<Module>();
 		if (getModuleDir().mkdir())
 			bFundamentals.log(Level.INFO, "Creating Module Directory...");
 	}
 	
 	/**
 	 * Gets the directory of the modules.
 	 *
 	 * @return the module dir
 	 */
 	public File getModuleDir() {
 		return new File(bFundamentals.getInstance().getDataFolder(), "modules");
 	}
 	
 	/**
 	 * Loads all the modules in the base modules directory.
 	 */
 	public void load() {
 		m_loader = new Loader(bFundamentals.getInstance(), getModuleDir());
 		m_modules.addAll(m_loader.sort(m_loader.load()));
 		
 		for (Module module : m_modules) {
 			try {
 				module.onLoad();
 				Bukkit.getHelpMap().addTopic(new ModuleHelpTopic(module));
 				module.log(Level.INFO, module.getName() + " v:" + module.getVersion() + " has been loaded successfuly");
			} catch (Throwable ex) {
 				ExceptionHandler.handleException(ex);
 			}
 		}
 		
 		bFundamentals.log(Level.INFO, "Loaded " + m_modules.size() + " modules.");
 	}
 	
 	/**
 	 * Loads a module with a given name
 	 * 
 	 * @param fileName the files name
 	 */
 	public void load(String fileName) {
 		File module = new File(getModuleDir() + File.separator + fileName + ".jar");
 		load(module);
 	}
 	
 	/**
 	 * Loads a module with a jar file
 	 *  
 	 * @param file the jar file for this module
 	 */
 	public void load(File file) {
 		if (m_loader == null) 
 			m_loader = new Loader(bFundamentals.getInstance(), getModuleDir());
 		
 		if (getModule(file) != null)
 			throw new IllegalArgumentException("Module " + file.getName() + " is already loaded");
 		
 		m_modules.clear();
 		m_modules.addAll(m_loader.sort(m_loader.load(file)));
 		
 		Module module = getModule(file);
 		module.onLoad();
 		module.log(Level.INFO, module.getName() + " v:" + module.getVersion() + " has been loaded successfuly");
 	}
 	
 	/**
 	 * Unloads the modules.
 	 */
 	public void unload() {
 		disable();
 		m_modules.clear();
 	}
 	
 	/**
 	 * Unload a specific module.
 	 *
 	 * @param module the module
 	 */
 	public void unload(Module module) {
 		try {
 			module.setEnabled(false);
 			for (Listener listener : module.getListeners()) {
 				HandlerList.unregisterAll(listener);
 			}
 			m_modules.remove(module);
		} catch (Throwable ex) {
 			ExceptionHandler.handleException(ex);
 		}
 	}
 	
 	/**
 	 * Run on enable in all modules.
 	 */
 	public void enable() {
 		for (Module module : m_modules) {
 			try {
 				module.setEnabled(true);
			} catch (Throwable ex) {
 				ExceptionHandler.handleException(ex);
 			}
 		}
 	}
 	
 	/**
 	 * run on disable in all modules.
 	 */
 	public void disable() {
 		List<Module> modules = new ArrayList<Module>(m_modules);
 		for (Module module : modules) {
 			try {
 				unload(module);
			} catch (Throwable ex) {
 				ExceptionHandler.handleException(ex);
 			}
 		}
 	}
 
 	/**
 	 * Gets the modules.
 	 *
 	 * @return the modules
 	 */
 	public List<Module> getModules() {
 		return m_modules;
 	}
 	
 	
 	/**
 	 * Update all the loaded modules if an updater is set
 	 */
 	public void update() {
 		for (Module module : m_modules) {
 			module.update();
 		}
 	}
 
 	/**
 	 * Gets the module from its name.
 	 *
 	 * @param string the string
 	 * @return the module
 	 */
 	public Module getModule(String string) {
 		Iterator<Module> itr = m_modules.iterator();
 		while(itr.hasNext()) {
 			Module module = itr.next();
 			if (module.getName().equalsIgnoreCase(string)) {
 				return module;
 			}
 		}
 		return null;		
 	}
 	
 	/**
 	 * Gets the module from its file.
 	 *
 	 * @param file the file
 	 * @return the module
 	 */
 	public Module getModule(File file) {
 		Iterator<Module> itr = m_modules.iterator();
 		while(itr.hasNext()) {
 			Module module = itr.next();
 			if (module.getFile().getPath().equalsIgnoreCase(file.getPath())) {
 				return module;
 			}
 		}
 		return null;
 	}
 
 }
