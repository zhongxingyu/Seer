 package com.m0pt0pmatt.menuservice;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 import com.m0pt0pmatt.menuservice.api.AbstractComponent;
 import com.m0pt0pmatt.menuservice.api.Component;
 import com.m0pt0pmatt.menuservice.api.ContainerAttribute;
 import com.m0pt0pmatt.menuservice.api.Menu;
 import com.m0pt0pmatt.menuservice.api.MenuComponent;
 
 /**
  * This class converts a YAML file into a MenuObject, or a MenuObject into a YAML file
  * 
  * @author Matthew Broomfield
  *
  */
 class YAMLBuilder {
 
 	
 	public boolean saveYAML(Plugin plugin, Menu menu, String fileName){
 		
 		//create file
 		File configFile = new File(plugin.getDataFolder(), fileName);
 		if (!configFile.exists()){
 			try {
 				configFile.createNewFile();
 			} catch (IOException e) {
 				plugin.getLogger().warning("Unable to create config file!");
 				return false;
 			}
 		}
 		YamlConfiguration config = new YamlConfiguration();
 		
 		if (!(menu instanceof MenuComponent)){
 			plugin.getLogger().warning("Menu is not a MenuComponent. Don't know how to save!");
 			return false;
 		}
 		
 		saveComponent(config, (MenuComponent) menu);
 		
 		try {
 			config.save(configFile);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private void saveComponent(MemorySection config, Component component) {
 		
 		if (!(config instanceof YamlConfiguration)){
 			config.createSection(component.getTag());
 			config = (MemorySection) config.get(component.getTag());
 		}
 		
 		for (Entry<String, Object> attribute: component.getAttributes().entrySet()){
 			if (attribute.getValue() instanceof ContainerAttribute){
 				saveContainerAttribute(config, (ContainerAttribute) attribute.getValue());
 			}else {
 				config.set(attribute.getKey(), attribute.getValue());
 			}
 		}
 		
 		if (component.getType().equalsIgnoreCase("menu")){
 			saveMenu(config, (MenuComponent) component);
 		}
 		
 	}
 
 	private void saveMenu(MemorySection config, MenuComponent menu) {
 		config.createSection("components");
 		config = (MemorySection) config.get("components");
 		for (Entry<String, Component> component: menu.getComponents().entrySet()){
 			saveComponent(config, component.getValue());
 		}
 		
 	}
 
 	private void saveContainerAttribute(MemorySection config, ContainerAttribute attribute) {
 		config.createSection(attribute.getName());
 		config = (MemorySection) config.get(attribute.getName());
 		
 		for (Entry<String, Object> entry: attribute.getAttributes().entrySet()){
 			if (entry.getValue() instanceof ContainerAttribute){
 				saveContainerAttribute(config, (ContainerAttribute) entry.getValue());
 			}else {
 				config.set(entry.getKey(), entry.getValue());
 			}
 		}
 		
 	}
 
 	public Menu loadYAML(Plugin plugin, String fileName){
 		
 		//check for null plugin
 		if (plugin == null){
 			return null;
 		}
 		
 		//check for null fileName
 		if (fileName == null){
 			plugin.getLogger().warning("No fileName given. Could not load menu.");
 			return null;
 		}
 		
 		
 		
 		//make the plugin data folder if it doesn't exist
 		if (!plugin.getDataFolder().exists()){
 			plugin.getDataFolder().mkdir();
 		}
 		
 		//Try to open the menu file
 		File menuFile = new File(plugin.getDataFolder(), fileName);
 		
 		//make sure the file was found, otherwise fail
 		if (!menuFile.exists()){
 			plugin.getLogger().warning(fileName + " was not found. Could not load menu.");
 			return null;
 		}
 		
 		plugin.getLogger().warning(fileName + " was found.");
 				
 		//create the yaml Object
 		YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(menuFile);
 		if (yamlConfig == null){
 			plugin.getLogger().warning("could not create YamlConfiguration for " + fileName + ". Could not load menu.");
 			return null;
 		}
 		
 		//from the configuration, gather the menu
		return (Menu) loadMenuComponent(yamlConfig, plugin);
 	}
 	
 	/**
 	 * Grabs the type of the MenuComponent and creates the Component.
 	 * This is needed because the Object type depends on the type of the Component
 	 * @param yamlConfig The current MenorySection
 	 * @return The MenuComponent, which is either an AbstractComponent or a MenuComponent
 	 */
 	private Component grabType(MemorySection yamlConfig){
 		
 		Component component;
 		
 		//get the type
 		if (yamlConfig.contains("type")){
 			
 			String type = (String) yamlConfig.get("type");			
 			
 			if (type.equals("menu")){
 				component = new MenuComponent();
 				component.setType("menu");
 			}
 			else{
 				component = new AbstractComponent();
 				component.setType(type);
 			}
 			
 		}
 		else{
 			component = new AbstractComponent();
 			component.setType("no-type");
 		}
 		return component;
 	}
 	
 	/**
 	 * Grabs the tag for the MenuComponent.
 	 * If there is no tag, the name of the MemorySection is used.
 	 * @param yamlConfig The current MemorySection
 	 * @return
 	 */
 	private String grabTag(MemorySection yamlConfig){
 		
 		//get the tag if it is not already specified
 		if (yamlConfig.contains("tag")){
 			return yamlConfig.getString("tag");
 		}
 		
 		return yamlConfig.getName();
 	}
 
 	/**
 	 * Loads a Component from the current MemorySection.
 	 * A Plugin is needed in case the Component is stoed in another file.
 	 * @param yamlConfig
 	 * @param plugin
 	 * @return
 	 */
 	private Component loadMenuComponent(MemorySection yamlConfig, Plugin plugin){
 		
 		Set<String> collectedAttributes = new HashSet<String>();
 		
 		//grab the type, exiting if there is none
 		Component component = grabType(yamlConfig);
 		if (component == null){
 			return null;
 		}
 		collectedAttributes.add("type");
 		
 		//Grab the tag
 		component.setTag(grabTag(yamlConfig));
 		collectedAttributes.add("tag");
 		
 		//check if plugin is specified
 		if (yamlConfig.contains("plugin") && (yamlConfig.get("plugin") instanceof String)){
 			component.addAttribute("plugin", yamlConfig.get("plugin"));
 		} else{
 			component.addAttribute("plugin", plugin.getName());
 		}
 		collectedAttributes.add("plugin");
 		
 		//check if filename is specified. If it is, load the correct configuration file
 //		if (yamlConfig.contains("file")){
 //			
 //			//find the plugin
 //			Plugin newPlugin = Bukkit.getPluginManager().getPlugin((String) component.getAttribute("plugin"));
 //			if (newPlugin != null){
 //			
 //				File file = new File(newPlugin.getDataFolder(), yamlConfig.getString("file"));
 //				if (file.exists()){
 //					YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(file);
 //					return loadMenuComponent(newConfig, newPlugin);
 //				}
 //			}
 //		}
 		
 		//filename was not specified. Therefore, this config file must have the details for the MenuComponent
 		
 		
 		//if a menu, go get menu specific details
 		if (component.getType().equals("menu")){
 			loadMenu(yamlConfig, (MenuComponent) component, plugin);
 		}
 		collectedAttributes.add("components");
 		
 		//collect other attributes
 		for (String name: yamlConfig.getValues(false).keySet()){
 			if (!collectedAttributes.contains(name)){
 				
 				if (yamlConfig.get(name) instanceof MemorySection){
 					ContainerAttribute container = getContainerAttribute((MemorySection) yamlConfig.get(name));
 					component.addAttribute(container.getName(), container);
 				}
 				else{
 					//create and add attribute
 					component.addAttribute(name, yamlConfig.get(name));
 				}
 				
 			}
 		}	
 		
 		return component;
 	}
 
 	private ContainerAttribute getContainerAttribute(MemorySection yamlConfig){
 		Map<String, Object> attributes = new HashMap<String, Object>();
 		
 		for (Entry<String, Object> attribute: yamlConfig.getValues(false).entrySet()){
 			if (attribute.getValue() instanceof MemorySection){
 				ContainerAttribute con = getContainerAttribute((MemorySection) yamlConfig.get(attribute.getKey()));
 				attributes.put(con.getName(), con);
 			}
 			else{
 				attributes.put(attribute.getKey(), attribute.getValue());
 			}
 		}
 		
 		ContainerAttribute container = new ContainerAttribute(yamlConfig.getName(), attributes);
 		return container;
 	}
 	
 	
 	private void loadMenu(MemorySection yamlConfig, MenuComponent menu, Plugin plugin){
 		
 		//get the components
 		if (yamlConfig.contains("components")){
 
 			MemorySection components = (MemorySection) yamlConfig.get("components");
 			for (String path: components.getValues(false).keySet()){
 				Component component = loadMenuComponent((MemorySection)components.get(path), plugin);				
 				menu.addComponent(component);
 			}
 			
 		}
 		
 	}
 }
