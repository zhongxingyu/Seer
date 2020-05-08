 // LangBase is a language-dependent code used in GreenTea.java
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 public abstract class LangDeps {
 
 	public final static void println(String msg) {
 		System.out.println(msg);
 	}
 
 	public final static void DebugP(String msg) {
 		if(GtStatic.DebugPrintOption) {
 			LangDeps.println("DEBUG" + LangDeps.GetStackInfo(2) + ": " + msg);
 		}
 	}
 
 	public final static String GetStackInfo(int depth){
 		String LineNumber = " ";
 		try{
 			throw new Exception();
 		}
 		catch(Exception e){
 			StackTraceElement[] Elements = e.getStackTrace();
 			if(depth < Elements.length){
 				StackTraceElement elem = Elements[depth];
 				LineNumber += elem;
 			}
 		}
 		return LineNumber;
 	}
 
 	public final static void Exit(int status, String Message) {
 		System.err.println(Message);
 		System.exit(1);
 		//throw new RuntimeException(Message);
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
 
 	public final static int ParseInt(String Text) {
 		return Integer.parseInt(Text);
 	}
 
 	public final static Method LookupMethod(Object Callee, String MethodName) {
 		if(MethodName != null) {
 			// DebugP("looking up method : " + Callee.getClass().getSimpleName() + "." + MethodName);
 			Method[] methods = Callee.getClass().getMethods();
 			for(int i = 0; i < methods.length; i++) {
 				if(MethodName.equals(methods[i].getName())) {
 					return methods[i];
 				}
 			}
 			DebugP("method not found: " + Callee.getClass().getSimpleName() + "." + MethodName);
 		}
 		return null; /*throw new GtParserException("method not found: " + callee.getClass().getName() + "." + methodName);*/
 	}
 
 	public final static boolean EqualsMethod(Method m1, Method m2) {
 		if(m1 == null) {
 			return (m2 == null) ? true : false;
 		}
 		else {
 			return (m2 == null) ? false : m1.equals(m2);
 		}
 	}
 
 	public final static TokenFunc CreateOrReuseTokenFunc(GtDelegateToken f, TokenFunc prev) {
 		if(prev != null && EqualsMethod(prev.Func.Method, f.Method)) {
 			return prev;
 		}
 		return new TokenFunc(f, prev);
 	}
 
 	public final static int ApplyTokenFunc(GtDelegateToken Delegate, Object TokenContext, String Text, int pos) {
 		try {
 			Integer n = (Integer)Delegate.Method.invoke(Delegate.Self, TokenContext, Text, pos);
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
 
 	public final static GtSyntaxTree ApplyMatchFunc(GtDelegateMatch Delegate, Object Pattern, Object LeftTree, Object TokenContext) {
 		try {
 			return (GtSyntaxTree)Delegate.Method.invoke(Delegate.Self, Pattern, LeftTree, TokenContext);
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
 			return (GtNode)Delegate.Method.invoke(Delegate.Self, Gamma, ParsedTree, TypeInfo);
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
 
 	public final static void Usage() {
 		System.out.println("usage : ");
 		System.out.println("  Source Code Generation");
 		System.out.println("  --bash                Bash");
 		System.out.println("  --C                   C99");
 		System.out.println("  --CSharp              CSharp");
 		System.out.println("  --greentea            GreenTea (itself)");
 		System.out.println("  --java                Java");
 		System.out.println("  --javascript --js     JavaScript");
 		System.out.println("  --lua                 Lua");
 		System.out.println("  --haxe                Haxe");
 		System.out.println("  --ocaml               OCaml");
 		System.out.println("  --perl                Perl");
 		System.out.println("  --python              Python");
 		System.out.println("  --R                   R");
 		System.out.println("  --ruby                Ruby");
 		System.out.println("  --typescript --ts     TypeScript");
 		System.out.println("  --X                   Executable Code");
 		System.out.println("");
 		System.out.println("  -e expr               Program passed in as string");
 		System.exit(0);
 	}
 
 	public final static GtGenerator CodeGenerator(String Option) {
 		Option = Option.replaceAll("--", "");
 		if(Option.equalsIgnoreCase("js") || Option.equalsIgnoreCase("javascript")) {
 			return new JavaScriptSourceGenerator();
 		}
 		else if(Option.equalsIgnoreCase("perl")) {
 			return new PerlSourceGenerator();
 		}
 		else if(Option.equalsIgnoreCase("bash")) {
 			return new BashSourceGenerator();
 		}
 		else if(Option.equalsIgnoreCase("c")) {
 			return new CSourceGenerator();
 		}
 		else if(Option.equalsIgnoreCase("java")) {
 			return new JavaSourceGenerator();
 		}
 		else if(Option.equalsIgnoreCase("X")) {
 			return new JavaByteCodeGenerator();
 		}
 		return null;
 	}
 
 	private static java.io.Console Console = null;
 
 	public final static String ReadLine(String Prompt) {
 		if(Console == null) {
 			Console = System.console();
 		}
 		String Line = Console.readLine(Prompt);
 		if(Line != null) {
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
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.exit(1);
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "";
 	}
 }
