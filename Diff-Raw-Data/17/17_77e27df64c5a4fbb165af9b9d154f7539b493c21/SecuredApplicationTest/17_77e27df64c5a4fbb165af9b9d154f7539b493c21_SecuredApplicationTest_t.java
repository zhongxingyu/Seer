 package com.smash.revolance.ui.explorer.application.secured;
 
 /*
         This file is part of Revolance UI Suite.
 
         Revolance UI Suite is free software: you can redistribute it and/or modify
         it under the terms of the GNU General Public License as published by
         the Free Software Foundation, either version 3 of the License, or
         (at your option) any later version.
 
         Revolance UI Suite is distributed in the hope that it will be useful,
         but WITHOUT ANY WARRANTY; without even the implied warranty of
         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
         GNU General Public License for more details.
 
         You should have received a copy of the GNU General Public License
         along with Revolance UI Suite.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import com.smash.revolance.ui.explorer.application.Application;
 import com.smash.revolance.ui.explorer.application.ApplicationManager;
 import com.smash.revolance.ui.explorer.user.User;
 import com.smash.revolance.ui.explorer.bot.BrowserFactory;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.File;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 
 /**
  * User: wsmash
  * Date: 26/01/13
  * Time: 17:50
  */
@Ignore
 public class SecuredApplicationTest
 {
     private static String      target, templates;
     private static Application app;
 
     private static File   usersCfg, appCfg;
     private static String login;
 
     private static final String home, index, changePasswd, pageA, pageB, broken, help;
 
     private static final String domain, website;
     private static ApplicationManager manager;
 
     static
     {
         website = new File( new File( "" ).getAbsoluteFile(), "src/test/resources/website" ).getAbsolutePath();
         templates = new File( new File( "" ).getAbsoluteFile(), "src/test/resources/templates" ).getAbsolutePath();
         String securedWebsite = new File( new File( "" ).getAbsoluteFile(), "src/test/resources/secured-website" ).getAbsolutePath();
 
         target = new File( new File( "" ).getAbsoluteFile(), "target" ).getAbsolutePath();
 
         appCfg = new File( new File( "" ).getAbsoluteFile(), "src/test/config/cfg-app.xml" );
         usersCfg = new File( new File( "" ).getAbsoluteFile(), "src/test/config/cfg-users.xml" );
 
         domain = "file:///" + securedWebsite;
         home = domain + "/home.html";
         login = domain + "/login.html";
         changePasswd = domain + "/change-passwd.html";
 
         index = "file:///" + website + "/index.html";
         pageA = "file:///" + website + "/page_A.html";
         pageB = "file:///" + website + "/page_B.html";
         broken = "file:///" + website + "/broken.html";
         help = "file:///" + website + "/help.html";
     }
 
     private static Application app2;
 
     @BeforeClass
     public static void setUp() throws Exception
     {
         manager = new ApplicationManager(appCfg);
         app = manager.getRefApp();
 
         app.setReportFolder(target);
 
         app.setHomeUrl(login);
         app.setDomain(domain);
 
         assertThat(app.isRef(), is(true));
         assertThat(app.getHomeUrl(), is(login));
         assertThat(app.getReportFolder(), is(target + "/" + app.getId()));
 
         assertThat(app.getUserCount(), is(3));
 
         User administrator = app.getRefUser();
         assertThat(administrator.isRef(), is(true));
 
         User user_A = app.getUser("user_A");
         assertNotNull(user_A);
 
         assertThat( user_A.getBrowserHeight(), is( 600 ) );
         assertThat( user_A.getBrowserWidth(), is( 800 ) );
 
         User user_B = app.getUser("user_B");
         assertNotNull(user_B);
 
         assertThat( user_B.getBrowserHeight(), is( 600 ) );
         assertThat( user_B.getBrowserWidth(), is( 800 ) );
 
     }
 
     @AfterClass
     public static void tearDown() throws Exception
     {
         for (User user : app.getUsers())
         {
             try
             {
                 if (user.isBrowserActive())
                 {
                     user.getBrowser().quit();
                 }
             }
             catch (Exception e)
             {
                 // Ignore gently
             }
         }
     }
 
     @Test
     public void testerShouldDetectEnforcementOnSecuredWebsite() throws Exception, BrowserFactory.InstanciationError
     {
         app.explore();
     }
 
 }
