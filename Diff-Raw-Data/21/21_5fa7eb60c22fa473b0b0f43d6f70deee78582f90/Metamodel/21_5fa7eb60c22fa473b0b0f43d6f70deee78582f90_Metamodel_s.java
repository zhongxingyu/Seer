 package com.redhat.ceylon.compiler.java.runtime.metamodel;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationHandler;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import ceylon.language.ArraySequence;
 import ceylon.language.Iterator;
 import ceylon.language.Null;
 import ceylon.language.SequenceBuilder;
 import ceylon.language.Sequential;
 import ceylon.language.empty_;
 import ceylon.language.finished_;
 import ceylon.language.model.Annotated;
 import ceylon.language.model.ClassOrInterface;
 import ceylon.language.model.ConstrainedAnnotation;
 import ceylon.language.model.declaration.Module;
 
 import com.redhat.ceylon.cmr.api.ArtifactResult;
 import com.redhat.ceylon.cmr.api.Logger;
 import com.redhat.ceylon.cmr.api.RepositoryManager;
 import com.redhat.ceylon.cmr.api.RepositoryManagerBuilder;
 import com.redhat.ceylon.compiler.java.Util;
 import com.redhat.ceylon.compiler.java.codegen.Naming;
 import com.redhat.ceylon.compiler.java.metadata.Ceylon;
 import com.redhat.ceylon.compiler.java.runtime.model.ReifiedType;
 import com.redhat.ceylon.compiler.java.runtime.model.RuntimeModuleManager;
 import com.redhat.ceylon.compiler.java.runtime.model.TypeDescriptor;
 import com.redhat.ceylon.compiler.loader.ModelLoader.DeclarationType;
 import com.redhat.ceylon.compiler.loader.impl.reflect.mirror.ReflectionClass;
 import com.redhat.ceylon.compiler.loader.impl.reflect.mirror.ReflectionMethod;
 import com.redhat.ceylon.compiler.loader.model.JavaMethod;
 import com.redhat.ceylon.compiler.loader.model.LazyClass;
 import com.redhat.ceylon.compiler.loader.model.LazyInterface;
 import com.redhat.ceylon.compiler.loader.model.LazyMethod;
 import com.redhat.ceylon.compiler.loader.model.LazyPackage;
 import com.redhat.ceylon.compiler.loader.model.LazyValue;
 import com.redhat.ceylon.compiler.typechecker.context.Context;
 import com.redhat.ceylon.compiler.typechecker.io.VFS;
 import com.redhat.ceylon.compiler.typechecker.model.Functional;
 import com.redhat.ceylon.compiler.typechecker.model.Generic;
 import com.redhat.ceylon.compiler.typechecker.model.Method;
 import com.redhat.ceylon.compiler.typechecker.model.NothingType;
 import com.redhat.ceylon.compiler.typechecker.model.Parameter;
 import com.redhat.ceylon.compiler.typechecker.model.ProducedReference;
 import com.redhat.ceylon.compiler.typechecker.model.ProducedType;
 import com.redhat.ceylon.compiler.typechecker.model.ProducedTypedReference;
 import com.redhat.ceylon.compiler.typechecker.model.Scope;
 import com.redhat.ceylon.compiler.typechecker.model.TypeAlias;
 import com.redhat.ceylon.compiler.typechecker.model.TypeDeclaration;
 import com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration;
 
 public class Metamodel {
 
     private static RuntimeModuleManager moduleManager;
     
     // FIXME: this will need better thinking in terms of memory usage
     private static Map<com.redhat.ceylon.compiler.typechecker.model.Declaration, com.redhat.ceylon.compiler.java.runtime.metamodel.FreeTopLevelOrMemberDeclaration> typeCheckModelToRuntimeModel
         = new HashMap<com.redhat.ceylon.compiler.typechecker.model.Declaration, com.redhat.ceylon.compiler.java.runtime.metamodel.FreeTopLevelOrMemberDeclaration>();
 
     private static Map<com.redhat.ceylon.compiler.typechecker.model.Package, com.redhat.ceylon.compiler.java.runtime.metamodel.FreePackage> typeCheckPackagesToRuntimeModel
         = new HashMap<com.redhat.ceylon.compiler.typechecker.model.Package, com.redhat.ceylon.compiler.java.runtime.metamodel.FreePackage>();
 
     private static Map<com.redhat.ceylon.compiler.typechecker.model.Module, com.redhat.ceylon.compiler.java.runtime.metamodel.FreeModule> typeCheckModulesToRuntimeModel
         = new HashMap<com.redhat.ceylon.compiler.typechecker.model.Module, com.redhat.ceylon.compiler.java.runtime.metamodel.FreeModule>();
 
     static{
         resetModuleManager();
     }
 
     public static void loadModule(String name, String version, ArtifactResult result, ClassLoader classLoader){
         moduleManager.loadModule(name, version, result, classLoader);
     }
     
     public static void resetModuleManager() {
         RepositoryManagerBuilder builder = new RepositoryManagerBuilder(new Logger(){
 
             @Override
             public void error(String str) {
                 System.err.println("ERROR: "+str);
             }
 
             @Override
             public void warning(String str) {
                 System.err.println("WARN: "+str);
             }
 
             @Override
             public void info(String str) {
                 System.err.println("INFO: "+str);
             }
 
             @Override
             public void debug(String str) {
                 System.err.println("DEBUG: "+str);
             }
             
         }, false);
         RepositoryManager repoManager = builder.buildRepository();
         VFS vfs = new VFS();
         Context context = new Context(repoManager, vfs);
         moduleManager = new RuntimeModuleManager(context);
         moduleManager.initCoreModules();
         moduleManager.prepareForTypeChecking();
         typeCheckModelToRuntimeModel.clear();
         typeCheckModulesToRuntimeModel.clear();
         typeCheckPackagesToRuntimeModel.clear();
     }
 
     public static TypeDescriptor getTypeDescriptor(Object instance) {
         if(instance == null)
             return Null.$TypeDescriptor;
         else if(instance instanceof ReifiedType)
             return((ReifiedType) instance).$getType();
         else
             return null; // FIXME: interop?
     }
     public static boolean isReified(java.lang.Object o, TypeDescriptor type){
         TypeDescriptor instanceType = getTypeDescriptor(o);
         if(instanceType == null)
             return false; // FIXME: interop?
         return instanceType.toProducedType(moduleManager).isSubtypeOf(type.toProducedType(moduleManager));
     }
 
     public static ProducedType getProducedType(Object instance) {
         TypeDescriptor instanceType = getTypeDescriptor(instance);
         if(instanceType == null)
             throw new RuntimeException("Metamodel not yet supported for Java types");
         return instanceType.toProducedType(moduleManager);
     }
 
     public static ceylon.language.model.Type getAppliedMetamodel(TypeDescriptor typeDescriptor) {
         if(typeDescriptor == null)
             throw new RuntimeException("Metamodel not yet supported for Java types");
         ProducedType pt = typeDescriptor.toProducedType(moduleManager);
         return getAppliedMetamodel(pt);
     }
     
     public static com.redhat.ceylon.compiler.java.runtime.metamodel.FreeTopLevelOrMemberDeclaration getOrCreateMetamodel(com.redhat.ceylon.compiler.typechecker.model.Declaration declaration){
         synchronized(typeCheckModelToRuntimeModel){
             com.redhat.ceylon.compiler.java.runtime.metamodel.FreeTopLevelOrMemberDeclaration ret = typeCheckModelToRuntimeModel.get(declaration);
             if(ret == null){
                 if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.Class){
                     com.redhat.ceylon.compiler.typechecker.model.Class klass = (com.redhat.ceylon.compiler.typechecker.model.Class) declaration;
                     ret = new com.redhat.ceylon.compiler.java.runtime.metamodel.FreeClass(klass);
                 }else if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.Interface){
                     com.redhat.ceylon.compiler.typechecker.model.Interface interf = (com.redhat.ceylon.compiler.typechecker.model.Interface)declaration;
                     ret = new com.redhat.ceylon.compiler.java.runtime.metamodel.FreeInterface(interf);
                 }else if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.TypeAlias){
                     com.redhat.ceylon.compiler.typechecker.model.TypeAlias alias = (com.redhat.ceylon.compiler.typechecker.model.TypeAlias)declaration;
                     ret = new com.redhat.ceylon.compiler.java.runtime.metamodel.FreeAliasDeclaration(alias);
                 }else if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.Method){
                     com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration method = (com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration)declaration;
                     ret = new com.redhat.ceylon.compiler.java.runtime.metamodel.FreeFunction(method);
                 }else if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.Value){
                     com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration value = (com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration)declaration;
                     if (value.isVariable()) {
                         ret = new FreeVariable(value);
                     } else {
                         ret = new FreeAttribute(value);
                     }
                 }else{
                     throw new RuntimeException("Declaration type not supported yet: "+declaration);
                 }
                 typeCheckModelToRuntimeModel.put(declaration, ret);
             }
             return ret;
         }
     }
 
     public static boolean hasTypeParameters(com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration model) {
         if(model instanceof com.redhat.ceylon.compiler.typechecker.model.Generic)
             return hasTypeParameters((com.redhat.ceylon.compiler.typechecker.model.Generic)model);
         if(model.getContainer() instanceof com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface)
             return hasTypeParameters((com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface)model.getContainer());
         return false;
     }
     
     public static boolean hasTypeParameters(com.redhat.ceylon.compiler.typechecker.model.Generic model) {
         if(!model.getTypeParameters().isEmpty())
             return true;
         Object container = ((com.redhat.ceylon.compiler.typechecker.model.Declaration)model).getContainer();
         if(container instanceof com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface)
             return hasTypeParameters((com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface) container);
         return false;
     }
 
     public static com.redhat.ceylon.compiler.java.runtime.metamodel.FreePackage getOrCreateMetamodel(com.redhat.ceylon.compiler.typechecker.model.Package declaration){
         synchronized(typeCheckPackagesToRuntimeModel){
             com.redhat.ceylon.compiler.java.runtime.metamodel.FreePackage ret = typeCheckPackagesToRuntimeModel.get(declaration);
             if(ret == null){
                 ret = new com.redhat.ceylon.compiler.java.runtime.metamodel.FreePackage(declaration); 
                 typeCheckPackagesToRuntimeModel.put(declaration, ret);
             }
             return ret;
         }
     }
 
     public static com.redhat.ceylon.compiler.java.runtime.metamodel.FreeModule getOrCreateMetamodel(com.redhat.ceylon.compiler.typechecker.model.Module declaration){
         synchronized(typeCheckModulesToRuntimeModel){
             com.redhat.ceylon.compiler.java.runtime.metamodel.FreeModule ret = typeCheckModulesToRuntimeModel.get(declaration);
             if(ret == null){
                 ret = new com.redhat.ceylon.compiler.java.runtime.metamodel.FreeModule(declaration); 
                 typeCheckModulesToRuntimeModel.put(declaration, ret);
             }
             return ret;
         }
     }
 
     public static ceylon.language.model.declaration.OpenType getMetamodel(ProducedType pt) {
         TypeDeclaration declaration = pt.getDeclaration();
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface){
             return new com.redhat.ceylon.compiler.java.runtime.metamodel.FreeParameterisedType(null, pt);
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.TypeParameter){
             com.redhat.ceylon.compiler.typechecker.model.TypeParameter tp = (com.redhat.ceylon.compiler.typechecker.model.TypeParameter) declaration;
             return new FreeTypeParameterType(tp);
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.UnionType){
             return new FreeUnionType(declaration.getCaseTypes());
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.IntersectionType){
             return new FreeIntersectionType(declaration.getSatisfiedTypes());
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.NothingType){
             return ceylon.language.model.declaration.nothingType_.$get();
         }
         throw new RuntimeException("Declaration type not supported yet: "+declaration);
     }
 
     public static Sequential<? extends ceylon.language.model.declaration.OpenType> getMetamodelSequential(List<ProducedType> types) {
         if(types.isEmpty())
             return (Sequential)empty_.$get();
         ceylon.language.model.declaration.OpenType[] ret = new ceylon.language.model.declaration.OpenType[types.size()];
         int i=0;
         for(ProducedType pt : types){
             ret[i++] = Metamodel.getMetamodel(pt);
         }
         return (Sequential)Util.sequentialInstance(ceylon.language.model.declaration.OpenType.$TypeDescriptor, ret);
     }
 
     public static ceylon.language.model.Type getAppliedMetamodel(ProducedType pt) {
         TypeDeclaration declaration = pt.getDeclaration();
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.Class){
             // anonymous classes don't have parameter lists
             TypeDescriptor reifiedArguments;
             if(!declaration.isAnonymous())
                 reifiedArguments = Metamodel.getTypeDescriptorForArguments(declaration.getUnit(), (Functional)declaration, pt);
             else
                 reifiedArguments = TypeDescriptor.NothingType;
             TypeDescriptor reifiedType = getTypeDescriptorForProducedType(pt);
 
             if(declaration.isToplevel())
                 return new com.redhat.ceylon.compiler.java.runtime.metamodel.AppliedClass(reifiedType, reifiedArguments, pt, null);
             
             TypeDescriptor reifiedContainer = getTypeDescriptorForProducedType(pt.getQualifyingType());
             return new com.redhat.ceylon.compiler.java.runtime.metamodel.AppliedMemberClass(reifiedContainer, reifiedType, reifiedArguments, pt);
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.Interface){
             TypeDescriptor reifiedType = getTypeDescriptorForProducedType(pt);
             if(declaration.isToplevel())
                 return new com.redhat.ceylon.compiler.java.runtime.metamodel.AppliedInterface(reifiedType, pt);
 
             TypeDescriptor reifiedContainer = getTypeDescriptorForProducedType(pt.getQualifyingType());
             return new com.redhat.ceylon.compiler.java.runtime.metamodel.AppliedMemberInterface(reifiedContainer, reifiedType, pt);
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.UnionType){
             TypeDescriptor reifiedType = getTypeDescriptorForProducedType(pt);
             return new AppliedUnionType(reifiedType, declaration.getCaseTypes());
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.IntersectionType){
             TypeDescriptor reifiedType = getTypeDescriptorForProducedType(pt);
             return new AppliedIntersectionType(reifiedType, declaration.getSatisfiedTypes());
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.NothingType){
             return ceylon.language.model.nothingType_.$get();
         }
         throw new RuntimeException("Declaration type not supported yet: "+declaration);
     }
 
     public static java.lang.Class<?> getJavaClass(com.redhat.ceylon.compiler.typechecker.model.Module module) {
         
         String className = module.getNameAsString() + ".module_";
         ReflectionClass classMirror = (ReflectionClass)moduleManager.getModelLoader().lookupClassMirror(module, className);
         return classMirror.klass;
         
     }
     
     public static java.lang.Class<?> getJavaClass(com.redhat.ceylon.compiler.typechecker.model.Package pkg) {
         String className = ((LazyPackage) pkg).getNameAsString()+ ".package_";
         ReflectionClass classMirror = (ReflectionClass)moduleManager.getModelLoader().lookupClassMirror(pkg.getModule(), className);
         return classMirror != null ? classMirror.klass : null;
     }
     
     public static java.lang.Class<?> getJavaClass(com.redhat.ceylon.compiler.typechecker.model.Declaration declaration) {
         if(declaration instanceof LazyClass){
             ReflectionClass classMirror = (ReflectionClass) ((LazyClass) declaration).classMirror;
             return classMirror.klass;
         }
         if(declaration instanceof LazyInterface){
             ReflectionClass classMirror = (ReflectionClass) ((LazyInterface) declaration).classMirror;
             return classMirror.klass;
         }
         if(declaration instanceof LazyMethod){
             ReflectionClass classMirror = (ReflectionClass) ((LazyMethod) declaration).classMirror;
             return classMirror.klass;
         }
         if(declaration instanceof LazyValue){
             ReflectionClass classMirror = (ReflectionClass) ((LazyValue) declaration).classMirror;
             return classMirror.klass;
         }
         if(declaration.getContainer() instanceof com.redhat.ceylon.compiler.typechecker.model.Declaration){
             return getJavaClass((com.redhat.ceylon.compiler.typechecker.model.Declaration)declaration.getContainer());
         }
         throw new RuntimeException("Unsupported declaration type: " + declaration);
     }
 
     public static java.lang.reflect.Method getJavaMethod(com.redhat.ceylon.compiler.typechecker.model.Method declaration) {
         if(declaration instanceof JavaMethod){
             ReflectionMethod methodMirror = (ReflectionMethod) ((JavaMethod) declaration).mirror;
             return (java.lang.reflect.Method) methodMirror.method;
         }
         if(declaration instanceof LazyMethod){
             ReflectionMethod methodMirror = (ReflectionMethod) ((LazyMethod) declaration).getMethodMirror();
             return (java.lang.reflect.Method) methodMirror.method;
         }
         throw new RuntimeException("Unsupported declaration type: " + declaration);
     }
 
     public static TypeDescriptor getTypeDescriptorForProducedType(com.redhat.ceylon.compiler.typechecker.model.ProducedType type) {
         TypeDeclaration declaration = type.getDeclaration();
         if(declaration instanceof LazyClass){
             ReflectionClass classMirror = (ReflectionClass) ((LazyClass) declaration).classMirror;
             TypeDescriptor[] tdArgs = getTypeDescriptorsForProducedTypes(type.getTypeArgumentList());
             TypeDescriptor ret = TypeDescriptor.klass(classMirror.klass, tdArgs);
             if(type.getQualifyingType() != null)
                 return TypeDescriptor.member(getTypeDescriptorForProducedType(type.getQualifyingType()), ret);
             return ret;
         }
         if(declaration instanceof LazyInterface){
             ReflectionClass classMirror = (ReflectionClass) ((LazyInterface) declaration).classMirror;
             TypeDescriptor[] tdArgs = getTypeDescriptorsForProducedTypes(type.getTypeArgumentList());
             TypeDescriptor ret = TypeDescriptor.klass(classMirror.klass, tdArgs);
             if(type.getQualifyingType() != null)
                 return TypeDescriptor.member(getTypeDescriptorForProducedType(type.getQualifyingType()), ret);
             return ret;
         }
         if(declaration instanceof NothingType){
             return TypeDescriptor.NothingType;
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.UnionType){
             TypeDescriptor[] tdArgs = getTypeDescriptorsForProducedTypes(type.getCaseTypes());
             return TypeDescriptor.union(tdArgs);
         }
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.IntersectionType){
             TypeDescriptor[] tdArgs = getTypeDescriptorsForProducedTypes(type.getSatisfiedTypes());
             return TypeDescriptor.intersection(tdArgs);
         }
         throw new RuntimeException("Unsupported declaration type: " + declaration);
     }
 
     private static TypeDescriptor[] getTypeDescriptorsForProducedTypes(List<ProducedType> args) {
         TypeDescriptor[] tdArgs = new TypeDescriptor[args.size()];
         for(int i=0;i<tdArgs.length;i++){
             tdArgs[i] = getTypeDescriptorForProducedType(args.get(i));
         }
         return tdArgs;
     }
 
    public static FreeFunction getMetamodel(Method method) {
         // find its container
         Scope container = method.getContainer();
         if(container instanceof com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface){
             com.redhat.ceylon.compiler.java.runtime.metamodel.FreeClassOrInterface classOrInterface = (FreeClassOrInterface) getOrCreateMetamodel((com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface) container);
             // now find the method
            FreeFunction ret = classOrInterface.findMethod(method.getName());
             if(ret == null)
                 throw new RuntimeException("Failed to find method "+method.getName()+" in "+container);
             return ret;
         }
         throw new RuntimeException("Unsupported method container for "+method.getName()+": "+container);
     }
 
     public static com.redhat.ceylon.compiler.typechecker.model.ProducedType getModel(ceylon.language.model.declaration.OpenType pt) {
         if(pt instanceof FreeParameterisedType)
             return ((FreeParameterisedType)pt).producedType;
         throw new RuntimeException("Unsupported produced type: " + pt);
     }
 
     public static com.redhat.ceylon.compiler.typechecker.model.ProducedType getModel(ceylon.language.model.Type pt) {
         if(pt instanceof AppliedClassOrInterface)
             return ((AppliedClassOrInterface)pt).producedType;
             
         throw new RuntimeException("Unsupported applied produced type: " + pt);
     }
 
     public static com.redhat.ceylon.compiler.typechecker.model.Package getPackage(com.redhat.ceylon.compiler.typechecker.model.Declaration declaration) {
         Scope scope = declaration.getContainer();
         while(scope != null && scope instanceof com.redhat.ceylon.compiler.typechecker.model.Package == false)
             scope = scope.getContainer();
         if(scope == null)
             throw new RuntimeException("Declaration with no package: "+declaration);
         return (com.redhat.ceylon.compiler.typechecker.model.Package)scope;
     }
 
 
     public static java.util.List<com.redhat.ceylon.compiler.typechecker.model.ProducedType> getProducedTypes(Sequential<? extends ceylon.language.model.Type> types) {
         Iterator<?> iterator = types.iterator();
         Object it;
         List<com.redhat.ceylon.compiler.typechecker.model.ProducedType> producedTypes = new LinkedList<com.redhat.ceylon.compiler.typechecker.model.ProducedType>();
         while((it = iterator.next()) != finished_.$get()){
             ceylon.language.model.Type pt = (ceylon.language.model.Type) it;
             com.redhat.ceylon.compiler.typechecker.model.ProducedType modelPt = Metamodel.getModel(pt);
             producedTypes.add(modelPt);
         }
         return producedTypes;
     }
     
     /**
      * returns the java.lang.Class of the given the Ceylon metamodel of 
      * an annotation class.
      */
     public static 
     <Value extends ConstrainedAnnotation<? extends Value, ? extends Values, ? super ProgramElement>, 
     Values, 
     ProgramElement extends Annotated>
     Class<?> getReflectedAnnotationClass(
             ClassOrInterface<? extends ConstrainedAnnotation<? extends Value, ? extends Values, ? super ProgramElement>> annotationType) {
         FreeClassOrInterface freeClass;
         if (annotationType instanceof AppliedClassOrInterface) {
             freeClass = (FreeClassOrInterface)(annotationType.getDeclaration());
         } else {
             freeClass = (FreeClassOrInterface)annotationType;
         }
         final Class<?> refAnnotationClass = getJavaClass(freeClass.declaration);
         return refAnnotationClass;
     }
     
     private static <A extends ceylon.language.model.Annotation<? extends A>> void addAnnotation(
             SequenceBuilder<A> ceylonAnnotations,
             java.lang.annotation.Annotation jAnnotation,
             Predicates.Predicate<A> pred) {
         Class<? extends java.lang.annotation.Annotation> jAnnotationType = jAnnotation.annotationType();
         if (pred != null && pred instanceof Predicates.AnnotationPredicate && !((Predicates.AnnotationPredicate<A>)pred).shouldInstantiate(jAnnotationType)) {
             return;
         }
         if (jAnnotationType.getAnnotation(Ceylon.class) == null) {
             // It's a Java annotation
             addProxyCeylonAnnotation(ceylonAnnotations, jAnnotation);
             return;
         }
         if (jAnnotationType.getName().endsWith("$annotations")) {
             java.lang.annotation.Annotation[] jAnnotations;
             try {
                 jAnnotations = (java.lang.annotation.Annotation[])jAnnotationType.getMethod("value").invoke(jAnnotation);
             } catch (ReflectiveOperationException e) {
                 throw new RuntimeException("While unwrapping a sequenced annotation", e);
             }
             for (java.lang.annotation.Annotation wrapped : jAnnotations) {
                 addAnnotation(ceylonAnnotations, wrapped, pred);
             }
         } else {
             // Find the annotation class
             String annotationName = jAnnotationType.getName();
             if (!annotationName.endsWith("$annotation")) {
                 throw new RuntimeException();
             }
             String className = annotationName.substring(0, annotationName.length() - "$annotation".length());
             java.lang.Class<A> annotationClass;
             try {
                 annotationClass = (java.lang.Class<A>)Class.forName(className, false, jAnnotationType.getClassLoader());
             } catch (ClassNotFoundException e) {
                 throw new RuntimeException("Unable to find annotation class " + className + " for annotation type " + annotationName, e);
             }
             
             // Invoke it with the jAnnotation as the only argument
             try {
                 Constructor<A> constructor = annotationClass.getDeclaredConstructor(jAnnotationType);
                 constructor.setAccessible(true);
                 A cAnnotation = constructor.newInstance(jAnnotation);
                 if (pred.accept(cAnnotation)) {
                     ceylonAnnotations.append(cAnnotation);
                 }
             } catch (ReflectiveOperationException e) {
                 throw new RuntimeException("While reflectively instantiating " + annotationClass, e);
             } 
         }
     }
     
     private static void addProxyCeylonAnnotation(
             SequenceBuilder<? extends ceylon.language.model.Annotation> ceylonAnnotations,
             java.lang.annotation.Annotation jAnnotation) {
         Class<? extends java.lang.annotation.Annotation> jAnnotationType = jAnnotation.annotationType();
         InvocationHandler handler = new InvocationHandler() {
             @Override
             public Object invoke(Object proxy, java.lang.reflect.Method method,
                     Object[] args) throws Throwable {
                 // TODO Auto-generated method stub
                 // 
                 return null;
             }
         };
         java.lang.reflect.Proxy.newProxyInstance(jAnnotationType.getClassLoader(), 
                 new Class[]{jAnnotationType, ceylon.language.model.Annotation.class}, 
                 handler);
     }
     
     public static <A extends ceylon.language.model.Annotation<? extends A>> Sequential<? extends A> annotations(
             TypeDescriptor $reifiedValues,
             Annotated annotated) {
         // TODO If the annotated is not a valid target for the annotationType
         // we can return empty immediately
         Predicates.Predicate<A> predicate = Predicates.isAnnotationOfType($reifiedValues);
         return annotations($reifiedValues, annotated, predicate);
     }
 
     public static <A extends ceylon.language.model.Annotation<? extends A>> Sequential<? extends A> annotations(TypeDescriptor $reifiedValues,
             Annotated annotated, Predicates.Predicate<A> predicate) {
         java.lang.annotation.Annotation[] jAnnotations = ((AnnotationBearing)annotated).$getJavaAnnotations();
         if (jAnnotations == null) {
             throw new RuntimeException("Unable to find java.lang.reflect.AnnotatedElement for " + annotated);
         }
         
         // TODO Fix initial size estimate when query for OptionalAnnotation
         SequenceBuilder<A> ceylonAnnotations = new SequenceBuilder<A>($reifiedValues, jAnnotations.length);
         for (java.lang.annotation.Annotation jAnnotation: jAnnotations) {
             addAnnotation(ceylonAnnotations, jAnnotation, predicate);
         }
         return ceylonAnnotations.getSequence();
     }
 
     public static String getJavaMethodName(Method method) {
         // FIXME: introduce a damn interface for getRealName()
         if(method instanceof JavaMethod)
             return ((JavaMethod)method).getRealName();
         else if(method instanceof LazyMethod){
             return ((LazyMethod)method).getRealMethodName();
         }else
             throw new RuntimeException("Function declaration type not supported yet: "+method);
     }
 
     public static int getFirstDefaultedParameter(List<Parameter> parameters) {
         int i = 0;
         for(Parameter param : parameters){
             if(param.isDefaulted()){
                 return i;
             }
             i++;
         }
         return -1;
     }
 
     public static Sequential<? extends ceylon.language.model.declaration.Module> getModuleList() {
         // FIXME: this probably needs synchronisation to avoid new modules loaded during traversal
         Set<com.redhat.ceylon.compiler.typechecker.model.Module> modules = moduleManager.getContext().getModules().getListOfModules();
         ceylon.language.model.declaration.Module[] array = new ceylon.language.model.declaration.Module[modules.size()];
         int i=0;
         for(com.redhat.ceylon.compiler.typechecker.model.Module module : modules){
             array[i++] = getOrCreateMetamodel(module);
         }
         return Util.sequentialInstance(Module.$TypeDescriptor, array);
     }
 
     public static ceylon.language.model.declaration.Module findLoadedModule(String name, String version) {
         // FIXME: this probably needs synchronisation to avoid new modules loaded during traversal
         com.redhat.ceylon.compiler.typechecker.model.Module module = moduleManager.findLoadedModule(name, version);
         return module != null ? getOrCreateMetamodel(module) : null;
     }
 
     public static Module getDefaultModule() {
         com.redhat.ceylon.compiler.typechecker.model.Module module = moduleManager.getContext().getModules().getDefaultModule();
         return module != null ? getOrCreateMetamodel(module) : null;
     }
 
     public static List<ProducedType> getParameterProducedTypes(List<Parameter> parameters, ProducedReference producedReference) {
         List<ProducedType> parameterProducedTypes = new ArrayList<ProducedType>(parameters.size());
         for(Parameter parameter : parameters){
             ProducedType ft = producedReference.getTypedParameter(parameter).getFullType();
             parameterProducedTypes.add(ft);
         }
         return parameterProducedTypes;
     }
     
     public static boolean isCeylon(com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface declaration){
         if(declaration instanceof LazyClass)
             return ((LazyClass) declaration).isCeylon();
         if(declaration instanceof LazyInterface)
             return ((LazyInterface) declaration).isCeylon();
         throw new RuntimeException("Declaration type not supported: "+declaration);
     }
 
     public static TypeDescriptor getTypeDescriptorForArguments(com.redhat.ceylon.compiler.typechecker.model.Unit unit, 
             com.redhat.ceylon.compiler.typechecker.model.Functional decl, 
             ProducedReference producedReference) {
         
         if(!decl.getParameterLists().isEmpty()){
             List<Parameter> parameters = decl.getParameterLists().get(0).getParameters();
             com.redhat.ceylon.compiler.typechecker.model.ProducedType tupleType 
             = unit.getParameterTypesAsTupleType(parameters, producedReference);
             return Metamodel.getTypeDescriptorForProducedType(tupleType);
         }else{
             return TypeDescriptor.NothingType;
         }
     }
     
     /**
      * This is also used by generated code in the JVM compiler, for type declaration literals.
      * In theory this can only be used for ClassOrInterface or TypeAlias.
      */
     public static ceylon.language.model.declaration.TopLevelOrMemberDeclaration getOrCreateMetamodel(java.lang.Class<?> klass){
         // FIXME: is this really enough?
         String typeName = klass.getName();
         com.redhat.ceylon.compiler.typechecker.model.Module module = moduleManager.findModuleForClass(klass);
         com.redhat.ceylon.compiler.typechecker.model.TypeDeclaration decl = 
                 (com.redhat.ceylon.compiler.typechecker.model.TypeDeclaration) 
                     moduleManager.getModelLoader().getDeclaration(module, typeName, DeclarationType.TYPE);
         return (ceylon.language.model.declaration.TopLevelOrMemberDeclaration) getOrCreateMetamodel(decl);
     }
 
     public static TypeDescriptor getTypeDescriptorForFunction(ProducedReference appliedFunction) {
         return getTypeDescriptorForProducedType(getFunctionReturnType(appliedFunction));
     }
     
     public static ProducedType getFunctionReturnType(ProducedReference appliedFunction) {
         // pull the return type out of the Callable
         ProducedType fullType = appliedFunction.getFullType();
         return fullType.getTypeArgumentList().get(0);
     }
 
     public static String getProducedTypedReferenceString(ProducedTypedReference typedReference) {
         StringBuilder sb = new StringBuilder();
         if(typedReference.getQualifyingType() != null)
             sb.append(typedReference.getQualifyingType().getProducedTypeName()).append(".");
         TypedDeclaration modelDeclaration = typedReference.getDeclaration();
         sb.append(modelDeclaration.getName());
         if(modelDeclaration instanceof Generic){
             Map<com.redhat.ceylon.compiler.typechecker.model.TypeParameter, ProducedType> typeArguments = typedReference.getTypeArguments();
             boolean first = true;
             for(com.redhat.ceylon.compiler.typechecker.model.TypeParameter tp : ((Generic) modelDeclaration).getTypeParameters()){
                 if(first){
                     first = false;
                     sb.append("<");
                 }else
                     sb.append(",");
                 ProducedType typeArgument = typeArguments.get(tp);
                 if(typeArgument != null)
                     sb.append(typeArgument.getProducedTypeName());
                 else
                     sb.append("##MISSING##");
             }
             if(!first)
                 sb.append(">");
         }
         return sb.toString();
     }
 
     public static com.redhat.ceylon.compiler.typechecker.model.Parameter getParameterFromTypedDeclaration(com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration declaration) {
         if(declaration instanceof com.redhat.ceylon.compiler.typechecker.model.MethodOrValue)
             return ((com.redhat.ceylon.compiler.typechecker.model.MethodOrValue) declaration).getInitializerParameter();
         return null;
     }
     
     /**
      * Called when an annotation class is instantiated via an annotation 
      * constructor or annotation callsite to convert the String representation
      * of a Declaration literal back into the corresponding Declaration.
      */
     public static <T extends ceylon.language.model.declaration.Declaration> T parseMetamodelReference(String ref/*, java.lang.Class<?> klass*/) {
         DeclarationParser parser = new DeclarationParser();
         return (T)parser.ref(ref);
     }
     
     /**
      * Called when an annotation class is instantiated via an annotation 
      * constructor or annotation callsite to convert an array of String representations
      * of Declaration literals back into a Sequential of Declarations.
      */
     public static <T extends ceylon.language.model.declaration.Declaration> Sequential<T> parseMetamodelReferences(TypeDescriptor $reifiedElement, String[] refs) {
         DeclarationParser parser = new DeclarationParser();
         ceylon.language.model.declaration.Declaration[] array = new ceylon.language.model.declaration.Declaration[refs.length];
         for (int ii = 0; ii < refs.length; ii++) {
             array[ii] = (T)parser.ref(refs[ii]);
         }
         return ArraySequence.instance($reifiedElement, array);
     }
     
     public static <T> T parseEnumerationReference(java.lang.Class<T> klass) {
         FreeClassOrInterface decl = (FreeClassOrInterface)getOrCreateMetamodel(klass);
         String getterName = Naming.getGetterName(decl.declaration);
         try {
             java.lang.reflect.Method method = klass.getMethod(getterName);
             return (T)method.invoke(null);
         } catch (ReflectiveOperationException e) {
             throw new RuntimeException(e);
         }
     }
     
     public static <T> Sequential<T> parseEnumerationReferences(TypeDescriptor $reifiedElement, java.lang.Class[] refs) {
         Object[] array = new Object[refs.length];
         for (int ii = 0; ii < refs.length; ii++) {
             array[ii] = parseEnumerationReference(refs[ii]);
         }
         return ArraySequence.instance($reifiedElement, array);
     }
 
     public static Sequential<? extends ceylon.language.model.declaration.TypeParameter> getTypeParameters(com.redhat.ceylon.compiler.typechecker.model.Generic declaration) {
         List<com.redhat.ceylon.compiler.typechecker.model.TypeParameter> typeParameters = declaration.getTypeParameters();
         ceylon.language.model.declaration.TypeParameter[] typeParametersArray = new ceylon.language.model.declaration.TypeParameter[typeParameters.size()];
         int i=0;
         for(com.redhat.ceylon.compiler.typechecker.model.TypeParameter tp : typeParameters){
             typeParametersArray[i++] = new com.redhat.ceylon.compiler.java.runtime.metamodel.FreeTypeParameter(tp);
         }
         return (Sequential)Util.sequentialInstance(ceylon.language.model.declaration.TypeParameter.$TypeDescriptor, typeParametersArray);
     }
 
     public static <DeclarationType extends ceylon.language.model.declaration.Declaration>
         DeclarationType findDeclarationByName(Sequential<? extends DeclarationType> declarations, String name) {
         Iterator<? extends DeclarationType> iterator = declarations.iterator();
         Object it;
         while((it = iterator.next()) != finished_.$get()){
             DeclarationType tp = (DeclarationType) it;
             if(tp.getName().equals(name))
                 return tp;
         }
         return null;
     }
 }
