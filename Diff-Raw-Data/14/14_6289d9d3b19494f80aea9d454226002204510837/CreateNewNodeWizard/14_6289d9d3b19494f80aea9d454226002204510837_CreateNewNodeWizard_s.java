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
 
 import org.amanzi.neo.core.enums.INodeType;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.EditPropertiesPage.PropertyWrapper;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.statistic.IPropertyHeader;
 import org.amanzi.neo.services.statistic.PropertyHeader;
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
 
     private IWorkbench workbench;
     private IStructuredSelection selection;
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
         try {
             // TODO if SECTOR created need to increeas
 
             // Creating new node
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Transaction tx = service.beginTx();
             try {
                 Node targetNode = service.createNode();
                 targetNode.setProperty("type", iNodeType.getId());
                 sourceNode.createRelationshipTo(targetNode, NetworkRelationshipTypes.CHILD);
                 // NodeTypes type = NodeTypes.getEnumById(iNodeType.getId());
                 // if (type != null) {
                 // IPropertyHeader ph =
                 // PropertyHeader.getPropertyStatistic(NeoUtils.getParentNode(sourceNode,
                 // NodeTypes.NETWORK.getId()));
                 // Map<String, Object> statisticProperties = ph.getStatisticParams(type);
                 // for (String key : statisticProperties.keySet()) {
                 // targetNode.setProperty(key, statisticProperties.get(key));
                 // }
                 // }
                 IPropertyHeader ph = PropertyHeader.getPropertyStatistic(NeoUtils.getParentNode(sourceNode, NodeTypes.NETWORK.getId()));
                 List<PropertyWrapper> properties = page.getProperties();
                 for (PropertyWrapper propertyWrapper : properties) {
                     targetNode.setProperty(propertyWrapper.getName(), propertyWrapper.getParsedValue());
                     ph.updateStatistic(iNodeType.getId(), propertyWrapper.getName(), propertyWrapper.getParsedValue(), null);
                 }

                 tx.success();
             } catch (Exception e) {
                 e.printStackTrace();
                 tx.failure();
             } finally {
                 tx.finish();
             }

             // Change structure
            DatasetService ds = NeoServiceFactory.getInstance().getDatasetService();
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
                 ds.setStructure(NeoUtils.getParentNode(sourceNode, NodeTypes.NETWORK.getId()), newStructureTypes);
             }
 
             NeoServiceProvider.getProvider().commit();
 
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
         System.out.println("\nOK\n");
         return true;
     }
 
     @Override
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         this.workbench = workbench;
         this.selection = selection;
         setWindowTitle("Create new node");
         page = new CreateNewNodeWizardPage("Create new node", "Create new " + iNodeType, iNodeType, sourceNode);
         addPage(page);
     }
 
 }
