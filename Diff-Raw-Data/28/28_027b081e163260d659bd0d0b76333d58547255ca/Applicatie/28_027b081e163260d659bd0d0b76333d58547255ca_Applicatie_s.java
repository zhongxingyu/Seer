 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 public class Applicatie {
 
 	private PERTNetwerk<Vak, Integer> netwerk = new PERTNetwerk<Vak, Integer>();
 	List<Vak> vakken = new ArrayList<Vak>();
 	List<Vak> TopoList = new ArrayList<Vak>();
 
 	public Applicatie() {
 		// createTestingData();
 		createExampleData();
 		createTopologicalList();
		getMinimumWeight();
		getMaximumWeigth();
 
 		for (Vak vak : TopoList) {
 			System.out.println(vak.getNaam() + " " + vak.getMinPathValue()
 					+ " " + vak.getMaxPathValue());
 		}
 
 	}
 
 	public void createTestingData() {
 		Vak vak1 = new Vak("D&A opdracht 1");
 		Vak vak2 = new Vak("Concurrency opdracht 1");
 		Vak vak3 = new Vak("Server en clients start sprint 1");
 		Vak vak4 = new Vak("Concurrency opdracht 1.5");
 		Vak vak5 = new Vak("Server en clients week 1 sprint 1");
 		Vak vak6 = new Vak("Concurrency opdracht 2");
 		Vak vak7 = new Vak("Server en clients week 2 sprint 1");
 		Vak vak8 = new Vak("Concurrency opdracht 2.5");
 		Vak vak9 = new Vak("Server en clients afsluiten sprint 1");
 		Vak vak10 = new Vak("Concurrency opdracht 3");
 		Vak vak11 = new Vak("Server en clients start sprint 2");
 		Vak vak12 = new Vak("Concurrency opdracht 3.5");
 		Vak vak13 = new Vak("Server en clients week 1 sprint 2");
 		Vak vak14 = new Vak("Concurrency opdracht 4");
 		Vak vak15 = new Vak("Server en clients week 2 sprint 2");
 		Vak vak16 = new Vak("D&A opdracht 5");
 		Vak vak17 = new Vak("Concurrency opdracht 4.5");
 		Vak vak18 = new Vak("Server en clients afsluiten sprint 2");
 
 		vakken.add(vak1);
 		vakken.add(vak2);
 		vakken.add(vak3);
 		vakken.add(vak4);
 		vakken.add(vak5);
 		vakken.add(vak6);
 		vakken.add(vak7);
 		vakken.add(vak8);
 		vakken.add(vak9);
 		vakken.add(vak10);
 		vakken.add(vak11);
 		vakken.add(vak12);
 		vakken.add(vak13);
 		vakken.add(vak14);
 		vakken.add(vak15);
 		vakken.add(vak16);
 		vakken.add(vak17);
 		vakken.add(vak18);
 
 		for (Vak v : vakken) {
 			netwerk.putVertex(v);
 		}
 
 		netwerk.putEdge(vak1, vak2, 4);
 		netwerk.putEdge(vak1, vak3, 2);
 		netwerk.putEdge(vak1, vak4, 3);
 		netwerk.putEdge(vak2, vak5, 5);
 		netwerk.putEdge(vak2, vak6, 7);
 		netwerk.putEdge(vak3, vak7, 3);
 		netwerk.putEdge(vak3, vak8, 2);
 		netwerk.putEdge(vak4, vak9, 1);
 		netwerk.putEdge(vak4, vak10, 8);
 		netwerk.putEdge(vak4, vak11, 2);
 		netwerk.putEdge(vak5, vak12, 5);
 		netwerk.putEdge(vak5, vak13, 3);
 		netwerk.putEdge(vak5, vak14, 7);
 		netwerk.putEdge(vak6, vak15, 9);
 		netwerk.putEdge(vak6, vak16, 10);
 		netwerk.putEdge(vak6, vak17, 11);
 		netwerk.putEdge(vak6, vak18, 13);
 	}
 
 	public void createExampleData() {
 		Vak vak1 = new Vak("A");
 		Vak vak2 = new Vak("B");
 		Vak vak3 = new Vak("C");
 		Vak vak4 = new Vak("D");
 		Vak vak5 = new Vak("E");
 		Vak vak6 = new Vak("F");
 		Vak vak7 = new Vak("G");
 
 		vakken.add(vak1);
 		vakken.add(vak2);
 		vakken.add(vak3);
 		vakken.add(vak4);
 		vakken.add(vak5);
 		vakken.add(vak6);
 		vakken.add(vak7);
 
 		for (Vak v : vakken) {
 			netwerk.putVertex(v);
 		}
 
 		netwerk.putEdge(vak1, vak2, 3);
 		netwerk.putEdge(vak1, vak4, 1);
 		netwerk.putEdge(vak1, vak7, 3);
 		netwerk.putEdge(vak2, vak6, 1);
 		netwerk.putEdge(vak2, vak3, 1);
 		netwerk.putEdge(vak4, vak5, 1);
 		netwerk.putEdge(vak5, vak6, 1);
 		netwerk.putEdge(vak7, vak6, 2);
 		netwerk.putEdge(vak6, vak3, 1);
 	}
 
	public void getMinimumWeight() {
 		for (Vak v : TopoList) {
 			int order = TopoList.indexOf(v);
 			if (order == 0) {
 				v.setMinPathValue(0);
 			} else {
 				for (Vak vak : TopoList) {
 					int order2 = TopoList.indexOf(vak);
 					if (order2 < order) {
 						if (netwerk.hasEdge(vak, v)) {
 							if (v.getMinPathValue() < netwerk.edge(vak, v)
 									+ vak.getMinPathValue()) {
 								v.setMinPathValue(netwerk.edge(vak, v)
 										+ vak.getMinPathValue());
 							}
 						}
 					} else {
 						break;
 					}
 				}
 			}
 		}
 	}
 
	public void getMaximumWeigth() {
 		Collections.reverse(TopoList);
 
 		for (Vak v : TopoList) {
 			int order = TopoList.indexOf(v);
			if (order == 0 || order == TopoList.size() - 1) {
 				v.setMaxPathValue(v.getMinPathValue());
 			}
 			for (Vak vak : TopoList) {
 				if (netwerk.hasEdge(vak, v)) {
 					int maxValue = v.getMaxPathValue() - netwerk.edge(vak, v);
 						vak.setMaxPathValue(maxValue);
 				}
 			}
 		}
 		Collections.reverse(TopoList);
 	}
 
 	public void createTopologicalList() {
 		while (vakken.size() > 0) {
 			Iterator<Vak> i = vakken.iterator();
 			while (i.hasNext()) {
 				Vak v = i.next();
 				boolean hasedge = false;
 				for (Vak vak : vakken) {
 					if (vak != v) {
 						if (netwerk.hasEdge(vak, v)) {
 							hasedge = true;
 							break;
 						}
 					}
 				}
 				if (!hasedge) {
 					TopoList.add(v);
 					i.remove();
 				}
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		new Applicatie();
 	}
 }
