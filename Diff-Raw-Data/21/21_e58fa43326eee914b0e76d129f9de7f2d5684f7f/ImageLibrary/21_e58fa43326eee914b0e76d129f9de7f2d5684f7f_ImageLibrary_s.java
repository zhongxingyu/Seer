 package com.episkipoe.common.draw;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.episkipoe.common.Game;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.event.dom.client.LoadEvent;
 import com.google.gwt.event.dom.client.LoadHandler;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class ImageLibrary {
 	Map<String,ImageElement> images;
 	public ImageLibrary() { 
 		images = new HashMap<String,ImageElement>();
 	}
 	
 	public ImageElement getImage(String filename) {
 		if(!images.containsKey(filename)) {
 			loadImage(filename);
 		}
 		return images.get(filename);
 	}
 	
 	public void loadImage(String filename) {
 		if(images.containsKey(filename)) return ;
 		Image img = new Image("images/"+filename);
 		img.setTitle(filename);
 		img.addLoadHandler(new LoadHandler() {
 		      public void onLoad(LoadEvent event) {
 		    	  Image img = (Image) event.getSource();
 		    	  images.put(img.getTitle(), (ImageElement)img.getElement().cast());
 		    	  if(Game.room != null) {
 			    	  Game.room.imageLoaded(img.getTitle());
 		    	  }
 		      }
 		    });
 		    img.setVisible(false);
 		    RootPanel.get().add(img); // image must be on page to fire load
 	}
 	public void loadImages(Collection<String> filenames) {
 		Set<String> fileSet = new HashSet<String>();
 		for(String f: filenames) fileSet.add(f);
 		for(String f: fileSet) loadImage(f);
 	}
 
 	public boolean contains(String filename) {
 		return images.containsKey(filename);
 	}
 
 }
