 /*
  *  Model
  *  
  *  This class provides methods for loading and drawing
  *  Wavefront OBJ model files (.obj)
  *  
  *  The model is interpreted as containing only triangular
  *  faces
  *  
  *  v1.0.0
  *  
  *  23 August 2010
  *  
  *  Sean Wild
  */
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import javax.media.opengl.GL2;
 
 public class Model {
 
 	/* Texture coordinate and normal indices to use
 	 * when none exist in model file
 	 */
 	private static final int DEFAULT_TC_INDEX = -1;
 	private static final int DEFAULT_NORM_INDEX = -1;
 	
 	/* Texture coordinate and normal values
 	 * to use when none exist in model file
 	 */
 	private static final float[] DEFAULT_TC_VALUES = {0.0f, 1.0f};
 	private static final float[] DEFAULT_NORM_VALUES = {0.0f, 0.0f, 0.0f};
 	
 	// Vertex data
 	private float[] vertices;
 	private float[] texture_coords;
 	private float[] normals;
 	
 	/* Indices that point to the vertex data
 	 * of triangular faces
 	 */
 	private int[] vert_indices;
 	private int[] tc_indices;
 	private int[] normal_indices;
 	
 	/* The number of vertices, texture coordinates and
 	 * normals defined in the model file
 	 */
 	private int vertex_count = 0;
 	private int tc_count = 0;
 	private int normal_count = 0;
 	private int index_count = 0;
 	
 	private int displayList;
 	
 	public Model (String filepath) {
 		BufferedReader reader;
 		String line;
 		String[] elements;
 		String[] attributes;
 		
 		try {
 		  reader = new BufferedReader(new FileReader(filepath));
 		  
 		  while (reader.ready()) {
 			  line = reader.readLine();
 			  elements = line.split("\\s+", -1);
 			  
 			  if (elements[0].equals("v")) 
 				  vertex_count++;
 			  else if (elements[0].equals("vt"))
 				  tc_count++;
 			  else if (elements[0].equals("vn"))
 				  normal_count++;
 			  else if (elements[0].equals("f"))
 				  index_count++;
 		  }
 			
 		  reader.close();
 		  reader = new BufferedReader(new FileReader(filepath));
 		  
 		  vertices = new float[vertex_count * 3];
 		  texture_coords = new float[tc_count * 2];
 		  normals = new float[normal_count * 3];
 		  
 		  vert_indices = new int[index_count * 3];
 		  tc_indices = new int[index_count * 3];
 		  normal_indices = new int[index_count * 3];
 		  
 		  int v = 0;
 		  int tc = 0;
 		  int n = 0;
 		  int i = 0;
 		  
 		  while (reader.ready()) {
 			  
 			  line = reader.readLine();
 			  elements = line.split("\\s+", -1);
 			  
 			  if (elements[0].equals("v")) {
 				  vertices[v] = Float.parseFloat(elements[1]);
 				  vertices[v + 1] = Float.parseFloat(elements[2]);
 		    	  vertices[v + 2] = Float.parseFloat(elements[3]);
 		    	  v += 3;
 			  } else if (elements[0].equals("vt")) {
 				  texture_coords[tc] = Float.parseFloat(elements[1]);
 				  texture_coords[tc + 1] = Float.parseFloat(elements[2]);
 				  tc += 2;
 			  } else if (elements[0].equals("vn")) {
 				  normals[n] = Float.parseFloat(elements[1]);
 				  normals[n + 1] = Float.parseFloat(elements[2]);
 				  normals[n + 2] = Float.parseFloat(elements[3]);
 				  n += 3;
 			  } else if (elements[0].equals("f")) {
 				  for (int loop = 1; loop < 4; loop++, i++) {
 					  attributes = elements[loop].split("/", -1);
 					  
 					  switch (attributes.length) {
 					    case 3:
 					    	if (attributes[1].isEmpty()) {
 					    		vert_indices[i] = Integer.parseInt(attributes[0]);
 					    		tc_indices[i] = DEFAULT_TC_INDEX;
 					    		normal_indices[i] = Integer.parseInt(attributes[2]);
 					    	} else {
 					    		vert_indices[i] = Integer.parseInt(attributes[0]);
 					    		tc_indices[i] = Integer.parseInt(attributes[1]);
 					    		normal_indices[i] = Integer.parseInt(attributes[2]);
 					    	}
 					    	break;
 					    case 2:
 					    	vert_indices[i] = Integer.parseInt(attributes[0]) - 1;
 				    		tc_indices[i] = Integer.parseInt(attributes[1]) - 1;
 				    		normal_indices[i] = DEFAULT_NORM_INDEX;
 					    	break;
 					    default:
 					    	vert_indices[i] = Integer.parseInt(attributes[0]) - 1;
 				    		tc_indices[i] = DEFAULT_TC_INDEX;
 				    		normal_indices[i] = DEFAULT_NORM_INDEX;
 					  }
 						  
 				  }
 			  }
 		  }
 		  
 		  reader.close();
 		} catch (IOException e) {
 			System.err.println(e);
 		}
 		
 	}
 	
 	public void buildList(GL2 gl) {
 		displayList = gl.glGenLists(1);
 		
 		gl.glNewList(displayList, GL2.GL_COMPILE);
 		
 		  gl.glBegin(GL2.GL_TRIANGLES);
 		  
 		  for (int i = 0; i < index_count * 3; i++) {
 			  int tc_index = tc_indices[i];
 			  int n_index = normal_indices[i];
 			  int v_index = vert_indices[i];
 			  
 			  switch (tc_index) {
 			  case DEFAULT_TC_INDEX:
 				  gl.glTexCoord2f(DEFAULT_TC_VALUES[0], DEFAULT_TC_VALUES[1]);
 				  break;
 			  default:
				  gl.glTexCoord2f(texture_coords[(tc_index - 1) * 2], texture_coords[(tc_index - 1) * 2 + 1]);
 			  }
 			  
 			  switch (n_index) {
 			  case DEFAULT_NORM_INDEX:
 				  gl.glNormal3f(DEFAULT_NORM_VALUES[0], DEFAULT_NORM_VALUES[1], DEFAULT_NORM_VALUES[2]);
 				  break;
 			  default:
 				  gl.glNormal3f(normals[(n_index - 1) * 3], normals[(n_index - 1) * 3 + 1], normals[(n_index - 1) * 3 + 2]);
 			  }
 			  gl.glVertex3f(vertices[(v_index - 1) * 3], vertices[(v_index - 1) * 3 + 1], vertices[(v_index - 1) * 3 + 2]);
 		  }
 		  
 		  gl.glEnd();
 		  
 		gl.glEndList();
 	}
 	
 	public void draw(GL2 gl) {
 		gl.glCallList(displayList);
 	}
 	
 	public void deleteLists(GL2 gl) {
 	  gl.glDeleteLists(displayList, 1);	
 	}
 	
 	//Test/debug methods
 	/*
 	private void printVertices() {
 		for (int i = 0; i < vertices.length; i++) {
 			if (i % 3 == 0) 
 			  System.out.print("\n" + ((i/3) + 1) + " " + vertices[i]);
 			else
 			  System.out.print(" " + vertices[i]);
 		}
 		System.out.println();
 	}
 	
 	private void printTexCoords() {
 		for (int i = 0; i < texture_coords.length; i++) {
 			if (i % 2 == 0) 
 			  System.out.print("\n" + ((i/2) + 1) + " " + texture_coords[i]);
 			else
 			  System.out.print(" " + texture_coords[i]);
 		}
 		System.out.println();
 	}
 	
 	private void printNormals() {
 		for (int i = 0; i < normals.length; i++) {
 			if (i % 3 == 0) 
 			  System.out.print("\n" + ((i/3) + 1) + " " + normals[i]);
 			else
 			  System.out.print(" " + normals[i]);
 		}
 		System.out.println();
 	}
 	
 	private void printVertIndices() {
 		for (int i = 0; i < vert_indices.length; i++) {
 			if (i % 3 == 0) 
 			  System.out.print("\n" + ((i/3) + 1) + ": " + vert_indices[i]);
 			else
 			  System.out.print(" " + vert_indices[i]);
 		}
 		System.out.println();
 	}
 	
 	private void printTCIndices() {
 		for (int i = 0; i < tc_indices.length; i++) {
 			if (i % 2 == 0) 
 			  System.out.print("\n" + ((i/2) + 1) + ": " + tc_indices[i]);
 			else
 			  System.out.print(" " + tc_indices[i]);
 		}
 		System.out.println();
 	}
 	
 	private void printNormalIndices() {
 		for (int i = 0; i < normal_indices.length; i++) {
 			if (i % 3 == 0) 
 			  System.out.print("\n" + ((i/3) + 1) + ": " + normal_indices[i]);
 			else
 			  System.out.print(" " + normal_indices[i]);
 		}
 		System.out.println();
 	}
 	
 	private void printBufferData() {
 		for (int i = 0; i < vertex_buffer_length; i++) {
 			System.out.println(vertex_buffer_data.get(i));
 		}
 	}
 	
 	private void printVertexData() {
 		System.out.println("Vertices: " + vertex_count);
 		System.out.println("Tex Coords:" + tc_count);
 		System.out.println("Normals:" + normal_count);
 		System.out.println("Faces:" + index_count);
 		System.out.println("Indexed Vertex Count:" + vert_indices.length);
 	} */
 }
