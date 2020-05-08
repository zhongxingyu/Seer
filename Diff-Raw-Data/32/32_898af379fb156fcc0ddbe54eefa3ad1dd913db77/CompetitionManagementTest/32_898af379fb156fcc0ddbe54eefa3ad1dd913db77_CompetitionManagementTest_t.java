 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.fofo.services.management;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Properties;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import org.junit.Test;
 import org.junit.Before;
 import static org.junit.Assert.*;
 
 import org.fofo.entity.*;
 import org.fofo.dao.CompetitionDAO;
 import org.fofo.services.*;
 
 import org.jmock.States;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import  org.junit.runner.RunWith; 
 import org.jmock.Sequence;
 import org.joda.time.DateTime;
 
 
 /**
  *
  * @author josepma
  */
 @RunWith(JMock.class)
 public class CompetitionManagementTest {
 
     ManagementService service;
     
     Mockery context = new JUnit4Mockery();
     
     CompetitionDAO cdao;
     Competition comp;
     DateTime time;
     
     public CompetitionManagementTest() {
     }
 
     //Proves unitaries per fer TDD sobre ManagementService.addCompetition(...)  
     @Before
     public void setup(){
     
       service = new ManagementService();  
       cdao  = context.mock(CompetitionDAO.class);
 
       comp = Competition.create(Type.LEAGUE);
         
       comp.setCategory(Category.MALE);
       comp.setMinTeams(2);
       comp.setMaxTeams(20);
       DateTime date = new DateTime();
       date.plusWeeks(3);
       comp.setInici(date.toDate());
         //...
 
       time = new DateTime();
         
     }
     
     /*
      * 
      * A category which is not either MALE, FEMALE, VETERAN throws an exception
      * of type ***********
      * 
      */
     
     @Test(expected=IncorrectCompetitionData.class)
     public void testIncorrectCategory() throws Exception {
        Competition comp2 = Competition.create(Type.CUP);
        
        comp2.setMaxTeams(12);
        //...
        comp2.setCategory(null);
        //TO BE COMPLETED....
        service.addCompetition(comp2);
        
        
     }
     
     @Test(expected=IncorrectTypeData.class)
     public void testIncorrectType() throws Exception{
         Competition comp2 = Competition.create(Type.CUP);
        comp2.setCategory(Category.MALE);
         comp2.setType(null);
         service.addCompetition(comp2);
     }
     
     @Test(expected=IncorrectMinNumberOfTeams.class)
     public void testIncorrectMinTeams() throws Exception{
         Competition comp2 = Competition.create(Type.LEAGUE);
         comp2.setMinTeams(1);
         service.addCompetition(comp2);
     }
     
     @Test(expected=IncorrectMaxNumberOfTeams.class)
     public void testIncorrectMaxTeams() throws Exception{
         Competition comp2 = Competition.create(Type.LEAGUE);
         comp2.setMaxTeams(21);
         service.addCompetition(comp2);
     }
     
     @Test(expected=IncorrectMinNumberOfTeams.class)
     public void testMinTeamsNotEven() throws Exception{
         Competition comp2 = Competition.create(Type.LEAGUE);
         comp2.setMinTeams(3);
         service.addCompetition(comp2);
     }
     
     @Test(expected=IncorrectMaxNumberOfTeams.class)
     public void testMaxTeamsNotEven() throws Exception{
         Competition comp2 = Competition.create(Type.LEAGUE);
         comp2.setMaxTeams(17);
         service.addCompetition(comp2);
     }
     
     @Test(expected=IncorrectMaxNumberOfTeams.class)
     public void testIncorrectMaxTeamsInCup() throws Exception{
         Competition comp2 = Competition.create(Type.CUP);
         comp2.setMaxTeams(65);
         service.addCompetition(comp2);
     }
     
     @Test(expected=IncorrectMinNumberOfTeams.class)
     public void testMinTeamsNotPowerOfTwo() throws Exception{
         Competition comp2 = Competition.create(Type.CUP);
         comp2.setMinTeams(6);
         service.addCompetition(comp2);
     }
     
     @Test(expected=IncorrectMaxNumberOfTeams.class)
     public void testMaxTeamsNotPowerOfTwo() throws Exception{
         Competition comp2 = Competition.create(Type.CUP);
         comp2.setMaxTeams(18);
         service.addCompetition(comp2);
     }
     
     @Test(expected=IncorrectDate.class)
     public void testIncorrectDate() throws Exception{
         Competition comp2 = Competition.create(Type.CUP);
         DateTime date = new DateTime();
         date.plusWeeks(1);
         comp2.setInici(date.toDate());
         service.addCompetition(comp2);
     }
     
     @Test
     public void comprobar_dia_actual() {
         assertEquals(6,time.getDayOfMonth());
     }
     
     @Test
     public void comprobar_suma(){   
        assertEquals(7, time.plusDays(1).getDayOfMonth()); 
     }
     
     /***
      * 
      * ADD MORE TESTS TO CHECK ALL THE ASPECTS CONSIDERED IN THE ACCEPTANCE TESTS
      * 
      */ 
     
     
     
     /*
      * 
      * A competition should be inserted into the database... Therefore,
      * check that, after ManagementService.addCompetition(comp) 
      * the competition comp has been actually inserted into the database
      * 
      */
     
     //@Test
     public void competitionInsertedIntoTheDB() throws Exception {
       
         
         
         context.checking(new Expectations() {{
             oneOf (cdao).addCompetition(comp);
             
         }});
 
         service.addCompetition(comp);      
         
     }
     
      @Test(expected=MessagingException.class)
     public void envio_incorrecto() throws MessagingException{
         
          String servidorSMTP = "smtp.gmail.com";
         String puerto = "587";
         String usuario = "alejandrodelgadorios@gmail.com";
         String password = "pass";
 
         String destino = "alejandrodelgadorios@gmail.com";
         String asunto = "Prueba!";
         String mensaje = "Este es un mensaje de prueba.";
 
         Properties props = new Properties();
 
         props.put("mail.debug", "true");
         props.put("mail.smtp.auth", true);
         props.put("mail.smtp.starttls.enable", true);
         props.put("mail.smtp.host", servidorSMTP);
         props.put("mail.smtp.port", puerto);
 
         Session session = Session.getInstance(props, null);
 
         
             MimeMessage message = new MimeMessage(session);
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                     destino));
             message.setSubject(asunto);
             message.setSentDate(new Date());
             message.setText(mensaje);
 
             Transport tr = session.getTransport("smtp");
             tr.connect(servidorSMTP, usuario, password);
             message.saveChanges();
             tr.sendMessage(message, message.getAllRecipients());
             tr.close();
    
 }
     
 }
