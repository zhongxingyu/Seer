 package org.dialogix.session;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import javax.ejb.Stateless;
 import org.dialogix.entities.*;
 import javax.persistence.*;
 import org.dialogix.beans.InstrumentSessionResultBean;
 import org.dialogix.beans.InstrumentVersionView;
 
 /**
  * This interface is for running instruments which already exist within the database.
  */
 @Stateless  
 public class DialogixEntitiesFacade implements DialogixEntitiesFacadeRemote, DialogixEntitiesFacadeLocal {
 
     @PersistenceContext
     private EntityManager em;
     
     /**
      * Get list of ActionTypes for use locally
      * @return
      */
     public List<ActionType> getActionTypes() {
         return em.createQuery("select object(o) from ActionType o").getResultList();                
     }
     
     /**
      * Get list of NullFlavors for use locally
      * @return
      */
     public List<NullFlavor> getNullFlavors() {
         return em.createQuery("select object(o) from NullFlavor o").getResultList();        
     }
     
     /**
      * Get list of NullFlavorChange for use locally
      * @return
      */
     public List<NullFlavorChange> getNullFlavorChanges() {
         return em.createQuery("select object(o) from NullFlavorChange o").getResultList();        
     }    
 
     /**
      * Update values of running InstrumentSession
      * @param instrumentSession
      */
     public void merge(InstrumentSession instrumentSession) {
         em.merge(instrumentSession);
     }
 
     /**
      * Initialize an InstrumentSession
      * @param instrumentSession
      */
     public void persist(InstrumentSession instrumentSession) {
         em.persist(instrumentSession);
     }
 
     /**
      * Load an instance of InstrumentVersion from the database.  This will be used to create an InstrumentSession.
      * @param name
      * @param major
      * @param minor
      * @return null if the InstrumentVersion doesn't exist
      */
     public InstrumentVersion getInstrumentVersion(String name, String major, String minor) {
         InstrumentVersion _instrumentVersion = null;
         Query query = em.createQuery("SELECT iv FROM InstrumentVersion iv JOIN iv.instrumentId i WHERE i.instrumentName = :title AND iv.versionString = :versionString");
         String version = major.concat(".").concat(minor);
         query.setParameter("versionString", version);
         query.setParameter("title", name);
         try {
             List list = query.getResultList();
             _instrumentVersion = (InstrumentVersion) list.get(0);
         } catch (NoResultException e) {
             return null;
         } catch (IndexOutOfBoundsException e) {
             return null;
         }
         if (_instrumentVersion == null) {
             return null;
         }
         return _instrumentVersion;
     }    
     
     /**
      * FIXME - may need InstrumentVersion object, not Integer
      * @param instrumentVersionId
      * @return
      */
     public InstrumentVersion getInstrumentVersion(Long instrumentVersionId) {
         return em.find(org.dialogix.entities.InstrumentVersion.class, instrumentVersionId);
     }
     
     /**
      * Get an instrument session by its  Id
      * @param instrumentSessionId
      * @return
      */
     public InstrumentSession getInstrumentSession(Long instrumentSessionId) {
         return em.find(org.dialogix.entities.InstrumentSession.class, instrumentSessionId);
     }
     
     /**
      * Get and itemUsage by its id
      * @param itemUsageId
      * @return
      */
     public ItemUsage getItemUsage(Long itemUsageId) {
         return em.find(org.dialogix.entities.ItemUsage.class, itemUsageId);
     }
     
     /**
      * Get list of Instruments - hopefully using shallow searching
      * @return
      */
     public List<InstrumentVersion> getInstrumentVersionCollection() {
         return em.createQuery("select object(o) from InstrumentVersion o").getResultList();    
     }
     
     public List<ItemUsage> getItemUsages(Long instrumentSessionId) {
         return em.createQuery("select object(iu) from ItemUsage iu JOIN iu.dataElementId de JOIN de.instrumentSessionId ins " +
             "where ins.instrumentSessionId = :instrumentSessionId " +
             "and iu.displayNum > 0" +
            "order by iu.itemUsageId, iu.itemUsageSequence").
             setParameter("instrumentSessionId", instrumentSessionId).
             getResultList();
     }
     
     /**
      * Get list of all available instruments, showing title,  version, versionId, and number of started sessions
      * @return
      */
     public List<InstrumentVersionView> getInstrumentVersions() {
         List<InstrumentVersionView> instrumentVersionViewList = new ArrayList<InstrumentVersionView> ();
         String q = 
             "select  " +
             "	iv.instrument_version_id,  " +
             "	i.instrument_name as title,  " +
             "	iv.version_string as version,  " +
             "	ins.num_sessions, " +
             "   h.num_equations, " +
             "   h.num_questions, " + 
             "   h.num_branches, " +
             "   h.num_languages, " + 
             "   h.num_tailorings, " + 
             "   h.num_vars, " +
             "   h.num_groups, " +
             "   h.num_instructions, " +
             "   'empty' as instrument_version_file_name " +
             " from instrument i, instrument_hash h, instrument_version iv, " +
             "	(select iv2.instrument_version_id," +
             "		count(ins2.instrument_session_id) as  num_sessions" +
             "		from instrument_version iv2 left join instrument_session ins2" +
             "		on iv2.instrument_version_id = ins2.instrument_version_id" +
             "		group by iv2.instrument_version_id" +
             "		order by iv2.instrument_version_id) ins" +
             " where iv.instrument_id = i.instrument_id    " +
             "	and iv.instrument_version_id = ins.instrument_version_id  " +
             "   and iv.instrument_hash_id = h.instrument_hash_id " +
             " order by title, version";
         Query query = em.createNativeQuery(q);
         List<Vector> results = query.getResultList();
         if (results == null) {
             return null;
         }
         Iterator<Vector> iterator = results.iterator();
         while (iterator.hasNext()) {
             Vector vector = iterator.next();
             instrumentVersionViewList.add(new InstrumentVersionView(
                 (Long) vector.get(0),   // instrumentVersionId
                 (String) vector.get(1), // instrumentName
                 (String) vector.get(2), // instrumentVersion
                 (Long) vector.get(3), // numSessions
                 (Integer) vector.get(4), // numEquations
                 (Integer) vector.get(5), // numQuestions
                 (Integer) vector.get(6), // numBranches
                 (Integer) vector.get(7), // numLanguages
                 (Integer) vector.get(8), // numTailorings
                 (Integer) vector.get(9), // numVars
                 (Integer) vector.get(10), // numGroups
                 (Integer) vector.get(11), // numInstructions
                 (String) vector.get(12) // instrumentVersionFileName
                 )); 
         }
         return instrumentVersionViewList;
     }    
     
     /**
      * Retrieve an InstrumentSession by its filename.  
      * TODO:  Once the system is fully databased, this won't be needed.
      * @param name
      * @return  null if the session doesn't exist
      */
     public InstrumentSession findInstrumentSessionByName(String name) {
         if (name == null || name.trim().length() == 0) {
             return null;
         }
         String q = "SELECT v FROM InstrumentSession v WHERE v.instrumentSessionFileName = :instrumentSessionFileName";
         Query query = em.createQuery(q);
         query.setParameter("instrumentSessionFileName", name);
         InstrumentSession instrumentSession = null;
         try {
             instrumentSession = (InstrumentSession) query.getSingleResult();
         } catch (NoResultException e) {
             return null;
         } catch (NonUniqueResultException e) {
             return null;
         }
         return instrumentSession;
     } 
     
     /**
      * Find an existing variable name by its name
      * @param name
      * @return
      */
     public VarName findVarNameByName(String name) {
         if (name == null || name.trim().length() == 0) {
             return null;
         }
         String q = "SELECT v FROM VarName v WHERE v.varName = :varName";
         Query query = em.createQuery(q);
         query.setParameter("varName", name);
         VarName varName = null;
         try {
             varName = (VarName) query.getSingleResult();
         } catch (NoResultException e) {
             return null;
         } catch (NonUniqueResultException e) {
             return null;
         }
         return varName;        
     }
     
     /**
      * Extract raw results, including sessionId, dataElementSequence, varNameId, varName, answerCode, answerString, and nullFlavor.
      * @param instrumentVersionId
      * @param inVarNameIds
      * @param sortByName
      * @return
      */
     public List<InstrumentSessionResultBean> getFinalInstrumentSessionResults(Long instrumentVersionId, String inVarNameIds, Boolean sortByName) {
         String q =
             "SELECT " +
             "	deiu.instrument_session_id, " +
             "	deiu.data_element_sequence, " +
             "	vn.var_name_id," +
             "	vn.var_name," +
             "	deiu.answer_code, " +
             "	deiu.null_flavor_id " +
             "FROM var_name vn, " +
             "	(SELECT de.instrument_session_id, " +
             "			de.var_name_id," +
             "			de.data_element_sequence, " +
             "			iu.answer_code, " +
             "			iu.null_flavor_id" +
             "		FROM data_element de LEFT OUTER JOIN item_usage iu" +
             "		ON iu.data_element_id = de.data_element_id" +
             "		AND iu.item_visit = de.item_visits" +
             "		WHERE de.data_element_sequence > 0" +
             "		AND de.instrument_session_id" +
             "			IN (" +
             "			SELECT instrument_session_id" +
             "			FROM instrument_session" +
             "			WHERE instrument_version_id = " + instrumentVersionId +
             "			)" +
             ((inVarNameIds != null) ? " AND de.var_name_id in " + inVarNameIds : "") +
             "	) deiu " +
             "WHERE deiu.var_name_id = vn.var_name_id " +
             "ORDER BY deiu.instrument_session_id, " +
             ((sortByName == true) ? "vn.var_name" : "deiu.data_element_sequence");
         Query query = em.createNativeQuery(q);
         List<Vector> results = query.getResultList();
         if (results == null) {
             return null;
         }
         Iterator<Vector> iterator = results.iterator();
         ArrayList<InstrumentSessionResultBean> isrb = new ArrayList<InstrumentSessionResultBean>();
         while (iterator.hasNext()) {
             Vector v = iterator.next();
             isrb.add(new InstrumentSessionResultBean((Long) v.get(0), 
                 (Integer) v.get(1), 
                 (Long) v.get(2), 
                 (String) v.get(3), 
                 (String) v.get(4), 
                 (Integer) v.get(5)));
         }
         return isrb;
     }
     
     public List<InstrumentSession> getInstrumentSessions(InstrumentVersion instrumentVersionId) {
         return em.
             createQuery("select object(o) from InstrumentSession o where o.instrumentVersionId = :instrumentVersionId").
             setParameter("instrumentVersionId",instrumentVersionId).
             getResultList();            
     }    
 }
