 package feather.rs.html;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.parser.Tag;
 import org.jsoup.select.Elements;
 
 import feather.rs.View;
 import feather.rs.forms.Form;
 import feather.rs.forms.FormBuilder;
 import feather.rs.log.Log;
 import feather.rs.log.LogFactory;
 
 /**
  * Primary class for end-developers to interact with the 
  * html templating system of powderj.
  * 
  * @author sheenobu
  *
  */
 public class Html {
 
 	Document document;
 
 	Log log = LogFactory.getLog(Html.class);
 	FormBuilder formBuilder;
 	
 	public void setFormBuilder(FormBuilder formBuilder) {
 		this.formBuilder = formBuilder;
 	}
 	
 	public Document getDocument() {
 		return document;
 	}
 	
 	protected String convertProtocol(String proto){
 		if(proto.toLowerCase().contains("https"))
 		{
 			return "https";		
 		}else{
 			return "http";
 		}
 	}
 	 
 	public  <T>  void form(String cssSelector,Form<T> form)
 	{
 		formBuilder.renderFormUsingP(this,cssSelector,form);
 	}
 	
 	public void updateLinks(HttpServletRequest req){
 		Elements ex = document.select("a");
 		Elements ex2 = document.select("link");
 		Elements ex3 = document.select("form");
 		Elements ex4 = document.select("img");
 		for(Element e : ex)
 		{
 			updateLink(req, e);
 		}
 		for(Element e : ex2)
 		{
 			updateLink(req, e);
 		}
 		for(Element e : ex3)
 		{
 			updateLink(req, e);
 		}
 		for(Element e : ex4)
 		{
 			updateLink(req, e);
 		}
 	}
 	
 	protected void updateLink(HttpServletRequest req, Element e)
 	{
 		if((e.tagName().equalsIgnoreCase("a") || e.tagName().equalsIgnoreCase("link")) && 
 			e.attr("href").startsWith("/"))
 		{
 			if( (req.getServerPort() == 80 && req.getProtocol().matches("HTTP/.*")) || 
 				(req.getServerPort() == 443 && req.getProtocol().matches("HTTPS/.*")))
 			{					
 				e.attr("href", 
 						String.format("%s://%s%s%s",
 								convertProtocol(req.getProtocol()),
 								req.getServerName(),
 								req.getContextPath(),
 								e.attr("href")));
 			}else{
 				e.attr("href", 
 						String.format("%s://%s:%s%s%s",
 								convertProtocol(req.getProtocol()),
 								req.getServerName(),
 								Integer.toString(req.getServerPort()),
 								req.getContextPath(),
 								e.attr("href")));
 			}
 		}else if(e.tagName().equalsIgnoreCase("form") && e.attr("action").startsWith("/"))  {
 			if( (req.getServerPort() == 80 && req.getProtocol().matches("HTTP/.*")) || 
 					(req.getServerPort() == 443 && req.getProtocol().matches("HTTPS/.*")))
 				{					
 					e.attr("action", 
 							String.format("%s://%s%s%s",
 									convertProtocol(req.getProtocol()),
 									req.getServerName(),
 									req.getContextPath(),
 									e.attr("action")));
 				}else{
 					e.attr("action", 
 							String.format("%s://%s:%s%s%s",
 									convertProtocol(req.getProtocol()),
 									req.getServerName(),
 									Integer.toString(req.getServerPort()),
 									req.getContextPath(),
 									e.attr("action")));
 				}
 			
 		}else if(e.tagName().equalsIgnoreCase("img") && e.attr("src").startsWith("/"))  {
 			if( (req.getServerPort() == 80 && req.getProtocol().matches("HTTP/.*")) || 
 					(req.getServerPort() == 443 && req.getProtocol().matches("HTTPS/.*")))
 				{					
 					e.attr("src", 
 							String.format("%s://%s%s%s",
 									convertProtocol(req.getProtocol()),
 									req.getServerName(),
 									req.getContextPath(),
 									e.attr("src")));
 				}else{
 					e.attr("src", 
 							String.format("%s://%s:%s%s%s",
 									convertProtocol(req.getProtocol()),
 									req.getServerName(),
 									Integer.toString(req.getServerPort()),
 									req.getContextPath(),
 									e.attr("src")));
 				}
 			
 		}
 	}
 	
 	public void renderTo(String cssSelector, View view) throws Exception
 	{
 		Html newHtml = new Html();
		newHtml.setFormBuilder(formBuilder);
 		view.render(newHtml);
 		Elements ex = document.select(cssSelector);
 	
 		for (Element e : ex) {
 			e.empty();
 			e.append(newHtml.document.html());
 		}
 	}
 	
 	/**
 	 * 
 	 * @param filename
 	 * @throws IOException
 	 */
 	public void load(InputStream is) throws IOException {		
 		log.warn("Use baseUri for parsing");
 		document = Jsoup.parse(is, "UTF-8","/");
 	}
 	
 	public void loadFromString(String content) throws IOException {	
 		log.warn("Use baseUri for parsing");
 		document = Jsoup.parse(content);
 	}
 
 	public void bindValue(String cssSelector, String content) {
 		Elements ex = document.select(cssSelector);
 		for (Element e : ex) {
 			e.text(content);
 		}
 	}
 	
 	public void html(String cssSelector, String content) {
 		Elements ex = document.select(cssSelector);
 		for (Element e : ex) {
 			e.empty();
 			e.append(content);
 		}
 	}
 	
 	
 
 	public void bindAttr(String cssSelector, String attrName,String attrVal) {
 		Elements ex = document.select(cssSelector);
 		for (Element e : ex) {
 			e.attr(attrName, attrVal);
 		}
 	}
 
 	public <T> void forEachValue(String cssSelector, Iterable<T> iter,
 			ForEach<T> foreach) {
 		Elements ex = document.select(cssSelector);
 		for (Element e : ex) {
 			Element repeatedChild = e.children().first();	
 			
 			for(Element ch : e.children()) ch.remove();		
 					
 			for (T t : iter) 
 			{	
 				Element newChild;
 	
 				if(repeatedChild != null) {
 					newChild = new Element(repeatedChild.tag(),repeatedChild.baseUri());
 					newChild.attributes().addAll(repeatedChild.attributes());
 					
 					for(Node child : repeatedChild.childNodes())
 					{
 						newChild.appendChild(child.clone());	
 					}
 					//newChild.text(repeatedChild.text());			
 				}else{
 					newChild = new Element(Tag.valueOf("div"),"");
 				}
 				
 				Item<T> i = new Item<T>(t,newChild);				
 				foreach.render(i);				
 				e.appendChild(newChild);
 			}			
 		}
 	}
 
 }
