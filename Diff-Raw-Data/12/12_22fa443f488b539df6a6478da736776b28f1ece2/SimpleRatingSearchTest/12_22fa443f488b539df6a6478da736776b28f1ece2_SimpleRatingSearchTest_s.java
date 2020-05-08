 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.mv.fgsk.server.search;
 
 import de.cismet.cids.custom.wrrl_db_mv.fgsk.server.search.SimpleRatingSearch;
 import Sirius.server.ServerExit;
 import Sirius.server.ServerExitError;
 import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
 import Sirius.server.middleware.impls.proxy.StartProxy;
 import Sirius.server.newuser.User;
 import Sirius.server.newuser.UserGroup;
 import Sirius.server.property.ServerProperties;
 import Sirius.server.registry.Registry;
 import Sirius.server.search.CidsServerSearch;
 import Sirius.server.sql.DBConnectionPool;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import java.sql.Connection;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 
 import de.cismet.remotetesthelper.RemoteTestHelperService;
 
 import de.cismet.remotetesthelper.ws.rest.RemoteTestHelperClient;
 
 import de.cismet.tools.ScriptRunner;
 
 import static org.junit.Assert.*;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public class SimpleRatingSearchTest {
 
     //~ Static fields/initializers ---------------------------------------------
 
    private static final String TEST_DB_NAME = "calc_cache_search_test_db";
     private static final RemoteTestHelperService SERVICE = new RemoteTestHelperClient();
     private static final String SERVER_CONFIG =
         "src/test/resources/de/cismet/cids/custom/mv/fgsk/server/search/runtime.properties"; // NOI18N
 
     private static Registry registry;
     private static StartProxy proxy;
     private static DomainServerImpl server;
     private static User user;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new SimpleRatingSearchTest object.
      */
     public SimpleRatingSearchTest() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Throwable              DOCUMENT ME!
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     @BeforeClass
     public static void setUpClass() throws Throwable {
         final Properties p = new Properties();
         p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
         p.put("log4j.appender.Remote.remoteHost", "localhost");
         p.put("log4j.appender.Remote.port", "4445");
         p.put("log4j.appender.Remote.locationInfo", "true");
         p.put("log4j.rootLogger", "ALL,Remote");
         org.apache.log4j.PropertyConfigurator.configure(p);
 
         if (!Boolean.valueOf(SERVICE.initCidsSystem(TEST_DB_NAME))) {
             throw new IllegalStateException("cannot initilise test db");
         }
         final ServerProperties props = new ServerProperties(SERVER_CONFIG);
         final DBConnectionPool pool = new DBConnectionPool(props);
         final Connection con = pool.getConnection();
        final ScriptRunner runner = new ScriptRunner(con, true, false);
         runner.runScript(new BufferedReader(
                 new InputStreamReader(
                    SimpleRatingSearchTest.class.getResourceAsStream("CalcCacheSearchTestInit.sql"))));
         con.close();
         pool.closeConnections();
 
         registry = Sirius.server.registry.Registry.getServerInstance(1099);
         proxy = StartProxy.getInstance(SERVER_CONFIG);
         server = new DomainServerImpl(props);
         user = new User(1, "dummy", "WRRL_DB_MV", new UserGroup(1, "dummy", "WRRL_DB_MV"));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Throwable              Exception DOCUMENT ME!
      * @throws  IllegalStateException  DOCUMENT ME!
      */
     @AfterClass
     public static void tearDownClass() throws Throwable {
         // server shuts down all the other instances since we'return interface simple mode
         try {
            server.shutdown();
         } catch (final ServerExit e) {
             // success
         } catch (final ServerExitError e) {
             System.err.println("exit error");
         }
 
         if (!Boolean.valueOf(SERVICE.dropDatabase(TEST_DB_NAME))) {
             throw new IllegalStateException("could not drop test db");
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     @Before
     public void setUp() {
     }
 
     /**
      * DOCUMENT ME!
      */
     @After
     public void tearDown() {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String getCurrentMethodName() {
         return new Throwable().getStackTrace()[1].getMethodName();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @Test
     public void testPerformWBTrimmingRatingSearch() throws Exception {
         System.out.println("TEST " + getCurrentMethodName());
 
         final CidsServerSearch search = new SimpleRatingSearch.IntegerRatingSearch(
                 "public.fgsk_gewaesserrandstreifen_auswertung");
         final Collection result = proxy.getCallServer().customServerSearch(user, search);
 
         assertNotNull(result);
         assertFalse(result.isEmpty());
 
         final Iterator it = result.iterator();
         assertTrue(it.hasNext());
 
         final Object o = it.next();
 
         assertFalse(it.hasNext());
         assertTrue(o instanceof Map);
 
         final Map<String, Integer> map = (Map)o;
 
         Integer rating = map.get("1-11");
         assertEquals((Integer)1, rating);
 
         rating = map.get("4-11");
         assertEquals((Integer)3, rating);
 
         rating = map.get("3-12");
         assertEquals((Integer)4, rating);
 
         rating = map.get("2-14");
         assertEquals((Integer)5, rating);
 
         rating = map.get("4-14");
         assertEquals((Integer)3, rating);
 
         rating = map.get("2-17");
         assertEquals((Integer)5, rating);
 
         rating = map.get("1-23");
         assertEquals((Integer)1, rating);
 
         assertEquals("mapsize wrong", 28, map.size());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @Test
     public void testPerformWBLandUseRatingSearch() throws Exception {
         System.out.println("TEST " + getCurrentMethodName());
 
         final CidsServerSearch search = new SimpleRatingSearch.IntegerRatingSearch(
                 "public.fgsk_flaechennutzung_auswertung");
         final Collection result = proxy.getCallServer().customServerSearch(user, search);
 
         assertNotNull(result);
         assertFalse(result.isEmpty());
 
         final Iterator it = result.iterator();
         assertTrue(it.hasNext());
 
         final Object o = it.next();
 
         assertFalse(it.hasNext());
         assertTrue(o instanceof Map);
 
         final Map<String, Integer> map = (Map)o;
 
         Integer rating = map.get("1-11");
         assertEquals((Integer)5, rating);
 
         rating = map.get("4-11");
         assertEquals((Integer)3, rating);
 
         rating = map.get("3-14");
         assertEquals((Integer)4, rating);
 
         rating = map.get("15-12");
         assertEquals((Integer)1, rating);
 
         rating = map.get("16-15");
         assertEquals((Integer)4, rating);
 
         rating = map.get("9-17");
         assertEquals((Integer)3, rating);
 
         rating = map.get("13-23");
         assertEquals((Integer)5, rating);
 
         assertEquals("mapsize wrong", 111, map.size());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     @Test
     public void testPerformBadEnvStructureRatingSearch() throws Exception {
         System.out.println("TEST " + getCurrentMethodName());
 
         final CidsServerSearch search = new SimpleRatingSearch.DoubleRatingSearch(
                 "public.fgsk_schaedlicheumfeldstrukturen_auswertung");
         final Collection result = proxy.getCallServer().customServerSearch(user, search);
 
         assertNotNull(result);
         assertFalse(result.isEmpty());
 
         final Iterator it = result.iterator();
         assertTrue(it.hasNext());
 
         final Object o = it.next();
 
         assertFalse(it.hasNext());
         assertTrue(o instanceof Map);
 
         final Map<String, Double> map = (Map)o;
 
         Double rating = map.get("1-11");
         assertEquals((Double)(-0.5d), rating);
 
         rating = map.get("4-14");
         assertEquals((Double)(-0.5d), rating);
 
         rating = map.get("8-11");
         assertEquals((Double)0d, rating);
 
         rating = map.get("7-15");
         assertEquals((Double)(-0.5d), rating);
 
         rating = map.get("8-15");
         assertEquals((Double)0d, rating);
 
         rating = map.get("7-17");
         assertEquals((Double)(-0.5d), rating);
 
         rating = map.get("1-23");
         assertEquals((Double)(-0.5d), rating);
 
         assertEquals("mapsize wrong", 56, map.size());
     }
 }
