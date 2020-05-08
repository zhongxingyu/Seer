 package org.codehaus.xfire.annotations.jsr181;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 import javax.jws.HandlerChain;
 import javax.jws.Oneway;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 
 import org.codehaus.xfire.annotations.HandlerChainAnnotation;
 import org.codehaus.xfire.annotations.WebAnnotations;
 import org.codehaus.xfire.annotations.WebMethodAnnotation;
 import org.codehaus.xfire.annotations.WebParamAnnotation;
 import org.codehaus.xfire.annotations.WebResultAnnotation;
 import org.codehaus.xfire.annotations.WebServiceAnnotation;
 import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;
 
 public class Jsr181WebAnnotations
         implements WebAnnotations
 {
     public boolean hasWebServiceAnnotation(Class clazz)
     {
         return clazz.isAnnotationPresent(WebService.class);
     }
 
     public WebServiceAnnotation getWebServiceAnnotation(Class clazz)
     {
         WebService webService = (WebService) clazz.getAnnotation(WebService.class);
         if (webService != null)
         {
             WebServiceAnnotation annotation = new WebServiceAnnotation();
             annotation.setEndpointInterface(webService.endpointInterface());
             annotation.setName(webService.name());
             annotation.setServiceName(webService.serviceName());
             annotation.setTargetNamespace(webService.targetNamespace());
             annotation.setPortName(webService.portName());
            annotation.setWsdlLocation(webService.wsdlLocation());
             
             return annotation;
         }
         else
         {
             return null;
         }
     }
 
     public boolean hasWebMethodAnnotation(Method method)
     {
         return method.isAnnotationPresent(WebMethod.class);
     }
 
     public WebMethodAnnotation getWebMethodAnnotation(Method method)
     {
         WebMethod webMethod = (WebMethod) method.getAnnotation(WebMethod.class);
         if (webMethod != null)
         {
             WebMethodAnnotation annotation = new WebMethodAnnotation();
             annotation.setAction(webMethod.action());
             annotation.setOperationName(webMethod.operationName());
             annotation.setExclude(webMethod.exclude());
             
             return annotation;
         }
         else
         {
             return null;
         }
     }
 
     public boolean hasWebResultAnnotation(Method method)
     {
         return method.isAnnotationPresent(WebResult.class);
     }
 
     public WebResultAnnotation getWebResultAnnotation(Method method)
     {
         Annotation[][] annotations = method.getParameterAnnotations();
         WebResult webResult = (WebResult) method.getAnnotation(WebResult.class);
         if (webResult != null)
         {
             WebResultAnnotation annot = new WebResultAnnotation();
             annot.setName(webResult.name());
             annot.setTargetNamespace(webResult.targetNamespace());
             annot.setHeader(webResult.header());
             annot.setPartName(webResult.partName());
             
             return annot;
         }
         else
         {
             return null;
         }
     }
 
     public boolean hasWebParamAnnotation(Method method, int parameter)
     {
         Annotation[][] annotations = method.getParameterAnnotations();
         if (parameter >= annotations.length)
         {
             return false;
         }
         else
         {
             for (int i = 0; i < annotations[parameter].length; i++)
             {
                 Annotation annotation = annotations[parameter][i];
                 if (annotation.annotationType().equals(WebParam.class))
                 {
                     return true;
                 }
             }
             return false;
         }
     }
 
     public WebParamAnnotation getWebParamAnnotation(Method method, int parameter)
     {
         Annotation[][] annotations = method.getParameterAnnotations();
         if (parameter >= annotations.length)
         {
             return null;
         }
         WebParam webParam = null;
         for (int i = 0; i < annotations[parameter].length; i++)
         {
             Annotation annotation = annotations[parameter][i];
             if (annotation.annotationType().equals(WebParam.class))
             {
                 webParam = (WebParam) annotations[parameter][i];
                 break;
             }
         }
         if (webParam != null)
         {
             WebParamAnnotation annot = new WebParamAnnotation();
             annot.setName(webParam.name());
             annot.setTargetNamespace(webParam.targetNamespace());
             annot.setHeader(webParam.header());
             annot.setPartName(webParam.partName());
             
             if (webParam.mode() == WebParam.Mode.IN)
             {
                 annot.setMode(WebParamAnnotation.MODE_IN);
             }
             else if (webParam.mode() == WebParam.Mode.INOUT)
             {
                 annot.setMode(WebParamAnnotation.MODE_INOUT);
             }
             else if (webParam.mode() == WebParam.Mode.OUT)
             {
                 annot.setMode(WebParamAnnotation.MODE_OUT);
             }
 
             return annot;
         }
         else
         {
             return null;
         }
     }
 
     public boolean hasOnewayAnnotation(Method method)
     {
         return method.isAnnotationPresent(Oneway.class);
     }
 
     public boolean hasSOAPBindingAnnotation(Class clazz)
     {
         return clazz.isAnnotationPresent(SOAPBinding.class);
     }
 
     public SOAPBindingAnnotation getSOAPBindingAnnotation(Class clazz)
     {
         SOAPBinding binding = (SOAPBinding) clazz.getAnnotation(SOAPBinding.class);
 
         SOAPBindingAnnotation annot = null;
         if (binding != null)
         {
             annot = new SOAPBindingAnnotation();
             if (binding.parameterStyle() == SOAPBinding.ParameterStyle.BARE)
             {
                 annot.setParameterStyle(SOAPBindingAnnotation.PARAMETER_STYLE_BARE);
             }
             else if (binding.parameterStyle() == SOAPBinding.ParameterStyle.WRAPPED)
             {
                 annot.setParameterStyle(SOAPBindingAnnotation.PARAMETER_STYLE_WRAPPED);
             }
 
             if (binding.style() == SOAPBinding.Style.DOCUMENT)
             {
                 annot.setStyle(SOAPBindingAnnotation.STYLE_DOCUMENT);
             }
             else if (binding.style() == SOAPBinding.Style.RPC)
             {
                 annot.setStyle(SOAPBindingAnnotation.STYLE_RPC);
             }
 
             if (binding.use() == SOAPBinding.Use.ENCODED)
             {
                 annot.setUse(SOAPBindingAnnotation.USE_ENCODED);
             }
             else if (binding.use() == SOAPBinding.Use.LITERAL)
             {
                 annot.setUse(SOAPBindingAnnotation.USE_LITERAL);
             }
         }
 
         return annot;
     }
 
     public boolean hasHandlerChainAnnotation(Class clazz)
     {
         return clazz.isAnnotationPresent(HandlerChain.class);
     }
 
     public HandlerChainAnnotation getHandlerChainAnnotation(Class clazz)
     {
         HandlerChain handlerChain = (HandlerChain) clazz.getAnnotation(HandlerChain.class);
         HandlerChainAnnotation annotation = null;
         if (handlerChain != null)
         {
             annotation = new HandlerChainAnnotation(handlerChain.file(), 
                                                     handlerChain.name());
         }
         return annotation;
     }
 }
