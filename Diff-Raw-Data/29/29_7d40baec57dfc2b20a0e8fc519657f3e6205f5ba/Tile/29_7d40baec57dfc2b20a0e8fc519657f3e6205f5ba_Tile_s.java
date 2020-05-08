 package net.todd.games.boardgame;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.Geometry;
 import javax.media.j3d.GeometryArray;
 import javax.media.j3d.PolygonAttributes;
 import javax.media.j3d.QuadArray;
 import javax.media.j3d.Shape3D;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3f;
 
 public class Tile {
 
 	private final Shape3D shape;
 	private final int size;
 	private final int centerX;
 	private final int centerY;
 	private final int centerZ;
 	private final Color3f tileColor;
 
 	public Tile(int centerX, int centerY, int centerZ, int size, Color3f tileColor) {
 		this.centerX = centerX;
 		this.centerY = centerY;
 		this.centerZ = centerZ;
 		this.size = size;
 		this.tileColor = tileColor;
 		shape = new Shape3D();
 		shape.setGeometry(getGeometry());
 		shape.setAppearance(getAppearance());
 	}
 
 	private Appearance getAppearance() {
 		Appearance appearance = new Appearance();
 
 		PolygonAttributes polygonAttributes = new PolygonAttributes();
 		polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
 
 		appearance.setPolygonAttributes(polygonAttributes);
 
 		return appearance;
 	}
 
 	private Geometry getGeometry() {
 		Point3f[] points = new Point3f[4];
		points[0] = new Point3f(new float[] { centerX - (size / 2), centerY,
		        centerZ - (size / 2) });
		points[1] = new Point3f(new float[] { centerX + (size / 2), centerY,
		        centerZ - (size / 2) });
		points[2] = new Point3f(new float[] { centerX + (size / 2), centerY,
		        centerZ + (size / 2) });
		points[3] = new Point3f(new float[] { centerX - (size / 2), centerY,
		        centerZ + (size / 2) });
 
 		QuadArray quadArray = new QuadArray(points.length, GeometryArray.COORDINATES
		        | GeometryArray.COLOR_3);
 		quadArray.setCoordinates(0, points);
 
 		for (int i = 0; i < points.length; i++) {
 			quadArray.setColor(i, tileColor);
 		}
 
 		return quadArray;
 	}
 
 	public Shape3D getShape() {
 		return shape;
 	}
 
 }
