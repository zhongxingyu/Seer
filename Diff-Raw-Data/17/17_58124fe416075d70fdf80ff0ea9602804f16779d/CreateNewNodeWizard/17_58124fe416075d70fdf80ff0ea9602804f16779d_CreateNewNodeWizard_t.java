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
 
 package org.amanzi.awe.views.network.view;
 
 import java.util.List;
 
 import org.amanzi.awe.catalog.neo.NeoCatalogPlugin;
 import org.amanzi.awe.catalog.neo.upd_layers.events.UpdateLayerEvent;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.utils.EditPropertiesPage.PropertyWrapper;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.IndexManager;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.events.UpdateDrillDownEvent;
 import org.amanzi.neo.services.statistic.IPropertyHeader;
 import org.amanzi.neo.services.statistic.PropertyHeader;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 
 /**
  * <p>
  * Wizard for creating new network nodes
  * </p>
  * 
  * @author NiCK
  * @since 1.0.0
  */
 public class CreateNewNodeWizard extends Wizard implements INewWizard {
 
     private final INodeType iNodeType;
     private CreateNewNodeWizardPage page;
     private final Node sourceNode;
 
     /**
      * @param iNodeType
      * @param sourceNode
      */
     public CreateNewNodeWizard(INodeType iNodeType, Node sourceNode) {
         this.iNodeType = iNodeType;
         this.sourceNode = sourceNode;
 
     }
 
     @Override
     public boolean performFinish() {
         Node network = NeoUtils.getParentNode(sourceNode, NodeTypes.NETWORK.getId());
 
         Node parentnode;
         DatasetService ds = NeoServiceFactory.getInstance().getDatasetService();
         GraphDatabaseService service = NeoServiceProviderUi.getProvider().getService();
         boolean needCheckStructure = true;
         if (ds.getNodeType(sourceNode).equals(iNodeType)) {
             parentnode = NeoUtils.getParent(service, sourceNode);
             needCheckStructure = false;
         } else {
             parentnode = sourceNode;
         }
 
         // Creating new node
        Node targetNode=null;
         Transaction tx = service.beginTx();
         try {
            targetNode = service.createNode();
             targetNode.setProperty("type", iNodeType.getId());
             parentnode.createRelationshipTo(targetNode, NetworkRelationshipTypes.CHILD);
 
             IPropertyHeader ph = PropertyHeader.getPropertyStatistic(network);
             List<PropertyWrapper> properties = page.getProperties();
             IndexManager indexManader = ds.getIndexManager(ds.findRootByChild(targetNode));
             for (PropertyWrapper propertyWrapper : properties) {
                 targetNode.setProperty(propertyWrapper.getName(), propertyWrapper.getParsedValue());
                 ph.updateStatistic(iNodeType.getId(), propertyWrapper.getName(), propertyWrapper.getParsedValue(), null);
                 indexManader.updateIndexes(targetNode, propertyWrapper.getName(), null);
             }
 
             tx.success();
         } catch (Exception e) {
             e.printStackTrace();
             tx.failure();
         } finally {
             tx.finish();
         }
 
         if (needCheckStructure) {
             // Change structure
             INodeType sourceType = ds.getNodeType(sourceNode);
 
             List<INodeType> structureTypes = ds.getSructureTypes(sourceNode);
 
             List<INodeType> userDefTypes = ds.getUserDefinedNodeTypes();
             userDefTypes.removeAll(structureTypes);
 
             if (userDefTypes.contains(iNodeType)) {
                 String[] newStructureTypes = new String[structureTypes.size() + 1];
                 int i = 0;
                 for (INodeType type : structureTypes) {
                     newStructureTypes[i++] = type.getId();
                     if (type.equals(sourceType)) {
                         newStructureTypes[i++] = iNodeType.getId();
                     }
                 }
                 ds.setStructure(network, newStructureTypes);
             }
         }
 
         NeoServiceProviderUi.getProvider().commit();
 
         // TODO imho need to use service for updating layer
         Node gisNode = NeoUtils.findGisNodeByChild(sourceNode);
         NeoCatalogPlugin.getDefault().getLayerManager().sendUpdateMessage(new UpdateLayerEvent(gisNode));
         // TODO imho need to use service for updating Tree View
        NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(new UpdateDrillDownEvent(targetNode, "org.amanzi.neo.wizards.CreateNetworkWizard"));
 
         return true;
     }
 
     @Override
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         DatasetService ds = NeoServiceFactory.getInstance().getDatasetService();
         if (ds.getNodeType(sourceNode).equals(iNodeType)) {
             setWindowTitle("Copy node");
             page = new CreateNewNodeWizardPage("Copy node", "Copy " + iNodeType.getId(), iNodeType, sourceNode);
             addPage(page);
         } else {
             setWindowTitle("Create new node");
             page = new CreateNewNodeWizardPage("Create new node", "Create new " + iNodeType.getId(), iNodeType, sourceNode);
             addPage(page);
         }
     }
 
 }
