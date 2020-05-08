 package com.google.wave.extensions.emaily.robot;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.wave.api.AbstractRobotServlet;
 import com.google.wave.api.Annotation;
 import com.google.wave.api.Blip;
 import com.google.wave.api.Event;
 import com.google.wave.api.EventType;
 import com.google.wave.api.Range;
 import com.google.wave.api.RobotMessageBundle;
 import com.google.wave.api.TextView;
 import com.google.wave.extensions.emaily.email.EmailAddressUtil;
 import com.google.wave.extensions.emaily.email.EmailSender;
 
 @Singleton
 public class EmailyRobotServlet extends AbstractRobotServlet {
   private static final long serialVersionUID = 8878209094937861353L;
 
   // Injected dependencies
   private final EmailAddressUtil emailAddressUtil;
   private final EmailSender emailSender;
 
   @Inject
   public EmailyRobotServlet(EmailAddressUtil emailAddressUtil,
       EmailSender emailSender) {
     this.emailAddressUtil = emailAddressUtil;
     this.emailSender = emailSender;
   }
 
   /**
    * Handles all robot events.
    */
   @Override
   public void processEvents(RobotMessageBundle bundle) {
     for (Event event : bundle.getEvents()) {
       if (event.getType() == EventType.BLIP_SUBMITTED) {
         handleBlipSubmitted(bundle, event);
       }
     }
   }
 
   /**
    * Handle when a blip is submitted. Currenlty it sends the blip in email
    * immediately.
    * 
    * @param bundle RobotMessageBundle received from the Wave Server.
    * @param event The BLIP_SUBMITTED event.
    */
   private void handleBlipSubmitted(RobotMessageBundle bundle, Event event) {
     // Find Email Subject
     Blip rootBlip = bundle.getWavelet().getRootBlip();
     TextView rootTextView = rootBlip.getDocument();
     String emailSubject;
    String emailBody;
     List<Annotation> titleAnnotations = rootTextView
         .getAnnotations("conv/title");
     if (titleAnnotations.size() > 0) {
       Range subjectRange = titleAnnotations.get(0).getRange();
       emailSubject = rootTextView.getText().substring(0, subjectRange.getEnd());
      emailBody = rootTextView.getText().substring(subjectRange.getEnd());
     } else {
       emailSubject = "";
      emailBody = rootTextView.getText();
     }
 
     // Get sender email addresses.
    String senderEmail = emailAddressUtil.emailAddressToWaveParticipantId(event.getBlip()
        .getCreator());
 
     // Get recipients
     List<String> recipients = new ArrayList<String>();
     for (String participant : bundle.getWavelet().getParticipants()) {
      String emailAddress = emailAddressUtil.waveParticipantIdToEmailAddress(participant);
       if (emailAddress != null) {
         recipients.add(emailAddress);
       }
     }
     if (recipients.size() > 0) {
       emailSender.simpleSendTextEmail(senderEmail, recipients, emailSubject,
           emailBody);
     }
   }
 
 }
