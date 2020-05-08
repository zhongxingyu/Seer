 package org.emsionline.emsiweb.service.orderform;
 
 import java.io.IOException;
 
 import org.emsionline.emsiweb.domain.orderform.Cart;
 import org.emsionline.emsiweb.domain.orderform.CartItem;
 import org.emsionline.emsiweb.domain.orderform.CatalogItem;
 import org.emsionline.emsiweb.domain.orderform.CustomerInfo;
 import org.emsionline.emsiweb.mail.EmailService;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
 
 import freemarker.template.TemplateException;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "/test-root-context.xml" })
 public class MailTest {
 	
 	
 	@Autowired
 	JavaMailSender mailSender;
 	
 	@Autowired
 	FreeMarkerConfigurationFactoryBean configFactory;
 
 	@Autowired
 	String fromAddress;
 
 	@Autowired
 	String bccAddress;
 	
 	@Autowired
 	String subject;
 	
 	@Test
	@Ignore
 	public void testFreemarkHtmlMessage() throws IOException, TemplateException {
 		
 		CatalogItem catItem1 = new CatalogItem();
 		catItem1.setItemCode("1A0001");
 		catItem1.setItemShortDescr("創世與末世");
 		catItem1.setAuthor("亨利．默瑞斯 著 國際福音神學佈道會譯");
 		catItem1.setNumVolumes(1);
 		catItem1.setPriceUS(7.00);
 		
 		CatalogItem catItem2 = new CatalogItem();
 		catItem2.setItemCode("1D0001");
 		catItem2.setItemShortDescr("信仰與科學叢書系列(一)");
 		catItem2.setAuthor("亨利．默瑞斯 著 國際福音神學佈道會譯");
 		catItem2.setNumVolumes(1);
 		catItem2.setPriceUS(5.00);
 
 		
 		Cart cart = new Cart();
 		CartItem item = new CartItem(catItem1);
 		item.setQuantity(5);
 		cart.add(item);
 		item = new CartItem(catItem2);
 		item.setQuantity(2);
 		cart.add(item);
 		
 		
 		CustomerInfo custInfo = new CustomerInfo();
 		custInfo.setPrefix("Mr.");
 		custInfo.setFirstname("Joe");
 		custInfo.setLastname("Bloggs");
 		custInfo.setAddress1("1010 Mockingbird Lane");
 		custInfo.setCity("Smallville");
 		custInfo.setStateProvince("IL");
 		custInfo.setZipPostalCode("99999");
 		custInfo.setCountryCode("US");
 		custInfo.setEmail("robert.tam+test@gmail.com");
 		
 		EmailService emailService = new EmailService(mailSender, configFactory);
 		emailService.sendConfirmationEmail(fromAddress, bccAddress, subject, custInfo, cart);
 	}
 		
 }
