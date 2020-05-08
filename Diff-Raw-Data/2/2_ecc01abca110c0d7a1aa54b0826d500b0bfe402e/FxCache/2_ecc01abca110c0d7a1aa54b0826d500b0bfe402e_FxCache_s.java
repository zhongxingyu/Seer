 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.ejb.mbeans;
 
 import com.flexive.core.stream.BinaryDownloadProtocol;
 import com.flexive.core.stream.BinaryUploadProtocol;
 import com.flexive.core.structure.StructureLoader;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.cache.FxBackingCache;
 import com.flexive.shared.cache.FxBackingCacheProvider;
 import com.flexive.shared.cache.FxBackingCacheProviderFactory;
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.mbeans.FxCacheMBean;
 import com.flexive.shared.mbeans.MBeanHelper;
 import com.flexive.shared.stream.FxStreamUtils;
 import com.flexive.stream.ServerLocation;
 import com.flexive.stream.StreamServer;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.cache.Cache;
 
 import javax.management.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * FxCache MBean
  * TODO: implement missing skeletons ...
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 //@Service(objectName = CacheAdmin.CACHE_SERVICE_NAME)
 public class FxCache implements FxCacheMBean, DynamicMBean {
     private static final Log LOG = LogFactory.getLog(FxCache.class);
 
 
     private StreamServer server = null;
     private long nodeStartupTime = -1;
 
     private FxBackingCacheProvider cacheProvider = null;
 
 
     /**
      * Get the backing cache
      *
      * @return FxBackingCache
      * @throws FxCacheException on errors
      */
     private FxBackingCache getBackingCache() throws FxCacheException {
         if (cacheProvider == null) {
             //start the cache
             cacheProvider = FxBackingCacheProviderFactory.createNew();
             LOG.info("Starting backing Cache {" + cacheProvider.getDescription() + "}");
             cacheProvider.init();
             if (cacheProvider.getInstance().get("/" + this.getClass().getName(), SYSTEM_UP_KEY) == null) {
                 cacheProvider.getInstance().put("/" + this.getClass().getName(), SYSTEM_UP_KEY, System.currentTimeMillis());
             }
             nodeStartupTime = System.currentTimeMillis();
         }
         return cacheProvider.getInstance();
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public void create() throws Exception {
         if (server != null) return;
 
         //switch to UTF-8 encoding
         if (!"UTF-8".equals(System.getProperty("file.encoding"))) {
             // set default charset to UTF-8
             LOG.warn("Changing system character encoding from " + System.getProperty("file.encoding") + " to UTF-8.");
             System.setProperty("file.encoding", "UTF-8");
         }
         //start streamserver
         try {
             server = new StreamServer(FxStreamUtils.probeNetworkInterfaces(), FxCacheMBean.STREAMING_PORT);
             server.addProtocol(new BinaryUploadProtocol());
             server.addProtocol(new BinaryDownloadProtocol());
             server.start();
             List<ServerLocation> servers = null;
             if (globalExists(CacheAdmin.STREAMSERVER_BASE, CacheAdmin.STREAMSERVER_EJB_KEY))
                 servers = (List<ServerLocation>) globalGet(CacheAdmin.STREAMSERVER_BASE, CacheAdmin.STREAMSERVER_EJB_KEY);
             if (servers == null)
                 servers = new ArrayList<ServerLocation>(5);
             ServerLocation thisServer = new ServerLocation(server.getAddress().getAddress(), server.getPort());
             if ((thisServer.getAddress().isLinkLocalAddress() || thisServer.getAddress().isAnyLocalAddress() || thisServer.getAddress().isLoopbackAddress()))
                 FxStreamUtils.addLocalServer(thisServer);
             else if (!servers.contains(thisServer)) //only add if not contained already and not bound to a local address
                 servers.add(thisServer);
             globalPut(CacheAdmin.STREAMSERVER_BASE, CacheAdmin.STREAMSERVER_EJB_KEY, servers);
            LOG.info("Added " + thisServer + " to available StreamServers (" + servers.size() + " total)");
         } catch (Exception e) {
             LOG.error("Failed to start StreamServer. Error: " + e.getMessage(), e);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void destroy() throws Exception {
         //System.out.println("about to uninstall timer");
         //EJBLookup.getTimerServiceInterface().uninstall();
         //System.out.println("timers uninstalled");
         if (server != null) {
             LOG.info("Shutting down StreamServer {" + server.getDescription() + "}");
             try {
                 server.stop();
             } catch (Exception e) {
                 LOG.error(e, e);
             }
             server = null;
         }
         try {
             if (cacheProvider != null) {
                 cacheProvider.shutdown();
                 cacheProvider = null;
             }
         } catch (FxCacheException e) {
             LOG.error(e, e);
         }
     }
 
 
     //TODO: finish me!
     private static MBeanInfo info = new MBeanInfo(
             FxCache.class.getCanonicalName(),
             "[fleXive] Cache MBean",
             new MBeanAttributeInfo[]{
                     new MBeanAttributeInfo("FxCache", FxCache.class.getCanonicalName(), "", true, false, false)
             },
             new MBeanConstructorInfo[]{
             },
             new MBeanOperationInfo[]{
                     new MBeanOperationInfo("get", "",
                             new MBeanParameterInfo[]{
                                     new MBeanParameterInfo("path", "java.lang.String", ""),
                                     new MBeanParameterInfo("key", "java.lang.Object", "")
                             }, "java.lang.Object", MBeanOperationInfo.INFO),
                     new MBeanOperationInfo("put", "",
                             new MBeanParameterInfo[]{
                                     new MBeanParameterInfo("path", "java.lang.String", ""),
                                     new MBeanParameterInfo("key", "java.lang.Object", ""),
                                     new MBeanParameterInfo("value", "java.lang.Object", "")
                             }, "void", MBeanOperationInfo.ACTION)
             },
             new MBeanNotificationInfo[]{
             }
     );
 
 
     /**
      * {@inheritDoc}
      */
     public String getDeploymentId() {
         return MBeanHelper.DEPLOYMENT_ID;
     }
 
     /**
      * Includes the division id into the path.
      *
      * @param path the path to encode
      * @return the encoded path
      * @throws FxCacheException if the division id could not be resolved
      */
     private String divisionEncodePath(String path) throws FxCacheException {
         try {
             int divId;
             //#<id>  - purposely undocumented hack to force a division ;) - used during environment loading
             if (path.charAt(0) == '#') {
                 try {
                     divId = Integer.parseInt(path.substring(1, path.indexOf('/')));
                     path = path.substring(path.indexOf('/'));
                 } catch (Exception e) {
                     throw new FxCacheException("Invalid Division Id in path [" + path + "]!");
                 }
             } else {
                 FxContext ri = FxContext.get();
                 if (ri.getDivisionId() == -1) {
                     throw new FxCacheException("Division ID missing in request information [" + ri.getRequestURI() + "]");
                 }
                 divId = ri.getDivisionId();
             }
             return "/Division" + divId + (path.startsWith("/") ? "" : "/") + path;
         } catch (Throwable t) {
             LOG.error("Unable to encode division ID in cache path: " + t.getMessage(), t);
             throw new FxCacheException("Unable to encode path: " + t.getMessage());
         }
     }
 
     /**
      * Includes the global division id into the path.
      *
      * @param path the path to encode
      * @return the encoded path
      */
     private String globalDivisionEncodePath(final String path) {
         return "/GlobalConfiguration" + (path.startsWith("/") ? "" : "/") + path;
     }
 
     /**
      * {@inheritDoc}
      */
     public Cache<Object, Object> getCache() throws FxCacheException {
         return getBackingCache().getCache();
     }
 
     /**
      * {@inheritDoc}
      */
     public Object get(String path, Object key) throws FxCacheException {
         return getBackingCache().get(divisionEncodePath(path), key);
     }
 
     /**
      * {@inheritDoc}
      */
     public Object globalGet(String path, Object key) throws FxCacheException {
         return getBackingCache().get(globalDivisionEncodePath(path), key);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean globalExists(String path, Object key) throws FxCacheException {
         return getBackingCache().exists(globalDivisionEncodePath(path), key);
     }
 
     /**
      * {@inheritDoc}
      */
     public void put(String path, Object key, Object value) throws FxCacheException {
         getBackingCache().put(divisionEncodePath(path), key, value);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean exists(String path, Object key) throws FxCacheException {
         return getBackingCache().exists(divisionEncodePath(path), key);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void globalPut(String path, Object key, Object value) throws FxCacheException {
         getBackingCache().put(globalDivisionEncodePath(path), key, value);
     }
 
     /**
      * {@inheritDoc}
      */
     public void remove(String path) throws FxCacheException {
         getBackingCache().remove(divisionEncodePath(path));
     }
 
     /**
      * {@inheritDoc}
      */
     public void globalRemove(String path) throws FxCacheException {
         getBackingCache().remove(globalDivisionEncodePath(path));
     }
 
     /**
      * {@inheritDoc}
      */
     public Set getKeys(String path) throws FxCacheException {
         return getBackingCache().getKeys(divisionEncodePath(path));
     }
 
     /**
      * {@inheritDoc}
      */
     public Set globalGetKeys(String path) throws FxCacheException {
         return getBackingCache().getKeys(globalDivisionEncodePath(path));
     }
 
 
     /**
      * {@inheritDoc}
      */
     public Set getChildrenNames(String path) throws FxCacheException {
         return getBackingCache().getChildrenNames(divisionEncodePath(path));
     }
 
     /**
      * {@inheritDoc}
      */
     public void remove(String path, Object key) throws FxCacheException {
         getBackingCache().remove(divisionEncodePath(path), key);
     }
 
     /**
      * {@inheritDoc}
      */
     public void globalRemove(String path, Object key) throws FxCacheException {
         getBackingCache().remove(globalDivisionEncodePath(path), key);
     }
 
     /**
      * {@inheritDoc}
      */
     public void reloadEnvironment(Integer divisionId) throws Exception {
         StructureLoader.load(divisionId, true, null);
     }
 
     /**
      * {@inheritDoc}
      */
     public void setEvictionStrategy(Integer divisionId, String path, Integer maxContents, Integer timeToIdle,
                                     Integer timeToLive) throws FxCacheException {
         setEvictionStrategy(divisionId, path, maxContents, timeToIdle, timeToLive, true);
     }
 
     /**
      * {@inheritDoc}
      */
     public void setEvictionStrategy(Integer divisionId, String path, Integer maxContents, Integer timeToIdle, Integer timeToLive, Boolean overwrite) throws FxCacheException {
         cacheProvider.setEvictionStrategy("/Division" + divisionId + (path.charAt(0) == '/' ? path : '/' + path),
                 maxContents, timeToIdle, timeToLive, overwrite);
     }
 
     /**
      * {@inheritDoc}
      */
     public long getSystemStartTime() {
         try {
             return (Long) getBackingCache().get("/" + this.getClass().getName(), SYSTEM_UP_KEY);
         } catch (Exception exc) {
             return -1;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public long getNodeStartTime() {
         return nodeStartupTime;
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
         /*if ("FxCache".equals(attribute))
             return getCache();
         else*/
         if ("DeploymentId".equals(attribute))
             return getDeploymentId();
         else if ("SystemStartTime".equals(attribute))
             return getSystemStartTime();
         else if ("NodeStartTime".equals(attribute))
             return getNodeStartTime();
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * {@inheritDoc}
      */
     public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
         //TODO: code me!
     }
 
     /**
      * {@inheritDoc}
      */
     public AttributeList getAttributes(String[] attributes) {
         //TODO: code me!
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public AttributeList setAttributes(AttributeList attributes) {
         //TODO: code me!
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public Object invoke(String actionName, Object params[], String signature[]) throws MBeanException, ReflectionException {
         try {
             if ("get".equals(actionName)) {
                 return get((String) params[0], params[1]);
             } else if ("put".equals(actionName)) {
                 put((String) params[0], params[1], params[2]);
             } else if ("remove".equals(actionName) && params.length == 1) {
                 remove((String) params[0]);
             } else if ("remove".equals(actionName) && params.length == 2) {
                 remove((String) params[0], params[1]);
             } else if ("exists".equals(actionName) && params.length == 2) {
                 return exists((String) params[0], params[1]);
             } else if ("getKeys".equals(actionName)) {
                 return getKeys((String) params[0]);
             } else if ("globalGet".equals(actionName)) {
                 return globalGet((String) params[0], params[1]);
             } else if ("globalPut".equals(actionName)) {
                 globalPut((String) params[0], params[1], params[2]);
             } else if ("globalRemove".equals(actionName) && params.length == 1) {
                 globalRemove((String) params[0]);
             } else if ("globalRemove".equals(actionName) && params.length == 2) {
                 globalRemove((String) params[0], params[1]);
             } else if ("globalExists".equals(actionName) && params.length == 2) {
                 return globalExists((String) params[0], params[1]);
             } else if ("globalGetKeys".equals(actionName) && params.length == 1) {
                 return globalGetKeys((String) params[0]);
             } else if ("getChildrenNames".equals(actionName)) {
                 return getChildrenNames((String) params[0]);
             } else if ("reloadEnvironment".equals(actionName)) {
                 reloadEnvironment((Integer) params[0]);
             } else if ("create".equals(actionName)) {
                 create();
             } else if ("destroy".equals(actionName)) {
                 destroy();
             } else if ("setEvictionStrategy".equals(actionName) && params.length == 5) {
                 setEvictionStrategy((Integer) params[0], (String) params[1],
                         (Integer) params[2], (Integer) params[3], (Integer) params[4]);
             } else if ("setEvictionStrategy".equals(actionName) && params.length == 6) {
                 setEvictionStrategy((Integer) params[0], (String) params[1],
                         (Integer) params[2], (Integer) params[3], (Integer) params[4],
                         (Boolean) params[5]);
             } else {
                 LOG.warn("Tried to call [" + actionName + "] which is not implemented!");
             }
         } catch (Exception e) {
             LOG.error("Failed to invoke MBean op: " + e.getMessage(), e);
             throw new MBeanException(e);
         }
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public MBeanInfo getMBeanInfo() {
         return info;
     }
 }
