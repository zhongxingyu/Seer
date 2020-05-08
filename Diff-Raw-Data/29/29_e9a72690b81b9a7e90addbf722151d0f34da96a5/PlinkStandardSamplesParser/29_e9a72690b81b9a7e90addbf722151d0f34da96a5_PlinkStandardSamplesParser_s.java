 package org.gwaspi.samples;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedList;
 import org.gwaspi.constants.cImport;
 import org.gwaspi.model.SampleInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PlinkStandardSamplesParser implements SamplesParser {
 
 	private static final Logger log
 			= LoggerFactory.getLogger(PlinkStandardSamplesParser.class);
 
 	@Override
 	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {
 
 		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();
 		FileReader inputFileReader;
 		BufferedReader inputBufferReader;
 
 		File sampleFile = new File(sampleInfoPath);
 		inputFileReader = new FileReader(sampleFile);
 		inputBufferReader = new BufferedReader(inputFileReader);
 
 		char[] chunker = new char[300];
 		inputBufferReader.read(chunker, 0, 300);
 		if (String.valueOf(chunker).contains("\n")) { // SHORT PED FILE
 			inputBufferReader.close();
 			inputFileReader.close();
 			inputFileReader = new FileReader(sampleFile);
 			inputBufferReader = new BufferedReader(inputFileReader);
 
 			int count = 0;
 			while (inputBufferReader.ready()) {
 				String l = inputBufferReader.readLine();
 				SampleInfo sampleInfo;
 				if (chunker.length > 0) {
 					String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10);
					String sexStr = cVals[cImport.Annotation.Plink_Binary.ped_sex];
 					sexStr = sexStr.equals("-9") ? "0" : sexStr;
					String affectionStr = cVals[cImport.Annotation.Plink_Binary.ped_affection];
 					affectionStr = affectionStr.equals("-9") ? "0" : affectionStr;
 					SampleInfo.Sex sex = SampleInfo.Sex.parse(sexStr);
 					SampleInfo.Affection affection = SampleInfo.Affection.parse(affectionStr);
 					sampleInfo = new SampleInfo(
							cVals[cImport.Annotation.Plink_Binary.ped_sampleId],
							cVals[cImport.Annotation.Plink_Binary.ped_familyId],
							cVals[cImport.Annotation.Plink_Binary.ped_fatherId],
							cVals[cImport.Annotation.Plink_Binary.ped_motherId],
 							sex,
 							affection,
 							"0",
 							"0",
 							"0",
 							0
 							);
 				} else {
 					sampleInfo = new SampleInfo();
 				}
 
 				sampleInfos.add(sampleInfo);
 
 				count++;
 				if (count % 100 == 0) {
 					log.info("Parsed {} Samples for info...", count);
 				}
 			}
 		} else { // LONG PED FILE
 			// This has sucked out 1 week of my life and caused many grey hairs!
 			int count = 0;
 			while (inputBufferReader.ready()) {
 				if (count != 0) {
 					chunker = new char[300];
 					inputBufferReader.read(chunker, 0, 300); // Read a sizable but conrolled chunk of data into memory
 				}
 
 				SampleInfo sampleInfo;
 				if (chunker.length > 0) {
 					String[] cVals = String.valueOf(chunker).split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10);
					String sexStr = cVals[cImport.Annotation.Plink_Binary.ped_sex];
 					sexStr = sexStr.equals("-9") ? "0" : sexStr;
					String affectionStr = cVals[cImport.Annotation.Plink_Binary.ped_affection];
 					affectionStr = affectionStr.equals("-9") ? "0" : affectionStr;
 					SampleInfo.Sex sex = SampleInfo.Sex.parse(sexStr);
 					SampleInfo.Affection affection = SampleInfo.Affection.parse(affectionStr);
 					sampleInfo = new SampleInfo(
							cVals[cImport.Annotation.Plink_Binary.ped_sampleId],
							cVals[cImport.Annotation.Plink_Binary.ped_familyId],
							cVals[cImport.Annotation.Plink_Binary.ped_fatherId],
							cVals[cImport.Annotation.Plink_Binary.ped_motherId],
 							sex,
 							affection,
 							"0",
 							"0",
 							"0",
 							0
 							);
 				} else {
 					sampleInfo = new SampleInfo();
 				}
 				inputBufferReader.readLine(); // Read rest of line and discard it...
 
 				sampleInfos.add(sampleInfo);
 
 				count++;
 				if (count % 100 == 0) {
 					log.info("Parsed {} Samples for info...", count);
 				}
 			}
 		}
 
 		inputBufferReader.close();
 		inputFileReader.close();
 
 		return sampleInfos;
 	}
 }
