 package net.rujel.reusables;
 
import com.webobjects.appserver.WOActionResults;
 import com.webobjects.appserver.WOContext;
 import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
 import com.webobjects.appserver.WOResponse;
 import com.webobjects.foundation.NSDictionary;
 
 public class HTMLPageWrapper extends ExtDynamicElement {
     public HTMLPageWrapper(String name, NSDictionary associations, WOElement template) {
 		super(name, associations, template);
     }
 
     public void appendToResponse(WOResponse response, WOContext aContext) {
     	if(Various.boolForObject(valueForBinding("omit", aContext)) ||
     			(!hasBinding("hide", aContext) && aContext.page() != aContext.component())) {
     		super.appendToResponse(response, aContext);
     		return;
     	}
     	if(Various.boolForObject(valueForBinding("headClose", aContext))) {
     		response.appendContentString("</head>\n<body>");
     		return;
     	}
 		response.appendContentString(
 				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n");
 		response.appendContentString(
 				"\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
 		response.appendContentString("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n");
 		response.appendContentString(
 				"\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
 		String title = (String)valueForBinding("title", aContext);
 		if(title != null) {
 			response.appendContentString("\t<title>");
 			response.appendContentString(title);
 			response.appendContentString("</title>\n");
 		}
 		if(!Various.boolForObject(valueForBinding("headOpen", aContext)))
 			response.appendContentString("</head>\n<body>");
 		super.appendToResponse(response, aContext);
 		response.appendContentString("</body>\n</html>");
     }
    
	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
		if(children != null)
			return children.invokeAction(aRequest, aContext);
		return null;
	}
 }
