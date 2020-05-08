 package info.emptybrain.myna;
 
 import java.io.*;
 import java.net.URI;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import org.mozilla.javascript.*;
 import java.util.*;
 import java.util.regex.*;
 import java.sql.*;
 import org.apache.jcs.*;
 import org.apache.jcs.engine.behavior.*;
 import org.apache.commons.pool.impl.*;
 import org.apache.commons.dbcp.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory; 
 
 /**
 * This class handles calling MynaThreads from the commandline
 *
 */
 public class JsCmd{
 	public static File mynaRoot;
 	public static void main(String args[]) {
 		long start =System.currentTimeMillis();
 		MynaThread thread = null;
 		if (args.length == 0) {
 			System.err.println("JS file path is required.");
 			System.exit(1);
 		}
 		File jsFile = new File(args[0]);
 		if (!jsFile.exists()) {
 			System.err.println(args[0] + " does not exist");
 			System.exit(1);
 		}
 		try{
 			mynaRoot=new File(JsCmd.class.getResource(".").toURI().resolve("../../../../../"));
 			thread = new MynaThread();
 			thread.rootDir = mynaRoot.toURI().toString();
 			thread.loadGeneralProperties();
 			thread.environment.put("isCommandline",true);
 			thread.environment.put("commandlineArguments",args);
 			
 			thread.handleRequest(jsFile.toURI().toString());
 			
 			System.out.print(thread.generatedContent);
 			String exitCode = (String)thread.environment.get("exitCode");
 			if (exitCode == null){
 				System.exit(0);
 			} else if (!exitCode.equals("-1")){
 				System.exit(Integer.parseInt(exitCode));
 			}
 			
 		} catch (Exception e){
 			System.err.println("============== Error ============");
 			System.err.println(e.toString());
 			System.err.println("============== Stacktrace ============");
 			e.printStackTrace(System.err);
 			System.exit(1);
 		} finally{
 			if (thread != null)	System.out.print(thread.generatedContent);
 		}
 	}
 	
 }
