 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.support;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Pattern;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.openqa.selenium.By;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Ordering;
 import com.google.common.io.Files;
 import com.icegreen.greenmail.util.GreenMail;
 import com.icegreen.greenmail.util.GreenMailUtil;
 import com.icegreen.greenmail.util.ServerSetup;
 
 public class BodWebDriver {
 
   public static final String URL_UNDER_TEST = withoutEndingSlash(
     System.getProperty("selenium.test.url", "http://localhost:8083/bod"));
 
   public static final DateTimeFormatter RESERVATION_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd H:mm");
 
   private static final int MAIL_SMTP_PORT = 4025;
 
   private FirefoxDriver driver;
   private GreenMail mailServer;
 
   private BodUserWebDriver userDriver;
   private BodManagerWebDriver managerDriver;
   private BodNocWebDriver nocDriver;
   private BodAppManagerWebDriver appManagerDriver;
 
   private static String withoutEndingSlash(String path) {
     return path.endsWith("/") ? StringUtils.chop(path) : path;
   }
 
   public synchronized void initializeOnce() {
 
     if (driver == null) {
       this.driver = new FirefoxDriver();
       this.driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
 
       Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
           if (driver != null) {
             driver.quit();
           }
         }
       });
     }
 
     if (mailServer == null) {
       mailServer = new GreenMail(new ServerSetup(MAIL_SMTP_PORT, null, ServerSetup.PROTOCOL_SMTP));
       mailServer.start();
 
       Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
           if (mailServer != null) {
             mailServer.stop();
           }
         }
       });
     }
 
     if (managerDriver == null) {
       managerDriver = new BodManagerWebDriver(driver);
     }
 
     if (nocDriver == null) {
       nocDriver = new BodNocWebDriver(driver);
     }
 
     if (userDriver == null) {
       userDriver = new BodUserWebDriver(driver);
     }
 
     if (appManagerDriver == null) {
       appManagerDriver = new BodAppManagerWebDriver(driver);
     }
   }
 
   public BodManagerWebDriver getManagerDriver() {
     return managerDriver;
   }
 
   public BodAppManagerWebDriver getAppManagerDriver() {
     return appManagerDriver;
   }
 
   public BodNocWebDriver getNocDriver() {
     return nocDriver;
   }
 
   public BodUserWebDriver getUserDriver() {
     return userDriver;
   }
 
   public void takeScreenshot(File screenshot) throws Exception {
     if (driver != null) {
       File temp = driver.getScreenshotAs(OutputType.FILE);
       Files.copy(temp, screenshot);
     }
   }
 
   private MimeMessage getLastEmail() {
     List<MimeMessage> mails = getMailsSortedByDate();
 
     return Iterables.getLast(mails);
   }
 
   private List<MimeMessage> getMailsSortedByDate() {
     Ordering<MimeMessage> mailMessageOrdering = new Ordering<MimeMessage>() {
 
       @Override
       public int compare(MimeMessage left, MimeMessage right) {
         return getDateTime(left).compareTo(getDateTime(right));
       }
 
       private DateTime getDateTime(MimeMessage message) {
        final DateTimeFormatter dateParser = DateTimeFormat.forPattern("EEE, d MMM yyyy HH:mm:ss Z '(CET)'")
            .withLocale(Locale.ENGLISH);
         try {
          return dateParser.parseDateTime(message.getHeader("Date")[0]);
         }
         catch (MessagingException e) {
           throw new RuntimeException(e);
         }
       }
     };
 
     return mailMessageOrdering.sortedCopy(Arrays.asList(mailServer.getReceivedMessages()));
   }
 
   private MimeMessage getBeforeLastEmail() {
     List<MimeMessage> mails = getMailsSortedByDate();
     return mails.get(mails.size() - 2);
   }
 
   public void verifyLastEmailRecipient(String to) {
     MimeMessage lastMail = getLastEmail();
 
     assertThat(GreenMailUtil.getHeaders(lastMail), containsString("To: " + to));
   }
 
   public void verifyLastEmailSubjectContains(String subject) {
     MimeMessage lastEmail = getLastEmail();
 
     try {
       assertThat(lastEmail.getSubject(), containsString(subject));
     }
     catch (MessagingException e) {
       fail(e.getMessage());
     }
   }
 
   private String extractLink(String message) {
     Pattern pattern = Pattern.compile(".*(https?://[\\w:/\\-\\.\\?&=]+).*", Pattern.DOTALL);
 
     java.util.regex.Matcher matcher = pattern.matcher(message);
 
     if (matcher.matches()) {
       return matcher.group(1);
     }
 
     throw new AssertionError("Could not find link in message: " + message);
   }
 
   public void clickLinkInLastEmail() {
     clickLinkInMail(getLastEmail());
   }
 
   public void clickLinkInBeforeLastEmail() {
     clickLinkInMail(getBeforeLastEmail());
   }
 
   private void clickLinkInMail(MimeMessage email) {
     String body = GreenMailUtil.getBody(email);
     driver.get(extractLink(body));
   }
 
   public void verifyPageHasModalHeader(String text) {
     WebElement modalHeader = driver.findElement(By.className("modal-header"));
 
     assertThat(modalHeader.getText(), containsString(text));
   }
 
   public void verifyPageHasModalMessage(String text) {
     WebElement modal = driver.findElement(By.className("modal-body"));
 
     assertThat(modal.getText(), containsString(text));
   }
 
 }
