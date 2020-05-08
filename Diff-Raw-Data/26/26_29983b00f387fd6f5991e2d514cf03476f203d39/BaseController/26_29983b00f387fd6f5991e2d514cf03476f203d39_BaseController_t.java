 package ru.pfo.controller;
 
import java.io.IOException;
import java.net.URLEncoder;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.NoSuchMessageException;
import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
 
 import ru.pfo.Constant;
 import ru.pfo.controller.model.SiteModel;
 import ru.pfo.model.VideoNews;
 import ru.pfo.service.ICategoryService;
 import ru.pfo.service.IVideoNewsService;
 import ru.pfo.util.PropertiesUtil;
 
 public class BaseController {
 
 	private static final Logger LOG = Logger.getLogger(BaseController.class);
 
 	@Autowired
 	protected IVideoNewsService videoNewsService;
 
 	@Autowired
 	protected ICategoryService categoryService;
 
 	@Autowired
 	private ApplicationContext context;
 
 	@ModelAttribute("siteModel")
 	public SiteModel getSiteModel(HttpServletRequest request) {
 		List<VideoNews> bestVideoNewses = videoNewsService.getBestVideoNewses();
 		SiteModel siteModel = new SiteModel();
 		siteModel.setBestVideoNewses(bestVideoNewses);
 		return siteModel;
 	}
 
 	/**
 	 * @param code
 	 *            message code
 	 * @param arg
 	 *            can by null
 	 * @param locale
 	 *            , if is null then locale will by default locale from props
 	 * @return
 	 */
 	protected String getMessage(String code, String[] arg, Locale locale) {
 		if (locale == null) {
 			locale = new Locale(PropertiesUtil.getProperty(Constant.PROPS_KEY_DEFAULT_LOCALE));
 		}
 		String message = null;
 		try {
 			message = context.getMessage(code, arg, locale);
 		} catch (NoSuchMessageException e) {
 			LOG.warn(e);
 		}
 		return message;
 	}

	@ExceptionHandler(Exception.class)
	public String handleException(Exception ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
		return "redirect:/error?message=" + URLEncoder.encode(ex.toString(), "UTF-8") + "&code=500";
	}
 }
