 package de.tum.in.cindy3dplugin;
 
 import java.awt.Color;
 import java.util.Hashtable;
 
 /**
  * Interface every Cindy3D viewer must implement.
  */
 public interface Cindy3DViewer {
 	/**
 	 * Total number of supported lights.
 	 */
 	public static final int MAX_LIGHTS = 8;
 
 	/**
 	 * Topology on a grid-based mesh.
 	 */
 	public enum MeshTopology {
 		/**
 		 * Open topology.
 		 */
 		OPEN,
 		/**
 		 * Topology with closed rows.
 		 */
 		CLOSE_ROWS,
 		/**
 		 * Topology with closed columns.
 		 */
 		CLOSE_COLUMNS,
 		/**
 		 * Topology with closed columns and rows.
 		 */
 		CLOSE_BOTH
 	}
 
 	/**
 	 * Type of auto-generated mesh normals.
 	 */
 	public enum NormalType {
 		/**
 		 * One normal per vertex.
 		 */
 		PER_VERTEX,
 		/**
 		 * One normal per grid face.
 		 */
 		PER_FACE
 	}
 
 	/**
 	 * Marks the beginning of a new scene.
 	 */
 	void begin();
 
 	/**
 	 * Finishes and displays the scene.
 	 */
 	void end();
 
 	/**
 	 * Shuts down the viewer
 	 */
 	void shutdown();
 
 	/**
 	 * Adds a point to the scene.
 	 * 
 	 * @param x
 	 *            x coordinate
 	 * @param y
 	 *            y coordinate
 	 * @param z
 	 *            z coordinate
 	 * @param appearance
 	 *            appearance of the point
 	 */
 	void addPoint(double x, double y, double z, AppearanceState appearance);
 
 	/**
 	 * Adds a circle to the scene.
 	 * 
 	 * @param cx
 	 *            x coordinate of center
 	 * @param cy
 	 *            y coordinate of center
 	 * @param cz
 	 *            z coordinate of center
 	 * @param nx
 	 *            x component of normal vector
 	 * @param ny
 	 *            y component of normal vector
 	 * @param nz
 	 *            z component of normal vector
 	 * @param radius
 	 *            radius of the circle
 	 * @param appearance
 	 *            appearance of the circle
 	 */
 	void addCircle(double cx, double cy, double cz, double nx, double ny,
 			double nz, double radius, AppearanceState appearance);
 
 	/**
 	 * Adds a sphere to the scene.
 	 * 
 	 * @param cx
 	 *            x coordinate of center
 	 * @param cy
 	 *            y coordinate of center
 	 * @param cz
 	 *            z coordinate of center
 	 * @param radius
 	 *            radius of the sphere
 	 * @param appearance
 	 *            appearance of the sphere
 	 */
 	void addSphere(double cx, double cy, double cz, double radius,
 			AppearanceState appearance);
 
 	/**
 	 * Adds a line segment to the scene.
 	 * 
 	 * @param x1
 	 *            x coordinate of first point
 	 * @param y1
 	 *            y coordinate of first point
 	 * @param z1
 	 *            z coordinate of first point
 	 * @param x2
 	 *            x coordinate of second point
 	 * @param y2
 	 *            y coordinate of second point
 	 * @param z2
 	 *            z coordinate of second point
 	 * @param appearance
 	 *            appearance of the line segment
 	 */
 	void addSegment(double x1, double y1, double z1, double x2, double y2,
 			double z2, AppearanceState appearance);
 
 	/**
 	 * Adds a line to the scene.
 	 * 
 	 * The first and second points must be arbitrary but different points on the
 	 * line.
 	 * 
 	 * @param x1
 	 *            x coordinate of first point
 	 * @param y1
 	 *            y coordinate of first point
 	 * @param z1
 	 *            z coordinate of first point
 	 * @param x2
 	 *            x coordinate of second point
 	 * @param y2
 	 *            y coordinate of second point
 	 * @param z2
 	 *            z coordinate of second point
 	 * @param appearance
 	 *            appearance of the line
 	 */
 	void addLine(double x1, double y1, double z1, double x2, double y2,
 			double z2, AppearanceState appearance);
 
 	/**
 	 * Adds a ray (half-line) to the scene.
 	 * 
 	 * The first point is the ray's origin. The second point must be an
 	 * arbitrary point on the ray which differs from the first point.
 	 * 
 	 * @param x1
 	 *            x coordinate of first point
 	 * @param y1
 	 *            y coordinate of first point
 	 * @param z1
 	 *            z coordinate of first point
 	 * @param x2
 	 *            x coordinate of second point
 	 * @param y2
 	 *            y coordinate of second point
 	 * @param z2
 	 *            z coordinate of second point
 	 * @param appearance
 	 *            appearance of the ray
 	 */
 	void addRay(double x1, double y1, double z1, double x2, double y2,
 			double z2, AppearanceState appearance);
 
 	/**
 	 * Adds a strip of connected lines to the scene.
 	 * 
 	 * @param vertices
 	 *            array of points (coordinate arrays) to connect
 	 * @param appearance
 	 *            appearance of the line strip
 	 * @param closed
 	 *            <code>true</code> if the last point should be connected to the
 	 *            first
 	 */
 	void addLineStrip(double[][] vertices, AppearanceState appearance,
 			boolean closed);
 
 	/**
 	 * Adds a filled polygon to the scene.
 	 * 
 	 * @param vertices
 	 *            array of at least 3 points (coordinate arrays)
 	 * @param normals
 	 *            array of normals for the points. If <code>null</code>, the
 	 *            vector orthogonal to the plane defined by the first three
 	 *            vertices is assumed as a common normal for all vertices.
 	 * @param appearance
 	 *            appearance of the polygon
 	 */
 	void addPolygon(double[][] vertices, double[][] normals,
 			AppearanceState appearance);
 
 	/**
 	 * Adds a grid-based mesh with user-supplied normals to the scene.
 	 * 
 	 * @param rows
 	 *            number of rows of vertices
 	 * @param columns
 	 *            number of columns of vertices
 	 * @param vertices
 	 *            array of vertices, in row-major order
 	 * @param normals
 	 *            array of per-vertex normals, in row-major order
 	 * @param topology
 	 *            topology of the mesh
 	 * @param appearance
 	 *            appearance of the mesh
 	 */
 	void addMesh(int rows, int columns, double[][] vertices,
 			double[][] normals, MeshTopology topology,
 			AppearanceState appearance);
 
 	/**
 	 * Adds a grid-based mesh with auto-generated normals to the scene.
 	 * 
 	 * @param rows
 	 *            number of rows of vertices
 	 * @param columns
 	 *            number of columns of vertices
 	 * @param vertices
 	 *            array of vertices, in row-major order
 	 * @param normalType
 	 *            type of normals to generate
 	 * @param topology
 	 *            topology of the mesh
 	 * @param appearance
 	 *            appearance of the mesh
 	 */
 	void addMesh(int rows, int columns, double[][] vertices,
 			NormalType normalType, MeshTopology topology,
 			AppearanceState appearance);
 
 	/**
 	 * Sets the background color of the scene.
 	 * 
 	 * @param color
 	 *            new background color
 	 */
 	void setBackgroundColor(Color color);
 
 	/**
 	 * Sets the camera's depth range.
 	 * 
 	 * All objects with distance below <code>near</code> or above
 	 * <code>far</code> are not displayed.
 	 * 
 	 * @param near
 	 *            near distance
 	 * @param far
 	 *            far distance
 	 */
 	void setDepthRange(double near, double far);
 
 	/**
 	 * Sets light source parameters.
 	 * 
 	 * @param light
 	 *            light index, between 0 (inclusive) and {@value #MAX_LIGHTS}
 	 *            (exclusive)
 	 * @param info
 	 *            Light parameters to set
 	 */
 	void setLight(int light, LightModificationInfo info);
 
 	/**
 	 * Disables a light source.
 	 * 
 	 * @param light
 	 *            light index, between 0 (inclusive) and {@value #MAX_LIGHTS}
 	 *            (exclusive)
 	 */
 	void disableLight(int light);
 
 	/**
 	 * Sets render hints.
 	 * 
 	 * The meaning of the render hints is implementation-defined.
 	 * 
 	 * @param hints
 	 *            Render hints
 	 */
 	void setRenderHints(Hashtable<String, Object> hints);
 
 	/**
 	 * Positions the camera.
 	 * 
 	 * @param eyeX
 	 *            x coordinate of eye point
 	 * @param eyeY
 	 *            y coordinate of eye point
 	 * @param eyeZ
 	 *            z coordinate of eye point
 	 * @param lookAtX
 	 *            x coordinate of look at point
 	 * @param lookAtY
 	 *            y coordinate of look at point
 	 * @param lookAtZ
 	 *            z coordinate of look at point
 	 * @param upX
 	 *            x coordinate of up vector
 	 * @param upY
 	 *            y coordinate of up vector
 	 * @param upZ
 	 *            z coordinate of up vector
 	 */
 	void setCamera(double eyeX, double eyeY, double eyeZ, double lookAtX,
 			double lookAtY, double lookAtZ, double upX, double upY, double upZ);
 
 	/**
 	 * Sets the camera's field of view.
 	 * 
 	 * @param fieldOfView
	 *            the field of view to set
 	 */
 	void setFieldOfView(double fieldOfView);
 }
