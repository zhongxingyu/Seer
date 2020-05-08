 package com.prodyna.pac.mmonshausen.conference.test;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.container.test.api.RunAsClient;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.resteasy.client.ProxyFactory;
 import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.jboss.shrinkwrap.resolver.api.maven.Maven;
 
 import com.prodyna.pac.mmonshausen.conference.model.Conference;
 import com.prodyna.pac.mmonshausen.conference.rest.ConferenceRestService;
 /**
  * test all methods of the ConferenceService
  * 
  * @author Martin Monshausen, PRODYNA AG
  */
 @RunWith(Arquillian.class)
 public class ConferenceRestServiceTest {
 
 	@Deployment(testable=false)
 	public static Archive<?> createTestArchive() {
 		final File[] libs = Maven.resolver()
                 .loadPomFromFile("pom.xml")
                 .resolve("com.prodyna.pac.mmonshausen.conference:conference-backend")
                 .withoutTransitivity().asFile();
 
 		final WebArchive archive = ShrinkWrap
                 .create(WebArchive.class, "test.war")
                .addPackages(true, "com/prodyna/pac/mmonshausen/conference/")
                .addAsResource("META-INF/beans.xml", "WEB-INF/beans.xml")
                 .addAsLibraries(libs);
 		return archive;
 	}
 	
 	@ArquillianResource
     private URL contextPath;
 
     @BeforeClass
     public static void beforeClass() {
             RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
     }
     
     @Test
     @RunAsClient
     public void testConferenceRestService() throws Exception {
             // Get Service proxies.
             final ConferenceRestService confRestService = ProxyFactory
                             .create(ConferenceRestService.class,
                                            contextPath.toString() + "rest/conference");
 
             // Create conference
             final Calendar startCal = new GregorianCalendar(2013, Calendar.DECEMBER, 24);
             final Calendar endCal = new GregorianCalendar(2013, Calendar.DECEMBER, 24);
             Conference conference = new Conference();
             conference.setDescription("Beschreibung der WJAX");
             conference.setName("W-JAX");
             conference.setStartDate(startCal.getTime());
             conference.setEndDate(endCal.getTime());
 
             final Response response = confRestService.saveConference(conference);
             conference = (Conference) response.getEntity();
             Assert.assertTrue(response.getStatus() == Status.OK.getStatusCode());
             Assert.assertTrue(conference.getId() != null);
             
             // Get conference by ID.
             final Conference conferenceById = confRestService.getConferenceById(conference.getId());
             Assert.assertTrue(conferenceById != null);
             
             //get all conferences
             final List<Conference> conferenceList = confRestService.listAllConferences();
             Assert.assertTrue(conferenceList != null);
             Assert.assertTrue(conferenceList.size() > 0);
             
             conference.setName("W-JAX 2013");
             final Response updResponse = confRestService.updateConference(conference);
             final Conference updConference = (Conference) updResponse.getEntity();
             Assert.assertTrue(updResponse.getStatus() == Status.OK.getStatusCode());
             Assert.assertTrue("WJAX 2013".equals(updConference.getName()));
             
             final Response delResponse = confRestService.deleteConference(conference.getId());
             Assert.assertTrue(delResponse.getStatus() == Status.OK.getStatusCode());
     }
 }
