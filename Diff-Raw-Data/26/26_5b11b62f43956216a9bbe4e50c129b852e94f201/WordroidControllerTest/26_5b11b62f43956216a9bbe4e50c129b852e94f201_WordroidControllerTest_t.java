 package com.hoyoul.wordroid;
 
 import static org.mockito.Mockito.*;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestSuite;
 
 import org.hibernate.SessionFactory;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
 
 import com.hoyoul.wordroid.controller.WordroidController;
 import com.hoyoul.wordroid.dto.Word;
 import com.hoyoul.wordroid.service.WordService;
 
 //@RunWith(SpringJUnit4ClassRunner.class)
 //@ContextConfiguration("servlet-context.xml")
 public class WordroidControllerTest {
 	private MockHttpServletRequest request;
     private MockHttpServletResponse response;
     private MockHttpSession session;
     private AnnotationMethodHandlerAdapter adapter;
 
     //@Autowired
     //private ApplicationContext applicationContext; 
     
     @InjectMocks
     private static WordroidController controller;
     
     @Mock
     private static WordService service;
     
     /*
     @InjectMocks
     private static BoardController2 controller;
     
     @Mock
     private static BoardService boardService;
     private static CommentService commentService;
 	*/
     
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
		controller = context.getBean("wordroidController",WordroidController.class);
 		service = context.getBean("wordServiceImpl",WordService.class);
		
 		service.addWord(new Word("She's got her father's eyes","개 눈은 아빠랑 닮았어"));
 		service.addWord(new Word("I'm going to the English class!","영어학원 가려구요"));
 		service.addWord(new Word("My feet are killing me","다리가 너무 아파"));
 
     }
     
     @Test
     public void testData() throws Exception{
     	
     	request.setRequestURI("/data");
         request.setMethod("GET");
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         List<Word> list = service.listWord();
         
         assertEquals(3,list.size());
         assertThat(mav.getModelMap().get("wordList"),is(List.class));
         assertThat(mav.getViewName(),is("data"));
     }
     
     /*
     @SuppressWarnings("unchecked")
 	@Test
     public void listPage() throws Exception {
         request.setRequestURI("/board/listpage");
         request.setMethod("GET");
 
         session.setAttribute("loginUser", new User());
         //List<Board> list = new ArrayList<Board>();
         //when(boardService.listBoard()).thenReturn(list);
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         //verify(boardService,times(1)).listBoard();
         
         List<Board> list = boardService.listBoard();
         
         assertEquals(3,list.size());
         assertThat(mav.getModelMap().get("boardList"),is(List.class));
         assertThat(mav.getViewName(),is("board/list"));        
     }
     
     @Test
     public void listPageWithoutSession() throws Exception {
         request.setRequestURI("/board/listpage");
         request.setMethod("GET");
        
         ModelAndView mav = adapter.handle(request, response, controller);
         
         assertThat(mav.getViewName(),is("redirect:/indexpage"));        
     }
     
     @Test
     public void detailPage() throws Exception{
     	request.setRequestURI("/board/detailpage/2");
         request.setMethod("GET");
 
         session.setAttribute("loginUser", new User());
         //Board board = new Board();
         //when(boardService.getBoard(1)).thenReturn(board);
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         //verify(boardService).getBoard(1);
         assertNotNull(mav.getModelMap().get("board"));
         assertThat(mav.getModelMap().get("board"),is(Board.class));
         assertThat(mav.getViewName(),is("board/detail"));        
     }
     
     @Test
     public void detailPageWithoutSession() throws Exception{
     	request.setRequestURI("/board/detailpage/2");
         request.setMethod("GET");
         
         ModelAndView mav = adapter.handle(request, response, controller);
 
         assertThat(mav.getViewName(),is("redirect:/indexpage")); ;
     }
     
     @Test
     public void modifyPage() throws Exception{
     	request.setRequestURI("/board/modifypage/2");
         request.setMethod("GET");
 
         session.setAttribute("loginUser", new User());
         //when(boardService.getBoard(1)).thenReturn(new Board());
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         //verify(boardService).getBoard(1);
         assertNotNull(mav.getModelMap().get("board"));
         assertThat(mav.getModelMap().get("board"),is(Board.class));
         assertThat(mav.getViewName(),is("board/modify"));        
     }
     
     @Test
     public void modifyPageWithoutSession() throws Exception{
     	request.setRequestURI("/board/modifypage/2");
         request.setMethod("GET");
         
         ModelAndView mav = adapter.handle(request, response, controller);
 
         assertThat(mav.getViewName(),is("redirect:/indexpage")); ;
     }
     
     @Test
     public void addBoard() throws Exception{
     	request.setRequestURI("/board/add");
         request.setMethod("POST");
         request.setParameter("title", "TEST4");
         request.setParameter("content", "TEST4");
         
         User user = new User();
         user.setId(1);
         
         session.setAttribute("loginUser", user);
         //Board board = new Board();
         //when(boardService.addBoard(board)).thenReturn(1);
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         //verify(boardService).addBoard(board);
         //assertThat(mav.getModelMap().get("boardList"),is(List.class));
         //assertThat(mav.getViewName(),is("board/list"));
         
         Board actualBoard = boardService.getBoard(4);
         assertEquals(1,actualBoard.getUserId());
         assertEquals("TEST4",actualBoard.getTitle());
         assertEquals("TEST4",actualBoard.getContent());
         assertThat(mav.getViewName(),is("redirect:/board/listpage"));
     }
     
     @Test
     public void addBoardWithoutSession() throws Exception{
     	request.setRequestURI("/board/add");
         request.setMethod("POST");
         request.setParameter("title", "TEST");
         request.setParameter("content", "TEST");
         
         ModelAndView mav = adapter.handle(request, response, controller);
 
         assertThat(mav.getViewName(),is("redirect:/indexpage"));
     }
     
     @Test
     public void modifyBoard() throws Exception{
     	request.setRequestURI("/board/modify/2");
         request.setMethod("POST");
         request.setParameter("id", "2");
         request.setParameter("title", "TEST1234");
         request.setParameter("content", "TEST1234");
 
         session.setAttribute("loginUser", new User());
         //Board board = new Board();
         //when(boardService.updateBoard(board));
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         //verify(boardService).updateBoard(board);
         //assertThat(mav.getModelMap().get("board"),is(Board.class));
         //assertThat(mav.getViewName(),is("board/modifypage"));
         
         Board actualBoard = boardService.getBoard(2);
         assertEquals(0,actualBoard.getUserId());
         assertEquals("TEST1234",actualBoard.getTitle());
         assertEquals("TEST1234",actualBoard.getContent());
         
         assertThat(mav.getViewName(),is("redirect:/board/modifypage/2"));
     }
     
     @Test
     public void modifyBoardWithoutSession() throws Exception{
     	request.setRequestURI("/board/modify/1");
         request.setMethod("POST");
         request.setParameter("id", "1");
         request.setParameter("title", "TEST");
         request.setParameter("content", "TEST");
         
         ModelAndView mav = adapter.handle(request, response, controller);
 
         assertThat(mav.getViewName(),is("redirect:/indexpage")); ;
     }
     
     @Test
     public void deleteBoard() throws Exception{
     	request.setRequestURI("/board/delete/1");
         request.setMethod("POST");
         
         session.setAttribute("loginUser", new User());
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         //verify(boardService).removeBoard(1);
         //assertThat(mav.getModelMap().get("boardList"),is(List.class));
         //assertThat(mav.getViewName(),is("board/list"));
         Board actualBoard = boardService.getBoard(1);
         assertEquals(actualBoard,null);
         assertThat(mav.getViewName(),is("redirect:/board/listpage"));
     }
     
     @Test
     public void deleteBoardWithoutSession() throws Exception{
     	request.setRequestURI("/board/delete/1");
         request.setMethod("POST");
         
         ModelAndView mav = adapter.handle(request, response, controller);
 
         assertThat(mav.getViewName(),is("redirect:/indexpage")); ;
     }
     
     @Test
     public void addComment() throws Exception{
     	request.setRequestURI("/board/addcomment/3");
         request.setMethod("POST");
         request.setParameter("comment", "Test");
         request.setParameter("password","1234");
         
         session.setAttribute("loginUser", new User());
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         Comment actualComment = commentService.getComment(2);
         
         assertEquals("Test",actualComment.getComment());
         assertEquals("1234",actualComment.getPassword());
         assertThat(mav.getViewName(),is("redirect:/board/detailpage"));
     }
     
     @Test
     public void addCommentWithoutSession() throws Exception{
     	request.setRequestURI("/board/addcomment/3");
         request.setMethod("POST");
         request.setParameter("comment", "Test");
         request.setParameter("password","1234");
         
         //session.setAttribute("loginUser", new User());
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         assertThat(mav.getViewName(),is("redirect:/indexpage"));
     }
     
     @Test
     public void deleteComment() throws Exception{
     	request.setRequestURI("/board/deletecomment/1");
         request.setMethod("POST");
         
         session.setAttribute("loginUser", new User());
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         Comment actualComment = commentService.getComment(1);
         
         assertEquals(actualComment,null);
         assertThat(mav.getViewName(),is("redirect:/board/detailpage"));
     }
     
     @Test
     public void deleteCommentWithoutSession() throws Exception{
     	request.setRequestURI("/board/deletecomment/1");
         request.setMethod("POST");
         
         ModelAndView mav = adapter.handle(request, response, controller);
         
         assertThat(mav.getViewName(),is("redirect:/indexpage"));
     }
     */
 }
