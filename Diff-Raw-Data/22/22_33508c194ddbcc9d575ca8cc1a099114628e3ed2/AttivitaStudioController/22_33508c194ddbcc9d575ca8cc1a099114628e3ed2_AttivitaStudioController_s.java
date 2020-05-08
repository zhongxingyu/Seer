 package eu.trentorise.smartcampus.corsi.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.communicator.CommunicatorConnector;
 import eu.trentorise.smartcampus.communicator.model.EntityObject;
 import eu.trentorise.smartcampus.communicator.model.Notification;
 import eu.trentorise.smartcampus.communicator.model.NotificationAuthor;
 import eu.trentorise.smartcampus.corsi.model.AttivitaDiStudio;
 import eu.trentorise.smartcampus.corsi.model.Corso;
 import eu.trentorise.smartcampus.corsi.model.GruppoDiStudio;
 import eu.trentorise.smartcampus.corsi.model.Studente;
 import eu.trentorise.smartcampus.corsi.repository.AttivitaStudioRepository;
 import eu.trentorise.smartcampus.corsi.repository.CorsoRepository;
 import eu.trentorise.smartcampus.corsi.repository.EventoRepository;
 import eu.trentorise.smartcampus.corsi.repository.GruppoDiStudioRepository;
 import eu.trentorise.smartcampus.corsi.repository.StudenteRepository;
 import eu.trentorise.smartcampus.corsi.util.EasyTokenManger;
 import eu.trentorise.smartcampus.profileservice.BasicProfileService;
 import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
 
 @Controller("attivitaStudioController")
 public class AttivitaStudioController {
 
 	private enum TypeNotification {
 		AVVISO
 	}
 
 	private static final String CLIENT_ID = "b8fcb94d-b4cf-438f-802a-c0a560734c88";
 	private static final String CLIENT_SECRET = "186b10c3-1f14-4833-9728-14eaa6c27891";
 
 	private static final Logger logger = Logger
 			.getLogger(AttivitaStudioController.class);
 	/*
 	 * the base url of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${profile.address}")
 	private String profileaddress;
 
 	/*
 	 * the base appName of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${webapp.name}")
 	private String appName;
 
 	@Autowired
 	@Value("${communicator.address}")
 	private String communicatoraddress;
 
 	@Autowired
 	private AttivitaStudioRepository attivitastudioRepository;
 
 	@Autowired
 	private GruppoDiStudioRepository gruppstudioRepository;
 
 	@Autowired
 	private EventoRepository eventoRepository;
 
 	@Autowired
 	private CorsoRepository corsoRepository;
 
 	@Autowired
 	private StudenteRepository studenteRepository;
 
 	@RequestMapping(method = RequestMethod.POST, value = "/attivitadistudio/add")
 	public @ResponseBody
 	boolean saveAttivitaStudio(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@RequestBody AttivitaDiStudio atDiStudio)
 
 	throws IOException {
 		try {
 
 			logger.info("/attivitadistudio/add");
 
 			// controllo se i campi sono validi
 			if (atDiStudio != null && atDiStudio.getTitolo() != "") {
 				String token = getToken(request);
 				BasicProfileService service = new BasicProfileService(
 						profileaddress);
 				BasicProfile profile = service.getBasicProfile(token);
 				Long userId = Long.valueOf(profile.getUserId());
 
 				// ottengo i membri che fanno parte del gruppo di studio
 				// relativo all'attività di studio
 				GruppoDiStudio gruppoRefersAttivita = gruppstudioRepository
 						.findOne(atDiStudio.getGruppo());
 
 				// controllo se lo studente che manda la richiesta fa parte del
 				// gruppo associato all'attività e se il gruppo esiste
 				if (gruppoRefersAttivita == null
 						|| !gruppoRefersAttivita.isContainsStudente(userId))
 					return false;
 
 				List<String> users = new ArrayList<String>();
 				List<String> idsInvited = gruppoRefersAttivita
 						.getListInvited(userId);
 
 				// aggiungo gli user a cui deve essere inviata l'avviso (tranne
 				// chi ha effettuato la richiesta)
 				for (String id : idsInvited) {
 					users.add(id);
 				}
 
 				// se ci sono notifiche da mandare
 				if (users.size() > 0) {
 
 					CommunicatorConnector communicatorConnector = new CommunicatorConnector(
 							communicatoraddress, appName);
 
 					Notification n = new Notification();
 					n.setTitle(atDiStudio.getTitolo());
 					NotificationAuthor nAuthor = new NotificationAuthor();
 					nAuthor.setAppId(appName);
 					nAuthor.setUserId(userId.toString());
 					n.setAuthor(nAuthor);
 					n.setUser(userId.toString());
 					n.setType(TypeNotification.AVVISO.toString());
 					n.setTimestamp(System.currentTimeMillis());
 					n.setDescription("Nuova attività " + atDiStudio.getTitolo()
 							+ " creata da " + profile.getName() + " "
 							+ profile.getSurname() + " nel gruppo "
 							+ gruppoRefersAttivita.getNome());
 					Map<String, Object> mapAttivita = new HashMap<String, Object>();
 					mapAttivita.put("AttivitaDiStudio", atDiStudio); // passo
 																		// come
 																		// contenuto
 																		// della
 																		// notifica
 																		// l'hashmap
 																		// con
 																		// l'attivita
 					n.setContent(mapAttivita);
 
 					// ottengo il client token
 					EasyTokenManger tManager = new EasyTokenManger(CLIENT_ID,
 							CLIENT_SECRET, profileaddress);
 
 					communicatorConnector.sendAppNotification(n, appName,
 							users, tManager.getClientSmartCampusToken());
 
 				}
 
 				AttivitaDiStudio attivitaSaved = attivitastudioRepository
 						.save(atDiStudio);
 
 				if (attivitaSaved == null)
 					return false;
 				else
 					return true;
 			} else
 				return false;
 
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return false;
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/attivitadistudio/{id_gruppodistudio}")
 	public @ResponseBody
 	List<AttivitaDiStudio> getAttivitadistudioByID(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("id_gruppodistudio") Long id_gruppodistudio)
 
 	throws IOException {
 		try {
 			logger.info("/attivitastudio/{id_gruppidistudio}");
 
 			if (id_gruppodistudio == null)
 				return null;
 
 			return attivitastudioRepository.findAttByIdGds(id_gruppodistudio);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "/attivitadistudio/change")
 	public @ResponseBody
 	boolean changeAttivitaDiStudio(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@RequestBody AttivitaDiStudio attivitadistudio)
 
 	throws IOException {
 		try {
 			logger.info("/attivitadistudio/change");
 
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			Long userId = Long.valueOf(profile.getUserId());
 
 			Studente studente = studenteRepository.findOne(userId);
 			if (studente == null) {
 				studente = new Studente();
 				studente.setId(userId);
 				studente.setNome(profile.getName());
 				studente.setCognome(profile.getSurname());
 				studente = studenteRepository.save(studente);
 
 				// studente = studenteRepository.save(studente);
 
 				// TODO caricare corsi da esse3
 				// Creare associazione su frequenze
 
 				// TEST
 				List<Corso> corsiEsse3 = corsoRepository.findAll();
 
 				String supera = null;
 				String interesse = null;
 				int z = 0;
 				supera = new String();
 				interesse = new String();
 
 				for (Corso cors : corsiEsse3) {
 
 					if (z % 2 == 0) {
 						supera = supera.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 
 					if (z % 4 == 0) {
 						interesse = interesse.concat(String.valueOf(
 								cors.getId()).concat(","));
 					}
 
 					z++;
 				}
 
 				// Set corso follwed by studente
 				studente.setCorsi(corsiEsse3);
 				studente = studenteRepository.save(studente);
 
 				// Set corsi superati
 				studente.setIdsCorsiSuperati(supera);
 				studente.setIdsCorsiInteresse(interesse);
 
 				studente = studenteRepository.save(studente);
 			}
 
 			// ottengo i membri che fanno parte del gruppo di studio relativo
 			// all'attività di studio
 			GruppoDiStudio gruppoRefersAttivita = gruppstudioRepository
 					.findOne(attivitadistudio.getGruppo());
 
 			// controllo se lo studente che manda la richiesta fa parte del
 			// gruppo associato all'attività e se il gruppo esiste
 			if (gruppoRefersAttivita == null
 					|| !gruppoRefersAttivita.isContainsStudente(userId))
 				return false;
 
 			List<String> users = new ArrayList<String>();
 			List<String> idsInvited = gruppoRefersAttivita
 					.getListInvited(userId);
 
 			// controllo se lo studente che manda la richiesta fa parte del
 			// gruppo associato all'attività e se il gruppo esiste
 			for (String id : idsInvited) {
 				users.add(id);
 			}
 
 			// se ci sono notifiche da mandare
 			if (users.size() > 0) {
 
 				CommunicatorConnector communicatorConnector = new CommunicatorConnector(
 						communicatoraddress, appName);
 
 				Notification n = new Notification();
 				n.setTitle(attivitadistudio.getTitolo());
 				NotificationAuthor nAuthor = new NotificationAuthor();
 				nAuthor.setAppId(appName);
 				nAuthor.setUserId(userId.toString());
 				n.setAuthor(nAuthor);
 				n.setUser(userId.toString());
 				n.setTimestamp(System.currentTimeMillis());
 				n.setDescription("L'attività " + attivitadistudio.getTitolo()
 						+ " è stata modificata da " + profile.getName() + " "
 						+ profile.getSurname());
 				Map<String, Object> mapAttivita = new HashMap<String, Object>();
 				mapAttivita.put("AttivitaDiStudio", attivitadistudio); // passo
 																		// come
 																		// contenuto
 																		// della
 																		// notifica
 																		// l'hashmap
 																		// con
 																		// l'attivita
 				n.setContent(mapAttivita);
 
 				// ottengo il client token
 				EasyTokenManger tManager = new EasyTokenManger(CLIENT_ID,
 						CLIENT_SECRET, profileaddress);
 
 				communicatorConnector.sendAppNotification(n, appName, users,
 						tManager.getClientSmartCampusToken());
 			}
 
 			AttivitaDiStudio attivitadistudioAggiornato = attivitastudioRepository
 					.save(attivitadistudio);
 
 			if (attivitadistudioAggiornato == null) {
 				return false;
 			} else {
 				// controllo che il l'attivita aggiornata abbia lo stesso id di
 				// prima
 				if (attivitadistudioAggiornato.getId() == attivitadistudio
 						.getId())
 					return true;
 				else
 					return false;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param gruppodistudio
 	 * @return true se l'operazione è andata a buon fine
 	 * @throws IOExceptionù
 	 * 
 	 *             Elimina un'attività di studio
 	 * 
 	 */
 	@RequestMapping(method = RequestMethod.DELETE, value = "/attivitadistudio/delete")
 	public @ResponseBody
 	boolean deleteAttivita(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@RequestBody AttivitaDiStudio attivitadistudio)
 
 	throws IOException {
 		try {
 			logger.info("/attivitadistudio/delete");
 
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			Long userId = Long.valueOf(profile.getUserId());
 
 			Studente studente = studenteRepository.findOne(userId);
 			if (studente == null) {
 				studente = new Studente();
 				studente.setId(userId);
 				studente.setNome(profile.getName());
 				studente.setCognome(profile.getSurname());
 				studente = studenteRepository.save(studente);
 
 				// studente = studenteRepository.save(studente);
 
 				// TODO caricare corsi da esse3
 				// Creare associazione su frequenze
 
 				// TEST
 				List<Corso> corsiEsse3 = corsoRepository.findAll();
 
 				String supera = null;
 				String interesse = null;
 				int z = 0;
 				supera = new String();
 				interesse = new String();
 
 				for (Corso cors : corsiEsse3) {
 
 					if (z % 2 == 0) {
 						supera = supera.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 
 					if (z % 4 == 0) {
 						interesse = interesse.concat(String.valueOf(
 								cors.getId()).concat(","));
 					}
 
 					z++;
 				}
 
 				// Set corso follwed by studente
 				studente.setCorsi(corsiEsse3);
 				studente = studenteRepository.save(studente);
 
 				// Set corsi superati
 				studente.setIdsCorsiSuperati(supera);
 				studente.setIdsCorsiInteresse(interesse);
 
 				studente = studenteRepository.save(studente);
 			}
 
 			if (userId == null)
 				return false;
 
 			// ottengo i membri che fanno parte del gruppo di studio relativo
 			// all'attività di studio
 			GruppoDiStudio gruppoRefersAttivita = gruppstudioRepository
 					.findOne(attivitadistudio.getGruppo());
 
 			// controllo se lo studente che manda la richiesta fa parte del
 			// gruppo associato all'attività e se il gruppo esiste
 			if (gruppoRefersAttivita == null
 					|| !gruppoRefersAttivita.isContainsStudente(userId))
 				return false;
 
 			List<String> users = new ArrayList<String>();
 			List<String> idsInvited = gruppoRefersAttivita
 					.getListInvited(userId);
 
 			// controllo se lo studente che manda la richiesta fa parte del
 			// gruppo associato all'attività e se il gruppo esiste
 			for (String id : idsInvited) {
 				users.add(id);
 			}
 
 			// se ci sono notifiche da mandare
 			if (users.size() > 0) {
 
 				CommunicatorConnector communicatorConnector = new CommunicatorConnector(
 						communicatoraddress, appName);
 
 				Notification n = new Notification();
 				n.setTitle(attivitadistudio.getTitolo());
 				NotificationAuthor nAuthor = new NotificationAuthor();
 				nAuthor.setAppId(appName);
 				nAuthor.setUserId(userId.toString());
 				n.setAuthor(nAuthor);
 				n.setUser(userId.toString());
 				n.setTimestamp(System.currentTimeMillis());
 				n.setDescription("L'attività " + attivitadistudio.getTitolo()
 						+ " è stata eliminata da " + profile.getName() + " "
 						+ profile.getSurname());
 				Map<String, Object> mapAttivita = new HashMap<String, Object>();
 				mapAttivita.put("AttivitaDiStudio", attivitadistudio); // passo
 																		// come
 																		// contenuto
 																		// della
 																		// notifica
 																		// l'hashmap
 																		// con
 																		// l'attivita
 				n.setContent(mapAttivita);
 
 				// ottengo il client token
 				EasyTokenManger tManager = new EasyTokenManger(CLIENT_ID,
 						CLIENT_SECRET, profileaddress);
 
 				communicatorConnector.sendAppNotification(n, appName, users,
 						tManager.getClientSmartCampusToken());
 			}
 
 			AttivitaDiStudio AttivitaFromDB = attivitastudioRepository
 					.findOne(attivitadistudio.getId());
 
 			attivitastudioRepository.delete(AttivitaFromDB);
 
 			return true;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return String
 	 * 
 	 *         Ottiene il token
 	 * 
 	 */
 	private String getToken(HttpServletRequest request) {
 		return (String) SecurityContextHolder.getContext().getAuthentication()
 				.getPrincipal();
 	}
 
 }
