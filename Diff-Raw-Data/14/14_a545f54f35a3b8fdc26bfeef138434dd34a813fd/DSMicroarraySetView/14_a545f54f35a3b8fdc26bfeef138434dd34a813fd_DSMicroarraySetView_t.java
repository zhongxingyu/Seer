 package org.geworkbench.bison.datastructure.biocollections.views;
 
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 
 /**
  * 
  * Interface of the view of microarray dataset for a given marker subset and a given microarray subset.
  * 
  * Copyright: Copyright (c) 2003 -2004
  * 
  * Company: Columbia University
  * 
  * @author Adam Margolin
  * @version $Id$
  */
 public interface DSMicroarraySetView<T extends DSGeneMarker, Q extends DSMicroarray>
 		extends DSDataSetView<Q> {
 	
 	// the following 7 methods used to be in DSDataSetView
 	public int size();
 	/**
 	 * @return A DSItemList containing all the <code>Q</code> type objects
 	 *         (generally microarrays) associated with this
 	 *         <code>DSDataView</code>.
 	 */
 	public DSItemList<Q> items();
 	/**
 	 * Set/reset item subselection based on activated panels.
 	 */
 	public void useItemPanel(boolean status);
 	/**
 	 * Gets the status of item activation.
 	 * 
 	 */
 	public boolean useItemPanel();
 
 	/**
 	 * Assigns a specific item panel selection.
 	 */
 	public void setItemPanel(DSPanel<Q> mArrayPanel);
 
 	/**
 	 * Assigns a specific item panel selection.
 	 */
 	public DSPanel<Q> getItemPanel();
 
 	/**
 	 * Sets the reference {@link DSDataSet} for this <code>DSDataSetView</code>.
 	 * 
 	 * @param dataSet
 	 *            The new reference dataset.
 	 */
 	//only used by AracneNalysis
 	public void setDataSet(DSDataSet<Q> dataSet);
 
 
 	// the following two methods used to be in DSMatrixDataSetView
     public double[] getRow(int index);
 
     public double getValue(int markerIndex, int arrayIndex);
 
 	public double getValue(T object, int arrayIndex);
 
 	public double getMeanValue(T marker, int maIndex);
 
 	public void setMicroarraySet(DSMicroarraySet<Q> ma);
 
 	public DSMicroarraySet<Q> getMicroarraySet();
 
 	/**
 	 * @return A DSItemList containing all the <code>T</code> type objects
 	 *         (generally markers) associated with this <code>DSDataView</code>.
 	 */
 	public DSItemList<T> markers();
 
 	/**
 	 * Set/resets marker subselection based on activated panels.
 	 * 
 	 * @param status
 	 */
 	public void useMarkerPanel(boolean status);
 
 	/**
 	 * Gets the status of marker activation
 	 * 
 	 * @return the status of marker activation
 	 */
 	public boolean useMarkerPanel();
 
 	/**
 	 * Allows to assign a specific microarray panel selection
 	 * 
 	 * @param markerPanel
 	 *            DSPanel
 	 */
 	public void setMarkerPanel(DSPanel<T> markerPanel);
 
 	/**
 	 * Allows to retrieve the marker panel selection
 	 */
 	public DSPanel<T> getMarkerPanel();
 
 	public DSItemList<T> allMarkers();
 
 	public Q get(int index);
 
 	public DSItemList<T> getUniqueMarkers();
 }
