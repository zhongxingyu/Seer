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
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.locks.ReentrantLock;
 
 import ncsa.hdf.hdf5lib.H5;
 import ncsa.hdf.hdf5lib.HDF5Constants;
 import ncsa.hdf.hdf5lib.HDFNativeData;
 import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
 import ncsa.hdf.hdf5lib.structs.H5G_info_t;
 import ncsa.hdf.object.Attribute;
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.FileFormat;
 import ncsa.hdf.object.HObject;
 import ncsa.hdf.object.h5.H5Datatype;
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
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5SymLink;
 import uk.ac.gda.monitor.IMonitor;
 
 /**
  * Load HDF5 files using NCSA's Java library
  */
 public class HDF5Loader extends AbstractFileLoader implements IMetaLoader, ISliceLoader {
 	protected static final Logger logger = LoggerFactory.getLogger(HDF5Loader.class);
 
 	private static Map<String, ReentrantLock> openFiles = new ConcurrentHashMap<String, ReentrantLock>();
 
 	private static ReentrantLock acquireLock(String file) {
 		ReentrantLock lock = openFiles.get(file);
 		if (lock == null) {
 			lock = new ReentrantLock();
 			openFiles.put(file, lock);
 		}
 		lock.lock();
 		return lock;
 	}
 
 	private static void releaseLock(ReentrantLock lock) {
 		lock.unlock();
 		if (lock.getHoldCount() == 0) {
 			if (openFiles.containsValue(lock)) {
 				for (Entry<String, ReentrantLock> e : openFiles.entrySet()) {
 					if (e.getValue().equals(lock)) {
 						openFiles.remove(e.getKey());
 					}
 				}
 			}
 		}
 	}
 
 	private String fileName;
 	private boolean keepBitWidth = false;
 
 	private String host = null;
 
 	public static final String DATA_FILENAME_ATTR_NAME = "data_filename";
 
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
 	public DataHolder loadFile() throws ScanFileHolderException {
 		return loadFile(null);
 	}
 
 	@Override
 	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
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
 
 	public HDF5File loadTree(IMonitor mon) throws ScanFileHolderException {
 		
 		if (!monitorIncrement(mon)) {
 			return null;
 		}
 
 		File f = new File(fileName);
 		if (!f.exists()) {
 			throw new ScanFileHolderException("File, " + fileName + ", does not exist");
 		}
 
 		ReentrantLock lock = acquireLock(fileName);
 		int fid = -1;
 		try {
 			fid = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
 
 			if (!monitorIncrement(mon)) {
 				try {
 					H5.H5Fclose(fid);
 				} catch (Exception ex) {
 				}
 				return null;
 			}
 
 			tFile = createTree(fid, keepBitWidth);
 		} catch (Exception le) {
 			throw new ScanFileHolderException("Problem loading file: " + fileName, le);
 		} finally {
 			try {
 				H5.H5Fclose(fid);
 			} catch (Exception e) {
 			}
 			releaseLock(lock);
 		}
 
 		return tFile;
 	}
 
 	/**
 	 * @param fid file ID
 	 * @param keepBitWidth
 	 * @return a HDF5 tree
 	 * @throws Exception
 	 */
 	private HDF5File createTree(final int fid, final boolean keepBitWidth) throws Exception {
 		final long oid = fileName.hashCode(); // include file name in ID
 		HDF5File f = new HDF5File(oid, fileName);
 		f.setHostname(host);
 
 		HDF5Group g = (HDF5Group) createGroup(fid, f, new HashMap<Long, HDF5Node>(), HDF5File.ROOT, keepBitWidth);
 		if (g == null) {
 			throw new ScanFileHolderException("Could not copy root group");
 		}
 		f.setGroup(g);
 		return f;
 	}
 
 	private static int LIMIT = 10240;
 
 	/**
 	 * Create a group from given location ID
 	 * @param lid location ID
 	 * @param pool
 	 * @param name of group (full path)
 	 * @param keepBitWidth
 	 * @return node
 	 * @throws Exception
 	 */
 	private static HDF5Node createGroup(final int lid, final HDF5File f, final HashMap<Long, HDF5Node> pool, final String name, final boolean keepBitWidth) throws Exception {
 		int nelems = 0;
 		int gid = -1;
 
 		try {
 			gid = H5.H5Gopen(lid, name, HDF5Constants.H5P_DEFAULT);
 			H5G_info_t info = H5.H5Gget_info(gid);
 			nelems = (int) info.nlinks;
 		} catch (HDF5Exception ex) {
 			throw new ScanFileHolderException("Could not open group", ex);
 		}
 
 		byte[] idbuf = null;
 		long oid = -1;
 		try {
 			idbuf = H5.H5Rcreate(lid, name, HDF5Constants.H5R_OBJECT, -1);
 			oid = HDFNativeData.byteToLong(idbuf, 0);
 		} catch (HDF5Exception ex) {
 			throw new ScanFileHolderException("Could not find group reference", ex);
 		}
 
 		final HDF5Group group = new HDF5Group(oid);
 		if (pool != null)
 			pool.put(oid, group);
 		if (copyAttributes(name, group, gid)) {
 			final String link = group.getAttribute(NAPIMOUNT).getFirstElement();
 			return copyNAPIMountNode(f, pool, link, keepBitWidth);
 		}
 
 		if (nelems <= 0) {
 			try {
 				H5.H5Gclose(gid);
 			} catch (HDF5Exception ex) {
 				return null;
 			}
 			return group;
 		}
 
 		int[] oTypes = new int[nelems];
 		int[] lTypes = new int[nelems];
 		long[] oids = new long[nelems];
 		String[] oNames = new String[nelems];
 		try {
 			H5.H5Gget_obj_info_all(lid, name, oNames, oTypes, lTypes, oids, HDF5Constants.H5_INDEX_NAME);
 		} catch (HDF5Exception ex) {
 			return null;
 		}
 
 		if (nelems > LIMIT) {
 			logger.warn("Number of members in group {} exceed limit ({} > {}). Only reading up to limit", new Object[] {name, nelems, LIMIT});
 			nelems = LIMIT;
 		}
 
 		String oname;
 		int otype;
 		int ltype;
 
 		// Iterate through the file to see members of the group
 		for (int i = 0; i < nelems; i++) {
 			oname = oNames[i];
 			if (oname == null) {
 				continue;
 			}
 			otype = oTypes[i];
 			ltype = lTypes[i];
 			oid = oids[i];
 
 			if (ltype == HDF5Constants.H5L_TYPE_HARD) {
 				if (otype == HDF5Constants.H5O_TYPE_GROUP) {
 //					System.err.println("G: " + oname);
 					if (oid >= 0 && pool != null && pool.containsKey(oid)) {
 						HDF5Node p = pool.get(oid);
 						if (!(p instanceof HDF5Group)) {
 							throw new IllegalStateException("Matching pooled node is not a group");
 						}
 						group.addNode(name, oname, p);
 						continue;
 					}
 
 					group.addNode(name, oname, createGroup(gid, f, pool, name + oname + HDF5Node.SEPARATOR, keepBitWidth));
 				} else if (otype == HDF5Constants.H5O_TYPE_DATASET) {
 //					System.err.println("D: " + oname);
 					if (oid >= 0 && pool != null && pool.containsKey(oid)) {
 						HDF5Node p = pool.get(oid);
 						if (!(p instanceof HDF5Dataset)) {
 							throw new IllegalStateException("Matching pooled node is not a dataset");
 						}
 						group.addNode(name, oname, p);
 						continue;
 					}
 
 					int did = -1, tid = -1, tclass = -1;
 					try {
 						did = H5.H5Dopen(gid, oname, HDF5Constants.H5P_DEFAULT);
 						tid = H5.H5Dget_type(did);
 
 						tclass = H5.H5Tget_class(tid);
 						if (tclass == HDF5Constants.H5T_ARRAY || tclass == HDF5Constants.H5T_VLEN) {
 							// for ARRAY, the type is determined by the base type
 							int btid = H5.H5Tget_super(tid);
 							tclass = H5.H5Tget_class(btid);
 							try {
 								H5.H5Tclose(btid);
 							} catch (HDF5Exception ex) {
 							}
 						}
 						if (tclass == HDF5Constants.H5T_COMPOUND) {
 							logger.error("Compound dataset not supported"); // TODO
 						} else {
 							// create a new scalar dataset
 							HDF5Dataset d = new HDF5Dataset(oid);
 							if (copyAttributes(name, d, did)) {
								final String link = group.getAttribute(NAPIMOUNT).getFirstElement();
 								group.addDataset(name, oname,
 										(HDF5Dataset) copyNAPIMountNode(f, pool, link, keepBitWidth));
 							} else {
 								ILazyDataset ld = createLazyDataset(f, name + oname, oname, did, tid,
 										keepBitWidth, d.containsAttribute(DATA_FILENAME_ATTR_NAME));
 
 								if (ld == null) {
 									logger.error("Could not create a lazy dataset {} from {}", oname, name);
 									continue;
 								}
 								d.setDataset(ld);
 								group.addDataset(name, oname, d);
 							}
 							if (pool != null)
 								pool.put(oid, d);
 						}
 					} catch (HDF5Exception ex) {
 						logger.error("Could not open dataset", ex);
 					} finally {
 						try {
 							H5.H5Tclose(tid);
 						} catch (HDF5Exception ex) {
 						}
 						try {
 							H5.H5Dclose(did);
 						} catch (HDF5Exception ex) {
 						}
 					}
 
 
 				} else if (otype == HDF5Constants.H5O_TYPE_NAMED_DATATYPE) {
 					logger.error("Named datatype not supported"); // TODO
 				}
 
 			} else if (ltype == HDF5Constants.H5L_TYPE_SOFT) {
 //				System.err.println("S: " + oname);
 				String[] linkName = new String[1];
 				int t = H5.H5Lget_val(gid, oname, linkName, HDF5Constants.H5P_DEFAULT);
 				if (t < 0) {
 					logger.warn("Could not get value of link");
 					continue;
 				}
 //				System.err.println("  -> " + linkName[0]);
 				HDF5SymLink slink = new HDF5SymLink(oid, f, linkName[0]);
 				group.addNode(name, oname, slink);
 			} else if (ltype == HDF5Constants.H5L_TYPE_EXTERNAL) {
 //				System.err.println("E: " + oname);
 				String[] linkName = new String[2]; // file name and file path
 				int t = H5.H5Lget_val(gid, oname, linkName, HDF5Constants.H5P_DEFAULT);
 //				System.err.println("  -> " + linkName[0] + " in " + linkName[1]);
 				if (t < 0) {
 					logger.warn("Could not get value of link");
 					continue;
 				}
 				String[] prefix = new String[1];
 				t = (int) H5.H5Pget_elink_prefix(H5.H5Pcreate(HDF5Constants.H5P_LINK_ACCESS), prefix);
 				if (t <= 0) {
 					logger.warn("Could not get prefix");
 					if (!(new File(linkName[1]).exists())) {
 						prefix[0] = f.getParentDirectory(); // use directory of linking file
 					}
 				}
 				group.addNode(name, oname, getExternalNode(pool, (new File(prefix[0], linkName[1])).getAbsolutePath(), linkName[0], keepBitWidth));
 			} else {
 //				System.err.println("U: " + oname);
 			}
 		}
 
 		try {
 			H5.H5Gclose(gid);
 		} catch (HDF5Exception ex) {
 		}
 
 		return group;
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
 
 	// return true when attributes contain a NAPI mount - dodgy external linking for HDF5 version < 1.8
 	private static boolean copyAttributes(final String name, final HDF5Node nn, final int id) {
 		boolean hasNAPIMount = false;
 
 		try {
 			final List<Attribute> attributes = H5File.getAttribute(id);
 			for (Attribute a : attributes) {
 				HDF5Attribute h = new HDF5Attribute(name, a.getName(), a.getValue(), a.isUnsigned());
 				h.setTypeName(getTypeName(a.getType()));
 				nn.addAttribute(h);
 				if (a.getName().equals(NAPIMOUNT)) {
 					hasNAPIMount = true;
 				}
 			}
 		} catch (Exception e) {
 			logger.warn("Problem with attributes on {}: {}", name, e.getMessage());
 		}
 		return hasNAPIMount;
 	}
 
 	// get external node
 	private static HDF5Node getExternalNode(final HashMap<Long, HDF5Node> pool, final String lpath, String node, final boolean keepBitWidth) throws Exception {
 		HDF5Node nn = null;
 
 		ReentrantLock lock = acquireLock(lpath);
 		try {
 			final H5File hdf = (H5File) FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5).createInstance(lpath,
 					FileFormat.READ);
 			if (!hdf.canRead())
 				throw new IllegalArgumentException("Cannot read file");
 
 //			final int lid = hdf.open();
 //			if (lid < 0)
 //				throw new IllegalArgumentException("Opening file was unsuccessful");
 
 			if (!node.startsWith(HDF5File.ROOT)) {
 				node = HDF5File.ROOT + node;
 			}
 
 			try {
 				HObject lobj = hdf.get(node);
 				if (lobj != null) {
 					final long oid = lobj.getOID()[0] + hdf.getAbsolutePath().hashCode() * 17; // include file name in
 																								// ID
 					nn = copyNode(new HDF5File(oid, lpath), pool, lobj, keepBitWidth);
 				} else {
 					logger.warn("Could not get external node {} in {}", node, lpath);
 				}
 			} catch (Exception e) {
 				throw new ScanFileHolderException("Problem loading file", e);
 			} finally {
 				hdf.close();
 			}
 		} finally {
 			releaseLock(lock);
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
 
 	/**
 	 * Method can be used to set up a HObject which is a group.
 	 * @param file
 	 * @param pool - may be null
 	 * @param oo
 	 * @param keepBitWidth
 	 * @return HDF5Node, child to be added.
 	 * @throws Exception
 	 */
 	public static HDF5Node copyNode(final HDF5File file, final HashMap<Long, HDF5Node> pool, final HObject oo, final boolean keepBitWidth) throws Exception {
 		if (oo instanceof H5Link) {
 			// a link which cannot be resolved remains a link
 			H5Link ol = (H5Link) oo;
 			ol.getMetadata(); // API quirk needed to populate link target
 			final String target = ol.getLinkTargetObjName();
 			if (target == null) {
 				throw new IllegalArgumentException("No link target defined in link object");
 			}
 
 			throw new IllegalArgumentException("Link target cannot be resolved: " + target);
 		}
 
 		final Long oid = oo.getOID()[0] + file.getFilename().hashCode()*17; // include file name in ID
 
 		if (oo instanceof Dataset) {
 			if (pool != null && pool.containsKey(oid)) {
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
 					try {
 						ILazyDataset l = createStackedDatasetFromStrings(ef);
 						nd.setDataset(l);
 					} catch (Throwable th) {
 						logger.warn("Could not find {}, trying in {}", ef.files[0], file.getParentDirectory());
 						try { // try again with known-to-be-good directory
 							ILazyDataset l = createStackedDatasetFromStrings(ef, file.getParentDirectory());
 							nd.setDataset(l);
 						} catch (Throwable th2) {
 							logger.error("Unable to create lazydataset for" + osd, th2);
 							nd.setString(ef.getAsText());
 						}
 					}
 				} else {
 					nd.setDataset(createLazyDataset(file.getHostname(), osd, keepBitWidth));
 					nd.setMaxShape(osd.getMaxDims());
 				}
 			} else {
 				nd.setDataset(createLazyDataset(file.getHostname(), osd, keepBitWidth));
 				nd.setMaxShape(osd.getMaxDims());
 			}
 			if (pool != null)
 				pool.put(oid, nd);
 			return nd;
 		} else if (oo instanceof H5Group) {
 			if (pool != null && pool.containsKey(oid)) {
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
 
 			if (pool != null)
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
 			return AbstractDataset.STRING;
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
 
 	/**
 	 * Create a lazy dataset from a H5 scalar dataset
 	 * @param host
 	 * @param osd
 	 * @param keepBitWidth
 	 * @return the dataset
 	 * @throws Exception
 	 */
 	public static ILazyDataset createLazyDataset(final String host, final H5ScalarDS osd, final boolean keepBitWidth) throws Exception {
 		long[] dims = osd.getDims();
 		if (dims == null) {
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
 						final int trank = trueShape.length;
 						int[] tstart = new int[trank];
 						int[] tsize = new int[trank];
 						int[] tstep = new int[trank];
 
 						if (rank > trank) { // shape was extended (from left) then need to translate to true slice
 							int j = 0;
 							for (int i = 0; i < trank; i++) {
 								if (trueShape[i] == 1) {
 									tstart[i] = 0;
 									tsize[i] = 1;
 									tstep[i] = 1;
 								} else {
 									while (shape[j] == 1 && (rank - j) > (trank - i))
 										j++;
 
 									tstart[i] = lstart[j];
 									tsize[i] = newShape[j];
 									tstep[i] = lstep[j];
 									j++;
 								}
 							}
 						} else { // shape was squeezed then need to translate to true slice
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
 
 	/**
 	 * Create a lazy dataset from given dataset and datatype IDs
 	 * @param file
 	 * @param nodePath full node path
 	 * @param name
 	 * @param did
 	 * @param tid
 	 * @param keepBitWidth
 	 * @param useExternalFiles
 	 * @return the dataset
 	 * @throws Exception
 	 */
 	private static ILazyDataset createLazyDataset(final HDF5File file, final String nodePath, final String name, final int did, final int tid, final boolean keepBitWidth, final boolean useExternalFiles) throws Exception {
 		int sid = -1, pid = -1;
 		int rank;
 		boolean isText, isVLEN, isUnsigned = false;
 //		boolean isEnum, isRegRef, isNativeDatatype;
 		Object fillValue;
 		long[] dims;
 		long[] maxDims;
 		Datatype type;
 		final int[] trueShape;
 
 		try {
 			sid = H5.H5Dget_space(did);
 
 			int tclass = H5.H5Tget_class(tid);
 			rank = H5.H5Sget_simple_extent_ndims(sid);
 
 			isText = tclass == HDF5Constants.H5T_STRING;
 			isVLEN = tclass == HDF5Constants.H5T_VLEN || H5.H5Tis_variable_str(tid);
 			isUnsigned = H5Datatype.isUnsigned(tid);
 //			isEnum = tclass == HDF5Constants.H5T_ENUM;
 //			isRegRef = H5.H5Tequal(tid, HDF5Constants.H5T_STD_REF_DSETREG);
 
 			// check if it is an external dataset
 			try {
 				pid = H5.H5Dget_create_plist(did);
 				int nfiles = H5.H5Pget_external_count(pid);
 				if (nfiles > 0)
 					return null;
 			} catch (Exception ex) {
 			}
 
 			// check if datatype in file is native datatype
 			int tmptid = 0;
 			try {
 				tmptid = H5.H5Tget_native_type(tid);
 //				isNativeDatatype = H5.H5Tequal(tid, tmptid);
 
 				/* see if fill value is defined */
 				int[] fillStatus = { 0 };
 				if (H5.H5Pfill_value_defined(pid, fillStatus) >= 0) {
 					if (fillStatus[0] == HDF5Constants.H5D_FILL_VALUE_USER_DEFINED) {
 						fillValue = H5Datatype.allocateArray(tmptid, 1);
 						try {
 							H5.H5Pget_fill_value(pid, tmptid, fillValue);
 						} catch (Exception ex2) {
 							fillValue = null;
 						}
 					}
 				}
 				type = new H5Datatype(tid);
 			} catch (HDF5Exception ex) {
 				logger.error("Could not get dataset type");
 				return null;
 			} finally {
 				try {
 					H5.H5Tclose(tmptid);
 				} catch (HDF5Exception ex) {
 				}
 				try {
 					H5.H5Pclose(pid);
 				} catch (Exception ex) {
 				}
 			}
 
 			if (rank == 0) {
 				// a scalar data point
 				rank = 1;
 				dims = new long[1];
 				dims[0] = 1;
 			} else {
 				dims = new long[rank];
 				maxDims = new long[rank];
 				H5.H5Sget_simple_extent_dims(sid, dims, maxDims);
 			}
 		} catch (HDF5Exception ex) {
 			logger.error("Could not get data space information", ex);
 			return null;
 		} finally {
 			try {
 				H5.H5Sclose(sid);
 			} catch (HDF5Exception ex2) {
 			}
 		}
 
 		trueShape = new int[dims.length];
 		for (int i = 0; i < dims.length; i++) {
 			long d = dims[i];
 			if (d > Integer.MAX_VALUE) {
 				throw new IllegalArgumentException("Dimension larger than ints");
 			}
 			trueShape[i] = (int) d;
 		}
 
 		if (trueShape.length == 1 && trueShape[0] == 1) { // special case for single values
 			try {
 				int[] spaceIDs = { -1, -1 };
 				spaceIDs[0] = HDF5Constants.H5S_ALL;
 				spaceIDs[1] = HDF5Constants.H5S_ALL;
 				Object data;
 				try {
 					data = H5Datatype.allocateArray(tid, 1);
 				} catch (OutOfMemoryError err) {
 					throw new HDF5Exception("Out Of Memory.");
 				}
 
 				boolean isREF = H5.H5Tequal(tid, HDF5Constants.H5T_STD_REF_OBJ);
 				if (isVLEN) {
 					H5.H5DreadVL(did, tid, spaceIDs[0], spaceIDs[1], HDF5Constants.H5P_DEFAULT, (Object[]) data);
 				} else {
 					H5.H5Dread(did, tid, spaceIDs[0], spaceIDs[1], HDF5Constants.H5P_DEFAULT, data);
 
 					if (isText) {
 						data = Dataset.byteToString((byte[]) data, H5.H5Tget_size(tid));
 					} else if (isREF) {
 						data = HDFNativeData.byteToLong((byte[]) data);
 					}
 				}
 				final AbstractDataset d = AbstractDataset.array(data);
 				d.setName(name);
 				return d;
 			} catch (HDF5Exception ex) {
 				logger.error("Could not read single value dataset", ex);
 				return null;
 			}
 		}
 
 		final boolean extendUnsigned = !keepBitWidth && isUnsigned;
 		final int dtype = getDtype(type.getDatatypeClass(), type.getDatatypeSize());
 
 		// cope with external files specified in a non-standard way and which may not be HDF5 either
 		if (dtype == AbstractDataset.STRING && useExternalFiles) {
 			// interpret set of strings as the full path names to a group of external files that are stacked together
 			if (!isVLEN && !isText) {
 				logger.error("String dataset not variable length or text!");
 				return null;
 			}
 				
 			ExternalFiles ef = extractExternalFileNames(did, tid, isVLEN, trueShape);
 			try {
 				return createStackedDatasetFromStrings(ef);
 			} catch (Throwable th) {
 				try { // try again with known-to-be-good directory
 					return createStackedDatasetFromStrings(ef, file.getParentDirectory());
 				} catch (Throwable th2) {
 					logger.error("Unable to create lazy dataset", th2);
 					return null;
 				}
 			}
 		}
 
 		final String host = file.getHostname();
 		final String filePath = file.getFilename();
 
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
 						final int trank = trueShape.length;
 						int[] tstart = new int[trank];
 						int[] tsize = new int[trank];
 						int[] tstep = new int[trank];
 
 						if (rank > trank) { // shape was extended (from left) then need to translate to true slice
 							int j = 0;
 							for (int i = 0; i < trank; i++) {
 								if (trueShape[i] == 1) {
 									tstart[i] = 0;
 									tsize[i] = 1;
 									tstep[i] = 1;
 								} else {
 									while (shape[j] == 1 && (rank - j) > (trank - i))
 										j++;
 
 									tstart[i] = lstart[j];
 									tsize[i] = newShape[j];
 									tstep[i] = lstep[j];
 									j++;
 								}
 							}
 						} else { // shape was squeezed then need to translate to true slice
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
 
 	/**
 	 * Create a stacked dataset from external files
 	 * @param ef
 	 * @return lazy data set from external file
 	 * @throws OutOfMemoryError
 	 * @throws Exception
 	 */
 	public static ILazyDataset createStackedDatasetFromStrings(ExternalFiles ef) throws OutOfMemoryError, Exception {
 		//remove final dimension as that is for the characters of the strings
 		//shape here is for the actual filenames
 		
 		ImageStackLoaderEx loader = new ImageStackLoaderEx(ef.shape, ef.files);
 		return new LazyDataset("file_name", loader.getDtype(), loader.getShape(), loader);
 	}
 
 	/**
 	 * Create a stacked dataset from external files
 	 * @param ef
 	 * @param directory
 	 * @return lazy data set from external file
 	 * @throws OutOfMemoryError
 	 * @throws Exception
 	 */
 	public static ILazyDataset createStackedDatasetFromStrings(ExternalFiles ef, String directory) throws OutOfMemoryError, Exception {
 		//remove final dimension as that is for the characters of the strings
 		//shape here is for the actual filenames
 		
 		ImageStackLoaderEx loader = new ImageStackLoaderEx(ef.shape, ef.files, directory);
 		return new LazyDataset("file_name", loader.getDtype(), loader.getShape(), loader);
 	}
 
 	/**
 	 * Return any External File references to be followed.
 	 * @return ExternalFiles object
 	 * @throws Exception
 	 */
 	private static ExternalFiles extractExternalFileNames(final int did, final int tid, final boolean isVLEN, final int[] shape) throws Exception {
 		int length = 1;
 		for (int i = 0; i < shape.length; i++) {
 			length *= shape[i];
 		}
 
 		int[] spaceIDs = { -1, -1 };
 		spaceIDs[0] = HDF5Constants.H5S_ALL;
 		spaceIDs[1] = HDF5Constants.H5S_ALL;
 		Object data;
 		try {
 			data = H5Datatype.allocateArray(tid, length);
 		} catch (OutOfMemoryError err) {
 			throw new HDF5Exception("Out of memory");
 		}
 
 		if (isVLEN) {
 			H5.H5DreadVL(did, tid, spaceIDs[0], spaceIDs[1], HDF5Constants.H5P_DEFAULT, (Object[]) data);
 		} else {
 			H5.H5Dread(did, tid, spaceIDs[0], spaceIDs[1], HDF5Constants.H5P_DEFAULT, data);
 			data = Dataset.byteToString((byte[]) data, H5.H5Tget_size(tid));
 		}
 
 		final String[] files = (String[]) data;
 		ExternalFiles ef= new ExternalFiles();
 		//reduce shape as we have removed the filenames
 		ef.shape = AbstractDataset.squeezeShape(shape, false);
 		ef.files = files;
 		return ef;
 	}
 
 	/**
 	 * Return any External File references to be followed.
 	 * @param sds
 	 * @return ExternalFiles object
 	 * @throws Exception
 	 */
 	public static ExternalFiles extractExternalFileNames(H5ScalarDS sds) throws Exception {
 		sds.init();
 		final long[] dshape = sds.getDims();
 		final long[] dstart = sds.getStartDims();
 		final long[] dstride = sds.getStride();
 		final long[] dsize = sds.getSelectedDims();
 
 		int length = 1;
 		final int[] shape = new int[dshape.length];
 		for (int i = 0; i < dshape.length; i++) {
 			dstride[i] = 1;
 			dsize[i] = 1;
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
 		ef.shape = AbstractDataset.squeezeShape(shape, false);
 		ef.files = files;
 		return ef;
 	}
 
 	@SuppressWarnings("null")
 	private static AbstractDataset loadData(final String fileName, final String node, final int[] start, final int[] count,
 			final int[] step, final int dtype, final boolean extend) throws ScanFileHolderException {
 		AbstractDataset data = null;
 
 		ReentrantLock lock = acquireLock(fileName);
 		try {
 			final FileFormat hdf5 = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
 			final H5File hdf = (H5File) hdf5.createInstance(fileName, FileFormat.READ);
 			if (!hdf.canRead())
 				throw new IllegalArgumentException("Cannot read file");
 
 			HObject obj = hdf.get(node);
 
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
 		} catch (Exception le) {
 			throw new ScanFileHolderException("Problem loading file", le);
 		} finally {
 			releaseLock(lock);
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
 
 	/**
 	 * Create metadata from tree
 	 * @param tree
 	 * @return a metadata object
 	 */
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
