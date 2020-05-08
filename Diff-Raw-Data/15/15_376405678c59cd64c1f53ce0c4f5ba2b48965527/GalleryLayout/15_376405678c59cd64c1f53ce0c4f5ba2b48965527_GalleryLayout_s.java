 package com.example.t_gallery;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.graphics.BitmapFactory;
 
 import com.example.t_gallery.GalleryList.Config;
 
 
 class ImageCell {
 	ImageCell(long aId, int aWidth, int aHeight){
 		id = aId;
 	    inWidth = aWidth;
 	    inHeight = aHeight;
 	    yRatio = (float)inHeight / (float)inWidth;
 	}
 	
 	public void multiplyXGravity(float factor){
 		xGravity *= factor;
 	}
 	
 	public void applyWidth(int totalWidth){
 		outWidth = (int)(xGravity * totalWidth);
 		outHeight = (int)(yRatio * outWidth);
 	}
 	
 	public void adjustXGravity(float SiblingYRatio){
 		xGravity = SiblingYRatio/(yRatio + SiblingYRatio);
 	}
 	
 	
 	public long id = 0;
 	
 	public int inWidth = 0;
 	public int inHeight = 0;
 	
 	public int outWidth = 0;
 	public int outHeight = 0;
 	
 	public int outX = 0;
 	public int outY = 0;
 	
 	/*For calculate*/
 	public float xGravity = 1.0f;
 	public float yRatio = 0.0f;
 }
 
 
 class ImageLineGroup {
 
 	ImageLineGroup(){
 		imageList = new ArrayList<ImageCell>();
 	}
 
 	public void addImage(long id, int width, int height){
 		ImageCell image = new ImageCell(id, width, height);
 		imageList.add(image);
 	}
 	
 	public void addImage(ImageCell image){
 		imageList.add(image);
 	}
 	
 	public int getImageNum(){
 		return imageList.size();
 	}
 	
 	public ImageCell getImage(int i){
 		return imageList.get(i);
 	}
 	
 	public int getHeight(){
 		return height;
 	}
 	
 	protected int height;
 	public ArrayList<ImageCell> imageList;
 }
 
 class ImageSingleLineGroup extends ImageLineGroup{
 	private int totalWidth = 1080;
 	private static final int MAX_HEIGHT = 540;
 	private static final int MIN_HEIGHT = 180;
 	
 	ImageSingleLineGroup(int containerWidth){
 		totalWidth = containerWidth;
 	}
 	
 	
 	private void layout(){
 		int maxContentWidth = totalWidth;
 		int contentWidth = 0;
 		
 		/*First Round, to resize all picture as high as MAX_HEIGHT*/
 		for (int i=0; i<imageList.size(); i++){
 			ImageCell image = imageList.get(i);
 			image.outHeight = MAX_HEIGHT;
 			image.outWidth = (image.inWidth*image.outHeight)/image.inHeight;
 			
 			contentWidth += image.outWidth;
 		}
 		
 		if (contentWidth > maxContentWidth){
 			for (int i=0; i<imageList.size(); i++){
 				ImageCell image = imageList.get(i);
 				
 				image.outHeight = (image.outHeight*maxContentWidth)/contentWidth;
 				image.outWidth = (image.outWidth*maxContentWidth)/contentWidth;
 				
				height = image.outHeight;
 			}
 		}
 		
 		contentWidth = 0;
 		
 		for (int i=0; i<imageList.size(); i++){
 			ImageCell image = imageList.get(i);
 			
 			image.outX = contentWidth;
 			image.outY = 0;
 			
			contentWidth += image.outWidth;
 		}
 		
 	}
 	
 	public boolean properForMoreImage(){
 		if (imageList.size() >= 4 || height<=MIN_HEIGHT){
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean needMoreImage(){
 		if (imageList.isEmpty()){
 			return true;
 		}
 		else if (imageList.size() >= 4){
 			layout();
 			return false; //Consider as "full" if 4 pictures in one line...
 		}
 		
 		layout();
 		
 		int contentWidth = 0;
 		
 		for (int i=0; i<imageList.size(); i++){
 			ImageCell image = imageList.get(i);			
			contentWidth += image.outWidth;
 		}
 		
 		if (contentWidth < (totalWidth-30)){
 			return true;
 		}
 		else {
 			return false;
 		}
 
 	}	
 }
 
 class ImageProcessBuffer{	
 	ImageProcessBuffer(int length){
 		buffer = new ArrayList<ImageCell>();
 		maxLength = length;
 	}
 	
 	public void add(ImageCell cell){
 		buffer.add(cell);
 	}
 	
 	public ImageCell remove(int index){
 		return buffer.remove(index);
 	}
 	
 	public boolean isFull(){
 		if (buffer.size() == maxLength){
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public boolean isEmpty(){
 		return buffer.isEmpty();
 	}
 	
 	public ArrayList<ImageCell> shed(int length){
 		if (length > buffer.size()){
 			length = buffer.size();
 		}
 		
 		ArrayList<ImageCell> result = new ArrayList<ImageCell>();
 		
 		for (int i=0; i<length; i++){
 			result.add(buffer.remove(0));
 		}
 		
 		return result;
 	}
 	
 	public ArrayList<ImageCell> buffer;
 	private int maxLength = 0;
 }
 
 interface ImageRichLinePattern {
 	/*Return the number of items matching the pattern*/
 	int match(ArrayList<ImageCell> images);
 	
 	/*Just return how many items the pattern applies*/
 	int imageCount();
 	
 	/*Return the height of line*/
 	int layout(ArrayList<ImageCell> images, int totalWidth);
 }
 
 class ImageRichLinePatternCollection {
 	private static final int PATTERN_NUM = 3;
 	
 	private static final int IMAGE_PORTRAIT = 1;
 	private static final int IMAGE_LANDSCAPE = 2;
 	
 	public static boolean isImagePortrait(ImageCell image){
 		return (image.yRatio > 1);
 	}
 	
 	public static boolean isImageSlim(ImageCell image){
 		return (image.yRatio >= 1.6) || (image.yRatio <= 0.625);
 	}
 	
 	ImageRichLinePatternCollection(){
 		patterns = new ImageRichLinePattern[PATTERN_NUM];
 		availablePatterns = new ArrayList<Integer>();
 		
 		patterns[0] = new ImageRichLinePattern(){
 			public int imageCount(){
 				return 5;
 			}
 
 			public int match(ArrayList<ImageCell> images) {
 				
 				if (images.size() < 5){
 					return -1;
 				}
 				
 				for (int i=0; i<5; i++){
 					if (false == isImagePortrait(images.get(i))){
 						return -1;
 					}
 				}
 				return 5;
 			}
 
 			public int layout(ArrayList<ImageCell> images, int totalWidth) {		
 				for (int i=0; i<5; i++){
 					images.get(i).xGravity = 1.0f;
 				}
 				
 				images.get(0).xGravity = images.get(1).yRatio/(images.get(0).yRatio + images.get(1).yRatio);
 				images.get(1).xGravity = images.get(0).yRatio/(images.get(1).yRatio + images.get(0).yRatio);
 				
 				images.get(2).xGravity = images.get(3).yRatio/(images.get(2).yRatio + images.get(3).yRatio);
 				images.get(3).xGravity = images.get(2).yRatio/(images.get(3).yRatio + images.get(2).yRatio);
 				
 				float compoundYRatio = images.get(0).xGravity*images.get(0).yRatio + images.get(2).xGravity*images.get(2).yRatio;
 				float compoundXGravity = images.get(4).yRatio/(compoundYRatio + images.get(4).yRatio);
 				
 				images.get(4).xGravity = compoundYRatio / (compoundYRatio + images.get(4).yRatio);
 				
 				images.get(0).xGravity *= compoundXGravity;
 				images.get(1).xGravity *= compoundXGravity;
 				images.get(2).xGravity *= compoundXGravity;
 				images.get(3).xGravity *= compoundXGravity;
 				
 				for (int i=0; i<5; i++){
 					ImageCell image = images.get(i);
 					image.outWidth = (int)(image.xGravity * totalWidth);
 					image.outHeight = (int)(image.outWidth * image.yRatio);
 				}
 				
 				images.get(0).outX = 0;
 				images.get(0).outY = 0;
 				images.get(1).outX = images.get(0).outWidth;
 				images.get(1).outY = 0;
 				images.get(2).outX = 0;
 				images.get(2).outY = images.get(0).outHeight;
 				images.get(3).outX = images.get(2).outWidth;
 				images.get(3).outY = images.get(0).outHeight;
 				images.get(4).outX = images.get(0).outWidth + images.get(1).outWidth;
 				images.get(4).outY = 0;
 				
 				return images.get(4).outHeight;
 			};
 			
 		};
 		
 		patterns[1] = new ImageRichLinePattern(){
 			public int imageCount(){
 				return 5;
 			}
 
 			public int match(ArrayList<ImageCell> images) {
 				
 				if (images.size() < 5){
 					return -1;
 				}
 				
 				for (int i=0; i<5; i++){
 					if (false == isImagePortrait(images.get(i))){
 						return -1;
 					}
 				}
 				return 5;
 			}
 
 			public int layout(ArrayList<ImageCell> images, int totalWidth) {		
 				for (int i=0; i<5; i++){
 					images.get(i).xGravity = 1.0f;
 				}
 				
 				images.get(1).xGravity = images.get(2).yRatio/(images.get(1).yRatio + images.get(2).yRatio);
 				images.get(2).xGravity = images.get(1).yRatio/(images.get(2).yRatio + images.get(1).yRatio);
 				
 				images.get(3).xGravity = images.get(4).yRatio/(images.get(3).yRatio + images.get(4).yRatio);
 				images.get(4).xGravity = images.get(3).yRatio/(images.get(4).yRatio + images.get(3).yRatio);
 				
 				float compoundYRatio = images.get(1).xGravity*images.get(1).yRatio + images.get(3).xGravity*images.get(3).yRatio;
 				float compoundXGravity = images.get(0).yRatio/(compoundYRatio + images.get(0).yRatio);
 				
 				images.get(0).xGravity = compoundYRatio / (compoundYRatio + images.get(0).yRatio);
 				
 				images.get(1).xGravity *= compoundXGravity;
 				images.get(2).xGravity *= compoundXGravity;
 				images.get(3).xGravity *= compoundXGravity;
 				images.get(4).xGravity *= compoundXGravity;
 				
 				for (int i=0; i<5; i++){
 					ImageCell image = images.get(i);
 					image.outWidth = (int)(image.xGravity * totalWidth);
 					image.outHeight = (int)(image.outWidth * image.yRatio);
 				}
 				
 				images.get(0).outX = 0;
 				images.get(0).outY = 0;
 				images.get(1).outX = images.get(0).outWidth;
 				images.get(1).outY = 0;
 				images.get(2).outX = images.get(0).outWidth + images.get(1).outWidth;
 				images.get(2).outY = 0;
 				images.get(3).outX = images.get(0).outWidth;
 				images.get(3).outY = images.get(1).outHeight;
 				images.get(4).outX = images.get(0).outWidth + images.get(3).outWidth;
 				images.get(4).outY = images.get(1).outHeight;
 				
 				return images.get(0).outHeight;
 			};
 			
 		};
 		
 		patterns[2] = new ImageRichLinePattern(){
 			public int imageCount(){
 				return 5;
 			}
 
 			public int match(ArrayList<ImageCell> images) {
 				
 				if (images.size() < 5){
 					return -1;
 				}
 				
 				for (int i=0; i<5; i++){
 					if (false == isImagePortrait(images.get(i))){
 						return -1;
 					}
 				}
 				return 5;
 			}
 
 			public int layout(ArrayList<ImageCell> images, int totalWidth) {		
 				for (int i=0; i<5; i++){
 					images.get(i).xGravity = 1.0f;
 				}
 				
 				float leftCompoundYRatio = images.get(0).yRatio + images.get(3).yRatio;
 				float rightCompoundYRatio = images.get(2).yRatio + images.get(4).yRatio;
 				
 				float tempYBase = leftCompoundYRatio*rightCompoundYRatio + leftCompoundYRatio*images.get(1).yRatio + rightCompoundYRatio*images.get(1).yRatio;
 				images.get(0).xGravity = images.get(3).xGravity = images.get(1).yRatio*rightCompoundYRatio/tempYBase;
 				images.get(2).xGravity = images.get(4).xGravity = images.get(1).yRatio*leftCompoundYRatio/tempYBase;
 				images.get(1).xGravity = leftCompoundYRatio*rightCompoundYRatio / tempYBase;
 				
 				for (int i=0; i<5; i++){
 					ImageCell image = images.get(i);
 					image.outWidth = (int)(image.xGravity * totalWidth);
 					image.outHeight = (int)(image.outWidth * image.yRatio);
 				}
 				
 				images.get(0).outX = 0;
 				images.get(0).outY = 0;
 				images.get(1).outX = images.get(0).outWidth;
 				images.get(1).outY = 0;
 				images.get(2).outX = images.get(0).outWidth + images.get(1).outWidth;
 				images.get(2).outY = 0;
 				images.get(3).outX = 0;
 				images.get(3).outY = images.get(0).outHeight;
 				images.get(4).outX = images.get(2).outX;
 				images.get(4).outY = images.get(2).outHeight;
 				
 				return images.get(1).outHeight;
 			};
 			
 		};
 	
 
 	}
 	
 	public int checkPattern(ArrayList<ImageCell> images){
 		availablePatterns.clear();
 		
 		for (int i=0; i<PATTERN_NUM; i++){
 			if (patterns[i].match(images) != -1){
 				availablePatterns.add(i);
 			}
 		}
 		
 		return availablePatterns.size();
 	}
 	
 	public int pickPattern(int index){
 		return patterns[availablePatterns.get(index)].imageCount();
 	}
 	
 	public int applyPattern(ArrayList<ImageCell> images, int index, int totalWidth){
 		return patterns[availablePatterns.get(index)].layout(images, totalWidth);
 	}
 	
 	ImageRichLinePattern[] patterns;
 	ArrayList<Integer> availablePatterns;
 }
 
 class ImageRichLineGroup extends ImageLineGroup{
 	ImageRichLineGroup(){
 	}
 	
 	void addImages(ArrayList<ImageCell> images){
 		imageList = images;
 	}
 }
 
 public class GalleryLayout {
 	private static final int MAX_BUFFER_LENGTH = 5;
 	
 	GalleryLayout(int containerWidth){
 		totalWidth = containerWidth;
 		lines = new ArrayList<ImageLineGroup>();
 		
 		itemBuffer = new ImageProcessBuffer(MAX_BUFFER_LENGTH);
 		patterns = new ImageRichLinePatternCollection();
 		random = new Random();
 	}
 	
 	public void addLine(ImageLineGroup aLine){
 		lines.add(aLine);
 	}
 	
 	private void processImageBuffer(){
 		/*Try to find some pattern*/
 		int availPattern = patterns.checkPattern(itemBuffer.buffer);
 		
 		if (availPattern > 0){
 			ImageRichLineGroup line = new ImageRichLineGroup();
 			
 			int choice = random.nextInt(availPattern);
 			int consumeCount = patterns.pickPattern(choice);
 			ArrayList<ImageCell> images = itemBuffer.shed(consumeCount);
 			int height = patterns.applyPattern(images, choice, totalWidth);
 			line.addImages(images);
 			line.height = height;
 			addLine(line);
 		}
 		else {
 			/*No pattern found */
 			ImageSingleLineGroup line = new ImageSingleLineGroup(totalWidth);
 			while (true == line.needMoreImage() && false == itemBuffer.isEmpty()){
 				line.addImage(itemBuffer.remove(0));
 			}
 			addLine(line);
 		}
 	}
 	
 	public void addImage(long id, int width, int height){
 		
 		itemBuffer.add(new ImageCell(id, width, height));
 		
 		if (itemBuffer.isFull()){
 			processImageBuffer();
 		}
 	}
 	
 	public void addImageFinish(){
 		processImageBuffer();
 	}
 	
 	public int getLineNum(){
 		return lines.size();
 	}
 	
 	public ArrayList<ImageLineGroup> lines = null;
 	private  ImageProcessBuffer itemBuffer = null;
 	private ImageRichLinePatternCollection patterns = null;
 	private Random random = null;
 	private int totalWidth = 0;
 }
