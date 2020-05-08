 package com.brosser.model; 
 
 import android.content.res.Resources;
 
 import com.brosser.database.DatabaseHandler;
 import com.brosser.model.Element;
 
 public class ElementTable {
 
 	private static ElementTable INSTANCE = null;
 	
 	private static int periods;
 	private static int groups;
 	
 	private static Element[] table;
 	private static Element activeElement;
 	private static boolean[] options;
 	
 	/** Load elements */
 	private ElementTable(Resources resources) {
 		periods = 10;
 		groups = 18;
 		table = DatabaseHandler.getElementList();
 		activeElement = table[0];
 		options = new boolean[3];
 		for(int i = 0; i < 3; i++) {
 			options[i] = true;
 		}
 	}	
 	
 	public static ElementTable getInstance(Resources resources) { 
 		if(INSTANCE != null) {
 			return INSTANCE;
 		}
 		else {
 			INSTANCE = new ElementTable(resources);
 			return INSTANCE;
 		}
 	}
 	
 	public static int selectedNumber(Element activeElement) {
 		int i = 0;
 		int elementPosition = -1;
 		for(Element e : table) {
 			if(e.equals(activeElement)) {
 				elementPosition = i;
 			}
 			i++;
 		}
 		if(elementPosition == -1) {
 			return 2;
 		}
 		return elementPosition;
 	}
 	
 	public static Element getElement(int number) {
 		return table[number];
 	}
 	
 	public static Element getElement(int group, int period) {
		if(group > 17 || group < 0 || period > 6 || period < 0) {
			return null;
 		}
 		else {
 			if(period == 0) {
 				return (group == 0 ? table[0] : table[1]);
 			}
 			else if(period == 1) {
 				return (group < 2 ? table[group+2] : table[group-8]);
 			}
 			else if(period == 2) {
 				return (group < 2 ? table[group+2+8] : table[group]);
 			}
 			else {
				return table[group + (period*18) - 36];
 			}
 		}
 	}
 	
 	public static void setOption(int i, boolean value) {
 		options[i] = value;
 	}
 	
 	public static boolean getOption(int i) {
 		return options[i];
 	}
 	
 	public static int getPeriods() {
 		return periods;
 	}
 	
 	public static int getGroups() {
 		return groups;
 	}
 	
 	public void setStarred(int group, int period, boolean starred) {
 		getElement(group, period).setStarred(starred);
 	}
 	
 	public static Element getActiveElement() {
 		return activeElement;
 	}
 	
 	public static void setActiveElement(Element element) {
 		activeElement = element;
 	}
 }
