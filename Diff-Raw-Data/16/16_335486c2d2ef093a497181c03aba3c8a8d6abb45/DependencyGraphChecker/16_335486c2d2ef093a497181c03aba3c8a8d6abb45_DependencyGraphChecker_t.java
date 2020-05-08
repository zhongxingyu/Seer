 package org.zwobble.shed.compiler.ordering;
 
 import java.util.Queue;
 import java.util.Set;
 
 import org.zwobble.shed.compiler.CompilerError;
 import org.zwobble.shed.compiler.ordering.errors.UndeclaredDependenciesError;
 import org.zwobble.shed.compiler.parsing.NodeLocations;
 import org.zwobble.shed.compiler.parsing.nodes.DeclarationNode;
 import org.zwobble.shed.compiler.parsing.nodes.FunctionDeclarationNode;
 import org.zwobble.shed.compiler.parsing.nodes.Identity;
 import org.zwobble.shed.compiler.parsing.nodes.StatementNode;
 import org.zwobble.shed.compiler.typechecker.TypeResult;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 import static com.google.common.base.Predicates.not;
 import static com.google.common.collect.Iterables.filter;
 import static org.zwobble.shed.compiler.Eager.transform;
 
 public class DependencyGraphChecker {
     public TypeResult<Void> check(Iterable<? extends StatementNode> statements, DependencyGraph graph, NodeLocations nodeLocations) {
         return new Visitor(filter(statements, isFixedStatement()), graph, nodeLocations).visitAll();
     }
     
     private static Predicate<StatementNode> isFixedStatement() {
         return not(isReorderableStatement());
     }
     
     private static Predicate<StatementNode> isReorderableStatement() {
         return new Predicate<StatementNode>() {
             @Override
             public boolean apply(StatementNode input) {
                 return isReorderableStatement(input);
             }
         };
     }
     
     private static boolean isReorderableStatement(StatementNode statement) {
         return statement instanceof FunctionDeclarationNode;
     }
 
     private static class Visitor {
         private final Set<Identity<StatementNode>> declared = Sets.newHashSet();
         private final Queue<Identity<DeclarationNode>> dependents = Lists.newLinkedList();
         private TypeResult<Void> result = TypeResult.success();
         private final Iterable<? extends StatementNode> fixedStatements;
         private final DependencyGraph graph;
         private final NodeLocations nodeLocations;
         
         public Visitor(Iterable<? extends StatementNode> fixedStatements, DependencyGraph graph, NodeLocations nodeLocations) {
             this.fixedStatements = fixedStatements;
             this.graph = graph;
             this.nodeLocations = nodeLocations;
         }
         
         public TypeResult<Void> visitAll() {
             for (StatementNode statement : fixedStatements) {
                 visitFixedStatement(statement);
             }
             return result;
         }
         
         private void visitFixedStatement(StatementNode statement) {
             for (DeclarationNode dependency : graph.dependenciesOf(statement)) {
                 visitDependency(dependency);
             }
             declared.add(new Identity<StatementNode>(statement));
         }
 
         private void visitDependency(DeclarationNode declaration) {
             if (isAlreadyDeclared(declaration) || (isReorderableStatement(declaration) && isBeingDeclared(declaration))) {
                 return;
             }
             dependents.add(identity(declaration));
             if (isFixedStatement().apply(declaration)) {
                 addDependencyError(declaration);
             } else {
                 for (DeclarationNode dependency : graph.dependenciesOf(declaration)) {
                     visitDependency(dependency);
                 }
             }
            dependents.remove();
         }
 
         private boolean isAlreadyDeclared(DeclarationNode statement) {
             return declared.contains(identity(statement));
         }
 
         private boolean isBeingDeclared(DeclarationNode statement) {
             return dependents.contains(identity(statement));
         }
 
         private void addDependencyError(StatementNode statement) {
             result = result.withErrorsFrom(TypeResult.failure(new CompilerError(
                 nodeLocations.locate(statement),
                 new UndeclaredDependenciesError(transform(dependents, toIdentifier()))
             )));
         }
 
         private Function<Identity<DeclarationNode>, String> toIdentifier() {
             return new Function<Identity<DeclarationNode>, String>() {
                 @Override
                 public String apply(Identity<DeclarationNode> input) {
                     return input.get().getIdentifier();
                 }
             };
         }
     }
 
     private static Identity<DeclarationNode> identity(DeclarationNode declaration) {
         return new Identity<DeclarationNode>(declaration);
     }
 }
