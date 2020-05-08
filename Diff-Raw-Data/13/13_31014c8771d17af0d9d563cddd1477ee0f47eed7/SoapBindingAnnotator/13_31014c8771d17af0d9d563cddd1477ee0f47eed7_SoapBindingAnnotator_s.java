 package org.codehaus.xfire.wsdl11.parser;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.wsdl.Binding;
 import javax.wsdl.BindingFault;
 import javax.wsdl.BindingInput;
 import javax.wsdl.BindingOperation;
 import javax.wsdl.BindingOutput;
 import javax.wsdl.Fault;
 import javax.wsdl.Input;
 import javax.wsdl.Message;
 import javax.wsdl.Output;
 import javax.wsdl.Part;
 import javax.wsdl.Port;
 import javax.wsdl.extensions.soap.SOAPAddress;
 import javax.wsdl.extensions.soap.SOAPBinding;
 import javax.wsdl.extensions.soap.SOAPBody;
 import javax.wsdl.extensions.soap.SOAPHeader;
 import javax.wsdl.extensions.soap.SOAPOperation;
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.service.Endpoint;
 import org.codehaus.xfire.service.FaultInfo;
 import org.codehaus.xfire.service.MessageInfo;
 import org.codehaus.xfire.service.MessagePartContainer;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.soap.AbstractSoapBinding;
 import org.codehaus.xfire.soap.Soap11Binding;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.wsdl.SchemaType;
 
 public class SoapBindingAnnotator extends BindingAnnotator
 {
     private AbstractSoapBinding soapBinding;
     private boolean useSet = false;
     
     protected org.codehaus.xfire.service.Binding getBinding()
     {
         return soapBinding;
     }
 
     public AbstractSoapBinding getSoapBinding()
     {
         return soapBinding;
     }
 
     public void setSoapBinding(AbstractSoapBinding soapBinding)
     {
         this.soapBinding = soapBinding;
     }
 
     protected void visit(Binding wbinding)
     {
         SOAPBinding sbind = DefinitionsHelper.getSOAPBinding(wbinding);
 
         soapBinding = new Soap11Binding(wbinding.getQName(), sbind.getTransportURI(), getService());
 
         getService().addBinding(soapBinding);
         
         soapBinding.setStyle(null);
         setStyle(sbind.getStyle());
     }
 
     protected void visit(BindingFault bindingFault, Fault fault, FaultInfo msg)
     {
         visitMessage(msg, bindingFault.getExtensibilityElements());
     }
 
     protected void visit(BindingInput bindingInput, Input input, MessageInfo msg)
     {
         visitMessage(msg, bindingInput.getExtensibilityElements());
     }
 
     private void visitMessage(MessagePartContainer msg, List ext)
     {
         for (Iterator itr = ext.iterator(); itr.hasNext();)
         {
             Object o = itr.next();
 
             if (o instanceof SOAPBody)
             {
                 SOAPBody body = (SOAPBody) o;
                 
                 setUse(msg, body.getUse());
             }
             else if (o instanceof SOAPHeader)
             {
                 SOAPHeader header = (SOAPHeader) o;
 
                 QName msgName = header.getMessage();
                 Message hmsg = getDefinition().getMessage(msgName);
                 Part part = hmsg.getPart(header.getPart());
                 
                 QName name = part.getElementName();
                 if (name == null)
                 {
                    name = part.getTypeName();
                 }
                 
                SchemaType st = getBindingProvider().getSchemaType(name, getService());
                 
                 MessagePartInfo info = getSoapBinding().getHeaders((MessageInfo) msg).addMessagePart(name, null);
                 info.setSchemaType(st);
             }
         }
     }
 
     protected void visit(BindingOperation operation, OperationInfo opInfo)
     {
         SOAPOperation soapOp = DefinitionsHelper.getSOAPOperation(operation);
         
         AbstractSoapBinding binding = getSoapBinding();
         
         if (soapOp != null)
         {
             binding.setSoapAction(opInfo, soapOp.getSoapActionURI());
     
             String style = soapOp.getStyle();
             if (style != null)
             {
                 setStyle(style);
             }
         }
         
         binding.setSerializer(opInfo, AbstractSoapBinding.getSerializer(binding.getStyle(), binding.getUse()));
     }
 
     protected void setStyle(String style)
     {
         if (getService().getServiceInfo().isWrapped())
             style = SoapConstants.STYLE_WRAPPED;
         
         String current = getSoapBinding().getStyle();
 
         if (current == null)
         {
             getSoapBinding().setStyle(style);
         }
         else
         {
             if (!current.equals(style))
                 throw new XFireRuntimeException("Multiple styles not supported at this time.");
         }
     }
 
     protected void setUse(MessagePartContainer msg, String use)
     {
         String current = getSoapBinding().getUse();
         
         if (!useSet)
         {
             getSoapBinding().setUse(use);
             useSet = true;
         }
         else
         {
             if (!current.equals(use))
                 throw new XFireRuntimeException("Multiple uses not supported at this time.");
         }
     }
     
     protected void visit(BindingOutput bindingOutput, Output output, MessageInfo msg)
     {
         visitMessage(msg, bindingOutput.getExtensibilityElements());
     }
 
     protected boolean isUnderstood(Binding op)
     {
         SOAPBinding ee = DefinitionsHelper.getSOAPBinding(op);
         
         return ee != null;
     }
 
     protected void visit(Port port)
     {
         SOAPAddress add = DefinitionsHelper.getSOAPAddress(port);
         SOAPBinding sbind = DefinitionsHelper.getSOAPBinding(port.getBinding());
         
         org.codehaus.xfire.service.Binding binding = 
             getService().getBinding(port.getBinding().getQName());
 
         if (binding != null)
         {
             Endpoint ep = new Endpoint(new QName(getService().getTargetNamespace(), 
                                                  port.getName()), 
                                        binding, 
                                        add.getLocationURI());
             
             getService().addEndpoint(ep);
         }
     }
 }
