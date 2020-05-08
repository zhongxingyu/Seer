 package controllers.compta;
 
 import controllers.security.Secure;
 import models.compta.*;
 import models.main.Person;
 import models.security.User;
 import org.apache.pdfbox.PDFToImage;
 import org.apache.pdfbox.util.PDFMergerUtility;
 import play.Play;
 import play.libs.Codec;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 @With(Secure.class)
 public class Invoices extends Controller {
 
     public static void index(int page) {
         if (page == 0) {
             page = 1;
         }
 
         User user = User.loadFromSession();
         List<Invoice> invoices;
         if (user.hasAtLeastOneRole(Arrays.asList("adminCompta","adminIV"))) {
             invoices = Invoice.getAll(page);
         } else {
             invoices = Invoice.byReceiver(user.person, page);
         }
 
         render(invoices);
     }
 
     public static void add() {
         Calendar date = new GregorianCalendar();
        long number = Invoice.nextNumber();
        render(date, number);
     }
 
     public static void showSearch() {
         List<Analytic> analytics = Analytic.getAll();
         render(analytics);
     }
 
     public static void creation(Invoice invoice, File invoicePDF) {
         invoice.token = Codec.UUID();
         invoice.save();
 
         invoicePDF.renameTo(new File(Invoice.INVOICE_DIR + "ID-" + invoice.id + ".pdf"));
 
         index(1);
     }
 
     public static void save(Invoice invoice, Person newReceiver, boolean sign
             , List<Input> inputs, File invoicePDF) {
         if (newReceiver.id != null) {
             invoice.changeReceiver(newReceiver);
         }
 
         if (invoicePDF != null) {
             invoice.status = 0;
             new File(Invoice.INVOICE_DIR + "ID-" + invoice.id + ".pdf").delete();
             invoicePDF.renameTo(new File(Invoice.INVOICE_DIR + "ID-" + invoice.id + ".pdf"));
         }
 
         invoice.save();
         invoice.saveInputs(inputs);
 
         if (sign) {
             chooseSignaturePlacement(invoice.id);
         } else {
             flash.success("saved");
             index(1);
         }
     }
 
     public static void delete(long id) {
         Invoice invoice = Invoice.findById(id);
         invoice.remove();
 
         flash.success("deleted");
 
         index(1);
     }
 
     public static void edit(long id) {
         Invoice invoice = Invoice.findById(id);
 
         render(invoice);
     }
 
     public static void show(String token) {
         Invoice invoice = Invoice.byToken(token);
         List<Input> inputs = Input.byInvoice(invoice);
         List<Analytic> analytics = Analytic.getAll();
         List<Building> buildings = Building.getAll();
         List<Car> cars = Car.getAll();
 
         render(invoice, inputs, analytics, buildings, cars);
     }
 
     public static void chooseSignaturePlacement(long id) {
         Invoice invoice = Invoice.findById(id);
 
         String[] args_2 = new String[7];
         args_2[0] = "-startPage";
         args_2[1] = "1";
         args_2[2] = "-endPage";
         args_2[3] = "1";
         args_2[4] = "-outputPrefix";
         args_2[5] = Invoice.INVOICE_IMG_DIR + "ID-" + invoice.id;
         if (invoice.status == 1) {
             args_2[6] = new File(Invoice.INVOICE_SIGNED_DIR + "ID-" + id + ".pdf").getAbsolutePath();
         } else {
             args_2[6] = new File(Invoice.INVOICE_DIR + "ID-" + id + ".pdf").getAbsolutePath();
         }
 
         try {
             PDFToImage.main(args_2);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         render(invoice);
     }
 
     public static void sign(long id, float x, float y) {
         Invoice invoice = Invoice.findById(id);
 
         try {
             invoice.addSignature(User.loadFromSession(),invoice, x, y);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         invoice.status = 1;
         invoice.save();
 
         index(1);
     }
 
     public static void search(Calendar startAt, Calendar endAt, Supplier supplier
             , Analytic analytic, long number, double startAmount, double endAmount
             , Person receiver) {
 
         List<Invoice> invoices = Invoice.search(startAt, endAt, supplier, analytic, number
                 , startAmount, endAmount,receiver);
 
         render(invoices, startAt, endAt, supplier, analytic, number, startAmount, endAmount,receiver);
     }
 
     public static void searchPDF(Calendar startAt, Calendar endAt, Supplier supplier
             , Analytic analytic, long number, double startAmount, double endAmount
             , Person receiver) throws Exception {
 
         List<Invoice> invoices = Invoice.search(startAt, endAt, supplier, analytic, number
                 , startAmount, endAmount,receiver);
 
         String destFile = Codec.UUID();
         PDFMergerUtility mergePdf = new PDFMergerUtility();
         for (Invoice invoice : invoices) {
             mergePdf.addSource(invoice.getPDF());
         }
         mergePdf.setDestinationFileName(Play.tmpDir.getAbsolutePath() + File.separator + destFile + ".pdf");
         mergePdf.mergeDocuments();
 
         renderBinary(new File(Play.tmpDir.getAbsolutePath() + File.separator + destFile + ".pdf"));
     }
 
     public static void showInvoicePDF(long id) {
         Invoice invoice = Invoice.findById(id);
 
         renderBinary(invoice.getPDF());
     }
 
     public static void showInvoiceIMG(long id) {
         renderBinary(new File(Invoice.INVOICE_IMG_DIR + "ID-" + id + "1.jpg"));
     }
 
     public static void showSignIMG(){
         User user = User.loadFromSession();
 
         renderBinary(new File(Invoice.INVOICE_SIGN_DIR+user.username+".png"));
     }
 
     public static void showUserSignIMG(long id){
         Person person = Person.findById(id);
 
         renderBinary(new File(Invoice.INVOICE_SIGN_DIR+person.user.username+".png"));
     }
 
 }
