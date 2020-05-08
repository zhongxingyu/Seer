 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.test;
 
 import org.mule.DefaultMuleMessage;
 import org.mule.api.MuleContext;
 import org.mule.api.MuleMessage;
 import org.mule.ibeans.IBeansException;
 import org.mule.ibeans.api.client.State;
 import org.mule.ibeans.api.client.Template;
 import org.mule.ibeans.api.client.params.InvocationContext;
 import org.mule.ibeans.internal.client.IBeanParamsHelper;
 import org.mule.ibeans.internal.client.IntegrationBeanInvocationHandler;
 import org.mule.ibeans.internal.client.TemplateAnnotationHandler;
 import org.mule.model.seda.SedaService;
 import org.mule.module.xml.transformer.XmlPrettyPrinter;
 
 import java.beans.ExceptionListener;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import javax.activation.DataSource;
 
 import org.w3c.dom.Document;
 
 /**
  * The proxy handler used to handle calls made to an iBean proxy generated using the {@link org.mule.ibeans.api.client.MockIntegrationBean}annotation.
  */
 public class MockIBeanHandler extends IntegrationBeanInvocationHandler implements MockIBean
 {
     protected ExceptionListener exceptionListener;
 
     protected IBeanParamsHelper helper;
 
     protected MuleContext muleContext;
 
     protected InvocationContext ctx;
 
     protected Object mock;
 
     protected Class ibeanInterface;
 
     //We can still process Template methods in a Mock
     protected TemplateAnnotationHandler templateHandler;
 
     protected String mime;
 
 
     public MockIBeanHandler(Class iface, MuleContext muleContext, Object mock)
     {
         super(iface, new SedaService(), muleContext);
 
         ibeanInterface = iface;
         this.muleContext = muleContext;
         this.mock = mock;
         helper = new IBeanParamsHelper(muleContext, iface);
         templateHandler = new TemplateAnnotationHandler(muleContext);
     }
 
     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
     {
         if (method.getName().equals("toString"))
         {
             return toString();
         }
         else if (method.getName().equals("setExceptionListener"))
         {
             exceptionListener = (ExceptionListener) args[0];
             return null;
         }
         else if (method.isAnnotationPresent(State.class))
         {
             ctx = helper.createInvocationContext(method, args);
             return null;
         }
         else if (method.getName().startsWith("ibean"))
         {
             return method.invoke(this, args);
         }
         //Lets process Template as normal since mocking this behaviour doesn't help us much
         else if (method.isAnnotationPresent(Template.class))
         {
             getTemplateHandler().getEvals().put(method.toString(), method.getAnnotation(Template.class).value());
             ctx = helper.createInvocationContext(method, args);
             MuleMessage message = helper.createMessage(ctx);
             MuleMessage result = getTemplateHandler().invoke(proxy, method, args, message);
             return result.getPayload(method.getReturnType());
         }
         else
         {
             Object object = method.invoke(mock, args);
            ibeanErrorCheck(object, mime);
             return object;
         }
     }
 
     public void ibeanSetMimeType(String mime)
     {
         this.mime = mime;
     }
 
     public void ibeanErrorCheck(Object data, String mimeType) throws Exception
     {
         if (mimeType == null)
         {
             throw new IllegalArgumentException("Content mime type needs to be set when checking for errors on a Mock IBean");
         }
         MuleMessage result = new DefaultMuleMessage(data, muleContext);
         result.setProperty("Content-Type", mimeType);
 
         if (isErrorReply(null, data, result))
         {
             //TODO URGENT remove add dependency to Xml
             String msg;
             if (result.getPayload() instanceof Document)
             {
                 msg = (String) new XmlPrettyPrinter().transform(result.getPayload());
             }
             else
             {
                 msg = result.getPayloadAsString();
             }
             Exception e = createCallException(result, new IBeansException(msg), "mock");
             if (exceptionListener != null)
             {
                 exceptionListener.exceptionThrown(e);
             }
             else
             {
                 throw e;
             }
         }
     }
 
     public Class ibeanReturnType()
     {
         if (ctx == null || ctx.getReturnType().getName().equals("void"))
         {
             return helper.getReturnType();
         }
         else
         {
             return ctx.getReturnType();
         }
     }
 
     public Object ibeanUriParam(String name)
     {
         if (ctx == null)
         {
             return helper.getDefaultUriParams().get(name);
         }
         else
         {
             return ctx.getUriParams().get(name);
         }
     }
 
     public Object ibeanHeaderParam(String name)
     {
         if (ctx == null)
         {
             return helper.getDefaultHeaderParams().get(name);
         }
         else
         {
             return ctx.getHeaderParams().get(name);
         }
     }
 
     public Object ibeanPropertyParam(String name)
     {
         if (ctx == null)
         {
             return helper.getDefaultPropertyParams().get(name);
         }
         else
         {
             return ctx.getPropertyParams().get(name);
         }
     }
 
     public Object ibeanPayloadParam(String name)
     {
         if (ctx == null)
         {
             return helper.getDefaultPayloadParams().get(name);
         }
         else
         {
             return ctx.getPayloadParams().get(name);
         }
     }
 
     public List<Object> ibeanPayloads()
     {
         if (ctx == null)
         {
             return null;
         }
         else
         {
             return ctx.getPayloads();
         }
     }
 
     public List<DataSource> ibeanAttachments()
     {
         if (ctx == null)
         {
             return null;
         }
         else
         {
             return ctx.getAttachments();
         }
     }
 
     public ExceptionListener getExceptionListener()
     {
         return exceptionListener;
     }
 
     protected void checkCtx()
     {
         if (ctx == null)
         {
             throw new IllegalStateException("No method called yet");
         }
     }
 
     public TemplateAnnotationHandler getTemplateHandler()
     {
         return templateHandler;
     }
 }
