 package org.dawb.gda.extensions.h5;
 
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeNode;
 
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.Group;
 import ncsa.hdf.object.HObject;
 import ncsa.hdf.object.h5.H5Group;
 import ncsa.hdf.object.h5.H5ScalarDS;
 
 import org.dawb.hdf5.HierarchicalDataUtils;
 import org.dawb.hdf5.editor.IH5DoubleClickSelectionProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Dataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Group;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
 import uk.ac.diamond.scisoft.analysis.io.ExternalFiles;
 import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
 import uk.ac.diamond.scisoft.analysis.rcp.hdf5.HDF5Utils;
 
 
 public class HDF5SelectionProvider implements IH5DoubleClickSelectionProvider {
 
 	private static final Logger logger = LoggerFactory.getLogger(HDF5SelectionProvider.class);
 
 	@Override
 	public ISelection getSelection(final ISelection selection, final String filePath) throws Exception {
 
         if (selection instanceof IStructuredSelection) {
         	
         	final IStructuredSelection sel = (IStructuredSelection)selection;
         	final Object[] oa  = sel.toArray();
         	if (oa == null) return null;
 
         	HDF5File     file      = null;
         	HDF5Node     dest      = null;
         	HDF5Node     parent    = null;
         	String      fullName   = null;
             TreeNode parNode  = null;
         	for (int i = 0; i < oa.length; i++) {
 				
 				if (oa[i] instanceof TreeNode) { // HDF5Tree
 					
 					final TreeNode treeNode = (TreeNode)oa[i];
                     parNode  = treeNode.getParent();
                    	final HObject root = (HObject)((DefaultMutableTreeNode)((DefaultMutableTreeNode)treeNode).getRoot()).getUserObject();
                     
                     HObject object = null;
                     if (parNode!=null) {
                     	object  = (HObject)((DefaultMutableTreeNode)parNode).getUserObject();
                     	file = new HDF5File(root.getOID()[0], filePath);
                     	
                     	parent  = createGroup(object, file);
                     }
 
 					object   = (HObject)((DefaultMutableTreeNode)treeNode).getUserObject();
 					HObject link   = HierarchicalDataUtils.getDataLink(object, null);
 					object = link != null ? link : object;
 					
                     if (object instanceof Dataset) {
                     	Dataset ds = (Dataset)object;
                      	dest    = createDataset(ds, filePath, new HDF5File(root.getOID()[0], filePath));
                     	fullName   = ds.getFullName();
                     } else if (object instanceof Group) {
                     	Group g = (Group) object;
                     	dest = createGroup(g, file);
                     	fullName = g.getFullName();
                     }
 				}
 			}
         	if (dest == null)
         		return null;
 
         	final HDF5NodeLink link = new HDF5NodeLink(file, filePath, fullName, parent, dest);
 
         	// find out if it is a GDA NeXus tree by looking for the top-level group that
         	// contains the node and then seeing if has a string dataset called program_name
         	TreeNode root = null;
         	TreeNode prev = null;
         	TreeNode n = parNode;
         	while (n != null) {
         		prev = root;
         		root = n;
         		n = n.getParent();
         	}
         	boolean isGDA = false;
 			HObject object = (HObject) ((DefaultMutableTreeNode) prev).getUserObject();
 			if (object instanceof Group) {
 				for (HObject o : ((Group) object).getMemberList()) {
 					if (o instanceof Dataset) {
 						String name = o.getName();
 						if ("program_name".equals(name)) {
 							Dataset d = (Dataset) o;
 							d.setConvertByteToString(true);
 							Object obj = d.getData();
 							if (obj instanceof String[]) {
 								isGDA = ((String[]) obj)[0].startsWith("GDA ");
 							}
 							break;
 						}
 					}
 				}
 			}
         	
         	return HDF5Utils.createDatasetSelection(link, isGDA);
         }
         
         return null;
 	}
 
 	private HDF5Dataset createDataset(final Dataset oo, final String path, HDF5File file) throws Exception {
 
 		final long oid = oo.getOID()[0];
 		HDF5Dataset nd = new HDF5Dataset(oid);
 
 		final Datatype type = ((Dataset) oo).getDatatype();
 		final int dclass = type.getDatatypeClass();
 
 		if (dclass == Datatype.CLASS_COMPOUND) { // special case for compound data types
 			return nd;
 		}
 
 		nd.setTypeName(HDF5Loader.getTypeName(type));
 
 		if (!(oo instanceof H5ScalarDS)) {
 			throw new IllegalArgumentException("Dataset unsupported");
 		}
 
 		H5ScalarDS osd = (H5ScalarDS) oo;
 
 		if (dclass == Datatype.CLASS_STRING) { // special case for strings
 			// This is a kludge for linking to non-hdf5 files
 			// An attribute called "data_filename" is defined and refers to 
 			// an external file and acts like a group
 			osd.setConvertByteToString(true);
 			if (nd.containsAttribute(HDF5Loader.DATA_FILENAME_ATTR_NAME)) { // FIXME how can this ever be true??? (no attributes have been set)
 				// interpret set of strings as the full path names to a group of external files that are stacked together
 				ExternalFiles ef = HDF5Loader.extractExternalFileNames(osd);
 				try{
 					ILazyDataset l = HDF5Loader.createStackedDatasetFromStrings(ef);
 					nd.setDataset(l);
 				} catch(Throwable th){
 					logger.error("Unable to create lazydataset for" + osd, th);
 					nd.setString(ef.getAsText());
 				}
 			} else {
 				nd.setDataset(HDF5Loader.createLazyDataset(file.getHostname(), osd, false));
 				nd.setMaxShape(osd.getMaxDims());
 			}
 		} else {
 			nd.setDataset(HDF5Loader.createLazyDataset(file.getHostname(), osd, false));
 			nd.setMaxShape(osd.getMaxDims());
 		}
 		return nd;
 
 	}
 	
 	private HDF5Group createGroup(final HObject oo, HDF5File file) throws Exception {
 
 		final long oid = oo.getOID()[0];
         H5Group og = (H5Group) oo;
 		HDF5Group ng = new HDF5Group(oid);
 
 
 		List<HObject> members = og.getMemberList();
 		for (HObject h : members) {
 			final String path = h.getPath();
 			final String name = h.getName();
 			ng.addNode(file, path, name, HDF5Loader.copyNode(file, null, h, false));
 		}
 
 		return ng;
 	}
 
 }
