 package prosjekt.guests;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import prosjekt.IStorable;
 import prosjekt.Main;
 import prosjekt.utils.Utils;
 
 /**
  *
  * This is the class implementing the guest registry.
  * This class is responsible for keeping track of all guests at the hotel, current and previous guests alike.
  * 
  * The list is loaded from the file 'guestRegistry.json' and is saved whenever modifications are done, such as adding or removing a guest.
  *  
  * @author Kristoffer Berdal <web@flexd.net>
  * @since 2012-04-16
  */
 public class GuestRegistry implements IStorable {
 
   /**
    * This holds the guests!
    */
   private HashMap<String, AbstractGuest> list = new HashMap<String, AbstractGuest>();
   /**
    * This holds any errors produced since the last time we called getErrors();
    */
   private StringBuilder errors;
 
   /**
    * This is the constructor for the GuestRegistry.
    * This runs the method init() which either loads the registry from file (if it exists) or creates a sane default.
    *
    */
   public GuestRegistry() {
     init();
   }
 
   /**
    *
    * This is the method init()
    * This method either loads the list from the file 'guestRegistry.json' or creates sane defaults before saving it.
    * This is also where we create our guests for demo purposes.
    *
    */
   @Override
   public final void init() {
     if (Utils.fileExists("guestRegistry.json")) {
       load();
     } else {
       // Default values
       ArrayList<AbstractGuest> guests = new ArrayList<AbstractGuest>();
       // 10 people
       guests.add(new Person("Even", "Augdal", "1234567822", "Skoleveien 1", "1002"));
       guests.add(new Person("Kristoffer", "Berdal", "93828106", "Skoleveien 2", "1000"));
       guests.add(new Person("Ole", "Hansen", "38383838", "Skoleveien 5", "1000"));
       guests.add(new Person("Ole", "Augdal", "99999999", "En vei 2", "4000"));
       guests.add(new Person("Jens", "Knutsen", "48586949", "Skoleveien 389", "5000"));
       guests.add(new Person("Hanna", "Jonsen", "13131313", "Byveien 2", "2222"));
       guests.add(new Person("Even", "Halvorsen", "14949494", "Skogveien 50", "7070"));
       guests.add(new Person("Tommy", "Nyrud", "50392342", "Fjellveien 3", "5883"));
       guests.add(new Person("Lise", "Olsen", "16969482", "Skoleveien 19", "1000"));
       guests.add(new Person("Marie", "Olsen", "15838292", "Skoleveien 19", "1000"));
 
       // 5 companies
       guests.add(new Company("Sergey", "Brin", "10203040", "Ampfitheatre Parkway", "1600", "Google"));
       guests.add(new Company("Steve", "Jobs", "30405060", "Infinite Loop 1", "1337", "Apple"));
       guests.add(new Company("Steve", "Balmer", "92939106", "Microsoft road 1", "0101", "Microsoft"));
       guests.add(new Company("Jeff", "Bezos", "30284020", "Amazonas 1", "1593", "Amazon"));
       guests.add(new Company("Paul", "Graham", "13371337", "San Franscisco road 1", "4022", "YCombinator"));
 
       // Loop through and add them all.
       for (AbstractGuest g : guests) {
         add(g);
       }
     }
   }
   
   /**
    *
    * This is the method save()
    * This method saves the list to the file 'guestRegistry.json'
    *
    */
   @Override
   public void save() {
     Utils.save(list, "guestRegistry.json");
   }
   
   /**
    *
    * This is the method load()
    * This method loads the list from the file 'guestRegistry.json'
    *
    */
   @Override
   public void load() {
     list = (HashMap<String, AbstractGuest>) Utils.load("guestRegistry.json");
   }
 
   /**
    * This method creates a very simple hash based on the guests first name, last name and phoneNumber.
    * In the future this should be replaced with a better solution.
    * 
    * @param AbstractGuest guest
    * @return String "hash" based on guest.
    * 
    */
   public String getHash(AbstractGuest guest) {
     StringBuilder output = new StringBuilder();
     // This is not the best way of making a hash, and should probably be refactored in a future version. Changing a guests details will result in a new hash, which means that guests bookings and everything will need to be changed to the new hash.
 
     // Fetch the first name, last name and phone number to create a unique "hash" to identify a guest.
     output.append(guest.getFirstName());
     output.append(guest.getLastName());
     output.append(guest.getPhoneNumber());
     return output.toString();
   }
   
   /**
    * This method adds a guest to the guestregistry.
    * Before adding the guest the method checks if 
    * the guest is already in the list and if the
    * guest is valid.
    * 
    * @param guest the guest to add.
    * @return true or false based on success.
    */
   public boolean add(AbstractGuest guest) {
     if (exists(guest)) {
       return false;
     }
     if (!guest.validate()) {
       Utils.showWarningMessage(null, "Gjesten er ikke gyldig! " + guest.getFirstName() + " " + guest.getLastName() + "\n" + "Feilmelding: " + guest.getErrors(), "Validerings feil!");
       return false;
     }
     String hash = getHash(guest);
     list.put(hash, guest);
     save();
     return true;
   }
   /**
    * This method returns the list of guests.
    * 
    * @return HashMap<String,AbstractGuest> of all guests in the list.
    */
   public HashMap<String, AbstractGuest> getList() {
     return list;
   }
   /**
    * This method removes a guest from the list
    * It takes one parameter, which is the guest we want to remove.
    * 
    * @param guest the guest we want to remove from the list
    * @return true or false based on result.
    */
   public boolean remove(AbstractGuest guest) {
     boolean result = false;
     String hash = getHash(guest);
     if (list.remove(hash) != null) {
       result = true;
       save();
      //Main.bookingRegistry.removeGuestBookings(guest); // Remove all of this guests bookings.
     }
     else {
       errors.append("Denne gjesten finnes ikke!");
       result = false;
     }
     return result;
   }
   /**
    * 
    * This method gets a guest from the list (if it exists) based on the guests first name, last name and phone Number.
    * The methods generates a hash based on the parameters and fetches the guest.
    * 
    * @param firstName the first name of the guest
    * @param lastName  the last name of  the guest
    * @param phoneNumber the phone number of the guest
    * @return AbstractGuest guest or null if the guest does not exist.
    */
   public AbstractGuest getGuest(String firstName, String lastName, String phoneNumber) {
     //TODO: Normaliser telefonnummer med regulært utrykk: Hvis noen skriver f.eks 93 82 81 06 må vi kunne matche det mot 93828106 osv.
 
     String hash = firstName + lastName + phoneNumber;
     return list.get(hash);
   }
   
   /**
    * @param guest Guest to find
    * @returns AbstractGuest guest
    */
   public AbstractGuest getGuest(AbstractGuest guest) {
     return list.get(getHash(guest));
   }
   
   /**
    * This method returns a guest with the guestID==i.
    * 
    * @param guestID guestID of the guest we want.
    * @return AbstractGuest guest or null if the guest does not exist.
    */
   public AbstractGuest getGuest(int guestID) {
     for (AbstractGuest g : list.values()) {
       if (g.getID() == guestID) {
         return g;
       }
     }
     return null;
   }
 
   /**
    * This method checks if the guestRegistry contains a specific guest.
    * 
    * @param guest The guest to check for.
    * @return true or false.
    */
   public boolean exists(AbstractGuest guest) {
     return list.containsKey(getHash(guest));
   }
 
   /**
    * 
    * This method searches through the list based on incoming parameters.
    * It is used to search for guests in the administration interface.
    * 
    * 
    * @param firstName The guests first name
    * @param lastName The guests last name
    * @param phoneNumber The guests phone Number
    * @param address The guests address
    * @param postNumber The guests post number.
    * 
    * @returns an ArrayList of matching guests or null if there is no matches.
    */
   public ArrayList<AbstractGuest> searchGuests(String firstName, String lastName, String phoneNumber, String address, String postNumber, String company) {
     ArrayList<AbstractGuest> matches = new ArrayList();
 
     for (AbstractGuest g : list.values()) {
       if (g.getFirstName().toLowerCase().contains(firstName.toLowerCase()) && g.getLastName().toLowerCase().contains(lastName.toLowerCase())
               && g.getPhoneNumber().toLowerCase().contains(phoneNumber.toLowerCase()) && g.getAddress().toLowerCase().contains(address.toLowerCase())
               && (g.getPostNumber().equals(postNumber) || postNumber.equals("0"))) {
 
         if (g instanceof Company) {
           Company c = (Company) g;
 
           if (company.length() > 0 && c.getCompanyName().toLowerCase().contains(company.toLowerCase())) {
             matches.add(c);
             break;
           } else if (c.getPostNumber() == postNumber) {
             matches.add(c);
             break;
           }
 
         }
 
         // if no companyname is specified, we should show the element
         if (company.length() == 0) {
           matches.add(g);
         }
       }
 
     }
     return (matches.isEmpty()) ? null : matches;
   }
   /**
    * 
    * This method generates a printable (or displayable) text string with the contents
    * of our guest list.
    * 
    * @return A nicely formatted string of guests that can be printed or displayed in a dialogue.
    */
   @Override
   public String toString() {
     StringBuilder r = new StringBuilder();
     for (AbstractGuest g : list.values()) {
       r.append(g.toString());
       r.append("\n");
     }
     return r.toString();
   }
 
   public String getErrors() {
     String err = errors.toString();
     errors = new StringBuilder();
     return err;
   }
 }
