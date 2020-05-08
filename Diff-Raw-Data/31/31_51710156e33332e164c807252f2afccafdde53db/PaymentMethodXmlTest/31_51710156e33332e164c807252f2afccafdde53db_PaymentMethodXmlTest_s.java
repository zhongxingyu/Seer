 package com.feefighers.model;
 
 import java.io.IOException;
 import java.util.Date;
 
 import org.apache.commons.io.IOUtils;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

 public class PaymentMethodXmlTest {
 	
 	@Test
 	public void shouldSerializePaymentMethod() throws Exception {
 		// given
 		final Message message = new Message("input.cvv", "too_long", "");
 		message.messageClass = "error";
 		
 		final PaymentMethod paymentMethod = new PaymentMethod();
 		paymentMethod.setPaymentMethodToken("56f0c882c4565da74d2a748e");
		paymentMethod.setCreatedAt(new Date());
		paymentMethod.setUpdatedAt(new Date());
 		paymentMethod.setRetained(false);
 		paymentMethod.setRedacted(false);
 		paymentMethod.setSensitiveDataValid(false);		
 		paymentMethod.getMessageList().getList().add(message);
 		paymentMethod.setLastFourDigits("1111");
 		paymentMethod.setFirstName("Joe");
 		paymentMethod.setLastName("FeeFighter");
 		paymentMethod.setExpiryMonth(1);
 		paymentMethod.setExpiryYear(2011);
		final String xml = IOUtils.toString(getClass().getResourceAsStream("payment_method.xml"));		
 				
 		// when
 		final String outputXml = paymentMethod.toXml();
 		
 		// then
 		Assert.assertEquals(outputXml.trim(), xml.trim());		
 	}	
 	
 	@Test
 	public void shouldDeserializePaymentMethod() throws IOException {
 		// given
 		final String xml = IOUtils.toString(getClass().getResourceAsStream("payment_method.xml"));
 		
 		// when
 		final PaymentMethod paymentMethod = PaymentMethod.fromXml(xml);
 		final String outputXml = paymentMethod.toXml(); 
 		
 		// then
 		Assert.assertEquals(outputXml.trim(), xml.trim());
 	}		
 }
