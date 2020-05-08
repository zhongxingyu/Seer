 /*
  * Copyright 2011 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import gda.analysis.io.ScanFileHolderException;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
 import ncsa.hdf.object.Attribute;
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.FileFormat;
 import ncsa.hdf.object.HObject;
 import ncsa.hdf.object.h5.H5File;
 import ncsa.hdf.object.h5.H5Group;
 import ncsa.hdf.object.h5.H5Link;
 import ncsa.hdf.object.h5.H5ScalarDS;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.PositionIterator;
 import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Attribute;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Dataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Group;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
 import uk.ac.gda.monitor.IMonitor;
 
 /**
  * Load HDF5 files using NCSA's Java library
  */
 public class HDF5Loader extends AbstractFileLoader implements IMetaLoader, ISliceLoader {
 	transient protected static final Logger logger = LoggerFactory.getLogger(HDF5Loader.class);
 
 	private String fileName;
 	private boolean keepBitWidth = false;
 
 	private String host = null;
 
 	private static final String DATA_FILENAME_ATTR_NAME = "data_filename";
 
 	public HDF5Loader() {
 		setHost();
 	}
 
 	public HDF5Loader(final String name) {
 		setHost();
 		setFile(name);
 	}
 
 	private void setHost() {
 		try {
 			host = InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException e) {
 			logger.error("Could not find host name", e);
 		}
 	}
 
 	public void setFile(final String name) {
 		fileName = name;
 	}
 
 	@Override
 	public synchronized DataHolder loadFile() throws ScanFileHolderException {
 		return loadFile(null);
 	}
 
 	@Override
 	public synchronized DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
 
 		DataHolder dh = null;
 		dh = new DataHolder();
 
 		HDF5File tree = loadTree(mon);
 		Map<String, ILazyDataset> map = createDatasetsMap(tree.getGroup());
 		
 		for (String key : map.keySet()) {
 			dh.addDataset(key, map.get(key));
 		}
 
 		if (loadMetadata)
 			dh.setMetadata(getMetaData());
 
 		return dh;
 	}
 
 	/**
 	 * @param group - group to investigate
 	 * @return a map of all the data in the group collected recursively
 	 */
 	public static Map<String, ILazyDataset> createDatasetsMap(HDF5Group group) {
 		HashMap<String, ILazyDataset> map = new HashMap<String, ILazyDataset>();
 		addAllDatasetsToMap(group, map);
 		return map;
 	}
 
 	/**
 	 * Adds the data items in a group to the given map recursively
 	 * @param group - group to investigate
 	 * @param map - the map to add items to, to aid the recursive method
 	 */
 	private static void addAllDatasetsToMap(HDF5Group group, Map<String, ILazyDataset> map) {
 
 		for (HDF5NodeLink l : group) {
 			if (l.isDestinationAGroup()) {
 				addAllDatasetsToMap((HDF5Group) l.getDestination(), map);
 			}
 			
 			if (l.isDestinationADataset()) {
 				ILazyDataset dataset = ((HDF5Dataset) l.getDestination()).getDataset();
 				map.put(l.getFullName(), dataset);
 			}
 		}
 	}
 
 	public HDF5File loadTree() throws ScanFileHolderException {
 		return loadTree(null);
 	}
 
 	HDF5File tFile = null;
 
 	public synchronized HDF5File loadTree(IMonitor mon) throws ScanFileHolderException {
 		
 		if (!monitorIncrement(mon)) {
 			return null;
 		}
 
 		File f = new File(fileName);
 		if (!f.exists()) {
 			throw new ScanFileHolderException("File, " + fileName + ", does not exist");
 		}
 
 		HObject root = null;
 		try {
 			final FileFormat hdf5 = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
 			if (hdf5 == null) {
 				throw new ScanFileHolderException("HDF5 support not found: configure the library path to include hdf shared libraries");
 			}
 			final H5File hdf = (H5File) hdf5.createInstance(fileName, FileFormat.READ);
 			if (!hdf.canRead())
 				throw new IllegalArgumentException("Cannot read file");
 
 			final int h = hdf.open();
 			if (h < 0)
 				throw new IllegalArgumentException("Opening file was unsuccessful");
 
 			root = (HObject) ((DefaultMutableTreeNode) hdf.getRootNode()).getUserObject();
 
 			if (!monitorIncrement(mon)) {
 				hdf.close();
 				return null;
 			}
 
 			tFile = copyTree((H5Group) root, keepBitWidth);
 
 			hdf.close();
 		} catch (Exception le) {
			throw new ScanFileHolderException("Problem loading file", le);
 		}
 
 		return tFile;
 	}
 
 	/**
 	 * @param root
 	 * @param keepBitWidth
 	 * @return a copy of a HDF tree
 	 * @throws Exception
 	 */
 	private HDF5File copyTree(final H5Group root, final boolean keepBitWidth) throws Exception {
 		final long oid = root.getOID()[0] + root.getFile().hashCode()*17; // include file name in ID
 		HDF5File f = new HDF5File(oid, fileName);
 		f.setHostname(host);
 
 		f.setGroup((HDF5Group) copyNode(f, new HashMap<Long, HDF5Node>(), root, keepBitWidth));
 		return f;
 	}
 
 	private static final String NAPIMOUNT = "napimount";
 	private static final String NAPISCHEME = "nxfile";
 
 	// return true when attributes contain a NAPI mount - dodgy external linking for HDF5 version < 1.8
 	private static boolean copyAttributes(final HDF5Node nn, final HObject oo) {
 		boolean hasNAPIMount = false;
 
 		try {
 			if (oo.hasAttribute()) {
 				@SuppressWarnings("unchecked")
 				final List<Attribute> attributes = oo.getMetadata();
 				final String fname = (oo instanceof H5Group) && ((H5Group) oo).isRoot() ? HDF5File.ROOT : oo
 						.getFullName();
 				for (Attribute a : attributes) {
 					HDF5Attribute h = new HDF5Attribute(fname, a.getName(), a.getValue(), a.isUnsigned());
 					h.setTypeName(getTypeName(a.getType()));
 					nn.addAttribute(h);
 					if (a.getName().equals(NAPIMOUNT)) {
 						hasNAPIMount = true;
 					}
 				}
 			}
 		} catch (Exception e) {
 			logger.warn("Problem with attributes on {}: {}", oo.getFullName(), e.getMessage());
 		}
 		return hasNAPIMount;
 	}
 
 	// get external node
 	private static HDF5Node getExternalNode(final HashMap<Long, HDF5Node> pool, final String lpath, String node, final boolean keepBitWidth) throws Exception {
 		HDF5Node nn = null;
 		final H5File hdf = (H5File) FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5).createInstance(lpath, FileFormat.READ);
 		if (!hdf.canRead())
 			throw new IllegalArgumentException("Cannot read file");
 
 		final int lid = hdf.open();
 		if (lid < 0)
 			throw new IllegalArgumentException("Opening file was unsuccessful");
 
 		if (!node.startsWith(HDF5File.ROOT)) {
 			node = HDF5File.ROOT + node;
 		}
 
 		try {
 			HObject lobj = findObject(hdf, node);
 			if (lobj != null) {
 				final long oid = lobj.getOID()[0] + hdf.getAbsolutePath().hashCode()*17; // include file name in ID
 				nn = copyNode(new HDF5File(oid, lpath), pool, lobj, keepBitWidth);
 			}
 		} catch (Exception e) {
 			throw new ScanFileHolderException("Problem loading file", e);
 		} finally {
 			hdf.close();
 		}
 		return nn;
 	}
 
 	// retrieve external file link
 	private static HDF5Node copyNAPIMountNode(final HDF5File file, final HashMap<Long, HDF5Node> pool, final String link, final boolean keepBitWidth) throws Exception {
 		final URI ulink = new URI(link);
 		HDF5Node nn = null;
 		if (ulink.getScheme().equals(NAPISCHEME)) {
 			String lpath = ulink.getPath();
 			String ltarget = ulink.getFragment();
 			File f = new File(lpath);
 			if (!f.exists()) {
 				logger.debug("File, {}, does not exist!", lpath);
 
 				// see if linked file in same directory
 				String dpath = file.getFilename();
 				dpath = dpath.substring(0, dpath.lastIndexOf(HDF5Node.SEPARATOR)+1) + f.getName();
 				f = new File(dpath);
 				if (!f.exists()) {
 					throw new ScanFileHolderException("File, " + lpath + ", does not exist");
 				}
 				lpath = f.getAbsolutePath();
 			}
 			nn = getExternalNode(pool, lpath, ltarget, keepBitWidth);
 			if (nn == null)
 				logger.warn("Could not find external node: {}", ltarget);
 		} else {
 			System.err.println("Wrong scheme: " + ulink.getScheme());
 		}
 
 		return nn;
 	}
 
 	//	private static final String LINKSEPARATOR = HDF5File.HOST_SEPARATOR + HDF5File.FILE_STARTER;
 
 	// depth-first recursion that copies object and return node
 	private static HDF5Node copyNode(final HDF5File file, final HashMap<Long, HDF5Node> pool, final HObject oo, final boolean keepBitWidth) throws Exception {
 		if (oo instanceof H5Link) {
 			// a link which cannot be resolved remains a link
 			H5Link ol = (H5Link) oo;
 			ol.getMetadata(); // API quirk needed to populate link target
 			final String target = ol.getLinkTargetObjName();
 			if (target == null) {
 				throw new IllegalArgumentException("No link target defined in link object");
 			}
 
 			throw new IllegalArgumentException("Link target cannot be resolved: " + target);
 //			String lfile = null;
 //			String link = null;
 //			if (target.contains(LINKSEPARATOR)) { // external link?
 //				int i = target.indexOf(LINKSEPARATOR);
 //				lfile = target.substring(0, i);
 //				link = target.substring(i + LINKSEPARATOR.length(), target.length());
 //			} else {
 //				if (target.startsWith(HDF5File.FILE_STARTER))
 //					link = target.substring(HDF5File.FILE_STARTER.length());
 //				else
 //					link = target;
 //			}
 //
 //			if (lfile == null || file.getFilename().equals(lfile)) {
 //				// link is internal, i.e. soft
 //				return new HDF5SymLink(file, link);
 //			}
 //
 //			if (!lfile.startsWith(HDF5File.ROOT)) { // linked file path is relative so make full path
 //				String dir = file.getFilename();
 //				lfile = dir.substring(0, dir.lastIndexOf(HDF5Node.SEPARATOR) + 1) + lfile;
 //			}
 //			HDF5File linkedFile = new HDF5File(lfile);
 //			linkedFile.setHostname(file.getHostname());
 //			return new HDF5SymLink(linkedFile, link);
 //
 //			return getExternalNode(pool, lfile, link, keepBitWidth);
 		}
 
 		final Long oid = oo.getOID()[0] + file.getFilename().hashCode()*17; // include file name in ID
 
 		if (oo instanceof Dataset) {
 			if (pool.containsKey(oid)) {
 				HDF5Node p = pool.get(oid);
 				if (!(p instanceof HDF5Dataset)) {
 					throw new IllegalStateException("Matching pooled node is not a dataset");
 				}
 				return p;
 			}
 
 			HDF5Dataset nd = new HDF5Dataset(oid);
 			if (copyAttributes(nd, oo)) {
 				final String link = nd.getAttribute(NAPIMOUNT).getFirstElement();
 				return copyNAPIMountNode(file, pool, link, keepBitWidth);
 			}
 
 			final Datatype type = ((Dataset) oo).getDatatype();
 			final int dclass = type.getDatatypeClass();
 
 			if (dclass == Datatype.CLASS_COMPOUND) { // special case for compound data types
 				return nd;
 			}
 
 			nd.setTypeName(getTypeName(type));
 
 			if (!(oo instanceof H5ScalarDS)) {
 				throw new IllegalArgumentException("Dataset unsupported");
 			}
 
 			H5ScalarDS osd = (H5ScalarDS) oo;
 			if (dclass == Datatype.CLASS_STRING) { // special case for strings
 				// This is a kludge for linking to non-hdf5 files
 				// An attribute called "data_filename" is defined and refers to 
 				// an external file and acts like a group
 				osd.setConvertByteToString(true);
 				if (nd.containsAttribute(DATA_FILENAME_ATTR_NAME)) {
 					// interpret set of strings as the full path names to a group of external files that are stacked together
 					ExternalFiles ef = extractExternalFileNames(osd);
 					try{
 						ILazyDataset l = createStackedDatasetFromStrings(ef);
 						nd.setDataset(l);
 					} catch(Throwable th){
 						logger.error("Unable to create lazydataset for" + osd, th);
 						nd.setString(ef.getAsText());
 					}
 				} else {
 					nd.setDataset(createLazyDataset(file.getHostname(), osd, keepBitWidth));
 					nd.setMaxShape(osd.getMaxDims());
 				}
 			} else {
 				nd.setDataset(createLazyDataset(file.getHostname(), osd, keepBitWidth));
 				nd.setMaxShape(osd.getMaxDims());
 			}
 			pool.put(oid, nd);
 			return nd;
 		} else if (oo instanceof H5Group) {
 			if (pool.containsKey(oid)) {
 				HDF5Node p = pool.get(oid);
 				if (!(p instanceof HDF5Group)) {
 					throw new IllegalStateException("Matching pooled node is not a group");
 				}
 				return p;
 			}
 			H5Group og = (H5Group) oo;
 			HDF5Group ng = new HDF5Group(oid);
 			if (copyAttributes(ng, og)) {
 				final String link = ng.getAttribute(NAPIMOUNT).getFirstElement();
 				return copyNAPIMountNode(file, pool, link, keepBitWidth);
 			}
 
 			List<HObject> members = og.getMemberList();
 			for (HObject h : members) {
 				final String path = h.getPath();
 				final String name = h.getName();
 				ng.addNode(path, name, copyNode(file, pool, h, keepBitWidth));
 			}
 
 			pool.put(oid, ng);
 			return ng;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Translate between data type and dataset type
 	 * @param dclass data type class
 	 * @param dsize data type item size in bytes
 	 * @return dataset type
 	 */
 	private static int getDtype(final int dclass, final int dsize) {
 		switch (dclass) {
 		case Datatype.CLASS_STRING:
 			break;
 		case Datatype.CLASS_CHAR:
 		case Datatype.CLASS_INTEGER:
 			switch (dsize) {
 			case 1:
 				return AbstractDataset.INT8;
 			case 2:
 				return AbstractDataset.INT16;
 			case 4:
 				return AbstractDataset.INT32;
 			case 8:
 				return AbstractDataset.INT64;
 			}
 			break;
 		case Datatype.CLASS_FLOAT:
 			switch (dsize) {
 			case 4:
 				return AbstractDataset.FLOAT32;
 			case 8:
 				return AbstractDataset.FLOAT64;
 			}
 			break;
 		}
 		return -1;
 	}
 
 	/**
 	 * @param t
 	 * @return a string to represent that data type
 	 */
 	public static String getTypeName(final Datatype t) {
 		final int dclass = t.getDatatypeClass();
 		final int dsize = t.getDatatypeSize();
 		switch (dclass) {
 		case Datatype.CLASS_CHAR:
 		case Datatype.CLASS_INTEGER:
 			String header = t.isUnsigned() ? "U" : "";
 			switch (dsize) {
 			case 1:
 				return header + "INT8";
 			case 2:
 				return header + "INT16";
 			case 4:
 				return header + "INT32";
 			case 8:
 				return header + "INT64";
 			}
 			break;
 		case Datatype.CLASS_FLOAT:
 			switch (dsize) {
 			case 4:
 				return "FLOAT32";
 			case 8:
 				return "FLOAT64";
 			}
 			break;
 		case Datatype.CLASS_STRING:
 			return "STRING";
 		}
 		return t.getDatatypeDescription();
 	}
 
 	/**
 	 * Create a dataset from the given data object
 	 * @param data
 	 * @param shape
 	 * @param dtype
 	 * @param extend true dataset for unsigned types 
 	 * @return dataset
 	 */
 	public static AbstractDataset createDataset(final Object data, final int[] shape, final int dtype,
 			final boolean extend) {
 		AbstractDataset ds = null;
 		switch (dtype) {
 		case AbstractDataset.FLOAT32:
 			float[] fData = (float[]) data;
 			ds = new FloatDataset(fData, shape);
 			break;
 		case AbstractDataset.FLOAT64:
 			double[] dData = (double[]) data;
 			ds = new DoubleDataset(dData, shape);
 			break;
 		case AbstractDataset.INT8:
 			byte[] bData = (byte[]) data;
 			ds = new ByteDataset(bData, shape);
 			break;
 		case AbstractDataset.INT16:
 			short[] sData = (short[]) data;
 			ds = new ShortDataset(sData, shape);
 			break;
 		case AbstractDataset.INT32:
 			int[] iData = (int[]) data;
 			ds = new IntegerDataset(iData, shape);
 			break;
 		case AbstractDataset.INT64:
 			long[] lData = (long[]) data;
 			ds = new LongDataset(lData, shape);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown or unsupported dataset type");
 		}
 
 		if (extend) {
 			switch (dtype) {
 			case AbstractDataset.INT32:
 				ds = new LongDataset(ds);
 				DatasetUtils.unwrapUnsigned(ds, 32);
 				break;
 			case AbstractDataset.INT16:
 				ds = new IntegerDataset(ds);
 				DatasetUtils.unwrapUnsigned(ds, 16);
 				break;
 			case AbstractDataset.INT8:
 				ds = new ShortDataset(ds);
 				DatasetUtils.unwrapUnsigned(ds, 8);
 				break;
 			}
 		}
 		return ds;
 	}
 
 	private static ILazyDataset createLazyDataset(final String host, final H5ScalarDS osd, final boolean keepBitWidth) throws Exception {
 		long[] dims = osd.getDims();
 		if (dims == null) {
 			osd.init();
 			dims = osd.getDims();
 		}
 		if (dims == null) {
 			System.err.println("Problem getting dimensions!");
 			osd.init();
 			dims = osd.getDims();
 		}
 		final int[] trueShape = new int[dims.length];
 		for (int i = 0; i < dims.length; i++) {
 			long d = dims[i];
 			if (d > Integer.MAX_VALUE) {
 				throw new IllegalArgumentException("Dimension larger than ints");
 			}
 			trueShape[i] = (int) d;
 		}
 
 		if (trueShape.length == 1 && trueShape[0] == 1) { // special case for single values
 			final AbstractDataset d = AbstractDataset.array(osd.read());
 			d.setName(osd.getName());
 			return d;
 		}
 
 		final String filePath = osd.getFile();
 		final String nodePath = osd.getFullName();
 		final String name = osd.getName();
 		final Datatype type = osd.getDatatype();
 		final boolean extendUnsigned = !keepBitWidth && type.isUnsigned();
 		final int dtype = getDtype(type.getDatatypeClass(), type.getDatatypeSize());
 
 		ILazyLoader l = new ILazyLoader() {
 			@Override
 			public boolean isFileReadable() {
 				try {
 					if (host != null && host.length() > 0 && !host.equals(InetAddress.getLocalHost().getHostName()))
 						return false;
 				} catch (UnknownHostException e) {
 					logger.warn("Problem finding local host so ignoring check", e);
 				}
 				return new File(filePath).canRead();
 			}
 
 			@Override
 			public String toString() {
 				return filePath + ":" + nodePath;
 			}
 
 			@Override
 			public AbstractDataset getDataset(IMonitor mon, int[] shape, int[] start, int[] stop, int[] step)
 					throws ScanFileHolderException {
 				final int rank = shape.length;
 				int[] lstart, lstop, lstep;
 
 				if (step == null) {
 					lstep = new int[rank];
 					for (int i = 0; i < rank; i++) {
 						lstep[i] = 1;
 					}
 				} else {
 					lstep = step;
 				}
 
 				if (start == null) {
 					lstart = new int[rank];
 				} else {
 					lstart = start;
 				}
 
 				if (stop == null) {
 					lstop = new int[rank];
 				} else {
 					lstop = stop;
 				}
 
 				int[] newShape = AbstractDataset.checkSlice(shape, start, stop, lstart, lstop, lstep);
 
 				AbstractDataset d = null;
 				try {
 					if (!Arrays.equals(trueShape, shape)) {
 						// if shape was squeezed then need to translate to true slice
 						final int trank = trueShape.length;
 						int[] tstart = new int[trank];
 						int[] tsize = new int[trank];
 						int[] tstep = new int[trank];
 
 						int j = 0;
 						for (int i = 0; i < trank; i++) {
 							if (trueShape[i] == 1) {
 								tstart[i] = 0;
 								tsize[i] = 1;
 								tstep[i] = 1;
 							} else {
 								tstart[i] = lstart[j];
 								tsize[i] = newShape[j];
 								tstep[i] = lstep[j];
 								j++;
 							}
 						}
 
 						d = loadData(filePath, nodePath, tstart, tsize, tstep, dtype, extendUnsigned);
 						d.setShape(newShape); // squeeze shape back
 					} else {
 						d = loadData(filePath, nodePath, lstart, newShape, lstep, dtype, extendUnsigned);
 					}
 					if (d != null) {
 						d.setName(name);
 					}
 				} catch (Exception e) {
 					throw new ScanFileHolderException("Problem with HDF library", e);
 				}
 				return d;
 			}
 		};
 
 		return new LazyDataset(name, dtype, trueShape.clone(), l);
 	}
 
 	private static ILazyDataset createStackedDatasetFromStrings(ExternalFiles ef) throws OutOfMemoryError, Exception {
 		//remove final dimension as that is for the characters of the strings
 		//shape here is for the actual filenames
 		
 		ImageStackLoaderEx loader = new ImageStackLoaderEx(ef.shape, ef.files);
 		return new LazyDataset("file_name", loader.getDtype(), loader.getShape(), loader);
 	}
 
 	private static ExternalFiles extractExternalFileNames(H5ScalarDS sds) throws Exception {
 		sds.init();
 		final long[] dshape = sds.getDims();
 		final long[] dstart = sds.getStartDims();
 		final long[] dstride = sds.getStride();
 		final long[] dsize = sds.getSelectedDims();
 		dstride[0] = 1;
 		dsize[0] = 1;
 
 		int length = 1;
 		final int[] shape = new int[dshape.length];
 		for (int i = 0; i < dshape.length; i++) {
 			int s = (int) dshape[i];
 			shape[i] = s;
 			length *= s;
 		}
 		final String[] files = new String[length];
 		PositionIterator iter = new PositionIterator(shape);
 		final int[] pos = iter.getPos();
 		int index = 0;
 		while (iter.hasNext()) {
 			for (int i = 0; i < shape.length; i++) {
 				dstart[i] = pos[i];
 			}
 			//DO NOT CHANGE THE FOLLOWING UNLESS YOU HAVE A TEST TO PROVE IT IS NOT WORKING. 
 			sds.clear();
 			files[index++] = ((String[]) sds.getData())[0];
 		}
 		ExternalFiles ef= new ExternalFiles();
 		//reduce shape as we have removed the filenames
 		int[] squeezeShape = java.util.Arrays.copyOf(shape, shape.length-1);
 		ef.shape = squeezeShape;
 		ef.files = files;
 		return ef;
 	}
 
 	private static HObject findObject(FileFormat file, String path) throws Exception {
 		if (file == null || path == null)
 			return null;
 		if (!path.endsWith(HDF5Node.SEPARATOR))
 			path = path + HDF5Node.SEPARATOR;
 		return file.get(path);
 		//		DefaultMutableTreeNode theRoot = (DefaultMutableTreeNode) file.getRootNode();
 		//		if (theRoot == null)
 		//			return null;
 		//		else if (path.equals(HDF5File.ROOT))
 		//			return (HObject) theRoot.getUserObject();
 		//		@SuppressWarnings("unchecked")
 		//		Enumeration<DefaultMutableTreeNode> local_enum = theRoot.breadthFirstEnumeration();
 		//		DefaultMutableTreeNode theNode = null;
 		//		HObject theObj = null;
 		//		while (local_enum.hasMoreElements()) {
 		//			theNode = local_enum.nextElement();
 		//			theObj = (HObject) theNode.getUserObject();
 		//			String fullPath = theObj.getFullName() + HDF5Node.SEPARATOR;
 		//			if (path.equals(fullPath))
 		//				return theObj;
 		//		}
 		//		return null;
 	}
 
 	@SuppressWarnings("null")
 	private static synchronized AbstractDataset loadData(final String fileName, final String node, final int[] start, final int[] count,
 			final int[] step, final int dtype, final boolean extend) throws ScanFileHolderException {
 		AbstractDataset data = null;
 		try {
 			final FileFormat hdf5 = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
 			final H5File hdf = (H5File) hdf5.createInstance(fileName, FileFormat.READ);
 			if (!hdf.canRead())
 				throw new IllegalArgumentException("Cannot read file");
 
 			final int h = hdf.open();
 			if (h < 0)
 				throw new IllegalArgumentException("Opening file was unsuccessful");
 
 			HObject obj = findObject(hdf, node);
 
 			if (obj == null)
 				throw new IllegalArgumentException("Node not found: " + node);
 
 			if (obj instanceof H5ScalarDS) {
 				final H5ScalarDS dobj = (H5ScalarDS) obj;
 				dobj.getMetadata(); // also runs init()
 				dobj.clear(); // don't cache
 				final long[] sstart = dobj.getStartDims();   // source start
 				final long[] sstride = dobj.getStride();     // source steps
 				final long[] dsize = dobj.getSelectedDims(); // destination size
 				final int rank = sstart.length;
 				final int type;
 				if (dtype < 0) {
 					final Datatype datatype = dobj.getDatatype();
 					type = getDtype(datatype.getDatatypeClass(), datatype.getDatatypeSize());
 				} else {
 					type = dtype;
 				}
 
 				for (int i = 0; i < rank; i++) {
 					sstart[i] = start[i];
 					sstride[i] = step[i];
 					dsize[i] = count[i];
 				}
 
 				final long[] schunk = dobj.getChunkSize(); // source chunking
 
 				final boolean all;
 				if (schunk == null) {
 					all = true;
 				} else {
 					int j = rank - 1; // find last chunked dimension that is sliced across
 					while (j >= 0) {
 						if (schunk[j] > 1 && dsize[j] <= 1)
 							break;
 						j--;
 					}
 					all = j < 0;
 				}
 
 				if (all) {
 					try {
 						data = createDataset(dobj.read(), count, type, extend);
 					} catch (HDF5Exception e) {
 						throw new ScanFileHolderException("Problem reading dataset from HDF5 file", e);
 					}
 				} else {
 					// read in many split chunks
 					final boolean[] isSplit = new boolean[rank];
 					final long[] send = new long[rank];
 					int s = 1;
 					for (int i = 0; i < rank; i++) {
 						send[i] = sstart[i] + count[i]*step[i];
 						isSplit[i] = (schunk[i] <= 1 || dsize[i] > 1);
 						if (isSplit[i]) {
 							dsize[i] = 1;
 						} else {
 							s *= dsize[i];
 						}
 					}
 					if (s == 1) { // if just single point then bulk up request 
 						for (int i = rank - 1; i >= 0; i--) {
 							int l = count[i];
 							if (l > 1) {
 								dsize[i] = l;
 								isSplit[i] = false;
 								break;
 							}
 						}
 					}
 					final List<Integer> notSplit = new ArrayList<Integer>();
 					for (int i = 0; i < rank; i++) {
 						if (!isSplit[i])
 							notSplit.add(i);
 					}
 					final int[] axes = new int[notSplit.size()];
 					for (int i = 0; i < axes.length; i++) {
 						axes[i] = notSplit.get(i);
 					}
 					data = AbstractDataset.zeros(count, type);
 
 					PositionIterator it = data.getPositionIterator(axes);
 					final int[] pos = it.getPos();
 					final boolean[] hit = it.getOmit();
 					while (it.hasNext()) {
 						data.setItemsOnAxes(pos, hit, dobj.read());
 						int j = rank - 1;
 						for (; j >= 0; j--) {
 							if (isSplit[j]) {
 								sstart[j] += sstride[j];
 								if (sstart[j] >= send[j]) {
 									sstart[j] = start[j];
 								} else {
 									break;
 								}
 							}
 						}
 						if (j == -1)
 							break;
 					}
 
 					if (extend) {
 						switch (type) {
 						case AbstractDataset.INT32:
 							data = new LongDataset(data);
 							DatasetUtils.unwrapUnsigned(data, 32);
 							break;
 						case AbstractDataset.INT16:
 							data = new IntegerDataset(data);
 							DatasetUtils.unwrapUnsigned(data, 16);
 							break;
 						case AbstractDataset.INT8:
 							data = new ShortDataset(data);
 							DatasetUtils.unwrapUnsigned(data, 8);
 							break;
 						}
 					}
 				}
 			} else {
 				throw new ScanFileHolderException("Cannot handle (non-scalar) dataset type");
 			}
 			hdf.close();
 		} catch (Exception le) {
 			throw new ScanFileHolderException("Problem loading file", le);
 		}
 
 		return data;
 	}
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 		loadTree(mon);
 	}
 
 	@Override
 	public IMetaData getMetaData() {
 		return createMetaData(tFile);
 	}
 
 	public static IMetaData createMetaData(final HDF5File tree) {
 		if (tree == null)
 			return null;
 
 		final List<String> metanames = createMetaNames(tree.getNodeLink());
 
 		return new MetaDataAdapter() {
 			
 			@Override
 			public Collection<String> getMetaNames() throws Exception {
 				return Collections.unmodifiableCollection(metanames);
 			}
 
 			@Override
 			public String getMetaValue(String key) throws Exception {
 				if (!metanames.contains(key))
 					return null;
 				
 				HDF5Node node = tree.findNodeLink(key).getDestination();
 				if (key.contains(HDF5Node.ATTRIBUTE)) {
 					return node.getAttribute(key.substring(key.indexOf(HDF5Node.ATTRIBUTE) + 1)).getFirstElement();
 				}
 
 				AbstractDataset a = (AbstractDataset) ((HDF5Dataset) node).getDataset();
 				return a.getRank() == 0 ? a.getString() : a.getString(0);
 			}
 		};
 	}
 
 	private static List<String> createMetaNames(HDF5NodeLink link) {
 		List<String> list = new ArrayList<String>();
 		addMetadataToList(link, list);
 		return list;
 	}
 
 	/**
 	 * Adds the attributes and scalar dataset items in a node to the given list recursively
 	 * @param link - link to node to investigate
 	 * @param list - the list to add items to, to aid the recursive method
 	 */
 	private static void addMetadataToList(HDF5NodeLink link, List<String> list) {
 		HDF5Node node = link.getDestination();
 		Iterator<String> iter = node.getAttributeNameIterator();
 		String name = link.getFullName() + HDF5Node.ATTRIBUTE;
 		while (iter.hasNext()) {
 			list.add(name + iter.next());
 		}
 
 		if (node instanceof HDF5Group) {
 			for (HDF5NodeLink l : (HDF5Group) node) {
 				addMetadataToList(l, list);
 
 				if (l.isDestinationADataset()) {
 					HDF5Node n = l.getDestination();
 					ILazyDataset dataset = ((HDF5Dataset) n).getDataset();
 					if (dataset instanceof AbstractDataset) { // scalar dataset
 						list.add(l.getFullName());
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public AbstractDataset slice(SliceObject object, IMonitor mon) throws Exception {
 		final int[] start = object.getSliceStart();
 		final int[] stop = object.getSliceStop();
 		final int[] step = object.getSliceStep();
 		int[] lstart, lstop, lstep;
 		int rank;
 		if (start != null)
 			rank = start.length;
 		else if (stop != null)
 			rank = stop.length;
 		else if (step != null)
 			rank = step.length;
 		else
 			throw new IllegalArgumentException("Slice object does not have any info about rank");
 
 		if (step == null) {
 			lstep = new int[rank];
 			for (int i = 0; i < rank; i++) {
 				lstep[i] = 1;
 			}
 		} else {
 			lstep = step;
 		}
 
 		if (start == null) {
 			lstart = new int[rank];
 		} else {
 			lstart = start;
 		}
 
 		if (stop == null) {
 			lstop = object.getSlicedShape();
 			if (lstop == null)
 				lstop = object.getFullShape();
 		} else {
 			lstop = stop;
 		}
 
 		if (lstop == null)
 			throw new IllegalArgumentException("Slice object does not have any info about stop or shape");
 
 		int[] newShape = new int[rank];
 
 		for (int i = 0; i < rank; i++) {
 			if (lstep[i] > 0) {
 				newShape[i] = (lstop[i] - lstart[i] - 1) / lstep[i] + 1;
 			} else {
 				newShape[i] = (lstop[i] - lstart[i] + 1) / lstep[i] + 1;
 			}
 		}
 		return loadData(object.getPath(), object.getName(), lstart, newShape, lstep, -1, true);
 	}
 }
 
 class ExternalFiles {
 	int[] shape;
 	String[] files;
 
 	String getAsText() {
 		if (files == null)
 			return "";
 		StringBuilder sBuilder = new StringBuilder();
 		boolean addCR = false;
 		for (String filename : files) {
 			if (addCR) {
 				sBuilder.append("\n");
 			}
 			sBuilder.append(filename);
 			addCR = true;
 		}
 		return sBuilder.toString();
 	}
 }
