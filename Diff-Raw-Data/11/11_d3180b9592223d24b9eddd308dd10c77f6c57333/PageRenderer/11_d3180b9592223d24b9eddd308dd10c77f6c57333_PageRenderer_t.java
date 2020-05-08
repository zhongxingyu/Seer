 package org.otherobjects.cms.controllers.renderers;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.model.CmsNode;
 import org.otherobjects.cms.util.StringUtils;
 import org.springframework.util.Assert;
 import org.springframework.web.servlet.ModelAndView;
 
 public class PageRenderer implements ResourceRenderer
 {
     //    private NavigatorService navigatorService;
     //    private SiteNavigatorService siteNavigatorService;
     private DaoService daoService;
 
     public ModelAndView handleRequest(CmsNode o, HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         BaseNode resourceObject = (BaseNode) o;
 
         // Determine template to use
         BaseNode template = determineTemplate(resourceObject);
         Assert.notNull(template, "No template found for type: " + resourceObject.getTypeDef().getName());
 
         // Return page and context
        ModelAndView view = new ModelAndView("/site/templates/layouts/" + ((String)template.get("layout.code")).replaceAll("\\.html","") + "");
 
         view.addObject("resourceObject", resourceObject);
 
         view.addObject("ooTemplate", template);
         //        view.addObject("navigatorService", this.navigatorService);
         //        view.addObject("siteNavigator", this.siteNavigatorService);
         view.addObject("daoService", this.daoService);
         //if (SiteItem.class.isAssignableFrom(resourceObject.getClass())) //put the trail in the ctx if we are dealing with a SiteItem object
         //    view.addObject("trail", siteNavigatorService.getTrail((SiteItem) resourceObject));
 
         return view;
     }
 
     public DaoService getDaoService()
     {
         return daoService;
     }
 
     public void setDaoService(DaoService daoService)
     {
         this.daoService = daoService;
     }
 
     /**
      * FIXME Classes for TemplateDao and Template?
      * @param resourceObject
      * @return
      */
     private BaseNode determineTemplate(BaseNode resourceObject)
     {
         if (resourceObject.hasProperty("template") && resourceObject.get("template") != null)
             return (BaseNode) resourceObject.get("template");
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
        
        String templateCode = "";
        if(resourceObject.getTypeDef().getName().contains("."))
            templateCode = StringUtils.substringAfterLast(resourceObject.getTypeDef().getName(), ".").toLowerCase();
        else 
            templateCode = resourceObject.getTypeDef().getName().toLowerCase();
            
         BaseNode template = universalJcrDao.getByPath("/designer/templates/" + templateCode);
         return template;
     }
 }
