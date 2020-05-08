 package org.clafer.ast.analysis;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.clafer.common.Check;
 import org.clafer.ast.AstSetTest;
 import org.clafer.ast.AstGlobal;
 import static org.clafer.ast.Asts.*;
 import org.clafer.ast.AstAbstractClafer;
 import org.clafer.ast.AstArithm;
 import org.clafer.ast.AstBoolArithm;
 import org.clafer.ast.AstBoolExpr;
 import org.clafer.ast.AstCard;
 import org.clafer.ast.AstClafer;
 import org.clafer.ast.AstCompare;
 import org.clafer.ast.AstConcreteClafer;
 import org.clafer.ast.AstConstant;
 import org.clafer.ast.AstConstraint;
 import org.clafer.ast.AstDecl;
 import org.clafer.ast.AstExpr;
 import org.clafer.ast.AstExprVisitor;
 import org.clafer.ast.AstIfThenElse;
 import org.clafer.ast.AstIntClafer;
 import org.clafer.ast.AstJoin;
 import org.clafer.ast.AstJoinParent;
 import org.clafer.ast.AstJoinRef;
 import org.clafer.ast.AstLocal;
 import org.clafer.ast.AstMembership;
 import org.clafer.ast.AstMinus;
 import org.clafer.ast.AstNot;
 import org.clafer.ast.AstPrimClafer;
 import org.clafer.ast.AstQuantify;
 import org.clafer.ast.AstSetArithm;
 import org.clafer.ast.AstSetExpr;
 import org.clafer.ast.AstTernary;
 import org.clafer.ast.AstThis;
 import org.clafer.ast.AstUpcast;
 import org.clafer.ast.AstUtil;
 import org.clafer.common.Util;
 
 /**
  * Type checks and creates explicit upcast nodes in the AST. When the
  * expressions are rewritten, the types need to be reanalyzed.
  *
  * @author jimmy
  */
 public class TypeAnalyzer implements Analyzer {
 
     @Override
     public Analysis analyze(Analysis analysis) {
         Map<AstExpr, AstClafer> typeMap = new HashMap<AstExpr, AstClafer>();
         List<AstConstraint> typedConstraints = new ArrayList<AstConstraint>();
         for (AstConstraint constraint : analysis.getConstraints()) {
             AstClafer clafer = constraint.getContext();
             TypeVisitor visitor = new TypeVisitor(clafer, typeMap);
             TypedExpr<AstBoolExpr> typedConstraint = visitor.typeCheck(constraint.getExpr());
             typedConstraints.add(constraint.withExpr(typedConstraint.getExpr()));
         }
         return analysis.setTypeMap(typeMap).setConstraints(typedConstraints);
     }
 
     private static class TypeVisitor implements AstExprVisitor<Void, TypedExpr<?>> {
 
         private final AstClafer context;
         private final Map<AstExpr, AstClafer> typeMap;
 
         TypeVisitor(AstClafer context, Map<AstExpr, AstClafer> typeMap) {
             this.context = context;
             this.typeMap = typeMap;
         }
 
         private <T extends AstExpr> TypedExpr<T> typeCheck(T expr) {
             @SuppressWarnings("unchecked")
             TypedExpr<T> typedExpr = (TypedExpr<T>) expr.accept(this, null);
             return typedExpr;
         }
 
         private <T extends AstExpr> TypedExpr<T>[] typeCheck(T[] exprs) {
             @SuppressWarnings("unchecked")
             TypedExpr<T>[] typeChecked = new TypedExpr[exprs.length];
             for (int i = 0; i < exprs.length; i++) {
                 typeChecked[i] = typeCheck(exprs[i]);
             }
             return typeChecked;
         }
 
         /**
          * Multilevel upcast.
          *
          * @param expr the expression
          * @param target the target type
          * @return the same expression but with the target type
          */
         private AstSetExpr upcastTo(TypedExpr<AstSetExpr> expr, AstClafer target) {
             AstClafer exprType = expr.getType();
             if (exprType.equals(target)) {
                 return expr.getExpr();
             }
             AstSetExpr superExpr = expr.getExpr();
             List<AstAbstractClafer> superTypes = AstUtil.getSupers(exprType);
             for (AstAbstractClafer superType : superTypes) {
                 superExpr = upcast(superExpr, superType);
                 TypedExpr<AstSetExpr> typedSuper = put(superType, superExpr);
                 if (superType.equals(target)) {
                     return typedSuper.getExpr();
                 }
             }
             throw new AnalysisException("Cannot upcast " + expr.getType().getName() + " to " + target.getName());
         }
 
         /**
          * Multilevel upcast.
          *
          * @param exprs the expressions
          * @param target the target type
          * @return the same expressions but with the target type or {@code null}
          * if the upcast is illegal
          */
         private AstSetExpr[] upcastTo(TypedExpr<AstSetExpr>[] exprs, AstClafer target) {
             AstSetExpr[] upcasts = new AstSetExpr[exprs.length];
             for (int i = 0; i < upcasts.length; i++) {
                 upcasts[i] = upcastTo(exprs[i], target);
             }
             return upcasts;
         }
 
         private <T extends AstExpr> TypedExpr<T> put(AstClafer type, T expr) {
             typeMap.put(expr, type);
             return new TypedExpr<T>(type, expr);
         }
 
         @Override
         public TypedExpr<AstThis> visit(AstThis ast, Void a) {
             return put(context, ast);
         }
 
         @Override
         public TypedExpr<AstGlobal> visit(AstGlobal ast, Void a) {
             return put(ast.getType(), ast);
         }
 
         @Override
         public TypedExpr<AstConstant> visit(AstConstant ast, Void a) {
             return put(ast.getType(), ast);
         }
 
         @Override
         public TypedExpr<AstSetExpr> visit(AstJoin ast, Void a) {
             TypedExpr<AstSetExpr> left = typeCheck(ast.getLeft());
             AstConcreteClafer rightType = ast.getRight();
             if (!AstUtil.isTop(rightType)) {
                 AstClafer joinType = rightType.getParent();
                 if (AstUtil.isAssignable(left.getType(), joinType)) {
                     return put(rightType, join(upcastTo(left, joinType), rightType));
                 }
             }
             throw new AnalysisException("Cannot join " + left.getType().getName() + " . " + rightType.getName());
         }
 
         @Override
         public TypedExpr<AstSetExpr> visit(AstJoinParent ast, Void a) {
             TypedExpr<AstSetExpr> children = typeCheck(ast.getChildren());
             if (!(children.getType() instanceof AstConcreteClafer)) {
                 throw new AnalysisException("Cannot join " + children.getType().getName() + " . parent");
             }
             AstConcreteClafer concreteChildrenType = (AstConcreteClafer) children.getType();
             if (AstUtil.isTop(concreteChildrenType)) {
                 throw new AnalysisException("Cannot join " + children.getType().getName() + " . parent");
             }
             return put(concreteChildrenType.getParent(), joinParent(children.getExpr()));
         }
 
         @Override
         public TypedExpr<AstSetExpr> visit(AstJoinRef ast, Void a) {
             TypedExpr<AstSetExpr> deref = typeCheck(ast.getDeref());
             AstClafer derefType = deref.getType();
             while (derefType != null && !derefType.hasRef()) {
                 derefType = derefType.getSuperClafer();
             }
             if (derefType == null) {
                 throw new AnalysisException("Cannot join " + deref.getType().getName() + " . ref");
             }
             return put(derefType.getRef().getTargetType(), joinRef(upcastTo(deref, derefType)));
         }
 
         @Override
         public TypedExpr<AstBoolExpr> visit(AstNot ast, Void a) {
             TypedExpr<AstBoolExpr> expr = typeCheck(ast.getExpr());
             return put(BoolType, not(expr.getExpr()));
         }
 
         @Override
         public TypedExpr<AstSetExpr> visit(AstMinus ast, Void a) {
             TypedExpr<AstSetExpr> expr = typeCheck(ast.getExpr());
             if (!(expr.getType() instanceof AstIntClafer)) {
                 throw new AnalysisException("Cannot -" + expr.getType().getName());
             }
             return put(IntType, minus(expr.getExpr()));
         }
 
         @Override
         public TypedExpr<AstSetExpr> visit(AstCard ast, Void a) {
             TypedExpr<AstSetExpr> set = typeCheck(ast.getSet());
             if (set.getType() instanceof AstPrimClafer) {
                 throw new AnalysisException("Cannot |" + set.getType().getName() + "|");
             }
             return put(IntType, card(set.getExpr()));
         }
 
         @Override
         public TypedExpr<AstBoolExpr> visit(AstSetTest ast, Void a) {
             TypedExpr<AstSetExpr> left = typeCheck(ast.getLeft());
             TypedExpr<AstSetExpr> right = typeCheck(ast.getRight());
             AstClafer unionType = AstUtil.getUnionType(left.getType(), right.getType());
             if (unionType == null) {
                 throw new AnalysisException("Cannot " + left.getType().getName() + " "
                         + ast.getOp().getSyntax() + " " + right.getType().getName());
             }
             return put(BoolType, test(upcastTo(left, unionType), ast.getOp(), upcastTo(right, unionType)));
         }
 
         @Override
         public TypedExpr<AstBoolExpr> visit(AstCompare ast, Void a) {
             TypedExpr<AstSetExpr> left = typeCheck(ast.getLeft());
             TypedExpr<AstSetExpr> right = typeCheck(ast.getRight());
             if (!(left.getType() instanceof AstIntClafer) || !(right.getType() instanceof AstIntClafer)) {
                 throw new AnalysisException("Cannot " + left.getType().getName() + " "
                         + ast.getOp().getSyntax() + " " + right.getType().getName());
             }
             return put(BoolType, compare(left.getExpr(), ast.getOp(), right.getExpr()));
         }
 
         @Override
         public TypedExpr<AstSetExpr> visit(AstArithm ast, Void a) {
             TypedExpr<AstSetExpr>[] operands = typeCheck(ast.getOperands());
             for (TypedExpr<AstSetExpr> operand : operands) {
                 if (!(operand.getType() instanceof AstIntClafer)) {
                     throw new AnalysisException("Cannot "
                             + Util.intercalate(" " + ast.getOp().getSyntax() + " ",
                             AstUtil.getNames(getTypes(operands))));
                 }
             }
             return put(IntType, arithm(ast.getOp(), getSetExprs(operands)));
         }
 
         @Override
         public TypedExpr<?> visit(AstBoolArithm ast, Void a) {
             TypedExpr<AstBoolExpr>[] operands = typeCheck(ast.getOperands());
             return put(BoolType, arithm(ast.getOp(), getBoolExprs(operands)));
         }
 
         @Override
         public TypedExpr<AstSetExpr> visit(AstSetArithm ast, Void a) {
             TypedExpr<AstSetExpr>[] operands = typeCheck(ast.getOperands());
             AstClafer unionType = AstUtil.getUnionType(getTypes(operands));
             if (unionType == null) {
                 throw new AnalysisException("Cannot "
                         + Util.intercalate(" " + ast.getOp().getSyntax() + " ",
                         AstUtil.getNames(getTypes(operands))));
             }
             return put(unionType, arithm(ast.getOp(), upcastTo(operands, unionType)));
         }
 
         @Override
         public TypedExpr<AstBoolExpr> visit(AstMembership ast, Void a) {
             TypedExpr<AstSetExpr> member = typeCheck(ast.getMember());
             TypedExpr<AstSetExpr> set = typeCheck(ast.getSet());
             AstClafer unionType = AstUtil.getUnionType(member.getType(), set.getType());
             if (unionType == null) {
                 throw new AnalysisException("Cannot " + member.getType().getName()
                         + " " + ast.getOp().getSyntax() + " " + set.getType().getName());
             }
             return put(BoolType, membership(
                     upcastTo(member, unionType), ast.getOp(), upcastTo(set, unionType)));
         }
 
         @Override
         public TypedExpr<AstSetExpr> visit(AstTernary ast, Void a) {
             TypedExpr<AstBoolExpr> antecedent = typeCheck(ast.getAntecedent());
             TypedExpr<AstSetExpr> alternative = typeCheck(ast.getAlternative());
             TypedExpr<AstSetExpr> consequent = typeCheck(ast.getConsequent());
             AstClafer unionType = AstUtil.getUnionType(alternative.getType(), consequent.getType());
             if (unionType == null) {
                 throw new AnalysisException("Cannot if " + antecedent.getType().getName() + " then "
                         + consequent.getType().getName() + " else " + alternative.getType().getName());
             }
             return put(unionType, ifThenElse(antecedent.getExpr(),
                     upcastTo(consequent, unionType), upcastTo(alternative, unionType)));
         }
 
         @Override
         public TypedExpr<AstBoolExpr> visit(AstIfThenElse ast, Void a) {
             TypedExpr<AstBoolExpr> antecedent = typeCheck(ast.getAntecedent());
             TypedExpr<AstBoolExpr> alternative = typeCheck(ast.getAlternative());
             TypedExpr<AstBoolExpr> consequent = typeCheck(ast.getConsequent());
             return put(BoolType, ifThenElse(antecedent.getExpr(), alternative.getExpr(), consequent.getExpr()));
         }
 
         @Override
         public TypedExpr<AstSetExpr> visit(AstUpcast ast, Void a) {
             TypedExpr<AstSetExpr> base = typeCheck(ast.getBase());
             AstAbstractClafer to = ast.getTarget();
             if (!AstUtil.isAssignable(base.getType(), to)) {
                 throw new AnalysisException("Cannot upcast from " + base.getType().getName() + " to " + to);
             }
             return put(to, upcast(base.getExpr(), ast.getTarget()));
         }
 
         @Override
         public TypedExpr<AstLocal> visit(AstLocal ast, Void a) {
             return put(AnalysisUtil.notNull(ast + " type not analyzed yet", typeMap.get(ast)), ast);
         }
 
         @Override
         public TypedExpr<AstBoolExpr> visit(AstQuantify ast, Void a) {
             AstDecl[] decls = new AstDecl[ast.getDecls().length];
             for (int i = 0; i < ast.getDecls().length; i++) {
                 AstDecl decl = ast.getDecls()[i];
                 TypedExpr<AstSetExpr> body = typeCheck(decl.getBody());
                 for (AstLocal local : decl.getLocals()) {
                     put(body.getType(), local);
                 }
                decls[i] = decl(decl.getLocals(), body.getExpr());
             }
             TypedExpr<AstBoolExpr> body = typeCheck(ast.getBody());
             return put(BoolType, quantify(ast.getQuantifier(), decls, body.getExpr()));
         }
     }
 
     private static AstClafer[] getTypes(TypedExpr<?>... exprs) {
         AstClafer[] types = new AstClafer[exprs.length];
         for (int i = 0; i < types.length; i++) {
             types[i] = exprs[i].getType();
         }
         return types;
     }
 
     private static <T extends AstBoolExpr> AstBoolExpr[] getBoolExprs(TypedExpr<T>... exprs) {
         AstBoolExpr[] boolExprs = new AstBoolExpr[exprs.length];
         for (int i = 0; i < boolExprs.length; i++) {
             boolExprs[i] = exprs[i].getExpr();
         }
         return boolExprs;
     }
 
     private static <T extends AstSetExpr> AstSetExpr[] getSetExprs(TypedExpr<T>... exprs) {
         AstSetExpr[] setExprs = new AstSetExpr[exprs.length];
         for (int i = 0; i < setExprs.length; i++) {
             setExprs[i] = exprs[i].getExpr();
         }
         return setExprs;
     }
 
     private static class TypedExpr<T extends AstExpr> {
 
         private final AstClafer type;
         private final T expr;
 
         TypedExpr(AstClafer type, T expr) {
             this.type = Check.notNull(type);
             this.expr = Check.notNull(expr);
         }
 
         public AstClafer getType() {
             return type;
         }
 
         public T getExpr() {
             return expr;
         }
     }
 }
