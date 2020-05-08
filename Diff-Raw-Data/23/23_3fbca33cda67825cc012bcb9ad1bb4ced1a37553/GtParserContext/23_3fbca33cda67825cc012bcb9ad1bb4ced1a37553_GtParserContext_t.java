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
 
 public final class GtParserContext extends GreenTeaUtils {
 	/*field*/public final  int ParserId;
 	/*field*/public final  GtGenerator   Generator;
 	/*field*/public final  GtNameSpace   RootNameSpace;
 	/*field*/public GtNameSpace		     TopLevelNameSpace;
 
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
 	/*field*/public final GtType		EnumBaseType;
 	/*field*/public final GtType		StructType;
 	/*field*/public final GtType		VarType;
 
 	/*field*/public final GtType		TypeType;
 
 	/*field*/public final  GtMap               SourceMap;
 	/*field*/public final  ArrayList<String>   SourceList;
 	/*field*/public final  GtMap			   ClassNameMap;
 
 	/*field*/public final GtStat Stat;
 	/*field*/public ArrayList<String>    ReportedErrorList;
 	/*filed*/private boolean NoErrorReport;
 	
 	/*field*/public final ArrayList<GtType>    TypePools;
 	/*field*/public final ArrayList<GtFunc>    FuncPools;
 	
 	GtParserContext/*constructor*/(GtGrammar Grammar, GtGenerator Generator) {
 		this.ParserId     = LibGreenTea.NewParserId();
 		this.Generator    = Generator;
 		this.Generator.Context = this;
 		this.SourceMap     = new GtMap();
 		this.SourceList    = new ArrayList<String>();
 		this.ClassNameMap  = new GtMap();
 		this.RootNameSpace = new GtNameSpace(this, null);
 		this.TypePools     = new ArrayList<GtType>();
 		this.FuncPools     = new ArrayList<GtFunc>();
 		this.Stat = new GtStat();
 		this.NoErrorReport = false;
 		this.ReportedErrorList = new ArrayList<String>();
 
		this.TopType       = this.RootNameSpace.AppendTypeName(new GtType(this, 0, "Top", null, GreenTeaTopObject.class), null);
 		this.StructType    = this.TopType.CreateSubType(0, "record", null, null);       //  unregistered
		this.EnumBaseType  = this.TopType.CreateSubType(EnumType, "enum", null, GreenTeaEnum.class);  //  unregistered
 
 		this.VoidType    = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType, "void", null, Void.class), null);
 		this.BooleanType = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType|UnboxType, "boolean", false, Boolean.class), null);
 		this.IntType     = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType|UnboxType, "int", 0L, Long.class), null);
 		this.FloatType   = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType|UnboxType, "float", 0.0, Double.class), null);
 		this.StringType  = this.RootNameSpace.AppendTypeName(new GtType(this, NativeType, "String", null, String.class), null);
 		this.VarType     = this.RootNameSpace.AppendTypeName(new GtType(this, 0, "var", null, null), null);
 		this.AnyType     = this.RootNameSpace.AppendTypeName(new GtType(this, DynamicType, "any", null, null), null);
 		this.TypeType    = this.RootNameSpace.AppendTypeName(this.TopType.CreateSubType(0, "Type", null, null), null);
 		this.ArrayType   = this.RootNameSpace.AppendTypeName(this.TopType.CreateSubType(0, "Array", null, null), null);
 		this.FuncType    = this.RootNameSpace.AppendTypeName(this.TopType.CreateSubType(0, "Func", null, null), null);
 
 		this.ArrayType.TypeParams = new GtType[1];
 		this.ArrayType.TypeParams[0] = this.VarType;
 		this.FuncType.TypeParams = new GtType[1];
 		this.FuncType.TypeParams[0] = this.VarType;  // for PolyFunc
 
 //ifdef JAVA
 		this.SetNativeTypeName("org.GreenTeaScript.GreenTeaTopObject", this.TopType);
 		this.SetNativeTypeName("void",    this.VoidType);
 		this.SetNativeTypeName("java.lang.Object",  this.AnyType);
 		this.SetNativeTypeName("boolean", this.BooleanType);
 		this.SetNativeTypeName("java.lang.Boolean", this.BooleanType);
 		this.SetNativeTypeName("long",    this.IntType);
 		this.SetNativeTypeName("java.lang.Long",    this.IntType);
 		this.SetNativeTypeName("java.lang.String",  this.StringType);
 		this.SetNativeTypeName("org.GreenTeaScript.GtType", this.TypeType);
 		this.SetNativeTypeName("org.GreenTeaScript.GreenTeaEnum", this.EnumBaseType);
 		this.SetNativeTypeName("org.GreenTeaScript.GreenTeaArray", this.ArrayType);
 		this.SetNativeTypeName("org.GreenTeaScript.Konoha.GreenTeaIntArray", this.GetGenericType1(this.ArrayType, this.IntType, true));
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
 		else if(Value instanceof GreenTeaObject) {
 			// FIXME In typescript, we cannot use GreenTeaObject
 			// TODO fix downcast
 			return (/*cast*/GtType)((/*cast*/GreenTeaObject)Value).GetGreenType();
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
 		/*local*/GtType GenericType = (/*cast*/GtType)this.ClassNameMap.GetOrNull(MangleName);
 		if(GenericType == null && IsCreation) {
 			/*local*/int i = BaseIdx;
 			/*local*/String s = BaseType.ShortName + "<";
 			while(i < LibGreenTea.ListSize(TypeList)) {
 				s = s + TypeList.get(i).ShortName;
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
 		/*local*/Integer Id = /* (FileName == null) ? 0 :*/ (/*cast*/Integer)this.SourceMap.GetOrNull(FileName);
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
 	
 	public final GtType GetTypeById(int TypeId) {
 		return this.TypePools.get(TypeId);
 	}
 
 	public final GtFunc GetFuncById(int FuncId) {
 		return this.FuncPools.get(FuncId);
 	}
 }
