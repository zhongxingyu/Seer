 package edu.channel4.mm.db.android.model.request;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Parcel;
 import android.os.Parcelable;
 import edu.channel4.mm.db.android.R;
 import edu.channel4.mm.db.android.activity.GraphViewerActivity;
 import edu.channel4.mm.db.android.util.Keys;
 
 public class SessionOverTimeGraphRequest extends GraphRequest {
 
    // DO NOT CHANGE THIS STRING. APEX code relies on it!
    private final static String REQUEST_TYPE = "Line";
 
    public SessionOverTimeGraphRequest() {
    }
 
    public String toString() {
       return "Sessions over Time";
    }
 
    @Override
    public int getIconId() {
       return R.drawable.sessions_over_time;
    }
 
    @Override
    public Intent constructGraphRequestIntent(Context context) {
       Intent intent = new Intent(context, GraphViewerActivity.class);
       intent.putExtra(Keys.GRAPH_REQUEST_EXTRA, this);
 
       return intent;
    }
    
    /* Everything from here down is for implementing the Parcelable interface */
    @Override
    public int describeContents() {
       // Auto-generated method stub
       return 0;
    }
 
    @Override
    public void writeToParcel(Parcel dest, int flags) {
       // Note: Parcel data is read in a FIFO manner.
       dest.writeString(super.getTimeScope().name());
       dest.writeInt(super.getTimeRangeStart());
       dest.writeInt(super.getTimeRangeStop());
    }
 
    public static final Parcelable.Creator<SessionOverTimeGraphRequest> CREATOR = new Parcelable.Creator<SessionOverTimeGraphRequest>() {
       public SessionOverTimeGraphRequest createFromParcel(Parcel in) {
          return new SessionOverTimeGraphRequest(in);
       }
 
       @Override
       public SessionOverTimeGraphRequest[] newArray(int size) {
          return new SessionOverTimeGraphRequest[size];
       }
    };
 
    private SessionOverTimeGraphRequest(Parcel in) {
       // Note: Parcel data is read in a FIFO manner.
       super.setTimeScope(TimeScope.valueOf(in.readString()));
       super.setTimeRangeStart(in.readInt());
       super.setTimeRangeStop(in.readInt());
    }
 
    @Override
    public List<NameValuePair> getAdditionalUriParameters() {
       // create a list for the URL parameters to add
       List<NameValuePair> params = new ArrayList<NameValuePair>();
 
       params.add(new BasicNameValuePair(Keys.REQUEST_TYPE, REQUEST_TYPE));
       params.add(new BasicNameValuePair(Keys.TIME_SCOPE, timeScope.name()));
       params.add(new BasicNameValuePair(Keys.EVENT_NAME, ""));
       
       if (rangedTime) {
          params.add(new BasicNameValuePair(Keys.RANGE_START, "" + super.getTimeRangeStart()));
          params.add(new BasicNameValuePair(Keys.RANGE_STOP, "" + super.getTimeRangeStop()));
       }
       
       return params;
    }
 }
