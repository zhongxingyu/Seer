 package gutenberg.workers;
 
 import gutenberg.blocs.ManifestType;
 
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Bundles {    
     
     public Bundles(Config config) throws Exception {
         vaultPath = config.getPath(Resource.vault);
         bundlePath = config.getPath(Resource.bundles);
     }
     
     /**
      * Creates a bundle
      * 
      * @param examinerId - id of person creating question
      * @return Manifest
      * @throws Exception
      */
     public ManifestType create(String bundleId) throws Exception {
         Path newBundle = bundlePath.resolve(String.format("%s.zip", bundleId));
         final URI uri = URI.create("jar:file:" + newBundle.toUri().getPath());
        
         final Map<String, String> env = new HashMap<>();
         env.put("create", "true");
         FileSystems.newFileSystem(uri, env);
         
         ManifestType manifest = new ManifestType();
         manifest.setRoot(bundleId);
         
         return manifest;
     }
     
     public ManifestType addTo(String bundleId, String[] questions) throws Exception {
         Path zipfile = bundlePath.resolve(String.format("%s.zip", bundleId));
         if (Files.notExists(zipfile)) create(bundleId);
         
         try (FileSystem fs = FileSystems.newFileSystem(zipfile, null)) {
             
             final Path root = fs.getPath("/");
             String manifestXML = String.format("%s.xml", bundleId);
            Path zipManifest = vaultPath.resolve(manifestXML);
             PrintWriter pw = new PrintWriter(zipManifest.toFile());
             
             if (Files.exists(root.resolve(manifestXML))) {
                 List<String> lines = Files.readAllLines(root.resolve(manifestXML), 
                     StandardCharsets.UTF_8);
                 // Copy existing manifest
                 pw.println(lines.get(0));
                 boolean skip = false;
                 for (int i = 1; i < lines.size()-1; i++) {
                     for (String question : questions) {
                         if (lines.get(i).contains(question.split("\\|")[0])) {
                             skip = true;
                             break;
                         }
                     }
                     if (!skip)
                         pw.println(lines.get(i));
                 }
             } else {
                 // Start new manifest
                 pw.println("<qbundle tag=\"" + bundleId + "\">");
             }
             
             Path srcDir, destDir = null;
             for (String question : questions) {
                 String path = question.split("\\|")[0], label = question.split("\\|")[1];
                
                 // Create question dir
                 destDir = fs.getPath(root.toString(), path.replace('/', '-'));
                 if (Files.notExists(destDir)) Files.createDirectory(destDir);
                 
                 srcDir = vaultPath.resolve(path);
                 try (DirectoryStream<Path> stream = 
                     Files.newDirectoryStream(srcDir, "*.svg")) {
                     for (Path entry: stream) {
                         Files.copy(entry, destDir.resolve(entry.getFileName().toString()), 
                             StandardCopyOption.REPLACE_EXISTING);
                     }
                     Files.copy(srcDir.resolve(QSN_XML), destDir.resolve(QSN_XML), 
                         StandardCopyOption.REPLACE_EXISTING);
                }
                 pw.println(String.format("    <question tag=\"%s\" label=\"%s\"/>", path, label));
                System.out.println("Processed " + srcDir.resolve(QSN_XML).toString());
             }
             pw.println("</qbundle>");
             pw.close();
             
             Files.copy(zipManifest, root.resolve(manifestXML),
                 StandardCopyOption.REPLACE_EXISTING);
             Files.delete(zipManifest);
         }
         ManifestType manifest = new ManifestType();
         manifest.setRoot(bundleId);
         
         return manifest;
     }
     
     public void removeFrom(String bundleId, String[] questionIds) {
         
     }
     
     Path vaultPath, bundlePath;
    final String QSN_XML = "question.xml";
 
 }
