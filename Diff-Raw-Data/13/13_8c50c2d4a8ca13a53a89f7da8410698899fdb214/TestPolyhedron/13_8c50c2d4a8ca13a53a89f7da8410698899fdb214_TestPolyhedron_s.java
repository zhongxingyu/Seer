 import Jcg.*;
 import Jcg.geometry.*;
 import Jcg.viewer.*;
 import Jcg.polyhedron.*;
 
 /**
  * A class for testing the Half-edge data structure for representing polyhedral surfaces.
  *
  * @author Luca Castelli Aleardi
  *
  */
 public class TestPolyhedron {
  
 	/**
 	 * Testing a plane triangulation (loaded from an OFF file)
 	 */    
 	public static void test2D() {   	
     	MeshRepresentation m=new MeshRepresentation();
     	m.readOffFile("delaunay100.off");
     	
     	LoadMesh<Point_2> load2D=new LoadMesh<Point_2>();
     	Point_2[] points2D=LoadMesh.Point3DToPoint2D(m.points);
     	Polyhedron_3<Point_2> planarGraph=
     		load2D.createPolyhedron(points2D,m.faceDegrees,m.faces,m.sizeHalfedges);
 
     	System.out.println(planarGraph.verticesToString());   	
     	System.out.println(planarGraph.facesToString());
     	planarGraph.isValid(false);
     	
     	Fenetre f=new Fenetre();
     	f.addPolyhedronEdges(planarGraph);
     }
 
 	/**
 	 * Testing a 3D surface mesh (loaded from an OFF file)
 	 */    
     public static void test3D() {   	
     	MeshRepresentation m=new MeshRepresentation();
    	m.readOffFile("cube.off");
     	LoadMesh<Point_3> load3D=new LoadMesh<Point_3>();
     	
     	Polyhedron_3<Point_3> polyhedron3D=
     		load3D.createPolyhedron(m.points,m.faceDegrees,m.faces,m.sizeHalfedges);
     	System.out.println(polyhedron3D.verticesToString());   	
     	System.out.println(polyhedron3D.facesToString());
     	polyhedron3D.isValid(false);
     	
     	new MeshViewer(polyhedron3D);
     }
 
     public static void main (String[] args) {
     	if (args.length != 1 || (!args[0].equals("2") && !args[0].equals("3"))) {
     		System.out.println("Usage : java testPolyhedron <dim>\n ou <dim> est la dimension (2 ou 3)");
     		return;
     	}
     	if (args[0].equals("2"))
     		test2D();
     	else  if (args[0].equals("3"))
     		test3D();	
     }
 }
