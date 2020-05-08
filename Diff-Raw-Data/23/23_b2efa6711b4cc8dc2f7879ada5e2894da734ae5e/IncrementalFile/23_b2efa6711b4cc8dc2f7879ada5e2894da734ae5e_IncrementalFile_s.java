 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
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
 
 package gda.data.scan.datawriter;
 
 import gda.configuration.properties.LocalProperties;
 import gda.data.NumTracker;
 import gda.data.PathConstructor;
 import gda.util.exceptionUtils;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class is for writing ascii files of tabulated data, with a header and footer.
  * <P>
  * This class is abstract as it does not dictate the format used when writing to the file.
  * <P>
  * The files created use names which are an increment from the last name. Files are named in the format 0001.dat
  */
 public abstract class IncrementalFile extends DataWriterBase implements DataWriter {
 	
 	private static final Logger logger = LoggerFactory.getLogger(IncrementalFile.class);
 	
 	// the number of the file being written to
 	// (format is 0001.dat, new files have an incremental increase)
 	private long thisFileNumber = 0;
 	
 	private boolean fileNumberConfigured=false;
 	
 	// file extension to use
 	protected String fileExtension = null;
 
 	// file prefix to use (if any)
 	protected String filePrefix = null;
 
 	// where all file will be placed
 	protected String dataDir = null;
 
 	// file handle of currently open file
 	protected FileWriter file = null;
 
 	protected String fileName = null;
 
 	protected String fileUrl = null;
 
 	// to determine whether to write out column headers
 	protected boolean firstData = true;
 
 	protected String currentFileName = null;
 
 	/**
 	 * Constructor which determines the name of the next file.
 	 * 
 	 * @throws InstantiationException
 	 */
 	public IncrementalFile() throws InstantiationException {
 		// check for files using the format 1234.dat in the data directory
 		dataDir = PathConstructor.createFromDefaultProperty();
 
 		if (this.dataDir == null) {
 			// this is compulsory - stop the scan
 			throw new InstantiationException("cannot work out the data directory");
 		}
 
 		// Set the file extension to be used.
 		this.fileExtension = LocalProperties.get("gda.data.scan.datawriter.fileExtension");
 		if (this.fileExtension == null) {
 			this.fileExtension = "dat";
 		}
 	}
 
 	/**
 	 * Close down the file.
 	 */
 	void destroy() {
 		this.releaseFile();
 	}
 
 	/**
 	 * Open files and writes out headers.
 	 * @throws Exception 
 	 */
 	void prepareForCollection() throws Exception {
 		createNextFile();
 		if (file != null) {
 			writeHeader();
 		}
 	}
 
 	/**
 	 * Writes any file footers and closes file.
 	 * @throws Exception 
 	 */
 	@Override
 	public void completeCollection() throws Exception {
 		if (file != null) {
 			writeFooter();
 			releaseFile();
 		}
 		super.completeCollection();
 	}
 
 	
 
 	@Override
 	public void configureScanNumber(Long scanNumber) throws Exception {
 		if( !fileNumberConfigured){
 			if(scanNumber != null){
 				thisFileNumber = scanNumber;
 			}
 			else {
 				//if not set then generate scan number and set in point
 				try {
 					NumTracker runs = new NumTracker("tmp");
 					thisFileNumber = runs.incrementNumber();
 				} catch (IOException e) {
 					throw new InstantiationException("Could not instantiate NumTracker in IncrementalFile(): " + e.getLocalizedMessage().toString());
 				}
 			}
 			fileNumberConfigured = true;
 		} 
 	}
 
 	/**
 	 * Closes current file and opens a new file with an incremental number. For use when many files being created
 	 * instead of a single file being appended to.
 	 * @throws Exception 
 	 */
 	public void createNextFile() throws Exception {
 		try {
 			if (file != null) {
 				file.close();
 			}
 			
 			// subclasses may have already set this
 			if (this.currentFileName == null) {
 				currentFileName = getFileNumber() + "." + this.fileExtension;
 
 				this.filePrefix = LocalProperties.get("gda.data.scan.datawriter.filePrefix");
 
 				if (this.filePrefix != null) {
					currentFileName = this.filePrefix + "_" + currentFileName;
 				}
 			}
 
 			fileName = currentFileName;
 			
 			// don't use File.extension as the print out will not work in windows - and Java will know what to do with a
 			// / on Windows.
 			if (!dataDir.endsWith("/")){
 				dataDir += "/";
 			}
 			
 			fileUrl = dataDir + currentFileName;
 			try {
 				// Check to see if the file(s) already exists!
 				File f = new File(fileUrl);
 				if (f.exists()) {
 					throw new Exception("The file " + fileUrl + " already exists.");
 				}
 				File fparent = new File(f.getParent());
 				if( !fparent.exists()){
 					fparent.mkdirs();	
 				}
 				file = new FileWriter(f);
 			} catch (IOException ex1) {
 				String error = "Failed to create a new data file: " + fileUrl + " - " + ex1.getMessage();
 				exceptionUtils.logException(logger, "Failed to create a new data file: " + fileUrl, ex1);
 				currentScanController.haltCurrentScan();
 				terminalPrinter.print(error);
 			}
 			terminalPrinter.print("Writing data to file:" + fileUrl);
 		} catch (Exception ex) {
 			String error = "Failed to create a new data file: " + fileUrl + " - " + ex.getMessage();
 			exceptionUtils.logException(logger, "Failed to create a new data file: " + fileUrl, ex);
 			terminalPrinter.print(error);
 			terminalPrinter.print(ex.getMessage());
 			throw new Exception(error);
 		}
 	}
 
 	/**
 	 * Releases the file handle.
 	 */
 	public void releaseFile() {
 		try {
 			// System.err.println("Closing file.");
 			logger.info("Closing incremental file: " + file.toString());
 			file.close();
 		} catch (IOException ex) {
 		}
 		finally{
 			file = null;
 		}
 	}
 
 	/**
 	 * This should be extended by inheriting classes.
 	 */
 	public abstract void writeHeader();
 
 	/**
 	 * This should be extended by inheriting classes.
 	 */
 	public abstract void writeColumnHeadings();
 
 	/**
 	 * This should be extended by inheriting classes.
 	 */
 	public abstract void writeFooter();
 
 	/**
 	 * Returns the full path of the folder which data files are written to.
 	 * 
 	 * @return the full path of the folder which data files are written
 	 */
 	public String getDataDir() {
 		return dataDir;
 	}
 
 	/**
 	 * Get the delimiter used between columns
 	 * 
 	 * @return the delimiter used between columns
 	 */
 	public String getDelimiter() {
 		return delimiter;
 	}
 
 	/**
 	 * Set the delimiter used between columns (default is a tab '\t')
 	 * 
 	 * @param delimiter
 	 *            String
 	 */
 	public void setDelimiter(String delimiter) {
 		DataWriterBase.delimiter = delimiter;
 	}
 
 	/**
 	 * Returns the number of the last file written to.
 	 * 
 	 * @return int
 	 * @throws Exception 
 	 */
 	public long getFileNumber() throws Exception {
 		configureScanNumber(null); //ensure it has been configured
 		return thisFileNumber;
 	}
 
 	@Override
 	public String getCurrentFileName() {
 		return fileUrl;
 	}
 	
 	@Override
 	public String getCurrentScanIdentifier() {
 		return String.valueOf(thisFileNumber);
 	}
 }
