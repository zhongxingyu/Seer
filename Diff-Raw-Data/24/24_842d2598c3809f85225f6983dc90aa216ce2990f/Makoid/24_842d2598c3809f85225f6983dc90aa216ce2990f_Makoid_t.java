 package com.rtg.makovm;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.widget.Toast;

 import com.rtg.makovm.MakoKeyboard.MakoKeyboardListener;
 import com.rtg.makovm.MakoView.MakoViewListener;
 
 
public class Makoid extends Activity implements MakoKeyboardListener, MakoViewListener {
 	public static final String EXTRA_ROM_FILE = "romfile";
 	
 	private MakoView mView = null;
 
 	private MakoKeyboard mKeyboard = null;
 	
 	private ProgressDialog mLoadingDlg = null;
 	
 	private String mRomFileName = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		mRomFileName = getIntent()!=null?getIntent().getStringExtra(EXTRA_ROM_FILE):null;
 		
 		if(savedInstanceState!=null)
 		{
 			mRomFileName = savedInstanceState.getString(EXTRA_ROM_FILE);
 		}
 		
 		if(mRomFileName == null)
 		{
 			startActivity(new Intent(this, RomChooserActivity.class));
 			finish();
 			return;
 		}
 
 		// Retrieve a reference to the view inflated in the layout
 		mView = (MakoView) findViewById(R.id.Makoid_MakoView);
 		mView.setListener(this);
 		
 		mView.setRom(mRomFileName);
 		
 		mKeyboard = (MakoKeyboard) findViewById(R.id.Makoid_Keyboard);
 		mKeyboard.setListener(this);
 	}
 
 	// Support HW keyboards
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 		mView.dispatchKeyEvent(event);
 		return super.dispatchKeyEvent(event);
 	}
 
 	@Override
 	public void makoKeyboardKeyTyped(String key) {
 		// Unused?
 	}
 
 	@Override
 	public void makoKeyboardKeyPressed(String key) {
 		if      ("Delete".equals(key)) { mView.keyPressed( 8); }
 		else if ( "Enter".equals(key)) { mView.keyPressed(10); }
 		else if (    "up".equals(key)) { mView.setKeys(MakoConstants.KEY_UP); }
 		else if (    "dn".equals(key)) { mView.setKeys(MakoConstants.KEY_DN); }
 		else if (    "lf".equals(key)) { mView.setKeys(MakoConstants.KEY_LF); }
 		else if (    "rt".equals(key)) { mView.setKeys(MakoConstants.KEY_RT); }
 		else {
 			if(key.equals("a"))
 			{
 				mView.setKeys(MakoConstants.KEY_A);
 			}
 			mView.keyPressed((int)key.charAt(0));
 		}
 		
 	}
 
 	@Override
 	public void makoKeyboardKeyReleased(String key) {
 		if      ("Delete".equals(key)) { mView.keyReleased( 8); }
 		else if ( "Enter".equals(key)) { mView.keyReleased(10); }
 		else if (    "up".equals(key)) { mView.unsetKeys(MakoConstants.KEY_UP); }
 		else if (    "dn".equals(key)) { mView.unsetKeys(MakoConstants.KEY_DN); }
 		else if (    "lf".equals(key)) { mView.unsetKeys(MakoConstants.KEY_LF); }
 		else if (    "rt".equals(key)) { mView.unsetKeys(MakoConstants.KEY_RT); }
 		else {
 			if(key.equals("a"))
 			{
 				mView.unsetKeys(MakoConstants.KEY_A);
 			}
 			mView.keyReleased((int)key.charAt(0));
 		}
 	}
 
 	@Override
 	public void makoViewStartLoading() {
 		mLoadingDlg = ProgressDialog.show(this, null, "Loading Rom...");
 		mLoadingDlg.setOwnerActivity(this);
 	}
 
 	@Override
 	public void makoViewFinishLoading() {
 		mLoadingDlg.dismiss();
 		mLoadingDlg = null;		
 	}
 
 	@Override
 	public void makoViewLoadError() {
 		Toast.makeText(this, "The ROM could not be loaded.", Toast.LENGTH_LONG).show();

 	}
}
