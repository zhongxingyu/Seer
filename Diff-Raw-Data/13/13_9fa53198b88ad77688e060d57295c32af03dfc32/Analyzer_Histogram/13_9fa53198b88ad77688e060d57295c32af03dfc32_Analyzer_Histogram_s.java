 package util;
 
 import play.*;
 import java.util.*;
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 
 import org.codehaus.jackson.*;
 import org.codehaus.jackson.node.*;
 
 //------------------------------------------------------------------------------
 // Analyzer_Histogram
 //
 // This program selects cells in the raster map based on user request 
 // Inputs are a selection and ONE of the following data sources:
 //	1) two FILES to compare - results are read into memory
 //	2) one FILE and one IN-MEMORY dataset to compare
 //	3) two IN-MEMORY datasets to compare
 // 
 // Outputs are an array that hold the selected cells
 //
 //------------------------------------------------------------------------------
 public class Analyzer_Histogram
 {
	private final static int DEFAULT_histogramSize = 10;
 	private int mHistogramSize;
 	private int [] mFile1_Histogram, mFile2_Histogram;
 	
 	private Selection mSelection;
 	private float [][] mFile1Data, mFile2Data;
 	
 	//--------------------------------------------------------------------------
 	private class MinMax {
 		public float mMin, mMax;
 		
 		public MinMax(float min, float max) {
 			
 			mMin = min;
 			mMax = max;
 		}
 	}
 	
 	// Create histogram that generates our typical binning size	
 	//--------------------------------------------------------------------------
 	public Analyzer_Histogram(Selection selection) {
 	
 		this(DEFAULT_histogramSize, selection);
 	}
 	
 	//--------------------------------------------------------------------------
 	public Analyzer_Histogram(int histogramBinCount, Selection selection) {
 		
 		mHistogramSize = histogramBinCount;
 		mFile1_Histogram = new int [mHistogramSize];
 		mFile2_Histogram = new int [mHistogramSize];
 		mSelection = selection;
 		
 		mFile1Data = null; mFile2Data = null;
 	}
 	
 	//--------------------------------------------------------------------------
 	private void clearBins() {
 
 		for (int i = 0; i < mHistogramSize; i++) {
 			mFile1_Histogram[i] = 0;;
 			mFile2_Histogram[i] = 0;
 		}
 	}
 
 	//--------------------------------------------------------------------------
 	private MinMax loadFileData(File file, boolean assignToFile1_Data) {
 		
 		int width = mSelection.mWidth, height = mSelection.mHeight;
 		float min = 1000000.0f, max = -1000000.0f;
 		
 		float [][] rasterData = null;
 		if (assignToFile1_Data) {
 			if (mFile1Data == null) {			
 				mFile1Data = new float[height][width];
 			}
 			rasterData = mFile1Data;
 		} else {
 			if (mFile2Data == null) {			
 				mFile2Data = new float[height][width];
 			}
 			rasterData = mFile2Data;
 		}
 		
 		Binary_Reader fileReader = new Binary_Reader(file);
 		if (fileReader.readHeader()) {
 			if (fileReader.getWidth() != width || fileReader.getHeight() != height) {
 				// FIXME: bad...error...etc...
 				Logger.info("Selection width and height does not match the binary file 1 being read...");
 				Logger.info("Selection (w, h): (" + Integer.toString(width) + ", " +
 									Integer.toString(height) + ")");
 				Logger.info("Binary File 1 (w, h): (" + Integer.toString(fileReader.getWidth()) + ", " +
 									Integer.toString(fileReader.getHeight()) + ")");
 				return null;
 			}
 			
 			for (int y = 0; y < height; y++) {
 				ByteBuffer buff = fileReader.readLine();
 				if (buff != null) {
 					for (int x=0; x < width; x++) {
 						float data = buff.getFloat(x * 4); // blah, 4 = size of float, ie 32bit
 						if (mSelection.mSelection[y][x] >= 1 && data > -9999.0f) { // FIXME: NoData check...
 							min = min(min, data);
 							max = max(max, data);
							rasterData[y][x] = data;
 						}
 					}
 				}
 			}
 			fileReader.close();
 		}
 		
 		return new MinMax(min, max);
 	}
 	
 	//--------------------------------------------------------------------------
 	private JsonNode binify(float min, float max, float [][] rasterData, int [] histogram) {
 		
 		int totalCountFile = 0;
 		double totalFile = 0.0;
 		
 		int width = mSelection.mWidth, height = mSelection.mHeight;
 		for (int y=0; y < height; y++) {
 			for (int x=0; x < width; x++) {
 				float data = rasterData[y][x];
 				if (mSelection.mSelection[y][x] >= 1 && data > -9999.0f) { // FIXME: NoData check...
 					totalFile += data;
					int binIndex = (int)((data - min)/(max - min) * (mHistogramSize - 1));
 					histogram[binIndex]++;
 					totalCountFile++;
 				}
 			}
 		}
 
 		// 4th step, package up the data into a JSON object to ship back to the client
 		//	Define Json to store and return data to client
 		ObjectNode obj = JsonNodeFactory.instance.objectNode();
 		
 		// Convert java Histogram bins to JSON Array Node
 		ArrayNode jsonHistogramFile = JsonNodeFactory.instance.arrayNode();
 		for (int i = 0; i < histogram.length; i++) {
 			jsonHistogramFile.add(histogram[i]);
 		}
 		
 		obj.put("histogram", jsonHistogramFile);
 		obj.put("sum", totalFile);
 		obj.put("count", totalCountFile);
 		
 		return obj;
 	}
 	
 	// COMPARE a FILE to another FILE
 	//--------------------------------------------------------------------------
 	public JsonNode run(File file1, File file2) {
 
 		clearBins();
 
 		int width = mSelection.mWidth, height = mSelection.mHeight;
 		
 long processStart = System.currentTimeMillis();
 		// 1st step, Scan both files in turn, to find the MIN / MAX
 		//--------------------------------
 		MinMax minMax1 = loadFileData(file1, true);
 		MinMax minMax2 = loadFileData(file2, false);
 		
 		if (minMax1 == null || minMax2 == null) {
 			return null;
 		}
 
 		float min = min(minMax1.mMin, minMax2.mMin);
 		float max = max(minMax1.mMax, minMax2.mMax);
  		
 		// 2nd step, cast each file contents into a binned/histogrammed array, summing
 		//	the total result as we go for passing back as a simple average
 		//--------------------------------
 		JsonNode fileHist1 = binify(min, max, mFile1Data, mFile1_Histogram);
 		JsonNode fileHist2 = binify(min, max, mFile2Data, mFile2_Histogram);
 		
 		// 3rd step, package up the data into a JSON object to ship back to the client
 		//	Define Json to store and return data to client
 		//--------------------------------
 		ObjectNode obj = JsonNodeFactory.instance.objectNode();
 		
 		obj.put("min", min);
 		obj.put("max", max);
 		obj.put("file1", fileHist1);
 		obj.put("file2", fileHist2);
 		
 long timingEnd = System.currentTimeMillis();
 float timeSec = (timingEnd - processStart) / 1000.0f;
 Logger.info(">>> Total process: " + Float.toString(timeSec));
 
 		return obj;
 	}
 	
 	// COMPARE a FILE to in-memory data
 	//--------------------------------------------------------------------------
 	public JsonNode run(File file1, int width, int height, float [][] data2) {
 
 		clearBins();
 		
 		if (width != mSelection.mWidth || height != mSelection.mHeight) {
 			Logger.info("Incoming width and height does not match selection width and height...");
 			Logger.info("Incoming (w, h): (" + Integer.toString(width) + ", " + 
 									Integer.toString(height) + ")");
 			Logger.info("Selection (w, h): (" + Integer.toString(mSelection.mWidth) + ", " + 
 									Integer.toString(mSelection.mHeight) + ")");
 			return null;
 		}
 		
 		// 1st step, Scan both files in turn, to find the MIN / MAX
 		//--------------------------------
 long processStart = System.currentTimeMillis();
 		MinMax minMax1 = loadFileData(file1, true);
 		
 		if (minMax1 == null) {
 			return null;
 		}
 
 		float min = minMax1.mMin, max = minMax1.mMax;
 		
 		for (int y=0; y < height; y++) {
 			for (int x=0; x < width; x++) {
 				float data = data2[y][x];
 				if (mSelection.mSelection[y][x] >= 1 && data > -9999.0f) { // FIXME: NoData check...
 					min = min(min, data);
 					max = max(max, data);
 				}
 			}
 		}
 		
 		// 2nd step, cast each file contents into a binned/histogrammed array, summing
 		//	the total result as we go for passing back as a simple average
 		//--------------------------------
 		JsonNode fileHist1 = binify(min, max, mFile1Data, mFile1_Histogram);
 		JsonNode fileHist2 = binify(min, max, data2, mFile2_Histogram);
 		
 		// 3rd step, package up the data into a JSON object to ship back to the client
 		//	Define Json to store and return data to client
 		//--------------------------------
 		ObjectNode obj = JsonNodeFactory.instance.objectNode();
 		
 		obj.put("min", min);
 		obj.put("max", max);
 		obj.put("file1", fileHist1);
 		obj.put("file2", fileHist2);
 		
 long timingEnd = System.currentTimeMillis();
 float timeSec = (timingEnd - processStart) / 1000.0f;
 Logger.info(">>> Total process: " + Float.toString(timeSec));
 
 		return obj;
 	}
 
 	// COMPARE in-memory results to in-memory results
 	//--------------------------------------------------------------------------
 	public JsonNode run(int width, int height, float [][] data1, float [][] data2) {
 
 		clearBins();
 		
 		if (width != mSelection.mWidth || height != mSelection.mHeight) {
 			Logger.info("Incoming width and height does not match selection width and height...");
 			Logger.info("Incoming (w, h): (" + Integer.toString(width) + ", " + 
 									Integer.toString(height) + ")");
 			Logger.info("Selection (w, h): (" + Integer.toString(mSelection.mWidth) + ", " + 
 									Integer.toString(mSelection.mHeight) + ")");
 			return null;
 		}
 		
 		// 1st step, Scan both files in turn, to find the MIN / MAX
 		//--------------------------------
 long processStart = System.currentTimeMillis();
 		float min = 1000000.0f, max = -1000000.0f;
 		
 		for (int y=0; y < height; y++) {
 			for (int x=0; x < width; x++) {
 				float data = data1[y][x];
 				if (mSelection.mSelection[y][x] >= 1 && data > -9999.0f) { // FIXME: NoData check...
 					min = min(min, data);
 					max = max(max, data);
 				}
 			}
 		}
 		
 		for (int y=0; y < height; y++) {
 			for (int x=0; x < width; x++) {
 				float data = data2[y][x];
 				if (mSelection.mSelection[y][x] >= 1 && data > -9999.0f) { // FIXME: NoData check...
 					min = min(min, data);
 					max = max(max, data);
 				}
 			}
 		}
 		
 		// 2nd step, cast each file contents into a binned/histogrammed array, summing
 		//	the total result as we go for passing back as a simple average
 		//--------------------------------
 		JsonNode fileHist1 = binify(min, max, data1, mFile1_Histogram);
 		JsonNode fileHist2 = binify(min, max, data2, mFile2_Histogram);
 		
 		// 3rd step, package up the data into a JSON object to ship back to the client
 		//	Define Json to store and return data to client
 		//--------------------------------
 		ObjectNode obj = JsonNodeFactory.instance.objectNode();
 		
 		obj.put("min", min);
 		obj.put("max", max);
 		obj.put("file1", fileHist1);
 		obj.put("file2", fileHist2);
 		
 long timingEnd = System.currentTimeMillis();
 float timeSec = (timingEnd - processStart) / 1000.0f;
 Logger.info(">>> Total process: " + Float.toString(timeSec));
 
 		return obj;
 	}
 	
 	//--------------------------------------------------------------------------
 	public final static float min(float min, float num) { 
 		if (num < min) {
 			return num;
 		}
 		return min;
 	}
 	
 	//--------------------------------------------------------------------------
 	public final static float max(float max, float num) {
 		if (num > max) {
 			return num;
 		}
 		return max;
 	}
 }
 
