 package net.bodz.bas.c.loader.scan;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.*;
 import java.util.Map.Entry;
 
 import net.bodz.bas.c.java.io.FileData;
 import net.bodz.bas.c.java.util.Collections;
 import net.bodz.bas.c.loader.ClassLoaders;
 import net.bodz.bas.c.loader.ClassResource;
 import net.bodz.bas.c.m2.MavenProjectOrigin;
 import net.bodz.bas.c.m2.MavenTestClassLoader;
 import net.bodz.bas.c.type.TypeParam;
 import net.bodz.bas.log.Logger;
 import net.bodz.bas.log.LoggerFactory;
 import net.bodz.bas.meta.codegen.ExcludedFromIndex;
 import net.bodz.bas.meta.codegen.IndexedType;
 
 @IndexedType
 public abstract class TypeCollector<T> {
 
     static Logger logger = LoggerFactory.getLogger(TypeCollector.class);
 
     final Class<?> baseClass;
     boolean includeAbstract = true;
     boolean includeNonPublic = false;
 
     ClassScanner scanner;
     List<String> includePackages = new ArrayList<String>();
     List<String> excludePackages = new ArrayList<String>();
     boolean scanned;
 
     boolean showPaths;
     List<Class<?>> extensions;
     Map<File, List<String>> fileContentMap;
     boolean deleteEmptyFiles = true;
 
     public TypeCollector() {
         this(null, null);
     }
 
     public TypeCollector(Class<?> baseClass, Boolean global) {
         if (global == null)
             global = getClass().getName().contains(".uber.");
 
         URLClassLoader mainLoader = (URLClassLoader) ClassLoaders.getRuntimeClassLoader();
         if (!global) {
             mainLoader = new URLClassLoader(new URL[] { //
                     ClassResource.getRootURL(getClass()), //
                     });
         }
 
         URLClassLoader testLoader = MavenTestClassLoader.createMavenTestClassLoader(mainLoader);
 
         scanner = createClassScanner();
         scanner.setClassLoader(testLoader);
 
         if (baseClass != null)
             this.baseClass = baseClass;
         else
             this.baseClass = TypeParam.infer1(getClass(), TypeCollector.class, 0);
     }
 
     protected ClassScanner createClassScanner() {
         return new ClassScanner();
     }
 
     public boolean isShowPaths() {
         return showPaths;
     }
 
     public void setShowPaths(boolean showPaths) {
         this.showPaths = showPaths;
     }
 
     public boolean isIncludeAbstract() {
         return includeAbstract;
     }
 
     public void setIncludeAbstract(boolean includeAbstract) {
         this.includeAbstract = includeAbstract;
     }
 
     public boolean isIncludeNonPublic() {
         return includeNonPublic;
     }
 
     public void setIncludeNonPublic(boolean includeNonPublic) {
         this.includeNonPublic = includeNonPublic;
     }
 
     public ClassScanner getScanner() {
         return scanner;
     }
 
     public void includePackageToScan(String packageName) {
         includePackages.add(packageName);
     }
 
     public void excludePackageToScan(String packageName) {
         excludePackages.remove(packageName);
     }
 
     protected void scanTypes()
             throws IOException {
         for (String packageName : includePackages)
             scanner.scan(packageName);
     }
 
     protected void createExtensionFiles() {
         IndexedType indexing = baseClass.getAnnotation(IndexedType.class);
 
         logger.info("For " + baseClass.getCanonicalName());
 
         for (Class<?> extensionClass : getScannedExtensions(baseClass)) {
             logger.info("    Extension: " + extensionClass.getCanonicalName());
 
             if (indexing == null) {
                 logger.debug("        (Not indexed-type, skipped)");
                 continue;
             }
 
             extensions.add(extensionClass);
 
             int mod = extensionClass.getModifiers();
             if (Modifier.isAbstract(mod)) {
                 if (indexing.includeAbstract())
                     logger.debug("    (Included abstract class)");
                 else
                     continue;
             }
 
             MavenProjectOrigin pomDir = MavenProjectOrigin.fromClass(extensionClass);
             File resdir = pomDir.getResourceDir(extensionClass);
             if (resdir == null)
                 continue;
 
             String publishPrefix = indexing.publishDir();
            if (!publishPrefix.endsWith("/"))
                 publishPrefix += "/";
 
             File sfile = new File(resdir, publishPrefix + baseClass.getName());
 
             List<String> lines = fileContentMap.get(sfile);
             if (lines == null) {
                 lines = new ArrayList<String>();
                 fileContentMap.put(sfile, lines);
             }
 
             if (indexing.obsoleted()) {
                 // lines.add("# " + extension.getName());
             } else {
                 lines.add(extensionClass.getName());
             }
         }
     }
 
     protected Collection<Class<?>> getScannedExtensions(Class<?> base) {
         List<Class<?>> list = new ArrayList<Class<?>>();
 
         for (Class<?> derivation : scanner.getDerivations(base)) {
             int mod = derivation.getModifiers();
             if (Modifier.isAbstract(mod))
                 if (!includeAbstract)
                     continue;
             if (!Modifier.isPublic(mod))
                 if (!includeNonPublic)
                     continue;
             // defined in code-block, or inner class with-in enclosing instance.
             if (derivation.isAnonymousClass() || derivation.isLocalClass() || derivation.isMemberClass())
                 continue;
             if (derivation.isAnnotationPresent(ExcludedFromIndex.class))
                 continue;
             list.add(derivation);
         }
         return list;
     }
 
     protected void saveFiles(Map<File, List<String>> fileContentMap)
             throws IOException {
         for (Entry<File, List<String>> entry : fileContentMap.entrySet()) {
             File file = entry.getKey();
 
             List<String> lines = entry.getValue();
             Collections.sort(lines);
 
             if (lines.isEmpty() && deleteEmptyFiles) {
                 if (file.delete())
                     logger.info("Deleted " + file);
                 continue;
             }
 
             StringBuilder buf = new StringBuilder(lines.size() * 100);
             for (String line : new LinkedHashSet<String>(lines)) {
                 buf.append(line);
                 buf.append('\n');
             }
             String content = buf.toString();
 
             // The same?
             if (file.exists() == Boolean.TRUE) {
                 String old = FileData.readString(file);
                 if (content.equals(old))
                     continue;
             }
 
             try {
                 file.getParentFile().mkdirs();
                 FileData.writeString(file, content);
                 logger.info("Updated " + file);
             } catch (IOException e) {
                 logger.error("Failed to update " + file, e);
             }
         }
     }
 
     public void collect()
             throws IOException {
         TypeCollector<T> wired = this;
         wired._collect();
     }
 
     synchronized void _collect()
             throws IOException {
         fileContentMap = new HashMap<File, List<String>>();
         extensions = new ArrayList<>();
         scanTypes();
         createExtensionFiles();
         saveFiles(fileContentMap);
         if (showPaths)
             for (Class<?> extension : extensions) {
                 MavenProjectOrigin pomDir = MavenProjectOrigin.fromClass(extension);
                 File sourceFile = pomDir.getSourceFile(extension);
                 System.out.println(sourceFile);
             }
         fileContentMap = null;
         extensions = null;
     }
 
 }
