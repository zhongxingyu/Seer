 package hudson.model;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.NullSCM;
 import hudson.scm.SubversionChangeLogParser;
 import hudson.tasks.ArtifactArchiver;
 import hudson.tasks.Builder;
 import hudson.tasks.MailSender;
 import hudson.tasks.Mailer;
 import hudson.tasks.Shell;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.logging.Level;
 
 import javax.mail.Address;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.tools.ant.types.selectors.SelectorUtils;
 
 /**
  * Setup a free-style project and uses the Shell builder to exercise both SUCCESS and FAILURE, and check that the proper
  * recipients are notified. A custom NullSCM is used in this example putting fake changelogs upon builds.
  * 
  * TODO test with changelogs that contain multiple authors, for consistency
  */
 public class FreeStyleProjectTest extends HudsonTestCase {
     MimeMessage mail;
 
     File nextChangeLog;
 
     public void testCreateProject() throws Exception {
         FreeStyleProject project = new FreeStyleProject(Hudson.getInstance(), "test");
         project.setScm(new NullSCM() {
             @Override
             /**
              * Put a fake changelog
              */
             public boolean checkout(AbstractBuild build, Launcher launcher, FilePath remoteDir, BuildListener listener,
                     File changeLogFile) throws IOException {
                 try {
                     if (nextChangeLog != null)
                         FileUtils.copyFile(nextChangeLog, changeLogFile);
                     else
                         createEmptyChangeLog(changeLogFile, listener, "log");
                 } catch (IOException e) {
                     e.printStackTrace();
                     // FIXME I wonder why Hudson does not propagate exceptions
                     // here, what's the point of returning a boolean?
                     return false;
                 }
                 return true;
             }
 
             public ChangeLogParser createChangeLogParser() {
                 return new SubversionChangeLogParser();
             }
         });
         // FIXME add a factory method to create the MailSender to avoid big
         // copy/paste of _perform()
         Mailer mailer = new Mailer() {
             public <P extends Project<P, B>, B extends Build<P, B>> boolean _perform(B build, Launcher launcher,
                     BuildListener listener) throws InterruptedException {
                 if (debug)
                     listener.getLogger().println("Running mailer");
                 return new MailSender<P, B>(recipients, dontNotifyEveryUnstableBuild, sendToIndividuals) {
                     /** Check whether a path (/-separated) will be archived. */
                     @Override
                     public boolean artifactMatches(String path, B build) {
                         ArtifactArchiver aa = (ArtifactArchiver) build.getProject().getPublishers().get(
                                 ArtifactArchiver.DESCRIPTOR);
                         if (aa == null) {
                             LOGGER.finer("No ArtifactArchiver found");
                             return false;
                         }
                         String artifacts = aa.getArtifacts();
                         for (String include : artifacts.split("[, ]+")) {
                             String pattern = include.replace(File.separatorChar, '/');
                             if (pattern.endsWith("/")) {
                                 pattern += "**";
                             }
                             if (SelectorUtils.matchPath(pattern, path)) {
                                 LOGGER.log(Level.FINER, "DescriptorImpl.artifactMatches true for {0} against {1}",
                                         new Object[] { path, pattern });
                                 return true;
                             }
                         }
                         LOGGER.log(Level.FINER, "DescriptorImpl.artifactMatches for {0} matched none of {1}",
                                 new Object[] { path, artifacts });
                         return false;
                     }
 
                     /**
                      * Override parent method to avoid sending the email for real, and to hold a reference to last email
                      */
                     public boolean execute(B build, BuildListener listener) throws InterruptedException {
                         try {
                             mail = getMail(build, listener);
                             if (mail != null) {
                                 Address[] allRecipients = mail.getAllRecipients();
                                 if (allRecipients != null) {
                                     StringBuffer buf = new StringBuffer("Sending e-mails to:");
                                     for (Address a : allRecipients)
                                         buf.append(' ').append(a);
                                     listener.getLogger().println(buf);
                                 } else {
                                     listener.getLogger().println(
                                             "An attempt to send an e-mail" + " to empty list of recipients, ignored.");
                                 }
                             }
                         } catch (MessagingException e) {
                             e.printStackTrace(listener.error(e.getMessage()));
                         }
 
                         return true;
                     }
                 }.execute(build, listener);
             }
         };
         mailer.recipients = "";
         mailer.sendToIndividuals = true;
         project.addPublisher(mailer);
 
         Result result;
 
         setCommand(project, "echo Hello World");
         nextChangeLog = null;
         result = build(project);
         assertSuccess(result);
         assertNull("Not expecting an email", mail);
 
         setCommand(project, "nonexistentcommand1");
         nextChangeLog = new File("src/test/java/hudson/model/changelog-user1.xml");
         result = build(project);
         assertFailure(result);
         assertTrue("number of recipients not 1", mail.getAllRecipients().length == 1);
         assertEquals(mail.getAllRecipients()[0], new InternetAddress("user1@company.com"));
 
         setCommand(project, "nonexistentcommand2");
         nextChangeLog = new File("src/test/java/hudson/model/changelog-user2.xml");
         result = build(project);
         assertFailure(result);
         assertTrue("number of recipients not 2", mail.getAllRecipients().length == 2);
        assertEquals(mail.getAllRecipients()[0], new InternetAddress("user1@company.com"));
        assertEquals(mail.getAllRecipients()[1], new InternetAddress("user2@company.com"));
 
         setCommand(project, "echo Back to normal");
         nextChangeLog = new File("src/test/java/hudson/model/changelog-user2.xml");
         result = build(project);
         assertSuccess(result);
         assertTrue("number of recipients not 2", mail.getAllRecipients().length == 2);
        assertEquals(mail.getAllRecipients()[0], new InternetAddress("user1@company.com"));
        assertEquals(mail.getAllRecipients()[1], new InternetAddress("user2@company.com"));
 
         setCommand(project, "new failure");
         nextChangeLog = new File("src/test/java/hudson/model/changelog-user1.xml");
         result = build(project);
         assertFailure(result);
         assertTrue("number of recipients not 1", mail.getAllRecipients().length == 1);
         assertEquals(mail.getAllRecipients()[0], new InternetAddress("user1@company.com"));
     }
 }
