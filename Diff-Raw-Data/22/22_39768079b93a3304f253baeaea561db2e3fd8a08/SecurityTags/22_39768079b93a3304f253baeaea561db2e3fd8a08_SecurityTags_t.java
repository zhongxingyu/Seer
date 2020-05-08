 package templates;
 
 import groovy.lang.Closure;
 
 import java.io.PrintWriter;
 import java.util.Map;
 
 import controllers.Security;
 
 import play.mvc.Http;
 import play.templates.FastTags;
 import play.templates.GroovyTemplate.ExecutableTemplate;
 import play.templates.TagContext;
 
 public class SecurityTags extends FastTags {
 
 	public static void _ifLoggedIn(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
 		if (Security.isConnected()) {
 			body.call();
 			TagContext.parent().data.put("_executeNextElse", false);
 		} else {
 			TagContext.parent().data.put("_executeNextElse", true);
 		}
 	}
 	
 	public static void _userEmailAddress(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
 		if (Security.isConnected()) {
 			out.write(Security.connected());
 		}
 	}
 	
 	public static void _loginLink(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		out.print("<a href='/login'>Login</a>");
	}
	
	public static void _logoutLink(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		out.print("<a href='/logout'>Logout</a>");
 	}
 }
