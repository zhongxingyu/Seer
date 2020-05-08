 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.ibeans.internal.client;
 
 import org.mule.api.MuleContext;
 import org.mule.api.MuleException;
 import org.mule.api.MuleMessage;
 import org.mule.api.endpoint.ImmutableEndpoint;
 import org.mule.api.routing.InterfaceBinding;
 import org.mule.api.routing.filter.Filter;
 import org.mule.api.service.Service;
 import org.mule.api.transformer.Transformer;
 import org.mule.api.transformer.TransformerException;
 import org.mule.config.ExceptionHelper;
 import org.mule.config.i18n.CoreMessages;
 import org.mule.ibeans.IBeansException;
 import org.mule.ibeans.api.client.AbstractCallInterceptor;
 import org.mule.ibeans.api.client.CallException;
 import org.mule.ibeans.api.client.CallInterceptor;
 import org.mule.ibeans.api.client.Interceptor;
 import org.mule.ibeans.api.client.Return;
 import org.mule.ibeans.api.client.params.InvocationContext;
 import org.mule.routing.filters.ExpressionFilter;
 import org.mule.transport.NullPayload;
 import org.mule.util.StringMessageUtils;
import org.mule.util.StringUtils;
 import org.mule.util.TemplateParser;
 
 import com.sun.org.apache.xml.internal.serialize.OutputFormat;
 import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 
 import java.beans.ExceptionListener;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 
 /**
  * The proxy handler responsible for making calls on behalf of the the IntegrationBean.  This handler maintains any state
  * and parses any parameter annotations before making the call.
  */
 public class IntegrationBeanInvocationHandler implements InvocationHandler, Serializable
 {
     protected static transient Log logger = LogFactory.getLog(IntegrationBeanInvocationHandler.class);
 
     protected transient MuleContext muleContext;
 
     protected transient TemplateAnnotationHandler templateHandler;
     protected transient CallAnnotationHandler callHandler;
 
     protected transient ExceptionListener exceptionListener;
 
     protected transient IBeanParamsHelper helper;
 
     protected transient TemplateParser parser = TemplateParser.createCurlyBracesStyleParser();
 
     protected transient List<CallInterceptor> defaultInterceptorList = new ArrayList<CallInterceptor>();
 
     protected transient CallInterceptor invoker;
 
     protected transient Map<Method, List<CallInterceptor>> interceptorListCache = new HashMap<Method, List<CallInterceptor>>();
 
     public IntegrationBeanInvocationHandler(Class iface, Service service, MuleContext muleContext) throws IBeansException
     {
         if (muleContext == null)
         {
             throw new IBeansException(CoreMessages.objectIsNull("MuleContext").toString());
         }
 
         if (service == null)
         {
             throw new IBeansException(CoreMessages.objectIsNull("Service").toString());
         }
 
         if (iface == null)
         {
             throw new IBeansException(CoreMessages.objectIsNull("IBean Interface").toString());
         }
 
         this.muleContext = muleContext;
         helper = new IBeanParamsHelper(muleContext, iface);
         templateHandler = new TemplateAnnotationHandler(muleContext);
         callHandler = new CallAnnotationHandler(muleContext, service, helper);
 
         // Performs special handling for standard and non-integration methods
         defaultInterceptorList.add(new NonIntegrationMethodsCallInterceptor());
         // Populates invocationContext with field and method level params
         defaultInterceptorList.add(new PopulateiBeansParamsInterceptor());
         // Adds default endpoint properties so they are available to any ParmFactory's
         defaultInterceptorList.add(new DefaultEndpointPropertiesInterceptor());
         // 
         defaultInterceptorList.add(new StateCallInterceptor());
         defaultInterceptorList.add(new ResponseTransformInterceptor());
         defaultInterceptorList.add(new ProcessErrorsInterceptor());
         defaultInterceptorList.add(new IntegrationBeanInvokerInterceptor());
 
     }
 
     public void addRouter(InterfaceBinding router)
     {
         callHandler.addRouterForInterface(router);
 
     }
 
     public TemplateAnnotationHandler getTemplateHandler()
     {
         return templateHandler;
     }
 
     public CallAnnotationHandler getCallHandler()
     {
         return callHandler;
     }
 
     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
     {
         if (interceptorListCache.get(method) == null)
         {
             if (method.getAnnotation(Interceptor.class) != null)
             {
                 Interceptor interceptorAnnotation = method.getAnnotation(Interceptor.class);
                 List interceptors = new ArrayList<CallInterceptor>();
                 interceptors.addAll(defaultInterceptorList);
                 interceptors.add(interceptors.size() - 3, interceptorAnnotation.value().newInstance());
                 interceptorListCache.put(method, interceptors);
             }
             else
             {
                 interceptorListCache.put(method, defaultInterceptorList);
             }
         }
 
         InternalInvocationContext invocationContext = new InternalInvocationContext(proxy, method, args,
                 muleContext, exceptionListener, interceptorListCache.get(method));
 
         invocationContext.proceed();
 
         if (invocationContext.exceptionThrown())
         {
             invocationContext.rethrowException();
         }
 
         return invocationContext.result;
     }
 
     protected Object handleReturnAnnotation(String expr, MuleMessage message, InvocationContext ctx)
     {
         if (parser.isContainsTemplate(expr))
         {
             expr = parser.parse(ctx.getUriParams(), expr);
             expr = parser.parse(ctx.getRequestHeaderParams(), expr);
             expr = parser.parse(ctx.getPropertyParams(), expr);
         }
 
         if (Boolean.class.equals(ctx.getReturnType()) || boolean.class.equals(ctx.getReturnType()))
         {
             ExpressionFilter filter = new ExpressionFilter(expr);
             filter.setMuleContext(muleContext);
             return filter.accept(message);
         }
         else
         {
             return muleContext.getExpressionManager().evaluate(expr, message);
         }
     }
 
     protected boolean isErrorReply(Method method, Object finalResult, MuleMessage message)
     {
         if (finalResult == null || finalResult.equals(""))
         {
             return false;
         }
         MuleMessage test = message;
         if (finalResult instanceof MuleMessage)
         {
             test = (MuleMessage) finalResult;
         }
         else
         {
             test.setPayload(finalResult);
         }
         String mime = getMimeForMessage(test);
         Filter f = helper.getMethodErrorFilters().get(method);
         if (f == null)
         {
             f = helper.getErrorFilters().get(mime);
         }
 
         if (f == null)
         {
             if (logger.isDebugEnabled())
             {
                 logger.debug("No matching error filter for Mime Type: " + mime);
             }
             return false;
         }
         else
         {
             return f.accept(test);
         }
     }
 
     protected String getMimeForMessage(MuleMessage message)
     {
         String mime = (String) message.getProperty("Content-Type");
         if (mime == null)
         {
             mime = "*";
         }
         else if (mime.indexOf(";") > -1)
         {
             mime = mime.substring(0, mime.indexOf(";"));
         }
         return mime;
     }
 
     protected CallException createCallException(MuleMessage message, Throwable t, String protocol)
     {
         if (t instanceof InvocationTargetException || t instanceof UndeclaredThrowableException)
         {
             t = t.getCause();
         }
         String mime = getMimeForMessage(message);
         Filter filter = helper.getErrorFilters().get(mime);
         Object errorCode = null;
         Throwable root = ExceptionHelper.getRootException(t);
         MuleException muleException = ExceptionHelper.getRootMuleException(t);
 
         if (filter instanceof ErrorExpressionFilter)
         {
             ErrorExpressionFilter f = (ErrorExpressionFilter) filter;
 
             if (f.getErrorCodeExpr() != null)
             {
                 errorCode = muleContext.getExpressionManager().evaluate(f.getErrorCodeExpr(), f.getEvaluator(),
                         message, false);
                //if errorCode is non-numeric, return the http status code instead
                if (!StringUtils.isNumeric(errorCode.toString()))
                {
                    errorCode = null;
                }
             }
        }
 
        if (errorCode == null)
        {
            String statusCodeName = ExceptionHelper.getErrorCodePropertyName(protocol);
            errorCode = message.getProperty(statusCodeName);
         }
 
         if (errorCode != null)
         {
             errorCode = errorCode.toString();
         }
         CallException ce = new CallException(t.getMessage(), (String) errorCode, root);
         for (Iterator iterator = message.getPropertyNames().iterator(); iterator.hasNext();)
         {
             String name = (String) iterator.next();
             ce.getInfo().put(name, message.getProperty(name));
         }
         if (muleException != null)
         {
             ce.getInfo().putAll(muleException.getInfo());
         }
         try
         {
             ce.getInfo().put("response.payload", message.getPayloadAsString());
         }
         catch (Exception e1)
         {
             ce.getInfo().put("exception.handler.error", e1.getMessage());
         }
         return ce;
     }
 
     protected String getScheme(InvocationContext invocationContext)
     {
         if (templateHandler != null && templateHandler.isMatch(invocationContext.getMethod()))
         {
             return templateHandler.getScheme(invocationContext.getMethod());
         }
         else
         {
             return callHandler.getScheme(invocationContext.getMethod());
         }
     }
 
     public String toString()
     {
         final StringBuffer sb = new StringBuffer();
         sb.append("IBean");
         sb.append("{Calls: ").append(getCallHandler());
         sb.append("}, {Templates: ").append(getTemplateHandler());
         sb.append('}');
         return sb.toString();
     }
 
     // Interceptors
 
     private final class ProcessErrorsInterceptor extends AbstractCallInterceptor
     {
         public void afterCall(InvocationContext invocationContext) throws Exception
         {
             Object finalResult = invocationContext.getResult();
             MuleMessage result = ((InternalInvocationContext) invocationContext).responseMuleMessage;
             String scheme = getScheme(invocationContext);
 
             if (isErrorReply(invocationContext.getMethod(), finalResult, result))
             {
                 String msg;
                 if (result.getPayload() instanceof Document)
                 {
 
                     msg = prettyPrint((Document) result.getPayload());
                 }
                 else
                 {
                     msg = result.getPayloadAsString();
                 }
                 Exception e = createCallException(result, new IBeansException(msg), scheme);
                 if (invocationContext.getExceptionListener() != null)
                 {
                     invocationContext.getExceptionListener().exceptionThrown(e);
                 }
                 else
                 {
                     throw e;
                 }
             }
         }
 
         private String prettyPrint(Document document) throws IOException
         {
             StringWriter stringWriter = new StringWriter();
             XMLSerializer serializer = new XMLSerializer(stringWriter, new OutputFormat(com.sun.org.apache.xml.internal.serialize.Method.XML, "UTF-8", true));
             serializer.serialize(document);
             return stringWriter.toString();
         }
     }
 
     private final class StateCallInterceptor extends AbstractCallInterceptor
     {
         public void intercept(InvocationContext invocationContext) throws Exception
         {
             if (invocationContext.isStateCall())
             {
                 // If this is a state call we don't need to create a message
                 // Neither do we proceed down the interceptor chain
                 return;
             }
             else
             {
                 invocationContext.proceed();
             }
         }
     }
 
     private final class DefaultEndpointPropertiesInterceptor extends AbstractCallInterceptor
     {
         public void beforeCall(InvocationContext invocationContext)
         {
             // Not keen on property munging here be the endpoint sometimes has
             // default props that we should make available to the context
             ImmutableEndpoint endpoint = callHandler.getEndpointForMethod(invocationContext.getMethod());
             if (endpoint != null && endpoint.getProperties().size() > 0)
             {
                 for (Iterator iterator = endpoint.getProperties().entrySet().iterator(); iterator.hasNext();)
                 {
                     Map.Entry entry = (Map.Entry) iterator.next();
                     invocationContext.getPropertyParams().put(entry.getKey().toString(), entry.getValue());
                 }
             }
         }
     }
 
     private final class PopulateiBeansParamsInterceptor extends AbstractCallInterceptor
     {
         public void beforeCall(InvocationContext invocationContext) throws Exception
         {
             helper.populateInvocationContext(invocationContext);
         }
     }
 
     private final class NonIntegrationMethodsCallInterceptor implements CallInterceptor
     {
         public void intercept(InvocationContext invocationContext)
         {
             if (invocationContext.getMethod().getName().equals("toString"))
             {
                 invocationContext.setResult(toString());
             }
             else if (invocationContext.getMethod().getName().equals("hashCode"))
             {
                 invocationContext.setResult(hashCode());
             }
             else if (invocationContext.getMethod().getName().equals("equals"))
             {
                 invocationContext.setResult(equals(invocationContext.getArgs()[0]));
             }
             else if (invocationContext.getMethod().getName().equals("setExceptionListener"))
             {
                 exceptionListener = (ExceptionListener) invocationContext.getArgs()[0];
             }
             else
             {
                 invocationContext.proceed();
             }
         }
     }
 
     class IntegrationBeanInvokerInterceptor implements CallInterceptor
     {
 
         public void intercept(InvocationContext invocationContext) throws Throwable
         {
             ExceptionListener exceptionListener = invocationContext.getExceptionListener();
             MuleMessage requestMessage = helper.createMessage(invocationContext);
 
             if (logger.isTraceEnabled())
             {
                 try
                 {
                     logger.trace("Message Before invoking "
                             + invocationContext.getMethod()
                             + ": \n"
                             + StringMessageUtils.truncate(
                             StringMessageUtils.toString(requestMessage.getPayload()),
                             2000, false));
                     logger.trace("Message Headers: \n"
                             + StringMessageUtils.headersToString(requestMessage));
                 }
                 catch (Exception e)
                 {
                     // ignore
                 }
             }
 
             MuleMessage result;
 
             if (templateHandler != null && templateHandler.isMatch(invocationContext.getMethod()))
             {
                 result = templateHandler.invoke(invocationContext.getProxy(),
                         invocationContext.getMethod(), invocationContext.getArgs(), requestMessage);
             }
             else
             {
                 result = callHandler.invoke(invocationContext.getProxy(),
                         invocationContext.getMethod(), invocationContext.getArgs(), requestMessage);
             }
 
             ((InternalInvocationContext) invocationContext).responseMuleMessage = result;
            if (result != null)
            {
                invocationContext.setResult(result.getPayload());
            }
 
             Method method = invocationContext.getMethod();
             String scheme = getScheme(invocationContext);
             if (result != null)
             {
                 if (result.getExceptionPayload() != null)
                 {
                     Throwable t = result.getExceptionPayload().getRootException();
                     if (exceptionListener != null)
                     {
                         if (Exception.class.isAssignableFrom(t.getClass()))
                         {
                             exceptionListener.exceptionThrown((Exception) t);
                         }
                         else
                         {
                             exceptionListener.exceptionThrown(new Exception(t));
                         }
                     }
                     else
                     {
                         t = createCallException(result, t, scheme);
                         throw t;
                     }
                 }
                 else if (result.getPayload() instanceof NullPayload
                         || method.getReturnType().equals(Void.TYPE))
                 {
                     return;
                 }
             }
         }
 
     }
 
     class ResponseTransformInterceptor extends AbstractCallInterceptor
     {
         @Override
         public void afterCall(InvocationContext invocationContext) throws Throwable
         {
             MuleMessage result = ((InternalInvocationContext) invocationContext).responseMuleMessage;
            if (result == null || NullPayload.getInstance().equals(result.getPayload()))
            {
                return;
            }

             Object finalResult = null;
             Method method = invocationContext.getMethod();
             String scheme = getScheme(invocationContext);
             if (method.getAnnotation(Return.class) != null)
             {
                 String returnExpression = method.getAnnotation(Return.class).value();
 
                 finalResult = handleReturnAnnotation(returnExpression, result, invocationContext);
 
                 if (!invocationContext.getReturnType().isInstance(finalResult))
                 {
                     Transformer transformer = muleContext.getRegistry().lookupTransformer(
                             finalResult.getClass(), invocationContext.getReturnType());
                     finalResult = transformer.transform(finalResult);
                 }
             }
             else
             {
                 Class retType = invocationContext.getReturnType();
                 if (retType.equals(MuleMessage.class))
                 {
                     finalResult = result;
                 }
                 else
                 {
                     try
                     {
                         finalResult = invocationContext.getIBeansContext().transform(result, retType);
                     }
                     catch (TransformerException e)
                     {
                         Exception ex = createCallException(result, e, scheme);
                         if (exceptionListener != null)
                         {
                             exceptionListener.exceptionThrown(ex);
                         }
                         else
                         {
                             throw ex;
                         }
                     }
                 }
             }
             invocationContext.setResult(finalResult);
         }
     }
 }
