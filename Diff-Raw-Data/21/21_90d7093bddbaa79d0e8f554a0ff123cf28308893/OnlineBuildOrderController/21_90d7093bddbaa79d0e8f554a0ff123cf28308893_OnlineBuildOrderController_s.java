 package com.ALC.SC2BOAserver.web;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.ALC.SC2BOAserver.dao.SC2BOADAO;
 import com.ALC.SC2BOAserver.entities.OnlineBuildOrder;
 import com.ALC.SC2BOAserver.entities.BuildOrderCollection;
 import com.ALC.SC2BOAserver.util.DEBUG;
 
 
 @Controller
 @RequestMapping(value = "/buildorder")
 public class OnlineBuildOrderController {
 	private SC2BOADAO dao;
 	
 	@RequestMapping(value="/getall", method=RequestMethod.GET)
 	public BuildOrderCollection getAllBuildOrders(){
 		DEBUG.d("getAllBuildOrders called");
 		BuildOrderCollection collection = new BuildOrderCollection();
 		collection.setBuilds(dao.getAllOnlineBuildOrders());
 		return collection;
 	}
 	
 	@RequestMapping(value="/getallarray", method=RequestMethod.GET)
 	public OnlineBuildOrder[] getAllBuildOrdersArray(){
 		DEBUG.d("getAllBuildOrdersArray called");
 		List<OnlineBuildOrder> list = dao.getAllOnlineBuildOrders();
 		OnlineBuildOrder[] array = new OnlineBuildOrder[list.size()];
 		array = dao.getAllOnlineBuildOrders().toArray(array);
 		return array;
 	}
 	
 	@RequestMapping(value="/getlist", method=RequestMethod.GET)
 	public List<OnlineBuildOrder> getAllBuildOrdersList(){
 		DEBUG.d("getAllBuildOrdersList called");
 		return  dao.getAllOnlineBuildOrders();
 		
 	}
 	
     
 	@RequestMapping(value="/get/{buildid}", method=RequestMethod.GET)
 	public OnlineBuildOrder getBuildOrder(@PathVariable String buildid) {
 		DEBUG.d("getbuildorder called");
 		return dao.getOnlineBuildOrder(buildid);
 		//TODO check this
 	}
 	@RequestMapping(value="/searchbyname/{name}", method=RequestMethod.GET)
 	public List<OnlineBuildOrder> searchByName(@PathVariable String name) {
 		DEBUG.d("searchbyname called: "+name);
 		return dao.searchOnlineBuildOrderByName(name);
 		//TODO check this
 	}
 	
     
 	@RequestMapping(value="/delete/{buildid}", method=RequestMethod.GET)
     //@Secured("ROLE_ADMIN")
     public void deleteOnlineBuildOrder (@PathVariable String buildid) {
 		DEBUG.d("deletebuildorder called");
 		dao.deleteOnlineBuildOrder(buildid);
        //TODO check this
     }
     
     @RequestMapping(value="/add", method=RequestMethod.GET)
     //@Secured("ROLE_ADMIN")
     public void addOnlineBuildOrder (@RequestParam("buildname") String buildname,
     		@RequestParam("buildinstructions") String buildinstructions,
     		@RequestParam("race") String race,
     		ModelMap map) {
         OnlineBuildOrder buildorder = new OnlineBuildOrder();
         buildorder.setBuildName(buildname);
         buildorder.setBuildOrderInstructions(buildinstructions);
         buildorder.setRace(race);
         
         dao.addOnlineBuildOrder(buildorder);
         //TODO check this
     }
     
     @RequestMapping(value="/update/{buildid}", method=RequestMethod.GET)
     public void updateOnlineBuildOrder(@PathVariable String buildId,
     		@RequestParam("buildname") String buildName,
     		@RequestParam("buildinstructions") String buildinstructions,
     		@RequestParam("race") String race){
     	OnlineBuildOrder buildorder = dao.getOnlineBuildOrder(buildId);
     	if(buildName!=null&&buildName.equalsIgnoreCase(""))buildorder.setBuildName(buildName);
     	if(buildinstructions!=null&&buildinstructions.equalsIgnoreCase(""))buildorder.setBuildOrderInstructions(buildinstructions);
     	if(race!=null&&race.equalsIgnoreCase(""))buildorder.setRace(race);
     	//TODO finish this
     	//TODO check this
     }
 	
 	@Autowired
     public void setSC2BOADAO (SC2BOADAO dao) {
         this.dao = dao;
     }
 	
 	
 
 }
