 import java.awt.Color;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.ColoringAttributes;
 import javax.media.j3d.GeometryArray;
 import javax.media.j3d.Node;
 import javax.media.j3d.PointArray;
 import javax.media.j3d.PointAttributes;
 import javax.media.j3d.PolygonAttributes;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.TriangleFanArray;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Tuple2f;
 import javax.vecmath.Vector2f;
 
 
 public class ConvexPolygon extends PhysicsObject {
     private Vector2f[] vertexCache;
     private Vector2f[] normalCache;
     private Vector2f[] baseVertices;
     
     public ConvexPolygon(float mass, float positionX, float positionY, float velocityX, float velocityY, float orientation, float angularVelocity, float width, Color3f color, Vector2f[] vertices) {
         super(mass, positionX, positionY, velocityX, velocityY, orientation, angularVelocity);
         
         if (width <= 0) {
             throw new IllegalArgumentException();
         }
         
         baseVertices = getVerticesForDesiredWidth(vertices, width);
         
         if (!isConvex()) {
             throw new IllegalArgumentException();
         }
         
         // Center of mass should be same as geometric center assuming uniform density.
         centerOfMass.x = centerOfMass.y = 0;
         momentOfInertia = getMomentOfInertia(baseVertices);
         TG.addChild(createShape(width, color));
     }
     
     public ConvexPolygon(float mass, Tuple2f position, Tuple2f velocity, float orientation, float angularVelocity, float width, Color3f color, Vector2f[] vertices) {
         this(mass, position.x, position.y, velocity.x, velocity.y, orientation, angularVelocity, width, color, vertices);
     }
     
     public void clearCaches() {
         vertexCache = null;
         normalCache = null;
     }
 
     public Vector2f[] getVertices() {
         if (vertexCache == null) {
             vertexCache = new Vector2f[baseVertices.length];
             for (int i = 0; i < baseVertices.length; i++) {
                 float tmpX = baseVertices[i].x;
                 float tmpY = baseVertices[i].y;
                 vertexCache[i] = new Vector2f();
                 vertexCache[i].x = (float)(Math.cos(orientation) * tmpX - Math.sin(orientation) * tmpY) + position.x;
                 vertexCache[i].y = (float)(Math.sin(orientation) * tmpX + Math.cos(orientation) * tmpY) + position.y;
             }
         }
         return vertexCache;
     }
     
     public Vector2f[] getNormals() {
         if (normalCache == null) {
             Vector2f[] vertices = getVertices();
             normalCache = new Vector2f[vertices.length];
 
             for (int i = 0; i < vertices.length; i++) {
                 normalCache[i] = new Vector2f();
                 normalCache[i].scaleAdd(-1, vertices[i], vertices[(i+1)%vertices.length]);
                 normalCache[i].normalize();
                 float tmp = normalCache[i].x;
                 normalCache[i].x = normalCache[i].y;
                 normalCache[i].y = -tmp;
             }
         }
         return normalCache;
     }
     
     private boolean isConvex() {
         // If the angle between all adjacent pairs of lines in the polygon
         // is <= 180 degrees, the polygon is convex.
         for (int i = 0; i < baseVertices.length - 1; i++) {
             Vector2f a = new Vector2f(baseVertices[i]);
             a.sub(baseVertices[i + 1]);
             
             Vector2f b = new Vector2f(baseVertices[(i + 2) % baseVertices.length]);
             b.sub(baseVertices[i + 1]);
             
             if (a.angle(b) > Math.PI) {
                 return false;
             }
         }
         
         return true;
     }
 
     private Vector2f[] getVerticesForDesiredWidth(final Vector2f[] vertices,
             final float idealWidth) {
         // Scale the vertices so that the distance between the most distant pair
         // matches the ideal width.
         final float scale = idealWidth / getActualWidth(vertices);
         final Vector2f[] scaledVertices = new Vector2f[vertices.length];
         
         for (int i = 0; i < vertices.length; i++) {
             final Vector2f temp = new Vector2f();
             temp.x = scale * vertices[i].x;
             temp.y = scale * vertices[i].y;
             scaledVertices[i] = temp;
         }
         
         // Center the properly scaled vertices about origin
         final Vector2f scaledCentroid = getCentroid(scaledVertices);
         
         for (int i = 0; i < vertices.length; i++) {
             scaledVertices[i].sub(scaledCentroid);
         }
         
         return scaledVertices;
     }
 
     private float getActualWidth(final Vector2f[] vertices) {
         // Find largest distance between pairs of vertices. 
         float actualWidthSquared = -1;
         for (int i = 0; i < vertices.length - 1; i++) {
             for (int j = i + 1; j < vertices.length; j++) {
                 final float xDiff = vertices[i].x - vertices[j].x;
                 final float yDiff = vertices[i].y - vertices[j].y;
                 final float widthSquared = xDiff * xDiff + yDiff * yDiff;
                 if (actualWidthSquared < widthSquared) {
                     actualWidthSquared = widthSquared;
                 }
             }
         }
         
         return (float) Math.sqrt(actualWidthSquared);
     }
 
     private Vector2f getCentroid(Vector2f[] vertices) {
         final Vector2f centroid = new Vector2f(0, 0);
         
         // http://en.wikipedia.org/wiki/Centroid#Centroid_of_polygon
         for (int i = 0; i < vertices.length; i++) {
             final float xi = vertices[i].x;
             final float xi_1 = vertices[(i + 1) % vertices.length].x;
             final float yi = vertices[i].y;
             final float yi_1 = vertices[(i + 1) % vertices.length].y;
             final float term = (xi * yi_1 - xi_1 * yi);
             
             centroid.x += (xi + xi_1) * term;
             centroid.y += (yi + yi_1) * term;
         }
         
         centroid.scale(1 / (6 * getSignedArea(vertices)));
         
         return centroid;
     }
 
     private float getSignedArea(Vector2f[] vertices) {
         // http://en.wikipedia.org/wiki/Centroid#Centroid_of_polygon
         float doubledArea = 0; 
         
         for (int i = 0; i < vertices.length; i++) {
             doubledArea += vertices[i].x * vertices[(i + 1) % vertices.length].y -
                     vertices[(i + 1) % vertices.length].x * vertices[i].y;
         }
         
         return doubledArea / 2;
     }
     
     private float getMomentOfInertia(Vector2f[] vertices) {
         // This method may only work for star-shaped polygons whose axis of
         // rotation is through the origin, under assumption that mass is
         // uniformly distributed. A convex polygon with its center of mass
         // located at the origin satisfies these requirements.
         
         // http://en.wikipedia.org/wiki/List_of_moments_of_inertia
         // http://mathoverflow.net/questions/73556/calculating-moment-of-inertia-in-2d-planar-polygon
         
         float numerator = 0;
         float denominator = 0;
        float vi_lengthSquared = vertices[0].lengthSquared();
         
         for (int i = 0; i < vertices.length - 1; i++) {
             final Vector2f vi0 = vertices[i];
             final Vector2f vi1 = vertices[i + 1];
             
             final float commonTerm = vi0.x * vi1.y - vi1.x * vi0.y;
             denominator += commonTerm;
             
            float numeratorTerm = vi_lengthSquared + vi0.dot(vi1);
            vi_lengthSquared = vi1.lengthSquared();
            numeratorTerm += vi_lengthSquared;
             numerator += numeratorTerm * commonTerm;
         }
         
         return (mass / 6) * (numerator / denominator);
     }
 


     private Node createShape(float width, Color3f color) {
         TriangleFanArray geometry = new TriangleFanArray(baseVertices.length, GeometryArray.COORDINATES, new int[] {baseVertices.length});
         for (int i = 0; i < baseVertices.length; i++) {
             geometry.setCoordinate(i, new Point3f(baseVertices[i].x, baseVertices[i].y, 0));
         }
 
         PointArray centerOfMassGeometry = new PointArray(1, GeometryArray.COORDINATES);
         centerOfMassGeometry.setCoordinate(0, new Point3f(centerOfMass.x, centerOfMass.y, 0));
         
         BranchGroup root = new BranchGroup();
         if (color == null)
             color = new Color3f(Color.getHSBColor((float)Math.random(), 1, 1));
         Appearance appearance = new Appearance();
         appearance.setColoringAttributes(new ColoringAttributes(color, ColoringAttributes.FASTEST));
         PolygonAttributes polyAttr = new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0);
         appearance.setPolygonAttributes(polyAttr);
         root.addChild(new Shape3D(geometry, appearance));
         
         appearance = new Appearance();
         appearance.setPointAttributes(new PointAttributes(4, true));
         root.addChild(new Shape3D(centerOfMassGeometry, appearance));
     
         return root;
     }
 }
