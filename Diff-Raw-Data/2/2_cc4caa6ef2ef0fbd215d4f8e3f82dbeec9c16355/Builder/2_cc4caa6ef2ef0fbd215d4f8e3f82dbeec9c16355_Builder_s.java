 package org.renjin.cran;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Maps;
 import com.google.common.io.Files;
 
 import freemarker.template.TemplateException;
 
 /**
  * Program that will retrieve package sources from CRAN,
  * build, and report results.
  */
 public class Builder {
 
   private File outputDir;
   private Map<String, PackageNode> nodes = Maps.newHashMap();
 
   public static void main(String[] args) throws IOException, TemplateException {
     
     Builder builder = new Builder();
     builder.outputDir = new File(args[0]);
     builder.outputDir.mkdirs();
 
     if(args.length > 1 && args[1].equals("unpack")) {
       builder.unpack();
     }
     builder.buildNodes();
     builder.buildPackages();
 
   }
   
   private void unpack() throws IOException {
 
     // download package index
     File packageIndex = new File(outputDir, "index.html");
     if(!packageIndex.exists()) {
       CRAN.fetchPackageIndex(packageIndex);
     }
     List<CranPackage> cranPackages = CRAN.parsePackageList(
         Files.newInputStreamSupplier(packageIndex));
 
     for(CranPackage pkg : cranPackages) {
       System.out.println(pkg.getName());
       String pkgName = pkg.getName().trim();
       if(!Strings.isNullOrEmpty(pkgName)) {
         File pkgRoot = new File(outputDir, pkgName);
         CRAN.unpackSources(pkg, pkgRoot);
       }
     }
   }
 
   /**
    * Build the list of package nodes from the package
    * directories present.
    */
   private void buildNodes() throws IOException {
 
     for(File dir : outputDir.listFiles()) {
      if(dir.isDirectory() && dir.getName().equals("00buildlogs")) {
         try {
           PackageNode node = new PackageNode(dir);
           nodes.put(node.getName(), node);
         } catch(Exception e) {
           System.err.println("Error building POM for " + dir.getName());
           e.printStackTrace(System.err);
         }
       }
     }
   }
   
   private void buildPackages() throws IOException, TemplateException {
         
     File reportDir = new File(outputDir, "00buildlogs");
     reportDir = new File(reportDir, System.getProperty("BUILD_NUMBER"));
     reportDir.mkdirs();
     
     Reporter reporter = new Reporter(reportDir);
     
     PackageGraph graph = new PackageGraph(nodes);
     List<PackageNode> buildOrder = graph.sortTopologically();
     
     int count = 0;
     for(PackageNode node : buildOrder) {
       System.out.println("Building " + node + "...");
       node.writePom();
       boolean succeeded = node.build(reporter.getPackageReporter(node));
       if(succeeded) { 
         System.out.println("SUCCESS");
       } else {
         System.out.println("FAILURE");
       }
       count++;
       
       if(count > 10) {
         break;
       }
     }
     
     reporter.writeIndex();
   }
 }
