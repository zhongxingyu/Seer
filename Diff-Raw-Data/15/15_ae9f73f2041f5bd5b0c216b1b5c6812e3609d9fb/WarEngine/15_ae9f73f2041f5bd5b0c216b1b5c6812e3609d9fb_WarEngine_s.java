 package com.oneandone.lavendel.publisher;
 
 import com.oneandone.lavendel.filter.Lavendelizer;
 import com.oneandone.lavendel.index.Index;
 import com.oneandone.lavendel.index.Label;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.io.Buffer;
 
 import java.io.FileInputStream;
 import java.io.IOException;
import java.io.InputStream;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 /**
  * Drives the war publishing process: Extracts resources from a war files to FileStorage and creates a new war file with Lavendelizer
  * configured. Main class of this module, used by Cli and the Publisher plugin.
  */
 public class WarEngine {
     private final Log log;
     private final FileNode inputWar;
     private final FileNode outputWar;
     private final Map<String, Distributor> storages;
     private final FileNode outputWebXmlFile;
     private final Index outputIndex;
     private final FileNode outputNodesFile;
     private final String nodes;
 
     public WarEngine(Log log, FileNode inputWar, FileNode outputWar, Distributor lavendelStorage, FileNode outputWebXmlFile,
                      Index outputIndex, FileNode outputNodesFile, String nodes) {
         this(log, inputWar, outputWar, defaultStorage(lavendelStorage), outputWebXmlFile, outputIndex, outputNodesFile, nodes);
     }
 
     private static Map<String, Distributor> defaultStorage(Distributor lavendelStorage) {
         Map<String, Distributor> storages;
 
         storages = new HashMap<>();
         storages.put(Extractor.DEFAULT_STORAGE, lavendelStorage);
         return storages;
     }
 
     /**
      * @param inputWar
      *            the existing original WAR file
      * @param outputWar
      *            the file where the updated WAR file is saved to
      * @param storages
      * @param outputIndex
      *            Index of the resources for this war
      * @param outputNodesFile
      *            the file where the nodes file is saved to
      * @param nodes
      *            the lavendel nodes. Each URI must contain the scheme (http or https), hostname, optional port, and
      *            optional path. The collection must contain separate URIs for http and https.
      */
     public WarEngine(Log log, FileNode inputWar, FileNode outputWar, Map<String, Distributor> storages, FileNode outputWebXmlFile,
                      Index outputIndex, FileNode outputNodesFile, String nodes) {
         this.log = log;
         this.inputWar = inputWar;
         this.outputWar = outputWar;
         this.storages = storages;
         this.outputWebXmlFile = outputWebXmlFile;
         this.outputIndex = outputIndex;
         this.outputNodesFile = outputNodesFile;
         this.nodes = nodes;
     }
 
     /**
      * Lavendelizes the WAR file and publishes resources.
      *
      * @throws IOException
      */
     public void run() throws IOException {
         long started;
         List<Extractor> extractors;
         Index index;
         long changed;
 
         started = System.currentTimeMillis();
         extractors = Extractor.fromWar(log, inputWar);
         changed = extract(extractors);
         for (Map.Entry<String, Distributor> entry : storages.entrySet()) {
             index = entry.getValue().close();
             //  TODO
             if (!entry.getKey().contains("flash") && index != null /* for tests */) {
                 for (Label label : index) {
                     outputIndex.add(label);
                 }
             }
         }
         outputNodesFile.writeString(nodes);
         mergeWebXmlFile();
         updateWarFile();
         log.info("done: " + changed + "/" + outputIndex.size() + " files changed (" + (System.currentTimeMillis() - started) + " ms)");
     }
 
     public long extract(Extractor... extractors) throws IOException {
         return extract(Arrays.asList(extractors));
     }
 
     public long extract(List<Extractor> extractors) throws IOException {
         Distributor storage;
         long changed;
 
         changed = 0;
         for (Extractor extractor : extractors) {
             storage = storages.get(extractor.getStorage());
             if (storage == null) {
                 throw new IllegalStateException("storage not found: " + extractor.getStorage());
             }
             changed += extractor.run(storage);
         }
         return changed;
     }
 
     private void mergeWebXmlFile() throws IOException {
         ZipInputStream zin = new ZipInputStream(new FileInputStream(inputWar.toPath().toFile()));
 
         ZipEntry entry = zin.getNextEntry();
         while (entry != null) {
             if ("WEB-INF/web.xml".equals(entry.getName())) {
                 outputWebXmlFile.getWorld().getBuffer().copy(zin, outputWebXmlFile);
                 break;
             }
             entry = zin.getNextEntry();
         }
         zin.close();
 
         String endTag = "</web-app>";
         String filterString = "";
         filterString += "\n<filter>\n";
         filterString += "  <filter-name>Lavendelizer</filter-name>\n";
         filterString += "  <filter-class>com.oneandone.lavendel.filter.Lavendelizer</filter-class>\n";
         filterString += "</filter>\n";
         filterString += "<filter-mapping>\n";
         filterString += "  <filter-name>Lavendelizer</filter-name>\n";
         filterString += "  <url-pattern>/*</url-pattern>\n";
         filterString += "</filter-mapping>\n";
 
         String webXmlContent = outputWebXmlFile.readString();
         webXmlContent = webXmlContent.replace(endTag, filterString + endTag);
         outputWebXmlFile.writeString(webXmlContent);
     }
 
     private void updateWarFile() throws IOException {
         ZipInputStream zin = new ZipInputStream(new FileInputStream(inputWar.toPath().toFile()));
         ZipOutputStream out = new ZipOutputStream(outputWar.createOutputStream());
         ZipEntry entry;
         Buffer buffer;
         String name;
         boolean log4j;
 
         log4j = false;
         entry = zin.getNextEntry();
         buffer = outputWar.getWorld().getBuffer();
         while (entry != null) {
             name = entry.getName();
             if (!"WEB-INF/web.xml".equals(name)) {
                 ZipEntry outEntry = new ZipEntry(name);
                 out.putNextEntry(outEntry);
                 buffer.copy(zin, out);
                 out.closeEntry();
             }
             if (name.matches("WEB-INF/lib/lavendelizer.*jar")) {
                 throw new IOException("hard-coded lavandelizer.jar found in war file: " + entry.getName());
             }
             if (name.matches("WEB-INF/lib/log4j.*jar")) {
                 log4j = true;
             }
 
             entry = zin.getNextEntry();
         }
         zin.close();
 
         if (!log4j) {
             throw new IOException("missing log4j.jar in war file");
         }
 
         ZipEntry indexEntry = new ZipEntry(Lavendelizer.LAVENDEL_IDX.substring(1));
         out.putNextEntry(indexEntry);
         outputIndex.save(out);
         out.closeEntry();
 
         ZipEntry nodesEntry = new ZipEntry(Lavendelizer.LAVENDEL_NODES.substring(1));
         out.putNextEntry(nodesEntry);
         outputNodesFile.writeTo(out);
         out.closeEntry();
 
         ZipEntry webXmlEntry = new ZipEntry("WEB-INF/web.xml");
         out.putNextEntry(webXmlEntry);
         outputWebXmlFile.writeTo(out);
         out.closeEntry();
 
        addLavendelizer(out);
         out.close();
     }
 
    private void addLavendelizer(ZipOutputStream out) throws IOException {
        ZipEntry entry;
        InputStream src;

        entry = new ZipEntry("WEB-INF/lib/lavendelizer.jar");
        out.putNextEntry(entry);
        src = getClass().getResourceAsStream("/lavendelizer.jar");
        outputWar.getWorld().getBuffer().copy(src, out);
        src.close();
        out.closeEntry();
    }
     //--
 
     public static Log createNullLog() {
         return new Log() {
             @Override
             public void warn(String str) {
             }
 
             @Override
             public void info(String str) {
             }
 
             @Override
             public void verbose(String str) {
             }
         };
     }
 }
