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
 import java.util.ArrayList;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * This class loads an SRS data file and also images from a Dectris Pilatus detector
  * <p>
  * <b>Note</b>: the metadata from this loader is left as strings
  */
 public class ExtendedSRSLoader extends SRSLoader implements ISliceLoader {
 
 	private static final String PILATUS_DIR = "pilatus100k"; // sub-directory for 100k images
 	private static final String PATH_DATASET = "path"; // path dataset name
 	private static final String PILATUS_TEMPLATE = "pilatus100k_path_template"; // metadata key for template format
 																				// string
 	private static final String DATA_NAME = "Pilatus";
 
 	public ExtendedSRSLoader(String filename) {
 		super(filename);
 	}
 
 	private void appendPilatusData(DataHolder currentDataHolder, IMonitor mon) {
 
 		ImageStackLoader loader = null;
 
 		// now we need to try to load in the the pilatus data
 		if (currentDataHolder.contains(PATH_DATASET)) {
 			AbstractDataset paths = currentDataHolder.getDataset(PATH_DATASET);
 			String template = textMetadata.get(PILATUS_TEMPLATE);
 			if (template == null) {
 				// bodged format v1
				loader = getImageStack(PILATUS_DIR + "/test%d.tif", PILATUS_DIR + "p%d.tif", paths, mon);
 			} else {
 				// bodged format v2
 				loader = getImageStack(template, null, paths, mon);
 			}
 		}
 
 		if (loader != null) {
 			LazyDataset lazyDataset = new LazyDataset(DATA_NAME, loader.getDtype(), loader.getShape(), loader);
 			currentDataHolder.addDataset(lazyDataset.getName(), lazyDataset);
 			datasetNames.add(lazyDataset.getName());
 			dataShapes.put(lazyDataset.getName(), lazyDataset.getShape());
 		}
 	}
 
 	private ImageStackLoader getImageStack(String format, String format2, AbstractDataset paths, IMonitor mon) {
 		ArrayList<String> files = new ArrayList<String>();
 		final File dir = new File(fileName).getParentFile();
 
 		// Only works with 1D set which is likely ok, we are a very specific format here.
 		for (int i = 0; i < paths.getSize(); ++i) {
 			int n = paths.getInt(i);
 			File iFile = new File(dir, String.format(format, n));
 			if (!iFile.exists() && format2 != null) iFile = new File(dir, String.format(format2, n));
 			if (!iFile.exists()) continue;
 			files.add(iFile.getAbsolutePath());
 		}
 		
 		// fix to http://jira.diamond.ac.uk/browse/DAWNSCI-439
 		// We fudge loading if the names of the tifs were not matched.
 		if (files.isEmpty() && format2==null) {
 			try {
 				final String subDir = format.substring(0, format.lastIndexOf('/'));
 				final File imageDir = new File(dir, subDir);
 				if (imageDir.exists() && imageDir.list()!=null) {
 					final File[] fa = imageDir.listFiles();
 					for (int i = 0; i < fa.length; i++) {
 						final File f = fa[i];
 						if (f.getName().endsWith(".tif")) {
 						    files.add(f.getAbsolutePath());
 						}
 					}
 				}
 			} catch (Exception e) {
 				logger.warn("Could not create ImageStackLoader, not populating Pilatus image stack");
 				return null;
 			}
 		}
 
 		try {
 			return new ImageStackLoader(files, mon);
 		} catch (Exception e) {
 			logger.warn("Could not create ImageStackLoader, not populating Pilatus image stack");
 			return null;
 		}
 	}
 
 	@Override
 	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
 		// load all the standard data in
 		DataHolder data = super.loadFile(mon);
 
 		appendPilatusData(data, mon);
 		if (loadMetadata) {
 			createMetadata();
 			data.setMetadata(getMetaData());
 		}
 		return data;
 	}
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 		super.loadMetaData(mon);
 
 		// Cannot do this if decorator, this means that the I16 data folder would parse all
 		// the ascii files in the whole directory!!
 
 		if (textMetadata.containsKey(PILATUS_TEMPLATE)) {
 
 			if (!datasetNames.contains(PATH_DATASET))
 				return;
 			/**
 			 * IMPORTANT DO NOT PARSE WHOLE FILE HERE! It will break the decorators!
 			 */
 			datasetNames.add(DATA_NAME);
 			createMetadata();
 		}
 	}
 
 	/**
 	 * Slices the stack of images
 	 */
 	@Override
 	public AbstractDataset slice(SliceObject bean, IMonitor mon) throws Exception {
 
 		/**
 		 * Not ideal have to parse SRS file once for each slice. The LoaderFactory caches slices which helps a little.
 		 */
 		this.fileName = bean.getPath();
 		final DataHolder dh = loadFile(mon);
 		ILazyDataset imageStack = dh.getLazyDataset(DATA_NAME);
 		// ImageStackLoader does load the AbstractDataset at this point
 		return (AbstractDataset) imageStack.getSlice(bean.getSliceStart(), bean.getSliceStop(), bean.getSliceStep());
 	}
 }
