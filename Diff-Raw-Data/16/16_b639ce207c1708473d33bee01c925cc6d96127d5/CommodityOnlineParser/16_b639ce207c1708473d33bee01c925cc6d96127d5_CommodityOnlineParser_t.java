 package com.agsupport.parser.derivative;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.parser.Tag;
 
 import com.agsupport.core.jpa.model.DerivativeValue;
 
 public class CommodityOnlineParser extends DerivativeParser {
 	public CommodityOnlineParser(String url, String name) {
 		super(url);
 		setStockMarketName(name);
 	}
 
 	@Override
 	protected void getAllValuesRows() {
 		// Tworz nowy tag div, eby wyodrbni kolejny wiersz (strona zawiera w caej tabelce zagniedzone na jednym poziomie tagi li)
 		
 		Element ul = getDocument().getElementsByClass("mcxlisting2").get(1).child(0);
 			
 		for(int i = 0; i < ul.children().size(); i += 4) {
 			Element div = new Element(Tag.valueOf("div"), "");
			String expiryDate = ul.child(i).text();
			Boolean unique = true;
 			
 			div.appendChild(ul.child(i).clone());
 			div.appendChild(ul.child(i + 1).clone());
 			
			for(Element index : indexes){
				if(index.child(0).text().equals(expiryDate)) {
					System.out.println("Yes");
					unique = false;
				}
			}
			
			if(unique)
				indexes.add(div);
 		}
 	}
 
 	@Override
 	protected DerivativeValue getValue(Element index) {
 		DerivativeValue derValue = new DerivativeValue();
 		
 		derValue.setDateOfAdd(new Date());
 		try {
 			derValue.setExpiredDate(parseDate(index.child(0).text()));
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		derValue.setPrice(parsePrice(index.child(1).text()));
 		
 		return derValue;
 	}
 	
 	private Date parseDate(String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
 		
 		return sdf.parse(date);
 	}
 	
 	private Double parsePrice(String price) {		
 		//price = price.replaceAll("\\(.*\\)", "");
 				
 		return Double.parseDouble(price);
 	}
 }
