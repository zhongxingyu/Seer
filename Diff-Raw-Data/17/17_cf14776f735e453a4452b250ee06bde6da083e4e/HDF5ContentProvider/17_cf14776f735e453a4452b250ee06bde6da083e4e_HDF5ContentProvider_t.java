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
 
 package uk.ac.diamond.sda.navigator.hdf5;
 
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Attribute;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Dataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Group;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
 import uk.ac.diamond.scisoft.analysis.rcp.hdf5.HDF5TableTree;
 import uk.ac.diamond.scisoft.analysis.rcp.hdf5.TreeFilter;
 import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
 
 
 public class HDF5ContentProvider implements ITreeContentProvider {
 	private TreeFilter treeFilter;
 	public static final String H5_EXT = "h5"; //$NON-NLS-1$
 	public static final String HDF5_EXT = "hdf5"; //$NON-NLS-1$
 	public static final String NXS_EXT = "nxs"; //$NON-NLS-1$
 	private HDF5File hdf5File;
 	private String fileName;
 	private IFile modelFile;
 	private static final Object[] NO_CHILDREN = new Object[0];
 	private static final Logger logger = LoggerFactory.getLogger(HDF5ContentProvider.class);
 
 	public HDF5ContentProvider() {
 		this.treeFilter = new TreeFilter(new String[] { "target", HDF5File.NXCLASS });
 	}
 
 	@Override
 	public Object[] getChildren(Object parent) {
 		Object[] children = NO_CHILDREN;
 		
 		if (parent instanceof IFile) {
 			modelFile = (IFile) parent;
 		
 			if (H5_EXT.equals(modelFile.getFileExtension())||HDF5_EXT.equals(modelFile.getFileExtension())||NXS_EXT.equals(modelFile.getFileExtension())) {
 				loadHDF5Data(modelFile);
 				HDF5Group pNode = hdf5File.getGroup();
 
 				children = new Object[pNode.getNumberOfNodelinks()];
 				int count = 0;
 				for (HDF5NodeLink link : pNode) {
 					link.setFile(modelFile);
 					children[count] = link;
 					count++;
 				}
 				return children;
 			}
 		}
 		if (parent instanceof HDF5Attribute) {
 			return null;
 		}
 		assert parent instanceof HDF5NodeLink : "Not an attribute or a link";
 		HDF5Node pNode = ((HDF5NodeLink) parent).getDestination();
 		
 		int count = 0;
 		Iterator<String> iter = pNode.getAttributeNameIterator();
		children = new Object[HDF5TableTree.countChildren(parent, treeFilter)];

 		while (iter.hasNext()) {
 			String name = iter.next();
 			if (treeFilter.select(name)) {
 				HDF5Attribute a = pNode.getAttribute(name);
 				children[count] = a;
 				count++;
 			}
 		}
 		if (pNode instanceof HDF5Group) {
 			for (HDF5NodeLink link : (HDF5Group) pNode) {
 				if (link.isDestinationAGroup()) {
 					String name = link.getName();
 					if (treeFilter.select(name)) {
 						link.setFile(modelFile);
 						children[count] = link;
 						count++;
 					}
 				}
 			}
 			for (HDF5NodeLink link : (HDF5Group) pNode) {
 				if (link.isDestinationADataset()) {
 					String name = link.getName();
 					if (treeFilter.select(name)) {
 						link.setFile(modelFile);
 						children[count] = link;
 						count++;
 					}
 				}
 			}
 
 		} else if (pNode instanceof HDF5Dataset) {
 			// do nothing
 		}
 		return children;
 	}
 
 	@Override
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	@Override
 	public boolean hasChildren(Object element) {
 		if((element instanceof HDF5NodeLink) && ((HDF5TableTree.countChildren(element, treeFilter) > 0)) || (element instanceof IFile))
 			return true;
 		return false;
 	}
 
 	@Override
 	public Object getParent(Object element) {
 		if (element == null || !(element instanceof HDF5NodeLink)) {
 			return null;
 		}
 		HDF5Node node = ((HDF5NodeLink) element).getSource();
 		if (node == null)
 			return element;
 		return node;
 	}
 
 	@Override
 	public void dispose() {
 //		cachedModelMap.clear();
 	}
 
 	@Override
 	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
 //		if (oldInput != null && !oldInput.equals(newInput)) {
 //			cachedModelMap.clear();
 //		}
 //		viewer = (StructuredViewer) aViewer;
 	}
 
 	/**
 	 * Load the HDF5 tree from the given file, if possible.
 	 * 
 	 * @param file
 	 *            The IFile which contains the hdf5 tree
 	 */
 	private void loadHDF5Data(IFile file) {
 		fileName = file.getLocation().toString();
 		try {
 			hdf5File = new HDF5Loader(fileName).loadTree();
 		} catch (Exception e) {
 			logger.warn("Could not load NeXus file {}", fileName);
 		}
 	}
 }
