 package com.github.tommywalsh.map;
 
 import android.view.Menu;
 import android.view.SubMenu;
 import android.view.MenuItem;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.HashMap;
 
 // Provides a submenu consisting of KML files on the sdcard
 // On selection, tells a RouteProvider to load data from the KML file
 public class KMLSubmenu {
     
     KMLSubmenu (Menu parentMenu) {
  
 	SubMenu sm = parentMenu.addSubMenu(R.string.load_data);
 
 	final String path="/sdcard/mapdata";
 	File dir = new File(path);
 	if (dir.exists() && dir.isDirectory()) {
 	    m_idToFile = new HashMap<Integer, File>();
 	    FilenameFilter filter = new FilenameFilter() {
 		    public boolean accept(File dir, String filename) {
 			return filename.matches(".+kml");
 		    }
 		};
 	    
 	    int id = 1000;
 	    for (File file : dir.listFiles(filter)) {
 		MenuItem mi = sm.add(Menu.NONE, id, Menu.NONE, file.getName());
 		m_idToFile.put(mi.getItemId(), file);
 		id++;
 	    }
 	}
     }
 
    private HashMap<Integer,File> m_idToFile;
 
 
     boolean processSelection(int itemId, RouteOverlay routeOverlay) {
 	File file = m_idToFile.get(itemId);
 	if (file != null) {
 	    routeOverlay.loadDataSet(file.getAbsolutePath());
 	    return true;
 	} else {
 	    return false;
 	}
     }
 }
