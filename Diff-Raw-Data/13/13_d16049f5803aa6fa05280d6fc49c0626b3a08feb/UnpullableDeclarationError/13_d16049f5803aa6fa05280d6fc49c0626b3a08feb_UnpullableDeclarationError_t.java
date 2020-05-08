 package org.zwobble.shed.compiler.ordering.errors;
 
 import lombok.Data;
 
 import org.zwobble.shed.compiler.CompilerErrorDescription;
 import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
 import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
 
 @Data
 public class UnpullableDeclarationError implements CompilerErrorDescription {
     private final DeclarationNode declaration;
    private final DeclarationNode declarationDependency;
     private final StatementNode declarationDependent;
     
     @Override
     public String describe() {
        return "Cannot pull declaration up: the declaration of " + declaration.getIdentifier() + " depends on " +
            declarationDependency.getIdentifier() + ", but " + declaration.getIdentifier() + " is used before this";
     }
 }
