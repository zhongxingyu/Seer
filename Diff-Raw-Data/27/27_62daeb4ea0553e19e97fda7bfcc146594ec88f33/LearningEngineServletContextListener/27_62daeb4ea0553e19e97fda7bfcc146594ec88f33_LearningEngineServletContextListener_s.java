 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import edu.neumont.learningChess.engine.LearningEngine;
 
 
 public class LearningEngineServletContextListener implements ServletContextListener {
 
 	private static final String LEARNING_ENGINE = "LearningEngine";
 	private static final long RECORD_SIZE = 32; // TODO this needs to be fixed before intergration
 	
 	@Override
 	public void contextDestroyed(ServletContextEvent contextEvent) {
 		
 		ServletContext context = contextEvent.getServletContext();
 		LearningEngine learningEngine = (LearningEngine) context.getAttribute(LEARNING_ENGINE);
 		learningEngine.close();
 		context.setAttribute(LEARNING_ENGINE, null);
 	}
 	
 	@Override
 	public void contextInitialized(ServletContextEvent contextEvent) {
		
 		ServletContext context = contextEvent.getServletContext();
 		String fileName = context.getInitParameter("FILE_NAME");
 		String cacheSize = context.getInitParameter("CACHE_SIZE");
 		
 		context.log("fileName: " + fileName);
 		context.log("cacheSize: " + cacheSize);
 		context.log("LearningEngine: " + LEARNING_ENGINE);
 		context.log("record size: " + RECORD_SIZE);
 		
 		LearningEngine learningEngine = null;
 		try
 		{
 			learningEngine = LearningEngine.open(fileName);
 			
 		} catch (Throwable e)
 		{
 			LearningEngine.create(fileName, Integer.parseInt(cacheSize));
 			learningEngine = LearningEngine.open(fileName);
 		}
 		
 		context.setAttribute(LEARNING_ENGINE, learningEngine);
 		
 	}
 
 }
