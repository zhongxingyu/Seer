 package org.codehaus.xfire.transport.http;
 
import java.util.ArrayList;
import java.util.List;

 import javax.servlet.http.HttpServletRequest;
 import javax.wsdl.Binding;
 import javax.wsdl.BindingInput;
 import javax.wsdl.BindingOperation;
 import javax.wsdl.BindingOutput;
 import javax.wsdl.Operation;
 import javax.wsdl.Port;
 import javax.wsdl.PortType;
 import javax.wsdl.extensions.soap.SOAPBinding;
 import javax.wsdl.extensions.soap.SOAPBody;
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.transport.Transport;
 
 import com.ibm.wsdl.BindingImpl;
 import com.ibm.wsdl.BindingInputImpl;
 import com.ibm.wsdl.BindingOperationImpl;
 import com.ibm.wsdl.BindingOutputImpl;
 import com.ibm.wsdl.PortImpl;
 import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
 import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
 import com.ibm.wsdl.extensions.soap.SOAPBodyImpl;
 import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class SoapHttpTransport
 	implements Transport
 {
     public final static String ID = "http";
 
     public static final String HTTP_TRANSPORT_NS = "http://schemas.xmlsoap.org/soap/http";
 
     public SoapHttpTransport()
     {
     }
     
     /**
      * @see org.codehaus.xfire.transport.Transport#getName()
      */
     public String getName()
     {
         return ID;
     }
 
     /**
      * @see org.codehaus.xfire.transport.Transport#createBinding(javax.wsdl.PortType)
      */
     public Binding createBinding(PortType portType, Service service)
     {
 		Binding binding = new BindingImpl();
         binding.setQName( new QName( service.getDefaultNamespace(), service.getName()+"Binding" ) );
         binding.setPortType( portType );
         binding.setUndefined(false);
         
         binding.addExtensibilityElement( createSoapBinding(service) );
 
         return binding;
     }
 
 	protected SOAPBinding createSoapBinding(Service service)
 	{
 	    SOAPBinding soapBind = new SOAPBindingImpl();
 		
 		String style = service.getStyle();
         if ( style.equals( SoapConstants.STYLE_WRAPPED ) )
             style = SoapConstants.STYLE_DOCUMENT;
         
         soapBind.setStyle( style );
         soapBind.setTransportURI( HTTP_TRANSPORT_NS );
 
 		return soapBind;
 	}
 
     /**
      * @see org.codehaus.xfire.transport.Transport#createPort(javax.wsdl.Binding)
      */
     public Port createPort(Binding transportBinding, Service service)
     {
         SOAPAddressImpl add = new SOAPAddressImpl();
         add.setLocationURI( getUrl( service.getName() ) );
         
         Port port = new PortImpl();
         port.setBinding( transportBinding );
         port.setName( service.getName()+"Port" );
         port.addExtensibilityElement( add );
        
         return port;
     }
 
     /**
 	 * Get the URL for a particular service.
 	 */
 	protected String getUrl( String serviceName )
 	{
 		HttpServletRequest req = XFireServletController.getRequest();
         
         return getWebappBase(req) + "/services/" + serviceName;
 	}
 
 	/**
      * @see org.codehaus.xfire.transport.Transport#createBindingOperation(javax.wsdl.Message, javax.wsdl.Message, org.codehaus.xfire.java.JavaService)
      */
     public BindingOperation createBindingOperation(PortType portType, Operation wsdlOp, Service service)
     {
         // Create bindings
         SOAPBody body = createSoapBody( service );
 
         SOAPOperationImpl soapOp = new SOAPOperationImpl();
         soapOp.setSoapActionURI("");
         
         BindingInput bindIn = new BindingInputImpl();
         bindIn.setName( wsdlOp.getInput().getName() );
         bindIn.addExtensibilityElement( body );
         
         BindingOutput bindOut = new BindingOutputImpl();
         bindOut.setName( wsdlOp.getOutput().getName() );
         bindOut.addExtensibilityElement( body );
         
         BindingOperation bindOp = new BindingOperationImpl();
         bindOp.setName( wsdlOp.getName() );
         bindOp.setOperation( wsdlOp );
         bindOp.setBindingInput( bindIn );
         bindOp.setBindingOutput( bindOut );
         bindOp.addExtensibilityElement( soapOp );
         
         return bindOp;
     }
     
     public SOAPBody createSoapBody( Service service )
     {
         SOAPBody body = new SOAPBodyImpl();
         body.setUse( service.getUse() ); 
 
         if ( service.getStyle().equals( SoapConstants.STYLE_RPC ) )
        {
             body.setNamespaceURI( service.getDefaultNamespace() );
        }
        
        if ( service.getUse().equals( SoapConstants.USE_ENCODED ) )
        {
            List encodingStyles = new ArrayList();
            encodingStyles.add( service.getSoapVersion().getSoapEncodingStyle() );
            
            body.setEncodingStyles(encodingStyles);
        }
         
         return body;
     }
     
     protected String getWebappBase(HttpServletRequest request)
 	{
 		StringBuffer baseURL = new StringBuffer(128);
 		baseURL.append(request.getScheme());
 		baseURL.append("://");
 		baseURL.append(request.getServerName());
 		if (request.getServerPort() != 80)
 		{
 			baseURL.append(":");
 			baseURL.append(request.getServerPort());
 		}
 		baseURL.append(request.getContextPath());
 		return baseURL.toString();
 	}
 }
