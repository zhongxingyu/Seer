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
 import java.io.File;
 import java.lang.reflect.Method;
 
 import org.GreenTeaScript.DShell.DFault;
 
 
 public class DShellGrammar extends GreenTeaUtils {
 	// LibDShell
 	public final static String GetEnv(String Key) {
 		return System.getenv(Key);
 	}
 
 	public final static boolean IsUnixCommand(String cmd) {
 		String[] path = GetEnv("PATH").split(":");
 		int i = 0;
 		while(i < path.length) {
 			if(LibGreenTea.HasFile(path[i] + "/" + cmd)) {
 				return true;
 			}
 			i = i + 1;
 		}
 		return false;
 	}
 
 	public final static boolean IsFile(String Path) {
 		return new File(Path).isFile();
 	}
 
 	public final static boolean IsDirectory(String Path) {
 		return new File(Path).isDirectory();
 	}
 
 	public final static boolean IsFileExists(String Path) {
 		return new File(Path).exists();
 	}
 
 	public final static boolean IsFileReadable(String Path) {
 		return new File(Path).canRead();
 	}
 
 	public final static boolean IsFileWritable(String Path) {
 		return new File(Path).canWrite();
 	}
 
 	public final static boolean IsFileExecutable(String Path) {
 		return new File(Path).canExecute();
 	}
 
 	public final static String[] ExpandPath(String Path) {
 		/*local*/int Index = Path.indexOf("*");
 		/*local*/String NewPath = LibGreenTea.SubString(Path, 0, Index);
 		/*local*/String[] ExpanddedPaths = new File(NewPath).list();
 		if(ExpanddedPaths != null) {
 			return ExpanddedPaths;
 		}
 		return new String[0];
 	}
 
 	private static String ErrorMessage = "no error reported";
 
 	public static void SetErrorMessage(String Message) {
 		ErrorMessage = Message;
 	}
 	
 	private static Object GetErrorMessage() {
 		return ErrorMessage;
 	}
 	
 	public final static DFault CreateFault(GtNameSpace NameSpace, String DCaseNode, String FaultInfo, String ErrorInfo) {
 		if(FaultInfo == null) {
 			FaultInfo = NameSpace.GetSymbolText("AssumedFault");
 		}
 		DFault Fault = new DFault(NameSpace.GetSymbolText("Location"), FaultInfo, ErrorInfo);
 		return Fault.UpdateDCaseReference(NameSpace.GetSymbolText("DCaseURL"), DCaseNode);
 	}
 
 	public final static DFault CreateExceptionFault(GtNameSpace NameSpace, String DCaseNode, Exception e) {
 		// TODO: Dispatch Fault by type of Exception e
 		// default fault is set to UnexpectedFault
 		return CreateFault(NameSpace, DCaseNode, "UnexpectedFault", e.toString());
 	}
 
 	public final static DFault ExecAction(GtNameSpace NameSpace, String DCaseNode, GtFunc Action) {
 		DFault Fault = null;
 		try {
 			Fault = (DFault)((Method)Action.FuncBody).invoke(null);
 		}
 		catch (Exception e) {
 			Fault = CreateExceptionFault(NameSpace, DCaseNode, e);
 		}
 		if(Fault == null) {
 			// report in success case
 		}
 		else {
 			// report failed case
 		}
 		return Fault;
 	}
 
 	
 	// Grammar 
 	private static String CommandSymbol(String Symbol) {
 		return "__$" + Symbol;
 	}
 
 	private static void AppendCommand(GtNameSpace NameSpace, String CommandPath, GtToken SourceToken) {
 		if(CommandPath.length() > 0) {
 			int loc = CommandPath.lastIndexOf('/');
 			String Command = CommandPath;
 			if(loc != -1) {
 				if(!IsFileExecutable(CommandPath)) {
 					NameSpace.Context.ReportError(ErrorLevel, SourceToken, "not executable: " + CommandPath);
 				}
 				else {
 					Command = CommandPath.substring(loc+1);
 					NameSpace.SetSymbol(Command, NameSpace.GetSyntaxPattern("$DShell2$"), SourceToken);
 					NameSpace.SetSymbol(CommandSymbol(Command), CommandPath, null);
 				}
 			}
 			else {
 				if(IsUnixCommand(CommandPath)) {
 					NameSpace.SetSymbol(Command, NameSpace.GetSyntaxPattern("$DShell2$"), SourceToken);
 					NameSpace.SetSymbol(CommandSymbol(Command), CommandPath, null);
 				} 
 				else {
 					NameSpace.Context.ReportError(ErrorLevel, SourceToken, "unknown command: " + CommandPath);
 				}
 			}
 		}
 	}
 
 	public static GtSyntaxTree ParseCommand(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree CommandTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "command");
 		/*local*/String Command = "";
 		/*local*/GtToken SourceToken = null;
 		while(TokenContext.HasNext()) {
 			/*local*/GtToken Token = TokenContext.Next();
 			if(Token.EqualsText(",")) {
 				Token.ParsedText = "";
 			}
 			if(Token.IsDelim() || Token.IsIndent()) {
 				break;
 			}
 			SourceToken = Token;
 			Command += Token.ParsedText;
 			if(Token.IsNextWhiteSpace()) {
 				AppendCommand(NameSpace, Command, SourceToken);
 				Command = "";
 				if(SourceToken.IsError()) {
 					CommandTree.ToError(SourceToken);
 				}
 			}
 		}
 		AppendCommand(NameSpace, Command, SourceToken);
 		if(SourceToken.IsError()) {
 			CommandTree.ToError(SourceToken);
 		}
 		return CommandTree;
 	}
 
 	public static GtSyntaxTree ParseEnv(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree CommandTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "letenv");
 		GtToken Token = TokenContext.Next();
 		if(!LibGreenTea.IsVariableName(Token.ParsedText, 0)) {
 			return TokenContext.ReportExpectedMessage(Token, "name", true);
 		}
 		String Name = Token.ParsedText;
 		String Env  = GetEnv(Name);
 		if(TokenContext.MatchToken("=")) {
 			GtSyntaxTree ConstTree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 			if(GreenTeaUtils.IsMismatchedOrError(ConstTree)) {
 				return ConstTree;
 			}
 			if(Env == null) {
 				GtTypeEnv Gamma = new GtTypeEnv(NameSpace);
 				GtNode ConstNode = ConstTree.TypeCheck(Gamma, Gamma.StringType, DefaultTypeCheckPolicy);
 				Env = (/*cast*/String)ConstNode.ToConstValue(true);
 			}
 		}
 		if(Env == null) {
 			NameSpace.Context.ReportError(ErrorLevel, Token, "undefined environment variable: " + Name);
 			CommandTree.ToError(Token);
 		}
 		else {
 			NameSpace.SetSymbol(Name, Env, Token);
 			CommandTree.ToConstTree(Env);
 		}
 		return CommandTree;
 	}
 
 //	private static void AppendInRedirectTree(GtSyntaxTree CommandTree, String TargetName) {
 //		/*local*/GtToken Token = new GtToken("<", CommandTree.KeyToken.FileLine);
 //		/*local*/GtSyntaxTree SubTree = new GtSyntaxTree(CommandTree.Pattern, CommandTree.NameSpace, Token, null);
 //		String[] Value = {"<", TargetName};
 //		SubTree.ParsedValue = Value;
 //		CommandTree.AppendParsedTree2(SubTree);
 //	}
 //
 //	public static GtSyntaxTree ParseDShell(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 //		/*local*/GtToken CommandToken = TokenContext.GetToken();
 //		/*local*/String Command = (/*cast*/String)NameSpace.GetSymbol(CommandSymbol(CommandToken.ParsedText));
 //		if(Command == null) {
 //			//TODO()
 //		}
 //		/*local*/GtSyntaxTree CommandTree = new GtSyntaxTree(Pattern, NameSpace, CommandToken, null);
 //		/*local*/GtSyntaxTree SubTree = new GtSyntaxTree(Pattern, NameSpace, CommandToken, null);
 //		/*local*/ArrayList<String> CommandLine = new ArrayList<String>();
 //		/*local*/String Argument = "";
 //		/*local*/boolean FoundOpen = false;
 //		/*local*/boolean InputRedirect = false;
 //		while(TokenContext.HasNext()) {
 //			GtToken Token = TokenContext.GetToken();
 //			if(Token.EqualsText("||") || Token.EqualsText("&&")) {
 //				if(Argument.length() > 0) {
 //					if(InputRedirect) {
 //						AppendInRedirectTree(CommandTree, Argument);
 //						InputRedirect = false;
 //					}
 //					else {
 //						CommandLine.add(Argument);
 //					}
 //				}
 //				if(CommandLine.size() > 0) {
 //					SubTree.ParsedValue = CommandLine.toArray(new String[CommandLine.size()]);
 //					CommandTree.AppendParsedTree2(SubTree);
 //				}
 //				/*local*/GtSyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern(NameSpace);
 //				return GreenTeaUtils.ApplySyntaxPattern(NameSpace, TokenContext, CommandTree, ExtendedPattern);
 //			}
 //			if(Token.IsDelim() || Token.IsIndent()) {
 //				break;
 //			}
 //			if(!FoundOpen && StopTokens.indexOf(Token.ParsedText) != -1) {
 //				if(!LibGreenTea.EqualsString(Token.ParsedText, "&") && 
 //						!LibGreenTea.EqualsString(Token.ParsedText, "|")) {
 //					break;
 //				}
 //			}
 //			Token = TokenContext.Next();
 //			if(Token.EqualsText("{")) {
 //				FoundOpen = true;
 //			}
 //			if(Token.EqualsText("}")) {
 //				FoundOpen = false;
 //			}
 //			if(Token.EqualsText("$")) {   // $HOME/hoge
 //				GtToken Token2 = TokenContext.GetToken();
 //				if(LibGreenTea.IsVariableName(Token2.ParsedText, 0)) {
 //					Object Env = NameSpace.GetSymbol(Token2.ParsedText);
 //					if(Env instanceof String) {
 //						Argument += Env.toString();
 //					}
 //					else {
 //						Argument += "${" + Token2.ParsedText + "}";
 //					}
 //					TokenContext.Next();
 //					continue;
 //				}
 //			}
 //			
 //			if(!FoundOpen && Token.PresetPattern == null && Token.EqualsText("<")) {
 //				if(Argument.length() > 0) {
 //					CommandLine.add(Argument);
 //					Argument = "";
 //				}
 //				InputRedirect = true;
 //				continue;
 //			}
 //			if(!FoundOpen && Token.PresetPattern == null && 
 //					(Token.EqualsText("|") || Token.EqualsText(">") || Token.EqualsText("&"))) {
 //				if(Argument.length() > 0) {
 //					if(InputRedirect) {
 //						AppendInRedirectTree(CommandTree, Argument);
 //						InputRedirect = false;
 //					}
 //					else {
 //						CommandLine.add(Argument);
 //					}
 //					Argument = "";
 //				}
 //				SubTree.ParsedValue = CommandLine.toArray(new String[CommandLine.size()]);
 //				CommandTree.AppendParsedTree2(SubTree);
 //				CommandLine = new ArrayList<String>();
 //				SubTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 //				if(Token.EqualsText(">")) {
 //					SubTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 //					CommandLine.add(Token.ParsedText);
 //				}
 //				if(Token.EqualsText("&")) {
 //					SubTree.KeyToken = Token;
 //					/*local*/String[] BackGround = {"set", "background"};
 //					SubTree.ParsedValue = BackGround ;
 //					CommandTree.AppendParsedTree2(SubTree);
 //					SubTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 //				}
 //				continue;
 //			}
 //			
 //			/*local*/String ParsedText = Token.ParsedText;
 //			if(Token.PresetPattern != null && Token.PresetPattern.EqualsName("$StringLiteral$")) {
 //				ParsedText = LibGreenTea.UnquoteString(ParsedText);
 //			}
 //			if(!FoundOpen && Token.IsNextWhiteSpace() ) {
 //				if(Argument.length() > 0) {
 //					ParsedText = Argument + ParsedText;
 //				}
 //				if(InputRedirect) {
 //					AppendInRedirectTree(CommandTree, ParsedText);
 //					InputRedirect = false;
 //				}
 //				else {
 //					CommandLine.add(ParsedText);
 //				}
 //				Argument = "";
 //			}
 //			else {
 //				Argument += ParsedText;
 //			}
 //		}
 //		if(Argument.length() > 0) {
 //			if(InputRedirect) {
 //				AppendInRedirectTree(CommandTree, Argument);
 //				InputRedirect = false;
 //			}
 //			else {
 //				CommandLine.add(Argument);
 //			}
 //		}
 //		if(CommandLine.size() > 0) {
 //			SubTree.ParsedValue = CommandLine.toArray(new String[CommandLine.size()]);
 //			CommandTree.AppendParsedTree2(SubTree);
 //		}
 //		return CommandTree;
 //	}
 //
 //	public static GtNode TypeDShell(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 //		/*local*/GtType Type = null;
 //		if(ContextType.IsStringType() || ContextType.IsBooleanType() || ContextType.IsVoidType()) {
 //			Type = ContextType;
 //		}
 //		else {
 //			/*local*/Object ConstValue = Gamma.NameSpace.GetSymbol("Process");
 //			if(!(ConstValue instanceof GtType)) {
 //				return Gamma.CreateSyntaxErrorNode(ParsedTree, "Process type is not defined in this context");
 //			}
 //			Type = (/*cast*/GtType) ConstValue;
 //		}
 //		/*local*/CommandNode Node = null;
 //		/*local*/CommandNode PrevNode = null;
 //		/*local*/int Index = 0;
 //		/*local*/int SubTreeSize = LibGreenTea.ListSize(ParsedTree.SubTreeList);
 //		while(Index < SubTreeSize) {
 //			/*local*/GtSyntaxTree SubTree = ParsedTree.SubTreeList.get(Index);
 //			/*local*/CommandNode CurrentNode = (/*cast*/CommandNode) Gamma.Generator.CreateCommandNode(Type, SubTree, null);
 //			String[] CommandLine = (/*cast*/String[]) SubTree.ParsedValue;
 //			int i = 0;
 //			while(i < CommandLine.length) {
 //				String Argument = CommandLine[i];
 //				if(Argument.indexOf("${") != -1) {
 //					/*local*/String Text = LibGreenTea.QuoteString(Argument);
 //					/*local*/GtTokenContext TokenContext = new GtTokenContext(Gamma.NameSpace, Text, CurrentNode.Token.FileLine);
 //					/*local*/GtSyntaxTree TopLevelTree = TokenContext.ParsePattern(Gamma.NameSpace, "$Expression$", Required);
 //					/*local*/GtNode ExprNode = TopLevelTree.TypeCheck(Gamma, Gamma.StringType, DefaultTypeCheckPolicy);
 //					CurrentNode.Append(ExprNode);
 //				}
 //				else if(Argument.indexOf("*") != -1) {
 //					String[] ExpandedArguments = DShellGrammar.ExpandPath(Argument);
 //					int j = 0;
 //					while(j < ExpandedArguments.length) {
 //						CurrentNode.Append(Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, ExpandedArguments[j]));
 //						j = j + 1;
 //					}
 //					if(ExpandedArguments.length == 0) {
 //						Gamma.Context.ReportError(InfoLevel, ParsedTree.KeyToken, "no file: " + Argument);
 //					}
 //				}
 //				else {
 //					CurrentNode.Append(Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, CommandLine[i]));
 //				}
 //				i += 1;
 //			}
 //			
 //			if(Node == null) {
 //				Node = CurrentNode;
 //				PrevNode = CurrentNode;
 //			}
 //			else {
 //				PrevNode.PipedNextNode = CurrentNode;
 //				PrevNode = CurrentNode;
 //			}
 //			Index += 1;
 //		}
 //		return Node;
 //	}
 
 	private final static String FileOperators = "-d -e -f -r -w -x";
 	private final static String StopTokens = ";,)]}&&||";
 
 	public static GtSyntaxTree ParseFilePath(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.GetToken();
 		boolean HasStringExpr = false;
 		String Path = null;
 		if(Token.IsIndent() || StopTokens.indexOf(Token.ParsedText) != -1) {
 			return null;
 		}
 		else if(Token.IsQuoted()) {
 			Path = LibGreenTea.UnquoteString(Token.ParsedText);
 			if(Path.indexOf("${") != -1) {
 				HasStringExpr = true;
 			}
 			TokenContext.Next();
 		}
 		if(Path == null) {
 			boolean FoundOpen = false;
 			Path = "";
 			while(TokenContext.HasNext()) {
 				Token = TokenContext.GetToken();
 				/*local*/String ParsedText = Token.ParsedText;
 				if(Token.IsIndent() || (!FoundOpen && StopTokens.indexOf(Token.ParsedText) != -1)) {
 					break;
 				}
 				TokenContext.Next();
 				if(Token.EqualsText("$")) {   // $HOME/hoge
 					GtToken Token2 = TokenContext.GetToken();
 					if(LibGreenTea.IsVariableName(Token2.ParsedText, 0)) {
 						Path += "${" + Token2.ParsedText + "}";
 						HasStringExpr = true;
 						TokenContext.Next();
						if(Token2.IsNextWhiteSpace()) {
							break;
						}
 						continue;
 					}
 				}
 				if(Token.EqualsText("{")) {
 					HasStringExpr = true;
 					FoundOpen = true;
 				}
 				if(Token.EqualsText("}")) {
 					FoundOpen = false;
 				}
 				if(Token.EqualsText("~")) {
 					ParsedText = System.getenv("HOME");
 				}
 				Path += ParsedText;
 				if(!FoundOpen && Token.IsNextWhiteSpace()) {
 					break;
 				}
 			}
 		}
 		if(!HasStringExpr) {
 			GtSyntaxTree PathTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 			PathTree.ToConstTree(Path);
 //			System.err.println("debug: " + Path + " ...");
 			return PathTree;
 		}
 		else {
 //			System.err.println("debug: " + Path);			
 			Path = "\"" + Path + "\"";
 			Path = Path.replaceAll("\\$\\{", "\" + (");
 			Path = Path.replaceAll("\\}", ") + \"");
 //			System.err.println("debug: " + Path);
 			/*local*/GtTokenContext LocalContext = new GtTokenContext(NameSpace, Path, Token.FileLine);
 			return LocalContext.ParsePattern(NameSpace, "$Expression$", Required);
 		}
 	}
 		
 	public static GtSyntaxTree ParseFileOperator(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();   // "-"
 		/*local*/GtToken Token2 = TokenContext.Next();  // "f"
 		if(!Token.IsNextWhiteSpace()) {
 			if(FileOperators.indexOf(Token2.ParsedText) != -1) {
 				Token.ParsedText += Token2.ParsedText;  // join to "-f";
 				/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 				Tree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$FilePath$", Required);
 				return Tree;
 			}
 		}
 		return null;
 	}
 
 	public static GtNode TypeFileOperator(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode PathNode = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.StringType, DefaultTypeCheckPolicy);
 		if(!PathNode.IsErrorNode()) {
 			/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 			/*local*/GtPolyFunc PolyFunc = Gamma.NameSpace.GetMethod(Gamma.StringType, FuncSymbol(OperatorSymbol), true);
 			/*local*/GtFunc ResolvedFunc = PolyFunc.ResolveUnaryMethod(Gamma, PathNode.Type);
 			LibGreenTea.Assert(ResolvedFunc != null);
 			/*local*/GtNode ApplyNode =  Gamma.Generator.CreateApplyNode(ResolvedFunc.GetReturnType(), ParsedTree, ResolvedFunc);
 			ApplyNode.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, ResolvedFunc));
 			ApplyNode.Append(PathNode);
 			return ApplyNode;
 		}
 		return PathNode;
 	}
 
 	private static void IncreasePos(GtTokenContext TokenContext, int time, boolean allowIncrement) {
 		if(allowIncrement) {
 			for(int i = 0; i < time; i++) {
 				TokenContext.Next();
 			}
 		}
 	}
 
 	// >, >>, >&, 1>, 2>, 1>>, 2>>, &>, &>>, 1>&1, 1>&2, 2>&1, 2>&2, >&1, >&2
 	private static String FindRedirectSymbol(GtTokenContext TokenContext, boolean allowIncrement) {
 		/*local*/GtToken Token = TokenContext.GetToken();
 		/*local*/int CurrentPos = TokenContext.GetPosition(0);
 		/*local*/int NextLen = 0;
 		/*local*/String RedirectSymbol = Token.ParsedText;
 		if(Token.EqualsText(">>")) {
 			IncreasePos(TokenContext, 1, allowIncrement);
 			return RedirectSymbol;
 		}
 		else if(Token.EqualsText(">")) {
 			NextLen = 2;
 		}
 		else if(Token.EqualsText("1") || Token.EqualsText("2") || Token.EqualsText("&")) {
 			NextLen = 3;
 		}
 		
 		/*local*/GtToken[] NextTokens = new GtToken[NextLen];
 		for(int i = 0; i < NextLen; i++) {
 			TokenContext.Next();
 			NextTokens[i] = TokenContext.GetToken();
 		}
 		TokenContext.RollbackPosition(CurrentPos, 0);
 		
 		if(NextLen == 2) {
 			if(!Token.IsNextWhiteSpace() && NextTokens[0].EqualsText("&")) {
 				RedirectSymbol += NextTokens[0].ParsedText;
 				if(!NextTokens[0].IsNextWhiteSpace()) {
 					if(NextTokens[1].EqualsText("1") || NextTokens[1].EqualsText("2")) {
 						RedirectSymbol += NextTokens[1].ParsedText;
 						IncreasePos(TokenContext, 3, allowIncrement);
 						return RedirectSymbol;
 					}
 				}
 				IncreasePos(TokenContext, 2, allowIncrement);
 				return RedirectSymbol;
 			}
 			IncreasePos(TokenContext, 1, allowIncrement);
 			return RedirectSymbol;
 		}
 		else if(NextLen == 3) {
 			if(!Token.IsNextWhiteSpace() && (NextTokens[0].EqualsText(">") || NextTokens[0].EqualsText(">>"))) {
 				RedirectSymbol += NextTokens[0].ParsedText;
 				if(!NextTokens[0].IsNextWhiteSpace() && 
 						(LibGreenTea.EqualsString(RedirectSymbol, "1>") || LibGreenTea.EqualsString(RedirectSymbol, "2>"))) {
 					if(NextTokens[1].EqualsText("&") && !NextTokens[1].IsNextWhiteSpace()) {
 						if(NextTokens[2].EqualsText("1") || NextTokens[2].EqualsText("2")) {
 							RedirectSymbol += NextTokens[1].ParsedText + NextTokens[2].ParsedText;
 							IncreasePos(TokenContext, 4, allowIncrement);
 							return RedirectSymbol;
 						}
 					}
 				}
 				IncreasePos(TokenContext, 2, allowIncrement);
 				return RedirectSymbol;
 			}
 		}
 		return null;
 	}
 	
 	public static GtSyntaxTree ParseDShell2(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree CommandTree = TokenContext.CreateSyntaxTree(NameSpace, Pattern, null);
 		/*local*/GtToken CommandToken = TokenContext.GetToken();
 		/*local*/String RedirectSymbol = FindRedirectSymbol(TokenContext, true);
 		if(RedirectSymbol != null) {
 			CommandTree.AppendParsedTree2(CommandTree.CreateConstTree(RedirectSymbol));
 		}
 		else {
 			/*local*/String Command = (/*cast*/String)NameSpace.GetSymbol(CommandSymbol(CommandToken.ParsedText));
 			if(Command != null) {
 				CommandTree.AppendParsedTree2(CommandTree.CreateConstTree(Command));
 				TokenContext.Next();
 			}
 			else {
 				CommandTree.AppendMatchedPattern(NameSpace, TokenContext, "$FilePath$", Required);
 			}
 		}
 		TokenContext.SetBackTrack(false);
 		while(TokenContext.HasNext() && CommandTree.IsValidSyntax()) {
 			GtToken Token = TokenContext.GetToken();
 			if(Token.IsIndent() || StopTokens.indexOf(Token.ParsedText) != -1) {
 				if(!Token.EqualsText("|") && !Token.EqualsText("&")) {
 					break;
 				}
 			}
 			if(Token.EqualsText("||") || Token.EqualsText("&&")) {
 				/*local*/GtSyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern(NameSpace);
 				return GreenTeaUtils.ApplySyntaxPattern(NameSpace, TokenContext, CommandTree, ExtendedPattern);
 			}
 			if(Token.EqualsText("|")) {
 				TokenContext.Next();
 				/*local*/GtSyntaxTree PipedTree = TokenContext.ParsePattern(NameSpace, "$DShell2$", Required);
 				if(PipedTree.IsError()) {
 					return PipedTree;
 				}
 				CommandTree.AppendParsedTree2(PipedTree);
 				return CommandTree;
 			}
 			if(FindRedirectSymbol(TokenContext, false) != null) {
 				/*local*/GtSyntaxTree RedirectTree = TokenContext.ParsePattern(NameSpace, "$DShell2$", Required);
 				if(RedirectTree.IsError()) {
 					return RedirectTree;
 				}
 				CommandTree.AppendParsedTree2(RedirectTree);
 				return CommandTree;
 			}
 			CommandTree.AppendMatchedPattern(NameSpace, TokenContext, "$FilePath$", Required);
 		}
 		return CommandTree;
 	}
 
 	public static GtNode TypeDShell2(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType Type = null;
 		if(ContextType.IsStringType() || ContextType.IsBooleanType()) {
 			Type = ContextType;
 		}
 		else {
 			Type = Gamma.VoidType;
 		}
 		/*local*/GtNode PipedNode = null;
 		/*local*/int Index = 0;
 		/*local*/int ArgumentSize = LibGreenTea.ListSize(ParsedTree.SubTreeList);
 		while(Index < ArgumentSize) {
 			/*local*/GtSyntaxTree SubTree = ParsedTree.SubTreeList.get(Index);
 			if(SubTree.Pattern.EqualsName("$DShell2$")) {
 				PipedNode = TypeDShell2(Gamma, SubTree, ContextType);
 				ArgumentSize = Index;
 //				Type = Gamma.VoidType;
 				break;
 			}
 			Index += 1;
 		}
 		/*local*/GtNode Node = Gamma.Generator.CreateCommandNode(Type, ParsedTree, PipedNode);
 		Index = 0;
 		while(Index < ArgumentSize) {
 			/*local*/GtNode ArgumentNode = ParsedTree.TypeCheckAt(Index, Gamma, Gamma.StringType, DefaultTypeCheckPolicy);
 			if(ArgumentNode.IsErrorNode()) {
 				return ArgumentNode;
 			}
 			Node.Append(ArgumentNode);
 			Index += 1;
 		}
 		return Node;
 	}
 
 	public static GtSyntaxTree ParseShell(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		TokenContext.Next();
 		return TokenContext.ParsePattern(NameSpace, "$DShell2$", Required);
 	}
 	
 	// dlog $Expr 
 	private static GtNode CreateDCaseNode(GtTypeEnv Gamma, GtSyntaxTree ParsedTree) {
 		String ContextualFuncName = "Admin";
 		if(Gamma.Func != null) {
 			ContextualFuncName = Gamma.Func.FuncName;
 		}
 		return Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, ContextualFuncName);		
 	}
 	
 	public static GtSyntaxTree ParseDLog(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree Tree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "dlog");
 		Tree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$Expression$", Required);
 		return Tree;
 	}
 
 	// dlog FunctionName => ExecAction(NameSpace, ContextualFuncName, Action);
 	public static GtNode TypeDLog(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		ContextType = LibGreenTea.GetNativeType(Gamma.Context, DFault.class);
 		GtNode ActionNode = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, ContextType, DefaultTypeCheckPolicy);
 		if(ActionNode.IsErrorNode()) {
 			return ActionNode;
 		}
 		if(ActionNode instanceof GtApplyNode) {
 			GtFunc ActionFunc = ((GtApplyNode)ActionNode).Func;
 			if(ActionFunc.GetFuncParamSize() == 0) {
 				GtFunc ReportFunc = (GtFunc)Gamma.NameSpace.GetSymbol("$ReportBuiltInFunc");
 				GtNode ApplyNode = Gamma.Generator.CreateApplyNode(ContextType, ParsedTree, ReportFunc);
 				ApplyNode.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, ReportFunc));
 				ApplyNode.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, Gamma.NameSpace));
 				ApplyNode.Append(CreateDCaseNode(Gamma, ParsedTree));
 				ApplyNode.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, ActionFunc));
 				return ApplyNode;
 			}
 		}
 		return Gamma.CreateSyntaxErrorNode(ParsedTree, "action must be Func<DFault, void>");
 	}
 	
 	// Raise Expression
 	public final static int ErrorTerm = 0;
 	public final static int FaultTerm = 1;
 	
 	public static GtSyntaxTree ParseFault(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree FaultTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "fault");
 		if(TokenContext.MatchToken("(")) {
 			FaultTree.SetMatchedPatternAt(ErrorTerm, NameSpace, TokenContext, "$Expression$", Required);
 			if(TokenContext.MatchToken(",")) {
 				FaultTree.SetMatchedPatternAt(FaultTerm, NameSpace, TokenContext, "$Variable$", Required);
 			}
 			FaultTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		}
 		return FaultTree;
 	}
 
 	public static GtNode TypeFault(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		GtFunc CreateFunc = (GtFunc)Gamma.NameSpace.GetSymbol("$CreateFaultBuiltInFunc");
 		GtNode ApplyNode = Gamma.Generator.CreateApplyNode(ContextType, ParsedTree, CreateFunc);
 		ApplyNode.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, CreateFunc));
 		ApplyNode.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, Gamma.NameSpace));
 		ApplyNode.Append(CreateDCaseNode(Gamma, ParsedTree));
 		String FaultInfo;
 		if(ParsedTree.HasNodeAt(FaultTerm)) {
 			FaultInfo = ParsedTree.GetSyntaxTreeAt(FaultTerm).KeyToken.ParsedText;
 		}
 		else {
 			FaultInfo = Gamma.NameSpace.GetSymbolText("AssumedFault");
 			if(FaultInfo == null) {
 				FaultInfo = "UnexpectedFault";
 			}
 		}
 		ApplyNode.Append(Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, FaultInfo));
 		/*local*/GtNode ErrorInfoNode = null;
 		if(ParsedTree.HasNodeAt(ErrorTerm)) {
 			ErrorInfoNode = ParsedTree.TypeCheckAt(ErrorTerm, Gamma, Gamma.StringType, DefaultTypeCheckPolicy);
 		}
 		else {
 			ErrorInfoNode = Gamma.Generator.CreateConstNode(Gamma.VarType, ParsedTree, DShellGrammar.GetErrorMessage());
 		}
 		ApplyNode.Append(ErrorInfoNode);
 		return ApplyNode;
 	}
 
 	static private final GtNode CreateConstNode(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, Object ConstValue) {
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ConstValue), ParsedTree, ConstValue);
 	}
 
 	// dexec CallAdmin() 
 	// D-exec Expression
 	// dexec FunctionName
 	public static GtSyntaxTree ParseDexec(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree Tree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "dexec");
 		Tree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$Variable$", Required);
 		return Tree;
 	}
 
 	// dexec FunctionName
 	// => ReportAction(FunctionName(), "FunctionName", CurrentFuncName, DCaseRevision)
 	public static GtNode TypeDexec(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/Object ConstValue = ParsedTree.NameSpace.GetSymbol("DCaseRevision");
 		if(ConstValue == null) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "constant variable DCaseRevision is not defined in this context");
 		}
 		/*local*/GtType DFaultType = (/*cast*/GtType) ParsedTree.NameSpace.GetSymbol("DFault");
 		if(DFaultType == null) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "DFault type is not defined in this context");
 		}
 
 		GtNode ApplyNode = KonohaGrammar.TypeApply(Gamma, ParsedTree, DFaultType);
 		if(ApplyNode.IsErrorNode()) {
 			return ApplyNode;
 		}
 
 		// create UpdateFaultInfomation(FunctionName(), "FunctionName", CurrentFuncName, DCaseRevision);
 		/*local*/GtNode Revision = DShellGrammar.CreateConstNode(Gamma, ParsedTree, ConstValue);
 		/*local*/String FunctionName = (/*cast*/String) ParsedTree.GetSyntaxTreeAt(UnaryTerm).KeyToken.ParsedText;
 		/*local*/String CurrentFuncName = Gamma.Func.GetNativeFuncName();
 
 		/*local*/GtNode FuncNameNode = DShellGrammar.CreateConstNode(Gamma, ParsedTree, FunctionName);
 		/*local*/GtNode CurFuncNameNode = DShellGrammar.CreateConstNode(Gamma, ParsedTree, CurrentFuncName);
 		
 		ConstValue = ParsedTree.NameSpace.GetSymbol("Location");
 		if(ConstValue == null || !(ConstValue instanceof String)) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "Location is not defined in this context");
 		}
 		/*local*/GtNode LocationNode = DShellGrammar.CreateConstNode(Gamma, ParsedTree, ConstValue);
 		ConstValue = ParsedTree.NameSpace.GetSymbol("RecServer");
 		if(ConstValue == null || !(ConstValue instanceof String)) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "RecServer is not defined in this context");
 		}
 		/*local*/GtNode RecServerNode = DShellGrammar.CreateConstNode(Gamma, ParsedTree, ConstValue);
 		ConstValue = ParsedTree.NameSpace.GetSymbol("ReportAction");
 		if(!(ConstValue instanceof GtFunc)) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "ReportAction is not defined in this context");
 		}
 		/*local*/GtFunc Func = (/*cast*/GtFunc) ConstValue;
 		/*local*/GtNode ApplyNode2 = Gamma.Generator.CreateApplyNode(Func.GetReturnType(), ParsedTree, Func);
 		ApplyNode2.Append(DShellGrammar.CreateConstNode(Gamma, ParsedTree, Func));
 		ApplyNode2.Append(ApplyNode);
 		ApplyNode2.Append(FuncNameNode);
 		ApplyNode2.Append(CurFuncNameNode);
 		ApplyNode2.Append(Revision);
 		ApplyNode2.Append(LocationNode);
 		ApplyNode2.Append(RecServerNode);
 		return ApplyNode2;
 	}
 
 	// Raise Expression
 	public static GtSyntaxTree ParseRaise(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ReturnTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "raise");
 		ReturnTree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$Type$", Required);
 		return ReturnTree;
 	}
 
 	public static GtNode TypeRaise(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		if(Gamma.IsTopLevel() || Gamma.Func == null) {
 			return Gamma.UnsupportedTopLevelError(ParsedTree);
 		}
 		/*local*/GtNode Expr = ParsedTree.TypeCheckAt(UnaryTerm, Gamma, Gamma.NameSpace.Context.TypeType, DefaultTypeCheckPolicy);
 		if(Expr instanceof GtConstNode && Expr.Type.IsTypeType()) {
 			/*local*/GtType ObjectType = (/*cast*/GtType)((/*cast*/GtConstNode)Expr).ConstValue;
 			Expr = Gamma.Generator.CreateNewNode(ObjectType, ParsedTree);
 			//Expr = KonohaGrammar.TypeApply(Gamma, ParsedTree, ObjectType);
 		}
 		return Gamma.Generator.CreateReturnNode(Expr.Type, ParsedTree, Expr);
 	}
 
 	public static DFault UpdateFaultInfomation(DFault Fault, String CalledFuncName, String CurrentFuncName, long DCaseRevision, String Location, String RecServer) {
 		if(Fault == null) {
 			return null;
 		}
 //		Fault.UpdateFaultInfomation(CalledFuncName, CurrentFuncName, DCaseRevision);
 		return Fault;
 	}
 
 //ifdef JAVA
 	// this is a new interface used in ImportNativeObject
 	public static void ImportGrammar(GtNameSpace NameSpace, Class<?> GrammarClass) {
 		/*local*/GtParserContext ParserContext = NameSpace.Context;
 		NameSpace.AppendSyntax("letenv", LoadParseFunc2(ParserContext, GrammarClass, "ParseEnv"), null);
 		NameSpace.AppendSyntax("command", LoadParseFunc2(ParserContext, GrammarClass, "ParseCommand"), null);
 		NameSpace.AppendSyntax("-", LoadParseFunc2(ParserContext, GrammarClass, "ParseFileOperator"), LoadTypeFunc2(ParserContext, GrammarClass, "TypeFileOperator"));
 //		NameSpace.AppendSyntax("$DShell$", LoadParseFunc2(ParserContext, GrammarClass, "ParseDShell"), LoadTypeFunc2(ParserContext, GrammarClass, "TypeDShell"));
 		NameSpace.AppendSyntax("$FilePath$", LoadParseFunc2(ParserContext, GrammarClass, "ParseFilePath"), null);
 		NameSpace.AppendSyntax("$DShell2$", LoadParseFunc2(ParserContext, GrammarClass, "ParseDShell2"), LoadTypeFunc2(ParserContext, GrammarClass, "TypeDShell2"));
 		NameSpace.AppendSyntax("shell", LoadParseFunc2(ParserContext, GrammarClass, "ParseShell"), null);
 
 		NameSpace.SetSymbol("$CreateFaultBuiltInFunc", LibGreenTea.ImportNativeObject(NameSpace, "DShellGrammar.CreateFault"), null);
 		NameSpace.SetSymbol("$ReportBuiltInFunc", LibGreenTea.ImportNativeObject(NameSpace, "DShellGrammar.ExecAction"), null);
 		NameSpace.AppendSyntax("dlog", LoadParseFunc2(ParserContext, GrammarClass, "ParseDLog"), LoadTypeFunc2(ParserContext, GrammarClass, "TypeDLog"));
 		NameSpace.AppendSyntax("fault", LoadParseFunc2(ParserContext, GrammarClass, "ParseFault"), LoadTypeFunc2(ParserContext, GrammarClass, "TypeFault"));
 		
 		NameSpace.AppendSyntax("raise", LoadParseFunc2(ParserContext, GrammarClass, "ParseRaise"), LoadTypeFunc2(ParserContext, GrammarClass, "TypeRaise"));
 		NameSpace.AppendSyntax("dexec", LoadParseFunc2(ParserContext, GrammarClass, "ParseDexec"), LoadTypeFunc2(ParserContext, GrammarClass, "TypeDexec"));
 
 	}
 //endif VAJA
 }
