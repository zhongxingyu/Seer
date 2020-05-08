 package org.otherobjects.cms.controllers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.otherobjects.cms.controllers.renderers.ResourceRenderer;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.model.SiteFolder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 /**
  * Site controller handles all site page requests. For custom application functionality
  * you will need to create and map your own controllers.
  * 
  * @author rich
  */
 public class SiteController extends AbstractController
 {
     private final Logger logger = LoggerFactory.getLogger(SiteController.class);
 
     private DaoService daoService;
 
     private Map<String, ResourceRenderer> renderers = new HashMap<String, ResourceRenderer>();
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
 
         // Make sure folders end with slash
         String path = request.getPathInfo();
 
         if (StringUtils.isEmpty(path))
             path = "/";
         else if (path.length() > 1 && !path.contains(".") && !path.endsWith("/"))
             path = path + "/";
 
         this.logger.info("Requested resource: {}", path);
 
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         BaseNode resourceObject = null;
         if (path.contains("."))
         {
             // Page requested
             resourceObject = universalJcrDao.getByPath("/site" + path);
         }
         else
         {
             // Folder requested. If this folder has it's own template then
             // render that.
             resourceObject = universalJcrDao.getByPath("/site" + path);
 
             if (resourceObject instanceof SiteFolder)
             {
 //                resourceObject = null;
 //                // Folder requested. Get first item that isn't a folder.
 //                List<BaseNode> contents = universalJcrDao.getAllByPath("/site" + path);
 //
 //                for (BaseNode n : contents)
 //                {
 //                    // FIXME Need better way of dealing with components
 //                    if (!n.isFolder() && !(n instanceof PublishingOptions))
 //                    {
 //                        resourceObject = n;
 //                        break;
 //                    }
 //                }
                 SiteFolder folder = (SiteFolder) resourceObject;
                 String defaultPage = StringUtils.isNotBlank(folder.getDefaultPage()) ? folder.getDefaultPage() : "index.html";
                 resourceObject = universalJcrDao.getByPath(folder.getJcrPath() + "/" + defaultPage);
                 Assert.notNull(resourceObject, "No resources in this folder.");
             }
         }
 
         // Handle page not found
         if (resourceObject == null)
         {
             // Determine folder
             String newCode = StringUtils.substringAfterLast(path, "/");
             path = StringUtils.substringBeforeLast(path, "/");
             Object folder = universalJcrDao.getByPath("/site" + path);
 
             //FIXME Add Security check here
            ModelAndView mv = new ModelAndView("/otherobjects/templates/pages/oo-404-create");
             mv.addObject("requestedPath", path);
             mv.addObject("folder", folder);
             mv.addObject("newCode", newCode);
             return mv;
             
             
             //throw new NotFoundException("No resource at: " + path);
         }
 
         // Pass control to renderer
         ResourceRenderer handler = renderers.get(resourceObject.getClass().getName());
         if (handler == null)
             handler = renderers.get("*");
         return handler.handleRequest(resourceObject, request, response);
     }
 
     public void setRenderers(Map<String, ResourceRenderer> renderers)
     {
         this.renderers = renderers;
     }
 
     public void setDaoService(DaoService daoService)
     {
         this.daoService = daoService;
     }
 }
