 package org.suite.doer;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.parser.Operator;
 import org.parser.Operator.Assoc;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 import org.util.FormatUtil;
 
 public class PrettyPrinter {
 
 	private int indent;
 	private Map<Integer, Integer> lengthByIds = new HashMap<Integer, Integer>();
 	private StringBuilder sb = new StringBuilder();
 	private int nLines = 0;
 	private int currentLineIndent = 0;
 
 	private static final int LINELENGTH = 96; // Estimated
 	private static final int SQUEEZELINELENGTH = 8;
 	private static final String INDENTSPACES = "    ";
 
 	private static final Set<Node> LINEBREAKBEFOREKEYWORDS = new HashSet<Node>(
 			Arrays.asList(Atom.create("else-if")));
 	private static final Set<Node> PREFERLINEBREAKBEFOREKEYWORDS = new HashSet<Node>(
			Arrays.asList(Atom.create("else")));
 	private static final Set<Operator> LINEBREAKAFTEROPERATORS = new HashSet<Operator>(
 			Arrays.asList(TermOp.CONTD_, TermOp.FUN___));
 
 	private static class OperatorPosition {
 		private int indent;
 		private int y;
 
 		public OperatorPosition(int indent, int y) {
 			this.indent = indent;
 			this.y = y;
 		}
 	}
 
 	public String prettyPrint(Node node) {
 		estimateLengths(node);
 		prettyPrint0(node, null, 0);
 		return sb.toString();
 	}
 
 	private void prettyPrint0(Node node //
 			, Operator op0 // for avoiding unnecessary indenting
 			, int prec0 // for parenthesizing
 	) {
 		node = node.finalNode();
 		int x = getX(), y = getY();
 		int length = getEstimatedLength(node);
 
 		// Line too long?
 		if (node instanceof Tree) {
 			Tree tree = (Tree) node;
 			Operator op = tree.getOperator();
 			int prec = op.getPrecedence();
 			boolean isNeedPars = prec <= prec0;
 			int parsIndent = 0, parsIndent0 = 0;
 
 			if (isNeedPars) {
 				parsIndent = currentLineIndent;
 				parsIndent0 = incrementIndent();
 				append("(");
 			}
 
 			if (x + length > LINELENGTH)
 				if (isLookingLikeList(op, node))
 					prettyPrintList(op, node);
 				else {
 					Node left = tree.getLeft();
 					Node right = tree.getRight();
 					Assoc assoc = op.getAssoc();
 
 					int leftPrec = prec - (assoc == Assoc.LEFT ? 1 : 0);
 					int rightPrec = prec - (assoc == Assoc.RIGHT ? 1 : 0);
 					if (op == TermOp.BRACES)
 						leftPrec = rightPrec = 0;
 
 					Tree tree1 = Tree.decompose(right, op);
 					Node r0 = tree1 != null ? tree1.getLeft() : null;
 					Integer es0 = getEstimatedLength(left);
 					Integer es1 = r0 != null ? getEstimatedLength(r0) : null;
 					int opLength = op.getName().length();
 
 					// Breaks "a + b + xxx" in the second operator
 					if (assoc == Assoc.RIGHT //
 							&& es1 != null //
 							&& x + es0 + es1 + opLength < LINELENGTH //
 							&& !PREFERLINEBREAKBEFOREKEYWORDS.contains(r0)) {
 						prettyPrint0(left, op, leftPrec);
 						OperatorPosition opPos = appendOperator(op);
 						prettyPrint0(right, op, rightPrec);
 						closeBraces(op, opPos);
 					} else { // Breaks after the operator
 						boolean isIncRightIndent = op != op0;
 						int indent0 = 0;
 
 						prettyPrint0(left, op, leftPrec);
 
 						if (isIncRightIndent)
 							indent0 = incrementIndent();
 
 						OperatorPosition opPos;
 						if (getLineSize() + getEstimatedLength(right) < SQUEEZELINELENGTH)
 							opPos = appendOperator(op);
 						else
 							opPos = appendOperatorLineFeed(op);
 
 						prettyPrint0(right, op, rightPrec);
 						closeBraces(op, opPos);
 
 						if (isIncRightIndent)
 							revertIndent(indent0);
 					}
 				}
 			else
 				append(Formatter.dump(node));
 
 			if (isNeedPars) {
 				if (y != getY())
 					nl(parsIndent);
 				append(")");
 				revertIndent(parsIndent0);
 			}
 		} else {
 			if (LINEBREAKBEFOREKEYWORDS.contains(node) && !isLineBegin())
 				nl();
 
 			append(Formatter.dump(node)); // Space sufficient
 		}
 	}
 
 	private void prettyPrintList(Operator op, Node node) {
 		int prec = op.getPrecedence(), prec1 = prec - 1;
 
 		if (node instanceof Tree) {
 			Tree tree = (Tree) node;
 
 			if (tree.getOperator() == op) {
 				boolean isLeftAssoc = op.getAssoc() == Assoc.LEFT;
 				OperatorPosition opPos;
 
 				if (isLeftAssoc) {
 					prettyPrintList(op, tree.getLeft());
 					opPos = appendOperatorLineFeed(op);
 					prettyPrintIndented(tree.getRight(), prec1);
 				} else {
 					prettyPrintIndented(tree.getLeft(), prec1);
 					opPos = appendOperatorLineFeed(op);
 					prettyPrintList(op, tree.getRight());
 				}
 
 				closeBraces(op, opPos);
 				return;
 			}
 		}
 
 		prettyPrint0(node, op, prec);
 	}
 
 	private void prettyPrintIndented(Node left, int prec) {
 		int indent0 = incrementIndent();
 		prettyPrint0(left, null, prec);
 		revertIndent(indent0);
 	}
 
 	private void closeBraces(Operator op, OperatorPosition opPos) {
 		if (op == TermOp.BRACES) {
 			if (opPos.y != getY())
 				nl(opPos.indent);
 			append("}");
 		}
 	}
 
 	private boolean isLookingLikeList(Operator op, Node node) {
 		if (node instanceof Tree) {
 			Tree tree = (Tree) node;
 
 			if (tree.getOperator() == op) {
 				boolean isLeftAssoc = op.getAssoc() == Assoc.LEFT;
 				Node child = isLeftAssoc ? tree.getLeft() : tree.getRight();
 				return isLookingLikeList(op, child);
 			}
 		}
 
 		return op != TermOp.TUPLE_
 				&& (op == TermOp.AND___ || op == TermOp.OR____ || node == Atom.nil);
 	}
 
 	private int estimateLengths(Node node) {
 		node = node.finalNode();
 		int key = getKey(node);
 		Integer length = lengthByIds.get(key);
 
 		if (length == null) {
 			int len;
 
 			if (node instanceof Tree) {
 				Tree tree = (Tree) node;
 
 				Operator op = tree.getOperator();
 				int len0 = estimateLengths(tree.getLeft());
 				int len1 = estimateLengths(tree.getRight());
 				int opLength = op.getName().length();
 
 				// Rough estimation
 				len = len0 + len1 + opLength + 2;
 			} else
 				len = Formatter.dump(node).length();
 
 			length = len;
 			lengthByIds.put(key, length);
 		}
 
 		return length;
 	}
 
 	private Integer getEstimatedLength(Node node) {
 		return lengthByIds.get(getKey(node));
 	}
 
 	private OperatorPosition appendOperatorLineFeed(Operator op) {
 		boolean isLineFeedAfterOp = LINEBREAKAFTEROPERATORS.contains(op);
 		if (!isLineFeedAfterOp)
 			nl();
 		OperatorPosition result = appendOperator(op);
 		if (isLineFeedAfterOp || op == TermOp.NEXT__)
 			nl();
 		return result;
 	}
 
 	private OperatorPosition appendOperator(Operator op) {
 		String name = op.getName();
 		name += op == TermOp.AND___ || op == TermOp.OR____ ? " " : "";
 		if (isLineBegin())
 			name = FormatUtil.leftTrim(name);
 		append(name);
 		return new OperatorPosition(currentLineIndent, getY());
 	}
 
 	private int incrementIndent() { // Would not jump by two
 		int indent0 = indent;
 		indent = Math.min(indent, currentLineIndent) + 1;
 		return indent0;
 	}
 
 	private void revertIndent(int indent0) {
 		indent = indent0;
 	}
 
 	private boolean isLineBegin() {
 		boolean result = true;
 		String l = sb.substring(getLineBeginPosition(), getCurrentPosition());
 		for (char c : l.toCharArray())
 			result &= Character.isWhitespace(c);
 		return result;
 	}
 
 	private int getLineSize() {
 		return getCurrentPosition() - getLineContentBeginPosition();
 	}
 
 	private int getX() {
 		return getCurrentPosition() - getLineBeginPosition();
 	}
 
 	private int getY() {
 		return nLines;
 	}
 
 	private int getLineContentBeginPosition() {
 		int pos = getLineBeginPosition();
 		while (pos < getCurrentPosition()
 				&& Character.isWhitespace(sb.charAt(pos)))
 			pos++;
 		return pos;
 	}
 
 	private int getLineBeginPosition() {
 		int pos = getCurrentPosition();
 		while (--pos > 0 && sb.charAt(pos) != '\n')
 			;
 		return pos + 1;
 	}
 
 	private int getCurrentPosition() {
 		return sb.length();
 	}
 
 	private void nl() {
 		nl(indent);
 	}
 
 	private void nl(int indent) {
 		append("\n");
 		nLines++;
 		pre(currentLineIndent = indent);
 	}
 
 	private void pre(int indent) {
 		for (int i = 0; i < indent; i++)
 			append(INDENTSPACES);
 	}
 
 	private void append(String s) {
 		sb.append(s);
 	}
 
 	private int getKey(Node node) {
 		return System.identityHashCode(node);
 	}
 
 }
