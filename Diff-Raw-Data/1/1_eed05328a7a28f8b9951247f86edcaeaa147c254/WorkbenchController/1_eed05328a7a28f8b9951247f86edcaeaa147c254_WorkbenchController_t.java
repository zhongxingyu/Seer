 package org.otherobjects.cms.controllers;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.otherobjects.cms.Url;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.model.Folder;
 import org.otherobjects.cms.model.FolderDao;
 import org.otherobjects.cms.model.SiteFolder;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.util.ActionUtils;
 import org.otherobjects.cms.util.RequestUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class WorkbenchController
 {
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     @Resource
     private TypeService typeService;
 
     @Resource
     private DaoService daoService;
 
     //    @Resource
     //    private LocaleResolver localeResolver;
 
     @RequestMapping({"", "/", "/workbench/*"})
     public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         //        String newLocale = ServletRequestUtils.getStringParameter(request, "locale");
         //        if (newLocale != null)
         //        {
         //            this.localeResolver.setLocale(request, response, StringUtils.parseLocaleString(newLocale));
         //            this.logger.info("Locale set to: " + this.localeResolver.resolveLocale(request));
         //        }
 
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/overview");
         return mav;
 
         /*
         String path = request.getPathInfo();
 
         if (path.length() < 10)
             path = "/otherobjects/workbench/workbench";
         else
         {
             path = path.substring(10);
             if (path.equals("/"))
                 path = "/otherobjects/workbench/workbench";
             else
                 path = "/otherobjects/workbench/" + path;
         }
         path = path.replaceAll(".html", "");
         this.logger.info("WorkbenchController: " + path);
 
         Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
         ModelAndView view = new ModelAndView(path);
         view.addObject("user", principal);
         view.addObject("request", request);
         view.addObject("cmsImageTool", new CmsImageTool());
         return view;
         */
     }
 
     @RequestMapping({"/workbench/view/*"})
     public ModelAndView view(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String id = RequestUtils.getId(request);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/view");
         mav.addObject("id", id);
         return mav;
     }
 
     @RequestMapping({"/workbench/edit/*"})
     public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String id = RequestUtils.getId(request);
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/edit");
         BaseNode item = universalJcrDao.get(id);
         mav.addObject("id", id);
         mav.addObject("object", item);
         mav.addObject("typeDef", item.getTypeDef());
         return mav;
     }
 
     @RequestMapping({"/workbench/create/*"})
     public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String type = RequestUtils.getId(request);
         TypeDef typeDef = typeService.getType(type);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/edit");
 
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         BaseNode create = universalJcrDao.create(type);
         if(request.getParameter("code")!=null)
             create.setCode(request.getParameter("code"));
         mav.addObject("object", create);
         mav.addObject("containerId", request.getParameter("container"));
         mav.addObject("typeDef", typeDef);
         return mav;
     }
 
     @RequestMapping({"/workbench/list/*"})
     public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String id = RequestUtils.getId(request);
         FolderDao folderDao = (FolderDao) this.daoService.getDao(Folder.class);
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/list");
         mav.addObject("id", id);
         SiteFolder folder = folderDao.get(id);
         mav.addObject("folder", folder);
         mav.addObject("items", universalJcrDao.getAllByPath(folder.getJcrPath()));
         
         return mav;
     }
     
     @RequestMapping({"/workbench/search*"})
     public ModelAndView search(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         // Search JCR
         String q = request.getParameter("q");
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         List<BaseNode> results = universalJcrDao.getAllByJcrExpression("/jcr:root/site//(*) [jcr:contains(data/., '" + q + "')]");        
 
         // Search DB
         ModelAndView mav = new ModelAndView("/otherobjects/templates/legacy/pages/search");
         mav.addObject("results", results);
         return mav;
     }
 
     @RequestMapping({"/workbench/history/*"})
     public ModelAndView history(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         return null;
     }
     
     /* Actions */
 
     @RequestMapping({"/workbench/publish/*"})
     public ModelAndView publish(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         // Publish
         String id = RequestUtils.getId(request);
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         BaseNode item = universalJcrDao.get(id);
         universalJcrDao.publish(item, null);
 
         // FIXME - Catch errors
         
         // Response
         ActionUtils actionUtils = new ActionUtils(request, response, null, null);
         actionUtils.flashInfo("Your object was published.");
         Url u = new Url("/otherobjects/workbench/view/" + item.getId());
         response.sendRedirect(u.toString());
         return null;
     }
 
     @RequestMapping({"/workbench/unpublish/*"})
     public ModelAndView unpublish(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         return null;
     }
 
     @RequestMapping({"/workbench/delete/*"})
     public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         // FIXME Need to publish this delete too.
         String id = RequestUtils.getId(request);
         FolderDao folderDao = (FolderDao) this.daoService.getDao(Folder.class);
         UniversalJcrDao universalJcrDao = (UniversalJcrDao) this.daoService.getDao(BaseNode.class);
         
         BaseNode item = universalJcrDao.get(id);
         universalJcrDao.remove(id);
         
         ActionUtils actionUtils = new ActionUtils(request, response, null, null);
         actionUtils.flashInfo("Your object was deleted.");
         
         SiteFolder folder = folderDao.getByPath(item.getPath());
         Url u = new Url("/otherobjects/workbench/list/" + folder.getId());
         response.sendRedirect(u.toString());
         return null;
     }
 
     @RequestMapping({"/workbench/revert/*"})
     public ModelAndView revert(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         return null;
     }
 
     @RequestMapping({"/workbench/reorder/*"})
     public ModelAndView reorder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         return null;
     }
 
 }
