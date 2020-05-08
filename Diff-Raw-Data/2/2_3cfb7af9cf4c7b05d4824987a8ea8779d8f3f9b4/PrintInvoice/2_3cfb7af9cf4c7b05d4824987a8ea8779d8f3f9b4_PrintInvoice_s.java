 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package utils;
 
 import com.itextpdf.text.BaseColor;
 import com.itextpdf.text.Chunk;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Font.FontFamily;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfWriter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import pojos.SaleBillPharma;
 import utils.MyLogger;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.BaseFont;
 import com.itextpdf.text.pdf.ColumnText;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import pojos.Customer;
 import pojos.SaleBillPharmaItem;
 
 /**
  *
  * @author ashutoshsingh
  */
 public class PrintInvoice {
     
     private Document document = new Document(PageSize.A4);
     private SaleBillPharma salebill ;
     
    public static final Font[] FONT = new Font[5];
     static {
         FONT[0] = new Font(FontFamily.HELVETICA, 24);
         FONT[1] = new Font(FontFamily.HELVETICA, 18);
         FONT[2] = new Font(FontFamily.HELVETICA, 10);
         FONT[3] = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
         FONT[4] = new Font(FontFamily.TIMES_ROMAN, 12, Font.ITALIC | Font.UNDERLINE);
     }
     public static final Font BOLD_UNDERLINED = new Font(FontFamily.TIMES_ROMAN, 12, Font.BOLD | Font.UNDERLINE);
      public static final Font ITALICS = new Font(FontFamily.TIMES_ROMAN, 12, Font.ITALIC | Font.UNDERLINE);
     
     
      private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
     public PrintInvoice(SaleBillPharma salebill){
         this.salebill = salebill;
     }
     
     public void getDocument(){
         try{
              PdfWriter writer = PdfWriter.getInstance(document,  new FileOutputStream("SaleBill#"+salebill.getId()+".pdf"));
 
             document.open();
             ////////////////////////////////////////////////////////////////////////////////////
             ///////////////////Start Document Here/////////////////////////////////
             PdfContentByte directContent = writer.getDirectContent();
             Paragraph p1 = new Paragraph("SALE BILL");
             p1.setFont(FONT[4]);
             p1.setAlignment(Element.ALIGN_CENTER);
                     
                 document.add(p1);
             //show the company details here.
             Phrase company = new Phrase(new Chunk("BIO PHARMA\nAKOT 444101(M.S)", FONT[3]));
             document.add(company);
             document.add(new Phrase("\nLicense No : 20B : AK-88888\n                     21B : AK-88889",FONT[2]));
             
             System.out.println(dateFormatter.format(salebill.getBillDate()));
             //show the invoice details
           //  String txt = "Bill No. : " + salebill.getId()+"\nBill Date : " + dateFormatter.format(salebill.getBillDate()) +;
             Phrase invoiceDetails = new Phrase( "Bill No. : " + salebill.getId());
             ColumnText.showTextAligned(directContent, Element.ALIGN_LEFT, invoiceDetails , 400, 693,0);
             invoiceDetails = new Phrase("Bill Date : " + dateFormatter.format(salebill.getBillDate()));
             ColumnText.showTextAligned(directContent, Element.ALIGN_LEFT, invoiceDetails , 400, 681,0);
             invoiceDetails = new Phrase("Mode of Payment : " + salebill.getMode());
             ColumnText.showTextAligned(directContent, Element.ALIGN_LEFT, invoiceDetails , 400, 668,0);
             
             //show the customer details
             Customer c = salebill.getCustomerId();
             Phrase custDetails = new Phrase("SOLD TO", FONT[3]);
             ColumnText.showTextAligned(directContent, Element.ALIGN_LEFT, custDetails , 35, 707,0);
             custDetails = new Phrase(c.getCompanyName());
             ColumnText.showTextAligned(directContent, Element.ALIGN_LEFT, custDetails , 35, 693,0);
             custDetails = new Phrase(c.getSiteAddress());
             ColumnText.showTextAligned(directContent, Element.ALIGN_LEFT, custDetails , 35, 681,0);
             custDetails = new Phrase("Licence : "+c.getLicenceNo());
             ColumnText.showTextAligned(directContent, Element.ALIGN_LEFT, custDetails , 35, 668,0);
             
             
             
             
             document.add(Chunk.NEWLINE);
             document.add(Chunk.NEWLINE);
             document.add(Chunk.NEWLINE);
             document.add(Chunk.NEWLINE);
             document.add(Chunk.NEWLINE);
             
             //Item Particulars are shown here
             PdfPTable table = new PdfPTable(5);
             table.setTotalWidth(new float[]{275,50,50,50,75});
             table.setHeaderRows(1);
             
             //headers
             table.getDefaultCell().setBackgroundColor(BaseColor.LIGHT_GRAY);
             table.addCell("Particulars");
             table.addCell("MRP");
             table.addCell("Rate");
             table.addCell("Qnty");
             table.addCell("SubTotal");
             table.getDefaultCell().setBackgroundColor(null);
             table.setSpacingAfter(5.0f);
             
              List<SaleBillPharmaItem> items = salebill.getSaleBillPharmaItemList();
              for(int i = 0 ;i <items.size();i++){
                 PdfPCell desc = new PdfPCell(new Phrase(items.get(i).getItemName()));
 //                 //desc.setBorderColor(BaseColor.WHITE);
 //                 desc.setBorderColorLeft(BaseColor.BLACK);
 //                 desc.setBorderColorRight(BaseColor.WHITE);
                  table.addCell(desc);
                  PdfPCell mrp = new PdfPCell(new Phrase(items.get(i).getMrp()+""));
 //                 //mrp.setBorderColor(BaseColor.WHITE);
 //                 mrp.setBorderColorLeft(BaseColor.BLACK);
 //                 mrp.setBorderColorRight(BaseColor.WHITE);
                  table.addCell(mrp);
                  PdfPCell rate = new PdfPCell(new Phrase(items.get(i).getItemRate()+""));
 //                 //rate.setBorderColor(BaseColor.WHITE);
 //                 rate.setBorderColorLeft(BaseColor.BLACK);
 //                 rate.setBorderColorRight(BaseColor.WHITE);
                  table.addCell(rate);
                  PdfPCell quantity = new PdfPCell(new Phrase(items.get(i).getQnty()+""));
 //                 //quantity.setBorderColor(BaseColor.WHITE);
 //                 quantity.setBorderColorLeft(BaseColor.BLACK);
 //                 quantity.setBorderColorRight(BaseColor.WHITE);
                  table.addCell(quantity);
                  PdfPCell subtotal = new PdfPCell(new Phrase(items.get(i).getAmt()+""));
 //                 //subtotal.setBorderColor(BaseColor.WHITE);
 //                 subtotal.setBorderColorLeft(BaseColor.BLACK);
 //                 subtotal.setBorderColorRight(BaseColor.WHITE);
                  table.addCell(subtotal);
                  
              }
              
              //now show the sub details
              PdfPCell finalCell = new PdfPCell(new Phrase("Total VAT Amt : Rs " + salebill.getTotalVat() + "                     Total Amount : Rs "));
              finalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
              finalCell.setColspan(4);
              table.addCell(finalCell);
              table.addCell(""+salebill.getTotalAmt());
              
              PdfPCell cdCell = new PdfPCell(new Phrase("Cash Discount (2 %) : (-) Rs"));
              cdCell.setColspan(4);
              cdCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
              table.addCell(cdCell);
              table.addCell(""+salebill.getDiscount());
              
              
              
              PdfPCell finalAmtCell = new PdfPCell(new Phrase("Final Amount : Rs" ));
              finalAmtCell.setColspan(4);
              finalAmtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
              table.addCell(finalAmtCell);
              table.addCell(""+salebill.getFinalAmt());
                        
              
              
              
             document.add(table);
               document.add(Chunk.NEWLINE);
                 document.add(Chunk.NEWLINE);
                 
              Paragraph sign = new Paragraph(new Chunk("Authorized signatory\n(BIO PHARMA)"))   ;
              sign.setAlignment(Element.ALIGN_RIGHT);
              document.add(sign);
                 document.add(Chunk.NEWLINE);
              document.add(Chunk.NEWLINE);
              
             document.add(new Chunk("Terms and Conditions ", FONT[3]));
               document.add(Chunk.NEWLINE);
             document.add(new Chunk("1. This invoice is valid only for 30 days from date of generation"));
               document.add(Chunk.NEWLINE);
             document.add(new Chunk("2. All disputes will be subject to AKOT jurisdiction."));
             
             
             document.add(Chunk.NEWLINE);
             
             Paragraph p = new Paragraph("THANK YOU FOR YOUR BUSINESS");
             p.setFont(FONT[4]);
             p.setAlignment(Element.ALIGN_CENTER);
             document.add(p);
             
             
             
             
             
             ///////////////////End Documnet here//////////////////////////////////
             ///////////////////////////////////////////////////////////////////////////////////
             document.close(); // no need to close PDFwriter?
             
         } catch (DocumentException | FileNotFoundException e) {
             //LOGGER
             e.printStackTrace();
         }
      
 }
     
     public void drawTable(PdfContentByte directcontent){
        
     }
     
     
      /** The number of locations on our time table. */
     public static final int LOCATIONS = 9;
     /** The number of time slots on our time table. */
     public static final int TIMESLOTS = 32;
  
     /** The offset to the left of our time table. */
     public static final float OFFSET_LEFT = 30;
     /** The width of our time table. */
     public static final float WIDTH = 540;
     /** The offset from the bottom of our time table. */
     public static final float OFFSET_BOTTOM = 80;
     /** The height of our time table */
     public static final float HEIGHT = 504;
  
     /** The offset of the location bar next to our time table. */
     public static final float OFFSET_LOCATION = 26;
     /** The width of the location bar next to our time table. */
     public static final float WIDTH_LOCATION = 48;
  
     /** The height of a bar showing the movies at one specific location. */
     public static final float HEIGHT_LOCATION = HEIGHT / LOCATIONS;
     /** The width of a time slot. */
     public static final float WIDTH_TIMESLOT = WIDTH / TIMESLOTS;
 }
