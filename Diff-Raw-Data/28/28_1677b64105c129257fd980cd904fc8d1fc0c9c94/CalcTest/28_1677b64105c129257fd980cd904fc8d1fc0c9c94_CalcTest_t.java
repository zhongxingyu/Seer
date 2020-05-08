 package henix.jillus.test;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import static henix.jillus.Pegs.*;
 
 import henix.jillus.*;
 import henix.jillus.pegs.capture.NonTerminalCapturer;
 import henix.jillus.pegs.pattern.Literal;
 import henix.jillus.test.CalcTest.BinaryExpr.PartExp;
 
 public class CalcTest {
 
 	// ASTs
 	public static abstract class Expr {
 	}
 
 	public static class IntLiteral extends Expr {
 		public final int value;
 		public IntLiteral(int value) {
 			this.value = value;
 		}
 	}
 
 	public static class BinaryExpr extends Expr {
 		public static class PartExp {
 			public String op;
 			public Expr expr;
 		}
 		public Expr first;
 		public List<PartExp> others;
 	}
 
 	static final PegPattern digit = charInRange('0', '9');
 
 	static final PegPattern space = charInSet(" \t");
 
 	static final PegPattern spaces = atLeast(0, space);
 
 	/**
 	 * convenient functions for creating punctuations
 	 */
 	static PegPattern punct(PegPattern e) {
 		return sequence(spaces, e, spaces);
 	}
 
 	static PegPattern punct(String s) {
 		return punct(new Literal(s));
 	}
 
 	static <E> Capturer<E> punct(Capturer<E> e) {
 		return passCapture(spaces, e, spaces);
 	}
 
 	static final ValueCreator<IntLiteral> intLiteralParser = new ValueCreator<IntLiteral>() {
 		public IntLiteral create(String capture) {
 			return new IntLiteral(Integer.parseInt(capture));
 		}
 	};
 
 	static final Capturer<IntLiteral> IntLiteral = capture(intLiteralParser, atLeast(1, digit));
 
 	static final NonTerminalCapturer<Expr> TermExpr = new NonTerminalCapturer<Expr>();
 
 	static final NonTerminalCapturer<BinaryExpr> MulExpr = new NonTerminalCapturer<BinaryExpr>();
 
 	static final NonTerminalCapturer<BinaryExpr> AddExpr = new NonTerminalCapturer<BinaryExpr>();
 
 	static {
 		// TermExpr := IntLiteral / "(" AddExpr ")"
 		TermExpr.set(orderChoice(IntLiteral, passCapture(punct("("), AddExpr, punct(")"))));
 
 		// MulExpr := TermExpr ( [*/] TermExpr )*
 		MulExpr.set(asStruct(BinaryExpr.class, sequence(
 			bindField(BinaryExpr.class, Expr.class, "first", TermExpr),
 			bindField(BinaryExpr.class, List.class, "others", asList(0, asStruct(PartExp.class, sequence(
 				bindNothing(spaces),
 				bindField(PartExp.class, String.class, "op", capture(charInSet("*/"))),
 				bindNothing(spaces),
 				bindField(PartExp.class, Expr.class, "expr", TermExpr)
 			))))
 		)));
 
 		// AddExpr := MulExpr ( [+-] MulExpr )*
 		AddExpr.set(asStruct(BinaryExpr.class, sequence(
 			bindField(BinaryExpr.class, Expr.class, "first", MulExpr),
 			bindField(BinaryExpr.class, List.class, "others", asList(0, asStruct(PartExp.class, sequence(
 				bindField(PartExp.class, String.class, "op", punct(capture(charInSet("+-")))),
 				bindField(PartExp.class, Expr.class, "expr", MulExpr)
 			))))
 		)));
 	}
 
 	// allow spaces at start and end of input
	static final Capturer<BinaryExpr> all = passCapture(spaces, AddExpr, sequence(spaces, Pegs.eof));
 
 	static int eval(Expr expr) {
 		if (expr instanceof IntLiteral) {
 			IntLiteral literal = (IntLiteral)expr;
 			return literal.value;
 		} else if (expr instanceof BinaryExpr) {
 			BinaryExpr binaryExpr = (BinaryExpr)expr;
 			int v = eval(binaryExpr.first);
 			for (PartExp partExp : binaryExpr.others) {
 				int v2 = eval(partExp.expr);
 				if (partExp.op.equals("+")) {
 					v += v2;
 				} else if (partExp.op.equals("-")) {
 					v -= v2;
 				} else if (partExp.op.equals("*")) {
 					v *= v2;
 				} else if (partExp.op.equals("/")) {
 					v /= v2;
 				}
 			}
 			return v;
 		} else {
 			throw new IllegalArgumentException("Unknown type: " + expr.getClass().getName());
 		}
 	}
 
 	static int calculate(String str) {
 		final PatternExecutor executor = new PatternExecutor(new StringSource(str));
 		return eval(executor.execute(all));
 	}
 
 	@Test
 	public void main() {
 		Assert.assertEquals(3, calculate(" 1+ 2 "));
 		Assert.assertEquals(7, calculate("1+ 2 * 3"));
 		Assert.assertEquals(42, calculate("(1 + 5) * (3 + 4) "));
 	}
 }
