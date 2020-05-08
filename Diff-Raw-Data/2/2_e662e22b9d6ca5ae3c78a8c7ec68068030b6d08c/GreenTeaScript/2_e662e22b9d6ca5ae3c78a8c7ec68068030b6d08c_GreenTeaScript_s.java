 // ***************************************************************************
 // Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // *  Redistributions of source code must retain the above copyright notice,
 //    this list of conditions and the following disclaimer.
 // *  Redistributions in binary form must reproduce the above copyright
 //    notice, this list of conditions and the following disclaimer in the
 //    documentation and/or other materials provided with the distribution.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 // TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 // PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 // CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 // EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 // PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 // OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 // OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 // ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 // **************************************************************************
 
 //ifdef JAVA
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 //endif VAJA
 
 //ifdef JAVA
 interface GtConst {
 //endif VAJA
 	// ClassFlag
 	public final static int		NativeClass	     				= 1 << 0;
 	public final static int		StructClass				    	= 1 << 1;
 	public final static int		DynamicClass				    = 1 << 2;
 	public final static int     EnumClass                       = 1 << 3;
 	public final static int     OpenClass                       = 1 << 4;
 
 	// MethodFlag
 	public final static int		ExportMethod		= 1 << 0;
 	public final static int		AbstractMethod		= 1 << 1;
 	public final static int		VirtualMethod		= 1 << 2;
 	public final static int		NativeMethod		= 1 << 3;
 	public final static int		NativeStaticMethod	= 1 << 4;
 	public final static int		NativeMacroMethod	= 1 << 5;
 	public final static int		NativeVariadicMethod	= 1 << 6;
 	public final static int		DynamicMethod		= 1 << 7;
 	public final static int		ConstMethod			= 1 << 8;
 	public final static int     ImplicitMethod      = 1 << 9;  // used for implicit cast
 
 	public final static int		SymbolMaskSize					= 3;
 	public final static int		LowerSymbolMask					= 1;
 	public final static int		GetterSymbolMask				= (1 << 1);
 	public final static int		SetterSymbolMask				= (1 << 2);
 	public final static int		MetaSymbolMask					= (GetterSymbolMask | SetterSymbolMask);
 	public final static String	GetterPrefix					= "Get";
 	public final static String	SetterPrefix					= "Set";
 	public final static String	MetaPrefix						= "\\";
 
 	public final static int		CreateNewSymbolId				= -1;
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
 		1, TabChar, NewLineChar, 1, 1, NewLineChar, 1, 1,
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
 
 	// TokenFlag
 	public final static int	SourceTokenFlag	    = 1;
 	public final static int	ErrorTokenFlag	    = (1 << 1);
 	public final static int IndentTokenFlag	    = (1 << 2);
 	public final static int	WhiteSpaceTokenFlag	= (1 << 3);
 	public final static int DelimTokenFlag	    = (1 << 4);
 	public final static int QuotedTokenFlag	    = (1 << 5);
 	public final static int NameSymbolTokenFlag	    = (1 << 6);
 
 	// ParseFlag
 	public final static int	BackTrackParseFlag	= 1;
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
 
 	// Const Decl;
 	public final static int	ConstDeclClassIndex	= 0;
 	public final static int	ConstDeclNameIndex	= 1;
 	public final static int	ConstDeclValueIndex	= 2;
 
 	// Method Decl;
 	public final static int	FuncDeclReturnType	= 0;
 	public final static int	FuncDeclClass		= 1;
 	public final static int	FuncDeclName		= 2;
 	public final static int	FuncDeclBlock		= 3;
 	public final static int	FuncDeclParam		= 4;
 
 	// Class Decl;
 	public final static int	ClassParentNameOffset	= 0;
 	public final static int	ClassNameOffset			= 1;
 	public final static int	ClassBlockOffset		= 2;
 
 	// try-catch
 	public final static int TryBody         = 0;
 	public final static int CatchVariable   = 1;
 	public final static int CatchBody       = 2;
 	public final static int FinallyBody     = 3;
 
 	// Enum
 	public final static int EnumNameTreeIndex = 0;
 
 //	// spec
 //	public final static int TokenFuncSpec     = 0;
 //	public final static int SymbolPatternSpec = 1;
 //	public final static int ExtendedPatternSpec = 2;
 
 	public final static int BinaryOperator					= 1;
 	public final static int LeftJoin						= 1 << 1;
 //	public final static int Parenthesis						= 1 << 2;
 	public final static int PrecedenceShift					= 3;
 //	public final static int Precedence_CStyleValue			= (1 << PrecedenceShift);
 //	public final static int Precedence_CPPStyleScope		= (50 << PrecedenceShift);
 //	public final static int Precedence_CStyleSuffixCall		= (100 << PrecedenceShift);				/*x(); x[]; x.x x->x x++ */
 //	public final static int Precedence_CStylePrefixOperator	= (200 << PrecedenceShift);				/*++x; --x; sizeof x &x +x -x !x (T)x  */
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
 //	public final static int Precedence_Error				= (1700 << PrecedenceShift);
 //	public final static int Precedence_Statement			= (1900 << PrecedenceShift);
 //	public final static int Precedence_CStyleDelim			= (2000 << PrecedenceShift);
 
 	public final static int DefaultTypeCheckPolicy			= 0;
 	public final static int NoCheckPolicy                   = 1;
 	public final static int CastPolicy                      = (1 << 1);
 	public final static int IgnoreEmptyPolicy               = (1 << 2);
 	public final static int AllowEmptyPolicy                = (1 << 3);
 	public final static int AllowVoidPolicy                 = (1 << 4);
 	public final static int AllowCoercionPolicy             = (1 << 5);
 
 //	public final static String	GlobalConstName					= "global";
 
 	public final static ArrayList<String> SymbolList = new ArrayList<String>();
 	public final static GtMap   SymbolMap  = new GtMap();
 
 	public final static String[] ShellGrammarReservedKeywords = {"true", "false", "as", "if"};
 
 	public final static boolean UseLangStat = true;
 
 //ifdef JAVA
 }
 
 class GtStatic implements GtConst {
 //endif VAJA
 	// debug flags
 	public static boolean DebugPrintOption = false;
 
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
 
 	public final static int AsciiToTokenMatrixIndex(char c) {
 		if(c < 128) {
 			return CharMatrix[c];
 		}
 		return UnicodeChar;
 	}
 
 //	// Symbol
 //	public final static boolean IsGetterSymbol(int SymbolId) {
 //		return IsFlag(SymbolId, GetterSymbolMask);
 //	}
 //
 //	public final static boolean IsSetterSymbol(int SymbolId) {
 //		return IsFlag(SymbolId, SetterSymbolMask);
 //	}
 //
 //	public final static int ToSetterSymbol(int SymbolId) {
 //		LangDeps.Assert(IsGetterSymbol(SymbolId));
 //		return (SymbolId & (~GetterSymbolMask)) | SetterSymbolMask;
 //	}
 //
 //	public final static int MaskSymbol(int SymbolId, int Mask) {
 //		return (SymbolId << SymbolMaskSize) | Mask;
 //	}
 //
 //	public final static int UnmaskSymbol(int SymbolId) {
 //		return SymbolId >> SymbolMaskSize;
 //	}
 //
 //	public final static String StringfySymbol(int SymbolId) {
 //		/*local*/String Key = SymbolList.get(UnmaskSymbol(SymbolId));
 //		if(IsFlag(SymbolId, GetterSymbolMask)) {
 //			return GetterPrefix + Key;
 //		}
 //		if(IsFlag(SymbolId, SetterSymbolMask)) {
 //			return SetterPrefix + Key;
 //		}
 //		if(IsFlag(SymbolId, MetaSymbolMask)) {
 //			return MetaPrefix + Key;
 //		}
 //		return Key;
 //	}
 //
 //	public final static int GetSymbolId(String Symbol, int DefaultSymbolId) {
 //		/*local*/String Key = Symbol;
 //		/*local*/int Mask = 0;
 //		if(Symbol.length() >= 3 && LangDeps.CharAt(Symbol, 1) == 'e' && LangDeps.CharAt(Symbol, 2) == 't') {
 //			if(LangDeps.CharAt(Symbol, 0) == 'g' && LangDeps.CharAt(Symbol, 0) == 'G') {
 //				Key = Symbol.substring(3);
 //				Mask = GetterSymbolMask;
 //			}
 //			if(LangDeps.CharAt(Symbol, 0) == 's' && LangDeps.CharAt(Symbol, 0) == 'S') {
 //				Key = Symbol.substring(3);
 //				Mask = SetterSymbolMask;
 //			}
 //		}
 //		if(Symbol.startsWith("\\")) {
 //			Mask = MetaSymbolMask;
 //		}
 //		/*local*/Integer SymbolObject = (/*cast*/Integer)SymbolMap.get(Key);
 //		if(SymbolObject == null) {
 //			if(DefaultSymbolId == CreateNewSymbolId) {
 //				/*local*/int SymbolId = SymbolList.size();
 //				SymbolList.add(Key);
 //				SymbolMap.put(Key, SymbolId); //new Integer(SymbolId));
 //				return MaskSymbol(SymbolId, Mask);
 //			}
 //			return DefaultSymbolId;
 //		}
 //		return MaskSymbol(SymbolObject.intValue(), Mask);
 //	}
 //
 //	public final static String CanonicalSymbol(String Symbol) {
 //		return Symbol.toLowerCase().replaceAll("_", "");
 //	}
 //
 //	public final static int GetCanonicalSymbolId(String Symbol) {
 //		return GetSymbolId(CanonicalSymbol(Symbol), CreateNewSymbolId);
 //	}
 
 	private final static String n2s(int n) {
 		if(n < 10) {
 			return LangDeps.CharToString((char)(48 + (n)));
 		}
 		else if(n < (27 + 10)) {
 			return LangDeps.CharToString((char)(65 + (n - 10)));
 		}
 		else {
 			return LangDeps.CharToString((char)(97 + (n - 37)));
 		}
 	}
 
 	public final static String NumberToAscii(int number) {
 		LangDeps.Assert(number < (62 * 62));
 		return n2s((number / 62)) + (number % 62);
 	}
 
 	public final static String MangleGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList) {
 		/*local*/String s = BaseType.ShortClassName + "__";
 		/*local*/int i = BaseIdx;
 		while(i < ListSize(TypeList)) {
 			s = s + NumberToAscii(TypeList.get(i).ClassId);
 			i = i + 1;
 		}
 		return s;
 	}
 
 	public final static String MangleMethodName(GtType BaseType, String MethodName, int BaseIdx, ArrayList<GtType> TypeList) {
 		/*local*/String s = MethodName + "__" + NumberToAscii(BaseType.ClassId);
 		/*local*/int i = BaseIdx;
 		while(i < ListSize(TypeList)) {
 			s = s + NumberToAscii(TypeList.get(i).ClassId);
 			i = i + 1;
 		}
 		return s;
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
 
 	public final static int ApplyTokenFunc(TokenFunc TokenFunc, GtTokenContext TokenContext, String ScriptSource, int Pos) {
 		while(TokenFunc != null) {
 			/*local*/GtDelegateToken delegate = TokenFunc.Func;
 			/*local*/int NextIdx = LangDeps.ApplyTokenFunc(delegate, TokenContext, ScriptSource, Pos);
 			if(NextIdx > Pos) return NextIdx;
 			TokenFunc = TokenFunc.ParentFunc;
 		}
 		return NoMatch;
 	}
 
 	public final static GtSyntaxPattern MergeSyntaxPattern(GtSyntaxPattern Pattern, GtSyntaxPattern Parent) {
 		if(Parent == null) return Pattern;
 		/*local*/GtSyntaxPattern MergedPattern = new GtSyntaxPattern(Pattern.PackageNameSpace, Pattern.PatternName, Pattern.MatchFunc, Pattern.TypeFunc);
 		MergedPattern.ParentPattern = Parent;
 		return MergedPattern;
 	}
 
 	public final static boolean IsEmptyOrError(GtSyntaxTree Tree) {
 		return Tree == null || Tree.IsEmptyOrError();
 	}
 
 	public final static GtSyntaxTree LinkTree(GtSyntaxTree LastNode, GtSyntaxTree Node) {
 		Node.PrevTree = LastNode;
 		if(LastNode != null) {
 			LastNode.NextTree = Node;
 		}
 		return Node;
 	}
 
 	public final static GtSyntaxTree TreeHead(GtSyntaxTree Tree) {
 		if(Tree != null) {
 			while(Tree.PrevTree != null) {
 				Tree = Tree.PrevTree;
 			}
 		}
 		return Tree;
 	}
 
 	public final static GtSyntaxTree ApplySyntaxPattern(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int Pos = TokenContext.CurrentPosition;
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		/*local*/GtSyntaxPattern CurrentPattern = Pattern;
 		while(CurrentPattern != null) {
 			/*local*/GtDelegateMatch delegate = CurrentPattern.MatchFunc;
 			TokenContext.CurrentPosition = Pos;
 			if(CurrentPattern.ParentPattern != null) {
 				TokenContext.ParseFlag = ParseFlag | BackTrackParseFlag;
 			}
 			//DebugP("B :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + ", next=" + CurrentPattern.ParentPattern);
 			TokenContext.IndentLevel += 1;
 			/*local*/GtSyntaxTree ParsedTree = (/*cast*/GtSyntaxTree)LangDeps.ApplyMatchFunc(delegate, NameSpace, TokenContext, LeftTree, CurrentPattern);
 			TokenContext.IndentLevel -= 1;
 			if(ParsedTree != null && ParsedTree.IsEmpty()) {
 				ParsedTree = null;
 			}
 			//DebugP("E :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + " => " + ParsedTree);
 			TokenContext.ParseFlag = ParseFlag;
 			if(ParsedTree != null) {
 				return ParsedTree;
 			}
 			CurrentPattern = CurrentPattern.ParentPattern;
 		}
 		if(TokenContext.IsAllowedBackTrack()) {
 			TokenContext.CurrentPosition = Pos;
 		}
 		if(Pattern == null) {
 			DebugP("undefined syntax pattern: " + Pattern);
 		}
 		return TokenContext.ReportExpectedPattern(Pattern);
 	}
 
 	public final static GtSyntaxTree ParseExpression(GtNameSpace NameSpace, GtTokenContext TokenContext) {
 		/*local*/GtSyntaxPattern Pattern = TokenContext.GetFirstPattern();
 		/*local*/GtSyntaxTree LeftTree = GtStatic.ApplySyntaxPattern(NameSpace, TokenContext, null, Pattern);
 		TokenContext.SkipIndent();
 		while(!GtStatic.IsEmptyOrError(LeftTree) && !TokenContext.MatchToken(";")) {
 			/*local*/GtSyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern();
 			if(ExtendedPattern == null) {
 				//DebugP("In $Expression$ ending: " + TokenContext.GetToken());
 				break;
 			}
 			LeftTree = GtStatic.ApplySyntaxPattern(NameSpace, TokenContext, LeftTree, ExtendedPattern);
 		}
 		return LeftTree;
 	}
 
 	// typing
 	public final static GtNode ApplyTypeFunc(GtDelegateType delegate, GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		LangDeps.Assert(delegate != null);
 		return (/*cast*/GtNode)LangDeps.ApplyTypeFunc(delegate, Gamma, ParsedTree, Type);
 	}
 
 	public final static GtNode LinkNode(GtNode LastNode, GtNode Node) {
 		Node.PrevNode = LastNode;
 		if(LastNode != null) {
 			LastNode.NextNode = Node;
 		}
 		return Node;
 	}
 //ifdef JAVA
 }
 
 final class GtMap {
 	final HashMap<String, Object>	Map;
 
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
 		return LangDeps.MapGetKeys(this);
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
 	/*field*/public GtSyntaxPattern	PresetPattern;
 
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
 
 	public boolean IsQuoted() {
 		return IsFlag(this.TokenFlag, QuotedTokenFlag);
 	}
 
 	public boolean IsNameSymbol() {
 		return IsFlag(this.TokenFlag, NameSymbolTokenFlag);
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
 
 final class GtTokenContext extends GtStatic {
 	/*field*/public final static GtToken NullToken = new GtToken("", 0);
 
 	/*field*/public GtNameSpace TopLevelNameSpace;
 	/*field*/public ArrayList<GtToken> SourceList;
 	/*field*/public int CurrentPosition;
 	/*field*/public long ParsingLine;
 	/*field*/public int ParseFlag;
 	/*field*/public int IndentLevel = 0;
 
 	GtTokenContext/*constructor*/(GtNameSpace NameSpace, String Text, long FileLine) {
 		this.TopLevelNameSpace = NameSpace;
 		this.SourceList = new ArrayList<GtToken>();
 		this.CurrentPosition = 0;
 		this.ParsingLine = FileLine;
 		this.ParseFlag = 0;
 		this.AddNewToken(Text, SourceTokenFlag, null);
 		this.IndentLevel = 0;
 	}
 
 	public GtToken AddNewToken(String Text, int TokenFlag, String PatternName) {
 		/*local*/GtToken Token = new GtToken(Text, this.ParsingLine);
 		Token.TokenFlag |= TokenFlag;
 		if(PatternName != null) {
 			Token.PresetPattern = this.TopLevelNameSpace.GetPattern(PatternName);
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
 		this.TopLevelNameSpace.Context.ReportError(Level, Token, Message);
 	}
 
 	public GtSyntaxTree NewErrorSyntaxTree(GtToken Token, String Message) {
 		if(!this.IsAllowedBackTrack()) {
 			this.TopLevelNameSpace.Context.ReportError(ErrorLevel, Token, Message);
 			/*local*/GtSyntaxTree ErrorTree = new GtSyntaxTree(Token.PresetPattern, this.TopLevelNameSpace, Token, null);
 			return ErrorTree;
 		}
 		return null;
 	}
 
 	public GtToken GetBeforeToken() {
 		/*local*/int pos = this.CurrentPosition - 1;
 		while(pos >= 0 && pos < this.SourceList.size()) {
 			/*local*/GtToken Token = this.SourceList.get(pos);
 			if(IsFlag(Token.TokenFlag, IndentTokenFlag)) {
 				pos -= 1;
 				continue;
 			}
 			return Token;
 		}
 		return null;
 	}
 
 	public GtSyntaxTree ReportExpectedToken(String TokenText) {
 		if(!this.IsAllowedBackTrack()) {
 			/*local*/GtToken Token = this.GetBeforeToken();
 			if(Token != null) {
 				return this.NewErrorSyntaxTree(Token, TokenText + " is expected after " + Token.ParsedText);
 			}
 			Token = this.GetToken();
 			LangDeps.Assert(Token != GtTokenContext.NullToken);
 			return this.NewErrorSyntaxTree(Token, TokenText + " is expected at " + Token.ParsedText);
 		}
 		return null;
 	}
 
 	public GtSyntaxTree ReportExpectedPattern(GtSyntaxPattern Pattern) {
 		if(Pattern == null){
 			return this.ReportExpectedToken("null");
 		}
 		return this.ReportExpectedToken(Pattern.PatternName);
 	}
 
 	public void Vacume() {
 		if(this.CurrentPosition > 0) {
 			/*local*/ArrayList<GtToken> NewList = new ArrayList<GtToken>();
 			/*local*/int i = this.CurrentPosition;
 			while(i < ListSize(this.SourceList)) {
 				NewList.add(this.SourceList.get(i));
 				i = i + 1;
 			}
 			this.SourceList = NewList;
 			this.CurrentPosition = 0;
 		}
 	}
 
 	private int DispatchFunc(String ScriptSource, int GtChar, int pos) {
 		/*local*/TokenFunc TokenFunc = this.TopLevelNameSpace.GetTokenFunc(GtChar);
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
 			/*local*/int gtCode = AsciiToTokenMatrixIndex(LangDeps.CharAt(ScriptSource, currentPos));
 			/*local*/int nextPos = this.DispatchFunc(ScriptSource, gtCode, currentPos);
 			if(currentPos >= nextPos) {
 				break;
 			}
 			currentPos = nextPos;
 		}
 		this.Dump();
 	}
 
 	public GtToken GetToken() {
 		while((this.CurrentPosition < this.SourceList.size())) {
 			/*local*/GtToken Token = this.SourceList.get(this.CurrentPosition);
 			if(Token.IsSource()) {
 				this.SourceList.remove(this.SourceList.size()-1);
 				this.Tokenize(Token.ParsedText, Token.FileLine);
 				Token = this.SourceList.get(this.CurrentPosition);
 			}
 			if(IsFlag(this.ParseFlag, SkipIndentParseFlag) && Token.IsIndent()) {
 				this.CurrentPosition = this.CurrentPosition + 1;
 				continue;
 			}
 			return Token;
 		}
 		return GtTokenContext.NullToken;
 	}
 
 	public boolean HasNext() {
 		return (this.GetToken() != GtTokenContext.NullToken);
 	}
 
 	public GtToken Next() {
 		/*local*/GtToken Token = this.GetToken();
 		this.CurrentPosition += 1;
 		return Token;
 	}
 
 	public void SkipIndent() {
 		GtToken Token = this.GetToken();
 		while(Token.IsIndent()) {
 			this.CurrentPosition = this.CurrentPosition + 1;
 			Token = this.GetToken();
 		}
 	}
 
 	public GtSyntaxPattern GetPattern(String PatternName) {
 		return this.TopLevelNameSpace.GetPattern(PatternName);
 	}
 
 	public GtSyntaxPattern GetFirstPattern() {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token.PresetPattern != null) {
 			return Token.PresetPattern;
 		}
 		/*local*/GtSyntaxPattern Pattern = this.TopLevelNameSpace.GetPattern(Token.ParsedText);
 		if(Pattern == null) {
 			return this.TopLevelNameSpace.GetPattern("$Symbol$");
 		}
 		return Pattern;
 	}
 
 	public GtSyntaxPattern GetExtendedPattern() {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token != GtTokenContext.NullToken) {
 			/*local*/GtSyntaxPattern Pattern = this.TopLevelNameSpace.GetExtendedPattern(Token.ParsedText);
 			return Pattern;
 		}
 		return null;
 	}
 
 	public boolean MatchToken(String TokenText) {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token.EqualsText(TokenText)) {
 			this.CurrentPosition += 1;
 			return true;
 		}
 		return false;
 	}
 
 	public GtToken GetMatchedToken(String TokenText) {
 		/*local*/GtToken Token = this.GetToken();
 		while(Token != GtTokenContext.NullToken) {
 			this.CurrentPosition += 1;
 			if(Token.EqualsText(TokenText)) {
 				break;
 			}
 			Token = this.GetToken();
 		}
 		return Token;
 	}
 
 	public final boolean IsAllowedBackTrack() {
 		return IsFlag(this.ParseFlag, BackTrackParseFlag);
 	}
 
 	public final int SetBackTrack(boolean Allowed) {
 		/*local*/int ParseFlag = this.ParseFlag;
 		if(Allowed) {
 			this.ParseFlag = this.ParseFlag | BackTrackParseFlag;
 		}
 		else {
 			this.ParseFlag = (~(BackTrackParseFlag) & this.ParseFlag);
 		}
 		return ParseFlag;
 	}
 
 	public final GtSyntaxTree ParsePatternAfter(GtNameSpace NameSpace, GtSyntaxTree LeftTree, String PatternName, boolean IsOptional) {
 		/*local*/int Pos = this.CurrentPosition;
 		/*local*/int ParseFlag = this.ParseFlag;
 		/*local*/GtSyntaxPattern Pattern = this.GetPattern(PatternName);
 		if(IsOptional) {
 			this.ParseFlag = this.ParseFlag | BackTrackParseFlag;
 		}
 		/*local*/GtSyntaxTree SyntaxTree = GtStatic.ApplySyntaxPattern(NameSpace, this, LeftTree, Pattern);
 		this.ParseFlag = ParseFlag;
 		if(SyntaxTree != null) {
 			return SyntaxTree;
 		}
 		this.CurrentPosition = Pos;
 		return null;
 	}
 
 	public final GtSyntaxTree ParsePattern(GtNameSpace NameSpace, String PatternName, boolean IsOptional) {
 		return this.ParsePatternAfter(NameSpace, null, PatternName, IsOptional);
 	}
 
 	public final GtMap SkipAndGetAnnotation(boolean IsAllowedDelim) {
 		// this is tentative implementation. In the future, you have to
 		// use this pattern.
 		GtMap Annotation = null;
 		this.SkipIndent();
 		while(this.MatchToken("@")) {
 			/*local*/GtToken Token = this.Next();
 			if(Annotation == null) {
 				Annotation = new GtMap();
 			}
 			Annotation.put(Token.ParsedText, true);
 			this.SkipIndent();
 //			if(this.MatchToken(";")) {
 //				if(IsAllowedDelim) {
 //					Annotation = null; // empty statement
 //					this.SkipIndent();
 //				}
 //				else {
 //					return null;
 //				}
 //			}
 		}
 		return Annotation;
 	}
 
 	public final boolean SkipEmptyStatement() {
 		/*local*/GtToken Token = null;
 		while((Token = this.GetToken()) != GtTokenContext.NullToken) {
 			if(Token.IsIndent() || Token.IsDelim()) {
 				this.CurrentPosition += 1;
 				continue;
 			}
 			break;
 		}
 		return (Token != GtTokenContext.NullToken);
 	}
 
 	public void Dump() {
 		/*local*/int pos = this.CurrentPosition;
 		while(pos < this.SourceList.size()) {
 			/*local*/GtToken token = this.SourceList.get(pos);
 			DebugP("["+pos+"]\t" + token + " : " + token.PresetPattern);
 			pos += 1;
 		}
 	}
 }
 
 final class GtSyntaxPattern extends GtStatic {
 	/*field*/public GtNameSpace	          PackageNameSpace;
 	/*field*/public String		          PatternName;
 	/*field*/int				          SyntaxFlag;
 	/*field*/public GtDelegateMatch       MatchFunc;
 	/*field*/public GtDelegateType        TypeFunc;
 	/*field*/public GtSyntaxPattern	      ParentPattern;
 
 	GtSyntaxPattern/*constructor*/(GtNameSpace NameSpace, String PatternName, GtDelegateMatch MatchFunc, GtDelegateType TypeFunc) {
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
 
 	public boolean IsLeftJoin(GtSyntaxPattern Right) {
 		/*local*/int left = this.SyntaxFlag >> PrecedenceShift;
 		/*local*/int right = Right.SyntaxFlag >> PrecedenceShift;
 		return (left < right || (left == right && IsFlag(this.SyntaxFlag, LeftJoin) && IsFlag(Right.SyntaxFlag, LeftJoin)));
 	}
 }
 
 class GtSyntaxTree extends GtStatic {
 	/*field*/public GtSyntaxTree		ParentTree;
 	/*field*/public GtSyntaxTree		PrevTree;
 	/*field*/public GtSyntaxTree		NextTree;
 
 	/*field*/public GtNameSpace	    NameSpace;
 	/*field*/public GtSyntaxPattern	Pattern;
 	/*field*/public GtToken		    KeyToken;
 	/*field*/public ArrayList<GtSyntaxTree> TreeList;
 	/*field*/public Object          ConstValue;
 	/*field*/public GtMap           Annotation;
 
 	GtSyntaxTree/*constructor*/(GtSyntaxPattern Pattern, GtNameSpace NameSpace, GtToken KeyToken, Object ConstValue) {
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
 			/*local*/GtSyntaxTree SubTree = this.TreeList.get(i);
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
 				this.Annotation.put(Key, false);  // consumed;
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
 		return (this.KeyToken == GtTokenContext.NullToken && this.Pattern == null);
 	}
 
 	public void ToEmpty() {
 		this.KeyToken = GtTokenContext.NullToken;
 		this.TreeList = null;
 		this.Pattern = null; // Empty tree must backtrack
 	}
 
 	public boolean IsEmptyOrError() {
 		return this.KeyToken == GtTokenContext.NullToken || this.KeyToken.IsError();
 	}
 
 	public void ToEmptyOrError(GtSyntaxTree ErrorTree) {
 		if(ErrorTree == null) {
 			this.ToEmpty();
 		}
 		else {
 			this.ToError(ErrorTree.KeyToken);
 		}
 	}
 
 	public GtSyntaxTree GetSyntaxTreeAt(int Index) {
 		if(this.TreeList != null && Index >= this.TreeList.size()) {
 			return null;
 		}
 		return this.TreeList.get(Index);
 	}
 
 	public void SetSyntaxTreeAt(int Index, GtSyntaxTree Tree) {
 		if(!this.IsError()) {
 			if(Tree.IsError()) {
 				this.ToError(Tree.KeyToken);
 			}
 			else {
 				if(Index >= 0) {
 					if(this.TreeList == null) {
 						this.TreeList = new ArrayList<GtSyntaxTree>();
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
 
 	public void SetMatchedPatternAt(int Index, GtNameSpace NameSpace, GtTokenContext TokenContext, String PatternName,  boolean IsOptional) {
 		if(!this.IsEmptyOrError()) {
 			/*local*/GtSyntaxTree ParsedTree = TokenContext.ParsePattern(NameSpace, PatternName, IsOptional);
 			if(ParsedTree != null) {
 				this.SetSyntaxTreeAt(Index, ParsedTree);
 			}
 			else if(ParsedTree == null && !IsOptional) {
 				this.ToEmpty();
 			}
 		}
 	}
 
 	public void SetMatchedTokenAt(int Index, GtNameSpace NameSpace, GtTokenContext TokenContext, String TokenText, boolean IsOptional) {
 		if(!this.IsEmptyOrError()) {
 			/*local*/int Pos = TokenContext.CurrentPosition;
 			/*local*/GtToken Token = TokenContext.Next();
 			if(Token.ParsedText.equals(TokenText)) {
 				this.SetSyntaxTreeAt(Index, new GtSyntaxTree(null, NameSpace, Token, null));
 			}
 			else {
 				TokenContext.CurrentPosition = Pos;
 				if(!IsOptional) {
 					this.ToEmptyOrError(TokenContext.ReportExpectedToken(TokenText));
 				}
 			}
 		}
 	}
 
 	public void AppendParsedTree(GtSyntaxTree Tree) {
 		if(!this.IsError()) {
 			if(Tree.IsError()) {
 				this.ToError(Tree.KeyToken);
 			}
 			else {
 				if(this.TreeList == null) {
 					this.TreeList = new ArrayList<GtSyntaxTree>();
 				}
 				this.TreeList.add(Tree);
 			}
 		}
 	}
 
 	public final GtType GetParsedType() {
 		return (/*cast*/GtType)this.ConstValue;
 	}
 
 	public final boolean HasNodeAt(int Index) {
 		return this.TreeList != null && Index < this.TreeList.size();
 	}
 
 	public final GtNode TypeCheckNodeAt(int Index, GtTypeEnv Gamma, GtType Type, int TypeCheckPolicy) {
 		if(this.TreeList != null && Index < this.TreeList.size()) {
 			/*local*/GtSyntaxTree ParsedTree = this.TreeList.get(Index);
 			if(ParsedTree != null) {
 				/*local*/GtNode Node = GtStatic.ApplyTypeFunc(ParsedTree.Pattern.TypeFunc, Gamma, ParsedTree, Type);
 				/*local*/GtNode TypedNode = Gamma.TypeCheckSingleNode(ParsedTree, Node, Type, TypeCheckPolicy);
 				return TypedNode;
 			}
 		}
 		if(IsFlag(TypeCheckPolicy, AllowEmptyPolicy)) {
 			return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 		}
 		return Gamma.CreateErrorNode2(this, "not empty");
 	}
 
 }
 
 /* typing */
 
 final class GtVariableInfo {
 	/*field*/public GtType	Type;
 	/*field*/public String	Name;
 	/*field*/public String	LocalName;
 
 	GtVariableInfo/*constructor*/(GtType Type, String Name, int Index) {
 		this.Type = Type;
 		this.Name = Name;
 		this.LocalName = Name + Index;
 	}
 }
 
 final class GtTypeEnv extends GtStatic {
 	/*field*/public final GtClassContext    Context;
 	/*field*/public final GtGenerator       Generator;
 	/*field*/public final GtNameSpace	    NameSpace;
 
 	/*field*/public GtMethod	Method;
 	/*field*/public ArrayList<GtVariableInfo> LocalStackList;
 	/*field*/public int StackTopIndex;
 
 	/* for convinient short cut */
 	/*field*/public final GtType	VoidType;
 	/*field*/public final GtType	BooleanType;
 	/*field*/public final GtType	IntType;
 	/*field*/public final GtType	StringType;
 	/*field*/public final GtType	VarType;
 	/*field*/public final GtType	AnyType;
 	/*field*/public final GtType    ArrayType;
 	/*field*/public final GtType    FuncType;
 
 	GtTypeEnv/*constructor*/(GtNameSpace NameSpace) {
 		this.NameSpace = NameSpace;
 		this.Context   = NameSpace.Context;
 		this.Generator = NameSpace.Context.Generator;
 		this.Method = null;
 		this.LocalStackList = new ArrayList<GtVariableInfo>();
 		this.StackTopIndex = 0;
 
 		this.VoidType = NameSpace.Context.VoidType;
 		this.BooleanType = NameSpace.Context.BooleanType;
 		this.IntType = NameSpace.Context.IntType;
 		this.StringType = NameSpace.Context.StringType;
 		this.VarType = NameSpace.Context.VarType;
 		this.AnyType = NameSpace.Context.AnyType;
 		this.ArrayType = NameSpace.Context.ArrayType;
 		this.FuncType = NameSpace.Context.FuncType;
 	}
 
 	public final boolean IsStrictMode() {
 		return this.Generator.IsStrictMode();
 	}
 
 	public final boolean IsTopLevel() {
 		return (this.Method == null);
 	}
 
 	public boolean AppendDeclaredVariable(GtType Type, String Name) {
 		/*local*/GtVariableInfo VarInfo = new GtVariableInfo(Type, Name, this.StackTopIndex);
 		if(this.StackTopIndex < this.LocalStackList.size()) {
 			this.LocalStackList.set(this.StackTopIndex, VarInfo);
 		}
 		else {
 			this.LocalStackList.add(VarInfo);
 		}
 		this.StackTopIndex += 1;
 		return true;
 	}
 
 	public GtVariableInfo LookupDeclaredVariable(String Symbol) {
 		/*local*/int i = this.StackTopIndex - 1;
 		while(i >= 0) {
 			/*local*/GtVariableInfo VarInfo = this.LocalStackList.get(i);
 			if(VarInfo.Name.equals(Symbol)) {
 				return VarInfo;
 			}
 			i -= 1;
 		}
 		return null;
 	}
 
 	public final GtNode ReportTypeResult(GtSyntaxTree ParsedTree, GtNode Node, int Level, String Message) {
 		this.NameSpace.Context.ReportError(Level, Node.Token, Message);
 		if(!this.IsStrictMode()) {
 			return Node;
 		}
 		return this.Generator.CreateErrorNode(this.VoidType, ParsedTree);
 	}
 
 	public GtNode CreateErrorNode2(GtSyntaxTree ParsedTree, String Message) {
 		this.NameSpace.Context.ReportError(ErrorLevel, ParsedTree.KeyToken, Message);
 		return this.Generator.CreateErrorNode(this.VoidType, ParsedTree);
 	}
 
 	public GtNode DefaultValueConstNode(GtSyntaxTree ParsedTree, GtType Type) {
 		if(Type.DefaultNullValue != null) {
 			return this.Generator.CreateConstNode(Type, ParsedTree, Type.DefaultNullValue);
 		}
 		return this.CreateErrorNode2(ParsedTree, "undefined initial value of " + Type);
 	}
 
 	public GtNode SupportedOnlyTopLevelError(GtSyntaxTree ParsedTree) {
 		return this.CreateErrorNode2(ParsedTree, "supported only at top level " + ParsedTree.Pattern);
 	}
 
 	public GtNode UnsupportedTopLevelError(GtSyntaxTree ParsedTree) {
 		return this.CreateErrorNode2(ParsedTree, "unsupported at top level " + ParsedTree.Pattern);
 	}
 
 	/* typing */
 	public GtNode TypeEachNode(GtSyntaxTree Tree, GtType Type) {
 		/*local*/GtNode Node = GtStatic.ApplyTypeFunc(Tree.Pattern.TypeFunc, this, Tree, Type);
 		return Node;
 	}
 
 	public GtNode TypeCheckEachNode(GtSyntaxTree Tree, GtType Type, int TypeCheckPolicy) {
 		/*local*/GtNode Node = this.TypeEachNode(Tree, Type);
 		if(Node.IsError()) {
 			return Node;
 		}
 		return Node;
 	}
 
 	public GtNode TypeBlock(GtSyntaxTree ParsedTree, GtType Type) {
 		/*local*/int StackTopIndex = this.StackTopIndex;
 		/*local*/GtNode LastNode = null;
 		while(ParsedTree != null) {
 			/*local*/GtType CurrentType = Type;
 			if(ParsedTree.NextTree != null) {
 				CurrentType = this.VoidType;
 			}
 			/*local*/GtNode TypedNode = this.TypeCheckEachNode(ParsedTree, CurrentType, DefaultTypeCheckPolicy);
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
 
 	public GtNode TypeCheck(GtSyntaxTree ParsedTree, GtType Type, int TypeCheckPolicy) {
 		return this.TypeBlock(ParsedTree, Type);
 	}
 
 	public final GtNode TypeCheckSingleNode(GtSyntaxTree ParsedTree, GtNode Node, GtType Type, int TypeCheckPolicy) {
 		if(Node.IsError() || IsFlag(TypeCheckPolicy, NoCheckPolicy)) {
 			return Node;
 		}
 		if(IsFlag(TypeCheckPolicy, AllowVoidPolicy) || Type == this.VoidType) {
 			return Node;
 		}
 		if(Node.Type == this.VarType) {
 			return this.ReportTypeResult(ParsedTree, Node, ErrorLevel, "unspecified type: " + Node.Token.ParsedText);
 		}
 		if(Node.Type == Type || Type == this.VarType || Type.Accept(Node.Type)) {
 			return Node;
 		}
 		GtMethod Method = Type.Context.GetCastMethod(Node.Type, Type, true);
 		if(Method != null && (IsFlag(TypeCheckPolicy, CastPolicy) || Method.Is(ImplicitMethod))) {
 			GtNode ApplyNode = this.Generator.CreateApplyNode(Type, ParsedTree, Method);
 			ApplyNode.Append(Node);
 			return ApplyNode;
 		}
 		return this.ReportTypeResult(ParsedTree, Node, ErrorLevel, "type error: requested = " + Type + ", given = " + Node.Type);
 	}
 
 	public final void DefineMethod(GtMethod Method) {
 		this.NameSpace.Context.DefineMethod(Method);
 		/*local*/Object Value = this.NameSpace.GetSymbol(Method.MethodName);
 		if(Value == null) {
 			this.NameSpace.DefineSymbol(Method.MethodName, Method);
 		}
 		this.Method = Method;
 	}
 
 }
 
 // NameSpace
 
 final class GtNameSpace extends GtStatic {
 	/*field*/public final GtClassContext		Context;
 	/*field*/public final GtNameSpace		    ParentNameSpace;
 	/*field*/public String                      PackageName;
 
 	/*field*/TokenFunc[] TokenMatrix;
 	/*field*/GtMap	 SymbolPatternTable;
 
 	GtNameSpace/*constructor*/(GtClassContext Context, GtNameSpace ParentNameSpace) {
 		this.Context = Context;
 		this.ParentNameSpace = ParentNameSpace;
 		this.PackageName = (ParentNameSpace != null) ? ParentNameSpace.PackageName : null;
 		this.TokenMatrix = null;
 		this.SymbolPatternTable = null;
 	}
 
 	public final TokenFunc GetTokenFunc(int GtChar2) {
 		if(this.TokenMatrix == null) {
 			return this.ParentNameSpace.GetTokenFunc(GtChar2);
 		}
 		return this.TokenMatrix[GtChar2];
 	}
 
 	public final void DefineTokenFunc(String keys, GtDelegateToken f) {
 		/*local*/int i = 0;
 		if(this.TokenMatrix == null) {
 			this.TokenMatrix = new TokenFunc[MaxSizeOfChars];
 			if(this.ParentNameSpace != null) {
 				while(i < MaxSizeOfChars) {
 					this.TokenMatrix[i] = this.ParentNameSpace.GetTokenFunc(i);
 				}
 			}
 		}
 		i = 0;
 		while(i < keys.length()) {
 			/*local*/int kchar = GtStatic.AsciiToTokenMatrixIndex(LangDeps.CharAt(keys, i));
 			this.TokenMatrix[kchar] = LangDeps.CreateOrReuseTokenFunc(f, this.TokenMatrix[kchar]);
 			i += 1;
 		}
 	}
 
 	public final Object GetSymbol(String Key) {
 		GtNameSpace NameSpace = this;
 		while(NameSpace != null) {
 			if(NameSpace.SymbolPatternTable != null) {
 				Object Value = NameSpace.SymbolPatternTable.get(Key);
 				if(Value != null) {
 					return Value;
 				}
 			}
 			NameSpace = NameSpace.ParentNameSpace;
 		}
 		return null;
 	}
 
 	public void DefineSymbol(String Key, Object Value) {
 		if(this.SymbolPatternTable == null) {
 			this.SymbolPatternTable = new GtMap();
 		}
 		this.SymbolPatternTable.put(Key, Value);
 	}
 
 	public GtSyntaxPattern GetPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol(PatternName);
 		if(Body instanceof GtSyntaxPattern){
 			return (/*cast*/GtSyntaxPattern)Body;
 		}
 		return null;
 	}
 
 	public GtSyntaxPattern GetExtendedPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol("\t" + PatternName);
 		if(Body instanceof GtSyntaxPattern){
 			return (/*cast*/GtSyntaxPattern)Body;
 		}
 		return null;
 	}
 
 	private void AppendPattern(String PatternName, GtSyntaxPattern NewPattern) {
 		LangDeps.Assert(NewPattern.ParentPattern == null);
 		/*local*/GtSyntaxPattern ParentPattern = this.GetPattern(PatternName);
 		NewPattern.ParentPattern = ParentPattern;
 		this.DefineSymbol(PatternName, NewPattern);
 	}
 
 	public void DefineSyntaxPattern(String PatternName, GtDelegateMatch MatchFunc, GtDelegateType TypeFunc) {
 		/*local*/GtSyntaxPattern Pattern = new GtSyntaxPattern(this, PatternName, MatchFunc, TypeFunc);
 		this.AppendPattern(PatternName, Pattern);
 	}
 
 	public void DefineExtendedPattern(String PatternName, int SyntaxFlag, GtDelegateMatch MatchFunc, GtDelegateType TypeFunc) {
 		/*local*/GtSyntaxPattern Pattern = new GtSyntaxPattern(this, PatternName, MatchFunc, TypeFunc);
 		Pattern.SyntaxFlag = SyntaxFlag;
 		this.AppendPattern("\t" + PatternName, Pattern);
 	}
 
 	public final GtType DefineClassSymbol(GtType ClassInfo) {
 		if(ClassInfo.PackageNameSpace == null) {
 			ClassInfo.PackageNameSpace = this;
 			if(this.PackageName != null) {
 				this.Context.ClassNameMap.put(this.PackageName + "." + ClassInfo.ShortClassName, ClassInfo);
 			}
 		}
 		if(ClassInfo.BaseClass == ClassInfo) {
 			this.DefineSymbol(ClassInfo.ShortClassName, ClassInfo);
 		}
 		return ClassInfo;
 	}
 
 }
 
 class GtGrammar extends GtStatic {
 	public void LoadTo(GtNameSpace NameSpace) {
 		/*extension*/
 	}
 }
 
 final class DScriptGrammar extends GtGrammar {
 	// Token
 	public static int WhiteSpaceToken(GtTokenContext TokenContext, String SourceText, int pos) {
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
 
 	public static int IndentToken(GtTokenContext TokenContext, String SourceText, int pos) {
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
 
 	public static int SymbolToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, pos);
 			if(!LangDeps.IsLetter(ch) && !LangDeps.IsDigit(ch) && ch != '_') {
 				break;
 			}
 			pos += 1;
 		}
 		TokenContext.AddNewToken(SourceText.substring(start, pos), NameSymbolTokenFlag, null);
 		return pos;
 	}
 
 	public static int OperatorToken(GtTokenContext TokenContext, String SourceText, int pos) {
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
 			/*local*/GtSyntaxPattern Pattern = TokenContext.TopLevelNameSpace.GetExtendedPattern(Sub);
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
 
 	public static int CommentToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int NextPos = pos + 1;
 		if(NextPos < SourceText.length()) {
 			/*local*/char NextChar = LangDeps.CharAt(SourceText, NextPos);
 			if(NextChar != '/' && NextChar != '*') {
 				return NoMatch;
 			}
 			int Level = 0;
 			/*local*/char PrevChar = 0;
 			if(NextChar == '*') {
 				Level = 1;
 			}
 			while(NextPos < SourceText.length()) {
 				NextChar = LangDeps.CharAt(SourceText, NextPos);
 				if(NextChar == '\n' && Level == 0) {
 					return DScriptGrammar.IndentToken(TokenContext, SourceText, NextPos);
 				}
 				if(NextChar == '/' && PrevChar == '*') {
 					if(Level == 1) {
 						return NextPos + 1;
 					}
 					Level = Level - 1;
 				}
 				if(Level > 0) {
 					if(NextChar == '*' && PrevChar == '/') {
 						Level = Level + 1;
 					}
 				}
 				NextPos = NextPos + 1;
 			}
 		}
 		return NoMatch;
 	}
 
 	public static int NumberLiteralToken(GtTokenContext TokenContext, String SourceText, int pos) {
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
 
 	public static int StringLiteralToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos + 1;
 		/*local*/char prev = '"';
 		pos = pos + 1; // eat "\""
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, pos);
 			if(ch == '"' && prev != '\\') {
 				TokenContext.AddNewToken(SourceText.substring(start, pos), QuotedTokenFlag, "$StringLiteral$");
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
 
 	public static int StringLiteralToken_StringInterpolation(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos + 1;
 		/*local*/int NextPos = start;
 		/*local*/char prev = '"';
 		while(NextPos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, NextPos);
 			if(ch == '$') {
 				/*local*/int end = NextPos + 1;
 				ch = LangDeps.CharAt(SourceText, end);
 				if(ch == '{') {
 					while(end < SourceText.length()) {
 						ch = LangDeps.CharAt(SourceText, end);
 						if(ch == '}') {
 							break;
 						}
 						end = end + 1;
 					}
 					/*local*/String Expr = SourceText.substring(NextPos + 2, end);
 					/*local*/GtTokenContext LocalContext = new GtTokenContext(TokenContext.TopLevelNameSpace, Expr, TokenContext.ParsingLine);
 					LocalContext.SkipEmptyStatement();
 
 					TokenContext.AddNewToken(SourceText.substring(start, NextPos), 0, "$StringLiteral$");
 					TokenContext.AddNewToken("+", 0, null);
 					while(LocalContext.HasNext()) {
 						GtToken NewToken = LocalContext.Next();
 						TokenContext.AddNewToken(NewToken.ParsedText, 0, null);
 					}
 					TokenContext.AddNewToken("+", 0, null);
 					end = end + 1;
 					start = end;
 					NextPos = end;
 					prev = ch;
 					if(ch == '"') {
 						TokenContext.AddNewToken(SourceText.substring(start, NextPos), 0, "$StringLiteral$");
 						return NextPos + 1;
 					}
 					continue;
 				}
 			}
 			if(ch == '"' && prev != '\\') {
 				TokenContext.AddNewToken(SourceText.substring(start, NextPos), 0, "$StringLiteral$");
 				return NextPos + 1;
 			}
 			if(ch == '\n') {
 				TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", SourceText.substring(start, NextPos));
 				TokenContext.FoundLineFeed(1);
 				return NextPos;
 			}
 			NextPos = NextPos + 1;
 			prev = ch;
 		}
 		TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", SourceText.substring(start, NextPos));
 		return NextPos;
 	}
 
 	// parser and type checker
 	public static GtSyntaxTree ParseType(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/Object ConstValue = NameSpace.GetSymbol(Token.ParsedText);
 		if(!(ConstValue instanceof GtType)) {
 			return null;  // Not matched
 		}
 		/*local*/GtType ParsedType = (/*cast*/GtType)ConstValue;
 		/*local*/int BacktrackPosition = TokenContext.CurrentPosition;
 		if(ParsedType.IsGenericType()) {
 			if(TokenContext.MatchToken("<")) {  // Generics
 				/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 				while(!TokenContext.MatchToken(">")) {
 					/*local*/GtSyntaxTree ParamTree = TokenContext.ParsePattern(NameSpace, "$Type$", Optional);
 					if(ParamTree == null) {
 						TokenContext.CurrentPosition = BacktrackPosition;
 						return new GtSyntaxTree(Pattern, NameSpace, Token, ParsedType);
 					}
 					TypeList.add(ParamTree.GetParsedType());
 					if(TokenContext.MatchToken(",")) {
 						continue;
 					}
 				}
 				ParsedType = NameSpace.Context.GetGenericType(ParsedType, 0, TypeList, true);
 				BacktrackPosition = TokenContext.CurrentPosition;
 			}
 		}
 		while(TokenContext.MatchToken("[")) {  // Array
 			if(!TokenContext.MatchToken("]")) {
 				TokenContext.CurrentPosition = BacktrackPosition;
 				return new GtSyntaxTree(Pattern, NameSpace, Token, ParsedType);
 			}
 			ParsedType = NameSpace.Context.GetGenericType1(NameSpace.Context.ArrayType, ParsedType, true);
 			BacktrackPosition = TokenContext.CurrentPosition;
 		}
 		return new GtSyntaxTree(Pattern, NameSpace, Token, ParsedType);
 	}
 
 	public static GtSyntaxTree ParseConst(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/Object ConstValue = NameSpace.GetSymbol(Token.ParsedText);
 		if(ConstValue != null) {
 			return new GtSyntaxTree(Pattern, NameSpace, Token, ConstValue);
 		}
 		return null; // Not Matched
 	}
 
 	public static GtNode TypeConst(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ParsedTree.ConstValue), ParsedTree, ParsedTree.ConstValue);
 	}
 
 	public static GtSyntaxTree ParseSymbol(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree TypeTree = TokenContext.ParsePattern(NameSpace, "$Type$", Optional);
 		if(TypeTree != null) {
 			/*local*/GtSyntaxTree DeclTree = TokenContext.ParsePatternAfter(NameSpace, TypeTree, "$FuncDecl$", Optional);
 			if(DeclTree != null) {
 				return DeclTree;
 			}
 			DeclTree = TokenContext.ParsePatternAfter(NameSpace, TypeTree, "$VarDecl$", Optional);
 			if(DeclTree != null) {
 				return DeclTree;
 			}
 			return TypeTree;
 		}
 		/*local*/GtToken Token = TokenContext.Next();
 //		/*local*/Object ConstValue = NameSpace.GetSymbol(Token.ParsedText);
 //		if(ConstValue != null && !(ConstValue instanceof GtType)) {
 //			return new GtSyntaxTree(NameSpace.GetPattern("$Const$"), NameSpace, Token, ConstValue);
 //		}
 		return new GtSyntaxTree(NameSpace.GetPattern("$Variable$"), NameSpace, Token, null);
 	}
 
 	public static GtSyntaxTree ParseVariable(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/char ch = LangDeps.CharAt(Token.ParsedText, 0);
 		if(LangDeps.IsLetter(ch) || ch == '_') {
 			return new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		}
 		return null;
 	}
 
 	public static GtNode TypeVariable(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtVariableInfo VariableInfo = Gamma.LookupDeclaredVariable(Name);
 		if(VariableInfo != null) {
 			return Gamma.Generator.CreateLocalNode(VariableInfo.Type, ParsedTree, VariableInfo.LocalName);
 		}
 		/*local*/Object ConstValue = (/*cast*/Object) Gamma.NameSpace.GetSymbol(Name);
 		if(ConstValue != null) {
 			return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ConstValue), ParsedTree, ConstValue);
 		}
 		GtNode Node = Gamma.Generator.CreateConstNode(Gamma.AnyType, ParsedTree, Name);
 		return Gamma.ReportTypeResult(ParsedTree, Node, ErrorLevel, "undefined name: " + Name);
 	}
 
 	public static GtSyntaxTree ParseVarDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 		if(LeftTree == null) {
 			Tree.SetMatchedPatternAt(VarDeclType, NameSpace, TokenContext, "$Type$", Required);
 		}
 		else {
 			Tree.SetSyntaxTreeAt(VarDeclType, LeftTree);
 		}
 		Tree.SetMatchedPatternAt(VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 		if(Tree.IsEmptyOrError()) {
 			return null;
 		}
 		if(TokenContext.MatchToken("=")) {
 			Tree.SetMatchedPatternAt(VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 		}
 		while(TokenContext.MatchToken(",")) {
 			/*local*/GtSyntaxTree NextTree = new GtSyntaxTree(Pattern, NameSpace, Tree.KeyToken, null);
 			NextTree.SetSyntaxTreeAt(VarDeclType, Tree.GetSyntaxTreeAt(VarDeclType));
 			Tree = GtStatic.LinkTree(Tree, NextTree);
 			Tree.SetMatchedPatternAt(VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 			if(TokenContext.MatchToken("=")) {
 				Tree.SetMatchedPatternAt(VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 			}
 		}
 		return Tree;
 	}
 
 	public static GtNode TypeVarDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtSyntaxTree TypeTree = ParsedTree.GetSyntaxTreeAt(VarDeclType);
 		/*local*/GtSyntaxTree NameTree = ParsedTree.GetSyntaxTreeAt(VarDeclName);
 		/*local*/GtSyntaxTree ValueTree = ParsedTree.GetSyntaxTreeAt(VarDeclValue);
 		/*local*/GtType DeclType = TypeTree.GetParsedType();
 		/*local*/String VariableName = NameTree.KeyToken.ParsedText;
 		Gamma.AppendDeclaredVariable(DeclType, VariableName);
 		/*local*/GtNode VariableNode = Gamma.TypeCheck(NameTree, DeclType, DefaultTypeCheckPolicy);
 		if(VariableNode.IsError()) {
 			return VariableNode;
 		}
 		/*local*/GtNode InitValueNode = null;
 		if(ValueTree == null){
 			InitValueNode = Gamma.DefaultValueConstNode(ParsedTree, DeclType);
 		}else{
 			InitValueNode = Gamma.TypeCheck(ValueTree, DeclType, DefaultTypeCheckPolicy);
 		}
 		/*local*/GtNode BlockNode = Gamma.TypeBlock(ParsedTree.NextTree, ContextType);
 		ParsedTree.NextTree = null;
 		return Gamma.Generator.CreateLetNode(DeclType, ParsedTree, DeclType, ((/*cast*/LocalNode)VariableNode).LocalName, InitValueNode, BlockNode);
 	}
 
 	// Parse And Type
 	public static GtSyntaxTree ParseIntegerLiteral(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		return new GtSyntaxTree(Pattern, NameSpace, Token, LangDeps.ParseInt(Token.ParsedText));
 	}
 
 	public static GtSyntaxTree ParseStringLiteral(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next(); /* this must be \" and we should eat it*/
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, Token.ParsedText);
 		return NewTree;
 	}
 
 	public static GtSyntaxTree ParseExpression(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return GtStatic.ParseExpression(NameSpace, TokenContext);
 	}
 
 	public static GtSyntaxTree ParseUnary(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		Tree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$Expression$", Required);
 		return Tree;
 	}
 
 	public static GtNode TypeUnary(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode ExprNode  = ParsedTree.TypeCheckNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateUnaryNode(Gamma.AnyType, ParsedTree, null/*Method*/, ExprNode);
 	}
 
 	public static GtSyntaxTree ParseBinary(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree RightTree = GtStatic.ParseExpression(NameSpace, TokenContext);
 		if(GtStatic.IsEmptyOrError(RightTree)) {
 			return RightTree;
 		}
 		if(RightTree.Pattern.IsBinaryOperator()) {
 			if(Pattern.IsLeftJoin(RightTree.Pattern)) {
 				/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 				NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 				NewTree.SetSyntaxTreeAt(RightHandTerm, RightTree.GetSyntaxTreeAt(LeftHandTerm));
 				RightTree.SetSyntaxTreeAt(LeftHandTerm, NewTree);
 				return RightTree;
 			}
 		}
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 		NewTree.SetSyntaxTreeAt(RightHandTerm, RightTree);
 		if(RightTree.NextTree != null) {
 			GtStatic.LinkTree(NewTree, RightTree.NextTree);
 			RightTree.NextTree = null;
 		}
 		return NewTree;
 	}
 
 	public static GtNode TypeBinary(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Operator = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode LeftNode  = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(!LeftNode.IsError() && !RightNode.IsError()) {
 			/*local*/GtType BaseType = LeftNode.Type;
 			while(BaseType != null) {
 				/*local*/GtMethod Method = Gamma.Context.GetListedMethod(BaseType, Operator, 1, false);
 				while(Method != null) {
 					if(Method.GetFuncParamType(1).Accept(RightNode.Type)) {
 						return Gamma.Generator.CreateBinaryNode(Method.GetReturnType(), ParsedTree, Method, LeftNode, RightNode);
 					}
 					Method = Method.ListedMethods;
 				}
 				BaseType = BaseType.SearchSuperMethodClass;
 			}
 			Gamma.Context.ReportError(WarningLevel, ParsedTree.KeyToken, "undefined operator: " + Operator + " of " + LeftNode.Type);
 		}
 		return Gamma.Generator.CreateBinaryNode(ContextType, ParsedTree, null, LeftNode, RightNode);
 	}
 
 	public static GtSyntaxTree ParseGetter(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		TokenContext.MatchToken(".");
 		/*local*/GtToken Token = TokenContext.Next();
 		if(Token.IsNameSymbol()) {
 			/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 			NewTree.AppendParsedTree(LeftTree);
 			return NewTree;
 		}
 		return TokenContext.ReportExpectedToken("field name");
 	}
 
 	public static GtNode TypeGetter(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode ObjectNode = ParsedTree.TypeCheckNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ObjectNode.IsError()) {
 			return ObjectNode;
 		}
 		// To start, check class const such as Math.Pi if base is a type value
 		if(ObjectNode instanceof ConstNode && ObjectNode.Type == Gamma.Context.TypeType) {
 			/*local*/GtType ObjectType = (/*cast*/GtType)((/*cast*/ConstNode)ObjectNode).ConstValue;
 			/*local*/Object ConstValue = ObjectType.GetClassSymbol(Name, true);
 			if(ConstValue != null) {
 				return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ConstValue), ParsedTree, ConstValue);
 			}
 		}
 		/*local*/GtMethod Method = Gamma.Context.GetGetterMethod(ObjectNode.Type, Name, true);
 		/*local*/GtType ReturnType = (Method != null) ? Method.GetReturnType() : Gamma.AnyType;
 		/*local*/GtNode Node = Gamma.Generator.CreateGetterNode(ReturnType, ParsedTree, Method, ObjectNode);
 		if(Method == null) {
 			if(!ObjectNode.Type.IsDynamic() && ContextType != Gamma.FuncType) {
 				return Gamma.ReportTypeResult(ParsedTree, Node, ErrorLevel, "undefined name " + Name + " of " + ObjectNode.Type);
 			}
 		}
 		return Node;
 	}
 
 	// PatternName: "("
 	public static GtSyntaxTree ParseGroup(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/GtSyntaxTree GroupTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("("), null);
 		/*local*/GtSyntaxTree Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 		GroupTree.AppendParsedTree(Tree);
 		if(!TokenContext.MatchToken(")")) {
 			GroupTree = TokenContext.ReportExpectedToken(")");
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return GroupTree;
 	}
 
 	public static GtNode TypeGroup(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return ParsedTree.TypeCheckNodeAt(UnaryTerm, Gamma, ContextType, DefaultTypeCheckPolicy);
 	}
 
 	// PatternName: "(" "to" $Type$ ")"
 	public static GtSyntaxTree ParseCast(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/GtToken FirstToken = TokenContext.Next(); // skip the first token
 		/*local*/GtSyntaxTree CastTree = null;
 		if(TokenContext.MatchToken("to")) {
 			CastTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("to"), null);
 		}
 		else if(TokenContext.MatchToken("as")) {
 			CastTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("as"), null);
 		}
 		else {
 			CastTree = new GtSyntaxTree(Pattern, NameSpace, FirstToken, null);
 		}
 		CastTree.SetMatchedPatternAt(LeftHandTerm, NameSpace, TokenContext, "$Type$", Required);
 		CastTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		TokenContext.ParseFlag = ParseFlag;
 		CastTree.SetMatchedPatternAt(RightHandTerm, NameSpace, TokenContext, "$Expression$", Required);
 		return CastTree;
 	}
 
 	public static GtNode TypeCast(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType CastType = ParsedTree.GetSyntaxTreeAt(LeftHandTerm).GetParsedType();
 		/*local*/int TypeCheckPolicy = CastPolicy;
 		return ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, CastType, TypeCheckPolicy);
 	}
 
 	public static GtSyntaxTree ParseApply(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/GtSyntaxTree FuncTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("("), null);
 		FuncTree.AppendParsedTree(LeftTree);
 		if(!TokenContext.MatchToken(")")) {
 			while(!FuncTree.IsEmptyOrError()) {
 				/*local*/GtSyntaxTree Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 				FuncTree.AppendParsedTree(Tree);
 				if(TokenContext.MatchToken(")")) break;
 				FuncTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 			}
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return FuncTree;
 	}
 
 	public static GtNode TypeApply(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
		/*local*/GtNode FuncNode = ParsedTree.TypeCheckNodeAt(0, Gamma, Gamma.FuncType, DefaultTypeCheckPolicy);
 		/*local*/String MethodName = FuncNode.Token.ParsedText;
 		/*local*/GtType BaseType = null;
 		/*local*/ArrayList<GtNode> NodeList = new ArrayList<GtNode>();
 		/*local*/int ParamIndex = 1;
 		/*local*/int ParamSize = ListSize(ParsedTree.TreeList) - 1;
 		if(FuncNode.IsError()) {
 			return FuncNode;
 		}
 		if(FuncNode instanceof GetterNode) {
 			/*local*/GtNode BaseNode = ((/*cast*/GetterNode)FuncNode).Expr;
 			NodeList.add(BaseNode);
 			BaseType = FuncNode.Type;
 		}
 		else if(ParamSize > 0) {
 			/*local*/GtNode BaseNode = ParsedTree.TypeCheckNodeAt(1, Gamma, Gamma.AnyType, DefaultTypeCheckPolicy);
 			NodeList.add(BaseNode);
 			ParamIndex = 2;
 			BaseType = BaseNode.Type;
 		}
 		else {
 			BaseType = Gamma.VoidType;
 		}
 		return DScriptGrammar.TypeFuncParam(Gamma, ParsedTree, MethodName, BaseType, NodeList, ParamIndex, ParamSize);
 	}
 
 	private static GtNode TypeFuncParam(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, String MethodName, GtType BaseType, ArrayList<GtNode> NodeList, int ParamIndex, int ParamSize) {
 		/*local*/GtMethod Method = Gamma.Context.GetListedMethod(BaseType, MethodName, ParamSize - 1, true);
 		/*local*/GtType ReturnType = Gamma.AnyType;
 		if(Method == null) {
 			if(!BaseType.IsDynamic()) {
 				/*local*/GtNode TypeError = Gamma.CreateErrorNode2(ParsedTree, "undefined function " + MethodName + " of " + BaseType);
 				if(Gamma.IsStrictMode()) {
 					return TypeError;
 				}
 			}
 			else {
 				while(ParamIndex < ListSize(ParsedTree.TreeList)) {
 					/*local*/GtNode Node = ParsedTree.TypeCheckNodeAt(ParamIndex, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 					if(Node.IsError()) {
 						return Node;
 					}
 					NodeList.add(Node);
 					ParamIndex = ParamIndex + 1;
 				}
 			}
 		}
 		else { //if(Method != null) {
 			if(Method.ListedMethods == null) {
 				DebugP("Contextual Typing");
 				while(ParamIndex < ListSize(ParsedTree.TreeList)) {
 					/*local*/GtNode Node = ParsedTree.TypeCheckNodeAt(ParamIndex, Gamma, Method.Types[ParamIndex], DefaultTypeCheckPolicy);
 					if(Node.IsError()) {
 						return Node;
 					}
 					NodeList.add(Node);
 					ParamIndex = ParamIndex + 1;
 				}
 				ReturnType = Method.GetReturnType();
 			}
 			else {
 				while(ParamIndex < ListSize(ParsedTree.TreeList)) {
 					/*local*/GtNode Node = ParsedTree.TypeCheckNodeAt(ParamIndex, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 					if(Node.IsError()) {
 						return Node;
 					}
 					NodeList.add(Node);
 					ParamIndex = ParamIndex + 1;
 				}
 				Method = DScriptGrammar.LookupOverloadedMethod(Gamma, Method, NodeList);
 				if(Method == null) {
 					/*local*/GtNode TypeError = Gamma.CreateErrorNode2(ParsedTree, "mismatched method " + MethodName + " of " + BaseType);
 					if(Gamma.IsStrictMode()) {
 						return TypeError;
 					}
 				}
 				ReturnType = Method.GetReturnType();
 			}
 		}
 		/*local*/GtNode Node = Gamma.Generator.CreateApplyNode(ReturnType, ParsedTree, Method);
 		/*local*/int i = 0;
 		while(i < NodeList.size()) {
 			Node.Append(NodeList.get(i));
 			i = i + 1;
 		}
 		return Node;
 	}
 
 	private static boolean ExactlyMatchMethod(GtMethod Method, ArrayList<GtNode> NodeList) {
 		/*local*/int p = 1;
 		while(p < ListSize(NodeList)) {
 			/*local*/GtNode ParamNode = NodeList.get(p);
 			if(Method.Types[p+1] != ParamNode.Type) {
 				return false;
 			}
 			p = p + 1;
 		}
 		return true;
 	}
 
 	private static boolean AcceptablyMatchMethod(GtTypeEnv Gamma, GtMethod Method, ArrayList<GtNode> NodeList) {
 		/*local*/int p = 1;
 		while(p < ListSize(NodeList)) {
 			/*local*/GtNode ParamNode = NodeList.get(p);
 			if(!Method.Types[p+1].Accept(ParamNode.Type)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private static GtMethod LookupOverloadedMethod(GtTypeEnv Gamma, GtMethod Method, ArrayList<GtNode> NodeList) {
 		/*local*/GtMethod StartMethod = Method;
 		/*local*/GtType BaseType = Method.GetRecvType();
 		/*local*/String MethodName = Method.MangledName;
 		/*local*/int ParamSize = Method.GetMethodParamSize();
 		while(Method != null) {
 			if(DScriptGrammar.ExactlyMatchMethod(Method, NodeList)) {
 				return Method;
 			}
 			Method = Method.ListedMethods;
 			if(Method == null) {
 				BaseType = BaseType.SearchSuperMethodClass;
 				if(BaseType == null) {
 					break;
 				}
 				Method = Gamma.Context.GetListedMethod(BaseType, MethodName, ParamSize, false);
 			}
 		}
 		Method = StartMethod;
 		BaseType = Method.GetRecvType();
 		while(Method != null) {
 			if(DScriptGrammar.AcceptablyMatchMethod(Gamma, Method, NodeList)) {
 				return Method;
 			}
 			Method = Method.ListedMethods;
 			if(Method == null) {
 				BaseType = BaseType.SearchSuperMethodClass;
 				if(BaseType == null) {
 					break;
 				}
 				Method = Gamma.Context.GetListedMethod(BaseType, MethodName, ParamSize, false);
 			}
 		}
 		return null;
 	}
 
 	public static GtNode TypeAnd(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateAndNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static GtNode TypeOr(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateOrNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static GtNode TypeAssign(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, LeftNode.Type, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateAssignNode(LeftNode.Type, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static GtSyntaxTree ParseEmpty(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetBeforeToken(), null);
 	}
 
 	public static GtNode TypeEmpty(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 	}
 
 	public static GtSyntaxTree ParseBlock(GtNameSpace ParentNameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		if(TokenContext.MatchToken("{")) {
 			/*local*/GtSyntaxTree PrevTree = null;
 			/*local*/GtNameSpace NameSpace = new GtNameSpace(ParentNameSpace.Context, ParentNameSpace);
 			while(TokenContext.SkipEmptyStatement()) {
 				if(TokenContext.MatchToken("}")) {
 					break;
 				}
 				/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 				/*local*/GtSyntaxTree CurrentTree = GtStatic.ParseExpression(NameSpace, TokenContext);
 				if(GtStatic.IsEmptyOrError(CurrentTree)) {
 					return CurrentTree;
 				}
 				CurrentTree.SetAnnotation(Annotation);
 				PrevTree = GtStatic.LinkTree(PrevTree, CurrentTree);
 			}
 			if(PrevTree == null) {
 				return TokenContext.ParsePattern(NameSpace, "$Empty$", Required);
 			}
 			return GtStatic.TreeHead(PrevTree);
 		}
 		return null;
 	}
 
 	public static GtSyntaxTree ParseStatement(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree StmtTree = TokenContext.ParsePattern(NameSpace, "$Block$", Optional);
 		if(StmtTree == null) {
 			StmtTree = GtStatic.ParseExpression(NameSpace, TokenContext);
 		}
 		if(StmtTree == null) {
 			StmtTree = TokenContext.ParsePattern(NameSpace, "$Empty$", Required);
 		}
 		return StmtTree;
 	}
 
 	public static GtNode TypeBlock(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.TypeBlock(ParsedTree, ContextType);
 	}
 
 	// If Statement
 	public static GtSyntaxTree ParseIf(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("if");
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		NewTree.SetMatchedPatternAt(IfCond, NameSpace, TokenContext, "$Expression$", Required);
 		NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		NewTree.SetMatchedPatternAt(IfThen, NameSpace, TokenContext, "$Statement$", Required);
 		if(TokenContext.MatchToken("else")) {
 			NewTree.SetMatchedPatternAt(IfElse, NameSpace, TokenContext, "$Statement$", Required);
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return NewTree;
 	}
 
 	public static GtNode TypeIf(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckNodeAt(IfCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode ThenNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(IfThen), ContextType);
 		/*local*/GtNode ElseNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(IfElse), ThenNode.Type);
 		return Gamma.Generator.CreateIfNode(ThenNode.Type, ParsedTree, CondNode, ThenNode, ElseNode);
 	}
 
 	// While Statement
 	public static GtSyntaxTree ParseWhile(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree WhileTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("while"), null);
 		WhileTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		WhileTree.SetMatchedPatternAt(WhileCond, NameSpace, TokenContext, "$Expression$", Required);
 		WhileTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		WhileTree.SetMatchedPatternAt(WhileBody, NameSpace, TokenContext, "$Block$", Required);
 		return WhileTree;
 	}
 
 	public static GtNode TypeWhile(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckNodeAt(WhileCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode BodyNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(WhileBody), ContextType);
 		return Gamma.Generator.CreateWhileNode(BodyNode.Type, ParsedTree, CondNode, BodyNode);
 	}
 
 	// Break/Continue Statement
 	public static GtSyntaxTree ParseBreak(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("break");
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		return NewTree;
 	}
 
 	public static GtNode TypeBreak(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.Generator.CreateBreakNode(Gamma.VoidType, ParsedTree, null, "");
 	}
 
 	public static GtSyntaxTree ParseContinue(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("continue");
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		return NewTree;
 	}
 
 	public static GtNode TypeContinue(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.Generator.CreateContinueNode(Gamma.VoidType, ParsedTree, null, "");
 	}
 
 	// Return Statement
 	public static GtSyntaxTree ParseReturn(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ReturnTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("return"), null);
 		ReturnTree.SetMatchedPatternAt(ReturnExpr, NameSpace, TokenContext, "$Expression$", Optional);
 		return ReturnTree;
 	}
 
 	public static GtNode TypeReturn(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		if(Gamma.IsTopLevel()) {
 			return Gamma.UnsupportedTopLevelError(ParsedTree);
 		}
 		/*local*/GtType ReturnType = Gamma.Method.GetReturnType();
 		/*local*/GtNode Expr = ParsedTree.TypeCheckNodeAt(ReturnExpr, Gamma, ReturnType, DefaultTypeCheckPolicy);
 		if(ReturnType == Gamma.VoidType){
 			return Gamma.Generator.CreateReturnNode(Expr.Type, ParsedTree, null);
 		}
 		return Gamma.Generator.CreateReturnNode(Expr.Type, ParsedTree, Expr);
 	}
 
 	// try
 	public static GtSyntaxTree ParseTry(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree TryTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("try"), null);
 		TryTree.SetMatchedPatternAt(TryBody, NameSpace, TokenContext, "$Block$", Required);
 		if(TokenContext.MatchToken("catch")) {
 			TryTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 			TryTree.SetMatchedPatternAt(CatchVariable, NameSpace, TokenContext, "$VarDecl$", Required);
 			TryTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 			TryTree.SetMatchedPatternAt(CatchBody, NameSpace, TokenContext, "$Block$", Required);
 		}
 		if(TokenContext.MatchToken("finally")) {
 			TryTree.SetMatchedPatternAt(FinallyBody, NameSpace, TokenContext, "$Block$", Required);
 		}
 		return TryTree;
 	}
 
 	public static GtNode TypeTry(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType FaultType = ContextType; // FIXME Gamma.FaultType;
 		/*local*/GtNode TryNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(TryBody), ContextType);
 		/*local*/GtNode CatchExpr = ParsedTree.TypeCheckNodeAt(CatchVariable, Gamma, FaultType, DefaultTypeCheckPolicy);
 		/*local*/GtNode CatchNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(CatchBody), ContextType);
 		/*local*/GtNode FinallyNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(FinallyBody), ContextType);
 		return Gamma.Generator.CreateTryNode(TryNode.Type, ParsedTree, TryNode, CatchExpr, CatchNode, FinallyNode);
 	}
 
 	// throw $Expr$
 	public static GtSyntaxTree ParseThrow(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ThrowTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("throw"), null);
 		ThrowTree.SetMatchedPatternAt(ReturnExpr, NameSpace, TokenContext, "$Expression$", Required);
 		return ThrowTree;
 	}
 
 	public static GtNode TypeThrow(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType FaultType = ContextType; // FIXME Gamma.FaultType;
 		/*local*/GtNode ExprNode = ParsedTree.TypeCheckNodeAt(ReturnExpr, Gamma, FaultType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateThrowNode(ExprNode.Type, ParsedTree, ExprNode);
 	}
 
 	// new $Type ( $Expr$ [, $Expr$] )
 	public static GtSyntaxTree ParseNew(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("new"), null);
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		NewTree.SetMatchedPatternAt(CallExpressionOffset, NameSpace, TokenContext, "$Type$", Required);
 		TokenContext.MatchToken("(");
 		if(!TokenContext.MatchToken(")")) {
 			while(!NewTree.IsEmptyOrError()) {
 				/*local*/GtSyntaxTree Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 				NewTree.AppendParsedTree(Tree);
 				if(TokenContext.MatchToken(")")) {
 					break;
 				}
 				NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 			}
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return NewTree;
 	}
 
 	public static GtNode TypeNew(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		// new $Type$($Params$) => constructor(new $Type$, $Params$)
 		/*local*/GtType ReturnType = ParsedTree.GetSyntaxTreeAt(CallExpressionOffset).GetParsedType();
 		/*local*/String MethodName = "constructor";
 		/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 		/*local*/int ParamIndex = 1;
 		/*local*/int ParamSize = ListSize(ParsedTree.TreeList);
 		ParamList.add(Gamma.Generator.CreateNewNode(ReturnType, ParsedTree));
 		return DScriptGrammar.TypeFuncParam(Gamma, ParsedTree, MethodName, ReturnType, ParamList, ParamIndex, ParamSize);
 	}
 
 	// switch
 	public static GtSyntaxTree ParseEnum(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/String EnumTypeName = null;
 		/*local*/GtType NewEnumType = null;
 		/*local*/GtMap VocabMap = new GtMap();
 		/*local*/GtSyntaxTree EnumTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("enum"), null);
 		EnumTree.SetMatchedPatternAt(EnumNameTreeIndex, NameSpace, TokenContext, "$FuncName$", Required);  // $ClassName$ is better
 		if(!EnumTree.IsEmptyOrError()) {
 			EnumTypeName = EnumTree.GetSyntaxTreeAt(EnumNameTreeIndex).KeyToken.ParsedText;
 			if(NameSpace.GetSymbol(EnumTypeName) != null) {
 				NameSpace.Context.ReportError(ErrorLevel, EnumTree.KeyToken, "already defined name: " + EnumTypeName);
 			}
 			NewEnumType = new GtType(NameSpace.Context, EnumClass, EnumTypeName, null, VocabMap);
 		}
 		EnumTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "{", Required);
 		/*local*/int EnumValue = 0;
 		while(!EnumTree.IsEmptyOrError()) {
 			TokenContext.SkipIndent();
 			if(TokenContext.MatchToken(",")) {
 				continue;
 			}
 			if(TokenContext.MatchToken("}")) {
 				break;
 			}
 			/*local*/GtToken Token = TokenContext.Next();
 			if(LangDeps.IsLetter(LangDeps.CharAt(Token.ParsedText, 0))) {
 				if(VocabMap.get(Token.ParsedText) != null) {
 					NameSpace.Context.ReportError(WarningLevel, Token, "already defined name: " + Token.ParsedText);
 					continue;
 				}
 				VocabMap.put(Token.ParsedText, new GreenTeaEnum(NewEnumType, EnumValue, Token.ParsedText));
 				EnumValue += 1;
 				continue;
 			}
 		}
 		if(!EnumTree.IsEmptyOrError()) {
 			NameSpace.DefineClassSymbol(NewEnumType);
 			EnumTree.ConstValue = NewEnumType;
 		}
 		return EnumTree;
 	}
 
 	public static GtNode TypeEnum(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/Object EnumType = ParsedTree.ConstValue;
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(EnumType), ParsedTree, EnumType);
 	}
 
 	public static GtSyntaxTree ParseSwitch(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree SwitchTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("switch"), null);
 		SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		SwitchTree.SetMatchedPatternAt(CatchVariable, NameSpace, TokenContext, "$Expression$", Required);
 		SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "{", Required);
 		while(!SwitchTree.IsEmptyOrError() && !TokenContext.MatchToken("}")) {
 			if(TokenContext.MatchToken("case")) {
 				SwitchTree.SetMatchedPatternAt(CatchVariable, NameSpace, TokenContext, "$Expression$", Required);
 				SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 				SwitchTree.SetMatchedPatternAt(TryBody, NameSpace, TokenContext, "$CaseBlock$", Required);
 				continue;
 			}
 			SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "default", Required);
 			SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 			SwitchTree.SetMatchedPatternAt(TryBody, NameSpace, TokenContext, "$CaseBlock$", Required);
 		}
 		return SwitchTree;
 	}
 
 	public static GtNode TypeSwitch(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return null;
 	}
 
 	// const decl
 	public static GtSyntaxTree ParseConstDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ConstDeclTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("const"), null);
 		/*local*/GtSyntaxTree ClassNameTree = TokenContext.ParsePattern(NameSpace, "$Type$", Optional);
 		/*local*/GtType ConstClass = null;
 		if(ClassNameTree != null) {
 			ConstDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ".", Required);
 			if(!ConstDeclTree.IsEmptyOrError()) {
 				ConstDeclTree.SetSyntaxTreeAt(ConstDeclClassIndex, ClassNameTree);
 				ConstClass = ConstDeclTree.GetParsedType();
 			}
 		}
 		ConstDeclTree.SetMatchedPatternAt(ConstDeclNameIndex, NameSpace, TokenContext, "$Variable$", Required);
 		ConstDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "=", Required);
 		ConstDeclTree.SetMatchedPatternAt(ConstDeclValueIndex, NameSpace, TokenContext, "$Expression$", Required);
 
 		if(!ConstDeclTree.IsEmptyOrError()) {
 			/*local*/String ConstName = ConstDeclTree.GetSyntaxTreeAt(ConstDeclNameIndex).KeyToken.ParsedText;
 			/*local*/Object ConstValue = null;
 			if(ConstDeclTree.GetSyntaxTreeAt(ConstDeclValueIndex).Pattern.PatternName.equals("$Const$")) {
 				ConstValue = ConstDeclTree.GetSyntaxTreeAt(ConstDeclValueIndex).ConstValue;
 			}
 			if(ConstValue == null) {
 
 			}
 			if(ConstClass != null) {
 				if(ConstClass.GetClassSymbol(ConstName, false) != null) {
 
 				}
 				ConstClass.SetClassSymbol(ConstName, ConstValue);
 			}
 			else {
 				if(NameSpace.GetSymbol(ConstName) != null) {
 
 				}
 				NameSpace.DefineSymbol(ConstName, ConstValue);
 			}
 		}
 		return ConstDeclTree;
 	}
 
 	public static GtNode TypeConstDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtSyntaxTree NameTree = ParsedTree.GetSyntaxTreeAt(ConstDeclNameIndex);
 		/*local*/GtSyntaxTree ValueTree = ParsedTree.GetSyntaxTreeAt(ConstDeclValueIndex);
 		/*local*/String VariableName = NameTree.KeyToken.ParsedText;
 		/*local*/GtNode ValueNode = Gamma.TypeCheck(ValueTree, Gamma.AnyType, DefaultTypeCheckPolicy);
 		if(!(ValueNode instanceof ConstNode)) {
 			return Gamma.CreateErrorNode2(ParsedTree, "definition of variable " + VariableName + " is not constant");
 		}
 		/*local*/ConstNode CNode = (/*cast*/ConstNode) ValueNode;
 		Gamma.NameSpace.DefineSymbol(VariableName, CNode.ConstValue);
 		return Gamma.Generator.CreateEmptyNode(ContextType);
 	}
 
 	// FuncDecl
 	public static GtSyntaxTree ParseFuncName(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		if(Token != GtTokenContext.NullToken) {
 			/*local*/char ch = LangDeps.CharAt(Token.ParsedText, 0);
 			if(ch != '.') {
 				return new GtSyntaxTree(Pattern, NameSpace, Token, Token.ParsedText);
 			}
 		}
 		return null;
 	}
 
 	public static GtSyntaxTree ParseFuncDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 		if(LeftTree == null) {
 			Tree.SetMatchedPatternAt(FuncDeclReturnType, NameSpace, TokenContext, "$Type$", Required);
 		}
 		else {
 			Tree.SetSyntaxTreeAt(FuncDeclReturnType, LeftTree);
 		}
 		Tree.SetMatchedPatternAt(FuncDeclName, NameSpace, TokenContext, "$FuncName$", Required);
 		if(TokenContext.MatchToken("(")) {
 			/*local*/int ParseFlag = TokenContext.SetBackTrack(false);  // disabled
 			/*local*/int ParamBase = FuncDeclParam;
 			while(!Tree.IsEmptyOrError() && !TokenContext.MatchToken(")")) {
 				if(ParamBase != FuncDeclParam) {
 					Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 				}
 				Tree.SetMatchedPatternAt(ParamBase + VarDeclType, NameSpace, TokenContext, "$Type$", Required);
 				Tree.SetMatchedPatternAt(ParamBase + VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 				if(TokenContext.MatchToken("=")) {
 					Tree.SetMatchedPatternAt(ParamBase + VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 				}
 				ParamBase += 3;
 			}
 			TokenContext.SkipIndent();
 			if(TokenContext.MatchToken("as")) {  // this is little ad hoc
 				/*local*/GtToken Token = TokenContext.Next();
 				Tree.ConstValue = Token.ParsedText;
 			}
 			else {
 				Tree.SetMatchedPatternAt(FuncDeclBlock, NameSpace, TokenContext, "$Block$", Optional);
 			}
 			TokenContext.ParseFlag = ParseFlag;
 			return Tree;
 		}
 		return null;
 	}
 
 	public static GtNode TypeFuncDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/int MethodFlag = Gamma.Generator.ParseMethodFlag(0, ParsedTree);
 		Gamma = new GtTypeEnv(ParsedTree.NameSpace);  // creation of new type environment
 		/*local*/String MethodName = (/*cast*/String)ParsedTree.GetSyntaxTreeAt(FuncDeclName).ConstValue;
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		/*local*/GtType ReturnType = ParsedTree.GetSyntaxTreeAt(FuncDeclReturnType).GetParsedType();
 		TypeList.add(ReturnType);
 		/*local*/ArrayList<String> ParamNameList = new ArrayList<String>();
 		/*local*/int ParamBase = FuncDeclParam;
 		/*local*/int i = 0;
 		while(ParamBase < ParsedTree.TreeList.size()) {
 			/*local*/GtType ParamType = ParsedTree.GetSyntaxTreeAt(ParamBase).GetParsedType();
 			/*local*/String ParamName = ParsedTree.GetSyntaxTreeAt(ParamBase+1).KeyToken.ParsedText;
 			TypeList.add(ParamType);
 			ParamNameList.add(ParamName + i);
 			Gamma.AppendDeclaredVariable(ParamType, ParamName);
 			ParamBase += 3;
 			i = i + 1;
 		}
 
 		/*local*/GtMethod Method = null;
 		/*local*/String NativeMacro =  (/*cast*/String)ParsedTree.ConstValue;
 		if(NativeMacro == null && !ParsedTree.HasNodeAt(FuncDeclBlock)) {
 			MethodFlag |= AbstractMethod;
 		}
 		if(MethodName.equals("converter")) {
 			Method = DScriptGrammar.CreateConverterMethod(Gamma, ParsedTree, MethodFlag, TypeList);
 		}
 		else {
 			Method = DScriptGrammar.CreateMethod(Gamma, ParsedTree, MethodFlag, MethodName, TypeList, NativeMacro);
 		}
 		if(Method != null && NativeMacro == null && ParsedTree.HasNodeAt(FuncDeclBlock)) {
 			/*local*/GtNode BodyNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(FuncDeclBlock), ReturnType);
 			Gamma.Generator.GenerateMethod(Method, ParamNameList, BodyNode);
 		}
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 	}
 
 	private static GtMethod CreateConverterMethod(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, int MethodFlag, ArrayList<GtType> TypeList) {
 		/*local*/GtType ToType = TypeList.get(0);
 		/*local*/GtType FromType = TypeList.get(1);
 		/*local*/GtMethod Method = Gamma.Context.GetCastMethod(FromType, ToType, false);
 		if(Method != null) {
 			Gamma.Context.ReportError(ErrorLevel, ParsedTree.KeyToken, "already defined: " + FromType + " to " + ToType);
 			return null;
 		}
 		Method = Gamma.Generator.CreateMethod(MethodFlag, "to" + ToType.ShortClassName, 0, TypeList, (String)ParsedTree.ConstValue);
 		Gamma.Context.DefineConverterMethod(Method);
 		return Method;
 	}
 
 	private static GtMethod CreateMethod(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, int MethodFlag, String MethodName, ArrayList<GtType> TypeList, String NativeMacro) {
 		/*local*/GtType RecvType = Gamma.VoidType;
 		if(TypeList.size() > 1) {
 			RecvType = TypeList.get(1);
 		}
 		/*local*/GtMethod Method = Gamma.Context.GetMethod(RecvType, MethodName, 2, TypeList, true);
 		if(Method != null) {
 			if(Method.GetRecvType() != RecvType) {
 				if(!Method.Is(VirtualMethod)) {
 					// not virtual method
 					return null;
 				}
 				Method = null;
 			}
 			else {
 				if(!Method.Is(AbstractMethod)) {
 					// not override
 					return null;
 				}
 				if(GtStatic.IsFlag(MethodFlag, AbstractMethod)) {
 					// do nothing
 					return null;
 				}
 			}
 		}
 		if(Method == null) {
 			Method = Gamma.Generator.CreateMethod(MethodFlag, MethodName, 0, TypeList, NativeMacro);
 		}
 		Gamma.DefineMethod(Method);
 		return Method;
 	}
 
 	// ClassDecl
 	public static GtSyntaxTree ParseClassDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ClassDeclTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 		// "class" $Symbol$ ["extends" $Type$]
 		TokenContext.MatchToken("class");
 		/*local*/GtSyntaxTree ClassNameTree = TokenContext.ParsePattern(NameSpace, "$Symbol$", Required);
 		ClassDeclTree.SetSyntaxTreeAt(ClassNameOffset, ClassNameTree);
 		if(TokenContext.MatchToken("extends")) {
 			ClassDeclTree.SetMatchedPatternAt(ClassParentNameOffset, NameSpace, TokenContext, "$Type$", Required);
 		}
 
 		// define new class
 		/*local*/String ClassName = ClassNameTree.KeyToken.ParsedText;
 		/*local*/GtSyntaxTree SuperClassTree = ClassDeclTree.GetSyntaxTreeAt(ClassParentNameOffset);
 		/*local*/GtType SuperType = NameSpace.Context.StructType;
 		if(SuperClassTree != null) {
 			SuperType = SuperClassTree.GetParsedType();
 		}
 		/*local*/int ClassFlag = 0; //Gamma.Generator.ParseMethodFlag(0, ParsedTree);
 		/*local*/GtType NewType = SuperType.CreateSubType(ClassFlag, ClassName, null, null);
 		/*local*/GreenTeaTopObject DefaultObject = new GreenTeaTopObject(NewType);
 		NewType.DefaultNullValue = DefaultObject;
 
 		NameSpace.DefineClassSymbol(NewType);
 		ClassDeclTree.ConstValue = NewType;
 
 		/*local*/int ParseFlag = TokenContext.SetBackTrack(false) | SkipIndentParseFlag;
 		TokenContext.ParseFlag = ParseFlag;
 		if(TokenContext.MatchToken("{")) {
 			/*local*/int i = ClassBlockOffset;
 			while(!ClassDeclTree.IsEmptyOrError() && !TokenContext.MatchToken("}")) {
 				/*local*/GtSyntaxTree FuncDecl = TokenContext.ParsePattern(NameSpace, "$FuncDecl$", Optional);
 				if(FuncDecl != null) {
 					ClassDeclTree.SetSyntaxTreeAt(i, FuncDecl);
 					TokenContext.MatchToken(";");
 					i = i + 1;
 				}
 				/*local*/GtSyntaxTree VarDecl = TokenContext.ParsePattern(NameSpace, "$VarDecl$", Optional);
 				if(VarDecl != null) {
 					ClassDeclTree.SetSyntaxTreeAt(i, VarDecl);
 					TokenContext.MatchToken(";");
 					i = i + 1;
 				}
 				/*local*/GtSyntaxTree InitDecl = TokenContext.ParsePatternAfter(NameSpace, ClassNameTree, "constructor", Optional);
 				if(InitDecl != null) {
 					ClassDeclTree.SetSyntaxTreeAt(i, InitDecl);
 					if(InitDecl.HasNodeAt(FuncDeclBlock)) {
 						/*local*/GtSyntaxTree FuncBody = InitDecl.GetSyntaxTreeAt(FuncDeclBlock);
 						/*local*/GtSyntaxTree TailTree = FuncBody;
 						while(TailTree.NextTree != null) {
 							TailTree = TailTree.NextTree;
 						}
 						/*local*/GtSyntaxTree ThisTree = new GtSyntaxTree(NameSpace.GetPattern("$Variable$"), NameSpace, new GtToken("this", 0), null);
 						/*local*/GtSyntaxTree ReturnTree = new GtSyntaxTree(NameSpace.GetPattern("return"), NameSpace, new GtToken("return", 0), null);
 						ReturnTree.SetSyntaxTreeAt(ReturnExpr, ThisTree);
 						GtStatic.LinkTree(TailTree, ReturnTree);
 
 					}
 					i = i + 1;
 				}
 			}
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return ClassDeclTree;
 	}
 
 	public static GtNode TypeClassDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtSyntaxTree ClassNameTree = ParsedTree.GetSyntaxTreeAt(ClassNameOffset);
 		/*local*/GtType NewType = ParsedTree.GetParsedType();
 		/*local*/int FieldOffset = ClassBlockOffset;
 		Gamma = new GtTypeEnv(ParsedTree.NameSpace);  // creation of new type environment
 		Gamma.AppendDeclaredVariable(NewType, "this");
 		ClassNameTree.ConstValue = NewType;
 		/*local*/ArrayList<GtVariableInfo> FieldList = new ArrayList<GtVariableInfo>();
 
 		while(FieldOffset < ParsedTree.TreeList.size()) {
 			/*local*/GtSyntaxTree FieldTree = ParsedTree.GetSyntaxTreeAt(FieldOffset);
 			if(FieldTree.Pattern.PatternName.equals("$VarDecl$")) {
 				/*local*/GtNode FieldNode = ParsedTree.TypeCheckNodeAt(FieldOffset, Gamma, Gamma.AnyType, DefaultTypeCheckPolicy);
 				if(FieldNode.IsError()) {
 					return FieldNode;
 				}
 				/*local*/String FieldName = FieldTree.GetSyntaxTreeAt(VarDeclName).KeyToken.ParsedText;
 				/*local*/GtVariableInfo FieldInfo = Gamma.LookupDeclaredVariable(FieldName);
 				FieldList.add(FieldInfo);
 			}
 			else if(FieldTree.Pattern.PatternName.equals("$FuncDecl$")) {
 				/*local*/GtSyntaxTree ReturnTree = FieldTree.GetSyntaxTreeAt(FuncDeclReturnType);
 				/*local*/ArrayList<GtSyntaxTree> NewTreeList = new ArrayList<GtSyntaxTree>();
 				/*local*/int i = 0;
 				while(i < FieldTree.TreeList.size() + 3) {
 					NewTreeList.add(null);
 					i = i + 1;
 				}
 				NewTreeList.set(FuncDeclReturnType, ReturnTree);
 				NewTreeList.set(FuncDeclClass, ClassNameTree);
 				NewTreeList.set(FuncDeclName, FieldTree.GetSyntaxTreeAt(FuncDeclName));
 				NewTreeList.set(FuncDeclBlock, FieldTree.GetSyntaxTreeAt(FuncDeclBlock));
 				/*local*/int ParamBase = FuncDeclParam;
 				NewTreeList.set(ParamBase + 0, ClassNameTree);
 				NewTreeList.set(ParamBase + 1, new GtSyntaxTree(Gamma.NameSpace.GetPattern("$Variable$"), Gamma.NameSpace, new GtToken("this", 0), null));
 				if(ParamBase + 2 < NewTreeList.size()) {
 					NewTreeList.set(ParamBase + 2, null);
 				}
 				while(ParamBase < FieldTree.TreeList.size()) {
 					NewTreeList.set(ParamBase + 3, FieldTree.GetSyntaxTreeAt(ParamBase + 0));
 					NewTreeList.set(ParamBase + 4, FieldTree.GetSyntaxTreeAt(ParamBase + 1));
 					if(ParamBase + 5 < FieldTree.TreeList.size()) {
 						NewTreeList.set(ParamBase + 5, FieldTree.GetSyntaxTreeAt(ParamBase + 2));
 					}
 					ParamBase += 3;
 				}
 				FieldTree.TreeList = NewTreeList;
 				Gamma.TypeCheck(FieldTree, Gamma.AnyType, DefaultTypeCheckPolicy);
 			}
 			else if(FieldTree.Pattern.PatternName.equals("constructor")) {
 				FieldTree.Pattern = Gamma.NameSpace.GetPattern("$FuncDecl$");
 				FieldTree.GetSyntaxTreeAt(FuncDeclName).ConstValue = "constructor";
 				Gamma.TypeCheck(FieldTree, NewType, DefaultTypeCheckPolicy);
 			}
 
 			FieldOffset += 1;
 		}
 		Gamma.Generator.GenerateClassField(Gamma.NameSpace, NewType, FieldList);
 		return Gamma.Generator.CreateConstNode(ParsedTree.NameSpace.Context.TypeType, ParsedTree, NewType);
 	}
 
 	// constructor
 	public static GtSyntaxTree ParseConstructor(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		LangDeps.Assert(LeftTree != null);
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 		Tree.SetSyntaxTreeAt(FuncDeclReturnType, LeftTree);
 		Tree.SetMatchedTokenAt(FuncDeclName, NameSpace, TokenContext, "constructor", Required);
 		if(!Tree.HasNodeAt(FuncDeclName)) {
 			return null;
 		}
 		if(TokenContext.MatchToken("(")) {
 			/*local*/int ParseFlag = TokenContext.SetBackTrack(false);  // disabled
 			/*local*/int ParamBase = FuncDeclParam + 3;
 			Tree.SetSyntaxTreeAt(FuncDeclParam + 0, LeftTree);
 			Tree.SetSyntaxTreeAt(FuncDeclParam + 1, new GtSyntaxTree(NameSpace.GetPattern("$Variable$"), NameSpace, new GtToken("this", 0), null));
 			while(!Tree.IsEmptyOrError() && !TokenContext.MatchToken(")")) {
 				if(ParamBase != FuncDeclParam + 3) {
 					Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 				}
 				Tree.SetMatchedPatternAt(ParamBase + VarDeclType, NameSpace, TokenContext, "$Type$", Required);
 				Tree.SetMatchedPatternAt(ParamBase + VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 				if(TokenContext.MatchToken("=")) {
 					Tree.SetMatchedPatternAt(ParamBase + VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 				}
 				ParamBase += 3;
 			}
 			TokenContext.SkipIndent();
 			Tree.SetMatchedPatternAt(FuncDeclBlock, NameSpace, TokenContext, "$Block$", Optional);
 			TokenContext.ParseFlag = ParseFlag;
 			return Tree;
 		}
 		return null;
 	}
 
 	// shell grammar
 	private static boolean IsUnixCommand(String cmd) {
 //ifdef  JAVA
 		/*local*/String[] path = System.getenv("PATH").split(":");
 		/*local*/int i = 0;
 		while(i < path.length) {
 			if(LangDeps.HasFile(path[i] + "/" + cmd)) {
 				return true;
 			}
 			i = i + 1;
 		}
 //endif VAJA
 		return false;
 	}
 
 	public static int SymbolShellToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/boolean ShellMode = false;
 		/*local*/int start = pos;
 		if(TokenContext.SourceList.size() > 0) {
 			/*local*/GtToken PrevToken = TokenContext.SourceList.get(TokenContext.SourceList.size() - 1);
 			if(PrevToken != null && PrevToken.PresetPattern != null &&
 				PrevToken.PresetPattern.PatternName.equals("$ShellExpression$")) {
 				ShellMode = true;
 			}
 		}
 
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LangDeps.CharAt(SourceText, pos);
 			// a-zA-Z0-9_-
 			if(LangDeps.IsLetter(ch)) {
 			}
 			else if(LangDeps.IsDigit(ch)) {
 			}
 			else if(ch == '_') {
 			}
 			else if(ShellMode && (ch == '-' || ch == '/')) {
 			}
 			else {
 				break;
 			}
 			pos += 1;
 		}
 		if(start == pos) {
 			return NoMatch;
 		}
 		String Symbol = SourceText.substring(start, pos);
 
 		/*local*/int i = 0;
 		while(i < ShellGrammarReservedKeywords.length) {
 			/*local*/String Keyword = ShellGrammarReservedKeywords[i];
 			if(Symbol.equals(Keyword)) {
 				return GtStatic.NoMatch;
 			}
 			i = i + 1;
 		}
 		if(Symbol.startsWith("/") || Symbol.startsWith("-")) {
 			if(Symbol.startsWith("//")) { // One-Line Comment
 				return GtStatic.NoMatch;
 			}
 			TokenContext.AddNewToken(Symbol, 0, "$StringLiteral$");
 			return pos;
 		}
 
 		if(DScriptGrammar.IsUnixCommand(Symbol)) {
 			TokenContext.AddNewToken(Symbol, 0, "$ShellExpression$");
 			return pos;
 		}
 		return GtStatic.NoMatch;
 	}
 
 	public final static GtSyntaxTree ParseShell(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		while(!GtStatic.IsEmptyOrError(NewTree) && !TokenContext.MatchToken(";")) {
 			/*local*/GtSyntaxTree Tree = null;
 			if(TokenContext.GetToken().IsDelim() || TokenContext.GetToken().IsIndent()) {
 				break;
 			}
 			if(TokenContext.MatchToken("$ShellExpression$")) {
 				// FIXME
 			}
 			if(Tree == null) {
 				Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Optional);
 			}
 			NewTree.AppendParsedTree(Tree);
 		}
 		return NewTree;
 	}
 
 	public static GtNode TypeShell(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/CommandNode Node = (/*cast*/CommandNode) Gamma.Generator.CreateCommandNode(ContextType, ParsedTree, null);
 		/*local*/GtNode HeadNode = Node;
 		/*local*/int i = 0;
 		/*local*/String Command = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode ThisNode = Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, Command);
 		Node.Append(ThisNode);
 		while(i < ListSize(ParsedTree.TreeList)) {
 			/*local*/GtNode ExprNode = ParsedTree.TypeCheckNodeAt(i, Gamma, Gamma.StringType, DefaultTypeCheckPolicy);
 			if(ExprNode instanceof ConstNode) {
 				/*local*/ConstNode CNode = (/*cast*/ConstNode) ExprNode;
 				if(CNode.ConstValue instanceof String) {
 					/*local*/String Val = (/*cast*/String) CNode.ConstValue;
 					if(Val.equals("|")) {
 						DebugP("PIPE");
 						/*local*/CommandNode PrevNode = Node;
 						Node = (/*cast*/CommandNode) Gamma.Generator.CreateCommandNode(ContextType, ParsedTree, null);
 						PrevNode.PipedNextNode = Node;
 					}
 				}
 			}
 			Node.Append(ExprNode);
 			i = i + 1;
 		}
 		return HeadNode;
 	}
 
 	@Override public void LoadTo(GtNameSpace NameSpace) {
 		// Define Constants
 		NameSpace.DefineSymbol("true", true);
 		NameSpace.DefineSymbol("false", false);
 		NameSpace.DefineSymbol("null", null);
 
 		NameSpace.DefineTokenFunc(" \t", FunctionA(this, "WhiteSpaceToken"));
 		NameSpace.DefineTokenFunc("\n",  FunctionA(this, "IndentToken"));
 		NameSpace.DefineTokenFunc("(){}[]<>.,:;+-*/%=&|!@", FunctionA(this, "OperatorToken"));
 		NameSpace.DefineTokenFunc("/", FunctionA(this, "CommentToken"));  // overloading
 		NameSpace.DefineTokenFunc("Aa", FunctionA(this, "SymbolToken"));
 		NameSpace.DefineTokenFunc("Aa-/", FunctionA(this, "SymbolShellToken")); // overloading
 
 		NameSpace.DefineTokenFunc("\"", FunctionA(this, "StringLiteralToken"));
 		NameSpace.DefineTokenFunc("\"", FunctionA(this, "StringLiteralToken_StringInterpolation"));
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
 
 		NameSpace.DefineSyntaxPattern("$Empty$", FunctionB(this, "ParseEmpty"), FunctionC(this, "TypeEmpty"));
 
 		NameSpace.DefineSyntaxPattern("$Symbol$", FunctionB(this, "ParseSymbol"), null);
 		NameSpace.DefineSyntaxPattern("$Type$", FunctionB(this, "ParseType"), TypeConst);
 		NameSpace.DefineSyntaxPattern("$Variable$", FunctionB(this, "ParseVariable"), FunctionC(this, "TypeVariable"));
 		NameSpace.DefineSyntaxPattern("$Const$", FunctionB(this, "ParseConst"), TypeConst);
 		NameSpace.DefineSyntaxPattern("$StringLiteral$", FunctionB(this, "ParseStringLiteral"), TypeConst);
 		NameSpace.DefineSyntaxPattern("$IntegerLiteral$", FunctionB(this, "ParseIntegerLiteral"), TypeConst);
 
 		NameSpace.DefineSyntaxPattern("$ShellExpression$", FunctionB(this, "ParseShell"), FunctionC(this, "TypeShell"));
 
 		NameSpace.DefineSyntaxPattern("(", FunctionB(this, "ParseGroup"), FunctionC(this, "TypeGroup"));
 		NameSpace.DefineExtendedPattern(".", 0, FunctionB(this, "ParseGetter"), FunctionC(this, "TypeGetter"));
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
 		NameSpace.DefineSyntaxPattern("const", FunctionB(this, "ParseConstDecl"), FunctionC(this, "TypeConstDecl"));
 		NameSpace.DefineSyntaxPattern("class", FunctionB(this, "ParseClassDecl"), FunctionC(this, "TypeClassDecl"));
 		NameSpace.DefineSyntaxPattern("constructor", FunctionB(this, "ParseConstructor"), FunctionC(this, "TypeFuncDecl"));
 		NameSpace.DefineSyntaxPattern("try", FunctionB(this, "ParseTry"), FunctionC(this, "TypeTry"));
 		NameSpace.DefineSyntaxPattern("throw", FunctionB(this, "ParseThrow"), FunctionC(this, "TypeThrow"));
 		NameSpace.DefineSyntaxPattern("new", FunctionB(this, "ParseNew"), FunctionC(this, "TypeNew"));
 		NameSpace.DefineSyntaxPattern("enum", FunctionB(this, "ParseEnum"), FunctionC(this, "TypeEnum"));
 	}
 }
 
 final class GtStat {
 	/*field*/public long MatchCount;
 	/*field*/public long BacktrackCount;  // To count how many times backtracks happen.
 
 	GtStat/*constructor*/() {
 		this.MatchCount     = 0;
 		this.BacktrackCount = 0;
 	}
 }
 
 final class GtClassContext extends GtStatic {
 	/*field*/public final  GtGenerator   Generator;
 	/*field*/public final  GtNameSpace		   RootNameSpace;
 	/*field*/public GtNameSpace		           TopLevelNameSpace;
 
 	// basic class
 	/*field*/public final GtType		VoidType;
 //	/*field*/public final GtType		ObjectType;
 	/*field*/public final GtType		BooleanType;
 	/*field*/public final GtType		IntType;
 	/*field*/public final GtType		StringType;
 	/*field*/public final GtType		AnyType;
 	/*field*/public final GtType		ArrayType;
 	/*field*/public final GtType		FuncType;
 
 	/*field*/public final GtType		TopType;
 	/*field*/public final GtType		EnumType;
 	/*field*/public final GtType		StructType;
 	/*field*/public final GtType		VarType;
 
 	/*field*/public final GtType		TypeType;
 	/*field*/public GtType		PolyFuncType;
 
 	/*field*/public final  GtMap               SourceMap;
 	/*field*/public final  GtMap			   ClassNameMap;
 	/*field*/public final  GtMap               UniqueMethodMap;
 
 	/*field*/public int ClassCount;
 	/*field*/public int MethodCount;
 	/*field*/public final GtStat Stat;
 	/*field*/public ArrayList<String>    ReportedErrorList;
 
 	GtClassContext/*constructor*/(GtGrammar Grammar, GtGenerator Generator) {
 		this.Generator    = Generator;
 		this.Generator.Context = this;
 		this.SourceMap = new GtMap();
 		this.ClassNameMap = new GtMap();
 		this.UniqueMethodMap = new GtMap();
 		this.RootNameSpace = new GtNameSpace(this, null);
 		this.ClassCount = 0;
 		this.MethodCount = 0;
 		this.Stat = new GtStat();
 
 		this.TopType     = new GtType(this, 0, "top", null, null);               //  unregistered
 		this.StructType  = this.TopType.CreateSubType(0, "record", null, null);  //  unregistered
 		this.EnumType    = this.TopType.CreateSubType(EnumClass, "enum", null, null);    //  unregistered
 
 		this.VoidType    = this.RootNameSpace.DefineClassSymbol(new GtType(this, NativeClass, "void", null, Void.class));
 		this.BooleanType = this.RootNameSpace.DefineClassSymbol(new GtType(this, NativeClass, "boolean", false, Boolean.class));
 		this.IntType     = this.RootNameSpace.DefineClassSymbol(new GtType(this, NativeClass, "int", 0L, Long.class));
 		this.StringType  = this.RootNameSpace.DefineClassSymbol(new GtType(this, NativeClass, "String", "", String.class));
 		this.VarType     = this.RootNameSpace.DefineClassSymbol(new GtType(this, 0, "var", null, null));
 		this.AnyType     = this.RootNameSpace.DefineClassSymbol(new GtType(this, DynamicClass, "any", null, null));
 		this.TypeType    = this.RootNameSpace.DefineClassSymbol(this.TopType.CreateSubType(0, "Type", null, null));
 		this.ArrayType   = this.RootNameSpace.DefineClassSymbol(this.TopType.CreateSubType(0, "Array", null, null));
 		this.FuncType    = this.RootNameSpace.DefineClassSymbol(this.TopType.CreateSubType(0, "Func", null, null));
 
 		this.ArrayType.Types = new GtType[1];
 		this.ArrayType.Types[0] = this.AnyType;
 		this.FuncType.Types = new GtType[1];
 		this.FuncType.Types[0] = this.AnyType;
 //ifdef JAVA
 		this.ClassNameMap.put("java.lang.Void",    this.VoidType);
 		this.ClassNameMap.put("java.lang.Boolean", this.BooleanType);
 		this.ClassNameMap.put("java.lang.Integer", this.IntType);
 		this.ClassNameMap.put("java.lang.Long",    this.IntType);
 		this.ClassNameMap.put("java.lang.Short",   this.IntType);
 		this.ClassNameMap.put("java.lang.String",  this.StringType);
 //endif VAJA
 		Grammar.LoadTo(this.RootNameSpace);
 		this.TopLevelNameSpace = new GtNameSpace(this, this.RootNameSpace);
 		this.Generator.SetLanguageContext(this);
 		this.ReportedErrorList = new ArrayList<String>();
 	}
 
 	public void LoadGrammar(GtGrammar Grammar) {
 		Grammar.LoadTo(this.TopLevelNameSpace);
 	}
 
 //	public void Define(String Symbol, Object Value) {
 //		this.RootNameSpace.DefineSymbol(Symbol, Value);
 //	}
 
 	public Object Eval(String ScriptSource, long FileLine) {
 		/*local*/Object resultValue = null;
 		DebugP("Eval: " + ScriptSource);
 		/*local*/GtTokenContext TokenContext = new GtTokenContext(this.TopLevelNameSpace, ScriptSource, FileLine);
 		TokenContext.SkipEmptyStatement();
 		while(TokenContext.HasNext()) {
 			/*local*/GtMap annotation = TokenContext.SkipAndGetAnnotation(true);
 			/*local*/GtSyntaxTree topLevelTree = GtStatic.ParseExpression(this.TopLevelNameSpace, TokenContext);
 			topLevelTree.SetAnnotation(annotation);
 			DebugP("untyped tree: " + topLevelTree);
 			/*local*/GtTypeEnv gamma = new GtTypeEnv(this.TopLevelNameSpace);
 			/*local*/GtNode node = gamma.TypeCheckEachNode(topLevelTree, gamma.VoidType, DefaultTypeCheckPolicy);
 			resultValue = this.Generator.Eval(node);
 			TokenContext.SkipEmptyStatement();
 			TokenContext.Vacume();
 		}
 		return resultValue;
 	}
 
 	public final GtType GuessType (Object Value) {
 		if(Value instanceof GtMethod) {
 			return ((/*cast*/GtMethod)Value).GetFuncType();
 		}
 		else if(Value instanceof GreenTeaTopObject) {
 			return ((/*cast*/GreenTeaTopObject)Value).GreenType;
 		}
 		else {
 			return this.Generator.GetNativeType(Value);
 		}
 	}
 
 	public final boolean CheckSubType(GtType SubType, GtType SuperType) {
 		// TODO: Structual Typing database
 		return false;
 	}
 
 	public GtType GetGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList, boolean IsCreation) {
 		LangDeps.Assert(BaseType.IsGenericType());
 		/*local*/String MangleName = GtStatic.MangleGenericType(BaseType, BaseIdx, TypeList);
 		/*local*/GtType GenericType = (/*cast*/GtType)this.ClassNameMap.get(MangleName);
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
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		TypeList.add(ParamType);
 		return this.GetGenericType(BaseType, 0, TypeList, IsCreation);
 	}
 
 	public final boolean CheckExportableName(GtMethod Method) {
 //		if(Method.Is(ExportMethod)) {
 //			Object Value = this.UniqueMethodMap.get(Method.MethodName);
 //			if(Value == null) {
 //				this.UniqueMethodMap.put(Method.MethodName, Method);
 //				return true;
 //			}
 //			return false;
 //		}
 		return true;
 	}
 
 	/* getter */
 	private String GetterName(GtType BaseType, String Name) {
 		return BaseType.GetSignature() + "@" + Name;
 	}
 
 	public void DefineGetterMethod(GtMethod Method) {
 		/*local*/String Key = this.GetterName(Method.GetRecvType(), Method.MethodName);
 		if(this.UniqueMethodMap.get(Key) == null) {
 			this.UniqueMethodMap.put(Key, Method);
 		}
 	}
 
 	public GtMethod GetGetterMethod(GtType BaseType, String Name, boolean RecursiveSearch) {
 		while(BaseType != null) {
 			/*local*/String Key = this.GetterName(BaseType, Name);
 			/*local*/Object Method = this.UniqueMethodMap.get(Key);
 			if(Method != null) {
 				return (/*cast*/GtMethod)Method;
 			}
 			if(!RecursiveSearch) {
 				break;
 			}
 			BaseType = BaseType.SearchSuperMethodClass;
 		}
 		return null;
 	}
 
 	/* methods */
 	private void SetUniqueMethod(String Key, GtMethod Method) {
 		/*local*/Object Value = this.UniqueMethodMap.get(Key);
 		if(Value == null) {
 			this.UniqueMethodMap.put(Key, Method);
 		}
 		else if(Value instanceof GtMethod) {
 			this.UniqueMethodMap.put(Key, Key);  // not unique !!
 		}
 	}
 
 	private void AddOverloadedMethod(String Key, GtMethod Method) {
 		/*local*/Object Value = this.UniqueMethodMap.get(Key);
 		if(Value instanceof GtMethod) {
 			Method.ListedMethods = (/*cast*/GtMethod)Value;
 		}
 		this.UniqueMethodMap.put(Key, Method);  // not unique !!
 	}
 
 	private final String FuncNameParamSize(String Name, int ParamSize) {
 		return Name + "@" + ParamSize;
 	}
 
 	private String MethodNameParamSize(GtType BaseType, String Name, int ParamSize) {
 		return BaseType.GetSignature() + ":" + Name + "@" + ParamSize;
 	}
 
 	public void DefineMethod(GtMethod Method) {
 		/*local*/String MethodName = Method.MethodName;
 		this.SetUniqueMethod(MethodName, Method);
 		/*local*/String Key = this.FuncNameParamSize(MethodName, (Method.Types.length - 1));
 		this.SetUniqueMethod(Key, Method);
 		/*local*/GtType RecvType = Method.GetRecvType();
 		Key = this.MethodNameParamSize(RecvType, MethodName, (Method.Types.length - 2));
 		this.SetUniqueMethod(Key, Method);
 		this.AddOverloadedMethod(Key, Method);
 		this.SetUniqueMethod(Method.MangledName, Method);
 	}
 
 	public GtMethod GetUniqueFunctionName(String Name) {
 		/*local*/Object Value = this.UniqueMethodMap.get(Name);
 		if(Value != null && Value instanceof GtMethod) {
 			return (/*cast*/GtMethod)Value;
 		}
 		return null;
 	}
 
 	public GtMethod GetUniqueFunction(String Name, int FuncParamSize) {
 		/*local*/Object Value = this.UniqueMethodMap.get(this.FuncNameParamSize(Name, FuncParamSize));
 		if(Value != null && Value instanceof GtMethod) {
 			return (/*cast*/GtMethod)Value;
 		}
 		return null;
 	}
 
 	public final GtMethod GetGreenListedMethod(GtType BaseType, String MethodName, int MethodParamSize, boolean RecursiveSearch) {
 		while(BaseType != null) {
 			/*local*/Object Value = this.UniqueMethodMap.get(this.MethodNameParamSize(BaseType, MethodName, MethodParamSize));
 			if(Value instanceof GtMethod) {
 				return (/*cast*/GtMethod)Value;
 			}
 			if(!RecursiveSearch) {
 				break;
 			}
 			BaseType = BaseType.SearchSuperMethodClass;
 		}
 		return null;
 	}
 
 	public final GtMethod GetListedMethod(GtType BaseType, String MethodName, int MethodParamSize, boolean RecursiveSearch) {
 		/*local*/GtMethod Method = this.GetGreenListedMethod(BaseType, MethodName, MethodParamSize, RecursiveSearch);
 		if(Method == null && BaseType.IsNative() && this.Generator.TransformNativeMethods(BaseType, MethodName)) {
 			Method = this.GetGreenListedMethod(BaseType, MethodName, MethodParamSize, RecursiveSearch);
 		}
 		return Method;
 	}
 
 	public final GtMethod GetGreenMethod(GtType BaseType, String Name, int BaseIndex, ArrayList<GtType> TypeList, boolean RecursiveSearch) {
 		while(BaseType != null) {
 			/*local*/String Key = GtStatic.MangleMethodName(BaseType, Name, BaseIndex, TypeList);
 			/*local*/Object Value = this.UniqueMethodMap.get(Key);
 			if(Value instanceof GtMethod) {
 				return (/*cast*/GtMethod)Value;
 			}
 			if(!RecursiveSearch) {
 				break;
 			}
 			BaseType = BaseType.SearchSuperMethodClass;
 		}
 		return null;
 	}
 
 	public final GtMethod GetMethod(GtType BaseType, String Name, int BaseIndex, ArrayList<GtType> TypeList, boolean RecursiveSearch) {
 		/*local*/GtMethod Method = this.GetGreenMethod(BaseType, Name, BaseIndex, TypeList, RecursiveSearch);
 		if(Method == null && BaseType.IsNative() && this.Generator.TransformNativeMethods(BaseType, Name)) {
 			Method = this.GetGreenMethod(BaseType, Name, BaseIndex, TypeList, RecursiveSearch);
 		}
 		return Method;
 	}
 
 	/* convertor, wrapper */
 	private final String ConverterName(GtType FromType, GtType ToType) {
 		return FromType.GetSignature() + ">" + ToType.GetSignature();
 	}
 
 	private final String WrapperName(GtType FromType, GtType ToType) {
 		return FromType.GetSignature() + "<" + ToType.GetSignature();
 	}
 
 	public GtMethod GetConverterMethod(GtType FromType, GtType ToType, boolean RecursiveSearch) {
 		/*local*/String Key = this.ConverterName(FromType, ToType);
 		/*local*/Object Method = this.UniqueMethodMap.get(Key);
 		if(Method != null) {
 			return (/*cast*/GtMethod)Method;
 		}
 		if(RecursiveSearch && FromType.SearchSuperMethodClass != null) {
 			return this.GetConverterMethod(FromType.SearchSuperMethodClass, ToType, RecursiveSearch);
 		}
 		return null;
 	}
 
 	public GtMethod GetWrapperMethod(GtType FromType, GtType ToType, boolean RecursiveSearch) {
 		/*local*/String Key = this.WrapperName(FromType, ToType);
 		/*local*/Object Method = this.UniqueMethodMap.get(Key);
 		if(Method != null) {
 			return (/*cast*/GtMethod)Method;
 		}
 		if(RecursiveSearch && FromType.SearchSuperMethodClass != null) {
 			return this.GetWrapperMethod(FromType.SearchSuperMethodClass, ToType, RecursiveSearch);
 		}
 		return null;
 	}
 
 	public GtMethod GetCastMethod(GtType FromType, GtType ToType, boolean RecursiveSearch) {
 		/*local*/String Key = this.WrapperName(FromType, ToType);
 		/*local*/Object Method = this.UniqueMethodMap.get(Key);
 		if(Method != null) {
 			return (/*cast*/GtMethod)Method;
 		}
 		Key = this.ConverterName(FromType, ToType);
 		Method = this.UniqueMethodMap.get(Key);
 		if(Method != null) {
 			return (/*cast*/GtMethod)Method;
 		}
 		if(RecursiveSearch && FromType.SearchSuperMethodClass != null) {
 			return this.GetCastMethod(FromType.SearchSuperMethodClass, ToType, RecursiveSearch);
 		}
 		return null;
 	}
 
 	public final void DefineConverterMethod(GtMethod Method) {
 		/*local*/String Key = this.ConverterName(Method.GetRecvType(), Method.GetReturnType());
 		this.UniqueMethodMap.put(Key, Method);
 	}
 
 	public final void DefineWrapperMethod(GtMethod Method) {
 		/*local*/String Key = this.WrapperName(Method.GetRecvType(), Method.GetReturnType());
 		this.UniqueMethodMap.put(Key, Method);
 	}
 
 	private final String GetSourcePosition(long FileLine) {
 		return "(eval:" + (int) FileLine + ")";  // FIXME: USE SourceMap
 	}
 
 	public final void ReportError(int Level, GtToken Token, String Message) {
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
 			this.ReportedErrorList.add(Message);
 			//GtStatic.println(Message);
 		}
 	}
 
 	public final ArrayList<String> GetReportedErrors() {
 		ArrayList<String> List = this.ReportedErrorList;
 		this.ReportedErrorList = new ArrayList<String>();
 		return List;
 	}
 
 
 }
 
 public class GreenTeaScript extends GtStatic {
 	public final static void main(String[] Args) {
 		/*local*/String CodeGeneratorName = "--java";
 		/*local*/int Index = 0;
 		/*local*/String OneLiner = null;
 		while(Index < Args.length) {
 			/*local*/String Argu = Args[Index];
 			if(!Argu.startsWith("-")) {
 				break;
 			}
 			Index += 1;
 			if(Argu.startsWith("--")) {
 				CodeGeneratorName = Argu;
 				continue;
 			}
 			if(Argu.equals("-e") && Index < Args.length) {
 				OneLiner = Args[Index];
 				Index += 1;
 				continue;
 			}
 			if(Argu.equals("-verbose")) {
 				GtStatic.DebugPrintOption = true;
 				continue;
 			}
 			LangDeps.Usage();
 		}
 		/*local*/GtGenerator Generator = LangDeps.CodeGenerator(CodeGeneratorName);
 		if(Generator == null) {
 			LangDeps.Usage();
 		}
 		/*local*/GtClassContext Context = new GtClassContext(new DScriptGrammar(), Generator);
 		/*local*/boolean ShellMode = true;
 		if(OneLiner != null) {
 			Context.Eval(OneLiner, 1);
 			ShellMode = false;
 		}
 		while(Index < Args.length) {
 			Context.Eval(LangDeps.LoadFile(Args[Index]), 1);
 			ShellMode = false;
 			Index += 1;
 		}
 		if(ShellMode) {
 			/*local*/int linenum = 1;
 			/*local*/String Line = null;
 			while((Line = LangDeps.ReadLine(">>> ")) != null) {
 				Context.Eval(Line, linenum);
 				linenum += 1;
 			}
 		}
 	}
 
 }
