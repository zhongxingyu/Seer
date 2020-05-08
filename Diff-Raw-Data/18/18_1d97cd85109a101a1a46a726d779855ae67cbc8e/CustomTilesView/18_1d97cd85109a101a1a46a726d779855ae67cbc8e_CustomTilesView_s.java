 package au.com.regimo.web.spring;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.hibernate.Hibernate;
 import org.springframework.context.MessageSource;
 import org.springframework.context.support.MessageSourceResourceBundle;
 import org.springframework.web.context.support.ContextExposingHttpServletRequest;
 import org.springframework.web.servlet.view.tiles2.TilesView;
 
 import au.com.regimo.core.domain.Dashboard;
 import au.com.regimo.core.domain.UserDashlet;
 import au.com.regimo.core.repository.DashboardRepository;
 
 import com.google.common.collect.Sets;
 
 public class CustomTilesView extends TilesView {
 
 	private static String KEY_MENU = "menu";
 	private static String KEY_MODEL = "modelName";
 	private static String KEY_MESSAGE = "msg";
 
 	private Dashboard menu = null;
 
 	@Override
 	protected void renderMergedOutputModel(
 			Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
 		HttpServletRequest requestToExpose = new ContextExposingHttpServletRequest(
 				request, getWebApplicationContext(), Sets.newHashSet("referenceData", "securityService"));
 		model.put(KEY_MESSAGE, new MessageSourceResourceBundle(getWebApplicationContext().getBean(
 				MessageSource.class), request.getLocale()));
 		if(!model.containsKey(KEY_MENU)){
 			if(menu==null){
 				DashboardRepository r = getWebApplicationContext().getBean(DashboardRepository.class);
 				menu = r.findByViewName("HomeMenu");
 				if(menu!=null)
 				for(UserDashlet ud: menu.getUserDashlets()){
 					Hibernate.initialize(ud.getDashlet());
 				}
 			}
 			model.put(KEY_MENU, menu);
 		}
 		if(!model.containsKey(KEY_MODEL)){
 			String url = getUrl();
 			int firstSlash = url.indexOf("/");
 			if(firstSlash > 0){
 				url = url.substring(0, firstSlash);
 			}
 			model.put(KEY_MODEL, url);
			logger.debug(KEY_MODEL+": "+url);
 		}
 		super.renderMergedOutputModel(model, requestToExpose, response);
 	}
 
 }
