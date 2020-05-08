 package com.dorado.ui;
 
import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 public class WindowManager {
 	private static WindowManager instance = null;
 	
 	public static synchronized WindowManager getInstance() {
 		if (instance == null) {
 			instance = new WindowManager();
 		}
 		
 		return instance;
 	}
 	
 	private List<AppWindow> openWindows;
 	
 	protected WindowManager() {
 		openWindows = new ArrayList<AppWindow>();
 	}
 	
 	public void register(final AppWindow window) {
 		openWindows.add(window);
 		
		window.getFrame().addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				unregister(window);
 			}
 		});
 	}
 	
 	private void unregister(AppWindow window) {
 		openWindows.remove(window);
 	}
 	
 	public List<AppWindow> getWindows() {
 		return openWindows;
 	}
 }
