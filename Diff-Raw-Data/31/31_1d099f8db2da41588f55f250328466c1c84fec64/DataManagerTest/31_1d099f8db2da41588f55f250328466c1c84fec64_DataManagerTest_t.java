 /*
  * DataManagerTest.java
  * 
  * Copyright (C) 2009 Nicola Roberto Viganò
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gestionecassa.server.datamanager;
 
 import gestionecassa.backends.BackendAPI_2;
 import gestionecassa.backends.BackendAPI_1;
 import gestionecassa.Admin;
 import gestionecassa.Article;
 import gestionecassa.ArticleGroup;
 import gestionecassa.ArticleWithOptions;
 import gestionecassa.ArticlesList;
 import gestionecassa.Cassiere;
 import gestionecassa.Person;
 import gestionecassa.stubs.BackendStub_1;
 import gestionecassa.stubs.BackendStub_2;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Vector;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author ben
  */
 public class DataManagerTest {
 
     DataManager dataManager;
    BackendAPI_1 backend_1;
    BackendAPI_2 backend_2;
 
     public DataManagerTest() {
        backend_1 = new BackendStub_1();
        backend_2 = new BackendStub_2();
         dataManager = new DataManager(backend_2, "", backend_1);
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of registerUser method, of class DataManager.
      */
     @Test
     public void testRegisterCasssiere() {
         System.out.println("registerUser: Cassiere's part");
        Cassiere temp = new Cassiere(dataManager.getCassieresList().size(), "yeah", "bhoo");
         dataManager.registerUser(temp);
        assertEquals(temp, dataManager.getCassieresList().get(temp.getUsername()));
     }
 
     /**
      * Test of registerUser method, of class DataManager.
      */
     @Test
     public void testRegisterAdmin() {
         System.out.println("registerUser: Admin's part");
        Admin temp = new Admin(dataManager.getAdminsList().size(), "yeah", "bhoo");
         dataManager.registerUser(temp);
        assertEquals(temp, dataManager.getAdminsList().get(temp.getUsername()));
     }
 
     /**
      * Test of verifyUsername method, of class DataManager.
      */
     @Test
     public void testVerifyUsernamePresent() {
         System.out.println("verifyUsername: Test searching for a present one");
         Person temp = dataManager.verifyUsername("bene");
         assertNotNull(temp);
        assertEquals(temp, dataManager.getCassieresList().get("bene"));
     }
 
     /**
      * Test of verifyUsername method, of class DataManager.
      */
     @Test
     public void testVerifyUsernameNotPresent() {
         System.out.println("verifyUsername: Test searching for a not present one");
         assertNull(dataManager.verifyUsername("male"));
     }
 
     /**
      * Test of getArticlesList method, of class DataManager.
      */
     @Test
     public void testGetCurrentArticlesList() throws IOException {
         System.out.println("getCurrentArticlesList");
         assertEquals(dataManager.getArticlesList().getGroupsList(),
                 backend_1.loadArticlesList());
     }
 
     /**
      * Test of createNewCassaSession method, of class DataManager.
      */
     @Test
     public void testCreateNewCassaSession() {
         System.out.println("createNewCassaSession");
     }
 
     /**
      * Test of closeCassaSession method, of class DataManager.
      */
     @Test
     public void testCloseCassaSession() {
         System.out.println("closeCassaSession");
     }
 
     /**
      * Test of addNewOrder method, of class DataManager.
      */
     @Test
     public void testAddNewOrder() {
         System.out.println("addNewOrder");
     }
 
     /**
      * Test of delLastOrder method, of class DataManager.
      */
     @Test
 
     public void testDelLastOrder() {
         System.out.println("delLastOrder");
     }
 
     /**
      * Test of getNProgressive method, of class DataManager.
      */
     @Test
     public void testGetNProgressivo() {
         System.out.println("getNProgressivo");
     }
 
     /**
      * Test of saveNewArticlesList method, of class DataManager.
      */
     @Test
     public void testSaveNewArticlesList() throws IOException {
         System.out.println("saveNewArticlesList");
 
        Collection<Article> oldArticles = dataManager.getArticlesList().getArticlesList();
         
         List<Article> articles = new Vector<Article>();
         Collection<String> listaOpzioni = new Vector<String>();
         listaOpzioni.add("cacca secca");
         listaOpzioni.add("cacca liquida");
         articles.add(new Article(1,"fagiolo", 25));
         articles.add(new Article(2,"blabla", 35));
         articles.add(new Article(3,"merda dello stige", 5.5));
         articles.add(new ArticleWithOptions(4,"yeah", 10.25, listaOpzioni));
         
         List<ArticleGroup> groups = new Vector<ArticleGroup>();
         groups.add(new ArticleGroup(1, "group", articles));
 
         dataManager.saveNewArticlesList(new ArticlesList(groups));
 
         // FIXME non preservato l'ordine!
        assertEquals(dataManager.getArticlesList().getGroup(0).getList(), articles);
 
         assertEquals(
                 ((List<ArticleGroup>)
                     backend_1.loadArticlesList()).get(0).getList(),
                 articles);
 
         assertNotSame(
                 ((List<ArticleGroup>)
                     backend_1.loadArticlesList()).get(0).getList(),
                 oldArticles);
     }
 
 }
