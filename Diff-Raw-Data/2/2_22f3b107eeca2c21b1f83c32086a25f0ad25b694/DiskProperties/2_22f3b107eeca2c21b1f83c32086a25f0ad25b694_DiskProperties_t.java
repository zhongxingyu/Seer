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
 import java.util.logging.Logger;
 
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
 
     private static final String DEVICE_PREFIX = "vd";
 
     private static final String ZK_USAGE_PATH = "/usage";
 
     private static final String ZK_DISKS_PATH = "/disks";
 
     private static final String ZK_TARGETS_PATH = "/targets";
 
     // Property keys
     public static final String UUID_KEY = "uuid";
     public static final String DISK_OWNER_KEY = "owner";
     public static final String DISK_VISIBILITY_KEY = "visibility";
     public static final String DISK_CREATION_DATE_KEY = "created";
     public static final String DISK_USERS_KEY = "users";
     public static final String DISK_READ_ONLY_KEY = "isreadonly";
     public static final String DISK_COW_BASE_KEY = "iscow";
 	public static final String DISK_TAG_KEY = "tag";
 	public static final String DISK_SIZE_KEY = "size";
 
     public static final String STATIC_DISK_TARGET = "static";
     public static final String DISK_TARGET_LIMIT = "limit";
 
 	public static final Object DISK_IDENTIFER_KEY = "Marketplace_id";
 
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
             if (zk.exists(ZK_DISKS_PATH, false) == null) {
                 zk.create(ZK_DISKS_PATH, "pdiskDisks".getBytes(),
                         Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
             }
             if (zk.exists(ZK_USAGE_PATH, false) == null) {
                 zk.create(ZK_USAGE_PATH, "pdiskUsers".getBytes(),
                         Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
             }
             if (zk.exists(ZK_TARGETS_PATH, false) == null) {
                 zk.create(ZK_TARGETS_PATH, "pdiskTargets".getBytes(),
                         Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
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
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
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
 
     private void createNodeOrFail(String path, String content)
             throws KeeperException.NodeExistsException {
 
         try {
             zk.create(path, content.getBytes(), Ids.OPEN_ACL_UNSAFE,
                     CreateMode.PERSISTENT);
         } catch (KeeperException.NodeExistsException e) {
             // If this already exists, rethrow the exception to allow
             // the client to deal with the fact that the node exists.
             throw e;
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
 
     public void deleteRecursively(String uuid) {
 
         String root = getDiskZkPath(uuid);
 
         List<String> tree = listSubTree(root);
         for (int i = tree.size() - 1; i >= 0; --i) {
             deleteNode(tree.get(i));
         }
     }
 
     private static String getDiskZkPath(String uuid) {
     	return String.format("%s/%s", ZK_DISKS_PATH, uuid);
     }
 
     public Boolean diskExists(String uuid) {
         return pathExists(getDiskZkPath(uuid));
     }
 
     public List<String> getDisks() {
         return getChildren(ZK_DISKS_PATH);
     }
 
     public void saveDiskProperties(Properties properties) {
 
         String uuid = properties.getProperty(UUID_KEY);
 
         String diskRoot = getDiskZkPath(uuid);
 
         createNode(diskRoot, uuid);
 
         updateDiskProperties(uuid, properties);
     }
 
     public void updateDiskProperties(String uuid, Properties properties) {
 
         String diskRoot = getDiskZkPath(uuid);
 
         Enumeration<?> propertiesEnum = properties.propertyNames();
 
         while (propertiesEnum.hasMoreElements()) {
             String key = (String) propertiesEnum.nextElement();
             String content = properties.get(key).toString();
 
             if (key != UUID_KEY) {
             	String path = diskRoot + "/" + key;
             	deleteIfExists(path);
                 createNode(path, content);
             }
         }
     }
 
     public Properties getDiskProperties(String uuid) {
 
         String root = getDiskZkPath(uuid);
 
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
 
         // Add the number of mounts since this isn't in the same
         // metadata tree.
         int mounts = getNumberOfMounts(uuid);
         properties.put(DISK_USERS_KEY, String.valueOf(mounts));
 
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
 
     public void addDiskMount(String node, String vmId, String uuid,
             String target, Logger logger) {
 
         String diskPath = getDiskPath(uuid);
 
         if (!pathExists(diskPath)) {
             String timestamp = MiscUtils.getTimestamp();
             logger.info("Creating node: " + diskPath + " " + timestamp);
             createNode(diskPath, timestamp);
         }
 
         String diskMountPath = getDiskMountPath(uuid);
 
         if (!pathExists(diskMountPath)) {
             String timestamp = MiscUtils.getTimestamp();
             logger.info("Creating node: " + diskMountPath + " " + timestamp);
             createNode(diskMountPath, timestamp);
         }
 
         String mountPath = getMountPath(node, vmId, uuid);
 
         if (pathExists(mountPath)) {
             String msg = String.format(
                     "disk %s is already mounted on VM %s on node %s", uuid,
                     vmId, node);
             throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, msg);
         }
 
         logger.info("Creating node: " + mountPath + ", " + target);
         createNode(mountPath, target);
 
         logger.info("add disk usage path: " + mountPath + ", "
                 + pathExists(mountPath) + ", " + target);
     }
 
     public void addDiskMountDevice(String vmId, String uuid, String target,
             Logger logger) {
 
         String deviceMountPath = getMountDevicePath(vmId, uuid);
 
         deleteIfExists(deviceMountPath);
 
         logger.info("Creating node: " + deviceMountPath + " " + target);
         createNode(deviceMountPath, target);
     }
 
 	public int incrementUserCount(String uuid) {
 		String path = getPathValue(DISK_USERS_KEY, uuid);
         int count = Integer.parseInt(getNode(path));
         deleteIfExists(path);
         count++;
         createNode(path, String.valueOf(count));
         return count;
 	}
 
 	public void deleteIfExists(String path) {
 		if (pathExists(path)) {
             deleteNode(path);
         }
 	}
 
     public void removeDiskMount(String node, String vmId, String uuid,
             Logger logger) {
 
         String mountPath = getMountPath(node, vmId, uuid);
 
         logger.info("Deleting node: " + mountPath + ", "
                 + pathExists(mountPath));
 
         if (pathExists(mountPath)) {
             deleteNode(mountPath);
         } else {
             String msg = String.format(
                     "disk %s is not mounted on VM %s on node %s", uuid, vmId,
                     node);
             throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, msg);
         }
     }
 
     public static String getDiskPath(String uuid) {
     	return getDiskZkPath(uuid);
     }
 
     private String getDiskMountPath(String uuid) {
         return String.format("%s/mounts", getDiskPath(uuid));
     }
 
     private String getMountPath(String node, String vmId, String uuid) {
         return String.format("%s/%s-%s", getDiskMountPath(uuid), vmId, node);
     }
 
     private String getMountDevicePath(String vmId, String uuid) {
         return String.format("%s/%s", getDiskPath(uuid), vmId);
     }
 
     public String getPathValue(String key, String uuid) {
         return String.format("%s/%s", getDiskZkPath(uuid), key);
     }
 
     public int getNumberOfMounts(String uuid) {
         List<String> mounts = getDiskMountIds(uuid);
         return mounts.size();
     }
 
     private String getTargetsVMPath(String vmId) {
         return String.format("%s/%s", ZK_TARGETS_PATH, vmId);
     }
 
     private String getTargetPath(String vmId, String target) {
         return String.format("%s/%s/%s", ZK_TARGETS_PATH, vmId, target);
     }
 
     public List<String> getDiskMountIds(String uuid) {
 
         List<String> results = Collections.emptyList();
 
         String diskMountPath = getDiskMountPath(uuid);
         if (pathExists(diskMountPath)) {
             results = getChildren(diskMountPath);
         }
 
         return results;
     }
 
     public List<Properties> getDiskMounts(String uuid) {
 
         List<Properties> results = new LinkedList<Properties>();
 
         String diskMountPath = getDiskMountPath(uuid);
 
         for (String mount : getDiskMountIds(uuid)) {
             String childPath = diskMountPath + "/" + mount;
             String value = getNode(childPath);
 
             Properties properties = new Properties();
             properties.put(UUID_KEY, uuid);
             properties.put("mountid", mount);
             properties.put("device", value);
             results.add(properties);
         }
 
         return results;
     }
 
     public String diskTarget(String node, String vmId, String uuid) {
 
         String mountPath = getMountPath(node, vmId, uuid);
 
         if (!pathExists(mountPath)) {
             return STATIC_DISK_TARGET;
         }
 
         return getNode(mountPath);
     }
 
     public void ensureTargetParentExists(String vmId) {
 
         String targetsPath = getTargetsVMPath(vmId);
         if (!pathExists(targetsPath)) {
             String timestamp = MiscUtils.getTimestamp();
             createNode(targetsPath, timestamp);
         }
 
     }
 
     public String nextDiskTarget(String vmId) {
 
         String nextTarget = null;
 
         ensureTargetParentExists(vmId);
 
         for (char driveLetter = 'a'; driveLetter <= 'z'; driveLetter++) {
             String device = DEVICE_PREFIX + driveLetter;
             String devicePath = getTargetPath(vmId, device);
             try {
                 createNodeOrFail(devicePath, MiscUtils.getTimestamp());
             } catch (KeeperException.NodeExistsException e) {
                 continue;
             }
             nextTarget = device;
             break;
         }
 
         if (nextTarget == null) {
             throw new ResourceException(Status.CLIENT_ERROR_CONFLICT,
                     "no free devices available");
         }
 
         return nextTarget;
     }
 
     public void deleteDiskTarget(String vmId, String target) {
 
         ensureTargetParentExists(vmId);
 
         String device = getTargetPath(vmId, target);
         deleteNode(device);
 
     }
 
     public int remainingFreeUser(String path) {
         return RootApplication.CONFIGURATION.USERS_PER_DISK
                 - getNumberOfMounts(path);
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
 
         List<String> children = Collections.emptyList();
 
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
     
     public boolean isCoW(String uuid) {
         return isTrue(DiskProperties.DISK_COW_BASE_KEY, uuid);
     }
     
     public boolean isReadOnly(String uuid) {
         return isTrue(DiskProperties.DISK_READ_ONLY_KEY, uuid);
     	
     }
 
 	protected boolean isTrue(String key, String uuid) {
 		String path = getPathValue(key, uuid);
 		
 		if (pathExists(path)) {
 			String value = getNode(path);
 			if("false".equals(value)) {
 				return false;
 			} else {
 				return true;
 			}
 		} else {
 			return false;
 		}
 	}
 }
