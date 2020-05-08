 package com.example.nxtdriver;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 public class Compiler {
 	String CHECK = "";
 	ArrayList <JSONObject> variables = new ArrayList <JSONObject>(); 
 	public JSONObject definitions;
 	public JSONArray main;
 	int count = 0;
 	Threads t;
 	public Compiler(String jsonstring, Threads t) 
 	{
 		JSONObject superJSON = new JSONObject(jsonstring);
 		this.definitions = superJSON.getJSONObject("definitions");		
 		this.main = superJSON.getJSONArray("main");
 		variables.add(new JSONObject());
 		this.t=t;
 	}
 
 	public void compile () {
 
 		this.compile(this.main);
 
 	}
 
 	public void compile(JSONArray code) 
 	{
 
 		JSONArray copy = new JSONArray(code.toString());
 		for (int i = 0; i < code.length(); i++) 
 		{
 			evaluateObject(copy.getJSONObject(i));
 		}
 	}
 
 	public void next() 
 	{
 		evaluateObject(this.main.getJSONObject(this.count++));
 	}
 
 	private String evaluateObject(JSONObject jsonObject) 
 	{
 		String name = (String) jsonObject.keys().next();
 		JSONObject function = jsonObject.getJSONObject(name);
 		JSONArray keys = function.names();
 		JSONObject copy = new JSONObject(function.toString());
 		if (keys != null && !name.equals("if") &&!name.equals("while")) {
 			for (int i = 0; i < keys.length(); i++) { 
 				String temp_str = keys.getString(i);
 				if (function.get(temp_str).getClass() != CHECK.getClass()) 
 				{
 					function.put(temp_str, evaluateObject(copy.getJSONObject(temp_str)));
 				}
 			}
 		}
 		else if (name.equals("if")) 
 		{
 			if (evaluateObject(copy.getJSONObject("condition")).equals("true")) 
 			{
 				this.compile(copy.getJSONArray("code"));
 			}
 			else 
 			{
 				this.compile(copy.getJSONArray("else"));
 			}
 		}
 
 		else if (name.equals("while")) 
 		{
 			while (evaluateObject(copy.getJSONObject("condition")).equals("true"))
 			{
 				this.compile(copy.getJSONArray("code"));
 			}
 		}
 
 		if (name.equals("add")) { return add((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("sub")) { return sub((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("assign")) { assign((String) function.get("arg0"),(String) function.get("arg1")); 
 
 		} else if (name.equals("print")) { print((String) function.get("arg0"));
 
 		} else if (name.equals("greater_than")) { return greater((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("less_than")) { return greater((String) function.get("arg1"),(String) function.get("arg0"));
 
 		} else if (name.equals("equals")) { return equals((String) function.get("arg1"),(String) function.get("arg0"));
 
 		} else if (name.equals("and")) { return and((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("or")) { return or((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("not")) { return not((String) function.get("arg0"));
 
 		} else if (name.equals("mult")) { return mult((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("div")) { return div((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("mod")) { return mod((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("motorA")) { motorA((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("motorB")) { motorB((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("motorC")) { motorC((String) function.get("arg0"),(String) function.get("arg1"));
 
 		} else if (name.equals("motorAB")) { Log.v("nxtdrivercompiler", "compiled motorab");motorAB((String) function.get("arg0"),(String) function.get("arg1"), (String) function.get("arg2"));
 
 		} else if (name.equals("motorAC")) { motorAC((String) function.get("arg0"),(String) function.get("arg1"), (String) function.get("arg2"));
 
 		} else if (name.equals("motorBC")) { motorBC((String) function.get("arg0"),(String) function.get("arg1"), (String) function.get("arg2"));
 
 		} else if (name.equals("motorABC")) { motorABC((String) function.get("arg0"),(String) function.get("arg1"), (String) function.get("arg2"), (String) function.get("arg3"));
 
 		} else if (name.equals("forward") || name.equals("fd")) {motorAB("80", "80", String.valueOf(Integer.parseInt(lookup(((String)function.get("arg0"))))*1000));
 
 		} else if (name.equals("backward") || name.equals("bd")) { motorAB("-80", "-80", String.valueOf(Integer.parseInt(lookup(((String)function.get("arg0"))))*1000));
 
 		} else if (name.equals("right") || name.equals("rt")) { motorAB("0", "80", String.valueOf(Integer.parseInt(lookup(((String)function.get("arg0"))))*1000));
 
 		} else if (name.equals("left") || name.equals("lt")) { motorAB("80", "0", String.valueOf(Integer.parseInt(lookup(((String)function.get("arg0"))))*1000));
 
 		} else if (name.equals("return")) { return ((String) lookup((String) function.get("arg0")));
 
 		} else if (name.equals("delay")) { delay(String.valueOf(Integer.parseInt(lookup(((String)function.get("arg0"))))*1000));
 
 		} else { 
 			if (this.definitions.has(name)) {
 				//put all arguments into an array
 				JSONObject definedfunction = this.definitions.getJSONObject(name);
 				JSONObject localvariables = new JSONObject();
 				JSONObject argumentsdefinitions = definedfunction.getJSONObject("args"); //definition of arguments
 				if (function.length() == argumentsdefinitions.length()) {
 
 					for (int i = 0; i < function.length(); i++) {
 						String argumentnum = "arg" + i;
 						localvariables.put(argumentsdefinitions.getString(argumentnum), lookup(function.getString(argumentnum)));
 					}
 
 					variables.add(localvariables);
 					JSONArray ourcode = definedfunction.getJSONArray("code");
 
 					for (int i = 0; i < ourcode.length(); i++) {
 
 						JSONObject codetoexecute = ourcode.getJSONObject(i);
 
 						if (codetoexecute.has("return")) {
 							return evaluateObject(codetoexecute);
 						}
 						else {
 							evaluateObject(codetoexecute);
 						}
 
 					}
 
 					variables.remove(variables.size()-1);
 				}
 				else {
 
 					//"different number of arguments" error
 
 				}
 			}
 			else {
 
 				//put an error statement "not a function" here
 
 			}
 		}
 
 		return "";		
 
 	}
 
 	private void motorA(String x, String y) {
 		t.change_motor(new boolean[] {true, false, false}, new byte[] {Byte.parseByte(lookup(x))}, Integer.parseInt(lookup(y)));
 	}
 
 	private void motorB(String x, String y) {
 		t.change_motor(new boolean[] {false, true, false}, new byte[] {Byte.parseByte(lookup(x))}, Integer.parseInt(lookup(y)));
 	}
 
 	private void motorC(String x, String y) {
 		t.change_motor(new boolean[] {false, false, true}, new byte[] {Byte.parseByte(lookup(x))}, Integer.parseInt(lookup(y)));
 	}
 
 	private void motorAB(String x1, String x2, String y) {
 		Log.v("nxtcompiler", x1+" "+x2+" "+y);
 		t.change_motor(new boolean[] {true, true, false}, new byte[] {Byte.parseByte(lookup(x1)), Byte.parseByte(lookup(x2))}, Integer.parseInt(lookup(y)));
 	}
 
 	private void motorAC(String x1, String x2, String y) {
 		t.change_motor(new boolean[] {true, false, true}, new byte[] {Byte.parseByte(lookup(x1)), Byte.parseByte(lookup(x2))}, Integer.parseInt(lookup(y)));
 	}
 
 	private void motorBC(String x1, String x2, String y) {
 		t.change_motor(new boolean[] {false, true, true}, new byte[] {Byte.parseByte(lookup(x1)), Byte.parseByte(lookup(x2))}, Integer.parseInt(lookup(y)));
 	}
 
 	private void motorABC(String x1, String x2, String x3, String y) {
 		t.change_motor(new boolean[] {true, true, true}, new byte[] {Byte.parseByte(lookup(x1)), Byte.parseByte(lookup(x2)), Byte.parseByte(lookup(x3))}, Integer.parseInt(lookup(y)));
 	}
 
 	private void assign(String x, String y) 
 	{
 		if (getScope().has(x)) 
 		{
 			getScope().remove(x);
 		}
 		getScope().put(x, y);
 	}
 
 	private  String add(String x, String y) 
 	{
 		return Integer.toString(Integer.parseInt(lookup(x))+Integer.parseInt(lookup(y)));
 	}
 
 	private  String sub(String x, String y) 
 	{
 		return Integer.toString(Integer.parseInt(lookup(x))-Integer.parseInt(lookup(y)));
 	}
 
 	private  String mult(String x, String y) 
 	{
 		return Integer.toString(Integer.parseInt(lookup(x))*Integer.parseInt(lookup(y)));
 	}
 
 	private  String div(String x, String y) 
 	{
 		return Integer.toString(Integer.parseInt(lookup(x))/Integer.parseInt(lookup(y)));
 	}
 
 	private  String mod(String x, String y) 
 	{
 		return Integer.toString(Integer.parseInt(lookup(x)) % Integer.parseInt(lookup(y)));
 	}
 	
 	private void delay(String x) 
 	{
 	    t.change_motor(new boolean[] {true, true, true}, new byte[] {0,0,0}, Integer.parseInt(lookup(x)));
 	}
 
 	private void print(String x) 
 	{
 	    	t.sendserver.submit(t.new postHTML(String.valueOf(lookup(x))));
 	}
 
 	private  String greater(String x, String y) 
 	{
 		return Boolean.toString(Integer.parseInt(lookup(x))>Integer.parseInt(lookup(y)));
 	}
 
 	private  String equals(String x, String y) 
 	{
 		return Boolean.toString(Integer.parseInt(lookup(x))==Integer.parseInt(lookup(y)));
 	}
 
 	private  String and(String x, String y) 
 	{
 		return Boolean.toString(x.equals("true") && y.equals("true"));
 	}
 
 	private  String or(String x, String y) 
 	{
 		return Boolean.toString(x.equals("true") || y.equals("true"));
 	}
 
 	private  String not(String x) 
 	{
 		return Boolean.toString(!(x.equals("true")));
 	}
 
 	private  String lookup(String x) {
 		if (x.length() > 0) {
 			if (x.substring(0, 1).equals("^")) 
 			{
 				return x.substring(1, x.length());
 			}
 		}
 
 		for (int i = variables.size()-1; i > -1; i--) 
 		{
 			if (variables.get(i).has(x)) 
 			{
 				return (String) variables.get(i).getString(x);
 			}
 		}
 		return x;
 	}
 
 	private  JSONObject getScope() 
 	{
 		return variables.get(variables.size()-1);
 	}
 }
 
