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
 
 package org.GreenTeaScript.Konoha;
 import org.GreenTeaScript.GreenTeaArray;
 import org.GreenTeaScript.GreenTeaTopObject;
 import org.GreenTeaScript.GtType;
 
 public class ArrayApi {
 	public final static Object NewArray(GtType Type, int InitSize) {
 		GtType ArrayType = Type.Context.GetGenericType1(Type.Context.ArrayType, Type, true); 
 		if(Type.BaseType.IsIntType()) {
 			return new GreenTeaIntArray(ArrayType, InitSize);
 		}
 		else {
 			GreenTeaArray ArrayObject =  new GreenTeaArray(ArrayType);
 			for(int i = 0; i < InitSize; i++) {
 				ArrayObject.ArrayBody.add(Type.DefaultNullValue);
 			}
 			return ArrayObject;
 		}
 	}
 	public final static Object NewArrayLiteral(GtType ArrayType, Object[] Values) {
 		if(ArrayType.TypeParams[0].BaseType.IsIntType()) {
 			GreenTeaIntArray ArrayObject =  new GreenTeaIntArray(ArrayType, Values.length);
 			for(int i = 0; i < Values.length; i++) {
 				ArrayObject.Add(Values[i] == null ? 0 : ((Number)Values[i]).longValue());
 			}
 			return ArrayObject;
 		}
 		else {
 			GreenTeaArray ArrayObject =  new GreenTeaArray(ArrayType);
 			for(int i = 0; i < Values.length; i++) {
 				ArrayObject.ArrayBody.add(Values[i]);
 			}
 			return ArrayObject;
 		}
 	}
 	public final static long GetSize(GreenTeaArray self) {
 		return self.ArrayBody.size();
 	}
 	public final static long GetSizeI(GreenTeaIntArray self) {
 		return self.Size;
 	}
 	public final static Object Get(GreenTeaArray self, long Index) {
 		return self.ArrayBody.get((int)Index);
 	}
 	public final static long GetI(GreenTeaIntArray self, long Index) {
 		if(!((int)Index < self.Size)) {
 			throw new ArrayIndexOutOfBoundsException(""+Index);
 		}
 		return self.ArrayBody[(int)Index];
 	}
 	public final static void Set(GreenTeaArray self, long Index, Object Value) {
 		self.ArrayBody.set((int)Index, Value);
 	}
 	public final static void SetI(GreenTeaIntArray self, long Index, long Value) {
 		if(!((int)Index < self.Size)) {
 			throw new ArrayIndexOutOfBoundsException(""+Index);
 		}
 		self.ArrayBody[(int)Index] = Value;
 	}
 	public final static GreenTeaArray Add(GreenTeaArray self, Object Value) {
 		self.ArrayBody.add(Value);
 		return self;
 	}
 	public final static GreenTeaIntArray AddI(GreenTeaIntArray self, long Value) {
 		self.Add(Value);
 		return self;
 	}
 	public final static GreenTeaArray Slice(GreenTeaArray self, long BIndex) {
 		int bindex = (BIndex < 0) ? self.ArrayBody.size() - (int)BIndex : (int)BIndex;
 		return self.SubArray(bindex, self.ArrayBody.size());
 	}
 	public final static GreenTeaArray Slice(GreenTeaArray self, long BIndex, long EIndex) {
 		int bindex = (BIndex < 0) ? self.ArrayBody.size() - (int)BIndex : (int)BIndex;
 		int eindex = (EIndex < 0) ? self.ArrayBody.size() - (int)EIndex : (int)EIndex;
 		return self.SubArray(bindex, eindex);
 	}
 	public final static GreenTeaIntArray SliceI(GreenTeaIntArray self, long BIndex) {
 		int bindex = (BIndex < 0) ? self.Size - (int)BIndex : (int)BIndex;
 		return self.SubArray(bindex, self.Size);
 	}
 	public final static GreenTeaIntArray SliceI(GreenTeaIntArray self, long BIndex, long EIndex) {
 		int bindex = (BIndex < 0) ? self.Size - (int)BIndex : (int)BIndex;
 		int eindex = (EIndex < 0) ? self.Size - (int)EIndex : (int)EIndex;
 		return self.SubArray(bindex, eindex);
 	}
 
 //	// Converter
 //	public final static GreenTeaArray<?> AnyToGreenArray(GtType Type, Object Value) {
 //		if(Value.getClass().isArray()) {
 //			Class<?> ComponentClass = Value.getClass().getComponentType();
 //			LibGreenTea.GetNativeType(Type.Context, ComponentClass);
 //		}
 //		//return ArrayObject;
 //	}
 
 //	public final static String[] GreenArrayToAny(GtType Type, GreenTeaArray<String> ArrayObject) {
 //		String[] Values = new String[ArrayObject.ArrayBody.size()];
 //		for(int i = 0; i < Values.length; i++) {
 //			Object Value = ArrayObject.ArrayBody.get(i);
 //			Values[i] = (String)Value;
 //		}
 //		return Values;
 //	}
 	
 	public final static GreenTeaArray StringArrayToGreenArray(GtType Type, String[] Values) {
 		GtType ArrayType = Type.Context.GetGenericType1(Type.Context.ArrayType, Type.Context.StringType, true);
 		GreenTeaArray ArrayObject = new GreenTeaArray(ArrayType);
 		for(int i = 0; i < Values.length; i++) {
 			ArrayObject.ArrayBody.add(Values[i]);
 		}
 		return ArrayObject;
 	}
 
 	public final static String[] GreenArrayToStringArray(GtType Type, GreenTeaArray ArrayObject) {
 		String[] Values = new String[ArrayObject.ArrayBody.size()];
 		for(int i = 0; i < Values.length; i++) {
 			Object Value = ArrayObject.ArrayBody.get(i);
 			Values[i] = (String)Value;
 		}
 		return Values;
 	}
 }
 
 class GreenTeaIntArray extends GreenTeaTopObject {
 	public long[] ArrayBody;
 	public int Size;
 	public GreenTeaIntArray/*constructor*/(GtType GreenType, int InitCapacity) {
 		super(GreenType);
 		this.ArrayBody = new long[InitCapacity < 8 ? 8 : InitCapacity];
 		this.Size = 0;
 	}
 	public void Add(long Value) {
 		if(!(this.Size < this.ArrayBody.length)) {
 			long[] NewArray = new long[this.ArrayBody.length * 2];
 			System.arraycopy(this.ArrayBody, 0, NewArray, 0, this.Size);
 			this.ArrayBody = NewArray;
 		}
 		this.ArrayBody[this.Size] = Value;
 		this.Size += 1;
 	}
 	
 	public GreenTeaIntArray SubArray(int bindex, int eindex) {
 		GreenTeaIntArray ArrayObject = new GreenTeaIntArray(this.GreenType, eindex-bindex);
 		for(int i = bindex; i < eindex; i++) {
 			this.Add(this.ArrayBody[i]);
 		}
 		return ArrayObject;
 	}
 	@Override public String toString() {
 		String s = "[";
 		for(int i = 0; i < this.Size; i++) {
 			Object Value = this.ArrayBody[i];
 			if(i > 0) {
 				s += ", " + Value;
 			}
 			else {
 				s += Value;
 			}
 		}
 		return s + "]";
 	}
 }
