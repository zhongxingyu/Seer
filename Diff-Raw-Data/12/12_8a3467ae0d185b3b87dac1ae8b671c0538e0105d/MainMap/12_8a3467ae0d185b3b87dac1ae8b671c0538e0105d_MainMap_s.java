 package com.aavu.client.gui;
 
 import com.aavu.client.domain.Tag;
 import com.aavu.client.domain.Topic;
 import com.aavu.client.gui.ext.FlashContainer;
 import com.aavu.client.gui.ext.MultiDivPanel;
 import com.aavu.client.gui.ext.PopupWindow;
 import com.aavu.client.service.Manager;
 import com.aavu.client.service.cache.HippoCache;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 
 public class MainMap extends Composite {
 
 	private Sidebar sideBar;	
 	private Manager manager;
 	//private TagSearch tagSearch;	
 	private Ocean ocean;
 	private StatusPanel statusPanel;
 	
 	public MainMap(Manager manager){
 		this.manager = manager;
 		manager.setMap(this);
 		
 		MultiDivPanel mainP = new MultiDivPanel();	
 
 		sideBar = new Sidebar(manager);
 		
 		//TODO cleanout remove TagSearch class, css deadwood
 		//tagSearch = new TagSearch(manager);
 		
 		ocean = new Ocean(manager);
 		
 		statusPanel = new StatusPanel();
 		
 		
 		mainP.add(new CompassRose(manager));
 		mainP.add(ocean);
 		mainP.add(sideBar);
 		mainP.add(new Dashboard(manager));
 		mainP.add(statusPanel);
 		
 		//mainP.add(tagSearch);
 		
		mainP.addStyleName("");
 		initWidget(mainP);
 	}
 		
 
 	public void growIsland(Tag tag) {
 		ocean.growIsland(tag);
 	}
 
 	public void updateSidebar() {
 		sideBar.load();
 	}
 	//TODO shouldn't need null checks, but we do.
 	public void updateStatusWindow(int id, String string, StatusCode statusCode) {
 		if(statusPanel != null){
 			statusPanel.update(id,string,statusCode);
 		}
 	}
 }
