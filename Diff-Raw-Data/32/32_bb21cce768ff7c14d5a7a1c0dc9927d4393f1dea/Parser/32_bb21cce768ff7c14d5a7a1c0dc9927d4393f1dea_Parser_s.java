 package application;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.xml.sax.SAXException;
 
 public class Parser {
 	public List<Transport> loadTimeTable() {
 		Elements rows = null;
 		List<Transport> transportList = new LinkedList<Transport>();
 		
 		Date now = new Date();
 		String nowString = new SimpleDateFormat("HH:mm").format(now);
 		
 		try {
 			rows = this.extractTableRows(Web.loadDoc("http://www.fahrinfo-berlin.de/Fahrinfo/bin/stboard.bin/dn?input=9230005&boardType=dep&time=" + nowString + "&maxJourneys=20&dateBegin=&dateEnd=&selectDate=&productsFilter=0&start=yes&dirInput=&"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		for (Element row : rows) {
 			transportList.add(this.extractTransport(row));
 		}			
 			
 		return transportList;
 	}
 	
 	private Elements extractTableRows(Document doc) {
 		Elements table = doc.getElementById("ivuContent").select("table.ivuTable").first().select("tbody").first().select("tr");
 		return table;
 	}
 	
 	private Transport extractTransport(Element row) {
 		Elements cells = row.select("td");
 		Transport transport = new Transport(
 			cells.get(0).text(),
			cells.get(1).text(),
			cells.get(2).select("span.bold > a").text()
 		);
 		
 		return transport;
 	}
 }
