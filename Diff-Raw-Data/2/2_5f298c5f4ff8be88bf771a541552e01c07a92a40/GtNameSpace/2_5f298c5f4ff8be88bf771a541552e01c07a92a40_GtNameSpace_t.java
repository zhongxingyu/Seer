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
 
 public final class GtNameSpace extends GreenTeaUtils {
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
 
 	public GtNameSpace CreateSubNameSpace() {
 		return new GtNameSpace(this.Context, this);
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
 
 	public final GtNameSpace Minimum() {
 		GtNameSpace NameSpace = this;
		while(NameSpace.SymbolPatternTable == null) {
 			NameSpace = NameSpace.ParentNameSpace;
 		}
 		return NameSpace;
 	}
 
 	public final Object GetLocalUndefinedSymbol(String Key) {
 		if(this.SymbolPatternTable != null) {
 			return this.SymbolPatternTable.GetOrNull(Key);
 		}
 		return null;
 	}
 
 	public final Object GetLocalSymbol(String Key) {
 		if(this.SymbolPatternTable != null) {
 			/*local*/Object Value = this.SymbolPatternTable.GetOrNull(Key);
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
 				/*local*/Object Value = NameSpace.SymbolPatternTable.GetOrNull(Key);
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
 			/*local*/Object OldValue = this.SymbolPatternTable.GetOrNull(Key);
 			if(OldValue != null && OldValue != UndefinedSymbol) {
 				if(LibGreenTea.DebugMode) {
 					this.Context.ReportError(WarningLevel, SourceToken, "duplicated symbol: " + SourceToken + " old, new =" + OldValue + ", " + Value);
 				}
 				else {
 					if(!LibGreenTea.EqualsString(Key, "_")) {
 						this.Context.ReportError(WarningLevel, SourceToken, "duplicated symbol: " + SourceToken);
 					}
 				}
 			}
 		}
 		this.SymbolPatternTable.put(Key, Value);
 		LibGreenTea.VerboseLog(VerboseSymbol, "symbol: " + Key + ", " + Value);
 	}
 
 	public final void SetUndefinedSymbol(String Symbol, GtToken SourceToken) {
 		this.SetSymbol(Symbol, UndefinedSymbol, SourceToken);
 	}
 
 	public final String GetSymbolText(String Key) {
 		/*local*/Object Body = this.GetSymbol(Key);
 		if(Body instanceof String) {
 			return (/*cast*/String)Body;
 		}
 		return null;
 	}
 
 	public GtSyntaxPattern GetSyntaxPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol(PatternName);
 		if(Body instanceof GtSyntaxPattern) {
 			return (/*cast*/GtSyntaxPattern)Body;
 		}
 		return null;
 	}
 
 	public GtSyntaxPattern GetExtendedSyntaxPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol(ExtendedPatternSymbol(PatternName));
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
 		this.AppendSyntaxPattern(ExtendedPatternSymbol(Name), Pattern, null);
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
 		if(Type.BaseType == Type) {
 			this.SetSymbol(Type.ShortName, Type, SourceToken);
 		}
 		return Type;
 	}
 
 	public final GtType AppendTypeVariable(String Name, GtType ParamBaseType, GtToken SourceToken, ArrayList<Object> RevertList) {
 		this.UpdateRevertList(Name, RevertList);
 		/*local*/GtType TypeVar = new GtType(TypeVariable, Name, ParamBaseType, null);
 		this.SetSymbol(Name, TypeVar, SourceToken);
 		return TypeVar;
 	}
 
 	public final Object GetClassSymbol(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		while(ClassType != null) {
 			/*local*/String Key = ClassSymbol(ClassType, Symbol);
 			/*local*/Object Value = this.GetSymbol(Key);
 			if(Value != null) {
 				return Value;
 			}
 //			if(ClassType.IsDynamicNaitiveLoading() & this.Context.RootNameSpace.GetLocalUndefinedSymbol(Key) == null) {
 //				Value = LibGreenTea.LoadNativeStaticFieldValue(ClassType, Symbol.substring(1));
 //				if(Value != null) {
 //					return Value;
 //				}
 //				//LibGreenTea.LoadNativeMethods(ClassType, Symbol, FuncList);
 //			}
 			if(!RecursiveSearch) {
 				break;
 			}
 			ClassType = ClassType.ParentMethodSearch;
 		}
 		return null;
 	}
 
 	public final Object GetClassStaticSymbol(GtType StaticClassType, String Symbol, boolean RecursiveSearch) {
 		/*local*/String Key = null;
 		/*local*/GtType ClassType = StaticClassType;
 		while(ClassType != null) {
 			Key = ClassStaticSymbol(ClassType, Symbol);
 			/*local*/Object Value = this.GetSymbol(Key);
 			if(Value != null) {
 				return Value;
 			}
 			if(!RecursiveSearch) {
 				break;
 			}
 			ClassType = ClassType.SuperType;
 		}
 		Key = ClassStaticSymbol(StaticClassType, Symbol);
 		if(StaticClassType.IsDynamicNaitiveLoading() && this.Context.RootNameSpace.GetLocalUndefinedSymbol(Key) == null) {
 			/*local*/Object Value = LibGreenTea.LoadNativeStaticFieldValue(this.Context, StaticClassType, Symbol);
 			if(Value == null) {
 				this.Context.RootNameSpace.SetUndefinedSymbol(Key, null);
 			}
 			else {
 				this.Context.RootNameSpace.SetSymbol(Key, Value, null);
 			}
 			return Value;
 		}
 		return null;
 	}
 	
 //	public final void ImportClassSymbol(GtNameSpace NameSpace, String Prefix, GtType ClassType, GtToken SourceToken) {
 //		/*local*/String ClassPrefix = ClassSymbol(ClassType, ClassStaticName(""));
 //		/*local*/ArrayList<String> KeyList = new ArrayList<String>();
 //		/*local*/GtNameSpace ns = NameSpace;
 //		while(ns != null) {
 //			if(ns.SymbolPatternTable != null) {
 //				LibGreenTea.RetrieveMapKeys(ns.SymbolPatternTable, ClassPrefix, KeyList);
 //			}
 //			ns = ns.ParentNameSpace;
 //		}
 //		/*local*/int i = 0;
 //		while(i < KeyList.size()) {
 //			/*local*/String Key = KeyList.get(i);
 //			/*local*/Object Value = NameSpace.GetSymbol(Key);
 //			Key = Key.replace(ClassPrefix, Prefix);
 //			if(SourceToken != null) {
 //				SourceToken.ParsedText = Key;
 //			}
 //			this.SetSymbol(Key, Value, SourceToken);
 //			i = i + 1;
 //		}
 //	}
 
 	public final GtFunc GetGetterFunc(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		/*local*/Object Func = this.Context.RootNameSpace.GetClassSymbol(ClassType, GetterSymbol(Symbol), RecursiveSearch);
 		if(Func instanceof GtFunc) {
 			return (/*cast*/GtFunc)Func;
 		}
 		Func = this.Context.RootNameSpace.GetLocalUndefinedSymbol(ClassSymbol(ClassType, GetterSymbol(Symbol)));
 		if(ClassType.IsDynamicNaitiveLoading() && Func == null) {
 			return LibGreenTea.LoadNativeField(this.Context, ClassType, Symbol, false);
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
 			return LibGreenTea.LoadNativeField(this.Context, ClassType, Symbol, true);
 		}
 		return null;
 	}
 
 	public final GtFunc GetConverterFunc(GtType FromType, GtType ToType, boolean RecursiveSearch) {
 		/*local*/Object Func = this.GetClassSymbol(FromType, ConverterSymbol(ToType), RecursiveSearch);
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
 					LibGreenTea.LoadNativeConstructors(this.Context, ClassType, FuncList);
 				}
 				else {
 					LibGreenTea.LoadNativeMethods(this.Context, ClassType, Symbol, FuncList);
 				}
 			}
 			if(!RecursiveSearch) {
 				break;
 			}
 			//System.err.println("** " + ClassType + ", " + ClassType.ParentMethodSearch);
 			ClassType = ClassType.ParentMethodSearch;
 		}
 		return new GtPolyFunc(FuncList);
 	}
 
 	public final GtPolyFunc GetConstructorFunc(GtType ClassType) {
 		return this.Context.RootNameSpace.GetMethod(ClassType, ConstructorSymbol(), false);
 	}
 
 	public final GtFunc GetOverridedMethod(GtType ClassType, GtFunc GivenFunc) {
 		/*local*/String Symbol = FuncSymbol(GivenFunc.FuncName);
 		/*local*/GtType GivenClassType = GivenFunc.GetRecvType();
 		if(ClassType != GivenClassType) {
 			/*local*/ArrayList<GtFunc> FuncList = new ArrayList<GtFunc>();
 			while(ClassType != null) {
 				/*local*/String Key = ClassSymbol(ClassType, Symbol);
 				this.RetrieveFuncList(Key, FuncList);
 				/*local*/int i = 0;
 				while(i < FuncList.size()) {
 					/*local*/GtFunc Func = FuncList.get(i); 
 					i += 1;
 					if(Func.EqualsOverridedMethod(GivenFunc)) {
 						return Func;
 					}
 				}
 				FuncList.clear();
 				ClassType = ClassType.ParentMethodSearch;
 			}
 		}
 		return GivenFunc;
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
 
 	public final GtPolyFunc GetPolyFunc(String FuncName) {
 		/*local*/ArrayList<GtFunc> FuncList = new ArrayList<GtFunc>();
 		this.RetrieveFuncList(FuncName, FuncList);
 		return new GtPolyFunc(FuncList);
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
 				PolyFunc.Append(this.Context, OldFunc, SourceToken);
 				PolyFunc.Append(this.Context, Func, SourceToken);
 				this.SetSymbol(Key, PolyFunc, null);
 				return PolyFunc;
 			}
 			// error
 		}
 		else if(OldValue instanceof GtPolyFunc) {
 			/*local*/GtPolyFunc PolyFunc = (/*cast*/GtPolyFunc)OldValue;
 			PolyFunc.Append(this.Context, Func, SourceToken);
 			return PolyFunc;
 		}
 		this.SetSymbol(Key, Func, SourceToken);
 		return OldValue;
 	}
 
 	public final Object AppendFunc(GtFunc Func, GtToken SourceToken) {
 		return this.AppendFuncName(Func.FuncName, Func, SourceToken);
 	}
 
 	public final Object AppendStaticFunc(GtType StaticType, GtFunc Func, GtToken SourceToken) {
 		int loc = Func.FuncName.lastIndexOf(".");
 		return this.AppendFuncName(ClassStaticSymbol(StaticType, Func.FuncName.substring(loc+1)), Func, SourceToken);
 	}
 
 	public final Object AppendMethod(GtFunc Func, GtToken SourceToken) {
 		/*local*/GtType ClassType = Func.GetRecvType();
 		if(ClassType.IsGenericType() && ClassType.HasTypeVariable()) {
 			ClassType = ClassType.BaseType;
 		}
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
 			ClassType = Func.GetFuncParamType(0);
 		}
 		if(ToType == null) {
 			ToType = Func.GetReturnType();
 		}
 		/*local*/String Key = ClassSymbol(ClassType, ConverterSymbol(ToType));
 		LibGreenTea.Assert(Func.Is(ConverterFunc));		
 		this.SetSymbol(Key, Func, SourceToken);
 	}
 	
 	final Object EvalWithErrorInfo(String ScriptText, long FileLine) {
 		/*local*/Object ResultValue = null;
 		LibGreenTea.VerboseLog(VerboseEval, "eval: " + ScriptText);
 		/*local*/GtTokenContext TokenContext = new GtTokenContext(this, ScriptText, FileLine);
 		this.Context.Generator.StartCompilationUnit();
 		TokenContext.SkipEmptyStatement();
 		while(TokenContext.HasNext()) {
 			/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 			TokenContext.ParseFlag = 0; // init
 			//System.err.println("** TokenContext.Position=" + TokenContext.CurrentPosition + ", " + TokenContext.IsAllowedBackTrack());
 			/*local*/GtSyntaxTree TopLevelTree = TokenContext.ParsePattern(this, "$Expression$", Required);
 			TokenContext.SkipEmptyStatement();			
 			if(TopLevelTree.IsError() && TokenContext.HasNext()) {
 				/*local*/GtToken Token = TokenContext.GetToken();
 				this.Context.ReportError(InfoLevel, TokenContext.GetToken(), "stopping script eval at " + Token.ParsedText);
 				ResultValue = TopLevelTree.KeyToken;  // in case of error, return error token
 				break;
 			}
 			if(TopLevelTree.IsValidSyntax()) {
 				TopLevelTree.SetAnnotation(Annotation);
 				/*local*/GtTypeEnv Gamma = new GtTypeEnv(this);
 				/*local*/GtNode Node = TopLevelTree.TypeCheck(Gamma, GtStaticTable.VoidType, DefaultTypeCheckPolicy);
 				ResultValue = Node.ToConstValue(this.Context, true/*EnforceConst*/);
 			}
 			TokenContext.Vacume();
 		}
 		this.Context.Generator.FinishCompilationUnit();
 		return ResultValue;
 	}
 
 	public final Object Eval(String ScriptText, long FileLine) {
 		/*local*/Object ResultValue = this.EvalWithErrorInfo(ScriptText, FileLine);
 		if(ResultValue instanceof GtToken && ((/*cast*/GtToken)ResultValue).IsError()) {
 			return null;
 		}
 		return ResultValue;
 	}
 
 	public final boolean Load(String ScriptText, long FileLine) {
 		/*local*/Object Token = this.EvalWithErrorInfo(ScriptText, FileLine);
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
 
 	private void UpdateRevertList(String Key, ArrayList<Object> RevertList) {
 		/*local*/Object Value = this.GetLocalSymbol(Key);
 		RevertList.add(Key);
 		if(Value != null) {
 			RevertList.add(Value);
 		}
 		else {
 			RevertList.add(UndefinedSymbol);
 		}
 	}
 
 	public void Revert(ArrayList<Object> RevertList) {
 		/*local*/int i = 0;
 		while(i < RevertList.size()) {
 			/*local*/String Key = (/*cast*/String)RevertList.get(i);
 			/*local*/Object Value = RevertList.get(i+1);
 			this.SetSymbol(Key, Value, null);
 			i += 2;
 		}
 	}
 
 
 }
