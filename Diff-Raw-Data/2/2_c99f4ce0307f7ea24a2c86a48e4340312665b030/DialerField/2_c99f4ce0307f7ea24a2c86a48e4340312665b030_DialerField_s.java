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
 
 import javax.microedition.pim.Contact;
 import javax.microedition.pim.PIMException;
 
 import org.linphone.core.LinphoneAddress;
 import org.linphone.core.LinphoneCore;
 import org.linphone.core.LinphoneCoreException;
 import org.linphone.jortp.JOrtpFactory;
 import org.linphone.jortp.Logger;
 
 import net.rim.device.api.system.Characters;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.KeywordFilterField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.component.TextField;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import net.rim.device.api.ui.text.PhoneTextFilter;
 
 public class DialerField extends VerticalFieldManager {
 	private TextField  mInputAddress;
 	private KeywordFilterField mkeyWordField;    
 	private static Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
 	String mDisplayName;
 	LinphoneCore mCore;
 	final static int GREEN_BUTTON_KEY=1114112;
 	public DialerField(LinphoneCore aCore) {
 		mCore=aCore;
 		try {
 		    
 			mkeyWordField = new SearchableContactList(new SearchableContactList.Listener() {
 
 				public void onSelected(Contact selected) {
 					setAddressAndDisplay(selected);
 				}
 			}).getKeywordFilterField();
 		  } catch (PIMException e) {
 			  sLogger.error("Cannot open contact list",e);
 		  }
 		  mkeyWordField.setChangeListener(new FieldChangeListener() {
 
 			public void fieldChanged(Field field, int context) {
 				if (mkeyWordField.getKeyword().length() == 0) {
 					mkeyWordField.setKeyword(null);
 					 mInputAddress.setLabel("sip:");
 				}
 			}
 			  
 		  });
 		  mkeyWordField.setKeywordField(new TextField(Field.NON_FOCUSABLE));
 		  mkeyWordField.getKeywordField().setLabel("Find:");
 		  mkeyWordField.getKeywordField().setEditable(false);
 		  mInputAddress = new TextField(Field.FOCUSABLE) {
 	    	PhoneTextFilter mPhoneTextFilter = new PhoneTextFilter();
 	    	boolean mInDigitMode=true;
 	    	protected boolean insert(char charater, int arg1) {
 				char lNumber = mPhoneTextFilter.convert(charater, 0);
 				StringBuffer lnewKey = new StringBuffer(mkeyWordField.getKeywordField().getText());
 				mkeyWordField.setKeyword(lnewKey.insert(getCursorPosition(), charater).toString());
 				
 				if (mInDigitMode ==true && 0<=Character.digit(lNumber,10) && Character.digit(lNumber,10)<10) {
 					 return super.insert(lNumber, arg1);
 				} else {
 					if (mInDigitMode==true) {
 						setText(mkeyWordField.getKeyword());
 						mInDigitMode=false;
 						return true;
 					}
 					return super.insert(charater, arg1);
 				}
 				
 			}
 			protected synchronized boolean backspace() {
 				if(getCursorPosition()<=mkeyWordField.getKeyword().length()){
 					StringBuffer lnewKey = new StringBuffer(mkeyWordField.getKeyword());
 					mkeyWordField.setKeyword(lnewKey.delete(getCursorPosition()-1,getCursorPosition()).toString());
 				}
 				return super.backspace();
 			}
 			protected boolean keyChar(char key, int status, int time) {
 				mDisplayName=null; //Erase display name if any key is manually entered
 				if (getTextLength()!=0) {
 					mInputAddress.setLabel("");
 				} else {
 					mInDigitMode=true;
 				}
 				if (key == Characters.BACKSPACE && getCursorPosition()==0 && getTextLength() !=0) {
 					StringBuffer lnewKey = new StringBuffer(mkeyWordField.getKeywordField().getText());
 					mkeyWordField.setKeyword(lnewKey.delete(0,1).toString());
 				}
 				if (getTextLength() == 0 || (key == Characters.BACKSPACE && getTextLength()==1)) {
 					setLabel("sip:");
 				}
 				return super.keyChar(key, status, time);
 			}
 			
 
 	    };
 	    mInputAddress.setLabel("sip:");
 	    mInputAddress.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,50));
 		add(mInputAddress);
 		add(new SeparatorField());
 		add(mkeyWordField.getKeywordField());
 		add(new SeparatorField());
 		add(mkeyWordField);
 	}
 	
 	
 	protected boolean keyDown(int keycode, int time) {
 		if (keycode==GREEN_BUTTON_KEY && (mkeyWordField.getSelectedElement()!=null ||mInputAddress.getText().length()>0)) {
 			try {
 				if (mCore.isInComingInvitePending()){
 					throw new LinphoneCoreException("Already in call");
 				}else{
					if (mkeyWordField.getSelectedElement() != null && mkeyWordField.isFocus()) {
 						DialerField.this.setAddressAndDisplay((Contact) mkeyWordField.getSelectedElement());
 					}
 					LinphoneAddress lTo = mCore.interpretUrl(getAddress());
 					lTo.setDisplayName(getDisplayName());
 					mCore.invite(lTo);
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
 
 	private void setAddressAndDisplay (Contact aContact) {
 		setAddress( aContact.getString(Contact.TEL, 0));
 		String[] lContactNames = aContact.getStringArray(Contact.NAME, 0);
 		StringBuffer lDisplayName = new StringBuffer();
 
 		if (lContactNames[Contact.NAME_GIVEN] != null ) {
 			lDisplayName.append(lContactNames[Contact.NAME_GIVEN]);
 		}
 		if (lContactNames[Contact.NAME_FAMILY] != null ) {
 			if (lDisplayName.length()!= 0) lDisplayName.append(' ');
 			lDisplayName.append(lContactNames[Contact.NAME_FAMILY]);
 		}
 		if (lDisplayName.length()!=0) {
 			setDisplayName(lDisplayName.toString());
 		} else {
 			setDisplayName(null);
 		}
 	}
 	public void setAddress(String aValue) {
 		if (aValue.length()>0) mInputAddress.setLabel(null);
 		mInputAddress.setText(aValue);
 	}
 	public String getAddress() {
 		return mInputAddress.getText();
 	}
 	public String getDisplayName() {
 		return mDisplayName;
 	}
 	public void setDisplayName(String aDisplayName) {
 		mDisplayName=aDisplayName;
 	}
 }
