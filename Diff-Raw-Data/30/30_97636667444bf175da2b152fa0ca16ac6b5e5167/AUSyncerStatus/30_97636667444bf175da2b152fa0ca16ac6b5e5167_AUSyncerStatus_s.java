 /*
  * Copyright (C) 2012 Jacek Marchwicki <jacek.marchwicki@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License
  */
 
 package com.appunite.syncer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 @SuppressWarnings("UnusedDeclaration")
 public class AUSyncerStatus implements Parcelable {
 
 	private static final String JSON_DEFAULT_ERROR = "ERROR";
 	private static final String JSON_DEFAULT_ERRORS = "ERRORS";
 
 	private static final int SUCCESS = 0;
 
 	private static final int NO_INTERNET_CONNECTION = 2;
 	private static final int INTERNAL_ISSUE = 3;
 	private static final int CUSTOM_ERROR = 5;
 
 	private final int mMessage;
 	private final long mStatusTimeMs;
 	private final JSONObject mMessageObject;
 	private final long mLastDownloaded;
 
 	public static final Parcelable.Creator<AUSyncerStatus> CREATOR = new Parcelable.Creator<AUSyncerStatus>() {
 		public AUSyncerStatus createFromParcel(Parcel in) {
 			return new AUSyncerStatus(in);
 		}
 
 		public AUSyncerStatus[] newArray(int size) {
 			return new AUSyncerStatus[size];
 		}
 	};
 
 	private AUSyncerStatus(int message, JSONObject msgObject) {
 		mMessage = message;
 		mStatusTimeMs = System.currentTimeMillis();
 		mMessageObject = msgObject;
 		if (message == SUCCESS) {
 			mLastDownloaded = mStatusTimeMs;
 		} else {
 			mLastDownloaded = -1L;
 		}
 	}
 
 	AUSyncerStatus(int message, long statusTimeMs, long lastDownloaded, JSONObject messageObject) {
 		mMessage = message;
 		mStatusTimeMs = statusTimeMs;
 		mMessageObject = messageObject;
 		mLastDownloaded = lastDownloaded;
 	}
 
 	private AUSyncerStatus(Parcel in) {
 		mMessage = in.readInt();
 		mStatusTimeMs = in.readLong();
		String messageObjectStr = in.readString();

		if (messageObjectStr == null) {
			mMessageObject = null;
		} else {
			try {
				mMessageObject = new JSONObject(messageObjectStr);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
 		mLastDownloaded = in.readLong();
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(mMessage);
 		dest.writeLong(mStatusTimeMs);
		dest.writeString(mMessageObject == null ? null : mMessageObject
				.toString());
 		dest.writeLong(mLastDownloaded);
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	public static AUSyncerStatus statusSuccess() {
 		return new AUSyncerStatus(SUCCESS, null);
 	}
 
 	static AUSyncerStatus statusNeverDownloaded() {
 		return new AUSyncerStatus(SUCCESS, System.currentTimeMillis(), -1L,
 				null);
 	}
 
 	public static AUSyncerStatus statusNoInternetConnection() {
 		return new AUSyncerStatus(NO_INTERNET_CONNECTION, null);
 	}
 	
 	public static AUSyncerStatus statusInternalIssue() {
 		return new AUSyncerStatus(INTERNAL_ISSUE, null);
 	}
 
 	public static AUSyncerStatus statusInternalIssue(JSONObject msgObject) {
 		return new AUSyncerStatus(INTERNAL_ISSUE, msgObject);
 	}
 
 	public static AUSyncerStatus statusInternalIssue(String error) {
 		return new AUSyncerStatus(INTERNAL_ISSUE, fromString(error));
 	}
 
 	public static AUSyncerStatus statusInternalIssue(Collection<String> errors) {
 		return new AUSyncerStatus(INTERNAL_ISSUE, fromStringsCollection(errors));
 	}
 	
 	public static AUSyncerStatus statusCustomError() {
 		return new AUSyncerStatus(CUSTOM_ERROR, null);
 	}
 
 	public static AUSyncerStatus statusCustomError(JSONObject msgObject) {
 		return new AUSyncerStatus(CUSTOM_ERROR, msgObject);
 	}
 
 	public static AUSyncerStatus statusCustomError(String error) {
 		return new AUSyncerStatus(CUSTOM_ERROR, fromString(error));
 	}
 
 	public static AUSyncerStatus statusCustomError(Collection<String> errors) {
 		return new AUSyncerStatus(CUSTOM_ERROR, fromStringsCollection(errors));
 	}
 
 	private static JSONObject fromStringsCollection(Collection<String> errors) {
 		try {
 			JSONArray array = new JSONArray();
 			for (String error : errors) {
 				array.put(error);
 			}
 			return new JSONObject().put(JSON_DEFAULT_ERRORS, array);
 		} catch (JSONException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private static JSONObject fromString(String error) {
 		try {
 			return new JSONObject().put(JSON_DEFAULT_ERROR, error);
 		} catch (JSONException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public boolean isSuccess() {
 		return mMessage == SUCCESS;
 	}
 
 	public boolean isNeverDownloaded() {
 		return mLastDownloaded == -1L;
 	}
 
 	public boolean isNoInternetConnection() {
 		return mMessage == NO_INTERNET_CONNECTION;
 	}
 
 	public boolean isInternalIssue() {
 		return mMessage == INTERNAL_ISSUE;
 	}
 
 	public boolean isCustomIssue() {
 		return mMessage == CUSTOM_ERROR;
 	}
 
 	public boolean isError() {
 		return mMessage != SUCCESS;
 	}
 
 	int getMessage() {
 		return mMessage;
 	}
 
 	public long getStatusTimeMs() {
 		return mStatusTimeMs;
 	}
 
 	public JSONObject getMsgObjectOrNull() {
 		return mMessageObject;
 	}
 	
 	public String getStringOrNull() {
 		if (mMessageObject == null) {
 			return null;
 		}
 		try {
 			return mMessageObject.getString(JSON_DEFAULT_ERROR);
 		} catch (JSONException e) {
 			throw null;
 		}
 	}
 
 	public String getMsgObjectAsStringOrThrow() {
 		if (mMessageObject == null) {
 			throw new IllegalStateException("There is no message data");
 		}
 		try {
 			return mMessageObject.getString(JSON_DEFAULT_ERROR);
 		} catch (JSONException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	public Collection<String> getMsgObjectAsStringArrayOrNull() {
 		if (mMessageObject == null) {
 			return null;
 		}
 		try {
 			List<String> ret = new ArrayList<String>();
 			JSONArray jsonArray = mMessageObject.getJSONArray(JSON_DEFAULT_ERRORS);
 			for (int i = 0; i < jsonArray.length(); i++) {
 				ret.add(jsonArray.getString(i));
 			}
 			return ret;
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 
 	public Collection<String> getMsgObjectAsStringArrayOrThrow() {
 		if (mMessageObject == null) {
 			throw new IllegalStateException("There is no message data");
 		}
 		try {
 			List<String> ret = new ArrayList<String>();
 			JSONArray jsonArray = mMessageObject.getJSONArray(JSON_DEFAULT_ERRORS);
 			for (int i = 0; i < jsonArray.length(); i++) {
 				ret.add(jsonArray.getString(i));
 			}
 			return ret;
 		} catch (JSONException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	public long getLastDownloaded() {
 		return mLastDownloaded;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (o == null) {
 			return false;
 		}
 		if (!(o instanceof AUSyncerStatus)) {
 			return false;
 		}
 		AUSyncerStatus oo = (AUSyncerStatus) o;
 		return this.mMessage == oo.mMessage;
 	}
 	
 	private static String getMessageTypeString(int message) {
 		switch (message) {
 		case SUCCESS:
 			return "success";
 		case NO_INTERNET_CONNECTION:
 			return "no_internet_connection";
 		case INTERNAL_ISSUE:
 			return "internal issue";
 		case CUSTOM_ERROR:
 			return "custom error";
 		default:
 			return "wrong message type!!!";
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "Message: " + getMessageTypeString(mMessage) +
                 ", Date: " + new Date(mStatusTimeMs) +
                 ", MessageObject: " + (mMessageObject == null ? null : mMessageObject.toString()) +
                 ", LastDownloaded: " + mLastDownloaded;
 	}
 	
 }
