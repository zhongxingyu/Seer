 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.tracker.backend.webinterface;
 
 import com.tracker.backend.StringUtils;
 import com.tracker.backend.entity.Peer;
 import com.tracker.backend.entity.Torrent;
 import com.tracker.backend.webinterface.entity.TorrentData;
 import java.net.InetAddress;
 import java.security.MessageDigest;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Vector;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author bo
  */
 public class TorrentSearchTest {
 
     static Peer p;
     static Vector<Torrent> torrents = new Vector<Torrent>();
     static Vector<TorrentData> torrentMetaData = new Vector<TorrentData>();
 
 
     static EntityManagerFactory emf = Persistence.createEntityManagerFactory("TorrentTrackerPU");
 
     public TorrentSearchTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
 
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() throws Exception {
         // we need to add some torrents and peers to test this
         // in setUp to have the same environment before each test
         Torrent t;
         TorrentData tData;
 
         String infoHash;
         String peerId;
         byte[] rawInfoHash = new byte[20];
         byte[] rawPeerId = new byte[20];
 
         System.out.println("Setting up database");
 
         EntityManager em = emf.createEntityManager();
         Random r = new Random(Calendar.getInstance().getTimeInMillis());
 
         t = new Torrent();
         tData = new TorrentData();
         p = new Peer();
 
         // fix up a convincing info hash
         byte byteHash[] = new byte[1024];
         r.nextBytes(byteHash);
         // get SHA-1 sum of byteHash
         MessageDigest md;
         md = MessageDigest.getInstance("SHA-1");
         md.update(byteHash);
         rawInfoHash = md.digest();
 
         infoHash = StringUtils.getHexString(rawInfoHash);
 
         t.setInfoHash(infoHash);
 
         tData.setName((String)"This name contains the word 'Brilliant'");
         tData.setTorrentSize((long)999999999);
 
         tData.setAdded(Calendar.getInstance().getTime());
         tData.setDescription((String)"This description contains the word 'fabulous'.");
 
         t.setTorrentData(tData);
 
         p.setBytesLeft(tData.getTorrentSize());
         p.setIp(InetAddress.getByName("192.168.1.3"));
 
         Long port = (long) 63049;
         p.setPort(port);
 
         p.setLastActionTime(Calendar.getInstance().getTime());
 
         // set peer id and raw peer id
         peerId = new String();
         peerId = "lbdfgd1321-------001";
         for(int i = 0; i < peerId.length(); i++) {
             rawPeerId[i] = (byte) peerId.charAt(i);
         }
         p.setPeerId(peerId);
 
         p.setSeed(false);
         p.setTorrent(t);
 
         t.addLeecher(p);
 
         em.getTransaction().begin();
         em.persist(t);
         em.persist(tData);
         em.persist(p);
         em.getTransaction().commit();
 
         torrents.add(t);
         torrentMetaData.add(tData);
 
         // I need at least two torrents to test this class
         t = new Torrent();
         tData = new TorrentData();
         r.nextBytes(byteHash);
         md.update(byteHash);
         rawInfoHash = md.digest();
 
         infoHash = StringUtils.getHexString(rawInfoHash);
         t.setInfoHash(infoHash);
 
         tData.setName((String)"This name contains the word 'Awful'");
         tData.setDescription((String)"This description contains the word 'Horrible'");
         tData.setTorrentSize(965485654L);
         tData.setAdded(Calendar.getInstance().getTime());
 
         t.setTorrentData(tData);
 
         em.getTransaction().begin();
         em.persist(t);
         em.persist(tData);
         em.getTransaction().commit();
 
         torrents.add(t);
 
         em.close();
     }
 
     @After
     public void tearDown() {
         // removes all the peers and torrent after each test
         EntityManager em = emf.createEntityManager();
 
         System.out.println("Tearing down database");
 
         em.getTransaction().begin();
         em.clear();
 
         Query q = em.createQuery("SELECT p FROM Peer p");
         List l = q.getResultList();
         Iterator itr = l.iterator();
         while(itr.hasNext()) {
             Peer tmp = (Peer) itr.next();
             Torrent tTemp = tmp.getTorrent();
 
             if(tTemp != null)
                 tTemp.removePeer(tmp);
             else
                 System.out.println("woot?");
             em.remove(tmp);
         }
 
         q = em.createQuery("SELECT t FROM Torrent t");
         l = q.getResultList();
         itr = l.iterator();
         while(itr.hasNext()) {
             Torrent tmp = (Torrent) itr.next();
             em.remove(tmp);
         }
 
         em.getTransaction().commit();
         em.close();
 
         // clear out the torrents array
         torrents.clear();
     }
 
     /**
      * Test of getList method, of class TorrentSearch.
      */
     @Test
     public void testGetList_3args() {
         /*
          * current layout of the database is this:
          * name             description         seeds       leechers
          * ...brilliant     ...fabulous         0           1       <-- alive
          * ...awful         ...horrible         0           0       <-- dead
          *
          * common strings:
          * "this name contains the word"
          * "this description contains the word"
          */
         System.out.println("getList");
 
         // should be the first entry
         Torrent brilliant = torrents.get(0);
         Torrent awful = torrents.get(1);
 
         // try to get the 'brilliant' torrent by name only
         String searchString = "Brilliant";
         boolean searchDescriptions = false;
         boolean includeDead = false;
         List<Torrent> expResult = new Vector<Torrent>();
         List<Torrent> result;
 
         try {
             // I do not guarantee the ordering of the result, so do not simply
             // compare it to the expected result array, but instead check to
             // see if they contain the same amount and the same elements,
             // regardless of index
 
             expResult.add(brilliant);
            result = TorrentSearch.getList(searchString, searchDescriptions, includeDead);
             assertTrue(result.containsAll(expResult));
 
             // now try it with includeDead = true to make sure that the other one does
             // not match
             includeDead = true;
            result = TorrentSearch.getList(searchString, searchDescriptions, includeDead);
             assertTrue(result.containsAll(expResult));
 
             includeDead = false;
 
             // try to find the brilliant torrent by description
             searchString = "fabulous";
             searchDescriptions = true;
            result = TorrentSearch.getList(searchString, searchDescriptions, includeDead);
             assertTrue("cannot find the torrent based on description search", result.containsAll(expResult));
             searchDescriptions = false;
 
             // try to find both torrents by searching for common strings, first only
             // the brilliant one, then include dead for the awful one
 
             // test if it can match % by replacing 'name' and 'description'
             searchString = "this contains";
            result = TorrentSearch.getList(searchString, searchDescriptions, includeDead);
             assertTrue(result.containsAll(expResult));
 
             // search description
             // look for the 'description' word which only happens in the description
             searchString = "'description'";
             searchDescriptions = true;
            result = TorrentSearch.getList(searchString, searchDescriptions, includeDead);
             assertTrue(result.containsAll(expResult));
 
             // include dead, this should find 'awful'
             includeDead = true;
             expResult.add(awful);
            result = TorrentSearch.getList(searchString, searchDescriptions, includeDead);
             assertTrue(result.containsAll(expResult));
         }
         catch(Exception ex) {
             String failMessage = "Exception Caught: ";
             failMessage += ex.toString();
             failMessage += " ";
             failMessage += ex.getMessage();
             ex.printStackTrace();
             fail(failMessage);
         }
     }
 
     /**
      * Test of getList method, of class TorrentSearch.
      */
     @Test
    public void testGetList() {
         System.out.println("getList");
         // this should give back a list of all torrents.
         List<Torrent> expResult = torrents;
         List<Torrent> result = TorrentSearch.getList();
 
         // i don't want/need to guarantee the ordering of torrents
         assertEquals(expResult.size(), result.size());
         Iterator itr = expResult.iterator();
 
         // iterate over expected result to see if the result contains all the
         // expected entries
         while(itr.hasNext()) {
             Torrent t = (Torrent) itr.next();
             System.out.println("Does result contain " + t.toString() + "?");
             assertTrue(result.contains(t));
         }
     }
 }
