 package org.openmrs.module.jembiregistration.extension.html;
 
 import org.openmrs.module.Extension;
 
 public class BarCodeExt extends Extension{
 
 	@Override
 	public MEDIA_TYPE getMediaType() {
 		return Extension.MEDIA_TYPE.html;
 	}
 
 	public String getOverrideContent(String test) {
 		
 		Integer patientId = Integer.valueOf(getParameterMap().get("patientId"));
 		
		StringBuilder link = new StringBuilder("<a href=\"moduleServlet/jembiregistration/printBarCodeServlet?patientId=");
		link.append(patientId.toString());
		link.append("\">Print Bar Code</a>");
 		
		return link.toString();
 	}
 
 }
