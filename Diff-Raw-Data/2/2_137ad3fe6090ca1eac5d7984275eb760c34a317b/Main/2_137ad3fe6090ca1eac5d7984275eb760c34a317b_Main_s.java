 package jade2j;
 
 import de.neuland.jade4j.Jade4J;
 import de.neuland.jade4j.JadeConfiguration;
 import de.neuland.jade4j.template.JadeTemplate;
 import org.apache.commons.lang3.time.StopWatch;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Main {
     public static void main(String[] args) throws Exception {
         List<Book> books = new ArrayList<Book>();
         books.add(new Book("The Hitchhiker's Guide to the Galaxy", 5.70, true, "0"));
         books.add(new Book("Life, the Universe and Everything", 5.60, false, "N"));
         books.add(new Book("The > Restaurant at the < End of the Universe & all", 5.40, true, "Yes"));
 
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("books", books);
         model.put("pageName", "My Bookshelf");
 
         JadeConfiguration jadeConfiguration = new JadeConfiguration();
         jadeConfiguration.setPrettyPrint(true);
         jadeConfiguration.setMode(Jade4J.Mode.XML);
 
        JadeTemplate template = jadeConfiguration.getTemplate("/Users/graemel/IdeaProjects/jade2j/src/main/java/jade2j/index.jade");
 
         StopWatch stopWatch = new StopWatch();
 
         stopWatch.start();
         String result = jadeConfiguration.renderTemplate(template, model);
         stopWatch.stop();
 
         System.out.println(result);
         System.out.println(stopWatch.toString());
     }
 }
