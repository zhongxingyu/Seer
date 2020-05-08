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
 // Sam2WigConverter.java
 // Since: 2010/09/28
 //
 //--------------------------------------
 package org.utgenome.format.sam;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMFileReader.ValidationStringency;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMRecordIterator;
 
 import org.utgenome.gwt.utgb.client.bio.Interval;
 
 /**
  * Converting SAM into WIG (coverage depth)
  * 
  * @author leo
  * 
  */
 public class Sam2WigConverter {
 
 	private List<Interval> readsInBlock = new ArrayList<Interval>();
 	private String currentChr = null;
 	private int sweepLine = 1;
 	private Writer out;
 	private int blockSize = 10000;
 	private Interval block = new Interval(1, blockSize);
 
 	public void convert(File samOrBam, Writer out) throws IOException {
 		this.out = out;
 		SAMFileReader.setDefaultValidationStringency(ValidationStringency.SILENT);
 		SAMFileReader samReader = new SAMFileReader(samOrBam);
 
 		// assume that SAM reads are sorted in the start order
 		for (SAMRecordIterator it = samReader.iterator(); it.hasNext();) {
 			SAMRecord read = it.next();
 
 			String ref = read.getReferenceName();
 
 			if (currentChr != null && !currentChr.equals(ref)) {
 				// flush the block
 				outputReadDepth();
 				currentChr = ref;
 
 				readsInBlock.clear();
 				block = new Interval(1, blockSize);
 			}
 			else
 				currentChr = ref;
 
 			Interval readInterval = new Interval(read.getAlignmentStart(), read.getAlignmentEnd() + 1);
 			if (block.contains(readInterval)) {
 				readsInBlock.add(readInterval);
 			}
 			else { // obtained a read out of the current block
 					// output 
 				outputReadDepth();
 
 				// move to the next block
 				block = new Interval(block.getEnd() + 1, block.getEnd() + blockSize);
 
 				// preserve the reads contained in the next block
 				List<Interval> remainingReads = new ArrayList<Interval>();
 				for (Interval each : readsInBlock) {
 					if (block.contains(each)) {
 						remainingReads.add(each);
 					}
 				}
 
 				readsInBlock = remainingReads;
 			}
 		}
 
 		if (!readsInBlock.isEmpty()) {
 			outputReadDepth();
 		}
 
 		out.flush();
 
 		samReader.close();
 	}
 
 	public void outputReadDepth() throws IOException {
 		out.write(String.format("fixedStep chrom=%s start=%d step=1 span=1\n", currentChr, block.getStart()));
 		int[] coverage = getReadDepth(block, readsInBlock);
 		for (int i = 0; i < coverage.length; ++i) {
 			out.write(Integer.toString(coverage[i]));
 			out.write("\n");
 		}
 	}
 
 	public static int[] getReadDepth(Interval block, List<Interval> readsInBlock) {
 
 		int offset = block.getStart();
 
 		// get the max read range
 		int endMax = block.getStart();
 		for (Interval each : readsInBlock) {
 			if (endMax < each.getEnd())
 				endMax = each.getEnd();
 		}
		if (endMax > block.getEnd())
			endMax = block.getEnd();
 
 		final int width = endMax - block.getStart();
 		int[] coverage = new int[width];
 		// initialize the array
 		for (int i = 0; i < coverage.length; ++i)
 			coverage[i] = 0;
 
 		// accumulate coverage depth
 		for (Interval each : readsInBlock) {
 			int posStart = each.getStart() - offset;
 			int posEnd = each.getEnd() - offset;
 
 			if (posStart < 0)
 				posStart = 0;
 			if (posEnd >= width)
 				posEnd = width - 1;
 
 			for (int i = posStart; i < posEnd; i++) {
 				coverage[i]++;
 			}
 		}
 
 		return coverage;
 	}
 
 }
