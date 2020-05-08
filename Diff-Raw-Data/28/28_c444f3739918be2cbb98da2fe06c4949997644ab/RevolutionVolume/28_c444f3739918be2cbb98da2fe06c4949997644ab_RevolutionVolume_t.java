 package cs4620.spline;
 
 import javax.media.opengl.GL2;
 
 import cs4620.shape.TriangleMesh;
 
 /*
  * A revolution volume. Generates a mesh by revolving its control curve
  * around the y axis.
  */
 public class RevolutionVolume extends TriangleMesh {
     protected DiscreteCurve curve;
 
     public RevolutionVolume(GL2 gl, DiscreteCurve curve) {
         super(gl);
         this.curve = curve;
     }
 
     @Override
     public void buildMesh(GL2 gl, float tolerance) {
         // TODO (Splines Problem 1, Section 3.4):
     	// Compute the positions, normals, and texture coordinates for the surface
     	// of revolution mesh. Start with the vertices in the DiscreteCurve.
     	
 		
 		float curveVert[] = curve.vertices;
 		float curveNorm[] = curve.normals;
		
		float maxRad = 0;
		
		for (int i = 0; i < curve.vertices.length; i = i + 2) {
			if (curveVert[i] > maxRad) {
				maxRad = curveVert[i];
			}
		}
 
    	float numSectors = (float)((maxRad*2*Math.PI) / tolerance);
 		int rowLength = (int)(Math.ceil(numSectors));
		int rowHeight = curveVert.length/2;
		int numPoints = ((rowLength+1) * (rowHeight+1));
 		
 		float[] vertexCoords = new float[numPoints*3];
 		float[] normalCoords = new float[numPoints*3];
 		float[] textureCoords = new float[numPoints*2];
 		int[] triangleVerts = new int[numPoints*3*2];
 		int[] wireframeVerts = new int[numPoints*2*2];
 		
 		float x = 0;
 		float y = 0;
 		float z = 0;
 		
 		float phi = 0;
 		float theta = 0;
 		
		for (float v = 0; v < rowHeight; v++) {
 			for (float u = 0; u <= rowLength; u++) {
 				int rowPos = (int)(((v*(rowLength+1)) + u)*3);
 				
 				//Vertices
 				x = (float)curveVert[(int)v*2]*(float)Math.cos(2 * (u/rowLength) * Math.PI);
 				y = (float)curveVert[(int)v*2+1];
 				z = (float)-1*curveVert[(int)v*2]*(float)Math.sin(2 * (u/rowLength) * Math.PI);
 				
 				//normals
 				float xn = (float)curveNorm[(int)v*2]*(float)Math.cos(2 * (u/rowLength) * Math.PI);
 				float yn = (float)curveNorm[(int)v*2+1];
 				float zn = (float)-1*curveNorm[(int)v*2]*(float)Math.sin(2 * (u/rowLength) * Math.PI);
 				
 				vertexCoords[rowPos] = x;
 				vertexCoords[rowPos+1] = y;
 				vertexCoords[rowPos+2] = z;
 				
 				normalCoords[rowPos] = xn;
 				normalCoords[rowPos+1] = yn;
 				normalCoords[rowPos+2] = zn;
 				
 				int texPos = (int)(((v*(rowLength+1)) + u)*2);
 
 				textureCoords[texPos] = 1 - u/rowLength;
				textureCoords[texPos+1] = v/rowHeight;
 
 			}
 		}
 		
		for (int v = 0; v < rowHeight; v++) {
 			for (int u = 0; u < rowLength; u++) {
 				int rowPos = (((v*(rowLength)) + u)*3*2);
 
 				triangleVerts[rowPos] = v*(rowLength+1) + u;
 				triangleVerts[rowPos+1] = (v+1)*(rowLength+1) + u;
 				triangleVerts[rowPos+2] = v*(rowLength+1) + u + 1;
 				
 				triangleVerts[rowPos+3] = triangleVerts[rowPos+2];
 				triangleVerts[rowPos+4] = triangleVerts[rowPos+1];
 				triangleVerts[rowPos+5] = triangleVerts[rowPos+1] + 1;
 
 				int vertPos = (((v*(rowLength)) + u)*2*2);
 
 				wireframeVerts[vertPos] = triangleVerts[rowPos];
 				wireframeVerts[vertPos+1] = triangleVerts[rowPos+1];
 				
 				wireframeVerts[vertPos+2] = triangleVerts[rowPos];
 				wireframeVerts[vertPos+3] = triangleVerts[rowPos+2];
 				
 			}
 		}
 		
 		setVertices(gl, vertexCoords);
 		setNormals(gl, normalCoords);
 		setTriangleIndices(gl, triangleVerts);
 		setWireframeIndices(gl, wireframeVerts);
 		setTexCoords(gl, textureCoords);
     }
 
 	@Override
 	public Object getYamlObjectRepresentation() {
 		return null;
 	}
 }
