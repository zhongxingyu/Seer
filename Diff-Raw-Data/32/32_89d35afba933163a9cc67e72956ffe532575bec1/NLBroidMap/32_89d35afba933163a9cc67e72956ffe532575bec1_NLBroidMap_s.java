 /**
  * Copyleft 2011 Singularity.Sg
 
 This file is part of NLBroid.
 
 NLBroid is free software: you can redistribute it and/or modify it under the terms of the 
 GNU General Public License as published by the Free Software Foundation, either version 3 
 of the License, or (at your option) any later version.
 NLBroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with NLBroid.
 If not, see http://www.gnu.org/licenses/.
  **/
 
 package org.singularity;
 
 import java.util.List;
 
 import org.singularity.bean.NLBBean;
 import org.singularity.bean.NLBBeanParcelable;
 import org.singularity.map.NLBLibrariesOverlay;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class NLBroidMap extends MapActivity {
 
 	@Override
 	protected void onCreate(Bundle arg0) {
 		
 		super.onCreate(arg0);
 		setContentView(R.layout.result_details);
 		
 		MapView view = (MapView) findViewById(R.id.map_results);
 		view.setBuiltInZoomControls(true);
 		
 		List<Overlay> mapOverlays = view.getOverlays();
 		Drawable nlbIcon = this.getResources().getDrawable(R.drawable.nlb_icon);
		NLBLibrariesOverlay overlayItems = new NLBLibrariesOverlay(nlbIcon);
 		
 		NLBBeanParcelable b =  (NLBBeanParcelable) getIntent().getParcelableExtra(NLBroidMain.ACTION_SHOW_MAP);
 		NLBBean bean = b.getBean();
 		int latitude = Double.valueOf(bean.getLatitude() * Math.pow(10,6)).intValue();
 		int longitude = Double.valueOf(bean.getLongitude() * Math.pow(10,6)).intValue();
 		
 		GeoPoint pt = new GeoPoint(latitude, longitude);
 		OverlayItem item = new OverlayItem(pt, bean.getListTitle(), bean.getDetails());
 		overlayItems.addOverlay(item);
 		
 		mapOverlays.add(overlayItems);
 		
 		TextView text = (TextView) this.findViewById(R.id.dialog_text);
 		text.setText(bean.getDetails());
 		
 		view.getController().setZoom(21);
 		view.getController().setCenter(pt);
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
