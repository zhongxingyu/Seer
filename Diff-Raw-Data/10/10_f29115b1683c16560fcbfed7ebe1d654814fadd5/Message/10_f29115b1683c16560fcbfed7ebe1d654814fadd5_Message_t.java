 package com.hersan.bablrr;
 
 import processing.core.PApplet;
 import processing.core.PImage;
 import processing.core.PGraphics;
 
 import java.util.ArrayList;
 
 /* from Processing :
  * 		PImage : theImage
  *			createImage()
  *			copy()
  *		PImage : ti
  *			width + height
  *		PGraphics : theGraphics
  *			get() 
  */
 
 public class Message {
 
 	private final static int SUCCESS = 0;
 	private final static int FAIL = 1;
 
 	private ArrayList<Word> words;
 	private PImage theImage;
 	private PApplet myProcessing;
 
 	// constructor
 	//   create array of words and an image
 	//     msg := text message
 	//     img := path to image file
 	//     initw,inith := init dimensions for the final image
 	public Message(String msg, String img, PApplet pa, int initw, int inith) {
 		myProcessing = pa;
 		myProcessing.colorMode(PApplet.RGB);
 		// fill array with words from msg
 		MessageHelper(msg);
 		// add picture at the end
 		if((img != null)&&(!img.equals(""))){
 			words.add(new Picture(img,myProcessing));
 		}
 		// gen graphic
 		this.genImage(initw, inith);
 	}
 	// no image
 	public Message(String msg, PApplet pa, int initw, int inith) {
 		this(msg,"",pa,initw,inith);
 	}
 	// some defaut values for w and h
 	public Message(String msg, PApplet pa) {
 		this(msg,"",pa,300,200);
 	}
 	public Message(String msg, String img, PApplet pa) {
 		this(msg,img,pa,300,200);
 	}
 
 	// constructor helper to fill array with words from msg
 	private void MessageHelper(String msg) {
 		words = new ArrayList<Word>();
 		// fill array
 		String[] tWords = msg.split(" ");
 		for(String s : tWords){
 			if (s.length() > 0) {
 				words.add(new Word(s, myProcessing));
 			}
 		}
 
 		/*
 		for (int i=0; i<tWords.length; i++) {
 			if (tWords[i].length() > 0) {
 				words.add(new Word(tWords[i], myProcessing));
 			}
 		}
 		*/
 	}
 
 	private void genImage(int initw, int inith) {
 		int w = initw;
 		int h = inith;
 		int gcnt = 1;
 		
 		System.out.println("!!!! Try "+gcnt+". Size: "+w+" x "+h);
 		int result = placeWords(w, h);
 		
 		while (result == FAIL) {
 			w += (int)(0.2f*w);
 			if(w > (2*h)){
 				h += (int)(0.2f*h);				
 			}
 
 			gcnt++;
 			System.out.println("!!!! Try "+gcnt+". Size: "+w+" x "+h);
 			result = placeWords(w, h);
 		}
 	}
 
 	private int placeWords(int width_, int height_) {
 		/// DEBUG
 		//System.out.println("!!!!!!!: from placeWords : "+width_+" x "+height_);
 
 		// init graphic
 		PGraphics theGraphic = myProcessing.createGraphics(width_, height_, PApplet.JAVA2D);
 		theGraphic.beginDraw();
 		theGraphic.background(0xffffffff);
 
 		// keep track of max dimensions
 		float maxX = 0;
 		float maxY = 0;
 
 		// current offsets for placing
 		float xoff = 0;
 		float yoff = 0;
 
 		// for every word w
 		for(Word w : words){
 			PImage ti = w.getDImage();
 
 			// DEBUG
 			//System.out.println("!!!! word "+i+" : "+ti.width+ " x "+ti.height);
 
			// special case to take care of long words that don't fit in their own line.
			if ((ti.width>theGraphic.width)) {
 				theGraphic.endDraw();
 				theGraphic.dispose();
 				theGraphic.delete();
 				return FAIL;
 			}
 			
 			// if word doesn't fit in this line.
			//    this doens't work when very first word of a line doesn't fit in the line.
 			if ((xoff+ti.width)>theGraphic.width) {
 				// reset xoffset
 				xoff = 0;
 				// bump up yoffset
 				yoff = maxY+2;
 			}
 
 			// check if out of bounds in the y direction
 			if ((yoff+ti.height) > theGraphic.height) {
 				theGraphic.endDraw();
 				theGraphic.dispose();
 				theGraphic.delete();
 				return FAIL;
 			}
 
 			// offsets should be good: place word
 			theGraphic.image(ti, xoff, yoff);
 
 			// bump up xoffset
 			xoff += ti.width+2;
 
 			// update max values
 			if (xoff > maxX) maxX = xoff;
 			if ((yoff+ti.height)>maxY) maxY = yoff+ti.height;			
 		}
 		// should have image
 		theGraphic.endDraw();
 
 		// create, size and copy onto theImage
 		theImage = myProcessing.createImage((int)(maxX+2), (int)(maxY+2), PApplet.RGB);
 		theImage.copy(theGraphic.get(), 0, 0, (int)(maxX+2), (int)(maxY+2), 0, 0, theImage.width, theImage.height);
 
 		// clean up
 		theGraphic.dispose();
 		theGraphic.delete();
 
 		// success
 		return SUCCESS;
 	}
 
 	// get Image
 	public PImage getImage() {
 		return theImage;
 	}
 
 	// access words
 	public Word getWord(int i) {
 		if ((i>-1)&&(i<words.size())) {
 			return words.get(i);
 		}
 		else {
 			return words.get(0);
 		}
 	}
 }
 
