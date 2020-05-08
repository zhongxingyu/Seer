 package com.mulesoft.util;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import org.mule.api.MuleMessage;
 import org.mule.api.transformer.TransformerException;
 import org.mule.transformer.AbstractMessageTransformer;
 
 public class ObjectNamesHTMLTransformer extends AbstractMessageTransformer{
 
 	@Override
 	public Object transformMessage(MuleMessage msg, String encoding) throws TransformerException{
 		
 		String lPrices = (String)msg.getPayload().toString();
 		lPrices = lPrices.substring(1,lPrices.length()-1);		
 		String sHTML = "<html>" +
 						"<head>" +
 							"<link rel=stylesheet href=http://twitter.github.com/bootstrap/1.4.0/bootstrap.min.css >" +
 							"<script language=javascript> " +
 								"function combofunction(sel){var value = sel.options[sel.selectedIndex].value;}" +
 							"</script>" +
 						"</head>" +
 						"<style type=text/css>" +
 						"body { padding-top: 60px;}" +
 					    "</style>" +
 						"<body>" +
						"<div class=container>" +
						"<div class=row>" +
						"<div class=span12>" +
						"<form action=http://localhost:65082/results method=post>" +
						"<h1>Results for Account</h1>";
 		StringTokenizer stringtokenizer = new StringTokenizer(lPrices, ",");
		sHTML += "<select size=10 name=objectsCombo>";
 		while (stringtokenizer.hasMoreElements()) {
 //			System.out.println(stringtokenizer.nextToken());
 			sHTML+="<option>"+stringtokenizer.nextToken()+"</option>";
 		}	
 		sHTML+="</select><br/>" +
				"<input type=submit value=Initialize! onclick=combofunction(objectsCombo.value) />" + 
				"<input type=hidden name=userOrig value=" + msg.getSessionProperty("userOrig") + " />" +
				"<input type=hidden name=passOrig value=" + msg.getSessionProperty("passOrig") + " />" +
				"<input type=hidden name=tokenOrig value=" + msg.getSessionProperty("tokenOrig") + " />" +
				"<input type=hidden name=userDest value=" + msg.getSessionProperty("userDest") + " />" +
				"<input type=hidden name=passDest value=" + msg.getSessionProperty("passDest") + " />" +
				"<input type=hidden name=tokenDest value=" + msg.getSessionProperty("tokenDest") + " />" +
 				"</form>" +
 				"</div>" +
 				"</div>" +
 				"<footer>" +
 		       	"<p>&copy; Mulesoft 2011</p>" +	
 				"</footer>" +
 				"</div>" +
 				"</body>" +
 				"</html>";		
 		return sHTML;
 	}
 	
 }
