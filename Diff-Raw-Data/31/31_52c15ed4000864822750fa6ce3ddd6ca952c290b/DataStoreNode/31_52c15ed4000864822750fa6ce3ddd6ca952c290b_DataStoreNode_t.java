 /**
  * 
  */
 package org.geoserver.web.data.tree;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.FeatureTypeInfo;
 
 class DataStoreNode extends AbstractCatalogNode {
     
     boolean unconfiguredChildrenVisible;
 
     public DataStoreNode(String id, AbstractCatalogNode parent) {
         super(id, parent);
     }
 
     protected List<AbstractCatalogNode> buildChildNodes() {
         List<FeatureTypeInfo> types = getCatalog().getFeatureTypesByStore(
                 getModel());
         List<AbstractCatalogNode> childNodes = new ArrayList<AbstractCatalogNode>();
         for (FeatureTypeInfo type : types) {
             childNodes.add(new ResourceNode(type.getName(), this,
                     FeatureTypeInfo.class));
         }
         if(!unconfiguredChildrenVisible) {      
             childNodes.add(new UnconfiguredFeatureTypesNode(name, this));
         } else {
             childNodes.addAll(buildUnconfiguredChildren());
         }
         return childNodes;
     }
 
     private List<AbstractCatalogNode> buildUnconfiguredChildren() {
         List<AbstractCatalogNode> result = new ArrayList<AbstractCatalogNode>();
         List<FeatureTypeInfo> types = getCatalog().getFeatureTypesByStore(
                 getModel());
         try {
             Set<String> typeNames = new HashSet<String>(Arrays.asList(getModel().getDataStore(null).getTypeNames()));
             for (FeatureTypeInfo type : types) {
                 typeNames.remove(type.getName());
             }
             SelectionState state = selectionState != SelectionState.PARTIAL ? selectionState : SelectionState.UNSELECTED;
             for (String typeName : typeNames) {
                 UnconfiguredFeatureTypeNode node = new UnconfiguredFeatureTypeNode(name, typeName, this);
                 node.setSelectionState(state);
                 result.add(node);
             }
             Collections.sort(result);
         } catch(IOException e) {
             LOGGER.log(Level.SEVERE, "Problem occurred while computing unconfigured elements");
         }
         return result;
     }
 
     @Override
     protected DataStoreInfo getModel() {
         return getCatalog().getDataStoreByName(name);
     }
 
     @Override
     protected String getNodeLabel() {
         return getModel().getName();
     }
 
     public void setUnconfiguredChildrenVisible(boolean unconfiguredChildren) {
         this.unconfiguredChildrenVisible = unconfiguredChildren;
         if(childNodes != null) {
             if(childNodes.get(childNodes.size() - 1) instanceof UnconfiguredFeatureTypesNode)
                 childNodes.remove(childNodes.size() - 1);
             childNodes.addAll(buildUnconfiguredChildren());
             checkPartialSelection();
         }
     }
 }
