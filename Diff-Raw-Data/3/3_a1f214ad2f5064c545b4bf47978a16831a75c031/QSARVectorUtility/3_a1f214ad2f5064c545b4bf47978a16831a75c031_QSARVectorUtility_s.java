 /*
  * Copyright (C) 2010 by Andreas Truszkowski <ATruszkowski@gmx.de>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  * All we ask is that proper credit is given for our work, which includes
  * - but is not limited to - adding the above copyright notice to the beginning
  * of your source code files, and to any copyright notice that you may distribute
  * with programs based on this work.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package org.openscience.cdk.applications.taverna.qsar;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import org.openscience.cdk.applications.art2aclassification.FingerprintItem;
 import org.openscience.cdk.applications.taverna.CDKTavernaException;
 import org.openscience.cdk.applications.taverna.basicutilities.ErrorLogger;
 
 /**
  * Utility class for handling QSAR vectors.
  * 
  * @author Andreas Truszkowski
  * 
  */
 public class QSARVectorUtility {
 
 	public static final int CURATE_ONLY_COLUMNS = 0;
 	public static final int CURATE_ONLY_ROWS = 1;
 	public static final int DYNAMIC_CURATION = 2;
 
 	private Map<UUID, Map<String, Object>> curatedVectorMap = new HashMap<UUID, Map<String, Object>>();
 	private ArrayList<String> curatedDescriptorNames = new ArrayList<String>();
 	private HashMap<UUID, Integer> numberOfErrorsInRowMap = new HashMap<UUID, Integer>();
 	private double errorStandardDeviation = 0;
 	private double averageErrorRatio = 0;
 
 	/**
 	 * Returns a list of the UUIDs from given QSAR vector.
 	 * 
 	 * @param vectorMap
 	 *            QSAR descriptor vector map.
 	 * @return List of the UUIDs from given the QSAR vector.
 	 */
 	public List<UUID> getUUIDs(Map<UUID, Map<String, Object>> vectorMap) {
 		ArrayList<UUID> uuids = new ArrayList<UUID>();
 		for (Entry<UUID, Map<String, Object>> entry : vectorMap.entrySet()) {
 			uuids.add(entry.getKey());
 		}
 		return uuids;
 	}
 
 	/**
 	 * Calculates statistic parameters like the standard deviation of the errors or the number of errors in each row.
 	 * 
 	 * @param vectorMap
 	 *            QSAR vector map
 	 * @param descriptorNames
 	 *            List of all calculated descriptor names
 	 * @param uuids
 	 *            UUIDs of all molecules.
 	 */
 	private void evaluateStatistics(Map<UUID, Map<String, Object>> vectorMap, ArrayList<String> descriptorNames, List<UUID> uuids) {
 		this.numberOfErrorsInRowMap.clear();
 		int entriesTotal = 0;
 		int errorsTotal = 0;
 		int errorsInRow = 0;
 		for (UUID uuid : uuids) {
 			Map<String, Object> descriptorResultMap = vectorMap.get(uuid);
 			errorsInRow = 0;
 			for (int i = 0; i < descriptorNames.size(); i++) {
 				entriesTotal++;
 				String key = descriptorNames.get(i);
 				Object result = descriptorResultMap.get(key);
 				if (result == null) {
 					errorsTotal++;
 					errorsInRow++;
 					continue;
 				}
 				Double value = Double.NaN;
 				if (result instanceof Double) {
 					value = (Double) result;
 				} else {
 					value = Double.valueOf((Integer) result);
 				}
 				if (Double.isNaN(value) || Double.isInfinite(value)) {
 					errorsInRow++;
 					errorsTotal++;
 				}
 			}
 			this.numberOfErrorsInRowMap.put(uuid, errorsInRow);
 		}
 		double averageErrorsPerRow = errorsTotal / (double) (uuids.size() - 1);
 		double s = 0;
 		for (Entry<UUID, Integer> entry : this.numberOfErrorsInRowMap.entrySet()) {
 			s += Math.pow(averageErrorsPerRow - entry.getValue(), 2);
 		}
 		this.errorStandardDeviation = Math.sqrt(s / (double) (descriptorNames.size() - 1));
 		this.averageErrorRatio = errorsTotal / (double) entriesTotal;
 	}
 
 	/**
 	 * Curates given QSAR vector data.
 	 * 
 	 * @param vectorMap
 	 *            QSAR vector map
 	 * @param descriptorNames
 	 *            List of all calculated descriptor names
 	 * @param type
 	 *            Curation type
 	 * @param curateMinMax
 	 *            If true - Reject columns with no min max difference
 	 * @throws CDKTavernaException
 	 */
 	public void curateQSARVector(Map<UUID, Map<String, Object>> vectorMap, ArrayList<String> descriptorNames, int type,
 			boolean curateMinMax) throws CDKTavernaException {
 		this.curatedDescriptorNames.clear();
 		this.curatedVectorMap.clear();
 		HashSet<Integer> columnsToDelete = null;
 		HashSet<UUID> rowsToDelete = new HashSet<UUID>();
 		List<UUID> uuids = getUUIDs(vectorMap);
 		this.evaluateStatistics(vectorMap, descriptorNames, uuids);
 		try {
 			// Search invalid values
 			for (UUID uuid : uuids) {
 				int rowErrors = this.numberOfErrorsInRowMap.get(uuid);
 				if (rowErrors == 0) {
 					continue;
 				}
 				// Curate only rows
 				if (type == QSARVectorUtility.CURATE_ONLY_ROWS) {
 					if (rowErrors > 0) {
 						rowsToDelete.add(uuid);
 					}
 					continue;
 				}
 				// Decrease accuracy by standard deviation
 				// TODO perhaps 1, 2 or 3 times standard deviation?
 				double rowErrorRatio = (rowErrors - (2 * this.errorStandardDeviation)) / (double) (descriptorNames.size() - 1);
 				if (type == QSARVectorUtility.DYNAMIC_CURATION && rowErrorRatio > this.averageErrorRatio) {
 					rowsToDelete.add(uuid);
 				} else {
 					if (columnsToDelete == null) {
 						columnsToDelete = this.getCorruptedColumns(vectorMap, descriptorNames, uuid);
 					} else {
 						HashSet<Integer> tempColumns = this.getCorruptedColumns(vectorMap, descriptorNames, uuid);
 						for (Integer column : tempColumns) {
 							if (!columnsToDelete.contains(column)) {
 								columnsToDelete.add(column);
 							}
 						}
 					}
 				}
 			}
 			// Remove all components which min and max value do not differ
 			if (curateMinMax) {
 				if (columnsToDelete == null) {
 					columnsToDelete = this.getMinMaxNotDifferColumns(vectorMap, descriptorNames);
 				} else {
 					HashSet<Integer> tempColumns = this.getMinMaxNotDifferColumns(vectorMap, descriptorNames);
 					for (Integer column : tempColumns) {
 						if (!columnsToDelete.contains(column)) {
 							columnsToDelete.add(column);
 						}
 					}
 				}
 			}
 			// Build curated data
 			ArrayList<Integer> columsToKeep = new ArrayList<Integer>();
 			for (int i = 0; i < descriptorNames.size(); i++) {
 				if (!columnsToDelete.contains(i)) {
 					columsToKeep.add(i);
 				}
 			}
 			for (int idx : columsToKeep) {
 				String key = descriptorNames.get(idx);
 				this.curatedDescriptorNames.add(key);
 			}
 			for (UUID uuid : uuids) {
 				if (rowsToDelete.contains(uuid)) {
 					continue;
 				}
 				Map<String, Object> descriptorResultMap = vectorMap.get(uuid);
 				Map<String, Object> curatedDescriptorResultMap = new HashMap<String, Object>();
 				for (int idx : columsToKeep) {
 					String key = descriptorNames.get(idx);
 					Double value = Double.NaN;
 					Object result = descriptorResultMap.get(key);
 					if (result instanceof Double) {
 						value = (Double) result;
 					} else {
 						value = Double.valueOf((Integer) result);
 					}
 					curatedDescriptorResultMap.put(key, value);
 				}
 				this.curatedVectorMap.put(uuid, curatedDescriptorResultMap);
 			}
 		} catch (Exception e) {
 			ErrorLogger.getInstance().writeError("Error during curation of QSAR vector!", this.getClass().getSimpleName(), e);
 			throw new CDKTavernaException(this.getClass().getSimpleName(), "Error during curation of QSAR vector!");
 		}
 	}
 
 	/**
 	 * Creates a fingerprint item list from given QSAR vector.
 	 * 
 	 * @param vectorMap
 	 *            QSAR vector map
 	 * @param descriptorNames
 	 *            List of all calculated descriptor names
 	 * @return Fingerprint item list
 	 */
 	public List<FingerprintItem> createFingerprintItemListFromQSARVector(Map<UUID, Map<String, Object>> vectorMap,
 			ArrayList<String> descriptorNames) {
 		ArrayList<FingerprintItem> itemList = new ArrayList<FingerprintItem>();
 		List<UUID> uuids = getUUIDs(vectorMap);
 		for (UUID uuid : uuids) {
 			Map<String, Object> descriptorResultMap = vectorMap.get(uuid);
 			double[] vector = new double[descriptorNames.size()];
 			for (int i = 0; i < descriptorNames.size(); i++) {
 				String key = descriptorNames.get(i);
 				Double value;
 				if (descriptorResultMap.get(key) == null) {
 					value = Double.NaN;
 				} else {
 					value = ((Number) descriptorResultMap.get(key)).doubleValue();
 				}
 				vector[i] = value;
 			}
 			FingerprintItem item = new FingerprintItem();
 			item.correspondingObject = uuid;
 			item.fingerprintVector = vector;
 			itemList.add(item);
 		}
 		return itemList;
 	}
 
 	/**
 	 * Creates a minimum set of descriptor names from given descriptor name list.
 	 * 
 	 * @param descriptorNameList
 	 *            List of all descriptor names.
 	 * @return
 	 */
 	public ArrayList<String> createMinimumDescriptorNamesList(List<List<String>> descriptorNameList) {
 		ArrayList<String> mergedDescriptorNames = new ArrayList<String>();
 		for (int i = 0; i < descriptorNameList.size(); i++) {
 			List<String> descriptorNames = descriptorNameList.get(i);
 			if (i == 0) {
 				for (String name : descriptorNames) {
 					mergedDescriptorNames.add(name);
 				}
 			} else {
 				for (int j = 0; j < mergedDescriptorNames.size(); j++) {
 					String name = mergedDescriptorNames.get(j);
 					if (!descriptorNames.contains(name)) {
 						mergedDescriptorNames.remove(j);
 					}
 				}
 			}
 		}
 		return mergedDescriptorNames;
 	}
 
 	/**
 	 * Merges given QSAR vector maps.
 	 * 
 	 * @param vectorMapList
 	 *            List with QSAR vectors
 	 * @param mergedDescriptorNames
 	 * @return
 	 */
 	public Map<UUID, Map<String, Object>> mergeQSARVectors(List<Map<UUID, Map<String, Object>>> vectorMapList,
 			List<String> mergedDescriptorNames) {
 		Map<UUID, Map<String, Object>> mergedVectorMap = new HashMap<UUID, Map<String, Object>>();
 		for (Map<UUID, Map<String, Object>> vectorMap : vectorMapList) {
 			List<UUID> uuids = this.getUUIDs(vectorMap);
 			for (UUID uuid : uuids) {
 				Map<String, Object> mergedDescriptorResultMap = new HashMap<String, Object>();
 				Map<String, Object> tempDescriptorMap = vectorMap.get(uuid);
 				for (String descriptorName : mergedDescriptorNames) {
 					Double value;
 					if (tempDescriptorMap.get(descriptorName) == null) {
 						value = Double.NaN;
 					} else {
 						value = ((Number) tempDescriptorMap.get(descriptorName)).doubleValue();
 					}
 					mergedDescriptorResultMap.put(descriptorName, value);
 				}
 				mergedVectorMap.put(uuid, mergedDescriptorResultMap);
 			}
 		}
 		return mergedVectorMap;
 	}
 
 	/**
 	 * Identifies the indices of corrupted columns. (Double.NAN, Double.Infinit, Null)
 	 * 
 	 * @param vectorMap
 	 *            QSAR vector map
 	 * @param descriptorNames
 	 *            List of all calculated descriptor names
 	 * @param uuids
 	 *            UUIDs of all molecules.
 	 * @return Indices of corrupted columns.
 	 */
 	private HashSet<Integer> getCorruptedColumns(Map<UUID, Map<String, Object>> vectorMap, ArrayList<String> descriptorNames,
 			UUID uuid) {
 		HashSet<Integer> columnsToDelete = new HashSet<Integer>();
 		Map<String, Object> descriptorResultMap = vectorMap.get(uuid);
 		for (int i = 0; i < descriptorNames.size(); i++) {
 			String key = descriptorNames.get(i);
 			Object result = descriptorResultMap.get(key);
 			if (result == null) {
 				columnsToDelete.add(i);
 				continue;
 			}
 			Double value = Double.NaN;
 			if (result instanceof Double) {
 				value = (Double) result;
 			} else {
 				value = Double.valueOf((Integer) result);
 			}
 			if ((Double.isNaN(value) || Double.isInfinite(value)) && !columnsToDelete.contains(i)) {
 				columnsToDelete.add(i);
 			}
 		}
 		return columnsToDelete;
 	}
 
 	/**
 	 * Identifies the indices of columns where the min and max not differ.
 	 * 
 	 * @param vectorMap
 	 *            QSAR vector map
 	 * @param descriptorNames
 	 *            List of all calculated descriptor names
 	 * @return
 	 */
 	private HashSet<Integer> getMinMaxNotDifferColumns(Map<UUID, Map<String, Object>> vectorMap, ArrayList<String> descriptorNames) {
 		HashSet<Integer> columnsToDelete = new HashSet<Integer>();
 		List<UUID> uuids = getUUIDs(vectorMap);
 		for (int i = 0; i < descriptorNames.size(); i++) {
 			String key = descriptorNames.get(i);
 			double min = 0;
 			double max = 0;
 			boolean init = true;
 			for (UUID uuid : uuids) {
 				Map<String, Object> descriptorResultMap = vectorMap.get(uuid);
 				Object result = descriptorResultMap.get(key);
 				Double value = Double.NaN;
 				if (result != null) {
 					if (result instanceof Double) {
 						value = (Double) result;
 					} else {
 						value = Double.valueOf((Integer) result);
 					}
 				}
 				if (init) {
 					min = value;
 					max = value;
 					init = false;
 				} else {
 					if (Double.compare(value, min) < 0) {
 						min = value;
 					}
 					if (Double.compare(value, max) > 0) {
 						max = value;
 					}
 				}
 			}
 			if (min == max) {
 				columnsToDelete.add(i);
 			}
 		}
 		return columnsToDelete;
 	}
 
 	/**
 	 * Calculates statistic data of the given QSAR vector.
 	 * 
 	 * @param vectorMap
 	 *            QSAR vector map
 	 * @param descriptorNames
 	 *            List of all calculated descriptor names
 	 * @return
 	 */
 	public List<String> calculateQSARVectorStatistics(Map<UUID, Map<String, Object>> vectorMap, ArrayList<String> descriptorNames) {
 		ArrayList<String> stats = new ArrayList<String>();
 		List<UUID> uuids = getUUIDs(vectorMap);
 		this.evaluateStatistics(vectorMap, descriptorNames, uuids);
 		String stat = "Number Of Molecules: " + uuids.size();
 		stats.add(stat);
 		stat = "Number Of Descriptors: " + (descriptorNames.size() - 1);
 		stats.add(stat);
 		HashSet<Integer> columnsToDeleteError = new HashSet<Integer>();
 		// Calculate column errors
 		for (UUID uuid : uuids) {
 			if (columnsToDeleteError == null) {
 				columnsToDeleteError = this.getCorruptedColumns(vectorMap, descriptorNames, uuid);
 			} else {
 				HashSet<Integer> tempColumns = this.getCorruptedColumns(vectorMap, descriptorNames, uuid);
 				for (Integer column : tempColumns) {
 					if (!columnsToDeleteError.contains(column)) {
 						columnsToDeleteError.add(column);
 					}
 				}
 			}
 		}
 		stat = "Number Of Corrupted Descriptors: " + columnsToDeleteError.size();
 		double ratio = columnsToDeleteError.size() / (double) (descriptorNames.size() - 1) * 100;
 		stat += " --> " + String.format("%.2f", ratio) + "%";
 		stats.add(stat);
 		HashSet<Integer> columnsToDeleteMinMax = this.getMinMaxNotDifferColumns(vectorMap, descriptorNames);
 		stat = "Number Of Descriptors Where Min Max Not Differ: " + columnsToDeleteMinMax.size();
 		ratio = columnsToDeleteMinMax.size() / (double) (descriptorNames.size() - 1) * 100;
 		stat += " --> " + String.format("%.2f", ratio) + "%";
 		stats.add(stat);
 		for (Integer column : columnsToDeleteMinMax) {
 			if (!columnsToDeleteError.contains(column)) {
 				columnsToDeleteError.add(column);
 			}
 		}
 		stat = "Total Number Of Descriptors With Error: " + columnsToDeleteError.size();
 		ratio = columnsToDeleteError.size() / (double) (descriptorNames.size() - 1) * 100;
 		stat += " --> " + String.format("%.2f", ratio) + "%";
 		stats.add(stat);
 		// Row errors
 		HashSet<UUID> rowsToDelete = new HashSet<UUID>();
 		for (UUID uuid : uuids) {
 			int rowErrors = this.numberOfErrorsInRowMap.get(uuid);
 			if (rowErrors > 0) {
 				rowsToDelete.add(uuid);
 			}
 		}
 		stat = "Number Of Molecules With Descriptor Error: " + rowsToDelete.size();
 		ratio = rowsToDelete.size() / (double) (uuids.size()) * 100;
 		stat += " --> " + String.format("%.2f", ratio) + "%";
 		stats.add(stat);
 		// Dynamic curation
 		HashSet<Integer> columnsToDelete = null;
 		rowsToDelete = new HashSet<UUID>();
 		this.evaluateStatistics(vectorMap, descriptorNames, uuids);
 		for (UUID uuid : uuids) {
 			int rowErrors = this.numberOfErrorsInRowMap.get(uuid);
 			if (rowErrors == 0) {
 				continue;
 			}
 			// Decrease accuracy by standard deviation
 			// TODO perhaps 1, 2 or 3 times standard deviation?
 			double rowErrorRatio = (rowErrors - (2 * this.errorStandardDeviation)) / (double) (descriptorNames.size() - 1);
 			if (rowErrorRatio > this.averageErrorRatio) {
 				rowsToDelete.add(uuid);
 			} else {
 				if (columnsToDelete == null) {
 					columnsToDelete = this.getCorruptedColumns(vectorMap, descriptorNames, uuid);
 				} else {
 					HashSet<Integer> tempColumns = this.getCorruptedColumns(vectorMap, descriptorNames, uuid);
 					for (Integer column : tempColumns) {
 						if (!columnsToDelete.contains(column)) {
 							columnsToDelete.add(column);
 						}
 					}
 				}
 			}
 		}
 		double ratioMolecules = rowsToDelete.size() / (double) (uuids.size()) * 100;
 		double ratioDescriptors = columnsToDelete.size() / (double) (descriptorNames.size() - 1) * 100;
 		stat = "Dynamic Curation: Corrupted Molecules: " + rowsToDelete.size() + " --> " + String.format("%.2f", ratioMolecules)
 				+ "%";
 		stats.add(stat);
 		stat = "Dynamic Curation: Corrupted Descriptors: " + columnsToDelete.size() + " --> "
 				+ String.format("%.2f", ratioDescriptors) + "%";
 		stats.add(stat);
 		return stats;
 	}
 
 	/**
 	 * @return Curated QSAR vector map.
 	 */
 	public Map<UUID, Map<String, Object>> getCuratedVectorMap() {
 		return curatedVectorMap;
 	}
 
 	/**
 	 * @return Curated descriptor names.
 	 */
 	public ArrayList<String> getCuratedDescriptorNames() {
 		return curatedDescriptorNames;
 	}
 
 }
