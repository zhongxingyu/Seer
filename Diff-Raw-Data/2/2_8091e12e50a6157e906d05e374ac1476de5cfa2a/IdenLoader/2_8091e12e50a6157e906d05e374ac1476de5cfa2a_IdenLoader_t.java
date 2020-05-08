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
 
 package org.amanzi.neo.loader;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.OssType;
 import org.amanzi.neo.core.enums.SectorIdentificationType;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.index.lucene.LuceneIndexService;
 
 /**
  * Loader for iDEN Performance Counters 
  * 
  * @author Saelenchits_N
  * @since 1.0.0
  */
 public class IdenLoader extends AbstractLoader {
     private static boolean needParceHeader = true;
 
     private Node ossRoot;
     private Node fileNode;
     private Node lastChild;
     
     private LuceneIndexService luceneService;
     
     private String luceneIndexName;
 
     /**
      * Constructor
      * 
      * @param directory
      * @param datasetName
      * @param display
      */
     public IdenLoader(String directory, String datasetName, Display display) {
         initialize("iDEN", null, directory, display);
         basename = datasetName;
         needParceHeader = true;
         
         getHeaderMap(1);
         
         initializeLucene();
     }
     
     private void initializeLucene() {
         luceneService = NeoServiceProvider.getProvider().getIndexService();
         luceneIndexName = null;
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
         return ossRoot;
     }
     
     @Override
     protected String getPrymaryType(Integer key) {
         return NodeTypes.M.getId();
     }
     @Override
     protected boolean needParceHeaders() {
         if (needParceHeader) {
             needParceHeader = false;
             return true;
         }
         return false;
     }
 
     @Override
     protected void parseLine(String line) {
         if (fileNode == null) {
             ossRoot = LoaderUtils.findOrCreateOSSNode(OssType.iDEN, basename, neo);
            luceneIndexName = NeoUtils.getLuceneIndexKeyByProperty(ossRoot, INeoConstants.SECTOR_ID_PROPERTIES, NodeTypes.M);
             Pair<Boolean, Node> fileNodePair = NeoUtils.findOrCreateFileNode(neo, ossRoot,new File(basename).getName(), new File(basename).getName());
             fileNode = fileNodePair.getRight();
             lastChild = null;
         }
         List<String> fields = splitLine(line);
         if (fields.size() < 2)
             return;
         if (this.isOverLimit())
             return;
         Map<String, Object> lineData = makeDataMap(fields);
         saveStatistic(lineData);
     }
     
     /**
      * @param lineData
      */
     private void saveStatistic(Map<String, Object> lineData) {
         Transaction transaction = neo.beginTx();
         try {
             Node node = neo.createNode();
             NodeTypes.M.setNodeType(node, neo);
             for (Map.Entry<String, Object> entry : lineData.entrySet()) {
                 node.setProperty(entry.getKey(), entry.getValue());
                 
                 if (entry.getKey().equals("cell_name")) {
                     luceneService.index(node, luceneIndexName, entry.getValue());
                 }
             }
             node.setProperty(INeoConstants.PROPERTY_NAME_NAME, "iDEN counter");
             index(node);
             
             NeoUtils.addChild(fileNode, node, lastChild, neo);            
             lastChild = node;
             storingProperties.values().iterator().next().incSaved();
         } finally {
             transaction.finish();
         }
     }
 
     @Override
     public Node[] getRootNodes() {
         return new Node[]{ossRoot};
     }
     
     @Override
     public void finishUp() {
         getStoringNode(1).setProperty(INeoConstants.SECTOR_ID_TYPE, SectorIdentificationType.NAME.toString());
         super.finishUp();
     }
 }
