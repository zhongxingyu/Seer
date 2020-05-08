 package org.makumba.aether.percolation;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.makumba.aether.AetherEvent;
 import org.makumba.aether.PercolationException;
 import org.makumba.aether.UserTypes;
 import org.makumba.aether.model.InitialPercolationRule;
 import org.makumba.aether.model.MatchedAetherEvent;
 import org.makumba.aether.model.PercolationRule;
 import org.makumba.aether.model.PercolationStep;
 import org.makumba.aether.model.RelationQuery;
 import org.makumba.parade.aether.ActionTypes;
 import org.makumba.parade.aether.ObjectTypes;
 import org.makumba.parade.init.InitServlet;
 
 /**
  * A rule-based percolator.<br>
  * 
  * This percolator uses 5 tables in order to perform percolation and provide ALE:
  * <ul>
  * <li>initial percolation rule table: set of rules to match an event and attribute an initial strength to it</li>
  * <li>percolation rule table: set of rules for that processes relations a {@link MatchedAetherEvent} can propagate
  * through, i.e. calculates consumption</li>
  * <li>percolation step table: stores each step of one event percolation</li>
  * <li>relation table: table holding relations between objects</li>
  * <li>focus table: table holding focus values per object and user. there's no nimbus equivalent because this is much
  * more complicated to re-compute after percolation (only one focus value is set per percolation vs. many nimbus values)</li>
  * </ul>
  * 
  * The percolator also runs two timers that update focus/nimbus values and perform garbage collection of
  * {@link PercolationStep}-s that are below MIN_ENERGY_LEVEL. The task execution interval is set by
  * GARBAGE_COLLECTION_INTERVAL and CURVE_UPDATE_INTERVAL.
  * 
  * @author Manuel Gay
  * 
  */
 public class RuleBasedPercolator implements Percolator {
 
     static final int MIN_ENERGY_LEVEL = 21;
 
     static final int GARBAGE_COLLECTION_INTERVAL = 1000 * 10 * 60; // 10 mins
 
     static final int CURVE_UPDATE_INTERVAL = 1000 * 15 * 60; // 15 mins
 
     static final int MAX_PERCOLATION_TIME = 5000000;
 
     private Logger logger = Logger.getLogger(RuleBasedPercolator.class);
 
     private SessionFactory sessionFactory;
 
     private PercolationStrategy strategy;
 
     private Timer garbageTimer;
 
     private Timer curveTimer;
 
     public static boolean rulesChanged = false;
     
     static ThreadLocal<Boolean> percolationLock = new ThreadLocal<Boolean>() {
         @Override
         protected Boolean initialValue() {
             return false;
         }
     };
 
     public void percolate(AetherEvent e) throws PercolationException {
         int startQueries = RelationQuery.getExecutedQueries();
         percolationLock.set(true);
         strategy.percolate(e, sessionFactory);
         percolationLock.set(false);
         int endQueries = RelationQuery.getExecutedQueries();
         logger.info("Percolation of event "+e.toString()+" needed "+(endQueries - startQueries)+ " queries to be executed");
     }
 
     private void checkInitialPercolationRules() {
         logger.info("Checking initial percolation rules...");
 
         Session s = null;
         Transaction tx = null;
         try {
             s = sessionFactory.openSession();
             tx = s.beginTransaction();
 
             List<InitialPercolationRule> rules = s.createQuery("from InitialPercolationRule r").list();
             logger.debug("Found " + rules.size() + " initial percolation rules");
             for (InitialPercolationRule r : rules) {
                 logger.debug(r.toString());
 
                 if (!ActionTypes.getActions().contains(r.getAction())) {
                     logger.warn("Initial percolation rule \"" + r.toString() + "\" has invalid action " + r.getAction()
                             + ". It will be ignored.");
                     r.setActive(false);
                 }
 
                 if (!UserTypes.getUserTypes().contains(r.getUserType())) {
                     logger.warn("Initial percolation rule \"" + r.toString() + "\" has invalid user type "
                             + r.getUserType() + ". It will be ignored.");
                     r.setActive(false);
                 }
 
                 if (!ObjectTypes.getObjectTypes().contains(r.getObjectType())) {
                     logger.warn("Initial percolation rule \"" + r.toString() + "\" has invalid object type "
                             + r.getObjectType() + ". It will be ignored.");
                     r.setActive(false);
                 }
 
             }
 
             tx.commit();
 
         } finally {
             if (s != null) {
                 s.close();
             }
         }
 
     }
 
     private void checkPercolationRules() {
         logger.info("Checking percolation rules...");
 
         Session s = null;
         Transaction tx = null;
         try {
             s = sessionFactory.openSession();
             tx = s.beginTransaction();
 
             List<PercolationRule> rules = s.createQuery("from PercolationRule r").list();
             logger.debug("Found " + rules.size() + " percolation rules");
             for (PercolationRule r : rules) {
                 logger.debug(r.toString());
 
                 if (!ObjectTypes.getObjectTypes().contains(r.getObject())) {
                     logger.warn("Initial percolation rule \"" + r.toString() + "\" has invalid object type "
                             + r.getObject() + ". It will be ignored.");
                     r.setActive(false);
                 }
 
                 if (!ObjectTypes.getObjectTypes().contains(r.getSubject())) {
                     logger.warn("Initial percolation rule \"" + r.toString() + "\" has invalid subject type "
                             + r.getObject() + ". It will be ignored.");
                     r.setActive(false);
                 }
 
             }
 
             tx.commit();
 
         } finally {
             if (s != null) {
                 s.close();
             }
         }
 
     }
 
     public void configure(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
         this.strategy = new GroupedPercolationStrategy(sessionFactory); //new SimplePercolationStrategy();
 
         logger.info("Starting initialisation of Rule-based percolator");
 
         checkInitialPercolationRules();
         checkPercolationRules();
 
         configureProgressionCurveTimer();
         configureGarbageCollectionTimer();
 
         logger.info("Finished initialisation of Rule-based percolator");
     }
 
     /**
      * Configures the timer that runs garbage collection
      */
     private void configureGarbageCollectionTimer() {
         garbageTimer = new Timer(true);
         Date runAt = new Date();
 
         garbageTimer.scheduleAtFixedRate(new GarbageTask(), runAt, GARBAGE_COLLECTION_INTERVAL);
     }
 
     /**
      * Configures the timer that runs the F/N update according to the progression curves
      */
     private void configureProgressionCurveTimer() {
         curveTimer = new Timer(true);
         Date runAt = new Date();
 
         curveTimer.scheduleAtFixedRate(new CurveTask(), runAt, CURVE_UPDATE_INTERVAL);
     }
 
     /**
      * Computes the ALE (focus + nimbus) of one object for one event
      * 
      * @param objectURL
      *            the object for which the ALE should be computed
      * @param user
      *            the user for which the ALE should be computed
      * @return the sum of focus and nimbus, if there is focus/nimbus match
      */
     public int getALE(String objectURL, String user) {
         return getALEfromPercolationSteps(objectURL, user);
     }
 
     /**
      * Returns the ALE by computing the direct sum of focus and nimbus values
      * 
      * @param objectURL
      *            the object for which the ALE should be computed
      * @param user
      *            the user for which the ALE should be computed
      * @return the sum of focus and nimbus, if there is focus/nimbus match
      */
     private int getALEfromPercolationSteps(String objectURL, String user) {
         Session s = null;
         Transaction tx = null;
         try {
             s = sessionFactory.openSession();
             tx = s.beginTransaction();
 
             String query = "select sum(ps.focus), sum(ps.nimbus) from PercolationStep ps where ps.objectURL = :objectURL and ps.userGroup like '%*%' and ps.userGroup not like :minusUser";
 
             Query q = s.createQuery(query).setString("objectURL", objectURL).setString("minusUser", "%-" + user + "%");
 
             List<Object> list = q.list();
 
             for (Object object : list) {
 
                 Object[] pair = (Object[]) object;
 
                 if (pair[0] == null || pair[1] == null) {
                     return 0;
                 }
 
                 Long nimbus = (Long) pair[0];
                 Long focus = (Long) pair[1];
 
                 if (nimbus != 0 && focus != 0) {
                     return new Long(nimbus + focus).intValue();
                 } else {
                     return 0;
                 }
 
             }
 
             tx.commit();
 
         } finally {
             if (s != null) {
                 s.close();
             }
         }
 
         return 0;
     }
 
     /**
      * Deletes all the {@link PercolationStep}-s of which the energy is too low.
      * 
      * @param s
      *            a Hibernate {@link Session}
      */
     private void collectGarbage(Session s) {
         
         Query q1 = s
                 .createQuery("delete from PercolationStep ps where (ps.nimbus < 20 and ps.focus = 0) and ps.matchedAetherEvent.id in (select mae.id from MatchedAetherEvent mae join mae.initialPercolationRule ipr where (ipr.percolationMode = 20 or ipr.percolationMode = 30))");
         // q1.setInteger("minValue", MIN_ENERGY_LEVEL);
         int d1 = q1.executeUpdate();
 
         Query q2 = s
                 .createQuery("delete from PercolationStep ps where (ps.focus < 20 and ps.nimbus = 0) and ps.matchedAetherEvent.id in (select mae.id from MatchedAetherEvent mae join mae.initialPercolationRule ipr where (ipr.percolationMode = 10 or ipr.percolationMode = 30))");
         // q2.setInteger("minValue", MIN_ENERGY_LEVEL);
         int d2 = q2.executeUpdate();
 
         logger.debug("Garbage-collected " + d1 + d2 + " percolation steps");
         
     }
 
     /**
      * Recomputes the focus values
      * 
      * @param s
      *            a Hibernate session
      */
     private void updateFocusValues(Session s) {
         
        String q = "update Focus f set f.focus = (select sum(ps.focus) from PercolationStep ps where ps.objectURL = f.objectURL and ps.userGroup like '%*%' and ps.userGroup not like concat(concat('%-',f.user),'%'))";
         int updated = s.createQuery(q).executeUpdate();
 
         logger.debug("Updated " + updated + " focus values");
         
     }
 
     /**
      * Updates all the focus and nimbus values of {@link PercolationStep}-s according the the progression curves
      * 
      * @param s
      *            a Hibernate {@link Session}
      */
     public void executeEnergyProgressionUpdate(Session s) {
         List<InitialPercolationRule> iprs = s.createQuery("from InitialPercolationRule ipr").list();
         for (InitialPercolationRule initialPercolationRule : iprs) {
             executeEnergyProgressionForProgressionCurve(initialPercolationRule, s);
         }
     }
 
     /**
      * Updates all the focus and nimbus values of {@link PercolationStep}-s for one {@link InitialPercolationRule}
      * 
      * @param ipr
      *            the {@link InitialPercolationRule} containing the progression curves
      * @param s
      *            a Hibernate {@link Session}
      */
     private void executeEnergyProgressionForProgressionCurve(InitialPercolationRule ipr, Session s) {
         
 
         int updatedFocusPercolationSteps = 0;
         int updatedNimbusPercolationSteps = 0;
 
         if (ipr.getFocusProgressionCurve() != null && ipr.getFocusProgressionCurve().trim().length() != 0) {
             String focusQuery = buildEnergyUpdateStatement(ipr.getFocusProgressionCurve(), true);
             Query focusUpdate = s.createQuery(focusQuery).setParameter(0, ipr.getId());
             logger.debug("Now running " + focusQuery);
             updatedFocusPercolationSteps = focusUpdate.executeUpdate();
         }
 
         if (ipr.getNimbusProgressionCurve() != null && ipr.getNimbusProgressionCurve().trim().length() != 0) {
             String nimbusQuery = buildEnergyUpdateStatement(ipr.getNimbusProgressionCurve(), false);
             Query nimbusUpdate = s.createQuery(nimbusQuery).setParameter(0, ipr.getId());
             logger.debug("Now running " + nimbusQuery);
             updatedNimbusPercolationSteps = nimbusUpdate.executeUpdate();
         }
 
         logger.debug("Updated " + updatedFocusPercolationSteps + " percolation steps for for focus and "
                 + updatedNimbusPercolationSteps + " for nimbus");
                 
     }
 
     /**
      * Builds a UPDATE statement that updates all the nimbus or focus values of all PercolationSteps matched by a
      * {@link InitialPercolationRule}.
      * 
      * @param progressionCurve
      *            the string representing the progression curve. Supports only basic SQL operations
      * @param isFocusCurve
      *            whether this is a focus or a nimbus progression curve
      * 
      * @return a query string of a UPDATE query that expects as argument the reference to a InitialPercolationRule id
      *         (not named parameter)
      */
     private String buildEnergyUpdateStatement(String progressionCurve, boolean isFocusCurve) {
         String query = "UPDATE PercolationStep SET " + (isFocusCurve ? "focus" : "nimbus") + " = ";
 
         // progression curve is something like "1 - t" or "1 - t*t + t - 1/t"
         query += (isFocusCurve ? "initialFocus" : "initialNimbus") + " * ("
                 + progressionCurve.replaceAll("t", "TIMESTAMPDIFF(HOUR, created, now())") + ") ";
         query += "WHERE matchedAetherEvent.id IN (SELECT mae.id FROM MatchedAetherEvent mae JOIN mae.initialPercolationRule ipr WHERE ipr.id = ?)";
 
         return query;
     }
 
     private class GarbageTask extends TimerTask {
 
         @Override
         public void run() {
 
             if (!percolationLock.get()) {
 
                 Session s = null;
                 try {
                     s = InitServlet.getSessionFactory().openSession();
                     collectGarbage(s);
                     updateFocusValues(s);
                 } finally {
                     if (s != null) {
                         s.close();
                     }
                 }
             }
         }
     }
 
     private class CurveTask extends TimerTask {
 
         @Override
         public void run() {
 
             if (!percolationLock.get()) {
 
                 Session s = null;
                 try {
                     s = InitServlet.getSessionFactory().openSession();
                     executeEnergyProgressionUpdate(s);
                     collectGarbage(s);
                     updateFocusValues(s);
                 } finally {
                     if (s != null) {
                         s.close();
                     }
                 }
             }
         }
     }
 
 }
