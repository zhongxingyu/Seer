 package com.novel.odisp;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.lang.reflect.InvocationTargetException;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.logging.Logger;
 
 import com.novel.odisp.common.Message;
 import com.novel.odisp.common.ODObject;
 import com.novel.odisp.common.Resource;
 import com.novel.odisp.common.ProxyResource;
 import com.novel.odisp.common.Dispatcher;
 import com.novel.odisp.common.CallbackODObject; //   stddispatcher
 import com.novel.odisp.common.MessageHandler; // --''--
 
 /**   ODISP.
  *        
  *    .
  * @author  . 
  * @author (C) 2003,  "-"
 * @version $Id: Dispatcher.java,v 1.17 2003/11/23 00:06:36 valeks Exp $
  */
 public class StandartDispatcher implements Dispatcher {
   /**    */
   private static Logger log = Logger.getLogger("com.novel.odisp");
   /**     */
   private Map objects = new HashMap();
   /**    */
   private Map resources = new HashMap();
   /**     */
   private List provided = new ArrayList();
   /**    */
   private List requested = new ArrayList();
   /**   */
   private DefferedMessages messages = new DefferedMessages();
   /** -   */
   private int objCount = 0;
   /**         */
   private void loadPending() {
     // resources
     Iterator it = resources.keySet().iterator();
     while (it.hasNext()) {
       String objectName = (String) it.next();
       ResourceEntry re = (ResourceEntry) resources.get(objectName);
       if (re.isLoaded()) {
 	continue;
       }
       re.setLoaded(true);
       log.fine("added resource provider " + objectName);
       provided.add(objectName.substring(0, objectName.lastIndexOf(":")));
     }
     it = objects.keySet().iterator();
     while (it.hasNext()) {
       String objectName = (String) it.next();
       ObjectEntry oe = (ObjectEntry) objects.get(objectName);
       if (oe.isLoaded()) {
 	continue;
       }
       log.config("trying to load object " + objectName);
       int requested = oe.getDepends().length;
       for (int i = 0; i < oe.depends.length; i++) {
 	if (provided.contains(oe.getDepends()[i])) {
 	  requested--;
 	} else {
 	  log.finer("dependency not met: " + oe.getDepends()[i]);
 	}
       }
       if (requested == 0) {
 	oe.getObject().start();
 	oe.setLoaded(true);
 	for (int i = 0; i < oe.getProvides().length; i++) {
 	  if (!provided.contains(oe.getProvides()[i])) {
 	    log.fine("added provider of " + oe.getProvides()[i]);
 	    provided.add(oe.getProvides()[i]);
 	  }
 	}
 	log.config(" ok. loaded = " + objectName);
 	Message m = getNewMessage("od_object_loaded", objectName, "stddispatcher", 0);
 	oe.getObject().addMessage(m);
       }
     }
   }
   /**    
       @param className   
       @param mult   
       @param param  
   */
   private void loadResource(String className, int mult, String param) {
     String logMessage = "loading resource " + className;
     for (int i = 0; i < mult; i++) {
       try {
 	Resource r = (Resource) Class.forName(className).newInstance();
 	ResourceEntry re = new ResourceEntry(className);
 	re.setResource(r);
 	log.fine("r instanceof ProxyResource " + (r instanceof ProxyResource) + " r.className:" + r.getClass().getName());
 	if (r instanceof ProxyResource) {
 	  ((ProxyResource) r).setResource(param);
 	  resources.put(param + ":" + i, re);
 	} else {
 	  resources.put(className + ":" + i, re);
 	}
 	logMessage += "+";
       } catch (ClassNotFoundException e) {
 	log.warning(" failed: " + e);
       } catch (InstantiationException e) {
 	log.warning(" failed: " + e);
       } catch (IllegalAccessException e) {
 	log.warning(" failed: " + e);
       }	    
     }
     logMessage += " ok.";
     log.config(logMessage);
   }
   /**   
       @param roName   
       @param code  
   */
   private void unloadResource(String roName, int code) {
     if (resources.containsKey(roName)) {
       ResourceEntry res = (ResourceEntry) resources.get(roName);
       List dependingObjs = new ArrayList();
       Iterator it = objects.keySet().iterator();
       while (it.hasNext()) {
 	String className = (String) it.next();
 	String[] depends = ((ObjectEntry) objects.get(className)).depends;
 	for (int i = 0; i < depends.length; i++) {
 	  if (depends[i].equals(roName.substring(0, roName.length() - roName.indexOf(":")))
 	      && !dependingObjs.contains(roName)) {
 	    dependingObjs.add(className);
 	  }
 	}
       }
       if (code == 0) {
 	it = dependingObjs.iterator();
 	while (it.hasNext()) {
 	  unloadObject((String) it.next(), code);
 	}
       }
       res.getResource().cleanUp(code);
       resources.remove(roName);
     }
   }
   /**    ( ޣ )
    * @param className   
    */
   private void loadObject(String className) {
     log.config("loading object " + className);
     try {
       Object params[] = new Object[1];
       params[0] = new Integer(objCount++);
       Class declParams[] = new Class[1];
       declParams[0] = params[0].getClass();
       ODObject load = (ODObject) Class.forName(className).getConstructor(declParams).newInstance(params);
       load.setDispatcher(this);
       synchronized (objects) {
 	ObjectEntry oe = new ObjectEntry(className, false, load.getDepends(), load.getProviding());
 	oe.object = load;
 	oe.loaded = false;
 	objects.put(load.getObjectName(), oe);
       }
     } catch (InvocationTargetException e) {
       log.warning(" failed: " + e);
     } catch (NoSuchMethodException e) {
       log.warning(" failed: " + e);
     } catch (ClassNotFoundException e) {
       log.warning(" failed: " + e);
     } catch (InstantiationException e) {
       log.warning(" failed: " + e);
     } catch (IllegalAccessException e) {
       log.warning(" failed: " + e);
     } catch (IllegalArgumentException e) {
       log.warning(" failed: " + e);
     }
   }
   /**      
    * ,    :
    * <ul>
    * <li>    
    * <li>   
    * <li>   
    * </ul>
    * @param objectName     .
    * @param code   ( code != 0  
    *  ).
    */
   private void unloadObject(String objectName, int code) {
     if (objects.containsKey(objectName)) {
       ObjectEntry oe = (ObjectEntry) objects.get(objectName);
       String[] provides = oe.provides;
       Iterator it = objects.keySet().iterator();
       List dependingObjs = new ArrayList();
       
       while (it.hasNext()) {
 	String className = (String) it.next();
 	String depends[] = ((ObjectEntry) objects.get(className)).depends;
 	for (int i = 0; i < provides.length; i++) {
 	  for (int j = 0; j < depends.length; j++) {
 	    if (provides[i].equals(depends[j]) && !dependingObjs.contains(className)) {
 	      dependingObjs.add(className);
 	    }
 	  }
 	}
       }
       if (code == 0) {
 	it = dependingObjs.iterator();
 	while (it.hasNext()) {
 	  String className = (String) it.next();
 	  if (objects.containsKey(className)) {
 	    log.fine("removing " + objectName + "'s dependency " + className);
 	    unloadObject(className, code);
 	  }
 	}
       }
       ODObject obj = oe.object;
       Message m = getNewMessage("od_cleanup", objectName, "stddispatcher", 0);
       m.addField(new Integer(code));
       sendMessage(m);
       obj.interrupt();
       objects.remove(objectName);
       log.config("\tobject " + objectName + " unloaded");
     }
   }
   /**       .
    *  multicast  
    * @param message   
    */
   public void sendMessage(Message message) {
     if (message.getAction().length() == 0) {
       return;
     }
     synchronized (objects) {
       Iterator it = objects.keySet().iterator();
       while (it.hasNext()) {
 	String className = (String) it.next();
 	ObjectEntry oe = (ObjectEntry) objects.get(className);
 	if (oe.blockedState || !oe.loaded) {
 	  log.finer("deffered message for " + className + " (loaded=" + oe.loaded + ")");
 	  messages.addMessage(className, message);
 	  continue;
 	}
 	ODObject objToSendTo = oe.object;
 	objToSendTo.addMessage(message);
 	synchronized (objToSendTo) {
 	  objToSendTo.notify();
 	}
       }
     }
   }
   /**       .
    *  multicast   
    * @param messageList    
    */
   public void sendMessages(Message[] messageList) {
     if (messageList.length == 0) {
       return;
     }
     for (int i = 0; i < messageList.length; i++) {
       Message message = messageList[i];
       if (message.getAction().length() == 0) {
 	continue;
       }
       synchronized (objects) {
 	Iterator it = objects.keySet().iterator();
 	while (it.hasNext()) {
 	  String className = (String) it.next();
 	  ObjectEntry oe = (ObjectEntry) objects.get(className);
 	  if (oe.blockedState || !oe.loaded) {
 	    log.finer("deffered message for " + className + " (loaded=" + oe.loaded + ")");
 	    messages.addMessage(className, message);
 	    continue;
 	  }
 	  ODObject objToSendTo = oe.object;
 	  objToSendTo.addMessage(message);
 	  synchronized (objToSendTo) {
 	    objToSendTo.notify();
 	  }
 	}
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
   public Message getNewMessage(String action, String destination, String origin, int inReplyTo) {
     return new StandartMessage(action, destination, origin, inReplyTo);
   }
   public Message getNewMessage() {
     return new StandartMessage();
   }
   /**       
    * @param objName  
    * @param state   
    */
   private void setBlockedState(String objName, boolean state) {
     if (!objects.containsKey(objName)) {
       return;
     }
     ((ObjectEntry) objects.get(objName)).setBlockedState(state);
   }
   /**         
    * @param objectName  
    */
   private void flushDefferedMessages(String objectName) {
     if (!objects.containsKey(objectName)) {
 		return;
     }
     ObjectEntry oe = (ObjectEntry) objects.get(objectName);
     if (!oe.isLoaded()) {
 		return;
     }
     ODObject objectRef = oe.object;
     objectRef.addMessages(messages.flush(objectName));
     synchronized (objectRef) {
       objectRef.notify();
     }
     loadPending();
   }
   /**     
    *   
    * @param objs    
    */
   public StandartDispatcher(List objs) {
     log.info(toString() + " starting up...");
     StandartDispatcherHandler stdh = new StandartDispatcherHandler(new Integer(0));
     ObjectEntry oe = new ObjectEntry(stdh.getClass().getName(), false, stdh.getDepends(), stdh.getProviding());
     oe.object = stdh;
     objects.put("stddispatcher", oe);
     loadPending();
     Iterator it = objs.iterator();
     Pattern p = Pattern.compile("(o:|(r:)(\\d+:)?)([^:]+)(:(.*))?");
     //                            type    mult     class  param
     while (it.hasNext()) {
       int mult = 1;
       String param = "";
       String className = (String) it.next();
       Matcher m = p.matcher(className);
       m.find();
       String parsedLine = "";
       for (int i = 0; i != m.groupCount(); i++) {
 	parsedLine += i + "='" + m.group(i) + "' ";
       }
       log.finest(parsedLine);
       if (m.groupCount() == 6) {
 	if (m.group(1).equals("o:")) {
 			loadObject(m.group(4));
 	}
 	if (m.group(1).startsWith("r:")) {
 	  if (m.group(3) != null) {
 	    mult = new Integer(m.group(3).substring(0, m.group(3).length() - 1)).intValue();
 	  }
 	  if (m.group(5) != null) {
                             param = m.group(5).substring(1);
 	  }
 	  loadResource(m.group(4), mult, param);
 	}
 	loadPending();
       }
     }
   }
   /**         */
   public static void usage() {
     log.severe("Usage: java com.novel.odisp.StandartDispatcher <file-with-list-of-ODobjects-to-load>");
     System.exit(0);
   }
   /**    StandartDispatcher.
    * @param args  0       ,   
    */
   public static void main(String args[]) {
     log.setLevel(java.util.logging.Level.ALL);
     if (args.length != 1) {
 		usage();
     } else {
       try {
 	BufferedReader cfg = new BufferedReader(new FileReader(args[0]));
 	List objs = new ArrayList();
 	String s;
 	while ((s = cfg.readLine()) != null) {
 	  if (!s.startsWith("#")) {
 	    objs.add(s);
 	  }
 	}
 	new StandartDispatcher(objs);
       } catch (FileNotFoundException e) {
 	log.severe("[e] configuration file " + args[0] + " not found.");
       } catch (IOException e) {
 	log.severe("[e] unable to read configuration file.");
       }
     }
   }
 
   /**       */
   private class ObjectEntry {
     /**    */
     private boolean loaded;
     /**    
      * @return  
      */
     public boolean isLoaded() {
       return loaded;
     }
     /**    
      * @param newLoaded   
      */
     public void setLoaded(boolean newLoaded) {
       loaded = newLoaded;
     }
     
     /**    */
     private String className;
     /**     
      * @return  
      */
     public String getClassName() {
       return className;
     }
     /**     
      * @param newClassName   
      */
     public void setClassName(String newClassName) {
       className = newClassName;
     }
     /**   */
     private boolean blockedState;
     /**    
      * @return  
      */
     public boolean isBlockedState() {
       return blockedState;
     }
     /**    
      * @param newBlockedState   
      */
     public void setBlockedState(boolean newBlockedState) {
       blockedState = newBlockedState;
     }
     /**    */
     private ODObject object;
     /**     
      * @return   
      */
     public ODObject getObject() {
       return object;
     }
     /**  */
     private String[] depends;
     /**    
      * @return  
     */
     public String[] getDepends() {
       return depends;
     }
     /**     
      * @param toRemove 
      */
     public void removeDepend(String toRemove) {
       String[] newDeps = new String[depends.length - 1];
       for (int i = 0; i < depends.length; i++) {
 	if (!depends[i].equals(toRemove)) {
 	  newDeps[i] = new String(depends[i]);
 	}
       }
       depends = newDeps;
     }
     /**   */
     private String[] provides;
     /**    
      * @return  
      */
     public String[] getProvides() {
       return provides;
     }
     /**   
      * @param cn  
      * @param bs   
      * @param depends  
      * @param provides  
      */
     public ObjectEntry(String cn, boolean bs, String[] depends, String[] provides) {
       className = cn;
       blockedState = bs;
       this.depends = depends;
       for (int i = 0; i < depends.length; i++) {
 	log.fine("object " + cn + " depends on " + depends[i]);
       }
       this.provides = provides;
     }
   }
   /**       */
   private class ResourceEntry {
     /**   */
     private boolean loaded;
     /**   
      * @return  
      */
     public boolean isLoaded() {
       return loaded;
     }
     /**   
      * @param newLoaded   
      */
     public void setLoaded(boolean newLoaded) {
       this.loaded = newLoaded;
     }
     
     /**    */
     private String className;
     /**    */
     private Resource resource;
     /**    
      * @return   
      */
     public Resource getResource() {
       return resource;
     }
     /**     
      * @param newResource   
      */
     public void setResource(Resource newResource) {
       resource = newResource;
     }
     /**  
      * @param cn   
      */
     public ResourceEntry(String cn) {
       loaded = false;
       className = cn;
     }
   }
   /**    */
   private class DefferedMessages {
     /**    */
     private Map queues = new HashMap();
     /**      
      * @param objName   ()
      * @param m   
      */
     public void addMessage(String objName, Message m) {
       if (!queues.containsKey(objName)) {
 	List messages = new ArrayList();
 	messages.add(m);
 	queues.put(objName, messages);
       } else {
 	((List) queues.get(objName)).add(m);
       }
     }
     /**      
      * @param objectName   ()
      * @return    
      */
     public List flush(String objectName) {
       if (queues.containsKey(objectName)) {
 	List res = new ArrayList((List) queues.get(objectName));
 	queues.remove(objectName);
 	return res;
       } else {
 	return new ArrayList();
       }
     }
   }
   /**    */
   private class StandartDispatcherHandler extends CallbackODObject {
     /**   */
     private String name = "stddispatcher";
     /**   
      * @return  
      */
     public String[] getProviding() {
       String res[] = new String[1];
       res[0] = "stddispatcher";
       return res;
     }
     /**    */
     protected void registerHandlers() {
       addHandler("unload_object", new MessageHandler() {
 	  public void messageReceived(Message msg) {
 	    if (msg.getFieldsCount() != 1) {
 	      return;
 	    }
 	    String name = (String) msg.getField(0);
 	    unloadObject(name, 1);
 	    objects.remove(name);
 	  }
 	});
       addHandler("load_object", new MessageHandler() {
 	  public void messageReceived(Message msg) {
 	    if (msg.getFieldsCount() != 1) {
 	      return;
 	    }
 	    String name = (String) msg.getField(0);
 	    loadObject(name);
 	    loadPending();
 	  }
 	});
       addHandler("od_shutdown", new MessageHandler(){
 	  public void messageReceived(Message msg) {
 	    int exitCode = 0;
 	    log.info(toString() + " shutting down...");
 	    if (msg.getFieldsCount() == 1) {
 	      exitCode = ((Integer) msg.getField(0)).intValue();
 	    }
 	    unloadObject("stddispatcher", exitCode);
 	  }
 	});
       addHandler("od_acquire", new MessageHandler() {
 	  public void messageReceived(Message msg) {
 	    if (msg.getFieldsCount() > 0) {
 	      String className = (String) msg.getField(0);
 	      boolean willBlockState = false;
 	      if (msg.getFieldsCount() == 2) {
 		willBlockState = ((Boolean) msg.getField(1)).booleanValue();
 	      }
 	      Iterator it = resources.keySet().iterator();
 	      while (it.hasNext()) { // first hit
 		String curClassName = (String) it.next();
 		if (Pattern.matches(className + ":\\d+", curClassName) && ((ResourceEntry) resources.get(curClassName)).loaded) {
 		  Message m = getNewMessage("resource_acquired", msg.getOrigin(), "stddispatcher", msg.getId());
 		  m.addField(curClassName);
 		  m.addField(((ResourceEntry) resources.get(curClassName)).resource);
 		  resources.remove(curClassName);
 		  sendMessage(m);
 		  setBlockedState(msg.getOrigin(), willBlockState);
 		  break;
 		}
 	      }
 	    }
 	  }
 	});
       addHandler("od_release", new MessageHandler() {
 	  public void messageReceived(Message msg) {
 	    if (msg.getFieldsCount() != 2) {
 	      return;
 	    }
 	    String className = (String) msg.getField(0);
 	    Resource res = (Resource) msg.getField(1);
 	    resources.put(className, new ResourceEntry(className.substring(0, className.length() - className.indexOf(":"))));
 	    flushDefferedMessages(msg.getOrigin());
 	    setBlockedState(msg.getOrigin(), false);
 	  }
 	});
       addHandler("od_list_objects", new MessageHandler() {
 	  public void messageReceived(Message msg) {
 	    Message m = getNewMessage("object_list", msg.getOrigin(), "stddispatcher", msg.getId());
 	    m.addField(new ArrayList(objects.keySet()));
 	    sendMessage(m);
 	  }
 	});
       addHandler("od_list_resources", new MessageHandler() {
 	  public void messageReceived(Message msg) {
 	    Message m = getNewMessage("resource_list", msg.getOrigin(), "stddispatcher", msg.getId());
 	    m.addField(new ArrayList(resources.keySet()));
 	    sendMessage(m);
 	  }
 	});
       addHandler("od_remove_dep", new MessageHandler() {
 	  public void messageReceived(Message msg) {
 	    if (msg.getFieldsCount() != 1) {
 	      return;
 	    }
 	    ObjectEntry oe = (ObjectEntry) objects.get(msg.getOrigin());
 	    oe.removeDepend((String) msg.getField(0));
 	  }
 	});
     }
     /**     
      * @param type  
      * @return  
      */
     public int cleanUp(int type) {
       return 0;
     }
     /**   
      * @param id   
      */
     public StandartDispatcherHandler(Integer id) {
       super("stddispatcher");
     }
   }
 }
