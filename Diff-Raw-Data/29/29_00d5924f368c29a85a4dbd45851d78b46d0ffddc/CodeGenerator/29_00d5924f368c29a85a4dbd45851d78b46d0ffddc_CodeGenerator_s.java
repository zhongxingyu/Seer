 /*
  * CodeGenerator.java
  * Copyright (C) 2012 Pre-Alpha Software
  * All rights reserved.
  */
 
 package com.prealpha.diamond.compiler;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMultimap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ListMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 import com.prealpha.diamond.compiler.analysis.DepthFirstAdapter;
 import com.prealpha.diamond.compiler.node.ABlockStatement;
 import com.prealpha.diamond.compiler.node.ABreakStatement;
 import com.prealpha.diamond.compiler.node.ACaseGroup;
 import com.prealpha.diamond.compiler.node.AClassDeclaration;
 import com.prealpha.diamond.compiler.node.AClassTopLevelStatement;
 import com.prealpha.diamond.compiler.node.AConstructorClassStatement;
 import com.prealpha.diamond.compiler.node.AConstructorDeclaration;
 import com.prealpha.diamond.compiler.node.AContinueStatement;
 import com.prealpha.diamond.compiler.node.ADefaultCaseGroup;
 import com.prealpha.diamond.compiler.node.ADeleteStatement;
 import com.prealpha.diamond.compiler.node.ADoStatement;
 import com.prealpha.diamond.compiler.node.AExpressionStatement;
 import com.prealpha.diamond.compiler.node.AFieldClassStatement;
 import com.prealpha.diamond.compiler.node.AFieldDeclaration;
 import com.prealpha.diamond.compiler.node.AForStatement;
 import com.prealpha.diamond.compiler.node.AFunctionClassStatement;
 import com.prealpha.diamond.compiler.node.AFunctionDeclaration;
 import com.prealpha.diamond.compiler.node.AFunctionTopLevelStatement;
 import com.prealpha.diamond.compiler.node.AIfThenElseStatement;
 import com.prealpha.diamond.compiler.node.AIfThenStatement;
 import com.prealpha.diamond.compiler.node.ALocalDeclaration;
 import com.prealpha.diamond.compiler.node.AReturnStatement;
 import com.prealpha.diamond.compiler.node.ASwitchStatement;
 import com.prealpha.diamond.compiler.node.AVoidFunctionDeclaration;
 import com.prealpha.diamond.compiler.node.AWhileStatement;
 import com.prealpha.diamond.compiler.node.Node;
 import com.prealpha.diamond.compiler.node.PCaseGroup;
 import com.prealpha.diamond.compiler.node.PClassStatement;
 import com.prealpha.diamond.compiler.node.PExpression;
 import com.prealpha.diamond.compiler.node.PFunctionDeclaration;
 import com.prealpha.diamond.compiler.node.PIntegralLiteral;
 import com.prealpha.diamond.compiler.node.PLocalDeclaration;
 import com.prealpha.diamond.compiler.node.PStatement;
 import com.prealpha.diamond.compiler.node.PTopLevelStatement;
 import com.prealpha.diamond.compiler.node.Token;
 
 import java.util.Deque;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.*;
 
 final class CodeGenerator extends ScopeAwareWalker {
     private final List<Exception> exceptionBuffer;
 
     private final Map<Node, TypeToken> types;
 
     private final Map<Node, String> labels;
 
     private final ListMultimap<PTopLevelStatement, String> instructions;
 
     private final Deque<TypedSymbol> stack;
 
     private final Deque<FlowStructure> flowStructures;
 
     private PTopLevelStatement context;
 
     private TypedSymbol expressionResult;
 
     private TypedSymbol returnLocation;
 
     private TypedSymbol thisSymbol;
 
     public CodeGenerator(ScopeAwareWalker scopeSource, List<Exception> exceptionBuffer, Map<Node, TypeToken> types) {
         super(scopeSource);
         this.exceptionBuffer = exceptionBuffer;
         this.types = ImmutableMap.copyOf(types);
         labels = Maps.newHashMap();
         instructions = ArrayListMultimap.create();
         stack = Lists.newLinkedList();
         flowStructures = Lists.newLinkedList();
     }
 
     public List<String> getInstructions() throws SemanticException {
         PTopLevelStatement mainMethod = null;
         for (PTopLevelStatement topLevelStatement : instructions.keySet()) {
             if (topLevelStatement instanceof AFunctionTopLevelStatement) {
                 PFunctionDeclaration functionDeclaration = ((AFunctionTopLevelStatement) topLevelStatement).getFunctionDeclaration();
                 if (functionDeclaration instanceof AVoidFunctionDeclaration) {
                     AVoidFunctionDeclaration voidFunctionDeclaration = (AVoidFunctionDeclaration) functionDeclaration;
                     if (voidFunctionDeclaration.getName().getText().equals("main") && voidFunctionDeclaration.getParameters().isEmpty()) {
                         mainMethod = topLevelStatement;
                     }
                 }
             }
         }
         if (mainMethod == null) {
             throw new SemanticException("no main method located");
         }
 
         List<String> toReturn = Lists.newArrayList();
         Multimap<PTopLevelStatement, String> instructionsCopy = ImmutableMultimap.copyOf(instructions);
         toReturn.addAll(instructionsCopy.get(mainMethod));
         instructionsCopy.removeAll(mainMethod);
        toReturn.add(":crash SET PC crash");
         toReturn.addAll(instructionsCopy.values());
         return toReturn;
     }
 
     @Override
     protected void onExitScope(Node scopeKey) {
         assert (getScope() == getScope(scopeKey));
         reclaimScope(getScope(scopeKey));
         super.onExitScope(scopeKey);
     }
 
     private void reclaimScope(Scope scope) {
         for (LocalSymbol local : Lists.reverse(scope.getLocals())) {
             reclaimLocal(local);
         }
     }
 
     void reclaimScope() {
         for (LocalSymbol local : Lists.reverse(getScope().getLocals())) {
             doReclaimLocal(local);
         }
     }
 
     private void declareLocal(TypedSymbol local) {
         stack.push(local);
         for (int i = 0; i < local.getType().getWidth(); i++) {
             write("SET PUSH 0x0000");
         }
     }
 
     private void reclaimLocal(TypedSymbol local) {
         TypedSymbol popped = stack.pop();
         assert (popped == local);
         doReclaimLocal(local);
     }
 
     private void doReclaimLocal(TypedSymbol local) {
         String widthString = String.format("0x%4x", local.getType().getWidth());
         write("ADD SP " + widthString);
     }
 
     private void generateLabel(Node node) {
         if (!labels.containsKey(node)) {
             class LineNumberFinder extends DepthFirstAdapter {
                 private Token firstToken = null;
                 @Override
                 public void defaultIn(Node node) {
                     if (node instanceof Token && firstToken == null) {
                         firstToken = (Token) node;
                     }
                 }
             }
             LineNumberFinder finder = new LineNumberFinder();
             node.apply(finder);
             labels.put(node, String.format("%s_%s_%s", node.getClass().getSimpleName(), finder.firstToken.getLine(), finder.firstToken.getPos()));
         }
     }
 
     String obtainStartLabel(Node node) {
         generateLabel(node);
         return "start_" + labels.get(node);
     }
 
     String obtainEndLabel(Node node) {
         generateLabel(node);
         return "end_" + labels.get(node);
     }
 
     private void inline(Node subject) {
         write(":" + obtainStartLabel(subject));
         subject.apply(this);
         write(":" + obtainEndLabel(subject));
     }
 
     private String lookup(TypedSymbol symbol, int wordOffset) {
         checkArgument(wordOffset < symbol.getType().getWidth());
         checkArgument(wordOffset >= 0);
         int symbolOffset = 0;
         for (TypedSymbol stackSymbol : stack) {
             if (stackSymbol == symbol) {
                 break;
             } else {
                 symbolOffset += stackSymbol.getType().getWidth();
             }
         }
         if (symbolOffset == 0 && wordOffset == 0) {
             return "[SP]";
         } else {
             return String.format("[SP+%d]", symbolOffset + wordOffset);
         }
     }
     
     void write(String instruction) {
         instructions.put(context, instruction);
     }
 
     private static final class PseudoLocal implements TypedSymbol {
         private final TypeToken type;
 
         public PseudoLocal(TypeToken type) {
             checkNotNull(type);
             this.type = type;
         }
 
         @Override
         public Node getDeclaration() {
             return null;
         }
 
         @Override
         public TypeToken getType() {
             return type;
         }
 
         @Override
         public Set<Modifier> getModifiers() {
             return ImmutableSet.of();
         }
     }
 
     @Override
     public void caseAClassTopLevelStatement(AClassTopLevelStatement topLevelStatement) {
         assert stack.isEmpty();
         context = topLevelStatement;
         inline(topLevelStatement.getClassDeclaration());
         assert stack.isEmpty();
     }
 
     @Override
     public void caseAFunctionTopLevelStatement(AFunctionTopLevelStatement topLevelStatement) {
         assert stack.isEmpty();
         context = topLevelStatement;
         inline(topLevelStatement.getFunctionDeclaration());
         assert stack.isEmpty();
     }
 
     @Override
     public void caseAIfThenStatement(AIfThenStatement statement) {
         evaluateIfThenElse(statement, statement.getCondition(), statement.getThen(), null);
     }
 
     @Override
     public void caseAIfThenElseStatement(AIfThenElseStatement statement) {
         evaluateIfThenElse(statement, statement.getCondition(), statement.getThen(), statement.getElse());
     }
 
     private void evaluateIfThenElse(Node statement, PExpression condition, PStatement thenBody, PStatement elseBody) {
         expressionResult = new PseudoLocal(BooleanTypeToken.INSTANCE);
         declareLocal(expressionResult);
 
         inline(condition);
 
         write("IFN POP 0x0000");
         write("SET PC " + obtainStartLabel(thenBody));
         TypedSymbol popped = stack.pop();
         assert (popped == expressionResult);
         expressionResult = null;
 
         if (elseBody != null) {
             inline(elseBody);
         }
         write("SET PC " + obtainEndLabel(statement));
         inline(thenBody);
     }
 
     @Override
     public void caseAWhileStatement(AWhileStatement statement) {
         flowStructures.push(new WhileFlowStructure(this, statement));
         expressionResult = new PseudoLocal(BooleanTypeToken.INSTANCE);
         declareLocal(expressionResult);
 
         write("SET " + lookup(expressionResult, 0) + " 0x0000");
         inline(statement.getCondition());
 
         write("IFE " + lookup(expressionResult, 0) + " 0x0000");
         write("SET PC " + obtainEndLabel(statement.getBody()));
 
         inline(statement.getBody());
         write("SET PC " + obtainStartLabel(statement.getCondition()));
 
         reclaimLocal(expressionResult);
         expressionResult = null;
         flowStructures.pop();
     }
 
     @Override
     public void caseAForStatement(AForStatement statement) {
         super.inAForStatement(statement);
         flowStructures.push(new ForFlowStructure(this, statement));
 
         inline(statement.getInit());
 
         expressionResult = new PseudoLocal(BooleanTypeToken.INSTANCE);
         declareLocal(expressionResult);
 
         // we actually want the update on top, so skip to the condition
         write("SET PC " + obtainStartLabel(statement.getCondition()));
 
         inline(statement.getUpdate());
 
         write("SET " + lookup(expressionResult, 0) + " 0x0000");
         inline(statement.getCondition());
 
         write("IFE " + lookup(expressionResult, 0) + " 0x0000");
         write("SET PC " + obtainEndLabel(statement.getBody()));
 
         inline(statement.getBody());
         write("SET PC " + obtainStartLabel(statement.getUpdate()));
 
         reclaimLocal(expressionResult);
         expressionResult = null;
         flowStructures.pop();
         super.outAForStatement(statement);
     }
 
     @Override
     public void caseADoStatement(ADoStatement statement) {
         flowStructures.push(new DoFlowStructure(this, statement));
         expressionResult = new PseudoLocal(BooleanTypeToken.INSTANCE);
         declareLocal(expressionResult);
 
         inline(statement.getBody());
 
         write("SET " + lookup(expressionResult, 0) + " 0x0000");
         inline(statement.getCondition());
         write("IFN " + lookup(expressionResult, 0) + " 0x0000");
         write("SET PC " + obtainStartLabel(statement.getBody()));
 
         reclaimLocal(expressionResult);
         expressionResult = null;
         flowStructures.pop();
     }
 
     @Override
     public void caseASwitchStatement(ASwitchStatement statement) {
         flowStructures.push(new SwitchFlowStructure(this, statement));
         expressionResult = new PseudoLocal(types.get(statement.getValue()));
         declareLocal(expressionResult);
 
         inline(statement.getValue());
 
         PCaseGroup defaultCaseGroup = null;
         for (PCaseGroup caseGroup : statement.getBody()) {
             for (PIntegralLiteral literal : getCaseGroupValues(caseGroup)) {
                 try {
                     long value = IntegralTypeToken.parseLiteral(literal).longValue();
                     switch (types.get(statement.getValue()).getWidth()) {
                         case 4:
                             write(String.format("IFE %s 0x%4x", lookup(expressionResult, 3), (value & 0xffff000000000000L) >>> 48));
                             write(String.format("IFE %s 0x%4x", lookup(expressionResult, 2), (value & 0x0000ffff00000000L) >>> 32));
                         case 2:
                             write(String.format("IFE %s 0x%4x", lookup(expressionResult, 1), (value & 0x00000000ffff0000L) >>> 16));
                         case 1:
                             write(String.format("IFE %s 0x%4x", lookup(expressionResult, 0), value & 0x000000000000ffffL));
                             break;
                         default:
                             assert false; // there shouldn't be any other widths
                     }
                     write("SET PC " + obtainStartLabel(caseGroup));
                 } catch (SemanticException sx) {
                     exceptionBuffer.add(sx);
                 }
             }
             if (caseGroup instanceof ADefaultCaseGroup) {
                 assert (defaultCaseGroup == null);
                 defaultCaseGroup = caseGroup;
             }
         }
         if (defaultCaseGroup != null) {
             write("SET PC " + obtainStartLabel(defaultCaseGroup));
         } else if (!statement.getBody().isEmpty()) {
             write("SET PC " + obtainEndLabel(statement.getBody().descendingIterator().next()));
         }
 
         for (PCaseGroup caseGroup : statement.getBody()) {
             inline(caseGroup);
         }
 
         reclaimLocal(expressionResult);
         expressionResult = null;
         flowStructures.pop();
     }
 
     private Iterable<PIntegralLiteral> getCaseGroupValues(PCaseGroup caseGroup) {
         if (caseGroup instanceof ACaseGroup) {
             return ((ACaseGroup) caseGroup).getValues();
         } else if (caseGroup instanceof ADefaultCaseGroup) {
             return ((ADefaultCaseGroup) caseGroup).getValues();
         } else {
             throw new UnsupportedOperationException("unknown case group flavor");
         }
     }
 
     @Override
     public void caseADeleteStatement(ADeleteStatement statement) {
         throw new NoHeapException();
     }
 
     @Override
     public void caseABreakStatement(ABreakStatement statement) {
         boolean flag;
         do {
             if (flowStructures.isEmpty()) {
                 exceptionBuffer.add(new SemanticException(statement, "invalid break"));
                 break;
             }
             flag = flowStructures.pop().onBreak();
         } while (!flag);
     }
 
     @Override
     public void caseAContinueStatement(AContinueStatement statement) {
         boolean flag;
         do {
             if (flowStructures.isEmpty()) {
                 exceptionBuffer.add(new SemanticException(statement, "invalid continue"));
                 break;
             }
             flag = flowStructures.pop().onContinue();
         } while (!flag);
     }
 
     @Override
     public void caseAReturnStatement(AReturnStatement statement) {
         // evaluate the return expression
         expressionResult = new PseudoLocal(types.get(statement.getReturnValue()));
         declareLocal(expressionResult);
 
         inline(statement.getReturnValue());
 
         // copy the pseudo-local into the return location
         switch (types.get(statement.getReturnValue()).getWidth()) {
             case 4:
                 write("SET " + lookup(returnLocation, 3) + " " + lookup(expressionResult, 3));
                 write("SET " + lookup(returnLocation, 2) + " " + lookup(expressionResult, 2));
             case 2:
                 write("SET " + lookup(returnLocation, 1) + " " + lookup(expressionResult, 1));
             case 1:
                 write("SET " + lookup(returnLocation, 0) + " " + lookup(expressionResult, 0));
                 break;
             default:
                 assert false; // there shouldn't be any other widths
         }
 
         reclaimLocal(expressionResult);
         expressionResult = null;
 
         boolean flag;
         do {
             if (flowStructures.isEmpty()) {
                 exceptionBuffer.add(new SemanticException(statement, "invalid return"));
                 break;
             }
             flag = flowStructures.pop().onReturn();
         } while (!flag);
     }
 
     @Override
     public void caseAExpressionStatement(AExpressionStatement statement) {
         inline(statement.getExpression());
     }
 
     @Override
     public void caseABlockStatement(ABlockStatement statement) {
         super.inABlockStatement(statement);
         for (PStatement enclosedStatement : statement.getStatement()) {
             inline(enclosedStatement);
         }
         super.outABlockStatement(statement);
     }
 
     @Override
     public void caseACaseGroup(ACaseGroup caseGroup) {
         for (PStatement enclosedStatement : caseGroup.getBody()) {
             inline(enclosedStatement);
         }
     }
 
     @Override
     public void caseADefaultCaseGroup(ADefaultCaseGroup caseGroup) {
         for (PStatement enclosedStatement : caseGroup.getBody()) {
             inline(enclosedStatement);
         }
     }
 
     @Override
     public void caseAClassDeclaration(AClassDeclaration classDeclaration) {
         super.inAClassDeclaration(classDeclaration);
         assert (thisSymbol == null);
         thisSymbol = new PseudoLocal(new UserDefinedTypeToken(classDeclaration.getName().getText()));
         for (PClassStatement enclosedStatement : classDeclaration.getBody()) {
             inline(enclosedStatement);
         }
         thisSymbol = null;
         super.outAClassDeclaration(classDeclaration);
     }
 
     @Override
     public void caseAFieldClassStatement(AFieldClassStatement classStatement) {
         inline(classStatement.getFieldDeclaration());
     }
 
     @Override
     public void caseAFunctionClassStatement(AFunctionClassStatement classStatement) {
         inline(classStatement.getFunctionDeclaration());
     }
 
     @Override
     public void caseAConstructorClassStatement(AConstructorClassStatement classStatement) {
         inline(classStatement.getConstructorDeclaration());
     }
 
     @Override
     public void caseAFieldDeclaration(AFieldDeclaration declaration) {
         try {
             FieldSymbol symbol = getScope().resolveField(declaration.getName().getText());
             if (symbol.getModifiers().contains(Modifier.STATIC)) {
                 generateLabel(declaration);
                 for (int i = 0; i < symbol.getType().getWidth(); i++) {
                     write(String.format(":%d_%s", i, labels.get(declaration)));
                     write("DAT 0x0000");
                 }
             }
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 
     @Override
     public void caseAFunctionDeclaration(AFunctionDeclaration declaration) {
         evaluateParametrizedDeclaration(declaration, declaration.getName().getText(), declaration.getParameters(), declaration.getBody());
     }
 
     @Override
     public void caseAVoidFunctionDeclaration(AVoidFunctionDeclaration declaration) {
         evaluateParametrizedDeclaration(declaration, declaration.getName().getText(), declaration.getParameters(), declaration.getBody());
     }
 
     @Override
     public void caseAConstructorDeclaration(AConstructorDeclaration declaration) {
         evaluateParametrizedDeclaration(declaration, "new", declaration.getParameters(), declaration.getBody());
     }
 
     private void evaluateParametrizedDeclaration(Node declaration, String name, List<PLocalDeclaration> parameters, List<PStatement> body) {
         assert stack.isEmpty();
         try {
             List<TypeToken> parameterTypes = Lists.transform(parameters, TypeTokenUtil.getDeclarationFunction());
             ParametrizedSymbol symbol;
             if (declaration instanceof AConstructorDeclaration) {
                 symbol = getScope().resolveConstructor(parameterTypes);
             } else {
                 symbol = getScope().resolveFunction(name, parameterTypes);
             }
 
             flowStructures.push(new ParametrizedFlowStructure(this, declaration));
 
             TypeToken returnType = symbol.getReturnType();
             if (returnType != null) {
                 returnLocation = new PseudoLocal(symbol.getReturnType());
                 stack.push(returnLocation);
             }
             if (thisSymbol != null && !symbol.getModifiers().contains(Modifier.STATIC)) {
                 stack.push(thisSymbol);
             }
 
             for (PLocalDeclaration parameterDeclaration : parameters) {
                 inline(parameterDeclaration);
             }
 
             TypedSymbol jsrPointer = new PseudoLocal(IntegralTypeToken.UNSIGNED_SHORT);
             stack.push(jsrPointer);
 
             // in a constructor, we need to create this on the heap before proceeding further
             if (declaration instanceof AConstructorDeclaration) {
                 // however, there is no heap
                 throw new NoHeapException();
             }
 
            onEnterScope(declaration);
             for (PStatement enclosedStatement : body) {
                 inline(enclosedStatement);
             }
            onExitScope(declaration);
             write("SET PC POP");
             TypedSymbol poppedJsr = stack.pop();
             assert (poppedJsr == jsrPointer);
 
             for (LocalSymbol parameter : Lists.reverse(symbol.getParameters())) {
                 TypedSymbol poppedParameter = stack.pop();
                 assert (poppedParameter == parameter);
             }
             if (thisSymbol != null && !symbol.getModifiers().contains(Modifier.STATIC)) {
                 TypedSymbol poppedThis = stack.pop();
                 assert (poppedThis == thisSymbol);
             }
             if (returnLocation != null) {
                 TypedSymbol poppedReturn = stack.pop();
                 assert (poppedReturn == returnLocation);
                 returnLocation = null;
             }
             flowStructures.pop();
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
         assert stack.isEmpty();
     }
 
     @Override
     public void caseALocalDeclaration(ALocalDeclaration declaration) {
         try {
             LocalSymbol symbol = getScope().resolveLocal(declaration.getName().getText());
             declareLocal(symbol);
         } catch (SemanticException sx) {
             exceptionBuffer.add(sx);
         }
     }
 }
