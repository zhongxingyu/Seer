 package pipline;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import javax.swing.JTextArea;
 
 import components.Edge;
 import components.Polygon;
 import components.Transform;
 import components.Vector3D;
 import components.ZBufferValue;
 
 public class SimpleRenderer {
 
 	ArrayList<Polygon> mesh;
 	ArrayList<Polygon> visibleMesh;
 
 	Vector3D lightSource;
 	float[] ambientLight;
 	// array of the minx miny maxx and maxy in that order
 	float[] boundingBoxXY;
 
 	JTextArea textOutput;
 
 	/*
 	 * int minY; int maxY;
 	 */
 	private int imageHeight;
 
 	public SimpleRenderer(ArrayList<Polygon> mesh2, Vector3D lightSource,
 			float[] ambientLight, int imageHeight) {
 		this.mesh = mesh2;
 		// do a quick check to ensure mesh is not malformed
 		for (Polygon p : mesh) {
 			assert (p.getVerts().length == 3);
 			assert (p.edges.length == 3);
 		}
 
 		this.lightSource = lightSource;
 		this.ambientLight = ambientLight;
 		this.imageHeight = imageHeight;
 
 	}
 
 	public Color[][] rotatePolygons(String direction) {
 		Transform dir;
 		if (direction.equals("UP"))
 			dir = Transform.newXRotation(10);
 		else if (direction.equals("DOWN"))
 			dir = Transform.newXRotation(-10);
 		else if (direction.equals("LEFT"))
 			dir = Transform.newYRotation(-10);
 		else if (direction.equals("RIGHT"))
 			dir = Transform.newYRotation(10);
 		else {
 			return null;
 		}
 		transformAll(dir);
 		// dir.multiply(lightSource);
 		return this.render(textOutput);
 	}
 
 	public void transformPolygons() {
 		// get the bounding box
 		float xMax = Float.MIN_VALUE;
 		float xMin = Float.MAX_VALUE;
 		float yMax = Float.MIN_VALUE;
 		float yMin = Float.MAX_VALUE;
 		for (Polygon p : mesh) {
 			// then for all vertcies in a given polygon
 			Vector3D[] verts = p.getVerts();
 			for (int i = 0; i < verts.length - 1; i++) {
 				// first check the x
 				if (verts[i].x > xMax)
 					xMax = verts[i].x;
 				if (verts[i].x < xMin)
 					xMin = verts[i].x;
 				// then the y
 				if (verts[i].y > yMax)
 					yMax = verts[i].y;
 				if (verts[i].y < yMin)
 					yMin = verts[i].y;
 
 			}
 		}
 
 		System.out.println("the bounding box is now\n\t" + xMin + "\t" + yMin
 				+ "\n\t" + xMax + "\t" + yMax);
 		// bounding box is now set
 
 		// get the polygon's centre from these values
 		// centre value is (max value + min) /2
 		float xCentre = (xMax + xMin) / 2f;
 		float yCentre = (yMax + yMin) / 2f;
 		System.out.println("current centre is at " + xCentre + "," + yCentre);
 		float centreWindow = imageHeight / 2f;
 		float xDist = centreWindow - xCentre;
 		float yDist = centreWindow - yCentre;
 		System.out.println("Distance to be translated " + xDist + "," + yDist);
 		Transform moveToCentre = Transform.newTranslation(xDist, yDist, 0f);
 		transformAll(moveToCentre);
 
 		// scalePolygons(new Vector3D((imageHeight/2),(imageHeight/2),0.0f));
 
 		for (Polygon p : mesh) {
 			// then for all vertcies in a given polygon
 			Vector3D[] verts = p.getVerts();
 			for (int i = 0; i < verts.length - 1; i++) {
 				// first check the x
 				if (verts[i].x > xMax)
 					xMax = verts[i].x;
 				if (verts[i].x < xMin)
 					xMin = verts[i].x;
 				// then the y
 				if (verts[i].y > yMax)
 					yMax = verts[i].y;
 				if (verts[i].y < yMin)
 					yMin = verts[i].y;
 
 			}
 		}
 
 		System.out.println("the bounding box is now\n\t" + xMin + "\t" + yMin
 				+ "\n\t" + xMax + "\t" + yMax);
 
 		/*
 		 * boundingBoxXY = new float[] { Float.MAX_VALUE, Float.MAX_VALUE,
 		 * Float.MIN_VALUE, Float.MIN_VALUE };
 		 * 
 		 * float lowerXBound = Float.MAX_VALUE; float upperXBound =
 		 * Float.MIN_VALUE; float lowerYBound = Float.MAX_VALUE; float
 		 * upperYBound = Float.MIN_VALUE;
 		 * 
 		 * // go through all the polygons in the mesh for (Polygon p : mesh) {
 		 * // check if a given vertex of that polygon is larger than any bounds
 		 * Vector3D[] verts = p.getVerts(); for (int i = 0; i < 3; i++) {
 		 * boundingBoxXY[0] = verts[0].minX(boundingBoxXY[0]); boundingBoxXY[1]
 		 * = verts[0].minY(boundingBoxXY[1]); boundingBoxXY[2] =
 		 * verts[0].maxX(boundingBoxXY[2]); boundingBoxXY[3] =
 		 * verts[0].maxY(boundingBoxXY[3]); } } float xTranslateDiff =
 		 * Math.max(Math.abs(0 - boundingBoxXY[0]), Math.abs(700 -
 		 * boundingBoxXY[2])); float yTranslateDiff = Math.max(Math.abs(0 -
 		 * boundingBoxXY[1]), Math.abs(700 - boundingBoxXY[3])); Transform t =
 		 * Transform.newTranslation(xTranslateDiff, yTranslateDiff, 0);
 		 * transformAll(t);
 		 */
 
 	}
 
 	private void setBoundingBox() {
 		float[] bBox = new float[4];
 		float xMax = Float.MIN_VALUE;
 		float xMin = Float.MAX_VALUE;
 		float yMax = Float.MIN_VALUE;
 		float yMin = Float.MAX_VALUE;
 		for (Polygon p : mesh) {
 			// then for all vertcies in a given polygon
 			Vector3D[] verts = p.getVerts();
 			for (int i = 0; i < verts.length - 1; i++) {
 				// first check the x
 				if (verts[i].x > xMax)
 					xMax = verts[i].x;
 				if (verts[i].x < xMin)
 					xMin = verts[i].x;
 				// then the y
 				if (verts[i].y > yMax)
 					yMax = verts[i].y;
 				if (verts[i].y < yMin)
 					yMin = verts[i].y;
 
 			}
 		}
 		// put it all into the bounding box
 		bBox[0] = xMin;
 		bBox[1] = yMin;
 		bBox[2] = xMax;
 		bBox[3] = yMax;
 
 	}
 
 	public void scalePolygons(Vector3D centre) {
 		// need to find the largest distance from the centre of the object any
 		// point on the object
 		// in terms of x and y
 
 		// first find the centre
 		System.out.println("Centre of polygon is at " + centre.x + ","
 				+ centre.y);
 
 		float maxX = Float.MIN_NORMAL;
 		float maxY = Float.MAX_VALUE;
 		Vector3D f = null;
 		for (Polygon p : mesh) {
 			Vector3D[] verts = p.getVerts();
 			for (int i = 0; i < 3; i++) {
 				if ((Math.abs(verts[i].x - centre.x) > maxX)) {
 					maxX = Math.abs(verts[i].x - centre.x);
 					f = verts[i];
 				}
 				if (Math.abs(verts[i].y - centre.y) > maxY) {
 					maxY = Math.abs(verts[i].y - centre.y);
 					f = verts[i];
 				}
 			}
 		}
 		// find the distance from furthest vert to the view window edge
 		// scale by that
 		float xDist = Math.abs(imageHeight - f.x);
 
 		float yDist = Math.abs(imageHeight - f.y);
 		float maxDist = Float.MIN_VALUE;
 		if (xDist > yDist) {
 			maxDist = xDist;
 		} else {
 			maxDist = yDist;
 		}
 
 		Transform scale = Transform.newScale(maxDist, maxDist, maxDist);
 		transformAll(scale);
 
 		/*
 		 * 
 		 * 
 		 * // need to find the smallest value that will scale the polygon to
 		 * within // the view window // so ill find the maximum distance from
 		 * the centre of the screen to the // furthest point of a polygon float
 		 * maxDist = Float.MIN_VALUE;
 		 * 
 		 * // go through all the polygons for (Polygon p : mesh) { // for the
 		 * verticies in that polygon Vector3D[] verts = p.getVerts(); for (int i
 		 * = 0; i < verts.length - 1; i++) { // get the absolute distance from
 		 * the centre to x and y of // vertex float maxXDist =
 		 * Math.abs(verts[i].x - centre.x); float maxYDist = Math.abs(verts[i].y
 		 * - centre.y); float currentMax; if (maxXDist > maxYDist) currentMax =
 		 * maxXDist; else currentMax = maxYDist;
 		 * 
 		 * if (currentMax > maxDist) maxDist = currentMax; } }
 		 * 
 		 * // so now we have the current max distance // so scale by it
 		 * transformAll(Transform.newScale(maxDist,maxDist, maxDist));
 		 * 
 		 * // do a quick check to ensure model is in the view window for
 		 * (Polygon p : mesh) { Vector3D[] verts = p.getVerts(); for (int i = 0;
 		 * i < 3; i++) { // assert verts[i].isWithin(imageHeight, imageHeight) :
 		 * //
 		 * "All polygons must be within the view window (i.e.  between 0 and 700 on both x and y!"
 		 * ; /* assert (verts[i].x < imageHeight && verts[i].x > 0) :
 		 * "must be within screen"; assert (verts[i].y < imageHeight &&
 		 * verts[i].y > 0) : "must be within screen";
 		 * 
 		 * } }
 		 */
 	}
 
 	public void removeHidden() {
 		// a different set of polys to remove as to avoid concurrent mod
 		visibleMesh = new ArrayList<Polygon>();
 		for (Polygon p : mesh) {
 			Vector3D[] verts = p.getVerts();
 			Vector3D v1 = verts[0];
 			Vector3D v2 = verts[1];
 			Vector3D v3 = verts[2];
 			/*
 			 * Vector3D normal1 = v1.plus(v2); Vector3D normal2 = v2.plus(v3);
 			 * Vector3D normal = normal1.crossProduct(normal2);
 			 */
			Vector3D normal = v1.crossProduct(v2);
 
 			if (normal.z < 0) {
 				visibleMesh.add(p);
 			}
 		}
 
 	}
 
 	public Color[][] render(JTextArea textOutput) {
 		this.textOutput = textOutput;
 		// this.computeShadng();
 		textOutput.setText("Computing done now for some heavy stuff");
 		// this.constructEdgeLists();
 		/*
 		 * int[] minMaxY = getMinMaxY(); minY= minMaxY[0]; maxY = minMaxY[1];
 		 */
 		return this.zBufferRender();
 
 	}
 
 	private Color[][] zBufferRender() {
 		Color[][] zBufferC = new Color[imageHeight][imageHeight];
 		float[][] zBufferD = new float[imageHeight][imageHeight];
 
 		// set the z buffers depth to positive infinity at all points
 		// and set the z buffers default color to background color
 
 		for (int i = 0; i < zBufferD.length - 1; i++) {
 			for (int j = 0; j < zBufferD[i].length - 1; j++) {
 				// Set the depth by default to positive infinity
 				zBufferD[i][j] = Float.POSITIVE_INFINITY;
 				zBufferC[i][j] = Color.DARK_GRAY;
 			}
 		}
 
 		textOutput.setText("intialised the zBuffer");
 
 		// for each polygon
 		for (Polygon p : visibleMesh) {
 			// compute edgelists el for given polygon
 			float[][] edgeLists = computeEdgeLists(p);
 			// and the shaded color of that polygon
 			Color shading = getShading(p);
 
 			for (int y = 0; y < edgeLists.length - 1; y++) {
 
 				float[] el = edgeLists[y];
 				int x = Math.round(el[0]);
 				float z = el[1];
 				float mz = ((el[3] - el[1]) / (el[2] - el[0]));
 
 				while (x <= Math.round(el[2])) {
 					if (z < zBufferD[x][y]) {
 						zBufferD[x][y] = z;
 						zBufferC[x][y] = shading;
 					}
 					x++;
 					z += mz;
 				}
 			}
 		}
 
 		return zBufferC;
 	}
 
 	private Color getShading(Polygon p) {
 		Vector3D d = lightSource.unitVector();
 		// FIXME check this!
 		float[] lightSourceCols = new float[] { 0.5f, 0.5f, 0.5f };
 
 		Vector3D[] verts = p.getVerts();
 		Vector3D v1 = verts[0];
 		Vector3D v2 = verts[1];
 		Vector3D n = v1.crossProduct(v2).unitVector();
 		// angle between normal and the lightsource vectors
 		float costh = n.cosTheta(d);
 		float redOuput = ((ambientLight[0] + lightSourceCols[0] * costh) * p
 				.getRed());
 		float greenOuput = ((ambientLight[1] + lightSourceCols[1] * costh) * p
 				.getGreen());
 		float blueOutput = ((ambientLight[2] + lightSourceCols[2] * costh) * p
 				.getBlue());
 		assert redOuput <= 255 && redOuput >= 0 : "Red color must between 0 and 255 inclusive";
 		assert greenOuput <= 255 && greenOuput >= 0 : "Green color must between 0 and 255 inclusive";
 		assert blueOutput <= 255 && blueOutput >= 0 : "Blue color must between 0 and 255 inclusive";
 		Color outputC = new Color((int) (redOuput), (int) (greenOuput),
 				(int) (blueOutput));
 		return outputC;
 	}
 
 	private float[][] computeEdgeLists(Polygon p) {
 		// find the min and maxy of all the verticies in the polygon
 		int minY = Math.round(getMinY(p));
 		int maxY = Math.round(getMaxY(p));
 
 		float[][] edgeList = new float[imageHeight][4];
 		for (int i = 0; i < (edgeList.length - 1); i++) {
 			edgeList[i][1] = Float.POSITIVE_INFINITY;
 			edgeList[i][3] = Float.POSITIVE_INFINITY;
 		}
 
 		// for each edge in the polygon
 		Edge[] edges = p.getEdges();
 
 		for (int edgeIndex = 0; edgeIndex < 3; edgeIndex++) {
 			// initialise x values as those are the only ones we are comparing
 			// this is done in the loop so the first vertex of an edge is always
 			// added
 			// to both left and right which is correct.
 			float[] innerList = new float[4];
 			// set left to be positive infinity
 			innerList[0] = Float.POSITIVE_INFINITY;
 
 			// set right to be negative infinity
 			innerList[2] = Float.NEGATIVE_INFINITY;
 
 			Vector3D va; // vertex with the smallest y value
 			Vector3D vb; // the other vertex of the edge
 			Vector3D v1 = edges[edgeIndex].v1;
 			Vector3D v2 = edges[edgeIndex].v2;
 
 			if (v1.y > v2.y) { // if the first vertex has a greater y value then
 								// 2nd v is va
 				va = v2;
 				vb = v1;
 			} else {
 				va = v1;
 				vb = v2;
 			}
 
 			assert (va.y <= vb.y) : "smallest vertex based on y value isnt being set correctly";
 
 			float my = vb.y - va.y;
 
 			// m denotes 'change in' in this case in terms of y
 			float mx = (vb.x - va.x) / (my);
 			float mz = (vb.z - va.z) / (my);
 
 			// the current x and z values when interpolating
 			float x = va.x;
 			float z = va.z;
 
 			// values to interpolate through
 			int i = Math.round(va.y);
 			int maxI = Math.round(vb.y);
 			// // ________________________________________
 			// / +++++++++++++++++++++++++++++++++++++++
 			// / NOTE TO SELF!!!!
 			// //____________------------------------________________
 			// this fails because when mz and mx are really small
 			// that point is added to both left and right repeatedly
 			// until it is simply a striaght line
 			while (i < maxI) {

 				// Figured it out
 				// it's all references to the same objects?
 				
 				// if x is a point on left edge
 				if (x < innerList[0]) {
 					innerList[0] = x;
 					innerList[1] = z;
 				}
 				// if x is a point on the right edge
 				if (x > innerList[2]) {
 					innerList[2] = x;
 					innerList[3] = z;
 				}
 
 				// put the inner list in
 				edgeList[i] = innerList;
 				i++;
 
 				x = x + mx;
 
 				z = z + mz;
 				
 
 			}
 			
 			// get the edgeList at maxI (the last list for this edge
 			innerList = edgeList[maxI];
 			// check for left
 			if (vb.x < innerList[0]) {
 				innerList[0] = vb.x;
 				innerList[1] = vb.z;
 			}
 			// check for right
 			if (vb.x > innerList[2]) {
 				innerList[2] = vb.x;
 				innerList[3] = vb.z;
 			}
 			edgeList[maxI] = innerList;
 
 		}
 
 		return edgeList;
 
 	}
 
 	private void printEdgeList(float[][] edgeList, Vector3D va, Vector3D vb) {
 		System.out.println("xLeft    ||     xRight    ");
 		for (int k = Math.round(va.y); k < Math.round(vb.y); k++) {
 			for (int l = 0; l < 4; l++) {
 				// assert(edgeList[k][l] !=Float.POSITIVE_INFINITY) :
 				// "Invalid edgeList value";
 				System.out.print(edgeList[k][0] + "||	");
 				System.out.print(edgeList[k][2] + "\n");
 			}
 
 		}
 	}
 
 	/**
 	 * Gets the minimum y value for all vertices
 	 * 
 	 * 
 	 * @param p
 	 * @return the max y value of all the verts in the polygon
 	 */
 	private float getMinY(Polygon p) {
 		float minY = Float.MAX_VALUE;
 		Vector3D[] verts = p.getVerts();
 		for (int i = 0; i < (verts.length - 1); i++) {
 			if (verts[i].y < minY) {
 				minY = verts[i].y;
 			}
 		}
 		return minY;
 	}
 
 	private float getMaxY(Polygon p) {
 		float maxY = Float.MIN_VALUE;
 		Vector3D[] verts = p.getVerts();
 		for (int i = 0; i < (verts.length - 1); i++) {
 			if (verts[i].y > maxY) {
 				maxY = verts[i].y;
 			}
 		}
 		return maxY;
 	}
 
 	private void transformAll(Transform dir) {
 		for (Polygon p : mesh) {
 			Vector3D[] verts = p.getVerts();
 			Edge[] edges = p.edges;
 			for (int i = 0; i < 3; i++) {
 				Vector3D v1 = edges[i].v1;
 				Vector3D v2 = edges[i].v2;
 				v1 = dir.multiply(v1);
 				v2 = dir.multiply(v2);
 				Vector3D newSpot = dir.multiply(verts[i]);
 				verts[i] = newSpot;
 			}
 		}
 	}
 
 }
