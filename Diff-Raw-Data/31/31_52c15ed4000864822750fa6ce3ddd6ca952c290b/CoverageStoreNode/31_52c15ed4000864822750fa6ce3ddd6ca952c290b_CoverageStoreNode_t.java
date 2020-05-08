 /**
  * 
  */
 package org.geoserver.web.data.tree;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.geoserver.catalog.CoverageInfo;
 import org.geoserver.catalog.CoverageStoreInfo;
 
 class CoverageStoreNode extends AbstractCatalogNode {
 
     public CoverageStoreNode(String id, AbstractCatalogNode parent) {
         super(id, parent);
     }
 
     protected List<AbstractCatalogNode> buildChildNodes() {
         List<CoverageInfo> coverages = getCatalog().getCoveragesByStore(
                 getModel());
         List<AbstractCatalogNode> childNodes = new ArrayList<AbstractCatalogNode>();
         for (CoverageInfo coverage : coverages) {
             childNodes.add(new ResourceNode(coverage.getName(), this,
                     CoverageInfo.class));
         }
         return childNodes;
 
     }
 
     @Override
     protected CoverageStoreInfo getModel() {
         return getCatalog().getCoverageStoreByName(name);
     }
 
     @Override
     protected String getNodeLabel() {
         return getModel().getName();
     }
 }
