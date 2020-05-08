 package com.zenred.cosmos.controller.json;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import com.zenred.data_access.MarshallClustersAndStarsAndPlanetsInOneSystem;
 import com.zenred.data_access.MarshallSystems;
 import com.zenred.service.GenerateOneSystem;
 import com.zenred.service.MarshalSystemDetails;
 import com.zenred.visualization.BasicMessageResponse;
 import com.zenred.visualization.ClusterResponse;
 import com.zenred.visualization.StarsResponse;
 import com.zenred.visualization.SystemPlusSomeDetails;
 import com.zenred.visualization.SystemSimpleArray;
 
 import cosmos.hibernate.ClusterRep;
 import cosmos.hibernate.StarRep;
 import cosmos.hibernate.SystemRep;
 
 public class CreateSystemController2 implements Controller {
 	
 	private GenerateOneSystem generateOneSystem;
 	public void setGenerateOneSystem(GenerateOneSystem generateOneSystem) {
 		this.generateOneSystem = generateOneSystem;
 	}
 
 	public void setMarshallClustersAndStarsAndPlanetsInOneSystem(
 			MarshallClustersAndStarsAndPlanetsInOneSystem marshallClustersAndStarsAndPlanetsInOneSystem) {
 	}
 
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		String udim = request.getParameter("udim");
 		String vdim = request.getParameter("vdim");
 		boolean result = generateOneSystem.doesSystemAllReadyExist(udim, vdim);
 		SystemPlusSomeDetails systemPlusSomeDetails = null;
 
 		if(result){
			systemPlusSomeDetails = new SystemPlusSomeDetails();
 			systemPlusSomeDetails.setTheMessage(udim+":"+vdim+" already exists");
 		}
 		else{
 			generateOneSystem.generateSystem(udim, vdim);
 			SystemRep systemsRep = new MarshallSystems().getOneSystemRep(udim, vdim);
 			List<SystemRep> systemsRepList = new ArrayList<SystemRep>();
 			systemsRepList.add(systemsRep);
 			systemPlusSomeDetails = new MarshalSystemDetails().addClustersAndStars(systemsRepList).get(0);
 			systemPlusSomeDetails.setTheMessage(udim+":"+vdim+" generated");
 
 		}
 		ModelAndView modelAndView = new ModelAndView(new SystemDetailView());
 		
 		modelAndView.addObject(SystemDetailView.JSON_ROOT,SystemSimpleArray.genSimpleArray(systemPlusSomeDetails));
 		return modelAndView;
 	}
 
 }
