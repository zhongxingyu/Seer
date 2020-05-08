 package test;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import model.Arc;
 import model.Chemin;
 import model.Node;
 import model.ZoneGeo;
 
 import org.junit.Test;
 
 import dijkstra.Dijkstra;
 
 
 public class testDijkstra {
 
 	@Test
 	public void run() {
 		Node n1 = new Node(0,0,0);
 		Node n2 = new Node(0,1,1);
 		Node n3 = new Node(1,0,2);
 		Node n4 = new Node(1,1,3);
 		Node n5 = new Node(20,20,4);
 		
 		Arc a1 = new Arc(n1,n2,10,10,"n1");
 		Arc a2 = new Arc(n1,n3,15,15,"n2");
 		Arc a3 = new Arc(n2,n4,7,7,"n3");
 		Arc a4 = new Arc(n3,n5,40,40,"n4");
 		
 		n1.addOutArc(a1);
 		n2.addInArc(a1);
 		n1.addOutArc(a2);
 		n3.addInArc(a2);
 		n2.addOutArc(a3);
 		n4.addInArc(a3);
 		n3.addOutArc(a4);
 		n5.addInArc(a4);
 		
 		ZoneGeo uneZone= new ZoneGeo();
 		uneZone.addNode(n1);
 		uneZone.addNode(n2);
 		uneZone.addNode(n3);
 		uneZone.addNode(n4);
 		uneZone.addNode(n5);
 		
 		ArrayList<Node> eng= new ArrayList<Node>();
 		eng.add(n5);
 		eng.add(n1);
 		System.out.println("Lancement de Dijkstra");
 
 		ArrayList<Chemin> solution = Dijkstra.solve(uneZone, n1, eng);
 		
 		for(Chemin c: solution)
 		{
			for(Node n :c.getTrajectory())
 			{
 				System.out.print(n.getX()+ " "+n.getY());
 			}
 			System.out.println("Suivant");
 		}
 
 		fail("Not yet implemented");
 	}
 
 }
