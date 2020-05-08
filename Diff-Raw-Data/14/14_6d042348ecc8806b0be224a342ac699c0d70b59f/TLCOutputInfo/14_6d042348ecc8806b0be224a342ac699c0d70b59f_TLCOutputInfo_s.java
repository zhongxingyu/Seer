 package de.b2tla.tlc;
 
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Set;
 
 import de.b2tla.analysis.MachineContext;
 import de.b2tla.analysis.Renamer;
 import de.b2tla.btypes.BType;
 import de.be4.classicalb.core.parser.node.Node;
 
 public class TLCOutputInfo {
 	
 	Hashtable<String, String> namesMapping;
 	Hashtable<String, BType> typesTable;
 	Set<String> constants;
 	
 	public String getBName(String tlaName){
 		return namesMapping.get(tlaName);
 	}
 	
 	public BType getBType(String tlaName){
 		return typesTable.get(tlaName);
 	}
 	
 	public Hashtable<String, BType> getTypes(){
 		return typesTable;
 	}
 	
 	public boolean isAConstant(String tlaName){
 		return constants.contains(getBName(tlaName));
 	}
 	
 	public boolean hasConstants(){
		return constants.size()>0;
 	}
 	
 	public TLCOutputInfo(MachineContext machineContext, Renamer renamer,
 			Hashtable<Node, BType> types) {
 		
 		namesMapping = new Hashtable<String, String>();
 		typesTable = new Hashtable<String, BType>();
 		constants = machineContext.getConstants().keySet();
 		
 		LinkedHashMap<String, Node> identifiers = new LinkedHashMap<String, Node>();
 		identifiers.putAll(machineContext.getConstants());
 		identifiers.putAll(machineContext.getVariables());
 		
 		for (Iterator<String> iter = identifiers.keySet().iterator(); iter.hasNext();) {
 			String name = iter.next();
 			Node node = identifiers.get(name);
 			String newName = renamer.getNameOfRef(node);
 			namesMapping.put(newName, name);
 			typesTable.put(newName, types.get(node));
 		}
 		
 	}
 
 }
