 package it.ivncr.erp.service.utente;
 
 import it.ivncr.erp.model.accesso.AccountEmail;
 import it.ivncr.erp.model.accesso.Ruolo;
 import it.ivncr.erp.model.accesso.Utente;
 import it.ivncr.erp.model.accesso.UtenteAzienda;
 import it.ivncr.erp.model.generale.Azienda;
 import it.ivncr.erp.service.AbstractService;
 import it.ivncr.erp.service.AlreadyPresentException;
 import it.ivncr.erp.service.NotFoundException;
 import it.ivncr.erp.service.QueryResult;
 import it.ivncr.erp.service.ServiceException;
 import it.ivncr.erp.service.SortDirection;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.shiro.crypto.SecureRandomNumberGenerator;
 import org.apache.shiro.crypto.hash.SimpleHash;
 import org.hibernate.Query;
 
 public class UtenteServiceImpl extends AbstractService implements UtenteService {
 
 	private static final int HASH_ITERATIONS = 10000;
 
 	@Override
 	public QueryResult<Utente> list(
 			Integer id,
 			String username,
 			String nome,
 			String cognome,
 			String note,
 			Boolean attivo,
 			Integer offset,
 			Integer limit) {
 
 		QueryByIdUsernameNomeCognomeNoteAttivo q = new QueryByIdUsernameNomeCognomeNoteAttivo(session);
 
 		q.setId(id);
 		q.setUsername(username);
 		q.setNome(nome);
 		q.setCognome(cognome);
 		q.setNote(note);
 		q.setAttivo(attivo);
 		q.setOffset(offset);
 		q.setLimit(limit);
 
 		QueryResult<Utente> result = q.query();
 		logger.debug("Query returned: " + result);
 
 		return result;
 	}
 
 	@Override
 	public QueryResult<Utente> list(
 			int first,
 			int pageSize,
 			String sortCriteria,
 			SortDirection sortDirection,
 			Map<String, String> filters) {
 
 		QueryByIdUsernameNomeCognomeNoteAttivo q = new QueryByIdUsernameNomeCognomeNoteAttivo(session);
 
 		Integer id = null;
 		if(filters.get("id") != null)
 			id = Integer.decode(filters.get("id"));
 
 		String username = filters.get("username");
 		String nome = filters.get("nome");
 		String cognome = filters.get("cognome");
 		String note = filters.get("note");
 
 		q.setId(id);
 		q.setUsername(username);
 		q.setNome(nome);
 		q.setCognome(cognome);
 		q.setNote(note);
 		q.setAttivo(null);
 
 		q.setOffset(first);
 		q.setLimit(pageSize);
 
 		q.setSortCriteria(sortCriteria);
 		q.setSortDirection(sortDirection);
 
 		QueryResult<Utente> result = q.query();
 		logger.debug("Query returned: " + result);
 
 		return result;
 	}
 
 	@Override
 	public Utente retrieve(Integer id) {
 
 		Utente utente = (Utente)session.get(Utente.class, id);
 		logger.debug("Utente found: " + utente);
 
 		return utente;
 	}
 
 	@Override
 	public Utente retrieveByUsername(String username) {
 
 		String hql =
 				"from Utente ute " +
 				"where ute.username = :username ";
 		Query query = session.createQuery(hql);
 		query.setParameter("username", username);
 		Utente utente = (Utente)query.uniqueResult();
 		logger.debug("Utente found: " + utente);
 
 		return utente;
 	}
 
 	@Override
 	public Utente retrieveWithAccountEmail(Integer id) {
 
 		String hql =
 				"from Utente ute " +
 				"left join fetch ute.accountEmail aem " +
 				"where ute.id = :id ";
 		Query query = session.createQuery(hql);
 		query.setParameter("id", id);
 		Utente utente = (Utente)query.uniqueResult();
 		logger.debug("Utente found: " + utente);
 
 		return utente;
 	}
 
 	@Override
 	public void delete(Integer id) {
 
 		Utente utente = retrieve(id);
 		if(utente == null) {
 			String message = String.format("It has not been possible to retrieve specified user: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		session.delete(utente);
 		logger.debug("Utente successfully deleted.");
 	}
 
 	@Override
 	public Utente create(
 			String username,
 			String password,
 			String nome,
 			String cognome,
 			String note) {
 
 		Date now = new Date();
 
 		Utente utente = new Utente();
 
 		utente.setUsername(username);
 		utente.setNome(nome);
 		utente.setCognome(cognome);
 		utente.setNote(note);
 		utente.setCreazione(now);
 		utente.setUltimaAttivazione(now);
 		utente.setAttivo(true);
 
 		String salt = generateSalt();
 		String digest = generateDigest(password, salt, HASH_ITERATIONS);
 
 		utente.setDigest(digest);
 		utente.setSalt(salt);
 		utente.setIterazioni(HASH_ITERATIONS);
 
 		session.save(utente);
 		logger.debug("Utente successfully created.");
 
 		return utente;
 	}
 
 	@Override
 	public Utente update(
 			Integer id,
 			String username,
 			String nome,
 			String cognome,
 			String note) {
 
 		Utente utente = retrieve(id);
 		if(utente == null) {
 			String message = String.format("It has not been possible to retrieve specified user: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		utente.setUsername(username);
 		utente.setNome(nome);
 		utente.setCognome(cognome);
 		utente.setNote(note);
 
 		session.update(utente);
 		logger.debug("Utente successfully updated.");
 
 		return utente;
 	}
 
 	@Override
 	public Utente updateAccountEmail(Integer id, String account, String password) {
 
 		Utente utente = retrieve(id);
 		if(utente == null) {
 			String message = String.format("It has not been possible to retrieve specified user: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		AccountEmail accountEmail = utente.getAccountEmail();
 		if(accountEmail == null) {
 			accountEmail = new AccountEmail();
 		}
 
 		accountEmail.setAccount(account);
 		accountEmail.setPassword(password);
 		accountEmail.setUtente(utente);
 		session.saveOrUpdate(accountEmail);
 
 		utente.setAccountEmail(accountEmail);
 		session.update(utente);
 
 		return utente;
 	}
 
 	@Override
 	public Utente changePassword(Integer id, String password) {
 
 		Utente utente = retrieve(id);
 		if(utente == null) {
 			String message = String.format("It has not been possible to retrieve specified user: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		String salt = generateSalt();
 		String digest = generateDigest(password, salt, HASH_ITERATIONS);
 
 		utente.setDigest(digest);
 		utente.setSalt(salt);
 		utente.setIterazioni(HASH_ITERATIONS);
 
 		session.update(utente);
 		logger.debug("Password successfully changed.");
 
 		return utente;
 	}
 
 	@Override
 	public Utente activate(Integer id) {
 
 		Utente utente = retrieve(id);
 		if(utente == null) {
 			String message = String.format("It has not been possible to retrieve specified user: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		// Check if the user is already active.
 		//
 		if(utente.getAttivo()) {
 			String message = String.format("The specified user %d is already active.", id);
 			logger.info(message);
 			throw new ServiceException(message);
 		}
 
 		Date now = new Date();
 
 		utente.setAttivo(true);
 		utente.setUltimaAttivazione(now);
 
 		session.update(utente);
 		logger.debug("User successfully activated.");
 
 		return utente;
 	}
 
 	@Override
 	public Utente deactivate(Integer id) {
 
 		Utente utente = retrieve(id);
 		if(utente == null) {
 			String message = String.format("It has not been possible to retrieve specified user: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		// Check if the user is already not active.
 		//
 		if(!utente.getAttivo()) {
 			String message = String.format("The specified user %d is already not active.", id);
 			logger.info(message);
 			throw new ServiceException(message);
 		}
 
 		Date now = new Date();
 
 		utente.setAttivo(false);
 		utente.setUltimaDisattivazione(now);
 
 		session.update(utente);
 		logger.debug("User successfully deactivated.");
 
 		return utente;
 	}
 
 	@Override
 	public Utente updateLastLogonTimestamp(Integer id) {
 
 		Utente utente = retrieve(id);
 		if(utente == null) {
 			String message = String.format("It has not been possible to retrieve specified user: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		utente.setUltimoLogin(new Date());
 		session.update(utente);
 		logger.debug("Last logon timestamp successfully updated.");
 
 		return utente;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Ruolo> listRuoli(Integer id) {
 
 		Utente utente = retrieve(id);
 		if(utente == null) {
 			String message = String.format("It has not been possible to retrieve specified user: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		String hql =
 				"select distinct ruo from Ruolo ruo " +
 				"inner join ruo.utenti ute " +
 				"where ute.id = :id ";
 		Query query = session.createQuery(hql);
 		query.setParameter("id", id);
 		List<Ruolo> list = query.list();
 
 		logger.debug("Found ruoli list: " + list);
 		return list;
 	}
 
 	@Override
 	public Ruolo addRuolo(Integer utenteId, Integer ruoloId) {
 
 		Utente utente = retrieve(utenteId);
 		if(utente == null) {
 			String message = String.format("It has not been possible to retrieve specified user: %d", utenteId);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		Ruolo ruolo = (Ruolo) session.get(Ruolo.class, ruoloId);
 		if (ruolo == null) {
 			String message = String.format(
 					"It has not been possible to retrieve specified role: %d",
 					ruoloId);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		if (utente.getRuoli().contains(ruolo)) {
 			String message = String
 					.format("Specified role %d already associated to specified user %d.", ruoloId, utenteId);
 			logger.info(message);
 			throw new AlreadyPresentException(message);
 		}
 
 		utente.getRuoli().add(ruolo);
 		session.update(utente);
 
 		return ruolo;
 	}
 
 	@Override
 	public void deleteRuolo(Integer utenteId, Integer ruoloId) {
 
 		Utente utente = retrieve(utenteId);
 		if (utente == null) {
 			String message = String.format(
 					"It has not been possible to retrieve specified user: %d",
 					utenteId);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		Ruolo foundRole = null;
 		for (Ruolo role : utente.getRuoli()) {
 
 			if (role.getId().equals(ruoloId)) {
 				foundRole = role;
 				break;
 			}
 		}
 
 		if (foundRole == null) {
 			String message = String.format(
 					"It has not been possible to find role %d associated to user %d.",
 					ruoloId, utenteId);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		utente.getRuoli().remove(foundRole);
 		session.update(utente);
 	}
 
 	@Override
 	public void setRuoli(Integer utenteId, Integer[] ruoliId) {
 
 		Utente utente = retrieve(utenteId);
 		if (utente == null) {
 			String message = String.format(
 					"It has not been possible to retrieve specified user: %d",
 					utenteId);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		// Remove all roles.
 		//
 		utente.getRuoli().clear();
 
 		// Iterate on passed array, retrieve selected rows
 		// and add them back to the current user.
 		//
 		for(Integer ruoloId : ruoliId) {
 
 			Ruolo ruolo = (Ruolo) session.get(Ruolo.class, ruoloId);
 			if (ruolo == null) {
 				String message = String.format(
 						"It has not been possible to retrieve specified role: %d",
 						ruoloId);
 				logger.info(message);
 				throw new NotFoundException(message);
 			}
 
 			utente.getRuoli().add(ruolo);
 		}
 
 		session.update(utente);
 		session.flush();
 	}
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Azienda> listAziende(Integer id) {
 
 		Utente utente = retrieve(id);
 		if (utente == null) {
 			String message = String.format(
 					"It has not been possible to retrieve specified user: %d",
 					id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		String hql =
 				"select distinct uaz.azienda from UtenteAzienda uaz " +
 				"left join uaz.utente ute " +
 				"where ute.id = :id ";
 		Query query = session.createQuery(hql);
 		query.setParameter("id", id);
 		List<Azienda> list = query.list();
 
 		logger.debug("Found azienda list: " + list);
 		return list;
 	}
 
 
 	@Override
	public Azienda retrieveDefaultAzienda(Integer id, boolean firstIfNotFound) {
 
 		Utente utente = retrieve(id);
 		if (utente == null) {
 			String message = String.format(
 					"It has not been possible to retrieve specified user: %d",
 					id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		// Iterate on configured records and look for default azienda.
 		//
 		Azienda found = null;
 		for(UtenteAzienda azienda : utente.getAziende()) {
 
			if(found == null && firstIfNotFound) {
 				found = azienda.getAzienda();
 			}
 
 			if(azienda.getPreferita() != null && azienda.getPreferita()) {
 				found = azienda.getAzienda();
 				break;
 			}
 		}
 
 		return found;
 	}
 
 	@Override
 	public void setAziende(Integer utenteId, Integer[] aziendeId, Integer preferita) {
 
 		Utente utente = retrieve(utenteId);
 		if (utente == null) {
 			String message = String.format(
 					"It has not been possible to retrieve specified user: %d",
 					utenteId);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		// Remove all aziende.
 		//
 		for(UtenteAzienda ua : utente.getAziende()) {
 			session.delete(ua);
 		}
 		utente.getAziende().clear();
 
 		// Iterate on passed array, retrieve selected rows
 		// and add them back to the current user.
 		//
 		for(Integer aziendaId : aziendeId) {
 
 			Azienda azienda = (Azienda) session.get(Azienda.class, aziendaId);
 			if (azienda == null) {
 				String message = String.format(
 						"It has not been possible to retrieve specified azienda: %d",
 						aziendaId);
 				logger.info(message);
 				throw new NotFoundException(message);
 			}
 
 			UtenteAzienda ua = new UtenteAzienda();
 			ua.setUtente(utente);
 			ua.setAzienda(azienda);
 			if(aziendaId.equals(preferita)) {
 				ua.setPreferita(true);
 			}
 			session.save(ua);
 
 			utente.getAziende().add(ua);
 			azienda.getUtenti().add(ua);
 		}
 
 		session.update(utente);
 	}
 
 
 	private String generateSalt() {
 
 		SecureRandomNumberGenerator srng = new SecureRandomNumberGenerator();
 		String salt = srng.nextBytes(64).toHex();
 
 		return salt;
 	}
 
 
 	private String generateDigest(String password, String salt, int iterations) {
 
 		String hashed = new SimpleHash("SHA-512", password, salt, iterations).toHex();
 		return hashed;
 	}
 }
