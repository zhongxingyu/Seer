 package com.smash.revolance.ui.explorer.application;
 
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
 
 import com.smash.revolance.ui.explorer.element.IElement;
 import com.smash.revolance.ui.explorer.element.api.ElementBean;
 import com.smash.revolance.ui.explorer.element.api.Link;
 import com.smash.revolance.ui.explorer.helper.BotHelper;
 import com.smash.revolance.ui.explorer.helper.FileHelper;
 import com.smash.revolance.ui.explorer.page.IPage;
 import com.smash.revolance.ui.explorer.page.api.Page;
 import com.smash.revolance.ui.explorer.page.api.PageBean;
 import com.smash.revolance.ui.explorer.sitemap.SiteMap;
 import com.smash.revolance.ui.explorer.user.User;
 import com.smash.revolance.ui.explorer.bot.Bot;
 import org.junit.*;
 import org.openqa.selenium.WebDriver;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.containsString;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 
 /**
  * User: wsmash
  * Date: 30/11/12
  * Time: 20:08
  */
 public class BotTest
 {
     private static final File appCfg;
 
     private static final String website;
     private static final String target;
     private static final String index;
     private static final String pageA;
     private static final String pageB;
     private static final String help;
     private static final String broken;
 
     static
     {
         appCfg = new File(new File("").getAbsoluteFile(), "src/test/config/cfg-app.xml");
 
         website = "file://" + new File(new File("").getAbsoluteFile(), "src/test/resources/website").getAbsolutePath();
         target = new File(new File("").getAbsoluteFile(), "target").getAbsolutePath();
         index = website + "/index.html";
         pageA = website + "/page_A.html";
         pageB = website + "/page_B.html";
         broken = website + "/broken.html";
         help = website + "/help.html";
     }
 
     private static User               user;
     private static SiteMap            sitemap;
     private static WebDriver          browser;
     private static Bot                bot;
     private static ApplicationManager manager;
     private static Application        app;
 
     @BeforeClass
     public static void setUp() throws Exception
     {
         manager = new ApplicationManager( appCfg );
         app = manager.get( "website" );
 
         user = app.getUser( "super user" );
         user.enablePageScreenshot( false );
         user.enablePageElementScreenshot( false );
         user.enableFollowButtons( false );
         user.enableFollowLinks( true );
 
 
         user.setDomain( website );
         user.getSiteMap().setHome( index );
 
         user.explore();
 
         bot = user.getBot();
         browser = bot.getBrowser();
         sitemap = user.getSiteMap();
     }
 
     @After
     public void tearDown()
     {
         try
         {
             browser.quit();
         }
         catch (Exception e)
         {
 
         }
     }
 
     @Test
     public void botShouldExploreAllPages() throws Exception
     {
         Collection<PageBean> pages = sitemap.getPages();
         assertThat( pages.size(), is( 6 ) );
     }
 
     @Test
     public void botShouldExtractButtonsFromPageContent() throws Exception
     {
         List<IElement> buttons;
         IPage page_B = sitemap.findPageByUrl( pageB ).getInstance();
         buttons = page_B.getLinks();
         assertThat( buttons.size(), is( 2 ) );
         assertThat( Link.containsLink( buttons, "button" ), is( true ) );
         assertThat( Link.containsLink( buttons, "index" ), is( true ) );
     }
 
     @Test
     public void botShouldExtractLinksFromPageContent() throws Exception
     {
         List<IElement> links;
 
 
         // Home page check
         IPage homePage = sitemap.findPageByUrl( index ).getInstance();
         links = homePage.getLinks();
         assertThat( links.size(), is( 2 ) );
         assertThat( Link.containsLink( links, "page_A" ), is( true ) );
         assertThat( Link.containsLink( links, "page_B" ), is( true ) );
 
         // page_A page check
         IPage page_A = sitemap.findPageByUrl( pageA ).getInstance();
         links = page_A.getLinks();
         assertThat( links.size(), is( 3 ) );
         assertThat( Link.containsLink( links, "index" ), is( true ) );
         assertThat( Link.containsLink( links, "page_B" ), is( true ) );
         assertThat( Link.containsLink( links, "help" ), is( true ) );
 
         // page_B page check
         IPage page_B = sitemap.findPageByUrl( pageB ).getInstance();
         links = page_B.getLinks();
         assertThat( links.size(), is( 3 ) );
         assertThat( Link.containsLink( links, "index" ), is( true ) );
         assertThat( Link.containsLink( links, "page_A" ), is( true ) );
         assertThat( Link.containsLink( links, "help" ), is( true ) );
 
         // help page check
         IPage helpPage = sitemap.findPageByUrl( help ).getInstance();
         links = helpPage.getLinks();
        assertThat( links.size(), is( 2 ) );
         assertThat( Link.containsLink( links, "Broken" ), is( true ) );
         assertThat( Link.containsLink( links, "Google" ), is( true ) );
     }
 
     @Test
     public void botShouldDetectBrokenLinks() throws Exception
     {
         assertThat( sitemap.getBrokenLinks().size(), is( 2 ) );
         assertThat( sitemap.getBrokenLinks().contains( broken ), is( true ) );
 
         // help page check
         IPage helpPage = sitemap.getPage( help, false ).getInstance();
         List<IElement> brokenLinks = helpPage.getBrokenLinks();
         assertThat(helpPage.hasBrokenLinks(), is(true));
         assertThat(Link.containsLink(brokenLinks, broken), is(true));
     }
 
     @Test
     public void botShouldNotExploreAnyPageOutOfTheUserDomain() throws Exception
     {
         // True
         user.setHome("http://google.fr");
         assertThat(BotHelper.rightDomain(user, "https://google.fr/maynotexists/nothingcanbedonehere"), is(true));
 
         // Not True
         sitemap.setHome("http://google.fr");
         assertThat(BotHelper.rightDomain(user, "https://glagla.fr"), is(false));
     }
 
     @Test
     public void botShouldHandleVariants() throws Exception
     {
         assertThat(sitemap.findPageByUrl(pageB).getVariants().size(), is(1));
     }
 
 }
