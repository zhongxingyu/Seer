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
 
 package uk.ac.diamond.scisoft.analysis.plotserver;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import uk.ac.diamond.scisoft.analysis.roi.GridPreferences;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.ROIList;
 
 /**
  * This class holds the names of the parameters used in GuiBean to communicate with the GUI
  */
 public final class GuiParameters implements Serializable {
 
 	private String param;
 
 	// Ordinal of next parameter to be created
 	private static int nextOrdinal = 0;
 
 	// Assign an ordinal to this parameter
 	private final int ordinal = nextOrdinal++;
 	private final Class<?> clazz;
 	
 	private static final HashMap<String, GuiParameters> namesToObjects = new HashMap<String, GuiParameters>();
 
 	/**
 	 * Create a new GuiParameters with the specified name that can contain objects of the specified type.
 	 * 
 	 * @param parameter
 	 * @param clazz
 	 */
 	private GuiParameters(String parameter, Class<?> clazz) {
 		param = parameter;
 		this.clazz = clazz;
 		namesToObjects.put(parameter, this);
 	}
 
 	/**
 	 * Create a new GuiParameters with the specified name that can contain objects of the top level type, Serializable.
 	 * 
 	 * @param parameter
 	 */
 	private GuiParameters(String parameter) {
 		param = parameter;
 		this.clazz = Serializable.class;
 		namesToObjects.put(parameter, this);
 	}
 
 	@Override
 	public String toString() {
 		return param;
 	}
 
 
 	/**
 	 * Provide enum like valueOf method.
 	 * @param name String value of GuiParameters
 	 * @return the matching GuiParameters object, or <code>null</code> if no matching GuiParameters found
 	 */
 	public static GuiParameters valueOf(String name) {
 		return namesToObjects.get(name);
 	}	
 
 	/**
 	 * Provide enum like values method.
 	 * @return the set of all GuiParameters objects
 	 */
 	public static GuiParameters[] values() {
 		Collection<GuiParameters> values = namesToObjects.values();
 		return values.toArray(new GuiParameters[namesToObjects.size()]);
 	}
 	
 	/**
 	 * Return the permitted storage type for the given parameter.
 	 * <p>
 	 * Note, some class types are List.class which does not reflect the contents of the list, see documentation for
 	 * individual item for more details.
 	 * 
 	 * @return class
 	 */
 	public Class<?> getStorageClass() {
 		return clazz;
 	}
 
 	@Override
 	public final boolean equals(Object that) {
		return ordinal == ((GuiParameters) that).ordinal;
 	}
 
 	@Override
 	public final int hashCode() {
 		return ordinal;
 	}
 
 
 	/**
 	 * Specifies the plotting mode can be any of the values in GuiPlotMode
 	 */
 	public static final GuiParameters PLOTMODE = new GuiParameters("PlotMode", GuiPlotMode.class);
 
 	/**
 	 * Specifies the Title string of the graph
 	 */
 	public static final GuiParameters TITLE = new GuiParameters("Title", String.class);
 
 	/**
 	 * Specifies the ROI data
 	 */
 	public static final GuiParameters ROIDATA = new GuiParameters("ROI", ROIBase.class);
 
 	/**
 	 * Specifies the ROI data list
 	 */
 	public static final GuiParameters ROIDATALIST = new GuiParameters("ROIList", ROIList.class);
 
 	/**
 	 * Specifies the UUID of the plot client that originates the bean
 	 */
 	public static final GuiParameters PLOTID = new GuiParameters("PlotID", UUID.class);
 
 	/**
 	 * Specifies the plot operation at the moment can only be UPDATE or nothing
 	 */
 	public static final GuiParameters PLOTOPERATION = new GuiParameters("PlotOp", String.class);
 
 	/**
 	 * Specified the file operation and should hold a FileOperationsBean for further detail
 	 */
 	public static final GuiParameters FILEOPERATION = new GuiParameters("FileOp", FileOperationBean.class);
 
 	/**
 	 * Specifies the filename
 	 */
 	public static final GuiParameters FILENAME = new GuiParameters("Filename", String.class);
 
 	/**
 	 * Specifies a list of selected filenames (as a list of strings)
 	 */
 	public static final GuiParameters FILESELECTEDLIST = new GuiParameters("FileList", List.class); // List<String>
 
 	/**
 	 * Specifies the view to send the loaded file
 	 */
 	public static final GuiParameters DISPLAYFILEONVIEW = new GuiParameters("DisplayOnView", String.class);
 
 	/**
 	 * Specifies the X position / column the data should be placed in the image grid
 	 */
 	public static final GuiParameters IMAGEGRIDXPOS = new GuiParameters("IGridX", Integer.class);
 
 	/**
 	 * Specifies the Y position / row the data should be placed in the image grid
 	 */
 
 	public static final GuiParameters IMAGEGRIDYPOS = new GuiParameters("IGridY", Integer.class);
 
 	/**
 	 * Specifies the number of columns (or rows and columns) in Image Grid
 	 */
 	public static final GuiParameters IMAGEGRIDSIZE = new GuiParameters("IGridSize", Integer[].class);
 
 	/**
 	 * Metadata node path
 	 */
 	public static final GuiParameters METADATANODEPATH = new GuiParameters("NodePath", String.class);
 
 	/**
 	 * Tree node path (filename#/path/to/node)
 	 */
 	public static final GuiParameters TREENODEPATH = new GuiParameters("TreeNodePath", String.class);
 
 	/**
 	 * Specifies the GUI Preferences used by GridProfile
 	 */
 	public static final GuiParameters GRIDPREFERENCES = new GuiParameters("GridPrefs", GridPreferences.class);
 
 	/**
 	 * Session store for all the images in the ImageGridView for retrieval at next startup.
 	 * <p>
 	 * The Images are as a list of {@link GridImageEntry}
 	 */
 	public static final GuiParameters IMAGEGRIDSTORE = new GuiParameters("ImageGridStore", List.class); // List<GridImageEntry>
 
 	public static final GuiParameters VOLUMEHEADERSIZE = new GuiParameters("RawVolumeHeaderSize", Integer.class);
 
 	public static final GuiParameters VOLUMEVOXELTYPE = new GuiParameters("RawVolumeVoxelType", Integer.class);
 
 	public static final GuiParameters VOLUMEXDIM = new GuiParameters("RawVolumeVoxelXDim", Integer.class);
 
 	public static final GuiParameters VOLUMEYDIM = new GuiParameters("RawVolumeVoxelYDim", Integer.class);
 
 	public static final GuiParameters VOLUMEZDIM = new GuiParameters("RawVolumeVoxelZDim", Integer.class);
 
 	/**
 	 * Parameter for external access of the image grid view
 	 */
 	public static final GuiParameters IMAGEGRIDLIVEVIEW = new GuiParameters("ImageGridLiveView", String.class);
 
 	/**
 	 * List of IPeaks from fitting routing
 	 */
 	public static final GuiParameters FITTEDPEAKS = new GuiParameters("FittedPeaks", List.class); // List<IPeak>
 
 	public static final GuiParameters MASKING = new GuiParameters("Masking"); // Unknown/unused?
 
 	/**
 	 * Calibration peaks for NCD
 	 */
 	public static final GuiParameters CALIBRATIONPEAKS = new GuiParameters("CalibrationPeaks"); // Unknown/unused?
 
 	/**
 	 * Calibration function for NCD
 	 */
 	public static final GuiParameters CALIBRATIONFUNCTIONNCD = new GuiParameters("CalibrationFunction"); // Unknown/unused?
 	
 	/** 
  	 * Specifies the OneDFile 
  	 */ 
  	public static final GuiParameters ONEDFILE = new GuiParameters("OneDFile", OneDDataFilePlotDefinition.class); 
 
 }
