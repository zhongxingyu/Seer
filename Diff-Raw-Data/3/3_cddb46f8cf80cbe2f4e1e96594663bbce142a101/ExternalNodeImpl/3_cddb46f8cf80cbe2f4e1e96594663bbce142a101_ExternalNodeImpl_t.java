 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.external;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.*;
 
 import javax.jcr.*;
 import javax.jcr.lock.Lock;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.*;
 import javax.jcr.version.Version;
 import javax.jcr.version.VersionException;
 import javax.jcr.version.VersionHistory;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.jackrabbit.util.ChildrenCollectorFilter;
 import org.apache.jackrabbit.value.BinaryImpl;
 import org.jahia.api.Constants;
 import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
 import org.jahia.services.content.nodetypes.ExtendedNodeType;
 import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
 import org.jahia.services.content.nodetypes.Name;
 import org.jahia.services.content.nodetypes.NodeTypeRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Implementation of the {@link javax.jcr.Node} for the {@link org.jahia.modules.external.ExternalData}.
  * 
  * @author Thomas Draier
  */
 public class ExternalNodeImpl extends ExternalItemImpl implements Node {
 
     private static final Logger logger = LoggerFactory.getLogger(ExternalNodeImpl.class);
     
     private ExternalData data;
     private Map<String, ExternalPropertyImpl> properties = null;
     
     private String uuid;
     
     public ExternalNodeImpl(ExternalData data, ExternalSessionImpl session) throws RepositoryException {
         super(session);
         this.data = data;
         this.properties = new HashMap<String, ExternalPropertyImpl>();
 
         for (Map.Entry<String, String[]> entry : data.getProperties().entrySet()) {
             ExtendedPropertyDefinition definition = getPropertyDefinition(entry.getKey());
             if (definition != null) {
                 int requiredType = definition.getRequiredType();
                 if (requiredType == PropertyType.UNDEFINED) {
                     requiredType = PropertyType.STRING;
                 }
                 if (definition.isMultiple()) {
                     Value[] values = new Value[entry.getValue().length];
                     for (int i = 0; i < entry.getValue().length; i++) {
                         values[i] = session.getValueFactory().createValue(entry.getValue()[i], requiredType);
                     }
                     properties.put(entry.getKey(),new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session, values));
                 } else {
                     properties.put(entry.getKey(),
                             new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session,
                                     session.getValueFactory().createValue(entry.getValue().length > 0 ? entry.getValue()[0] : null, requiredType)));
                 }
             }
         }
         if (data.getBinaryProperties() != null) {
             for (Map.Entry<String, Binary[]> entry : data.getBinaryProperties().entrySet()) {
                 ExtendedPropertyDefinition definition =  getPropertyDefinition(entry.getKey());
                 if (definition != null && definition.getRequiredType() == PropertyType.BINARY) {
                     if (definition.isMultiple()) {
                         Value[] values = new Value[entry.getValue().length];
                         for (int i = 0; i < entry.getValue().length; i++) {
                             values[i] = session.getValueFactory().createValue(entry.getValue()[i]);
                         }
                         properties.put(entry.getKey(),new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session, values));
                     } else {
                         properties.put(entry.getKey(),
                                 new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session,
                                         session.getValueFactory().createValue(entry.getValue()[0])));
                     }
                 }
             }
         }
         properties.put("jcr:uuid",
                 new ExternalPropertyImpl(new Name("jcr:uuid", NodeTypeRegistry.getInstance().getNamespaces()), this, session,
                         session.getValueFactory().createValue(getIdentifier())));
     }
 
     private ExtendedPropertyDefinition getPropertyDefinition(String name) throws RepositoryException {
         Map<String, ExtendedPropertyDefinition> propertyDefinitionsAsMap = getExtendedPrimaryNodeType().getPropertyDefinitionsAsMap();
         if (propertyDefinitionsAsMap.containsKey(name)) {
             return propertyDefinitionsAsMap.get(name);
         }
         for (NodeType nodeType : getMixinNodeTypes()) {
             propertyDefinitionsAsMap = ((ExtendedNodeType)nodeType).getPropertyDefinitionsAsMap();
             if (propertyDefinitionsAsMap.containsKey(name)) {
                 return propertyDefinitionsAsMap.get(name);
             }
         }
         if (!getExtendedPrimaryNodeType().getUnstructuredPropertyDefinitions().isEmpty()) {
             return getExtendedPrimaryNodeType().getUnstructuredPropertyDefinitions().values().iterator().next();
         }
         return null;
     }
 
 
     public String getPath() throws RepositoryException {
         return data.getPath();
     }
 
     public String getName() throws RepositoryException {
         return StringUtils.substringAfterLast(data.getPath(), "/");
     }
 
     public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
         if (data.getPath().equals("/")) {
             throw new ItemNotFoundException();
         }
         String path = StringUtils.substringBeforeLast(data.getPath(), "/");
         return session.getNode(path.isEmpty() ? "/" : path);
     }
 
     public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public int getDepth() throws RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public boolean isNode() {
         return true;
     }
 
     public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         session.getDeletedData().put(getPath(),data);
     }
 
     public void removeProperty(String name) throws RepositoryException {
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         boolean hasProperty = false;
         if (data.getBinaryProperties() != null) {
             hasProperty = data.getBinaryProperties().containsKey(name) || data.getProperties().containsKey(name);
             data.getBinaryProperties().remove(name);
         }
         if (hasProperty) {
             data.getProperties().remove(name);
             properties.remove(name);
             session.getChangedData().put(getPath(), data);
         }
     }
 
     public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         return addNode(relPath,null);
     }
 
     public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         String separator = StringUtils.equals(this.data.getId(),"/")?"":"/";
         ExternalData data = new ExternalData(this.data.getId() + separator + relPath ,getPath() + ( getPath().equals("/")? "" : "/" ) + relPath,primaryNodeTypeName,new HashMap<String, String[]>());
         session.getChangedData().put(data.getPath(),data);
         return  new ExternalNodeImpl(data,session);
     }
 
     public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
         if (srcChildRelPath.equals(destChildRelPath)) {
             return;
         }
         List<String> children = session.getOrderedData().containsKey(getPath()) ? session.getOrderedData().get(getPath()) : session.getRepository().getDataSource().getChildren(getPath());
 
         children.remove(srcChildRelPath);
         if (destChildRelPath == null || !children.contains(destChildRelPath)) {
             // put scrChildNode at the end of the list
             children.add(srcChildRelPath);
         } else {
             children.add(children.indexOf(destChildRelPath), srcChildRelPath);
         }
         session.getOrderedData().put(getPath(), children);
     }
 
     public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (getExtensionNode(true) != null && canItemBeExtended(getPropertyDefinition(name))) {
             return getExtensionNode(true).setProperty(name, value);
         }
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         if (value == null) {
             Property p = getProperty(name);
             if (hasProperty(name)) {
                 removeProperty(name);
                 return p;
             } else  {
                 String s = null;
                 return new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, new ExternalValueImpl(s));
             }
         }
         ExtendedPropertyDefinition epd = getPropertyDefinition(name);
 
         if (!hasProperty(name) || (hasProperty(name) && !getProperty(name).equals(value))) {
             if (name.equals(Constants.JCR_DATA)) {
                 if (data.getBinaryProperties() == null) {
                     data.setBinaryProperties(new HashMap<String, Binary[]>());
                 }
                 data.getBinaryProperties().put(name, new Binary[]{value.getBinary()});
             } else if (epd.isInternationalized()) {
                 Map<String,String[]> valMap = new HashMap<String, String[]>();
                 if (StringUtils.substringAfterLast(getPath(), "/").startsWith("j:translation_")) {
                     String lang = StringUtils.substringAfterLast(getPath(), "_");
                     valMap.put(lang,new String[]{value.getString()});
                     data.getI18nProperties().put(name,valMap);
                 }
 
             } else {
                 data.getProperties().put(name, new String[]{value.getString()});
             }
             properties.put(name, new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, value));
             session.getChangedData().put(getPath(),data);
         }
         return getProperty(name);
     }
 
     public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,value);
     }
 
     public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (getExtensionNode(true) != null && canItemBeExtended(getPropertyDefinition(name))) {
             return getExtensionNode(true).setProperty(name, values);
         }
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         if (values == null) {
             Property p = getProperty(name);
             if (hasProperty(name)) {
                 removeProperty(name);
                 return p;
             } else  {
                 String s = null;
                 return new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, new ExternalValueImpl(s));
             }
         }
 
         if (!hasProperty(name) || (hasProperty(name) && !getProperty(name).equals(values))) {
             String[] s = new String[values.length];
             for (int i = 0; i < values.length; i ++) {
                 if (values[i] != null)  {
                     s[i] = values[i].getString();
                 }   else {
                     s[i] = null;
                 }
             }
             data.getProperties().put(name,s);
             properties.put(name, new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, values));
             session.getChangedData().put(getPath(),data);
         }
         return getProperty(name);
     }
 
     public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,values);
     }
 
     public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value[] v = null;
         if (values != null) {
             v = new Value[values.length];
             for (int i =0; i < values.length; i ++ ) {
                 if (values[i] != null) {
                     v[i] = getSession().getValueFactory().createValue(values[i]);
                 }  else {
                     v[i] = null;
                 }
             }
         }
         return setProperty(name,v);
     }
 
     public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name, values);
     }
 
     public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,getSession().getValueFactory().createValue(value));
     }
 
     public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,value);
     }
 
     public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (getExtensionNode(true) != null && canItemBeExtended(getPropertyDefinition(name))) {
             return getExtensionNode(true).setProperty(name, value);
         }
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         if (value == null) {
             if (hasProperty(name)) {
                 Property p = getProperty(name);
                 removeProperty(name);
                 return p;
             } else {
                 Binary b = null;
                 return new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, new ExternalValueImpl(b));
             }
         }
         Value v = null;
         Binary binary = null;
         try{
             binary = new BinaryImpl(value);
             Binary[] b = {binary};
             if (data.getBinaryProperties() == null) {
                 data.setBinaryProperties(new HashMap<String, Binary[]>());
             }
             data.getBinaryProperties().put(name, b);
             v = getSession().getValueFactory().createValue(binary);
         } catch (IOException e) {
             throw new RepositoryException(e);
         }
 
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     public Node getNode(String s) throws PathNotFoundException, RepositoryException {
         return session.getNode(getPath().endsWith("/") ? getPath() + s : getPath() + "/" + s);
     }
 
     public NodeIterator getNodes() throws RepositoryException {
         List<String> l = session.getRepository().getDataSource().getChildren(getPath());
         if (data.getI18nProperties() != null) {
             l = new ArrayList<String>(l);
             for (String lang : data.getI18nProperties().keySet()) {
                 l.add("j:translation_"+lang);
             }
         }
         return new ExternalNodeIterator(l);
     }
 
     public NodeIterator getNodes(String namePattern) throws RepositoryException {
         final List<String> l = session.getRepository().getDataSource().getChildren(getPath());
         final List<String> filteredList = new ArrayList<String>();
         for (String path : l) {
             if (ChildrenCollectorFilter.matches(path,namePattern)) {
                 filteredList.add(path);
             }
         }
         if (data.getI18nProperties() != null) {
             for (String lang : data.getI18nProperties().keySet()) {
                 if (ChildrenCollectorFilter.matches("j:translation_"+lang,namePattern)) {
                     filteredList.add("j:translation_"+lang);
                 }
             }
         }
         return new ExternalNodeIterator(filteredList);
     }
 
     public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
         Node n = getExtensionNode(false);
         if (n != null && n.hasProperty(s)  && canItemBeExtended(getPropertyDefinition(s))) {
             return n.getProperty(s);
         }
         Property property = properties.get(s);
         if (property == null) {
             throw new PathNotFoundException(s);
         }
         return property;
     }
 
     public PropertyIterator getProperties() throws RepositoryException {
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalPropertyIterator(properties, n.getProperties());
         }
         return new ExternalPropertyIterator(properties);
     }
 
     public PropertyIterator getProperties(String namePattern) throws RepositoryException {
         final Map<String, ExternalPropertyImpl> filteredList = new HashMap<String, ExternalPropertyImpl>();
         for (Map.Entry<String, ExternalPropertyImpl> entry : properties.entrySet()) {
             if (ChildrenCollectorFilter.matches(entry.getKey(),namePattern)) {
                 filteredList.put(entry.getKey(), entry.getValue());
             }
         }
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalPropertyIterator(filteredList, n.getProperties(namePattern));
         }
         return new ExternalPropertyIterator(filteredList);
     }
 
     public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
         throw new ItemNotFoundException("External node does not support getPrimaryItem");
     }
 
     public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
         return getIdentifier();
     }
 
     public int getIndex() throws RepositoryException {
         return 0;
     }
 
     public PropertyIterator getReferences() throws RepositoryException {
         return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>());
     }
 
     public boolean hasNode(String s) throws RepositoryException {
         return session.itemExists(getPath().endsWith("/") ? getPath() + s : getPath() + "/" + s);
     }
 
     public boolean hasProperty(String relPath) throws RepositoryException {
         return properties.containsKey(relPath);
     }
 
     public boolean hasNodes() throws RepositoryException {
         return getNodes().hasNext();
     }
 
     public boolean hasProperties() throws RepositoryException {
         return !properties.isEmpty();
     }
 
     public NodeType getPrimaryNodeType() throws RepositoryException {
         return getExtendedPrimaryNodeType();
     }
 
     public ExtendedNodeType getExtendedPrimaryNodeType() throws RepositoryException {
         return NodeTypeRegistry.getInstance().getNodeType(data.getType());
     }
 
     public NodeType[] getMixinNodeTypes() throws RepositoryException {
         List<NodeType> nt = new ArrayList<NodeType>();
         if (data.getMixin() != null) {
             for (String s : data.getMixin()) {
                 nt.add(NodeTypeRegistry.getInstance().getNodeType(s));
             }
         }
         return nt.toArray(new NodeType[nt.size()]);
     }
 
     public boolean isNodeType(String nodeTypeName) throws RepositoryException {
         if (getPrimaryNodeType().isNodeType(nodeTypeName)) {
             return true;
         }
         for (NodeType nodeType : getMixinNodeTypes()) {
             if (nodeType.isNodeType(nodeTypeName)) {
                 return true;
             }
         }
         return false;
     }
 
     public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
         return false;
     }
 
     public NodeDefinition getDefinition() throws RepositoryException {
         ExternalNodeImpl parentNode = (ExternalNodeImpl) getParent();
         ExtendedNodeType parentNodeType = parentNode.getExtendedPrimaryNodeType();
         ExtendedNodeDefinition nodeDefinition = parentNodeType.getNodeDefinition(getPrimaryNodeType().getName());
         if (nodeDefinition != null) {
             return nodeDefinition;
         }
         for (ExtendedNodeDefinition extendedNodeDefinition : parentNodeType.getUnstructuredChildNodeDefinitions().values()) {
             return extendedNodeDefinition;
         }
         return null;
     }
 
     public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
     }
 
     public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
         return getPath();
     }
 
     public boolean isCheckedOut() throws RepositoryException {
         return true;
     }
 
     public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public Lock lock(boolean b, boolean b1) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
         throw new UnsupportedRepositoryOperationException("Locking is not supported on External repository");
     }
 
     public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException("Locking is not supported on External repository");
     }
 
     public boolean holdsLock() throws RepositoryException {
         return false;
     }
 
     public boolean isLocked() throws RepositoryException {
         return false;
     }
 
     public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         InputStream stream = value.getStream();
         try {
             return setProperty(name, stream);
         } finally {
             IOUtils.closeQuietly(stream);
         }
     }
 
     public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
         final List<String> l = session.getRepository().getDataSource().getChildren(getPath());
         final List<String> filteredList = new ArrayList<String>();
         for (String path : l) {
             if (ChildrenCollectorFilter.matches(path,nameGlobs)) {
                 filteredList.add(path);
             }
         }
         return new ExternalNodeIterator(filteredList);
     }
 
     public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
         final Map<String, ExternalPropertyImpl> filteredList = new HashMap<String, ExternalPropertyImpl>();
         for (Map.Entry<String, ExternalPropertyImpl> entry : properties.entrySet()) {
             if (ChildrenCollectorFilter.matches(entry.getKey(),nameGlobs)) {
                 filteredList.put(entry.getKey(), entry.getValue());
             }
         }
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalPropertyIterator(filteredList, n.getProperties(nameGlobs));
         }
         return new ExternalPropertyIterator(filteredList);
     }
 
     public String getIdentifier() throws RepositoryException {
         if (uuid == null) {
             if (!session.getRepository().getDataSource().isSupportsUuid() || data.getId().startsWith("translation:")) {
                 uuid = getStoreProvider().getInternalIdentifier(data.getId());
                 if (uuid == null) {
                     // not mapped yet -> store mapping
                     uuid = getStoreProvider().mapInternalIdentifier(data.getId());
                 }
             } else {
                 uuid = data.getId();
             }
         }
 
         return uuid;
     }
 
     public PropertyIterator getReferences(String name) throws RepositoryException {
         return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>());
     }
 
     public PropertyIterator getWeakReferences() throws RepositoryException {
         return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>());
     }
 
     public PropertyIterator getWeakReferences(String name) throws RepositoryException {
         return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>());
     }
 
     public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public NodeIterator getSharedSet() throws RepositoryException {
         return new ExternalNodeIterator(new ArrayList<String>());
     }
 
     public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
         return new String[0];  
     }
 
     public Node getExtensionNode(boolean create) throws RepositoryException {
         Session extensionSession = getSession().getExtensionSession();
         if (extensionSession == null) {
             return null;
         }
         List<String> extensionAllowedTypes = getSession().getExtensionAllowedTypes();
         if (extensionAllowedTypes != null) {
             for (String type : extensionAllowedTypes) {
                 if (!isNodeType(type)) {
                     return null;
                 }
             }
         }
         String path = getPath();
         boolean isRoot = path.equals("/");
 
         String mountPoint = getStoreProvider().getMountPoint();
         String globalPath = mountPoint + (isRoot ? "" : path);
 
         if (!extensionSession.itemExists(globalPath)) {
             if (!create) {
                 return null;
             } else if (isRoot) {
                 String parent = StringUtils.substringBeforeLast(mountPoint, "/");
                 if (parent.equals("")) {
                     parent = "/";
                 }
                 extensionSession.getNode(parent).addNode(StringUtils.substringAfterLast(mountPoint, "/"), getPrimaryNodeType().getName());
             } else {
                 ((ExternalNodeImpl) getParent()).getExtensionNode(true).addNode(getName(), getPrimaryNodeType().getName());
             }
         }
 
         return extensionSession.getNode(globalPath);
     }
 
     private boolean canItemBeExtended(ItemDefinition definition) throws RepositoryException {
         NodeType type = definition.getDeclaringNodeType();
 
         List<String> extensionAllowedTypes = getSession().getExtensionAllowedTypes();
         for (String extensionAllowedType : extensionAllowedTypes) {
             if (type.isNodeType(extensionAllowedType)) {
                 return true;
             }
         }
         return false;
     }
 
     private class ExternalPropertyIterator implements PropertyIterator {
         private int pos = 0;
         private Iterator<ExternalPropertyImpl> it;
         private PropertyIterator extensionPropertiesIterator;
         private Property nextProperty = null;
         private Map<String, ExternalPropertyImpl> externalProperties;
 
         ExternalPropertyIterator(Map<String, ExternalPropertyImpl> externalPropertyMap) {
             this(externalPropertyMap, null);
         }
 
         ExternalPropertyIterator(Map<String, ExternalPropertyImpl> externalPropertyMap, PropertyIterator extensionPropertiesIterator) {
             this.extensionPropertiesIterator = extensionPropertiesIterator;
             this.externalProperties = new HashMap<String, ExternalPropertyImpl>(externalPropertyMap);
             fetchNext();
         }
 
         private Property fetchNext() {
             nextProperty = null;
             if (extensionPropertiesIterator != null) {
                 while (extensionPropertiesIterator.hasNext()) {
                     Property next = extensionPropertiesIterator.nextProperty();
                     try {
                         if (canItemBeExtended(next.getDefinition())) {
                             nextProperty = next;
                             externalProperties.remove(next.getName());
                             return next;
                         }
                     } catch (RepositoryException e) {
                         e.printStackTrace();
                     }
                 }
             }
             if (it == null) {
                 it = externalProperties.values().iterator();
             }
             if (it.hasNext()) {
                 nextProperty = it.next();
                 return nextProperty;
             }
             return null;
         }
 
         @Override
         public Property nextProperty() {
             if (nextProperty == null) {
                 throw new NoSuchElementException();
             }
             Property next = nextProperty;
             fetchNext();
            pos ++;
             return next;
         }
 
         public void skip(long skipNum) {
             for (int i=0; i<skipNum; i++) {
                 nextProperty();
             }
         }
 
         @Override
         public long getSize() {
             return externalProperties.size() + (extensionPropertiesIterator != null ? extensionPropertiesIterator.getSize() : 0);
         }
 
         @Override
         public long getPosition() {
             return pos;
         }
 
         @Override
         public boolean hasNext() {
             return nextProperty != null;
         }
 
         @Override
         public Object next() {
             return nextProperty();
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("remove");
         }
     }
 
     private class ExternalNodeIterator implements NodeIterator {
         private int pos;
         private final Iterator<String> it;
         private final List<String> list;
 
         public ExternalNodeIterator(List<String> list) {
             this.list = list;
             it = list.iterator();
             pos = 0;
         }
 
         public Node nextNode() {
             pos++;
             try {
                 return getNode(it.next());
             } catch (RepositoryException e) {
                 logger.error(e.getMessage(), e);
                 return null;
             }
         }
 
         public void skip(long skipNum) {
             for (int i = 0; i<skipNum ; i++) {
                 it.next();
             }
             pos+=skipNum;
         }
 
         public long getSize() {
             return list.size();
         }
 
         public long getPosition() {
             return pos;
         }
 
         public boolean hasNext() {
             return it.hasNext();
         }
 
         public Object next() {
             return nextNode();
         }
 
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 }
