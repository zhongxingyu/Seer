 package com.singpath.verifier;
 
 import java.io.FileReader;
 import java.io.IOException;
 //import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 //import javax.script.ScriptEngine;
 //import javax.script.ScriptEngineManager;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.mozilla.javascript.Context;
 //import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Scriptable;
 //import org.mozilla.javascript.ScriptableObject;
 
 import org.json.JSONObject;
 @SuppressWarnings("serial")
 public class JavascriptVerifierServlet extends HttpServlet {
 
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		JavascriptVerifierServlet instance = new JavascriptVerifierServlet();
 		String testscript = "var firstname;\nfirstnam=\"Hege\";\n";
 		try
 		{
 			System.out.print(instance.parseJs(testscript, "org.junit.Assert.assertEquals(firstname, \"Hee\");\norg.junit.Assert.assertEquals(firstname, \"Hege\");"));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws IOException{
 		//String script = req.getParameter("script");
 		//String tests = req.getParameter("tests");
 		String userStr = req.getParameter("jsonrequest");
 		String script = null;
 		String tests = null;
 
 
 		try
 		{
 			if(userStr != null)
 			{
 				JSONObject jsonObj = new JSONObject(userStr);
 
 				script = jsonObj.getString("solution");
 				tests = jsonObj.getString("tests");
 			}
 			else
 			{
 				script = req.getParameter("script");
 				tests = req.getParameter("tests");
 			}
 
 			resp.getWriter().println(this.parseJs(script, tests));
 		}
 		catch(Exception e)
 		{
 			logger.info("error in doPost:" + e);
 			HashMap<String, String> em = new HashMap<String, String>();
 			em.put("errors", e.toString());
 			resp.getWriter().println(new JSONObject(em).toString());
 		}
 	}
 
 
 
 	public String parseJs(String script, String tests) throws Exception
 	{
 		/*
 		ScriptEngineManager manager = new ScriptEngineManager();
 		ScriptEngine engine = manager.getEngineByName("JavaScript");
 		engine.eval(new FileReader("E:/odeskproject/appengine/env.js"));
 		engine.eval(script);appengine
 		*/
 
 		//Object ret = jsContext.evaluateString(scope, script, "<stdin>", 0,
 		//        null);
 		//return org.mozilla.javascript.Context.toString(ret);
 		StringBuffer strResult = new StringBuffer();
 		Context cx = Context.enter();
 		Scriptable scope = cx.initStandardObjects();
 		cx.setOptimizationLevel(-1);
 		cx.evaluateReader(scope, new FileReader("WEB-INF/js/env.rhino.js"), "env.rhino.js", 0, null);
 		cx.evaluateReader(scope, new FileReader("WEB-INF/js/assert_equal.js"), "assert_equal.js", 0, null);
 		//cx.evaluateReader(scope, new FileReader("WEB-INF/js/jquery-1.4.2.js"), "jquery-1.4.2.js", 0, null);
 		//Main.processSource(cx, "E:/odeskproject/appengine/thatcher-enthatcherv-js-cb738b9/dist/env.rhino.js");
 		//cx.evaluateReader(scope, new FileReader("E:/odeskproject/appengine/jquery-1.4.2.js"), "<stdin>", 0, null);
 		//Object wrappedOut = Context.javaToJS(this., scope);
 		//ScriptableObject.putProperty(scope, "out", wrappedOut);
 
 		Object ret = cx.evaluateString(scope, script, "script", 0, null);
 
 		String[] testscripts = tests.split("\n");
 		boolean solved = true;
 
 		ArrayList<JSONObject> testResults = new ArrayList<JSONObject>();
 
 		for(String testscript : testscripts)
 		{
 			System.out.println("Evaluating test "+testscript);
 
 			if(testscript.trim().equals(""))
 				continue;
 			try
 			{
 				cx.evaluateString(scope, testscript, "tests", 0, null);
 				if(testscript.indexOf("assert") == -1)
 					continue;
 			}
 			catch(Throwable e)
 			{
 				System.out.println("Caught an error for test "+testscript);
 				System.out.println(e.getMessage());
 				HashMap<String, Object> resulthash = new HashMap<String, Object>();
 				solved = false;
 
 				String failS = e.getMessage();
 				failS = failS.replace("expected:<", "");
 				failS = failS.replace("> but was:<", "MYSPLIT");
 				failS = failS.replace(">", "MYSPLIT");
 				String[] ss = failS.split("MYSPLIT");

				if (ss.length != 2)
					throw new BadFailureMessageException(failS);

 				resulthash.put("expected", ss[0]);
 				resulthash.put("received", ss[1]);
 				resulthash.put("call", testscript);
 				resulthash.put("correct", false);
 				testResults.add(new JSONObject(resulthash));
 
 				continue;
 			}
 			HashMap<String, Object> resulthash = new HashMap<String, Object>();
 			//resulthash.put("expected", value);
 			//resulthash.put("received", valueincode);
 			//resulthash.put("call", key);
 			resulthash.put("call", testscript);
 			resulthash.put("correct", true);
 			testResults.add(new JSONObject(resulthash));
 		}
 		HashMap resultjson = new HashMap();
 		resultjson.put("solved", solved);
 		resultjson.put("results", testResults);
 		JSONObject json = new JSONObject(resultjson);
 		strResult.append(json.toString());
 
 
 		return strResult.toString();
 	}
 
 	public static void print(Object o)
 	{
 		System.out.println(o);
 	}
 }
