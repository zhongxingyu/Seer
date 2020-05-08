 package uk.co.brotherlogic.jarpur;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import uk.co.brotherlogic.jarpur.replacers.SimpleReplacer;
 
 /**
  * Front of house deals with request and routes them to the relevant controller
  * 
  * @author sat
  * 
  */
 public class FrontOfHouse extends HttpServlet {
 
 	public FrontOfHouse() {
 	}
 
 	public static void main(String[] args) {
 		FrontOfHouse foh = new FrontOfHouse();
 	}
 
 	@Override
 	public void doGet(HttpServletRequest req, HttpServletResponse res)
 			throws IOException, ServletException {
 
 		//Get the path and drop off the leading / and add a trailing one if not already there
 		String path = req.getPathInfo().substring(1);
 		if (!path.endsWith("/"))
 			path = path + "/";
 		
 		System.err.println("SEARCH:" + path + ":");
 		
 		//Start searching for the appropriate class
 		List<String> params = new LinkedList<String>();
 		String page = search(path,params,req.getParameterMap());
 		
 		//Write out the result
 		OutputStream os = res.getOutputStream();
 		PrintStream ps = new PrintStream(os);
 		ps.print(page);
 		os.close();
 	}
 	
 	private String capitalize(String in)
 	{
 		return Character.toUpperCase(in.charAt(0)) + in.substring(1);
 	}
 	
 	private String search(String path, List<String> params, Map<String,String> paramMap)
 	{
 		String className = JarpurProperties.get("base") + "." + path.replace("/", ".");
 		while (className.endsWith("."))
 			className = className.substring(0,className.length()-1);
 		System.err.println("PROB:" + className);
 		
 		//Build on Default
 		String defaultClass = className.trim() + ".Default";
 		String res = build(defaultClass,params,paramMap);
 		if (res != null)
 			return res;
 		
 		String[] pathElems = path.split("/");
 		System.err.println("CLASS = " + JarpurProperties.get("base"));
 		className = JarpurProperties.get("base") + "." + path.substring(0,path.length()-pathElems[pathElems.length-1].length()-1).replace("/", ".");
 		String nClass = className.trim() + capitalize(pathElems[pathElems.length-1]);
 		res = build(nClass,params,paramMap);
 		if (res != null)
 			return res;
 		
 		if (pathElems.length > 0)
 		{
 			params.add(pathElems[pathElems.length-1]);
 			return search (path.substring(0,path.length()-pathElems[pathElems.length-1].length()-1),params,paramMap);
 		}
 		
 		//Try down a bit
 		return "";
 	}
 	
 	private String build(String className, List<String> params, Map<String,String> paramMap)
 	{
 		try
 		{
 			System.err.println("BUILDING: " + className);
 			Class cls = Class.forName(className);
 			Page pg = (Page)cls.getConstructor(new Class[0]).newInstance(new Object[0]);
 			return pg.generate(params, paramMap);
 		}
 		catch (Exception e)
 		{
 			System.err.println("SKIPPING:" + className + " (" + e.getLocalizedMessage() + ")");
 		}
 		
 		return null;
 	}
 }
