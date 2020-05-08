 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.hdf5;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.PartInitException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Attribute;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Dataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
 import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;
 import uk.ac.diamond.scisoft.analysis.rcp.explorers.MetadataSelection;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
 import uk.ac.diamond.scisoft.analysis.rcp.views.AsciiTextView;
 import uk.ac.gda.monitor.IMonitor;
 
 public class HDF5TreeExplorer extends AbstractExplorer implements ISelectionProvider {
 	private static final Logger logger = LoggerFactory.getLogger(HDF5TreeExplorer.class);
 
 	HDF5File tree = null;
 	private HDF5TableTree tableTree = null;
 	private Display display;
 
 	private String filename;
 
 	/**
 	 * Separator between (full) file name and node path
 	 */
 	public static final String HDF5FILENAME_NODEPATH_SEPARATOR = "#";
 
 	private HDF5Selection hdf5Selection;
 	private Set<ISelectionChangedListener> cListeners;
 
 	private Listener contextListener = null;
 
 	private DataHolder holder;
 
 	private boolean isOldGDA = false; // true if file has NXentry/program_name < GDAVERSION
 
 	private HDF5Loader loader = null;
 
 	public HDF5TreeExplorer(Composite parent, IWorkbenchPartSite partSite, ISelectionChangedListener valueSelect) {
 		super(parent, partSite, valueSelect);
 
 		display = parent.getDisplay();
 
 		setLayout(new FillLayout());
 
 		if (metaValueListener != null) {
 			contextListener = new Listener() {
 				@Override
 				public void handleEvent(Event event) {
 					handleContextClick();
 				}
 			};
 		}
 		
 		Listener singleListener = new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				if (event.button == 1)
 					handleSingleClick();
 			}
 		};
 
 		tableTree = new HDF5TableTree(this, singleListener, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				if (event.button == 1)
 					handleDoubleClick();
 			}
 		}, contextListener);
 
 		cListeners = new HashSet<ISelectionChangedListener>();
 	}
 
 	/**
 	 * Select a node and populate a selection object
 	 * @param link node link
 	 * @param type
 	 */
 	public void selectHDF5Node(HDF5NodeLink link, InspectorType type) {
 		if (link == null)
 			return;
 
 		if (processTextNode(link)) {
 			return;
 		}
 
 		HDF5Selection s = HDF5Utils.createDatasetSelection(link, isOldGDA);
 		if (s == null) {
 			logger.error("Could not process update of selected node: {}", link.getName());
 			return;
 		}
 
 		// provide selection
 		s.setFileName(filename);
 		s.setType(type);
 		setSelection(s);
 	}
 
 	/**
 	 * Select a node and populate a selection object
 	 * @param path path of node
 	 * @param type
 	 */
 	public void selectHDF5Node(String path, InspectorType type) {
 		HDF5NodeLink link = tree.findNodeLink(path);
 
 		if (link != null) {
 			selectHDF5Node(link, type);
 		} else {
 			logger.debug("Could not find selected node: {}", path);
 		}
 	}
 
 	private void handleContextClick() {
 		IStructuredSelection selection = tableTree.getSelection();
 
 		try {
 			// check if selection is valid for plotting
 			if (selection != null) {
 				Object obj = selection.getFirstElement();
 				String metaName = null;
 				if (obj instanceof HDF5NodeLink) {
 					metaName = ((HDF5NodeLink) obj).getFullName();
 				} else if (obj instanceof HDF5Attribute) {
 					metaName = ((HDF5Attribute) obj).getFullName();
 				}
 				if (metaName != null) {
 					SelectionChangedEvent ce = new SelectionChangedEvent(this, new MetadataSelection(metaName));
 					metaValueListener.selectionChanged(ce);
 				}
 			}
 		} catch (Exception e) {
 			logger.error("Error processing selection: {}", e.getMessage());
 		}
 	}
 
 	private void handleSingleClick() {
 		// Single click passes the standard tree selection on.
 		IStructuredSelection selection = tableTree.getSelection();
 		SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
 		for (ISelectionChangedListener s : cListeners) s.selectionChanged(e);
 	}
 	
 	private void handleDoubleClick() {
 		final Cursor cursor = getCursor();
 		Cursor tempCursor = getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
 		if (tempCursor != null) setCursor(tempCursor);
 
 		IStructuredSelection selection = tableTree.getSelection();
 
 		try {
 			// check if selection is valid for plotting
 			if (selection != null && selection.getFirstElement() instanceof HDF5NodeLink) {
 				HDF5NodeLink link = (HDF5NodeLink) selection.getFirstElement();
 				selectHDF5Node(link, InspectorType.LINE);
 			}
 		} catch (Exception e) {
 			logger.error("Error processing selection: {}", e.getMessage());
 		} finally {
 			if (tempCursor != null)
 				setCursor(cursor);
 		}
 	}
 
 	private boolean processTextNode(HDF5NodeLink link) {
 		HDF5Node node = link.getDestination();
 		if (!(node instanceof HDF5Dataset))
 			return false;
 
 		HDF5Dataset dataset = (HDF5Dataset) node;
 		if (!dataset.isString())
 			return false;
 
 		try {
 			getTextView().setData(dataset.getString());
 			return true;
 		} catch (Exception e) {
 			logger.error("Error processing text node {}: {}", link.getName(), e);
 		}
 		return false;
 	}
 
 	@Override
 	public void dispose() {
 		if (loader != null)
 			loader.stopAsyncLoading();
 
 		cListeners.clear();
 		tree = null;
 		holder = null;
 		loader = null;
 		super.dispose();
 	}
 
 	private AsciiTextView getTextView() {
 		AsciiTextView textView = null;
 		// check if Dataset Table View is open
 		try {
 			textView = (AsciiTextView) site.getPage().showView(AsciiTextView.ID);
 		} catch (PartInitException e) {
 			logger.error("All over now! Cannot find ASCII text view: {} ", e);
 		}
 
 		return textView;
 	}
 
 	@Override
 	public DataHolder loadFile(String fileName, IMonitor mon) throws Exception {
 		if (fileName == filename)
 			return holder;
 
 		return new HDF5Loader(fileName).loadFile(mon);
 	}
 
 	@Override
 	public void loadFileAndDisplay(String fileName, IMonitor mon) throws Exception {
 		loader = new HDF5Loader(fileName);
 		loader.setAsyncLoad(true);
 		HDF5File ltree = loader.loadTree(mon);
 		if (ltree != null) {
 			holder = new DataHolder();
 			Map<String, ILazyDataset> map = HDF5Loader.createDatasetsMap(ltree.getGroup());
 			for (String n : map.keySet()) {
 				holder.addDataset(n, map.get(n));
 			}
 			holder.setMetadata(HDF5Loader.createMetaData(ltree));
 
 			setFilename(fileName);
 
 			setHDF5Tree(ltree);
 		}
 	}
 
 	/**
 	 * @return loaded tree or null
 	 */
 	public HDF5File getHDF5Tree() {
 		return tree;
 	}
 
 	public void setHDF5Tree(HDF5File htree) {
 		if (htree == null)
 			return;
 
 		tree = htree;
 		isOldGDA = HDF5Utils.isOldGDAFile(tree);
 
 		if (display != null)
 			display.asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					tableTree.setInput(tree.getNodeLink());
 					display.update();
 				}
 			});
 	}
 
 
 	public void expandAll() {
 		tableTree.expandAll();
 	}
 	
 	public void expandToLevel(int level) {
 		tableTree.expandToLevel(level);
 	}
 	
 	public void expandToLevel(Object link, int level) {
 		tableTree.expandToLevel(link, level);
 	}
 	
 	public TreePath[] getExpandedTreePaths() {
 		return tableTree.getExpandedTreePaths();
 	}
 
 
 	// selection provider interface
 	@Override
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		if (cListeners.add(listener)) {
 			return;
 		}
 	}
 
 	@Override
 	public ISelection getSelection() {
 		return hdf5Selection;
 	}
 
 	@Override
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		if (cListeners.remove(listener))
 			return;
 	}
 
 	@Override
 	public void setSelection(ISelection selection) {
 		if (selection instanceof HDF5Selection)
 			hdf5Selection = (HDF5Selection) selection;
 		else
 			return;
 
 		SelectionChangedEvent e = new SelectionChangedEvent(this, hdf5Selection);
 		for (ISelectionChangedListener s : cListeners)
 			s.selectionChanged(e);
 	}
 
 	/**
 	 * Set full name for file (including path)
 	 * @param fileName
 	 */
 	public void setFilename(String fileName) {
 		filename = fileName;
 	}
 
 	public HDF5TableTree getTableTree(){
 		return tableTree;
 	}
 }
