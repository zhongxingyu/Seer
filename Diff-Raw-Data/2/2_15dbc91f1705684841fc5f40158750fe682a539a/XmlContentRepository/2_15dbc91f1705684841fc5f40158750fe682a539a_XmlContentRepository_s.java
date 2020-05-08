 package com.gentics.cr.rest.xml;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.exceptions.CRException.ERRORTYPE;
 import com.gentics.cr.rest.ContentRepository;
 
 /**
  * Contentrepository suited for XML.
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  */
 public class XmlContentRepository extends ContentRepository {
 
 	/**
 	 * Serial id.
 	 */
 	private static final long serialVersionUID = -6929053170765114770L;
 
 	/**
 	 * the root element in the xml code.
 	 */
 	protected Element rootElement;
 
 	/**
 	 * the xml document to write to the stream.
 	 */
 	protected Document doc;
 
 	/**
 	 * The {@link DOMSource} to write the elements into.
 	 */
 	protected DOMSource src;
 
 	/**
 	 * Create new instance of the {@link XmlContentRepository} with UTF-8 as
 	 * encoding.
 	 * @param attr TODO javadoc
 	 */
 	public XmlContentRepository(final String[] attr) {
 		this(attr, "UTF-8");
 	}
 
 	/**
 	 * TODO javadoc.
 	 * @param attr TODO javadoc
 	 * @param encoding TODO javadoc
 	 */
 	public XmlContentRepository(final String[] attr, final String encoding) {
 		this(attr, encoding, null);
 	}
 
 	/**
 	 * Calls super constructor from ContentRepository.
 	 * @param attr
 	 * @param encoding
 	 * @param object
 	 */
 	public XmlContentRepository(final String[] attr, final String encoding, final String[] object) {
 		this(attr, encoding, object, null);
 	}
 
 	/**
 	 * 
 	 * @param attr
 	 * @param encoding
 	 * @param options
 	 */
 	public XmlContentRepository(final String[] attr, final String encoding, final String[] options, final String name) {
 		super(attr, encoding, options);
 		if (name == null || name.equals("")) {
 			initializeContentRepository("Contentrepository");
 		}
 	}
 
 	/**
 	 * Initialize content repository (create root element with name provided).
 	 * @param name Name for the contentrepository.
 	 */
 	private void initializeContentRepository(final String name) {
 		// Create XML Document
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder;
 		try {
 			builder = factory.newDocumentBuilder();
 			this.doc = builder.newDocument();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 
 		this.src = new DOMSource(doc);
 
 		// Create Root Element
 		this.rootElement = doc.createElement(name);
 		doc.appendChild(rootElement);
 	}
 
 	/**
 	 * returns content type for this contentrepository.
 	 * @return text/xml
 	 */
 	public String getContentType() {
 		return "text/xml";
 	}
 
 	/**
 	 * Write an error to the specified stream.
 	 * @param stream Stream to write the error into
 	 * @param ex exception to write into the stream
 	 * @param isDebug specifies if debug is enabled (e.g. Output the stacktrace)
 	 */
 	@Override
 	public final void respondWithError(final OutputStream stream, final CRException ex, final boolean isDebug) {
 		if (this.rootElement == null) {
 			// Create Root Element
 			this.rootElement = doc.createElement("Contentrepository");
 			doc.appendChild(rootElement);
 		}
 		clearElement(this.rootElement);
 		Element errElement = doc.createElement("Error");
 		errElement.setAttribute("type", ex.getType());
		errElement.setAttribute("messge", ex.getMessage());
 		if (isDebug) {
 			Element stackTrace = doc.createElement("StackTrace");
 			Text text = doc.createCDATASection(ex.getStringStackTrace());
 			stackTrace.appendChild(text);
 			errElement.appendChild(stackTrace);
 		}
 
 		this.rootElement.setAttribute("status", "error");
 		this.rootElement.appendChild(errElement);
 
 		StreamResult strRes = new StreamResult(stream);
 		try {
 			TransformerFactory.newInstance().newTransformer().transform(this.src, strRes);
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Write XML Elements to the specified stream.
 	 * @param stream 
 	 * @throws CRException 
 	 */
 	public void toStream(final OutputStream stream) throws CRException {
 
 		if (this.resolvableColl.isEmpty()) {
 			//No Data Found
 			throw new CRException("NoDataFound", "Data could not be found.", ERRORTYPE.NO_DATA_FOUND);
 		} else {
 			//Elements found/status ok
 			this.rootElement.setAttribute("status", "ok");
 
 			for (Iterator<CRResolvableBean> it = this.resolvableColl.iterator(); it.hasNext();) {
 				CRResolvableBean crBean = it.next();
 
 				Element objElement = processElement(crBean);
 				this.rootElement.appendChild(objElement);
 			}
 		}
 
 		// output xml
 		try {
 			OutputStreamWriter wr = new OutputStreamWriter(stream, this.getResponseEncoding());
 
 			StreamResult strRes = new StreamResult(wr);
 			TransformerFactory.newInstance().newTransformer().transform(this.src, strRes);
 			wr.flush();
 			wr.close();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create an element from the provided bean.
 	 * @param crBean bean to transform into an xml element.
 	 * @return transformed element.
 	 */
 	private Element processElement(final CRResolvableBean crBean) {
 		Element objElement = doc.createElement("Object");
 
 		objElement.setAttribute("contentid", "" + crBean.getContentid());
 		objElement.setAttribute("obj_id", "" + crBean.getObj_id());
 		objElement.setAttribute("obj_type", "" + crBean.getObj_type());
 		objElement.setAttribute("mother_id", crBean.getMother_id() == null ? "" : "" + crBean.getMother_id());
 		objElement.setAttribute("mother_type", crBean.getMother_type() == null ? "" : "" + crBean.getMother_type());
 
 		if (crBean.getAttrMap() != null && !crBean.getAttrMap().isEmpty()) {
 			Element attrContainer = doc.createElement("attributes");
 			Iterator<String> bit = crBean.getAttrMap().keySet().iterator();
 			while (bit.hasNext()) {
 				String entry = bit.next();
 				if (!"".equals(entry)) {
 					Object bValue = crBean.getAttrMap().get(entry);
 					String value = "";
 					if (bValue != null) {
 						if (!entry.equals("binarycontent") && (bValue.getClass().isArray() || bValue.getClass() == ArrayList.class)) {
 							Object[] arr;
 							if (bValue.getClass() == ArrayList.class) {
 								arr = ((ArrayList<Object>) bValue).toArray();
 							} else {
 								arr = (Object[]) bValue;
 							}
 							for (int i = 0; i < arr.length; i++) {
 								Element attrElement = doc.createElement(entry);
 								attrContainer.appendChild(attrElement);
 								if (arr[i].getClass() == String.class) {
 									value = (String) arr[i];
 								} else {
 									try {
 										value = new String(getBytes(bValue));
 									} catch (IOException e) {
 										e.printStackTrace();
 									}
 								}
 								Text text = doc.createCDATASection(value);
 								attrElement.appendChild(text);
 							}
 						} else {
 							Element attrElement = doc.createElement(entry);
 							attrContainer.appendChild(attrElement);
 
 							valueToNode(doc, attrElement, entry, bValue);
 							
 						}
 
 					} else {
 						Element attrElement = doc.createElement(entry);
 						attrContainer.appendChild(attrElement);
 
 						Text text = doc.createCDATASection(value);
 						attrElement.appendChild(text);
 					}
 				}
 			}
 			objElement.appendChild(attrContainer);
 		}
 
 		if (crBean.getChildRepository() != null && crBean.getChildRepository().size() > 0) {
 			Element childContainer = doc.createElement("children");
 
 			for (Iterator<CRResolvableBean> it = crBean.getChildRepository().iterator(); it.hasNext();) {
 
 				CRResolvableBean chBean = it.next();
 
 				Element chElement = processElement(chBean);
 				childContainer.appendChild(chElement);
 			}
 
 			objElement.appendChild(childContainer);
 		}
 		return objElement;
 	}
 
 	/**
 	 * Convert the value to a XML Node.
 	 * @param d the current xml document.
 	 * @param attrElement attribute.
 	 * @param entry attribute name
 	 * @param bValue value.
 	 */
 	private void valueToNode(final Document d, final Element attrElement, final String entry, final Object bValue) {
 		String value = "";
 		if (entry.equals("binarycontent")) {
 			try {
 				value = new String((byte[]) bValue);
 			} catch (ClassCastException x) {
 				try {
 					value = new String(getBytes(bValue));
 				} catch (IOException e) {
 					value = bValue.toString();
 					e.printStackTrace();
 				}
 			}
 		} else {
 			if (bValue instanceof String) {
 				value = (String) bValue;
 			} else if (bValue instanceof Map<?, ?>) {
 				Map<?, ?> map = (Map<?, ?>) bValue;
 				for (Entry<?, ?> e : map.entrySet()) {
 					String key = e.getKey().toString();
 					Object mValue = e.getValue();
 					if (mValue instanceof String[]) {
 						String[] arr = (String[]) mValue;
 						for (String s : arr) {
 							Element elem = doc.createElement(key);
 							Text text = doc.createCDATASection(s);
 							elem.appendChild(text);
 							attrElement.appendChild(elem);
 						}
 						return;
 					} else {
 						Element elem = doc.createElement(key);
 						Text text = doc.createCDATASection(mValue.toString());
 						elem.appendChild(text);
 						attrElement.appendChild(elem);
 						return;
 					}
 				}
 			} else {
 				value = bValue.toString();
 			}
 		}
 		Text text = doc.createCDATASection(value);
 		attrElement.appendChild(text);
 	}
 }
