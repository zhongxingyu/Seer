 /**********************************************************************
  * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/server/HandlerMappingImpl.java,v $
 * $Revision: 1.16 $
 * $Date: 2007/11/16 18:34:06 $
  * $Author: willuhn $
  * $Locker:  $
  * $State: Exp $
  *
  * Copyright (c) by willuhn.webdesign
  * All rights reserved
  *
  **********************************************************************/
 
 package de.willuhn.jameica.xmlrpc.server;
 
 import java.util.ArrayList;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.XmlRpcHandler;
 import org.apache.xmlrpc.XmlRpcRequest;
 import org.apache.xmlrpc.XmlRpcRequestConfig;
 import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
 import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
 import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
 import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;
 
 import de.willuhn.jameica.messaging.Message;
 import de.willuhn.jameica.messaging.MessageConsumer;
 import de.willuhn.jameica.messaging.SystemMessage;
 import de.willuhn.jameica.system.Application;
 import de.willuhn.jameica.xmlrpc.Settings;
 import de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor;
 import de.willuhn.logging.Logger;
 
 /**
  * Implementiert das Mapping von XML-RPC-Namen auf Klassen.
  * @author willuhn
  */
 public class HandlerMappingImpl extends AbstractReflectiveHandlerMapping implements XmlRpcHandlerMapping, MessageConsumer
 {
   // Statisch, weil wir die Liste nur einmal ermitteln wollen.
   private static ArrayList services  = null;
 
   private boolean initialized = false;
   
   // Wir implementieren noch den Message-Consumer, damit wir die XMLRPC-Handler
   // automatisch registrieren, wenn alle Services initialisiert wurden.
   /**
    * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
    */
   public boolean autoRegister()
   {
     return true;
   }
 
   /**
    * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
    */
   public Class[] getExpectedMessageTypes()
   {
     return new Class[]{SystemMessage.class};
   }
 
   /**
    * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
    */
   public synchronized void handleMessage(Message message) throws Exception
   {
     SystemMessage m = (SystemMessage) message;
 
     // Haben wir das schonmal gemacht?
     if (m.getStatusCode() != SystemMessage.SYSTEM_STARTED || services != null)
       return;
 
     try
     {
       services = new ArrayList();
 
       // Wir registrieren die Services hier noch nicht sondern ermitteln
       // nur die Liste und geben die URL aus, damit der Entwickler sofort
       // sieht, welches Services verfuegbar sind. Die eigentliche
       // Registrierung machen wir erst beim ersten Request, da das vom
       // HTTP-Server in einem anderen Thread passiert.
       Logger.info("deploying xml rpc services");
       XmlRpcServiceDescriptor[] all = Settings.getServices();
       for (int i=0;i<all.length;++i)
       {
         try
         {
           XmlRpcServiceDescriptor service = all[i];
           if (!service.isShared())
             continue;
 
           Logger.info("* XML-RPC-Service: " + service.getURL());
           services.add(service);
         }
         catch (Exception e)
         {
           Logger.error("unable to register service, skipping",e);
         }
       }
     }
     catch (Exception e)
     {
       Logger.error("unable to register services",e);
       throw e;
     }
   }
 
 
   /**
    * Aktiviert die Services.
    */
   private synchronized void init()
   {
     if (initialized || services == null)
       return;
 
     try
     {
       Logger.info("applying request processor factory");
       this.setRequestProcessorFactoryFactory(new MyRequestProcessorFactoryFactory());
       Logger.info("applying authentication handler");
       this.setAuthenticationHandler(new Auth());
 
       for (int i=0;i<services.size();++i)
       {
         try
         {
           XmlRpcServiceDescriptor service = (XmlRpcServiceDescriptor) services.get(i);
           Logger.info("activating service [id: " + service.getID() + "]");
           registerPublicMethods(service.getID(),service.getService().getClass());
         }
         catch (Exception e)
         {
           Logger.error("unable to activate service",e);
         }
       }
     }
     finally
     {
       initialized = true;
     }
   }
   /**
    * @see org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping#getHandler(java.lang.String)
    */
   public XmlRpcHandler getHandler(String pHandlerName) throws XmlRpcNoSuchHandlerException, XmlRpcException
   {
     init();
     return super.getHandler(pHandlerName);
   }
 
 
   /**
    * Authentifizierer.
    */
   private class Auth implements AuthenticationHandler
   {
       
     /**
     * @see org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler#isAuthorized(org.apache.xmlrpc.XmlRpcRequest)
      */
     public boolean isAuthorized(XmlRpcRequest request) throws XmlRpcException
     {
       if (!Settings.getUseAuth())
         return true;
   
       XmlRpcRequestConfig c = request.getConfig();
       if (c instanceof XmlRpcHttpRequestConfig)
       {
         XmlRpcHttpRequestConfig config = (XmlRpcHttpRequestConfig) c;
         String password = config.getBasicPassword();
         if (password == null || password.length() == 0)
           return false;
         try
         {
           return password.equals(Application.getCallback().getPassword());
         }
         catch (Exception e)
         {
           Logger.error("error while checking password, denying request",e);
           return false;
         }
       }
       Logger.warn("authentication enabled, but this is no http request, denying request");
       return false;
     }
   }
 }
 
 
 /*********************************************************************
  * $Log: HandlerMappingImpl.java,v $
 * Revision 1.16  2007/11/16 18:34:06  willuhn
 * @D javadoc fixed
 * @R removed unused methods/deprecated methods
 *
  * Revision 1.15  2007/10/18 22:13:14  willuhn
  * @N XML-RPC URL via Service-Descriptor abfragbar
  *
  * Revision 1.14  2007/04/05 12:14:40  willuhn
  * @N Liste der Services im Handler statisch
  * @C XmlRpcService in XmlRpcServiceDescriptor umbenannt
  *
  * Revision 1.13  2007/04/05 11:12:56  willuhn
  * *** empty log message ***
  *
  * Revision 1.12  2007/04/05 10:42:32  willuhn
  * @N Registrieren der XML/RPC-Handler erst nachdem alle Services geladen wurden (mittels SystemMessage). Somit koennen bereits beim Initialisieren die XMLRPC-URLs im Log ausgegeben werden und nicht erst beim ersten Request.
  *
  * Revision 1.11  2007/03/07 17:05:09  willuhn
  * @B Bei Fehler eines Services nicht gleich die komplette Registrierung abbrechen
  *
  * Revision 1.10  2007/02/15 11:04:25  willuhn
  * @D
  *
  * Revision 1.9  2007/02/15 11:03:58  willuhn
  * @B Services wurden bei jedem Request neu instanziiert
  *
  * Revision 1.8  2007/02/13 16:00:15  willuhn
  * *** empty log message ***
  *
  * Revision 1.7  2006/12/22 16:14:07  willuhn
  * @N Ausgabe der IP, an die der Service gebunden wurde
  *
  * Revision 1.6  2006/10/31 17:06:26  willuhn
  * @N GUI to configure xml-rpc
  *
  * Revision 1.5  2006/10/31 01:43:07  willuhn
  * *** empty log message ***
  *
  * Revision 1.4  2006/10/28 01:05:37  willuhn
  * @N add bindings on demand
  *
  * Revision 1.3  2006/10/26 23:54:15  willuhn
  * @N added needed jars
  * @N first working version
  *
  * Revision 1.2  2006/10/23 23:07:28  willuhn
  * *** empty log message ***
  *
  * Revision 1.1  2006/10/19 16:08:30  willuhn
  * *** empty log message ***
  *
  *********************************************************************/
