 package de.unipotsdam.nexplorer.client.android.js;
 
 import android.app.Activity;
 
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class Marker extends UIElement {
 
 	public Map map;
 	private com.google.android.gms.maps.model.Marker inner = null;
 	protected LatLng position;
 	protected String title = "";
 	protected MarkerImage icon;
 	protected int zIndex;
 
 	public Marker(Activity context) {
 		super(context);
 		setData();
 	}
 
 	protected void setData() {
 	}
 
 	public void setPosition(final LatLng latlng) {
 		this.position = latlng;
 		runOnUIThread(new Runnable() {
 
 			@Override
 			public void run() {
 				if (inner != null) {
 					inner.setPosition(latlng.create());
 				}
 			}
 		});
 	}
 
 	public void setMap(final Map map2) {
 		this.map = map2;
 		runOnUIThread(new Runnable() {
 
 			@Override
 			public void run() {
 				if (map2 == null && inner != null) {
 					inner.remove();
 				} else if (map2 != null) {
 					inner = map2.getMap().addMarker(new MarkerOptions().position(position.create()).title(title).icon(icon.create()));
 				}
 			}
 		});
 	}
 
 	public void setTitle(String string) {
 		this.title = string;
 		runOnUIThread(new Runnable() {
 
 			@Override
 			public void run() {
 				if (inner != null) {
 					inner.setTitle(title);
 				}
 			}
 		});
 	}
 }
