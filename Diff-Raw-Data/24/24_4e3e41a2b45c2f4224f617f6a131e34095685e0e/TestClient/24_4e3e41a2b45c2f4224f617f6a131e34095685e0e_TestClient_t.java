 package is.idega.idegaweb.pheidippides.webservice.client;
 
 import java.net.URL;
 
 public class TestClient {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		TestClient client = new TestClient();
 		client.doStuff();
 	}
 
 	private void doStuff() {
 		try {
 			CharityServiceServiceLocator locator = new CharityServiceServiceLocator();
			CharityService port = locator.getCharityService(new URL("http://pheidippidestest.sidan.is/services/CharityService"));
 			
 			//Session session = port.authenticateUser("isb", "ch4r1tys3rv1c3");//getCharities();//getCharityInformation("0610703899");
			CharityInformation charity = port.getCharityInformation("0610703899");
 
 /*			if (info != null) {
 				for (Charity charity : info) {
 					System.out.println(charity.getId() + ", " + charity.getName() + ", " + charity.getDescription());
 				}
 			}*/
 			
 			//System.out.println("session = " + session.getSessionID());
 			if (charity == null) {
 				System.out.println("no charity");				
 			} else {
 				System.out.println("address = " + charity.getAddress());
 				System.out.println("charity id = " + charity.getCharityID());
 				System.out.println("charity name = " + charity.getCharityName());
 				System.out.println("city = " + charity.getCity());
 				System.out.println("country = " + charity.getCountry());
 				System.out.println("distance = " + charity.getDistance());
 				System.out.println("email = " + charity.getEmail());
 				System.out.println("gender = " + charity.getGender());
 				System.out.println("mobile = " + charity.getMobile());
 				System.out.println("name = " + charity.getName());
 				System.out.println("nationality = " + charity.getNationality());
 				System.out.println("personal id = " + charity.getPersonalID());
 				System.out.println("phone = " + charity.getPhone());
 				System.out.println("postal code = " + charity.getPostalCode());
 			}
			
			//Charity charity = port.getCharity("0610703899");
			//if (charity != null) {
			//	System.out.println("Got something");
			//}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
