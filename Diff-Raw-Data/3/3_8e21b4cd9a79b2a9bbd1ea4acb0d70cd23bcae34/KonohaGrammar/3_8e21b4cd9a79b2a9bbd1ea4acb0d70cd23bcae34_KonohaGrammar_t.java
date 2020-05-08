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
 import java.util.ArrayList;
 //endif VAJA
 
 public class KonohaGrammar extends GtGrammar {
 
 	private static final boolean HasAnnotation(GtMap Annotation, String Key) {
 		if(Annotation != null) {
 			/*local*/Object Value = Annotation.GetOrNull(Key);
 			if(Value instanceof Boolean) {
 				Annotation.put(Key, false);  // consumed;
 			}
 			return (Value != null);
 		}
 		return false;
 	}
 
 	public static int ParseNameSpaceFlag(int Flag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(KonohaGrammar.HasAnnotation(Annotation, "RootNameSpace")) {
 				Flag = Flag | RootNameSpace;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Public")) {
 				Flag = Flag | PublicNameSpace;
 			}
 		}
 		return Flag;
 	}
 
 	public static int ParseClassFlag(int Flag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(KonohaGrammar.HasAnnotation(Annotation, "Export")) {
 				Flag = Flag | ExportFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Public")) {
 				Flag = Flag | PublicFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Virtual")) {
 				Flag = Flag | VirtualFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Deprecated")) {
 				Flag = Flag | DeprecatedFunc;
 			}
 		}
 		return Flag;
 	}
 
 	public static int ParseFuncFlag(int Flag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(KonohaGrammar.HasAnnotation(Annotation, "Export")) {
 				Flag = Flag | ExportFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Public")) {
 				Flag = Flag | PublicFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Hidden")) {
 				Flag = Flag | HiddenFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Const")) {
 				Flag = Flag | ConstFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Common")) {
 				Flag = Flag | CommonFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Operator")) {
 				Flag = Flag | OperatorFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Method")) {
 				Flag = Flag | MethodFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Coercion")) {
 				Flag = Flag | CoercionFunc;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "StrongCoercion")) {
 				Flag = Flag | CoercionFunc | StrongCoercionFunc ;
 			}
 			if(KonohaGrammar.HasAnnotation(Annotation, "Deprecated")) {
 				Flag = Flag | DeprecatedFunc;
 			}
 		}
 		return Flag;
 	}
 
 	public static int ParseVarFlag(int Flag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(KonohaGrammar.HasAnnotation(Annotation, "ReadOnly")) {
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
			if(LibGreenTea.CharAt(SourceText, pos) == '\n') {
				TokenContext.FoundLineFeed(1);
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
 		if(NextChar == '*') { // MultiLineComment
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
 			/*local*/int Level = 1;
 			/*local*/char PrevChar = 0;
 			while(NextPos < SourceText.length()) {
 				NextChar = LibGreenTea.CharAt(SourceText, NextPos);
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
 		}
 		else if(NextChar == '/') { // SingleLineComment
 			while(NextPos < SourceText.length()) {
 				NextChar = LibGreenTea.CharAt(SourceText, NextPos);
 				if(NextChar == '\n') {
 					return KonohaGrammar.IndentToken(TokenContext, SourceText, NextPos);
 				}
 				NextPos = NextPos + 1;
 			}
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
 				TokenContext.ReportTokenError1(ErrorLevel, "expected ' to close the charctor literal", LibGreenTea.SubString(SourceText, start, pos));
 				TokenContext.FoundLineFeed(1);
 				return pos;
 			}
 			pos = pos + 1;
 			prev = ch;
 		}
 		TokenContext.ReportTokenError1(ErrorLevel, "expected ' to close the charctor literal", LibGreenTea.SubString(SourceText, start, pos));
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
 				TokenContext.ReportTokenError1(ErrorLevel, "expected \" to close the string literal", LibGreenTea.SubString(SourceText, start, pos));
 				TokenContext.FoundLineFeed(1);
 				return pos;
 			}
 			pos = pos + 1;
 			prev = ch;
 		}
 		TokenContext.ReportTokenError1(ErrorLevel, "expected \" to close the string literal", LibGreenTea.SubString(SourceText, start, pos));
 		return pos;
 	}
 
 //	public static long StringLiteralToken_StringInterpolation(GtTokenContext TokenContext, String SourceText, long pos) {
 //		/*local*/long start = pos + 1;
 //		/*local*/long NextPos = start;
 //		/*local*/char prev = '"';
 //		while(NextPos < SourceText.length()) {
 //			/*local*/char ch = LibGreenTea.CharAt(SourceText, NextPos);
 //			if(ch == '$') {
 //				/*local*/long end = NextPos + 1;
 //				/*local*/char nextch = LibGreenTea.CharAt(SourceText, end);
 //				if(nextch == '{') {
 //					while(end < SourceText.length()) {
 //						ch = LibGreenTea.CharAt(SourceText, end);
 //						if(ch == '}') {
 //							break;
 //						}
 //						end = end + 1;
 //					}
 //					/*local*/String Expr = LibGreenTea.SubString(SourceText, (NextPos + 2), end);
 //					/*local*/GtTokenContext LocalContext = new GtTokenContext(TokenContext.TopLevelNameSpace, Expr, TokenContext.ParsingLine);
 //					LocalContext.SkipEmptyStatement();
 //
 //					TokenContext.AddNewToken("\"" + LibGreenTea.SubString(SourceText, start, NextPos) + "\"", QuotedTokenFlag, "$StringLiteral$");
 //					TokenContext.AddNewToken("+", 0, null);
 //					while(LocalContext.HasNext()) {
 //						/*local*/GtToken NewToken = LocalContext.Next();
 //						TokenContext.AddNewToken(NewToken.ParsedText, 0, null);
 //					}
 //					TokenContext.AddNewToken("+", 0, null);
 //					end = end + 1;
 //					start = end;
 //					NextPos = end;
 //					prev = ch;
 //					if(ch == '"') {
 //						TokenContext.AddNewToken("\"" + LibGreenTea.SubString(SourceText, start, NextPos) + "\"", QuotedTokenFlag, "$StringLiteral$");
 //						return NextPos + 1;
 //					}
 //					continue;
 //				}
 //			}
 //			if(ch == '"' && prev != '\\') {
 //				TokenContext.AddNewToken("\"" + LibGreenTea.SubString(SourceText, start, NextPos) + "\"", QuotedTokenFlag, "$StringLiteral$");
 //				return NextPos + 1;
 //			}
 //			if(ch == '\n') {
 //				TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", LibGreenTea.SubString(SourceText, start, NextPos));
 //				TokenContext.FoundLineFeed(1);
 //				return NextPos;
 //			}
 //			NextPos = NextPos + 1;
 //			prev = ch;
 //		}
 //		TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", LibGreenTea.SubString(SourceText, start, NextPos));
 //		return NextPos;
 //	}
 
 	public static GtSyntaxTree ParseTypeOf(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree TypeOfTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "typeof");
 		TypeOfTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		TypeOfTree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$Expression$", Required);
 		TypeOfTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		if(!TypeOfTree.IsMismatchedOrError()) {
 			/*local*/GtTypeEnv Gamma = new GtTypeEnv(NameSpace);
 			/*local*/GtNode ObjectNode = TypeOfTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 			if(ObjectNode.IsErrorNode()) {
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
 
 	// parser and type checker
 	public static GtSyntaxTree ParseType(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		if(TokenContext.MatchToken("typeof")) {
 			return KonohaGrammar.ParseTypeOf(NameSpace, TokenContext, LeftTree, Pattern);
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
 		return TokenContext.ReportExpectedMessage(Token, "name", true);
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
 		/*local*/int VarFlag = KonohaGrammar.ParseVarFlag(0, ParsedTree.Annotation);
 		/*local*/GtType DeclType = ParsedTree.GetSyntaxTreeAt(VarDeclType).GetParsedType();
 		/*local*/String VariableName = ParsedTree.GetSyntaxTreeAt(VarDeclName).KeyToken.ParsedText;
 		/*local*/GtNode InitValueNode = null;
 		if(ParsedTree.HasNodeAt(VarDeclValue)) {
 			InitValueNode = ParsedTree.TypeCheckAt(VarDeclValue, Gamma, DeclType, DefaultTypeCheckPolicy);
 			if(InitValueNode.IsErrorNode()) {
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
 		return new GtSyntaxTree(Pattern, NameSpace, Token, LibGreenTea.UnquoteString(Token.ParsedText));
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
 		return KonohaGrammar.TypeConst(Gamma, ParsedTree, ContextType);
 	}
 
 	public static GtSyntaxTree ParseExpression(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		//return GreenTeaUtils.ParseExpression(NameSpace, TokenContext, false/*SuffixOnly*/);
 		Pattern = TokenContext.GetFirstPattern(NameSpace);
 		LeftTree = GreenTeaUtils.ApplySyntaxPattern(NameSpace, TokenContext, LeftTree, Pattern);
 		while(!GreenTeaUtils.IsMismatchedOrError(LeftTree)) {
 			/*local*/GtSyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern(NameSpace);
 			if(ExtendedPattern == null) {
 				break;
 			}
 			LeftTree = GreenTeaUtils.ApplySyntaxPattern(NameSpace, TokenContext, LeftTree, ExtendedPattern);
 		}
 		return LeftTree;
 	}
 
 	public static GtSyntaxTree ParseSuffixExpression(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		Pattern = TokenContext.GetFirstPattern(NameSpace);
 		LeftTree = GreenTeaUtils.ApplySyntaxPattern(NameSpace, TokenContext, LeftTree, Pattern);
 		while(!GreenTeaUtils.IsMismatchedOrError(LeftTree)) {
 			/*local*/GtSyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern(NameSpace);
 			if(ExtendedPattern == null || ExtendedPattern.IsBinaryOperator()) {
 				break;
 			}
 			LeftTree = GreenTeaUtils.ApplySyntaxPattern(NameSpace, TokenContext, LeftTree, ExtendedPattern);
 		}
 		return LeftTree;
 	}
 
 	public static GtSyntaxTree ParseUnary(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		Tree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$SuffixExpression$", Required);
 		return Tree;
 	}
 
 	public static GtNode TypeUnary(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode ExprNode  = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ExprNode.IsErrorNode()) {
 			return ExprNode;
 		}
 		/*local*/GtType BaseType = ExprNode.Type;
 		/*local*/GtType ReturnType = Gamma.AnyType;
 		/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(BaseType, FuncSymbol(OperatorSymbol), true);
 		/*local*/GtFunc ResolvedFunc = PolyFunc.ResolveUnaryMethod(Gamma, BaseType);
 		if(ResolvedFunc == null) {
 			Gamma.Context.ReportError(TypeErrorLevel, ParsedTree.KeyToken, "mismatched operators: " + PolyFunc);
 		}
 		else {
 			Gamma.CheckFunc("operator", ResolvedFunc, ParsedTree.KeyToken);
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
 			RightTree.SetSyntaxTreeAt(LeftHandTerm, KonohaGrammar.RightJoin(NameSpace, LeftTree, Pattern, OperatorToken, RightLeft));
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
 		/*local*/GtSyntaxTree RightTree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 		if(GreenTeaUtils.IsMismatchedOrError(RightTree)) {
 			return RightTree;
 		}
 		//System.err.println("left=" + Pattern.SyntaxFlag + ", right=" + RightTree.Pattern.SyntaxFlag + ", binary?" +  RightTree.Pattern.IsBinaryOperator() + RightTree.Pattern);
 		if(RightTree.Pattern.IsBinaryOperator() && Pattern.IsRightJoin(RightTree.Pattern)) {
 			return KonohaGrammar.RightJoin(NameSpace, LeftTree, Pattern, OperatorToken, RightTree);
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
 		if(!LeftNode.IsErrorNode()) {
 			/*local*/GtType BaseType = LeftNode.Type;
 			/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 			/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(BaseType, FuncSymbol(OperatorSymbol), true);
 			/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 			ParamList.add(LeftNode);
 			/*local*/GtResolvedFunc ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 			if(ResolvedFunc.Func == null) {
 				Gamma.Context.ReportError(TypeErrorLevel, ParsedTree.KeyToken, "mismatched operators: " + PolyFunc);
 			}
 			else {
 				Gamma.CheckFunc("operator", ResolvedFunc.Func, ParsedTree.KeyToken);
 			}
 			/*local*/GtNode BinaryNode =  Gamma.Generator.CreateBinaryNode(ResolvedFunc.ReturnType, ParsedTree, ResolvedFunc.Func, LeftNode, ParamList.get(1));
 			if(ResolvedFunc.Func == null && !BaseType.IsDynamic()) {
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
 		if(ThenNode.IsErrorNode()) {
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
 		CastTree.SetMatchedPatternAt(RightHandTerm, NameSpace, TokenContext, "$SuffixExpression$", Required);
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
 		if(!Token.IsNameSymbol()) {
 			return TokenContext.ReportExpectedMessage(Token, "field name", true);		
 		}
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		NewTree.AppendParsedTree2(LeftTree);
 		if(TokenContext.MatchToken("=")) {
 			NewTree.Pattern = NameSpace.GetSyntaxPattern("$Setter$");
 			NewTree.SetMatchedPatternAt(RightHandTerm, NameSpace, TokenContext, "$Expression$", Required);
 		}
 		return NewTree;
 	}
 
 	public static GtNode TypeGetter(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode ObjectNode = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ObjectNode.IsErrorNode()) {
 			return ObjectNode;
 		}
 		// 1. To start, check class const such as Math.Pi if base is a type value
 		/*local*/String TypeName = ObjectNode.Type.ShortName;
 		if(ObjectNode instanceof GtConstNode && ObjectNode.Type.IsTypeType()) {
 			/*local*/GtType ObjectType = (/*cast*/GtType)((/*cast*/GtConstNode)ObjectNode).ConstValue;
 			/*local*/Object ConstValue = ParsedTree.NameSpace.GetClassStaticSymbol(ObjectType, Name, true);
 //			if(ConstValue instanceof GreenTeaEnum) {
 //				if(ContextType.IsStringType()) {
 //					ConstValue = ((/*cast*/GreenTeaEnum)ConstValue).EnumSymbol;
 //				}
 //				else {
 //					ConstValue = ((/*cast*/GreenTeaEnum)ConstValue).EnumValue;
 //				}
 //			}
 			if(ConstValue != null) {
 				return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ConstValue), ParsedTree, ConstValue);
 			}
 			TypeName = ObjectType.ShortName;
 		}
 		// 2. find Class method
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(ObjectNode.Type, Name, true);
 		if(PolyFunc.FuncList.size() > 0 && ContextType == Gamma.FuncType) {
 			/*local*/GtFunc FirstFunc = PolyFunc.FuncList.get(0);
 			return Gamma.Generator.CreateGetterNode(ContextType, ParsedTree, FirstFunc, ObjectNode);
 		}
 		// 3. find object field
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
 
 	public static GtNode TypeSetter(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode ObjectNode = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ObjectNode.IsErrorNode()) {
 			return ObjectNode;
 		}
 		/*local*/GtFunc SetterFunc = ParsedTree.NameSpace.GetSetterFunc(ObjectNode.Type, Name, true);
 		if(SetterFunc != null) {
 			/*local*/GtType ValueType = SetterFunc.GetFuncParamType(1);
 			/*local*/GtNode ValueNode = ParsedTree.TypeCheckAt(RightHandTerm, Gamma, ValueType, DefaultTypeCheckPolicy);
 			return Gamma.Generator.CreateSetterNode(Gamma.VoidType, ParsedTree, SetterFunc, ObjectNode, ValueNode);
 		}
 		else if(ObjectNode.Type.IsDynamic()) {
 			/*local*/GtNode ValueNode = ParsedTree.TypeCheckAt(RightHandTerm, Gamma, Gamma.VoidType, DefaultTypeCheckPolicy);
 			return Gamma.Generator.CreateSetterNode(Gamma.VoidType, ParsedTree, SetterFunc, ObjectNode, ValueNode);			
 		}
 		else {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "undefined name: " + Name + " of " + ObjectNode.Type);
 		}
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
 		return Gamma.Generator.CreateConstNode(Gamma.BooleanType, ParsedTree, (ObjectNode instanceof GtConstNode));
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
 
 	public static GtNode TypeNewNode(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtToken ClassToken, GtType ClassType, GtType ContextType) {
 		if(ClassType.IsVarType()) {  /* constructor */
 			ClassType = ContextType;
 			if(ClassType.IsVarType()) {
 				return Gamma.CreateSyntaxErrorNode(ParsedTree, "ambigious constructor: " + ClassToken);
 			}
 			Gamma.ReportTypeInference(ClassToken, "constructor", ClassType);
 		}
 		if(ClassType.IsAbstract()) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "type is abstract");
 		}
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetConstructorFunc(/*GtFunc*/ClassType);
 		/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 		if(ClassType.IsNative()) {
 			/*local*/GtResolvedFunc ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 			if(ResolvedFunc.ErrorNode != null) {
 				return ResolvedFunc.ErrorNode;
 			}
 			if(ResolvedFunc.Func != null && ResolvedFunc.Func.Is(NativeFunc)) {
 				Gamma.CheckFunc("constructor", ResolvedFunc.Func, ParsedTree.KeyToken);
 				return Gamma.Generator.CreateConstructorNode(ClassType, ParsedTree, ResolvedFunc.Func, ParamList);
 			}
 		}
 		else {
 			/*local*/GtNode NewNode = Gamma.Generator.CreateNewNode(ClassType, ParsedTree);
 			ParamList.add(NewNode);
 			/*local*/GtResolvedFunc ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 			if(ResolvedFunc.ErrorNode != null) {
 				return ResolvedFunc.ErrorNode;
 			}
 			if(ResolvedFunc.Func == null) {
 				if(ParsedTree.SubTreeList.size() == 1) {
 					return NewNode;
 				}
 			}
 			else {
 				Gamma.CheckFunc("constructor", ResolvedFunc.Func, ParsedTree.KeyToken);
 				/*local*/GtNode Node = Gamma.Generator.CreateApplyNode(ResolvedFunc.ReturnType, ParsedTree, ResolvedFunc.Func);
 				Node.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, ResolvedFunc.Func));
 				Node.AppendNodeList(0, ParamList);
 				return Node;
 			}
 		}
 		return PolyFunc.ReportTypeError(Gamma, ParsedTree, ClassType, "constructor");
 	}
 	
 	public static GtNode TypeMethodCall(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtNode RecvNode, String MethodName) {
 		if(!RecvNode.IsErrorNode()) {
 			/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(RecvNode.Type, FuncSymbol(MethodName), true);
 			//System.err.println("polyfunc: " + PolyFunc);
 			/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 			ParamList.add(RecvNode);
 			/*local*/GtResolvedFunc ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 			if(ResolvedFunc.ErrorNode != null) {
 				return ResolvedFunc.ErrorNode;
 			}
 			if(ResolvedFunc.Func == null) {
 				if(LibGreenTea.EqualsString(MethodName, "()")) {
 					return Gamma.CreateSyntaxErrorNode(ParsedTree, RecvNode.Type + " is not applicapable");
 				}
 				else {
 					return PolyFunc.ReportTypeError(Gamma, ParsedTree, RecvNode.Type, MethodName);
 				}
 			}
 			Gamma.CheckFunc("method", ResolvedFunc.Func, ParsedTree.KeyToken);
 			/*local*/GtNode Node = Gamma.Generator.CreateApplyNode(ResolvedFunc.ReturnType, ParsedTree, ResolvedFunc.Func);
 			Node.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, ResolvedFunc.Func));
 			Node.AppendNodeList(0, ParamList);
 			return Node;
 		}
 		return RecvNode;
 	}
 
 	public static GtNode TypePolyFunc(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtConstNode FuncNode, GtPolyFunc PolyFunc) {
 		/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 		/*local*/GtResolvedFunc ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 		if(ResolvedFunc.ErrorNode != null) {
 			return ResolvedFunc.ErrorNode;
 		}
 		if(ResolvedFunc.Func != null) {
 			// reset ConstValue as if non-polymorphic function were found
 			FuncNode.ConstValue = ResolvedFunc.Func;
 			FuncNode.Type = ResolvedFunc.Func.GetFuncType();
 		}
 		Gamma.CheckFunc("function", ResolvedFunc.Func, ParsedTree.KeyToken);
 		/*local*/GtNode Node = Gamma.Generator.CreateApplyNode(ResolvedFunc.ReturnType, ParsedTree, ResolvedFunc.Func);
 		Node.Append(FuncNode);
 		Node.AppendNodeList(0, ParamList);
 		return Node;
 	}
 	
 	public static GtNode TypeApply(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode FuncNode = ParsedTree.TypeCheckAt(0, Gamma, Gamma.FuncType, NoCheckPolicy);
 		if(FuncNode.IsErrorNode()) {
 			return FuncNode;
 		}
 		if(FuncNode instanceof GtGetterNode) { /* Func style .. o.f x, y, .. */
 			/*local*/String FuncName = FuncNode.Token.ParsedText;
 			/*local*/GtNode BaseNode = ((/*cast*/GtGetterNode)FuncNode).ExprNode;
 			return KonohaGrammar.TypeMethodCall(Gamma, ParsedTree, BaseNode, FuncName);
 		}
 		if(FuncNode instanceof GtConstNode) { /* static */
 			/*local*/Object Func = ((/*cast*/GtConstNode)FuncNode).ConstValue;
 			if(Func instanceof GtType) {  // constructor;
 				return KonohaGrammar.TypeNewNode(Gamma, ParsedTree, FuncNode.Token, (/*cast*/GtType)Func, ContextType);
 			}
 			else if(Func instanceof GtFunc) {
 				return KonohaGrammar.TypePolyFunc(Gamma, ParsedTree, ((/*cast*/GtConstNode)FuncNode), new GtPolyFunc(null).Append((/*cast*/GtFunc)Func, null));
 			}
 			else if(Func instanceof GtPolyFunc) {
 				return KonohaGrammar.TypePolyFunc(Gamma, ParsedTree, ((/*cast*/GtConstNode)FuncNode), (/*cast*/GtPolyFunc)Func);
 			}
 		}
 //		/*local*/GtType ReturnType = Gamma.AnyType;
 		if(FuncNode.Type.IsFuncType()) {
 //			/*local*/GtType FuncType = FuncNode.Type;
 //			LibGreenTea.Assert(LibGreenTea.ListSize(NodeList) + LibGreenTea.ListSize(ParsedTree.SubTreeList) - TreeIndex == FuncType.TypeParams.length);
 //			while(TreeIndex < LibGreenTea.ListSize(ParsedTree.SubTreeList)) {
 //				/*local*/GtNode Node = ParsedTree.TypeCheckAt(TreeIndex, Gamma, FuncType.TypeParams[TreeIndex], DefaultTypeCheckPolicy);
 //				if(Node.IsError()) {
 //					return Node;
 //				}
 //				GreenTeaUtils.AppendTypedNode(NodeList, Node);
 //				TreeIndex = TreeIndex + 1;
 //			}
 //			ReturnType = FuncType.TypeParams[0];			
 		}
 //		if(FuncNode.Type == Gamma.AnyType) {
 //			while(TreeIndex < LibGreenTea.ListSize(ParsedTree.SubTreeList)) {
 //				/*local*/GtNode Node = ParsedTree.TypeCheckAt(TreeIndex, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 //				if(Node.IsError()) {
 //					return Node;
 //				}
 //				GreenTeaUtils.AppendTypedNode(NodeList, Node);
 //				TreeIndex = TreeIndex + 1;
 //			}
 //		}
 //		else {
 		return KonohaGrammar.TypeMethodCall(Gamma, ParsedTree, FuncNode, "()");
 //		}
 	}
 
 	public static GtSyntaxTree ParseNot(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.Next(), null);
 		Tree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$SuffixExpression$", Required);
 		return Tree;
 	}
 
 	public static GtNode TypeNot(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode ExprNode  = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		if(ExprNode.IsErrorNode()) {
 			return ExprNode;
 		}
 		/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(ExprNode.Type, FuncSymbol(OperatorSymbol), true);
 		/*local*/GtFunc ResolvedFunc = PolyFunc.ResolveUnaryMethod(Gamma, Gamma.BooleanType);
 		LibGreenTea.Assert(ResolvedFunc != null);
 		return Gamma.Generator.CreateUnaryNode(Gamma.BooleanType, ParsedTree, ResolvedFunc, ExprNode);
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
 		if(LeftNode.IsErrorNode()) {
 			return LeftNode;
 		}
 		if(LeftNode instanceof GtLocalNode) {
 			/*local*/GtNode RightNode = ParsedTree.TypeCheckAt(RightHandTerm, Gamma, LeftNode.Type, DefaultTypeCheckPolicy);
 			return Gamma.Generator.CreateAssignNode(LeftNode.Type, ParsedTree, LeftNode, RightNode);
 		}
 		return Gamma.CreateSyntaxErrorNode(ParsedTree, "the left-hand side of an assignment must be variable");
 	}
 
 	public static GtNode TypeSelfAssign(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(LeftNode.IsErrorNode()) {
 			return LeftNode;
 		}
 		if(!(LeftNode instanceof GtLocalNode || LeftNode instanceof GtGetterNode || LeftNode instanceof GtIndexerNode)) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "the left-hand side of an assignment must be variable");
 		}
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckAt(RightHandTerm, Gamma, LeftNode.Type, DefaultTypeCheckPolicy);
 		/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 		OperatorSymbol = OperatorSymbol.substring(0, OperatorSymbol.length() - 1);
 		/*local*/GtFunc Func = null;
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(LeftNode.Type, FuncSymbol(OperatorSymbol), true);
 		/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 		ParamList.add(LeftNode);
 		/*local*/GtResolvedFunc ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 		if(ResolvedFunc.Func != null) {
 			LeftNode = ParamList.get(0);
 			RightNode = ParamList.get(1);
 		}
 		return Gamma.Generator.CreateSelfAssignNode(LeftNode.Type, ParsedTree, ResolvedFunc.Func, LeftNode, RightNode);
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
 			if(LeftNode instanceof GtLocalNode || LeftNode instanceof GtGetterNode || LeftNode instanceof GtIndexerNode) {
 				/*local*/GtNode ConstNode = Gamma.Generator.CreateConstNode(LeftNode.Type, ParsedTree, 1L);
 				// ++ => +
 				/*local*/String OperatorSymbol = LibGreenTea.SubString(ParsedTree.KeyToken.ParsedText, 0, 1);
 				/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(LeftNode.Type, FuncSymbol(OperatorSymbol), true);
 				/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 				ParamList.add(LeftNode);
 				ParamList.add(ConstNode);
 				/*local*/GtResolvedFunc ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 				return Gamma.Generator.CreateSelfAssignNode(LeftNode.Type, ParsedTree, ResolvedFunc.Func, LeftNode, ConstNode);
 			}
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "neither incremental nor decrimental");
 		}
 		return LeftNode.IsErrorNode() ? LeftNode : KonohaGrammar.TypeUnary(Gamma, ParsedTree, Type);
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
 			return TokenContext.ReportTokenError2(TokenContext.GetToken(), "unexpected ;", false);
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
 		return KonohaGrammar.ParseEmpty(NameSpace, TokenContext, LeftTree, Pattern);
 	}
 
 	private static String ParseJoinedName(GtTokenContext TokenContext) {
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
 		return PackageName;
 	}
 
 	public static GtSyntaxTree ParseImport(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ImportTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "import");
 		/*local*/String PackageName = KonohaGrammar.ParseJoinedName(TokenContext);
 		ImportTree.ParsedValue = PackageName;
 		return ImportTree;
 	}
 
 	public static GtNode TypeImport(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		/*local*/Object Value = LibGreenTea.ImportNativeObject(Gamma.NameSpace, (/*cast*/String)ParsedTree.ParsedValue);
 		if(Value == null) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "cannot import: " + ParsedTree.ParsedValue);
 		}
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(Value), ParsedTree, Value);
 	}
 
 	public static GtSyntaxTree ParseBlock(GtNameSpace ParentNameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		if(TokenContext.MatchToken("{")) {
 			/*local*/GtSyntaxTree PrevTree = null;
 			/*local*/GtNameSpace NameSpace = ParentNameSpace.CreateSubNameSpace();
 			while(TokenContext.HasNext()) {
 				TokenContext.SkipEmptyStatement();
 				if(TokenContext.MatchToken("}")) {
 					break;
 				}
 				/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 				/*local*/GtSyntaxTree ParsedTree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
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
 		if(ThenNode.HasReturnNode() && ElseNode != null && ElseNode.HasReturnNode()) {
 			ParsedTree.NextTree = null;
 		}
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
 			if(InitNode instanceof GtVarNode) {
 				((/*cast*/GtVarNode)InitNode).BlockNode = ForNode;
 			}
 			else {
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
 		return TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "continue");
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
 		if(Gamma.IsTopLevel()) {
 			return Gamma.UnsupportedTopLevelError(ParsedTree);
 		}
 		/*local*/GtType ReturnType = Gamma.Func.GetReturnType();
 		if(ParsedTree.HasNodeAt(ReturnExpr)) {
 			/*local*/GtNode Expr = ParsedTree.TypeCheckAt(ReturnExpr, Gamma, ReturnType, DefaultTypeCheckPolicy);
 			if(ReturnType == Gamma.VarType && !Expr.IsErrorNode()) {
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
 		ParsedTree.NextTree = null;
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
 
 	public static GtSyntaxTree ParseSymbols(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "__").ToConstTree(NameSpace);
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
 			NewEnumType = NameSpace.Context.EnumBaseType.CreateSubType(EnumType, EnumTypeName, null, EnumMap);
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
 				if(EnumMap.GetOrNull(Token.ParsedText) != null) {
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
 			/*local*/GtNameSpace StoreNameSpace = NameSpace.GetNameSpace(KonohaGrammar.ParseNameSpaceFlag(0, TokenContext.ParsingAnnotation));
 			StoreNameSpace.AppendTypeName(NewEnumType, EnumTree.GetSyntaxTreeAt(EnumNameTreeIndex).KeyToken);
 			/*local*/int i = 0;
 			while(i < LibGreenTea.ListSize(NameList)) {
 				/*local*/String Key = NameList.get(i).ParsedText;
 				StoreNameSpace.SetSymbol(GreenTeaUtils.ClassStaticSymbol(NewEnumType, Key), EnumMap.GetOrNull(Key), NameList.get(i));
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
 		/*local*/GtNameSpace NameSpace = ParentNameSpace.CreateSubNameSpace();
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
 			/*local*/GtSyntaxTree CurrentTree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
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
 		/*local*/GtType ConstClass = null;
 		SymbolDeclTree.SetMatchedPatternAt(SymbolDeclNameIndex, NameSpace, TokenContext, "$Variable$", Required);
 		if(TokenContext.MatchToken(".")) {
 			/*local*/String ClassName = SymbolDeclTree.GetSyntaxTreeAt(SymbolDeclNameIndex).KeyToken.ParsedText;
 			ConstClass = NameSpace.GetType(ClassName);
 			if(ConstClass == null) {
 				return TokenContext.ReportExpectedMessage(SymbolDeclTree.GetSyntaxTreeAt(SymbolDeclNameIndex).KeyToken, "type name", true);
 			}
 			SymbolDeclTree.SetMatchedPatternAt(SymbolDeclNameIndex, NameSpace, TokenContext, "$Variable$", Required);			
 		}
 		SymbolDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "=", Required);
 		SymbolDeclTree.SetMatchedPatternAt(SymbolDeclValueIndex, NameSpace, TokenContext, "$Expression$", Required);
 		
 		if(SymbolDeclTree.IsValidSyntax()) {
 			/*local*/GtToken SourceToken = SymbolDeclTree.GetSyntaxTreeAt(SymbolDeclNameIndex).KeyToken;
 			/*local*/String ConstName = SourceToken.ParsedText;
 			if(ConstClass != null) {
 				ConstName = GreenTeaUtils.ClassStaticSymbol(ConstClass, ConstName);
 				SourceToken.AddTypeInfoToErrorMessage(ConstClass);
 			}
 			/*local*/Object ConstValue = null;
 			if(SymbolDeclTree.GetSyntaxTreeAt(SymbolDeclValueIndex).Pattern.EqualsName("$Const$")) {
 				ConstValue = SymbolDeclTree.GetSyntaxTreeAt(SymbolDeclValueIndex).ParsedValue;
 			}
 			if(ConstValue == null) {
 				/*local*/GtTypeEnv Gamma = new GtTypeEnv(NameSpace);
 				/*local*/GtNode Node = SymbolDeclTree.TypeCheckAt(SymbolDeclValueIndex, Gamma, Gamma.VarType, OnlyConstPolicy);
 				if(Node.IsErrorNode()) {
 					SymbolDeclTree.ToError(Node.Token);
 					return SymbolDeclTree;
 				}
 				ConstValue = Node.ToConstValue(true);
 			}
 			/*local*/int NameSpaceFlag = KonohaGrammar.ParseNameSpaceFlag(0, TokenContext.ParsingAnnotation);
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
 		if(Token.IsQuoted()) {
 			Name = LibGreenTea.UnquoteString(Name);
 			Token.ParsedText = Name;
 			return new GtSyntaxTree(Pattern, NameSpace, Token, Name);
 		}
 		if(Name.length() > 0 && LibGreenTea.CharAt(Name, 0) != '(' && LibGreenTea.CharAt(Name, 0) != '.') {
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
 				if(!LibGreenTea.ImportNativeMethod(NameSpace, FuncBlock.DefinedFunc, (/*cast*/String)ImportTree.ParsedValue)) {
 					NameSpace.Context.ReportError(WarningLevel, ImportTree.KeyToken, "unable to import: " + ImportTree.ParsedValue);
 				}
 			}
 		}
 		else {
 			/*local*/GtSyntaxTree BlockTree = TokenContext.ParsePattern(NameSpace, "$Block$", Optional);
 			if(GreenTeaUtils.IsValidSyntax(BlockTree)) {
 				FuncBlock.FuncBlock = BlockTree;
 				/*local*/GtSyntaxTree ReturnTree = new GtSyntaxTree(NameSpace.GetSyntaxPattern("return"), NameSpace, BlockTree.KeyToken, null);
 				GreenTeaUtils.LinkTree(GreenTeaUtils.TreeTail(BlockTree), ReturnTree);
 				FuncBlock.DefinedFunc.FuncBody = FuncBlock;
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
 		/*local*/int FuncFlag = KonohaGrammar.ParseFuncFlag(0, TokenContext.ParsingAnnotation);
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		LibGreenTea.Assert(LeftTree != null);
 		FuncDeclTree.SetSyntaxTreeAt(FuncDeclReturnType, LeftTree);
 		TypeList.add(LeftTree.GetParsedType());
 		FuncDeclTree.SetMatchedPatternAt(FuncDeclName, NameSpace, TokenContext, "$FuncName$", Required);
 		FuncDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 		if(FuncDeclTree.IsValidSyntax()) {
 			/*local*/GtFuncBlock FuncBlock = new GtFuncBlock(NameSpace, TypeList);
 			/*local*/boolean FoundAbstractFunc = false;
 			/*local*/GtToken SourceToken = FuncDeclTree.GetSyntaxTreeAt(FuncDeclName).KeyToken;
 			/*local*/String FuncName = GreenTeaUtils.FuncSymbol(SourceToken.ParsedText);
 			/*local*/int ParseFlag = TokenContext.SetBackTrack(false);  // disabled
 			/*local*/GtNameSpace StoreNameSpace = NameSpace.GetNameSpace(KonohaGrammar.ParseNameSpaceFlag(0, TokenContext.ParsingAnnotation));
 			if(LibGreenTea.EqualsString(FuncName, "converter")) {
 				FuncFlag |= ConverterFunc;
 				FuncBlock.SetConverterType();
 				KonohaGrammar.ParseFuncParam(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				if(TypeList.size() != 3) {
 					NameSpace.Context.ReportError(ErrorLevel, SourceToken, "converter takes one parameter");
 					FuncDeclTree.ToError(SourceToken);
 					return FuncDeclTree;
 				}
 				FuncName = "to" + TypeList.get(0);
 				FuncBlock.DefinedFunc = NameSpace.Context.Generator.CreateFunc(FuncFlag, FuncName, 0, FuncBlock.TypeList);
 				KonohaGrammar.ParseFuncBody(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				if(GreenTeaUtils.IsFlag(FuncFlag, StrongCoercionFunc)) {  // this part is for weak type treatment
 					/*local*/GtType FromType = FuncBlock.DefinedFunc.GetFuncParamType(1);
 					/*local*/GtType ToType = FuncBlock.DefinedFunc.GetReturnType();
 					FromType.SetUnrevealedType(ToType);
 					StoreNameSpace = ToType.Context.RootNameSpace;
 				}
 				SourceToken.ParsedText = FuncName;
 				StoreNameSpace.SetConverterFunc(null, null, FuncBlock.DefinedFunc, SourceToken);
 			}
 			else {
 				FuncBlock.SetThisIfInClass(NameSpace.GetType("This"));
 				KonohaGrammar.ParseFuncParam(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				FuncBlock.DefinedFunc = NameSpace.GetFunc(FuncName, 0, TypeList);
 				if(FuncBlock.DefinedFunc == null || !FuncBlock.DefinedFunc.IsAbstract()) {
 					FuncBlock.DefinedFunc = NameSpace.Context.Generator.CreateFunc(FuncFlag, FuncName, 0, TypeList);
 				}
 				else {
 					FoundAbstractFunc = true;
 					FuncBlock.DefinedFunc.FuncFlag = FuncFlag;
 				}
 				KonohaGrammar.ParseFuncBody(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				if(!FuncBlock.DefinedFunc.IsAbstract() || !FoundAbstractFunc) { 
 					StoreNameSpace.AppendFunc(FuncBlock.DefinedFunc, SourceToken);
 					/*local*/GtType RecvType = FuncBlock.DefinedFunc.GetRecvType();
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
 		/*local*/GtFunc DefinedFunc = (/*cast*/GtFunc)ParsedTree.ParsedValue;
 		DefinedFunc.GenerateNativeFunc();
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 	}
 
 	public static GtSyntaxTree ParseGenericFuncDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree FuncTree = TokenContext.CreateSyntaxTree(NameSpace, Pattern, null);
 		/*local*/ArrayList<Object> RevertList = new ArrayList<Object>();
 		FuncTree.SetMatchedTokenAt(KeyTokenIndex, NameSpace, TokenContext, "<", Required);
 		/*local*/int StartIndex = GenericParam;
 		while(FuncTree.IsValidSyntax()) {
 			/*local*/GtType ParamBaseType = NameSpace.Context.VarType;
 			FuncTree.SetMatchedPatternAt(StartIndex, NameSpace, TokenContext, "$Variable$", Required);
 			if(TokenContext.MatchToken(":")) {
 				FuncTree.SetMatchedPatternAt(StartIndex + 1, NameSpace, TokenContext, "$Type$", Required);
 				if(FuncTree.IsValidSyntax()) {
 					ParamBaseType = FuncTree.GetSyntaxTreeAt(StartIndex).GetParsedType();
 				}
 			}
 			if(FuncTree.IsValidSyntax()) {
 				/*local*/GtToken SourceToken = FuncTree.GetSyntaxTreeAt(StartIndex).KeyToken;
 				NameSpace.AppendTypeVariable(SourceToken.ParsedText, ParamBaseType, SourceToken, RevertList);
 				
 			}
 			if(TokenContext.MatchToken(">")) {
 				break;
 			}
 			FuncTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 			StartIndex += 2;
 		}
 		FuncTree.SetMatchedPatternAt(GenericReturnType, NameSpace, TokenContext, "$Type$", Required);
 		if(FuncTree.IsValidSyntax()) {
 			FuncTree = KonohaGrammar.ParseFuncDecl(NameSpace, TokenContext, FuncTree.GetSyntaxTreeAt(GenericReturnType), NameSpace.GetSyntaxPattern("$FuncDecl$"));
 			if(FuncTree.IsValidSyntax()) {
 				/*local*/GtFunc DefinedFunc = (/*cast*/GtFunc)FuncTree.ParsedValue;
 				DefinedFunc.FuncFlag |= GenericFunc;
 			}
 		}
 		NameSpace.Revert(RevertList);
 		return FuncTree;
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
 		/*local*/int FuncFlag = KonohaGrammar.ParseFuncFlag(ConstructorFunc, TokenContext.ParsingAnnotation);
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		TypeList.add(ThisType);
 		FuncDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required | OpenSkipIndent);
 		if(FuncDeclTree.IsValidSyntax()) {
 			/*local*/GtFuncBlock FuncBlock = new GtFuncBlock(NameSpace, TypeList);
 			/*local*/GtToken SourceToken = FuncDeclTree.KeyToken;
 			/*local*/int ParseFlag = TokenContext.SetBackTrack(false);  // disabled
 			/*local*/GtNameSpace StoreNameSpace = NameSpace.GetNameSpace(KonohaGrammar.ParseNameSpaceFlag(0, TokenContext.ParsingAnnotation));
 			FuncBlock.SetThisIfInClass(ThisType);
 			KonohaGrammar.ParseFuncParam(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 			if(FuncDeclTree.IsValidSyntax()) {
 				FuncBlock.DefinedFunc = NameSpace.Context.Generator.CreateFunc(FuncFlag, ThisType.ShortName, 0, FuncBlock.TypeList);
 				KonohaGrammar.ParseFuncBody(NameSpace, TokenContext, FuncDeclTree, FuncBlock);
 				StoreNameSpace.AppendConstructor(ThisType, FuncBlock.DefinedFunc, SourceToken.AddTypeInfoToErrorMessage(ThisType));
 				FuncDeclTree.ParsedValue = FuncBlock.DefinedFunc;
 			}
 			TokenContext.SetRememberFlag(ParseFlag);
 		}
 		return FuncDeclTree;
 	}
 
 	// Array
 	public static GtSyntaxTree ParseNewArray(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ArrayTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "new");
 		ArrayTree.AppendMatchedPattern(NameSpace, TokenContext, "$Type$", Required);
 		while(TokenContext.HasNext() && ArrayTree.IsValidSyntax()) {
 			ArrayTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "[", Required);
 			ArrayTree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Required);
 			ArrayTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "]", Required);
 			if(!TokenContext.IsToken("[")) {
 				break;
 			}
 		}
 		return ArrayTree;
 	}
 
 	public static GtNode TypeNewArray(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType ArrayType = ParsedTree.GetSyntaxTreeAt(0).GetParsedType();
 		/*local*/GtNode ArrayNode = Gamma.Generator.CreateNewArrayNode(Gamma.ArrayType, ParsedTree);
 		/*local*/int i = 1;
 		while(i < LibGreenTea.ListSize(ParsedTree.SubTreeList)) {
 			/*local*/GtNode Node = ParsedTree.TypeCheckAt(i, Gamma, Gamma.IntType, DefaultTypeCheckPolicy);
 			if(Node.IsErrorNode()) {
 				return Node;
 			}
 			ArrayType = Gamma.Context.GetGenericType1(Gamma.ArrayType, ArrayType, true);
 			ArrayNode.Append(Node);
 			i = i + 1;
 		}
 		ArrayNode.Type = ArrayType;
 		return ArrayNode;
 	}
 
 	public static GtSyntaxTree ParseArray(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int OldFlag = TokenContext.SetSkipIndent(true);
 		/*local*/GtSyntaxTree ArrayTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "[");
 		while(TokenContext.HasNext() && ArrayTree.IsValidSyntax()) {
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
 			if(Node.IsErrorNode()) {
 				return Node;
 			}
 			if(ElemType.IsVarType()) {
 				ElemType = Node.Type;
 				ArrayNode.Type = Gamma.Context.GetGenericType1(Gamma.ArrayType, ElemType, true);
 			}
 			ArrayNode.Append(Node);
 			i = i + 1;
 		}
 		if(ElemType.IsVarType()) {
 			ArrayNode.Type = Gamma.Context.GetGenericType1(Gamma.ArrayType, Gamma.AnyType, true);
 		}
 		return ArrayNode;
 	}
 
 	public static GtSyntaxTree ParseSize(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ArrayTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "|");
 		ArrayTree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$SuffixExpression$", Required);
 		ArrayTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "|", Required);
 		return ArrayTree;
 	}
 
 	public static GtNode TypeSize(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode ExprNode = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ExprNode.IsErrorNode()) {
 			return ExprNode;
 		}
 		/*local*/GtPolyFunc PolyFunc = Gamma.NameSpace.GetMethod(ExprNode.Type, GreenTeaUtils.FuncSymbol("||"), true);
 		//System.err.println("polyfunc: " + PolyFunc);
 		/*local*/GtFunc Func = PolyFunc.ResolveUnaryMethod(Gamma, ExprNode.Type);
 		LibGreenTea.Assert(Func != null);  // any has ||
 		Gamma.CheckFunc("operator", Func, ParsedTree.KeyToken);
 		/*local*/GtNode Node = Gamma.Generator.CreateApplyNode(Func.GetReturnType(), ParsedTree, Func);
 		Node.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, Func));
 		Node.Append(ExprNode);
 		return Node;
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
 		/*local*/String OperatorSymbol = "[]";
 		if(TokenContext.MatchToken("=")) {
 			ArrayTree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Required);
 			OperatorSymbol = "[]=";
 		}
 		if(ArrayTree.IsValidSyntax()) {
 			ArrayTree.KeyToken.ParsedText = OperatorSymbol;
 		}
 		return ArrayTree;
 	}
 	
 	public static GtNode TypeIndexer(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		/*local*/GtNode RecvNode = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(!RecvNode.IsErrorNode()) {
 			/*local*/String MethodName = ParsedTree.KeyToken.ParsedText;
 			/*local*/GtResolvedFunc ResolvedFunc = null;
 			/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(RecvNode.Type, GreenTeaUtils.FuncSymbol(MethodName), true);
 			//System.err.println("polyfunc: " + PolyFunc);
 			/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 			ParamList.add(RecvNode);
 			ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 			if(ResolvedFunc.Func == null) {
 				return Gamma.CreateSyntaxErrorNode(ParsedTree, "undefined: " + MethodName + " of " + RecvNode.Type);
 			}
 			/*local*/GtNode Node = Gamma.Generator.CreateIndexerNode(ResolvedFunc.ReturnType, ParsedTree, ResolvedFunc.Func, RecvNode);
 			Node.AppendNodeList(1, ParamList);
 			return Node;
 		}
 		return RecvNode;
 	}
 
 	public static GtSyntaxTree ParseSlice(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree SliceTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "[");
 		SliceTree.AppendParsedTree2(LeftTree);
 		SliceTree.SetMatchedPatternAt(1, NameSpace, TokenContext, "$Expression$", Optional);
 		if(!SliceTree.HasNodeAt(1)) {
 			SliceTree.SetSyntaxTreeAt(1, SliceTree.CreateConstTree(0L)); // s[:x]
 		}
 		SliceTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 		SliceTree.AppendMatchedPattern(NameSpace, TokenContext, "$Expression$", Optional);
 		SliceTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "]", Required);
 		return SliceTree;
 	}
 
 	public static GtNode TypeSlice(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode RecvNode = ParsedTree.TypeCheckAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(!RecvNode.IsErrorNode()) {
 			return KonohaGrammar.TypeMethodCall(Gamma, ParsedTree, RecvNode, "[:]");
 		}
 		return RecvNode;
 	}
 
 	// ClassDecl
 
 	private static boolean TypeFieldDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtClassField ClassField) {
 		/*local*/int    FieldFlag = KonohaGrammar.ParseVarFlag(0, ParsedTree.Annotation);
 		/*local*/GtType DeclType = ParsedTree.GetSyntaxTreeAt(VarDeclType).GetParsedType();
 		/*local*/String FieldName = ParsedTree.GetSyntaxTreeAt(VarDeclName).KeyToken.ParsedText;
 		/*local*/GtNode InitValueNode = null;
 		/*local*/Object InitValue = null;
 		if(ParsedTree.HasNodeAt(VarDeclValue)) {
 			InitValueNode = ParsedTree.TypeCheckAt(VarDeclValue, Gamma, DeclType, OnlyConstPolicy | NullablePolicy);
 			if(InitValueNode.IsErrorNode()) {
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
 		/*local*/GtNameSpace ClassNameSpace = new GtNameSpace(NameSpace.Context, NameSpace);
 		/*local*/GtToken NameToken = ClassDeclTree.GetSyntaxTreeAt(ClassDeclName).KeyToken;
 		/*local*/GtType SuperType = NameSpace.Context.StructType;
 		if(ClassDeclTree.HasNodeAt(ClassDeclSuperType)) {
 			SuperType = ClassDeclTree.GetSyntaxTreeAt(ClassDeclSuperType).GetParsedType();
 		}
 		/*local*/int ClassFlag = KonohaGrammar.ParseClassFlag(0, TokenContext.ParsingAnnotation);
 		/*local*/String ClassName = NameToken.ParsedText;
 		/*local*/GtType DefinedType = NameSpace.GetType(ClassName);
 		if(DefinedType != null && DefinedType.IsAbstract()) {
 			DefinedType.TypeFlag = ClassFlag;
 			DefinedType.SuperType = SuperType;
 			NameToken = null; // preventing duplicated symbol message at (A)
 		}
 		else {
 			DefinedType = SuperType.CreateSubType(ClassFlag, ClassName, null, null);
 			ClassNameSpace.AppendTypeName(DefinedType, NameToken);  // temporary
 		}
 		ClassNameSpace.SetSymbol("This", DefinedType, NameToken);
 		ClassDeclTree.SetMatchedPatternAt(ClassDeclBlock, ClassNameSpace, TokenContext, "$Block$", Optional);
 		if(ClassDeclTree.HasNodeAt(ClassDeclBlock)) {
 			/*local*/GtClassField ClassField = new GtClassField(DefinedType, NameSpace);
 			/*local*/GtTypeEnv Gamma = new GtTypeEnv(ClassNameSpace);
 			/*local*/GtSyntaxTree SubTree = ClassDeclTree.GetSyntaxTreeAt(ClassDeclBlock);
 			while(SubTree != null) {
 				if(SubTree.Pattern.EqualsName("$VarDecl$")) {
 					KonohaGrammar.TypeFieldDecl(Gamma, SubTree, ClassField);
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
 		/*local*/GtParserContext Context = NameSpace.Context;
 		NameSpace.SetSymbol("true", true, null);
 		NameSpace.SetSymbol("false", false, null);
 
 		NameSpace.AppendTokenFunc(" \t", GtGrammar.LoadTokenFunc(Context, this, "WhiteSpaceToken"));
 		NameSpace.AppendTokenFunc("\n",  GtGrammar.LoadTokenFunc(Context, this, "IndentToken"));
 		NameSpace.AppendTokenFunc(";", GtGrammar.LoadTokenFunc(Context, this, "SemiColonToken"));
 		NameSpace.AppendTokenFunc("{}()[]<>.,?:+-*/%=&|!@~^$", GtGrammar.LoadTokenFunc(Context, this, "OperatorToken"));
 		NameSpace.AppendTokenFunc("/", GtGrammar.LoadTokenFunc(Context, this, "CommentToken"));  // overloading
 		NameSpace.AppendTokenFunc("Aa_", GtGrammar.LoadTokenFunc(Context, this, "SymbolToken"));
 
 		NameSpace.AppendTokenFunc("\"", GtGrammar.LoadTokenFunc(Context, this, "StringLiteralToken"));
 		//NameSpace.AppendTokenFunc("\"", GtGrammar.LoadTokenFunc(ParserContext, this, "StringLiteralToken_StringInterpolation"));
 		NameSpace.AppendTokenFunc("'", GtGrammar.LoadTokenFunc(Context, this, "CharLiteralToken"));
 		NameSpace.AppendTokenFunc("1",  GtGrammar.LoadTokenFunc(Context, this, "NumberLiteralToken"));
 
 		/*local*/GtFunc ParseUnary     = GtGrammar.LoadParseFunc(Context, this, "ParseUnary");
 		/*local*/GtFunc  TypeUnary      = GtGrammar.LoadTypeFunc(Context, this, "TypeUnary");
 		/*local*/GtFunc ParseBinary    = GtGrammar.LoadParseFunc(Context, this, "ParseBinary");
 		/*local*/GtFunc  TypeBinary     = GtGrammar.LoadTypeFunc(Context, this, "TypeBinary");
 		/*local*/GtFunc  TypeConst      = GtGrammar.LoadTypeFunc(Context, this, "TypeConst");
 
 		NameSpace.AppendSyntax("+", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("-", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("~", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("! not", GtGrammar.LoadParseFunc(Context, this, "ParseNot"), GtGrammar.LoadTypeFunc(Context, this, "TypeNot"));
 		NameSpace.AppendSyntax("++ --", GtGrammar.LoadParseFunc(Context, this, "ParseIncl"), GtGrammar.LoadTypeFunc(Context, this, "TypeIncl"));
 
 		NameSpace.AppendExtendedSyntax("* / % mod", PrecedenceCStyleMUL, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("+ -", PrecedenceCStyleADD, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("< <= > >=", PrecedenceCStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("== !=", PrecedenceCStyleEquals, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("<< >>", PrecedenceCStyleSHIFT, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("&", PrecedenceCStyleBITAND, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("|", PrecedenceCStyleBITOR, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("^", PrecedenceCStyleBITXOR, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("=", PrecedenceCStyleAssign | LeftJoin, ParseBinary, GtGrammar.LoadTypeFunc(Context, this, "TypeAssign"));
 		NameSpace.AppendExtendedSyntax("+= -= *= /= %= <<= >>= & | ^=", PrecedenceCStyleAssign, ParseBinary, GtGrammar.LoadTypeFunc(Context, this, "TypeSelfAssign"));
 		NameSpace.AppendExtendedSyntax("++ --", 0, GtGrammar.LoadParseFunc(Context, this, "ParseIncl"), GtGrammar.LoadTypeFunc(Context, this, "TypeIncl"));
 
 		NameSpace.AppendExtendedSyntax("&& and", PrecedenceCStyleAND, ParseBinary, GtGrammar.LoadTypeFunc(Context, this, "TypeAnd"));
 		NameSpace.AppendExtendedSyntax("|| or", PrecedenceCStyleOR, ParseBinary, GtGrammar.LoadTypeFunc(Context, this, "TypeOr"));
 		NameSpace.AppendExtendedSyntax("<: instanceof", PrecedenceInstanceof, ParseBinary, GtGrammar.LoadTypeFunc(Context, this, "TypeInstanceOf"));
 
 		NameSpace.AppendExtendedSyntax("?", 0, GtGrammar.LoadParseFunc(Context, this, "ParseTrinary"), GtGrammar.LoadTypeFunc(Context, this, "TypeTrinary"));
 
 		NameSpace.AppendSyntax("$Error$", GtGrammar.LoadParseFunc(Context, this, "ParseError"), GtGrammar.LoadTypeFunc(Context, this, "TypeError"));
 		NameSpace.AppendSyntax("$Empty$", GtGrammar.LoadParseFunc(Context, this, "ParseEmpty"), GtGrammar.LoadTypeFunc(Context, this, "TypeEmpty"));
 		NameSpace.AppendSyntax(";", GtGrammar.LoadParseFunc(Context, this, "ParseSemiColon"), null);
 		NameSpace.AppendSyntax("$Symbol$", GtGrammar.LoadParseFunc(Context, this, "ParseSymbol"), null);
 		NameSpace.AppendSyntax("$Type$",GtGrammar.LoadParseFunc(Context, this, "ParseType"), TypeConst);
 		NameSpace.AppendSyntax("$TypeSuffix$", GtGrammar.LoadParseFunc(Context, this, "ParseTypeSuffix"), null);
 		NameSpace.AppendSyntax("<", GtGrammar.LoadParseFunc(Context, this, "ParseGenericFuncDecl"), null);
 		NameSpace.AppendSyntax("$Variable$", GtGrammar.LoadParseFunc(Context, this, "ParseVariable"), GtGrammar.LoadTypeFunc(Context, this, "TypeVariable"));
 		NameSpace.AppendSyntax("$Const$", GtGrammar.LoadParseFunc(Context, this, "ParseConst"), TypeConst);
 		NameSpace.AppendSyntax("$CharLiteral$", GtGrammar.LoadParseFunc(Context, this, "ParseCharLiteral"), GtGrammar.LoadTypeFunc(Context, this, "TypeCharLiteral"));
 		NameSpace.AppendSyntax("$StringLiteral$", GtGrammar.LoadParseFunc(Context, this, "ParseStringLiteral"), TypeConst);
 		NameSpace.AppendSyntax("$IntegerLiteral$", GtGrammar.LoadParseFunc(Context, this, "ParseIntegerLiteral"), TypeConst);
 		NameSpace.AppendSyntax("$FloatLiteral$", GtGrammar.LoadParseFunc(Context, this, "ParseFloatLiteral"), TypeConst);
 
 		NameSpace.AppendExtendedSyntax(".", 0, GtGrammar.LoadParseFunc(Context, this, "ParseGetter"), GtGrammar.LoadTypeFunc(Context, this, "TypeGetter"));
 		NameSpace.AppendSyntax("$Setter$", null, GtGrammar.LoadTypeFunc(Context, this, "TypeSetter"));
 		
 		NameSpace.AppendSyntax("(", GtGrammar.LoadParseFunc(Context, this, "ParseGroup"), GtGrammar.LoadTypeFunc(Context, this, "TypeGroup"));
 		NameSpace.AppendSyntax("(", GtGrammar.LoadParseFunc(Context, this, "ParseCast"), GtGrammar.LoadTypeFunc(Context, this, "TypeCast"));
 		NameSpace.AppendExtendedSyntax("(", 0, GtGrammar.LoadParseFunc(Context, this, "ParseApply"), GtGrammar.LoadTypeFunc(Context, this, "TypeApply"));
 		NameSpace.AppendSyntax("[", GtGrammar.LoadParseFunc(Context, this, "ParseArray"), GtGrammar.LoadTypeFunc(Context, this, "TypeArray"));
 		NameSpace.AppendExtendedSyntax("[", 0, GtGrammar.LoadParseFunc(Context, this, "ParseIndexer"), GtGrammar.LoadTypeFunc(Context, this, "TypeIndexer"));
 		NameSpace.AppendExtendedSyntax("[", 0, GtGrammar.LoadParseFunc(Context, this, "ParseSlice"), GtGrammar.LoadTypeFunc(Context, this, "TypeSlice"));
 		NameSpace.AppendSyntax("|", GtGrammar.LoadParseFunc(Context, this, "ParseSize"), GtGrammar.LoadTypeFunc(Context, this, "TypeSize"));
 
 		NameSpace.AppendSyntax("$Block$", GtGrammar.LoadParseFunc(Context, this, "ParseBlock"), null);
 		NameSpace.AppendSyntax("$Statement$", GtGrammar.LoadParseFunc(Context, this, "ParseStatement"), null);
 		NameSpace.AppendSyntax("$Expression$", GtGrammar.LoadParseFunc(Context, this, "ParseExpression"), null);
 		NameSpace.AppendSyntax("$SuffixExpression$", GtGrammar.LoadParseFunc(Context, this, "ParseSuffixExpression"), null);
 
 		NameSpace.AppendSyntax("$FuncName$", GtGrammar.LoadParseFunc(Context, this, "ParseFuncName"), TypeConst);
 		NameSpace.AppendSyntax("$FuncDecl$", GtGrammar.LoadParseFunc(Context, this, "ParseFuncDecl"), GtGrammar.LoadTypeFunc(Context, this, "TypeFuncDecl"));
 		NameSpace.AppendSyntax("$VarDecl$",  GtGrammar.LoadParseFunc(Context, this, "ParseVarDecl"), GtGrammar.LoadTypeFunc(Context, this, "TypeVarDecl"));
 
 		NameSpace.AppendSyntax("null", GtGrammar.LoadParseFunc(Context, this, "ParseNull"), GtGrammar.LoadTypeFunc(Context, this, "TypeNull"));
 		NameSpace.AppendSyntax("defined", GtGrammar.LoadParseFunc(Context, this, "ParseDefined"), GtGrammar.LoadTypeFunc(Context, this, "TypeDefined"));
 		NameSpace.AppendSyntax("typeof", GtGrammar.LoadParseFunc(Context, this, "ParseTypeOf"), TypeConst);
 		NameSpace.AppendSyntax("require", GtGrammar.LoadParseFunc(Context, this, "ParseRequire"), null);
 		NameSpace.AppendSyntax("import", GtGrammar.LoadParseFunc(Context, this, "ParseImport"), GtGrammar.LoadTypeFunc(Context, this, "TypeImport"));
 
 		NameSpace.AppendSyntax("if", GtGrammar.LoadParseFunc(Context, this, "ParseIf"), GtGrammar.LoadTypeFunc(Context, this, "TypeIf"));
 		NameSpace.AppendSyntax("while", GtGrammar.LoadParseFunc(Context, this, "ParseWhile"), GtGrammar.LoadTypeFunc(Context, this, "TypeWhile"));
 		NameSpace.AppendSyntax("do", GtGrammar.LoadParseFunc(Context, this, "ParseDoWhile"), GtGrammar.LoadTypeFunc(Context, this, "TypeDoWhile"));
 		NameSpace.AppendSyntax("for", GtGrammar.LoadParseFunc(Context, this, "ParseFor"), GtGrammar.LoadTypeFunc(Context, this, "TypeFor"));
 		NameSpace.AppendSyntax("continue", GtGrammar.LoadParseFunc(Context, this, "ParseContinue"), GtGrammar.LoadTypeFunc(Context, this, "TypeContinue"));
 		NameSpace.AppendSyntax("break", GtGrammar.LoadParseFunc(Context, this, "ParseBreak"), GtGrammar.LoadTypeFunc(Context, this, "TypeBreak"));
 		NameSpace.AppendSyntax("return", GtGrammar.LoadParseFunc(Context, this, "ParseReturn"), GtGrammar.LoadTypeFunc(Context, this, "TypeReturn"));
 		NameSpace.AppendSyntax("let const", GtGrammar.LoadParseFunc(Context, this, "ParseSymbolDecl"), null/*GtGrammar.LoadTypeFunc(ParserContext, this, "TypeSymbolDecl")*/);
 
 		NameSpace.AppendSyntax("try", GtGrammar.LoadParseFunc(Context, this, "ParseTry"), GtGrammar.LoadTypeFunc(Context, this, "TypeTry"));
 		NameSpace.AppendSyntax("throw", GtGrammar.LoadParseFunc(Context, this, "ParseThrow"), GtGrammar.LoadTypeFunc(Context, this, "TypeThrow"));
 
 		NameSpace.AppendSyntax("class", GtGrammar.LoadParseFunc(Context, this, "ParseClassDecl2"), GtGrammar.LoadTypeFunc(Context, this, "TypeClassDecl2"));
 		NameSpace.AppendSyntax("constructor", GtGrammar.LoadParseFunc(Context, this, "ParseConstructor2"), GtGrammar.LoadTypeFunc(Context, this, "TypeFuncDecl"));
 		NameSpace.AppendSyntax("super", GtGrammar.LoadParseFunc(Context, this, "ParseSuper"), null);
 		NameSpace.AppendSyntax("this", GtGrammar.LoadParseFunc(Context, this, "ParseThis"), GtGrammar.LoadTypeFunc(Context, this, "TypeThis"));
 		NameSpace.AppendSyntax("new", GtGrammar.LoadParseFunc(Context, this, "ParseNew"), GtGrammar.LoadTypeFunc(Context, this, "TypeApply"));
 		NameSpace.AppendSyntax("new", GtGrammar.LoadParseFunc(Context, this, "ParseNewArray"), GtGrammar.LoadTypeFunc(Context, this, "TypeNewArray"));
 
 		NameSpace.AppendSyntax("enum", GtGrammar.LoadParseFunc(Context, this, "ParseEnum"), GtGrammar.LoadTypeFunc(Context, this, "TypeEnum"));
 		NameSpace.AppendSyntax("switch", GtGrammar.LoadParseFunc(Context, this, "ParseSwitch"), GtGrammar.LoadTypeFunc(Context, this, "TypeSwitch"));
 		NameSpace.AppendSyntax("$CaseBlock$", GtGrammar.LoadParseFunc(Context, this, "ParseCaseBlock"), null);
 
 		// expermental
 		NameSpace.AppendSyntax("__line__", GtGrammar.LoadParseFunc(Context, this, "ParseLine"), GtGrammar.LoadTypeFunc(Context, this, "TypeLine"));
 		NameSpace.AppendSyntax("__", GtGrammar.LoadParseFunc(Context, this, "ParseSymbols"), null);
 	}
 }
