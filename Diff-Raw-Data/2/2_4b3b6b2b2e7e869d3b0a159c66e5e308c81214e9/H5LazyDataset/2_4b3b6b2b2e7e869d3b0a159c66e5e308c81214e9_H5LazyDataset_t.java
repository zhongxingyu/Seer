 package org.dawb.gda.extensions.loaders;
 
 import ncsa.hdf.object.Dataset;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public class H5LazyDataset extends LazyDataset {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4375441355891182709L;
 
 	/**
 	 * You must ensure the meta data is loaded for this data set before using this
 	 * constructor, 		set.getMetadata();
 	 * @param set
 	 * @param filePath
 	 * @throws Exception 
 	 */
 	public H5LazyDataset(final Dataset set, final String filePath) throws Exception {
 		
 	    super(set.getFullName(), 
               H5Utils.getDataType(set.getDatatype()), 
               H5Utils.getInt(set.getDims()),
			  new H5LazyLoader(set.getFile(), set.getFullName()));
 	}
 	
 	public AbstractDataset getCompleteData(IMonitor monitor) throws Exception {
 		return ((H5LazyLoader)this.loader).getCompleteData(monitor);
 	}
 }
