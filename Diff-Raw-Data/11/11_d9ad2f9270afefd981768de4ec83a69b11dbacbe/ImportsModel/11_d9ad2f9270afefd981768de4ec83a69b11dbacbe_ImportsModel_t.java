 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.netbeans.modules.javafx.editor.imports;
 
 import java.util.HashSet;
 import java.util.Set;
 import javax.lang.model.element.TypeElement;
 import org.netbeans.api.javafx.source.ElementHandle;
 
 /**
  * This is a collector for all relevant information gathered during scanning for
  * imports. It holds the information about the declared imports, the import usage
  * and also the start and end position of the imports block
  * @author Jaroslav Bachorik
  */
 public class ImportsModel {
     final static public class Unresolved implements Comparable<Unresolved> {
         final private String unresolvedName;
         final private Set<ElementHandle<TypeElement>> options;
         final private long elementPos;
         private String resolved = null;
 
         public Unresolved(long pos, String unresolvedName, Set<ElementHandle<TypeElement>> options) {
             this.unresolvedName = unresolvedName;
             this.options = options;
             this.elementPos = pos;
         }
 
         public Set<ElementHandle<TypeElement>> getOptions() {
             return options;
         }
 
         public String getUnresolvedElement() {
             return unresolvedName;
         }
 
         public String getResolvedName() {
             return resolved;
         }
 
         public void resolve(String element) {
             resolved = element;
         }
 
         public long getElementPos() {
             return elementPos;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final Unresolved other = (Unresolved) obj;
             if (this.unresolvedName != other.unresolvedName && (this.unresolvedName == null || !this.unresolvedName.equals(other.unresolvedName))) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 7;
             hash = 97 * hash + (this.unresolvedName != null ? this.unresolvedName.hashCode() : 0);
             return hash;
         }
 
         public int compareTo(Unresolved o) {
             if (this.getResolvedName() == null && o.getResolvedName() != null) return 1;
             if (this.getResolvedName() != null && o.getResolvedName() == null) return -1;
             if (this.getResolvedName() == null && o.getResolvedName() == null) return 0;
 
             if (this.getResolvedName().startsWith("javafx") && !o.getResolvedName().startsWith("javafx")) return -1;
             if (!this.getResolvedName().startsWith("javafx") && o.getResolvedName().startsWith("javafx")) return 1;
 
             return this.getResolvedName().compareTo(o.getResolvedName());
         }
 
 
     }
     final static public class Declared {
         final private String importName;
         final private long start, end;
 
         public Declared(String importName, long start, long end) {
             this.importName = importName;
             this.start = start;
             this.end = end;
         }
 
         public long getEnd() {
             return end;
         }
 
         public String getImportName() {
             return importName;
         }
 
         public long getStart() {
             return start;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final Declared other = (Declared) obj;
             if ((this.importName == null) ? (other.importName != null) : !this.importName.equals(other.importName)) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 5;
             hash = 67 * hash + (this.importName != null ? this.importName.hashCode() : 0);
             return hash;
         }
 
     }
 
     final private Set<Declared> declaredImports = new HashSet<Declared>();
     final private Set<String> usedImports = new HashSet<String>();
     final private Set<Unresolved> unresolved = new HashSet<Unresolved>();
 
     private long importsStart = Integer.MAX_VALUE;
     private long importsEnd = Integer.MIN_VALUE;
 
     /**
      * Call this method to indicate import declaration
      * @param imprt The import statement
      * @param start The start position in the document
      * @param end The end position in the document
      */
     public void addDeclaredImport(String imprt, long start, long end) {
         importsStart = Math.min(importsStart, start);
         importsEnd = Math.max(importsEnd, end);
 
         declaredImports.add(new Declared(imprt, start, end));
     }
 
     /**
      * Call this method to indicate the usage of a particular type
      * @param className The type class name
      */
     public void addUsage(String className) {
         usedImports.add(className);
     }
 
     /**
      * Call this method to indicate the occurance of an unknown type
      * @param unresolvedName The type name of the unresolved eleemnt
      * @param options The list of types that could be used to resolve the element
      * @param pos The element position in the document
      */
     public void addUnresolved(String unresolvedName, Set<ElementHandle<TypeElement>> options, long pos) {
         unresolved.add(new Unresolved(pos, unresolvedName, options));
     }
 
     /**
      * Calculates the set of unused imports
      * It uses full text matching as well as wildcard matching
      * @return Return a new set of all unused imports
      */
     public Set<Declared> getUnusedImports() {
         Set<Declared> unused = new HashSet<Declared>();
         for(Declared declared : declaredImports) {
             if (declared.importName.endsWith(".*")) {
                int imprtLen = declared.importName.length() - 2;
                 String target = declared.importName.substring(0, imprtLen);
                 boolean found = false;
                 for(String imprt : usedImports) {
                     if (imprt.length() > imprtLen) {
                        if (imprt.indexOf(".", imprtLen + 2) > -1)  continue;
                         if (imprt.substring(0, imprtLen).equals(target)) {
                             found = true;
                             break;
                         }
                    } else {
                        if (imprt.length() == imprtLen && target.equals(imprt)) {
                            found = true;
                            break;
                        }
                     }
                 }
                 if (!found) {
                     unused.add(declared);
                 }
             } else {
                 if (!usedImports.contains(declared.importName)) {
                     unused.add(declared);
                 }
             }
         }
         return unused;
     }
 
     /**
      *
      * @return Return the set of all unresolved types
      */
     public Set<Unresolved> getUnresolved() {
         return unresolved;
     }
 
     /**
      *
      * @return Returns the position in the document where imports end
      */
     public long getImportsEnd() {
         return importsEnd;
     }
 
     /**
      *
      * @return Return the position in the document where imports start
      */
     public long getImportsStart() {
         return importsStart;
     }
 }
