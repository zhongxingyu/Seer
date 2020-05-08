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
 
 // LangBase is a language-dependent code used in GreenTea.java
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public abstract class LibGreenTea {
 
 	public final static String GetPlatform() {
 		return "Java JVM-" + System.getProperty("java.version");
 	}
 
 	public final static void println(String msg) {
 		System.out.println(msg);
 	}
 
 	public static boolean DebugMode = true;
 	
 	private final static String GetStackInfo(int depth){
 		String LineNumber = " ";
 		Exception e =  new Exception();
 		StackTraceElement[] Elements = e.getStackTrace();
 		if(depth < Elements.length){
 			StackTraceElement elem = Elements[depth];
 			LineNumber += elem;
 		}
 		return LineNumber;
 	}
 
 
 	public final static void TODO(String msg) {
 		LibGreenTea.println("TODO" + LibGreenTea.GetStackInfo(2) + ": " + msg);
 	}
 
 	public final static void DebugP(String msg) {
 		if(LibGreenTea.DebugMode) {
 			LibGreenTea.println("DEBUG" + LibGreenTea.GetStackInfo(2) + ": " + msg);
 		}
 	}
 
 	public final static void VerboseLog(int VerboseFlag, String Message) {
 		LibGreenTea.println(Message);
 	}
 	
 
 	public final static void Exit(int status, String Message) {
 		System.err.println(Message);
 		System.exit(1);
 	}
 
 	public final static void Assert(boolean TestResult) {
 		if(!TestResult) {
 			Exit(1, "Assertion Failed");
 		}
 	}
 
 	public final static boolean IsWhitespace(char ch) {
 		return Character.isWhitespace(ch);
 	}
 
 	public final static boolean IsLetter(char ch) {
 		return Character.isLetter(ch);
 	}
 
 	public final static boolean IsDigit(char ch) {
 		return Character.isDigit(ch);
 	}
 	
 	public final static char CharAt(String Text, int Pos) {
 		return Text.charAt(Pos);
 	}
 
 	public final static String CharToString(char code){
 		return Character.toString(code);
 	}
 
 	public final static boolean EqualsString(String s, String s2){
 		return s.equals(s2);
 	}
 	
 	public final static int ParseInt(String Text) {
 		return Integer.parseInt(Text);
 	}
 
 	public final static Method LookupNativeMethod(Object Callee, String FuncName) {
 		if(FuncName != null) {
 			// LibGreenTea.DebugP("looking up method : " + Callee.getClass().getSimpleName() + "." + FuncName);
 			Method[] methods = Callee.getClass().getMethods();
 			for(int i = 0; i < methods.length; i++) {
 				if(FuncName.equals(methods[i].getName())) {
 					return methods[i];
 				}
 			}
 			LibGreenTea.DebugP("method not found: " + Callee.getClass().getSimpleName() + "." + FuncName);
 		}
 		return null;
 	}
 
 	public final static boolean EqualsFunc(Method m1, Method m2) {
 		if(m1 == null) {
 			return (m2 == null) ? true : false;
 		}
 		else {
 			return (m2 == null) ? false : m1.equals(m2);
 		}
 	}
 
 	public final static TokenFunc CreateOrReuseTokenFunc(GtDelegateToken f, TokenFunc prev) {
 		if(prev != null && EqualsFunc(prev.Func.Func, f.Func)) {
 			return prev;
 		}
 		return new TokenFunc(f, prev);
 	}
 
 	public final static int ApplyTokenFunc(GtDelegateToken Delegate, Object TokenContext, String Text, int pos) {
 		try {
 			Integer n = (Integer)Delegate.Func.invoke(Delegate.Self, TokenContext, Text, pos);
 			return n.intValue();
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		Exit(1, "Failed ApplyTokenFunc");
 		return -1;
 	}
 
 	public final static GtSyntaxTree ApplyMatchFunc(GtDelegateMatch Delegate, Object NameSpace, Object TokenContext, Object LeftTree, Object Pattern) {
 		try {
 			return (GtSyntaxTree)Delegate.Func.invoke(Delegate.Self, NameSpace, TokenContext, LeftTree, Pattern);
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		Exit(1, "Failed ApplyMatchFunc");
 		return null;
 	}
 
 	public final static GtNode ApplyTypeFunc(GtDelegateType Delegate, Object Gamma, Object ParsedTree, Object TypeInfo) {
 		try {
 			return (GtNode)Delegate.Func.invoke(Delegate.Self, Gamma, ParsedTree, TypeInfo);
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		Exit(1, "Failed ApplyTypeFunc");
 		return null;
 	}
 
 	public final static GtType[] CompactTypeList(int BaseIndex, ArrayList<GtType> List) {
 		GtType[] Tuple = new GtType[List.size() - BaseIndex];
 		for(int i = BaseIndex; i < List.size(); i++) {
 			Tuple[i] = List.get(i);
 		}
 		return Tuple;
 	}
 
 	public final static String[] CompactStringList(ArrayList<String> List) {
 		if(List == null) return null;
 		String[] Tuple = new String[List.size()];
 		for(int i = 0; i < List.size(); i++) {
 			Tuple[i] = List.get(i);
 		}
 		return Tuple;
 	}
 
 	public static String[] MapGetKeys(GtMap Map) {
 		/*local*/Iterator<String> itr = Map.Map.keySet().iterator();
 		/*local*/ArrayList<String> List = new ArrayList<String>(Map.Map.size());
 		/*local*/int i = 0;
 		while(itr.hasNext()) {
 			List.add(itr.next());
 			i = i + 1;
 		}
 		return List.toArray(new String[List.size()]);
 	}
 
 	public final static void Usage() {
 		System.out.println("usage : ");
 		System.out.println("  --lang|-l LANG        Set Target Language");
 		System.out.println("      bash                Bash");
 		System.out.println("      C C99               C99");
 		System.out.println("      CSharp              CSharp");
 		System.out.println("      java java7 java8    Java");
 		System.out.println("      javascript js       JavaScript");
 		System.out.println("      lua                 Lua");
 		System.out.println("      haxe                Haxe");
 		System.out.println("      ocaml               OCaml");
 		System.out.println("      perl                Perl");
 		System.out.println("      python              Python");
 		System.out.println("      R                   R");
 		System.out.println("      ruby                Ruby");
 		System.out.println("      typescript ts       TypeScript");
 		System.out.println("");
 		System.out.println("  --out|-o  FILE        Program passed in as string");
 		System.out.println("  --eval|-e EXPR        Program passed in as string");
 		System.out.println("  --verbose             Printing Debug infomation");
 		System.exit(0);
 	}
 
 	public final static GtGenerator CodeGenerator(String TargetCode, String OutputFile, int GeneratorFlag) {
 		String Extension = (OutputFile == null) ? "-" : OutputFile;
 		TargetCode = TargetCode.toLowerCase();
 		if(Extension.endsWith(".js") || TargetCode.startsWith("js") || TargetCode.startsWith("javascript")) {
 			return new JavaScriptSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(Extension.endsWith(".pl") || TargetCode.startsWith("perl")) {
 			return new PerlSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(Extension.endsWith(".py") || TargetCode.startsWith("python")) {
 			return new PythonSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(Extension.endsWith(".sh") || TargetCode.startsWith("bash")) {
 			return new BashSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(Extension.endsWith(".java") || TargetCode.startsWith("java")) {
 			return new JavaSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(Extension.endsWith(".c") || TargetCode.startsWith("c")) {
 			return new CSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(TargetCode.startsWith("X") || TargetCode.startsWith("exe")) {
 			return new JavaByteCodeGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		return null;
 	}
 
 	public final static void Eval(String SourceCode) {
 		//eval(SourceCode);
 		System.out.println("Eval: " + SourceCode);  // In Java, no eval
 	}
 	
 	public final static void WriteCode(String OutputFile, String SourceCode) {
 		if(OutputFile == null) {
 			LibGreenTea.Eval(SourceCode);
 		}
 		if(OutputFile.equals("-")) {
 			System.out.println(SourceCode);
 			System.out.flush();
 		}
 		else {
 			Writer out = null;
 			try {
 				out = new FileWriter(OutputFile);
 				out.write(SourceCode);
 				out.flush();
 				out.close();
 			} catch (IOException e) {
 				System.err.println("Cannot write: " + OutputFile);
 				System.exit(1);
 			}
 		}
 	}
 	
 	private static java.io.Console Console = null;
 
 	public final static String ReadLine(String Prompt) {
 		if(Console == null) {
 			Console = System.console();
 		}
 		String Line = Console.readLine(Prompt);
 		if(Line == null) {
 			System.exit(0);
 		}
 		return Line;
 	}
 
 	public final static boolean HasFile(String Path) {
 		return new File(Path).exists();
 	}
 
 	public final static String LoadFile(String FileName) {
 		File f = new File(FileName);
 		byte[] b = new byte[(int) f.length()];
 		FileInputStream fi;
 		try {
 			fi = new FileInputStream(f);
 			fi.read(b);
 			fi.close();
 			return new String(b);
 		} catch (FileNotFoundException e) {
 			System.err.println(e.getMessage());
 			System.exit(1);
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.exit(1);
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	public final static boolean IsSupportedTarget(String TargetCode) {
 		return HasFile("lib/" + TargetCode + "/common.green");
 	}
 	
 	public final static String GetLibPath(String TargetCode, String LibName) {
		return LoadFile("lib/" + TargetCode + "/" + LibName + ".green");
 	}
 
 	public static long JoinIntId(int UpperId, int LowerId) {
 		long id = UpperId;
 		id = (id << 32) + LowerId;
 		return id;
 	}
 
 	public static int UpperId(long FileLine) {
 		return (int)(FileLine >> 32);
 	}
 
 	public static int LowerId(long FileLine) {
 		return (int)FileLine;
 	}
 
 
 
 
 	
 }
