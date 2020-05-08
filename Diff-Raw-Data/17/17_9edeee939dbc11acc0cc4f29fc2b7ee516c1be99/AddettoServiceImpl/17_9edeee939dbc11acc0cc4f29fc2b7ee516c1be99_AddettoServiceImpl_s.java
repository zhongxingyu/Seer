 package it.ivncr.erp.service.addetto;
 
 import it.ivncr.erp.model.generale.Azienda;
 import it.ivncr.erp.model.operativo.Servizio;
 import it.ivncr.erp.model.personale.Addetto;
 import it.ivncr.erp.model.personale.SistemaLavoro;
 import it.ivncr.erp.model.personale.StatoCivile;
 import it.ivncr.erp.service.AbstractService;
 import it.ivncr.erp.service.NotFoundException;
 import it.ivncr.erp.service.QueryResult;
 import it.ivncr.erp.service.SortDirection;
 import it.ivncr.erp.util.AuditUtil;
 import it.ivncr.erp.util.AuditUtil.Operation;
 import it.ivncr.erp.util.AuditUtil.Snapshot;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.time.DateUtils;
 import org.hibernate.Query;
 
 public class AddettoServiceImpl extends AbstractService implements AddettoService {
 
 	@Override
 	public QueryResult<Addetto> list(
 			int first,
 			int pageSize,
 			String sortCriteria,
 			SortDirection sortDirection,
 			Map<String, String> filters) {
 
 		QueryByCodiceAziendaMatricolaNomeCognomeCodiceFiscale q =
 				new QueryByCodiceAziendaMatricolaNomeCognomeCodiceFiscale(session);
 
 		Integer codiceAzienda = null;
 		if(filters.get("codiceAzienda") != null)
 			codiceAzienda = Integer.decode(filters.get("codiceAzienda"));
 
 		String matricola = filters.get("matricola");
 		String nome = filters.get("nome");
 		String cognome = filters.get("cognome");
 		String codiceFiscale = filters.get("codiceFiscale");
 
 		q.setCodiceAzienda(codiceAzienda);
 		q.setMatricola(matricola);
 		q.setNome(nome);
 		q.setCognome(cognome);
 		q.setCodiceFiscale(codiceFiscale);
 		q.setOffset(first);
 		q.setLimit(pageSize);
 
 		q.setSortCriteria(sortCriteria);
 		q.setSortDirection(sortDirection);
 
 		QueryResult<Addetto> result = q.query();
 		logger.debug("Query returned: " + result);
 
 		return result;
 	}
 
 	@Override
 	public QueryResult<Addetto> list(
 			Integer codiceAzienda,
 			String matricola,
 			String nome,
 			String cognome,
 			String codiceFiscale,
 			Integer offset,
 			Integer limit) {
 
 		QueryByCodiceAziendaMatricolaNomeCognomeCodiceFiscale q =
 				new QueryByCodiceAziendaMatricolaNomeCognomeCodiceFiscale(session);
 
 		q.setCodiceAzienda(codiceAzienda);
 		q.setMatricola(matricola);
 		q.setNome(nome);
 		q.setCognome(cognome);
 		q.setCodiceFiscale(codiceFiscale);
 		q.setOffset(offset);
 		q.setLimit(limit);
 
 		QueryResult<Addetto> result = q.query();
 		logger.debug("Query returned: " + result);
 
 		return result;
 	}
 
 	@Override
 	public Addetto retrieve(Integer id) {
 
 		Addetto addetto = (Addetto)session.get(Addetto.class, id);
 		logger.debug("Addetto found: " + addetto);
 
 		return addetto;
 	}
 
 	@Override
 	public Addetto retrieveDeep(Integer id) {
 
 		String hql =
 				"from Addetto add " +
 				"left join fetch add.statoCivile stc " +
 				"where add.id = :id ";
 		Query query = session.createQuery(hql);
 		query.setParameter("id", id);
 		Addetto addetto = (Addetto)query.uniqueResult();
 		logger.debug("Addetto found: " + addetto);
 
 		return addetto;
 	}
 
 	@Override
 	public Addetto retrieveByMatricola(Integer codiceAzienda, String matricola) {
 
 		String hql =
 				"from Addetto add " +
 				"where add.azienda.id = :codiceAzienda " +
 				"and add.matricola = :matricola ";
 		Query query = session.createQuery(hql);
 		query.setParameter("codiceAzienda", codiceAzienda);
 		query.setParameter("matricola", matricola);
 		Addetto addetto = (Addetto)query.uniqueResult();
 		logger.debug("Addetto found: " + addetto);
 
 		return addetto;
 	}
 
 	@Override
 	public Addetto create(
 			Integer codiceAzienda,
 			String matricola,
 			String nome,
 			String cognome,
 			Date dataNascita,
 			String luogoNascita,
 			String codiceFiscale,
 			String sesso,
 			String note,
 			Boolean fittizio,
 			Date dataGiuramento,
 			Integer codiceStatoCivile) {
 
 		Date now = new Date();
 
 		// Fetch referred entities.
 		//
 		Azienda azienda = (Azienda)session.get(Azienda.class, codiceAzienda);
 		StatoCivile statoCivile = (StatoCivile)session.get(StatoCivile.class, codiceStatoCivile);
 
 		// Create the new entity.
 		//
 		Addetto addetto = new Addetto();
 
 		// Set entity fields.
 		//
 		addetto.setAzienda(azienda);
 		addetto.setMatricola(matricola);
 		addetto.setNome(nome);
 		addetto.setCognome(cognome);
 		addetto.setDataNascita(dataNascita);
 		addetto.setLuogoNascita(luogoNascita);
 		addetto.setCodiceFiscale(codiceFiscale);
 		addetto.setSesso(sesso);
 		addetto.setNote(note);
 		addetto.setAttivo(true);
 		addetto.setFittizio(fittizio);
 		addetto.setDataGiuramento(dataGiuramento);
 		addetto.setStatoCivile(statoCivile);
 
 		addetto.setCreazione(now);
 		addetto.setUltimaModifica(now);
 
 		// Persist the entity to the database.
 		//
 		session.save(addetto);
 		logger.debug("Addetto successfully created.");
 
 		// Audit call for the create operation.
 		//
 		AuditUtil.log(Operation.Create, Snapshot.Destination, addetto);
 
 		return addetto;
 	}
 
 	@Override
 	public Addetto update(
 			Integer id,
 			String nome,
 			String cognome,
 			Date dataNascita,
 			String luogoNascita,
 			String codiceFiscale,
 			String sesso,
 			String note,
 			Boolean fittizio,
 			Date dataGiuramento,
 			Integer codiceStatoCivile) {
 
 		Addetto addetto = retrieve(id);
 		if(addetto == null) {
 			String message = String.format("It has not been possible to retrieve specified entity: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		// Audit call for the update operation.
 		//
 		AuditUtil.log(Operation.Update, Snapshot.Source, addetto);
 
 
 		Date now = new Date();
 
 		// Fetch referred entities.
 		//
 		StatoCivile statoCivile = (StatoCivile)session.get(StatoCivile.class, codiceStatoCivile);
 
 		// Set entity fields.
 		//
 		addetto.setNome(nome);
 		addetto.setCognome(cognome);
 		addetto.setDataNascita(dataNascita);
 		addetto.setLuogoNascita(luogoNascita);
 		addetto.setCodiceFiscale(codiceFiscale);
 		addetto.setSesso(sesso);
 		addetto.setNote(note);
 		addetto.setFittizio(fittizio);
 		addetto.setDataGiuramento(dataGiuramento);
 		addetto.setStatoCivile(statoCivile);
 
 		addetto.setUltimaModifica(now);
 
 		session.update(addetto);
 		logger.debug("Entity successfully updated.");
 
 		// Audit call for the update operation.
 		//
 		AuditUtil.log(Operation.Update, Snapshot.Destination, addetto);
 
 		return addetto;
 	}
 
 
 	@Override
 	public String retrieveNextMatricola(Integer codiceAzienda) {
 
 		String hql =
 				"select max(add.matricola) " +
 				"from Addetto add " +
 				"where add.azienda.id = :codiceAzienda ";
 		Query query = session.createQuery(hql);
 		query.setParameter("codiceAzienda", codiceAzienda);
 		String lastCodice = (String)query.uniqueResult();
 
 		Integer max = null;
 		if(lastCodice == null) {
 			max = 1;
 		} else {
 			max = Integer.parseInt(lastCodice) + 1;
 		}
 
 		String matricola = String.format("%05d", max);
 		logger.debug("Next matricola: " + matricola);
 		return matricola;
 	}
 
 
 	@Override
 	public void setFoto(Integer id, byte[] foto) {
 
 		Addetto addetto = retrieve(id);
 		if(addetto == null) {
 			String message = String.format("It has not been possible to retrieve specified entity: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		// Audit call for the update operation.
 		//
 		AuditUtil.log(Operation.Update, Snapshot.Source, addetto);
 
 		addetto.setFoto(foto);
 		session.update(addetto);
 		logger.debug(String.format("Picture successfully uploaded for specified addetto: %s", id));
 
 		// Audit call for the update operation.
 		//
 		AuditUtil.log(Operation.Update, Snapshot.Destination, addetto);
 	}
 
 
 	@Override
 	public Addetto setNote(Integer id, String note) {
 
 		Addetto addetto = retrieve(id);
 		if(addetto == null) {
 			String message = String.format("It has not been possible to retrieve specified entity: %d", id);
 			logger.info(message);
 			throw new NotFoundException(message);
 		}
 
 		// Audit call for the update operation.
 		//
 		AuditUtil.log(Operation.Update, Snapshot.Source, addetto);
 
 		// Update the record.
 		//
 		Date now = new Date();
 		addetto.setNote(note);
 		addetto.setUltimaModifica(now);
 		session.update(addetto);
 
 		// Audit call for the update operation.
 		//
 		AuditUtil.log(Operation.Update, Snapshot.Destination, addetto);
 
 		return addetto;
 	}
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Object[]> listServiziSettimanaAddetto(Integer codiceAddetto, Date dataMattinale) {
 
 		Calendar c = new GregorianCalendar();
 		c.setTime(dataMattinale);
 		c = DateUtils.truncate(c, Calendar.DATE);
 		Date dataInizio = DateUtils.addDays(c.getTime(), 2 - c.get(Calendar.DAY_OF_WEEK));
 		Date dataFine = DateUtils.addWeeks(dataInizio, 1);
		
 		String hql =
 				"select distinct(ser) " +
 				"from Servizio ser " +
 				"inner join fetch ser.addetto add " +
 				"left join fetch ser.ods ods " +
 				"left join fetch ser.causaleOds cau " +
 				"where add.id = :codiceAddetto " +
 				"and ser.dataMattinale >= :dataInizio " +
 				"and ser.dataMattinale < :dataFine ";
 		Query query = session.createQuery(hql);
 		query.setParameter("codiceAddetto", codiceAddetto);
 		query.setParameter("dataInizio", dataInizio);
 		query.setParameter("dataFine", dataFine);
 
 		List<Servizio> list = query.list();
 
 		List<Object[]> result = new ArrayList<Object[]>();
 
 		for(Servizio servizio : list) {
 
 			String descrizione = null;
			
 			if(servizio.getOds() != null) {
 				descrizione = servizio.getOds().getAlias();
 			} else if(servizio.getCausaleOds() != null) {
 				descrizione = servizio.getCausaleOds().getDescrizione();
 			}
			
			Object[] row = new Object[] { 
					servizio.getDataMattinale(), 
					servizio.getOrarioDa(), 
 					servizio.getOrarioA(),
 					descrizione };
 			result.add(row);
 		}
 
 		logger.debug("listServiziSettimanaAddetto returned: " + result);
 		return result;
 	}
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Object[]> listAddettiAndServizi(Integer codiceReparto, Date dataMattinale) {
 
 		String hql =
 				"select distinct(add) " +
 				"from Addetto add " +
 				"inner join add.reparti are " +
 				"inner join are.reparto rep " +
 				"where rep.id = :codiceReparto " +
 				"and add.attivo = true ";
 		Query query = session.createQuery(hql);
 		query.setParameter("codiceReparto", codiceReparto);
 
 		List<Addetto> list = query.list();
 
 		List<Object[]> result = new ArrayList<Object[]>();
 
 		hql =
 				"from Servizio ser " +
 				"left join fetch ser.causaleOds cod " +
 				"where ser.addetto.id = :codiceAddetto " +
 				"and ser.dataMattinale = :dataMattinale ";
 		Query queryServizio = session.createQuery(hql);
 
 		hql =
 				"from SistemaLavoro sla " +
 				"inner join fetch sla.tipoSistemaLavoro tsl " +
 				"where sla.addetto.id = :codiceAddetto " +
				"and sla.validoA is null " +
 				"order by sla.validoDa desc ";
 		Query querySistemaLavoro = session.createQuery(hql);
 		querySistemaLavoro.setMaxResults(1);
 
 		for(Addetto addetto : list) {
 
 			session.evict(addetto);
 
 			// Retrieve servizi.
 			//
 			queryServizio.setParameter("codiceAddetto", addetto.getId());
 			queryServizio.setParameter("dataMattinale", dataMattinale);
 			List<Servizio> servizi = queryServizio.list();
 
 			// Calculated worked period and keep it in milliseconds.
 			//
 			Long workedMillis = 0L;
 			for(Servizio servizio : servizi) {
 
 				Date orarioDa = servizio.getOrarioDa();
 				Date orarioA = servizio.getOrarioA();
 
 				if(orarioDa == null && orarioA == null) {
 					continue;
 				}
 
 				if(
 					(orarioDa == null && orarioA != null) ||
 					(orarioA == null && orarioDa != null)) {
 					logger.error("Both or none between orarioDa and orarioA must be present.");
 					throw new RuntimeException("Both or none between orarioDa and orarioA must be present.");
 				}
 
 				if(orarioA.before(orarioDa)) {
 					orarioA = DateUtils.addDays(orarioA, 1);
 				}
 
 				workedMillis += (orarioA.getTime() - orarioDa.getTime());
 			}
 
 			// Retrieve sistema lavoro.
 			//
 			querySistemaLavoro.setParameter("codiceAddetto", addetto.getId());
 			SistemaLavoro sistemaLavoro = (SistemaLavoro)querySistemaLavoro.uniqueResult();
 
 			Object[] row = new Object[] { addetto, servizi, workedMillis, sistemaLavoro };
 			result.add(row);
 		}
 
 		logger.debug("listAddettiAndServizi returned: " + result);
 		return result;
 	}
 }
