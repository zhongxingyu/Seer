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
 	public final static int		NativeType	     = 1 << 2;
 	public final static int		VirtualType		   = 1 << 3;  // @Virtual
 	public final static int     EnumType           = 1 << 4;
 	public final static int     DeprecatedType     = 1 << 5;  // @Deprecated
 
 	public final static int		DynamicType	    = 1 << 6;  // @Dynamic
 	public final static int     OpenType           = 1 << 7;  // @Open for the future
 	public final static int     CommonType         = 1 << 8;  // @Common
 	public final static int     TypeParameter       = 1 << 15;
 
 	// FuncFlag
 	public final static int		ExportFunc		    = 1 << 0;  // @Export
 	public final static int     PublicFunc          = 1 << 1;  // @Public
 	public final static int		NativeFunc		    = 1 << 2;
 	public final static int		VirtualFunc		    = 1 << 3;
 	public final static int		ConstFunc			= 1 << 4;  // @Const
 	public final static int     DeprecatedFunc      = 1 << 5;  // @Deprecated
 
 	public final static int		NativeStaticFunc	= 1 << 6;
 	public final static int		NativeMacroFunc	    = 1 << 7;
 	public final static int		NativeVariadicFunc	= 1 << 8;
 	public final static int     ConstructorFunc     = 1 << 9;
 	public final static int     GetterFunc          = 1 << 10;
 	public final static int     SetterFunc          = 1 << 11;
 	public final static int     OperatorFunc        = 1 << 12;  //@Operator
 	public final static int     ConverterFunc       = 1 << 13;
 	public final static int     CoercionFunc        = 1 << 14;  //@Coercion
 	public final static int		LazyFunc		    = 1 << 15;
 	public final static int     GenericFunc         = 1 << 16;
 
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
 	public final static int	FuncDeclParam		= 4;
 
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
 
 	public final static Object UndefinedSymbol = new Object();   // any class
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
 
 //ifdef JAVA
 }
 
 class GreenTeaUtils implements GreenTeaConsts {
 //endif VAJA
 
 	public final static boolean IsFlag(int flag, int flag2) {
 		return ((flag & flag2) == flag2);
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
 
 	final static String ClassSymbol(GtType ClassType, String Symbol) {
 		return ClassType.GetUniqueName() + "." + Symbol;
 	}
 
 	final static String SafeFuncName(String Symbol) {
 		return LibGreenTea.IsLetter(Symbol, 0) ? Symbol : "__" + Symbol;
 	}
 
 	final static String ClassStaticName(String Symbol) {
 		return "@" + Symbol;
 	}
 
 	final static String ConverterSymbol(GtType ClassType) {
 		return ClassType.GetUniqueName();
 	}
 
 	final static String ConstructorSymbol() {
 		return "";
 	}
 
 	final static String GetterSymbol(String Symbol) {
 		return Symbol + "+";
 	}
 
 	final static String SetterSymbol(String Symbol) {
 		return Symbol + "=";
 	}
 
 	public final static String MangleGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList) {
 		/*local*/String s = BaseType.ShortClassName + NativeNameSuffix;
 		/*local*/int i = BaseIdx;
 		while(i < LibGreenTea.ListSize(TypeList)) {
 			s = s + NumberToAscii(TypeList.get(i).ClassId);
 			i = i + 1;
 		}
 		return s;
 	}
 
 	public final static int ApplyTokenFunc(GtTokenFunc TokenFunc, GtTokenContext TokenContext, String ScriptSource, int Pos) {
 		while(TokenFunc != null) {
 			/*local*/int NextIdx = (/*cast*/int)LibGreenTea.ApplyTokenFunc(TokenFunc.Func, TokenContext, ScriptSource, Pos);
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
 			/*local*/GtSyntaxTree ParsedTree = (/*cast*/GtSyntaxTree)LibGreenTea.ApplyParseFunc(delegate, NameSpace, TokenContext, LeftTree, CurrentPattern);
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
 
 	public final static GtSyntaxTree ParseExpression(GtNameSpace NameSpace, GtTokenContext TokenContext, boolean SuffixOnly) {
 		/*local*/GtSyntaxPattern Pattern = TokenContext.GetFirstPattern(NameSpace);
 		/*local*/GtSyntaxTree LeftTree = GreenTeaUtils.ApplySyntaxPattern(NameSpace, TokenContext, null, Pattern);
 		while(!GreenTeaUtils.IsMismatchedOrError(LeftTree)) {
 			/*local*/GtSyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern(NameSpace);
 			if(ExtendedPattern == null || (SuffixOnly && ExtendedPattern.IsBinaryOperator()) ) {
 				break;
 			}
 			LeftTree = GreenTeaUtils.ApplySyntaxPattern(NameSpace, TokenContext, LeftTree, ExtendedPattern);
 		}
 		return LeftTree;
 	}
 
 	// typing
 	public final static GtNode ApplyTypeFunc(GtFunc TypeFunc, GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		if(TypeFunc != null) {
 			Gamma.NameSpace = ParsedTree.NameSpace;
 			return (/*cast*/GtNode)LibGreenTea.ApplyTypeFunc(TypeFunc, Gamma, ParsedTree, Type);
 		}
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType); 
 	}
 
 	public final static GtNode LinkNode(GtNode LastNode, GtNode Node) {
 		Node.SetPrevNode(LastNode);
 		if(LastNode != null) {
 			LastNode.SetNextNode(Node);
 			if(Node.GetParentNode() != null) {
 				Node.GetParentNode().SetParent(LastNode);
 			}
 		}
 		return Node;
 	}
 
 	public final static GtNode TypeBlock(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/int StackTopIndex = Gamma.StackTopIndex;
 		/*local*/GtNode LastNode = null;
 		while(ParsedTree != null) {
 			/*local*/GtNode Node = GreenTeaUtils.ApplyTypeFunc(ParsedTree.Pattern.TypeFunc, Gamma, ParsedTree, Gamma.VoidType);
 			/*local*/Node = Gamma.TypeCheckSingleNode(ParsedTree, Node, Gamma.VoidType, DefaultTypeCheckPolicy);
 			/*local*/LastNode = GreenTeaUtils.LinkNode(LastNode, Node);
 			if(Node.IsError()) {
 				break;
 			}
 			ParsedTree = ParsedTree.NextTree;
 		}
 		Gamma.PushBackStackIndex(StackTopIndex);
 		if(LastNode == null) {
 			return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 		}
 		return LastNode.MoveHeadNode();
 	}
 
 	public final static boolean AppendTypedNode(ArrayList<GtNode> NodeList, GtNode Node) {
 		NodeList.add(Node);
 		if(Node.IsError()) {
 			NodeList.set(0, Node);
 			return false;
 		}
 		return true;
 	}
 
 	public final static boolean HasErrorNode(ArrayList<GtNode> NodeList) {
 		return (NodeList.size() > 0 && NodeList.get(0).IsError());
 	}
 
 //ifdef JAVA
 }
 
 final class GtMap {
 	final HashMap<String, Object>	Map;
 
 	public GtMap() {
 		this.Map = new HashMap<String, Object>();
 	}
 
 	public final void put(String Key, Object Value) {
 		this.Map.put(Key, Value);
 	}
 
 	public final Object get(String Key) {
 		return this.Map.get(Key);
 	}
 }
 
 //endif VAJA
 
 // tokenizer
 
 final class GtToken extends GreenTeaUtils {
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
 
 	public final boolean IsNextWhiteSpace() {
 		return IsFlag(this.TokenFlag, WhiteSpaceTokenFlag);
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
 
 	public String SetErrorMessage(String Message, GtSyntaxPattern ErrorPattern) {
 		if(this.ParsedText.length() > 0) {  // skip null token
 			this.TokenFlag = ErrorTokenFlag;
 			this.ParsedText = Message;
 			this.PresetPattern = ErrorPattern;
 		}
 		return Message;
 	}
 
 	public String GetErrorMessage() {
 		LibGreenTea.Assert(this.IsError());
 		return this.ParsedText;
 	}
 
 	public final GtToken AddTypeInfoToErrorMessage(GtType ClassType) {
 		this.ParsedText += " of " + ClassType.ShortClassName;
 		return this;
 	}
 
 }
 
 final class GtTokenFunc {
 	/*field*/public GtFunc      Func;
 	/*field*/public GtTokenFunc	ParentFunc;
 
 	GtTokenFunc/*constructor*/(GtFunc Func, GtTokenFunc Parent) {
 		this.Func = Func;
 		this.ParentFunc = Parent;
 	}
 
 	@Override public String toString() {
 		return this.Func.toString();
 	}
 }
 
 final class GtTokenContext extends GreenTeaUtils {
 	/*field*/public final static GtToken NullToken = new GtToken("", 0);
 
 	/*field*/public GtNameSpace TopLevelNameSpace;
 	/*field*/public ArrayList<GtToken> SourceList;
 	/*field*/private int CurrentPosition;
 	/*field*/public long ParsingLine;
 	/*field*/public int  ParseFlag;
 	/*field*/public GtMap ParsingAnnotation;
 	/*field*/public int IndentLevel = 0;
 
 	GtTokenContext/*constructor*/(GtNameSpace NameSpace, String Text, long FileLine) {
 		this.TopLevelNameSpace = NameSpace;
 		this.SourceList = new ArrayList<GtToken>();
 		this.CurrentPosition = 0;
 		this.ParsingLine = FileLine;
 		this.ParseFlag = 0;
 		this.AddNewToken(Text, SourceTokenFlag, null);
 		this.ParsingAnnotation = null;
 		this.IndentLevel = 0;
 	}
 
 	public GtToken AddNewToken(String Text, int TokenFlag, String PatternName) {
 		/*local*/GtToken Token = new GtToken(Text, this.ParsingLine);
 		Token.TokenFlag |= TokenFlag;
 		if(PatternName != null) {
 			Token.PresetPattern = this.TopLevelNameSpace.GetSyntaxPattern(PatternName);
 			LibGreenTea.Assert(Token.PresetPattern != null);
 		}
 		this.SourceList.add(Token);
 		return Token;
 	}
 
 	public void FoundWhiteSpace() {
 		/*local*/GtToken Token = this.SourceList.get(this.SourceList.size() - 1);
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
 
 	public void SkipErrorStatement() {
 		while(this.HasNext()) {
 			GtToken T = this.GetToken();
 			if(T.IsDelim() || T.EqualsText("}")) {
 				break;
 			}
 			this.TopLevelNameSpace.Context.ReportError(InfoLevel, T, "skipping: " + T.ParsedText);
 			this.Next();
 		}		
 	}
 	
 	public GtSyntaxTree ReportTokenError(GtToken Token, String Message, boolean SkipToken) {
 		if(this.IsAllowedBackTrack()) {
 			return null;
 		}
 		else {
 			if(SkipToken) {
 				this.SkipErrorStatement();
 			}
 			return this.NewErrorSyntaxTree(Token, Message);
 		}
 	}
 
 	public GtSyntaxTree ReportExpectedMessage(GtToken Token, String Message, boolean SkipToken) {
 		return this.ReportTokenError(Token, "expected: " + Message + "; given = " + Token.ParsedText, SkipToken);
 	}
 
 	public GtSyntaxTree ReportExpectedToken2(String TokenText) {
 		if(!this.IsAllowedBackTrack()) {
 			/*local*/GtToken Token = this.GetBeforeToken();
 			if(Token != null) {
 				return this.NewErrorSyntaxTree(Token, TokenText + " is expected after " + Token.ParsedText);
 			}
 			Token = this.GetToken();
 			LibGreenTea.Assert(Token != GtTokenContext.NullToken);
 			return this.NewErrorSyntaxTree(Token, TokenText + " is expected at " + Token.ParsedText);
 		}
 		return null;
 	}
 
 	public GtSyntaxTree ReportExpectedPattern(GtSyntaxPattern Pattern) {
 //		if(Pattern == null) {
 //			return this.ReportExpectedToken("null/*if you find this message, it will be bugs*/");  // really ?
 //		}
 		return this.ReportExpectedToken2(Pattern.PatternName);
 	}
 
 	public void Vacume() {
 		if(this.CurrentPosition > 0) {
 			/*local*/int i = this.CurrentPosition;
 			while(i > 0) {
 				LibGreenTea.Assert(this.SourceList.size() > 0);
 				this.SourceList.remove(0);
 				i = i - 1;
 			}
 			this.CurrentPosition = 0;
 		}
 	}
 
 	private int DispatchFunc(String ScriptSource, int GtChar, int pos) {
 		/*local*/GtTokenFunc TokenFunc = this.TopLevelNameSpace.GetTokenFunc(GtChar);
 		/*local*/int NextIdx = GreenTeaUtils.ApplyTokenFunc(TokenFunc, this, ScriptSource, pos);
 		if(NextIdx == MismatchedPosition) {
 			LibGreenTea.VerboseLog(VerboseUndefined, "undefined tokenizer: " + ScriptSource.substring(pos, pos+1));
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
 			/*local*/int gtCode = AsciiToTokenMatrixIndex(LibGreenTea.CharAt(ScriptSource, currentPos));
 			/*local*/int nextPos = this.DispatchFunc(ScriptSource, gtCode, currentPos);
 			if(currentPos >= nextPos) {
 				break;
 			}
 			currentPos = nextPos;
 		}
 		this.Dump();
 	}
 
 	public final int GetPosition(int MatchFlag) {
 		int Pos = this.CurrentPosition;
 		if(IsFlag(MatchFlag, AllowLineFeed)) {
 			this.SkipIndent();
 		}
 		if(IsFlag(MatchFlag, AllowAnnotation)) {
 			//this.PushParsingAnnotation();
 			this.SkipAndGetAnnotation(true);  
 		}
 		return Pos;
 	}
 
 	public final void RollbackPosition(int Pos, int MatchFlag) {
 		this.CurrentPosition = Pos;
 		if(IsFlag(MatchFlag, AllowAnnotation)) {
 			//this.PopParsingAnnotation();
 		}
 	}
 
 	public GtToken GetToken() {
 		while(this.CurrentPosition < this.SourceList.size()) {
 			/*local*/GtToken Token = this.SourceList.get(this.CurrentPosition);
 			if(Token.IsSource()) {
 				this.SourceList.remove(this.SourceList.size()-1);
 				this.Tokenize(Token.ParsedText, Token.FileLine);
 				if(this.CurrentPosition < this.SourceList.size()) {
 					Token = this.SourceList.get(this.CurrentPosition);
 				}else{
 					break;
 				}
 			}
 			if(IsFlag(this.ParseFlag, SkipIndentParseFlag) && Token.IsIndent()) {
 				this.CurrentPosition = this.CurrentPosition + 1;
 				continue;
 			}
 //			this.ParsingLine = Token.FileLine;
 			return Token;
 		}
 //		GtTokenContext.NullToken.FileLine = this.ParsingLine;
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
 		/*local*/GtToken Token = this.GetToken();
 		while(Token.IsIndent()) {
 			this.CurrentPosition = this.CurrentPosition + 1;
 			Token = this.GetToken();
 		}
 	}
 
 	public GtSyntaxPattern GetPattern(String PatternName) {
 		return this.TopLevelNameSpace.GetSyntaxPattern(PatternName);
 	}
 
 	public GtSyntaxPattern GetFirstPattern(GtNameSpace NameSpace) {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token.PresetPattern != null) {
 			return Token.PresetPattern;
 		}
 		/*local*/GtSyntaxPattern Pattern = NameSpace.GetSyntaxPattern(Token.ParsedText);
 		if(Pattern == null) {
 			return NameSpace.GetSyntaxPattern("$Symbol$");
 		}
 		return Pattern;
 	}
 
 	public GtSyntaxPattern GetExtendedPattern(GtNameSpace NameSpace) {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token != GtTokenContext.NullToken) {
 			/*local*/GtSyntaxPattern Pattern = NameSpace.GetExtendedSyntaxPattern(Token.ParsedText);
 			return Pattern;
 		}
 		return null;
 	}
 
 	public final boolean IsToken(String TokenText) {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token.EqualsText(TokenText)) {
 			return true;
 		}
 		return false;
 	}
 
 	public final boolean MatchToken(String TokenText) {
 		if(this.IsToken(TokenText)) {
 			this.CurrentPosition += 1;
 			return true;
 		}
 		return false;
 	}
 
 	public final boolean MatchToken2(String TokenText, int MatchFlag) {
 		/*local*/int Pos = this.GetPosition(MatchFlag);
 		/*local*/GtToken Token = this.Next();
 		if(Token.EqualsText(TokenText)) {
 			return true;
 		}
 		this.RollbackPosition(Pos, MatchFlag);
 		return false;
 	}
 
 	public final boolean StartsWithToken(String TokenText) {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token.EqualsText(TokenText)) {
 			this.CurrentPosition += 1;
 			return true;
 		}
 		if(Token.ParsedText.startsWith(TokenText)) {
 			Token = new GtToken(Token.ParsedText.substring(TokenText.length()), Token.FileLine);
 			this.CurrentPosition += 1;
 			this.SourceList.add(this.CurrentPosition, Token);
 			return true;
 		}
 		return false;
 	}
 
 	public GtSyntaxTree CreateSyntaxTree(GtNameSpace NameSpace, Object Pattern, Object ConstValue) {
 		if(ConstValue != null) {
 			Pattern = NameSpace.GetSyntaxPattern("$Const$");
 		}
 		if(Pattern instanceof String) {
 			Pattern = NameSpace.GetSyntaxPattern(Pattern.toString());
 		}
 		return new GtSyntaxTree((/*cast*/GtSyntaxPattern)Pattern, NameSpace, this.GetToken(), ConstValue);
 	}
 
 	public GtSyntaxTree CreateMatchedSyntaxTree(GtNameSpace NameSpace, GtSyntaxPattern Pattern, String TokenText) {
 		GtSyntaxTree SyntaxTree = this.CreateSyntaxTree(NameSpace, Pattern, null);
 		SyntaxTree.SetMatchedTokenAt(KeyTokenIndex, NameSpace, this, TokenText, Required);
 		return SyntaxTree;
 	}
 
 	public final boolean IsAllowedBackTrack() {
 		return IsFlag(this.ParseFlag, BackTrackParseFlag);
 	}
 
 	public final int SetBackTrack(boolean Allowed) {
 		/*local*/int OldFlag = this.ParseFlag;
 		if(Allowed) {
 			this.ParseFlag = this.ParseFlag | BackTrackParseFlag;
 		}
 		else {
 			this.ParseFlag = (~(BackTrackParseFlag) & this.ParseFlag);
 		}
 		return OldFlag;
 	}
 
 	public final int SetSkipIndent(boolean Allowed) {
 		/*local*/int OldFlag = this.ParseFlag;
 		if(Allowed) {
 			this.ParseFlag = this.ParseFlag | SkipIndentParseFlag;
 		}
 		else {
 			this.ParseFlag = (~(SkipIndentParseFlag) & this.ParseFlag);
 		}
 		return OldFlag;
 	}
 
 	public final void SetRememberFlag(int OldFlag) {
 		this.ParseFlag = OldFlag;
 	}
 
 	public final GtSyntaxTree ParsePatternAfter(GtNameSpace NameSpace, GtSyntaxTree LeftTree, String PatternName, int MatchFlag) {
 		/*local*/int Pos = this.GetPosition(MatchFlag);
 		/*local*/int ParseFlag = this.ParseFlag;
 		if(IsFlag(MatchFlag, Optional)) {
 			this.ParseFlag = this.ParseFlag | BackTrackParseFlag;
 		}
 		/*local*/GtSyntaxPattern Pattern = this.GetPattern(PatternName);
 		/*local*/GtSyntaxTree SyntaxTree = GreenTeaUtils.ApplySyntaxPattern(NameSpace, this, LeftTree, Pattern);
 		this.ParseFlag = ParseFlag;
 		if(SyntaxTree != null) {
 			return SyntaxTree;
 		}
 		this.RollbackPosition(Pos, MatchFlag);
 		return null;
 	}
 
 	public final GtSyntaxTree ParsePattern(GtNameSpace NameSpace, String PatternName, int MatchFlag) {
 		return this.ParsePatternAfter(NameSpace, null, PatternName, MatchFlag);
 	}
 
 	public final GtMap SkipAndGetAnnotation(boolean IsAllowedDelim) {
 		// this is tentative implementation. In the future, you have to
 		// use this pattern.
 		this.ParsingAnnotation = null;
 		this.SkipEmptyStatement();
 		while(this.MatchToken("@")) {
 			/*local*/GtToken Token = this.Next();
 			if(this.ParsingAnnotation == null) {
 				this.ParsingAnnotation = new GtMap();
 			}
 			this.ParsingAnnotation.put(Token.ParsedText, true);
 			this.SkipIndent();
 //			if(this.MatchToken(";")) {
 //				if(IsAllowedDelim) {
 //					Annotation = null; // empty statement
 //					this.();
 //				}
 //				else {
 //					return null;
 //				}
 //			}
 		}
 		return this.ParsingAnnotation;
 	}
 
 	public final void SkipEmptyStatement() {
 		while(this.HasNext()) {
 			/*local*/GtToken Token = this.GetToken();
 			if(Token.IsIndent() || Token.IsDelim()) {
 				this.CurrentPosition += 1;
 				continue;
 			}
 			break;
 		}
 //		return (Token != GtTokenContext.NullToken);
 	}
 
 	public final void SkipIncompleteStatement() {
 //		if(this.HasNext()) {
 //			/*local*/GtToken Token = this.GetToken();
 //			if(!Token.IsIndent() && !Token.IsDelim()) {
 //				this.TopLevelNameSpace.Context.ReportError(WarningLevel, Token, "needs ;");
 //				if(Token.EqualsText("}")) {
 //					return;
 //				}
 //				this.CurrentPosition += 1;
 //				while(this.HasNext()) {
 //					Token = this.GetToken();
 //					if(Token.IsIndent() || Token.IsDelim()) {
 //						break;
 //					}
 //					if(Token.EqualsText("}")) {
 //						return;
 //					}
 //					this.CurrentPosition += 1;
 //				}
 //			}
 			this.SkipEmptyStatement();
 //		}
 	}
 
 	public final String Stringfy(String PreText, int BeginIdx, int EndIdx) {
 		/*local*/String Buffer = PreText;
 		/*local*/int Position = BeginIdx;
 		while(Position < EndIdx) {
 			/*local*/GtToken Token = this.SourceList.get(Position);
 			if(Token.IsIndent()) {
 				Buffer += "\n";
 			}
 			Buffer += Token.ParsedText;
 			if(Token.IsNextWhiteSpace()) {
 				Buffer += " ";
 			}
 			Position += 1;
 		}
 		return Buffer;
 	}
 
 	public final void Dump() {
 		/*local*/int Position = this.CurrentPosition;
 		while(Position < this.SourceList.size()) {
 			/*local*/GtToken Token = this.SourceList.get(Position);
 			/*local*/String DumpedToken = "["+Position+"] " + Token;
 			if(Token.PresetPattern != null) {
 				DumpedToken = DumpedToken + " : " + Token.PresetPattern;
 			}
 			LibGreenTea.VerboseLog(VerboseToken,  DumpedToken);
 			Position += 1;
 		}
 	}
 
 	public final void SetSourceMap(String SourceMap) {
 		/*local*/int Index = SourceMap.lastIndexOf(":");
 		if(Index != -1) {
 			/*local*/String FileName = SourceMap.substring(0, Index);
 			/*local*/int Line = (/*cast*/int)LibGreenTea.ParseInt(SourceMap.substring(Index+1));
 			this.ParsingLine = this.TopLevelNameSpace.Context.GetFileLine(FileName, Line);
 		}
 	}
 
 
 }
 
 final class GtSyntaxPattern extends GreenTeaUtils {
 	/*field*/public GtNameSpace	          PackageNameSpace;
 	/*field*/public String		          PatternName;
 	/*field*/int				          SyntaxFlag;
 	/*field*/public GtFunc       MatchFunc;
 	/*field*/public GtFunc            TypeFunc;
 	/*field*/public GtSyntaxPattern	      ParentPattern;
 
 	GtSyntaxPattern/*constructor*/(GtNameSpace NameSpace, String PatternName, GtFunc MatchFunc, GtFunc TypeFunc) {
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
 
 	public final boolean IsRightJoin(GtSyntaxPattern Right) {
 		/*local*/int left = this.SyntaxFlag;
 		/*local*/int right = Right.SyntaxFlag;
 		return (left < right || (left == right && !IsFlag(this.SyntaxFlag, LeftJoin) && !IsFlag(Right.SyntaxFlag, LeftJoin)));
 	}
 
 	public final boolean EqualsName(String Name) {
 		return LibGreenTea.EqualsString(this.PatternName, Name);
 	}
 
 }
 
 class GtSyntaxTree extends GreenTeaUtils {
 	/*field*/public GtMap               Annotation;
 	/*field*/public GtSyntaxTree		ParentTree;
 	/*field*/public GtSyntaxTree		PrevTree;
 	/*field*/public GtSyntaxTree		NextTree;
 
 	/*field*/public GtNameSpace	             NameSpace;
 	/*field*/public GtSyntaxPattern	         Pattern;
 	/*field*/public GtToken		             KeyToken;
 	/*field*/public ArrayList<GtSyntaxTree>  SubTreeList;
 	/*field*/public Object                   ParsedValue;
 
 	GtSyntaxTree/*constructor*/(GtSyntaxPattern Pattern, GtNameSpace NameSpace, GtToken KeyToken, Object ParsedValue) {
 		this.NameSpace   = NameSpace;
 		this.Annotation  = null;
 		this.KeyToken    = KeyToken;
 		this.Pattern     = Pattern;
 		this.ParentTree  = null;
 		this.PrevTree    = null;
 		this.NextTree    = null;
 		this.SubTreeList = null;
 		this.ParsedValue  = ParsedValue;
 	}
 
 	@Override public String toString() {
 		/*local*/String s = "(" + this.KeyToken.ParsedText;
 		/*local*/int i = 0;
 		while(i < LibGreenTea.ListSize(this.SubTreeList)) {
 			/*local*/GtSyntaxTree SubTree = this.SubTreeList.get(i);
 			while(SubTree != null) {
 				/*local*/String Entry = SubTree.toString();
 				if(LibGreenTea.ListSize(SubTree.SubTreeList) == 0) {
 					Entry = SubTree.KeyToken.ParsedText;
 				}
 				s = s + " " + Entry;
 				SubTree = SubTree.NextTree;
 			}
 			i += 1;
 		}
 		return s + ")";
 	}
 
 	public final void AppendNext(GtSyntaxTree Tree) {
 		/*local*/GtSyntaxTree TailTree = this;
 		while(TailTree.NextTree != null) {
 			TailTree = TailTree.NextTree;
 		}
 		TailTree.NextTree = Tree;
 	}
 
 	public void SetAnnotation(GtMap Annotation) {
 		this.Annotation = Annotation;
 	}
 
 	public boolean IsError() {
 		return this.KeyToken.IsError();
 	}
 
 	public void ToError(GtToken Token) {
 		LibGreenTea.Assert(Token.IsError());
 		this.KeyToken = Token;
 		this.SubTreeList = null;
 		this.Pattern = Token.PresetPattern;		
 	}
 
 	public boolean IsMismatched() {
 		return (this.Pattern == null);
 	}
 
 	public void ToMismatched() {
 		this.SubTreeList = null;
 		this.Pattern = null; // Empty tree must backtrack
 	}
 
 	public boolean IsMismatchedOrError() {
 		return this.IsMismatched() || this.KeyToken.IsError();
 	}
 
 	public final boolean IsValidSyntax() {
 		return !(this.IsMismatchedOrError());
 	}
 
 	public void ToEmptyOrError(GtSyntaxTree ErrorTree) {
 		if(ErrorTree == null) {
 			this.ToMismatched();
 		}
 		else {
 			this.ToError(ErrorTree.KeyToken);
 		}
 	}
 
 	public GtSyntaxTree GetSyntaxTreeAt(int Index) {
 		if(this.SubTreeList != null && Index >= this.SubTreeList.size()) {
 			return null;
 		}
 		return this.SubTreeList.get(Index);
 	}
 
 	public void SetSyntaxTreeAt(int Index, GtSyntaxTree Tree) {
 		if(!this.IsError()) {
 			if(Tree.IsError()) {
 				this.ToError(Tree.KeyToken);
 			}
 			else {
 				if(Index >= 0) {
 					if(this.SubTreeList == null) {
 						this.SubTreeList = new ArrayList<GtSyntaxTree>();
 					}
 					if(Index < this.SubTreeList.size()) {
 						this.SubTreeList.set(Index, Tree);
 						return;
 					}
 					while(this.SubTreeList.size() < Index) {
 						this.SubTreeList.add(null);
 					}
 					this.SubTreeList.add(Tree);
 					Tree.ParentTree = this;
 				}
 			}
 		}
 	}
 
 	public void SetMatchedPatternAt(int Index, GtNameSpace NameSpace, GtTokenContext TokenContext, String PatternName,  int MatchFlag) {
 		if(!this.IsMismatchedOrError()) {
 			/*local*/GtSyntaxTree ParsedTree = TokenContext.ParsePattern(NameSpace, PatternName, MatchFlag);
 			if(ParsedTree != null) {
 				this.SetSyntaxTreeAt(Index, ParsedTree);
 			}
 			else {
 				if(IsFlag(MatchFlag, Required)) {
 					this.ToMismatched();
 				}
 			}
 		}
 	}
 
 	public void SetMatchedTokenAt(int Index, GtNameSpace NameSpace, GtTokenContext TokenContext, String TokenText, int MatchFlag) {
 		if(!this.IsMismatchedOrError()) {
 			/*local*/int Pos = TokenContext.GetPosition(MatchFlag);
 			/*local*/GtToken Token = TokenContext.Next();
 			if(Token.ParsedText.equals(TokenText)) {
 				if(Index == KeyTokenIndex) {
 					this.KeyToken = Token;
 				}
 				else if(Index != NoWhere) {
 					this.SetSyntaxTreeAt(Index, new GtSyntaxTree(null, NameSpace, Token, null));
 				}
 				if(IsFlag(MatchFlag, OpenSkipIndent)) {
 					TokenContext.SetSkipIndent(true);
 				}
 				if(IsFlag(MatchFlag, CloseSkipIndent)) {
 					TokenContext.SetSkipIndent(false);
 				}
 			}
 			else {
 				TokenContext.RollbackPosition(Pos, MatchFlag);
 				if(IsFlag(MatchFlag, Required)) {
 					this.ToEmptyOrError(TokenContext.ReportExpectedToken2(TokenText));
 				}
 			}
 		}
 	}
 
 	public void AppendParsedTree2(GtSyntaxTree Tree) {
 		if(!this.IsError()) {
 			LibGreenTea.Assert(Tree != null);
 			if(Tree.IsError()) {
 				this.ToError(Tree.KeyToken);
 			}
 			else {
 				if(this.SubTreeList == null) {
 					this.SubTreeList = new ArrayList<GtSyntaxTree>();
 				}
 				this.SubTreeList.add(Tree);
 			}
 		}
 	}
 
 	public void AppendMatchedPattern(GtNameSpace NameSpace, GtTokenContext TokenContext, String PatternName,  int MatchFlag) {
 		if(!this.IsMismatchedOrError()) {
 			/*local*/GtSyntaxTree ParsedTree = TokenContext.ParsePattern(NameSpace, PatternName, MatchFlag);
 			if(ParsedTree != null) {
 				this.AppendParsedTree2(ParsedTree);
 			}
 			else {
 				if(IsFlag(MatchFlag, Required)) {
 					this.ToMismatched();
 				}
 			}
 		}
 	}
 
 	public final GtType GetParsedType() {
 		return (this.ParsedValue instanceof GtType) ? (/*cast*/GtType)this.ParsedValue : null;
 	}
 
 	public final boolean HasNodeAt(int Index) {
 		if(this.SubTreeList != null && Index < this.SubTreeList.size()) {
 			return this.SubTreeList.get(Index) != null;
 		}
 		return false;
 	}
 
 	public GtNode TypeCheck(GtTypeEnv Gamma, GtType ContextType, int TypeCheckPolicy) {
 		/*local*/GtNode Node = GreenTeaUtils.ApplyTypeFunc(this.Pattern.TypeFunc, Gamma, this, ContextType);
 		return Gamma.TypeCheckSingleNode(this, Node, ContextType, TypeCheckPolicy);
 	}
 
 	public final GtNode TypeCheckAt(int Index, GtTypeEnv Gamma, GtType ContextType, int TypeCheckPolicy) {
 		/*local*/GtSyntaxTree ParsedTree = this.GetSyntaxTreeAt(Index);
 		if(ContextType == Gamma.VoidType || IsFlag(TypeCheckPolicy, BlockPolicy)) {
 			return GreenTeaUtils.TypeBlock(Gamma, ParsedTree, ContextType);
 		}
 		else if(ParsedTree != null) {
 			return ParsedTree.TypeCheck(Gamma, ContextType, TypeCheckPolicy);
 		}
 		return Gamma.CreateSyntaxErrorNode(this, "not empty");
 	}
 
 	public final void TypeCheckParam(GtTypeEnv Gamma, int TreeIndex, ArrayList<GtNode> NodeList) {
 		while(TreeIndex < LibGreenTea.ListSize(this.SubTreeList)) {
 			/*local*/GtNode Node = this.TypeCheckAt(TreeIndex, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 			GreenTeaUtils.AppendTypedNode(NodeList, Node);
 			TreeIndex = TreeIndex + 1;
 		}
 	}
 
 	public final void ToConstTree(Object ConstValue) {
 		this.Pattern = this.NameSpace.GetSyntaxPattern("$Const$");
 		this.ParsedValue = ConstValue;
 	}
 
 	public final GtSyntaxTree CreateConstTree(Object ConstValue) {
 		return new GtSyntaxTree(this.NameSpace.GetSyntaxPattern("$Const$"), this.NameSpace, this.KeyToken, ConstValue);
 	}
 
 }
 
 /* typing */
 class GtFieldInfo extends GreenTeaUtils {
 	/*field*/public int     FieldFlag;
 	/*field*/public int     FieldIndex;
 	/*field*/public GtType	Type;
 	/*field*/public String	Name;
 	/*field*/public String	NativeName;
 	/*field*/public Object  InitValue;
 	/*field*/public GtFunc	GetterFunc;
 	/*field*/public GtFunc	SetterFunc;
 
 	GtFieldInfo/*constructor*/(int FieldFlag, GtType Type, String Name, int FieldIndex, Object InitValue) {
 		this.FieldFlag = FieldFlag;
 		this.Type = Type;
 		this.Name = Name;
 		this.NativeName = Name; // use this in a generator
 		this.FieldIndex = FieldIndex;
 		this.InitValue = InitValue;
 		this.GetterFunc = null;
 		this.SetterFunc = null;
 	}
 }
 
 final class GtClassField extends GreenTeaUtils {
 	/*field*/ public GtType DefinedType;
 	/*field*/ private GtNameSpace NameSpace;
 	/*field*/ public ArrayList<GtFieldInfo> FieldList;
 	/*field*/ public int ThisClassIndex;
 
 	GtClassField/*constructor*/(GtType DefinedType, GtNameSpace NameSpace) {
 		this.DefinedType = DefinedType;
 		this.NameSpace = NameSpace;
 		this.FieldList = new ArrayList<GtFieldInfo>();
 		/*local*/GtType SuperClass = DefinedType.SuperType;
 		if(SuperClass.TypeBody instanceof GtClassField) {
 			/*local*/GtClassField SuperField = (/*cast*/GtClassField)SuperClass.TypeBody;
 			/*local*/int i = 0;
 			while(i < SuperField.FieldList.size()) {
 				this.FieldList.add(SuperField.FieldList.get(i));
 				i+=1;
 			}
 		}
 		this.ThisClassIndex = this.FieldList.size();
 	}
 
 	public GtFieldInfo CreateField(int FieldFlag, GtType Type, String Name, GtToken SourceToken, Object InitValue) {
 		/*local*/int i = 0;
 		while(i < this.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = this.FieldList.get(i);
 			if(FieldInfo.Name.equals(Name)) {
 				Type.Context.ReportError(WarningLevel, SourceToken, "duplicated field: " + Name);
 				return null;
 			}
 			i = i + 1;
 		}
 		/*local*/GtFieldInfo FieldInfo = new GtFieldInfo(FieldFlag, Type, Name, this.FieldList.size(), InitValue);
 		/*local*/ArrayList<GtType> ParamList = new ArrayList<GtType>();
 		ParamList.add(FieldInfo.Type);
 		ParamList.add(this.DefinedType);
 		FieldInfo.GetterFunc = new GtFunc(GetterFunc, FieldInfo.Name, 0, ParamList);
 		this.NameSpace.SetGetterFunc(this.DefinedType, FieldInfo.Name, FieldInfo.GetterFunc, SourceToken);
 		ParamList.clear();
 		ParamList.add(Type.Context.VoidType);
 		ParamList.add(this.DefinedType);
 		ParamList.add(FieldInfo.Type);
 		FieldInfo.SetterFunc = new GtFunc(SetterFunc, FieldInfo.Name, 0, ParamList);
 		this.NameSpace.SetSetterFunc(this.DefinedType, FieldInfo.Name, FieldInfo.SetterFunc, SourceToken);
 		this.FieldList.add(FieldInfo);
 		return FieldInfo;
 	}
 
 }
 
 class GtVariableInfo extends GreenTeaUtils {
 	/*field*/public int     VariableFlag;
 	/*field*/public GtType	Type;
 	/*field*/public String	Name;
 	/*field*/public String	NativeName;
 	/*field*/public GtToken NameToken;
 	/*field*/public Object  InitValue;
 	/*field*/public int     DefCount;
 	/*field*/public int     UsedCount;
 
 	GtVariableInfo/*constructor*/(int VarFlag, GtType Type, String Name, int Index, GtToken NameToken, Object InitValue) {
 		this.VariableFlag = VarFlag;
 		this.Type = Type;
 		this.NameToken = NameToken;
 		this.Name = Name;
 		this.NativeName = (NameToken == null) ? Name : GreenTeaUtils.NativeVariableName(Name, Index);
 		this.InitValue = null;
 		this.UsedCount = 0;
 		this.DefCount  = 1;
 	}
 
 	public final void Defined() {
 		this.DefCount += 1;
 		this.InitValue = null;
 	}
 
 	public final void Used() {
 		this.UsedCount += 1;
 	}
 
 	public void Check() {
 		if(this.UsedCount == 0 && this.NameToken != null) {
 			this.Type.Context.ReportError(WarningLevel, this.NameToken, "unused variable: " + this.Name);
 		}
 	}
 	// for debug
 	@Override public String toString() {
 		return "(" + this.Type + " " + this.Name + ", " + this.NativeName + ")";
 	}
 }
 
 final class GtTypeEnv extends GreenTeaUtils {
 	/*field*/public final GtParserContext    Context;
 	/*field*/public final GtGenerator       Generator;
 	/*field*/public GtNameSpace	    NameSpace;
 
 	/*field*/public GtFunc	Func;
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
 		this.Func = null;
 		this.LocalStackList = new ArrayList<GtVariableInfo>();
 		this.StackTopIndex = 0;
 
 		this.VoidType    = NameSpace.Context.VoidType;
 		this.BooleanType = NameSpace.Context.BooleanType;
 		this.IntType     = NameSpace.Context.IntType;
 		this.StringType  = NameSpace.Context.StringType;
 		this.VarType     = NameSpace.Context.VarType;
 		this.AnyType     = NameSpace.Context.AnyType;
 		this.ArrayType   = NameSpace.Context.ArrayType;
 		this.FuncType    = NameSpace.Context.FuncType;
 	}
 
 	public final boolean IsStrictMode() {
 		return this.Generator.IsStrictMode();
 	}
 
 	public final boolean IsTopLevel() {
 		return (this.Func == null);
 	}
 
 	public void AppendRecv(GtType RecvType) {
 		/*local*/String ThisName = this.Generator.GetRecvName();
 		this.AppendDeclaredVariable(0, RecvType, ThisName, null, null);
 		this.LocalStackList.get(this.StackTopIndex-1).NativeName = ThisName;
 	}
 
 	public GtVariableInfo AppendDeclaredVariable(int VarFlag, GtType Type, String Name, GtToken NameToken, Object InitValue) {
 		/*local*/GtVariableInfo VarInfo = new GtVariableInfo(VarFlag, Type, Name, this.StackTopIndex, NameToken, InitValue);
 		if(this.StackTopIndex < this.LocalStackList.size()) {
 			this.LocalStackList.set(this.StackTopIndex, VarInfo);
 		}
 		else {
 			this.LocalStackList.add(VarInfo);
 		}
 		this.StackTopIndex += 1;
 		return VarInfo;
 	}
 
 	public GtVariableInfo LookupDeclaredVariable(String Symbol) {
 		/*local*/int i = this.StackTopIndex - 1;
 		while(i >= 0) {
 			/*local*/GtVariableInfo VarInfo = this.LocalStackList.get(i);
 			if(VarInfo.Name.equals(Symbol)) {
 				return VarInfo;
 			}
 			i = i - 1;
 		}
 		return null;
 	}
 
 	public void PushBackStackIndex(int PushBackIndex) {
 		/*local*/int i = this.StackTopIndex - 1;
 		while(i >= PushBackIndex) {
 			/*local*/GtVariableInfo VarInfo = this.LocalStackList.get(i);
 			VarInfo.Check();
 			i = i - 1;
 		}
 		this.StackTopIndex = PushBackIndex;
 	}
 
 	public final GtNode CreateCoercionNode(GtType Type, GtFunc Func, GtNode Node) {
 		/*local*/GtNode ApplyNode = this.Generator.CreateApplyNode(Type, null, Func);
 		/*local*/GtNode TypeNode = this.Generator.CreateConstNode(Type.Context.TypeType, null, Type);
 		ApplyNode.Append(TypeNode);
 		ApplyNode.Append(TypeNode);
 		ApplyNode.Append(Node);
 		return ApplyNode;
 	}
 
 	public final GtNode ReportTypeResult(GtSyntaxTree ParsedTree, GtNode Node, int Level, String Message) {
 		if(Level == ErrorLevel || (this.IsStrictMode() && Level == TypeErrorLevel)) {
 			LibGreenTea.Assert(Node.Token == ParsedTree.KeyToken);
 			this.NameSpace.Context.ReportError(ErrorLevel, Node.Token, Message);
 			return this.Generator.CreateErrorNode(this.VoidType, ParsedTree);
 		}
 		else {
 			this.NameSpace.Context.ReportError(Level, Node.Token, Message);
 		}
 		return Node;
 	}
 
 	public final void ReportTypeInference(GtToken SourceToken, String Name, GtType InfferedType) {
 		this.Context.ReportError(InfoLevel, SourceToken, Name + " has type " + InfferedType);
 	}
 
 	public final GtNode CreateSyntaxErrorNode(GtSyntaxTree ParsedTree, String Message) {
 		this.NameSpace.Context.ReportError(ErrorLevel, ParsedTree.KeyToken, Message);
 		return this.Generator.CreateErrorNode(this.VoidType, ParsedTree);
 	}
 
 	public final GtNode UnsupportedTopLevelError(GtSyntaxTree ParsedTree) {
 		return this.CreateSyntaxErrorNode(ParsedTree, "unsupported " + ParsedTree.Pattern.PatternName + " at the top level");
 	}
 
 	public final GtNode CreateLocalNode(GtSyntaxTree ParsedTree, String Name) {
 		/*local*/GtVariableInfo VariableInfo = this.LookupDeclaredVariable(Name);
 		if(VariableInfo != null) {
 			return this.Generator.CreateLocalNode(VariableInfo.Type, ParsedTree, VariableInfo.NativeName);
 		}
 		return this.CreateSyntaxErrorNode(ParsedTree, "unresolved name: " + Name + "; not your fault");
 	}
 
 	public final GtNode CreateDefaultValue(GtSyntaxTree ParsedTree, GtType Type) {
 		return this.Generator.CreateConstNode(Type, ParsedTree, Type.DefaultNullValue);
 	}
 
 	public final GtNode TypeCheckSingleNode(GtSyntaxTree ParsedTree, GtNode Node, GtType Type, int TypeCheckPolicy) {
 		LibGreenTea.Assert(Node != null);
 		if(Node.IsError() || IsFlag(TypeCheckPolicy, NoCheckPolicy)) {
 			return Node;
 		}
 //		System.err.println("**** " + Node.getClass());
 		/*local*/Object ConstValue = Node.ToConstValue(IsFlag(TypeCheckPolicy, OnlyConstPolicy));
 		if(ConstValue != null && !(Node instanceof ConstNode)) {  // recreated
 			Node = this.Generator.CreateConstNode(Node.Type, ParsedTree, ConstValue);
 		}
 		if(IsFlag(TypeCheckPolicy, OnlyConstPolicy) && ConstValue == null) {
 			if(IsFlag(TypeCheckPolicy, NullablePolicy) && Node instanceof NullNode) { // OK
 			}
 			else {
 				return this.CreateSyntaxErrorNode(ParsedTree, "value must be const");
 			}
 		}
 		if(IsFlag(TypeCheckPolicy, AllowVoidPolicy) || Type == this.VoidType) {
 			return Node;
 		}
 		if(Node.Type == this.VarType) {
 			return this.ReportTypeResult(ParsedTree, Node, TypeErrorLevel, "unspecified type: " + Node.Token.ParsedText);
 		}
 		if(Node.Type == Type || Type == this.VarType || Type.Accept(Node.Type)) {
 			return Node;
 		}
 		/*local*/GtFunc Func = ParsedTree.NameSpace.GetConverterFunc(Node.Type, Type, true);
 		if(Func != null && (Func.Is(CoercionFunc) || IsFlag(TypeCheckPolicy, CastPolicy))) {
 			return this.CreateCoercionNode(Type, Func, Node);
 		}
 		return this.ReportTypeResult(ParsedTree, Node, TypeErrorLevel, "type error: requested = " + Type + ", given = " + Node.Type);
 	}
 }
 
 // NameSpace
 
 final class GtNameSpace extends GreenTeaUtils {
 	/*field*/public final GtParserContext		Context;
 	/*field*/public final GtNameSpace		    ParentNameSpace;
 	/*field*/public String                      PackageName;
 
 	/*field*/GtTokenFunc[] TokenMatrix;
 	/*field*/GtMap	 SymbolPatternTable;
 
 	GtNameSpace/*constructor*/(GtParserContext Context, GtNameSpace ParentNameSpace) {
 		this.Context = Context;
 		this.ParentNameSpace = ParentNameSpace;
 		this.PackageName = (ParentNameSpace != null) ? ParentNameSpace.PackageName : null;
 		this.TokenMatrix = null;
 		this.SymbolPatternTable = null;
 	}
 
 	public final GtNameSpace GetNameSpace(int NameSpaceFlag) {
 		if(IsFlag(NameSpaceFlag, RootNameSpace)) {
 			return this.Context.RootNameSpace;
 		}
 		if(IsFlag(NameSpaceFlag, PublicNameSpace)) {
 			return this.ParentNameSpace;
 		}
 		return this;
 	}
 
 	public final GtTokenFunc GetTokenFunc(int GtChar2) {
 		if(this.TokenMatrix == null) {
 			return this.ParentNameSpace.GetTokenFunc(GtChar2);
 		}
 		return this.TokenMatrix[GtChar2];
 	}
 
 	private final GtTokenFunc JoinParentFunc(GtFunc Func, GtTokenFunc Parent) {
 		if(Parent != null && Parent.Func == Func) {
 			return Parent;
 		}
 		return new GtTokenFunc(Func, Parent);
 	}
 
 	public final void AppendTokenFunc(String keys, GtFunc TokenFunc) {
 		/*local*/int i = 0;
 		if(this.TokenMatrix == null) {
 			this.TokenMatrix = new GtTokenFunc[MaxSizeOfChars];
 			if(this.ParentNameSpace != null) {
 				while(i < MaxSizeOfChars) {
 					this.TokenMatrix[i] = this.ParentNameSpace.GetTokenFunc(i);
 				}
 			}
 		}
 		i = 0;
 		while(i < keys.length()) {
 			/*local*/int kchar = GreenTeaUtils.AsciiToTokenMatrixIndex(LibGreenTea.CharAt(keys, i));
 			this.TokenMatrix[kchar] = this.JoinParentFunc(TokenFunc, this.TokenMatrix[kchar]);
 			i += 1;
 		}
 	}
 
 	public final Object GetLocalUndefinedSymbol(String Key) {
 		if(this.SymbolPatternTable != null) {
 			return this.SymbolPatternTable.get(Key);
 		}
 		return null;
 	}
 
 	public final Object GetLocalSymbol(String Key) {
 		if(this.SymbolPatternTable != null) {
 			/*local*/Object Value = this.SymbolPatternTable.get(Key);
 			if(Value != null) {
 				return Value == UndefinedSymbol ? null : Value;
 			}
 		}
 		return null;
 	}
 
 	public final Object GetSymbol(String Key) {
 		/*local*/GtNameSpace NameSpace = this;
 		while(NameSpace != null) {
 			if(NameSpace.SymbolPatternTable != null) {
 				/*local*/Object Value = NameSpace.SymbolPatternTable.get(Key);
 				if(Value != null) {
 					return Value == UndefinedSymbol ? null : Value;
 				}
 			}
 			NameSpace = NameSpace.ParentNameSpace;
 		}
 		return null;
 	}
 
 	public final boolean HasSymbol(String Key) {
 		return (this.GetSymbol(Key) != null);
 	}
 
 	public final void SetSymbol(String Key, Object Value, GtToken SourceToken) {
 		if(this.SymbolPatternTable == null) {
 			this.SymbolPatternTable = new GtMap();
 		}
 		if(SourceToken != null) {
 			/*local*/Object OldValue = this.SymbolPatternTable.get(Key);
 			if(OldValue != null) {
 				if(LibGreenTea.DebugMode) {
 					this.Context.ReportError(WarningLevel, SourceToken, "duplicated symbol: " + SourceToken + " oldnew=" + OldValue + ", " + Value);
 				}
 				else {
 					this.Context.ReportError(WarningLevel, SourceToken, "duplicated symbol: " + SourceToken);
 				}
 			}
 		}
 		this.SymbolPatternTable.put(Key, Value);
 		LibGreenTea.VerboseLog(VerboseSymbol, "symbol: " + Key + ", " + Value);
 	}
 
 	public final void SetUndefinedSymbol(String Symbol, GtToken SourceToken) {
 		this.SetSymbol(Symbol, UndefinedSymbol, SourceToken);
 	}
 
 	public GtSyntaxPattern GetSyntaxPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol(PatternName);
 		if(Body instanceof GtSyntaxPattern) {
 			return (/*cast*/GtSyntaxPattern)Body;
 		}
 		return null;
 	}
 
 	public GtSyntaxPattern GetExtendedSyntaxPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol("\t" + PatternName);
 		if(Body instanceof GtSyntaxPattern) {
 			return (/*cast*/GtSyntaxPattern)Body;
 		}
 		return null;
 	}
 
 	private void AppendSyntaxPattern(String PatternName, GtSyntaxPattern NewPattern, GtToken SourceToken) {
 		LibGreenTea.Assert(NewPattern.ParentPattern == null);
 		/*local*/GtSyntaxPattern ParentPattern = this.GetSyntaxPattern(PatternName);
 		NewPattern.ParentPattern = ParentPattern;
 		this.SetSymbol(PatternName, NewPattern, SourceToken);
 	}
 
 	public void AppendSyntax(String PatternName, GtFunc MatchFunc, GtFunc TypeFunc) {
 		/*local*/int Alias = PatternName.indexOf(" ");
 		/*local*/String Name = (Alias == -1) ? PatternName : PatternName.substring(0, Alias);
 		/*local*/GtSyntaxPattern Pattern = new GtSyntaxPattern(this, Name, MatchFunc, TypeFunc);
 		this.AppendSyntaxPattern(Name, Pattern, null);
 		if(Alias != -1) {
 			this.AppendSyntax(PatternName.substring(Alias+1), MatchFunc, TypeFunc);
 		}
 	}
 
 	public void AppendExtendedSyntax(String PatternName, int SyntaxFlag, GtFunc MatchFunc, GtFunc TypeFunc) {
 		/*local*/int Alias = PatternName.indexOf(" ");
 		/*local*/String Name = (Alias == -1) ? PatternName : PatternName.substring(0, Alias);
 		/*local*/GtSyntaxPattern Pattern = new GtSyntaxPattern(this, Name, MatchFunc, TypeFunc);
 		Pattern.SyntaxFlag = SyntaxFlag;
 		this.AppendSyntaxPattern("\t" + Name, Pattern, null);
 		if(Alias != -1) {
 			this.AppendExtendedSyntax(PatternName.substring(Alias+1), SyntaxFlag, MatchFunc, TypeFunc);
 		}
 	}
 
 	public final GtType GetType(String TypeName) {
 		/*local*/Object TypeInfo = this.GetSymbol(TypeName);
 		if(TypeInfo instanceof GtType) {
 			return (/*cast*/GtType)TypeInfo;
 		}
 		return null;
 	}
 
 	public final GtType AppendTypeName(GtType Type, GtToken SourceToken) {
 		if(Type.PackageNameSpace == null) {
 			Type.PackageNameSpace = this;
 			if(this.PackageName != null) {
 				this.Context.SetNativeTypeName(this.PackageName + "." + Type.ShortClassName, Type);
 			}
 		}
 		if(Type.BaseType == Type) {
 			this.SetSymbol(Type.ShortClassName, Type, SourceToken);
 		}
 		return Type;
 	}
 
 	public final Object GetClassSymbol(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		while(ClassType != null) {
 			/*local*/String Key = ClassSymbol(ClassType, Symbol);
 			/*local*/Object Value = this.GetSymbol(Key);
 			if(Value != null) {
 				return Value;
 			}
 			if(ClassType.IsDynamicNaitiveLoading() & this.Context.RootNameSpace.GetLocalUndefinedSymbol(Key) == null) {
 				Value = LibGreenTea.LoadNativeStaticFieldValue(ClassType, Symbol.substring(1));
 				if(Value != null) {
 					return Value;
 				}
 				//LibGreenTea.LoadNativeMethods(ClassType, Symbol, FuncList);
 			}
 			if(!RecursiveSearch) {
 				break;
 			}
 			ClassType = ClassType.SuperType;
 		}
 		return null;
 	}
 
 	public final void ImportClassSymbol(GtNameSpace NameSpace, String Prefix, GtType ClassType, GtToken SourceToken) {
 		/*local*/String ClassPrefix = ClassSymbol(ClassType, ClassStaticName(""));
 		/*local*/ArrayList<String> KeyList = new ArrayList<String>();
 		/*local*/GtNameSpace ns = NameSpace;
 		while(ns != null) {
 			if(ns.SymbolPatternTable != null) {
 				LibGreenTea.RetrieveMapKeys(ns.SymbolPatternTable, ClassPrefix, KeyList);
 			}
 			ns = ns.ParentNameSpace;
 		}
 		/*local*/int i = 0;
 		while(i < KeyList.size()) {
 			/*local*/String Key = KeyList.get(i);
 			/*local*/Object Value = NameSpace.GetSymbol(Key);
 			Key = Key.replace(ClassPrefix, Prefix);
 			if(SourceToken != null) {
 				SourceToken.ParsedText = Key;
 			}
 			this.SetSymbol(Key, Value, SourceToken);
 			i = i + 1;
 		}
 	}
 	
 	public final GtFunc GetGetterFunc(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		/*local*/Object Func = this.Context.RootNameSpace.GetClassSymbol(ClassType, GetterSymbol(Symbol), RecursiveSearch);
 		if(Func instanceof GtFunc) {
 			return (/*cast*/GtFunc)Func;
 		}
 		Func = this.Context.RootNameSpace.GetLocalUndefinedSymbol(ClassSymbol(ClassType, GetterSymbol(Symbol)));
 		if(ClassType.IsDynamicNaitiveLoading() && Func == null) {
 			return LibGreenTea.LoadNativeField(ClassType, Symbol, false);
 		}
 		return null;
 	}
 
 	public final GtFunc GetSetterFunc(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		/*local*/Object Func = this.Context.RootNameSpace.GetClassSymbol(ClassType, SetterSymbol(Symbol), RecursiveSearch);
 		if(Func instanceof GtFunc) {
 			return (/*cast*/GtFunc)Func;
 		}
 		Func = this.Context.RootNameSpace.GetLocalUndefinedSymbol(ClassSymbol(ClassType, SetterSymbol(Symbol)));
 		if(ClassType.IsDynamicNaitiveLoading() && Func == null) {
 			return LibGreenTea.LoadNativeField(ClassType, Symbol, true);
 		}
 		return null;
 	}
 
 	public final GtFunc GetConverterFunc(GtType FromType, GtType ToType, boolean RecursiveSearch) {
 		/*local*/Object Func = this.Context.RootNameSpace.GetClassSymbol(FromType, ConverterSymbol(ToType), RecursiveSearch);
 		if(Func instanceof GtFunc) {
 			return (/*cast*/GtFunc)Func;
 		}
 		return null;
 	}
 
 	public final GtPolyFunc GetMethod(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		/*local*/ArrayList<GtFunc> FuncList = new ArrayList<GtFunc>();
 		while(ClassType != null) {
 			/*local*/String Key = ClassSymbol(ClassType, Symbol);
 			/*local*/Object RootValue = this.RetrieveFuncList(Key, FuncList);
 			if(RootValue == null && ClassType.IsDynamicNaitiveLoading()) {
 				if(LibGreenTea.EqualsString(Symbol, ConstructorSymbol())) {
 					LibGreenTea.LoadNativeConstructors(ClassType, FuncList);
 				}
 				else {
 					LibGreenTea.LoadNativeMethods(ClassType, Symbol, FuncList);
 				}
 			}
 			if(!RecursiveSearch) {
 				break;
 			}
 			ClassType = ClassType.SuperType;
 		}
 		return new GtPolyFunc(FuncList);
 	}
 
 	public final GtPolyFunc GetConstructorFunc(GtType ClassType) {
 		return this.Context.RootNameSpace.GetMethod(ClassType, ConstructorSymbol(), false);
 	}
 
 	public final Object RetrieveFuncList(String FuncName, ArrayList<GtFunc> FuncList) {
 		/*local*/Object FuncValue = this.GetLocalSymbol(FuncName);
 		if(FuncValue instanceof GtFunc) {
 			/*local*/GtFunc Func = (/*cast*/GtFunc)FuncValue;
 			FuncList.add(Func);
 		}
 		else if(FuncValue instanceof GtPolyFunc) {
 			/*local*/GtPolyFunc PolyFunc = (/*cast*/GtPolyFunc)FuncValue;
 			/*local*/int i = PolyFunc.FuncList.size() - 1;
 			while(i >= 0) {
 				FuncList.add(PolyFunc.FuncList.get(i));
 				i = i - 1;
 			}
 		}
 		if(this.ParentNameSpace != null) {
 			return this.ParentNameSpace.RetrieveFuncList(FuncName, FuncList);
 		}
 		return FuncValue;
 	}
 	
 	public final GtFunc GetFunc(String FuncName, int BaseIndex, ArrayList<GtType> TypeList) {
 		/*local*/ArrayList<GtFunc> FuncList = new ArrayList<GtFunc>();
 		this.RetrieveFuncList(FuncName, FuncList);
 		/*local*/int i = 0;
 		while(i < FuncList.size()) {
 			/*local*/GtFunc Func = FuncList.get(i);
 			if(Func.Types.length == TypeList.size() - BaseIndex) {
 				/*local*/int j = 0;
 				while(j < Func.Types.length) {
 					if(TypeList.get(BaseIndex + j) != Func.Types[j]) {
 						Func = null;
 						break;
 					}
 					j = j + 1;
 				}
 				if(Func != null) {
 					return Func;
 				}
 			}
 			i = i + 1;
 		}
 		return null;
 	}
 
 	public final Object AppendFuncName(String Key, GtFunc Func, GtToken SourceToken) {
 		/*local*/Object OldValue = this.GetLocalSymbol(Key);
 		if(OldValue instanceof GtSyntaxPattern) {
 			return OldValue;
 		}
 		if(OldValue instanceof GtFunc) {
 			/*local*/GtFunc OldFunc = (/*cast*/GtFunc)OldValue;
 			if(!OldFunc.EqualsType(Func)) {
 				/*local*/GtPolyFunc PolyFunc = new GtPolyFunc(null);
 				PolyFunc.Append(OldFunc, SourceToken);
 				PolyFunc.Append(Func, SourceToken);
 				this.SetSymbol(Key, PolyFunc, null);
 				return PolyFunc;
 			}
 			// error
 		}
 		else if(OldValue instanceof GtPolyFunc) {
 			/*local*/GtPolyFunc PolyFunc = (/*cast*/GtPolyFunc)OldValue;
 			PolyFunc.Append(Func, SourceToken);
 			return PolyFunc;
 		}
 		this.SetSymbol(Key, Func, SourceToken);
 		return OldValue;
 	}
 
 	public final Object AppendFunc(GtFunc Func, GtToken SourceToken) {
 		return this.AppendFuncName(Func.FuncName, Func, SourceToken);
 	}
 
 	public final Object AppendMethod(GtFunc Func, GtToken SourceToken) {
 		GtType ClassType = Func.GetRecvType();
 		/*local*/String Key = ClassSymbol(ClassType, Func.FuncName);
 		return this.AppendFuncName(Key, Func, SourceToken);
 	}
 
 	public final void AppendConstructor(GtType ClassType, GtFunc Func, GtToken SourceToken) {
 		/*local*/String Key = ClassSymbol(ClassType, ConstructorSymbol());
 		LibGreenTea.Assert(Func.Is(ConstructorFunc));
 		this.Context.RootNameSpace.AppendFuncName(Key, Func, SourceToken);  // @Public
 	}
 
 	public final void SetGetterFunc(GtType ClassType, String Name, GtFunc Func, GtToken SourceToken) {
 		/*local*/String Key = ClassSymbol(ClassType, GetterSymbol(Name));
 		LibGreenTea.Assert(Func.Is(GetterFunc));
 		this.Context.RootNameSpace.SetSymbol(Key, Func, SourceToken);  // @Public
 	}
 
 	public final void SetSetterFunc(GtType ClassType, String Name, GtFunc Func, GtToken SourceToken) {
 		/*local*/String Key = ClassSymbol(ClassType, SetterSymbol(Name));
 		LibGreenTea.Assert(Func.Is(SetterFunc));
 		this.Context.RootNameSpace.SetSymbol(Key, Func, SourceToken);  // @Public
 	}
 
 	public final void SetConverterFunc(GtType ClassType, GtType ToType, GtFunc Func, GtToken SourceToken) {
 		if(ClassType == null) {
 			ClassType = Func.GetFuncParamType(1);
 		}
 		if(ToType == null) {
 			ToType = Func.GetReturnType();
 		}
 		/*local*/String Key = ClassSymbol(ClassType, ConverterSymbol(ToType));
 		LibGreenTea.Assert(Func.Is(ConverterFunc));		
 		this.Context.RootNameSpace.SetSymbol(Key, Func, SourceToken);
 	}
 
 	public final Object Eval(String ScriptSource, long FileLine) {
 		/*local*/Object ResultValue = null;
 		LibGreenTea.VerboseLog(VerboseEval, "eval: " + ScriptSource);
 		/*local*/GtTokenContext TokenContext = new GtTokenContext(this, ScriptSource, FileLine);
 		this.Context.Generator.StartCompilationUnit();
 		TokenContext.SkipEmptyStatement();
 		while(TokenContext.HasNext()) {
 			/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 			TokenContext.ParseFlag = 0; // init
 			//System.err.println("** TokenContext.Position=" + TokenContext.CurrentPosition + ", " + TokenContext.IsAllowedBackTrack());
 			/*local*/GtSyntaxTree TopLevelTree = GreenTeaUtils.ParseExpression(this, TokenContext, false/*SuffixOnly*/);
 			TokenContext.SkipEmptyStatement();			
 			if(TopLevelTree.IsError() && TokenContext.HasNext()) {
 				GtToken Token = TokenContext.GetToken();
 				this.Context.ReportError(InfoLevel, TokenContext.GetToken(), "stopping script eval at " + Token.ParsedText);
 				ResultValue = TopLevelTree.KeyToken;  // in case of error, return error token
 				break;
 			}
 			if(TopLevelTree.IsValidSyntax()) {
 				TopLevelTree.SetAnnotation(Annotation);
 				/*local*/GtTypeEnv Gamma = new GtTypeEnv(this);
 				/*local*/GtNode Node = TopLevelTree.TypeCheck(Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 				ResultValue = Node.ToConstValue(true/*EnforceConst*/);
 //				if(ResultValue == null) {
 //					System.err.println(Node.getClass());
 //				}
 			}
 			TokenContext.Vacume();
 		}
 		this.Context.Generator.FinishCompilationUnit();
 		return ResultValue;
 	}
 
 	public final boolean Load(String ScriptText, long FileLine) {
 		/*local*/Object Token = this.Eval(ScriptText, FileLine);
 		if(Token instanceof GtToken && ((/*cast*/GtToken)Token).IsError()) {
 			return false;
 		}
 		return true;
 	}
 
 	public final boolean LoadFile(String FileName) {
 		/*local*/String ScriptText = LibGreenTea.LoadFile2(FileName);
 		if(ScriptText != null) {
 			/*local*/long FileLine = this.Context.GetFileLine(FileName, 1);
 			return this.Load(ScriptText, FileLine);
 		}
 		return false;
 	}
 
 	public final boolean LoadRequiredLib(String LibName) {
 		/*local*/String Key = GreenTeaUtils.NativeNameSuffix + "L" + LibName.toLowerCase();
 		if(!this.HasSymbol(Key)) {
 			/*local*/String Path = LibGreenTea.GetLibPath(this.Context.Generator.TargetCode, LibName);
 			/*local*/String Script = LibGreenTea.LoadFile2(Path);
 			if(Script != null) {
 				/*local*/long FileLine = this.Context.GetFileLine(Path, 1);
 				if(this.Load(Script, FileLine)) {
 					this.SetSymbol(Key, Path, null);
 					return true;
 				}
 			}
 			return false;
 		}
 		return true;
 	}
 
 }
 
 class GtGrammar extends GreenTeaUtils {
 //ifdef JAVA
 	public final static GtFunc LoadTokenFunc(GtParserContext ParserContext, Object Grammar, String FuncName) {
 		try {
 			Method JavaMethod = Grammar.getClass().getMethod(FuncName, GtTokenContext.class, String.class, long.class);
 			return LibGreenTea.ConvertNativeMethodToFunc(ParserContext, JavaMethod);
 		}
 		catch(NoSuchMethodException e) {
 			LibGreenTea.VerboseException(e);
 			LibGreenTea.Exit(1, e.toString());
 		}
 		return null;
 	}
 
 	public final static GtFunc LoadParseFunc(GtParserContext ParserContext, Object Grammar, String FuncName) {
 		try {
 			Method JavaMethod = Grammar.getClass().getMethod(FuncName, GtNameSpace.class, GtTokenContext.class, GtSyntaxTree.class, GtSyntaxPattern.class);
 			return LibGreenTea.ConvertNativeMethodToFunc(ParserContext, JavaMethod);
 		}
 		catch(NoSuchMethodException e) {
 			LibGreenTea.VerboseException(e);
 			LibGreenTea.Exit(1, e.toString());
 		}
 		return null;
 	}
 
 	public final static GtFunc LoadTypeFunc(GtParserContext ParserContext, Object Grammar, String FuncName) {
 		try {
 			Method JavaMethod = Grammar.getClass().getMethod(FuncName, GtTypeEnv.class, GtSyntaxTree.class, GtType.class);
 			return LibGreenTea.ConvertNativeMethodToFunc(ParserContext, JavaMethod);
 		}
 		catch(NoSuchMethodException e) {
 			LibGreenTea.VerboseException(e);
 			LibGreenTea.Exit(1, e.toString());
 		}
 		return null;
 	}
 //endif VAJA
 
 	public void LoadTo(GtNameSpace NameSpace) {
 		/*extension*/
 	}
 }
 
 final class GreenTeaGrammar extends GtGrammar {
 
 	private static final boolean HasAnnotation(GtMap Annotation, String Key) {
 		if(Annotation != null) {
 			/*local*/Object Value = Annotation.get(Key);
 			if(Value instanceof Boolean) {
 				Annotation.put(Key, false);  // consumed;
 			}
 			return (Value != null);
 		}
 		return false;
 	}
 
 	public static int ParseNameSpaceFlag(int Flag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "RootNameSpace")) {
 				Flag = Flag | RootNameSpace;
 			}
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Public")) {
 				Flag = Flag | PublicNameSpace;
 			}
 		}
 		return Flag;
 	}
 
 	public static int ParseClassFlag(int Flag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Export")) {
 				Flag = Flag | ExportFunc;
 			}
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Public")) {
 				Flag = Flag | PublicFunc;
 			}
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Virtual")) {
 				Flag = Flag | VirtualFunc;
 			}
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Deprecated")) {
 				Flag = Flag | DeprecatedFunc;
 			}
 		}
 		return Flag;
 	}
 
 	public static int ParseFuncFlag(int Flag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Export")) {
 				Flag = Flag | ExportFunc;
 			}
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Public")) {
 				Flag = Flag | PublicFunc;
 			}
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Const")) {
 				Flag = Flag | ConstFunc;
 			}
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Operator")) {
 				Flag = Flag | OperatorFunc;
 			}
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Coercion")) {
 				Flag = Flag | CoercionFunc;
 			}
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "Deprecated")) {
 				Flag = Flag | DeprecatedFunc;
 			}
 		}
 		return Flag;
 	}
 
 	public static int ParseVarFlag(int Flag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(GreenTeaGrammar.HasAnnotation(Annotation, "ReadOnly")) {
 				Flag = Flag | ReadOnlyVar;
 			}
 		}
 		return Flag;
 	}
 
 	// Token
 	public static long WhiteSpaceToken(GtTokenContext TokenContext, String SourceText, long pos) {
 		TokenContext.FoundWhiteSpace();
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 			if(ch == '\n' || !LibGreenTea.IsWhitespace(SourceText, pos)) {
 				break;
 			}
 			pos += 1;
 		}
 		return pos;
 	}
 
 	public static long IndentToken(GtTokenContext TokenContext, String SourceText, long pos) {
 		/*local*/long LineStart = pos + 1;
 		TokenContext.FoundLineFeed(1);
 		pos = pos + 1;
 		while(pos < SourceText.length()) {
 			if(!LibGreenTea.IsWhitespace(SourceText, pos)) {
 				break;
 			}
 			pos += 1;
 		}
 		/*local*/String Text = "";
 		if(LineStart < pos) {
 			Text = LibGreenTea.SubString(SourceText, LineStart, pos);
 		}
 		TokenContext.AddNewToken(Text, IndentTokenFlag, null);
 		return pos;
 		//TokenContext.AddNewToken(SourceText.substring(pos), SourceTokenFlag, null);
 		//return SourceText.length();
 	}
 
 	public static long SemiColonToken(GtTokenContext TokenContext, String SourceText, long pos) {
 		TokenContext.AddNewToken(LibGreenTea.SubString(SourceText, pos, (pos+1)), DelimTokenFlag, null);
 		return pos+1;
 	}
 
 	public static long SymbolToken(GtTokenContext TokenContext, String SourceText, long pos) {
 		/*local*/long start = pos;
 		/*local*/String PresetPattern = null;
 		while(pos < SourceText.length()) {
 			if(!LibGreenTea.IsVariableName(SourceText, pos) && !LibGreenTea.IsDigit(SourceText, pos)) {
 				break;
 			}
 			pos += 1;
 		}
 		TokenContext.AddNewToken(LibGreenTea.SubString(SourceText, start, pos), NameSymbolTokenFlag, PresetPattern);
 		return pos;
 	}
 
 	public static long OperatorToken(GtTokenContext TokenContext, String SourceText, long pos) {
 		/*local*/long NextPos = pos + 1;
 		while(NextPos < SourceText.length()) {
 			if(LibGreenTea.IsWhitespace(SourceText, NextPos) || LibGreenTea.IsLetter(SourceText, NextPos) || LibGreenTea.IsDigit(SourceText, NextPos)) {
 				break;
 			}
 			NextPos += 1;
 		}
 		/*local*/boolean Matched = false;
 		while(NextPos > pos) {
 			/*local*/String Sub = LibGreenTea.SubString(SourceText, pos, NextPos);
 			/*local*/GtSyntaxPattern Pattern = TokenContext.TopLevelNameSpace.GetExtendedSyntaxPattern(Sub);
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
 		TokenContext.AddNewToken(LibGreenTea.SubString(SourceText, pos, NextPos), 0, null);
 		return NextPos;
 	}
 
 	public static long CommentToken(GtTokenContext TokenContext, String SourceText, long pos) {
 		/*local*/long NextPos = pos + 1;
 		/*local*/char NextChar = LibGreenTea.CharAt(SourceText, NextPos);
 		if(NextChar != '/' && NextChar != '*') {
 			return MismatchedPosition;
 		}
 		/*local*/int Level = 0;
 		/*local*/char PrevChar = 0;
 		if(NextChar == '*') {
 			Level = 1;
 			// SourceMap ${file:line}
 			if(LibGreenTea.CharAt(SourceText, NextPos+1) == '$' && LibGreenTea.CharAt(SourceText, NextPos+2) == '{') {
 				/*local*/long StartPos = NextPos + 3;
 				NextPos += 3;
 				while(NextChar != 0) {
 					NextChar = LibGreenTea.CharAt(SourceText, NextPos);
 					if(NextChar == '}') {
 						TokenContext.SetSourceMap(LibGreenTea.SubString(SourceText, StartPos, NextPos));
 						break;
 					}
 					if(NextChar == '\n' || NextChar == '*') {
 						break;  // stop
 					}
 					NextPos += 1;
 				}
 			}
 		}
 		while(NextPos < SourceText.length()) {
 			NextChar = LibGreenTea.CharAt(SourceText, NextPos);
 			if(NextChar == '\n' && Level == 0) {
 				return GreenTeaGrammar.IndentToken(TokenContext, SourceText, NextPos);
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
 			PrevChar = NextChar;
 			NextPos = NextPos + 1;
 		}
 		return MismatchedPosition;
 	}
 
 	public static long NumberLiteralToken(GtTokenContext TokenContext, String SourceText, long pos) {
 		/*local*/long start = pos;
 		/*local*/long LastMatchedPos = pos;
 		while(pos < SourceText.length()) {
 			if(!LibGreenTea.IsDigit(SourceText, pos)) {
 				break;
 			}
 			pos += 1;
 		}
 		LastMatchedPos = pos;
 		/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 		if(ch != '.' && ch != 'e' && ch != 'E') {
 			TokenContext.AddNewToken(LibGreenTea.SubString(SourceText, start, pos), 0, "$IntegerLiteral$");
 			return pos;
 		}
 	    if(ch == '.') {
         	pos += 1;
 			while(pos < SourceText.length()) {
 				if(!LibGreenTea.IsDigit(SourceText, pos)) {
 					break;
 				}
 				pos += 1;
 			}
 	    }
 	    ch = LibGreenTea.CharAt(SourceText, pos);
 	    if(ch == 'e' || ch == 'E') {
 	    	pos += 1;
 		    ch = LibGreenTea.CharAt(SourceText, pos);
 	        if(ch == '+' || ch == '-') {
 	        	pos += 1;
 			    ch = LibGreenTea.CharAt(SourceText, pos);
 	        }
 		    /*local*/long saved = pos;
 			while(pos < SourceText.length()) {
 				if(!LibGreenTea.IsDigit(SourceText, pos)) {
 					break;
 				}
 				pos += 1;
 			}
 			if(saved == pos) {
 				pos = LastMatchedPos;
 			}
 	    }
 		TokenContext.AddNewToken(LibGreenTea.SubString(SourceText, start, pos), 0, "$FloatLiteral$");
 		return pos;
 	}
 
 	public static long CharLiteralToken(GtTokenContext TokenContext, String SourceText, long pos) {
 		/*local*/long start = pos;
 		/*local*/char prev = '\'';
 		pos = pos + 1; // eat "\'"
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 			if(ch == '\'' && prev != '\\') {
 				TokenContext.AddNewToken(LibGreenTea.SubString(SourceText, start, (pos + 1)), QuotedTokenFlag, "$CharLiteral$");
 				return pos + 1;
 			}
 			if(ch == '\n') {
 				TokenContext.ReportTokenError(ErrorLevel, "expected ' to close the charctor literal", LibGreenTea.SubString(SourceText, start, pos));
 				TokenContext.FoundLineFeed(1);
 				return pos;
 			}
 			pos = pos + 1;
 			prev = ch;
 		}
 		TokenContext.ReportTokenError(ErrorLevel, "expected ' to close the charctor literal", LibGreenTea.SubString(SourceText, start, pos));
 		return pos;
 	}
 
 	public static long StringLiteralToken(GtTokenContext TokenContext, String SourceText, long pos) {
 		/*local*/long start = pos;
 		/*local*/char prev = '"';
 		pos = pos + 1; // eat "\""
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 			if(ch == '"' && prev != '\\') {
 				TokenContext.AddNewToken(LibGreenTea.SubString(SourceText, start, (pos + 1)), QuotedTokenFlag, "$StringLiteral$");
 				return pos + 1;
 			}
 			if(ch == '\n') {
 				TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", LibGreenTea.SubString(SourceText, start, pos));
 				TokenContext.FoundLineFeed(1);
 				return pos;
 			}
 			pos = pos + 1;
 			prev = ch;
 		}
 		TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", LibGreenTea.SubString(SourceText, start, pos));
 		return pos;
 	}
 
 	public static long StringLiteralToken_StringInterpolation(GtTokenContext TokenContext, String SourceText, long pos) {
 		/*local*/long start = pos + 1;
 		/*local*/long NextPos = start;
 		/*local*/char prev = '"';
 		while(NextPos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, NextPos);
 			if(ch == '$') {
 				/*local*/long end = NextPos + 1;
 				/*local*/char nextch = LibGreenTea.CharAt(SourceText, end);
 				if(nextch == '{') {
 					while(end < SourceText.length()) {
 						ch = LibGreenTea.CharAt(SourceText, end);
 						if(ch == '}') {
 							break;
 						}
 						end = end + 1;
 					}
 					/*local*/String Expr = LibGreenTea.SubString(SourceText, (NextPos + 2), end);
 					/*local*/GtTokenContext LocalContext = new GtTokenContext(TokenContext.TopLevelNameSpace, Expr, TokenContext.ParsingLine);
 					LocalContext.SkipEmptyStatement();
 
 					TokenContext.AddNewToken("\"" + LibGreenTea.SubString(SourceText, start, NextPos) + "\"", 0, "$StringLiteral$");
 					TokenContext.AddNewToken("+", 0, null);
 					while(LocalContext.HasNext()) {
 						/*local*/GtToken NewToken = LocalContext.Next();
 						TokenContext.AddNewToken(NewToken.ParsedText, 0, null);
 					}
 					TokenContext.AddNewToken("+", 0, null);
 					end = end + 1;
 					start = end;
 					NextPos = end;
 					prev = ch;
 					if(ch == '"') {
 						TokenContext.AddNewToken("\"" + LibGreenTea.SubString(SourceText, start, NextPos) + "\"", 0, "$StringLiteral$");
 						return NextPos + 1;
 					}
 					continue;
 				}
 			}
 			if(ch == '"' && prev != '\\') {
 				TokenContext.AddNewToken("\"" + LibGreenTea.SubString(SourceText, start, NextPos) + "\"", 0, "$StringLiteral$");
 				return NextPos + 1;
 			}
 			if(ch == '\n') {
 				TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", LibGreenTea.SubString(SourceText, start, NextPos));
 				TokenContext.FoundLineFeed(1);
 				return NextPos;
 			}
 			NextPos = NextPos + 1;
 			prev = ch;
 		}
 		TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", LibGreenTea.SubString(SourceText, start, NextPos));
 		return NextPos;
 	}
 
 	public static GtSyntaxTree ParseTypeOf(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree TypeOfTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "typeof");
 		TypeOfTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		TypeOfTree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$Expression$", Required);
 		TypeOfTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		if(!TypeOfTree.IsMismatchedOrError()) {
 			/*local*/GtTypeEnv Gamma = new GtTypeEnv(NameSpace);
 			/*local*/GtNode ObjectNode = TypeOfTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 			if(ObjectNode.IsError()) {
 				TypeOfTree.ToError(ObjectNode.Token);
 			}
 			else {
 				TypeOfTree.ToConstTree(ObjectNode.Type);
 				/*local*/GtSyntaxTree TypeTree = TokenContext.ParsePatternAfter(NameSpace, TypeOfTree, "$TypeSuffix$", Optional);
 				return (TypeTree == null) ? TypeOfTree : TypeTree;
 			}
 		}
 		return TypeOfTree;
 	}
 
 	public static GtSyntaxTree ParseTypeSuffix(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree TypeTree, GtSyntaxPattern Pattern) {
 		/*local*/GtType ParsedType = TypeTree.GetParsedType();
 		if(ParsedType.IsGenericType()) {
 			if(TokenContext.MatchToken("<")) {  // Generics
 				/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 				while(!TokenContext.StartsWithToken(">")) {
 					if(TypeList.size() > 0 && !TokenContext.MatchToken(",")) {
 						return null;
 					}
 					/*local*/GtSyntaxTree ParamTypeTree = TokenContext.ParsePattern(NameSpace, "$Type$", Optional);
 					if(ParamTypeTree == null) {
 						return ParamTypeTree;
 					}
 					TypeList.add(ParamTypeTree.GetParsedType());
 				}
 				ParsedType = NameSpace.Context.GetGenericType(ParsedType, 0, TypeList, true);
 			}
 		}
 		while(TokenContext.MatchToken("[")) {  // Array
 			if(!TokenContext.MatchToken("]")) {
 				return null;
 			}
 			ParsedType = NameSpace.Context.GetGenericType1(NameSpace.Context.ArrayType, ParsedType, true);
 		}
 		TypeTree.ToConstTree(ParsedType);
 		return TypeTree;
 	}
 
 	// PatternName: "<" $Type$ ">"
 	public static GtSyntaxTree ParseTypeParam(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree GroupTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "<");
 		GroupTree.AppendMatchedPattern(NameSpace, TokenContext, "$Type$", Required);
 		GroupTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ">", Required);
 		return GroupTree;
 	}
 
 	public static GtNode TypeTypeParam(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType ParamBaseType = ParsedTree.GetSyntaxTreeAt(UnaryTerm).GetParsedType();
 		return Gamma.Generator.CreateConstNode(Gamma.Context.TypeType, ParsedTree, new GtType(Gamma.Context, TypeParameter, "", ParamBaseType, null));
 	}
 
 	// parser and type checker
 	public static GtSyntaxTree ParseType(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		if(TokenContext.MatchToken("typeof")) {
 			return GreenTeaGrammar.ParseTypeOf(NameSpace, TokenContext, LeftTree, Pattern);
 		}
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/Object ConstValue = NameSpace.GetSymbol(Token.ParsedText);
 		if(!(ConstValue instanceof GtType)) {
 			return null;  // Not matched
 		}
 		/*local*/GtSyntaxTree TypeTree = new GtSyntaxTree(Pattern, NameSpace, Token, ConstValue);
 		/*local*/GtSyntaxTree TypeSuffixTree = TokenContext.ParsePatternAfter(NameSpace, TypeTree, "$TypeSuffix$", Optional);
 		return (TypeSuffixTree == null) ? TypeTree : TypeSuffixTree;
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
 		if(ParsedTree.ParsedValue instanceof String) { // FIXME IMIFU
 			ParsedTree.ParsedValue = (/*cast*/String) ParsedTree.ParsedValue;
 		}
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ParsedTree.ParsedValue), ParsedTree, ParsedTree.ParsedValue);
 	}
 
 	public static GtSyntaxTree ParseNull(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "null");
 	}
 
 	public static GtNode TypeNull(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType ThisType = ContextType;
 		if(ThisType == Gamma.VarType) {
 			ThisType = Gamma.AnyType;
 		}
 		if(ThisType.DefaultNullValue != null) {
 			return Gamma.Generator.CreateConstNode(ThisType, ParsedTree, ThisType.DefaultNullValue);
 		}
 		return Gamma.Generator.CreateNullNode(ThisType, ParsedTree);
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
 			TypeTree.Pattern = NameSpace.GetSyntaxPattern("$Const$");
 			return TypeTree;
 		}
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree VarTree = new GtSyntaxTree(NameSpace.GetSyntaxPattern("$Variable$"), NameSpace, Token, null);
 		if(!LibGreenTea.IsVariableName(Token.ParsedText, 0)) {
 			return TokenContext.ReportExpectedMessage(Token, "name", true);
 		}
 		return VarTree;
 	}
 
 	public static GtSyntaxTree ParseVariable(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		if(LibGreenTea.IsVariableName(Token.ParsedText, 0)) {
 			return new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		}
 		return null;
 	}
 
 	public static GtNode TypeVariable(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtVariableInfo VariableInfo = Gamma.LookupDeclaredVariable(Name);
 		if(VariableInfo != null) {
 			VariableInfo.Used();
 			return Gamma.Generator.CreateLocalNode(VariableInfo.Type, ParsedTree, VariableInfo.NativeName);
 		}
 		/*local*/Object ConstValue = (/*cast*/Object) ParsedTree.NameSpace.GetSymbol(Name);
 		if(ConstValue != null) {
 			return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ConstValue), ParsedTree, ConstValue);
 		}
 		/*local*/GtNode Node = Gamma.Generator.CreateLocalNode(Gamma.AnyType, ParsedTree, Name + Gamma.Generator.BlockComment("undefined"));
 		return Gamma.ReportTypeResult(ParsedTree, Node, TypeErrorLevel, "undefined name: " + Name);
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
 		if(Tree.IsMismatchedOrError()) {
 			return Tree;  // stopping to funcdecl operator
 		}
 		if(TokenContext.MatchToken("=")) {
 			Tree.SetMatchedPatternAt(VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 		}
 		while(TokenContext.MatchToken(",")) {
 			/*local*/GtSyntaxTree NextTree = new GtSyntaxTree(Pattern, NameSpace, Tree.KeyToken, null);
 			NextTree.SetSyntaxTreeAt(VarDeclType, Tree.GetSyntaxTreeAt(VarDeclType));
 			NextTree.SetMatchedPatternAt(VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 			if(TokenContext.MatchToken("=")) {
 				NextTree.SetMatchedPatternAt(VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 			}
 			Tree = GreenTeaUtils.LinkTree(Tree, NextTree);
 		}
 		return Tree;
 	}
 
 	public static GtNode TypeVarDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/int VarFlag = GreenTeaGrammar.ParseVarFlag(0, ParsedTree.Annotation);
 		/*local*/GtType DeclType = ParsedTree.GetSyntaxTreeAt(VarDeclType).GetParsedType();
 		/*local*/String VariableName = ParsedTree.GetSyntaxTreeAt(VarDeclName).KeyToken.ParsedText;
 		/*local*/GtNode InitValueNode = null;
 		if(ParsedTree.HasNodeAt(VarDeclValue)) {
 			InitValueNode = ParsedTree.TypeCheckAt(VarDeclValue, Gamma, DeclType, DefaultTypeCheckPolicy);
 			if(InitValueNode.IsError()) {
 				return InitValueNode;
 			}
 		}
 		if(GreenTeaUtils.UseLangStat) {
 			Gamma.Context.Stat.VarDecl += 1;
 		}/*EndOfStat*/
 		if(DeclType.IsVarType()) {
 			if(InitValueNode == null) {
 				DeclType = Gamma.AnyType;
 			}
 			else {
 				DeclType = InitValueNode.Type;
 			}
 			Gamma.ReportTypeInference(ParsedTree.KeyToken, VariableName, DeclType);
 			if(GreenTeaUtils.UseLangStat) {
 				Gamma.Context.Stat.VarDeclInfer += 1;
 				if(DeclType.IsAnyType()) {
 					Gamma.Context.Stat.VarDeclInferAny += 1;
 				}
 			}/*EndOfStat*/
 		}
 		if(GreenTeaUtils.UseLangStat) {
 			if(DeclType.IsAnyType()) {
 				Gamma.Context.Stat.VarDeclAny += 1;
 			}
 		}/*EndOfStat*/
 		if(InitValueNode == null) {
 			InitValueNode = Gamma.CreateDefaultValue(ParsedTree, DeclType);
 		}
 		/*local*/GtVariableInfo VarInfo = Gamma.AppendDeclaredVariable(VarFlag, DeclType, VariableName, ParsedTree.GetSyntaxTreeAt(VarDeclName).KeyToken, InitValueNode.ToConstValue(false));
 		/*local*/GtNode BlockNode = GreenTeaUtils.TypeBlock(Gamma, ParsedTree.NextTree, Gamma.VoidType);
 		ParsedTree.NextTree = null;
 		return Gamma.Generator.CreateVarNode(DeclType, ParsedTree, DeclType, VarInfo.NativeName, InitValueNode, BlockNode);
 	}
 
 	// Parse And Type
 	public static GtSyntaxTree ParseIntegerLiteral(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		return new GtSyntaxTree(Pattern, NameSpace, Token, LibGreenTea.ParseInt(Token.ParsedText));
 	}
 	public static GtSyntaxTree ParseFloatLiteral(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		return new GtSyntaxTree(Pattern, NameSpace, Token, LibGreenTea.ParseFloat(Token.ParsedText));
 	}
 
 	public static GtSyntaxTree ParseStringLiteral(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, LibGreenTea.UnquoteString(Token.ParsedText));
 		return NewTree;
 	}
 
 	public static GtSyntaxTree ParseCharLiteral(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, LibGreenTea.UnquoteString(Token.ParsedText));
 		return NewTree;
 	}
 
 	public static GtNode TypeCharLiteral(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Text = ParsedTree.KeyToken.ParsedText;
 		if(Text.length() == 3/*'A'*/) {
 			/*local*/int ch = LibGreenTea.CharAt(Text, 1);
 			/*local*/Object Value = ch;
 			ParsedTree.ParsedValue = LibGreenTea.ParseInt(Value.toString());
 		}
 		else if(Text.length() == 4/*'\n'*/) {
 			/*local*/int ch = LibGreenTea.CharAt(Text, 2);
 			if(LibGreenTea.CharAt(Text, 1) == '\\') {
 				switch(ch) {
 				case '\'': ch = '\''; break;
 				case '\\': ch = '\\'; break;
 				case 'b':  ch = '\b'; break;
 				case 'f':  ch = '\f'; break;
 				case 'n':  ch = '\n'; break;
 				case 'r':  ch = '\r'; break;
 				case 't':  ch = '\t'; break;
 				default:   ch = -1;
 				}
 				if(ch >= 0) {
 					/*local*/Object Value = ch;
 					ParsedTree.ParsedValue = LibGreenTea.ParseInt(Value.toString());
 				}
 			}
 		}
 		return GreenTeaGrammar.TypeConst(Gamma, ParsedTree, ContextType);
 	}
 
 	public static GtSyntaxTree ParseExpression(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return GreenTeaUtils.ParseExpression(NameSpace, TokenContext, false/*SuffixOnly*/);
 	}
 
 	public static GtSyntaxTree ParseUnary(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		/*local*/GtSyntaxTree SubTree = GreenTeaUtils.ParseExpression(NameSpace, TokenContext, true/*SuffixOnly*/);
 		Tree.SetSyntaxTreeAt(UnaryTerm, SubTree);
 		return Tree;
 	}
 
 	public static GtNode TypeUnary(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode ExprNode  = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ExprNode.IsError()) {
 			return ExprNode;
 		}
 		/*local*/GtType BaseType = ExprNode.Type;
 		/*local*/GtType ReturnType = Gamma.AnyType;
 		/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(BaseType, SafeFuncName(OperatorSymbol), true);
 		/*local*/GtFunc ResolvedFunc = PolyFunc.ResolveUnaryFunc(Gamma, ParsedTree, ExprNode);
 		if(ResolvedFunc == null) {
 			Gamma.Context.ReportError(TypeErrorLevel, ParsedTree.KeyToken, "mismatched operators: " + PolyFunc);
 		}
 		else {
 			ReturnType = ResolvedFunc.GetReturnType();
 		}
 		/*local*/GtNode UnaryNode =  Gamma.Generator.CreateUnaryNode(ReturnType, ParsedTree, ResolvedFunc, ExprNode);
 		if(ResolvedFunc == null && !BaseType.IsDynamic()) {
 			return Gamma.ReportTypeResult(ParsedTree, UnaryNode, TypeErrorLevel, "undefined operator: "+ OperatorSymbol + " of " + BaseType);
 		}
 		return UnaryNode;
 	}
 
 	private static GtSyntaxTree RightJoin(GtNameSpace NameSpace, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern, GtToken OperatorToken, GtSyntaxTree RightTree) {
 		/*local*/GtSyntaxTree RightLeft = RightTree.GetSyntaxTreeAt(LeftHandTerm);
 		if(RightLeft.Pattern.IsBinaryOperator() && Pattern.IsRightJoin(RightLeft.Pattern)) {
 			RightTree.SetSyntaxTreeAt(LeftHandTerm, GreenTeaGrammar.RightJoin(NameSpace, LeftTree, Pattern, OperatorToken, RightLeft));
 		}
 		else {
 			/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, OperatorToken, null);
 			NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 			NewTree.SetSyntaxTreeAt(RightHandTerm, RightLeft);
 			RightTree.SetSyntaxTreeAt(LeftHandTerm, NewTree);
 		}
 		return RightTree;
 	}
 
 	public static GtSyntaxTree ParseBinary(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken OperatorToken = TokenContext.Next();
 		/*local*/GtSyntaxTree RightTree = GreenTeaUtils.ParseExpression(NameSpace, TokenContext, false/*SuffixOnly*/);
 		if(GreenTeaUtils.IsMismatchedOrError(RightTree)) {
 			return RightTree;
 		}
 		//System.err.println("left=" + Pattern.SyntaxFlag + ", right=" + RightTree.Pattern.SyntaxFlag + ", binary?" +  RightTree.Pattern.IsBinaryOperator() + RightTree.Pattern);
 		if(RightTree.Pattern.IsBinaryOperator() && Pattern.IsRightJoin(RightTree.Pattern)) {
 			return GreenTeaGrammar.RightJoin(NameSpace, LeftTree, Pattern, OperatorToken, RightTree);
 		}
 		// LeftJoin
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, OperatorToken, null);
 		NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 		NewTree.SetSyntaxTreeAt(RightHandTerm, RightTree);
 		if(RightTree.NextTree != null) {  // necesarry; don't remove
 			GreenTeaUtils.LinkTree(NewTree, RightTree.NextTree);
 			RightTree.NextTree = null;
 		}
 		return NewTree;
 	}
 
 	public static GtNode TypeBinary(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode  = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(!LeftNode.IsError()) {
 			/*local*/GtType BaseType = LeftNode.Type;
 			/*local*/GtType ReturnType = Gamma.AnyType;
 			/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 			/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(BaseType, SafeFuncName(OperatorSymbol), true);
 			/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 			ParamList.add(LeftNode);
 			/*local*/GtFunc ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 			if(ResolvedFunc == null) {
 				Gamma.Context.ReportError(TypeErrorLevel, ParsedTree.KeyToken, "mismatched operators: " + PolyFunc);
 			}
 			else {
 				ReturnType = ResolvedFunc.GetReturnType();
 			}
 			/*local*/GtNode BinaryNode =  Gamma.Generator.CreateBinaryNode(ReturnType, ParsedTree, ResolvedFunc, LeftNode, ParamList.get(1));
 			if(ResolvedFunc == null && !BaseType.IsDynamic()) {
 				return Gamma.ReportTypeResult(ParsedTree, BinaryNode, TypeErrorLevel, "undefined operator: "+ OperatorSymbol + " of " + LeftNode.Type);
 			}
 			return BinaryNode;
 		}
 		return LeftNode;
 	}
 
 	public static GtSyntaxTree ParseTrinary(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree TrinaryTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "?");
 		TrinaryTree.SetSyntaxTreeAt(IfCond, LeftTree);
 		TrinaryTree.SetMatchedPatternAt(IfThen, NameSpace, TokenContext, "$Expression$", Required);
 		TrinaryTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 		TrinaryTree.SetMatchedPatternAt(IfElse, NameSpace, TokenContext, "$Expression$", Required);
 		return TrinaryTree;
 	}
 
 	public static GtNode TypeTrinary(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckAt(IfCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode ThenNode = ParsedTree.TypeCheckAt(IfThen, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ThenNode.IsError()) {
 			return ThenNode;
 		}
 		/*local*/GtNode ElseNode = ParsedTree.TypeCheckAt(IfElse, Gamma, ThenNode.Type, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateTrinaryNode(ThenNode.Type, ParsedTree, CondNode, ThenNode, ElseNode);
 	}
 
 	// PatternName: "("
 	public static GtSyntaxTree ParseGroup(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree GroupTree = TokenContext.CreateSyntaxTree(NameSpace, Pattern, null);
 		GroupTree.SetMatchedTokenAt(KeyTokenIndex, NameSpace, TokenContext, "(", Required);
 		/*local*/int ParseFlag = TokenContext.SetSkipIndent(true);
 		GroupTree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Required);
 		GroupTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		TokenContext.SetRememberFlag(ParseFlag);
 		return GroupTree;
 	}
 
 	public static GtNode TypeGroup(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return ParsedTree.TypeCheckAt(UnaryTerm, Gamma, ContextType, DefaultTypeCheckPolicy);
 	}
 
 	// PatternName: "(" "to" $Type$ ")"
 	public static GtSyntaxTree ParseCast(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken FirstToken = TokenContext.Next(); // skip the first token
 		/*local*/GtSyntaxTree CastTree = null;
 		if(TokenContext.MatchToken("to")) {
 			CastTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetBeforeToken(), null);
 		}
 		else {
 			CastTree = new GtSyntaxTree(Pattern, NameSpace, FirstToken, null);
 		}
 		CastTree.SetMatchedPatternAt(LeftHandTerm, NameSpace, TokenContext, "$Type$", Required);
 		CastTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		/*local*/GtSyntaxTree ExprTree = GreenTeaUtils.ParseExpression(NameSpace, TokenContext, true/*SuffixOnly*/);
 		if(ExprTree == null) {
 			return null;
 		}
 		CastTree.SetSyntaxTreeAt(RightHandTerm, ExprTree);
 		return CastTree;
 	}
 
 	public static GtNode TypeCast(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType CastType = ParsedTree.GetSyntaxTreeAt(LeftHandTerm).GetParsedType();
 		/*local*/int TypeCheckPolicy = CastPolicy;
 		return ParsedTree.TypeCheckAt(RightHandTerm, Gamma, CastType, TypeCheckPolicy);
 	}
 
 	public static GtSyntaxTree ParseGetter(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		TokenContext.MatchToken(".");
 		/*local*/GtToken Token = TokenContext.Next();
 		if(Token.IsNameSymbol()) {
 			/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 			NewTree.AppendParsedTree2(LeftTree);
 			return NewTree;
 		}
 		return TokenContext.ReportExpectedMessage(Token, "field name", true);
 	}
 
 	public static GtNode TypeGetter(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode ObjectNode = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ObjectNode.IsError()) {
 			return ObjectNode;
 		}
 		// To start, check class const such as Math.Pi if base is a type value
 		/*local*/String TypeName = ObjectNode.Type.ShortClassName;
 		if(ObjectNode instanceof ConstNode && ObjectNode.Type.IsTypeType()) {
 			/*local*/GtType ObjectType = (/*cast*/GtType)((/*cast*/ConstNode)ObjectNode).ConstValue;
 			/*local*/Object ConstValue = ParsedTree.NameSpace.GetClassSymbol(ObjectType, ClassStaticName(Name), true);
 			if(ConstValue instanceof GreenTeaEnum) {
 				if(ContextType.IsStringType()) {
 					ConstValue = ((/*cast*/GreenTeaEnum)ConstValue).EnumSymbol;
 				}
 				else {
 					ConstValue = ((/*cast*/GreenTeaEnum)ConstValue).EnumValue;
 				}
 			}
 			if(ConstValue != null) {
 				return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ConstValue), ParsedTree, ConstValue);
 			}
 			TypeName = ObjectType.ShortClassName;
 		}
 		/*local*/GtFunc GetterFunc = ParsedTree.NameSpace.GetGetterFunc(ObjectNode.Type, Name, true);
 		/*local*/GtType ReturnType = (GetterFunc != null) ? GetterFunc.GetReturnType() : Gamma.AnyType;
 		/*local*/GtNode Node = Gamma.Generator.CreateGetterNode(ReturnType, ParsedTree, GetterFunc, ObjectNode);
 		if(GetterFunc == null) {
 			if(!ObjectNode.Type.IsDynamic() && ContextType != Gamma.FuncType) {
 				return Gamma.ReportTypeResult(ParsedTree, Node, TypeErrorLevel, "undefined name: " + Name + " of " + TypeName);
 			}
 		}
 		return Node;
 	}
 
 	public static GtSyntaxTree ParseDefined(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree DefinedTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "defined");
 		DefinedTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		DefinedTree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$Expression$", Required);
 		DefinedTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		return DefinedTree;
 	}
 
 	public static GtNode TypeDefined(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		Gamma.Context.SetNoErrorReport(true);
 		/*local*/GtNode ObjectNode = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		Gamma.Context.SetNoErrorReport(false);
 		return Gamma.Generator.CreateConstNode(Gamma.BooleanType, ParsedTree, (ObjectNode instanceof ConstNode));
 	}
 	public static GtSyntaxTree ParseApply(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int ParseFlag = TokenContext.SetSkipIndent(true);
 		/*local*/GtSyntaxTree FuncTree = TokenContext.CreateSyntaxTree(NameSpace, Pattern, null);
 		FuncTree.SetMatchedTokenAt(KeyTokenIndex, NameSpace, TokenContext, "(", Required);
 		FuncTree.AppendParsedTree2(LeftTree);
 		if(!TokenContext.MatchToken(")")) {
 			while(!FuncTree.IsMismatchedOrError()) {
 				FuncTree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Required);
 				if(TokenContext.MatchToken(")")) {
 					break;
 				}
 				FuncTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 			}
 		}
 		TokenContext.SetRememberFlag(ParseFlag);
 		return FuncTree;
 	}
 
 	public static GtNode TypeApply(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode FuncNode = ParsedTree.TypeCheckAt(0, Gamma, Gamma.FuncType, NoCheckPolicy);
 		/*local*/ArrayList<GtNode> NodeList = new ArrayList<GtNode>();
 		GreenTeaUtils.AppendTypedNode(NodeList, FuncNode);
 		/*local*/GtFunc ResolvedFunc = null;
 		/*local*/int TreeIndex = 1;
 		if(FuncNode instanceof GetterNode) { /* Func style .. o.f x, y, .. */
 			/*local*/String FuncName = FuncNode.Token.ParsedText;
 			/*local*/GtNode BaseNode = ((/*cast*/GetterNode)FuncNode).Expr;
 			GreenTeaUtils.AppendTypedNode(NodeList, BaseNode);
 			/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(BaseNode.Type, FuncName, true);
 			if(PolyFunc != null) {
 				ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, TreeIndex, NodeList);
 			}
 			else {
 				ParsedTree.TypeCheckParam(Gamma, TreeIndex, NodeList);
 			}
 			//return Gamma.CreateSyntaxErrorNode(ParsedTree, "undefined method: " + BaseNode.Type + "." + FuncName);
 		}
 		else if(FuncNode instanceof ConstNode) { /* Func style .. f x, y .. */
 			/*local*/Object Func = ((/*cast*/ConstNode)FuncNode).ConstValue;
 			if(Func instanceof GtType) {  // constructor;
 				/*local*/GtType ClassType = (/*cast*/GtType)Func;
 				if(ClassType.IsVarType()) {  /* constructor */
 					ClassType = ContextType;
 					if(ClassType.IsVarType()) {
 						return Gamma.CreateSyntaxErrorNode(ParsedTree, "ambigious constructor: " + FuncNode.Token);
 					}
 					Gamma.ReportTypeInference(FuncNode.Token, "constructor", ClassType);
 				}
 				if(ClassType.IsAbstract()) {
 					return Gamma.CreateSyntaxErrorNode(ParsedTree, "type is abstract");
 				}
 				//System.err.println("tree size = " + ParsedTree.SubTreeList.size());
 				/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetConstructorFunc(/*GtFunc*/ClassType);
 				//NodeList.set(0, Gamma.Generator.CreateNullNode(ClassType, ParsedTree));
 				ResolvedFunc = PolyFunc.ResolveConstructor(Gamma, ParsedTree, 1, NodeList);
 				if(ResolvedFunc == null) {
 					if(!ClassType.IsNative() && ParsedTree.SubTreeList.size() == 1) {
 						return Gamma.Generator.CreateNewNode(ClassType, ParsedTree);
 					}
 					Gamma.Context.ReportError(TypeErrorLevel, ParsedTree.KeyToken, "mismatched : constructor" + PolyFunc);
 				}
 				else {
 					if(ResolvedFunc.Is(NativeFunc)) {
 						return Gamma.Generator.CreateConstructorNode(ClassType, ParsedTree, ResolvedFunc, NodeList);
 					}
 					NodeList.add(1, Gamma.Generator.CreateNewNode(ClassType, ParsedTree)); //JAVAONLY?
 				}
 				// TODO;
 //				return Gamma.Generator.CreateConstructorNode(ClassType, ParsedTree, ResolvedFunc, NodeList);
 			}
 			else if(Func instanceof GtFunc) {
 				ResolvedFunc = (/*cast*/GtFunc)Func;
 			}
 			else if(Func instanceof GtPolyFunc) {
 				/*local*/GtPolyFunc PolyFunc = (/*cast*/GtPolyFunc)Func;
 				/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 				ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 				if(ResolvedFunc != null) {
 					// reset ConstValue as if non-polymorphic function were found
 					((/*cast*/ConstNode)FuncNode).ConstValue = ResolvedFunc;
 					((/*cast*/ConstNode)FuncNode).Type = ResolvedFunc.GetFuncType();
 				}
 			}
 		}
 		/*local*/GtType ReturnType = Gamma.AnyType;
 		if(FuncNode.Type == Gamma.AnyType) {
 			while(TreeIndex < LibGreenTea.ListSize(ParsedTree.SubTreeList)) {
 				/*local*/GtNode Node = ParsedTree.TypeCheckAt(TreeIndex, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 				if(Node.IsError()) {
 					return Node;
 				}
 				GreenTeaUtils.AppendTypedNode(NodeList, Node);
 				TreeIndex = TreeIndex + 1;
 			}
 		}
 		else if(FuncNode.Type.BaseType == Gamma.FuncType) {
 			/*local*/GtType FuncType = FuncNode.Type;
 			LibGreenTea.Assert(LibGreenTea.ListSize(ParsedTree.SubTreeList) == FuncType.TypeParams.length); // FIXME: add check paramerter size
 			while(TreeIndex < LibGreenTea.ListSize(ParsedTree.SubTreeList)) {
 				/*local*/GtNode Node = ParsedTree.TypeCheckAt(TreeIndex, Gamma, FuncType.TypeParams[TreeIndex], DefaultTypeCheckPolicy);
 				if(Node.IsError()) {
 					return Node;
 				}
 				GreenTeaUtils.AppendTypedNode(NodeList, Node);
 				TreeIndex = TreeIndex + 1;
 			}
 			ReturnType = FuncType.TypeParams[0];
 		}
 		else {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, FuncNode.Type + " is not applicapable");
 		}
 		/*local*/GtNode Node = Gamma.Generator.CreateApplyNode(ReturnType, ParsedTree, ResolvedFunc);
 		Node.AppendNodeList(NodeList);
 		return Node;
 	}
 
 	public static GtNode TypeAnd(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateAndNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static GtNode TypeOr(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateOrNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static GtNode TypeInstanceOf(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/GtType GivenType = ParsedTree.GetSyntaxTreeAt(RightHandTerm).GetParsedType();
 		if(GivenType == null) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree,  "type is expected in " + ParsedTree.KeyToken);
 		}
 		return Gamma.Generator.CreateInstanceOfNode(Gamma.BooleanType, ParsedTree, LeftNode, GivenType);
 	}
 
 	public static GtNode TypeAssign(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(LeftNode instanceof LocalNode || LeftNode instanceof GetterNode || LeftNode instanceof IndexerNode) {
 			/*local*/GtNode RightNode = ParsedTree.TypeCheckAt(RightHandTerm, Gamma, LeftNode.Type, DefaultTypeCheckPolicy);
 			return Gamma.Generator.CreateAssignNode(LeftNode.Type, ParsedTree, LeftNode, RightNode);
 		}
 		return Gamma.CreateSyntaxErrorNode(ParsedTree, "the left-hand side of an assignment must be variable");
 	}
 
 	public static GtNode TypeSelfAssign(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(!(LeftNode instanceof LocalNode || LeftNode instanceof GetterNode || LeftNode instanceof IndexerNode)) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "the left-hand side of an assignment must be variable");
 		}
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckAt(RightHandTerm, Gamma, LeftNode.Type, DefaultTypeCheckPolicy);
 		/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 		OperatorSymbol = OperatorSymbol.substring(0, OperatorSymbol.length() - 1);
 		/*local*/GtFunc Func = null;
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(LeftNode.Type, OperatorSymbol, true);
 		if(PolyFunc != null) {
 			/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 			ParamList.add(LeftNode);
 			Func = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 			if(Func != null) {
 				LeftNode = ParamList.get(0);
 				RightNode = ParamList.get(1);
 			}
 		}
 		return Gamma.Generator.CreateSelfAssignNode(LeftNode.Type, ParsedTree, Func, LeftNode, RightNode);
 	}
 
 	public static GtSyntaxTree ParseIncl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree InclTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.Next(), null);
 		if(LeftTree != null) { /* i++ */
 			InclTree.SetSyntaxTreeAt(UnaryTerm, LeftTree);
 		}
 		else { /* ++i */
 			/*local*/GtSyntaxTree Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 			InclTree.SetSyntaxTreeAt(UnaryTerm, Tree);
 		}
 		return InclTree;
 	}
 
 	public static GtNode TypeIncl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(LeftNode.Type == Gamma.IntType) {
 			if(Type != Gamma.VoidType) {
 				Gamma.Context.ReportError(WarningLevel, ParsedTree.KeyToken, "only available as statement: " + ParsedTree.KeyToken);
 			}
 			if(LeftNode instanceof LocalNode || LeftNode instanceof GetterNode || LeftNode instanceof IndexerNode) {
 				/*local*/GtNode ConstNode = Gamma.Generator.CreateConstNode(LeftNode.Type, ParsedTree, 1);
 				return Gamma.Generator.CreateSelfAssignNode(LeftNode.Type, ParsedTree, null, LeftNode, ConstNode);
 			}
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "neither incremental nor decrimental");
 		}
 		return LeftNode.IsError() ? LeftNode : GreenTeaGrammar.TypeUnary(Gamma, ParsedTree, Type);
 	}
 
 	public static GtSyntaxTree ParseError(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 	}
 
 	public static GtNode TypeError(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateErrorNode(Gamma.VoidType, ParsedTree);
 	}
 
 	public static GtSyntaxTree ParseEmpty(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetBeforeToken(), null);
 	}
 
 	public static GtNode TypeEmpty(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 	}
 
 	public static GtSyntaxTree ParseSemiColon(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		if(TokenContext.IsAllowedBackTrack()) {
 			return null;
 		}
 		else {
 			return TokenContext.ReportTokenError(TokenContext.GetToken(), "unexpected ;", false);
 		}
 	}
 
 	public static GtSyntaxTree ParseRequire(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		TokenContext.Next(); // skipped first token "require";
 		while(TokenContext.HasNext()) {
 			/*local*/GtToken Token = TokenContext.Next();
 			if(Token.IsIndent() || Token.IsDelim()) {
 				break;
 			}
 			if(Token.IsNameSymbol()) {
 				if(!NameSpace.LoadRequiredLib(Token.ParsedText)) {
 					return TokenContext.NewErrorSyntaxTree(Token, "failed to load required library: " + Token.ParsedText);
 				}
 			}
 			if(TokenContext.MatchToken(",")) {
 				continue;
 			}
 		}
 		return GreenTeaGrammar.ParseEmpty(NameSpace, TokenContext, LeftTree, Pattern);
 	}
 
 	public static GtSyntaxTree ParseImport(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ImportTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "import");
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/String PackageName = LibGreenTea.UnquoteString(Token.ParsedText);
 		while(TokenContext.HasNext()) {
 			Token = TokenContext.Next();
 			if(Token.IsNameSymbol() || LibGreenTea.EqualsString(Token.ParsedText, ".")) {
 				PackageName += Token.ParsedText;
 				continue;
 			}
 			break;
 		}
 		ImportTree.ParsedValue = PackageName;
 		return ImportTree;
 	}
 
 	public static GtNode TypeImport(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		/*local*/Object Value = Gamma.Generator.ImportNativeObject(Type, (/*cast*/String)ParsedTree.ParsedValue);
 		if(Value == null) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "cannot import: " + ParsedTree.ParsedValue);
 		}
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(Value), ParsedTree, Value);
 	}
 
 	public static GtSyntaxTree ParseBlock(GtNameSpace ParentNameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		if(TokenContext.MatchToken("{")) {
 			/*local*/GtSyntaxTree PrevTree = null;
 			/*local*/GtNameSpace NameSpace = new GtNameSpace(ParentNameSpace.Context, ParentNameSpace);
 			while(TokenContext.HasNext()) {
 				TokenContext.SkipEmptyStatement();
 				if(TokenContext.MatchToken("}")) {
 					break;
 				}
 				/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 				/*local*/GtSyntaxTree ParsedTree = GreenTeaUtils.ParseExpression(NameSpace, TokenContext, false/*SuffixOnly*/);
 				if(GreenTeaUtils.IsMismatchedOrError(ParsedTree)) {
 					return ParsedTree;
 				}
 				ParsedTree.SetAnnotation(Annotation);
 				//PrevTree = GtStatic.TreeTail(GtStatic.LinkTree(PrevTree, GtStatic.TreeHead(CurrentTree)));
 				if(ParsedTree.PrevTree != null) {
 					ParsedTree = GreenTeaUtils.TreeHead(ParsedTree);
 				}
 				PrevTree = GreenTeaUtils.LinkTree(PrevTree, ParsedTree);
 				TokenContext.SkipIncompleteStatement();  // check; and skip empty statement
 			}
 			if(PrevTree == null) {
 				return TokenContext.ParsePattern(NameSpace, "$Empty$", Required);
 			}
 			return GreenTeaUtils.TreeHead(PrevTree);
 		}
 		return null;
 	}
 
 	public static GtSyntaxTree ParseStatement(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree StmtTree = TokenContext.ParsePattern(NameSpace, "$Block$", Optional);
 		if(StmtTree == null) {
 			StmtTree = TokenContext.ParsePattern(NameSpace, "$Expression$", Optional);
 		}
 		if(StmtTree == null) {
 			StmtTree = TokenContext.ParsePattern(NameSpace, "$Empty$", Required);
 		}
 		return StmtTree;
 	}
 
 	// If Statement
 	public static GtSyntaxTree ParseIf(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree NewTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "if");
 		NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 		NewTree.SetMatchedPatternAt(IfCond, NameSpace, TokenContext, "$Expression$", Required);
 		NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required | CloseSkipIndent);
 		NewTree.SetMatchedPatternAt(IfThen, NameSpace, TokenContext, "$Statement$", AllowLineFeed | Required);
 		TokenContext.SkipEmptyStatement();
 		if(TokenContext.MatchToken2("else", AllowLineFeed)) {
 			NewTree.SetMatchedPatternAt(IfElse, NameSpace, TokenContext, "$Statement$", AllowLineFeed | Required);
 		}
 		return NewTree;
 	}
 
 	public static GtNode TypeIf(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckAt(IfCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode ThenNode = ParsedTree.TypeCheckAt(IfThen, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 		/*local*/GtNode ElseNode = ParsedTree.TypeCheckAt(IfElse, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateIfNode(ThenNode.Type, ParsedTree, CondNode, ThenNode, ElseNode);
 	}
 
 	// While Statement
 	public static GtSyntaxTree ParseWhile(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree WhileTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "while");
 		WhileTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 		WhileTree.SetMatchedPatternAt(WhileCond, NameSpace, TokenContext, "$Expression$", Required);
 		WhileTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required | CloseSkipIndent);
 		WhileTree.SetMatchedPatternAt(WhileBody, NameSpace, TokenContext, "$Statement$", Required);
 		return WhileTree;
 	}
 
 	public static GtNode TypeWhile(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckAt(WhileCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode BodyNode =  ParsedTree.TypeCheckAt(WhileBody, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateWhileNode(BodyNode.Type, ParsedTree, CondNode, BodyNode);
 	}
 
 	// DoWhile Statement
 	public static GtSyntaxTree ParseDoWhile(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree Tree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "do");
 		Tree.SetMatchedPatternAt(WhileBody, NameSpace, TokenContext, "$Statement$", Required);
 		Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "while", Required);
 		Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 		Tree.SetMatchedPatternAt(WhileCond, NameSpace, TokenContext, "$Expression$", Required);
 		Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required | CloseSkipIndent);
 		return Tree;
 	}
 
 	public static GtNode TypeDoWhile(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckAt(WhileCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode BodyNode =  ParsedTree.TypeCheckAt(WhileBody, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateDoWhileNode(BodyNode.Type, ParsedTree, CondNode, BodyNode);
 	}
 
 	// For Statement
 	public static GtSyntaxTree ParseFor(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree Tree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "for");
 		Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 		Tree.SetMatchedPatternAt(ForInit, NameSpace, TokenContext, "$Expression$", Optional);
 		Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ";", Required);
 		Tree.SetMatchedPatternAt(ForCond, NameSpace, TokenContext, "$Expression$", Optional);
 		Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ";", Required);
 		Tree.SetMatchedPatternAt(ForIteration, NameSpace, TokenContext, "$Expression$", Optional);
 		Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required | CloseSkipIndent);
 		Tree.SetMatchedPatternAt(ForBody, NameSpace, TokenContext, "$Statement$", Required);
 		return Tree;
 	}
 
 	public static GtNode TypeFor(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode InitNode = null;
 		/*local*/GtNode CondNode = null;
 		/*local*/GtNode IterNode = null;
 		if(ParsedTree.HasNodeAt(ForInit)) {
 			InitNode =  ParsedTree.TypeCheckAt(ForInit, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		}
 		if(ParsedTree.HasNodeAt(ForCond)) {
 			CondNode =  ParsedTree.TypeCheckAt(ForCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		}
 		if(ParsedTree.HasNodeAt(ForIteration)) {
 			IterNode =  ParsedTree.TypeCheckAt(ForIteration, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		}
 		/*local*/GtNode BodyNode =  ParsedTree.TypeCheckAt(ForBody, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 		/*local*/GtNode ForNode = Gamma.Generator.CreateForNode(BodyNode.Type, ParsedTree, CondNode, IterNode, BodyNode);
 		if(InitNode != null) {
 			if(InitNode instanceof VarNode) {
 				((/*cast*/VarNode)InitNode).BlockNode = ForNode;
 			}			else {
 				InitNode = GreenTeaUtils.LinkNode(InitNode, ForNode);
 			}
 			return InitNode;
 		}
 		return ForNode;
 	}
 
 	// Break/Continue Statement
 	public static GtSyntaxTree ParseBreak(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "break");
 	}
 
 	public static GtNode TypeBreak(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.Generator.CreateBreakNode(Gamma.VoidType, ParsedTree, "");
 	}
 
 	public static GtSyntaxTree ParseContinue(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
		return TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "break");
 	}
 
 	public static GtNode TypeContinue(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.Generator.CreateContinueNode(Gamma.VoidType, ParsedTree, "");
 	}
 
 	// Return Statement
 	public static GtSyntaxTree ParseReturn(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ReturnTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "return");
 		ReturnTree.SetMatchedPatternAt(ReturnExpr, NameSpace, TokenContext, "$Expression$", Optional);
 		return ReturnTree;
 	}
 
 	public static GtNode TypeReturn(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		ParsedTree.NextTree = null; // stop typing of next trees
 		if(Gamma.IsTopLevel() || Gamma.Func == null) {
 			return Gamma.UnsupportedTopLevelError(ParsedTree);
 		}
 		/*local*/GtType ReturnType = Gamma.Func.GetReturnType();
 		if(ParsedTree.HasNodeAt(ReturnExpr)) {
 			/*local*/GtNode Expr = ParsedTree.TypeCheckAt(ReturnExpr, Gamma, ReturnType, DefaultTypeCheckPolicy);
 			if(ReturnType == Gamma.VarType && !Expr.IsError()) {
 				Gamma.Func.Types[0] = Expr.Type;
 				Gamma.ReportTypeInference(ParsedTree.KeyToken, "return value of " + Gamma.Func.FuncName, Expr.Type);
 			}
 			if(ReturnType == Gamma.VoidType) {
 				Gamma.Context.ReportError(WarningLevel, ParsedTree.KeyToken, "ignored return value");
 				return Gamma.Generator.CreateReturnNode(ReturnType, ParsedTree, null);
 			}
 			return Gamma.Generator.CreateReturnNode(Expr.Type, ParsedTree, Expr);
 		}
 		else {
 			if(ReturnType == Gamma.VarType) {
 				Gamma.Func.Types[0] = Gamma.VoidType;
 				Gamma.ReportTypeInference(ParsedTree.KeyToken, "return value of " + Gamma.Func.FuncName, Gamma.VoidType);
 			}
 			if(Gamma.Func.Is(ConstructorFunc)) {
 				/*local*/GtNode ThisNode = Gamma.CreateLocalNode(ParsedTree, Gamma.Generator.GetRecvName());
 				return Gamma.Generator.CreateReturnNode(ThisNode.Type, ParsedTree, ThisNode);
 			}
 			if(ReturnType != Gamma.VoidType) {
 				Gamma.Context.ReportError(WarningLevel, ParsedTree.KeyToken, "returning default value of " + ReturnType);
 				return Gamma.Generator.CreateReturnNode(ReturnType, ParsedTree, Gamma.CreateDefaultValue(ParsedTree, ReturnType));
 			}
 			return Gamma.Generator.CreateReturnNode(ReturnType, ParsedTree, null);
 		}
 	}
 
 	// try
 	public static GtSyntaxTree ParseTry(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree TryTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "try");
 		TryTree.SetMatchedPatternAt(TryBody, NameSpace, TokenContext, "$Block$", Required);
 		TokenContext.SkipEmptyStatement();
 		if(TokenContext.MatchToken("catch")) {
 			TryTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 			TryTree.SetMatchedPatternAt(CatchVariable, NameSpace, TokenContext, "$VarDecl$", Required);
 			TryTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required | CloseSkipIndent);
 			TryTree.SetMatchedPatternAt(CatchBody, NameSpace, TokenContext, "$Block$", Required);
 		}
 		TokenContext.SkipEmptyStatement();
 		if(TokenContext.MatchToken("finally")) {
 			TryTree.SetMatchedPatternAt(FinallyBody, NameSpace, TokenContext, "$Block$", Required);
 		}
 		return TryTree;
 	}
 
 	public static GtNode TypeTry(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode TryNode = ParsedTree.TypeCheckAt(TryBody, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 		/*local*/GtNode CatchExpr = null;
 		/*local*/GtNode CatchNode = null;
 		if(ParsedTree.HasNodeAt(CatchVariable)) {
 			CatchExpr = ParsedTree.TypeCheckAt(CatchVariable, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 			CatchNode = ParsedTree.TypeCheckAt(CatchBody, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 		}
 		/*local*/GtNode FinallyNode = ParsedTree.TypeCheckAt(FinallyBody, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateTryNode(TryNode.Type, ParsedTree, TryNode, CatchExpr, CatchNode, FinallyNode);
 	}
 
 	// throw $Expr$
 	public static GtSyntaxTree ParseThrow(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ThrowTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "throw");
 		ThrowTree.SetMatchedPatternAt(ReturnExpr, NameSpace, TokenContext, "$Expression$", Required);
 		return ThrowTree;
 	}
 
 	public static GtNode TypeThrow(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType FaultType = ContextType; // FIXME Gamma.FaultType;
 		/*local*/GtNode ExprNode = ParsedTree.TypeCheckAt(ReturnExpr, Gamma, FaultType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateThrowNode(ExprNode.Type, ParsedTree, ExprNode);
 	}
 
 	public static GtSyntaxTree ParseThis(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "this");
 	}
 
 	public static GtNode TypeThis(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.CreateLocalNode(ParsedTree, Gamma.Generator.GetRecvName());
 	}
 
 	public static GtSyntaxTree ParseLine(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "__line__");
 	}
 
 	public static GtNode TypeLine(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, Gamma.Context.GetSourcePosition(ParsedTree.KeyToken.FileLine));
 	}
 	
 	public static GtSyntaxTree ParseSuper(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree Tree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "super");
 //		/*local*/int ParseFlag = TokenContext.SetSkipIndent(true);
 //		Tree.SetSyntaxTreeAt(0, new GtSyntaxTree(NameSpace.GetSyntaxPattern("$Variable$"), NameSpace, Token, null));
 //		Tree.SetSyntaxTreeAt(1,  new GtSyntaxTree(NameSpace.GetSyntaxPattern("this"), NameSpace, new GtToken("this", 0), null));
 //		TokenContext.MatchToken("(");
 //		if(!TokenContext.MatchToken(")")) {
 //			while(!Tree.IsMismatchedOrError()) {
 //				Tree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Required);
 //				if(TokenContext.MatchToken(")")) {
 //					break;
 //				}
 //				Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 //			}
 //		}
 //		TokenContext.ParseFlag = ParseFlag;
 //		if(!Tree.IsMismatchedOrError()) {
 //			// translate '$super$(this, $Params$)' => 'super(this, $Params$)'
 //			Tree.Pattern = NameSpace.GetExtendedSyntaxPattern("(");
 //			return Tree;
 //		}
 		return Tree;
 	}
 
 	// new $Type ( $Expr$ [, $Expr$] )
 	public static GtSyntaxTree ParseNew(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree NewTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "new");
 		NewTree.SetMatchedPatternAt(0, NameSpace, TokenContext, "$Type$", Optional);
 		if(!NewTree.HasNodeAt(0)) {
 			NewTree.SetSyntaxTreeAt(0, NewTree.CreateConstTree(NameSpace.Context.VarType)); // TODO
 		}
 		/*local*/int ParseFlag = TokenContext.SetSkipIndent(true);
 		NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		if(!TokenContext.MatchToken(")")) {
 			while(!NewTree.IsMismatchedOrError()) {
 				NewTree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Required);
 				if(TokenContext.MatchToken(")")) {
 					break;
 				}
 				NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 			}
 		}
 		TokenContext.SetRememberFlag(ParseFlag);
 		return NewTree;
 	}
 
 	// switch
 	public static GtSyntaxTree ParseEnum(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/String EnumTypeName = null;
 		/*local*/GtType NewEnumType = null;
 		/*local*/GtMap EnumMap = new GtMap();
 		/*local*/GtSyntaxTree EnumTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "enum");
 		EnumTree.SetMatchedPatternAt(EnumNameTreeIndex, NameSpace, TokenContext, "$FuncName$", Required);  // $ClassName$ is better
 		if(!EnumTree.IsMismatchedOrError()) {
 			EnumTypeName = EnumTree.GetSyntaxTreeAt(EnumNameTreeIndex).KeyToken.ParsedText;
 			NewEnumType = NameSpace.Context.TenumType.CreateSubType(EnumType, EnumTypeName, null, EnumMap);
 		}
 		EnumTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "{", Required);
 		/*local*/int EnumValue = 0;
 		/*local*/ArrayList<GtToken> NameList = new ArrayList<GtToken>();
 		while(!EnumTree.IsMismatchedOrError()) {
 			TokenContext.SkipIndent();
 			if(TokenContext.MatchToken(",")) {
 				continue;
 			}
 			if(TokenContext.MatchToken("}")) {
 				break;
 			}
 			/*local*/GtToken Token = TokenContext.Next();
 			if(LibGreenTea.IsVariableName(Token.ParsedText, 0)) {
 				if(EnumMap.get(Token.ParsedText) != null) {
 					NameSpace.Context.ReportError(ErrorLevel, Token, "duplicated name: " + Token.ParsedText);
 					continue;
 				}
 				NameList.add(Token);
 				EnumMap.put(Token.ParsedText, new GreenTeaEnum(NewEnumType, EnumValue, Token.ParsedText));
 				EnumValue += 1;
 				continue;
 			}
 		}
 		if(!EnumTree.IsMismatchedOrError()) {
 			/*local*/GtNameSpace StoreNameSpace = NameSpace.GetNameSpace(GreenTeaGrammar.ParseNameSpaceFlag(0, TokenContext.ParsingAnnotation));
 			StoreNameSpace.AppendTypeName(NewEnumType, EnumTree.GetSyntaxTreeAt(EnumNameTreeIndex).KeyToken);
 			/*local*/int i = 0;
 			while(i < LibGreenTea.ListSize(NameList)) {
 				/*local*/String Key = NameList.get(i).ParsedText;
 				StoreNameSpace.SetSymbol(ClassSymbol(NewEnumType, ClassStaticName(Key)), EnumMap.get(Key), NameList.get(i));
 				i = i + 1;
 			}
 			EnumTree.ParsedValue = NewEnumType;
 		}
 		return EnumTree;
 	}
 
 	public static GtNode TypeEnum(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/Object EnumType = ParsedTree.ParsedValue;
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(EnumType), ParsedTree, EnumType);
 	}
 
 	public static GtSyntaxTree ParseCaseBlock(GtNameSpace ParentNameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree PrevTree = null;
 		/*local*/GtNameSpace NameSpace = new GtNameSpace(ParentNameSpace.Context, ParentNameSpace);
 		/*local*/boolean IsCaseBlock = TokenContext.MatchToken("{"); // case EXPR : {}
 		while(TokenContext.HasNext()) {
 			TokenContext.SkipEmptyStatement();
 			if(TokenContext.IsToken("case")) {
 				break;
 			}
 			if(TokenContext.IsToken("default")) {
 				break;
 			}
 			if(TokenContext.IsToken("}")) {
 				if(!IsCaseBlock) {
 				}
 				break;
 			}
 			/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 			/*local*/GtSyntaxTree CurrentTree = GreenTeaUtils.ParseExpression(NameSpace, TokenContext, false/*SuffixOnly*/);
 			if(GreenTeaUtils.IsMismatchedOrError(CurrentTree)) {
 				return CurrentTree;
 			}
 			CurrentTree.SetAnnotation(Annotation);
 			PrevTree = GreenTeaUtils.LinkTree(PrevTree, CurrentTree);
 		}
 		if(PrevTree == null) {
 			return TokenContext.ParsePattern(NameSpace, "$Empty$", Required);
 		}
 		return GreenTeaUtils.TreeHead(PrevTree);
 	}
 
 	public static GtSyntaxTree ParseSwitch(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree SwitchTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "switch");
 		SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 		SwitchTree.SetMatchedPatternAt(SwitchCaseCondExpr, NameSpace, TokenContext, "$Expression$", Required);
 		SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required | CloseSkipIndent);
 		SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "{", Required);
 
 		/*local*/int CaseIndex = SwitchCaseCaseIndex;
 		/*local*/int ParseFlag = TokenContext.SetSkipIndent(true);
 		while(!SwitchTree.IsMismatchedOrError() && !TokenContext.MatchToken("}")) {
 			if(TokenContext.MatchToken("case")) {
 				SwitchTree.SetMatchedPatternAt(CaseIndex, NameSpace, TokenContext, "$Expression$", Required);
 				SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 				SwitchTree.SetMatchedPatternAt(CaseIndex + 1, NameSpace, TokenContext, "$CaseBlock$", Required);
 				CaseIndex += 2;
 				continue;
 			}
 			if(TokenContext.MatchToken("default")) {
 				SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 				SwitchTree.SetMatchedPatternAt(SwitchCaseDefaultBlock, NameSpace, TokenContext, "$CaseBlock$", Required);
 			}
 		}
 		TokenContext.SetRememberFlag(ParseFlag);
 		return SwitchTree;
 	}
 
 	public static GtNode TypeSwitch(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckAt(IfCond, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/GtNode DefaultNode = null;
 		if(ParsedTree.HasNodeAt(SwitchCaseDefaultBlock)) {
 			DefaultNode = ParsedTree.TypeCheckAt(SwitchCaseDefaultBlock, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 		}
 		/*local*/GtNode Node = Gamma.Generator.CreateSwitchNode(Gamma.VoidType, ParsedTree, CondNode, DefaultNode);
 		/*local*/int CaseIndex = SwitchCaseCaseIndex;
 		while(CaseIndex < ParsedTree.SubTreeList.size()) {
 			/*local*/GtNode CaseExpr  = ParsedTree.TypeCheckAt(CaseIndex, Gamma, CondNode.Type, DefaultTypeCheckPolicy);
 			/*local*/GtNode CaseBlock = null;
 			if(ParsedTree.HasNodeAt(CaseIndex+1)) {
 				CaseBlock = ParsedTree.TypeCheckAt(CaseIndex+1, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 			}
 			Node.Append(CaseExpr);
 			Node.Append(CaseBlock);
 			CaseIndex += 2;
 		}
 		return Node;
 	}
 
 	// const decl
 	public static GtSyntaxTree ParseSymbolDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree SymbolDeclTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.Next() /*const, let */, null);
 		/*local*/GtSyntaxTree ClassNameTree = TokenContext.ParsePattern(NameSpace, "$Type$", Optional);
 		/*local*/GtType ConstClass = null;
 		if(ClassNameTree != null) {
 			SymbolDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ".", Required);
 			if(!SymbolDeclTree.IsMismatchedOrError()) {
 				SymbolDeclTree.SetSyntaxTreeAt(SymbolDeclClassIndex, ClassNameTree);
 				ConstClass = ClassNameTree.GetParsedType();
 			}
 		}
 		SymbolDeclTree.SetMatchedPatternAt(SymbolDeclNameIndex, NameSpace, TokenContext, "$Variable$", Required);
 		SymbolDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "=", Required);
 		SymbolDeclTree.SetMatchedPatternAt(SymbolDeclValueIndex, NameSpace, TokenContext, "$Expression$", Required);
 		
 		if(!SymbolDeclTree.IsMismatchedOrError()) {
 			/*local*/GtToken SourceToken = SymbolDeclTree.GetSyntaxTreeAt(SymbolDeclNameIndex).KeyToken;
 			/*local*/String ConstName = SourceToken.ParsedText;
 			if(ConstClass != null) {
 				ConstName = ClassSymbol(ConstClass, ClassStaticName(ConstName));
 				SourceToken.AddTypeInfoToErrorMessage(ConstClass);
 			}
 			/*local*/Object ConstValue = null;
 			if(SymbolDeclTree.GetSyntaxTreeAt(SymbolDeclValueIndex).Pattern.EqualsName("$Const$")) {
 				ConstValue = SymbolDeclTree.GetSyntaxTreeAt(SymbolDeclValueIndex).ParsedValue;
 			}
 			if(ConstValue == null) {
 				/*local*/GtTypeEnv Gamma = new GtTypeEnv(NameSpace);
 				/*local*/GtNode Node = SymbolDeclTree.TypeCheckAt(SymbolDeclValueIndex, Gamma, Gamma.VarType, OnlyConstPolicy);
 				if(Node.IsError()) {
 					SymbolDeclTree.ToError(Node.Token);
 					return SymbolDeclTree;
 				}
 				ConstValue = Node.ToConstValue(true);
 			}
 			if(ConstValue instanceof GtType && ((/*cast*/GtType)ConstValue).IsTypeParam()) {  // let T = <var>;
 				((/*cast*/GtType)ConstValue).ShortClassName = ConstName;
 			}
 			/*local*/int NameSpaceFlag = GreenTeaGrammar.ParseNameSpaceFlag(0, TokenContext.ParsingAnnotation);
 			/*local*/GtNameSpace StoreNameSpace = NameSpace.GetNameSpace(NameSpaceFlag);
 			StoreNameSpace.SetSymbol(ConstName, ConstValue, SourceToken);
 		}
 		return SymbolDeclTree;
 	}
 
 	public static GtNode TypeSymbolDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.Generator.CreateEmptyNode(ContextType);
 	}
 
 	// FuncDecl
 	public static GtSyntaxTree ParseFuncName(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/String Name = Token.ParsedText;
 		if(Name.length() > 0 && LibGreenTea.CharAt(Name, 0) != '(' && LibGreenTea.CharAt(Name, 0) != '.') {
 			if(Token.IsQuoted()) {
 				Name = LibGreenTea.UnquoteString(Name);
 			}
 			return new GtSyntaxTree(Pattern, NameSpace, Token, Name);
 		}
 		return TokenContext.ReportExpectedMessage(Token, "name", true);
 	}
 
 	private static void ParseFuncParam(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree FuncDeclTree, GtFuncBlock FuncBlock) {
 		/*local*/int ParamBase = FuncDeclParam;
 		while(!FuncDeclTree.IsMismatchedOrError() && !TokenContext.MatchToken(")")) {
 			TokenContext.SkipIndent();
 			if(ParamBase != FuncDeclParam) {
 				FuncDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 				TokenContext.SkipIndent();
 			}
 			FuncDeclTree.SetMatchedPatternAt(ParamBase + VarDeclType, NameSpace, TokenContext, "$Type$", Required);
 			FuncDeclTree.SetMatchedPatternAt(ParamBase + VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 			if(FuncDeclTree.IsValidSyntax()) {
 				FuncBlock.AddParameter(FuncDeclTree.GetSyntaxTreeAt(ParamBase + VarDeclType).GetParsedType(), FuncDeclTree.GetSyntaxTreeAt(ParamBase + VarDeclName).KeyToken.ParsedText);
 			}
 			if(TokenContext.MatchToken("=")) {
 				FuncDeclTree.SetMatchedPatternAt(ParamBase + VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 			}
 			ParamBase += 3;
 		}
 		TokenContext.SetSkipIndent(false);
 	}
 
 	private static void ParseFuncBody(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree FuncDeclTree, GtFuncBlock FuncBlock) {
 		TokenContext.SkipIndent();
 		if(TokenContext.MatchToken("as")) {
 			/*local*/GtToken Token = TokenContext.Next();
 			FuncBlock.DefinedFunc.SetNativeMacro(LibGreenTea.UnquoteString(Token.ParsedText));
 		}
 		else if(TokenContext.IsToken("import")) {
 			/*local*/GtSyntaxTree ImportTree = TokenContext.ParsePattern(NameSpace, "import", Optional);
 			if(GreenTeaUtils.IsValidSyntax(ImportTree)) {
 				if(!LibGreenTea.ImportNativeMethod(FuncBlock.DefinedFunc, (/*cast*/String)ImportTree.ParsedValue)) {
 					NameSpace.Context.ReportError(WarningLevel, ImportTree.KeyToken, "cannot import: " + ImportTree.ParsedValue);
 				}
 			}
 		}
 		else {
 			/*local*/GtSyntaxTree BlockTree = TokenContext.ParsePattern(NameSpace, "$Block$", Optional);
 			if(GreenTeaUtils.IsValidSyntax(BlockTree)) {
 				FuncBlock.FuncBlock = BlockTree;
 				/*local*/GtSyntaxTree ReturnTree = new GtSyntaxTree(NameSpace.GetSyntaxPattern("return"), NameSpace, BlockTree.KeyToken, null);
 				GreenTeaUtils.LinkTree(GreenTeaUtils.TreeTail(BlockTree), ReturnTree);
 				FuncBlock.DefinedFunc.NativeRef = FuncBlock;
 			}
 		}
 	}
 
 //	public static GtSyntaxTree ParseFunction(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 //		/*local*/GtSyntaxTree FuncDeclTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 //		FuncDeclTree.SetMatchedPatternAt(FuncDeclName, NameSpace, TokenContext, "$FuncName$", Optional);
 //		if(FuncDeclTree.HasNodeAt(FuncDeclName)) {
 //			//NameSpace = ParseFuncGenericParam(NameSpace, TokenContext, FuncDeclTree);
 //		}
 //		FuncDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 //		GreenTeaGrammar.ParseFuncParam(NameSpace, TokenContext, FuncDeclTree);
 //		if(!FuncDeclTree.IsEmptyOrError() && TokenContext.MatchToken(":")) {
 //			FuncDeclTree.SetMatchedPatternAt(FuncDeclReturnType, NameSpace, TokenContext, "$Type$", Required);
 //		}
 //		GreenTeaGrammar.ParseFuncBody(NameSpace, TokenContext, FuncDeclTree);
 //		return FuncDeclTree;
 //	}
 
 	public static GtSyntaxTree ParseFuncDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree FuncDeclTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 		/*local*/int FuncFlag = GreenTeaGrammar.ParseFuncFlag(0, TokenContext.ParsingAnnotation);
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		LibGreenTea.Assert(LeftTree != null);
 		FuncDeclTree.SetSyntaxTreeAt(FuncDeclReturnType, LeftTree);
 		TypeList.add(LeftTree.GetParsedType());
 		FuncDeclTree.SetMatchedPatternAt(FuncDeclName, NameSpace, TokenContext, "$FuncName$", Required);
 		FuncDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 		if(!FuncDeclTree.IsMismatchedOrError()) {
 			/*local*/GtFuncBlock FuncBlock = new GtFuncBlock(NameSpace, TypeList);
 			/*local*/boolean FoundAbstractFunc = false;
 			/*local*/GtToken SourceToken = FuncDeclTree.GetSyntaxTreeAt(FuncDeclName).KeyToken;
 			/*local*/String FuncName = SafeFuncName(SourceToken.ParsedText);
 			/*local*/int ParseFlag = TokenContext.SetBackTrack(false);  // disabled
 			GtNameSpace StoreNameSpace = NameSpace.GetNameSpace(GreenTeaGrammar.ParseNameSpaceFlag(0, TokenContext.ParsingAnnotation));
 			if(LibGreenTea.EqualsString(FuncName, "converter")) {
 				FuncFlag |= ConverterFunc;
 				FuncBlock.SetConverterType();
 				GreenTeaGrammar.ParseFuncParam(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				if(TypeList.size() != 3) {
 					NameSpace.Context.ReportError(ErrorLevel, SourceToken, "converter takes one parameter");
 					FuncDeclTree.ToError(SourceToken);
 					return FuncDeclTree;
 				}
 				FuncName = "to" + TypeList.get(2);
 				FuncBlock.DefinedFunc = NameSpace.Context.Generator.CreateFunc(FuncFlag, FuncName, 0, FuncBlock.TypeList);
 				GreenTeaGrammar.ParseFuncBody(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				SourceToken.ParsedText = FuncName;
 				StoreNameSpace.SetConverterFunc(null, null, FuncBlock.DefinedFunc, SourceToken);
 			}
 			else {
 				FuncBlock.SetThisIfInClass(NameSpace.GetType("This"));
 				GreenTeaGrammar.ParseFuncParam(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				FuncBlock.DefinedFunc = NameSpace.GetFunc(FuncName, 0, TypeList);
 				if(FuncBlock.DefinedFunc == null || !FuncBlock.DefinedFunc.IsAbstract()) {
 					FuncBlock.DefinedFunc = NameSpace.Context.Generator.CreateFunc(FuncFlag, FuncName, 0, TypeList);
 				}
 				else {
 					FoundAbstractFunc = true;
 					FuncBlock.DefinedFunc.FuncFlag = FuncFlag;
 				}
 				GreenTeaGrammar.ParseFuncBody(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				if(!FuncBlock.DefinedFunc.IsAbstract() || !FoundAbstractFunc) { 
 					StoreNameSpace.AppendFunc(FuncBlock.DefinedFunc, SourceToken);
 					GtType RecvType = FuncBlock.DefinedFunc.GetRecvType();
 					if(!RecvType.IsVoidType()) {
 						StoreNameSpace.AppendMethod(FuncBlock.DefinedFunc, SourceToken.AddTypeInfoToErrorMessage(RecvType));
 					}
 				}
 			}
 			FuncDeclTree.ParsedValue = FuncBlock.DefinedFunc;
 			TokenContext.SetRememberFlag(ParseFlag);
 		}
 		return FuncDeclTree;
 	}
 
 	public static GtNode TypeFuncDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		GtFunc DefinedFunc = (/*cast*/GtFunc)ParsedTree.ParsedValue;
 		DefinedFunc.GenerateNativeFunc();
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 	}
 
 	// constructor
 	public static GtSyntaxTree ParseConstructor2(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree FuncDeclTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "constructor");
 		/*local*/GtType ThisType = NameSpace.GetType("This");
 		if(ThisType == null) {
 			NameSpace.Context.ReportError(ErrorLevel, FuncDeclTree.KeyToken, "constructor is used inside class");
 			FuncDeclTree.ToError(FuncDeclTree.KeyToken);
 			return FuncDeclTree;
 		}
 		/*local*/int FuncFlag = GreenTeaGrammar.ParseFuncFlag(ConstructorFunc, TokenContext.ParsingAnnotation);
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		TypeList.add(ThisType);
 		FuncDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 		if(FuncDeclTree.IsValidSyntax()) {
 			/*local*/GtFuncBlock FuncBlock = new GtFuncBlock(NameSpace, TypeList);
 			/*local*/GtToken SourceToken = FuncDeclTree.KeyToken;
 			/*local*/int ParseFlag = TokenContext.SetBackTrack(false);  // disabled
 			/*local*/GtNameSpace StoreNameSpace = NameSpace.GetNameSpace(GreenTeaGrammar.ParseNameSpaceFlag(0, TokenContext.ParsingAnnotation));
 			FuncBlock.SetThisIfInClass(ThisType);
 			GreenTeaGrammar.ParseFuncParam(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 			if(FuncDeclTree.IsValidSyntax()) {
 				FuncBlock.DefinedFunc = NameSpace.Context.Generator.CreateFunc(FuncFlag, ThisType.ShortClassName, 0, FuncBlock.TypeList);
 				GreenTeaGrammar.ParseFuncBody(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				StoreNameSpace.AppendConstructor(ThisType, FuncBlock.DefinedFunc, SourceToken.AddTypeInfoToErrorMessage(ThisType));
 				FuncDeclTree.ParsedValue = FuncBlock.DefinedFunc;
 			}
 			TokenContext.SetRememberFlag(ParseFlag);
 		}
 		return FuncDeclTree;
 	}
 
 	// Array
 	public static GtSyntaxTree ParseArray(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int OldFlag = TokenContext.SetSkipIndent(true);
 		/*local*/GtSyntaxTree ArrayTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "[");
 
 		//FuncTree.AppendParsedTree(LeftTree);
 		while(TokenContext.HasNext() && !ArrayTree.IsMismatchedOrError()) {
 			if(TokenContext.MatchToken("]")) {
 				break;
 			}
 			if(TokenContext.MatchToken(",")) {
 				continue;
 			}
 			ArrayTree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Required);
 		}
 		TokenContext.SetRememberFlag(OldFlag);
 		return ArrayTree;
 	}
 
 	public static GtNode TypeArray(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode ArrayNode = Gamma.Generator.CreateArrayNode(Gamma.ArrayType, ParsedTree);
 		/*local*/GtType ElemType = Gamma.VarType;
 		if(ContextType.IsArrayType()) {
 			ElemType = ContextType.TypeParams[0];
 			ArrayNode.Type = ContextType;
 		}
 		/*local*/int i = 0;
 		while(i < LibGreenTea.ListSize(ParsedTree.SubTreeList)) {
 			/*local*/GtNode Node = ParsedTree.TypeCheckAt(i, Gamma, ElemType, DefaultTypeCheckPolicy);
 			if(Node.IsError()) {
 				return Node;
 			}
 			if(ElemType.IsVarType()) {
 				ElemType = Node.Type;
 				ArrayNode.Type = Gamma.Context.GetGenericType1(Gamma.ArrayType, ElemType, true);
 			}
 			i = i + 1;
 		}
 		return ArrayNode;
 	}
 
 	public static GtSyntaxTree ParseIndexer(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ArrayTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "[");
 		ArrayTree.AppendParsedTree2(LeftTree);
 		/*local*/int OldFlag = TokenContext.SetSkipIndent(true);
 		do {
 			ArrayTree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Required);
 		}
 		while(!ArrayTree.IsMismatchedOrError() && TokenContext.MatchToken(","));
 		ArrayTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "]", Required);
 		TokenContext.SetRememberFlag(OldFlag);
 		return ArrayTree;
 	}
 
 	public static GtNode TypeIndexer(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		/*local*/GtNode ExprNode = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.ArrayType, DefaultTypeCheckPolicy);
 		if(ExprNode.IsError()) {
 			return ExprNode;
 		}
 		/*local*/GtFunc ResolvedFunc = null;
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(ExprNode.Type, "get", true);
 		/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 		ParamList.add(ExprNode);
 		if(PolyFunc != null) {
 			ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 			if(ResolvedFunc != null) {
 				Type = ResolvedFunc.GetReturnType();
 			}
 		}
 		/*local*/GtNode Node = Gamma.Generator.CreateIndexerNode(Type, ParsedTree, ResolvedFunc, ExprNode);
 		Node.AppendNodeList(ParamList);
 		return Node;
 	}
 
 	public static GtSyntaxTree ParseSize(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ArrayTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "|");
 		/*local*/GtSyntaxTree SubTree = GreenTeaUtils.ParseExpression(NameSpace, TokenContext, true/*SuffixOnly*/);
 		ArrayTree.SetSyntaxTreeAt(UnaryTerm, SubTree);
 		ArrayTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "|", Required);
 		return ArrayTree;
 	}
 
 	public static GtNode TypeSize(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		/*local*/GtNode ExprNode = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ExprNode.IsError()) {
 			return ExprNode;
 		}
 		if(!(ExprNode.Type.Accept(Gamma.ArrayType) || ExprNode.Type.Accept(Gamma.StringType))) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, ExprNode.Type + " has no sizeof operator");
 		}
 		/*local*/GtPolyFunc PolyFunc = Gamma.NameSpace.GetMethod(ExprNode.Type, "length", true);
 		/*local*/ArrayList<GtNode> NodeList = new ArrayList<GtNode>();
 		/*local*/GtFunc Func = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, NodeList);
 		/*local*/GtNode Node = Gamma.Generator.CreateApplyNode(Type, ParsedTree, Func);
 		Node.Append(ExprNode);
 		return Node;
 	}
 
 	public static GtSyntaxTree ParseSlice(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ArrayTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "[");
 		ArrayTree.AppendParsedTree2(LeftTree);
 		/*local*/GtSyntaxTree Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Optional);
 		if(Tree == null) {
 			ArrayTree.AppendParsedTree2(GreenTeaGrammar.ParseEmpty(NameSpace, TokenContext, LeftTree, Pattern));
 		}
 		else {
 			ArrayTree.AppendParsedTree2(Tree);
 		}
 		ArrayTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 		ArrayTree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Optional);
 		ArrayTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "]", Required);
 		return ArrayTree;
 	}
 
 	public static GtNode TypeSlice(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return null;
 	}
 
 	// ClassDecl
 
 	private static boolean TypeFieldDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtClassField ClassField) {
 		/*local*/int    FieldFlag = GreenTeaGrammar.ParseVarFlag(0, ParsedTree.Annotation);
 		/*local*/GtType DeclType = ParsedTree.GetSyntaxTreeAt(VarDeclType).GetParsedType();
 		/*local*/String FieldName = ParsedTree.GetSyntaxTreeAt(VarDeclName).KeyToken.ParsedText;
 		/*local*/GtNode InitValueNode = null;
 		/*local*/Object InitValue = null;
 		if(ParsedTree.HasNodeAt(VarDeclValue)) {
 			InitValueNode = ParsedTree.TypeCheckAt(VarDeclValue, Gamma, DeclType, OnlyConstPolicy | NullablePolicy);
 			if(InitValueNode.IsError()) {
 				return false;
 			}
 			InitValue = InitValueNode.ToConstValue(true);
 		}
 		if(GreenTeaUtils.UseLangStat) {
 			Gamma.Context.Stat.VarDecl += 1;
 		}/*EndOfStat*/
 		if(DeclType.IsVarType()) {
 			if(InitValueNode == null) {
 				DeclType = Gamma.AnyType;
 			}
 			else {
 				DeclType = InitValueNode.Type;
 			}
 			Gamma.ReportTypeInference(ParsedTree.KeyToken, FieldName, DeclType);
 			if(GreenTeaUtils.UseLangStat) {
 				Gamma.Context.Stat.VarDeclInfer += 1;
 				if(DeclType.IsAnyType()) {
 					Gamma.Context.Stat.VarDeclInferAny += 1;
 				}
 			}/*EndOfStat*/
 		}
 		if(GreenTeaUtils.UseLangStat) {
 			if(DeclType.IsAnyType()) {
 				Gamma.Context.Stat.VarDeclAny += 1;
 			}
 		}/*EndOfStat*/
 		if(InitValueNode == null) {
 			InitValue = DeclType.DefaultNullValue;
 		}
 		ClassField.CreateField(FieldFlag, DeclType, FieldName, ParsedTree.GetSyntaxTreeAt(VarDeclName).KeyToken, InitValue);
 		return true;
 	}
 
 	
 	public static GtSyntaxTree ParseClassDecl2(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ClassDeclTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "class");
 		ClassDeclTree.SetMatchedPatternAt(ClassDeclName, NameSpace, TokenContext, "$FuncName$", Required); //$ClassName$ is better
 		if(TokenContext.MatchToken("extends")) {
 			ClassDeclTree.SetMatchedPatternAt(ClassDeclSuperType, NameSpace, TokenContext, "$Type$", Required);
 		}
 		if(ClassDeclTree.IsMismatchedOrError()) {
 			return ClassDeclTree;
 		}
 		// define new class
 //		/*local*/int ParseFlag = TokenContext.SetBackTrack(false);
 		/*local*/GtNameSpace ClassNameSpace = new GtNameSpace(NameSpace.Context, NameSpace);
 		/*local*/GtToken NameToken = ClassDeclTree.GetSyntaxTreeAt(ClassDeclName).KeyToken;
 		/*local*/GtType SuperType = NameSpace.Context.StructType;
 		if(ClassDeclTree.HasNodeAt(ClassDeclSuperType)) {
 			SuperType = ClassDeclTree.GetSyntaxTreeAt(ClassDeclSuperType).GetParsedType();
 		}
 		/*local*/int ClassFlag = GreenTeaGrammar.ParseClassFlag(0, TokenContext.ParsingAnnotation);
 		/*local*/String ClassName = NameToken.ParsedText;
 		/*local*/GtType DefinedType = NameSpace.GetType(ClassName);
 		if(DefinedType != null && DefinedType.IsAbstract()) {
 			DefinedType.ClassFlag = ClassFlag;
 			DefinedType.SuperType = SuperType;
 			NameToken = null; // preventing duplicated symbol message at (A)
 		}
 		else {
 			DefinedType = SuperType.CreateSubType(ClassFlag, ClassName, null, null);
 			ClassNameSpace.AppendTypeName(DefinedType, NameToken);  // temporary
 		}
 		ClassNameSpace.SetSymbol("This", DefinedType, NameToken);
 //		System.err.println("class = " + ClassName + ", isAbstract = " + DefinedType.IsAbstract());
 		ClassDeclTree.SetMatchedPatternAt(ClassDeclBlock, ClassNameSpace, TokenContext, "$Block$", Optional);
 		if(ClassDeclTree.HasNodeAt(ClassDeclBlock)) {
 			/*local*/GtClassField ClassField = new GtClassField(DefinedType, NameSpace);
 			/*local*/GtTypeEnv Gamma = new GtTypeEnv(ClassNameSpace);
 			/*local*/GtSyntaxTree SubTree = ClassDeclTree.GetSyntaxTreeAt(ClassDeclBlock);
 			while(SubTree != null) {
 				if(SubTree.Pattern.EqualsName("$VarDecl$")) {
 					GreenTeaGrammar.TypeFieldDecl(Gamma, SubTree, ClassField);
 				}
 				SubTree = SubTree.NextTree;
 			}
 			ClassDeclTree.ParsedValue = ClassField;
 		}
 		if(ClassDeclTree.IsValidSyntax()) {
 			NameSpace.AppendTypeName(DefinedType, NameToken);   /* (A) */
 		}
 		return ClassDeclTree;
 	}
 
 	public static GtNode TypeClassDecl2(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtClassField ClassField = (/*cast*/GtClassField)ParsedTree.ParsedValue;
 		if(ClassField != null) {
 			/*local*/GtType DefinedType = ClassField.DefinedType;
 			DefinedType.SetClassField(ClassField);
 			Gamma.Generator.OpenClassField(DefinedType, ClassField);
 			/*local*/GtSyntaxTree SubTree = ParsedTree.GetSyntaxTreeAt(ClassDeclBlock);
 			/*local*/ArrayList<GtFunc> MemberList = new ArrayList<GtFunc>();
 			while(SubTree != null) {
 				if(SubTree.Pattern.EqualsName("$FuncDecl$") || SubTree.Pattern.EqualsName("$Constructor2$")) {
 					MemberList.add((/*cast*/GtFunc)SubTree.ParsedValue);
 				}
 				if(!SubTree.Pattern.EqualsName("$VarDecl$")) {
 					SubTree.TypeCheck(Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 				}
 				SubTree = SubTree.NextTree;
 			}
 			Gamma.Generator.CloseClassField(DefinedType, MemberList);
 		}
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 	}
 	
 	@Override public void LoadTo(GtNameSpace NameSpace) {
 		// Define Constants
 		/*local*/GtParserContext ParserContext = NameSpace.Context;
 		NameSpace.SetSymbol("true", true, null);
 		NameSpace.SetSymbol("false", false, null);
 
 		NameSpace.AppendTokenFunc(" \t", LoadTokenFunc(ParserContext, this, "WhiteSpaceToken"));
 		NameSpace.AppendTokenFunc("\n",  LoadTokenFunc(ParserContext, this, "IndentToken"));
 		NameSpace.AppendTokenFunc(";", LoadTokenFunc(ParserContext, this, "SemiColonToken"));
 		NameSpace.AppendTokenFunc("{}()[]<>.,?:+-*/%=&|!@~^$", LoadTokenFunc(ParserContext, this, "OperatorToken"));
 		NameSpace.AppendTokenFunc("/", LoadTokenFunc(ParserContext, this, "CommentToken"));  // overloading
 		NameSpace.AppendTokenFunc("Aa_", LoadTokenFunc(ParserContext, this, "SymbolToken"));
 
 		NameSpace.AppendTokenFunc("\"", LoadTokenFunc(ParserContext, this, "StringLiteralToken"));
 		NameSpace.AppendTokenFunc("\"", LoadTokenFunc(ParserContext, this, "StringLiteralToken_StringInterpolation"));
 		NameSpace.AppendTokenFunc("'", LoadTokenFunc(ParserContext, this, "CharLiteralToken"));
 		NameSpace.AppendTokenFunc("1",  LoadTokenFunc(ParserContext, this, "NumberLiteralToken"));
 //#ifdef JAVA
 		GtFunc ParseUnary     = LoadParseFunc(ParserContext, this, "ParseUnary");
 		GtFunc  TypeUnary      = LoadTypeFunc(ParserContext, this, "TypeUnary");
 		GtFunc ParseBinary    = LoadParseFunc(ParserContext, this, "ParseBinary");
 		GtFunc  TypeBinary     = LoadTypeFunc(ParserContext, this, "TypeBinary");
 		GtFunc  TypeConst      = LoadTypeFunc(ParserContext, this, "TypeConst");
 //endif VAJA
 		NameSpace.AppendSyntax("+", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("-", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("~", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("! not", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("++ --", LoadParseFunc(ParserContext, this, "ParseIncl"), LoadTypeFunc(ParserContext, this, "TypeIncl"));
 
 		NameSpace.AppendExtendedSyntax("* / % mod", PrecedenceCStyleMUL, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("+ -", PrecedenceCStyleADD, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("< <= > >=", PrecedenceCStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("== !=", PrecedenceCStyleEquals, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("<< >>", PrecedenceCStyleSHIFT, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("&", PrecedenceCStyleBITAND, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("|", PrecedenceCStyleBITOR, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("^", PrecedenceCStyleBITXOR, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("=", PrecedenceCStyleAssign | LeftJoin, ParseBinary, LoadTypeFunc(ParserContext, this, "TypeAssign"));
 		NameSpace.AppendExtendedSyntax("+= -= *= /= %= <<= >>= & | ^=", PrecedenceCStyleAssign, ParseBinary, LoadTypeFunc(ParserContext, this, "TypeSelfAssign"));
 		NameSpace.AppendExtendedSyntax("++ --", 0, LoadParseFunc(ParserContext, this, "ParseIncl"), LoadTypeFunc(ParserContext, this, "TypeIncl"));
 
 		NameSpace.AppendExtendedSyntax("&& and", PrecedenceCStyleAND, ParseBinary, LoadTypeFunc(ParserContext, this, "TypeAnd"));
 		NameSpace.AppendExtendedSyntax("|| or", PrecedenceCStyleOR, ParseBinary, LoadTypeFunc(ParserContext, this, "TypeOr"));
 		NameSpace.AppendExtendedSyntax("<: instanceof", PrecedenceInstanceof, ParseBinary, LoadTypeFunc(ParserContext, this, "TypeInstanceOf"));
 
 		NameSpace.AppendExtendedSyntax("?", 0, LoadParseFunc(ParserContext, this, "ParseTrinary"), LoadTypeFunc(ParserContext, this, "TypeTrinary"));
 
 		NameSpace.AppendSyntax("$Error$", LoadParseFunc(ParserContext, this, "ParseError"), LoadTypeFunc(ParserContext, this, "TypeError"));
 		NameSpace.AppendSyntax("$Empty$", LoadParseFunc(ParserContext, this, "ParseEmpty"), LoadTypeFunc(ParserContext, this, "TypeEmpty"));
 		NameSpace.AppendSyntax(";", LoadParseFunc(ParserContext, this, "ParseSemiColon"), null);
 		NameSpace.AppendSyntax("$Symbol$", LoadParseFunc(ParserContext, this, "ParseSymbol"), null);
 		NameSpace.AppendSyntax("$Type$", LoadParseFunc(ParserContext, this, "ParseType"), TypeConst);
 		NameSpace.AppendSyntax("$TypeSuffix$", LoadParseFunc(ParserContext, this, "ParseTypeSuffix"), null);
 		NameSpace.AppendSyntax("<", LoadParseFunc(ParserContext, this, "ParseTypeParam"), LoadTypeFunc(ParserContext, this, "TypeTypeParam"));
 		NameSpace.AppendSyntax("$Variable$", LoadParseFunc(ParserContext, this, "ParseVariable"), LoadTypeFunc(ParserContext, this, "TypeVariable"));
 		NameSpace.AppendSyntax("$Const$", LoadParseFunc(ParserContext, this, "ParseConst"), TypeConst);
 		NameSpace.AppendSyntax("$CharLiteral$", LoadParseFunc(ParserContext, this, "ParseCharLiteral"), LoadTypeFunc(ParserContext, this, "TypeCharLiteral"));
 		NameSpace.AppendSyntax("$StringLiteral$", LoadParseFunc(ParserContext, this, "ParseStringLiteral"), TypeConst);
 		NameSpace.AppendSyntax("$IntegerLiteral$", LoadParseFunc(ParserContext, this, "ParseIntegerLiteral"), TypeConst);
 		NameSpace.AppendSyntax("$FloatLiteral$", LoadParseFunc(ParserContext, this, "ParseFloatLiteral"), TypeConst);
 
 		NameSpace.AppendExtendedSyntax(".", 0, LoadParseFunc(ParserContext, this, "ParseGetter"), LoadTypeFunc(ParserContext, this, "TypeGetter"));
 		NameSpace.AppendSyntax("(", LoadParseFunc(ParserContext, this, "ParseGroup"), LoadTypeFunc(ParserContext, this, "TypeGroup"));
 		NameSpace.AppendSyntax("(", LoadParseFunc(ParserContext, this, "ParseCast"), LoadTypeFunc(ParserContext, this, "TypeCast"));
 		NameSpace.AppendExtendedSyntax("(", 0, LoadParseFunc(ParserContext, this, "ParseApply"), LoadTypeFunc(ParserContext, this, "TypeApply"));
 		NameSpace.AppendSyntax("[", LoadParseFunc(ParserContext, this, "ParseArray"), LoadTypeFunc(ParserContext, this, "TypeArray"));
 		NameSpace.AppendExtendedSyntax("[", 0, LoadParseFunc(ParserContext, this, "ParseIndexer"), LoadTypeFunc(ParserContext, this, "TypeIndexer"));
 		NameSpace.AppendSyntax("|", LoadParseFunc(ParserContext, this, "ParseSize"), LoadTypeFunc(ParserContext, this, "TypeSize"));
 
 		NameSpace.AppendSyntax("$Block$", LoadParseFunc(ParserContext, this, "ParseBlock"), null);
 		NameSpace.AppendSyntax("$Statement$", LoadParseFunc(ParserContext, this, "ParseStatement"), null);
 		NameSpace.AppendSyntax("$Expression$", LoadParseFunc(ParserContext, this, "ParseExpression"), null);
 
 		NameSpace.AppendSyntax("$FuncName$", LoadParseFunc(ParserContext, this, "ParseFuncName"), TypeConst);
 		NameSpace.AppendSyntax("$FuncDecl$", LoadParseFunc(ParserContext, this, "ParseFuncDecl"), LoadTypeFunc(ParserContext, this, "TypeFuncDecl"));
 		NameSpace.AppendSyntax("$VarDecl$",  LoadParseFunc(ParserContext, this, "ParseVarDecl"), LoadTypeFunc(ParserContext, this, "TypeVarDecl"));
 
 		NameSpace.AppendSyntax("null", LoadParseFunc(ParserContext, this, "ParseNull"), LoadTypeFunc(ParserContext, this, "TypeNull"));
 		NameSpace.AppendSyntax("defined", LoadParseFunc(ParserContext, this, "ParseDefined"), LoadTypeFunc(ParserContext, this, "TypeDefined"));
 		NameSpace.AppendSyntax("typeof", LoadParseFunc(ParserContext, this, "ParseTypeOf"), TypeConst);
 		NameSpace.AppendSyntax("require", LoadParseFunc(ParserContext, this, "ParseRequire"), null);
 		NameSpace.AppendSyntax("import", LoadParseFunc(ParserContext, this, "ParseImport"), LoadTypeFunc(ParserContext, this, "TypeImport"));
 
 		NameSpace.AppendSyntax("if", LoadParseFunc(ParserContext, this, "ParseIf"), LoadTypeFunc(ParserContext, this, "TypeIf"));
 		NameSpace.AppendSyntax("while", LoadParseFunc(ParserContext, this, "ParseWhile"), LoadTypeFunc(ParserContext, this, "TypeWhile"));
 		NameSpace.AppendSyntax("do", LoadParseFunc(ParserContext, this, "ParseDoWhile"), LoadTypeFunc(ParserContext, this, "TypeDoWhile"));
 		NameSpace.AppendSyntax("for", LoadParseFunc(ParserContext, this, "ParseFor"), LoadTypeFunc(ParserContext, this, "TypeFor"));
 		NameSpace.AppendSyntax("continue", LoadParseFunc(ParserContext, this, "ParseContinue"), LoadTypeFunc(ParserContext, this, "TypeContinue"));
 		NameSpace.AppendSyntax("break", LoadParseFunc(ParserContext, this, "ParseBreak"), LoadTypeFunc(ParserContext, this, "TypeBreak"));
 		NameSpace.AppendSyntax("return", LoadParseFunc(ParserContext, this, "ParseReturn"), LoadTypeFunc(ParserContext, this, "TypeReturn"));
 		NameSpace.AppendSyntax("let const", LoadParseFunc(ParserContext, this, "ParseSymbolDecl"), null/*LoadTypeFunc(ParserContext, this, "TypeSymbolDecl")*/);
 
 		NameSpace.AppendSyntax("try", LoadParseFunc(ParserContext, this, "ParseTry"), LoadTypeFunc(ParserContext, this, "TypeTry"));
 		NameSpace.AppendSyntax("throw", LoadParseFunc(ParserContext, this, "ParseThrow"), LoadTypeFunc(ParserContext, this, "TypeThrow"));
 
 		NameSpace.AppendSyntax("class", LoadParseFunc(ParserContext, this, "ParseClassDecl2"), LoadTypeFunc(ParserContext, this, "TypeClassDecl2"));
 		NameSpace.AppendSyntax("constructor", LoadParseFunc(ParserContext, this, "ParseConstructor2"), LoadTypeFunc(ParserContext, this, "TypeFuncDecl"));
 		NameSpace.AppendSyntax("super", LoadParseFunc(ParserContext, this, "ParseSuper"), null);
 		NameSpace.AppendSyntax("this", LoadParseFunc(ParserContext, this, "ParseThis"), LoadTypeFunc(ParserContext, this, "TypeThis"));
 		NameSpace.AppendSyntax("new", LoadParseFunc(ParserContext, this, "ParseNew"), LoadTypeFunc(ParserContext, this, "TypeApply"));
 
 		NameSpace.AppendSyntax("enum", LoadParseFunc(ParserContext, this, "ParseEnum"), LoadTypeFunc(ParserContext, this, "TypeEnum"));
 		NameSpace.AppendSyntax("switch", LoadParseFunc(ParserContext, this, "ParseSwitch"), LoadTypeFunc(ParserContext, this, "TypeSwitch"));
 		NameSpace.AppendSyntax("$CaseBlock$", LoadParseFunc(ParserContext, this, "ParseCaseBlock"), null);
 
 		// expermental
 		NameSpace.AppendSyntax("__line__", LoadParseFunc(ParserContext, this, "ParseLine"), LoadTypeFunc(ParserContext, this, "TypeLine"));
 	}
 }
 
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
 
 final class GtParserContext extends GreenTeaUtils {
 	/*field*/public final  GtGenerator   Generator;
 	/*field*/public final  GtNameSpace		   RootNameSpace;
 	/*field*/public GtNameSpace		           TopLevelNameSpace;
 
 	// basic class
 	/*field*/public final GtType		VoidType;
 	/*field*/public final GtType		BooleanType;
 	/*field*/public final GtType		IntType;
 	/*field*/public final GtType        FloatType;
 	/*field*/public final GtType		StringType;
 	/*field*/public final GtType		AnyType;
 	/*field*/public final GtType		ArrayType;
 	/*field*/public final GtType		FuncType;
 
 	/*field*/public final GtType		TopType;
 	/*field*/public final GtType		TenumType;
 	/*field*/public final GtType		StructType;
 	/*field*/public final GtType		VarType;
 
 	/*field*/public final GtType		TypeType;
 
 	/*field*/public final  GtMap               SourceMap;
 	/*field*/public final  ArrayList<String>   SourceList;
 	/*field*/public final  GtMap			   ClassNameMap;
 
 	/*field*/public int ClassCount;
 	/*field*/public int FuncCount;
 	/*field*/public final GtStat Stat;
 	/*field*/public ArrayList<String>    ReportedErrorList;
 	/*filed*/private boolean NoErrorReport;
 
 	GtParserContext/*constructor*/(GtGrammar Grammar, GtGenerator Generator) {
 		this.Generator    = Generator;
 		this.Generator.Context = this;
 		this.SourceMap     = new GtMap();
 		this.SourceList    = new ArrayList<String>();
 		this.ClassNameMap  = new GtMap();
 		this.RootNameSpace = new GtNameSpace(this, null);
 		this.ClassCount = 0;
 		this.FuncCount = 0;
 		this.Stat = new GtStat();
 		this.NoErrorReport = false;
 		this.ReportedErrorList = new ArrayList<String>();
 
 		this.TopType     = new GtType(this, 0, "top", null, null);               //  unregistered
 		this.StructType  = this.TopType.CreateSubType(0, "record", null, null);  //  unregistered
 		this.TenumType   = this.TopType.CreateSubType(EnumType, "enum", null, null);    //  unregistered
 
 		this.VoidType    = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType, "void", null, Void.class), null);
 		this.BooleanType = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType, "boolean", false, Boolean.class), null);
 		this.IntType     = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType, "int", 0L, Long.class), null);
 		this.FloatType   = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType, "double", 0.0, Double.class), null);
 		this.StringType  = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType, "String", null, String.class), null);
 		this.VarType     = this.RootNameSpace.AppendTypeName(new GtType(this, 0, "var", null, null), null);
 		this.AnyType     = this.RootNameSpace.AppendTypeName(new GtType(this, DynamicType, "any", null, null), null);
 		this.TypeType    = this.RootNameSpace.AppendTypeName(this.TopType.CreateSubType(0, "Type", null, null), null);
 		this.ArrayType   = this.RootNameSpace.AppendTypeName(this.TopType.CreateSubType(0, "Array", null, null), null);
 		this.FuncType    = this.RootNameSpace.AppendTypeName(this.TopType.CreateSubType(0, "Func", null, null), null);
 
 		this.ArrayType.TypeParams = new GtType[1];
 		this.ArrayType.TypeParams[0] = this.AnyType;
 		this.FuncType.TypeParams = new GtType[1];
 		this.FuncType.TypeParams[0] = this.VarType;  // for PolyFunc
 
 //ifdef JAVA
 		this.SetNativeTypeName("void",    this.VoidType);
 		this.SetNativeTypeName("java.lang.Object",  this.AnyType);
 		this.SetNativeTypeName("boolean", this.BooleanType);
 		this.SetNativeTypeName("java.lang.Boolean", this.BooleanType);
 		this.SetNativeTypeName("long",    this.IntType);
 		this.SetNativeTypeName("java.lang.Long",    this.IntType);
 		this.SetNativeTypeName("java.lang.String",  this.StringType);
 		this.SetNativeTypeName("org.GreenTeaScript.GtType", this.TypeType);
 		this.SetNativeTypeName("org.GreenTeaScript.GreenTeaEnum", this.TenumType);
 		this.SetNativeTypeName("java.util.Array", this.ArrayType);
 		this.SetNativeTypeName("double",    this.FloatType);
 		this.SetNativeTypeName("java.lang.Double",  this.FloatType);
 //endif VAJA
 		Grammar.LoadTo(this.RootNameSpace);
 		this.TopLevelNameSpace = new GtNameSpace(this, this.RootNameSpace);
 		this.Generator.InitContext(this);
 	}
 
 	public void LoadGrammar(GtGrammar Grammar) {
 		Grammar.LoadTo(this.TopLevelNameSpace);
 	}
 
 	public final GtType GuessType (Object Value) {
 		if(Value instanceof GtFunc) {
 			return ((/*cast*/GtFunc)Value).GetFuncType();
 		}
 		else if(Value instanceof GtPolyFunc) {
 			return this.FuncType;
 		}
 		else if(Value instanceof GreenTeaTopObject) {
 			// FIXME In typescript, we cannot use GreenTeaObject
 			return ((/*cast*/GreenTeaObject)Value).GetGreenType();
 		}
 		else {
 			return this.Generator.GetNativeType(Value);
 		}
 	}
 
 	private final String SubtypeKey(GtType FromType, GtType ToType) {
 		return FromType.GetUniqueName() + "<" + ToType.GetUniqueName();
 	}
 
 	public final boolean CheckSubType(GtType SubType, GtType SuperType) {
 		// TODO: Structual Typing database
 		return false;
 	}
 
 	public void SetNativeTypeName(String Name, GtType Type) {
 		this.ClassNameMap.put(Name, Type);
 		LibGreenTea.VerboseLog(VerboseSymbol, "global type name: " + Name + ", " + Type);
 	}
 
 	public GtType GetGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList, boolean IsCreation) {
 		LibGreenTea.Assert(BaseType.IsGenericType());
 		/*local*/String MangleName = GreenTeaUtils.MangleGenericType(BaseType, BaseIdx, TypeList);
 		/*local*/GtType GenericType = (/*cast*/GtType)this.ClassNameMap.get(MangleName);
 		if(GenericType == null && IsCreation) {
 			/*local*/int i = BaseIdx;
 			/*local*/String s = BaseType.ShortClassName + "<";
 			while(i < LibGreenTea.ListSize(TypeList)) {
 				s = s + TypeList.get(i).ShortClassName;
 				i += 1;
 				if(i == LibGreenTea.ListSize(TypeList)) {
 					s = s + ">";
 				}
 				else {
 					s = s + ",";
 				}
 			}
 			GenericType = BaseType.CreateGenericType(BaseIdx, TypeList, s);
 			this.SetNativeTypeName(MangleName, GenericType);
 		}
 		return GenericType;
 	}
 
 	public GtType GetGenericType1(GtType BaseType, GtType ParamType, boolean IsCreation) {
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		TypeList.add(ParamType);
 		return this.GetGenericType(BaseType, 0, TypeList, IsCreation);
 	}
 
 	public final long GetFileLine(String FileName, int Line) {
 		/*local*/Integer Id = /* (FileName == null) ? 0 :*/ (/*cast*/Integer)this.SourceMap.get(FileName);
 		if(Id == null) {
 			this.SourceList.add(FileName);
 			Id = this.SourceList.size();
 			this.SourceMap.put(FileName, Id);
 		}
 		return LibGreenTea.JoinIntId(Id, Line);
 	}
 
 	public final String GetSourceFileName(long FileLine) {
 		/*local*/int FileId = LibGreenTea.UpperId(FileLine);
 		return (FileId == 0) ? null : this.SourceList.get(FileId - 1);
 	}
 
 	final String GetSourcePosition(long FileLine) {
 		/*local*/int FileId = LibGreenTea.UpperId(FileLine);
 		/*local*/int Line = LibGreenTea.LowerId(FileLine);
 		/*local*/String FileName = (FileId == 0) ? "eval" : this.SourceList.get(FileId - 1);
 		return "(" + FileName + ":" + Line + ")";
 	}
 
 	public void SetNoErrorReport(boolean b) {
 		this.NoErrorReport = b;
 	}
 
 	public final void ReportError(int Level, GtToken Token, String Message) {
 		if(!Token.IsError() || !this.NoErrorReport) {
 			if(Level == ErrorLevel) {
 				Message = "(error) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 				Token.SetErrorMessage(Message, this.RootNameSpace.GetSyntaxPattern("$Error$"));
 			}
 			else if(Level == TypeErrorLevel) {
 				Message = "(error) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			}
 			else if(Level == WarningLevel) {
 				Message = "(warning) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			}
 			else if(Level == InfoLevel) {
 				Message = "(info) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			}
 			//LibGreenTea.DebugP(Message);
 			//System.err.println("**" + Message + "    if dislike this, comment out In ReportError");
 			this.ReportedErrorList.add(Message);
 		}
 	}
 
 	public final String[] GetReportedErrors() {
 		/*local*/ArrayList<String> List = this.ReportedErrorList;
 		this.ReportedErrorList = new ArrayList<String>();
 		return LibGreenTea.CompactStringList(List);
 	}
 
 	public final void ShowReportedErrors() {
 		/*local*/int i = 0;
 		/*local*/String[] Messages = this.GetReportedErrors();
 		while(i < Messages.length) {
 			LibGreenTea.println(Messages[i]);
 			i = i + 1;
 		}
 	}
 }
 
 public class GreenTeaScript extends GreenTeaUtils {
 	public final static void ExecCommand(String[] Args) {
 		/*local*/String TargetCode = "exe";  // self executable
 		/*local*/int GeneratorFlag = 0;
 		/*local*/String OneLiner = null;
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
 		/*local*/GtParserContext Context = new GtParserContext(new GreenTeaGrammar(), Generator);
 		Context.LoadGrammar(new DShellGrammar());
 		if(OneLiner != null) {
 			Context.TopLevelNameSpace.Eval(OneLiner, 1);
 		}
 		if(!(Index < Args.length)) {
 			ShellMode = true;
 		}
 		while(Index < Args.length) {
 			/*local*/String ScriptText = LibGreenTea.LoadFile2(Args[Index]);
 			if(ScriptText == null) {
 				LibGreenTea.Exit(1, "file not found: " + Args[Index]);
 			}
 			/*local*/long FileLine = Context.GetFileLine(Args[Index], 1);
 			Context.TopLevelNameSpace.Eval(ScriptText, FileLine);
 			Index += 1;
 		}
 		if(ShellMode) {
 			LibGreenTea.println(GreenTeaUtils.ProgName + GreenTeaUtils.Version + " (" + GreenTeaUtils.CodeName + ") on " + LibGreenTea.GetPlatform());
 			LibGreenTea.println(GreenTeaUtils.Copyright);
 			Context.ShowReportedErrors();
 			/*local*/int linenum = 1;
 			/*local*/String Line = null;
 			while((Line = LibGreenTea.ReadLine(">>> ", "    ")) != null) {
 				try {
 					/*local*/Object EvaledValue = Context.TopLevelNameSpace.Eval(Line, linenum);
 					Context.ShowReportedErrors();
 					if(EvaledValue != null) {
 						if(EvaledValue instanceof String) {
 							EvaledValue = LibGreenTea.QuoteString((/*cast*/String)EvaledValue);
 						}
 						LibGreenTea.println(" (" + Context.GuessType(EvaledValue) + ":" + LibGreenTea.GetClassName(EvaledValue) + ") " + EvaledValue);
 					}
 					linenum += 1;
 				}
 				catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 			LibGreenTea.println("");
 		}
 		else {
 			Generator.FlushBuffer();
 		}
 	}
 
 	public final static void main(String[] Args)  {
 		GreenTeaScript.ExecCommand(Args);
 	}
 }
