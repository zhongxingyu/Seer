 package cn.uc.play.japid.template;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Load template by this interface. Used by TemplateLoaderFactory.
  * 
  * @author Robin Han<sakuyahan@163.com>
  * @date 2012-4-29
  */
 public interface UJapidTemplateLoader {
 	
	final String FILE_FILTER = "^japidviews(\\\\|/).*(\\.html|\\.json|\\.txt|\\.xml)$";
 	
 	/**
 	 * Get single template by path.
 	 * It doesn't always to load from store.
 	 * 
 	 * @param path
 	 * @return
 	 * @throws Exception
 	 */
 	UJapidTemplate getTemplate(String path) throws Exception;
 	
 	/**
 	 * Load single template by path.
 	 * It loads the template from store for every invoking.
 	 * 
 	 * @param path 
 	 *            Template path.
 	 *            
 	 * @return A UJapidTemplate object.
 	 */
 	UJapidTemplate loadTemplate(String path) throws Exception;
 	
 	
 	/**
 	 * Load all templates into a path-object map.
 	 * It loads templates data from store for every invoking.
 	 * @return
 	 */
 	Map<String, UJapidTemplate> loadAllTemplates() throws Exception;
 }
