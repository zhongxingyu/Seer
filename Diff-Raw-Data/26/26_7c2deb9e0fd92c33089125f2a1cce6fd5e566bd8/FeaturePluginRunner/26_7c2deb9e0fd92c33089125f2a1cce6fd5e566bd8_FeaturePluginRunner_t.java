 package io.seqware.queryengine.sandbox.testing.plugins;
 
 import io.seqware.queryengine.sandbox.testing.model.VCFReader;
 import io.seqware.queryengine.sandbox.testing.model.txtJSONParser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.commons.io.FilenameUtils;
 import org.broadinstitute.variant.variantcontext.VariantContext;
 
 public class FeaturePluginRunner {
 	//TODO: 1. Filter files
 	//		2. Run plugin on the filtered files
 	
 	public Map<String,String> applyMap(String InputFilePath, Map<FeatureSet, Collection<Feature>> features) throws IOException{
 		VCFReader inputVCF = 
 				new VCFReader(InputFilePath);
 			
 			Map<String,String> output = 
 				new HashMap<String,String>();
 			
 		Iterator<VariantContext> vcfIter = inputVCF.getVCFIterator();
 		String position;
 
 		 
 		
 		while(vcfIter.hasNext()){
 			VariantContext variant = vcfIter.next();
 			position = Integer.toString(variant.getStart());
 			Iterator it = variant.getAttributes().entrySet().iterator();
 			
 			while( it.hasNext()){
 				Map.Entry pair = (Map.Entry)it.next();
 				System.out.println(pair.getValue().toString());
 			}
 
 		}
 		
 		
 		return null;
 	}
 
 	//Apply filter as determined by the JSON query
 	public void getFilteredFiles(String Directory, String queryJSON, String OutputFilePath) throws IOException{
 		String InputFilePath;
 		File filedir = 
 				new File(Directory);
		System.out.println(OutputFilePath);
 		Feature FeatureID = 
 				new Feature();
 		
		File makefile = new File(Directory);
		if (!makefile.exists()){
			boolean success = makefile.mkdirs();
		}
		
 		txtJSONParser JParse = new txtJSONParser(queryJSON);
 		HashMap<String, String> fsmapq = JParse.getFEATURE_SET_MAP_QUERY();
 		
 		//Generate Complete Map of FeatureSetId and INFO
 		for (File child : filedir.listFiles()){
 			
 			InputFilePath = child.getAbsolutePath();
 			String filename = child
 					.getName()
 					.substring(0, child.getName().indexOf("."));
 			
 			if (FilenameUtils.getExtension(InputFilePath).equals("vcf")
 					&& ((fsmapq.keySet().contains(filename)) || (fsmapq.size() ==0))){ 
 				//Write this to temp file output
 				FeatureID.readVCFinfo(InputFilePath, queryJSON, OutputFilePath, filename); //Read INFO fields in VCF only
 
 			}
 			
 		}
 	}
 	
 	
 	//TODO Create function to go through input vcf file and implement map function
 	public Map<FeatureSet, Collection<Feature>> makeMapInput(String Directory) throws IOException{
 		File filedir = 
 				new File(Directory);
 		
 		Map<FeatureSet,Collection<Feature>> MapInput = 
 				new HashMap<FeatureSet,Collection<Feature>>();
 		
 		ArrayList<Feature> Features = 
 				new ArrayList<Feature>();
 		
 		String line;
 		for (File child : filedir.listFiles()){
 			String AbsolutePath = child.getAbsolutePath();
 			if (FilenameUtils.getExtension(AbsolutePath).equals("txt")){
 				FeatureSet featureset = 
 						new FeatureSet(child);
 				
 				BufferedReader in = 
 						new BufferedReader(
 								new FileReader(AbsolutePath));
 				
 				while((line = in.readLine()) != null){
 					Feature feature = 
 							new Feature(line);
 						Features.add(feature);
 				}
 				MapInput.put(featureset, Features);	
 			}
 		}
 		return MapInput;
 	}
 	
 }
