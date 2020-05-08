 package no.jts.android.simple.gameapi.example;
 
 import no.jts.android.simple.gameapi.AbstractActivity;
 import no.jts.android.simple.gameapi.Setup;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 
 public class GameActivity extends AbstractActivity {
 
 	private static final String TAG = "GameActivity";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i(TAG, "GameActivity created");
 		Bitmap originDesignBounds = getOriginDesignBounds();
 		Setup setup = new Setup(originDesignBounds.getWidth(), originDesignBounds.getHeight());
 		gameScreenManager = new GameScreenManager(this, setup);
 	}
 
 	private Bitmap getOriginDesignBounds() {
 		return BitmapFactory.decodeResource(getResources(), R.drawable.background);
 	}
 }
