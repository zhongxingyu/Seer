 /*
  * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
  * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
  * 
  * MONGKIE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * MONGKIE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mongkie.importer.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 import kobic.prefuse.data.GraphFactory;
 import kobic.prefuse.data.io.ReaderFactory;
 import org.mongkie.importer.*;
 import org.mongkie.importer.spi.Importer.OptionUI;
 import org.mongkie.importer.spi.Importer.SettingUI;
 import org.mongkie.importer.spi.Importer.WizardUI;
 import org.mongkie.importer.spi.*;
 import org.mongkie.util.io.FileType;
 import org.mongkie.visualization.VisualizationController;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 import org.openide.util.lookup.ServiceProvider;
 import prefuse.data.Graph;
 import prefuse.data.Table;
 import prefuse.data.Tuple;
 import prefuse.data.column.Column;
 import prefuse.data.io.CSVTableReader;
 import prefuse.data.util.Index;
 import prefuse.util.DataLib;
 
 /**
  *
  * @author Yeongjun Jang <yjjang@kribb.re.kr>
  */
 @ServiceProvider(service = ImportController.class)
 public class ImportControllerImpl implements ImportController {
 
     @Override
     public <C extends Container> C importFile(InputStream stream, FileImporter<C> importer) {
         //Create Container
         final C container = importer.createContainer();
 
         //Report
         Report report = new Report();
         container.setReport(report);
 
         importer.setInputStream(stream);
 
         try {
             if (importer.execute(container)) {
                 if (importer.getReport() != null) {
                     report.append(importer.getReport());
                 }
                 return container;
             }
         } catch (RuntimeException ex) {
             throw ex;
         } catch (Exception ex) {
             throw new RuntimeException(ex);
         }
         return null;
     }
 
     @Override
     public <I extends FileImporter, B extends FileImporterBuilder<I>> I getFileImporter(Class<B> builderClass, File file) {
         FileObject fileObject = FileUtil.toFileObject(file);
         FileImporterBuilder<I> builder = getMatchingFileImporterBuilder(builderClass, fileObject);
         if (fileObject != null && builder != null) {
             I importer = ImporterPool.getImporter(builder);
             if (fileObject.getPath().startsWith(System.getProperty("java.io.tmpdir"))) {
                 try {
                     fileObject.delete();
                 } catch (IOException ex) {
                     Exceptions.printStackTrace(ex);
                 }
             }
             return importer;
         }
         return null;
     }
 
     private FileImporterBuilder getMatchingFileImporterBuilder(Class<? extends FileImporterBuilder> builderClass, FileObject fileObject) {
         if (fileObject == null) {
             return null;
         }
         for (FileImporterBuilder builder : Lookup.getDefault().lookupAll(builderClass)) {
             if (builder.isMatchingImporter(fileObject)) {
                 return builder;
             }
         }
         return null;
     }
 
     @Override
     public FileType[] getFileTypes(Class<? extends FileImporterBuilder> builderClass) {
         ArrayList<FileType> list = new ArrayList<FileType>();
         for (FileImporterBuilder b : Lookup.getDefault().lookupAll(builderClass)) {
             list.addAll(Arrays.asList(b.getFileTypes()));
         }
         return list.toArray(new FileType[0]);
     }
 
     @Override
     public void process(GraphContainer container) {
         Processor processor = Lookup.getDefault().lookup(Processor.class);
         if (processor == null) {
             throw new RuntimeException(
                     NbBundle.getMessage(ImportControllerImpl.class, "ImportControllerImpl.error.noDefaultProcessor"));
         }
         process(container, processor);
     }
 
     @Override
     public void process(GraphContainer container, Processor processor) {
         processor.setContainer(container);
         processor.setDisplay(Lookup.getDefault().lookup(VisualizationController.class).getDisplay());
         processor.process();
     }
 
     @Override
     public OptionUI getOptionUI(ImporterBuilder builder) {
         for (OptionUI ui : Lookup.getDefault().lookupAll(OptionUI.class)) {
             if (ui.isUIForImporter(builder)) {
                 return ui;
             }
         }
         return null;
     }
 
     @Override
     public SettingUI getSettingUI(ImporterBuilder builder) {
         for (SettingUI ui : Lookup.getDefault().lookupAll(SettingUI.class)) {
             if (ui.isUIForImporter(builder)) {
                 return ui;
             }
         }
         return null;
     }
 
     @Override
     public WizardUI getWizardUI(ImporterBuilder builder) {
         for (WizardUI ui : Lookup.getDefault().lookupAll(WizardUI.class)) {
             if (ui.isUIForImporter(builder)) {
                 return ui;
             }
         }
         return null;
     }
 
     @Override
     public Processor.UI getProcessorUI(Processor processor) {
         for (Processor.UI ui : Lookup.getDefault().lookupAll(Processor.UI.class)) {
             if (ui.isUIForProcessor(processor)) {
                 return ui;
             }
         }
         return null;
     }
 
     @Override
     public <I extends Importer> I getImporter(ImporterBuilder<I> builder) {
         return ImporterPool.getImporter(builder);
     }
 
     @Override
     public <I extends Importer> ImporterBuilder<I> getBuilder(I importer) {
         return ImporterPool.getBuilder(importer);
     }
 
     @Override
     public GraphContainer importCSV(InputStream nodesInputStream, InputStream edgesInputStream,
             String nodeId, String nodeLabel, String sourceId, String targetId, String edgeLabel, boolean directed) {
         return importCSV(nodesInputStream, edgesInputStream, null, true, null, true, nodeId, nodeLabel, sourceId, targetId, edgeLabel, directed);
     }
 
     @Override
     public void importAttributes(InputStream is, Table to, String[] headerNames, boolean hasHeader, boolean multipleValue, String attributeKey, String networkKey) {
         Table attrTable = readTableFromCSV(is, headerNames, hasHeader);
         List<String> attrNames = new ArrayList(Arrays.asList(DataLib.getColumnNames(attrTable)));
         attrNames.remove(attributeKey);
         for (String attrName : attrNames) {
             if (to.getColumn(attrName) == null) {
                 to.addColumn(attrName, attrTable.getColumnType(attrName), null);
             }
         }
         for (Iterator<Tuple> tupleIter = to.tuples(); tupleIter.hasNext();) {
             Tuple ntuple = tupleIter.next();
             Object akey = ntuple.get(networkKey);
             if (akey == null) {
                 continue;
             }
            for (Iterator<Integer> rowIter = DataLib.rows(attrTable, attributeKey, akey); rowIter.hasNext();) {
                 Tuple atuple = attrTable.getTuple(rowIter.next());
                 for (String attrName : attrNames) {
                     String str = null;
                     if (multipleValue && to.getColumnType(attrName) == String.class && (str = ntuple.getString(attrName)) != null && !str.isEmpty()) {
                         ntuple.set(attrName, str + Column.MULTI_VAL_SEPARATOR + atuple.get(attrName));
                     } else {
                         ntuple.set(attrName, atuple.get(attrName));
                     }
                 }
             }
         }
     }
 
     @Override
     public GraphContainer importCSV(InputStream nodesInputStream, InputStream edgesInputStream,
             String[] nodeHeaderNames, boolean hasNodeHeader, String[] edgeHeaderNames, boolean hasEdgeHeader,
             String nodeId, String nodeLabel, String sourceId, String targetId, String edgeLabel, boolean directed) {
         //Create GraphContainer
         GraphContainer container = Lookup.getDefault().lookup(ContainerFactory.class).createGraphContainer();
         container.setDirected(directed);
         //Report
         Report report = new Report();
         container.setReport(report);
         // Read tables
         Table nodeTable = readTableFromCSV(nodesInputStream, nodeHeaderNames, hasNodeHeader);
         Table edgeTable = readTableFromCSV(edgesInputStream, edgeHeaderNames, hasEdgeHeader);
         Map<String, Integer> nodeRowsById = new HashMap<String, Integer>();
         if (nodeTable == null) {
             container.addProcessorHint(Processor.Hint.NODETABLE_NOT_AVAILABLE);
             report.logIssue(new Issue(NbBundle.getMessage(ImportControllerImpl.class, "ImportControllerImpl.issue.nodeTableSkipped"),
                     Issue.Level.WARNING));
         } else {
             nodeTable = populateNodeRowsByUniqueId(nodeTable, nodeId, report, nodeRowsById);
         }
         if (edgeTable == null) {
             assert nodeTable != null;
             container.addProcessorHint(Processor.Hint.EDGETABLE_NOT_AVAILABLE);
             report.logIssue(new Issue(NbBundle.getMessage(ImportControllerImpl.class, "ImportControllerImpl.issue.edgeTableSkipped"),
                     Issue.Level.WARNING));
             container.setResult(GraphFactory.create(nodeTable, null, nodeLabel, directed));
         } else {
             container.setResult(
                     createGraphAddingEdgeKeys(nodeTable, nodeLabel, edgeTable, sourceId, targetId, edgeLabel, nodeRowsById, report, directed));
         }
         return container;
     }
 
     private Table populateNodeRowsByUniqueId(Table origTable, String nodeId, Report report, Map<String, Integer> rowsById) {
         Table nodeTable = origTable.getSchema().instantiate();
         Map<Object, List<Tuple>> tuplesById = origTable.groupBy(nodeId);
         for (List<Tuple> tuples : tuplesById.values()) {
             assert !tuples.isEmpty();
             if (tuples.size() > 1) {
                 report.logIssue(new Issue(NbBundle.getMessage(
                         ImportControllerImpl.class, "ImportControllerImpl.issue.duplicatedNodeId", tuples.get(0).get(nodeId)),
                         Issue.Level.SEVERE));
             } else {
                 Tuple node = tuples.get(0);
                 String nid = node.get(nodeId).toString();
                 rowsById.put(nid, nodeTable.addTuple(node).getRow());
             }
         }
         return nodeTable;
     }
 
     private Graph createGraphAddingEdgeKeys(Table nodeTable, String nodeLabel, Table edgeTable, String sourceId, String targetId, String edgeLabel,
             Map<String, Integer> nodeRowsById, Report report, boolean directed) {
         String sourceKey = Graph.DEFAULT_SOURCE_KEY;
         String targetKey = Graph.DEFAULT_TARGET_KEY;
         edgeTable.addColumn(sourceKey, int.class, -1);
         edgeTable.addColumn(targetKey, int.class, -1);
         if (nodeTable == null) {
             report.logIssue(new Issue(NbBundle.getMessage(ImportControllerImpl.class, "ImportControllerImpl.issue.nodeTableCreated"),
                     Issue.Level.WARNING));
             nodeLabel = Graph.DEFAULT_LABEL_COLUMN;
             nodeTable = new Table();
             nodeTable.addColumn(nodeLabel, String.class, null);
             Map<String, Integer> id2Row = new HashMap<String, Integer>();
             for (Iterator<Tuple> edgeIter = edgeTable.tuples(); edgeIter.hasNext();) {
                 Tuple e = edgeIter.next();
                 String sid = e.get(sourceId).toString();
                 String tid = e.get(targetId).toString();
                 if (id2Row.containsKey(sid)) {
                     e.setInt(sourceKey, id2Row.get(sid));
                 } else {
                     int srow = nodeTable.addRow();
                     nodeTable.set(srow, nodeLabel, sid);
                     e.setInt(sourceKey, srow);
                     id2Row.put(sid, srow);
                 }
                 if (id2Row.containsKey(tid)) {
                     e.setInt(targetKey, id2Row.get(tid));
                 } else {
                     int trow = nodeTable.addRow();
                     nodeTable.set(trow, nodeLabel, tid);
                     e.setInt(targetKey, trow);
                     id2Row.put(tid, trow);
                 }
             }
         } else {
             for (Tuple e : edgeTable.toArray()) {
                 String sid = e.get(sourceId).toString();
                 String tid = e.get(targetId).toString();
                 if (nodeRowsById.containsKey(sid) && nodeRowsById.containsKey(tid)) {
                     e.setInt(sourceKey, nodeRowsById.get(sid));
                     e.setInt(targetKey, nodeRowsById.get(tid));
                 } else {
                     edgeTable.removeTuple(e);
                 }
             }
         }
         return GraphFactory.create(nodeTable, edgeTable, null, nodeLabel, sourceKey, targetKey, edgeLabel, directed);
     }
 
     private Table readTableFromCSV(InputStream in, String[] headerNames, boolean hasHeader) {
         if (in == null) {
             return null;
         }
         CSVTableReader reader = ReaderFactory.createCSVTableReader();
         reader.setHasHeader(hasHeader);
         if (headerNames != null) {
             reader.setHeaderNames(Arrays.asList(headerNames));
         }
         try {
             return reader.readTable(in);
         } catch (RuntimeException ex) {
             throw ex;
         } catch (Exception ex) {
             throw new RuntimeException(ex);
         }
     }
 
     private static class ImporterPool {
 
         private static final Map<ImporterBuilder, Importer> importers = new HashMap<ImporterBuilder, Importer>();
         private static final Map<Importer, ImporterBuilder> builders = new HashMap<Importer, ImporterBuilder>();
 
         static {
             lookupResultChanged();
         }
 
         private static void lookupResultChanged() {
             for (ImporterBuilder builder : Lookup.getDefault().lookupAll(ImporterBuilder.class)) {
                 if (importers.containsKey(builder)) {
                     continue;
                 }
                 Importer importer = builder.buildImporter();
                 importers.put(builder, importer);
                 builders.put(importer, builder);
             }
         }
 
         private static <I extends Importer> I getImporter(ImporterBuilder<I> builder) {
             if (!importers.containsKey(builder)) {
                 lookupResultChanged();
             }
             return (I) importers.get(builder);
         }
 
         private static <I extends Importer> ImporterBuilder<I> getBuilder(I importer) {
             if (!builders.containsKey(importer)) {
                 lookupResultChanged();
             }
             return builders.get(importer);
         }
     }
 }
