 package org.jks.web;
 
 import org.jks.domain.Profile;
 import org.jks.domain.User;
 import org.jks.util.TestUtil;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.jdbc.JdbcTestUtils;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.MvcResult;
 import org.springframework.test.web.servlet.ResultActions;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 
 import static org.hamcrest.Matchers.notNullValue;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
 import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 import static org.hamcrest.Matchers.is;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:spring/appServlet/servlet-context.xml", "classpath:spring/root-context.xml"} )
 @WebAppConfiguration
 public class TestsRest {
 
     private MockMvc mockMvc;
 
     @Autowired
     private WebApplicationContext webApplicationContext;
 
     @Autowired
     private JdbcTemplate jdbcTemplate;
 
     @Before
     public void setUp() throws Exception {
 
         String createScript = "src/main/resources/sql/insert-data.sql";
         JdbcTestUtils.executeSqlScript(jdbcTemplate, new FileSystemResource(createScript), false);
 
        jdbcTemplate.execute("INSERT INTO UserArticle (userid, username, email,profile,profileid, name, password)\n" +
                "values (2, \"test\",\"test@example.com\",\"admin\",0, \"test user user\", SHA1('admin'));");
        jdbcTemplate.execute("INSERT INTO Article (articleid, datearticle,subject,article,author,sectionid)\n" +
                "values (0, CURRENT_TIMESTAMP,\"Primera Prueba\",\"Este es el articulo de prueba de la aplicación de Juan Camilo Carrillo, Sharon Corrales, Karen Miranda.\",\"Todos\",1);");
 
         mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
     }
 
     @After
     public void tearDown() throws Exception {
         String deleteScript = "src/main/resources/sql/delete-data.sql";
 
         JdbcTestUtils.executeSqlScript(jdbcTemplate,
                 new FileSystemResource(deleteScript), false);
     }
 
     @Test
     public void getUserById() throws Exception {
         mockMvc.perform(get("/user/2"))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType("application/json;charset=UTF-8"))
                 .andExpect(jsonPath("$.username", is("test")));
     }
 
     @Test
     public void getNonExistenUserById() throws Exception {
         mockMvc.perform(get("/user/12"))
                 .andExpect(status().isNotFound())
                 .andExpect(content().contentType("application/json;charset=UTF-8"))
                 .andExpect(jsonPath("$.message", notNullValue()));
     }
 
     @Test
     public void createUser() throws Exception {
         User user = new User();
         user.setUsername("user12");
         user.setEmail("user12@gmail.com");
         user.setPassword(User.sha1("password"));
         user.setName("Test User");
         user.setProfile(Profile.ADMINISTRATOR.toString());
         user.setProfileid(Profile.ADMINISTRATOR.getValue());
         String userJson = TestUtil.convertObjectToJson(user);
 
         mockMvc.perform(post("/user/create")
                 .contentType(TestUtil.APPLICATION_JSON_UTF8)
                 .content(userJson)
                 )
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
                 .andExpect(jsonPath("$.message", notNullValue()));
     }
 
     @Test
     public void createUserNoPassword() throws Exception {
         User user = new User();
         user.setUsername("user12");
         user.setEmail("user12@gmail.com");
         user.setName("Test User");
         user.setProfile(Profile.ADMINISTRATOR.toString());
         user.setProfileid(Profile.ADMINISTRATOR.getValue());
         String userJson = TestUtil.convertObjectToJson(user);
 
         mockMvc.perform(post("/user/create")
                 .contentType(TestUtil.APPLICATION_JSON_UTF8)
                 .content(userJson)
         )   //.andDo(print())
                 .andExpect(status().isNotAcceptable());
     }
 
     @Test
     public void testInvalidEmail() throws Exception {
         User user = new User();
         user.setUsername("user12");
         user.setEmail("invalid-mail");
         user.setName("Test User");
         user.setProfile(Profile.ADMINISTRATOR.toString());
         user.setProfileid(Profile.ADMINISTRATOR.getValue());
         String userJson = TestUtil.convertObjectToJson(user);
 
         mockMvc.perform(post("/user/create")
                 .contentType(TestUtil.APPLICATION_JSON_UTF8)
                 .content(userJson)
         )   //.andDo(print())
             .andExpect(status().isNotAcceptable());
     }
 
     @Test
     public void testNoUsername() throws Exception {
         User user = new User();
         user.setUsername(" ");
         user.setPassword(User.sha1("password"));
         user.setEmail("test@mail.com");
         user.setName("Test User");
         user.setProfile(Profile.ADMINISTRATOR.toString());
         user.setProfileid(Profile.ADMINISTRATOR.getValue());
         String userJson = TestUtil.convertObjectToJson(user);
 
         mockMvc.perform(post("/user/create")
                 .contentType(TestUtil.APPLICATION_JSON_UTF8)
                 .content(userJson)
         )   .andDo(print())
             .andExpect(status().isNotAcceptable());
     }
 
     @Test
     public void updateUser() throws Exception {
         User user = new User();
         user.setUserid(2);
         user.setUsername("test2updated");
         user.setPassword(User.sha1("password"));
         user.setEmail("test@mail.com");
         user.setName("Test User");
         user.setProfile(Profile.ADMINISTRATOR.toString());
         user.setProfileid(Profile.ADMINISTRATOR.getValue());
         String userJson = TestUtil.convertObjectToJson(user);
 
         mockMvc.perform(put("/user/update")
                 .contentType(TestUtil.APPLICATION_JSON_UTF8)
                 .content(userJson)
         )
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
                 .andExpect(jsonPath("$.message", notNullValue()));
 
         mockMvc.perform(get("/user/2"))
                 .andExpect(status().isOk())
                 .andExpect(content().contentType("application/json;charset=UTF-8"))
                 .andExpect(jsonPath("$.username", is("test2updated")));
     }
 
     @Test
     public void testDeleteUser() throws Exception {
         mockMvc.perform(delete("/user/delete/2")
         )
                 .andExpect(status().isOk())
                 .andExpect(content().contentType(TestUtil.APPLICATION_JSON_UTF8))
                 .andExpect(jsonPath("$.message", notNullValue()));
 
         mockMvc.perform(get("/user/2"))
                 .andExpect(status().isNotFound())
                 .andExpect(content().contentType("application/json;charset=UTF-8"))
                 .andExpect(jsonPath("$.message", notNullValue()));
 
     }
 }
