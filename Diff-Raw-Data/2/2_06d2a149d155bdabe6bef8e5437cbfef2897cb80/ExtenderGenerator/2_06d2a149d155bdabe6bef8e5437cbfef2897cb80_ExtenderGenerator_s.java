 package cz.cvut.fit.hybljan2.apitestingcg.generator;
 
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIItem.Kind;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIModifier.Modifier;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorJobConfiguration;
 
 /**
  *
  * @author Jan Hybl
  */
 public class ExtenderGenerator extends Generator {
 
     @Override
     public void generate(API api, GeneratorJobConfiguration jobConfiguration) {
         for(APIPackage pkg : api.getPackages()) {
             for(APIClass cls : pkg.getClasses()) {
                 // filter out final classes and annotations
                 if(!cls.getModifiers().contains(Modifier.FINAL) && !cls.getType().equals(Kind.ANNOTATION)) {
                     ClassGenerator cgen = new ClassGenerator();
                     cgen.addImport(cls.getFullName());                    
                     cgen.setPackageName(generateName(jobConfiguration.getOutputPackage(), pkg.getName()));
                     
                     String pattern = null;
                     // if tested item is interface, create Implementator, otherwise Extender
                     if(cls.getType() == Kind.INTERFACE) {
                         pattern = configuration.getImplementerClassIdentifier();
                         cgen.addImplemening(cls.getName());
                     } else {
                         pattern = configuration.getExtenderClassIdentifier();
                         cgen.setExtending(cls.getName());
                     }
                     
                     cgen.setName(generateName(pattern, cls.getName()));
                     
                     // constructors tests
                     for(APIMethod constructor : cls.getConstructors()) {
                         MethodGenerator cnstr = new MethodGenerator();
                         cnstr.setModifiers("public");
                         cnstr.setName(cgen.getName());
                         
                         
                         cnstr.setParams(getMethodParamList(constructor));
                         
                         StringBuilder sb = new StringBuilder();
                                                 
                         sb.append("\t\tsuper(").append(getMethodParamNameList(cnstr.getParams())).append(");");
                         
                         cnstr.setBody(sb.toString());
                         cgen.addConstructor(cnstr);
                     }
                     
                     // method overriding tests
                     for(APIMethod method : cls.getMethods()) {
                         // filter out static and final methods - they can't be overriden
                         if(! (method.getModifiers().contains(Modifier.STATIC) || method.getModifiers().contains(Modifier.FINAL))) {
                             MethodGenerator mgen = new MethodGenerator();
                             mgen.setModifiers("public");
                             mgen.setName(method.getName());
                             mgen.setReturnType(method.getReturnType());
 
                             StringBuilder sb = new StringBuilder();
                             for (Modifier m : method.getModifiers()) {
                                 if(!m.equals(Modifier.ABSTRACT)) {
                                     sb.append(m.toString().toLowerCase()).append(" ");
                                 }
                             }                        
                             mgen.setModifiers(sb.toString().trim());
                             mgen.setThrown(method.getThrown());
                             mgen.addAnotation("Override");
                             mgen.setParams(getMethodParamList(method));
                             mgen.setBody("\t\tthrow new UnsupportedOperationException();");
                             cgen.addMethod(mgen);
                         }
                     }
 
                     // protected field tests
                     for(APIField field : cls.getFields()) {
 //                        if(field.getModifiers().contains(Modifier.PROTECTED)) {
                            MethodGenerator ftmg = new FieldTestMehtodGenerator(cls, field, getInstance(field.getModifiers(), cls) + '.' + field.getName(), configuration);
                             cgen.addMethod(ftmg);
 //                        }
                     }
                     cgen.generateClassFile();
                 }
             }
         }
     }
     
 }
