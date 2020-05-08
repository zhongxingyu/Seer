 package de.hh.changeRing;
 
 import static de.hh.changeRing.calendar.EventType.*;
 import static java.util.Calendar.DAY_OF_MONTH;
 import static java.util.Calendar.MONTH;
 import static java.util.Calendar.YEAR;
 
 import com.google.common.collect.Ordering;
 import de.hh.changeRing.advertisement.Advertisement;
 import de.hh.changeRing.advertisement.Category;
 import de.hh.changeRing.calendar.Event;
 import de.hh.changeRing.calendar.EventModel;
 import de.hh.changeRing.calendar.EventType;
 import de.hh.changeRing.transaction.Transaction;
 import de.hh.changeRing.user.User;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * ----------------GNU General Public License--------------------------------
  * <p/>
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * ----------------in addition-----------------------------------------------
  * <p/>
  * In addition, each military use, and the use for interest profit will be
  * excluded.
  * Environmental damage caused by the use must be kept as small as possible.
  */
 public class InitTestData {
     private static InitialData data;
     private static List<Transaction> transactions;
     private static final List<Advertisement> advertisements = new ArrayList<Advertisement>();
     private static Map<Advertisement.AdvertisementType, Map<Category, LinkedList<Advertisement>>> sortedAds;
 	private static List<Event> events;
 
 	static {
         InitTestData.init();
         InitTestData.getTransactions();
 		InitTestData.getEvents();
     }
 
     private static void init() {
         try {
             JAXBContext context = JAXBContext.newInstance(InitialData.class);
             Unmarshaller unmarshaller = context.createUnmarshaller();
             InputStream is = InitialData.class.getResourceAsStream("/initialData.xml");
 
             data = (InitialData) unmarshaller.unmarshal(is);
 
             List<User> users = InitTestData.getUsers();
             for (Long i = 1000L; i < 2000; i++) {
                 users.add(User.dummyUser(i));
 
             }
             initAds();
         } catch (JAXBException e) {
             throw new RuntimeException(e);
         }
     }
 
     private static void initAds() {
         for (User user : getUsers()) {
             if (user.getId() < 1400L) {
                 for (int i = 0; i < new Random().nextInt(5); i++) {
                     Advertisement advertisement = new Advertisement();
                     advertisement.setOwner(user);
                     advertisement.setType(Advertisement.AdvertisementType.values()[((int) (user.getId() % 2))]);
                     advertisement.setContent(loremYpsum());
                     advertisement.setLocation("egal bei " + user.getDisplayName());
                     advertisement.setTitle("anzeige von " + user.getId());
                     //noinspection NumericOverflow
                     GregorianCalendar calendar = new GregorianCalendar();
                     calendar.add(DAY_OF_MONTH, new Random().nextInt(365));
                     advertisement.setValidUntil(calendar.getTime());
                     calendar.add(YEAR, -1);
                     advertisement.setCreationDate(calendar.getTime());
 
                     Category category;
                     do {
                         category = Category.values()[new Random().nextInt(Category.values().length)];
                     } while (!category.getChildren().isEmpty());
                     advertisement.setCategory(category);
                     advertisement.setLinkLocation(user.getId() % 3 == 1);
                     addAdvertisement(advertisement);
                 }
             }
         }
     }
 
     public static Map<Advertisement.AdvertisementType, Map<Category, LinkedList<Advertisement>>> getSortedAds() {
         if (sortedAds == null) {
             sortedAds = createSortedAds();
         }
         return sortedAds;
     }
 
     private static HashMap<Advertisement.AdvertisementType, Map<Category, LinkedList<Advertisement>>> createSortedAds() {
         HashMap<Advertisement.AdvertisementType, Map<Category, LinkedList<Advertisement>>> result
                 = new HashMap<Advertisement.AdvertisementType, Map<Category, LinkedList<Advertisement>>>();
         for (Advertisement.AdvertisementType advertisementType : Advertisement.AdvertisementType.values()) {
             Map<Category, LinkedList<Advertisement>> categoryMap = new HashMap<Category, LinkedList<Advertisement>>();
             result.put(advertisementType, categoryMap);
             for (Category category : Category.values()) {
                 categoryMap.put(category, new LinkedList<Advertisement>());
             }
         }
         fillAdsInMap(result);
         //sort
         sortAdMap(result);
         return result;
     }
 
     private static void fillAdsInMap(HashMap<Advertisement.AdvertisementType, Map<Category, LinkedList<Advertisement>>> result) {
         for (Advertisement advertisement : advertisements) {
             boolean notExpired = advertisement.getValidUntil().getTime() > System.currentTimeMillis();
             if (notExpired) {
                 for (Category category : advertisement.getCategory().thisWithParents) {
                     result.get(advertisement.getType()).get(category).add(advertisement);
                 }
             }
         }
     }
 
     private static void sortAdMap(HashMap<Advertisement.AdvertisementType, Map<Category, LinkedList<Advertisement>>> result) {
         for (Advertisement.AdvertisementType advertisementType : Advertisement.AdvertisementType.values()) {
             Map<Category, LinkedList<Advertisement>> categoryMap = result.get(advertisementType);
             for (Category category : Category.values()) {
                 List<Advertisement> sorted = new Ordering<Advertisement>() {
                     @Override
                     public int compare(Advertisement advertisement, Advertisement advertisement1) {
                         return advertisement1.getCreationDate().compareTo(advertisement.getCreationDate());
                     }
                 }.sortedCopy(categoryMap.get(category));
                 LinkedList<Advertisement> linkedList = new LinkedList<Advertisement>();
                 linkedList.addAll(sorted);
                 categoryMap.put(category, linkedList);
             }
         }
     }
 
 
     public static List<User> getUsers() {
         return data.users;
     }
 
     public static List<Transaction> getTransactions() {
         if (transactions == null) {
             transactions = new Ordering<Transaction>() {
                 @Override
                 public int compare(Transaction transaction, Transaction transaction1) {
                     return transaction.getDate().compareTo(transaction1.getDate());
                 }
             }.sortedCopy(data.transactions);
             for (Transaction transaction : transactions) {
                 process(transaction);
             }
         }
         return transactions;
     }
 
     static void process(Transaction transaction) {
         transaction.wire();
     }
 
     public static User findUser(Long id) {
         for (User user : getUsers()) {
             if (user.getId().equals(id)) {
                 return user;
             }
         }
         return null;
     }
 
     public static User findUser(String idEmailOrNick) {
 
         for (User user : getUsers()) {
             if ((user.getId() != null && user.getId().toString().equals(idEmailOrNick))
                     || (user.getEmail() != null && user.getEmail().equals(idEmailOrNick))
                     || (user.getNickName() != null && user.getNickName().equals(idEmailOrNick))) {
                 return user;
             }
         }
         return null;
     }
 
     public static long getTotalRevenue() {
         long result = 0;
         for (Transaction transaction : transactions) {
             result += transaction.getAmount();
         }
         return result;
     }
 
     public static Boolean debtsAndAssetsAreEqual() {
         long debtsAndAssets = 0;
         for (User user : getUsers()) {
             debtsAndAssets += user.getBalance();
         }
         return debtsAndAssets == 0;
     }
 
     public static List<Advertisement> getAdvertisements() {
         return advertisements;
     }
 
     public static Advertisement findAd(Long id) {
         for (Advertisement advertisement : advertisements) {
             if (advertisement.getId().equals(id)) {
                 return advertisement;
             }
         }
         return null;
     }
 
     public static void addAdvertisement(Advertisement newAdvertisement) {
         advertisements.add(newAdvertisement);
         newAdvertisement.getOwner().getAdvertisements().add(newAdvertisement);
         sortedAds = null;
     }
 
     public static void clearSorted() {
         sortedAds = null;
     }
 
 	public static List<Event> getEvents() {
 		if (events == null) {
 			events = new ArrayList<Event>();
             for  (int daysToAdd : new int[]{-5, -1, 0, 7, 14, 36, 360}) {
                events.add(createStammtisch(daysToAdd));
             }
             events.add(createInfoStand());
             events.add(createMembersEvent());
             events.add(summerEvent());
             events.add(fleaMarketEvent());
         }
 		return events;
 	}
 
     private static Event fleaMarketEvent() {
         Event event = createEvent(182, 13l, fleaMarket);
         event.setLocation("Hintertupfingen");
         event.setTitle("Alles Kaufen");
         event.setContent("Alles darf gekauft werden. \nStände bitte Anmelde. \nEuros werden nicht akzeptiert!");
         return event;
     }
 
     private static Event summerEvent() {
         Event event = createEvent(180, 1l, summerFestival);
         event.setTitle("Riesen Sause");
         event.setContent("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" +
                 " nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
                 "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. " +
                 "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit " +
                "amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy " +
                "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. ");
         return event;
     }
 
     private static Event createInfoStand() {
         Event result = createEvent(15, 577l, info);
         result.setTitle("Mercado");
         result.setContent("Hier kann man Mitglied werden");
         result.setLocation("Mercado Altona");
         return result;
     }
 
     private static Event createMembersEvent() {
         Event result = createEvent(15, 595, individual);
         result.setTitle("Brotaufstrich Basteln");
         result.setContent("Bei mir werden vegetarische und vegane Brotaufstriche gekocht. Bitte vorher anmelden");
         result.setLocation("Bei mir in Winterhude");
         result.setDuration(90);
         return result;
     }
 
 
     private static Event createStammtisch(int daysToAdd) {
         Event result = createEvent(daysToAdd, 577L, regularsTable);
         result.setTitle("Eppendorf");
 		result.setContent("Monatlicher Stammtisch des Tauschrings");
 		result.setLocation("Kulturhaus Eppendorf, Julius-Reincke-Stieg 13a, 20251 Hamburg");
 		return result;
 	}
 
     private static Event createEvent(int daysToAdd, long userId, EventType eventType) {
         Event result = new Event();
         result.getId();
         result.setEventType(eventType);
         GregorianCalendar from = new GregorianCalendar();
         from.set(Calendar.HOUR, 19);
         from.set(Calendar.MINUTE, 0);
         from.add(DAY_OF_MONTH, daysToAdd);
 
         result.setWhen(from.getTime());
         User user = findUser(userId);
         result.setUser(user);
         user.getEvents().add(result);
         return result;
     }
 
     public static List<Event> getFilteredEvents(EventModel.TimeFilter timeFilter, List<String> selectedTypeFilters) {
 
         List<Event> result = new ArrayList<Event>();
         for (Event event : getEvents()) {
             if (selectedTypeFilters.contains(event.getEventType().name())){
                 if (timeFilter.accepts(event.getWhen())){
                     result.add(event);
                 }
             }
         }
         return timeFilter.order(result);
     }
 
     public static List<User> getNewestMembers(int count) {
         List<User> users = new Ordering<User>(){
             @Override
             public int compare(User user, User user2) {
                 return user2.getActivated().compareTo(user.getActivated());
             }
         }.sortedCopy(getUsers());
         Iterator<User> iterator = users.iterator();
         List<User> result = new ArrayList<User>();
         for (int i = 0; i < count; i++) {
             result.add(iterator.next());
         }
         return result;
     }
 
 
     @XmlRootElement(name = "exchangeRingInitial")
     @XmlAccessorType(XmlAccessType.PROPERTY)
     public static class InitialData {
         @XmlElement(name = "user")
         List<User> users;
 
         @XmlElement(name = "transaction")
         List<Transaction> transactions;
 		private static Object events;
 	}
 
     private static String loremYpsum() {
         return loremYpsum(new Random().nextInt(4));
     }
 
     private static String loremYpsum(int type) {
         switch (type) {
             case 1:
                 return "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam";
             case 2:
                 return "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
                         "sed diam nonumy eirmod tempor invidunt ut labore et " +
                         "dolore magna aliquyam erat, sed diam voluptua. At ";
             case 3:
                 return "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" +
                         " nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
                         "sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. " +
                         "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit " +
                         "amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy " +
                         "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. " +
                         "At vero eos et accusam et justo duo dolores et ea rebum.";
         }
         return "Ö";
     }
 
 
 }
