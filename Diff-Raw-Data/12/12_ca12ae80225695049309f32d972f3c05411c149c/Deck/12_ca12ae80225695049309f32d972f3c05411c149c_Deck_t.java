 /* memory
  * de.thm.ateam.memory.engine.type
  * Deck.java
  * 05.06.2012
  *
  * by Frank Kevin Zey
  */
 package de.thm.ateam.memory.engine.type;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import android.content.Context;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 import android.util.Log;
 
 import de.thm.ateam.memory.engine.MemoryDeckDAO;
 
 /**
  * @author Frank Kevin Zey
  *
  */
 public class Deck {
 	
	private final String TAG = this.getClass().getSimpleName();
 
 	private String name;
 	private long ID;
 	private Bitmap backSide = null;
 	private ArrayList<Bitmap> frontSide = null;
 	private MemoryDeckDAO dao;
 	
 	public Deck(Context ctx, long DECK_ID) throws NullPointerException {
 		dao = new MemoryDeckDAO(ctx);
 		Deck t = dao.getDeck(DECK_ID);
 		
 		if (t == null)
 			throw new NullPointerException("no deck found");
 		
 		this.ID = t.getID();
 		this.name = t.getName();
 		backSide = t.getBackSide();
 		frontSide = t.getFrontSide();
 	}
 	
 	public Deck(MemoryDeckDAO memoryDeckDAO, long ID, String name, Bitmap backSide) {
 		this.backSide = backSide;
 		this.name = name;
 		this.ID = ID;
 		
 		frontSide = new ArrayList<Bitmap>();
 		for( Bitmap b : memoryDeckDAO.getCard(ID))
 			frontSide.add(b);
 	}
 	
 	public Deck(ZipFile zip, Context ctx) throws IOException {
 		Enumeration<? extends ZipEntry> e = zip.entries();
 		frontSide = new ArrayList<Bitmap>();
 
 		while(e.hasMoreElements()) {
 			ZipEntry entry = e.nextElement();
 			InputStream f = zip.getInputStream(entry);
 			byte[] b = new byte[(int)f.available()];
 			f.read(b,0,b.length);
 			Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);
 			
 			if (entry.getName().equals("0.jpg"))
 				backSide = bm;
 				
 			else
 				frontSide.add(bm);
 			
 			Log.i(TAG,entry.getName());
 		}
 		
 		String[] splitted = zip.getName().split("/");
 		this.name = splitted[splitted.length-1].substring(0, splitted[splitted.length-1].length()-4);
 		dao = new MemoryDeckDAO(ctx);
 		if (dao.storeDeck(this))
 			Log.i(TAG, "stored");
 		else
 			Log.i(TAG, "not stored");
 		
 		zip.close();
 	}
 	
 	public ArrayList<Bitmap> getFrontSide() {
 		return frontSide;
 	}
 	
 	public Bitmap getBackSide() {
 		return this.backSide;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public long getID() {
 		return this.ID;
 	}
 	
 	public void setID(long ID) {
 		this.ID = ID;
 	}
 	
 }
