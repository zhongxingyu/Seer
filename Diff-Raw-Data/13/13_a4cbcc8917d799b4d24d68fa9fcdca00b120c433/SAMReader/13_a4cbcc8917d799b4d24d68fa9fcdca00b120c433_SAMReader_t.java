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
 // SAMReader.java
 // Since: Dec 3, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.format.sam;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMFileReader.ValidationStringency;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMSequenceDictionary;
 import net.sf.samtools.SAMSequenceRecord;
 import net.sf.samtools.util.CloseableIterator;
 
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.client.bio.SAMReadLight;
 import org.utgenome.gwt.utgb.client.bio.SAMReadPair;
 import org.utgenome.gwt.utgb.client.bio.SAMReadPairFragment;
 import org.xerial.util.log.Logger;
 
 /**
  * SAM File reader
  * 
  * @author leo
  * 
  */
 public class SAMReader {
 
 	private static Logger _logger = Logger.getLogger(SAMReader.class);
 
 	public static Iterable<SAMRecord> getSAMRecordReader(InputStream samFile) {
 		return new SAMFileReader(samFile);
 	}
 
 	public List<String> getChrList(File samOrBamFile) {
 
 		SAMFileReader sam = new SAMFileReader(samOrBamFile);
 		try {
 			sam.setValidationStringency(ValidationStringency.SILENT);
 
 			SAMSequenceDictionary dict = sam.getFileHeader().getSequenceDictionary();
 			List<String> chrList = new ArrayList<String>();
 			for (SAMSequenceRecord eachSeq : dict.getSequences()) {
 				chrList.add(eachSeq.getSequenceName());
 			}
 			return chrList;
 		}
 		finally {
 			sam.close();
 		}
 	}
 
 	public static File getBamIndexFile(File bamFile) {
 		File baiFile = new File(bamFile.getAbsolutePath() + ".bai");
 		return baiFile;
 	}
 
 	public static interface SAMReadFactory {
 		public SAMReadLight newSAMRead(SAMRecord r);
 	}
 
 	public static class CompleteSAMReadFactory implements SAMReadFactory {
 		public SAMReadLight newSAMRead(SAMRecord r) {
 			return SAM2SilkReader.convertToSAMRead(r);
 		}
 	}
 
 	public static class LightWeightSAMReadFactory implements SAMReadFactory {
 		public SAMReadLight newSAMRead(SAMRecord r) {
 			return SAM2SilkReader.convertToSAMReadLight(r);
 		}
 	}
 
 	/**
 	 * Retrieved SAMReads (or SAMReadPair) overlapped with the specified interval
 	 * 
 	 * @param bamFile
 	 * @param loc
 	 * @return
 	 */
 	public static List<OnGenome> overlapQuery(File bamFile, ChrLoc loc) {
 
 		File baiFile = getBamIndexFile(bamFile);
 		SAMFileReader sam = new SAMFileReader(bamFile, baiFile, false);
 		sam.setValidationStringency(ValidationStringency.SILENT);
 
 		if (_logger.isDebugEnabled())
 			_logger.debug(String.format("query BAM (%s) %s", bamFile, loc));
 
 		int readCount = 0;
 		List<OnGenome> result = new ArrayList<OnGenome>();
 		{
 			List<SAMRecord> readSet = new ArrayList<SAMRecord>();
 
 			// Retrieve SAMRecords from the  BAM file
 			CloseableIterator<SAMRecord> it = sam.queryOverlapping(loc.chr, loc.start, loc.end);
 			try {
 
 				for (; it.hasNext();) {
 					SAMRecord read = it.next();
 
 					if (_logger.isDebugEnabled() && (readCount % 10000) == 0) {
 						_logger.debug(String.format("reading (%s) %s : %d reads", bamFile.getName(), loc, readCount));
 					}
 
 					// ignore unmapped reads
 					if (read.getReadUnmappedFlag())
 						continue;
 
					readCount++;
 					readSet.add(read);
 				}
 			}
 			finally {
 				if (it != null)
 					it.close();
 
 				sam.close();
 
 				if (_logger.isDebugEnabled()) {
 					_logger.debug(String.format("finished reading (%s) %s : %d reads", bamFile.getName(), loc, readCount));
 				}
 			}
 			SAMReadFactory rf = readCount < 500 ? new CompleteSAMReadFactory() : new LightWeightSAMReadFactory();
 
 			// Mating paired-end reads
 			{
 				HashMap<String, SAMRecord> samReadTable = new HashMap<String, SAMRecord>();
 				for (SAMRecord read : readSet) {
 
 					// Add single-end read as is
 					if (!read.getReadPairedFlag()) {
 						result.add(rf.newSAMRead(read));
 						continue;
 					}
 
 					// The paired read names must be the same.
 					if (!samReadTable.containsKey(read.getReadName())) {
 						// new entry
 						samReadTable.put(read.getReadName(), read);
 						continue;
 					}
 
 					// Found a paired-end read set.
 					SAMRecord mate = samReadTable.get(read.getReadName());
 					boolean foundPair = false;
 					if (read.getFirstOfPairFlag()) {
 						if (mate.getSecondOfPairFlag()) {
 							result.add(new SAMReadPair(rf.newSAMRead(read), rf.newSAMRead(mate)));
 							foundPair = true;
 						}
 					}
 					else {
 						if (mate.getFirstOfPairFlag()) {
 							result.add(new SAMReadPair(rf.newSAMRead(mate), rf.newSAMRead(read)));
 							foundPair = true;
 						}
 					}
 
 					if (!foundPair) {
 						// The read names are the same, but they are not mated (error?)
 						result.add(rf.newSAMRead(mate));
 						result.add(rf.newSAMRead(read));
 					}
 
 					samReadTable.remove(mate.getReadName());
 				}
 
 				// add the remaining reads to the results 
 				for (SAMRecord each : samReadTable.values()) {
 					if (each.getReadPairedFlag()) {
 						result.add(new SAMReadPairFragment(rf.newSAMRead(each), each.getMateAlignmentStart()));
 					}
 				}
 			}
 		}
 
 		if (_logger.isDebugEnabled()) {
 			_logger.debug(String.format("sorting (%s) %s : %d reads", bamFile.getName(), loc, readCount));
 		}
 
 		Collections.sort(result, new Comparator<OnGenome>() {
 			public int compare(OnGenome o1, OnGenome o2) {
 				return o1.getStart() - o2.getStart();
 			}
 		});
 
 		if (_logger.isDebugEnabled()) {
 			_logger.debug(String.format("sorting (%s) %s : %d reads. done.", bamFile.getName(), loc, readCount));
 		}
 
 		return result;
 	}
 
 }
