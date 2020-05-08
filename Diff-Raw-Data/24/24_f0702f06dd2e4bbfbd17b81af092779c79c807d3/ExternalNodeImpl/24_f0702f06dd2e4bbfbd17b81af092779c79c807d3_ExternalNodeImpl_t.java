 /**
  * ==========================================================================================
  * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
  * ==========================================================================================
  *
  *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
  *     streamlining Enterprise digital projects across channels to truly control
  *     time-to-market and TCO, project after project.
  *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
  *     marketing teams to collaboratively and iteratively build cutting-edge
  *     online business solutions.
  *     These, in turn, are securely and easily deployed as modules and apps,
  *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
  *     Each solution provided by Jahia stems from this overarching vision:
  *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
  *     Founded in 2002 and headquartered in Geneva, Switzerland,
  *     Jahia Solutions Group has its North American headquarters in Washington DC,
  *     with offices in Chicago, Toronto and throughout Europe.
  *     Jahia counts hundreds of global brands and governmental organizations
  *     among its loyal customers, in more than 20 countries across the globe.
  *
  *     For more information, please visit http://www.jahia.com
  *
  * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
  * ============================================
  *
  *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
  *
  *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
  *     1/GPL OR 2/JSEL
  *
  *     1/ GPL
  *     ==========================================================
  *
  *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
  *
  *     "This program is free software; you can redistribute it and/or
  *     modify it under the terms of the GNU General Public License
  *     as published by the Free Software Foundation; either version 2
  *     of the License, or (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program; if not, write to the Free Software
  *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  *     As a special exception to the terms and conditions of version 2.0 of
  *     the GPL (or any later version), you may redistribute this Program in connection
  *     with Free/Libre and Open Source Software ("FLOSS") applications as described
  *     in Jahia's FLOSS exception. You should have received a copy of the text
  *     describing the FLOSS exception, and it is also available here:
  *     http://www.jahia.com/license"
  *
  *     2/ JSEL - Commercial and Supported Versions of the program
  *     ==========================================================
  *
  *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
  *
  *     Alternatively, commercial and supported versions of the program - also known as
  *     Enterprise Distributions - must be used in accordance with the terms and conditions
  *     contained in a separate written agreement between you and Jahia Solutions Group SA.
  *
  *     If you are unsure which license is appropriate for your use,
  *     please contact the sales department at sales@jahia.com.
  */
 package org.jahia.modules.external;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.jackrabbit.util.ChildrenCollectorFilter;
 import org.apache.jackrabbit.value.BinaryImpl;
 import org.jahia.services.content.nodetypes.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.jcr.*;
 import javax.jcr.lock.Lock;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.*;
 import javax.jcr.version.Version;
 import javax.jcr.version.VersionException;
 import javax.jcr.version.VersionHistory;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.*;
 
 /**
  * Implementation of the {@link javax.jcr.Node} for the {@link org.jahia.modules.external.ExternalData}.
  *
  * @author Thomas Draier
  */
 public class ExternalNodeImpl extends ExternalItemImpl implements Node {
     private static final Logger logger = LoggerFactory.getLogger(ExternalNodeImpl.class);
 
     private static final String J_TRANSLATION = "j:translation_";
     private ExternalData data;
     private List<String> externalChildren;
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
 
     private NodeDefinition getChildNodeDefinition(String name, String childType) throws RepositoryException {
         Map<String, ExtendedNodeDefinition> nodeDefinitionsAsMap = getExtendedPrimaryNodeType().getChildNodeDefinitionsAsMap();
         if (nodeDefinitionsAsMap.containsKey(name)) {
             return nodeDefinitionsAsMap.get(name);
         }
         ExtendedNodeType childTypeNT = NodeTypeRegistry.getInstance().getNodeType(childType);
         for (NodeType nodeType : getMixinNodeTypes()) {
             nodeDefinitionsAsMap = ((ExtendedNodeType)nodeType).getChildNodeDefinitionsAsMap();
             if (nodeDefinitionsAsMap.containsKey(name)) {
                 return nodeDefinitionsAsMap.get(name);
             }
             for (Map.Entry<String, ExtendedNodeDefinition> entry : ((ExtendedNodeType)nodeType).getUnstructuredChildNodeDefinitions().entrySet()) {
                 if (childTypeNT.isNodeType(entry.getKey())) {
                     return entry.getValue();
                 }
             }
         }
         for (Map.Entry<String, ExtendedNodeDefinition> entry : getExtendedPrimaryNodeType().getUnstructuredChildNodeDefinitions().entrySet()) {
             if (childTypeNT.isNodeType(entry.getKey())) {
                 return entry.getValue();
             }
         }
         Node extensionNode = getExtensionNode(false);
         if (extensionNode != null && extensionNode.isNodeType("jnt:externalProviderExtension")) {
             return extensionNode.getDefinition();
 
         }
 
         return null;
     }
 
     public ExtendedPropertyDefinition getPropertyDefinition(String name) throws RepositoryException {
         Map<String, ExtendedPropertyDefinition> propertyDefinitionsAsMap = getExtendedPrimaryNodeType().getPropertyDefinitionsAsMap();
         if (propertyDefinitionsAsMap.containsKey(name)) {
             return propertyDefinitionsAsMap.get(name);
         }
         for (NodeType nodeType : getMixinNodeTypes(false)) {
             propertyDefinitionsAsMap = ((ExtendedNodeType)nodeType).getPropertyDefinitionsAsMap();
             if (propertyDefinitionsAsMap.containsKey(name)) {
                 return propertyDefinitionsAsMap.get(name);
             }
         }
         if (getExtensionNode(false) != null) {
             for (NodeType nodeType : getExtensionNode(false).getMixinNodeTypes()) {
                 nodeType = NodeTypeRegistry.getInstance().getNodeType(nodeType.getName());
                 propertyDefinitionsAsMap = ((ExtendedNodeType)nodeType).getPropertyDefinitionsAsMap();
                 if (propertyDefinitionsAsMap.containsKey(name)) {
                     return propertyDefinitionsAsMap.get(name);
                 }
             }
         }
 
         if (!getExtendedPrimaryNodeType().getUnstructuredPropertyDefinitions().isEmpty()) {
             return getExtendedPrimaryNodeType().getUnstructuredPropertyDefinitions().values().iterator().next();
         }
         return null;
     }
 
     public ExternalData getData() {
         return data;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getPath() throws RepositoryException {
         return data.getPath();
     }
 
     /**
      * {@inheritDoc}
      */
     public String getName() throws RepositoryException {
         return StringUtils.substringAfterLast(data.getPath(), "/");
     }
 
     /**
      * {@inheritDoc}
      */
     public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
         if (data.getPath().equals("/")) {
             throw new ItemNotFoundException();
         }
         String path = StringUtils.substringBeforeLast(data.getPath(), "/");
         return session.getNode(path.isEmpty() ? "/" : path);
     }
 
     public List<String> getExternalChildren() throws RepositoryException {
         if (externalChildren == null) {
             externalChildren = session.getRepository().getDataSource().getChildren(getPath());
         }
         return externalChildren;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isNode() {
         return true;
     }
 
     @Override
     void setNew(boolean isNew) throws RepositoryException {
         super.setNew(isNew);
         if (!isNew) {
             if (!data.getTmpId().equals(data.getId())) {
                 getSession().getRepository().getStoreProvider().getExternalProviderInitializerService().updateExternalIdentifier(data.getTmpId(), data.getId(), getSession().getRepository().getProviderKey(), false);
             }
             data.markSaved();
         }
      }
 
     /**
      * {@inheritDoc}
      */
     public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
 
         if (!data.getPath().equals("/")) {
             ((ExternalNodeImpl)getParent()).getExternalChildren().remove(getName());
         }
 
         session.getDeletedData().put(getPath(),data);
         session.unregisterNode(this);
     }
 
     protected void removeProperty(String name) throws RepositoryException {
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         boolean hasProperty = false;
         if (data.getBinaryProperties() != null && data.getBinaryProperties().containsKey(name)) {
             hasProperty = true;
             data.getBinaryProperties().remove(name);
             properties.remove(name);
         }
         if (data.getProperties() != null && data.getProperties().containsKey(name)) {
             hasProperty = true;
             data.getProperties().remove(name);
             properties.remove(name);
         }
         if (data.getLazyBinaryProperties() != null && data.getLazyBinaryProperties().contains(name)) {
             hasProperty = true;
             data.getLazyBinaryProperties().remove(name);
         }
         if (data.getLazyProperties() != null && data.getLazyProperties().contains(name)) {
             hasProperty = true;
             data.getLazyProperties().remove(name);
         }
         if (hasProperty) {
             session.getChangedData().put(getPath(), data);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         return addNode(relPath,null);
     }
 
     /**
      * {@inheritDoc}
      */
     public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
         Node extendedNode = getExtensionNode(true);
         if (extendedNode != null && canItemBeExtended(relPath, primaryNodeTypeName)) {
             Node n = extendedNode.addNode(relPath, primaryNodeTypeName);
             n.addMixin("jmix:externalProviderExtension");
             List<Value> values = ExtensionNode.createNodeTypeValues(session.getValueFactory(), primaryNodeTypeName);
             n.setProperty("j:extendedType", values.toArray(new Value[values.size()]));
             n.setProperty("j:isExternalProviderRoot", false);
             return new ExtensionNode(n,getPath() + "/" + relPath,getSession());
         }
 
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         String separator = StringUtils.equals(this.data.getId(),"/")?"":"/";
         ExternalData subNodeData = new ExternalData(this.data.getId() + separator + relPath ,getPath() + ( getPath().equals("/")? "" : "/" ) + relPath,primaryNodeTypeName,new HashMap<String, String[]>(), true);
         final ExternalNodeImpl newNode = new ExternalNodeImpl(subNodeData, session);
         session.registerNode(newNode);
         session.getChangedData().put(subNodeData.getPath(), subNodeData);
         session.setNewItem(newNode);
         getExternalChildren().add(relPath);
         return newNode;
     }
 
     /**
      * {@inheritDoc}
      */
     public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
         if (srcChildRelPath.equals(destChildRelPath)) {
             return;
         }
         List<String> children = getExternalChildren();
 
         children.remove(srcChildRelPath);
         if (destChildRelPath == null || !children.contains(destChildRelPath)) {
             // put scrChildNode at the end of the list
             children.add(srcChildRelPath);
         } else {
             children.add(children.indexOf(destChildRelPath), srcChildRelPath);
         }
         session.getOrderedData().put(getPath(), children);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (canItemBeExtended(getPropertyDefinition(name))) {
             final Node extensionNode = getExtensionNode(true);
             if (extensionNode != null) {
                 return new ExtensionProperty(extensionNode.setProperty(name, value), getPath()+"/"+name, session, this);
             }
         }
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         if (value == null) {
             if (hasProperty(name)) {
                 removeProperty(name);
             }
             return null;
         }
         ExtendedPropertyDefinition epd = getPropertyDefinition(name);
 
         if (!hasProperty(name) || (hasProperty(name) && !getProperty(name).getValue().equals(value))) {
             if (epd.getRequiredType() == PropertyType.BINARY) {
                 if (data.getBinaryProperties() == null) {
                     data.setBinaryProperties(new HashMap<String, Binary[]>());
                 }
                 data.getBinaryProperties().put(name, new Binary[]{value.getBinary()});
             } else if (epd.isInternationalized()) {
                 Map<String,String[]> valMap = new HashMap<String, String[]>();
                 if (getName().startsWith(J_TRANSLATION)) {
                     String lang = StringUtils.substringAfter(getName(), "_");
                     valMap.put(lang,new String[]{value.getString()});
                     data.getI18nProperties().put(name,valMap);
                 } else {
                     throw new ConstraintViolationException("Property " + name + " is internationalized");
                 }
             } else {
                 data.getProperties().put(name, new String[]{value.getString()});
             }
             final ExternalPropertyImpl newProperty = new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, value);
             properties.put(name, newProperty);
             session.setNewItem(newProperty);
             session.getChangedData().put(getPath(),data);
         }
         return getProperty(name);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,value);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (canItemBeExtended(getPropertyDefinition(name))) {
             final Node extensionNode = getExtensionNode(true);
             if (extensionNode != null) {
                 return new ExtensionProperty(extensionNode.setProperty(name, values), getPath()+ "/" + name, session, this);
             }
         }
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         if (values == null) {
             if (hasProperty(name)) {
                 removeProperty(name);
             }
             return null;
         }
 
         ExtendedPropertyDefinition epd = getPropertyDefinition(name);
 
         if (!hasProperty(name) || (hasProperty(name) && !Arrays.equals(getProperty(name).getValues(),values))) {
             if (epd.getRequiredType() == PropertyType.BINARY) {
                 if (data.getBinaryProperties() == null) {
                     data.setBinaryProperties(new HashMap<String, Binary[]>());
                 }
 
                 Binary[] b = new Binary[values.length];
                 for (int i = 0; i < values.length; i ++) {
                     b[i] = values[i] != null ? values[i].getBinary() : null;
                 }
                 data.getBinaryProperties().put(name, b);
             } else {
                 String[] s = new String[values.length];
                 for (int i = 0; i < values.length; i ++) {
                     s[i] = values[i] != null ? values[i].getString() : null;
                 }
                 if (epd.isInternationalized()) {
                     Map<String,String[]> valMap = new HashMap<String, String[]>();
                     if (getName().startsWith(J_TRANSLATION)) {
                         String lang = StringUtils.substringAfter(getName(), "_");
                         valMap.put(lang,s);
                         data.getI18nProperties().put(name,valMap);
                     } else {
                         throw new ConstraintViolationException("Property " + name + " is internationalized");
                     }
                 } else {
                     data.getProperties().put(name,s);
                 }
             }
             final ExternalPropertyImpl newProperty = new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, values);
             properties.put(name, newProperty);
             session.setNewItem(newProperty);
             session.getChangedData().put(getPath(),data);
         }
         return getProperty(name);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,values);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value[] v = null;
         if (values != null) {
             v = new Value[values.length];
             for (int i =0; i < values.length; i ++ ) {
                 v[i] = values[i] != null ? getSession().getValueFactory().createValue(values[i]) : null;
             }
         }
         return setProperty(name, v);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name, values);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             v = getSession().getValueFactory().createValue(value);
         }
         return setProperty(name, v);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,value);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("deprecation")
     public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (canItemBeExtended(getPropertyDefinition(name))) {
             final Node extensionNode = getExtensionNode(true);
             if (extensionNode != null) {
                 return new ExtensionProperty(extensionNode.setProperty(name, value), getPath()+ "/" + name, session, this);
             }
         }
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         if (value == null) {
             if (hasProperty(name)) {
                 removeProperty(name);
             }
             return null;
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
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             v = getSession().getValueFactory().createValue(value);
         }
         return setProperty(name, v);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             v = getSession().getValueFactory().createValue(value);
         }
         return setProperty(name, v);
     }
 
     /**
      * {@inheritDoc}
      */
     public Node getNode(String s) throws RepositoryException {
         Node n = session.getNode(getPath().endsWith("/") ? getPath() + s : getPath() + "/" + s);
         if (n != null) {
             return  n;
         }
         n = getExtensionNode(false);
         if (n != null) {
             return new ExtensionNode(n.getNode(s),getPath() + "/" + s,getSession());
         }
         throw new PathNotFoundException();
     }
 
     /**
      * {@inheritDoc}
      */
     public NodeIterator getNodes() throws RepositoryException {
         return getNodes("*");
     }
 
     /**
      * {@inheritDoc}
      */
     public NodeIterator getNodes(String namePattern) throws RepositoryException {
         List<String> filteredList = new ArrayList<String>();
         if (!namePattern.equals("j:translation*") && !data.isNew()) {
             for (String path : getExternalChildren()) {
                 if (namePattern.equals("*") || ChildrenCollectorFilter.matches(path,namePattern)) {
                     filteredList.add(path);
                 }
             }
         }
         Set<String> languages = new HashSet<String>();
         if (data.getI18nProperties() != null) {
             languages.addAll(data.getI18nProperties().keySet());
         }
         if (data.getLazyI18nProperties() != null) {
             languages.addAll(data.getLazyI18nProperties().keySet());
         }
         for (String lang : languages) {
             if (namePattern.equals("*") || ChildrenCollectorFilter.matches(J_TRANSLATION + lang, namePattern)) {
                 filteredList.add(J_TRANSLATION + lang);
             }
         }
 
         Node n = getExtensionNode(false);
         if (n != null) {
             return  new ExternalNodeIterator(filteredList, namePattern.equals("*") ? n.getNodes() : n.getNodes(namePattern));
         }
         return new ExternalNodeIterator(filteredList);
     }
 
     /**
      * {@inheritDoc}
      */
     public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
         final List<String> filteredList = new ArrayList<String>();
         for (String path : getExternalChildren()) {
             if (ChildrenCollectorFilter.matches(path,nameGlobs)) {
                 filteredList.add(path);
             }
         }
         Set<String> languages = new HashSet<String>();
         if (data.getI18nProperties() != null) {
             languages.addAll(data.getI18nProperties().keySet());
         }
         if (data.getLazyI18nProperties() != null) {
             languages.addAll(data.getLazyI18nProperties().keySet());
         }
         for (String lang : languages) {
             if (ChildrenCollectorFilter.matches(J_TRANSLATION + lang, nameGlobs)) {
                 filteredList.add(J_TRANSLATION + lang);
             }
         }
 
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalNodeIterator(filteredList,n.getNodes(nameGlobs));
         }
         return new ExternalNodeIterator(filteredList);
     }
 
 
 
     /**
      * {@inheritDoc}
      */
     public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
         Node n = getExtensionNode(false);
         if (n != null && n.hasProperty(s)  && canItemBeExtended(getPropertyDefinition(s))) {
             return new ExtensionProperty(n.getProperty(s), getPath() + "/" + s, session, this);
         }
         Property property = properties.get(s);
         if (property == null) {
             if (data.getLazyProperties() != null && data.getLazyProperties().contains(s)) {
                 String[] values;
                 if (properties.containsKey("jcr:language")) {
                     values = session.getI18nPropertyValues(data, properties.get("jcr:language").getString(), s);
                 } else {
                     values = session.getPropertyValues(data, s);
                 }
                 data.getProperties().put(s, values);
                 data.getLazyProperties().remove(s);
                 ExternalPropertyImpl p = new ExternalPropertyImpl(new Name(s, NodeTypeRegistry.getInstance().getNamespaces()), this, session);
                 if (getPropertyDefinition(s).isMultiple()) {
                     p.setValue(values);
                 } else if (values != null && values.length > 0) {
                     p.setValue(values[0]);
                 }
                 properties.put(s, p);
                 return p;
             } else if (data.getLazyBinaryProperties() != null && data.getLazyBinaryProperties().contains(s)) {
                 Binary[] values = session.getBinaryPropertyValues(data, s);
                 data.getBinaryProperties().put(s, values);
                 data.getLazyBinaryProperties().remove(s);
                 ExternalPropertyImpl p = new ExternalPropertyImpl(new Name(s, NodeTypeRegistry.getInstance().getNamespaces()), this, session);
                 if (getPropertyDefinition(s).isMultiple()) {
                     p.setValue(values);
                 } else if (values != null && values.length > 0) {
                     p.setValue(values[0]);
                 }
                 properties.put(s, p);
                 return p;
             } else {
                 throw new PathNotFoundException(s);
             }
         }
         return property;
     }
 
     /**
      * {@inheritDoc}
      */
     public PropertyIterator getProperties() throws RepositoryException {
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalPropertyIterator(properties, n.getProperties(), data.getLazyProperties(), data.getLazyBinaryProperties(), this);
         }
         return new ExternalPropertyIterator(properties, data.getLazyProperties(), data.getLazyBinaryProperties(), this);
     }
 
     /**
      * {@inheritDoc}
      */
     public PropertyIterator getProperties(String namePattern) throws RepositoryException {
         final Map<String, ExternalPropertyImpl> filteredList = new HashMap<String, ExternalPropertyImpl>();
         for (Map.Entry<String, ExternalPropertyImpl> entry : properties.entrySet()) {
             if (ChildrenCollectorFilter.matches(entry.getKey(),namePattern)) {
                 filteredList.put(entry.getKey(), entry.getValue());
             }
         }
         Set<String> lazyProperties = null;
         if (data.getLazyProperties() != null) {
             lazyProperties = new HashSet<String>();
             for (String propertyName : data.getLazyProperties()) {
                 if (ChildrenCollectorFilter.matches(propertyName, namePattern)) {
                     lazyProperties.add(propertyName);
                 }
             }
         }
         Set<String> lazyBinaryProperties = null;
         if (data.getLazyBinaryProperties() != null) {
             lazyBinaryProperties = new HashSet<String>();
             for (String propertyName : data.getLazyBinaryProperties()) {
                 if (ChildrenCollectorFilter.matches(propertyName, namePattern)) {
                     lazyBinaryProperties.add(propertyName);
                 }
             }
         }
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalPropertyIterator(filteredList, n.getProperties(namePattern), lazyProperties, lazyBinaryProperties, this);
         }
         return new ExternalPropertyIterator(filteredList, lazyProperties, lazyBinaryProperties, this);
     }
 
     /**
      * {@inheritDoc}
      */
     public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
         throw new ItemNotFoundException("External node does not support getPrimaryItem");
     }
 
     /**
      * {@inheritDoc}
      */
     public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
         return getIdentifier();
     }
 
     /**
      * {@inheritDoc}
      */
     public int getIndex() throws RepositoryException {
         return 1;
     }
 
     /**
      * {@inheritDoc}
      */
     public PropertyIterator getReferences() throws RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean hasNode(String s) throws RepositoryException {
         return session.itemExists(getPath().endsWith("/") ? getPath() + s : getPath() + "/" + s);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean hasProperty(String relPath) throws RepositoryException {
         return properties.containsKey(relPath) ||
                 (data.getLazyProperties() != null && data.getLazyProperties().contains(relPath)) ||
                 (data.getLazyBinaryProperties() != null && data.getLazyBinaryProperties().contains(relPath)) ||
                 (getExtensionNode(false) != null && getPropertyDefinition(relPath) != null && canItemBeExtended(getPropertyDefinition(relPath)) && getExtensionNode(false).hasProperty(relPath));
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean hasNodes() throws RepositoryException {
         return getNodes().hasNext();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean hasProperties() throws RepositoryException {
         return !properties.isEmpty() ||
                 (data.getLazyProperties() != null && !data.getLazyProperties().isEmpty()) ||
                 (data.getLazyBinaryProperties() != null && !data.getLazyBinaryProperties().isEmpty()) ||
                 getProperties().hasNext();
     }
 
     /**
      * {@inheritDoc}
      */
     public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
         return getExtendedPrimaryNodeType();
     }
 
     public ExtendedNodeType getExtendedPrimaryNodeType() throws RepositoryException {
         return NodeTypeRegistry.getInstance().getNodeType(data.getType());
     }
 
     /**
      * {@inheritDoc}
      */
     public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
         return getMixinNodeTypes(true);
     }
 
     private ExtendedNodeType[] getMixinNodeTypes(boolean withExtension) throws RepositoryException {
         List<ExtendedNodeType> nt = new ArrayList<ExtendedNodeType>();
         if (data.getMixin() != null) {
             for (String s : data.getMixin()) {
                 nt.add(NodeTypeRegistry.getInstance().getNodeType(s));
             }
         }
         if (withExtension) {
             Node extensionNode = getExtensionNode(false);
             if (extensionNode != null) {
                 for (NodeType type : extensionNode.getMixinNodeTypes()) {
                     if (!type.isNodeType("jmix:externalProviderExtension")) {
                         nt.add(NodeTypeRegistry.getInstance().getNodeType(type.getName()));
                     }
                 }
             }
         }
         return nt.toArray(new ExtendedNodeType[nt.size()]);
     }
 
     /**
      * {@inheritDoc}
      */
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
 
     /**
      * {@inheritDoc}
      */
     public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         if (isNodeType(mixinName)) {
             return;
         }
 
         Node extensionNode = getExtensionNode(true);
         if (extensionNode != null) {
             extensionNode.addMixin(mixinName);
             return;
         }
 
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         Node extensionNode = getExtensionNode(false);
         if (extensionNode != null) {
             extensionNode.removeMixin(mixinName);
 
 
             // remove child node and properties brought by the mixin
             PropertyIterator pi = getProperties();
             while (pi.hasNext()) {
                 Property extensionProperty = pi.nextProperty();
                 List<NodeType> nodeTypes = new ArrayList<NodeType>();
                 nodeTypes.addAll(Arrays.asList(getMixinNodeTypes(true)));
                 nodeTypes.add(NodeTypeRegistry.getInstance().getNodeType("jmix:externalProviderExtension"));
                 boolean canSetProperty = extensionProperty.isMultiple()?getPrimaryNodeType().canSetProperty(extensionProperty.getName(), extensionProperty.getValues()):getPrimaryNodeType().canSetProperty(extensionProperty.getName(), extensionProperty.getValue());
                 for (PropertyDefinition propertyDefinition : getPrimaryNodeType().getPropertyDefinitions()) {
                     if (propertyDefinition.getName().equals(extensionProperty.getName()) && propertyDefinition.getRequiredType() == extensionProperty.getType()) {
                         canSetProperty = true;
                         break;
                     }
                 }
                 if (!canSetProperty) {
                     for (NodeType mixinType : nodeTypes) {
                         if (!StringUtils.equals(mixinType.getName(),mixinName)) {
                             if (extensionProperty.isMultiple()) {
                                 if (mixinType.canSetProperty(extensionProperty.getName(),extensionProperty.getValues())) {
                                     canSetProperty = true;
                                     break;
                                 }
                             } else {
                                 if (mixinType.canSetProperty(extensionProperty.getName(),extensionProperty.getValue())) {
                                     canSetProperty = true;
                                     break;
                                 }
                             }
                         }
                     }
                     if (!canSetProperty) {
                         extensionProperty.remove();
                     }
                 }
             }
             NodeIterator ni = extensionNode.getNodes();
             while (ni.hasNext()) {
                 Node extensionChildNode = ni.nextNode();
                 boolean canAddChildNode = getPrimaryNodeType().canAddChildNode(extensionChildNode.getName(), getPrimaryNodeType().getName());
                 if (!canAddChildNode) {
                     for (NodeType mixinType : getMixinNodeTypes(true)) {
                         if (!StringUtils.equals(mixinType.getName(),mixinName)) {
                             if (mixinType.canAddChildNode(extensionChildNode.getName(), getPrimaryNodeType().getName())) {
                                 canAddChildNode = true;
                                 break;
                             }
                         }
                     }
                     if (!canAddChildNode) {
                         extensionChildNode.remove();
                     }
                 }
             }
             return;
         }
         if (!isNodeType(mixinName)) {
             throw new NoSuchNodeTypeException("Mixin "+mixinName + " not included in node "+getPath());
         }
 
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException {
         Node extensionNode = getExtensionNode(true);
         if (extensionNode != null) {
             return extensionNode.canAddMixin(mixinName);
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public NodeDefinition getDefinition() throws RepositoryException {
         ExternalNodeImpl parentNode = (ExternalNodeImpl) getParent();
         ExtendedNodeType parentNodeType = parentNode.getExtendedPrimaryNodeType();
         ExtendedNodeDefinition nodeDefinition = parentNodeType.getChildNodeDefinitionsAsMap().get(getName());
         if (nodeDefinition != null) {
             return nodeDefinition;
         }
         for (Map.Entry<String, ExtendedNodeDefinition> entry : parentNodeType.getUnstructuredChildNodeDefinitions().entrySet()) {
             if (isNodeType(entry.getKey())) {
                 return entry.getValue();
             }
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
     }
 
     /**
      * {@inheritDoc}
      */
     public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
         return getPath();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isCheckedOut() throws RepositoryException {
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public Lock lock(boolean b, boolean b1) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
         return session.getWorkspace().getLockManager().lock(getPath(),b,b1, Long.MAX_VALUE, null);
     }
 
     /**
      * {@inheritDoc}
      */
     public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
         return session.getWorkspace().getLockManager()!= null ?session.getWorkspace().getLockManager().getLock(getPath()):null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
         if (session.getWorkspace().getLockManager()!=null) {
             session.getWorkspace().getLockManager().unlock(getPath());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean holdsLock() throws RepositoryException {
         return session.getWorkspace().getLockManager()!=null && session.getWorkspace().getLockManager().getLock(getPath()) != null;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isLocked() throws RepositoryException {
         return  session.getWorkspace().getLockManager()!=null && session.getWorkspace().getLockManager().isLocked(getPath());
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             InputStream stream = value.getStream();
             try {
                 return setProperty(name, stream);
             } finally {
                 IOUtils.closeQuietly(stream);
             }
         }
         return setProperty(name, v);
     }
 
     /**
      * {@inheritDoc}
      */
     public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             v = getSession().getValueFactory().createValue(value);
         }
         return setProperty(name, v);
     }
 
     /**
      * {@inheritDoc}
      */
     public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
         final Map<String, ExternalPropertyImpl> filteredList = new HashMap<String, ExternalPropertyImpl>();
         for (Map.Entry<String, ExternalPropertyImpl> entry : properties.entrySet()) {
             if (ChildrenCollectorFilter.matches(entry.getKey(),nameGlobs)) {
                 filteredList.put(entry.getKey(), entry.getValue());
             }
         }
         Set<String> lazyProperties = null;
         if (data.getLazyProperties() != null) {
             lazyProperties = new HashSet<String>();
             for (String propertyName : data.getLazyProperties()) {
                 if (ChildrenCollectorFilter.matches(propertyName, nameGlobs)) {
                     lazyProperties.add(propertyName);
                 }
             }
         }
         Set<String> lazyBinaryProperties = null;
         if (data.getLazyBinaryProperties() != null) {
             lazyBinaryProperties = new HashSet<String>();
             for (String propertyName : data.getLazyBinaryProperties()) {
                 if (ChildrenCollectorFilter.matches(propertyName, nameGlobs)) {
                     lazyBinaryProperties.add(propertyName);
                 }
             }
         }
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalPropertyIterator(filteredList, n.getProperties(nameGlobs), lazyProperties, lazyBinaryProperties, this);
         }
         return new ExternalPropertyIterator(filteredList, lazyProperties, lazyBinaryProperties, this);
     }
 
     /**
      * {@inheritDoc}
      */
     public final String getIdentifier() throws RepositoryException {
         if (uuid == null) {
             if (!session.getRepository().getDataSource().isSupportsUuid() || data.getId().startsWith(ExternalSessionImpl.TRANSLATION_PREFIX)) {
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
 
     /**
      * {@inheritDoc}
      */
     public PropertyIterator getReferences(String name) throws RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public PropertyIterator getWeakReferences() throws RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public PropertyIterator getWeakReferences(String name) throws RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public NodeIterator getSharedSet() throws RepositoryException {
         return new ExternalNodeIterator(new ArrayList<String>());
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
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
                 final Node extParent = extensionSession.getNode(parent);
                 takeLockToken(extParent);
                 extParent.addMixin("jmix:hasExternalProviderExtension");
                 Node n = extParent.addNode(StringUtils.substringAfterLast(mountPoint, "/"), "jnt:externalProviderExtension");
                 List<Value> values = ExtensionNode.createNodeTypeValues(session.getValueFactory(), getPrimaryNodeType().getName());
                 n.setProperty("j:extendedType", values.toArray(new Value[values.size()]));
                 n.addMixin("jmix:externalProviderExtension");
                 n.setProperty("j:isExternalProviderRoot", true);
                 n.setProperty("j:externalNodeIdentifier", getIdentifier());
             } else {
                 final Node extParent = ((ExternalNodeImpl) getParent()).getExtensionNode(true);
                 takeLockToken(extParent);
                 Node n = extParent.addNode(getName(), "jnt:externalProviderExtension");
                 List<Value> values = ExtensionNode.createNodeTypeValues(session.getValueFactory(), getPrimaryNodeType().getName());
                 n.setProperty("j:extendedType", values.toArray(new Value[values.size()]));
                 n.addMixin("jmix:externalProviderExtension");
                 n.setProperty("j:isExternalProviderRoot", false);
                 n.setProperty("j:externalNodeIdentifier", getIdentifier());
             }
         }
 
         return extensionSession.getNode(globalPath);
     }
 
     public void takeLockToken(Node parentNode) throws RepositoryException {
         if (parentNode.isLocked() && parentNode.hasProperty("j:locktoken")) {
             parentNode.getSession().addLockToken(parentNode.getProperty("j:locktoken").getString());
         }
     }
 
     public boolean canItemBeExtended(String relPath, String primaryNodeTypeName) throws RepositoryException {
         return canItemBeExtended(getChildNodeDefinition(relPath, primaryNodeTypeName));
     }
 
     public boolean canItemBeExtended(ItemDefinition definition) throws RepositoryException {
         if (definition == null) {
             throw new ConstraintViolationException();
         }
 
         NodeType type = definition.getDeclaringNodeType();
 
         Map<String,List<String>> overridableProperties = getSession().getOverridableProperties();
         for (Map.Entry<String, List<String>> entry : overridableProperties.entrySet()) {
             if ((entry.getKey().equals("*") || type.getName().equals(entry.getKey())) &&
                     (entry.getValue().contains("*") || entry.getValue().contains(definition.getName()))) {
                 return true;
             }
         }
 
         if (type.isMixin()) {
             Node ext = getExtensionNode(false);
             if (ext != null) {
                 for (NodeType assignedMixin : ext.getMixinNodeTypes()) {
                     if (assignedMixin.isNodeType(type.getName())) {
                         return true;
                     }
                 }
             }
         }
 
         return false;
     }
 
    @Override
    public String toString() {
        return "external node " + data.getPath();
    }

     /**
      * Property iterator implementation
      */
     private class ExternalPropertyIterator implements PropertyIterator {
         private int pos = 0;
         private Iterator<ExternalPropertyImpl> it;
         private PropertyIterator extensionPropertiesIterator;
         private Property nextProperty = null;
         private Map<String, ExternalPropertyImpl> externalProperties;
         private Set<String> lazyProperties;
         private Iterator<String> lazyPropertiesIterator;
         private ExternalNodeImpl node;
 
         ExternalPropertyIterator(Map<String, ExternalPropertyImpl> externalPropertyMap, Set<String> lazyProperties,
                                  Set<String> lazyBinaryProperties, ExternalNodeImpl node) {
             this(externalPropertyMap, null, lazyProperties, lazyBinaryProperties, node);
         }
 
         ExternalPropertyIterator(Map<String, ExternalPropertyImpl> externalPropertyMap, ExternalNodeImpl node) {
             this(externalPropertyMap, null, null, null, node);
         }
 
         ExternalPropertyIterator(Map<String, ExternalPropertyImpl> externalPropertyMap,
                                  PropertyIterator extensionPropertiesIterator, Set<String> lazyProperties,
                                  Set<String> lazyBinaryProperties, ExternalNodeImpl node) {
             this.extensionPropertiesIterator = extensionPropertiesIterator;
             this.externalProperties = new HashMap<String, ExternalPropertyImpl>(externalPropertyMap);
             this.lazyProperties = new HashSet<String>();
             if (lazyProperties != null) {
                 this.lazyProperties.addAll(lazyProperties);
             }
             if (lazyBinaryProperties != null) {
                 this.lazyProperties.addAll(lazyBinaryProperties);
             }
             this.node = node;
             fetchNext();
         }
 
         private void fetchNext() {
             nextProperty = null;
             if (extensionPropertiesIterator != null) {
                 while (extensionPropertiesIterator.hasNext()) {
                     Property next = extensionPropertiesIterator.nextProperty();
                     try {
                         final ExtendedPropertyDefinition propertyDefinition = getPropertyDefinition(next.getName());
                         if (propertyDefinition != null && canItemBeExtended(propertyDefinition)) {
                             nextProperty = new ExtensionProperty(next, getPath() + "/" + next.getName(), node.getSession(), ExternalNodeImpl.this);
                             externalProperties.remove(next.getName());
                             lazyProperties.remove(next.getName());
                             return;
                         }
                     } catch (RepositoryException e) {
                         logger.error("Cannot get property", e);
                     }
                 }
             }
             if (it == null) {
                 it = externalProperties.values().iterator();
             }
             if (it.hasNext()) {
                 nextProperty = it.next();
                 return;
             }
             if (lazyPropertiesIterator == null && lazyProperties != null) {
                 lazyPropertiesIterator = lazyProperties.iterator();
             }
             if (lazyPropertiesIterator != null && lazyPropertiesIterator.hasNext()) {
                 String propertyName = lazyPropertiesIterator.next();
                 try {
                     nextProperty = getProperty(propertyName);
                 } catch (RepositoryException e) {
                     logger.error(e.getMessage(),e);
                     logger.error(e.getMessage(),e);
                 }
                 return;
             }
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
 
     /**
      * Node iterator implementation
      */
     private class ExternalNodeIterator implements NodeIterator {
         private int pos = 0;
         private Iterator<String> it;
         private final List<String> list;
         private NodeIterator extensionNodeIterator;
         private Node nextNode;
 
         public ExternalNodeIterator(List<String> list) {
             this(list,null);
         }
 
         public ExternalNodeIterator(List<String> list, NodeIterator extensionNodeIterator) {
             this.extensionNodeIterator = extensionNodeIterator;
             this.list = list;
             this.it = list.iterator();
             fetchNext();
         }
 
         private Node fetchNext() {
             nextNode = null;
             if (it.hasNext()) {
                 Node next = null;
                 try {
                     next = getNode(it.next());
                 } catch (RepositoryException e) {
                     logger.error(e.getMessage(),e);
                 }
                 nextNode = next;
                 return nextNode;
             }
             if (extensionNodeIterator != null) {
                 while (extensionNodeIterator.hasNext()) {
                     Node n = extensionNodeIterator.nextNode();
                     try {
                         NodeDefinition childNodeDefinition = getChildNodeDefinition(n.getName(), n.getPrimaryNodeType().getName());
                         if (childNodeDefinition == null) {
                             for (NodeType t : n.getMixinNodeTypes()) {
                                 childNodeDefinition = getChildNodeDefinition(n.getName(), t.getName());
                                 if (childNodeDefinition != null) {
                                     break;
                                 }
                             }
                         }
                         if (childNodeDefinition != null && canItemBeExtended(childNodeDefinition) && !list.contains(n.getName())) {
                             nextNode = new ExtensionNode(n,getPath() + "/" + n.getName(),getSession());
                             return  nextNode;
                         }
                     } catch (RepositoryException e) {
                         logger.error(e.getMessage(),e);
                     }
                 }
             }
             return null;
         }
 
         public Node nextNode() {
             if (nextNode == null) {
                 throw new NoSuchElementException();
             }
             Node next = nextNode;
             fetchNext();
             pos++;
             return next;
         }
 
         public void skip(long skipNum) {
             for (int i = 0; i<skipNum ; i++) {
                 nextNode();
             }
         }
 
         public long getSize() {
             return list.size() + (extensionNodeIterator!= null ? extensionNodeIterator.getSize():0);
         }
 
         public long getPosition() {
             return pos;
         }
 
         public boolean hasNext() {
             return nextNode != null;
         }
 
         public Object next() {
             return nextNode();
         }
 
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 }
