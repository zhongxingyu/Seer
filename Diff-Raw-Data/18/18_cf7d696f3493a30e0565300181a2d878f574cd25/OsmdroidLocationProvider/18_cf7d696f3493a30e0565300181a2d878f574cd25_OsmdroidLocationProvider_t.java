 /*
  * Copyright (c) 2014. See AUTHORS file.
  *
  * This file is part of SleepFighter.
  *
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  */
 package se.toxbee.sleepfighter.gps.osmdroid;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.location.Location;
 import android.os.Build;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout.LayoutParams;
 
 import org.osmdroid.api.IGeoPoint;
 import org.osmdroid.api.IMapController;
 import org.osmdroid.bonuspack.overlays.FolderOverlay;
 import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
 import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
 import org.osmdroid.bonuspack.overlays.Polygon;
 import org.osmdroid.util.BoundingBoxE6;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.Overlay;
 import org.osmdroid.views.overlay.OverlayItem;
 
 import java.util.ArrayList;
 import java.util.List;
 
import se.toxbee.fimpl.annotation.ProvidedImplementation;
 import se.toxbee.sleepfighter.gps.gui.LocationGUIProvider;
 import se.toxbee.sleepfighter.gps.gui.LocationGUIReceiver;
 import se.toxbee.sleepfighter.gps.osmdroid.DraggableMarkerList.MarkerDragListener;
 import se.toxbee.sleepfighter.gps.osmdroid.Hotspot.OnHotspotTap;
 import se.toxbee.sleepfighter.model.gps.GPSLatLng;
 
 /**
  * GoogleLocationProvider uses Google Maps API v2<br/>
  * to let the user manipulate the latlng points of a location filter area.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Jan 8, 2014
  */
