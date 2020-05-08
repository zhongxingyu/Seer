 package org.dazeend.harmonium.screens;
 
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.Vector;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.dazeend.harmonium.Harmonium;
 import org.dazeend.harmonium.music.AlbumReadable;
 import com.tivo.hme.bananas.BApplication;
 import com.tivo.hme.bananas.BScreen;
 import com.tivo.hme.sdk.ImageResource;
 import com.tivo.hme.sdk.Resource;
 import com.tivo.hme.sdk.View;
 
 public abstract class HManagedResourceScreen extends BScreen {
 
 	private Vector<Resource> _managedResources;
 	private Vector<View> _managedViews;
 	private ReentrantLock _lock = new ReentrantLock();
 	private Boolean _freeOnExit = true;
 
 	protected Harmonium app;
 	
 	public HManagedResourceScreen(BApplication app) {
 		super(app);
 		this.app = (Harmonium) app;
 		_managedResources = new Vector<Resource>();
 		_managedViews = new Vector<View>();
 	}
 	
 	protected void doNotFreeResourcesOnExit()
 	{
 		_freeOnExit = false;
 	}
 	
 	protected void setManagedResource(View view, Resource resource, int flags)
 	{
 		if (resource != null)
 		{
 			_managedResources.add(resource);
 			if (this.app.isInSimulator())
 				System.out.println("setManagedResource:" + resource.toString());
 			view.setResource(resource, flags);
 		}
 	}
 	
 	protected void setManagedResource(View view, Resource resource)
 	{
 		if (resource != null)
 		{
 			_managedResources.add(resource);
 			if (this.app.isInSimulator())
 				System.out.println("setManagedResource:" + resource.toString());
 			view.setResource(resource);
 		}
 	}
 	
 	// This view will be recursed and all its resources will be removed.
 	protected void setManagedView(View view) {
 		_managedViews.add(view);
 	}
 		
 	private void cleanupManagedResources()
 	{
 		_lock.lock();
 		try {
 			for ( Resource r : _managedResources )
 			{
 				if (!AlbumArtCache.getInstance(app).Contains(r)) {
 					if (app.isInSimulator())
 						System.out.println("removeManagedResource:" + r.toString());
 					r.remove();
 				}
 			}
 			_managedResources.clear();
 
 			for ( View v : _managedViews )
 				cleanup(v);
 			_managedViews.clear();
 			
 			this.remove();
 			
 			flush();
 		} finally {
 			_lock.unlock();
 		}
 	}
 	
 	protected ImageResource createManagedImage(AlbumReadable album, int width, int height) {
 		return AlbumArtCache.getInstance(app).Add(this, album, width, height);
 	}
 
 	protected ImageResource createManagedImage(String arg0) {
 		ImageResource ir = createImage(arg0);
 		if (this.app.isInSimulator())
 			System.out.println("createManagedImage:" + ir.toString());
 		_managedResources.add(ir);
 		return ir;
 	}
 	
 	private void cleanup(View v) {
 		if (v != null) {
 			int childCount = v.getChildCount();
 			for (int i = 0; i < childCount; i++)
 				cleanup(v.getChild(i));
 			Resource r = v.getResource();
 			if (r != null && !AlbumArtCache.getInstance(app).Contains(r))
 				r.remove();
 		}
 	}
 	
 	public void cleanup(){
 		if (_freeOnExit)
 		{
 			new Thread() {
 				public void run() {
 					cleanupManagedResources();
 				}
 			}.start();
 		}
 	}
 	
 	// Singleton cache for album art.
 	private static class AlbumArtCache {
 		
 		private static final int CACHE_SIZE = 15;
 		
 		private class ArtCacheItem {
 			private int _hash;
 			private ImageResource _resource;
 			
 			public ArtCacheItem(int hash, ImageResource resource) {
 				_hash = hash;
 				_resource = resource;
 			}
 			
 			public int getHash() { return _hash; }
 			public ImageResource getResource() { return _resource; }
 		}
 		
 		private Harmonium _app;
 		private LinkedList<ArtCacheItem> _managedImageList;
 		private Hashtable<Integer, ArtCacheItem> _managedImageHashtable;
 		private Hashtable<Resource, Boolean> _managedResourceHashtable;
 		
 		private static AlbumArtCache instance = null;
 
 		public static AlbumArtCache getInstance(Harmonium app) {
 			if(instance == null) {
 				instance = new AlbumArtCache(app);
 			}
 			return instance;
 		}
 	   
 		// Disallow instantation by another class.
 		protected AlbumArtCache(Harmonium app) {
 			_app = app;
 			_managedImageList = new LinkedList<ArtCacheItem>();
 			_managedImageHashtable = new Hashtable<Integer, ArtCacheItem>(CACHE_SIZE);
 			_managedResourceHashtable = new Hashtable<Resource, Boolean>(CACHE_SIZE);
 		}
 		
 		public synchronized ImageResource Add(BScreen screen, AlbumReadable album, int width, int height) {
 			
 			int hash = 0;
 			if (album.hasAlbumArt())
 				hash = (album.getAlbumArtistName() + album.getAlbumName() + album.getReleaseYear() + width + height).hashCode();
 					
 			ArtCacheItem aci = _managedImageHashtable.get(hash);
 			if (aci == null) {
 				if (_app.isInSimulator()) {
 					System.out.println("album art cache miss: " + hash);
 
 					try {
 						Thread.sleep(500); // better simulate performance of real Tivo.
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 				if (hash != 0)
 					aci = new ArtCacheItem(hash, screen.createImage(album.getScaledAlbumArt(width, height)));
 				else
 					aci = new ArtCacheItem(hash, screen.createImage("default_album_art.gif"));
 				
 				if (_managedImageList.size() == CACHE_SIZE) {
 					ArtCacheItem removeItem = _managedImageList.removeLast();
 					Resource removeResource = removeItem.getResource();
 					_managedImageHashtable.remove(removeItem.getHash());
 					_managedResourceHashtable.remove(removeResource);
 					removeResource.remove();
 				}
 					
 				_managedImageList.addFirst(aci);
 				_managedImageHashtable.put(hash, aci);
 				_managedResourceHashtable.put(aci.getResource(), false);
 			}
 			else {
 				if (_app.isInSimulator())
 					System.out.println("album art cache hit: " + hash);
 				
 				// move it to the front
 				_managedImageList.remove(aci);
 				_managedImageList.addFirst(aci);
 			}
 			return aci.getResource();
 		}
 		
 		public Boolean Contains(Resource r) {
 			return _managedResourceHashtable.containsKey(r);
 		}
 	}
 }
