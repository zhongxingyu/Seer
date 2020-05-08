 /*
  * java-gnome, a UI library for writing GTK and GNOME programs from Java!
  *
  * Copyright © 2008-2010 Operational Dynamics Consulting, Pty Ltd
  *
  * The code in this file, and the program it is a part of, is made available
  * to you by its authors as open source software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License version
  * 2 ("GPL") as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
  *
  * You should have received a copy of the GPL along with this program. If not,
  * see http://www.gnu.org/licenses/. The authors of this program may be
  * contacted through http://java-gnome.sourceforge.net/.
  *
  * Linking this library statically or dynamically with other modules is making
  * a combined work based on this library. Thus, the terms and conditions of
  * the GPL cover the whole combination. As a special exception (the
  * "Classpath Exception"), the copyright holders of this library give you
  * permission to link this library with independent modules to produce an
  * executable, regardless of the license terms of these independent modules,
  * and to copy and distribute the resulting executable under terms of your
  * choice, provided that you also meet, for each linked independent module,
  * the terms and conditions of the license of that module. An independent
  * module is a module which is not derived from or based on this library. If
  * you modify this library, you may extend the Classpath Exception to your
  * version of the library, but you are not obligated to do so. If you do not
  * wish to do so, delete this exception statement from your version.
  */
 package org.gnome.vala;
 
 /**
  * Represents a node in the symbol tree.
  * 
  * @author Severin Heiniger
  */
 public class Symbol extends CodeNode
 {
 
     /**
      * The value used for the field {@link #nameSourceReference} to indicate
      * that the source reference could not be determined and that the class
      * should not try again.
      */
     private static final Object NULL_NAME_SOURCE_REFERENCE = new Object();
 
     private Object nameSourceReference = null;
 
     protected Symbol(long pointer) {
         super(pointer);
     }
 
     /**
      * Returns the parent of this symbol.
      */
     public Symbol getParentSymbol() {
         return ValaSymbol.getParentSymbol(this);
     }
 
     /**
      * Returns the symbol name.
      */
     public String getName() {
         return ValaSymbol.getName(this);
     }
 
     /**
      * Sets the symbol name.
      */
     public void setName(String name) {
         ValaSymbol.setName(this, name);
     }
 
     /**
      * Returns the source reference to the symbol name.
      * 
      * TODO make it possible to retrieve the content of the source reference
      * 
      * @return the reference to the symbol name or <code>null</code> if the
      *         name cannot be found in the source code, i.e., because the
      *         symbol is only implicit
      */
     public SourceReference getNameSourceReference() {
         if (nameSourceReference == null) {
             String name = getNameInSourceFile();
             SourceReference sourceReference = getSourceReference();
             SourceFile sourceFile = sourceReference.getSourceFile();
             String referenceContent = sourceReference.getContent();
             SourceLocation begin = sourceReference.getBegin();
             SourceLocation end = sourceReference.getEnd();
 
             if (begin.getLine() != end.getLine()) {
                 throw new IllegalArgumentException("SourceReference must only consist of a single line");
             }
 
             int index = referenceContent.indexOf(name);
             if (index < 0) {
                 nameSourceReference = NULL_NAME_SOURCE_REFERENCE;
             } else {
                 int line = begin.getLine();
                 int columnBegin = begin.getColumn() + index;
                 int columnEnd = columnBegin + name.length() - 1;
                 SourceLocation newBegin = new SourceLocation(line, columnBegin);
                 SourceLocation newEnd = new SourceLocation(line, columnEnd);
                 nameSourceReference = new SourceReference(sourceFile, newBegin, newEnd);
             }
         }
         if (nameSourceReference instanceof SourceReference) {
             return (SourceReference) nameSourceReference;
         } else {
             return null;
         }
     }
 
     /**
      * Whether the reference to the symbol name could be determined.
      */
     public boolean hasNameSourceReference() {
        return nameSourceReference != null;
     }
 
     public String getNameInSourceFile() {
         return getName();
     }
 
     /**
      * Returns the specifies accessibility of this symbol.
      */
     public SymbolAccessibility getAccessibility() {
         return ValaSymbol.getAccessibility(this);
     }
 
     /**
      * Sets accessibility of this symbol.
      */
     public void setAccessibility(SymbolAccessibility accessibility) {
         ValaSymbol.setAccessibility(this, accessibility);
     }
 
     @Override
     public String toString() {
         // Do not bind to vala_symbol_to_string, because it returns
         // a C code comment.
         return getClass().getSimpleName() + "[name=" + getName() + "]";
     }
 
 }
