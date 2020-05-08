 package net.toobop.dot.model;
 
 import java.io.StringWriter;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 
 
 public class Node extends Attributed
 {
 	private Document dom;
 
 	private final Set<Edge> fromEdges = new HashSet<Edge>();
 
 	private String id;
 
 	private final Set<Edge> toEdges = new HashSet<Edge>();
 
 	public void addFrom(Edge edge)
 	{
 		fromEdges.add(edge);
 	}
 
 	public void addTo(Edge edge)
 	{
 		toEdges.add(edge);
 	}
 
 	public Document getDom()
 	{
 		return dom;
 	}
 
 	public Set<Edge> getFromEdges()
 	{
 		return fromEdges;
 	}
 
 	public String getId()
 	{
 		return id;
 	}
 
 	public Set<Edge> getToEdges()
 	{
 		return toEdges;
 	}
 
 	@Override
 	public String getValue(String name)
 	{
 		if ("label".equals(name))
 		{
 			if (dom != null)
 			{
 				return writeDom();
 			}
 		}
 		return super.getValue(name);
 	}
 
 	public boolean isHtmlLabelShape()
 	{
 		String shape = getValue("shape");
 		return shape == null || shape.toLowerCase().equals("plaintext");
 	}
 
 	public void setDom(Document dom)
 	{
 		this.dom = dom;
 	}
 
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Node [dom=" + dom + ", id=" + id + "]";
 	}
 
 	private String writeDom()
 	{
 		try
 		{
 			TransformerFactory tf = TransformerFactory.newInstance();
 			Transformer transformer = tf.newTransformer();
 			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
 			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 
 			StringWriter stringWriter = new StringWriter();
 			transformer.transform(new DOMSource(this.dom), new StreamResult(stringWriter));
 			String result = stringWriter.toString();
			result = result.replaceAll("\"", "'");
			return "<\n" + result + "\n>";
 		}
 		catch (TransformerException e)
 		{
 			throw new IllegalStateException("cannot wwrite dom", e);
 		}
 	}
 
 }
