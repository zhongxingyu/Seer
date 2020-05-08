 package dataStructures.maps;
 
 import java.util.Map.Entry;
 import java.util.Set;
 
 import dataStructures.Importance;
 
 @SuppressWarnings("serial")
 public class ImportanceMap extends SuperkeyMap<Importance>{
 	
 	public ImportanceMap(){
 		super();
 	}
 	
 	public ImportanceMap(int uniqueID){
 		super(uniqueID);
 	}
 
 	public String toXML(){
		String importances = "<IMPORTANCES>\n";
		importances += "\t<UNIQUEMAPID>"+uniqueID+"</UNIQUEMAPID>\n";
 		Set<Entry<Integer, Importance>> allAttributes = entrySet();
 		for(Entry<Integer, Importance> entry : allAttributes){
 			importances += entry.getValue().toXML();
 		}
		importances += "</IMPORTANCES>\n";
 		return importances;
 	}
 	
 }
