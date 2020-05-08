 package com.socialize.test.ui;
 
 import java.io.File;
 import java.net.URL;
 
 import junit.framework.Assert;
 
 import org.jboss.arquillian.api.ArquillianResource;
 import org.jboss.arquillian.api.Deployment;
 import org.jboss.arquillian.drone.annotation.Drone;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.asset.StringAsset;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.descriptor.api.Descriptors;
 import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
 import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.socialize.account.MemberAuthenticator;
 import com.socialize.account.MemberListProducer;
 import com.socialize.account.Registration;
 import com.socialize.data.PersistenceContextProducers;
 import com.socialize.data.qualifier.Primary;
 import com.socialize.data.qualifier.Utility;
 import com.socialize.domain.Member;
 import com.socialize.support.SocializeLogger;
 import com.thoughtworks.selenium.DefaultSelenium;
 
 /**
  * Verify that a new member can register
  * 
  * @author <a href="http://community.jboss.org/people/dan.j.allen">Dan Allen</a>
  */
 @RunWith(Arquillian.class)
 public class RegistrationWebTest {
     private static final String WEBSRC = "src/main/webapp";
     
     @Deployment(testable = false)
     public static WebArchive createDeployment() {
         MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class).loadReposFromPom("pom.xml");
         return ShrinkWrap.create(WebArchive.class, "registration.war")
             .addPackage(Registration.class.getPackage())
             .addPackage(Member.class.getPackage())
             .addPackages(true, PersistenceContextProducers.class.getPackage())
             .addPackage(SocializeLogger.class.getPackage())
             .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
             .addAsWebResource(new File(WEBSRC, "index.xhtml"))
             .addAsWebResource(new File(WEBSRC, "register.xhtml"))
             .addAsWebInfResource(new File(WEBSRC, "WEB-INF/templates/default.xhtml"), "templates/default.xhtml")
             .addAsWebResource(new File(WEBSRC, "resources/css/screen.css"), "resources/css/screen.css")
            .addAsWebResource(new File(WEBSRC, "resources/gfx/banner.jpg"), "resources/gfx/banner.jpg")
             .addAsWebResource(new File(WEBSRC, "resources/gfx/weld.png"), "resources/gfx/weld.png")
             .addAsWebInfResource(new File(WEBSRC, "WEB-INF/faces-config.xml"))
             .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
             .addAsLibraries(resolver.artifacts(
                     "org.jboss.seam.security:seam-security:3.0.0.Final",
                     "org.jboss.seam.faces:seam-faces:3.0.0.Final").resolveAsFiles())
             // empty web.xml required on JBoss AS
             .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class).exportAsString()));
     }
     
     @Drone
     DefaultSelenium driver;
     
     @ArquillianResource
     URL deploymentUrl;
     
     @Test
     public void shouldRegisterMemberWithValidInputs() {
         driver.open(deploymentUrl + "register.jsf");
         NamingContainerRef regForm = new NamingContainerRef("reg");
         driver.type(regForm.locatorForChild("name"), "Ike");
         driver.type(regForm.locatorForChild("username"), "arquillian");
         driver.type(regForm.locatorForChild("password"), "jbosstesting");
         driver.type(regForm.locatorForChild("email"), "ike@arquillian.org");
         driver.click(regForm.locatorForChild("register"));
         driver.waitForPageToLoad("15000");
         
         Assert.assertTrue(driver.isTextPresent("Welcome to the conversation, Ike!"));
         Assert.assertTrue(driver.isElementPresent("xpath=//table[@id=\"members\"]//td[contains(text(), \"Ike\")]"));
     }
     
     public static class NamingContainerRef {
         private String id;
         
         public NamingContainerRef(String id) {
             this.id = id;
         }
         
         public String getId() {
             return id;
         }
         
         public String locator() {
             return "id=" + this.id;
         }
         
         public String locatorForChild(String relativeId) {
             return locator() + ":" + relativeId;
         }
     }
 }
