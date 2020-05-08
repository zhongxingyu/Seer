 package app;
 
 import models.Fruit;
 import org.milyn.Smooks;
 import org.milyn.payload.JavaResult;
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
     }
 
     private static List<Fruit> readFixedWidthFile() throws IOException, SAXException {
         Smooks smooks = new Smooks("smooks-config.xml");
         try {
             JavaResult result = new JavaResult();
             smooks.filterSource(new StreamSource(new File("fruit.txt")), result);
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
            smooks.filterSource(new StreamSource(new File("fruit.txt")), new StreamResult(writer));
             return writer.toString();
        }   finally {
             smooks.close();
         }
     }

 }
