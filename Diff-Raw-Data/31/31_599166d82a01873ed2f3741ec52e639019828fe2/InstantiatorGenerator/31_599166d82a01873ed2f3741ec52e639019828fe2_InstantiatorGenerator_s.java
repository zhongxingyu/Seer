 package cz.cvut.fit.hybljan2.apitestingcg.generator;
 
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIModifier.Modifier;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorJobConfiguration;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Generates instantiators for all nonabstract classes in API.
  * @author Jan Hybl
  */
 public class InstantiatorGenerator extends Generator {
 
     @Override
     public void generate(API api, GeneratorJobConfiguration jobConfiguration) {
         // get all packages in api
         for(APIPackage pkg : api.getPackages()) {
             // get all classes from every package
             for(APIClass cls : pkg.getClasses()) {
                 // filter out all abstract classes
                 if(!cls.getModifiers().contains(Modifier.ABSTRACT)) {
                     ClassGenerator cgen = new ClassGenerator();                                                                       
 
                     cgen.setPackageName(generateName(jobConfiguration.getOutputPackage(), pkg.getName()));
 
                     // Instantiator has to import tested class.
                     cgen.addImport(cls.getFullName());

                     // class header
                     cgen.setName(generateName(configuration.getInstantiatorClassIdentifier(), cls.getName()));
                     
                     // generate constructors
                     List<MethodGenerator> constructors = generateConstructors(cls);
                     cgen.addConstructors(constructors);
 
                     // generate method callers
                     List<MethodGenerator> callers = generateMethodCallers(cls);                
                     cgen.addMethods(callers);
                     
                     // generate fields test methods
                     List<MethodGenerator> fieldTest = generateFieldTests(cls);
                     cgen.addMethods(fieldTest);
 
                     cgen.generateClassFile();
                 }
             }
         }
     }
 
     private List<MethodGenerator> generateConstructors(APIClass cls) {        
         // Constructor testing
         List<MethodGenerator> result = new LinkedList<MethodGenerator>();
         // For every constructor in tested class, create method 
         // createClassXIntance, that return ClassX instance
         for(APIMethod constructor : cls.getConstructors()) {
             // only public constructors can be tested in instantiator
             if(constructor.getModifiers().contains(Modifier.PUBLIC)) {
 
                 // generate constructor
                 MethodGenerator method = new MethodGenerator();
                 method.setModifiers("public");
                 method.setName(generateName(configuration.getCreateInstanceIdentifier(), cls.getName()));
                 method.setReturnType(cls.getName());
                 List<String[]> params = getMethodParamList(constructor);
                 method.setParams(params);
                 method.setBody(generateConstructorBody(cls, getMethodParamNameList(params)));
                 result.add(method);
                 
                 // generate null constructor                                                        
                 try {
                     // nonparam constructor can't be tested with null values.                        
                     if(constructor.getParameters().isEmpty()) 
                         throw new Exception("Nonparam constructor can't be tested with null values.");
                     
                     String cstr = getNullParamString(constructor.getParameters());
 
                     // Check if there is no other same constructor
                     for(APIMethod c : cls.getConstructors()) {
                         if(!c.equals(constructor)) {
                             if(cstr.equals(getNullParamString(c.getParameters())))
                                 throw new Exception("Can't do null constructor.");                                            
                         }
                     }
 
                     MethodGenerator nconst = new MethodGenerator();
                     nconst.setName(generateName(configuration.getCreateNullInstanceIdentifier(),cls.getName()));
                     nconst.setModifiers("public");
                     nconst.setReturnType(cls.getName());
                     nconst.setBody(generateConstructorBody(cls, cstr));                             
                     // add constructor method to the class
                     result.add(nconst);
                 } catch (Exception ex) {}                                                            
             }
         }
         
         // if class extends other class, generate superconstructor
         if(cls.getExtending() != null && cls.getConstructors().size() > 0) {
             MethodGenerator scnstr = new MethodGenerator();
             scnstr.setModifiers("public");
             scnstr.setReturnType(cls.getExtending());
             scnstr.setName(generateName(configuration.getCreateSuperInstanceIdentifier(), scnstr.getReturnType()));
             List<String[]> params = getMethodParamList(cls.getConstructors().first());
             scnstr.setParams(params);
             scnstr.setBody(generateConstructorBody(cls, getMethodParamNameList(params)));
            result.add(scnstr);                    
         }
         
         // if class implements some interface, create instance of the interface
         for(String intName : cls.getImplementing()) {
             MethodGenerator icnstr = new MethodGenerator();
             icnstr.setModifiers("public");
             icnstr.setReturnType(intName);
             icnstr.setName(generateName(configuration.getCreateInterfaceInstanceIdentifier(), icnstr.getReturnType()));
             if(!cls.getConstructors().isEmpty()) { // TODO: Toto zkontrolovat, otestovat, jestli to tak muze byt...
                 List<String[]> params = getMethodParamList(cls.getConstructors().first());
                 icnstr.setParams(params);
                 icnstr.setBody(generateConstructorBody(cls, getMethodParamNameList(params)));
                 result.add(icnstr);
             }                                       
         }
         return result;
     }
 
     private List<MethodGenerator> generateMethodCallers(APIClass cls) {
         List<MethodGenerator> result = new LinkedList<MethodGenerator>();
         // list of callers (methods that test method calling)
         for(APIMethod method : cls.getMethods()) {
             // instantiator can test only public methods
             if(method.getModifiers().contains(Modifier.PUBLIC)) {
                 MethodGenerator callerMethod = new MethodGenerator();
                 List<String[]> params = new LinkedList<String[]>();
 
                 // if method isn't static, add instance param to param list
                 if(!method.getModifiers().contains(Modifier.STATIC)) {
                     params.add(new String[] {cls.getName(), configuration.getInstanceIdentifier()});
                 }
 
                 // add params of tested method
                 params.addAll(getMethodParamList(method));
 
                 callerMethod.setParams(params);
                 callerMethod.setModifiers("public");
                 // generated method return result of test method, so it has to have same return type
                 callerMethod.setReturnType(method.getReturnType());
                 callerMethod.setName(generateName(configuration.getMethodCallIdentifier(), method.getName()));
                 String body = generateCallerBody(method, cls);
                 callerMethod.setBody(body);
                 result.add(callerMethod);
                     
                 // Generate same method with null parameters
                 try {
                     if(method.getParameters().isEmpty())
                         throw new Exception("method with no parameters can't be called with null params.");
                     String nullParamString = getNullParamString(method.getParameters());
 
                     // Check if there is no other same method (with same name and parameters)
                     for(APIMethod mthd : cls.getMethods()) {
                         if(!mthd.equals(method)) {
                             if(nullParamString.equals(getNullParamString(mthd.getParameters())))
                                 throw new Exception("Cant generate null method.");
                         }
                     }
 
                     // Create clone of original test method.
                     MethodGenerator nullCallerMethod = callerMethod.clone();
                     // Set name of clone to nullCaller
                     nullCallerMethod.setName(generateName(configuration.getMethodNullCallIdentifier(), method.getName()));
                     nullCallerMethod.setBody(generateCallerBody(method, cls, nullParamString));
                     // add nullCaller to generated class.
                     result.add(nullCallerMethod);
                 } catch (Exception ex) {
                     /*
                     In some cases, nullCaller can't be generated. First case is if tested method has no parameters.
                     Second case is if in tested class is other method with same name and same parameters.
                     If one of these cases come, exception is thrown and generating of this nullCaller is skipped.
                     */
                 }
             }
         }
         return result;
     }
 
     private List<MethodGenerator> generateFieldTests(APIClass cls) {
         List<MethodGenerator> result = new LinkedList<MethodGenerator>();
         
         for (APIField field : cls.getFields()) {
             // only public fields can be tested in instantiator
             if(field.getModifiers().contains(Modifier.PUBLIC)) {
                 MethodGenerator fmg = new FieldTestMehtodGenerator(cls, field, getInstance(field.getModifiers(), cls) + '.' + field.getName(), configuration);
                 result.add(fmg);
             }
         }
         
         return result;
     }
 
     private String generateConstructorBody(APIClass cls, String cstr) {
         StringBuilder sb = new StringBuilder();
         sb.append("\t\treturn new ").append(cls.getName()).append("(");
         sb.append(cstr);
         sb.append(");\n");       
         return sb.toString();
     }
 
     private String generateCallerBody(APIMethod method, APIClass cls, String paramsString) {
         
         // String builder for creating command line with following structure: ("return ")? instance.method(params);
         StringBuilder cmdSB = new StringBuilder("\t\t");
         
         // if method return something, command starts with return.
         if(!method.getReturnType().equals("void")) cmdSB.append("return ");
         // instance of tested class that will be used to call method.
         cmdSB.append(getInstance(method.getModifiers(), cls));
         cmdSB.append('.');
         // name of tested method
         cmdSB.append(method.getName());
         cmdSB.append('(');
         cmdSB.append(paramsString);
         cmdSB.append(");");
 
         // if method throws any exception, surround command with try-catch command.
         if(!method.getThrown().isEmpty()) {
             cmdSB.insert(0, "\t\ttry {\n\t");
             for(String exception : method.getThrown()) {
                 cmdSB.append("\n\t\t} catch (").append(exception).append(" ex) {");
             }
             cmdSB.append("}\n");
             // Because return statement is surrounded by try-catch block, there have to be another return statement
             // returning default value at the end of generated method. (But only if method isn't void.)
             if(!method.getReturnType().equals("void")) cmdSB.append("\t\t").append("return ").append(getDefaultPrimitiveValue(method.getReturnType())).append(';');
         }
         return cmdSB.toString();
     }
 
     private String generateCallerBody(APIMethod method, APIClass cls) {
         return generateCallerBody(method, cls, getMethodParamNameList(getMethodParamList(method)));
     }
     
     private String getNullParamString(List<String> parameters) {        
         StringBuilder sb = new StringBuilder(); {
             for(String s : parameters) {
                 sb.append(getDefaultPrimitiveValue(s)).append(',');
             }
         }
         if(sb.length() > 0) sb.deleteCharAt(sb.length()-1);
         return sb.toString();
     }
 
 }
