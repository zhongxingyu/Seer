 package ibis.in.tum.de;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 public class Sample {
 
 	public Sample() throws JAXBException {
 
 		// Create a new user object
 		User user = new User();
 		user.setAge(32);
 		user.setPassword("password");
 		user.setUsername("username");
 		user.setSingature("signature ");
 
 		ArrayList<String> hobbies = new ArrayList<String>();
 		hobbies.add("tennis");
 		hobbies.add("reading");
 		user.setHobbies(hobbies);
		
 		ArrayList<Integer> values = new ArrayList<Integer>();
		for(int i=0; i<42; i++)
			values.add(i); 
		user.setList(values); 
 
 		// JAXB
 		JAXBContext jc = JAXBContext.newInstance(User.class);
		
 		// Marshaller and Unmarshaller
 		Unmarshaller u = jc.createUnmarshaller();
 		Marshaller m = jc.createMarshaller();
		
 		// Marshal (Serialize)
 		StringWriter writer = new StringWriter();
 		m.marshal(user, writer);
 		String s = writer.toString();
 		System.out.println("Marshalled XML: " + s);
 
 		// Unmarshal (Deserialize)
 		User duser = (User) u.unmarshal(new StringReader(s));
 		System.out.println("Username: " + duser.getUsername());
 	}
 
 	public static void main(String arg[]) {
 		try {
 			new Sample();
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
