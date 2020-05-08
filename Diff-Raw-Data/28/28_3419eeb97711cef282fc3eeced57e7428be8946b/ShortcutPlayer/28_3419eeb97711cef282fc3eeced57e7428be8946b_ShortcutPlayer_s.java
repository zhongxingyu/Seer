 package com.narrowtux.shortcuts;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkitcontrib.keyboard.Keyboard;
 import org.bukkitcontrib.player.ContribPlayer;
 
 public class ShortcutPlayer {
 	private static Map<String, ShortcutPlayer> instances = new HashMap<String, ShortcutPlayer>();
 	private String _name;
 	private Set<Keyboard> currentlyPressedKeys = new HashSet<Keyboard>();
 	private Set<Keyboard> keysUpLeft = new HashSet<Keyboard>();
 	private Map<Shortcut, ShortcutAction> actions = new HashMap<Shortcut, ShortcutAction>();
 	private ShortcutSetupAssistant assistant = null;
 	private boolean remove = false;
 	private boolean update = false;;
 	private ShortcutPlayer(String name){
 		_name = name;
 		instances.put(name, this);
 	}
 	
 	public static ShortcutPlayer get(String name){
 		if(!instances.containsKey(name)){
 			return new ShortcutPlayer(name);
 		} else {
 			return instances.get(name);
 		}
 	}
 	
 	public static ShortcutPlayer get(Player player){
 		return get(player.getName());
 	}
 	
 	public String getName(){
 		return _name;
 	}
 	
 	public void keyDown(Keyboard key){
 		if(key.equals(getPlayer().getChatKey()))
 		{
 			return;
 		}
 		currentlyPressedKeys.add(key);
 		keysUpLeft.add(key);
 	}
 	
 	public void keyUp(Keyboard key){
 		keysUpLeft.remove(key);
 		if(keysUpLeft.size()==0&&currentlyPressedKeys.size()>0){
 			onShortcut(new Shortcut(currentlyPressedKeys));
 			currentlyPressedKeys.clear();
 		}
 	}
 	
 	public void onShortcut(Shortcut shortcut){
 		//getPlayer().sendMessage(shortcut.toString());
 		if(remove){
 			actions.remove(shortcut);
 			remove = false;
 			ShortcutsMain.instance.save();
 			return;
 		}
 		if(update){
 			update = false;
 			ShortcutsMain.instance.save();
 			return;
 		}
 		if(assistant!=null)
 		{
 			assistant.shortcut = shortcut;
 			assistant.sendMessage(shortcut.toString()+" is the selected shortcut.");
 			return;
 		}
 		ShortcutAction action = actions.get(shortcut);
 		if(action!=null)
 		{
 			action.summon();
 		} else {
 			//getPlayer().sendMessage("No action found.");
 		}
 	}
 
 	public ContribPlayer getPlayer() {
 		return (ContribPlayer)Bukkit.getServer().getPlayer(getName());
 	}
 	
 	public void addAction(Shortcut sh, ShortcutAction act){
 		actions.put(sh, act);
 	}
 
 	public void setCurrentAssistant(ShortcutSetupAssistant shortcutSetupAssistant) {
 		assistant = shortcutSetupAssistant;
 	}
 	
 	public ShortcutSetupAssistant getCurrentAssistant(){
 		return assistant;
 	}
 
 	public void setRemove(boolean b) {
 		remove = b;
 	}
 
 	public void setUpdate(boolean b) {
 		update  = b;
 	}
 	
 	public Set<Shortcut> getRegisteredShortcuts(){
 		return actions.keySet();
 	}
 	
 	public ShortcutAction getAction(Shortcut s){
 		return actions.get(s);
 	}
 	
 	public static Collection<ShortcutPlayer> getPlayers(){
 		return instances.values();
 	}
 }
