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
 package org.GreenTeaScript;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 //endif VAJA
 
 class GtUndefinedSymbol {
 	@Override public String toString() {
 		return "UndefinedSymbol";
 	}
 }
 
 /*GreenTeaConst Begin*/
 //ifdef JAVA
 interface GreenTeaConsts {
 //endif VAJA
 	// Version
 	public final static String  ProgName  = "GreenTeaScript";
 	public final static String  CodeName  = "Reference Implementation of D-Script";
 	public final static int     MajorVersion = 0;
 	public final static int     MinerVersion = 1;
 	public final static int     PatchLevel   = 0;
 	public final static String  Version = "0.1";
 	public final static String  Copyright = "Copyright (c) 2013, JST/CREST DEOS and Konoha project authors";
 	public final static String  License = "BSD-Style Open Source";
 
 	// NameSpaceFlag
 	public final static int     RootNameSpace       = 1 << 0;  // @RootNameSpace
 	public final static int     PublicNameSpace     = 1 << 1;  // @Public
 
 	// ClassFlag
 	public final static int     ExportType         = 1 << 0;  // @Export
 	public final static int     PublicType         = 1 << 1;  // @Public
 	public final static int		NativeType	       = 1 << 2;
 	public final static int		VirtualType		   = 1 << 3;  // @Virtual
 	public final static int     EnumType           = 1 << 4;
 	public final static int     DeprecatedType     = 1 << 5;  // @Deprecated
 	public final static int     HiddenType         = 1 << 6;
 	// UnrevealedType is a type that must be hidden for users
 	// WeatType must be converted to non-UnrevealedType by StrongCoersion
 	// UnrevealedType is set only if StrongCoersion is defined
 	public final static int     UnrevealedType           = HiddenType;
 	public final static int     CommonType         = 1 << 7;  // @Common
 
 	public final static int		DynamicType	       = 1 << 8;  // @Dynamic
 	public final static int     OpenType           = 1 << 9;  // @Open for the future
 	public final static int     UnboxType          = 1 << 10; 
 	public final static int     TypeVariable       = 1 << 14;
 	public final static int     GenericVariable    = 1 << 15;
 
 	// FuncFlag
 	public final static int		ExportFunc		    = 1 << 0;  // @Export
 	public final static int     PublicFunc          = 1 << 1;  // @Public
 	public final static int		NativeFunc		    = 1 << 2;
 	public final static int		VirtualFunc		    = 1 << 3;
 	public final static int		ConstFunc			= 1 << 4;  // @Const
 	public final static int     DeprecatedFunc      = 1 << 5;  // @Deprecated
 	public final static int     HiddenFunc          = 1 << 6;  // @Hidden
 	public final static int     CommonFunc          = 1 << 7;  // @Common
 
 	public final static int		NativeMethodFunc	= 1 << 8;
 	public final static int		NativeMacroFunc	    = 1 << 9;
 	public final static int		NativeVariadicFunc	= 1 << 10;
 	public final static int     ConstructorFunc     = 1 << 11;
 	public final static int     MethodFunc          = 1 << 12;  
 	public final static int     GetterFunc          = 1 << 13;
 	public final static int     SetterFunc          = 1 << 14;
 	public final static int     OperatorFunc        = 1 << 15;  //@Operator
 	public final static int     ConverterFunc       = 1 << 16;
 	public final static int     CoercionFunc        = 1 << 17;  //@Coercion
 	public final static int     StrongCoercionFunc  = 1 << 18;  //@StrongCoercion
 	public final static int     GenericFunc         = 1 << 15;
 	public final static int		LazyFunc		    = 1 << 16;
 	
 	// VarFlag
 	public final static int  ReadOnlyVar = 1;              // @ReadOnly x = 1; disallow x = 2
 	//public final static int  MutableFieldVar  = (1 << 1);  // @Mutable x; x.y = 1 is allowed
 
 	public final static int		MismatchedPosition		= -1;
 	public final static int     Required          = (1 << 0);
 	public final static int     Optional          = (1 << 1);
 	public final static int     AllowLineFeed     = (1 << 2);
 	public final static int     AllowAnnotation   = (1 << 3);
 	public final static int     OpenSkipIndent    = (1 << 4);
 	public final static int     CloseSkipIndent   = (1 << 5);
 
 		
 	public final static int		ErrorLevel						= 0;
 	public final static int     TypeErrorLevel                  = 1;
 	public final static int		WarningLevel					= 2;
 	public final static int		InfoLevel					    = 3;
 
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
 	public final static int NameSymbolTokenFlag	  = (1 << 6);
 
 	// ParseFlag
 	public final static int	BackTrackParseFlag	= 1;
 	public final static int	SkipIndentParseFlag	= (1 << 1);
 
 	// SyntaxTree
 	public final static int KeyTokenIndex   = -2;
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
 
 	// for(init; cond; iter) {...}
 	static final int ForInit = 0;
 	static final int ForCond = 1;
 	static final int ForIteration = 2;
 	static final int ForBody = 3;
 
 	// for(init; cond; iter) {...}
 	static final int ForEachType = 0;
 	static final int ForEachName = 1;
 	static final int ForEachIter = 2;
 	static final int ForEachBody = 3;
 
 	// ReturnStmt
 	public final static int	ReturnExpr	= 0;
 
 	// var N = 1;
 	public final static int	VarDeclType		= 0;
 	public final static int	VarDeclName		= 1;
 	public final static int	VarDeclValue	= 2;
 	public final static int	VarDeclScope	= 3;
 
 //	//Func Call;
 //	public static final int	CallExpressionIndex	= 0;
 //	public static final int	CallParameterIndex		= 1;
 
 	// Const Decl;
 	public final static int	SymbolDeclClassIndex	= 0;
 	public final static int	SymbolDeclNameIndex	= 1;
 	public final static int	SymbolDeclValueIndex	= 2;
 
 	// Func Decl;
 	public final static int	FuncDeclReturnType	= 0;
 	public final static int	FuncDeclClass		= 1;
 	public final static int	FuncDeclName		= 2;
 	public final static int	FuncDeclBlock		= 3;
 	public final static int FuncGenericOption   = 4;
 	public final static int	FuncDeclParam		= 5;
 
 	public final static int GenericReturnType   = 0;
 	public final static int GenericParam        = 1;
 
 	// Class Decl;
 	public final static int	ClassDeclName		= 0;
 	public final static int	ClassDeclSuperType	= 1;
 	public final static int ClassDeclBlock      = 2;
 	public final static int	ClassDeclFieldStartIndex    = 2;
 
 	// try-catch
 	public final static int TryBody         = 0;
 	public final static int CatchVariable   = 1;
 	public final static int CatchBody       = 2;
 	public final static int FinallyBody     = 3;
 
 	// switch-case
 	static final int SwitchCaseCondExpr = 0;
 	static final int SwitchCaseDefaultBlock = 1;
 	static final int SwitchCaseCaseIndex = 2;
 
 	// Enum
 	public final static int EnumNameTreeIndex = 0;
 
 	public final static int BinaryOperator					= 1;
 	public final static int LeftJoin						= 1 << 1;
 	public final static int PrecedenceShift					= 3;
 	public final static int PrecedenceCStyleMUL			    = (100 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleADD			    = (200 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleSHIFT			= (300 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleCOMPARE		    = (400 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceInstanceof            = PrecedenceCStyleCOMPARE;
 	public final static int PrecedenceCStyleEquals			= (500 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleBITAND			= (600 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleBITXOR			= (700 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleBITOR			= (800 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleAND			    = (900 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleOR				= (1000 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleTRINARY		    = (1100 << PrecedenceShift) | BinaryOperator;				/* ? : */
 	public final static int PrecedenceCStyleAssign			= (1200 << PrecedenceShift) | BinaryOperator;
 	public final static int PrecedenceCStyleCOMMA			= (1300 << PrecedenceShift) | BinaryOperator;
 
 	public final static int DefaultTypeCheckPolicy			= 0;
 	public final static int NoCheckPolicy                   = 1;
 	public final static int CastPolicy                      = (1 << 1);
 	public final static int IgnoreEmptyPolicy               = (1 << 2);
 	public final static int AllowEmptyPolicy                = (1 << 3);
 	public final static int AllowVoidPolicy                 = (1 << 4);
 	public final static int AllowCoercionPolicy             = (1 << 5);
 	public final static int OnlyConstPolicy                 = (1 << 6);
 	public final static int NullablePolicy                  = (1 << 8);
 	public final static int BlockPolicy                     = (1 << 7);
 
 	public final static Object UndefinedSymbol = new GtUndefinedSymbol();
 	public final static String NativeNameSuffix = "__";
 
 	public final static boolean UseLangStat = true;
 
 	public final static int VerboseSymbol    = 1;
 	public final static int VerboseType      = (1 << 1);
 	public final static int VerboseFunc      = (1 << 2);
 	public final static int VerboseEval      = (1 << 3);
 	public final static int VerboseToken     = (1 << 4);
 	public final static int VerboseUndefined   = (1 << 5);
 
 	public final static int VerboseNative    = (1 << 6);
 	public final static int VerboseFile      = (1 << 7);
 	public final static int VerboseException = (1 << 8);
 
 	public final static int VerboseRuntime   = (1 << 9);
 
 /*GreenTeaConst End*/
 //ifdef JAVA
 }
 
 class GreenTeaUtils implements GreenTeaConsts {
 //endif VAJA
 /*GreenTeaUtils Begin*/
 	public final static boolean IsFlag(int flag, int flag2) {
 		return ((flag & flag2) == flag2);
 	}
 
 	public final static int UnsetFlag(int flag, int flag2) {
 		return (flag & (~flag2));
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
 
 	public final static int AsciiToTokenMatrixIndex(char c) {
 		if(c < 128) {
 			return CharMatrix[c];
 		}
 		return UnicodeChar;
 	}
 
 	private final static String n2s(int n) {
 		if(n < (27)) {
 			return LibGreenTea.CharToString((/*cast*/char)(65 + (n - 0)));
 		}
 		else if(n < (27 + 10)) {
 			return LibGreenTea.CharToString((/*cast*/char)(48 + (n - 27)));
 		}
 		else {
 			return LibGreenTea.CharToString((/*cast*/char)(97 + (n - 37)));
 		}
 	}
 
 	public final static String NumberToAscii(int number) {
 		if(number >= 3600) {
 			return n2s(number / 3600) + NumberToAscii(number % 3600);
 		}
 		return n2s((number / 60)) + n2s((number % 60));
 	}
 
 	public final static String NativeVariableName(String Name, int Index) {
 		return Name + NativeNameSuffix + Index;
 	}
 
 	public final static String ExtendedPatternSymbol(String PatternName) {
 		return "\t" + PatternName;
 	}
 	
 	public final static String ClassSymbol(GtType ClassType, String Symbol) {
 		return ClassType.GetUniqueName() + "." + Symbol;
 	}
 
 	public final static String ClassStaticSymbol(GtType ClassType, String Symbol) {
 		return ClassType.GetUniqueName() + ".@" + Symbol;
 	}
 
 	public final static String FuncSymbol(String Symbol) {
 		return LibGreenTea.IsVariableName(Symbol, 0) ? Symbol : "__" + Symbol;
 	}
 
 	public final static String ConverterSymbol(GtType ClassType) {
 		return ClassType.GetUniqueName();
 	}
 
 	public final static String ConstructorSymbol() {
 		return "";
 	}
 
 	public final static String GetterSymbol(String Symbol) {
 		return Symbol + "+";
 	}
 
 	public final static String SetterSymbol(String Symbol) {
 		return Symbol + "=";
 	}
 
 	public final static String MangleGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList) {
 		/*local*/String s = BaseType.ShortName + NativeNameSuffix;
 		/*local*/int i = BaseIdx;
 		while(i < LibGreenTea.ListSize(TypeList)) {
 			/*local*/GtType Type = TypeList.get(i);
 			if(Type.IsTypeVariable()) {
 				s = s + Type.ShortName;
 			}
 			else {
 				s = s + NumberToAscii(TypeList.get(i).TypeId);
 			}
 			i = i + 1;
 		}
 		return s;
 	}
 
 	public final static int ApplyTokenFunc(GtTokenFunc TokenFunc, GtTokenContext TokenContext, String ScriptSource, int Pos) {
 		while(TokenFunc != null) {
 			/*local*/int NextIdx = (/*cast*/int)LibNative.ApplyTokenFunc(TokenFunc.Func, TokenContext, ScriptSource, Pos);
 			if(NextIdx > Pos) return NextIdx;
 			TokenFunc = TokenFunc.ParentFunc;
 		}
 		return MismatchedPosition;
 	}
 
 	public final static GtSyntaxPattern MergeSyntaxPattern(GtSyntaxPattern Pattern, GtSyntaxPattern Parent) {
 		if(Parent == null) return Pattern;
 		/*local*/GtSyntaxPattern MergedPattern = new GtSyntaxPattern(Pattern.PackageNameSpace, Pattern.PatternName, Pattern.MatchFunc, Pattern.TypeFunc);
 		MergedPattern.ParentPattern = Parent;
 		return MergedPattern;
 	}
 
 	public final static boolean IsMismatchedOrError(GtSyntaxTree Tree) {
 		return (Tree == null || Tree.IsMismatchedOrError());
 	}
 
 	public final static boolean IsValidSyntax(GtSyntaxTree Tree) {
 		return !(GreenTeaUtils.IsMismatchedOrError(Tree));
 	}
 
 	public final static GtSyntaxTree TreeHead(GtSyntaxTree Tree) {
 		if(Tree != null) {
 			while(Tree.PrevTree != null) {
 				Tree = Tree.PrevTree;
 			}
 		}
 		return Tree;
 	}
 
 	public final static GtSyntaxTree TreeTail(GtSyntaxTree Tree) {
 		if(Tree != null) {
 			while(Tree.NextTree != null) {
 				Tree = Tree.NextTree;
 			}
 		}
 		return Tree;
 	}
 
 	public final static GtSyntaxTree LinkTree(GtSyntaxTree LastNode, GtSyntaxTree Node) {
 		Node.PrevTree = LastNode;
 		if(LastNode != null) {
 			LastNode.NextTree = Node;
 		}
 		return GreenTeaUtils.TreeTail(Node);
 	}
 
 	public final static GtSyntaxTree ApplySyntaxPattern(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int Pos = TokenContext.GetPosition(0);
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		/*local*/GtSyntaxPattern CurrentPattern = Pattern;
 		while(CurrentPattern != null) {
 			/*local*/GtFunc delegate = CurrentPattern.MatchFunc;
 			TokenContext.RollbackPosition(Pos, 0);
 			if(CurrentPattern.ParentPattern != null) {   // This means it has next patterns
 				TokenContext.ParseFlag = ParseFlag | BackTrackParseFlag;
 			}
 			//LibGreenTea.DebugP("B :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + ", next=" + CurrentPattern.ParentPattern);
 			TokenContext.IndentLevel += 1;
 			/*local*/GtSyntaxTree ParsedTree = (/*cast*/GtSyntaxTree)LibNative.ApplyParseFunc(delegate, NameSpace, TokenContext, LeftTree, CurrentPattern);
 			TokenContext.IndentLevel -= 1;
 			TokenContext.ParseFlag = ParseFlag;
 			if(ParsedTree != null && ParsedTree.IsMismatched()) {
 				ParsedTree = null;
 			}
 			//LibGreenTea.DebugP("E :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + " => " + ParsedTree);
 			if(ParsedTree != null) {
 				return ParsedTree;
 			}
 			CurrentPattern = CurrentPattern.ParentPattern;
 		}
 		if(TokenContext.IsAllowedBackTrack()) {
 			TokenContext.RollbackPosition(Pos, 0);
 		}
 		else {
 			TokenContext.SkipErrorStatement();
 		}
 		if(Pattern == null) {
 			LibGreenTea.VerboseLog(VerboseUndefined, "undefined syntax pattern: " + Pattern);
 		}
 		return TokenContext.ReportExpectedPattern(Pattern);
 	}
 
 	// typing
 	public final static GtNode ApplyTypeFunc(GtFunc TypeFunc, GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		if(TypeFunc != null) {
 			Gamma.NameSpace = ParsedTree.NameSpace;
 			return (/*cast*/GtNode)LibNative.ApplyTypeFunc(TypeFunc, Gamma, ParsedTree, Type);
 		}
 		return Gamma.Generator.CreateEmptyNode(GtStaticTable.VoidType);
 	}
 
 	public final static GtNode LinkNode(GtNode LastNode, GtNode Node) {
 		Node.PrevNode = LastNode;
 		if(LastNode != null) {
 			LastNode.NextNode = Node;
 // IMIFU
 //			if(Node.ParentNode != null) {
 //				Node.ParentNode.SetParent(LastNode);
 //			}
 		}
 		return Node;
 	}
 
 	public final static GtNode TypeBlock(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/int StackTopIndex = Gamma.StackTopIndex;
 		/*local*/GtNode LastNode = null;
 		while(ParsedTree != null) {
 			/*local*/GtNode Node = GreenTeaUtils.ApplyTypeFunc(ParsedTree.Pattern.TypeFunc, Gamma, ParsedTree, GtStaticTable.VoidType);
 			/*local*/Node = Gamma.TypeCheckSingleNode(ParsedTree, Node, GtStaticTable.VoidType, DefaultTypeCheckPolicy);
 			/*local*/LastNode = GreenTeaUtils.LinkNode(LastNode, Node);
 			if(Node.IsErrorNode()) {
 				break;
 			}
 			ParsedTree = ParsedTree.NextTree;
 		}
 		Gamma.PushBackStackIndex(StackTopIndex);
 		if(LastNode == null) {
 			return Gamma.Generator.CreateEmptyNode(GtStaticTable.VoidType);
 		}
 		return LastNode.MoveHeadNode();
 	}
 /*GreenTeaUtils End*/
 //ifdef JAVA
 	
 	public final static GtFunc LoadTokenFunc2(GtParserContext ParserContext, Class<?> GrammarClass, String FuncName) {
 		try {
 			Method JavaMethod = GrammarClass.getMethod(FuncName, GtTokenContext.class, String.class, long.class);
 			return LibGreenTea.ConvertNativeMethodToFunc(ParserContext, JavaMethod);
 		}
 		catch(NoSuchMethodException e) {
 			LibGreenTea.VerboseException(e);
 			LibGreenTea.Exit(1, e.toString());
 		}
 		return null;
 	}
 	public final static GtFunc LoadParseFunc2(GtParserContext ParserContext, Class<?> GrammarClass, String FuncName) {
 		try {
 			Method JavaMethod = GrammarClass.getMethod(FuncName, GtNameSpace.class, GtTokenContext.class, GtSyntaxTree.class, GtSyntaxPattern.class);
 			return LibGreenTea.ConvertNativeMethodToFunc(ParserContext, JavaMethod);
 		}
 		catch(NoSuchMethodException e) {
 			LibGreenTea.VerboseException(e);
 			LibGreenTea.Exit(1, e.toString());
 		}
 		return null;
 	}
 	public final static GtFunc LoadTypeFunc2(GtParserContext ParserContext, Class<?> GrammarClass, String FuncName) {
 		try {
 			Method JavaMethod = GrammarClass.getMethod(FuncName, GtTypeEnv.class, GtSyntaxTree.class, GtType.class);
 			return LibGreenTea.ConvertNativeMethodToFunc(ParserContext, JavaMethod);
 		}
 		catch(NoSuchMethodException e) {
 			LibGreenTea.VerboseException(e);
 			LibGreenTea.Exit(1, e.toString());
 		}
 		return null;
 	}
 }
 
 final class GtMap {
 	final HashMap<String, Object>	Map;
 
 	public GtMap() {
 		this.Map = new HashMap<String, Object>();
 	}
 
 	public final void put(String Key, Object Value) {
 		this.Map.put(Key, Value);
 	}
 
 	public final Object GetOrNull(String Key) {
 		return this.Map.get(Key);
 	}
 }
 //endif VAJA
 
 final class GtStat {
 	/*field*/public int VarDeclAny;
 	/*field*/public int VarDeclInferAny;
 	/*field*/public int VarDeclInfer;
 	/*field*/public int VarDecl;
 
 	/*field*/public long MatchCount;
 	/*field*/public long BacktrackCount;  // To count how many times backtracks happen.
 
 	GtStat/*constructor*/() {
 		this.VarDecl = 0;
 		this.VarDeclInfer = 0;
 		this.VarDeclAny = 0;
 		this.VarDeclInferAny = 0;
 
 		this.MatchCount     = 0;
 		this.BacktrackCount = 0;
 	}
 }
 
 public class GreenTeaScript extends GreenTeaUtils {
 	public final static void ExecCommand(String[] Args) {
 		/*local*/String TargetCode = "exe";  // self executable
 		/*local*/int GeneratorFlag = 0;
 		/*local*/String OneLiner = null;
 		/*local*/String RequiredLibName= null;
 		/*local*/String OutputFile = "-";  // stdout
 		/*local*/int    Index = 0;
 		/*local*/boolean ShellMode = false;
 		while(Index < Args.length) {
 			/*local*/String Argu = Args[Index];
 			if(!Argu.startsWith("-")) {
 				break;
 			}
 			Index += 1;
 			if((Argu.equals("-e") || Argu.equals("--eval")) && Index < Args.length) {
 				OneLiner = Args[Index];
 				Index += 1;
 				continue;
 			}
 			if((Argu.equals("-o") || Argu.equals("--out")) && Index < Args.length) {
 				if(!Args[Index].endsWith(".green")) {  // for safety
 					OutputFile = Args[Index];
 					Index += 1;
 					continue;
 				}
 			}
 			if((Argu.equals("-l") || Argu.equals("--lang")) && Index < Args.length) {
 				if(!Args[Index].endsWith(".green")) {  // for safety
 					TargetCode = Args[Index];
 					Index += 1;
 					continue;
 				}
 			}
 			if((Argu.equals("-r") || Argu.equals("--require")) && Index < Args.length) {
 				RequiredLibName = Args[Index];
 				Index += 1;
 				continue;
 			}
 			if(Argu.equals("-i")) {
 				ShellMode = true;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose")) {
 				LibGreenTea.DebugMode = true;
 				LibGreenTea.VerboseMask |= (GreenTeaUtils.VerboseFile|GreenTeaUtils.VerboseSymbol|GreenTeaUtils.VerboseNative);
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:token")) {
 				LibGreenTea.VerboseMask |= GreenTeaUtils.VerboseToken;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:type")) {
 				LibGreenTea.VerboseMask |= GreenTeaUtils.VerboseType;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:symbol")) {
 				LibGreenTea.VerboseMask |= GreenTeaUtils.VerboseSymbol;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:native")) {
 				LibGreenTea.VerboseMask |= GreenTeaUtils.VerboseNative;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:func")) {
 				LibGreenTea.VerboseMask |= GreenTeaUtils.VerboseFunc;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:all")) {
 				LibGreenTea.VerboseMask = -1;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:no")) {
 				LibGreenTea.VerboseMask = 0;
 				continue;
 			}
 			LibGreenTea.Usage(Argu + " is unknown");
 		}
 		/*local*/GtGenerator Generator = LibGreenTea.CodeGenerator(TargetCode, OutputFile, GeneratorFlag);
 		if(Generator == null) {
 			LibGreenTea.Usage("no target: " + TargetCode);
 		}
 		/*local*/GtParserContext Context = new GtParserContext(new KonohaGrammar(), Generator);
 		if(RequiredLibName != null) {
 			if(!Context.TopLevelNameSpace.LoadRequiredLib(RequiredLibName)) {
 				LibGreenTea.Exit(1, "failed to load required library: " + RequiredLibName);
 			}
 		}
 		if(OneLiner != null) {
 			Context.TopLevelNameSpace.Eval(OneLiner, 1);
 		}
 		if(!(Index < Args.length)) {
 			ShellMode = true;
 		}
 		GreenTeaArray ARGV = GreenTeaArray.NewArray1(GtStaticTable.StringType, 0);
 		while(Index < Args.length) {
 			ARGV.ArrayBody.add(Args[Index]);
 			Index += 1;
 		}
 		Context.RootNameSpace.SetSymbol("ARGV", ARGV, null);
 		if(ARGV.ArrayBody.size() > 0) {
 			/*local*/String FileName = (/*cast*/String)ARGV.ArrayBody.get(0);
 			/*local*/String ScriptText = LibGreenTea.LoadFile2(FileName);
 			if(ScriptText == null) {
 				LibGreenTea.Exit(1, "file not found: " + FileName);
 			}
 			/*local*/long FileLine = GtStaticTable.GetFileLine(FileName, 1);
 			/*local*/boolean Success = Context.TopLevelNameSpace.Load(ScriptText, FileLine);
 			Context.ShowReportedErrors();
 			if(!Success) {
 				LibGreenTea.Exit(1, "abort loading: " + FileName);
 			}
 		}
 		if(ShellMode) {
 			LibGreenTea.println(GreenTeaUtils.ProgName + GreenTeaUtils.Version + " (" + GreenTeaUtils.CodeName + ") on " + LibGreenTea.GetPlatform());
 			LibGreenTea.println(GreenTeaUtils.Copyright);
 			Context.ShowReportedErrors();
 			/*local*/int linenum = 1;
 			/*local*/String Line = null;
 			while((Line = LibGreenTea.ReadLine2(">>> ", "    ")) != null) {
 				try {
 					/*local*/Object EvaledValue = Context.TopLevelNameSpace.Eval(Line, linenum);
 					Context.ShowReportedErrors();
 					if(EvaledValue != null) {
 						LibGreenTea.println(" (" + GtStaticTable.GuessType(EvaledValue) + ":" + LibNative.GetClassName(EvaledValue) + ") " + LibGreenTea.Stringify(EvaledValue));
 					}
 					linenum += 1;
 				}
 				catch(Exception e) {
					/*local*/StackTraceElement elements[] = {
							new StackTraceElement("TopLevel", "TopLevelEval", "--shell--", linenum)};
					e.setStackTrace(elements);
 					e.printStackTrace();
					linenum = linenum + 1;
 				}
 			}
 			LibGreenTea.println("");
 		}
 		else {
 			Generator.FlushBuffer();
 		}
 	}
 
 	public final static void main(String[] Args)  {
 		try {
 			GreenTeaScript.ExecCommand(Args);
 		}
 		catch(SoftwareFaultException e) {
 			System.err.println(e.GetStackTrace());
 		}
 	}
 }
