 package org.otherobjects.cms.controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.otherobjects.cms.dao.DynaNodeDao;
 import org.otherobjects.cms.model.DynaNode;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.views.JsonView;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 /**
  * Controller providing a REST style interface to site data. Currently supports navigator structure and
  * container contents. Data is served in JSON format.
  * 
  * <p>/navigator?node= (sends sub-contaniners of specified container)
  * <br>/listing?node= (sends items in specified container)
  * <br/>/type/typeName (sends typeDef info)
  * 
  * @author rich
  */
 public class WorkbenchDataController implements Controller
 {
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     private DynaNodeDao dynaNodeDao;
     private TypeService typeService;
 
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         String path = request.getPathInfo();
         path = path.substring(6);
 
         if (path.contains("/types/"))
             return generateTypeData(request);
         else if (path.contains("/item/"))
             return generateItemData(request);
         else if (path.endsWith("/listing"))
             return generateListingData(request);
         else if (path.contains("/select"))
             return generateSelectData(request);
         else if (path.endsWith("/navigator"))
             return generateNavigatorData(request);
         else
             return null;
     }
 
     private ModelAndView generateItemData(HttpServletRequest request)
     {
         String path = request.getPathInfo();
         String id = path.substring(path.lastIndexOf("/") + 1);
 
         logger.info("Sending item data: {} ", id);
 
         DynaNode dynaNode = dynaNodeDao.get(id);
         Assert.notNull(dynaNode, "No item found: " + id);
 
         ModelAndView view = new ModelAndView("jsonView");
        view.addObject(JsonView.JSON_DATA_KEY, dynaNode);
        view.addObject(JsonView.JSON_DEEP_SERIALIZE, true);
         return view;
     }
 
     private ModelAndView generateSelectData(HttpServletRequest request)
     {
         String path = request.getPathInfo();
         String typeName = path.substring(path.lastIndexOf("/") + 1);
         
         logger.info("Sending select data of type: {} ", typeName);
         
         List<DynaNode> allByType = dynaNodeDao.getAllByType(typeName);
         
         ModelAndView view = new ModelAndView("jsonView");
         view.addObject(JsonView.JSON_DATA_KEY, allByType);
         view.addObject(JsonView.JSON_DEEP_SERIALIZE, false);
         return view;
     }
     
     private ModelAndView generateTypeData(HttpServletRequest request)
     {
         String path = request.getPathInfo();
         String typeName = path.substring(path.lastIndexOf("/") + 1);
 
         logger.info("Sending type definition: {} ", typeName);
 
         TypeDef type = typeService.getType(typeName);
         Assert.notNull(type, "No type found: " + typeName);
 
         ModelAndView view = new ModelAndView("jsonView");
         view.addObject(JsonView.JSON_DATA_KEY, type);
 //        view.addObject(JsonView.JSON_INCLUDES_KEY, "properties");
         view.addObject(JsonView.JSON_DEEP_SERIALIZE, true);
         return view;
     }
 
     private ModelAndView generateNavigatorData(HttpServletRequest request)
     {
         ModelAndView view = new ModelAndView("jsonView");
 
         String nodeId = request.getParameter("node");
         String jcrPath = "/";
         if (nodeId != null && nodeId.length() > 10)
         {
             DynaNode node = dynaNodeDao.get(nodeId);
             jcrPath = node.getJcrPath();
         }
 
         // FIXME M2 Use NavigatorService
         List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
         List<DynaNode> contents = dynaNodeDao.getAllByPath(jcrPath);
 
         for (DynaNode dynaNode : contents)
         {
             if (dynaNode.getOoType().equals("Folder"))
             {
                 Map<String, Object> n1 = new HashMap<String, Object>();
                 n1.put("id", dynaNode.getId());
                 n1.put("code", dynaNode.getCode());
 
                 // Localise labels
                 String label = dynaNode.getLabel();
                 // TODO This has moved to JsonView -- may move back!
 //                if (label.startsWith("$"))
 //                {
 //                    label = label.substring(2, label.length() -1);
 //                    label = requestContext.getMessage(label);
 //                }
                 n1.put("text", label);
                 
                 if (dynaNode.get("cssClass") != null)
                 {
                     n1.put("cls", dynaNode.get("cssClass") + "-nav-item");
                     n1.put("allowDrag", false);
 
                 }
                 else
                     n1.put("cls", "folder");
                 n1.put("leaf", false);
                 if (dynaNode.getCode().equals("site"))
                     n1.put("expanded", true);
 
                 nodes.add(n1);
             }
         }
         view.addObject(JsonView.JSON_DATA_KEY, nodes);
         return view;
     }
 
     private ModelAndView generateListingData(HttpServletRequest request)
     {
         String jcrPath = "/site";
 
         ModelAndView view = new ModelAndView("jsonView");
 
         String nodeId = request.getParameter("node");
         if (nodeId != null && nodeId.length() > 10)
         {
             DynaNode node = dynaNodeDao.get(nodeId);
             jcrPath = node.getJcrPath();
         }
 
         // FIXME M2 Can we exclude folders in the query?
         List<DynaNode> contents = dynaNodeDao.getAllByPath(jcrPath);
 
         List<DynaNode> nonFolders = new ArrayList<DynaNode>();
         for (DynaNode dynaNode : contents)
         {
             if (!dynaNode.getOoType().equals("Folder"))
                 nonFolders.add(dynaNode);
         }
         view.addObject(JsonView.JSON_DATA_KEY, nonFolders);
         view.addObject(JsonView.JSON_INCLUDES_KEY, new String[]{"data"});
         return view;
     }
 
     public void setDynaNodeDao(DynaNodeDao dynaNodeDao)
     {
         this.dynaNodeDao = dynaNodeDao;
     }
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 }
