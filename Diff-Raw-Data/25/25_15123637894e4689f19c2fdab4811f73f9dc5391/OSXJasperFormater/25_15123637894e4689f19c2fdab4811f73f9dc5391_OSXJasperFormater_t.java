 package org.opensixen.report;
 
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Currency;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.compiere.model.MBPartner;
 import org.compiere.model.MBPartnerLocation;
 import org.compiere.model.MInOut;
 import org.compiere.model.MInvoice;
 import org.compiere.model.MLocation;
 import org.compiere.model.MOrder;
 import org.compiere.util.Env;
 import org.opensixen.model.MVOpenItem;
 import org.opensixen.osgi.interfaces.IJasperFormater;
 
 public class OSXJasperFormater implements IJasperFormater {
 
 	@Override
 	public String formatQty(Locale locale, BigDecimal qty) {
 		NumberFormat formater = NumberFormat.getInstance(locale);
 		String str = formater.format(qty);
 		return str;
 	}
 
 	@Override
 	public String formatAmt(Locale locale, BigDecimal amount, String isoCode) {
 		NumberFormat formater = NumberFormat.getCurrencyInstance();
 		Currency currency = Currency.getInstance(isoCode);		
 		formater.setCurrency(currency);
 		String str = formater.format(amount);		
 		return str;
 	}
 
 	@Override
 	public String formatDate(Locale locale, Timestamp date) {
 		DateFormat df = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale);
 		String str = df.format(new Date(date.getTime()));
 		return str;
 	}
 
 	@Override
 	public String formatLoc(Integer C_BPartner_Location_ID) {
 		MBPartnerLocation bplocation = new MBPartnerLocation(Env.getCtx(), C_BPartner_Location_ID, null);
 		MLocation location = bplocation.getLocation(false);
 		StringBuffer address = new StringBuffer();
 		address.append(location.toStringCR());
 		String country = location.getCountry(true);
 		if (country != null)	{
 			address.append("\n").append(country);
 		}
 		return address.toString();
 	}
 
 	@Override
 	public String formatLocation(int C_BPartner_ID, int C_Bpartner_Location_ID,
 			boolean addTaxID) {
 		StringBuffer address = new StringBuffer();
 		MBPartner bp = new MBPartner(Env.getCtx(), C_BPartner_ID, null);
 		address.append("<b>").append(bp.getName()).append("</b>\n");
 		if (addTaxID && bp.getTaxID() != null)	{
 			address.append(bp.getTaxID()).append("\n");
 		}
 		address.append(formatLoc(C_Bpartner_Location_ID));
		String str = address.toString();
		return str.replaceAll("\\n", "<br/>");		
 	}
 
 	@Override
 	public String getShipToInOut(Integer M_InOut_ID) {
 		MInOut inout = new MInOut(Env.getCtx(), M_InOut_ID, null);
 		if (inout.isDropShip())	{
 			return formatLocation(inout.getDropShip_BPartner_ID(), inout.getDropShip_Location_ID(), false);
 			
 		}
 		else {
 			return formatLocation(inout.getC_BPartner_ID(), inout.getC_BPartner_Location_ID(), false);
 		}
 	}
 
 	@Override
 	public String getInvoiceToInvoice(Integer C_Invoice_ID) {
 		MInvoice invoice = new MInvoice(Env.getCtx(), C_Invoice_ID, null);
 		return formatLocation(invoice.getC_BPartner_ID(), invoice.getC_BPartner_Location_ID(), true);		
 	}
 
 	@Override
 	public String getInvoiceToOrder(Integer C_Order_ID) {
 		MOrder order = new MOrder(Env.getCtx(), C_Order_ID, null);
 		return formatLocation(order.getC_BPartner_ID(), order.getC_BPartner_Location_ID(), true);
 	}
 
 	@Override
 	public String getShipToOrder(Integer C_Order_ID) {
 		MOrder order = new MOrder(Env.getCtx(), C_Order_ID, null);
 		if (order.isDropShip())	{
 			return formatLocation(order.getDropShip_BPartner_ID(), order.getDropShip_Location_ID(), false);
 		}
 		else {
 			return formatLocation(order.getC_BPartner_ID(), order.getC_BPartner_Location_ID(), false);
 		}
 	}
 
 	@Override
 	public String getPaymentTermInvoice(Locale locale, Integer C_Invoice_ID,
 			String isoCode) {
 		StringBuffer term = new StringBuffer();
 		List<MVOpenItem> items = MVOpenItem.getFromInvoice(Env.getCtx(), C_Invoice_ID);
 		for (MVOpenItem item:items)		{
 			term.append(formatDate(locale, item.getDueDate())).append(": ").append(formatAmt(locale, item.getOpenAmt(), isoCode)).append("\n");
 		}
 		return term.toString();
 	}
 
 	@Override
 	public String getPaymentTermOrder(Locale locale, Integer C_Order_ID,
 			String isoCode) {
 		StringBuffer term = new StringBuffer();
 		List<MVOpenItem> items = MVOpenItem.getFromOrder(Env.getCtx(), C_Order_ID);
 		for (MVOpenItem item:items)		{
 			term.append(formatDate(locale, item.getDueDate())).append(": ").append(formatAmt(locale, item.getOpenAmt(), isoCode)).append("\n");
 		}
 		return term.toString();	
 	}
 
 }
