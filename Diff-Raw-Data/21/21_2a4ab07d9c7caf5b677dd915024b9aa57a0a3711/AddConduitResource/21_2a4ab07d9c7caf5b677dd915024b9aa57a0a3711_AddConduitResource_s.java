 package com.cj.scmconduit.server;
 
 import org.httpobjects.HttpObject;
 import org.httpobjects.Request;
 import org.httpobjects.Response;
 
 public class AddConduitResource extends HttpObject {
 	public interface Listener {
 		void addConduit(ConduitType type, String name, String p4Path, Integer p4FirstCL);
 	}
 	private final Listener listener;
 	
 	public AddConduitResource(Listener listener) {
 		super("/admin");
 		this.listener = listener;
 	}
 
 	@Override
 	public Response get(Request req) {
 		return OK(Html(
 				"<html><body>" +
 				"	<form action=\"/admin\" method=\"post\" >" +
 				"		<div>Name: <input type=\"text\" name=\"name\"/></div>" +
 				"		<div>Perforce Path: <input type=\"text\" name=\"p4path\"/></div>" +
				"		<div>Fetch History Since: <input type=\"text\" name=\"p4FirstCL\"/></div>" +
 				"		<div>Conduit Type: <select name=\"type\">" +
 				"			<option>" + ConduitType.BZR + "</option>" + 
 				"			<option>" + ConduitType.GIT + "</option>" + 
 				"		</div>" +
 				"		<input type=\"submit\" value=\"Add\">" +
 				"	</form>" +
 				"</body></html>"));
 	}
 	
 	@Override
 	public Response post(Request req) {
 		try{
 			ConduitType type = ConduitType.valueOf(req.getParameter("type"));
 			String name = req.getParameter("name");
 			String p4path = req.getParameter("p4path");
 			Integer p4FirstCL = Integer.parseInt(req.getParameter("p4FirstCL"));
 			
 			if(name.contains("-")){
 				return OK(Text("Sorry, conduit names must not contain hyphens"));
 			}else{
 				listener.addConduit(type, name, p4path, p4FirstCL);
				return OK(Text("Created a " + type + " conduit named \"" + name + "\" to " + p4path));
 			}
 			
 		}catch(Exception e){
 			return OK(Text("Sorry, there was an error: " + toString(e)));
 		}
 	}
 	
 
 	private static String toString(Throwable t) {
 		final StringBuffer text = new StringBuffer(t.getClass().getName());
 		final String message = t.getMessage();
 		if(message!=null){
 			text.append(": ");
 			text.append(message);
 		}
 		
 		for(StackTraceElement next : t.getStackTrace()){
 			text.append("\n    at " + next.getClassName() + "." + next.getMethodName() + "(" + next.getFileName() + ":" +  next.getLineNumber() + ")");
 		}
 		return text.toString();
 	}
 }
