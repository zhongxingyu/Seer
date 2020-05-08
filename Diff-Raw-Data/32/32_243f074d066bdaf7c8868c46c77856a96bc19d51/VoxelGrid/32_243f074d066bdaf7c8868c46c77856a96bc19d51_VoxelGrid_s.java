 package sculptnect;
 
 import javax.media.opengl.GL2;
 import javax.vecmath.Tuple3i;
 
 import shape.ShapeGenerator;
 
 public class VoxelGrid {
 	public static final byte VOXEL_GRID_NO_CHANGE = -1;
 	public static final byte VOXEL_GRID_AIR = 0;
 	public static final byte VOXEL_GRID_CLAY = 1;
 	
 	private byte[][][] _voxels;
 	
 	private boolean renderGrid = true;
 	private boolean renderMesh = false;

 	int width, height, depth;
 
 	VoxelGridRender render;
 	VoxelMeshRender meshRender;
 
 	public static int[][] offsets = {
 			//
 			{ -1, 0, 0 }, //
 			{ 1, 0, 0 }, //
 			{ 0, -1, 0 }, //
 			{ 0, 1, 0 }, //
 			{ 0, 0, -1 }, //
 			{ 0, 0, 1 }, //
 	};
 	
 	public VoxelGrid(int size) {
 		this(size, size, size);
 	}
 
 	public VoxelGrid(int width, int height, int depth) {
 		this.width = width;
 		this.height = height;
 		this.depth = depth;
 		_voxels = new byte[width][height][depth];
 
 		// Create render
 		render = new VoxelGridRender(this);
 		meshRender = new VoxelMeshRender(this);
 	}
 
 	public byte getVoxel(int x, int y, int z) {
 		return _voxels[x][y][z];
 	}
 
 	public void setVoxel(int x, int y, int z, byte value) {
 		// Inform render that this voxel changed
 		if (renderGrid) render.markVoxelDirty(x, y, z);
 		if (renderMesh) meshRender.markVoxelDirty(x, y, z);
 
 		_voxels[x][y][z] = value;
 	}
 
 	public boolean isAir(int x, int y, int z) {
 		return _voxels[x][y][z] == VOXEL_GRID_AIR;
 	}
 	
 	public void beginEditing() {
 		if (renderGrid) render.beginVoxelMarking();
 		if (renderMesh) meshRender.beginVoxelMarking();
 	}
 	
 	public void endEditing() {
 		if (renderGrid) render.endVoxelMarking();
 		if (renderMesh) meshRender.endVoxelMarking();
 	}
 	
 	public void toggleRenderMode() {
		renderGrid = !renderGrid;
		renderMesh = !renderMesh;
		if (renderGrid) {
			render.refresh();
		} else {
			meshRender.refresh();
		}
 	}
 	
 	public void clear() {
 		// Iterate through all voxels and set them all to air
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				for (int z = 0; z < depth; z++) {
 					setVoxel(x, y, z, VOXEL_GRID_AIR);
 				}
 			}
 		}
 	}
 
 	public void insertShape(ShapeGenerator generator) {
 		Tuple3i center = generator.getCenter();
 		Tuple3i size = generator.getSize();
 
 		// Calculate bounds for the shape to insert
 		int xmin = Math.max(0, center.x - size.x / 2);
 		int xmax = Math.min(width, center.x + size.x / 2);
 		int ymin = Math.max(0, center.y - size.y / 2);
 		int ymax = Math.min(height, center.y + size.y / 2);
 		int zmin = Math.max(0, center.z - size.z / 2);
 		int zmax = Math.min(depth, center.z + size.z / 2);
 
		if (renderGrid) render.beginVoxelMarking();
		if (renderMesh) meshRender.beginVoxelMarking();
 		// Iterate through the bounds and insert generated value
 		for (int x = xmin; x < xmax; x++) {
 			for (int y = ymin; y < ymax; y++) {
 				for (int z = zmin; z < zmax; z++) {
 					byte value = generator.valueForVoxel(x, y, z);
 					if (value != VOXEL_GRID_NO_CHANGE) {
 						setVoxel(x, y, z, value);
 					}
 				}
 			}
 		}
		if (renderMesh) meshRender.endVoxelMarking();
		if (renderGrid) render.endVoxelMarking();
 	}
 
 	public void draw(GL2 gl) {
 		if (renderGrid) render.updateDirtyCells(gl);
 		if (renderMesh) meshRender.updateDirtyChunks(gl);
 
 		if (renderGrid) render.draw(gl);
 		if (renderMesh) meshRender.draw(gl);
 		
 		if (dump) {
 			dump = false;
 			meshRender.dump(gl);
 		}
 	}
 	
 	private boolean dump = false;
 	
 	public void dumpMesh() {
 		if (renderMesh)
 			dump = true;
 	}
 }
