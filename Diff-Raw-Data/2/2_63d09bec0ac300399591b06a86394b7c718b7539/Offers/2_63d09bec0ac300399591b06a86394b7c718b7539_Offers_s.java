 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.AbstractMap;
 import java.util.HashMap;
 import java.util.Map;
 
 import javassist.bytecode.Descriptor.Iterator;
 
 import javax.persistence.Query;
 
 import models.Offer;
 import models.Request;
 import models.Tag;
 import models.User;
 import models.Handshake;
 import models.CreditType;
 import service.CreditManager;
 import service.MatchService;
 import service.Utils;
 
 
 import play.db.jpa.JPA;
 
 /**
  * Offers is the controller class that is responsible of handling
  * HTTP requests for offers.
  * <p>
  * Offers includes the following features:
  * <ul>
  * <li>Creating a new offer
  * <li>Modifying details of a specific offer
  * <li>Showing details of a specific offer
  * <li>Listing all offers
  * <li>Searching offers by keywords (e.g. phrase, location)
  * </ul>
  * <p>
  * The class was originally created by last year's "Let It Bee"
  * group members.
  * 
  * @author	Onur Yaman  <onuryaman@gmail.com>
  * @version 2.0
  * @since	1.0
  */
 public class Offers extends BaseController {
 	
 	/**
 	 * Creates a new offer instance (with empty tags) and delegates
 	 * it to the offer creation form template renderer, which in
 	 * turn renders the form.
 	 * 
 	 * @see		Offer
 	 * @see 	ArrayList
 	 * @see		Tag
 	 * @see		Controller
 	 * @see		#renderTemplate
 	 * @since	1.0
 	 */
     public static void create() {
     	Offer offerItem = new Offer();
     	offerItem.tags = new ArrayList<Tag>();
    	CreditType ct = CreditManager.getService(offerItem); 
    	offerItem.credit = (int)ct.requesterSocialPoint;
     	renderTemplate("Offers/form.html", offerItem);
     }
 
     /**
      * Handles the POST request of the offer creation form. It simply
      * gets the user-generated offer details (including the tags) and
      * saves it in the database.
      * <p>
      * After the offer is saved, it forwards the user to the offer
      * details showing page.
      * 
      * @param tags		tags to be attached to the offer
      * @param offerItem the offer instance that will be recorded in
      * 					the database
      * @see				String
      * @see				Offer
      * @see				User
      * @see				Tag
      * @see				List
      * @see				Utils
      * @see				ArrayList
      * @see				#validation
      * @see				#renderTemplate
      * @see				#show
      * @version			2.0
      * @since			1.0
      */
     public static void doCreate(HashMap<String, String> tags, Offer offerItem) {
     	// check whether or not the offer is new.
     	boolean isCreate = offerItem.id == null;
     	
     	// if the offer is not new;
     	if (! isCreate) {
     		// prevent tags to be appended to existing tags
     		// on edit.
     		Tag.delete("offer.id", offerItem.id);
     	}
 
     	// for each tag the user entered;
     	for (Map.Entry<String, String> entry : tags.entrySet()) {
     		// create a new Tag instance.
     		Tag tag = new Tag(offerItem, entry.getKey(), entry.getValue());
     		
     		// tag the offer.
     		offerItem.tags.add(tag);
     	}
     	
     	// make sure that the form is validated.
     	validation.valid(offerItem);
     	
     	// if there are errors;
     	if (validation.hasErrors()) {
     		// render the form view again.
     		renderTemplate("Offers/form.html", offerItem);
     	}
     	
     	// assign the current user to the offer.
     	offerItem.user = getConnectedUser();
     	
     	// save the offer.
     	offerItem.save();
     	
     	// render the offer view.
     	show(offerItem.id, isCreate);
     }
     
     /**
      * Seems like a dummy method. Need to be sure before
      * deleting.
      * 
      * @deprecated
      */
     public static void save(Long offerId) {
     	Offer offerItem = Offer.findById(offerId);
 	offerItem.save();
 	show(offerItem.id, true);
     }
 
     /**
      * Given the id of an offer and whether or not the operation
      * is creation; renders the corresponding view.
      * 
      * @param id		id of the offer
      * @param isCreate  whether or not the operation is creation
      * @see				Long
      * @see				Boolean
      * @see				Offer
      * @see				#render
      * @since			0.1
      */
     public static void show(Long id, Boolean isCreate) {
 	Offer offerItem = Offer.findById(id);
 	Boolean isOldOffer = !isCreate;
 	render(offerItem);
     }
 
     /**
      * Given the id of an offer; renders the corresponding
      * modification view.
      * 
      * @param id		id of the offer
      * @see				Long
      * @see				Offer
      * @see				Boolean
      * @see				#render
      * @since			0.1
      */
     public static void showAfterEdit(Long id) {
 	Offer offerItem = Offer.findById(id);
 	Boolean isOldOffer = true;
 	render(offerItem, isOldOffer);
     }
 
     /**
      * Given the id of an offer; fetches corresponding handshakes
      * and renders the offer details view.
      * 
      * @param id	id of the offer
      * @see			User
      * @see			#getConnectedUser
      * @see			Offer
      * @see			Long
      * @see			Query
      * @see			JPA
      * @see			List
      * @see			Boolean
      * @see			Object
      * @see			Handshake
      * @see			Request
      * @see			AbstractMap
      * @see			HashMap
      * @see			#render
      * @since		0.1
      */
     public static void showDetails(Long id) {
     	User user = getConnectedUser(); // user who is inspecting the offer
     	Offer offerItem = Offer.findById(id); // the offer being inspected
 	User offerOwner = offerItem.user; // owner of the offer
 
 	Long handshakeId = new Long(0L); // variable to store the id of the matched handshake
 	Query handshakeQuery = JPA.em().createQuery("from " + Handshake.class.getName() + " where offer.id=" + offerItem.id); // handshakes which have been initiated with the current offer's id
 	List<Object[]> handshakeList = handshakeQuery.getResultList(); // list of matching handshakes
 
 	Boolean hasApplied = false; // inititate hasApplied boolean to false
 	
 	for(Object singleHandshake : handshakeList) { // iterate over handshakes
 	    Handshake handshakeItem = (Handshake) singleHandshake; // type casting
 	    Request requestItem = handshakeItem.request; // the request belonging to the current iteration's handshake
 	    hasApplied = (requestItem.user == user); // if the user of the request is equal to the current user, set hasApplied to true
 	    if (hasApplied) { // store the matched handhshake's id and break out of the for loop if we know user has applied to the current offer
 		handshakeId = handshakeItem.id;
 		break;
 	    }
 	}
 
 	Query applicationsQuery = JPA.em().createQuery("from " + Handshake.class.getName() + " where offererId=" + offerOwner.id + " and offer_id=" + offerItem.id + " and status='WAITING_APPROVAL'");
 	List<Object[]> applications = applicationsQuery.getResultList();
 	List<Handshake> applicationList = new ArrayList(applications);
 
 	AbstractMap<User, Handshake> userApplications = new HashMap();
 	
 	for (Handshake handshakeItem : applicationList) {
 	    User applicant = User.findById(handshakeItem.requesterId);
 	    userApplications.put(applicant, handshakeItem);
 	}
 
 	Boolean isOfferOwner = (user == offerOwner);
 	Boolean someoneElsesOffer = (user != offerItem.user);
     	render(user, offerItem, offerOwner, someoneElsesOffer, hasApplied, userApplications, isOfferOwner);
     }
 
     /**
      * The search feature will be handled by a 3rd-party solution.
      * This method will be useless.
      * 
      * @deprecated
      */
     public static void search(String phrase, String location, String county_id, String district_id, String reocc, String m1, String t2, String w3, String t4, String f5, String s6, String s7, String tFrom, String tTo) {
     	User user = getConnectedUser();
 
     	if(location == null) location = "0";
     	Query openOffersQuery;   
     	String showFiltered = null;
     	String dayHoursFilter = "";
     	String originalPhrase = phrase;
     	
     	if(phrase != null && phrase.length() > 0)
     	{
     		if(phrase.toUpperCase().contains("ING"))
     		{
     			phrase = phrase.toUpperCase().replace("ING","");
     		}
     	}
     	
     	if(reocc != null && reocc.contains("1"))
     	{
     		dayHoursFilter = " and reoccure = True ";
     		
     		if(m1 != null && m1.contains("on"))
     			dayHoursFilter += " and is_rec_monday = True ";
     		
     		if(t2 != null && t2.contains("on"))
     			dayHoursFilter += " and is_rec_tuesday = True ";
     		
     		if(w3 != null && w3.contains("on"))
     			dayHoursFilter += " and is_rec_wednesday = True ";
     		
     		if(t4 != null && t4.contains("on"))
     			dayHoursFilter += " and is_rec_thursday = True ";
     		
     		if(f5 != null && f5.contains("on"))
     			dayHoursFilter += " and is_rec_friday = True ";
     		
     		if(s6 != null && s6.contains("on"))
     			dayHoursFilter += " and is_rec_saturday = True ";
     		
     		if(s7 != null && s7.contains("on"))
     			dayHoursFilter += " and is_rec_sunday = True ";
     		
     		Integer tFromInt = 0;
     		Integer tToInt = 0;
     		
     		if(tFrom != null && tFrom.length() > 0)
     		{
     			tFromInt = Integer.valueOf(tFrom);
     		}
     		
     		if(tTo!= null && tTo.length() > 0)
     		{
     			tToInt = Integer.valueOf(tTo);
     		}
     		
     		if(tFromInt != tToInt)
     		{
     			dayHoursFilter += " and ((reocc_start_hour_val < " +  tFromInt.toString() + " and reocc_end_hour_val > " + tFromInt.toString() + ")"; 
     			dayHoursFilter += " or (reocc_start_hour_val <" + tToInt.toString() + " and reocc_end_hour_val > " + tToInt.toString() + ")";
     			dayHoursFilter += " or (reocc_start_hour_val >" + tFromInt.toString() + " and reocc_end_hour_val < " + tToInt.toString() + "))";
     		}
     	}
     	
     	if(location.contains("1"))
     	{
     		Query openOffersQueryAll = JPA.em().createQuery("from " + Offer.class.getName() + " where status is 'WAITING'");
     		List<Object[]> openOffersListAll = openOffersQueryAll.getResultList();
     		
     		String addStr = "";
     		
     		if(district_id != null && district_id.length() > 0)
     		{
     			addStr = " and district_id =" + district_id;
     		}
     		else if(county_id != null && county_id.length() > 0)
     		{
     			addStr = " and county_id =" + county_id;
     		}
     		    		
     		openOffersQuery = JPA.em().createQuery("from " + Offer.class.getName() + " where status is 'WAITING' and (is_virtual is null or is_virtual = False) " + addStr + dayHoursFilter);
     		List<Object[]> openOffersList = openOffersQuery.getResultList();
         	List<Offer> allOffers = new ArrayList(openOffersList);
         	
         	List<Offer> foundOffers = MatchService.match(allOffers, phrase);
 
         	if(phrase == null || phrase.length() == 0)
         	{
         		showFiltered= "1";
         		foundOffers = allOffers;
         	}
         	
         	allOffers = new ArrayList(openOffersListAll);
         	
         	phrase = originalPhrase;
        		render(user, foundOffers, allOffers, phrase, location, county_id, district_id, showFiltered, reocc, m1, t2, w3, t4, f5, s6, s7, tFrom, tTo);
     	}
     	else if(location.contains("2"))
     	{   		
     		Query openOffersQueryAll = JPA.em().createQuery("from " + Offer.class.getName() + " where status is 'WAITING'");
     		List<Object[]> openOffersListAll = openOffersQueryAll.getResultList();
     		
     		openOffersQuery = JPA.em().createQuery("from " + Offer.class.getName() + " where status is 'WAITING'"+ " and is_virtual = True" + dayHoursFilter);
     		List<Object[]> openOffersList = openOffersQuery.getResultList();
         	List<Offer> allOffers = new ArrayList(openOffersList);
         	List<Offer> foundOffers = MatchService.match(allOffers, phrase);
 
         	if(phrase == null || phrase.length() == 0)
         	{
         		showFiltered= "1";
         		foundOffers = allOffers;
         	}
         	
         	allOffers = new ArrayList(openOffersListAll);
         	phrase = originalPhrase;
        		render(user, foundOffers, allOffers, phrase, location, county_id, district_id, showFiltered, reocc, m1, t2, w3, t4, f5, s6, s7, tFrom, tTo);
     	}
     	else 
     	{
     		Query openOffersQueryAll = JPA.em().createQuery("from " + Offer.class.getName() + " where status is 'WAITING'");
     		List<Object[]> openOffersListAll = openOffersQueryAll.getResultList();
     		
     		openOffersQuery = JPA.em().createQuery("from " + Offer.class.getName() + " where status is 'WAITING'" + dayHoursFilter);
     		List<Object[]> openOffersList = openOffersQuery.getResultList();
         	List<Offer> allOffers = new ArrayList(openOffersList);
         	List<Offer> foundOffers = MatchService.match(allOffers, phrase);
         	
         	if(phrase == null || phrase.length() == 0)
         	{
         		showFiltered= "1";
         		foundOffers = allOffers;
         	}
         	
         	allOffers = new ArrayList(openOffersListAll);
         	phrase = originalPhrase;
         	render(user, foundOffers, allOffers, phrase, location, county_id, district_id, showFiltered, reocc, m1, t2, w3, t4, f5, s6, s7, tFrom, tTo);
     	}
     }
     
     /**
      * Given the id of an offer; renders the corresponding
      * modification view.
      * 
      * @param id		id of the offer
      * @see				Long
      * @see				Offer
      * @see				Boolean
      * @see				#render
      * @since			0.1
      */
     public static void edit(Long id) {
     	Offer offerItem = Offer.findById(id);
     	renderTemplate("Offers/form.html", offerItem);
     }
 
     /**
      * Finds the offers created by the current logged in user
      * and lists them.
      * 
      * @see		User
      * @see		#getConnectedUser
      * @see		List
      * @see		Offer
      * @see		#render
      * @since	0.1
      */
     public static void list() {
 	User user = getConnectedUser();
 	List<Offer> offers = Offer.find("user.id", user.id).fetch();
 	render(user, offers);
     }
 }
