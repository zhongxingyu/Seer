 package de.switajski.priebes.flexibleorders.report.itextpdf;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Component;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import de.switajski.priebes.flexibleorders.domain.Amount;
 import de.switajski.priebes.flexibleorders.domain.DeliveryNotes;
 import de.switajski.priebes.flexibleorders.domain.HandlingEvent;
 import de.switajski.priebes.flexibleorders.domain.helper.AmountCalculator;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.FourStrings;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.PdfPTableBuilder;
 
 @Component
 public class DeliveryNotesPdfView extends PriebesIText5PdfView {
 
 	private static final double VAT_RATE = 0.19d;
 
 	@Override
 	protected void buildPdfDocument(Map<String, Object> model,
 			Document document, PdfWriter writer, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		
 		DeliveryNotes invoice =  (DeliveryNotes) model.get(DeliveryNotes.class.getSimpleName());
 		insertHeader(document);
         insertAdresse(document, invoice.getShippedAddress());
        insertSubject(document, "Lieferschein Nr." + invoice.getDocumentNumber());
         insertInfo(document,"Lieferdatum: " + dateFormat.format(invoice.getCreated()));
         this.insertEmptyLines(document, 1);
         document.add(createTable(invoice, document));
         this.insertEmptyLines(document, 1);
 //        this.insertSmallText(document, invoice.getPaymentConditions());
 	}
 	
 	private PdfPTable createTable(DeliveryNotes deliveryNotes, Document doc){
 		PdfPTableBuilder builder = new PdfPTableBuilder(doc)
 		.setHeader(new FourStrings("Bestellpos.", "Artikel", "Menge x Preis", "Betrag"));
 		for (HandlingEvent he: deliveryNotes.getEvents()){
 			if (!he.getOrderItem().isShippingCosts()){
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
 		}
 		Amount net = AmountCalculator.calculateNetAmount(deliveryNotes);
 		//TODO: make vatRate dependent from order
 		Amount vat = AmountCalculator.calculateVatAmount(deliveryNotes, VAT_RATE);
 		builder.addFooterRow("Warenwert netto:   "+ net.toString())
 		.addFooterRow("zzgl. 19% MwSt.:     " + vat.toString())
 //		.addFooterRow("Versandkosten:     " + deliveryNotes.getShipment().getShippingCosts().toString())
 		.addFooterRow("Gesamtbetrag brutto:   " + net.add(vat));
 		return builder.build();
 	}
 
 }
