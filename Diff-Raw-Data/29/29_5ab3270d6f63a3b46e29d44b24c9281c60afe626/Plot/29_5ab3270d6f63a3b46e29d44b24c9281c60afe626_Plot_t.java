 package edu.berkeley.cs160.smartnature;
 
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.*;
 
 import com.google.gson.annotations.Expose;
 
 public class Plot {
 	
 	static final int RECT = 1, OVAL = 2, POLY = 3;
 	
 	private int db_id;
 	/** database id on server */
 	private int id;
 	private ShapeDrawable shape;
 	@Expose private String name;
 	@Expose private int shapetype;
 	@Expose private int color;
 	@Expose private String bounds; // used in JSON
 	@Expose private String points; // used in JSON
 	/** angle of clockwise rotation in degrees */
 	@Expose private float angle;
 	private float[] polyPoints = {};
 	private ArrayList<Plant> plants = new ArrayList<Plant>();
 
 	/** creates a copy of a plot except for its plants */
 	Plot(Plot src) { set(src); }
 	
 	Plot(String plotName, Rect bounds, int type) {
 		this(plotName, bounds, 0, type);
 	}
 	
 	Plot(String plotName, Rect bounds, float[] points) {
 		this(plotName, bounds, 0, points);
 	}
 	
 	Plot(String plotName, float[] points) {
 		name = plotName;
 		shapetype = POLY;
 
 		// compute bounds 
 		RectF boundsF = new RectF(points[0], points[1], points[0], points[1]);
 		for (int i = 2; i < points.length; i += 2) {
 			boundsF.left = Math.min(boundsF.left, points[i]);
 			boundsF.top = Math.min(boundsF.top, points[i + 1]);
 			boundsF.right = Math.max(boundsF.right, points[i]);
 			boundsF.bottom = Math.max(boundsF.bottom, points[i + 1]);
 		}
 
 		points[0] -= boundsF.left;
 		points[1] -= boundsF.top;
 		Path p = new Path();
 		p.moveTo(points[0], points[1]);
 		for (int i = 2; i < points.length; i += 2) {
 			// translate
 			points[i] -= boundsF.left;
 			points[i + 1] -= boundsF.top;
 			p.lineTo(points[i], points[i + 1]);
 		}
 		p.close();
 		polyPoints = points;
 		Rect bounds = new Rect((int) boundsF.left, (int) boundsF.top, (int) boundsF.right, (int) boundsF.bottom);
 		PathShape pshape = new PathShape(p, bounds.width(), bounds.height());
 		shape = new ShapeDrawable(pshape);
 		setBounds(bounds);
 		initPaint();
 	}
 	
 	/** create a rectangular or elliptical plot */
 	Plot(String plotName, Rect bounds, float angle, int type) {
 		name = plotName;
 		shapetype = type;
 		this.angle = angle;
 		shape = new ShapeDrawable(shapetype == OVAL ? new OvalShape() : new RectShape());
 		setBounds(bounds);
 		initPaint();
 	}
 	
 	/** create a polygonal plot */
 	Plot(String plotName, Rect bounds, float angle, float[] points) {
 		name = plotName;
 		shapetype = POLY;
 		polyPoints = points;
 		this.angle = angle;
 		Path p = new Path();
 		p.moveTo(points[0], points[1]);
 		for (int i = 2; i < points.length; i += 2)
 			p.lineTo(points[i], points[i + 1]);
 		p.close();
 		PathShape pshape = new PathShape(p, bounds.width(), bounds.height());
 		shape = new ShapeDrawable(pshape);
 		setBounds(bounds);
 		initPaint();
 	}
 	
 	/** clones data from another plot except for plants */
 	public void set(Plot src) {
 		name = src.getName();
 		shapetype = src.getType();
 		color = src.getColor();
 		angle = src.getAngle();
 
 		if (shapetype == POLY)
 			polyPoints = src.getPoints().clone();	
 
 		try {
 			shape = new ShapeDrawable(src.getShape().clone());
 		} catch (CloneNotSupportedException e) { e.printStackTrace(); }
 		setBounds(src.getBounds());
 		getPaint().set(src.getPaint());
 		plants = src.getPlants();
 	}
 	
 	public void initPaint() {
 		Paint paint = getPaint();
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeCap(Paint.Cap.ROUND);
 		paint.setStrokeJoin(Paint.Join.ROUND);
 	}
 	
 	public boolean contains(float x, float y) {
 		if (shapetype == OVAL)
 			return ovalContains(x, y);
 		else if (shapetype == POLY)
 			return pathContains(x, y);
 		else
 			return rectContains(x, y);
 	}
 	
 	private boolean rectContains(float x, float y) {
 		Rect bounds = getBounds();
 		return bounds.left <= x && x <= bounds.right && bounds.top <= y && y <= bounds.bottom;  
 	}
 	
 	/** @see java.awt.geom.Ellipse2D#contains(double, double) */
 	private boolean ovalContains(float x, float y) {
 		Rect bounds = getBounds();
 		float ellw = bounds.width(), ellh = bounds.height();
 		float normx = (x - bounds.left) / ellw - 0.5f;
 		float normy = (y - bounds.top) / ellh - 0.5f;
 		return (normx * normx + normy * normy) <= 0.25f;
 	}
 
 	/** @see <a href="http://alienryderflex.com/polygon">http://alienryderflex.com/polygon</a> */
 	private boolean pathContains(float x, float y) {
 		x -= shape.getBounds().left;
 		y -= shape.getBounds().top;
 		int polySides = polyPoints.length / 2;
 		float[] polyX = new float[polySides], polyY = new float[polySides];
 		for (int i = 0; i < polyPoints.length; i += 2) {
 			polyX[i/2] = polyPoints[i];
 			polyY[i/2] = polyPoints[i + 1]; 
 		}
 
 		boolean oddTransitions = false;
 		for (int i = 0, j = polySides - 1; i < polySides; j = i++) {
 			if ((polyY[i] < y && polyY[j] >= y) || (polyY[j] < y && polyY[i] >= y))
 				if (polyX[i] + (y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < x)
 					oddTransitions = !oddTransitions;
 		}
 		return oddTransitions;
 	}
 	
 	/** bounds of plot taking rotation into account */
 	public RectF getRotateBounds() {
 		if (angle == 0)
 			return new RectF(shape.getBounds());
 		
 		RectF bounds;
 		Matrix m = new Matrix();
 		m.setRotate(angle, getBounds().centerX(), getBounds().centerY());
 		
 		if (shapetype == POLY) {
 			float[] rotPoints = new float[polyPoints.length];
 			m.preTranslate(getBounds().left, getBounds().top);
 			m.mapPoints(rotPoints, polyPoints);
 			bounds = new RectF(rotPoints[0], rotPoints[1], rotPoints[0], rotPoints[1]);
 			for (int i = 0; i < rotPoints.length; i += 2) {
 				bounds.left = Math.min(bounds.left, rotPoints[i]);
 				bounds.top = Math.min(bounds.top, rotPoints[i + 1]);
 				bounds.right = Math.max(bounds.right, rotPoints[i]);
 				bounds.bottom = Math.max(bounds.bottom, rotPoints[i + 1]);
 			}
 		}
 		else {
 			bounds = new RectF(getBounds());
 			m.mapRect(bounds);
 		}
 		
 		return bounds;	
 	}
 	
 	public float[] getCenter() {
 		if (shapetype == POLY) {
 			float centerX = 0, centerY = 0;
 			for (int i = 0; i < polyPoints.length; i += 2) {
 				centerX += polyPoints[i];
 				centerY += polyPoints[i + 1];
 			}
 			return new float[] { centerX * 2/polyPoints.length, centerY * 2/polyPoints.length };
 		}
 		
 		return new float[] { getBounds().centerX(), getBounds().centerY() };
 	}
 	
 	public void resize(int dx, int dy) {
 		Rect newBounds = new Rect(getBounds());
 		newBounds.inset(-dx, -dy);
 		if (shapetype == POLY) {
 			RectF oldPBounds = new RectF(0, 0, getBounds().width(), getBounds().height());
 			RectF newPBounds = new RectF(0, 0, getBounds().width() + 2 * dx, getBounds().height() + 2 * dy);
 			Matrix m = new Matrix();
 			m.setRectToRect(oldPBounds, newPBounds, Matrix.ScaleToFit.FILL);
 			m.mapPoints(polyPoints);
 			Path p = new Path();
 			p.moveTo(polyPoints[0], polyPoints[1]);
 			for (int i = 2; i < polyPoints.length; i += 2) {
 				p.lineTo(polyPoints[i], polyPoints[i + 1]);
 			}
 			p.close();
 
 			PathShape pshape = new PathShape(p, newBounds.width(), newBounds.height());
 			setShape(pshape);
 		}
 
 		setBounds(newBounds);
 	}
 	
 	public void preUpload() {
 		bounds = getBounds().flattenToString();
 		points = "";
 		for (float f : polyPoints)
 			points += f + " ";
 	}
 	
 	public void postDownload() {
 		Rect rbounds = Rect.unflattenFromString(bounds);
 		if (shapetype != POLY)
 			set(new Plot(name, rbounds, angle, shapetype));
 		else {
 			String[] list = points.split(" ");
 			float[] pointsList = new float[list.length];
 			for (int i = 0; i < list.length; i++)
 				pointsList[i] = Float.parseFloat(list[i]);
 			set(new Plot(name, rbounds, angle, pointsList));
 		}
 	}
 	
 	public void addPlant(Plant p) { plants.add(p); }
 
 	public float getAngle() { return angle; }
 	
 	public int getColor() { return color; }
 	
 	public ContentValues getContentValues() {
 		ContentValues values = new ContentValues();
 		values.put("server_id", id);
 		values.put("name", name);
 		values.put("shape", shapetype);
 		values.put("color", color);
 		values.put("angle", angle);
 		values.put("bounds", getBounds().flattenToString());
 		if (shapetype == POLY)
			values.put("points", Helper.toString(polyPoints));
 		return values;
 	}
 	
 	public int getId(){ return db_id; }
 	
 	public String getName() { return name; }
 	
 	public Plant getPlant(int index) { return plants.get(index); }
 	
 	public ArrayList<Plant> getPlants() { return plants; }
 	
 	public float[] getPoints() { return polyPoints; }
 	
 	public int getServerId() { return id; }
 	
 	public int getType() { return shapetype; }
 	
 	public void setAngle(float angle) { this.angle = angle; }
 	
 	public void setColor(int color) { this.color = color; }
 	
 	public void setId(int id) { db_id = id; }
 	
 	public void setName(String name) { this.name = name; }
 	
 	public void setPoints(float[] points) { polyPoints = points; }
 	
 	public void setServerId(int serverId) { this.id = serverId; }
 	
 	/** ShapeDrawable related methods */
 	
 	public void draw(Canvas canvas) { shape.draw(canvas); }
 	
 	public Rect getBounds() { return shape.getBounds(); }
 	
 	public Paint getPaint() { return shape.getPaint(); }
 	
 	public Shape getShape() { return shape.getShape(); }
 	
 	public void setBounds(Rect bounds) { shape.setBounds(bounds); }
 	
 	public void setShape(Shape shape) { this.shape.setShape(shape); }
 	
 }
