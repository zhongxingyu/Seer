 package edu.uib.info323.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class ImageFactoryImpl implements ImageFactory {
 	private static final int defaultHeight = -1;
 	private static final int defaultId = -1;
 	private static final int defaultWidth = -1;
 	
 	
 	public Image createImage(int id, String imageUri, List<String> pageUris, int height, int width) {
		
		return this.createImage(imageUri, pageUris, width, height, defaultId);
 	}
 
 	public Image createImage(String imageUri) {
 		return this.createImage(imageUri, new ArrayList<String>());
 	}
 	
 	public Image createImage(String imageUri, int id) {
 		return this.createImage(imageUri,new ArrayList<String>(), defaultId);
 	}
 	
 	
 	public Image createImage(String imageUri, int height, int width) {
 		return this.createImage(imageUri, new ArrayList<String>(), height, width);
 	}
 
 	public Image createImage(String imageUri, int height, int width, int id) {
 		return this.createImage(imageUri, new ArrayList<String>(), width, height, id);
 	}
 
 	public Image createImage(String imageUri, List<String> pageUris) {
 		return this.createImage(imageUri, pageUris, 0, 0);
 	}
 	
 	public Image createImage(String imageUri, List<String> pageUri, int id) {
 		return this.createImage(imageUri, pageUri, defaultHeight, defaultWidth, id);
 	}
 
 	public Image createImage(String imageUri, List<String> pageUris, int height, int width) {
 		return this.createImage(-1, imageUri, pageUris, height, width);
 	}
 
 	public Image createImage(String imageUri, List<String> pageUris, int width,
 			int height, int id) {
 		return new ImageImpl(id, imageUri, pageUris, height, width);
 	}
 
 	public Image createImage(String imageUri, String pageUri) {
 		List<String> pageUris = new ArrayList<String>();
 		pageUris.add(pageUri);
 		return this.createImage(imageUri, pageUris);
 	}
 
 	public Image createImage(String imageUri, String pageUri, int id) {
 		List<String> pageUris = new ArrayList<String>();
 		pageUris.add(pageUri);
 		return this.createImage(imageUri, pageUris, id);
 	}
 
 	public Image createImage(String imageUri, String pageUri, int height, int width) {
 		List<String> pageUris = new ArrayList<String>();
 		pageUris.add(pageUri);
 		return this.createImage(imageUri, pageUris, height, width);
 	}
 
 }
