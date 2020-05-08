 import java.io.PrintWriter;
 
 import org.tdom.TDom;
 import static org.tdom.TDom.*;
 
 public class Example
 {
     public static void main(String args[])
     {
         PrintWriter printWriter = new PrintWriter(System.out);
 
         TNode html =
             n("html",
               n("head",
                 n("title", t("A title"))),
               n("body",
                 n("div", a("class", "content"),
                   t("Hello, world."))));
 
         // Insert a title before the content div
         html.before(".content", n("h1", t("The Title")));
 
        // html.before(".content", ...) internally runs
         // html.select(".content").before(...)
 
         html.select(".content").dump(printWriter);
         printWriter.println();
 
         html.append("body",
                     n("div", a("class", "content"),
                       t("Goodbye, World")));
 
         html.select(".content").dump(printWriter);
         printWriter.println();
 
         html
             .after(".content", n("hr", a("class", "space")))
 
             .append("head",
                 n("link", a("rel", "stylesheet"),
                   a("href", "css/style.css")))
 
             .dump(printWriter);
 
         printWriter.println();
         printWriter.flush();
     }
 }
