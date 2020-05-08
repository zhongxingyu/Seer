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
 		
 		try {
 			// TODO config: maxJourneys
 			// TODO config: input (origin)
 			// TODO config: min dep time
 			rows = this.extractTableRows(Web.loadDoc("http://www.vbb-fahrinfo.de/hafas/stboard.exe/dn?boardOverview=yes&input=9230999&boardType=dep&maxJourneys=40&productsFilter=11111111&start=yes&pageViewMode=PRINT&dirInput=&"));
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
 	
 		// remove header
 		rows.remove(0);
 		
 		for (Element row : rows) {
			Transport transport = this.extractTransport(row);
			if (transport != null) transportList.add(transport);
 		}			
 			
 		return transportList;
 	}
 	
 	private Elements extractTableRows(Document doc) {
 		Elements table = doc.select("table.resultTable > tbody > tr");
 		return table;
 	}
 	
 	private Transport extractTransport(Element row) {
 		Elements cells = row.select("td");
		Transport transport = null;
		
		if (cells.size() == 4) {
			transport = new Transport(
				cells.get(0).text(),
				cells.get(2).select("strong").text(),
				cells.get(1).text(),
				cells.get(3).text()
			);
		} else if (cells.size() == 5) {
			transport = new Transport(
				cells.get(0).text(),
				cells.get(3).select("strong").text(),
				cells.get(2).text(),
				cells.get(4).text()
			);
		}
 		
 		return transport;
 	}
 }
