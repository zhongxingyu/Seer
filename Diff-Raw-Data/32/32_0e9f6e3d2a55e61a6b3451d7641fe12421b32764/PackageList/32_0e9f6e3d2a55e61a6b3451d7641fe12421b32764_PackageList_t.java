 package com.asylumsw.bukkit.kits;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Player;
 
 public class PackageList extends FileLoader {
 	protected HashMap<String, Package> packages;
 
 	public PackageList() {
 		this.filename = "kits.txt";
 		this.packages = new HashMap<String,Package>();
 	}
 
 	public void listPackages(Player player) {
		player.sendMessage(ChatColor.GRAY + "Available Kits:");
 		for( Map.Entry<String, Package> pkg: packages.entrySet() ) {
 			player.sendMessage(pkg.getValue().getChatMessage());
 		}
 	}
 
 	protected void addPackage(String[] tokens) {
 		String[] pkgInfo = tokens[0].split(":");
 		if( 2 > pkgInfo.length ) {
 			System.out.println("Invalid Package: "+tokens[0]);
 			return;
 		}
 
 		Package pkg;
 		int cooldown;
 
 		try {
 			cooldown = Integer.parseInt(pkgInfo[1]);
 		}
 		catch( NumberFormatException ex ) {
 			cooldown = 0;
 		}
 
 		pkg = new Package(pkgInfo[0], cooldown);
 
 		String[] itemInfo;
 		int quantity;
 		Material item;
 		for(int i = 1; i < tokens.length; i++) {
 			itemInfo = tokens[i].split(":");
 			if( 2 > itemInfo.length ) continue;
 
 			try {
 				quantity = Integer.parseInt(itemInfo[1]);
 			}
 			catch( NumberFormatException ex ) {
 				quantity = 1;
 			}
 
			item = Material.getMaterial(itemInfo[0]);
			if( item != null ) {
 				pkg.addItem(item, quantity);
 			}
			else {
				System.out.println("Invalid package item: "+itemInfo[0]);
 			}
 		}

		if( 0 >= pkg.getNumItems() ) {
			System.out.println("Package has no valid items: "+pkg.getName());
			return;
		}

		packages.put(pkg.getName(), pkg);
 	}
 
 	public void givePlayerPackage(Player player, String pkgName) {
 		if( !packages.containsKey(pkgName) ) {
 			player.sendMessage(ChatColor.RED + "ERROR: Unknown kit: "+pkgName);
 			return;
 		}
 
 		// Do cooldown checking here.
 
 		packages.get(pkgName).spawnAt(player.getLocation());
 	}
 
 	@Override
 	protected void beforeLoad() {
 		packages.clear();
 	}
 
 	@Override
 	protected void loadLine(String line) {
 		String[] tokens = line.split(";");
 		if( tokens.length < 2 ) return;
 
 		this.addPackage(tokens);
 	}
 
 	@Override
 	protected String saveString() {
 		String line="";
 		for( Map.Entry<String, Package> pkg: packages.entrySet() ) {
 			line += String.format("%s\r\n", pkg.getValue().toString());
 		}
 		return line;
 	}
 
 }
