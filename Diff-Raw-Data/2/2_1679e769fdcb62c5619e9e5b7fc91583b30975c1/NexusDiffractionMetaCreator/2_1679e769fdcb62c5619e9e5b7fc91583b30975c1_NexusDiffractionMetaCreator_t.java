 package org.dawnsci.plotting.tools.diffraction;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.dawnsci.plotting.tools.preference.detector.DiffractionDetectorHelper;
 
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionMetaReader;
 
 
 public class NexusDiffractionMetaCreator {
 	
 	NexusDiffractionMetaReader nexusDiffraction = null;
 	
 	public NexusDiffractionMetaCreator(String filePath) {
 		nexusDiffraction = new NexusDiffractionMetaReader(filePath); 
 	}
 	
 	/**
 	 * Read the diffraction metadata from a Nexus file.
 	 * Other methods on the class can be used to determine how complete the read is
 	 * May return null
 	 * Uses NexusDiffactionMetaReader to read the data, this class just gives access to the 
 	 * metadata stored in the preference file
 	 * 
 	 * @param imageSize. Size of the image the diffraction metadata is associated with in pixels (can be null)
 	 */
 	public IDiffractionMetadata getDiffractionMetadataFromNexus(int[] imageSize) {
 		final DetectorBean bean = DiffractionDefaultMetadata.getPersistedDetectorPropertiesBean(imageSize);
 		final DiffractionCrystalEnvironment diffcrys = DiffractionDefaultMetadata.getPersistedDiffractionCrystalEnvironment();
 		
 		double[] xyPixelSize = DiffractionDetectorHelper.getXYPixelSizeMM(imageSize);
 		
 		IDiffractionMetadata md = nexusDiffraction.getDiffractionMetadataFromNexus(imageSize, null, null, xyPixelSize);
 		
		if (md != null && !nexusDiffraction.anyValuesRead()) {
 			md = new DiffractionMetadata(nexusDiffraction.getFilePath(), bean.getDetectorProperties(), diffcrys);
 			Collection<Serializable> col = new ArrayList<Serializable>();
 			col.add(bean.getDiffractionDetector());
 			((DiffractionMetadata)md).setUserObjects(col);
 		}
 		
 		if (!nexusDiffraction.isDetectorRead()) {
 			if (md instanceof DiffractionMetadata) {
 				
 				Collection<Serializable> col = new ArrayList<Serializable>();
 				col.add(bean.getDiffractionDetector());
 				((DiffractionMetadata)md).setUserObjects(col);
 			}
 		}
 		
 		return md;
 	}
 	
 	/**
 	 * Have complete DetectorProperties and DiffractionCrystalEnvironment values been read
 	 */
 	public boolean isCompleteRead() {
 		return nexusDiffraction.isCompleteRead();
 	}
 	
 	/**
 	 * Have enough values to perform downstream calculations been read (ie exposure time not read)
 	 */
 	public boolean isPartialRead() {
 		return nexusDiffraction.isPartialRead();
 	}
 	
 	/**
 	 * Were any values read from the Nexus file
 	 */
 	public boolean anyValuesRead() {
 		return nexusDiffraction.anyValuesRead();
 	}
 
 }
