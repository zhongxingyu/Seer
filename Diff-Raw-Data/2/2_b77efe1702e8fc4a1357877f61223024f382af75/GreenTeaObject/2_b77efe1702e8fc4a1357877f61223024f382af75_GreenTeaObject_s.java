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
 import java.util.Arrays;
 //endif VAJA
 
 class GtType extends GreenTeaUtils {
 	/*field*/public final GtParserContext	Context;
 	/*field*/public GtNameSpace     PackageNameSpace;
 	/*field*/int					ClassFlag;
 	/*field*/int                    ClassId;
 	/*field*/public String			ShortClassName;
 	/*field*/GtType					SuperType;
 	/*field*/public GtType			SearchSuperFuncClass;
 	/*field*/GtType					BaseType;
 	/*field*/GtType[]				TypeParams;
 	/*field*/public Object          TypeBody;
 	/*field*/public Object			DefaultNullValue;
 
 	GtType/*constructor*/(GtParserContext Context, int ClassFlag, String ClassName, Object DefaultNullValue, Object TypeBody) {
 		this.Context = Context;
 		this.ClassFlag = ClassFlag;
 		this.ShortClassName = ClassName;
 		this.SuperType = null;
 		this.BaseType = this;
 		this.SearchSuperFuncClass = null;
 		this.DefaultNullValue = DefaultNullValue;
 		this.TypeBody = TypeBody;
 		if(!IsFlag(ClassFlag, TypeParameter)) {
 			this.ClassId = Context.ClassCount;
 			Context.ClassCount += 1;
 		}
 		this.TypeParams = null;
 	}
 
 	public GtType CreateSubType(int ClassFlag, String ClassName, Object DefaultNullValue, Object NativeSpec) {
 		/*local*/GtType SubType = new GtType(this.Context, ClassFlag, ClassName, DefaultNullValue, NativeSpec);
 		SubType.SuperType = this;
 		SubType.SearchSuperFuncClass = this;
 		return SubType;
 	}
 
 	// Note Don't call this directly. Use Context.GetGenericType instead.
 	public GtType CreateGenericType(int BaseIndex, ArrayList<GtType> TypeList, String ShortName) {
 		/*local*/GtType GenericType = new GtType(this.Context, this.ClassFlag, ShortName, null, null);
 		GenericType.BaseType = this.BaseType;
 		GenericType.SearchSuperFuncClass = this.BaseType;
 		GenericType.SuperType = this.SuperType;
 		GenericType.TypeParams = LibGreenTea.CompactTypeList(BaseIndex, TypeList);
 		LibGreenTea.VerboseLog(VerboseType, "new class: " + GenericType.ShortClassName + ", ClassId=" + GenericType.ClassId);
 		return GenericType;
 	}
 
 	public final boolean IsAbstract() {
 		return (this.TypeBody == null && this.SuperType == this.Context.StructType/*default*/);
 	}
 
 	public final boolean IsNative() {
 		return IsFlag(this.ClassFlag, NativeType);
 	}
 
 	public final boolean IsDynamic() {
 		return IsFlag(this.ClassFlag, DynamicType);
 	}
 
 	public final boolean IsGenericType() {
 		return (this.TypeParams != null);
 	}
 
 	@Override public String toString() {
 		return this.ShortClassName;
 	}
 
 	public final String GetNativeName() {
 		if(IsFlag(this.ClassFlag, ExportType)) {
 			return this.ShortClassName;
 		}
 		else {
 			return this.BaseType.ShortClassName + NativeNameSuffix + this.ClassId;
 		}
 	}
 	
 	public final String GetUniqueName() {
 		if(IsFlag(this.ClassFlag, TypeParameter)) {
 			return this.ShortClassName;
 		}
 		else {
 			if(LibGreenTea.DebugMode) {
 				return this.BaseType.ShortClassName + NativeNameSuffix + this.ClassId;
 			}
 			else {
 				return NativeNameSuffix + this.ClassId;
 			}
 		}
 	}
 
 	public final boolean Accept(GtType Type) {
 		if(this == Type/* || this == this.Context.AnyType*/) {
 			return true;
 		}
 		/*local*/GtType SuperClass = this.SuperType;
 		while(SuperClass != null) {
 			if(SuperClass == Type) {
 				return true;
 			}
 			SuperClass = SuperClass.SuperType;
 		}
 		return this.Context.CheckSubType(Type, this);
 	}
 
 	public final boolean AcceptValue(Object Value) {
 		return (Value != null) ? this.Accept(this.Context.GuessType(Value)) : true;
 	}
 
 	public void SetClassField(GtClassField ClassField) {
 		this.TypeBody = ClassField;
 	}
 
 	public final boolean IsFuncType() {
 		return (this.BaseType == this.Context.FuncType);
 	}
 
 	public final boolean IsVoidType() {
 		return (this == this.Context.VoidType);
 	}
 
 	public final boolean IsVarType() {
 		return (this == this.Context.VarType);
 	}
 
 	public final boolean IsAnyType() {
 		return (this == this.Context.AnyType);
 	}
 
 	public final boolean IsTypeType() {
 		return (this == this.Context.TypeType);
 	}
 
 	public final boolean IsBooleanType() {
 		return (this == this.Context.BooleanType);
 	}
 
 	public final boolean IsStringType() {
 		return (this == this.Context.StringType);
 	}
 
 	public final boolean IsArrayType() {
 		return (this == this.Context.ArrayType);
 	}
 
 	public final boolean IsEnumType() {
 		return IsFlag(this.ClassFlag, EnumType);
 	}
 
 	public final boolean IsTypeParam() {
 		return IsFlag(this.ClassFlag, TypeParameter);
 	}
 
 	public GtType RealType(GtTypeEnv Gamma) {
 		if(this.IsTypeParam()) {
 			return null;
 		}
 		return this;
 	}
 
 	public boolean IsDynamicNaitiveLoading() {
 		return this.IsNative() && !IsFlag(this.ClassFlag, CommonType);
 	}
 }
 
 class GtFuncBlock extends GreenTeaUtils {
 	/*field*/public GtNameSpace       NameSpace;
 	/*field*/public ArrayList<String> NameList;
 	/*field*/public GtSyntaxTree FuncBlock;
 	/*field*/public boolean IsVarArgument;
 	/*field*/public ArrayList<GtType> TypeList;
 	/*field*/public GtFunc DefinedFunc;
 	
 	GtFuncBlock/*constructor*/(GtNameSpace NameSpace, ArrayList<GtType> TypeList) {
 		this.NameSpace = NameSpace;
 		this.TypeList = TypeList;
 		this.NameList = new ArrayList<String>();
 		this.FuncBlock = null;
 		this.IsVarArgument = false;
 		this.DefinedFunc = null;
 	}
 	
 	void SetThisIfInClass(GtType Type) {
 		if(Type != null) {
 			this.TypeList.add(Type);
 			this.NameList.add(this.NameSpace.Context.Generator.GetRecvName());
 		}
 	}
 
 	void SetConverterType() {
 		this.TypeList.add(this.NameSpace.Context.TypeType);
 		this.NameList.add("type");
 	}
 	
 	void AddParameter(GtType Type, String Name) {
 		this.TypeList.add(Type);
 		if(Type.IsVarType()) {
 			this.IsVarArgument = true;
 		}
 		this.NameList.add(Name);
 	}
 }
 
 class GtFunc extends GreenTeaUtils {
 	/*field*/public int				FuncFlag;
 	/*field*/public String			FuncName;
 	/*field*/public String          MangledName;
 	/*field*/public GtType[]		Types;
 	/*field*/public GtType          FuncType;
 	/*field*/public                 int FuncId;
 	/*field*/public Object          NativeRef;  // Abstract function if null
 	/*field*/public String[]        GenericParam;
 
 	GtFunc/*constructor*/(int FuncFlag, String FuncName, int BaseIndex, ArrayList<GtType> ParamList) {
 		this.FuncFlag = FuncFlag;
 		this.FuncName = FuncName;
 		this.Types = LibGreenTea.CompactTypeList(BaseIndex, ParamList);
 		LibGreenTea.Assert(this.Types.length > 0);
 		this.FuncType = null;
 		this.NativeRef = null;
 		/*local*/GtParserContext Context = this.GetContext();
 		this.FuncId = Context.FuncCount;
 		Context.FuncCount += 1;
 		this.MangledName = FuncName + NativeNameSuffix + this.FuncId;
 	}
 
 	public final GtParserContext GetContext() {
 		return this.GetReturnType().Context;
 	}
 
 	public final String GetNativeFuncName() {
 		if(this.Is(ExportFunc)) {
 			return this.FuncName;
 		}
 		else {
 			return this.MangledName;
 		}
 	}
 
 	public final GtType GetFuncType() {
 		if(this.FuncType == null) {
 			/*local*/GtParserContext Context = this.GetRecvType().Context;
 			this.FuncType = Context.GetGenericType(Context.FuncType, 0, new ArrayList<GtType>(Arrays.asList(this.Types)), true);
 		}
 		return this.FuncType;
 	}
 
 	@Override public String toString() {
 		/*local*/String s = this.FuncName + "(";
 		/*local*/int i = 0;
 		while(i < this.GetFuncParamSize()) {
 			/*local*/GtType ParamType = this.GetFuncParamType(i);
 			if(i > 0) {
 				s += ", ";
 			}
 			s += ParamType;
 			i += 1;
 		}
 		return s + ") : " + this.GetReturnType();
 	}
 
 	public boolean Is(int Flag) {
 		return IsFlag(this.FuncFlag, Flag);
 	}
 
 	public final GtType GetReturnType() {
 		return this.Types[0];
 	}
 
 	public final void SetReturnType(GtType ReturnType) {
 		LibGreenTea.Assert(this.GetReturnType().IsVarType());
 		this.Types[0] = ReturnType;
 		this.FuncType = null; // reset
 	}
 
 	public final GtType GetRecvType() {
 		if(this.Types.length == 1) {
 			return this.Types[0].Context.VoidType;
 		}
 		return this.Types[1];
 	}
 
 	public final int GetFuncParamSize() {
 		return this.Types.length - 1;
 	}
 
 	public final GtType GetFuncParamType(int ParamIdx) {
 		return this.Types[ParamIdx+1];
 	}
 
 	public final int GetMethodParamSize() {
 		return this.Types.length - 2;
 	}
 
	public final boolean EqualsParamTypes(int BaseIndex, GtType[] ParamTypes) {
 		if(this.Types.length == ParamTypes.length) {
 			/*local*/int i = BaseIndex;
 			while(i < this.Types.length) {
 				if(this.Types[i] != ParamTypes[i]) {
 					return false;
 				}
 				i = i + 1;
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public final boolean EqualsType(GtFunc AFunc) {
 		return this.EqualsParamTypes(0, AFunc.Types);
 	}
 
 	public final boolean IsAbstract() {
 		return this.NativeRef == null;
 	}
 
 	public final void SetNativeMacro(String NativeMacro) {
 		LibGreenTea.Assert(this.NativeRef == null);
 		this.FuncFlag |= NativeMacroFunc;
 		this.NativeRef = NativeMacro;
 	}
 
 	public final String GetNativeMacro() {
 		return (/*cast*/String)this.NativeRef;
 	}
 
 	public final void SetNativeMethod(int OptionalFuncFlag, Object Method) {
 //		LibGreenTea.Assert(this.NativeRef == null);
 		this.FuncFlag |= NativeFunc | OptionalFuncFlag;
 		this.NativeRef = Method;
 	}
 
 	private boolean HasStaticBlock() {
 		if(this.NativeRef instanceof GtFuncBlock) {
 			GtFuncBlock FuncBlock = (/*cast*/GtFuncBlock)this.NativeRef;
 			return !FuncBlock.IsVarArgument;
 		}
 		return false;
 	}
 
 	public void GenerateNativeFunc() {
 		if(this.HasStaticBlock()) {
 			/*local*/GtFuncBlock FuncBlock = (/*cast*/GtFuncBlock)this.NativeRef;
 			/*local*/GtTypeEnv Gamma = new GtTypeEnv(FuncBlock.NameSpace);
 			/*local*/int i = 0;
 			/*local*/ArrayList<String> NameList = new ArrayList<String>();
 			while(i <  FuncBlock.NameList.size()) {
 				/*local*/GtVariableInfo VarInfo = Gamma.AppendDeclaredVariable(0, FuncBlock.DefinedFunc.Types[i+1], FuncBlock.NameList.get(i), null, null);
 				NameList.add(VarInfo.NativeName);
 				i = i + 1;
 			}
 			Gamma.Func = FuncBlock.DefinedFunc;
 			/*local*/GtNode BodyNode = GreenTeaUtils.TypeBlock(Gamma, FuncBlock.FuncBlock, Gamma.VoidType);
 			/*local*/String FuncName = FuncBlock.DefinedFunc.GetNativeFuncName();
 			Gamma.Generator.GenerateFunc(FuncBlock.DefinedFunc, NameList, BodyNode);
 			if(FuncName.equals("main")) {
 				Gamma.Generator.InvokeMainFunc(FuncName);
 			}
 		}
 	}
 
 	public boolean HasLazyBlock() {
 		if(this.NativeRef instanceof GtFuncBlock) {
 			GtFuncBlock FuncBlock = (/*cast*/GtFuncBlock)this.NativeRef;
 			return FuncBlock.IsVarArgument;
 		}
 		return false;
 	}
 	
 	public GtFunc GenerateLazyFunc(ArrayList<GtNode> NodeList) {
 		return null; // TODO
 	}
 
 }
 
 class GtPolyFunc extends GreenTeaUtils {
 	/*field*/public ArrayList<GtFunc> FuncList;
 
 	GtPolyFunc/*constructor*/(ArrayList<GtFunc> FuncList) {
 		this.FuncList = FuncList == null ? new ArrayList<GtFunc>() : FuncList;
 	}
 
 	@Override public String toString() { // this is used in an error message
 		/*local*/String s = "";
 		/*local*/int i = 0;
 		while(i < this.FuncList.size()) {
 			if(i > 0) {
 				s = s + " ";
 			}
 			s = s + this.FuncList.get(i);
 			i = i + 1;
 		}
 		return s;
 	}
 
 	public final void Append(GtFunc Func, GtToken SourceToken) {
 		if(SourceToken != null) {
 			/*local*/int i = 0;
 			while(i < this.FuncList.size()) {
 				/*local*/GtFunc ListedFunc = this.FuncList.get(i);
 				if(ListedFunc == Func) {
 					return; /* same function */
 				}
 				if(Func.EqualsType(ListedFunc)) {
 					Func.GetContext().ReportError(WarningLevel, SourceToken, "duplicated symbol" + SourceToken.ParsedText);
 					break;
 				}
 				i = i + 1;
 			}
 		}
 		this.FuncList.add(Func);
 	}
 
 	public GtFunc ResolveUnaryFunc(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtNode ExprNode) {
 		/*local*/int i = this.FuncList.size() - 1;
 		while(i >= 0) {
 			/*local*/GtFunc Func = this.FuncList.get(i);
 			if(Func.GetFuncParamSize() == 1 && Func.Types[1].Accept(ExprNode.Type)) {
 				return Func;
 			}
 			i = i - 1;
 		}
 		return null;
 	}
 
 	public final GtFunc IncrementalMatch(int FuncParamSize, ArrayList<GtNode> NodeList) {
 		/*local*/GtFunc ResolvedFunc = null;
 		/*local*/int i = 0;
 		while(i < this.FuncList.size()) {
 			/*local*/GtFunc Func = this.FuncList.get(i);
 			if(Func.GetFuncParamSize() == FuncParamSize) {
 				/*local*/int p = 0;
 				while(p < NodeList.size()) {
 					/*local*/GtNode Node = NodeList.get(p);
 					if(!Func.Types[p + 1].Accept(Node.Type)) {
 						Func = null;
 						break;
 					}
 					p = p + 1;
 				}
 				if(Func != null) {
 					if(ResolvedFunc != null) {
 						return null; // two more func
 					}
 					ResolvedFunc = Func;
 				}
 			}
 			i = i + 1;
 		}
 		return ResolvedFunc;
 	}
 
 	public GtFunc MatchAcceptableFunc(GtTypeEnv Gamma, int FuncParamSize, ArrayList<GtNode> NodeList) {
 		/*local*/int i = 0;
 		while(i < this.FuncList.size()) {
 			/*local*/GtFunc Func = this.FuncList.get(i);
 			if(Func.GetFuncParamSize() == FuncParamSize) {
 				/*local*/int p = 0;
 				/*local*/GtNode[] Coercions = null;
 				while(p < NodeList.size()) {
 					/*local*/GtType ParamType = Func.Types[p + 1];
 					/*local*/GtNode Node = NodeList.get(p);
 					if(ParamType.Accept(Node.Type)) {
 						p = p + 1;
 						continue;
 					}
 					/*local*/GtFunc TypeCoercion = Gamma.NameSpace.GetConverterFunc(Node.Type, ParamType, true);
 					if(TypeCoercion != null && TypeCoercion.Is(CoercionFunc)) {
 						if(Coercions == null) {
 							Coercions = new GtNode[NodeList.size()];
 						}
 						Coercions[p] = Gamma.CreateCoercionNode(ParamType, TypeCoercion, Node);
 						p = p + 1;
 						continue;
 					}
 					Func = null;
 					Coercions = null;
 					break;
 				}
 				if(Func != null) {
 					if(Coercions != null) {
 						i = 1;
 						while(i < Coercions.length) {
 							if(Coercions[i] != null) {
 								NodeList.set(i, Coercions[i]);
 							}
 							i = i + 1;
 						}
 						Coercions = null;
 					}
 					return Func;
 				}
 			}
 			i = i + 1;
 		}
 		return null;
 	}
 
 	public GtFunc ResolveFunc(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, int TreeIndex, ArrayList<GtNode> NodeList) {
 		/*local*/int FuncParamSize = LibGreenTea.ListSize(ParsedTree.SubTreeList) - TreeIndex + NodeList.size();
 		//System.err.println("*** FuncParamSize=" + FuncParamSize + "resolved_size=" + NodeList.size());
 		//System.err.println("*** FuncList=" + this);
 		
 		/*local*/GtFunc ResolvedFunc = this.IncrementalMatch(FuncParamSize, NodeList);
 		while(ResolvedFunc == null && TreeIndex < LibGreenTea.ListSize(ParsedTree.SubTreeList)) {
 			/*local*/GtNode Node = ParsedTree.TypeCheckAt(TreeIndex, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 			GreenTeaUtils.AppendTypedNode(NodeList, Node);
 			TreeIndex = TreeIndex + 1;
 			ResolvedFunc = this.IncrementalMatch(FuncParamSize, NodeList);
 		}
 		if(ResolvedFunc != null) {
 			while(TreeIndex < LibGreenTea.ListSize(ParsedTree.SubTreeList)) {
 				/*local*/GtType ContextType = ResolvedFunc.GetFuncParamType(NodeList.size()/*ResolvedSize*/);
 				/*local*/GtNode Node = ParsedTree.TypeCheckAt(TreeIndex, Gamma, ContextType, DefaultTypeCheckPolicy);
 				GreenTeaUtils.AppendTypedNode(NodeList, Node);
 				TreeIndex = TreeIndex + 1;
 			}
 			return ResolvedFunc;			
 		}
 		return this.MatchAcceptableFunc(Gamma, FuncParamSize, NodeList);
 	}
 
 	public GtFunc ResolveConstructor(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, int TreeIndex, ArrayList<GtNode> NodeList) {
 		/*local*/int FuncParamSize = LibGreenTea.ListSize(ParsedTree.SubTreeList) - TreeIndex + NodeList.size();
 //		System.err.println("*** FuncParamSize=" + FuncParamSize + " resolved_size=" + NodeList.size());
 //		System.err.println("*** FuncList=" + this);
 		GtFunc ResolvedFunc = this.ResolveFunc(Gamma, ParsedTree, TreeIndex, NodeList);
 		if(ResolvedFunc == null  && FuncParamSize == 1) {
 			
 		}
 		return ResolvedFunc;
 	}
 
 }
 
 public interface GreenTeaObject {
 	GtType GetGreenType();
 }
 
 class GreenTeaTopObject implements GreenTeaObject {
 	/*field*/public GtType GreenType;
 	GreenTeaTopObject/*constructor*/(GtType GreenType) {
 		this.GreenType = GreenType;
 	}
 	public final GtType GetGreenType() {
 		return this.GreenType;
 	}
 }
 
 final class GreenTeaAnyObject extends GreenTeaTopObject {
 	/*field*/public final Object NativeValue;
 	GreenTeaAnyObject/*constructor*/(GtType GreenType, Object NativeValue) {
 		super(GreenType);
 		this.NativeValue = NativeValue;
 	}
 }
 
 class GreenTeaArray extends GreenTeaTopObject {
 	GreenTeaArray/*constructor*/(GtType GreenType) {
 		super(GreenType);
 	}
 }
 
 class GreenTeaEnum extends GreenTeaTopObject {
 	/*field*/public final long EnumValue;
 	/*field*/public final String EnumSymbol;
 	GreenTeaEnum/*constructor*/(GtType GreenType, long EnumValue, String EnumSymbol) {
 		super(GreenType);
 		this.EnumValue = EnumValue;
 		this.EnumSymbol = EnumSymbol;
 	}
 
 	@Override public String toString() {
 		return ""+this.EnumValue;
 	}
 }
