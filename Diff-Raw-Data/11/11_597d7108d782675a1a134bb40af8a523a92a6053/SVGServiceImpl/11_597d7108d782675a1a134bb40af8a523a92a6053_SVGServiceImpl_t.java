 package de.enwida.web.service.implementation;
 
 import java.awt.Color;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringWriter;
 
 import javax.servlet.ServletContext;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
 import org.apache.batik.transcoder.TranscoderInput;
 import org.apache.batik.transcoder.TranscoderOutput;
 import org.apache.batik.transcoder.image.ImageTranscoder;
 import org.apache.batik.transcoder.image.PNGTranscoder;
 import org.apache.batik.util.XMLResourceDescriptor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.enwida.web.service.interfaces.ISVGService;
 
@Service("svgService")
 public class SVGServiceImpl implements ISVGService {
 	
	@Autowired(required = false)
 	private ServletContext servletContext;
 	
 	public SVGServiceImpl() {
 		System.setProperty("org.xml.sax.driver","org.apache.xerces.parsers.SAXParser");
 		XMLResourceDescriptor.setXMLParserClassName("org.apache.xerces.parsers.SAXParser");
 	}
 	
 	@Override
 	public void rasterize(InputStream in, OutputStream out) throws Exception {
 		final Document doc = sanitizeSVG(in);
 		final String svgData = serializeNode(doc);
 		final PNGTranscoder transcoder = new PNGTranscoder();
 		in = new ByteArrayInputStream(svgData.getBytes("UTF-8"));
 
 		transcoder.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE);
 		transcoder.transcode(new TranscoderInput(in), new TranscoderOutput(out));
 	}
 	
 	@Override
 	public Document sanitizeSVG(InputStream svgData) throws IOException {
 	    final String parser = XMLResourceDescriptor.getXMLParserClassName();
 	    final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
 	    final Document doc = f.createSVGDocument(null, svgData);
 	    final InputStream cssData = servletContext.getResourceAsStream("resources/css/chart/chart-svg.css");
 	    
 	    removeTooltipData(doc);
 	    addCSS(doc, cssData);
 	    return doc;
 	}
 
 	@Override
 	public String serializeNode(Node node) throws Exception {
 		final StringWriter out = new StringWriter();
 		final Transformer transformer = TransformerFactory.newInstance().newTransformer();
 		transformer.transform(new DOMSource(node), new StreamResult(out));
 		return out.toString();
 	}
 
 	private static void removeTooltipData(Node node) {
 		final NodeList children = node.getChildNodes();
 		for (int i = 0; i < children.getLength(); i++) {
 			final Node child = children.item(i);
 			removeTooltipData(child);
 		}
 		
 		final NamedNodeMap attrs = node.getAttributes();
 		if (attrs != null) {
 			try { node.getAttributes().removeNamedItem("data-legend"); } catch (Exception e) { }
 			try { node.getAttributes().removeNamedItem("original-title"); } catch (Exception e) { }
 		}
 	}
 	
 	private static void addCSS(Document doc, InputStream in) throws IOException {
 		final byte[] buffer = new byte[2048];
 		final StringWriter sw = new StringWriter();
 		int read;
 		
 		while ((read = in.read(buffer)) > 0) {
 			sw.append(new String(buffer, 0, read, "UTF-8"));
 		}
 		
 		final Node defs = doc.createElement("defs");
 		final Node style = doc.createElement("style");
 		final Attr type = doc.createAttribute("type");
 		type.setValue("text/css");
 		style.getAttributes().setNamedItem(type);
 		final Node css = doc.createCDATASection(sw.toString());
 		
 		doc.getDocumentElement().insertBefore(defs, doc.getDocumentElement().getChildNodes().item(0));
 		defs.appendChild(style);
 		style.appendChild(css);
 	}
 	
 
 }
