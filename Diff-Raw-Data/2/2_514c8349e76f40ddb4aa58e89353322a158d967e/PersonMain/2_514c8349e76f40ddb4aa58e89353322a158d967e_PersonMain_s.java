 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import model.Person;
 
 public class PersonMain
 {
 
     private static final String PERSON_XML = "./person-jaxb.xml";
 
     public static void main(String[] args) throws JAXBException, IOException
     {
         // create person
         Person person1 = new Person();
         person1.setName("Thomas");
         person1.setAge(35);
         person1.setAddress("Via Malpensada 140");
 
         // create JAXB context and instantiate marshaller
         JAXBContext context = JAXBContext.newInstance(Person.class);
         Marshaller m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 
         // Write to System.out
         m.marshal(person1, System.out);
 
         // Write to File
         m.marshal(person1, new File(PERSON_XML));
 
         // get variables from our xml file, created before
         System.out.println();
         System.out.println("Output from our XML File: ");
         Unmarshaller um = context.createUnmarshaller();
         Person person2 = (Person) um.unmarshal(new FileReader(PERSON_XML));
        System.out.println("Person: " + person2.getName() + " " + person2.getAge() + " year old and from " + person2.getAddress());
     }
 } 
