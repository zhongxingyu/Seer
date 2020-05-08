 package org.babblelang.engine.impl;
 
 import org.babblelang.parser.BabbleBaseVisitor;
 import org.babblelang.parser.BabbleParser;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class Function extends BabbleBaseVisitor<Object> implements Callable {
     private final BabbleParser.FunctionLiteralContext definition;
     private final Scope closure;
 
     public Function(BabbleParser.FunctionLiteralContext definition, Scope scope) {
         this.definition = definition;
 
         // Compute closure
         ClosureExtractor closureExtractor = new ClosureExtractor();
         closureExtractor.visit(definition);
        this.closure = scope.closure(closureExtractor.getClosureKeys());
         this.closure.define("$recurse", this);
     }
 
     public Scope bindParameters(Interpreter interpreter, Scope parent, Parameters parameters) {
         Scope scope = closure.enter(null);
 
         int count = 0;
         for (BabbleParser.ParameterDeclarationContext parameter : definition.functionType().parametersDeclaration().parameterDeclaration()) {
             String pos = "$" + (count++);
             String name = parameter.ID().getText();
             Object value;
             if (parameters.containsKey(name)) {
                 value = parameters.get(name);
             } else if (parameters.containsKey(pos)) {
                 value = parameters.get(pos);
             } else {
                 throw new IllegalArgumentException("Missing parameter : " + name);
             }
             checkType(parameter, value);
             scope.define(name, value);
         }
         return scope;
     }
 
     public Object call(Interpreter interpreter, Scope scope) {
         return interpreter.visit(definition.block());
     }
 
     private void checkType(BabbleParser.ParameterDeclarationContext parameter, Object value) {
         if (value instanceof Scope) {
             throw new IllegalArgumentException("Parameter type mismatch : " + parameter.ID().getText() + " is expected to be of type " + parameter.type().getText() + ", got " + value.getClass().getCanonicalName());
         }
     }
 
     private static class ClosureExtractor extends BabbleBaseVisitor<Void> {
         private Scope functionScope;
         private Set<String> closureKeys;
 
         public ClosureExtractor() {
             functionScope = new Scope();
             closureKeys = new HashSet<String>();
         }
 
         @Override
         public Void visitPackageStatement(BabbleParser.PackageStatementContext ctx) {
             functionScope = functionScope.enter(ctx.ID().getText());
             super.visitPackageStatement(ctx);
             functionScope = functionScope.leave();
             return null;
         }
 
         @Override
         public Void visitBlock(BabbleParser.BlockContext ctx) {
             functionScope = functionScope.enter(null);
             super.visitBlock(ctx);
             functionScope = functionScope.leave();
             return null;
         }
 
         @Override
         public Void visitFunctionLiteral(BabbleParser.FunctionLiteralContext ctx) {
             functionScope = functionScope.enter(null);
             super.visitFunctionLiteral(ctx);
             functionScope = functionScope.leave();
             return null;
         }
 
         @Override
         public Void visitParameterDeclaration(BabbleParser.ParameterDeclarationContext ctx) {
             functionScope.define(ctx.ID().getText(), true);
             return super.visitParameterDeclaration(ctx);
         }
 
         @Override
         public Void visitDefStatement(BabbleParser.DefStatementContext ctx) {
             super.visitDefStatement(ctx);
             functionScope.define(ctx.ID().getText(), true);
             return null;
         }
 
         @Override
         public Void visitId(BabbleParser.IdContext ctx) {
             String name = ctx.ID().getText();
 
             if (functionScope.isDeclared(name)) {
                 // if the ID has been defined within the function
                 // then we don't need it in the closure
             } else {
                 closureKeys.add(name);
             }
 
             return super.visitId(ctx);
         }

        private Set<String> getClosureKeys() {
            return closureKeys;
        }
     }
 }
