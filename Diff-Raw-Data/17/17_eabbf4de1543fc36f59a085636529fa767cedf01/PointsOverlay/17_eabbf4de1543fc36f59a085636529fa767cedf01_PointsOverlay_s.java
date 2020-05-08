 package com.ghostrun.overlays;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.view.GestureDetector;
 
 import com.ghostrun.activity.MapEditor;
 import com.ghostrun.driving.DrivingDirections;
 import com.ghostrun.driving.DrivingDirections.Mode;
 import com.ghostrun.driving.DrivingDirectionsFactory;
 import com.ghostrun.driving.Node;
 import com.ghostrun.driving.NodePair;
 import com.ghostrun.driving.Route;
 import com.ghostrun.driving.impl.RouteImpl;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.google.android.maps.Projection;
 import com.google.gson.Gson;
 
 public class PointsOverlay extends ItemizedOverlay<OverlayItem> {
 	int node_id = 0;
 	int selected = -1;
 	HashMap<NodePair, Route> routesMap;
 	List<Node> nodes;
 	GestureDetector gestureDetector;
 	MapView mapView;
 	Paint mPaint;
 	boolean select;
 	
 	Drawable marker, selectedMarker;
 	MapEditor editor;
 	
 	public PointsOverlay(Drawable marker, Drawable selectedMarker, MapEditor editor, List<Node> nodes) {
 		this(marker, selectedMarker, editor);
 		
 		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
 		for (Node n : nodes) {
 			this.nodes.add(n);
 			nodeMap.put(n.id, n);
 		}
 		
		/*
 		int processedNodes = 0;
 		for (Node n1 : nodes) {
 			final Node f1 = n1; 
 			for (Node n2 : n1.neighbors) {
 				final Node f2 = n2;
 				if (processedNodes % 20 == 0) {
 					try {
 						Thread.sleep(10000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				processedNodes ++;
 				DrivingDirections directions = DrivingDirectionsFactory.createDrivingDirections();
 				directions.driveTo(n1.latlng, n2.latlng, 
 						DrivingDirections.Mode.DRIVING, 
 						new DrivingDirections.IDirectionsListener() {
 							public void onDirectionsAvailable (Route route, Mode mode) {
 								routesMap.put(new NodePair(f1.id, f2.id), route);
 							}
 					
 							public void onDirectionsNotAvailable () {
 					
 							}
 					});
 			}
		}
		*/		
 		this.setLastFocusedIndex(-1);
 		this.selected = this.nodes.size() -1;
 		this.mapView.invalidate();
 		this.populate();
 	}
 	
 	public PointsOverlay(Drawable marker, Drawable selectedMarker, MapEditor editor) {
 		super(boundCenterBottom(marker));
 		
 		this.editor = editor;
 		this.select = true;
 		this.marker = marker;
 		this.selectedMarker = selectedMarker;
 		this.mapView = editor.mapView;
 		this.nodes = new ArrayList<Node>();
 		this.routesMap = new HashMap<NodePair, Route>();
 		
         this.mPaint = new Paint();
         this.mPaint.setDither(true);
         this.mPaint.setColor(Color.RED);
         this.mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
         this.mPaint.setStrokeJoin(Paint.Join.ROUND);
         this.mPaint.setStrokeCap(Paint.Cap.ROUND);
         this.mPaint.setStrokeWidth(2);
                 
 		this.populate();
 	}
 	
 	public String getJson() {
 		Map<Object, Object> m = new HashMap<Object, Object>();
 		@SuppressWarnings("unchecked")
                 Map<Object, Object>[] jsonNodes = new HashMap[this.nodes.size()];
 		for (int i = 0; i < jsonNodes.length; i ++) {
 			jsonNodes[i] = this.nodes.get(i).toJson();
 		}
 		m.put("nodes", jsonNodes);
 		
 		Map<Object, Object> jsonRoutes = new HashMap<Object, Object>();
 		Set<Entry<NodePair, Route>> entries = this.routesMap.entrySet();
 		for (Entry<NodePair, Route> entry : entries) {
 			jsonRoutes.put(entry.getKey().id1 + " " + entry.getKey().id2, 
 					((RouteImpl)entry.getValue()).toJson());
 		}
 		m.put("routes", jsonRoutes);
 		Gson gson = new Gson();
 		String json = gson.toJson(m);
 		System.out.println(json);
 		return json;
 	}
 
 	protected void assertTrue(boolean t, String s) {
 		if (!t)
 			throw new RuntimeException (s);
 	}
 	
 	public void addUnsafeMarker(GeoPoint p) {
 		DrivingDirections directions = DrivingDirectionsFactory.createDrivingDirections();
 		directions.driveTo(p, p, DrivingDirections.Mode.DRIVING, 
 				new DrivingDirections.IDirectionsListener() {
 					public void onDirectionsAvailable (Route route, Mode mode) {
 						List<GeoPoint> points = route.getGeoPoints();
 						try {
 							addMarker(points.get(points.size()-1));
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 			
 					public void onDirectionsNotAvailable () {
 			
 					}
 			});
 	}
 	
 	public synchronized void select() {
 		select = true;
 	}
 	
 	public synchronized void remove() {
 		select = false;
 	}
 	
 	public void addMarker(GeoPoint p) throws Exception {
 		assertTrue(selected != -1 || nodes.size() == 0, "No points selected");
 		
 		final Node endPt = new Node(p, node_id++);
 
 		if (nodes.size() > 0) {
 			final Node startPt = nodes.get(selected);
 			startPt.addNeighbor(endPt);
 			endPt.addNeighbor(startPt);
 			
 			DrivingDirections directions = DrivingDirectionsFactory.createDrivingDirections();
 			directions.driveTo(startPt.latlng, endPt.latlng, 
 					DrivingDirections.Mode.DRIVING, 
 					new DrivingDirections.IDirectionsListener() {
 						public void onDirectionsAvailable (Route route, Mode mode) {
 							routesMap.put(new NodePair(startPt.id, endPt.id), route);
 						}
 				
 						public void onDirectionsNotAvailable () {
 				
 						}
 				});
 		}
 		this.setLastFocusedIndex(-1);
 		this.nodes.add(endPt);
 		this.selected = this.nodes.size() -1;
 		this.mapView.invalidate();
 		this.populate();
 	}
 	@Override
 	protected OverlayItem createItem(int i) {
 		// TODO Auto-generated method stub
 		
 		OverlayItem item = new OverlayItem(this.nodes.get(i).latlng, "", "");
 		
 		if (this.selected == i)
 			item.setMarker(boundCenterBottom(this.selectedMarker));
 		
 		return item;
 	}
 
 	@Override
 	public int size() {
 		// TODO Auto-generated method stub
 		//System.out.println("Size: " + this.nodes.size());
 		return this.nodes.size();
 	}
 	
 	@Override
 	public void draw(Canvas canvas, MapView mapv, boolean shadow){
         super.draw(canvas, mapv, shadow);
         
         Collection<Route> routes = routesMap.values();
         Projection projection = mapv.getProjection();
         for (Route route : routes) {
         	List<GeoPoint> geoPoints = route.getGeoPoints();
         	GeoPoint pt1 = geoPoints.get(0);
         	Point p1 = new Point();
         	projection.toPixels(pt1, p1);
         	
         	for (int i = 1; i < geoPoints.size(); i++) {
         		GeoPoint pt2 = geoPoints.get(i); 
                 Point p2 = new Point();
 
                 Path path = new Path();
                 projection.toPixels(pt2, p2);
 
                 path.moveTo(p2.x, p2.y);
                 path.lineTo(p1.x, p1.y);
         		canvas.drawPath(path, this.mPaint);
         		
         		pt1 = pt2;
         		p1 = p2;
         	}
         }
         //this.mapView.invalidate();
     }
 	
 	public boolean onTap(GeoPoint p, MapView mapView) { 
 		try {
 			boolean tapped = super.onTap(p, mapView);
 			if (tapped){            
 				//do what you want to do when you hit an item           
 		    }           
 		    else{
 		        //do what you want to do when you DONT hit an item
 		    	addUnsafeMarker(p);
 		    }      
 		}
 		catch (Exception e){
 			e.printStackTrace();
 		}
 	    return true;
 	}
 	
 	@Override
 	public boolean onTap(int index) {
 		System.out.println("tapped: " + index);
 		if (this.select) {
 			if (index == this.selected)
 				this.selected = -1;
 			else
 				this.selected = index;
 			System.out.println("selecting: " + this.selected);
 		} else {
 			this.selected = -1;
 			Node n = this.nodes.get(index);
 			for (Node neighbor : n.neighbors) {
 				assertTrue(routesMap.remove(new NodePair(n.id, neighbor.id)) != null, 
 					"route map should contain key");
 				neighbor.removeNeighbor(n);
 			}
 			this.setLastFocusedIndex(-1);
 			this.nodes.remove(index);
 			this.populate();
 		}
 		this.populate();
 		this.mapView.invalidate();
 		return true;
 	}
 }
