 package org.codehaus.xfire.gen.jsr181;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jws.Oneway;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 import javax.jws.soap.SOAPBinding.ParameterStyle;
 import javax.jws.soap.SOAPBinding.Style;
 import javax.jws.soap.SOAPBinding.Use;
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.gen.GenerationContext;
 import org.codehaus.xfire.service.Binding;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceInfo;
 import org.codehaus.xfire.soap.AbstractSoapBinding;
 import org.codehaus.xfire.soap.SoapConstants;
 
 import com.sun.codemodel.ClassType;
 import com.sun.codemodel.JAnnotationUse;
 import com.sun.codemodel.JDefinedClass;
 import com.sun.codemodel.JMethod;
 import com.sun.codemodel.JVar;
 
 
 public class ServiceInterfaceGenerator
     extends AbstractServiceGenerator
 {
     public final static String SERVICE_INTERFACE = "service.interface";
     private JAnnotationUse wpann;
     private JAnnotationUse wmAnn;
     
     private List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();
 
     @Override
     public void generate(GenerationContext context, Service service)
         throws Exception
     {
         if (service.getEndpoints().size() == 0) return;
         
         super.generate(context, service);
     }
 
     protected boolean isWritten(GenerationContext context)
     {
         if (serviceInfos.contains(getCurrentService().getServiceInfo()))
         {
             return true;
         }
         
         serviceInfos.add(getCurrentService().getServiceInfo());
         return false;
     }
     
     protected ClassType getClassType()
     {
         return ClassType.INTERFACE;
     }
 
     protected void annotate(GenerationContext context, Service service, JDefinedClass jc, Binding binding)
     {
         JAnnotationUse ann = jc.annotate(WebService.class);
         
         ann.param("name", service.getServiceInfo().getPortType().getLocalPart());
         ann.param("targetNamespace", service.getServiceInfo().getPortType().getNamespaceURI());
         
         service.setProperty(SERVICE_INTERFACE, jc);
         
         AbstractSoapBinding soapBinding = (AbstractSoapBinding) binding;
         ann = jc.annotate(SOAPBinding.class);
         if (soapBinding.getStyle().equals(SoapConstants.STYLE_DOCUMENT)) 
         {
             ann.param("style", Style.DOCUMENT);
         }
         else if (soapBinding.getStyle().equals(SoapConstants.STYLE_RPC)) 
         {
             ann.param("style", Style.RPC);
         }
         
         ann.param("use", Use.LITERAL);
         
         if (service.getServiceInfo().isWrapped())
         {
             ann.param("parameterStyle", ParameterStyle.WRAPPED);
         }
         else
         {
            ann.param("parameterStyle", ParameterStyle.BARE);
         }
     }
 
     protected void annotate(GenerationContext context, OperationInfo op, JMethod method)
     {
         wmAnn = method.annotate(WebMethod.class);
         wmAnn.param("operationName", op.getName());
     }
     
     protected void annotate(GenerationContext context, OperationInfo op, JMethod method, Binding binding)
     {
         if (binding instanceof AbstractSoapBinding)
         {
             String action = ((AbstractSoapBinding) binding).getSoapAction(op);
 
             if (action != null) wmAnn.param("action", action);
         }
     }
 
     protected void annotateOutParam(MessagePartInfo part, JVar jvar)
     {
         JAnnotationUse wpann = jvar.annotate(WebParam.class);
         wpann.param("name", part.getName().getLocalPart());
         wpann.param("targetNamespace", part.getName().getNamespaceURI());
         wpann.param("mode", WebParam.Mode.OUT);
     }
     
     protected void annotateOutParam(MessagePartInfo part, JVar jvar, Binding binding)
     {
         JAnnotationUse wpann = jvar.annotate(WebParam.class);
         wpann.param("name", part.getName().getLocalPart());
         wpann.param("targetNamespace", part.getName().getNamespaceURI());
         wpann.param("mode", WebParam.Mode.OUT);
         wpann.param("header", true);
     }
 
     protected void annotateReturnType(JMethod method, MessagePartInfo returnPart)
     {
         JAnnotationUse wrAnn = method.annotate(WebResult.class);
         
         wrAnn.param("name", returnPart.getName().getLocalPart());
         wrAnn.param("targetNamespace", returnPart.getName().getNamespaceURI());
     }
 
     @Override
     protected void annotateOneWay(JMethod method) 
     {
         method.annotate(Oneway.class);
     }
 
     protected void annotate(MessagePartInfo part, JVar jvar)
     {
         wpann = jvar.annotate(WebParam.class);
         wpann.param("name", part.getName().getLocalPart());
         wpann.param("targetNamespace", part.getName().getNamespaceURI());
     }
 
     protected void annotate(MessagePartInfo part, JVar jvar, Binding binding)
     {
         wpann = jvar.annotate(WebParam.class);
         wpann.param("name", part.getName().getLocalPart());
         wpann.param("targetNamespace", part.getName().getNamespaceURI());
         wpann.param("header", true);
     }
     
     @Override
     protected String getClassName(GenerationContext context, Service service)
     {
         QName name = service.getServiceInfo().getPortType();
         String pckg = getPackage(service.getName(), context);
         String clsName = javify(name.getLocalPart());
 
         return pckg + "." + clsName;
     }
 }
