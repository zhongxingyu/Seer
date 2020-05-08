 package css.annotation;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.ontology.OntProperty;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 public class CSSTemplate {
 	
 	private Model model;
 	private OntModel ontModel;
 	
 	public CSSTemplate(String url){
 		try{
 			String vocabDomain = (new URL(url)).getHost();
 			model = RDFModelLoader.loadTriplesFromURL(url);
 			ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
 			ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF); //deal with owl:unionOf
 			ontModel.add(model);
 		}
 		catch(MalformedURLException murle){
 			murle.printStackTrace();
 		}
 	}
 	
 	public String getCSSSkeletonByClass(String flag, String prefix, OntClass c){
 		String cSSSkeleton = "";
 		String originFlag = flag;
 		flag = flag.replaceAll("\\s+?", "");
 		cSSSkeleton += 	"/*style for the " + c.getLocalName() + " (" + c.getURI() + ")"+ " as " + originFlag + "*/\r\n" +
 						"\t\r\n";
 		cSSSkeleton += 	"\t/*style for the type " + c.getLocalName() + "*/\r\n";
 		if(flag.equalsIgnoreCase("microdata"))
 			cSSSkeleton +=	"\t[itemscope][itemtype=\"" + c.getURI() + "\"] {\r\n" +
 //							"\t[itemscope] [itemtype=\"" + c.getURI() + "\"] {\r\n" +
 							"\t\r\n" +
 							"\t}\r\n" +
 							"\t\r\n";
 		else if(flag.equalsIgnoreCase("rdfalite"))
 			cSSSkeleton += 	"\t[typeof=\"" + c.getURI() + "\"],\r\n" +
 							"\t[typeof=\"" + prefix + ":" + c.getLocalName() + "\"],\r\n" +
 							"\t[typeof=\"" + c.getLocalName() + "\"] {\r\n" +
 							"\t\r\n" +
 							"\t}\r\n" +
 							"\t\r\n";
 		else 
 			System.err.println("Unknown format!");
 		Iterator properties = c.listDeclaredProperties();
 		while(properties.hasNext()){
 			OntProperty p = (OntProperty) properties.next();
 			if(!p.getNameSpace().equalsIgnoreCase(c.getNameSpace())) continue; //get rid of properties coming from another name spaces
 //			System.out.println(c.getURI() + " : " + p.getURI());
 			cSSSkeleton += "\t/*style for the property " + p.getLocalName()  + "*/\r\n";
 			if(flag.equalsIgnoreCase("microdata"))
 				cSSSkeleton +=	"\t[itemscope][itemtype=\"" + c.getURI() + "\"][itemprop=\"" + p.getLocalName() +"\"],\r\n" +
 								"\t[itemscope][itemtype=\"" + c.getURI() + "\"] [itemprop=\"" + p.getLocalName() +"\"] {\r\n" +
 //								"\t[itemscope] [itemtype=\"" + c.getURI() + "\"][itemprop=\"" + p.getLocalName() +"\"],\r\n" +
 //								"\t[itemscope] [itemtype=\"" + c.getURI() + "\"] [itemprop=\"" + p.getLocalName() +"\"] {\r\n" +
 								"\t\r\n" +
 								"\t}\r\n" +
 								"\t\r\n";
 			else if(flag.equalsIgnoreCase("rdfalite"))
 				cSSSkeleton +=	"\t[typeof=\"" + c.getURI() + "\"][property=\"" + p.getURI() + "\"],\r\n" +
 								"\t[typeof=\"" + c.getURI() + "\"] [property=\"" + p.getURI() + "\"],\r\n" +
 								"\t[typeof=\"" + prefix + ":" + c.getLocalName() + "\"][property=\"" + prefix + ":" + p.getLocalName() + "\"],\r\n" +
 								"\t[typeof=\"" + prefix + ":" + c.getLocalName() + "\"] [property=\"" + prefix + ":" + p.getLocalName() + "\"],\r\n" +
 								"\t[typeof=\"" + c.getLocalName() + "\"][property=\"" + p.getLocalName() + "\"],\r\n" +
 								"\t[typeof=\"" + c.getLocalName() + "\"] [property=\"" + p.getLocalName() + "\"] {\r\n" +
 								"\t\r\n" +
 								"\t}\r\n" +
 								"\t\r\n";
 			else
 				System.err.println("Unknown format!");
 		}
 		return cSSSkeleton.trim();
 	}
 	
 	public String getLessCSSSkeletonByClass(String flag, String prefix, OntClass c){
 		String cSSSkeleton = "";
 		String interpolations = "";
 		String originFlag = flag;
 		flag = flag.replaceAll("\\s+?", "");
		interpolations += 	"/*LESS for the " + c.getLocalName() + " (" + c.getURI() + ")"+ " as " + originFlag + "*/\r\n" +
 						"\t\r\n";
 		if(flag.equalsIgnoreCase("microdata")) {
 			interpolations += "\t@" + prefix + "_" + c.getLocalName() + ":~'[itemscope][itemtype=\"" + c.getURI() + "\"]';\r\n";
			cSSSkeleton += 	"\t/*LESS for the type " + c.getLocalName() + "*/\r\n";
 		}
 		else if(flag.equalsIgnoreCase("rdfalite")) {
 			interpolations += "\t@" + prefix + "_" + c.getLocalName() + ":~'[typeof=\"" + c.getURI() + "\"],[typeof=\"" + prefix + ":" + c.getLocalName() + "\"], [typeof=\"" + c.getLocalName() + "\"]';\r\n";
			cSSSkeleton += 	"\t/*LESS for the type " + c.getLocalName() + "*/\r\n";
 		}
 		else 
 			System.err.println("Unknown format!");
 		cSSSkeleton +=	"\t@{" + prefix + "_" + c.getLocalName() + "} {\r\n" +
 						"\t\r\n" +
 						"\t}\r\n" +
 						"\t\r\n";
 		Iterator properties = c.listDeclaredProperties();
 		while(properties.hasNext()){
 			OntProperty p = (OntProperty) properties.next();
 			if(!p.getNameSpace().equalsIgnoreCase(c.getNameSpace())) continue; //get rid of properties coming from another name spaces
 //			System.out.println(c.getURI() + " : " + p.getURI());
			cSSSkeleton += "\t/*LESS for the property " + p.getLocalName()  + "*/\r\n";
 			if(flag.equalsIgnoreCase("microdata")) {
				interpolations += "\t@" + prefix + "_" + c.getLocalName() + "-" + p.getLocalName() + "~'[itemscope][itemtype=\"" + c.getURI() + "\"][itemprop=\"" + p.getLocalName() +"\"],[itemscope][itemtype=\"" + c.getURI() + "\"] [itemprop=\"" + p.getLocalName() +"\"]';\r\n";
 			}
 			else if(flag.equalsIgnoreCase("rdfalite"))
				interpolations += "\t@" + prefix + "_" + c.getLocalName() + "-" + p.getLocalName() + "~'[typeof=\"" + c.getURI() + "\"][property=\"" + p.getURI() + "\"],[typeof=\"" + c.getURI() + "\"] [property=\"" + p.getURI() + "\"],[typeof=\"" + prefix + ":" + c.getLocalName() + "\"][property=\"" + prefix + ":" + p.getLocalName() + "\"],[typeof=\"" + prefix + ":" + c.getLocalName() + "\"] [property=\"" + prefix + ":" + p.getLocalName() + "\"],[typeof=\"" + c.getLocalName() + "\"][property=\"" + p.getLocalName() + "\"],[typeof=\"" + c.getLocalName() + "\"] [property=\"" + p.getLocalName() + "\"]';\r\n";
 			else
 				System.err.println("Unknown format!");
 			cSSSkeleton +=	"\t@{" + prefix + "_" + c.getLocalName() + "-" + p.getLocalName() + "} {\r\n" +
 							"\t\r\n" +
 							"\t}\r\n" +
 							"\t\r\n";
 		}
		return interpolations.trim() + cSSSkeleton.trim();
 	}
 	
 //	public String getVocabCSSSkeleton(String flag, String prefix, String baseURI){
 //		String cSSSkeleton = "";	
 //		Iterator classes = ontModel.listNamedClasses();
 //		while(classes.hasNext()){
 //			OntClass c = (OntClass) classes.next();
 //			//Presume that the namespace domain is the same with the domain of the vocabulary
 //			if(!c.getURI().startsWith(baseURI)) continue;
 ////				if(!(new URL(c.getURI())).getHost().equalsIgnoreCase(vocabDomain)) continue;
 ////					System.out.println(((OntClass) classes.next()).getLocalName());
 //			cSSSkeleton += getCSSSkeletonByClass(flag, prefix, c);
 //		}
 //		return cSSSkeleton.trim();
 //		
 //	}
 	
 	public String getCSSSkeleton(String targetURL, String flag, String prefix){
 		OntClass c = ontModel.getOntClass(targetURL);
 		String cSSSkeleton = getCSSSkeletonByClass(flag, prefix, c);
 		return cSSSkeleton.trim();
 	}
 	
 	public String getLessCSSSkeleton(String targetURL, String flag, String prefix){
 		OntClass c = ontModel.getOntClass(targetURL);
 		String cSSSkeleton = getLessCSSSkeletonByClass(flag, prefix, c);
 		return cSSSkeleton.trim();
 	}
 	
 	public static void main(String[] args){
 		CSSTemplate csst = new CSSTemplate("http://schema.rdfs.org/all.rdf");
 		System.out.println(csst.getCSSSkeleton("http://xmlns.com/foaf/spec/index.rdf", "RDFa Lite", "foaf"));
 //		System.out.println(csst.getCSSSkeleton("Microdata", "", "http://schema.org/"));
 //		System.out.println(csst.getCSSSkeleton("http://schema.org/Person", "Microdata", "", "http://schema.org/"));
 //		System.out.println(csst.getCSSSkeleton("http://schema.org/Person", "RDFa Lite", "foaf", "http://schema.org/"));
 	}
 
 }
