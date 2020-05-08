 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawb.tango.extensions.editors;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeNode;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.util.list.IdentityList;
 import org.dawb.gda.extensions.spec.MultiScanDataParser;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ICheckStateProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 /**
  * This view can view and plot any file. It is most efficient if the Loader that LoaderFactory
  * uses for this file type is an IMetaLoader. 
  */
 public class MultiScanComponent implements ICheckStateListener {
 		
 	private static final Logger logger = LoggerFactory.getLogger(MultiScanComponent.class);
 
 	// NOTE Old ID before this class was convert to display files without knowing the 
 	// underlying file type.
 	public static final String ID = "uk.ac.gda.views.nexus.NexusPlotView"; //$NON-NLS-1$
 
 	// Use table as it might get extended to do more later.
 	protected CheckboxTreeViewer  dataViewer;
 	protected MultiScanDataParser            data;
 	protected AbstractPlottingSystem system;
 	
 	public MultiScanComponent(AbstractPlottingSystem system) {
 		this.system = system;
 	}
 	/**
 	 * Create contents of the view part.
 	 * @param parent
 	 */
 	public void createPartControl(Composite parent) {
 		
 		Composite container = new Composite(parent, SWT.NONE);
 		if (parent.getLayout() instanceof GridLayout) container.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		GridLayout gl_container = new GridLayout(1, false);
 		gl_container.verticalSpacing = 0;
 		gl_container.marginWidth = 0;
 		gl_container.marginHeight = 0;
 		gl_container.horizontalSpacing = 0;
 
 		container.setLayout(gl_container);
 		
 		this.dataViewer = new CheckboxTreeViewer(container, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER | SWT.VIRTUAL);
 		dataViewer.setCheckStateProvider(new ICheckStateProvider() {		
 			@Override
 			public boolean isGrayed(Object element) {
 				return false;
 			}
 			
 			@Override
 			public boolean isChecked(Object element) {
 				if (checked==null) return false;
 				return checked.contains(element);
 			}
 		});
 		
 		dataViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
 		dataViewer.addCheckStateListener(this);
 		dataViewer.setUseHashlookup(true);
 		dataViewer.getTree().addListener(SWT.EraseItem, new Listener()  {
 			public void handleEvent(Event event) {
 				event.detail &= ~SWT.HOT;
 				if ((event.detail & SWT.SELECTED) == 0) return; /// item not selected
 
 				Tree table =(Tree)event.widget;
 				TreeItem item =(TreeItem)event.item;
 				int clientWidth = table.getClientArea().width;
 
 				GC gc = event.gc;                               
 				Color oldForeground = gc.getForeground();
 				Color oldBackground = gc.getBackground();
 
 				final Object set = item.getData();
 				if (set == null) return;
 				
 				Color back = ((ColumnLabelProvider)dataViewer.getLabelProvider()).getBackground(set);
 				if (back==null) back = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
 				gc.setBackground(back);
 
				Color fore = ((ColumnLabelProvider)dataViewer.getLabelProvider()).getForeground(set);
				if (fore==null) fore = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
 				if (fore!=null) {
 					final String text = ((ColumnLabelProvider)dataViewer.getLabelProvider()).getText(set);
 
 					gc.setForeground(fore);                              
 					gc.fillRectangle(0, event.y, clientWidth, event.height);
 					int yOffset = 0;
 					if (event.index == 1) {
 						Point size = gc.textExtent(text);
 						yOffset = Math.max(0, (event.height - size.y) / 2);
 					}
 					event.gc.drawText(text, event.x + item.getBounds().x+1, event.y + yOffset +3, false);
 					event.doit = false;
 				}
 				
 				gc.setForeground(oldForeground);
 				gc.setBackground(oldBackground);
 				event.detail &= ~SWT.SELECTED;
 			}
 
 		});
 
 		createRightClickMenu();
 		
 		/**
 		 * No need to remove this one, the listeners are cleared on a dispose
 		 */
 		system.addTraceListener(new ITraceListener.Stub() {
 			@Override
 			public void tracesUpdated(TraceEvent evt) {
 				updateSelection();
 			}
 		});
 	}
 	
 	public void setData(MultiScanDataParser data) {
 		this.data = data;
 		
 		dataViewer.getTree().setItemCount(data.getScanNames().size());
 		dataViewer.setUseHashlookup(true);
 		
 		dataViewer.setContentProvider(new MultiScanContentProvider());
 		dataViewer.setLabelProvider(new MultiScanLabelProvider(system));
 		
 		dataViewer.setInput(data.getNode());
 		dataViewer.refresh();
 	}
 
 	public void setFocus() {
 		if (dataViewer!=null && !dataViewer.getControl().isDisposed()) {
 			dataViewer.getControl().setFocus();
 		}
 	}
 
 	private void createRightClickMenu() {	
 //	    final MenuManager menuManager = new MenuManager();
 //	    dataViewer.getControl().setMenu (menuManager.createContextMenu(dataViewer.getControl()));
 //		menuManager.add(new Separator(getClass().getName()+"sep1"));
 //		menuManager.add(new Action("Preferences...") {
 //			@Override
 //			public void run() {
 //				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.edna.workbench.editors.preferencePage", null, null);
 //				if (pref != null) pref.open();
 //			}
 //		});
 	}
 
 	private Set<DefaultMutableTreeNode>  checked;
 	private List<AbstractDataset>        selections;
 
 	
 	@Override
 	public void checkStateChanged(final CheckStateChangedEvent event) {
 		
 
 		if (checked==null)    checked    = new HashSet<DefaultMutableTreeNode>(7);
 		if (selections==null) selections = new IdentityList<AbstractDataset>();
 
 		if (event!=null) {
 			final DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getElement();
 
 			if (!event.getChecked()) {
 				checked.remove(node);
 			} else {
 				checked.add(node);
 			}
 
 			if (node.getUserObject() instanceof AbstractDataset) {
 				final AbstractDataset        set  = (AbstractDataset)node.getUserObject();
 				if (!event.getChecked()) {
 					selections.remove(set);
 				} else {
 					// We only allow selection of one set not 1D
 					if (!selections.contains(set))  selections.add(set);
 				}
 			} else if (node.getUserObject() instanceof String) {
 
 
 				for (int i = 0 ; i<node.getChildCount(); ++i) {
 					final DefaultMutableTreeNode c = (DefaultMutableTreeNode)node.getChildAt(i);
 					dataViewer.setChecked(c, event.getChecked());
 					final AbstractDataset  set  = (AbstractDataset)c.getUserObject();
 					if (event.getChecked()) {
 						checked.add(c);
 						if (!selections.contains(set))  selections.add(set);
 					} else {
 						checked.remove(c);
 						selections.remove(set);
 					}
 				}
 			}
 
 		} else {
 			selections.clear();
 		}
 
 		updateSelection();
 
 
 
 		dataViewer.getControl().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				dataViewer.refresh();
 			}
 		});
 	}
 
 	private synchronized void updateSelection() {
 		if (selections==null) return;
 		fireDatasetListeners(selections);
 	}
 
 	private List<IDatasetListener> listeners;
 	
 	/**
 	 * Call to be notified of data set collections being made.
 	 * The selections returned are a StructuredSelection with a list
 	 * of objects some are Strings for the data set name and
 	 * others are ExpressionObject if the user created expressions.
 	 * 
 	 * NOTE: The listener is NOT called on the GUI thread.
 	 * 
 	 * @param l
 	 */
 	public void addDatasetListener(final IDatasetListener l){
 		if (listeners==null) listeners = new ArrayList<IDatasetListener>(7);
 		listeners.add(l);
 	}
 	
 	public void removeSelectionListener(final IDatasetListener l){
 		if (listeners==null) return;
 		listeners.remove(l);
 	}
 	
 	private void fireDatasetListeners(List<AbstractDataset> selections) {
 		if (listeners==null) return;
 		final DatasetChangedEvent event = new DatasetChangedEvent(this.dataViewer, selections);
 		for (IDatasetListener l : listeners) l.datasetSelectionChanged(event);
 	}
 	
 	public void dispose() {
 		if (listeners!=null) listeners.clear();
 		if (data != null)       this.data.clear();
 		this.data = null;
 		if (dataViewer!=null && !dataViewer.getControl().isDisposed()) {
 			dataViewer.removeCheckStateListener(this);
 		}
 		dataViewer = null;
 	}
 
 	public void refresh(final String scanName) {
 		
 		if (dataViewer!=null && !dataViewer.getControl().isDisposed()) {
 			// TODO Make SpecData only have TreeNodes for its data.
 			// this will be more efficient, and a simple refresh() will work.
 			final Object element = this.data.updateNode(scanName);
 			this.dataViewer.refresh();
 			if (element!=null) {
 				
 				final TreeNode root = (TreeNode)dataViewer.getInput();
 				for (int i = 0; i < root.getChildCount(); i++) {
 					final DefaultMutableTreeNode n = (DefaultMutableTreeNode)root.getChildAt(i);
 					if (!n.getUserObject().equals(scanName)) {
 						this.dataViewer.setChecked(n, false);
 						this.dataViewer.setSubtreeChecked(n, false);
 					}
 				}
 				
 				this.dataViewer.setChecked(element, true);
 				this.dataViewer.setExpandedElements(new Object[]{element});
 				this.dataViewer.setSubtreeChecked(element, true);
 			
 				final List<AbstractDataset> selections = new IdentityList<AbstractDataset>();
 				final TreeNode node = (TreeNode)element;
 				for (int i = 0; i < node.getChildCount(); i++) {
 					selections.add((AbstractDataset)((DefaultMutableTreeNode)node.getChildAt(i)).getUserObject());
 				}
 				fireDatasetListeners(selections);
 			}
 		}
 	}
 	
 	public void clear() {
 		dataViewer.collapseAll();
 	    dataViewer.setAllChecked(false);
 	}
 
     public ColumnViewer getViewer() {
     	return this.dataViewer;
     }
     
     
 	protected void setPlot(String scanName, String... plotNames) {
 		
 		if (selections==null) selections = new IdentityList<AbstractDataset>();
 		this.selections.clear();
 		updateSelection();
 		
 		final Collection<String>         names = Arrays.asList(plotNames);
 		final Collection<AbstractDataset> sets = this.data.getSets(scanName);
 		for (AbstractDataset as : sets) {
 			if (names.contains(as.getName())) selections.add(as);
 		}
 		updateSelection();
 	}
 }
