 package it.ivncr.erp.service.utente;
 
 import it.ivncr.erp.model.accesso.Ruolo;
 import it.ivncr.erp.model.accesso.Utente;
 import it.ivncr.erp.model.generale.Azienda;
 import it.ivncr.erp.service.EntityService;
 import it.ivncr.erp.service.QueryResult;
 
 import java.util.List;
 
 public interface UtenteService extends EntityService<Utente> {
 
 	QueryResult<Utente> list(
 			Integer id,
 			String username,
 			String nome,
 			String cognome,
 			String note,
 			Boolean attivo,
 			Integer offset,
 			Integer limit);
 
 	Utente retrieve(Integer id);
 	Utente retrieveByUsername(String username);
 	Utente retrieveWithAccountEmail(Integer id);
 
 	void delete(Integer id);
 
 	Utente create(
 			String username,
 			String password,
 			String nome,
 			String cognome,
 			String note);
 
 	Utente update(
 			Integer id,
 			String username,
 			String nome,
 			String cognome,
 			String note);
 	Utente updateAccountEmail(
 			Integer id,
 			String account,
 			String password);
 
 	Utente changePassword(
 			Integer id,
 			String password);
 
 	Utente activate(Integer id);
 
 	Utente deactivate(Integer id);
 
 	Utente updateLastLogonTimestamp(Integer id);
 
 	List<Ruolo> listRuoli(Integer id);
 
 	Ruolo addRuolo(Integer utenteId, Integer ruoloId);
 
 	void deleteRuolo(Integer utenteId, Integer ruoloId);
 
 	void setRuoli(Integer utenteId, Integer[] ruoliId);
 
 	List<Azienda> listAziende(Integer id);
 
	Azienda retrieveDefaultAzienda(Integer id);
 
 	void setAziende(Integer utenteId, Integer[] aziendeId, Integer preferita);
 }
