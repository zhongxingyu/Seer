 package ch.itraum.springapp.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Collection;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.ui.ExtendedModelMap;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
 
 import ch.itraum.springapp.controller.SpringAppController;
 import ch.itraum.springapp.model.JavaBean;
 import ch.itraum.springapp.model.ReferenceJavaBean;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:./src/main/webapp/WEB-INF/spring/servlet-context.xml")
 public class SpringAppControllerTest {
 	
 	@Autowired
 	private SpringAppController springAppController;
 	
 	@Autowired
 	private AnnotationMethodHandlerAdapter methodHandlerAdapter;
 	
 	@Test
 	public void testViewNames() {
 		assertEquals("springapp", springAppController.get(new ExtendedModelMap()));
 		assertEquals("springapp", springAppController.post(new JavaBean(), new ExtendedModelMap()));
 	}
 
 	@Test
 	public void testReferenceData() throws Exception {
 		MockHttpServletRequest request = new MockHttpServletRequest(RequestMethod.GET.name(), "/");
 		MockHttpServletResponse response = new MockHttpServletResponse();
 
 		ModelAndView modelAndView = methodHandlerAdapter.handle(request, response, springAppController);
 			
 		@SuppressWarnings("unchecked")
 		Collection<ReferenceJavaBean> referenceJavaBean = (Collection<ReferenceJavaBean>)modelAndView.getModel().get("referenceJavaBeans");
 			
 		assertEquals(true, referenceJavaBean.contains(new ReferenceJavaBean("BBB")));
 		assertEquals(3, referenceJavaBean.size());
 	}
 	
 	@Test
 	public void testPost() throws Exception {		
 		MockHttpServletRequest request = new MockHttpServletRequest(RequestMethod.POST.name(), "/");
 		request.addParameter("name", "FooBar");
 		request.addParameter("referenceJavaBean", "BBB");
 		request.addParameter("referenceJavaBeanSet", "AAA");
 		request.addParameter("referenceJavaBeanSet", "CCC");
 				
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		
 		ModelAndView modelAndView = methodHandlerAdapter.handle(request, response, springAppController);
 		JavaBean javaBean = (JavaBean)modelAndView.getModel().get("javaBean");
 		
 		
 		assertEquals("FooBar", javaBean.getName());
 		assertEquals(new ReferenceJavaBean("BBB"), javaBean.getReferenceJavaBean());
 		assertEquals(2, javaBean.getReferenceJavaBeanSet().size());
 		assertEquals(true, javaBean.getReferenceJavaBeanSet().contains(new ReferenceJavaBean("AAA")));
 	}
 }
