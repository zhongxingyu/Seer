 package org.sakaiproject.hierarchy.tool.vm;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.hierarchy.api.HierarchyServiceException;
 import org.sakaiproject.hierarchy.api.PortalHierarchyService;
 import org.sakaiproject.hierarchy.api.model.Hierarchy;
 import org.sakaiproject.hierarchy.api.model.PortalNode;
 import org.sakaiproject.hierarchy.tool.vm.NewSiteCommand.Method;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.sitemanage.api.SiteHelper;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 public class ReuseSiteController extends SimpleFormController {
 
 	private String cancelledView;
 
 	private Method defaultMethod;
 	
 	public ReuseSiteController() {
 		setCommandClass(NewSiteCommand.class);
 		setValidator(new NewSiteValidator());
 	}
 
 	@Override
 	protected void onBindOnNewForm(HttpServletRequest request, Object object)
 			throws Exception {
 		NewSiteCommand command = (NewSiteCommand)object;
 		HttpSession session = request.getSession();
 		
 		Object attribute = session.getAttribute(SiteHelper.SITE_PICKER_SITE_ID);
 		if (attribute instanceof String) {
 			String siteId = (String)attribute;
 			// If throw exception if doesn't exist and bomb out.
 			Site site = SiteService.getSite(siteId);
 			command.setTitle(site.getTitle());
			command.setSiteId(siteId);
 		}		
 		
 	}
 	
 	protected Object formBackingObject(HttpServletRequest request) throws Exception {
 		NewSiteCommand command = (NewSiteCommand)super.formBackingObject(request);
 		// Make sure the default method is set.
 		command.setMethod(defaultMethod);
 		return command;
 	}
 	
 	@Override
 	protected ModelAndView handleRequestInternal(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		HttpSession session = request.getSession();
 		if (session.getAttribute(SiteHelper.SITE_PICKER_CANCELLED) != null) {
 			session.removeAttribute(SiteHelper.SITE_PICKER_CANCELLED);
 			return new ModelAndView(cancelledView);
 		}
 		return super.handleRequestInternal(request, response);
 	}
 
 	@Override
 	protected ModelAndView onSubmit(HttpServletRequest request,
 			HttpServletResponse response, Object object, BindException errors)
 			throws Exception {
 		
 		
 		NewSiteCommand command = (NewSiteCommand)object;
 		PortalHierarchyService hs = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
 		String sitePath = null;
 		try {
 			PortalNode node = hs.getCurrentPortalNode();
 			PortalNode newNode = hs.newNode(node.getId(), command.getName(), command.getSiteId(), node.getManagementSite().getId());
 			sitePath = newNode.getPath();
			Map model = errors.getModel();
			model.put("siteUrl", ServerConfigurationService.getPortalUrl()+"/hierarchy"+ sitePath);
 		} catch (Exception hse) {
 			errors.reject("error.add.hierarchy");
			return showForm(request, errors, getFormView(), errors.getModel());
 		} 
 		

 		return super.onSubmit(request, response, command, errors);
 	}
 
 	public String getCancelledView() {
 		return cancelledView;
 	}
 
 	public void setCancelledView(String cancelledView) {
 		this.cancelledView = cancelledView;
 	}
 
 	public Method getDefaultMethod() {
 		return defaultMethod;
 	}
 
 	public void setDefaultMethod(Method defaultMethod) {
 		this.defaultMethod = defaultMethod;
 	}
 
 	@Override
 	protected Map referenceData(HttpServletRequest request, Object command,
 			Errors errors) throws Exception {
 		return VelocityControllerUtils.referenceData(request, command, errors);
 	}
 	
 	
 
 	
 	
 }
