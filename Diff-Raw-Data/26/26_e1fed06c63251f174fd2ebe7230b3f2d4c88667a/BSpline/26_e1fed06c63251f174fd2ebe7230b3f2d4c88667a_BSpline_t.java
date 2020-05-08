 package cs4620.spline;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.media.opengl.GL2;
 import javax.vecmath.Tuple2f;
 import javax.vecmath.Vector2f;
 import javax.vecmath.Matrix4f;
 
 import org.yaml.snakeyaml.Yaml;
 
 import cs4620.util.Util;
 
 /*
  * The BSpline class. Takes a set of control points and produces
  * a set of vertices along the smooth curve based on an epsilon
  * criteria. Also computes the accumulated length of the curve at
  * each vertex, which is used for animation.
  */
 public class BSpline extends DiscreteCurve {
     protected boolean isClosed;
 
     protected Vector2f[] bezier = null;
     protected ArrayList<Vector2f> localControlPoints = new ArrayList<Vector2f>();
     protected float [] length_buffer;
     private float totLength = 0.0f;
    //TODO: include these global variable when copying over code
    private Vector2f endVertex;
    private Vector2f endNormal;
 
     // Workspaces for spline tesselation routines
     // (caution, not a thread safe programming practice)
 	private Vector2f[] bspPoints = new Vector2f[4];
 	private Vector2f[] bezPoints = new Vector2f[4];
     
     public BSpline(GL2 gl, boolean isClosed, float canvasSize) {
     	super(gl, canvasSize);
         this.isClosed = isClosed;
 		for (int i = 0; i < 4; i++) {
 			bspPoints[i] = new Vector2f();
 			bezPoints[i] = new Vector2f();
 		}
 }
 
     public ArrayList<Vector2f> getCtrlPts() {
     	return localControlPoints;
     }
 
     public void setCtrlPts( ArrayList<Vector2f> ctrlPts) {
     	localControlPoints = ctrlPts;
     }
 
     public boolean isClosed()
     {
     	return isClosed;
     }
 
     public void setClosed(boolean value)
     {
     	isClosed = value;
     }
 
     public float getTotalLength() {
     	return totLength;
     }
 
     public float[] getLengthBuffer() {
     	return length_buffer;
     }
     
 
     /*
      * Approximate a Bezier segment with a number of vertices, according to an appropriate
      * criterion for how many are needed.  The points on the curve are written into the
      * array outPoints, and the corresponding tangent vectors (not normalized) are written
      * into outTangents.  The last point (the 4th control point) is not included.
      */
     public void tessellate_bezier(Vector2f cp[], float epsilon, ArrayList<Vector2f> outPoints, ArrayList<Vector2f> outDerivs) {
     	 	
     	// TODO (Splines P1): Tesselate a bezier segment
     	tessellate_helper(cp, epsilon, outPoints, outDerivs, 0);
     }
     
     /*
      * Helper method for tesselate_bezier with a countdown
      */
    	private void tessellate_helper(Vector2f cp[], float epsilon, ArrayList<Vector2f> outPoints, ArrayList<Vector2f> outDerivs, int counter) {
    		Vector2f vec1 = new Vector2f(cp[1].x, cp[1].y);
    		vec1.sub(cp[0]);
    		Vector2f vec2 = new Vector2f(cp[3].x, cp[3].y);
    		vec2.sub(cp[0]);
   		float angle = vec1.angle(vec2);   		
    		
    		Vector2f normal = new Vector2f(-vec1.y, vec1.x);
    		normal.normalize();
    		
     	if (counter == 10 || angle <= epsilon/2) {
     		outPoints.add(new Vector2f(cp[0].x, cp[0].y));
     		outDerivs.add(normal);
	   		vec1 = new Vector2f(cp[3].x, cp[3].y);
	   		vec1.sub(cp[2]);  		
	   		
	   		normal = new Vector2f(-vec1.y, vec1.x);
	   		normal.normalize();
			endVertex = new Vector2f(cp[3].x, cp[3].y);
			endNormal = normal;
     	} else {
 	    	Vector2f p0, p1, p2, p3, p10, p20, p30, p11, p21, p12;
 	    	p10 = new Vector2f();
 	    	p20 = new Vector2f();
 	    	p30 = new Vector2f();
 	    	p11 = new Vector2f();
 	    	p21 = new Vector2f();
 	    	p12 = new Vector2f();
 	
 			p0 = new Vector2f(cp[0].x, cp[0].y);
 			p1 = new Vector2f(cp[1].x, cp[1].y);
 			p2 = new Vector2f(cp[2].x, cp[2].y);
 			p3 = new Vector2f(cp[3].x, cp[3].y);
 			p10.interpolate((Tuple2f)p0, ((Tuple2f)p1), 0.5f);
 			p11.interpolate((Tuple2f)p1, ((Tuple2f)p2), 0.5f);
 			p12.interpolate((Tuple2f)p2, ((Tuple2f)p3), 0.5f);
 			p20.interpolate((Tuple2f)p10, ((Tuple2f)p11), 0.5f);
 			p21.interpolate((Tuple2f)p11, ((Tuple2f)p12), 0.5f);
 			p30.interpolate((Tuple2f)p20, ((Tuple2f)p21), 0.5f);
 	    		
 	    	Vector2f[] lhs = {p0, p10, p20, p30};
 	    	Vector2f[] rhs = {p30, p21, p12, p3};
 	    	
 	    	tessellate_helper(lhs, epsilon, outPoints, outDerivs, counter+1);
 	    	tessellate_helper(rhs, epsilon, outPoints, outDerivs, counter+1);
     	}
     }
     
     
     /*
      * Approximate a single segment of a B-spline with a number of vertices, according to 
      * an appropriate criterion for how many are needed.  The points on the curve are written 
      * into the array outPoints, and the corresponding tangent vectors (not normalized) are 
      * written into outTangents.  The last point is not included.
      */
     public void tessellate_bspline(Vector2f bspPoints[], float epsilon, ArrayList<Vector2f> outPoints, ArrayList<Vector2f> outDerivs) {
     	// Strategy: convert the B-spline segment to a Bezier segment, then approximate that.
     	// TODO (Splines P1): Convert the B-spline control points to control points for an equivalent
     	// Bezier segment, then tesselate that segment, using the tesselate_bezier function.
     	
     	Vector2f p = bspPoints[0];
 		float a = p.x;
 		float b = p.y;
 		p = bspPoints[1];
 		float c = p.x;
 		float d = p.y;
 		p = bspPoints[2];
 		float e = p.x;
 		float f = p.y;
 		p = bspPoints[3];
 		float g = p.x;
 		float h = p.y;
 		
     	Vector2f bezCP[] = {new Vector2f(a/6 + (2*c)/3 + e/6, b/6 + (2*d)/3 + f/6), 
     						new Vector2f((2*c)/3 + e/3, (2*d)/3 + f/3),
     						new Vector2f(c/3 + (2*e)/3, d/3 + (2*f)/3), 
     						new Vector2f(c/6 + (2*e)/3 + g/6, d/6 + (2*f)/3 + h/6)};
     	
     	tessellate_bezier(bezCP, epsilon, outPoints, outDerivs);
     	
     }
     
     @Override
     public void build(GL2 gl, float[] controlPoints, float epsilon) {
     	// copy control points into list of vectors
     	ArrayList<Vector2f> cp = new ArrayList<Vector2f>();
     	int N = controlPoints.length / 2;
     	for (int i = 0; i < N; i++)
     		cp.add(new Vector2f(controlPoints[2*i], controlPoints[2*i+1]));
     	
     	// Use the vertices array list to store your computed vertices
     	ArrayList<Vector2f> vertices = new ArrayList<Vector2f>();
     	//TODO: added this normals list
 		ArrayList<Vector2f> normals = new ArrayList<Vector2f>();
     	
     	// For each segment of the spline, call tesselate_bspline to add its points
     	if (isClosed) {
 			// TODO: Splines Problem 2, Section 4:
         	// Compute Bezier control points for a closed curve. Put the computed vertices
     		// into the vertices ArrayList declared above.
     	} else {
 			// TODO: Splines Problem 1, Section 3.1:
         	// Compute Bezier control points for an open curve with boundary conditions.
     		// Put the computed vertices into the vertices ArrayList declared above.
     		for (int j = 0; j < cp.size()-1; j++) {
     			if (j < 1) {
 					bspPoints[1] = cp.get(j);
         			bspPoints[2] = cp.get(j+1);
         			bspPoints[3] = cp.get(j+2);
         			
         			Vector2f frontEnd = new Vector2f(bspPoints[1].x, bspPoints[1].y);
         			frontEnd.scale(2f);
         			frontEnd.sub(bspPoints[2]);
     				bspPoints[0] = frontEnd;
     			} else if (j == cp.size()-2) {
     				bspPoints[0] = cp.get(j-1);
         			bspPoints[1] = cp.get(j);
         			bspPoints[2] = cp.get(j+1);
         			
         			Vector2f backEnd = new Vector2f(bspPoints[2].x, bspPoints[2].y);
         			backEnd.scale(2f);
         			backEnd.sub(bspPoints[1]);
     				bspPoints[3] = backEnd;
     			} else {
 	    			bspPoints[0] = cp.get(j-1);
 	    			bspPoints[1] = cp.get(j);
 	    			bspPoints[2] = cp.get(j+1);
 	    			bspPoints[3] = cp.get(j+2);
     			}
     			
     			tessellate_bspline(bspPoints, epsilon, vertices, normals);
     		}
    		vertices.add(endVertex);
    		normals.add(endNormal);
     	}
     	float[] flat_vertices = new float[2 * vertices.size()];
     	float[] flat_normals = new float[2 * vertices.size()];
 
         // TODO Splines Problem 1 and 2: Copy the vertices and normals into the flat arrays
     	for (int i = 0; i < vertices.size(); i++) {
     		flat_vertices[2*i] = vertices.get(i).x;
     		flat_vertices[2*i + 1] = vertices.get(i).y;
     		flat_normals[2*i] = normals.get(i).x;
     		flat_normals[2*i + 1] = normals.get(i).y;
     	}
             
         int nvertices = vertices.size();
         length_buffer = new float[nvertices];
         totLength = 0.0f;
   
         // TODO: PPA2 Problem 3, Section 5.1:
 	    // Compute the 'normalized' total length values.
 	
         setVertices(gl, flat_vertices);
         setNormals(gl, flat_normals);
 
         return;
     }
 
     /*
      * Computes the distance between two points.
      */
     private static float distance(Vector2f p, Vector2f q) {
         float dx = p.x - q.x, dy = p.y - q.y;
         return (float) Math.sqrt(dx * dx + dy * dy);
     }
 
     private List<Object> ctrlsToYamlList()
     {
     	List<Object> list = new ArrayList<Object>();
     	for(Vector2f p : localControlPoints) {
     		list.add(p.x);
     		list.add(p.y);
     	}
 
     	return list;
     }
 
     public Object getYamlObjectRepresentation()
     {
     	Map<String, Object> result = new HashMap<String, Object>();
 		result.put("type", "BSpline");
 		result.put("closed", Boolean.toString(isClosed));
 		result.put("ctrls", ctrlsToYamlList());
 		return result;
     }
 
     @SuppressWarnings("unchecked")
 	public void fromYamlObject(Object yamlObject)
 	{
     	// Validate the file.
 		if (!(yamlObject instanceof Map))
 			throw new RuntimeException("yamlObject not a Map");
 
 		Map<String, Object> yamlMap = (Map<String, Object>)yamlObject;
 		Object ctrlObject = yamlMap.get("ctrls");
 
 		if (!yamlMap.containsKey("closed"))
 			throw new RuntimeException("Must have a 'closed' entry");
 
 		if (!(ctrlObject instanceof List))
 			throw new RuntimeException("ctrlObject not a List");
 
 		List<Double> yamlList = (List<Double>)ctrlObject;
 
 		if (yamlList.size() % 2 > 0)
 			throw new RuntimeException("Must have an even number of control point values");
 
 		// Validation complete, attempt to load.
 		isClosed = Boolean.valueOf(yamlMap.get("closed").toString());
 
 		localControlPoints.clear();
 		for(int i = 0; i < yamlList.size(); i += 2) {
 			localControlPoints.add(new Vector2f(
 				yamlList.get(i + 0).floatValue(),
 				yamlList.get(i + 1).floatValue()));
 		}
 	}
 
     public void save(String filename) throws IOException
 	{
 		Yaml yaml = new Yaml();
 		Object rep = getYamlObjectRepresentation();
 		String output = yaml.dump(rep);
 
 		FileWriter fstream = new FileWriter(filename);
 		BufferedWriter out = new BufferedWriter(fstream);
 		out.write(output);
 		out.close();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void load(String filename) throws IOException
 	{
 		String fileContent = Util.readFileAsString(filename);
 		Yaml yaml = new Yaml();
 		Object yamlObject = yaml.load(fileContent);
 
 		if (!(yamlObject instanceof Map))
 			throw new RuntimeException("yamlObject not a Map");
 		Map<String, Object> yamlMap = (Map<String, Object>)yamlObject;
 
 		if (yamlMap.get("type").equals("BSpline"))
 			fromYamlObject(yamlObject);
 		else
 			throw new RuntimeException("invalid BSpline type: " + yamlMap.get("type").toString());
 	}
 }
