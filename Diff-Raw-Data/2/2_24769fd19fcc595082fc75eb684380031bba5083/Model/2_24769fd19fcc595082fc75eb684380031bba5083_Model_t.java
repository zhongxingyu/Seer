 package core;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.List;
 
 /**
  * Represents a bunch of polygons put together to make one or more shapes with 3 dimensions
  * @author Oliver Greenaway
  *
  */
 public class Model {
 
 	private List<Polygon> polygons;
 	private Light ambient, intensity;
 	private Point3D lightDirection;
 
 	/**
 	 * Creates a new model containing the given polygons and light levels
 	 * @param polygons List of polygons to mkae up the model
 	 * @param ambient The level of ambient light
 	 * @param intensity The intensity of the light
 	 * @param lightDirection The direction of the light
 	 */
 	public Model(List<Polygon> polygons, Light ambient, Light intensity,
 			Point3D lightDirection) {
 		this.polygons = polygons;
 		this.lightDirection = lightDirection;
 		this.ambient = ambient;
 		this.intensity = intensity;
 	}
 
 	/**
 	 * Manipulates what will be drawn to the screen and renders the model to the graphics object
 	 * @param g Graphics object
 	 * @param width Width of the canvas
 	 * @param height Height of the canvas
 	 */
 	public void draw(Graphics2D g, int width, int height) {
 		Color[][] colors = new Color[width][height];
 		double[][] depth = new double[width][height];
 		for (int i = 0; i < depth.length; i++) {
 			for (int j = 0; j < depth[i].length; j++) {
 				depth[i][j] = Double.MAX_VALUE;
 				colors[i][j] = null;
 			}
 		}
 		for (Polygon p : polygons) {
 			if (p.getNormal().getZ() < 0) {
 				int minY = Integer.MAX_VALUE;
 				for (Point3D point : p.getPoints()) {
 					minY = (int) Math.min(minY, Math.round(point.getY()));
 				}
 				double[][] edgeList = p.getEdgeList();
 				for (int i = 0; i < edgeList.length && i+minY < depth[0].length; i++) {
 					int x = (int) Math.round(edgeList[i][0]);
 					double z = edgeList[i][1];
 					double gradiantZ = (edgeList[i][3] - edgeList[i][1])
 							/ (edgeList[i][2] - edgeList[i][0]);
 					for (int j = x; j <= (int) Math.round(edgeList[i][2]) && j < depth.length; j++) {
 						if (z < depth[j][i + minY]) {
 							depth[j][i + minY] = z;
 							colors[j][i + minY] = p.getColor(lightDirection,
 									ambient, intensity);
 						}
 						z += gradiantZ;
 					}
 				}
 			}
 		}
 		for (int i = 0; i < colors.length; i++) {
 			for (int j = 0; j < colors[i].length; j++) {
 				if (colors[i][j] != null) {
 					g.setColor(colors[i][j]);
 					g.fillRect(i, j, 1, 1);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Rotate the model around its x axis by the given radians
 	 * @param radians Radians to be rotated
 	 */
 	public void rotateX(double radians) {
 		for (Polygon polygon : polygons) {
 			for (Point3D point : polygon.getPoints()) {
 				point.rotateX(radians);
 			}
 		}
 		lightDirection.rotateX(radians);
 	}
 
 	/**
 	 * Rotate the model around its Y axis by the given radians
 	 * @param radians Radians to be rotated
 	 */
 	public void rotateY(double radians) {
 		for (Polygon polygon : polygons) {
 			for (Point3D point : polygon.getPoints()) {
 				point.rotateY(radians);
 			}
 		}
 		lightDirection.rotateY(radians);
 	}
 
 	/**
 	 * Centers the image within a given bounds
 	 * @param width Width of the canvas
 	 * @param height Height of the canvas
 	 */
 	public void center(int width, int height) {
 		double minX = Double.MAX_VALUE;
 		double maxX = Double.MIN_VALUE;
 		double minY = Double.MAX_VALUE;
 		double maxY = Double.MIN_VALUE;
 		double minZ = Double.MAX_VALUE;
 		double maxZ = Double.MIN_VALUE;
 		for (Polygon polygon : polygons) {
 			for (Point3D point : polygon.getPoints()) {
 				minX = Math.min(minX, point.getX());
 				maxX = Math.max(maxX, point.getX());
 				minY = Math.min(minY, point.getY());
 				maxY = Math.max(maxY, point.getY());
 				minZ = Math.min(minZ, point.getZ());
 				maxZ = Math.max(maxZ, point.getZ());
 			}
 		}
 		double diffX = maxX - minX;
 		double diffY = maxY - minY;
 		if (diffX > width || diffY > height) {
 			double scaleFactor = Math.min(height / diffY, width / diffX);
 			for (Polygon polygon : polygons) {
 				for (Point3D point : polygon.getPoints()) {
 					point.scale(scaleFactor);
 				}
 			}
 			//lightDirection.scale(scaleFactor);
 			center(width, height);
 			return;
 		}
 		double centerX = (maxX + minX) / 2;
 		double centerY = (maxY + minY) / 2;
 		double centerZ = (maxZ + minZ) / 2;
 		double transX = ((double) width) / 2 - centerX;
 		double transY = ((double) height) / 2 - centerY;
 		double transZ = 0 - centerZ;
 		for (Polygon polygon : polygons) {
 			for (Point3D point : polygon.getPoints()) {
 				point.translate(transX, transY, transZ);
 			}
 		}
 		//lightDirection.translate(transX, transY, transZ);
 	}
 
 	/**
 	 * Reset the model to its original position
 	 */
 	public void reset() {
 		for (Polygon polygon : polygons) {
 			for (Point3D point : polygon.getPoints()) {
 				point.reset();
 			}
 		}
		lightDirection.reset();
 	}
 
 }
