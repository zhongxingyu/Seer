 package templates;
 
 import groovy.lang.Closure;
 import play.mvc.Router;
 import play.templates.FastTags;
 import play.templates.GroovyTemplate.ExecutableTemplate;
 
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Custom tags for use in templates.
  */
 public class PrimesTags extends FastTags {
 
    /**
     * A simple navigation navigation menu.
     */
    public static void _navigation2(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int line) {
       final String html = "<p class=\"navigation\"><a href=\"%s\">Template examples</a>: %s</p>";
       final String url = Router.reverse("Application.index").url;
       final String title = args.get("title").toString();
       out.print(String.format(html, url, title));
    }
 
    /**
     * A page heading for a list of prime numbers.
     */
    public static void _heading2(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int line) {
       final String html = "<h1>The first %d prime numbers</h1>";
      final Integer length = (Integer) (args.containsKey("length") ? args.get("length") : args.get("arg"));
       out.print(String.format(html, length));
    }
 
    /**
     * Output a comma-separated list of
     */
    public static void _list2(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int line) {
       final Collection<?> items = (Collection<?>) args.get("arg");
       int index = 1;
 
       for (Iterator<?> iterator = items.iterator(); iterator.hasNext(); ) {
          out.print("<span>");
          final Object prime =  iterator.next();
          out.print(String.format("<i>p</i><sub>(%d)</sub> = %d", index, prime));
 
          if (items.iterator().hasNext()) {
             out.print(" , ");
          }
 
          out.println("</span>");
          index++;
       }
    }
 }
