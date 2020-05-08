 package org.linphone.jlinphone.gui;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.linphone.core.LinphoneCore;
 import org.linphone.core.LinphoneCoreException;
 import org.linphone.core.LinphoneCoreFactory;
 import org.linphone.core.LinphoneCoreListener;
 import org.linphone.core.LinphoneCore.GeneralState;
 import org.linphone.jortp.JOrtpFactory;
 import org.linphone.jortp.Logger;
 
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.FocusChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Manager;
 import net.rim.device.api.ui.XYEdges;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.BitmapField;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.EditField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.RichTextField;
 import net.rim.device.api.ui.container.GridFieldManager;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 import net.rim.device.api.ui.container.MainScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import net.rim.device.api.ui.decor.Background;
 import net.rim.device.api.ui.decor.BackgroundFactory;
 import net.rim.device.api.ui.decor.Border;
 import net.rim.device.api.ui.decor.BorderFactory;
 
 public class LinphoneScreen extends MainScreen implements FieldChangeListener, FocusChangeListener, LinphoneCoreListener{
 	private BasicEditField mInputAddress;
 	private ButtonField mCall;
 	private ButtonField mHangup;
 	private HorizontalFieldManager mLayout;
 	private LabelField mStatus;
 	private Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
 	private LinphoneCore mCore;
 	private Timer mTimer;
 	
 	LinphoneScreen()
     {
 		LinphoneCoreFactory.setFactoryClassName("org.linphone.jlinphone.core.LinphoneFactoryImpl");
 		VerticalFieldManager v=new VerticalFieldManager();
 		mInputAddress=new BasicEditField(null,null);
 		XYEdges edges = new XYEdges(8,8,8,8);
 		Border border=BorderFactory.createRoundedBorder(edges);
 		mInputAddress.setBorder(border);
 		mInputAddress.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,30));
 		mCall=new ButtonField("      ",Field.USE_ALL_WIDTH|Field.FIELD_LEFT|ButtonField.CONSUME_CLICK);
 		Bitmap bitmap=Bitmap.getBitmapResource("startcall-green.png");
 		Background b=BackgroundFactory.createBitmapBackground(bitmap,Background.POSITION_X_CENTER,Background.POSITION_Y_CENTER,Background.REPEAT_SCALE_TO_FIT);
 		mCall.setBackground(b);
 		mCall.setMargin(10, 10, 10, 10);
 		
 		mHangup=new ButtonField("      ",Field.USE_ALL_WIDTH|Field.FIELD_RIGHT|ButtonField.CONSUME_CLICK);
 		bitmap=Bitmap.getBitmapResource("stopcall-red.png");
 		b=BackgroundFactory.createBitmapBackground(bitmap,Background.POSITION_X_CENTER,Background.POSITION_Y_CENTER,Background.REPEAT_SCALE_TO_FIT);
 		mHangup.setBackground(b);
 		mHangup.setMargin(10, 10, 10, 10);
 		
 		mLayout=new HorizontalFieldManager(Field.FIELD_HCENTER|HorizontalFieldManager.NO_HORIZONTAL_SCROLL);
 	
 		mStatus=new LabelField("",Field.FIELD_BOTTOM);
         // Set the displayed title of the screen       
         setTitle("Linphone");
 
         // Add a read only text field (RichTextField) to the screen.  The
         // RichTextField is focusable by default. Here we provide a style
         // parameter to make the field non-focusable.
         add(v);
         
         //mLayout.add(mCall);
         //mLayout.add(mHangup);
         v.add(new LabelField("SIP address or phone number:"));
         v.add(mInputAddress);
         v.add(mLayout);
         mLayout.add(mCall);
         mLayout.add(mHangup);
         v.add(mStatus);
         
         mCall.setChangeListener(this);
         mHangup.setChangeListener(this);
         
         try {
         	
			LinphoneCoreFactory.instance().createLinphoneCore(this, null, null, this);
 		} catch (LinphoneCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
 		mTimer=new Timer();
 		TimerTask task=new TimerTask(){
 
 			public void run() {
 				mCore.iterate();
 			}
 			
 		};
 		mTimer.scheduleAtFixedRate(task, 0, 200);
     }
 
     
     /**
      * Displays a dialog box to the user with the text "Goodbye!" when the
      * application is closed.
      * 
      * @see net.rim.device.api.ui.Screen#close()
      */
     public void close()
     {
         // Display a farewell message before closing the application
         Dialog.alert("Goodbye!");
         mCore.destroy();
         super.close();
     }
 
 
 	public void fieldChanged(Field field, int context) {
 		if (field==mCall){
 			sLogger.info("Called button pressed.");
 			try {
 				if (mCore.isInComingInvitePending()){
 					mCore.acceptCall();
 				}else{
 					mCore.invite(mInputAddress.getText());
 				}
 			} catch (LinphoneCoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else if (field==mHangup){
 			sLogger.info("Hangup button pressed");
 			mCore.terminateCall();
 		}
 	}
 
 
 	public void focusChanged(Field field, int eventType) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void authInfoRequested(LinphoneCore lc, String realm, String username) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void byeReceived(LinphoneCore lc, String from) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void displayMessage(LinphoneCore lc, String message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void displayStatus(LinphoneCore lc, String message) {
 		mStatus.setText(message);
 	}
 
 
 	public void displayWarning(LinphoneCore lc, String message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void generalState(LinphoneCore lc, GeneralState state) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void inviteReceived(LinphoneCore lc, String from) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void show(LinphoneCore lc) {
 		// TODO Auto-generated method stub
 		
 	}   
 }
