 package fr.ybonnel.csvengine.sample.basic;
 
import fr.ybo.csvengine.CsvEngine;
import fr.ybo.csvengine.exception.CsvErrorsExceededException;
import fr.ybo.csvengine.model.InsertObject;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.util.List;
 
 /**
  * This is the BasicExample for CsvEngine
  */
 public class BasicExample {
 
     /**
      * Create the CsvEngine for the class Dog.
      */
     public static CsvEngine engine = new CsvEngine(Dog.class);
 
     public static void main(String[] args) throws CsvErrorsExceededException {
         List<Dog> dogs = readDogs();
 
         writeDogs(dogs);
 
         readDogsWithHandler();
     }
 
     private static void readDogsWithHandler() throws CsvErrorsExceededException {
         // Use handler on row.
         System.out.println("**************");
         System.out.println("read dogs with handler");
         System.out.println("**************");
         InputStream stream = getCsvFile();
         engine.parseFileAndInsert(new InputStreamReader(stream), Dog.class, new InsertObject<Dog>() {
             public void insertObject(Dog dog) {
                 System.out.println(dog.toString());
             }
         });
         System.out.println();
     }
 
     private static void writeDogs(List<Dog> dogs) {
         // Write dogs into CSV File.
         System.out.println("**************");
         System.out.println("write dogs");
         System.out.println("**************");
         StringWriter writer = new StringWriter();
         engine.writeFile(writer, dogs, Dog.class);
         // Display the content of writer.
         System.out.println(writer.toString());
         System.out.println();
     }
 
     private static List<Dog> readDogs() throws CsvErrorsExceededException {
         // Read the dogs from a CSV File and put it into a list of objects
         System.out.println("**************");
         System.out.println("read dogs");
         System.out.println("**************");
         InputStream csvFile = getCsvFile();
         List<Dog> dogs = engine.parseInputStream(csvFile, Dog.class).getObjects();
         for (Dog dog : dogs) {
             System.out.println(dog.toString());
         }
         System.out.println();
         return dogs;
     }
 
     private static InputStream getCsvFile() {
         return BasicExample.class.getResourceAsStream("/fr/ybonnel/csvengine/sample/basic/dogs.csv");
     }
 
 }
