 package de.fhdw.atpinfo.linafm;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import de.fhdw.atpinfo.linafm.Tile.TileState;
 
 /**
  * Diese Klasse beinhaltet alles, was zum aktuellen Spiel gehört.
  * Sie wird instanziiert, sobald ein neues Spiel gestartet wird
  * 
  * @author Esther Hentrich, Daniel Philipp, Alexander Brügmann
  * @version 0.1
  *
  */
 public class Spiel extends Activity implements OnClickListener, OnLongClickListener {
 
 	/**
 	 * Unser Spielfeld
 	 */
 	private Spielfeld spielfeld;
 	
 	/**
 	 * Der Context
 	 */
 	private Context context;
 	
 	/**
 	 * Die ID des aktuellen Levels
 	 */
 	private int levelId;
 	
 	/**
 	 * Der Button zum Öffnen des Popups
 	 */
 	private Button mBtnPopup;
 	
 	/**
 	 * Der Button zur Validierung
 	 */
 	private Button mBtnCheck;
 	
 	/**
 	 * Der Button zum Zurücksetzen des Spielfeldes
 	 */
 	private Button mBtnReset;
 	
 	/**
 	 * Der Popup-Dialog
 	 */
 	private Dialog mDlgPopup;
 	
 	/**
 	 * Das aktuell zum Tausch ausgewählte Plättchen
 	 */
 	private Tile activeTile = null;
 	
 	/**
 	 * Das Raster, welches auf dem Spielfeld zu sehen ist
 	 */
 	private Raster rasterUnten;
 
 	/**
 	 * Dieses Raster erscheint im Popup
 	 */
 	private Raster rasterPopup;
 
 	/**
 	 * Wird aufgerufen, sobald ein neues Spiel erstellt wird
 	 * 
 	 * @param savedInstanceState
 	 * @param spielfeld
 	 */
 	protected void onCreate(Bundle savedInstanceState) {
 		context = Spiel.this;
 		super.onCreate(savedInstanceState);
 		
 		// die Level-ID steckt in den Extras
 		Bundle b = getIntent().getExtras();
 		levelId = b.getInt("levelId");
 
 		// Level laden
 		try {
 			spielfeld = LevelHandler.loadLevel(levelId, context);
 		} catch (XmlPullParserException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// Spielfeld anzeigen
 		String levelName = LevelHandler.getLevelById(levelId);
 		setContentView(R.layout.spielfeld);
 		setTitle(levelName);
 		
 		// Bild oben
 		ImageView image = (ImageView)findViewById(R.id.imgOben);
 		image.setImageBitmap(spielfeld.getImg());
 		
 		// Raster unten
 		FrameLayout frame = (FrameLayout)findViewById(R.id.tilesUnten);
 		rasterUnten = spielfeld.getRasterUnten();
 		rasterUnten.setOnClickListenerForAllTiles(this);
 		rasterUnten.setOnLongClickListenerForAllTiles(this);
 		frame.addView(rasterUnten);
 		
 		// auch Plättchen im Popup sollen klickbar sein
 		rasterPopup = spielfeld.getRasterPopup();
 		rasterPopup.setOnClickListenerForAllTiles(this);		
 		
 		// Popup generieren
 		mDlgPopup = drawPopupDialog();
 		
 		mBtnPopup = (Button) findViewById(R.id.btnPopup);
 		mBtnPopup.setOnClickListener(this);
 		
 		// Validierung
 		mBtnCheck = (Button) findViewById(R.id.btnCheck);
 		mBtnCheck.setOnClickListener(this);
 	
 	}
 	
 	/**
 	 * Sichert die Position der Plättchen, damit diese rekonstruiert werden kann,
 	 * wenn z.B. der Bildschirm gedreht wird
 	 * @param outState beinhaltet Positionen der Plättchen
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState)
 	{
 	    super.onSaveInstanceState(outState);
 	    outState.putIntArray("rPopup", spielfeld.getRasterPopup().getTileIDs()); // Raster Popup
 	    outState.putIntArray("rUnten", spielfeld.getRasterUnten().getTileIDs()); // Raster unten			
 	}
 	
 	/**
 	 * Sortiert die Plättchen wieder so ein, wie sie waren, bevor z.B. der Bildschirm
 	 * gedreht wurde
 	 * @param tileOrder Bundle, welches die Reihenfolge der Plättchen beinhaltet
 	 */
 	@Override
 	protected void onRestoreInstanceState(Bundle tileOrder)
 	{
 		super.onRestoreInstanceState(tileOrder);
 		
 		if ( tileOrder != null )
 		{
 			// unsere beiden Raster
 			Raster rUnten = spielfeld.getRasterUnten();
 			Raster rPopup = spielfeld.getRasterPopup();
 			
 			// erstmal alle Plättchen einsammeln
 			List<Tile> allTiles = new ArrayList<Tile>();
 			Collections.addAll(allTiles, rPopup.getTiles());
 			Collections.addAll(allTiles, rUnten.getTiles());
 			
 			// IDs der Plättchen aus dem Bundle auslesen
 			int rPopupIDs[] = tileOrder.getIntArray("rPopup");
 			int rUntenIDs[] = tileOrder.getIntArray("rUnten");
 			int rasterIDs[][] = { rPopupIDs, rUntenIDs };
 			
 			// alle Plättchen entfernen...
 			rUnten.clear();
 			rPopup.clear();
 			
 			// ... und in der richtigen Reihenfolge neu befüllen
 			// zuerst das Popup-Raster
 			Raster currRaster = rPopup;
 			for ( int[] IDs : rasterIDs ) {
 				for ( int id : IDs ) {
 					// Anhand der ID ermitteln wir das passende Plättchen
 					for ( Tile t : allTiles ) {
 						if ( t.getTileId() == id ) {
 							allTiles.remove(t);					
 							currRaster.addTile(t, -1);
 							break;
 						}
 					}
 				}
 				
 				// und jetzt das untere
 				currRaster = rUnten;
 			}
 			
 		}
 	}
 	/**
 	 * Popup generieren
 	 */
 	private Dialog drawPopupDialog() {
         // Neuen Dialog initialisieren
 		final Dialog dialog = new Dialog(context);
         dialog.setContentView(R.layout.popup);
         dialog.setTitle(R.string.popup);
         dialog.setCancelable(true);
         dialog.setCanceledOnTouchOutside(true);
         
         FrameLayout fl = (FrameLayout) dialog.findViewById(R.id.popup);
         // Raster zum FrameLayout hinzufügen
         fl.addView(rasterPopup);
         
         // Abbrechen-Button
         Button button = (Button) dialog.findViewById(R.id.btnAbbruch);
         button.setOnClickListener(this);
         
         // Tipp-Button
         button = (Button) dialog.findViewById(R.id.btnTipp);
         button.setOnClickListener(this);
         
         // Reset-Button
         button = (Button) dialog.findViewById(R.id.btnReset);
         button.setOnClickListener(this);
         
 		return dialog;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 			case R.id.btnPopup:
 				if( mDlgPopup.isShowing() ) {
 					hidePopup();
 				} else {
 					showPopup();
 				}
 				break;
 			case R.id.btnCheck:
 				spielfeld.validate(context);
 				break;
 			case R.id.btnAbbruch:
 				mDlgPopup.cancel();
 				break;
 			case R.id.btnTipp:
 				spielfeld.tipp(context);
 				break;
 			case R.id.btnReset:
 				if(!rasterPopup.isEmpty()) {
 					
 					Tile[] tmp = rasterPopup.getTiles();
 					for (int i = 0; i < tmp.length; i++)
 					{
 						tmp[i] = new Tile(context, -1, null);
 						tmp[i].setNumeralImage(i);
 					}
 					rasterPopup.setTiles(tmp);
 					rasterPopup.buildRaster(context);
 					rasterPopup.setOnClickListenerForAllTiles(this);
 				}
 				// unteres Raster wieder auf den Original
 				rasterUnten.setTiles(rasterUnten.getOriginalFelder().clone());
 				rasterUnten.buildRaster(context);
 				rasterUnten.setOnClickListenerForAllTiles(this);
 				
 				
 				break;
 		}
 		
 		// Wurde ein Plättchen geklickt?
 		if ( v instanceof Tile )
 			onTileClick( (Tile)v );
 	}
 	
 	@Override
 	public boolean onLongClick(View v) {
 		resetTileFromPopUp( (Tile)v );
 		return false;
 	}
 
 	/**
 	 * Wird beim Klicken eines Plättchens aufgerufen
 	 * @param v das geklickte Plättchen
 	 */
 	private void onTileClick(Tile v) {
 		// Falls wir kein Dummy sind und noch kein Plättchen zum Tausch ausgewählt wurde...
 		if ( !v.isDummy() && activeTile == null ) {
 			// ... dann wählen wir uns selbst aus
 			activeTile = v;
 			v.setState(TileState.SELECTED);
 			
 			// befindet sich unser Plättchen im unteren Raster?
 			// ( getParent():  Tile --> TableRow --> Raster )
 			if ( ((View)v.getParent().getParent()).getId() == R.id.rasterUnten ) {
 				showPopup();
 			}
 		}
 		else if ( activeTile != null ) {
 			// Wurde ein anderes Plättchen gedrückt?
 			if ( v != activeTile )
 			{
 				v.setState(TileState.SELECTED);
 				
 				// Raster der zwei Tauschpartner
 				Raster srcRaster = (Raster) activeTile.getParent().getParent();
 				Raster destRaster = (Raster) v.getParent().getParent();
 				
 				// Position der zwei Plättchen im Raster
 				int srcPos = srcRaster.getTilePosition(activeTile);
 				int destPos = destRaster.getTilePosition(v);
 				
 				// Plättchen entfernen...
 				// keine Aktualisierung der Ansicht, da dies eh gleich durch das Hinzufügen 
 				// neuer Plättchen geschieht
 				srcRaster.removeTile(srcPos);
 				destRaster.removeTile(destPos);
 				
 				// ...und umgekehrt wieder einfügen.
 				srcRaster.addTile(v, srcPos);
 				destRaster.addTile(activeTile, destPos);
 				
 				// Status wieder normal setzen
 				if ( v.isDummy() )
 					v.setState(TileState.EMPTY);
 				else
 					v.setState(TileState.NORMAL);
 			}
 			
 			// kein aktives Plättchen mehr
 			activeTile.setState(TileState.NORMAL);
 			activeTile = null;
 		}
 		
 		
 
 	}
 	
 	/**
 	 * Wird beim LongClick eines Plättchens aufgerufen
 	 * @param v das geklickte Plättchen
 	 */
 	public void resetTileFromPopUp(Tile v) {
 		// befindet sich unser Plättchen im unteren Raster?
 		// ( getParent():  Tile --> TableRow --> Raster )
 		if ( ((View)v.getParent().getParent()).getId() != R.id.rasterUnten ) {
 					
 			// Position des Plättchens im Raster
 			int srcPos = rasterPopup.getTilePosition(v);
 			int destPos = 0;
 			
 			//freies Feld suchen
 			Tile[] felder = rasterUnten.getTiles();
 			Tile dummyTile = null;
 			for (int i = 0; i <= felder.length; i++){
 				if (felder[i].isDummy()) {
 					destPos = rasterUnten.getTilePosition(felder[i]);
 					dummyTile = felder[i];					
 					break;
 				}
 			}
 			
 			
 			// Plättchen entfernen...
 			rasterPopup.removeTile(srcPos);
 			rasterUnten.removeTile(destPos);
 			
 			// ...und umgekehrt wieder einfügen.
 			rasterUnten.addTile(v, destPos);
 			rasterPopup.addTile(dummyTile, srcPos);
 		}
 	}
 	
 	/**
 	 * Popup schließen
 	 */
 	public void hidePopup() {
 			mDlgPopup.hide();
 	}
 	
 	/**
 	 * Popup öffnen
 	 */
 	public void showPopup() {
 		// Popup schon offen?
 		if ( mDlgPopup.isShowing() )
 			return;
 		
 		// Achtung, Pfusch! Das geht bestimmt auch irgendwie schöner...
 		// Größe des Popups an das untere Raster angleichen
 		FrameLayout fl = (FrameLayout) mDlgPopup.findViewById(R.id.popup); 
 		// Höhe des Containers für das Popup-Raster setzen
 		fl.setLayoutParams(new LinearLayout.LayoutParams(
 				LayoutParams.FILL_PARENT, 
 				spielfeld.getRasterUnten().getHeight() // Höhe unteres Raster
 		));
 		// Breite des Popup-Fensters setzen
 		mDlgPopup.getWindow().setLayout(
 				spielfeld.getRasterUnten().getWidth(), // Breite unteres Raster
 				LayoutParams.WRAP_CONTENT
 		);
 		// -- Pfusch Ende --
 		
 		mDlgPopup.show();
 	}
 }
