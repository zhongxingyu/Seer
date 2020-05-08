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
 // InfoSilkGenerator.java
 // Since: May 25, 2010
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.bio;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Generating read information in Silk format, which is used for displaying mouse over message
  * 
  * @author leo
  * 
  */
 public class InfoSilkGenerator implements OnGenomeDataVisitor {
 
 	public ArrayList<String> lines = new ArrayList<String>();
 
 	public String getSilk() {
 		StringBuilder buf = new StringBuilder();
 		for (String each : lines) {
 			buf.append(each);
 			buf.append("\n");
 		}
 		return buf.toString();
 	}
 
 	public List<String> getLines() {
 		return lines;
 	}
 
 	public void addLine(String text) {
 		lines.add(text);
 	}
 
 	public void visitBSSRead(BSSRead b) {
 		visitInterval(b);
 
 	}
 
 	public void visitGene(Gene g) {
 		visitRead(g);
 	}
 
 	public void visitInterval(Interval interval) {
 		addLine("-name: " + interval.getName());
 		addLine("-start: " + interval.getStart());
 		addLine("-end: " + interval.getEnd());
 		addLine("-length: " + interval.length());
 	}
 
 	public void visitRead(Read r) {
 		visitInterval(r);
 		addLine("-strand: " + r.getStrand());
 	}
 
 	public void visitSAMRead(SAMRead r) {
 		visitInterval(r);
 		addLine("-flag: " + Integer.toBinaryString(r.flag));
 		addLine("-strand: " + (r.isSense() ? "+" : "-"));
 		addLine("-cigar: " + r.cigar);
 		if (r.iSize != 0)
 			addLine("-insert size: " + r.iSize);
 		addLine("-mapq: " + r.mapq);
 		if (r.isPairedRead()) {
 			addLine("-mate ref: " + r.mrnm);
 			addLine("-mate start: " + r.mStart);
 		}
 
 		addLine("-tag");
 		for (String key : r.tag.keySet()) {
 			addLine("  -" + key + ": " + r.tag.get(key));
 		}
 	}
 
 	public void visitSAMReadPair(SAMReadPair pair) {
 
 		visitSAMRead(pair.getFirst());
 		visitSAMRead(pair.getSecond());
 	}
 
 	public void visitSequence(ReferenceSequence referenceSequence) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void visitReadCoverage(ReadCoverage readCoverage) {
 		addLine("-name: " + readCoverage.getName());
 		addLine("-start: " + readCoverage.getStart());
 		addLine("-length: " + readCoverage.length());
 	}
 
 	public void visitGraph(GraphData graph) {
 		addLine("graph data");
 	}
 
 }
