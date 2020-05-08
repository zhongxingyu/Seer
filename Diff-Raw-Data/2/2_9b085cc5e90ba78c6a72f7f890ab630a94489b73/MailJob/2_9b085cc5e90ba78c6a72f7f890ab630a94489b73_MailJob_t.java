 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package testing;
 
 import java.util.Properties;
 import javax.mail.Message;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import org.quartz.Job;
 import org.quartz.JobDetail;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 /**
  *
  * @author Quality of Service
  */
 public class MailJob implements Job {
 
     @Override
     public void execute(JobExecutionContext jec) throws JobExecutionException {
         try {
 
             JobDetail detalle = jec.getJobDetail();
             String nombre = (String) detalle.getJobDataMap().get("nombre_persona");
             // Propiedades de la conexión
             System.out.println("-------------Inicializando Propiedades del Mail---------");
             Properties props = new Properties();
             props.setProperty("mail.smtp.host", "smtp.gmail.com");
             props.setProperty("mail.smtp.starttls.enable", "true");
             props.setProperty("mail.smtp.port", "587");
             props.setProperty("mail.smtp.user", "brueradamian@gmail.com");
             props.setProperty("mail.smtp.auth", "true");
 
             // Preparamos la sesion
             System.out.println("-----------Inicializamos la Sesion-----------");
             Session session = Session.getDefaultInstance(props);
 
             // Construimos el mensaje
             System.out.println("---------Construimos el mensaje--------");
             MimeMessage message = new MimeMessage(session);
             message.setFrom(new InternetAddress("bruera@noreply.com"));
             message.addRecipient(
                     Message.RecipientType.TO,
                     new InternetAddress("brueradamian@gmail.com"));
             message.setSubject("System Configuration");
             message.setText(
                     "Otra prueba de que esto funciona + " + nombre);
 
             // Lo enviamos.
             System.out.println("-------Enviamos el mansaje al servidor smtp--------");
             Transport t = session.getTransport("smtp");
            t.connect("brueradamian@gmail.com", "null");
             t.sendMessage(message, message.getAllRecipients());
 
             // Cierre.
             System.out.println("-----------Cerramos la coneccion, mensaje a fue enviado--------");
             t.close();
         } catch (Exception e) {
             System.out.println("-------Exception la puta madre!!!!-------");
             e.printStackTrace();
         }
     }
 }
