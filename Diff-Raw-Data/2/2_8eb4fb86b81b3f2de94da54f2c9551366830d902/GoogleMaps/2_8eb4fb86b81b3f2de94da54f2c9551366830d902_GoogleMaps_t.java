 package model;
 
 import java.awt.Image;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 import controller.DeliveryController;
 
 
 /**
  * A class for the Google Maps API
  * 
  * @author Ole J. Pettersen
  *
  */
 
 public class GoogleMaps extends JFrame {
 	
 	/**
 	 * Returns a map as a JLabel showing the delivery address for a specific order.
 	 * 
 	 * @param input
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public static JLabel map(String input) throws MalformedURLException, IOException {
 		String address = 
 				"http://maps.googleapis.com/maps/api/staticmap?markers=size:mid%7Ccolor:red%7C|"
 				+ validateAddress(input)
 				+ "&zoom=14&size=350x350&maptype=roadmap&&sensor=false";
 		
 		Image image = ImageIO.read(new URL(address));
 		JLabel label = new JLabel(new ImageIcon(image));
 
 		return label;
 	}
 	
 	
 	/**
 	 * Runs a few tests and stuff to format the String after the Google Maps API's liking. 
 	 * Otherwise it gets sad :(
 	 * 
 	 * @param input
 	 */
 	public static String validateAddress(String input) {
 		String output = "";
 		
 	
 		
 		for (int i = 0; i < input.length(); i++) {
 			
 			int value = (char)input.charAt(i);
 			
 			if ((value >= 65) && (value <= 93))
 				output += input.charAt(i);
 			else if ((value >= 97) && (value <= 125))
 				output += input.charAt(i);
 			else if ((value >= 48) && (value <= 57))
 				output += input.charAt(i);
 			else if (value == 32)
 				output += "%20";
 		}
 			
 		
 		return output;
 		
 	}
 	
 	
 	/**
 	 * Returns a map as a JLabel showing the delivery address for all orders.
 	 * 
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public static JLabel mainMap() throws MalformedURLException, IOException {
 		String marker = "&markers=color:red%7C";
 		String address = "";
 		ArrayList<String> addressList = DeliveryController.getAddressForMap();
 		
 		
 		if (addressList.size() == 0) {
			address = "http://maps.googleapis.com/maps/api/staticmap?center=Trondheim&zoom=12&size=380x455&sensor=false";
 		}
 		
 		else {
 		
 			address = 
 					"http://maps.googleapis.com/maps/api/staticmap?center=Trondheim%20Norge&zoom=12&size=380x455&maptype=roadmap&markers=color:red%7C"
 					+ validateAddress(addressList.get(0));
 			
 				for (int i = 1; i < addressList.size(); i++) {
 					address += marker;
 					address += validateAddress(addressList.get(i));
 				}
 					
 				address += "&sensor=false";
 		}
 				
 		Image image = ImageIO.read(new URL(address));
 		JLabel label = new JLabel(new ImageIcon(image));
 
 		return label;
 	}
 }
