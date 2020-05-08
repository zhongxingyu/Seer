 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package printer.printables;
 
 import database.extra.InvoiceItem;
 import database.tables.Invoice;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import printer.adts.PrintableHorizontalLineObject;
 import printer.adts.PrintableLine;
 import printer.adts.PrintableMulti;
 import printer.adts.PrintableNewline;
 import printer.adts.PrintableString;
 
 /**
  *
  * @author Warkst
  */
 public class PrintableInvoiceNew extends MyPrintable{
 
     /*
      * Data model.
      */
     private final Invoice model;
     
     public PrintableInvoiceNew(Invoice model) {
 	super(new Font("Serif", Font.PLAIN, 12));
 	this.model = model;
     }
     
     @Override
     public List<PrintableHorizontalLineObject> transformBody(int width, int margin, FontMetrics fontMetrics) {
 	/*
 	 * Formatting variables
 	 */
 	int half = width/2;
 	width-=10; // small correction...
 	half-=5;
 	int[] tabs = new int[]{0, half, 3*width/5, 4*width/5, width};
 	
 	/*
 	 * Incremental print model
 	 */
 	List<PrintableHorizontalLineObject> printModel = new ArrayList<PrintableHorizontalLineObject>();
 	
 	/*
 	 * Actual transformation
 	 * Start by printing header
 	 */
 	printModel.add(new PrintableLine(half, width));
 	printModel.add(new PrintableNewline());
 	
 	ArrayList<PrintableHorizontalLineObject> header = new ArrayList<PrintableHorizontalLineObject>();
 	
 	header.add(new PrintableString("FACTUUR: "+model.getNumber(), half+3));
 	String dateString = "DATUM: "+model.getDate();
 	int dateAnchor = width - fontMetrics.charsWidth(dateString.toCharArray(), 0, dateString.length());
 	header.add(new PrintableString(dateString, dateAnchor-15));
 	
 	printModel.add(new PrintableMulti(header));
 	
 	printModel.add(new PrintableLine(half, width));
 	
 	/*
 	 * Print client information
 	 */
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	
 	printModel.add(new PrintableString(model.getClient().getContact(), half+3));
 	
 	printModel.add(new PrintableNewline());
 	
 	printModel.add(new PrintableString(model.getClient().getAddress(), half+3));
 	
 	printModel.add(new PrintableNewline());
 	
 	printModel.add(new PrintableString(model.getClient().getZipcode()+" "+model.getClient().getMunicipality(), half+3));
 	
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableString(model.getClient().getTaxnumber(), half+3));
 	
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	
 	/*
 	 * Print article header
 	 */
 	printModel.add(new PrintableLine(0, width));
 	printModel.add(new PrintableNewline());
 	ArrayList<PrintableHorizontalLineObject> group = new ArrayList<PrintableHorizontalLineObject>();
 	group.add(new PrintableString("Omschrijving artikels", tabs[0]));
 	group.add(new PrintableString("BTW", tabs[1]));
 	group.add(new PrintableString("Hoeveelheid", tabs[2]));
 	group.add(new PrintableString("Prijs", tabs[3]));
 	group.add(new PrintableString("Totaal", tabs[4]-fontMetrics.charsWidth("Totaal".toCharArray(), 0, "Totaal".length())));
 	printModel.add(new PrintableMulti(group));
 	printModel.add(new PrintableLine(0, width));
 	printModel.add(new PrintableNewline());
 	
 	/*
 	 * Print invoice articles (InvoiceItems)
 	 */
 	DecimalFormat threeFormatter = new DecimalFormat("0.000");
 	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
 	otherSymbols.setDecimalSeparator(',');
 	otherSymbols.setGroupingSeparator('.'); 
 	threeFormatter.setDecimalFormatSymbols(otherSymbols);
 	
 	for (InvoiceItem invoiceItem : model.items()) {
 	    /*
 	     * Group per savings?
 	     */
 	    ArrayList<PrintableHorizontalLineObject> ii = new ArrayList<PrintableHorizontalLineObject>();
 	    ii.add(new PrintableString(invoiceItem.getArticle().getName(), tabs[0]));
 	    ii.add(new PrintableString(""+(int)(invoiceItem.getArticle().getTaxes()), tabs[1]));
 	    ii.add(new PrintableString(threeFormatter.format(invoiceItem.getAmount())+" "+invoiceItem.getArticle().getUnit(), tabs[2]));
 	    ii.add(new PrintableString(threeFormatter.format(invoiceItem.getArticle().getPriceForCode(model.getPriceCode())), tabs[3]));
 	    
 	    String tot = threeFormatter.format(invoiceItem.getArticle().getPriceForCode(model.getPriceCode())*invoiceItem.getAmount());
 	    
 	    ii.add(new PrintableString(tot, tabs[4]-fontMetrics.charsWidth(tot.toCharArray(), 0, tot.length())));
 	    printModel.add(new PrintableMulti(ii));
 	    
 	    printModel.add(new PrintableNewline());
 	}
 	
 	return printModel;
     }
 
     @Override
     public List<PrintableHorizontalLineObject> transformFooter(int width, int margin, FontMetrics fontMetrics) {
 	width-=10;
 	
 	/*
 	 * Incremental print model
 	 */
 	List<PrintableHorizontalLineObject> printModel = new ArrayList<PrintableHorizontalLineObject>();
 	
 	/*
 	 * Formatting variables
 	 */
 //	int[] tabs = new int[]{0};
 	
 	/*
 	 * Actual transformation
 	 * Print savings at bottom: set y from bottom upwards
 	 * Group the invoice items per tax value
 	 */
 	
 	Map<Double, List<InvoiceItem>> categories = new HashMap<Double, List<InvoiceItem>>();
 	
 	for (InvoiceItem invoiceItem : model.items()) {
 	    if (categories.containsKey(invoiceItem.getArticle().getTaxes())) {
 		categories.get(invoiceItem.getArticle().getTaxes()).add(invoiceItem);
 	    } else {
 		List<InvoiceItem> items = new ArrayList<InvoiceItem>();
 		items.add(invoiceItem);
 		categories.put(invoiceItem.getArticle().getTaxes(), items);
 	    }
 	}
 	
 	int[] tabs = new int[categories.size()+3];
 	tabs[0] = margin;
 	int base = 30;
 	for (int i = 0; i < categories.size(); i++) {
	    tabs[i+1] = margin + 60*(i+1);
 	}
 	tabs[tabs.length-2] = 4*width/5;
 	tabs[tabs.length-1] = width;
 	
 	if (model.getSave()>0) {
 	    
 	    printModel.add(0, new PrintableNewline());
 	    
 	    /*
 	     * Print savings
 	     */
 	}
 	
 	int threeZeroesWidth = fontMetrics.charsWidth("000".toCharArray(), 0 , 3);
 	
 	printModel.add(new PrintableLine(0, width));
 	printModel.add(new PrintableNewline());
 	ArrayList<PrintableHorizontalLineObject> savingsCategories = new ArrayList<PrintableHorizontalLineObject>();
 	savingsCategories.add(new PrintableString("BTW %", 0));
 	int index = 1;
 	for (Double savings : categories.keySet()) {
 	    String printMe = savings+" %";
 	    savingsCategories.add(new PrintableString(printMe, tabs[index]+threeZeroesWidth-fontMetrics.charsWidth(printMe.toCharArray(), 0, printMe.length()-4)));
 	    index++;
 	}
 	printModel.add(new PrintableMulti(savingsCategories));
 	
 	printModel.add(new PrintableNewline());
 	ArrayList<PrintableHorizontalLineObject> prices = new ArrayList<PrintableHorizontalLineObject>();
 	prices.add(new PrintableString("Excl.", 0));
 	index = 1;
 	double pricesTot = 0.0;
 	for (List<InvoiceItem> list : categories.values()) {
 	    double price = 0;
 	    for (InvoiceItem invoiceItem : list) {
 		price += invoiceItem.getArticle().getPriceForCode(model.getPriceCode())*invoiceItem.getAmount();
 	    }
 	    pricesTot+= price;
 	    String pr = new DecimalFormat("0.000").format(price);
 	    prices.add(new PrintableString(pr, tabs[index]+threeZeroesWidth-fontMetrics.charsWidth(pr.toCharArray(), 0, pr.length()-4)));
 //	    prices.add(new PrintableString(pr, tabs[index]));
 	    index++;
 	}
 	prices.add(new PrintableString("Tot. Excl.", tabs[tabs.length-2]));
 	String prTot = new DecimalFormat("0.000").format(pricesTot);
 	prices.add(new PrintableString(prTot, tabs[tabs.length-1]-fontMetrics.charsWidth(prTot.toCharArray(), 0, prTot.length())));
 	printModel.add(new PrintableMulti(prices));
 	
 	printModel.add(new PrintableNewline());
 	ArrayList<PrintableHorizontalLineObject> taxes = new ArrayList<PrintableHorizontalLineObject>();
 	taxes.add(new PrintableString("BTW", 0));
 	index = 1;
 	double taxesTot = 0.0;
 	for (Map.Entry<Double, List<InvoiceItem>> entry : categories.entrySet()) {
 	    double price = 0;
 	    for (InvoiceItem invoiceItem : entry.getValue()) {
 		price += invoiceItem.getArticle().getPriceForCode(model.getPriceCode())*invoiceItem.getAmount();
 	    }
 	    double tax = price * entry.getKey()/100;
 	    taxesTot+= tax;
 	    String t = new DecimalFormat("0.000").format(tax);
 	    taxes.add(new PrintableString(t, tabs[index]+threeZeroesWidth-fontMetrics.charsWidth(t.toCharArray(), 0, t.length()-4)));
 //	    taxes.add(new PrintableString(t, tabs[index]));
 	    index++;
 	}
 	taxes.add(new PrintableString("BTW", tabs[tabs.length-2]));
 	String tTot = new DecimalFormat("0.000").format(taxesTot);
 	taxes.add(new PrintableString(tTot, tabs[tabs.length-1]-fontMetrics.charsWidth(tTot.toCharArray(), 0, tTot.length())));
 	printModel.add(new PrintableMulti(taxes));
 	
 	printModel.add(new PrintableLine(0, width));
 	printModel.add(new PrintableNewline());
 	ArrayList<PrintableHorizontalLineObject> totals = new ArrayList<PrintableHorizontalLineObject>();
 	totals.add(new PrintableString("Totaal", 0));
 	double total = 0.0;
 	index = 1;
 	for (Map.Entry<Double, List<InvoiceItem>> entry : categories.entrySet()) {
 	    double price = 0;
 	    for (InvoiceItem invoiceItem : entry.getValue()) {
 		price += invoiceItem.getArticle().getPriceForCode(model.getPriceCode())*invoiceItem.getAmount();
 	    }
 	    double tot = price * (1.0+entry.getKey()/100);
 	    total+=tot;
 	    String t = new DecimalFormat("0.00").format(tot);
 	    totals.add(new PrintableString(t, tabs[index]+threeZeroesWidth-fontMetrics.charsWidth((t+"0").toCharArray(), 0, t.length()-3)));
 //	    totals.add(new PrintableString(t, tabs[index]));
 	    index++;
 	}
 	totals.add(new PrintableString("TOTAAL", tabs[tabs.length-2]));
 	String tot = new DecimalFormat("0.00").format(total);
 	totals.add(new PrintableString(tot, tabs[tabs.length-1]-fontMetrics.charsWidth((tot+"0").toCharArray(), 0, tot.length()+1)));
 	printModel.add(new PrintableMulti(totals));
 	
 	printModel.add(new PrintableLine(0, width));
 	
 	
 	/*
 	 * Keep 7 white lines from the bottom
 	 */
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	
 	printModel.add(new PrintableNewline());
 	printModel.add(new PrintableNewline());
 	
 	return printModel;
     }
 }
