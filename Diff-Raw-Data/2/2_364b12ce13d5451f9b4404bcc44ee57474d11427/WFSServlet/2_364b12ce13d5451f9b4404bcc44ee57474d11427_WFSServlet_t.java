 package gov.usgs.cida.ogc;
 
 import gov.usgs.cida.ogc.specs.OGC_WFSConstants;
 import gov.usgs.cida.ogc.specs.WFS_1_1_Operation;
 import gov.usgs.cida.ogc.utils.FileResponseUtil;
 import gov.usgs.cida.ogc.utils.ServletHandlingUtils;
 import gov.usgs.cida.ogc.utils.ServletHandlingUtils.RequestBodyExceededException;
 import gov.usgs.cida.utils.collections.CaseInsensitiveMap;
 import gov.usgs.webservices.ibatis.XMLStreamReaderDAO;
 import gov.usgs.webservices.stax.XMLStreamUtils;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.codehaus.stax2.XMLOutputFactory2;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * Servlet implementation class WFSServlet
  */
 public class WFSServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private final static String XPATH_request = "local-name(/*)";
 	private final static String XPATH_maxFeatures = "/wfs:GetFeature/@maxFeatures";
 	private final static String XPATH_typeName = "/wfs:GetFeature/wfs:Query/@typeName";
 	
 	// Bounding box XPATH
 	private final static String XPATH_Envelope = "/wfs:GetFeature/wfs:Query/ogc:Filter/ogc:BBOX/gml:Envelope";
 	private final static String XPATH_cornerLower = "gml:lowerCorner/text()";
 	private final static String XPATH_upperCorner = "gml:upperCorner/text()";
 	
 	private final static String XPATH_featureId = "/wfs:GetFeature/wfs:Query/ogc:Filter/ogc:GmlObjectId/@gml:id";
 	
 	private final static Pattern PATTERN_cornerSplit = Pattern.compile("\\s+");
        
 	
 	private final static XMLOutputFactory2 xmlOutputFactory;
 	static {
 		xmlOutputFactory = (XMLOutputFactory2)XMLOutputFactory2.newInstance();
 		xmlOutputFactory.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);
 		xmlOutputFactory.configureForSpeed();
 	}
 	
 
 	
     /**
      * @see HttpServlet#HttpServlet()
      */
     public WFSServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		CaseInsensitiveMap<String> parameterMap = CaseInsensitiveMap.stringifyValues(request.getParameterMap());
 		queryAndSend(request, response, parameterMap);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			Document document = ServletHandlingUtils.extractXMLRequestDocument(request);
 			
 			CaseInsensitiveMap<String> parameterMap = createParameterMapFromDocument(document);
 			queryAndSend(request, response, parameterMap);
 			
 		} catch (RequestBodyExceededException rbe) {
 			// If we get errors in the request parsing, then just send the error
 			int errorCode = (rbe.errorCode != null)? rbe.errorCode: 403; // 403 as default value for error code
 			rbe.printStackTrace();
 			response.sendError(errorCode, rbe.getMessage());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private CaseInsensitiveMap<String> createParameterMapFromDocument(Document document) throws XPathExpressionException {
 		if (document == null) {
 			return null;
 		}
 		
 		CaseInsensitiveMap<String> parameterMap = new CaseInsensitiveMap<String>();
 		
 		XPathFactory xpathFactory = XPathFactory.newInstance();
 		XPath xpath = xpathFactory.newXPath();
 		xpath.setNamespaceContext(new OGCBinding.GetObservationNamespaceContext());
 
 		
 		XPathExpression requestExpression =  xpath.compile(XPATH_request);
 		String requestString = requestExpression.evaluate(document);
 		parameterMap.put("request", requestString);
 		
 		XPathExpression maxFeaturesExpression =  xpath.compile(XPATH_maxFeatures);
 		String maxFeaturesString = maxFeaturesExpression.evaluate(document);
		if (!"".equals(maxFeaturesString)) {  // optional attribute
 			parameterMap.put("maxFeatures", maxFeaturesString);
 		}
 		
 		XPathExpression typeNameExpression =  xpath.compile(XPATH_typeName);
 		String typeNameString = typeNameExpression.evaluate(document);
 		parameterMap.put("typeName", typeNameString);
 		
 		XPathExpression featureIdExpression =  xpath.compile(XPATH_featureId);
 		String featureIdString = featureIdExpression.evaluate(document);
 		if (!"".equals(featureIdString)) {
 			parameterMap.put("featureId", featureIdString);
 		}
 		
 		XPathExpression envelopeExpression =  xpath.compile(XPATH_Envelope);
 		Object envelopeResult = envelopeExpression.evaluate(document, XPathConstants.NODE);
 		if (envelopeResult != null && envelopeResult instanceof Node) {
 			
 			Node envelopeNode = (Node)envelopeResult;
 			
 			XPathExpression lowerCornerExpression =  xpath.compile(XPATH_cornerLower);
 			XPathExpression upperCornerExpression =  xpath.compile(XPATH_upperCorner);
 			
 			String lowerCornerString = lowerCornerExpression.evaluate(envelopeNode);
 			String upperCornerString = upperCornerExpression.evaluate(envelopeNode);
 			
 			// Parse the coordinates of the bounding box corner parameters
 			if (lowerCornerString != null && upperCornerString != null) {
 				String[] lowerSplit = PATTERN_cornerSplit.split(lowerCornerString.trim());
 				String[] upperSplit = PATTERN_cornerSplit.split(upperCornerString.trim());
 				
 				// Format to match the bBox passed in as kvp
 				String bBox = lowerSplit[0] + "," + lowerSplit[1] + "," + upperSplit[0] + "," + upperSplit[1];
 				parameterMap.put("bBox", bBox);
 			}
 		}
 				
 		return parameterMap;
 	}
 	
 	private void queryAndSend(HttpServletRequest request, HttpServletResponse response, CaseInsensitiveMap<String> parmeterMap) throws IOException {
 		
 		// request pre-processing
 		WFS_1_1_Operation opType = WFS_1_1_Operation.parse(parmeterMap.get("request"));
 		
 		Map<String, Object> parameters = null;
 		switch(opType) {
 			case GetFeature:
 				parameters = applyGetFeatureBusinessRulesScrubbing(parmeterMap);
 				break;
 			case GetCapabilities:
 			case DescribeFeatureType:
 				/* From 04-094 WFS Implementation Spec v 1.1 Section 8.1
 				 * 
 				 * The function of the DescribeFeatureType operation is to
 				 * generate a schema description of feature types serviced by a
 				 * WFS implementation. The schema descriptions define how a WFS
 				 * implementation expects feature instances to be encoded on
 				 * input (via Insert and Update requests) and how feature
 				 * instances will be generated on output (in response to
 				 * GetFeature and GetGmlObject requests). The only mandatory
 				 * output in response to a DescribeFeatureType request is a GML3
 				 * application schema defined using XML Schema. However, for the
 				 * purposes of experimentation, vendor extension, or even
 				 * extensions that serve a specific community of interest, other
 				 * acceptable output format values may be advertised by a WFS
 				 * service in the capabilities document [clause 13]. The meaning
 				 * of such values in not defined in the WFS specification. The
 				 * only proviso in such cases is that WFS clients may safely
 				 * ignore outputFormat values that they do not recognize.
 				 */
 				parameters = new CaseInsensitiveMap<Object>();
 				parameters.putAll(parmeterMap);
 				// Note that we don't actually do anything with the parameters at the moment.
 				break;
 			default:
 				throw new IllegalArgumentException("Currently not handling REQUEST=" + opType.name());		
 		}
 		
 		response.setContentType(OGC_WFSConstants.DEFAULT_DESCRIBEFEATURETYPE_OUTPUTFORMAT);
 		response.setCharacterEncoding("UTF-8");
 		OutputStream outputStream = response.getOutputStream();
 		
 		try {
 			handleRequest(request, outputStream, parameters, opType);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			outputStream.flush();
 		}
 
 	}
 
 	private CaseInsensitiveMap<Object> applyGetFeatureBusinessRulesScrubbing(CaseInsensitiveMap<String> params) {
 		CaseInsensitiveMap<Object> result = new CaseInsensitiveMap<Object>();
 		result.putAll(params);
 		
 		String typeName = (String) params.get("typeName");
 		String bBox = (String) params.get("bBox");
 		String featureId = (String) params.get("featureId");
 		String maxFeatures = (String) params.get("maxFeatures");
 		
 		if(!"gwml:WaterWell".equals(typeName) && featureId == null) {
 			throw new IllegalArgumentException("TYPENAME missing or invalid");
 		}
 		if(bBox != null) {
 			String[] bBoxSplit = bBox.split(",");
 			if (bBoxSplit != null && bBoxSplit.length == 4) {
 				try {
 					float west = Float.parseFloat(bBoxSplit[0]);
 					float south = Float.parseFloat(bBoxSplit[1]);
 					float east = Float.parseFloat(bBoxSplit[2]);
 					float north = Float.parseFloat(bBoxSplit[3]);
 					if (Float.isNaN(west) || Float.isNaN(south) || Float.isNaN(east) || Float.isNaN(north)) {
 						throw new IllegalArgumentException("BBOX invalid number value");
 					}
 					// Is this for Alaska, this switching of order?
 					if (west > east) {
 						String s = bBoxSplit[0]; bBoxSplit[0] = bBoxSplit[2]; bBoxSplit[2] = s;
 					}
 					if (south > north) {
 						String s = bBoxSplit[1]; bBoxSplit[1] = bBoxSplit[3]; bBoxSplit[3] = s;
 					}
 					result.put("bBox", bBoxSplit);
 				} catch (NumberFormatException e) {
 					throw new IllegalArgumentException("BBOX invalid number format");
 				}
 			} else {
 				throw new IllegalArgumentException("BBOX invalid argument count");
 			}
 		}
 		if (featureId != null) {
 			if (featureId.startsWith("USGS.")) {
 				System.out.println(featureId + " - ");
 				featureId = featureId.substring(5);
 				System.out.println(featureId);
 			}
 			result.put("featureId", featureId);
 		}
 		if (maxFeatures != null) {
 			try {
 				int maxFeaturesI = Integer.parseInt(maxFeatures);
 				if (maxFeaturesI > 0) {
 					result.put("maxFeatures", maxFeaturesI);
 				}
 			} catch (NumberFormatException e) {
 				throw new IllegalArgumentException("MAXFEATURES invalid number format");
 			}
 		}
 		
 		return result;
 	}
 	
 	private void handleRequest(HttpServletRequest request, OutputStream outputStream, Map<String, Object> parameters, WFS_1_1_Operation opType)
 			throws ServletException, XMLStreamException, IOException {
 		switch(opType) {
 			case GetFeature:
 				XMLStreamReader streamReader = getXMLStreamReaderDAO().getStreamReader("wfsMapper.wfsSelect", parameters);
 				XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);
 				XMLStreamUtils.copy(streamReader, streamWriter);
 				break;
 			case GetCapabilities:
 			case DescribeFeatureType:
 
 				Map<String, String> replacementMap = new HashMap<String, String>();
 				replacementMap.put("base.url", ServletHandlingUtils.parseBaseURL(request));
 
 				// Just sending back static file for now.
 				String resource = opType == WFS_1_1_Operation.GetCapabilities ?
 					"/ogc/wfs/GetCapabilities.xml" :
 					"/ogc/wfs/DescribeFeatureType.xml";
 				String errorMessage = "<error>Unable to retrieve resource " + resource + "</error";
 
 				FileResponseUtil.writeToStreamWithReplacements(resource, outputStream, replacementMap,
 						errorMessage);
 				break;
 		}
 	}
 
 	/**
 	 * Retrieves Spring configuration bean
 	 * @return
 	 * @throws ServletException
 	 */
 	private XMLStreamReaderDAO getXMLStreamReaderDAO() throws ServletException {
 		XMLStreamReaderDAO xmlStreamReaderDAO = null;
 		ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
 		if (ac != null) {
 			Object o = ac.getBean("xmlStreamReaderDAO");
 			if (o != null && o instanceof XMLStreamReaderDAO) {
 				xmlStreamReaderDAO = (XMLStreamReaderDAO)o;
 			}
 		}
 		if(xmlStreamReaderDAO == null) {
 			throw new ServletException("Configuation error, unable to obtain reference to XMLStreamReaderDAO");
 		}
 		return xmlStreamReaderDAO;
 	}
 }
