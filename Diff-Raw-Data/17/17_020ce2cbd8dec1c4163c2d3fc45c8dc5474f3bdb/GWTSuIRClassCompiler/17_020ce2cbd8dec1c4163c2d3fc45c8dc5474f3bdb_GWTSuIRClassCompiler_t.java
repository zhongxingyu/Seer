 package gwtsu;
 
 import com.google.gwt.thirdparty.guava.common.collect.Lists;
 import gw.lang.ir.IRAnnotation;
 import gw.lang.ir.IRClass;
 import gw.lang.ir.IRElement;
 import gw.lang.ir.IRExpression;
 import gw.lang.ir.IRStatement;
 import gw.lang.ir.IRSymbol;
 import gw.lang.ir.IRType;
 import gw.lang.ir.expression.IRArithmeticExpression;
 import gw.lang.ir.expression.IRArrayLengthExpression;
 import gw.lang.ir.expression.IRArrayLoadExpression;
 import gw.lang.ir.expression.IRBooleanLiteral;
 import gw.lang.ir.expression.IRCastExpression;
 import gw.lang.ir.expression.IRCharacterLiteral;
 import gw.lang.ir.expression.IRClassLiteral;
 import gw.lang.ir.expression.IRCompositeExpression;
 import gw.lang.ir.expression.IRConditionalAndExpression;
 import gw.lang.ir.expression.IRConditionalOrExpression;
 import gw.lang.ir.expression.IREqualityExpression;
 import gw.lang.ir.expression.IRFieldGetExpression;
 import gw.lang.ir.expression.IRIdentifier;
 import gw.lang.ir.expression.IRInstanceOfExpression;
 import gw.lang.ir.expression.IRMethodCallExpression;
 import gw.lang.ir.expression.IRNegationExpression;
 import gw.lang.ir.expression.IRNewArrayExpression;
 import gw.lang.ir.expression.IRNewExpression;
 import gw.lang.ir.expression.IRNewMultiDimensionalArrayExpression;
 import gw.lang.ir.expression.IRNoOpExpression;
 import gw.lang.ir.expression.IRNotExpression;
 import gw.lang.ir.expression.IRNullLiteral;
 import gw.lang.ir.expression.IRNumericLiteral;
 import gw.lang.ir.expression.IRPrimitiveTypeConversion;
 import gw.lang.ir.expression.IRRelationalExpression;
 import gw.lang.ir.expression.IRStringLiteralExpression;
 import gw.lang.ir.expression.IRTernaryExpression;
 import gw.lang.ir.statement.IRArrayStoreStatement;
 import gw.lang.ir.statement.IRAssignmentStatement;
 import gw.lang.ir.statement.IRBreakStatement;
 import gw.lang.ir.statement.IRCatchClause;
 import gw.lang.ir.statement.IRContinueStatement;
 import gw.lang.ir.statement.IRDoWhileStatement;
 import gw.lang.ir.statement.IREvalStatement;
 import gw.lang.ir.statement.IRFieldDecl;
 import gw.lang.ir.statement.IRFieldSetStatement;
 import gw.lang.ir.statement.IRForEachStatement;
 import gw.lang.ir.statement.IRIfStatement;
 import gw.lang.ir.statement.IRMethodCallStatement;
 import gw.lang.ir.statement.IRMethodStatement;
 import gw.lang.ir.statement.IRMonitorLockAcquireStatement;
 import gw.lang.ir.statement.IRMonitorLockReleaseStatement;
 import gw.lang.ir.statement.IRNoOpStatement;
 import gw.lang.ir.statement.IRReturnStatement;
 import gw.lang.ir.statement.IRStatementList;
 import gw.lang.ir.statement.IRSwitchStatement;
 import gw.lang.ir.statement.IRSyntheticStatement;
 import gw.lang.ir.statement.IRThrowStatement;
 import gw.lang.ir.statement.IRTryCatchFinallyStatement;
 import gw.lang.ir.statement.IRWhileStatement;
 import gw.lang.reflect.Modifier;
 import gw.util.GosuClassUtil;
 
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author kprevas
  */
 public class GWTSuIRClassCompiler {
   private IRClass irClass;
 
   public String compileToJava(IRClass irClass) {
     this.irClass = irClass;
     StringBuilder builder = new StringBuilder();
     builder.append("package ")
             .append(GosuClassUtil.getPackage(irClass.getName()))
             .append(";\n");
     appendClass(builder);
     return builder.toString();
   }
 
   private void appendClass(StringBuilder builder) {
     for (IRAnnotation annotation : irClass.getAnnotations()) {
       appendAnnotation(builder, annotation);
     }
     builder.append(Modifier.toModifierString(
             irClass.getModifiers() & ~Modifier.SYNCHRONIZED));
     if (irClass.getThisType().isInterface()) {
       builder.append(" interface ");
     } else {
       builder.append(" class ");
     }
     builder.append(irClass.getThisType().getRelativeName());
     if (irClass.getSuperType() != null) {
       builder.append(" extends ")
               .append(irClass.getSuperType().getName());
     }
     List<IRType> interfaces = Lists.newArrayList(irClass.getInterfaces());
     Iterator<IRType> iterator = interfaces.iterator();
     while (iterator.hasNext()) {
       IRType type = iterator.next();
       if (type.getName().equals("gw.lang.reflect.gs.IGosuClassObject")) {
         iterator.remove();
       }
     }
     if (!interfaces.isEmpty()) {
       builder.append(" implements ");
       for (int i = 0, interfacesSize = interfaces.size(); i < interfacesSize; i++) {
         if (i > 0) {
           builder.append(", ");
         }
         builder.append(interfaces.get(i).getName());
       }
     }
     builder.append(" {\n");
 //    for (IRClass.InnerClassInfo innerClassInfo : irClass.getInnerClasses()) {
 //      appendClass(builder, innerClassInfo.getInnerClass());
 //    }
     for (IRFieldDecl field : irClass.getFields()) {
       appendField(builder, field);
     }
     for (IRMethodStatement method : irClass.getMethods()) {
       appendMethod(builder, method, irClass);
     }
     builder.append("}");
   }
 
   private void appendField(StringBuilder builder, IRFieldDecl field) {
     for (IRAnnotation annotation : field.getAnnotations()) {
       appendAnnotation(builder, annotation);
     }
     builder.append(Modifier.toModifierString(field.getModifiers()))
             .append(" ")
             .append(field.getType().getName())
             .append(" ")
             .append(field.getName());
     // TODO kcp - initializer?
     builder.append(";\n");
   }
 
   private void appendMethod(StringBuilder builder, IRMethodStatement method, IRClass ownerType) {
     if (method.getName().equals("getIntrinsicType") || method.getName().startsWith("$")) {
       return;
     }
     for (IRAnnotation annotation : method.getAnnotations()) {
       appendAnnotation(builder, annotation);
     }
     builder.append(Modifier.toModifierString(method.getModifiers()))
             .append(" ");
     if (method.getName().equals("<init>")) {
       builder.append(ownerType.getThisType().getRelativeName());
     } else {
       builder.append(method.getReturnType().getName())
               .append(" ")
               .append(method.getName());
     }
     builder.append("(");
     List<IRSymbol> parameters = method.getParameters();
     for (int i = 0, parametersSize = parameters.size(); i < parametersSize; i++) {
       if (i > 0) {
         builder.append(", ");
       }
       IRSymbol parameter = parameters.get(i);
       builder.append(parameter.getType().getName())
               .append(" ")
               .append(parameter.getName());
     }
     builder.append(") ");
     if (!(method.getMethodBody() instanceof IRStatementList)) {
       builder.append("{\n");
     }
     appendStatement(builder, method.getMethodBody());
     if (!(method.getMethodBody() instanceof IRStatementList)) {
       builder.append("}\n");
     }
   }
 
   private void appendStatement(StringBuilder builder, IRStatement statement) {
     if (statement instanceof IRFieldDecl) {
       // TODO kcp
     } else if (statement instanceof IRMethodStatement) {
       // TODO kcp
     } else if (statement instanceof IRArrayStoreStatement) {
       IRArrayStoreStatement arrayStoreStatement = (IRArrayStoreStatement) statement;
       appendExpression(builder, arrayStoreStatement.getTarget());
       builder.append("[");
       appendExpression(builder, arrayStoreStatement.getIndex());
       builder.append("] = ");
       appendExpression(builder, arrayStoreStatement.getValue());
       builder.append(";\n");
     } else if (statement instanceof IRAssignmentStatement) {
       IRAssignmentStatement assignmentStatement = (IRAssignmentStatement) statement;
       builder.append(assignmentStatement.getSymbol().getType().getName())
               .append(" ")
               .append(assignmentStatement.getSymbol().getName())
               .append(" = ");
       appendExpression(builder, assignmentStatement.getValue());
       builder.append(";\n");
     } else if (statement instanceof IRBreakStatement) {
       builder.append("break;\n");
     } else if (statement instanceof IRContinueStatement) {
       builder.append("continue;\n");
     } else if (statement instanceof IRDoWhileStatement) {
       IRDoWhileStatement doWhileStatement = (IRDoWhileStatement) statement;
       builder.append("do ");
       appendStatement(builder, doWhileStatement.getBody());
       builder.append("while(");
       appendExpression(builder, doWhileStatement.getLoopTest());
       builder.append(");\n");
     } else if (statement instanceof IREvalStatement) {
       // TODO kcp
     } else if (statement instanceof IRFieldSetStatement) {
       IRFieldSetStatement fieldSetStatement = (IRFieldSetStatement) statement;
      if (fieldSetStatement.getLhs() != null) {
        appendExpression(builder, fieldSetStatement.getLhs());
        builder.append(".");
      }
      builder.append(fieldSetStatement.getName())
               .append(" = ");
       appendExpression(builder, fieldSetStatement.getRhs());
       builder.append(";\n");
     } else if (statement instanceof IRForEachStatement) {
       IRForEachStatement forEachStatement = (IRForEachStatement) statement;
       builder.append("for (");
       List<IRStatement> initializers = forEachStatement.getInitializers();
       for (int i = 0, initializersSize = initializers.size(); i < initializersSize; i++) {
         if (i > 0) {
           builder.append(", ");
         }
         appendStatement(builder, initializers.get(i));
       }
       builder.append("; ");
       appendExpression(builder, forEachStatement.getLoopTest());
       builder.append("; ");
       List<IRStatement> incrementors = forEachStatement.getIncrementors();
       for (int i = 0, incrementorsSize = incrementors.size(); i < incrementorsSize; i++) {
         if (i > 0) {
           builder.append(", ");
         }
         appendStatement(builder, incrementors.get(i));
       }
       builder.append(") ");
       appendStatement(builder, forEachStatement.getBody());
     } else if (statement instanceof IRIfStatement) {
       IRIfStatement ifStatement = (IRIfStatement) statement;
       builder.append("if (");
       appendExpression(builder, ifStatement.getExpression());
       builder.append(") ");
       appendStatement(builder, ifStatement.getIfStatement());
       if (ifStatement.getElseStatement() != null) {
         builder.append(" else ");
         appendStatement(builder, ifStatement.getElseStatement());
       }
     } else if (statement instanceof IRMethodCallStatement) {
       appendExpression(builder, ((IRMethodCallStatement) statement).getExpression());
       builder.append(";\n");
     } else if (statement instanceof IRMonitorLockAcquireStatement) {
       // TODO kcp
     } else if (statement instanceof IRMonitorLockReleaseStatement) {
       // TODO kcp
     } else if (statement instanceof IRNoOpStatement) {
       // no-op
     } else if (statement instanceof IRReturnStatement) {
       builder.append("return");
       IRReturnStatement returnStatement = (IRReturnStatement) statement;
       if (returnStatement.getReturnValue() != null) {
         builder.append(" ");
         appendExpression(builder, returnStatement.getReturnValue());
       }
       builder.append(";\n");
     } else if (statement instanceof IRStatementList) {
       builder.append("{\n");
       for (IRStatement child : ((IRStatementList) statement).getStatements()) {
         appendStatement(builder, child);
       }
       builder.append("}\n");
     } else if (statement instanceof IRSwitchStatement) {
       // TODO kcp
     } else if (statement instanceof IRSyntheticStatement) {
       // TODO kcp
     } else if (statement instanceof IRThrowStatement) {
       builder.append("throw ");
       appendExpression(builder, ((IRThrowStatement) statement).getException());
       builder.append(";\n");
     } else if (statement instanceof IRTryCatchFinallyStatement) {
       builder.append("try ");
       IRTryCatchFinallyStatement tryCatchFinallyStatement = (IRTryCatchFinallyStatement) statement;
       appendStatement(builder, tryCatchFinallyStatement.getTryBody());
       for (IRCatchClause catchClause : tryCatchFinallyStatement.getCatchStatements()) {
         builder.append(" catch (")
                 .append(catchClause.getIdentifier().getType().getName())
                 .append(" ")
                 .append(catchClause.getIdentifier().getName())
                 .append(") ");
         appendStatement(builder, catchClause.getBody());
       }
       if (tryCatchFinallyStatement.getFinallyBody() != null) {
         builder.append(" finally ");
         appendStatement(builder, tryCatchFinallyStatement.getFinallyBody());
       }
     } else if (statement instanceof IRWhileStatement) {
       builder.append("while (");
       IRWhileStatement whileStatement = (IRWhileStatement) statement;
       appendExpression(builder, whileStatement.getLoopTest());
       builder.append(") ");
       appendStatement(builder, whileStatement.getBody());
     } else {
       throw new IllegalArgumentException("Unknown statement type " + statement.getClass().getSimpleName());
     }
   }
 
   private void appendExpression(StringBuilder builder, IRExpression expression) {
     if (expression instanceof IRArithmeticExpression) {
       IRArithmeticExpression arithmeticExpression = (IRArithmeticExpression) expression;
       appendExpression(builder, arithmeticExpression.getLhs());
       builder.append(" ")
               .append(arithmeticExpression.getOp())
               .append(" ");
       appendExpression(builder, arithmeticExpression.getRhs());
     } else if (expression instanceof IRArrayLengthExpression) {
       appendExpression(builder, ((IRArrayLengthExpression) expression).getRoot());
       builder.append(".length");
     } else if (expression instanceof IRArrayLoadExpression) {
       // TODO kcp
     } else if (expression instanceof IRBooleanLiteral) {
       builder.append(((IRBooleanLiteral) expression).getValue());
     } else if (expression instanceof IRCastExpression) {
       builder.append("((")
               .append(expression.getType().getName())
               .append(") ");
       appendExpression(builder, ((IRCastExpression) expression).getRoot());
       builder.append(")");
     } else if (expression instanceof IRCharacterLiteral) {
       builder.append("'")
               .append(((IRCharacterLiteral) expression).getValue())
               .append("'");
     } else if (expression instanceof IRClassLiteral) {
       builder.append(((IRClassLiteral) expression).getLiteralType().getName());
     } else if (expression instanceof IRCompositeExpression) {
       // TODO kcp
     } else if (expression instanceof IRConditionalAndExpression) {
       IRConditionalAndExpression andExpression = (IRConditionalAndExpression) expression;
       appendExpression(builder, andExpression.getLhs());
       builder.append(" && ");
       appendExpression(builder, andExpression.getRhs());
     } else if (expression instanceof IRConditionalOrExpression) {
       IRConditionalOrExpression orExpression = (IRConditionalOrExpression) expression;
       appendExpression(builder, orExpression.getLhs());
       builder.append(" || ");
       appendExpression(builder, orExpression.getRhs());
     } else if (expression instanceof IREqualityExpression) {
       IREqualityExpression equalityExpression = (IREqualityExpression) expression;
       appendExpression(builder, equalityExpression.getLhs());
       builder.append(" == ");
       appendExpression(builder, equalityExpression.getRhs());
     } else if (expression instanceof IRFieldGetExpression) {
       IRFieldGetExpression fieldGetExpression = (IRFieldGetExpression) expression;
      if (fieldGetExpression.getLhs() != null) {
        appendExpression(builder, fieldGetExpression.getLhs());
        builder.append(".");
      }
      builder.append(fieldGetExpression.getName());
     } else if (expression instanceof IRIdentifier) {
       builder.append(((IRIdentifier) expression).getSymbol().getName());
     } else if (expression instanceof IRInstanceOfExpression) {
       IRInstanceOfExpression instanceOfExpression = (IRInstanceOfExpression) expression;
       appendExpression(builder, instanceOfExpression.getRoot());
       builder.append(" instanceof ")
               .append(instanceOfExpression.getTestType().getName());
     } else if (expression instanceof IRMethodCallExpression) {
       IRMethodCallExpression methodCallExpression = (IRMethodCallExpression) expression;
       boolean skipName = false;
       IRExpression root = methodCallExpression.getRoot();
       if (root != null) {
         if (root instanceof IRIdentifier && ((IRIdentifier) root).getSymbol().getName().equals("this")
                 && methodCallExpression.getName().equals("<init>")) {
           IRElement ancestor = expression.getParent();
           while (ancestor.getParent() != null && !(ancestor instanceof IRMethodStatement)) {
             ancestor = ancestor.getParent();
           }
           if (((IRMethodStatement) ancestor).getName().equals("<init>") &&
                   ((IRMethodStatement) ancestor).getParameters().size() == methodCallExpression.getArgs().size()) {
             // super call??
             builder.append("super(");
           } else {
             builder.append("this(");
           }
           skipName = true;
         } else {
           appendExpression(builder, root);
           builder.append(".");
         }
       } else {
         if (!irClass.getThisType().isAssignableFrom(methodCallExpression.getOwnersType())) {
           builder.append(methodCallExpression.getOwnersType().getName())
                   .append(".");
         }
       }
       if (!skipName) {
         builder.append(methodCallExpression.getName())
                 .append("(");
       }
       List<IRExpression> args = methodCallExpression.getArgs();
       for (int i = 0, argsSize = args.size(); i < argsSize; i++) {
         if (i > 0) {
           builder.append(", ");
         }
         appendExpression(builder, args.get(i));
       }
       builder.append(")");
     } else if (expression instanceof IRNegationExpression) {
       builder.append("-");
       appendExpression(builder, ((IRNegationExpression) expression).getRoot());
     } else if (expression instanceof IRNewArrayExpression) {
       IRNewArrayExpression newArrayExpression = (IRNewArrayExpression) expression;
       builder.append("new ")
               .append(newArrayExpression.getComponentType().getName())
               .append("[");
       if (newArrayExpression.getSizeExpression() != null) {
         appendExpression(builder, newArrayExpression.getSizeExpression());
       }
       builder.append("]");
     } else if (expression instanceof IRNewExpression) {
       IRNewExpression newExpression = (IRNewExpression) expression;
       builder.append("new ")
               .append(newExpression.getOwnersType().getName())
               .append("(");
       List<IRExpression> args = newExpression.getArgs();
       for (int i = 0, argsSize = args.size(); i < argsSize; i++) {
         if (i > 0) {
           builder.append(", ");
         }
         appendExpression(builder, args.get(i));
       }
       builder.append(")");
     } else if (expression instanceof IRNewMultiDimensionalArrayExpression) {
       IRNewMultiDimensionalArrayExpression arrayExpression = (IRNewMultiDimensionalArrayExpression) expression;
       builder.append("new ")
               .append(arrayExpression.getResultType().getName());
       for (IRExpression size : arrayExpression.getSizeExpressions()) {
         builder.append("[");
         appendExpression(builder, size);
         builder.append("]");
       }
     } else if (expression instanceof IRNotExpression) {
       builder.append("!");
       appendExpression(builder, ((IRNotExpression) expression).getRoot());
     } else if (expression instanceof IRNoOpExpression) {
       // no-op
     } else if (expression instanceof IRNullLiteral) {
       builder.append("null");
     } else if (expression instanceof IRNumericLiteral) {
       builder.append(((IRNumericLiteral) expression).getValue());
     } else if (expression instanceof IRPrimitiveTypeConversion) {
       // TODO kcp
     } else if (expression instanceof IRRelationalExpression) {
       IRRelationalExpression relationalExpression = (IRRelationalExpression) expression;
       appendExpression(builder, relationalExpression.getLhs());
       builder.append(" ")
               .append(relationalExpression.getOp())
               .append(" ");
       appendExpression(builder, relationalExpression.getRhs());
     } else if (expression instanceof IRStringLiteralExpression) {
       builder.append("\"")
               .append(((IRStringLiteralExpression) expression).getValue())  // TODO kcp - escape?
               .append("\"");
     } else if (expression instanceof IRTernaryExpression) {
       IRTernaryExpression ternaryExpression = (IRTernaryExpression) expression;
       appendExpression(builder, ternaryExpression.getTest());
       builder.append(" ? ");
       appendExpression(builder, ternaryExpression.getTrueValue());
       builder.append(" : ");
       appendExpression(builder, ternaryExpression.getFalseValue());
     } else {
       throw new IllegalArgumentException("Unknown expression type " + expression.getClass().getSimpleName());
     }
   }
 
   private void appendAnnotation(StringBuilder builder, IRAnnotation annotation) {
     builder.append("@")
             .append(annotation.getDescriptor().getName())
             // TODO kcp - args?
             .append("\n");
   }
 }
