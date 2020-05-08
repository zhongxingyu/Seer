 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 
 	public class Drawing {
 		MainPanel container = null;
 		public int panelWidth;
 		public int panelHeight;
 		BufferedReader incoming = null;
 		PrintWriter outgoing = null;
 		Color currentColor = Color.BLACK;
 		boolean firstLayerAdded = false;
 		boolean mouseDown = false;
 		int lastX;
 		int lastY;
 		int lineWidth;
 		Curve currentCurve = new Curve(-1, -1);
 		LinkedList<Curve> curves = new LinkedList<Curve>();
 		LinkedList<Curve> newCurves = new LinkedList<Curve>();
 		Layer[] layers = new Layer[1];
 		int nextLayerID = 0;
 
 		public Drawing() {
 			layers[0] = new Layer(1, 1, "Layer 0");
 		}
 
 		public boolean layerExists(String name) {
 			for (Layer layer: layers) {
 				if (name.equals(layer.name)) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		public void swapLayers(int a, int b) {
 			Layer temp = layers[b];
 			layers[b] = layers[a];
 			layers[a] = temp;
 		}
 
 		public Layer insertLayer(int w, int h, String n, int insertionPoint) {
 			Layer[] newLayers = new Layer[layers.length+1];
 			newLayers[insertionPoint] = new Layer(w, h, n);
 			for (int i = 0; i < insertionPoint; i++) {
 				newLayers[i] = layers[i];
 			}
 			for (int i = insertionPoint + 1; i < newLayers.length; i++) {
 				newLayers[i] = layers[i-1]; 
 			}
 			layers = new Layer[newLayers.length];
 			System.arraycopy(newLayers, 0, layers, 0, newLayers.length);
 			nextLayerID++;
 			return layers[insertionPoint];
 		}
 
 		public Layer addLayer(int w, int h, String n) {
 			Layer[] newLayers = new Layer[layers.length+1];
 			System.arraycopy(layers, 0, newLayers, 0, layers.length);
 			layers = new Layer[newLayers.length];
 			System.arraycopy(newLayers, 0, layers, 0, newLayers.length);
 			layers[newLayers.length-1] = new Layer(w, h, n);
 			layers[newLayers.length-1].id = nextLayerID;
 			nextLayerID++;
 			return layers[newLayers.length-1];
 		}
 
 		public void drawCurve(Curve c, Graphics2D g) {
 			/*GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, c.coords.length);
        float[] points;
        polyline.moveTo(c.coords[0].x, c.coords[0].y);
        for (Coord coord : c.coords) {
        System.out.println("x = " + coord.x +"; y = " + coord.y);
        polyline.lineTo(coord.x, coord.y);
        }*/
 			Coord lastCoord = c.coords[0];
 			g.setBackground(new Color(0, 0, 0, 0));
 			for (Coord coord : c.coords) {
 				if (!coord.drawn) {
 					for (Coord lineCoord: lineBetween(lastCoord, coord)) {
 						if (c.lineWidth == 1 && !c.erase) {
 							g.drawRect(lineCoord.x, lineCoord.y, 0, 0);
 						} else if (!c.erase) {
 							g.fillOval(lineCoord.x-c.lineWidth/2, lineCoord.y-c.lineWidth/2, c.lineWidth, c.lineWidth);
 						} else {
 							g.setBackground(new Color(0, 0, 0, 0));
 							g.clearRect(lineCoord.x-c.lineWidth/2, lineCoord.y-c.lineWidth/2, c.lineWidth, c.lineWidth);
 						}
 					}
 					coord.drawn = true;
 				}
 				lastCoord = coord;
 			}
 		}
 
 		public Curve startNewCurve(int x, int y, int lineWidth, Color color, boolean erase, int layer) {
 			Curve newCurve = new Curve(x, y, lineWidth, color, layer, erase);
 			synchronized(curves) {
 				curves.add(newCurve);
 			}
 			//newCurves.add(newCurve);
 			return newCurve;
 		}
 
 		public void addToCurve(int x, int y, Curve c) {
 			c.addPoint(x, y);
 		}
 		public Color getColorAt(int x, int y) {
 			Color newColor = Color.WHITE;
 			Color layerColor;
 			int alpha;
 			for (Layer layer: layers) {
 				alpha = (layer.i.getRGB(x,y)>>24) & 0xff;
 				layerColor = new Color(layer.i.getRGB(x, y));
 				if (layer.visible && alpha != 0) {
 					newColor = layerColor;
 				}
 			}
 			return newColor;
 		}
 
 		public void resizeAllLayers(Dimension newSize) {
 			for (Layer l: layers) {
 				l.resize(newSize);
 			}
 		}
 		
		public LinkedList<Coord> lineBetween(Coord pointA, Coord pointB) {
 			Coord pt1;
 			Coord pt2;
 			if (pointA.x<pointB.x) {
 				pt1 = pointA;
 				pt2 = pointB;
 			} else {
 				pt1 = pointB;
 				pt2 = pointA;
 			}
 
 			LinkedList<Coord> coords = new LinkedList<Coord>();
 			coords.add(pt1);
 
 			if (pt1.x == pt2.x) {
 				if (pt1.y < pt2.y) for (int y = pt1.y+1; y <= pt2.y; y++) coords.add(new Coord(pt1.x, y));
 				else for (int y = pt1.y-1; y >= pt2.y; y--) coords.add(new Coord(pt1.x, y)); 
 				return coords;
 			}
 
 			double slope = (double)(pt2.y-pt1.y)/(double)(pt2.x-pt1.x);
 
 			double oldY = pt1.y;
 			double newY;
 
 			for (int x = pt1.x+1; x <= pt2.x; x++) {
 				if (x == pt2.x) newY = pt2.y;
 				else newY = oldY + slope;
 				if (Math.abs(slope)<1) {
 					coords.add(new Coord(x, (int)newY));
 				}
 				else {
 					if (slope > 0) {
 						for (int y = (int)oldY+1; y <= (int)newY; y++) {
 							coords.add(new Coord(x, y));
 						}
 					} else {
 						for (int y = (int)oldY-1; y >= (int)newY; y--) {
 							coords.add(new Coord(x, y));
 						}
 					}
 				}
 				oldY = newY;
 			}
 			return coords;
 		}
 	}
