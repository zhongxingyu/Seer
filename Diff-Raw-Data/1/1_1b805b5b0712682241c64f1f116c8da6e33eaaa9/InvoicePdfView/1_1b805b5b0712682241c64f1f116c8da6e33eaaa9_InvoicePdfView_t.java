 package de.switajski.priebes.flexibleorders.report.itextpdf;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Component;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import de.switajski.priebes.flexibleorders.domain.Amount;
 import de.switajski.priebes.flexibleorders.domain.HandlingEvent;
 import de.switajski.priebes.flexibleorders.domain.Invoice;
 import de.switajski.priebes.flexibleorders.domain.helper.AmountCalculator;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.FourStrings;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.PdfPTableBuilder;
 
 @Component
 public class InvoicePdfView extends PriebesIText5PdfView {
 
 	private static final double VAT_RATE = 0.19d;
 
 	@Override
 	protected void buildPdfDocument(Map<String, Object> model,
 			Document document, PdfWriter writer, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		
 		Invoice invoice =  (Invoice) model.get(Invoice.class.getSimpleName());
 		insertHeader(document);
         insertAdresse(document, invoice.getInvoiceAddress());
         insertSubject(document, "Rechnungnr." + invoice.getDocumentNumber());
         insertInfo(document,"Rechnungsdatum: " + dateFormat.format(invoice.getCreated()));
         this.insertEmptyLines(document, 1);
         document.add(createTable(invoice, document));
         this.insertEmptyLines(document, 1);
 	}
 	
 	private PdfPTable createTable(Invoice invoice, Document doc){
 		PdfPTableBuilder builder = new PdfPTableBuilder(doc)
 		.setHeader(new FourStrings("Bestellpos.", "Artikel", "Menge x Preis", "Betrag"));
 		for (HandlingEvent he: invoice.getEvents()){
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
 		Amount net = AmountCalculator.calculateNetAmount(invoice);
 		//TODO: make vatRate dependent from order
 		Amount vat = AmountCalculator.calculateVatAmount(invoice, VAT_RATE);
 		builder.addFooterRow("Warenwert netto:   "+ net.toString())
 		.addFooterRow("zzgl. 19% MwSt.:     " + vat.toString());
 		
 		Amount shippingCosts = new Amount();
 		for (HandlingEvent shipment:invoice.getShippingCosts()){
 			Amount price = shipment.getOrderItem().getNegotiatedPriceNet();
 			if (!price.isGreaterZero())
 				throw new IllegalStateException("Versand ohne Preis angegeben!");
 			builder.addFooterRow("Versandkosten:     " + price.toString());
 			shippingCosts.add(price);
 		}
 		net = net.add(vat);
 		net = net.add(shippingCosts);
 		builder.addFooterRow("Gesamtbetrag brutto:   " + 
 				net.toString());
 		return builder.build();
 	}
 
 }
