 package de.skuzzle.polly.core.parser.ast.visitor;
 
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.List;
 
 import de.skuzzle.polly.core.parser.ast.Identifier;
 import de.skuzzle.polly.core.parser.ast.Node;
 import de.skuzzle.polly.core.parser.ast.ResolvableIdentifier;
 import de.skuzzle.polly.core.parser.ast.Root;
 import de.skuzzle.polly.core.parser.ast.declarations.Declaration;
 import de.skuzzle.polly.core.parser.ast.expressions.Assignment;
 import de.skuzzle.polly.core.parser.ast.expressions.Braced;
 import de.skuzzle.polly.core.parser.ast.expressions.Call;
 import de.skuzzle.polly.core.parser.ast.expressions.Delete;
 import de.skuzzle.polly.core.parser.ast.expressions.Expression;
 import de.skuzzle.polly.core.parser.ast.expressions.Inspect;
 import de.skuzzle.polly.core.parser.ast.expressions.NamespaceAccess;
 import de.skuzzle.polly.core.parser.ast.expressions.OperatorCall;
 import de.skuzzle.polly.core.parser.ast.expressions.VarAccess;
 import de.skuzzle.polly.core.parser.ast.expressions.Delete.DeleteableIdentifier;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.FunctionLiteral;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.ListLiteral;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.Literal;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.LiteralFormatter;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.ProductLiteral;
 import de.skuzzle.polly.core.parser.ast.lang.Operator.OpType;
 import de.skuzzle.polly.tools.streams.StringBuilderWriter;
 
 
 /**
  * The unparser traverses the AST to re-generate the string that the AST was created from.
  * If using the default literal formatter, the generated string is guaranteed to be 
  * parseable again. If the AST was not transformed in any way, the resulting string will
  * be syntactical equal (disregarding whitespaces) to the string that the AST was created
  * from.
  * 
  * @author Simon Taddiken
  */
 public class Unparser extends DepthFirstVisitor {
     
     /**
      * Formats an AST (or just a sub tree) into a string using a 
      * {@link LiteralFormatter#DEFAULT default literal formatter}. This string is 
      * guaranteed to be parseable as a valid polly command again. 
      * 
      * @param node AST node to format.
      * @return The formatted string.
      */
     public static String toString(Node node) {
         return toString(node, LiteralFormatter.DEFAULT);
     }
     
     
     
     /**
      * Formats an AST (or just a sub tree) into a string using a custom 
      * {@link LiteralFormatter}. Depending on the output of this formatter, the resulting
      * string might not bot parseable again as a valid polly command.
      * 
      * @param node AST node to format.
      * @param formatter Formatter to format occurring literals with. 
      * @return The formatted string.
      */
     public static String toString(Node node, LiteralFormatter formatter) {
         final StringBuilderWriter sbw = new StringBuilderWriter();
         final Unparser unp = new Unparser(new PrintWriter(sbw), formatter);
         try {
             node.visit(unp);
         } catch (ASTTraversalException e) {
             e.printStackTrace();
         }
         return sbw.getBuilder().toString();
     }
 
     
     
     private final PrintWriter out;
     private final LiteralFormatter literalFormatter;
     
 
     
     /**
      * Creates a new Unparser.
      * 
      * @param out Writer to write the output to.
      * @param formatter Formatter to format occuring literals with.
      */
     public Unparser(PrintWriter out, LiteralFormatter formatter) {
         this.out = out;
         this.literalFormatter = formatter;
     }
     
     
     
     /**
      * Creates a new Unparser which uses a default literal formatter.
     * @param out Writer to write the output to.
      */
     public Unparser(PrintWriter out) {
         this(out, LiteralFormatter.DEFAULT);
     }
     
     
     
     @Override
     public int before(Braced node) throws ASTTraversalException {
         this.out.print("(");
         return CONTINUE;
     }
     
     
     
     @Override
     public int after(Braced node) throws ASTTraversalException {
         this.out.print(")");
         return CONTINUE;
     }
     
     
     
     @Override
     public int before(Declaration node) throws ASTTraversalException {
         node.getType().getName().visit(this);
         this.out.print(" ");
         node.getName().visit(this);
         return CONTINUE;
     }
     
     
     
     @Override
     public boolean visit(Root node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }
         
         this.out.print(":");
         if (!node.getCommand().visit(this)) {
             return false;
         }
         
         if (!node.getExpressions().isEmpty()) {
             this.out.print(" ");
             final Iterator<Expression> it = node.getExpressions().iterator();
             while (it.hasNext()) {
                 if (!it.next().visit(this)) {
                     return false;
                 }
                 if (it.hasNext()) {
                     this.out.print(" ");
                 }
             }
         }
         return this.after(node) == CONTINUE;
     }
     
     
     
     @Override
     public boolean visit(FunctionLiteral node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }
         
         this.out.print(node.format(this.literalFormatter));
         
         return this.after(node) == CONTINUE;
     }
     
     
     
     @Override
     public boolean visit(Literal node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }
         this.out.print(node.format(this.literalFormatter));
         return this.after(node) == CONTINUE;
     }
     
     
     
     @Override
     public boolean visit(ListLiteral node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }        
         this.out.print(node.format(this.literalFormatter));
         
         return this.after(node) == CONTINUE;
     }
     
     
     
     @Override
     public boolean visit(ProductLiteral node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }        
         final Iterator<Expression> it = node.getContent().iterator();
         while (it.hasNext()) {
             if (!it.next().visit(this)) {
                 return false;
             }
             if (it.hasNext()) {
                 this.out.print(",");
             }
         }
         return this.after(node) == CONTINUE;
     }
     
     
     
     @Override
     public int before(Identifier node) throws ASTTraversalException {
         if (node.wasEscaped()) {
             this.out.print("\\");
         }
         this.out.print(node.getId());
         return CONTINUE;
     }
     
     
     
     @Override
     public int before(ResolvableIdentifier node) throws ASTTraversalException {
         this.before((Identifier) node);
         return CONTINUE;
     }
     
     
     
     @Override
     public boolean visit(OperatorCall node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }
         
         final List<Expression> content = node.getRhs().getContent();
         final Iterator<Expression> it = content.iterator();
         
         if (content.size() == 1) {
             if (node.isPostfix()) {
                 if (!it.next().visit(this)) {
                     return false;
                 }
                 this.out.print(node.getOperator().getId());
             } else {
                 this.out.print(node.getOperator().getId());
                 if (!it.next().visit(this)) {
                     return false;
                 }
             }
         } else if (content.size() == 2) {
             // HACK: special treatment for certain operators
             if (node.getOperator() == OpType.INDEX) {
                 if (!it.next().visit(this)) {
                     return false;
                 }
                 this.out.print("[");
                 if (!it.next().visit(this)) {
                     return false;
                 }
                 this.out.print("]");
             } else {
                 if (!it.next().visit(this)) {
                     return false;
                 }
                 this.out.print(node.getOperator().getId());
                 if (!it.next().visit(this)) {
                     return false;
                 }
             }
         } else if (content.size() == 3) {
             if (node.getOperator() == OpType.IF) {
                 this.out.print("if ");
                 if (!it.next().visit(this)) {
                     return false;
                 }
                 this.out.print(" ? ");
                 if (!it.next().visit(this)) {
                     return false;
                 }
                 this.out.print(" : ");
                 if (!it.next().visit(this)) {
                     return false;
                 }
             } else if (node.getOperator() == OpType.DOTDOT) {
                 if (!it.next().visit(this)) {
                     return false;
                 }
                 this.out.print("..");
                 if (!it.next().visit(this)) {
                     return false;
                 }
                 this.out.print("$");
                 if (!it.next().visit(this)) {
                     return false;
                 }
             }
         }
         return this.after(node) == CONTINUE; 
     }
     
     
     
     @Override
     public boolean visit(Call node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }
         
         if (!node.getLhs().visit(this)) {
             return false;
         }
         this.out.print("(");
         if (!node.getRhs().visit(this)) {
             return false;
         }
         this.out.print(")");
         
         return this.after(node) == CONTINUE;
     }
     
     
     
     @Override
     public boolean visit(NamespaceAccess node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }
         if (!node.getLhs().visit(this)) {
             return false;
         }
         this.out.print(".");
         if (!node.getRhs().visit(this)) {
             return false;
         }
         return this.after(node) == CONTINUE; 
     }
     
     
     
     @Override
     public boolean visit(VarAccess node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }
         if (!node.getIdentifier().visit(this)) {
             return false;
         }
         return this.after(node) == CONTINUE;
     }
     
     
     
     @Override
     public boolean visit(Assignment node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }
         if (!node.getExpression().visit(this)) {
             return false;
         }
         this.out.print("->");
         if (!node.getName().visit(this)) {
             return false;
         }
         return this.after(node) == CONTINUE; 
     }
 
     
     
     @Override
     public boolean visit(Delete node) throws ASTTraversalException {
         switch (this.before(node)) {
         case SKIP: return true;
         case ABORT: return false;
         }
         this.out.print("del(");
         final Iterator<DeleteableIdentifier> it = node.getIdentifiers().iterator();
         while (it.hasNext()) {
             final DeleteableIdentifier next = it.next();
             if (next.isGlobal()) {
                 this.out.print("public ");
             }
             if (!next.visit(this)) {
                 return false;
             }
             if (it.hasNext()) {
                 this.out.print(",");
             }
         }
         this.out.print(")");
         return this.after(node) == CONTINUE;
     }
     
     
     
     @Override
     public int before(Inspect node) throws ASTTraversalException {
         this.out.print("inspect(");
         return CONTINUE;
     }
     
     
     
     @Override
     public int after(Inspect node) throws ASTTraversalException {
         this.out.print(")");
         return CONTINUE;
     }
 }
