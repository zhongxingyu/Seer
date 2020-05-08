 package app;
 
 import models.Fruit;
 import models.Juice;
 import org.milyn.Smooks;
 import org.milyn.container.ExecutionContext;
 import org.milyn.event.report.HtmlReportGenerator;
 import org.milyn.payload.JavaResult;
 import org.milyn.payload.JavaSource;
 import org.xml.sax.SAXException;
 
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.*;
 import java.lang.String;
 import java.util.List;
 
 /**
  * @author Ludovico Fischer
  */
 public class App {
     public static void main(String[] args) throws IOException, SAXException {
         System.out.println("Starting program.");
         List<Fruit> fruits = readFixedWidthFile();
         System.out.println(String.format("We have this many fruits: %s", fruits.size()));
         for (Fruit fruit : fruits) {
             System.out.println(fruit.getFruit());
         }
         System.out.println(writeOutput());
         System.out.println(writeJuiceOutput(new Juice(fruits.get(0))));
 
     }
 
     private static List<Fruit> readFixedWidthFile() throws IOException, SAXException {
         Smooks smooks = new Smooks("smooks-config.xml");
         try {
             JavaResult result = new JavaResult();
            smooks.filterSource(new StreamSource(new File("samples/fruit.txt")), result);
             List<Fruit> fruits = (List<Fruit>) result.getBean("fruits");
             return fruits;
         } finally {
             smooks.close();
         }
 
     }
 
     private static String writeOutput() throws IOException, SAXException {
         Smooks smooks = new Smooks("smooks-config.xml");
         try {
             StringWriter writer = new StringWriter();
            smooks.filterSource(new StreamSource(new File("samples/fruit.txt")), new StreamResult(writer));
             return writer.toString();
         }   finally {
             smooks.close();
         }
     }
 
     public static String writeJuiceOutput(Juice juice) throws IOException, SAXException {
         Smooks smooks = new Smooks("smooks-juice-config.xml");
         try {
             ExecutionContext context = smooks.createExecutionContext();
             context.setEventListener(new HtmlReportGenerator("target/reports/report.html"));
            StringWriter writer = new StringWriter();
             smooks.filterSource(context, new JavaSource(juice), new StreamResult(writer));
             return writer.toString();
         }  finally {
             smooks.close();
         }
     }
 }
