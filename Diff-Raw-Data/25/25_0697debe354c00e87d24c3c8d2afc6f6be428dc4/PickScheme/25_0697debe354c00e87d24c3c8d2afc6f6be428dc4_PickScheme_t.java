 package chvck.colourMate.activities;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import chvck.colourMate.R;
 
 public class PickScheme extends Activity {
 	public Button closeMono;
 	public Button wideMono;
 	public Button exactComp;
 	public Button closeComp;
 	public Button wideComp;
 	public Button exactAnalog;
 	public Button closeAnalog;
 	public Button wideAnalog;
 	public Button exactTriad;
 	public Button closeTriad;
 	public Button wideTriad;
 	public Button exactSplitComp;
 	public Button closeSplitComp;
 	public Button wideSplitComp;
 	public Button exactTetradic;
 	public Button closeTetradic;
 	public Button wideTetradic;
 	public Button exactSquare;
 	public Button closeSquare;
 	public Button wideSquare;
 	public Button blank;
 	int colour;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.schemes);
 	    
 	    //get the selected colour 
 	    Bundle extras = getIntent().getExtras();
 	    colour = extras.getInt("colour");
    	    
 	    //we don't have exact mono so just create a space in the top left of the grid
 	    blank = new Button(this);
 	    blank.setText("");
 	    blank.setVisibility(View.INVISIBLE);
 	    	    
 	    //make the buttons
	    closeMono = new GeneratorButton(this, "Close Mono", "close", "chrom");
	    wideMono = new GeneratorButton(this, "Wide Mono", "wide", "chrom");
	    exactComp = new GeneratorButton(this, "Exact Comp", "exact", "comp");
	    closeComp = new GeneratorButton(this, "Close Comp", "close", "comp");
	    wideComp = new GeneratorButton(this, "Wide Comp", "wide", "comp");
	    exactAnalog = new GeneratorButton(this, "Exact Analogous", "exact", "analog");
	    closeAnalog = new GeneratorButton(this, "Close Analogous", "close", "analog");
	    wideAnalog = new GeneratorButton(this, "Wide Analogous", "wide", "analog");
 	    exactTriad = new GeneratorButton(this, "Exact Triadic", "exact", "triad");
 	    closeTriad = new GeneratorButton(this, "Close Triadic", "close", "triad");
 	    wideTriad = new GeneratorButton(this, "Wide Triadic", "wide", "triad");
	    exactSplitComp = new GeneratorButton(this, "Exact Split Comp", "exact", "splitComp");
	    closeSplitComp = new GeneratorButton(this, "Close Split Comp", "close", "splitComp");
	    wideSplitComp = new GeneratorButton(this, "Wide Split Comp", "wide", "splitComp");
 	    exactTetradic = new GeneratorButton(this, "Exact Tetradic", "exact", "tetradic");
 	    closeTetradic = new GeneratorButton(this, "Close Tetradic", "close", "tetradic");
 	    wideTetradic = new GeneratorButton(this, "Wide Tetradic", "wide", "tetradic");
 	    exactSquare = new GeneratorButton(this, "Exact Square", "exact", "square");
 	    closeSquare = new GeneratorButton(this, "Close Square", "close", "square");
 	    wideSquare = new GeneratorButton(this, "Wide Square", "wide", "square");
 
 	    //setup the grid
 	    GridView gridview = (GridView) findViewById(R.id.schemesgridview);
         gridview.setAdapter(new ButtonAdapter(this));     
 	}
 	
 	protected void generatorPicked(String generator, String angle) {
 		Intent intent = new Intent();
 		intent.setClass(this, chvck.colourMate.activities.GenerateNewColours.class);
 		intent.putExtra("generator", generator);
 		intent.putExtra("angle", angle);
 		intent.putExtra("colour", colour);
 		startActivity(intent);
 	}
 	
 	private void generatorDetails(String generator) {
 		Intent intent = new Intent();
 		intent.setClass(this, chvck.colourMate.activities.GeneratorDetails.class);
 		intent.putExtra("generator", generator);
 		startActivity(intent);
 	}
 	
 	//make a simple custom button
 	public class GeneratorButton extends Button {
 		public GeneratorButton(Context context, String buttonText, final String angle, final String generator) {
 			super(context);
 			this.setText(buttonText);
			this.setHeight(50);
 			this.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 				    generatorPicked(generator, angle);
 				}
 	        });
 			
 			this.setOnLongClickListener(new OnLongClickListener() {
 				public boolean onLongClick(View v) {
 					generatorDetails(generator);
 					return true;
 				} 	
 			});
 		}
 	}
 	
 	public class ButtonAdapter extends BaseAdapter {
 		// Gets the context so it can be used later
 		public ButtonAdapter(Context c) {
 		}
 		
 		// Total number of things contained within the adapter
 		public int getCount() {
 			return buttons.length;
 		}
 		
 		// Require for structure, not really used in my code.
 		public Object getItem(int position) {
 			return null;
 		}
 		
 		// Require for structure, not really used in my code. Can
 		// be used to get the id of an item in the adapter for
 		// manual control.
 		public long getItemId(int position) {
 			return position;
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 			Button button;
 			//should really be checking if convertView already exists here
 			//not bothering because it causes odd glitchy behaviour on some phones
 			button = (Button) buttons[position];
 			//button.setLayoutParams(new GridView.LayoutParams(100, 100));
 			button.setPadding(8, 8, 8, 8);
 			button.setId(position);
 				 	
 			return button;
 		}
 				
 		private Button[] buttons = {
 			blank,
 			closeMono,
 			wideMono,
 			exactComp,
 			closeComp,
 			wideComp,
 			exactAnalog,
 			closeAnalog,
 			wideAnalog,
 			exactTriad,
 			closeTriad,
 			wideTriad,
 			exactSplitComp,
 			closeSplitComp,
 			wideSplitComp,
 			exactTetradic,
 			closeTetradic,
 			wideTetradic,
 			exactSquare,
 			closeSquare,
 			wideSquare
 		};
 	}
 }
