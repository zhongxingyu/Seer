 package parser;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.commons.digester3.Digester;
 import org.xml.sax.SAXException;
 
 import dataClasses.ICD10;
 
 public class ICDParser {
 	
 	private ArrayList<ICD10> parsedICDs;
 	
 	public void addCode(ICD10 code) throws FileNotFoundException {
 		
 		parsedICDs.add(code);
 	}
 	
 	
 	public ArrayList<ICD10> getParsedICDs(){
 		return parsedICDs;
 	}
 	
 	
 	public ICDParser(String ICDPath) throws IOException, SAXException {
 		
 		parsedICDs = new ArrayList<ICD10>();
 		
 		Digester digester = new Digester();
 		
 		digester.setValidating(false);
 		
 		digester.addObjectCreate("rdf:RDF", ICDParser.class);
 		
 		digester.addObjectCreate("rdf:RDF/owl:Class", ICD10.class);
 		
 		digester.addCallMethod("rdf:RDF/owl:Class/rdfs:label", "setLabel", 0);
 		digester.addCallMethod("rdf:RDF/owl:Class/code_formatted", "setICDCode", 0);
 		digester.addCallMethod("rdf:RDF/owl:Class/synonym", "addSynonym", 0);
 		
 		digester.addSetNext("rdf:RDF/owl:Class/", "addCode");
 		
 		File input = new File(ICDPath);
 		digester.clear();
 		ICDParser parser = (ICDParser) digester.parse(input);
 	}
 	
 	public static void main(String[] args) throws IOException, SAXException {
		ICDParser icd = new ICDParser(Constants.ICDPath);
 	}
 
 }
