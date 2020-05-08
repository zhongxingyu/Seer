 package util;
 
 import play.*;
 import java.util.*;
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 
 import org.codehaus.jackson.*;
 import org.codehaus.jackson.node.*;
 
 //------------------------------------------------------------------------------
 public abstract class Layer_Base
 {
 	private static Map<String, Layer_Base>	mLayers;
 	
 	protected final boolean mbUseBinaryFormat = false;
 	
 	protected String mName;
 	protected int mWidth, mHeight;
 	protected int mNoDataValue;
 	protected int[][] mIntData;
 
 	// Return the Layer_Base object when asked for it by name
 	//--------------------------------------------------------------------------
 	public static Layer_Base getLayer(String name) {
 		
 		if (mLayers == null) {
 			Logger.info("Info: getLayer called with no mLayers. Allocating one.");
 			mLayers = new HashMap<String, Layer_Base>();
 		}
 		String name_low = name.toLowerCase();
 		if (!mLayers.containsKey(name_low)) {
 			Logger.info("getLayer called looking for: " + name_low + "  but layer doesn't exist");
 			return null;
 		}
 		return mLayers.get(name_low);
 	}
 
 	// Use carefully...e.g., only if you are temporarily loading data for a process that rarely runs...
 	//--------------------------------------------------------------------------
 	public static void removeLayer(String name) {
 
 		Logger.info("A call was made to remove layer: " + name + " from memory");
 		name = name.toLowerCase();
 		mLayers.remove(name);
 	}
 	
 	// Use even more carefully...currently only be used when the server shuts down.
 	//--------------------------------------------------------------------------
 	public static void removeAllLayers() {
 
 		Logger.info("A call was made to clear all Layers!");
 		mLayers.clear();
 	}
 	
 	// Base constructor
 	//--------------------------------------------------------------------------
 	public Layer_Base(String name) {
 		
 		if (mLayers == null) {
 			mLayers = new HashMap<String, Layer_Base>();
 		}
 		mName = name.toLowerCase();
 		mLayers.put(mName, this);
 	}
 	
 	//--------------------------------------------------------------------------
 	public int getWidth() {
 		return mWidth;
 	}
 
 	//--------------------------------------------------------------------------
 	public int getHeight() {
 		return mHeight;
 	}
 	
 	//--------------------------------------------------------------------------
 	public int[][] getIntData() {
 		return mIntData;
 	}
 	
 	//--------------------------------------------------------------------------
 	public void init() {
 		
 		try {
 			// TODO: FIXME: binary format reading or writing has an issue, not sure which
 			if (mbUseBinaryFormat) {
 				File input = new File("./layerData/" + mName + ".bin");
 				if(input.exists()) {
 					readBinary();
 				}
 				else {
 					// wanting to use binary format but file doesn't exist...
 					// 	load ASC, save as Binary
 					loadASC();
 					writeBinary();
 				}
 			}
 			else {
 				loadASC();
 			}
 		}
 		catch(Exception e) {
 			Logger.error(e.toString());
 		}
 	}
 	
 	//--------------------------------------------------------------------------
 	private String getASC_HeaderValue(String line) {
 		
 		String split[] = line.split("\\s+");
 		if (split.length == 2) {
 			return split[1];
 		}
 		
 		return null;
 	}
 	
 	//--------------------------------------------------------------------------
 	private void readASC_Header(BufferedReader reader) throws Exception {
 		
 		try {
 			String width = reader.readLine(); // ncols
 			String height = reader.readLine(); // nrows
			
			String tmp = reader.readLine(); /* xll corner */ Logger.info("  " + tmp);
			tmp = reader.readLine(); /* yll corner */ Logger.info("  " + tmp);
			tmp = reader.readLine(); /* cellsize */ Logger.info("  " + tmp);
 			String noData = reader.readLine(); // nodata value
 			
 			Logger.info("  " + width);
 			Logger.info("  " + height);
 			Logger.info("  " + noData);
 			
 			mWidth = Integer.parseInt(getASC_HeaderValue(width));
 			mHeight = Integer.parseInt(getASC_HeaderValue(height));
 			mNoDataValue = Integer.parseInt(getASC_HeaderValue(noData));
 		}
 		catch (Exception e) {
 			Logger.info(e.toString());
 		}
 	}
 	
 	//--------------------------------------------------------------------------
 	private void loadASC() throws Exception {
 		
 		Logger.info("+-------------------------------------------------------+");
 		Logger.info("| " + mName);
 		Logger.info("+-------------------------------------------------------+");
 		
 		BufferedReader br = new BufferedReader(new FileReader("./layerData/" + mName + ".asc"));
 		try {
 			readASC_Header(br);
 			allocMemory(mWidth, mHeight);
 			
 			Logger.info("  Attempting to read the array data");
 			
 			int x = 0, y = 0;
 			
 			// now read the array data
 			while (br.ready()) {
 				if (y >= mHeight) {
 					Logger.error("BAD READ - more lines than expected!");
 					break;
 				}
 				String line = br.readLine();
 				String split[] = line.split("\\s+");
 				processASC_Line(y, split);
 				y++;
 			}
 		}
 		catch (Exception e) {
 			Logger.info(e.toString());
 		}
 		finally {
 			br.close();
 		}
 		
 		onLoadEnd();
 	}
 	
 	//--------------------------------------------------------------------------
 	protected void allocMemory(int width, int height) {
 		
 		Logger.info("  Allocating new work array");
 		mIntData = new int[mHeight][mWidth];
 	}
 	
 	// Generally comes from a client request for data about this layer...
 	//	this could be layer width/height, maybe cell size (30m), maybe the layer
 	//	ranges, in the case of indexed layers...the key/legend for the layer
 	//--------------------------------------------------------------------------
 	public static JsonNode getParameter(JsonNode clientRequest) throws Exception {
 		
 		JsonNode ret = null;
 		String layername = clientRequest.get("name").getTextValue();
 		
 		Layer_Base layer = Layer_Base.getLayer(layername);
 		if (layer != null) {
 			ret = layer.getParameterInternal(clientRequest);
 		}
 
 		return ret;
 	}
 	
 	// ordering expects subclasses to call the super (this) first...
 	//--------------------------------------------------------------------------
 	protected JsonNode getParameterInternal(JsonNode clientRequest) throws Exception {
 		
 		// Set this as a default
 		JsonNode ret = null;
 		
 		// Do any processing here if the base layer class needs to...
 		//	e.g., if we ever need to pass back layer dimensions, cell size, etc...
 		
 		return ret;
 	}
 	
 	// Functions that must be in the subclass. 
 	//--------------------------------------------------------------------------
 	abstract protected void onLoadEnd();
 	abstract protected void processASC_Line(int y, String lineElementsArray[]);
 	abstract protected Selection query(JsonNode queryNode, Selection selection);
 		
 	//--------------------------------------------------------------------------
 	public static void execQuery(JsonNode layerList, Selection selection) {
 
 		if (layerList.isArray()) {
 	
 			ArrayNode arNode = (ArrayNode)layerList;
 			int count = arNode.size();
 			for (int i = 0; i < count; i++) {
 				Logger.info("Processing one array element in the queryLayers layer list");
 				JsonNode arElem = arNode.get(i);
 				JsonNode layerName = arElem.get("name");
 				if (arElem != null && layerName != null) {
 					Layer_Base layer = Layer_Base.getLayer(layerName.getValueAsText());
 					if (layer != null) {
 						layer.query(arElem, selection);
 					}
 				}
 			}
 		}
 	}
 
 	//--------------------------------------------------------------------------
 	// BINARY format reading/writing
 	//	~3-5x faster 
 	//	FIXME: TODO: (but doesn't current work right yet) unsure exactly why. 
 	//	Needs diagnosing and fixing...
 	//--------------------------------------------------------------------------
 	private void writeBinary() throws Exception {
 		
 		Logger.info("Writing Binary");
 		File output = new File("./layerData/" + mName + ".bin");
 
 		FileOutputStream fos = new FileOutputStream(output);
 		try {
 			WritableByteChannel channel = fos.getChannel();
 			ByteBuffer buf = ByteBuffer.allocateDirect(3 * 4); // FIXME: size of int * header?
 			
 			// FIXME: TODO: Continuous layers need min/max range property read
 			//	else...could scan the data layer later worst case
 			buf.putInt(mWidth);
 			buf.putInt(mHeight);
 			buf.putInt(mNoDataValue);
 			buf.flip();
 			channel.write(buf);
 			
 			buf = ByteBuffer.allocateDirect(mWidth * 4); // FIXME: size of int?
 			
 			for (int y = 0; y < mHeight; y++) {
 				buf.clear();
 				for (int x = 0; x < mWidth; x++) {
 					buf.putInt(mIntData[y][x]);
 				}
 				buf.flip();
 				channel.write(buf);
 			}
 			channel.close();
 		}
 		catch (Exception e) {
 			Logger.info(e.toString());
 		}
 		finally {
 			fos.close();
 		}
 	}
 	
 	//--------------------------------------------------------------------------
 	private void readBinary() throws Exception {
 		
 		Logger.info("+-------------------------------------------------------+");
 		Logger.info("| Binary Read: " + mName);
 		Logger.info("+-------------------------------------------------------+");
 		File input = new File("./layerData/" + mName + ".bin");
 
 		FileInputStream fis = new FileInputStream(input);
 		
 		try {
 			ReadableByteChannel channel = fis.getChannel();
 			
 			Logger.info("  Reading header...");
 			ByteBuffer buf = ByteBuffer.allocateDirect(3 * 4); // FIXME: size of header * size of int?
 			channel.read(buf); 
 			buf.rewind();
 			Logger.info("  Extracting header...");
 			// FIXME: TODO: Continuous layers need min/max range property read
 			//	else...could scan the data layer later worst case
 			mWidth = buf.getInt();
 			mHeight = buf.getInt();
 			mNoDataValue = buf.getInt();
 			
 			Logger.info("  Width: " + Integer.toString(mWidth) 
 							+ "  Height: " + Integer.toString(mHeight));
 			allocMemory(mWidth, mHeight);
 			
 			buf = ByteBuffer.allocateDirect(mWidth * 4); // FIXME: size of int?
 			Logger.info("  Reading file with ByteBuffer and NIO...");
 			
 			for (int y = 0; y < mHeight; y++) {
 				buf.rewind();
 				channel.read(buf);
 				for (int x = 0; x < mWidth; x++) {
					mIntData[y][x] = buf.getInt(x);
 				}
 			}
 			channel.close();
 		}
 		catch (Exception e) {
 			Logger.info(e.toString());
 		}
 		finally {
 			fis.close();
 		}
 		
 		onLoadEnd();
 	}
 }
