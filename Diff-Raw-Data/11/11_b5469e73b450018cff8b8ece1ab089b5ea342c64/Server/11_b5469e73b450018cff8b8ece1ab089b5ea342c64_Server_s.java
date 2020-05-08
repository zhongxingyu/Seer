 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package FP;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 import no.ntnu.fp.net.admin.Log;
 import no.ntnu.fp.net.co.Connection;
 import no.ntnu.fp.net.co.ConnectionImpl;
 import sun.misc.BASE64Decoder;
 import sun.misc.BASE64Encoder;
 
 /**
  *
  * @author lycantrophe
  */
 public class Server {
 
     public static Map<String, Person> persons;
     public static Map<String, Location> locations;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
 
         // Create log
         Log log = new Log();
         log.setLogName("Server-side application");
 
         buildAllObjects();
 
         // server connection instance, listen on port 5555
         Connection server = new ConnectionImpl(5555);
         // each new connection lives in its own instance
        Connection conn;
         // TODO: Thread at this point?
 
 
         while (true) {
             try {
                conn = server.accept();
 
                 Thread thread = new ServerThread(conn);
                thread.run();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private static void buildAllObjects() {
 
         System.out.println("Building objects!");
         Query query = new Query();
         persons = new HashMap<String, Person>();
         // Get persons
         for (Person other : query.getPersons()) {
            // User.addPerson(other);
             persons.put(other.getUsername(), other);
         }
 
         // Get locations
         locations = new HashMap<String, Location>();
         for (Location location : query.getLocations()) {
             locations.put(location.getName(), location);
         }
 
         // Create appointments
 
         query.createAllAppointments(persons, locations);
         query.close();
     }
 
     public static String Serialize(Object object) throws IOException {
 
         BASE64Encoder encode = new BASE64Encoder();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(baos);
         out.writeObject(object);
         out.flush();
         return encode.encode(baos.toByteArray());
     }
 
     public static Object Deserialize(String input) throws IOException, ClassNotFoundException {
 
         BASE64Decoder decode = new BASE64Decoder();
         ByteArrayInputStream bis = new ByteArrayInputStream(decode.decodeBuffer(input));
         ObjectInputStream ois = new ObjectInputStream(bis);
         return ois.readObject();
     }
 }
