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
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Vector3d;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 
 /**
 * Class to load ADSC images. Class returns a DataHolder that is called from the ScanFileHolder class.
  */
 public class ADSCImageLoader extends AbstractFileLoader implements IMetaLoader {
 
 	private String fileName = "";
 
 	private HashMap<String, String>      metadata    = new HashMap<String, String>();
 //	public HashMap<String, Serializable> GDAMetadata = new HashMap<String, Serializable>();
 	private Vector<String> extraHeaders;
 	DetectorProperties detectorProperties;
 	DiffractionCrystalEnvironment diffractionCrystalEnvironment;
 	private boolean keepBitWidth = false;
 
 	private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss yyyy";
 
 	private DiffractionMetadata diffMetadata;
 
 	/**
 	 * @return true if loader keeps bit width of pixels
 	 */
 	public boolean isKeepBitWidth() {
 		return keepBitWidth;
 	}
 
 	/**
 	 * set loader to keep bit width of pixels
 	 * 
 	 * @param keepBitWidth
 	 */
 	public void setKeepBitWidth(boolean keepBitWidth) {
 		this.keepBitWidth = keepBitWidth;
 	}
 
 	public ADSCImageLoader() {
 		
 	}
 
 	/**
 	 * @param FileName
 	 */
 	public ADSCImageLoader(String FileName) {
 		this(FileName, false);
 	}
 
 	/**
 	 * @param FileName
 	 * @param keepBitWidth
 	 *            true if loader keeps bit width of pixels
 	 */
 	public ADSCImageLoader(String FileName, boolean keepBitWidth) {
 		setFile(FileName);
 		this.keepBitWidth = keepBitWidth;
 	}
 	
 	public void setFile(final String fileName) {
 		this.fileName = fileName;
 		// New file, new meta data 
 		metadata.clear();
 //		GDAMetadata.clear();
 	}
 
 	@Override
 	public DataHolder loadFile() throws ScanFileHolderException {
 
 		DataHolder output = new DataHolder();
 		// opens the file and reads the header information
 		RandomAccessFile raf = null;
 		try {
 
 			raf = new RandomAccessFile(fileName, "r");
 			processingMetadata(raf);
 
 		} catch (FileNotFoundException fnf) {
 			throw new ScanFileHolderException("File not found", fnf);
 		} catch (Exception e) {
 			try {
 				if (raf != null)
 					raf.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 			throw new ScanFileHolderException("There was a problem loading or reading metadata", e);
 		}
 		// Opens the file and reads the byte information and parsed them to doubles.
 		try {
 			int height = getInteger("SIZE1");
 			int width = getInteger("SIZE2");
 			int pointer = getInteger("HEADER_BYTES");
 
 			raf.seek(pointer);
 
 			int[] shape = { height, width };
 			AbstractDataset data;
 
 			// read in all the data at once for speed.
 
 			byte[] read = new byte[(height * width) * 2];
 			raf.read(read);
 
 			// and put it into the dataset
 			data = new IntegerDataset(shape);
 			int[] databuf = ((IntegerDataset) data).getData();
 			int amax = Integer.MIN_VALUE;
 			int amin = Integer.MAX_VALUE;
 			int hash = 0;
 			for (int i = 0, j = 0; i < databuf.length; i++, j += 2) {
 				int value = Utils.leInt(read[j], read[j + 1]);
 				hash = (hash * 19 + value);
 				databuf[i] = value;
 				if (value > amax) {
 					amax = value;
 				}
 				if (value < amin) {
 					amin = value;
 				}
 			}
 
 			if (keepBitWidth||amax < (1 << 15)) {
 					data = DatasetUtils.cast(data, AbstractDataset.INT16);
 			}
 
 			hash = hash*19 + data.getDtype()*17 + data.getElementsPerItem();
 			int rank = shape.length;
 			for (int i = 0; i < rank; i++) {
 				hash = hash*17 + shape[i];
 			}
 			data.setStoredValue(AbstractDataset.STORE_MAX, amax);
 			data.setStoredValue(AbstractDataset.STORE_MIN, amin);
 			data.setStoredValue(AbstractDataset.STORE_HASH, hash);
 
 			data.setName(DEF_IMAGE_NAME);
 			output.addDataset("ADSC Image", data);
 			if (loadMetadata) {
 				data.setMetadata(getMetaData());
 				output.setMetadata(data.getMetadata());
 			}
 		} catch (Exception e) {
 			throw new ScanFileHolderException("There was a problem reading the ADSC image", e);
 		} finally {
 			try {
 				raf.close();
 			} catch (IOException e) {
 				throw new ScanFileHolderException("Problem closing ADSC file", e);
 			}
 		}
 
 		return output;
 	}
 
 	/**
 	 * processing all Metadata between { and } tags at the top of the file, put it into a key-value pair map if they are
 	 * in "key=value" format. remove ; from line ending
 	 * 
 	 * @param in
 	 * @throws ScanFileHolderException
 	 */
 	private void processingMetadata(RandomAccessFile in) throws ScanFileHolderException {
 		// handling metadata in the file header
 		try {
 			byte firstChar = in.readByte();
 			in.seek(0);
 			if (firstChar != '{')
 				throw new ScanFileHolderException("This is not a valid ADSC image");
 			String line = in.readLine();
 
 			// an updated header reader which ignores all of the header.
 			while (!line.contains("{"))
 				line = in.readLine();
 
 			while (true) {
 				line = in.readLine();
 				if (line.contains("}")) {// stop at end of header
 					double[] detectorOrigin = { getDouble("BEAM_CENTER_Y"), getDouble("BEAM_CENTER_X"),
 							getDouble("DISTANCE") };
 //					createGDAMetadata(detectorOrigin);
 					createMetadata(detectorOrigin);
 					return;
 				} else if (line.contains("=")) {
 					String[] keyvalue = line.split("=");
 					metadata.put(keyvalue[0], keyvalue[1].substring(0, keyvalue[1].length() - 1));
 				} else {
 					extraHeaders.add(line);
 				}
 			}
 		} catch (IOException e) {
 			throw new ScanFileHolderException("There was a problem parsing the ADSC header information", e);
 		}
 	}
 
 //	private void createGDAMetadata(double[] detectorOrigin) throws ScanFileHolderException {
 		// NXGeometry:NXtranslation
 //		double pixelsize = getDouble("PIXEL_SIZE");
 //		double x = getInteger("SIZE1") * pixelsize;
 //		double y = getInteger("SIZE2") * pixelsize;
 		// bodge since bean centre of diamond ADSC detectors are in a DIFFERENT REFERENCE FRAME!!!!!!!!
 //		double[] detectorOrigin = { x - getDouble("BEAM_CENTER_X"), y - getDouble("BEAM_CENTER_Y"),
 //				getDouble("DISTANCE") };
 //		GDAMetadata.put("NXdetector:NXgeometery:NXtranslation", detectorOrigin);
 //		GDAMetadata.put("NXdetector:NXgeometery:NXtranslation:NXunits", "milli*meter");
 //
 //		// NXGeometery:NXOrientation
 //		double[] directionCosine = { 1, 0, 0, 0, 1, 0 }; // to form identity matrix as no header data
 //		GDAMetadata.put("NXdetector:NXgeometery:NXorientation", directionCosine);
 //		// NXGeometery:XShape (shape from origin (+x, +y, +z,0, 0, 0) > x,y,0,0,0,0)
 //		double[] detectorShape = { getDouble("SIZE1") * getDouble("PIXEL_SIZE"),
 //				getDouble("SIZE2") * getDouble("PIXEL_SIZE"), 0, 0, 0, 0 };
 //		GDAMetadata.put("NXdetector:NXgeometery:NXshape", detectorShape);
 //		GDAMetadata.put("NXdetector:NXgeometery:NXshape:NXshape", "milli*metre");
 //
 //		// NXGeometery:NXFloat
 //		double[] pixelSize = { getDouble("PIXEL_SIZE"), getDouble("PIXEL_SIZE") };
 //		GDAMetadata.put("NXdetector:x_pixel_size", pixelSize[0]);
 //		GDAMetadata.put("NXdetector:x_pixel_size:NXunits", "milli*metre");
 //		GDAMetadata.put("NXdetector:y_pixel_size", pixelSize[1]);
 //		GDAMetadata.put("NXdetector:y_pixel_size:NXunits", "milli*metre");
 //		// "NXmonochromator:wavelength"
 //		GDAMetadata.put("NXmonochromator:wavelength", getDouble("WAVELENGTH"));
 //		GDAMetadata.put("NXmonochromator:wavelength:NXunits", "Angstrom");
 //
 //		// oscillation range
 //		GDAMetadata.put("NXSample:rotation_start", getDouble("OSC_START"));
 //		GDAMetadata.put("NXSample:rotation_start:NXUnits", "degree");
 //		GDAMetadata.put("NXSample:rotation_range", getDouble("OSC_RANGE"));
 //		GDAMetadata.put("NXSample:rotation_range:NXUnits", "degree");
 //
 //		// Exposure time
 //		GDAMetadata.put("NXSample:exposure_time", getDouble("TIME"));
 //		GDAMetadata.put("NXSample:exposure_time:NXUnits", "seconds");
 //	}
 
 	private void createMetadata(double[] detectorOrigin) throws ScanFileHolderException {
 		// This is new metadata
 		Matrix3d identityMatrix = new Matrix3d();
 		identityMatrix.setIdentity();
 		detectorProperties = new DetectorProperties(new Vector3d(detectorOrigin), getInteger("SIZE1"),
 				getInteger("SIZE2"), getDouble("PIXEL_SIZE"), getDouble("PIXEL_SIZE"), identityMatrix);
 
 		diffractionCrystalEnvironment = new DiffractionCrystalEnvironment(getDouble("WAVELENGTH"),
 				getDouble("OSC_START"), getDouble("OSC_RANGE"), getDouble("TIME"));
 
 		diffMetadata = new DiffractionMetadata(fileName, detectorProperties, diffractionCrystalEnvironment);
 		diffMetadata.addDataInfo("ADSC Image", getInteger("SIZE1"), getInteger("SIZE2"));
 		diffMetadata.setMetadata(metadata);
 		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
 		try {
 			Date date = sdf.parse(metadata.get("DATE"));
 			diffMetadata.setCreationDate(date);
 		} catch (ParseException e) {
 			throw new ScanFileHolderException("Could not parse the date from the header", e);
 		}
 	}
 
 	private int getInteger(String key) throws ScanFileHolderException {
 		try {
 			return Integer.parseInt(getMetadataValue(key).trim());
 		} catch (NumberFormatException e) {
 			throw new ScanFileHolderException("There was a problem parsing integer value from string",e);
 		}
 	}
 
 	private double getDouble(String key) throws ScanFileHolderException {
 		try {
 			return Double.parseDouble(getMetadataValue(key));
 		} catch (NumberFormatException e) {
 			throw new ScanFileHolderException("There was a problem parsing double value from string",e);
 		}
 	}
 
 	private String getMetadataValue(String key) throws ScanFileHolderException {
		try {
			return metadata.get(key);
		} catch (Exception e) {
			throw new ScanFileHolderException("The keyword " + key + " was not found in the ADSC Header", e);
		}
 	}
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 		// opens the file and reads the header information
 		RandomAccessFile raf = null;
 		try {
 
 			raf = new RandomAccessFile(fileName, "r");
 			processingMetadata(raf);
 			
 		} catch (FileNotFoundException fnf) {
 			throw new ScanFileHolderException("File not found", fnf);
 		} finally{
 			if (raf != null)
 			raf.close();
 		}
 	}
 
 	@Override
 	public IMetaData getMetaData() {
 		return diffMetadata;
 	}
 }
