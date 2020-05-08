 /*
  * TaskHolder.java
  *
  * Created on November 16, 2002, 8:53 PM
 
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.remote.proxy;
 
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.Map;
 
 import org.jbundle.base.model.Utility;
 import org.jbundle.model.PropertyOwner;
 import org.jbundle.thin.base.remote.RemoteBaseSession;
 import org.jbundle.thin.base.remote.RemoteException;
 import org.jbundle.thin.base.remote.proxy.ProxyConstants;
 
 
 /**
  *
  * @author  don
  */
 public class BaseHolder extends Object
     implements ProxyConstants
 {
     /**
      * This holder's parent.
      */
     protected BaseHolder m_parentHolder = null;
     /**
      * The "remote" task that this holder references.
      */
     protected RemoteBaseSession m_remoteObject = null;
     /**
      * List of remote databases (by unique ID).
      */
     protected MapList m_mapChildHolders = null;
 
     /**
      * Creates a new instance of TaskHolder
      */
     public BaseHolder()
     {
         super();
     }
     /**
      * Creates a new instance of TaskHolder
      */
     public BaseHolder(BaseHolder parentHolder, RemoteBaseSession remoteObject)
     {
         this();
         this.init(parentHolder, remoteObject);
     }
     /**
      * Creates a new instance of TaskHolder
      */
     public void init(BaseHolder parentHolder, RemoteBaseSession remoteObject)
     {
         m_parentHolder = parentHolder;
         m_remoteObject = remoteObject;
     }
     /**
      * Free the resources for this holder.
      */
     public void free()
     {
         if ((m_mapChildHolders != null) && (m_mapChildHolders.size() > 0))
         {
             Utility.getLogger().info("Not all sub-sessions freed by client - I will free them");
             synchronized (this)
             {
                for (String strID : m_mapChildHolders.keySet())
                 {
                     BaseHolder baseHolder = (BaseHolder)m_mapChildHolders.get(strID);
                     baseHolder.free();  // Should be removed from collection.
                 }
                 m_mapChildHolders.clear();
             }
         }
         m_mapChildHolders = null;
         if (m_parentHolder != null)
             m_parentHolder.remove(this);
         m_parentHolder = null;
         m_remoteObject = null;
     }
     /**
      * Get the remote session for this holder.
      * @return The remote session.
      */
     public RemoteBaseSession getRemoteObject()
     {
         return m_remoteObject;
     }
     /**
      * Get the remote session for this holder.
      * @return The remote session.
      */
     public BaseHolder getParentHolder()
     {
         return m_parentHolder;
     }
     /**
      * Handle the command send from my client peer.
      * @param in The (optional) Inputstream to get the params from.
      * @param out The stream to write the results.
      * @param properties Temporary session properties.
      */
     public void doProcess(InputStream in, PrintWriter out, Map<String, Object> properties)
         throws RemoteException
     {
         Utility.getLogger().warning("Command not handled: " + this.getProperty(REMOTE_COMMAND, properties));
     }
     /**
      * Get the servlet's property.
      * @param properties Temporary session properties.
      */
     public String getProperty(String strKey, Map<String, Object> properties)
     {
         String strProperty = null;
         if (properties != null)
             if (properties.get(strKey) != null)
                 strProperty = properties.get(strKey).toString();
         if (strProperty == null)
             if (m_remoteObject instanceof PropertyOwner)
                 strProperty = ((PropertyOwner)m_remoteObject).getProperty(strKey);
         if (strProperty == null)
         {
             if (m_parentHolder != null)
                 strProperty = m_parentHolder.getProperty(strKey, properties);
             if (NULL.equals(strProperty))
                 strProperty = null;
         }
         return strProperty;
     }
     /**
      * Get the next (String) param.
      * @param strName The param name (in most implementations this is optional).
      * @param properties Temporary session properties.
      * @return The next param as a string.
      */
     public String getNextStringParam(InputStream in, String strName, Map<String, Object> properties)
     {
         return m_parentHolder.getNextStringParam(in, strName, properties);
     }
     /**
      * Get the next (String) param.
      * @param strName The param name (in most implementations this is optional).
      * @param properties Temporary session properties.
      * @return The next param as a string.
      */
     public int getNextIntParam(InputStream in, String strName, Map<String, Object> properties)
     {
         return m_parentHolder.getNextIntParam(in, strName, properties);
     }
     /**
      * Get the next (String) param.
      * @param strName The param name (in most implementations this is optional).
      * @param properties Temporary session properties.
      * @return The next param as a string.
      */
     public boolean getNextBooleanParam(InputStream in, String strName, Map<String, Object> properties)
     {
         return m_parentHolder.getNextBooleanParam(in, strName, properties);
     }
     /**
      * Get the next (String) param.
      * @param strName The param name (in most implementations this is optional).
      * @param properties Temporary session properties.
      * @return The next param as a string.
      */
     public Map<String, Object> getNextPropertiesParam(InputStream in, String strName, Map<String, Object> properties)
     {
         return m_parentHolder.getNextPropertiesParam(in, strName, properties);
     }
     /**
      * Get the next (String) param.
      * @param strName The param name (in most implementations this is optional).
      * @param properties Temporary session properties.
      * @return The next param as a string.
      */
     public Object getNextObjectParam(InputStream in, String strName, Map<String, Object> properties)
     {
         return m_parentHolder.getNextObjectParam(in, strName, properties);
     }
     /**
      * Sent/send this return string.
      * @param out The return output stream.
      * @param strReturn The string to return.
      */
     public void setReturnString(PrintWriter out, String strReturn)
     {
         m_parentHolder.setReturnString(out, strReturn);
     }
     /**
      * Sent/send this return string.
      * @param out The return output stream.
      * @param strReturn The string to return.
      */
     public void setReturnObject(PrintWriter out, Object objReturn)
     {
         m_parentHolder.setReturnObject(out, objReturn);
     }
     /**
      * Get the session with this path (This method must be called from the ProxyTask).
      * @param properties Temporary session properties.
      * @param out The return output stream.
      * @param strReturn The string to return.
      */
     public BaseHolder getSessionFromPath(String strSessionPathID)
     {
         return m_parentHolder.getSessionFromPath(strSessionPathID);
     }
     /**
      * Find the key for this BaseHolder
      */
     public String find(BaseHolder baseHolder)
     {
         if (m_mapChildHolders == null)
             return null;
         return m_mapChildHolders.find(baseHolder);
     }
     /**
      * Find the key for the baseholder that holds this session.
      */
     public String find(RemoteBaseSession obj)
     {
         if (m_mapChildHolders == null)
             return null;
         return m_mapChildHolders.find(obj);
     }
     /**
      * Add this remote object holder to this parent.
      */
     public String add(BaseHolder baseHolder)
     {
         if (m_mapChildHolders == null)
             m_mapChildHolders = new MapList();
         synchronized (this)
         {
             return m_mapChildHolders.add(baseHolder);
         }
     }
     /**
      * Remove this BaseHolder from my list.
      * @param baseHolder The remote session peer to remove.
      * @return true if successful.
      */
     public boolean remove(BaseHolder baseHolder)
     {
         if (m_mapChildHolders != null)
         {
             synchronized (this)
             {
                 return m_mapChildHolders.remove(baseHolder);
             }
         }
         return false;
     }
     /**
      *
      */
     public BaseHolder get(String strObjID)
     {
         if (m_mapChildHolders == null)
             return null;
         return (BaseHolder)m_mapChildHolders.get(strObjID);
     }
 }
