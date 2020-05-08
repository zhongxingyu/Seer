 package org.geworkbench.util.pathwaydecoder.mutualinformation;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 
 /**
 * AdjacencyMatrix <p>
  * 
  * This class needs deep cleaning-up.
  * 
  * @author not attributable
  * @version $Id$
  */
 
 public class AdjacencyMatrix implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4163326138016520666L;
 
 	private static Log log = LogFactory.getLog(AdjacencyMatrix.class);
 
 	private HashMap<Integer, HashMap<Integer, Float>> geneRows = new HashMap<Integer, HashMap<Integer, Float>>();
 	private HashMap<Integer, HashMap<Integer, String>> geneInteractionRows = new HashMap<Integer, HashMap<Integer, String>>();
 	private HashMap<String, HashMap<String, Float>> geneRowsNotInMicroarray = new HashMap<String, HashMap<String, Float>>();
 	private HashMap<String, HashMap<String, String>> geneInteractionRowsNotInMicroarray = new HashMap<String, HashMap<String, String>>();
 
 	private Map<String, Integer> idToGeneMapper = new HashMap<String, Integer>();
 	private Map<String, Integer> snToGeneMapper = new HashMap<String, Integer>();
 
 	private int[] histogram = new int[1024];
 	private DSMicroarraySet<DSMicroarray> maSet = null;
 
 	private String adjName;
 
 	static private final double edgeScale = 1024.0 / 0.15;
 
 	public AdjacencyMatrix() {
 		super();
 	}
 
 	public HashMap<Integer, HashMap<Integer, Float>> getGeneRows() {
 		return this.geneRows;
 	}
 
 	public HashMap<String, HashMap<String, Float>> getGeneRowsNotInMicroarray() {
 		return this.geneRowsNotInMicroarray;
 	}
 
 	/**
 	 * returns the strength of the edge between geneId1 and geneId2 (0.0 == no
 	 * edge)
 	 * 
 	 * @param geneId1
 	 *            int
 	 * @param geneId2
 	 *            int
 	 * @return float
 	 */
 	public float get(int geneId1, int geneId2) {
 		float maxValue = 0;
 		geneId1 = getMappedId(geneId1);
 		if (geneId1 >= 0) {
 			HashMap<Integer, Float> row = geneRows.get(new Integer(geneId1));
 			if (row != null) {
 				geneId2 = getMappedId(geneId2);
 				if (geneId2 >= 0) {
 					Float f = (Float) row.get(new Integer(geneId2));
 					if (f != null) {
 						maxValue = f.floatValue();
 					}
 				}
 			}
 		}
 		return maxValue;
 	}
 
 	public HashMap<Integer, HashMap<Integer, String>> getInteractionMap() {
 		return this.geneInteractionRows;
 	}
 
 	public HashMap<String, HashMap<String, String>> getInteractionNotInMicroarrayMap() {
 		return this.geneInteractionRowsNotInMicroarray;
 	}
 
 	/**
 	 * Returns a map with all the edges to geneId
 	 * 
 	 * @param geneId
 	 *            int
 	 * @return HashMap
 	 */
 	public HashMap<Integer, Float> get(int geneId) {
 		try {
 			geneId = getMappedId(geneId);
 			if (geneId > 0) {
 				return geneRows.get(new Integer(geneId));
 			}
 		} catch (Exception ex) {
 			System.out.println("Oh oh");
 		}
 		return null;
 	}
 
 	public void addGeneRow(int geneId) {
 		HashMap<Integer, Float> row = geneRows.get(new Integer(geneId));
 		if (row == null) {
 			row = new HashMap<Integer, Float>();
 			geneRows.put(new Integer(geneId), row);
 		}
 	}
 
 	public String getLabel() {
 		return this.adjName;
 	}
 
 	public void setLabel(String name) {
 		this.adjName = name;
 	}
 
 	/**
 	 * Adds and edge between geneId1 and geneId2
 	 * 
 	 * @param geneId1
 	 *            int
 	 * @param geneId2
 	 *            int
 	 * @param edge
 	 *            float
 	 */
 	public void add(int geneId1, int geneId2, float edge) {
 		geneId1 = getMappedId(geneId1);
 		geneId2 = getMappedId(geneId2);
 		if ((geneId1 >= 0) && (geneId2 >= 0)) {
 			int bin = Math.min(1023, (int) (edge * edgeScale));
 			if (bin >= 0) {
 				histogram[bin]++;
 			}
 			// adding the neighbor and edge for geneId1
 			// gene1 -> (gene2, edge)
 		}
 		HashMap<Integer, Float> row = geneRows.get(new Integer(geneId1));
 		if (row == null) {
 			row = new HashMap<Integer, Float>();
 			geneRows.put(new Integer(geneId1), row);
 		}
 		row.put(new Integer(geneId2), new Float(edge));
 
 		// doing it both ways; [gene2 -> (gene1, edge)]
 		row = geneRows.get(new Integer(geneId2));
 		if (row == null) {
 			row = new HashMap<Integer, Float>();
 			geneRows.put(new Integer(geneId2), row);
 		}
 		row.put(new Integer(geneId1), new Float(edge));
 
 	}
 
 	/**
 	 * Adds and edge between geneId1 and geneId2
 	 * 
 	 * @param geneId1
 	 *            String
 	 * @param geneId2
 	 *            String
 	 * @param edge
 	 *            float
 	 */
 	public void add(String geneId1, String geneId2,
 			boolean isGene1InMicroarray, boolean isGene2InMicroarray, float edge) {
 
 		if (isGene1InMicroarray == true)
 			geneId1 = String.valueOf(getMappedId(new Integer(geneId1)));
 		if (isGene2InMicroarray == true)
 			geneId2 = String.valueOf(getMappedId(new Integer(geneId2)));
		 
 
 		HashMap<String, Float> row = (HashMap<String, Float>) geneRowsNotInMicroarray
 				.get(geneId1);
 		if (row == null) {
 			row = new HashMap<String, Float>();
 			geneRowsNotInMicroarray.put(geneId1, row);
 		}
 		row.put(new String(geneId2), new Float(edge));
 
 		// doing it both ways; [gene2 -> (gene1, edge)]
 		row = (HashMap<String, Float>) geneRowsNotInMicroarray.get(geneId2);
 		if (row == null) {
 			row = new HashMap<String, Float>();
 			geneRowsNotInMicroarray.put(geneId2, row);
 		}
 		row.put(geneId1, new Float(edge));
 
 	}
 
 	/**
 	 * 
 	 * @param geneId1
 	 *            int
 	 * @param geneId2
 	 *            int
 	 * @param interaction
 	 *            String
 	 */
 	public void addDirectional(int geneId1, int geneId2, String interaction) {
 		geneId1 = getMappedId(geneId1);
 		geneId2 = getMappedId(geneId2);
 
 		HashMap<Integer, String> row = (HashMap<Integer, String>) geneInteractionRows
 				.get(new Integer(geneId1));
 		if (row == null) {
 			row = new HashMap<Integer, String>();
 			geneInteractionRows.put(new Integer(geneId1), row);
 		}
 		row.put(new Integer(geneId2), interaction);
 
 	}
 
 	/**
 	 * 
 	 * @param geneId1
 	 *            String
 	 * @param geneId2
 	 *            String
 	 * @param interaction
 	 *            String
 	 */
 	public void addDirectional(String geneId1, String geneId2,
 			boolean isGene1InMicroarray, boolean isGene2InMicroarray,
 			String interaction) {
 
 		if (isGene1InMicroarray == true)
 			geneId1 = String.valueOf(getMappedId(new Integer(geneId1)));
 		if (isGene2InMicroarray == true)
 			geneId2 = String.valueOf(getMappedId(new Integer(geneId2)));
		 
 
 		HashMap<String, String> row = (HashMap<String, String>) geneInteractionRowsNotInMicroarray
 				.get(new String(geneId1));
 		if (row == null) {
 			row = new HashMap<String, String>();
 			geneInteractionRowsNotInMicroarray.put(new String(geneId1), row);
 		}
 		row.put(new String(geneId2), interaction);
 	}
 
 	public int getMappedId(int geneId) {
 		if (geneId >= 0 && geneId < maSet.getMarkers().size()) {
 			DSGeneMarker gm = maSet.getMarkers().get(geneId);
 			// bug 2000, replaced getShortName() with getGeneName()
 			String sn = gm.getGeneName();
 			if (sn == null || sn.trim().length() == 0) {
 				return geneId;
 			}
 			if (sn.compareToIgnoreCase("ExoBCL6") == 0) {
 				geneId = -1;
 			} else if (gm.getLabel().compareToIgnoreCase("1827_s_at") == 0) {
 				geneId = gm.getSerial();
 			} else if (gm.getLabel().compareToIgnoreCase("1936_s_at") == 0) {
 				geneId = gm.getSerial();
 			} else {
 				if (sn.compareToIgnoreCase("MYC") == 0) {
 					// do nothing
 				}
 				if (gm.getLabel().compareToIgnoreCase("1936_s_at") == 0) {
 					sn = "MYC";
 					try {
 						gm = maSet.getMarkers().get("1973_s_at");
 					} catch (Exception ex) {
 						gm = null;
 					}
 				}
 				if (gm != null) {
 					// Test if a gene with the same name was mapped before.
 					Integer prevId = (Integer) idToGeneMapper
 							.get(gm.getLabel());
 					if (prevId != null) {
 						// This gene was mapped before. Replace with mapped one
 						geneId = prevId.intValue();
 					} else {
 						// Test if a gene with the same name was reported
 						// before.
 						prevId = (Integer) snToGeneMapper.get(sn);
 						if (prevId != null) {
 							// There was a previous gene with the same name.
 							// Hence:
 							// replace the id, and add a new mapping to both
 							// idToGeneMapper
 							// and geneToIdMapper
 							snToGeneMapper.put(sn, prevId);
 							idToGeneMapper.put(gm.getLabel(), prevId);
 							geneId = prevId.intValue();
 						} else {
 							snToGeneMapper.put(sn, new Integer(geneId));
 							idToGeneMapper.put(gm.getLabel(), new Integer(
 									geneId));
 						}
 					}
 				}
 			}
 		}
 		return geneId;
 	}
 
 	public Set<Integer> getKeys() {
 		return geneRows.keySet();
 	}
 
 	public int getConnectionNo() {
 		int connectionNo = 0;
		Iterator<HashMap<Integer, Float>> valuesIt = geneRows.values().iterator();
 		while (valuesIt.hasNext()) {
 			HashMap<Integer, Float> geneRow = valuesIt.next();
 			connectionNo += geneRow.size();
 		}
 
 		return connectionNo;
 	}
 
 	public void setMicroarraySet(DSMicroarraySet<DSMicroarray> microarraySet) {
 		maSet = microarraySet;
		log.debug("set microarray set "+maSet.getDataSetName());
 	}
 
 	public DSMicroarraySet<DSMicroarray> getMicroarraySet() {
 		return maSet;
 	}
	
 }
