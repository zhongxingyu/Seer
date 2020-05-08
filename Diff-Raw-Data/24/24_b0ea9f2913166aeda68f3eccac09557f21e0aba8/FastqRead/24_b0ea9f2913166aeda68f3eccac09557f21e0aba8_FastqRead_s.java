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
 // FastqEntry.java
 // Since: Jun 14, 2010
 //
 //--------------------------------------
 package org.utgenome.format.fastq;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 
 import org.utgenome.UTGBErrorCode;
 import org.utgenome.UTGBException;
 import org.xerial.silk.SilkWriter;
 import org.xerial.util.StringUtil;
 
 /**
  * An entry of FASTQ format
  * 
  * @author leo
  * 
  */
 public class FastqRead {
 	public String seqname;
 	public String seq;
 	public String qual;
 
 	public FastqRead() {
 	}
 
 	public FastqRead(String seqname, String seq, String qual) {
 		this.seqname = seqname;
 		this.seq = seq;
 		this.qual = qual;
 	}
 
 	public String toFASTQString() {
 		StringBuilder buf = new StringBuilder();
 		buf.append("@");
 		buf.append(seqname);
 		buf.append(StringUtil.NEW_LINE);
 		buf.append(seq);
 		buf.append(StringUtil.NEW_LINE);
 		buf.append("+");
 		buf.append(StringUtil.NEW_LINE);
 		buf.append(qual);
 		buf.append(StringUtil.NEW_LINE);
 		return buf.toString();
 	}
 
 	public static FastqRead parse(BufferedReader reader) throws UTGBException {
 
 		try {
 			String seqNameLine = reader.readLine();
 			String sequence = reader.readLine();
 			reader.readLine();
 			String qual = reader.readLine();
 
 			if (seqNameLine == null || sequence == null || qual == null) {
 				return null; // no more entry
 			}
 
 			if (seqNameLine.length() < 2) {
 				throw new UTGBException(UTGBErrorCode.PARSE_ERROR, "invalid sequence name: " + seqNameLine);
 			}
 
 			return new FastqRead(seqNameLine.substring(1), sequence, qual);
 		}
 		catch (IOException e) {
 			throw new UTGBException(UTGBErrorCode.PARSE_ERROR, "invalid fastq block");
 		}
 	}
 
 	public void toSilk(SilkWriter silk) {
 		SilkWriter sub = silk.node("fastq");
 		sub.leaf("name", seqname);
 		sub.leaf("seq", seq);
 		sub.leaf("qual", qual);
 	}
 
 }
