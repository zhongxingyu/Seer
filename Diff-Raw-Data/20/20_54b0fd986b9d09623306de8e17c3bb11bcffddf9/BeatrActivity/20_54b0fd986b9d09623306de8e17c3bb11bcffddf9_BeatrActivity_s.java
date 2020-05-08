 package se.purestyle.beatr;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import se.purestyle.beatr.controller.InstrumentMixerController;
 import se.purestyle.beatr.controller.instrumentmixer.volumeobject.InstrumentController;
 import se.purestyle.beatr.editoractivities.BassEditorActivity;
 import se.purestyle.beatr.editoractivities.DrumEditorActivity;
 import se.purestyle.beatr.editoractivities.SynthEditorActivity;
 import se.purestyle.beatr.helpers.FileModifier;
 import se.purestyle.beatr.helpers.MetronomePlayer;
 import se.purestyle.beatr.helpers.PdConnector;
 import se.purestyle.beatr.helpers.beatplayer.BeatPlayer;
 import se.purestyle.beatr.model.FemaleNames;
 import se.purestyle.beatr.model.instrumentmixer.volumeobject.InstrumentModel;
 import se.purestyle.beatr.view.InstrumentMixerView;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Pair;
 import android.view.Window;
 
 public class BeatrActivity extends Activity implements PropertyChangeListener {
 	
 	//Main controller
 	private InstrumentMixerController mixerController;
 	
 	private static PdConnector conn;
 	
 	@Override
 	protected void onCreate( Bundle savedInstanceState ) {
 		
 		//Super to whatever the android framework does in activity
 		super.onCreate( savedInstanceState );
 		
 		//Remove the standard top bar stating the application name, I use a logo instead
 		requestWindowFeature( Window.FEATURE_NO_TITLE );
 		
 		conn = new PdConnector( getApplicationContext(), this, BeatrActivity.class );
 		
 		//Initialize the FileModifier, just need to give it an activity and context to work within
 		FileModifier.init( this, getApplicationContext() );
 		
 		//Initialize the MetronomePlayer and give it a default value for beats per minute
 		MetronomePlayer.init( getApplicationContext() );
 		MetronomePlayer.updateBpm( MetronomePlayer.DEFAULT_BPM );
 		
 		FemaleNames.populate();
 		
 		mixerController = new InstrumentMixerController( getApplicationContext() );
 		mixerController.setup();
 		mixerController.addListener( this );
 		
 		setContentView( mixerController.getViews().get( InstrumentMixerController.INSTRUMENT_MIXER_VIEW ).getViews().get( InstrumentMixerView.START_VIEW ) );
 	}
 	
 	@Override
 	protected void onResume() {
 		
 		super.onResume();
 		
 		BeatPlayer.getInstance().setMode( BeatPlayer.ALL );
 	}
 	
 	@Override
 	protected void onDestroy() {
 		
 		super.onDestroy();
 		
 		MetronomePlayer.destroy();
 		
 		conn.cleanup();
 	}
 
 	@Override
 	public void propertyChange( PropertyChangeEvent event ) {
 		
 		if( event.getPropertyName().equals( InstrumentController.EDIT_EVENT ) ) {
 			
 			Intent intent = null;
 			
 			@SuppressWarnings("unchecked")
 			Pair<String, String> pair = ( Pair<String, String> ) event.getNewValue();
 			
 			if( pair.first.equals( InstrumentModel.SYNTH ) ) {
 				
 				intent = new Intent( this, SynthEditorActivity.class );
 				
 			} else if( pair.first.equals( InstrumentModel.DRUM ) ) {
 				
 				intent = new Intent( this, DrumEditorActivity.class );
 				
 			} else if( pair.first.equals( InstrumentModel.BASS ) ) {
 				
 				intent = new Intent( this, BassEditorActivity.class );
 				
 			} else {
 				
				throw new RuntimeException( "" );
 			}
 			
 			intent.putExtra( "INSTRUMENT_NAME", pair.second );
 			
 			startActivity( intent );
 		
 		}
 	}
 }
