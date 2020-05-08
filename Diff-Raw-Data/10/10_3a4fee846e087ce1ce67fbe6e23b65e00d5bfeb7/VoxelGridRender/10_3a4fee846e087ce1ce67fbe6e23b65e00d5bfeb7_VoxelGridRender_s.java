 package sculptnect;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.HashSet;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.vecmath.Point3i;
 import javax.vecmath.Tuple3i;
 import javax.vecmath.Vector3f;
 
 import com.jogamp.common.nio.Buffers;
 
 public class VoxelGridRender {
 	private static final int CELL_SIZE = 64;
 
 	VoxelGrid grid;
 	BufferCell[][][] bufferCells;
	HashSet<BufferCell> dirtyCells = new HashSet<BufferCell>();
	HashSet<BufferCell> visibleCells = new HashSet<BufferCell>();

 	// Allocate a direct byte buffer large enough to hold a cell full of points
 	FloatBuffer floatBuffer = ByteBuffer
 			.allocateDirect(CELL_SIZE * CELL_SIZE * CELL_SIZE * 6 * 4)
 			.order(ByteOrder.nativeOrder()).asFloatBuffer();
 
 	Tuple3i dimensions = new Point3i();
 
 	private class BufferCell {
 		// The position of this cell in the buffer grid
 		Tuple3i position = new Point3i();
 
 		// The lower indices of the voxel grid for this cell
 		Tuple3i lowerIndices = new Point3i();
 		// The upper indices of the voxel grid for this cell
 		Tuple3i upperIndices = new Point3i();
 
 		// The buffer object name used as handle in OpenGL
 		int bufferName;
 		// The number of indices this buffer object contains
 		int numIndices;
 
 		// Marks this cell as dirty or not
 		boolean dirty;
 	}
 
 	public VoxelGridRender(GL2 gl, VoxelGrid grid) {
 		this.grid = grid;
 
 		// Calculate buffer dimensions
 		dimensions.x = (int) Math.ceil((double) grid.width / CELL_SIZE);
 		dimensions.y = (int) Math.ceil((double) grid.height / CELL_SIZE);
 		dimensions.z = (int) Math.ceil((double) grid.depth / CELL_SIZE);
 
 		// Create the buffer cells and initialize them
 		bufferCells = new BufferCell[dimensions.x][dimensions.y][dimensions.z];
 		for (int x = 0; x < dimensions.x; x++) {
 			for (int y = 0; y < dimensions.y; y++) {
 				for (int z = 0; z < dimensions.z; z++) {
 					BufferCell cell = new BufferCell();
 					bufferCells[x][y][z] = cell;
 
 					// Set cell position and indices bounds
 					cell.position.set(x, y, z);
 					cell.lowerIndices.set(x * CELL_SIZE, y * CELL_SIZE, z
 							* CELL_SIZE);
 					cell.upperIndices.set(
 							Math.min((x + 1) * CELL_SIZE, grid.width),
 							Math.min((y + 1) * CELL_SIZE, grid.height),
 							Math.min((z + 1) * CELL_SIZE, grid.depth));
 
 					// Generate and set a buffer object name for this cell
 					int[] buf = new int[1];
 					gl.glGenBuffers(1, buf, 0);
 					cell.bufferName = buf[0];
 				}
 			}
 		}
 	}
 
 	public void markVoxelDirty(int x, int y, int z) {
 		BufferCell cell = bufferCells[x / CELL_SIZE][y / CELL_SIZE][z
 				/ CELL_SIZE];
 		if (cell.dirty)
 			return;
 
 		cell.dirty = true;
 		dirtyCells.add(cell);
 	}
 
 	public void updateDirtyCells(GL2 gl) {
 		for (BufferCell cell : dirtyCells) {
 			updateBufferCell(gl, cell);
 			cell.dirty = false;
 		}
 
 		dirtyCells.clear();
 	}
 
 	public void draw(GL2 gl) {
 		// Position grid around origin for rotation around center
 		//gl.glTranslatef(-grid.width / 2.0f, -grid.height / 2.0f,
 		//		-grid.depth / 2.0f);
 		
 		gl.glColor3f(0.3f, 0.3f, 0.3f);
 
 		// Enable the vertex and normal arrays
 		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
 		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
 
 		// Loop through visible cells and draw them
 		for (BufferCell cell : visibleCells) {
 			// Bind buffer containing points and normals
 			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, cell.bufferName);
 
 			// Specify vertex and normal data
 			int stride = Buffers.SIZEOF_FLOAT * 6;
 			gl.glVertexPointer(3, GL2.GL_FLOAT, stride, 0);
 			gl.glNormalPointer(GL2.GL_FLOAT, stride, Buffers.SIZEOF_FLOAT * 3);
 
 			// Draw the points
 			gl.glDrawArrays(GL2.GL_POINTS, 0, cell.numIndices);
 		}
 
 		// Unbind the buffer data
 		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
 
 		// Disable vertex and normal arrays
 		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
 		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
 	}
 
 	private Vector3f normalForVoxel(int x, int y, int z, Vector3f normal) {
 		float numAdded = 0;
 		normal.set(0, 0, 0);
 
 		// Iterate through the 26 neighbors of the voxel
 		for (int xd = -1; xd < 2; xd++) {
 			for (int yd = -1; yd < 2; yd++) {
 				for (int zd = -1; zd < 2; zd++) {
 					int xoff = x + xd;
 					int yoff = y + yd;
 					int zoff = z + zd;
 
 					if (xoff >= 0 && xoff < grid.width && yoff >= 0
 							&& yoff < grid.height && zoff >= 0
 							&& zoff < grid.depth) {
 						if (grid.isAir(xoff, yoff, zoff)) {
 							// If the neighbor voxel is empty, add the direction
 							// to it to the normal
 							normal.x += xd;
 							normal.y += yd;
 							normal.z += zd;
 							numAdded++;
 						}
 					}
 				}
 			}
 		}
 
 		// Take the average of all summed vectors and normalize the result
 		normal.scale(1.0f / numAdded);
 		normal.normalize();
 
 		return normal;
 	}
 
 	private void updateBufferCell(GL2 gl, BufferCell cell) {
 		cell.numIndices = 0;
 
 		floatBuffer.clear();
 
 		Vector3f normal = new Vector3f();
 		for (int x = cell.lowerIndices.x; x < cell.upperIndices.x; x++) {
 			for (int y = cell.lowerIndices.y; y < cell.upperIndices.y; y++) {
 				for (int z = cell.lowerIndices.z; z < cell.upperIndices.z; z++) {
 					// Skip voxel if it's empty
 					if (grid.isAir(x, y, z))
 						continue;
 
 					// Determine if voxel is completely inside by
 					// examining its neighbors
 					for (int i = 0; i < 6; i++) {
 						int[] offset = VoxelGrid.offsets[i];
 						int xoff = x + offset[0];
 						int yoff = y + offset[1];
 						int zoff = z + offset[2];
 
 						boolean inside = xoff >= 0 && xoff < grid.width
 								&& yoff >= 0 && yoff < grid.height && zoff >= 0
 								&& zoff < grid.depth;
 						if (!inside
 								|| (grid.getVoxel(xoff, yoff, zoff) != grid
 										.getVoxel(x, y, z))) {
 							cell.numIndices++;
 
 							// Put vertex data into buffer
 							floatBuffer.put(x);
 							floatBuffer.put(y);
 							floatBuffer.put(z);
 
 							// Put normal for the vertex into buffer
 							Vector3f n = normalForVoxel(x, y, z, normal);
 							floatBuffer.put(n.x);
 							floatBuffer.put(n.y);
 							floatBuffer.put(n.z);
 
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		// If this cell doesn't contain any points, remove it from the visible
 		// buffer cell set
 		if (cell.numIndices == 0) {
 			visibleCells.remove(cell);
 		} else {
 			visibleCells.add(cell);
 		}
 
 		floatBuffer.rewind();
 
 		// Upload the vertex and normal data to the buffer
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, cell.bufferName);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, cell.numIndices * 6
 				* Buffers.SIZEOF_FLOAT, floatBuffer, GL.GL_STATIC_DRAW);
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
 	}
 }
