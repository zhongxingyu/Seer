 package bot;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import models.AppProps;
 import models.Bundle;
 import models.Field;
 import models.Service;
 
 import org.apache.commons.mail.Email;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.mvc.Scope;
 import play.test.UnitTest;
 import util.Utils;
 
 public class BotWorkerTest extends UnitTest{
 
 	private Bundle bundle;
 	private BotWorker worker;
 	private Service service;
 	
 	@Before
 	public void before() throws AddressException { 
 		bundle = Bundle.read( new File(AppProps.BUNDLES_FOLDER, "tcoffee") );
 		worker = new BotWorker();
 		worker.service = bundle.services.get(0);
 		worker.subject = "test";
 		worker.mailFrom = AppProps.instance().getWebmasterEmail();
 		worker.to = new InternetAddress("pablo@host.com");
 
 		Service.current(service=worker.service);
 		//ValidationHack.set();
         Scope.RouteArgs.current.set(new Scope.RouteArgs());
         
 		Field fieldSeqs = Utils.firstItem(service.input.fields(), "name", "seqs");
 		fieldSeqs.value = "something";
 
 		service.init();
 	}
 	
 	@Test
 	public void testParseSubjectBody() { 
 		
 		String test = 
 			"[hola]\n" +
 			"\n\n\nthis is the mail content\n" +
 			"cheers";
 		
 		String result[] = BotWorker.parseSubjectBodyParts(test);
 		
 		assertEquals( "hola", result[0] );
 		assertEquals( "this is the mail content\ncheers", result[1] );
 		
 		/* 
 		 * without subject 
 		 */
 		result = BotWorker.parseSubjectBodyParts("blah\nblah\blah"); 
 		assertEquals( null, result[0] );
 		assertEquals( "blah\nblah\blah", result[1] );
 	}
 
 	
 	@Test 
 	public void testLoadCachedTemplete() throws IOException, MessagingException { 
 		
 		String from = AppProps.instance().getWebmasterEmail();
 		Email result = worker.loadMailTemplate("bot-cached-result.txt");
 		
		assertEquals(  "test - T-Coffee request "+service.rid()+" (CACHED)", result.getSubject() );
 		assertEquals(  from, result.getFromAddress().getAddress() );
 		assertEquals(  "pablo@host.com", ((InternetAddress)result.getToAddresses().get(0)).getAddress() );
 	}
 	
 	
 	@Test 
 	public void testLoadTemplateResultOk() throws IOException, MessagingException { 
 		
 		String from = AppProps.instance().getWebmasterEmail();
 		Email result = worker.loadMailTemplate("bot-result-ok.txt");
 		
		assertEquals(  "test - T-Coffee request "+service.rid()+" (DONE)", result.getSubject() );
 		assertEquals(  from, result.getFromAddress().getAddress() );
 		assertEquals(  "pablo@host.com", ((InternetAddress)result.getToAddresses().get(0)).getAddress() );
 	}
 	
 
 	@Test 
 	public void testLoadTemplateResultFail() throws IOException, MessagingException { 
 		
 		String from = AppProps.instance().getWebmasterEmail();
 		Email result = worker.loadMailTemplate("bot-result-fail.txt");
 		
		assertEquals(  "test - T-Coffee request "+service.rid()+" (fail)", result.getSubject() );
 		assertEquals(  from, result.getFromAddress().getAddress() );
 		assertEquals(  "pablo@host.com", ((InternetAddress)result.getToAddresses().get(0)).getAddress() );
 	}
 	
 	
 	@Test 
 	public void testLoadTemplateInvlidaData() throws IOException, MessagingException { 
 		
 		String from = AppProps.instance().getWebmasterEmail();
 		Email result = worker.loadMailTemplate("bot-invalid-data.txt");
 		
		assertEquals(  "test - T-Coffee request (invalid data)", result.getSubject() );
 		assertEquals(  from, result.getFromAddress().getAddress() );
 		assertEquals(  "pablo@host.com", ((InternetAddress)result.getToAddresses().get(0)).getAddress() );
 	}
 
 	
 	
 	
 }
