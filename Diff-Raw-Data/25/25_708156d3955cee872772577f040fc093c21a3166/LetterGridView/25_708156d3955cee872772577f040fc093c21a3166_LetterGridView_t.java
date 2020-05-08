 package com.WordFinder;
 
 import java.util.Observable;
 
 import java.util.Observer;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.View;
 
 // -------------------------------------------------------------------------
 /**
  *  Character Grid
  *
  *  @author John Mooring (jmooring)
  *  @version Oct 30, 2011
  */
 public class LetterGridView extends View implements Observer{
 
     public LetterGridView(Context context, AttributeSet attrs) {
 	super(context, attrs);
     }
 
     public void onDraw(Canvas c) {

     }
 
     public void update(Observable observable, Object data) {
 	// TODO Auto-generated method stub

     }
 }
