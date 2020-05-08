 package sim.demos;
 
 import java.awt.Rectangle;
 
import javax.swing.JFrame;

 import sim.functions.Add;
 import sim.functions.Remove;
 import sim.gui.elements.GuiSettings;
 import sim.structures.Heap;
 import sim.structures.Heap.CompareKey;
 import sim.structures.Info;
 import sim.structures.Queue;
 import sim.structures.Tree.Traversal;
 import sim.structures.Variable;
 
 public class Heapsort {
 	public static void main(String[] args) {
 
 		new Heapsort();
 
 	}
 
 	public Heapsort(){
 		GuiSettings.isAnimated = false;
 		//Use DemoFrame instead of JFrame
 		DemoFrame frame = new DemoFrame("Heapsort", 800, 550);
 
 		Heap h = new Heap(new Rectangle(400,50, 380, 465));
 		h.getGuiElement().showValues(true);
 		h.setTraversal(Traversal.BREADTHFIRST);
 		h.setSortKey(CompareKey.ALPHABETICAL);
 		//	h.addBreadthFirst("6");
 		//	h.addBreadthFirst("13");
 		//	h.addBreadthFirst("4");
 		//	h.addBreadthFirst("5");
 		//	h.addBreadthFirst("9");
 		//	h.addBreadthFirst("2");
 		//	h.addBreadthFirst("1");
 		//	h.addBreadthFirst("12");
 		//	h.addBreadthFirst("7");
 		//	h.addBreadthFirst("8");
 		//	h.addBreadthFirst("11");
 		//	h.addBreadthFirst("10");
 
 		h.addBreadthFirst("Hi");
 		h.addBreadthFirst("Elg");
 		h.addBreadthFirst("Alge");
 		h.addBreadthFirst("Pizza");
 		h.addBreadthFirst("Sitron");
 		h.addBreadthFirst("Oligark");
 		h.addBreadthFirst("Red Bull");
 		h.addBreadthFirst("Traverser");
 
 		h.setMax(false);
 
 		Variable v = new Variable(new Rectangle(225,310, 90, 25), "output", true);
 		Variable i = new Variable(new Rectangle(220,275, 70, 25), "0", false);
 		Remove r = new Remove(new Rectangle(220, 275, 100,25), h, v, i);
 		Queue q = new Queue(new Rectangle(20, 340, 350, 175));	
 		Add p = new Add(new Rectangle(100, 275, 100,25), q,v);
 		
 		String info = 
 				"This is a demonstration of the heapsort algorithm." +
 						"It uses a heap which is built as either a max or a min-heap. " +
 						"A max heap satisfies the condition of each node having only " +
 						"smaller or equal nodes in its subtree. For a min-heap each node " +
 						"has only larger values as its descendants." +
 						System.getProperty("line.separator") +System.getProperty("line.separator")+
 						"This property assures that the root will always be either the largest or the " +
 						"smallest value in the heap." +
 						" Heapsort uses this property to sort the elements by removing the root and placing" +
 						" it in in a queue, rearranging the heap and repeating this " +
 						"until the heap is empty. This will result in a sorted queue." +
 						System.getProperty("line.separator")+System.getProperty("line.separator")+
 						"How to use this demo:" +System.getProperty("line.separator")+
 						"Use the heap settings to specify max/min and the sorting-key which is set to " +
 						"alphabetical by default (changing the traversal is not" +
 						" recommended as this demo uses a hidden index set to 0 to remove the root)." +
 						"Then press 'remove' to get the first value, then 'add' to put it in the queue, and repeat until" +
 						" the heap is empty." +System.getProperty("line.separator")+
 						"P.S. Try sorting a min-heap by length for a little surprise. =)";
 
 		Info in = new Info(new Rectangle(10, 50, 380,200),info, false);
 		Info title = new Info(new Rectangle(50, 10, 100,25),"HEAPSORT", false);
 
 		frame.add(v);
 		frame.add(h);
 		frame.add(q);
 		frame.add(p);
 		frame.add(r);
 		frame.add(in);
 		frame.add(title);
 		
 		//To be on the safe side, add a validate at the end of each demo.
 		frame.validate();
 	}
 }
