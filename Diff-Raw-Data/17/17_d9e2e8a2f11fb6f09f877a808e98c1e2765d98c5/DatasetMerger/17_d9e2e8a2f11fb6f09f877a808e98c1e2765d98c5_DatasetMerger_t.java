 package experiments;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.util.FileManager;
 
 public class DatasetMerger {
 	
 	/**
 	 * From <http://jena.apache.org/tutorials/rdf_api.html#ch-Operations on Models>:
 	 * Jena provides three operations for manipulating Models as a whole. These are the common set operations of 
 	 * union, intersection and difference. The union of two Models is the union of the sets of statements which 
 	 * represent each Model. This is one of the key operations that the design of RDF supports. It enables data from 
 	 * disparate data sources to be merged.
 	 * 
 	 * I am going to use the union operation to merge my two (abridged) datasets, triplesWhereObjectIsAMusicalWorkExtract.xml and
 	 * triplesWhereSubjectIsAMusicalWorkExtract.xml
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		/* log4j logging configuration */
 		Logger.getRootLogger().setLevel(Level.INFO);
		PropertyConfigurator.configure("log4j.properties");
 		
 		// create two empty models into which to read my two RDF/XML files
 		Model model1 = ModelFactory.createDefaultModel();
 		Model model2 = ModelFactory.createDefaultModel();
 		
		String model1Url = "data\\mergedTestDatasetAug13b.xml";
		String model2Url = "data\\dbPediaExtracts\\subjectIsAnAlbumNoAbstractsNoCommentsWithLabels.xml";
 		
 		
 		// Load the two files into the two models
 		model1 = FileManager.get().loadModel(model1Url);
 		model2 = FileManager.get().loadModel(model2Url);
 		
 		Model mergedModel = model1.union(model2); // create a third model which is the union of the two models
 		
 		/* Define a prefix for one of the many namespaces used in the merged model
 		mergedModel.setNsPrefix("dbpedia-owl","http://dbpedia.org/ontology/"); */
 		
 		NsPrefixLoader prefixLoader = new NsPrefixLoader(mergedModel);
 		prefixLoader.loadStandardPrefixes();
 		
 		// Now, write the model out to a file in RDF/XML-ABBREV format:
		String fileName = "data\\mergedTestDatasetAug13c.xml";
 		try {
 			FileOutputStream outFile = new FileOutputStream(fileName);
 			mergedModel.write(outFile, "RDF/XML-ABBREV");
 			outFile.close();
 		}
 		catch(FileNotFoundException e) {
 			e.printStackTrace();
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
