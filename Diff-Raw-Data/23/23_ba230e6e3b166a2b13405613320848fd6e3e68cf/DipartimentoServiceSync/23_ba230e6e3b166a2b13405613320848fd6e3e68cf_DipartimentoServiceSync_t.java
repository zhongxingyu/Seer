 package eu.trentorise.smartcampus.corsi.servicesync;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.stereotype.Service;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.corsi.model.Dipartimento;
 import eu.trentorise.smartcampus.corsi.repository.DipartimentoRepository;
 import eu.trentorise.smartcampus.corsi.util.EasyTokenManger;
 import eu.trentorise.smartcampus.corsi.util.UniDepartmentMapper;
 import eu.trentorise.smartcampus.unidataservice.UniversityPlannerService;
 import eu.trentorise.smartcampus.unidataservice.model.FacoltaData;
 
 @Service("dipartimentoServiceSync")
 @Configuration
 @ComponentScan("eu.trentorise.smartcampus.corsi.servicesync")
 public class DipartimentoServiceSync {
 	
 	private static final Logger logger = Logger
 			.getLogger(DipartimentoServiceSync.class);
 	/*
 	 * the base url of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${profile.address}")
 	private String profileaddress;
 
 	@Autowired
 	@Value("${url.studente.service}")
 	private String unidataaddress;
 
 	/*
 	 * the base appName of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${webapp.name}")
 	private String appName;
 
 	@Autowired
 	private DipartimentoRepository dipartimentoRepository;
 
	// client id studymate
	@Autowired
	@Value("${studymate.client.id}")
	private String client_id;
	
	// client secret studymate
	@Autowired
	@Value("${studymate.client.secret}")
	private String client_secret;
	
	
	private String client_auth_token;
 	
 	List<Dipartimento> dipartimenti;
 
 	/**
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @return List<Dipartimento>
 	 * @throws IOException
 	 * 
 	 *             Restituisce la lista dei dipartimenti soltanto se sono da sincronizzare
 	 * 
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/sync/dipartimento/all")
 	public @ResponseBody
 	List<Dipartimento> getDipartimentoSync(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 
 	throws IOException {
 		try {
 			logger.info("/sync/dipartimento/all");
 
 			dipartimenti = dipartimentoRepository.findAll();
 			
 			// se la lista dei dipartimenti  gi stata scaricata ritorno null
 			if(dipartimenti.size() > 0)
 				return null;
 
 			// prendo i dati da unidata e li mappo
 			UniversityPlannerService uniConnector = new UniversityPlannerService(
 					unidataaddress);
 			
			EasyTokenManger clientTokenManager = new EasyTokenManger(client_id, client_secret, profileaddress);
 			client_auth_token = clientTokenManager.getClientSmartCampusToken();
 			System.out.println("Client auth token: " + client_auth_token);
 			List<FacoltaData> dataDepartmentsUni = uniConnector.getFacoltaData(client_auth_token);
 			
 			if (dataDepartmentsUni == null)
 				return null;
 
 			UniDepartmentMapper departmentMapper = new UniDepartmentMapper();
 			dipartimenti = departmentMapper.convert(dataDepartmentsUni, client_auth_token);
 			
 
 			dipartimenti = dipartimentoRepository.save(dipartimenti);
 
 			return dipartimenti;
 
 		} catch (Exception e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * 
 	 * @param request
 	 * @return String
 	 * 
 	 *         Ottiene il token riferito alla request
 	 * 
 	 */
 	private String getToken(HttpServletRequest request) {
 		return (String) SecurityContextHolder.getContext().getAuthentication()
 				.getPrincipal();
 	}
 
 }
