 package nl.surfnet.bod.service;
 
 import nl.surfnet.bod.domain.ActivationEmailLink;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 
 import org.springframework.mail.SimpleMailMessage;
 
 public interface EmailSender {
 
   void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink);
 
  void sendVirtualPortRequestMail(String to, String from, String requestMessage, PhysicalResourceGroup pGroup,
      VirtualResourceGroup vGroup);
 
   void send(SimpleMailMessage mail);
 
   void setFromAddress(String fromAddress);
 
   void setExternalBodUrl(String externalBodUrl);
 
 }
