 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.gadgetcontainer.view;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import static org.easymock.classextension.EasyMock.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.After;
 
 import java.util.List;
 import java.io.File;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 
 import com.globant.katari.hibernate.coreuser.domain.CoreUser;
import com.globant.katari.core.spring.controller.JsonCommandController;
 
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.globant.katari.gadgetcontainer.SpringTestUtils;
 import org.springframework.context.ApplicationContext;
 
 import com.globant.katari.shindig.domain.Application;
 
 import com.globant.katari.gadgetcontainer.domain.GadgetGroup;
 import com.globant.katari.gadgetcontainer.domain.SampleUser;
 
 import com.globant.katari.gadgetcontainer.application.ListApplicationsCommand;
 
 public class DirectoryDoTest {
 
   private String gadgetXmlUrl = "file:///" + new File(
       "target/test-classes/SampleGadget.xml").getAbsolutePath();
 
   private ApplicationContext appContext;
 
   private Session session;
 
   @Before
   public void setUp() throws Exception {
 
     appContext = SpringTestUtils.getContext();
 
     session = ((SessionFactory) appContext.getBean("katari.sessionFactory"))
       .openSession();
 
     session.createQuery("delete from GadgetInstance").executeUpdate();
     session.createQuery("delete from GadgetGroup").executeUpdate();
     session.createQuery("delete from CoreUser").executeUpdate();
     session.createQuery("delete from Application").executeUpdate();
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void test() throws Exception {
 
     CoreUser user = new SampleUser("me");
     session.saveOrUpdate(user);
     user = (CoreUser) session.createQuery("from CoreUser").uniqueResult();
 
     GadgetGroup group = new GadgetGroup(user, "gadget group", 2);
     session.saveOrUpdate(group);
 
     Application app = new Application(gadgetXmlUrl);
     session.saveOrUpdate(app);
 
     // Sets the currently logged on user
     SpringTestUtils.setLoggedInUser(user);
 
     ViewCommandController controller;
     controller = (ViewCommandController) appContext.getBean(
         "/directory.do");
 
     ByteArrayOutputStream os = new ByteArrayOutputStream();
     PrintWriter writer = new PrintWriter(os);
 
     MockHttpServletRequest request;
     request = new MockHttpServletRequest("GET", "directory.do");
     request.setParameter("gadgetGroupName", "gadget group");
 
     HttpServletResponse response = createMock(HttpServletResponse.class);
     response.addHeader("Content-type", "application/json");
     expect(response.getWriter()).andReturn(writer);
     replay(response);
 
     ModelAndView mv;
     mv = controller.handleRequest(request, response);
 
     assertThat(mv.getViewName(), is("directory.ftl"));
     assertThat(mv.getModel().get("command"), is(ListApplicationsCommand.class));
     assertThat(mv.getModel().get("result"), is(List.class));
     List<Application> applications;
     applications = (List<Application>) mv.getModel().get("result");
     assertThat(applications.get(0).getTitle(), is("Test title"));
   }
 
   @After
   public void tearDown() {
     session.close();
   }
 }
 
