 package org.genedb.crawl.business;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import net.sf.samtools.AlignmentBlock;
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMRecordIterator;
 import net.sf.samtools.SAMSequenceRecord;
 
 import org.apache.log4j.Logger;
 import org.genedb.crawl.model.BaseResult;
 import org.genedb.crawl.model.FileInfo;
 import org.genedb.crawl.model.FileInfoList;
 import org.genedb.crawl.model.MappedCoverage;
 import org.genedb.crawl.model.MappedQuery;
 import org.genedb.crawl.model.MappedSAMHeader;
 import org.genedb.crawl.model.MappedSAMSequence;
 
 
 public class Sam {
 	
 	private Logger logger = Logger.getLogger(Sam.class);
 	
 	public AlignmentStore alignmentStore;
 	
 	private final String[] defaultProperties = {"alignmentStart", "alignmentEnd", "flags", "readName"};
 	private final Method[] methods = SAMRecord.class.getDeclaredMethods();
 	
 	private SAMFileReader getSamOrBam(int fileID) throws Exception {
 		final SAMFileReader inputSam = alignmentStore.getReader(fileID); 
 		if (inputSam == null) {
 			throw new Exception ("Could not find the file " + fileID);
 		}
 		return inputSam;
 	}
 	
 	public MappedSAMHeader header(int fileID) throws Exception {
 		return this.header(getSamOrBam(fileID));
 	}
 	
 	public MappedSAMHeader header(SAMFileReader file) throws Exception {
 		MappedSAMHeader model = new MappedSAMHeader();
 		
 		for (Map.Entry<String, Object> entry : file.getFileHeader().getAttributes()) {
 			model.attributes.put(entry.getKey(), entry.getValue().toString());
 		}
 		
 		return model;
 	}
 	
 	public BaseResult sequence(int fileID) throws Exception {
 		return this.sequence(getSamOrBam(fileID));
 	}
 	
 	public BaseResult sequence(SAMFileReader file) throws Exception {
 		BaseResult model = new BaseResult();
 		for (SAMSequenceRecord ssr : file.getFileHeader().getSequenceDictionary().getSequences()) {
 			MappedSAMSequence mss = new MappedSAMSequence();
 			mss.length = ssr.getSequenceLength();
 			mss.name = ssr.getSequenceName();
 			mss.index = ssr.getSequenceIndex();
 			model.addResult(mss);
 		}
 		return model;
 	}
 	
 	private FileInfoList list(List<Alignment> alignments) {
 		
 		FileInfoList files = new FileInfoList();
 		
 		for (Alignment alignment : alignments) {
			FileInfo file = new FileInfo(alignment.fileID, alignment.file.getAbsolutePath(), alignment.meta);
 			logger.info(alignment.file.getName());
 			logger.info(alignment.meta);
 			files.files.add(file);
 			
 		}
 		return files;
 	}
 	
 	public FileInfoList list() {
 		return list(alignmentStore.getAlignments());
 	}
 	
 	public FileInfoList listfororganism(String organism) {
 		
 		List<Alignment> alignments = new ArrayList<Alignment>();
 		
 		for (Alignment alignment : alignmentStore.getAlignments()) {
 			if (alignment.organism.equals(organism) || alignment.organism.equals("com:" + organism)) {
 				alignments.add(alignment);
 			}
 		}
 		
 		return list(alignments);
 	}
 	
 	public synchronized MappedQuery query(int fileID, String sequence, int start,  int end, boolean contained, int filter) throws Exception {
 		return query(fileID, sequence, start, end, contained, defaultProperties, filter);		
 	}
 	
 	
 	public synchronized MappedQuery query(int fileID, String sequence, int start,  int end, boolean contained, String[] properties, int filter ) throws Exception {
 		logger.debug(String.format("fileID: %d\tlocation: '%s:%d-%d'\tcontained?%s\tfilter: %d(%s)", fileID, sequence, start, end, contained, filter, padLeft(Integer.toBinaryString(filter), 8)));
 		return this.query(getSamOrBam(fileID), sequence, start, end, contained, properties, filter);
 	}
 	
 	public synchronized MappedQuery query(SAMFileReader file, String sequence, int start,  int end, boolean contained, String[] properties, int filter ) throws Exception {
 		
 		long startTime = System.currentTimeMillis();
 		
 		MappedQuery model = new MappedQuery();
 		
 		Set<String> propertySet = new HashSet<String>(Arrays.asList(properties));
 		Map<Method,String> methods2properties = new HashMap<Method,String>();
 		
 		
 		for (Method method : methods) {
 			String methodName = method.getName();
 			if (methodName.startsWith("get")) {
 				String propertyName = methodName.substring(3);
 				propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
 				// logger.info(methodName + " " + propertyName);
 				
 				if (propertySet.contains(propertyName)) {
 					//logger.info("added!");
 					model.records.put(propertyName, new ArrayList<Object>());
 					methods2properties.put(method, propertyName);
 				}
 				
 			}
 		}
 		
 		model.count = 0;
 		
 		SAMRecordIterator i = null;
 		try {
 			
 			/**
 			 * 
 			 * According to the BAMFileReader2 doc:
 			 * 
 		     * "Prepare to iterate through the SAMRecords in file order.
 		     * Only a single iterator on a BAM file can be extant at a time.  If getIterator() or a query method has been called once,
 		     * that iterator must be closed before getIterator() can be called again.
 		     * A somewhat peculiar aspect of this method is that if the file is not seekable, a second call to
 		     * getIterator() begins its iteration where the last one left off.  That is the best that can be
 		     * done in that situation."
 		     * 
 		     * For this reason, we must make sure that we close the iterator at the end of the loop AND make sure that the methods
 		     * that use this iterator are iterates is synchronized. 
 		     * 
 		     */
 			
 			i = file.query(sequence, start, end, contained);
 			
 			while ( i.hasNext() )  {
 				SAMRecord record = i.next();
 				
 				/*
 				int toFilter = record.getFlags() & filter;
 				logger.debug(String.format("Read: %s, Filter: %s, Flags: %s, Result: %s", record.getReadName(), filter, record.getFlags(), toFilter ));
 				logger.debug(padLeft(Integer.toBinaryString(filter), 8));
 				logger.debug(padLeft(Integer.toBinaryString(record.getFlags()), 8));
 				logger.debug(padLeft(Integer.toBinaryString(toFilter), 8));
 				*/
 				
 				if ((record.getFlags() & filter) > 0) {
 					//logger.debug("some matches ... skipping");
 					continue;
 				}
 				
 				for (Entry<Method, String> entry : methods2properties.entrySet()) {
 					Method method = entry.getKey();
 					String propertyName = entry.getValue();
 					Object result = method.invoke(record, new Object[]{});
 					List<Object> list = model.records.get(propertyName);
 					list.add(result);
 				}
 				
 				model.count++;
 				
 			}
 		
 		} catch (Exception e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 			
 		} finally {
 			if (i != null) {
 				i.close();
 			}
 		}
 		
 		long endTime = System.currentTimeMillis() ;
 		float time = (endTime - startTime) / (float) 1000 ;
 		
 		model.contained = contained;
 		model.start = start;
 		model.end = end;
 		model.sequence = sequence;
 		model.time = Float.toString(time);
 		
 		return model;
 	}
 	
 	
 	public synchronized MappedCoverage coverage(int fileID, String sequence, int start, int end, int window) throws Exception {
 		return this.coverage(getSamOrBam(fileID), sequence, start, end, window);
 	}
 	
 	public synchronized MappedCoverage coverage(SAMFileReader file, String sequence, int start, int end, int window) throws Exception {
 		
 		long startTime = System.currentTimeMillis();
 		
 		int max = 0;
 		final int nBins = Math.round((end-start+1.f)/window);
 		
 	    int coverage[] = new int[nBins];
 	    
 	    for(int i=0; i<coverage.length; i++) {
 	    	coverage[i] = 0;  
 	    }
 		
 	    logger.debug("starting iterations");
 	    logger.debug(start + "," + end + "," + window + "," + nBins);
 		
 		SAMRecordIterator iter = null;
 		
 		
 		logger.debug(startTime);
 		
 		try {
 			iter = file.query(sequence, start, end, false);
 			while (iter.hasNext()) {
 				
 				SAMRecord record = iter.next();
 				List<AlignmentBlock> blocks = record.getAlignmentBlocks();
 				
 				for (AlignmentBlock block : blocks) {
 					for (int k = 0; k < block.getLength(); k++) {
 						
 						final int pos = block.getReferenceStart() + k - start;
 						final float fbin =  pos / (float) window;
 						
 						if ((fbin < 0) || (fbin > nBins-1)) {
 							continue;
 						}
 						
 						final int ibin = (int) fbin;
                         
 						coverage[ibin] += 1;
 						
 						if(coverage[ibin] > max) {
 							max = coverage[ibin];
 						}
 					}
 				}
 			}
 			
 		} catch (Exception e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 			
 		} finally {
 			if (iter != null) {
 				iter.close();
 			}
 		}
 		
 		long endTime = System.currentTimeMillis() ;
 		float time = (endTime - startTime) / (float) 1000 ;
 		
 		
 		
 		logger.debug("ending iterations");
 		
 		MappedCoverage mc = new MappedCoverage();
 		mc.coverage = coverage;
 		mc.start = start;
 		mc.end = end;
 		mc.window = window;
 		mc.max = max;
 		mc.time = Float.toString(time);
 		mc.bins = nBins;
 		
 		return mc;
 		
 	}
 	
 	private String padLeft(String s, int n) {
 	    return String.format("%1$#" + n + "s", s);  
 	}
 	
 }
