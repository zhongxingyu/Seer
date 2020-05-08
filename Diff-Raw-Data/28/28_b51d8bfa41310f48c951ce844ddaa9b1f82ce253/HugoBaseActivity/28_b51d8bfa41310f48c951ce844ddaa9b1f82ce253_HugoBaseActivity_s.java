 package de.hugo;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 /**
  * 
  * @author Jonas Stadler
  * 
  */
 
 public class HugoBaseActivity extends Activity {
 
 	// -------------------------------------------
 	// DEBUGEINSTELLUNGEN
 	// -------------------------------------------
 
 	protected String DEBUG_TAG = "HUGO";
 
 	// protected boolean DEBUG_GENERAL = false;
 	protected boolean DEBUG_LOGIN = true;
 
 	// -------------------------------------------
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		// DEBUG-Part
		// this.checkGeneralDebug();
 	}
 
 	/**
 	 * Checkt ob ein debugwert true ist.
 	 * @return false, wenn kein debugwert true gesetzt ist.
 	 */
 	protected boolean checkGeneralDebug() {
		if (this.DEBUG_LOGIN == true) return true;
 		// falls kein wert true ist.
 		return false;
 	}
 }
