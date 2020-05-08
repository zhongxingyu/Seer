 /*
 DialerField.java
 Copyright (C) 2010  Belledonne Communications, Grenoble, France
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 package org.linphone.jlinphone.gui;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.microedition.pim.Contact;
 
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.system.Display;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.RichTextField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.component.TextField;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import net.rim.device.api.ui.text.PhoneTextFilter;
 
 import org.linphone.core.CallDirection;
 import org.linphone.core.LinphoneAddress;
 import org.linphone.core.LinphoneCore;
 import org.linphone.core.LinphoneCoreException;
 import org.linphone.jortp.JOrtpFactory;
 import org.linphone.jortp.Logger;
 
 public class DialerField extends VerticalFieldManager implements TabFieldItem, LinphoneResource{
 	private VerticalFieldManager mOutcallFields = new VerticalFieldManager();
 	private AdvancedSearchableContactList mASCL;
 	
 	private VerticalFieldManager mIncallFields = new VerticalFieldManager(Field.USE_ALL_WIDTH);
 	RichTextField mDisplayNameField;
 	TextField mPhoneNumberField;
 	TextField mDurationField;
 	CheckboxField mMute;
 	CheckboxField mSpeaker;
 	static final private Timer mTimer = new Timer();
 	TimerTask mCallDurationTask;
 	private static Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
 	String mDisplayName;
 	LinphoneCore mCore;
 	final static int GREEN_BUTTON_KEY=1114112;
 	PhoneTextFilter mPhoneTextFilter = new PhoneTextFilter();
 	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
 	public DialerField(LinphoneCore aCore) {
 		mCore=aCore;
 		mASCL=new AdvancedSearchableContactList() {
 			protected void onContactChosen(String uri, String displayName) {
 					try {
 						if (mCore.isInComingInvitePending()){
 							throw new LinphoneCoreException("Already in call");
 						}else{
 							LinphoneAddress lTo = mCore.interpretUrl(uri);
 							lTo.setDisplayName(displayName);
 							mCore.invite(lTo);
 						}
 					} catch (final LinphoneCoreException e) {
 						sLogger.error("call error",e);
 						UiApplication.getUiApplication().invokeLater(new Runnable() {
 							public void run() {
 								Dialog.alert(e.getMessage());
 
 							}
 						});
 					}
 			}
 			protected boolean keyDown(int keycode, int time) {
 				if (keycode==GREEN_BUTTON_KEY && (mKeywordFilter.getSelectedElement() != null ||mInputAddress.getText().length()>0)) {
 					try {
 						if (mCore.isInComingInvitePending()){
 							throw new LinphoneCoreException("Already in call");
 						}else{
 							if (mKeywordFilter.getSelectedElement() != null) {
 								Field focused=getFieldWithFocus();
 								if (focused instanceof VerticalFieldManager) {
 									Field focused2=((VerticalFieldManager)focused).getFieldWithFocus();
 									if (focused2==mKeywordFilter) {
 										setAddressAndDisplay((Contact) mKeywordFilter.getSelectedElement());										
 									}
 								}
 							}
 							if (getAddress().length() >0 ) {
 								LinphoneAddress lTo = mCore.interpretUrl(getAddress());
 								lTo.setDisplayName(getDisplayName());
 								mCore.invite(lTo);
 							}
 							return true;
 						}
 					} catch (final LinphoneCoreException e) {
 						sLogger.error("call error",e);
 						UiApplication.getUiApplication().invokeLater(new Runnable() {
 							public void run() {
 								Dialog.alert(e.getMessage());
 
 							}
 						});
 					}
 				}
 				return super.keyDown(keycode, time);
 			}
 		};
 	    mOutcallFields.add(mASCL);
 	    
 	    //incall fields
 	    mDisplayNameField = new RichTextField(Field.NON_FOCUSABLE|RichTextField.TEXT_ALIGN_HCENTER);
 	    mDisplayNameField.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,50));
 	    mIncallFields.add(mDisplayNameField);
 	    mIncallFields.add(new SeparatorField());
 	    
 	    mPhoneNumberField = new TextField(Field.NON_FOCUSABLE);
 	    mDurationField = new TextField(Field.NON_FOCUSABLE);
 	    
 	    HorizontalFieldManager lNumAndDuration = new HorizontalFieldManager(Field.USE_ALL_WIDTH) {
 	    	{
 	    	    add(mPhoneNumberField);
 	    	    add(mDurationField);	
 	    	}
 			protected void sublayout(int maxWidth, int maxHeight) {
 				layoutChild(mPhoneNumberField, 2*maxWidth/3, maxHeight);   
 				layoutChild(mDurationField, maxWidth/3, maxHeight);   
 				setPositionChild(mPhoneNumberField, 10, 0);    
 				setPositionChild(mDurationField, maxWidth - maxWidth/3, 0);    
 				setExtent(maxWidth, mPhoneNumberField.getHeight());  
 			}
 	    	
 	    };
 	    
 
 
 	    mIncallFields.add(lNumAndDuration);
 	    mMute =  new CheckboxField(mRes.getString(MUTE),false);
 	    mMute.setChangeListener(new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				mCore.muteMic(((CheckboxField)field).getChecked());
 			}
 		});
 
 	    mSpeaker =  new CheckboxField(mRes.getString(SPEAKER),false);
 	    mSpeaker.setChangeListener(new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				mCore.enableSpeaker(((CheckboxField)field).getChecked());
 			}
 		});
 	   
 	    
 	    HorizontalFieldManager lMuteAndSpeaker = new HorizontalFieldManager(Field.USE_ALL_WIDTH) {
 	    	{
 	    	    add(mMute);
 	    	    add(mSpeaker);	
 	    	}
 			protected void sublayout(int maxWidth, int maxHeight) {
 				layoutChild(mMute, 2*maxWidth/3, maxHeight);   
 				layoutChild(mSpeaker, maxWidth/3, maxHeight);   
 				int lYPosition = Display.getHeight() - 2*TabField.SIZE -mDisplayNameField.getContentHeight() - mPhoneNumberField.getHeight()-60;
 				setPositionChild(mMute, 10, lYPosition);    
 				setPositionChild(mSpeaker, maxWidth - mSpeaker.getWidth()-10, lYPosition);    
 				setExtent(maxWidth, lYPosition+mSpeaker.getHeight());  
 			}
 	    	
 	    };
 	
 
 	    mIncallFields.add(lMuteAndSpeaker);
 	    
 	    enableOutOfCallFields();
 	}
 	
 
 	public boolean keyChar(char ch, int status, int time) {
 		char lNumber = mPhoneTextFilter.convert(ch, 0);
 		if (mCore.isIncall() && 
 				((0<=Character.digit(lNumber,10) && Character.digit(lNumber,10)<10)
 				|| lNumber=='#' || lNumber=='*')) {
 			 mCore.sendDtmf(lNumber);
 			return true;
 		} else {
 			return super.keyChar(ch, status, time);
 		}
 	}
 
 
 	public void enableIncallFields() {
 		if (mOutcallFields.getManager() == this ) delete (mOutcallFields);
 		add(mIncallFields);
 		String lDisplay=null;
 		String lNumber="";
 		if (mCore.getCurrentCall().getDirection() == CallDirection.Incoming) {
 			LinphoneAddress lIncallAddress = mCore.getCurrentCall().getRemoteAddress();
 			lDisplay = lIncallAddress.getDisplayName();
 			lNumber = lIncallAddress.getUserName();
 		} else {
 			lDisplay = mDisplayName;
 			lNumber = mASCL.getAddress();
 		
 		}
 		if (lDisplay !=null) {
 			mDisplayNameField.setText(lDisplay);
 			mPhoneNumberField.setText(lNumber);
 		} else {
 			mDisplayNameField.setText(lNumber);
 			mPhoneNumberField.setText("");
 		}
 		mSpeaker.setChecked(mCore.isSpeakerEnabled());
 		mMute.setChecked(mCore.isMicMuted());
 		mCallDurationTask = new TimerTask() {
 			int mDuration=0;
 			{
 				mDurationField.setText("0s");
 			}
 			public void run() {
 				mDuration++;
 				if (mDuration <=60) {
 					mDurationField.setText(mDuration+"s");
 				} else {
					mDurationField.setText(mDuration/60+":"+(mDuration - mDuration/60)+"s" );
 				}
 			}
 		};
 		mTimer.scheduleAtFixedRate(mCallDurationTask, 0, 1000);
 	}
 	public void enableOutOfCallFields() {
 		if (mIncallFields.getManager() == this ) {
 			mCallDurationTask.cancel();
 			delete (mIncallFields);
 		}
 		add(mOutcallFields);
 	}
 
 
 	public void onSelected() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void onUnSelected() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public boolean navigateBack() {
 		return false;
 	}
 	
 	public void setAddress(String aValue) {
 		mASCL.setAddress(aValue);
 	}
 	public void setDisplayName(String aDisplayName) {
 		mASCL.setDisplayName(aDisplayName);
 	}
 	
 }
