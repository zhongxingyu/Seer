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
 
 package org.singularity.map;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
 public class NLBLibrariesOverlay extends ItemizedOverlay<OverlayItem> {
 	
 	private List<OverlayItem> items = new ArrayList<OverlayItem>();
 	private Context ctx;
 
 	public NLBLibrariesOverlay(Drawable defaultMarker, Context ctx) {
		super(boundCenterBottom(defaultMarker));
 		this.ctx = ctx;
 	}
 	
 	public NLBLibrariesOverlay(Drawable defaultMarker) {
 		super(boundCenterBottom(defaultMarker));
 	}
 
 	@Override
 	protected OverlayItem createItem(int i) {
 		return items.get(i);
 	}
 
 	@Override
 	public int size() {
 		// TODO Auto-generated method stub
 		return items.size();
 	}
 
 	public void addOverlay(OverlayItem overlay) {
 	    items.add(overlay);
 	    populate();
 	}
 	
 	@Override
 	protected boolean onTap(int i) {
 		OverlayItem item = items.get(i);
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		
		AlertDialog dialog = builder.setTitle(item.getTitle()).setMessage(item.getSnippet()).setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
			
		}).create();
		
 		dialog.show();
		
 		return true;
 	}
 	
 }
