 /*
  *  Copyright (C) 2010 GSyC/LibreSoft, Universidad Rey Juan Carlos.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see http://www.gnu.org/licenses/. 
  *
  *  Author : Raúl Román López <rroman@gsyc.es>
  *
  */
 package com.libresoft.apps.ARviewer;
 
 import java.util.ArrayList;
 import java.util.concurrent.Semaphore;
 
 import com.libresoft.apps.ARviewer.Overlays.ARDinamicSummaryBox;
 import com.libresoft.apps.ARviewer.Overlays.ARStaticSummaryBox;
 import com.libresoft.apps.ARviewer.Overlays.ARSummaryBox;
 import com.libresoft.apps.ARviewer.Overlays.DrawRadar;
 import com.libresoft.apps.ARviewer.Overlays.DrawResource;
 import com.libresoft.apps.ARviewer.Overlays.DrawResourceSearcher;
 import com.libresoft.apps.ARviewer.Overlays.DrawResource.OnCenterListener;
 import com.libresoft.apps.ARviewer.Overlays.DrawResource.OnGetIconListener;
 import com.libresoft.apps.ARviewer.Overlays.DrawResource.OnShowIconListener;
 import com.libresoft.apps.ARviewer.Utils.LocationUtils;
 import com.libresoft.sdk.ARviewer.Types.Audio;
 import com.libresoft.sdk.ARviewer.Types.GeoNode;
 import com.libresoft.sdk.ARviewer.Types.Note;
 import com.libresoft.sdk.ARviewer.Types.Photo;
 import com.libresoft.sdk.ARviewer.Types.User;
 import com.libresoft.sdk.ARviewer.Types.Video;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.location.Location;
 import android.os.Handler;
 import android.os.Message;
