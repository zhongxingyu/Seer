 package se.flashcards;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.util.Log;
 
 public class InfoSaver
 {
 	private SharedPreferences prefs;
 	
 	public static InfoSaver getInfoSaver(Context c) {
 		return new InfoSaver(c, "flashcards");
 	}
 	
 	public InfoSaver(Context c, String name) {
 		prefs = c.getSharedPreferences(name, Context.MODE_PRIVATE);
 	}
 	
 	public static String cardListName(int cardListNr) {
 		return "cardlist_" + cardListNr;
 	}
 	
 	public static String cardQuestionName(String listName, int nr) {
 		return "cardlist_" + listName + "_q_" + nr;
 	}
 	
 	public static String cardAnswerName(String listName, int nr) {
 		return "cardlist_" + listName + "_a_" + nr;
 	}
 	
 	public SharedPreferences getPrefs() {
 		return prefs;
 	}
 	
 	public void removeCardList(String name) {
 		SharedPreferences.Editor editor = prefs.edit();
 		int nr = 0;
 		while (!prefs.getString(cardQuestionName(name, nr), "").equals("")) {
 			removeCardContent(cardQuestionName(name, nr));
 			removeCardContent(cardAnswerName(name, nr));
 			nr++;
 		}
 		editor.commit();
 	}
 	
 	public void saveCardLists(List<String> cardLists) {
 		SharedPreferences.Editor edit = prefs.edit();
 		int nr = 0;
 		for (String s : cardLists) {
 			edit.putString(cardListName(nr), s);
 			nr++;
 		}
 		edit.putInt("cardlist_length", nr);
 		edit.commit();
 	}
 	
 	public List<String> getCardLists() {
 		List<String> cardLists = new ArrayList<String>();
 		int cardListsAmnt = prefs.getInt("cardlist_length", 0);
 		for (int i = 0; i < cardListsAmnt; i++) {
 			cardLists.add(prefs.getString("cardlist_" + i, "katja le hipster"));
 		}
 		
 		return cardLists;
 	}
 	
 	public void removeAllCardsAbove(String listName, int pos) {
 		int remPos = pos;
 		while (cardContentExists(cardAnswerName(listName, remPos))) {
			remPos++;
 			removeCardContent(cardAnswerName(listName, remPos));
 			removeCardContent(cardQuestionName(listName, remPos));
 		}
 	}
 	
 	public void saveCards(String listName, List<Card> cards) {
 		int nr = 0;
 		for (Card card : cards) {			
 			saveCardContent(cardQuestionName(listName, nr), card.getQuestion());
 			saveCardContent(cardAnswerName(listName, nr), card.getAnswer());	
 			nr++;
 		}
 		removeAllCardsAbove(listName, nr);
 	}
 	
 	public boolean cardContentExists(String name) {
		return !prefs.getString(name, "").equals("");
 	}
 	
 	private void removeCardContent(String name) {
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.remove(name);
 		editor.remove(name + "_bmp");
 		editor.commit();
 	}
 	
 	public void saveCardContent(String name, CardContent content) {
 		SharedPreferences.Editor editor = prefs.edit();
 		
 		String toSave = "";
 		boolean isBmp = false;
 		if (content.isBitmap()) {
 			toSave = content.getUri().toString();
 			isBmp = true;
 		} else {
 			toSave = content.getString();
 			isBmp = false;
 		}
 		editor.putBoolean(name + "_bmp", isBmp);
 		editor.putString(name, toSave);
 		editor.commit();
 	}
 	
 	public CardContent loadCardContent(String name, BitmapDownsampler downSampler) {
 		boolean isBitmap = prefs.getBoolean(name + "_bmp", true);
 		if (isBitmap) {
 			Uri q = Uri.parse(prefs.getString(name, ""));
 			try {
 				return new CardContent(downSampler.decode(q), q);
 			} catch (IOException e) {
 				Log.v("flashcards", "Couldn't load question bitmap");
 				return null;
 			}
 		} else {
 			return new CardContent(prefs.getString(name, ""));
 		}
 	}
 	
 	public static class CardLoaderIterator implements Iterator<Card>, Iterable<Card> {
 		private int pos;
 		private BitmapDownsampler sampler;
 		private String listName;
 		private InfoSaver infoSaver;
 		
 		private boolean gotNext;
 		private String nextQ;
 		
 		public CardLoaderIterator(String listName, BitmapDownsampler sampler, InfoSaver infoSaver) {
 			this.sampler = sampler;
 			this.listName = listName;
 			this.infoSaver = infoSaver;
 		}
 		
 		@Override
 		public boolean hasNext() {
 			//TODO: optimize, no unnecessary calls to this
 			return !infoSaver.getPrefs().getString(cardQuestionName(listName, pos), "").equals("");
 		}
 
 		@Override
 		public Card next() {
 			CardContent question = infoSaver.loadCardContent(cardQuestionName(listName, pos), sampler);
 			CardContent answer = infoSaver.loadCardContent(cardAnswerName(listName, pos), sampler);
 			
 			pos++;
 			gotNext = false;
 			return new Card(question, answer);
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public Iterator<Card> iterator() {
 			return this;
 		}	
 	}
 	
 	public CardLoaderIterator getCardLoaderIterator(Context c, String listName, BitmapDownsampler sampler) {
 		return new CardLoaderIterator(listName, sampler, getInfoSaver(c));
 	}
 	
 	public List<Card> getCards(Context c, String listName, BitmapDownsampler sampler) throws IOException {
 		List<Card> cardList = new ArrayList<Card>();
 		CardLoaderIterator it = getCardLoaderIterator(c, listName, sampler);
 		
 		for (Card card : it) {
 			cardList.add(card);
 		}
 		
 		return cardList;
 	}
 }