@ProvidedImplementation(of = LocationGUIProvider.class)
 public class OsmdroidLocationProvider implements LocationGUIProvider, MapEventsReceiver, MarkerDragListener<OverlayItem> {
 	public static final int STROKE_WIDTH = 2;
 
 	private OsmdroidMapView mapView;
 	private DraggableMarkerList<OverlayItem> markers;
 	private FolderOverlay polygons;
 
 	private android.os.Handler handler;
 
	private LocationGUIReceiver receiver;

	@Override
	public void bind( LocationGUIReceiver receiver ) {
		this.receiver = receiver;
	}

 	@Override
 	public boolean isAvailable( boolean allowError ) {
 		return true;
 	}
 
 	@Override
 	public void initMap( ViewGroup viewContainer ) {
 		// Init map view.
 		Context ctx = viewContainer.getContext();
 		this.mapView = new OsmdroidMapView( ctx, 256 );
 		LayoutParams layoutParams = new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );
 		viewContainer.addView( this.mapView, layoutParams );
 
 		// Init handler.
 		this.handler = new android.os.Handler();
 
 		this.configMap();
 
 		this.receiver.onMapReady();
 	}
 
 	private void configMap() {
 		Context ctx = this.ctx();
 
 		this.setHardwareAccelerationOff();
 
 		this.mapView.setBuiltInZoomControls( true );
 		this.mapView.setMultiTouchControls( true );
 
 		List<Overlay> over = this.mapView.getOverlays();
 
 		// Intercept click/tap events on map.
 		over.add( new MapEventsOverlay( ctx, this ) );
 
 		// Setup polygons holder.
 		this.polygons = new FolderOverlay( ctx );
 		over.add( this.polygons );
 
 		// Setup markers.
 		this.markers = new DraggableMarkerList<>( ctx, new ArrayList<OverlayItem>() );
 		this.markers.setDragListener( this );
 		over.add( this.markers );
 
 		// My location button.
 		over.add( this.makeMyLocationOverlay( ctx ) );
 	}
 
 	private Overlay makeMyLocationOverlay( Context ctx ) {
 		MyLocationButton mlb = new MyLocationButton( ctx );
 		mlb.setListener( new OnHotspotTap() {
 			@Override
 			public boolean onHotspotTap( MotionEvent e, MapView mapView, Hotspot hotspot ) {
 				return true;
 			}
 		} );
 
 		return mlb;
 	}
 
 	private Context ctx() {
 		return this.mapView.getContext();
 	}
 
 	@TargetApi( Build.VERSION_CODES.HONEYCOMB )
 	private void setHardwareAccelerationOff() {
 		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
 			this.mapView.setLayerType( View.LAYER_TYPE_SOFTWARE, null );
 		}
 	}
 
 	@Override
 	public boolean singleTapUpHelper( IGeoPoint p ) {
 		this.receiver.onMapClick( this.convertLocation( p ) );
 		return false;
 	}
 
 	@Override
 	public boolean longPressHelper( IGeoPoint p ) {
 		return false;
 	}
 
 	private GeoPoint convertLocation( Location loc ) {
 		return new GeoPoint( loc.getLatitude(), loc.getLongitude() );
 	}
 
 	private GeoPoint convertLocation( GPSLatLng loc ) {
 		return new GeoPoint( loc.getLat(), loc.getLng() );
 	}
 
 	private GPSLatLng convertLocation( IGeoPoint loc ) {
 		return new GPSLatLng( loc.getLatitude(), loc.getLongitude() );
 	}
 
 	private ArrayList<GeoPoint> getPoints() {
 		ArrayList<GeoPoint> pts = new ArrayList<>();
 		for ( OverlayItem item : this.markers.getItems() ) {
 			pts.add( item.getPoint() );
 		}
 		return pts;
 	}
 
 	@Override
 	public boolean satisfiesPolygon() {
 		return this.markers.size() >= 3;
 	}
 
 	public boolean hasPoints() {
 		return this.markers.size() > 0;
 	}
 
 	public void addPoint( GPSLatLng loc ) {
 		this.markers.addItem( new OverlayItem( null, null, this.convertLocation( loc ) ) );
 	}
 
 	public void undoLastPoint() {
 		this.markers.removeLast();
 	}
 
 	public void clearPoints() {
 		this.markers.removeAllItems();
 	}
 
 	public void updateGuiPolygon() {
 		this.polygons.getItems().clear();
 
 		if ( this.satisfiesPolygon() ) {
 			Polygon poly = new Polygon( ctx() );
 			poly.setFillColor( this.receiver.getPolygonFillColor() );
 			poly.setStrokeColor( this.receiver.getPolygonStrokeColor() );
 			poly.setStrokeWidth( STROKE_WIDTH );
 			poly.setPoints( this.getPoints() );
 
 			this.polygons.add( poly );
 		}
 
 		this.mapView.invalidate();
 	}
 
 	@Override
 	public void initCameraToPolygon() {
 		this.handler.postDelayed( new Runnable() {
 			@Override
 			public void run() {
 				moveCameraToPolygon( false );
 				moveCameraToPolygon( true );
 			}
 		}, 100 );
 	}
 
 	public void moveCamera( Location loc, boolean animate ) {
 		IGeoPoint point = this.convertLocation( loc );
 		IMapController ctrl = this.mapView.getController();
 		ctrl.setZoom( (int) this.receiver.getZoomFactor() );
 
 		if ( animate ) {
 			ctrl.animateTo( point );
 		} else {
 			ctrl.setCenter( point );
 		}
 
 		this.mapView.invalidate();
 	}
 
 	@Override
 	public void moveCameraToPolygon( boolean animate ) {
 		if ( this.markers.size() > 0 ) {
 			this.mapView.zoomToBoundingBox( BoundingBoxE6.fromGeoPoints( this.getPoints() ), animate );
 		}
 	}
 
 	@Override
 	public void onMarkerDrag( OverlayItem item ) {
 		this.receiver.onMarkerDrag( this.convertLocation( item.getPoint() ) );
 	}
 
 	@Override
 	public void onMarkerDragEnd( OverlayItem item ) {
 		this.receiver.onMarkerDragEnd( this.markers.getItems().indexOf( item ), this.convertLocation( item.getPoint() ) );
 	}
 
 	@Override
 	public void onMarkerDragStart( OverlayItem item ) {
 		this.receiver.onMarkerDragStart( this.convertLocation( item.getPoint() ) );
 	}
 }
