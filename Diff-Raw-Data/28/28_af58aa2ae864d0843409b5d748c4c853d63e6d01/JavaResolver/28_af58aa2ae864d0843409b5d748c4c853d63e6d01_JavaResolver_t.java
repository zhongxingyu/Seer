 /*
  * JavaResolver.java
  *
  * Copyright (c) 2006-2007 GradSoft  Ukraine
  * All Rights Reserved
  */
 
 
 package ua.gradsoft.javachecker.models;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import ua.gradsoft.javachecker.EntityNotFoundException;
 import ua.gradsoft.javachecker.JUtils;
 import ua.gradsoft.javachecker.Main;
 import ua.gradsoft.javachecker.NotSupportedException;
 import ua.gradsoft.javachecker.util.Pair;
 import ua.gradsoft.termware.Term;
 import ua.gradsoft.termware.TermHelper;
 import ua.gradsoft.termware.TermWareException;
 import ua.gradsoft.termware.TermWareRuntimeException;
 import ua.gradsoft.termware.exceptions.AssertException;
 
 /**
  *Static class
  * @author Ruslan Shevchenko
  */
 public class JavaResolver {
     
     public static JavaTypeModel resolveTypeToModel(Term t,JavaTypeModel where) throws EntityNotFoundException,TermWareException {
         return resolveTypeToModel(t,where,null);
     }
     
     public static JavaTypeModel resolveTypeToModel(Term t,JavaTypeModel where,List<JavaTypeVariableAbstractModel> typeVariables) throws EntityNotFoundException,TermWareException {
         JavaUnitModel unitModel = where.getUnitModel();
         JavaPackageModel packageModel = where.getPackageModel();
         return resolveTypeToModel(t,unitModel,packageModel,where,typeVariables,null);
     }
     
     public static JavaTypeModel resolveTypeToModel(Term t, JavaStatementModel where) throws EntityNotFoundException, TermWareException {
         boolean debug=false;
         JavaTypeModel ownerType=where.getTopLevelBlockModel().getOwnerModel().getTypeModel();
         JavaTopLevelBlockOwnerModel blockOwner=where.getTopLevelBlockModel().getOwnerModel();
         if (debug) {
             LOG.info("blockOwner name = "+blockOwner.getName());
             LOG.info("blockOwner signature = "+JavaTopLevelBlockOwnerModelHelper.getStringSignature(blockOwner));
         }
         List<JavaTypeVariableAbstractModel> typeVariables=blockOwner.getTypeParameters();
         Iterable<JavaTypeModel> localTypes = new LocalTypesIterable(where);
         return resolveTypeToModel(t,ownerType.getUnitModel(),ownerType.getPackageModel(),ownerType,typeVariables,localTypes);
     }
     
     
     public static JavaTypeModel resolveTypeToModel(Term t, JavaUnitModel unitModel, JavaPackageModel packageModel, JavaTypeModel where,List<JavaTypeVariableAbstractModel> typeVariables,Iterable<JavaTypeModel> localTypes) throws EntityNotFoundException,TermWareException {
         boolean printDetails=false;      
         
         if (printDetails) {
             String stv = ((typeVariables==null ? "null" :  typeVariables.toString()));
             LOG.log(Level.INFO,"resolveTypeToModel, t="+TermHelper.termToString(t)+" in "+ (where==null ? "null" : where.getFullName())+" stv="+stv);
             if (where!=null) {
                 if (where.hasTypeParameters()) {
                     LOG.log(Level.FINE,where.getName()+"have type parameters:"+where.getTypeParameters().toString());
                 }
             }
         }
         
         if (t.isAtom()) {
             if (t.getName().equals("boolean")) {
                 return JavaPrimitiveTypeModel.BOOLEAN;
             }else if (t.getName().equals("char")) {
                 return JavaPrimitiveTypeModel.CHAR;
             }else if (t.getName().equals("byte")) {
                 return JavaPrimitiveTypeModel.BYTE;
             }else if (t.getName().equals("short")) {
                 return JavaPrimitiveTypeModel.SHORT;
             }else if (t.getName().equals("int")) {
                 return JavaPrimitiveTypeModel.INT;
             }else if (t.getName().equals("long")) {
                 return JavaPrimitiveTypeModel.LONG;
             }else if (t.getName().equals("float")) {
                 return JavaPrimitiveTypeModel.FLOAT;
             }else if (t.getName().equals("double")) {
                 return JavaPrimitiveTypeModel.DOUBLE;
             }else if (t.getName().equals("void")){
                 return JavaPrimitiveTypeModel.VOID;
             }else{
                 throw new AssertException("Atom type model must be primitive, instead:"+TermHelper.termToString(t));
             }
         }else if (t.isComplexTerm()) {
             if (t.getName().equals("TypeRef")) {
                 // already resolved type is a second argument.
                 Term jt = t.getSubtermAt(1);
                 if (!jt.isJavaObject()) {
                     throw new AssertException("second TypeRef argument must be java object");
                 }
                 Object o = jt.getJavaObject();
                 return (JavaTypeModel)o;
             } else if (t.getName().equals("ReferenceType")) {
                 Term t1=t.getSubtermAt(1);
                 JavaTypeModel tm=resolveTypeToModel(t1,unitModel,packageModel,where,typeVariables,localTypes);
                 int referenceLevel=t.getSubtermAt(0).getInt();
                 while(referenceLevel>0) {
                     tm=new JavaArrayTypeModel(tm,null);
                     --referenceLevel;
                 }
                 return tm;
             }else if (t.getName().equals("Identifier")) {
                 String name=t.getSubtermAt(0).getString();
                 if (where==null) {
                     try {
                       return resolveTypeModelByName(name,unitModel,packageModel,typeVariables);
                     }catch(EntityNotFoundException ex){                        
                         throw new EntityNotFoundException("type",TermHelper.termToString(t),"",JUtils.getFileAndLine(t),ex);                        
                         //ex.setFileAndLine(JUtils.getFileAndLine(t));                        
                         //throw ex;
                     }
                 }else{
                     try {
                       return resolveTypeModelByName(name,where,typeVariables,localTypes);
                     }catch(EntityNotFoundException ex){
                         ex.setFileAndLine(JUtils.getFileAndLine(t));
                         throw ex;
                     }
                 }
             }else if (t.getName().equals("ClassOrInterfaceType")||t.getName().equals("Name")) {
                 //at first, try to find class in our package or imported classes
                 Term head=t.getSubtermAt(0);
                 Term ct=head.getSubtermAt(0); // must be identifier
                 String name=ct.getSubtermAt(0).getName();
                 JavaTypeModel frsTypeModel=null;
                 boolean found=true;
                 try {
                     if (where==null) {
                         frsTypeModel=resolveTypeModelByName(name,unitModel,packageModel, typeVariables);
                     }else{
                         frsTypeModel=resolveTypeModelByName(name,where,typeVariables,localTypes);
                     }
                 }catch(EntityNotFoundException ex){
                     found=false;
                 }
                 if (found) {
                     if (head.getSubtermAt(1).isNil()) {
                         return frsTypeModel;
                     }else{
                         try {
                             return resolveRestOfClassOrInterfaceType(frsTypeModel,head.getSubtermAt(1),unitModel,packageModel,where,typeVariables,localTypes,printDetails);
                         }catch(EntityNotFoundException ex){
                             //System.err.println("failed resolveRestOfClassAndInterfaceType");
                             found=false;
                         }
                     }
                 }
                 // still not found: try to search with package name
                 try {
                     return resolveTypeModelWithFullPackage(t,where,typeVariables);
                 }catch(EntityNotFoundException ex){
                     found=false;
                 }
                 // at least, all
             }else if (t.getName().equals("NestedType")) {
                 Term t1=t.getSubtermAt(0);
                 Term nested=t.getSubtermAt(1);
                 JavaTypeModel tm = resolveTypeToModel(t1, unitModel, packageModel, where, typeVariables, localTypes);
                 return JavaResolver.resolveTypeToModel(nested,tm,typeVariables);
             }else if (t.getName().equals("FullTypeName")) {
                 String packageName = t.getSubtermAt(0).getString();
                 String classShortName = t.getSubtermAt(1).getSubtermAt(0).getString();
                 return JavaResolver.resolveTypeModelFromPackage(classShortName,packageName);
             }else if (t.getName().equals("TypeRef")){
                 Term tpref = t.getSubtermAt(1);
                 Object o = tpref.getJavaObject();
                 if (! (o instanceof JavaTypeModel)) {
                     throw new AssertException("second argument of TypeRef must be JavaTypeModel");
                 }
                 return (JavaTypeModel)o;
             }else{
                 // this is not ReferenceType and not PrimaryType and not ClassOrInterface body
                 throw new AssertException("Unknown type term:"+TermHelper.termToString(t));
             }
         }else{
             throw new AssertException("Primitive non-atom type term:"+TermHelper.termToString(t));
         }
         if (printDetails) {
             System.err.println("failed resolveTypeToModel, t="+TermHelper.termToString(t)+"+ in "+ (where==null ? "null" : where.getName()));
         }
         throw new EntityNotFoundException("type",TermHelper.termToString(t),"");
     }
     
     
     private static JavaTypeModel  resolveRestOfClassOrInterfaceType(JavaTypeModel prevModel, Term t, JavaUnitModel unitModel,JavaPackageModel packageModel,JavaTypeModel where, List<JavaTypeVariableAbstractModel> typeVariables,Iterable<JavaTypeModel> localTypes, boolean debug) throws EntityNotFoundException, TermWareException {
         boolean printDetails=debug;
         
         if (printDetails) {
             LOG.info("resolveRestOfClassOrInterfaceType "+TermHelper.termToString(t)+", prevModel="+prevModel.getName());
             if (where!=null) {
               LOG.info("where="+where.getFullName());
             }else{
               LOG.info("where=null"); 
             }
         }
         JavaTypeModel curModel=prevModel;
         while(!t.isNil()) {
             Term ct=t.getSubtermAt(0);
             if (ct.getName().equals("Identifier")) {
                 String name=ct.getSubtermAt(0).getString();
                 //TODO: think about typeArguments among classOrInterfaceType ?
                 if (curModel.hasNestedTypeModels()) {
                     try {
                         curModel=curModel.findNestedTypeModel(name);
                     }catch(NotSupportedException ex){
                         throw new AssertException("impossible, hasNestedTypeMopdels but getNestedTypeModels throws NotSupported");
                     }
                 }else{
                     if (printDetails) {
                         System.err.println("failed resolveRestOfCalssAndInterfaceType");
                     }
                     throw new EntityNotFoundException("type",t.getSubtermAt(0).getName(),"");
                 }
             }else if(ct.getName().equals("TypeArguments")) {
                 if (printDetails) {
                     System.err.println("creating new ArgumentBoundTypeModel");
                 }
                 if (where!=null) {
                    curModel=new JavaTypeArgumentBoundTypeModel(curModel,ct,where,typeVariables,null);
                 }else{
                     curModel=new JavaTypeArgumentBoundTypeModel(curModel,ct,unitModel,packageModel,typeVariables);
                 }
             }else{
                 throw new AssertException("Only Identifiers or TypeArgumentrs are allowed in ClassOrInterfaceType sequence, we have "+TermHelper.termToString(ct));
             }
             t=t.getSubtermAt(1);
         }
         if (printDetails) {
             System.err.println("resolveRestOfCalssAndInterfaceType succesfull");
         }
         return curModel;
     }
     
     public static JavaTypeModel resolveTypeModelByName(String name, JavaTypeModel where,List<JavaTypeVariableAbstractModel> typeVariables,Iterable<JavaTypeModel> localTypes) throws EntityNotFoundException, TermWareException {
         boolean printDetails=false;
         
        // if (name.equals("TypeCheckerProperties")) {
        //     printDetails=true;
        // }
         
         if (printDetails) {
             String stv;
             if (typeVariables==null) {
                 stv="null";
             }else{
                 stv=typeVariables.toString();
             }
             //System.err.println("!!!resolveTypeModelByName("+name+","+where.toString()+","+stv+")");
             LOG.info("resolveTypeModelByName("+name+","+where.getFullName()+","+stv+")");
         }
         
         //0. may be this is where
         // if (name.equals(where.getName())) {
         //     return where;
         // }
 
         // try to find among type variables.
         if (typeVariables!=null) {
             for(JavaTypeVariableAbstractModel tv: typeVariables) {
                 if (tv.getName().equals(name)) {
                     if (printDetails) {
                       LOG.info("found in type variables");
                     }
                     return tv;
                 }
             }
         }
         
         
         // may be model, where we resolve, have typeVariables
         if (where.hasTypeParameters()) {
             for(JavaTypeVariableAbstractModel tv:where.getTypeParameters()) {
                 if (tv.getName().equals(name)) {
                     if (printDetails) {
                         LOG.info("found in type parameters");
                     }
                     return tv;
                 }
             }
         }
         
         
         
         
         
         //2. try to find among local types
         if (localTypes!=null) {
             for(JavaTypeModel tm: localTypes) {
                 if (tm.getName().equals(name)) {
                     if (printDetails) {
                         LOG.info("found in local types");
                     }
                     return tm;
                 }
             }
         }
         
         //2. try to find subtype of current type.
         if (where.hasNestedTypeModels()) {
             //System.err.println("try to search in nested type models");
             try {
                 return where.findNestedTypeModel(name);
             }catch(EntityNotFoundException ex){
                 
                 ; /* do nothing */
             }catch(NotSupportedException ex){
                 ; /* do nothing */
             }
             //System.err.println("failed");
         }
 
         
         // if current class is nested, try to find next one.
         if (where.isNested()) {
             JavaTypeModel enclosed=where;
             while(enclosed.isNested()) {
                 try {
                     return enclosed.getEnclosedType().findNestedTypeModel(name);
                 }catch(EntityNotFoundException ex){
                     ; /* do nothing */
                 }catch(NotSupportedException ex){
                     ; /* do nothing */
                 }
                 // or may be this is type parameter of enclosed ?
                 if (enclosed.hasTypeParameters()) {
                     for(JavaTypeVariableAbstractModel tv: enclosed.getTypeParameters()) {
                         if (tv.getName().equals(name)) {
                             return tv;
                         }
                     }
                 }
                 
                 
                 try {
                     enclosed=enclosed.getEnclosedType();
                 }catch(NotSupportedException ex){
                     // impossible, becouse encloused is nested.
                     throw new AssertException("getEnclosedType is not supported, when type is nested");
                 }
                 
                 // and try to resolve one in enclosed.
                 try {
                     return resolveTypeModelByName(name, enclosed, typeVariables, localTypes);
                 }catch(EntityNotFoundException ex){
                     // do nothing.
                     ;
                 }
                 
             }
         }
                
         
         
         //3. try to find as nested in super-class
         JavaTypeModel superModel = null;
         try {
             superModel = where.getSuperClass();
         }catch(NotSupportedException ex) {
             ;
         }
         if (superModel!=null) {
             
             while(!superModel.isNull()
             && !(superModel.getFullName().equals("java.lang.Object"))
             && !(superModel.getFullName().equals("java.lang.Enum"))
             ) {
                 if (superModel.hasNestedTypeModels()) {
                     try {
                         if (printDetails) {
                             LOG.log(Level.INFO,"try to find as nested in "+superModel.getFullName());
                         }
                         JavaTypeModel retval = superModel.findNestedTypeModel(name);
                         if (printDetails) {
                             LOG.log(Level.INFO,"found "+retval.getFullName());
                         }
                         return retval;
                     }catch(EntityNotFoundException ex){
                         if (printDetails) {
                             LOG.log(Level.INFO,"not here");
                         }
                         ;
                     }catch(NotSupportedException ex){
                         ;
                     }
                 }
                 try {
                     superModel=superModel.getSuperClass();
                 }catch(NotSupportedException ex){
                     break;
                 }
             }
         }
         
         //or in all supers, include super-interfaces
         //System.err.println("!!!check for nested types of interfaces "+name+" in "+where.getName());
         LinkedList<JavaTypeModel> allSupers=new LinkedList<JavaTypeModel>();
         try {
             allSupers.addAll(where.getSuperInterfaces());
         }catch(NotSupportedException ex){
             ;
         }       
         try {
             superModel = where.getSuperClass();
             allSupers.add(superModel);
         }catch(NotSupportedException ex) {
             ;
         }
         
         while(!allSupers.isEmpty()) {
             JavaTypeModel curr = allSupers.removeFirst();
             if (curr.hasNestedTypeModels()) {
                 try {
                     
                     JavaTypeModel retval = curr.findNestedTypeModel(name);
                     if (printDetails) {
                         LOG.log(Level.INFO,"found "+retval.getFullName()+" in "+where.getName());
                     }
                     return retval;
                 }catch(EntityNotFoundException ex){
                     ;
                 }catch(NotSupportedException ex){
                     ;
                 }
                 if (printDetails) {
                     LOG.log(Level.INFO,"failed search of "+name+" as nested in "+curr.getName());
                 }
             }
             try {
                 allSupers.addAll(curr.getSuperInterfaces());
             }catch(NotSupportedException ex){
                 ;
             }
             
             if (curr.isClass()) {
                 try {
                    superModel = curr.getSuperClass(); 
                    if (!superModel.isNull()) {
                        allSupers.add(superModel);
                    }
                 }catch(NotSupportedException ex){
                     // impossible
                     ;
                 }
                 
             }
                                                     
             if (curr.isNested()) {
                 // add nested type.
                 try {
                     allSupers.add(curr.getEnclosedType());
                 }catch(NotSupportedException ex){
                     // impossible, do nothing.
                 }
             }
         }
         
         
         //now try to find in statement
         if (where.isLocal()||where.isAnonimous()) {
             JavaStatementModel st = where.getEnclosedStatement();
             JavaTypeModel enclosedType = st.getTopLevelBlockModel().getOwnerModel().getTypeModel();
             List<JavaTypeVariableAbstractModel> enclosedTypeVariables=st.getTopLevelBlockModel().getOwnerModel().getTypeParameters();
             Iterable<JavaTypeModel> enclosedLocalTypes = new LocalTypesIterable(st);
 
             try {
                 return resolveTypeModelByName(name,enclosedType,enclosedTypeVariables,enclosedLocalTypes);
             }catch(EntityNotFoundException ex){
                 ; // impossible ?
             }
         }
         
         
         // now get current package model and try to find class in current compilation unit in this
         // package.
         JavaPackageModel pm=where.getPackageModel();
         
         JavaUnitModel um=where.getUnitModel();
         if (um==null) {
             LOG.warning("um is null for "+where.getFullName());
             try {
                 JavaTypeModel retval=pm.findTypeModel(name);
                 if (retval!=null) {
                     return retval;
                 }
             }catch(EntityNotFoundException ex){
                 ; /* do nothing */
             }
         }else{
             return resolveTypeModelByName(name,um,pm,typeVariables);
         }
         
         
         
         
         try {
             //if all fail, try to find in "java.lang"
             return resolveTypeModelFromPackage(name,"java.lang");
         }catch(EntityNotFoundException ex){
             /* do nothing */
             ;
         }
         
         if (printDetails) {
             LOG.log(Level.INFO,"failed resolving of "+name+" in "+where.getName());
         }
         //we still here - it means that class was not found in import declarations.
         throw new EntityNotFoundException(" type ",name,"");
     }
     
     /**
      * resolve type outside parent type context or throw EntityNotFoundExceptionis if type is not avaible
      * @param name - name of type to resolve
      * @param um - unit model where we situated. (i.e. we use import statements from this module)
      * @param pm - package model. where we situated.
      * @param typeVariables - type variables, aviable in this context
      * @return resolved type
      */
     public static JavaTypeModel resolveTypeModelByName(String name, JavaUnitModel um, JavaPackageModel pm, List<JavaTypeVariableAbstractModel> typeVariables) throws TermWareException, EntityNotFoundException {
         boolean printDetails=false;
         
        // if (name.equals("TypeCheckerProperties")) {
        //     printDetails=true;
        // }
         
         if (printDetails) {
             LOG.log(Level.INFO,"resolveTypeModelByName("+name+","+um.toString()+","+pm.getName()+"), typeVariables="+typeVariables);
         }
         
         
         if (typeVariables!=null) {
             for(JavaTypeVariableAbstractModel tv: typeVariables) {
                 if (tv.getName().equals(name)) {
                     if (printDetails) {
                       LOG.log(Level.INFO,"found in type variables");
                     }
                     return tv;
                 }
             }
         }
         
         
         
         if (um instanceof JavaCompilationUnitModel) {
             JavaCompilationUnitModel cu=(JavaCompilationUnitModel)um;
             //at first, try to find in class imports.
             JavaCompilationUnitModel.ClassImportSuffix suffix = cu.getClassImports().get(name);
             if (suffix!=null) {
                 JavaTypeModel tm = suffix.getTypeModel();
                 if (tm!=null) {
                     return tm;
                 }else{
                     String fullClassName = suffix.getFullClassName();                 
                     tm = JavaResolver.resolveTypeModelByFullClassName(fullClassName);                       
                     suffix.setTypeModel(tm);
                     return tm;
                 }
             }
             
             // then at static member imports
             String staticImportClassName = cu.getStaticMemberImports().get(name);
             if (staticImportClassName!=null) {
                 // cool, we found what we need.
                 JavaTypeModel tm=JavaResolver.resolveTypeModelByFullClassName(staticImportClassName);
                 // then it can be nested type
                 try {
                     return tm.findNestedTypeModel(name);
                 }catch(EntityNotFoundException ex){
                     // Hmm, this was method with same name ?
                     ;
                 }catch(NotSupportedException ex){
                     // near impossible.
                     ;
                 }
                    
                 
             }
             
         }
         
         // if not found in class imports, than in current package
         try {
             JavaTypeModel retval=pm.findTypeModel(name);
             if (retval!=null) {
                 return retval;
             }
         }catch(EntityNotFoundException ex){
             ; /* do nothing */
         }
         
        
         
         
         // try to get Type Model from any aviable packages.
         if (um instanceof JavaCompilationUnitModel) {
             JavaCompilationUnitModel cu=(JavaCompilationUnitModel)um;
             for(Map.Entry<String,JavaCompilationUnitModel.PackageOrClassModelHolder> e:cu.getPackageOrClassImports().entrySet()){
                 String cPackageOrClassName = e.getKey();
                 JavaCompilationUnitModel.PackageOrClassModelHolder holder = e.getValue();
                 if (holder.isInitialized()) {
                    if (holder.getPackageModel()!=null) { 
                      try {
                         return holder.getPackageModel().findTypeModel(name); 
                        //return resolveTypeModelFromPackage(name,cPackageOrClassName);
                      }catch(EntityNotFoundException ex){
                     /* do nothing */
                        ;
                      }
                    }else if (holder.getTypeModel()!=null) {
                        JavaTypeModel tm = holder.getTypeModel();
                        // tyr to find nested class here.
                        // without inheritance.
                        try {
                            return tm.findNestedTypeModel(name);
                        }catch(EntityNotFoundException ex){
                            /* do nothing */
                            ;
                        }catch(NotSupportedException ex){
                            /* strange, but skip */
                            ; 
                        }
                    }
                 }else{                    
                     JavaTypeModel retval=null;                    
                     JavaPackageModel npm=Main.getFacts().getPackagesStore().findOrAddPackage(cPackageOrClassName);
                     try {                       
                         retval = npm.findTypeModel(name); 
                     }catch(EntityNotFoundException ex){
                         ;
                     }
                     if (retval!=null) {
                         // great, we find all waht we need.
                         holder.setPackageModel(npm);
                         return retval;
                     }else{
                         // hmm, problem here.
                         if (npm.getNumberOfLoadedReferences()==0) {
                             // nothig was loaded, so we can remove one
                             Main.getFacts().getPackagesStore().removePackage(cPackageOrClassName);
                         }
                     }
                     // if we still here, try to look to find class import
                     JavaTypeModel importedType=null;
                     try {
                        importedType=JavaResolver.resolveTypeModelByFullClassName(cPackageOrClassName); 
                     }catch(EntityNotFoundException ex){
                         // it's not our case;
                         ;
                     }
                     if (importedType!=null) {
                         try {
                             retval = importedType.findNestedTypeModel(name);
                         }catch(EntityNotFoundException ex){
                             ;
                         }catch(NotSupportedException ex){
                             ;
                         }
                         if (retval!=null) {
                             // great, save to holder.
                             holder.setTypeModel(importedType);
                             return retval;
                         }
                     }
                     
                 }
             }
         }
        
          // try to find as nested type of static import
          //  (or not nested)
         if (um instanceof JavaCompilationUnitModel) {
             JavaCompilationUnitModel cu=(JavaCompilationUnitModel)um;
             Set<String> staticClassImports = cu.getStaticClassImports();
             for(String className: staticClassImports) {
                 JavaTypeModel importedClass = null;
                 try {
                    importedClass = JavaResolver.resolveTypeModelByFullClassName(className); 
                 }catch(EntityNotFoundException ex){
                     // near impossible. ignore
                     continue;
                 }
                 if (importedClass!=null) {
                   if (importedClass.getName().equals(name)) {
                       return importedClass;
                   }  
                   if (importedClass.hasNestedTypeModels()) {  
                     try {  
                        return importedClass.findNestedTypeModel(name);
                     }catch(EntityNotFoundException ex){
                       //
                         continue;
                     }catch(NotSupportedException ex){
                         // impossible.
                         ;
                     }
                   }
                 }
             }                        
         }
         
         
         
         
         
         try {
             //if all fail, try to find in "java.lang"
             return resolveTypeModelFromPackage(name,"java.lang");
         }catch(EntityNotFoundException ex){
             /* do nothing */
             ;
         }
         
         if (printDetails) {
             LOG.log(Level.INFO,"failed search of type "+name+" outside class ");
         }
         //we still here - it means that class was not found in import declarations.
         throw new EntityNotFoundException(" type ",name,"");
     }
     
     public static JavaTypeModel resolveTypeModelWithFullPackage(Term t, JavaTypeModel where, List<JavaTypeVariableAbstractModel> typeVariables) throws EntityNotFoundException, TermWareException {
         boolean printDetails=false;
         if (!t.getName().equals("ClassOrInterfaceType")) {
             throw new AssertException("argument of resolveTypeModelWithFullPackageName must be ClassOrInterfaceType, we have:"+TermHelper.termToString(t));
         }
         JavaUnitModel unitModel=null;
         JavaPackageModel packageModel=null;
         if (where!=null) {
             unitModel=where.getUnitModel();
             packageModel=where.getPackageModel();
         }
         Term head=t.getSubtermAt(0);
         Term prev=null;
         int fi=0;
         while(!head.isNil()) {
             Term ct=head.getSubtermAt(0);
             head=head.getSubtermAt(1);
             if (!ct.getName().equals("Identifier")) {
                 
                 break;
             }
             prev=ct;
             ++fi;
         }
         
         String classShortName=null;
         if (prev!=null) {
             classShortName=prev.getSubtermAt(0).getString();
         }else{
             throw new AssertException("No identifier inside ClassOrInterfceType");
         }
         
         StringBuffer sb=new StringBuffer();
         head=t.getSubtermAt(0);
         Term revList=TermUtils.createNil();
         int index=0;
         boolean frs=true;
         while(!head.isNil()) {
             Term ct=head.getSubtermAt(0);
             revList=TermUtils.createTerm("cons",ct,revList);
             head=head.getSubtermAt(1);
             ++index;
             if (ct==prev) {
                 break;
             }
             if (!frs) {
                 sb.append('.');
             }else{
                 frs=false;
             }
             sb.append(ct.getSubtermAt(0).getString());
         }
         String packageName=sb.toString();
         try {
             JavaTypeModel retval=resolveTypeModelFromPackage(classShortName,packageName);
             if (!head.isNil()) {
                 return resolveRestOfClassOrInterfaceType(retval,head,unitModel,packageModel,where,typeVariables,null,printDetails);
             }
             return retval;
         }catch(EntityNotFoundException ex){
             for(;;) {
                 int lastDotIndex=packageName.lastIndexOf(".");
                 if (lastDotIndex==-1) {
                     throw new EntityNotFoundException("type",TermHelper.termToString(t),"");
                 }
                 String candidateName = packageName.substring(lastDotIndex+1);
                 packageName = packageName.substring(0,lastDotIndex);
                 //System.err.println("candidateName:"+candidateName+",packageName="+packageName);
                 Term ct = revList.getSubtermAt(0);
                 revList = revList.getSubtermAt(1);
                 head=TermUtils.createTerm("cons",ct,head);
                 try {
                     JavaTypeModel retval=resolveTypeModelFromPackage(candidateName,packageName);
                     if (!head.isNil()) {
                         return resolveRestOfClassOrInterfaceType(retval,head,unitModel,packageModel,where,typeVariables,null,printDetails);
                     }
                 }catch(EntityNotFoundException ex1){
                     ; //ignore
                 }
             }
         }
     }
     
     
     public static JavaTypeModel resolveTypeModelFromPackage(String classShortName,String packageName) throws EntityNotFoundException, TermWareException {
        // LOG.info("resolveTypeModelFromPackage:"+classShortName+","+packageName);
         JavaPackageModel packageModel = resolvePackage(packageName);
         return packageModel.findTypeModel(classShortName);
     }
     
     public static JavaTypeModel  resolveTypeModelByFullClassName(String name) throws EntityNotFoundException, TermWareException {
        // LOG.info("resolveTypeModelByFullClassName:"+name);
         int lastDotIndex=name.lastIndexOf('.');
         if (lastDotIndex!=-1) {
             String packageName = name.substring(0,lastDotIndex);
             String className = name.substring(lastDotIndex+1);
             try {
                 return resolveTypeModelFromPackage(className,packageName);
             }catch(EntityNotFoundException ex){
                 // this can be request to nested class.
                 JavaTypeModel enclosingModel = resolveTypeModelByFullClassName(packageName);
                 try {
                     return enclosingModel.findNestedTypeModel(className);
                 }catch(NotSupportedException ex1){
                     throw new EntityNotFoundException("type",name,"");
                 }
             }
         }else{
             throw new EntityNotFoundException("type",name,"");
         }
     }
     
     public static JavaPackageModel resolvePackage(String packageName) {
         return Main.getFacts().getPackagesStore().findOrAddPackage(packageName);
     }
     
     /**
      * resolve member variable by name in scope of class definition (outside block)
      */
     public static JavaMemberVariableModel resolveMemberVariableByName(String name,JavaTypeModel where) throws TermWareException, EntityNotFoundException {
         boolean printDetails=false;
         if(printDetails) {
             LOG.log(Level.INFO,"resolveMemberVariableByName("+name+","+where.getFullName()+")");
         }
         LinkedList<JavaTypeModel> toCheck = new LinkedList<JavaTypeModel>();
         toCheck.addFirst(where);
         while(!toCheck.isEmpty()) {
             JavaTypeModel checked = toCheck.removeLast();
             try {
                 if (printDetails) {
                     LOG.log(Level.INFO,"check in "+checked.getFullName());
                 }
                 JavaMemberVariableModel retval = checked.findMemberVariableModel(name);
                 if (printDetails) {
                     LOG.log(Level.INFO,"found");
                 }
                 return retval;
             }catch(EntityNotFoundException ex){
                 // ignore
                 ;
             }catch(NotSupportedException ex){
                 // ignore
                 ;
             }
             try {
                 JavaTypeModel checkedSuperClass = checked.getSuperClass();
                 if (!checkedSuperClass.isNull()) {
                     toCheck.addFirst(checkedSuperClass);
                 }
             }catch(NotSupportedException ex){
                 //do nothing.
                 ;
             }
             try {
                 List<JavaTypeModel> checkedSuperInterfaces = checked.getSuperInterfaces();
                 for(JavaTypeModel si: checkedSuperInterfaces) {
                     toCheck.addFirst(si);
                 }
             }catch(NotSupportedException ex){
                 //do nothing
                 ;
             }
             if (checked.isNested()) {
                 try {
                     toCheck.add(checked.getEnclosedType());
                 }catch(NotSupportedException ex){
                     /*impossible ignore */
                     ;
                 }
             }
         }
         if (printDetails) {
             LOG.log(Level.INFO,"not found("+name+","+where.getFullName()+")");
         }
         throw new EntityNotFoundException(" member variable ",name,where.getFullName());
     }
     
     
     /**
      *resolve any variable outside scope definition.
      *(this can be member variable, constant from static import,
      * variable of enclosed statement for local and anonimous classes)
      */
     public static JavaVariableModel resolveVariableByName(String name,JavaTypeModel where) throws TermWareException, EntityNotFoundException {
         boolean printDetails=false;
         // check member variable.
      //   if (name.equals("facts")) {
      ///       printDetails=true;
      //   }
         
         if (printDetails) {
             LOG.log(Level.INFO,"resolveVariableByName("+name+","+where.getFullName()+")");
         }
         
         try {
             JavaVariableModel retval = resolveMemberVariableByName(name,where);
             if (printDetails) {
                 LOG.log(Level.INFO,"found as member variable");
             }
             return retval;
         }catch(EntityNotFoundException ex){
             if (printDetails) {
                 LOG.log(Level.INFO,"it is not member variable");
             }
         }
         
         
         // now, may be we have enclosed statement ?
         JavaTypeModel currWhere = where;
         while (currWhere.isNested()||currWhere.isAnonimous()||currWhere.isLocal()) {
             JavaStatementModel st = currWhere.getEnclosedStatement();
             if (st!=null) {
                 try {
                     if (printDetails) {
                         LOG.log(Level.INFO,"try to search in enclosed statement");
                     }
                     JavaVariableModel retval = resolveVariableByName(name,st);
                     if (printDetails) {
                         LOG.log(Level.INFO,"found");
                     }
                     return retval;
                 }catch(EntityNotFoundException ex){
                     // block which contains where does not define variable name.
                     if (printDetails) {
                         LOG.log(Level.INFO,"not in enclosed statement");
                     }
                     
                 }
             }else{
                 if (printDetails) {
                     LOG.log(Level.INFO,"is nested but enclosed statement is null");
                 }
             }
             try {
                 currWhere=currWhere.getEnclosedType();
             }catch(NotSupportedException ex){
                 // impossible, nested means exists enclosed type;
                 break;
             }
         }
         
         // now try to get static imports.
         JavaUnitModel um=where.getUnitModel();
         if (um==null) {
             // strange
             LOG.warning("um is null for "+where.getFullName());
         } else if (um instanceof JavaCompilationUnitModel) {            
             JavaCompilationUnitModel cum=(JavaCompilationUnitModel)um;
             
             Map<String,String> staticMemberImports = cum.getStaticMemberImports();
             for(Map.Entry<String,String> e: staticMemberImports.entrySet()) {
                 if (e.getKey().equals(name)) {
                     JavaTypeModel tp=null;
                     try {
                         tp=resolveTypeModelByFullClassName(e.getValue());
                         if (printDetails) {
                             LOG.log(Level.INFO,"try to search in "+tp.getFullName());
                         }
                         JavaVariableModel retval = resolveMemberVariableByName(name,tp);
                         if (printDetails) {
                             LOG.log(Level.INFO,"found");
                         }
                         return retval;
                     }catch(EntityNotFoundException ex){
                         // this was a method. ignore.
                         if (printDetails) {
                             LOG.log(Level.INFO,"not in this static import");
                         }
                     }
                 }
             }
             
             //search such fields in static class imports
             Set<String> staticClassImports = cum.getStaticClassImports();
             for (String s:staticClassImports) {
                 JavaTypeModel st=null;
                 try {                    
                     st=resolveTypeModelByFullClassName(s);
                 }catch(EntityNotFoundException ex){
                     // class not found, i.e. this must not compile.
                     // we can ignore this.
                     continue;
                 }
                 try {
                     if (printDetails) {
                         LOG.log(Level.INFO,"try to search in "+st.getFullName());
                     }
                     JavaVariableModel retval = resolveVariableByName(name,st);
                     if (printDetails) {
                         LOG.log(Level.INFO,"found");
                     }
                     return retval;
                 }catch(EntityNotFoundException ex){
                     // it's not our case, ignore
                     if (printDetails) {
                         LOG.log(Level.INFO,"not here");
                     }
                 }
             }
             // if we still here, than static import does not work for us.
         }
         
         if (printDetails) {
             LOG.log(Level.INFO,"name "+name+" is not found in "+where.getFullName());
         }
         throw new EntityNotFoundException("variable",name,where.getFullName());
     }
     
     
     public static JavaVariableModel resolveVariableByName(String name, JavaStatementModel statement) throws TermWareException, EntityNotFoundException {
         boolean printDetails=false;
     //    if (name.equals("facts")) {
     //        printDetails=true;
     //    }
         boolean quit=false;
         JavaStatementModel parentStatement=statement.getParentStatementModel();
         while(!quit) {
             if (printDetails) {
                 StringBuilder sb = new StringBuilder();
                 sb.append("resolveVariableByName("+name+"),statement "+statement);
                 if (statement instanceof JavaTermStatementModel) {
                     JavaTermStatementModel ts = (JavaTermStatementModel)statement;
                     sb.append(":"+TermHelper.termToString(ts.getTerm()));
                 }
                 LOG.log(Level.INFO,sb.toString());
             }
             
             List<JavaLocalVariableModel> lv = statement.getLocalVariables();
             for(JavaLocalVariableModel v:lv) {
                 if (printDetails) {
                     LOG.log(Level.INFO,"check for variable: "+v.getName());
                 }
                 if (v.getName().equals(name)) {
                     return v;
                 }
             }
             if (parentStatement!=null) {
                 if (printDetails) {
                     LOG.log(Level.INFO,"parentStatement.getKind()="+parentStatement.getKind());
                 }
                 JavaStatementKind parentKind=parentStatement.getKind();
                 if (parentKind==JavaStatementKind.BLOCK || parentKind==JavaStatementKind.SWITCH_LABEL_BLOCK) {
                     statement=statement.getPreviousStatementModel();
                     if (statement==null) {
                         statement=parentStatement;
                         parentStatement=statement.getParentStatementModel();
                         if (parentKind==JavaStatementKind.SWITCH_LABEL_BLOCK) {
                             JavaStatementModel prev=statement.getPreviousStatementModel();
                             while (prev!=null) {
                                 int prevSize = prev.getChildStatements().size();
                                 if (prevSize==0) {
                                     prev=prev.getPreviousStatementModel();
                                 }else{
                                     statement=prev.getChildStatements().get(prevSize-1);
                                     parentStatement=statement.getParentStatementModel();
                                     break;
                                 }
                             }
                         }
                     }
                 }else{
                     statement=parentStatement;
                     parentStatement=statement.getParentStatementModel();
                 }
             }else{
                 //System.out.println("parentStatement=null");
                 // parentStatement is null
                 if (statement.getPreviousStatementModel()!=null) {
                     // top-level block, where root statement is not block.
                     statement=statement.getPreviousStatementModel();
                 }else{
                     quit=true;
                 }
             }
         }
         
         // This can be variable in enclosed function.
         JavaTypeModel tm = statement.getTopLevelBlockModel().getOwnerModel().getTypeModel();
         if (tm.isLocal()||tm.isAnonimous()) {
             JavaStatementModel enclosed=tm.getEnclosedStatement();
             if (enclosed!=null) {
                 try {
                     return resolveVariableByName(name,enclosed);
                 }catch(EntityNotFoundException ex){
                     //ohh, this is not our case, skip
                     ;
                 }
             }
         }
         
         
         // This is not local variable, let's check formal parameters.
         Map<String,JavaFormalParameterModel> fpm = statement.getTopLevelBlockModel().getOwnerModel().getFormalParametersMap();
         JavaFormalParameterModel fp = fpm.get(name);
         if (fp!=null) {
             return fp;
         }
         // Now this can be resolved only outside our top-level block.
         JavaVariableModel retval=resolveVariableByName(name,statement.getTopLevelBlockModel().getOwnerModel().getTypeModel());
         return retval;
     }
     
     public static JavaFormalParameterModel resolveFormalParameterByName(String name, JavaTopLevelBlockOwnerModel ownerModel) throws EntityNotFoundException, TermWareException {
         Map<String,JavaFormalParameterModel> fpm = ownerModel.getFormalParametersMap();
         JavaFormalParameterModel fp = fpm.get(name);
         if (fp!=null) {
             return fp;
         }
         throw new EntityNotFoundException("formal parameter",name,ownerModel.getTypeModel().getFullName()+"."+ownerModel.getName());
     }
     
    
     
     /**
      * resolve method call to where (in where and all superclasses) and build substitutuion of method type arguments if needed.
      */
     public static JavaMethodModel resolveMethod(String methodName,List<JavaTypeModel> argumentTypes, JavaTypeArgumentsSubstitution substitution,JavaTypeModel where) throws EntityNotFoundException, TermWareException {
         boolean printDetails=false;
         
        // if (methodName.equals("addAll")) {
        //      printDetails=true;
        // }
         
         if (printDetails) {
             StringBuilder sb=new StringBuilder();
             boolean frs=true;
             sb.append("resolveMethod "+methodName+"(");
             for(JavaTypeModel at: argumentTypes) {
                 if (frs) frs=false; else sb.append(",");
                 sb.append(at.getName());
             }
             sb.append(")");
             sb.append("  in "+where.getName());    
             if (where.hasTypeParameters()) {
                 sb.append(", tp="+where.getTypeParameters() );
             }
             LOG.log(Level.INFO,sb.toString());
         }
         boolean found=false;
         LinkedList<Pair<JavaTypeModel,Integer>> toCheck = new LinkedList<Pair<JavaTypeModel,Integer>>();
         toCheck.add(new Pair<JavaTypeModel,Integer>(where,0));
         List<Pair<JavaMethodModel,MethodMatchingConversions>> goodCandidates = new ArrayList<Pair<JavaMethodModel,MethodMatchingConversions>>();
         
        
         while(!toCheck.isEmpty()){
             Pair<JavaTypeModel,Integer> currPair = toCheck.removeFirst();            
             JavaTypeModel currWhere=currPair.getFirst();
             int nSupers=currPair.getSecond();
             List<JavaMethodModel> candidates=null;
             try{
                 if (printDetails) {
                     LOG.log(Level.INFO,"Search candidates in "+currWhere.getFullName()+" ("+currWhere.toString()+")");
                 }
                 candidates = currWhere.findMethodModels(methodName);
             }catch(EntityNotFoundException ex) {
                 // candidates are still null
                 if (printDetails) {
                     LOG.log(Level.INFO,"not found in "+currWhere.getFullName());
                 }
             }catch(NotSupportedException ex) {
                 // try to search in primitive type.
                 // impossible, we can ignore one here
                 if (printDetails) {
                     LOG.log(Level.INFO,"NotSupported in "+currWhere.getFullName());
                 }
             }
             if (candidates!=null) {
                 int nArguments=argumentTypes.size();
                 for(JavaMethodModel candidate:candidates) {
                     if (printDetails){
                         StringWriter swr=new StringWriter();
                         PrintWriter wr=new PrintWriter(swr);
                         wr.print("found candidate:");
                         candidate.printSignature(wr);
                         wr.flush();
                         String s = swr.toString();
                         wr.close();
                         LOG.log(Level.INFO,s);
                     }
                     
                     List<JavaFormalParameterModel> fpts = candidate.getFormalParametersList();
                     
                                         
                     MethodMatchingConversions conversions = new MethodMatchingConversions();                               
                     conversions.setNSupers(nSupers);
                     if (match(fpts,argumentTypes,conversions,false,printDetails)) {
                         // TODO: we must peek best instead returning first.
                         if (printDetails) {
                             StringWriter swr=new StringWriter();
                             PrintWriter wr=new PrintWriter(swr);
                             wr.print("matched, substitution=");
                             conversions.getSubstitution().print(wr);
                             wr.flush();
                             String s = swr.toString();
                             wr.close();
                             LOG.log(Level.INFO,s);
                         }      
                         if (conversions.exactly()) {
                             substitution.putAll(conversions.getSubstitution());
                             return candidate;
                         }
                         goodCandidates.add(new Pair<JavaMethodModel,MethodMatchingConversions>(candidate,conversions));
                     }else{
                         if (printDetails) {
                             LOG.log(Level.INFO,"not match");
                         }
                         //TODO: restore substituton
                     }
                 }
             }
             // if we here, than nothing found here, try supers
             if (!currWhere.isNull() && !(currWhere.isEnum() && currWhere.getFullName().equals("java.lang.Enum"))) {
                 try {                    
                     toCheck.add(new Pair<JavaTypeModel,Integer>(currWhere.getSuperClass(),nSupers+1));
                 }catch(NotSupportedException ex){
                     // impossible,
                     ;
                 }
             }
             if(true /*currWhere.isInterface()||currWhere.getModifiersModel().isAbstract()*/) {
                 try {
                     List<JavaTypeModel> interfaces = currWhere.getSuperInterfaces(); 
                     int nextNSupers=nSupers+1;
                     for(JavaTypeModel si:currWhere.getSuperInterfaces()) {
                       toCheck.add(new Pair<JavaTypeModel,Integer>(si,nextNSupers));
                     }
                 }catch(NotSupportedException ex){
                     //  ignore
                     ;
                 }
             }
             if (currWhere.isTypeVariable()) {
                 JavaTypeVariableAbstractModel tmaWhere = (JavaTypeVariableAbstractModel)currWhere;
                 List<JavaTypeModel> bounds = tmaWhere.getBounds();
                 if (printDetails) {                    
                     StringWriter swr = new StringWriter();
                     PrintWriter pwr = new PrintWriter(swr);
                     boolean frs=true;                    
                     for(JavaTypeModel bound: bounds) {
                         if (!frs) pwr.print(","); else frs=false;
                         pwr.print(bound.getFullName());
                     }
                     pwr.close();
                     LOG.log(Level.INFO,"typeArgument, bounds are:"+swr.toString());
                 }
                 if (bounds.isEmpty()) {
                     // check in object.
                     JavaTypeModel objectTypeModel = JavaResolver.resolveJavaLangObject();
                     toCheck.addLast(new Pair<JavaTypeModel,Integer>(objectTypeModel,nSupers+1));
                 }else{
                     int nextNSupers=nSupers+1;
                     for(JavaTypeModel bound: bounds) {
                        toCheck.add(new Pair<JavaTypeModel,Integer>(bound,nextNSupers));
                     }
                 }
             }
             
             //then may be this is a method of enclosing class ?
             if (currWhere.isNested()) {
                 try {
                     toCheck.addLast(new Pair<JavaTypeModel,Integer>(currWhere.getEnclosedType(),nSupers));
                 }catch(NotSupportedException ex){
                     ;
                     //impossible, ignore.
                 }
             }
             
             
         }
         
         //see static import
         JavaUnitModel um=where.getUnitModel();
         if (um==null) {
             // strange
             LOG.warning("um is null for "+where.getFullName());
         } else if (um instanceof JavaCompilationUnitModel) {
             JavaCompilationUnitModel cum = (JavaCompilationUnitModel)um;
                         Map<String,String> methodClassImports = cum.getStaticMemberImports();
             if (methodClassImports.get(methodName)!=null) {
                 String fullClassName = methodClassImports.get(methodName);
                 if (printDetails) {
                   LOG.log(Level.INFO,"search in static method imports for "+fullClassName);
                 }
 
                 //System.err.println("full class name is:"+fullClassName);
                 JavaTypeModel importedType = null;
                 try {
                     importedType = JavaResolver.resolveTypeModelByFullClassName(fullClassName);
                 }catch(EntityNotFoundException ex){
                     // invalid import. ignore
                     ;
                 }
                 if (importedType!=null) {
                     List<JavaMethodModel> ml=null;
                     try{
                         ml=importedType.findMethodModels(methodName);
                     }catch(EntityNotFoundException ex){
                         // invalid model, ignore
                         ;
                     }catch(NotSupportedException ex){
                         // invalid model, ignore
                         ;
                     }
                     if (ml!=null) {
                         for(JavaMethodModel m: ml){
                             List<JavaFormalParameterModel> fps=m.getFormalParametersList();                        
                             MethodMatchingConversions conversions=new MethodMatchingConversions();
                             if (match(fps,argumentTypes,conversions,false,printDetails)) {
                                 if (printDetails) {
                                     LOG.log(Level.INFO,"found candidate "+m.toString());
                                 }
                                 goodCandidates.add(new Pair<JavaMethodModel,MethodMatchingConversions>(m,conversions));
                             }else{
                                // nothibng                              
                             }
                         }
                     }
                 }
             } // end of method class imports.
             
             Set<String> staticClassImports = cum.getStaticClassImports();
             for(String className: staticClassImports) {
                 if (printDetails) {
                   LOG.log(Level.INFO,"search in static class imports for "+className);
                 }                
                 JavaTypeModel importedType=null;
                 try {
                     importedType = JavaResolver.resolveTypeModelByFullClassName(className);
                 }catch(EntityNotFoundException ex){
                     continue;
                 }
                 if (importedType!=null) {
                     List<JavaMethodModel> ml=null;
                     try {
                      ml = importedType.findMethodModels(methodName);
                     }catch(EntityNotFoundException ex){
                         continue;                        
                     }catch(NotSupportedException ex){
                         continue;
                     }
                     if (ml!=null) {
                         for(JavaMethodModel m: ml) {
                             List<JavaFormalParameterModel> fps=m.getFormalParametersList();                        
                             MethodMatchingConversions conversions=new MethodMatchingConversions();
                             if (match(fps,argumentTypes,conversions,false,printDetails)) {
                                 if (printDetails) {
                                     LOG.log(Level.INFO,"found candidate");
                                 }
                                 goodCandidates.add(new Pair<JavaMethodModel,MethodMatchingConversions>(m,conversions));
                             }else{
                                // nothibng                              
                             }                            
                         }
                     }
                 }
             }
             
         }
         
         Pair<JavaMethodModel,MethodMatchingConversions> retval = MethodMatchingConversions.best(goodCandidates);
         if (retval!=null) {
             substitution.putAll(retval.getSecond().getSubstitution());
             return retval.getFirst();
         }
         
         // we still here ?
         //  ok, it means that notning is found.
         //  let's prepare full method name and throw exception
         StringBuilder sb = new StringBuilder();
         sb.append(methodName);
         sb.append('(');
         boolean frs=true;
         for(JavaTypeModel tm: argumentTypes) {
             if (!frs) {
                 sb.append(",");
             }else{
                 frs=false;
             }
             sb.append(tm.getFullName());
         }
         sb.append(')');
         String fullMethodName=sb.toString();
         throw new EntityNotFoundException("method",fullMethodName, "in class "+where.getFullName());
     }
     
     
     public static JavaTypeModel resolveTypeTerm(Term t, JavaPlaceContext ctx) throws EntityNotFoundException, TermWareException {
         if (ctx.getStatementModel()!=null) {
             return resolveTypeToModel(t,ctx.getStatementModel());
         }else if (ctx.getTopLeveBlockOwnerModel()!=null) {
             return resolveTypeToModel(t,ctx.getTypeModel(),ctx.getTopLeveBlockOwnerModel().getTypeParameters());
         }else if (ctx.getTypeModel()!=null) {
             return resolveTypeToModel(t,ctx.getTypeModel(),null);
         }else if (ctx.getPackageModel()!=null) {
             throw new AssertException("Can't resolve type model without enclosing type");
         }else{
             return resolveTypeModelWithFullPackage(t,null,null);
         }
     }
     
     /**
      *resolve type of Java expression.
      *@param t - term, which must be java expression.
      *@param ctx - place context for resolving names.
      */
     public static JavaTypeModel resolveExpressionType(Term t, JavaPlaceContext ctx) throws EntityNotFoundException, TermWareException {
         return JavaExpressionHelper.resolveExpressionType(t,ctx);
     }
     
     /**
      * resolve type of Java expression and return one if found, otherwise throw EntityNotFoundException
      * @param name name to resolve
      * @param ctx context, in which we try to resolve
      * @return resolved typeModel if found. 
      * @exception EntityNotFoundException
      */
    public static JavaTypeModel resolveTypeModelByName(String name, JavaPlaceContext ctx) throws EntityNotFoundException, TermWareException {
         if (ctx.getStatementModel()!=null) {
             return resolveTypeModelByName(name,ctx.getTypeModel(),ctx.getTopLeveBlockOwnerModel().getTypeParameters(),new LocalTypesIterable(ctx.getStatementModel()));
        }else if (
                ctx.getTopLeveBlockOwnerModel()!=null) {
             return resolveTypeModelByName(name,ctx.getTypeModel(),ctx.getTopLeveBlockOwnerModel().getTypeParameters(),null);
         }else if (ctx.getTypeModel()!=null) {
             return resolveTypeModelByName(name,ctx.getTypeModel(),null,null);
         }else if (ctx.getPackageModel()!=null) {
             try {
                 return resolveTypeModelFromPackage(name,ctx.getPackageModel().getName());
             }catch(EntityNotFoundException ex){
                 ;
             }
         }
         return resolveTypeModelFromPackage(name,"java.lang");
     }
     
     /**
      * resolve variavle name in given context or throw EntityNotFoundException is not found
      * @param name - name of varibale to resolve
      * @param ctx - context
      * @return model of variable if found
      */
     public static JavaVariableModel resolveVariableByName(String name, JavaPlaceContext ctx) throws EntityNotFoundException, TermWareException {
         if (ctx.getStatementModel()!=null) {
             return resolveVariableByName(name,ctx.getStatementModel());
         }else if (ctx.getTopLeveBlockOwnerModel()!=null){
             try {
                 return resolveFormalParameterByName(name,ctx.getTopLeveBlockOwnerModel());
             }catch(EntityNotFoundException ex){
                 return resolveVariableByName(name,ctx.getTypeModel());
             }
         }else if (ctx.getTypeModel()!=null) {
             return resolveVariableByName(name,ctx.getTypeModel());
         }else{
             throw new AssertException("Can't resolve variable outsde type model");
         }
     }
     
     
     public static boolean match(JavaTypeModel pattern, JavaTypeModel x, MethodMatchingConversions conversions, boolean debug) throws TermWareException, EntityNotFoundException {
         if (pattern.isPrimitiveType() && !x.isPrimitiveType()) {           
             return match1(pattern,JavaTypeModelHelper.unboxingConversion(x,conversions),conversions,debug);
         }else if (!pattern.isPrimitiveType() && x.isPrimitiveType()) {
             return match1(pattern,JavaTypeModelHelper.boxingConversion(x,conversions),conversions,debug);
         }else{
             return match1(pattern,x,conversions,debug);
         }
     }
     
     /**
      * match patern type with x and fill substitution if needed after aplying boxing/unboxing conventions.
      */
     public static boolean match1(JavaTypeModel pattern, JavaTypeModel x,MethodMatchingConversions conversions, boolean topDebug) throws TermWareException, EntityNotFoundException {
         boolean match1DebugEnabled=true;
         boolean debug = topDebug && match1DebugEnabled;
         if (debug) {
             LOG.log(Level.INFO,"match1("+pattern.getFullName()+","+x.getFullName()+")");
         }
         boolean retval=true;
         MethodMatchingConversions cn=new MethodMatchingConversions(conversions);
         if (pattern.isTypeVariable()) {
             JavaTypeVariableAbstractModel vpattern = (JavaTypeVariableAbstractModel)pattern;
             JavaTypeModel matched=conversions.getSubstitution().get(vpattern);
             if (matched!=null) {
                 if (matched.isTypeVariable()) {                    
                    if (JavaTypeModelHelper.subtypeOrSame(x,pattern,cn,true,debug)) {                       
                        retval=true;
                    }else{
                        //to prevent recursion we must deconstruct or put pattern here.
                        JavaTypeVariableAbstractModel vmatched = (JavaTypeVariableAbstractModel)matched;
                        List<JavaTypeModel> bounds = vmatched.getBounds();
                        for(JavaTypeModel bound: bounds) {
                            if (match(bound,x,cn,debug)) {
                                retval=true;
                                break;
                            }
                        }    
                    }
                 }else{
                    retval=match(matched,x,cn,debug);       
                    if (!retval) {
                        // may be it is possible to find pattern, which comply matched and x ?
                        if (refineMatching(vpattern,conversions,matched,x,debug)) {
                            retval=true;
                        }
                    }
                 }                                
             }else{
                 List<JavaTypeModel> bounds = vpattern.getBounds();
                 for(JavaTypeModel bound: bounds) {
                     if (!match(bound,x,cn,debug)) {
                         retval=false;
                         break;
                     }
                 }
                 if (retval) {
                     if (x!=vpattern) {
                       cn.getSubstitution().put(vpattern,x);
                     }
                 }
             }
         }else if (pattern.isWildcardBounds()) {
             JavaWildcardBoundsTypeModel wpattern = (JavaWildcardBoundsTypeModel)pattern;
             switch(wpattern.getKind()) {
                 case OBJECT:
                     retval=true;
                     break;
                 case EXTENDS:
                     retval=JavaTypeModelHelper.subtypeOrSame(x,wpattern.getBoundTypeModel(),cn,true,debug);                    
                     break;
                 case SUPER:
                     retval=JavaTypeModelHelper.subtypeOrSame(wpattern.getBoundTypeModel(),x,cn,true,debug);
                     break;
                 default:
                     throw new AssertException("Impossible: invalid JavaWildcardBoundsKind");
             }
         }else if (x.isNull()) {
             // null can be object of any type.
             retval=true;
         }else if (pattern.isArray()) {
             if (x.isArray()) {
                try { 
                 retval=match1(pattern.getReferencedType(),x.getReferencedType(),cn,debug);
                }catch(NotSupportedException ex){
                    throw new TermWareRuntimeException(ex);
                }
             }else{
                 retval=false;
             }
         }else{
             retval=JavaTypeModelHelper.subtypeOrSame(x,pattern,cn,true,debug);
             if (!retval) {
                 // unchecked conversion.
                if (true/*JavaTypeModelHelper.isRowType(pattern)*/) {
                    //JavaTypeModel uncheckedX=JavaTypeModel.erasureConversion(x);
                    retval=JavaTypeModelHelper.subtypeOrSame(pattern,x,cn,true,debug);
                    // ? add new type of conversion.
                    //TODO: think
                    cn.incrementNNarrows();
                }
            }
         }
         if (debug) {
             LOG.log(Level.INFO,"match return "+retval);
         }
         if (retval==true) {
             conversions.assign(cn);
         }
         return retval;
     }
     
     /**
      * match over list and feel substitution if needed.
      */
     public static boolean match(List<JavaFormalParameterModel> patterns,List<JavaTypeModel> xs,MethodMatchingConversions conversions, boolean forseVarArgs, boolean debug) throws TermWareException, EntityNotFoundException {
         Iterator<JavaFormalParameterModel> pit = patterns.iterator();
         Iterator<JavaTypeModel> xit = xs.iterator();
         boolean inVarArg=false;
         JavaTypeModel varArgPattern=null;
         boolean quit=false;
         boolean retval=true;
         boolean getNextX=true;
         boolean wasVarArgs=true;
         JavaTypeModel x=null;        
         while(!quit) {
             if (!inVarArg) {
                 if (!pit.hasNext()) {
                     if (xit.hasNext()) {     
                         if (!forseVarArgs && wasVarArgs) {
                           MethodMatchingConversions newConversions = new MethodMatchingConversions();
                           retval=match(patterns,xs,newConversions,true,debug);
                           if (retval) {
                              conversions.assign(newConversions);
                           }
                         }else{
                           retval=false;
                         }
                     }
                     break;
                 }else if(!xit.hasNext()) {
                     // check, that it was varArgs,
                     //  if yes - one will associated with empty array
                     retval=(pit.next().getModifiersModel().isVarArgs());                    
                     break;
                 }     
                 JavaFormalParameterModel p = pit.next();
                 wasVarArgs=p.getModifiersModel().isVarArgs();
                 x=xit.next();
                 if ((forseVarArgs && p.getModifiersModel().isVarArgs()) || !match(p.getTypeModel(),x,conversions,debug)) {
                     if (p.getModifiersModel().isVarArgs()) {
                         try {
                             varArgPattern=p.getTypeModel().getReferencedType();
                         }catch(NotSupportedException ex){
                             throw new AssertException("VarArgs parameter must be array");
                         }
                         inVarArg=true;
                         conversions.setVarArg(true);
                         getNextX=false;
                     }else{
                         retval=false;
                         break;
                     }
                 }else{                    
                     getNextX=true;
                 }   
             }else{
                 if (getNextX && !xit.hasNext()) {
                     break;
                 }else{
                     if (getNextX) {
                         x = xit.next();
                     }                    
                     if (!match(varArgPattern,x,conversions,debug)) {
                         if (x.isArray()) {
                             JavaTypeModel x1;
                             try {
                                 x1=x.getReferencedType();
                             }catch(NotSupportedException ex){
                                 throw new AssertException("getReferencedType on Array is unsupported");
                             }
                             if (match(varArgPattern,x1,conversions,debug)) {
                                 // ... matched with array
                                 inVarArg=false;
                                 getNextX=true;
                                 continue;
                             }
                         }
                         if (!pit.hasNext()) {
                             retval=false;
                             break;
                         }else{
                             // just end og var-args.
                             //   try to match with next formal parameter.
                             // This behavious is not supported with JDK5, but can be
                             // potentially usefull for future versions.
                             inVarArg=false;
                             getNextX=false;
                         }
                     }else{
                         getNextX=true;
                     }
                 }
             }
         }
         return retval;
     }
     
     /**
      * refine matching for <code> pattern </code> in MethodMatchingConversions <code> cn </code> in such way,
      *that pattern satisficcy both <code> x </code> and <code> y </code>.
      */
     private static boolean refineMatching(JavaTypeVariableAbstractModel pattern,MethodMatchingConversions cn, JavaTypeModel x, JavaTypeModel y,boolean debug) throws TermWareException, EntityNotFoundException
     {
       List<JavaTypeModel> bounds  = pattern.getBounds();
       for(JavaTypeModel b: bounds) {
           if (!JavaTypeModelHelper.subtypeOrSame(x,b) && JavaTypeModelHelper.subtypeOrSame(y,b)) {
              return false;                           
           }
       }           
       JavaTypeModel newType = JavaTypeModelHelper.minmax(x,y,cn,debug); 
       if (newType!=null) {
         cn.getSubstitution().put(pattern,newType);
         return true;
       }else{
           return false;
       }
     }
     
     public static JavaTypeModel resolveJavaLangObject() throws TermWareException {
         try {
             return resolveTypeModelFromPackage("Object","java.lang");
         }catch(EntityNotFoundException ex){
             throw new AssertException("java.lang.Object must be resolved");
         }
     }
     
     public static JavaTypeModel resolveJavaLangCloneable() throws TermWareException {
         try {
             return resolveTypeModelFromPackage("Cloneable","java.lang");
         }catch(EntityNotFoundException ex){
             throw new AssertException("java.lang.Cloneable must be resolved");
         }
     }
     
     public static JavaTypeModel resolveJavaIoSerializable() throws TermWareException {
         try {
             return resolveTypeModelFromPackage("Serializable","java.io");
         }catch(EntityNotFoundException ex){
             throw new AssertException("java.io.Serializable must be resolved");
         }
     }
     
     public static JavaTypeModel resolveJavaLangAnnotationAnnotation() throws TermWareException {
         try {
             return resolveTypeModelFromPackage("Annotation","java.lang.annotation");
         }catch(EntityNotFoundException ex){
             throw new AssertException("java.lang.Annotation must be resolved");
         }
     }
     
     private static final Logger LOG = Logger.getLogger(JavaResolver.class.getName());
     
 }