import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.RelativeLayout;
 
 public class ARGeoNode implements ARNodeDrawingIF{
 	
 	private DrawResource drawn;
 	private boolean isloaded = false;
 	private GeoNode geoNode;
 	private float distance;
 	private float azimuth = 0;
 	private Point point = null;
 	private DrawResourceSearcher nodeSearcher;
 	
 	private ARBase mActivity;
 	
 	private static ARSummaryBox arSummaryBox = null;
 	private static boolean doCenterSummary = false;
 	private static Semaphore sem = new Semaphore(1);
 	private static DrawRadar mRadar = null;
 	private static boolean refreshIcon = false;
 	private static boolean isSearchSystem = false;
 	
 	private Thread th = null;
 	
 	private OnShowIconListener onShowIconListener = new OnShowIconListener() {
 		
 		@Override
 		public void onShow(boolean show) {
 			if(nodeSearcher != null) {
 				if(!isSearchSystem) {
 					if (nodeSearcher.isVisible())
 						nodeSearcher.setVisible(false);
 				}else if(nodeSearcher.isVisible() == show)
 					nodeSearcher.setVisible(!show);
 			}
 		}
 	};
 	
 	private OnGetIconListener onGetIconListener = new OnGetIconListener() {
 		@Override
 		public void onGetIcon() {
 			if(drawn != null){
 				drawn.setIcon(getIcon(refreshIcon));
 			}
 		}
 	};
 	
 	private DrawResource.OnBoxChangeListener onBoxChangeListener = new DrawResource.OnBoxChangeListener() {
 		
 		public void onChange(int left, int top, int right, int bottom) {
 			int num = mActivity.getResourcesList().indexOf(ARGeoNode.this);
 			if((arSummaryBox != null) && ARDinamicSummaryBox.class.isInstance(arSummaryBox))
 				((ARDinamicSummaryBox)arSummaryBox).translateSummaryBox(num, left, top, right, bottom);
 		}
 	};
 	
 	private DrawResource.OnIconClickListener onIconClickListener = new DrawResource.OnIconClickListener() {
 		public void onClick(boolean isOpen) {
 			setClicked(isOpen);
 		}
 	};
 	
 	private OnClickListener onLeftClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			ARviewer.GesturePrevious();
 		}
 	};
 	
 	private OnClickListener onRightClickListener = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			ARviewer.GestureNext();
 		}
 	};
 	
 	private OnCenterListener onCenterListener = new OnCenterListener() {
 		
 		@Override
 		public void onCenter() {
 			if(doCenterSummary)
 				if(sem.tryAcquire()){
 					setClicked(true);
 					sem.release();
 				}
 		}
 	};
 	
 
 	private Handler mHandler = new Handler(){
 		@Override
 		public void handleMessage(Message msg) {
 			th = null;
 			refreshIcon();
 		}
 	};
 	
 	public ARGeoNode(ARBase activity, GeoNode geoNode, RelativeLayout container) {
 		this.mActivity = activity;
 		this.geoNode = geoNode;
 		
 		point = new Point();
 		nodeSearcher = new DrawResourceSearcher(activity.getBaseContext(), container);
 	}
 	
 	public static void setRadar(DrawRadar mRadar){
 		ARGeoNode.mRadar = mRadar;
 	}
 	
 	public static void setDinamicSummary(Activity mActivity, RelativeLayout layer, boolean isDinamic){
 		removeSummaryBox();
 		
 		if(isDinamic){
 			if(!ARDinamicSummaryBox.class.isInstance(arSummaryBox))
 				arSummaryBox = new ARDinamicSummaryBox(layer);
 		}else{
 			if(!ARStaticSummaryBox.class.isInstance(arSummaryBox))
 				arSummaryBox = new ARStaticSummaryBox(mActivity, layer);
 		}
 	}
 	
 	public static void removeSummaryBox(){
 		if(ARDinamicSummaryBox.class.isInstance(arSummaryBox))
 			((ARDinamicSummaryBox)arSummaryBox).removeSummaryBox();
 		else if(ARStaticSummaryBox.class.isInstance(arSummaryBox))
 			((ARStaticSummaryBox)arSummaryBox).removeSummaryBox();
 	}
 	
 	public static void clearBox(){
 		removeSummaryBox();
 		arSummaryBox = null;
 	}
 	
 	public static void setResourcesList(ArrayList<ARGeoNode> nodes_list){
 		if(arSummaryBox != null)
 			arSummaryBox.setNodesList(nodes_list);
 	}
 	
 	public static int getNodeClicked(){
 		if(arSummaryBox != null)
 			return arSummaryBox.getNodeClicked();
 		return -1;
 	}
 	
 	public static void removeNode(ARLayerManager layer, ArrayList<ARGeoNode> nodes_list){
 		if((layer == null) || (nodes_list == null))
 				return;
 		int num_node = arSummaryBox.getNodeClicked();
 		if(num_node == -1)
 			return;
 		
 		clearClicked(nodes_list);
 		
 		ARGeoNode node = nodes_list.get(num_node);
 		nodes_list.remove(node);
 		layer.removeResourceElement(node.getDrawn());
 	}
 	
 	public static void clearClicked(ArrayList<ARGeoNode> nodes_list){
 		if(nodes_list == null)
 			return;
 		if(arSummaryBox == null)
 			return;
 		
 		int num_node = arSummaryBox.getNodeClicked();
 		if(num_node == -1)
 			return;
 		
 		nodes_list.get(num_node).setClicked(false);
 	}
 	
 	public static void setCenterSummary(boolean doCenterSummary){
 		ARGeoNode.doCenterSummary = doCenterSummary;
 	}
 	
 	public static void setRefreshIcon(ArrayList<ARGeoNode> list_nodes, boolean refreshIcon){
		try{
			ARGeoNode.refreshIcon = refreshIcon;
			if(list_nodes != null){
				int max = list_nodes.size();
				ARGeoNode node;
				for(int i = max-1; i > -1; i--){
					node = list_nodes.get(i);
					if(Photo.class.isInstance(node.getGeoNode()))
						node.getDrawn().setIcon(null);
					else if(Video.class.isInstance(node.getGeoNode()))
						node.getDrawn().setIcon(null);
				}

 			}
		}catch(Exception e){
			Log.e("ARGeoNode", "", e);
 		}
 	}
 	
 	public static void activeSearchSystem(boolean active){
 		isSearchSystem = active;
 	}
 	
 	public static synchronized void refreshSummaryBoxImage(int num_node, Bitmap image){
 		if(arSummaryBox != null)
 			arSummaryBox.refreshPhoto(num_node, image);
 	}
 
 	public GeoNode getGeoNode(){
 		return geoNode;
 	}
 	
 	public DrawResource getDrawn(){
 		return drawn;
 	}
 	
 	public Point getPoint(){
 		return point;
 	}
 	
 	public boolean isLoaded(){
 		return isloaded;
 	}
 	
 	public void setLoaded(boolean isloaded){
 		this.isloaded = isloaded;
 	}
 	
 	private void removeLastClicked(){
 		/* Set the last clicked node icon to normal state */
 		int node_clicked = mActivity.getResourcesList().indexOf(this);
 		
 		int last_node_clicked = -1;
 		last_node_clicked = arSummaryBox.getNodeClicked();
 		
 		if((last_node_clicked > -1) && (node_clicked != last_node_clicked))
 			mActivity.getResourcesList().get(last_node_clicked).setDrawnClicked(false);
 	}
 	
 	public void setDrawnClicked(boolean isOpen){
 		/* Switch the icon of this node between normal state and clicked state */
 		int node_clicked = mActivity.getResourcesList().indexOf(this);
 		
 		drawn.setClicked(isOpen);
 		if(isOpen){
 			if(ARDinamicSummaryBox.class.isInstance(arSummaryBox))
 				drawn.setOnBoxChangeListener(onBoxChangeListener);
 			if(mRadar != null)
 				mRadar.setPointClicked(node_clicked);
 		}else{
 			if(ARDinamicSummaryBox.class.isInstance(arSummaryBox))
 				drawn.unregisterBoxListener();
 			if(mRadar != null)
 				mRadar.setPointClicked(-1);
 		}
 		
 		setOnShowIconListener(isOpen);
 	}
 	
 	public void setClicked(boolean isOpen) {
 		/* Set the last node clicked into normal state */
 		removeLastClicked();
 		
 		/* Turn this node into clicked state */
 		setDrawnClicked(isOpen);
 		
 		/* Show the summary box for this node */
 		setSummaryBox(isOpen);
 	}
 	
 	public void setOnShowIconListener(boolean show){
 		if(drawn != null){
 			if(show)
 				drawn.setOnShowIconListener(onShowIconListener);
 			else{
 				drawn.setOnShowIconListener(null);
 				if(nodeSearcher != null)
 					nodeSearcher.setVisible(false);
 			}
 		}
 	}
 	
 	public void setDrawnValues(float azimuth, float abs_azimuth, float elevation, float distance){
 		drawn.setValues(azimuth, elevation);
 		this.distance = distance;
 		this.azimuth = abs_azimuth;
 		
 		point.set((int)(LocationUtils.distanceLog((double)distance) * 100 * Math.sin(Math.toRadians(abs_azimuth))), 
 				(int)(LocationUtils.distanceLog((double)distance) * -100 * Math.cos(Math.toRadians(abs_azimuth))));
 		
 		if(isSearchSystem && (nodeSearcher != null))
 			nodeSearcher.setValues(azimuth, elevation);
 	}
 	
 	public float getAzimuth(){
 		return azimuth;
 	}
 	
 	public float getDistance(){
 		return distance;
 	}
 	
 	public void createDrawn(Context context){
 		this.drawn = new DrawResource(context, getTitle(), onGetIconListener);
 		this.drawn.setOnIconClickListener(onIconClickListener);
 		drawn.setOnCenterListener(onCenterListener);
 	}
 	
 	public void bringDrawnToFront(){
 		drawn.bringToFront();
 	}
 	
 	public void drawSummary(boolean draw){
 		drawn.setClicked(draw);
 	}
 	
 	public void invalidateDrawn(){
 		drawn.invalidate();
 	}
 	
 	public float distanceToResource(float[] source){
 		float d;
 		
 		Location sLocation = new Location ("gps");
 		sLocation.setLatitude(source[0]);
 		sLocation.setLongitude(source[1]);
 		
 		d = sLocation.distanceTo(geoNode.getLocation());
 		
 		return d;
 	}
 	
 	public float azimuthToResource(float source[]){
 		float azimuth, resource[] = new float[2];
 		
 		resource[0] = (float) geoNode.getLocation().getLatitude();
 		resource[1] = (float) geoNode.getLocation().getLongitude();
 		
 		azimuth = LocationUtils.calculateResourceAzimuth(source, resource);
 		
 		return azimuth;
 	}
 	
 	public float rollToResource(float distance){
 		return LocationUtils.calculateRoll(distance, (float) geoNode.getLocation().getAltitude());
 	}
 	
 	public void setSummaryBox(boolean setBox){
 		if(arSummaryBox == null)
 			return;
 		
 		if(!setBox){
 			removeSummaryBox();
 			return;
 		}
 		
 		int num = mActivity.getResourcesList().indexOf(this);
 		
 		if(ARDinamicSummaryBox.class.isInstance(arSummaryBox)){
 
 			((ARDinamicSummaryBox)arSummaryBox).drawSummaryBox(
 					mActivity, 
 					num, 
 					onLeftClickListener,
 					onRightClickListener);
 		}else if(ARStaticSummaryBox.class.isInstance(arSummaryBox))
 			((ARStaticSummaryBox)arSummaryBox).drawSummaryBox(mActivity.getBaseContext(), num);
 		
 	}
 	
 	public String getTitle(){
 		String title = "";
 		if(Photo.class.isInstance(geoNode)){
 			Photo node = (Photo) geoNode;
 			title = node.getName();
 		}else if(Note.class.isInstance(geoNode)){
 			Note node = (Note) geoNode;
 			title = node.getTitle();
 		}else if(Audio.class.isInstance(geoNode)){
 			Audio node = (Audio) geoNode;
 			title = node.getName();
 		}else if(Video.class.isInstance(geoNode)){
 			Video node = (Video) geoNode;
 			title = node.getName();
 		}else if(User.class.isInstance(geoNode)){
 			User node = (User)geoNode;
 			title = node.getUsername();
 		}
 		return title;
 	}
 	
 	public String getDescription(){
 		String description = "";
 		if(Photo.class.isInstance(geoNode)){
 			Photo node = (Photo) geoNode;
 			description = node.getDescription();
 		}else if(Note.class.isInstance(geoNode)){
 			Note node = (Note) geoNode;
 			description = node.getBody();
 		}else if(Audio.class.isInstance(geoNode)){
 			Audio node = (Audio) geoNode;
 			description = node.getDescription();
 		}else if(Video.class.isInstance(geoNode)){
 			Video node = (Video) geoNode;
 			description = node.getDescription();
 		}else if(User.class.isInstance(geoNode)){
 			User node = (User)geoNode;
 			description = node.getName() + "\n\n" + node.getState();
 		}
 		return description;
 	}
 	
 	public Bitmap getIcon(boolean requestImage){
 		Bitmap image = null;
 		if(Photo.class.isInstance(geoNode)){
 			final Photo node = (Photo) geoNode;
 			if(requestImage && (node.isBitmapPhotoThumb()))
 				image = node.getBitmapPhotoThumb();
 			else{
 				image = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.camera);
 				if(requestImage && (th == null)){
 					th = new Thread(){
 						public void run(){
 							if(node.getBitmapPhotoThumb() != null)
 								mHandler.sendEmptyMessage(0);
 						}
 					};
 					th.start();
 				}
 			}
 		}else if(Note.class.isInstance(geoNode)){
 			image = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.paper_pencil_70);
 		}else if(Audio.class.isInstance(geoNode)){
 			image = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.audio_70);
 		}else if(Video.class.isInstance(geoNode)){
 			final Video node = (Video) geoNode;
 			if(requestImage && (node.isBitmapImageThumbLoaded()))
 				image = node.getBitmapImageThumb();
 			else{
 				image = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.media);
 				if(requestImage && (th == null)){
 					th = new Thread(){
 						public void run(){
 							if(node.getBitmapImageThumb() != null)
 								mHandler.sendEmptyMessage(0);
 						}
 					};
 					th.start();
 				}
 			}
 		}else if(User.class.isInstance(geoNode)){
 			final User node = (User)geoNode;
 			if(node.getAvatarId() == 0)
 				image = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.user_70);
 			else{
 				if(node.isBitmapAvatarThumb())
 					image = node.getAvatarBitmapThumb();
 				else{
 					image = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.user_70);
 					if((th == null)){
 						th = new Thread(){
 							public void run(){
 								if(node.getAvatarBitmapThumb() != null)
 									mHandler.sendEmptyMessage(0);
 							}
 						};
 						th.start();
 					}
 				}
 			}
 		}
 		return image;
 	}
 	
 	public boolean isMedia(){
 		boolean is_media = false;
 		if(Audio.class.isInstance(geoNode))
 			is_media = true;
 		return is_media;
 	}
 	
 	public OnClickListener getRemoveNodeListener(final Activity mActivity){
 		
 		OnClickListener listener = new OnClickListener() {
 			public void onClick(View v) {
 				mActivity.showDialog(ARSummaryBox.DIALOG_SURE);
 			}
 		};		
 		return listener;
 	}
 	
 	private void refreshIcon(){
 		Bitmap icon = null;
 		if(Photo.class.isInstance(geoNode))
 			icon = ((Photo)geoNode).getBitmapPhotoThumb();
 		if(Video.class.isInstance(geoNode))
 			icon = ((Video)geoNode).getBitmapImageThumb();
 		if(User.class.isInstance(geoNode))
 			icon = ((User)geoNode).getAvatarBitmapThumb();
 		
 		if((refreshIcon) && (drawn != null))
 			drawn.setIcon(icon);
 		
 		int num = mActivity.getResourcesList().indexOf(ARGeoNode.this);
 		refreshSummaryBoxImage(num, icon);
 	}
 	
 }
