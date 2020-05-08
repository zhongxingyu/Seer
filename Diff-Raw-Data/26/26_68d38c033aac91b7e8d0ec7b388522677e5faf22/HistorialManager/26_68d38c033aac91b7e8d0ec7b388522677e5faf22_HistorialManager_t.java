 package com.majomi.zeninstants.settingscontroller;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 import com.majomi.zeninstants.messagesentities.MessageImageEntity;
 import com.majomi.zeninstants.messagesentities.MessageSoundEntity;
 import com.majomi.zeninstants.messagesentities.MessageTextEntity;
 import com.majomi.zeninstants.messagesentities.MessageVideoEntity;
 import com.majomi.zeninstants.utils.Utils;
 
 
 public class HistorialManager {
 
 	//********** Singleton *************
 	static private HistorialManager historialManager = null;
 
 	static public HistorialManager getHistorialManager() {
 		if(historialManager == null) {
 			historialManager = new HistorialManager();
 		}	
 		return historialManager;
 	}
 
 	private HistorialManager() {
 		//TODO remove all that stuff later..
 		this.messages.add(new MessageTextEntity("Roses are red\nViolets are blue\nThis is the first phrase\nYeah yeah youpi yeah"));
 		this.messages.add(new MessageTextEntity("Roses are red\nViolets are blue\nThis is the first phrase\nYeah yeah youpi yeah"));
 		this.messages.add(new MessageTextEntity("Roses are red\nViolets are blue\nThis is the first phrase\nYeah yeah youpi yeah"));
 		this.messages.add(new MessageTextEntity("Roses are red\nViolets are blue\nThis is the first phrase\nYeah yeah youpi yeah"));
 		this.messages.add(new MessageTextEntity("Roses are red\nViolets are blue\nThis is the first phrase\nYeah yeah youpi yeah"));
 		this.messages.add(new MessageVideoEntity("LONG TEXT HERE\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque auctor enim eget sem pulvinar pretium. Donec suscipit consequat mi id commodo. Vestibulum semper volutpat odio, id luctus sapien malesuada eget. Quisque semper leo quis urna rhoncus at euismod orci malesuada. Quisque tortor arcu, condimentum vel cursus eu, consequat sit amet ante. Vestibulum sodales tincidunt urna, sit amet interdum diam sodales vitae. Curabitur ac enim erat. Quisque rhoncus venenatis orci, vitae ultricies arcu fermentum blandit. Aliquam malesuada ornare varius. Integer lobortis tempus porttitor. Aenean libero enim, adipiscing in pharetra eget, pellentesque eu nibh. Pellentesque eleifend faucibus suscipit. Maecenas ornare imperdiet bibendum.Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque auctor enim eget sem pulvinar pretium. Donec suscipit consequat mi id commodo. Vestibulum semper volutpat odio, id luctus sapien malesuada eget. Quisque semper leo quis urna rhoncus at euismod orci malesuada. Quisque tortor arcu, condimentum vel cursus eu, consequat sit amet ante. Vestibulum sodales tincidunt urna, sit amet interdum diam sodales vitae. Curabitur ac enim erat. Quisque rhoncus venenatis orci, vitae ultricies arcu fermentum blandit. Aliquam malesuada ornare varius. Integer lobortis tempus porttitor. Aenean libero enim, adipiscing in pharetra eget, pellentesque eu nibh. Pellentesque eleifend faucibus suscipit. Maecenas ornare imperdiet bibendum.Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque auctor enim eget sem pulvinar pretium. Donec suscipit consequat mi id commodo. Vestibulum semper volutpat odio, id luctus sapien malesuada eget. Quisque semper leo quis urna rhoncus at euismod orci malesuada. Quisque tortor arcu, condimentum vel cursus eu, consequat sit amet ante. Vestibulum sodales tincidunt urna, sit amet interdum diam sodales vitae. Curabitur ac enim erat. Quisque rhoncus venenatis orci, vitae ultricies arcu fermentum blandit. Aliquam malesuada ornare varius. Integer lobortis tempus porttitor. Aenean libero enim, adipiscing in pharetra eget, pellentesque eu nibh. Pellentesque eleifend faucibus suscipit. Maecenas ornare imperdiet bibendum.", "TODO"));
		this.messages.add(new MessageSoundEntity("Test exist Roses ar", "http://192.168.1.136/zenManagement/music.mp3"));
 		this.messages.add(new MessageImageEntity("LONG TEXT HERE\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque auctor enim eget sem pulvinar pretium. Donec suscipit consequat mi id commodo. Vestibulum semper volutpat odio, id luctus sapien malesuada eget. Quisque semper leo quis urna rhoncus at euismod orci malesuada. Quisque tortor arcu, condimentum vel cursus eu, consequat sit amet ante. Vestibulum sodales tincidunt urna, sit amet interdum diam sodales vitae. Curabitur ac enim erat. Quisque rhoncus venenatis orci, vitae ultricies arcu fermentum blandit. Aliquam malesuada ornare varius. Integer lobortis tempus porttitor. Aenean libero enim, adipiscing in pharetra eget, pellentesque eu nibh. Pellentesque eleifend faucibus suscipit. Maecenas ornare imperdiet bibendum.Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque auctor enim eget sem pulvinar pretium. Donec suscipit consequat mi id commodo. Vestibulum semper volutpat odio, id luctus sapien malesuada eget. Quisque semper leo quis urna rhoncus at euismod orci malesuada. Quisque tortor arcu, condimentum vel cursus eu, consequat sit amet ante. Vestibulum sodales tincidunt urna, sit amet interdum diam sodales vitae. Curabitur ac enim erat. Quisque rhoncus venenatis orci, vitae ultricies arcu fermentum blandit. Aliquam malesuada ornare varius. Integer lobortis tempus porttitor. Aenean libero enim, adipiscing in pharetra eget, pellentesque eu nibh. Pellentesque eleifend faucibus suscipit. Maecenas ornare imperdiet bibendum.Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque auctor enim eget sem pulvinar pretium. Donec suscipit consequat mi id commodo. Vestibulum semper volutpat odio, id luctus sapien malesuada eget. Quisque semper leo quis urna rhoncus at euismod orci malesuada. Quisque tortor arcu, condimentum vel cursus eu, consequat sit amet ante. Vestibulum sodales tincidunt urna, sit amet interdum diam sodales vitae. Curabitur ac enim erat. Quisque rhoncus venenatis orci, vitae ultricies arcu fermentum blandit. Aliquam malesuada ornare varius. Integer lobortis tempus porttitor. Aenean libero enim, adipiscing in pharetra eget, pellentesque eu nibh. Pellentesque eleifend faucibus suscipit. Maecenas ornare imperdiet bibendum.","http://cache.20minutes.fr/img/photos/20mn/2009-10/2009-10-19/article_NicolasCage.jpg"));
 		this.messages.add(new MessageTextEntity("LONG TEXT HERE\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque auctor enim eget sem pulvinar pretium. Donec suscipit consequat mi id commodo. Vestibulum semper volutpat odio, id luctus sapien malesuada eget. Quisque semper leo quis urna rhoncus at euismod orci malesuada. Quisque tortor arcu, condimentum vel cursus eu, consequat sit amet ante. Vestibulum sodales tincidunt urna, sit amet interdum diam sodales vitae. Curabitur ac enim erat. Quisque rhoncus venenatis orci, vitae ultricies arcu fermentum blandit. Aliquam malesuada ornare varius. Integer lobortis tempus porttitor. Aenean libero enim, adipiscing in pharetra eget, pellentesque eu nibh. Pellentesque eleifend faucibus suscipit. Maecenas ornare imperdiet bibendum.Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque auctor enim eget sem pulvinar pretium. Donec suscipit consequat mi id commodo. Vestibulum semper volutpat odio, id luctus sapien malesuada eget. Quisque semper leo quis urna rhoncus at euismod orci malesuada. Quisque tortor arcu, condimentum vel cursus eu, consequat sit amet ante. Vestibulum sodales tincidunt urna, sit amet interdum diam sodales vitae. Curabitur ac enim erat. Quisque rhoncus venenatis orci, vitae ultricies arcu fermentum blandit. Aliquam malesuada ornare varius. Integer lobortis tempus porttitor. Aenean libero enim, adipiscing in pharetra eget, pellentesque eu nibh. Pellentesque eleifend faucibus suscipit. Maecenas ornare imperdiet bibendum.Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque auctor enim eget sem pulvinar pretium. Donec suscipit consequat mi id commodo. Vestibulum semper volutpat odio, id luctus sapien malesuada eget. Quisque semper leo quis urna rhoncus at euismod orci malesuada. Quisque tortor arcu, condimentum vel cursus eu, consequat sit amet ante. Vestibulum sodales tincidunt urna, sit amet interdum diam sodales vitae. Curabitur ac enim erat. Quisque rhoncus venenatis orci, vitae ultricies arcu fermentum blandit. Aliquam malesuada ornare varius. Integer lobortis tempus porttitor. Aenean libero enim, adipiscing in pharetra eget, pellentesque eu nibh. Pellentesque eleifend faucibus suscipit. Maecenas ornare imperdiet bibendum."));
 		this.messages.add(new MessageVideoEntity("Test Roses are red\nViolets are blue\nThis is the first phrase\nYeah yeah youpi yeah", "http://www.youtube.com/watch?feature=player_detailpage&v=ykwqXuMPsoc"));
 		this.messages.add(new MessageImageEntity("Roses are red\nViolets","http://www.ecrans.fr/local/cache-vignettes/L450xH341/pub_cage_450-de1e1.jpg"));
		this.messages.add(new MessageSoundEntity("In the beginner's mind there are many possibilities, but in the expert's mind there are few.","http://upload.wikimedia.org/wikipedia/commons/a/a9/Tromboon-sample.ogg"));
		this.messages.add(new MessageImageEntity("Do not permit the events of your daily life to bind you, but never withdraw yourself from them.","http://distilleryimage6.s3.amazonaws.com/1e5a6ef837d211e2b41022000a9f1899_7.jpg"));
		this.messages.add(new MessageTextEntity("The quieter you become, the more you are able to hear.\n- Lao Tzu"));
 
 		//TODO:remove
 		for(int i = 0; i < this.messages.size(); i++) {
 			this.messages.get(i).setId(i);
 		}		
 		//FavoritesManager.getFavoritesManager().addFavorite(this.messages.get(0));
 	}
 	//******** END Singleton ***********
 
 	private ArrayList<MessageTextEntity> messages = new ArrayList<MessageTextEntity>();
 
 	public void addMessage(MessageTextEntity message) {
 		this.messages.add((MessageTextEntity)message.clone());
 
 		saveMessages();
 	}
 
 	public MessageTextEntity getMessage(int index) {
 		return this.messages.get(this.messages.size() - 1 - index);
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public MessageTextEntity getMessage(long id, Class entityClass) {
 		MessageTextEntity ret = null;
 		
 		for(MessageTextEntity m : this.messages) {
 			if(m.getId() == id && m.getClass().equals(entityClass)) {
 				ret = m;
 				break;
 			}
 		}
 		
 		return ret;
 	}
 
 	public boolean contains(MessageTextEntity msg) {
 		for(MessageTextEntity e : this.messages) {
 			if(e.equals(msg)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public MessageTextEntity getRandomMessage() {
 		@SuppressWarnings("unchecked")
 		ArrayList<MessageTextEntity> msgs = (ArrayList<MessageTextEntity>) this.messages.clone();
 		
 		SettingsManager sm = SettingsManager.getSettingsManager();
 
 		for (MessageTextEntity mte : this.messages) {
 			if((!sm.isPhoto_enabled()) && mte.getClass() == MessageImageEntity.class
 					|| (!sm.isMusic_enabled()) && mte.getClass() == MessageSoundEntity.class
 					|| (!sm.isVideo_enabled()) && mte.getClass() == MessageVideoEntity.class
 					|| (!sm.isText_enabled()) && mte.getClass() == MessageTextEntity.class) {
 				msgs.remove(mte);
 			}
 		}
 		
 		if(!msgs.isEmpty()) {
 			int random = (int)(Math.random() * msgs.size());
 			return msgs.get(random);
 		} else {
 			return null;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadMessages() {
 		Object res = Utils.getObjectFromSharedPreferences("historial_messages");
 
 		if(res != null) {
 			this.messages = (ArrayList<MessageTextEntity>) res;
 		}
 	}
 
 	public int getMessagesSize() {
 		return this.messages.size();
 	}
 
 	public void saveMessages() {
 		Utils.putObjectIntoSharedPreferences("historial_messages", this.messages);	
 	}
 
 }
