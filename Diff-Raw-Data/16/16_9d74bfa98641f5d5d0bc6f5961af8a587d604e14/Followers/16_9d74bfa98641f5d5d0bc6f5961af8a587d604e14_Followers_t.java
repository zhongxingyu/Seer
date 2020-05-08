 package together.utils;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import together.activity.R;
 import together.activity.TogetherApp;
 
 import android.R.bool;
 import android.R.integer;
 import android.R.string;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
import android.util.Log;
 import android.widget.Toast;
 
 import com.baidu.mapapi.BMapManager;
 import com.baidu.mapapi.map.ItemizedOverlay;
 import com.baidu.mapapi.map.MapView;
 import com.baidu.mapapi.map.OverlayItem;
 import com.baidu.mapapi.map.PopupClickListener;
 import com.baidu.mapapi.map.PopupOverlay;
 import com.baidu.mapapi.search.MKPlanNode;
 import com.baidu.mapapi.search.MKSearch;
 import com.baidu.platform.comapi.basestruct.GeoPoint;
 
 public class Followers extends ItemizedOverlay<OverlayItem>{
 
 	private List<OverlayItem> overlaylist=new ArrayList<OverlayItem>();
 	//private List<PopupOverlay> popList;
 	//private int[] popflags;
 	private Context context;
 	private MapView mapView;
 	private static int selected=0;
 	private BMapManager mapManager;
 
 	public Followers(Context context,Drawable d,List<GeoPoint> list,MapView mapView,ArrayList<HashMap<String, Object>> allfollowers) throws IOException {
 		super(d);
 		this.context=context;
 		this.mapView=mapView;
 		mapManager=((TogetherApp)context.getApplicationContext()).getMapManager();
 		//this.popList=popList;
 		// TODO Auto-generated constructor stub
 		//popflags=new int[list.size()];
 		//popList=new ArrayList<PopupOverlay>(list.size());
 
 		for (int i=0;i<list.size()-1;i++){
 			HashMap<String, Object> follower=allfollowers.get(i);
 			OverlayItem item=new OverlayItem(list.get(i), null,"uid: "+(String)follower.get("uid"));
 			overlaylist.add(item);
 			//popflags[i]=0;
 			//popList.add(null);
             //popList.add(new PopUpOverlay(context, mapView,new PopUpOverlay.poplistener(context,item),item));      
 
 		}
 		overlaylist.add(new OverlayItem(list.get(list.size()-1), "event", "event"));
		Log.v("together", "geopoint: "+list.get(list.size()-1).toString()+","+list.get(list.size()-2));
 
 		overlaylist.get(list.size()-1).setMarker(context.getResources().getDrawable(R.drawable.arrow));
 
 		populate();
 
 
 	}
 	
 	protected boolean onTap(int index) {
 		super.onTap(index);
 		Toast.makeText(context, overlaylist.get(index).getSnippet(), Toast.LENGTH_SHORT);
 		return true;
 		
 	}
 	
 
 	@Override
 	protected OverlayItem createItem(int i) {
 		// TODO Auto-generated method stub
 		return overlaylist.get(i);
 	}
 
 	@Override
 	public int size() {
 		// TODO Auto-generated method stub
 		return overlaylist.size();
 	}
 
 
 
 }
 
