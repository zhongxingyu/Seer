 package cern.accsoft.steering.jmad.modeldefs;
 
 import java.io.File;
 
 import org.apache.log4j.BasicConfigurator;
 
 import cern.accsoft.steering.jmad.modeldefs.defs.lhc.LhcAtsModelDefinitionFactory;
 import cern.accsoft.steering.jmad.modeldefs.defs.lhc.LhcNominalModelDefinitionFactory;
 import cern.accsoft.steering.jmad.modeldefs.defs.lhc.LhcThinModelDefinitionFactory;
 import cern.accsoft.steering.jmad.modeldefs.domain.JMadModelDefinition;
 import cern.accsoft.steering.jmad.modeldefs.io.ModelDefinitionPersistenceService;
 import cern.accsoft.steering.jmad.modeldefs.io.impl.ModelDefinitionUtil;
 import cern.accsoft.steering.jmad.modeldefs.io.impl.XmlModelDefinitionPersistenceService;
 import cern.accsoft.steering.jmad.util.xml.PersistenceServiceException;
 
 public class ModelDefCreator {
 
     public final static void main(String[] args) {
 
         String destPath = "";
         if (args.length > 0) {
             destPath = args[0];
         }
 
         BasicConfigurator.configure();
 
         ModelDefinitionFactory[] factories = new ModelDefinitionFactory[] { //
         /*    */new LhcNominalModelDefinitionFactory(), //
                 new LhcThinModelDefinitionFactory(), //
                 new LhcAtsModelDefinitionFactory() };
 
         ModelDefinitionPersistenceService service = new XmlModelDefinitionPersistenceService();
 
         for (ModelDefinitionFactory factory : factories) {
             JMadModelDefinition modelDefinition = factory.create();
             String fileName = ModelDefinitionUtil.getProposedXmlFileName(modelDefinition);
             String filePath;
             if (destPath.length() > 0) {
                 filePath = destPath + "/" + fileName;
             } else {
                filePath = fileName;
             }
             File file = new File(filePath);
             System.out.println("Writing file '" + file.getAbsolutePath() + "'.");
             try {
                 service.save(modelDefinition, file);
             } catch (PersistenceServiceException e) {
                 System.out.println("Could not save model definition to file '" + file.getAbsolutePath());
                 e.printStackTrace();
             }
         }
 
     }
 }
