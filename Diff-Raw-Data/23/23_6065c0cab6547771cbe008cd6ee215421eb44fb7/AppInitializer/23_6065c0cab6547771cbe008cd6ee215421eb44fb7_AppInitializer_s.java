 package cz.muni.fi.pv243.et.util;
 
 import cz.muni.fi.pv243.et.data.ExpenseReportListProducer;
 import cz.muni.fi.pv243.et.data.ExpenseReportRepository;
 import cz.muni.fi.pv243.et.data.MoneyTransferListProducer;
 import cz.muni.fi.pv243.et.data.MoneyTransferRepository;
 import cz.muni.fi.pv243.et.data.PaymentListProducer;
 import cz.muni.fi.pv243.et.data.PaymentRepository;
 import cz.muni.fi.pv243.et.data.PersonListProducer;
 import cz.muni.fi.pv243.et.data.PersonRepository;
 import cz.muni.fi.pv243.et.data.PurposeListProducer;
 import cz.muni.fi.pv243.et.data.PurposeRepository;
 import cz.muni.fi.pv243.et.data.ReceiptListProducer;
 import cz.muni.fi.pv243.et.data.ReceiptRepository;
 import cz.muni.fi.pv243.et.model.Currency;
 import cz.muni.fi.pv243.et.model.ExpenseReport;
 import cz.muni.fi.pv243.et.model.MoneyTransfer;
 import cz.muni.fi.pv243.et.model.Payment;
 import cz.muni.fi.pv243.et.model.Person;
 import cz.muni.fi.pv243.et.model.PersonRole;
 import cz.muni.fi.pv243.et.model.Purpose;
 import cz.muni.fi.pv243.et.model.Receipt;
 import cz.muni.fi.pv243.et.model.ReportStatus;
 import cz.muni.fi.pv243.et.model.UserModel;
 import cz.muni.fi.pv243.et.security.EventLog;
 import cz.muni.fi.pv243.et.security.UserManager;
 import org.picketlink.idm.IdentityManager;
 import org.picketlink.idm.credential.internal.Password;
 import org.picketlink.idm.model.*;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.inject.Inject;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import javax.enterprise.event.Event;
 import javax.inject.Named;
 import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
 
 @Singleton
 @Startup
 @Named
 public class AppInitializer {
 
     @Inject
     private EventLog log;
 
     @Inject
     private IdentityManager identityManager;
 
     @Inject
     private PersonRepository personRepository;
 
     @Inject
     private Event<ExceptionToCatchEvent> exceptionEvent;
 
     @Inject
     private PersonListProducer plp;
 
     @Inject
     private PaymentRepository paymentRepo;
 
     @Inject
     private PurposeRepository purposeRepo;
 
     @Inject
     private ReceiptRepository receiptRepo;
 
     @Inject
     private ExpenseReportRepository expenseRepo;
 
     @Inject
     private MoneyTransferRepository moneyRepo;
 
     @Inject
     private UserManager userManager;
 
     @PostConstruct
     private void initAdmin() {
         log.logInitializingUsers();
 
         // create admin if none exists
         if (identityManager
                 .createIdentityQuery(User.class)
                 .setParameter(User.LOGIN_NAME, "admin")
                 .getResultCount() == 0) {
 
             Person adminPerson = new Person("Admin", "Admin", "admin@admin.sk", "000000000");
             personRepository.create(adminPerson);
             Long id = adminPerson.getId();
 
             User admin = new SimpleUser();
             admin.setLoginName("admin");
             admin.setFirstName("admin");
             admin.setLastName("admin");
             admin.setAttribute(new Attribute<String>("personId", id.toString()));
 
             identityManager.add(admin);
 
             Password password = new Password("admin".toCharArray());
             this.identityManager.updateCredential(admin, password);
 
             Role adminRole = identityManager.getRole(PersonRole.ADMIN.toString());
 
             if (adminRole == null) {
                 adminRole = new SimpleRole(PersonRole.ADMIN.toString());
                 identityManager.add(adminRole);
             }
 
             Role verifierRole = identityManager.getRole(PersonRole.VERIFIER.toString());
 
             if (verifierRole == null) {
                 verifierRole = new SimpleRole(PersonRole.VERIFIER.toString());
                 identityManager.add(verifierRole);
             }
 
             identityManager.grantRole(admin, adminRole);
             identityManager.grantRole(admin, verifierRole);
         }
     }
 
     public String initialize() {
         // create persons
         Person personTom = createPerson("Tomas", "Submitterovic", "test@test.cz", "123456789");
         
         Person personJana = createPerson("Jana", "Submitterova", "nova.jana@test.cz", "789313244");
 
         Person personVerifier = createPerson("Hermiona", "Verifierova", "herim@ona.cz", "120313244");
 
         // PURPOSES
         Purpose purp1 = new Purpose("TestPurp", "Purpose 2 Desctipriton");
         purposeRepo.create(purp1);
 
         Purpose purp2 = new Purpose("Purpose 2", "Purpose of not writing nice Java code");
         purposeRepo.create(purp2);
 
         Purpose purp3 = new Purpose("Purpose 3", "Arquillian not testing purpose - give us some money bicth!");
         purposeRepo.create(purp3);
 
 
         // RECEIPTS
        Receipt rec = new Receipt(new Date(System.currentTimeMillis() - 100000), personTom, null, "Platba za vlak a autobus");
         receiptRepo.create(rec);
 
        Receipt rec2 = new Receipt(new Date(System.currentTimeMillis() - 20000), personJana, null, "Way there and back");
         receiptRepo.create(rec2);
 
         // REPORTS
         ExpenseReport report = new ExpenseReport("Berlin 2013 Bienale", "Visit of some stupid Conference", personTom, personJana, ReportStatus.OPEN);
         report.setLastSubmittedDate(new Date(System.currentTimeMillis()));
         expenseRepo.create(report);
 
         ExpenseReport report2 = new ExpenseReport("Munich 2012 FUD Conference", "Conference in Munich", personTom, null, ReportStatus.APPROVED);
         report2.setLastSubmittedDate(new Date(System.currentTimeMillis()));
         expenseRepo.create(report2);
 
         ExpenseReport report3 = new ExpenseReport("Non Testing in Arquillian 2012", "Why are we not testing in Arquillian?", personJana, personVerifier, ReportStatus.REJECTED);
         report3.setLastSubmittedDate(new Date(System.currentTimeMillis()));
         expenseRepo.create(report3);
 
         // MONEY TRANSFERS
         MoneyTransfer mt = createMoneyTransfer(personVerifier, report2, BigDecimal.valueOf(2400), Currency.CZK, new Date(System.currentTimeMillis() + 20));
         MoneyTransfer mt2 = createMoneyTransfer(personVerifier, report3, BigDecimal.valueOf(200), Currency.CZK, new Date(System.currentTimeMillis() - 15000));
         MoneyTransfer mt3 = createMoneyTransfer(personVerifier, report3, BigDecimal.valueOf(100), Currency.CZK, new Date(System.currentTimeMillis() - 14000));
 
         // PAYMENTS
         Payment payment = createPayment(report, purp1, rec, BigDecimal.valueOf(1500), Currency.CZK);
         Payment payment2 = createPayment(report2, purp2, rec, BigDecimal.valueOf(2345), Currency.CZK);
         Payment payment3 = createPayment(report3, purp3, rec2, BigDecimal.valueOf(300), Currency.CZK);
         Payment payment4 = createPayment(report3, purp3, rec2, BigDecimal.valueOf(100), Currency.CZK);
 
         return "created";
     }
 
     private Person createPerson(String firstName, String lastName, String email, String bankAccount) {
         UserModel model = new UserModel();
         model.setFirstName(firstName);
         model.setLastName(lastName);
         model.setEmail(email);
         model.setBankAccount(bankAccount);
         model.setUserName(firstName.toLowerCase());
         userManager.add(model);
 
         // firstname.tolowercase == username == password
         userManager.changePassword(model.getUserName(), model.getUserName());
 
         return plp.getPerson(model.getId());
     }
 
     private Payment createPayment(ExpenseReport report, Purpose purpose, Receipt receipt, BigDecimal value, Currency currency) {
         Payment payment = new Payment();
         payment.setPurpose(purpose);
         payment.setReport(report);
         payment.setReceipt(receipt);
         payment.setCurrency(currency);
         payment.setValue(value);
         payment.setDate(new Date(System.currentTimeMillis() + 10000));
         paymentRepo.create(payment);
 
         return payment;
     }
 
     private MoneyTransfer createMoneyTransfer(Person creator, ExpenseReport report, BigDecimal value, Currency currency, Date date) {
         MoneyTransfer mt = new MoneyTransfer();
         mt.setReport(report);
         mt.setCreator(creator);
         mt.setValue(value);
         mt.setCurrency(currency);
         mt.setDate(date);
         moneyRepo.create(mt);
 
         return mt;
     }
 }
