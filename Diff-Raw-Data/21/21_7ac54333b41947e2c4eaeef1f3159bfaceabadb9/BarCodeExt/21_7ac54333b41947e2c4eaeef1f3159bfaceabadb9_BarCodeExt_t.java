 package org.openmrs.module.jembiregistration.extension.html;
 
 import org.openmrs.module.Extension;
 
 public class BarCodeExt extends Extension{
 
 	@Override
 	public MEDIA_TYPE getMediaType() {
 		return Extension.MEDIA_TYPE.html;
 	}
 
 	public String getOverrideContent(String test) {
 		
 		Integer patientId = Integer.valueOf(getParameterMap().get("patientId"));
 		
		StringBuffer headerHTML = new StringBuffer();
 		
		String servletResource = "moduleServlet/jembiregistration/printBarCodeServlet?patientId=" + patientId.toString();
				
		headerHTML.append("<script type=\"text/javascript\">");
		headerHTML.append("function printBarcode() {");
		headerHTML.append("	jQuery.get('" + servletResource + "', function(data) {");
		headerHTML.append("		//alert('Printed Barcode!');");
		headerHTML.append("		});	");
		headerHTML.append("}");
		headerHTML.append("</script>");
		
		headerHTML.append("<table><tr><td>");
		headerHTML.append("<button onClick=\"printBarcode();\"><spring:message code='jembiregistration.printButtonLabel'/></button>");
		headerHTML.append("</td></tr></table>");
		
		return headerHTML.toString();
 	}
 
 }
