 package org.sonar.plugins.newcpd;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.sonar.api.batch.Sensor;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.resources.InputFile;
 import org.sonar.api.resources.Java;
 import org.sonar.api.resources.JavaFile;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.utils.Logs;
 import org.sonar.duplications.block.Block;
 import org.sonar.duplications.block.BlockChunker;
 import org.sonar.duplications.detector.original.OriginalCloneDetectionAlgorithm;
 import org.sonar.duplications.index.CloneGroup;
 import org.sonar.duplications.index.CloneIndex;
 import org.sonar.duplications.index.ClonePart;
 import org.sonar.duplications.index.PackedMemoryCloneIndex;
 import org.sonar.duplications.java.JavaStatementBuilder;
 import org.sonar.duplications.java.JavaTokenProducer;
 import org.sonar.duplications.statement.Statement;
 import org.sonar.duplications.statement.StatementChunker;
 import org.sonar.duplications.token.TokenChunker;
 import org.sonar.duplications.token.TokenQueue;
 
 import com.google.common.collect.Lists;
 
 public class CpdSensor implements Sensor {
 
   private static int BLOCK_SIZE = 13;
 
   public boolean shouldExecuteOnProject(Project project) {
     if (!Java.INSTANCE.equals(project.getLanguage())) {
       Logs.INFO.info("{} not supported", project.getLanguage());
       return false;
     }
     if (isSkipped(project)) {
       Logs.INFO.info("Skipped");
       return false;
     }
     return true;
   }
 
   private boolean isSkipped(Project project) {
     Configuration conf = project.getConfiguration();
     return conf.getBoolean("sonar.newcpd.skip", false);
   }
 
   public void analyse(Project project, SensorContext context) {
     List<InputFile> inputFiles = project.getFileSystem().mainFiles(project.getLanguageKey());
     if (inputFiles.isEmpty()) {
       return;
     }
 
     CloneIndex index = new PackedMemoryCloneIndex();
 
     TokenChunker tokenChunker = JavaTokenProducer.build();
     StatementChunker statementChunker = JavaStatementBuilder.build();
     BlockChunker blockChunker = new BlockChunker(BLOCK_SIZE);
 
     for (InputFile inputFile : inputFiles) {
       File file = inputFile.getFile();
       TokenQueue tokenQueue = tokenChunker.chunk(file);
       List<Statement> statements = statementChunker.chunk(tokenQueue);
       Resource resource = getResource(inputFile);
       List<Block> blocks = blockChunker.chunk(resource.getKey(), statements);
       for (Block block : blocks) {
         index.insert(block);
       }
     }
 
     for (InputFile inputFile : inputFiles) {
       Resource resource = getResource(inputFile);
 
       List<Block> fileBlocks = Lists.newArrayList(index.getByResourceId(resource.getKey()));
       List<CloneGroup> clones = OriginalCloneDetectionAlgorithm.detect(index, fileBlocks);

      DuplicationsData data = new DuplicationsData();
      for (CloneGroup clone : clones) {
        poplulateData(data, clone);
       }
      data.save(context, resource);
     }
   }
 
   private void poplulateData(DuplicationsData data, CloneGroup clone) {
     ClonePart origin = clone.getOriginPart();
     int originLines = origin.getLineEnd() - origin.getLineStart() + 1;
 
     data.incrementDuplicatedBlock();
     for (ClonePart part : clone.getCloneParts()) {
       if (part.equals(origin)) {
         continue;
       }
       data.cumulate(part.getResourceId(), part.getLineStart(), origin.getLineStart(), originLines);
 
       if (part.getResourceId().equals(origin.getResourceId())) {
         data.incrementDuplicatedBlock();
         data.cumulate(origin.getResourceId(), origin.getLineStart(), part.getLineStart(), originLines);
       }
     }
   }
 
   private Resource getResource(InputFile inputFile) {
     return JavaFile.fromRelativePath(inputFile.getRelativePath(), false);
   }
 
   private static final class DuplicationsData {
 
     protected Set<Integer> duplicatedLines = new HashSet<Integer>();
     protected double duplicatedBlocks = 0;
     private List<XmlEntry> duplicationXMLEntries = new ArrayList<XmlEntry>();
 
     private static final class XmlEntry {
       protected StringBuilder xml;
       protected int startLine;
       protected int lines;
 
       private XmlEntry(int startLine, int lines, StringBuilder xml) {
         this.xml = xml;
         this.startLine = startLine;
         this.lines = lines;
       }
     }
 
     private DuplicationsData() {
     }
 
     protected void cumulate(String targetResource, int targetDuplicationStartLine, int duplicationStartLine, int duplicatedLines) {
       StringBuilder xml = new StringBuilder();
       xml.append("<duplication lines=\"").append(duplicatedLines).append("\" start=\"").append(duplicationStartLine)
           .append("\" target-start=\"").append(targetDuplicationStartLine).append("\" target-resource=\"")
           .append(targetResource).append("\"/>");
 
       duplicationXMLEntries.add(new XmlEntry(duplicationStartLine, duplicatedLines, xml));
 
       for (int duplicatedLine = duplicationStartLine; duplicatedLine < duplicationStartLine + duplicatedLines; duplicatedLine++) {
         this.duplicatedLines.add(duplicatedLine);
       }
     }
 
     protected void incrementDuplicatedBlock() {
       duplicatedBlocks++;
     }
 
     protected void save(SensorContext context, Resource resource) {
       context.saveMeasure(resource, CoreMetrics.DUPLICATED_FILES, 1d);
       context.saveMeasure(resource, CoreMetrics.DUPLICATED_LINES, (double) duplicatedLines.size());
       context.saveMeasure(resource, CoreMetrics.DUPLICATED_BLOCKS, duplicatedBlocks);
       context.saveMeasure(resource, new Measure(CoreMetrics.DUPLICATIONS_DATA, getDuplicationXMLData()));
     }
 
     private String getDuplicationXMLData() {
       StringBuilder duplicationXML = new StringBuilder("<duplications>");
 
       Comparator<XmlEntry> comp = new Comparator<XmlEntry>() {
         public int compare(XmlEntry o1, XmlEntry o2) {
           if (o1.startLine == o2.startLine) {
             return o2.lines - o1.lines;
           }
           return o1.startLine - o2.startLine;
         }
       };
       Collections.sort(duplicationXMLEntries, comp);
 
       for (XmlEntry xmlEntry : duplicationXMLEntries) {
         duplicationXML.append(xmlEntry.xml);
       }
       duplicationXML.append("</duplications>");
       return duplicationXML.toString();
     }
   }
 
 }
