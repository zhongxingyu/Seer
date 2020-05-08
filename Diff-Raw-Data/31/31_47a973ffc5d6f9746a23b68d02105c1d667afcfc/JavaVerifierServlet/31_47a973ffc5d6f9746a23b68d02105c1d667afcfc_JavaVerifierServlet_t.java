 package com.singpath.verifier;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.StringReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
 import javax.servlet.http.*;
 
 import org.json.JSONObject;
 
 //import java.net.URLDecoder;
 import bsh.Interpreter;
 import bsh.TargetError;
 
 import static org.junit.Assert.*;
 
 @SuppressWarnings("serial")
 public class JavaVerifierServlet extends HttpServlet {
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws IOException{
 		this.doGet(req, resp);
 	}
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String userStr = req.getParameter("jsonrequest");
 
 		logger.info("userStr:" + userStr);
 		//userStr = URLDecoder.decode(userStr, "UTF-8");
 		//mock a json test string
 
 		try
 		{
 			//HashMap hash = new HashMap();
 			//hash.put("tests", ">>> a \n 1\n>>> b\n2\n\n\n");
 			//hash.put("solution", "int a=1;\nint b=1;\n\n");
 			//JSONObject testObj = new JSONObject(hash);
 			//userStr = testObj.toString();
 			//resp.getWriter().println(userStr);
 			logger.info("userStr:" + userStr);
 			JSONObject jsonObj = new JSONObject(userStr);
 			resp.getWriter().println(parseJava(jsonObj.getString("solution"), jsonObj.getString("tests")));
 
 		}
 		catch(Exception e)
 		{
 			logger.info("error in doGet: " + e);
 			HashMap<String, String> em = new HashMap<String, String>();
 			em.put("errors", e.toString());
 			resp.getWriter().println(new JSONObject(em).toString());
 		}
 
 		resp.setContentType("application/json");
 
 
 	}
 
 	public HashMap<String, String> getTests(String s) throws Exception
 	{
 		HashMap<String, String> tests = new HashMap<String, String>();
 		String[] test = s.split(">>>");
 		for(int i = 1; i < test.length; i++)
 		{
 			BufferedReader reader = new BufferedReader(new StringReader(test[i]));
 			String key = reader.readLine().trim();
 			String value = reader.readLine().trim();
 			tests.put(key, value);
 
 		}
 		return tests;
 	}
 
 	public String readJSONString(HttpServletRequest request){
 		StringBuffer json = new StringBuffer();
 		String line = null;
 		try {
 			BufferedReader reader = request.getReader();
 			while((line = reader.readLine()) != null) {
 				json.append(line);
 			}
 		}
 		catch(Exception e) {
 			logger.info("Error in readJSONString:" + e.toString());
 		}
 		return json.toString();
 	}
 
 	public String parseJava(String script, String tests) throws Exception
 	{
 		StringBuffer strResult = new StringBuffer();
 		ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
 		PrintStream output = new PrintStream(outputBuffer);
 		Interpreter interpreter = new Interpreter(null, null, null, false);
 		interpreter.setStrictJava(true);
 
 		try
 		{
 			interpreter.eval("static import org.junit.Assert.*;");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		PrintStream out = System.out;
 		PrintStream err = System.err;
 		System.setOut(output);
 		System.setErr(output);
 
 		try {
 			// Eval the user text
 			interpreter.eval(script);
 		}
 		finally {
 			System.setOut(out);
 			System.setErr(err);
 		}
 
 		output.flush();
 
 		String[] testscripts = tests.split("\n");
 		ArrayList<JSONObject> testResults = new ArrayList<JSONObject>();
 		boolean solved = true;
 		for(String testscript : testscripts)
 		{
 			//ignore blank line
 			if(testscript.trim().equals(""))
 				continue;
 			try
 			{
 				interpreter.eval(testscript);
 				if(testscript.indexOf("assert") == -1)
 					continue;
 			}
 			catch(TargetError e)
 			{
 				HashMap<String, Object> resulthash = new HashMap<String, Object>();
 				solved = false;
 				//special handling for assertTrue and assertFalse, because the exception message is empty
 				if(testscript.indexOf("assertTrue(") != -1)
 				{
 					resulthash.put("expected", true);
 					resulthash.put("received", false);
 					resulthash.put("call", testscript);
 					resulthash.put("correct", false);
 					testResults.add(new JSONObject(resulthash));
 					continue;
 				}
 				else
 				if(testscript.indexOf("assertFalse(") != -1)
 				{
 					resulthash.put("expected", false);
 					resulthash.put("received", true);
 					resulthash.put("call", testscript);
 					resulthash.put("correct", false);
 					testResults.add(new JSONObject(resulthash));
 					continue;
 				}
 
 				String failS = e.getTarget().getMessage();
				
				// Compile and use regular expression to find the expected and received values
				String patternStr = "^expected:<(.*)> but was:<(.*)>$";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(failS);
				if (matcher.find()) {
					resulthash.put("expected", matcher.group(1));
					resulthash.put("received", matcher.group(2));
					resulthash.put("call", testscript);
					resulthash.put("correct", false);
				} else { //if the regular expression fails, use the old method
					failS = failS.replace("expected:<", "");
					failS = failS.replace("> but was:<", ",");
					failS = failS.replace(">", "");
					String[] ss = failS.split(",");
					resulthash.put("expected", ss[0]);
					resulthash.put("received", ss[1]);
					resulthash.put("call", testscript);
					resulthash.put("correct", false);
				}
 				testResults.add(new JSONObject(resulthash));
 				continue;
 			}
 
 			//correct without exception
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
 			resultjson.put("printed", new String(outputBuffer.toByteArray(), Charset.forName("UTF-8")));
 			JSONObject json = new JSONObject(resultjson);
 			strResult.append(json.toString());
 
 
 		return strResult.toString();
 	}
 
 
 	public static void main(String[] args)
 	{
 
 		int a = 1; //
 		assertEquals(a ,1);
 		String userStr = "%7B%22tests%22%3A+%22+%5Cn%3E%3E%3E+b+%5Cn+2%5Cn%5Cn%5Cn%5Cn%5Cn%22%2C+%22solution%22%3A+%22+%5Cnint+b%3D2%3B%5Cn%5Cn%5Ct%5Cn%5Cn%22%7D";
 		//System.out.println(userStr);
 		HashMap hash = new HashMap();
 		hash.put("tests", "assertTrue(false);\n assertEquals(b,2);\n assertEquals(a, 1);");
 		hash.put("solution", "int a=1;\nint b=1;\n\n");
 		JSONObject testObj = new JSONObject(hash);
 		userStr = testObj.toString();
 		try
 		{
 
 			JSONObject jsonObj = new JSONObject(userStr);
 			JavaVerifierServlet instance = new JavaVerifierServlet();
 
 			System.out.println(instance.parseJava(jsonObj.getString("solution"), jsonObj.getString("tests")));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 
 
 	}
 
 }
