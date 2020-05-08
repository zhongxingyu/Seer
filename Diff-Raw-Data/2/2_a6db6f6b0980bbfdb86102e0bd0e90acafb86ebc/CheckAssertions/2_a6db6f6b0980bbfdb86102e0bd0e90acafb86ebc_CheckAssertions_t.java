 package ist.meic.pa;
 
 import java.lang.reflect.*;
 import java.lang.annotation.*;
 import javassist.*;
 import javassist.expr.*;
 import java.util.Arrays;
 
 public class CheckAssertions {
 
     static class CheckerTranslator implements Translator {
 
         public void start(ClassPool pool) throws NotFoundException, CannotCompileException {}
 
         public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException {
             CtClass clazz = pool.get(className);
             checkBehaviors(clazz);
             reCheckFieldAccess(clazz);
         }
 
         // check all behaviors supported by assertion
         public void checkBehaviors(CtClass clazz) throws NotFoundException, CannotCompileException {
             for (CtBehavior behavior : clazz.getDeclaredBehaviors()){
                 checkFieldAccess(clazz, behavior);
                 // if its a method and its not declared in an abstract or interface class
                 if(!javassist.Modifier.isInterface(clazz.getModifiers()) && (behavior instanceof CtMethod)){
                     CtMethod method = (CtMethod) behavior;
                     String name = method.getName();
                     String[] templates = inheritedAssertions(clazz, name, method.getSignature());
                     if(!Arrays.equals(templates, new String[]{"", ""})){
                         String newName = "m$" + Math.abs(method.hashCode());
                         method.setName(newName);
                         method = CtNewMethod.copy(method, name, clazz, null);
                         method.setBody("return ($r)" + newName + "($$);");
                         clazz.addMethod(method);
                         method.insertBefore(templates[1]);
                        method.insertAfter(templates[0], true);
                     }
                 }
                 // Support for constructors assertions
                 if((behavior instanceof CtConstructor) && hasAssertion(behavior)){
                     CtConstructor constructor = (CtConstructor) behavior;
                     constructor.insertBeforeBody(exprTemplate(getAssertionValues(constructor)));
                 }
             }
         }
 
         // Check Field Accesses in a behavior
         static final String hashSetTemplate = "static java.util.HashSet f$writes = new java.util.HashSet();";
         private void checkFieldAccess(CtClass clazz, CtBehavior behavior)
             throws NotFoundException, CannotCompileException
         {
             // adds 'f$writes' hash to current class to check variable access
             if(getFieldByClassAndName(clazz, "f$writes") == null){
                 CtField writesField = CtField.make(hashSetTemplate, clazz);
                 clazz.addField(writesField);
             }
             // injects code to check assertion and initialization
             behavior.instrument(new ExprEditor() {
                 public void edit(FieldAccess fa) throws CannotCompileException {
                     CtField field = getFieldByFieldAccess(fa);
                     if((field != null) && hasAssertion(field)){
                         if (fa.isReader()){
                             fa.replace(
                                 getInitTemplate("f$writes.contains(($w) " + field.hashCode() + ")", field.getName())
                                 + "$_ = $proceed(); ");
                         }
                         if (fa.isWriter()) {
                             fa.replace( "$proceed($$); "
                                 + "f$writes.add(($w) " + field.hashCode() + "); "
                                 + exprTemplate(getAssertionValues(field)));
                         }
                     }
                 }
             });
         }
 
         // Runs the field instruments again to inject code on the first injected code
         private void reCheckFieldAccess(CtClass clazz) throws NotFoundException, CannotCompileException {
             for (CtBehavior behavior : clazz.getDeclaredBehaviors()){
                 checkFieldAccess(clazz, behavior);
             }
         }
 
         // Returns array with all class-tree value assertions in the first position and entry in the second position
         private String[] inheritedAssertions(CtClass clazz, String methodName, String methodSignature){
             if(clazz != null){
                 String[] templates = inheritedAssertions(getSuperclass(clazz), methodName, methodSignature);
                 CtMethod method = getMethod(clazz, methodName, methodSignature);
                 if((method != null) && hasAssertion(method)){
                     templates[0] += exprTemplate(getAssertionValues(method));
                     templates[1] += getEntryTemplate(getAssertionValues(method));
                 }
                 return templates;
             }
             return new String[] {"", ""};
         }
 
         private String getInitTemplate(String val, String fieldName){
             return abstractTemplate(val, "\"Error: " + fieldName + " was not initialized\"");
         }
 
         static final String msgFormat = "\"The assertion \" + \"%s\" + \" is \" + (%s)";
         private String exprTemplate(String[] val){
             return abstractTemplate(val[0], String.format(msgFormat, val[0], val[0]));
         }
 
         private String getEntryTemplate(String[] val){
             return abstractTemplate(val[1], String.format(msgFormat, val[1], val[1]));
         }
 
         private String abstractTemplate(String val, String msg){
             // return (!val.isEmpty()) ? "if(!" + val + ") throw new RuntimeException(" + msg + ");" : "";
             return (!val.isEmpty()) ? "if(!(" + val + ")) System.out.println(" + msg + "); " : "";
         }
 
         private boolean hasAssertion(CtMember member){
             return member.hasAnnotation(Assertion.class);
         }
 
         // If there is an Assertions, returns the pair of its field [value, entry] otherwise returns null
         private String[] getAssertionValues(CtMember member){
             try{
                 Assertion assertion = (Assertion) member.getAnnotation(Assertion.class);
                 return new String[] { assertion.value(), assertion.entry() };
             } catch (ClassNotFoundException e){
                 return null;
             }
         }
 
         private CtField getFieldByFieldAccess(FieldAccess fa){
             try{
                 return fa.getField();
             } catch (NotFoundException e){
                 return null;
             }
         }
 
         private CtField getFieldByClassAndName(CtClass clazz, String fieldName){
             try{
                 return clazz.getField(fieldName);
             } catch (NotFoundException e){
                 return null;
             }
         }
 
         private CtMethod getMethod(CtClass clazz, String methodName, String methodSignature){
             try{
                 return clazz.getMethod(methodName, methodSignature);
             } catch (NotFoundException e){
                 return null;
             }
         }
 
         private CtClass getSuperclass(CtClass clazz){
             try{
                 return clazz.getSuperclass();
             } catch (NotFoundException e){
                 return null;
             }
         }
     }
 
     public static void main(String[] args) throws Exception, Throwable {
         if(args.length < 1) {
             System.err.println("[ERROR] Invalid command line: one and one only filename expected");
             System.exit(1);
         }
 
         Loader classLoader = new Loader();
         classLoader.addTranslator(ClassPool.getDefault(), new CheckerTranslator());
         String[] restArgs = new String[args.length - 1];
         System.arraycopy(args, 1, restArgs, 0, restArgs.length);
         classLoader.run(args[0], restArgs);
     }
 }
