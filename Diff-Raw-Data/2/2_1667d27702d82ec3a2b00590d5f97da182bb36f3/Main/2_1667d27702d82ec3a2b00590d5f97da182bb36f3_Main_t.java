 package assignment7;
 
 import java.util.ArrayList;
 import java.util.List;
 import assignment7.EpidemicSystem;
 import assignment7.EpidemicSystem.Node;
 
 
 public class Main {
 	final static List<Patient> list = new ArrayList<Patient>();
 	
 	public static void main( String[] args ) {
 		list.add(new Patient(160, 72.7));
 		list.add(new Patient(178, 82.7));
 		list.add(new Patient(167, 76.9));
		list.add(new Patient(177, 82.0));
 		list.add(new Patient(156, 79.3));
 		list.add(new Patient(180, 76.4));
 		list.add(new Patient(176, 78.9));
 		list.add(new Patient(187, 73.1));
 		list.add(new Patient(155, 76.7));
 		list.add(new Patient(190, 78.8));
 		
 		EpidemicSystem es = new EpidemicSystem();
 		
 		sendListToTree(list, es);
 		
 		es.printInOrder(es.root);
 		
 	}
 	
 	public static void sendListToTree( List<Patient> list, EpidemicSystem es ) {
 		Node root = new Node(list.get(0).getWeight());
 		es.root = root;
 		for( Patient current : list ) {
 			es.insert(root, current.getWeight());
 		}
 	}
 }
