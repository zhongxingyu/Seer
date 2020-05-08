 package org.zkoss.zats.example.testcase;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import javax.servlet.http.HttpSession;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.zkoss.zats.mimic.ComponentAgent;
 import org.zkoss.zats.mimic.Conversations;
 import org.zkoss.zats.mimic.DesktopAgent;
 import org.zkoss.zats.mimic.Searcher;
 import org.zkoss.zats.mimic.operation.ClickAgent;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.Window;
 
 public class ConversationsTest
 {
 	@BeforeClass
 	public static void init()
 	{
 //		Conversations.start("."); //from project folder
 		Conversations.start("./src/main/webapp"); // user can load by configuration file
 	}
 
 	@AfterClass
 	public static void end()
 	{
 		Conversations.stop();//
 	}
 
 	@After
 	public void after()
 	{
 		Conversations.clean();
 	}
 
 	@Test
 	public void test()
 	{
		Conversations.open("/session.zul");
 
 		assertNotNull(Conversations.getSession());
 		assertNotNull(Conversations.getDesktop());
 
 		HttpSession session = Conversations.getSession();
 		DesktopAgent desktop = Conversations.getDesktop();
 		assertEquals("session", session.getAttribute("msg"));
 		assertEquals("desktop", desktop.getAttribute("msg"));
 		
 		ComponentAgent win = Searcher.find("#win");
 		assertNotNull(win);
 		assertNotNull(win.as(Window.class));
 		assertEquals("my window",win.as(Window.class).getTitle());
 		
 		ComponentAgent msg = Searcher.find(win, "#msg"); 
 		assertNotNull(msg);
 		assertEquals("hello", msg.as(Label.class).getValue());
 //		assertEquals("hello", ((Label)msg.nat()).getValue());
 		
 		for(int i = 0; i < 10; ++i)
 		{
 			Searcher.find(win, "#btn").as(ClickAgent.class).click();
 			assertEquals("s" + i, session.getAttribute("msg"));
 			assertEquals("d" + i, desktop.getAttribute("msg"));
 			assertEquals("" + i, msg.as(Label.class).getValue());
 		}
 	}
 }
