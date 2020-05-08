 package tetrix.utilities;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Vector3f;
 
import tetrix.entities.shapes.Block;
 import tetrix.entities.shapes.Shape;
 
 /**
  * Class containing static helper methods to aid in rendering {@link Shape} objects.
  * @author Dustin Biser
  *
  */
 public class ShapeUtils {
 	
 	/**
 	 * Returns a flipped {@link FloatBuffer} containing vertex coordinate data that
 	 * can be used to render the argument {@link Shape} parameter.
 	 * 
 	 * <p>
 	 * Every 3 floats,
 	 * starting from the beginning of the buffer,  corresponds to the x, y and z
 	 * values of a given vertex.  Every 12 floats, starting from the beginning of
 	 * the buffer, corresponds to 4 vertices.  These 4 vertices correspond to the
 	 * corners of one {@link Block} of the {@link Shape} parameter, with vertices
 	 * arranged in counter-clockwise order starting from the lower left corner for
 	 * a given <code>Block</code>.  A <code>Shape</code> contains 4
 	 * <code>Block</code>s, each of which has 4 vertices, and each vertex has 3
 	 * float coordinates.  This gives 48 float entries in total that are contained
 	 * in the returned <code>FloatBuffer</code>.
 	 * 
 	 * <p>
 	 * The vertices of the given <code>Shape</code> parameter are calculated so
 	 * that they correspond to Blocks with side length <code>2 *
 	 * blockHalfWidth</code>.
 	 * @param shape - <code>Shape</code> to be used in creating the returned FloatBuffer.
 	 * @param blockHalfWidth - One half the desired side length for each of
 	 * <code>shape</code>'s Blocks.
 	 * @return Flipped {@link FloatBuffer} containing the vertices of the Shape parameter. 
 	 */
 	public static FloatBuffer createVertexFloatBuffer(Shape shape, float blockHalfWidth){
 		if (shape == null) return null;
 		
 		Vector3f[] blockVertices = ShapeUtils.createBlockVertices(shape, blockHalfWidth);
 		
 		// Create vertex buffer to hold 3 float coordinates per Block vertex.
 		FloatBuffer verticesBuffer =
 				BufferUtils.createFloatBuffer(blockVertices.length * 3);
 		
 		// Copy block vertices into the verticesBuffer.
 		for(Vector3f vertex : blockVertices){
 			verticesBuffer.put(vertex.x);
 			verticesBuffer.put(vertex.y);
 			verticesBuffer.put(vertex.z);
 		}
 		
 		verticesBuffer.flip();
 		
 		return verticesBuffer;
 	}
 
 	/**
 	 * Returns an array of {@link Vector3f}s representing the vertices of the given
 	 * {@link Shape} parameter.  Every 4 <code>Vector3f</code>s correspond to the
 	 * vertices of one {@link Block} of the given <code>Shape</code>, where the
 	 * vertices are given in counter-clockwise order starting from the bottom left
 	 * vertex for each Block.  Block vertices are given in order of Blocks A, B, C,
 	 * and then D.
 	 * 
 	 * @param shape - Shape with Block positions to be converted to array of vertices.
 	 */
 	public static Vector3f[] createBlockVertices(Shape shape, float blockHalfWidth){
 		if (shape == null) return null;
 		
 		int[] blockPositions = shape.getBlockPositions();
 		int blocksPerShape = 4;
 		int verticesPerBlock = 4;
 		int numVertices = blocksPerShape * verticesPerBlock;
 		Vector3f[] vertices = new Vector3f[ numVertices ];
 		float xCenter, yCenter;
 		
 		// For every col-row Block position pair, create 4 vertices for the Block's
 		// corners coordinates.
 		for(int i = 0, j = 0; i < blockPositions.length; i += 2, j += 4){
 			xCenter = blockPositions[i] * blockHalfWidth * 2;
 			yCenter = blockPositions[i+1] * blockHalfWidth * 2;
 			
 			// Bottom left Block vertex
 			vertices[j] = new Vector3f(xCenter - blockHalfWidth,
 									   yCenter - blockHalfWidth,
 									   0f);
 			
 			// Bottom right Block vertex
 			vertices[j+1] = new Vector3f(xCenter + blockHalfWidth,
 										 yCenter - blockHalfWidth,
 										 0f);
 			
 			// Top right Block vertex
 			vertices[j+2] = new Vector3f(xCenter + blockHalfWidth,
 									     yCenter + blockHalfWidth,
 									     0f);
 			
 			// Top left Block vertex
 			vertices[j+3] = new Vector3f(xCenter - blockHalfWidth,
 										 yCenter + blockHalfWidth,
 										 0f);
 		}
 		
 		return vertices;
 	}
 }
