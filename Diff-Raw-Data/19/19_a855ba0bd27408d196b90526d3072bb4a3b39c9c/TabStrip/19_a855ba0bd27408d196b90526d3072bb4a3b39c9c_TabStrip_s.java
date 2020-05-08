 /**
  * 
  */
 package de.jwic.controls;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import de.jwic.base.Control;
 import de.jwic.base.ControlContainer;
 import de.jwic.base.IControlContainer;
 import de.jwic.base.IResourceControl;
 import de.jwic.base.JWicException;
 import de.jwic.base.JavaScriptSupport;
 import de.jwic.events.SelectionEvent;
 import de.jwic.events.SelectionListener;
 
 /**
  * Represents a TabStrip control using the jQuery Tabs control. Controls added to the 
  * tabs are rendered on demand and remain in the client even if tabs are switched. When
  * a user re-opens a tab, the content is not automatically refreshed.
  *  
  * @author lippisch
  */
 @JavaScriptSupport
 public class TabStrip extends ControlContainer implements IResourceControl {
 
	private final List<SelectionListener> listeners;
 	
 	private List<Tab> tabs = new ArrayList<Tab>();
 	
 	private String activeTabName = null;
 	
 	/**
 	 * @param container
 	 */
 	public TabStrip(IControlContainer container) {
 		this(container, null);
 	}
 
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public TabStrip(IControlContainer container, String name) {
 		super(container, name);
		this.listeners = new ArrayList<SelectionListener>();
 	}
 
 	/**
 	 * A tab was activated..
 	 * @param tabName
 	 */
 	public void actionActivateTab(String tabName) {
 		
 		if (activeTabName != null && !activeTabName.equals(tabName)) {
 			setActiveTabName(tabName, false);
 		}
 	}
 	
 	/**
 	 * Returns the index of the active tab.
 	 * @return
 	 */
 	public int getActiveIndex() {
 		if (activeTabName != null) {
 			for (int i = 0; i < tabs.size(); i++) {
 				if (activeTabName.equals(tabs.get(i).getName())) {
 					return i;
 				}
 			}
 		}
 		return 0;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IResourceControl#attachResource(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	public void attachResource(HttpServletRequest req, HttpServletResponse res) throws IOException {
 
 	}
 
 	/**
 	 * Add a tab to the TabStrip.
 	 * @param title
 	 * @return
 	 */
 	public Tab addTab(String title) {
 	
 		return addTab(title, null);
 		
 	}
 
 	/**
 	 * Add a tab to the TabStrip with a custom name.
 	 * @param title
 	 * @return
 	 */
 	public Tab addTab(String title, String name) {
 	
 		Tab newTab = new Tab(this, name, title);
 		if (activeTabName == null) {
 			activeTabName = newTab.getName();
 		} 
 		return newTab;
 		
 	}
 
 	/**
 	 * @return the tabs
 	 */
 	public List<Tab> getTabs() {
 		return tabs;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.jwic.base.ControlContainer#registerControl(de.jwic.base.Control, java.lang.String)
 	 */
 	public void registerControl(Control control, String name)
 			throws JWicException {
 		if (!(control instanceof Tab)) {
 			throw new IllegalArgumentException("Only TabControls may be added to a TabStripControl.");
 		}
 		super.registerControl(control, name);
 		tabs.add((Tab) control);
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.base.ControlContainer#unregisterControl(de.jwic.base.Control)
 	 */
 	public void unregisterControl(Control control) {
 		if(control != null) {
 			tabs.remove(control);
 			if (control.getName().equals(getActiveTabName())) {
 				if (tabs.size() > 0) {
 					setActiveTabName(tabs.get(0).getName());
 				} else {
 					setActiveTabName(null);
 				}
 			}
 		}
 		super.unregisterControl(control);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.jwic.base.ControlContainer#isRenderingRelevant(de.jwic.base.Control)
 	 */
 	public boolean isRenderingRelevant(Control childControl) {
 		return childControl.getName().equals(activeTabName);
 	}
 	
 	/**
 	 * Returns the name of the active tab.
 	 * @return java.lang.String
 	 */
 	public String getActiveTabName() {
 		return activeTabName ;
 	}
 	/**
 	 * Creation date: (03.02.2003 12:26:12)
 	 * @param newActiveTabName java.lang.String
 	 */
 	public void setActiveTabName(String newActiveTabName) {
 		setActiveTabName(newActiveTabName, true);
 	}
 	
 	/**
 	 * Creation date: (03.02.2003 12:26:12)
 	 * @param newActiveTabName java.lang.String
 	 */
 	protected void setActiveTabName(String newActiveTabName, boolean executeActivateScript) {
 		for (Tab tab : tabs) {
 			if (tab.getName().equals(newActiveTabName)) {
 				activeTabName = newActiveTabName;
 				tab.requireRedraw(); // force drawing the control
 				SelectionEvent event = new SelectionEvent(tab);
				for(SelectionListener sl : this.listeners){
 					sl.objectSelected(event);
 				}
 				
 				break;
 			}
 		}
 		if(executeActivateScript)
 			getSessionContext().queueScriptCall("JWic.controls.TabStrip.activate('" + getControlID() + "', " + getActiveIndex() +");");
 	}
 
 	/**
 	 * @param listener
 	 * @return
 	 */
 	public boolean addSelectionListener(SelectionListener listener) {
 		return listeners.add(listener);
 	}
 
 	/**
 	 * @param listener
 	 * @return
 	 */
 	public boolean removeSelectionListener(SelectionListener listener) {
 		return listeners.remove(listener);
 	}
 
 }
