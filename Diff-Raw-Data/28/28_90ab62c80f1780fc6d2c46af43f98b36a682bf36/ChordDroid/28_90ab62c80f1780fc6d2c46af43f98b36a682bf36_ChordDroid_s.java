 package de.wertarbyte.chorddroid;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.AdapterView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class ChordDroid extends Activity implements OnItemSelectedListener, OnClickListener, OnLongClickListener {	
 	private List<Instrument> instruments;
 	
 	private Spinner s_instrument;
 	private Spinner s_root;
	private Spinner s_scale;
 	private Spinner s_extra;
 	
 	private ChordView chordView;
 	private TextView t_variant;
 	
 	private int chord_variant;
 	
 	private List<Shape> currentShapes;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         chord_variant = 0;
         currentShapes = new ArrayList<Shape>();
         
 		// load known instruments
         String[] instr = getResources().getStringArray(R.array.instrument_dirs);
         
         instruments = new ArrayList<Instrument>(instr.length);
         
         for (int i = 0; i < instr.length; i++) {
 			try {
 				instruments.add(new Instrument(getAssets(), instr[i]));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
        setContentView(R.layout.main);
        
        chordView = (ChordView) findViewById(R.id.chordview);
        
         s_instrument = (Spinner) findViewById(R.id.instrument);
         s_root = (Spinner) findViewById(R.id.root);
        s_scale = (Spinner) findViewById(R.id.triad);
         s_extra = (Spinner) findViewById(R.id.extra);
         
         t_variant = (TextView) findViewById(R.id.variant);
         
         s_instrument.setOnItemSelectedListener(this);
         s_root.setOnItemSelectedListener(this);
        s_scale.setOnItemSelectedListener(this);
         s_extra.setOnItemSelectedListener(this);
         
         // restore old bundle data
         setSpinner(s_instrument, savedInstanceState, "instrument");
         setSpinner(s_root, savedInstanceState, "root");
        setSpinner(s_scale, savedInstanceState, "scale");
         setSpinner(s_extra, savedInstanceState, "extra");
         
         chordView.setClickable(true);
         chordView.setOnClickListener(this);
         chordView.setOnLongClickListener(this);
     }
     
     private void setSpinner(Spinner s, Bundle b, String key) {
     	if (b != null && b.containsKey(key)) {
     		int pos = b.getInt(key);
     		s.setSelection(pos);
     	}
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
     	outState.putInt("instrument", s_instrument.getSelectedItemPosition());
     	outState.putInt("root", s_root.getSelectedItemPosition());
    	outState.putInt("scale", s_scale.getSelectedItemPosition());
     	outState.putInt("extra", s_extra.getSelectedItemPosition());
     }
     
     public String getSelectedChord() {
     	StringBuilder sb = new StringBuilder();
     	sb.append( s_root.getSelectedItem() );
    	sb.append( s_scale.getSelectedItem() );
    	sb.append( s_extra.getSelectedItem() );
     	return sb.toString();
     }
     
     public Instrument getSelectedInstrument() {
     	int pos = s_instrument.getSelectedItemPosition();
     	return instruments.get(pos);
     }
 
 	public void onItemSelected(AdapterView<?> sender, View v, int pos, long id) {
 		chord_variant = 0;
 		currentShapes = getSelectedInstrument().lookup(getSelectedChord());
 		refresh_shape();
 	}
 	
 	public List<Shape> getShapes() {
 		return currentShapes;
 	}
 	
 	public void refresh_shape() {
 		chordView.setShape(null);
 		t_variant.setText(getSelectedChord()+": -");
 		List<Shape> shapes = getShapes();
 		if (shapes != null && shapes.size() > 0) {
 			Shape s = shapes.get(chord_variant);
 			t_variant.setText(s.getLabel()+": "+(chord_variant+1)+"/"+shapes.size());
 			chordView.setShape( shapes.get(chord_variant) );
 		}
 	}
 
 	public void onNothingSelected(AdapterView<?> v) {}
 
 	public void onClick(View src) {
 		if (src == chordView) {
 			// advance to next chord variant
 			if (getShapes() != null) {
 				chord_variant++;
 				chord_variant %= getShapes().size();
 				refresh_shape();
 			}
 		}
 	}
 
 	public boolean onLongClick(View src) {
 		if (src == chordView) {
 			chord_variant = 0;
 			refresh_shape();
 			return true;
 		}
 		return false;
 	}
     
 }
