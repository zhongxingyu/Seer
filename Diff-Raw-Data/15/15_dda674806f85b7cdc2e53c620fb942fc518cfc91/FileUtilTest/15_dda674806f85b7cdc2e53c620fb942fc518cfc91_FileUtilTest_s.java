 package be.kdg.groepi.utils;
 
 import be.kdg.groepi.model.User;
 import be.kdg.groepi.service.UserService;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.mock.web.MockMultipartFile;
 import org.springframework.mock.web.MockServletContext;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpSession;
 import java.io.File;
 import java.io.IOException;
 
 import static be.kdg.groepi.utils.DateUtil.dateToLong;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mitch Va Daele
  * Date: 28-2-13
  * Time: 15:22
  * To change this template use File | Settings | File Templates.
  */
 public class FileUtilTest {
     private MockMultipartFile file;
 
     @Before
     public void beforeEachTest() {
         file = new MockMultipartFile("Test.txt", "Test".getBytes());
     }
 
     @After
     public void afterEachTest() {
 
     }
 
     @Test
     public void testUpload() throws IOException {
         User user = new User("TIMMEH", "TIM@M.EH", "hemmit", dateToLong(4, 5, 2011, 15, 32, 0));
         UserService.createUser(user);
        ServletContext servletContext = new MockServletContext("file:C:/images");
         HttpSession mockHttpSession = new MockHttpSession(servletContext);
 
         assertEquals("File has not been uploaded", FileUtil.savePicture(mockHttpSession, file, user.getId()),
                 File.separator + "images" + File.separator + "profilepictures" + File.separator + user.getId() + ".jpg");
 
 
         assertTrue("File paths are not equal", FileUtil.savePicture(mockHttpSession, file,
                 user.getId()).equals(File.separator + "images" + File.separator + "profilepictures" + File.separator + user.getId() + ".jpg"));
     }
 }
