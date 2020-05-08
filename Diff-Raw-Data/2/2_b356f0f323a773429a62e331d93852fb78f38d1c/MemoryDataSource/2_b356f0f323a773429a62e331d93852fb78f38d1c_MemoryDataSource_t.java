 package fr.webmarket.backend.datasource.impl;
 
 import com.google.common.collect.Maps;
 import fr.webmarket.backend.datasource.DataSource;
 import fr.webmarket.backend.marshalling.MarshallingUtils;
 import fr.webmarket.backend.model.*;
 
 import java.io.IOException;
 import java.util.Map;
 
 public class MemoryDataSource implements DataSource {
 
 	private static MemoryDataSource INSTANCE;
 
 	private final TagsCatalog tagsCatalog;
 
 	private final ItemsCatalog itemsCatalog;
 
 	private final Map<String, User> users;
 
 	private MemoryDataSource() {
 		tagsCatalog = new TagsCatalog();
 		itemsCatalog = new ItemsCatalog();
 		users = Maps.newHashMap();
 		initMockData();
 	}
 
 	public static DataSource getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new MemoryDataSource();
 		}
 		return INSTANCE;
 	}
 
 	private void initMockData() {
 
 		// Initialize users
 		addUser(new User("eoriou", "pass", "Elian", "ORIOU", "eoriou@gmail.com"));
 
 		// Initialize default image
		String imagePath = "webmarket-backend/src/main/resources/default-image.png";
 		String base64Image = null;
 		try {
 			base64Image = MarshallingUtils.transformImageToBase64(imagePath);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// Initialize mock objects
 		String description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vitae ultrices quam. Sed eu ante at ipsum ultrices volutpat vel in nibh. Nullam at ipsum eu massa ultrices euismod. Proin ligula dolor, tincidunt eu viverra id, ultricies sit amet risus. Sed et eros vel ligula fermentum aliquam et at lorem.";
 
 		// 1. Tags
 		ItemTag electroMenager = new ItemTag("Electroménager");
 		ItemTag aspirateur = new ItemTag("Aspirateur");
 		ItemTag laveVaisselle = new ItemTag("Lave-Vaisselle");
 		ItemTag lecteurSalon = new ItemTag("Lecteur Salon");
 		ItemTag hifi = new ItemTag("HiFi");
 		ItemTag tv = new ItemTag("TV");
 		ItemTag baladeur = new ItemTag("Baladeur");
 
 		tagsCatalog.addTag(electroMenager);
 		tagsCatalog.addTag(aspirateur);
 		tagsCatalog.addTag(laveVaisselle);
 		tagsCatalog.addTag(lecteurSalon);
 		tagsCatalog.addTag(hifi);
 		tagsCatalog.addTag(tv);
 		tagsCatalog.addTag(baladeur);
 
 		// 2. Items
 		Item item1 = new Item("Lave-Vaisselle PV123", "Miele", description, 250);
 		item1.getTags().add(electroMenager);
 		item1.getTags().add(laveVaisselle);
 		item1.setBase64Image(base64Image);
 
 		Item item2 = new Item("Lave-Vaisselle", "Wirpool", description, 190);
 		item2.getTags().add(electroMenager);
 		item2.getTags().add(laveVaisselle);
 		item2.setBase64Image(base64Image);
 
 		Item item3 = new Item("iPod", "Apple", description, 340);
 		item3.getTags().add(hifi);
 		item3.getTags().add(baladeur);
 		item3.setBase64Image(base64Image);
 
 		Item item4 = new Item("Lecteur Blu-Ray", "Samsung", description, 190);
 		item4.getTags().add(hifi);
 		item4.getTags().add(lecteurSalon);
 		item4.setBase64Image(base64Image);
 
 		Item item5 = new Item("Aspirateur SupraPlus", "Dyson", description, 450);
 		item5.getTags().add(aspirateur);
 		item5.getTags().add(electroMenager);
 		item5.setBase64Image(base64Image);
 
 		Item item6 = new Item("Lave-Vaisselle PV567", "Miele", description
 				+ "pouet", 280);
 		item6.getTags().add(electroMenager);
 		item6.getTags().add(laveVaisselle);
 		item6.setBase64Image(base64Image);
 
 		Item item7 = new Item("TV LCD-LED", "Samsung", description, 670);
 		item7.getTags().add(hifi);
 		item7.getTags().add(tv);
 		item7.setBase64Image(base64Image);
 
 		Item item8 = new Item("TV Plasma SXH-819", "Sony", description, 670);
 		item8.getTags().add(hifi);
 		item8.getTags().add(tv);
 		item8.setBase64Image(base64Image);
 
 		Item item9 = new Item("TV Plasma SRF-876", "LG", description, 670);
 		item9.getTags().add(hifi);
 		item9.getTags().add(tv);
 		item9.setBase64Image(base64Image);
 
 		Item item10 = new Item("TV LCD-LED YTG-789", "Loewe", description, 670);
 		item10.getTags().add(hifi);
 		item10.getTags().add(tv);
 		item10.setBase64Image(base64Image);
 
 		itemsCatalog.addItem(item1);
 		itemsCatalog.addItem(item2);
 		itemsCatalog.addItem(item3);
 		itemsCatalog.addItem(item4);
 		itemsCatalog.addItem(item5);
 		itemsCatalog.addItem(item6);
 		itemsCatalog.addItem(item7);
 		itemsCatalog.addItem(item8);
 		itemsCatalog.addItem(item9);
 		itemsCatalog.addItem(item10);
 
 	}
 
 	@Override
 	public ItemsCatalog getItemsCatalog() {
 		return itemsCatalog;
 	}
 
 	@Override
 	public TagsCatalog getTagsCatalog() {
 		return tagsCatalog;
 	}
 
 	@Override
 	public Map<String, User> getUsers() {
 		return users;
 	}
 
 	@Override
 	public void addUser(User u) {
 		users.put(u.getLogin(), u);
 	}
 
 }
