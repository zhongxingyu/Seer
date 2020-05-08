 /*
 SettingsScreen.java
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
 
 import java.util.Hashtable;
 
 import org.linphone.core.LinphoneAuthInfo;
 import org.linphone.core.LinphoneCore;
 import org.linphone.core.LinphoneCoreException;
 import org.linphone.core.LinphoneCoreFactory;
 import org.linphone.core.LinphoneProxyConfig;
 import org.linphone.jortp.JOrtpFactory;
 import org.linphone.jortp.Logger;
 
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.system.PersistentObject;
 import net.rim.device.api.system.PersistentStore;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.ObjectChoiceField;
 import net.rim.device.api.ui.component.SeparatorField;
 
 import net.rim.device.api.ui.container.MainScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import net.rim.device.api.ui.decor.BackgroundFactory;
 import net.rim.device.api.util.Persistable;
 
 
 class LinphonePersistentHashTable extends Hashtable implements Persistable {
 	   
 }
 public class SettingsScreen extends MainScreen implements Settings, LinphoneResource{
 
 	private PersistentObject mPersistentObject;
 	private LinphonePersistentHashTable mSettingsMap;
 	private final LinphoneCore mCore;
 	private Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
 	SettingsFieldContent mSettingsFields;
 	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
 	class SettingsFieldContent {
 		private BasicEditField mUserNameField;
 		private BasicEditField mUserPasswd;
 		private BasicEditField mDomain;
 		private BasicEditField mProxy;
 		public final String[] SIP_TRANSPORT_TYPE={"udp","tcp"};  
 		private CheckboxField mDebugMode;
 		private CheckboxField mSubstituteZero2Plus;
 		private ObjectChoiceField mTransPort;
 		VerticalFieldManager mMainFiedManager = new VerticalFieldManager();
 
 		public SettingsFieldContent (){
 			VerticalFieldManager lSipAccount = new VerticalFieldManager();
 			mMainFiedManager.add(lSipAccount);
 			LabelField lSipAccountLabelField = new LabelField(mRes.getString(SETTING_SIP_ACCOUNT));
 			lSipAccountLabelField.setFont(Font.getDefault().derive(Font.BOLD|Font.UNDERLINED));
 			lSipAccount.add(lSipAccountLabelField);
 			mUserNameField = new BasicEditField(mRes.getString(SETTING_USERNAME), "", 128, 0);
 			mUserNameField.setText(getString(SIP_USERNAME,""));
 			lSipAccount.add(mUserNameField);
 			mUserPasswd = new BasicEditField(mRes.getString(SETTING_PASSWD), "", 128, 0);
 			mUserPasswd.setText(getString(SIP_PASSWORD,""));
 			lSipAccount.add(mUserPasswd);
 			mDomain = new BasicEditField(mRes.getString(SETTING_DOMAIN), "", 128, 0);
 			mDomain.setText(getString(SIP_DOMAIN,""));
 			lSipAccount.add(mDomain);
 			mProxy = new BasicEditField(mRes.getString(SETTING_PROXY), "", 128, 0);
 			mProxy.setText(getString(SIP_PROXY,""));
 			lSipAccount.add(mProxy);
 
 			
 			SeparatorField lSipAccountSeparator = new SeparatorField();
 			mMainFiedManager.add(lSipAccountSeparator);
 
 			VerticalFieldManager lAdvanced = new VerticalFieldManager();
 			mMainFiedManager.add(lAdvanced);
 			LabelField lAvancedLabelField = new LabelField(mRes.getString(SETTING_ADVANCED));
 			lAvancedLabelField.setFont(Font.getDefault().derive(Font.BOLD|Font.UNDERLINED));
 			lAdvanced.add(lAvancedLabelField);
 			
 			mTransPort= new ObjectChoiceField(mRes.getString(SETTING_TRANSPORT),SIP_TRANSPORT_TYPE,SIP_TRANSPORT_TYPE[0].equals(getString(SIP_TRANSPORT,SIP_TRANSPORT_TYPE[0]))?0:1);
 			lAdvanced.add(mTransPort); 
 			mDebugMode = new CheckboxField(mRes.getString(SETTING_DEBUG), false);
 			mDebugMode.setChecked(getBoolean(ADVANCED_DEBUG,false));
 			lAdvanced.add(mDebugMode);
 			mSubstituteZero2Plus = new CheckboxField(mRes.getString(SETTING_ESCAPE_PLUS), false);
 			mSubstituteZero2Plus.setChecked(getBoolean(ADVANCED_SUBSTITUTE_PLUS_TO_DOUBLE_ZERO,false));
 			lAdvanced.add(mSubstituteZero2Plus);
 		}
 		public void save() {
 			mSettingsMap.put(SIP_USERNAME, mUserNameField.getText());
 			mSettingsMap.put(SIP_PASSWORD, mUserPasswd.getText());
 			mSettingsMap.put(SIP_DOMAIN, mDomain.getText());
 			mSettingsMap.put(SIP_PROXY, mProxy.getText());
 			mSettingsMap.put(SIP_TRANSPORT,SIP_TRANSPORT_TYPE[mTransPort.getSelectedIndex()]);
 			mSettingsMap.put(ADVANCED_DEBUG, new Boolean(mDebugMode.getChecked()));
 			mSettingsMap.put(ADVANCED_SUBSTITUTE_PLUS_TO_DOUBLE_ZERO, new Boolean(mSubstituteZero2Plus.getChecked()));
 			try {
 				initFromConf();
 				mPersistentObject.setContents(mSettingsMap);
 				mPersistentObject.commit();
 				
 			} catch (final LinphoneConfigException e) {
 				sLogger.error("Configuration error",e);
 				UiApplication.getUiApplication().invokeLater(new Runnable() {
 					public void run() {
 						Dialog.alert(e.getMessage());
 						
 					}
 				});
 			}
 		}
 		public Field getRootField() {
 			return mMainFiedManager;
 		}
 	}
 	SettingsScreen(LinphoneCore lc) {
 		mCore = lc;	
 		mPersistentObject = PersistentStore.getPersistentObject( "org.jlinphone.settings".hashCode() );
 		if (mPersistentObject.getContents() != null && mPersistentObject.getContents() instanceof LinphonePersistentHashTable ) {
 			mSettingsMap = (LinphonePersistentHashTable) mPersistentObject.getContents();
 		} else {
 			mSettingsMap = new LinphonePersistentHashTable();
 		}
 		setTitle("Linphone "+mRes.getString(SETTINGS));
 		((VerticalFieldManager)getMainManager()).setBackground(BackgroundFactory.createSolidBackground(Color.LIGHTGREY));
 		mSettingsFields = new SettingsFieldContent(); 
 		add(mSettingsFields.getRootField());
 		try {
 			initFromConf();
 		} catch (LinphoneConfigException e) {
 			sLogger.warn("no configuration ready yet", e);
 		}
 
 
 	}
 	
 	protected boolean onSave() {
 		mSettingsFields.save();
 		return true;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.linphone.jlinphone.gui.Settings#getBoolean(java.lang.String, boolean)
 	 */
 	public boolean getBoolean(String key,boolean defaultValue) {
 		boolean lResult = defaultValue;
 		if (mSettingsMap != null) {
 			Boolean value = (Boolean) mSettingsMap.get(key);
 			if (value != null) {
 				return value.booleanValue();
 			}
 		}
 		return lResult;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.linphone.jlinphone.gui.Settings#getString(java.lang.String, java.lang.String)
 	 */
 	public String getString(String key,String defaultValue) {
 		String lResult = defaultValue;
 		if (mSettingsMap != null) {
 			String value = (String) mSettingsMap.get(key);
 			if (value != null) {
 				return value;
 			}
 		}
 		return lResult;
 	}
 
 	public void initFromConf() throws LinphoneConfigException {
 
 		
 		//traces
 		boolean lIsDebug = getBoolean(Settings.ADVANCED_DEBUG, false);
 		LinphoneCoreFactory.instance().setDebugMode(lIsDebug);
 		
 		//1 read proxy config from preferences
 		String lUserName = getString(Settings.SIP_USERNAME, null);
 		if (lUserName == null || lUserName.length()==0) {
 			throw new LinphoneConfigException(mRes.getString(SETTING_ERROR_NO_USER));
 		}
 
 		String lPasswd = getString(Settings.SIP_PASSWORD, null);
 		if (lPasswd == null || lPasswd.length()==0) {
 			throw new LinphoneConfigException(mRes.getString(SETTING_ERROR_NO_PASSWD));
 		}
 
 		String lDomain = getString(Settings.SIP_DOMAIN, null);
 		if (lDomain == null || lDomain.length()==0) {
 			throw new LinphoneConfigException(mRes.getString(SETTING_DOMAIN));
 		}
 
 		String lTransport = getString(Settings.SIP_TRANSPORT, null);
 		LinphoneCore.Transports transport = new LinphoneCore.Transports();
 		transport.tcp = 0;
 		transport.udp = 0;
 		transport.tls = 0;
 		if (lTransport != null && "tcp".equalsIgnoreCase(lTransport)) {
 			transport.tcp = 5060;
 		} else {
 			transport.udp = 5060;
 		}
 		mCore.setSignalingTransportPorts(transport);	
 		
 		//auth
 		mCore.clearAuthInfos();
 		LinphoneAuthInfo lAuthInfo =  LinphoneCoreFactory.instance().createAuthInfo(lUserName, lPasswd,null);
 		mCore.addAuthInfo(lAuthInfo);
 
 
 		//proxy
 		String lProxy = getString(Settings.SIP_PROXY,null);
 		if (lProxy == null || lProxy.length() == 0) {
 			lProxy = "sip:"+lDomain;
 		} else if (lProxy.startsWith("sip:")== false){
 			lProxy="sip:"+lProxy;
 		}
 		//get Default proxy if any
 		LinphoneProxyConfig lDefaultProxyConfig = mCore.getDefaultProxyConfig();
 		String lIdentity = "sip:"+lUserName+"@"+lDomain;
 		try {
 			if (lDefaultProxyConfig == null) {
 				lDefaultProxyConfig = LinphoneCoreFactory.instance().createProxyConfig(lIdentity, lProxy, null,true);
				lDefaultProxyConfig.setExpires(600);
 				mCore.addProxyConfig(lDefaultProxyConfig);
 				mCore.setDefaultProxyConfig(lDefaultProxyConfig);
 
 			} else {
 				lDefaultProxyConfig.edit();
 				lDefaultProxyConfig.setIdentity(lIdentity);
 				lDefaultProxyConfig.setProxy(lProxy);
 				lDefaultProxyConfig.enableRegister(true);
 				lDefaultProxyConfig.done();
 			}
 			lDefaultProxyConfig = mCore.getDefaultProxyConfig();
 			lDefaultProxyConfig.setDialEscapePlus(getBoolean(Settings.ADVANCED_SUBSTITUTE_PLUS_TO_DOUBLE_ZERO, false));
 
 			//init network state
 			
 		} catch (LinphoneCoreException e) {
 			throw new LinphoneConfigException(mRes.getString(SETTING_ERROR_BAD_CONFIG),e);
 		}
 	}
 	public SettingsFieldContent createSettingsFields() {
 		return new SettingsFieldContent();
 	}
  }
