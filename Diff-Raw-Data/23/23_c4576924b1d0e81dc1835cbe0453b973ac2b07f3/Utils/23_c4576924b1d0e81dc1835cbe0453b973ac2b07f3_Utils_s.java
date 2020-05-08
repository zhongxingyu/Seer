 package org.eclipse.test.performance.ui;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 
 import java.io.BufferedOutputStream;
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.test.internal.performance.data.Dim;
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.TimeSeries;
 import org.eclipse.test.internal.performance.PerformanceTestPlugin;
 import org.eclipse.test.internal.performance.db.DB;
 import org.eclipse.test.internal.performance.db.Variations;
 
 public class Utils {
 
 	/**
 	 * @param dimension
 	 * @return A description of the dimension.
 	 */
 	public static String getDimensionDescription(String dimension){
 		/*Descriptions of dimensions */
 		//Windows and Linux
 		Hashtable descriptions = new Hashtable();
 		descriptions.put("cpu time","Amount of time the process ran on the CPU.");
 			
 		descriptions.put("kernel time","Amount of time the process ran on the CPU, while the CPU was in kernel mode.");
 		descriptions.put("used java heap","Change in the amount of memory allocated for Java objects.");
 		descriptions.put("working set","Change in the amount of physical memory used by the process (other data resides in swap space).");
 
 		//Linux
 		descriptions.put("data size","Change in the process' data and stack memory size.");
 		descriptions.put("hard page faults","Number of memory pages that were loaded from swap space on disk.");
 		descriptions.put("library size","Change in the process' library memory size.");
 		descriptions.put("soft page faults","Number of memory pages that were loaded from memory (i.e., they were not mapped in the process' page table, but already present in memory for some reason).");
 		descriptions.put("text size","Change in the process' code memory size, useful with start-up tests.");
 
 		//Windows
 		descriptions.put("committed","Change in the amount of allocated memory (both, in physical memory and swap space, can be preallocated for future use).");
 		descriptions.put("elapsed process","Amount of wall-clock time.");
 		descriptions.put("gdi objects","Change in the number of GDI (Graphics Device Interface) objects, can be useful for UI tests (particularly start-up tests).");
 		descriptions.put("page faults","Number of memory pages that were loaded from swap space on disk or from memory (i.e., in the latter case, they were not mapped in the process' page table, but already present in memory for some reason).");
 		descriptions.put("system time","* no longer measured, same as elapsed time, see PerformanceMonitor *");
 		descriptions.put("working set peak","Increase of the maximum working set size, useful with start-up tests.");
 
 		if(descriptions.get(dimension.toLowerCase())!=null)
 			return descriptions.get(dimension.toLowerCase()).toString();
 		return "";
 	}
 
 	/**
 	 * @param timeSeriesLabels - an array of build ID's with results for a scenario.
 	 * @param current - the current build ID
 	 * @return Build Id's of Nightly builds preceding current.
 	 */
 	public static ArrayList lastSevenNightlyBuildNames(String[] timeSeriesLabels, String current) {
 		int currentIndex=getBuildNameIndex(timeSeriesLabels,current);
 		ArrayList labels = new ArrayList();
 		int j=6;
 		
 			for (int i = timeSeriesLabels.length-1; i>-1; i--) {
 				if (j==-1)
 					break;
 				String timeSeriesLabel = timeSeriesLabels[i];
 				if (timeSeriesLabel.startsWith("N")&&i<currentIndex){
 					labels.add(timeSeriesLabel);
 					j--;
 				}		
 		}
 		return labels;
 	}
 	
 	/** 
	 * HTML source used at beginning of html document.
 	 */
 	public static String HTML_OPEN = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">";
 
 	/**
	 * Closing HTML tag </html>.
 	 */
 	public static String HTML_CLOSE = "</html>";
 
 	/**
 	 * Default style-sheet used on eclipse.org
 	 */
 	public static String HTML_DEFAULT_CSS = "<style type=\"text/css\">"
 			+ "p, table, td, th {  font-family: arial, helvetica, geneva; font-size: 10pt}\n"
 			+ "pre {  font-family: \"Courier New\", Courier, mono; font-size: 10pt}\n"
 			+ "h2 { font-family: arial, helvetica, geneva; font-size: 18pt; font-weight: bold ; line-height: 14px}\n"
 			+ "code {  font-family: \"Courier New\", Courier, mono; font-size: 10pt}\n"
 			+ "sup {  font-family: arial,helvetica,geneva; font-size: 10px}\n"
 			+ "h3 {  font-family: arial, helvetica, geneva; font-size: 14pt; font-weight: bold}\n"
 			+ "li {  font-family: arial, helvetica, geneva; font-size: 10pt}\n"
 			+ "h1 {  font-family: arial, helvetica, geneva; font-size: 28px; font-weight: bold}\n"
 			+ "body {  font-family: arial, helvetica, geneva; font-size: 10pt; clip:   rect(   ); margin-top: 5mm; margin-left: 3mm}\n"
 			+ ".indextop { font-size: x-large;; font-family: Verdana, Arial, Helvetica, sans-serif; font-weight: bold}\n"
 			+ ".indexsub { font-size: xx-small;; font-family: Arial, Helvetica, sans-serif; color: #8080FF}\n"
 			+ "</style>";
 
 	/**
 	 * An utility object which stores a name, description, url and optional output directory.
 	 */
 	public static class ConfigDescriptor {
 		String name;
 
 		String description;
 
 		String url;
 
 		String outputDir;
 
 		/**
 		 * 
 		 * @param name
 		 *            the value specifed for the key config in the
 		 *            eclipse.perf.config system.property key value listings.
 		 *            ie. relengbuildwin2 (machine name)
 		 * @param description
 		 *            a meaningful description to further describe the config.
 		 *            ie. win32-win32-x86 Sun 1.4.2_06
 		 * @param url
 		 *            a url to results for this config. Used in hyperlinks from
 		 *            graphs or tables.
 		 */
 		public ConfigDescriptor(String name, String description, String url,
 				String outputDir) {
 			this.name = name;
 			this.description = description;
 			this.url = url;
 			this.outputDir = outputDir;
 		}
 
 	}
 
 	/**
 	 * @param configDescriptors
 	 *            A semi-colon separated listing of config descriptions.<br>
 	 *            Uses the following format: name,description, url,outputdir;name2,
 	 *            description2,url2,output2;etc..
 	 * @return a mapping of config names to their ConfigDescriptors.
 	 */
 	public static Hashtable getConfigDescriptors(String configDescriptors) {
 		// labelMappings in pairs separated by semi-colon ie.
 		// relengbuildwin2,win32-win32-x86;trelenggtk,linux-gtk-x86
 		StringTokenizer tokenizer = new StringTokenizer(configDescriptors, ";");
 		Hashtable configMap = new Hashtable();
 
 		while (tokenizer.hasMoreTokens()) {
 			String labelDescriptor = tokenizer.nextToken();
 			int commaIndex = labelDescriptor.indexOf(",");
 			String[] elements = labelDescriptor.split(",");
 			String output = null;
 			if (elements.length == 4)
 				output = elements[3];
 			ConfigDescriptor descriptor = new ConfigDescriptor(elements[0],
 					elements[1], elements[2], output);
 			configMap.put(elements[0], descriptor);
 		}
 		return configMap;
 	}
 
 	/**
 	 * Queries database with variation composed of buildIdPattern, config and jvm.
 	 * 
 	 * @return Array of scenarios.
 	 */
 	public static Scenario[] getScenarios(String buildIdPattern,
 			String scenarioPattern, String config, String jvm) {
 		Dim[] qd = null;
 		if (scenarioPattern == null)
 			scenarioPattern = "";
 
 		Variations variations = getVariations(buildIdPattern, config, jvm);
 		return DB.queryScenarios(variations, scenarioPattern + "%",
 				PerformanceTestPlugin.BUILD, qd);
 	}
 
 	/**
 	 * Creates a Variations object using build id pattern, config and jvm.
 	 * 
 	 * @param buildIdPattern
 	 * @param config
 	 * @param jvm
 	 */
 	public static Variations getVariations(String buildIdPattern,
 			String config, String jvm) {
 		String buildIdPatterns=buildIdPattern.replace(',','%');
 		Variations variations = new Variations();
 		variations.put(PerformanceTestPlugin.CONFIG, config);
 		variations.put(PerformanceTestPlugin.BUILD, buildIdPatterns);
 		variations.put("jvm", jvm);
 		return variations;
 	}
 
 	/**
 	 * @param scenarios
 	 * @return list of unique component names derived from prefixes to scenario names.
 	 */
 	public static ArrayList getComponentNames(Scenario[] scenarios) {
 		ArrayList componentNames = new ArrayList();
 
 		for (int i = 0; i < scenarios.length; i++) {
 			String prefix = null;
 			Scenario scenario = scenarios[i];
 			String scenarioName = scenario.getScenarioName();
 
 			// use part of scenario name prior to .test to identify component
 			if (scenarioName.indexOf(".test") != -1) {
 				prefix = scenarioName.substring(0, scenarioName
 						.indexOf(".test"));
 				if (!componentNames.contains(prefix))
 					componentNames.add(prefix);
 			}
 		}
 		return componentNames;
 	}
 
 	/**
 	 * @param fp - a FingerPrint object
 	 * @return - an html representation of the fingerprint.
 	 */
 	public static String getImageMap(FingerPrint fp) {
 		String componentDescription = fp.config;
 		if (fp.configDescriptors != null)
 			componentDescription = ((Utils.ConfigDescriptor) fp.configDescriptors
 					.get(fp.config)).description;
 		String areas = fp.bar.getAreas();
 		if (areas==null)
 			areas="";
 		String output = "";
 		if (new File(fp.outputDirectory,fp.getOutName()+".gif").exists()) {
 			output = "<h4>" + componentDescription + "</h4>";
 			output = output.concat("<img src=\"" + fp.getOutName()
 					+ ".gif\" usemap=\"#" + fp.outName + "\">"
 					+ "<map name=\"" + fp.getOutName() + "\">" + areas
 					+ "</map>\n");
 		} else {
 			output = output.concat("<br><br>There is no fingerprint for "
 					+ componentDescription + "<br><br>\n");
 		}
 		return output;
 	}
 	
 
 	/**
 	 * @param fp - a fingerprint object which provides variations, component name and configDescriptor 
 	 * information.
 	 * @return - HTML table of all scenarios for a component with red x or green check indicators.
 	 */
 	public static String printScenarioStatusTable(FingerPrint fp) {
 		// print the component scenario table beneath the fingerprint
 		fp.variations.put("config", "%");
 		ScenarioStatusTable sst = new ScenarioStatusTable(fp.variations,
 				fp.component + "%", fp.configDescriptors);
 		return sst.toString();
 	}
 
 	/**
 	 * Utility method to copy a file.
 	 * @param src - the source file.
 	 * @param dest - the destination.
 	 */
 	public static void copyFile(URL src, String dest) {
 		try {
 			InputStream in = new FileInputStream(src.getFile());
 			OutputStream out = new FileOutputStream(dest);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			in.close();
 			out.close();
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Returns a LineGraph object representing measurements for a scenario over builds.
 	 * @param t - the scenario object.
 	 * @param dimensionName - the name of the measurement for which to generate graph.
 	 * @param reference - the reference build to label.
 	 * @param current - the current build to label.
 	 * @param pointsOfInterest - an array of buildIds.  Points for these are highlighted on graph.
 	 * @return a LineGraph object.
 	 */
 	public static LineGraph getLineGraph(Scenario t, String dimensionName,
 		String reference, String current,ArrayList pointsOfInterest) {
 		Display display = Display.getDefault();
 
 		Color black = display.getSystemColor(SWT.COLOR_BLACK);
 		Color yellow = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
 		Color magenta = display.getSystemColor(SWT.COLOR_MAGENTA);
 		String scenarioName = t.getScenarioName();
 		Dim[] dimensions = t.getDimensions();
 		Dim dim=null;
 		for (int i = 0; i < dimensions.length; i++) {
 			if (dimensions[i].getName().equals(dimensionName))
 				dim = dimensions[i];
 		}
 
 		LineGraph graph = new LineGraph(scenarioName + ": " + dimensionName,
 				dim);
 		TimeSeries ts = null;
 		try {
 			ts = t.getTimeSeries(dim);
 			int n = ts.getLength();
 			
 
 			if (n > 0) {
 				for (int j = 0; j < n; j++) {
 					String buildID = ts.getLabel(j);
 					double value = ts.getValue(j);
 					Color c=yellow;
 					if (buildID.equals(reference)||pointsOfInterest.contains(buildID))
 						c =magenta;
 					else if (buildID.startsWith("I"))
 						c=black;
 					
 					int underscoreIndex = buildID.indexOf('_');
 					buildID = (buildID.indexOf('_') == -1) ? buildID : buildID
 							.substring(0, underscoreIndex);
					if (ts.getLabel(j).equals(reference) || ts.getLabel(j).equals(current))
 						graph.addItem(buildID, dim.getDisplayValue(value),
 								value, c, true);
 					else if(buildID.startsWith("I")||pointsOfInterest.contains(ts.getLabel(j))||lastSevenNightlyBuildNames(t.getTimeSeriesLabels(),current).contains(buildID))
 						graph.addItem(ts.getLabel(j), dim.getDisplayValue(value),
 								value, c);
 				}
 			}
 		} catch (AssertionFailedError e) {
 			// System.err.println("Unable to get result for:
 			// "+t.getScenarioName()+" "+ts.toString());
 		}
 		return graph;
 	}
 
 	/**
 	 * Prints a LineGraph object as a gif
 	 * @param p - the LineGraph object.
 	 * @param output - the output file path.
 	 */
 	public static void printLineGraphGif(LineGraph p, String output) {
 		File outputFile = new  File(output);
 		outputFile.getParentFile().mkdir();
 		int GRAPH_WIDTH = 600;
 		int GRAPH_HEIGHT = 200;
 		Image image = new Image(Display.getDefault(), GRAPH_WIDTH, GRAPH_HEIGHT);
 		p.paint(image);
 		
 	     /* Downscale to 8 bit depth palette to save to gif */
         ImageData data = Utils.downSample(image);
         ImageLoader il= new ImageLoader();
         il.data= new ImageData[] { data };
 		OutputStream out = null;
 		try {
 			out = new BufferedOutputStream(new FileOutputStream(output));
 			il.save(out, SWT.IMAGE_GIF);
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			image.dispose();
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException e1) {
 					// silently ignored
 				}
 			}
 		}
 	}
 
 	/**
 	 * Utility method which returns HTML code representing an image map for a LineGraph object.
 	 * @param p - the LineGraph object.
 	 * @param imageSource - the path to insert for the src attribute to <img>. 
 	 */
 	public static String getImageMap(LineGraph p, String imageSource) {
 		String result="";
 		String areas = p.getAreas();
 		if (areas!=null){
 				result=result.concat("<img src=\"" + imageSource+"\" usemap=\"#" + p.fTitle + "\">");
 				result=result.concat("<map name=\"" + p.fTitle + "\">");
 				result=result.concat(areas);
 				result=result.concat("</map>");
 		}
 		return result;
 	}
 
 	/**
 	 * A utility which returns the index of a given buildId in an array.
 	 * @param timeSeriesLabels - array of buildIds
 	 * @param buildId - the buildId for which to find return value.
 	 * @return The index of the buildID in the array.
 	 */
 	public static int getBuildNameIndex(String[] timeSeriesLabels,
 			String buildId) {
 		for (int i = timeSeriesLabels.length-1; i>-1;i--) {
 			String timeSeriesLabel = timeSeriesLabels[i];
 			if (timeSeriesLabel.startsWith(buildId))
 				return i;
 		}
 		return -1;
 	}
 	
     /** Downsample Image to 8 bit depth format
      *  so that the resulting image data can
      *  be saved to GIF.
      *  Note.  If the source image contains photo
      *  quality content with more than 256 colours,
      *  resulting data will look very poor.
      */
 	static int closest(RGB[] rgbs, int n, RGB rgb) {
 		int minDist = 256*256*3;
 		int minIndex = 0;
 		for (int i = 0; i < n; ++i) {
 			RGB rgb2 = rgbs[i];
 			int da = rgb2.red - rgb.red;
 			int dg = rgb2.green - rgb.green;
 			int db = rgb2.blue - rgb.blue;
 			int dist = da*da + dg*dg + db*db;
 			if (dist < minDist) {
 				minDist = dist;
 				minIndex = i;
 			}
 		}
 		return minIndex;
 	}
     
 
 	
 	static class ColorCounter implements Comparable {
 		RGB rgb;
 
 		int count;
 
 		public int compareTo(Object o) {
 			return ((ColorCounter) o).count - count;
 		}
 	}
 
     public static ImageData downSample(Image image) {
         ImageData data = image.getImageData();
         if (!data.palette.isDirect && data.depth <= 8) return data;
 
         // compute a histogram of color frequencies
         HashMap freq = new HashMap();
         int width = data.width;
         int[] pixels = new int[width];
         int[] maskPixels = new int[width];
         for (int y = 0, height = data.height; y < height; ++y) {
             data.getPixels(0, y, width, pixels, 0);
             for (int x = 0; x < width; ++x) {
                 RGB rgb = data.palette.getRGB(pixels[x]);
                 ColorCounter counter = (ColorCounter) freq.get(rgb);
                 if (counter == null) {
                     counter = new ColorCounter();
                     counter.rgb = rgb;
                     freq.put(rgb, counter);
                 }
                 counter.count++;
             }
         }
 
         // sort colors by most frequently used
         ColorCounter[] counters = new ColorCounter[freq.size()];
         freq.values().toArray(counters);
         Arrays.sort(counters);
         
         // pick the most frequently used 256 (or fewer), and make a palette
         ImageData mask = null;
         if (data.transparentPixel != -1 || data.maskData != null) {
             mask = data.getTransparencyMask();
         }
         int n = Math.min(256, freq.size());
         RGB[] rgbs = new RGB[n + (mask != null ? 1 : 0)];
         for (int i = 0; i < n; ++i) rgbs[i] = counters[i].rgb;
         if (mask != null) {
             rgbs[rgbs.length - 1] = data.transparentPixel != -1 ? data.palette.getRGB(data.transparentPixel) : new RGB(255, 255, 255);
         }
         PaletteData palette = new PaletteData(rgbs);
 
         // create a new image using the new palette:
         //   for each pixel in the old image, look up the best matching 
         //   index in the new palette
         ImageData newData = new ImageData(width, data.height, 8, palette);
         if (mask != null) newData.transparentPixel = rgbs.length - 1;
         for (int y = 0, height = data.height; y < height; ++y) {
             data.getPixels(0, y, width, pixels, 0);
             if (mask != null) mask.getPixels(0, y, width, maskPixels, 0);
             for (int x = 0; x < width; ++x) {
             	if (mask != null && maskPixels[x] == 0) {
             		pixels[x] = rgbs.length - 1;
             	}
                 else {
                 	RGB rgb = data.palette.getRGB(pixels[x]);
                 	pixels[x] = closest(rgbs, n, rgb);
                 }
              }
             newData.setPixels(0, y, width, pixels, 0);
         }
         return newData;
 	}
 }
