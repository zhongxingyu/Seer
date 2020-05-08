import java.io.*;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.*;
 import java.util.regex.Pattern;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 import javax.xml.transform.stream.StreamResult;
 import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 
 public class xmlBuilder {
 	static StreamResult streamResult;
     static TransformerHandler handler;
     static AttributesImpl atts;
     static String[] partNumbers = new String[350];
     static String[] EAN = new String[350];
     static String[] qty = new String[350];
     static String[] price = new String[350];
     static String[] elements = new String[10000];
     static String[] priceListPn = new String[450];
     static String[] priceListEan = new String[450];
     static int pnlen, eanlen, pricelistlen= 0;
     static Set<String> hSet= new LinkedHashSet<String>();
     static Set<String> hTempSet= new LinkedHashSet<String>();
     static Set<String> pnSet= new LinkedHashSet<String>();
     static Set<String> eanSet= new LinkedHashSet<String>();
     static Map<String, String> eanMap = new LinkedHashMap<String, String>();
     static Map<String, String> pnMap = new LinkedHashMap<String, String>();
    static File dataXml;
 	
 public static void initXML() throws ParserConfigurationException,
 	TransformerConfigurationException, SAXException {
 		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
         .newInstance();
 	
 		handler = tf.newTransformerHandler();
 		streamResult = new StreamResult("/Users/gustavopinheiro/Desktop/data.xml");
 		Transformer serializer = handler.getTransformer();
 		serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
 		serializer.setOutputProperty(
         "{http://xml.apache.org/xslt}indent-amount", "4");
 		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
 		handler.setResult(streamResult);
 		handler.startDocument();
 		atts = new AttributesImpl();
 		handler.startElement("", "", "Batch", atts);
 }
 
 public static void pnArrayBuilder(String s, String soldTo) {
 	String lSoldTo = soldTo;
 	String searchMe = s;
 	String matc = null;
 	String matcShort = null;
 	int j=0;
 	int p=0;
 	Pattern findMe = Pattern.compile("[mM]{1}"+"\\w{1}"+"[0-9]{3}"+"\\w{2}"+"/"+"[a-zA-Z]{1}");
 	Pattern findMeShort = Pattern.compile("[mM]{1}"+"\\w{1}"+"[0-9]{3}"+"\\w{1}"+"/"+"[a-zA-Z]{1}");
 	Pattern findMeEAN = Pattern.compile("[8]{2}"+"[0-9]{10}");
 	Pattern findMeEAN7 = Pattern.compile("[7][1][8]"+"[0-9]{9}");
 	
 	//reset arrays and sets, except the ones of price list
 			for (int i = 0; i < 200; i++) {
 			partNumbers[i] = null;
 		    EAN[i] = null;
 		    qty[i] = null;
 		    price[i] = null;
 			}
 			for (int i = 0; i < elements.length; i++) {
 		    elements[i] = null;
 			}
 			hSet.clear();
 			hTempSet.clear();
 	
 	for (int i = 0; i< (searchMe.length()-9); i++) {
 		matc = searchMe.substring(i, i+9);
 		matcShort = searchMe.substring(i, i+8);
 		
 		if (findMe.matcher(matc).matches()) {
 			partNumbers[j] = matc;
 			hSet.add(partNumbers[j]);
 			j++;
 		} else if (findMeShort.matcher(matcShort).matches()){
 			partNumbers[j] = matcShort;
 			hSet.add(partNumbers[j]);
 			j++;
 		}
 	}
 	pnlen = hSet.size();
 
 
 	for (int i = 0; i< (searchMe.length()-12); i++) { 
 			matc = searchMe.substring(i, i+12);
 			if (findMeEAN.matcher(matc).matches()) {
 				EAN[p] = searchMe.substring(i, i+12);
 				hTempSet.add(EAN[p]);
 				p++;
 			} else if (findMeEAN7.matcher(matc).matches()) {
 				EAN[p] = searchMe.substring(i, i+12);
 				hTempSet.add(EAN[p]);
 				p++;
 			}
 		} 
 	eanlen = hTempSet.size();
 	
 	buildEanMap();
 	
 	if (!(lSoldTo.equalsIgnoreCase("avnet") || lSoldTo.equalsIgnoreCase("664692") || lSoldTo.equalsIgnoreCase("662803"))) {	//avnet uses EAN and PN only in some SKUs, others have only PN: Officer sometimes gets a "concatenate" error, so it's included here
 		if (eanlen!=0) {
 			hSet.clear(); //clear this set to start from empty with the EAN
 			eanToPn();
 		}
 	}
 	getEAN();
 	
 
 }
 
 public static void buildEanMap(){
 	XSSFEventBasedExcelExtractor extractor = null;
 	String searchMe = null;
 	String path = "/Users/gustavopinheiro/Documents/BRAZIL/Lista Precos/PNxEAN 2012-03-14.xlsx";
 	
 	try {
 		if (!(priceListPn[0] != null)) {   
 	        
 	        extractor = new XSSFEventBasedExcelExtractor(path);        
 	        searchMe = extractor.getText();
 	    	String matc = null;
 	    	String matcShort = null;
 	    	int j=0;
 	    	int p=0;
 	    	int pos2=0;
 	    	Pattern findMe = Pattern.compile("[mM]{1}"+"\\w{1}"+"[0-9]{3}"+"\\w{2}"+"/"+"[a-zA-Z]{1}");
 	    	Pattern findMeShort = Pattern.compile("[mM]{1}"+"\\w{1}"+"[0-9]{3}"+"\\w{1}"+"/"+"[a-zA-Z]{1}");
 	    	Pattern findMeEAN = Pattern.compile("[8]{2}"+"[0-9]{10}");
 	    	Pattern findMeEAN7 = Pattern.compile("[7][1]"+"[0-9]{10}");
 	    	
 	    	for (int i = 0; i< (searchMe.length()-9); i++) {
 	    		matc = searchMe.substring(i, i+9);
 	    		matcShort = searchMe.substring(i, i+8);
 	    		if (findMe.matcher(matc).matches()) {
 	    			priceListPn[j] = matc;
 	    			pnSet.add(priceListPn[j]);
 	    			j++;
 	    		} else if (findMeShort.matcher(matcShort).matches()){
 	    			priceListPn[j] = matcShort;
 	    			pnSet.add(priceListPn[j]);
 	    			j++;
 	    		}
 	    	}
 	    	pricelistlen = pnSet.size();
 	    	System.out.println("pnSet Size = " + pricelistlen);
 	    	
 	    	for (int i = 0; i< (searchMe.length()-12); i++) { 
     			matc = searchMe.substring(i, i+12);
     			if (findMeEAN.matcher(matc).matches()) {
     				priceListEan[p] = matc;
     				eanSet.add(priceListEan[p]);
     				p++;
     			} else if (findMeEAN7.matcher(matc).matches()){
     				priceListEan[p] = matc;
     				eanSet.add(priceListEan[p]);
         			p++;
         		}
     		}
 	    	while (pos2 < pricelistlen) {
 	    		eanMap.put(priceListEan[pos2], priceListPn[pos2]);
 	    		pnMap.put(priceListPn[pos2], priceListEan[pos2]);
 	    		pos2++;
 	    	}
 	    	
 	    	System.out.println("eanSet Size = " + eanSet.size());
 	    	System.out.println("eanMap size = " + eanMap.size());
 	}
 	
 	}  catch (Exception e) {
 		System.err.format("Exception in eanToPn: %s%n", e);
 	}
 }
 
 public static void eanToPn () {
 
     	for (int pos = 0; pos < eanlen; pos++) {
     		hSet.add(eanMap.get(EAN[pos]));
     	}
     	
 }
 
 public static void getEAN() {
 	hTempSet.clear();
 	for (String searchPnInMap : hSet) {
 		hTempSet.add(pnMap.get(searchPnInMap));
 	}
 }
 
 public static void qtyArrayBuilder(String s, String order, String totalAmount, String soldTo) {
 	elements = s.split("\\s+");
 	int j = 0;
 	int i = 0;
 	int k = 0;
 	int firstIndex, lastIndex;
 		
 	List <Double> allNumbers = new ArrayList<Double>();
 	 
 	NumberFormat nFEng = NumberFormat.getInstance(Locale.ENGLISH); //ACOM uses US/UK format> On 03/12 ACOM not using US format anymore
 	NumberFormat nFGer = NumberFormat.getInstance(Locale.GERMANY); //Germany uses same currency format as Brazil t.hhh,dd
 	
 	
 	Object temp = null;
 	List<Double> qtySol = new ArrayList<Double>();
 	List<Double> priceSol = new ArrayList<Double>();
 	List<ArrayList<Double>> qtyPriceSol = new ArrayList<ArrayList<Double>>();
 	
 	
 	//code under development starts here
 	//try to start after the first ean or mc element to filter all scrap numbers out, exception is WMS which has to start in 0 due to bad formated PDF
 	//for everybody (not only avnet), stop at last ean or mc + 25
 	firstIndex = 0;
 	if(!(soldTo.equalsIgnoreCase("wms") || soldTo.equalsIgnoreCase("664684") || soldTo.equalsIgnoreCase("magalu") || soldTo.equalsIgnoreCase("725179") || soldTo.equalsIgnoreCase("664689") || soldTo.equalsIgnoreCase("753252"))) {	//wms and walmart 664689 files have the qty before the pn, so it needs to get all number from beginning
 		firstIndex = findFirstIndex(elements);
 	}
 	lastIndex = elements.length;
 	if(!(soldTo.equalsIgnoreCase("avnet") || soldTo.equalsIgnoreCase("664692") || soldTo.equalsIgnoreCase("magalu") || soldTo.equalsIgnoreCase("725179") || soldTo.equalsIgnoreCase("664689") || soldTo.equalsIgnoreCase("753252"))) {	//avnet  and wms files have the qty in the end
 		lastIndex = findLastIndex(elements);
 	}
 		for (i = firstIndex; i<lastIndex; i++) {
 			try {
 				try {
 					if(soldTo.equalsIgnoreCase("avnetLA") ) { //ACOM 664711  using English format, NOT ANYMORE (04/20/2012), AGAIN 0n 05/04, NOT ANYMORE (05/16), AGAIN (06/06), NOT ANYMORE (06/12), AGAIN (06/19)
 						temp = nFEng.parse(elements[i]);
 					} else {
 						temp = nFGer.parse(elements[i]);
 					}
 					
 					if ( temp.getClass().getName() == "java.lang.Double" && !temp.equals (Double.valueOf(0.0))) {
 						if (!((soldTo.equalsIgnoreCase("fnac") || soldTo.equalsIgnoreCase("664715")) && (Double)temp == 210.0)) {
 							allNumbers.add((Double) temp);
 						}
 					} else if ( temp.getClass().getName() == "java.lang.Long" && !temp.equals(Long.valueOf(0))) {
 						if (!((soldTo.equalsIgnoreCase("fnac") || soldTo.equalsIgnoreCase("664715")) && (Long)temp == 210)) {
 							allNumbers.add(Double.valueOf((temp).toString()));
 						}
 					} 
 				} catch (ParseException e1) {
 					//do nothing
 				} catch (ClassCastException e) {
 					//do nothing
 				}
 			} catch (NumberFormatException e) {
 			//	System.err.print(e);
 			}
 			
 		}
 		System.out.println(allNumbers);
 		
 		try {
 			if (hSet.size()>0) {
 				qtyPriceSol = EvoAlgor.main(hSet.size(), allNumbers.toArray(), totalAmount, soldTo);
 			
 				qtySol = qtyPriceSol.get(0);
 				priceSol = qtyPriceSol.get(1);
 			} else {
 				System.out.println("No Valid SKU found on PO " + order + "\nAborting");
 				OEMultiT.noValidSkuMessage(order);
 			}
 		} catch (IOException e) {
 			System.err.format("Exception in xmlBuilder: %s%n", e);
 			if ((e.toString().contains("NoSuchElement"))) {
 				System.out.println("No Solution Found");
 			}
 		}
 		if (!qtySol.isEmpty()) {
 			for (double qty2: qtySol) {
 				qty[j] = Integer.toString((new Double(qty2)).intValue());
 				j++;
 			}
 			for (double price2: priceSol) {
 				price[k] = Double.toString((new Double(price2)));
 				k++;
 			}
 		} 
 }
 
 public static int findFirstIndex (String[] elements) {
 	int firstIndex = 0;
 	Pattern findMe = Pattern.compile("[mM]{1}"+"\\w{1}"+"[0-9]{3}"+"\\w{2}"+"/"+"[a-zA-Z]{1}");
 	Pattern findMeShort = Pattern.compile("[mM]{1}"+"\\w{1}"+"[0-9]{3}"+"\\w{1}"+"/"+"[a-zA-Z]{1}");
 	Pattern findMeEAN = Pattern.compile("[8][8]"+"[0-9]{10}");
 	Pattern findMeEAN0 = Pattern.compile("[0][8][8]"+"[0-9]{10}");
 	Pattern findMeEAN7 = Pattern.compile("[7][1][8]"+"[0-9]{9}");
 	Pattern findMeEAN07 = Pattern.compile("[0][7][1][8]"+"[0-9]{9}");
 	
 	for (int i = 0; i < elements.length; i++) {
 		if (findMe.matcher(elements[i]).matches() 
 			|| findMeShort.matcher(elements[i]).matches()
 			|| findMeEAN.matcher(elements[i]).matches()
 			|| findMeEAN0.matcher(elements[i]).matches()
 			|| findMeEAN7.matcher(elements[i]).matches()
 			|| findMeEAN07.matcher(elements[i]).matches()) {
 			if (i-25 >= 0){
 				firstIndex = i - 25;
 			} else {
 				//firstIndex has already been initialized as = 0
 			}
 			break;
 		}
 	}
 	return firstIndex;
 }
 
 public static int findLastIndex (String[] elements) {
 	int lastIndex = elements.length;
 	Pattern findMe = Pattern.compile("[mM]{1}"+"\\w{1}"+"[0-9]{3}"+"\\w{2}"+"/"+"[a-zA-Z]{1}");
 	Pattern findMeShort = Pattern.compile("[mM]{1}"+"\\w{1}"+"[0-9]{3}"+"\\w{1}"+"/"+"[a-zA-Z]{1}");
 	Pattern findMeEAN = Pattern.compile("[8][8]"+"[0-9]{10}");
 	Pattern findMeEAN0 = Pattern.compile("[0][8][8]"+"[0-9]{10}");
 	Pattern findMeEAN7 = Pattern.compile("[7][1][8]"+"[0-9]{9}");
 	Pattern findMeEAN07 = Pattern.compile("[0][7][1][8]"+"[0-9]{9}");
 	
 	for (int i = elements.length - 1; i >= 0; i--) {
 		if (findMe.matcher(elements[i]).matches() 
 			|| findMeShort.matcher(elements[i]).matches()
 			|| findMeEAN.matcher(elements[i]).matches()
 			|| findMeEAN0.matcher(elements[i]).matches()
 			|| findMeEAN7.matcher(elements[i]).matches()
 			|| findMeEAN07.matcher(elements[i]).matches()) {
 			if (i+40 < elements.length){
 				lastIndex = i + 40;
 			} else {
 				//lastIndex has already been initialized as elements.length
 			}
 			break;
 		}
 	}
 	
 	return lastIndex;
 }
 
 public static void elementBuilder(String soldTo, String shipTo, String pO) throws SAXException {
 	int k = 0;
 	
 		atts.clear();
 		handler.startElement("", "", "Order", atts);
 		handler.startElement("", "", "SoldTo", atts);
 		handler.characters(soldTo.toCharArray(), 0, soldTo.length());
 		handler.endElement("","", "SoldTo");
 		handler.startElement("", "", "ShipTo", atts);
 		handler.characters(shipTo.toCharArray(), 0, shipTo.length());
 		handler.endElement("", "", "ShipTo");
 		handler.startElement("", "", "PO", atts);
 		handler.characters(pO.toCharArray(), 0, pO.length());
 		handler.endElement("","", "PO");
 		
 		for (String pnString : hSet) {
 			atts.clear();
 			handler.startElement("", "", "Item", atts);
 			handler.startElement("", "", "PartNumber", atts);
 			handler.characters(pnString.toCharArray(), 0, pnString.length());
 			handler.endElement("", "", "PartNumber");
 			handler.startElement("", "", "EAN", atts);
 			handler.characters(((String) hTempSet.toArray()[k]).toCharArray(), 0, ((String) hTempSet.toArray()[k]).length());
 			handler.endElement("", "", "EAN");
 			handler.startElement("", "", "Qty", atts);
 			handler.characters(qty[k].toCharArray(), 0, qty[k].length());
 			handler.endElement("", "", "Qty");
 			handler.startElement("", "", "Price", atts);
 			handler.characters(price[k].toCharArray(), 0, price[k].length());
 			handler.endElement("", "", "Price");
 			handler.endElement("", "", "Item");
 			k++;
 			}
 		handler.endElement("", "", "Order");
 	}
 
 public static void closeXML() throws SAXException {
 	handler.endElement("", "", "Batch");
 	handler.endDocument();
 	xlsxBuilder.main();
 	csvBuilder.main();
 	OEMultiT.doneMessage();
 }
 
 }
