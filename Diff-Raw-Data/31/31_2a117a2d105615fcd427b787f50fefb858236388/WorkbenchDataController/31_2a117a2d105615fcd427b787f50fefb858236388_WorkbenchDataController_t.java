 package org.otherobjects.cms.controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.otherobjects.cms.dao.DynaNodeDao;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 public class WorkbenchDataController implements Controller
 {
    private DynaNodeDao dynaNodeDao;

     @SuppressWarnings("unchecked")
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         String path = request.getPathInfo();
         path = path.substring(6);

         ModelAndView view = new ModelAndView("jsonView");
         List nodes = new ArrayList();

         Map n1 = new HashMap();
        n1.put("id", "1");
        n1.put("text", "Node");
        n1.put("leaf", "false");

         nodes.add(n1);
         nodes.add(n1);

         view.addObject("data", nodes);
         return view;
     }
 }
