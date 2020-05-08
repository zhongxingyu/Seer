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
 import org.utgenome.gwt.utgb.client.bio.SAMRead;
 import org.utgenome.gwt.utgb.client.bio.SAMReadPair;
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
 
 	/**
 	 * Retrieved SAMReads (or SAMReadPair) overlapped with the specified interval
 	 * 
 	 * @param bamFile
 	 * @param loc
 	 * @return
 	 */
 	public static List<OnGenome> overlapQuery(File bamFile, ChrLoc loc) {
 
 		File baiFile = getBamIndexFile(bamFile);
 		SAMFileReader sam = new SAMFileReader(bamFile, baiFile, true);
 		sam.setValidationStringency(ValidationStringency.SILENT);
 
 		List<OnGenome> result = new ArrayList<OnGenome>();
 
 		HashMap<String, SAMRead> samReadTable = new HashMap<String, SAMRead>();
 
 		if (_logger.isDebugEnabled())
 			_logger.debug(String.format("query BAM (%s) %s", bamFile, loc));
 
 		int readCount = 0;
 		CloseableIterator<SAMRecord> it = sam.queryOverlapping(loc.chr, loc.start, loc.end);
 		try {
 
 			for (; it.hasNext();) {
 				SAMRead query = SAM2SilkReader.convertToSAMRead(it.next());
 
 				readCount++;
 				if (_logger.isDebugEnabled() && (readCount % 10000) == 0) {
 					_logger.debug(String.format("reading (%s) %s : %d reads", bamFile.getName(), loc, readCount));
 				}
 
 				if (query.isUnmapped()) {
 					// ignore unmapped sequence
 					continue;
 				}
 
				// Is properly mapped mate-pair?
				if (!query.isMappedInProperPair()) {
 					result.add(query);
 					continue;
 				}
 
 				// The paired read names must be the same.
 				if (!samReadTable.containsKey(query.getName())) {
 					// new entry
 					samReadTable.put(query.getName(), query);
 					continue;
 				}
 
 				// Found a paired-end read set.
 				SAMRead mate = samReadTable.get(query.getName());
 				boolean foundPair = false;
 				if (query.isFirstRead()) {
					if (mate.isSecondRead()) {
 						result.add(new SAMReadPair(query, mate));
 						foundPair = true;
 					}
 				}
 				else {
					if (mate.isFirstRead()) {
 						result.add(new SAMReadPair(mate, query));
 						foundPair = true;
 					}
 				}
 
 				if (!foundPair) {
 					// The read names are the same, but they are not mated (error?)
 					result.add(mate);
 					result.add(query);
 				}
 
 				samReadTable.remove(mate.getName());
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
 
 		for (SAMRead each : samReadTable.values()) {
 			result.add(each);
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
