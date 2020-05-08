 package nl.surfnet.bod.service;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.annotation.PostConstruct;
 
 import nl.surfnet.bod.domain.ActivationEmailLink;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.web.manager.ActivationEmailController;
 import nl.surfnet.bod.web.manager.VirtualPortController;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.util.StringUtils;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 
 public class EmailSenderOnline implements EmailSender {
 
   @Value("${mail.fromAddress}")
   private String fromAddress;
 
   @Value("${external.bod.url}")
   private String externalBodUrl;
 
   @Autowired
   private MailSender mailSender;
 
   private static final String ACTIVATION_BODY = "Please click the link to activate this email adres for physical resource group: %s";
 
   private static final String VIRTUAL_PORT_REQUEST_BODY = "Dear ICT Manager,\n\n"
       + "You have received a new Virtual Port Request.\n\n" + "Who: %s\n" + "Reason: %s\n\n"
       + "Click on the following link %s to create the virtual port";
 
   @PostConstruct
   protected void init() {
     if (!StringUtils.endsWithIgnoreCase(externalBodUrl, "/")) {
       externalBodUrl = externalBodUrl.concat("/");
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * nl.surfnet.bod.service.EmailSender#sendActivationMail(nl.surfnet.bod.domain
    * .ActivationEmailLink)
    */
   @Override
   public void sendActivationMail(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
     String bodyText = String.format(ACTIVATION_BODY, generateActivationUrl(activationEmailLink).toExternalForm());
 
     SimpleMailMessage mail = new MailMessageBuilder().withTo(activationEmailLink.getToEmail())
         .withSubject("Activation mail for Physical Resource Group " + activationEmailLink.getSourceObject().getName())
         .withBodyText(bodyText).create();
 
     send(mail);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * nl.surfnet.bod.service.EmailSender#sendVirtualPortRequestMail(java.lang
    * .String, java.lang.String, java.lang.String,
    * nl.surfnet.bod.domain.PhysicalResourceGroup,
    * nl.surfnet.bod.domain.VirtualResourceGroup)
    */
   @Override
  public void sendVirtualPortRequestMail(String to, String from, String requestMessage, PhysicalResourceGroup pGroup,
      VirtualResourceGroup vGroup) {
     String link = String.format(externalBodUrl + VirtualPortController.PAGE_URL + "/create?vgroup=%d&pgroup=%d",
         vGroup.getId(), pGroup.getId());
 
    SimpleMailMessage mail = new MailMessageBuilder().withTo(to).withReplyTo(from)
         .withSubject("A Virtual Port Request")
        .withBodyText(String.format(VIRTUAL_PORT_REQUEST_BODY, from, requestMessage, link)).create();
 
     send(mail);
   }

   private URL generateActivationUrl(ActivationEmailLink<PhysicalResourceGroup> activationEmailLink) {
     if (!StringUtils.endsWithIgnoreCase(externalBodUrl, "/")) {
       externalBodUrl = externalBodUrl + "/";
     }
 
     try {
       return new URL(String.format(externalBodUrl + "%s/%s", ActivationEmailController.ACTIVATION_MANAGER_PATH,
           activationEmailLink.getUuid()));
     }
     catch (MalformedURLException e) {
       throw new RuntimeException(e);
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see nl.surfnet.bod.service.EmailSender#send(org.springframework.mail.
    * SimpleMailMessage)
    */
   @Override
   public void send(SimpleMailMessage mail) {
     mailSender.send(mail);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see nl.surfnet.bod.service.EmailSender#setFromAddress(java.lang.String)
    */
   @Override
   public void setFromAddress(String fromAddress) {
     this.fromAddress = fromAddress;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see nl.surfnet.bod.service.EmailSender#setExternalBodUrl(java.lang.String)
    */
   @Override
   public void setExternalBodUrl(String externalBodUrl) {
     this.externalBodUrl = externalBodUrl;
   }
 
   private final class MailMessageBuilder {
     private String to;
     private String subject;
     private String text;
     private String replyTo;
 
     public MailMessageBuilder() {
     }
 
     public SimpleMailMessage create() {
       Preconditions.checkState(Strings.emptyToNull(to) != null);
       Preconditions.checkState(Strings.emptyToNull(subject) != null);
       Preconditions.checkState(Strings.emptyToNull(text) != null);
 
       SimpleMailMessage mailMessage = new SimpleMailMessage();
       mailMessage.setFrom(fromAddress);
       mailMessage.setTo(to);
       mailMessage.setReplyTo(replyTo);
       mailMessage.setSubject(subject);
       mailMessage.setText(text);
       return mailMessage;
     }
 
     public MailMessageBuilder withReplyTo(String replyTo) {
       this.replyTo = replyTo;
       return this;
     }
 
     public MailMessageBuilder withSubject(String subject) {
       this.subject = subject;
       return this;
     }
 
     public MailMessageBuilder withTo(String to) {
       this.to = to;
       return this;
     }
 
     public MailMessageBuilder withBodyText(String text) {
       this.text = text;
       return this;
     }
   }
 
 }
