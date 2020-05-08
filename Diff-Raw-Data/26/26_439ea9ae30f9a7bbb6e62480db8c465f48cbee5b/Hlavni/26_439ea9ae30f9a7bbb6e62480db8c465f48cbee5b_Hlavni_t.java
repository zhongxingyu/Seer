 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.List;
 
 import com.hp.hpl.jena.ontology.OntModelSpec;
 
 import tools.JenaBeanExtensionTool;
 import tools.OntologyProperties;
 import tools.OwlApiTool;
 import tools.Syntax;
 
 
 /**
  * Třída, která plní funkci "uživatelského programu".
  * Zde se vytvoří objekty určené k transformaci a zavolá se metoda, která proces spustí.
  *
  * @author Jakub Krauz
  */
 public class Hlavni {
 	
 	private static InputStream is;
 
     public static void main(String[] args) {
     	
     	/* vytvoreni objektu - musi provadet uzivatel knihovny sam */
     	//ExampleHodneDat ex = new ExampleHodneDat(1000);
     	Example ex = new Example();
     	List<Object> dataList = ex.getVstupniPole();
     	
     	JenaBeanExtensionTool jenaBean;
     	//OwlApiTool owlApi;
 		try {
 			jenaBean = new JenaBeanExtensionTool(dataList);
 			jenaBean.setBasePackage("data.pojo");
 			jenaBean.setOntology(new OntologyProperties("http://www.kiv.zcu.cz/eegdatabase"));
 			is = jenaBean.getOntologyDocument(Syntax.RDF_XML_ABBREV);
 //			owlApi = new OwlApiTool(is);
 //			is = owlApi.convertToSemanticStandard("owl");
 			
 			/*jenaBean = new JenaBeanExtensionTool(dataList);
 			jenaBean.setBasePackage("data.pojo");
 			System.out.println("JenaBean: data nactena");
 			FileOutputStream out = new FileOutputStream(new File("ontologyDocument.owl"));
 			jenaBean.writeOntologyDocument(out, Syntax.RDF_XML_ABBREV);
 			out.close();
 			System.out.println("Zapsano do souboru.");*/
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
    	writeSemanticToFile("ontologyDocument2.owl");
 
     }
     
     
     @SuppressWarnings("unused")
 	private static void writeSemanticToFile(String fileName) {
     	System.out.println("- USER -");
     	System.out.println("Creating file " + fileName + "...");
     	try {
 			FileOutputStream out = new FileOutputStream(new File(fileName));
 			int pom;
 			while ((pom = is.read()) != -1) {
 				out.write(pom);
 			}
 			out.close();
 			System.out.println("File " + fileName + " was created.");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     
     @SuppressWarnings("unused")
 	private static void vypisTrvani(long millis, String usek) {
 		long min = millis / 60000;
 		long sec = (millis - min * 60000) / 1000;
 		System.out.println(usek + ": " + min + " min, " + sec + " sec");
     }
     
 }
