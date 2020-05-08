 package se.chalmers.h_sektionen.containers;
 
 import java.util.List;
 
 /**
  * A container class that contains information used in InfoActivity.
  * The meaning of this class is that, LoadData should fill it, and 
  * InfoActivity should later use it.
  * 
  * @author robin
  *
  */
 public class InfoContainer {
 	
 	private List<ContactCard> contactCards = null;
 	private String htmlLinks = null;
 	private String openingHoursString = null;
 	
 	/**
 	 * Constructor that initializes the variables.
 	 * 
 	 * @param contactCards A list of ContactCard objects
 	 * @param htmlLinks A HTML formed string containing links
 	 * @param openingHoursString A HTML formed string containing opening hours
 	 */
 	public InfoContainer(List<ContactCard> contactCards, String htmlLinks, String openingHoursString) {
 		this.contactCards = contactCards;
 		this.htmlLinks = htmlLinks;
 		this.openingHoursString = openingHoursString;
 	}
 	
 	/**
 	 * 
 	 * @return The contactCards variable
 	 */
 	public List<ContactCard> getContactCards() {
 		return contactCards;
 	}
 	
 	/**
 	 * 
 	 * @return The htmlLinks variable
 	 */
 	public String getHtmlLinks() {
 		return htmlLinks;
 	}
 	
 	/**
 	 * 
 	 * @return The openingHoursString variable
 	 */
 	public String getOpeningHoursString() {
 		return openingHoursString;
 	}
 	
 }
