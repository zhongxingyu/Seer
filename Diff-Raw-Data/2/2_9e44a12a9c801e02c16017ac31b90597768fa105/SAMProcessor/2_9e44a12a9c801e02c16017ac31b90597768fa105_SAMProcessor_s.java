 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMFileReader.ValidationStringency;
 import net.sf.samtools.util.CloseableIterator;
 
 import org.biojava.bio.BioException;
 import org.biojava.bio.seq.db.HashSequenceDB;
 import org.biojava.bio.seq.db.SequenceDB;
 import org.bjv2.util.cli.Option;
 
 
 public abstract class SAMProcessor {
 	private SAMFileReader inReader;
 	private SequenceDB seqDB = new HashSequenceDB();
 	private int qualityCutoff = 10;
 	
 	private File indexFile;
 	private String in;
 	protected Map<String,Integer> refSeqLengths = new HashMap<String,Integer>();
 	
 	
 	
 	private int windowSize = 1;
 	protected int frequency = 10;
 	private IterationType iterationType = IterationType.ONE_BY_ONE;
 	private QueryType queryType = QueryType.RECORD;
 	private ArrayList<String> nameList;
 
 	public enum IterationType {
 		ONE_BY_ONE,
 		MOVING_WINDOW,
 		WITH_FREQUENCY
 	}
 	
 	public enum QueryType {
 		RECORD,
 		CONTAINED,
 		OVERLAP
 	}
 
 	@Option(help="Input reads (SAM/BAM formatted)")
 	public void setMap(String in) {
 		this.in = in;
 	}
 	
 	public void setIterationType(IterationType type) {
 		this.iterationType = type;
 	}
 	
 	public void setQueryType(QueryType type) {
 		this.queryType  = type;
 	}
 	
 	@Option(help="Sequence window size around the current position (default=1)", optional=true)
 	public void setWindowSize(int i) {
 		this.windowSize = i;
 	}
 	
 	@Option(help="Frequency of positions sampled (default=1)", optional=true)
 	public void setFreq(int i) {
 		this.frequency = i;
 	}
 	
 	@Option(help="Index file for the reads", optional=true)
 	public void setIndex(File f) {
 		this.indexFile = f;
 	}
 	
 	@Option(help="Reference sequence names and lengths in a TSV formatted file")
 	public void setRefLengths(File f) throws NoSuchElementException, BioException, NumberFormatException, IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(f));
 		
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			StringTokenizer tok = new StringTokenizer(line,"\t");
 			refSeqLengths.put(tok.nextToken(), Integer.parseInt(tok.nextToken()));
 		}
 		
 		nameList = new ArrayList<String>(refSeqLengths.keySet());
 		Collections.sort(
 				nameList, 
 				new Comparator<String>() {
 					public int compare(String str1, String str2) {
 						Integer thisI = new Integer(Integer.parseInt(str1));
 						Integer otherI = Integer.parseInt(str2);
 						return thisI.compareTo(otherI);
 					}
 				}
 		);
 	}
 	
 	@Option(help="Mapping quality threshold (exclude reads whose mapping quality is below. default=10)", optional=true)
 	public void setMappingQualityCutoff(int quality) {
 		this.qualityCutoff = quality;
 	}
 	
 	//override in subclass to handle QueryType.CONTAINED and QueryType.OVERLAP
 	public void process(List<SAMRecord> recs, String refName, int begin, int end, int seqLength) {
 		
 	}
 	
 	//override in subclass to handle QueryType.ONE_BY_ONE
 	public void process(SAMRecord rec, int readIndex) {
 		
 	}
 	
 	public void initializeSAMReader() {
 		if (this.in.equals("-")) {
 			if (queryType != QueryType.RECORD) {
 				System.err.println("Query type -record is the only allowed query type when reading from stdin");
 				System.exit(1);
 			}
 			this.inReader = new SAMFileReader(System.in);
 		} else {
 			if (indexFile == null && (this.queryType.equals(QueryType.CONTAINED) || this.queryType.equals(QueryType.OVERLAP))) {
				System.err.println("Index file was not specified");
 				System.exit(2);
 			}
 			
 			if (indexFile != null) {
 				this.inReader = new SAMFileReader(new File(in),indexFile);				
 			} else {
 				this.inReader = new SAMFileReader(new File(in));
 			}
 		}
 		this.inReader.setValidationStringency(ValidationStringency.SILENT);
 	}
 
 	//the main method in subclasses can pretty much look like this (you can customise of course)
 	public void main(String[] args) throws BioException {
 		initializeSAMReader();
 		process();
 	}
 	
 	public void process() throws BioException {
 		
 		if (iterationType == IterationType.ONE_BY_ONE) {
 			int excludedReads = 0;
 			int readCount = 0;
 
 			for (SAMRecord record : inReader) {
 				if ((readCount++ % frequency) != 0) continue;
 				
 				int quality = record.getMappingQuality();
 				if (quality < qualityCutoff) {
 					excludedReads += 1;
 					continue;
 				}
 				
 				process(record, readCount);
 			}
 			System.err.printf("Excluded %d reads (%.2f%)%n", excludedReads, (double)excludedReads / (double)readCount * 100.0);			
 		} else if (iterationType == IterationType.MOVING_WINDOW) {
 			int halfFreq = (frequency / 2);
 			int windowCenter = halfFreq;
 			
 			final List<SAMRecord> recs = new ArrayList<SAMRecord>();
 			
 			for (String seqName : nameList) {
 				int len = refSeqLengths.get(seqName);
 				while ((windowCenter + halfFreq) < len) {
 					CloseableIterator<SAMRecord> recIterator = this.query(seqName, windowCenter - halfFreq, windowCenter + halfFreq);
 					
 					iterateAndFilterToList(recIterator,recs);
 					process(recs,seqName,windowCenter - halfFreq,windowCenter + halfFreq,len);
 					recs.clear();
 				}
 				
 				windowCenter += frequency;
 			}
 			
 		} else if (iterationType == IterationType.WITH_FREQUENCY) {
 			int windowBegin = 0;
 			
 			final List<SAMRecord> recs = new ArrayList<SAMRecord>();
 			for (String seqName : nameList) {
 				int len = refSeqLengths.get(seqName);
 				
 				while ((windowBegin + frequency)  < len) {
 					CloseableIterator<SAMRecord> recIterator = this.query(seqName, windowBegin, windowBegin + frequency);
 					
 					iterateAndFilterToList(recIterator,recs);
 					recIterator.close();
 					process(recs,seqName,windowBegin,windowBegin + frequency,len);
 					recs.clear();
 				}
 			}
 			
 			windowBegin += frequency;
 		}
 	}
 
 	private List<SAMRecord> iterateAndFilterToList(CloseableIterator<SAMRecord> recIterator,final List<SAMRecord> recs) {
 		while (recIterator.hasNext()) {
 			SAMRecord rec = recIterator.next();
 			if (rec.getMappingQuality() < this.qualityCutoff) continue;
 			recs.add(rec);
 		}
 		return recs;
 	}
 	
 	private CloseableIterator<SAMRecord> query(String seqName, int begin, int end) {
 		if (this.queryType == QueryType.CONTAINED) {
 			return inReader.queryContained(seqName, begin, end);			
 		} else {
 			return inReader.queryOverlapping(seqName, begin, end);
 		}
 		
 	}
 }
