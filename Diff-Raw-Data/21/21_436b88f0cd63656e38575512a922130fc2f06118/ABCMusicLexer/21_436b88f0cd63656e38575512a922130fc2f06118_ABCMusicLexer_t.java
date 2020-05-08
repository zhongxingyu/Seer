 // Generated from ABCMusicHeader.g4 by ANTLR 4.0
 
 package grammar;
 
 import org.antlr.v4.runtime.Lexer;
 import org.antlr.v4.runtime.CharStream;
 import org.antlr.v4.runtime.Token;
 import org.antlr.v4.runtime.TokenStream;
 import org.antlr.v4.runtime.*;
 import org.antlr.v4.runtime.atn.*;
 import org.antlr.v4.runtime.dfa.DFA;
 import org.antlr.v4.runtime.misc.*;
 
 @SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
 public class ABCMusicLexer extends Lexer {
 	protected static final DFA[] _decisionToDFA;
 	protected static final PredictionContextCache _sharedContextCache =
 		new PredictionContextCache();
 	public static final int
 		X=1, T=2, C=3, L=4, M=5, Q=6, V=7, K=8, W=9, ACCIDENTAL=10, BASE_NOTE=11, 
 		REST=12, BAR_LINE=13, NTH_REPEAT=14, KEY_ACCIDENTAL=15, MODE_MINOR=16, 
 		EQUALS=17, OCTAVE=18, OPEN_PAREN=19, OPEN_BRACK=20, CLOSED_BRACK=21, DIVIDE=22, 
 		DIGIT=23, SPACE=24, LINE_FEED=25, PERCENT=26, TEXT=27, LYRIC_TEXT=28, 
 		LYRIC_ELEMENTS=29, METER_VARIANTS=30, METER_FRACTION=31, METER=32, TEMPO=33, 
 		KEY=34, KEY_NOTE=35, COMMENT=36, EOL=37, NOTE_LENGTH_STRICT=38;
 	public static String[] modeNames = {
 		"DEFAULT_MODE"
 	};
 
 	public static final String[] tokenNames = {
 		"<INVALID>",
 		"'X:'", "'T:'", "'C:'", "'L:'", "'M:'", "'Q:'", "'V:'", "'K:'", "'w:'", 
 		"ACCIDENTAL", "BASE_NOTE", "'z'", "BAR_LINE", "NTH_REPEAT", "KEY_ACCIDENTAL", 
 		"'m'", "'='", "OCTAVE", "'('", "'['", "']'", "'/'", "DIGIT", "' '", "LINE_FEED", 
 		"'%'", "TEXT", "LYRIC_TEXT", "LYRIC_ELEMENTS", "METER_VARIANTS", "METER_FRACTION", 
 		"METER", "TEMPO", "KEY", "KEY_NOTE", "COMMENT", "EOL", "NOTE_LENGTH_STRICT"
 	};
 	public static final String[] ruleNames = {
 		"X", "T", "C", "L", "M", "Q", "V", "K", "W", "ACCIDENTAL", "BASE_NOTE", 
 		"REST", "BAR_LINE", "NTH_REPEAT", "KEY_ACCIDENTAL", "MODE_MINOR", "EQUALS", 
 		"OCTAVE", "OPEN_PAREN", "OPEN_BRACK", "CLOSED_BRACK", "DIVIDE", "DIGIT", 
 		"SPACE", "LINE_FEED", "PERCENT", "TEXT", "LYRIC_TEXT", "LYRIC_ELEMENTS", 
 		"METER_VARIANTS", "METER_FRACTION", "METER", "TEMPO", "KEY", "KEY_NOTE", 
 		"COMMENT", "EOL", "NOTE_LENGTH_STRICT"
 	};
 
 
 	    // This method makes the lexer or parser stop running if it encounters
 	    // invalid input and throw a RuntimeException.
 	    public void reportErrorsAsExceptions() {
 	        removeErrorListeners();
 	        addErrorListener(new ExceptionThrowingErrorListener());
 	    }
 	    
 	    private static class ExceptionThrowingErrorListener extends BaseErrorListener {
 	        @Override
 	        public void syntaxError(Recognizer<?, ?> recognizer,
 	                Object offendingSymbol, int line, int charPositionInLine,
 	                String msg, RecognitionException e) {
 	            throw new RuntimeException(msg);
 	        }
 	    }
 
 
 	public ABCMusicLexer(CharStream input) {
 		super(input);
 		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
 	}
 
 	@Override
 	public String getGrammarFileName() { return "ABCMusicHeader.g4"; }
 
 	@Override
 	public String[] getTokenNames() { return tokenNames; }
 
 	@Override
 	public String[] getRuleNames() { return ruleNames; }
 
 	@Override
 	public String[] getModeNames() { return modeNames; }
 
 	@Override
 	public ATN getATN() { return _ATN; }
 
 	public static final String _serializedATN =
 		"\2\4(\u00f8\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t"+
 		"\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20"+
 		"\t\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27"+
 		"\t\27\4\30\t\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36"+
 		"\t\36\4\37\t\37\4 \t \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\3"+
 		"\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7"+
 		"\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
 		"\5\13r\n\13\3\f\3\f\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3"+
 		"\16\3\16\3\16\5\16\u0083\n\16\3\17\3\17\3\17\3\17\5\17\u0089\n\17\3\20"+
 		"\3\20\3\21\3\21\3\22\3\22\3\23\6\23\u0092\n\23\r\23\16\23\u0093\3\24\3"+
 		"\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\6\30\u009f\n\30\r\30\16\30\u00a0"+
 		"\3\31\3\31\3\32\5\32\u00a6\n\32\3\32\3\32\5\32\u00aa\n\32\3\33\3\33\3"+
 		"\34\6\34\u00af\n\34\r\34\16\34\u00b0\3\35\6\35\u00b4\n\35\r\35\16\35\u00b5"+
 		"\3\36\6\36\u00b9\n\36\r\36\16\36\u00ba\3\36\3\36\3\36\3\36\5\36\u00c1"+
 		"\n\36\3\37\3\37\3\37\5\37\u00c6\n\37\3 \6 \u00c9\n \r \16 \u00ca\3 \3"+
 		" \6 \u00cf\n \r \16 \u00d0\3!\3!\5!\u00d5\n!\3\"\3\"\3\"\6\"\u00da\n\""+
 		"\r\"\16\"\u00db\3#\3#\5#\u00e0\n#\3$\3$\5$\u00e4\n$\3%\3%\3%\3%\3&\3&"+
 		"\5&\u00ec\n&\3\'\6\'\u00ef\n\'\r\'\16\'\u00f0\3\'\3\'\6\'\u00f5\n\'\r"+
 		"\'\16\'\u00f6\2(\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23"+
 		"\13\1\25\f\1\27\r\1\31\16\1\33\17\1\35\20\1\37\21\1!\22\1#\23\1%\24\1"+
 		"\'\25\1)\26\1+\27\1-\30\1/\31\1\61\32\1\63\33\1\65\34\1\67\35\19\36\1"+
 		";\37\1= \1?!\1A\"\1C#\1E$\1G%\1I&\1K\'\1M(\1\3\2\t\4CIci\4%%dd\4))..\3"+
 		"\62;\5\f\f\17\17<<\4C\\c|\6,,//aa\u0080\u0080\u0115\2\3\3\2\2\2\2\5\3"+
 		"\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2"+
 		"\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3"+
 		"\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'"+
 		"\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63"+
 		"\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2"+
 		"?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3"+
 		"\2\2\2\2M\3\2\2\2\3O\3\2\2\2\5R\3\2\2\2\7U\3\2\2\2\tX\3\2\2\2\13[\3\2"+
 		"\2\2\r^\3\2\2\2\17a\3\2\2\2\21d\3\2\2\2\23g\3\2\2\2\25q\3\2\2\2\27s\3"+
 		"\2\2\2\31u\3\2\2\2\33\u0082\3\2\2\2\35\u0088\3\2\2\2\37\u008a\3\2\2\2"+
 		"!\u008c\3\2\2\2#\u008e\3\2\2\2%\u0091\3\2\2\2\'\u0095\3\2\2\2)\u0097\3"+
 		"\2\2\2+\u0099\3\2\2\2-\u009b\3\2\2\2/\u009e\3\2\2\2\61\u00a2\3\2\2\2\63"+
 		"\u00a9\3\2\2\2\65\u00ab\3\2\2\2\67\u00ae\3\2\2\29\u00b3\3\2\2\2;\u00c0"+
 		"\3\2\2\2=\u00c5\3\2\2\2?\u00c8\3\2\2\2A\u00d4\3\2\2\2C\u00d6\3\2\2\2E"+
 		"\u00dd\3\2\2\2G\u00e1\3\2\2\2I\u00e5\3\2\2\2K\u00eb\3\2\2\2M\u00ee\3\2"+
 		"\2\2OP\7Z\2\2PQ\7<\2\2Q\4\3\2\2\2RS\7V\2\2ST\7<\2\2T\6\3\2\2\2UV\7E\2"+
 		"\2VW\7<\2\2W\b\3\2\2\2XY\7N\2\2YZ\7<\2\2Z\n\3\2\2\2[\\\7O\2\2\\]\7<\2"+
 		"\2]\f\3\2\2\2^_\7S\2\2_`\7<\2\2`\16\3\2\2\2ab\7X\2\2bc\7<\2\2c\20\3\2"+
 		"\2\2de\7M\2\2ef\7<\2\2f\22\3\2\2\2gh\7y\2\2hi\7<\2\2i\24\3\2\2\2jr\7`"+
 		"\2\2kl\7`\2\2lr\7`\2\2mr\7a\2\2no\7a\2\2or\7a\2\2pr\7?\2\2qj\3\2\2\2q"+
 		"k\3\2\2\2qm\3\2\2\2qn\3\2\2\2qp\3\2\2\2r\26\3\2\2\2st\t\2\2\2t\30\3\2"+
 		"\2\2uv\7|\2\2v\32\3\2\2\2w\u0083\7~\2\2xy\7~\2\2y\u0083\7~\2\2z{\7]\2"+
 		"\2{\u0083\7~\2\2|}\7~\2\2}\u0083\7_\2\2~\177\7<\2\2\177\u0083\7~\2\2\u0080"+
 		"\u0081\7~\2\2\u0081\u0083\7<\2\2\u0082w\3\2\2\2\u0082x\3\2\2\2\u0082z"+
 		"\3\2\2\2\u0082|\3\2\2\2\u0082~\3\2\2\2\u0082\u0080\3\2\2\2\u0083\34\3"+
 		"\2\2\2\u0084\u0085\7]\2\2\u0085\u0089\7\63\2\2\u0086\u0087\7]\2\2\u0087"+
 		"\u0089\7\64\2\2\u0088\u0084\3\2\2\2\u0088\u0086\3\2\2\2\u0089\36\3\2\2"+
 		"\2\u008a\u008b\t\3\2\2\u008b \3\2\2\2\u008c\u008d\7o\2\2\u008d\"\3\2\2"+
 		"\2\u008e\u008f\7?\2\2\u008f$\3\2\2\2\u0090\u0092\t\4\2\2\u0091\u0090\3"+
 		"\2\2\2\u0092\u0093\3\2\2\2\u0093\u0091\3\2\2\2\u0093\u0094\3\2\2\2\u0094"+
 		"&\3\2\2\2\u0095\u0096\7*\2\2\u0096(\3\2\2\2\u0097\u0098\7]\2\2\u0098*"+
 		"\3\2\2\2\u0099\u009a\7_\2\2\u009a,\3\2\2\2\u009b\u009c\7\61\2\2\u009c"+
 		".\3\2\2\2\u009d\u009f\t\5\2\2\u009e\u009d\3\2\2\2\u009f\u00a0\3\2\2\2"+
 		"\u00a0\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\60\3\2\2\2\u00a2\u00a3"+
 		"\7\"\2\2\u00a3\62\3\2\2\2\u00a4\u00a6\7\17\2\2\u00a5\u00a4\3\2\2\2\u00a5"+
 		"\u00a6\3\2\2\2\u00a6\u00a7\3\2\2\2\u00a7\u00aa\7\f\2\2\u00a8\u00aa\7\17"+
 		"\2\2\u00a9\u00a5\3\2\2\2\u00a9\u00a8\3\2\2\2\u00aa\64\3\2\2\2\u00ab\u00ac"+
 		"\7\'\2\2\u00ac\66\3\2\2\2\u00ad\u00af\n\6\2\2\u00ae\u00ad\3\2\2\2\u00af"+
 		"\u00b0\3\2\2\2\u00b0\u00ae\3\2\2\2\u00b0\u00b1\3\2\2\2\u00b18\3\2\2\2"+
 		"\u00b2\u00b4\t\7\2\2\u00b3\u00b2\3\2\2\2\u00b4\u00b5\3\2\2\2\u00b5\u00b3"+
 		"\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6:\3\2\2\2\u00b7\u00b9\7\"\2\2\u00b8"+
 		"\u00b7\3\2\2\2\u00b9\u00ba\3\2\2\2\u00ba\u00b8\3\2\2\2\u00ba\u00bb\3\2"+
 		"\2\2\u00bb\u00c1\3\2\2\2\u00bc\u00c1\t\b\2\2\u00bd\u00be\7^\2\2\u00be"+
 		"\u00c1\7/\2\2\u00bf\u00c1\7~\2\2\u00c0\u00b8\3\2\2\2\u00c0\u00bc\3\2\2"+
 		"\2\u00c0\u00bd\3\2\2\2\u00c0\u00bf\3\2\2\2\u00c1<\3\2\2\2\u00c2\u00c6"+
 		"\7E\2\2\u00c3\u00c4\7E\2\2\u00c4\u00c6\7~\2\2\u00c5\u00c2\3\2\2\2\u00c5"+
 		"\u00c3\3\2\2\2\u00c6>\3\2\2\2\u00c7\u00c9\5/\30\2\u00c8\u00c7\3\2\2\2"+
 		"\u00c9\u00ca\3\2\2\2\u00ca\u00c8\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00cc"+
 		"\3\2\2\2\u00cc\u00ce\5-\27\2\u00cd\u00cf\5/\30\2\u00ce\u00cd\3\2\2\2\u00cf"+
 		"\u00d0\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1@\3\2\2\2"+
 		"\u00d2\u00d5\5=\37\2\u00d3\u00d5\5? \2\u00d4\u00d2\3\2\2\2\u00d4\u00d3"+
 		"\3\2\2\2\u00d5B\3\2\2\2\u00d6\u00d7\5? \2\u00d7\u00d9\5#\22\2\u00d8\u00da"+
 		"\5/\30\2\u00d9\u00d8\3\2\2\2\u00da\u00db\3\2\2\2\u00db\u00d9\3\2\2\2\u00db"+
 		"\u00dc\3\2\2\2\u00dcD\3\2\2\2\u00dd\u00df\5G$\2\u00de\u00e0\5!\21\2\u00df"+
 		"\u00de\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0F\3\2\2\2\u00e1\u00e3\5\27\f\2"+
 		"\u00e2\u00e4\5\37\20\2\u00e3\u00e2\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4H"+
 		"\3\2\2\2\u00e5\u00e6\5\65\33\2\u00e6\u00e7\5\67\34\2\u00e7\u00e8\5\63"+
 		"\32\2\u00e8J\3\2\2\2\u00e9\u00ec\5I%\2\u00ea\u00ec\5\63\32\2\u00eb\u00e9"+
 		"\3\2\2\2\u00eb\u00ea\3\2\2\2\u00ecL\3\2\2\2\u00ed\u00ef\5/\30\2\u00ee"+
 		"\u00ed\3\2\2\2\u00ef\u00f0\3\2\2\2\u00f0\u00ee\3\2\2\2\u00f0\u00f1\3\2"+
 		"\2\2\u00f1\u00f2\3\2\2\2\u00f2\u00f4\5-\27\2\u00f3\u00f5\5/\30\2\u00f4"+
 		"\u00f3\3\2\2\2\u00f5\u00f6\3\2\2\2\u00f6\u00f4\3\2\2\2\u00f6\u00f7\3\2"+
 		"\2\2\u00f7N\3\2\2\2\30\2q\u0082\u0088\u0093\u00a0\u00a5\u00a9\u00b0\u00b5"+
 		"\u00ba\u00c0\u00c5\u00ca\u00d0\u00d4\u00db\u00df\u00e3\u00eb\u00f0\u00f6";
 	public static final ATN _ATN =
 		ATNSimulator.deserialize(_serializedATN.toCharArray());
 	static {
 		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
 	}
 }
