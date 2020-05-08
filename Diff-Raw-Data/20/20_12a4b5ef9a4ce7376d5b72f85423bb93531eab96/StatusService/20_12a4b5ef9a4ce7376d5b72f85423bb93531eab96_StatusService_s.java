 /**
  * 
  */
 package cz.cuni.mff.odcleanstore.engine.outputws;
 
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.data.CharacterSet;
 import org.restlet.data.Language;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 
 /**
  * @author Petr Jerman
  *
  */
 public class StatusService extends org.restlet.service.StatusService {
 	
 	private static final String HTML_ERROR_RESPONSE_PATTERN =
 			
 			"<head>" +
 			"	<title>Status page</title>" +
 			"</head>" +
 			"<body>" +
 			"	<p style=\"font-size: 1.2em;font-weight: bold;margin: 1em 0px;\"> %s</p>" +
 		    "	<p> %s </p>" +
		    "	<p>You can get technical details <a href=\"http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1\">here</a>.<br>" +
 			"</body>" +
 		    "</html>";
 
 	@Override
 	public Representation getRepresentation(Status status, Request request,	Response response) {
		return new StringRepresentation(String.format(HTML_ERROR_RESPONSE_PATTERN, status.getReasonPhrase(), status.getDescription()),
 				MediaType.TEXT_HTML, Language.ALL, CharacterSet.UTF_8);
 	}
 	
 	@Override
 	public Status getStatus(Throwable throwable, Request request, Response response) {
 		return super.getStatus(throwable, request, response);
 	}
 }
