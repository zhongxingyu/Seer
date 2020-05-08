 package org.jbundle.base.remote.proxy.transport;
 
 /**
  * @(#)DBServlet.java 0.00 12-Feb-97 Don Corley
  *
  * Copyright (c) 2009 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.rmi.RemoteException;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.jbundle.base.remote.db.TaskSession;
 import org.jbundle.base.remote.proxy.BaseHolder;
 import org.jbundle.base.remote.proxy.MapList;
 import org.jbundle.base.remote.proxy.TaskHolder;
 import org.jbundle.base.screen.control.servlet.BaseHttpTask;
 import org.jbundle.base.screen.control.servlet.BasicServlet;
 import org.jbundle.base.util.BaseApplication;
 import org.jbundle.base.util.DBConstants;
 import org.jbundle.base.util.DBParams;
 import org.jbundle.base.util.MainApplication;
 import org.jbundle.base.util.Utility;
 import org.jbundle.thin.base.db.Params;
 import org.jbundle.thin.base.remote.RemoteTask;
 import org.jbundle.thin.base.remote.proxy.ProxyConstants;
 import org.jbundle.thin.base.util.Application;
 import org.jbundle.thin.base.util.base64.Base64;
 
 
 /**
  * ProxyServlet The servlet to handle proxy messages (tunneled through http).
  * pend(don) Need to free the remote sessions if they haven't been accessed for a while.
  * WARNING: A ProxyTask is NOT like a servlet task.
  * - A Servlet task is created for each servlet session.
  * - A Proxy task is created once and shared between all proxy callers.
  * The task info must be saved in the newly created tasks.
  */
 public class ProxyTask extends BaseHttpTask
     implements ProxyConstants
 {
     /**
      * List of remote tasks (by unique ID).
      */
     protected MapList m_mapTasks = new MapList();
 
     /**
      * Constructor.
      */
     public ProxyTask()
     {
         super();
     }
     /**
      * Constructor.
      */
     public ProxyTask(BasicServlet servlet, SERVLET_TYPE servletType)
     {
         this();
         this.init(servlet, servletType);
     }
     /**
      * Constructor.
      */
     public void init(BasicServlet servlet, SERVLET_TYPE servletType)
     {
         super.init(servlet, servletType);
         // First see if this is an active session
         if (m_application == null)
             m_application = this.getNonUserApplication();   // This task belongs to the servlet.
     }
     /**
      * Free the resources for this holder.
      */
     public void free()
     {
         super.free();
     }
     /**
      * Remove this BaseHolder from my list.
      * @param baseHolder The remote session peer to remove.
      * @return true if successful.
      */
     public boolean remove(BaseHolder baseHolder)
     {
         if (m_mapTasks != null)
             return m_mapTasks.remove(baseHolder);
         return false;
     }
     /**
      * Remove this BaseHolder from my list.
      * @param baseHolder The remote session peer to remove.
      * @return true if successful.
      */
     public String add(BaseHolder baseHolder)
     {
         return m_mapTasks.add(baseHolder);
     }
     /**
      * Process an HTML get or post.
      * @exception ServletException From inherited class.
      * @exception IOException From inherited class.
      */
     public void doProcess(BasicServlet servlet, HttpServletRequest req, HttpServletResponse res, PrintWriter out) 
         throws ServletException, IOException
     {
         Map<String,Object> propRequest = this.getRequestProperties(req, true);
         String strBaseURL = (String)propRequest.get(DBParams.BASE_URL);
         if (strBaseURL != null)
         {
             if (strBaseURL.endsWith(AJAX))
                 strBaseURL = strBaseURL.substring(0, strBaseURL.length() - AJAX.length());
             propRequest.put(DBParams.BASE_URL, strBaseURL);
         }
         String strCodeBase = this.getRealPath(req, DBConstants.BLANK);
         if ((strCodeBase != null) && (strCodeBase.length() > 0))
         {
             if (!strCodeBase.endsWith(System.getProperty("file.separator")))
                 strCodeBase = strCodeBase + System.getProperty("file.separator");
             if (strCodeBase != null)
                 propRequest.put(Params.CODEBASE, strCodeBase);
         }
 
         servlet.setContentType(res);
         InputStream in = null;  // Not used for the default.
         if (out == null)
             out = servlet.getOutputStream(res);
 
         String strTarget = (String)propRequest.get(TARGET);
         if (strTarget == null)
         {   // Application method
             String strCommand = (String)propRequest.get(REMOTE_COMMAND);
             if (CREATE_REMOTE_TASK.equals(strCommand))
             {
                 Map<String,Object> properties = this.getNextPropertiesParam(in, PROPERTIES, propRequest);
                 if (properties != null)
                     propRequest.putAll(properties);
                 RemoteTask remoteTask = this.getNewRemoteTask(null, propRequest);
                 String strUniqueID = this.add(new TaskHolder(this, remoteTask));
                 out.write(strUniqueID);
             }
         }
         else 
         {
             try {
                 BaseHolder baseHolder = this.getSessionFromPath(strTarget);
                 if (baseHolder != null)
                 	baseHolder.doProcess(in, out, propRequest);
                 else
                 	Utility.getLogger().warning("Session not found, command: " + propRequest.get(REMOTE_COMMAND));
             } catch (RemoteException ex) {
                 this.setErrorReturn(out, ex);
             }
         }
     }
     public static final String AJAX = "ajax";
     /**
      * Sent/send this return string.
      * @param out The return output stream.
      * @param strReturn The string to return.
      */
     public void setErrorReturn(PrintWriter out, RemoteException ex)
         throws RemoteException
     {
         throw ex;   // Override this for other behavior
     }
     /**
      * Sent/send this return string.
      * @param out The return output stream.
      * @param strReturn The string to return.
      */
     public void setReturnString(PrintWriter out, String strReturn)
     {
         out.write(strReturn);
     }
     /**
      * Sent/send this return string.
      * @param out The return output stream.
      * @param strReturn The string to return.
      */
     public void setReturnObject(PrintWriter out, Object objReturn)
     {
         String strReturn = org.jbundle.thin.base.remote.proxy.transport.BaseTransport.convertObjectToString(objReturn);
         strReturn = Base64.encode(strReturn);
         this.setReturnString(out, strReturn);
     }
     /**
      * Look up this session in the hierarchy.
      */
     public BaseHolder getSessionFromPath(String strSessionPathID)
     {
         if (strSessionPathID == null)
             return null;
         BaseHolder rootHolder = null;
         // Now, traverse the path
         int iStartPosition = 0;
         int iEndPosition = 0;
         while (iEndPosition < strSessionPathID.length())
         {
             iEndPosition = strSessionPathID.indexOf(PATH_SEPARATOR, iStartPosition);
             if (iEndPosition == -1)
                 iEndPosition = strSessionPathID.length();
             String strID = strSessionPathID.substring(iStartPosition, iEndPosition);
             if (iStartPosition == 0)
                 rootHolder = (TaskHolder)m_mapTasks.get(strID);
             else
                 rootHolder = rootHolder.get(strID);
             if (rootHolder == null)
                 return null;
             iStartPosition = iEndPosition + 1;
         }
         return rootHolder;
     }
     /**
      * Get the next (String) param.
      * Typically this is overidden in the concrete implementation.
      * @param strName The param name (in most implementations this is optional).
      * @param properties The temporary remote session properties
      * @return The next param as a string.
      */
     public String getNextStringParam(InputStream in, String strName, Map<String, Object> properties)
     {
         String string = null;
         if (properties != null)
             if (properties.get(strName) != null)
                 string = properties.get(strName).toString();
         if (NULL.equals(string))
             string = null;
         return string;
     }
     /**
      * Get the next (String) param.
      * @param strName The param name (in most implementations this is optional).
      * @param properties The temporary remote session properties
      * @return The next param as a string.
      */
     public int getNextIntParam(InputStream in, String strName, Map<String, Object> properties)
     {
         String strInteger = this.getNextStringParam(in, strName, properties);
         return Integer.parseInt(strInteger);
     }
     /**
      * Get the next (String) param.
      * @param strName The param name (in most implementations this is optional).
      * @param properties The temporary remote session properties
      * @return The next param as a string.
      */
     public boolean getNextBooleanParam(InputStream in, String strName, Map<String, Object> properties)
     {
         String strBoolean = this.getNextStringParam(in, strName, properties);
         return (DBConstants.FALSE.equals(strBoolean) ? false : true);
     }
     /**
      * Get the next (String) param.
      * @param strName The param name (in most implementations this is optional).
      * @param properties The temporary remote session properties
      * @return The next param as a string.
      */
     @SuppressWarnings("unchecked")
     public Map<String, Object> getNextPropertiesParam(InputStream in, String strName, Map<String, Object> properties)
     {
         return (Map)this.getNextObjectParam(in, strName, properties);
     }
     /**
      * Get the next (String) param.
      * @param strName The param name (in most implementations this is optional).
      * @param properties The temporary remote session properties
      * @return The next param as a string.
      */
     public Object getNextObjectParam(InputStream in, String strName, Map<String, Object> properties)
     {
         String strParam = this.getNextStringParam(in, strName, properties);
         strParam = Base64.decode(strParam);
         return org.jbundle.thin.base.remote.proxy.transport.BaseTransport.convertStringToObject(strParam);
     }
     /**
      * Get a remote session from the pool and initialize it.
      * If the pool is empty, create a new remote session.
      * @param strUserID The user name/ID of the user, or null if unknown.
      * @return The remote Task.
      */
     public RemoteTask getNewRemoteTask(Application app, Map<String,Object> properties)
     {
         try   {
            Map<String,Object> propApp = this.getApplicationProperties(properties);
 
            Map<String,Object> propInitial = ProxyTask.getInitialProperties(this.getBasicServlet(), SERVLET_TYPE.PROXY);
            if (propInitial != null)
            	propApp.putAll(propInitial);
             
             if (app == null)
                app = new MainApplication(((BaseApplication)m_application).getEnvironment(), propApp, null);
             RemoteTask remoteServer = new TaskSession(app);
             ((TaskSession)remoteServer).setProperties(properties);
             return remoteServer;
         } catch (Exception ex) {
             ex.printStackTrace();
             return null;
         }
     }
     /**
      * Get application properties from proxy properties.
      * @return Just the application properties
      */
     public Map<String, Object> getApplicationProperties(Map<String, Object> properties)
     {
         if (properties == null)
             return null;
         Map<String, Object> propApp = new Hashtable<String,Object>();
         if (properties.get(DBParams.LANGUAGE) != null)
             propApp.put(DBParams.LANGUAGE, properties.get(DBParams.LANGUAGE));
         if (properties.get(DBParams.DOMAIN) != null)
             propApp.put(DBParams.DOMAIN, properties.get(DBParams.DOMAIN));
         return propApp;
     }
 }
