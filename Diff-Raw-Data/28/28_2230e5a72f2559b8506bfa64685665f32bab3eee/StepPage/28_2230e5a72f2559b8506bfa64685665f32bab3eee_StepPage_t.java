 package edu.cmu.hcii.novo.kadarbra.page;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Environment;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewManager;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.MediaController;
 import android.widget.TextView;
 import android.widget.VideoView;
 import edu.cmu.hcii.novo.kadarbra.R;
 import edu.cmu.hcii.novo.kadarbra.structure.Callout;
 import edu.cmu.hcii.novo.kadarbra.structure.ExecNote;
 import edu.cmu.hcii.novo.kadarbra.structure.Reference;
 import edu.cmu.hcii.novo.kadarbra.structure.Reference.RType;
 import edu.cmu.hcii.novo.kadarbra.structure.Step;
 
 public class StepPage extends LinearLayout {
 	private static final String TAG = "StepPage";
 	
 	private Step parent;
 	private Step step;
 	
 	/**
 	 * constructs a step page, which is a layout object that can have children objects - 
 	 * the type of layout object used will change in the future (probably a vertical ViewPager)
 	 * 
 	 * If there is no parent, it needs to be passed a null
 	 * 
 	 * @param context
 	 * @param step
 	 * @param parent
 	 */
 	public StepPage(Context context, Step step, Step parent) {
 		super(context);
 		this.setOrientation(VERTICAL);
 		
 		String fullNumber = (parent != null ? parent.getNumber() + "." : "")  + 
 				step.getNumber();
 		String cycleLabel = (step.getCycle() > 0 ? "Cycle " + step.getCycle() : "");
 		
 		Log.d(TAG, "Setting up step page " + fullNumber + " " + cycleLabel);
 		
 		this.step = step;
 		this.parent = parent;
 		
 		LayoutInflater inflater = LayoutInflater.from(context);
         View page = (View)inflater.inflate(R.layout.step_page, null);
         
		
        //Add in an indicator if in a cycle
 		if (step.getCycle() > 0) {
 			((TextView)page.findViewById(R.id.stepCycleNumber)).setText(cycleLabel);
 			
 		} else {
 			((ViewManager)page).removeView(page.findViewById(R.id.stepCycleNumber));
 		}
 		
		
 		//setup the parent
 		if (parent != null) {
 			((TextView)page.findViewById(R.id.stepParentStep)).setText(parent.getNumber() + ": " + parent.getText());
 			
 		} else {
 			((ViewManager)page).removeView(page.findViewById(R.id.stepParentStep));
 		}
 		
		
		setupExecutionNotes();
		setupCallouts();
		
		
 		//Add the normal text
 		final TextView stepView = ((TextView)page.findViewById(R.id.stepStepText));
 		stepView.setText(fullNumber + ": " + step.getText());
 
 		
 		//if it is a conditional
 		if (step.isConditional()) {
 			Log.v(TAG, "Step has conditional content");
 			
 			final TextView conseqView = ((TextView)page.findViewById(R.id.stepConsequent));
 			conseqView.setText(step.getConsequent());
 			conseqView.setVisibility(INVISIBLE);
 			
 			stepView.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					//There's got to be a toggle somewhere...
 					if (conseqView.getVisibility() == VISIBLE){
 						conseqView.setVisibility(INVISIBLE);
 					} else {
 						conseqView.setVisibility(VISIBLE);
 					}
 				}
 			});
 			
 		} else {
 			((ViewManager)page).removeView(page.findViewById(R.id.stepConsequent));
 		}
 		
 		this.addView(page);
 		
 		setupReferences();
 	}
 	
 	
 	
 	/**
 	 * @param context
 	 * @param attrs
 	 */
 	public StepPage(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 	}
 	
 
 	
 	@Override
 	protected void onLayout(boolean changed, int l, int t, int r, int b) {
 		super.onLayout(changed,l,t,r,b);
 		// TODO Auto-generated method stub	
 	}
 	
 	
 
 	/**
 	 * Sets up the execution notes to be displayed for this step.
 	 * If there is a parent step, show that one too.
 	 */
 	private void setupExecutionNotes() {
 		if (parent != null) setupExecutionNote(parent.getExecNote());
 		setupExecutionNote(step.getExecNote());
 	}
 	
 	
 	
 	/**
 	 * Add the given execution note to the step page.
 	 * 
 	 * @param note the note to display
 	 */
 	private void setupExecutionNote(ExecNote note) {
 		if (note != null) {
			Log.i(TAG, "Setting up execution note");
 			LayoutInflater inflater = LayoutInflater.from(getContext());
 	        View noteView = (View)inflater.inflate(R.layout.ex_note, null);
 	        
 	        ((TextView)noteView.findViewById(R.id.exNoteText)).setText(note.getText());
 
 			this.addView(noteView);
 		}
 	}
 	
 	
 	
 	/**
 	 * Setup the step's callouts
 	 */
 	private void setupCallouts() {
 		List<Callout> calls = step.getCallouts();
 		for (int i = 0; i < calls.size(); i++) {
 			setupCallout(calls.get(i));
 		}
 	}
 	
 	
 	
 	/**
 	 * Add the given callout object to the step
 	 * @param call the callout to render
 	 */
 	private void setupCallout(Callout call) {
 		if (call != null) {
 			//TODO
 			//setup conditional header
 			//setup text
 		}
 	}
 	
 	
 	
 	/**
 	 * Setup the references for the step
 	 */
 	private void setupReferences() {
 		List<Reference> refs = step.getReferences();
 		for (int i = 0; i < refs.size(); i++) {
 			RType type = refs.get(i).getType();
 			
 			switch(type) {
 				case IMAGE:
 					setupImageReference(refs.get(i));
 					break;
 					
 				case VIDEO:
 					setupVideoReference(refs.get(i));
 					break;
 					
 				case AUDIO:
 					setupAudioReference(refs.get(i));
 					break;
 					
 				case TABLE:
 					setupTableReference(refs.get(i));
 					break;
 					
 				default:
 					break;
 			}
 		}
 	}
 	
 	
 	
 	/**
 	 * Setup the given reference as an image reference
 	 * @param ref the reference to render
 	 */
 	private void setupImageReference(Reference ref) {
 		Log.v(TAG, "Setting up image view: " + ref.getUrl());
 		
 		try {
 			LayoutInflater inflater = LayoutInflater.from(getContext());
 	        View reference = (View)inflater.inflate(R.layout.reference_image, null);
 			
 			InputStream is = getContext().getAssets().open("procedures/references/" + ref.getUrl());
 			Drawable d = Drawable.createFromStream(is, null);
 			((ImageView)reference.findViewById(R.id.referenceImage)).setImageDrawable(d);
 	        //img.setImageDrawable(Drawable.createFromPath(ref.getUrl()));
 	
 			((TextView)reference.findViewById(R.id.referenceCaption)).setText(ref.getName() + ": " + ref.getDescription());
 			
 	        this.addView(reference);
 			
 		} catch (IOException e) {
 			Log.e(TAG, "Error loading image", e);
 		}
         
 	}
 	
 	
 	
 	/**
 	 * Setup the given reference as a video reference
 	 * 
 	 * TODO: we should make our own media controller so it's stylized
 	 * 
 	 * @param ref the reference to render
 	 */
 	private void setupVideoReference(Reference ref) {
 		Log.v(TAG, "Setting up video view: " + ref.getUrl());
 		
 		LayoutInflater inflater = LayoutInflater.from(getContext());
         View reference = (View)inflater.inflate(R.layout.reference_video, null);
 		
         VideoView vid = ((VideoView)reference.findViewById(R.id.referenceVideo));
 			
 		//TODO for some reason this fucking thing doesn't work.
 		//vid.setVideoURI(Uri.parse("file:///android_asset/procedures/references/" + ref.getUrl()));
 		vid.setVideoURI(Uri.parse(Environment.getExternalStorageDirectory().toString() + "/" + ref.getUrl()));
 			
 		vid.setMediaController(new MediaController(getContext()));
 		vid.setOnPreparedListener(new OnPreparedListener() {
             public void onPrepared(MediaPlayer mp) {
                 mp.start();
             }
         });
 		
 		((TextView)reference.findViewById(R.id.referenceCaption)).setText(ref.getName() + ": " + ref.getDescription());
 			
 		this.addView(reference);
 	}
 	
 	
 	
 	/**
 	 * Setup the given reference as an audio reference
 	 * @param ref the reference to render
 	 */
 	private void setupAudioReference(Reference ref) {
 		//TODO
 	}
 	
 	
 	
 	/**
 	 * Setup the given reference as a table reference
 	 * @param ref the reference to render
 	 */
 	private void setupTableReference(Reference ref) {
 		//TODO
 	}
 
 
 
 	/**
 	 * 
 	 * @return step
 	 */
 	public Step getStep(){
 		return step;
 	}
 
 
 
 	/**
 	 * 
 	 * @return parent
 	 */
 	public Step getStepParent(){
 		return parent;
 	}
 }
