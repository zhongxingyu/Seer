 /* Soot - a J*va Optimization Framework
  * Copyright (C) 2000 Patrice Pominville
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 /*
  * Modified by the Sable Research Group and others 1997-1999.  
  * See the 'credits' file distributed with Soot for the complete list of
  * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
  */
 
 
 package soot;
 import soot.*;
 import soot.options.*;
 
 import soot.coffi.*;
 import java.util.*;
 import java.io.*;
 import soot.util.*;
 import soot.jimple.*;
 import soot.javaToJimple.*;
 
 /** Loads symbols for SootClasses from either class files or jimple files. */
 public class SootResolver 
 {
     public SootResolver (Singletons.Global g) {}
 
     public static SootResolver v() { return G.v().SootResolver();}
     
     private Set markedClasses = new HashSet();
     private LinkedList classesToResolve = new LinkedList();
     private boolean mIsResolving = false;
     private InitialResolver initSourceResolver;
 
    public InitialResolver getInitSourceResolver(){
        if (initSourceResolver == null) {
            initSourceResolver = new InitialResolver();
        }
        return initSourceResolver;
    }
     /** Creates a new SootResolver. */
     //public SootResolver()
     //{
     //}
 
     /** Returns a SootClass object for the given className. 
      * Creates a new context class if needed. */
     public SootClass getResolvedClass(String className)
     {
         if(Scene.v().containsClass(className))
             return Scene.v().getSootClass(className);
 
         SootClass newClass;
         if(mIsResolving) {
             newClass = new SootClass(className);
             Scene.v().addClass(newClass);
         
             markedClasses.add(newClass);
             classesToResolve.addLast(newClass);
         } else {
             newClass = resolveClassAndSupportClasses(className);
         }
         
         return newClass;
     }
 
 
 
     /** Resolves the given className and all dependent classes. */
     public SootClass resolveClassAndSupportClasses(String className)
     {
         mIsResolving = true;
         SootClass resolvedClass = getResolvedClass(className);
        
         while(!classesToResolve.isEmpty()) {
             
             InputStream is = null;
             SootClass sc = (SootClass) classesToResolve.removeFirst();
             className = sc.getName();
            
             try 
             {
                 is = SourceLocator.v().getInputStreamOf(className);
             } catch(ClassNotFoundException e) 
             {
                 if(!Scene.v().allowsPhantomRefs())
                     throw new RuntimeException("couldn't find type: " + className + " (is your soot-class-path set properly?)");
                 else 
                 {
                     G.v().out.println("Warning: " + className + " is a phantom class!");
                     sc.setPhantomClass();
                     continue;
                 }
             }
                 
             Set s = null;
             if(is instanceof ClassInputStream) {
                 if(Options.v().verbose())
                     G.v().out.println("resolving [from .class]: " + className );
                 soot.coffi.Util.v().resolveFromClassFile(sc, is, this, Scene.v());
             } else if(is instanceof JimpleInputStream) {
                 if(Options.v().verbose())
                     G.v().out.println("resolving [from .jimple]: " + className );
                 if(sc == null) throw new RuntimeException("sc is null!!");
                 
                 soot.jimple.parser.JimpleAST jimpAST = new soot.jimple.parser.JimpleAST((JimpleInputStream) is, this);                
                 jimpAST.getSkeleton(sc);
                 JimpleMethodSource mtdSrc = new JimpleMethodSource(jimpAST);
 
                 Iterator mtdIt = sc.methodIterator();
                 while(mtdIt.hasNext()) {
                     SootMethod sm = (SootMethod) mtdIt.next();
                     sm.setSource(mtdSrc);
                 }
                 
                 Iterator it = jimpAST.getCstPool().iterator();                
                 while(it.hasNext()) {
                     String nclass = (String) it.next();
                     assertResolvedClass(nclass);
                 }
                 
             }else if(is instanceof JavaInputStream) {
                 if (Options.v().verbose())
                     G.v().out.println("resolving [from .java]: " + className);
                             
 
                 if (initSourceResolver == null) {
                     initSourceResolver = new soot.javaToJimple.InitialResolver();
                 }
                 initSourceResolver.formAst(SourceLocator.v().getFullPathFound(), SourceLocator.v().getLocationsFound());
                 //System.out.println("about to call initial resolver in j2j: "+sc.getName());
                 initSourceResolver.resolveFromJavaFile(sc, this);
             }
             else {
                 throw new RuntimeException("could not resolve class: " + is+" (is your soot-class-path correct?)");
             }
             try
             {
                 is.close();
             }
             catch (IOException e) { throw new RuntimeException("!?"); }
         }        
         
         mIsResolving = false;
         return resolvedClass;
     }
 
     /** Asserts that type is resolved. */
     public void assertResolvedClassForType(Type type)
     {
         if(type instanceof RefType)
             assertResolvedClass(((RefType) type).getClassName());
         else if(type instanceof ArrayType)
             assertResolvedClassForType(((ArrayType) type).baseType);
     }
     
     /** Asserts that class is resolved. */
     public void assertResolvedClass(String className)
     {
         if(!Scene.v().containsClass(className))
         {
             SootClass newClass = new SootClass(className);
             Scene.v().addClass(newClass);
             
             markedClasses.add(newClass);
             classesToResolve.addLast(newClass);
         }
     }
 }
 
 
