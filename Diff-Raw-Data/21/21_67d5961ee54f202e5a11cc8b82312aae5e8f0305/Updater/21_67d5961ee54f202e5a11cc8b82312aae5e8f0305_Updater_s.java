 package com.september.tableroids;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.graphics.Bitmap;
 import android.util.SparseArray;
 
 import com.september.tableroids.model.Sprite;
 
 public class Updater {
 
 	private static  Updater INSTANCE;
 	private List<Sprite> addList;
 	private SparseArray<Bitmap> resources;
 	
 	private Updater() {
 	}
 	
 	public static Updater getInstance() {
 		if(INSTANCE == null) {
 			INSTANCE = new Updater();
 		}
 		return INSTANCE;
 	}
 	
	public synchronized void addSprites(Sprite s) {
 		getSprites().add(s);
 	}
 
	private synchronized List<Sprite> getSprites() {
 		if(addList == null) {
			addList = Collections.synchronizedList(new LinkedList<Sprite>());
 		}
 		return addList;
 	}
 	
	public synchronized List<Sprite> getMirrorOfSprites() {
 		List<Sprite> b = new LinkedList<Sprite>();
 		b.addAll(getSprites());
 		return b;
 	}
 	
	public synchronized void clear() {
 		for (Iterator<Sprite> iterator = getSprites().iterator(); iterator.hasNext();) {
 			Sprite s = iterator.next();
 			if(s.isDirty()) {
 				iterator.remove();
 			}
 			
 		}
 		//addList = null;
 	}
 
 	public SparseArray<Bitmap> getResources() {
 		if(resources == null) {
 			resources = new SparseArray<Bitmap>();
 		}
 		return resources;
 	}
 
 //	public void setResources(SparseArray<Bitmap> resources) {
 //		this.resources = resources;
 //	}
 	
	public synchronized Sprite getById(int id) {
 			for(Sprite s: getMirrorOfSprites()) {
 				if(s.getId() == id) {
 					return s;
 				}
 			}
 		return null;
 	}
 	
	public synchronized Sprite getById(int id,List<Sprite> input) {
 		for(Sprite s: input) {
 			if(s.getId() == id) {
 				return s;
 			}
 		}
 	return null;
 }
 	
 	
 	
 }
