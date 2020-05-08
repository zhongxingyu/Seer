 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.support;
 
 import static junit.framework.Assert.fail;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Pattern;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.openqa.selenium.By;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Ordering;
 import com.google.common.io.Files;
 import com.icegreen.greenmail.util.GreenMail;
 import com.icegreen.greenmail.util.GreenMailUtil;
 import com.icegreen.greenmail.util.ServerSetup;
 
 public class BodWebDriver {
 
   public static final String URL_UNDER_TEST = withEndingSlash(System.getProperty("selenium.test.url",
       "http://localhost:8083/bod"));
 
   public static final String DB_URL = "jdbc.jdbcUrl";
   public static final String DB_USER = "jdbc.user";
   public static final String DB_PASS = "jdbc.password";
   public static final String DB_DRIVER_CLASS = "jdbc.driverClass";
 
   public static final DateTimeFormatter RESERVATION_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd H:mm");
 
   private static final int MAIL_SMTP_PORT = 4025;
  private static final InputStream PROP_STREAM = BodWebDriver.class.getResourceAsStream("/bod-default.properties");
 
   private final Logger log = LoggerFactory.getLogger(getClass());
 
   private FirefoxDriver driver;
   private GreenMail mailServer;
 
   private BodUserWebDriver userDriver;
   private BodManagerWebDriver managerDriver;
   private BodNocWebDriver nocDriver;
 
   private Properties props;
 
   private static String withEndingSlash(String path) {
     return path.endsWith("/") ? path : path + "/";
   }
 
   public synchronized void initializeOnce() {
 
     clearDatabase();
 
     if (driver == null) {
       this.driver = new FirefoxDriver();
       this.driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
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
 
     managerDriver = new BodManagerWebDriver(driver);
     nocDriver = new BodNocWebDriver(driver);
     userDriver = new BodUserWebDriver(driver);
   }
 
   private void clearDatabase() {
 
     Connection connection = createDbConnection(getProperty(DB_URL), getProperty(DB_USER), getProperty(DB_PASS),
         getProperty(DB_DRIVER_CLASS));
 
     Statement statement = null;
     try {
       statement = connection.createStatement();
       statement.executeUpdate("truncate physical_resource_group CASCADE;");
 
       statement = connection.createStatement();
       statement.executeUpdate("truncate virtual_resource_group CASCADE;");
     }
     catch (SQLException e) {
       throw new RuntimeException(e);
     }
     finally {
       try {
         if (statement != null) {
           statement.close();
         }
 
         if (connection != null) {
           connection.close();
         }
       }
       catch (SQLException e) {
         throw new RuntimeException(e);
       }
     }
 
     log.info("Database cleared");
 
   }
 
   private String getProperty(String key) {
 
     if (props == null) {
       props = new Properties();
       try {
        props.load(PROP_STREAM);
       }
       catch (IOException e) {
         throw new RuntimeException(e);
       }
     }
     return props.getProperty(key);
   }
 
   public BodManagerWebDriver getManagerDriver() {
     return managerDriver;
   }
 
   public BodNocWebDriver getNocDriver() {
     return nocDriver;
   }
 
   public BodUserWebDriver getUserDriver() {
     return userDriver;
   }
 
   private Connection createDbConnection(String dbUrl, String dbUser, String dbPass, String dbDriverClass) {
 
     Connection dbConnection = null;
 
     try {
       Class.forName(dbDriverClass).newInstance();
     }
     catch (InstantiationException e) {
       throw new RuntimeException(e);
     }
     catch (IllegalAccessException e) {
       throw new RuntimeException(e);
     }
     catch (ClassNotFoundException e) {
       throw new RuntimeException(e);
     }
 
     try {
       dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
     }
     catch (SQLException e) {
       throw new RuntimeException(e);
     }
 
     return dbConnection;
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
       private DateTimeFormatter dateParser = DateTimeFormat.forPattern("EEE, d MMM yyyy HH:mm:ss Z '(CEST)'")
           .withLocale(Locale.ENGLISH);
 
       @Override
       public int compare(MimeMessage left, MimeMessage right) {
         return getDateTime(left).compareTo(getDateTime(right));
       }
 
       private DateTime getDateTime(MimeMessage message) {
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
 
   public void verifyPageHasMessage(String text) {
     WebElement modal = driver.findElement(By.className("modal-body"));
 
     assertThat(modal.getText(), containsString(text));
   }
 
 }
