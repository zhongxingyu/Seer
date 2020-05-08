 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.container.parsing.impl;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.*;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.services.container.parsing.ServletParsingService;
 import org.gridlab.gridsphere.portletcontainer.descriptor.PortletApplication;
 import org.gridlab.gridsphere.portletcontainer.descriptor.PortletDefinition;
 import org.gridlab.gridsphere.portletcontainer.descriptor.ConcretePortletApplication;
 
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletConfig;
 import java.util.List;
 
 public class ServletParsingServiceImpl implements PortletServiceProvider, ServletParsingService {
 
     private static PortletLog log = SportletLog.getInstance(ServletParsingServiceImpl.class);
 
     public void init(PortletServiceConfig config) {
         log.info("in init()");
     }
 
     public void destroy() {
         log.info("in destroy()");
     }
 
     public PortletRequest getPortletRequest(HttpServletRequest request) {
         SportletRequestImpl req = new SportletRequestImpl(request);
         return (PortletRequest) req;
     }
 
     public PortletResponse getPortletResponse(HttpServletResponse res) {
        SportletResponse sportletResponse = new SportletResponse(res);
         return (PortletResponse) sportletResponse;
     }
 
     public void putPortletRequest(PortletRequest req) {
 
     }
 
     public PortletSettings getPortletSettings(ConcretePortletApplication portletApp, List knownGroups, List knownRoles) {
         SportletSettings settings = new SportletSettings(portletApp, knownGroups, knownRoles);
         return settings;
     }
 
     public PortletConfig getPortletConfig(ServletConfig config) {
         SportletConfig portletConfig = new SportletConfig(config);
         return portletConfig;
     }
 
     public PortletContext getPortletContext(ServletConfig config) {
         SportletContext portletContext = new SportletContext(config);
         return portletContext;
     }
 
 }
