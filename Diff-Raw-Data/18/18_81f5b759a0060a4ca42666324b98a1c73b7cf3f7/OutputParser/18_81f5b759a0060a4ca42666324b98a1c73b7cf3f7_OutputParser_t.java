 package it.uniroma3.dia.alfred.mpi;
 
 import it.uniroma3.dia.alfred.mpi.model.ConfigHolder;
 import it.uniroma3.dia.alfred.mpi.model.XPathHolder;
 import it.uniroma3.dia.alfred.mpi.model.constants.ConfigHolderKeys;
 import it.uniroma3.dia.alfred.mpi.model.serializer.XPathHolderSerializable;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 public class OutputParser {
 	private OutputParser() {}
 
 	private static final String DEFAULT_WORKING_DIRECTORY = "";
 	private static final String DEFAULT_OUTPUT_DIRECTORY = "ouputJson";
 	private static final String DEFAULT_FILE_EXT = ".json";
 	
 	private static Map<String, XPathHolder> domain2XPathHolderMap;
 
 	private static XPathHolder getXPathFromDomain(String domain){
 	XPathHolder xPathHolder = domain2XPathHolderMap.get(domain);
 		if (xPathHolder == null){
 			xPathHolder = new XPathHolder();
 			domain2XPathHolderMap.put(domain, xPathHolder);
 		}
 		return xPathHolder;
 	}
 	
 	public static boolean parse(List<ConfigHolder> configHolderList) {
 		return parse(configHolderList, DEFAULT_WORKING_DIRECTORY, DEFAULT_OUTPUT_DIRECTORY);
 	}
 	
 	@SuppressWarnings("resource")
 	public static boolean parse(List<ConfigHolder> configHolderList, String workingDirectory, String outputFilesPath) {
 		
 		domain2XPathHolderMap = Maps.newHashMap();
 		
 		String currentOutputFolderPath = null;
 		File currentOutputFolder = null;
 		OutputFileDomainFilter currentOutputFilter = new OutputFileDomainFilter();
 		String currentDomain = null;
 		String currentAttribute = null;
 		XPathHolder currentXPathHolder = null;
 		
 		BufferedReader reader = null;
 		String currentLine = null;
 		
 		if (workingDirectory==null){
 			workingDirectory = "";
 		}
 		
 		for (ConfigHolder configHolder : configHolderList) {
 			
 			currentOutputFolderPath = workingDirectory + configHolder.getConfigurationValue(ConfigHolderKeys.OUTPUT_FOLDER_KEY);
 			currentOutputFolder = new File(currentOutputFolderPath);
 			
 			currentOutputFilter.setUidToFilter(configHolder.getUid());
 			
 			currentDomain = configHolder.getUid().split(ConfigHolderIdBroker.ID_DIVISOR)[1];
 			currentXPathHolder = getXPathFromDomain(currentDomain);
 			
 			for(File currentFile : currentOutputFolder.listFiles(currentOutputFilter)){
 				
 				currentAttribute = currentFile.getName().split("-")[2]; //cambiare
 				
 				try {
 					reader = new BufferedReader(new FileReader(currentFile));
 				} catch (FileNotFoundException e) {
 					System.err.println("Error in opening "+ currentFile.getName());
 					e.printStackTrace();
 					return false;
 				}
 				
 				try {
 					while ((currentLine = reader.readLine()) != null) {
 						currentXPathHolder.addXpathToAttr(currentAttribute, currentLine);
 					}
 				} catch (IOException e) {
 					System.err.println("Error in reading "+ currentFile.getName());
 					e.printStackTrace();
 					return false;
 				}
 				
 				currentXPathHolder.addXpathToAttr(currentAttribute, configHolder.getAssociatedDomain().getGoldenXPath(currentAttribute));
 				
 			}
 
 		}
 		
		new File(outputFilesPath + "/").mkdirs();
 		for(String domain : domain2XPathHolderMap.keySet()){
 			XPathHolderSerializable.toJsonFile(domain2XPathHolderMap.get(domain), outputFilesPath+"/"+domain+DEFAULT_FILE_EXT);
 		}
 
 		domain2XPathHolderMap.clear();
 		return true;
 	}
 
 }
 
 class OutputFileDomainFilter implements FileFilter {
 
 	private String uidToFilter;
 	
 	@Override
 	public boolean accept(File file) {
 		return file.getName().matches("^"+uidToFilter+".*");
 	}
 
 	public String getUidToFilter() {
 		return uidToFilter;
 	}
 
 	public void setUidToFilter(String uidToFilter) {
 		this.uidToFilter = uidToFilter;
 	}
 	
 }
