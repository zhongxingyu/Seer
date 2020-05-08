 package com.flexymind.alpha.customviews;
 
 import android.content.Context;
 import android.graphics.*;
 import android.util.AttributeSet;
 import android.view.View;
 import com.flexymind.alpha.R;
 import com.flexymind.alpha.player.Tone;
 import com.larvalabs.svgandroid.SVG;
 import com.larvalabs.svgandroid.SVGParser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * All coordinates are in density pixels
  *
  */
 public class NoteBoard extends View {
     /** Number of lines of the board. The value is {@value}. */
     private static final int LINE_COUNT = 5;
     /** The maximum number of notes that can be drawn on the notes board. The value is {@value}. */
     private static final int MAX_NOTES = 7;
 
     /** A margin from each side of the display. */
     private int margin;
     /** The horizontal distance between two (@code Notes} */
     private int hStep;
 
     private Bitmap clef;
     private ArrayList<Note> notes = new ArrayList<Note>(MAX_NOTES);
     private HashMap<Tone, Integer> noteYMap = new HashMap<Tone, Integer>(MAX_NOTES);
 
     public NoteBoard(Context context) {
         super(context);
         init();
     }
 
     public NoteBoard(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public NoteBoard(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
 
     private void init() {
         clef = BitmapFactory.decodeResource(getResources(), R.drawable.treble_clef);
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
 
         // set parameters for drawing
         margin = this.getHeight() / 5;
        int linesHeight = (this.getHeight() - (int) (margin * 2.5));
         int linesWidth = (this.getWidth() - margin * 2);
         hStep = linesWidth / MAX_NOTES;
 
         // draw 5 lines
         SVG linesSvg = SVGParser.getSVGFromResource(getResources(), R.raw.linescut);
         Picture linesPicture = linesSvg.getPicture();
         canvas.drawPicture(linesPicture, new Rect(margin, margin, linesWidth + margin, linesHeight + margin));
 
         // draw the clef
         SVG clefSvg = SVGParser.getSVGFromResource(getResources(), R.raw.goodclef);
         Picture clefPicture = clefSvg.getPicture();
         RectF clefLimits = clefSvg.getLimits();
         float proportion = clefLimits.bottom / clefLimits.right;
         int clefHeight = linesHeight + margin;
         int clefLeftIndent = (int) (margin * 1.5);
         int clefTopIndent = margin / 2;
         canvas.drawPicture(clefPicture, new RectF(clefLeftIndent, clefTopIndent,
                                                   clefHeight / proportion + clefLeftIndent,
                                                   clefHeight + clefTopIndent + margin / 2));
     }
 
     /**
      * Puts the specified {@code note} on the specified position of the note board
      *
      * @param note
      */
     public void outputNote(Note note) {
 
     }
 
     /**
      * Puts specified {@code notes} on the note board
      *
      * @param notes
      */
     public void outputNotes(List<Note> notes) {
 
     }
 
     /**
      * Removes all notes
      */
     public void clear() {
         notes.clear();
     }
 
     /**
      * Changes the color of a note.
      *
      * @param note
      * @param position Position of a note on the note board, starting from 0.
      */
     public void highlightCorrectNote(Note note, int position, Color color) {
         // check range
         if (position < 0
                 || position >= notes.size()) {
             return;
         }
 
         notes.get(position).setColor(color);
     }
 
     /**
      * Changes the color of a note.
      *
      * @param note
      * @param position Position of a note on the note board, starting from 0.
      */
     public void highlightErrorNote(Note note, int position, Color color) {
         // check range
         if (position < 0
                 || position >= notes.size()) {
             return;
         }
 
         notes.get(position).setColor(color);
     }
 
 }
