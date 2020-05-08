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
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 
 import org.amanzi.awe.views.network.proxy.NeoNode;
import org.amanzi.neo.loader.core.LoaderUtils;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.statistic.IPropertyHeader;
 import org.amanzi.neo.services.statistic.PropertyHeader;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.graphdb.traversal.Uniqueness;
 import org.neo4j.helpers.Predicate;
 import org.neo4j.kernel.Traversal;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 /**
  * <p>
  * Export network sector
  * </p>
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class ExportNetworkAction extends Action {
 
     Node root = null;
 
     /**
      * @param selection
      */
     public ExportNetworkAction(IStructuredSelection selection) {
         setText("Export network sector data in csv file");
         if (selection.size() == 1) {
             Object elem = selection.getFirstElement();
             if (elem instanceof NeoNode) {
                 Node node = ((NeoNode)elem).getNode();
                 if (NodeTypes.NETWORK.checkNode(node)) {
                     root = node;
                 }
             }
         }
         setEnabled(root != null);
     }
 
     @Override
     public void run() {
         Shell shell = new Shell(Display.getDefault());
         IPropertyHeader stat = PropertyHeader.getPropertyStatistic(root);
         DatasetService datasetService = NeoServiceFactory.getInstance().getDatasetService();
         String[] strtypes = datasetService.getSructureTypesId(root);
         List<String> headers = new ArrayList<String>();
         for (int i = 1; i < strtypes.length; i++) {
             headers.add(strtypes[i]);
         }
         FileDialog dialog = new FileDialog(shell, SWT.SAVE /* or SAVE or MULTI */);
         String fileSelected = dialog.open();
        
         HashMap<String, Collection<String>> propertyMap = new HashMap<String, Collection<String>>();
 
         TraversalDescription descr = Traversal.description().depthFirst().uniqueness(Uniqueness.NONE).relationships(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)
                 .filter(Traversal.returnAllButStartNode());
         for (Path path : descr.traverse(root)) {
             Node node = path.endNode();
             INodeType type = datasetService.getNodeType(node);
             if (type != null && headers.contains(type.getId())) {
                 Collection<String> coll = propertyMap.get(type.getId());
                 if (coll == null) {
                     coll = new TreeSet<String>();
                     propertyMap.put(type.getId(), coll);
                 }
                 for (String propertyName : node.getPropertyKeys()) {
                     if ("type".equals(propertyName)){
                         continue;
                     }
                     coll.add(propertyName);
                 }
             }
         }
         descr = descr.filter(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path paramT) {
                 return !paramT.endNode().hasRelationship(GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING);
             }
         });
         Iterator<Path> iter = descr.traverse(root).iterator();
         List<String> fields = new ArrayList<String>();
         if (fileSelected != null) {
             try {
                // Kasnitskij_V:
                String extention = LoaderUtils.getFileExtension(fileSelected);
                if (extention.equals("")) {
                    fileSelected += ".csv";
                }
                else if (extention.equals(".")) {
                    fileSelected += "csv";
                }
                
                 CSVWriter writer = new CSVWriter(new FileWriter(fileSelected));
                 try {
                     for (String headerType : headers) {
                         Collection<String> propertyCol = propertyMap.get(headerType);
                         if (propertyCol != null) {
                             for (String propertyName : propertyCol) {
                                 fields.add(new StringBuilder(headerType).append("_").append(propertyName).toString());
                             }
                         }
                     }
                     writer.writeNext(fields.toArray(new String[0]));
                     while (iter.hasNext()) {
                         fields.clear();
                         Path path = iter.next();
                         int order = 1;
                         for (Node node : path.nodes()) {
                             INodeType nodeType = datasetService.getNodeType(node);
                             if (nodeType == NodeTypes.NETWORK) {
                                 continue;
                             }
                             while (order < strtypes.length && !strtypes[order++].equals(nodeType.getId())) {
                                 Collection<String> propertyCol = propertyMap.get(strtypes[order - 1]);
                                 if (propertyCol != null) {
                                     for (@SuppressWarnings("unused")
                                     String propertyName : propertyCol) {
                                         fields.add("");
                                     }
                                 }
                             }
                             if (order > strtypes.length) {
                                 fields.add("ERROR - incorrect node structure ");
                                 break;
                             } else {
                                 Collection<String> propertyCol = propertyMap.get(nodeType.getId());
                                 if (propertyCol != null) {
                                     for (String propertyName : propertyCol) {
                                         fields.add(String.valueOf(node.getProperty(propertyName, "")));
                                     }
                                 }
                             }
                         }
                         writer.writeNext(fields.toArray(new String[0]));
                     }
                 } finally {
                     writer.close();
                 }
             } catch (IOException e) {
                 // TODO Handle IOException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
         }
     }
 }
