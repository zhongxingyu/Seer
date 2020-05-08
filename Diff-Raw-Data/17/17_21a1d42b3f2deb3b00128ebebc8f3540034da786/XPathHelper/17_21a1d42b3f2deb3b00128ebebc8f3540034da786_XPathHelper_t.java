 package org.computer.knauss.reqtDiscussion.io.jazz.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.jdom2.Attribute;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.filter.AbstractFilter;
 import org.jdom2.filter.ElementFilter;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.xpath.XPathExpression;
 import org.jdom2.xpath.XPathFactory;
 
 public class XPathHelper {
 
 	private Document document;
 	private InputStream inputStream;
 	private SAXBuilder builder;
 
 	public XPathHelper() {
 		builder = new SAXBuilder();
 	}
 
 	public synchronized void setDocument(InputStream is) throws JDOMException, IOException {
 		if (this.inputStream != null)
 			this.inputStream.close();
 		this.inputStream = is;
 		
 		this.document = builder.build(is);
 
 		// XXX removing namespaces makes XPath easier but can introduce semantic
 		// errors
 		for (Element el : this.document.getRootElement().getDescendants(
 				new ElementFilter())) {
 			if (el.getNamespace() != null) {
 				// System.out.println("removing namespae" + el.getNamespace());
 				el.setNamespace(null);
 				for (Attribute a : el.getAttributes())
 					if (a.getNamespace() != null)
 						a.setNamespace(null);
 			}
 		}
 
 	}
 
	public synchronized Document getDocument() {
 		return this.document;
 	}
 
 	public int count(String xmlExp) {
 		List<Object> selection = select(xmlExp);
 		return selection.size();
 	}
 
 	public List<Object> select(String xmlExp) {
 		return select(this.document, xmlExp);
 	}
 
 	public List<Object> select(Object object, String xmlExp) {
 		// System.out.println("----ns inherited");
 		// for (Namespace ns : this.document.getNamespacesInherited())
 		// System.out.println(ns);
 		// System.out.println("----ns in scope");
 		// for (Namespace ns : this.document.getNamespacesInScope())
 		// System.out.println(ns);
 		// System.out.println("----ns introduced");
 		// for (Namespace ns : this.document.getNamespacesIntroduced())
 		// System.out.println(ns);
 
 		XPathExpression<Object> xpath = XPathFactory.instance().compile(xmlExp,
 				new NullFilter(), null, this.document.getNamespacesInherited());
 		List<Object> selection = xpath.evaluate(object);
 		return selection;
 	}
 
 	private class NullFilter extends AbstractFilter<Object> {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Object filter(Object arg0) {
 			return arg0;
 		}
 
 	}
 
 }
