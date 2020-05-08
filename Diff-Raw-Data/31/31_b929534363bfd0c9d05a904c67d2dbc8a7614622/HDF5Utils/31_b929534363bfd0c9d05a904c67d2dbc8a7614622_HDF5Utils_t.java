 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.hdf5;
 
 import gda.analysis.io.ScanFileHolderException;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.ArrayUtils;
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
 import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
 import uk.ac.diamond.scisoft.analysis.rcp.explorers.AbstractExplorer;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisChoice;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.AxisSelection;
 import uk.ac.diamond.scisoft.analysis.rcp.inspector.DatasetSelection.InspectorType;
 import uk.ac.gda.monitor.IMonitor;
 
 public class HDF5Utils {
 	private static final Logger logger = LoggerFactory.getLogger(HDF5Utils.class);
 
 	private static final String NXAXES = "axes";
 	private static final String NXAXIS = "axis";
 	private static final String NXLABEL = "label";
 	private static final String NXPRIMARY = "primary";
 	private static final String NXSIGNAL = "signal";
 	private static final String NXDATA = "NXdata";
 	private static final String NXNAME = "long_name";
 	private static final String SDS = "SDS";
 
 	/**
 	 * Create a (HDF5) dataset selection from given node link. It defaults the inspector type
 	 * to a line and leaves file name null
 	 * @param link
 	 * @param isOldGDA
 	 * @return HDF5 selection
 	 */
 	public static HDF5Selection createDatasetSelection(HDF5NodeLink link, final boolean isOldGDA) {
 		// two cases: axis and primary or axes
 		// iterate through each child to find axes and primary attributes
 		HDF5Node node = link.getDestination();
 		List<AxisChoice> choices = new ArrayList<AxisChoice>();
 		HDF5Group gNode = null;
 		HDF5Dataset dNode = null;
 
 		// see if chosen node is a NXdata class
 		HDF5Attribute stringAttr = node.getAttribute(HDF5File.NXCLASS);
 		String nxClass = stringAttr != null ? stringAttr.getFirstElement() : null;
 		if (nxClass == null || nxClass.equals(SDS)) {
 			if (!(node instanceof HDF5Dataset))
 				return null;
 
 			dNode = (HDF5Dataset) node;
 			if (!dNode.isSupported())
 				return null;
 
 			gNode = (HDF5Group) link.getSource(); // before hunting for axes
 		} else if (nxClass.equals(NXDATA)) {
 			assert node instanceof HDF5Group;
 			gNode = (HDF5Group) node;
 			// find data (signal=1) and check for axes attribute
 			for (HDF5NodeLink l : (HDF5Group) node) {
 				if (l.isDestinationADataset()) {
 					dNode = (HDF5Dataset) l.getDestination();
 					if (dNode.containsAttribute(NXSIGNAL) && dNode.isSupported()) {
 						link = l;
 						break; // only one signal per NXdata item
 					}
 					dNode = null;
 				}
 			}
 		}
 
 		if (dNode == null || gNode == null) return null;
 		ILazyDataset cData = dNode.getDataset(); // chosen dataset
 		HDF5Attribute axesAttr = dNode.getAttribute(NXAXES);
 
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
 		for (HDF5NodeLink l : gNode) {
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
 
 		// build up list of choice per dimension
 		List<AxisSelection> axes  = new ArrayList<AxisSelection>(); // list of axes for each dimension
 
 		for (int i = 0; i < rank; i++) {
 			int dim = shape[i];
 			AxisSelection aSel = new AxisSelection(dim, i);
 			axes.add(aSel);
 			for (AxisChoice c : choices) {
 				if (c.getAxisNumber() == i) {
 					// add if choice has been designated as for this dimension
 					aSel.addChoice(c, c.getPrimary());
 				} else if (c.isDimensionUsed(i)) {
 					// add if axis index mapping refers to this dimension
 					aSel.addChoice(c, 0);
 				} else if (aNames.contains(c.getName())) {
 					// assume order of axes names FIXME
 					// add if name is in list of axis names
 					if (aNames.indexOf(c.getName()) == i && ArrayUtils.contains(c.getValues().getShape(), dim))
 						aSel.addChoice(c, 1);
 				}
 			}
 
 			// add in an automatically generated axis with top order so it appears after primary axes
 			AbstractDataset axis = AbstractDataset.arange(dim, AbstractDataset.INT32);
 			axis.setName(AbstractExplorer.DIM_PREFIX + (i + 1));
 			AxisChoice newChoice = new AxisChoice(axis);
 			newChoice.setAxisNumber(i);
 			aSel.addChoice(newChoice, aSel.getMaxOrder() + 1);
 		}
 
 		return new HDF5Selection(InspectorType.LINE, null, link.getFullName(), axes, cData);
 	}
 
 	private static final String NXENTRY = "NXentry";
 	private static final String NXPROGRAM = "program_name";
 	private static final String GDAVERSIONSTRING = "GDA ";
//	private static final int GDAMAJOR = 8;
//	private static final int GDAMINOR = 20;
 
 	/**
 	 * Analyses an HDF5 file to see if it is likely to be the old type of nexus file.
 	 * 
 	 * Warning loads whole tree and parses.
 	 * 
 	 * @param path
 	 * @param mon
 	 * @return true if old GDA tree
 	 * @throws ScanFileHolderException
 	 */
 	public static boolean isOldGDAFile(final String path, final IMonitor mon) throws ScanFileHolderException {
 		HDF5File ltree = new HDF5Loader(path).loadTree(mon);
         return isOldGDAFile(ltree);
 	}
 	
 	/**
 	 * Analyses an HDF5 file to see if it is likely to be the old type of nexus file.
 	 * 
 	 * Warning - it has to parse a lot of the tree to do this.
 	 * 
 	 * @param tree
 	 * @return true is old
 	 */
 	public static boolean isOldGDAFile(HDF5File tree) {
 		for (HDF5NodeLink link : tree.getGroup()) {
 			if (link.isDestinationAGroup()) {
 				HDF5Group g = (HDF5Group) link.getDestination();
 				HDF5Attribute stringAttr = g.getAttribute(HDF5File.NXCLASS);
 				if (stringAttr != null && stringAttr.isString() && NXENTRY.equals(stringAttr.getFirstElement())) {
 					if (g.containsDataset(NXPROGRAM)) {
 						HDF5Dataset d = g.getDataset(NXPROGRAM);
 						if (d.isString()) {
 							String s = d.getString().trim();
							return s.contains(GDAVERSIONSTRING); // as there's no current plans to change, just check for GDA 
//							int i = s.indexOf(GDAVERSIONSTRING);
//							if (i >= 0) {
//								String v = s.substring(i+4, s.lastIndexOf("."));
//								int j = v.indexOf(".");
//								int maj = Integer.parseInt(v.substring(0, j));
//								int min = Integer.parseInt(v.substring(j+1, v.length()));
//								return maj <= GDAMAJOR || (maj == GDAMAJOR && min < GDAMINOR);
//							}
 						}
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 
 }
