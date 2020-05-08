 /**
  * 
  */
 package edu.cmu.hcii.peer.page;
 
 import java.util.List;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import edu.cmu.hcii.novo.kadarbra.R;
 import edu.cmu.hcii.peer.structure.Cycle;
 import edu.cmu.hcii.peer.structure.CycleNote;
 import edu.cmu.hcii.peer.structure.Step;
 import edu.cmu.hcii.peer.util.FontManager;
 import edu.cmu.hcii.peer.util.ViewFactory;
 import edu.cmu.hcii.peer.util.FontManager.FontStyle;
 
 /**
  * @author Chris
  *
  */
 public class CycleMarkerPage extends LinearLayout {
 	
 	private int totalReps;
 	private int currentRep;
 	private List<CycleNote> notes;
 	
 	
 	
 	/**
 	 * 
 	 */
 	public CycleMarkerPage(Context context, Cycle c, int currentRep) {
 		super(context);
 		
 		this.totalReps = c.getReps();
 		this.currentRep = currentRep;
 		this.notes = c.getNotes();
 		
 		LayoutInflater inflater = LayoutInflater.from(context);
 		ViewGroup page = (ViewGroup)inflater.inflate(R.layout.cycle_marker_page, this);
 		
 		if (c.getNumChildren() > 0) {
 			String start = ((Step)c.getChild(0)).getNumber();
 			String end = ((Step)c.getChild(c.getNumChildren()-1)).getNumber();
 			
 			String tense = currentRep > 1 ? "are" : "will be";
						
			((TextView)page.findViewById(R.id.cycleMarkerText)).setText("You " + tense + " repeating steps " + 
 					start + "-" + end + " a total of " + totalReps + " times");
 			
 		} else {
			((TextView)page.findViewById(R.id.cycleMarkerText)).setText("Uhh... There is a " + 
 					context.getResources().getString(R.string.cycle_display_name) + 
 					" with no steps in it.  Someone screwed up.");
 		}
		/*
         ((TextView)page.findViewById(R.id.cycleMarkerText)).setText("Beginning " + 
         		context.getResources().getString(R.string.cycle_display_name) + " " + currentRep);
        */
		((TextView)page.findViewById(R.id.cycleMarkerTitle)).setText("Repetition " + currentRep + " information");

         ViewGroup container = (ViewGroup)page.findViewById(R.id.cycleMarkerTextContainer);
         
         for (int i = 0; i < notes.size(); i++) {
         	container.addView(ViewFactory.getCycleNote(context, notes.get(i)));
         }
         
         initFonts();
 	}
 	
 	
 	
 	/**
 	 * @param context
 	 * @param attrs
 	 */
 	public CycleMarkerPage(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 	}
 
 
 	
 	/**
 	 * Setup the custom fonts for this view.
 	 */
 	private void initFonts() {
 		FontManager fm = FontManager.getInstance(getContext().getAssets());
 		
 		((TextView)findViewById(R.id.cycleMarkerTitle)).setTypeface(fm.getFont(FontStyle.HEADER));
 		((TextView)findViewById(R.id.cycleMarkerText)).setTypeface(fm.getFont(FontStyle.BODY));
 	}
 	
 	
 
 	/**
 	 * @return the totalReps
 	 */
 	public int getTotalReps() {
 		return totalReps;
 	}
 
 
 
 	/**
 	 * @param totalReps the totalReps to set
 	 */
 	public void setTotalReps(int totalReps) {
 		this.totalReps = totalReps;
 	}
 
 
 
 	/**
 	 * @return the repetition
 	 */
 	public int getCurrentRep() {
 		return currentRep;
 	}
 
 
 
 	/**
 	 * @param repetition the repetition to set
 	 */
 	public void setCurrentRep(int currentRep) {
 		this.currentRep = currentRep;
 	}
 
 
 
 	/**
 	 * @return the notes
 	 */
 	public List<CycleNote> getNotes() {
 		return notes;
 	}
 
 
 
 	/**
 	 * @param notes the notes to set
 	 */
 	public void setNotes(List<CycleNote> notes) {
 		this.notes = notes;
 	}
 }
