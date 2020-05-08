 package com.hoyoul.wordroid;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
 
 import com.hoyoul.wordroid.controller.WordroidController;
 import com.hoyoul.wordroid.controller.WordsetController;
 import com.hoyoul.wordroid.dto.Word;
 import com.hoyoul.wordroid.dto.Wordset;
 import com.hoyoul.wordroid.service.WordService;
 import com.hoyoul.wordroid.service.WordsetService;
 
 public class WordsetControllerTest {
 	private MockHttpServletRequest request;
     private MockHttpServletResponse response;
     private MockHttpSession session;
     private AnnotationMethodHandlerAdapter adapter;
 
     @InjectMocks
     private static WordsetController controller;
     
     @Mock
     private static WordsetService service;
     
     private static ApplicationContext context;
     
 	@BeforeClass
 	public static void init(){
 
 	}
 	
     @Before
     public void setUp() {
         request  = new MockHttpServletRequest();
         response = new MockHttpServletResponse();
         session  = (MockHttpSession) request.getSession();
         adapter  = new AnnotationMethodHandlerAdapter();
         
 		context = new ClassPathXmlApplicationContext("servlet-context.xml");
 		
 		//SessionFactory session=context.getBean("sessionFactory",SessionFactory.class);
 		controller = context.getBean("wordsetController",WordsetController.class);
 		service = context.getBean("wordsetServiceImpl",WordsetService.class);
 		
 		service.addWordset(new Wordset("First Wordset","First Wordset Description"));
 		service.addWordset(new Wordset("Second Wordset","Second Wordset Description"));
 
 		Wordset wordset = service.getWordset(1);
 		
 		wordset.getWords().add(new Word(wordset,"She's got her father's eyes","개 눈은 아빠랑 닮았어"));
 		wordset.getWords().add(new Word(wordset,"I'm going to the English class!","영어학원 가려구요"));
 		wordset.getWords().add(new Word(wordset,"My feet are killing me","다리가 너무 아파"));
 		
 		service.updateWordset(wordset);
 		
     }
     
     @Test
     public void listPage() throws Exception{
     	
     	request.setRequestURI("/wordset/list");
         request.setMethod("GET");
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         List<Wordset> list = service.listWordset();
         
         assertEquals(2,list.size());
         assertThat(mav.getModelMap().get("wordsetList"),is(List.class));
         assertThat(mav.getViewName(),is("wordset/list"));
     }
     
     @Test
     public void detailPage() throws Exception{
     	
     	request.setRequestURI("/wordset/detail/1");
         request.setMethod("GET");
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         List<Wordset> list = service.listWordset();
         
         assertNotNull(mav.getModelMap().get("wordset"));
         assertThat(mav.getModelMap().get("wordset"),is(Wordset.class));
         assertThat(mav.getViewName(),is("wordset/detail"));    
     }    
     
     @Test
     public void add() throws Exception{
     	
     	request.setRequestURI("/wordset/list");
         request.setMethod("POST");
         request.setParameter("name","Third Wordset");
         request.setParameter("desc", "Third Wordset Description.");
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         Wordset actualWordset = service.getWordset(3);
         assertEquals("Third Wordset",actualWordset.getName());
         assertEquals("Third Wordset Description.",actualWordset.getDescription());
         assertThat(mav.getViewName(),is("redirect:/wordset/list"));
     }
     
     @Test
     public void updatePage() throws Exception{
     	request.setRequestURI("/wordset/updatepage/2");
         request.setMethod("GET");
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         assertNotNull(mav.getModelMap().get("wordset"));
         assertThat(mav.getModelMap().get("wordset"),is(Wordset.class));
         assertThat(mav.getViewName(),is("wordset/modify"));    
     }
     
     @Test
     public void update() throws Exception{
     	
     	request.setRequestURI("/wordset/update/2");
         request.setMethod("POST");
         request.setParameter("name","Second Wordset[Modify]");
         request.setParameter("desc", "Second Wordset Description[Modify].");
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         Wordset actualWordset = service.getWordset(2);
         assertEquals("Second Wordset[Modify]",actualWordset.getName());
         assertEquals("Second Wordset Description[Modify].",actualWordset.getDescription());
         assertThat(mav.getViewName(),is("redirect:/wordset/updatepage/2"));
     }
     
     @Test
     public void delete() throws Exception{
     	
     	request.setRequestURI("/wordset/delete/2");
         request.setMethod("GET");
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         Wordset actualWordset = service.getWordset(2);
         assertNull(actualWordset);
         assertThat(mav.getViewName(),is("redirect:/wordset/list"));
     }
     
     
 }
