 package eu.trentorise.smartcampus.universiadi.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.json.JSONException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.ac.provider.filters.AcProviderFilter;
 import eu.trentorise.smartcampus.presentation.common.exception.DataException;
 import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
 import eu.trentorise.smartcampus.universiadi.model.TurnoObj;
 import eu.trentorise.smartcampus.universiadi.model.UserObj;
 import eu.trentorise.smartcampus.universiadi.model.containerData.ContainerUtenti;
 
 @Controller("UserObjectController")
 public class UserObjectController {
 
 	@Autowired(required = false)
 	MongoTemplate db;
 
 	@PostConstruct
 	public void init() {
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/login/{user}/{password}")
 	public @ResponseBody
 	String getAuthToken(HttpServletRequest request,
 			@PathVariable("user") String user, HttpServletResponse response,
 			@PathVariable("password") String password,
 			HttpServletResponse response1, HttpSession session)
 			throws JSONException {
 
 		ArrayList<UserObj> mListaUtenti = ContainerUtenti.getUtenti();
 		for (UserObj utente : mListaUtenti)
 			if (utente.getUser().equalsIgnoreCase(user)
 					&& utente.getPassword().equalsIgnoreCase(password)) {
 				String token = "aee58a92-d42d-42e8-b55e-12e4289586fc";
 				UserObj newUser = new UserObj(utente.getNome(),
 						utente.getCognome(), utente.getAmbito(),
 						utente.getRuolo(), utente.getFoto(), utente.getUser(),
 						utente.getPassword(), utente.getNumeroTelefonico());
 				newUser.setToken(token);
 				ContainerUtenti.replaceUtente(utente, newUser);
 				return token;
 			}
 		return null;
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/read_user_data")
 	public @ResponseBody
 	UserObj getUserData(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session) {
		String token = request.getHeader(AcProviderFilter.TOKEN_HEADER);
 		
 		ArrayList<UserObj> mListaUtenti = ContainerUtenti.getUtenti();
		for (UserObj utente : mListaUtenti)
			if (utente.getToken() != null
					&& utente.getToken().equalsIgnoreCase(token))
				return utente;
		return null;
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/anonymus_login")
 	public @ResponseBody
 	String getAnonymousToken(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session) {
 
 		return "token_anonymous";
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/random_turn")
 	public @ResponseBody
 	TurnoObj getTurnoRandom(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session) {
 		ArrayList<UserObj> listaUtenti = new ArrayList<UserObj>();
 		ArrayList<TurnoObj> listaTurni = new ArrayList<TurnoObj>();
 		listaUtenti = ContainerUtenti.getUtenti();
 
 		for (UserObj user : listaUtenti) {
 
 			listaTurni = user.getListaTurni();
 
 		}
 
 		return listaTurni.get(0);
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/invalidate")
 	public @ResponseBody
 	boolean invalidateToken(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session) {
 		String token = request.getHeader(AcProviderFilter.TOKEN_HEADER);
 		
 		ArrayList<UserObj> mListaUtenti = ContainerUtenti.getUtenti();
 		for (UserObj utente : mListaUtenti)
 			if (utente.getToken() != null
 					&& utente.getToken().equalsIgnoreCase(token)) {
 				UserObj newUser = new UserObj(utente.getNome(),
 						utente.getCognome(), utente.getAmbito(),
 						utente.getRuolo(), utente.getFoto(), utente.getUser(),
 						utente.getPassword(), utente.getNumeroTelefonico());
 				newUser.setToken(null);
 				ContainerUtenti.replaceUtente(utente, newUser);
 				return true;
 			}
 		return false;
 	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "/users_for_turn")
 	public @ResponseBody
 	ArrayList<UserObj> getAtletiForEvento(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@RequestBody TurnoObj turno) throws DataException, IOException,
 			NotFoundException {
 		ArrayList<UserObj> listaUtenti = new ArrayList<UserObj>();
 		ArrayList<UserObj> listaUtentiMatch = new ArrayList<UserObj>();
 		listaUtenti = ContainerUtenti.getUtenti();
 
 		for (UserObj utente : listaUtenti) {
 			ArrayList<TurnoObj> listaTurniUtente = new ArrayList<TurnoObj>();
 			listaTurniUtente = utente.getListaTurni();
 			for (TurnoObj turnoSingoloUtente : listaTurniUtente) {
 				if (turnoSingoloUtente.getData() == turno.getData()
 						&& turnoSingoloUtente.getLuogo().equalsIgnoreCase(
 								turno.getLuogo())
 						&& turnoSingoloUtente.getCategoria().equalsIgnoreCase(
 								turno.getCategoria())
 						&& turnoSingoloUtente.getOraInizio() == turno
 								.getOraInizio()
 						&& turnoSingoloUtente.getOraFine() == turno
 								.getOraFine()) {
 					listaUtentiMatch.add(utente);
 				}
 			}
 		}
 
 		return listaUtentiMatch;
 	}
 
 }
