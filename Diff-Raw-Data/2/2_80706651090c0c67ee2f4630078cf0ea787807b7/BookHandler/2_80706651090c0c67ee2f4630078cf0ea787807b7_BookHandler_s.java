 package com.amoebaman.kitmaster.handlers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 
 public class BookHandler {
 
 	private static final YamlConfiguration yaml = new YamlConfiguration();
 	
 	public static void load(File file) throws IOException, InvalidConfigurationException{
 		yaml.load(file);
 	}
 	
 	public static void save(File file) throws IOException{
 		yaml.save(file);
 	}
 	
 	public static void addSample(){
 		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
 		BookMeta meta = (BookMeta) book.getItemMeta();
 		meta.setTitle("Sample Book");
 		meta.setAuthor("AmoebaMan");
 		meta.setPages("This is a sample book to show you how KitMaster saves books.", "You can add books manually in this config by following the same format you see here.", "Each line corresponds to a single page.");
 		book.setItemMeta(meta);
 		saveBook(book, "sample");
 	}
 	
 	private static ConfigurationSection getSection(String name){
 		ConfigurationSection section = yaml.getConfigurationSection(name);
 		if(section == null)
 			for(String key : yaml.getKeys(false))
 				if(key.equalsIgnoreCase(name))
 					section = yaml.getConfigurationSection(key);
 		return section;
 	}
 
 	public static boolean isBook(String name){
 		return getSection(name) != null;
 	}
 	
 	public static void saveBook(ItemStack book, String name){
 		if(book.getType() != Material.WRITTEN_BOOK)
 			return;
 		BookMeta meta = (BookMeta) book.getItemMeta();
 		ConfigurationSection bookYaml = yaml.createSection(name);
 		bookYaml.set("title", meta.getTitle());
 		bookYaml.set("author", meta.getAuthor());
 		List<String> pages = meta.getPages();
 		for(int i = 0; i < pages.size(); i++)
 			pages.set(i, pages.get(i).replace("\n", "|n").replace("\r", "|r"));
 		bookYaml.set("pages", pages);
 	}
 	
 	public static ItemStack loadBook(ItemStack book, String name){
 		if(book.getType() != Material.WRITTEN_BOOK)
 			return book;
 		ConfigurationSection bookYaml = getSection(name);
 		if(bookYaml == null)
 			return book;
 		BookMeta meta = (BookMeta) book.getItemMeta();
 		meta.setTitle(bookYaml.getString("title"));
 		meta.setAuthor(bookYaml.getString("author"));
 		List<String> pages = bookYaml.getStringList("pages");
 		for(int i = 0; i < pages.size(); i++)
 			pages.set(i, pages.get(i).replace("|n", "\n").replace("|r", "\r"));
		meta.setPages(bookYaml.getStringList("pages"));
 		book.setItemMeta(meta);
 		return book;
 	}
 	
 	public static ItemStack getBook(String name){
 		return loadBook(new ItemStack(Material.WRITTEN_BOOK), name);
 	}
 	
 	public static String getBookName(ItemStack book){
 		for(String name : yaml.getKeys(false))
 			if(getBook(name).getItemMeta().equals(book.getItemMeta()))
 				return name;
 		return null;
 	}
 	
 }
