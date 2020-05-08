 package nz.co.searchwellington.controllers.models;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nz.co.searchwellington.repositories.ContentRetrievalService;
 import nz.co.searchwellington.views.JsonViewFactory;
 import nz.co.searchwellington.views.RssViewFactory;
 
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 
 public class ContentModelBuilderService {
 	
 	private static Logger logger = Logger.getLogger(ContentModelBuilderService.class);
 
 	private static final String JSON_CALLBACK_PARAMETER = "callback";
 	
 	private RssViewFactory rssViewFactory;
 	private JsonViewFactory jsonViewFactory;
 	private JsonCallbackNameValidator jsonCallbackNameValidator;
 	private ContentRetrievalService contentRetrievalService;
 	private ModelBuilder[] modelBuilders;
 	
 	public ContentModelBuilderService(RssViewFactory rssViewFactory, JsonViewFactory jsonViewFactory, JsonCallbackNameValidator jsonCallbackNameValidator, ContentRetrievalService contentRetrievalService, ModelBuilder[] modelBuilders) {
 		this.rssViewFactory = rssViewFactory;
 		this.jsonViewFactory = jsonViewFactory;
 		this.jsonCallbackNameValidator = jsonCallbackNameValidator;
 		this.contentRetrievalService = contentRetrievalService;
 		this.modelBuilders = modelBuilders;
 	}
 	
 	public ModelAndView populateContentModel(HttpServletRequest request) {
 		for (int i = 0; i < modelBuilders.length; i++) {
 			ModelBuilder modelBuilder = modelBuilders[i];
 			if (modelBuilder.isValid(request)) {
 				logger.info("Using " + modelBuilder.getClass().getName() + " to serve path: " + request.getPathInfo());
 				
 				ModelAndView mv = modelBuilder.populateContentModel(request);
 				
 				final String path = request.getPathInfo();
 				if (path.endsWith("/rss")) {
 					logger.info("Selecting rss view for path: " + path);
 					mv.setView(rssViewFactory.makeView());
 					return mv;
 				}				
 				if (path.endsWith("/json")) {
 					logger.info("Selecting json view for path: " + path);
 					mv.setView(jsonViewFactory.makeView());					
 					populateJsonCallback(request, mv);								
 					return mv;
 				}
 				
 				if (mv != null) {
					mv.setViewName(modelBuilder.getViewName(mv));
 					modelBuilder.populateExtraModelConent(request, mv);
 					addCommonModelElements(mv);
 					return mv;
 				}
 				return null;				
 			}
 		}
 		logger.warn("No matching model builders found for path: " + request.getPathInfo());
         return null;
 	}
 
 	private void populateJsonCallback(HttpServletRequest request, ModelAndView mv) {
 		if(request.getParameter(JSON_CALLBACK_PARAMETER) != null) {
 			final String callback = request.getParameter(JSON_CALLBACK_PARAMETER);
 			if (jsonCallbackNameValidator.isValidCallbackName(callback)) {
 				logger.info("Adding callback to model:" + callback);
 				mv.addObject(JSON_CALLBACK_PARAMETER, callback);
 			}	 
 		}
 	}
 	
 	private void addCommonModelElements(ModelAndView mv) {
 		mv.addObject("top_level_tags", contentRetrievalService.getTopLevelTags());
 		mv.addObject("featuredTags", contentRetrievalService.getFeaturedTags());
 	}
 	
 }
