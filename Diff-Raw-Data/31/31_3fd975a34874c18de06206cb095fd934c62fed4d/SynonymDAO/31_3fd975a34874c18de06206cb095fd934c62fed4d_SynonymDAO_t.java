 package org.iucn.sis.server.api.persistance;
 /**
  * "Visual Paradigm: DO NOT MODIFY THIS FILE!"
  * 
  * This is an automatic generated file. It will be regenerated every time 
  * you generate persistence class.
  * 
  * Modifying its content may cause the program not work, or your work may lost.
  */
 
 /**
  * Licensee: 
  * License Type: Evaluation
  */
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Synonym;
 
 public class SynonymDAO {
 	/*public static Synonym loadSynonymByORMID(int id) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return loadSynonymByORMID(session, id);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	public static Synonym getSynonymByORMID(int id) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return getSynonymByORMID(session, id);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	public static Synonym loadSynonymByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return loadSynonymByORMID(session, id, lockMode);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	public static Synonym getSynonymByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return getSynonymByORMID(session, id, lockMode);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}*/
 	
 	public static Synonym loadSynonymByORMID(Session session, int id) throws PersistentException {
 		try {
 			return (Synonym) session.load(Synonym.class, new Integer(id));
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static Synonym getSynonymByORMID(Session session, int id) throws PersistentException {
 		try {
 			return (Synonym) session.get(Synonym.class, new Integer(id));
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static Synonym loadSynonymByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
 		try {
 			return (Synonym) session.load(Synonym.class, new Integer(id), lockMode);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static Synonym getSynonymByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
 		try {
 			return (Synonym) session.get(Synonym.class, new Integer(id), lockMode);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	/*public static Synonym[] listSynonymByQuery(String condition, String orderBy) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return listSynonymByQuery(session, condition, orderBy);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	public static Synonym[] listSynonymByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return listSynonymByQuery(session, condition, orderBy, lockMode);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}*/
 	
 	public static Synonym[] listSynonymByQuery(Session session, String condition, String orderBy) throws PersistentException {
 		StringBuffer sb = new StringBuffer("From Synonym as Synonym");
 		if (condition != null)
 			sb.append(" Where ").append(condition);
 		if (orderBy != null)
 			sb.append(" Order By ").append(orderBy);
 		try {
 			Query query = session.createQuery(sb.toString());
 			List list = query.list();
 			return (Synonym[]) list.toArray(new Synonym[list.size()]);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static Synonym[] listSynonymByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
 		StringBuffer sb = new StringBuffer("From Synonym as Synonym");
 		if (condition != null)
 			sb.append(" Where ").append(condition);
 		if (orderBy != null)
 			sb.append(" Order By ").append(orderBy);
 		try {
 			Query query = session.createQuery(sb.toString());
 			query.setLockMode("this", lockMode);
 			List list = query.list();
 			return (Synonym[]) list.toArray(new Synonym[list.size()]);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	/*public static Synonym loadSynonymByQuery(String condition, String orderBy) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return loadSynonymByQuery(session, condition, orderBy);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	public static Synonym loadSynonymByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return loadSynonymByQuery(session, condition, orderBy, lockMode);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}*/
 	
 	public static Synonym loadSynonymByQuery(Session session, String condition, String orderBy) throws PersistentException {
 		Synonym[] synonyms = listSynonymByQuery(session, condition, orderBy);
 		if (synonyms != null && synonyms.length > 0)
 			return synonyms[0];
 		else
 			return null;
 	}
 	
 	public static Synonym loadSynonymByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
 		Synonym[] synonyms = listSynonymByQuery(session, condition, orderBy, lockMode);
 		if (synonyms != null && synonyms.length > 0)
 			return synonyms[0];
 		else
 			return null;
 	}
 	
 	/*public static java.util.Iterator iterateSynonymByQuery(String condition, String orderBy) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return iterateSynonymByQuery(session, condition, orderBy);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	public static java.util.Iterator iterateSynonymByQuery(String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return iterateSynonymByQuery(session, condition, orderBy, lockMode);
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}*/
 	
 	public static java.util.Iterator iterateSynonymByQuery(Session session, String condition, String orderBy) throws PersistentException {
 		StringBuffer sb = new StringBuffer("From Synonym as Synonym");
 		if (condition != null)
 			sb.append(" Where ").append(condition);
 		if (orderBy != null)
 			sb.append(" Order By ").append(orderBy);
 		try {
 			Query query = session.createQuery(sb.toString());
 			return query.iterate();
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static java.util.Iterator iterateSynonymByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
 		StringBuffer sb = new StringBuffer("From Synonym as Synonym");
 		if (condition != null)
 			sb.append(" Where ").append(condition);
 		if (orderBy != null)
 			sb.append(" Order By ").append(orderBy);
 		try {
 			Query query = session.createQuery(sb.toString());
 			query.setLockMode("this", lockMode);
 			return query.iterate();
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static Synonym createSynonym() {
 		return new Synonym();
 	}
 	
 	/*public static boolean save(Synonym synonym) throws PersistentException {
 		try {
 			SISPersistentManager.instance().saveObject(session, synonym);
 			return true;
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static boolean delete(Synonym synonym) throws PersistentException {
 		try {
 			SISPersistentManager.instance().deleteObject(session, synonym);
 			return true;
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}*/
 	
 	public static boolean deleteAndDissociate(Synonym synonym, Session session)throws PersistentException {
 		try {
			synonym.setTaxon_level(null);
			/*
			 * TODO: not sure if the below is necessary, but it is 
			 * incredibly slow for obvious reasons.  Removing it 
			 * does not cause the delete to fail, though...
			 */
			/*if(synonym.getTaxon_level() != null) {
 				synonym.getTaxon_level().getSynonyms().remove(synonym);
			}*/
 			
 			if(synonym.getTaxon() != null) {
 				synonym.getTaxon().getSynonyms().remove(synonym);
 				synonym.setTaxon(null);
 			}
 			
 			Notes[] lNotess = (Notes[])synonym.getNotes().toArray(new Notes[synonym.getNotes().size()]);
 			for(int i = 0; i < lNotess.length; i++) {
 				lNotess[i].getSynonyms().remove(synonym);
 			}
			
 			Reference[] lReferences = (Reference[])synonym.getReference().toArray(new Reference[synonym.getReference().size()]);
 			for(int i = 0; i < lReferences.length; i++) {
 				lReferences[i].getSynonym().remove(synonym);
 			}
			
 			try {
 				session.delete(synonym);
 				return true;
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		catch(Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 		
 	/*public static boolean refresh(Synonym synonym) throws PersistentException {
 		try {
 			SISPersistentManager.instance().getSession().refresh(synonym);
 			return true;
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static boolean evict(Synonym synonym) throws PersistentException {
 		try {
 			SISPersistentManager.instance().getSession().evict(synonym);
 			return true;
 		}
 		catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}*/
 	
 	public static Synonym loadSynonymByCriteria(SynonymCriteria synonymCriteria) {
 		Synonym[] synonyms = listSynonymByCriteria(synonymCriteria);
 		if(synonyms == null || synonyms.length == 0) {
 			return null;
 		}
 		return synonyms[0];
 	}
 	
 	public static Synonym[] listSynonymByCriteria(SynonymCriteria synonymCriteria) {
 		return synonymCriteria.listSynonym();
 	}
 }
