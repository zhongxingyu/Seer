 package com.aerodynelabs.map;
 
 //XXX zoomIn/Out to mouse location
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.util.Collection;
 import java.util.Hashtable;
 
 import javax.swing.JPanel;
 
 /**
  * A tile based slippy map.
  * 
  * @author Ethan Harstad
  *
  */
 @SuppressWarnings("serial")
 public class MapPanel extends JPanel implements MouseListener, MouseMotionListener{
 	
 	private static final String attribution1 = 
 			"Tiles courtesy of MapQuest.com\n";
 	private static final String attribution2 = 
 			"Map data \u00a9 OpenStreetMap contributors, CC-BY-SA";
 	
 	private double lat, lon;
 	private int zoom;
 	private Point mouseDown;
 	protected Hashtable<String, MapOverlay> overlays;
 	
 	private TileServer server;
 	
 	/**
 	 * A map centered on Ames, IA using Mapquest tiles.
 	 */
 	public MapPanel() {
 		this(42.01, -93.57, 11, "http://otile1.mqcdn.com/tiles/1.0.0/osm/", 18);
 	}
 	
 	/**
 	 * A map centered on the given coordinates with the given zoom using Mapquest tiles.
 	 * @param lat
 	 * @param lon
 	 * @param zoom
 	 */
 	public MapPanel(double lat, double lon, int zoom) {
 		this(lat, lon, zoom, "http://otile1.mqcdn.com/tiles/1.0.0/osm/", 18);
 	}
 	
 	/**
 	 * A map centered on the given coordinates with the given zoom using the given tileset.
 	 * @param lat
 	 * @param lon
 	 * @param zoom
 	 * @param url
 	 */
 	public MapPanel(double lat, double lon, int zoom, String url) {
 		this(lat, lon, zoom, url, 17);
 	}
 	
 	/**
 	 * A map centered on the given coordinates with the given zoom and max zoom using the given tileset.
 	 * @param lat
 	 * @param lon
 	 * @param zoom
 	 * @param url
 	 * @param maxZoom
 	 */
 	public MapPanel(double lat, double lon, int zoom, String url, int maxZoom) {
 		super.setPreferredSize(new Dimension(640, 480));
 		server = new TileServer(url, maxZoom, this);
 		overlays = new Hashtable<String, MapOverlay>();
 		setZoom(zoom);
 		setCenter(lat, lon);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 	
 	/**
 	 * Add an overlay to the map.
 	 * @param name
 	 * @param overlay
 	 */
 	public void addOverlay(String name, MapOverlay overlay) {
 		overlays.put(name, overlay);
 	}
 	
 	/**
 	 * Add an overlay to the map.
 	 * @param overlay
 	 */
 	public void addOverlay(MapOverlay overlay) {
 		overlays.put(overlay.getName(), overlay);
 	}
 	
 	/**
 	 * Set the zoom level of the map.
 	 * @param zoom
 	 */
 	protected void setZoom(int zoom) {
 		this.zoom = zoom;
 		repaint();
 	}
 	
 	/**
 	 * Get the zoom level of the map.
 	 * @return
 	 */
 	public int getZoom() {
 		return zoom;
 	}
 	
 	/**
 	 * Zoom in
 	 */
 	protected void zoomIn() {
 		setZoom(zoom + 1);
 	}
 	
 	/**
 	 * Zoom out
 	 */
 	protected void zoomOut() {
 		setZoom(zoom - 1);
 	}
 	
 	/**
 	 * Set the center coordinates of the map.
 	 * @param lat
 	 * @param lon
 	 */
 	public void setCenter(double lat, double lon) {
 		this.lat = lat;
 		this.lon = lon;
 		repaint();
 	}
 	
 	/**
 	 * Get the western point of the given tile.
 	 * @param x
 	 * @param zoom
 	 * @return
 	 */
 	protected static double tile2lon(int x, int zoom) {
 		return x / Math.pow(2.0, zoom) * 360.0 - 180;
 	}
 	
 	/**
 	 * Get the northern point of the given tile.
 	 * @param y
 	 * @param zoom
 	 * @return
 	 */
 	protected static double tile2lat(int y, int zoom) {
 		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, zoom);
 		return Math.toDegrees(Math.atan(Math.sinh(n)));
 	}
 	
 	/**
 	 * Get the tile associated with the given latitude.
 	 * @param lat
 	 * @param zoom
 	 * @return
 	 */
 	protected static int lat2tile(double lat, int zoom) {
 		return (int)Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom));
 	}
 	
 	/**
 	 * Get the tile associated with the given longitude.
 	 * @param lon
 	 * @param zoom
 	 * @return
 	 */
 	protected static int lon2tile(double lon, int zoom) {
 		return (int)Math.floor((lon + 180) / 360 * (1<<zoom));
 	}
 	
 	/**
 	 * Convert the given latitude to map screen space.
 	 * @param lat
 	 * @return
 	 */
 	public int getLatPos(double lat) {
 		return lat2pos(lat, zoom) - lat2pos(this.lat, zoom);
 	}
 	
 	/**
 	 * Convert the given longitude to map screen space.
 	 * @param lon
 	 * @return
 	 */
 	public int getLonPos(double lon) {
 		return lon2pos(lon, zoom) - lon2pos(this.lon, zoom);
 	}
 	
 	/**
 	 * Convert the given longitude to map pixel space.
 	 * @param lon
 	 * @param zoom
 	 * @return
 	 */
 	public static int lon2pos(double lon, int zoom) {
 		double max = 256 * (1 << zoom);
 		return (int)Math.floor((lon + 180) / 360 * max);
 	}
 	
 	/**
 	 * Convert the given latitude to map pixel space.
 	 * @param lat
 	 * @param zoom
 	 * @return
 	 */
 	public static int lat2pos(double lat, int zoom) {
 		double max = 256 * (1 << zoom);
 		double rlat = Math.toRadians(lat);
 		return (int)Math.floor((1 - Math.log(Math.tan(rlat) + 1 / Math.cos(rlat)) / Math.PI) / 2 * max);
 	}
 	
 	/**
 	 * Get the northern bound of the map window.
 	 * @return
 	 */
 	public double getNorthBound() {
 		int sy = lat2tile(lat, zoom);
 		double sLat = tile2lat(sy, zoom);
 		double dLat = (tile2lat(sy + 1, zoom) - sLat) / 256.0;
 		return lat - (dLat * (this.getHeight() / 2.0));
 	}
 	
 	/**
 	 * Get the southern bound of the map window.
 	 * @return
 	 */
 	public double getSouthBound() {
 		int sy = lat2tile(lat, zoom);
 		double sLat = tile2lat(sy, zoom);
 		double dLat = (tile2lat(sy + 1, zoom) - sLat) / 256.0;
 		return lat + (dLat * (this.getHeight() / 2.0));
 	}
 	
 	/**
 	 * Get the eastern bound of the map window.
 	 * @return
 	 */
 	public double getEastBound() {
 		int sx = lon2tile(lon, zoom);
 		double sLon = tile2lon(sx, zoom);
 		double dLon = (tile2lon(sx + 1, zoom) - sLon) / 256.0;
 		return lon + (dLon * (this.getWidth() / 2.0));
 	}
 	
 	/**
 	 * Get the western bound of the map window.
 	 * @return
 	 */
 	public double getWestBound() {
 		int sx = lon2tile(lon, zoom);
 		double sLon = tile2lon(sx, zoom);
 		double dLon = (tile2lon(sx + 1, zoom) - sLon) / 256.0;
 		return lon - (dLon * (this.getWidth() / 2.0));
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g0) {
 		super.paintComponents(g0);
 		Graphics2D g = (Graphics2D)g0.create();
 		int width = this.getWidth();
 		int height = this.getHeight();
 		g.translate(width/2, height/2);
 		
 		int sx = lon2tile(lon, zoom);
 		int sy = lat2tile(lat, zoom);
 		int nx = ((width / 256) + 2) / 2;
 		int ny = ((height / 256) + 2) / 2;
 		double slon = tile2lon(sx, zoom);
 		double slat = tile2lat(sy, zoom);
 		int ox = (int)((256/(tile2lon(sx+1, zoom)-slon))*(lon-slon)+0.5);
 		int oy = (int)((256/(tile2lat(sy+1, zoom)-slat))*(lat-slat)+0.5);
 		
 		for(int i = -nx; i <= nx; i++) {
 			for(int j = -ny; j <= ny; j++) {
 				int dx = i * 256 - ox;
 				int dy = j * 256 - oy;
 				BufferedImage tile = server.getTile(sx+i, sy+j, zoom);
 				g.drawImage(tile, dx, dy, null);
 			}
 		}
 		
 		Collection<MapOverlay> c = overlays.values();
 		for(MapOverlay overlay : c) overlay.drawOverlay(this, g);
 		
 		Font font = new Font("SansSerif", Font.PLAIN, 10);
 		FontMetrics metrics = super.getFontMetrics(font);
 		g.setFont(font);
 		int x = metrics.stringWidth(attribution1);
 		int y = metrics.getHeight();
 		g.drawString(attribution1, width/2 - x - 5, height/2 - y - 5);
 		x = metrics.stringWidth(attribution2);
 		g.drawString(attribution2, width/2 - x - 5, height/2 - 5);
 	}
 	
 	/**
 	 * The map has been removed from the screen. Close down.
 	 */
 	@Override
 	public void removeNotify() {
 		super.removeNotify();
		server.close();
 	}
 	
 	/**
 	 * Move the map by the given number of pixels.
 	 * @param tx
 	 * @param ty
 	 */
 	protected void translateMap(int tx, int ty) {
 		int x0 = lon2tile(lon, zoom);
 		int y0 = lat2tile(lat, zoom);
 		double dLon = tile2lon(x0 + 1, zoom) - tile2lon(x0, zoom);
 		double dLat = tile2lat(y0 + 1, zoom) - tile2lat(y0, zoom);
 		double dx = -dLon / 256;
 		double dy = -dLat / 256;
 		setCenter(ty * dy + lat, tx * dx + lon);
 	}
 
 	/**
 	 * Handle the mouse drag event.
 	 */
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		int tx = e.getX() - mouseDown.x;
 		int ty = e.getY() - mouseDown.y;
 		mouseDown = e.getPoint();
 		translateMap(tx, ty);
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// Not needed
 	}
 
 	/**
 	 * Handle the mouse click event.
 	 */
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if(e.getClickCount() == 2) {
 			if(e.getButton() == MouseEvent.BUTTON1) {
 				zoomIn();
 				e.consume();
 			} else if(e.getButton() == MouseEvent.BUTTON3) {
 				zoomOut();
 				e.consume();
 			}
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// Not needed
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// Not needed
 	}
 
 	/**
 	 * Handle the mouse pressed event.
 	 */
 	@Override
 	public void mousePressed(MouseEvent e) {
 		mouseDown = e.getPoint();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// Not needed
 	}
 	
 	/**
 	 * Notify the map of a change, redraw the map.
 	 */
 	public void updateNotify() {
 		repaint();
 	}
 
 }
