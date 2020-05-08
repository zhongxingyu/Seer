 /**
  * 
  */
 package edu.cmu.hcii.peer.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.MediaPlayer.OnVideoSizeChangedListener;
 import android.net.Uri;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.MediaController;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.VideoView;
 import android.widget.LinearLayout.LayoutParams;
 import edu.cmu.hcii.novo.kadarbra.R;
 import edu.cmu.hcii.peer.MessageHandler;
 import edu.cmu.hcii.peer.structure.Callout;
 import edu.cmu.hcii.peer.structure.Cycle;
 import edu.cmu.hcii.peer.structure.CycleNote;
 import edu.cmu.hcii.peer.structure.ExecNote;
 import edu.cmu.hcii.peer.structure.Reference;
 import edu.cmu.hcii.peer.structure.Step;
 import edu.cmu.hcii.peer.structure.StowageItem;
 import edu.cmu.hcii.peer.util.FontManager.FontStyle;
 
 /**
  * @author Chris
  *
  */
 public class ViewFactory {
 	
 	private static final String TAG = "ViewFactory";
 	
 	
 	
 	/**
 	 * Add a basic step
 	 * 
 	 * @param context
 	 * @param index
 	 * @param s
 	 */
 	public static ViewGroup getNavigationStep(Context context, int curStepIndex, int index, Step s, int reps) {
 		LayoutInflater inflater = LayoutInflater.from(context);
 		ViewGroup newStep = (ViewGroup) inflater.inflate(R.layout.nav_item, null);
 		
 		((TextView)newStep.findViewById(R.id.navItemNumber)).setText("STEP " + s.getNumber());
 		((TextView)newStep.findViewById(R.id.navItemText)).setText(s.getText());
 		
 		//If a lone step, set a margin so that it matches those which are a the cycle
 		if (reps < 2) {
 			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 			params.setMargins(34, 0, 0, 0);
 			newStep.setLayoutParams(params);
 		}
 		
 		final String step = String.valueOf(index);
 		
 		newStep.setOnClickListener(new OnClickListener(){
 
 			//The step number sent will be 0 indexed.
 			//So step 1 will send over the index of 0.
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent("command");
 				intent.putExtra("msg", MessageHandler.COMMAND_GO_TO_STEP);
 				intent.putExtra("str", step);
 				arg0.getContext().sendBroadcast(intent);
 			}
 		});
 		
 		//Color it if it is the current step
 		if (index == curStepIndex) ((TextView)newStep.findViewById(R.id.navItemNumber)).setTextColor(context.getResources().getColor(R.color.main));
 		
 		//Set up the custom fonts
 		FontManager fm = FontManager.getInstance(context.getAssets());
 		((TextView)newStep.findViewById(R.id.navItemNumber)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
 		((TextView)newStep.findViewById(R.id.navItemText)).setTypeface(fm.getFont(FontStyle.BODY));
 		
 		return newStep;
 	}
 	
 	
 	public static ViewGroup getNavigationCycle(Context context, int curStepIndex, int index, Cycle c) {
 		LayoutInflater inflater = LayoutInflater.from(context);
 		ViewGroup newCycle = (ViewGroup) inflater.inflate(R.layout.nav_item_cycle, null);
 		
 		((TextView)newCycle.findViewById(R.id.navCycleCount)).setText(c.getReps() + "x");		
 		ViewGroup steps = (ViewGroup) newCycle.findViewById(R.id.navCycleSteps);
 		
 		for (int j = 0; j < c.getNumChildren(); j++) {
 			//TODO this will break on cycles within cycles
 			steps.addView(getNavigationStep(context, curStepIndex, index, (Step)c.getChild(j), c.getReps()));
 			index++;
 		}
 		
 		//Set up the custom fonts
 		FontManager fm = FontManager.getInstance(context.getAssets());
 		((TextView)newCycle.findViewById(R.id.navCycleCount)).setTypeface(fm.getFont(FontStyle.BODY));
 		
 		return newCycle;
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param context
 	 * @param index
 	 * @return
 	 */
 	public static View getCycleSelect(Context context, int index) {
 		final String rep = String.valueOf(index);
     	
 		LayoutInflater inflater = LayoutInflater.from(context);
     	TextView newItem = (TextView)inflater.inflate(R.layout.cycle_select_item, null);       	
     	newItem.setText(context.getResources().getString(R.string.cycle_display_name).toUpperCase() + " " + rep);	
     	
     	newItem.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent("command");
 				intent.putExtra("msg", MessageHandler.COMMAND_CYCLE_NUMBER);
 				intent.putExtra("str", rep);
 				v.getContext().sendBroadcast(intent);
 			}
     	});
     	
     	//Set up the custom fonts
     	FontManager fm = FontManager.getInstance(context.getAssets());
     	newItem.setTypeface(fm.getFont(FontStyle.SELECTABLE));
     	
     	return newItem;
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param module
 	 * @param items
 	 * @return
 	 */
 	public static ViewGroup getStowageTable(Context context, String module, List<StowageItem> items) {
 		LayoutInflater inflater = LayoutInflater.from(context);
 		ViewGroup container = (ViewGroup)inflater.inflate(R.layout.stowage_table, null);
 		TableLayout table = (TableLayout)container.findViewById(R.id.stow_table);
 		
 		//set table title
 		((TextView)container.findViewById(R.id.stow_table_title)).setText(module);
 		
 		for (int i = 0; i < items.size(); i++) {
 			table.addView(getStowageRow(context, items.get(i)));
 		}
 		
 		//Set up the custom fonts
     	FontManager fm = FontManager.getInstance(context.getAssets());
     	((TextView)container.findViewById(R.id.stow_table_title)).setTypeface(fm.getFont(FontStyle.HEADER));
     	
     	((TextView)table.findViewById(R.id.binCodeHeader)).setTypeface(fm.getFont(FontStyle.HEADER));
     	((TextView)table.findViewById(R.id.itemHeader)).setTypeface(fm.getFont(FontStyle.HEADER));
     	((TextView)table.findViewById(R.id.quantityHeader)).setTypeface(fm.getFont(FontStyle.HEADER));
     	((TextView)table.findViewById(R.id.itemCodeHeader)).setTypeface(fm.getFont(FontStyle.HEADER));
     	((TextView)table.findViewById(R.id.notesHeader)).setTypeface(fm.getFont(FontStyle.HEADER));
 		
 		return container;
 	}
 	
 	
 	public static ViewGroup getStowageRow(Context context, StowageItem item) {
 		LayoutInflater inflater = LayoutInflater.from(context);
 		TableRow row = (TableRow) inflater.inflate(R.layout.stowage_row, null);
 		
 		((TextView)row.findViewById(R.id.stowNoteBinCode)).setText(item.getBinCode());
 		((TextView)row.findViewById(R.id.stowNoteItem)).setText(item.getName());
 		((TextView)row.findViewById(R.id.stowNoteQuantity)).setText(String.valueOf(item.getQuantity()));
 		((TextView)row.findViewById(R.id.stowNoteItemCode)).setText(item.getItemCode());
 		((TextView)row.findViewById(R.id.stowNoteNotes)).setText(item.getText());
 		
 		try {
 			InputStream is = context.getAssets().open("procedures/references/" + item.getUrl());
 			Drawable d = Drawable.createFromStream(is, null);
 			((ImageView)row.findViewById(R.id.stowNoteImage)).setImageDrawable(d);
 			
 		} catch(Exception e) {
 			Log.e(TAG, "Error adding reference image to stowage note", e);
 		}
 		
 		//Set up the custom fonts
     	FontManager fm = FontManager.getInstance(context.getAssets());    	
     	((TextView)row.findViewById(R.id.stowNoteBinCode)).setTypeface(fm.getFont(FontStyle.BODY));
     	((TextView)row.findViewById(R.id.stowNoteItem)).setTypeface(fm.getFont(FontStyle.BODY));
     	((TextView)row.findViewById(R.id.stowNoteQuantity)).setTypeface(fm.getFont(FontStyle.BODY));
     	((TextView)row.findViewById(R.id.stowNoteItemCode)).setTypeface(fm.getFont(FontStyle.BODY));
     	((TextView)row.findViewById(R.id.stowNoteNotes)).setTypeface(fm.getFont(FontStyle.BODY));
 		
 		return row;
 	}
 	
 	/**
 	 * 
 	 * @param context
 	 * @param note
 	 * @return
 	 */
 	public static ViewGroup getExecutionNoteOverview(Context context, ExecNote note) {
 		if (note != null) {
 			Log.v(TAG, "Setting up overview execution note");
 			LayoutInflater inflater = LayoutInflater.from(context);
 			ViewGroup noteView = (ViewGroup)inflater.inflate(R.layout.ex_note_overall, null);
 			
 			((TextView)noteView.findViewById(R.id.exNoteNumber)).setText("Step " + note.getNumber());
 			((TextView)noteView.findViewById(R.id.exNoteText)).setText(note.getText());
 
 			//Set up the custom fonts
 	    	FontManager fm = FontManager.getInstance(context.getAssets());
 	    	((TextView)noteView.findViewById(R.id.exNoteNumber)).setTypeface(fm.getFont(FontStyle.BODY));
 	    	((TextView)noteView.findViewById(R.id.exNoteText)).setTypeface(fm.getFont(FontStyle.BODY));
 			
 			return noteView;
 		}
 		
 		return null;
 	}
 	
 	
 	
 	/**
 	 * Add the given execution note to the step page.
 	 * 
 	 * @param note the note to display
 	 */
 	public static ViewGroup getExecutionNote(Context context, ExecNote note) {
 		if (note != null) {
 			Log.v(TAG, "Setting up execution note");
 			LayoutInflater inflater = LayoutInflater.from(context);
 			ViewGroup noteView = (ViewGroup)inflater.inflate(R.layout.callout, null);
 			
 	        ((TextView)noteView.findViewById(R.id.calloutTitle)).setText(R.string.ex_note_title);
 	        ((TextView)noteView.findViewById(R.id.calloutText)).setText(note.getText());
 	        
 	        //Set up the custom fonts
 	    	FontManager fm = FontManager.getInstance(context.getAssets());
 	    	((TextView)noteView.findViewById(R.id.calloutTitle)).setTypeface(fm.getFont(FontStyle.HEADER));
 	    	((TextView)noteView.findViewById(R.id.calloutText)).setTypeface(fm.getFont(FontStyle.BODY));
 
 			return noteView;
 		}
 		
 		return null;
 	}	
 	
 	
 	
 	/**
 	 * Add the given callout object to the step
 	 * @param call the callout to render
 	 */
 	public static ViewGroup getCallout(Context context, Callout call) {
 		if (call != null) {
 			Log.v(TAG, "Setting up callout");
 			LayoutInflater inflater = LayoutInflater.from(context);
 			ViewGroup callView = (ViewGroup)inflater.inflate(R.layout.callout, null);
 	        
 	        String typeName = "";
 	        int bg = 0;
 	        int border = 0;
 	        
 	        switch(call.getType()) {
 	        	case NOTE:
 	        		typeName = "NOTE";
 	        		bg = R.drawable.dot_bg_white;
 	        		border = R.drawable.border_white;
 	        		break;
 	        	
 	        	case CAUTION:
 	        		typeName = "CAUTION";
 	        		bg = R.drawable.dot_bg_yellow;
 	        		border = R.drawable.border_yellow;
 	        		break;
 	        		
 	        	case WARNING:
 	        		typeName = "WARNING";
 	        		bg = R.drawable.dot_bg_red;
 	        		border = R.drawable.border_red;
 	        		break;	        	
 	        		
 	        	default:
 	        		break;
 	        }
 	        
 	        ((ViewGroup)callView.findViewById(R.id.calloutTable)).setBackgroundResource(border);
 	        ((ViewGroup)callView.findViewById(R.id.calloutHeader)).setBackgroundResource(bg);
 	        ((TextView)callView.findViewById(R.id.calloutTitle)).setText(typeName);
 	        ((TextView)callView.findViewById(R.id.calloutText)).setText(call.getText());
 	        
 	        //Set up the custom fonts
 	    	FontManager fm = FontManager.getInstance(context.getAssets());
 	    	((TextView)callView.findViewById(R.id.calloutTitle)).setTypeface(fm.getFont(FontStyle.HEADER));
 	    	((TextView)callView.findViewById(R.id.calloutText)).setTypeface(fm.getFont(FontStyle.BODY));
 
 			return callView;
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Setup the give cycle note
 	 * 
 	 * @param context
 	 * @param note
 	 * @return
 	 */
 	public static ViewGroup getCycleNote(Context context, CycleNote note) {
 		LayoutInflater inflater = LayoutInflater.from(context);
 		ViewGroup newNote = (ViewGroup)inflater.inflate(R.layout.cycle_note, null);
 
 		((TextView)newNote.findViewById(R.id.cycleNoteText)).setText(note.getText());
         
         if (note.getReference() != null) {
         	newNote.addView(getReference(context, note.getReference()));
         }
         
         //Set up the custom fonts
     	FontManager fm = FontManager.getInstance(context.getAssets());
     	((TextView)newNote.findViewById(R.id.cycleNoteText)).setTypeface(fm.getFont(FontStyle.HEADER));
         
         return newNote;
 	}
 	
 	
 	
 	/**
 	 * Setup the reference view group corresponding to the given
 	 * reference object.
 	 * 
 	 * @param ref
 	 * @return
 	 */
 	public static ViewGroup getReference(Context context, Reference ref) {		
 		switch(ref.getType()) {
 			case IMAGE:
 				return getImageReference(context, ref);
 				
 			case VIDEO:
 				return getVideoReference(context, ref);
 				
 			case AUDIO:
 				return getAudioReference(context, ref);
 				
 			case TABLE:
 				return getTableReference(context, ref);
 				
 			default:
 				break;
 		}
 		
 		return null;
 	}
 	
 	
 	
 	/**
 	 * Setup the given reference as an image reference
 	 * @param ref the reference to render
 	 */
 	public static ViewGroup getImageReference(Context context, Reference ref) {
 		Log.v(TAG, "Setting up image view: " + ref.getUrl());
 		
 		LayoutInflater inflater = LayoutInflater.from(context);
         ViewGroup reference = (ViewGroup)inflater.inflate(R.layout.reference, null);
         ImageView img = (ImageView)inflater.inflate(R.layout.image, null);
 		
 		try {			
 			InputStream is = context.getAssets().open("procedures/references/" + ref.getUrl());
 			Drawable d = Drawable.createFromStream(is, null);
 			
 			img.setImageDrawable(d);
 	        //img.setImageDrawable(Drawable.createFromPath(ref.getUrl()));
 			
 			reference.addView(img, 0);
 		
 		} catch (IOException e) {
 			Log.e(TAG, "Error loading image", e);
 		}		
 		//TODO this last code gets repeated for every reference type
 		((TextView)reference.findViewById(R.id.referenceCaption)).setText(ref.getName() + ": " + ref.getDescription());
 		
 		//Set up the custom fonts
     	FontManager fm = FontManager.getInstance(context.getAssets());
     	((TextView)reference.findViewById(R.id.referenceCaption)).setTypeface(fm.getFont(FontStyle.BODY));
     	
 		return reference;
 	}
 	
 	
 	
 	/**
 	 * Setup the given reference as a video reference
 	 * 
 	 * TODO: we should make our own media controller so it's stylized
 	 * 
 	 * @param ref the reference to render
 	 */
 	public static ViewGroup getVideoReference(final Context context, Reference ref) {
 		Log.v(TAG, "Setting up video view: " + ref.getUrl());
 		
         LayoutInflater inflater = LayoutInflater.from(context);
         ViewGroup reference = (ViewGroup)inflater.inflate(R.layout.reference_video, null);
         final VideoView vid = (VideoView)reference.findViewById(R.id.referenceVideo);
 		
 		//TODO for some reason this fucking thing doesn't work.
 		//vid.setVideoURI(Uri.parse("file:///android_asset/procedures/references/" + ref.getUrl()));
 		vid.setVideoURI(Uri.parse(Environment.getExternalStorageDirectory().toString() + "/" + ref.getUrl()));
 
 		vid.setOnPreparedListener(new OnPreparedListener() {
             public void onPrepared(MediaPlayer mp) {
             	mp.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() { 
                     @Override
                     public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                           /*
                            *  add media controller and set its position
                            *  TODO this still isn't laying where we want it
                            *  TODO probably should make this a custom videoview class
                            */
                           MediaController mc = new MediaController(context);
                           vid.setMediaController(mc);
                           mc.setAnchorView(vid);
                           
                           LayoutParams lp = new LinearLayout.LayoutParams(mp.getVideoWidth(), mp.getVideoHeight());
                           lp.gravity = Gravity.CENTER;
                           vid.setLayoutParams(lp);
                     }
                 });
             	
            	mp.start();
             }
         });			
 				
 		((TextView)reference.findViewById(R.id.referenceCaption)).setText(ref.getName() + ": " + ref.getDescription());
 			
 		//Set up the custom fonts
     	FontManager fm = FontManager.getInstance(context.getAssets());
     	((TextView)reference.findViewById(R.id.referenceCaption)).setTypeface(fm.getFont(FontStyle.BODY));
 
 		return reference;
 	}
 	
 	
 	
 	/**
 	 * Setup the given reference as an audio reference
 	 * @param ref the reference to render
 	 */
 	public static ViewGroup getAudioReference(Context context, Reference ref) {
 		//TODO
 		return null;
 	}
 	
 	
 	
 	/**
 	 * Setup the given reference as a table reference
 	 * @param ref the reference to render
 	 */
 	public static ViewGroup getTableReference(Context context, Reference ref) {
 		Log.v(TAG, "Setting up table view");
 		
 		LayoutInflater inflater = LayoutInflater.from(context);
         ViewGroup reference = (ViewGroup)inflater.inflate(R.layout.reference, null);
         TableLayout table = (TableLayout)inflater.inflate(R.layout.table, null);
         
         List<List<String>> cells = ref.getTable();
         
         for (int i = 0; i < cells.size(); i++) {
         	if (i==0) {
         		table.addView(getRow(context, cells.get(i), R.layout.table_header_row, R.layout.table_header_cell));
         	
         	} else {
         		table.addView(getRow(context, cells.get(i), R.layout.table_row, R.layout.table_cell));
         	}
         }
         
         reference.addView(table, 0);
         
         ((TextView)reference.findViewById(R.id.referenceCaption)).setText(ref.getName() + ": " + ref.getDescription());
         
         //Set up the custom fonts
     	FontManager fm = FontManager.getInstance(context.getAssets());
     	((TextView)reference.findViewById(R.id.referenceCaption)).setTypeface(fm.getFont(FontStyle.BODY));        
         
         return reference;
 	}
 	
 	
 	
 	/**
 	 * Set up a table row with the given values.  
 	 * 
 	 * @param cells
 	 * @param rowId
 	 * @param cellId
 	 * @return
 	 */
 	public static TableRow getRow(Context context, List<String> cells, int rowId, int cellId) {
 		FontManager fm = FontManager.getInstance(context.getAssets());
 		
 		LayoutInflater inflater = LayoutInflater.from(context);
 		TableRow row = (TableRow)inflater.inflate(rowId, null);
         
         for (int i = 0; i < cells.size(); i++) {
         	TextView t = (TextView)inflater.inflate(cellId, null);
         	t.setText(cells.get(i));
         	
         	//Set up the custom fonts
         	t.setTypeface(fm.getFont(cellId == R.layout.table_header_cell ? FontStyle.HEADER : FontStyle.BODY)); 
         	
         	row.addView(t);
         }
         
         return row;
 	}
 	
 	
 	
 	/**
 	 * 
 	 */
 	public static ViewGroup getInput(Context context) {
 		Log.v(TAG, "Setting up input");
 		LayoutInflater inflater = LayoutInflater.from(context);
         ViewGroup input = (ViewGroup)inflater.inflate(R.layout.input, null);
 
         //Set up the custom fonts
     	FontManager fm = FontManager.getInstance(context.getAssets());
     	((TextView)input.findViewById(R.id.inputCommand)).setTypeface(fm.getFont(FontStyle.SELECTABLE)); 
     	((TextView)input.findViewById(R.id.inputValue)).setTypeface(fm.getFont(FontStyle.BODY));
     	((TextView)input.findViewById(R.id.inputConfirm)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
     	((TextView)input.findViewById(R.id.inputRetry)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
     	((TextView)input.findViewById(R.id.inputInstruction)).setTypeface(fm.getFont(FontStyle.BODY));
     	
 		return input;
 	}
 	
 	
 	/**
 	 * 
 	 * @param container
 	 */
 	public static ViewGroup getTimer(Context context){
 		Log.v(TAG, "Setting up timer");
 		LayoutInflater inflater = LayoutInflater.from(context);
 	    ViewGroup timer = (ViewGroup)inflater.inflate(R.layout.timer, null);
 		
 	    //Set up the custom fonts
     	FontManager fm = FontManager.getInstance(context.getAssets());
     	((TextView)timer.findViewById(R.id.timerTimeText)).setTypeface(fm.getFont(FontStyle.TIMER));
     	((TextView)timer.findViewById(R.id.timerStartText)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
     	((TextView)timer.findViewById(R.id.timerStopText)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
     	((TextView)timer.findViewById(R.id.timerResetText)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
 	    
 	    return timer;
 	}
 
 
 
 	/**
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public static View getCallGround(Context context) {
 		TextView v = (TextView)LayoutInflater.from(context).inflate(R.layout.call_ground, null);
 		
 		//Set up the custom fonts
 		FontManager fm = FontManager.getInstance(context.getAssets());
 		v.setTypeface(fm.getFont(FontStyle.BODY));
 		
 		return v;
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public static ViewGroup getCompletionPage(Context context) {
 		ViewGroup v = (ViewGroup)LayoutInflater.from(context).inflate(R.layout.complete_page, null);
 		
 		//Set up the custom fonts
 		FontManager fm = FontManager.getInstance(context.getAssets());
 		((TextView)v.findViewById(R.id.completeTitle)).setTypeface(fm.getFont(FontStyle.BODY));
 		((TextView)v.findViewById(R.id.completeText)).setTypeface(fm.getFont(FontStyle.BODY));
 		
 		return v;
 	}
 }
