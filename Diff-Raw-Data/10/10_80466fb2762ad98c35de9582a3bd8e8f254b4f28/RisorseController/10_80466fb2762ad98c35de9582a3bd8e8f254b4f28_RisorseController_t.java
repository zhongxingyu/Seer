 package eu.trentorise.smartcampus.corsi.controllers;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.corsi.model.Risorse;
 import eu.trentorise.smartcampus.corsi.repository.CorsoRepository;
 
 @Controller("risorsaController")
 public class RisorseController {
 	private static final Logger logger = Logger
 			.getLogger(RisorseController.class);
 
 	/*
 	 * the base appName of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${phl.url}")
 	private String phlUrl;
 
 	@Autowired
 	private CorsoRepository corsoRepository;
 
 	/*
 	 * Ritorna le risorse del corso specificato
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/risorsa/{idcorso}")
 	public @ResponseBody
 	Risorse getRisorsaByCorsoId(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("idcorso") String idcorso)
 
 	throws IOException {
 		try {
 
 			logger.info("/risorsa/{idcorso}");
 			String json = "";
 			Risorse erre = new Risorse();
 
 			if (corsoRepository.findOne(Long.valueOf(idcorso)) != null) {
			//	WebClient client = WebClient.create(phlUrl);
			//	client.path("getFiles/" + idcorso).accept(MediaType.TEXT_PLAIN);
			//	json = client.get(String.class);
 				erre = erre.convert(json);
 			}
 
 			return erre;
 
 		} catch (Exception e) {
 
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 
 		return null;
 	}
 }
