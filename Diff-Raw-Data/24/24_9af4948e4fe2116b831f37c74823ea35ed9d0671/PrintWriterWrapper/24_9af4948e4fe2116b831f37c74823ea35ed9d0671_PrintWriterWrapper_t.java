 package com.chrisgammage.ginjitsu.server;
 
 import com.chrisgammage.ginjitsu.client.AfterInject;
 import com.chrisgammage.ginjitsu.client.GinExtension;
 import com.google.gwt.core.ext.Generator;
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JMethod;
 
 import java.io.PrintWriter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: gammagec
  * Date: 10/29/12
  * Time: 1:22 PM
  */
 public class PrintWriterWrapper extends PrintWriter {
 
   static final private Logger LOG = Logger.getLogger(PrintWriterWrapper.class.getName());
 
   private final PrintWriter pw;
   private final GinExtension[] ginExtensions;
   private final TreeLogger treeLogger;
   private final GeneratorContext generatorContext;
   private boolean lookingForReturn;
   private String returnType;
 
   public PrintWriterWrapper(PrintWriter writer,
                             TreeLogger treeLogger,
                             GeneratorContext generatorContext,
                             GinExtension[] ginExtensions) {
     super(writer);
     this.treeLogger = treeLogger;
     this.generatorContext = generatorContext;
     pw = writer;
     this.ginExtensions = ginExtensions;
   }
 
   public PrintWriter getOriginal() {
     return pw;
   }
 
   @Override
   public void write(String s) {
     if(lookingForReturn && s.contains("return result")) {
      lookingForReturn = false;
       JClassType clazz = generatorContext.getTypeOracle().findType(returnType);
       for(JMethod method : clazz.getOverridableMethods()) {
         if(method.isAnnotationPresent(AfterInject.class)) {
           super.write("((" + returnType + ")result)." + method.getName() + "();\n");
         }
       }
       super.write(s);
       return;
    } else if(s.contains("GWT.create(") || s.contains("return new")) {

      boolean isNew = s.contains("return new");
      String className;
      if(!isNew) {
        int startInd = s.indexOf("GWT.create(") + 11;
        int endInd = s.indexOf(".class);");
        className = s.substring(startInd, endInd);
      } else {
        int startInd = s.indexOf("return new") + 11;
        int endInd = s.indexOf("(");
        className = s.substring(startInd, endInd);
      }
       lookingForReturn = true;
       returnType = className;
      if(className.endsWith("Generated")) {
        super.write(s);
        return;
      }
       if(ginExtensions != null) {
         JClassType clazz = generatorContext.getTypeOracle().findType(className);
         try {
           for(GinExtension extension : ginExtensions) {
             JClassType extClazz =
                     generatorContext.getTypeOracle().findType(extension.clazz().getCanonicalName());
             if(clazz.isAssignableTo(extClazz)) {
               String newClass = runExtension(clazz, extension.generator());
               LOG.log(Level.INFO, "Executing Extension for " +
                       extension.clazz().getCanonicalName());
               LOG.log(Level.INFO, "writing " + s);
               super.write(s.replaceAll(className, newClass));
               return;
             }
           }
         } catch(Exception e) {
           LOG.log(Level.SEVERE, "Caught exception running extension", e);
         }
       }
     }
     super.write(s);
   }
 
   private String runExtension(JClassType clazz, Class<? extends Generator> generator)
           throws Exception {
     Generator gen = generator.newInstance();
     return gen.generate(treeLogger, generatorContext, clazz.getQualifiedSourceName());
   }
 }
