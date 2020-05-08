 package nl.han.dare2date.service.web;
 
 import java.util.logging.Level;
 import javax.jms.JMSException;
 import javax.naming.NamingException;
 import nl.han.dare2date.applyregistrationservice.ApplyRegistrationRequest;
 import nl.han.dare2date.applyregistrationservice.ApplyRegistrationResponse;
 import nl.han.dare2date.applyregistrationservice.Creditcard;
 import nl.han.dare2date.applyregistrationservice.Registration;
 import org.apache.log4j.Logger;
 import org.springframework.oxm.Marshaller;
 import org.springframework.oxm.Unmarshaller;
 import org.springframework.ws.server.endpoint.annotation.Endpoint;
 import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
 
 @Endpoint
 public class ApplyRegistrationServiceEndpoint {
 
     private static final String REG_SUCCESSFUL_TOPIC = "RegistrationConfirmationTopic";
     private Logger log = Logger.getLogger(getClass().getName());
     private Marshaller marshaller;
     private Unmarshaller unmarshaller;
 
	@PayloadRoot(localPart = "ApplyRegistrationRequest", namespace = "http://www.han.nl/schemas/messages")
	public ApplyRegistrationResponse applyRegistration(ApplyRegistrationRequest req) {
            boolean success = false;
            System.out.println("Yz: REQUEST");
            ApplyRegistrationResponse ret = new ApplyRegistrationResponse();
            Creditcard cc = req.getRegistration().getUser().getCard();
        
            //check if valid
            //Connection con = JMSUtil.getConnection();
            ValidateCreditcardService vccs;
         try {
             vccs = new ValidateCreditcardService();
             success = vccs.validate(cc);
 
         } catch (JMSException ex) {
             java.util.logging.Logger.getLogger(ApplyRegistrationServiceEndpoint.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NamingException ex) {
             java.util.logging.Logger.getLogger(ApplyRegistrationServiceEndpoint.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
 
 
         //invalid -> ValidateCreditcardservice
         //valid -> ConfirmRegistrationService
 
         ret.setRegistration(req.getRegistration());
         ret.getRegistration().setSuccesFul(success);
         return ret;
     }
 
     private void notifyEveryone(Registration reg) {
         try {
             ConfirmRegistrationService confirm = new ConfirmRegistrationService(REG_SUCCESSFUL_TOPIC);
             confirm.confirm(reg);
 
         } catch (JMSException | NamingException ex) {
             log.error("", ex);
         }
     }
 }
