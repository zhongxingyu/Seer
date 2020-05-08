 package application;
 
 import java.awt.Font;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;
 import filter.*;
 
 public class Main {
 
 	private static Font font = null; // the font used on the AR-Display
 	public static Map<String, Integer> configuration; // configuration file which holds the keybindings
 	private static ArrayList<filter.Filter> filterList; // list of all filters, that are usable in the gui
 	private static filter.ContentLayer contentLayer; // the contentLayer-Filter
 
 	// main - here starts aSight
 	public static void main(String[] args) {
 		
 		// load configuration and create font
 		configuration = loadConfig();
 		font = new Font("Arial", Font.BOLD, 20);
 		
 		// create filter list, order here is the order in which the filters are applied
 		contentLayer = new filter.ContentLayer();
 		filterList = new ArrayList<filter.Filter>();
 		filterList.add(new AdjustmentFilter(getFromConfig("AdjustmentFilterPresetX"), getFromConfig("AdjustmentFilterPresetY"), getFromConfig("AdjustmentFilterPresetDimensionOutX"), getFromConfig("AdjustmentFilterPresetDimensionOutY")));
 		filterList.add(new ZoomFilter());
 		filterList.add(new PictureSearchFilter());
 		filterList.add(new CodeParaFilter(contentLayer));
 		filterList.add(contentLayer);
 		filterList.add(new CorrectionFilter());
 		contentLayer.setFilterList(filterList);
 		
 		// start GUI
 		Gui.createGui();
 	}
 
 	public static ArrayList<filter.Filter> getFilter() {
 		return filterList;
 	}
 
 	public static filter.ContentLayer getContentLayer() {
 		return contentLayer;
 	}
 
 	// getOutputdevices() - returns all Devices, which are available for output
 	// - for aSight to run correctly with the EV-Driver choose the Monitor, which is used by the Driver (should be 1)
 	public static Vector<Integer> getCountOutputDevices() {
 		Vector<Integer> v = new Vector<Integer>();
 		GraphicsDevice[] monitorArray = GraphicsEnvironment
 				.getLocalGraphicsEnvironment().getScreenDevices();
 		v.add(0);
 		for (int i = 1; i <= monitorArray.length; i++) {
 			v.add(i);
 		}
 		return v;
 	}
 
 	// applyFilters(..) - uses all active Filters on the BufferedImage in and displays it on the VideoField out
 	public static void applyFilters(BufferedImage in, VideoField out) {
 		for (Filter filter : filterList) {
 			in = filter.useFilter(in);
 		}
 		out.setImage(in);
 	}
 
 	// loadConfig() - loads the configuration-file into a map
 	private static Map<String, Integer> loadConfig() {
 		try {
 			
 			// load file
 			File fi = new File("config.txt");
 			FileReader f = new FileReader(fi);
 			BufferedReader b = new BufferedReader(f);
 			
 			// read and process file
 			String line;
 			HashMap<String, Integer> keys = new HashMap<String, Integer>();
 			while ((line = b.readLine()) != null) {
 				keys.put(line.substring(0, line.indexOf(":")),
 						new Integer(line.substring(line.indexOf(":") + 1)));
 			}
 			b.close();
 			f.close();
 			return keys;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return new HashMap<String, Integer>();
 	}
 
 	// getFromConfig(..) - returns configuration which is saved with the key s
 	public static int getFromConfig(String s) {
 		return configuration.get(s);
 	}
 	
 	public static Font getFont() {
 		return font;
 	}
 }
