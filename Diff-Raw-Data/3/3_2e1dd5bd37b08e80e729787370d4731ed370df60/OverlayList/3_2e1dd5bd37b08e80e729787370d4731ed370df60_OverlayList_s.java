 package com.sp.norsesquare.froyo;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 
 public class OverlayList extends ItemizedOverlay<OverlayItem>
 {
 	private ArrayList<OverlayItem> oList = new ArrayList<OverlayItem>();
 	Context mContext;
 	
 	public OverlayList(Drawable defaultMarker) 
 	{
 		  super(boundCenterBottom(defaultMarker));
 	}
 	
 	public OverlayList(Drawable defaultMarker, Context context) 
 	{
 		  super(boundCenterBottom(defaultMarker));
 		  mContext = context;
 	}
 
 	public void addOverlay(OverlayItem overlay) {
 	    oList.add(overlay);
 	    populate();
 	}
 
 	protected OverlayItem createItem(int i) 
 	{
 		  return oList.get(i);
 	}
 	
 	@Override
 	public int size() {
 		// TODO Auto-generated method stub
 		return oList.size();
 	}
 	
 
 	
 	protected boolean onTap(int index) 
 	{
 		  OverlayItem item = oList.get(index);
 		  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
 		  dialog.setTitle(item.getTitle());
 		  dialog.setMessage(item.getSnippet());
 		  dialog.show();
 		  return true;
 	}
 }
