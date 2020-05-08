 package game.floor;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import game.entity.WallEntity;
 import initial3d.Initial3D;
 import initial3d.Texture;
 import initial3d.engine.Color;
 import initial3d.engine.Material;
 import initial3d.engine.Mesh;
 import initial3d.engine.MeshContext;
 import initial3d.engine.MeshLOD;
 import initial3d.engine.MovableReferenceFrame;
 import initial3d.engine.ReferenceFrame;
 import initial3d.engine.Scene;
 import initial3d.engine.Vec3;
 
 public class Floor {
 
 	private final int size;
 	private final MeshContext mesh;
 	private static final int detail = 4; 
 	
 	private List<WallEntity> walls = new ArrayList<WallEntity>();
 	
 	private static final Texture floor_tex;
 	private static final Material floor_mtl;
 	
 	static {
 		BufferedImage bi = null;
 		try {
 			bi = ImageIO.read(new File("resources/texturetiles.png"));
 		} catch (IOException e) {
 			throw new AssertionError(e);
 		}
 		floor_tex = Initial3D.createTexture(Texture.requiredSize(Math.max(bi.getHeight(), bi.getWidth())));
 		floor_tex.drawImage(bi);
 		floor_tex.useMipMaps(true);
 		floor_tex.composeMipMaps();
 		
 		Material mat = new Material(Color.WHITE, 
 				new Color(0.6f, 0.6f, 0.6f), 
 				new Color(0.3f, 0.3f, 0.3f), 
 				new Color(0f, 0f, 0f), 1f, 1f);
 		
 		floor_mtl = new Material(mat, floor_tex, null, null);
 	}
 	
 	public Floor(Space[][] _floor){
 		size = _floor.length;
 		//add the walls
 		
 		//TODO FIXME stuff
 		long i = 125l;
 		for(int x=0; x < size; x++){
 			for(int z=0; z < size; z++){
 				if(_floor[x][z].type==Space.WALL){
 					walls.add(new WallEntity(i++, Vec3.create(x,0,z)));
 				}
 			}
 		}
 		
 		mesh = buildMesh(_floor);
 	}
 	
 	private MeshContext buildMesh(Space[][] _floor){
 		int meshSize = size*detail+1;
 		int[][][] ver = new int[meshSize][detail*2+1][meshSize];
 		//TODO change the anmount of texture coords to be loaded AND LOAD THEM YOU FUCKWIT
 		MeshLOD meshlod = new MeshLOD(meshSize*meshSize*detail*2+1, 4, 
 				meshSize*meshSize*detail*2+1, 25, 6, 1);
 		
 		
 		
 		//could be used leter one maybe?
 		//int[][][] ind = new int[meshSize+1][detail+1][meshSize + 1];
 		
 		int pos_x = meshlod.addNormal(1, 0, 0);
 		int neg_x = meshlod.addNormal(-1, 0, 0);
 		int pos_z = meshlod.addNormal(0, 0, 1);
 		int neg_z = meshlod.addNormal(0, 0, -1);
 		int pos_y = meshlod.addNormal(0, 1, 0);
 
 		int[] pos_x_norm = new int[]{pos_x, pos_x, pos_x, pos_x};
 		Vec3[] pos_x_verPos = new Vec3[]{
 			Vec3.create(0, 1, 1), Vec3.create(0, 0, 1),
 			Vec3.create(0, 0, 0), Vec3.create(0, 1, 0)};
 		
 		int[] neg_x_norm = new int[]{neg_x, neg_x, neg_x, neg_x};
 		Vec3[] neg_x_verPos = new Vec3[]{
 			Vec3.create(0, 1, 0), Vec3.create(0, 0, 0),
 			Vec3.create(0, 0, 1), Vec3.create(0, 1, 1)};
 	
 		int[] pos_z_norm = new int[]{pos_z, pos_z, pos_z, pos_z};
 		Vec3[] pos_z_verPos = new Vec3[]{
 			Vec3.create(0, 1, 0), Vec3.create(0, 0, 0),
 			Vec3.create(1, 0, 0), Vec3.create(1, 1, 0)};
 		
 		int[] neg_z_norm = new int[]{neg_z, neg_z, neg_z, neg_z};
 		Vec3[] neg_z_verPos = new Vec3[]{
 			Vec3.create(1, 1, 0), Vec3.create(1, 0, 0),
 			Vec3.create(0, 0, 0), Vec3.create(0, 1, 0)};
 		
 		int[] pos_y_norm = new int[]{pos_y, pos_y, pos_y, pos_y};
 		Vec3[] pos_y_verPos = new Vec3[]{
 				Vec3.create(0, 0, 0), Vec3.create(0, 0, 1),
 				Vec3.create(1, 0, 1), Vec3.create(1, 0, 0)};
 
 		int[] wall_top_tex_coord = new int[]{
 				meshlod.addTexCoord(0, 0),
 				meshlod.addTexCoord(0, 0.25),
 				meshlod.addTexCoord(0.25, 0.25),
 				meshlod.addTexCoord(0.25, 0)};
 		int[] wall_side_tex_coord = new int[]{
 				meshlod.addTexCoord(0.25, 0),
 				meshlod.addTexCoord(0.25, 0.25),
 				meshlod.addTexCoord(0.5, 0.25),
 				meshlod.addTexCoord(0.5, 0)};
 		int[] floor_tex_coord = new int[]{
 				meshlod.addTexCoord(0.25, 0.25),
 				meshlod.addTexCoord(0.25, 0.5),
 				meshlod.addTexCoord(0.5, 0.5),
 				meshlod.addTexCoord(0.5, 0.25)};
 		int[] roof_tex_coord = new int[]{
 				meshlod.addTexCoord(0, 0.25),
 				meshlod.addTexCoord(0, 0.5),
 				meshlod.addTexCoord(0.25, 0.5),
 				meshlod.addTexCoord(0.25, 0.25)};
 		
 		
 		
 		
 		for(int x=1; x <= size-1; x++){
 			for(int z=0; z < size; z++){
 				Space s = _floor[x-1][z], r = _floor[x][z];
 
 				
 				//ensures that a wall of sorts needs to be drawn
 				if(s.type!=r.type){
 					int[] norm;
 					Vec3[] vert;
 					//on the left facing right
 					if(s.type==Space.WALL && r.type == Space.EMPTY){
 						norm = pos_x_norm;
 						vert = pos_x_verPos;
 					//on the right facing left
 					}else{
 						norm = neg_x_norm;
 						vert = neg_x_verPos;
 					}
 					for(double vy=0; vy < 1; vy += 1d/detail){
 						for(double vz=z; vz < z + 1; vz += 1d/detail){
 							//could span this out a bit TODO
 							int[] ver_final = new int[]{
 									verAt(x + vert[0].x/detail, vy + vert[0].y/detail, vz + vert[0].z/detail, ver, meshlod),
 									verAt(x + vert[1].x/detail, vy + vert[1].y/detail, vz + vert[1].z/detail, ver, meshlod),
 									verAt(x + vert[2].x/detail, vy + vert[2].y/detail, vz + vert[2].z/detail, ver, meshlod),
 									verAt(x + vert[3].x/detail, vy + vert[3].y/detail, vz + vert[3].z/detail, ver, meshlod)};
 							meshlod.addPolygon(ver_final, wall_side_tex_coord, norm, null);
 						}
 					}
 				}
 			}
 		}
 		
 		for(int x=0; x < size; x++){
 			for(int z=1; z <= size-1; z++){
 				Space s = _floor[x][z-1], r = _floor[x][z];
 
 				
 				//ensures that a wall of sorts needs to be drawn
 				if(s.type!=r.type){
 					int[] norm;
 					Vec3[] vert;
 					//above facing down
 					if(s.type==Space.WALL && r.type == Space.EMPTY){
 						norm = pos_z_norm;
 						vert = pos_z_verPos;
 					//below facing up
 					}else{
 						norm = neg_z_norm;
 						vert = neg_z_verPos;
 					}
 					for(double vy=0; vy < 1; vy += 1d/detail){
 						for(double vx=x; vx < x + 1; vx += 1d/detail){
 							//could span this out a bit TODO
 							//Off by one with the z here? TODO FIXME
 							int[] ver_final = new int[]{
 									verAt(vx + vert[0].x/detail, vy + vert[0].y/detail, z + vert[0].z/detail, ver, meshlod),
 									verAt(vx + vert[1].x/detail, vy + vert[1].y/detail, z + vert[0].z/detail, ver, meshlod),
 									verAt(vx + vert[2].x/detail, vy + vert[2].y/detail, z + vert[0].z/detail, ver, meshlod),
 									verAt(vx + vert[3].x/detail, vy + vert[3].y/detail, z + vert[0].z/detail, ver, meshlod)};
 							meshlod.addPolygon(ver_final, wall_side_tex_coord, norm, null);
 						}
 					}
 				}
 			}
 		}
 		
 		
 		//now we create the floor
 		for(int x=0; x < size; x++){
 			for(int z=0; z < size; z++){
 				Vec3[] vert = pos_y_verPos;
 				int[] text_coord;
 				int y;
 				if(_floor[x][z].type==Space.WALL){
 					y = 1;
 					text_coord = wall_top_tex_coord;
 				}else{
 					y = 0;
 					text_coord = floor_tex_coord;
 				}
 				
 				for(double vz=z; vz < z + 1; vz += 1d/detail){
 					for(double vx=x; vx < x + 1; vx += 1d/detail){
 						int[] ver_final = new int[]{
 								verAt(vx + vert[0].x/detail, y + vert[0].y/detail, vz + vert[0].z/detail, ver, meshlod),
 								verAt(vx + vert[1].x/detail, y + vert[1].y/detail, vz + vert[1].z/detail, ver, meshlod),
 								verAt(vx + vert[2].x/detail, y + vert[2].y/detail, vz + vert[2].z/detail, ver, meshlod),
 								verAt(vx + vert[3].x/detail, y + vert[3].y/detail, vz + vert[3].z/detail, ver, meshlod)};
 						meshlod.addPolygon(ver_final, text_coord, pos_y_norm, null);
 					}
 				}
 			}
 		}
 		
 
		System.out.println(meshlod.getVertices().count());

 		Mesh mesh = new Mesh();
 		mesh.add(meshlod);
 		
 		MovableReferenceFrame rf = new MovableReferenceFrame(ReferenceFrame.SCENE_ROOT);
 		rf.setPosition(Vec3.create(-0.5, 0, -0.5));
 		MeshContext mesh_con = new MeshContext(mesh, floor_mtl, rf);
 		
 		mesh_con.setFarCull(10);
 		
 		return mesh_con;
 	}
 	
 	private int verAt(double _x, double _y, double _z, int[][][]_ver, MeshLOD _meshlod){
 		int x = (int) Math.round(_x*detail);
 		int y = (int) Math.round(_y*detail);
 		int z = (int) Math.round(_z*detail);
 		if(_ver[x][y][z]==0){
 			_ver[x][y][z] = _meshlod.addVertex(_x, _y, _z);
 		}
 		return _ver[x][y][z];
 	}
 	
 	public List<WallEntity> getWalls(){
 		return walls;
 	}
 	
 	public void addToScene(Scene s)
 	{
 //		for(WallEntity w : walls)
 //		{
 //			w.addToScene(s);
 //		}
 		s.addDrawable(mesh);
 		
 	}
 }
