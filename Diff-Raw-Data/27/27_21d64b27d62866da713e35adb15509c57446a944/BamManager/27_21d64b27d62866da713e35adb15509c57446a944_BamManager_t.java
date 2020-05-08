 package org.bioinfo.gcs.lib.bam;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ResourceBundle;
 
 import net.sf.samtools.CigarOperator;
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMRecord.SAMTagAndValue;
 import net.sf.samtools.SAMRecordIterator;
 
 import org.bioinfo.commons.Config;
 import org.bioinfo.commons.io.utils.FileUtils;
 import org.bioinfo.commons.log.Logger;
 
 import com.google.gson.Gson;
 
 
 public class BamManager {
 	
 	protected Gson gson = new Gson();
 	protected Config config;
 	protected Logger logger;
 	
 	public BamManager(ResourceBundle properties) throws IOException{
 //		ResourceBundle properties = ResourceBundle.getBundle("application");
 		config = new Config(properties);
 		gson = new Gson();
 		logger = new Logger();
 		logger.setLevel(Logger.INFO_LEVEL);
 	}
 	
 //	public String getByRegion(List<Region> regions)
 //	open_file
 //		for(region) {
 //			SAMRecordIterator recordsFound = inputSam.query(region);
 //		}
 //	}
 	
 	public String getByRegion(final String fileName, final String chr, final int start, final int end) throws IOException{
 		
 		System.out.println("chr: "+chr+" start: "+start+" end: "+end);
 
 		File inputSamFile = new File(config.getProperty("FILES.PATH")+"/bam/"+fileName+".bam");
 		File indexFile = null;
 		if(!new File(config.getProperty("FILES.PATH")+"/bam/"+fileName+".bam.bai").exists()) {
 			// crearlo!
 			
 		}
 		indexFile = new File(config.getProperty("FILES.PATH")+"/bam/"+fileName+".bam.bai");
 		
 		
 		long t = System.currentTimeMillis();
 		final SAMFileReader inputSam = new SAMFileReader(inputSamFile, indexFile);
 		System.out.println("new SamFileReader in "+(System.currentTimeMillis()-t)+"ms");
 		
 		System.out.println("hasIndex " + inputSam.hasIndex());
 		
 		t = System.currentTimeMillis();
 		SAMRecordIterator recordsFound = inputSam.query(chr, start, end, false);
 		
 //		Coverage coverage = new HsMetricCollector.Coverage(new Interval(chr, start, end), 10);
 //		short[] coverages = coverage.getDepths();
 		System.out.println("query SamFileReader in "+(System.currentTimeMillis()-t)+"ms");
 		
 		/**
 		 * COVERAGE
 		 */
 		short[] coverageArray = new short [end-start+1];
 		short[] aBaseArray = new short [end-start+1];
 		short[] cBaseArray = new short [end-start+1];
 		short[] gBaseArray = new short [end-start+1];
 		short[] tBaseArray = new short [end-start+1];
 		
 		t = System.currentTimeMillis();
 		StringBuilder sb = new StringBuilder();
 		sb.append("{");
 		sb.append("\"reads\":[");
 		StringBuilder attrString;
 		String readStr;
 		int readPos;
 		while(recordsFound.hasNext()){
 			SAMRecord record = recordsFound.next();
 		
 			attrString = new StringBuilder();
 			attrString.append("{");
 			for (SAMTagAndValue attr : record.getAttributes()) {
 				attrString.append("\""+attr.tag+"\":\""+    attr.value.toString().replace("\\", "\\\\").replace("\"", "\\\"")    +"\",");
 			}
 			//Remove last comma
 			if(attrString.length()>1){
 				attrString.replace(attrString.length()-1, attrString.length(), "");
 			}
 			attrString.append("}");
 			
 			readStr = record.getReadString();
 			
			/*
			 *Base quality ascii conversion
			 * */
//			String baseQualityString = record.getBaseQualityString();
//			int baseLen = baseQualityString.length();
//			short[] baseQualityArray = new short[baseLen];
//			for (int i = 0; i < baseLen; i++) {
//				baseQualityArray[i] = (short)baseQualityString.charAt(i);
//			}
			/**/
			
			
 			sb.append("{");
 			sb.append("\"start\":"+record.getAlignmentStart()+",");
 			sb.append("\"end\":"+record.getAlignmentEnd()+",");
 			sb.append("\"chromosome\":\""+chr+"\",");
 			sb.append("\"flags\":\""+record.getFlags()+"\",");//with flags the strand will be calculated
 			sb.append("\"cigar\":\""+record.getCigarString()+"\",");
 			sb.append("\"name\":\""+record.getReadName()+"\",");
 			sb.append("\"blocks\":\""+record.getAlignmentBlocks().get(0).getLength()+"\",");
 			
 			sb.append("\"attributes\":"+attrString.toString()+",");
 			
 			sb.append("\"readGroupId\":\""+record.getReadGroup().getId()+"\",");
 			sb.append("\"readGroupPlatform\":\""+record.getReadGroup().getPlatform()+"\",");
 			sb.append("\"readGroupLibrary\":\""+record.getReadGroup().getLibrary()+"\",");
 			sb.append("\"referenceName\":\""+record.getReferenceName()+"\",");
 			sb.append("\"baseQualityString\":\""+record.getBaseQualityString().replace("\\", "\\\\").replace("\"", "\\\"")+"\",");// the " char unables parse from javascript
//			sb.append("\"baseQualityString\":\""+gson.toJson(baseQualityArray)+"\",");// the " char unables parse from javascript
 			sb.append("\"header\":\""+record.getHeader().toString()+"\",");
 			sb.append("\"readLength\":"+record.getReadLength()+",");
 			sb.append("\"mappingQuality\":"+record.getMappingQuality()+",");
 			
 			sb.append("\"mateReferenceName\":\""+record.getMateReferenceName()+"\",");
 			sb.append("\"mateAlignmentStart\":"+record.getMateAlignmentStart()+",");
 			sb.append("\"inferredInsertSize\":"+record.getInferredInsertSize()+",");
 
 			sb.append("\"read\":\""+readStr+"\"");
 			sb.append("},");
 			
 			
 //			
 			
 			
 //			//TODO cigar check for correct coverage calculation
 			int refgenomeOffset = 0;
 			int readOffset = 0;
 			int offset = record.getAlignmentStart()-start;
 			for(int i=0; i < record.getCigar().getCigarElements().size(); i++) {
 //				System.out.println(record.getCigar().getCigarElement(i).getLength());
 				if(record.getCigar().getCigarElement(i).getOperator() == CigarOperator.M) {
 					for(int j=record.getAlignmentStart()-start+refgenomeOffset, cont=0; cont<record.getCigar().getCigarElement(i).getLength(); j++,cont++) {
 						if(j>=0 && j<coverageArray.length) {
 							coverageArray[j]++;
 							readPos = j-offset;
 //							if(record.getAlignmentStart() == 32877696){
 //								System.out.println(i-(record.getAlignmentStart()-start));
 //								System.out.println(record.getAlignmentStart()-start);
 //							}
 //							System.out.print(" - "+(cont+readOffset));
 //							System.out.print("|"+readStr.length());
 							int total = cont+readOffset;
 //							if(total < readStr.length()){
 								switch(readStr.charAt(total)){
 								case  'A': aBaseArray[j]++; break;
 								case  'C': cBaseArray[j]++; break;
 								case  'G': gBaseArray[j]++; break;
 								case  'T': tBaseArray[j]++; break;
 								}
 //							}
 						}
 					}
 				}
 				if(record.getCigar().getCigarElement(i).getOperator() != CigarOperator.I) {
 					refgenomeOffset += record.getCigar().getCigarElement(i).getLength()-1;
 					readOffset += record.getCigar().getCigarElement(i).getLength()-1;
 				}else { 
 					refgenomeOffset++;
 					readOffset += record.getCigar().getCigarElement(i).getLength()-1;
 				}
 			}
 //			System.out.println("-------------");
 			
 			
 //			System.out.println("***\n"+(record.getAlignmentStart()-start));
 //			System.out.println(record.getAlignmentEnd()-start);
 //			System.out.println(record.getCigarString());
 //			System.out.println(record.getReadLength());
 //			System.out.println(record.getAlignmentEnd()-record.getAlignmentStart()+1);
 //			System.out.println(readStr.length()+"\n*****");
 			
 //			int offset = record.getAlignmentStart()-start;
 //			for(int i=offset; i<=record.getAlignmentEnd()-start ;i++) {
 //				if(i>=0 && i<coverageArray.length) {
 //					coverageArray[i]++;
 ////						if(record.getAlignmentStart() == 32877696){
 ////							System.out.println(i-(record.getAlignmentStart()-start));
 ////////							System.out.println(record.getAlignmentStart()-start);
 ////////							System.out.println(record.getAlignmentStart());
 //////							System.out.println(readStr.length());
 ////						}
 //					readPos = i-offset;
 //					if(readPos < readStr.length()){
 //						switch(readStr.charAt(readPos)){
 //						case  'A': aBaseArray[i]++; break;
 //						case  'C': cBaseArray[i]++; break;
 //						case  'G': gBaseArray[i]++; break;
 //						case  'T': tBaseArray[i]++; break;
 //						}
 //					}
 //				}
 //			}
 		}
 		//Remove last comma
		if(sb.length()>1 && sb.charAt(sb.length()-1) == ','){
 			sb.replace(sb.length()-1, sb.length(), "");
 		}
 		
 //		//FIXME
 //		sb.append("]");
 //		sb.append(",\"coverage\":"+gson.toJson(coverageArray));
 //		sb.append("}");
 		
 		
 		//FIXME
 		sb.append("]");
 		sb.append(",\"coverage\":{\"all\":"+gson.toJson(coverageArray));
 		sb.append(",\"a\":"+gson.toJson(aBaseArray));
 		sb.append(",\"c\":"+gson.toJson(cBaseArray));
 		sb.append(",\"g\":"+gson.toJson(gBaseArray));
 		sb.append(",\"t\":"+gson.toJson(tBaseArray));
 		sb.append("}");
 		sb.append("}");
 		
 		
 		String json = sb.toString();
 		
 		System.out.println("query result processed in "+(System.currentTimeMillis()-t)+"ms");
 		
 //		IOUtils.write("/tmp/dqslastgetByRegionCall", json);
 		
 		inputSam.close();
 		return json;
 	}
 	
 	
 	public String getFileList(){
 		File bamDir = new File(config.getProperty("FILES.PATH")+"/bam");
 		try {
 			FileUtils.checkDirectory(bamDir);
 			
 			File[] files = FileUtils.listFiles(bamDir, ".+.bam");
 			StringBuilder sb = new StringBuilder();
 			
 			sb.append("[");
 			for (int i = 0; i < files.length; i++) {
 				if(!files[i].isDirectory()){
 					File bai = new File(files[i].getAbsolutePath()+".bai");	
 					try {
 						FileUtils.checkFile(bai);
 						sb.append("\""+files[i].getName()+"\",");
 					} catch (IOException e) {
 						logger.info(files[i].getName()+" was not added because "+files[i].getName()+".bai was not found.");
 					}
 				}
 			}
 			//Remove last comma
 			if(sb.length()>1){
 				sb.replace(sb.length()-1, sb.length(), "");
 			}
 			sb.append("]");
 			
 			logger.info(sb.toString());
 			return sb.toString();
 			
 		} catch (IOException e1) {
 			return bamDir.getAbsolutePath() +"not exists.";
 		}
 		
 		
 	}
 	
 	
     public static class BamMiniRecord {
         // public fields are serialized.
         public String cigarString;
         public int alignmentStart;
     }
 	
 }
