 
 //ifdef JAVA
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 interface GtConst {
 //endif VAJA
 	// ClassFlag
 	public final static int		PrivateClass					= 1 << 0;
 	public final static int		SingletonClass					= 1 << 1;
 	public final static int		FinalClass						= 1 << 2;
 	public final static int		GreenClass		    			= 1 << 3;
 	public final static int		StaticClass						= 1 << 4;
 	public final static int		ImmutableClass					= 1 << 5;
 	public final static int		InterfaceClass					= 1 << 6;
 
 	// MethodFlag
 	public final static int		PrivateMethod					= 1 << 0;
 	public final static int		VirtualMethod					= 1 << 1;
 	public final static int		FinalMethod						= 1 << 2;
 	public final static int		ConstMethod						= 1 << 3;
 	public final static int		StaticMethod					= 1 << 4;
 	public final static int		ImmutableMethod					= 1 << 5;
 	public final static int		TopLevelMethod					= 1 << 6;
 
 	// call rule
 	public final static int		CoercionMethod					= 1 << 7;
 	public final static int		RestrictedMethod				= 1 << 8;
 	public final static int		UncheckedMethod					= 1 << 9;
 	public final static int		SmartReturnMethod				= 1 << 10;
 	public final static int		VariadicMethod					= 1 << 11;
 	public final static int		IterativeMethod					= 1 << 12;
 
 	// compatible
 	public final static int		UniversalMethod					= 1 << 13;
 	
 	public final static int		UniqueMethod					= 1 << 14; /* used */
 	public final static int		ExportMethod					= 1 << 15; /* used */
 
 	// internal
 	public final static int		HiddenMethod					= 1 << 17;
 	public final static int		AbstractMethod					= 1 << 18;
 	public final static int		OverloadedMethod				= 1 << 19;
 	public final static int		Override						= 1 << 20;
 	public final static int		DynamicCall						= 1 << 22;
 
 	public final static int		SymbolMaskSize					= 3;
 	public final static int		LowerSymbolMask					= 1;
 	public final static int		GetterSymbolMask				= (1 << 1);
 	public final static int		SetterSymbolMask				= (1 << 2);
 	public final static int		MetaSymbolMask					= (GetterSymbolMask | SetterSymbolMask);
 	public final static String	GetterPrefix					= "Get";
 	public final static String	SetterPrefix					= "Set";
 	public final static String	MetaPrefix						= "\\";
 
 	public final static int		AllowNewId						= -1;
 	public final static int		NoMatch							= -1;
 	public final static int		BreakPreProcess					= -1;
 
 	public final static boolean Optional = true;
 	public final static boolean Required = false;
 
 	public final static int		ErrorLevel						= 0;
 	public final static int		WarningLevel					= 1;
 	public final static int		InfoLevel					     = 2;
 
 	public final static int	NullChar				= 0;
 	public final static int	UndefinedChar			= 1;
 	public final static int	DigitChar				= 2;
 	public final static int	UpperAlphaChar			= 3;
 	public final static int	LowerAlphaChar			= 4;
 	public final static int	UnderBarChar			= 5;
 	public final static int	NewLineChar				= 6;
 	public final static int	TabChar					= 7;
 	public final static int	SpaceChar				= 8;
 	public final static int	OpenParChar				= 9;
 	public final static int	CloseParChar			= 10;
 	public final static int	OpenBracketChar			= 11;
 	public final static int	CloseBracketChar		= 12;
 	public final static int	OpenBraceChar			= 13;
 	public final static int	CloseBraceChar			= 14;
 	public final static int	LessThanChar			= 15;
 	public final static int	GreaterThanChar			= 16;
 	public final static int	QuoteChar				= 17;
 	public final static int	DoubleQuoteChar			= 18;
 	public final static int	BackQuoteChar			= 19;
 	public final static int	SurprisedChar			= 20;
 	public final static int	SharpChar				= 21;
 	public final static int	DollarChar				= 22;
 	public final static int	PercentChar				= 23;
 	public final static int	AndChar					= 24;
 	public final static int	StarChar				= 25;
 	public final static int	PlusChar				= 26;
 	public final static int	CommaChar				= 27;
 	public final static int	MinusChar				= 28;
 	public final static int	DotChar					= 29;
 	public final static int	SlashChar				= 30;
 	public final static int	ColonChar				= 31;
 	public final static int	SemiColonChar			= 32;
 	public final static int	EqualChar				= 33;
 	public final static int	QuestionChar			= 34;
 	public final static int	AtmarkChar				= 35;
 	public final static int	VarChar					= 36;
 	public final static int	ChilderChar				= 37;
 	public final static int	BackSlashChar			= 38;
 	public final static int	HatChar					= 39;
 	public final static int	UnicodeChar				= 40;
 	public final static int MaxSizeOfChars          = 41;
 
 	public final static int[]	CharMatrix = /*BeginArray*/{
 		/*nul soh stx etx eot enq ack bel*/
 		0, 1, 1, 1, 1, 1, 1, 1,
 		/*bs ht nl vt np cr so si  */
 		1, TabChar, NewLineChar, 1, 1, 1, 1, 1,
 		/*020 dle  021 dc1  022 dc2  023 dc3  024 dc4  025 nak  026 syn  027 etb */
 		1, 1, 1, 1, 1, 1, 1, 1,
 		/*030 can  031 em   032 sub  033 esc  034 fs   035 gs   036 rs   037 us */
 		1, 1, 1, 1, 1, 1, 1, 1,
 		/*040 sp   041  !   042  "   043  #   044  $   045  %   046  &   047  ' */
 		SpaceChar, SurprisedChar, DoubleQuoteChar, SharpChar, DollarChar, PercentChar, AndChar, QuoteChar,
 		/*050  (   051  )   052  *   053  +   054  ,   055  -   056  .   057  / */
 		OpenParChar, CloseParChar, StarChar, PlusChar, CommaChar, MinusChar, DotChar, SlashChar,
 		/*060  0   061  1   062  2   063  3   064  4   065  5   066  6   067  7 */
 		DigitChar, DigitChar, DigitChar, DigitChar, DigitChar, DigitChar, DigitChar, DigitChar,
 		/*070  8   071  9   072  :   073  ;   074  <   075  =   076  >   077  ? */
 		DigitChar, DigitChar, ColonChar, SemiColonChar, LessThanChar, EqualChar, GreaterThanChar, QuestionChar,
 		/*100  @   101  A   102  B   103  C   104  D   105  E   106  F   107  G */
 		AtmarkChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar,
 		/*110  H   111  I   112  J   113  K   114  L   115  M   116  N   117  O */
 		UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar,
 		/*120  P   121  Q   122  R   123  S   124  T   125  U   126  V   127  W */
 		UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar,
 		/*130  X   131  Y   132  Z   133  [   134  \   135  ]   136  ^   137  _ */
 		UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, OpenBracketChar, BackSlashChar, CloseBracketChar, HatChar, UnderBarChar,
 		/*140  `   141  a   142  b   143  c   144  d   145  e   146  f   147  g */
 		BackQuoteChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar,
 		/*150  h   151  i   152  j   153  k   154  l   155  m   156  n   157  o */
 		LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar,
 		/*160  p   161  q   162  r   163  s   164  t   165  u   166  v   167  w */
 		LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar,
 		/*170  x   171  y   172  z   173  {   174  |   175  }   176  ~   177 del*/
 		LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, OpenBraceChar, VarChar, CloseBraceChar, ChilderChar, 1,
 		/*EndArray*/};
 
 	public final static GtToken NullToken = new GtToken("", 0);
 
 	// TokenFlag
 	public final static int	SourceTokenFlag	= 1;
 	public final static int	ErrorTokenFlag	= (1 << 1);
 	public final static int IndentTokenFlag	= (1 << 2);
 	public final static int	WhiteSpaceTokenFlag	= (1 << 3);
 	public final static int DelimTokenFlag	= (1 << 4);
 
 	// ParseFlag
 	public final static int	TrackbackParseFlag	= 1;
 	public final static int	SkipIndentParseFlag	= (1 << 1);
 
 	// SyntaxTree
 	public final static int NoWhere         = -1;
 	// UnaryTree, SuffixTree
 	public final static int UnaryTerm      = 0;
 	// BinaryTree
 	public final static int	LeftHandTerm	= 0;
 	public final static int	RightHandTerm	= 1;
 
 	// IfStmt
 	public final static int	IfCond	= 0;
 	public final static int	IfThen	= 1;
 	public final static int	IfElse	= 2;
 
 	// while(cond) {...}
 	static final int WhileCond = 0;
 	static final int WhileBody = 1;
 
 	// ReturnStmt
 	public final static int	ReturnExpr	= 0;
 
 	// var N = 1;
 	public final static int	VarDeclType		= 0;
 	public final static int	VarDeclName		= 1;
 	public final static int	VarDeclValue	= 2;
 	public final static int	VarDeclScope	= 3;
 
 	//Method Call;
 	public static final int	CallExpressionOffset	= 0;
 	public static final int	CallParameterOffset		= 1;
 
 	// Method Decl;
 	public final static int	FuncDeclReturnType	= 0;
 	public final static int	FuncDeclClass		= 1;
 	public final static int	FuncDeclName		= 2;
 	public final static int	FuncDeclBlock		= 3;
 	public final static int	FuncDeclParam		= 4;
 
 	// spec
 	public final static int TokenFuncSpec     = 0;
 	public final static int SymbolPatternSpec = 1;
 	public final static int ExtendedPatternSpec = 2;
 
 	public final static int BinaryOperator					= 1;
 	public final static int LeftJoin						= 1 << 1;
 	public final static int PrecedenceShift					= 2;
 	public final static int Precedence_CStyleValue			= (1 << PrecedenceShift);
 	public final static int Precedence_CPPStyleScope		= (50 << PrecedenceShift);
 	public final static int Precedence_CStyleSuffixCall		= (100 << PrecedenceShift);				/*x(); x[]; x.x x->x x++ */
 	public final static int Precedence_CStylePrefixOperator	= (200 << PrecedenceShift);				/*++x; --x; sizeof x &x +x -x !x (T)x  */
 	//	Precedence_CppMember      = 300;  /* .x ->x */
 	public final static int Precedence_CStyleMUL			= (400 << PrecedenceShift);				/* x * x; x / x; x % x*/
 	public final static int Precedence_CStyleADD			= (500 << PrecedenceShift);				/* x + x; x - x */
 	public final static int Precedence_CStyleSHIFT			= (600 << PrecedenceShift);				/* x << x; x >> x */
 	public final static int Precedence_CStyleCOMPARE		= (700 << PrecedenceShift);
 	public final static int Precedence_CStyleEquals			= (800 << PrecedenceShift);
 	public final static int Precedence_CStyleBITAND			= (900 << PrecedenceShift);
 	public final static int Precedence_CStyleBITXOR			= (1000 << PrecedenceShift);
 	public final static int Precedence_CStyleBITOR			= (1100 << PrecedenceShift);
 	public final static int Precedence_CStyleAND			= (1200 << PrecedenceShift);
 	public final static int Precedence_CStyleOR				= (1300 << PrecedenceShift);
 	public final static int Precedence_CStyleTRINARY		= (1400 << PrecedenceShift);				/* ? : */
 	public final static int Precedence_CStyleAssign			= (1500 << PrecedenceShift);
 	public final static int Precedence_CStyleCOMMA			= (1600 << PrecedenceShift);
 	public final static int Precedence_Error				= (1700 << PrecedenceShift);
 	public final static int Precedence_Statement			= (1900 << PrecedenceShift);
 	public final static int Precedence_CStyleDelim			= (2000 << PrecedenceShift);
 
 	public final static int DefaultTypeCheckPolicy			= 0;
 	public final static int NoCheckPolicy                   = 1;
 	public final static int IgnoreEmptyPolicy               = (1 << 1);
 	public final static int AllowEmptyPolicy                = (1 << 2);
 	public final static int AllowVoidPolicy                 = (1 << 3);
 	public final static int AllowCoercionPolicy             = (1 << 4);
 
 	public final static String	GlobalConstName					= "global";
 
 	public final static ArrayList<String> SymbolList = new ArrayList<String>();
 	public final static GtMap   SymbolMap  = new GtMap();
 	public final static GtMap   MangleNameMap = new GtMap();
 
 	public final static GtMethod AnyGetter = null;
 
 	// debug flags
 	public static final boolean	UseBuiltInTest	= true;
 	public static final boolean	DebugPrintOption = true;
 
 	// TestFlags (temporary)
 	static final int TestTokenizer = 1 << 0;
 	static final int TestParseOnly = 1 << 1;
 	static final int TestTypeChecker = 1 << 2;
 	static final int TestCodeGeneration = 1 << 3;
 
 //ifdef JAVA
 }
 
 class GtStatic implements GtConst {
 //endif VAJA
 	public final static void println(String msg) {
 		LangDeps.println(msg);
 	}
 
 	public final static void DebugP(String msg) {
 		if(DebugPrintOption) {
 			LangDeps.println("DEBUG" + LangDeps.GetStackInfo(2) + ": " + msg);
 		}
 	}
 
 	public final static void TODO(String msg) {
 		LangDeps.println("TODO" + LangDeps.GetStackInfo(2) + ": " + msg);
 	}
 
 	public final static String JoinStrings(String Unit, int Times) {
 		/*local*/String s = "";
 		/*local*/int i = 0;
 		while(i < Times) {
 			s = s + Unit;
 			i = i + 1;
 		}
 		return s;
 	}
 
 	public final static int ListSize(ArrayList<?> a) {
 		if(a == null){
 			return 0;
 		}
 		return a.size();
 	}
 
 	public final static boolean IsFlag(int flag, int flag2) {
 		return ((flag & flag2) == flag2);
 	}
 
 	public final static int FromJavaChar(char c) {
 		if(c < 128) {
 			return CharMatrix[c];
 		}
 		return UnicodeChar;
 	}
 
 	// Symbol
 	public final static boolean IsGetterSymbol(int SymbolId) {
 		return IsFlag(SymbolId, GetterSymbolMask);
 	}
 
 	public final static boolean IsSetterSymbol(int SymbolId) {
 		return IsFlag(SymbolId, SetterSymbolMask);
 	}
 
 	public final static int ToSetterSymbol(int SymbolId) {
 		LangDeps.Assert(IsGetterSymbol(SymbolId));
 		return (SymbolId & (~GetterSymbolMask)) | SetterSymbolMask;
 	}
 
 	public final static int MaskSymbol(int SymbolId, int Mask) {
 		return (SymbolId << SymbolMaskSize) | Mask;
 	}
 
 	public final static int UnmaskSymbol(int SymbolId) {
 		return SymbolId >> SymbolMaskSize;
 	}
 
 	public final static String StringfySymbol(int SymbolId) {
 		/*local*/String Key = SymbolList.get(UnmaskSymbol(SymbolId));
 		if(IsFlag(SymbolId, GetterSymbolMask)) {
 			return GetterPrefix + Key;
 		}
 		if(IsFlag(SymbolId, SetterSymbolMask)) {
 			return SetterPrefix + Key;
 		}
 		if(IsFlag(SymbolId, MetaSymbolMask)) {
 			return MetaPrefix + Key;
 		}
 		return Key;
 	}
 
 	public final static int GetSymbolId(String Symbol, int DefaultSymbolId) {
 		/*local*/String Key = Symbol;
 		/*local*/int Mask = 0;
 		if(Symbol.length() >= 3 && LangDeps.CharAt(Symbol, 1) == 'e' && LangDeps.CharAt(Symbol, 2) == 't') {
 			if(LangDeps.CharAt(Symbol, 0) == 'g' && LangDeps.CharAt(Symbol, 0) == 'G') {
 				Key = Symbol.substring(3);
 				Mask = GetterSymbolMask;
 			}
 			if(LangDeps.CharAt(Symbol, 0) == 's' && LangDeps.CharAt(Symbol, 0) == 'S') {
 				Key = Symbol.substring(3);
 				Mask = SetterSymbolMask;
 			}
 		}
 		if(Symbol.startsWith("\\")) {
 			Mask = MetaSymbolMask;
 		}
 		/*local*/Integer SymbolObject = (/*cast*/Integer)SymbolMap.get(Key);
 		if(SymbolObject == null) {
 			if(DefaultSymbolId == AllowNewId) {
 				/*local*/int SymbolId = SymbolList.size();
 				SymbolList.add(Key);
 				SymbolMap.put(Key, SymbolId); //new Integer(SymbolId));
 				return MaskSymbol(SymbolId, Mask);
 			}
 			return DefaultSymbolId;
 		}
 		return MaskSymbol(SymbolObject.intValue(), Mask);
 	}
 
 	public final static String CanonicalSymbol(String Symbol) {
 		return Symbol.toLowerCase().replaceAll("_", "");
 	}
 
 	public final static int GetCanonicalSymbolId(String Symbol) {
 		return GetSymbolId(CanonicalSymbol(Symbol), AllowNewId);
 	}
 
 	public final static String NumberToAscii(int number) {
 		int num = number /26;
 		String s = Character.toString((char)(65 + (number % 26)));
 		if(num == 0) {
 			return s;
 		}
 		else {
 			return NumberToAscii(num) + s;
 		}
 	}
 
 	public final static String Mangle(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList) {
 		/*local*/String s = NumberToAscii(BaseType.ClassId);
 		/*local*/int i = BaseIdx;
 		while(i < ListSize(TypeList)) {
 			s = s + "." + NumberToAscii(TypeList.get(i).ClassId);
 			i = i + 1;
 		}
 		String MangleName = (/*cast*/String)MangleNameMap.get(s);
 		if(MangleName == null) {
 			MangleName = NumberToAscii(MangleNameMap.size());
 			MangleNameMap.put(s, MangleName);
 		}
 		return MangleName;
 	}
 
 //ifdef JAVA
 	public final static GtDelegateToken FunctionA(Object Callee, String MethodName) {
 		return new GtDelegateToken(Callee, LangDeps.LookupMethod(Callee, MethodName));
 	}
 
 	public final static GtDelegateMatch FunctionB(Object Callee, String MethodName) {
 		return new GtDelegateMatch(Callee, LangDeps.LookupMethod(Callee, MethodName));
 	}
 
 	public final static GtDelegateType FunctionC(Object Callee, String MethodName) {
 		return new GtDelegateType(Callee, LangDeps.LookupMethod(Callee, MethodName));
 	}
 //endif VAJA
 
 	public final static int ApplyTokenFunc(TokenFunc TokenFunc, TokenContext TokenContext, String ScriptSource, int Pos) {
 		while(TokenFunc != null) {
 			/*local*/GtDelegateToken delegate = TokenFunc.Func;
 			/*local*/int NextIdx = LangDeps.ApplyTokenFunc(delegate.Self, delegate.Method, TokenContext, ScriptSource, Pos);
 			if(NextIdx > Pos) return NextIdx;
 			TokenFunc = TokenFunc.ParentFunc;
 		}
 		return NoMatch;
 	}
 
 	public final static SyntaxPattern MergeSyntaxPattern(SyntaxPattern Pattern, SyntaxPattern Parent) {
 		if(Parent == null) return Pattern;
 		/*local*/SyntaxPattern MergedPattern = new SyntaxPattern(Pattern.PackageNameSpace, Pattern.PatternName, Pattern.MatchFunc, Pattern.TypeFunc);
 		MergedPattern.ParentPattern = Parent;
 		return MergedPattern;
 	}
 
 	public final static boolean IsEmptyOrError(SyntaxTree Tree) {
 		return Tree == null || Tree.IsEmptyOrError();
 	}
 
 	public final static SyntaxTree LinkTree(SyntaxTree LastNode, SyntaxTree Node) {
 		Node.PrevTree = LastNode;
 		if(LastNode != null) {
 			LastNode.NextTree = Node;
 		}
 		return Node;
 	}
 
 	public final static SyntaxTree TreeHead(SyntaxTree Tree) {
 		if(Tree != null) {
 			while(Tree.PrevTree != null) {
 				Tree = Tree.PrevTree;
 			}
 		}
 		return Tree;
 	}
 
 	public final static SyntaxTree ApplySyntaxPattern(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/int Pos = TokenContext.Pos;
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		/*local*/SyntaxPattern CurrentPattern = Pattern;
 		while(CurrentPattern != null) {
 			/*local*/GtDelegateMatch delegate = CurrentPattern.MatchFunc;
 			TokenContext.Pos = Pos;
 			if(CurrentPattern.ParentPattern != null) {
 				TokenContext.ParseFlag = ParseFlag | TrackbackParseFlag;
 			}
 			DebugP("B :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + ", next=" + CurrentPattern.ParentPattern);
 			TokenContext.IndentLevel += 1;
 			/*local*/SyntaxTree ParsedTree = (/*cast*/SyntaxTree)LangDeps.ApplyMatchFunc(delegate.Self, delegate.Method, CurrentPattern, LeftTree, TokenContext);
 			TokenContext.IndentLevel -= 1;
 			if(ParsedTree != null && ParsedTree.IsEmpty()) ParsedTree = null;
 			DebugP("E :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + " => " + ParsedTree);
 			TokenContext.ParseFlag = ParseFlag;
 			if(ParsedTree != null) {
 				return ParsedTree;
 			}
 			CurrentPattern = CurrentPattern.ParentPattern;
 		}
 		if(TokenContext.IsAllowedTrackback()) {
 			TokenContext.Pos = Pos;
 		}
 		if(Pattern == null) {
 			DebugP("undefined syntax pattern: " + Pattern);
 		}
 		return TokenContext.ReportExpectedPattern(Pattern);
 	}
 
 	public final static SyntaxTree ParseExpression(TokenContext TokenContext) {
 		/*local*/SyntaxPattern Pattern = TokenContext.GetFirstPattern();
 		/*local*/SyntaxTree LeftTree = GtStatic.ApplySyntaxPattern(Pattern, null, TokenContext);
 		while(!GtStatic.IsEmptyOrError(LeftTree) && !TokenContext.MatchToken(";")) {
 			/*local*/SyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern();
 			if(ExtendedPattern == null) {
 				DebugP("In $Expression$ ending: " + TokenContext.GetToken());
 				break;
 			}
 			LeftTree = GtStatic.ApplySyntaxPattern(ExtendedPattern, LeftTree, TokenContext);
 		}
 		return LeftTree;
 	}
 
 	// typing
 	public final static TypedNode ApplyTypeFunc(GtDelegateType delegate, TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		if(delegate == null || delegate.Method == null){
 			DebugP("try to invoke null TypeFunc");
 			return null;
 		}
 		return (/*cast*/TypedNode)LangDeps.ApplyTypeFunc(delegate.Self, delegate.Method, Gamma, ParsedTree, Type);
 	}
 
 	public final static TypedNode LinkNode(TypedNode LastNode, TypedNode Node) {
 		Node.PrevNode = LastNode;
 		if(LastNode != null) {
 			LastNode.NextNode = Node;
 		}
 		return Node;
 	}
 
 	public final static void TestToken(GtContext Context, String Source, String TokenText, String TokenText2) {
 		/*local*/GtNameSpace NameSpace = Context.DefaultNameSpace;
 		/*local*/TokenContext TokenContext = new TokenContext(NameSpace, Source, 1);
 		LangDeps.Assert(TokenContext.MatchToken(TokenText) && TokenContext.MatchToken(TokenText2));
 	}
 
 	public final static void TestSyntaxPattern(GtContext Context, String Text) {
 		/*local*/int TestLevel = TestTypeChecker;
 		/*local*/GtNameSpace NameSpace = Context.DefaultNameSpace;
 		/*local*/TokenContext TokenContext = new TokenContext(NameSpace, Text, 1);
 		/*local*/SyntaxTree ParsedTree = GtStatic.ParseExpression(TokenContext);
 		LangDeps.Assert(ParsedTree != null);
 		if((TestLevel & TestTypeChecker) != TestTypeChecker) {
 			return;
 		}
 		/*local*/TypeEnv Gamma = new TypeEnv(NameSpace);
 		/*local*/TypedNode TNode = Gamma.TypeCheck(ParsedTree, Gamma.AnyType, DefaultTypeCheckPolicy);
 		System.out.println(TNode.toString());
 		if((TestLevel & TestCodeGeneration) == TestCodeGeneration) {
 		}
 	}
 
 
 //ifdef JAVA
 }
 
 final class GtMap {
 	private final HashMap<String, Object>	Map;
 
 	public GtMap() {
 		this.Map = new HashMap<String, Object>();
 	}
 
 	public int size() {
 		return this.Map.size();
 	}
 
 	public void put(String Key, Object Value) {
 		this.Map.put(Key, Value);
 	}
 
 	public Object get(String Key) {
 		return this.Map.get(Key);
 	}
 
 	public ArrayList<String> keys() {
 		GtStatic.TODO("implement");
 		return null;
 	}
 }
 
 class GtDelegateCommon {
 	/*field*/public Object	Self;
 	/*field*/public Method	Method;
 	GtDelegateCommon(Object Self, Method method) {
 		this.Self = Self;
 		this.Method = method;
 	}
 	@Override public final String toString() {
 		if(this.Method == null){
 			return "*undefined*";
 		}
 		return this.Method.getName();
 	}
 }
 
 final class GtDelegateToken extends GtDelegateCommon {
 	GtDelegateToken/*constructor*/(Object Self, Method method) {
 		super(Self, method);
 	}
 }
 
 final class GtDelegateMatch extends GtDelegateCommon {
 	GtDelegateMatch/*constructor*/(Object Self, Method method) {
 		super(Self, method);
 	}
 }
 
 final class GtDelegateType extends GtDelegateCommon {
 	GtDelegateType/*constructor*/(Object Self, Method method) {
 		super(Self, method);
 	}
 }
 
 //endif VAJA
 
 // tokenizer
 
 final class GtToken extends GtStatic {
 	/*field*/public int		        TokenFlag;
 	/*field*/public String	        ParsedText;
 	/*field*/public long		    FileLine;
 	/*field*/public SyntaxPattern	PresetPattern;
 
 	GtToken/*constructor*/(String text, long FileLine) {
 		this.TokenFlag = 0;
 		this.ParsedText = text;
 		this.FileLine = FileLine;
 		this.PresetPattern = null;
 	}
 
 	public boolean IsSource() {
 		return IsFlag(this.TokenFlag, SourceTokenFlag);
 	}
 
 	public boolean IsError() {
 		return IsFlag(this.TokenFlag, ErrorTokenFlag);
 	}
 
 	public boolean IsIndent() {
 		return IsFlag(this.TokenFlag, IndentTokenFlag);
 	}
 
 	public boolean IsDelim() {
 		return IsFlag(this.TokenFlag, DelimTokenFlag);
 	}
 
 	public boolean EqualsText(String text) {
 		return this.ParsedText.equals(text);
 	}
 
 	@Override public String toString() {
 		/*local*/String TokenText = "";
 		if(this.PresetPattern != null) {
 			TokenText = "(" + this.PresetPattern.PatternName + ") ";
 		}
 		return TokenText + this.ParsedText;
 	}
 
 	public String ToErrorToken(String Message) {
 		this.TokenFlag = ErrorTokenFlag;
 		this.ParsedText = Message;
 		return Message;
 	}
 
 	public String GetErrorMessage() {
 		LangDeps.Assert(this.IsError());
 		return this.ParsedText;
 	}
 }
 
 final class TokenFunc {
 	/*field*/public GtDelegateToken       Func;
 	/*field*/public TokenFunc	ParentFunc;
 
 	TokenFunc/*constructor*/(GtDelegateToken Func, TokenFunc Parent) {
 		this.Func = Func;
 		this.ParentFunc = Parent;
 	}
 
 	@Override public String toString() {
 		return this.Func.Method.toString();
 	}
 }
 
 final class TokenContext extends GtStatic {
 	/*field*/public GtNameSpace NameSpace;
 	/*field*/public ArrayList<GtToken> SourceList;
 	/*field*/public int Pos;
 	/*field*/public long ParsingLine;
 	/*field*/public int ParseFlag;
 	/*field*/public int IndentLevel = 0;
 
 	TokenContext/*constructor*/(GtNameSpace NameSpace, String Text, long FileLine) {
 		this.NameSpace = NameSpace;
 		this.SourceList = new ArrayList<GtToken>();
 		this.Pos = 0;
 		this.ParsingLine = FileLine;
 		this.ParseFlag = 0;
 		this.AddNewToken(Text, SourceTokenFlag, null);
 		this.IndentLevel = 0;
 	}
 
 	public GtToken AddNewToken(String Text, int TokenFlag, String PatternName) {
 		/*local*/GtToken Token = new GtToken(Text, this.ParsingLine);
 		Token.TokenFlag |= TokenFlag;
 		if(PatternName != null) {
 			Token.PresetPattern = this.NameSpace.GetPattern(PatternName);
 			LangDeps.Assert(Token.PresetPattern != null);
 		}
 		//DebugP("<< " + Text + " : " + PatternName);
 		this.SourceList.add(Token);
 		return Token;
 	}
 
 	public void FoundWhiteSpace() {
 		/*local*/GtToken Token = this.GetToken();
 		Token.TokenFlag |= WhiteSpaceTokenFlag;
 	}
 
 	public void FoundLineFeed(long line) {
 		this.ParsingLine += line;
 	}
 
 	public void ReportTokenError(int Level, String Message, String TokenText) {
 		/*local*/GtToken Token = this.AddNewToken(TokenText, 0, "$Error$");
 		this.NameSpace.ReportError(Level, Token, Message);
 	}
 
 	public SyntaxTree NewErrorSyntaxTree(GtToken Token, String Message) {
 		if(!this.IsAllowedTrackback()) {
 			this.NameSpace.ReportError(ErrorLevel, Token, Message);
 			/*local*/SyntaxTree ErrorTree = new SyntaxTree(Token.PresetPattern, this.NameSpace, Token, null);
 			return ErrorTree;
 		}
 		return null;
 	}
 
 	public GtToken GetBeforeToken() {
 		/*local*/int pos = this.Pos - 1;
 		while(pos >= 0) {
 			/*local*/GtToken Token = this.SourceList.get(pos);
 			if(IsFlag(Token.TokenFlag, IndentTokenFlag)) {
 				pos -= 1;
 				continue;
 			}
 			return Token;
 		}
 		return null;
 	}
 
 	public SyntaxTree ReportExpectedToken(String TokenText) {
 		if(!this.IsAllowedTrackback()) {
 			/*local*/GtToken Token = this.GetBeforeToken();
 			if(Token != null) {
 				return this.NewErrorSyntaxTree(Token, TokenText + " is expected after " + Token.ParsedText);
 			}
 			Token = this.GetToken();
 			LangDeps.Assert(Token != NullToken);
 			return this.NewErrorSyntaxTree(Token, TokenText + " is expected at " + Token.ParsedText);
 		}
 		return null;
 	}
 
 	public SyntaxTree ReportExpectedPattern(SyntaxPattern Pattern) {
 		if(Pattern == null){
 			return this.ReportExpectedToken("null");
 		}
 		return this.ReportExpectedToken(Pattern.PatternName);
 	}
 
 	public void Vacume() {
 		if(this.Pos > 0) {
 			/*local*/ArrayList<GtToken> NewList = new ArrayList<GtToken>();
 			/*local*/int i = this.Pos;
 			while(i < ListSize(this.SourceList)) {
 				NewList.add(this.SourceList.get(i));
 				i = i + 1;
 			}
 			this.SourceList = NewList;
 			this.Pos = 0;
 		}
 	}
 
 	private int DispatchFunc(String ScriptSource, int GtChar, int pos) {
 		/*local*/TokenFunc TokenFunc = this.NameSpace.GetTokenFunc(GtChar);
 		/*local*/int NextIdx = GtStatic.ApplyTokenFunc(TokenFunc, this, ScriptSource, pos);
 		if(NextIdx == NoMatch) {
 			DebugP("undefined tokenizer: " + LangDeps.CharAt(ScriptSource, pos));
 			this.AddNewToken(ScriptSource.substring(pos, pos + 1), 0, null);
 			return pos + 1;
 		}
 		return NextIdx;
 	}
 
 	private void Tokenize(String ScriptSource, long CurrentLine) {
 		/*local*/int currentPos = 0;
 		/*local*/int len = ScriptSource.length();
 		this.ParsingLine = CurrentLine;
 		while(currentPos < len) {
 			/*local*/int gtCode = FromJavaChar(LangDeps.CharAt(ScriptSource, currentPos));
 			/*local*/int nextPos = this.DispatchFunc(ScriptSource, gtCode, currentPos);
 			if(currentPos >= nextPos) {
 				break;
 			}
 			currentPos = nextPos;
 		}
 		this.Dump();
 	}
 
 	public GtToken GetToken() {
 		while((this.Pos < this.SourceList.size())) {
 			/*local*/GtToken Token = this.SourceList.get(this.Pos);
 			if(Token.IsSource()) {
 				this.SourceList.remove(this.SourceList.size()-1);
 				this.Tokenize(Token.ParsedText, Token.FileLine);
 				Token = this.SourceList.get(this.Pos);
 			}
 			if(IsFlag(this.ParseFlag, SkipIndentParseFlag) && Token.IsIndent()) {
 				this.Pos = this.Pos + 1;
 				continue;
 			}
 			return Token;
 		}
 		return NullToken;
 	}
 
 	public boolean HasNext() {
 		return (this.GetToken() != NullToken);
 	}
 
 	public GtToken Next() {
 		/*local*/GtToken Token = this.GetToken();
 		this.Pos += 1;
 		return Token;
 	}
 
 	public SyntaxPattern GetPattern(String PatternName) {
 		return this.NameSpace.GetPattern(PatternName);
 	}
 
 	public SyntaxPattern GetFirstPattern() {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token.PresetPattern != null) {
 			return Token.PresetPattern;
 		}
 		/*local*/SyntaxPattern Pattern = this.NameSpace.GetPattern(Token.ParsedText);
 		if(Pattern == null) {
 			return this.NameSpace.GetPattern("$Symbol$");
 		}
 		return Pattern;
 	}
 
 	public SyntaxPattern GetExtendedPattern() {
 		/*local*/GtToken Token = this.GetToken();
 		/*local*/SyntaxPattern Pattern = this.NameSpace.GetExtendedPattern(Token.ParsedText);
 		return Pattern;
 	}
 
 	public boolean MatchToken(String TokenText) {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token.EqualsText(TokenText)) {
 			this.Pos += 1;
 			return true;
 		}
 		return false;
 	}
 
 	public GtToken GetMatchedToken(String TokenText) {
 		/*local*/GtToken Token = this.GetToken();
 		while(Token != NullToken) {
 			this.Pos += 1;
 			if(Token.EqualsText(TokenText)) {
 				break;
 			}
 			Token = this.GetToken();
 		}
 		return Token;
 	}
 
 	public final boolean IsAllowedTrackback() {
 		return IsFlag(this.ParseFlag, TrackbackParseFlag);
 	}
 
 	public final int SetTrackback(boolean Allowed) {
 		/*local*/int ParseFlag = this.ParseFlag;
 		if(Allowed) {
 			this.ParseFlag = this.ParseFlag | TrackbackParseFlag;
 		}
 		else {
 			this.ParseFlag = (~(TrackbackParseFlag) & this.ParseFlag);
 		}
 		return ParseFlag;
 	}
 
 	public final SyntaxTree ParsePatternAfter(SyntaxTree LeftTree, String PatternName, boolean IsOptional) {
 		/*local*/int Pos = this.Pos;
 		/*local*/int ParseFlag = this.ParseFlag;
 		/*local*/SyntaxPattern Pattern = this.GetPattern(PatternName);
 		if(IsOptional) {
 			this.ParseFlag = this.ParseFlag | TrackbackParseFlag;
 		}
 		/*local*/SyntaxTree SyntaxTree = GtStatic.ApplySyntaxPattern(Pattern, LeftTree, this);
 		this.ParseFlag = ParseFlag;
 		if(SyntaxTree != null) {
 			return SyntaxTree;
 		}
 		this.Pos = Pos;
 		return null;
 	}
 
 	public final SyntaxTree ParsePattern(String PatternName, boolean IsOptional) {
 		return this.ParsePatternAfter(null, PatternName, IsOptional);
 	}
 
 	public final boolean SkipEmptyStatement() {
 		/*local*/GtToken Token = null;
 		while((Token = this.GetToken()) != NullToken) {
 			if(Token.IsIndent() || Token.IsDelim()) {
 				this.Pos += 1;
 				continue;
 			}
 			break;
 		}
 		return (Token != NullToken);
 	}
 
 	public final GtMap SkipAnnotation() {
 		//TODO: Parse Annotation
 		return null;
 	}
 
 	public void Dump() {
 		/*local*/int pos = this.Pos;
 		while(pos < this.SourceList.size()) {
 			/*local*/GtToken token = this.SourceList.get(pos);
 			DebugP("["+pos+"]\t" + token + " : " + token.PresetPattern);
 			pos += 1;
 		}
 	}
 }
 
 final class SyntaxPattern extends GtStatic {
 	/*field*/public GtNameSpace	          PackageNameSpace;
 	/*field*/public String		          PatternName;
 	/*field*/int				          SyntaxFlag;
 	/*field*/public GtDelegateMatch       MatchFunc;
 	/*field*/public GtDelegateType        TypeFunc;
 	/*field*/public SyntaxPattern	      ParentPattern;
 
 	SyntaxPattern/*constructor*/(GtNameSpace NameSpace, String PatternName, GtDelegateMatch MatchFunc, GtDelegateType TypeFunc) {
 		this.PackageNameSpace = NameSpace;
 		this.PatternName = PatternName;
 		this.SyntaxFlag = 0;
 		this.MatchFunc = MatchFunc;
 		this.TypeFunc  = TypeFunc;
 		this.ParentPattern = null;
 	}
 
 	@Override public String toString() {
 		return this.PatternName + "<" + this.MatchFunc + ">";
 	}
 
 	public boolean IsBinaryOperator() {
 		return IsFlag(this.SyntaxFlag, BinaryOperator);
 	}
 
 	public boolean IsLeftJoin(SyntaxPattern Right) {
 		/*local*/int left = this.SyntaxFlag >> PrecedenceShift;
 		/*local*/int right = Right.SyntaxFlag >> PrecedenceShift;
 		return (left < right || (left == right && IsFlag(this.SyntaxFlag, LeftJoin) && IsFlag(Right.SyntaxFlag, LeftJoin)));
 	}
 }
 
 class SyntaxTree extends GtStatic {
 	/*field*/public SyntaxTree		ParentTree;
 	/*field*/public SyntaxTree		PrevTree;
 	/*field*/public SyntaxTree		NextTree;
 
 	/*field*/public GtNameSpace	    NameSpace;
 	/*field*/public SyntaxPattern	Pattern;
 	/*field*/public GtToken		    KeyToken;
 	/*field*/public ArrayList<SyntaxTree> TreeList;
 	/*field*/public Object          ConstValue;
 	/*field*/public GtMap           Annotation;
 
 	SyntaxTree/*constructor*/(SyntaxPattern Pattern, GtNameSpace NameSpace, GtToken KeyToken, Object ConstValue) {
 		this.NameSpace = NameSpace;
 		this.KeyToken = KeyToken;
 		this.Pattern = Pattern;
 		this.ParentTree = null;
 		this.PrevTree = null;
 		this.NextTree = null;
 		this.TreeList = null;
 		this.ConstValue = ConstValue;
 		this.Annotation = null;
 	}
 
 	@Override public String toString() {
 		/*local*/String s = "(" + this.KeyToken.ParsedText;
 		/*local*/int i = 0;
 		while(i < ListSize(this.TreeList)) {
 			/*local*/SyntaxTree SubTree = this.TreeList.get(i);
 			while(SubTree != null){
 				/*local*/String Entry = SubTree.toString();
 				if(ListSize(SubTree.TreeList) == 0) {
 					Entry = SubTree.KeyToken.ParsedText;
 				}
 				s = s + " " + Entry;
 				SubTree = SubTree.NextTree;
 			}
 			i += 1;
 		}
 		return s + ")";
 	}
 
 	public void SetAnnotation(GtMap Annotation) {
 		this.Annotation = Annotation;
 	}
 
 	public boolean HasAnnotation(String Key) {
 		if(this.Annotation != null) {
 			/*local*/Object Value = this.Annotation.get(Key);
 			if(Value instanceof Boolean) {
 				this.Annotation.put(Key, true);  // consumed;
 			}
 			return (Value != null);
 		}
 		return false;
 	}
 
 	public boolean IsError() {
 		return this.KeyToken.IsError();
 	}
 
 	public void ToError(GtToken Token) {
 		LangDeps.Assert(Token.IsError());
 		this.KeyToken = Token;
 		this.TreeList = null;
 	}
 
 	public boolean IsEmpty() {
 		return this.KeyToken == NullToken;
 	}
 
 	public void ToEmpty() {
 		this.KeyToken = NullToken;
 		this.TreeList = null;
 		this.Pattern = this.NameSpace.GetPattern("$Empty$");
 	}
 
 	public boolean IsEmptyOrError() {
 		return this.KeyToken == NullToken || this.KeyToken.IsError();
 	}
 
 	public void ToEmptyOrError(SyntaxTree ErrorTree) {
 		if(ErrorTree == null) {
 			this.ToEmpty();
 		}
 		else {
 			this.ToError(ErrorTree.KeyToken);
 		}
 	}
 
 	public SyntaxTree GetSyntaxTreeAt(int Index) {
 		if(this.TreeList != null && Index >= this.TreeList.size()) {
 			return null;
 		}
 		return this.TreeList.get(Index);
 	}
 
 	public void SetSyntaxTreeAt(int Index, SyntaxTree Tree) {
 		if(!this.IsError()) {
 			if(Tree.IsError()) {
 				this.ToError(Tree.KeyToken);
 			}
 			else {
 				if(Index >= 0) {
 					if(this.TreeList == null) {
 						this.TreeList = new ArrayList<SyntaxTree>();
 					}
 					if(Index < this.TreeList.size()) {
 						this.TreeList.set(Index, Tree);
 						return;
 					}
 					while(this.TreeList.size() < Index) {
 						this.TreeList.add(null);
 					}
 					this.TreeList.add(Tree);
 					Tree.ParentTree = this;
 				}
 			}
 		}
 	}
 
 	public void SetMatchedPatternAt(int Index, TokenContext TokenContext, String PatternName,  boolean IsOptional) {
 		if(!this.IsEmptyOrError()) {
 			/*local*/SyntaxTree ParsedTree = TokenContext.ParsePattern(PatternName, IsOptional);
 			if(PatternName.equals("$Expression$") && ParsedTree == null){
 				ParsedTree = GtStatic.ParseExpression(TokenContext);
 			}
 			if(ParsedTree != null) {
 				this.SetSyntaxTreeAt(Index, ParsedTree);
 			}
 			else if(ParsedTree == null && !IsOptional) {
 				this.ToEmpty();
 			}
 		}
 	}
 
 	public void SetMatchedTokenAt(int Index, TokenContext TokenContext, String TokenText, boolean IsOptional) {
 		if(!this.IsEmptyOrError()) {
 			/*local*/int Pos = TokenContext.Pos;
 			/*local*/GtToken Token = TokenContext.Next();
 			if(Token.ParsedText.equals(TokenText)) {
 				this.SetSyntaxTreeAt(Index, new SyntaxTree(null, TokenContext.NameSpace, Token, null));
 			}
 			else {
 				TokenContext.Pos = Pos;
 				if(!IsOptional) {
 					this.ToEmptyOrError(TokenContext.ReportExpectedToken(TokenText));
 				}
 			}
 		}
 	}
 
 	public void AppendParsedTree(SyntaxTree Tree) {
 		if(!this.IsError()) {
 			if(Tree.IsError()) {
 				this.ToError(Tree.KeyToken);
 			}
 			else {
 				if(this.TreeList == null) {
 					this.TreeList = new ArrayList<SyntaxTree>();
 				}
 				this.TreeList.add(Tree);
 			}
 		}
 	}
 
 	public final TypedNode TypeNodeAt(int Index, TypeEnv Gamma, GtType Type, int TypeCheckPolicy) {
 		if(this.TreeList != null && Index < this.TreeList.size()) {
 			/*local*/SyntaxTree NodeObject = this.TreeList.get(Index);
 			/*local*/TypedNode TypedNode = Gamma.TypeCheck(NodeObject, Type, TypeCheckPolicy);
 			return TypedNode;
 		}
 		if(!IsFlag(TypeCheckPolicy, AllowEmptyPolicy) && !IsFlag(TypeCheckPolicy, IgnoreEmptyPolicy)) {
 			Gamma.NameSpace.ReportError(ErrorLevel, this.KeyToken, this.KeyToken.ParsedText + " needs more expression at " + Index);
 			return Gamma.Generator.CreateErrorNode(Type, this); // TODO, "syntax tree error: " + Index);
 		}
 		return null;
 	}
 }
 
 /* typing */
 
 /* builder */
 class GtObject extends GtStatic {
 	/*field*/public GtType	Type;
 	GtObject/*constructor*/(GtType Type) {
 		this.Type = Type;
 	}
 }
 
 class GtType extends GtStatic {
 	/*field*/public GtNameSpace     PackageNameSpace;
 	/*field*/int					ClassFlag;
 	/*field*/public GtContext		Context;
 	/*field*/int                    ClassId;
 	/*field*/public String			ShortClassName;
 	/*field*/GtType					SuperClass;
 	/*field*/public GtType			SearchSuperMethodClass;
 	/*field*/public Object			DefaultNullValue;
 	/*field*/GtType					BaseClass;
 	/*field*/GtType[]				Types;
 	/*field*/public Object          LocalSpec;
 
 	GtType/*constructor*/(GtContext Context, int ClassFlag, String ClassName, Object DefaultNullValue) {
 		this.Context = Context;
 		this.ClassFlag = ClassFlag;
 		this.ShortClassName = ClassName;
 		this.SuperClass = null;
 		this.BaseClass = this;
 		this.SearchSuperMethodClass = null;
 		this.DefaultNullValue = DefaultNullValue;
 		this.LocalSpec = null;
 		this.ClassId = Context.ClassCount;
 		Context.ClassCount += 1;
 		this.Types = null;
 		DebugP("new class: " + this.ShortClassName + ", ClassId=" + this.ClassId);
 	}
 
 	public final boolean IsGenericType() {
 		return (this.Types != null);
 	}
 
 	// Note Don't call this directly. Use Context.GetGenericType instead.
 	public GtType CreateGenericType(int BaseIndex, ArrayList<GtType> TypeList, String ShortName) {
 		GtType GenericType = new GtType(this.Context, this.ClassFlag, ShortName, null);
 		GenericType.BaseClass = this.BaseClass;
 		GenericType.SearchSuperMethodClass = this.BaseClass;
 		GenericType.SuperClass = this.SuperClass;
 		this.Types = LangDeps.CompactTypeList(BaseIndex, TypeList);
 		return GenericType;
 	}
 
 	public void SetParamType(GtType ParamType) {
 		this.Types = new GtType[1];
 		this.Types[0] = ParamType;
 	}
 
 	@Override public String toString() {
 		return this.ShortClassName;
 	}
 
 	public final String GetMethodId(String MethodName) {
 		return "" + this.ClassId + "@" + MethodName;
 	}
 
 	public final boolean Accept(GtType Type) {
 		if(this == Type || this == this.Context.AnyType) {
 			return true;
 		}
 		return false;
 	}
 }
 
 class GtMethod extends GtStatic {
 	/*field*/public GtLayer         Layer;
 	/*field*/public int				MethodFlag;
 	/*field*/int					MethodSymbolId;
 	/*field*/public String			MethodName;
 	/*field*/public String          LocalFuncName;
 	/*field*/public GtType[]		Types;
 	/*field*/public GtMethod        ElderMethod;
 
 	GtMethod/*constructor*/(int MethodFlag, String MethodName, ArrayList<GtType> ParamList) {
 		super();
 		this.MethodFlag = MethodFlag;
 		this.MethodName = MethodName;
 		this.MethodSymbolId = GtStatic.GetCanonicalSymbolId(MethodName);
 		this.Types = LangDeps.CompactTypeList(0, ParamList);
 		LangDeps.Assert(this.Types.length > 0);
 		this.Layer = null;
 		this.ElderMethod = null;
 		
 		String Name = this.MethodName;
 		if(!LangDeps.IsLetter(LangDeps.CharAt(Name, 0))) {
 			Name = "operator" + this.MethodSymbolId;
 		}
 		if(!this.Is(ExportMethod)) {
 			Name = Name + "__" + GtStatic.Mangle(this.GetRecvType(), 1, ParamList);
 		}
 		this.LocalFuncName = Name;
 	}
 
 	@Override public String toString() {
 		/*local*/String s = this.MethodName + "(";
 		/*local*/int i = 0;
 		while(i < this.GetParamSize()) {
 			/*local*/GtType ParamType = this.GetParamType(i);
 			if(i > 0) {
 				s += ", ";
 			}
 			s += ParamType.ShortClassName;
 			i += 1;
 		}
 		return s + ": " + this.GetReturnType();
 	}
 
 	public boolean Is(int Flag) {
 		return IsFlag(this.MethodFlag, Flag);
 	}
 
 	public final GtType GetReturnType() {
 		return this.Types[0];
 	}
 
 	public final GtType GetRecvType() {
 		if(this.Types.length == 1){
 			return this.Types[0].Context.VoidType;
 		}
 		return this.Types[1];
 	}
 
 	public final int GetParamSize() {
 		return this.Types.length - 1;
 	}
 
 	public final GtType GetParamType(int ParamIdx) {
 		return this.Types[ParamIdx+1];
 	}
 	
 	
 }
 
 final class GtLayer extends GtStatic {
 	/*public*/public String Name;
 	/*public*/public GtMap MethodTable;
 	GtLayer/*constructor*/(String Name) {
 		this.Name = Name;
 		this.MethodTable = new GtMap();
 	}
 
 	public final GtMethod LookupUniqueMethod(String Name) {
 		return (/*cast*/GtMethod)this.MethodTable.get(Name);
 	}
 
 	public final GtMethod GetMethod(String MethodId) {
 		return (/*cast*/GtMethod)this.MethodTable.get(MethodId);
 	}
 
 	public final void DefineMethod(GtMethod Method) {
 		LangDeps.Assert(Method.Layer == null);
 		/*local*/GtType Class = Method.GetRecvType();
 		/*local*/String MethodId = Class.GetMethodId(Method.MethodName);
 		/*local*/GtMethod MethodPrev = (/*cast*/GtMethod)this.MethodTable.get(MethodId);
 		Method.ElderMethod = MethodPrev;
 		Method.Layer = this;
 		this.MethodTable.put(MethodId, Method);
 		//MethodPrev = this.LookupUniqueMethod(Method.MethodName);
 		//if(MethodPrev != null) {
 		//	TODO("check identity");
 		//	this.MethodTable.remove(Id);
 		//}
 	}
 }
 
 
 final class VariableInfo {
 	/*field*/public GtType	Type;
 	/*field*/public String	Name;
 	/*field*/public String	LocalName;
 
 	VariableInfo/*constructor*/(GtType Type, String Name, int Index) {
 		this.Type = Type;
 		this.Name = Name;
 		this.LocalName = Name + Index;
 	}
 }
 
 final class GtDelegate {
 	/*field*/public GtMethod Method;
 	/*field*/public Object   Callee;
 	/*field*/public GtType   Type;
 	GtDelegate/*constructor*/() {
 	}
 }
 
 final class TypeEnv extends GtStatic {
 	/*field*/public GtNameSpace	      NameSpace;
 	/*field*/public GreenTeaGenerator Generator;
 
 	/*field*/public GtMethod	Method;
 	/*field*/public ArrayList<VariableInfo> LocalStackList;
 	/*field*/public int StackTopIndex;
 
 	/* for convinient short cut */
 	/*field*/public final GtType	VoidType;
 	/*field*/public final GtType	BooleanType;
 	/*field*/public final GtType	IntType;
 	/*field*/public final GtType	StringType;
 	/*field*/public final GtType	VarType;
 	/*field*/public final GtType	AnyType;
 
 	TypeEnv/*constructor*/(GtNameSpace NameSpace) {
 		this.NameSpace = NameSpace;
 		this.Generator = NameSpace.Context.Generator;
 		this.Method = null;
 		this.LocalStackList = new ArrayList<VariableInfo>();
 		this.StackTopIndex = 0;
 
 		this.VoidType = NameSpace.Context.VoidType;
 		this.BooleanType = NameSpace.Context.BooleanType;
 		this.IntType = NameSpace.Context.IntType;
 		this.StringType = NameSpace.Context.StringType;
 		this.VarType = NameSpace.Context.VarType;
 		this.AnyType = NameSpace.Context.AnyType;
 	}
 
 	public void SetMethod(GtMethod Method) {
 		this.Method = Method;
 	}
 
 	public final boolean IsTopLevel() {
 		return (this.Method == null);
 	}
 
 	public boolean AppendDeclaredVariable(GtType Type, String Name) {
 		/*local*/VariableInfo VarInfo = new VariableInfo(Type, Name, this.StackTopIndex);
 		if(this.StackTopIndex < this.LocalStackList.size()) {
 			this.LocalStackList.add(VarInfo);
 		}
 		else {
 			this.LocalStackList.add(VarInfo);
 		}
 		this.StackTopIndex += 1;
 		return true;
 	}
 
 	public VariableInfo LookupDeclaredVariable(String Symbol) {
 		/*local*/int i = this.StackTopIndex - 1;
 		while(i >= 0) {
 			/*local*/VariableInfo VarInfo = this.LocalStackList.get(i);
 			if(VarInfo.Name.equals(Symbol)) {
 				return VarInfo;
 			}
 			i -= 1;
 		}
 		return null;
 	}
 
 	public GtType GuessType (Object Value) {
 		TODO("GuessType");
 		if(Value instanceof Integer) {
 			return this.IntType;
 		}
 		else if(Value instanceof String) {
 			return this.StringType;
 		}
 		return this.AnyType;
 	}
 
 	public GtDelegate LookupDelegate(String Name) {
 		TODO("finding delegate");
 		return new GtDelegate();
 		//return null;
 	}
 
 	public TypedNode DefaultValueConstNode(SyntaxTree ParsedTree, GtType Type) {
 		if(Type.DefaultNullValue != null) {
 			return this.Generator.CreateConstNode(Type, ParsedTree, Type.DefaultNullValue);
 		}
 		return this.CreateErrorNode(ParsedTree, "undefined initial value of " + Type);
 	}
 
 	public TypedNode CreateErrorNode(SyntaxTree ParsedTree, String Message) {
 		this.NameSpace.ReportError(ErrorLevel, ParsedTree.KeyToken, Message);
 		return this.Generator.CreateErrorNode(this.VoidType, ParsedTree);
 	}
 
 	public TypedNode SupportedOnlyTopLevelError(SyntaxTree ParsedTree) {
 		return this.CreateErrorNode(ParsedTree, "supported only at top level " + ParsedTree.Pattern);
 	}
 
 	public TypedNode UnsupportedTopLevelError(SyntaxTree ParsedTree) {
 		return this.CreateErrorNode(ParsedTree, "unsupported at top level " + ParsedTree.Pattern);
 	}
 
 
 	/* typing */
 	public TypedNode TypeEachNode(SyntaxTree Tree, GtType Type) {
 		/*local*/TypedNode Node = GtStatic.ApplyTypeFunc(Tree.Pattern.TypeFunc, this, Tree, Type);
 		if(Node == null) {
 			Node = this.CreateErrorNode(Tree, "undefined type checker: " + Tree.Pattern);
 		}
 		return Node;
 	}
 
 	public TypedNode TypeCheckEachNode(SyntaxTree Tree, GtType Type, int TypeCheckPolicy) {
 		/*local*/TypedNode Node = this.TypeEachNode(Tree, Type);
 		if(Node.IsError()) {
 			return Node;
 		}
 		return Node;
 	}
 
 	public TypedNode TypeCheck(SyntaxTree ParsedTree, GtType Type, int TypeCheckPolicy) {
 		return this.TypeBlock(ParsedTree, Type);
 	}
 
 	public TypedNode TypeBlock(SyntaxTree ParsedTree, GtType Type) {
 		/*local*/int StackTopIndex = this.StackTopIndex;
 		/*local*/TypedNode LastNode = null;
 		while(ParsedTree != null) {
 			/*local*/GtType CurrentType = Type;
 			if(ParsedTree.NextTree != null){
 				CurrentType = this.VoidType;
 			}
 			/*local*/TypedNode TypedNode = this.TypeCheckEachNode(ParsedTree, CurrentType, DefaultTypeCheckPolicy);
 			/*local*/LastNode = GtStatic.LinkNode(LastNode, TypedNode);
 			if(TypedNode.IsError()) {
 				break;
 			}
 			ParsedTree = ParsedTree.NextTree;
 		}
 		this.StackTopIndex = StackTopIndex;
 		if(LastNode == null){
 			return null;
 		}
 		return LastNode.MoveHeadNode();
 	}
 }
 
 // NameSpace
 
 final class GtSpec extends GtStatic {
 	/*field*/public int SpecType;
 	/*field*/public String SpecKey;
 	/*field*/public Object SpecBody;
 
 	GtSpec/*constructor*/(int SpecType, String SpecKey, Object SpecBody) {
 		this.SpecType = SpecType;
 		this.SpecKey  = SpecKey;
 		this.SpecBody = SpecBody;
 	}
 }
 
 final class GtNameSpace extends GtStatic {
 	/*field*/public GtContext		Context;
 	/*field*/public String          PackageName;
 	/*field*/public GtNameSpace		ParentNameSpace;
 	/*field*/public ArrayList<GtNameSpace>	  ImportedNameSpaceList;
 	/*field*/public ArrayList<GtSpec>         PublicSpecList;
 	/*field*/public ArrayList<GtSpec>         PrivateSpecList;
 
 	/*field*/TokenFunc[] TokenMatrix;
 	/*field*/GtMap	 SymbolPatternTable;
 	/*field*/GtMap   ExtendedPatternTable;
 	/*field*/public ArrayList<GtLayer>        LayerList;
 	/*field*/GtLayer TopLevelLayer;
 
 	GtNameSpace/*constructor*/(GtContext Context, GtNameSpace ParentNameSpace) {
 		this.Context = Context;
 		this.ParentNameSpace = ParentNameSpace;
 		this.LayerList = new ArrayList<GtLayer>();
 		if(ParentNameSpace != null) {
 			this.PackageName = ParentNameSpace.PackageName;
 			this.TopLevelLayer = ParentNameSpace.TopLevelLayer;
 		}
 		else {
 			this.TopLevelLayer = Context.UserDefinedLayer;
 		}
 		this.LayerList.add(Context.GreenLayer);
 		this.LayerList.add(this.TopLevelLayer);
 		this.ImportedNameSpaceList = null;
 		this.PublicSpecList = new ArrayList<GtSpec>();
 		this.PrivateSpecList = null;
 		this.TokenMatrix = null;
 		this.SymbolPatternTable = null;
 		this.ExtendedPatternTable = null;
 	}
 
 	private void RemakeTokenMatrixEach(GtNameSpace NameSpace) {
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(NameSpace.PublicSpecList)) {
 			/*local*/GtSpec Spec = NameSpace.PublicSpecList.get(i);
 			if(Spec.SpecType == TokenFuncSpec) {
 				/*local*/int j = 0;
 				while(j < Spec.SpecKey.length()) {
 					/*local*/int kchar = GtStatic.FromJavaChar(LangDeps.CharAt(Spec.SpecKey, j));
 					/*local*/GtDelegateToken Func = (/*cast*/GtDelegateToken)Spec.SpecBody;
 					this.TokenMatrix[kchar] = LangDeps.CreateOrReuseTokenFunc(Func, this.TokenMatrix[kchar]);
 					j += 1;
 				}
 			}
 			i += 1;
 		}
 	}
 
 	private void RemakeTokenMatrix(GtNameSpace NameSpace) {
 		if(NameSpace.ParentNameSpace != null) {
 			this.RemakeTokenMatrix(NameSpace.ParentNameSpace);
 		}
 		this.RemakeTokenMatrixEach(NameSpace);
 		/*local*/int i = 0;
 		while(i < ListSize(NameSpace.ImportedNameSpaceList)) {
 			/*local*/GtNameSpace Imported = NameSpace.ImportedNameSpaceList.get(i);
 			this.RemakeTokenMatrixEach(Imported);
 			i += 1;
 		}
 	}
 
 	public TokenFunc GetTokenFunc(int GtChar2) {
 		if(this.TokenMatrix == null) {
 			this.TokenMatrix = new TokenFunc[MaxSizeOfChars];
 			this.RemakeTokenMatrix(this);
 		}
 		return this.TokenMatrix[GtChar2];
 	}
 
 	public void DefineTokenFunc(String keys, GtDelegateToken f) {
 		this.PublicSpecList.add(new GtSpec(TokenFuncSpec, keys, f));
 		this.TokenMatrix = null;
 	}
 
 	private void TableAddSpec(GtMap Table, GtSpec Spec) {
 		/*local*/Object Body = Spec.SpecBody;
 		if(Body instanceof SyntaxPattern) {
 			/*local*/Object Parent = Table.get(Spec.SpecKey);
 			if(Parent instanceof SyntaxPattern) {
 				Body = GtStatic.MergeSyntaxPattern((/*cast*/SyntaxPattern)Body, (/*cast*/SyntaxPattern)Parent);
 			}
 		}
 		Table.put(Spec.SpecKey, Body);
 	}
 
 	private void RemakeSymbolTableEach(GtNameSpace NameSpace, ArrayList<GtSpec> SpecList) {
 		/*local*/int i = 0;
 		while(i < ListSize(SpecList)) {
 			/*local*/GtSpec Spec = SpecList.get(i);
 			if(Spec.SpecType == SymbolPatternSpec) {
 				this.TableAddSpec(this.SymbolPatternTable, Spec);
 			}
 			else if(Spec.SpecType == ExtendedPatternSpec) {
 				this.TableAddSpec(this.ExtendedPatternTable, Spec);
 			}
 			i += 1;
 		}
 	}
 
 	private void RemakeSymbolTable(GtNameSpace NameSpace) {
 		if(NameSpace.ParentNameSpace != null) {
 			this.RemakeSymbolTable(NameSpace.ParentNameSpace);
 		}
 		this.RemakeSymbolTableEach(NameSpace, NameSpace.PublicSpecList);
 		this.RemakeSymbolTableEach(NameSpace, NameSpace.PrivateSpecList);
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(NameSpace.ImportedNameSpaceList)) {
 			/*local*/GtNameSpace Imported = NameSpace.ImportedNameSpaceList.get(i);
 			this.RemakeSymbolTableEach(Imported, Imported.PublicSpecList);
 			i += 1;
 		}
 	}
 
 	public Object GetSymbol(String Key) {
 		if(this.SymbolPatternTable == null) {
 			this.SymbolPatternTable = new GtMap();
 			this.ExtendedPatternTable = new GtMap();
 			this.RemakeSymbolTable(this);
 		}
 		return this.SymbolPatternTable.get(Key);
 	}
 
 	public SyntaxPattern GetPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol(PatternName);
 		if(Body instanceof SyntaxPattern){
 			return (/*cast*/SyntaxPattern)Body;
 		}
 		return null;
 	}
 
 	public SyntaxPattern GetExtendedPattern(String PatternName) {
 		if(this.ExtendedPatternTable == null) {
 			this.SymbolPatternTable = new GtMap();
 			this.ExtendedPatternTable = new GtMap();
 			this.RemakeSymbolTable(this);
 		}
 		/*local*/Object Body = this.ExtendedPatternTable.get(PatternName);
 		if(Body instanceof SyntaxPattern){
 			return (/*cast*/SyntaxPattern)Body;
 		}
 		return null;
 	}
 
 	public void DefineSymbol(String Key, Object Value) {
 		/*local*/GtSpec Spec = new GtSpec(SymbolPatternSpec, Key, Value);
 		this.PublicSpecList.add(Spec);
 		if(this.SymbolPatternTable != null) {
 			this.TableAddSpec(this.SymbolPatternTable, Spec);
 		}
 	}
 
 	public void DefinePrivateSymbol(String Key, Object Value) {
 		/*local*/GtSpec Spec = new GtSpec(SymbolPatternSpec, Key, Value);
 		if(this.PrivateSpecList == null) {
 			this.PrivateSpecList = new ArrayList<GtSpec>();
 		}
 		this.PrivateSpecList.add(Spec);
 		if(this.SymbolPatternTable != null) {
 			this.TableAddSpec(this.SymbolPatternTable, Spec);
 		}
 	}
 
 	public void DefineSyntaxPattern(String PatternName, GtDelegateMatch MatchFunc, GtDelegateType TypeFunc) {
 		/*local*/SyntaxPattern Pattern = new SyntaxPattern(this, PatternName, MatchFunc, TypeFunc);
 		/*local*/GtSpec Spec = new GtSpec(SymbolPatternSpec, PatternName, Pattern);
 		this.PublicSpecList.add(Spec);
 		if(this.SymbolPatternTable != null) {
 			this.TableAddSpec(this.SymbolPatternTable, Spec);
 		}
 	}
 
 	public void DefineExtendedPattern(String PatternName, int SyntaxFlag, GtDelegateMatch MatchFunc, GtDelegateType TypeFunc) {
 		/*local*/SyntaxPattern Pattern = new SyntaxPattern(this, PatternName, MatchFunc, TypeFunc);
 		Pattern.SyntaxFlag = SyntaxFlag;
 		/*local*/GtSpec Spec = new GtSpec(ExtendedPatternSpec, PatternName, Pattern);
 		this.PublicSpecList.add(Spec);
 		if(this.ExtendedPatternTable != null) {
 			this.TableAddSpec(this.ExtendedPatternTable, Spec);
 		}
 	}
 
 	public GtType DefineClass(GtType ClassInfo) {
 		if(ClassInfo.PackageNameSpace == null) {
 			ClassInfo.PackageNameSpace = this;
 			if(this.PackageName != null) {
 				this.Context.ClassNameMap.put(this.PackageName + "." + ClassInfo.ShortClassName, ClassInfo);
 			}
 			this.Context.Generator.AddClass(ClassInfo);
 		}
 		this.DefineSymbol(ClassInfo.ShortClassName, ClassInfo);
 		return ClassInfo;
 	}
 
 	public GtMethod DefineMethod(GtMethod Method) {
 		this.TopLevelLayer.DefineMethod(Method);
 		return Method;
 	}
 
 	private GtMethod FilterOverloadedMethods(GtMethod Method, int ParamSize, int ResolvedSize, ArrayList<GtType> TypeList, int BaseIndex, GtMethod FoundMethod) {
 		while(Method != null) {
 			if(Method.GetParamSize() == ParamSize) {
 				/*local*/int i = 1;  // because the first type is mached by given class
 				/*local*/GtMethod MatchedMethod = Method;
 				while(i < ResolvedSize) {
 					if(!Method.GetParamType(i).Accept(TypeList.get(BaseIndex + i))) {
 						MatchedMethod = null;
 						break;
 					}
 					i += 1;
 				}
 				if(MatchedMethod != null) {
 					if(FoundMethod != null) {
 						return null; /* found overloaded methods*/
 					}
 					FoundMethod = MatchedMethod;
 				}
 			}
 			Method = Method.ElderMethod;
 		}
 		return FoundMethod;
 	}
 
 	public GtMethod LookupMethod(String MethodName, int ParamSize, int ResolvedSize, ArrayList<GtType> TypeList, int BaseIndex) {
 		/*local*/int i = this.LayerList.size() - 1;
 		/*local*/GtMethod FoundMethod = null;
 		if(ResolvedSize > 0) {
 			/*local*/GtType Class = TypeList.get(BaseIndex + 0);
 			while(FoundMethod == null && Class != null) {
 				/*local*/String MethodId = Class.GetMethodId(MethodName);
 				while(i >= 0) {
 					/*local*/GtLayer Layer = this.LayerList.get(i);
 					/*local*/GtMethod Method = Layer.GetMethod(MethodId);
 					FoundMethod = FilterOverloadedMethods(Method, ParamSize, ResolvedSize, TypeList, BaseIndex, FoundMethod);
 					i -= 1;
 				}
 				Class = Class.SearchSuperMethodClass;
 			}
 		}
 		return FoundMethod;
 	}
 
 	public GtMethod GetGetter(GtType Class, String FieldName) {
 		/*local*/String MethodId = Class.GetMethodId(FieldName);
 		while(Class != null) {
 			/*local*/GtMethod FoundMethod = this.Context.FieldLayer.GetMethod(MethodId);
 			if(FoundMethod != null) {
 				return FoundMethod;
 			}
 			Class = Class.SearchSuperMethodClass;
 		}
 		return null;
 	}
 
 
 
 	// Global Object
 	public GtObject CreateGlobalObject(int ClassFlag, String ShortName) {
 		/*local*/GtType NewClass = new GtType(this.Context, ClassFlag, ShortName, null);
 		/*local*/GtObject GlobalObject = new GtObject(NewClass);
 		NewClass.DefaultNullValue = GlobalObject;
 		return GlobalObject;
 	}
 
 	public GtObject GetGlobalObject() {
 		/*local*/Object GlobalObject = this.GetSymbol(GlobalConstName);
 		if(GlobalObject == null || !(GlobalObject instanceof GtObject)) {
 			GlobalObject = this.CreateGlobalObject(SingletonClass, "global");
 			this.DefinePrivateSymbol(GlobalConstName, GlobalObject);
 		}
 		return (/*cast*/GtObject) GlobalObject;
 	}
 
 	public void ImportNameSpace(GtNameSpace ImportedNameSpace) {
 		if(this.ImportedNameSpaceList == null) {
 			this.ImportedNameSpaceList = new ArrayList<GtNameSpace>();
 			this.ImportedNameSpaceList.add(ImportedNameSpace);
 		}
 		this.TokenMatrix = null;
 		this.SymbolPatternTable = null;
 		this.ExtendedPatternTable = null;
 	}
 
 	public Object Eval(String ScriptSource, long FileLine, GreenTeaGenerator Generator) {
 		/*local*/Object ResultValue = null;
 		DebugP("Eval: " + ScriptSource);
 		/*local*/TokenContext TokenContext = new TokenContext(this, ScriptSource, FileLine);
 		TokenContext.SkipEmptyStatement();
 		while(TokenContext.HasNext()) {
 			/*local*/SyntaxTree Tree = GtStatic.ParseExpression(TokenContext);
 			DebugP("untyped tree: " + Tree);
 			/*local*/TypeEnv Gamma = new TypeEnv(this);
 			/*local*/TypedNode Node = Gamma.TypeCheckEachNode(Tree, Gamma.VoidType, DefaultTypeCheckPolicy);
 			ResultValue = Generator.Eval(Node);
 			TokenContext.SkipEmptyStatement();
 			TokenContext.Vacume();
 		}
 		return ResultValue;
 	}
 
 	public GtMethod LookupMethod(String MethodName, int ParamSize) {
 		//FIXME
 		//MethodName = "ClassName.MethodName";
 		//1. (ClassName, MethodName) = MethodName.split(".")
 		//2. find MethodName(arg0, arg1, ... , arg_ParamSize)
 		return null;
 	}
 
 	private String GetSourcePosition(long FileLine) {
 		return "(eval:" + (int) FileLine + ")";
 	}
 
 	public String ReportError(int Level, GtToken Token, String Message) {
 		if(!Token.IsError()) {
 			if(Level == ErrorLevel) {
 				Message = "(error) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 				Token.ToErrorToken(Message);
 			}
 			else if(Level == WarningLevel) {
 				Message = "(warning) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			}
 			else if(Level == InfoLevel) {
 				Message = "(info) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			}
 			GtStatic.println(Message);
 			return Message;
 		}
 		return Token.GetErrorMessage();
 	}
 
 }
 
 class GtGrammar extends GtStatic {
 	public void LoadTo(GtNameSpace NameSpace) {
 		/*extension*/
 	}
 }
 
 final class KonohaGrammar extends GtGrammar {
 
 	// Token
 	public static int WhiteSpaceToken(TokenContext TokenContext, String SourceText, int pos) {
 		TokenContext.FoundWhiteSpace();
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, pos);
 			if(!LangDeps.IsWhitespace(ch)) {
 				break;
 			}
 			pos += 1;
 		}
 		return pos;
 	}
 
 	public static int IndentToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int LineStart = pos + 1;
 		TokenContext.FoundLineFeed(1);
 		pos = pos + 1;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, pos);
 			if(!LangDeps.IsWhitespace(ch)) {
 				break;
 			}
 			pos += 1;
 		}
 		/*local*/String Text = "";
 		if(LineStart < pos) {
 			Text = SourceText.substring(LineStart, pos);
 		}
 		TokenContext.AddNewToken(Text, IndentTokenFlag, null);
 		return pos;
 		//TokenContext.AddNewToken(SourceText.substring(pos), SourceTokenFlag, null);
 		//return SourceText.length();
 	}
 
 	public static int SymbolToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, pos);
 			if(!LangDeps.IsLetter(ch) && !LangDeps.IsDigit(ch) && ch != '_') {
 				break;
 			}
 			pos += 1;
 		}
 		TokenContext.AddNewToken(SourceText.substring(start, pos), 0, null);
 		return pos;
 	}
 
 	public static int OperatorToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int NextPos = pos + 1;
 		while(NextPos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, NextPos);
 			if(LangDeps.IsWhitespace(ch) || LangDeps.IsLetter(ch) || LangDeps.IsDigit(ch)) {
 				break;
 			}
 			NextPos += 1;
 		}
 
 		/*local*/boolean Matched = false;
 		while(NextPos > pos) {
 			/*local*/String Sub = SourceText.substring(pos, NextPos);
 			/*local*/SyntaxPattern Pattern = TokenContext.NameSpace.GetExtendedPattern(Sub);
 			if(Pattern != null) {
 				Matched = true;
 				break;
 			}
 			NextPos -= 1;
 		}
 		// FIXME
 		if(Matched == false) {
 			NextPos = pos + 1;
 		}
 		TokenContext.AddNewToken(SourceText.substring(pos, NextPos), 0, null);
 		return NextPos;
 	}
 
 	public static int CommentToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int NextPos = pos + 1;
 		if(pos + 1 < SourceText.length()) {
 			/*local*/char NextChar = LangDeps.CharAt(SourceText, pos+1);
 			if(NextChar == '/') {
 				NextPos = NextPos + 1;
 				while(NextPos < SourceText.length()) {
 					/*local*/char ch = LangDeps.CharAt(SourceText, NextPos);
 					if(ch == '\n') {
 						return IndentToken(TokenContext, SourceText, NextPos);
 					}
					NextPos = NextPos + 1;
 				}
 			}
 		}
 		return NoMatch;
 	}
 
 	public static int NumberLiteralToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, pos);
 			if(!LangDeps.IsDigit(ch)) {
 				break;
 			}
 			pos += 1;
 		}
 		TokenContext.AddNewToken(SourceText.substring(start, pos), 0, "$IntegerLiteral$");
 		return pos;
 	}
 
 	public static int StringLiteralToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		/*local*/char prev = '"';
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, pos);
 			if(ch == '"' && prev != '\\') {
 				TokenContext.AddNewToken(SourceText.substring(start, pos+1), 0, "$StringLiteral$");
 				return pos + 1;
 			}
 			if(ch == '\n') {
 				TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", SourceText.substring(start, pos));
 				TokenContext.FoundLineFeed(1);
 				return pos;
 			}
 			pos = pos + 1;
 			prev = ch;
 		}
 		TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", SourceText.substring(start, pos));
 		return pos;
 	}
 
 	public static SyntaxTree ParseType(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/Object ConstValue = TokenContext.NameSpace.GetSymbol(Token.ParsedText);
 		if(ConstValue instanceof GtType) {
 			return new SyntaxTree(Pattern, TokenContext.NameSpace, Token, ConstValue);
 		}
 		return null; // Not Matched
 	}
 
 	public static SyntaxTree ParseConst(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/Object ConstValue = TokenContext.NameSpace.GetSymbol(Token.ParsedText);
 		if(ConstValue != null) {
 			return new SyntaxTree(Pattern, TokenContext.NameSpace, Token, ConstValue);
 		}
 		return null; // Not Matched
 	}
 
 	public static TypedNode TypeConst(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateConstNode(Gamma.GuessType(ParsedTree.ConstValue), ParsedTree, ParsedTree.ConstValue);
 	}
 
 	public static SyntaxTree ParseSymbol(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/SyntaxTree TypeTree = TokenContext.ParsePattern("$Type$", Optional);
 		if(TypeTree != null) {
 			/*local*/SyntaxTree DeclTree = TokenContext.ParsePatternAfter(TypeTree, "$FuncDecl$", Optional);
 			if(DeclTree != null) {
 				return DeclTree;
 			}
 			DeclTree = TokenContext.ParsePatternAfter(TypeTree, "$VarDecl$", Optional);
 			if(DeclTree != null) {
 				return DeclTree;
 			}
 			return TypeTree;
 		}
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtNameSpace NameSpace = TokenContext.NameSpace;
 		/*local*/Object ConstValue = NameSpace.GetSymbol(Token.ParsedText);
 		if(ConstValue != null && !(ConstValue instanceof GtType)) {
 			return new SyntaxTree(NameSpace.GetPattern("$Const$"), NameSpace, Token, ConstValue);
 		}
 		return new SyntaxTree(NameSpace.GetPattern("$Variable$"), NameSpace, Token, null);
 	}
 
 	public static SyntaxTree ParseVariable(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/char ch = LangDeps.CharAt(Token.ParsedText, 0);
 		if(LangDeps.IsLetter(ch) || ch == '_') {
 			return new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		}
 		return null;
 	}
 
 	public static TypedNode TypeVariable(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/VariableInfo VariableInfo = Gamma.LookupDeclaredVariable(Name);
 		if(VariableInfo != null) {
 			return Gamma.Generator.CreateLocalNode(VariableInfo.Type, ParsedTree, VariableInfo.LocalName);
 		}
 		/*local*/GtDelegate Delegate = Gamma.LookupDelegate(Name);
 		if(Delegate != null) {
 			return Gamma.Generator.CreateConstNode(Delegate.Type, ParsedTree, Delegate);
 		}
 		return Gamma.CreateErrorNode(ParsedTree, "undefined name: " + Name);
 	}
 
 	public static SyntaxTree ParseVarDecl(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/SyntaxTree Tree = new SyntaxTree(Pattern, TokenContext.NameSpace, TokenContext.GetToken(), null);
 		if(LeftTree == null) {
 			Tree.SetMatchedPatternAt(VarDeclType, TokenContext, "$Type$", Required);
 		}
 		else {
 			Tree.SetSyntaxTreeAt(VarDeclType, LeftTree);
 		}
 		Tree.SetMatchedPatternAt(VarDeclName, TokenContext, "$Variable$", Required);
 		if(TokenContext.MatchToken("=")) {
 			Tree.SetMatchedPatternAt(VarDeclValue, TokenContext, "$Expression$", Required);
 		}
 		while(TokenContext.MatchToken(",")) {
 			/*local*/SyntaxTree NextTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Tree.KeyToken, null);
 			NextTree.SetSyntaxTreeAt(VarDeclType, Tree.GetSyntaxTreeAt(VarDeclType));
 			Tree = GtStatic.LinkTree(Tree, NextTree);
 			Tree.SetMatchedPatternAt(VarDeclName, TokenContext, "$Variable$", Required);
 			if(TokenContext.MatchToken("=")) {
 				Tree.SetMatchedPatternAt(VarDeclValue, TokenContext, "$Expression$", Required);
 			}
 		}
 		return Tree;
 	}
 
 	public static TypedNode TypeVarDecl(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/SyntaxTree TypeTree = ParsedTree.GetSyntaxTreeAt(VarDeclType);
 		/*local*/SyntaxTree NameTree = ParsedTree.GetSyntaxTreeAt(VarDeclName);
 		/*local*/SyntaxTree ValueTree = ParsedTree.GetSyntaxTreeAt(VarDeclValue);
 		/*local*/GtType DeclType = (/*cast*/GtType)TypeTree.ConstValue;
 		/*local*/String VariableName = NameTree.KeyToken.ParsedText;
 		if(!Gamma.AppendDeclaredVariable(DeclType, VariableName)) {
 			Gamma.CreateErrorNode(TypeTree, "already defined variable " + VariableName);
 		}
 		/*local*/TypedNode VariableNode = Gamma.TypeCheck(NameTree, DeclType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode InitValueNode = null;
 		if(ValueTree == null){
 			InitValueNode = Gamma.DefaultValueConstNode(ParsedTree, DeclType);
 		}else{
 			InitValueNode = Gamma.TypeCheck(ValueTree, DeclType, DefaultTypeCheckPolicy);
 		}
 		/*local*/TypedNode AssignNode = Gamma.Generator.CreateAssignNode(DeclType, ParsedTree, VariableNode, InitValueNode);
 		/*local*/TypedNode BlockNode = Gamma.TypeBlock(ParsedTree.NextTree, Type);
 		ParsedTree.NextTree = null;
 		if(BlockNode != null) {
 			GtStatic.LinkNode(AssignNode, BlockNode);
 		}
 		return Gamma.Generator.CreateLetNode(DeclType, ParsedTree, DeclType, VariableNode, AssignNode/*connected block*/);
 	}
 
 	// Parse And Type
 	public static SyntaxTree ParseIntegerLiteral(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		return new SyntaxTree(Pattern, TokenContext.NameSpace, Token, LangDeps.ParseInt(Token.ParsedText));
 	}
 
 	public static SyntaxTree ParseStringLiteral(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next(); /* this must be \" and we should eat it*/
 		/*local*/Token = TokenContext.Next();
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, Token.ParsedText);
 		if(!TokenContext.MatchToken("\"")) {
 			return TokenContext.NewErrorSyntaxTree(Token, "String must close with \"");
 		}
 		return NewTree;
 	}
 
 	public static SyntaxTree ParseExpression(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		return GtStatic.ParseExpression(TokenContext);
 	}
 
 	public static SyntaxTree ParseUnary(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/SyntaxTree Tree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		Tree.SetMatchedPatternAt(UnaryTerm, TokenContext, "$Expression$", Required);
 		return Tree;
 	}
 
 	public static TypedNode TypeUnary(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode ExprNode  = ParsedTree.TypeNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateUnaryNode(Gamma.AnyType, ParsedTree, null/*Method*/, ExprNode);
 	}
 
 	public static SyntaxTree ParseBinary(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/SyntaxTree RightTree = GtStatic.ParseExpression(TokenContext);
 		if(GtStatic.IsEmptyOrError(RightTree)) return RightTree;
 		/* 1 + 2 * 3 */
 		/* 1 * 2 + 3 */
 		if(RightTree.Pattern.IsBinaryOperator()) {
 			if(Pattern.IsLeftJoin(RightTree.Pattern)) {
 				/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 				NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 				NewTree.SetSyntaxTreeAt(RightHandTerm, RightTree.GetSyntaxTreeAt(LeftHandTerm));
 				RightTree.SetSyntaxTreeAt(LeftHandTerm, NewTree);
 				return RightTree;
 			}
 		}
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 		NewTree.SetSyntaxTreeAt(RightHandTerm, RightTree);
 		if(RightTree.NextTree != null) {
 			GtStatic.LinkTree(NewTree, RightTree.NextTree);
 			RightTree.NextTree = null;
 		}
 		return NewTree;
 	}
 
 	public static TypedNode TypeBinary(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode LeftNode  = ParsedTree.TypeNodeAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode RightNode = ParsedTree.TypeNodeAt(RightHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/String Operator = ParsedTree.KeyToken.ParsedText;
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		TypeList.add(LeftNode.Type);
 		TypeList.add(RightNode.Type);
 		/*local*/GtType ReturnType = Gamma.VarType; // FIXME Method.GetReturnType();
 		/*local*/GtMethod Method = Gamma.NameSpace.LookupMethod(Operator, 2, 1/*FIXME*/, TypeList, 0);
 		return Gamma.Generator.CreateBinaryNode(ReturnType, ParsedTree, Method, LeftNode, RightNode);
 	}
 
 	public static SyntaxTree ParseField(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		TokenContext.MatchToken(".");
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.AppendParsedTree(LeftTree);
 		return NewTree;
 	}
 
 	public static TypedNode TypeField(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode ExprNode = ParsedTree.TypeNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/GtMethod Method = null; //ExprNode.Type.GetGetter(ParsedTree.KeyToken.ParsedText);
 		return Gamma.Generator.CreateGetterNode(Method.GetReturnType(), ParsedTree, Method, ExprNode);
 	}
 
 	// PatternName: "("
 	public static SyntaxTree ParseParenthesis(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.MatchToken("(");
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/SyntaxTree Tree = TokenContext.ParsePattern("$Expression$", Required);
 		if(!TokenContext.MatchToken(")")) {
 			Tree = TokenContext.ReportExpectedToken(")");
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return Tree;
 	}
 
 	public static SyntaxTree ParseApply(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/SyntaxTree FuncTree = new SyntaxTree(Pattern, TokenContext.NameSpace, TokenContext.GetMatchedToken("("), null);
 		FuncTree.AppendParsedTree(LeftTree);
 		while(!FuncTree.IsEmptyOrError()) {
 			/*local*/SyntaxTree Tree = TokenContext.ParsePattern("$Expression$", Required);
 			FuncTree.AppendParsedTree(Tree);
 			if(TokenContext.MatchToken(",")) continue;
 			/*local*/SyntaxTree EndTree = new SyntaxTree(Pattern, TokenContext.NameSpace, TokenContext.GetMatchedToken(")"), null);
 			if(EndTree != null) {
 				FuncTree.AppendParsedTree(EndTree);
 				break;
 			}
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return FuncTree;
 	}
 
 	public static TypedNode TypeApply(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode ApplyNode = Gamma.Generator.CreateApplyNode(Gamma.AnyType, ParsedTree, null);
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		/*FIXME It should be the return type of the function*/
 		TypeList.add(Gamma.NameSpace.Context.IntType);
 		/*local*/int i = 1;
 		while(i < ListSize(ParsedTree.TreeList) - 1/* this is for ")" */) {
 			/*local*/TypedNode ExprNode = ParsedTree.TypeNodeAt(i, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 			ApplyNode.Append(ExprNode);
 			TypeList.add(ExprNode.Type);
 			i += 1;
 		}
 
 		///*local*/GtMethod Method = Gamma.NameSpace.LookupMethod(MethodName, ParamSize, Type, TypeList, BaseIndex)
 		/*local*/ArrayList<SyntaxTree> TreeList = ParsedTree.TreeList;
 		/*local*/String MethodName = TreeList.get(0/*todo*/).KeyToken.ParsedText;
 		/*local*/int ParamSize = TreeList.size() - 2; /*MethodName and ")" symol*/
 		/*local*/GtMethod Method = Gamma.NameSpace.LookupMethod(MethodName, ParamSize, 1/*FIXME*/, TypeList, 0);
 		((/*cast*/ApplyNode)ApplyNode).Method = Method;
 		return ApplyNode;
 	}
 
 	public static TypedNode TypeAnd(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode LeftNode = ParsedTree.TypeNodeAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode RightNode = ParsedTree.TypeNodeAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateAndNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static TypedNode TypeOr(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode LeftNode = ParsedTree.TypeNodeAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode RightNode = ParsedTree.TypeNodeAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateOrNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static TypedNode TypeAssign(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode LeftNode = ParsedTree.TypeNodeAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode RightNode = ParsedTree.TypeNodeAt(RightHandTerm, Gamma, LeftNode.Type, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateAssignNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static SyntaxTree ParseEmpty(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		return new SyntaxTree(Pattern, TokenContext.NameSpace, NullToken, null);
 	}
 
 	public static TypedNode TypeEmpty(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateNullNode(Gamma.VoidType, ParsedTree);
 	}
 
 	public static SyntaxTree ParseBlock(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		if(TokenContext.MatchToken("{")) {
 			/*local*/SyntaxTree PrevTree = null;
 			while(TokenContext.SkipEmptyStatement()) {
 				if(TokenContext.MatchToken("}")) break;
 				/*local*/GtMap Annotation = TokenContext.SkipAnnotation();
 				/*local*/SyntaxTree CurrentTree = GtStatic.ParseExpression(TokenContext);
 				if(GtStatic.IsEmptyOrError(CurrentTree)) {
 					return CurrentTree;
 				}
 				CurrentTree.SetAnnotation(Annotation);
 				PrevTree = GtStatic.LinkTree(PrevTree, CurrentTree);
 			}
 			if(PrevTree == null) {
 				return TokenContext.ParsePattern("$Empty$", Required);
 			}
 			return GtStatic.TreeHead(PrevTree);
 		}
 		return null;
 	}
 
 	public static SyntaxTree ParseStatement(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/SyntaxTree StmtTree = TokenContext.ParsePattern("$Block$", Optional);
 		if(StmtTree == null) {
 			StmtTree = GtStatic.ParseExpression(TokenContext);
 		}
 		if(StmtTree == null) {
 			StmtTree = TokenContext.ParsePattern("$Empty$", Required);
 		}
 		return StmtTree;
 	}
 
 	public static TypedNode TypeBlock(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		return Gamma.TypeBlock(ParsedTree, Type);
 	}
 
 	// If Statement
 	public static SyntaxTree ParseIf(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("if");
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.SetMatchedTokenAt(NoWhere, TokenContext, "(", Required);
 		NewTree.SetMatchedPatternAt(IfCond, TokenContext, "$Expression$", Required);
 		NewTree.SetMatchedTokenAt(NoWhere, TokenContext, ")", Required);
 		NewTree.SetMatchedPatternAt(IfThen, TokenContext, "$Statement$", Required);
 		if(TokenContext.MatchToken("else")) {
 			NewTree.SetMatchedPatternAt(IfElse, TokenContext, "$Statement$", Required);
 		}
 		return NewTree;
 	}
 
 	public static TypedNode TypeIf(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode CondNode = ParsedTree.TypeNodeAt(IfCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode ThenNode = ParsedTree.TypeNodeAt(IfThen, Gamma, Type, DefaultTypeCheckPolicy);
 		/*local*/TypedNode ElseNode = ParsedTree.TypeNodeAt(IfElse, Gamma, ThenNode.Type, AllowEmptyPolicy);
 		return Gamma.Generator.CreateIfNode(ThenNode.Type, ParsedTree, CondNode, ThenNode, ElseNode);
 	}
 
 	// While Statement
 	public static SyntaxTree ParseWhile(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("while");
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.SetMatchedTokenAt(NoWhere, TokenContext, "(", Required);
 		NewTree.SetMatchedPatternAt(WhileCond, TokenContext, "$Expression$", Required);
 		NewTree.SetMatchedTokenAt(NoWhere, TokenContext, ")", Required);
 		NewTree.SetMatchedPatternAt(WhileBody, TokenContext, "$Block$", Required);
 		return NewTree;
 	}
 
 	public static TypedNode TypeWhile(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode CondNode = ParsedTree.TypeNodeAt(WhileCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode BodyNode = ParsedTree.TypeNodeAt(WhileBody, Gamma, Type, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateWhileNode(BodyNode.Type, ParsedTree, CondNode, BodyNode);
 	}
 	// Break/Continue Statement
 	public static SyntaxTree ParseBreak(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("break");
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		//FIXME support break with label (e.g. break LABEL; )
 		return NewTree;
 	}
 
 	public static TypedNode TypeBreak(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateBreakNode(Gamma.VoidType, ParsedTree, null, "break");
 	}
 	public static SyntaxTree ParseContinue(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("continue");
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		//FIXME support continue with label (e.g. continue LABEL; )
 		return NewTree;
 	}
 
 	public static TypedNode TypeContinue(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateContinueNode(Gamma.VoidType, ParsedTree, null, "continue");
 	}
 	// Return Statement
 	public static SyntaxTree ParseReturn(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("return");
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.SetMatchedPatternAt(ReturnExpr, TokenContext, "$Expression$", Optional);
 		return NewTree;
 	}
 
 	public static TypedNode TypeReturn(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		if(Gamma.IsTopLevel()) {
 			return Gamma.UnsupportedTopLevelError(ParsedTree);
 		}
 		/*local*/GtType ReturnType = Gamma.Method.GetReturnType();
 		/*local*/TypedNode Expr = ParsedTree.TypeNodeAt(ReturnExpr, Gamma, ReturnType, DefaultTypeCheckPolicy);
 		if(ReturnType == Gamma.VoidType){
 			return Gamma.Generator.CreateReturnNode(Expr.Type, ParsedTree, null);
 		}
 		return Gamma.Generator.CreateReturnNode(Expr.Type, ParsedTree, Expr);
 	}
 
 	// New Expression
 	public static SyntaxTree ParseNew(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("new");
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.SetMatchedPatternAt(CallExpressionOffset, TokenContext, "$Type$", Required);
 		return NewTree;
 	}
 
 	public static TypedNode TypeNew(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode SelfNode = ParsedTree.TypeNodeAt(CallExpressionOffset, Gamma, Gamma.AnyType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode ApplyNode = Gamma.Generator.CreateApplyNode(Gamma.AnyType, ParsedTree, null);
 		/*local*/int i = 0;
 		SelfNode = Gamma.Generator.CreateNewNode(SelfNode.Type, ParsedTree);
 		ApplyNode.Append(SelfNode);
 		/* copied from TypeApply */
 		while(i < ListSize(ParsedTree.TreeList)) {
 			/*local*/TypedNode ExprNode = ParsedTree.TypeNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 			ApplyNode.Append(ExprNode);
 			i += 1;
 		}
 		return ApplyNode;
 	}
 
 	// FuncName
 	public static SyntaxTree ParseFuncName(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		if(Token != NullToken) {
 			return new SyntaxTree(Pattern, TokenContext.NameSpace, Token, Token.ParsedText);
 		}
 		return null;
 	}
 
 	// FuncDecl
 	public static SyntaxTree ParseFuncDecl(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/SyntaxTree Tree = new SyntaxTree(Pattern, TokenContext.NameSpace, TokenContext.GetToken(), null);
 		if(LeftTree == null) {
 			Tree.SetMatchedPatternAt(FuncDeclReturnType, TokenContext, "$Type$", Required);
 		}
 		else {
 			Tree.SetSyntaxTreeAt(FuncDeclReturnType, LeftTree);
 		}
 		//Tree.SetMatchedPatternAt(FuncDeclClass, TokenContext, "$MethodClass$", Optional);
 		//Tree.SetMatchedTokenAt(NoWhere, TokenContext, ".", Optional);
 		Tree.SetMatchedPatternAt(FuncDeclName, TokenContext, "$FuncName$", Required);
 		if(TokenContext.MatchToken("(")) {
 			/*local*/int ParseFlag = TokenContext.SetTrackback(false);  // disabled
 			/*local*/int ParamBase = FuncDeclParam;
 			while(!Tree.IsEmptyOrError() && !TokenContext.MatchToken(")")) {
 				if(ParamBase != FuncDeclParam) {
 					Tree.SetMatchedTokenAt(NoWhere, TokenContext, ",", Required);
 				}
 				Tree.SetMatchedPatternAt(ParamBase + VarDeclType, TokenContext, "$Type$", Required);
 				Tree.SetMatchedPatternAt(ParamBase + VarDeclName, TokenContext, "$Variable$", Required);
 				if(TokenContext.MatchToken("=")) {
 					Tree.SetMatchedPatternAt(ParamBase + VarDeclValue, TokenContext, "$Expression$", Required);
 				}
 				ParamBase += 3;
 			}
 			Tree.SetMatchedPatternAt(FuncDeclBlock, TokenContext, "$Block$", Optional);
 			TokenContext.ParseFlag = ParseFlag;
 			return Tree;
 		}
 		return null;
 	}
 
 	public static TypedNode TypeFuncDecl(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		Gamma = new TypeEnv(ParsedTree.NameSpace);  // creation of new type environment
 		/*local*/String MethodName = (/*cast*/String)ParsedTree.GetSyntaxTreeAt(FuncDeclName).ConstValue;
 		/*local*/ArrayList<GtType> TypeBuffer = new ArrayList<GtType>();
 		/*local*/GtType ReturnType = (/*cast*/GtType)ParsedTree.GetSyntaxTreeAt(FuncDeclReturnType).ConstValue;
 		TypeBuffer.add(ReturnType);
 		/*local*/ArrayList<String> ParamNameList = new ArrayList<String>();
 		/*local*/int ParamBase = FuncDeclParam;
 		/*local*/int i = 0;
 		while(ParamBase < ParsedTree.TreeList.size()) {
 			/*local*/GtType ParamType = (/*cast*/GtType)ParsedTree.GetSyntaxTreeAt(ParamBase).ConstValue;
 			/*local*/String ParamName = ParsedTree.GetSyntaxTreeAt(ParamBase+1).KeyToken.ParsedText;
 			TypeBuffer.add(ParamType);
 			ParamNameList.add(ParamName + i);
 			Gamma.AppendDeclaredVariable(ParamType, ParamName);
 			ParamBase += 3;
 			i = i + 1;
 		}
 		/*local*/GtMethod Method = new GtMethod(0, MethodName, TypeBuffer);
 		Gamma.Method = Method;
 		Gamma.NameSpace.DefineMethod(Method);
 		/*local*/TypedNode BodyNode = ParsedTree.TypeNodeAt(FuncDeclBlock, Gamma, ReturnType, IgnoreEmptyPolicy);
 		if(BodyNode != null) {
 			Gamma.Generator.DefineFunction(Method, ParamNameList, BodyNode);
 		}
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType, ParsedTree);
 	}
 
 	public static SyntaxTree ParseClassDecl(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/SyntaxTree Tree = new SyntaxTree(Pattern, TokenContext.NameSpace, TokenContext.GetToken(), null);
 		//		Tree.SetMatchedPatternAt(FuncDeclClass, TokenContext, "$MethodClass$", Optional);
 		//		Tree.SetMatchedTokenAt(NoWhere, TokenContext, ".", Optional);
 		return null;
 	}
 
 	public static TypedNode TypeClassDecl(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		return null;
 	}
 
 	@Override public void LoadTo(GtNameSpace NameSpace) {
 		// Define Constants
 		NameSpace.DefineSymbol("true", true);
 		NameSpace.DefineSymbol("false", false);
 
 		NameSpace.DefineTokenFunc(" \t", FunctionA(this, "WhiteSpaceToken"));
 		NameSpace.DefineTokenFunc("\n",  FunctionA(this, "IndentToken"));
 		NameSpace.DefineTokenFunc("(){}[]<>.,:;+-*/%=&|!@", FunctionA(this, "OperatorToken"));
 		NameSpace.DefineTokenFunc("/", FunctionA(this, "CommentToken"));  // overloading
 		NameSpace.DefineTokenFunc("Aa", FunctionA(this, "SymbolToken"));
 		NameSpace.DefineTokenFunc("\"", FunctionA(this, "StringLiteralToken"));
 		NameSpace.DefineTokenFunc("1",  FunctionA(this, "NumberLiteralToken"));
 //#ifdef JAVA
 		GtDelegateMatch ParseUnary     = FunctionB(this, "ParseUnary");
 		GtDelegateType  TypeUnary      = FunctionC(this, "TypeUnary");
 		GtDelegateMatch ParseBinary    = FunctionB(this, "ParseBinary");
 		GtDelegateType  TypeBinary     = FunctionC(this, "TypeBinary");
 		GtDelegateType  TypeConst      = FunctionC(this, "TypeConst");
 		GtDelegateType  TypeBlock      = FunctionC(this, "TypeBlock");
 //endif VAJA
 		NameSpace.DefineSyntaxPattern("+", ParseUnary, TypeUnary);
 		NameSpace.DefineSyntaxPattern("-", ParseUnary, TypeUnary);
 		NameSpace.DefineSyntaxPattern("!", ParseUnary, TypeUnary);
 
 		NameSpace.DefineExtendedPattern("*", BinaryOperator | Precedence_CStyleMUL, ParseBinary, TypeBinary);
 		NameSpace.DefineExtendedPattern("/", BinaryOperator | Precedence_CStyleMUL, ParseBinary, TypeBinary);
 		NameSpace.DefineExtendedPattern("%", BinaryOperator | Precedence_CStyleMUL, ParseBinary, TypeBinary);
 
 		NameSpace.DefineExtendedPattern("+", BinaryOperator | Precedence_CStyleADD, ParseBinary, TypeBinary);
 		NameSpace.DefineExtendedPattern("-", BinaryOperator | Precedence_CStyleADD, ParseBinary, TypeBinary);
 
 		NameSpace.DefineExtendedPattern("<", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.DefineExtendedPattern("<=", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.DefineExtendedPattern(">", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.DefineExtendedPattern(">=", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.DefineExtendedPattern("==", BinaryOperator | Precedence_CStyleEquals, ParseBinary, TypeBinary);
 		NameSpace.DefineExtendedPattern("!=", BinaryOperator | Precedence_CStyleEquals, ParseBinary, TypeBinary);
 		//NameSpace.DefineExtendedPattern("!==", BinaryOperator | Precedence_CStyleEquals, ParseBinary, TypeBinary);
 
 		NameSpace.DefineExtendedPattern("=", BinaryOperator | Precedence_CStyleAssign | LeftJoin, ParseBinary, FunctionC(this, "TypeAssign"));
 		NameSpace.DefineExtendedPattern("&&", BinaryOperator | Precedence_CStyleAND, ParseBinary, FunctionC(this, "TypeAnd"));
 		NameSpace.DefineExtendedPattern("||", BinaryOperator | Precedence_CStyleOR, ParseBinary, FunctionC(this, "TypeOr"));
 
 		NameSpace.DefineSyntaxPattern("$Symbol$", FunctionB(this, "ParseSymbol"), null);
 		NameSpace.DefineSyntaxPattern("$Type$", FunctionB(this, "ParseType"), TypeConst);
 		NameSpace.DefineSyntaxPattern("$Variable$", FunctionB(this, "ParseVariable"), FunctionC(this, "TypeVariable"));
 		NameSpace.DefineSyntaxPattern("$Const$", FunctionB(this, "ParseConst"), TypeConst);
 		NameSpace.DefineSyntaxPattern("$StringLiteral$", FunctionB(this, "ParseStringLiteral"), TypeConst);
 		NameSpace.DefineSyntaxPattern("$IntegerLiteral$", FunctionB(this, "ParseIntegerLiteral"), TypeConst);
 
 		NameSpace.DefineSyntaxPattern("(", FunctionB(this, "ParseParenthesis"), null); /* => */
 		NameSpace.DefineExtendedPattern(".", 0, FunctionB(this, "ParseField"), FunctionC(this, "TypeField"));
 		NameSpace.DefineExtendedPattern("(", 0, FunctionB(this, "ParseApply"), FunctionC(this, "TypeApply"));
 		//future: NameSpace.DefineExtendedPattern("[", 0, FunctionB(this, "ParseIndexer"), FunctionC(this, "TypeIndexer"));
 
 		NameSpace.DefineSyntaxPattern("$Block$", FunctionB(this, "ParseBlock"), TypeBlock);
 		NameSpace.DefineSyntaxPattern("$Statement$", FunctionB(this, "ParseStatement"), TypeBlock);
 		NameSpace.DefineSyntaxPattern("$Expression$", FunctionB(this, "ParseExpression"), TypeBlock);
 
 		NameSpace.DefineSyntaxPattern("$FuncName$", FunctionB(this, "ParseFuncName"), TypeConst);
 		NameSpace.DefineSyntaxPattern("$FuncDecl$", FunctionB(this, "ParseFuncDecl"), FunctionC(this, "TypeFuncDecl"));
 		NameSpace.DefineSyntaxPattern("$VarDecl$",  FunctionB(this, "ParseVarDecl"), FunctionC(this, "TypeVarDecl"));
 
 		NameSpace.DefineSyntaxPattern("if", FunctionB(this, "ParseIf"), FunctionC(this, "TypeIf"));
 		NameSpace.DefineSyntaxPattern("while", FunctionB(this, "ParseWhile"), FunctionC(this, "TypeWhile"));
 		NameSpace.DefineSyntaxPattern("continue", FunctionB(this, "ParseContinue"), FunctionC(this, "TypeContinue"));
 		NameSpace.DefineSyntaxPattern("break", FunctionB(this, "ParseBreak"), FunctionC(this, "TypeBreak"));
 		NameSpace.DefineSyntaxPattern("return", FunctionB(this, "ParseReturn"), FunctionC(this, "TypeReturn"));
 		NameSpace.DefineSyntaxPattern("new", FunctionB(this, "ParseNew"), FunctionC(this, "TypeNew"));  // tentative
 	}
 }
 
 class GtContext extends GtStatic {
 	/*field*/public final  GreenTeaGenerator   Generator;
 	/*field*/public final  GtNameSpace		   RootNameSpace;
 	/*field*/public GtNameSpace		           DefaultNameSpace;
 
 	/*field*/public final GtType		VoidType;
 	/*field*/public final GtType		ObjectType;
 	/*field*/public final GtType		BooleanType;
 	/*field*/public final GtType		IntType;
 	/*field*/public final GtType		StringType;
 	/*field*/public final GtType		VarType;
 	/*field*/public final GtType		AnyType;
 	/*field*/public final GtType		ArrayType;
 	/*field*/public final GtType		FuncType;
 
 	/*field*/public final  GtMap			   ClassNameMap;
 	/*field*/public final  GtMap               LayerMap;
 	/*field*/public final  GtLayer             GreenLayer;
 	/*field*/public final  GtLayer             FieldLayer;
 	/*field*/public final  GtLayer             UserDefinedLayer;
 	/*field*/public int ClassCount;
 	/*field*/public int MethodCount;
 
 	GtContext/*constructor*/(GtGrammar Grammar, GreenTeaGenerator Generator) {
 		this.Generator    = Generator;
 		this.Generator.Context = this;
 		this.ClassNameMap = new GtMap();
 		this.LayerMap     = new GtMap();
 		this.GreenLayer   = this.LoadLayer("GreenTea");
 		this.FieldLayer   = this.LoadLayer("Field");
 		this.UserDefinedLayer = this.LoadLayer("UserDefined");
 		this.RootNameSpace = new GtNameSpace(this, null);
 		this.ClassCount = 0;
 		this.MethodCount = 0;
 		this.VoidType    = this.RootNameSpace.DefineClass(new GtType(this, 0, "void", null));
 		this.ObjectType  = this.RootNameSpace.DefineClass(new GtType(this, 0, "Object", new Object()));
 		this.BooleanType = this.RootNameSpace.DefineClass(new GtType(this, 0, "boolean", false));
 		this.IntType     = this.RootNameSpace.DefineClass(new GtType(this, 0, "int", 0));
 		this.StringType  = this.RootNameSpace.DefineClass(new GtType(this, 0, "String", ""));
 		this.VarType     = this.RootNameSpace.DefineClass(new GtType(this, 0, "var", null));
 		this.AnyType     = this.RootNameSpace.DefineClass(new GtType(this, 0, "any", null));
 		this.ArrayType   = this.RootNameSpace.DefineClass(new GtType(this, 0, "Array", null));
 		this.FuncType    = this.RootNameSpace.DefineClass(new GtType(this, 0, "Func", null));
 		this.ArrayType.Types = new GtType[1];
 		this.ArrayType.Types[0] = this.AnyType;
 		this.FuncType.Types = new GtType[1];
 		this.FuncType.Types[0] = this.VoidType;
 		Grammar.LoadTo(this.RootNameSpace);
 		this.DefaultNameSpace = new GtNameSpace(this, this.RootNameSpace);
 		this.Generator.LoadContext(this);
 	}
 
 	public GtLayer LoadLayer(String Name) {
 		/*local*/GtLayer Layer = new GtLayer(Name);
 		this.LayerMap.put(Name, Layer);
 		return Layer;
 	}
 
 	public void LoadGrammar(GtGrammar Grammar) {
 		Grammar.LoadTo(this.DefaultNameSpace);
 	}
 
 	public void Define(String Symbol, Object Value) {
 		this.RootNameSpace.DefineSymbol(Symbol, Value);
 	}
 
 	public Object Eval(String text, long FileLine) {
 		return this.DefaultNameSpace.Eval(text, FileLine, this.Generator);
 	}
 
 	public GtType GetGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList, boolean IsCreation) {
 		LangDeps.Assert(BaseType.IsGenericType());
 		String MangleName = GtStatic.Mangle(BaseType, BaseIdx, TypeList);
 		GtType GenericType = (/*cast*/GtType)this.ClassNameMap.get(MangleName);
 		if(GenericType == null && IsCreation) {
 			/*local*/int i = BaseIdx;
 			/*local*/String s = BaseType.ShortClassName + "<";
 			while(i < ListSize(TypeList)) {
 				s = s + TypeList.get(i).ShortClassName;
 				i += 1;
 				if(i == ListSize(TypeList)) {
 					s = s + ">";
 				}
 				else {
 					s = s + ",";
 				}
 			}
 			GenericType = BaseType.CreateGenericType(BaseIdx, TypeList, s);
 			this.ClassNameMap.put(MangleName, GenericType);
 		}
 		return GenericType;
 	}
 
 	public GtType GetGenericType1(GtType BaseType, GtType ParamType, boolean IsCreation) {
 		ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		TypeList.add(ParamType);
 		return this.GetGenericType(BaseType, 0, TypeList, IsCreation);
 	}
 
 }
 
 public class GreenTeaScript extends GtStatic {
 
 	private static void TestAll(GtContext Context) {
 		//GtStatic.TestSyntaxPattern(Context, "int");
 		//GtStatic.TestSyntaxPattern(Context, "123");
 		//GtStatic.TestSyntaxPattern(Context, "1 + 2 * 3");
 		TestToken(Context, "1 || 2", "1", "||");
 		TestToken(Context, "1 == 2", "1", "==");
 		TestToken(Context, "1 != 2", "1", "!=");
 		//TestToken(Context, "1 !== 2", "1", "!==");
 		TestToken(Context, "1 *= 2", "1", "*");
 		TestToken(Context, "1 = 2", "1", "=");
 	}
 
 	public final static void main(String[] Args) {
 		//Args = new String[2];
 		//Args[0] = "--perl";
 		//Args[1] = "sample/fibo.green";
 
 		/*local*/int FileIndex = 0;
 		/*local*/String CodeGeneratorName = "--Java";
 		if(Args.length > 0 && Args[0].startsWith("--")) {
 			CodeGeneratorName = Args[0];
 			FileIndex = 1;
 		}
 		/*local*/GreenTeaGenerator Generator = LangDeps.CodeGenerator(CodeGeneratorName);
 		/*local*/GtContext Context = new GtContext(new KonohaGrammar(), Generator);
 		if(Args.length > FileIndex) {
 			Context.Eval(LangDeps.LoadFile(Args[FileIndex]), 1);
 		}
 		else {
 			GreenTeaScript.TestAll(Context);
 		}
 	}
 
 }
