 package uk.ac.cam.db538.dexter;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import lombok.val;
 
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;
 
 import uk.ac.cam.db538.dexter.apk.Apk;
 import uk.ac.cam.db538.dexter.dex.Dex;
 import uk.ac.cam.db538.dexter.dex.DexClass;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.builder.HierarchyBuilder;
 import uk.ac.cam.db538.dexter.transform.Transform;
 import uk.ac.cam.db538.dexter.transform.taint.AuxiliaryDex;
 import uk.ac.cam.db538.dexter.transform.taint.TaintTransform;
 
 import com.android.dx.ssa.Optimizer;
 import com.rx201.dx.translator.DexCodeGeneration;
 
 
 public class MainTest {
 
     private static void dumpAnnotation(File apkFile) {
         val out = new ByteArrayAnnotatedOutput();
         out.enableAnnotations(80, true);
 
         DexFile outFile;
         try {
             outFile = new DexFile(apkFile);
             outFile.place();
             outFile.writeTo(out);
             out.writeAnnotationsTo(new FileWriter("annot_original.txt"));
         } catch (IOException e1) {
             e1.printStackTrace();
         }
     }
 
 //    private static void writeToJar(Dex dex, File targetFile) {
 //        final ByteArrayInputStream newDex = new ByteArrayInputStream(
 //                dex.writeToFile());
 //        byte[] buffer = new byte[16 * 1024];
 //
 //        System.out.println("Creating JAR");
 //        try {
 //            targetFile.delete();
 //            ZipOutputStream jarStream = new ZipOutputStream(
 //                    new FileOutputStream(targetFile));
 //            ZipEntry entry = new ZipEntry("classes.dex");
 //            jarStream.putNextEntry(entry);
 //            int len;
 //            while ((len = newDex.read(buffer)) > 0) {
 //                jarStream.write(buffer, 0, len);
 //            }
 //            jarStream.closeEntry();
 //            jarStream.close();
 //        } catch (ZipException e) {
 //            e.printStackTrace();
 //        } catch (IOException e) {
 //            e.printStackTrace();
 //        }
 //    }
 
     public static void main(String[] args) throws IOException {
         long epoch = System.currentTimeMillis();
        if (args.length != 2 && args.length != 3) {
             System.err.println("usage: dexter <framework-dir> <apk-file> [<destination-apk> [method ID]]>");
             System.exit(1);
         }
 
         val apkFile = new File(args[1]);
         if (!apkFile.isFile()) {
             System.err.println("<apk-file> is not a file");
             System.exit(1);
         }
 
         String methodId = null;
         File apkFile_new = new File(apkFile.getAbsolutePath() + "_new.apk");
         
         if (args.length >= 3) {
             apkFile_new = new File(args[2]);
         }
         if (args.length >= 4) {
             methodId = args[3];
         }
 
         // dumpAnnotation(apkFile);
 
         val frameworkDir = new File(args[0]);
         if (!frameworkDir.isDirectory()) {
             System.err.println("<framework-dir> is not a directory");
             System.exit(1);
         }
 
         File frameworkCache = new File(frameworkDir, "hierarchy.cache");
 
         HierarchyBuilder hierarchyBuilder = null;
         try {
             if (frameworkCache.exists()) {
                 System.out.println("Loading framework from cache.");
                 hierarchyBuilder = HierarchyBuilder.deserialize(frameworkCache);
             }
         } catch (Exception e) {}
         if (hierarchyBuilder == null) {
             System.out.println("Scanning framework");
             hierarchyBuilder = new HierarchyBuilder();
             hierarchyBuilder.importFrameworkFolder(frameworkDir);
             hierarchyBuilder.serialize(frameworkCache);
         }
         long hierarchyTime = System.currentTimeMillis() - epoch;
 
         System.out.println("Scanning application");
         val fileApp = new DexFile(apkFile);
         val fileAux = new DexFile("dexter_aux/build/libs/dexter_aux.dex");
 
         System.out.println("Building hierarchy");
         val buildData = hierarchyBuilder.buildAgainstApp(fileApp, fileAux);
         val hierarchy = buildData.getValA();
         val renamerAux = buildData.getValB();
 
         System.out.println("Parsing application");
         AuxiliaryDex dexAux = new AuxiliaryDex(fileAux, hierarchy, renamerAux);
        Dex dexApp = new Dex(fileApp, hierarchy, dexAux, null, null);
 
         DexCodeGeneration.DEBUG = false;
         Optimizer.DEBUG_SSA_DUMP = false;
         Transform transform = new TaintTransform();
         if (args.length >= 3) {
             System.out.println("Instrumenting application");
             dexApp.setTransform(transform);
         } else {
 //            dexApp.setTransform(transform);
         }
 
         if (methodId != null) {
             int arrowIndex = methodId.indexOf("->");
             assert (arrowIndex > 0);
             
             String className = methodId.substring(0, arrowIndex);
             BaseClassDefinition clazzDef = hierarchy.getClassDefinition(DexClassType.parse(className, dexApp.getTypeCache()));
             DexClass clazz = dexApp.getClass(clazzDef);
             DexCodeGeneration.debugMethod = dexApp.findMethodByDefStr(methodId, clazz);
             DexCodeGeneration.INFO = DexCodeGeneration.DEBUG = false;
         }
  
 //        writeToJar(dexApp, apkFile_new);
        Apk.produceAPK(apkFile, apkFile_new, null, dexApp.writeToFile());
 
         long analysisTime = DexCodeGeneration.totalAnalysisTime;
         long translationTime = DexCodeGeneration.totalCGTime;
         long compileTime = DexCodeGeneration.totalDxTime;
         long totalTime = System.currentTimeMillis() - epoch;
 
         System.out.println("===1=== Hierarchy:" + hierarchyTime +
                            ", Analyze:" + analysisTime +
                            ", Translate:" + translationTime +
                            ", Compile:" + compileTime +
                            ", Total:" + totalTime);
     }
 
 }
