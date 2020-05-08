 package fr.ybonnel.csvengine.sample.basic;
 
import fr.ybo.csvengine.annotation.CsvColumn;
import fr.ybo.csvengine.annotation.CsvFile;
 
 /**
  * Class representing a Dog.
  * This class is mapped to a CSV File.
  */
 // This annotation is use to declare Dog as a class mapped to a CSV File.
 // You can change default separator with : @CsvFile(separator = ";")
 @CsvFile
 public class Dog {
     // The "name" field is mapped to a column in CSV named "name"
     @CsvColumn("name")
     private String name;
     // The "race" field is mapped to a column in CSV named "race"
     @CsvColumn("race")
     private String race;
     @CsvColumn("proprietary")
     // The "proprietary" field is mapped to a column in CSV named "proprietary"
     private String proprietary;
 
     @Override
     public String toString() {
         return "Dog{" +
                 "name='" + name + '\'' +
                 ", race='" + race + '\'' +
                 ", proprietary='" + proprietary + '\'' +
                 '}';
     }
 }
