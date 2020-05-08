 package eu.nets.crazycrow.nsa;
 
 import static eu.nets.crazycrow.nsa.TwitterPaymentProcessor.toPaymentInstruction;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.math.BigDecimal;
 
 import org.junit.Test;
 
 import twitter4j.Status;
 import twitter4j.User;
 
 public class TwitterPaymentProcessorTest {
 
 	// 1 - @fra: @til ... Her får du 100 kroner ...
 	// 2 - @fra: @til ... Jeg sender deg 100 spenn ...
 	
 	@Test
 	public void shouldAcceptFormat1() throws Exception {
 		PaymentInstruction paymentInstruction = toPaymentInstruction(status("@runepeter", "@steingrd Her får du 100 kroner"));
 		
 		assertThat(paymentInstruction.getSource()).isEqualTo(Source.Twitter);
 		assertThat(paymentInstruction.getDebitSocialId()).isEqualTo("@runepeter");
 		assertThat(paymentInstruction.getCreditSocialId()).isEqualTo("@steingrd");
		assertThat(paymentInstruction.getAmount()).isEqualTo(new BigDecimal("100.00"));
 	}
 
 	private Status status(String sender, String text) {
 		User user = mock(User.class);
 		when(user.getName()).thenReturn(sender);
 		
 		Status status = mock(Status.class);
 		when(status.getText()).thenReturn(text);
 		when(status.getUser()).thenReturn(user);
 		return status;
 	}
 	
 }
