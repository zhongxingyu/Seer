 package org.otherobjects.cms.controllers.renderers;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.model.CmsNode;
 import org.otherobjects.cms.model.Template;
 import org.otherobjects.cms.model.TemplateLayout;
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
         Template template = determineTemplate(resourceObject);
         if (template == null)
         {
            ModelAndView mv = new ModelAndView("/otherobjects/templates/pages/oo-500-create-template");
             mv.addObject("resourceObject", resourceObject);
             String templateName = resourceObject.getTypeDef().getLabel();
             mv.addObject("templateName", templateName);
             // FIXME Bring this into a generic place
             mv.addObject("templateCode", templateName.toLowerCase().replaceAll("\\s", ""));
             mv.addObject("templates", getTemplates());
             mv.addObject("layouts", getLayouts());
             return mv;
         }
 
         // Return page and context
         TemplateLayout layout = template.getLayout();
         Assert.notNull(layout, "No layout defined for template: " + template.getLabel());
 
         ModelAndView view = new ModelAndView("/site/templates/layouts/" + layout.getCode().replaceAll("\\.html", "") + "");
 
         view.addObject("resourceObject", resourceObject);
 
         view.addObject("ooTemplate", template);
 
         // TODO Would be good to have a flag to enable this for testing
 //         ActionUtils actionUtils = new ActionUtils(request, response, null, null);
 //         actionUtils.flashWarning("Hey guys! How is it going?");
         
         return view;
     }
 
     private Object getTemplates()
     {
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         return universalJcrDao.getAllByType(Template.class);
     }
 
     private Object getLayouts()
     {
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         return universalJcrDao.getAllByType(TemplateLayout.class);
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
     private Template determineTemplate(BaseNode resourceObject)
     {
         Template template = null;
 
         // FIXME This needs to be more generic -- or at least tied to SitePage
         if (resourceObject.hasProperty("data.publishingOptions") && resourceObject.getPropertyValue("data.publishingOptions") != null)
         {
             template = (Template) resourceObject.getPropertyValue("data.publishingOptions.template");
             if (template != null)
                 return template;
         }
 
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
 
         String templateCode = "";
         if (resourceObject.getTypeDef().getName().contains("."))
             templateCode = StringUtils.substringAfterLast(resourceObject.getTypeDef().getName(), ".").toLowerCase();
         else
             templateCode = resourceObject.getTypeDef().getName().toLowerCase();
 
         template = (Template) universalJcrDao.getByPath("/designer/templates/" + templateCode);
         return template;
     }
 }
