 package org.otherobjects.cms.controllers;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springmodules.jcr.JcrTemplate;
 
 /**
  * Controller that serves up block content. Used for serving up content for AJAX.
  * 
  * FIXME Needs merging with SiteController
  * FIXME Needs to respect block security
  * 
  * @author rich
  */
 @Controller
 public class BlockController extends MultiActionController
 {
     @Resource
    private JcrTemplate jcrTemplate;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private OtherObjectsConfigurator otherObjectsConfigurator;
    
    @Resource
     private DaoService daoService;
 
     @RequestMapping("/block/*")
     public ModelAndView get(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         UniversalJcrDao dao = (UniversalJcrDao) daoService.getDao("baseNode");
         BaseNode blockData = dao.getByPath("/blocks/promo");
 
         // Return page and context
         ModelAndView view = new ModelAndView("blocks/promo");
 
         //view.addObject("template", template);
         view.addObject("data", blockData);
         view.addObject("daoService", this.daoService);
 
         return view;
     }
     
     @RequestMapping("/block/*")
     public ModelAndView form(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         UniversalJcrDao dao = (UniversalJcrDao) daoService.getDao("baseNode");
         BaseNode blockData = dao.getByPath("/blocks/promo");
 
         // Return page and context
         ModelAndView view = new ModelAndView("blocks/oo-form");
 
         //view.addObject("template", template);
         view.addObject("data", blockData);
         view.addObject("daoService", this.daoService);
 
         return view;
     }
 }
