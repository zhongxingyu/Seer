 package com.jbrisbin.vpc.jobsched.zk;
 
 import groovy.lang.*;
 import org.apache.zookeeper.*;
 import org.apache.zookeeper.data.ACL;
 import org.apache.zookeeper.data.Stat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Jon Brisbin <jon@jbrisbin.com>
  */
 public class GroovyZooKeeperHelper extends GroovyObjectSupport {
 
   protected static final Logger log = LoggerFactory.getLogger(GroovyZooKeeperHelper.class);
   private static int DEFAULT_TIMEOUT = 20000;
 
   private ZooKeeper zookeeper;
   private Watcher clientWatcher;
   private Closure onNodeChildrenChanged;
   private Closure onNodeCreated;
   private Closure onNodeDeleted;
   private Closure onDataChanged;
   private Closure onEvent;
 
   public GroovyZooKeeperHelper() {
     setMetaClass(new ExpandoMetaClass(getClass()));
     getMetaClass().initialize();
     clientWatcher = new ClientWatcher();
   }
 
   public GroovyZooKeeperHelper(ZooKeeper zookeeper) {
     this();
     this.zookeeper = zookeeper;
   }
 
   public GroovyZooKeeperHelper(String url) throws IOException {
     this();
    setZookeeper(new ZooKeeper(url, DEFAULT_TIMEOUT, clientWatcher));
   }
 
   public GroovyZooKeeperHelper(Map<String, Closure> callbacks, String url) throws IOException {
     this();
     for (String key : callbacks.keySet()) {
       try {
         getClass().getDeclaredField(key).set(this, callbacks.get(key));
       } catch (IllegalAccessException e) {
         log.error(e.getMessage(), e);
       } catch (NoSuchFieldException e) {
         log.error(e.getMessage(), e);
       }
     }
     setZookeeper(new ZooKeeper(url, DEFAULT_TIMEOUT, clientWatcher));
   }
 
   public ZooKeeper getZookeeper() {
     return zookeeper;
   }
 
   public void setZookeeper(ZooKeeper zookeeper) {
     if (null != this.zookeeper) {
       try {
         this.zookeeper.close();
       } catch (InterruptedException e) {
         log.error(e.getMessage(), e);
       }
     }
     this.zookeeper = zookeeper;
   }
 
   public Watcher getClientWatcher() {
     return clientWatcher;
   }
 
   public void setClientWatcher(Watcher clientWatcher) {
     this.clientWatcher = clientWatcher;
     if (null != this.zookeeper) {
       this.zookeeper.register(this.clientWatcher);
     }
   }
 
   public Closure getOnNodeChildrenChanged() {
     return onNodeChildrenChanged;
   }
 
   public void setOnNodeChildrenChanged(Closure onNodeChildrenChanged) {
     this.onNodeChildrenChanged = onNodeChildrenChanged;
   }
 
   public Closure getOnNodeCreated() {
     return onNodeCreated;
   }
 
   public void setOnNodeCreated(Closure onNodeCreated) {
     this.onNodeCreated = onNodeCreated;
   }
 
   public Closure getOnNodeDeleted() {
     return onNodeDeleted;
   }
 
   public void setOnNodeDeleted(Closure onNodeDeleted) {
     this.onNodeDeleted = onNodeDeleted;
   }
 
   public Closure getOnDataChanged() {
     return onDataChanged;
   }
 
   public void setOnDataChanged(Closure onDataChanged) {
     this.onDataChanged = onDataChanged;
   }
 
   public Closure getOnEvent() {
     return onEvent;
   }
 
   public void setOnEvent(Closure onEvent) {
     this.onEvent = onEvent;
   }
 
   public void delete(Object path, int version) throws InterruptedException, KeeperException {
     zookeeper.delete(getPathAsString(path), version);
   }
 
   public void delete(Object path, int version, final Closure callback) {
     zookeeper.delete(getPathAsString(path), version, new AsyncCallback.VoidCallback() {
       public void processResult(int rc, String path, Object ctx) {
         callback.setProperty("returnCode", rc);
         callback.setProperty("path", path);
         callback.setDelegate(ctx);
         callback.call();
       }
     }, this);
   }
 
   public Node createSequenceNode(Object path) throws InterruptedException, KeeperException {
     return create(path, CreateMode.EPHEMERAL_SEQUENTIAL);
   }
 
   public Node createPersistentNode(Object path) throws InterruptedException, KeeperException {
     return create(path, CreateMode.PERSISTENT);
   }
 
   public String createPersistentNodeAndParents(
       Object path) throws InterruptedException, KeeperException {
     String spath = getPathAsString(path);
     String[] parts = spath.substring(1).split("/");
     StringBuffer buff = new StringBuffer();
     String fullPath = null;
     for (String p : parts) {
       buff.append("/").append(p);
       try {
         fullPath = zookeeper.create(buff.toString(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
             CreateMode.PERSISTENT);
       } catch (KeeperException.NodeExistsException ignored) {
         fullPath = buff.toString();
       }
     }
     return fullPath;
   }
 
   public Node create(Object path) throws InterruptedException, KeeperException {
     return create(path, new byte[0]);
   }
 
   public Node create(Object path,
                      CreateMode mode) throws InterruptedException, KeeperException {
     return create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
   }
 
   public Node create(Object path, Object data) throws InterruptedException, KeeperException {
     return create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
   }
 
   public void create(Object path, Object data,
                      Closure callback) throws InterruptedException, KeeperException {
     create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, callback);
   }
 
   public void create(Object path, Object data, List<ACL> acls, CreateMode mode,
                      final Closure callback) {
     zookeeper.create(getPathAsString(path), serialize(data), acls, mode,
         new AsyncCallback.StringCallback() {
           public void processResult(int rc, String path, Object ctx, String name) {
             callback.setProperty("returnCode", rc);
             callback.setProperty("path", path);
             callback.setDelegate(ctx);
             callback.call(name);
           }
         }, this);
   }
 
   public Node create(Object path, Object data, List<ACL> acls, CreateMode mode) throws
       InterruptedException,
       KeeperException {
     String s = zookeeper.create(getPathAsString(path), serialize(data), acls, mode);
     return new Node(s, mode);
   }
 
   public Stat exists(Object path) throws InterruptedException, KeeperException {
     return exists(path, false);
   }
 
   public void exists(Object path, boolean watch,
                      final Closure callback) throws InterruptedException, KeeperException {
     zookeeper.exists(getPathAsString(path), watch, new AsyncCallback.StatCallback() {
       public void processResult(int rc, String path, Object ctx, Stat stat) {
         callback.setProperty("returnCode", rc);
         callback.setProperty("path", path);
         callback.setDelegate(ctx);
         callback.call(stat);
       }
     }, this);
   }
 
   public Stat exists(Object path, boolean watch) throws InterruptedException, KeeperException {
     return zookeeper.exists(getPathAsString(path), watch);
   }
 
   public List<String> getChildren(Object path,
                                   boolean watch) throws InterruptedException, KeeperException {
     return zookeeper.getChildren(getPathAsString(path), watch);
   }
 
   public Object getData(Object path, Stat stat) throws InterruptedException, KeeperException {
     return deserialize(zookeeper.getData(getPathAsString(path), false, stat));
   }
 
   public Object getData(Object path, final Closure watcher,
                         Stat stat) throws InterruptedException, KeeperException {
     return deserialize(zookeeper.getData(getPathAsString(path), new Watcher() {
       public void process(WatchedEvent event) {
         watcher.call(event);
       }
     }, stat));
   }
 
   public Stat setData(Object path, Object data,
                       int version) throws InterruptedException, KeeperException {
     return zookeeper.setData(getPathAsString(path), serialize(data), version);
   }
 
   public void setData(Object path, Object data, int version, final Closure callback) {
     zookeeper.setData(getPathAsString(path), serialize(data), version,
         new AsyncCallback.StatCallback() {
           public void processResult(int rc, String path, Object ctx, Stat stat) {
             callback.setDelegate(ctx);
             callback.setProperty("returnCode", rc);
             callback.setProperty("path", path);
             callback.call(stat);
           }
         }, this);
   }
 
   private String getPathAsString(Object path) {
     String spath = (path instanceof String || path instanceof GString ? path.toString() : null);
     if (null == spath) {
       throw new IllegalArgumentException("First parameter must be a String " + path);
     }
     return spath;
   }
 
   private byte[] serialize(Object obj) {
     byte[] bytes = new byte[0];
     if (null != obj) {
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       ObjectOutputStream oout = null;
       try {
         oout = new ObjectOutputStream(out);
         oout.writeObject(obj);
       } catch (IOException e) {
         log.error(e.getMessage(), e);
       }
       bytes = out.toByteArray();
     }
     return bytes;
   }
 
   private Object deserialize(byte[] bytes) {
     ObjectInputStream oin = null;
     try {
       oin = new ObjectInputStream(new ByteArrayInputStream(bytes));
       return oin.readObject();
     } catch (IOException e) {
       log.error(e.getMessage(), e);
     } catch (ClassNotFoundException e) {
       log.error(e.getMessage(), e);
     }
     return null;
   }
 
   private class ClientWatcher implements Watcher {
     public void process(WatchedEvent watchedEvent) {
       try {
         Closure callback = null;
         switch (watchedEvent.getType()) {
           case NodeChildrenChanged:
             if (null != onNodeChildrenChanged) {
               callback = onNodeChildrenChanged;
             } else {
               callback = onEvent;
             }
             break;
           case NodeCreated:
             if (null != onNodeCreated) {
               callback = onNodeCreated;
             } else {
               callback = onEvent;
             }
             break;
           case NodeDeleted:
             if (null != onNodeDeleted) {
               callback = onNodeDeleted;
             } else {
               callback = onEvent;
             }
             break;
           case NodeDataChanged:
             if (null != onDataChanged) {
               callback = onDataChanged;
             } else {
               callback = onEvent;
             }
             break;
           case None:
             if (null != onEvent) {
               callback = onEvent;
             }
         }
         if (null != callback) {
           callback.call(watchedEvent);
         } else {
           log.warn("No callbacks defined to accept event: " + watchedEvent);
         }
       } catch (Throwable t) {
         log.debug(t.getMessage());
       }
     }
   }
 
   public class Node extends GroovyObjectSupport {
 
     private String path;
     private CreateMode type;
     private Stat stat;
     private boolean setWatch = true;
 
     public Node(String path, CreateMode type) throws InterruptedException, KeeperException {
       this.path = path;
       this.type = type;
       refresh();
     }
 
     public String getPath() {
       return path;
     }
 
     public void setPath(String path) {
       this.path = path;
     }
 
     public CreateMode getType() {
       return type;
     }
 
     public void setType(CreateMode type) {
       this.type = type;
     }
 
     public Stat getStat() {
       return stat;
     }
 
     public void setStat(Stat stat) {
       this.stat = stat;
     }
 
     public boolean isSetWatch() {
       return setWatch;
     }
 
     public void setSetWatch(boolean setWatch) {
       this.setWatch = setWatch;
     }
 
     public void setData(Object data) throws InterruptedException, KeeperException {
       zookeeper.setData(path, serialize(data), stat.getVersion());
     }
 
     public Object getData() throws InterruptedException, KeeperException {
       return deserialize(zookeeper.getData(path, setWatch, stat));
     }
 
     public void refresh() throws InterruptedException, KeeperException {
       stat = zookeeper.exists(path, setWatch);
     }
   }
 }
