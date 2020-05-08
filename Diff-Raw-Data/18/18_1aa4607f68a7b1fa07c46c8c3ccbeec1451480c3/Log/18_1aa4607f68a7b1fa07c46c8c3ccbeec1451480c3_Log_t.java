 package com.pelzer.util;
 
 import groovy.lang.GroovyClassLoader;
 import org.codehaus.groovy.ast.ClassHelper;
 import org.codehaus.groovy.ast.ClassNode;
 import org.codehaus.groovy.ast.FieldNode;
 import org.codehaus.groovy.ast.expr.*;
 import org.codehaus.groovy.transform.GroovyASTTransformationClass;
 import org.codehaus.groovy.transform.LogASTTransformation;
 import org.objectweb.asm.Opcodes;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 @java.lang.annotation.Documented
 @Retention(RetentionPolicy.SOURCE)
 @Target({ElementType.TYPE})
 @GroovyASTTransformationClass("org.codehaus.groovy.transform.LogASTTransformation")
 public @interface Log{
   String value() default "log";
   Class<? extends LogASTTransformation.LoggingStrategy> loggingStrategy() default PelzerUtilLoggingStrategy.class;
 
   /**
    * This class contains the logic of how to weave a Pelzer Util Logging logger into the host class.
    */
   public static class PelzerUtilLoggingStrategy extends LogASTTransformation.AbstractLoggingStrategy{
 
     private static final ClassNode LOGGING_CLASSNODE = ClassHelper.make(com.pelzer.util.Logging.class);
     private static final ClassNode LOGGER_CLASSNODE = ClassHelper.make(com.pelzer.util.Logging.Logger.class);
 
     protected PelzerUtilLoggingStrategy(final GroovyClassLoader loader){
       super(loader);
     }
 
     public FieldNode addLoggerFieldToClass(ClassNode classNode, String logFieldName){
       return classNode.addField(logFieldName,
              Opcodes.ACC_FINAL | Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE,
               LOGGER_CLASSNODE,
               new MethodCallExpression(
                       new ClassExpression(LOGGING_CLASSNODE), "getLogger",
                       new ConstantExpression(classNode.getName())));
     }
 
     public boolean isLoggingMethod(String methodName){
       return methodName.matches("fatal|error|warn|info|debug");
     }
 
     public Expression wrapLoggingMethodCall(Expression logVariable, String methodName, Expression originalExpression){
       if("debug".equals(methodName)){
         return new TernaryExpression(
                new BooleanExpression(new MethodCallExpression(logVariable, "isDebugEnabled", ArgumentListExpression.EMPTY_ARGUMENTS)),
                 originalExpression,
                ConstantExpression.EMPTY_EXPRESSION);
       }else if("info".equals(methodName)){
         return new TernaryExpression(
                new BooleanExpression(new MethodCallExpression(logVariable, "isInfoEnabled", ArgumentListExpression.EMPTY_ARGUMENTS)),
                 originalExpression,
                ConstantExpression.EMPTY_EXPRESSION);
      }else //*/
         return originalExpression;
     }
   }
 }
