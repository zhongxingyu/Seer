 package uk.ac.cam.db538.dexter.dex;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.CodeItem;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;
 
 import uk.ac.cam.db538.dexter.ProgressCallback;
 import uk.ac.cam.db538.dexter.dex.type.ClassRenamer;
 import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
 import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 import uk.ac.cam.db538.dexter.transform.taint.AuxiliaryDex;
 import uk.ac.cam.db538.dexter.utils.Utils;
 
 public class Dex {
 
     @Getter final RuntimeHierarchy hierarchy;
     @Getter final AuxiliaryDex auxiliaryDex;
     @Getter private List<DexClass> classes;
 
     private final ProgressCallback progressCallback;
 
 //  @Getter private DexClass externalStaticFieldTaint_Class;
 //  @Getter private DexMethodWithCode externalStaticFieldTaint_Clinit;
 
     /*
      * Creates an empty Dex
      */
     public Dex(RuntimeHierarchy hierarchy, AuxiliaryDex dexAux, ProgressCallback progressCallback) {
         this.hierarchy = hierarchy;
         this.auxiliaryDex = dexAux;
         this.progressCallback = progressCallback;
         this.classes = Collections.emptyList();
     }
 
     /*
      * Creates a new Dex and parses all classes inside the given DexFile
      */
     public Dex(DexFile dex, RuntimeHierarchy hierarchy, AuxiliaryDex dexAux, ProgressCallback progressCallback) {
         this(hierarchy, dexAux, progressCallback);
 
         val dexClsInfos = dex.ClassDefsSection.getItems();
         int clsCount = dexClsInfos.size();
         int i = 0;
         progressUpdate(0, clsCount);
         List<DexClass> classes = new ArrayList<DexClass>(clsCount);
         for (val dexClsInfo : dexClsInfos) {
             classes.add(new DexClass(this, dexClsInfo));
             progressUpdate(++i, clsCount);
         }
         
         sortClassesByName(classes);
         this.classes = Utils.finalList(classes);
     }
     
     private static void sortClassesByName(List<DexClass> classes) {
         Collections.sort(classes, new Comparator<DexClass>() {
 			@Override
 			public int compare(DexClass o1, DexClass o2) {
				if (o1.getClassDef().getType().getDescriptor().equals("Lcom/inmobi/adtracker/androidsdk/dck/jnc/brn;"))
 					return -1;
				else if (o2.getClassDef().getType().getDescriptor().equals("Lcom/inmobi/adtracker/androidsdk/dck/jnc/brn;"))
 					return 1;
  
 				String d1 = o1.getClassDef().getType().getDescriptor();
 				String d2 = o2.getClassDef().getType().getDescriptor();
 				return d1.compareTo(d2);
 			}
 		});
     }
 
     public Dex(DexFile dex, RuntimeHierarchy hierarchy, AuxiliaryDex dexAux) {
         this(dex, hierarchy, dexAux, (ProgressCallback) null);
     }
 
     /*
      * This constructor applies a descriptor renamer on the classes parsed from given DexFile
      */
     public Dex(DexFile dex, RuntimeHierarchy hierarchy, AuxiliaryDex dexAux, ClassRenamer renamer, ProgressCallback progressCallback) {
         this(dex, setRenamer(hierarchy, renamer), dexAux, progressCallback);
         unsetRenamer(hierarchy);
     }
 
     public Dex(DexFile dex, RuntimeHierarchy hierarchy, AuxiliaryDex dexAux, ClassRenamer renamer) {
         this(dex, hierarchy, dexAux, renamer, null);
     }
 
     private static RuntimeHierarchy setRenamer(RuntimeHierarchy hierarchy, ClassRenamer renamer) {
         hierarchy.getTypeCache().setClassRenamer(renamer);
         return hierarchy;
     }
 
     private static void unsetRenamer(RuntimeHierarchy hierarchy) {
         hierarchy.getTypeCache().setClassRenamer(null);
     }
 
     public DexTypeCache getTypeCache() {
         return hierarchy.getTypeCache();
     }
 
     private void progressUpdate(int finished, int outOf) {
         if (this.progressCallback != null)
             this.progressCallback.update(finished, outOf);
     }
 
     public DexClass getClass(BaseClassDefinition classDef) {
         for (DexClass clazz : classes)
             if (clazz.getClassDef().equals(classDef))
                 return clazz;
         return null;
     }
 
     public byte[] writeToFile() {
         val outFile = new DexFile();
         val out = new ByteArrayAnnotatedOutput();
 
         int i = 0;
         int count = classes.size();
         progressUpdate(0, count);
         val asmCache = new DexAssemblingCache(outFile, hierarchy);
         for (val cls : classes) {
             cls.writeToFile(outFile, asmCache);
             progressUpdate(++i, count);
         }
 
         // Apply jumbo-instruction fix requires ReferencedItem being
         // placed first, after which the code needs to be placed again
         // because jumbo instruction is wider.
         // The second pass shoudn't change ReferencedItem's placement
         // (because they are ordered deterministically by its content)
         // otherwise we'll be in trouble.
         outFile.place();
         fixInstructions(outFile);
         outFile.place();
         outFile.writeTo(out);
 
         val bytes = out.toByteArray();
 
         DexFile.calcSignature(bytes);
         DexFile.calcChecksum(bytes);
 
         return bytes;
     }
 
     private void fixInstructions(DexFile outFile) {
         for (CodeItem codeItem : outFile.CodeItemsSection.getItems()) {
             codeItem.fixInstructions(true, true);
         }
     }
 
     public void addClasses(Collection<DexClass> cls) {
     	List<DexClass> newClasses = new ArrayList<DexClass>(this.classes.size() + cls.size());
         newClasses.addAll(this.classes);
         newClasses.addAll(cls);
         sortClassesByName(newClasses);
         this.classes = Utils.finalList(newClasses);
     }
 }
