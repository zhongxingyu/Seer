 package assignment3;
 
 import glWrapper.GLHalfEdgeStructure;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import algorithms.energy.SSDMatrices;
 import algorithms.marchable.MarchingCubes;
 import meshes.PointCloud;
 import meshes.exception.DanglingTriangleException;
 import meshes.exception.MeshNotOrientedException;
 import meshes.reader.PlyReader;
 import openGL.MyDisplay;
 import sparse.LinearSystem;
 import sparse.SCIPY;
 import transformers.TrivialSmoother;
 import datastructure.halfedge.HalfEdgeStructure;
 import datastructure.octree.HashOctree;
 
 public class Assignment3Angel {
 
 	public static void main(String[] args) throws IOException,
 			MeshNotOrientedException, DanglingTriangleException {
 
 		marchingCubesDemo();
 
 	}
 
 	public static void marchingCubesDemo() throws MeshNotOrientedException,
 			DanglingTriangleException, IOException {
 
 		PointCloud pc = angel();
 		HashOctree tree = new HashOctree(pc, 7, 1, 1.3f);
		tree.refineTree(1);
 		ArrayList<Float> x = values(tree, pc);
 
 		MarchingCubes dual_mc = new MarchingCubes(tree);
 
 		MyDisplay d = new MyDisplay();
 
 		dual_mc.dualMC(x);
 		HalfEdgeStructure dual_hs = new HalfEdgeStructure(dual_mc.getResult());
 		dual_hs.setTitle("Dual Reconstruction");
 		GLHalfEdgeStructure gl_dual_mesh = new GLHalfEdgeStructure(dual_hs);
 		gl_dual_mesh.configurePreferredShader("shaders/trimesh_flat.vert",
 				"shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
 		d.addToDisplay(gl_dual_mesh);
 
 		HalfEdgeStructure dual_hs_smoothed = new TrivialSmoother()
 				.call(dual_hs);
 		dual_hs_smoothed.setTitle("Smoothed Dual");
 		GLHalfEdgeStructure gl_dual_smoothed = new GLHalfEdgeStructure(
 				dual_hs_smoothed);
 		gl_dual_smoothed.configurePreferredShader("shaders/trimesh_flat.vert",
 				"shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
 		d.addToDisplay(gl_dual_smoothed);
 	}
 
 
 	private static PointCloud angel() throws IOException {
		PointCloud points = PlyReader.readPointCloud("./objs/angel_points.ply",
 				true);
 		points.normalizeNormals();
 		System.out.println(points.points.size());
 		return points;
 	}
 
 	private static ArrayList<Float> values(HashOctree tree, PointCloud pc) throws IOException {
 		ArrayList<Float> out = new ArrayList<>();
 		float lambda0 = 1f;
 		float lambda1 = 0.0001f; // normals suck
 		float lambda2 = 10f;
 		LinearSystem l = SSDMatrices.ssdSystem(tree, pc, lambda0, lambda1, lambda2);
 		SCIPY.solve(l, "", out);
 		return out;
 	}
 }
