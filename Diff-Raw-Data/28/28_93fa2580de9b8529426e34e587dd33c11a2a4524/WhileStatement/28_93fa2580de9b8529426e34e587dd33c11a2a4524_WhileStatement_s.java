 package descent.internal.compiler.parser;
 
 import static descent.internal.compiler.parser.BE.BEbreak;
 import static descent.internal.compiler.parser.BE.BEcontinue;
 import static descent.internal.compiler.parser.BE.BEfallthru;
 import static descent.internal.compiler.parser.BE.BEnone;
 import static descent.internal.compiler.parser.BE.BEthrow;
 import melnorme.miscutil.tree.TreeVisitor;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 public class WhileStatement extends Statement {
 
	public Expression condition;
	public Statement body;
 
 	public WhileStatement(char[] filename, int lineNumber, Expression c, Statement b) {
 		super(filename, lineNumber);
		this.condition = c;
		this.body = b;
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		boolean children = visitor.visit(this);
 		if (children) {
			TreeVisitor.acceptChildren(visitor, condition);
			TreeVisitor.acceptChildren(visitor, body);
 		}
 		visitor.endVisit(this);
 	}
 	
 	@Override
 	public int blockExit(SemanticContext context) {
 		int result = BEnone;
 		if (condition.canThrow(context)) {
 			result |= BEthrow;
 		}
 		
 		if (body != null) {
 			result |= body.blockExit(context);
 			if ((result & BEbreak) != 0) {
 				result |= BEfallthru;
 			}
 			result &= ~(BEbreak | BEcontinue);
 		} else {
 			result |= BEfallthru;
 		}
 		return result;
 	}
 
 	@Override
 	public boolean comeFrom() {
 		if (body != null) {
 			return body.comeFrom();
 		}
 		return false;
 	}
 
 	@Override
 	public int getNodeType() {
 		return WHILE_STATEMENT;
 	}
 
 	@Override
 	public boolean hasBreak() {
 		return true;
 	}
 
 	@Override
 	public boolean hasContinue() {
 		return true;
 	}
 
 	@Override
 	public Expression interpret(InterState istate, SemanticContext context) {
 		if (istate.start == this) {
 			istate.start = null;
 		}
 		Expression e;
 
 		if (istate.start != null) {
 			e = body != null ? body.interpret(istate, context) : null;
 			if (istate.start != null) {
 				return null;
 			}
 			if (e == EXP_CANT_INTERPRET) {
 				return e;
 			}
 			if (e == EXP_BREAK_INTERPRET) {
 				return null;
 			}
 			if (e != EXP_CONTINUE_INTERPRET) {
 				return e;
 			}
 		}
 
 		while (true) {
 			e = condition.interpret(istate, context);
 			if (e == EXP_CANT_INTERPRET) {
 				break;
 			}
 			if (!e.isConst()) {
 				e = EXP_CANT_INTERPRET;
 				break;
 			}
 			if (e.isBool(true)) {
 				e = body != null ? body.interpret(istate, context) : null;
 				if (e == EXP_CANT_INTERPRET) {
 					break;
 				}
 				if (e == EXP_CONTINUE_INTERPRET) {
 					continue;
 				}
 				if (e == EXP_BREAK_INTERPRET) {
 					e = null;
 					break;
 				}
 				if (e != null) {
 					break;
 				}
 			} else if (e.isBool(false)) {
 				e = null;
 				break;
 			} else {
 				throw new IllegalStateException("assert(0);");
 			}
 		}
 		return e;
 	}
 
 	@Override
 	public Statement semantic(Scope sc, SemanticContext context) {
 		condition = condition.semantic(sc, context);
 		condition = resolveProperties(sc, condition, context);
 	    condition = condition.optimize(WANTvalue, context);
 		condition = condition.checkToBoolean(context);
 
 		sc.noctor++;
 
 		Scope scd = sc.push();
 		scd.sbreak = this;
 		scd.scontinue = this;
 		if (body != null) {
 			body = body.semantic(scd, context);
 		}
 		scd.pop();
 
 		sc.noctor--;
 
 		return this;
 	}
 
 	@Override
 	public Statement syntaxCopy(SemanticContext context) {
 		WhileStatement s = context.newWhileStatement(filename, lineNumber, condition.syntaxCopy(context),
 				body != null ? body.syntaxCopy(context) : null);
 		s.copySourceRange(this);
 		return s;
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		buf.writestring("while (");
 		condition.toCBuffer(buf, hgs, context);
 		buf.writebyte(')');
 		buf.writenl();
 		if (body != null) {
 			body.toCBuffer(buf, hgs, context);
 		}
 	}
 
 	@Override
 	public boolean usesEH(SemanticContext context) {
 		return body != null ? body.usesEH(context) : false;
 	}
 
 }
