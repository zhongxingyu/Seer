 package com.zenred.cosmos.controller.json;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.zenred.data_access.MarshallSystems;
 import com.zenred.service.GenerateOneSystem;
 import com.zenred.service.MarshalSystemDetails;
 import com.zenred.visualization.EmptySystem;
 import com.zenred.visualization.SystemPlusSomeDetails;
 import com.zenred.visualization.SystemSimpleArray;
 
 import cosmos.hibernate.SystemRep;
 
 public class PageSystemController implements Controller {
 	
 	static private Logger logger = LoggerFactory
 			.getLogger(PageSystemController.class);
 	
 	/**
 	 * pages in one system on client event if it exists.
 	 */
 	
 	private GenerateOneSystem generateOneSystem;
 	public void setGenerateOneSystem(GenerateOneSystem generateOneSystem) {
 		this.generateOneSystem = generateOneSystem;
 	}
 
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		String udim = request.getParameter("udim");
 		String vdim = request.getParameter("vdim");
 		boolean result = generateOneSystem.doesSystemAllReadyExist(udim, vdim);
 		List<SystemPlusSomeDetails> systemPlusSomeDetailsList = null;
 
 		if(result){
 			MarshallSystems marshallSytems = new MarshallSystems();
 			SystemRep systemRep = marshallSytems.getOneSystemRep(udim, vdim);
 			List<SystemRep> _system_rep_list  = new ArrayList<SystemRep>();
 			_system_rep_list.add(systemRep);
 			systemPlusSomeDetailsList = new MarshalSystemDetails()
 			.addClustersAndStars(_system_rep_list);
			logger.info("systemPlusSomeDetails: "+ systemPlusSomeDetailsList.get(0) + ":::" + systemPlusSomeDetailsList.size() );
 			systemPlusSomeDetailsList.get(0).setTheMessage(udim+":"+vdim+" already exists");
 		}
 		else{
 			systemPlusSomeDetailsList = EmptySystem.one();
 			systemPlusSomeDetailsList.get(0).setTheMessage(udim+":"+vdim+" DOES NOT exist");
 		}
 		ModelAndView modelAndView = new ModelAndView(new SystemDetailView());
		SystemPlusSomeDetails systemPlusSomeDetails = systemPlusSomeDetailsList
				.get(0);
		modelAndView.addObject(SystemDetailView.JSON_ROOT,
				SystemSimpleArray.genSimpleArray(systemPlusSomeDetails));
 		return modelAndView;
 	}
 
 }
