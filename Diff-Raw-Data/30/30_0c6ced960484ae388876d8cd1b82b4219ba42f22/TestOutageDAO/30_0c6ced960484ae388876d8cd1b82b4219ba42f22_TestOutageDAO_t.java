 package com.pocketcookies.pepco.model;
 
 import java.sql.Timestamp;
 import java.util.Arrays;
 import java.util.Collection;
 
 import junit.framework.TestCase;
 
 import org.hibernate.SessionFactory;
 
 import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
 import com.pocketcookies.pepco.model.dao.OutageDAO;
 import org.hibernate.cfg.AnnotationConfiguration;
 
 public class TestOutageDAO extends TestCase {
 
     private SessionFactory sessionFactory;
 
     public void setUp() {
         this.sessionFactory = SessionFactoryLoader.loadSessionFactory();
         this.sessionFactory.getCurrentSession().beginTransaction();
     }
 
     public void testUpdateOutage() {
         final OutageDAO outageDao = new OutageDAO(this.sessionFactory);
         // Saved
         final Outage o1 = new Outage(1, 1, new Timestamp(1), null);
         // Gets saved
         final OutageClusterRevision r1 = new OutageClusterRevision(1,
                 new Timestamp(1), new Timestamp(1), o1, null, 1);
         // Gets saved
         final OutageClusterRevision r2 = new OutageClusterRevision(2,
                 new Timestamp(2), new Timestamp(1), o1, null, 1);
         // Not saved
         final OutageClusterRevision r3 = new OutageClusterRevision(1,
                 new Timestamp(1), new Timestamp(1), o1, null, 1);
         outageDao.updateOutage(r1);
         assertEquals(
                 1,
                 this.sessionFactory.getCurrentSession().createQuery("from Outage").list().size());
         assertEquals(
                 1,
                 this.sessionFactory.getCurrentSession().createQuery("from AbstractOutageRevision").list().size());
         outageDao.updateOutage(r2);
         assertEquals(
                 1,
                 this.sessionFactory.getCurrentSession().createQuery("from Outage").list().size());
         assertEquals(
                 2,
                 this.sessionFactory.getCurrentSession().createQuery("from AbstractOutageRevision").list().size());
         outageDao.updateOutage(r3);
         assertEquals(
                 1,
                 this.sessionFactory.getCurrentSession().createQuery("from Outage").list().size());
         assertEquals(
                 2,
                 this.sessionFactory.getCurrentSession().createQuery("from AbstractOutageRevision").list().size());
     }
 
     public void testCloseMissingOutages() {
         final OutageDAO outageDao = new OutageDAO(sessionFactory);
         final Outage o1 = new Outage(1, 1, new Timestamp(1), null);
         final Outage o2 = new Outage(2, 2, new Timestamp(1), null);
         final Outage o3 = new Outage(3, 3, new Timestamp(1), new Timestamp(2));
         this.sessionFactory.getCurrentSession().save(o1);
         this.sessionFactory.getCurrentSession().save(o2);
         this.sessionFactory.getCurrentSession().save(o3);
         this.sessionFactory.getCurrentSession().flush();
         outageDao.closeMissingOutages(
                 Arrays.asList(new Integer[]{o1.getId()}), new Timestamp(3));
         this.sessionFactory.getCurrentSession().flush();
         this.sessionFactory.getCurrentSession().refresh(o1);
         this.sessionFactory.getCurrentSession().refresh(o2);
         this.sessionFactory.getCurrentSession().refresh(o3);
         assertNull(o1.getObservedEnd());
         assertEquals(3, o2.getObservedEnd().getTime());
         assertEquals(2, o3.getObservedEnd().getTime());
     }
 
     public void testUpdateNullExpectedRestoration() {
         final OutageDAO dao = new OutageDAO(sessionFactory);
         final Outage o1 = new Outage(1, 1, new Timestamp(1), null);
         final OutageRevision or1 = new OutageRevision(1, null,
                 new Timestamp(1), o1, null, null, null);
         final OutageRevision or2 = new OutageRevision(2, null,
                 new Timestamp(2), o1, null, null, null);
         dao.updateOutage(or1);
         final OutageRevision retrievedor1 = (OutageRevision) this.sessionFactory.getCurrentSession().createQuery("from AbstractOutageRevision").list().get(0);
         assertEquals(1, retrievedor1.getNumCustomersAffected());
         assertEquals(null, retrievedor1.getEstimatedRestoration());
         dao.updateOutage(or2);
         final OutageRevision retrievedor2 = (OutageRevision) this.sessionFactory.getCurrentSession().createQuery(
                 "from AbstractOutageRevision order by observationDate desc").list().get(0);
         assertEquals(2, retrievedor2.getNumCustomersAffected());
         assertEquals(null, retrievedor2.getEstimatedRestoration());
     }
 
     public void testOutagesAsOf() {
         final OutageDAO dao = new OutageDAO(sessionFactory);
         final Outage previousOutage = new Outage(1, 1, new Timestamp(1),
                 new Timestamp(2));
         final OutageRevision previousOutageRevision = new OutageRevision(1,
                 null, new Timestamp(4), previousOutage, null, "cause",
                 CrewStatus.PENDING);
         final Outage currentOutage = new Outage(2, 2, new Timestamp(3), null);
         final OutageRevision currentOutageRevision = new OutageRevision(1,
                 null, new Timestamp(4), currentOutage, null, "cause",
                 CrewStatus.PENDING);
         final Outage futureOutage = new Outage(3, 3, new Timestamp(6), null);
         final OutageRevision futureOutageRevision = new OutageRevision(1, null,
                 new Timestamp(4), futureOutage, null, "cause",
                 CrewStatus.PENDING);
         final Outage closedOutage = new Outage(4, 4, new Timestamp(4),
                 new Timestamp(5));
         final OutageRevision closedOutageRevision = new OutageRevision(1, null,
                 new Timestamp(4), closedOutage, null, "cause",
                 CrewStatus.PENDING);
         this.sessionFactory.getCurrentSession().save(previousOutage);
         this.sessionFactory.getCurrentSession().save(currentOutage);
         this.sessionFactory.getCurrentSession().save(futureOutage);
         this.sessionFactory.getCurrentSession().save(closedOutage);
         this.sessionFactory.getCurrentSession().save(previousOutageRevision);
         this.sessionFactory.getCurrentSession().save(currentOutageRevision);
         this.sessionFactory.getCurrentSession().save(futureOutageRevision);
         this.sessionFactory.getCurrentSession().save(closedOutageRevision);
         this.sessionFactory.getCurrentSession().flush();
 
         final Collection<AbstractOutageRevision> revisions = dao.getOutagesAtZoomLevelAsOf(new Timestamp(4), null, AbstractOutageRevision.class);
 
         assertEquals(2, revisions.size());
         assertTrue(revisions.contains(currentOutageRevision));
         assertTrue(revisions.contains(closedOutageRevision));
 
     }
 
     public void testOutageRevisionsAsOf() {
         final OutageDAO dao = new OutageDAO(sessionFactory);
         final Outage currentOutage = new Outage(1, 1, new Timestamp(1),
                 new Timestamp(3));
         final OutageRevision r1 = new OutageRevision(1, null, new Timestamp(1),
                 currentOutage, null, "cause", CrewStatus.PENDING);
         final OutageRevision r2 = new OutageRevision(1, null, new Timestamp(2),
                 currentOutage, null, "cause", CrewStatus.PENDING);
         final OutageRevision r3 = new OutageRevision(1, null, new Timestamp(3),
                 currentOutage, null, "cause", CrewStatus.PENDING);
 
         final Outage futureOutage = new Outage(2, 2, new Timestamp(4), null);
         final OutageRevision pastOutageRevision = new OutageRevision(1, null,
                 new Timestamp(2), futureOutage, null, "Cause",
                 CrewStatus.PENDING);
         final OutageRevision futureOutageRevision = new OutageRevision(1, null,
                 new Timestamp(4), futureOutage, null, "Cause",
                 CrewStatus.PENDING);
 
         this.sessionFactory.getCurrentSession().save(currentOutage);
         this.sessionFactory.getCurrentSession().save(futureOutage);
         this.sessionFactory.getCurrentSession().save(r1);
         this.sessionFactory.getCurrentSession().save(r2);
         this.sessionFactory.getCurrentSession().save(r3);
         this.sessionFactory.getCurrentSession().save(pastOutageRevision);
         this.sessionFactory.getCurrentSession().save(futureOutageRevision);
 
         final Collection<AbstractOutageRevision> revisions = dao.getOutagesAtZoomLevelAsOf(new Timestamp(2), null, AbstractOutageRevision.class);
         assertEquals(1, revisions.size());
         assertTrue(revisions.contains(r2));
     }
 
     public void testOutageRevisionsAsOfDiscriminator() {
         final OutageDAO dao = new OutageDAO(sessionFactory);
         final Outage o1 = new Outage(1, 1, new Timestamp(1), new Timestamp(3));
         final OutageRevision r1 = new OutageRevision(1, null, new Timestamp(1),
                 o1, null, "cause", CrewStatus.PENDING);
         final OutageClusterRevision cr1 = new OutageClusterRevision(1, null,
                 new Timestamp(1), o1, null, 1);
         this.sessionFactory.getCurrentSession().save(o1);
         this.sessionFactory.getCurrentSession().save(r1);
         this.sessionFactory.getCurrentSession().save(cr1);
 
         final Collection<AbstractOutageRevision> outages = dao.getOutagesAtZoomLevelAsOf(
                 new Timestamp(2), null, OutageRevision.class);
         assertEquals(1, outages.size());
         assertTrue(outages.contains(r1));
 
         final Collection<AbstractOutageRevision> outageClusters = dao.getOutagesAtZoomLevelAsOf(new Timestamp(2), null, OutageClusterRevision.class);
         assertEquals(1, outageClusters.size());
         assertTrue(outageClusters.contains(cr1));
 
         final Collection<AbstractOutageRevision> allOutages = dao.getOutagesAtZoomLevelAsOf(new Timestamp(2), null, AbstractOutageRevision.class);
         assertEquals(2, allOutages.size());
         assertTrue(allOutages.contains(r1));
         assertTrue(allOutages.contains(cr1));
     }
 
     public void testOutagesAsOfZoomLevel() {
         final OutageDAO dao = new OutageDAO(sessionFactory);
         final Outage o1 = new Outage(1, 1, new Timestamp(1), null);
         o1.getZoomLevels().add(1);
         final Outage o2 = new Outage(2, 2, new Timestamp(1), null);
         o2.getZoomLevels().add(2);
         final OutageRevision or1 = new OutageRevision(1, new Timestamp(1), new Timestamp(1), o1, null, "test", CrewStatus.PENDING);
         final OutageRevision or2 = new OutageRevision(1, new Timestamp(1), new Timestamp(1), o2, null, "test", CrewStatus.PENDING);
         this.sessionFactory.getCurrentSession().save(o1);
         this.sessionFactory.getCurrentSession().save(o2);
         this.sessionFactory.getCurrentSession().save(or1);
         this.sessionFactory.getCurrentSession().save(or2);
 
         final Collection<AbstractOutageRevision> outages = dao.getOutagesAtZoomLevelAsOf(new Timestamp(2), 1, AbstractOutageRevision.class);
         assertEquals(1, outages.size());
         assertEquals(or1,outages.iterator().next());
     }
     
     public void testOutagesAsOfZoomLevelWithDiscriminator() {
         final OutageDAO dao = new OutageDAO(sessionFactory);
         final Outage o1 = new Outage(1, 1, new Timestamp(1), null);
         o1.getZoomLevels().add(1);
         final Outage o2 = new Outage(2, 2, new Timestamp(1), null);
         o2.getZoomLevels().add(2);
         final OutageRevision or1 = new OutageRevision(1, new Timestamp(1), new Timestamp(1), o1, null, "test", CrewStatus.PENDING);
         final OutageClusterRevision or2 = new OutageClusterRevision(1, new Timestamp(1), new Timestamp(1), o2, null, 1);
         this.sessionFactory.getCurrentSession().save(o1);
         this.sessionFactory.getCurrentSession().save(o2);
         this.sessionFactory.getCurrentSession().save(or1);
         this.sessionFactory.getCurrentSession().save(or2);
 
         Collection<AbstractOutageRevision> outages = dao.getOutagesAtZoomLevelAsOf(new Timestamp(2), 1, OutageRevision.class);
         assertEquals(1, outages.size());
         assertEquals(or1,outages.iterator().next());
         
         outages = dao.getOutagesAtZoomLevelAsOf(new Timestamp(2), 1, OutageClusterRevision.class);
         assertEquals(0, outages.size());
         
         outages = dao.getOutagesAtZoomLevelAsOf(new Timestamp(2), 2, OutageRevision.class);
         assertEquals(0, outages.size());
         
         outages = dao.getOutagesAtZoomLevelAsOf(new Timestamp(2), 2, OutageClusterRevision.class);
         assertEquals(1, outages.size());
         assertEquals(or2,outages.iterator().next());
     }
     
     //Test that updating an outage adds zoom levels to the existing outage in the database.
     public void testUpdateZoomLevels() {
         final OutageDAO outageDao = new OutageDAO(this.sessionFactory);
         // Saved
         final Outage storedOutage = new Outage(1, 1, new Timestamp(1), null);
         storedOutage.getZoomLevels().add(1);
         final OutageClusterRevision storedRevision = new OutageClusterRevision(1, new Timestamp(1), new Timestamp(1), storedOutage, null, 1);
 
         //We need the outage to be in the database with a revision.
         outageDao.updateOutage(storedRevision);
 
         //Check that there is exactly one zoom level for this outage.
         assertEquals(1, ((Outage) this.sessionFactory.getCurrentSession().createQuery("from Outage").list().get(0)).getZoomLevels().size());
         //Check that the only zoom level is 1.
         assertEquals(1, (int) ((Outage) this.sessionFactory.getCurrentSession().createQuery("from Outage").list().get(0)).getZoomLevels().iterator().next());
 
         //Craft the outage revision with the new zoom level.
         final Outage dummyOutage = new Outage(1, 1, new Timestamp(1), null);
         dummyOutage.getZoomLevels().add(2);
         final OutageClusterRevision revision = new OutageClusterRevision(1,
                 new Timestamp(1), new Timestamp(1), dummyOutage, null, 1);
 
         outageDao.updateOutage(revision);
 
         //Test that this has added zoom levels.
         //Check that there are exactly 2 zoom levels for this outage.
         assertEquals(2, ((Outage) this.sessionFactory.getCurrentSession().createQuery("from Outage").list().get(0)).getZoomLevels().size());
         //Check that the only zoom level is 1.
         assertTrue(((Outage) this.sessionFactory.getCurrentSession().createQuery("from Outage").list().get(0)).getZoomLevels().contains(1));
         assertTrue(((Outage) this.sessionFactory.getCurrentSession().createQuery("from Outage").list().get(0)).getZoomLevels().contains(2));
     }
    
    /**
     * Test that revisions collected during a different parser run are still
     * fetched by getOutagesAtZoomLevelAsOf.
     */
    public void testPreviouslyCollectedRevisions()
    {
        final OutageDAO dao = new OutageDAO(this.sessionFactory);
        final Outage oldOutage = new Outage(1, 1, new Timestamp(1), null);
        final Outage newOutage = new Outage(1, 1, new Timestamp(2), null);
        final OutageRevision oldRevision = new OutageRevision(1, new Timestamp(10), new Timestamp(1), oldOutage, null, "test", CrewStatus.PENDING);
        final OutageRevision newRevision = new OutageRevision(1, new Timestamp(10), new Timestamp(2), newOutage, null, "test", CrewStatus.PENDING);
        this.sessionFactory.getCurrentSession().save(oldOutage);
        this.sessionFactory.getCurrentSession().save(newOutage);
        this.sessionFactory.getCurrentSession().save(oldRevision);
        this.sessionFactory.getCurrentSession().save(newRevision);
        this.sessionFactory.getCurrentSession().flush();
        
        final Collection<AbstractOutageRevision> revisions = dao.getOutagesAtZoomLevelAsOf(new Timestamp(3), null, OutageRevision.class);
        assertEquals(2, revisions.size());
        assertTrue(revisions.contains(oldRevision));
        assertTrue(revisions.contains(newRevision));
    }
 }
