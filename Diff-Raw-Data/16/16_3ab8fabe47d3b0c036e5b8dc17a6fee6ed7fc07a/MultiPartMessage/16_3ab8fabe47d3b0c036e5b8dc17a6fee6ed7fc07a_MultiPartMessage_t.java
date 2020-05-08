 package net.codjo.test.release.task.mail;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import net.codjo.test.common.fixture.MailMessage;
 import net.codjo.test.release.task.file.FileAssert;
 import net.codjo.util.file.FileUtil;
 
 /**
  *
  */
 public class MultiPartMessage extends Message {
     private List<AssertAttachment> attachments = new ArrayList<AssertAttachment>();
 
 
     public void addAssertAttachment(AssertAttachment assertion) {
         this.attachments.add(assertion);
     }
 
 
     public void assertAttachments(MailMessage mailMessage, AssertInboxTask assertInboxTask, String tmpDirectory) {
         for (AssertAttachment attachment : attachments) {
             FileAssert fileAssert = new FileAssert();
             fileAssert.setRemote(false);
             fileAssert.setProject(assertInboxTask.getProject());
 
             String actual = generateFileFromMultipart(tmpDirectory, attachment.getAttachmentIndex(), mailMessage);
             fileAssert.setActual(actual);
             fileAssert.setExpected(attachment.getExpected());
             fileAssert.setComparisonType(attachment.getComparisonType());
 
             fileAssert.execute();
 
             FileUtil.deleteRecursively(new File(tmpDirectory, actual));
         }
     }
 
 
     private String generateFileFromMultipart(String tmpDirectory, int attachmentIndex, MailMessage mailMessage) {
         File file = mailMessage.getMultipart(attachmentIndex, getTempFilename(tmpDirectory, attachmentIndex));
         return file.getName();
     }
 
 
     private String getTempFilename(String tmpDirectory, int attachmentIndex) {
        return tmpDirectory + File.separator + System.currentTimeMillis() + "multipartFile" + attachmentIndex;
     }
 }
