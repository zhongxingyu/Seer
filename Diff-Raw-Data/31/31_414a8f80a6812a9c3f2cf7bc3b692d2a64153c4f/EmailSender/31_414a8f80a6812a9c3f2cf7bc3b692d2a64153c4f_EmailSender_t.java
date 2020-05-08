 package nl.surfnet.bod.service;
 
 import nl.surfnet.bod.domain.ActivationEmailLink;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.springframework.mail.SimpleMailMessage;
 
 public interface EmailSender {
 
   void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink);
 
  void sendVirtualPortRequestMail(RichUserDetails from, PhysicalResourceGroup pGroup, VirtualResourceGroup vGroup,
      String requestMessage);
 
   void send(SimpleMailMessage mail);
 
   void setFromAddress(String fromAddress);
 
   void setExternalBodUrl(String externalBodUrl);
 
 }
