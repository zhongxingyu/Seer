 package it.polimi.jbps.web.config;
 
 import static com.google.common.collect.Maps.newHashMap;
 import static it.polimi.jbps.utils.OntologyUtils.getOntologyFromFile;
 import it.polimi.jbps.bpmn.simulation.OntologySimulator;
 import it.polimi.jbps.engine.Engine;
 import it.polimi.jbps.engine.SimpleEngine;
 import it.polimi.jbps.form.Form;
 import it.polimi.jbps.form.FormsConfiguration;
 import it.polimi.jbps.model.OntologyModelManipulator;
 
import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Lazy;
 import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 
import com.google.common.base.Charsets;
import com.google.common.io.Files;
 import com.hp.hpl.jena.ontology.OntModel;
 
 @Configuration
 @Lazy
 public class AppConfig {
 	
 	private final Map<String, String> bpmnOntologyPaths;
 	private final String modelOntologyPath;
 	private final Map<String, String> formAssociationPaths;
 	private final Map<String, String> lanesDescriptions;
 	private final String baseURI;
 	
 	public AppConfig(Map<String, String> bpmnOntologyPaths, String modelOntologyPath,
 			Map<String, String> formAssociationPaths, Map<String, String> lanesDescriptions,
 			String baseURI) {
 		this.bpmnOntologyPaths = bpmnOntologyPaths;
 		this.modelOntologyPath = modelOntologyPath;
 		this.formAssociationPaths = formAssociationPaths;
 		this.lanesDescriptions = lanesDescriptions;
 		this.baseURI = baseURI;
 	}
 	
     @Bean
     public ViewResolver viewResolver() {
         InternalResourceViewResolver resolver = new InternalResourceViewResolver();
         resolver.setPrefix("/WEB-INF/views/");
         resolver.setSuffix(".jsp");
         return resolver;
     }
     
     @Bean
     public Map<String, String> lanesDescriptions() {
     	return lanesDescriptions;
     }
     
     @Bean
     public Map<String, OntModel> bpmnOntologyByLane() {
     	Map<String, OntModel> bpmnOntologyByLane = newHashMap();
     	for (String lane : bpmnOntologyPaths.keySet()) {
     		bpmnOntologyByLane.put(lane, getOntologyFromFile(bpmnOntologyPaths.get(lane)));
     	}
     	return bpmnOntologyByLane;
     }
     
     @Bean
     public OntModel modelOntology() {
     	return getOntologyFromFile(modelOntologyPath);
     }
     
     @Bean
     public Map<String, Engine> engines() throws IOException {
     	Map<String, Engine> engines = newHashMap();
     	Map<String, OntModel> bpmnOntologyByLane = bpmnOntologyByLane();
     	
     	for (String lane : bpmnOntologyByLane.keySet()) {
     		String laneFormPath = formAssociationPaths.get(lane);
     		
     		OntModel modelOntology = modelOntology();
    		File formAssociationFile = new File(laneFormPath);
    		String formAssociationJson = Files.toString(formAssociationFile, Charsets.UTF_8);
     		
    		Form form = new Form(FormsConfiguration.createFromFile(formAssociationJson, modelOntology));
     		
     		SimpleEngine simpleEngine = new SimpleEngine(
     				new OntologySimulator(bpmnOntologyByLane.get(lane)),
     				new OntologyModelManipulator(modelOntology, form, baseURI));
     		
     		engines.put(lane, simpleEngine);
     	}
 		return engines;
     }
 }
