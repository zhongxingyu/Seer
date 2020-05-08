 package descent.internal.compiler.parser;
 
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 
 public class TypeExp extends Expression {
 
 	public TypeExp(char[] filename, int lineNumber, Type type) {
 		super(filename, lineNumber, TOK.TOKtype);
 		this.type = type;
 		this.sourceType = type;
 	}
 	
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
 			TreeVisitor.acceptChildren(visitor, type);
 		}
 		visitor.endVisit(this);
 	}
 	
 	@Override
 	public int getNodeType() {
 		return TYPE_EXP;
 	}
 
 	
 	@Override
 	public Expression optimize(int result, SemanticContext context) {
 		return this;
 	}
 	
 	@Override
 	public Expression semantic(Scope sc, SemanticContext context) {
 		type = type.semantic(filename, lineNumber, sc, context);
 	    return this;
 	}
 	
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
 		type.toCBuffer(buf, null, hgs, context);
 	}
 
 }
