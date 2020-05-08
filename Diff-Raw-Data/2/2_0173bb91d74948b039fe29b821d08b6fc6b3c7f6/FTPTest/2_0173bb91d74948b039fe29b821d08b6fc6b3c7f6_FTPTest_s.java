 package org.genedb.crawl;
 
 import java.io.File;
 import java.io.IOException;
 
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMRecordIterator;
 import net.sf.samtools.SAMSequenceRecord;
 import net.sf.samtools.SAMFileReader.ValidationStringency;
 
 import org.apache.log4j.Logger;
 
 import uk.ac.sanger.artemis.components.variant.FTPSeekableStream;
 
 import java.net.SocketException;
 import java.net.URL;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 public class FTPTest extends TestCase {
 	
 	private static final Logger logger = Logger.getLogger(FTPTest.class);
 	
 	private static final String[] urls = new String[] {
 		//"ftp://ftp.sanger.ac.uk/pub/mouse_genomes/current_bams/129S1.bam",
		"ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/data/HG00096/alignment/HG00096.chrom20.ILLUMINA.bwa.GBR.low_coverage.20101123.bam"
 	};
 	
 	public void testURLs() throws SocketException, IOException {
 		for (String url : urls) {
 			run (new URL(url));
 		}
 	}
 	
 	public void run(URL url) throws SocketException, IOException {
 		
 		FTPSeekableStream fss = new FTPSeekableStream(url);
 		File index = fss.getIndexFile();
 		
 		SAMFileReader reader = new SAMFileReader(fss, index, false);
 		reader.getFileHeader();
 		
 		reader.setValidationStringency(ValidationStringency.SILENT);
 		
 		logger.info("attributes");
 		for (Map.Entry<String, String> entry : reader.getFileHeader().getAttributes()) {
 			logger.info(String.format("%s : %s", entry.getKey(), entry.getValue()));
 		}
 		
 		logger.info("sequences");
 		for (SAMSequenceRecord ssr : reader.getFileHeader().getSequenceDictionary().getSequences()) {
 			logger.info(String.format("%s : %s ", ssr.getSequenceName(),  ssr.getSequenceLength() ));
 		}
 		
 		logger.info("sequences");
 		for (SAMSequenceRecord ssr : reader.getFileHeader().getSequenceDictionary().getSequences()) {
 			
 //			if (! ssr.getSequenceName().equals("NT_166325")) {
 //				continue;
 //			}
 			
 			int length = ssr.getSequenceLength();
 			
 			int min = 1000;
 			int max = 100000;
 			
 			if (min >= length) {
 				min = 0;
 			}
 			
 			//logger.warn((max >= length));
 			
 			if (max >= length) {
 				max = length;
 			}
 			
 			if (min >= max) {
 				min = 0;
 			}
 			
 			logger.info(String.format("Sequence: %s (%s) %s-%s", ssr.getSequenceName(), length, min, max));
 			
 			SAMRecordIterator i = reader.query(ssr.getSequenceName(), min, max, false);
 			
 			while ( i.hasNext() )  {
 				SAMRecord record = i.next();
 				logger.info(String.format("Read: %s (%s (%s-%s) %s) / %s", 
 						record.getReadName(), 
 						min, 
 						record.getAlignmentStart(), 
 						record.getAlignmentEnd(), 
 						max, 
 						record.getFlags()));
 				
 				assertTrue(record.getAlignmentStart() >= min);
 			}
 			
 			i.close();
 			
 			logger.info("_________________________________________________");
 		}
 		
 		logger.info("Done");
 		
 	}
 	
 	
 	
 	
 
 
 	
 	
 	
 	
 
 }
