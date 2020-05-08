 package de.switajski.priebes.flexibleorders.report.itextpdf;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Component;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.FontFactory;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import de.switajski.priebes.flexibleorders.domain.Address;
 import de.switajski.priebes.flexibleorders.domain.DeliveryNotes;
 import de.switajski.priebes.flexibleorders.domain.HandlingEvent;
 import de.switajski.priebes.flexibleorders.domain.Report;
 import de.switajski.priebes.flexibleorders.domain.helper.AmountCalculator;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.CustomPdfPTableBuilder;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.ParagraphBuilder;
 import de.switajski.priebes.flexibleorders.report.itextpdf.builder.PdfPTableBuilder;
 
 @Component
 public class DeliveryNotesPdfView extends PriebesIText5PdfView {
 
 	@Override
 	protected void buildPdfDocument(Map<String, Object> model,
 			Document document, PdfWriter writer, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		
 		DeliveryNotes report = (DeliveryNotes) model.get(DeliveryNotes.class.getSimpleName());
 		
 		String leftTop = "Lieferscheinnummer: "+ report.getDocumentNumber().toString();
 		String leftBottom = "Lieferdatum: " + dateFormat.format(report.getCreated());
 		String rightTop = "";
 		//TODO
 //		String rightBottom = "Kundennr.: " + "TODO";
 		String rightBottom = "";
 		Address adresse = report.getShippedAddress();
 		String heading = "Lieferschein";
 
 		// insert address
 		document.add(ParagraphBuilder.createEmptyLine());
 		document.add(ParagraphBuilder.createEmptyLine());
 		document.add(ParagraphBuilder.createEmptyLine());
 		document.add(ParagraphBuilder.createEmptyLine());
 		if (adresse == null) {
 			document.add(ParagraphBuilder.createEmptyLine());
 			document.add(ParagraphBuilder.createEmptyLine());
 			document.add(ParagraphBuilder.createEmptyLine());
 		} else {
 			document.add(new ParagraphBuilder(adresse.getName1())
 			.withIndentationLeft(36f)
 			.withLineSpacing(12f)
 			.addTextLine(adresse.getName2())
 			.addTextLine(adresse.getStreet())
 			.addTextLine(adresse.getPostalCode() + " " + adresse.getCity())
 			.addTextLine(adresse.getCountry().toString())
 			.build());
 		}
 		document.add(ParagraphBuilder.createEmptyLine());
 		document.add(ParagraphBuilder.createEmptyLine());
         
 		
         // insert heading
 		document.add(new ParagraphBuilder(heading)
 		.withFont(FontFactory.getFont(FONT, 12, Font.BOLD))
 		.build());
 		document.add(ParagraphBuilder.createEmptyLine());
 		
 
 		// info table
 		CustomPdfPTableBuilder infoTableBuilder = CustomPdfPTableBuilder.createInfoTable(
         		leftTop, leftBottom, rightTop, rightBottom);
         PdfPTable infoTable = infoTableBuilder.build();
 		infoTable.setWidthPercentage(100);
 		document.add(infoTable);
         //TODO: if (auftragsbestaetigung.getAusliefDatum==null) insertInfo(document,"Voraussichtliches Auslieferungsdatum:" + auftragsbestaetigung.getGeplAusliefDatum());
         document.add(ParagraphBuilder.createEmptyLine());
 
         
         // insert main table
         document.add(createTable(report));
 
 		
        // insert footer table
        CustomPdfPTableBuilder footerBuilder = CustomPdfPTableBuilder.createFooterBuilder(
				AmountCalculator.calculateNetAmount(report), 
				AmountCalculator.calculateVatAmount(report, OrderConfirmationPdfView.VAT_RATE), null, null, null)
				.withTotalWidth(PriebesIText5PdfView.WIDTH);
	    
	    PdfPTable footer = footerBuilder.build();
	    
	    footer.writeSelectedRows(0, -1,
	    		/*xPos*/ PriebesIText5PdfView.PAGE_MARGIN_LEFT, 
	    		/*yPos*/ PriebesIText5PdfView.PAGE_MARGIN_BOTTOM + FOOTER_MARGIN_BOTTOM, 
	    		writer.getDirectContent());
 	}
 	
 //	private PdfPTable createTable(DeliveryNotes deliveryNotes, Document doc) throws DocumentException{
 //		PdfPTableBuilder builder = PdfPTableBuilder.buildWithFourCols();
 //		for (HandlingEvent he: deliveryNotes.getEvents()){
 //			if (!he.getOrderItem().isShippingCosts()){
 //				builder.addBodyRow(
 //						new FourStrings("",
 //								// product Name
 //								"Art.Nr.: " + he.getOrderItem().getProduct().getProductNumber() + " - "
 //								+ he.getOrderItem().getProduct().getName(),
 //								// price
 //								he.getQuantity()+ " x " + he.getOrderItem().getNegotiatedPriceNet().toString(),
 //								// amount of single item
 //								he.getOrderItem().getNegotiatedPriceNet().multiply(he.getQuantity()).toString()
 //								));
 //			}
 //		}
 //		Amount net = AmountCalculator.calculateNetAmount(deliveryNotes);
 //		//TODO: make vatRate dependent from order
 //		Amount vat = AmountCalculator.calculateVatAmount(deliveryNotes, VAT_RATE);
 //		builder.addFooterRow("Warenwert netto:   "+ net.toString())
 //		.addFooterRow("zzgl. 19% MwSt.:     " + vat.toString())
 ////		.addFooterRow("Versandkosten:     " + deliveryNotes.getShipment().getShippingCosts().toString())
 //		.addFooterRow("Gesamtbetrag brutto:   " + net.add(vat));
 //		return builder.build();
 //	}
 	
 	private PdfPTable createTable(Report cReport) throws DocumentException{
 		PdfPTableBuilder builder = new PdfPTableBuilder(PdfPTableBuilder.createPropertiesWithFourCols());
 		for (HandlingEvent he: cReport.getEvents()){
 			if (!he.getOrderItem().isShippingCosts()){
 				List<String> row = new ArrayList<String>();
 				// Anzahl
 				row.add(String.valueOf(he.getQuantity()) + " x ");
 				// Art.Nr.:
 				row.add(he.getOrderItem().getProduct().getProductNumber().toString());
 				// Artikel
 				row.add(he.getOrderItem().getProduct().getName());
 				// Bestellnr
 				row.add(he.getOrderItem().getOrder().getOrderNumber());
 
 				builder.addBodyRow(row);
 			}
 		}
 		
 		return builder.withFooter(false).build();
 	}
 
 }
