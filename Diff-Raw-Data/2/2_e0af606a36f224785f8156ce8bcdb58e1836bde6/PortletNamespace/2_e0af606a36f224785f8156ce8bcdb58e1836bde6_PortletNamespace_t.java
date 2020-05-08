 package ee.midaiganes.model;
 
 import java.io.Serializable;
 
 import ee.midaiganes.util.StringPool;
 
 public class PortletNamespace implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private static final String SEPARATOR = "_";
 	private final PortletName portletName;
 	private final String windowID;
 
 	public PortletNamespace(PortletName portletName, String windowID) {
 		this.portletName = portletName;
 		this.windowID = windowID;
 	}
 
 	public PortletNamespace(String fullWindowID) {
 		int sep = fullWindowID.lastIndexOf(SEPARATOR);
 		windowID = fullWindowID.substring(sep + 1);
 		portletName = new PortletName(fullWindowID.substring(0, sep));
 	}
 
 	public PortletName getPortletName() {
 		return portletName;
 	}
 
 	public String getWindowID() {
 		return windowID;
 	}
 
 	public String getNamespace() {
 		return portletName.getFullName() + SEPARATOR + windowID;
 	}
 
 	public boolean isDefaultWindowID() {
 		return StringPool.DEFAULT_PORTLET_WINDOWID.equals(windowID);
 	}
 
 	@Override
 	public int hashCode() {
 		return windowID.hashCode() + portletName.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return obj instanceof PortletNamespace && equals((PortletNamespace) obj);
 	}
 
 	private boolean equals(PortletNamespace pn) {
		return pn != null && windowID.equals(pn.windowID) && portletName.equals(pn.portletName);
 	}
 }
