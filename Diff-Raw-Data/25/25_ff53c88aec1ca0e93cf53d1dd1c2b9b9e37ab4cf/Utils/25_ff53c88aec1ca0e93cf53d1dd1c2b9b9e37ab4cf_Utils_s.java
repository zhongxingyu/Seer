 package org.eclipse.test.performance.ui;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.test.internal.performance.PerformanceTestPlugin;
 import org.eclipse.test.internal.performance.data.Dim;
 import org.eclipse.test.internal.performance.db.DB;
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.TimeSeries;
 import org.eclipse.test.internal.performance.db.Variations;
 
 public class Utils {
 
 	/**
 	 * @param dimension
 	 * @return A description of the dimension.
 	 */
 	public static String getDimensionDescription(String dimension) {
 		/* Descriptions of dimensions */
 		// Windows and Linux
 		Hashtable descriptions = new Hashtable();
 		descriptions.put("cpu time", "Amount of time the process ran on the CPU.");
 
 		descriptions.put("kernel time", "Amount of time the process ran on the CPU, while the CPU was in kernel mode.");
 		descriptions.put("used java heap", "Change in the amount of memory allocated for Java objects.");
 		descriptions.put("working set", "Change in the amount of physical memory used by the process (other data resides in swap space).");
 
 		// Linux
 		descriptions.put("data size", "Change in the process' data and stack memory size.");
 		descriptions.put("hard page faults", "Number of memory pages that were loaded from swap space on disk.");
 		descriptions.put("library size", "Change in the process' library memory size.");
 		descriptions.put("soft page faults",
 				"Number of memory pages that were loaded from memory (i.e., they were not mapped in the process' page table, but already present in memory for some reason).");
 		descriptions.put("text size", "Change in the process' code memory size, useful with start-up tests.");
 
 		// Windows
 		descriptions.put("committed", "Change in the amount of allocated memory (both, in physical memory and swap space, can be preallocated for future use).");
 		descriptions.put("elapsed process", "Amount of wall-clock time.");
 		descriptions.put("gdi objects", "Change in the number of GDI (Graphics Device Interface) objects, can be useful for UI tests (particularly start-up tests).");
 		descriptions
 				.put(
 						"page faults",
 						"Number of memory pages that were loaded from swap space on disk or from memory (i.e., in the latter case, they were not mapped in the process' page table, but already present in memory for some reason).");
 		descriptions.put("system time", "* no longer measured, same as elapsed time, see PerformanceMonitor *");
 		descriptions.put("working set peak", "Increase of the maximum working set size, useful with start-up tests.");
 
 		if (descriptions.get(dimension.toLowerCase()) != null)
 			return descriptions.get(dimension.toLowerCase()).toString();
 		return "";
 	}
 
 	/**
 	 * @param timeSeriesLabels -
 	 *            an array of build ID's with results for a scenario.
 	 * @param current -
 	 *            the current build ID
 	 * @return Build Id's of Nightly builds preceding current.
 	 */
 	public static ArrayList lastSevenNightlyBuildNames(String[] timeSeriesLabels, String current) {
 		int currentIndex = getBuildNameIndex(timeSeriesLabels, current);
 		ArrayList labels = new ArrayList();
 		int j = 6;
 
 		for (int i = timeSeriesLabels.length - 1; i > -1; i--) {
 			if (j == -1)
 				break;
 			String timeSeriesLabel = timeSeriesLabels[i];
 			if (timeSeriesLabel.startsWith("N") && i < currentIndex) {
 				labels.add(timeSeriesLabel);
 				j--;
 			}
 		}
 		return labels;
 	}
 
 	/**
 	 * @return &lt;html&gt;&lt;head&gt;&lt;meta http-equiv="Content-Type"
 	 *         content="text/html; charset=iso-8859-1"&gt;
 	 */
 	public static String HTML_OPEN = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">";
 
 	/**
 	 * @return "&lt;/html&gt;".
 	 */
 	public static String HTML_CLOSE = "</html>";
 
 	/**
 	 * Default style-sheet used on eclipse.org
 	 */
 	public static String HTML_DEFAULT_CSS = "<style type=\"text/css\">" + "p, table, td, th {  font-family: arial, helvetica, geneva; font-size: 10pt}\n"
 			+ "pre {  font-family: \"Courier New\", Courier, mono; font-size: 10pt}\n" + "h2 { font-family: arial, helvetica, geneva; font-size: 18pt; font-weight: bold ; line-height: 14px}\n"
 			+ "code {  font-family: \"Courier New\", Courier, mono; font-size: 10pt}\n" + "sup {  font-family: arial,helvetica,geneva; font-size: 10px}\n"
 			+ "h3 {  font-family: arial, helvetica, geneva; font-size: 14pt; font-weight: bold}\n" + "li {  font-family: arial, helvetica, geneva; font-size: 10pt}\n"
 			+ "h1 {  font-family: arial, helvetica, geneva; font-size: 28px; font-weight: bold}\n"
 			+ "body {  font-family: arial, helvetica, geneva; font-size: 10pt; clip:   rect(   ); margin-top: 5mm; margin-left: 3mm}\n"
 			+ ".indextop { font-size: x-large;; font-family: Verdana, Arial, Helvetica, sans-serif; font-weight: bold}\n"
 			+ ".indexsub { font-size: xx-small;; font-family: Arial, Helvetica, sans-serif; color: #8080FF}\n" + "</style>";
 
 	/**
 	 * An utility object which stores a name, description, url and optional
 	 * output directory.
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
 		public ConfigDescriptor(String name, String description, String url, String outputDir) {
 			this.name = name;
 			this.description = description;
 			this.url = url;
 			this.outputDir = outputDir;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public boolean descriptionMatches(String description) {
 			return description.equals(this.description);
 		}
 	}
 
 	/**
 	 * @param configDescriptors
 	 *            A semi-colon separated listing of config descriptions.<br>
 	 *            Uses the following format: name,description,
 	 *            url,outputdir;name2, description2,url2,output2;etc..
 	 * @return a mapping of config names to their ConfigDescriptors.
 	 */
 	public static Hashtable getConfigDescriptors(String configDescriptors) {
 		// labelMappings in pairs separated by semi-colon ie.
 		// relengbuildwin2,win32-win32-x86;trelenggtk,linux-gtk-x86
 		StringTokenizer tokenizer = new StringTokenizer(configDescriptors, ";");
 		Hashtable configMap = new Hashtable();
 
 		while (tokenizer.hasMoreTokens()) {
 			String labelDescriptor = tokenizer.nextToken();
 			String[] elements = labelDescriptor.split(",");
 			String output = null;
 			if (elements.length == 4)
 				output = elements[3];
 			ConfigDescriptor descriptor = new ConfigDescriptor(elements[0], elements[1], elements[2], output);
 			configMap.put(elements[0], descriptor);
 		}
 		return configMap;
 	}
 
 	/**
 	 * Queries database with variation composed of buildIdPattern, config and
 	 * jvm.
 	 * 
 	 * @return Array of scenarios.
 	 */
 	public static Scenario[] getScenarios(String buildIdPattern, String scenarioPattern, String config, String jvm) {
 		Dim[] qd = null;
 		if (scenarioPattern == null)
 			scenarioPattern = "";
 
 		Variations variations = getVariations(buildIdPattern, config, jvm);
 		return DB.queryScenarios(variations, scenarioPattern + "%", PerformanceTestPlugin.BUILD, qd);
 	}
 
 	/**
 	 * Creates a Variations object using build id pattern, config and jvm.
 	 * 
 	 * @param buildIdPattern
 	 * @param config
 	 * @param jvm
 	 */
 	public static Variations getVariations(String buildIdPattern, String config, String jvm) {
 		String buildIdPatterns = buildIdPattern.replace(',', '%');
 		Variations variations = new Variations();
 		variations.put(PerformanceTestPlugin.CONFIG, config);
 		variations.put(PerformanceTestPlugin.BUILD, buildIdPatterns);
 		variations.put("jvm", jvm);
 		return variations;
 	}
 
 	/**
 	 * @param scenarios
 	 * @return list of unique component names derived from prefixes to scenario
 	 *         names.
 	 */
 	public static ArrayList getComponentNames(Scenario[] scenarios) {
 		ArrayList componentNames = new ArrayList();
 
 		for (int i = 0; i < scenarios.length; i++) {
 			String prefix = null;
 			Scenario scenario = scenarios[i];
 			String scenarioName = scenario.getScenarioName();
 
 			// use part of scenario name prior to .test to identify component
 			if (scenarioName.indexOf(".test") != -1) {
 				prefix = scenarioName.substring(0, scenarioName.indexOf(".test"));
 				if (!componentNames.contains(prefix))
 					componentNames.add(prefix);
 			}
 		}
 		return componentNames;
 	}
 
 	/**
 	 * @param fp -
 	 *            a FingerPrint object
 	 * @return - an html representation of the fingerprint.
 	 */
 	public static String getImageMap(FingerPrint fp) {
 		String componentDescription = fp.configDescriptor.description;
 		String areas = fp.bar.getAreas();
 		if (areas == null)
 			areas = "";
 		String output = "";
 		if (new File(fp.outputDirectory, fp.getOutName() + ".gif").exists()) {
 			output = "<h4>" + componentDescription + "</h4>";
 			output = output.concat("<img src=\"" + fp.getOutName() + ".gif\" usemap=\"#" + fp.outName + "\">" + "<map name=\"" + fp.getOutName() + "\">" + areas + "</map>\n");
 		} else {
 			output = output.concat("<br><br>There is no fingerprint for " + componentDescription + "<br><br>\n");
 		}
 		return output;
 	}
 
 	/**
 	 * Utility method to copy a file.
 	 * 
 	 * @param src -
 	 *            the source file.
 	 * @param dest -
 	 *            the destination.
 	 */
 	public static void copyFile(File src, String dest) {
 
 		try {
 			InputStream in = new FileInputStream(src);
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
 	 * Returns a LineGraph object representing measurements for a scenario over
 	 * builds.
 	 * 
 	 * @param t -
 	 *            the scenario object.
 	 * @param dimensionName -
 	 *            the name of the measurement for which to generate graph.
 	 * @param baseline -
 	 *            the reference build to label.
 	 * @param current -
 	 *            the current build to label.
 	 * @param pointsOfInterest -
 	 *            an array of buildIds. Points for these are highlighted on
 	 *            graph.
 	 * @return a LineGraph object.
 	 */
 	public static TimeLineGraph getLineGraph(Scenario t, String dimensionName, String baseline, String baselinePrefix, String current, ArrayList pointsOfInterest,ArrayList currentBuildIdPrefixes) {
 		Display display = Display.getDefault();
 
 		Color black = display.getSystemColor(SWT.COLOR_BLACK);
 		Color yellow = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
 		Color magenta = display.getSystemColor(SWT.COLOR_MAGENTA);
 
 		String scenarioName = t.getScenarioName();
 		Dim[] dimensions = t.getDimensions();
 		Dim dim = null;
 		for (int i = 0; i < dimensions.length; i++) {
 			if (dimensions[i].getName().equals(dimensionName))
 				dim = dimensions[i];
 		}
 
 		TimeLineGraph graph = new TimeLineGraph(scenarioName + ": " + dimensionName, dim);
 		TimeSeries ts = null;
 		try {
 			ts = t.getTimeSeries(dim);
 			int n = ts.getLength();
 
 			if (n > 0) {
 				for (int j = 0; j < n; j++) {
 					String buildID = ts.getLabel(j);
 					int underscoreIndex = buildID.indexOf('_');
 					String label = (underscoreIndex != -1 && (buildID.equals(baseline) || buildID.equals(current))) ? buildID.substring(0, underscoreIndex) : buildID;
 
 					double value = ts.getValue(j);
 
 					if (buildID.equals(current)) {
 						Color color = black;
 						if (buildID.startsWith("N"))
 							color = yellow;
 
 						graph.addItem("main", label, dim.getDisplayValue(value), value, color, true, getDateFromBuildID(buildID), true);
 						break;
 					}
 					if (pointsOfInterest.contains(buildID)) {
 						graph.addItem("main", label, dim.getDisplayValue(value), value, black, false, getDateFromBuildID(buildID, false), true);
 						continue;
 					}
 					if (buildID.equals(baseline)) {
 						boolean drawBaseline = (baselinePrefix != null) ? false : true;
 						graph.addItem("reference", label, dim.getDisplayValue(value), value, magenta, true, getDateFromBuildID(buildID, true), true, drawBaseline);
 						continue;
 					}
 					if (baselinePrefix != null) {
 						if (buildID.startsWith(baselinePrefix) && !buildID.equals(baseline) && getDateFromBuildID(buildID, true) < getDateFromBuildID(baseline, true)) {
 							graph.addItem("reference", label, dim.getDisplayValue(value), value, magenta, false, getDateFromBuildID(buildID, true), false);
 							continue;
 						}
 					}
 					if (lastSevenNightlyBuildNames(t.getTimeSeriesLabels(), current).contains(buildID)) {
 						graph.addItem("main", buildID, dim.getDisplayValue(value), value, yellow, false, getDateFromBuildID(buildID), false);
 						continue;
 					} else if (buildID.startsWith("N"))
 							continue;
 					
 					for (int i=0;i<currentBuildIdPrefixes.size();i++){
 						if (buildID.startsWith(currentBuildIdPrefixes.get(i).toString())) {
 							graph.addItem("main", buildID, dim.getDisplayValue(value), value, black, false, getDateFromBuildID(buildID), false);
 							break;
 						}
 					}
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
 	 * 
 	 * @param p -
 	 *            the LineGraph object.
 	 * @param output -
 	 *            the output file path.
 	 */
 	public static void printLineGraphGif(LineGraph p, String output) {
 		File outputFile = new File(output);
 		outputFile.getParentFile().mkdir();
 		int GRAPH_WIDTH = 600;
 		int GRAPH_HEIGHT = 200;
 		Image image = new Image(Display.getDefault(), GRAPH_WIDTH, GRAPH_HEIGHT);
 		p.paint(image);
 
 		/* Downscale to 8 bit depth palette to save to gif */
 		ImageData data = Utils.downSample(image);
 		ImageLoader il = new ImageLoader();
 		il.data = new ImageData[] { data };
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
 	 * Utility method which returns HTML code representing an image map for a
 	 * LineGraph object.
 	 * 
 	 * @param p -
 	 *            the LineGraph object.
 	 * @param imageSource -
 	 *            the path to insert for the src attribute to <img>.
 	 */
 	public static String getImageMap(LineGraph p, String imageSource, String rawDataFileLink) {
 		String result = "";
 		String areas = p.getAreas();
 
 		result = result.concat("<img" + " src=\"" + imageSource + "\" usemap=\"#" + p.fTitle + "\">");
 		result = result.concat("<map name=\"" + p.fTitle + "\">");
 		result = result.concat(areas);
 		result = result.concat("</map>\n");
 
 		return result;
 	}
 
 	/**
 	 * A utility which returns the index of a given buildId in an array.
 	 * 
 	 * @param timeSeriesLabels -
 	 *            array of buildIds
 	 * @param buildId -
 	 *            the buildId for which to find return value.
 	 * @return The index of the buildID in the array.
 	 */
 	public static int getBuildNameIndex(String[] timeSeriesLabels, String buildId) {
 		for (int i = timeSeriesLabels.length - 1; i > -1; i--) {
 			String timeSeriesLabel = timeSeriesLabels[i];
 			if (timeSeriesLabel.startsWith(buildId))
 				return i;
 		}
 		return -1;
 	}
 
 	/**
 	 * Downsample Image to 8 bit depth format so that the resulting image data
 	 * can be saved to GIF. Note. If the source image contains photo quality
 	 * content with more than 256 colours, resulting data will look very poor.
 	 */
 	static int closest(RGB[] rgbs, int n, RGB rgb) {
 		int minDist = 256 * 256 * 3;
 		int minIndex = 0;
 		for (int i = 0; i < n; ++i) {
 			RGB rgb2 = rgbs[i];
 			int da = rgb2.red - rgb.red;
 			int dg = rgb2.green - rgb.green;
 			int db = rgb2.blue - rgb.blue;
 			int dist = da * da + dg * dg + db * db;
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
 		if (!data.palette.isDirect && data.depth <= 8)
 			return data;
 
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
 		for (int i = 0; i < n; ++i)
 			rgbs[i] = counters[i].rgb;
 		if (mask != null) {
 			rgbs[rgbs.length - 1] = data.transparentPixel != -1 ? data.palette.getRGB(data.transparentPixel) : new RGB(255, 255, 255);
 		}
 		PaletteData palette = new PaletteData(rgbs);
 
 		// create a new image using the new palette:
 		// for each pixel in the old image, look up the best matching
 		// index in the new palette
 		ImageData newData = new ImageData(width, data.height, 8, palette);
 		if (mask != null)
 			newData.transparentPixel = rgbs.length - 1;
 		for (int y = 0, height = data.height; y < height; ++y) {
 			data.getPixels(0, y, width, pixels, 0);
 			if (mask != null)
 				mask.getPixels(0, y, width, maskPixels, 0);
 			for (int x = 0; x < width; ++x) {
 				if (mask != null && maskPixels[x] == 0) {
 					pixels[x] = rgbs.length - 1;
 				} else {
 					RGB rgb = data.palette.getRGB(pixels[x]);
 					pixels[x] = closest(rgbs, n, rgb);
 				}
 			}
 			newData.setPixels(0, y, width, pixels, 0);
 		}
 		return newData;
 	}
 
 	/**
 	 * Returns the date/time from the build id in format yyyymmddhm
 	 * 
 	 * @param buildId
 	 * @return date/time in format YYYYMMDDHHMM, ie. 200504060010
 	 */
 	public static long getDateFromBuildID(String buildId) {
 		return Utils.getDateFromBuildID(buildId, false);
 	}
 
 	public static long getDateFromBuildID(String buildId, boolean matchLast) {
 		Calendar calendar = Calendar.getInstance();
 
 		if (buildId.indexOf('_') != -1) {
 			String[] buildIdParts = buildId.split("_");
 
 			int buildIdSegment = 1;
 			if (matchLast)
 				buildIdSegment = buildIdParts.length - 1;
 			// if release build, expect <release>_<release date and
 			// timestamp>_<date and timestamp test ran>
 			// use test date and time for plotting
 			int year = Integer.parseInt(buildIdParts[buildIdSegment].substring(0, 4));
 			int month = Integer.parseInt(buildIdParts[buildIdSegment].substring(4, 6)) - 1;
 			int date = Integer.parseInt(buildIdParts[buildIdSegment].substring(6, 8));
 			int hours = Integer.parseInt(buildIdParts[buildIdSegment].substring(8, 10));
 			int min = Integer.parseInt(buildIdParts[buildIdSegment].substring(10, 12));
 
 			calendar.set(year, month, date, hours, min);
 			return calendar.getTimeInMillis();
 
 		} else if (buildId.indexOf('-') != -1) {
 			// if regular build, expect <buildType><date>-<time> format
 			String[] buildIdParts = buildId.split("-");
 			int year = Integer.parseInt(buildIdParts[0].substring(1, 5));
 			int month = Integer.parseInt(buildIdParts[0].substring(5, 7)) - 1;
 			int date = Integer.parseInt(buildIdParts[0].substring(7, 9));
 			int hours = Integer.parseInt(buildIdParts[1].substring(0, 2));
 			int min = Integer.parseInt(buildIdParts[1].substring(2, 4));
 			calendar.set(year, month, date, hours, min);
 
 			return calendar.getTimeInMillis();
 		}
 
 		return -1;
 	}
 	public static void printVariabilityTable(Hashtable rawDataTables, String outputFile, String[] configList) {
 		Hashtable configs=new Hashtable();
 		for (int i=0;i<configList.length;i++){
 			String configName=configList[i];
 			ConfigDescriptor cd=new ConfigDescriptor(configName,configName,configName,new File(outputFile).getParent()+"/"+configName);
 			configs.put(configName,cd);
 		}
 		printVariabilityTable(rawDataTables,outputFile,configs);
 	}
 
 	public static void printVariabilityTable(Hashtable rawDataTables, String outputFile, Hashtable configDescriptors) {
		//map of config descriptions to their output locations
		Hashtable configURLs=new Hashtable();
		Enumeration keys=configDescriptors.keys();
		
		while (keys.hasMoreElements()){
			ConfigDescriptor descriptor=(ConfigDescriptor)configDescriptors.get(keys.nextElement());
			configURLs.put(descriptor.description,descriptor.url);
		}
		
 		String[] scenarios = (String[]) rawDataTables.keySet().toArray(new String[rawDataTables.size()]);
 		
 		Arrays.sort(scenarios);
 		PrintWriter out=null;
 		try {
 			out = new PrintWriter(new FileWriter(new File(outputFile)));
 			out.println(Utils.HTML_OPEN + "</head><body>\n");
 			out.println("<h3>Summary of Elapsed Process Variation Coefficients</h3>\n"+
 		"<p> This table provides a bird's eye view of variability in elapsed process times\n"+ 
 		  "for baseline and current build stream performance scenarios." +
 		  " This summary is provided to facilitate the identification of scenarios that should be examined due to high variability." +
 		  "The variability for each scenario is expressed as a <a href=\"http://en.wikipedia.org/wiki/Coefficient_of_variation\">coefficient\n"+ 
 		  "of varation</a> (CV). The CV is calculated by dividing the <b>standard deviation\n"+ 
 		  "of the elapse process time over builds</b> by the <b>average elapsed process\n"+ 
 		  "time over builds</b> and multiplying by 100.\n"+ 
 		"</p><p>High CV values may be indicative of any of the following:<br></p>\n"+
 		"<ol><li> an unstable performance test. </li>\n"+
 		  "<ul><li>may be evidenced by an erratic elapsed process line graph.<br><br></li></ul>\n"+
 		  "<li>performance regressions or improvements at some time in the course of builds.</li>\n"+
 		  "<ul><li>may be evidenced by plateaus in elapsed process line graphs.<br><br></li></ul>\n"+
 		  "<li>unstable testing hardware.\n" +
 		  "<ul><li>consistent higher CV values for one test configuration as compared to others across" +
 		  " scenarios may be related to hardward problems.</li></ul></li></ol>\n"+
 		"<p> Scenarios are listed in alphabetical order in the far right column. A scenario's\n"+ 
 		  "variation coefficients (CVs) are in columns to the left for baseline and current\n"+ 
 		  "build streams for each test configuration. Scenarios with CVs > 10% are highlighted\n"+ 
 		  "in yellow (10%<CV>&lt;CV<20%) and orange(CV>20%). </p>\n"+
 		"<p> Each CV value links to the scenario's detailed results to allow viewers to\n"+ 
 		  "investigate the variability.</p>\n");
 			
 		  	int configColumns=configDescriptors.size();
 			out.println("<table border=\"1\"><tr>" +
 					    "<td colspan=\""+configColumns+"\"><b>Baseline CVs</b></td>"+
 					    "<td colspan=\""+configColumns+"\"><b>Current Build Stream CVs</b></td>"+
 					    "<td rowspan=\"2\"><b>Scenario Name</b></td>"+
 					    "</tr><tr>");
 	
 			Hashtable cvTable = (Hashtable) rawDataTables.get(scenarios[0]);
 			String[] configNames = (String[]) cvTable.keySet().toArray(new String[cvTable.size()]);
 			Arrays.sort(configNames);
 
 			for (int i = 0; i < configNames.length; i++) {
 				//configNames here have prefix cConfig- or bConfig- depending on whether the data comes from 
 				//current build stream data or baseline data.
				out.print("<td>" + configNames[i].substring(8) + "</td>");
 			}
 			out.println("</tr><tr>\n");
 
 			for (int i = 0; i < scenarios.length; i++) {
 				Hashtable aCvTable = (Hashtable) rawDataTables.get(scenarios[i]);
 				String scenario = scenarios[i];
 				String scenarioFile=scenario.replace('#', '.').replace(':', '_').replace('\\', '_')+".html";
 				
 				for (int j = 0; j < configNames.length; j++) {
					String url=configURLs.get(configNames[j].split("Config-")[1])+"/"+scenarioFile;
 					if (aCvTable.get(configNames[j]) == null) {
 						out.print("<td>n/a</td>");
 						continue;
 					}
 					String displayValue = aCvTable.get(configNames[j]).toString();
 					if (displayValue==null){
 						out.print("<td>n/a</td>");
 						continue;
 					}
 					try {
 						double value = Double.parseDouble(displayValue.substring(0, displayValue.length() - 1));
 						if (value > 10 && value < 20)
 							out.print("<td bgcolor=\"yellow\"><a href=\""+url+"\"/>" + displayValue + "</a></td>");
 						else if (value >= 20)
 							out.print("<td bgcolor=\"FF9900\"><a href=\""+url+"\">" + displayValue + "</a></td>");
 						else
 							out.print("<td><a href=\""+url+"\">" + displayValue + "</a></td>");
 					} catch (NumberFormatException e) {
 						e.printStackTrace();
 						out.print("<td>n/a</td>");
 					}
 				}
 				out.println("<td>" + scenario + "</td>");
 				out.println("</tr>\n");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}finally{
 			out.println("</table></body></html>");
 			out.flush();
 			out.close();
 		}
 	}
 }
