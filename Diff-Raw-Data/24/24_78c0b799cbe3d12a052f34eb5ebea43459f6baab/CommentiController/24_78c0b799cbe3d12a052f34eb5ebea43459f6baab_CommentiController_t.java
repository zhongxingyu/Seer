 package smartcampus.webtemplate.controllers;
 
 import java.io.IOException;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.controllers.SCController;
 import eu.trentorise.smartcampus.corsi.model.Commento;
 import eu.trentorise.smartcampus.corsi.model.Corso;
 import eu.trentorise.smartcampus.corsi.model.CorsoLaurea;
 import eu.trentorise.smartcampus.corsi.model.Dipartimento;
 import eu.trentorise.smartcampus.corsi.model.Evento;
 import eu.trentorise.smartcampus.corsi.model.Studente;
 import eu.trentorise.smartcampus.corsi.repository.CommentiRepository;
 import eu.trentorise.smartcampus.corsi.repository.CorsoLaureaRepository;
 import eu.trentorise.smartcampus.corsi.repository.CorsoRepository;
 import eu.trentorise.smartcampus.corsi.repository.DipartimentoRepository;
 import eu.trentorise.smartcampus.corsi.repository.EventoRepository;
 import eu.trentorise.smartcampus.corsi.repository.StudenteRepository;
 
 @Controller("commentiController")
 public class CommentiController extends SCController {
 
 	private static final Logger logger = Logger
 			.getLogger(CommentiController.class);
 
 	@Autowired
 	private CommentiRepository commentiRepository;
 
 	@Autowired
 	private CorsoRepository corsoRepository;
 
 	@Autowired
 	private StudenteRepository studenteRepository;
 
 	@Autowired
 	private DipartimentoRepository dipartimentoRepository;
 	
 	@Autowired
 	private CorsoLaureaRepository corsoLaureaRepository;
 	
 	@Autowired
 	private EventoRepository eventoRepository;
 	
 	
 
 	/*
 	 * Ritorna tutte le recensioni dato l'id di un corso
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/commento/{id_corso}")
 	public @ResponseBody
 	List<Commento> getCommentoByCorsoId(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("id_corso") Long id_corso)
 
 	throws IOException {
 		try {
 			logger.info("/commenti/{id_corso}");
 			if (id_corso == null)
 				return null;
 
 			return commentiRepository.getCommentoByCorso(corsoRepository
 					.findOne(id_corso));
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 	
 	
 	
 	/*
 	 * Ritorna tutte le recensioni dato l'id di un corso
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/commento/{id_corso}/{id_studente}")
 	public @ResponseBody
 	Commento getCommentoByStudenteId(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, @PathVariable("id_corso") Long id_corso,
 			@PathVariable("id_studente") Long id_studente)
 
 	throws IOException {
 		try {
 			logger.info("/commenti/{id_corso}/{id_studente}");
 			if (id_studente == null)
 				return null;
 
 			return commentiRepository.getCommentoByStudente(studenteRepository
 					.findOne(id_studente), corsoRepository.findOne(id_corso));
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 	
 	
 
 	/*
 	 * Ritorna tutte le recensioni dato l'id di un corso
 	 */
 	@RequestMapping(method = RequestMethod.POST, value = "/commento")
 	//
 	public @ResponseBody
 	boolean saveCommento(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@RequestBody Commento commento)
 
 	throws IOException {
 		try {
 			logger.info("/commento");
 			// TODO control valid field
 			if (commento == null)
 				return false;
 			
 			Commento commentoDaModificare = commentiRepository.getCommentoByStudente(studenteRepository
 					.findOne(commento.getId_studente().getId()), corsoRepository.findOne(commento.getCorso().getId()));
 			
 			if (commentoDaModificare == null){
 				return commentiRepository.save(commento) != null;
 			}else{
 				
 				commentiRepository.delete(commentoDaModificare);
 				
 				return commentiRepository.save(commento) != null;
 				
 			}
 			
 
 		} catch (Exception e) {
 
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return false;
 		}
 
 	}
 
 	
 	
 	
 	
 	
 	@PostConstruct
 	private void initCommenti() {
 
 		Dipartimento d = new Dipartimento();
 
 		d.setNome("Ingegneria e scienza dell informazione");
 
 		dipartimentoRepository.save(d);
 
 		/////// dipartimento 2
 
 		d = new Dipartimento();
 
 		d.setNome("Psicologia e scienze cognitive");
 
 		dipartimentoRepository.save(d);
 
 		/////// dipartimento 3
 
 		d = new Dipartimento();
 
 		d.setNome("Fisica");
 
 		dipartimentoRepository.save(d);
 
 		List<Dipartimento> dip = new ArrayList();
 
 		dip = dipartimentoRepository.findAll();
 
 		for (Dipartimento dipart : dip) {
 
 			CorsoLaurea corsoL = new CorsoLaurea();
 			corsoL.setDipartimento(dipart);
 			corsoL.setNome("Informatica");
 			corsoLaureaRepository.save(corsoL);
 
 			corsoL = new CorsoLaurea();
 			corsoL.setDipartimento(dipart);
 			corsoL.setNome("Ingegneria dell informazione");
 			corsoLaureaRepository.save(corsoL);
 
 			corsoL = new CorsoLaurea();
 			corsoL.setDipartimento(dipart);
 			corsoL.setNome("Ingegneria elettronica e delle telecomunicazioni");
 
 			corsoLaureaRepository.save(corsoL);
 
 		}
 
 		Corso c = new Corso();
 
 		c.setNome("Programmazione 1");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Analisi matematica 1");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Matematica discreta 1");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Programmazione 2");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Programmazione funzionale");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Architettura degli elaboratori");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Matematica discreta 2");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Probabilit� e statistica");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Ingegneria del software");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Basi di dati");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Sistemi operativi");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Reti di calcolatori");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Algoritmi e strutture dati");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		c = new Corso();
 
 		c.setNome("Programmazione per sistemi mobili e tablet");
 		c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 				+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 				+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 				+ "dolore eu fugiat nulla pariatur.");
 		c.setValutazione_media(4);
 		c.setId_dipartimento(1);
 		c.setId_corsoLaurea(1);
 		corsoRepository.save(c);
 
 		int i = 0;
 		int j = 0;
 		for (i = 2; i < 4; i++) {
 
 			c = new Corso();
 
 			c.setNome("Corso" + j++);
 			c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 					+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 					+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 					+ "dolore eu fugiat nulla pariatur.");
 			c.setValutazione_media(4);
 			c.setId_dipartimento(i);
 			c.setId_corsoLaurea(i);
 			corsoRepository.save(c);
 
 			c = new Corso();
 
 			c.setNome("Corso" + j++);
 			c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 					+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 					+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 					+ "dolore eu fugiat nulla pariatur.");
 			c.setValutazione_media(4);
 			c.setId_dipartimento(i);
 			c.setId_corsoLaurea(i);
 			corsoRepository.save(c);
 
 			c = new Corso();
 
 			c.setNome("Corso" + j++);
 			c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 					+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 					+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 					+ "dolore eu fugiat nulla pariatur.");
 			c.setValutazione_media(4);
 			c.setId_dipartimento(i);
 			c.setId_corsoLaurea(i);
 
 			corsoRepository.save(c);
 
 			c = new Corso();
 
 			c.setNome("Corso" + j++);
 			c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 					+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 					+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 					+ "dolore eu fugiat nulla pariatur.");
 			c.setValutazione_media(4);
 			c.setId_dipartimento(i);
 			c.setId_corsoLaurea(i);
 			corsoRepository.save(c);
 
 			c = new Corso();
 
 			c.setNome("Analisi matematica 1");
 			c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 					+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 					+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 					+ "dolore eu fugiat nulla pariatur.");
 			c.setValutazione_media(4);
 			c.setId_dipartimento(i);
 			c.setId_corsoLaurea(i);
 			corsoRepository.save(c);
 
 			c = new Corso();
 
 			c.setNome("Corso" + j++);
 			c.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "
 					+ "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
 					+ "aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum "
 					+ "dolore eu fugiat nulla pariatur.");
 			c.setValutazione_media(4);
 			c.setId_dipartimento(i);
 			c.setId_corsoLaurea(i);
 
 			corsoRepository.save(c);
 
 		}
 
 		
 		
 		
 		List<Corso> esse3 = corsoRepository.findCorsoByCorsoLaureaId((long) 1);
 
 			for (int i1 = 0; i1 < 20; i1++) {
 				Studente studente = new Studente();
 				studente.setId((long) i1);
 				studente.setNome("NomeStudente" + i1);
 				studente.setCognome("CognomeStudente" + i1);
 				studente.setCorsi(esse3);
 				studenteRepository.save(studente);
 			
 
 
 		}
 		
 		
 		
 		
 		esse3 = corsoRepository.findAll();
		Studente stud = studenteRepository.findOne((long) 1);
 
 		for (Corso co : esse3) {
			
 
 				Commento commento = new Commento();
 				commento.setCorso(co);
 				commento.setRating_carico_studio((float) 4);
 				commento.setRating_contenuto((float) 3);
 				commento.setRating_esame((float) 5);
 				commento.setRating_lezioni((float) 4);
 				commento.setRating_materiali((float) 3);
				commento.setId_studente(stud);
 				commento.setTesto("Corso molto utile e soprattutto il professore coinvolge nelle lezioni.");
 				commentiRepository.save(commento);
			
 
 		}
 		
 		
 		
 		
 		
 		
 		
 		esse3 = corsoRepository.findAll();
 		for (Corso index : esse3) {
 			for (int i1 = 0; i1 < 2; i1++) {
 				Evento x = new Evento();
 				x.setCorso(index);
 				x.setTitolo(index.getNome());
 				x.setDescrizione("Lezione teorica di "+ index.getNome());
 				x.setRoom("A20"+i1);
 				x.setEvent_location("Polo Tecnologico Ferrari, Povo");
 				x.setData(new Date("2013/06/2"+String.valueOf(i1+1)));
 				x.setStart(new Time(8, 30, 00));
 				x.setStop(new Time(10, 30, 00));
 				eventoRepository.save(x);
 				
 				x = new Evento();
 				x.setCorso(index);
 				x.setTitolo(index.getNome());
 				x.setDescrizione("Appello d'esame di "+ index.getNome());
 				x.setRoom("A10"+i1);
 				x.setEvent_location("Polo Tecnologico Ferrari, Povo");
 				x.setData(new Date(("2013/07/0"+String.valueOf(i1+1)).toString()));
 				x.setStart(new Time(10, 30, 0));
 				x.setStop(new Time(12, 30, 0));
 				eventoRepository.save(x);
 				
 				
 				x = new Evento();
 				x.setCorso(index);
 				x.setTitolo(index.getNome());
 				x.setDescrizione("Lezione di laboratorio di "+ index.getNome());
 				x.setRoom("A10"+i1);
 				x.setEvent_location("Polo Tecnologico Ferrari, Povo");
 				x.setData(new Date(("2013/07/0"+String.valueOf(i1+1)).toString()));
 				x.setStart(new Time(14, 0, 0));
 				x.setStop(new Time(16, 0, 0));
 				eventoRepository.save(x);
 			}
 		}
 
 	}
 
 }
