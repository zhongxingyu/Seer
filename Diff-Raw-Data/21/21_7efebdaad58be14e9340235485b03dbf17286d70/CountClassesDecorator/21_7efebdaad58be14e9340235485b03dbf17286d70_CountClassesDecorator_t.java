 package org.sonar.plugins.modelbus.batch;
 
 /**
  *
  * @deprecated use ServicePostJob
  *
  */
 
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.sonar.api.batch.Decorator;
 import org.sonar.api.batch.DecoratorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.resources.Language;
 
 import org.sonar.api.resources.ResourceUtils;
 import org.sonar.plugins.modelbus.Resources;
 import org.sonar.plugins.modelbus.language.uml.Uml;
 import org.sonar.plugins.modelbus.smmparser.SmmModelAdapter;
 
 ;
 
 public class CountClassesDecorator implements Decorator {
 	public static final Logger LOG = LoggerFactory.getLogger(CountClassesDecorator.class);
 
 	String baseDirPath;
 	String modelPath;
 
 	public boolean shouldExecuteOnProject(Project project) {
 		LOG.info("Testlog in CountClassesDecorator");
 
 		baseDirPath = project.getFileSystem().getBasedir().getAbsolutePath();
 		modelPath = baseDirPath + ModelBusPluginConst.MODEL_PATH;
 
 		LOG.info("baseDirPath: " + baseDirPath);
 		LOG.info("Modelpath: " + modelPath);
 
 		// execute on all until we get the right language code
 		return true;
 	}
 
 	public void decorate(@SuppressWarnings("rawtypes") Resource resource, DecoratorContext context) {
 		LOG.debug("decorating resource "+resource+" (" +resource.getLongName()+")");
 		
 		if (ResourceUtils.isFile(resource)) {
 			 
 			Language language = resource.getLanguage();
			if (language instanceof Uml) {
 				Resources resources = Resources.getInstance();
 
 				String key = resource.getKey();
 				
 				//if (key.endsWith(Resources.UML_EXT)) {
 
 					SmmModelAdapter smm = resources.getModel();
 				
 					Map<Metric, Double> measurements = smm.getMeasurements(resource);
 					// smm.getMetrics();
 					
 					if(measurements!=null)
 					for (Metric m : measurements.keySet()) {
 						try {
 							context.saveMeasure(m, measurements.get(m));
 						}
 						catch(Exception e) {
							LOG.warn("Could not save measure \""+m+"\" for resource \""+resource.getName()+"\": "+e.getMessage());
 						}
 					}
 				//}
 			}
 		}
 
 	}
 
 }
