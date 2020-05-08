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
 
 import gda.analysis.io.IFileSaver;
 import gda.analysis.io.ScanFileHolderException;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.StringDataset;
 import uk.ac.gda.monitor.IMonitor;
 
 /**
  * This class loads a SRS data files
  * <p>
  * <b>Note</b>: the metadata from this loader is left as strings
  */
 public class SRSLoader extends AbstractFileLoader implements IFileSaver, IMetaLoader {
 	
 	transient protected static final Logger logger = LoggerFactory.getLogger(SRSLoader.class);
 
 	protected String fileName;
 	protected List<String> datasetNames = new ArrayList<String>();
 	protected Map<String, String> textMetadata = new HashMap<String, String>();
 	protected List<String> extraHeaders = new ArrayList<String>();
 	
 	private boolean storeStringValues = false;
 	private boolean useImageLoaderForStrings = true;
 
 	public SRSLoader() {
 	}
 	
 	/**
 	 * @param FileName
 	 */
 	public SRSLoader(String FileName) {
 		fileName = FileName;
 	}
 
 	public void setFile(final String fileName) {
 		this.fileName = fileName;
 	}
 
 	/**
 	 * 
 	 * @return if columns containing strings are added to the DataHolder as a StringDataSet. 
 	 *         Retrieve from DataHolder using getLazyDataSet
 	 *         
 	 */
 	public boolean isStoreStringValues() {
 		return storeStringValues;
 	}
 
 	public void setStoreStringValues(boolean storeStringValues) {
 		this.storeStringValues = storeStringValues;
 	}
 
 	
 	/**
 	 * 
 	 * @return if columns containing strings that point to image file are treated as such and added to DataHolder
 	 * 
 	 */	
 	public boolean isUseImageLoaderForStrings() {
 		return useImageLoaderForStrings;
 	}
 
 	public void setUseImageLoaderForStrings(boolean useImageLoaderForStrings) {
 		this.useImageLoaderForStrings = useImageLoaderForStrings;
 	}
 
 	@Override
 	public DataHolder loadFile() throws ScanFileHolderException {
 		return loadFile(null);
 	}
 
 	private final Pattern splitRegex = Pattern.compile("\\s+");
 
 	/**
 	 * Function that loads in the standard SRS datafile
 	 * 
 	 * @return The package which contains the data that has been loaded
 	 * @throws ScanFileHolderException
 	 */
 	@Override
 	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
 		
 		// first instantiate the return object.
 		DataHolder result = new DataHolder();
 
 		// then try to read the file given
 		BufferedReader in = null;
 		
 		try {
 			in = new BufferedReader(new FileReader(fileName));
 			String dataStr;
 			String headStr;
 			// an updated header reader grabs all the metadata
 			readMetadata(in, mon);
 			if (textMetadata.size() > 0)
 				result.setMetadata(getMetaData());
 
 			// read in the names of the different datasets which will be needed
 			headStr = in.readLine();
 			headStr = headStr.trim();//remove whitespace to prevent the following split on white
 			String[] vals = splitRegex.split(headStr);
 			datasetNames.clear();
 			datasetNames.addAll(Arrays.asList(vals));
 			
 			List<?> [] columns = new List<?>[vals.length];
 
 			// now add the data to the appropriate vectors
 			while ((dataStr = in.readLine()) != null) {
 				
 				if (mon!=null) {
 					mon.worked(1);
 					if (mon.isCancelled()) throw new ScanFileHolderException("Load cancelled!");
 				}
 
 				parseColumns(splitRegex.split(dataStr.trim()), columns);
 			}
 
 			convertToDatasets(result, vals, columns, isStoreStringValues(), isUseImageLoaderForStrings(), (new File(this.fileName)).getParent());
 			
 			if (result.getMap().isEmpty()) throw new Exception("Cannot parse "+fileName+" into datasets!");
 			
 		} catch (Exception e) {
 			throw new ScanFileHolderException("SRSLoader.loadFile exception loading  " + fileName, e);
 		} finally {
 			try {
 				if (in!=null) in.close();
 			} catch (IOException e) {
 				throw new ScanFileHolderException("Cannot read file", e);
 			}
 			
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * Parse columns into lists
 	 * @param data
 	 * @param columns
 	 * @throws ScanFileHolderException
 	 */
 	@SuppressWarnings("unchecked")
 	protected static void parseColumns(String[] data, List<?>[] columns) throws ScanFileHolderException {
 		int cols = data.length;
 		if (cols > columns.length) {
 			logger.warn("Number of columns on data line {} exceeds number of headers - ignoring excess columns", cols);
 			cols = columns.length;
 		}
 
 		for (int i = 0; i < cols; i++) {
 			String text = data[i];
 			if (columns[i] != null) {
 				List<?> list = columns[i];
 				Object first = list.get(0);
 				if (first instanceof Number) {
 					List<Number> listN = (List<Number>) list;
 					listN.add(Utils.parseValue(text));
 				} else if (first instanceof String) {
 					List<String> listN = (List<String>) list;
 					listN.add(text);
 				} else {
 					throw new ScanFileHolderException("Type unknown");
 				}
 			} else {
 				Number parseValue = Utils.parseValue(text);
 				if (parseValue != null) {
 					columns[i] = new ArrayList<Number>();
 					((List<Number>) columns[i]).add(parseValue);
 				} else {
 					columns[i] = new ArrayList<String>();
 					// we need to add a value in so that test of type for future lines detect a String type
 					((List<String>) columns[i]).add(text);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Create all the datasets (1D)
 	 * @param holder
 	 * @param names column headings
 	 * @param columns array of lists of data
 	 * @param storeStrings
 	 * @param useImageLoader
 	 */
 	protected static void convertToDatasets(DataHolder holder, String[] names, List<?>[] columns, boolean storeStrings, boolean useImageLoader, String file_directory) {
 		for (int i = 0; i < names.length; i++) {
 			if (columns[i] != null) {
 				final AbstractDataset ds = AbstractDataset.array(columns[i]);
 				ds.setName(names[i]);
 				if (ds.getDtype() == AbstractDataset.STRING) {
 					StringDataset sds = (StringDataset) ds;
 					if (storeStrings) {
 						holder.addDataset(names[i], ds);
 					}
 					if (useImageLoader) {
 						ImageStackLoaderEx loader;
 						try {
 							String[] oldpaths = sds.getData();
 							String[] paths = new String[oldpaths.length];
 							for (int j = 0; j < paths.length ; j++) {
 								if(!(new File(oldpaths[j])).exists())
 									paths[j] = (new File(file_directory, oldpaths[j])).getAbsolutePath();
 								else
 									paths[j] = oldpaths[j];
 							}
 							loader = new ImageStackLoaderEx(sds.getShape(), paths);
 							String name = names[i] + "_image";
 							LazyDataset lazyDataset = new LazyDataset(name, loader.dtype, loader.getShape(), loader);
 							holder.addDataset(name, lazyDataset);
 						} catch (Exception ex) {
 							logger.warn("Unable to treat " + sds.getAbs(0) + " as an image file", ex);
 						}
 					}
 				} else {
 					holder.addDataset(names[i], ds);
 				}
 			}
 		}
 	}
 
 	private final int MARK_LIMIT = 1024;
 	private final Pattern numberRegex = Pattern.compile("^\\d.+");
 
 	protected void readMetadata(BufferedReader in, IMonitor mon) throws ScanFileHolderException {
 		
 		textMetadata.clear();
 		
 		// handling metadata in the file header
 		try {
 			String line;
 
 			while (true) {
 				
 				if (mon!=null) {
 					mon.worked(1);
 					if (mon.isCancelled()) throw new ScanFileHolderException("Load cancelled!");
 				}
 				line = in.readLine();
 				if (line == null || line.contains("&END")) {
 					return;
 				}
 				if (line.contains("MetaDataAtStart")) { // stop at end of header
 					String[] bits = line.split("</?MetaDataAtStart>");
 					if (bits.length > 0) {
 						for (String s : bits) {
 							if (s.contains("=")) {
 								in.mark(MARK_LIMIT);
 								parseString(s);
 							} else {
 								extraHeaders.add(s);
 							}
 						}
 						continue;
 					}
 				}
 				if (line.length() == 0) {
 					continue;
 				}
 				
 				if (line.contains("=")) {
 					in.mark(MARK_LIMIT);
 					parseString(line);
 				} else {
 					if (numberRegex.matcher(line).matches()) {
 						in.reset(); // backtrack to last line with an equal sign
 						return;
 					}
 					extraHeaders.add(line);
 				}
 			}
 		} catch (IOException e) {
 			logger.error("Problem parsing header of SRS file {}", fileName);
 			throw new ScanFileHolderException("There was a problem parsing header information", e);
 		}
 	}
 
 	private void parseString(String line) {
 		String key = null;
 		String value = line.trim();
 		int i = value.indexOf('=');
 		int end = value.length();
 		while (i >= 0) {
 			key = value.substring(0, i);
 			if (i == (end - 1))
 				break;
 			value = value.substring(i + 1).trim();
 
 			i = value.indexOf(',');
 			if (i >= 0) {
 				addMetadata(key, value.substring(0, i));
 				value = value.substring(i + 1).trim();
 				key = null;
 			} else {
 				break;
 			}
 			i = value.indexOf('=');
 			end = value.length();
 		}
 		if (key != null) {
 			addMetadata(key, value);
 		}
 	}
 
 	private void addMetadata(String key, String value) {
 		String strippedValue = extractQuotedString(value, true);
 		if (strippedValue == null) {
 			strippedValue = extractQuotedString(value, false);
 			if (strippedValue == null)
 				strippedValue = value;
 		}
 		textMetadata.put(key, strippedValue);
 	}
 
 	private String extractQuotedString(String line, boolean single) {
 		char quote = single ? '\'' : '\"';
 		int start = findQuoteChar(line, quote, 0);
 		if (start < 0)
 			return null;
 
 		int end = findQuoteChar(line, quote, start+1);
 		if (end < 0) { 
 			logger.warn("String was not quoted correctly: {} from {}", line.substring(start), line);
 			return line.substring(start+1);
 		}
 
 		return line.substring(start+1, end);
 	}
 
 	private int findQuoteChar(String line, char quote, int start) {
 		int i = line.indexOf(quote, start);
 		if (i <= 0) {
 			return i;
 		}
 		if (line.charAt(i-1) == '\\')
 			return findQuoteChar(line, quote, i+1);
 		return i;
 	}
 
 	/**
 	 * Function which saves to the basic SRS format with no header, but the correct Header tags
 	 * 
 	 * @param dh
 	 *            The data holder containing the data to be saved out
 	 * @throws ScanFileHolderException
 	 */
 	@Override
 	public void saveFile(DataHolder dh) throws ScanFileHolderException {
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
 
 			out.write("&SRS\n");
 			writeMetadata(out, dh);
 			out.write("&END\n");
 
 			// now write out the data names
 			int imax = dh.namesSize();
 			for (int i = 0; i < imax; i++) {
 				out.write(dh.getName(i) + "\t");
 			}
 			out.write("\n");
 
 			// now write out all of the data
 			int rows = dh.getDataset(0).getSize();
 			AbstractDataset[] datasets = new AbstractDataset[imax];
 			for (int i = 0; i < imax; i++) {
 				datasets[i] = dh.getDataset(i);
 			}
 
 			for (int j = 0; j < rows; j++) {
 				for (int i = 0; i < imax; i++) {
 					try {
 						out.write(datasets[i].getElementDoubleAbs(j) + "\t");
 					} catch (ArrayIndexOutOfBoundsException e) {
 						out.write(0.0 + "\t"); // add in zeros if other datasets have less elements
 					}
 				}
 				out.write("\n");
 			}
 
 			out.close();
 
 		} catch (Exception e) {
 			logger.error("Problem saving SRS file {}", fileName);
 			throw new ScanFileHolderException("SRSLoader.saveFile exception saving to " + fileName, e);
 		}
 	}
 
 	/**
 	 * @param out
 	 * @throws IOException
 	 */
 	protected void writeMetadata(BufferedWriter out, @SuppressWarnings("unused") DataHolder holder) throws IOException {
 		String[] metadataKeys = getKeysToSave();
 		if (!textMetadata.isEmpty() && metadataKeys != null) {
 			for (String k : metadataKeys) {
 				Object value = textMetadata.get(k);
 				if (value == null) {
 					if (textMetadata.containsKey(k)) {
 						logger.warn("Metadata item (key {}) was null", k);
 					} else {
 						logger.warn("Metadata key {} is not contained in list", k);
 					}
 				} else {
 					out.write(k + "=" + value.toString() + "\n");
 				}
 			}
 		}
 		if (!extraHeaders.isEmpty()) {
 			for (String each : extraHeaders) {
 				out.write(each + "\n");
 			}
 		}
 	}
 
 	/**
 	 * @return array of keys of metadata items to save 
 	 */
 	protected String[] getKeysToSave() {
 		return null;
 	}
 	
 	@Override
 	public IMetaData getMetaData() {
 		return new ExtendedMetadataAdapter() {
 			@Override
 			public Collection<String> getDataNames() {
 				return datasetNames;
 			}
 			@Override
 			public String getMetaValue(String key) {
 				return textMetadata.get(key);
 			}			
 			@Override
 			public Collection<String> getMetaNames() throws Exception{
 				return textMetadata.keySet();
 			}
 			
 			@Override
 			public String getScanCommand() {
 				//if a new scan command there is, a new if condition there will be...
 				String scanCmd = textMetadata.get("cmd");
 				if(scanCmd == null)
 					scanCmd = textMetadata.get("command");
 				if(scanCmd == null)
 					scanCmd = textMetadata.get("scancommand");
 				return scanCmd;
 			}
 
 		};
 	}
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 		BufferedReader in = null;
 		
 		try {
 			in = new BufferedReader(new FileReader(fileName));
 			// an updated header reader grabs all the metadata
 			readMetadata(in, mon);
 			
 			final String headStr = in.readLine();
 			//String[] vals = headStr.split("\t");
			String[] vals = headStr.split("\\s{2,}|\\,\\s+|\\t");
 			datasetNames.clear();
 			datasetNames.addAll(Arrays.asList(vals));
 
 		} catch (Exception e) {
 			throw new ScanFileHolderException("SRSLoader.loadFile exception loading  " + fileName, e);
 		} finally {
 			try {
 				if (in!=null) in.close();
 			} catch (IOException e) {
 				throw new ScanFileHolderException("Cannot read file", e);
 			}
 			
 		}
 		
 	}
 
 }
