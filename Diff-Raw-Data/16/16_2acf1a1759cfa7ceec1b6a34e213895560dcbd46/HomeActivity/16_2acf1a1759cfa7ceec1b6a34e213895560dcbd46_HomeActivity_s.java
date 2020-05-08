 package com.workhub.android.activity;
 
 import jade.android.AgentContainerHandler;
 import jade.android.AndroidHelper;
 import jade.android.MicroRuntimeService;
 import jade.android.MicroRuntimeServiceBinder;
 import jade.android.RuntimeCallback;
 import jade.android.RuntimeService;
 import jade.core.AID;
 import jade.core.MicroRuntime;
 import jade.core.Profile;
 import jade.gui.GuiEvent;
 import jade.util.leap.Properties;
 import jade.wrapper.AgentContainer;
 import jade.wrapper.ControllerException;
 import jade.wrapper.StaleProxyException;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.ui.activity.SimpleLayoutGameActivity;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.MediaStore;
 import android.widget.Toast;
 
 import com.workhub.android.R;
 import com.workhub.android.element.AbstractElement;
 import com.workhub.android.element.BaseElement;
 import com.workhub.android.element.PictureElement;
 import com.workhub.android.scene.MainScene;
 import com.workhub.android.utils.ConstantsAndroid;
 import com.workhub.android.utils.Ressources;
 import com.workhub.jade.agent.ClientAgent;
 import com.workhub.jade.agent.ClientAgentInterface;
 import com.workhub.model.ElementModel;
 import com.workhub.utils.Constants;
 
 public class HomeActivity extends SimpleLayoutGameActivity implements PropertyChangeListener{
 
 	private Camera mCamera;
 	private Ressources res;
 	private ServiceConnection serviceConnection;
 	private MicroRuntimeServiceBinder microRuntimeServiceBinder;
 
 	
 	@Override
 	protected void onCreate(Bundle pSavedInstanceState) {
 
 
 		super.onCreate(pSavedInstanceState);
 
 	//	startJade(nickname, AndroidHelper.getLocalIPAddress(), "1099" );
 			startJade(nickname, "192.168.43.67", "1099" );
 
 	}
 
 	private String nickname = "Florian";
 	private AbstractElement askElement;
 	private MainScene scene;
 
 
 
 
 	public void startJade(final String nickname, final String host,
 			final String port) {
 
 		final Properties profile = new Properties();
 		profile.setProperty(Profile.MAIN_HOST, host);
 		profile.setProperty(Profile.MAIN_PORT, port);
 		profile.setProperty(Profile.MAIN, Boolean.TRUE.toString());
 		profile.setProperty(Profile.JVM, Profile.ANDROID);
 
 		if (AndroidHelper.isEmulator()) {
 			// Emulator: this is needed to work with emulated devices
 			profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
 		} else {
 			profile.setProperty(Profile.LOCAL_HOST,
 					AndroidHelper.getLocalIPAddress());
 		}
 		// Emulator: this is not really needed on a real device
 		profile.setProperty(Profile.LOCAL_PORT, "1099");
 
 		if (microRuntimeServiceBinder == null) {
 			serviceConnection = new ServiceConnection() {
 				public void onServiceConnected(ComponentName className,
 						IBinder service) {
 					microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
 					System.out.println("Gateway successfully bound to MicroRuntimeService");
 					startContainer(nickname, profile);
 				};
 
 				public void onServiceDisconnected(ComponentName className) {
 					microRuntimeServiceBinder = null;
 					System.out.println("Gateway unbound from MicroRuntimeService");
 				}
 			};
 			System.out.println( "Binding Gateway to MicroRuntimeService...");
 			bindService(new Intent(getApplicationContext(),
 					MicroRuntimeService.class), serviceConnection,
 					Context.BIND_AUTO_CREATE);
 		} else {
 			System.out.println( "MicroRumtimeGateway already binded to service");
 			startContainer(nickname, profile);
 		}
 	}
 	private void startContainer(final String nickname, final Properties profile) {
 
 		if (!MicroRuntime.isRunning()) {
 			RuntimeService runtimeService = new RuntimeService();
 			runtimeService.createMainAgentContainer(new RuntimeCallback<AgentContainerHandler>() {
 
 				@Override
 				public void onSuccess(AgentContainerHandler arg0) {
 					System.out.println("Successfully start of the container...");
 					startAgent(nickname);
 					microRuntimeServiceBinder.startAgentContainer(profile, 
 							new RuntimeCallback<Void>() {
 						@Override
 						public void onSuccess(Void thisIsNull) {
 							System.out.println("Successfully start of the container...");
 							startAgent(nickname);
 						}
 
 						@Override
 						public void onFailure(Throwable throwable) {
 							System.out.println( "Failed to start the container...");
 						}
 					});
 
 				}
 
 				@Override
 				public void onFailure(Throwable arg0) {
 				}
 			});
 
 		} else {
 			startAgent(nickname);
 		}
 	}
 
 
 	private void startAgent(final String nickname) {
 		microRuntimeServiceBinder.startAgent(nickname,
 				ClientAgent.class.getName(),
 				new Object[] { this },
 				new RuntimeCallback<Void>() {
 			@Override
 			public void onSuccess(Void thisIsNull) {
 				System.out.println("Successfully start of the "
 						+ ClientAgent.class.getName() + "...");
 
 			}
 
 			@Override
 			public void onFailure(Throwable throwable) {
 				System.out.println("Failed to start the "
 						+ ClientAgent.class.getName() + "...");
 			}
 		});
 	}
 	private ClientAgentInterface getAgent() throws StaleProxyException, ControllerException{
 		return  (ClientAgentInterface) MicroRuntime.getAgent(nickname).getO2AInterface(ClientAgentInterface.class);
 	}
 
 	private void fireOnGuiEvent(GuiEvent event) {
 		try {
 			getAgent().fireOnGuiEvent(event);
 		} catch (StaleProxyException e) {
 			e.printStackTrace();
 		} catch (ControllerException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		int CAMERA_LARGEUR = getResources().getDisplayMetrics().widthPixels;
 		int CAMERA_HAUTEUR = getResources().getDisplayMetrics().heightPixels;
 
 		int resolutionX= (int) (CAMERA_LARGEUR);
 		int resolutionY= (int) (CAMERA_HAUTEUR);
 		float x = Math.min(((float)resolutionX/CAMERA_LARGEUR), ((float)resolutionY/CAMERA_HAUTEUR));
 		resolutionX = (int) (CAMERA_LARGEUR*x);
 		resolutionY = (int) (CAMERA_HAUTEUR*x);
 		mCamera = new Camera(0, 0, resolutionX, resolutionY);
 		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, 
 				new RatioResolutionPolicy(CAMERA_LARGEUR, CAMERA_HAUTEUR), mCamera);
 		engineOptions.getAudioOptions().setNeedsMusic(true);
 		engineOptions.getAudioOptions().setNeedsSound(true);
 		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
 		return engineOptions;
 	}
 
 	@Override
 	protected void onCreateResources() {
 
 		res = new Ressources(this, mCamera);
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 
 		scene = new MainScene(res); 
 		scene.populate();
 		return scene;
 	}
 
 	@Override
 	protected int getLayoutID() {
 		return R.layout.game_activity;
 	}
 
 	@Override
 	protected int getRenderSurfaceViewID() {
 		return R.id.layout_rendersurfaceview;
 	}
 
 	public void loadImage(AbstractElement e, int requestCode) {
 		this.askElement = e;
 		Intent intent = new Intent();
 		intent.setType("image/*");
 		intent.setAction(Intent.ACTION_GET_CONTENT);
 		startActivityForResult(intent, requestCode);
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == RESULT_OK) {
 			if (requestCode == PictureElement.SELECT_PICTURE) {
 				Uri selectedImageUri = data.getData();
 				String imgPath;
 				if(selectedImageUri.toString().startsWith("file://")) {
 					imgPath = selectedImageUri.toString().replaceAll("file://", "");
 				} else {
 					imgPath = getPath(selectedImageUri);
 				}
 				((PictureElement)askElement).onActivityResult(imgPath);
 
 			}
 		}
 		if(askElement!=null){
 			askElement = null;
 		}
 	}
 
 	public String getPath(Uri uri) {
 
 		String[] projection = { MediaStore.Images.Media.DATA };
 		Cursor cursor = managedQuery(uri, projection, null, null, null);
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 
 		return cursor.getString(column_index);
 	}
 
 	public void sendElement(AID dest, AID elementAgent){
 		Object[] param = {dest , elementAgent};
 
 		GuiEvent event = new GuiEvent(null,Constants.EVENT_TYPE_SEND);
 		event.addParameter(param);
 		fireOnGuiEvent(event);
 	}
 	public void askEdition(AID elementAgent){
 		GuiEvent event = new GuiEvent(null,Constants.EVENT_TYPE_ASK_EDIT);
 		event.addParameter(elementAgent);
 		fireOnGuiEvent(event);
 	}
 
 
 	public void createElement(int elementType){
 		//GuiEvent event = new GuiEvent(null,Constants.EVENT_TYPE_CREATE_ELEMENT);
 		
 		
 		 Date dNow = new Date( );
 	      SimpleDateFormat ft = 
 	      new SimpleDateFormat ("hh:mm:ss");
 	      
 		try {
 			microRuntimeServiceBinder.startAgent("Nouvel element : "+ft.format(dNow),"com.workhub.jade.agent.ElementAgent",new Object[]{elementType, getAgent().getAgentAID()}, new RuntimeCallback<Void>() {
 				
 				@Override
 				public void onSuccess(Void arg0) {
 				}
 				
 				@Override
 				public void onFailure(Throwable arg0) {
 				}
 			});
 		} catch (StaleProxyException e) {
 			e.printStackTrace();
 		} catch (ControllerException e) {
 			e.printStackTrace();
 		}
 				
 //		event.addParameter(elementType);
 //		fireOnGuiEvent(event);
 	}
 
 	public void deleteElement(AID elementAgent){
 		GuiEvent event = new GuiEvent(null,Constants.EVENT_TYPE_DELETE);
 		event.addParameter(elementAgent);
 		fireOnGuiEvent(event);
 	}
 
 	public void saveElement(ElementModel model){
 		GuiEvent event = new GuiEvent(null, Constants.EVENT_TYPE_SAVE);
 		event.addParameter(model);
 		fireOnGuiEvent(event);
 	}
 
 	public void getElement(AID agentAID ){
 		GuiEvent event = new GuiEvent(null, Constants.EVENT_TYPE_CHARGE);
 		event.addParameter(agentAID);
 		fireOnGuiEvent(event);
 	}
 
 	public void getElementList(){
 		GuiEvent event = new GuiEvent(null, Constants.EVENT_TYPE_GET_ELEMENTS);
 		fireOnGuiEvent(event);
 	}
 
 	public void getNeightbourgList(){
 		GuiEvent event = new GuiEvent(null, Constants.EVENT_TYPE_GET_NEIGHBOURGS);
 		fireOnGuiEvent(event);
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		switch ((Integer.parseInt(event.getPropertyName()))) {
 		case Constants.EVENT_TYPE_CHANGE:
 		{
 			AID aidModel = (AID)event.getNewValue();
 			BaseElement element = scene.getElement(aidModel);
 			if(element!=null){
 				getElement(aidModel);
 			}
 			break;
 		}
 		case Constants.EVENT_TYPE_CONTENU:
 		{
 			ElementModel model = (ElementModel)event.getNewValue();
 			BaseElement element = scene.getElement(model.getAgent());
 			if(element!=null){
 				element.setModel(model);
 			}else{
 				try {
 					scene.addElement(model);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			break;
 		}
 		case Constants.EVENT_TYPE_DIED:
 		{
 			AID agent = (AID)event.getNewValue();
 			BaseElement element = scene.getElement(agent);
 			if(element!=null){
 				element.remove();
 			}
 			break;
 		}
 		case Constants.EVENT_TYPE_ELEMENTS:
 		{
 			Map<AID, String> map = (Map<AID, String>)event.getNewValue();
 			
 			for (Entry<AID, String> entry : map.entrySet()) {
 				scene.addToAdapter(entry);
 			}
			
 
 			break;
 		}
 		case Constants.EVENT_TYPE_NEIGHBOURS:
 		{
 			Map<AID, String> map = (Map<AID, String>)event.getNewValue();
 			for (Entry<AID, String> entry : map.entrySet()) {
 				scene.addToAdapter(entry);
 			}
 
 			break;
 		}
 		case Constants.EVENT_TYPE_CAN_EDIT:
 		{
 			AID aidModel = (AID)event.getNewValue();
 			BaseElement element = scene.getElement(aidModel);
 			if(element!=null){
 				element.edit();
 			}
 			
 			break;
 		}
 		case Constants.EVENT_TYPE_CANT_EDIT:
 		{
 			Toast.makeText(getApplicationContext(), "Vous ne pouvez pas éditer l'élément", Toast.LENGTH_SHORT).show();			
 			break;
 		}
 		}
 
 	}
 
 }
