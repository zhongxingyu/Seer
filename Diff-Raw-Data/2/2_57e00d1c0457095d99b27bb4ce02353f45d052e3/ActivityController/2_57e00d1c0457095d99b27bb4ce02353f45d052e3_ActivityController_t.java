 package com.bouncingdata.plfdemo.controller;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.request.WebRequest;
 
 import com.bouncingdata.plfdemo.datastore.pojo.model.Analysis;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Dataset;
 import com.bouncingdata.plfdemo.datastore.pojo.model.RepresentClass;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Tag;
 import com.bouncingdata.plfdemo.datastore.pojo.model.User;
 import com.bouncingdata.plfdemo.datastore.pojo.model.UserActionLog;
 import com.bouncingdata.plfdemo.service.DatastoreService;
 import com.bouncingdata.plfdemo.util.Utils;
 
 @Controller
 public class ActivityController {
   
   private Logger logger = LoggerFactory.getLogger(ActivityController.class);
   
   @Autowired
   private DatastoreService datastoreService;
   
   @RequestMapping(value={"/stream"}, method=RequestMethod.GET)
   public String getDefaultStream(WebRequest request, 
 			   ModelMap model, 
 			   Principal principal, HttpSession session) {
 	  
 	// vinhpq : remove temp object upload 
     if(session.getAttribute("varUp") != null)  
     	session.removeAttribute("varUp");
     
 	return "redirect:stream/a/all/recent";
   }
   
   @RequestMapping(value={"/stream/{page}/{filter}/{type}"}, method=RequestMethod.GET)
   public String getActivityStream(@PathVariable String page ,
 		  						   @PathVariable String filter ,
 		  						   @PathVariable String type ,
 		  						   WebRequest request, 
 		  						   ModelMap model, 
 		  						   Principal principal,
 		  						   HttpSession session) {
     try {    
       /*String filter = request.getParameter("filter");
       if (StringUtils.isEmpty(filter)) filter = "all";
       
       if (!Arrays.asList(new String[] {"all", "analysis", "dataset", "recent", "popular"}).contains(filter)) {
         return "error";
       }*/
       
       User user = (User) ((Authentication)principal).getPrincipal();
       boolean isOrder = true;
       int startPoint = 0;
       
       try {
         ObjectMapper logmapper = new ObjectMapper();
         String data = logmapper.writeValueAsString(new String[] {"0"});		   	 
         datastoreService.logUserAction(user.getId(),UserActionLog.ActionCode.GET_ACTIVITY_STREAM,data);
       } catch (Exception e) {
         logger.debug("Failed to log action", e);
       }
       
      /* List<Activity> activities = datastoreService.getRecentFeed(user.getId());
       model.addAttribute("activities", activities);*/
       List<Analysis> allAnalyses = new ArrayList<Analysis>();
       List<Dataset> allDatasets = new ArrayList<Dataset>();
       
       page = ((page==null||page.trim().equals(""))? "a" : page );
       type = ((type==null||type.trim().equals(""))? "recent" : type );
       filter = ((filter==null||filter.trim().equals(""))? "all" : filter );
       
       //page All
       if(page.equals("a")){
       
 	      // type order (recent/popular)
 	      if(type.equals("recent")){
 	    	  
 	    	  // type filter (all/analysis/dataset/scraper)
 	    	  if(filter.equals("all")){
 	    	      allAnalyses = datastoreService.getAnalysesIn1Month(0,10);
 	    	      allDatasets = datastoreService.getDatasetsIn1Month(0,10);    		  
 	    	  }
 	    	  
 	    	  else if(filter.equals("analysis")){
 	    		  allAnalyses = datastoreService.getAnalysesIn1Month(0,20);
 	    	  }
 	    	  
 	    	  else if(filter.equals("dataset")){
 	    		  allDatasets = datastoreService.getDatasetsIn1Month(0,20);
 	    	  }
 
 	    	  else if(filter.equals("scraper")){
 	    		  
 	    	  }
 	      } else if(type.equals("popular")){
 	    	  isOrder = false;
 	    	  // type filter (all/analysis/dataset/scraper)
 	    	  if(filter.equals("all")){
 	    		  allAnalyses = datastoreService.getPopularAnalysesIn1Month(0,10);
 	        	  allDatasets = datastoreService.getPopularDatasetsIn1Month(0,10);	  
 	    	  }
 	    	  
 	    	  else if(filter.equals("analysis")){
 	    		  allAnalyses = datastoreService.getPopularAnalysesIn1Month(0,20);
 	    	  }
 	    	  
 	    	  else if(filter.equals("dataset")){
 	    		  allDatasets = datastoreService.getPopularDatasetsIn1Month(0,20);	
 	    	  }
 	    	  
 	    	  else if(filter.equals("scraper")){
 	    	  }
 	      }
       }
       
       //page streambyself
       if(page.equals("streambyself")){
       
 	      // type order (recent/popular)
     	  if(type.equals("recent")){
         	  
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.getAllAnalysesBySelf(user.getId(), 0, 10);
         		  allDatasets = datastoreService.getAllDatasetsBySelf(user.getId(), 0, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.getAllAnalysesBySelf(user.getId(), 0, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.getAllDatasetsBySelf(user.getId(), 0, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         		  
         	  }
           } else if(type.equals("popular")){
         	  isOrder = false;
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.getPopularAnalysesBySelf(user.getId(),0 , 10);
         		  allDatasets = datastoreService.getPopularDatasetsBySelf(user.getId(),0 , 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.getPopularAnalysesBySelf(user.getId(), 0, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.getPopularDatasetsBySelf(user.getId(), 0, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         	  }
           }
       }
       
       //page staffpicks
       if(page.equals("staffpicks")){
       
 	      // type order (recent/popular)
     	  if(type.equals("recent")){
         	  
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.getRecentAnalysisStaffPick(0, 10);
         		  allDatasets = datastoreService.getRecentDatasetsStaffPick(0, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.getRecentAnalysisStaffPick(0, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.getRecentDatasetsStaffPick(0, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         		  
         	  }
           } else if(type.equals("popular")){
         	  isOrder = false;
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.getPopularAnalysesStaffPick(0, 10);
             	  allDatasets = datastoreService.getPopularDatasetsStaffPick(0, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.getPopularAnalysesStaffPick(0, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.getPopularDatasetsStaffPick(0, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         	  }
           }
       }
       
       //page popularAuthors
       if(page.equals("popularAuthors")){
       
 	      // type order (recent/popular)
     	  if(type.equals("recent")){
         	  
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.get20AuthorAnalysesRecent(0, 10);
         		  allDatasets = datastoreService.get20AuthorDataSetRecent(0, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.get20AuthorAnalysesRecent(0, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.get20AuthorDataSetRecent(0, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         		  
         	  }
           } else if(type.equals("popular")){
         	  isOrder = false;
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
             	  allAnalyses = datastoreService.get20AuthorAnalysesItemPopular(0, 10);
             	  allDatasets = datastoreService.get20AuthorDataSetItemPopular(0, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.get20AuthorAnalysesItemPopular(0, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.get20AuthorDataSetItemPopular(0, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         	  }
           }
       }
       
       // set number items for paging
       if(filter.equals("all"))
     	  startPoint = 10;
       else if(filter.equals("analysis") || filter.equals("dataset") || filter.equals("scraper"))
     	  startPoint = 20;
       
       session.setAttribute("startpoint", startPoint);
       
       // merge data 2 class Analysis and Dataset 
       List<RepresentClass> lstRepresentClass = Utils.mergeData2Class(allAnalyses, allDatasets, isOrder);
       model.addAttribute("recentAnalyses", lstRepresentClass);
       
       List<Analysis> mostPopularAnalyses = datastoreService.getMostPopularAnalyses();
       model.addAttribute("topAnalyses", mostPopularAnalyses);
       
       List<Dataset> mostPopularDatasets = datastoreService.getMostPopularDatasets();
       model.addAttribute("topDatasets", mostPopularDatasets);
       
      session.setAttribute("pageId", page);
       model.addAttribute("fm", filter);
       model.addAttribute("tp", type);
       
     } catch (Exception e) {
       logger.debug("Failed to load activity stream", e);
       model.addAttribute("errorMsg", "Failed to load the activity stream");
     }
     return "stream";
   }
   
   @RequestMapping(value="/a/more/{page}/{filter}/{type}", method=RequestMethod.GET)
   public @ResponseBody List<RepresentClass> getMoreActivities(@PathVariable String page ,
 													     @PathVariable String filter ,
 													     @PathVariable String type ,
 													     ModelMap model, 
 													     Principal principal,
 													     HttpSession session) {
     try {
       User user = (User) ((Authentication)principal).getPrincipal();
       try{
 	      ObjectMapper logmapper = new ObjectMapper();
 	      String data = logmapper.writeValueAsString(new String[] {"0"});				   	 
 	      datastoreService.logUserAction(user.getId(),UserActionLog.ActionCode.GET_MORE_ACTIVITY,data);
       }catch (Exception e) {
           logger.debug("Failed to log action", e);
       }
       
       int startPoint = 0;
       boolean isOrder = true;
       
       if(session.getAttribute("startpoint")!=null)
     	  startPoint = Integer.parseInt((session.getAttribute("startpoint").toString()));
       
       List<Analysis> allAnalyses = new ArrayList<Analysis>();
       List<Dataset> allDatasets = new ArrayList<Dataset>();
       
       page = ((page==null||page.trim().equals(""))? "a" : page );
       type = ((type==null||type.trim().equals(""))? "recent" : type );
       filter = ((filter==null||filter.trim().equals(""))? "all" : filter );
       
       //page All
       if(page.equals("a")){
       
 	      // type order (recent/popular)
 	      if(type.equals("recent")){
 	    	  
 	    	  // type filter (all/analysis/dataset/scraper)
 	    	  if(filter.equals("all")){
 	    	      allAnalyses = datastoreService.getAnalysesIn1Month(startPoint,10);
 	    	      allDatasets = datastoreService.getDatasetsIn1Month(startPoint,10);    		  
 	    	  }
 	    	  
 	    	  else if(filter.equals("analysis")){
 	    		  allAnalyses = datastoreService.getAnalysesIn1Month(startPoint,20);
 	    	  }
 	    	  
 	    	  else if(filter.equals("dataset")){
 	    		  allDatasets = datastoreService.getDatasetsIn1Month(startPoint,20);
 	    	  }
 
 	    	  else if(filter.equals("scraper")){
 	    		  
 	    	  }
 	      } else if(type.equals("popular")){
 	    	  isOrder = false;
 	    	  // type filter (all/analysis/dataset/scraper)
 	    	  if(filter.equals("all")){
 	    		  allAnalyses = datastoreService.getPopularAnalysesIn1Month(startPoint,10);
 	        	  allDatasets = datastoreService.getPopularDatasetsIn1Month(startPoint,10);	  
 	    	  }
 	    	  
 	    	  else if(filter.equals("analysis")){
 	    		  allAnalyses = datastoreService.getPopularAnalysesIn1Month(startPoint,20);
 	    	  }
 	    	  
 	    	  else if(filter.equals("dataset")){
 	    		  allDatasets = datastoreService.getPopularDatasetsIn1Month(startPoint,20);	
 	    	  }
 	    	  
 	    	  else if(filter.equals("scraper")){
 	    	  }
 	      }
       }
       //page streambyself
       if(page.equals("streambyself")){
       
 	      // type order (recent/popular)
     	  if(type.equals("recent")){
         	  
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.getAllAnalysesBySelf(user.getId(), startPoint, 10);
         		  allDatasets = datastoreService.getAllDatasetsBySelf(user.getId(), startPoint, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.getAllAnalysesBySelf(user.getId(), startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.getAllDatasetsBySelf(user.getId(), startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         		  
         	  }
           } else if(type.equals("popular")){
         	  isOrder = false;
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.getPopularAnalysesBySelf(user.getId(),startPoint , 10);
         		  allDatasets = datastoreService.getPopularDatasetsBySelf(user.getId(),startPoint , 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.getPopularAnalysesBySelf(user.getId(), startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.getPopularDatasetsBySelf(user.getId(), startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         	  }
           }
       }
       
       //page staffpicks
       if(page.equals("staffpicks")){
       
 	      // type order (recent/popular)
     	  if(type.equals("recent")){
         	  
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.getRecentAnalysisStaffPick(startPoint, 10);
         		  allDatasets = datastoreService.getRecentDatasetsStaffPick(startPoint, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.getRecentAnalysisStaffPick(startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.getRecentDatasetsStaffPick(startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         		  
         	  }
           } else if(type.equals("popular")){
         	  isOrder = false;
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.getPopularAnalysesStaffPick(startPoint, 10);
             	  allDatasets = datastoreService.getPopularDatasetsStaffPick(startPoint, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.getPopularAnalysesStaffPick(startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.getPopularDatasetsStaffPick(startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         	  }
           }
       }
       
       //page popularAuthors
       if(page.equals("popularAuthors")){
       
 	      // type order (recent/popular)
     	  if(type.equals("recent")){
         	  
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
         		  allAnalyses = datastoreService.get20AuthorAnalysesRecent(startPoint, 10);
         		  allDatasets = datastoreService.get20AuthorDataSetRecent(startPoint, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.get20AuthorAnalysesRecent(startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.get20AuthorDataSetRecent(startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         		  
         	  }
           } else if(type.equals("popular")){
         	  isOrder = false;
         	  // type filter (all/analysis/dataset/scraper)
         	  if(filter.equals("all")){
             	  allAnalyses = datastoreService.get20AuthorAnalysesItemPopular(startPoint, 10);
             	  allDatasets = datastoreService.get20AuthorDataSetItemPopular(startPoint, 10);
         	  }
         	  
         	  else if(filter.equals("analysis")){
         		  allAnalyses = datastoreService.get20AuthorAnalysesItemPopular(startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("dataset")){
         		  allDatasets = datastoreService.get20AuthorDataSetItemPopular(startPoint, 20);
         	  }
         	  
         	  else if(filter.equals("scraper")){
         	  }
           }
       }
       
       // set number items for paging
       if(filter.equals("all"))
     	  startPoint = startPoint + 10;
       else if(filter.equals("analysis") || filter.equals("dataset") || filter.equals("scraper"))
     	  startPoint = startPoint + 20;
       
       session.setAttribute("startpoint", startPoint);
       
       // merge data 2 class Analysis and Dataset 
       List<RepresentClass> lstRepresentClass = Utils.mergeData2Class(allAnalyses, allDatasets, isOrder);
       return (lstRepresentClass);
       
     } catch (Exception e) {
       logger.debug("Failed to load more activity", e);
       return null;
     }
   }
 
   @RequestMapping(value={"/tags"}, method=RequestMethod.GET)
   public String get10TopTags(WebRequest request, ModelMap model, Principal principal) {
 	  
 	  try {
 		  User user = (User) ((Authentication)principal).getPrincipal();
 	      
 	      try {
 	        ObjectMapper logmapper = new ObjectMapper();
 	        String data = logmapper.writeValueAsString(new String[] {"0"});		   	 
 	        datastoreService.logUserAction(user.getId(),UserActionLog.ActionCode.GET_ACTIVITY_STREAM,data);
 	      } catch (Exception e) {
 	        logger.debug("Failed to log action", e);
 	      }
 		  
 	      List<Tag> top10Tags = datastoreService.get10Tags();
 	      model.addAttribute("_tags", top10Tags);
 	      
 	      //---
 	      List<Analysis> mostPopularAnalyses = datastoreService.getMostPopularAnalyses();
 	      model.addAttribute("topAnalyses", mostPopularAnalyses);
 	      
 	      List<Dataset> mostPopularDatasets = datastoreService.getMostPopularDatasets();
 	      model.addAttribute("topDatasets", mostPopularDatasets);
 	      
 	      model.addAttribute("menuId", "tags");
 	  } catch (Exception e) {
 	      logger.debug("Failed to load activity stream", e);
 	      model.addAttribute("errorMsg", "Failed to load the activity stream");
       
 	  }
 	  return "tags";
   }
 }
