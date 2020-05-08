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
 
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonImage;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 public class TaxonDAO {
 	
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 	
 	public static Taxon getTaxon(Session session, int id) throws PersistentException {
 		Taxon taxon = SISPersistentManager.instance().getObject(session, Taxon.class, id);
 		if (taxon != null && taxon.getState() == Taxon.ACTIVE)
 			return taxon;
 		return null;
 	}
 	
 	/*public static Taxon getTaxonNonLazily(int id) throws PersistentException {
 		Taxon taxon = getTaxon(session, id); 
 		if (taxon != null) {
 			Hibernate.initialize(taxon.getReference());
 			Hibernate.initialize(taxon.getEdits());
 			Hibernate.initialize(taxon.getAssessments());
 			Hibernate.initialize(taxon.getNotes());
 			Hibernate.initialize(taxon.getCommonNames());
 			Hibernate.initialize(taxon.getSynonyms());
 		}
 		return taxon;
 	}*/
 	
 	public static Taxon[] getTrashedTaxa(Session session) throws PersistentException {
 		try {
 			TaxonCriteria criteria = new TaxonCriteria(session);
 			criteria.state.eq(Taxon.DELETED);
 			return listTaxonByCriteria(criteria);
 		} catch (Exception e) {
 			Debug.println(e);
 			throw new PersistentException(e);
 		}
 	}
 	
 	
 	public static Taxon getTrashedTaxon(Session session, int id) throws PersistentException {
		Taxon taxon = getTaxon(session, id);
 		if (taxon != null && taxon.getState() == Taxon.DELETED)
 			return taxon;
 		return null;
 	}
 	
 	public static Taxon[] getTaxonByCriteria(TaxonCriteria criteria) {
 		criteria.state.eq(Taxon.ACTIVE);
 		return listTaxonByCriteria(criteria);		
 	}
 	
 	public static Taxon[] getTrashedTaxaByCriteria(TaxonCriteria criteria) {
 		criteria.state.eq(Taxon.DELETED);
 		return listTaxonByCriteria(criteria);		
 	}
 	/* THINGS I HAVE ADDED... IF YOU REGENERATE, MUST ALSO COPY THIS */
 	
 	/*protected static Taxon loadTaxonByORMID(int id) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return loadTaxonByORMID(session, id);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	protected static Taxon getTaxonByORMID(int id) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return getTaxonByORMID(session, id);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	protected static Taxon loadTaxonByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return loadTaxonByORMID(session, id, lockMode);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	protected static Taxon getTaxonByORMID(int id, org.hibernate.LockMode lockMode) throws PersistentException {
 		Session session = SISPersistentManager.instance().openSession();
 		try {
 			return getTaxonByORMID(session, id, lockMode);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		} finally {
 			session.close();
 		}
 	}
 	
 	protected static Taxon loadTaxonByORMID(Session session, int id) throws PersistentException {
 		try {
 			return (Taxon) session.load(Taxon.class, new Integer(id));
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	
 	protected static Taxon getTaxonByORMID(Session session, int id) throws PersistentException {
 		try {
 			return (Taxon) session.get(Taxon.class, new Integer(id));
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}*/
 	
 	public static Taxon loadTaxonByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
 		try {
 			return (Taxon) session.load(Taxon.class, new Integer(id), lockMode);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static Taxon getTaxonByORMID(Session session, int id, org.hibernate.LockMode lockMode) throws PersistentException {
 		try {
 			return (Taxon) session.get(Taxon.class, new Integer(id), lockMode);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	
 	protected static Taxon[] listTaxonByQuery(Session session, String condition, String orderBy) throws PersistentException {
 		StringBuffer sb = new StringBuffer("From Taxon as Taxon");
 		if (condition != null)
 			sb.append(" Where ").append(condition);
 		if (orderBy != null)
 			sb.append(" Order By ").append(orderBy);
 		try {
 			Query query = session.createQuery(sb.toString());
 			List list = query.list();
 			return (Taxon[]) list.toArray(new Taxon[list.size()]);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	
 	protected static Taxon[] listTaxonByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
 		StringBuffer sb = new StringBuffer("From Taxon as Taxon");
 		if (condition != null)
 			sb.append(" Where ").append(condition);
 		if (orderBy != null)
 			sb.append(" Order By ").append(orderBy);
 		try {
 			Query query = session.createQuery(sb.toString());
 			query.setLockMode("this", lockMode);
 			List list = query.list();
 			return (Taxon[]) list.toArray(new Taxon[list.size()]);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	
 	protected static Taxon loadTaxonByQuery(Session session, String condition, String orderBy) throws PersistentException {
 		Taxon[] taxons = listTaxonByQuery(session, condition, orderBy);
 		if (taxons != null && taxons.length > 0)
 			return taxons[0];
 		else
 			return null;
 	}
 	
 	protected static Taxon loadTaxonByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
 		Taxon[] taxons = listTaxonByQuery(session, condition, orderBy, lockMode);
 		if (taxons != null && taxons.length > 0)
 			return taxons[0];
 		else
 			return null;
 	}
 	
 	protected static java.util.Iterator iterateTaxonByQuery(Session session, String condition, String orderBy) throws PersistentException {
 		StringBuffer sb = new StringBuffer("From Taxon as Taxon");
 		if (condition != null)
 			sb.append(" Where ").append(condition);
 		if (orderBy != null)
 			sb.append(" Order By ").append(orderBy);
 		try {
 			Query query = session.createQuery(sb.toString());
 			return query.iterate();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	
 	protected static java.util.Iterator iterateTaxonByQuery(Session session, String condition, String orderBy, org.hibernate.LockMode lockMode) throws PersistentException {
 		StringBuffer sb = new StringBuffer("From Taxon as Taxon");
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
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	
 	protected static Taxon createTaxon() {
 		return new Taxon();
 	}
 	
 	/*public static void save(Taxon taxon) throws PersistentException {
 		try {
 			SISPersistentManager.instance().saveObject(session, taxon);
 		} catch (Exception e) {
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static boolean delete(Taxon taxon) throws PersistentException {
 		try {
 			SISPersistentManager.instance().deleteObject(session, taxon);
 			return true;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	*/
 	
 	public static boolean deleteAndDissociate(Taxon taxon, Session session)throws PersistentException {
 		try {
 			if(taxon.getTaxonLevel() != null) {
 				taxon.getTaxonLevel().getTaxa().remove(taxon);
 			}
 			
 			if(taxon.getTaxonStatus() != null) {
 				taxon.getTaxonStatus().getTaxa().remove(taxon);
 			}
 			
 			if(taxon.getParent() != null) {
 				taxon.getParent().getChildren().remove(taxon);
 			}
 			
 			WorkingSet[] lWorking_sets = (WorkingSet[])taxon.getWorking_set().toArray(new WorkingSet[taxon.getWorking_set().size()]);
 			for(int i = 0; i < lWorking_sets.length; i++) {
 				lWorking_sets[i].getTaxon().remove(taxon);
 			}
 			Reference[] lReferences = (Reference[])taxon.getReference().toArray(new Reference[taxon.getReference().size()]);
 			for(int i = 0; i < lReferences.length; i++) {
 				lReferences[i].getTaxon().remove(taxon);
 			}
 			Taxon[] children = (Taxon[])taxon.getChildren().toArray(new Taxon[taxon.getChildren().size()]);
 			for(int i = 0; i < children.length; i++) {
 				children[i].setParent(null);
 			}
 			Edit[] edits = (Edit[])taxon.getEdits().toArray(new Edit[taxon.getEdits().size()]);
 			for(int i = 0; i < edits.length; i++) {
 				edits[i].getTaxon().remove(taxon);
 			}
 			Notes[] notes = (Notes[])taxon.getNotes().toArray(new Notes[taxon.getNotes().size()]);
 			for(int i = 0; i < notes.length; i++) {
 				NotesDAO.deleteAndDissociate(notes[i], session);
 			}
 			Assessment[] lAssessmentss = (Assessment[])taxon.getAssessments().toArray(new Assessment[taxon.getAssessments().size()]);
 			for(int i = 0; i < lAssessmentss.length; i++) {
 				AssessmentDAO.deleteAndDissociate(lAssessmentss[i], session);
 			}
 			Synonym[] lSynonymss = (Synonym[])taxon.getSynonyms().toArray(new Synonym[taxon.getSynonyms().size()]);
 			for(int i = 0; i < lSynonymss.length; i++) {
 				SynonymDAO.deleteAndDissociate(lSynonymss[i], session);
 			}
 			CommonName[] lCommonNamess = (CommonName[])taxon.getCommonNames().toArray(new CommonName[taxon.getCommonNames().size()]);
 			for(int i = 0; i < lCommonNamess.length; i++) {
 				CommonNameDAO.deleteAndDissociate(lCommonNamess[i], session);
 			}
 			TaxonImage[] lImagess = (TaxonImage[])taxon.getImages().toArray(new TaxonImage[taxon.getImages().size()]);
 			for (int i = 0; i < lImagess.length; i++) {
 				session.delete(lImagess[i]);
 			}
 			
 			if(taxon.getInfratype() != null) {
 				taxon.getInfratype().getTaxa().remove(taxon);
 			}
 			
 			try {
 				session.delete(taxon);
 				return true;
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	/**
 	
 	public static boolean deleteAndDissociate(Taxon taxon, org.orm.Session session)throws PersistentException {
 		try {
 			if(taxon.getTaxonLevel() != null) {
 				taxon.getTaxonLevel().getTaxa().remove(taxon);
 			}
 			
 			if(taxon.getTaxonStatus() != null) {
 				taxon.getTaxonStatus().getTaxa().remove(taxon);
 			}
 			
 			if(taxon.getParent() != null) {
 				taxon.getParent().getChildren().remove(taxon);
 			}
 			
 			WorkingSet[] lWorking_sets = (WorkingSet[])taxon.getWorking_set().toArray(new WorkingSet[taxon.getWorking_set().size()]);
 			for(int i = 0; i < lWorking_sets.length; i++) {
 				lWorking_sets[i].getTaxon().remove(taxon);
 			}
 			Reference[] lReferences = (Reference[])taxon.getReference().toArray(new Reference[taxon.getReference().size()]);
 			for(int i = 0; i < lReferences.length; i++) {
 				lReferences[i].getTaxon().remove(taxon);
 			}
 			Taxon[] lParents = (Taxon[])taxon.getChildren().toArray(new Taxon[taxon.getChildren().size()]);
 			for(int i = 0; i < lParents.length; i++) {
 				lParents[i].setParent(null);
 			}
 			Edit[] lEditss = (Edit[])taxon.getEdits().toArray(new Edit[taxon.getEdits().size()]);
 			for(int i = 0; i < lEditss.length; i++) {
 				lEditss[i].getTaxon().remove(taxon);
 			}
 			Notes[] lNotess = (Notes[])taxon.getNotes().toArray(new Notes[taxon.getNotes().size()]);
 			for(int i = 0; i < lNotess.length; i++) {
 				lNotess[i].getTaxon().remove(taxon);
 			}
 			Assessment[] lAssessmentss = (Assessment[])taxon.getAssessments().toArray(new Assessment[taxon.getAssessments().size()]);
 			for(int i = 0; i < lAssessmentss.length; i++) {
 				lAssessmentss[i].setTaxon(null);
 			}
 			Synonym[] lSynonymss = (Synonym[])taxon.getSynonyms().toArray(new Synonym[taxon.getSynonyms().size()]);
 			for(int i = 0; i < lSynonymss.length; i++) {
 				lSynonymss[i].setTaxon(null);
 			}
 			CommonName[] lCommonNamess = (CommonName[])taxon.getCommonNames().toArray(new CommonName[taxon.getCommonNames().size()]);
 			for(int i = 0; i < lCommonNamess.length; i++) {
 				lCommonNamess[i].setTaxon(null);
 			}
 			if(taxon.getInfratype() != null) {
 				taxon.getInfratype().setTaxon(null);
 			}
 			
 			try {
 				session.delete(taxon);
 				return true;
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	**/
 	
 	/*public static boolean refresh(Taxon taxon) throws PersistentException {
 		try {
 			SISPersistentManager.instance().getSession().refresh(taxon);
 			return true;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}
 	
 	public static boolean evict(Taxon taxon) throws PersistentException {
 		try {
 			SISPersistentManager.instance().getSession().evict(taxon);
 			return true;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new PersistentException(e);
 		}
 	}*/
 	
 	protected static Taxon loadTaxonByCriteria(TaxonCriteria taxonCriteria) {
 		Taxon[] taxons = listTaxonByCriteria(taxonCriteria);
 		if(taxons == null || taxons.length == 0) {
 			return null;
 		}
 		return taxons[0];
 	}
 	
 	protected static Taxon[] listTaxonByCriteria(TaxonCriteria taxonCriteria) {
 		return taxonCriteria.listTaxon();
 	}
 }
