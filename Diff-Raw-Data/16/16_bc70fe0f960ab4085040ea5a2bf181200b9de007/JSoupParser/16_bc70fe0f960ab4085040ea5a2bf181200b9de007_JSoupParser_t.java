 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 public class JSoupParser {
 	private Document doc;
 	private static float textLength;
 	private static float htmlLength;
 	private static float domLevel;
 	private static float maxHeight;
 	private static float totalLink;
 	private static float totalDiv;
 	private static float totalInteractive;
 	private static float totalImg;
 	private static float totalForm;
 	private static float totalOption;
 	private static float totalTable;
 	private static float totalP;
 	
 	public JSoupParser() {
 		textLength = 0;
 		htmlLength = 0;
 		domLevel = 1;
 		maxHeight = 0;
 		totalLink = 0;
 		totalDiv = 0;
 		totalInteractive = 0;
 		totalImg = 0;
 		totalForm = 0;
 		totalOption = 0;
 		totalTable = 0;
 		totalP = 0;
 	}
 	
 	public void parseHTML(String html){
 		doc = Jsoup.parse(html);
 	}
 
 	public void parseURL(String url) throws IOException {
 		doc = Jsoup.connect(url)
 				.data("query", "Java")
 				.userAgent("Mozilla")
 				.cookie("auth", "token")
 				.timeout(30000)
 				.get();
 	}
 	
 	public void parseFile(String fname) throws IOException {
 		doc = Jsoup.parse(new File(fname), "UTF-8");
 	}
 	
 	public Document getDocument() { 
 		return doc; 
 	}
 	
 	public String getTitle() { 
 		return doc.title(); 
 	}
 	
 	public Element getBody() {
 		return doc.body();
 	} 
 	
 	public Element getElementById(String id) { 
 		return doc.getElementById(id); 
 	}
 	
 	public Elements getElementsByTag(String tag) { 
 		return doc.getElementsByTag(tag); 
 	}
 	
 	@SuppressWarnings("unused")
 	private void count() {
 		for(Element select : getElementsByTag("select")) { totalInteractive++; }
 		for(Element input : getElementsByTag("input")) { totalInteractive++; }
 		for(Element img : getElementsByTag("img")) { totalImg++; }
 		for(Element form : getElementsByTag("form")) { totalForm++; }
 		for(Element option : getElementsByTag("option")) { totalOption++; }
 		for(Element table : getElementsByTag("table")) { totalTable++; }
 		for(Element div : getElementsByTag("div")) { totalDiv++; }
 		for(Element link : getElementsByTag("link")) { totalLink++; }
 		for(Element p : getElementsByTag("p")) { totalP++; }
 	}
 	
 	@SuppressWarnings("unused")
 	private static void traverse(Element element) {
 		float currentHeight = domLevel; 
 		List<Element> es = element.children();
 		if(es.isEmpty() && domLevel > maxHeight)
 			maxHeight = domLevel;
 		else 
 			domLevel++;
 
 		for(int i = 0; i < es.size(); i++) {
 			Element e = es.get(i);
 			e.attr("domHeight", Float.toString(domLevel));
 			
 			float eHtmlLength = e.html().length();
 			float innerHtml = eHtmlLength/htmlLength;
 			e.attr("innerHTML", Float.toString(innerHtml));
 			
 			float eTextLength = e.text().length();
 			float innerText = eTextLength/textLength;
 			e.attr("innerText", Float.toString(innerText));
 			
 			float interactiveNum = 0;
 			float imgNum = 0;
 			float formNum = 0;
 			float tableNum = 0;
 			float divNum = 0;
 			float linkNum = 0;
 			float linkTextLength = 0;
 			float pNum = 0;
 			
 			for(Element select : e.getElementsByTag("select")) { interactiveNum++; }
 			for(Element input : e.getElementsByTag("input")) { interactiveNum++; }
 			for(Element img : e.getElementsByTag("img")) { imgNum++; }
 			for(Element form : e.getElementsByTag("form")) { formNum++; }
 			for(Element table : e.getElementsByTag("table")) { tableNum++; }
 			for(Element div : e.getElementsByTag("div")) { divNum++; }
 			for(Element link : e.getElementsByTag("link")) { 
 				linkNum++;
				linkTextLength += link.text().length();
				System.out.println(linkTextLength);
			}
			for(Element a : e.getElementsByTag("a")) { 
				linkNum++;
				linkTextLength += a.text().length();
 			}
 			for(Element p : e.getElementsByTag("p")) { pNum++; }
 
 			e.attr("interactiveNum", Float.toString(interactiveNum/totalInteractive));
 			e.attr("imgNum", Float.toString(imgNum/totalImg));
 			e.attr("formNum", Float.toString(formNum/totalForm));
 			e.attr("tableNum", Float.toString(tableNum/totalTable));
 			e.attr("divNum", Float.toString(divNum/totalDiv));
 			e.attr("linkNum", Float.toString(linkNum/totalLink));
 			e.attr("linkToText", Float.toString(linkTextLength/e.text().length()));
 			e.attr("pNum", Float.toString(pNum/totalP));
 			
 			traverse(e);
 		}
 		
 		domLevel = currentHeight;
 	}
 	
 	public static void main(String[] args) throws IOException{
 		JSoupParser parser = new JSoupParser();
 		parser.parseURL("http://vnexpress.net/gl/xa-hoi/2012/04/giay-phut-lua-bao-trum-tiem-vang-lam-4-nguoi-chet/");
 		Element body = parser.getBody();
 		textLength = body.text().length();
 		htmlLength = body.html().length();
 	
 		parser.count();
 		traverse(body);		
 		for(Element e : body.getAllElements()) 
 			e.attr("domHeight", Float.toString(domLevel/maxHeight));
 	
 		FileOutputStream fout = new FileOutputStream("out.html");
 		fout.write(body.outerHtml().getBytes());
 		
//		System.out.println(body.outerHtml());
 	}
 }
