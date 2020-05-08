 package com.narrowtux.shortcuts;
 
 import java.util.Comparator;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.bukkitcontrib.keyboard.Keyboard;
 
 public class Shortcut {
 	private Comparator<Keyboard> keyCompare = new Comparator<Keyboard>() {
 
 		@Override
 		public int compare(Keyboard o1, Keyboard o2) {
 			if(o1.getKeyCode() < o2.getKeyCode()){
 				return -1;
 			}
 			if(o1.getKeyCode() > o2.getKeyCode()){
 				return 1;
 			}
 			return 0;
 		}
 		
 	};
 	private Set<Keyboard> keys = new TreeSet<Keyboard>(keyCompare);
 	
 	
 	public Shortcut(Set<Keyboard> currentlyPressedKeys) {
 		setKeys(currentlyPressedKeys);
 	}
 
 	public Set<Keyboard> getKeys(){
 		return keys;
 	}
 	
 	public void setKeys(Set<Keyboard> keys){
 		for(Keyboard key:keys){
 			this.keys.add(key);
 		}
 	}
 	
 	@Override
 	public String toString(){
 		String result = "";
 		for(Keyboard key:keys){
 			if(result.length()>0){
 				result+=" + ";
 			}
			result+=key.toString().replace("KEY_", "");
 		}
 		return result;
 	}
 
 	@Override
 	public int hashCode() {
 		return keys.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object other){
 		if(!(other instanceof Shortcut)){
 			return false;
 		}
 		Shortcut sh = (Shortcut) other;
 		return sh.keys.equals(keys);
 	}
 }
