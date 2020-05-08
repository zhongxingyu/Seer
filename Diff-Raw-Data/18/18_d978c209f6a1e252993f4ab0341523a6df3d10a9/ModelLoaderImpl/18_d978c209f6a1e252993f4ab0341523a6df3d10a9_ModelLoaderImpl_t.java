 /**
  * 
  */
 package app;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.hp.hpl.jena.rdf.model.InfModel;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.reasoner.ReasonerRegistry;
 import com.hp.hpl.jena.util.FileManager;
 
 import experiments.NsPrefixLoader;
 import framework.ModelLoader;
import framework.Pop;
 
 import lombok.AccessLevel;
 import lombok.Setter;
 
 /**
  * @author Ben Griffiths
  *
  */
 public class ModelLoaderImpl implements ModelLoader {
	private final String ONTOLOGY_URI = Pop.LOCAL_VOCAB_URI;
 	@Setter(AccessLevel.PRIVATE) private InfModel model;
 	
 	@Override
 	public InfModel getModel() {
 		if(this.model != null)
 			return this.model; // if the model has already been loaded, simply return it
 		
 		/* log4j logging configuration */
 		Logger.getRootLogger().setLevel(Level.INFO);
 		PropertyConfigurator.configure("log4j.properties");
 		
 		Model baseModel = FileManager.get().loadModel(ONTOLOGY_URI); // Read my ontology into a model
 		
 		/* Create an inference model using my ontology */
 		this.setModel(ModelFactory.createInfModel(ReasonerRegistry.getOWLMiniReasoner(), baseModel));
 		// Was previously using OntModelSpec.RDFS_MEM_RDFS_INF.getReasoner()
 		
 		
 		// load standard prefixes into the model
 		NsPrefixLoader prefixLoader = new NsPrefixLoader(this.model);
 		prefixLoader.loadStandardPrefixes();
 		
 		return this.model;
 	}
 	
 	
 }
