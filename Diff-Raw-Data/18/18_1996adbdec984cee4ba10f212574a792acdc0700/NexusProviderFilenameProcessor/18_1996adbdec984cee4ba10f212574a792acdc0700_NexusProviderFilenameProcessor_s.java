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
 
 package gda.device.detector.nexusprocessor;
 
 import gda.data.nexus.FileNameBufToStrings;
 import gda.data.nexus.extractor.NexusGroupData;
 
 import java.io.File;
 import java.util.List;
 
 import org.nexusformat.NexusFile;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 /**
  * Class to get create a dataset from an external filename in a nexus file
  */
 
 public class NexusProviderFilenameProcessor extends NexusProviderDatasetProcessor {
 
 	/**
 	 * 
 	 * @param detName - name of detector in NexusTree. If null or empty the first detector entry is used
 	 * @param dataName
 	 * @param className
 	 * @param processors
 	 * @param datasetCreator
 	 */
 	public NexusProviderFilenameProcessor(String detName, String dataName, String className,
 			List<DataSetProcessor> processors, DatasetCreator datasetCreator) {
 		super(detName, dataName, className, processors, datasetCreator);
 	}
 
 	int dataset_index=0;
 	
 	
 	
 	public int getDataset_index() {
 		return dataset_index;
 	}
 
 	public void setDataset_index(int dataset_index) {
 		this.dataset_index = dataset_index;
 	}
 
 	@Override
 	protected AbstractDataset getAbstractDatasetFromNexusGroupData(NexusGroupData ngd) throws Exception {
 		if (!isEnabled())
 			return null;
		if( ngd.type == NexusFile.NX_CHAR){
 			String[] filenames = (new FileNameBufToStrings( ngd.dimensions, (byte[])ngd.getBuffer())).getFilenames();
 			if( filenames.length == 1){ 
 				String path = filenames[0];
 				if( !(new File(path)).exists()){
 					Thread.sleep(1000);
 				}
				DataHolder data = LoaderFactory.getData(path);
 				return data.getDataset(dataset_index);
 			}
 		}
 		return null;
 	}
 
 }
