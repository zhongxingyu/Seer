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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.DoubleBuffer;
 import java.nio.IntBuffer;
 import java.util.HashMap;
 
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Vector3d;
 
 import org.iucr.cbflib.SWIGTYPE_p_p_char;
 import org.iucr.cbflib.cbf;
 import org.iucr.cbflib.cbfConstants;
 import org.iucr.cbflib.cbf_handle_struct;
 import org.iucr.cbflib.intP;
 import org.iucr.cbflib.sizetP;
 import org.iucr.cbflib.uintP;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.CBFlib.CBFlib;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * Crystallographic Binary File (CBF) and image-supporting Crystallographic Information File (imgCIF) loader
  *
  * TODO remove Nexus dependency
  */
 public class CBFLoader extends AbstractFileLoader implements IMetaLoader {
 	transient protected static final Logger logger = LoggerFactory.getLogger(CBFLoader.class);
 	private String fileName = null;
 	private HashMap<String, String> metadata = new HashMap<String, String>();
 	public HashMap<String, Serializable> GDAMetadata = new HashMap<String, Serializable>();
 	private Metadata diffMetadata;
 
 	static {
 		CBFlib.loadLibrary();
 	}
 
 	public CBFLoader() {
 	
 	}
 
 	/**
 	 * @param FileName
 	 */
 	public CBFLoader(String FileName) {
 		fileName = FileName;
 	}
 
 	public void setFile(final String fileName) {
 		this.fileName = fileName;
 	}
 
 	@Override
 	public DataHolder loadFile() throws ScanFileHolderException {
 		DataHolder output = new DataHolder();
 		AbstractDataset data = null;
 		ImageOrientation imageOrien = null;
 
 		logger.info("Loading {}", fileName);
 
 		cbf_handle_struct chs = new cbf_handle_struct(fileName);
 
 		CBFError.errorChecker(cbf.cbf_rewind_datablock(chs));
 
 		if (CBFError.errorChecker(cbf.cbf_find_category(chs, "diffrn_frame_data"))
 				|| CBFError.errorChecker(cbf.cbf_find_category(chs, "diffrn_data_frame"))) {
 			imageOrien = readCBFHeaderData(chs);
 		} else {
 			if (loadMetadata) {
 				imageOrien = readMiniCBFHeader(chs);
 			} else {
 				boolean status = true;
 				status &= CBFError.errorChecker(cbf.cbf_find_category(chs, "array_data"));
 				status &= CBFError.errorChecker(cbf.cbf_find_column(chs, "data"));
 				if (!status) {
 					throw new ScanFileHolderException("Could not find image");
 				}
 			}
 		}
 
 		data = readCBFBinaryData(chs, imageOrien);
 		data.setName(DEF_IMAGE_NAME);
 
 		chs.delete(); // this also closes the file
 
 		output.addDataset(fileName, data);
 		if (loadMetadata) {
 			data.setMetadata(getMetaData());
 			output.setMetadata(data.getMetadata());
 		}
 		return output;
 	}
 
 	static String[] miniCBFheaderNames = { "Pixel_size", "Silicon sensor, thickness", "Exposure_time",
 			"Exposure_period", "Tau =", "Count_cutoff", "Threshold_setting,", "N_excluded_pixels =",
 			"Excluded_pixels:", "Flat_field:", "Trim_directory:", "Wavelength", "Energy_range",
 			"Detector_distance", "Detector_Voffset", "Beam_xy", "Flux", "Filter_transmission",
 			"Start_angle", "Angle_increment", "Detector_2theta", "Polarization", "Alpha", "Kappa", "Phi",
 			"Chi", "Oscillation_axis", "N_oscillations" };
 
 	private ImageOrientation readMiniCBFHeader(cbf_handle_struct chs) throws ScanFileHolderException {
 		SWIGTYPE_p_p_char s = cbf.new_charPP();
 //		String convention;
 
 		CBFError.errorChecker(cbf.cbf_rewind_datablock(chs));
 		CBFError.errorChecker(cbf.cbf_find_category(chs, "array_data"));
 //		CBFError.errorChecker(cbf.cbf_find_column(chs, "header_convention"));
 //		CBFError.errorChecker(cbf.cbf_get_value(chs, s));
 //		convention = cbf.charPP_value(s);
 //		if (!convention.equalsIgnoreCase("SLS_1.0")) {
 //			throw new ScanFileHolderException("The miniCBF header convention in not recognised");
 //		}
 
 		CBFError.errorChecker(cbf.cbf_find_column(chs, "header_contents"));
 		CBFError.errorChecker(cbf.cbf_get_value(chs, s));
 		String header = new String(cbf.charPP_value(s));
 
 		BufferedReader in = new BufferedReader(new StringReader(header));
 		String temp;
 		int unknownNum = 0;
 		boolean found = false;
 		try {
 			while ((temp = in.readLine()) != null) {
 				if (temp.length() == 0)
 					continue;
 				for (int j = 0; j < miniCBFheaderNames.length; j++) {
 					found = false;
 					if (temp.startsWith(miniCBFheaderNames[j], 2)) {
 						metadata.put(miniCBFheaderNames[j], temp.substring(
 								temp.indexOf(miniCBFheaderNames[j]) + miniCBFheaderNames[j].length(), temp.length())
 								.trim());
 						found = true;
 						break;
 					}
 				}
 				if (!found) {
 					metadata.put("Unknown " + unknownNum, temp);
 					unknownNum++;
 				}
 			}
 		} catch (IOException e) {
 			throw new ScanFileHolderException("Error parsing miniCBF header ", e);
 		}
 
 		// get image data
 		CBFError.errorChecker(cbf.cbf_find_tag(chs, "_array_data.data"));
 		CBFError.errorChecker(cbf.cbf_rewind_row(chs));
 
 		uintP cifcomp = new uintP();
 		intP bid = new intP(), els = new intP(), elu = new intP();
 		intP minel = new intP(), maxel = new intP(), isre = new intP();
 		sizetP elsize = new sizetP(), elnum = new sizetP();
 		sizetP dim1 = new sizetP(), dim2 = new sizetP(), dim3 = new sizetP(), pad = new sizetP();
 		SWIGTYPE_p_p_char byteorder = cbf.new_charPP();
 		CBFError.errorChecker(cbf.cbf_get_arrayparameters_wdims(chs, cifcomp.cast(), bid.cast(), elsize.cast(),
 				els.cast(), elu.cast(), elnum.cast(), minel.cast(), maxel.cast(), isre.cast(), byteorder,
 				dim1.cast(), dim2.cast(), dim3.cast(), pad.cast()));
 
 		int xDimension = (int) dim1.value();
 		int yDimension = (int) dim2.value();
 		metadata.put("numPixels_x", Integer.toString(xDimension));
 		metadata.put("numPixels_y", Integer.toString(yDimension));
 
 		cifcomp.delete();
 		bid.delete(); els.delete(); elu.delete();
 		minel.delete(); maxel.delete(); isre.delete();
 		elsize.delete(); elnum.delete();
 		dim1.delete(); dim2.delete(); dim3.delete(); pad.delete();
 
 		// parse metadata from miniCBF to GDA
 		createGDAMetadata();
 
 		return new ImageOrientation(xDimension, yDimension);
 	}
 	
 	private void createGDAMetadata() throws ScanFileHolderException {
 		try {
 			String pixelSize = getMetadataValue("Pixel_size");
 			String[] xypixVal = pixelSize.split("m x");
 			double xPxVal = Double.parseDouble(xypixVal[0])*1000;
 			double yPXVal = Double.parseDouble(xypixVal[1].split("m")[0])*1000;
 			
 			String tmp = getMetadataValue("Beam_xy");
 			String [] beamxy = tmp.split(",");
 			double beamPosX = Double.parseDouble(beamxy[0].split("\\(")[1]);
 			double beamPosY = Double.parseDouble(beamxy[1].split("\\)")[0]);
 			
 			
 			// NXGeometery:NXtranslation
 //			double[] detectorOrigin = { 
 //					(Double.parseDouble(getMetadataValue("numPixels_x")) - beamPosX)* xPxVal ,
 //					(Double.parseDouble(getMetadataValue("numPixels_y")) - beamPosY)* yPXVal ,
 //					Double.parseDouble(getMetadataValue("Detector_distance").split("m")[0])*1000 };
 			double[] detectorOrigin = {  beamPosX* xPxVal, beamPosY* yPXVal ,
 					getFirstDouble("Detector_distance", "m")*1000 };
 			GDAMetadata.put("NXdetector:NXgeometery:NXtranslation", detectorOrigin);
 			//System.out.println(detectorOrigin[0] +"  "+detectorOrigin[1]+"   "+detectorOrigin[2]);
 			GDAMetadata.put("NXdetector:NXgeometery:NXtranslation:NXunits", "milli*metre");
 			
 			// NXGeometery:NXOrientation
 			double [] directionCosine = {1,0,0,0,1,0}; // to form identity matrix as no header data
 			GDAMetadata.put("NXdetector:NXgeometery:NXorientation",directionCosine);
 			
 			// NXGeometery:XShape (shape from origin (+x, +y, +z,0, 0, 0) > x,y,0,0,0,0)
 			double[] detectorShape = {
 					getDouble("numPixels_x") * xPxVal,
 					getDouble("numPixels_y") * yPXVal,0,0,0,0 };
 			GDAMetadata.put("NXdetector:NXgeometery:NXshape", detectorShape);
 			GDAMetadata.put("NXdetector:NXgeometery:NXshape:NXunits","milli*metre");
 			
 			// NXGeometery:NXFloat
 			GDAMetadata.put("NXdetector:x_pixel_size", xPxVal);
 			GDAMetadata.put("NXdetector:x_pixel_size:NXunits", "milli*metre");
 			GDAMetadata.put("NXdetector:y_pixel_size", yPXVal);
 			GDAMetadata.put("NXdetector:y_pixel_size:NXunits", "milli*metre");
 
 			// "NXmonochromator:wavelength"
 			double lambda;
 			if (getMetadataValue("Wavelength").contains("A"))
 				lambda = getFirstDouble("Wavelength", "A");
 			else if(getMetadataValue("Wavelength").contains("nm"))
 				lambda = getFirstDouble("Wavelength", "nm")*10;
 			else
 				throw new ScanFileHolderException("The wavelength could not be parsed in from the mini cbf file header");
 			GDAMetadata.put("NXmonochromator:wavelength",lambda);
 			GDAMetadata.put("NXmonochromator:wavelength:NXunits", "Angstrom");
 
 			// oscillation range
 			GDAMetadata.put("NXSample:rotation_start", getFirstDouble("Start_angle", "deg"));
 			GDAMetadata.put("NXSample:rotation_start:NXUnits","degree");
 			GDAMetadata.put("NXSample:rotation_range", getFirstDouble("Angle_increment", "deg"));
 			GDAMetadata.put("NXSample:rotation_range:NXUnits", "degree");
 			
 			//Exposure time
 			GDAMetadata.put("NXSample:exposure_time", getFirstDouble("Exposure_time", "s"));
 			GDAMetadata.put("NXSample:exposure_time:NXUnits", "seconds");
 			
 			GDAMetadata.put("NXdetector:pixel_overload", getFirstDouble("Count_cutoff", "counts"));
 			GDAMetadata.put("NXdetector:pixel_overload:NXUnits", "counts");
 			
 			createMetadata(detectorOrigin, xPxVal, yPXVal, lambda);
 		} catch (NumberFormatException e) {
 			throw new ScanFileHolderException("There was a problem parsing numerical value from string ",e);
 		}
 	}
 
 	private void createMetadata(double[] detectorOrigin, double xPxVal, double yPXVal, double lambda) {
 		try {
 			// This is new metadata
 			Matrix3d identityMatrix = new Matrix3d();
 			identityMatrix.setIdentity();
 			DetectorProperties detectorProperties = new DetectorProperties(new Vector3d(detectorOrigin),
 					getInteger("numPixels_x"), getInteger("numPixels_y"), xPxVal, yPXVal, identityMatrix);
 
 			DiffractionCrystalEnvironment diffractionCrystalEnvironment = new DiffractionCrystalEnvironment(lambda,
 					getFirstDouble("Start_angle", "deg"), getFirstDouble("Angle_increment", "deg"), getFirstDouble(
 							"Exposure_time", "s"));
 
 			diffMetadata = new DiffractionMetadata(fileName, detectorProperties, diffractionCrystalEnvironment);
 			diffMetadata.setMetadata(metadata);
 		} catch (ScanFileHolderException e) {
 			diffMetadata = new Metadata(metadata);
 		}
 		diffMetadata.setFilePath(fileName);
 	}
 
 	private ImageOrientation readCBFHeaderData(cbf_handle_struct chs)throws ScanFileHolderException{
 		
 		int xLength = 0;
 		int yLength = 0;
 		boolean xIncreasing = false;
 		boolean yIncreasing = false;
 		boolean isRowsX = false;
 		
 		CBFError.errorChecker(cbf.cbf_find_column(chs, "array_id"));
 		SWIGTYPE_p_p_char s = cbf.new_charPP();
 		intP ip = new intP();
 		CBFError.errorChecker(cbf.cbf_get_value(chs, s));
 		String arrayid = new String(cbf.charPP_value(s));
 		metadata.put("diffrn_data_frame.array_id", arrayid);
 
 		// get the image dimensions
 		CBFError.errorChecker(cbf.cbf_find_category(chs, "array_structure_list"));
 		CBFError.errorChecker(cbf.cbf_rewind_row(chs));
 		CBFError.errorChecker(cbf.cbf_find_column(chs, "array_id"));
 
 		// Attempt to find rows that matches above array_id
 		int index = 0;
 		int precedence = 0;
 		String direction;
 		String axis_set_id;
 
 		int status;
 		while ((status = cbf.cbf_find_nextrow(chs, arrayid)) == 0) {
 
 			CBFError.errorChecker(cbf.cbf_find_column(chs, "index"));
 			CBFError.errorChecker(cbf.cbf_get_integervalue(chs, ip.cast()));
 			index = ip.value();
 
 			CBFError.errorChecker(cbf.cbf_find_column(chs, "dimension"));
 			CBFError.errorChecker(cbf.cbf_get_integervalue(chs, ip.cast()));
 			metadata.put("SIZE " + String.valueOf(index), String.valueOf(ip.value()));
 
 			CBFError.errorChecker(cbf.cbf_find_column(chs, "precedence"));
 			CBFError.errorChecker(cbf.cbf_get_integervalue(chs, ip.cast()));
 			precedence = ip.value();
 			metadata.put("precedence " + index, String.valueOf(precedence));
 
 			CBFError.errorChecker(cbf.cbf_find_column(chs, "direction"));
 			CBFError.errorChecker(cbf.cbf_get_value(chs, s));
 			direction = new String(cbf.charPP_value(s));
 			metadata.put("direction " + index, direction);
 
 			CBFError.errorChecker(cbf.cbf_find_column(chs, "axis_set_id"));
 			CBFError.errorChecker(cbf.cbf_get_value(chs, s));
 			axis_set_id = new String(cbf.charPP_value(s));
 			metadata.put("axis_set_id " + index, axis_set_id);
 
 			//System.out.println("ind: " + index + ", dim: " + dimension + ", prec: " + precedence + ", dir: "
 			//		+ direction + ", axis: " + axis_set_id);
 
 			CBFError.errorChecker(cbf.cbf_find_column(chs, "array_id"));
 		}
 
 		ip.delete();
 
 		if (status != cbfConstants.CBF_NOTFOUND) {
 			CBFError.errorChecker(status);
 		}
 
 		CBFError.errorChecker(cbf.cbf_find_category(chs, "array_data"));
 		CBFError.errorChecker(cbf.cbf_find_column(chs, "array_id"));
 		CBFError.errorChecker(cbf.cbf_find_row(chs, getMetadataValue("diffrn_data_frame.array_id")));
 		CBFError.errorChecker(cbf.cbf_find_column(chs, "data"));
 
 		if (isMatch("axis_set_id 1", "ELEMENT_X")) { // FIXME is this always the case?
 			isRowsX = getInteger("precedence 1") == 1;
 
 			xLength = getInteger("SIZE 1");
 			yLength = getInteger("SIZE 2");
 
 			xIncreasing = isMatch("direction 1", "increasing");
 			yIncreasing = isMatch("direction 2", "increasing");
 		} else {
 			isRowsX = getInteger("precedence 2") == 1;
 
 			xLength = getInteger("SIZE 2");
 			yLength = getInteger("SIZE 1");
 
 			xIncreasing = isMatch("direction 2", "increasing");
 			yIncreasing = isMatch("direction 1", "increasing");
 		}
 
 		return new ImageOrientation(xLength, yLength, xIncreasing, yIncreasing, isRowsX);
 	}
 	
 	private AbstractDataset readCBFBinaryData(cbf_handle_struct chs, ImageOrientation imageOrien) throws ScanFileHolderException {
 		AbstractDataset data = null;
 
 
 			// change array_data to given dimensions
 	//		NexusTreeNode anode = ((NexusTreeNode) tree).findNode("array_data");
 	//		if (anode != null) {
 	//			anode = anode.findNode("row");
 	//			if (anode != null) {
 	//				NexusGroupData adata = anode.getData();
 	//				if (adata != null) {
 	//					adata.dimensions = new int[] { yLength, xLength };
 	//				}
 	//			}
 	//		}
 
 		uintP cifcomp = new uintP();
 		intP bid = new intP(), els = new intP(), elu = new intP();
 		intP minel = new intP(), maxel = new intP(), isre = new intP();
 		sizetP elsize = new sizetP(), elnum = new sizetP();
 		sizetP dim1 = new sizetP(), dim2 = new sizetP(), dim3 = new sizetP(), pad = new sizetP();
 		SWIGTYPE_p_p_char byteorder = cbf.new_charPP();
 
 		CBFError.errorChecker(cbf.cbf_get_arrayparameters_wdims(chs, cifcomp.cast(), bid.cast(), elsize.cast(), els
 				.cast(), elu.cast(), elnum.cast(), minel.cast(), maxel.cast(), isre.cast(), byteorder, dim1.cast(),
 				dim2.cast(), dim3.cast(), pad.cast()));
 
 		if (imageOrien == null) {
 			imageOrien = new ImageOrientation((int) dim1.value(), (int) dim2.value());
 		}
 
 		int xLength = imageOrien.getXLength();
 		int yLength = imageOrien.getYLength();
 		boolean xIncreasing = imageOrien.isXIncreasing();
 		boolean yIncreasing = imageOrien.isYIncreasing();
 		boolean isRowsX = imageOrien.isRowsX();
 
 		boolean isreal = (isre.value() == 1);
 		int numPixels = xLength * yLength;
 
 		if (numPixels != elnum.value()) {
 			throw new ScanFileHolderException("Mismatch of CBF binary data size");
 		}
 
 		cifcomp.delete();
 		minel.delete();
 		maxel.delete();
 		isre.delete();
 		elsize.delete();
 		elnum.delete();
 		dim1.delete();
 		dim2.delete();
 		dim3.delete();
 		pad.delete();
 
 		sizetP rsize = new sizetP();
 
 //		System.out.println("Loading " + fileName + ", " + numPixels);
 
 		// remember to explicitly delete arrays allocated on the JNI side
 		// as finalize method is up to garbage collector and is tardily done
 
 		// TODO add smaller data type support (with sign extension ala NeXus)
 		// deal with floating point data differently than integer data
 
 		int stride1; // stride is change in position on n-th dim
 		int stride2;
 		int start;  // start is offset in position
 
 		if (!isRowsX) { // swap row and column directions
 			boolean b = yIncreasing;
 			yIncreasing = !xIncreasing;
 			xIncreasing = !b;
 		}
 
 		if (!yIncreasing) { // note that image in GDA is plotted so Y increases from top to bottom
 			stride1 = xLength;
 			start = 0;
 		} else {
 			stride1 = -xLength;
 			start = xLength*yLength - xLength;
 		}
 
 		if (xIncreasing) {
 			stride2 = 1;
 		} else {
 			stride2 = -1;
 			start += xLength - 1;
 		}
 
 		int rows;
 		int cols;
 		int rstep;
 		int cstep;
 
 		if (isRowsX) {
 			rows = yLength;
 			cols = xLength;
 			rstep = stride1;
 			cstep = stride2;
 		} else {
 			rows = xLength;
 			cols = yLength;
 			rstep = stride2;
 			cstep = stride1;
 		}
 
 		int index = 0; // index in destination
 		int position = 0; // position in buffer
 		int hash = 0;
 		if (isreal) {
 			DoubleBuffer ddata;
 			try {
 				ddata = ByteBuffer.allocateDirect(numPixels * 8).order(ByteOrder.nativeOrder()).asDoubleBuffer();
 			} catch (OutOfMemoryError e) {
 				throw new ScanFileHolderException("CBFloader failed to while allocating the byte buffer ", e);
 			} catch (Exception eb) {
 				throw new ScanFileHolderException("CBFloader failed to while allocating the byte buffer ", eb);
 			}
 
 			CBFError.errorChecker(cbf.cbf_get_realarray(chs, bid.cast(), ddata, (Double.SIZE / 8), numPixels,
 					rsize.cast()));
 
 			if (numPixels != rsize.value()) {
 				throw new ScanFileHolderException("Mismatch of CBF binary data size");
 			}
 
 			try {
 				data = new DoubleDataset(yLength, xLength);
 			} catch (OutOfMemoryError e) {
 				throw new ScanFileHolderException("CBFLoader failed when creating a DoubleDataset for the data", e);
 			} catch (Exception eb) {
 				throw new ScanFileHolderException("CBFLoader failed when creating a DoubleDataset for the data", eb);
 			}
 			
 			double[] dArray = ((DoubleDataset) data).getData();
 
 			// map from CBF data to dataset
 			double amax = -Double.MAX_VALUE;
 			double amin = Double.MAX_VALUE;
 			double dhash = 0;
 			for (int j = 0; j < rows; j++) {
 				position = start;
 				for (int i = 0; i < cols; i++) {
 					double value = ddata.get(position);
 					position += cstep;
 					if (Double.isInfinite(value) || Double.isNaN(value))
 						dhash = (dhash * 19) % Integer.MAX_VALUE;
 					else
 						dhash = (dhash * 19 + value) % Integer.MAX_VALUE;
 					dArray[index++] = value;
 					if (value > amax) {
 						amax = value;
 					}
 					if (value < amin) {
 						amin = value;
 					}
 				}
 				start += rstep;
 			}
 			hash = (int) dhash;
 			data.setStoredValue("max", amax);
 			data.setStoredValue("min", amin);
 			ddata = null;
 		} else {
 			IntBuffer idata;
 			try {
 				idata = ByteBuffer.allocateDirect(numPixels * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
 			} catch (OutOfMemoryError e) {
 				throw new ScanFileHolderException("CBFLoader failed to allocate bytebuffer (intbuffer)",e);
 			} catch (Exception eb) {
 				throw new ScanFileHolderException("CBFLoader failed to allocate bytebuffer (intbuffer)",eb);
 			}
 			
 
 			CBFError.errorChecker(cbf.cbf_get_integerarray(chs, bid.cast(), idata, (Integer.SIZE / 8), els.value(),
 					numPixels, rsize.cast()));
 
 			if (numPixels != rsize.value()) {
 				throw new ScanFileHolderException("Mismatch of CBF binary data size");
 			}
 
 			try {
 				data = new IntegerDataset(yLength, xLength);
 			} catch (OutOfMemoryError e) {
 				throw new ScanFileHolderException("Could not assign IntegerDataset", e);
 			} catch (Exception eb) {
 				throw new ScanFileHolderException("Could not assign IntegerDataset", eb);
 			}
 			
 			int[] dArray = ((IntegerDataset) data).getData();
 			int amax = Integer.MIN_VALUE;
 			int amin = Integer.MAX_VALUE;
 
 			for (int j = 0; j < rows; j++) {
 				position = start;
 				for (int i = 0; i < cols; i++) {
 					int value = idata.get(position);
 					position += cstep;
 					hash = hash * 19 + value;
 					dArray[index++] = value;
 					if (value > amax) {
 						amax = value;
 					}
 					if (value < amin) {
 						amin = value;
 					}
 				}
 				start += rstep;
 			}
 
 			data.setStoredValue("max", amax);
 			data.setStoredValue("min", amin);
 			idata = null;
 		}
 
 		rsize.delete();
 		bid.delete();
 		els.delete();
 		elu.delete();
 
 		hash = hash*19 + data.getDtype()*17 + data.getElementsPerItem();
 		int[] shape = data.getShape();
 		int rank = shape.length;
 		for (int i = 0; i < rank; i++) {
 			hash = hash*17 + shape[i];
 		}
 		data.setStoredValue("hash", hash);
 
 		return data;
 	}
 
 	/**
 	 * @param chs
 	 * @param fileName
 	 * @return Completed NexusTree
 	 * @throws ScanFileHolderException
 	 */
 //	private INexusTree CBFMetaDataLoader(cbf_handle_struct chs, String fileName) throws ScanFileHolderException {
 //		CBFError.errorChecker(cbf.cbf_rewind_datablock(chs));
 //
 //		INexusTree root = new NexusTreeNode(fileName, "NXroot", null);
 //
 //		uintP blocks = new uintP();
 //		CBFError.errorChecker(cbf.cbf_count_datablocks(chs, blocks.cast()));
 //		int bmax = (int) blocks.value();
 //		blocks.delete();
 //
 //		for (int blockn = 0; blockn < bmax; blockn++) {
 //			SWIGTYPE_p_p_char name = cbf.new_charPP();
 //			CBFError.errorChecker(cbf.cbf_select_datablock(chs, blockn));
 //			CBFError.errorChecker(cbf.cbf_datablock_name(chs, name));
 //
 //			INexusTree child = new NexusTreeNode("NXcif_" + cbf.charPP_value(name), "NXentry", null);
 //			root.addChildNode(child);  // can't do in child's constructor as it doesn't update parent
 //
 //			int[] itemtype = { 0 };
 //			if (CBFError.errorChecker(cbf.cbf_rewind_blockitem(chs, itemtype))) {
 //				uintP items = new uintP();
 //				CBFError.errorChecker(cbf.cbf_count_blockitems(chs, items.cast()));
 //				int imax = (int) items.value();
 //				items.delete();
 //
 //				for (int itemn = 0; itemn < imax; itemn++) {
 //					CBFError.errorChecker(cbf.cbf_select_blockitem(chs, itemn, itemtype));
 //					if (itemtype[0] == CBF_NODETYPE.CBF_CATEGORY.swigValue()) {
 //						process_category(chs, child);
 //					} else {
 //						SWIGTYPE_p_p_char frameName = cbf.new_charPP();
 //
 //						CBFError.errorChecker(cbf.cbf_saveframe_name(chs, frameName));
 //						// make new save frame group
 //						INexusTree sfNode = new NexusTreeNode(new String(cbf.charPP_value(frameName)), "saveframe", null);
 //						if (CBFError.errorChecker(cbf.cbf_rewind_category(chs))) {
 //							uintP cats = new uintP();
 //							CBFError.errorChecker(cbf.cbf_count_categories(chs, cats.cast()));
 //							int cmax = (int) cats.value();
 //							cats.delete();
 //
 //							for (int catn = 0; catn < cmax; catn++) {
 //								CBFError.errorChecker(cbf.cbf_select_category(chs, catn));
 //								process_category(chs, sfNode);
 //							}
 //						}
 //						child.addChildNode(sfNode);
 //					}
 //				}
 //			}
 //		}
 //		return root;
 //	}
 
 	/**
 	 * make NXgroup by processing columns of category
 	 * 
 	 * @throws ScanFileHolderException
 	 */
 //	private void process_category(cbf_handle_struct chs, INexusTree parent) throws ScanFileHolderException {
 //		SWIGTYPE_p_p_char catName = cbf.new_charPP();
 //
 //		CBFError.errorChecker(cbf.cbf_category_name(chs, catName));
 //		// make new category group
 //		INexusTree catNode = new NexusTreeNode(new String(cbf.charPP_value(catName)), "category", null);
 //
 //		uintP rows = new uintP();
 //		CBFError.errorChecker(cbf.cbf_count_rows(chs, rows.cast()));
 //		int rmax = (int) rows.value();
 //		rows.delete();
 //
 //		if (rmax != 0) {
 //			boolean somebinary;
 //
 //			uintP cols = new uintP();
 //			CBFError.errorChecker(cbf.cbf_count_columns(chs, cols.cast()));
 //			int cmax = (int) cols.value();
 //			cols.delete();
 //
 //			if (cmax != 0 && CBFError.errorChecker(cbf.cbf_rewind_column(chs))) {
 //				SWIGTYPE_p_p_char colName = cbf.new_charPP();
 //				do {
 //					CBFError.errorChecker(cbf.cbf_column_name(chs, colName));
 //					somebinary = false;
 //					CBFError.errorChecker(cbf.cbf_rewind_row(chs));
 //					String[] vtype = new String[rmax];
 //					String[] value = new String[rmax];
 //					SWIGTYPE_p_p_char vtemp = cbf.new_charPP();
 //
 //					// transfer column from cif to Nexus
 //					for (int rown = 0; rown < rmax; rown++) {
 //						CBFError.errorChecker(cbf.cbf_select_row(chs, rown));
 //						int status;
 //						if ((status = cbf.cbf_get_value(chs, vtemp)) == 0) {
 //							value[rown] = new String(cbf.charPP_value(vtemp));
 //							CBFError.errorChecker(cbf.cbf_get_typeofvalue(chs, vtemp));
 //							vtype[rown] = new String(cbf.charPP_value(vtemp));
 //							if (value[rown] == null) {
 //								value[rown] = ".";
 //								vtype[rown] = "null";
 //							}
 //							if (vtype[rown] == null) {
 //								vtype[rown] = "undefined";
 //							}
 //							if (somebinary) {
 //								String data = formatdata(value[rown], vtype[rown]);
 //								int[] dims = { data.length() };
 //								NexusGroupData gdata = new NexusGroupData(dims, NexusFile.NX_CHAR, data);
 //								catNode.addChildNode(new NexusTreeNode("row", "data", catNode, gdata));
 //							}
 //						} else if (status == cbfConstants.CBF_BINARY) {
 //							if (!somebinary) {
 //								INexusTree colNode = new NexusTreeNode(new String(cbf.charPP_value(colName)), "column", catNode);
 //
 //								for (int irow = 0; irow < rown; irow++) {
 //									String data = formatdata(value[irow], vtype[irow]);
 //									int[] dims = { data.length() };
 //									NexusGroupData gdata = new NexusGroupData(dims, NexusFile.NX_CHAR, data);
 //									colNode.addChildNode(new NexusTreeNode("row" + irow, "data", catNode, gdata));
 //								}
 //								somebinary = true;
 //								catNode.addChildNode(colNode);
 //							}
 //
 //							uintP cifcomp = new uintP();
 //							intP bid = new intP(), els = new intP(), elu = new intP();
 //							intP minel = new intP(), maxel = new intP(), isre = new intP();
 //							sizetP elsize = new sizetP(), elnum = new sizetP();
 //							sizetP dim1 = new sizetP(), dim2 = new sizetP(), dim3 = new sizetP(), pad = new sizetP();
 //							SWIGTYPE_p_p_char byteorder = cbf.new_charPP();
 //
 //							CBFError.errorChecker(cbf.cbf_get_arrayparameters_wdims(chs, cifcomp.cast(), bid.cast(),
 //									elsize.cast(), els.cast(), elu.cast(), elnum.cast(), minel.cast(), maxel.cast(),
 //									isre.cast(), byteorder, dim1.cast(), dim2.cast(), dim3.cast(), pad.cast()));
 //
 //							// load and place in data array (perhaps do this in other part of Loader)
 //							boolean isreal = (isre.value() == 1);
 //							sizetP rsize = new sizetP();
 //							Serializable odata = null;
 //							int elmax = (int) elnum.value();
 //							int elbytes = (int) elsize.value();
 //							int nxtype = NexusFile.NX_UINT8;
 //							ByteBuffer bdata = ByteBuffer.allocateDirect(elmax*elbytes).order(ByteOrder.nativeOrder());
 //							if (isreal) {
 //
 //								CBFError.errorChecker(cbf.cbf_get_realarray(chs, bid.cast(), bdata,
 //										elbytes, elmax, rsize.cast()));
 //
 //								switch ((int) elsize.value()) {
 //								case 4:
 //									nxtype = NexusFile.NX_FLOAT32;
 //									odata = bdata.asFloatBuffer().array();
 //									break;
 //								case 8:
 //									nxtype = NexusFile.NX_FLOAT64;
 //									odata = bdata.asDoubleBuffer().array();
 //									break;
 //								}
 //							} else {
 //								CBFError.errorChecker(cbf.cbf_get_integerarray(chs, bid.cast(), bdata,
 //										elbytes, els.value(), elmax, rsize.cast()));
 //
 //								if (elu.value() != 0) {
 //									switch ((int) elsize.value()) {
 //									case 1:
 //										nxtype = NexusFile.NX_UINT8;
 //										odata = bdata.array();
 //										break;
 //									case 2:
 //										nxtype = NexusFile.NX_UINT16;
 //										odata = bdata.asShortBuffer().array();
 //										break;
 //									case 4:
 //										nxtype = NexusFile.NX_UINT32;
 //										odata = bdata.asIntBuffer().array();
 //										break;
 //									case 8:
 //										nxtype = NexusFile.NX_UINT64;
 //										odata = bdata.asLongBuffer().array();
 //										break;
 //									}
 //								} else {
 //									switch ((int) elsize.value()) {
 //									case 1:
 //										nxtype = NexusFile.NX_INT8;
 //										odata = bdata.array();
 //										break;
 //									case 2:
 //										nxtype = NexusFile.NX_INT16;
 //										odata = bdata.asShortBuffer().array();
 //										break;
 //									case 4:
 //										nxtype = NexusFile.NX_INT32;
 //										odata = bdata.asIntBuffer().array();
 //										break;
 //									case 8:
 //										nxtype = NexusFile.NX_INT64;
 //										odata = bdata.asLongBuffer().array();
 //										break;
 //									}
 //								}
 //							}
 //
 //							// sanity check dimension values as they could be zero
 //							int[] tdims = { (int) dim1.value(), (int) dim2.value(), (int) dim3.value() };
 //							int rank = 1;
 //							for (int d = 0; d < tdims.length; d++) {
 //								if (tdims[d] == 0)
 //									tdims[d] = 1;
 //								else if (tdims[d] > 1)
 //									rank = d + 1;
 //							}
 //							if (rank == 1 && tdims[0] != elmax)
 //								tdims[0] = elmax;
 //
 //							int[] dims = new int[rank];
 //							for (int d = 0; d < rank; d++) {
 //								dims[d] = tdims[d];
 //							}
 //
 //							rsize.delete();
 //							cifcomp.delete();
 //							bid.delete(); els.delete(); elu.delete();
 //							minel.delete(); maxel.delete(); isre.delete();
 //							elsize.delete(); elnum.delete();
 //							dim1.delete(); dim2.delete(); dim3.delete(); pad.delete();
 //
 //							catNode.addChildNode(new NexusTreeNode("row", "data", catNode, new NexusGroupData(dims,
 //									nxtype, odata)));
 //						}
 //					}
 //
 //					if (!somebinary) {
 //						int ctotal = 0;
 //						String tdata = "";
 //						if (rmax >= 1) {
 //							tdata += formatdata(value[0], vtype[0]);
 //							ctotal = tdata.length();
 //							for (int rown = 1; rown < rmax; rown++) {
 //								String data = "\n" + formatdata(value[rown], vtype[rown]);
 //								tdata += data;
 //								ctotal += data.length();
 //							}
 //						}
 //						if (ctotal != tdata.length()) {
 //							throw new ScanFileHolderException("Mismatch of CBF character data size");
 //						}
 //
 //						int[] dims = { tdata.length() };
 //						NexusGroupData gdata = new NexusGroupData(dims, NexusFile.NX_CHAR, tdata);
 //						catNode.addChildNode(new NexusTreeNode(new String(cbf.charPP_value(colName)), "column", catNode, gdata));
 //					}
 //				} while (CBFError.errorChecker(cbf.cbf_next_column(chs)));
 //			}
 //		}
 //		parent.addChildNode(catNode);
 //	}
 
 	/**
 	 * 
 	 */
 //	private String formatdata(String value, String type) {
 //		if (type.compareToIgnoreCase("sqlq") == 0) {
 //			return "\'" + value + "\'";
 //		} else if (type.compareToIgnoreCase("dblq") == 0) {
 //			return "\"" + value + "\"";
 //		} else if (type.compareToIgnoreCase("text") == 0) {
 //			return ";" + value + "\n;";
 //		} else if (type.length() == 0) {
 //			return ".";
 //		} else {
 //			return value;
 //		}
 //	}
 	
 
 	private int getInteger(String key) throws ScanFileHolderException {
 		try {
 			return Integer.parseInt(getMetadataValue(key));
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
 
 	private double getFirstDouble(String key, String split) throws ScanFileHolderException {
 		try {
 			return Double.parseDouble(getMetadataValue(key).split(split)[0]);
 		} catch (NumberFormatException e) {
 			throw new ScanFileHolderException("There was a problem parsing double value from string",e);
 		}
 	}
 
 	private boolean isMatch(String key, String value) throws ScanFileHolderException {
 		try {
 			return getMetadataValue(key).equalsIgnoreCase(value);
 		} catch (NumberFormatException e) {
 			throw new ScanFileHolderException("There was a problem parsing double value from string",e);
 		}
 	}
 
 	private String getMetadataValue(String key) throws ScanFileHolderException {
 		try {
 			String value = metadata.get(key);
 			return value;
 		} catch (Exception e) {
 			throw new ScanFileHolderException("The keyword " + key + " was not found in the CBF Header", e);
 		}
 	}
 
 	
 	private class ImageOrientation {
 
 		int xLength;
 		int yLength;
 		boolean xIncreasing;
 		boolean yIncreasing;
 		boolean isRowsX;
 
 		private ImageOrientation(int x, int y) {
 			// these values are to support the 6M on i24 for the time being
 			xLength = x;
 			yLength = y;
 			xIncreasing = true;
 			yIncreasing = false;
 			isRowsX = true;
 		}
 
 		private ImageOrientation(int x, int y, boolean increasingX, boolean increasingY,
 				boolean areRowsX) {
 			xLength = x;
 			yLength = y;
 			xIncreasing = increasingX;
 			yIncreasing = increasingY;
 			isRowsX = areRowsX;
 		}
 
 		public int getXLength() {
 			return xLength;
 		}
 
 		public int getYLength() {
 			return yLength;
 		}
 
 		public boolean isXIncreasing() {
 			return xIncreasing;
 		}
 
 		public boolean isYIncreasing() {
 			return yIncreasing;
 		}
 
 		public boolean isRowsX() {
 			return isRowsX;
 		}
 
 	}
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 		cbf_handle_struct chs = new cbf_handle_struct(fileName);
 		CBFError.errorChecker(cbf.cbf_rewind_datablock(chs));
 		try {
 			readMiniCBFHeader(chs);
 		} catch (Exception e) {
 			throw new ScanFileHolderException("Could not create metadata form CBF", e);
 		}
 	}
 
 	@Override
 	public IMetaData getMetaData() {
 		return diffMetadata;
 	}
 }
