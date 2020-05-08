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
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.ArrayUtils;
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
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Attribute;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Dataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Group;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
 import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;
 import uk.ac.diamond.scisoft.analysis.rcp.explorers.MetadataSelection;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisChoice;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisSelection;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
 import uk.ac.diamond.scisoft.analysis.rcp.views.AsciiTextView;
 import uk.ac.gda.monitor.IMonitor;
 
 public class HDF5TreeExplorer extends AbstractExplorer implements ISelectionProvider {
 	private static final Logger logger = LoggerFactory.getLogger(HDF5TreeExplorer.class);
 
 	HDF5File tree = null;
 	private HDF5TableTree tableTree = null;
 	private Display display;
 
 	private ILazyDataset cData; // chosen dataset
 	List<AxisSelection> axes; // list of axes for each dimension
 	private String filename;
 
 	/**
 	 * Separator between (full) file name and node path
 	 */
 	public static final String HDF5FILENAME_NODEPATH_SEPARATOR = "#";
 
 	public static class HDF5Selection extends DatasetSelection {
 		private String fileName;
 		private String node;
 
 		public HDF5Selection(InspectorType type, String filename, String node, List<AxisSelection> axes, ILazyDataset... dataset) {
 			super(type, axes, dataset);
 			this.fileName = filename;
 			this.node = node;
 		}
 
 		@Override
 		public boolean equals(Object other) {
 			if (super.equals(other) && other instanceof HDF5Selection) {
 				HDF5Selection that = (HDF5Selection) other;
 				if (fileName == null && that.fileName == null)
 					return node.equals(that.node);
 				if (fileName != null && fileName.equals(that.fileName))
 					return node.equals(that.node);
 			}
 			return false;
 		}
 
 		@Override
 		public int hashCode() {
 			int hash = super.hashCode();
 			hash = hash * 17 + node.hashCode();
 			return hash;
 		}
 
 		@Override
 		public String toString() {
 			return node + " = " + super.toString();
 		}
 
 		public String getFileName() {
 			return fileName;
 		}
 
 		public String getNode() {
 			return node;
 		}
 	}
 
 	private HDF5Selection hdf5Selection;
 	private Set<ISelectionChangedListener> cListeners;
 
 	private Listener contextListener = null;
 
 	private DataHolder holder;
 
 	private boolean isOldGDA = false; // true if file has NXentry/program_name < GDAVERSION
 
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
 		axes = new ArrayList<AxisSelection>();
 	}
 
 	/**
 	 * populate a selection object
 	 */
 	public void selectHDF5Node(HDF5NodeLink link, InspectorType type) {
 		if (link == null)
 			return;
 
 		if (handleSelectedNode(link)) {
 			// provide selection
 			setSelection(new HDF5Selection(type, filename, link.getFullName(), axes, cData));
 
 		} else
 			logger.error("Could not process update of selected node: {}", link.getName());
 	}
 
 	private boolean handleSelectedNode(HDF5NodeLink link) {
 		if (processTextNode(link)) {
 			return false;
 		}
 
 		if (!processSelectedNode(link))
 			return false;
 
 		if (cData == null)
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * Handle a node given by the path
 	 * @param path
 	 */
 	public void handleNode(String path) {
 		HDF5NodeLink link = tree.findNodeLink(path);
 
 		if (link != null) {
 			if (handleSelectedNode(link)) {
 				// provide selection
 				setSelection(new HDF5Selection(InspectorType.LINE, filename, link.getName(), axes, cData));
 				return;
 			}
 			logger.debug("Could not handle selected node: {}", link.getName());
 		}
 		logger.debug("Could not find selected node: {}", path);
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
 
 	private static final String NXAXES = "axes";
 	private static final String NXAXIS = "axis";
 	private static final String NXLABEL = "label";
 	private static final String NXPRIMARY = "primary";
 	private static final String NXSIGNAL = "signal";
 	private static final String NXDATA = "NXdata";
 	private static final String NXNAME = "long_name";
 	private static final String SDS = "SDS";
 
 	private boolean processSelectedNode(HDF5NodeLink link) {
 		// two cases: axis and primary or axes
 		// iterate through each child to find axes and primary attributes
 		HDF5Node node = link.getDestination();
 		List<AxisChoice> choices = new ArrayList<AxisChoice>();
 		HDF5Attribute axesAttr = null;
 		HDF5Group gNode = null;
 		HDF5Dataset dNode = null;
 
 		// see if chosen node is a NXdata class
 		HDF5Attribute stringAttr = node.getAttribute(HDF5File.NXCLASS);
 		String nxClass = stringAttr != null ? stringAttr.getFirstElement() : null;
 		if (nxClass == null || nxClass.equals(SDS)) {
 			if (!(node instanceof HDF5Dataset))
 				return false;
 
 			dNode = (HDF5Dataset) node;
 			if (!dNode.isSupported())
 				return false;
 			cData = dNode.getDataset();
 			axesAttr = dNode.getAttribute(NXAXES);
 			gNode = (HDF5Group) link.getSource(); // before hunting for axes
 		} else if (nxClass.equals(NXDATA)) {
 			assert node instanceof HDF5Group;
 			gNode = (HDF5Group) node;
 			// find data (signal=1) and check for axes attribute
 			Iterator<HDF5NodeLink> iter = gNode.getNodeLinkIterator();
 			while (iter.hasNext()) {
 				HDF5NodeLink l = iter.next();
 				if (l.isDestinationADataset()) {
 					dNode = (HDF5Dataset) l.getDestination();
 					if (dNode.containsAttribute(NXSIGNAL) && dNode.isSupported()) {
 						cData = dNode.getDataset();
 						axesAttr = dNode.getAttribute(NXAXES);
 						break; // only one signal per NXdata item
 					}
 					dNode = null;
 				}
 			}
 		}
 
 		if (dNode == null)
 			return false;
 
 		// find possible long name
 		stringAttr = dNode.getAttribute(NXNAME);
 		if (stringAttr != null && stringAttr.isString())
 			cData.setName(stringAttr.getFirstElement());
 
 		// remove extraneous dimensions
 		cData.squeeze(true);
 
 		// set up slices
 		int[] shape = cData.getShape();
 		int rank = shape.length;
 
 		// scan children for SDS as possible axes (could be referenced by axes)
 		@SuppressWarnings("null")
 		Iterator<HDF5NodeLink> iter = gNode.getNodeLinkIterator();
 		while (iter.hasNext()) {
 			HDF5NodeLink l = iter.next();
 			if (l.isDestinationADataset()) {
 				HDF5Dataset d = (HDF5Dataset) l.getDestination();
 				if (!d.isSupported() || d.isString() || dNode == d || d.containsAttribute(NXSIGNAL))
 					continue;
 
 				ILazyDataset a = d.getDataset();
 
 				try {
 					int[] s = a.getShape();
 					s = AbstractDataset.squeezeShape(s, true);
 
 					if (s.length != 0) // don't make a 0D dataset
 						a.squeeze(true);
 
 					int[] ashape = a.getShape();
 
 					AxisChoice choice = new AxisChoice(a);
 					stringAttr = d.getAttribute(NXNAME);
 					if (stringAttr != null && stringAttr.isString())
 						choice.setLongName(stringAttr.getFirstElement());
 
 					HDF5Attribute attr = d.getAttribute(NXAXIS);
 					HDF5Attribute attr_label = d.getAttribute(NXLABEL);
 					int[] intAxis = null;
 					if (attr != null) {
 						if (attr.isString()) {
 							String[] str = attr.getFirstElement().split(",");
 							if (str.length == ashape.length) {
 								intAxis = new int[str.length];
 								for (int i = 0; i < str.length; i++) {
 									int j = Integer.parseInt(str[i]) - 1;
									intAxis[i] = isOldGDA ? j : rank - 1 - j; // fix Fortran (column-major) dimension
 								}
 							}
 						} else {
 							AbstractDataset attrd = attr.getValue();
 							if (attrd.getSize() == ashape.length) {
 								intAxis = new int[attrd.getSize()];
 								IndexIterator it = attrd.getIterator();
 								int i = 0;
 								while (it.hasNext()) {
 									int j = (int) attrd.getElementLongAbs(it.index) - 1;
									intAxis[i++] = isOldGDA ? j : rank - 1 - j; // fix Fortran (column-major) dimension
 								}
 							}
 						}
 
 						if (intAxis == null) {
 							logger.warn("Axis attribute {} does not match rank", a.getName());
 						} else {
 							// check that axis attribute matches data dimensions
 							for (int i = 0; i < intAxis.length; i++) {
 								int al = ashape[i];
 								int il = intAxis[i];
 								if (il < 0 || il >= rank || al != shape[il]) {
 									intAxis = null;
 									logger.warn("Axis attribute {} does not match shape", a.getName());
 									break;
 								}
 							}
 						}
 					}
 
 					if (intAxis == null) {
 						// remedy bogus or missing axis attribute by simply pairing matching dimension
 						// lengths to the signal dataset shape (this may be wrong as transposes in
 						// common dimension lengths can occur)
 						logger.warn("Creating index mapping from axis shape");
 						Map<Integer, Integer> dims = new LinkedHashMap<Integer, Integer>();
 						for (int i = 0; i < rank; i++) {
 							dims.put(i, shape[i]);
 						}
 						intAxis = new int[ashape.length];
 						for (int i = 0; i < intAxis.length; i++) {
 							int al = ashape[i];
 							intAxis[i] = -1;
 							for (int k : dims.keySet()) {
 								if (al == dims.get(k)) { // find first signal dimension length that matches
 									intAxis[i] = k;
 									dims.remove(k);
 									break;
 								}
 							}
 							if (intAxis[i] == -1)
 								throw new IllegalArgumentException(
 										"Axis dimension does not match any data dimension");
 						}
 					}
 
 					choice.setIndexMapping(intAxis);
 					if (attr_label != null) {
 						if (attr_label.isString()) {
 							int j = Integer.parseInt(attr_label.getFirstElement()) - 1;
							choice.setAxisNumber(isOldGDA ? j : rank - 1 - j); // fix Fortran (column-major) dimension
 						} else {
 							int j = attr_label.getValue().getInt(0) - 1;
							choice.setAxisNumber(isOldGDA ? j : rank - 1 - j); // fix Fortran (column-major) dimension
 						}
 					} else
 						choice.setAxisNumber(intAxis[intAxis.length-1]);
 
 					attr = d.getAttribute(NXPRIMARY);
 					if (attr != null) {
 						if (attr.isString()) {
 							Integer intPrimary = Integer.parseInt(attr.getFirstElement());
 							choice.setPrimary(intPrimary);
 						} else {
 							AbstractDataset attrd = attr.getValue();
 							choice.setPrimary(attrd.getInt(0));
 						}
 					}
 					choices.add(choice);
 				} catch (Exception e) {
 					logger.warn("Axis attributes in {} are invalid - {}", a.getName(), e.getMessage());
 					continue;
 				}
 			}
 		}
 
 		List<String> aNames = new ArrayList<String>();
 		if (axesAttr != null) { // check axes attribute for list axes
 			String axesStr = axesAttr.getFirstElement().trim();
 			if (axesStr.startsWith("[")) { // strip opening and closing brackets
 				axesStr = axesStr.substring(1, axesStr.length() - 1);
 			}
 
 			// check if axes referenced by data's @axes tag exists
 			String[] names = null;
 			names = axesStr.split("[:,]");
 			for (String s : names) {
 				boolean flg = false;
 				for (AxisChoice c : choices) {
 					if (c.equals(s)) {
 						if (c.getRank() == 1) { // FIXME for N-D axes SDSes
 							// this needs a standard, e.g. axis SDS can span signal dataset dimensions
 							flg = true;
 							break;
 						}
 						logger.warn("Referenced axis {} in tree node {} is not 1D", s, node);
 					}
 				}
 				if (flg) {
 					aNames.add(s);
 				} else {
 					logger.warn("Referenced axis {} does not exist in tree node {}", s, node);
 					aNames.add(null);
 				}
 			}
 		}
 
 		// set up AxisSelector
 		// build up list of choice per dimension
 		axes.clear();
 
 		for (int i = 0; i < rank; i++) {
 			int dim = shape[i];
 			AxisSelection aSel = new AxisSelection(dim);
 			axes.add(i, null); // expand list
 			for (AxisChoice c : choices) {
 				if (c.getAxisNumber() == i) {
 					// add if choice has been designated as for this dimension
 					aSel.addChoice(c, c.getPrimary());
 				} else if (c.isDimensionUsed(i)) {
 					// add if axis index mapping refers to this dimension
 					aSel.addChoice(c, 0);
 				} else if (aNames.contains(c.getName())) {
 					// add if name is in list of axis names
 					aSel.addChoice(c, 1);
 				}
 			}
 
 			// add in an automatically generated axis with top order so it appears after primary axes
 			AbstractDataset axis = AbstractDataset.arange(dim, AbstractDataset.INT32);
 			axis.setName("dim:" + (i + 1));
 			AxisChoice newChoice = new AxisChoice(axis);
 			newChoice.setAxisNumber(i);
 			aSel.addChoice(newChoice, aSel.getMaxOrder() + 1);
 
 			aSel.reorderChoices();
 			aSel.selectAxis(0);
 			axes.set(i, aSel);
 		}
 
 		return true;
 	}
 
 	@Override
 	public void dispose() {
 		cListeners.clear();
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
 		
 		HDF5File ltree = new HDF5Loader(fileName).loadTree(mon);
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
 		isOldGDA = checkForOldGDAFile();
 
 		if (display != null)
 			display.asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					tableTree.setInput(tree.getNodeLink());
 					display.update();
 				}
 			});
 	}
 
 	private static final String NXENTRY = "NXentry";
 	private static final String NXPROGRAM = "program_name";
 	private static final String GDAVERSIONSTRING = "GDA ";
 	private static final int GDAMAJOR = 8;
 	private static final int GDAMINOR = 20;
 
 	private boolean checkForOldGDAFile() {
 		Iterator<HDF5NodeLink> iter = tree.getGroup().getNodeLinkIterator();
 		
 		while (iter.hasNext()) {
 			HDF5NodeLink link = iter.next();
 			if (link.isDestinationAGroup()) {
 				HDF5Group g = (HDF5Group) link.getDestination();
 				HDF5Attribute stringAttr = g.getAttribute(HDF5File.NXCLASS);
 				if (stringAttr != null && stringAttr.isString() && NXENTRY.equals(stringAttr.getFirstElement())) {
 					if (g.containsDataset(NXPROGRAM)) {
 						HDF5Dataset d = g.getDataset(NXPROGRAM);
 						if (d.isString()) {
 							String s = d.getString().trim();
 							int i = s.indexOf(GDAVERSIONSTRING);
 							if (i >= 0) {
 								String v = s.substring(i+4, s.lastIndexOf("."));
 								int j = v.indexOf(".");
 								int maj = Integer.parseInt(v.substring(0, j));
 								int min = Integer.parseInt(v.substring(j+1, v.length()));
 								return maj < GDAMAJOR || (maj == GDAMAJOR && min < GDAMINOR);
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return false;
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
