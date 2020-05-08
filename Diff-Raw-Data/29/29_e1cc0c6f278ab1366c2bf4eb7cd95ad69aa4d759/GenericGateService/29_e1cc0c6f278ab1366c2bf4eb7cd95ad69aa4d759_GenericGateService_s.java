 /**
  * Copyright (C) 2011-
  *
  * All rights reserved.
  */
 package gov.va.vinci.v3nlp.services.gate;
 
 import gate.Factory;
 import gate.ProcessingResource;
 import gate.creole.ResourceInstantiationException;
 import gate.creole.SerialAnalyserController;
 import gov.va.vinci.cm.DocumentInterface;
 import gov.va.vinci.v3nlp.registry.NlpComponent;
 import gov.va.vinci.v3nlp.services.NlpProcessingUnit;
 
 import java.util.List;
 
 public class GenericGateService extends BaseGateService implements NlpProcessingUnit {
 
 	protected ProcessingResource resource;
 
     public GenericGateService(ProcessingResource resource) {
         this.resource = resource;
     }
 
     public DocumentInterface process(String config, DocumentInterface document,List<NlpComponent> previousModuleProvided) {
         SerialAnalyserController controller = null;
 		gate.Corpus corpus = null;
 
 		gov.va.vinci.cm.Corpus results = new gov.va.vinci.cm.Corpus();
 
 		try {
 			controller = getSerialAnalyserController();
 			controller.reInit();
 
 			// Add Resource
 			controller.add(resource);
 
 			// Add Corpus
 			corpus = createGateCorpusFromCommonModel(document);
 			controller.setCorpus(corpus);
 
 			// run the application
 			controller.execute();
			return(processGateResults(corpus, (gov.va.vinci.cm.Document)document, 0, "GATE|" + resource.getClass().getCanonicalName()));
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} finally {
 			cleanupPipeLine(controller, corpus);
 		}
     }
 
     protected SerialAnalyserController getSerialAnalyserController() throws ResourceInstantiationException {
            return (SerialAnalyserController) Factory.createResource(
                                "gate.creole.SerialAnalyserController", Factory
                                .newFeatureMap(), Factory.newFeatureMap(), "V3NLP");
     }
 
     @Override
     public void initialize() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void destroy() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
