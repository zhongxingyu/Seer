 package de.skuzzle.polly.parsing.ast.visitor;
 
 import java.io.PrintStream;
 
 import de.skuzzle.polly.parsing.Position;
 import de.skuzzle.polly.parsing.ast.Node;
 import de.skuzzle.polly.parsing.ast.Root;
 import de.skuzzle.polly.parsing.ast.declarations.Declaration;
 import de.skuzzle.polly.parsing.ast.declarations.Namespace;
 import de.skuzzle.polly.parsing.ast.expressions.Assignment;
 import de.skuzzle.polly.parsing.ast.expressions.Braced;
 import de.skuzzle.polly.parsing.ast.expressions.Call;
 import de.skuzzle.polly.parsing.ast.expressions.Expression;
 import de.skuzzle.polly.parsing.ast.expressions.NamespaceAccess;
 import de.skuzzle.polly.parsing.ast.expressions.OperatorCall;
 import de.skuzzle.polly.parsing.ast.expressions.VarAccess;
 import de.skuzzle.polly.parsing.ast.expressions.literals.ChannelLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.DateLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.FunctionLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.HelpLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.ListLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.Literal;
 import de.skuzzle.polly.parsing.ast.expressions.literals.LiteralFormatter;
 import de.skuzzle.polly.parsing.ast.expressions.literals.NumberLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.ProductLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.StringLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.TimespanLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.UserLiteral;
 import de.skuzzle.polly.parsing.util.ASTDotBuilder;
 
 
 public class ExpASTVisualizer extends DepthFirstVisitor {
     
     
     private ASTDotBuilder dotBuilder;
     private final LiteralFormatter formatter;
     
     
     
     public ExpASTVisualizer() {
         this.formatter = new LiteralFormatter() {
             
             @Override
             public String formatUser(UserLiteral user) {
                 return DEFAULT.formatUser(user);
             }
             
             
             
             @Override
             public String formatTimespan(TimespanLiteral timespan) {
                 return DEFAULT.formatTimespan(timespan);
             }
             
             
             
             @Override
             public String formatString(StringLiteral string) {
                 return string.getValue();
             }
             
             
             
             @Override
             public String formatNumberLiteral(NumberLiteral number) {
                 return DEFAULT.formatNumberLiteral(number);
             }
             
             
             
             @Override
             public String formatList(ListLiteral listLiteral) {
                 return DEFAULT.formatList(listLiteral);
             }
             
             
             
             @Override
             public String formatFunction(FunctionLiteral functionLiteral) {
                 return "\\" + DEFAULT.formatFunction(functionLiteral);
             }
             
             
             
             @Override
             public String formatDate(DateLiteral date) {
                 return DEFAULT.formatDate(date);
             }
             
             
             
             @Override
             public String formatChannel(ChannelLiteral channel) {
                 return DEFAULT.formatChannel(channel);
             }
 
 
 
             @Override
             public String formatHelp(HelpLiteral helpLiteral) {
                 return DEFAULT.formatHelp(helpLiteral);
             }
         };
     }
     
     
     
     public void visualize(Node root, PrintStream out, Namespace ns) 
             throws ASTTraversalException {
         this.dotBuilder = new ASTDotBuilder(out);
         //this.dotBuilder.printNameSpace(ns);
         root.visit(this);
         this.dotBuilder.finish();
     }
     
     
     
     @Override
     public void visitRoot(Root root) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         this.beforeRoot(root);
         this.dotBuilder.printNode(root, "Root", root.getPosition().toString());
         this.dotBuilder.printNode(root.getCommand(), "Name: " + root.getCommand(), 
                 root.getCommand().getPosition().toString());
         this.dotBuilder.printEdge(root, root.getCommand(), "command");
         for (final Expression exp : root.getExpressions()) {
             exp.visit(this);
             this.dotBuilder.printEdge(root, exp, Unparser.toString(exp, this.formatter));
         }
         this.afterRoot(root);
     }
     
     
     
     @Override
     public void visitCall(Call call) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         
         this.beforeCall(call);
         this.dotBuilder.printExpression("Call", call);
         call.getLhs().visit(this);
         call.getRhs().visit(this);
         
         this.dotBuilder.printEdge(call, call.getLhs(), Unparser.toString(call.getLhs(), this.formatter));
         this.dotBuilder.printEdge(call, call.getRhs(), Unparser.toString(call.getRhs(), this.formatter));
         this.afterCall(call);
     }
     
     
     
     @Override
     public void visitOperatorCall(OperatorCall call) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         
         this.beforeOperatorCall(call);
         this.dotBuilder.printExpression("OpCall: " + call.getOperator().getId(), call);
         call.getLhs().visit(this);
         call.getRhs().visit(this);
         
         this.dotBuilder.printEdge(call, call.getLhs(), Unparser.toString(call.getLhs(), this.formatter));
         this.dotBuilder.printEdge(call, call.getRhs(), Unparser.toString(call.getRhs(), this.formatter));
         this.afterCall(call);
     }
     
     
     
     @Override
     public void visitListLiteral(ListLiteral list) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         this.beforeListLiteral(list);
         this.dotBuilder.printExpression("List", list);
         
         int i = 0;
         for (final Expression exp : list.getContent()) {
             exp.visit(this);
             this.dotBuilder.printEdge(list, exp, "" + (i++));
         }
         this.afterListLiteral(list);
     }
     
     
     
     @Override
     public void visitLiteral(Literal literal) throws ASTTraversalException {
         this.dotBuilder.printExpression(literal.toString(), literal);
     }
     
     
     
     @Override
     public void visitProductLiteral(ProductLiteral product) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         this.beforeProductLiteral(product);
         this.dotBuilder.printExpression("Product", product);
         
         int i = 0;
         for (final Expression exp : product.getContent()) {
             exp.visit(this);
             this.dotBuilder.printEdge(product, exp, "" + (i++));
         }
         this.afterProductLiteral(product);
     }
     
     
     
     @Override
     public void visitFunctionLiteral(FunctionLiteral func) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         this.beforeFunctionLiteral(func);
         
         this.dotBuilder.printExpression("Function", func);
         for (final Declaration param : func.getFormal()) {
             param.visit(this);
             this.dotBuilder.printEdge(func, param, "param");
         }
         func.getBody().visit(this);
         this.dotBuilder.printEdge(func, func.getBody(), "body");
         
         for (final Declaration param : func.getFormal()) {
             for (final VarAccess va : param.getUsage()) {
                 this.dotBuilder.printUsage(va, param);
             }
         }
         
         this.afterFunctionLiteral(func);
     }
 
     
     
     @Override
     public void visitAssignment(Assignment assign) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         this.beforeAssignment(assign);
         this.dotBuilder.printExpression("Assignment", assign);
         assign.getExpression().visit(this);
         this.dotBuilder.printNode(assign.getName(), "Name: " + assign.getName(), 
             assign.getName().getPosition().toString());
         this.dotBuilder.printEdge(assign, assign.getExpression(), "expresion");
         this.dotBuilder.printEdge(assign, assign.getName(), "to");
         this.afterAssignment(assign);
     }
     
     
     
     @Override
     public void visitAccess(NamespaceAccess access) throws ASTTraversalException {
         this.beforeAccess(access);
         this.dotBuilder.printExpression("Namespace Access", access);
         access.getLhs().visit(this);
         access.getRhs().visit(this);
         this.dotBuilder.printEdge(access, access.getLhs(), "LHS");
         this.dotBuilder.printEdge(access, access.getRhs(), "RHS");
         this.afterAccess(access);
     }
     
     
     
     @Override
     public void beforeVarAccess(VarAccess access) throws ASTTraversalException {
         this.dotBuilder.printExpression("VarAccess: " + access.getIdentifier(), access);
     }
     
     
     
     @Override
     public void beforeDecl(Declaration decl) throws ASTTraversalException {
         final StringBuilder b = new StringBuilder();
         b.append("Usage (by name):\\n");
         for (final VarAccess va : decl.getUsage()) {
             b.append(va.getPosition());
             b.append("\\n");
         }
         String pos = decl.getPosition() == Position.NONE 
             ? "" : " (" + decl.getPosition().toString() + ")";
         
         this.dotBuilder.printNode(decl, "Declaration" + pos ,  
             "Name: " + decl.getName(), 
             "Type: " + decl.getType(),
             b.toString());
     }
     
     
     
     @Override
     public void visitBraced(Braced braced) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         this.beforeBraced(braced);
         this.dotBuilder.printExpression("Embraced", braced);
         braced.getExpression().visit(this);
         this.dotBuilder.printEdge(braced, braced.getExpression(), "");
         
         this.afterBraced(braced);
     }
 }
