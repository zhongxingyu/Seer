 package descent.internal.compiler.parser;
 
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 import static descent.internal.compiler.parser.Scope.CSXlabel;
 
 // DMD 1.020
 public class LabelStatement extends Statement {
 
 	public IdentifierExp ident;
 	public Statement statement;
 	public TryFinallyStatement tf;
 	public boolean isReturnLabel;
 
 	public LabelStatement(Loc loc, IdentifierExp ident, Statement statement) {
 		super(loc);
 		this.ident = ident;
 		this.statement = statement;
		this.start = ident.start;
		this.length = statement.start + statement.length - ident.start;
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
 			TreeVisitor.acceptChildren(visitor, ident);
 			TreeVisitor.acceptChildren(visitor, statement);
 		}
 		visitor.endVisit(this);
 	}
 
 	@Override
 	public boolean comeFrom() {
 		return true;
 	}
 
 	@Override
 	public boolean fallOffEnd(SemanticContext context) {
 		return statement != null ? statement.fallOffEnd(context) : true;
 	}
 
 	@Override
 	public Statements flatten(Scope sc, SemanticContext context) {
 		Statements a = null;
 
 		if (statement != null) {
 			a = statement.flatten(sc, context);
 			if (a != null) {
 				if (0 == a.size()) {
 					a.add(new ExpStatement(loc, null));
 				}
 				Statement s = a.get(0);
 
 				s = new LabelStatement(loc, ident, s);
 				a.set(0, s);
 			}
 		}
 
 		return a;
 	}
 
 	@Override
 	public int getNodeType() {
 		return LABEL_STATEMENT;
 	}
 
 	@Override
 	public Statement inlineScan(InlineScanState iss, SemanticContext context) {
 		if (statement != null) {
 			statement = statement.inlineScan(iss, context);
 		}
 		return this;
 	}
 
 	@Override
 	public Expression interpret(InterState istate, SemanticContext context) {
 		if (istate.start == this) {
 			istate.start = null;
 		}
 		return statement != null ? statement.interpret(istate, context) : null;
 	}
 
 	@Override
 	public Statement semantic(Scope sc, SemanticContext context) {
 		LabelDsymbol ls;
 		FuncDeclaration fd = sc.parent.isFuncDeclaration();
 
 		ls = fd.searchLabel(ident);
 		if (ls.statement != null) {
 			error("Label '%s' already defined", ls.toChars(context));
 		} else {
 			ls.statement = this;
 		}
 		tf = sc.tf;
 		sc = sc.push();
 		sc.scopesym = sc.enclosing.scopesym;
 		sc.callSuper |= CSXlabel;
 		sc.slabel = this;
 		if (statement != null) {
 			statement = statement.semantic(sc, context);
 		}
 		sc.pop();
 		return this;
 	}
 
 	@Override
 	public Statement syntaxCopy() {
 		LabelStatement s = new LabelStatement(loc, ident, statement
 				.syntaxCopy());
 		return s;
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		buf.writestring(ident.toChars());
 		buf.writebyte(':');
 		buf.writenl();
 		if (statement != null) {
 			statement.toCBuffer(buf, hgs, context);
 		}
 	}
 
 	@Override
 	public boolean usesEH() {
 		return statement != null ? statement.usesEH() : false;
 	}
 
 }
