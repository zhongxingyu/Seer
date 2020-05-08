 package models;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 import util.TestHelper;
 import util.Utils;
 import util.XStreamHelper;
 import exception.CommandException;
 
 public class MailTest extends UnitTest {
 
 	@Before
 	public void init() {
 		TestHelper.init("address=paolo@crg.es");
 	}
 	
 	@Test
 	public void fromXML () {
 		String xml = 
 			"<mail >" +
 				"<from>paolo@crg.es</from>" + 
 				"<to>cedric@crg.es</to>" + 
 				"<cc>nobody</cc>" + 
 				"<bcc>email@gmail.com</bcc>" + 
 				"<subject>Hola</subject>" +
 				"<body>Hi your alignment is complete</body>" +
 			"</mail>"; 
 		
 		Mail mail = XStreamHelper.fromXML(xml);
 		
 		assertEquals("paolo@crg.es", mail.from.raw);
 		assertEquals("cedric@crg.es", mail.to.raw);
 		assertEquals("nobody", mail.cc.raw);
 		assertEquals("Hola", mail.subject.raw);
 		assertEquals("email@gmail.com", mail.bcc.raw);
 		assertEquals("Hi your alignment is complete", mail.body.raw);
 		
 	}
 
 	@Test 
 	public void testCopy() { 
 		Mail mail = new Mail();
 		mail.from = new Eval("from@host.com");
 		mail.to = new Eval("to@host.com");
 		mail.reply = new Eval("reply@host.com");
 		mail.cc = new Eval("cc@host.com");
 		mail.bcc = new Eval("bcc@host.com");
 		mail.subject = new Eval("subject");
 		mail.body = new Eval("body");
 		
 		Mail copy = Utils.copy(mail);
 		
 		assertEquals( mail.from, copy.from );
 		assertEquals( mail.reply, copy.reply );
 		assertEquals( mail.to, copy.to );
 		assertEquals( mail.cc, copy.cc );
 		assertEquals( mail.bcc, copy.bcc );
 		assertEquals( mail.subject, copy.subject );
 		assertEquals( mail.body, copy.body );
 		
 		assertEquals( mail, copy );
 	}
 	
 	@Test
 	public void testExecute() throws CommandException {
 		Mail mail = new Mail();
 		mail.from = new Eval("paolo.ditommaso@gmail.com");
 		mail.to = new Eval("paolo.ditommaso@crg.es");
 		mail.subject = new Eval("Hola");
 		mail.body = new Eval("This is the mail content");
 		
 		assertTrue(mail.execute());
 		assertTrue(mail.fSent);
 	}
 	
 	@Test 
 	public void testTrueWithoutTO () throws CommandException {
 
 		Mail mail = new Mail();
 		mail.from = new Eval("paolo.ditommaso@gmail.com");
 
 		assertTrue(mail.execute());
		assertFalse(mail.fSent);
 	}
 	
 
 	
 	@Test 
 	public void testVariableAddress() {
 		Mail mail = new Mail();
 		mail.from = new Eval("paolo.ditommaso@gmail.com");
 		mail.to = new Eval("${address}"); // <--check in the @Before initialization on top ...
 		
 		
 		assertEquals( "paolo@crg.es", mail.to.eval() );
 		
 	}
 }
