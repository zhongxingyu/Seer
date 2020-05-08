 package descent.internal.compiler.parser;
 
 import static descent.internal.compiler.parser.Parser.PScurlyscope;
 import static descent.internal.compiler.parser.Parser.PSsemi;
 import static descent.internal.compiler.parser.TOK.TOKeof;
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 
 public class CompileStatement extends Statement {
 
 	public Expression exp, sourceExp;
 
 	public CompileStatement(char[] filename, int lineNumber, Expression exp) {
 		super(filename, lineNumber);
 		this.exp = this.sourceExp = exp;
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
 			TreeVisitor.acceptChildren(visitor, sourceExp);
 		}
 		visitor.endVisit(this);
 	}
 
 	@Override
 	public int getNodeType() {
 		return COMPILE_STATEMENT;
 	}
 
 	@Override
 	public Statements flatten(Scope sc, SemanticContext context) {
 		exp = exp.semantic(sc, context);
 		exp = ASTDmdNode.resolveProperties(sc, exp, context);
 		exp = exp.optimize(ASTDmdNode.WANTvalue | ASTDmdNode.WANTinterpret,
 				context);
 		if (exp.op != TOK.TOKstring) {
 			if (context.acceptsErrors()) {
 				context.acceptProblem(Problem.newSemanticTypeError(IProblem.ArgumentToMixinMustBeString, this, exp.toChars(context)));
 			}
 			return null;
 		}
 		StringExp se = (StringExp) exp;
 		se = se.toUTF8(sc, context);
 		Parser p = context.newParser(context.Module_rootModule.apiLevel, se.string);
 		p.filename = filename;
 		p.lineNumber = lineNumber;
 
 		Statements a = new Statements();
 		while (p.token.value != TOKeof) {
 			Statement s = p.parseStatement(PSsemi | PScurlyscope);
 			
 			// TODO: no IJavaElement for CompileStatement...
 //			s.accept(new AstVisitorAdapter() {
 //				@Override
 //				public void preVisit(ASTNode node) {
 //					if (node instanceof ASTDmdNode) {
 //						ASTDmdNode s = (ASTDmdNode) node;
 //						s.synthetic = true;
 //						s.setStart(getStart() + 1);
 //						s.setLength(getLength());
 //						s.setLineNumber(getLineNumber());					
 //						s.creator = CompileStatement.this;
 //					}
 //				}
 //			});
 			
 			a.add(s);
 		}
 		
 		// TODO semantic do this better
 		if (p.problems != null) {
 			for (int i = 0; i < p.problems.size(); i++) {
 				Problem problem = (Problem) p.problems.get(i);
 				problem.setSourceStart(start);
 				problem.setSourceEnd(start + length - 1);
 				context.acceptProblem(problem);
 			}
 		}
 
 		return a;
 	}
 	
 	@Override
 	public Statement semantic(Scope sc, SemanticContext context) {
 		Statements a = flatten(sc, context);
 	    if (null == a) {
 	    	return null;
 	    }
 	    Statement s = context.newCompoundStatement(filename, lineNumber, a);
 	    return s.semantic(sc, context);
 	}
 
 	@Override
 	public Statement syntaxCopy(SemanticContext context) {
 		Expression e = exp.syntaxCopy(context);
 		CompileStatement es = context.newCompileStatement(filename, lineNumber, e);
 		es.copySourceRange(this);
 		return es;
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		buf.writestring("mixin(");
 		exp.toCBuffer(buf, hgs, context);
 		buf.writestring(");");
 		if (0 == hgs.FLinit.init) {
 			buf.writenl();
 		}
 	}
 
 }
