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
 
 package uk.ac.diamond.scisoft.analysis.rcp.preference;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
 
 import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
 
 public class PreferenceInitializer extends AbstractPreferenceInitializer {
 	public static final String DELIMITER =  "]}¬¬{[";
 	
 	private static final Boolean DEFAULT_SIDEPLOTTER1D_USE_LOG = false;
 
 	private static final int DEFAULT_GRIDSCAN_BEAMLINE_POSITION = 0;
 	private static final double DEFAULT_GRIDSCAN_RESOLUTION = 10000.0;
 	private static final String DEFAULT_DIFFRACTION_PEAK= "Gaussian";
 	private static final int DEFAULT_MAX_NUM_PEAKS = 10;
 	private static final int DEFAULT_PIXELOVERLOAD_THRESHOLD = 65535;
 	private static final boolean DEFAULT_SHOW_SCROLLBARS = true;
 	private static final int DEFAULT_COLOURMAP_CHOICE = 1;
 	private static final int DEFAULT_CAMERA_PROJECTION = 0;
 	private static final int DEFAULT_IMAGEXPLORER_COLOURMAP_CHOICE = 0;
 	private static final int DEFAULT_IMAGEEXPLORER_HISTOGRAM_SCALE = 98;
 	private static final int DEFAULT_IMAGEEXPLORER_TIMEDEAY = 1000;
 	private static final String DEFAULT_IMAGEEXPLORER_PLAYBACKVIEW = "Live Plot";
 	private static final int DEFAULT_IMAGEEXPLORER_PLAYBACKRATE = 1;
 	private static final boolean DEFAULT_COLOURMAP_EXPERT = false;
 	private static final boolean DEFAULT_AUTOHISTOGRAM = true;
 	private static final int DEFAULT_COLOURSCALE_CHOICE = 0;
 	
 	private static final boolean DEFAULT_DIFFRACTION_VIEWER_AUTOSTOPPING = true;
 	private static final int DEFAULT_DIFFRACTION_VIEWER_STOPPING_THRESHOLD= 25;
 	
 	private static final String DEFAULT_STANDARD_NAME_LIST = "Cr2O3"+DELIMITER+"Silicon"+DELIMITER+"Bees Wax";
 	private static final String DEFAULT_STANDARD_NAME = "Cr2O3";
 	
 	private static final String DEFAULT_STANDARD_DISTANCES_LIST = "3.645, 2.672, 2.487, 2.181, 1.819, 1.676, 1.467, 1.433"+DELIMITER+"3.6, 2.05, 1.89,1.5,0.25"+DELIMITER+"3.6,2.4";
 	private static final String DEFAULT_STANDARD_DISTANCES = "3.645, 2.672, 2.487, 2.181, 1.819, 1.676, 1.467, 1.433";
 
 	private static final String DEFAULT_FITTING_1D_PEAKTYPE ="Gaussian"; 
 	private static final String DEFAULT_FITTING_1D_PEAKLIST = "Gaussian" +DELIMITER+ "Lorentzian" +DELIMITER+"Pearson VII" +DELIMITER+"PseudoVoigt" ;
 	private static final int DEFAULT_FITTING_1D_PEAK_NUM = 10;
 	private static final String DEFAULT_FITTING_1D_ALG_TYPE = "Genetic Algorithm";
 	private static final String DEFAULT_FITTING_1D_ALG_LIST = "Nelder Mead" +DELIMITER+ "Genetic Algorithm" +DELIMITER+ "Apache Nelder Mead";
 	private static final int DEFAULT_FITTING_1D_ALG_SMOOTHING = 5;
 	private static final double DEFAULT_FITTING_1D_ALG_ACCURACY = 0.01;
 	private static final boolean DEFAULT_FITTING_1D_AUTO_SMOOTHING = false;
 	private static final boolean DEFAULT_FITTING_1D_AUTO_STOPPING = true;
 	private static final int DEFAULT_FITTING_1D_THRESHOLD = 5;
 	private static final String DEFAULT_FITTING_1D_THRESHOLD_MEASURE = "Area";
 	private static final String DEFAULT_FITTING_1D_THRESHOLD_MEASURE_LIST = "Height"+DELIMITER+"Area";
 	public static final int DEFAULT_FITTING_1D_DECIMAL_PLACES = 2;
 
 	public static final int DEFAULT_ANALYSIS_RPC_SERVER_PORT = 0;
 	public static final String DEFAULT_ANALYSIS_RPC_TEMP_FILE_LOCATION = "";
 	public static final int DEFAULT_RMI_SERVER_PORT = 0;
 	
	public static final String DEFAULT_PRINTSETTINGS_PRINTNAME = getDefaultPrinterName();
 	public static final Double DEFAULT_PRINTSETTINGS_SCALE = 0.5;
 	public static final int DEFAULT_PRINTSETTINGS_RESOLUTION = 2;
 	public static final String DEFAULT_PRINTSETTINGS_ORIENTATION = "Portrait";
 	
 
 	@Override
 	public void initializeDefaultPreferences() {
 		IPreferenceStore store = AnalysisRCPActivator.getDefault().getPreferenceStore();
 		store.setDefault(PreferenceConstants.IGNORE_DATASET_FILTERS, false);
 		store.setDefault(PreferenceConstants.SHOW_XY_COLUMN,         false);
 		store.setDefault(PreferenceConstants.SHOW_DATA_SIZE,         false);
 		store.setDefault(PreferenceConstants.SHOW_DIMS,              false);
 		store.setDefault(PreferenceConstants.SHOW_SHAPE,             false);
 		store.setDefault(PreferenceConstants.DATA_FORMAT,            "#0.00");
 		store.setDefault(PreferenceConstants.PLAY_SPEED,             1500);
 
 		store.setDefault(PreferenceConstants.SIDEPLOTTER1D_USE_LOG_Y, DEFAULT_SIDEPLOTTER1D_USE_LOG);
 
 		store.setDefault(PreferenceConstants.GRIDSCAN_RESOLUTION_X, DEFAULT_GRIDSCAN_RESOLUTION);
 		store.setDefault(PreferenceConstants.GRIDSCAN_RESOLUTION_Y, DEFAULT_GRIDSCAN_RESOLUTION);
 		store.setDefault(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX, DEFAULT_GRIDSCAN_BEAMLINE_POSITION);
 		store.setDefault(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX, DEFAULT_GRIDSCAN_BEAMLINE_POSITION);
 		
 		store.setDefault(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE, DEFAULT_DIFFRACTION_PEAK);
 		store.setDefault(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM, DEFAULT_MAX_NUM_PEAKS);		
 		store.setDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME, DEFAULT_STANDARD_NAME);
 		store.setDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME_LIST, DEFAULT_STANDARD_NAME_LIST);
 		store.setDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES,DEFAULT_STANDARD_DISTANCES);
 		store.setDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES_LIST, DEFAULT_STANDARD_DISTANCES_LIST);
 		store.setDefault(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD, DEFAULT_PIXELOVERLOAD_THRESHOLD);
 		store.setDefault(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING,DEFAULT_DIFFRACTION_VIEWER_AUTOSTOPPING);
 		store.setDefault(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD,DEFAULT_DIFFRACTION_VIEWER_STOPPING_THRESHOLD);
 		
 		store.setDefault(PreferenceConstants.FITTING_1D_PEAKTYPE,DEFAULT_FITTING_1D_PEAKTYPE);
 		store.setDefault(PreferenceConstants.FITTING_1D_PEAKLIST,DEFAULT_FITTING_1D_PEAKLIST);
 		store.setDefault(PreferenceConstants.FITTING_1D_PEAK_NUM, DEFAULT_FITTING_1D_PEAK_NUM);
 		store.setDefault(PreferenceConstants.FITTING_1D_ALG_TYPE,DEFAULT_FITTING_1D_ALG_TYPE);
 		store.setDefault(PreferenceConstants.FITTING_1D_ALG_LIST,DEFAULT_FITTING_1D_ALG_LIST);
 		store.setDefault(PreferenceConstants.FITTING_1D_SMOOTHING_VALUE,DEFAULT_FITTING_1D_ALG_SMOOTHING);
 		store.setDefault(PreferenceConstants.FITTING_1D_ALG_ACCURACY,DEFAULT_FITTING_1D_ALG_ACCURACY);
 		store.setDefault(PreferenceConstants.FITTING_1D_AUTO_SMOOTHING, DEFAULT_FITTING_1D_AUTO_SMOOTHING);
 		store.setDefault(PreferenceConstants.FITTING_1D_AUTO_STOPPING,DEFAULT_FITTING_1D_AUTO_STOPPING);
 		store.setDefault(PreferenceConstants.FITTING_1D_THRESHOLD,DEFAULT_FITTING_1D_THRESHOLD);
 		store.setDefault(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE,DEFAULT_FITTING_1D_THRESHOLD_MEASURE);
 		store.setDefault(PreferenceConstants.FITTING_1D_THRESHOLD_MEASURE_LIST,DEFAULT_FITTING_1D_THRESHOLD_MEASURE_LIST);
 		store.setDefault(PreferenceConstants.FITTING_1D_DECIMAL_PLACES,DEFAULT_FITTING_1D_DECIMAL_PLACES);
 
 		store.setDefault(PreferenceConstants.PLOT_VIEWER_PLOT2D_COLOURMAP,DEFAULT_COLOURMAP_CHOICE);
 		store.setDefault(PreferenceConstants.PLOT_VIEWER_PLOT2D_CMAP_EXPERT,DEFAULT_COLOURMAP_EXPERT);
 		store.setDefault(PreferenceConstants.PLOT_VIEWER_PLOT2D_AUTOHISTO,DEFAULT_AUTOHISTOGRAM);
 		store.setDefault(PreferenceConstants.PLOT_VIEWER_PLOT2D_SCALING, DEFAULT_COLOURSCALE_CHOICE);
 		store.setDefault(PreferenceConstants.PLOT_VIEWER_PLOT2D_SHOWSCROLLBAR,DEFAULT_SHOW_SCROLLBARS);
 		store.setDefault(PreferenceConstants.PLOT_VIEWER_MULTI1D_CAMERA_PROJ, DEFAULT_CAMERA_PROJECTION);
 		
 		store.setDefault(PreferenceConstants.IMAGEEXPLORER_COLOURMAP, DEFAULT_IMAGEXPLORER_COLOURMAP_CHOICE);
 		store.setDefault(PreferenceConstants.IMAGEEXPLORER_HISTOGRAMAUTOSCALETHRESHOLD, DEFAULT_IMAGEEXPLORER_HISTOGRAM_SCALE);
 		store.setDefault(PreferenceConstants.IMAGEEXPLORER_TIMEDELAYBETWEENIMAGES, DEFAULT_IMAGEEXPLORER_TIMEDEAY);
 		store.setDefault(PreferenceConstants.IMAGEEXPLORER_PLAYBACKVIEW, DEFAULT_IMAGEEXPLORER_PLAYBACKVIEW);
 		store.setDefault(PreferenceConstants.IMAGEEXPLORER_PLAYBACKRATE,DEFAULT_IMAGEEXPLORER_PLAYBACKRATE);
 		
 		store.setDefault(PreferenceConstants.ANALYSIS_RPC_SERVER_PORT,DEFAULT_ANALYSIS_RPC_SERVER_PORT);
 		store.setDefault(PreferenceConstants.ANALYSIS_RPC_TEMP_FILE_LOCATION,DEFAULT_ANALYSIS_RPC_TEMP_FILE_LOCATION);
 		store.setDefault(PreferenceConstants.RMI_SERVER_PORT,DEFAULT_RMI_SERVER_PORT);
 		
 		store.setDefault(PreferenceConstants.PRINTSETTINGS_PRINTER_NAME,DEFAULT_PRINTSETTINGS_PRINTNAME);
 		store.setDefault(PreferenceConstants.PRINTSETTINGS_SCALE,DEFAULT_PRINTSETTINGS_SCALE);
 		store.setDefault(PreferenceConstants.PRINTSETTINGS_RESOLUTION,DEFAULT_PRINTSETTINGS_RESOLUTION);
 		store.setDefault(PreferenceConstants.PRINTSETTINGS_ORIENTATION,DEFAULT_PRINTSETTINGS_ORIENTATION);
 	}
	
	private static String getDefaultPrinterName(){
		PrinterData pdata = Printer.getDefaultPrinterData();
		return pdata == null ? "" : pdata.name;
	}
 }
