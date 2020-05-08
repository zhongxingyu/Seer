 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.storage.disk.utils;
 
 import java.io.Closeable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.zookeeper.CreateMode;
 import org.apache.zookeeper.KeeperException;
 import org.apache.zookeeper.ZooDefs.Ids;
 import org.apache.zookeeper.ZooKeeper;
 import org.apache.zookeeper.ZooKeeper.States;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.storage.disk.main.RootApplication;
 
 public class DiskProperties implements Closeable {
     private ZooKeeper zk = null;
 
     // Property keys
     public static final String UUID_KEY = "uuid";
     public static final String DISK_OWNER_KEY = "owner";
     public static final String DISK_VISIBILITY_KEY = "visibility";
     public static final String DISK_CREATION_DATE_KEY = "created";
     public static final String DISK_USERS_KEY = "users";
 
     public static final String STATIC_DISK_TARGET = "static";
     public static final String DISK_TARGET_LIMIT = "limit";
 
     public DiskProperties() {
         connect(RootApplication.CONFIGURATION.ZK_ADDRESSES, 3000);
     }
 
     public void close() {
         disconnect();
     }
 
     @Override
     protected void finalize() throws Throwable {
         close();
         super.finalize();
     }
 
     private void disconnect() {
         if (zk != null) {
             boolean closed = false;
             while (!closed) {
                 try {
                     zk.close();
                     closed = true;
                 } catch (InterruptedException consumed) {
                 }
             }
         }
     }
 
     private void connect(String addresses, int timeout) {
 
         try {
             zk = new ZooKeeper(addresses, timeout, null);
         } catch (Exception e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "Unable to connect to ZooKeeper: " + e.getMessage());
         }
 
         try {
             while (zk.getState() == States.CONNECTING) {
                 Thread.sleep(20);
             }
 
             if (zk.getState() != States.CONNECTED) {
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                         "Unable to connect to ZooKeeper");
             }
         } catch (InterruptedException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "Unable to connect to ZooKeeper: " + e.getMessage());
         }
 
         try {
             if (zk.exists(RootApplication.CONFIGURATION.ZK_DISKS_PATH, false) == null) {
                 zk.create(RootApplication.CONFIGURATION.ZK_DISKS_PATH,
                         "pdiskDisks".getBytes(), Ids.OPEN_ACL_UNSAFE,
                         CreateMode.PERSISTENT);
             }
             if (zk.exists(RootApplication.CONFIGURATION.ZK_USAGE_PATH, false) == null) {
                 zk.create(RootApplication.CONFIGURATION.ZK_USAGE_PATH,
                         "pdiskUsers".getBytes(), Ids.OPEN_ACL_UNSAFE,
                         CreateMode.PERSISTENT);
             }
         } catch (KeeperException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "ZooKeeper error: " + e.getMessage());
         } catch (InterruptedException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     e.getMessage());
         }
     }
 
     private String getNode(String root) {
         String node = "";
 
         try {
             node = new String(zk.getData(root, false, null));
         } catch (KeeperException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "ZooKeeper error: " + e.getMessage());
         } catch (InterruptedException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     e.getMessage());
         }
 
         return node;
     }
 
     private void createNode(String path, String content) {
         try {
             zk.create(path, content.getBytes(), Ids.OPEN_ACL_UNSAFE,
                     CreateMode.PERSISTENT);
         } catch (KeeperException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "ZooKeeper error: " + e.getMessage());
         } catch (InterruptedException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     e.getMessage());
         }
     }
 
     private void setNode(String root, String value) {
         try {
             zk.setData(root, value.getBytes(), -1);
         } catch (KeeperException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "ZooKeeper error: " + e.getMessage());
         } catch (InterruptedException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     e.getMessage());
         }
     }
 
     private void deleteNode(String root) {
         try {
             zk.delete(root, -1);
         } catch (KeeperException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "ZooKeeper error: " + e.getMessage());
         } catch (InterruptedException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     e.getMessage());
         }
     }
 
     public void deleteRecursively(String root) {
         List<String> tree = listSubTree(root);
         for (int i = tree.size() - 1; i >= 0; --i) {
             deleteNode(tree.get(i));
         }
     }
 
     public List<String> getDisks() {
         return getChildren(RootApplication.CONFIGURATION.ZK_DISKS_PATH);
     }
 
     public void saveDiskProperties(String diskRoot, Properties properties) {
         Enumeration<?> propertiesEnum = properties.propertyNames();
 
         createNode(diskRoot, properties.get(DiskProperties.UUID_KEY).toString());
 
         while (propertiesEnum.hasMoreElements()) {
             String key = (String) propertiesEnum.nextElement();
             String content = properties.getProperty(key);
 
             if (key != DiskProperties.UUID_KEY) {
                 createNode(diskRoot + "/" + key, content);
             }
         }
     }
 
     public Properties getDiskProperties(String root) {
         Properties properties = new Properties();
         List<String> tree = listSubTree(root);
 
         for (int i = tree.size() - 1; i >= 0; --i) {
             String key = MiscUtils.last(tree.get(i).split("/"));
             String content = getNode(tree.get(i));
 
             if (tree.get(i) == root) {
                 key = UUID_KEY;
             }
 
             properties.put(key, content);
         }
         return properties;
     }
 
     public Boolean pathExists(String path) {
         try {
             return zk.exists(path, false) != null;
         } catch (KeeperException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "ZooKeeper error: " + e.getMessage());
         } catch (InterruptedException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     e.getMessage());
         }
     }
 
     public int getDiskUsersNo(String diskPath) {
         Properties diskProp = getDiskProperties(diskPath);
         int users = 0;
 
         try {
             users = Integer.parseInt(diskProp
                     .get(DiskProperties.DISK_USERS_KEY).toString());
         } catch (NumberFormatException e) {
             // Assume there's not user
         }
 
         return users;
     }
 
     private String getNodeUsagePath(String node) {
         return RootApplication.CONFIGURATION.ZK_USAGE_PATH + "/" + node;
     }
 
     private String getVmUsagePath(String node, String vmId) {
         return getNodeUsagePath(node) + "/" + vmId;
     }
 
     private String getDiskUsagePath(String node, String vmId, String diskUuid) {
         return getVmUsagePath(node, vmId) + "/" + diskUuid;
     }
 
     private String getDiskPath(String uuid) {
         return RootApplication.CONFIGURATION.ZK_DISKS_PATH + "/" + uuid;
     }
 
     public void addDiskUser(String node, String vmId, String diskUuid,
             String target) {
         if (!pathExists(getNodeUsagePath(node))) {
             createNode(getNodeUsagePath(node), MiscUtils.getTimestamp());
         }
 
         if (pathExists(getVmUsagePath(node, vmId))) {
             setNode(getVmUsagePath(node, vmId), target);
         } else {
             createNode(getVmUsagePath(node, vmId), target);
         }
 
         // This should normally not happen
         if (pathExists(getDiskUsagePath(node, vmId, diskUuid))) {
             setNode(getDiskUsagePath(node, vmId, diskUuid), target);
         } else {
             createNode(getDiskUsagePath(node, vmId, diskUuid), target);
             setDiskUserCounter(diskUuid, +1);
         }
     }
 
     public void removeDiskUser(String node, String vmId, String diskUuid) {
         String diskPath = getDiskUsagePath(node, vmId, diskUuid);
 
         deleteNode(diskPath);
         setDiskUserCounter(diskUuid, -1);
     }
 
     public Boolean removeDiskUser(String node, String vmId) {
         String vmUsagePath = getVmUsagePath(node, vmId);
 
         if (!pathExists(vmUsagePath)) {
             return false;
         }
 
         List<String> diskUuids = getAttachedDisks(node, vmId);
 
         deleteRecursively(vmUsagePath);
 
         for (String disk : diskUuids) {
             setDiskUserCounter(disk, -1);
         }
 
         return true;
     }
 
     public List<String> getAttachedDisks(String node, String vmId) {
 
         List<String> disks = Collections.emptyList();
 
         String nodeUsagePath = getNodeUsagePath(node);
         String vmUsagePath = getVmUsagePath(node, vmId);
 
         if (pathExists(nodeUsagePath) && pathExists(vmUsagePath)) {
             disks = getChildren(vmUsagePath);
         }
 
         return disks;
     }
 
     public String diskTarget(String node, String vmId, String diskUuid) {
         String diskPath = getDiskUsagePath(node, vmId, diskUuid);
 
         if (!pathExists(diskPath)) {
             return STATIC_DISK_TARGET;
         }
 
         return getNode(diskPath);
     }
 
     public String nextHotpluggedDiskTarget(String node, String vmId) {
         String target = "vd";
         char hotplugged = 'a';
 
         if (pathExists(getVmUsagePath(node, vmId))) {
             if (!getNode(getVmUsagePath(node, vmId)).equals(STATIC_DISK_TARGET)) {
                 char[] currentTarget = getNode(getVmUsagePath(node, vmId))
                         .toCharArray();
                 hotplugged = ++(currentTarget[currentTarget.length - 1]);
 
                 if (hotplugged > 'z') {
                     return DISK_TARGET_LIMIT;
                 }
             }
         }
 
        return target + String.valueOf(hotplugged);
     }
 
     private void setDiskUserCounter(String diskUuid, int operation) {
         String path = getDiskPath(diskUuid);
         int currentUser = getDiskUsersNo(path);
 
         setNode(path + "/" + DISK_USERS_KEY,
                 String.valueOf(currentUser + operation));
     }
 
     public int remainingFreeUser(String path) {
         return RootApplication.CONFIGURATION.USERS_PER_DISK
                 - getDiskUsersNo(path);
     }
 
     private List<String> listSubTree(String pathRoot) {
         Deque<String> queue = new LinkedList<String>();
         List<String> tree = new ArrayList<String>();
 
         queue.add(pathRoot);
         tree.add(pathRoot);
 
         while (true) {
             String node = queue.pollFirst();
             if (node == null) {
                 break;
             }
 
             for (String child : getChildren(node)) {
                 String childPath = node + "/" + child;
                 queue.add(childPath);
                 tree.add(childPath);
             }
         }
         return tree;
     }
 
     private List<String> getChildren(String path) {
         List<String> children = null;
 
         try {
             children = zk.getChildren(path, false);
         } catch (KeeperException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "ZooKeeper error: " + e.getMessage());
         } catch (InterruptedException e) {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     e.getMessage());
         }
 
         return children;
     }
 
 }
