 package carnero.netmap.model;
 
 import carnero.netmap.common.BtsLocationDownloader;
 import carnero.netmap.listener.OnLocationObtainedListener;
 import com.google.android.gms.maps.model.LatLng;
 
 public class Bts {
 
 	public int lac;
 	public int cid;
 	public String operator;
 	public int type;
 	public LatLng location;
 	//
 	private boolean mLoading = false;
 
 	public Bts(int lac, int cid, int type) {
 		this.lac = lac;
 		this.cid = cid;
 		this.type = type;
 	}
 
 	public static String getId(Bts bts) {
 		return getId(bts.lac, bts.cid);
 	}
 
 	public static String getId(int lac, int cid) {
 		final StringBuilder sb = new StringBuilder();
 		sb.append(Integer.toString(lac));
 		sb.append(":");
 		sb.append(Integer.toString(cid));
 
 		return sb.toString();
 	}
 
 	public void getLocation(OnLocationObtainedListener listener) {
 		if (location != null) {
 			if (listener != null) {
 				listener.onLocationObtained(this);
 			}
 
 			return;
 		}
 
 		if (!mLoading) {
 			new BtsLocationDownloader(this, listener).execute();
 		}
 	}
 
 	public void setLoading() {
 		mLoading = true;
 	}
 
 	public void clearLoading() {
 		mLoading = false;
 	}
 }
