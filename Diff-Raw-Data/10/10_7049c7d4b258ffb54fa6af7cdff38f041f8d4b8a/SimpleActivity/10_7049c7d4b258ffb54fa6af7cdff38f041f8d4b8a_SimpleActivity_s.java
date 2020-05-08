 package eu.nazgee.eccerobo.remote;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
 import org.andengine.engine.options.ConfigChooserOptions;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 import org.andengine.util.adt.color.Color;
 import org.andengine.util.call.Callable;
 import org.andengine.util.call.Callback;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences;
 import android.widget.EditText;
 import eu.nazgee.eccerobo.remote.AnalogOnScreenControlRect.IAnalogOnScreenControlListener;
 import eu.nazgee.eccerobo.remote.mjpeg.SimpleMjpegActivity;
 
 public class SimpleActivity extends SimpleMjpegActivity {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 720;
 	private static final int DIALOG_ENTER_ROBOT_IP_ID = 0;
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private BitmapTextureAtlas mBitmapTextureAtlas;
 	private ITextureRegion mFaceTextureRegion;
 
 	private ITexture mOnScreenControlBaseTexture;
 	private ITextureRegion mOnScreenControlBaseTextureRegion;
 	private ITexture mOnScreenControlKnobTexture;
 	private ITextureRegion mOnScreenControlKnobTextureRegion;
 	private Camera mCamera;
 	private Socket mSocket;
 	private BufferedReader mSSIn;
 	private PrintWriter mSSOut;
 	private String mServerIP;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		
 		mCamera = new Camera(0, 0, SimpleActivity.CAMERA_WIDTH, SimpleActivity.CAMERA_HEIGHT);
 //		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(SimpleActivity.CAMERA_WIDTH, SimpleActivity.CAMERA_HEIGHT), mCamera);
 		
 		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
 		final ConfigChooserOptions configChooserOptions = engineOptions.getRenderOptions().getConfigChooserOptions();
 		configChooserOptions.setRequestedRedSize(8);
 		configChooserOptions.setRequestedGreenSize(8);
 		configChooserOptions.setRequestedBlueSize(8);
 		configChooserOptions.setRequestedAlphaSize(8);
 		configChooserOptions.setRequestedDepthSize(16);
 		return engineOptions;
 	}
 
 	@Override
 	protected void onCreateResources() throws IOException {
 
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
 		this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(SimpleActivity.this.mBitmapTextureAtlas, SimpleActivity.this, "face_box.png", 0, 0);
 		this.mBitmapTextureAtlas.load();
 		
 		this.mOnScreenControlBaseTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/onscreen_control_base.png", TextureOptions.BILINEAR);
 		this.mOnScreenControlBaseTextureRegion = TextureRegionFactory.extractFromTexture(this.mOnScreenControlBaseTexture);
 		this.mOnScreenControlBaseTexture.load();
 
 		this.mOnScreenControlKnobTexture = new AssetBitmapTexture(this.getTextureManager(), this.getAssets(), "gfx/onscreen_control_knob.png", TextureOptions.BILINEAR);
 		this.mOnScreenControlKnobTextureRegion = TextureRegionFactory.extractFromTexture(this.mOnScreenControlKnobTexture);
 		this.mOnScreenControlKnobTexture.load();
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		final Scene pScene = new Scene();
 		pScene.getBackground().setColor(Color.TRANSPARENT);
 
 		final AnalogOnScreenControlRect analogOnScreenControl = new AnalogOnScreenControlRect(0, 0, this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.2f, 200, this.getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
 			@Override
 			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
 				if (mSSOut != null && mSocket != null) {
					mSSOut.write("set speed " + Math.round(pValueY * 4.5) + "\r\n");
					mSSOut.write("set turn " + Math.round(pValueX * 4.5) + "\r\n");
 					mSSOut.flush();
 				}
 			}
 
 			@Override
 			public void onControlClick(final AnalogOnScreenControlRect pAnalogOnScreenControl) {
 				//sprite.registerEntityModifier(new SequenceEntityModifier(new ScaleModifier(0.25f, 1, 1.5f), new ScaleModifier(0.25f, 1.5f, 1)));
 			}
 		});
 
 		final Sprite controlBase = analogOnScreenControl.getControlBase();
 		controlBase.setAlpha(0.5f);
 		controlBase.setOffsetCenter(0, 0);
 		controlBase.setScale(1);
 		/* Calculate the coordinates for the face, so its centered on the camera. */
 		final float centerX = (SimpleActivity.CAMERA_WIDTH - controlBase.getWidth()) / 2;
 //		final float centerY = (SimpleActivity.CAMERA_HEIGHT - controlBase.getHeight()) / 2; // CENTER
 //		final float centerY = (SimpleActivity.CAMERA_HEIGHT - controlBase.getHeight()) / 1; // TOP
 		final float centerY = 0; // BOTTOM
 		analogOnScreenControl.setPosition(centerX, centerY);
 
 		analogOnScreenControl.getControlKnob().setScale(1.25f);
 
 		pScene.setChildScene(analogOnScreenControl);
 	
 		return pScene;
 	}
 
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		this.showDialog(DIALOG_ENTER_ROBOT_IP_ID);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		try {
 			mSocket.close();
 			mSocket = null;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	@Override
 	protected Dialog onCreateDialog(final int pID) {
 		switch(pID) {
 			case DIALOG_ENTER_ROBOT_IP_ID:
 				SharedPreferences settings = getPreferences(0);
 				final EditText ipEditText = new EditText(this);
 				ipEditText.setText(settings.getString("defaultServer", "nazgee.dyndns.org"));
 				return new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_info)
 				.setTitle("Enter Oskar's IP address")
 				.setCancelable(false)
 				.setView(ipEditText)
 				.setPositiveButton("Connect", new OnClickListener() {
 
 					@Override
 					public void onClick(final DialogInterface pDialog, final int pWhich) {
 						mServerIP = ipEditText.getText().toString();
 						SharedPreferences settings = getPreferences(0);
 						SharedPreferences.Editor editor = settings.edit();
 						editor.putString("defaultServer", mServerIP);
 						// Commit the edits!
 						editor.commit();
 
 						SimpleActivity.this.doAsync(R.string.SOCKET_CONNECTING_DIALOG_TITLE, R.string.SOCKET_CONNECTING_DIALOG_TEXT, new Callable<Void>() {
 							@Override
 							public Void call() throws Exception {
 								try {
 									mSocket = new Socket(mServerIP, 7000);
 									mSSIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
 									mSSOut = new PrintWriter(mSocket.getOutputStream());
 
 								} catch (UnknownHostException e) {
 									e.printStackTrace();
 									SimpleActivity.this.finish();
 								} catch (IOException e) {
 									e.printStackTrace();
 									SimpleActivity.this.finish();
 								}
 								return null;
 							}
 						}, new Callback<Void>() {
 							@Override
 							public void onCallback(final Void pCallbackValue) {
 								new DoRead().execute("http://" + mServerIP + ":8080/?action=stream");
 							}
 						});
 					}
 				})
 				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
 					@Override
 					public void onClick(final DialogInterface pDialog, final int pWhich) {
 						SimpleActivity.this.finish();
 					}
 				})
 				.create();
 			default:
 				return super.onCreateDialog(pID);
 		}
 	}
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 }
