 /*
  * Created on Feb 21, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.internal.webservice.helper;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper;
 import org.eclipse.wst.ws.parser.wsil.WebServiceEntity;
 import org.eclipse.wst.ws.parser.wsil.WebServicesParser;
 import org.eclipse.wst.wsdl.Definition;
 import org.eclipse.wst.wsdl.Port;
 import org.eclipse.wst.wsdl.Service;
import org.eclipse.wst.wsdl.internal.util.WSDLResourceImpl;
 
 /**
  * @author cbridgha
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class WSDLServiceHelperImpl implements WSDLServiceHelper {
 
 	/**
 	 * 
 	 */
 	public WSDLServiceHelperImpl() {
 		super();
 	} 
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getPortName(java.lang.Object)
 	 */
 	public String getPortName(Object port) {
 		Port aPort = (Port)port;
 		return aPort.getName();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getServicePorts(java.lang.Object)
 	 */
 	public Map getServicePorts(Object aService) {
 		Service service =(Service)aService;
 		return service.getPorts();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getServiceNamespaceURI(java.lang.Object)
 	 */
 	public String getServiceNamespaceURI(Object aService) {
 		Service service =(Service)aService;
 		return service.getQName().getNamespaceURI();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getServiceDefinitionLocation(org.eclipse.emf.ecore.EObject)
 	 */
 	public Object getServiceDefinitionLocation(EObject aService) {
 		Service service =(Service)aService;
 		return service.getEnclosingDefinition().getLocation();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getPortBindingNamespaceURI(java.lang.Object)
 	 */
 	public String getPortBindingNamespaceURI(Object aPort) {
 		Port port = (Port)aPort;
 		return port.getBinding().getQName().getNamespaceURI();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getServiceLocalPart(java.lang.Object)
 	 */
 	public String getServiceLocalPart(Object aService) {
 		Service service =(Service)aService;
 		return service.getQName().getLocalPart();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getServiceQName(java.lang.Object)
 	 */
 	public Object getServiceQName(Object aService) {
 		Service service =(Service)aService;
 		return service.getQName();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getDefinitionServices(java.lang.Object)
 	 */
 	public Map getDefinitionServices(Object aDefinition) {
 		Definition definition =(Definition)aDefinition;
 		return definition.getServices();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getWSDLDefinition(java.lang.String)
 	 */
 	public Object getWSDLDefinition(String wsdlURL) {
 		try {
 			WSDLResourceImpl res = (WSDLResourceImpl) WorkbenchResourceHelperBase.getResource(URI.createURI(wsdlURL), true);
 			if (res == null)
 				return null;
 			return res.getDefinition();
 		} catch (Exception wsdle) {
 			return null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getWSDLDefinition(org.eclipse.emf.ecore.resource.Resource)
 	 */
 	public Object getWSDLDefinition(Resource wsdlResource) {
 		return ((WSDLResourceImpl)wsdlResource).getDefinition();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper#getWsdlServicesFromWsilFile(org.eclipse.core.resources.IFile)
 	 */
 	public List getWsdlServicesFromWsilFile(IFile wsil) {
 		List result = new ArrayList();
 		WebServiceEntity entity = parseWsilFile(wsil);
 		if (entity != null && entity.getType() == WebServiceEntity.TYPE_WSIL) {
 			// get all the WSDL references from the WSIL entity
 			List wsdlList = entity.getChildren();
 			for (Iterator it = wsdlList.iterator(); it.hasNext();) {
 				Object item = it.next();
 				if (item != null && item instanceof WebServiceEntity) {
 					if (((WebServiceEntity) item).getModel() != null && ((WebServiceEntity) item).getModel() instanceof Definition) {
 						Definition def = (Definition) ((WebServiceEntity) item).getModel();
 						if (def != null && !def.getServices().isEmpty())
 							result.addAll(def.getServices().values());
 					}
 				}
 			}
 		}
 		return result;
 	}
 	public WebServiceEntity parseWsilFile(IFile wsil) {
 	WebServicesParser parser = null;
 	String url = null;
 	// verify proper input
 	if (wsil == null || !wsil.getFileExtension().equals(WSIL_EXT))
 		return null;
 	// Parse wsil file to get wsdl services
 	try {
 		url = wsil.getLocation().toFile().toURL().toString();
 		parser = new WebServicesParser(url);
 		parser.parse(WebServicesParser.PARSE_WSIL | WebServicesParser.PARSE_WSDL);
 	} catch (Exception e) {
 		//Ignore
 	}
 	if (parser == null)
 		return null;
 	return parser.getWebServiceEntityByURI(url);
 }
 
 	public boolean isService(Object aService) {
 		
 		return aService instanceof Service;
 	}
 	public boolean isWSDLResource(Object aResource) {
 		
 		return aResource instanceof WSDLResourceImpl;
 	}
 	public boolean isDefinition(Object aDefinition) {
 		
 		return aDefinition instanceof Definition;
 	}
 }
