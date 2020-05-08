 //Copyright (C) 2011 Tomáš Vejpustek
 //Full copyright notice found in src/LICENSE.
 package xml;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import series.AbstractTSLoader;
 import series.TimeSeriesLoader;
 
 /**
  * Describes method of importing a time series. Used by {@link AbstractTSLoader} to get a {@link TimeSeriesLoader}.
  * 
  * Comprises of source file and a set of <code>String</code> parameters. May need access to file containing the formula
  * to relativize the URI of time series source  (if none is set, absolute URI is used).
  * 
  * @author Tomáš Vejpustek
  *
  */
 public class TimeSeriesSource implements XMLRepresentable {
 	private File srcFile = null;
 	private File formFile = null;
 	private Map<String, String> params = null;
 	
 	/**
 	 * Creates uninitialized time series source. 
 	 */
 	public TimeSeriesSource() {}
 	
 	/**
 	 * @return Source file of the time series.
 	 */
 	public File getSourceFile() {
 		return srcFile;
 	}
 	
 	/**
 	 * Sets source file of the time series.
 	 * @param target
 	 */
 	public void setSourceFile(File target) {
 		srcFile = target;
 	}
 	
 	/**
 	 * @return Name (and therefore the type) of the loader.
 	 */
 	public String getLoaderName() {
 		return params.get("name");
 	}
 	
 	/**
 	 * Returns parameter of time series loader according to its name.
 	 * @return Parameter of the name <code>name</code>.
 	 */
 	public String getParameter(String name) {
 		return params.get(name);
 	}
 	
 	/**
 	 * Sets loader parameters from existing {@link TimeSeriesLoader}.
 	 */
 	public void setLoader(TimeSeriesLoader target) {
 		params = target.export();
 	}
 	
 	/**
 	 * Sets file containing the formula used to relativize URI of time series source.
 	 * @param target A file or <code>null</code>, which means absolute URI is used.
 	 */
 	public void setFormulaFile(File target) {
 		formFile = target;
 	}
 
 	@Override
 	public Node toXML(Document document) {
 		return toXML(document, "series");
 	}
 
 	@Override
 	public Node toXML(Document document, String name) {
 		Element out = document.createElement(name);
 		URI outURI = srcFile.toURI();
 		if (formFile != null) {
			outURI = formFile.toURI().relativize(outURI);
 		}
 		
 		//series
 		Element src = document.createElement("source");
 		src.appendChild(document.createTextNode(outURI.toString()));
 		out.appendChild(src);
 		
 		//loader
 		Element ldr = document.createElement("loader");
 		ldr.appendChild(document.createTextNode(getLoaderName()));
 		for (Map.Entry<String, String> param : params.entrySet()) {
 			if (!param.getKey().equals("name")) {
 				ldr.setAttribute(param.getKey(), param.getValue());
 			}
  		}
 		out.appendChild(ldr);
 		return out;
 	}
 
 	@Override
 	public void loadFromXML(Node node) throws XMLException {
 		File newSrc = null;
 		Map<String, String> newParams = new HashMap<String, String>();
 		
 		NodeList nodes = node.getChildNodes();
 		for (int index = 0; index < nodes.getLength(); index++) {
 			Node n = nodes.item(index);
 			if (n.getNodeName().equals("source")) {
 				try {
 					URI inURI = new URI(n.getFirstChild().getNodeValue());
 					if (formFile != null) {
						inURI = formFile.toURI().resolve(inURI);
 					}
 					newSrc = new File(inURI);
 				} catch (DOMException dome) {
 					throw new XMLException("general", "Exception when parsing URI.", dome);
 				} catch (URISyntaxException urise) {
 					throw new XMLException("series_URI", "Garbled time series source URI.", urise);
 				}
 			} else if (n.getNodeName().equals("loader")) {
 				newParams.put("name", n.getFirstChild().getNodeValue());
 				
 				//parse attributes
 				NamedNodeMap p = n.getAttributes();
 				for (int pindex = 0; pindex < p.getLength(); pindex++) {
 					Node pn = p.item(pindex);
 					newParams.put(pn.getNodeName(), pn.getNodeValue());
 				}
 			}
 		}
 		
 		srcFile = newSrc;
 		params = newParams;
 	}
 
 }
