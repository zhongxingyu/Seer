 package models.compta;
 
 import com.lowagie.text.Image;
 import com.lowagie.text.pdf.BaseFont;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfReader;
 import com.lowagie.text.pdf.PdfStamper;
 import ext.Ext;
 import jobs.InvoiceNewReceiverMail;
 import models.main.Person;
 import models.security.User;
 import play.Play;
 import play.db.jpa.Model;
 import play.libs.Codec;
 import play.libs.Files;
 import utils.Dates;
 
 import javax.persistence.*;
 import java.awt.*;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 @Entity
 public class Invoice extends Model {
 
     public static final int PAGE_SIZE = 50;
     public static final String INVOICE_DIR = Play.applicationPath.getAbsolutePath()
             + File.separator + "data" + File.separator + "invoice" + File.separator;
 
     public static final String INVOICE_SIGNED_DIR = Play.applicationPath.getAbsolutePath()
             + File.separator + "data" + File.separator + "invoiceSigned" + File.separator;
 
     public static final String INVOICE_IMG_DIR = Play.applicationPath.getAbsolutePath()
             + File.separator + "data" + File.separator + "invoiceIMG" + File.separator;
 
     public static final String INVOICE_SIGN_DIR = Play.applicationPath.getAbsolutePath()
             + File.separator + "data" + File.separator + "signature" + File.separator;
 
    public long number;
     @Temporal(TemporalType.DATE)
     public Calendar date;
     @ManyToOne
     public Supplier supplier;
     public double amount;
     @ManyToOne
     public Person receiver;
     public String token;
     public int status;
     @ManyToMany(fetch = FetchType.EAGER)
     public List<Analytic> analytics = new ArrayList<Analytic>();
 
     public static List<Invoice> getAll(int page) {
         return Invoice.find("order by id").fetch(page, PAGE_SIZE);
     }
 
     public void remove() {
         Input.deleteByInvoice(this);
         this.removePDF();
         this.delete();
     }
 
     private void removePDF() {
         File pdf = new File(INVOICE_DIR + "ID-" + this.id + ".pdf");
         File pdfSinged = new File(INVOICE_SIGNED_DIR + "ID-" + this.id + ".pdf");
 
         if (pdf.exists()) {
             pdf.delete();
         }
 
         if (pdfSinged.exists()) {
             pdfSinged.delete();
         }
     }
 
     public static Invoice byToken(String token) {
         return Invoice.find("token = ?", token).first();
     }
 
     public void changeReceiver(Person newReceiver) {
         Person oldReceiver = this.receiver;
         this.receiver = newReceiver;
         this.token = Codec.UUID();
 
         try {
             new InvoiceNewReceiverMail(this, oldReceiver).now();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void saveInputs(List<Input> inputs) {
         if (inputs == null) {
             return;
         }
 
         this.analytics.clear();
         for (Input input : inputs) {
             Input.update(input, this);
 
             if (input.analytic.id != null) {
                 this.analytics.add(input.analytic);
             }
         }
 
         this.save();
     }
 
     public static List<Invoice> byReceiver(Person receiver, int page) {
         return Invoice.find("receiver = ?", receiver).fetch(page, Invoice.PAGE_SIZE);
     }
 
     public static List<Invoice> search(Calendar startAt, Calendar endAt, Supplier supplier,
                                        Analytic analytic, long number, double startAmount, double endAmount
             , Person receiver) {
 
         if (startAt == null) {
             startAt = Dates.getFirstDayOfMonth(new GregorianCalendar());
         }
 
         if (endAt == null) {
             endAt = Dates.getLastDayOfMonth(new GregorianCalendar());
         }
 
         if (endAmount == 0) {
             endAmount = Double.MAX_VALUE;
         }
 
         if (supplier.id == null) {
             supplier = null;
         }
 
         if (analytic.id == null) {
             analytic = null;
         }
 
         if (receiver.id == null) {
             receiver = null;
         }
 
         return Invoice.find("select i from Invoice i left join i.analytics a " +
                 "where (? = 0L or i.number = ?) " +
                 "and (i.date >= ? and i.date <= ?) " +
                 "and (? is null or i.supplier = ?) " +
                 "and (? is null or i.receiver = ?) " +
                 "and (? is null or a = ?) " +
                 "and (i.amount >= ? and i.amount <= ?)", number, number, startAt, endAt, supplier, supplier,
                 receiver, receiver, analytic, analytic, startAmount, endAmount).fetch();
     }
 
    public static long nextNumber() {
        long invoiceNumber = 0;
        try {
            invoiceNumber = Invoice.find("select max(number) "
                    + "from Invoice ").first();
        } catch (NullPointerException npe) {
        }

        invoiceNumber++;

        return invoiceNumber;
    }

     public void addSignature(User user, Invoice invoice, float x, float y) {
         try {
             PdfReader reader;
             if (invoice.status == 0) {
                 reader = new PdfReader(INVOICE_DIR + "ID-" + invoice.id + ".pdf");
             } else {
                 Files.copy(new File(INVOICE_SIGNED_DIR + "ID-" + invoice.id + ".pdf"),
                         new File(INVOICE_SIGNED_DIR + "ID-" + invoice.id + "-copy.pdf"));
                 Files.delete(new File(INVOICE_SIGNED_DIR + "ID-" + invoice.id + ".pdf"));
 
                 reader = new PdfReader(INVOICE_SIGNED_DIR + "ID-" + invoice.id + "-copy.pdf");
             }
 
             PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(INVOICE_SIGNED_DIR + "ID-" + invoice.id + ".pdf"));
 
             PdfContentByte cb = stamper.getOverContent(1);
             cb.setColorFill(Color.BLUE);
             BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
 
             float currentY = y;
             int fontSIZE = 10;
 
             cb.beginText();
             cb.moveText(x, y);
             cb.setFontAndSize(bf, fontSIZE);
             cb.showText("FACT " + invoice.number);
             cb.endText();
 
            for (Analytic analytic : analytics) {
                Input input = Input.byAnalyticAndInvoice(analytic, invoice);
                 currentY -= fontSIZE;
                 cb.beginText();
                 cb.moveText(x, currentY);
                 cb.setFontAndSize(bf, fontSIZE);
                cb.showText(analytic.name + " - " + Ext.format2(input.amount, "#.#") + "€");
                 cb.endText();
             }
 
             currentY -= fontSIZE;
             cb.beginText();
             cb.moveText(x, currentY);
             cb.setFontAndSize(bf, fontSIZE);
             cb.showText(Ext.format(invoice.date));
             cb.endText();
 
             currentY -= fontSIZE;
 
             String filename = Invoice.INVOICE_SIGN_DIR + user.username + ".png";
             Image image = Image.getInstance(filename);
             image.setAbsolutePosition(x, currentY - 75);
             image.scaleAbsolute(125, 75);
             cb.addImage(image);
 
             stamper.close();
             Files.delete(new File(INVOICE_SIGNED_DIR + "ID-" + invoice.id + "-copy.pdf"));
         } catch (Exception e) {
             e.printStackTrace();
             Files.delete(new File(INVOICE_SIGNED_DIR + "ID-" + invoice.id + ".pdf"));
             new File(INVOICE_SIGNED_DIR + "ID-" + invoice.id + "-copy.pdf").renameTo(
                     new File(INVOICE_SIGNED_DIR + "ID-" + invoice.id + ".pdf"));
         }
     }
 
     public File getPDF() {
         if (this.status == 0) {
             return new File(INVOICE_DIR + "ID-" + this.id + ".pdf");
         } else {
             return new File(INVOICE_SIGNED_DIR + "ID-" + this.id + ".pdf");
         }
     }
 
     public static List<Invoice> byDateNotSigned(Calendar date) {
         return Invoice.find("date = ? " +
                 "and status = 0 ", date).fetch();
     }
 }
