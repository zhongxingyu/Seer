 package de.hh.changeRing.initialData;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import de.hh.changeRing.advertisement.Advertisement;
 import de.hh.changeRing.advertisement.Category;
 import de.hh.changeRing.calendar.Event;
 import de.hh.changeRing.calendar.EventType;
 import de.hh.changeRing.transaction.Transaction;
 import de.hh.changeRing.user.Administrator;
 import de.hh.changeRing.user.Member;
 import de.hh.changeRing.user.SystemAccount;
 import de.hh.changeRing.user.User;
 import org.joda.time.DateTime;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import static de.hh.changeRing.calendar.EventType.fleaMarket;
 import static de.hh.changeRing.calendar.EventType.individual;
 import static de.hh.changeRing.calendar.EventType.info;
 import static de.hh.changeRing.calendar.EventType.regularsTable;
 import static de.hh.changeRing.calendar.EventType.summerFestival;
 
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
 
 	static {
 		InitTestData.init();
 		InitTestData.initTransactions();
 		InitTestData.initEvents();
 	}
 
 	private static void init() {
 		try {
 			List<Member> dummies = new ArrayList<Member>();
 			for (Long i = 1000L; i < 2000; i++) {
 				dummies.add((Member) User.dummyUser(i));
 			}
 			JAXBContext context = JAXBContext.newInstance(InitialData.class);
 			Unmarshaller unmarshaller = context.createUnmarshaller();
 			InputStream is = InitialData.class.getResourceAsStream("/initialData.xml");
 
 			data = (InitialData) unmarshaller.unmarshal(is);
 
 			List<Member> users = InitTestData.getUsers();
 			users.addAll(dummies);
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
 					String[] strings = {
 							"Blllp",
 							"Grrr",
 							"Ö",
 							"Ich habe es voll drauf",
 							"Nachts ist es kälter als draußen",
 							"wer lange Duscht wird furchtbar nass",
 							"Rollt ne Kugel um die Ecke und fällt um",
 							"Treffen sich drei Studenten und streiten sich wer wohl der Faulste sei.",
 							"Schraube",
 							"Töröööh",
 							"Fetz",
 							"Kokel",
 							"Zisch",
 							"Peng"
 					};
 					advertisement.setTitle(strings[new Random().nextInt(strings.length)]);
 
 					advertisement.setValidUntil(new DateTime().plusDays(new Random().nextInt(365)));
 					advertisement.setCreationDate(advertisement.getValidUntil().minusYears(1));
 
 					Category category;
 					do {
 						category = Category.values()[new Random().nextInt(Category.values().length)];
 					} while (!category.getChildren().isEmpty());
 					advertisement.setCategory(category);
 					advertisement.setLinkLocation(user.getId() % 3 == 1);
 					wire(advertisement);
 				}
 			}
 		}
 	}
 
 
 	public static List<Member> getUsers() {
 		return data.users;
 	}
 
 	private static void initTransactions() {
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
 	}
 
 	private static void process(Transaction transaction) {
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
 
 	private static void wire(Advertisement newAdvertisement) {
 		newAdvertisement.getOwner().getAdvertisements().add(newAdvertisement);
 	}
 
 	private static void initEvents() {
 		for (int daysToAdd : new int[]{-60, -30, 0, 30, 60, 90, 120}) {
 			createStammtisch(daysToAdd);
 		}
 		createInfoStand();
 		createMembersEvent();
 		summerEvent();
 		fleaMarketEvent();
 	}
 
 	private static void fleaMarketEvent() {
 		Event event = createEvent(182, 13l, fleaMarket);
 		event.setLocation("Hintertupfingen");
 		event.setTitle("Alles Kaufen");
 		event.setContent("Alles darf gekauft werden. \nStände bitte Anmelde. \nEuros werden nicht akzeptiert!");
 	}
 
 	private static void summerEvent() {
 		Event event = createEvent(180, 11l, summerFestival);
 		event.setTitle("Riesen Sause");
 		event.setContent("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam" +
 				" nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
 				"sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. " +
 				"Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit " +
 				"amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy ");
 	}
 
 	private static void createInfoStand() {
 		Event result = createEvent(15, 577l, info);
 		result.setTitle("Mercado");
 		result.setContent("Hier kann man Mitglied werden");
 		result.setLocation("Mercado Altona");
 	}
 
 	private static void createMembersEvent() {
 		Event result = createEvent(15, 595, individual);
 		result.setTitle("Brotaufstrich Basteln");
 		result.setContent("Bei mir werden vegetarische und vegane Brotaufstriche gekocht. Bitte vorher anmelden");
 		result.setLocation("Bei mir in Winterhude");
 		result.setDuration(90);
 	}
 
 
 	private static void createStammtisch(int daysToAdd) {
 		Event result = createEvent(daysToAdd, 577L, regularsTable);
 		result.setTitle("Eppendorf");
 		result.setContent("Monatlicher Stammtisch des Tauschrings");
 		result.setLocation("Kulturhaus Eppendorf, Julius-Reincke-Stieg 13a, 20251 Hamburg");
 	}
 
 	private static Event createEvent(int daysToAdd, long userId, EventType eventType) {
 		Event result = new Event();
 		result.setEventType(eventType);
 		result.setWhen(new DateTime().hourOfDay().withMinimumValue().plusHours(19).plusDays(daysToAdd));
 		User user = findUser(userId);
 		result.setUser(user);
 		user.getEvents().add(result);
 		return result;
 	}
 
 	public static List<Administrator> getAdministrators() {
 		return data.administrators;
 	}
 
 	public static SystemAccount getSystemAccount() {
 		return data.systemAccount;
 	}
 
 
 	@XmlRootElement(name = "exchangeRingInitial")
 	@XmlAccessorType(XmlAccessType.PROPERTY)
 	public static class InitialData {
 		@XmlElement(name = "user")
 		List<Member> users;
 
 		@XmlElement(name = "administrator")
 		List<Administrator> administrators;
 
		@XmlElement(name = "administrator")
 		SystemAccount systemAccount;
 
 		@XmlElement(name = "transaction")
 		List<Transaction> transactions;
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
