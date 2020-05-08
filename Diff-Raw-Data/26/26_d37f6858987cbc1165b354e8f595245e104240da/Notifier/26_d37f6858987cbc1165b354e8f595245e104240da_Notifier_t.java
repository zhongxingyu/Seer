 package notifiers;
 
 import ext.Ext;
 import models.conge.HolidayRequest;
 import models.ins.Contract;
 import models.ma.BS;
 import models.ma.Evaluation;
 import models.ma.Stay;
 import models.main.Config;
 import models.main.Person;
 import models.main.PersonEvaluation;
 import models.main.PersonStatus;
 import models.po.Info;
 import models.sa.Shop;
 import models.security.User;
 import play.i18n.Messages;
 import play.mvc.Mailer;
 import utils.Dates;
 
 import javax.mail.internet.InternetAddress;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 public class Notifier extends Mailer {
 
     public static boolean welcome(User user) throws Exception {
         setFrom(new InternetAddress("ricardonascimento@petitsriens.be", "Administrateur"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Aide"));
         setSubject("Bienvenue %s", user.username);
         addRecipient(user.email, new InternetAddress("ricardonascimento@petitsriens.local", "Nouveau utilisateur"));
         return sendAndWait(user);
     }
 
    public static boolean sendPasswordReset(User user) throws Exception {
         setFrom(new InternetAddress("ricardonascimento@petitsriens.local", "Administrator"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.local", "Help"));
         setSubject(Messages.get("info.lostPassword"));
         addRecipient(user.email, new InternetAddress("ricardonascimento@petitsriens.local", "New users notice"));
        setContentType("text/html");

         return sendAndWait(user);
     }
 
     public static boolean alertEncode(User user, List<Shop> shops) throws Exception {
         setFrom(new InternetAddress("admin@petitsriens.local", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.local", "Help"));
         setSubject(Messages.get("mail.alertEncode"));
         addRecipient(user.email);
         setContentType("text/html");
         return sendAndWait(user, shops);
     }
 
     public static boolean sendEvaluationToReferents(List<Person> referents,
                                                     PersonEvaluation evaluation) throws Exception {
         if (referents.isEmpty()) {
             return false;
         }
 
         setFrom(new InternetAddress("admin@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject(Messages.get("evaluationOf") + " "
                 + Ext.capFirst(evaluation.person.name) + " "
                 + Ext.capFirst(evaluation.person.firstname));
 
         for (Person person : referents) {
             person = Person.findById(person.id);
             addRecipient(person.user.email);
 
         }
 
         setContentType("text/html");
         return sendAndWait(evaluation);
     }
 
     public static boolean sendBirthdayList() throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject(Messages.get("birthDay"));
 
         Config config = Config.birthdayConfig();
         addRecipient(config.configValue, "ricardonascimento@petitsriens.be");
 
         Calendar date = new GregorianCalendar();
         date.add(Calendar.MONTH, 1);
 
         int month = date.get(Calendar.MONTH) + 1;
         String monthName = Dates.getMonthName(date);
         List<String> status = PersonStatus.getNotResident();
 
         List<Person> birthdays = Contract.birthDaysByMonth(month - 1, status);
         List<Contract> news = Contract.newEmployeesByMonth(date, status);
         List<Contract> outs = Contract.outEmployeesByMonth(date, status);
 
         setContentType("text/html");
         return sendAndWait(birthdays, monthName, month, news, outs);
     }
 
     public static boolean sendHolidayRequest(HolidayRequest holidayRequest) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject(Messages.get("holidayRequest")
                 + " - " + Ext.capFirst(holidayRequest.person.name)
                 + " " + Ext.capFirst(holidayRequest.person.firstname));
 
         Person chief = null;
         if (holidayRequest.person.department.person != null) {
             chief = holidayRequest.person.department.departmentChief;
         }
 
         if (holidayRequest.person.equals(chief)) {
             chief = null;
         }
 
         if (chief == null) {
             chief = holidayRequest.person.department.person;
         }
 
         if (holidayRequest.person.equals(chief)) {
             chief = null;
         }
 
         if (chief == null) {
             chief = Person.getDirector();
         }
 
         boolean isLsp = false;
         if (chief.user.hasRole("lsp")) {
             isLsp = true;
         }
 
         addRecipient(chief.user.email);
 
         setContentType("text/html");
         return sendAndWait(holidayRequest, chief,isLsp);
     }
 
     public static boolean sendHolidayRequestForUser(HolidayRequest holidayRequest) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject(Messages.get("holidayRequest")
                 + " - " + Ext.capFirst(holidayRequest.person.name)
                 + " " + Ext.capFirst(holidayRequest.person.firstname));
 
         User user = holidayRequest.person.user;
 
         if (user == null) {
             return true;
         }
 
         addRecipient(user.email);
 
         setContentType("text/html");
         return sendAndWait(holidayRequest);
     }
 
     public static boolean sendAlertAttendanceMA(Person person, int status) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Alerte, présences de nuit - " + Ext.format(new GregorianCalendar()));
 
         if (person.referent1 != null) {
             addRecipient(person.referent1.user.email);
         }
 
         if (person.referent2 != null) {
             addRecipient(person.referent2.user.email);
         }
 
         List<User> users = User.getDirMA();
         for (User user : users) {
             addRecipient(user.email);
         }
 
         setContentType("text/html");
         return sendAndWait(person, status);
     }
 
     public static boolean sendHolidayReminder(User user, List<HolidayRequest> holidayRequests) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Rappel, demande de congées en attente de validation");
 
         addRecipient(user.email);
 
         boolean isLsp = false;
         if (user.hasRole("lsp")) {
             isLsp = true;
         }
 
         setContentType("text/html");
         return sendAndWait(holidayRequests,isLsp);
     }
 
     public static boolean sendHolidayResponse(HolidayRequest holidayRequest) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
 
         if (holidayRequest.status == 2 || holidayRequest.status == 4) {
             setSubject("Demande de congés refusée - "+Ext.personLabel(holidayRequest.person));
         } else {
             setSubject("Demande de congés acceptée - "+Ext.personLabel(holidayRequest.person));
         }
 
         User user = holidayRequest.person.user;
 
         if (user == null) {
             addRecipient("ricardonascimento@petitsriens.be");
         } else {
             addRecipient(user.email);
         }
 
         setContentType("text/html");
         return sendAndWait(holidayRequest);
     }
 
     public static boolean sendHolidayReminderForRH(List<HolidayRequest> holidayRequests) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Rappel, demande de congées en attente de validation");
 
         List<Person> persons = Person.getRH();
 
         for (Person person : persons) {
             addRecipient(person.user.email);
         }
 
         setContentType("text/html");
         return sendAndWait(holidayRequests);
     }
 
     public static boolean sendReminderPending(List<Person> persons) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Congés - Personnes avec un solde négatif");
 
         List<Person> rhs = Person.getRH();
 
         for (Person person : rhs) {
             addRecipient(person.user.email);
         }
 
         setContentType("text/html");
         return sendAndWait(persons);
     }
 
     public static boolean newSaleInfo(Info info) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Informatique - Nouvelle vente");
 
         addRecipient("reparation@petitsriens.be");
 
         setContentType("text/html");
         return sendAndWait(info);
     }
 
     public static boolean sendAlertToChief(BS bs) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Bon de sortie - " + Ext.personLabel(bs.resident));
 
         addRecipient(bs.department.departmentChief.user.email);
         addRecipient(User.getInsMA().email);
 
         setContentType("text/html");
         return sendAndWait(bs);
     }
 
     public static boolean sendAlertToMAChief(BS bs) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Bon de sortie d\'urgence - " + Ext.personLabel(bs.resident));
 
         List<User> users = User.getMABS();
         for (User user : users) {
             addRecipient(user.email);
         }
 
         setContentType("text/html");
         return sendAndWait(bs);
     }
 
     public static boolean sendHolidayToValidateRH(HolidayRequest holidayRequest) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Demande de congés en attente de validation - " + Ext.personLabel(holidayRequest.person));
 
         List<Person> persons = Person.getRH();
 
         for (Person person : persons) {
             addRecipient(person.user.email);
         }
 
         setContentType("text/html");
         return sendAndWait(holidayRequest);
     }
 
     public static boolean sendHolidayResponseWaitFromRH(HolidayRequest holidayRequest) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("En attente : Demande de congés acceptée - "+Ext.personLabel(holidayRequest.person));
 
         User user = holidayRequest.person.user;
 
         if (user == null) {
             addRecipient("ricardonascimento@petitsriens.be");
         } else {
             addRecipient(user.email);
         }
 
         setContentType("text/html");
         return sendAndWait(holidayRequest);
     }
 
     public static boolean evaluationDone(Evaluation e) throws Exception{
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Evaluation faite : "+Ext.personLabel(e.resident));
 
         User user = User.getVincent();
         addRecipient(user.email);
 
         setContentType("text/html");
         return sendAndWait(e);
     }
 
     public static boolean bsValidated(BS bs) throws Exception{
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Bon de sortie validé : "+Ext.personLabel(bs.resident));
 
         User user = User.getInsMA();
         addRecipient(user.email);
 
         if(bs.resident.referent1 != null){
             addRecipient(bs.resident.referent1.user.email);
         }
 
         if(bs.resident.referent2 != null){
             addRecipient(bs.resident.referent2.user.email);
         }
 
         setContentType("text/html");
         return sendAndWait(bs);
     }
 
     public static boolean evaluationsNotDones(List<Evaluation> evaluations) throws Exception {
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Evaluations en retard");
 
         User user = User.getVincent();
         addRecipient(user.email);
 
         setContentType("text/html");
         return sendAndWait(evaluations);
     }
 
     public static boolean staysMustBeClosed(List<Stay> staysMustBeClosed) throws Exception{
         setFrom(new InternetAddress("app@petitsriens.be", "Admin"));
         setReplyTo(new InternetAddress("ricardonascimento@petitsriens.be", "Help"));
         setSubject("Dossier doit être clôturé");
 
         User user = User.getVincent();
         addRecipient(user.email);
 
         setContentType("text/html");
         return sendAndWait(staysMustBeClosed);
     }
 
     public static boolean sendErrorReport(String message) throws Exception{
         setFrom("app@petitsriens.be");
         addRecipient("ricardonascimento@petitsriens.be");
         setSubject("Exception APP ");
 
         setContentType("text/html");
         return sendAndWait(message);
     }
 }
