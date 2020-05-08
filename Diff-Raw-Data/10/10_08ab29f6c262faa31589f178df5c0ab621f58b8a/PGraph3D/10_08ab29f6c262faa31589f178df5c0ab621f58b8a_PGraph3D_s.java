 package applets.AnalytischeGeometrieundLA_Ebene_StuetzNormRichtung;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Polygon;
 import java.awt.event.MouseEvent;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import applets.AnalytischeGeometrieundLA_Ebene_StuetzNormRichtung.PGraph3D.Viewport.Primitive;
 
 import com.sun.org.apache.bcel.internal.generic.NEW;
 
 public class PGraph3D implements VTImage.PainterAndListener, Applet.CorrectCheck {
 
 	static float EPS = 0.001f;
 	
 	static public abstract class DynFloat {
 		public float get() throws Exception { throw new Exception("DynFloat::get() not defined"); }
 
 		public boolean isValid() {
 			try {
 				get();
 				return true;
 			} catch(Exception e) {
 				return false;
 			}
 		}
 		
 		public String toString() {
 			try {
 				return "" + get();
 			} catch (Exception e) {
 				return e.getMessage();
 			}
 		}
 	}
 
 	static public abstract class DynVector3D {
 		public float get(int i) throws Exception { throw new Exception("DynVector3D::get() not defined"); }
 		
 		public boolean isValid() {
 			try {
 				for(int i = 0; i < 3; ++i) get(i);
 				return true;
 			} catch(Exception e) {
 				return false;
 			}
 		}
 		
 		public float abs() throws Exception { return (float) Math.sqrt(get(0) * get(0) + get(1) * get(1) + get(2) * get(2)); }
 		
 		public Point3D fixed() {
 			try {
 				return new Point3D(this);
 			} catch(Exception e) {
 				return null;
 			}
 		}
 		
 		public DynVector3D crossProduct(final DynVector3D v) {
 			final DynVector3D t = this;
 			return new DynVector3D() {
 				public float get(int i) throws Exception {
 					return t.get(i + 1) * v.get(i + 2) - t.get(i + 2) * v.get(i + 1);
 				}
 			};
 		}
 		
 		public DynFloat dotProduct(final DynVector3D v) {
 			final DynVector3D t = this;
 			return new DynFloat() {
 				public float get() throws Exception {
 					return t.get(0) * v.get(0) + t.get(1) * v.get(1) + t.get(2) * v.get(2);
 				}
 			};
 		}
 
 		public DynVector3D product(final DynFloat f) {
 			final DynVector3D t = this;
 			return new DynVector3D() {
 				public float get(int i) throws Exception { return t.get(i) * f.get(); }
 			};
 		}
 
 		public DynVector3D sum(final DynVector3D v) {
 			final DynVector3D t = this;
 			return new DynVector3D() {
 				public float get(int i) throws Exception { return t.get(i) + v.get(i); }
 			};
 		}
 
 		public DynVector3D diff(final DynVector3D v) {
 			final DynVector3D t = this;
 			return new DynVector3D() {
 				public float get(int i) throws Exception { return t.get(i) - v.get(i); }
 			};
 		}
 		
 		public DynVector3D norminated() {
 			final DynVector3D t = this;
 			return new DynVector3D() {
 				public float get(int i) throws Exception { return t.get(i) / t.abs(); }
 			};
 		}
 		
 		private String get_toString(int i) {
 			try {
 				return "" + get(i);
 			} catch (Exception e) {
 				return e.getMessage();
 			}
 		}
 		
 		public String toString() {
 			return "(" + get_toString(0) + "," + get_toString(1) + "," + get_toString(2) + ")";
 		}
 	}
 	
 	static public class pair < First > {
 		public First x;
 		public First y;
 		public pair() {}
 		public pair(First _x, First _y) { x = _x; y = _y; }
 
 		public String toString() {
 			return "<" + x.toString() + "," + y.toString() + ">";
 		}
 	}
 	
 	static public class Vector3D extends DynVector3D {
 		public float x[] = new float[3];
 		public Vector3D() {}
 		public Vector3D(float a, float b, float c) { x[0] = a; x[1] = b; x[2] = c; }
 		public Vector3D(double[] ds) { for(int i = 0; i < 3; ++i) x[i] = (float) ds[i]; }
 
 		public float get(int i) { return x[i % 3]; }
 		public void set(Vector3D v) { for(int i = 0; i < 3; ++i) x[i] = v.x[i]; }
 
 		public String asString() {
 			float r = 10;
 			return "(" + Math.round(x[0]*r)/r + "," + Math.round(x[1]*r)/r + "," + Math.round(x[2]*r)/r + ")";
 		}
 	}
 	
 	static public class Point3D extends Vector3D {
 		public Point3D() {}
 		public Point3D(DynVector3D v) throws Exception { for(int i = 0; i < 3; ++i) x[i] = v.get(i); }
 	}
 	
 	static public class Float extends DynFloat {
 		public float x;
 		public Float() {}
 		public Float(float x_) { x = x_; }
 		public float get() { return x; }
 	}
 	
 	public class Viewport {
 		Graphics g;
 		Float eyeDistanceFactor = new Float(2.0f);
 		Float eyeHeight = new Float(10.0f);
 		Vector3D eyeDir = new Vector3D(0,1,0);
 		Vector3D xAxeDir = new Vector3D(1,0,0);
 		Plane eyePlane = new Plane(eyeHeight, eyeDir);
 		DynVector3D eyePoint = eyeDir.product(eyeHeight).product(eyeDistanceFactor);
 		float scaleFactor = 5;
 		
 		public void rotate(Matrix3D rotateMatrix) {
 			eyeDir.set( rotateMatrix.product( eyeDir ).norminated().fixed() );
 			xAxeDir.set( rotateMatrix.product( xAxeDir ).norminated().fixed() );
 		}
 		
 		public void setGraphics(Graphics g) { this.g = g; setColor(Color.black); }
 		public void setColor(Color c) { g.setColor(c); }
 		
 		public float maxSize() {
 			 return (float) ((Math.max(W, H) * 0.5 / viewport.scaleFactor) / viewport.eyeDistanceFactor.x);
 		}
 		
 		class Primitive {
 			float dist;
 			Color color;
 			public Primitive() {}
 			public Primitive(float d, Color c) { dist = d; color = c; }
 			void draw() {}
 		}
 		
 		SortedSet<Primitive> primitives = new TreeSet<Primitive>(new Comparator<Primitive>() {
			public int compare(Primitive o1, Primitive o2) {
 				float d = o2.dist - o1.dist;
 				if(d < -EPS) return -1;
 				if(d > EPS) return 1;
 				return o2.hashCode() - o1.hashCode();
			}});
 		
 		public void doFrame() {
 			for(Primitive p : primitives) {
 				setColorAtDepth( p.color, p.dist );
 				p.draw();
 			}
 			primitives.clear();
 		}
 		
 		private void addPrimitive(Primitive p) {
 			//if(p.dist >= 0)
 				primitives.add(p);
 		}
 		
 		private float getEyePlane_PointDistance(Point3D p) {
 			Point3D eyePlanePoint = getEyePlanePoint(p);
 			Point3D relativeP = p.diff(eyePlanePoint).fixed();
 			//Point3D relativeEye = eyePoint.diff(eyePlanePoint).fixed();
 			
 			try {
 				float linP = eyePlane.normal.dotProduct(relativeP).get();
 				//float linEye = eyePlane.normal.dotProduct(relativeEye).get();
 				return linP;
 				
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 			
 			return 0;
 		}
 		
 		private Point3D getEyePlanePoint(Point3D p) {
 			Line eyeLine = new Line();
 			eyeLine.point = eyePoint;
 			eyeLine.vector = p.diff(eyeLine.point);		
 			return eyePlane.intersectionPoint(eyeLine).point.fixed();
 		}
 
 		private java.awt.Point translate(Point3D p) throws Exception {
 			Point3D eyePtAbs = getEyePlanePoint(p);
 			if(eyePtAbs == null) throw new Exception("translate: plane point bad");
 			
 			Point3D eyePt = eyePtAbs.diff(eyePlane.basePoint()).fixed();
 			Vector3D xAxe = xAxeDir;
 			Vector3D yAxe = xAxe.crossProduct(eyePlane.normal).norminated().fixed();
 			
 			java.awt.Point tp = new java.awt.Point();
 			try {
 				tp.x = (int) ( xAxe.dotProduct( eyePt ).get() * scaleFactor );
 				tp.y = - (int) ( yAxe.dotProduct( eyePt ).get() * scaleFactor );
 			} catch (Exception e) {
 				// should not happen
 				e.printStackTrace();
 			}
 			
 			tp.x += W/2;
 			tp.y += H/2;
 			return tp;
 		}
 		
 		private float CLAMP(float x, float min, float max) {
 			return Math.min(max, Math.max(x, min));
 		}
 		
 		private void setColorAtDepth(Color c, float d) {
 			this.g.setColor(c); if(0==0)return;
 			
 			d /= (1.5f * maxSize());
 			d = CLAMP(d, 0, 1);
 			
 			float r = (float) c.getRed() / 255;
 			float g = (float) c.getGreen() / 255;
 			float b = (float) c.getBlue() / 255;
 			float a = (float) c.getAlpha() / 255;
 			
 			/*
 			r = d - (1-d)*r;
 			g = d - (1-d)*g;
 			b = d - (1-d)*b;
 			
 			r = CLAMP(r, 0, 1);
 			g = CLAMP(g, 0, 1);
 			b = CLAMP(b, 0, 1);
 			a = CLAMP(a, 0, 1);*/
 			
 			this.g.setColor(new Color(r, g, b, a));
 		}
 				
 		private float getPointsDistance(Point3D[] ps) {
 			float nearest = maxSize();
 			for(int i = 0; i < ps.length; ++i) {
 				float dist = getEyePlane_PointDistance(ps[i]);
 				nearest = Math.min(nearest, dist);
 			}
 			return nearest;
 		}
 		
 		private void drawLine_LowLevel(final Point3D p1, final Point3D p2) throws Exception {
 			float d = getPointsDistance(new Point3D[] {p1, p2});
 			addPrimitive(new Primitive(d, g.getColor()) {
 				void draw() {
 					try {
 						java.awt.Point tp1 = translate(p1);
 						java.awt.Point tp2 = translate(p2);
 						g.drawLine(tp1.x, tp1.y, tp2.x, tp2.y);
 					} catch(Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 		}
 		
 		public void drawPoint(final Point3D p) {
 			float d = getEyePlane_PointDistance(p);
 			addPrimitive(new Primitive(d, g.getColor()) {
 				void draw() {
 					try {
 						java.awt.Point tp = translate(p);
 						g.fillOval(tp.x, tp.y, 2, 2);					
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 		}
 		
 		public void drawArrow(Point3D p, Vector3D v) {
 			// TODO:...
 			drawLine(p, v);
 		}
 		
 		public void drawLine(Point3D p, Vector3D v) {
 			try {
 				int splitNum = 1 + (int) (20.0f * Math.min(v.abs(), maxSize()) / maxSize());
 				for(int i = 0; i < splitNum; ++i)
 					drawLine_LowLevel(
 							p.sum(v.product(new Float((float)(i) / splitNum))).fixed(),
 							p.sum(v.product(new Float((float)(i+1) / splitNum))).fixed()
 							);
 			} catch (Exception e) {
 				// should not happen
 				e.printStackTrace();
 			}
 		}
 		
 		public void drawPolygon(final Point3D[] p) {
 			float d = getPointsDistance(p);
 			addPrimitive(new Primitive(d, g.getColor()) {
 				void draw() {
 					try {
 						Polygon tpoly = new Polygon();
 						for(int i = 0; i < p.length; ++i) {
 							java.awt.Point tp = translate(p[i]);
 							tpoly.addPoint(tp.x, tp.y);
 						}
 						g.fillPolygon(tpoly);
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					}
 				}
 			});
 		}
 		
 	}
 	
 	Viewport viewport = new Viewport();
 	
 	static public interface Object3D {
 		public void draw(Viewport v);
 	}
 	
 	static public class Line implements Object3D {
 		DynVector3D point;
 		DynVector3D vector;
 		Color color = Color.black;
 		
 		public Line() {}
 		public Line(DynVector3D p, DynVector3D v, Color c) { point = p; vector = v; color = c; }  
 		
 		public void draw(Viewport v) {
 			if(point.isValid() && vector.isValid()) {
 				v.setColor(color);
 				v.drawLine( point.fixed(), vector.fixed() );
 			}
 		};
 		
 		public boolean isValid() {
 			try {
 				return point.isValid() && vector.abs() > EPS;
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		
 		public Point intersectionPoint(final Line l) {
 			final Line t = this;
 			
 			final DynMatrix2D ms[] = new DynMatrix2D[3];
 			
 			for(int k = 0; k < 3; ++k) {
 				final int K = k;
 				// [ v1, -v2 ] * (t1 t2) = p2 - p1  ->  p1 + t1 v1 ( = p2 + t2 v2 ) is intersection point			
 				ms[k] = new DynMatrix2D() {
 					public float get(int i, int j) throws Exception {
 						j %= 2;
 						if(j == 0) return t.vector.get(i + K);
 						else return -l.vector.get(i + K);
 					}
 				};
 			}
 			final DynVector3D pointDiff = l.point.diff( point );
 			
 			Point p = new Point();
 			p.point = new DynVector3D() {
 				public float get(int i) throws Exception {
 					for(int k = 0; k < 3; ++k) {
 						try {
 							final pair<DynFloat> ts = ms[k].solve(new pair<DynFloat>(new Float(pointDiff.get(k)), new Float(pointDiff.get(k + 1))));
 							float tx = ts.x.get();
 							float ty = ts.y.get();
 							
 							// we must check now
 							float check = vector.get(k + 2) * tx - l.vector.get(k + 2) * ty - (l.point.get(k + 2) - point.get(k + 2));
 							if(Math.abs(check) > EPS) throw new Exception("no solution");
 						
 							return t.point.sum( t.vector.product(ts.x) ).get(i);
 						}
 						catch(Exception e) {
 						}
 					}
 					throw new Exception("intersectionPoint: parameters invalid");
 				}
 			};
 			return p;
 		}
 	}
 	
 	static public class VectorArrow extends Line {
 		public VectorArrow() { super();	}
 		public VectorArrow(DynVector3D p, DynVector3D v, Color c) { super(p, v, c); }
 
 		public void draw(Viewport v) {
 			if(point.isValid() && vector.isValid()) {
 				v.setColor(color);
 				v.drawArrow( point.fixed(), vector.fixed() );
 			}
 		}
 	}
 	
 	static public class Point implements Object3D {
 		DynVector3D point;
 		Color color = Color.black;
 		
 		public Point() {}
 		public Point(DynVector3D p) { point = p; }
 		
 		public void draw(Viewport v) {
 			if(point.isValid()) {
 				v.setColor(color);
 				v.drawPoint(point.fixed());
 			}
 		}
 	}
 	
 	static public abstract class DynMatrix2D {
 		public float get(int i, int j) throws Exception { throw new Exception("DynMatrix2D::get not defined"); }
 		
 		public DynFloat det() {
 			final DynMatrix2D t = this;			
 			return new DynFloat() {
 				public float get() throws Exception { return t.get(0,0) * t.get(1,1) - t.get(0,1) * t.get(1,0); }
 			};
 		}
 		
 		public pair<DynFloat> solve(final pair<DynFloat> v) {
 			// using Cramer's rule
 			final DynMatrix2D t = this;
 			pair<DynFloat> p = new pair<DynFloat>();
 			p.x = new DynFloat() {
 				public float get() throws Exception {
 					float top = v.x.get() * t.get(1,1) - t.get(0,1) * v.y.get();
 					float bottom = t.det().get();
 					if(Math.abs(bottom) > EPS) return top / bottom;
 					throw new Exception("Matrix::solve: no solution for x");
 				}
 			};
 			p.y = new DynFloat() {
 				public float get() throws Exception {
 					float top = t.get(0,0) * v.y.get() - v.x.get() * t.get(1,0);
 					float bottom = t.det().get();
 					if(Math.abs(bottom) > EPS) return top / bottom;
 					throw new Exception("Matrix::solve: no solution for y");
 				}
 			};
 			return p;
 		}
 		
 		private String get_toString(int i, int j) {
 			try {
 				return "" + get(i,j);
 			} catch (Exception e) {
 				return e.getMessage();
 			}
 		}
 		
 		public String toString() {
 			return "[(" + get_toString(0,0) + "," + get_toString(0,1) + ") (" + get_toString(1,0) + "," + get_toString(1,1) + ")]";
 		}
 	}
 	
 	static public class Plane implements Object3D {
 		DynFloat height;
 		DynVector3D normal;
 		Color color = Color.black;
 		
 		public Plane() {}
 		public Plane(DynFloat height, DynVector3D normal) { this.height = height; this.normal = normal; }
 		public Plane(DynFloat height, DynVector3D normal, Color c) { this.height = height; this.normal = normal; color = c; }
 
 		DynVector3D basePoint() {
 			Line normalLine = new Line();
 			normalLine.point = new Point3D();
 			normalLine.vector = normal;
 			return intersectionPoint(normalLine).point;
 		}
 		
 		static public Plane xPlane = new Plane(new Float(0), new Vector3D(1,0,0));
 		static public Plane yPlane = new Plane(new Float(0), new Vector3D(0,1,0));
 		static public Plane zPlane = new Plane(new Float(0), new Vector3D(0,0,1));
 		static public Plane[] basePlanes = new Plane[] { xPlane, yPlane, zPlane };
 		
 		public void draw(Viewport v) {
 			v.setColor(color);
 			
 			Line[] lines = new Line[3];
 			for(int i = 0; i < 3; ++i) lines[i] = basePlanes[i].intersectionLine(this);
 
 			// just for easier debugging
 			for(int i = 0; i < 3; ++i) lines[i].point = lines[i].point.fixed(); 
 			for(int i = 0; i < 3; ++i) lines[i].vector = lines[i].vector.fixed(); 
 			
 			//for(int i = 0; i < 3; ++i) lines[i].draw(v); // debugging
 			
 			Point3D[] points = new Point3D[3];
 			for(int i = 0; i < 3; ++i) points[i] = lines[i].intersectionPoint(lines[ (i+1) % 3 ]).point.fixed();
 
 			//for(int i = 0; i < 3; ++i) if(points[i] != null) new Point(points[i]).draw(v); // debugging
 			//if(0 == 0) return;
 			
 			List<Point3D> pointList = new LinkedList<Point3D>();
 			for(int i = 0; i < 3; ++i) {
 				Point3D p = points[i];
 				if(p != null) pointList.add(p);
 				else {
 					Point3D[] otherps = new Point3D[2];
 					for(int j = 0; j < 2; ++j) otherps[j] = points[ (i+j+1) % 3 ];
 					for(int j = 0; j < 2; ++j) if(otherps[j] == null) return; // the whole plane seems invalid
 					// somewhat pulled out of thin air
 					for(int j = 0; j < 2; ++j) pointList.add( otherps[j].sum( basePlanes[(i+j) % 3].normal.product(new Float(5.0f)) ).fixed() );
 				}
 			}
 			
 			v.drawPolygon( pointList.toArray(new Point3D[] {}) );
 		}
 		
 		public Line intersectionLine(Plane other) {
 			Line l = new Line();
 			l.vector = normal.crossProduct(other.normal);
 			final DynVector3D normals[] = { normal, other.normal };
 			// [ ( (n1 n1) (n1 n2) ) ( (n2 n1) (n2 n2) ) ] * (c1 c2) = (h1 h2)  ->  n1 c1 + n2 c2  is point on line
 			DynMatrix2D nn = new DynMatrix2D() {
 				public float get(int i, int j) throws Exception {
 					i %= 2; j %= 2;
 					return normals[i].dotProduct( normals[j] ).get();
 				}
 			};
 			pair<DynFloat> heights = new pair<DynFloat>(height, other.height);
 			final pair<DynFloat> c = nn.solve(heights);
 			l.point = normal.product(c.x).sum( other.normal.product(c.y) );
 			return l;
 		}
 		
 		public DynFloat intersectionPoint_LineFactor(final Line line) {
 			return new DynFloat() {
 				public float get() throws Exception {
 					float nv = normal.dotProduct(line.vector).get();
 					float hnp = height.get() - normal.dotProduct(line.point).get();
 					if(Math.abs(nv) < EPS) {
 						if(Math.abs(hnp) < EPS) throw new Exception("Plane::intersectionPoint of line: line is on plane");
 						else throw new Exception("Plane::intersectionPoint of line: there is no intersection point");
 					}
 					float t = nv / hnp;
 					return t;
 				}	
 			};
 		}
 		
 		public Point intersectionPoint(final Line line) {
 			final DynFloat t = intersectionPoint_LineFactor(line);
 			Point p = new Point();
 			p.point = line.point.sum( line.vector.product(t) );
 			return p;
 		}
 	}
 	
 	static public class Matrix3D {
 		Vector3D[] v = new Vector3D[] { new Vector3D(), new Vector3D(), new Vector3D() };
 		public Matrix3D() {}
 		public Matrix3D(float f) { for(int i = 0; i < 3; ++i) v[i].x[i] = f; }
 		public Matrix3D(float[] m) {
 			for(int i = 0; i < 3; ++i)
 				for(int j = 0; j < 3; ++j)
 					v[i].x[j] = m[j*3 + i];
 		}
 		
 		public Matrix3D sum(Matrix3D m) {
 			Matrix3D res = new Matrix3D();
 			for(int i = 0; i < 3; ++i)
 				for(int j = 0; j < 3; ++j)
 					res.v[i].x[j] = v[i].x[j] + m.v[i].x[j];
 			return res;
 		}
 		
 		public Matrix3D product(Matrix3D m) {
 			Matrix3D res = new Matrix3D();
 			for(int i = 0; i < 3; ++i)
 				for(int j = 0; j < 3; ++j)
 					for(int k = 0; k < 3; ++k)
 						res.v[i].x[j] += v[k].x[j] * m.v[i].x[k];
 			return res;
 		}
 
 		public Matrix3D product(float f) {
 			Matrix3D res = new Matrix3D();
 			for(int i = 0; i < 3; ++i)
 				for(int j = 0; j < 3; ++j)
 					res.v[i].x[j] = v[i].x[j] * f;
 			return res;
 		}
 		
 		public Vector3D product(Vector3D v) {
 			Vector3D res = new Vector3D();
 			for(int i = 0; i < 3; ++i)
 				for(int j = 0; j < 3; ++j)
 					res.x[i] += this.v[j].x[i] * v.x[j];
 			return res;
 		}
 		
 	}
 	
 	private Applet applet;
 	public int W;
 	public int H;
 	
 	public PGraph3D(Applet applet, int w, int h) {
 		this.applet = applet;
 		W = w;
 		H = h;
 	}
 
 	public Set<Object3D> objects = new HashSet<Object3D>();
 	
 	public void paint(Graphics g) {
 		viewport.setGraphics(g);
 		for(Object3D o : objects) {
 			o.draw(viewport);
 		}
 		
 		// for now
 		//g.setColor(Color.CYAN);
 		//g.fillOval(W/2, H/2, 5, 5);
 		g.setColor(Color.orange);
 		
 		/*
 		try {
 			for(int i = 0; i < TeacupPoints.length; ++i) {
 				viewport.drawPoint(new Point3D(new Vector3D(TeacupPoints[i]).product(new Float(10.0f))));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		*/
 
 		viewport.doFrame();
 
 		
 		g.setColor(Color.orange);
 		g.drawString(viewport.eyeDir.asString() + viewport.eyePoint.fixed().asString(), 0, 10);
 	}
 	
 	public void mouseClicked(MouseEvent e) {
 	}
 	
 	public void mouseEntered(MouseEvent e) {
 	}
 	
 	public void mouseExited(MouseEvent e) {
 	}
 	
 	static protected java.awt.Point pointFromEvent(MouseEvent e) {
 		return new java.awt.Point(e.getX(), e.getY());
 	}
 	
 	protected Point3D pointOnEyeGlobe(java.awt.Point p) {
 		/*
 		Float eyeDistanceFactor = new Float(2.0f);
 		Float eyeHeight = new Float(1.0f);
 		Vector3D eyeDir = new Vector3D(-1,0,0);
 		Plane eyePlane = new Plane(eyeHeight, eyeDir);
 		DynVector3D eyePoint = eyeDir.product(eyeHeight).product(eyeDistanceFactor);
 		float scaleFactor = 5;
 		 */
 		
 		float x = (p.x - W/2) / viewport.scaleFactor;
 		float z = -(p.y - H/2) / viewport.scaleFactor;
 		
 		x /= viewport.eyeDistanceFactor.x;
 		z /= viewport.eyeDistanceFactor.x;
 		
 		float maxsize = viewport.maxSize();
 
 		double a = Math.sqrt(x*x + z*z); 
 		if(a > maxsize) {
 			x *= maxsize / a;
 			z *= maxsize / a;
 		}
 		
 		float y = maxsize*maxsize - x*x - z*z;
 		if(y < 0) y = 0; else y = (float) Math.sqrt(y);
 		
 		Point3D globePos = new Point3D();
 		globePos.x[0] = x;
 		globePos.x[1] = -y;
 		globePos.x[2] = z;
 	
 		//objects.add(new Point(globePos.sum(new Vector3D(0,y,0))));
 		
 		return globePos.norminated().fixed();
 	}
 	
 	java.awt.Point oldMousePoint = null;
 	
 	public void mousePressed(MouseEvent e) {
 		oldMousePoint = pointFromEvent(e);
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		oldMousePoint = null;
 	}
 	
 	public void mouseDragged(MouseEvent e) {
 		mouseMoved(e);
 	}
 	
 	static Matrix3D getRotateMatrix(Vector3D rotateAxe, float cos_a, float sin_a) {
 		float[] v = rotateAxe.x;
 		Matrix3D rotateM = new Matrix3D(new float[] {
 				cos_a + v[0]*v[0]*(1 - cos_a),		v[0]*v[1]*(1 - cos_a) - v[2]*sin_a,	v[0]*v[2]*(1 - cos_a) + v[1]*sin_a,
 				v[1]*v[0]*(1 - cos_a) + v[2]*sin_a,	cos_a + v[1]*v[1]*(1 - cos_a),		v[1]*v[2]*(1 - cos_a) - v[0]*sin_a,
 				v[2]*v[0]*(1 - cos_a) - v[1]*sin_a,	v[2]*v[1]*(1 - cos_a) + v[0]*sin_a,	cos_a + v[2]*v[2]*(1 - cos_a)
 		});
 		return rotateM;
 	}
 	
 	static Matrix3D getRotateMatrixForPoint(Point3D globePos, boolean swapRotate) {
 		// calculate rotation matrix which rotates (0,-1,0) <- globePos
 
 		float x = globePos.x[0];
 		float z = globePos.x[2];
 
 		final Vector3D rotateAxe = globePos.crossProduct( new Vector3D(0,-1,0) ).norminated().fixed();
 		final float[] v = rotateAxe.x;
 				
 		float sin_a = 0, cos_a = 0;
 		if(Math.abs(v[1]) > EPS) try {
 			// TODO: case v0 == 0 || v1 == 0
 			
 			DynMatrix2D m = new DynMatrix2D() {
 				float[] m = new float[] {
 						v[0] * v[1], - v[2],
 						v[1] * v[2], v[0]
 				};
 				public float get(int i, int j) throws Exception {
 					return m[i * 2 + j];
 				}
 			};
 			
 			pair<DynFloat> cossin_a = m.solve( new pair<DynFloat>(new Float(-x), new Float(-z)) );
 
 			sin_a = cossin_a.y.get();
 			cos_a = 1.0f - cossin_a.x.get();
 		} catch (Exception e1) {
 			// should not happen
 			e1.printStackTrace();
 		}
 		else { // v[1] == 0, we always have that case right now ...
 			if(Math.abs(v[0]) > Math.abs(v[2]) && Math.abs(v[0]) > EPS) {
 				sin_a = - z / v[0];
 			}
 			else if(Math.abs(v[2]) > EPS) {
 				sin_a = x / v[2];
 			}
 			else {
 				sin_a = 1f;				
 			}
 			cos_a = (float) Math.sqrt(1 - sin_a*sin_a);
 		}
 		
 		if(swapRotate)
 			// rotate in the other direction (g -> 0,-1,0) than what we calculated (0,-1,0 -> g)
 			sin_a = - sin_a;
 
 		return getRotateMatrix(rotateAxe, cos_a, sin_a);
 	}
 	
 	public void mouseMoved(MouseEvent e) {
 		if(oldMousePoint == null) return;
 		
 		Point3D globePosOld = pointOnEyeGlobe( oldMousePoint );
 		Point3D globePosNew = pointOnEyeGlobe( pointFromEvent(e) );
 		
 		Matrix3D rotateM1 = getRotateMatrixForPoint(globePosOld, true);
 		Matrix3D rotateM2 = getRotateMatrixForPoint(globePosNew, false);
 				
 		viewport.rotate(rotateM1);
 		viewport.rotate(rotateM2);
 		
 		oldMousePoint = pointFromEvent(e);
 
 		applet.repaint();
 	}
 	
 	public String getResultMsg() {
 		return "";
 	}
 	
 	public boolean isCorrect() {
 		return false;
 	}
 
 	
 	
 	static double[][] TeacupPoints =
 	{
 	        {0.409091,0.772727,0.0},
 	        {0.409091,0.772727,-0.229091},
 	        {0.229091,0.772727,-0.409091},
 	        {0.0,0.772727,-0.409091},
 	        {0.409091,0.886364,0.0},
 	        {0.409091,0.886364,-0.229091},
 	        {0.229091,0.886364,-0.409091},
 	        {0.0,0.886364,-0.409091},
 	        {0.454545,0.886364,0.0},
 	        {0.454545,0.886364,-0.254545},
 	        {0.254545,0.886364,-0.454545},
 	        {0.0,0.886364,-0.454545},
 	        {0.454545,0.772727,0.0},
 	        {0.454545,0.772727,-0.254545},
 	        {0.254545,0.772727,-0.454545},
 	        {0.0,0.772727,-0.454545},
 	        {-0.229091,0.772727,-0.409091},
 	        {-0.409091,0.772727,-0.229091},
 	        {-0.409091,0.772727,0.0},
 	        {-0.229091,0.886364,-0.409091},
 	        {-0.409091,0.886364,-0.229091},
 	        {-0.409091,0.886364,0.0},
 	        {-0.254545,0.886364,-0.454545},
 	        {-0.454545,0.886364,-0.254545},
 	        {-0.454545,0.886364,0.0},
 	        {-0.254545,0.772727,-0.454545},
 	        {-0.454545,0.772727,-0.254545},
 	        {-0.454545,0.772727,0.0},
 	        {-0.409091,0.772727,0.229091},
 	        {-0.229091,0.772727,0.409091},
 	        {0.0,0.772727,0.409091},
 	        {-0.409091,0.886364,0.229091},
 	        {-0.229091,0.886364,0.409091},
 	        {0.0,0.886364,0.409091},
 	        {-0.454545,0.886364,0.254545},
 	        {-0.254545,0.886364,0.454545},
 	        {0.0,0.886364,0.454545},
 	        {-0.454545,0.772727,0.254545},
 	        {-0.254545,0.772727,0.454545},
 	        {0.0,0.772727,0.454545},
 	        {0.229091,0.772727,0.409091},
 	        {0.409091,0.772727,0.229091},
 	        {0.229091,0.886364,0.409091},
 	        {0.409091,0.886364,0.229091},
 	        {0.254545,0.886364,0.454545},
 	        {0.454545,0.886364,0.254545},
 	        {0.254545,0.772727,0.454545},
 	        {0.454545,0.772727,0.254545},
 	        {0.454545,0.545455,0.0},
 	        {0.454545,0.545455,-0.254545},
 	        {0.254545,0.545455,-0.454545},
 	        {0.0,0.545455,-0.454545},
 	        {0.454545,0.272727,0.0},
 	        {0.454545,0.272727,-0.254545},
 	        {0.254545,0.272727,-0.454545},
 	        {0.0,0.272727,-0.454545},
 	        {0.318182,0.0454545,0.0},
 	        {0.318182,0.0454545,-0.178182},
 	        {0.178182,0.0454545,-0.318182},
 	        {0.0,0.0454545,-0.318182},
 	        {-0.254545,0.545455,-0.454545},
 	        {-0.454545,0.545455,-0.254545},
 	        {-0.454545,0.545455,0.0},
 	        {-0.254545,0.272727,-0.454545},
 	        {-0.454545,0.272727,-0.254545},
 	        {-0.454545,0.272727,0.0},
 	        {-0.178182,0.0454545,-0.318182},
 	        {-0.318182,0.0454545,-0.178182},
 	        {-0.318182,0.0454545,0.0},
 	        {-0.454545,0.545455,0.254545},
 	        {-0.254545,0.545455,0.454545},
 	        {0.0,0.545455,0.454545},
 	        {-0.454545,0.272727,0.254545},
 	        {-0.254545,0.272727,0.454545},
 	        {0.0,0.272727,0.454545},
 	        {-0.318182,0.0454545,0.178182},
 	        {-0.178182,0.0454545,0.318182},
 	        {0.0,0.0454545,0.318182},
 	        {0.254545,0.545455,0.454545},
 	        {0.454545,0.545455,0.254545},
 	        {0.254545,0.272727,0.454545},
 	        {0.454545,0.272727,0.254545},
 	        {0.178182,0.0454545,0.318182},
 	        {0.318182,0.0454545,0.178182},
 	        {0.545455,0.0454545,0.0},
 	        {0.545455,0.0454545,-0.305455},
 	        {0.305455,0.0454545,-0.545455},
 	        {0.0,0.0454545,-0.545455},
 	        {0.727273,0.136364,0.0},
 	        {0.727273,0.136364,-0.407273},
 	        {0.407273,0.136364,-0.727273},
 	        {0.0,0.136364,-0.727273},
 	        {0.909091,0.136364,0.0},
 	        {0.909091,0.136364,-0.509091},
 	        {0.509091,0.136364,-0.909091},
 	        {0.0,0.136364,-0.909091},
 	        {-0.305455,0.0454545,-0.545455},
 	        {-0.545455,0.0454545,-0.305455},
 	        {-0.545455,0.0454545,0.0},
 	        {-0.407273,0.136364,-0.727273},
 	        {-0.727273,0.136364,-0.407273},
 	        {-0.727273,0.136364,0.0},
 	        {-0.509091,0.136364,-0.909091},
 	        {-0.909091,0.136364,-0.509091},
 	        {-0.909091,0.136364,0.0},
 	        {-0.545455,0.0454545,0.305455},
 	        {-0.305455,0.0454545,0.545455},
 	        {0.0,0.0454545,0.545455},
 	        {-0.727273,0.136364,0.407273},
 	        {-0.407273,0.136364,0.727273},
 	        {0.0,0.136364,0.727273},
 	        {-0.909091,0.136364,0.509091},
 	        {-0.509091,0.136364,0.909091},
 	        {0.0,0.136364,0.909091},
 	        {0.305455,0.0454545,0.545455},
 	        {0.545455,0.0454545,0.305455},
 	        {0.407273,0.136364,0.727273},
 	        {0.727273,0.136364,0.407273},
 	        {0.509091,0.136364,0.909091},
 	        {0.909091,0.136364,0.509091},
 	        {1.0,0.136364,0.0},
 	        {1.0,0.136364,-0.56},
 	        {0.56,0.136364,-1.0},
 	        {0.0,0.136364,-1.0},
 	        {1.0,0.0909091,0.0},
 	        {1.0,0.0909091,-0.56},
 	        {0.56,0.0909091,-1.0},
 	        {0.0,0.0909091,-1.0},
 	        {0.909091,0.0909091,0.0},
 	        {0.909091,0.0909091,-0.509091},
 	        {0.509091,0.0909091,-0.909091},
 	        {0.0,0.0909091,-0.909091},
 	        {-0.56,0.136364,-1.0},
 	        {-1.0,0.136364,-0.56},
 	        {-1.0,0.136364,0.0},
 	        {-0.56,0.0909091,-1.0},
 	        {-1.0,0.0909091,-0.56},
 	        {-1.0,0.0909091,0.0},
 	        {-0.509091,0.0909091,-0.909091},
 	        {-0.909091,0.0909091,-0.509091},
 	        {-0.909091,0.0909091,0.0},
 	        {-1.0,0.136364,0.56},
 	        {-0.56,0.136364,1.0},
 	        {0.0,0.136364,1.0},
 	        {-1.0,0.0909091,0.56},
 	        {-0.56,0.0909091,1.0},
 	        {0.0,0.0909091,1.0},
 	        {-0.909091,0.0909091,0.509091},
 	        {-0.509091,0.0909091,0.909091},
 	        {0.0,0.0909091,0.909091},
 	        {0.56,0.136364,1.0},
 	        {1.0,0.136364,0.56},
 	        {0.56,0.0909091,1.0},
 	        {1.0,0.0909091,0.56},
 	        {0.509091,0.0909091,0.909091},
 	        {0.909091,0.0909091,0.509091},
 	        {0.727273,0.0909091,0.0},
 	        {0.727273,0.0909091,-0.407273},
 	        {0.407273,0.0909091,-0.727273},
 	        {0.0,0.0909091,-0.727273},
 	        {0.545455,0.0,0.0},
 	        {0.545455,0.0,-0.305455},
 	        {0.305455,0.0,-0.545455},
 	        {0.0,0.0,-0.545455},
 	        {0.318182,0.0,0.0},
 	        {0.318182,0.0,-0.178182},
 	        {0.178182,0.0,-0.318182},
 	        {0.0,0.0,-0.318182},
 	        {-0.407273,0.0909091,-0.727273},
 	        {-0.727273,0.0909091,-0.407273},
 	        {-0.727273,0.0909091,0.0},
 	        {-0.305455,0.0,-0.545455},
 	        {-0.545455,0.0,-0.305455},
 	        {-0.545455,0.0,0.0},
 	        {-0.178182,0.0,-0.318182},
 	        {-0.318182,0.0,-0.178182},
 	        {-0.318182,0.0,0.0},
 	        {-0.727273,0.0909091,0.407273},
 	        {-0.407273,0.0909091,0.727273},
 	        {0.0,0.0909091,0.727273},
 	        {-0.545455,0.0,0.305455},
 	        {-0.305455,0.0,0.545455},
 	        {0.0,0.0,0.545455},
 	        {-0.318182,0.0,0.178182},
 	        {-0.178182,0.0,0.318182},
 	        {0.0,0.0,0.318182},
 	        {0.407273,0.0909091,0.727273},
 	        {0.727273,0.0909091,0.407273},
 	        {0.305455,0.0,0.545455},
 	        {0.545455,0.0,0.305455},
 	        {0.178182,0.0,0.318182},
 	        {0.318182,0.0,0.178182},
 	        {0.272727,0.0454545,0.0},
 	        {0.272727,0.0454545,-0.152727},
 	        {0.152727,0.0454545,-0.272727},
 	        {0.0,0.0454545,-0.272727},
 	        {0.409091,0.272727,0.0},
 	        {0.409091,0.272727,-0.229091},
 	        {0.229091,0.272727,-0.409091},
 	        {0.0,0.272727,-0.409091},
 	        {0.409091,0.545455,0.0},
 	        {0.409091,0.545455,-0.229091},
 	        {0.229091,0.545455,-0.409091},
 	        {0.0,0.545455,-0.409091},
 	        {-0.152727,0.0454545,-0.272727},
 	        {-0.272727,0.0454545,-0.152727},
 	        {-0.272727,0.0454545,0.0},
 	        {-0.229091,0.272727,-0.409091},
 	        {-0.409091,0.272727,-0.229091},
 	        {-0.409091,0.272727,0.0},
 	        {-0.229091,0.545455,-0.409091},
 	        {-0.409091,0.545455,-0.229091},
 	        {-0.409091,0.545455,0.0},
 	        {-0.272727,0.0454545,0.152727},
 	        {-0.152727,0.0454545,0.272727},
 	        {0.0,0.0454545,0.272727},
 	        {-0.409091,0.272727,0.229091},
 	        {-0.229091,0.272727,0.409091},
 	        {0.0,0.272727,0.409091},
 	        {-0.409091,0.545455,0.229091},
 	        {-0.229091,0.545455,0.409091},
 	        {0.0,0.545455,0.409091},
 	        {0.152727,0.0454545,0.272727},
 	        {0.272727,0.0454545,0.152727},
 	        {0.229091,0.272727,0.409091},
 	        {0.409091,0.272727,0.229091},
 	        {0.229091,0.545455,0.409091},
 	        {0.409091,0.545455,0.229091},
 	        {-0.454545,0.704545,0.0},
 	        {-0.454545,0.704545,-0.0454545},
 	        {-0.454545,0.772727,-0.0454545},
 	        {-0.772727,0.863636,0.0},
 	        {-0.772727,0.863636,-0.0454545},
 	        {-0.818182,0.954545,-0.0454545},
 	        {-0.818182,0.954545,0.0},
 	        {-0.772727,0.522727,0.0},
 	        {-0.772727,0.522727,-0.0454545},
 	        {-0.909091,0.477273,-0.0454545},
 	        {-0.909091,0.477273,0.0},
 	        {-0.409091,0.363636,0.0},
 	        {-0.409091,0.363636,-0.0454545},
 	        {-0.409091,0.295455,-0.0454545},
 	        {-0.409091,0.295455,0.0},
 	        {-0.454545,0.772727,0.0454545},
 	        {-0.454545,0.704545,0.0454545},
 	        {-0.818182,0.954545,0.0454545},
 	        {-0.772727,0.863636,0.0454545},
 	        {-0.909091,0.477273,0.0454545},
 	        {-0.772727,0.522727,0.0454545},
 	        {-0.409091,0.295455,0.0454545},
 	        {-0.409091,0.363636,0.0454545}
 	};
 
 
 	
 }
