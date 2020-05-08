 package net.idea.restnet.c.reporters;
 
 import java.io.Writer;
 import java.util.Iterator;
 
 import net.idea.restnet.c.ResourceDoc;
 import net.idea.restnet.c.html.HTMLBeauty;
 
 import org.restlet.Request;
 
 /**
  * HTML reporter for {@link AlgorithmResource}
  * @author nina
  *
  * @param <T>
  */
 public class CatalogHTMLReporter<T> extends CatalogURIReporter<T> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7644836050657868159L;
 	protected HTMLBeauty htmlBeauty;
 	
 	public HTMLBeauty getHtmlBeauty() {
 		return htmlBeauty;
 	}
 	public void setHtmlBeauty(HTMLBeauty htmlBeauty) {
 		this.htmlBeauty = htmlBeauty;
 	}
 	public CatalogHTMLReporter(Request request,ResourceDoc doc) {
 		this(request,doc,null);
 	}
 	public CatalogHTMLReporter(Request request,ResourceDoc doc,HTMLBeauty htmlbeauty) {
 		super(request,doc);
 		this.htmlBeauty = htmlbeauty;
 	}
 	@Override
 	public void header(Writer output, Iterator<T> query) {
 		try {
 			if (htmlBeauty==null) htmlBeauty = new HTMLBeauty();
 			
			htmlBeauty.writeHTMLHeader(output, "AMBIT", getRequest(),
 					getDocumentation());//,"<meta http-equiv=\"refresh\" content=\"10\">");
 		} catch (Exception x) {
 			
 		}
 	}
 	public void processItem(T item, Writer output) {
 		try {
 			String t = super.getURI(item);
 			output.write(String.format("<a href='%s'>%s</a><br>", t,item));
 		} catch (Exception x) {
 			
 		}
 	};
 	@Override
 	public void footer(Writer output, Iterator<T> query) {
 		try {
 			if (htmlBeauty == null) htmlBeauty = new HTMLBeauty();
 			htmlBeauty.writeHTMLFooter(output, "", getRequest());
 			output.flush();
 		} catch (Exception x) {
 			
 		}
 	}
 }
