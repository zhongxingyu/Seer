 // ========================================================================
 // Copyright 199-2004 Mort Bay Consulting Pty. Ltd.
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 package org.cipango.servlet;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Stack;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.SingleThreadModel;
 import javax.servlet.UnavailableException;
 import javax.servlet.sip.SipServlet;
 
 import org.mortbay.jetty.servlet.Holder;
 import org.mortbay.log.Log;
 
 public class SipServletHolder extends Holder implements Comparable
 {
     private int _initOrder;
     private boolean _initOnStartup=false;
     
     private transient Servlet _servlet;
     private transient Config _config;
     private transient long _unavailable;
     private transient UnavailableException _unavailableEx;
 
     public SipServletHolder()
     {}
     
     public SipServletHolder(SipServlet servlet)
     {
         setServlet(servlet);
     }
     
     public SipServletHolder(Class<? extends SipServlet> servlet)
     {
         super(servlet);
     }
     
     /* ------------------------------------------------------------ */
     public synchronized void setServlet(Servlet servlet)
     {
         if (servlet==null || servlet instanceof SingleThreadModel)
             throw new IllegalArgumentException();
 
         _extInstance=true;
         _servlet=servlet;
         setHeldClass(servlet.getClass());
         if (getName()==null)
             setName(servlet.getClass().getName()+"-"+super.hashCode());
     }
     
     /* ------------------------------------------------------------ */
     public int getInitOrder()
     {
         return _initOrder;
     }
 
     /* ------------------------------------------------------------ */
     /** Set the initialize order.
      * Holders with order<0, are initialized on use. Those with
      * order>=0 are initialized in increasing order when the handler
      * is started.
      */
     public void setInitOrder(int order)
     {
         _initOnStartup=true;
         _initOrder = order;
     }
 
     /* ------------------------------------------------------------ */
     /** Comparitor by init order.
      */
     public int compareTo(Object o)
     {
         if (o instanceof SipServletHolder)
         {
             SipServletHolder sh= (SipServletHolder)o;
             if (sh==this)
                 return 0;
             if (sh._initOrder<_initOrder)
                 return 1;
             if (sh._initOrder>_initOrder)
                 return -1;
             
             int c=(_className!=null && sh._className!=null)?_className.compareTo(sh._className):0;
             if (c==0)
                 c=_name.compareTo(sh._name);
             if (c==0)
                 c=this.hashCode()>o.hashCode()?1:-1;
             return c;
         }
         return 1;
     }
 
     /* ------------------------------------------------------------ */
     public boolean equals(Object o)
     {
         return compareTo(o)==0;
     }
 
     /* ------------------------------------------------------------ */
     public int hashCode()
     {
         return _name==null?System.identityHashCode(this):_name.hashCode();
     }
 
     public void doStart()
     throws Exception
     {
         _unavailable=0;
         try
         {
             super.doStart();
             checkServletType();
         }
         catch (UnavailableException ue)
         {
             makeUnavailable(ue);
         }
 
         _config=new Config();
 
         if (javax.servlet.SingleThreadModel.class.isAssignableFrom(_class))
             _servlet = new SingleThreadedWrapper();
 
         if (_extInstance || _initOnStartup)
         {
             if (_servlet==null)
                 _servlet=(Servlet)newInstance();
             try
             {
                 initServlet(_servlet,_config);
             }
             catch(Throwable e)
             {
                 _servlet=null;
                 _config=null;
                 if (e instanceof Exception)
                     throw (Exception) e;
                 else if (e instanceof Error)
                     throw (Error)e;
                 else
                     throw new ServletException(e);
             } 
         }  
     }
 
     /* ------------------------------------------------------------ */
     public void doStop()
     {
         try
         {
             if (_servlet!=null)
             {                  
                 try
                 {
                     destroyInstance(_servlet);
                 }
                 catch (Exception e)
                 {
                     Log.warn(e);
                 }
             }
             
             if (!_extInstance)
                 _servlet=null;
            
             _config=null;
         }
         finally
         {
             super.doStop();
         }
     }
 
     /* ------------------------------------------------------------ */
     public void destroyInstance (Object o)
     throws Exception
     {
         if (o==null)
             return;
         Servlet servlet =  ((Servlet)o);
         servlet.destroy();
         getServletHandler().customizeServletDestroy(servlet);
     }
 
     /* ------------------------------------------------------------ */
     /** Get the servlet.
      * @return The servlet
      */
     public synchronized Servlet getServlet()
         throws ServletException
     {
         // Handle previous unavailability
         if (_unavailable!=0)
         {
             if (_unavailable<0 || _unavailable>0 && System.currentTimeMillis()<_unavailable)
                 throw _unavailableEx;
             _unavailable=0;
             _unavailableEx=null;
         }
         
         try
         {   
             if (_servlet==null)
             {
                 _servlet=(Servlet)newInstance();
                 if (_config==null)
                 	_config=new Config();
                 initServlet(_servlet,_config);
             }
         
             return _servlet;
         }
         catch(UnavailableException e)
         {
             _servlet=null;
             _config=null;
             return makeUnavailable(e);
         }
         catch(ServletException e)
         {
             _servlet=null;
             _config=null;
             throw e;
         }
         catch(Throwable e)
         {
             _servlet=null;
             _config=null;
             throw new ServletException("init",e);
         }    
     }
 
     /* ------------------------------------------------------------ */
     /**
      * Check to ensure class of servlet is acceptable.
      * @throws UnavailableException
      */
     public void checkServletType ()
         throws UnavailableException
     {
         if (!javax.servlet.Servlet.class.isAssignableFrom(_class))
         {
             throw new UnavailableException("Servlet "+_class+" is not a javax.servlet.Servlet");
         }
     }
 
     /* ------------------------------------------------------------ */
     /** 
      * @return true if the holder is started and is not unavailable
      */
     public boolean isAvailable()
     {
         if (isStarted()&& _unavailable==0)
             return true;
         try 
         {
             getServlet();
         }
         catch(Exception e)
         {
             Log.ignore(e);
         }
 
         return isStarted()&& _unavailable==0;
     }
     
     /* ------------------------------------------------------------ */
     private Servlet makeUnavailable(UnavailableException e) 
       throws UnavailableException 
     {
         _unavailableEx=e;
         _unavailable=-1;
         if (e.isPermanent())   
             _unavailable=-1;
         else
         {
             if (_unavailableEx.getUnavailableSeconds()>0)
                 _unavailable=System.currentTimeMillis()+1000*_unavailableEx.getUnavailableSeconds();
             else
                 _unavailable=System.currentTimeMillis()+5000; // TODO configure
         }
         
         throw _unavailableEx;
     }
 
     /* ------------------------------------------------------------ */
     private void initServlet(Servlet servlet, ServletConfig config) 
     	throws ServletException
     {
         try
         {
             //handle any cusomizations of the servlet, such as @postConstruct
             _servlet = getServletHandler().customizeServlet(servlet);
             
             servlet.init(config);
             
             if (servlet instanceof SipServlet)
             	((SipServletHandler) getServletHandler()).servletInitialized((SipServlet) servlet);
         }
         catch (Exception e)
         {
             throw new ServletException(e);
         }
     }
     
     public Servlet servlet()
     {
     	return _servlet;
     }
     
     /* ------------------------------------------------------------ */
     /** Service a request with this servlet.
      */
     public void handle(ServletRequest request,
                        ServletResponse response)
         throws ServletException,
                UnavailableException,
                IOException
     {
         if (_class==null)
             throw new UnavailableException("Servlet Not Initialized");
         
         Servlet servlet=_servlet;
         synchronized(this)
         {
             if (_unavailable!=0 || !_initOnStartup)
             servlet=getServlet();
             if (servlet==null)
                 throw new UnavailableException("Could not instantiate "+_class);
         }
         
         // Service the request
         boolean servlet_error=true;
 
         try
         {
     
             servlet.service(request,response);
             servlet_error=false;
         }
         catch(UnavailableException e)
         {
             makeUnavailable(e);
         }
         finally
         {
             // Handle error params.
            if (servlet_error)
                 request.setAttribute("javax.servlet.error.servlet_name",getName());
         }
     }
 
  
     /* ------------------------------------------------------------ */
     /* ------------------------------------------------------------ */
     /* ------------------------------------------------------------ */
     class Config implements ServletConfig
     {   
         /* -------------------------------------------------------- */
         public String getServletName()
         {
             return getName();
         }
         
         /* -------------------------------------------------------- */
         public ServletContext getServletContext()
         {
             return _servletHandler.getServletContext();
         }
 
         /* -------------------------------------------------------- */
         public String getInitParameter(String param)
         {
             return SipServletHolder.this.getInitParameter(param);
         }
     
         /* -------------------------------------------------------- */
         public Enumeration getInitParameterNames()
         {
             return SipServletHolder.this.getInitParameterNames();
         }
     }
 
     /* -------------------------------------------------------- */
     /* -------------------------------------------------------- */
     /* -------------------------------------------------------- */
     private class SingleThreadedWrapper implements Servlet
     {
         Stack _stack=new Stack();
         
         public void destroy()
         {
             synchronized(this)
             {
                 while(_stack.size()>0)
                     try { ((Servlet)_stack.pop()).destroy(); } catch (Exception e) { Log.warn(e); }
             }
         }
 
         public ServletConfig getServletConfig()
         {
             return _config;
         }
 
         public String getServletInfo()
         {
             return null;
         }
 
         public void init(ServletConfig config) throws ServletException
         {
             synchronized(this)
             {
                 if(_stack.size()==0)
                 {
                     try
                     {
                         Servlet s = (Servlet) newInstance();
                         s.init(config);
                         _stack.push(s);
                     }
                     catch(IllegalAccessException e)
                     {
                         throw new ServletException(e);
                     }
                     catch (InstantiationException e)
                     {
                         throw new ServletException(e);
                     }
                 }
             }
         }
 
         public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
         {
             Servlet s;
             synchronized(this)
             {
                 if(_stack.size()>0)
                     s=(Servlet)_stack.pop();
                 else
                 {
                     try
                     {
                         s = (Servlet) newInstance();
                         s.init(_config);
                     }
                     catch(IllegalAccessException e)
                     {
                         throw new ServletException(e);
                     }
                     catch (InstantiationException e)
                     {
                         throw new ServletException(e);
                     }
                 }
             }
             
             try
             {
                 s.service(req,res);
             }
             finally
             {
                 synchronized(this)
                 {
                     _stack.push(s);
                 }
             }
         }
     }
 }
 
 
 
 
 
