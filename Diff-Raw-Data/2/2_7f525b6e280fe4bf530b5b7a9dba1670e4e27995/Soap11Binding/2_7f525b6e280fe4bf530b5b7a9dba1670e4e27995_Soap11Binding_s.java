 package org.codehaus.xfire.soap;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.wsdl.BindingFault;
 import javax.wsdl.BindingInput;
 import javax.wsdl.BindingOperation;
 import javax.wsdl.BindingOutput;
 import javax.wsdl.Definition;
 import javax.wsdl.Fault;
 import javax.wsdl.Message;
 import javax.wsdl.Operation;
 import javax.wsdl.Part;
 import javax.wsdl.Port;
 import javax.wsdl.PortType;
 import javax.wsdl.extensions.soap.SOAPBinding;
 import javax.wsdl.extensions.soap.SOAPBody;
 import javax.wsdl.extensions.soap.SOAPFault;
 import javax.wsdl.extensions.soap.SOAPHeader;
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.service.Endpoint;
 import org.codehaus.xfire.service.MessageInfo;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.transport.Transport;
 import org.codehaus.xfire.wsdl11.WSDL11Transport;
 import org.codehaus.xfire.wsdl11.builder.WSDLBuilder;
 
 import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
 import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
 import com.ibm.wsdl.extensions.soap.SOAPBodyImpl;
 import com.ibm.wsdl.extensions.soap.SOAPFaultImpl;
 import com.ibm.wsdl.extensions.soap.SOAPHeaderImpl;
 import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;
 
 /**
  * A SOAP Binding which contains information on how SOAP is mapped to the service model.
  * @author Dan Diephouse
  */
 public class Soap11Binding extends AbstractSoapBinding
 {
 
     public Soap11Binding(QName name, String bindingId, Service serviceInfo)
     {
         super(name, bindingId, serviceInfo);
     }
     
     public SoapVersion getSoapVersion()
     {
         return Soap11.getInstance();
     }
 
     public javax.wsdl.Binding createBinding(WSDLBuilder builder, PortType portType)
     {
         Transport t = builder.getTransportManager().getTransport(getBindingId());
         if (!(t instanceof WSDL11Transport)) return null;
         
         Definition def = builder.getDefinition();
         javax.wsdl.Binding wbinding = def.createBinding(); 
 
         wbinding.setQName( getName() );
         wbinding.setPortType( portType );
         wbinding.setUndefined(false);
         
         // add in soap:Body, et al
         wbinding.addExtensibilityElement(createSoapBinding());
         
         for (Iterator oitr = getService().getServiceInfo().getOperations().iterator(); oitr.hasNext();)
         {
             OperationInfo op = (OperationInfo) oitr.next();
 
             javax.wsdl.Operation wsdlOp = 
                 (javax.wsdl.Operation) portType.getOperation(op.getName(), null, null);
 
             javax.wsdl.BindingOperation bop = createBindingOperation(builder, wsdlOp, op);
 
             createHeaders(builder, op, bop);
             
             wbinding.addBindingOperation(bop);
         }
 
         def.addBinding(wbinding);
         
         return wbinding;
     }
 
     protected javax.wsdl.BindingOperation createBindingOperation(WSDLBuilder builder, 
                                                                  Operation wsdlOp,
                                                                  OperationInfo op)
     {
         Definition def = builder.getDefinition();
         javax.wsdl.BindingOperation wbindOp = def.createBindingOperation();
 
         SOAPBody body = createSoapBody(builder.getService());
 
         SOAPOperationImpl soapOp = new SOAPOperationImpl();
         soapOp.setSoapActionURI(getSoapAction(op));
         
         BindingInput bindIn = def.createBindingInput();
        bindIn.setName( op.getInputMessage().getName().getLocalPart() );
         bindIn.addExtensibilityElement( body );
         wbindOp.setBindingInput( bindIn );
         
         if (wsdlOp.getOutput() != null)
         {
             BindingOutput bindOut = builder.getDefinition().createBindingOutput();
             bindOut.setName( wsdlOp.getOutput().getName() );
             bindOut.addExtensibilityElement( body );
             wbindOp.setBindingOutput( bindOut );
         }
         
         Map faults = wsdlOp.getFaults();
         if (faults != null)
         {
             for (Iterator itr = faults.values().iterator(); itr.hasNext();)
             {
                 Fault fault = (Fault) itr.next();
                 
                 BindingFault bindingFault = def.createBindingFault();
                 bindingFault.setName(fault.getName());
                 
                 SOAPFault soapFault = createSoapFault(builder.getService());
                 soapFault.setName(fault.getName());
 
                 bindingFault.addExtensibilityElement(soapFault);
                 wbindOp.addBindingFault(bindingFault);
             }
         }
         
         wbindOp.setName( wsdlOp.getName() );
         wbindOp.setOperation( wsdlOp );
         wbindOp.addExtensibilityElement( soapOp );
         
         return wbindOp;
     }
     
     protected void createHeaders(WSDLBuilder builder, OperationInfo op, BindingOperation bop)
     {
         if (op.getInputMessage() != null)
         {
             List inputHeaders = getHeaders(op.getInputMessage()).getMessageParts();
             BindingInput bindingInput = bop.getBindingInput();
             if (inputHeaders.size() > 0)
             {
                 Message reqHeaders = createHeaderMessages(builder, op.getInputMessage(), inputHeaders);
                 builder.getDefinition().addMessage(reqHeaders);
     
                 for (Iterator headerItr = reqHeaders.getParts().values().iterator(); headerItr.hasNext();)
                 {
                     Part headerInfo = (Part) headerItr.next();
     
                     SOAPHeader soapHeader = new SOAPHeaderImpl();
                     soapHeader.setMessage(reqHeaders.getQName());
                     soapHeader.setPart(headerInfo.getName());
                     soapHeader.setUse(getUse());
     
                     bindingInput.addExtensibilityElement(soapHeader);
                 }
             }
         }
 
         if (op.getOutputMessage() != null)
         {
             List outputHeaders = getHeaders(op.getOutputMessage()).getMessageParts();
             BindingOutput bindingOutput = bop.getBindingOutput();
 
             if (outputHeaders.size() > 0)
             {
                 Message resHeaders = createHeaderMessages(builder, op.getOutputMessage(), outputHeaders);
                 builder.getDefinition().addMessage(resHeaders);
                 
                 for (Iterator headerItr = resHeaders.getParts().values().iterator(); headerItr.hasNext();)
                 {
                     Part headerInfo = (Part) headerItr.next();
         
                     SOAPHeader soapHeader = new SOAPHeaderImpl();
                     soapHeader.setMessage(resHeaders.getQName());
                     soapHeader.setPart(headerInfo.getName());
                     soapHeader.setUse(getUse());
         
                     bindingOutput.addExtensibilityElement(soapHeader);
                 }
             }
         }
     }
 
     protected Message createHeaderMessages(WSDLBuilder builder, MessageInfo msgInfo, List headers)
     {
         Message msg = builder.getDefinition().createMessage();
 
         msg.setQName(new QName(builder.getTargetNamespace(), 
                                msgInfo.getName().getLocalPart() + "Headers"));
         msg.setUndefined(false);
 
         for (Iterator itr = headers.iterator(); itr.hasNext();)
         {
             MessagePartInfo header = (MessagePartInfo) itr.next();
 
             Part part = builder.createPart(header);
 
             msg.addPart(part);
         }
 
         return msg;
     }
 
     protected SOAPFault createSoapFault( Service endpoint )
     {
         String use = getUse();
         SOAPFault fault = new SOAPFaultImpl();
         fault.setUse(use); 
 
         if ( use.equals( SoapConstants.USE_ENCODED ) )
         {
             List encodingStyles = new ArrayList();
             encodingStyles.add( getSoapVersion().getSoapEncodingStyle() );
             
             fault.setEncodingStyles(encodingStyles);
         }
         
         return fault;
     }
     
     protected SOAPHeader createSoapHeader( Service endpoint )
     {
         String use = getUse();
         SOAPHeader header = new SOAPHeaderImpl();
         header.setUse( use ); 
 
         if ( use.equals( SoapConstants.USE_ENCODED ) )
         {
             List encodingStyles = new ArrayList();
             encodingStyles.add( getSoapVersion().getSoapEncodingStyle() );
             
             header.setEncodingStyles(encodingStyles);
         }
 
         return header;
     }
     
     protected SOAPBinding createSoapBinding()
     {
         SOAPBinding soapBind = new SOAPBindingImpl();
 
         String style = getStyle();
         if (style.equals(SoapConstants.STYLE_WRAPPED)) style = SoapConstants.STYLE_DOCUMENT;
         
         soapBind.setStyle( style );
         soapBind.setTransportURI( getBindingId() );
 
         return soapBind;
     }
 
     protected SOAPBody createSoapBody(Service service)
     {
         String use = getUse();
         SOAPBody body = new SOAPBodyImpl();
         body.setUse( use ); 
 
         if ( getStyle().equals( SoapConstants.STYLE_RPC ) )
         {
             body.setNamespaceURI( service.getTargetNamespace() );
         }
         
         if ( use.equals( SoapConstants.USE_ENCODED ) )
         {
             List encodingStyles = new ArrayList();
             encodingStyles.add( getSoapVersion().getSoapEncodingStyle() );
             
             body.setEncodingStyles(encodingStyles);
         }
         
         return body;
     }
 
     public Port createPort(Endpoint endpoint, WSDLBuilder builder, javax.wsdl.Binding wbinding)
     {
         SOAPAddressImpl add = new SOAPAddressImpl();
         add.setLocationURI(endpoint.getUrl());
         
         Port port = builder.getDefinition().createPort();
         port.setBinding( wbinding );
         port.setName( endpoint.getName().getLocalPart() );
         port.addExtensibilityElement( add );
        
         return port;
     }
     
     public Port createPort(WSDLBuilder builder, javax.wsdl.Binding wbinding)
     {
         Transport t = builder.getTransportManager().getTransport(getBindingId());
         if (!(t instanceof WSDL11Transport)) return null;
         
         WSDL11Transport transport = (WSDL11Transport) t;
         
         SOAPAddressImpl add = new SOAPAddressImpl();
         add.setLocationURI(transport.getServiceURL(builder.getService()));
         
         Port port = builder.getDefinition().createPort();
         port.setBinding( wbinding );
         QName portName = (QName) builder.getService().getProperty(ObjectServiceFactory.PORT_NAME);
         if (portName != null)
         {
             port.setName(portName.getLocalPart());   
         }
         else
         {
             port.setName( builder.getService().getSimpleName() + transport.getName() + "Port" );
         }
         port.addExtensibilityElement( add );
        
         return port;
     }
 }
