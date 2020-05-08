 package net.codjo.test.common.fixture;
 import com.dumbster.smtp.SmtpMessage;
 import java.util.Date;
 import java.util.List;
 import javax.mail.Message;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 /**
  *
  */
 public class MailFixtureTest extends TestCase {
    private static final int MY_MAIL_PORT = 9999;
     private MailFixture mailFixture = new MailFixture(MY_MAIL_PORT);
 
 
     public void test_getReceivedMessages() throws Exception {
         sendMail("darth.Vader", "luke.Skywalker", "scoop", "Je suis ton pere");
 
         List<MailMessage> list = mailFixture.getReceivedMessages();
         assertEquals(1, list.size());
         SmtpMessage message = list.get(0).getSmtpMessage();
         assertEquals("darth.Vader", message.getHeaderValue("From"));
         assertEquals("luke.Skywalker", message.getHeaderValue("To"));
         assertEquals("scoop", message.getHeaderValue("Subject"));
         assertEquals("Je suis ton pere", message.getBody());
     }
 
 
     public void test_assertReceivedMessagesCount() throws Exception {
         sendMail("darth.Vader", "luke.Skywalker", "scoop", "Je suis ton pere");
 
         mailFixture.assertReceivedMessagesCount(1);
 
         try {
             mailFixture.assertReceivedMessagesCount(2);
             fail("should fail");
         }
         catch (AssertionFailedError error) {
             assertEquals("Received message count expected:<2> but was:<1>", error.getMessage());
         }
     }
 
 
     public void test_assertReceivedMessage() throws Exception {
         sendMail("darth.Vader", "luke.Skywalker", "rvlation", "Je suis ton pre");
 
         assertNotNull(mailFixture.getReceivedMessage(0).getSmtpMessage());
 
         mailFixture.getReceivedMessage(0).assertThat()
               .from("darth.Vader")
               .to("luke.Skywalker")
               .subject("rvlation")
               .bodyContains("pre");
 
         assertEquals("rvlation", mailFixture.getReceivedMessage(0).getSubject());
     }
 
 
     public void test_assertReceivedMessage_withMultipleSender() throws Exception {
         sendMail("darth.Vader", new String[]{"luke", "chewbacca"}, "n/a", "n/a");
 
         mailFixture.getReceivedMessage(0).assertThat()
               .to("luke", "chewbacca");
 
         try {
             mailFixture.getReceivedMessage(0).assertThat()
                   .to("mika");
             fail("should fail");
         }
         catch (AssertionFailedError error) {
             assertEquals("Assert To - The value 'mika' is not in [luke, chewbacca]", error.getMessage());
         }
     }
 
 
     public void test_asserBadReceivedMessage() throws Exception {
         sendMail("darth.Vader", "luke.Skywalker", "scoop", "Je suis ton pere");
 
         try {
             mailFixture.getReceivedMessage(0).assertThat().from("yoda");
             fail();
         }
         catch (AssertionFailedError ex) {
             assertEquals("Assert From -  expected:<[yoda]> but was:<[darth.Vader]>", ex.getMessage());
         }
         try {
             mailFixture.getReceivedMessage(0).assertThat().to("yoda");
             fail();
         }
         catch (AssertionFailedError ex) {
             assertEquals("Assert To -  expected:<[yoda]> but was:<[luke.Skywalker]>", ex.getMessage());
         }
         try {
             mailFixture.getReceivedMessage(0).assertThat().subject("Hello Luke");
             fail();
         }
         catch (AssertionFailedError ex) {
             assertEquals("Assert Subject -  expected:<[Hello Luke]> but was:<[scoop]>", ex.getMessage());
         }
         try {
             mailFixture.getReceivedMessage(0).assertThat().bodyContains("Je suis ta mere");
             fail();
         }
         catch (AssertionFailedError ex) {
             assertEquals("Assert Body contains -"
                          + "  expected contains:<Je suis ta mere> but content was:<Je suis ton pere>",
                          ex.getMessage());
         }
     }
 
 
     public void test_badTearDownCall() throws Exception {
         mailFixture.doTearDown();
         mailFixture = new MailFixture(MY_MAIL_PORT);
         try {
             mailFixture.doTearDown();
         }
         catch (Exception ex) {
             assertEquals("Mail server has not started yet, doSetup() method should be called before",
                          ex.getMessage());
         }
     }
 
 
     public void test_invokePublicMethodsBeforeDoSetUp() throws Exception {
         mailFixture.doTearDown();
         mailFixture = new MailFixture(MY_MAIL_PORT);
         try {
             mailFixture.getReceivedMessage(0);
         }
         catch (Exception ex) {
             assertEquals(MailFixture.FIXTURE_NOT_STARTED_MESSAGE, ex.getMessage());
         }
 
         try {
             mailFixture.getReceivedMessages();
         }
         catch (Exception ex) {
             assertEquals(MailFixture.FIXTURE_NOT_STARTED_MESSAGE, ex.getMessage());
         }
     }
 
 
     @Override
     protected void setUp() throws Exception {
         mailFixture.doSetUp();
     }
 
 
     @Override
     protected void tearDown() throws Exception {
         mailFixture.doTearDown();
     }
 
 
     private void sendMail(String from, String to, String subject, String body) throws Exception {
         sendMail(from, new String[]{to}, subject, body);
     }
 
 
     private void sendMail(String from, String[] to, String subject, String body) throws Exception {
         Session session = Session.getInstance(System.getProperties());
         session.setDebug(true);
 
         MimeMessage msg = new MimeMessage(session);
         msg.setFrom(new InternetAddress(from));
 
         for (String aTo : to) {
             msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(aTo, false));
         }
 
         msg.setSubject(subject);
        msg.setContent(body, "text/html; charset=" + java.nio.charset.Charset.defaultCharset().name());
 
         msg.setHeader("X-Mailer", "iComp");
         msg.setSentDate(new Date());
 
         Transport.send(msg);
     }
 }
