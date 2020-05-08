 package uk.ac.cam.db538.dexter.hierarchy.builder;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.DexFile.NoClassesDexException;
 
 import uk.ac.cam.db538.dexter.dex.type.ClassRenamer;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
 import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.HierarchyException;
 import uk.ac.cam.db538.dexter.hierarchy.InstanceFieldDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.InterfaceDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 import uk.ac.cam.db538.dexter.hierarchy.StaticFieldDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.UnresolvedClassDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.UnresolvedInterfaceDefinition;
 import uk.ac.cam.db538.dexter.utils.Pair;
 
 public class HierarchyBuilder implements Serializable {
 
     private ClassDefinition root;
     @Getter private final DexTypeCache typeCache;
 
     private final Map<DexClassType, ClassVariants> definedClasses;
 
     public HierarchyBuilder() {
         root = null;
         typeCache = new DexTypeCache();
         definedClasses = new HashMap<DexClassType, ClassVariants>();
     }
 
     public boolean hasClass(DexClassType clsType) {
         return definedClasses.containsKey(clsType);
     }
 
     public void importDex(File file, boolean isInternal) throws IOException {
         // parse the file
         DexFile dex;
         try {
             dex = new DexFile(file, false, true);
         } catch (NoClassesDexException e) {
             // file does not contain classes.dex
             return;
         }
 
         importDex(dex, isInternal);
 
         // explicitly dispose of the object
         dex = null;
         System.gc();
     }
 
     public void importDex(DexFile dex, boolean isInternal) {
         // recursively scan classes
         for (val cls : dex.ClassDefsSection.getItems())
             scanClass(new DexClassScanner(cls, typeCache), isInternal);
     }
 
     private void scanClass(DexClassScanner clsScanner, boolean isInternal) {
         val clsType = DexClassType.parse(clsScanner.getClassDescriptor(), typeCache);
 
         val baseclsData = new ClassData();
 
         if (clsScanner.isInterface())
             baseclsData.classDef = new InterfaceDefinition(clsType, clsScanner.getAccessFlags(), isInternal);
         else {
             val clsDef = new ClassDefinition(clsType, clsScanner.getAccessFlags(), isInternal);
             baseclsData.classDef = clsDef;
 
             scanInstanceFields(clsScanner, clsDef);
             scanSuperclass(clsScanner, clsDef, baseclsData, isInternal);
         }
 
         scanMethods(clsScanner, baseclsData.classDef);
         scanStaticFields(clsScanner, baseclsData.classDef);
         baseclsData.interfaces = clsScanner.getInterfaces();
 
         // store data
         ClassVariants clsVariants = definedClasses.get(clsType);
         if (clsVariants == null) {
             clsVariants = new ClassVariants();
             definedClasses.put(clsType, clsVariants);
         }
         clsVariants.setVariant(baseclsData, isInternal);
     }
 
     private void foundRoot(ClassDefinition clsInfo, boolean isInternal) {
         // check only one root exists
         if (root != null)
             throw new HierarchyException("More than one hierarchy root found (" + root.getType().getPrettyName() + " vs. " + clsInfo.getType().getPrettyName() + ")");
         else if (isInternal)
             throw new HierarchyException("Hierarchy root cannot be internal");
         else
             root = clsInfo;
     }
 
     private void scanMethods(DexClassScanner clsScanner, BaseClassDefinition baseclsDef) {
         for (val methodScanner : clsScanner.getMethodScanners()) {
             baseclsDef.addDeclaredMethod(
                 new MethodDefinition(
                     baseclsDef,
                     methodScanner.getMethodId(),
                     methodScanner.getAccessFlags()));
         }
     }
 
     private void scanStaticFields(DexClassScanner clsScanner, BaseClassDefinition baseclsDef) {
         for (val fieldScanner : clsScanner.getStaticFieldScanners())
             baseclsDef.addDeclaredStaticField(
                 new StaticFieldDefinition(
                     baseclsDef,
                     fieldScanner.getFieldId(),
                     fieldScanner.getAccessFlags()));
     }
 
     private void scanInstanceFields(DexClassScanner clsScanner, ClassDefinition clsDef) {
         for (val fieldScanner : clsScanner.getInstanceFieldScanners())
             clsDef.addDeclaredInstanceField(
                 new InstanceFieldDefinition(
                     clsDef,
                     fieldScanner.getFieldId(),
                     fieldScanner.getAccessFlags()));
     }
 
     private void scanSuperclass(DexClassScanner clsScanner, ClassDefinition clsDef, ClassData baseclsData, boolean isInternal) {
         // acquire superclass info
         val typeDescriptor = clsScanner.getSuperclassDescriptor();
         if (typeDescriptor != null)
             baseclsData.superclass = DexClassType.parse(typeDescriptor, typeCache);
         else
             foundRoot(clsDef, isInternal);
     }
 
     public RuntimeHierarchy build() {
         val classList = new HashMap<DexClassType, BaseClassDefinition>();
         val unresolvedClassList = new HashMap<DexClassType, BaseClassDefinition>();
         val unresolvedInterfaceList = new HashMap<DexClassType, BaseClassDefinition>();
         
         for (val classDefPair : new ArrayList<ClassVariants>(definedClasses.values())) {
             val clsData = classDefPair.getClassData();
             val baseCls = clsData.classDef;
 
             // connect to parent and vice versa
             val sclsType = (baseCls instanceof ClassDefinition) ? clsData.superclass : root.getType();
             if (sclsType != null) {
                 ClassVariants sclsVariants = definedClasses.get(sclsType);
                 if (sclsVariants == null) {
                     sclsVariants = createUnresolvedClass(sclsType);
                     System.err.println("Class " + baseCls.getType().getPrettyName() + " is missing its parent " + sclsType.getPrettyName());
                     unresolvedClassList.put(sclsType, sclsVariants.getClassData().classDef);
                 }
                 baseCls.setSuperclass(sclsVariants.getClassData().classDef);
             }
 
             // connect to interfaces
             val ifaces = clsData.interfaces;
             if (ifaces != null) {
                 for (val ifaceType : ifaces) {
                     ClassVariants ifaceInfo_Pair = definedClasses.get(ifaceType);
                     if (ifaceInfo_Pair == null) {
                         ifaceInfo_Pair = createUnresolvedInterface(ifaceType);
                         System.err.println("Create dummy interface entry: " + ifaceType.getPrettyName());
                         unresolvedInterfaceList.put(ifaceType, ifaceInfo_Pair.getClassData().classDef);
                     }
                     if (!(ifaceInfo_Pair.getClassData().classDef instanceof InterfaceDefinition))
                         throw new HierarchyException("Class " + baseCls.getType().getPrettyName() + " is missing its interface " + ifaceType.getPrettyName());
                     baseCls.addImplementedInterface((InterfaceDefinition) ifaceInfo_Pair.getClassData().classDef);
                 }
             }
 
             classList.put(baseCls.getType(), baseCls);
         }
 
         if (root == null)
             throw new HierarchyException("Hierarchy is missing a root");
 
         return new RuntimeHierarchy(classList, unresolvedClassList, unresolvedInterfaceList, root, typeCache);
     }
 
     public void removeInternalClasses() {
     	List<Entry<DexClassType, ClassVariants>> classEntries = new ArrayList<Entry<DexClassType, ClassVariants>>(definedClasses.entrySet());
 
         for (Entry<DexClassType, ClassVariants> classEntry : classEntries) {
         	ClassVariants classPair = classEntry.getValue();
             classPair.deleteInternal();
             if (classPair.isEmpty())
                 definedClasses.remove(classEntry.getKey());
         }
     }
 
     private static final FilenameFilter FILTER_DEX_ODEX_JAR = new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
             return name.endsWith(".dex") || name.endsWith(".odex") || name.endsWith(".jar");
         }
     };
 
     private static class ClassVariants implements Serializable {
         private static final long serialVersionUID = 1L;
 
         private ClassData internal;
         private ClassData external;
 
         public ClassVariants() {
             this.internal = this.external = null;
         }
 
         public ClassData getClassData() {
             // prefer internal
             if (internal != null)
                 return internal;
             else if (external != null)
                 return external;
             else
                 throw new HierarchyException("No class data available");
         }
 
         public void setVariant(ClassData cls, boolean isInternal) {
             if (isInternal) {
                 if (this.internal != null)
                     throw new HierarchyException("Multiple definitions of internal class " + this.internal.classDef.getType().getPrettyName());
 
                 this.internal = cls;
             } else {
                 if (this.external != null)
                     throw new HierarchyException("Multiple definitions of external class " + this.external.classDef.getType().getPrettyName());
 
                 this.external = cls;
             }
         }
 
         public void deleteInternal() {
             internal = null;
         }
 
         public boolean isEmpty() {
             return (external == null) && (internal == null);
         }
     }
 
     private static class ClassData implements Serializable {
         private static final long serialVersionUID = 1L;
 
         BaseClassDefinition classDef = null;
 
         DexClassType superclass = null;
         Collection<DexClassType> interfaces = null;
     }
 
     // SERIALIZATION
 
     private static final long serialVersionUID = 1L;
 
     public void serialize(File outputFile) throws IOException {
         val fos = new FileOutputStream(outputFile);
         try {
             val oos = new ObjectOutputStream(new BufferedOutputStream(fos));
             try {
                 oos.writeObject(this);
             } finally {
                 oos.close();
             }
         } finally {
             fos.close();
         }
     }
 
     public static HierarchyBuilder deserialize(File inputFile) throws IOException {
         val fis = new FileInputStream(inputFile);
         try {
             return deserialize(fis);
         } finally {
             fis.close();
         }
     }
 
     public static HierarchyBuilder deserialize(InputStream is) throws IOException {
         val ois = new ObjectInputStream(new BufferedInputStream(is));
         try {
             Object hierarchy;
             try {
                 hierarchy = ois.readObject();
             } catch (ClassNotFoundException ex) {
                 throw new HierarchyException(ex);
             }
 
             if (hierarchy instanceof HierarchyBuilder)
                 return (HierarchyBuilder) hierarchy;
             else
                 throw new HierarchyException("Input file does not contain an instance of HierarchyBuilder");
         } finally {
             ois.close();
         }
     }
 
     // USEFUL SHORTCUTS
 
     public void importFrameworkFolder(File dir) throws IOException {
         String[] files = dir.list(FILTER_DEX_ODEX_JAR);
 
         for (String filename : files)
             importDex(new File(dir, filename), false);
     }
 
     public ClassRenamer importAuxiliaryDex(DexFile dexAux) {
         val classRenamer = new ClassRenamer(dexAux, this);
 
         typeCache.setClassRenamer(classRenamer);
        importDex(dexAux, false);
         typeCache.setClassRenamer(null);
 
         return classRenamer;
     }
 
     public Pair<RuntimeHierarchy, ClassRenamer> buildAgainstApp(DexFile dexApp, DexFile dexAux) {
         try {
             importDex(dexApp, true);
             val classRenamer = importAuxiliaryDex(dexAux);
             val runtimeHierarchy = build();
             return Pair.create(runtimeHierarchy, classRenamer);
         } finally {
             removeInternalClasses();
         }
     }
     
     private ClassVariants createUnresolvedInterface(DexClassType classType) {
         val clsData = new ClassData();
         clsData.classDef =  new UnresolvedInterfaceDefinition(classType);
         ClassVariants clsVariants = new ClassVariants();
         definedClasses.put(classType, clsVariants);
         clsVariants.setVariant(clsData, false);
         return clsVariants;
     }
     
     private ClassVariants createUnresolvedClass(DexClassType classType) {
         val clsData = new ClassData();
         clsData.classDef =  new UnresolvedClassDefinition(classType);
 
         //DexClassType root = DexClassType.parse("Ljava/lang/Object;", typeCache);
         //clsData.classDef.setSuperclassLink(definedClasses.get(root).getClassData().classDef);
         assert root != null;
         clsData.classDef.setSuperclass(root);
         
         ClassVariants clsVariants = new ClassVariants();
         definedClasses.put(classType, clsVariants);
         clsVariants.setVariant(clsData, false);
         return clsVariants;
     }
 }
