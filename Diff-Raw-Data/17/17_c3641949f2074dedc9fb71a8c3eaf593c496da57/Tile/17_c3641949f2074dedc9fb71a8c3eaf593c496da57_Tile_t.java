 package de.fhdw.atpinfo.linafm;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.PorterDuff.Mode;
 import android.widget.ImageButton;
 import android.widget.TableRow.LayoutParams;
 
 /**
  * Klasse Plättchen, abgeleitet vom ImageButton
  * Die werden wir später hin und her drehen!
  * 
  * @author Esther Hentrich, Daniel Philipp, Alexander Brügmann
  * @version 0.1
  *
  */
 public class Tile extends ImageButton {
 	
 	/**
 	 * Id des Plättchens: Wird pro Spiel von 0 bis x vergeben
 	 */
 	private int id;
 	
 	/**
 	 * Bild für die Vorderseite
 	 */
 	private Bitmap front;
 	
 	/**
 	 * Bild für die Rückseite
 	 */
 	private Bitmap back;
 	
 	/**
 	 * Ist das Plättchen umgedreht (Rückseite oben?)
 	 */
 	private boolean turned = false;
 
 	public Tile(Context context, int id, Bitmap front, Bitmap back) {
 		super(context);
 		this.id = id;
 		this.front = front;
 		this.back = back;
 		
 		setImageBitmap(front);
 
 		// Breite auf 0 Pixel festgesetzt, da anderenfalls die Buttons nicht
 		// gleichmäßig in der Zeile verteilt werden.
 		LayoutParams params = new LayoutParams(
                 0, // width
                 LayoutParams.MATCH_PARENT, // height 
                 1.0f); // layout_weight
 		
 		setScaleType(ScaleType.FIT_CENTER);
 		setLayoutParams(params);
 
 	}
 	
 	/**
 	 * @return the id
 	 */
 	public int getTileId() {
 		return id;
 	}
 
 	/**
 	 * Plättchen gedreht?
 	 * @return true, falls Plättchen umgedreht
 	 */
 	public boolean isTurned() {
 		return turned;
 	}
 
 	/**
 	 * Plättchen umdrehen
 	 */
 	public void turnAround()
 	{
 		if (turned)
 			setImageBitmap(front);
 		else
 			setImageBitmap(back);
 		
 		turned = !turned;
 	}
 
 	/**
 	 * Methode um das Bild eines Plättchens zu ändern
 	 * 
 	 * @param img Bild
 	 * @param back true setzen um Rückseite zu ändern
 	 */
 	public void setImage(Bitmap img, boolean back) {
 		if (!back)
 			this.front = img;
 		else
 			this.back = img;
 	}
 	
 	/**
 	 * Setze einen leeren Status für das Tile.
 	 */
 	public void setStateEmpty() {
		getBackground().setColorFilter(Color.GRAY, Mode.MULTIPLY);
 	}
 	
 	/**
 	 * Versetze das Tile in den Ausgangsstatus.
 	 */
 	public void setStateNormal() {
		getBackground().setColorFilter(Color.TRANSPARENT, Mode.MULTIPLY);
 	}
 
 	/**
 	 * Markiere das Tile als ausgewählt.
 	 */
 	public void setStateSelected() {
		getBackground().setColorFilter(Color.GREEN, Mode.MULTIPLY);
 	}
 }
