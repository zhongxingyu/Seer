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
 
 public final class GtTypeEnv extends GreenTeaUtils {
 	/*field*/public final GtParserContext    Context;
 	/*field*/public final GtGenerator       Generator;
 	/*field*/public GtNameSpace	    NameSpace;
 
 	/*field*/public ArrayList<GtVariableInfo> LocalStackList;
 	/*field*/public int StackTopIndex;
 	/*field*/public GtFunc	Func;
 	/*field*/boolean FoundUncommonFunc;
 	
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
 		this.FoundUncommonFunc = false;
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
 	
 	public void CheckFunc(String FuncType, GtFunc Func, GtToken SourceToken) {
 		if(!this.FoundUncommonFunc && (!Func.Is(CommonFunc))) {
 			this.FoundUncommonFunc = true;
 			if(this.Func != null && this.Func.Is(CommonFunc)) {
 				this.NameSpace.Context.ReportError(WarningLevel, SourceToken, "using uncommon " + FuncType + ": " + Func.FuncName);
 			}
 		}
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
 		if(Node.IsErrorNode() || IsFlag(TypeCheckPolicy, NoCheckPolicy)) {
 			return Node;
 		}
 		if(Node.Type.IsUnrevealedType()) {
 			/*local*/GtFunc Func = ParsedTree.NameSpace.GetConverterFunc(Node.Type, Node.Type.BaseType, true);
 			//System.err.println("found weaktype = " + Node.Type);
 			Node = this.Generator.CreateCoercionNode(Func.GetReturnType(), Func, Node);
 		}
 		//System.err.println("**** " + Node.getClass());
 		/*local*/Object ConstValue = Node.ToConstValue(IsFlag(TypeCheckPolicy, OnlyConstPolicy));
 		if(ConstValue != null && !(Node instanceof GtConstNode)) {  // recreated
 			Node = this.Generator.CreateConstNode(Node.Type, ParsedTree, ConstValue);
 		}
 		if(IsFlag(TypeCheckPolicy, OnlyConstPolicy) && ConstValue == null) {
 			if(IsFlag(TypeCheckPolicy, NullablePolicy) && Node.IsNullNode()) { // OK
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
 		if(Node.Type == Type || Type == this.VarType || Node.Type.Accept(Type)) {
 			return Node;
 		}
 		/*local*/GtFunc Func = ParsedTree.NameSpace.GetConverterFunc(Node.Type, Type, true);
 		if(Func != null && (Func.Is(CoercionFunc) || IsFlag(TypeCheckPolicy, CastPolicy))) {
 			return this.Generator.CreateCoercionNode(Type, Func, Node);
 		}
		System.err.println("node="+Node.getClass()+ "type error: requested = " + Type + ", given = " + Node.Type);
 		return this.ReportTypeResult(ParsedTree, Node, TypeErrorLevel, "type error: requested = " + Type + ", given = " + Node.Type);
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
