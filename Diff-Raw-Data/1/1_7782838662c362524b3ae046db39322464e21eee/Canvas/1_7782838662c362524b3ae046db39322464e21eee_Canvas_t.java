 /*
  * Copyright 2010 kk-electronic a/s. 
  * 
  * This file is part of KKPortal.
  *
  * KKPortal is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * KKPortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with KKPortal.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kk_electronic.kkportal.core.ui;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.kk_electronic.kkportal.core.ModuleWindowFactory;
 import com.kk_electronic.kkportal.core.activity.Activity;
 import com.kk_electronic.kkportal.core.event.ContentChangedEvent;
 import com.kk_electronic.kkportal.core.event.NewContentEvent;
 import com.kk_electronic.kkportal.core.event.TabSelectedEvent;
 import com.kk_electronic.kkportal.core.event.TabSelectedEvent.Handler;
 import com.kk_electronic.kkportal.core.services.ModuleService.ModuleInfo;
 import com.kk_electronic.kkportal.core.tabs.ModuleInfoProvider;
 import com.kk_electronic.kkportal.core.tabs.TabInfo;
 import com.kk_electronic.kkportal.core.tabs.TabsModel;
 
 /**
  * Note this is not a good example yet. Does not have View-Presenter separation
  * 
  * @author Jes Andersen type filter text
  */
 public class Canvas implements NewContentEvent.Handler, ContentChangedEvent.Handler, Activity, Handler, GroupDisplay.Handler<ModuleWindow> {
 	private final GroupDisplay<ModuleWindow> display;
 	private final ModuleInfoProvider moduleInfoProvider;
 	private final ModuleWindowFactory windowFactory;
 	private List<List<ModuleWindow>> groupedModuleWindows;
 	private final TabsModel tabsModel;
 	private TabInfo tabInfo;
 	
 	private final static int timerStart = 700;
 	private final static int timerSchedule = 300;
 	private final static int timerIterationCount = 10;
 
 	@Inject
 	public Canvas(EventBus eventBus, GroupDisplay<ModuleWindow> display, TabsModel tabsModel,
 			ModuleInfoProvider moduleInfoProvider,
 			ModuleWindowFactory windowFactory) {
 		this.display = display;
 		this.tabsModel = tabsModel;
 		this.moduleInfoProvider = moduleInfoProvider;
 		this.windowFactory = windowFactory;
 		eventBus.addHandler(ContentChangedEvent.TYPE, this);
 		eventBus.addHandler(NewContentEvent.TYPE, this);
 		tabsModel.addTabSelectedHandler(this);
 		display.setHandler(this);
 		showTab(tabsModel.getSelectedTab());
 	}
 
 	@Override
 	public Widget asWidget() {
 		return display.asWidget();
 	}
 
 	public void showTab(final TabInfo tabInfo) {
 		if (tabInfo != null && !tabInfo.equals(this.tabInfo)) {
 			firstRun = true;
 		}
 		this.tabInfo = tabInfo;
 		if (tabInfo == null) {
 			display.setWidgets(null);
 			return;
 		}
 		HashSet<Integer> needed = new HashSet<Integer>();
 		for (List<Integer> i : tabInfo.getModuleIds()) {
 			needed.addAll(i);
 		}
 		moduleInfoProvider.translate(needed,
 				new AsyncCallback<Map<Integer, ModuleInfo>>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						GWT.log("View failed to get module infos", caught);
 					}
 
 					@Override
 					public void onSuccess(Map<Integer, ModuleInfo> result) {
 						updateDisplay(tabInfo.getModuleIds(),result);
 						if (firstRun) {
 							delayedSizeCheck.schedule(timerStart);
 							firstRun = false;
 						}
 					}
 				});
 	}
 
 	private boolean firstRun = true; // Flag for updateDisplay Timer
 
 	private void updateDisplay(List<List<Integer>> ids,
 			Map<Integer, ModuleInfo> map) {
 		groupedModuleWindows = new ArrayList<List<ModuleWindow>>();
 		for (int index=0,l=ids.size();index<l;index++) {
 			List<Integer> idcolumn = ids.get(index);
 			List<ModuleWindow> column = new ArrayList<ModuleWindow>();
 			groupedModuleWindows.add(column);
 			for (int id : idcolumn) {
 				final ModuleWindow moduleWindow = windowFactory.get(map.get(id));
 				column.add(moduleWindow);
 				moduleWindow.setFirstColumn(index==0);
 				moduleWindow.setLastColumn((index+1)==l);
 				moduleWindow.addDeleteHandler(new MouseDownHandler() {
 					
 					@Override
 					public void onMouseDown(MouseDownEvent event) {
 						delete(moduleWindow);
 						event.stopPropagation();
 					}
 				});
 			}
 		}
 		display.setWidgets(groupedModuleWindows);
 	}
 
 	protected boolean delete(ModuleWindow moduleWindow) {
 		for(List<ModuleWindow> column:groupedModuleWindows){
 			if(column.remove(moduleWindow)) {
 				tabsModel.setModuleIds(tabInfo, this.getIds(groupedModuleWindows));
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void onTabSelected(TabSelectedEvent event) {
 		showTab(event.getTabInfo());
 	}
 
 	@Override
 	public void onElementDrop(double x, int y, ModuleWindow element) {
 		GWT.log("Before move"+groupedModuleWindows.toString());
 		for(List<ModuleWindow> moduleWindows : groupedModuleWindows){
 			if(moduleWindows.remove(element)) break;
 		}
 		List<ModuleWindow> column = findColumn(x);
 		int index = findIndex(column,y);
 		column.add(index,element);
 		element.setFirstColumn(column == groupedModuleWindows.get(0));
 		GWT.log("After move"+groupedModuleWindows.toString());
 		tabsModel.setModuleIds(tabInfo,getIds(groupedModuleWindows));
 	}
 	
 	private List<List<Integer>> getIds(
 			List<List<ModuleWindow>> listlist) {
 		List<List<Integer>> retval = new ArrayList<List<Integer>>();
 		for(List<ModuleWindow> list:listlist){
 			List<Integer> ids = new ArrayList<Integer>();
 			for(ModuleWindow o:list){
 				ids.add(o.getModule().getId());
 			}
 			retval.add(ids);
 		}
 		return retval;
 	}
 
 	private int findIndex(List<? extends KnownHeight> column, int y) {
 		int index = 0;
 		for(KnownHeight x:column){
 			int h = x.getDesiredHeight();
 			if(y < h/2) break;
 			y-= h;
 			index++;
 		}
 		return index;
 	}
 
 	private List<ModuleWindow> findColumn(final double x) {
 		//TODO: Support variable width
 		double cw = 0;
 		for (List<ModuleWindow> column : groupedModuleWindows) {
 			double width = 100.0/groupedModuleWindows.size();
 			cw += width;
 			if (x < cw)
 				return column;
 		}
 		return groupedModuleWindows.get(groupedModuleWindows.size()-1);
 	}
 
 	@Override
 	public void onContentSizeChanged(ContentChangedEvent event) {
 		if (display.checkForResizes()) {
 			showTab(tabsModel.getSelectedTab());
 			saveModuleHeights();
 		}
 	}
 
 	@Override
 	public void onNewContent(NewContentEvent event) {
 		delayedSizeCheck.schedule(timerStart);
 	}
 	
 	Timer delayedSizeCheck = new Timer() {
 		private int i = 0;
 		private boolean updated = false;
 		
 		@Override
 		public void run() {
 			if (display.checkForResizes()) {
 				showTab(tabsModel.getSelectedTab());
 				updated = true;
 			}
 			if (i >= timerIterationCount) {
 				i = 0;
 				if(updated) {
 					saveModuleHeights();
					updated = false;
 				}
 			} else {
 				this.schedule(timerSchedule);
 				//GWT.log("Check Height Timer run! #" + i);
 				i++;
 			}
 			
 		}
 	};
 	
 	private void saveModuleHeights() {
 		HashSet<Integer> needed = new HashSet<Integer>();
 		for (List<Integer> i : tabsModel.getSelectedTab().getModuleIds()) {
 			needed.addAll(i);
 		}
 		moduleInfoProvider.translate(needed, new AsyncCallback<Map<Integer,ModuleInfo>>(){
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("Canvas Height failed to get module infos", caught);
 			}
 
 			@Override
 			public void onSuccess(Map<Integer, ModuleInfo> result) {
 				for (Entry<Integer, ModuleInfo> entry : result.entrySet()) {
 					tabsModel.setModuleHeight(entry.getKey(), entry.getValue().getHeight());
 				}
 			}
 		});
 	}
 }
