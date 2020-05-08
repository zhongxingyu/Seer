 package assignment2;
 
 import glWrapper.GLHashtree;
 import glWrapper.GLHashtreeCellAdjacencies;
 import glWrapper.GLHashtreeVertexAdjacencies;
 import glWrapper.GLHashtreeVertices;
import glWrapper.GLPointCloud;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import meshes.PointCloud;
import meshes.reader.ObjReader;
 import meshes.reader.PlyReader;
 import openGL.MyDisplay;
 import openGL.gl.GLDisplayable;
 
 public class HashOctreeDemo {
 	public static void main(String[] args) throws IOException{
 		//Load a wireframe mesh
		PointCloud pc = ObjReader.readAsPointCloud("./objs/sphere.obj", true);
		//PointCloud pc = PlyReader.readPointCloud("./objs/octreeTest2.ply", true);
 		HashOctree ho = new HashOctree(pc, 4, 1, 1);
 		GLDisplayable pcGL = new GLHashtree(ho);
 		GLDisplayable pcGL2 = new GLHashtree(ho);
 		//... do something with it, display it ....
 		HashOctreeVertex v = ho.getVertex(0b1000010000000);
 		
 		System.out.println(ho.getNbr_v2v(v, 0b001));
		GLDisplayable pcGL5 = new GLPointCloud(pc);
		pcGL5.configurePreferredShader("shaders/default.vert", "shaders/default.frag", null);
 		MyDisplay disp = new MyDisplay();
 		pcGL.configurePreferredShader("shaders/octree.vert", "shaders/octree.frag", "shaders/octree.geom");
 		disp.addToDisplay(pcGL);
 		pcGL2.configurePreferredShader("shaders/octree.vert", "shaders/octree.frag", "shaders/octree_parents.geom");
 		disp.addToDisplay(pcGL2);
 		GLDisplayable pcGL3 = new GLHashtreeCellAdjacencies(ho); 
 		pcGL3.configurePreferredShader("shaders/octree.vert", "shaders/octree.frag", "shaders/octree_parents.geom");
 		disp.addToDisplay(pcGL3);
 		GLDisplayable pcGL4 = new GLHashtreeVertexAdjacencies(ho); 
 		pcGL4.configurePreferredShader("shaders/octree.vert", "shaders/octree.frag", "shaders/octree_adj.geom");
 		disp.addToDisplay(pcGL4);
		disp.addToDisplay(pcGL5);
 	}
 }
