 package PizzaPck;
 
 
 import java.text.DecimalFormat;
 
 public class PrintReceipt extends TheReceipt{
 	//private DB db;
 	private float delivery_price;
 	private float tot;
 	private double tot_u_mva;
 	private double mva;
 	private double final_price;
 	private int orderNr;
 	
 	private DecimalFormat format = new DecimalFormat("0.00");
 	
 	public PrintReceipt(/*DB db*/){
 		super();
 		//this.db = db;
 		super.setWindowTitle("Kvittering");
 		
 		print();
 	}
 	
 	//må skrive noe HTML-kode eller noe for å formatere utskriften
 	public void print(){
 		
 		this.textbox.append("<b>Ordre nr : "+orderNr+"</b>");
 		this.textbox.append("Skal være ferdig til : "/*orderID*/+1600);
 		this.textbox.append("Skal leveres "+"\n");//må hente ut om den skal leveres eller ikke
 		
 		this.textbox.append("<table>" +
 				"<tr>" +
 				"<td width=220><strong>Pizza</strong></td>" +
 				"<td><strong>Pris</strong></td>" +
 				"</tr>" +
 				"<tr>" +
				"<td colspan=2><hr align=\"left\" : width =\"305\" /></td>" +
 				"</tr>");
 
 		//Her må ordrene legges inn slik at de dukker opp på kviteringen
 		/*while(){*/
 			this.textbox.append("<tr style=\"font-size:10px\">"+ 
 			"<td width=220>"+ "Kebab Pizza" + " x "+ 2+"</td>"+
 			"<td>"+ format.format(150)+"</td>"+
 			"</tr>");
 			
 		/*}*/
 		
 		tot_u_mva = (tot)/(1.25);
 		mva = (tot-tot_u_mva);
 		final_price=tot+delivery_price;
 		
 		this.textbox.append("<tr>"+
				"<td colspan=2><hr align=\"left\" : width =\"300\" /></td>" +
 				"</tr>" +
 				"<p style=\"width: 20px;\">"+
 				"<strong>Bestillingsinfo</strong></p>" +
 				"<table style=\"font-size: 9px;\">" +
 				"<tr>" +
 				"<td width=\"220\">" +
 				"	Leveringspris:</td>" +
 				"<td> " + delivery_price + "</td>" +
 				"</tr>" +
 
 				"<tr>" +
 				"<td width=\"220\">Totalpris med mva:</td>" + 
 				"<td>" + format.format(tot) + "</td>" +
 				"</tr>"+
 
 				"<tr>" +
 				"<td width=\"220\">Totalpris uten mva:</td>" +
 				"<td>" + format.format(tot_u_mva) + "</td>" +
 				"</tr>" +
 
 				"<tr>" +
 				"<td width=\"220\">Merverdiavgift:</td>" +
 				"<td>" + format.format(mva) + "</td>" +
 				"</tr>" +
 				
 				"<tr>"+
				"<td colspan=2><hr align=\"left\" : width =\"300\" />"+
				"</td>" +
 				"</tr>" +
 
 				"<tr>" +
 				"<td style=\"font-size:11px\":width=\"220\"><b>Total pris:</b></td>" +
 				"<td style=\"font-size:11px\"><b>" + format.format(final_price) + "</b></td>" +
 				"</tr>" +
 		"</table>");
 		
 	}
 
 	public void setDelivery_price(float delivery_price) {
 		this.delivery_price = delivery_price;
 	}
 
 	public void setOrderNr(int orderNr) {
 		this.orderNr = orderNr;
 	}
 	
 	
 }
 
