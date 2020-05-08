 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawnsci.plotting.views;
 
 import java.lang.ref.SoftReference;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.tool.IToolChangeListener;
 import org.dawnsci.plotting.api.tool.IToolContainer;
 import org.dawnsci.plotting.api.tool.IToolPage;
 import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.tool.ToolChangeEvent;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.eclipse.core.commands.common.EventManager;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.jface.viewers.IPostSelectionProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IURIEditorInput;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.SubActionBars;
 import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
 import org.eclipse.ui.internal.WorkbenchPlugin;
 import org.eclipse.ui.internal.util.Util;
 import org.eclipse.ui.part.IPage;
 import org.eclipse.ui.part.IPageBookViewPage;
 import org.eclipse.ui.part.IPageSite;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.ViewPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 /**
  * This view can be shown at the side of a plotting part. The
  * plotting part contributes a tool page which is shown by the view.
  * 
  * For instance fitting, derviatives etc.
  * 
  * More than one instance of this view can be declared for different tool sets.
  * Tools are differentiated from one another by calling getToolPageRole() on the 
  * tool. Know ids of instances of this view are:
  * 
  * org.dawb.workbench.plotting.views.toolPageView.1D
  * org.dawb.workbench.plotting.views.toolPageView.2D
  * org.dawb.workbench.plotting.views.toolPageView.1D_and_2D
  * 
  * 
  * @author fcp94556
  *
  */
 public class ToolPageView extends ViewPart implements IPartListener, IToolChangeListener, IToolContainer { // Important: whole part must be IToolChangeListener
 
 	/**
 	 * The pagebook control, or <code>null</code> if not initialized.
 	 */
 	private PageBook book;
 
 	/**
 	 * The page record for the default page.
 	 */
 	private PageRec defaultPageRec;
 
 	/**
 	 * Map from pages to view sites Note that view sites were not added to page
 	 * recs to avoid breaking binary compatibility with previous builds
 	 */
 	private Map<IToolPage,IPageSite> mapToolToSite = new HashMap<IToolPage,IPageSite>(7);
 
 	/**
 	 * Map from pages to the number of pageRecs actively associated with a page.
 	 */
 	private Map<IToolPage,Integer> mapToolToNumRecs = new HashMap<IToolPage,Integer>(7);
 
 	/**
 	 * The page rec which provided the current page or <code>null</code>
 	 */
 	private PageRec activeRec;
 
 	/**
 	 * If the part is hidden (usually an editor) then store it so we can
 	 * continue to track it when it becomes visible.
 	 */
 	private IWorkbenchPart hiddenPart = null;
 	
 	private static final Logger logger = LoggerFactory.getLogger(ToolPageView.class);
 
 	public static final String ID = "org.dawb.workbench.plotting.views.ToolPageView";
 	
 	private Collection<SoftReference<IToolPageSystem>>     systems;
 	private Map<String,Map<String,PageRec>> recs;
 	private String                          unique_id;
 	
 	private IToolPage staticTool;
 	
 	public ToolPageView() {
 		super();
 		this.unique_id = AbstractToolPage.getUniqueId(ToolPageView.class);
 		this.systems   = new HashSet<SoftReference<IToolPageSystem>>(7);
 		this.recs      = new HashMap<String,Map<String,PageRec>>(7);
 	}
 
 	protected IToolPage createDefaultPage(PageBook book) {
 		EmptyTool emptyTool = new EmptyTool(getViewRole()) {
 			public String toString() {
 				return "Default page";
 			}
 		};
 		emptyTool.setTitle("No tool");
 		initPage(emptyTool);
 		emptyTool.createControl(book);
 		return emptyTool;
 	}
 
 
 	/**
 	 * The action bar property listener.
 	 */
 	private IPropertyChangeListener actionBarPropListener = new IPropertyChangeListener() {
 		public void propertyChange(PropertyChangeEvent event) {
 			if (event.getProperty().equals(SubActionBars.P_ACTION_HANDLERS)
 					&& activeRec != null
 					&& event.getSource() == activeRec.subActionBars) {
 				refreshGlobalActionHandlers();
 			}
 		}
 	};
 
 	/**
 	 * Selection change listener to listen for page selection changes
 	 */
 	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
 		public void selectionChanged(SelectionChangedEvent event) {
 			pageSelectionChanged(event);
 		}
 	};
 
 	/**
 	 * Selection change listener to listen for page selection changes
 	 */
 	private ISelectionChangedListener postSelectionListener = new ISelectionChangedListener() {
 		public void selectionChanged(SelectionChangedEvent event) {
 			postSelectionChanged(event);
 		}
 	};
 
 	/**
 	 * Selection provider for this view's site
 	 */
 	private SelectionProvider selectionProvider = new SelectionProvider();
 
 	/**
 	 * A data structure used to store the information about a single page within
 	 * a pagebook view.
 	 */
 	protected static class PageRec {
 
 		boolean isDisposed = false;
 		/**
 		 * The part.
 		 */
 		public IWorkbenchPart part;
 
 		/**
 		 * The page.
 		 */
 		public IToolPage tool;
 
 		/**
 		 * The page's action bars
 		 */
 		public SubActionBars subActionBars;
 
 		/**
 		 * Creates a new page record initialized to the given part and page.
 		 * 
 		 * @param part
 		 * @param page
 		 */
 		public PageRec(IWorkbenchPart part, IToolPage page) {
 			this.part = part;
 			this.tool = page;
 		}
 
 		/**
 		 * Disposes of this page record by <code>null</code>ing its fields.
 		 */
 		public void dispose() {
 			part = null;
 			tool = null;
 			isDisposed= true;
 		}
 		
 	    public String toString() {
 	    	if (tool!=null) return tool.toString();
 	    	return super.toString();
 	    }
 
 		public boolean isDisposed() {
 			return isDisposed || (tool!=null && tool.isDisposed());
 		}
 
 	}
 
 	private static class SelectionManager extends EventManager {
 		/**
 		 * 
 		 * @param listener
 		 *            listen
 		 */
 		public void addSelectionChangedListener(
 				ISelectionChangedListener listener) {
 			addListenerObject(listener);
 		}
 
 		/**
 		 * 
 		 * @param listener
 		 *            listen
 		 */
 		public void removeSelectionChangedListener(
 				ISelectionChangedListener listener) {
 			removeListenerObject(listener);
 		}
 
 		/**
 		 * 
 		 * @param event
 		 *            the event
 		 */
 		public void selectionChanged(final SelectionChangedEvent event) {
 			// pass on the notification to listeners
 			Object[] listeners = getListeners();
 			for (int i = 0; i < listeners.length; ++i) {
 				final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
 				Platform.run(new SafeRunnable() {
 					public void run() {
 						l.selectionChanged(event);
 					}
 				});
 			}
 		}
 
 	}
 
 	/**
 	 * A selection provider/listener for this view. It is a selection provider
 	 * for this view's site.
 	 */
 	protected class SelectionProvider implements IPostSelectionProvider {
 
 		private SelectionManager fSelectionListener = new SelectionManager();
 
 		private SelectionManager fPostSelectionListeners = new SelectionManager();
 
 		/*
 		 * (non-Javadoc) Method declared on ISelectionProvider.
 		 */
 		public void addSelectionChangedListener(
 				ISelectionChangedListener listener) {
 			fSelectionListener.addSelectionChangedListener(listener);
 		}
 
 		/*
 		 * (non-Javadoc) Method declared on ISelectionProvider.
 		 */
 		public ISelection getSelection() {
 			// get the selection provider from the current page
 			IPage currentPage = getCurrentPage();
 			// during workbench startup we may be in a state when
 			// there is no current page
 			if (currentPage == null) {
 				return StructuredSelection.EMPTY;
 			}
 			IPageSite site = getPageSite(currentPage);
 			if (site == null) {
 				return StructuredSelection.EMPTY;
 			}
 			ISelectionProvider selProvider = site.getSelectionProvider();
 			if (selProvider != null) {
 				return selProvider.getSelection();
 			}
 			return StructuredSelection.EMPTY;
 		}
 
 		/*
 		 * (non-Javadoc) Method declared on ISelectionProvider.
 		 */
 		public void removeSelectionChangedListener(
 				ISelectionChangedListener listener) {
 			fSelectionListener.removeSelectionChangedListener(listener);
 		}
 
 		/**
 		 * The selection has changed. Process the event, notifying selection
 		 * listeners and post selection listeners.
 		 * 
 		 * @param event
 		 *            the change
 		 */
 		public void selectionChanged(final SelectionChangedEvent event) {
 			fSelectionListener.selectionChanged(event);
 		}
 
 		/**
 		 * The selection has changed, so notify any post-selection listeners.
 		 * 
 		 * @param event
 		 *            the change
 		 */
 		public void postSelectionChanged(final SelectionChangedEvent event) {
 			fPostSelectionListeners.selectionChanged(event);			
 		}
 
 		/*
 		 * (non-Javadoc) Method declared on ISelectionProvider.
 		 */
 		public void setSelection(ISelection selection) {
 			// get the selection provider from the current page
 			IPage currentPage = getCurrentPage();
 			// during workbench startup we may be in a state when
 			// there is no current page
 			if (currentPage == null) {
 				return;
 			}
 			IPageSite site = getPageSite(currentPage);
 			if (site == null) {
 				return;
 			}
 			ISelectionProvider selProvider = site.getSelectionProvider();
 			// and set its selection
 			if (selProvider != null) {
 				selProvider.setSelection(selection);
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jface.viewers.IPostSelectionProvider#addPostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 		 */
 		public void addPostSelectionChangedListener(
 				ISelectionChangedListener listener) {
 			fPostSelectionListeners.addSelectionChangedListener(listener);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jface.viewers.IPostSelectionProvider#removePostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 		 */
 		public void removePostSelectionChangedListener(
 				ISelectionChangedListener listener) {
 			fPostSelectionListeners.removeSelectionChangedListener(listener);
 		}
 	}
 
 	/**
 	 * Creates a page for a given part. Adds it to the pagebook but does not
 	 * show it.
 	 * 
 	 * @param part
 	 *            The part we are making a page for.
 	 * @param tool
 	 *            May be null
 	 * @return IWorkbenchPart
 	 */
 	private PageRec createPage(IWorkbenchPart part, IToolPage tool) {
 
 		try {
 			PageRec rec = doCreatePage(part, tool);
 			if (rec != null) {
 				preparePage(rec);
 			}
 			return rec;
 		} catch (Exception ne) {
 			logger.error("Error creating tool page "+tool, ne);
 			return null;
 		}
 	}
 
 	/**
 	 * Prepares the page in the given page rec for use in this view.
 	 * 
 	 * @param rec
 	 */
 	private void preparePage(PageRec rec) {
 		IPageSite site = null;
 		Integer count;
 
 		if (!mapToolToNumRecs.containsKey(rec.tool)) {
 			if (rec.tool instanceof IPageBookViewPage) {
 				site = ((IPageBookViewPage) rec.tool).getSite();
 			}
 			if (site == null) {
 				// We will create a site for our use
 				site = new ToolPageSite(getViewSite());
 			}
 			mapToolToSite.put(rec.tool, site);
 
 			rec.subActionBars = (SubActionBars) site.getActionBars();
 			rec.subActionBars.addPropertyChangeListener(actionBarPropListener);
 			// for backward compability with IPage
 			rec.tool.setActionBars(rec.subActionBars);
 
 			count = new Integer(0);
 		} else {
 			site = (IPageSite) mapToolToSite.get(rec.tool);
 			rec.subActionBars = (SubActionBars) site.getActionBars();
 			count = ((Integer) mapToolToNumRecs.get(rec.tool));
 		}
 
 		mapToolToNumRecs.put(rec.tool, new Integer(count.intValue() + 1));
 	}
 
 	/**
 	 * Initializes the given page with a page site.
 	 * <p>
 	 * Subclasses should call this method after the page is created but before
 	 * creating its controls.
 	 * </p>
 	 * <p>
 	 * Subclasses may override
 	 * </p>
 	 * 
 	 * @param page
 	 *            The page to initialize
 	 */
 	protected void initPage(IToolPage page) {
 		try {
 			page.init(new ToolPageSite(getViewSite()));
 			page.setViewPart(this);
 		} catch (PartInitException e) {
 			WorkbenchPlugin.log(getClass(), "initPage", e); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * The <code>PageBookView</code> implementation of this
 	 * <code>IWorkbenchPart</code> method creates a <code>PageBook</code>
 	 * control with its default page showing. Subclasses may extend.
 	 */
 	public void createPartControl(Composite parent) {
 
 		// Create the page book.
 		book = new PageBook(parent, SWT.NONE);
 
 		// Create the default page rec.
 		IToolPage defaultPage = createDefaultPage(book);
 		defaultPageRec = new PageRec(null, defaultPage);
 		preparePage(defaultPageRec);
 
 		// Show the default page
 		showPageRec(defaultPageRec);
 
 		// Listen to part activation events.
 		getSite().getPage().addPartListener(partListener);
 		showBootstrapPart();
 	}
 
 
 	/**
 	 * The <code>PageBookView</code> implementation of this
 	 * <code>IAdaptable</code> method delegates to the current page, if it
 	 * implements <code>IAdaptable</code>.
 	 */
 	@SuppressWarnings("rawtypes")
 	public Object getAdapter(Class key) {
 		// delegate to the current page, if supported
 		IPage page = getCurrentPage();
 		Object adapter = Util.getAdapter(page, key);
 		if (adapter != null) {
 			return adapter;
 		}
 		// if the page did not find the adapter, look for one provided by
 		// this view before delegating to super.
 		adapter = getViewAdapter(key);
 		if (adapter != null) {
 			return adapter;
 		}
 		// delegate to super
 		return super.getAdapter(key);
 	}
 
 	/**
 	 * Returns an adapter of the specified type, as provided by this view (not
 	 * the current page), or <code>null</code> if this view does not provide
 	 * an adapter of the specified adapter.
 	 * <p>
 	 * The default implementation returns <code>null</code>. Subclasses may
 	 * override.
 	 * </p>
 	 * 
 	 * @param adapter
 	 *            the adapter class to look up
 	 * @return a object castable to the given class, or <code>null</code> if
 	 *         this object does not have an adapter for the given class
 	 * @since 3.2
 	 */
 	protected Object getViewAdapter(Class adapter) {
 		return null;
 	}
 
 
 
 	/**
 	 * Returns the part which contributed the current page to this view.
 	 * 
 	 * @return the part which contributed the current page or <code>null</code>
 	 *         if no part contributed the current page
 	 */
 	protected IWorkbenchPart getCurrentContributingPart() {
 		if (activeRec == null) {
 			return null;
 		}
 		return activeRec.part;
 	}
 
 	/**
 	 * Returns the currently visible page for this view or <code>null</code>
 	 * if no page is currently visible.
 	 * 
 	 * @return the currently visible page
 	 */
 	public IPage getCurrentPage() {
 		if (activeRec == null) {
 			return null;
 		}
 		return activeRec.tool;
 	}
 
 	/**
 	 * Returns the view site for the given page of this view.
 	 * 
 	 * @param page
 	 *            the page
 	 * @return the corresponding site, or <code>null</code> if not found
 	 */
 	protected ToolPageSite getPageSite(IPage page) {
 		return (ToolPageSite) mapToolToSite.get(page);
 	}
 
 	/**
 	 * Returns the default page for this view.
 	 * 
 	 * @return the default page
 	 */
 	public IPage getDefaultPage() {
 		return defaultPageRec.tool;
 	}
 
 	/**
 	 * Returns the pagebook control for this view.
 	 * 
 	 * @return the pagebook control, or <code>null</code> if not initialized
 	 */
 	protected PageBook getPageBook() {
 		return book;
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IViewPart.
 	 */
 	public void init(IViewSite site) throws PartInitException {
 		site.setSelectionProvider(selectionProvider);
 		super.init(site);
 	}
 
 	public String getPartName() {
 		if (getViewSite()!=null && getViewSite().getSecondaryId()!=null) {
 			return getTitleForToolPage(getViewSite().getSecondaryId());
 		}
 		return super.getPartName();
 	}
 
 	public Image getTitleImage() {
 		if (getViewSite()!=null && getViewSite().getSecondaryId()!=null) {
 			Image image = getImageForToolPage(getViewSite().getSecondaryId());
 			if (image.isDisposed()) {
 				return null;
 			}
 			return image;
 		}
 		Image image = super.getTitleImage();
 		if (image.isDisposed()) {
 			return null;
 		}
 		return image;
 	}
 
 	private String toolTitleOverride = null;
 	private String getTitleForToolPage(String toolId) {
 		
 		if (toolTitleOverride!=null) return toolTitleOverride;
 		final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.plotting.api.toolPage");
 	    for (IConfigurationElement e : configs) {
 			
 	    	if (!e.getName().equals("plotting_tool_page")) continue;
 	    	if (!toolId.equals(e.getAttribute("id"))) continue;
 	    	toolTitleOverride = e.getAttribute("label");
 	    	return toolTitleOverride;
 	    }
 	    toolTitleOverride = super.getPartName();
 	    return toolTitleOverride;
 	}
 
 	private Image toolImageOverride = null;
 	private Image getImageForToolPage(String toolId) {
 		
 		try {
 		    if (toolImageOverride!=null&&toolImageOverride.isDisposed()) toolImageOverride = null;
 		} catch (Throwable ne) {
 			toolImageOverride = null;
 		}
 		
 		if (toolImageOverride!=null) return toolImageOverride;
 		final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.plotting.api.toolPage");
 	    for (IConfigurationElement e : configs) {
 			
 	    	if (!e.getName().equals("plotting_tool_page")) continue;
 	    	if (!toolId.equals(e.getAttribute("id"))) continue;
 	    	final String icon =  e.getAttribute("icon");
 	    	try {
 	    		toolImageOverride = Activator.imageDescriptorFromPlugin(e.getContributor().getName(), icon).createImage();
 	    		return toolImageOverride;
 	    	} catch (Throwable be) {
 	    		toolImageOverride = null;
 	    		return null;
 	    	}
 	    }
 	    toolImageOverride = super.getTitleImage();
 	    return toolImageOverride;
 	}
 	/**
 	 * The <code>PageBookView</code> implementation of this
 	 * <code>IPartListener</code> method does nothing. Subclasses may extend.
 	 */
 	public void partBroughtToTop(IWorkbenchPart part) {
 		// Do nothing by default
 	}
 
 	/**
 	 * The <code>PageBookView</code> implementation of this
 	 * <code>IPartListener</code> method deal with the closing of the active
 	 * part. Subclasses may extend.
 	 */
 	public void partClosed(IWorkbenchPart part) {
 		
 		// Update the active part.
 		if (activeRec != null && activeRec.part == part) {
 			showPageRec(defaultPageRec);
 			activeRec = defaultPageRec;
 		}
 
 		// Find and remove the part page.
 		removeTools(getString(part) , true);
 	
 		if (part == hiddenPart) {
 			hiddenPart = null;
 		}
 	}
 
 	/**
 	 * The <code>PageBookView</code> implementation of this
 	 * <code>IPartListener</code> method does nothing. Subclasses may extend.
 	 */
 	public void partDeactivated(IWorkbenchPart part) {
 		// Do nothing.
 	}
 
 	private boolean newPartFound = false;
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
 	 */
 	public void partOpened(IWorkbenchPart part) {
         newPartFound = true;
 	}
 
 	/**
 	 * Refreshes the global actions for the active page.
 	 */
 	@SuppressWarnings("rawtypes")
 	private void refreshGlobalActionHandlers() {
 		// Clear old actions.
 		IActionBars bars = getViewSite().getActionBars();
 		bars.clearGlobalActionHandlers();
 
 		// Set new actions.
 		Map<?, ?> newActionHandlers = activeRec.subActionBars
 				.getGlobalActionHandlers();
 		if (newActionHandlers != null) {
 			Set<?> keys = newActionHandlers.entrySet();
 			Iterator<?> iter = keys.iterator();
 			while (iter.hasNext()) {
 				Map.Entry entry = (Map.Entry) iter.next();
 				bars.setGlobalActionHandler((String) entry.getKey(),
 						(IAction) entry.getValue());
 			}
 		}
 	}
 
 	/**
 	 * Removes all tools for a page
 	 * 
 	 * @param rec
 	 */
 	private void removeTools(String partLoc, boolean removeFromMap) {
 
 		
 		if (staticTool!=null) {
 			staticTool.deactivate();
 			return;
 		}
 		
 		final Map<String,PageRec> pageRecs = removeFromMap ? recs.remove(partLoc) : recs.get(partLoc);
 		
 		if (pageRecs==null) return;
 		
 		for (String title : pageRecs.keySet()) {
 			
 			final PageRec rec = pageRecs.get(title);
 			if (rec.isDisposed()) {
 				continue;
 			}
 			
 			if (rec == defaultPageRec) continue;
 			if (rec == activeRec)      {
 				activeRec.tool.deactivate();
 				continue;
 			}
 			
 			int newCount = ((Integer) mapToolToNumRecs.get(rec.tool)).intValue() - 1;
 	
 			if (newCount == 0) {
 				mapToolToNumRecs.remove(rec.tool);
 	
 				Control control = rec.tool.getControl();
 				if (control != null && !control.isDisposed()) {
 					// Dispose the page's control so pages don't have to do this in
 					// their
 					// dispose method.
 					// The page's control is a child of this view's control so if
 					// this view
 					// is closed, the page's control will already be disposed.
 					control.dispose();
 				}
 	
 				// free the page
 				IToolPageSystem toolSystem = (IToolPageSystem)rec.tool.getPlottingSystem();
 				toolSystem.disposeToolPage(rec.tool.getToolId());
 	
 				if (rec.subActionBars != null) {
 					rec.subActionBars.dispose();
 				}
 	
 				Object site = mapToolToSite.remove(rec.tool);
 				if (site instanceof ToolPageSite) {
 					((ToolPageSite) site).dispose(); 
 				}
 			} else {
 				mapToolToNumRecs.put(rec.tool, new Integer(newCount));
 			}
 		}
 		pageRecs.clear();
 		
 	}
 
 	/*
 	 * (non-Javadoc) Method declared on IWorkbenchPart.
 	 */
 	public void setFocus() {
 		// first set focus on the page book, in case the page
 		// doesn't properly handle setFocus
 		if (book != null) {
 			book.setFocus();
 		}
 		// then set focus on the page, if any
 		if (activeRec != null) {
 			activeRec.tool.setFocus();
 		}
 	}
 
 	/**
 	 * Handle page selection changes.
 	 * 
 	 * @param event
 	 */
 	private void pageSelectionChanged(SelectionChangedEvent event) {
 		// forward this change from a page to our site's selection provider
 		SelectionProvider provider = (SelectionProvider) getSite()
 				.getSelectionProvider();
 		if (provider != null) {
 			provider.selectionChanged(event);
 		}
 	}
 
 	/**
 	 * Handle page selection changes.
 	 * 
 	 * @param event
 	 */
 	private void postSelectionChanged(SelectionChangedEvent event) {
 		// forward this change from a page to our site's selection provider
 		SelectionProvider provider = (SelectionProvider) getSite()
 				.getSelectionProvider();
 		if (provider != null) {
 			provider.postSelectionChanged(event);
 		}
 	}
 
 	/**
 	 * Shows a page for the active workbench part.
 	 */
 	private void showBootstrapPart() {
 		IWorkbenchPart part = getBootstrapPart();
 		if (part != null) {
 			partActivated(part);
 		}
 	}
 
 	/**
 	 * Shows page contained in the given page record in this view. The page
 	 * record must be one from this pagebook view.
 	 * <p>
 	 * The <code>PageBookView</code> implementation of this method asks the
 	 * pagebook control to show the given page's control, and records that the
 	 * given page is now current. Subclasses may extend.
 	 * </p>
 	 * 
 	 * @param pageRec
 	 *            the page record containing the page to show
 	 */
 	protected void showPageRec(PageRec pageRec) {
 		// If already showing do nothing
 		if (activeRec == pageRec) {
 			if (!activeRec.tool.isActive()) activeRec.tool.activate();
 			return;
 		}
 		if (staticTool!=null && activeRec!=null && activeRec.tool==staticTool) { // We never show owt else.
 			return;
 		}
 		// If the page is the same, just set activeRec to pageRec
 		if (activeRec != null && pageRec != null
 				&& activeRec.tool == pageRec.tool) {
 			activeRec = pageRec;
 			return;
 		}
 
 		// Hide old page.
 		if (activeRec != null) {
 			
 			activeRec.tool.deactivate();
 			
 			ToolPageSite pageSite = (ToolPageSite) mapToolToSite.get(activeRec.tool);
 	
 			activeRec.subActionBars.deactivate();
 
 			pageSite.deactivate();
 
 			// remove our selection listener
 			ISelectionProvider provider = pageSite.getSelectionProvider();
 			if (provider != null) {
 				provider
 				.removeSelectionChangedListener(selectionChangedListener);
 				if (provider instanceof IPostSelectionProvider) {
 					((IPostSelectionProvider) provider)
 					.removePostSelectionChangedListener(postSelectionListener);
 				} else {
 					provider.removeSelectionChangedListener(postSelectionListener);
 				}
 			}
 			
 		}
 
 		// Show new page.
 		activeRec = pageRec;
 		Control pageControl = activeRec!=null && activeRec.tool!=null
 				            ? activeRec.tool.getControl()
 				            : null;
 		if (pageControl != null && !pageControl.isDisposed()) {
 			
 			activeRec.tool.activate();
 
 			ToolPageSite pageSite = (ToolPageSite) mapToolToSite.get(activeRec.tool);
 
 			// Verify that the page control is not disposed
 			// If we are closing, it may have already been disposed
 			book.showPage(pageControl);
 			activeRec.subActionBars.activate();
 			refreshGlobalActionHandlers();
 
 			// activate the nested services
 			pageSite.activate();
 
 			// add our selection listener
 			ISelectionProvider provider = pageSite.getSelectionProvider();
 			if (provider != null) {
 				provider.addSelectionChangedListener(selectionChangedListener);
 				if (provider instanceof IPostSelectionProvider) {
 					((IPostSelectionProvider) provider)
 							.addPostSelectionChangedListener(postSelectionListener);
 				} else {
 					provider.addSelectionChangedListener(postSelectionListener);
 				}
 			}
 			// Update action bars.
 			getViewSite().getActionBars().updateActionBars();
 		}
 	}
 
 	/**
 	 * Returns the selectionProvider for this page book view.
 	 * 
 	 * @return a SelectionProvider
 	 */
 	protected SelectionProvider getSelectionProvider() {
 		return selectionProvider;
 	}
 	
 	private IPartListener2 partListener = new IPartListener2() {
 		public void partActivated(IWorkbenchPartReference partRef) {
 			IWorkbenchPart part = partRef.getPart(false);
 			ToolPageView.this.partActivated(part);
 		}
 
 		public void partBroughtToTop(IWorkbenchPartReference partRef) {
 			ToolPageView.this.partBroughtToTop(partRef.getPart(false));
 		}
 
 		public void partClosed(IWorkbenchPartReference partRef) {
 			ToolPageView.this.partClosed(partRef.getPart(false));
 		}
 
 		public void partDeactivated(IWorkbenchPartReference partRef) {
 			ToolPageView.this.partDeactivated(partRef.getPart(false));
 		}
 
 		public void partHidden(IWorkbenchPartReference partRef) {
 			ToolPageView.this.partHidden(partRef.getPart(false));
 		}
 
 		public void partInputChanged(IWorkbenchPartReference partRef) {
 		}
 
 		public void partOpened(IWorkbenchPartReference partRef) {
 			ToolPageView.this.partOpened(partRef.getPart(false));
 		}
 
 		public void partVisible(IWorkbenchPartReference partRef) {
 			ToolPageView.this.partVisible(partRef.getPart(false));
 		}
 	};
 
 	/**
 	 * Make sure that the part is not considered if it is hidden.
 	 * @param part
 	 * @since 3.5
 	 */
 	protected void partHidden(IWorkbenchPart part) {
 		if (part == null || part != getCurrentContributingPart()) {
 			return;
 		}
		
		if (activeRec!=null && activeRec.tool != null && part.getAdapter(IPlottingSystem.class) == activeRec.tool.getPlottingSystem()) {
			return;
		}

 		// if we've minimized the editor stack, that's no reason to
 		// drop our content
 		if (getSite().getPage().getPartState(
 				getSite().getPage().getReference(part)) == IWorkbenchPage.STATE_MINIMIZED) {
 			return;
 		}
 		// if we're switching from a part source in our own stack,
 		// we also don't want to clear our content.
 		if (part instanceof IViewPart) {
 			final IViewPart[] viewStack = getSite().getPage()
 					.getViewStack(this);
 			if (containsPart(viewStack, part)) {
 				return;
 			}
 		}
 		hiddenPart = part;
 		showPageRec(defaultPageRec);
 	}
 
 	/**
 	 * @param viewStack
 	 * @param part
 	 * @return <code>true</code> if the part is in the viewStack
 	 */
 	private boolean containsPart(IViewPart[] viewStack, IWorkbenchPart part) {
 		if (viewStack == null) {
 			return false;
 		}
 		for (int i = 0; i < viewStack.length; i++) {
 			if (viewStack[i] == part) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Make sure that the part is not considered if it is hidden.
 	 * 
 	 * @param part
 	 * @since 3.5
 	 */
 	protected void partVisible(IWorkbenchPart part) {
 		if (part == null || part != hiddenPart) {
 			return;
 		}
 		partActivated(part);
 	}
 	
 	protected synchronized PageRec doCreatePage(IWorkbenchPart part, IToolPage tool) {
 				
 		// If static tool is not null we can only show that tool on this page.		
 		final IToolPageSystem sys = (IToolPageSystem)part.getAdapter(IToolPageSystem.class);
         if (systems==null||recs==null) return null;
         
 		if (sys!=null) {
 			
 			if (getViewSite().getSecondaryId()==null) {
 				systems.add(new SoftReference<IToolPageSystem>(sys));
 				sys.addToolChangeListener(this);
 			} else {
 				try {
 					tool = sys.createToolPage(getViewSite().getSecondaryId());
 				} catch (Exception e) {
 					logger.error("Cannot clone tool "+getViewSite().getSecondaryId(), e);
 					return null;
 				}
 			}
 
 			if (tool==null || tool.isDisposed()) { // We find the tool and check it is not a repeat.
 				tool = sys.getCurrentToolPage(getViewRole());	
 				if (!isToolAllowed(tool)) return null;
 			
 		        final PageRec existing = getPageRec(part);
 		        
 		        if (tool!=null && existing!=null&&existing.tool!=null && existing.tool.equals(tool)) {
 		        	if (!tool.isActive()) tool.activate();
 		        	return existing;
 		        }
 
 		    }
 			
 			if (tool == null)         return null;
 			if (!isToolAllowed(tool)) return null;
 			
 			updatePartInfo(tool);
 			initPage(tool);
 			tool.createControl(getPageBook());
 			createCommonActions(tool);
 			if (!tool.isActive()) tool.activate();
 			
 			if (tool.isStaticTool() && getViewSite().getSecondaryId()!=null) {
 				staticTool = tool;
 			}
 						
 			PageRec rec = new PageRec(part, tool);
 			recordPage(part, tool, rec);
             return rec;
 		}
 		
 		return null;
 	}
 	
 	private void createCommonActions(final IToolPage tool) {
 		
 		if (tool.getSite()==null)                 return;
 		if (tool.getSite().getActionBars()==null) return;
 		if (tool.getSite().getActionBars().getMenuManager()==null) return;
 		
 		if (tool.getCheatSheetId()!=null) {
 			final Action cheatAction = new Action("Open cheat sheet for '"+tool.getTitle()+"'") {
 				public void run() {
 					final String id = tool.getCheatSheetId();
 					final OpenCheatSheetAction oa = new OpenCheatSheetAction(id);
 					oa.run();
 				}
 			};
 			cheatAction.setImageDescriptor(Activator.getImageDescriptor("icons/help_view.gif"));
 			tool.getSite().getActionBars().getMenuManager().add(cheatAction);
 			tool.getSite().getActionBars().getMenuManager().add(new Separator());
 		}
 
 		final Action cloneAction = new Action("Open '"+tool.getTitle()+"' in dedicated view") {
 			public void run() {
 				try {
 					createToolInDedicatedView(tool);
 				} catch (Exception e) {
 					logger.error("Cannot open tool on its own page!", e);
 				}
 			}
 		};
 		
 		tool.getSite().getActionBars().getMenuManager().add(cloneAction);
 		
 	}
 	
 	@Override
 	public IToolPage createToolInDedicatedView(IToolPage tool) throws Exception {
 		
 		IToolPage orig = null;
 		if (activeRec!=null) {
 			orig = activeRec.tool; 
 			if (recs.get(getString(tool.getPart()))!=null) {
 				recs.get(getString(tool.getPart())).remove(tool.getToolId());
 			}
 			if (mapToolToNumRecs.get(activeRec.tool)!=null) {
 				mapToolToNumRecs.put(activeRec.tool, mapToolToNumRecs.get(activeRec.tool).intValue()-1);
 			}
 			if (activeRec.subActionBars!=null) {
 				activeRec.subActionBars.deactivate();
 			}
 			activeRec = null;
 		}
 		updatePartInfo(defaultPageRec.tool);
 		showPageRec(defaultPageRec);
 		
 		final ToolPageView view = (ToolPageView)getPage().showView("org.dawb.workbench.plotting.views.toolPageView.fixed",
 																				tool.getToolId(),
 																				IWorkbenchPage.VIEW_ACTIVATE);
 		view.update(orig);
 		if (orig!=null && view.activeRec!=null && view.activeRec.tool!=null) {
 			try {
 			    view.activeRec.tool.sync(orig);
 			} catch (Throwable ne) {
 				logger.error("Unable to sync "+view.activeRec.tool.getToolId());
 			}
 			
 			if (orig.getPart()!=null) {
 				final IToolPage finalOrig = orig;
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						try {
 						    getActivePage().activate(finalOrig.getPart());
 						} catch (Throwable ne) {
 							logger.error("Cannot activate part "+finalOrig, ne);
 						}
 					}
 				});
 			}
 			
 			if (orig.isActive()) orig.deactivate();
 			
 			if (view.activeRec.tool.isStaticTool()) {
 				view.staticTool = view.activeRec.tool;
 			}
 		}
 
 		return view.staticTool;
 	}
 	
 	private static IWorkbenchPage getActivePage() {
 		final IWorkbench bench = PlatformUI.getWorkbench();
 		if (bench==null) return null;
 		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
 		if (window==null) return null;
 		return window.getActivePage();
 	}
 	private static IWorkbenchPage getPage() {
 		IWorkbenchPage activePage = getActivePage();
 		if (activePage!=null) return activePage;
 		return getDefaultWorkbenchPage();
 	}
 	private static IWorkbenchPage getDefaultWorkbenchPage() {
 		final IWorkbench bench = PlatformUI.getWorkbench();
 		if (bench==null) return null;
 		final IWorkbenchWindow[] windows = bench.getWorkbenchWindows();
 		if (windows==null) return null;
 		
 		return windows[0].getActivePage();
 	}
 
 	
 	protected void update(IToolPage orig) {
 		AbstractToolPage tp = (AbstractToolPage)orig;
 		if (tp!=null && tp.isLinkedToolPage()) {
 			final IToolPage link = tp.getLinkedToolPage();
 			partActivated(link.getViewPart());
 			
 		} else {
 			final IWorkbenchPage page = getPage();
 		    partActivated(page.getActiveEditor());
 		}
 	}
 
 	private ToolPageRole getViewRole() {
 		if (getSite().getId().endsWith(".1D")) return ToolPageRole.ROLE_1D;
 		if (getSite().getId().endsWith(".2D")) return ToolPageRole.ROLE_2D;
 		if (getSite().getId().endsWith(".3D")) return ToolPageRole.ROLE_3D;
 		return ToolPageRole.ROLE_1D_AND_2D;
 	}
 
 	private void recordPage(IWorkbenchPart part, IToolPage tool, PageRec rec) {
 		final String path = getString(part);
 		Map<String,PageRec> pages = recs.get(path);
 		if (pages==null) {
 			pages = new HashMap<String, PageRec>(3);
 			recs.put(path, pages);
 		}
 		pages.put(tool.getToolId(), rec);
 	}
 		
 	private String getString(IWorkbenchPart part) {
 		
 		if (part         ==null)  return null;
 		if (part.getSite()==null) return null;
 		
 		if (part instanceof IEditorPart) {
 			final IEditorInput input = ((IEditorPart)part).getEditorInput();
 			if (input==null) return null;
 			return input instanceof IURIEditorInput 
 				   ? ((IURIEditorInput)input).getURI().getRawPath()
 				   : input.getName(); // TODO Not very secure
 		} else {
 		    // Use the id of the part
 			return part.getSite().getId();
 		}
 	}
 	
 	private boolean updatingActivated = false;
 	public void toolChanged(ToolChangeEvent evt) {
 		
 		if (updatingActivated)                    return;
 		if (getViewSite().getSecondaryId()!=null) return;
 		if (staticTool!=null)                     return; // Cannot change tool.
 		
 		// If there is a dedicated view for this tool then we do not accept the change.
 		if (!isToolAllowed(evt.getNewPage())) return;
 		
 		try {
 			IToolPage tool = evt.getNewPage();
 			if (tool==null || getViewRole()!=tool.getToolPageRole()) return;
 			
 			PageRec rec = getPageRec(tool);
 			if (rec == null || tool.getControl()==null || rec.tool!=tool) {
 				rec = createPage(tool.getPart(), tool);
 			}
 		
 			showPageRec(rec);
 			updatePartInfo(rec.tool);
 			
 		} catch (Exception ne) {
 			logger.error("Unexpected and serious problem trying to switch tool to "+evt.getNewPage(), ne);
 		}
 	}
 	
 	private boolean isToolAllowed(IToolPage newPage) {
 		
 		String toolId  = newPage!=null ? newPage.getToolId() : null;
 		if (toolId!=null) {
 			if (toolId.equals(getViewSite().getSecondaryId())) return true;
 		    if (getPage().findViewReference("org.dawb.workbench.plotting.views.toolPageView.fixed", toolId)!=null) {
 		    	return false;
 		    }
 		}
 		return true;
 	}
 
 	public void partActivated(IWorkbenchPart part) {
 
 		if (!isImportant(part)) return;
 
 		if (updatingActivated) return;
         try {
             updatingActivated = true;
                        
     		if (synchronizeStaticTool(part)) {
     			return;
     		}
     		
         	IToolPageSystem sys  = (IToolPageSystem)part.getAdapter(IToolPageSystem.class);
       		if (!sys.isActive(part)) return;
       			
         	final IToolPage tool = getViewSite().getSecondaryId()==null
 			             ? sys.getCurrentToolPage(getViewRole())
 			             : sys.getToolPage(getViewSite().getSecondaryId());
         	
  					            
             if (tool!=null&&tool.equals(getCurrentPage())) {
             	return;
             }
             
             /** Bodge warning. We now check a few incompatible tool combinations **/
             // For null tools with different dimensionality to plotting system, we ignore
            
             // If we are a IToolPageSystem and required tool is no tool and 
             // if we have plotted data inconsistent with the existing tool, 
             // leave the existing tool where it is. Likely it is an image tool
             // and sys is a 1D plot in a dedicated view.
             if (!newPartFound && sys instanceof IPlottingSystem && (tool instanceof EmptyTool || tool==null) ) {
             	final Collection<ITrace> images = ((IPlottingSystem)sys).getTraces(IImageTrace.class);   
                 
             	ToolPageRole role = (activeRec!=null && activeRec.tool!=null) 
             			          ? activeRec.tool.getToolPageRole()
             			          : getViewRole();
              	if (images==null|| images.isEmpty()) { // 1D in this part
             		if (role==ToolPageRole.ROLE_2D) return; // 2D original tool
             	} else { // 2D in this part
             		if (role!=ToolPageRole.ROLE_2D) return; // 1D original tool
             	}
             }
             
             /**
              * For dedicated tools we do not want to link them to parts that are tools and also
              * have the wrong dimensionality
              */
             if (!isDimensionalityOk(activeRec.tool, sys)) return;
             if (!isDimensionalityOk(tool, sys))           return;
                        
             /** End bodge warning. **/
             
         
     		hiddenPart = null;
 
     		// Create a page for the part.
     		PageRec rec = getPageRec(part);
     		if (rec == null || rec.tool==null) {
     			rec = createPage(part, null);
     		}
     		
     		// Show the page.
     		if (rec != null && isToolAllowed(rec.tool)) {
     			showPageRec(rec);
     	   		updatePartInfo(rec.tool);
     		} else {
     			showPageRec(defaultPageRec);
     	   		updatePartInfo(defaultPageRec.tool);
    		    }
 
  
         } catch (Throwable ne) {
         	logger.error("Problem updating activated state in "+getClass().getName(), ne); // No stack required in log here.
         } finally {
         	updatingActivated = false;
         	newPartFound      = false;
         }
 	}
 	
 	private boolean isDimensionalityOk(IToolPage tool, IToolPageSystem sys) {
 		if (tool instanceof AbstractToolPage && sys instanceof IPlottingSystem) {
         	if (((AbstractToolPage)tool).isDedicatedView()) {
         		IPlottingSystem ps = (IPlottingSystem)sys;
         		if (tool.getToolPageRole()==ToolPageRole.ROLE_2D && !ps.is2D()) {
         			return false;
         		} else if (tool.getToolPageRole()==ToolPageRole.ROLE_1D && !ps.getPlotType().is1D()) {
         			return false;
         		} else if (tool.getToolPageRole()==ToolPageRole.ROLE_3D && !ps.getPlotType().is3D()) {
         			return false;
         		}
         	}
         }
 	    return true;	
 	}
 
 	private void updatePartInfo(IToolPage tool) {
 		if (isDisposed) return;
 		if (getViewSite().getSecondaryId()!=null) return; // It is fixed
 		setPartName(tool.getTitle());
 		final ImageDescriptor des = tool.getImageDescriptor();
 		if (des!=null) setTitleImage(des.createImage());
 	}
 	
 
 	protected PageRec getPageRec(IWorkbenchPart part) {
 		
 		if (synchronizeStaticTool(part)) {
 			return activeRec;
 		}
 
 		final String path = getString(part);
 		if (path==null) return null;
 		
         final Map<String, PageRec> pages = recs.get(path);
         if (pages == null) return null;
         
 		IToolPageSystem sys = (IToolPageSystem)part.getAdapter(IToolPageSystem.class);
 		final IToolPage tool= getViewSite().getSecondaryId()==null
 				            ? sys.getCurrentToolPage(getViewRole())
 				            : sys.getToolPage(getViewSite().getSecondaryId());
 				            
 		PageRec rec =  (sys!=null && tool!=null) ? pages.get(tool.getToolId()) : null;
 		if (rec==null || rec.isDisposed()) return null;
 		return rec;
 	}	
 	
 	private boolean synchronizeStaticTool(IWorkbenchPart part) {
 		
 		try {
 			if (staticTool!=null && !(staticTool instanceof EmptyTool)) {
 				IToolPageSystem sys = (IToolPageSystem)part.getAdapter(IToolPageSystem.class);
 				staticTool.setToolSystem(sys);
 				if (sys instanceof IPlottingSystem) {
 					staticTool.setPlottingSystem((IPlottingSystem)sys);
 				}
 				staticTool.setPart(part);
 				staticTool.activate();
 				return true;
 			}
 			return false;
 		} catch (Throwable ne) {
 			logger.error("Cannot use static tool!", ne);
 			return false;
 		}
 	}
 
 	protected PageRec getPageRec(IPage page) {
 		
 		if (page instanceof IToolPage) {
 			final String path = getString(((IToolPage)page).getPart());
 			if (path==null) return null;
 	        final Map<String, PageRec> pages = recs.get(path);
 	        if (pages == null) return null;
 	        
 	        return pages.get(((IToolPage)page).getToolId());
 		} else {
 			return null;
 		}
 	}
 	
 	protected IWorkbenchPart getBootstrapPart() {
 		
 		IWorkbenchPage page = getSite().getPage();
 		if(page != null) {
 			// check whether the active part is important to us
 			IWorkbenchPart activePart = page.getActivePart();
 			return isImportant(activePart)?activePart:null;
 		}
 		return null;	
 	}
 
 	protected boolean isImportant(IWorkbenchPart part) {
 		if (isDisposed) return false;
         if (part==null) return false;
 		if (systems==null || recs==null)  return false;
 		
 		// If same part - not important
 		final String partId = part.getSite().getId();
 		final String ourId  = getSite().getId();
         if (partId.equals(ourId)) return false;
                  
  		IToolPageSystem sys = (IToolPageSystem)part.getAdapter(IToolPageSystem.class);
         return sys != null;
 	}
 	
 	private boolean isDisposed = false;
 	
 	public void dispose() {
 
 		// Run super.
 		super.dispose();
 
 		isDisposed = true;
 
 		// stop listening to part activation
 		getSite().getPage().removePartListener(partListener);
 
 		// Dedicated tool disposal
 		final String toolId = getViewSite().getSecondaryId();
 		if (toolId!=null && activeRec!=null && activeRec.tool!=null) {
 			activeRec.tool.dispose(); // Dispose cloned tool			
 		}
 
 		// Deref all of the pages.
 		activeRec = null;
 		if (defaultPageRec != null) {
 			// check for null since the default page may not have
 			// been created (ex. perspective never visible)
 			defaultPageRec.tool.dispose();
 			defaultPageRec = null;
 		}
 				
 		for (SoftReference<IToolPageSystem> ref : systems) {
 			IToolPageSystem sys = ref.get();
 			if (sys!=null) sys.removeToolChangeListener(this);
 		}
 		for(String partLoc : recs.keySet()) {
 			removeTools(partLoc, false);
 		}
 		recs.clear();
 		recs = null;
 		
 		if (mapToolToNumRecs!=null) this.mapToolToNumRecs.clear();
 		if (mapToolToSite!=null)    this.mapToolToSite.clear();
 		
 		systems.clear();
 		systems = null;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((unique_id == null) ? 0 : unique_id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ToolPageView other = (ToolPageView) obj;
 		if (unique_id == null) {
 			if (other.unique_id != null)
 				return false;
 		} else if (!unique_id.equals(other.unique_id))
 			return false;
 		return true;
 	}
 
 	public String toString() {
 		return ((IToolPage)getCurrentPage()).getTitle();
 	}
 
 	@Override
 	public AbstractToolPage getActiveTool() {
 		return (AbstractToolPage)getCurrentPage();
 	}
 }
