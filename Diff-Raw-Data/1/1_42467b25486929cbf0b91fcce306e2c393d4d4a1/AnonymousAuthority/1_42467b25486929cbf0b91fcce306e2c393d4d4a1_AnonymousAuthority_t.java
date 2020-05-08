 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.trentorise.smartcampus.ac.authorities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import eu.trentorise.smartcampus.ac.AuthListener;
 import eu.trentorise.smartcampus.ac.DeviceUuidFactory;
 
 /**
  * @author raman
  *
  */
 public class AnonymousAuthority extends WebAuthority {
 
 	/**
 	 * @param mName
 	 */
 	public AnonymousAuthority(String mName) {
 		super(mName);
 	}
 
 	private String mToken = null;
 	
 	@Override
 	public boolean isLocal() {
 		return true;
 	}
 
 	@Override
 	public void authenticate(final Activity activity, final AuthListener listener, final String clientId, final String clientSecret) {
 		mActivity = activity;
 		mAuthListener = listener;
 		mClientId = clientId;
 		mClientSecret = clientSecret;
 		mToken = new DeviceUuidFactory(activity).getDeviceUuid().toString();
		setUp();
 	}
 
 	@Override
 	protected String prepareURL(Intent intent) throws NameNotFoundException {
 		String url = super.prepareURL(intent);
 		url += "&token="+ mToken;
 		return url;
 	}
 }
