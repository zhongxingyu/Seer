 package ru.aptu.xml;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import ru.aptu.xml.ASTLexer.ErrorReporter;
 import ru.aptu.xml.ASTLexer.LapgSymbol;
 import ru.aptu.xml.ASTLexer.Lexems;
 import ru.aptu.xml.ast.AstInner;
 import ru.aptu.xml.ast.AstInput;
 import ru.aptu.xml.ast.AstTag;
 import ru.aptu.xml.ast.AstTagText;
 
 public class ASTParser {
 
 	public static class ParseException extends Exception {
 		private static final long serialVersionUID = 1L;
 
 		public ParseException() {
 		}
 	}
 
 	private final ErrorReporter reporter;
 
 	public ASTParser(ErrorReporter reporter) {
 		this.reporter = reporter;
 	}
 
 	private static final boolean DEBUG_SYNTAX = false;
 	private static final int[] tmAction = ASTLexer.unpack_int(12,
 		"\uffff\uffff\uffff\uffff\0\0\6\0\4\0\2\0\5\0\uffff\uffff\3\0\1\0\uffff\uffff\ufffe" +
 		"\uffff");
 
 	private static final short[] lapg_sym_goto = ASTLexer.unpack_short(11,
 		"\0\1\4\5\7\7\10\13\15\17\20");
 
 	private static final short[] lapg_sym_from = ASTLexer.unpack_short(16,
 		"\12\0\1\7\7\1\7\0\0\1\7\1\7\1\7\1");
 
 	private static final short[] lapg_sym_to = ASTLexer.unpack_short(16,
 		"\13\1\1\1\10\3\3\12\2\4\4\5\11\6\6\7");
 
 	private static final short[] lapg_rlen = ASTLexer.unpack_short(7,
 		"\1\2\1\3\1\1\1");
 
 	private static final short[] lapg_rlex = ASTLexer.unpack_short(7,
 		"\5\11\11\6\7\7\10");
 
 	protected static final String[] lapg_syms = new String[] {
 		"eoi",
 		"tagOpen",
 		"tagClose",
 		"innerText",
 		"_skip",
 		"input",
 		"tag",
 		"inner",
 		"tagText",
 		"inner_list",
 	};
 
 	public interface Tokens extends Lexems {
 		// non-terminals
 		public static final int input = 5;
 		public static final int tag = 6;
 		public static final int inner = 7;
 		public static final int tagText = 8;
 		public static final int inner_list = 9;
 	}
 
 	/**
 	 * -3-n   Lookahead (state id)
 	 * -2     Error
 	 * -1     Shift
 	 * 0..n   Reduce (rule index)
 	 */
 	protected static int tmAction(int state, int symbol) {
 		return tmAction[state];
 	}
 
 	protected static int tmGoto(int state, int symbol) {
 		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol + 1] - 1;
 		int i, e;
 
 		while (min <= max) {
 			e = (min + max) >> 1;
 			i = lapg_sym_from[e];
 			if (i == state) {
 				return lapg_sym_to[e];
 			} else if (i < state) {
 				min = e + 1;
 			} else {
 				max = e - 1;
 			}
 		}
 		return -1;
 	}
 
 	protected int tmHead;
 	protected LapgSymbol[] tmStack;
 	protected LapgSymbol tmNext;
 	protected ASTLexer tmLexer;
 
 	public AstInput parse(ASTLexer lexer) throws IOException, ParseException {
 
 		tmLexer = lexer;
 		tmStack = new LapgSymbol[1024];
 		tmHead = 0;
 
 		tmStack[0] = new LapgSymbol();
 		tmStack[0].state = 0;
 		tmNext = tmLexer.next();
 
 		while (tmStack[tmHead].state != 11) {
 			int action = tmAction(tmStack[tmHead].state, tmNext.symbol);
 
 			if (action >= 0) {
 				reduce(action);
 			} else if (action == -1) {
 				shift();
 			}
 
 			if (action == -2 || tmStack[tmHead].state == -1) {
 				break;
 			}
 		}
 
 		if (tmStack[tmHead].state != 11) {
 			reporter.error(tmNext.offset, tmNext.endoffset, tmNext.line,
 						MessageFormat.format("syntax error before line {0}",
 								tmLexer.getTokenLine()));
 			throw new ParseException();
 		}
 		return (AstInput)tmStack[tmHead - 1].value;
 	}
 
 	protected void shift() throws IOException {
 		tmStack[++tmHead] = tmNext;
 		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
 		if (DEBUG_SYNTAX) {
 			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[tmNext.symbol], tmLexer.current()));
 		}
 		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
 			tmNext = tmLexer.next();
 		}
 	}
 
 	protected void reduce(int rule) {
 		LapgSymbol lapg_gg = new LapgSymbol();
 		lapg_gg.value = (lapg_rlen[rule] != 0) ? tmStack[tmHead + 1 - lapg_rlen[rule]].value : null;
 		lapg_gg.symbol = lapg_rlex[rule];
 		lapg_gg.state = 0;
 		if (DEBUG_SYNTAX) {
 			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
 		}
 		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? tmStack[tmHead + 1 - lapg_rlen[rule]] : tmNext;
 		lapg_gg.line = startsym.line;
 		lapg_gg.offset = startsym.offset;
 		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
 		applyRule(lapg_gg, rule, lapg_rlen[rule]);
 		for (int e = lapg_rlen[rule]; e > 0; e--) {
 			tmStack[tmHead--] = null;
 		}
 		tmStack[++tmHead] = lapg_gg;
 		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, lapg_gg.symbol);
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
 		switch (rule) {
 			case 0:  // input ::= tag
 				lapg_gg.value = new AstInput(
 						((AstTag)tmStack[tmHead].value) /* rts */,
 						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
 				break;
 			case 1:  // inner_list ::= inner_list inner
 				((List<AstInner>)lapg_gg.value).add(((AstInner)tmStack[tmHead].value));
 				break;
 			case 2:  // inner_list ::= inner
 				lapg_gg.value = new ArrayList();
 				((List<AstInner>)lapg_gg.value).add(((AstInner)tmStack[tmHead].value));
 				break;
 			case 3:  // tag ::= tagOpen inner_list tagClose
 				lapg_gg.value = new AstTag(
 						((String)tmStack[tmHead - 2].value) /* name */,
 						((List<AstInner>)tmStack[tmHead - 1].value) /* inner */,
						((String)tmStack[tmHead].value) /* tagClose */,
 						null /* input */, tmStack[tmHead - 2].offset, tmStack[tmHead].endoffset);
 				break;
 			case 4:  // inner ::= tag
 				lapg_gg.value = new AstInner(
 						((AstTag)tmStack[tmHead].value) /* elem */,
 						null /* elem2 */,
 						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
 				break;
 			case 5:  // inner ::= tagText
 				lapg_gg.value = new AstInner(
 						null /* elem */,
 						((AstTagText)tmStack[tmHead].value) /* elem2 */,
 						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
 				break;
 			case 6:  // tagText ::= innerText
 				lapg_gg.value = new AstTagText(
 						((String)tmStack[tmHead].value) /* text */,
 						null /* input */, tmStack[tmHead].offset, tmStack[tmHead].endoffset);
 				break;
 		}
 	}
 }
