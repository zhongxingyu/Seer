 /*--------------------------------------------------------------------------
  *  Copyright 2010 utgenome.org
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
 // QSeqToFASTQ.java
 // Since: Jul 20, 2010
 //
 //--------------------------------------
 package org.utgenome.format.illumina;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Writer;
 
 import org.utgenome.UTGBErrorCode;
 import org.utgenome.UTGBException;
 import org.utgenome.format.fastq.FastqRead;
 import org.xerial.util.log.Logger;
 
 /**
  * Converting Illumina's qseq format into FASTQ
  * 
  * @author leo
  * 
  */
 public class QSeqToFASTQ {
 
 	private static Logger _logger = Logger.getLogger(QSeqToFASTQ.class);
 
 	private String readGroup = null;
 	private int readCount = 0;
 
 	public QSeqToFASTQ() {
 	}
 
 	public QSeqToFASTQ(String readGroup) {
 		this.readGroup = readGroup;
 	}
 
 	public FastqRead convertToFastq(String line) throws UTGBException {
 		if (line == null)
 			return null;
 
 		final String[] c = line.split("\t");
 		if (c.length < 11) {
 			throw new UTGBException(UTGBErrorCode.PARSE_ERROR, "insufficient number of columns: " + line);
 		}
 
 		final String qfilter = (c[10] == "1") ? "Y" : "N";
 
 		readCount++;
 
 		// name, lane, x, y, pair?
 		String readName;
 		if (readGroup == null)
 			readName = String.format("%s:%s:%s:%s:%s", c[2], c[3], c[4], c[5], qfilter);
 		else
 			readName = String.format("%s.%d", readGroup, readCount);
 
 		String seq = c[8];
 		String qual = c[9];
 		StringBuilder phreadQualityString = new StringBuilder();
 		for (int i = 0; i < qual.length(); ++i) {
 			int phreadQual = qual.charAt(i) - 64;
 			char phreadQualChar = (char) (phreadQual + 33);
 			phreadQualityString.append(phreadQualChar);
 		}
 
 		return new FastqRead(readName, seq, sanitizeQualityValue(phreadQualityString.toString()));
 	}
 
 	public static String sanitizeReadName(String name) {
 		return name.replaceAll("\\s+", "_");
 	}
 
 	public static String sanitizeQualityValue(String qual) {
 		return qual.replaceAll("[^!-~\n]+", "$");
 	}
 
 	public void convert(BufferedReader illuminaSequenceFile, Writer output) throws IOException {
 
 		int lineCount = 1;
 		for (String line; (line = illuminaSequenceFile.readLine()) != null; lineCount++) {
 			try {
 				FastqRead r = convertToFastq(line);
 				output.write(r.toFASTQString());
 			}
			catch (Exception e) {
 				_logger.warn(String.format("line %d: %s", lineCount, e));
 			}
 		}
 
 	}
 }
