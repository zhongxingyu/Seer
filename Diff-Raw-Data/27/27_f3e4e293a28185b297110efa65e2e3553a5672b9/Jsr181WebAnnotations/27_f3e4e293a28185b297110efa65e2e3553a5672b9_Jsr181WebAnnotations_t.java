 package org.codehaus.xfire.annotations.jsr181;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 
 import org.codehaus.xfire.annotations.*;
 
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
             annot.setTargetNameSpace(webResult.targetNamespace());
 
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
 
 }
