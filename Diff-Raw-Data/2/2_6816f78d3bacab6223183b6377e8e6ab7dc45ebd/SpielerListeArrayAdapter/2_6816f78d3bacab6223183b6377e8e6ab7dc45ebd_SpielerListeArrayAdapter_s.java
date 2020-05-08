 package com.dhbw.dvst.helper;
 
 import java.util.ArrayList;
 import android.app.Activity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.dhbw.dvst.R;
 import com.dhbw.dvst.activities.SpielerBearbeitenActivity;
 import com.dhbw.dvst.model.Control;
 import com.dhbw.dvst.model.Spiel;
 import com.dhbw.dvst.model.Spieler;
 
 public class SpielerListeArrayAdapter extends ArrayAdapter<Spieler> {
 
 	/**
 	 * Speichert alle Spieler als Values und einen hochzählenden int-Wert als Key
 	 */
    // private HashMap<Integer, Spieler> mIdMap = HashMap<Integer, Spieler>();
 	SparseArray<Spieler> mIdMap = new SparseArray<Spieler>();
     private Activity activity;
 	private int position;
 	private View zeilenansicht;
 
     /**
      * 
      * @param context momentaner Kontext
      * @param resourceId ID der Layout-Datei
      * @param textViewId ID des Textviews in der Layout-Datei
      * @param alleSpieler Listenobjekte
      */
     public SpielerListeArrayAdapter(Activity activity, int resourceId, int textViewId,
         ArrayList<Spieler> alleSpieler) {
     	super(activity, resourceId, textViewId, alleSpieler);
     	this.activity = activity;
     	for (int i = 0; i < alleSpieler.size(); ++i) {
     		mIdMap.put(i, alleSpieler.get(i));
     	}
     }
 
     @Override
     public boolean hasStableIds() {
       return true;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
     	LayoutInflater inflater = (LayoutInflater) activity
     	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	    View zeilenansicht = inflater.inflate(R.layout.zeilenansicht, parent, false);  
 	    this.zeilenansicht = zeilenansicht;
     	this.position = position;
 	    return fuelleListViewItem();
     }
 
 	private View fuelleListViewItem() {
 		setFigurIcon();
 	    setSpielerName();  
 	    initBearbeitenButton();
 	    initLoeschenButton();
 	    return zeilenansicht;
 	}
 
 	private void setFigurIcon() {
 		ImageView imageView = (ImageView) this.zeilenansicht.findViewById(R.id.img_gewaehlte_figur);
 	    int resID = activity.getResources().getIdentifier(baueBildNamen(), "drawable", "com.dhbw.dvst");	    
 	    imageView.setImageResource(resID);
 	}
 
 	private String baueBildNamen() {
 		String form = this.getItem(this.position).getSpielfigur().getForm().getText_en();
 	    String farbe = this.getItem(this.position).getSpielfigur().getFarbe().getText_en();
 	    String bildName = form +"_"+ farbe;
 		return bildName;
 	}
 	
 	private void setSpielerName() {
 		TextView textView = (TextView) this.zeilenansicht.findViewById(R.id.tv_gewaehlter_name);
 	    textView.setText(getItem(this.position).toString());
 	}
 
 	protected void initBearbeitenButton() {
 		Button btn_bearbeiten = (Button) this.zeilenansicht.findViewById(R.id.btn_spieler_bearbeiten);
 	    btn_bearbeiten.setTag(this.position);
 	    //TODO: bearbeitungsicon
 	    btn_bearbeiten.setBackgroundResource(R.drawable.ic_launcher);
 	    setBearbeitenListener(btn_bearbeiten);
 	}
     
 	protected void setBearbeitenListener(Button btn_bearbeiten) {
 		btn_bearbeiten.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {     
             	Intent intent_edit_spieler = new Intent(activity,SpielerBearbeitenActivity.class)
 					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             	intent_edit_spieler.putExtra("spieler_index", (Integer)v.getTag());
             	activity.startActivity(intent_edit_spieler);
             }
 	    });
 	}
 
 	protected void initLoeschenButton() {
 		Button btn_loeschen = (Button) this.zeilenansicht.findViewById(R.id.btn_spieler_loeschen);
 	    btn_loeschen.setTag(getItem(this.position));
 	    //TODO: löschicon
 	    btn_loeschen.setBackgroundResource(R.drawable.ic_launcher);
 	    setLoeschenListener(btn_loeschen);
 	}
 
 	protected void setLoeschenListener(Button btn_loeschen) {
 		btn_loeschen.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(final View v) {
 				OnClickListener spielerLoeschen = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
						SpielerListeArrayAdapter.this.remove(SpielerListeArrayAdapter.this.mIdMap.get(position));
 						Spiel spiel = Control.getInstance();
 						spiel.spielerLoeschen((Spieler)v.getTag());
 					}
 				};
 				new LoeschDialog(activity, activity.getString(R.string.wirklich_loeschen), spielerLoeschen);
 			}
 	    });
 	}
 
 }
