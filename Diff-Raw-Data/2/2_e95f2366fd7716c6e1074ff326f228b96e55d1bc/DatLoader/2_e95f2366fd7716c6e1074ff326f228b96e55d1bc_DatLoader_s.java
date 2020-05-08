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
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * This class loads a dat data files where:
  * 
  * 0. The file is ascii/utf-8
  * 1. The header is a section at the start and starting with a #
  * 2. The footer is a section at the end and starting with a #
  * 3. The data lines consist only of numbers separated by whitespace, 2 columns or more.
  * 4. The names of the data sets are defined in the last line of the header. They are separated
  *    by white space but if commas are used as well these will be stripped from the names.
  *    A single space without a comma is not included as data set names may contain spaces.
  *    
  *    
  *    Example: (see also unit tests)
  *    
  *    
 # DIAMOND LIGHT SOURCE
 # Instrument: I20-XAS Date: Mon, 19 Jul 2010 13:54:16 BST
 # Ring energy: 56.66749 GeV
 # Initial ring current: 72.23874 mA
 # Filling mode: 50.05388
 # Wiggler gap selected: 0.00000 mm
 #  
 # Primary slits:  vertical gap= 0.00000 mm; horizontal gap= 0.00000 mm; vertical offset= 0.00000 mm; horizontal offset= 0.00000 mm
 # Secondary slits:  vertical gap= 0.00000 mm; horizontal gap= 0.00000 mm; vertical offset= 0.00000 mm; horizontal offset= 0.00000 mm
 # Experimental slits:  vertical gap= 0.00000 mm; horizontal gap= 0.00000 mm; vertical offset= 0.00000 mm; horizontal offset= 0.00000 mm
 #  
 # Optic Hutch Mirrors Coating: 77.14589850662986
 # Monochromator crystal cut: 84.35454401226806
 # Incident angle for Harmonic Rejection Mirrors:     90.46 mrad
 # Harmonic rejection mirrors coating: 57.76820936133954
 #
 # Ascii output file name: 'FeKedge_1_15.dat'
 # Nexus output file: '/scratch/users/data/2010/cm1903-4/Experiment_1/nexus/FeKedge_1_15.nxs'
 # The XML files, ScanParameters, SampleParameters, DetectorParameters, OutputParameters
 # are stored in the nexus file.
 #
 # Sample name: Please set a sample name...
 # Please add a description...
 #
 # Detector: Ge (XSPRESS)
 #
 # Dark current intensity (Hz): I0    150   It    230   Iref    135
 # Dark current has been automatically removed from counts in main scan (I0,It,Iref)
 #
 # Energy          	I0              	It              	Iref            	ln(I0/It)       	ln(It/Iref)     	FF              	FF/I0           	Integration Time	Element 0       	Element 1       	Element 2       	Element 3       	Element 4       	Element 5       	Element 6       	Element 7       	Element 8       	Element 9       	Element 10      	Element 11      	Element 12      	Element 13      	Element 14      	Element 15      	Element 16      	Element 17      	Element 18      	Element 19      	Element 20      	Element 21      	Element 22      	Element 23      	Element 24      	Element 25      	Element 26      	Element 27      	Element 28      	Element 29      	Element 30      	Element 31      	Element 32      	Element 33      	Element 34      	Element 35      	Element 36      	Element 37      	Element 38      	Element 39      	Element 40      	Element 41      	Element 42      	Element 43      	Element 44      	Element 45      	Element 46      	Element 47      	Element 48      	Element 49      	Element 50      	Element 51      	Element 52      	Element 53      	Element 54      	Element 55      	Element 56      	Element 57      	Element 58      	Element 59      	Element 60      	Element 61      	Element 62      	Element 63      	
 6912.0000       	134878.0        	2040284.0       	295077.5        	-2.716          	1.934           	10270610.99     	10555624.6977   	0.5000          	-12522.13       	-39259.72       	240563.80       	-18371.26       	-23133.33       	-26645.71       	-8850.84        	-32861.71       	564452.59       	478611.82       	226456.06       	168938.08       	102221.88       	-1823.24        	-4091.32        	-47298.75       	726493.32       	-52899.04       	-32855.20       	593012.73       	199522.96       	202352.74       	185485.28       	38464.16        	-65122.61       	-10684.71       	-1507.41        	423263.14       	-4528.40        	350455.46       	-21235.13       	-75253.78       	-4209.37        	165226.15       	35165.22        	-78922.48       	-10288.62       	-77462.30       	-6722.40        	354852.04       	418246.19       	-30032.34       	-75643.93       	-3506.16        	92636.17        	-41200.23       	301540.78       	460221.12       	989244.43       	102729.58       	23667.98        	327453.07       	-42080.14       	102140.45       	-22449.90       	407986.95       	601299.66       	573668.62       	157472.43       	551740.78       	258796.50       	117267.94       	87473.48        	512949.58       	
 6917.0000       	111091.0        	1680648.0       	295086.5        	-2.717          	1.740           	8069498.24      	9446808.2869    	0.5000          	421871.76       	-17924.10       	-23503.70       	-18711.85       	-21437.16       	97863.36        	-7528.52        	90575.20        	562247.53       	9261.75         	-10753.65       	-6481.09        	511078.54       	-2886.82        	188671.06       	-76532.81       	144969.57       	591234.90       	-77537.48       	-13101.69       	78814.69        	529177.36       	573390.24       	992058.81       	169218.60       	-24463.02       	-76107.58       	144951.34       	492050.24       	-6852.50        	-61568.14       	-19371.64       	268265.42       	-43395.58       	-50644.15       	442379.96       	-25656.06       	-20971.66       	124481.27       	30026.80        	-9798.52        	-44064.34       	62571.57        	629673.44       	440609.56       	274190.73       	-19712.58       	247120.50       	-10970.01       	-18210.44       	-6132.86        	-52240.73       	-19340.80       	169917.83       	-87544.39       	-14745.72       	369390.43       	-2243.35        	78356.91        	-618.21         	-73468.71       	113662.67       	-32029.68       	217965.70       	
 6922.0000       	108474.0        	1641726.0       	295080.5        	-2.717          	1.716           	9099845.78      	10278670.8288   	0.5000          	388648.16       	672346.44       	523813.82       	179272.73       	143237.35       	-23828.01       	-26426.86       	-9288.31        	130818.56       	10848.06        	80126.47        	199052.08       	-54048.36       	562720.47       	428897.71       	157519.51       	-49771.42       	-38585.22       	198793.59       	-41702.60       	-55170.40       	328924.55       	-38532.78       	638121.31       	82337.46        	-19266.79       	374849.82       	36781.83        	40839.37        	459302.23       	13776.24        	-60335.77       	-52474.61       	-3892.66        	308830.00       	170752.88       	-12940.66       	533910.29       	-32203.15       	105197.46       	-4579.55        	767756.14       	470172.54       	192161.92       	54539.36        	270370.56       	-25394.73       	-13256.13       	-28438.05       	264131.67       	-5296.58        	154772.18       	-24307.41       	-12717.96       	-3569.43        	164294.45       	216975.65       	389819.72       	-43662.48       	-61559.76       	-5179.87        	94455.12        	68302.55        	-31194.92       	
 6927.0000       	108183.0        	1637764.0       	295081.5        	-2.717          	1.714           	6913071.88      	10655300.4342   	0.5000          	252997.35       	146202.54       	-20143.06       	166291.42       	478239.89       	-15245.91       	241066.30       	892037.59       	30055.41        	26569.29        	-18963.93       	-52547.75       	-7778.36        	665512.66       	321108.96       	-7330.62        	-4992.67        	-72195.55       	-33564.24       	36970.05        	-19453.16       	145710.39       	7210.57         	271347.83       	134907.53       	559452.65       	-22718.89       	44485.74        	311239.99       	648890.80       	-43491.14       	365036.73       	103054.76       	85826.55        	-10587.68       	171858.66       	219394.94       	113446.85       	76539.88        	212918.00       	-30767.21       	100902.77       	-57468.26       	-29552.13       	114026.72       	356607.93       	16867.18        	-21828.87       	-28344.87       	-39659.98       	-78626.70       	-7579.99        	-18246.21       	94921.76        	36276.76        	-11083.56       	-12283.48       	81419.01        	-60623.67       	116170.67       	-27800.29       	21024.00        	-39.34          	-600.72         	
 6932.0000       	106587.0        	1613746.0       	295083.5        	-2.717          	1.699           	10105332.26     	9346514.1243    	0.5000          	70999.97        	671639.66       	100746.97       	-50466.13       	180646.73       	371747.26       	27040.41        	-38820.46       	210770.91       	109409.64       	426746.80       	-25636.44       	709006.61       	-43193.62       	158345.44       	236952.14       	-26740.35       	-13963.00       	351078.03       	214539.53       	217651.74       	480419.04       	-1349.22        	162630.78       	9663.93         	247932.24       	-53587.21       	-22906.08       	-478.60         	536088.63       	-488.26         	372896.99       	15890.97        	114261.48       	3446.13         	-15841.29       	411066.81       	-74108.10       	-19437.23       	732304.44       	-8511.52        	-20428.18       	112549.69       	64122.86        	23501.66        	-8777.49        	426582.51       	356181.24       	-16.91          	-20362.83       	-60131.00       	548368.66       	-14238.60       	380402.00       	-3020.60        	-2341.35        	88064.37        	924556.90       	-56480.55       	294788.70       	-35404.98       	158376.77       	43115.64        	157527.98       	
 
 It is also legal to have no header section at all and just columns of white space separated numbers.
 In this case the columns will be labelled Column_1...Column_N.
  */
 public class DatLoader extends AbstractFileLoader implements IMetaLoader {
 	
 	transient protected static final Logger logger = LoggerFactory.getLogger(DatLoader.class);
 	
 	transient private static final String  FLOAT = "([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)|(0\\.)";
 	transient private static final Pattern DATA  = Pattern.compile("^(("+FLOAT+")\\s+)+("+FLOAT+")$");
 
 	protected String                    fileName;
 	protected List<String>              header;
 	protected Map<String,String>        metaData;
 	protected List<String>              footer;
 	protected Map<String, List<Double>> vals;
 	protected int                       columnIndex;
 
 	private ExtendedMetadata metadata;
 
 	public DatLoader() {
 	
 	}
 	
 	/**
 	 * @param fileName
 	 */
 	public DatLoader(final String fileName) {
 		setFile(fileName);
 	}
 	
 	public void setFile(final String fileName) {
 		this.fileName = fileName;
 		this.header   = new ArrayList<String>(31);
 		this.metaData = new HashMap<String,String>(7);
 		this.footer   = new ArrayList<String>(7);
 		
 		// Important must use LinkedHashMap as order assumes is insertion order.
 		this.vals   = new LinkedHashMap<String, List<Double>>();
 	}
 
 	@Override
 	public DataHolder loadFile() throws ScanFileHolderException {
 		return loadFile((IMonitor)null);
 	}
 	
 	/**
 	 * Function that loads in the standard SRS datafile
 	 * 
 	 * @return The package which contains the data that has been loaded
 	 * @throws ScanFileHolderException
 	 */
 	@Override
 	public DataHolder loadFile(final IMonitor mon) throws ScanFileHolderException {
         final DataHolder result = loadFile(null, mon);
 		return result;
 	}
 
 	private DataHolder loadFile(final String name, final IMonitor mon) throws ScanFileHolderException {
 		
 		// first instantiate the return object.
 		final DataHolder result = new DataHolder();
 		
 		// then try to read the file given
 		BufferedReader in = null;
 		try {
 			in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
 			
 			boolean readingFooter = false;
 			
 			String line	= parseHeaders(in, name, mon);
 
 			// Read data
 			DATA: while (line != null) {
 				
 				if (mon!=null) mon.worked(1);
 				if (mon!=null && mon.isCancelled()) {
 					throw new ScanFileHolderException("Loader cancelled during reading!");
 				}
 				
 				line = line.trim();
 				if (!readingFooter && DATA.matcher(line).matches()) {
 					
 					if (line.startsWith("#")) {
 						readingFooter = true;
 						break DATA;
 					}
 					if (vals.isEmpty()) throw new ScanFileHolderException("Cannot read header for data set names!");
 					
 					final String[] values = line.split("\\s+");
 					if (columnIndex>-1 && name!=null) {
 					    final String value = values[columnIndex];
 					    vals.get(name).add(Utils.parseDouble(value.trim()));
 					} else {
 						if (values.length != vals.size()) {
 							throw new ScanFileHolderException("Data and header must be the same size!");
 						}
 						final Iterator<String> it = vals.keySet().iterator();
 						for (String value : values) {
 							vals.get(it.next()).add(Utils.parseDouble(value.trim())); 
 						}
 					}
 					
 				}
 				
 				line = in.readLine();
 			}
 			
 			// Footer
 			footer.clear();
 			while ((line =in.readLine()) != null) {	
 				if (readingFooter) {
 					if (line.startsWith("#")) {
 						footer.add(line);
 						continue;
 					}
 					throw new ScanFileHolderException("Every line in the footer must start with #");
 				}
 			}
 
 			for (String n : vals.keySet()) {
 				final AbstractDataset set =  AbstractDataset.createFromList(vals.get(n));
 				set.setName(n);
 				result.addDataset(n, set);
 			}		
 
 			if (loadMetadata) {
 				createMetadata();
 				result.setMetadata(metadata);
 			}
 			return result;
 			
 		} catch (Exception e) {
 			throw new ScanFileHolderException("DatLoader.loadFile exception loading  " + fileName, e);
 			
 		} finally {
 			try {
 				if (in!=null) in.close();
 			} catch (IOException e) {
 				throw new ScanFileHolderException("Cannot close stream from file  " + fileName, e);
 			}
 		}
 	}
 
 	public AbstractDataset loadSet(final String path, final String name, final IMonitor mon) throws Exception {
 		
 		setFile(path);
 		/**
 		 * TODO Instead of **loading everything each time**, we should get the column
 		 * number from the name, and write the algorithm to extract 
 		 */
 		final DataHolder dh = loadFile(name, mon);
 		return dh.getDataset(name);
 	}
 	
 	/**
 	 * There are no efficiency gains in using this method, it reads everything in and garbage
 	 * collects what is not needed.
 	 */
 	public Map<String,ILazyDataset> loadSets(String path, List<String> names, IMonitor mon) throws Exception {
 		
 		setFile(path);
 		/**
 		 * TODO Instead of **loading everything each time**, we should get the column
 		 * number from the name, and write the algorithm to extract 
 		 */
 		final DataHolder dh = loadFile(mon);
 		final Map<String,ILazyDataset> ret = dh.toLazyMap();
 		ret.keySet().retainAll(names);
 		return ret;
 	}	
 
 
 	@Override
 	public void loadMetaData(final IMonitor mon) throws Exception {
 
 		final BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
 		int count = 1;
 		try {
 			parseHeaders(br, null, mon);
 			// We assume the rest of the lines not starting with # are all
 			// data lines in getting the meta data. We do not parse these lines.
 			String line=null;
 			while ((line = br.readLine()) != null) {	
 				line = line.trim();
 				if (line.startsWith("#")) break;
 				count++;
 			}
 			
 		} finally {
 			br.close();
 		}
 		createMetadata(count);
 	}
 	
 	private void createMetadata() {
 		createMetadata(-1);
 	}
 	
 	private void createMetadata(int approxSize) {
 		metadata = new ExtendedMetadata(new File(fileName));
 		metadata.setMetadata(metaData);
 		for (Entry<String, List<Double>> e : vals.entrySet()) {
 			if (approxSize>-1 &&  e.getValue().size()<1) {
 			    metadata.addDataInfo(e.getKey(), approxSize);
 			} else {
 			    metadata.addDataInfo(e.getKey(), e.getValue().size());
 			}
 		}
 	}
 
 	@Override
 	public IMetaData getMetaData() {
 		return metadata;
 	}
 
 	private static Pattern SCAN_LINE = Pattern.compile("#S \\d+ .*");
 	private static Pattern DATE_LINE = Pattern.compile("#D (Sun|Mon|Tue|Wed|Thu|Fri|Sat) [a-zA-Z]+ \\d+ .*");
 	/**
 	 * This method parses the headers. It tries to throw an exception
 	 * if it is sure an SRS file is found. Also it looks in the headers
 	 * and if it finds "#S" and "#D" it thinks that the file is a multi-scan
 	 * spec file and throws an exception.
 	 * 
 	 * Example of multi-scan spec file characteristic lines:
 	   #S 1  ascan  pvo -0.662 1.338  20 0.1
        #D Sat Apr 02 10:19:13 2011
 	 * 
 	 * @param in
 	 * @param name
 	 * @param mon
 	 * @return last line
 	 * @throws Exception
 	 */
 	private String parseHeaders(final BufferedReader in, final String name, IMonitor mon) throws Exception {
 		
 		String line = in.readLine();
 		if (line.trim().startsWith("&")) throw new Exception("Cannot load SRS files with DatLoader!");
 		metaData.clear();
 		header.clear();
 		vals.clear();
 		
 		boolean foundHeaderLine = false;
 		boolean wasScanLine     = false;
 		while (line.startsWith("#") || "".equals(line.trim())) {
 			
 			try {
 				if ("".equals(line.trim())) continue;
 				foundHeaderLine = true;
 				
 				if (mon!=null) mon.worked(1);
 				if (mon!=null && mon.isCancelled()) {
 					throw new ScanFileHolderException("Loader cancelled during reading!");
 				}
 				
 				if (wasScanLine && DATE_LINE.matcher(line.trim()).matches()) {
 					throw new ScanFileHolderException("This file is a multi-scan spec file - use SpecLoader instead!");
 				}
 				wasScanLine = SCAN_LINE.matcher(line.trim()).matches();
 				
 				header.add(line);
 				
 				// This caused problems with some of B18's files, so changing the methodology a little
 //				if (line.indexOf("=")>-1) {
 //					metaData.put(line.substring(0,line.indexOf("=")-1), line.substring(line.indexOf("=")+1));
 //				} else if (line.indexOf(":")>-1) {
 //					metaData.put(line.substring(0,line.indexOf(":")-1), line.substring(line.indexOf(":")+1));
 //				}
 		
 				if (line.contains(":")) {
 					String[] parts = line.split(":");
 					String key = parts[0].replace("#", "");
 					String value = parts[1];
 					for (int p = 2; p < parts.length; p++) {
 						value = value+":"+parts[p];
 					}
 					metaData.put(key.trim(),value.trim());
 				}
 				
 				
 			} finally {
 			    line = in.readLine();
 			}
 		}
 
 		if (header.size() < 1) {
 			if (!foundHeaderLine) {
 				final String[] values = line.trim().split("\\s+");
 				this.columnIndex = -1;
				int p = (int) Math.ceil(Math.log10(values.length));
 				String fmt = String.format("col%%0%dd", p); // same as python loader
 				for (int i = 0; i < values.length; i++) {
 					vals.put(String.format(fmt, i+1), new ArrayList<Double>(89));
 				}
 			}
 			return line;
 		}
 
 		final String lastHeaderLine = header.get(header.size()-1);
 		final String[] values = line.trim().split("\\s+");
 		
 		if (name!=null) {
 			this.columnIndex = -1;
 			vals.put(name, new ArrayList<Double>(89));
 			
 			// Busy line, been looking at too much python...
 			List<String> headers = new ArrayList<String>(Arrays.asList(lastHeaderLine.substring(1).trim().split("\\s{2,}|\\,\\s+|\\t")));
 			if (values.length > headers.size()) {
 				for (int j = headers.size(); j < values.length; j++) {
 					headers.add("Unknown"+j);
 				}			
 			}
 			for (int i = 0; i < headers.size(); i++) {
 				if (headers.get(i).equals(name)) {
 					columnIndex = i;
 					break;
 				}
 			}
             		    
 		} else {
 			createValues(vals, lastHeaderLine);
 
 		    // Check first line and headers are the same, sometimes the value names are not
 			// provided in parsable syntax
 			if (values.length > vals.size()) {
 				for (int j = vals.size(); j < values.length; j++) {
 					vals.put("Unknown"+j, new ArrayList<Double>(89));
 				}
 			}
 		}
 		
 		return line;
 	}
 	
 	
 
 	private void createValues(Map<String, List<Double>> v, String header) {
 		
 		// Two or more spaces or a comma and one more more space
 		final String[] headers = header.substring(1).trim().split("\\s{2,}|\\,\\s+|\\t");
 		
 		for (String name : headers) {
 			v.put(name, new ArrayList<Double>(89));
 		}
 	}
 }
