 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // BED2Silk.java
 // Since: 2009/05/07
 //
 // $URL: http://svn.utgenome.org/utgb/trunk/utgb/utgb-shell/src/main/java/org/utgenome/shell/db/bed/BED2Silk.java $ 
 // $Author: leo $
 //--------------------------------------
 package org.utgenome.format.bed;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.zip.DataFormatException;
 
 import org.antlr.runtime.ANTLRReaderStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.Tree;
 import org.utgenome.UTGBErrorCode;
 import org.utgenome.UTGBException;
 import org.utgenome.gwt.utgb.client.util.StringUtil;
 import org.xerial.core.XerialException;
 import org.xerial.silk.SilkWriter;
 import org.xerial.util.bean.impl.BeanUtilImpl;
 import org.xerial.util.log.Logger;
 
 /**
  * Converting BED into Silk format.
  * 
  * 
  * <p>
  * Note that BED is a 0-based gene data format, while UTGB uses 1-based [start, end) interval representation. To fill
  * the gap between BED and UTGB, BED2Silk translates BED's 0-based entries into 1-based ones.
  * </p>
  * 
  * 
  * @author yoshimura
  * 
  */
 public class BED2Silk {
 
 	private static Logger _logger = Logger.getLogger(BED2Silk.class);
 	private final BufferedReader reader;
 
 	public static class BEDHeaderDescription {
 		String name;
 		ArrayList<BEDHeaderAttribute> attributes = new ArrayList<BEDHeaderAttribute>();
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public void addAttribute(BEDHeaderAttribute attribute) {
 			attributes.add(attribute);
 		}
 
 		@Override
 		public String toString() {
 			return String.format("name=%s, attributes=%s", name, attributes.toString());
 		}
 	}
 
 	public static class BEDHeaderAttribute {
 		String name;
 		String value;
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public void setValue(String value) {
 			this.value = value;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("{name=%s, value=%s}", name, value);
 		}
 	}
 
 	public BED2Silk(File bedFile) throws IOException {
 		this(new FileReader(bedFile));
 	}
 
 	/**
 	 * 
 	 * @param bedFile
 	 * @throws IOException
 	 */
 	public BED2Silk(Reader bedFile) throws IOException {
 		this.reader = new BufferedReader(bedFile);
 
 	}
 
 	public void close() throws IOException {
 		if (reader != null)
 			reader.close();
 	}
 
 	/**
 	 * Convert a BED's gene line into a Silk's tab-delimited format
 	 * 
 	 * @param line
 	 * @param lineNum
 	 * @return
 	 * @throws DataFormatException
 	 * @throws UTGBException
 	 */
 	private String createGeneTSV(String line, int lineNum) throws UTGBException {
 
 		try {
 			String[] gene = readBEDLine(line);
 			StringBuilder sb = new StringBuilder();
 			if (gene.length < 3) {
 				throw new UTGBException(UTGBErrorCode.INVALID_BED_LINE, String.format("line %d doesn't have 3 columns: %s", lineNum, line));
 			}
 
 			int start = Integer.parseInt(gene[1]) + 1;
 			int end = Integer.parseInt(gene[2]) + 1;
 
 			// print "coordinate.name, start, end"
 			sb.append(String.format("%s\t%d\t%d\t", gene[0], start, end));
 			// print "name"
 			if (gene.length >= 4) {
 				sb.append(gene[3]);
 			}
 			// print "strand"
 			sb.append("\t");
 			if (gene.length >= 6) {
 				if (gene[5].equals("+") || gene[5].equals("-")) {
 					sb.append(gene[5]);
 				}
 				else {
 					_logger.warn(String.format("Illegal strand value '%s'. Using '+' instead. ", gene[5]));
 					sb.append("+");
 				}
 			}
 			// print "cds"
 			sb.append("\t");
 			if (gene.length >= 8) {
 				int cdsStart = Integer.parseInt(gene[6]) + 1;
 				int cdsEnd = Integer.parseInt(gene[7]) + 1;
 				sb.append(String.format("[%d, %d]", cdsStart, cdsEnd));
 			}
 			// print "exon"
 			sb.append("\t");
 			if (gene.length >= 12) {
 				String[] blockSizes = gene[10].split(",");
 				String[] blockStarts = gene[11].split(",");
 
 				sb.append("[");
 				Integer nExons = Integer.parseInt(gene[9]);
 				for (int i = 0; i < nExons; i++) {
 					int startExon = start + Integer.parseInt(blockStarts[i]);
 					int endExon = startExon + Integer.parseInt(blockSizes[i]);
 					sb.append("[" + startExon + ", " + endExon + "]");
 					if (i < nExons - 1) {
 						sb.append(", ");
 					}
 				}
 				sb.append("]");
 			}
 
 			// print "color"
 			sb.append("\t");
 			if (gene.length >= 9) {
 				sb.append(changeRGB2Hex(gene[8]));
 			}
 			// print "score"
 			sb.append("\t");
 			if (gene.length >= 5) {
 				sb.append("{\"score\":" + gene[4] + "}");
 			}
 
 			return sb.toString();
 		}
 		catch (NumberFormatException e) {
 			throw new UTGBException(UTGBErrorCode.INVALID_BED_LINE, String.format("line %d: %s", lineNum, e));
 		}
 		catch (DataFormatException e) {
 			throw new UTGBException(UTGBErrorCode.INVALID_BED_LINE, String.format("line %d: %s", lineNum, e));
 		}
 		catch (IllegalArgumentException e) {
 			throw new UTGBException(UTGBErrorCode.INVALID_BED_LINE, String.format("line %d: %s", lineNum, e));
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param out
 	 * @throws IOException
 	 * @throws UTGBShellException
 	 */
 	public void toSilk(PrintWriter pout) throws IOException, UTGBException {
 
 		SilkWriter out = new SilkWriter(pout);
 
 		// print header line
 		out.preamble();
 
 		int geneCount = 0;
 
 		int lineNum = 1;
 		for (String line; (line = reader.readLine()) != null; lineNum++) {
 			try {
 				if (line.startsWith("#") || line.length() == 0) {
 				}
 				else if (line.startsWith("browser")) {
 					// this.browser = readTrackLine(line,i);
 				}
 				else if (line.startsWith("track")) {
 					// print track line
 					BEDHeaderDescription track = readTrackLine(line);
 					SilkWriter trackNode = out.node("track");
 					for (BEDHeaderAttribute a : track.attributes) {
 						trackNode.leaf(a.name, StringUtil.unquote(a.value));
 					}
 				}
 				else {
 					String dataLine = createGeneTSV(line, lineNum);
 					// output data line
 					if (geneCount == 0) {
 						// print gene header line
 						SilkWriter geneNode = out.tabDataSchema("gene");
 						geneNode.attribute("coordinate");
 						geneNode.attribute("start");
 						geneNode.attribute("end");
 						geneNode.attribute("name");
 						geneNode.attribute("strand");
 						geneNode.attribute("cds(start, end)");
 						geneNode.attribute("exon(start, end)*");
 						geneNode.attribute("color");
 						geneNode.attribute("_[json]");
 					}
 					out.dataLine(dataLine);
 					geneCount++;
 				}
 			}
 			catch (RecognitionException e) {
 				_logger.error(String.format("line %d has invalid format: %s", lineNum, e));
 			}
 			catch (XerialException e) {
 				throw new UTGBException(String.format("line %d: %s", lineNum, e));
 			}
 			catch (UTGBException e) {
 				switch (e.getErrorCode()) {
 				case INVALID_BED_LINE:
 					_logger.warn(e);
 					continue;
 				default:
 					throw e;
 				}
 			}
 		}
 
 		out.endDocument();
 
 	}
 
 	public String toSilk() throws IOException, UTGBException {
 		StringWriter out = new StringWriter();
 		toSilk(new PrintWriter(out));
 		return out.toString();
 	}
 
 	private static String[] readBEDLine(String line) throws DataFormatException {
		String[] temp = line.trim().split("[ \t]+");
 		// split by tab or space
 		if (temp.length < 3) {
 			throw new DataFormatException("Number of line parameters < 3");
 		}
 		return temp;
 	}
 
 	private static BEDHeaderDescription readTrackLine(String line) throws IOException, XerialException, RecognitionException {
 		BEDLexer lexer = new BEDLexer(new ANTLRReaderStream(new StringReader(line)));
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 
 		BEDParser parser = new BEDParser(tokens);
 		BEDParser.description_return ret = parser.description();
 
 		return BeanUtilImpl.createBeanFromParseTree(BEDHeaderDescription.class, (Tree) ret.getTree(), BEDParser.tokenNames);
 	}
 
 	private static String changeRGB2Hex(String rgb) throws NumberFormatException {
 		String[] temp = rgb.split(",");
 		StringBuffer ret = new StringBuffer("\"#");
 		if (temp.length >= 3) {
 			for (int i = 0; i < 3; i++) {
 				Integer tempInt = Integer.parseInt(temp[i]);
 				if (tempInt > 255 || tempInt < 0) {
 					System.err.println("Warn : out of color range 0-255");
 					return "";
 				}
 				if (Integer.toHexString(tempInt).length() == 1) {
 					ret.append("0");
 				}
 				ret.append(Integer.toHexString(tempInt));
 			}
 			return ret.append("\"").toString();
 		}
 		else {
 			return "";
 		}
 	}
 
 }
