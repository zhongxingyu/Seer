 package se.purestyle.beatr.editoractivities;
 
 import org.puredata.android.service.R;
 
 import se.purestyle.beatr.controller.editors.BassEditorController;
 import se.purestyle.beatr.helpers.beatplayer.BeatPlayer;
 import se.purestyle.beatr.view.instrumentmixer.LogoView;
 
 import android.app.Activity;
 import android.graphics.BitmapFactory;
 import android.graphics.Shader;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.view.Window;
 import android.widget.LinearLayout;
 
 public class BassEditorActivity extends Activity {
 	
 	@Override
 	protected void onCreate( Bundle savedInstanceState ) {
 		
 		//Super to whatever the android framework does in activity
 		super.onCreate( savedInstanceState );
 		
 		//Remove the standard top bar stating the application name, I use a logo instead
 		requestWindowFeature( Window.FEATURE_NO_TITLE );
 		
 		LinearLayout holder = new LinearLayout( getApplicationContext() );	
 		holder.setOrientation( LinearLayout.VERTICAL );
 		setContentView( holder );
 		
 		//Create and add the logo to this activity
 		LogoView logoView = new LogoView( getApplicationContext() );
 		holder.addView( logoView );
 		
 		BitmapDrawable tiles = new BitmapDrawable( BitmapFactory.decodeResource( getResources(), R.drawable.illusion ) );
 		tiles.setTileModeX( Shader.TileMode.REPEAT );
 		tiles.setTileModeY( Shader.TileMode.REPEAT );
		
 		holder.setBackgroundDrawable( tiles );
 		
 		//Get the name of this instrument
 		Bundle extras = getIntent().getExtras();
 		
 		BassEditorController instrumentEditorController = new BassEditorController( getApplicationContext(), extras.getString( "INSTRUMENT_NAME" ), this );
 		instrumentEditorController.setup();
 		
 		holder.addView( instrumentEditorController.getView() );
 		
 		//Stop all players
 		BeatPlayer.getInstance().setMode( BeatPlayer.NONE );
 	}
 }
