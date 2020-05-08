 
 package pt.uac.cafeteria.model;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import pt.uac.cafeteria.model.domain.*;
 import pt.uac.cafeteria.model.persistence.*;
 
 /**
  * Application gateway.
  * <p>
  * Provides application initialization and gateway methods as a convenience
  * for units of work.
  */
 public class Application {
 
     /** Default administrator username */
     private static final String DEFAULT_ADMIN_USERNAME = "superadmin";
 
     /** Default administrator password */
     private static final String DEFAULT_ADMIN_PASSWORD = "12345678";
 
     /** Global configuration object instance. */
     private static Config config = Config.getInstance();
 
     /** Reusable database connection. */
     private static Connection db;
 
     /** Email session for sending emails. */
     private static Session mail_session;
 
     /** Application initialization. */
     public static void init() {
         initDBConnection();
         checkDefaultAdminAccount();
         MapperRegistry.account().loadAll();
     }
 
     /** Application finalization. Must be run on exit. */
     public static void close() {
         MapperRegistry.account().save();
     }
 
     /**
      * Creates a new database connection using credentials stored in Config.
      *
      * @return a database connection.
      * @throws ApplicationException if unable to create connection, or
      * db driver can't be loaded.
      */
     private static Connection initDBConnection() {
         String dbname = config.get(Config.DB_NAME);
         String dbuser = config.get(Config.DB_USER);
         String dbpass = config.get(Config.DB_PASS);
         String url = "jdbc:mysql://localhost/" + dbname;
         try {
             Class.forName("com.mysql.jdbc.Driver");
             return DriverManager.getConnection(url, dbuser, dbpass);
         } catch (ClassNotFoundException e) {
             throw new ApplicationException("Falha ao carregar driver mysql.", e);
         } catch (SQLException e) {
             throw new ApplicationException("Problema em ligar à base de dados.", e);
         }
     }
 
     /**
      * Gets a reference to reusable database connection, using credentials
      * stored on Config object.
      *
      * @return database connection.
      */
     public static Connection getDBConnection() {
         if (db == null) {
             db = initDBConnection();
         }
         return db;
     }
 
     /**
      * Returns a default SMTP email session for sending emails.
      * <p>
      * Settings can be overriden from Config.
      */
     public static Session getMailSession() {
         if (mail_session == null) {
             final String username = config.get(Config.MAIL_USER);
             final String password = config.get(Config.MAIL_PASS);
 
             java.util.Properties props = config.getSection("mail.smtp");
 
             mail_session = Session.getInstance(props,
                 new javax.mail.Authenticator() {
                     @Override
                     protected PasswordAuthentication getPasswordAuthentication() {
                         return new PasswordAuthentication(username, password);
                     }
                 }
             );
         }
         return mail_session;
     }
 
     /**
      * Sends a simple plain text mail to a single recipient.
      * <p>
      * If there is a problem sending the email, its contents will be sent
      * to the output console, along with more detailed debugging information.
      *
      * @param to the recipent's email address to send the email to.
      * @param subject the email subject.
      * @param body the body of the email message.
      * @throws ApplicationException if email could not be sent.
      */
     public static void sendMail(String to, String subject, String body) {
         String from = config.get(Config.MAIL_USER);
         try {
             // Instantiate a message
             Message msg = new MimeMessage(getMailSession());
 
             //Set message attributes
             msg.setFrom(new InternetAddress(from));
             InternetAddress[] address = {new InternetAddress(to)};
             msg.setRecipients(Message.RecipientType.TO, address);
             msg.setSubject(subject);
             msg.setSentDate(new java.util.Date());
 
             // Set message content
             msg.setText(body);
 
             //Send the message
             Transport.send(msg);
         }
         catch (MessagingException e) {
             System.err.println("--- Email não enviado ---");
             System.out.println("De: " + from);
             System.out.println("Para: " + to);
             System.out.println("Assunto: " + subject);
             System.out.println(body);
             System.out.println();
             System.err.flush();
 
             ApplicationException.log(e);
 
             throw new ApplicationException(
                 "Impossível enviar email. Conteúdo foi enviado para a "
                     + "consola, com relatório do erro."
             );
         }
     }
 
     /**
      * Checks if default administrator account exists,
      * and creates it if not found.
      */
     private static void checkDefaultAdminAccount() {
         AdministratorMapper adminMapper = MapperRegistry.administrator();
         if (adminMapper.findByUsername(DEFAULT_ADMIN_USERNAME) == null) {
             Administrator defaultAdmin = new Administrator(
                 "Administrador", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD
             );
             adminMapper.insert(defaultAdmin);
         }
     }
 
     /**
      * Authenticates an Administrator, against a password.
      *
      * @param username the Administrator username.
      * @param password the Administrator password.
      * @return Administrator object, or null if invalid.
      */
     public static Administrator authenticateAdmin(String username, String password) {
         Administrator admin = MapperRegistry.administrator().findByUsername(username);
 
         if (admin != null && admin.authenticate(password)) {
             return admin;
         }
 
         return null;
     }
 
     /**
      * Creates a new Student and Account.
      * <p>
      * The account seed in config is auto-incremented.
      *
      * @param student the Student object.
      * @return The new student's id number.
      */
     public static Integer createStudent(Student student) {
         Integer id = student.getId();
         if (id == null) {
             id = getNextStudentId();
             student.setId(id);
         }
         if (MapperRegistry.student().insert(student) != null) {
             incrementStudentId();
             return id;
         }
         return null;
     }
 
     /** Gets next student id number to be used. */
     public static Integer getNextStudentId() {
         String id = config.get(Config.YEAR) + config.get(Config.ACCOUNT_SEED);
         return Integer.valueOf(id);
     }
 
     /** Increments current account seed for student id number generation. */
     public static void incrementStudentId() {
         int currentSeed = config.getInt(Config.ACCOUNT_SEED);
         config.setInt(Config.ACCOUNT_SEED, ++currentSeed);
     }
 
     /**
      * Closes a student account.
      * <p>
      * The student is moved to a different list of old students, and the
      * account is closed.
      *
      * @param student the student to close the account.
      */
     public static void closeStudentAccount(Student student) {
         // move student from active list to historic list
         MapperRegistry.oldStudent().insert(student);
         MapperRegistry.student().delete(student);
 
         // close account
         student.getAccount().close();
         MapperRegistry.account().update(student.getAccount());
     }
 
     /**
      * Authenticates a Student against a pin code.
      *
      * @param accountNumber the Account process number or student id.
      * @param pinCode the Account pin code.
      * @return Student account object, or null if does not authenticate.
      * @throws IllegalStateException if the account gets blocked after a certain
      * amount of successive failed login attempts.
      */
     public static Student authenticateStudent(int accountNumber, int pinCode) {
         Account account = MapperRegistry.account().find(accountNumber);
 
         if (account != null) {
             if (account.authenticate(pinCode)) {
                 return MapperRegistry.student().find(accountNumber);
             }
             if (account.isBlocked()) {
                 throw new IllegalStateException("Conta bloqueada!");
             }
         }
 
         return null;
     }
 
     /**
      * Calculates the price of a meal for a student.
      *
      * @param meal the meal being bought.
      * @param student the student buying.
      * @return the cost of the meal for the student.
      */
     public static double mealPrice(Meal meal, Student student) {
         double price = config.getDouble(Config.MEAL_PRICE);
 
         if (meal.getDay().isToday()) {
             price += config.getDouble(Config.SAME_DAY_TAX);
         }
 
         if (student.hasScholarship()) {
             price -= config.getDouble(Config.SCHOLARSHIP_DISCOUNT);
         }
 
         return price;
     }
 }
