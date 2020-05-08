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
 
 public class AUSyncerStatus implements Parcelable {
 
 	private static final String JSON_DEFAULT_ERROR = "ERROR";
 	private static final String JSON_DEFAULT_ERRORS = "ERRORS";
 
 	private static final int SUCCESS = 0;
 	private static final int NEVER_DOWNLOADED = 1;
 
 	private static final int NO_INTERNET_CONNECTION = 2;
 	private static final int INTERNAL_ISSUE = 3;
 	private static final int CUSTOM_ERROR = 5;
 
 	private final int message;
 	private final long statusTimeMs;
 	private final JSONObject messageObject;
 
 	public static final Parcelable.Creator<AUSyncerStatus> CREATOR = new Parcelable.Creator<AUSyncerStatus>() {
 		public AUSyncerStatus createFromParcel(Parcel in) {
 			return new AUSyncerStatus(in);
 		}
 
 		public AUSyncerStatus[] newArray(int size) {
 			return new AUSyncerStatus[size];
 		}
 	};
 
 	private AUSyncerStatus(int message, JSONObject msgObject) {
 		this.message = message;
 		this.statusTimeMs = System.currentTimeMillis();
 		this.messageObject = msgObject;
 	}
 
 	AUSyncerStatus(int message, long statusTimeMs, JSONObject messageObject) {
 		this.message = message;
 		this.statusTimeMs = statusTimeMs;
 		this.messageObject = messageObject;
 	}
 
 	private AUSyncerStatus(Parcel in) {
 		this.message = in.readInt();
 		this.statusTimeMs = in.readLong();
 		String messageObjectStr = in.readString();
 
 		if (messageObjectStr == null) {
 			messageObject = null;
 		} else {
 			try {
 				messageObject = new JSONObject(messageObjectStr);
 			} catch (JSONException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(message);
 		dest.writeLong(statusTimeMs);
 		dest.writeString(messageObject == null ? null : messageObject
 				.toString());
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 
 	public static AUSyncerStatus statusSuccess() {
 		return new AUSyncerStatus(SUCCESS, null);
 	}
 
 	public static AUSyncerStatus statusNeverDownloaded() {
 		return new AUSyncerStatus(NEVER_DOWNLOADED, null);
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
 		return message == SUCCESS;
 	}
 
 	public boolean isNeverDownloaded() {
 		return message == NEVER_DOWNLOADED;
 	}
 
 	public boolean isNoInternetConnection() {
 		return message == NO_INTERNET_CONNECTION;
 	}
 
 	public boolean isInternalIssue() {
 		return message == INTERNAL_ISSUE;
 	}
 
 	public boolean isCustomIssue() {
 		return message == CUSTOM_ERROR;
 	}
 
 	public boolean isError() {
 		return message != SUCCESS && message != NEVER_DOWNLOADED;
 	}
 
 	int getMessage() {
 		return message;
 	}
 
 	public long getStatusTimeMs() {
 		return statusTimeMs;
 	}
 
 	public JSONObject getMsgObjectOrNull() {
 		return messageObject;
 	}
 	
 	public String getStringOrNull() {
 		try {
 			return messageObject.getString(JSON_DEFAULT_ERROR);
 		} catch (JSONException e) {
 			throw null;
 		}
 	}
 
 	public String getMsgObjectAsStringOrThrow() {
 		try {
 			return messageObject.getString(JSON_DEFAULT_ERROR);
 		} catch (JSONException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	public Collection<String> getMsgObjectAsStringArrayOrNull() {
 		try {
 			List<String> ret = new ArrayList<String>();
 			JSONArray jsonArray = messageObject.getJSONArray(JSON_DEFAULT_ERRORS);
 			for (int i = 0; i < jsonArray.length(); i++) {
 				ret.add(jsonArray.getString(i));
 			}
 			return ret;
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 
 	public Collection<String> getMsgObjectAsStringArrayOrThrow() {
 		try {
 			List<String> ret = new ArrayList<String>();
 			JSONArray jsonArray = messageObject.getJSONArray(JSON_DEFAULT_ERRORS);
 			for (int i = 0; i < jsonArray.length(); i++) {
 				ret.add(jsonArray.getString(i));
 			}
 			return ret;
 		} catch (JSONException e) {
 			throw new IllegalStateException(e);
 		}
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
 		return this.message == oo.message;
 	}
 	
 	private static String getMessageTypeString(int message) {
 		switch (message) {
 		case SUCCESS:
 			return "success";
 		case NEVER_DOWNLOADED:
 			return "never downloaded";
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

 		return new StringBuilder()
 				.append("Message: ")
 				.append(getMessageTypeString(message))
 				.append(", Date: ")
 				.append(new Date(statusTimeMs))
 				.append(", MessageObject: ")
 				.append(messageObject == null ? null : messageObject.toString())
 				.toString();
 	}
 	
 }
