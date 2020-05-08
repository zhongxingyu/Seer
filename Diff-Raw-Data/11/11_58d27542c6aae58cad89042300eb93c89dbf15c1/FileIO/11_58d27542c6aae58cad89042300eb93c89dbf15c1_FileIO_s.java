 package edu.brown.cs32.atian.crassus.file;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import edu.brown.cs32.atian.crassus.backend.DataSourceType;
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockFreqType;
 import edu.brown.cs32.atian.crassus.backend.StockImpl;
 import edu.brown.cs32.atian.crassus.backend.StockList;
 import edu.brown.cs32.atian.crassus.backend.StockListImpl;
 import edu.brown.cs32.atian.crassus.backend.StockTimeFrameData;
 import edu.brown.cs32.atian.crassus.indicators.BollingerBands;
 import edu.brown.cs32.atian.crassus.indicators.Indicator;
 import edu.brown.cs32.atian.crassus.indicators.MACD;
 import edu.brown.cs32.atian.crassus.indicators.PivotPoints;
 import edu.brown.cs32.atian.crassus.indicators.PriceChannel;
 import edu.brown.cs32.atian.crassus.indicators.RSI;
 import edu.brown.cs32.atian.crassus.indicators.StochasticOscillator;
 
 public class FileIO {
 	
	public final static DataSourceType DATA_SOURCE_TYPE = DataSourceType.YAHOOFINANCE;
 
 	private void writeIndicator(Document doc, Element parent, Indicator indicator) {
 		
 		Element element = doc.createElement("indicator");
 		
 		String visible = Boolean.toString(indicator.getVisible());
 		element.setAttribute("visible", visible);
 		
 		String active = Boolean.toString(indicator.getActive());
 		element.setAttribute("active", active);
 		
 		if(indicator instanceof BollingerBands){
 			
 			BollingerBands bb = (BollingerBands) indicator;
 			element.setAttribute("implementation", "BollingerBands");
 			
 			String period = Integer.toString(bb.getPeriod());
 			element.setAttribute("period", period);
 			
 			String bandWidth = Integer.toString(bb.getBandWidth());
 			element.setAttribute("bandWidth", bandWidth);
 			
 		}
 		else if(indicator instanceof MACD){
 			
 			MACD macd = (MACD) indicator;
 			element.setAttribute("implementation", "MACD");
 			
 			String shortPeriod = Integer.toString(macd.getShortPeriod());
 			element.setAttribute("shortPeriod", shortPeriod);
 			
 			String signalPeriod = Integer.toString(macd.getSignalPeriod());
 			element.setAttribute("signalPeriod", signalPeriod);
 			
 			String longPeriod = Integer.toString(macd.getLongPeriod());
 			element.setAttribute("longPeriod",longPeriod);
 			
 		}
 		else if(indicator instanceof PivotPoints){
 			
 			PivotPoints pp = (PivotPoints) indicator;
 			element.setAttribute("implementation", "PivotPoints");
 			
 			String pivotOption = pp.getPivotOption();
 			element.setAttribute("pivotOption", pivotOption);
 			
 		}
 		else if(indicator instanceof PriceChannel){
 			
 			PriceChannel pc = (PriceChannel) indicator;
 			element.setAttribute("implementation", "PriceChannel");
 			
 			String lookBackPeriod = Integer.toString(pc.getLookBackPeriod());
 			element.setAttribute("lookBackPeriod", lookBackPeriod);
 			
 		}
 		else if(indicator instanceof RSI){
 			
 			RSI rsi = (RSI) indicator;
 			element.setAttribute("implementation", "RSI");
 			
 			String period = Integer.toString(rsi.getPeriod());
 			element.setAttribute("period", period);
 			
 		}
 		else if(indicator instanceof StochasticOscillator){
 			
 			StochasticOscillator so = (StochasticOscillator) indicator;
 			element.setAttribute("implementation", "StochasticOscillator");
 			
 			String period = Integer.toString(so.getPeriod());
 			element.setAttribute("period", period);
 			
 		}
 		else{
 			return;
 		}
 		
 		parent.appendChild(element);
 		
 	}
 	
 	private Indicator readIndicator(List<StockTimeFrameData> data, Element element) {
 		
 		//Element elIndicator = doc.createElement("indicator");
 		
 		boolean visible = Boolean.parseBoolean(element.getAttribute("visible"));
 		boolean active = Boolean.parseBoolean(element.getAttribute("active"));
 		
 		Indicator indicator;
 		String implementation = element.getAttribute("implementation");
 		
 		if("BollingerBands".equals(implementation)){
 			
 			int period = Integer.parseInt(element.getAttribute("period"));
 			int bandWidth = Integer.parseInt(element.getAttribute("bandWidth"));
 			
 			indicator = new BollingerBands(data, period, bandWidth);
 			
 		}
 		else if("MACD".equals(implementation)){
 			
 			int shortPeriod = Integer.parseInt(element.getAttribute("shortPeriod"));
 			int signalPeriod = Integer.parseInt(element.getAttribute("signalPeriod"));
 			int longPeriod = Integer.parseInt(element.getAttribute("longPeriod"));
 			
 			indicator = new MACD(data, signalPeriod, shortPeriod, longPeriod);
 			
 		}
 		else if("PivotPoints".equals(implementation)){
 			
 			String pivotOption = element.getAttribute("pivotOption");
 			
 			indicator = new PivotPoints(data, pivotOption);
 			
 		}
 		else if("PriceChannel".equals(implementation)){
 			
 			int lookBackPeriod = Integer.parseInt(element.getAttribute("lookBackPeriod"));
 			
 			indicator = new PriceChannel(data, lookBackPeriod);
 			
 		}
 		else if("RSI".equals(implementation)){
 			
 			int period = Integer.parseInt(element.getAttribute("period"));
 			
 			indicator = new RSI(data,period);
 			
 		}
 		else if("StochasticOscillator".equals(implementation)){
 			
 			int period = Integer.parseInt(element.getAttribute("period"));
 			
 			indicator = new StochasticOscillator(data, period);
 			
 		}
 		else{
 			return null;
 		}
 		
 		indicator.setActive(active);
 		indicator.setVisible(visible);
 		return indicator;
 		
 	}
 	
 	private void writeStock(Document doc, Element parent, Stock stock){
 		
 		Element element = doc.createElement("stock");
 		element.setAttribute("ticker",stock.getTicker());
 		
 		for(Indicator indicator: stock.getEventList()){
 			writeIndicator(doc, element, indicator);
 		}
 		
 		parent.appendChild(element);
 		
 	}
 	
 	private Stock readStock(Element element) {
 		
 		String ticker = element.getAttribute("ticker");
 		Stock stock = new StockImpl(ticker);
 		
 		NodeList nList = element.getChildNodes();
 		
 		for(int i=0; i<nList.getLength(); i++){
 			
 			Node node = nList.item(i);
 			
 			if(node.getNodeType() == Node.ELEMENT_NODE && "indicator".equals(node.getNodeName())){
 				
 				Element child = (Element) node;
 				Indicator indicator = readIndicator(stock.getStockPriceData(stock.getCurrFreq()),child);
 				stock.addEvent(indicator);
 				
 			}
 			
 		}
 		
 		return stock;
 		
 	}
 	
 	public boolean write(File f, StockList stocks) throws ParserConfigurationException, FileNotFoundException, TransformerException {
 		
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 
 		Document doc = db.newDocument();
 
 		Element root = doc.createElement("root");
 
 		for(Stock stock: stocks.getStockList()){
 			writeStock(doc,root,stock);
 		}
 
 		doc.appendChild(root);
 		
 		DOMSource source = new DOMSource(doc);
 
 		PrintStream ps = new PrintStream(f);
 		StreamResult result = new StreamResult(ps);
 
 		TransformerFactory tf = TransformerFactory.newInstance();
 		Transformer transformer = tf.newTransformer();
 		
 		transformer.transform(source, result);
 		
 		ps.close();
 		
 		return true;
 	}
 
 	public StockList read(File f) throws ParserConfigurationException, SAXException, IOException {
 		
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		
 		Document doc = db.parse(f);
 		
 		Element root = doc.getDocumentElement();
 		
 		StockList stocks = new StockListImpl(DATA_SOURCE_TYPE);
 		
 		NodeList nList = root.getChildNodes();
 		
 		for(int i=0; i<nList.getLength(); i++){
 			
 			Node node = nList.item(i);
 			
 			if(node.getNodeType() == Node.ELEMENT_NODE && "stock".equals(node.getNodeName())){
 				
 				Element elem = (Element) node;
 				stocks.add(readStock(elem));
 				//TODO handle for tickers that may have become obsolete
 				
 			}
 			
 		}
 		
 		
 		return stocks;
 	}
 
 	
 }
