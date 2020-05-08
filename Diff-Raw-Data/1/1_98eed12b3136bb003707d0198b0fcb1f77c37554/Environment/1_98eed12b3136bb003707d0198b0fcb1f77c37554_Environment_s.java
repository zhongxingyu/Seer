 package edu.tum.lua;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.tum.lua.ast.Exp;
 import edu.tum.lua.stdlib.Assert;
 import edu.tum.lua.stdlib.Error;
 import edu.tum.lua.stdlib.GetMetatable;
 import edu.tum.lua.stdlib.IPairs;
 import edu.tum.lua.stdlib.Load;
 import edu.tum.lua.stdlib.LoadFile;
 import edu.tum.lua.stdlib.LoadString;
 import edu.tum.lua.stdlib.Next;
 import edu.tum.lua.stdlib.NotImplementedFunction;
 import edu.tum.lua.stdlib.PCall;
 import edu.tum.lua.stdlib.Pairs;
 import edu.tum.lua.stdlib.Print;
 import edu.tum.lua.stdlib.RawEqual;
 import edu.tum.lua.stdlib.RawGet;
 import edu.tum.lua.stdlib.RawSet;
 import edu.tum.lua.stdlib.Select;
 import edu.tum.lua.stdlib.SetMetatable;
 import edu.tum.lua.stdlib.ToNumber;
 import edu.tum.lua.stdlib.ToString;
 import edu.tum.lua.stdlib.Type;
 import edu.tum.lua.stdlib.Unpack;
 import edu.tum.lua.stdlib.VoidFunction;
 import edu.tum.lua.stdlib.math.Abs;
 import edu.tum.lua.stdlib.math.Acos;
 import edu.tum.lua.stdlib.math.Asin;
 import edu.tum.lua.stdlib.math.Atan;
 import edu.tum.lua.stdlib.math.Atan2;
 import edu.tum.lua.stdlib.math.Ceil;
 import edu.tum.lua.stdlib.math.Cos;
 import edu.tum.lua.stdlib.math.Cosh;
 import edu.tum.lua.stdlib.math.Deg;
 import edu.tum.lua.stdlib.math.Floor;
 import edu.tum.lua.stdlib.math.Log;
 import edu.tum.lua.stdlib.math.Log10;
 import edu.tum.lua.stdlib.math.Max;
 import edu.tum.lua.stdlib.math.Min;
 import edu.tum.lua.stdlib.math.Pow;
 import edu.tum.lua.stdlib.math.Rad;
 import edu.tum.lua.stdlib.math.Random;
 import edu.tum.lua.stdlib.math.Sin;
 import edu.tum.lua.stdlib.math.Sinh;
 import edu.tum.lua.stdlib.math.Sqrt;
 import edu.tum.lua.stdlib.math.Tan;
 import edu.tum.lua.stdlib.math.Tanh;
 import edu.tum.lua.stdlib.table.Concat;
 import edu.tum.lua.stdlib.table.Insert;
 import edu.tum.lua.stdlib.table.MaxN;
 import edu.tum.lua.stdlib.table.Remove;
 import edu.tum.lua.stdlib.table.Sort;
 import edu.tum.lua.types.LuaTable;
 
 public class Environment extends LuaTable {
 
 	private final static Environment _G = new Environment();
 
 	public static Environment getGlobalEnvironment() {
 		return _G;
 	}
 
 	/**
 	 * Create a new global environment.
 	 */
 	Environment() {
 		set("assert", new Assert());
 		set("collectgarbage", new VoidFunction());
 		set("dofile", new NotImplementedFunction());
 		set("error", new Error());
 		set("_G", this);
 		set("getfenv", new NotImplementedFunction());
 		set("getmetatable", new GetMetatable());
 		set("ipairs", new IPairs());
 		set("load", new Load());
 		set("loadfile", new LoadFile());
 		set("loadstring", new LoadString());
 		set("next", new Next());
 		set("pairs", new Pairs());
 		set("pcall", new PCall());
 		set("print", new Print());
 		set("rawequal", new RawEqual());
 		set("rawget", new RawGet());
 		set("rawset", new RawSet());
 		set("select", new Select());
 		set("setfenv", new NotImplementedFunction());
 		set("setmetatable", new SetMetatable());
 		set("tonumber", new ToNumber());
 		set("tostring", new ToString());
 		set("type", new Type());
 		set("unpack", new Unpack());
 		set("_VERSION", "Java Lua -1");
 		set("xpcall", new NotImplementedFunction());
 
 		LuaTable coroutine = new LuaTable();
 		LuaTable _package = new LuaTable();
 		LuaTable string = new LuaTable();
 		LuaTable table = new LuaTable();
 
 		table.set("concat", new Concat());
 		table.set("maxn", new MaxN());
 		table.set("insert", new Insert());
 		table.set("remove", new Remove());
 		table.set("sort", new Sort());
		;
 
 		// TODO math.huge math.pi
 		// TODO set implemented functions
 		LuaTable math = new LuaTable();
 		math.set("abs", new Abs());
 		math.set("acos", new Acos());
 		math.set("asin", new Asin());
 		math.set("atan", new Atan());
 		math.set("atan2", new Atan2());
 		math.set("ceil", new Ceil());
 		math.set("cos", new Cos());
 		math.set("cosh", new Cosh());
 		math.set("deg", new Deg());
 		math.set("exp", new edu.tum.lua.stdlib.math.Exp());
 		math.set("floor", new Floor());
 		math.set("fmod", new NotImplementedFunction());
 		math.set("frexp", new NotImplementedFunction());
 		math.set("ldexp", new NotImplementedFunction());
 		math.set("log", new Log());
 		math.set("log10", new Log10());
 		math.set("max", new Max());
 		math.set("min", new Min());
 		math.set("modf", new NotImplementedFunction());
 		math.set("pi", Math.PI);
 		math.set("pow", new Pow());
 		math.set("rad", new Rad());
 		math.set("random", new Random());
 		math.set("randomseed", new NotImplementedFunction());
 		math.set("sin", new Sin());
 		math.set("sinh", new Sinh());
 		math.set("sqrt", new Sqrt());
 		math.set("tan", new Tan());
 		math.set("tanh", new Tanh());
 
 		LuaTable io = new LuaTable();
 		LuaTable os = new LuaTable();
 		LuaTable debug = new LuaTable();
 
 		set("coroutine", coroutine);
 		set("package", _package);
 		set("string", string);
 		set("table", table);
 		set("math", math);
 		set("io", io);
 		set("os", os);
 		set("debug", debug);
 	}
 
 	public Environment(Environment forward) {
 		super(forward);
 	}
 
 	public void assign(List<String> identifiers, List<Exp> expressions) {
 		List<Object> values = new ArrayList<>(expressions.size());
 
 		for (Exp exp : expressions) {
 			values.add(LuaInterpreter.eval(exp, this));
 		}
 
 		for (int i = 0; i < identifiers.size(); i++) {
 			set(identifiers.get(i), values.get(i));
 		}
 	}
 }
