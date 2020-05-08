 package de.skuzzle.polly.parsing.ast.visitor.resolving;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import de.skuzzle.polly.parsing.ast.ResolvableIdentifier;
 import de.skuzzle.polly.parsing.ast.declarations.Declaration;
 import de.skuzzle.polly.parsing.ast.declarations.Namespace;
 import de.skuzzle.polly.parsing.ast.declarations.types.ListTypeConstructor;
 import de.skuzzle.polly.parsing.ast.declarations.types.MapTypeConstructor;
 import de.skuzzle.polly.parsing.ast.declarations.types.ProductTypeConstructor;
 import de.skuzzle.polly.parsing.ast.declarations.types.Type;
 import de.skuzzle.polly.parsing.ast.declarations.types.TypeUnifier;
 import de.skuzzle.polly.parsing.ast.expressions.Assignment;
 import de.skuzzle.polly.parsing.ast.expressions.Call;
 import de.skuzzle.polly.parsing.ast.expressions.Empty;
 import de.skuzzle.polly.parsing.ast.expressions.Expression;
 import de.skuzzle.polly.parsing.ast.expressions.NamespaceAccess;
 import de.skuzzle.polly.parsing.ast.expressions.Native;
 import de.skuzzle.polly.parsing.ast.expressions.OperatorCall;
 import de.skuzzle.polly.parsing.ast.expressions.VarAccess;
 import de.skuzzle.polly.parsing.ast.expressions.literals.FunctionLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.literals.ListLiteral;
 import de.skuzzle.polly.parsing.ast.expressions.parameters.FunctionParameter;
 import de.skuzzle.polly.parsing.ast.expressions.parameters.ListParameter;
 import de.skuzzle.polly.parsing.ast.expressions.parameters.Parameter;
 import de.skuzzle.polly.parsing.ast.visitor.ASTTraversalException;
 import de.skuzzle.polly.parsing.ast.visitor.Unparser;
 
 
 /**
  * This visitor resolves <b>all</b> possible types for an expression and stores them in
  * each expression's <i>types</i> attribute. A Second pass type resolval is needed to 
  * determine each expression's unique type.
  * 
  * @author Simon Taddiken
  * @see SecondPassTypeResolver
  */
 class FirstPassTypeResolver extends AbstractTypeResolver {
     
     
     
     public FirstPassTypeResolver(Namespace namespace) {
         super(namespace);
     }
     
     
     
     @Override
     public void beforeNative(Native hc) throws ASTTraversalException {
         hc.resolveType(this.nspace, this);
     }
     
     
     
     @Override
     public void afterParameter(Parameter param) throws ASTTraversalException {
         if (!param.typeResolved()) {
             param.setUnique(Type.resolve(param.getTypeName()));
         }
     }
     
     
     
     @Override
     public void afterListParameter(ListParameter param) throws ASTTraversalException {
         if (!param.typeResolved()) {
             final Type t = new ListTypeConstructor(
                 Type.resolve(param.getTypeName()));
             param.setUnique(t);
         }
     }
     
     
     
     @Override
     public void visitFunctionParameter(FunctionParameter param)
             throws ASTTraversalException {
         if (this.aborted || param.typeResolved()) {
             return;
         }
         
         this.beforeFunctionParameter(param);
         
         final Iterator<ResolvableIdentifier> types = param.getSignature().iterator();
         final Type returnType = Type.resolve(types.next());
         final List<Type> sig = new ArrayList<Type>(param.getSignature().size());
         while (types.hasNext()) {
             sig.add(Type.resolve(types.next()));
         }
         
         param.setUnique(
             new MapTypeConstructor(new ProductTypeConstructor(sig), returnType));
         
         this.afterFunctionParameter(param);
     }
     
     
     
     @Override
     public void visitFunctionLiteral(FunctionLiteral func) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         
         this.beforeFunctionLiteral(func);
         
         // resolve parameter types
         this.nspace = this.enter();
         for (final Parameter p : func.getFormal()) {
             p.visit(this);
             
             // Invariant: parameters always have a unique type
             this.nspace.declare(p.getDeclaration());
         }
         
         // resolve functions body type
         func.getExpression().visit(this);
         this.nspace = this.leave();
         
         for (final Parameter p : func.getFormal()) {
             p.getTypes().clear();
             for (final VarAccess va : p.getUsage()) {
                 p.addTypes(va.getTypes());
             }
         }
         
         boolean allChecked = false;
         final int[] indizes = new int[func.getFormal().size()];
         final boolean done[] = new boolean[func.getFormal().size()];
         
         
         while (!allChecked) {
             allChecked = true;
             
             final List<Type> types = new ArrayList<Type>();
             int i = 0;
             for (final Parameter p : func.getFormal()) {
                 types.add(p.getTypes().get(indizes[i]));
                 
                 done[i] = (indizes[i] + 1) == p.getTypes().size();
                 allChecked &= done[i];
                 indizes[i] = (indizes[i] + 1) % p.getTypes().size();
                 ++i;
             }
         
             final ProductTypeConstructor source = new ProductTypeConstructor(types);
             for (final Type target : func.getExpression().getTypes()) {
                 func.addType(new MapTypeConstructor(source, target));
             }
         }
         
         this.afterFunctionLiteral(func);
     }
     
     
     
     @Override
     public void afterListLiteral(ListLiteral list) throws ASTTraversalException {
         for (final Expression exp : list.getContent()) {
             for (final Type t : exp.getTypes()) {
                 list.addType(new ListTypeConstructor(t));
             }
         }
     }
     
     
     
     @Override
     public void afterAssignment(Assignment assign) throws ASTTraversalException {
         for (final Type t : assign.getExpression().getTypes()) {
             final Declaration vd = new Declaration(assign.getName().getPosition(), 
                 assign.getName(), new Empty(t, assign.getExpression().getPosition()));
             this.nspace.declare(vd);
             
             assign.addType(t);
         }
     }
     
     
     
     @Override
     public void afterOperatorCall(OperatorCall call) throws ASTTraversalException {
         this.afterCall(call);
     }
     
     
     
     @Override
     public void afterCall(Call call) throws ASTTraversalException {
         boolean allChecked = false;
         final int[] indizes = new int[call.getParameters().size()];
         final boolean done[] = new boolean[call.getParameters().size()];
         
         final List<Type> newLhsTypes = new ArrayList<Type>();
         
         // combine all possible parameter types
         while (!allChecked) {
             allChecked = true;
             
             final List<Type> types = new ArrayList<Type>();
             int i = 0;
             for (final Expression exp : call.getParameters()) {
                 types.add(exp.getTypes().get(indizes[i]));
                 
                 done[i] = (indizes[i] + 1) == exp.getTypes().size();
                 allChecked &= done[i];
                 indizes[i] = (indizes[i] + 1) % exp.getTypes().size();
                 ++i;
             }
             
             // one possible actual signature type
             final ProductTypeConstructor s = new ProductTypeConstructor(types);
             // one possible lhs type with unknown target type
             MapTypeConstructor p = new MapTypeConstructor(s, Type.newTypeVar());
             
             
             for (final Type lhsType : call.getLhs().getTypes()) {
                 
                 // HINT: do only substitute TypeVars in p
                 final TypeUnifier unifier = new TypeUnifier(p, lhsType);
                 if (unifier.isUnifiable()) {
                     unifier.substituteBoth();
                     
                    MapTypeConstructor mtc = (MapTypeConstructor) unifier.getFirst();
                     
                    call.addSignatureType(mtc.getSource());
                    call.addType(mtc.getTarget());
                     newLhsTypes.add(unifier.getFirst());
                 }
             }
         }
         
         call.getLhs().setTypes(newLhsTypes);
         if (call.getTypes().isEmpty()) {
             this.reportError(call.getLhs(),
                 "Keine passende Deklaration fr den Aufruf von " + 
                 Unparser.toString(call.getLhs()) + " gefunden");
         }
     }
     
     
     
     @Override
     public void beforeVarAccess(VarAccess access) throws ASTTraversalException {
         access.addTypes(this.nspace.lookup(access));
     }
     
     
     
     @Override
     public void visitAccess(NamespaceAccess access) throws ASTTraversalException {
         if (this.aborted) {
             return;
         }
         
         this.beforeAccess(access);
         
         if (!(access.getLhs() instanceof VarAccess)) {
             this.reportError(access.getLhs(), "Operand muss ein Bezeichner sein");
         }
         
         final VarAccess va = (VarAccess) access.getLhs();
         final Namespace last = this.nspace;
         this.nspace = Namespace.forName(va.getIdentifier()).derive(this.nspace);
         access.getRhs().visit(this);
         this.nspace = last;
 
         access.addTypes(access.getRhs().getTypes());
         
         this.afterAccess(access);
     }
 }
