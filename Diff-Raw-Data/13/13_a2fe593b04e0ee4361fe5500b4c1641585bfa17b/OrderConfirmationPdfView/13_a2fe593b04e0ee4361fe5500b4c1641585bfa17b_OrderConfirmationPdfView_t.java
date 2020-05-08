 package de.switajski.priebes.flexibleorders.report.itextpdf;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Component;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import de.switajski.priebes.flexibleorders.domain.Amount;
 import de.switajski.priebes.flexibleorders.domain.ConfirmationReport;
 import de.switajski.priebes.flexibleorders.domain.HandlingEvent;
 import de.switajski.priebes.flexibleorders.domain.helper.AmountCalculator;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.FourStrings;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.PdfPTableBuilder;
 
 @Component
 public class OrderConfirmationPdfView extends PriebesIText5PdfView {
 
 	//TODO: make VAT_RATE dependent from order
	private static final Double VAT_RATE = 0.19d;
 
 	@Override
 	protected void buildPdfDocument(Map<String, Object> model,
 			Document document, PdfWriter writer, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		
 		ConfirmationReport confirmationReport =  (ConfirmationReport) model.get(ConfirmationReport.class.getSimpleName());
 		insertHeader(document);
         insertAdresse(document, confirmationReport.getInvoiceAddress());
         //TODO: A-Umlaut wird nicht angezeigt
        insertSubject(document,"Auftragsbestaetigungsnr.                                   Kundennr.:" 
         		+ confirmationReport.getDocumentNumber());
         insertInfo(document,"Auftragsdatum: " + dateFormat.format(confirmationReport.getCreated()));
         //TODO: if (auftragsbestaetigung.getAusliefDatum==null) insertInfo(document,"Voraussichtliches Auslieferungsdatum:" + auftragsbestaetigung.getGeplAusliefDatum());
         this.insertEmptyLines(document, 2);
         document.add(createTable(confirmationReport, document));
 	}
 	
 	private PdfPTable createTable(ConfirmationReport cReport, Document document){
 		PdfPTableBuilder builder = new PdfPTableBuilder(document)
 		.setHeader(new FourStrings("Bestellpos.", "Artikel", "Menge x Preis", "Betrag"));
 		for (HandlingEvent he: cReport.getEvents()){
 			builder.addBodyRow(
 				new FourStrings("",
 				// product Name
 				"Art.Nr.: " + he.getOrderItem().getProduct().getProductNumber() + " - "
 				+ he.getOrderItem().getProduct().getName(),
 				// price
 				he.getQuantity()+ " x " + he.getOrderItem().getNegotiatedPriceNet().toString(),
 				// amount of single item
 				he.getOrderItem().getNegotiatedPriceNet().multiply(he.getQuantity()).toString()
 			));
 		}
 		Amount net = AmountCalculator.calculateNetAmount(cReport);
 		Amount vat = AmountCalculator.calculateVatAmount(cReport, VAT_RATE);
 		builder.addFooterRow("Warenwert netto:   "+ net.toString())
 		.addFooterRow("zzgl. 19% MwSt.   " + vat.toString())
 		.addFooterRow("Gesamtbetrag brutto:   " + net.add(vat).toString());
 		return builder.build();
 	}
 	
 }
