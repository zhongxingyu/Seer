 /*-
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
 
 package org.dawb.common.services.conversion;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * The conversion context which will drive what we are going to convert.
  */
 public interface IConversionContext {
 
 	/**
 	 * Schemes to be edited as required, current list is a guess
 	 * of the conversions we have spoken about before.
 	 */
 	public enum ConversionScheme {
 		ASCII_FROM_1D(" ascii from 1D data",   true,  1), 
 		ASCII_FROM_2D(" ascii from 2D data",   false, 2), 
 		TIFF_FROM_3D(" image files from image stack", true, 2,3,4,5),
 		AVI_FROM_3D(" video from image stack", true, 2,3,4,5),
		H5_FROM_IMAGEDIR(" nexus stack from directory of images", true, false, 2),
 		H5_FROM_1D(" nexus from 1D data",   false,  1),
 		CUSTOM_NCD(" ascii from NCD data",     true, 2,3,4,5,6),
 		CUSTOM_TOMO(" tiff from tomography nexus file(s) [nxtomo]",    true, 3);
 		
 		private final String  uiLabel;
 		private final int[]   preferredRanks;
 		private final boolean userVisible;
 		private final boolean nexusOnly;
 
 		ConversionScheme(String uiLabel, boolean userVisible, int... preferredRanks) {
 			this(uiLabel, userVisible, true, preferredRanks);
 		}
 		ConversionScheme(String uiLabel, boolean userVisible, boolean nexusOnly, int... preferredRanks) {
 			this.uiLabel        = uiLabel;
 			this.userVisible    = userVisible;
 			this.nexusOnly      = nexusOnly;
 			this.preferredRanks = preferredRanks;
 		}
 
 		public String getUiLabel() {
 			return uiLabel;
 		}
 		
 		public static ConversionScheme fromLabel(String uiLabel) {
 			for (ConversionScheme cs : values()) {
 				if (cs.getUiLabel().equals(uiLabel)) return cs;
 			}
 			return null;
 		}
 
 		/**
 		 * The labels of the active user interface schemes.
 		 * @return
 		 */
 		public static String[] getLabels() {
 			final List<String> labels = new ArrayList<String>(3);
 			for (int i = 0; i < values().length; i++)  {
 				if (values()[i].isUserVisible()) labels.add(values()[i].getUiLabel());
 			}
 			return labels.toArray(new String[labels.size()]);
 		}
 
 		/**
 		 * The preferred dimensions of data sets likely to be chosen
 		 * by this wizard, if null, there is no preference.
 		 * @return
 		 */
 		public int[] getPreferredRanks() {
 			return preferredRanks;
 		}
 
 		public boolean isRankSupported(int rank) {
 			if (preferredRanks==null) return false;
 			for (int i = 0; i < preferredRanks.length; i++) {
 				if (preferredRanks[i]==rank) return true;
 			}
 			return false;
 		}
 
 		/**
 		 * 
 		 * @return true if scheme should appear in UI choices such as the
 		 * conversion wizard.
 		 */
 		public boolean isUserVisible() {
 			return userVisible;
 		}
 		public boolean isNexusOnly() {
 			return nexusOnly;
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public IMonitor getMonitor();
 	
 	/**
 	 * 
 	 * @param monitor
 	 */
 	public void setMonitor(IMonitor monitor);
 	
 	/**
 	 * Get the current conversion.
 	 * @return
 	 */
 	public ConversionScheme getConversionScheme();
 	
 	/**
 	 * Set the way in which we will convert
 	 * @param cs
 	 */
 	public void setConversionScheme(ConversionScheme cs);
 	
 	/**
 	 * Call to override the conversion scheme. If this 
 	 * @return
 	 */
 	public void setConversionVisitor(IConversionVisitor visitor);
 
 	/**
 	 * Call to run the conversion. Process all files matching the filePathRegEx
 	 * @param context
 	 * @throws Exception if problem processing the conversion.
 	 */
 	public IConversionVisitor getConversionVisitor();
 
 	/**
 	 * The file that we are converting
 	 * @return
 	 */
 	public List<String> getFilePaths();
 	
 	/**
 	 * The dataset(s) we will process. Allows regular expressions inside
 	 * each set.
 	 * 
 	 * @return regexp of data set name (using / as the path separator)
 	 */
 	public List<String> getDatasetNames();
 	
 	/**
 	 * The dataset(s) we will process. Allows regular expressions inside
 	 * each set.
 	 * 
 	 * @return regexp of data set name (using / as the path separator)
 	 */
 	public void setDatasetNames(List<String> sets);
 	
 	/**
 	 * Instead of specifying a dataset name(s) to loop over
 	 * one can specify an ILazyDataset to use instead. In this
 	 * case the datasetName(s) will be ignored.
 	 * 
 	 * @return ILazyDataset
 	 */
 	public ILazyDataset getLazyDataset();
 	
 	/**
 	 * Instead of specifying a dataset name(s) to loop over
 	 * one can specify an ILazyDataset to use instead. In this
 	 * case the datasetName(s) will be ignored.
 	 * 
 	 * @return ILazyDataset
 	 */
 	public void setLazyDataset(ILazyDataset lazy);
 
 
 	/**
 	 * The dataset(s) we will process. Allows regular expressions.
 	 * Sets the dataset names to a string list of size one.
 	 *  
 	 * @param datasetRegExp all Datasets (H5 definition) whose full path 
 	 * matches this reg exp will be converted.
 	 */
 	public void setDatasetName(String datasetRegExp);
 	
 	/**
 	 * 
 	 * @return path export will process to.
 	 */
 	public String getOutputPath();
 	
 	/**
 	 * Set the file path to output to.
 	 * @param folderPath
 	 */
 	public void setOutputPath(String fileOrfolderPath);
 	
 	/**
 	 * 
 	 * @param dim
 	 * @param sliceString either an integer to hold the dimension constant or
 	 * a range of the form "start:end" where start is the start index and end is
 	 * the end index or the string "all" to use the size of the dataset (start=0,
 	 * end-length dimension).
 	 * 
 	 * There should be only one range set in the slicing, one set to "all" or one
 	 * set to "start:end" where start is the start index. Only one range can be
 	 * processed for a given convert, other dimensions may be constant however.
 	 */
 	public void addSliceDimension(int dim, String sliceString);
 
 	/**
 	 * 
 	 * @return the dimensions to slice in.
 	 */
 	public Map<Integer, String> getSliceDimensions();
 	
 	/**
 	 * Get custom data which may be needed for a certain ConversionScheme
 	 * @return
 	 */
 	public Object getUserObject();
 	
 	/**
 	 * Set custom data which may be needed for a certain ConversionScheme
 	 * @return
 	 */
 	public void setUserObject(Object object);
 	
 	/**
 	 * During converting many files the current active file being converted may be returned here.
 	 * @return current conversion file, may be null.
 	 */
 	public File getSelectedConversionFile();
 	
 	/**
 	 * Set the current active file to convert from. For instance when the conversion API loops over
 	 * a directory of images, this path will be the current image file we are packing.
 	 * 
 	 * @param selectedConversionFile current conversion file, may be null.
 	 */
 	public void setSelectedConversionFile(File selectedConversionFile);
 	
 	/**
 	 * During converting many data sets, the current dataset path being converted 
 	 * may be retrieved here.
 	 * @return current path being converted
 	 */
 	public String getSelectedH5Path();
 	
 	/**
 	 * During converting many data sets, the current dataset path being converted 
 	 * may be set here.
 	 * @param h5Path
 	 */
 	public void setSelectedH5Path(String h5Path);
 	
 	/**
 	 * get the dataset name to be used for an axis when saving 2d data as text
 	 * @return.
 	 */
 	public String getAxisDatasetName();
 	
 	/**
 	 * set the dataset name to be used for an axis when saving 2d data as text
 	 * @param axisDatasetName
 	 */
 	public void setAxisDatasetName(String axisDatasetName);
 
 	/**
 	 * Amount of work to be done which will be 
 	 * progressed if the task is run in a wizard.
 	 * @return
 	 */
 	public int getWorkSize();
 	
 	
 	/**
 	 * Amount of work to be done which will be 
 	 * progressed if the task is run in a wizard.
 	 * @return
 	 */
 	public void setWorkSize(int workSize);
 
 	/**
 	 * Gets whether the converted ILazyDataset is based on the result of an expression.
 	 * @return
 	 */
 	public boolean isExpression();
 	
 	
 	/**
 	 * Sets whether the converted ILazyDataset is based on the result of an expression.
 	 */
 	public void setExpression(boolean expression) ;
 
 }
