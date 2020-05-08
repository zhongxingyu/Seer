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
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * Should act like the SRSLoader, but for Xas format ascii files. This is because the associated editor subclasses the
  * SRSEditor.
  */
 public class XasAsciiLoader extends SRSLoader {
 
 	private static final String COMMENT_PREFIX = "#";
 
 	public XasAsciiLoader(String fileName) {
 		setFile(fileName);
 	}
 
 	@Override
 	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
 		// then try to read the file given
 		BufferedReader reader = null;
 
 		try {
 			reader = new BufferedReader(new FileReader(fileName));
 			String dataStr;
 			String previousHeaderLine = "";
 			boolean readingHeader = true;
 			boolean readingFooter = false;
 			List<?>[] columnData = null;
 			while ((dataStr = reader.readLine()) != null) {
 
 				// ignore blank
 				if (dataStr.isEmpty()) {
 					continue;
 				}
 
 				// first block of commented out lines are the header
 				if (dataStr.startsWith(COMMENT_PREFIX) && readingHeader) {
 					readHeaderLine(dataStr);
 					previousHeaderLine = dataStr;
 					continue;
 				}
 				readingHeader = false;
 
 				// next block of commented out lines after any break in comments will be the footer
 				if (dataStr.startsWith(COMMENT_PREFIX) && !readingHeader) {
 					readingFooter = true;
 					readFooterLine(dataStr);
 					continue;
 				}
 
 				// if get here then its data
 				if (readingFooter) {
 					// should not get here after reading a block of commented out lines for a second time: invalid
 					// format
 					throw new ScanFileHolderException("Cannot read file");
 				}
 
 				// the first time we get here, the previous line should have been the header
 				if (columnData == null && datasetNames.size() == 0) {
 					columnData = parseHeaderString(previousHeaderLine);
 				}
 
				parseColumns(splitLine(dataStr.trim()), columnData);
 			}
 
 			String[] names = datasetNames.toArray(new String[]{});
 			DataHolder result = new DataHolder();
 			try {
 				convertToDatasets(result, names, columnData, isStoreStringValues(), isUseImageLoaderForStrings(), (new File(this.fileName)).getParent());
 			} catch (Exception e) {
 				logger.warn(e.getMessage());
 			}
 
 			return result;
 		} catch (Exception e) {
 			throw new ScanFileHolderException("XasAsciiLoader.loadFile exception loading  " + fileName, e);
 		} finally {
 			try {
 				if (reader != null)
 					reader.close();
 			} catch (IOException e) {
 				throw new ScanFileHolderException("Cannot read file", e);
 			}
 		}
 	}
 
 	private List<?>[] parseHeaderString(String previousHeaderLine) {
 		// remove leading hash
 		previousHeaderLine = previousHeaderLine.substring(1).trim();
 		String parts[] = splitLine(previousHeaderLine);
 		datasetNames.clear();
 		datasetNames.addAll(Arrays.asList(parts));
 		return new List<?>[parts.length];
 	}
 
 	private String[] splitLine(String line) {
 		return line.split("[\t ]+");
 	}
 
 	private void readFooterLine(String dataStr) {
 		// do not differentiate for the moment
 		readHeaderLine(dataStr);
 	}
 
 	private void readHeaderLine(String dataStr) {
 		// remove leading hash
 		dataStr = dataStr.trim().substring(1);
 		int colonLoc = dataStr.indexOf(":");
 		if (colonLoc == -1) {
 			// must be a comment without any metadata
 			return;
 		}
 		// split by colon
 		String key = dataStr.substring(0, colonLoc);
 		String value = dataStr.substring(colonLoc+1);
 		textMetadata.put(key.trim(), value.trim());
 	}
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 	}
 
 	@Override
 	protected void writeHeader(BufferedWriter out, DataHolder dh) throws IOException {
 		// now write out the data names
 		out.write(COMMENT_PREFIX);
 		int imax = dh.size();
 		for (int i = 0; i < imax; i++) {
 			out.write(dh.getName(i) + "\t");
 		}
 		out.write("\n");
 	}
 }
