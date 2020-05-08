 
 // import the WebMacro core interfaces
 import org.webmacro.*;
 
 // import the WebMacro servlet classes
 import org.webmacro.servlet.*;
 
 import org.webmacro.util.*;
 import org.webmacro.profile.*;
 
 /**
   * Profile servlet checks to see if profiling has been enabled 
   * and if it has prints out some statistics about how WebMacro
   * has been performing. Note that to enable profiling you need
   * to do two things: enable it in org.webmacro.Flags and also 
  * in the WebMacro.properties file.<p>
  *
  *
  * ******NOTICE*****
  * To use this example, *you* must enable the #profile directive
  * by adding the following lines to your local WebMacro.properties file.
  * 
  *    Directives.profile: org.webmacro.directive.ProfileDirective
  *    Profile.rate: 0
  *
  * If you don't have a WebMacro.properties file, simply make one
  * and place it in the CLASSPATH of your JDK, servlet container, or
  * web-app.  
  *
   */
 public class Profile extends WMServlet
 {
 
    /**
      * WebMacro creates a WebContext and passes it to your servlet. 
      * The WebContext contains all the information you'll need to 
      * handle the request. It is also the only object you share 
      * with the template. If you add objects to the WebContext via
      * its put(key,value)method,they become available in the 
      * template as variables named after the key.
      */
    public Template handle(WebContext context) throws HandlerException
    {
       try {
          ProfileSystem ps = ProfileSystem.getInstance();
 
          context.startTiming("getProfileCategories");
          ProfileCategory[] pc = ps.getProfileCategories();
          context.stopTiming();
 
          context.put("cats", pc);
 
          context.put("compiled", Flags.PROFILE ? Boolean.TRUE : Boolean.FALSE);
 
          context.startTiming("CallGraph.init");
          if (pc != null) {
              CallGraph[] cg = new CallGraph[ pc.length ];
              for (int i = 0; i < pc.length; i++) {
                cg[i] = new CallGraph(pc[i]);
              }
              context.put("stats", cg);
             context.put("status", "All OK");
          } else {
             context.put("status", "No profiling information");
          }
          context.stopTiming();
       } catch (Exception e) {
          context.put("status", e);
          e.printStackTrace();
       }
       Template view;
       context.startTiming("getTemplate");
       try {
          view = getTemplate("profile.wm");
       } catch (ResourceException e) {
          throw new HandlerException(
             "Profiler was unable to load the template profile.wm: your" 
             + " WebMacro system is not set up properly. Please get "
             + " WebMacro working before trying out the Profiler\n\n"
             + "Here is the actual exception that was raised:\n" + e);
 
       }
       context.stopTiming();
       return view;
    }
 }
 
