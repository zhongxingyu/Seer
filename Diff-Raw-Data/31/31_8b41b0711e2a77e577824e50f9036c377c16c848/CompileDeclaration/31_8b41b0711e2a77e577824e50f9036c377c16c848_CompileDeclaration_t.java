 package descent.internal.compiler.parser;
 
 import static descent.internal.compiler.parser.TOK.TOKeof;
 import static descent.internal.compiler.parser.TOK.TOKstring;
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.core.IInitializer;
 import descent.core.IJavaElement;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.ast.ASTNode;
 import descent.internal.compiler.parser.ast.AstVisitorAdapter;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 
 public class CompileDeclaration extends AttribDeclaration {
 
 	public Expression exp, sourceExp;
 	public ScopeDsymbol sd;
 	public boolean compiled;
 	
 	protected IInitializer javaElement;
 	
 	public CompileDeclaration(char[] filename, int lineNumber, Expression exp) {
 		super(null);
 		this.filename = filename;
 		this.lineNumber = lineNumber;
 		this.exp = exp;
 		this.sourceExp = exp;
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
 	public int addMember(Scope sc, ScopeDsymbol sd, int memnum,
 			SemanticContext context) {
 		this.sd = sd;
 		if (memnum == 0) {
 			/*
 			 * No members yet, so parse the mixin now
 			 */
 			compileIt(sc, context);
 			memnum |= super.addMember(sc, sd, memnum, context);
 			compiled = true;
 		}
 		return memnum;
 	}
 	
 	public void compileIt(Scope sc, final SemanticContext context) {
 		exp = exp.semantic(sc, context);
 		exp = resolveProperties(sc, exp, context);
 		exp = exp.optimize(WANTvalue | WANTinterpret, context);
 		if (exp.op != TOKstring) {
 			if (context.acceptsErrors()) {
 				context.acceptProblem(Problem.newSemanticTypeError(IProblem.ArgumentToMixinMustBeString, this, exp.toChars(context)));
 			}
 		}
 	    else
 	    {
 	    	StringExp se = (StringExp) exp;
 			se = se.toUTF8(sc, context);
 			Parser p = context.newParser(context.Module_rootModule.apiLevel, se.string);
			p.module = sc.module;
 			// p.nextToken();
 			p.filename = filename;
 			p.lineNumber = lineNumber;
 			decl = p.parseModule();
 			
 			if (context.mustCopySourceRangeForMixins()) {
 				for(Dsymbol s : decl) {
 					s.accept(new AstVisitorAdapter() {
 						@Override
 						public void preVisit(ASTNode node) {
 							if (node instanceof ASTDmdNode) {
 								ASTDmdNode s = (ASTDmdNode) node;
 								s.setStart(getStart() + 1);
 								s.setLength(getLength());
 								s.setLineNumber(getLineNumber());
 								context.setCreator(s, CompileDeclaration.this);
 							}
 						}
 					});
 				}
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
 
 			if (p.token.value != TOKeof) {
 				if (context.acceptsErrors()) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.IncompleteMixinDeclaration, this,
 							new String[] { se.toChars(context) }));
 				}
 			}
 	    }
 	}
 
 	@Override
 	public int getNodeType() {
 		return COMPILE_DECLARATION;
 	}
 
 	@Override
 	public void semantic(Scope sc, SemanticContext context) {
 		if (!compiled) {
 			compileIt(sc, context);
 			super.addMember(sc, sd, 0, context);
 			compiled = true;
 		}
 		super.semantic(sc, context);
 	}
 
 	@Override
 	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
 		CompileDeclaration sc = context.newCompileDeclaration(filename, lineNumber, exp.syntaxCopy(context));
 		sc.copySourceRange(this);
 		return sc;
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		buf.writestring("mixin(");
 		exp.toCBuffer(buf, hgs, context);
 		buf.writestring(");");
 		buf.writenl();
 	}
 	
 	@Override
 	public IJavaElement getJavaElement() {
 		return javaElement;
 	}
 	
 	public void setJavaElement(IInitializer javaElement) {
 		this.javaElement = javaElement;
 	}
 
 }
