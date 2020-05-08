 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
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
 
 package gda.device.detector.addetector.filewriter;
 
 import java.io.IOException;
 
 import gda.data.NumTracker;
 import gda.device.detector.areadetector.v17.NDFile;
 
 public class SingleImagePerFileWriterWithNumTracker extends SingleImagePerFileWriter {
 
 	private NumTracker numTracker = null;
 	
 	/**
 	 * Creates a SingleImagePerFileWriterWithNumTracker which writes folders of files alongside the current file in the 'standard'
 	 * location (ndFile and numTracker must still be configured). e.g. <blockquote>
 	 * 
 	 * <pre>
 	 * datadir
 	 *    snapped-data
 	 *       pilatus100k
 	 *          00001.tif
 	 * @param detectorName
 	 */
 	public SingleImagePerFileWriterWithNumTracker(String detectorName) {
		setFileTemplate("%s%s%5.5d.tif");
 		setFilePathTemplate("$datadir$/snapped-data/" + detectorName);
 		setFileNameTemplate("");
 		setFileNumberAtScanStart(1);
 	}
 	
 	/**
 	 * 	/**
 	 * Creates a SingleImageFileWriter with ndFile, fileTemplate, filePathTemplate, fileNameTemplate,
 	 * fileNumberAtScanStart, and numTrackerExtension yet to be set.
 	 */
 	public SingleImagePerFileWriterWithNumTracker() {
 		super();
 	}
 	
 	public void setNumTrackerExtension(String numTrackerExtension) {
 		try {
 			numTracker = new NumTracker(numTrackerExtension);
 		} catch (IOException e) {
 			throw new IllegalArgumentException("NumTracker with extension '" + numTrackerExtension + "' could not be created.", e);
 		}
 	}
 	
 		//this(ndFile, "%s%s%5.5d.jpg", "$datadir$/snapped-data/" + detectorName, "", numTrackerExtension);
 
 	@Override
 	protected void configureNdFile() throws Exception {
 		setFileNumberAtScanStart(numTracker.incrementNumber());
 		super.configureNdFile();
 	}
 
 }
