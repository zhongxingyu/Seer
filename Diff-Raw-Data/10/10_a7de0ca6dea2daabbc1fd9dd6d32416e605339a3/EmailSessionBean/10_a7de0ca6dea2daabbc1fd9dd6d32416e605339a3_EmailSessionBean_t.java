 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ERMS.session;
 
 import ACMS.entity.ReservationEntity;
 import ACMS.entity.RoomEntity;
 import ATMS.entity.AttrComboEntity;
 import ATMS.entity.AttrTicketEntity;
 import ATMS.entity.TicketPurchaseEntity;
 import SMMS.entity.ContractEntity;
 import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
 import com.lowagie.text.Document;
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.Element;
 import com.lowagie.text.Font;
 import com.lowagie.text.FontFactory;
 import com.lowagie.text.Image;
 import com.lowagie.text.PageSize;
 import com.lowagie.text.Paragraph;
 import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfAction;
 import com.lowagie.text.pdf.PdfPCell;
 import com.lowagie.text.pdf.PdfPTable;
 import com.lowagie.text.pdf.PdfWriter;
 import java.awt.Color;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import javax.ejb.Stateless;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 /**
  *
  * @author Ser3na
  */
 @Stateless
 public class EmailSessionBean {
 
     String emailServerName = "smtp.gmail.com";
 
     public EmailSessionBean() {
     }
 
     public void emailInitialPassward(String toEmailAdress, String initialPassword) {
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.port", "465");
 
         Session session = Session.getInstance(props, new javax.mail.Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("is3102.it09", "weloveTWK");
             }
         });
 
         try {
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("is3102.it09@gmail.com"));
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(toEmailAdress));
             message.setSubject("Initial Password");
             message.setText("Greeting from Coral Island Resort!"
                     + "\nHere is your initial password:" + initialPassword +
                     "\nPlease login immediately to change your password.\n\n\nBest Regards,\nThe Coral Island Management Team");
                     
 
             Transport.send(message);
 
             System.out.println("Done");
 
         } catch (MessagingException e) {
             throw new RuntimeException(e);
         }
     }
     
     public void emailGeneratedPassword(String toEmailAdress, String initialPassword) {
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.port", "465");
 
         Session session = Session.getInstance(props, new javax.mail.Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("is3102.it09", "weloveTWK");
             }
         });
 
         try {
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("is3102.it09@gmail.com"));
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(toEmailAdress));
             message.setSubject("System Generated Password");
             message.setText("Greeting from Coral Island Resort!"
                     + "\nHere is a system generated password:" + initialPassword +
                     "\nPlease login immediately to change your password.\n\n\nBest Regards,\nThe Coral Island Management Team");
                     
 
             Transport.send(message);
 
             System.out.println("Done");
 
         } catch (MessagingException e) {
             throw new RuntimeException(e);
         }
     }
     
     
     public void emailCorporateBill(String toEmailAdress, RoomEntity room) throws IOException, FileNotFoundException, DocumentException {
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.port", "465");
 
         Session session = Session.getInstance(props, new javax.mail.Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("chrislx.nus", "ldc!((!1991");
             }
         });
 
         try {
             double totalBill = room.getRoomPrice().getPrice()*5 + room.getRoomServiceCharge();
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("chrislx.nus@gmail.com"));
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(toEmailAdress));
             message.setSubject("Your bill from Coral Island Resort");
             String text = "Thank you for coming to Coral Island Resort!"
                     + "\nYour staff " +room.getGuestName() + " has come to our hotel "
                     + "\nfrom " + room.getCheckInDate() + " to " + room.getCheckOutDate()
                     + "\nThe room fee is " + room.getRoomPrice().getPrice() + " per day,"
                     + " \nand the service charge is " +room.getRoomServiceCharge()
                     + "\nYou have a total bill of " + totalBill
                     + "\nPlease clear your bill in 10 days."
                     + "\nThank you. " 
                     + "\n\nIn case of any issues and inqueries, you may contact our corporate service manager "
                     + "\n@ 65-8180 1380"
                     + "\n\n\nBest Regards,\nThe Coral Island Management Team";
             
             String INPUTFILE;        
             INPUTFILE = createBill(toEmailAdress,room);
 
             
                 MimeBodyPart messageBodyPart;
                 MimeBodyPart textBodyPart;
                 
                 Multipart multipart = new MimeMultipart();
                 messageBodyPart = new MimeBodyPart();
                 String file;
                     file = INPUTFILE;
                 //Below attach a file within the email
                 String fileName = "CorelResort:Room Reservation";
                 messageBodyPart.setFileName(fileName);
                 messageBodyPart.attachFile(file);
                 //Below draft the contents of email
                 textBodyPart = new MimeBodyPart();
                 textBodyPart.setText(text);
                 
                 ((MimeMessage)message).setContent(multipart);
 
             System.out.println("Sending");
             Transport.send(message);
 
             System.out.println("Done");
 
         } catch (MessagingException e) {
             throw new RuntimeException(e);
         }
     }
     
     public void emailAttractionTicket(String toEmailAdress,TicketPurchaseEntity tp) throws IOException, FileNotFoundException, DocumentException {
         System.out.println("EmailSessionBean : emailAttractionTicket");
         
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.port", "465");
 
         Session session = Session.getInstance(props, new javax.mail.Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("is3102.it09", "weloveTWK");
             }
         });
         
         try {
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("is3102.it09@gmail.com"));
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(toEmailAdress));
             message.setSubject("Your attraction tickets from Coral Island Resort");
             
             AttrTicketEntity ticket;
             List<AttrTicketEntity> tkts = tp.getAttrTickets();
             List<Integer> quantities = tp.getAttrTicketQuantities();
             Integer quantity = 0;
             System.out.println("tkts size: " + tkts.size());
             System.out.println("quantities size: " + quantities.size());
             String information = "";
             for (int i = 0; i < tkts.size(); i++) {
                 ticket = tkts.get(i);
                 quantity = quantities.get(i);
                 information += quantity + " " + ticket.getAttrTicketName() + ", \n";
             }
             
             SimpleDateFormat sdf=new SimpleDateFormat("dd/MMM/yyyy");
             String dateString=sdf.format(tp.getAttrTicketBookDate());
             
             String text = "Thank you for visiting attractions at Coral Island Resort!\n"
                     +"\nHere's your ticket information: \n"
                     + information
                     + "\nDate: "+dateString
                     + "\nTotal Price: "+tp.getAttrTicketFee()
                     + "\nThe tickets sold are not refundable."
                     + "\nPlease print the attached ticket on the day of visit." 
                     + "\n\nIn case of any issues and inqueries, you may contact our corporate service manager "
                     + "\n@ 65-8180 1380"
                     + "\n\n\nBest Regards,\nThe Coral Island Management Team";
             
             message.setText(text);
     
 //            String INPUTFILE;        
 //            INPUTFILE = createAttrTicket(toEmailAdress,tp);
 
 //            
 //                MimeBodyPart messageBodyPart;
 //                MimeBodyPart textBodyPart;
 //                
 //                Multipart multipart = new MimeMultipart();
 //                messageBodyPart = new MimeBodyPart();
 ////                String file;
 ////                    file = INPUTFILE;
 ////                //Below attach a file within the email
 ////                String fileName = "CorelResort:Attraction Ticket";
 ////                messageBodyPart.setFileName(fileName);
 ////                messageBodyPart.attachFile(file);
 //                
 //                //Below draft the contents of email
 //                textBodyPart = new MimeBodyPart();
 //                textBodyPart.setText(text);
 //                
 //                ((MimeMessage)message).setContent(multipart);
 
             System.out.println("Sending");
             Transport.send(message);
 
             System.out.println("Done");
 
         }catch (MessagingException e) {
             throw new RuntimeException(e);
         }
         
     }
     
     
     public void emailReservationConfirmation(String toEmailAdress, ReservationEntity newReservation) {
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.port", "465");
 
         Session session = Session.getInstance(props, new javax.mail.Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("is3102.it09", "weloveTWK");
             }
         });
 
         try {
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("is3102.it09@gmail.com"));
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(toEmailAdress));
             message.setSubject("Thank you for your reservation");
             message.setText("Greeting from Coral Island Resort!"
                     +"\nYou have successfully make a reservation in Coral Island Resort Group. Thank you for your new reservation!"
                     + "\nHere is your Reservation Id:" + newReservation.getReservationId() 
                     + "\nBelow is your reservation detail:"
                     + "\nName: " + newReservation.getRcName()  
                     + "\nEmail Address: " + newReservation.getRcEmail()
                     +"\nCheckIn Date: " + newReservation.getRcCheckInDate()
                     +"\nCheckOut Date: " + newReservation.getRcCheckOutDate()
                     +"\nRoom Count " + newReservation.getReservationRoomCount()
                     +"\nRoom Type" + newReservation.getReservationRoomType()
                     + "\n\nIn case of any issues and inqueries, you may contact our corporate service manager "
                     + "\n@ 65-8180 1380"
                     + "\n\n\nBest Regards,\nThe Coral Island Management Team");
                     
             Transport.send(message);
 
             System.out.println("Done");
 
         } catch (MessagingException e) {
             throw new RuntimeException(e);
         }
     }
     
     public void emailRequest(String toEmailAdress, ContractEntity contract) {
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.port", "465");
 
         Session session = Session.getInstance(props, new javax.mail.Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("is3102.it09", "weloveTWK");
             }
         });
 
         try {
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("is3102.it09@gmail.com"));
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(toEmailAdress));
             message.setSubject("New Request");
             message.setText("Dear Contract Manager"
                     +"\n Greetings from Coral Island Resort!"
                     + "\n A new contract has been sent to you for review. Contract ID:"+contract.getContractId()+
                     "\nPlease login immediately and review the contract.\n\n\nBest Regards,\nThe Coral Island Management Team");
                     
 
             Transport.send(message);
 
             System.out.println("Done");
 
         } catch (MessagingException e) {
             throw new RuntimeException(e);
         }
     }
     
     public void emailAttractionTicketSingle(String toEmailAdress,TicketPurchaseEntity tpe) throws IOException, FileNotFoundException, DocumentException
     {
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.port", "465");
 
         Session session = Session.getInstance(props, new javax.mail.Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("is3102.it09", "weloveTWK");
             }
         });
 
         try {
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("is3102.it09@gmail.com"));
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(toEmailAdress));
             message.setSubject("New Request");
             message.setText("Dear Contract Operator"
                     +"\n Greetings from Coral Island Resort!"
                     + "\n The contract has been reviewed by the manager. Contract ID:"+contract.getContractId()
                     + "\n Contract Status is now: "+ contract.getLast().getEventStatus()
                     +"\n Please login immediately and review the contract.\n\n\nBest Regards,\nThe Coral Island Management Team");
                     
 
             Transport.send(message);
 
             System.out.println("Done");
 
         } catch (MessagingException e) {
             throw new RuntimeException(e);
         }
     }
    public void emailAttractionTicketCombo(String toEmailAdress,AttrComboEntity combo) throws IOException, FileNotFoundException, DocumentException
     {
         Properties props = new Properties();
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.port", "465");
         
         Session session = Session.getInstance(props, new javax.mail.Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("is3102.it09", "weloveTWK");
             }
         });
         
          try {
             Message message = new MimeMessage(session);
             message.setFrom(new InternetAddress("is3102.it09@gmail.com"));
             message.setRecipients(Message.RecipientType.TO,
                     InternetAddress.parse(toEmailAdress));
             message.setSubject("Your ticket from Coral Island Resort: Attraction "+combo.getAttrComboName());
             String text = "Thank you for booking ticket for Coral Island Resort Attraction services!"
                     +"\nYour Combo Ticket type is "+combo.getAttrComboType()
                     +"\nThe Combo Ticket is "+combo.getAttrComboName()
                     +"\nThe fee of tickets you have purchased is "+combo.getAttrComboPrice()
                     +"\nPlease refer to the attachment for your e-ticket: print the pdf file and bring it on the show date"
                     +"\nThank you for your support!"
                     +"\n\n\n For any queries, please call (+65)9272-8760";
             
             
                 String INPUTFILE = createTicketCombo(combo);        
            
 
             
                 MimeBodyPart messageBodyPart;
                 MimeBodyPart textBodyPart;
                 
                 Multipart multipart = new MimeMultipart();
                 messageBodyPart = new MimeBodyPart();
                 String file;
                     file = INPUTFILE;
                 //Below attach a file within the email
                 String fileName = "CorelResort:Room Reservation";
                 messageBodyPart.setFileName(fileName);
                 messageBodyPart.attachFile(file);
                 //Below draft the contents of email
                 textBodyPart = new MimeBodyPart();
                 textBodyPart.setText(text);
                 
                 ((MimeMessage)message).setContent(multipart);
 
             System.out.println("Sending");
             Transport.send(message);
 
             System.out.println("Done");
 
         } catch (MessagingException e) {
             throw new RuntimeException(e);
         }
         
     }
     private String createBill(String toEmailAdress, RoomEntity room) throws FileNotFoundException, DocumentException, BadElementException, MalformedURLException, IOException {
         //Below generate a PDF file
         Document document;
             document = new Document(PageSize.A4,50,50,50,50);
             String OUTPUTFILE = "C:\\Users\\Diana Wang\\Documents\\Diana\\Table_Reservation"+room.getRoomCorporate()
                     +room.getReservation().getReservationId()+".pdf";
             PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(OUTPUTFILE));
             document.open();
             
         //Below specify the font type
             Font catFont = new Font(Font.TIMES_ROMAN, 18,
       Font.BOLD);
         Font redFont = new Font(Font.TIMES_ROMAN, 12,
       Font.NORMAL,Color.RED);
         Font subFont = new Font(Font.TIMES_ROMAN, 16,
       Font.BOLD);
         Font tableFont;
         tableFont = new Font(Font.TIMES_ROMAN,16,Font.BOLD,Color.DARK_GRAY);
         Font smallItalic = new Font(Font.TIMES_ROMAN, 12,
       Font.BOLDITALIC);
         
         //Below specify contents
          String imagePath = "C:\\Users\\Diana Wang\\Documents\\NetBeansProjects\\coral_island_banner_customer.png";
          Image image = Image.getInstance(imagePath);
          document.add(image);
          
          Paragraph preface = new Paragraph();
          addEmptyLine(preface, 1);
          preface.add(new Paragraph("Your hotel booking summary", catFont));
          addEmptyLine(preface, 1);
          
          document.add(preface);
          
          //Below add a table
          PdfPTable table = new PdfPTable(2);
         table.setSpacingAfter(30);
         table.setSpacingBefore(30);
         table.setWidths(new int []{1,3});
         
         //Add table header
         PdfPCell c1 = new PdfPCell(new Phrase("Reservation Info"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
         
         c1 = new PdfPCell(new Phrase("Details & Remarks"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
         
         table.setHeaderRows(1);
         
         //Add table content
         table.addCell("Reservation ID");
         table.addCell(room.getReservation().getReservationId().toString());
         table.addCell("Guest Name");
         table.addCell(room.getGuestName());
         table.addCell("ReservationEmail");
         table.addCell(room.getReservation().getRcEmail());
         table.addCell("Check In Date");
         table.addCell(room.getCheckInDate().toString());
         table.addCell("Check Out Date");
         table.addCell(room.getCheckOutDate().toString());
         table.addCell("Hotel");
         table.addCell(Integer.toString(room.getRoomHotel()));
         table.addCell("Room Type");
         table.addCell(room.getRoomType());
         table.addCell("Number Of Rooms");
         table.addCell(Integer.toString(room.getReservation().getReservationRoomCount()));
         table.addCell("Number of Guests");
         table.addCell(Integer.toString(room.getReservation().getReservationGuestCount()));
         table.addCell("Status");
         table.addCell(room.getRoomStatus());
         table.addCell("Total Price");
         table.addCell(Double.toString(room.getReservation().getReservationTotal()));
         
         //below add notes paragraph
         document.add(new Paragraph("If it is a confirmed booking, please make sure you finish the booking"
                 + "process within 3 days"
                 , FontFactory.getFont(FontFactory.COURIER, 10,Font.BOLDITALIC ,Color.RED)));
         document.add(new Paragraph(""));
             
             return OUTPUTFILE;
     }
     
     //Add a empty line
     private Paragraph addEmptyLine(Paragraph paragraph, int number) {
        for (int k = 0; k < number; k++) {
       paragraph.add(new Paragraph(" "));
     }
        return paragraph;
     }
 
     private String createBill(String toEmailAdress, ReservationEntity newReservation) throws FileNotFoundException, DocumentException, BadElementException, MalformedURLException, IOException {
         
         //Below generate a PDF file
         Document document;
             document = new Document(PageSize.A4,50,50,50,50);
             String OUTPUTFILE = "C:\\Users\\Diana Wang\\Documents\\Diana\\RoomReservationConfirmation "+newReservation.getRcName()+
                     newReservation.getReservationId()+".pdf";
                     
             PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(OUTPUTFILE));
             document.open();
             
          //Below specify the font type
             Font catFont = new Font(Font.TIMES_ROMAN, 18,
       Font.BOLD);
         Font redFont = new Font(Font.TIMES_ROMAN, 12,
       Font.NORMAL,Color.RED);
         Font subFont = new Font(Font.TIMES_ROMAN, 16,
       Font.BOLD);
         Font tableFont;
         tableFont = new Font(Font.TIMES_ROMAN,16,Font.BOLD,Color.DARK_GRAY);
         Font smallItalic = new Font(Font.TIMES_ROMAN, 12,
       Font.BOLDITALIC);
         
        //Below specify contents
          String imagePath = "C:\\Users\\Diana Wang\\Documents\\NetBeansProjects\\coral_island_banner_customer.png";
          Image image = Image.getInstance(imagePath);
          document.add(image);
          
          Paragraph preface = new Paragraph();
          addEmptyLine(preface, 1);
          preface.add(new Paragraph("Your room booking confirmation summary", catFont));
          addEmptyLine(preface, 1);
          
          document.add(preface); 
          
          //Below add a table
          PdfPTable table = new PdfPTable(2);
         table.setSpacingAfter(30);
         table.setSpacingBefore(30);
         table.setWidths(new int []{1,3});
         
         //Add table header
         PdfPCell c1 = new PdfPCell(new Phrase("Reservation Info"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
         
         c1 = new PdfPCell(new Phrase("Details & Remarks"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
         
         table.setHeaderRows(1);
         
         //Add table contents
         table.addCell("Reservation ID");
         table.addCell(Long.toString(newReservation.getReservationId()));
         table.addCell("Guest Name");
         table.addCell(newReservation.getRcName());
         table.addCell("Guest Email");
         table.addCell(newReservation.getRcEmail());
         table.addCell("Hotel");
         table.addCell(Integer.toString(newReservation.getReservationHotelNo()));
         table.addCell("Type of Room");
         table.addCell(newReservation.getReservationRoomType());
         table.addCell("Check-in Date");
         table.addCell(newReservation.getRcCheckInDate().toString());
         table.addCell("Check-out Date");
         table.addCell(newReservation.getRcCheckOutDate().toString());
         table.addCell("Number of People");
         table.addCell(Integer.toString(newReservation.getReservationGuestCount()));
         table.addCell("Guest's Contact");
         table.addCell(newReservation.getRcHP());
         table.addCell("Credit Card Number");
         table.addCell(newReservation.getRcCreditCardNo());
         
          //below add notes paragraph
         Paragraph p = new Paragraph("If it is a confirmed booking, please make sure you finish the booking"
                 + "process within 3 days"
                 , FontFactory.getFont(FontFactory.COURIER, 10,Font.BOLDITALIC ,Color.RED));
         
         Chunk c;
         c = new Chunk("via localhost:8080/IRMSCustomer-war/");
         c.setAction(new PdfAction(new URL("localhost:8080/IRMSCustomer-war/")));
         p.add(c);
         document.add(p);
         document.add(new Paragraph(""));
         document.close();
         
         return OUTPUTFILE;
     }
 
     private String createTicket(TicketPurchaseEntity tpe) throws FileNotFoundException, DocumentException, BadElementException, MalformedURLException, IOException {
         
         //Below generate a PDF file
         Document document;
             document = new Document(PageSize.A4,50,50,50,50);
             String OUTPUTFILE = "C:\\Users\\Diana Wang\\Documents\\Diana\\SingleTicketReservation "+
                     tpe.getTpId()+".pdf";
                     
             PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(OUTPUTFILE));
             document.open();
             
         //Below specify the font type
         Font catFont = new Font(Font.TIMES_ROMAN, 18,
       Font.BOLD);
         Font redFont = new Font(Font.TIMES_ROMAN, 12,
       Font.NORMAL,Color.RED);
         Font subFont = new Font(Font.TIMES_ROMAN, 16,
       Font.BOLD);
         Font tableFont;
         tableFont = new Font(Font.TIMES_ROMAN,16,Font.BOLD,Color.DARK_GRAY);
         Font smallItalic = new Font(Font.TIMES_ROMAN, 12,
       Font.BOLDITALIC);
         
         //Below specify contents
          String imagePath = "C:\\Users\\Diana Wang\\Documents\\NetBeansProjects\\coral_island_banner_customer.png";
          Image image = Image.getInstance(imagePath);
          document.add(image);
          
          Paragraph preface = new Paragraph();
          addEmptyLine(preface, 1);
          preface.add(new Paragraph("Your e-ticket is displayed as below: ", catFont));
          addEmptyLine(preface, 1);
          
          document.add(preface); 
          //Below add a table
          PdfPTable table = new PdfPTable(2);
         table.setSpacingAfter(30);
         table.setSpacingBefore(30);
         table.setWidths(new int []{1,3});
         
          //Add table header
         PdfPCell c1 = new PdfPCell(new Phrase("Ticketing Info"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
         
         c1 = new PdfPCell(new Phrase("Details & Remarks"));
         c1.setHorizontalAlignment(Element.ALIGN_CENTER);
         table.addCell(c1);
         
         table.setHeaderRows(1);
         
         //Add table contents
         table.addCell("Attraction");
         if(!tpe.getAttrTickets().isEmpty())
             table.addCell(tpe.getAttrTickets().get(0).getAttr().getAttrName());
         else
             table.addCell("Attraction Not Assigned Yet!");
         
         if(!tpe.getAttrTickets().isEmpty())
         {
             Iterator <AttrTicketEntity> itr = tpe.getAttrTickets().iterator();
             Iterator <Integer> itr2 = tpe.getAttrTicketQuantities().iterator();
             int i = 1;
             
             while(itr.hasNext()&&itr2.hasNext())
             {
                 AttrTicketEntity currentTicket = itr.next();
                 Integer currentNumber = itr2.next();
                 table.addCell("No."+i+" Ticket Name & Type");
                 table.addCell(currentTicket.getAttrTicketName()+" "+currentTicket.getAttrTicketType());
                 table.addCell("Number of Tickets");
                 table.addCell(Integer.toString(currentNumber));
                 table.addCell("Ticket Date");
                 table.addCell(tpe.getAttrTicketBookDate().toString());
                 table.addCell("Ticket Price");
                 table.addCell(Double.toString(tpe.getAttrTicketFee()));
                 i++;
                 System.out.println("EmailSessionBean: a ticket has been added!"+i);
             }
             
         }
         
         return OUTPUTFILE;
     }
 
     private String createTicketCombo(AttrComboEntity combo) throws FileNotFoundException, DocumentException {
          //Below generate a PDF file
             Document document;
             document = new Document(PageSize.A4,50,50,50,50);
             String OUTPUTFILE = "C:\\Users\\Diana Wang\\Documents\\Diana\\ComboTicketConfirmation "+
                     combo.getAttrComboName()+" "+combo.getAttrComboId()+".pdf";
                     
             PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(OUTPUTFILE));
             document.open();
             
             //Below specify the font type
         Font catFont = new Font(Font.TIMES_ROMAN, 18,
             Font.BOLD);
         Font redFont = new Font(Font.TIMES_ROMAN, 12,
             Font.NORMAL,Color.RED);
         Font subFont = new Font(Font.TIMES_ROMAN, 16,
             Font.BOLD);
         Font tableFont;
             tableFont = new Font(Font.TIMES_ROMAN,16,Font.BOLD,Color.DARK_GRAY);
         Font smallItalic = new Font(Font.TIMES_ROMAN, 12,
             Font.BOLDITALIC);
         
         
             
     }
 }
