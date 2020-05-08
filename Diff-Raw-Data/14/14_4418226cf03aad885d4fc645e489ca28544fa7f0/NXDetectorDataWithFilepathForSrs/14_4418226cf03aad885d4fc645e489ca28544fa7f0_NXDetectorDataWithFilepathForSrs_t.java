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
 
 package gda.device.detector;
 
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.scannable.ScannableUtils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 
 import org.springframework.util.StringUtils;
 
 /**
  * Extends NXDetectorData to report a single filepath associated with the first extra name of a detector.
  */
 public class NXDetectorDataWithFilepathForSrs extends NXDetectorData {
 
 	private static final String[] A = new String[]{};
 
 	private String filepath;
 	private Vector<String> filepaths=new Vector<String>();
 
 	private int filepathOutputFieldIndex = 0;
 
 	public NXDetectorDataWithFilepathForSrs(Scannable adDetector) {
 		super(adDetector);
 	}
 	public NXDetectorDataWithFilepathForSrs() {
 		
 	}
 	@Override
 	public void addFileName(String detName, String filename) {
 		if (filepath != null) {
 			throw new IllegalStateException("A filepath has already been set, and only one is supported");
 		}
 		if( !StringUtils.hasLength(filename))
 			throw new IllegalStateException("Zero length filenames are not allowed");
 		super.addFileName(detName, filename);
 		filepath = filename;
 	}
 
 	public void addFileNames(String detName, String[] filenames) {
 		for (String each : filenames) {
 			//super.addFileName(detName, each);
 			filepaths.add(each);
 		}
 	}
 	
 	public void addScanFileLinks(String detName, String[] filenames) {
 		for (String each : filenames){
 			super.addScanFileLink(detName, each);
 		}
 	}
 	
 	@Override
 	public String toString() {
 
 
 		Double[] justTheDoubles = getDoubleVals();
 		if (justTheDoubles.length == 1 && justTheDoubles[0] == null) {
 			justTheDoubles = new Double[0];
 		}
 		assert (outputFormat.length == justTheDoubles.length );
 		List<Object> positions = new ArrayList<Object>(outputFormat.length);
 		List<String> tmpoutputFormats = new ArrayList<String>(outputFormat.length);
 		Double[] inputDoubles = Arrays.copyOf(justTheDoubles, filepathOutputFieldIndex);
 		for (int i = 0; i < inputDoubles.length; i++) {
 			positions.add(inputDoubles[i]);
 			tmpoutputFormats.add(outputFormat[i]);
 		}
 		if (filepath != null) {
 			positions.add(filepath);
 			tmpoutputFormats.add("%s");
 		}
 		if (!filepaths.isEmpty()) {
 			for (String each : filepaths){
 				positions.add(each);
 				tmpoutputFormats.add("%s");
 			}
 		}
		int pathsHandled = (filepath!=null ? 1: 0 ) + filepaths.size();
		
 		//miss out justDoubles entry that represents the filepath
		if (justTheDoubles.length > 0) {
			Double[] extraDoubles = Arrays.copyOfRange(justTheDoubles, filepathOutputFieldIndex + pathsHandled,
					justTheDoubles.length); //to be tested
			for (int i = 0; i < extraDoubles.length; i++) {
				positions.add(extraDoubles[i]);
				tmpoutputFormats.add((i < outputFormat.length) ? outputFormat[i] : "%.2f");
			}
 		}
 
 		StringBuilder output = new StringBuilder();
 		try {
 			String[] formats = tmpoutputFormats.toArray(A);
 			String[] formatted = ScannableUtils.getFormattedCurrentPositionArray(positions.toArray(new Object[0]), formats.length,formats);
 			for (String string : formatted) {
 				if( !StringUtils.hasLength(string))
 					throw new IllegalArgumentException("A position has zero length");
 				if (output.length() > 0)
 					output.append("\t");
 				output.append(string);
 			}
 		} catch (DeviceException e) {
 			throw new RuntimeException("Problem formatting NXDetectorDataWithFilepathForSrs", e);
 		}
 		return output.toString();
 	}
 
 	public int getFilepathOutputFieldIndex() {
 		return filepathOutputFieldIndex;
 	}
 
 	public void setFilepathOutputFieldIndex(int filepathOutputFiledIndex) {
 		this.filepathOutputFieldIndex = filepathOutputFiledIndex;
 	}
 	
 	public String getFilepath() {
 		return filepath;
 	}
 
 }
