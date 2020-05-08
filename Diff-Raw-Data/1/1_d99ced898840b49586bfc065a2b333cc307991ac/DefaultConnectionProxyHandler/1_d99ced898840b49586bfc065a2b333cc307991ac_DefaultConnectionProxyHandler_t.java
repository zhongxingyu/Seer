 package Sirius.navigator.connection.proxy;
 
 /*******************************************************************************
  *
  * Copyright (c)	:	EIG (Environmental Informatics Group)
  * http://www.enviromatics.net
  * Prof. Dr. Reinesear Guettler
  * Prof. Dr. Ralf Denzer
  *
  * HTW
  * University of Applied Sciences
  * Goebenstr. 40
  * 66117 Saarbruecken, Germany
  *
  * Programmers	:	Pascal <pascal@enviromatics.net>
  *
  * Project		:	Sirius
  * Version		:	1.0
  * Purpose		:
  * Created		:	12/20/2002
  * History		:
  *
  *******************************************************************************/
 import java.lang.reflect.*;
 import java.util.*;
 
 import Sirius.util.image.*;
 import Sirius.server.middleware.types.*;
 import Sirius.server.localserver.method.MethodMap;
 import Sirius.server.newuser.*;
 import Sirius.server.search.*;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.connection.*;
 import Sirius.navigator.exception.ConnectionException;
 import java.util.ArrayList;
 
 /**
  * Default implementation of the connection proxy interface.
  *
  * @version 1.0 12/22/2002
  * @author Pascal
  */
 public class DefaultConnectionProxyHandler extends ConnectionProxyHandler {
 
     protected final ProxyInterface proxyHandler;
     protected ImageHashMap iconCache = null;
     protected ClassAndMethodCache classAndMethodCache = null;
     protected HashMap objectCache = new HashMap();
     // protected HashMap comparatorCache = new HashMap();
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
 
     public HashMap getClassHash() {
         return classAndMethodCache.getClassHash();
     }
 
     public DefaultConnectionProxyHandler(ConnectionSession connectionSession) {
         super(connectionSession);
         proxyHandler = new DefaultConnectionProxyHandler.DefaultConnectionProxy();
     }
 
     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         // debug ---------------------------------------------------------------
         /*if(log.isDebugEnabled())
         {
         log.debug("[ConnectionProxy] invoking method '" + method.getName() + "'");
         log.debug("[ConnectionProxy] method declaring class '" + method.getDeclaringClass().getName() + "'");
 
         Class[] exceptions = method.getExceptionTypes();
         if(exceptions != null)
         {
         for(int i = 0; i < exceptions.length; i++)
         {
         log.debug("[ConnectionProxy] method exception types '" + exceptions[i].getName() + "'");
         }
         }
         }*/
 
         try {
             if (method.getDeclaringClass().equals(Sirius.navigator.connection.Connection.class)) {
                 //if(log.isDebugEnabled()log.debug("[ConnectionProxy] invoking connection method '" + method.getName() + "'");
                 // icon cache --------------------------------------------------
                 if(method.getName().equals("getDefaultIcons")) {  // NOI18N
                     if(iconCache == null) {
                         if(log.isInfoEnabled())
                             log.info("[ConnectionProxy] filling icon cache");  // NOI18N
                         iconCache = (ImageHashMap) method.invoke(connection, args);
                     }
 
                     return iconCache;
                 } /*if(method.getName().equals("getMetaClass"))
                 {
                 if(classAndMethodCache == null)
                 {
                 log.info("[ConnectionProxy] filling meta class cache");
                 classAndMethodCache = new ClassAndMethodCache(session.getUser(), connection.getDomains());
                 }
 
                 return classAndMethodCache.getCachedClass((User)args[0], ((Integer)args[0]).intValue(), (String)args[2]);
                 }*/ else {
                     return method.invoke(connection, args);
                 }
             } else if (method.getDeclaringClass().equals(Sirius.navigator.connection.proxy.ProxyInterface.class)) {
                 //if(log.isDebugEnabled()log.debug("[ConnectionProxy] invoking proxy method '" + method.getName() + "'");
                 return method.invoke(proxyHandler, args);
             } else {
                 log.error("[ConnectionProxy] undeclared method '" + method.getName() + "'");  // NOI18N
                 throw new RuntimeException("[ConnectionProxy] undeclared method '" + method.getName() + "'");  // NOI18N
             }
         } catch (InvocationTargetException itex) {
             // ok, no need to worry about
             throw itex.getTargetException();
         } catch (Exception ex) {
             log.error("[ConnectionProxy] unexpected invocation exception' " + ex.getMessage() + "'", ex);  // NOI18N
             throw new RuntimeException("[ConnectionProxy] unexpected invocation exception' " + ex.getMessage() + "'");  // NOI18N
         }
     }
 
     class ClassAndMethodCache {
 
         private HashMap classHash = null;
         private HashMap methodHash = null;
         private List lsNames = null;
         //private MetaService metaServiceRef;
 
         /*
          * Konstruiert einen neuen leeren ClassCache. Der ClassCache wird beim
          * erstmaligen Ladens einer Sirius.Middleware.Types.Class gefuellt.
          *
          * @param remoteNode Das Remote Interface von dem die Classes geladen werden
          */
         public ClassAndMethodCache() //MetaService metaService)
         {
             classHash = new HashMap(25, 0.5f);
             methodHash = new HashMap(25, 0.5f);
             lsNames = new ArrayList(5);
             //metaServiceRef = metaService;
         }
 
         /*
          * Konstruiert einen neuen ClassCache, der mit den Classes eines bestimmten
          * Lokalservers gefuellt wird.
          *
          * @param remoteNode Das Remote Interface von dem die Classes geladen werden
          * @param user User
          * @param LocalServer des User
          */
         public ClassAndMethodCache(User user, String[] localServerNames) {
             classHash = new HashMap(50, 0.5f);
             methodHash = new HashMap(25, 0.5f);
             lsNames = new ArrayList((localServerNames.length + 1));
             //metaServiceRef = metaService;
             try {
                 MethodMap methodMap = connection.getMethods(user);
                 if (methodMap != null) {
                     this.putMethods(methodMap);
                 }
 
             } catch (Exception e) {
                 log.fatal("Ausnahme im ClassAndMethodCache beim Aufruf von remoteNodeRef.getMethods(...): ", e);  // NOI18N
 
                 //_TA_ErrorDialog errorDialog = new ErrorDialog("<html><p>ClassCache Fehler:</p><p>Die Classes konnten nicht vom Server geladen werden.</p></html>", e.toString(), ErrorDialog.WARNING);
                 //ErrorDialog errorDialog = new ErrorDialog(StringLoader.getString("STL@classCacheError"), e.toString(), ErrorDialog.WARNING);
                 //errorDialog.show();
             }
             for (int i = 0; i < localServerNames.length; i++) {
                 try {
                     MetaClass[] tmpClasses = connection.getClasses(user, localServerNames[i]); // .getClasses(user, localServerNames[i]);
                     // MethodMap methodMap = connection.getMethods(user, localServerNames[i]);
 
                     if (tmpClasses != null) {
                         putClasses(tmpClasses, localServerNames[i]);
                     }
 
 //                    if(methodMap != null)
 //                        this.putMethods(methodMap);
                 } catch (Exception e) {
                     log.fatal("Ausnahme im ClassAndMethodCache beim Aufruf von remoteNodeRef.getClasses(...): ", e);  // NOI18N
 
                     //_TA_ErrorDialog errorDialog = new ErrorDialog("<html><p>ClassCache Fehler:</p><p>Die Classes konnten nicht vom Server geladen werden.</p></html>", e.toString(), ErrorDialog.WARNING);
                     //ErrorDialog errorDialog = new ErrorDialog(StringLoader.getString("STL@classCacheError"), e.toString(), ErrorDialog.WARNING);
                     //errorDialog.show();
                 }
             }
         }
 
         public HashMap getClassHash() {
             return classHash;
         }
 
         /*
          * Laedt eine Class aus dem Cache bzw. vom Server.<br>
          * Ist die Class noch nicht im Cache enthalten wird sie vom Server geladen,
          * wurden von diesem LocalServer noch keine Classes geladen, so werden alle
          * Classes dieses LocalServers gecacht.
          *
          * @param user User.
          * @param classID Die ID der zu ladenden Class.
          * @param localServerName Der LocalServer des Users.
          *
          */
         public MetaClass getCachedClass(User user, int classID, String localServerName) throws ConnectionException {
             String key = new String(localServerName + classID);
             // Falls noch keine Class von diesem LocalServer geladen wurde,
             // -> alle Classes des LocalServer cachen
             if (!lsNames.contains(localServerName)) {
                 //NavigatorLogger.printMessage("keine Classes");
                 MetaClass[] tmpClasses = connection.getClasses(user, localServerName);
                 this.putClasses(tmpClasses, localServerName);
                 this.putMethods(connection.getMethods(user, localServerName));
                 if (log.isDebugEnabled()) {
                     log.debug("<CC> Classes von neuem LocalServer " + localServerName + " gecacht");
                 }
             }
 
             // Falls die Class nicht im Cache enthalten ist
             // -> Class vom Server laden
             if (!classHash.containsKey(key)) {
                 //NavigatorLogger.printMessage(key);
                 //NavigatorLogger.printMessage("keine Class");
                 MetaClass tmpClass = connection.getMetaClass(user, classID, localServerName);
                 this.putClass(tmpClass, localServerName);
                 this.putMethods(connection.getMethods(user, localServerName));
                 return tmpClass;
             } else {
                 return (MetaClass) classHash.get(key);
             }
         }
 
         /**
          * Liefert alle Classes, die sich im Cache befinden.
          *
          * @return Ein Array von Type Sirius.Middleware.Types.Class oder null.
          */
         public MetaClass[] getAllCachedClasses() {
             Vector classVector = new Vector(classHash.values());
 
             if (classVector == null) {
                 return null;
             }
 
             return (MetaClass[]) classVector.toArray(new MetaClass[classVector.size()]);
         }
 
         /*
          * Fuegt eine Class zum ClassCache hinzu.
          *
          * @param cls Die zu cachende Class.
          * @param lsID LocalServer ID, bildet zusammen mit der Class ID einen
          * einduetigen Schluessel fuer die Hashtable
          */
         protected void putClass(MetaClass cls, String lsID) {
             String key = new String(lsID + cls.getID());
             //NavigatorLogger.printMessage(key);
             if (!classHash.containsKey(key)) {
                 classHash.put(key, cls);
             }
         }
 
         /*
          * Fuegt ein Array von Classes zum ClassCache hinzu.
          *
          * @param classes Ein Array von cachenden Classes.
          * @param lsID LocalServer ID, bildet zusammen mit der Class ID einen
          * einduetigen Schluessel fuer die Hashtable
          */
         protected void putClasses(MetaClass[] classes, String localServerName) {
             lsNames.add(localServerName);
             for (int i = 0; i < classes.length; i++) {
                 String key = localServerName + classes[i].getID();
                 if (!classHash.containsKey(key)) {
                     classHash.put(key, classes[i]);
                 }
 
                 if (log.isDebugEnabled()) {
                     log.debug("<CMC> Class gecacht: " + classes[i].getName() + " " + classes[i].getID() + " " + classes[i].getDomain());
                 }
             }
             if (log.isDebugEnabled()) {
                 log.debug("<CMC> " + classes.length + " Classes von LocalServer " + localServerName + " gecacht.");
             }
         }
 
         public Sirius.server.localserver.method.Method getCachedMethod(String methodKey) {
             return (Sirius.server.localserver.method.Method) methodHash.get(methodKey);
         }
 
         protected void putMethod(Sirius.server.localserver.method.Method method, String localServerName) {
             String key = new String(localServerName + method.getID());
             if (!methodHash.containsKey(key)) {
                 methodHash.put(key, method);
                 if (log.isDebugEnabled()) {
                     log.debug("<CMC> method '" + key + "' gecacht.");
                 }
             }
         }
 
         protected void putMethods(MethodMap methodMap) {
             if (methodMap != null) {
                 methodHash.putAll(methodMap);
                 if (log.isDebugEnabled()) {
                     Iterator iterator = methodMap.keySet().iterator();
                     if (iterator.hasNext()) {
                         log.debug("<CMC> method '" + iterator.next() + " gecacht.");//NOI18N
                     }
                 }
             }
         }
     }
 
     class DefaultConnectionProxy implements ProxyInterface {
 
         public void setProperty(String name, String value) {
             if (log.isDebugEnabled()) {
                 log.debug("[ProxyInterface] setting propety '" + name + "' to '" + value + "'");
             }
         }
 
         public ConnectionSession getSession() {
             return session;
         }
 
         public Node[] getRoots() throws ConnectionException {
             return connection.getRoots(session.getUser());
         }
 
         public Node[] getRoots(String domain) throws ConnectionException {
             return connection.getRoots(session.getUser(), domain);
         }
 
 //        public Node[] getChildren(int nodeID, String domain) throws ConnectionException
 //        {
 //            Node[] c = connection.getChildren(session.getUser(), nodeID, domain);
 //
 //            return sortNodes(c,null,true);
 //        }
 //
         public Node[] getChildren(Node node) throws ConnectionException {
             Node[] c = connection.getChildren(node, session.getUser());
 
             if (node.isDynamic() && node.isSqlSort()) {
                 return c;
             }
 
             return Sirius.navigator.tools.NodeSorter.sortNodes(c);
         }
 
         public Node getNode(int nodeID, String domain) throws ConnectionException {
             return connection.getNode(session.getUser(), nodeID, domain);
         }
 
         // .....................................................................
         public Node addNode(Node node, Link parent) throws ConnectionException {
             return connection.addNode(node, parent, session.getUser());
         }
 
         public boolean deleteNode(Node node) throws ConnectionException {
             return connection.deleteNode(node, session.getUser());
         }
 
         public boolean addLink(Node from, Node to) throws ConnectionException {
             return connection.addLink(from, to, session.getUser());
         }
 
         public boolean deleteLink(Node from, Node to) throws ConnectionException {
             return connection.deleteLink(from, to, session.getUser());
         }
 
 //        public boolean copySubTree(Node root) throws ConnectionException
 //        {
 //            return connection.copySubTree(root, session.getUser());
 //        }
         // .....................................................................
         public Node[] getClassTreeNodes() throws ConnectionException {
             return connection.getClassTreeNodes(session.getUser());
         }
 
         public HashMap getSearchOptions() throws ConnectionException {
             if (log.isDebugEnabled()) {
                 //HashMap searchOptions = connection.getSearchOptions(session.getUser(), session.getUser().getDomain());
                 HashMap searchOptions = connection.getSearchOptions(session.getUser());
                 log.info(searchOptions.size() + " search options loaded");  // NOI18N
                 return searchOptions;
             } else {
                 return connection.getSearchOptions(session.getUser());
             }
         }
 
         public SearchResult search(Collection classIds, Collection searchOptions) throws ConnectionException {
             log.fatal(classIds);
             for (Object so : searchOptions) {
                 SearchOption sopt = (SearchOption) so;
                 log.fatal(sopt);
             }
             return connection.search(session.getUser(), (String[]) classIds.toArray(new String[classIds.size()]), (SearchOption[]) searchOptions.toArray(new SearchOption[searchOptions.size()]));
         }
 
         public SearchResult search(Collection searchOptions) throws ConnectionException {
             return this.search(new LinkedList(), searchOptions);
         }
 
         public int addQuery(String name, String description, String statement) throws ConnectionException {
             return connection.addQuery(session.getUser(), name, description, statement);
         }
 
         public int addQuery(String name, String description, String statement, int resultType, char isUpdate, char isRoot, char isUnion, char isBatch) throws ConnectionException {
             return connection.addQuery(session.getUser(), name, description, statement, resultType, isUpdate, isRoot, isUnion, isBatch);
         }
 
         public boolean addQueryParameter(int queryId, String paramkey, String description) throws ConnectionException {
             return connection.addQueryParameter(session.getUser(), queryId, paramkey, description);
         }
 
         public boolean addQueryParameter(int queryId, int typeId, String paramkey, String description, char isQueryResult, int queryPosition) throws ConnectionException {
             return connection.addQueryParameter(session.getUser(), queryId, typeId, paramkey, description, isQueryResult, queryPosition);
         }
 
         public MetaClass[] getClasses() throws ConnectionException {
             String[] domains = connection.getDomains();
             ArrayList classes = new ArrayList();
 
 
             for (int i = 0; i < domains.length; i++) {
                 MetaClass[] classArray = new MetaClass[0];
                 try {
                     classArray = this.getClasses(domains[i]);
                 } catch (Throwable t) {
                     log.error("Fehler im DefaultConnectionProxyHandler bei getClasses", t);
                     //throw new ConnectionException(t.getMessage());
                 }
 
                 for (int j = 0; j < classArray.length; j++) {
                     classes.add(classArray[j]);
                 }
             }
 
 
 //            if(classes.isEmpty())
 //                throw new ConnectionException("could not load classes from localservers");
 
             return (MetaClass[]) classes.toArray(new MetaClass[classes.size()]);
         }
 
         public MetaClass[] getClasses(String domain) throws ConnectionException {
             return connection.getClasses(session.getUser(), domain);
         }
 
         public void initClassAndMethodCache() throws ConnectionException {
             classAndMethodCache = new ClassAndMethodCache(session.getUser(), connection.getDomains());
         }
 
         public MetaClass getMetaClass(int classID, String domain) throws ConnectionException {
             if (classAndMethodCache == null) {
                 if(log.isInfoEnabled())
                     log.info("[ConnectionProxy] filling meta class cache");  // NOI18N
                 classAndMethodCache = new ClassAndMethodCache(session.getUser(), connection.getDomains());
             }
 
 
             MetaClass metaClass = classAndMethodCache.getCachedClass(session.getUser(), classID, domain);
             if (log.isDebugEnabled()) {
                 log.debug("getgetMetaClass(): classID=" + classID + ", domain=" + domain);
                 log.debug("MetaClass: " + metaClass + "\nMetaClass.getName(): " + metaClass.getName() + "\nMetaClass.getEditor(): " + metaClass.getEditor() + "\nMetaClass.getComplexEditor(): " + metaClass.getComplexEditor());
             }
             return metaClass;
 
             //return classAndMethodCache.getCachedClass(session.getUser(), classID, domain);
         }
 
         public MetaClass getMetaClass(String classKey) throws ConnectionException {
             try {
                 StringTokenizer tokenizer = new StringTokenizer(classKey, "@");  // NOI18N
                 int classID = Integer.valueOf(tokenizer.nextToken()).intValue();
                 String domain = tokenizer.nextToken();
 
                 return this.getMetaClass(classID, domain);
 
             } catch (ConnectionException cexp) {
                 throw cexp;
             } catch (Throwable t) {
                 log.error("malformed classKey: '" + classKey + "' (classId@domain expected)");  // NOI18N
                 throw new ConnectionException("malformed class key: '" + classKey + "' (classId@domain expected)", ConnectionException.ERROR, t);  // NOI18N
             }
         }
 
         public Sirius.server.localserver.method.Method getMethod(String methodKey) throws ConnectionException {
             if (classAndMethodCache == null) {
                 if(log.isInfoEnabled())
                     log.info("[ConnectionProxy] filling meta class cache");  // NOI18N
                 classAndMethodCache = new ClassAndMethodCache(session.getUser(), connection.getDomains());
             }
 
             return classAndMethodCache.getCachedMethod(methodKey);
         }
 
         public MetaObject getMetaObject(int objectID, int classID, String domain) throws ConnectionException {
 
             if(log.isDebugEnabled())
                 log.debug("getMetaObject(): objectID=" + objectID + ", classID=" + classID + ", domain=" + domain);  // NOI18N
             MetaObject MetaObject = connection.getMetaObject(session.getUser(), objectID, classID, domain);
             log.debug(" MetaObject: " + MetaObject + " MetaObject.getName(): " + MetaObject.getName() + " MetaObject.getEditor(): " + MetaObject.getEditor() + " MetaObject.getComplexEditor(): " + MetaObject.getComplexEditor());
 
 
 //            if(MetaObject.getMetaClass()==null)
 //            {
 //                MetaClass mc =classAndMethodCache.getCachedClass(session.getUser(),classID,domain);
 //
 //                if(mc!=null)
 //                {
 //                    mc.setlog();
 //                    log.debug("Set Class for MO :: "+mc);
 //                    MetaObject.setMetaClass(mc);
 //                }
 //
 //            }
 //
             //set Classes in SubObjects as well
             MetaObject.setAllClasses(classAndMethodCache.getClassHash());
 
             return MetaObject;
 
             //return connection.getMetaObject(session.getUser(), objectID, classID, domain);
         }
 
         public MetaObject getMetaObject(String objectId) throws ConnectionException {
             try {
                 StringTokenizer tokenizer = new StringTokenizer(objectId, "@");  // NOI18N
                 int objectID = Integer.valueOf(tokenizer.nextToken()).intValue();
                 int classID = Integer.valueOf(tokenizer.nextToken()).intValue();
                 String domain = tokenizer.nextToken();
 
                 return this.getMetaObject(objectID, classID, domain);
 
             } catch (ConnectionException cexp) {
                 throw cexp;
             } catch (Throwable t) {
                 log.error("malformed object id: '" + objectId + "' (objectID@classID@domain expected)");  // NOI18N
                 throw new ConnectionException("malformed object id: '" + objectId + "' (objectID@classID@domain expected)", ConnectionException.ERROR, t);  // NOI18N
             }
         }
 
         public MetaObject[] getMetaObject(Query query) throws ConnectionException {
 
             MetaObject[] obs = connection.getMetaObject(session.getUser(), query);
 
             for (int i = 0; i < obs.length; i++) {
                 if (obs[i] != null) {
                     obs[i].setAllClasses(classAndMethodCache.getClassHash());
                 }
             }
 
             return obs;
 
         }
 
         public MetaObject[] getMetaObjectByQuery(String query, int sig) throws ConnectionException {
             if (classAndMethodCache == null) {
                 initClassAndMethodCache();
             }
            if(log.isDebugEnabled())
                 log.debug("getMetaObjectByQuery");  // NOI18N
             try {
                 MetaObject[] obs = connection.getMetaObjectByQuery(session.getUser(), query);
 
                 for (int i = 0; i < obs.length; i++) {
                     if (obs[i] != null) {
                         obs[i].setAllClasses(classAndMethodCache.getClassHash());
                     }
                 }
                 return obs;
             } catch (Throwable t) {
                 log.warn("Fehler in getMetaObjectByQuery", t);
             }
             return null;
         }
 
         public MetaObject insertMetaObject(MetaObject MetaObject, String domain) throws ConnectionException {
             return connection.insertMetaObject(session.getUser(), MetaObject, domain);
         }
 
         public int insertMetaObject(Query query, String domain) throws ConnectionException {
             return connection.insertMetaObject(session.getUser(), query, domain);
         }
 
         public int updateMetaObject(MetaObject MetaObject, String domain) throws ConnectionException {
             return connection.updateMetaObject(session.getUser(), MetaObject, domain);
         }
 
         public int deleteMetaObject(MetaObject MetaObject, String domain) throws ConnectionException {
             return connection.deleteMetaObject(session.getUser(), MetaObject, domain);
         }
 
         public MetaObject getInstance(MetaClass c) throws ConnectionException {
 //Hell wegen editorDoppeleffekten
 //            MetaObject MetaObject = null;
 //            if(objectCache.containsKey(c)) {
 //                MetaObject = (MetaObject)objectCache.get(c);
 //            } else {
 //                MetaObject = connection.getInstance(session.getUser(), c);
 //                MetaObject.setAllClasses(classAndMethodCache.getClassHash());
 //                objectCache.put(c, MetaObject);
 //            }
 //            boolean t=(MetaObject==null);
 //            return MetaObject;
 
             MetaObject MetaObject = null;
 
             MetaObject = connection.getInstance(session.getUser(), c);
             MetaObject.setAllClasses(classAndMethodCache.getClassHash());
             return MetaObject;
         }
     }
 }
