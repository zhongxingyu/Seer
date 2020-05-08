 package itu.dk.smds.e2012.common;
 import java.io.*;
 import java.util.ListIterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 
 /**
 *
  * @author GIGAR
  */
 
 public class CalSerializer {
 
     public static void main(String args[]) throws IOException {
         try {
 
             // assign path to the cal Xml, 
             String path = System.getProperty("user.dir") + "/web/WEB-INF/task-manager-xml.xml";
 
             // create an instance context class, to serialize/deserialize.
             JAXBContext jaxbContext = JAXBContext.newInstance(Cal.class);
 
             // Create a file input stream for the cal Xml.
             FileInputStream stream = new FileInputStream(path);
 
             // deserialize cal xml into java objects.
             Cal cal = (Cal) jaxbContext.createUnmarshaller().unmarshal(stream);
 
 
             // Iterate through the collection of object and print each object in the form of Xml to console.
             ListIterator<User> listIterator = cal.users.listIterator();
             
             System.out.println("Printing user objects serailized into Xml");
             
             
             while (listIterator.hasNext()) {
 
                 PrintUserObject(listIterator.next());
 
             }
             //same as above, but with tasks
             ListIterator<Task> listIterator2 = cal.tasks.listIterator();
             while (listIterator2.hasNext()) {
 
                 PrintTaskObject(listIterator2.next());
 
             }
             
 
             // Serialize cal object into xml.
             
             StringWriter writer = new StringWriter();
 
             // We can use the same context object, as it knows how to 
             //serialize or deserialize Cal class.
             jaxbContext.createMarshaller().marshal(cal, writer);
 
             
             System.out.println("Printing serialized cal Xml before saving into file!");
             
             // Print the serialized Xml to Console.
             System.out.println(writer.toString());
             
             
             // Finally save the Xml back to the file.
             SaveFile(writer.toString(), path);
 
 
 
         } catch (JAXBException ex) {
             Logger.getLogger(CalSerializer.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public static void makeXmlFile(Cal cal) throws IOException{
         try {
 
             // assign path to the Xml, 
             String path = System.getProperty("user.dir") + "/web/WEB-INF/task-manager-xml.xml";
 
             // create an instance context class, to serialize/deserialize.
             JAXBContext jaxbContext = JAXBContext.newInstance(Cal.class);
 
           
             // Serialize cal object into xml.
             
             StringWriter writer = new StringWriter();
 
             // We can use the same context object, as it knows how to 
             //serialize or deserialize Cal class.
             jaxbContext.createMarshaller().marshal(cal, writer);
 
             
             System.out.println("Printing serialized cal Xml before saving into file!");
             
             // Print the serialized Xml to Console.
             System.out.println(writer.toString());
             
             
             // Finally save the Xml back to the file.
             SaveFile(writer.toString(), path);
 
 
 
         } catch (JAXBException ex) {
             Logger.getLogger(CalSerializer.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     private static void PrintUserObject(User user) {
 
         try {
             
             StringWriter writer = new StringWriter();
 
             // create a context object for User Class
             JAXBContext jaxbContext = JAXBContext.newInstance(User.class);
 
             // Call marshal method to serialize User object into Xml
             jaxbContext.createMarshaller().marshal(user, writer);
 
             System.out.println(writer.toString());
 
         } catch (JAXBException ex) {
             Logger.getLogger(CalSerializer.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
     
     private static void PrintTaskObject(Task task) {
 
         try {
             
             StringWriter writer = new StringWriter();
 
             // create a context object for Task Class
             JAXBContext jaxbContext = JAXBContext.newInstance(Task.class);
 
             // Call marshal method to serialize Task object into Xml
             jaxbContext.createMarshaller().marshal(task, writer);
 
             System.out.println(writer.toString());
 
         } catch (JAXBException ex) {
             Logger.getLogger(CalSerializer.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     private static void SaveFile(String xml, String path) throws IOException {
 
 
         File file = new File(path);
 
         // create a bufferedwriter to write Xml
         BufferedWriter output = new BufferedWriter(new FileWriter(file));
 
         output.write(xml);
 
         output.close();
     }
 }
