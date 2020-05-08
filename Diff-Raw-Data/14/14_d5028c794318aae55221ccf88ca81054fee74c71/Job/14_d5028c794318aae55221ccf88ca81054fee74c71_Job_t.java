 import java.io.*;
 import java.util.*;
 
 public abstract class Job implements Serializable {
     private int jid;
     private int totalMaps;
     private int totalReduces;
     private String outputFileName;
     private List<String> inputFileNames;
 
     public Job() {
	totalMaps = 10;
	totalReduces = 3;
     }
 
     public void setInput(List<String> inputFiles){
 	inputFileNames = inputFiles;
     }
     public void setOutput(String outputFile){
 	outputFileName = outputFile;
     }
     public List<String> getInput(){
 	return inputFileNames;
     }
     public String getOutput(){
 	return outputFileName;
     }
     public int getJid() {
 	return jid;
     }
     public void setJid(int j){
 	jid = j;
     }
     public int getTotalMaps(){
 	return totalMaps;
     }
     public void setTotalMaps(int i){
 	totalMaps = i;
     }
     public int getTotalReduces(){
 	return totalReduces;
     }
     public void setTotalReduces(int i){
 	totalReduces = i;
     }
     
     //implemented by subclass
     public abstract String getIdentity();
 
     public abstract List<String[]> map(String key, String val);
     public abstract String reduce(String key, List<String> vals, String init);
 }
