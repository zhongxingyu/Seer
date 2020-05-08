 package com.ratethisfest.server.logic;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import com.ratethisfest.server.domain.Rating;
 import com.ratethisfest.shared.Set;
 
 public class LollaEmailSender {
   // TODO: put this in resource file
   public static final String SENDER_EMAIL = "info@lollapaloozer.com";
   public static final String SENDER_TITLE = "Lollapaloozer";
   public static final String SUBJECT = "Your Lollapalooza Set Ratings";
 
   public static String emailRatings(String authType, String authId, String authToken, String email) {
     String result;
     Properties props = new Properties();
     Session session = Session.getDefaultInstance(props, null);
 
     try {
       Message msg = new MimeMessage(session);
       msg.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_TITLE));
       msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
       msg.setSubject(SUBJECT);
       String messageBody = generateMessageBody(authType, authId, authToken, email);
       if (messageBody.isEmpty()) {
         result = "no ratings to send";
       } else {
         msg.setText(messageBody);
         Transport.send(msg);
         result = "Ratings successfully sent to " + email;
       }
     } catch (AddressException ae) {
       result = ae.getClass().getCanonicalName();
       result += ": " + ae.getMessage();
     } catch (MessagingException me) {
       result = me.getClass().getCanonicalName();
       result += ": " + me.getMessage();
     } catch (UnsupportedEncodingException uee) {
       result = uee.getClass().getCanonicalName();
       result += ": " + uee.getMessage();
     } catch (Exception e) {
       result = e.getClass().getCanonicalName();
       result += ": " + e.getMessage();
     }
 
     return result;
   }
 
   private static String generateMessageBody(String authType, String authId, String authToken,
       String email) {
     List<Rating> ratings = LollaRatingManager.getInstance().findRatingsByUserAndYear(authType,
         authId, authToken, email, 2012);
     if (ratings == null) {
       return null;
     }
     List<String> ratingStrings = new ArrayList<String>();
     for (Rating rating : ratings) {
       Set set = LollaRatingManager.getInstance().findSet(rating.getSet().getId());
       StringBuilder ratingString = new StringBuilder();
       ratingString.append(set.getYear());
       ratingString.append(" - ");
       ratingString.append(set.getArtistName());
       ratingString.append(": ");
       ratingString.append(rating.getScore());
       if (rating.getNotes() != null && !rating.getNotes().isEmpty()) {
        ratingString.append("\n");
         ratingString.append("\"");
         ratingString.append(rating.getNotes());
         ratingString.append("\"");
       }
       ratingString.append("\n");
       ratingString.append("\n");
       ratingStrings.add(ratingString.toString());
     }
     ArrayList<String> sortedItems = new ArrayList<String>(ratingStrings);
     Collections.sort(sortedItems, STRING_NAME_COMPARATOR);
     StringBuilder messageBody = new StringBuilder();
     for (String ratingString : sortedItems) {
       messageBody.append(ratingString);
     }
     return messageBody.toString();
   }
 
   public static final Comparator<? super String> STRING_NAME_COMPARATOR = new Comparator<String>() {
     public int compare(String t0, String t1) {
       return t0.compareToIgnoreCase(t1);
     }
   };
 
 }
