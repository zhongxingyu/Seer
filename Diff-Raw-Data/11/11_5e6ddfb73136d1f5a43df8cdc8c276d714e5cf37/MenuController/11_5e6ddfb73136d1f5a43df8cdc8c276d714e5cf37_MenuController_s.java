 package eu.trentorise.smartcampus.vas.ifame.controllers;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.profileservice.BasicProfileService;
 import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDelGiorno;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDelMese;
 import eu.trentorise.smartcampus.vas.ifame.model.Piatto;
 import eu.trentorise.smartcampus.vas.ifame.repository.MensaRepository;
 import eu.trentorise.smartcampus.vas.ifame.repository.PiattoGiornoRepository;
 import eu.trentorise.smartcampus.vas.ifame.repository.PiattoRepository;
 import eu.trentorise.smartcampus.vas.ifame.utils.GestoreMenu;
 import eu.trentorise.smartcampus.vas.ifame.utils.NewMenuXlsUtil;
 
 @Controller("MenuController")
 public class MenuController {
 
 	private static final Logger logger = Logger.getLogger(MenuController.class);
 
 	@Autowired
 	PiattoRepository piattoRepository;
 
 	@Autowired
 	MensaRepository mensaRepository;
 
 	@Autowired
 	PiattoGiornoRepository piattoGiornoRepo;
 
 	/*
 	 * the base url of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${services.server}")
 	private String serverAddress;
 
 	/*
 	 * the base appName of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${webapp.name}")
 	private String appName;
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * 
 	 * MENU DEL GIORNO CONTROLLER
 	 */
 
 	@Autowired
 	@Value("${profile.address}")
 	private String profileaddress;
 
 	private String getToken(HttpServletRequest request) {
 		return (String) SecurityContextHolder.getContext().getAuthentication()
 				.getPrincipal();
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/getmenudelgiorno")
 	public @ResponseBody
 	MenuDelGiorno getMenuDelGiornoStringList(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		try {
 			logger.info("/getmenudelgiorno");
 
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			if (profile != null) {
 
 				int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
 
 				return GestoreMenu.getMenuDelGiorno(piattoGiornoRepo,
 						piattoRepository, day);
 			}
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * 
 	 * MENU DEL MESE
 	 */
 
 	@RequestMapping(method = RequestMethod.GET, value = "/getmenudelmese")
 	public @ResponseBody
 	MenuDelMese getMenuDelMese(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		try {
 			logger.info("/getmenudelmese");
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			if (profile != null) {
 
 				int max = Calendar.getInstance().getActualMaximum(
 						Calendar.DAY_OF_MONTH);
 				int monday = getFirstMondayOfCurrentMonth();
 
 				return GestoreMenu.getMenuDelMese(piattoGiornoRepo,
 						piattoRepository, monday, max);
 
 			}
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 
 	private int getFirstMondayOfCurrentMonth() {
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.DAY_OF_MONTH, 1);
 
 		int firstMonday = -1;
 		for (int i = 0; i < 7; i++) {
 
 			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
 			if (Calendar.MONDAY == dayOfWeek) {
 
 				firstMonday = c.get(Calendar.DAY_OF_MONTH);
 				break;
 			}
 			c.add(Calendar.DAY_OF_MONTH, 1);
 		}
 		return firstMonday;
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * 
 	 * ALTERNATIVE CONTROLLER
 	 * 
 	 * restituisce le alternative al menu del giorno che sono sempre fisse
 	 * leggendo l'excel
 	 */
 
 	@RequestMapping(method = RequestMethod.GET, value = "/getalternative")
 	public @ResponseBody
 	List<Piatto> getAlternative(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		try {
 
 			logger.info("/getalternative");
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			if (profile != null) {
 
 				return GestoreMenu.getAlternative(piattoGiornoRepo,
 						piattoRepository);
 			}
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * 
 	 * INIZIALIZZO / AGGIORNO IL DATABASE
 	 */
 	@PostConstruct
 	private void inizializzaDatabase() throws BiffException, IOException {
 		logger.info("Inizializzazione database");
 
 		Workbook workbook = NewMenuXlsUtil.getWorkbook(getClass()
				.getResourceAsStream("/Dicembre.xls"));
 
 		GestoreMenu.inizializzazioneMenuDatabase(piattoGiornoRepo,
 				piattoRepository, workbook);
 
 		// ********************************************************************
 		// finche non ci sono aggiornamenti sui link delle webcam o nomi delle
 		// mense lasciare tutto commentato
 		// ********************************************************************
 		// /*
 		// *
 		// * Inizializzo le mense
 		// */
 		// final String url_povo_mensa_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mensa-povo2.jpg";
 		// final String url_povo_mensa_veloce_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mensa-povo1.jpg";
 		// final String url_tommaso_gar_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/gar-offline.jpg";
 		// final String url_zanella_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mensa-zanella.jpg";
 		// final String url_mesiano_1_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mesiano-offline.jpg";
 		// final String url_mesiano_2_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mesiano-web-2.jpg";
 		//
 		// final String url_povo_mensa_online =
 		// "http://www.operauni.tn.it/upload/Webcam/Povo02.jpg";
 		// final String url_povo_mensa_veloce_online =
 		// "http://www.operauni.tn.it/upload/Webcam/Povo01.jpg";
 		// final String url_tommaso_gar_online =
 		// "http://www.operauni.tn.it/upload/Webcam/MensaUni.jpg";
 		// final String url_zanella_online =
 		// "http://www.operauni.tn.it/upload/Webcam/mensa_zanella.jpg";
 		// final String url_mesiano_1_online =
 		// "http://www.operauni.tn.it/upload/Webcam/MensaMes01.jpg";
 		// final String url_mesiano_2_online =
 		// "http://www.operauni.tn.it/upload/Webcam/MensaMes02.jpg";
 		//
 		// final String name_povo_mensa = "Povo Mensa";
 		// final String name_povo_mensa_veloce = "Povo Mensa Veloce";
 		// final String name_tommaso_gar = "Tommaso Gar";
 		// final String name_zanella = "Zanella";
 		// final String name_mesiano_1 = "Mesiano 1";
 		// final String name_mesiano_2 = "Mesiano 2";
 		//
 		// Mensa povo_mensa = new Mensa(name_povo_mensa, url_povo_mensa_online,
 		// url_povo_mensa_offline);
 		// Mensa povo_mensa_veloce = new Mensa(name_povo_mensa_veloce,
 		// url_povo_mensa_veloce_online, url_povo_mensa_veloce_offline);
 		// Mensa tommaso_gar = new Mensa(name_tommaso_gar,
 		// url_tommaso_gar_online,
 		// url_tommaso_gar_offline);
 		// Mensa zanella = new Mensa(name_zanella, url_zanella_online,
 		// url_zanella_offline);
 		// Mensa mesiano_1 = new Mensa(name_mesiano_1, url_mesiano_1_online,
 		// url_mesiano_1_offline);
 		// Mensa mesiano_2 = new Mensa(name_mesiano_2, url_mesiano_2_online,
 		// url_mesiano_2_offline);
 		//
 		// mensaRepository.save(povo_mensa);
 		// mensaRepository.save(povo_mensa_veloce);
 		// mensaRepository.save(tommaso_gar);
 		// mensaRepository.save(zanella);
 		// mensaRepository.save(mesiano_1);
 		// mensaRepository.save(mesiano_2);
 		// ********************************************************************
 		logger.info("Tutto apposto a ferragosto");
 	}
 }
