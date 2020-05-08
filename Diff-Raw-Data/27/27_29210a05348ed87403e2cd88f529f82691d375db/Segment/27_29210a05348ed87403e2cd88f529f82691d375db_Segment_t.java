 package common.map;
 
 import initial3d.Initial3D;
 import initial3d.Texture;
 import initial3d.engine.Color;
 import initial3d.engine.Material;
 import initial3d.engine.Mesh;
 import initial3d.engine.MeshLOD;
 import initial3d.engine.ReferenceFrame;
 import initial3d.engine.Vec3;
 import initial3d.engine.MeshContext;
 
 import java.util.Random;
 
 public class Segment {
 	public static final int size = 16;
 	public static final int vertScale = 8;
 	public static final int horzScale = 2;
 	private static final int terr_tex_size = 32;
 
 	private float[][] heightmap = null;
 
 	public final int xPos;
 	public final int zPos;
 	public final long id;
 
 	
 	//TODO need to work out how to handel textures better than this... ^^;
 	public static Material terr_mtl;
 
 	static {
 		terr_mtl = new Material(new Color(0.4f, 0.4f, 0.4f), new Color(0.1f,
 				0.1f, 0.1f), 1f);
 		Texture terr_tx = Initial3D.createTexture(terr_tex_size);
 		// populate the terrain texture
 		for (int u = 0; u < terr_tex_size; u++) {
 			for (int v = 0; v < terr_tex_size; v++) {
 				terr_tx.setPixel(u, v, 1f, 0.0f,
 						(float) (Math.random() * 0.15 + 0.5), 0.0f);
 			}
 		}
 		terr_tx.composeMipMaps();
 		terr_tx.useMipMaps(true);
 
 		terr_mtl = new Material(terr_mtl, terr_tx, null, null);
 	}
	
	public static long getID(int posx, int posz){
		return (long)(posx<<32) | (((long)(posz)) & 0xFFFFFFFF);
	}
 
 	public Segment(int _xPos, int _zPos, float[][] heightmap) {
 		xPos = _xPos;
 		zPos = _zPos;
		id = getID(_xPos, _zPos);
 		this.heightmap = heightmap;
 	}
 
 	public float[][] getData() {
 		return this.heightmap;
 	}
 
 	public MeshContext getMeshContext() {
 		Mesh terr_mesh = new Mesh();
 
 		terr_mesh.add(lowLOD());
 
 		MeshContext terr_mc = new MeshContext(terr_mesh, terr_mtl, ReferenceFrame.SCENE_ROOT);
 
 		return terr_mc;
 	}
 
 	private MeshLOD highLOD() {
 
 		MeshLOD mLOD = new MeshLOD(size * size * 2 * 2, 3, (size + 1)
 				* (size + 1) * 2, 20, (size + 1) * (size + 1) * 2, 1);
 
 		int[][] ind = new int[(size + 2)][(size + 2)];
 
 		// first create vectors and normals
 		for (int z = 1; z <= size + 1; z++) {
 			for (int x = 1; x <= size + 1; x++) {
 				float h = heightmap[x][z] * vertScale;
 
 				ind[x - 1][z - 1] = mLOD.addVertex(x * horzScale
 						+ (xPos * size * horzScale), h, z * horzScale
 						+ (zPos * size * horzScale));
 
 				// try to construct normal from noise pseudo-derivative
 				Vec3 d0 = Vec3.create(-2, heightmap[x - 1][z] * vertScale - h,
 						0).unit();
 				Vec3 d1 = Vec3
 						.create(0, heightmap[x][z + 1] * vertScale - h, 2)
 						.unit();
 				Vec3 d2 = Vec3
 						.create(2, heightmap[x + 1][z] * vertScale - h, 0)
 						.unit();
 				Vec3 d3 = Vec3.create(0, heightmap[x][z - 1] * vertScale - h,
 						-2).unit();
 
 				Vec3 normal = (d0.cross(d1).add(d1.cross(d2)).add(d2.cross(d3))
 						.add(d3.cross(d0))).unit();
 
 				mLOD.addNormal(normal.x, normal.y, normal.z);
 
 				// mLOD.addNormal(0, 1, 0);
 			}
 		}
 
 		Random r = new Random(32);
 
 		int[] tri_vt_1a = new int[] { mLOD.addTexCoord(0, 1),
 				mLOD.addTexCoord(1, 1), mLOD.addTexCoord(0, 0) };
 		int[] tri_vt_1b = new int[] { mLOD.addTexCoord(1, 1),
 				mLOD.addTexCoord(1, 0), mLOD.addTexCoord(0, 0) };
 		int[] tri_vt_2a = new int[] { mLOD.addTexCoord(0, 1),
 				mLOD.addTexCoord(1, 1), mLOD.addTexCoord(1, 0) };
 		int[] tri_vt_2b = new int[] { mLOD.addTexCoord(0, 1),
 				mLOD.addTexCoord(1, 0), mLOD.addTexCoord(0, 0) };
 
 		// add polygons by theorized indexed values ^^;
 		for (int z = 0; z < size; z++) {
 			for (int x = 0; x < size; x++) {
 				int[] tri0, tri1;
 
 				if (r.nextBoolean()) {
 					tri0 = new int[] { ind[x + 1][z], ind[x][z],
 							ind[x + 1][z + 1] };
 					tri1 = new int[] { ind[x][z], ind[x][z + 1],
 							ind[x + 1][z + 1] };
 					mLOD.addPolygon(tri0, tri_vt_1a, tri0, null);
 					mLOD.addPolygon(tri1, tri_vt_1b, tri1, null);
 				} else {
 					tri0 = new int[] { ind[x + 1][z], ind[x][z], ind[x][z + 1] };
 					tri1 = new int[] { ind[x + 1][z], ind[x][z + 1],
 							ind[x + 1][z + 1] };
 					mLOD.addPolygon(tri0, tri_vt_2a, tri0, null);
 					mLOD.addPolygon(tri1, tri_vt_2b, tri1, null);
 				}
 
 			}
 		}
 
 		return mLOD;
 	}
 
 	//lowest LOD is 2 triangles for the whole context
 	private MeshLOD lowLOD() {
 		MeshLOD mLOD = new MeshLOD(3, 4, 5, 1, 5, 1);
 
 		
 		// first create vectors and normals
 		for (int z = 1; z <= size + 1; z+=size) {
 			for (int x = 1; x <= size + 1; x+=size) {
 				float h = heightmap[x][z] * vertScale;
 
 				mLOD.addVertex(x * horzScale
 						+ (xPos * size * horzScale), h, z * horzScale
 						+ (zPos * size * horzScale));
 
 				// try to construct normal from noise pseudo-derivative
 				Vec3 d0 = Vec3.create(-2, heightmap[x - 1][z] * vertScale - h,
 						0).unit();
 				Vec3 d1 = Vec3
 						.create(0, heightmap[x][z + 1] * vertScale - h, 2)
 						.unit();
 				Vec3 d2 = Vec3
 						.create(2, heightmap[x + 1][z] * vertScale - h, 0)
 						.unit();
 				Vec3 d3 = Vec3.create(0, heightmap[x][z - 1] * vertScale - h,
 						-2).unit();
 
 				Vec3 normal = (d0.cross(d1).add(d1.cross(d2)).add(d2.cross(d3))
 						.add(d3.cross(d0))).unit();
 
 				mLOD.addNormal(normal.x, normal.y, normal.z);
 			}
 		}
 
 		// add polygons by theorized indexed values ^^;
 		int[] tri0 = new int[] { 1, 4, 2 };
 		int[] tri1 = new int[] { 1, 3, 4 };
 
 		mLOD.addPolygon(tri0, null, tri0, null);
 		mLOD.addPolygon(tri1, null, tri1, null);
 
 		return mLOD;
 	}
 }
