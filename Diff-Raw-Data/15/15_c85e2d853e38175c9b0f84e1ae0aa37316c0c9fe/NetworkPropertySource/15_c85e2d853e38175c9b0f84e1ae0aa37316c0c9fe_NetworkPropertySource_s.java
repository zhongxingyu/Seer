 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.awe.views.network.property;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amanzi.awe.catalog.neo.NeoCatalogPlugin;
 import org.amanzi.awe.catalog.neo.upd_layers.events.UpdateLayerEvent;
 import org.amanzi.awe.views.network.proxy.NeoNode;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.IndexManager;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.statistic.IPropertyHeader;
 import org.amanzi.neo.services.statistic.PropertyHeader;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertySource;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.neoclipse.property.NodePropertySource;
 import org.neo4j.neoclipse.property.PropertyDescriptor;
 import org.neo4j.neoclipse.property.PropertyTransform;
 import org.neo4j.neoclipse.property.PropertyTransform.PropertyHandler;
 
 // TODO: Auto-generated Javadoc
 /**
  * Class that creates a properties of given Node.
  *
  * @author Lagutko_N
  * @since 1.0.0
  */
 
 public class NetworkPropertySource extends NodePropertySource implements IPropertySource {
     
     /** The is delta node. */
     private boolean isDeltaNode;
     
     /** The pattern. */
     Pattern pattern = Pattern.compile("Delta (\\w+)\\s+(.*)");
 
     /**
      * Instantiates a new network property source.
      *
      * @param node the node
      */
     public NetworkPropertySource(NeoNode node) {
         super(node.getNode(), null);
         isDeltaNode = node.getNode().getProperty("type","").toString().startsWith("delta_");
     }
 
     /**
      * Returns the descriptors for the properties of the node.
      *
      * @return the property descriptors
      */
     public IPropertyDescriptor[] getPropertyDescriptors() {
         List<IPropertyDescriptor> descs = new ArrayList<IPropertyDescriptor>();
         descs.addAll(getHeadPropertyDescriptors());
         Iterable<String> keys = container.getPropertyKeys();
         for (String key : keys) {
             Object value = container.getProperty((String)key);
             Class< ? > c = value.getClass();
             if(isDeltaNode && key.startsWith("Delta ")) {
                 String name = key.replace("Delta ", "");
                 String category = "Changes for";
                 Matcher matcher = pattern.matcher(key);
                 if(matcher.matches()) {
                     name = matcher.group(2);
                     category = category + " " + matcher.group(1);
                 } else if(container.hasProperty("name")) {
                     category = category + " " + container.getProperty("name");
                 }
                 descs.add(new PropertyDescriptor(key, name, category, c));
             } else {
                 NodeTypes nt = NodeTypes.getNodeType(container,null);
                 if(nt == null || nt.isPropertyEditable(key))
                     descs.add(new PropertyDescriptor(key, key, PROPERTIES_CATEGORY, c));
                 else
                     descs.add(new PropertyDescriptor(key, key, NODE_CATEGORY));
             }
         }
         return descs.toArray(new IPropertyDescriptor[descs.size()]);
     }
 
     /**
      * Sets the property value.
      *
      * @param id the id
      * @param value the value
      */
     @Override
     public void setPropertyValue(Object id, Object value) {
         if (!((String)id).startsWith("delta_")) {
             Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
             try {
                 DatasetService service = NeoServiceFactory.getInstance().getDatasetService();
 
                 Node root = service.findRootByChild((Node)container);
                 Object oldValue=null;
                 if (container.hasProperty((String)id)) {
                     oldValue=container.getProperty((String)id);
 
                     // try to keep the same type as the previous value
                     Class< ? > c = container.getProperty((String)id).getClass();
                     PropertyHandler propertyHandler = PropertyTransform.getHandler(c);
                     if (propertyHandler == null) {
                         MessageDialog.openError(null, "Error", "No property handler was found for type " + c.getSimpleName() + ".");
                         return;
                     }
                     Object o = null;
                     try {
                         o = propertyHandler.parse(value);
                     } catch (Exception e) {
                         MessageDialog.openError(null, "Error", "Could not parse the input as type " + c.getSimpleName() + ".");
                         return;
                     }
                     if (o == null) {
                         MessageDialog.openError(null, "Error", "Input parsing resulted in null value.");
                         return;
                     }
                     try {
                         container.setProperty((String)id, o);
                     } catch (Exception e) {
                         MessageDialog.openError(null, "Error", "Error in Neo service: " + e.getMessage());
                     }
                     updateIndexes(root,container, (String)id,oldValue);
                 } else {
                     // simply set the value
                     try {
                         container.setProperty((String)id, value);
                     } catch (Exception e) {
                         MessageDialog.openError(null, "Error", "Error in Neo service: " + e.getMessage());
                     }
                 }
                 tx.success();
                 updateLayer();
                 updateStatistics(root,container, (String)id,oldValue);
             } finally {
                 tx.finish();
                 NeoServiceProvider.getProvider().commit();
                 updateLayer();
             }
         }
     }
 
 
 
     /**
      * Update statistics.
      *
      * @param container the container
      * @param container 
      * @param id the id
      * @param oldValue the old value
      */
     private void updateStatistics(Node   root, PropertyContainer container, String id, Object oldValue) {
         if (container instanceof Node){
             DatasetService service = NeoServiceFactory.getInstance().getDatasetService();
             if (root!=null){
                 IPropertyHeader stat = PropertyHeader.getPropertyStatistic(root);
                 stat.updateStatistic(service.getNodeType((Node)container).getId(), id, container.getProperty(id, null), oldValue);
             }
         }
     }
 
     /**
      * Update indexes.
      *
      * @param container the container
      * @param propertyName the property name
      * @param oldValue the old value
      */
     private void updateIndexes(Node root,PropertyContainer container, String propertyName,Object oldValue) {
         if (container instanceof Node){
             DatasetService service = NeoServiceFactory.getInstance().getDatasetService();
             if (root!=null){
                IndexManager manager= service.getIndexManader(root);
                 manager.updateIndexes(container,propertyName,oldValue);
             }
         }
     }
 
     /**
      * updates layer.
      */
     private void updateLayer() {
         Node gisNode = NeoUtils.findGisNodeByChild((Node)container);
         NeoCatalogPlugin.getDefault().getLayerManager().sendUpdateMessage(new UpdateLayerEvent(gisNode));
     }
 
 }
