 package org.codehaus.xfire.annotations;
 
 import java.lang.reflect.Method;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.service.object.ObjectService;
 import org.codehaus.xfire.service.object.ObjectServiceFactory;
 import org.codehaus.xfire.soap.SoapVersion;
 import org.codehaus.xfire.transport.TransportManager;
 import org.codehaus.xfire.type.TypeMappingRegistry;
 import org.codehaus.xfire.util.NamespaceHelper;
 import org.codehaus.xfire.wsdl11.builder.WSDLBuilderInfo;
 
 /**
  * Annotations-bases implementation of the {@link ServiceFactory} interface.
  *
  * @author Arjen Poutsma
  */
 public class AnnotationServiceFactory
         extends ObjectServiceFactory
         implements ServiceFactory
 {
     private WebAnnotations webAnnotations;
 
     /**
      * Initializes a new instance of the <code>AnnotationServiceFactory</code> with the given annotations facade,
      * transport manager and type mapping registry.
      *
      * @param webAnnotations   the annotations facade
      * @param transportManager the transport manager
      * @param registry         the registry
      */
     public AnnotationServiceFactory(WebAnnotations webAnnotations, final TransportManager transportManager,
                                     final TypeMappingRegistry registry)
     {
         super(transportManager, registry);
         this.webAnnotations = webAnnotations;
     }
 
     /**
      * Creates a service from the specified class. If the class has a {@link SOAPBindingAnnotation}, it will be used to
      * define the style and use of the service. Otherwise, the {@link ObjectServiceFactory#create(Class) default
      * behaviour} will be used.
      *
      * @param clazz The service class used to populate the operations and parameters.
      * @return The service.
      */
     public Service create(final Class clazz)
     {
         if (webAnnotations.hasSOAPBindingAnnotation(clazz))
         {
             SOAPBindingAnnotation soapBindingAnnotation = webAnnotations.getSOAPBindingAnnotation(clazz);
             return create(clazz,
                           null,
                           soapBindingAnnotation.getStyleString(),
                           soapBindingAnnotation.getUseString());
         }
         else
         {
             return super.create(clazz);
         }
     }
 
     /**
      * Creates a service from the specified class, soap version, style and use. If the class has a {@link
      * WebServiceAnnotation}, it will be used to define the name, service name, target namespace. If the annotation
      * defines an endpoint interface, all methods of that interface are exposed as operations. If no endpoint interface
      * is defined, all methods that have the {@link WebMethodAnnotation} are exposed.
      *
      * @param clazz   The service class used to populate the operations and parameters.
      * @param version The soap version. If <code>null</code>, {@link org.codehaus.xfire.soap.Soap11} will be used.
      * @param style   The service style. If <code>null</code>, {@link org.codehaus.xfire.soap.SoapConstants#STYLE_WRAPPED}
      *                will be used.
      * @param use     The service use. If <code>null</code>, {@link org.codehaus.xfire.soap.SoapConstants#USE_LITERAL}
      *                will be used.
      */
     public Service create(Class clazz, SoapVersion version, String style, String use)
     {
         if (webAnnotations.hasWebServiceAnnotation(clazz))
         {
             WebServiceAnnotation webServiceAnnotation = webAnnotations.getWebServiceAnnotation(clazz);
 
             String serviceName = createServiceName(clazz, webServiceAnnotation);

             /* Attempt to load the endpoint interface if there is one. If there is an endpoint
             * interface the attribute WebService.serviceName is the only valid one for the
              * implementing bean class.
              */
             String tns = null;
             String portType = null;
             Class endpointInterface = clazz;
            if (webServiceAnnotation.getEndpointInterface() != null &&
                    webServiceAnnotation.getEndpointInterface().length() != 0)
             {
                 try
                 {
                     endpointInterface = loadClass(webServiceAnnotation.getEndpointInterface());
                     WebServiceAnnotation endpointWSAnnotation =
                             webAnnotations.getWebServiceAnnotation(endpointInterface);
 
                     tns = createServiceNamespace(endpointInterface, endpointWSAnnotation);
                     portType = createPortType(serviceName, endpointWSAnnotation);
                 }
                 catch (ClassNotFoundException e)
                 {
                    throw new XFireRuntimeException("Couldn't find endpoint interface " +
                                                    webServiceAnnotation.getEndpointInterface(), e);
                 }
             }
             else
             {
                 tns = createServiceNamespace(endpointInterface, webServiceAnnotation);
                 portType = createPortType(serviceName, webServiceAnnotation);
             }
 
             Service service = create(endpointInterface, serviceName, tns, version, style, use, null);

             // Fill in WSDL Builder metadata from annotations.
             WSDLBuilderInfo info = new WSDLBuilderInfo(service);
             info.setTargetNamespace(tns);
             info.setServiceName(serviceName);
             info.setPortType(portType);
             service.setProperty(WSDLBuilderInfo.KEY, info);
 
             if (clazz != endpointInterface)
             {
                 service.setProperty(ObjectService.SERVICE_IMPL_CLASS, clazz);
             }
 
             return service;
         }
         else
         {
             throw new XFireRuntimeException("Class " + clazz.getName() + " does not have a WebService annotation");
         }
     }
 
     /**
      * Attempt to load a class first from this class's ClassLoader, then from the context classloader.
      *
      * @param endpointInterface
      * @return
      * @throws ClassNotFoundException
      */
     protected Class loadClass(String endpointInterface)
             throws ClassNotFoundException
     {
         try
         {
             return getClass().getClassLoader().loadClass(endpointInterface);
         }
         catch (ClassNotFoundException e)
         {
             try
             {
                 return Thread.currentThread().getContextClassLoader().loadClass(endpointInterface);
             }
             catch (ClassNotFoundException e1)
             {
                 throw e;
             }
         }
     }
 
     protected String createServiceNamespace(Class clazz, WebServiceAnnotation webServiceAnnotation)
     {
         String ns = null;
         if (webServiceAnnotation.getTargetNamespace() != null)
         {
             ns = webServiceAnnotation.getTargetNamespace();
         }
         else
         {
             ns = NamespaceHelper.makeNamespaceFromClassName(clazz.getName(), "http");
         }
         return ns;
     }
 
     protected String createServiceName(Class clazz, WebServiceAnnotation webServiceAnnotation)
     {
         String name = null;
         if (webServiceAnnotation.getServiceName() != null)
         {
             name = webServiceAnnotation.getServiceName();
         }
         else
         {
             name = makeServiceNameFromClassName(clazz);
         }
         return name;
     }
 
     protected String createPortType(String serviceName, WebServiceAnnotation webServiceAnnotation)
     {
         String portType = null;
         if (webServiceAnnotation.getName() != null)
         {
             portType = webServiceAnnotation.getName();
         }
         else
         {
             portType = serviceName + "PortType";
         }
 
         return portType;
     }
 
     /**
      * Returns <code>true</code> if the specified method is valid for a SOAP operation.
      *
      * @param method the method.
      * @return <code>true</code> if valid; <code>false</code> otherwise.
      */
     protected boolean isValidMethod(Method method)
     {
         if (!super.isValidMethod(method))
             return false;
         
         // All methods on endpoint interfaces are valid WebMethods.
         if (method.getDeclaringClass().isInterface())
             return true;
 
         return webAnnotations.hasWebMethodAnnotation(method);
     }
 
     protected String getInParameterName(Method method, int paramNumber, boolean doc)
     {
         if (webAnnotations.hasWebParamAnnotation(method, paramNumber))
         {
             final WebParamAnnotation webParamAnnotation = webAnnotations.getWebParamAnnotation(method, paramNumber);
 
             return webParamAnnotation.getName();
         }
         else
         {
             return super.getInParameterName(method, paramNumber, doc);
         }
     }
 
     protected String getOutParameterName(Method method, boolean doc)
     {
         if (webAnnotations.hasWebResultAnnotation(method))
         {
             final WebResultAnnotation webResultAnnotation = webAnnotations.getWebResultAnnotation(method);
 
             return webResultAnnotation.getName();
         }
         else
         {
             return super.getOutParameterName(method, doc);
         }
     }
 
     protected boolean isAsync(Method method)
     {
         return webAnnotations.hasOnewayAnnotation(method);
     }
 }
