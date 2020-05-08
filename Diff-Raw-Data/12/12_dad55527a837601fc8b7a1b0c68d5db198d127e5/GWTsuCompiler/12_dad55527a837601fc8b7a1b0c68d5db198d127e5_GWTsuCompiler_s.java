 package gwtsu;
 
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.UnableToCompleteException;
 import com.google.gwt.dev.cfg.ModuleDef;
 import com.google.gwt.dev.cfg.ModuleDefLoader;
 import com.google.gwt.thirdparty.guava.common.collect.Maps;
 import com.google.gwt.thirdparty.guava.common.collect.Sets;
 import com.google.gwt.thirdparty.guava.common.io.Files;
 import com.google.gwt.thirdparty.guava.common.io.InputSupplier;
 import gw.lang.Gosu;
 import gw.lang.ir.IRClass;
 import gw.lang.parser.expressions.ITypeLiteralExpression;
 import gw.lang.parser.statements.IClassFileStatement;
 import gw.lang.reflect.IType;
 import gw.lang.reflect.TypeSystem;
 import gw.lang.reflect.gs.IGosuClass;
 import gw.util.GosuClassUtil;
 import org.apache.commons.io.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static gwtsu.CheckedExceptionAnalyzer.ExceptionMap;
 
 /**
  * @author kprevas
  */
 public class GWTsuCompiler {
 
   public static void main(String[] args) throws Exception {
     System.out.println("Pre-compiling Gosu classes...");
     URL[] gwtsuCacheUrls = new URL[1];
     File gwtsuCache = new File("gwtsu-cache");
     Gosu.init(Arrays.asList(new File("src")));
 
     Set<String> entryPoints = Sets.newHashSet();
     for (int i = args.length - 1; i >= 0; i--) {
       String arg = args[i];
       if (arg.startsWith("-")) {
         break;
       } else {
         try {
           ModuleDef moduleDef = ModuleDefLoader.loadFromClassPath(TreeLogger.NULL, args[i]);
           entryPoints.addAll(Arrays.asList(moduleDef.getEntryPointTypeNames()));
         } catch (UnableToCompleteException e) {
           break;
         }
       }
     }
 
     Set<IGosuClass> gosuClasses = Sets.newHashSet();
     for (String entryPoint : entryPoints) {
       IType type = TypeSystem.getByFullNameIfValid(entryPoint);
       if (type instanceof IGosuClass) {
         findReferencedTypes((IGosuClass) type, gosuClasses);
       }
     }
     Class<?> transformerClass = Class.forName("gw.internal.gosu.ir.transform.GosuClassTransformer");
     Class<?> classClass = Class.forName("gw.internal.gosu.parser.IGosuClassInternal");
     Method compile = transformerClass.getMethod("compile", classClass);
     Map<IGosuClass, IRClass> compiledClasses = Maps.newHashMap();
     ExceptionMap exceptionMap = new ExceptionMap();
     for (IGosuClass type : gosuClasses) {
       try {
         compiledClasses.put(type, (IRClass) compile.invoke(null, type));
       } catch (Throwable t) {
         t.printStackTrace();
       }
     }
     for (IGosuClass type : gosuClasses) {
       CheckedExceptionAnalyzer.findCheckedExceptions(type, exceptionMap, compiledClasses);
     }
     for (IGosuClass type : gosuClasses) {
       try {
        String name = type.getBackingClass().getName();
         String packageName = GosuClassUtil.getPackage(name);
         File dir = new File(gwtsuCache, packageName.replace('.', File.separatorChar));
         File javaFile = new File(dir, name.substring(packageName.length() + 1) + ".java");
         if (!javaFile.exists() ||
                 javaFile.lastModified() < type.getSourceFileHandle().getFileTimestamp()) {
           if (javaFile.exists()) {
             javaFile.delete();
           }
           IRClass irClass = (IRClass) compile.invoke(null, type);
           String javaSource = new IRClassCompiler(irClass, exceptionMap).compileToJava();
           dir.mkdirs();
           FileUtils.writeStringToFile(javaFile, javaSource);
         }
       } catch (Throwable t) {
         t.printStackTrace();
       }
     }
     File utilFile = new File(gwtsuCache, "Util.java");
     Files.copy(new InputSupplier<InputStream>() {
       @Override
       public InputStream getInput() throws IOException {
         return GWTsuCompiler.class.getClassLoader().getResourceAsStream("Util.java");
       }
     }, utilFile);
     gwtsuCacheUrls[0] = gwtsuCache.toURI().toURL();
     Thread.currentThread().setContextClassLoader(
             new URLClassLoader(gwtsuCacheUrls, Thread.currentThread().getContextClassLoader()));
     com.google.gwt.dev.Compiler.main(args);
     System.exit(0);
   }
 
   private static void findReferencedTypes(IGosuClass gsClass, Set<IGosuClass> gosuClasses) {
     if (gsClass.getName().startsWith("gwtsu.") || gosuClasses.contains(gsClass)) {
       return;
     }
     gosuClasses.add(gsClass);
     try {
       gsClass.getBackingClass();
     } catch (Exception e) {
       System.err.println(gsClass.getName() + " could not be compiled.");
       e.printStackTrace();
     }
     gsClass = TypeSystem.getPureGenericType(gsClass);
     if (!gsClass.isValid()) {
       System.err.println(gsClass.getName() + " has errors.");
       gsClass.getParseResultsException().printStackTrace();
       return;
     }
 
     IType supertype = gsClass.getSupertype();
     if (supertype instanceof IGosuClass) {
       findReferencedTypes((IGosuClass) supertype, gosuClasses);
     }
     for (IType iface : gsClass.getInterfaces()) {
       if (iface instanceof IGosuClass) {
         findReferencedTypes((IGosuClass) iface, gosuClasses);
       }
     }
     for (IType innerClass : gsClass.getInnerClasses()) {
       if (innerClass instanceof IGosuClass) {
         findReferencedTypes((IGosuClass) innerClass, gosuClasses);
       }
     }
     List<ITypeLiteralExpression> results = new ArrayList<ITypeLiteralExpression>();
     IClassFileStatement classFileStatement = gsClass.getClassStatement().getClassFileStatement();
     if (classFileStatement != null) {
       classFileStatement.getContainedParsedElementsByType(ITypeLiteralExpression.class, results);
       for (ITypeLiteralExpression tl : results) {
         IType type = tl.getType().getType();
         if (type instanceof IGosuClass) {
           findReferencedTypes((IGosuClass) (type.isParameterizedType() ? type.getGenericType() : type),
                   gosuClasses);
         }
       }
     }
 
   }
 
 }
