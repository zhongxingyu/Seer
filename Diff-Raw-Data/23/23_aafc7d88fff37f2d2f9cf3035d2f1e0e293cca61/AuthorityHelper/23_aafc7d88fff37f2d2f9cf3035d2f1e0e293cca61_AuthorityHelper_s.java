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
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
import eu.trentorise.smartcampus.ac.Constants;
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 /**
  * @author raman
 *
  */
 public class AuthorityHelper {
 
 	public static final String A_FBK = "fbk";
 	public static final String A_UNITN = "unitn";
 	public static final String A_GOOGLE = "google";
 	public static final String A_GOOGLE_LOCAL = "googlelocal";
 	public static final String A_ANONYMOUS = "anonymous";
	
	protected static Map<String,AuthorityHandler> mAuthorityMap = new HashMap<String, AuthorityHandler>();
	protected static Map<String,String> mAuthorityNameMap = new HashMap<String, String>();
 	static {
 		mAuthorityMap.put(A_FBK, new WebAuthority(A_FBK));
 		mAuthorityMap.put(A_UNITN, new WebAuthority(A_UNITN));
 		mAuthorityMap.put(A_GOOGLE_LOCAL, new GoogleAuthority(A_GOOGLE_LOCAL));
 		mAuthorityMap.put(A_ANONYMOUS, new AnonymousAuthority(A_ANONYMOUS));
		
 		mAuthorityNameMap.put(A_FBK, "FBK");
 		mAuthorityNameMap.put(A_UNITN, "UNITN");
 		mAuthorityNameMap.put(A_GOOGLE_LOCAL, "GOOGLE");
 		mAuthorityNameMap.put(A_ANONYMOUS, "ANONYMOUS");
 
 	}
	
 	/**
 	 * @param name
 	 * @return handler for the specified authority
 	 */
 	public static AuthorityHandler getAuthorityHandlerForName(String name) {
 		return mAuthorityMap.get(name);
 	}
 
 	/**
 	 * @return
 	 */
 	public static Collection<String> getAuthorities(Context ctx) {
 		try {
 			return Constants.getRequiredAuthorities(ctx);
 		} catch (NameNotFoundException e) {
 			return Collections.unmodifiableCollection(mAuthorityMap.keySet());
 		}
 	}
 
 	/**
 	 * @param name
 	 * @return
 	 */
 	public static String getAuthorityLabelForName(String name) {
 		return mAuthorityNameMap.get(name);
 	}
 }
