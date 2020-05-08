 package com.practice.others;
 
 /*****************************************************
 * 
* @author wuzhenzhong
* 
* @since May 6, 2009 
 * 
 * ʾʹjavamailhtmlʽʼ
 * 
 * ֱԷʹͲʽʾ
 * 
 * ͬʱʵ˷ʼ
 *****************************************************/
 import java.util.Date;
 import java.util.Properties;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 public class SendMail{
     @SuppressWarnings("static-access")
 public void SendMail2(String title, String attachment){
         // ԣSession 
         Properties props = new Properties();
         Session sendsession;
         Transport transport;
         MimeMessage message = null;
         BodyPart messageBodyPart = new MimeBodyPart();
         Multipart multipart = new MimeMultipart();
         String from = "nicky_qianlei@163.com";
        String to[]={"leqian@ebay.com"};
         String cc[] = {""};
         String bcc[] = {""};
         String content = "<font style=\"BACKGROUND-COLOR: #666699\" color=\"#ff0000\" size=\"5\">Ըʽݲ<a href=\"\">ʽ</a>Ը<em>ʽ</em></font>";
         try{
             sendsession = Session.getInstance(props, null);
             //дSMTPĵַ
             props.put("mail.smtp.host", "smtp.163.com");
             //SMTPҪȨ֤
             props.put("mail.smtp.auth", "true");
             //
             //sendsession.setDebug(true);
             //SessionMessage
             message = new MimeMessage(sendsession);
             //÷˵ַ
             message.setFrom(new InternetAddress(from));
             //˵ַ
             String toList = getMailList(to);
             InternetAddress[] iaToList = new InternetAddress().parse(toList);
             message.setRecipients(Message.RecipientType.TO,iaToList);
             if (cc != null){
                 String ccList = this.getMailList(cc);
                     InternetAddress[] iaCCList = new InternetAddress().parse(ccList);
                 message.setRecipients(Message.RecipientType.CC,iaCCList);
                  }
             if (bcc != null){
                 String bccList = this.getMailList(bcc);
                     InternetAddress[] iaBCCList = new InternetAddress().parse(bccList);
                 message.setRecipients(Message.RecipientType.BCC,iaBCCList);
                  }
             //e-mail 
             message.setSubject(title);
             //e-mailʱ
             message.setSentDate(new Date());
             //e-mail
            message.setText(content);
             //һ֣ı
             messageBodyPart.setContent(content, "text/html;charset=gbk");//BodyPartݺ͸ʽ/뷽ʽ    
             multipart.addBodyPart(messageBodyPart);
             if (!attachment.equals("")){
                 // ڶ֣     
                 messageBodyPart = new MimeBodyPart();
                 // ø
                 DataSource source = new FileDataSource(attachment);
                 //øݴ
                 messageBodyPart.setDataHandler(new DataHandler(source));
                 // øļ
                 messageBodyPart.setFileName(attachment);
                 // ڶ
                 multipart.addBodyPart(messageBodyPart);
             }
             // ಿݷŵe-mail
             message.setContent(multipart);
             //e-mail޸
             message.saveChanges();
             //SessionTransport
             transport = sendsession.getTransport("smtp");
             //ӵSMTP
            transport.connect("smtp.163.com", "nicky_qianlei", "Nickychao2613");
             //e-mail
             transport.sendMessage(message, message.getAllRecipients());
             //رTransport
             transport.close();
         } catch (MessagingException m){
             System.out.println(m.toString());
         } catch (Exception e){
             e.printStackTrace();
         } 
     }
     
     //ȡռ˵ַ
     public String getMailList(String[] mailArray){
          
         StringBuffer toList = new StringBuffer();
     int length = mailArray.length;
         if(mailArray!=null && length <2){
              toList.append(mailArray[0]);
         }else{
              for(int i=0;i<length;i++){
                      toList.append(mailArray[i]);
                      if(i!=(length-1)){
                          toList.append(",");
                      }
              }
          }
      return toList.toString();
     }
     public static void main(String args[]){
      SendMail m = new SendMail();
 //      m.SendMail2("Test", "D:\\tts.txt");
         m.SendMail2("Test", "");
     }
 }
