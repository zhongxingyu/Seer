 package com.meadowhawk.homepi.util.service;
 
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.meadowhawk.homepi.util.model.PublicRESTDoc;
 import com.meadowhawk.homepi.util.model.PublicRESTDocMethod;
 import com.meadowhawk.homepi.util.model.ServiceDocMethodTO;
 import com.meadowhawk.homepi.util.model.ServiceDocTO;
 
 /**
  * Generates Documentation on all provided REST services.
  * @author Lee Clarke
  */
 @Path("/docs")
 @PublicRESTDoc(serviceName = "DocumentationService", description = "Provides Documentation Services for all anotated REST EndPoints so the API can be easily documented from with in the codebase.")
 public class DocumentationRESTService {
 	private Log log = LogFactory.getLog(DocumentationRESTService.class);
 	private static final String CONTENT = "#Content#";
 //TODO: Add template loading or at least a CSS Param option for configuration.
 	private static final String NO_CACHE = "<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\"><META HTTP-EQUIV=\"Expires\" CONTENT=\"-0\">";
 	private static final String INDEX = "<HTML><head>" + NO_CACHE + "<style type=\"text/css\">a:link {text-decoration:none; color:#FFFFFF;} a:visited {text-decoration:none; color:#FFFFFF;} a:hover {text-decoration:underline;}</style></head><body><h2>WFM Service Endpoint Index.</h2><table>"+CONTENT+"</table></body></HTML>";
 
 	private static final String END_POINT = "<HTML><head>" + NO_CACHE + "<style type=\"text/css\">a:link {text-decoration:none; color:#06C} TH a:link {color:#FFFFFF;} a:visited {text-decoration:none; } TH a:visited{color:#FFFFFF;} a:hover {text-decoration:underline;} table.end-point{border-width: 1px; border-spacing: 0px; width: 100%} table.end-point th{vertical-align: top; text-align:left; width:120px} table.end-point td{text-align:left}</style></head><body><h2>INDEX_LINK>>WFM Service Endpoint Index.</h2><table>"+CONTENT+"</table></body></HTML>";
 	private static final String INDEX_LINK = "INDEX_LINK";
 	
 	@Context UriInfo uriInfo;
 	
 	@Autowired
 	DocService docService;
 	
 	@GET
 	@Path("/index")
 	@Produces({MediaType.TEXT_HTML})
 	@PublicRESTDocMethod(endPointName="Index", description="Generates an HTML page for browsing all supported EndPoints in the REST API.", sampleLinks={""})
 	public String getRestServiceDocs() {
 		List<ServiceDocTO> serviceDocs = docService.getEndpointDocs();
 		StringBuilder indexString = new StringBuilder();
 		for (ServiceDocTO serviceDoc : serviceDocs) {
 			
 			indexString.append("<TR><TH colspan=2 align=\"left\" bgcolor='red'><a href=\"")
 				.append("/wfm-dev/docs/endpoint/").append(serviceDoc.getServiceName()).append("\">")
 				.append(serviceDoc.getServiceName())   //TODO:  Build Links to Endpoint Pages.
 				.append("</a>")
 				.append("</TH></TR>");
 				
 			indexString.append("<TR><TH align=\"left\">Root Path</TH><TD>")
 				.append(serviceDoc.getServicePath())
 				.append("</TD></TR>");
 			
 			indexString.append("<TR><TH align=\"left\">Description</TH><TD>")
 				.append(serviceDoc.getServiceDescription())
 				.append("</TD></TR>").append("<TR><TD colspan=2>&nbsp;</TD></TR>");
 		}
 		
 		return INDEX.replaceFirst(CONTENT, indexString.toString());
 	}
 		
 	private String getIndexLinkHTML() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<a href=\"../index\">");
 		sb.append("Index</a>");
 		return sb.toString();
 	}
 
 	@GET
 	@Path("/endpoint/{endPointName}")
 	@Produces({MediaType.TEXT_HTML,MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
 	@PublicRESTDocMethod(endPointName="EndPoint Documentation", description="Generates an HTML page for viewing the EndPoints supported by the requested service.", sampleLinks={"docs/endpoint/DocumentationService"}, errorCodes="404 - If bad service name is used.")
 	public String getRestServiceEndPointDocs(@PathParam("endPointName") String endPointName) {
 		StringBuilder indexString = new StringBuilder();
 		ServiceDocTO serviceDoc = docService.getEndpointDocsByName(endPointName);
 			
 		indexString.append("<TR><TH colspan=2 align=\"left\" bgcolor='red'><a href=\"")
 			.append("/wfm-dev/docs/endpoint/").append(serviceDoc.getServiceName()).append("\">")
 			.append(serviceDoc.getServiceName())
 			.append("</a>")
 			.append("</TH></TR>");
 		
 		indexString.append("<TR><TH align=\"left\">Description</TH><TD>")
 			.append(serviceDoc.getServiceDescription())
 			.append("</TD></TR>");
 		indexString.append("<TR><TH align=\"left\">Root Path</TH><TD>")
 			.append(serviceDoc.getServicePath())
 			.append("</TD></TR>").append("<TR><TD colspan=2>&nbsp;</TD></TR>");
 		
 		//TODO: Output Endpoint Data.
 		indexString.append("<TR><TH align=\"left\">&nbsp;</TH><TD>");
 		
 		String rootPath = serviceDoc.getServicePath() + "/";
 		for (ServiceDocMethodTO method : serviceDoc.getMethodDocs()) {
 			indexString.append("<table class=\"end-point\"><TR><TH colspan=2 align=\"left\" bgcolor='red'><a href=\"#").append(method.getEndPointName()).append("\">")
 				.append(method.getEndPointName()).append("</TH></TR>")
 				.append("<TR><th>HTTP Method</th><td>").append(method.getEndPointRequestType()).append("</td></TR>")
 				.append("<TR><th>Path</th><td>").append(rootPath).append(method.getEndPointPath()).append("</td></TR>")
 				.append("<TR><th>Consumes</th><td>").append(convertToStringList(method.getConsumes())).append("</td></TR>")
 				.append("<TR><th>Provides</th><td>").append(convertToStringList(method.getEndPointProvides())).append("</td></TR>")
 				.append("<TR><th>Sample Links</th><td>").append(convertToLinks(method.getSampleLinks())).append("</td></TR>")
 				.append("<TR><th align=\"top\">Description</th><td>").append(method.getEndPointDescription()).append("</td></TR>")
 				.append("<TR><th align=\"top\">Error Codes</th><td>").append(convertToBulletedList(method.getErrors())).append("</td></TR>")
 				.append("<TR><TD colspan=2>&nbsp;</TD></TR></table>");
 		}
 		
 		return (END_POINT.replaceFirst(CONTENT, indexString.toString()).replaceFirst(INDEX_LINK, getIndexLinkHTML()));
 	}
 
 	/**
 	 * @param values
 	 * @return
 	 */
 	private String convertToBulletedList(String[] values) {
 		StringBuilder sb = new StringBuilder("<ul>");
 		if (values != null) {
 
 			for (String string : values) {
 				sb.append("<li>").append(string).append("</li>");
 			}
 		}
 		return sb.substring(0, sb.length())+"</ul>";
 	}
 
 	/**
 	 * Makes a list of links into HTML links.
 	 * @param array of url strings.
 	 * @return html string
 	 */
 	private Object convertToLinks(String[] sampleLinks) {
 		StringBuilder sb = new StringBuilder();
 		//TODO: Prepend this servers HostName so the links work right.
 		//uriInfo.getBaseUri().
 		
 		for (String string : sampleLinks) {
 			sb.append("<a href=\"")
 				.append(string)
 				.append("\" class=\"plain\">")
 				.append(string)
 				.append("</a><br>");
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Makes an array of string into a comma separated list for display.
 	 * @param consumes
 	 * @return
 	 */
 	private String convertToStringList(String[] values) {
 		return convertToStringList(values, ",");
 	}
 	
 	/**
 	 * Makes an array of string into a comma separated list for display allowing delimiter to be specified..
 	 * @param values
 	 * @param delem
 	 * @return
 	 */
 	private String convertToStringList(String[] values, String delem) {
 		StringBuilder sb = new StringBuilder();
 		if (values != null) {
 
 			for (String string : values) {
 				sb.append(" ").append(string).append(delem);
 			}
 		}
 		return sb.substring(0, sb.length());
 	}
 }
