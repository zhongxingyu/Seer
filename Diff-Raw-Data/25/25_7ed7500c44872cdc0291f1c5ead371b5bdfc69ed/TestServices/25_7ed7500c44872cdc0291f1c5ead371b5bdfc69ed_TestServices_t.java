 package org.jks.service;
 
 import org.jks.domain.*;
 import org.jks.service.UserService;
 import org.junit.Before;
 import org.junit.After;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 import org.springframework.test.jdbc.JdbcTestUtils;
 
 import java.security.NoSuchAlgorithmException;
 import java.sql.Timestamp;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 @ContextConfiguration(locations = "classpath:hibernate.xml" )
 public class TestServices extends AbstractJUnit4SpringContextTests {
 
     @Autowired
     private UserService userService;
 
     @Autowired
     private ArticleService articleService;
 
     @Autowired
     private SectionService sectionService;
 
     @Autowired
     private CommentService commentService;
     
     @Autowired
     private JdbcTemplate jdbcTemplate;
 
     @Before
     public void insertData() {
         String createScript = "src/main/resources/sql/insert-data.sql";
         JdbcTestUtils.executeSqlScript(jdbcTemplate,
                 new FileSystemResource(createScript), false);
//        jdbcTemplate.execute("INSERT INTO UserArticle (userid, username, email,profile,profileid, name, password)\n" +
//                "values (2, \"test\",\"test@example.com\",\"admin\",0, \"test user user\", SHA1('admin'));");
//        jdbcTemplate.execute("INSERT INTO Article (articleid, datearticle,subject,article,author,sectionid)\n" +
//                "values (0, CURRENT_TIMESTAMP,\"Primera Prueba\",\"Este es el articulo de prueba de la aplicación de Juan Camilo Carrillo, Sharon Corrales, Karen Miranda.\",\"Todos\",1);");
     }
 
     @After
     public void deleteData() {
         String deleteScript = "src/main/resources/sql/delete-data.sql";
 
         JdbcTestUtils.executeSqlScript(jdbcTemplate,
                 new FileSystemResource(deleteScript), false);
 
         //jdbcTemplate.execute("delete from article");
         //jdbcTemplate.execute("delete from userarticle");
     }
 
     @Test
     public void testFindUserById() {
         User user = userService.getUserById(2);
 
         assertEquals("test", user.getUsername());
     }
 
     @Test(expected=NullPointerException.class)
     public void testFindUserNullId() {
         Integer id = null;
         userService.getUserById(id);
     }
 
     @Test
     public void testFindAllUsers() {
         List<User> users = userService.getUsers();
         assertFalse(users.isEmpty());
     }
 
     @Test
     public void testFindUserByName() {
         User user = userService.getUserByUsername("test");
         assertNotNull(user);
     }
 
     @Test
     public void testExistUsername() throws Exception {
         boolean exist = userService.checkExistUsername("test");
         assertTrue(exist);
     }
 
     @Test
     public void testFindUserByEmail() {
         User user = userService.getUserByEmail("test@example.com");
         assertNotNull(user);
     }
 
     @Test
     public void testAddUser() throws NoSuchAlgorithmException {
         User user = new User();
         user.setUsername("test2");
         user.setEmail("test2@test.com");
         user.setName("Test User");
         user.setPassword(User.sha1("test"));
         user.setProfile(Profile.ADMINISTRATOR.toString());
         user.setProfileid(Profile.ADMINISTRATOR.getValue());
         userService.addUser(user);
         User user2 = userService.getUserByUsername("test2");
         assertEquals(user.getUsername(), user2.getUsername());
     }
 
     @Test
     public void testUpdateUser() {
         User user = userService.getUserById(2);
         user.setEmail("testUpdate@example.com");
         userService.updateUser(user);
         User user2 = userService.getUserById(2);
         assertEquals(user.getEmail(), user2.getEmail());
     }
 
     @Test
     public void testDeleteUser() {
         User user = userService.getUserById(2);
         userService.deleteUser(user);
         user = userService.getUserById(2);
         assertNull(user);
     }
 
     @Test(expected = NullPointerException.class)
     public void deleteNonExistentUser() {
         User user = userService.getUserByUsername("test");
         userService.deleteUser(user);
         userService.deleteUser(user);
         userService.deleteUserById(2);
     }
     
     @Test 
     public void testAddArticle(){
     	Article article = new Article();
     	article.setSectionid((long) 1);
     	java.util.Date date= new java.util.Date();
     	Timestamp datearticle = new Timestamp(date.getTime());
     	article.setDatearticle(datearticle);
     	article.setAuthor("KJS");
     	article.setArticle("contenido del articulo de prueba desde maven");
     	article.setSubject("Prueba mvn");
     	
     	articleService.addArticle(article);
         Article article2 = articleService.getArticleBySubject("Prueba mvn");
         assertEquals(article.getAuthor(), article2.getAuthor());
     }
 
     @Test
     public void testFindArticles() {
         List<Article> articles = articleService.getArticles(0, 3);
         assertFalse(articles.isEmpty());
     }
 
     @Test
     public void testFindSectionById(){
     	Section section= sectionService.getSectionById(-30);
     	assertNull(section);
     }
     
     @Test
     public void testAddSection(){
     	Section section= new Section();
     	section.setSectionArticle("Prueba");
     	sectionService.addSection(section);
     	Section section2= sectionService.getSectionByName("Prueba");
     	assertEquals(section.getSectionArticle(), section2.getSectionArticle());
     }
     
     @Test
     public void testFindSections(){
     	List<Section> sections = sectionService.getSections();
         assertFalse(sections.isEmpty());
     }
     
     @Test
     public void testFindSectionByName(){
     	Section section= sectionService.getSectionByName("Sección Principal");
     	assertNotNull(section);
     }
 
     @Test
     public void findComments() {
         List<Comment> comments = commentService.getComments(0, 0, 2);
         assertFalse(comments.isEmpty());
     }
 
 //    @Test
 //    public void testCompleteArticle() throws Exception {
 //        Article article = articleService.getCompleteArticle(0, 2);
 //        assertNotNull(article);
 //        assertNotNull(article.getSubject());
 //        assertFalse(article.getComments().isEmpty());
 //    }
 
     @Test
     public void validatePassword() throws Exception {
         User user = userService.getUserById(2);
        String pass = User.sha1("admin");
        assertEquals(pass, user.getPassword());
     }
 }
