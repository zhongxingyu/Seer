 package modelloTreni;
 
 import java.sql.Time;
 
 import javax.persistence.*;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.sql.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedMap;
 
 import javax.persistence.EntityManager;
 
 import ricerca.MainAlg;
 import ricerca.SearchResultItem;
 import ricerca.TrainState;
 import test.HibernateUtil;
 
 import modelloUtenti.UserManager;
import modelloUtenti.Utente;
 
 public class TrainManager {
 
 	// fa da dao
 	// i costruttori di tutte le classi (a parte TrainManager) sono volutamente
 	// con accesso package
 
 	private static TrainManager trainManager;
 
 	public static TrainManager getInstance() {
 		if (trainManager == null) {
 			trainManager = new TrainManager();
 		}
 		return trainManager;
 	}
 
 	private TrainManager() {
 		super();
 	}
 
 	public void addPosto(Posto posto) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		em.persist(posto);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void prenotaPosto(Posto posto) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		em.merge(posto);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void updateNumeroPostiDisponibili(IstanzaTrenoPrenotabile istanza) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		em.merge(istanza);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void removeTratta(int idTratta) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		Tratta t = em.find(Tratta.class, idTratta);
 
 		em.getTransaction().begin();
 		em.remove(t);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void removeStazione(String stazione) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Stazione> query = em
 				.createQuery("select s from Stazione s where s.nome =:fnome",
 						Stazione.class);
 
 		query.setParameter("fnome", stazione);
 		Stazione st = query.getSingleResult();
 
 		em.getTransaction().begin();
 		em.remove(st);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void removeCorsa(int idCorsa) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		Corsa c = em.find(Corsa.class, idCorsa);
 		em.remove(c);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void updateCorsa(int idCorsa, int idNuovaTratta,
 			Tipologia nuovaTipologia) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		Corsa c = em.find(Corsa.class, idCorsa);
 		Tratta nuovaTratta = em.find(Tratta.class, idNuovaTratta);
 		c.setTratta(nuovaTratta);
 		c.setTipo(nuovaTipologia);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void addStazioneToTratta(int idTratta, String stazione, int distanza)
 			throws TrainException {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		Tratta t = em.find(Tratta.class, idTratta);
 
 		for (int i = 0; i < t.getStazioniTratta().size(); i++) {
 			if (t.getStazioniTratta().get(i).getDistanza() == distanza) {
 				throw new TrainException(
 						"Hai gia' inserito una stazione con questa distanza, in questa tratta!");
 			}
 			if (t.getStazioniTratta().get(i).getStazione().getNome()
 					.equals(stazione)) {
 				throw new TrainException(
 						"Hai gia' inserito questa stazione, in questa tratta!");
 			}
 		}
 
 		TypedQuery<Stazione> query = em
 				.createQuery("select s from Stazione s where s.nome =:fnome",
 						Stazione.class);
 
 		query.setParameter("fnome", stazione);
 
 		Stazione s = query.getSingleResult();
 
 		if ((distanza == 0) && (s.isPrincipale() == false)) {
 			throw new TrainException(
 					"La prima stazione deve essere principale!");
 		}
 
 		em.getTransaction().begin();
 		StazioneTratta st = new StazioneTratta(s, distanza, t);
 		em.persist(st);
 		t.aggiungiStazioneTratta(st);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void removeStazioneFromTratta(int idTratta, String stazione) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		Tratta t = em.find(Tratta.class, idTratta);
 
 		TypedQuery<StazioneTratta> query = em
 				.createQuery(
 						"select st from StazioneTratta st,Stazione s where st.tratta =:ftratta and st.stazione "
 								+ "= s and s.nome =:fnome",
 						StazioneTratta.class);
 
 		query.setParameter("fnome", stazione);
 		query.setParameter("ftratta", t);
 
 		StazioneTratta st = query.getSingleResult();
 
 		em.getTransaction().begin();
 		em.remove(st);
 		t.togliStazioneTratta(st);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void addFermataToCorsa(int idCorsa, int idStazioneTratta, Time orario)
 			throws TrainException {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		Corsa c = em.find(Corsa.class, idCorsa);
 		StazioneTratta st = em.find(StazioneTratta.class, idStazioneTratta);
 		if (c.getFermate().size() != 0) {
 			ArrayList<Fermata> fermateInserite = c.getFermate();
 			for (int i = 0; i < fermateInserite.size(); i++) {
 				Fermata fermata = c.getFermate().get(i);
 				if (areEquals(orario, fermata.getTime())) {
 					throw new TrainException(
 							"Hai gia' inserito una fermata con questo orario!");
 				} else {
 					if (isBefore(fermata.getTime(), orario)) {
 						if (fermata.getStazioneTratta().getDistanza() > st
 								.getDistanza()) {
 							throw new TrainException(
 									"Gli orari e le distanze hanno lo stesso ordinamento!");
 						}
 					} else {
 						if (fermata.getStazioneTratta().getDistanza() < st
 								.getDistanza()) {
 							throw new TrainException(
 									"Gli orari e le distanze hanno lo stesso ordinamento!");
 						}
 					}
 				}
 			}
 		} else {
 			if (st.getDistanza() != 0) {
 				throw new TrainException(
 						"La prima fermata dev'essere la prima stazione della tratta");
 			}
 		}
 		Fermata f = new Fermata(c, st, orario);
 		em.persist(f);
 		c.aggiungiFermata(f);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	boolean areEquals(Time time1, Time time2) {
 
 		Calendar calendar1 = new GregorianCalendar();
 		calendar1.setTimeInMillis(time1.getTime());
 
 		Calendar calendar2 = new GregorianCalendar();
 		calendar2.setTimeInMillis(time2.getTime());
 
 		if (calendar1.get(Calendar.HOUR_OF_DAY) == calendar2
 				.get(Calendar.HOUR_OF_DAY)
 				&& calendar1.get(Calendar.MINUTE) == calendar2
 						.get(Calendar.MINUTE)
 				&& calendar1.get(Calendar.SECOND) == calendar2
 						.get(Calendar.SECOND)
 
 		)
 			return true;
 		else {
 			return false;
 		}
 	}
 
 	// returns True if Time1 < Time2
         public static boolean isBefore(Time time1, Time time2) {
 
 		Calendar calendar1 = new GregorianCalendar();
 		calendar1.setTimeInMillis(time1.getTime());
 
 		Calendar calendar2 = new GregorianCalendar();
 		calendar2.setTimeInMillis(time2.getTime());
 
 		calendar1.set(Calendar.YEAR, 1970);
 		calendar1.set(Calendar.MONTH, 0);
 		calendar1.set(Calendar.DATE, 1);
 
 		calendar2.set(Calendar.YEAR, 1970);
 		calendar2.set(Calendar.MONTH, 0);
 		calendar2.set(Calendar.DATE, 1);
 
 		if (calendar1.before(calendar2)) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	public void removeFermataFromCorsa(int idCorsa, int idStazioneTratta)
 			throws TrainException {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		Corsa c = em.find(Corsa.class, idCorsa);
 		try {
 			TypedQuery<IstanzaTreno> queryIstanza = em.createQuery(
 					"select i from IstanzaTreno i where i.corsa =:icorsa",
 					IstanzaTreno.class);
 			queryIstanza.setParameter("icorsa", c);
 			IstanzaTreno i = queryIstanza.getSingleResult();
 
 			if (i != null) {
 				if (c.getFermataArrivo().getStazioneTratta().getId() == idStazioneTratta) {
 					throw new TrainException(
 							"Non puoi eliminare questa fermata. E' la fermata di arrivo di una corsa utilizzata in un istanza!");
 				} else {
 					if (c.getFermataPartenza().getStazioneTratta().getId() == idStazioneTratta) {
 						throw new TrainException(
 								"Non puoi eliminare questa fermata. E' la fermata di partenza di una corsa utilizzata in un istanza!");
 					}
 				}
 			}
 		} catch (NoResultException e) {
 
 		}
 
 		StazioneTratta st = em.find(StazioneTratta.class, idStazioneTratta);
 
 		TypedQuery<Fermata> query = em
 				.createQuery(
 						"select f from Fermata f where f.corsa =:fcorsa and f.stazioneTratta =:fstazioneTratta",
 						Fermata.class);
 
 		query.setParameter("fcorsa", c);
 		query.setParameter("fstazioneTratta", st);
 
 		Fermata f = query.getSingleResult();
 
 		em.getTransaction().begin();
 		em.remove(f);
 		c.togliFermata(f);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void removeTreno(int idTreno) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		Treno t = em.find(Treno.class, idTreno);
 		em.remove(t);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void updateTreno(int idTreno, Tipologia nuovaTipologia) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		Treno t = em.find(Treno.class, idTreno);
 		t.setTipo(nuovaTipologia);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public Stazione createStazione(String nome) {
 		Stazione stazione = new Stazione(nome);
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		em.persist(stazione);
 		em.getTransaction().commit();
 		em.close();
 		return stazione;
 	}
 
 	public Stazione createStazione(String nome, boolean principale)
 			throws TrainException {
 		Stazione stazione = new Stazione(nome, principale);
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		try {
 			em.persist(stazione);
 		} catch (javax.persistence.PersistenceException e) {
 			throw new TrainException("La stazione esiste gia'!");
 		}
 		em.getTransaction().commit();
 		em.close();
 		return stazione;
 	}
 
 	public Tratta createTratta(SortedMap<Integer, Stazione> map, String nome)
 			throws TrainException {
 
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		if(!map.containsKey(0)){
 			throw new TrainException ("Non hai impostato una stazione di partenza!");
 		}
 		Tratta t = new Tratta(nome);
 		try {
 			em.persist(t);
 		} catch (javax.persistence.PersistenceException e) {
 			throw new TrainException("Una tratta con questo nome esiste gia'!");
 		}
 		ArrayList<StazioneTratta> stazioniTratta = new ArrayList<StazioneTratta>();
 		Set<Map.Entry<Integer, Stazione>> set = map.entrySet();
 
 		for (Entry<Integer, Stazione> e : set) {
 			StazioneTratta s = new StazioneTratta(e.getValue(), e.getKey(), t);
 			em.persist(s);
 			t.aggiungiStazioneTratta(s);
 			stazioniTratta.add(s);
 		}
 
 		em.getTransaction().commit();
 		em.close();
 		return t;
 
 	}
 
 public Corsa createCorsa(Tratta tratta, Tipologia tipo,
 			SortedMap<Time, StazioneTratta> map) throws TrainException {
 
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		if (tratta.getStazioniTratta().size() < 2) {
 			throw new TrainException("La tratta selezionata non e' completa");
 		}
 		StazioneTratta ultimaStazione = tratta.getStazioniTratta().get(0);
 		for(int i=1; i< tratta.getStazioniTratta().size(); i++){
 			if(ultimaStazione.getDistanza() < tratta.getStazioniTratta().get(i).getDistanza()){
 				ultimaStazione = tratta.getStazioniTratta().get(i);
 			}
 		}
 		if (ultimaStazione.getStazione().isPrincipale() == false) {
 			throw new TrainException(
 					"La tratta selezionata non e' completa. L'ultima stazione della tratta deve essere principale!");
 		}
 		Corsa c = new Corsa(null, tipo, null);
 
 		ArrayList<Fermata> fermate = new ArrayList<Fermata>();
 		Set<Map.Entry<Time, StazioneTratta>> set = map.entrySet();
 		em.getTransaction().begin();
 		em.persist(c);
 		em.getTransaction().commit();
 		for (Entry<Time, StazioneTratta> e : set) {
 			em.getTransaction().begin();
 			Fermata s = new Fermata(c, e.getValue(), e.getKey());
 			em.persist(s);
 			em.getTransaction().commit();
 			fermate.add(s);
 		}
 		em.getTransaction().begin();
 		c.setTratta(tratta);
 		c.setFermate(fermate);
 		em.merge(c);
 		em.getTransaction().commit();
 		em.close();
 		return c;
 	}
 
 	public Treno createTreno(SortedMap<ClassePosto, Integer> numPostiPerClasse,
 			Tipologia tipo) {
 
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		Treno t = new Treno(numPostiPerClasse, tipo);
 		em.persist(t);
 		em.getTransaction().commit();
 		em.close();
 		return t;
 	}
 
 	public IstanzaTreno createIstanza(Corsa corsa, Treno treno, Date data)
 			throws TrainException {
 		if (corsa.getTipo() != treno.getTipo()) {
 			throw new TrainException("Mismatch found");
 		}
 		if (corsa.getFermate().size() < 2) {
 			throw new TrainException("Corsa senza fermate partenza/arrivo!");
 		}
 		int distanzaUltimaStazione = corsa.getTratta().getStazioniTratta()
 				.get(corsa.getTratta().getStazioniTratta().size() - 1)
 				.getDistanza();
 		if (corsa.getFermataArrivo().getStazioneTratta().getDistanza() != distanzaUltimaStazione) {
 			throw new TrainException(
 					"Corsa non completa. La fermata di arrivo deve essere l'ultima della tratta!");
 		}
 		IstanzaTreno i = null;
 		if (corsa.getTipo() == Tipologia.REGIONALE) {
 			i = new IstanzaTrenoNonPrenotabile(corsa, treno, data);
 		} else {
 			i = new IstanzaTrenoPrenotabile(corsa, treno, data);
 		}
 		return i;
 	}
 
 	public void removeIstanza(int idIstanza) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		IstanzaTreno i = em.find(IstanzaTreno.class, idIstanza);
 		em.remove(i);
 		em.getTransaction().commit();
 		em.close();
 	}
 	
 	public void removePrenotazione(int idPrenotazione) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		Prenotazione p = em.find(Prenotazione.class, idPrenotazione);
 		Utente u = em.find(Utente.class, p.getUtente().getId());
 		u.removePrenotazione(p);
 		for(int i=0; i< p.getPostiPrenotati().size(); i++){
 			Posto posto = em.find(Posto.class, p.getPostiPrenotati().get(i).getId());
 			posto.setPrenotazione(null);
 		}
 		em.remove(p);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void addIstanza(IstanzaTreno istanza) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		em.persist(istanza);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void refreshIstanza(IstanzaTreno istanza) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		em.merge(istanza);
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void updateIstanza(int idIstanza, int idNuovoTreno, Date nuovaData)
 			throws TrainException {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		em.getTransaction().begin();
 		IstanzaTreno i = em.find(IstanzaTreno.class, idIstanza);
 
 		if (idNuovoTreno != 0) {
 			Treno nuovoTreno = em.find(Treno.class, idNuovoTreno);
 			if (i.getCorsa().getTipo() != nuovoTreno.getTipo()) {
 				throw new TrainException();
 			} else {
 				i.setTreno(nuovoTreno);
 			}
 		}
 		if (nuovaData != null) {
 			i.setData(nuovaData);
 		}
 
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	public void compraBiglietto(IstanzaTreno i, int numPosti, ClassePosto c)
 			throws TrainException {
 		try {
 			ArrayList<Posto> postiPrenotati = i.compraBiglietto(numPosti, c);
 			Date dataDiPrenotazione = new Date(Calendar.getInstance().getTime()
 					.getTime());
 			EntityManager em = HibernateUtil.getEntityManagerFactory()
 					.createEntityManager();
 			em.getTransaction().begin();
 			Prenotazione p = new Prenotazione(UserManager.getInstance()
 					.getLoggedUser(), postiPrenotati, dataDiPrenotazione);
 
 			em.persist(p);
 			em.getTransaction().commit();
 			em.getTransaction().begin();
 			UserManager.getInstance().getLoggedUser().addPrenotazione(p);
 			em.merge(UserManager.getInstance().getLoggedUser());
 			em.getTransaction().commit();
 			em.close();
 			for (int j = 0; j < postiPrenotati.size(); j++) {
 				prenotaPosto(postiPrenotati.get(j));
 			}
 		} catch (TrainException e) {
 			throw e;
 		}
 
 	}
 
 	public ArrayList<Double> calcolaPrezzo(Tipologia tipoTreno,
 			int idFermataPartenza, int idFermataArrivo) {
 		final double molt1Classe = 1.5;
 		final double moltIntercity = 1.5;
 		final double moltEurostar = 2.5;
 
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 		Fermata fermataPartenza = em.find(Fermata.class, idFermataPartenza);
 		int posizionePartenza = fermataPartenza.getStazioneTratta()
 				.getDistanza();
 		Fermata fermataArrivo = em.find(Fermata.class, idFermataArrivo);
 		int posizioneArrivo = fermataArrivo.getStazioneTratta().getDistanza();
 		int distanzaPercorsa = posizioneArrivo - posizionePartenza;
 		double prezzo = distanzaPercorsa;
 		if (tipoTreno == Tipologia.EUROSTAR) {
 			prezzo = prezzo * moltEurostar;
 		} else {
 			if (tipoTreno == Tipologia.INTERCITY) {
 				prezzo = prezzo * moltIntercity;
 			}
 		}
 		ArrayList<Double> prezzi = new ArrayList<Double>(2);
 		prezzi.add(prezzo * molt1Classe);
 		prezzi.add(prezzo);
 		em.close();
 		return prezzi;
 	}
 
 
 	public List<Prenotazione> getPrenotazioni() {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Prenotazione> query = em.createQuery(
 				"select p from Prenotazione p", Prenotazione.class);
 
 		List<Prenotazione> result = query.getResultList();
 		return result;
 
 	}
 
 	public List<Stazione> getStazioni() {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Stazione> query = em.createQuery("select s from Stazione s",
 				Stazione.class);
 
 		List<Stazione> result = query.getResultList();
 		return result;
 
 	}
 
 	public Stazione getStazione(String nome) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Stazione> query = em
 				.createQuery("select s from Stazione s where s.nome =:fnome",
 						Stazione.class);
 
 		query.setParameter("fnome", nome);
 
 		Stazione result = query.getSingleResult();
 		return result;
 
 	}
 
 	public List<Treno> getTreni() {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Treno> query = em.createQuery("select t from Treno t",
 				Treno.class);
 
 		List<Treno> result = query.getResultList();
 		return result;
 
 	}
 
 	public Treno getTreno(int id) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		return em.find(Treno.class, id);
 
 	}
 	
 	public List<Treno> getTreniUsati(Date data) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<IstanzaTreno> query = em.createQuery("select i from IstanzaTreno i where data='" +data + "'" ,
 				IstanzaTreno.class);
 
 		List<IstanzaTreno> result = query.getResultList();
 		List<Treno> treniUsati = new ArrayList<Treno>();
 		for(int i=0; i< result.size(); i++){
 			treniUsati.add(result.get(i).getTreno());
 		}
 		return treniUsati;
 
 	}
 
 	public List<Tratta> getTratte() {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Tratta> query = em.createQuery(
 				"select t from Tratta t order by t.id asc", Tratta.class);
 
 		List<Tratta> result = query.getResultList();
 		return result;
 	}
 
 	public List<Tratta> getTratteCrossingStation(Stazione crossStation) {
 
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Tratta> query = em
 				.createQuery(
 						"select distinct t from Tratta t, StazioneTratta st,Stazione s where st.tratta = t and st.stazione = s and s =:fcross",
 						Tratta.class);
 		query.setParameter("fcross", crossStation);
 
 		List<Tratta> result = query.getResultList();
 		return result;
 
 	}
 
 	public Tratta getTratta(int id) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		return em.find(Tratta.class, id);
 
 	}
 
 	public Tratta getTratta(String nome) {
 
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Tratta> query = em.createQuery(
 				"select t from Tratta t where t.nome =:fnome", Tratta.class);
 
 		query.setParameter("fnome", nome);
 
 		Tratta result = query.getSingleResult();
 		return result;
 	}
 
 	public List<Corsa> getCorse() {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Corsa> query = em.createQuery("select c from Corsa c",
 				Corsa.class);
 
 		List<Corsa> result = query.getResultList();
 		return result;
 	}
 
 	public Corsa getCorsa(int id) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		return em.find(Corsa.class, id);
 
 	}
 
 	public List<IstanzaTreno> getIstanzeTreno() {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<IstanzaTreno> query = em.createQuery(
 				"select i from IstanzaTreno i", IstanzaTreno.class);
 
 		List<IstanzaTreno> result = query.getResultList();
 		return result;
 	}
 
 	public IstanzaTreno getIstanzaTreno(int id) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		return em.find(IstanzaTreno.class, id);
 	}
 
 	public List<IstanzaTreno> getIstanzaTrenoInDateUsingTreno(Date date,
 			int idTreno) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		Treno treno = em.find(Treno.class, idTreno);
 
 		TypedQuery<IstanzaTreno> query = em
 				.createQuery(
 						"select distinct i from IstanzaTreno i where i.data =:fdata and i.treno =:ftreno ",
 						IstanzaTreno.class);
 
 		query.setParameter("ftreno", treno);
 		query.setParameter("fdata", date);
 
 		List<IstanzaTreno> result = query.getResultList();
 		return result;
 
 	}
 
 	// ritorna una lista di SearchResultItem se esiste una soluzione, altrimenti
 	// ritorna una lista vuota
 	public List<SearchResultItem> search(Stazione partenza, Stazione arrivo,
 			Calendar time) {
 
 		TrainState initialState = new TrainState(arrivo, partenza, 0, time);
 		MainAlg alg = new MainAlg(initialState);
 		List<SearchResultItem> r = alg.search();
 		return r;
 	}
 
 	public List<IstanzaTreno> getTrainsFromStationAfterHour(Stazione stazione,
 			Calendar calendar) {
 
 		Time time = new Time(calendar.getTimeInMillis());
 		Date date = new Date(calendar.getTimeInMillis());
 
 		if ((calendar.get(Calendar.HOUR_OF_DAY) + 3) < 24) {
 
 			Calendar calendar2 = (Calendar) calendar.clone();
 			calendar2.add(Calendar.HOUR_OF_DAY, 3);
 			Time time2 = new Time(calendar2.getTimeInMillis());
 			List<IstanzaTreno> result = getTrainsFromStationBetween(stazione,
 					date, time, time2);
 
 			return result;
 
 		} else {
 
 			Calendar calendar2359 = (Calendar) calendar.clone();
 			;
 			calendar2359.set(Calendar.HOUR_OF_DAY, 23);
 			calendar2359.set(Calendar.MINUTE, 59);
 			calendar2359.set(Calendar.SECOND, 59);
 			List<IstanzaTreno> result = getTrainsFromStationBetween(stazione,
 					date, time, new Time(calendar2359.getTimeInMillis()));
 
 			Calendar calendar2 = (Calendar) calendar2359.clone();
 			calendar2.add(Calendar.HOUR_OF_DAY,
 					3 - (24 - calendar.get(Calendar.HOUR_OF_DAY)));
 
 			Time time2 = new Time(calendar2.getTimeInMillis());
 			Date date2 = new Date(calendar2.getTimeInMillis());
 
 			Calendar calendar0000 = (Calendar) calendar.clone();
 			calendar0000.add(Calendar.DAY_OF_MONTH, 1);
 			calendar0000.set(Calendar.HOUR_OF_DAY, 0);
 			calendar0000.set(Calendar.MINUTE, 0);
 			calendar0000.set(Calendar.SECOND, 0);
 
 			System.out.println(time2 + "   " + date2);
 
 			result.addAll(getTrainsFromStationBetween(stazione, date2,
 					new Time(calendar0000.getTimeInMillis()), time2));
 
 			return result;
 
 		}
 
 	}
 
 	private List<IstanzaTreno> getTrainsFromStationBetween(Stazione stazione,
 			Date date, Time time1, Time time2) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<IstanzaTreno> query = em
 				.createQuery(
 						"select distinct t from IstanzaTreno t, Corsa c,Fermata f,StazioneTratta st,Stazione s where t.corsa = c and t.data =:fdata and f.corsa = c"
 								+ " and f.stazioneTratta = st and st.stazione = s and s = :fs "
 								+ "and f.time >= :ftime1 and f.time < :ftime2  "
 								+ "", IstanzaTreno.class);
 
 		query.setParameter("fs", stazione);
 
 		query.setParameter("ftime1", time1);
 		query.setParameter("ftime2", time2);
 		query.setParameter("fdata", date);
 
 		List<IstanzaTreno> result = query.getResultList();
 		return result;
 	}
 
 	public List<Corsa> getCourseFromStation(Stazione stazione) {
 		EntityManager em = HibernateUtil.getEntityManagerFactory()
 				.createEntityManager();
 
 		TypedQuery<Corsa> query = em
 				.createQuery(
 						"select distinct c from Corsa c,Fermata f,StazioneTratta st,Stazione s where f.corsa = c"
 								+ " and f.stazioneTratta = st and st.stazione = s and s = :fs ",
 						Corsa.class);
 		query.setParameter("fs", stazione);
 
 		List<Corsa> result = query.getResultList();
 		return result;
 	}
 
 }
