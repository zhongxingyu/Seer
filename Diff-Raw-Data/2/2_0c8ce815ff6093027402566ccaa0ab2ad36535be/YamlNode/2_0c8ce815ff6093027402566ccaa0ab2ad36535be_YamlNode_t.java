 package com.craftfire.commons.yaml;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Blob;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import com.craftfire.commons.util.AbstractValueHolder;
 import com.craftfire.commons.util.Util;
 import com.craftfire.commons.util.ValueHolder;
 import com.craftfire.commons.util.ValueHolderBase;
 import com.craftfire.commons.util.ValueType;
 
 /**
  * A class for yaml nodes.
  */
 public class YamlNode extends AbstractValueHolder {
     private List<YamlNode> listCache = null;
     private Map<String, YamlNode> mapCache = null;
     private boolean resolved = false;
     private ValueHolder holder;
     private final SimpleYamlManager manager;
     private YamlNode parent = null;
 
     /**
      * Creates a new YamlNode with given SimpleYamlManager, node name, and value.
      * 
      * @param manager  the YamlManager
      * @param name     the name
      * @param value    the value
      */
     public YamlNode(SimpleYamlManager manager, String name, Object value) {
         this.manager = manager;
         this.holder = new ValueHolderBase(normalizePath(name), false, value);
     }
 
     /**
      * Creates a new YamlNode with given parent node, node name, and value.
      * 
      * @param parent  the parent node
      * @param name    the name
      * @param value   the value
      */
     public YamlNode(YamlNode parent, String name, Object value) {
         this(parent.getYamlManager(), name, value);
         this.parent = parent;
     }
 
     /**
      * Checks if the node has a parent node.
      * 
      * @return {@code true} if has, {@code false} otherwise
      */
     public boolean hasParent() {
         return this.parent != null;
     }
 
     /**
      * Retuns the parent node of this node.
      * 
      * @return the parent node
      */
     public YamlNode getParent() {
         return this.parent;
     }
 
     /**
      * Sets the parent node of this node.
      * 
      * @param parent  the node to set as parent
      */
     protected void setParent(YamlNode parent) {
         this.parent = parent;
     }
 
     protected String normalizePath(String path) {
        if (this.manager.isCaseSensitive() || path == null) {
             return path;
         }
         return path.toLowerCase();
     }
 
     /**
      * Returns a list of names of all nodes in the path to this node.
      * 
      * @return  a list of path elements
      */
     public List<String> getPathElements() {
         List<String> elements;
         if (hasParent()) {
             elements = getParent().getPathElements();
         } else {
             elements = new ArrayList<String>();
         }
         if (getName() != null) {
             elements.add(getName());
         }
         return elements;
     }
 
     /**
      * Returns the path of the node, separated by the node's YamlManager's path separator.
      * 
      * @return the path
      */
     public String getPath() {
         return Util.join(getPathElements(), this.manager.getSeparator());
     }
 
     /**
      * Returns the SimpleYamlManager of the node.
      * 
      * @return the SimpleYamlManager
      */
     public SimpleYamlManager getYamlManager() {
         return this.manager;
     }
 
     /**
      * Checks if this node is a map.
      * 
      * @return {@code true} if it's a map, {@code false} otherwise
      */
     public boolean isMap() {
         if (this.resolved) {
             return this.mapCache != null;
         }
         return getValue() instanceof Map<?, ?>;
     }
 
     /**
      * Checks if this node is a list.
      * 
      * @return {@code true} if it's a list, {@code false} otherwise
      */
     public boolean isList() {
         if (this.resolved) {
             return this.listCache != null;
         }
         return getValue() instanceof Collection<?>;
     }
 
     /**
      * Checks if this node is a scalar (a node without child nodes).
      * 
      * @return {@code true} if it's a scalar, {@code false} otherwise
      */
     public boolean isScalar() {
         return !isMap() && !isList();
     }
 
     /**
      * Checks if this node's children has been loaded.
      * <p>
      * Only map or list nodes can be resolved.
      * 
      * @return {@code true} if resolved, {@code false} otherwise
      */
     public boolean isResloved() {
         return this.resolved;
     }
 
     /**
      * Returns a child node of this node with specified name.
      * <p>
      * Only map nodes can look up children by name.
      * 
      * @param name           name of the child node
      * @return               the child, or {@code null} if doesn't exist
      * @throws YamlException if the node is not a map
      */
     public YamlNode getChild(String name) throws YamlException {
         return getChild(name, false);
     }
 
     /**
      * Returns a child node of this node with specified name.
      * <p>
      * Only map nodes can look up children by name.
      * <p>
      * If the child doesn't exist, and {@code add} is {@code true}, then creates one. If the node is null, and attempting to create the child, the node will be turned into a map.
      * 
      * @param name           name of the child node
      * @param add            weather or not create the child if doesn't exist
      * @return               the child, or {@code null} if doesn't exist and not being created
      * @throws YamlException if the node is not a map and not null
      */
     public YamlNode getChild(String name, boolean add) throws YamlException {
         if (add && (isMap() || isNull()) && !hasChild(name)) {
             return addChild(name, null);
         }
         return getChildrenMap().get(normalizePath(name));
     }
 
     /**
      * Checks if the node has a child node with specified name.
      * <p>
      * Only map nodes can look up children by name. For other types of nodes will always return {@code false}.
      * 
      * @param name  name of the child node
      * @return      {@code true} if has, {@code false} otherwise
      */
     public boolean hasChild(String name) {
         if (!isMap()) {
             return false;
         }
         try {
             return getChildrenMap().containsKey(normalizePath(name));
         } catch (YamlException e) {
             this.manager.getLogger().stackTrace(e);
             return false;
         }
     }
 
     /**
      * Returns map of child nodes of this map node.
      * 
      * @return               a map of child nodes
      * @throws YamlException if the node is not a map
      */
     public Map<String, YamlNode> getChildrenMap() throws YamlException {
         if (!isMap()) {
             throw new YamlException("Node is not a map!", getPath());
         }
         if (!this.resolved) {
             this.mapCache = new HashMap<String, YamlNode>();
             for (Map.Entry<?, ?> entry : ((Map<?, ?>) getValue()).entrySet()) {
                 String name = normalizePath(entry.getKey().toString());
                 this.mapCache.put(name, new YamlNode(this, name, entry.getValue()));
             }
             this.holder = new ValueHolderBase(this.holder.getName(), false, null);
             this.resolved = true;
         }
         return new HashMap<String, YamlNode>(this.mapCache);
     }
 
     /**
      * Returns map of child nodes of this list node or map node.
      * 
      * @return               a list of child nodes
      * @throws YamlException if the node is a scalar
      */
     public List<YamlNode> getChildrenList() throws YamlException {
         if (isMap()) {
             if (this.resolved) {
                 return new ArrayList<YamlNode>(this.mapCache.values());
             }
             return new ArrayList<YamlNode>(getChildrenMap().values());
         }
         if (!isList()) {
             throw new YamlException("Node is not a list!", getPath());
         }
         if (!this.resolved) {
             this.listCache = new ArrayList<YamlNode>();
             for (Object o : (Collection<?>) getValue()) {
                 this.listCache.add(new YamlNode(this, "", o));
             }
             this.holder = new ValueHolderBase(this.holder.getName(), false, null);
             this.resolved = true;
         }
         return new ArrayList<YamlNode>(this.listCache);
     }
 
     /**
      * Adds a nameless child node to this node.
      * <p>
      * Adding nameless children works only for list nodes.
      * <p>
      * If this node is null, it will be turned into a list node.
      * 
      * @param value           value of the new node
      * @return                the added node
      * @throws YamlException  if this node is not a list
      */
     public YamlNode addChild(Object value) throws YamlException {
         return addChild("", value);
     }
 
     /**
      * Adds a child node to this node.
      * <p>
      * Adding child nodes doesn't work for scalar nodes.
      * <p>
      * If the node is a map, and a child with specified name already exists, it will be overriden.
      * <p>
      * If this node is null, it will be turned into a map node, or list node if the name is empty.
      * 
      * @param name            name of the new node
      * @param value           value of the new node
      * @return                the added node
      * @throws YamlException  if this node is a non-null scalar or the {@code name} is empty and the node is not a list
      */
     public YamlNode addChild(String name, Object value) throws YamlException {
         if (isScalar()) {
             if (isNull()) {
                 if (name == null || name.isEmpty()) {
                     this.listCache = new ArrayList<YamlNode>();
                 } else {
                     this.mapCache = new HashMap<String, YamlNode>();
                 }
                 this.resolved = true;
             } else {
                 throw new YamlException("Can't add child to scalar node", getPath());
             }
         }
         if (value instanceof ValueHolder) {
             value = ((ValueHolder) value).getValue();
         }
         YamlNode node;
         name = normalizePath(name);
         if (!this.resolved) {
             getChildrenList(); // This can resolve both Map and List
         }
         if (isList()) {
             node = new YamlNode(this, "", value);
             this.listCache.add(node);
             return node;
         }
         if (name == null || name.isEmpty()) {
             throw new YamlException("Can't add nameless child to a map node", getPath());
         }
         node = new YamlNode(this, name, value);
         this.mapCache.put(name, node);
         return node;
     }
 
     /**
      * Adds specified node as a child node of this node.
      * <p>
      * Adding child nodes doesn't work for scalar nodes.
      * <p>
      * If the node is a map, and a child with the same name already exists, it will be overriden.
      * Note that a copy of specified node will be added, not the node itself.
      * <p>
      * If this node is null, it will be turned into a map node, or list node if the name is empty.
      * 
      * @param node           the node to be added
      * @return               a copy of specified node, being a child of this node
      * @throws YamlException if this node is a non-null scalar or the name of the specified node is empty and this node is not a list
      */
     public YamlNode addChild(YamlNode node) throws YamlException {
         return addChild(node.getName(), node.dump());
     }
 
     /**
      * Adds specified nodes as child nodes of this node.
      * <p>
      * Adding child nodes doesn't work for scalar nodes.
      * <p>
      * If the node is a map, and a child with the same name already exists, it will be overriden.
      * Note that a copy of specified nodes will be added, not the nodes themselves.
      * <p>
      * If this node is null, it will be turned into a map node.
      * 
      * @param nodes          nodes to be added
      * @throws YamlException if this node is a non-null scalar, or this node is not a list and one of {@code nodes} has empty name
      */
     public void addChildren(YamlNode... nodes) throws YamlException {
         if (isScalar()) {
             if (isNull()) {
                 this.mapCache = new HashMap<String, YamlNode>();
                 this.resolved = true;
             } else {
                 throw new YamlException("Can't add child to scalar node", getPath());
             }
         }
         if (!this.resolved) {
             getChildrenList(); // This can resolve both Map and List
         }
         if (isList()) {
             for (YamlNode node : nodes) {
                 this.listCache.add(new YamlNode(this, "", node.dump()));
             }
             return;
         }
         for (YamlNode node : nodes) {
             String name = normalizePath(node.getName());
             if (name == null || name.isEmpty()) {
                 throw new YamlException("Can't add nameless child to a map node", getPath());
             }
             this.mapCache.put(name, new YamlNode(this, name, node.dump()));
         }
     }
 
     /**
      * Adds all entries of specified map as new child nodes of this node.
      * <p>
      * Adding child nodes doesn't work for scalar nodes.
      * <p>
      * If the node is a map, and a child with the same name already exists, it will be overriden.
      * <p>
      * If this node is null, it will be turned into a map node.
      * 
      * @param map            the map
      * @throws YamlException if this node is a non-null scalar, or this node is not a list and the {@code map} has empty text keys
      */
     public void addChildren(Map<?, ?> map) throws YamlException {
         if (isScalar()) {
             if (isNull()) {
                 this.mapCache = new HashMap<String, YamlNode>();
                 this.resolved = true;
             } else {
                 throw new YamlException("Can't add child to scalar node", getPath());
             }
         }
         if (!this.resolved) {
             getChildrenList(); // This can resolve both Map and List
         }
         if (isList()) {
             for (Object value : map.values()) {
                 this.listCache.add(new YamlNode(this, "", value));
             }
             return;
         }
         for (Map.Entry<?, ?> entry : map.entrySet()) {
             String name = entry.getKey().toString();
             if (name == null || name.isEmpty()) {
                 throw new YamlException("Can't add nameless child to a map node", getPath());
             }
             this.mapCache.put(name, new YamlNode(this, name, entry.getValue()));
         }
     }
 
     /**
      * Adds all elements of specified collection as new child nodes of this list node.
      * <p>
      * If this node is null, it will be turned into a list node.
      * 
      * @param collection     the collection
      * @throws YamlException if this node is not a list node and not null
      */
     public void addChildren(Collection<?> collection) throws YamlException {
         if (!isList()) {
             if (isNull()) {
                 this.listCache = new ArrayList<YamlNode>();
                 this.resolved = true;
             } else {
                 throw new YamlException("Can't add nameless child to non-list node", getPath());
             }
         }
         if (!this.resolved) {
             getChildrenList();
         }
         for (Object value : collection) {
             this.listCache.add(new YamlNode(this, "", value));
         }
     }
 
     /**
      * Returns number of children of the node.
      * 
      * @return number of children
      */
     public int getChildrenCount() {
         if (isScalar()) {
             return 0;
         }
         try {
             getChildrenList(); // This can resolve both Map and List
         } catch (YamlException e) {
             this.manager.getLogger().stackTrace(e);
             return 0;
         }
         if (isList()) {
             return this.listCache.size();
         }
         return this.mapCache.size();
     }
 
     /**
      * Returns number of final (scalar) nodes among descendants of this node
      * 
      * @return number of final nodes
      */
     public int getFinalNodeCount() {
         if (isScalar()) {
             return 1;
         }
         int count = 0;
         try {
             for (YamlNode node : getChildrenList()) {
                 count += node.getFinalNodeCount();
             }
         } catch (YamlException e) {
             this.manager.getLogger().stackTrace(e);
         }
         return count;
     }
 
     /**
      * Removes a child node with specified name from this node's children.
      * 
      * @param name  name of the child to remove
      * @return      the removed child, or {@code null} if didn't remove anything
      */
     public YamlNode removeChild(String name) {
         name = normalizePath(name);
         if (hasChild(name)) {
             try {
                 return removeChild(getChild(name));
             } catch (YamlException e) {
                 this.manager.getLogger().stackTrace(e);
             }
         }
         return null;
     }
 
     /**
      * Removes specified child node from this node's children.
      * 
      * @param node  the node to remove
      * @return      {@code node} if removed, or {@code null} if not this node's child
      */
     public YamlNode removeChild(YamlNode node) {
         if (isScalar() || node.getParent() != this) {
             return null;
         }
         if (!this.resolved) {
             try {
                 getChildrenList(); // This can resolve both Map and List
             } catch (YamlException e) {
                 this.manager.getLogger().stackTrace(e);
                 return null;
             }
         }
         if (isList()) {
             this.listCache.remove(node);
         } else {
             this.mapCache.remove(node.getName()); // It's our node, no need to normalize name.
         }
         node.setParent(null);
         return node;
     }
 
     /**
      * Removes all children of the node.
      */
     public void removeAllChildren() {
         if (isScalar()) {
             return;
         }
         try {
             for (YamlNode node : getChildrenList()) {
                 node.setParent(null);
             }
         } catch (YamlException e) {
             this.manager.getLogger().stackTrace(e);
         }
         if (isMap()) {
             this.mapCache = new HashMap<String, YamlNode>();
         } else {
             this.listCache = new ArrayList<YamlNode>();
         }
     }
 
     /**
      * Sets value of the node to specified {@code value}, and removes children, if any.
      * 
      * @param value  the value
      */
     public void setValue(Object value) {
         this.holder = new ValueHolderBase(this.holder.getName(), false, value);
         removeAllChildren();
         this.listCache = null;
         this.mapCache = null;
         this.resolved = false;
     }
 
     /**
      * Dumps the node with all children to a standard JRE class (lie Map, List, or Integer), which can be then dumped to file (eg. with SnakeYaml).
      * 
      * @return dumped node
      */
     public Object dump() {
         if (!this.resolved) {
             return getValue();
         }
         if (isList()) {
             List<Object> list = new ArrayList<Object>();
             for (YamlNode node : this.listCache) {
                 list.add(node.dump());
             }
             return list;
         }
         Map<String, Object> map = new HashMap<String, Object>();
         for (Map.Entry<String, YamlNode> entry : this.mapCache.entrySet()) {
             map.put(entry.getKey(), entry.getValue().dump());
         }
         return map;
     }
 
     /**
      * Returns a descendant node of this node with given path (relative to this node).
      * 
      * @param path           list of path elements of the descendant node
      * @return               the node with specified path, or {@code null} if not found
      * @throws YamlException if one of the nodes in the path is a scalar or list node
      */
     public YamlNode getNode(String... path) throws YamlException {
         return getNode(false, path);
     }
 
     /**
      * Returns a descendant node of this node with given path (relative to this node).
      * <p>
      * If any the nodes in path don't exist, and {@code add} is {@code true}, then creates them. If the node is null, and attempting to create the child, the node will be turned into a map.
      * 
      * @param add            weather or not create the node if doesn't exist
      * @param path           list of path elements of the descendant node
      * @return               the descendant node, or {@code null} if doesn't exist and not being created
      * @throws YamlException if any node in the path is not a map and not null
      */
     public YamlNode getNode(boolean add, String... path) throws YamlException {
         return getNode(Util.join(path, this.manager.getSeparator()), add);
     }
 
     /**
      * Returns a descendant node of this node with given path (relative to this node).
      * 
      * @param path           path of the descendant node, separated with this node's YamlManager's path separator
      * @return               the node with specified path, or {@code null} if not found
      * @throws YamlException if one of the nodes in the path is a scalar or list node
      */
     public YamlNode getNode(String path) throws YamlException {
         return getNode(path, false);
     }
 
     /**
      * Returns a descendant node of this node with given path (relative to this node).
      * <p>
      * If any the nodes in path don't exist, and {@code add} is {@code true}, then creates them. If the node is null, and attempting to create the child, the node will be turned into a map.
      * 
      * @param path           path of the descendant node, separated with this node's YamlManager's path separator
      * @param add            weather or not create the node if doesn't exist
      * @return               the descendant node, or {@code null} if doesn't exist and not being created
      * @throws YamlException if any node in the path is not a map and not null
      */
     public YamlNode getNode(String path, boolean add) throws YamlException {
         String[] elements = normalizePath(path).split(Pattern.quote(this.manager.getSeparator()), 2);
         if (elements.length == 0) {
             return this;
         }
         YamlNode node = getChild(elements[0], add);
         if (node == null || elements.length == 1) {
             return node;
         }
         return node.getNode(elements[1], add);
     }
 
     /**
      * Checks if the node has a descendant node with given path (relative to this node).
      * 
      * @param path  path elements of the descendant node
      * @return      {@code true} if has, {@code false} otherwise
      */
     public boolean hasNode(String... path) {
         return hasNode(Util.join(path, this.manager.getSeparator()));
     }
 
     /**
      * Checks if the node has a descendant node with given path (relative to this node).
      * 
      * @param path  path of the descendant node, separated with this node's YamlManager's path separator
      * @return      {@code true} if has, {@code false} otherwise
      */
     public boolean hasNode(String path) {
         String[] elements = normalizePath(path).split(Pattern.quote(this.manager.getSeparator()), 2);
         if (elements.length == 0 || !hasChild(elements[0])) {
             return false;
         }
         if (elements.length == 1) {
             return true;
         }
         try {
             return getChild(elements[0]).hasNode(elements[1]);
         } catch (YamlException e) {
             this.manager.getLogger().stackTrace(e);
             return false;
         }
     }
 
     /**
      * Dumps value of thie node as a list.
      * 
      * @return dumped list, or null if the node is not a list
      */
     @SuppressWarnings("unchecked")
     public List<Object> getList() {
         if (isList()) {
             return (List<Object>) dump();
         }
         return null;
     }
 
     /**
      * Dumps value of thie node as a map.
      * 
      * @return dumped map, or null if the node is not a map
      */
     @SuppressWarnings("unchecked")
     public Map<String, Object> getMap() {
         if (isMap()) {
             return (Map<String, Object>) dump();
         }
         return null;
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getName()
      */
     @Override
     public String getName() {
         return this.holder.getName();
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getType()
      */
     @Override
     public ValueType getType() {
         if (this.resolved) {
             return ValueType.UNKNOWN;
         }
         return this.holder.getType();
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getValue()
      */
     @Override
     public Object getValue() {
         return this.holder.getValue();
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.AbstractValueHolder#getString()
      */
     @Override
     public String getString() {
         return this.holder.getString();
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getString(java.lang.String)
      */
     @Override
     public String getString(String defaultValue) {
         return this.holder.getString(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getInt(int)
      */
     @Override
     public int getInt(int defaultValue) {
         return this.holder.getInt(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getLong(long)
      */
     @Override
     public long getLong(long defaultValue) {
         return this.holder.getLong(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getBigInt(java.math.BigInteger)
      */
     @Override
     public BigInteger getBigInt(BigInteger defaultValue) {
         return this.holder.getBigInt(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getDouble(double)
      */
     @Override
     public double getDouble(double defaultValue) {
         return this.holder.getDouble(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getFloat(float)
      */
     @Override
     public float getFloat(float defaultValue) {
         return this.holder.getFloat(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getDecimal(java.math.BigDecimal)
      */
     @Override
     public BigDecimal getDecimal(BigDecimal defaultValue) {
         return this.holder.getDecimal(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getBytes(byte[])
      */
     @Override
     public byte[] getBytes(byte[] defaultValue) {
         return this.holder.getBytes(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getDate(java.util.Date)
      */
     @Override
     public Date getDate(Date defaultValue) {
         return this.holder.getDate(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getBlob(java.sql.Blob)
      */
     @Override
     public Blob getBlob(Blob defaultValue) {
         return this.holder.getBlob(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#getBool(boolean)
      */
     @Override
     public boolean getBool(boolean defaultValue) {
         return this.holder.getBool(defaultValue);
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#isNull()
      */
     @Override
     public boolean isNull() {
         return !this.resolved && this.holder.isNull();
     }
 
     /* (non-Javadoc)
      * @see com.craftfire.commons.util.ValueHolder#isUnsigned()
      */
     @Override
     public boolean isUnsigned() {
         return this.holder.isUnsigned();
     }
 
 }
