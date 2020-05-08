 package de.enwida.web;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
 import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
 import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
 
 import de.enwida.web.model.User;
 import de.enwida.web.service.interfaces.IUserService;
 import de.enwida.web.utils.LogoFinder;
  
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/root-context-test.xml")
 public class AdminPanel {
 
     //Dont add test word into class name in order to preventtest run during mvn build 
     //These test case should be run manually when web site is up
     final WebClient webClient = new WebClient();
     
     @Autowired
     private IUserService userService;
     
     private String webSiteLink="http://localhost:8080/enwida/";
     
     private HtmlPage page; 
 
     private String link=webSiteLink+"user/admin/";
     
     private HtmlAnchor anchor;
     
     @Before
     public void initBrowser(){
         webClient.setCssEnabled(false);
         webClient.setJavaScriptEnabled(false);
     }
     
     @Test
     public void LogoFinder() throws Exception{
        //check if logo finder works
         LogoFinder lf=new LogoFinder();
         lf.getImages("siemens.de");
     }
     
     @Test
     public void TestRegisterPage() throws Exception{
         ArrayList<String> testList = new ArrayList<String>();
         testList.add("olcay@siemens.de");
         testList.add("olcay@tum.de");
         testList.add("olcay@in.tum.de");
         testList.add("olcay@gmail.de");
         testList.add("olcay@olcay.de");
         testList.add("olcay@a.de");
         for (String user : testList) {
             //Make sure we dont have this user in the database
             User persistedUser=userService.fetchUser(user);
             System.out.println("Registering user:"+user);
             if(persistedUser!=null){
                 //we have this user, delete it
                 System.out.println("This user in database");
                 userService.deleteUser(persistedUser.getUserId());
             }
             registerUser(user, "secret");
             //check if we registered the user
             User registedUser=userService.fetchUser(user);
             Assert.assertNotNull(registedUser);
             //Delete the user
             userService.deleteUser(registedUser.getUserId());
         }
     }
 
     @Test
     public void loginWithInvalidMailAddress() throws Exception{
       //Login with Invalid mail address
         Assert.assertEquals(false, Login("admins","secret2"));
     }
     
     @Test
     public void loginWithValidMailAddress() throws Exception{
         //Login with valid mail address
         Assert.assertEquals(true, Login("admin","secret"));
     }
     
     @Test
     public void LoginWithFirstAndLastName() throws Exception{
         User user=userService.fetchAllUsers().get(0);
         //login with first and last name
         Assert.assertEquals(true, Login(user.getUsername(),user.getPassword()));
     }
     
     //Select user from list and check its details
     @Test
     public void CheckUserDetails() throws Exception{
         //first log out
         webClient.getPage(webSiteLink+"j_spring_security_logout");
         //find an admin user
         Login("admin", "secret");
         //Go to UserList page
         String link=webSiteLink+"user/admin/";
         //Get user lists
         page = webClient.getPage(link);
         List<HtmlAnchor> list = page.getAnchors();
         for (HtmlAnchor htmlAnchor : list) {
             if (htmlAnchor.toString().contains("userID=")){
                 anchor=htmlAnchor;
                 break;
             }
         }
         //we click on the user test@enwida.de
         Assert.assertNotNull(anchor);
         if(anchor!=null){
             page = anchor.click();
         }
         Assert.assertEquals(true, page.getTitleText().equalsIgnoreCase("Enwida Admin User Page"));
     }
 
     //Try to login to the page
     public boolean Login(String userName,String password) throws Exception{
         //first log out
         webClient.getPage(webSiteLink+"j_spring_security_logout");
         
         // Get the first page
         final HtmlPage page1 = webClient.getPage(webSiteLink+"user/login.html");
 
         // Get the form that we are dealing with and within that form, 
         // find the submit button and the field that we want to change.
         final HtmlForm form = page1.getFormByName("loginForm");
 
         final HtmlSubmitInput button = form.getInputByName("submit");
         final HtmlTextInput textFieldUserName = form.getInputByName("j_username");
         final HtmlPasswordInput textFieldPassword = form.getInputByName("j_password");
         // Change the value of the text field
         textFieldUserName.setValueAttribute(userName);
         textFieldPassword.setValueAttribute(password);
         
         // Now submit the form by clicking the button and get back the second page.
         page = button.click();
         //Make sure we are in user list page
         link=webSiteLink+"user/admin/admin_userlist";
         page = webClient.getPage(link);
         return page.getTitleText().equalsIgnoreCase("Enwida Admin Page");
     }
 
     public void registerUser(String userName,String password) throws Exception {
         //first log out
         webClient.getPage(webSiteLink+"j_spring_security_logout");
         
         // Get the register page
         final HtmlPage page1 = webClient.getPage(webSiteLink+"user/register");
 
         // Get the form that we are dealing with and within that form, 
         // find the submit button and the field that we want to change.
         final HtmlForm form = page1.getFormByName("registrationForm");
 
         final HtmlSubmitInput button = form.getInputByName("submit");
         final HtmlTextInput textFieldUserName = form.getInputByName("userName");
         final HtmlTextInput textFieldUserEmail = form.getInputByName("email");
         final HtmlTextInput textFieldFirstName = form.getInputByName("firstName");
         final HtmlTextInput textFieldLastName = form.getInputByName("lastName");
         final HtmlPasswordInput textFieldPassword = form.getInputByName("password");
         final HtmlPasswordInput textFieldConfirmPassword = form.getInputByName("confirmPassword");
 
         // Change the value of the text field
         textFieldUserName.setValueAttribute(userName);
         textFieldUserEmail.setValueAttribute(userName);
         textFieldFirstName.setValueAttribute("test");
         textFieldLastName.setValueAttribute("test");
         textFieldPassword.setValueAttribute(password);
         textFieldConfirmPassword.setValueAttribute(password);
 
         // Now submit the form by clicking the button and get back the second page.
         final HtmlPage page2 = button.click();
         System.out.println(page2.asText());
         //Check if we passed the registration page
         Assert.assertTrue(!page2.getTitleText().equalsIgnoreCase("Enwida Registration"));
         //Check the home page is retrieved
        Assert.assertTrue(!page2.getTitleText().equalsIgnoreCase("Enwida Home Page"));
         System.out.println("User registered");
         webClient.closeAllWindows();
     }
     
 }
