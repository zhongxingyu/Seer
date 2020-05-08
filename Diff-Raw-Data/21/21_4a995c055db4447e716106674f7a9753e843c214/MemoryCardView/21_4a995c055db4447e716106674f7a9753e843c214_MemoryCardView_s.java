 package se.chalmers.dat255.sleepfighter.challenge;
 
 import android.content.Context;
 import android.widget.Button;
 
 /**
  * Represents a memory card graphically
  *
  * @author Eric Arneb�ck
  * @version 1.0
  * @since Sep 28, 2013
  */
 
 public class MemoryCardView extends Button {
 
 	// what to show when the memory card is flipped over and hidden.
   	private static String HIDDEN =  "";
   	// what to show when the memory card s flipped over and shown.
   	private String SHOWN;
   	
   	private int position;
   	
   	private String currentImage;
     
 	
 	public MemoryCardView(Context context) {
 		super(context);
 	}
 	
 	public void hide() {
 		currentImage = HIDDEN;
 		super.setText(currentImage);
 	}
 	
 	public void show() {
 		currentImage = SHOWN;
 		super.setText(currentImage);
 	}
 	
 	public boolean isHidden() {
		return this.currentImage == HIDDEN;
 	}
 	
 	public boolean isShown() {
 		return !isHidden();
 	}
 	
 	public void flip() {
 		if(isHidden()) {
 			this.show();
 		} else
 			this.hide();
 	}
 	
 	public void setImage(String image) {
 		//super.setBackgroundResource(android.R.color.white);
 
 		SHOWN = image;
 		
 		// all cards are hidden by default. 
 		hide();
 	}
 
 	public void setPosition(int position) {
 		this.position = position;
 	}
 	
 	public int getPosition() {
 		return this.position;
 	}
 }
