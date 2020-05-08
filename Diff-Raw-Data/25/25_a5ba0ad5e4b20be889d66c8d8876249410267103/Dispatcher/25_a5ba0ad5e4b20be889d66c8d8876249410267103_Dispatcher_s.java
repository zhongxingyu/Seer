 package org.valabs.odisp.standart;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.doomdark.uuid.UUID;
 import org.valabs.odisp.common.ConfigurationManager;
 import org.valabs.odisp.common.ExceptionHandler;
 import org.valabs.odisp.common.Message;
 import org.valabs.odisp.common.ObjectManager;
 import org.valabs.odisp.common.ResourceManager;
 import org.valabs.odisp.common.SecurityManager;
 import org.valabs.stdmsg.StandartMessage;
 
 /**   ODISP.
  *        
  *    .
  * @author (C) 2003-2004 <a href="mailto:valeks@novel-il.ru"> . </a>
  * @author (C) 2003-2004 <a href="mailto:dron@novel-il.ru"> . </a>
 * @version $Id: Dispatcher.java,v 1.61 2005/02/27 12:37:31 valeks Exp $
  */
 public class Dispatcher implements org.valabs.odisp.common.Dispatcher, ExceptionHandler {
   /** . */
   private static final Logger log = Logger.getLogger(Dispatcher.class.getName());
   /**  . */
   private final ResourceManager rman = new org.valabs.odisp.standart.ResourceManager(this);
   /**  . */
   private final ObjectManager oman = new org.valabs.odisp.standart.ObjectManager(this);
   /**   . */
   private final ConfigurationManager cman = new MultiConfigurationManager();
   /**  . */
   private SecurityManager sman;
   /**  . */
   private ExceptionHandler ehandler;
 
   /**    . 
    * @return    
    */
   public final ObjectManager getObjectManager() {
     return oman;
   }
 
   /**    . 
    * @return    
    */
   public final ResourceManager getResourceManager() {
     return rman;
   }
 
   /**       .
    * @param message   
    */
   public final void send(final Message message) {
     oman.send(message);
   }
 
   /**       .
    * @param messageList    
    */
   public final void send(final Message[] messageList) {
     if (messageList == null || messageList.length == 0) {
       return;
     }
     for (int i = 0; i < messageList.length; i++) {
       Message message = messageList[i];
       send(message);
     }
   }
 
   /**       .
    * @param messageList    
    */
   public final void send(final List messageList) {
     if (messageList != null) {
       Iterator it = messageList.iterator();
       while (it.hasNext()) {
         send((Message) it.next());
       }
     }
   }
 
   /**        
    * .
    * @param action    
    * @param destination  
    * @param origin  
    * @param inReplyTo      
    * @return Message  
    */
   public final Message getNewMessage(final String action,
 				     final String destination,
 				     final String origin,
 				     final UUID inReplyTo) {
     return new StandartMessage(action, destination, origin, inReplyTo);
   }
 
   /**    .
    * @return   
    */
   public final Message getNewMessage() {
     return new StandartMessage();
   }
 
   public Dispatcher(List args) {
     log.info(toString() + " starting up...");
     addConfigurationManager(new org.valabs.odisp.standart.ConfigurationManager());
     getConfigurationManager().setCommandLineArguments(args);
     if (getConfigurationManager().supportComponentListing()) {
       List resources = getConfigurationManager().getResourceList();
       List objects = getConfigurationManager().getObjectList();
       Iterator it = resources.iterator();
       while (it.hasNext()) {
         ConfigurationManager.ComponentConfiguration element = (ConfigurationManager.ComponentConfiguration) it.next();
         int mult = -1;
         if (element.getConfiguration() != null && element.getConfiguration().containsKey("mult")) {
           mult = new Integer((String) (element.getConfiguration().get("mult"))).intValue();
         }
         rman.loadResource(element.getClassName(), mult, element.getConfiguration());
       }
       it = objects.iterator();
       while (it.hasNext()) {
         ConfigurationManager.ComponentConfiguration element = (ConfigurationManager.ComponentConfiguration) it.next();
         oman.loadObject(element.getClassName(), element.getConfiguration());
       }
      oman.loadPending();
       Thread aliveThread = new Thread("alive thread") {
         public final void run() {
           try {
             synchronized (this) {
               wait();
             }
           } catch (InterruptedException e) {
           }
         }
       };
       Map tmp = new HashMap();
       tmp.put("runthr", aliveThread);
       oman.loadObject(DispatcherHandler.class.getName(), tmp);
       oman.loadPending();
 
       aliveThread.start();
       try {
         aliveThread.join();
       } catch (InterruptedException e) {
 
       }
     } else {
       log.severe("Default configuration manager does not support component listing. Bailing out.");
     }
   }
   
   /**        . */
   public static void usage() {
     log.severe("Usage: java org.valabs.odisp.standart.Dispatcher <config>");
     System.exit(0);
   }
   /**    StandartDispatcher.
    * @param args  0       ,
    *   
    */
   public static void main(final String[] args) {
     log.setLevel(java.util.logging.Level.ALL);
     if (args.length != 1) {
 		usage();
     } else {
       new Dispatcher(Arrays.asList(args));
     }
   }
   
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.Dispatcher#addSecurityManager(org.valabs.odisp.common.SecurityManager)
    */
   public void addSecurityManager(final SecurityManager additionalSecurityManager) {
   		sman = additionalSecurityManager;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.Dispatcher#getSecurityManager()
    */
   public SecurityManager getSecurityManager() {
     assert sman != null : "security manager is null! no one registered ever";
     return sman;
   }
  
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.Dispatcher#addExceptionHandler(org.valabs.odisp.common.ExceptionHandler)
    */
   public void addExceptionHandler(final ExceptionHandler exHandler) {
     ehandler = exHandler;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.Dispatcher#getExceptionHandler()
    */
   public ExceptionHandler getExceptionHandler() {
   		if(ehandler == null) {
   		  return this;
   		}
   		return ehandler;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ExceptionHandler#signalException(java.lang.Exception)
    */
   public void signalException(final Exception exception) {
     System.err.println("========================================================");
     System.err.println("Exception caught with default exception handler:");
     System.err.println("Exception: " + exception.toString());
     System.err.println("Stack trace:");
     exception.printStackTrace(System.err);
     System.err.println("========================================================");
   }
   
   public void addConfigurationManager(final ConfigurationManager _cman) {
     ((MultiConfigurationManager) cman).addConfigurationManager(_cman);
   }
 
   public ConfigurationManager getConfigurationManager() {
     return cman;
   }
   
   /**   . */
   class MultiConfigurationManager implements ConfigurationManager {
     private final List cman = new ArrayList();
     void addConfigurationManager(final ConfigurationManager _cman) {
       cman.add(_cman);
     }
     public List getObjectList() {
       final Iterator cmanIt = cman.iterator();
       List result = null;
       while (cmanIt.hasNext()) {
         final ConfigurationManager element = (ConfigurationManager) cmanIt.next();
         if (element.supportComponentListing()) {
           result = element.getObjectList();
           break;
         }
       }
       return result;
     }
     
     public String getParameter(final String domain, final String paramName) {
       final Iterator cmanIt = cman.iterator();
       String result = null;
       while (cmanIt.hasNext()) {
         final ConfigurationManager element = (ConfigurationManager) cmanIt.next();
         final String value = element.getParameter(domain, paramName);
         if (value != null) {
           result = value;
           break;
         }
       }
       return result;
     }
     public List getResourceList() {
       final Iterator cmanIt = cman.iterator();
       List result = null;
       while (cmanIt.hasNext()) {
         final ConfigurationManager element = (ConfigurationManager) cmanIt.next();
         if (element.supportComponentListing()) {
           result = element.getResourceList();
           break;
         }
       }
       return result;
     }
     
     public void setCommandLineArguments(final List args) {
       final Iterator cmanIt = cman.iterator();
       while (cmanIt.hasNext()) {
         ((ConfigurationManager) cmanIt.next()).setCommandLineArguments(args);
       }
     }
     
     public boolean supportComponentListing() {
       final Iterator cmanIt = cman.iterator();
       boolean result = false;
       while (cmanIt.hasNext()) {
         final ConfigurationManager element = (ConfigurationManager) cmanIt.next();
         if (element.supportComponentListing()) {
           result = true;
           break;
         }
       }
       return result;
     }
     public boolean supportParameterFetching() {
       final Iterator cmanIt = cman.iterator();
       boolean result = false;
       while (cmanIt.hasNext()) {
         final ConfigurationManager element = (ConfigurationManager) cmanIt.next();
         if (element.supportParameterFetching()) {
           result = true;
           break;
         }
       }
       return result;
     }
 }
 } // StandartDispatcher
