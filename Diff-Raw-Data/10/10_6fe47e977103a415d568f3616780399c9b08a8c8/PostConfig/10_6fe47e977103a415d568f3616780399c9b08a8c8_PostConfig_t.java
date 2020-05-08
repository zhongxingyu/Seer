 package ch.almana.spectrum.rest.net;
 
import java.util.ArrayList;
import java.util.List;
 import java.util.Set;
 
 public class PostConfig {
 
 	class FilterConfig {
 		String attr;
 		String value;
 		public FilterConfig(String attr, String value) {
 			super();
 			this.attr = attr;
 			this.value = value;
 		}
 	}
 	
 	private int throttlesize = Integer.MAX_VALUE;
	private final List<FilterConfig> filters = new ArrayList<PostConfig.FilterConfig>();
 	private Set<String> attributesHandles;
 	private Set<String> entityIds;
 
 	public PostConfig() {
 		super();
 	}
 	
 	public void setThrottlesize(int throttlesize) {
 		if (throttlesize > 0) {
 			this.throttlesize = throttlesize;
 		}
 	}
 	
 
 	private void addDisplayedAttributes(StringBuilder sb) {
 		if (attributesHandles == null) {
 			return;
 		}
 		for (String attr : attributesHandles) {
 			sb.append("<rs:requested-attribute id=\"");
 			sb.append(attr);
 			sb.append("\" />");
 			sb.append("\n");
 		}
 		sb.append("\n");
 	}
 	
 	private void addRequestHeader(StringBuilder sb) {
 		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rs:alarm-request ");
 		if (throttlesize > 0) {
 			sb.append("throttlesize=\"");
 			sb.append(throttlesize);
 			sb.append("\"");
 		}
 		sb.append("\n xmlns:rs=\"http://www.ca.com/spectrum/restful/schema/request\"\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
 				"  xsi:schemaLocation=\"http://www.ca.com/spectrum/restful/schema/request ../../../xsd/Request.xsd\">\n");
 		sb.append("\n");
 	}
 	
 	private void addRequestFooter(StringBuilder sb) {
 		sb.append("\n");
 		sb.append("</rs:alarm-request>");
 		sb.append("\n");
 	}
 	
 
 	private void addAttributeFilter(StringBuilder sb) {
 		// FIXME I doubt it works with more than one filter
 		for (FilterConfig filter : filters) {
 			sb.append("<rs:attribute-filter>\n" + 
 					"    <search-criteria xmlns=\"http://www.ca.com/spectrum/restful/schema/filter\">\n" + 
 					"    <filtered-models>\n" + 
 					"      <equals>\n" + 
 					"        <attribute id=\""+filter.attr+"\">\n" + 
 					"          <value>"+filter.value+"</value>\n" + 
 					"        </attribute>\n" + 
 					"      </equals>\n" + 
 					"      </filtered-models>\n" + 
 					"    </search-criteria>\n" + 
 					"  </rs:attribute-filter>\n");
 		}
 		
 	}
 
 
 	private static final String ALL_DEVICE_ALARMS_FILTER = "<rs:alarm-request throttlesize=\"10\"\n" + 
 			"  xmlns:rs=\"http://www.ca.com/spectrum/restful/schema/request\"\n" + 
 			"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
 			"  xsi:schemaLocation=\"http://www.ca.com/spectrum/restful/schema/request ../../../xsd/Request.xsd \">\n" + 
 			"\n" + 
 			"   <!-- \n" + 
 			"         This xml can be posted to the Alarms URL to obtain all alarms\n" + 
 			"         on all devices\n" + 
 			"      -->\n" + 
 			"\n" + 
 			"  <!-- Attributes of Interest -->\n" + 
 			"  <rs:requested-attribute id=\"0x11f53\" />\n" + 
 			"  <rs:requested-attribute id=\"0x10000\" />\n" + 
 			"  <rs:requested-attribute id=\"0x11f56\" />\n" + 
 			"  <rs:requested-attribute id=\"0x12b4c\" />\n" + 
 			"  <rs:requested-attribute id=\"0x11f4d\" />\n" + 
 			"\n" + 
 			"  <!-- Uses the existing All Devices Search delivered with Spectrum -->\n" + 
 			"  <rs:target-models>\n" + 
 			"    <rs:models-search>\n" + 
 			"      <rs:search-criteria-file>topo/config/search-devices-criteria.xml</rs:search-criteria-file>\n" + 
 			"    </rs:models-search>\n" + 
 			"  </rs:target-models>\n" + 
 			"\n" + 
 			"</rs:alarm-request>" +
 			"\n";
 	private static final String ALL_ALARMS_FILTER = "<rs:alarm-request throttlesize=\"10\"\n" + 
 			"  xmlns:rs=\"http://www.ca.com/spectrum/restful/schema/request\"\n" + 
 			"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
 			"  xsi:schemaLocation=\"http://www.ca.com/spectrum/restful/schema/request ../../../xsd/Request.xsd \">\n" + 
 			"\n" + 
 			"   <!-- \n" + 
 			"         This xml can be posted to the Alarms URL to obtain all alarms\n" + 
 			"         on all devices\n" + 
 			"      -->\n" + 
 			"\n" + 
 			"  <!-- Attributes of Interest -->\n" + 
 			"  <rs:requested-attribute id=\"0x11f53\" />\n" + 
 			"  <rs:requested-attribute id=\"0x10000\" />\n" + 
 			"  <rs:requested-attribute id=\"0x11f56\" />\n" + 
 			"  <rs:requested-attribute id=\"0x12b4c\" />\n" + 
 			"  <rs:requested-attribute id=\"0x11f4d\" />\n" + 
 			"\n" + 
 			"</rs:alarm-request>" +
 			"\n";
 
 
 	public String getConfig() {
 		StringBuilder sb = new StringBuilder();
 		addRequestHeader(sb);
 		addDisplayedAttributes(sb);
 		addAttributeFilter(sb);
 		addEntities(sb);
 		addRequestFooter(sb);
 		return sb.toString();
 	}
 
 	private void addEntities(StringBuilder sb) {
 		if (entityIds == null) {
 			return;
 		}
 		for (String entity : entityIds) {
 			sb.append("<rs:alarms id=\"").append(entity).append("\" />\n");
 		}
 	}
 
 	public void addFilter(String attr, String val) {
 		filters.add(new FilterConfig(attr, val));
 	}
 
 	public void setAttributes(Set<String> attributesHandles) {
 		this.attributesHandles = attributesHandles;
 	}
 
 	public void setEntityIds(Set<String> entityIds) {
 		this.entityIds = entityIds;
 	}
 
 }
