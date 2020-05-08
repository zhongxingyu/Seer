 package aeroport.sgbag.views;
 
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.*;
 
 import aeroport.sgbag.controler.Simulation;
 import aeroport.sgbag.controler.ViewSelector;
 import aeroport.sgbag.kernel.Bagage;
 import aeroport.sgbag.kernel.Hall;
 
 import java.util.*;
 
 import lombok.*;
 import lombok.extern.log4j.Log4j;
 
 @Log4j
 public class VueHall extends Canvas implements Viewable {
 
 	private Image buffer;
 
 	@Getter
 	@Setter
 	private Hall hall;
 
 	@Getter
 	@Setter
 	private Simulation simulation;
 
 	@Getter
 	@Setter
 	private GC gcBuffer;
 
 	@Getter
 	private TreeMap<Integer, LinkedList<VueElem>> calques;
 	
 	@Getter
 	private ArrayList<VueBagage> bagagesVues;
 
 	public VueHall(Composite parent, int style) {
 		super(parent, style);
 		calques = new TreeMap<Integer, LinkedList<VueElem>>();
 		
 		bagagesVues = new ArrayList<VueBagage>();
 
 		this.addPaintListener(new PaintListener() {
 			public void paintControl(PaintEvent event) {
 				// Create the image to fill the canvas
 				buffer = new Image(getDisplay(), getBounds());
 
 				// Set up the offscreen gc
 				gcBuffer = new GC(buffer);
 
 				// Draw the background
 				gcBuffer.setBackground(event.gc.getBackground());
 				gcBuffer.fillRectangle(buffer.getBounds());
 
 				// Draw all the views ordered by the layers :
 				for (Iterator<Integer> iterator = calques.keySet().iterator(); iterator.hasNext();) {
 					LinkedList<VueElem> vues = calques.get(iterator.next());
 					for (int j = 0; j < vues.size(); j++) {
 						vues.get(j).draw();
 					}
 				}
 
 				// Draw the offscreen buffer to the screen
 				event.gc.drawImage(buffer, 0, 0);
 
 				// Clean up
 				buffer.dispose();
 				gcBuffer.dispose();
 			}
 		});
 		
 		this.addMouseListener(new TraitementClic(this));
 	}
 
 	public void ajouterVue(VueElem vue, int layer) {
 		LinkedList<VueElem> elementOfLayer;
 		if (calques.get(layer) == null) {
 			elementOfLayer = new LinkedList<VueElem>();
 			calques.put(layer, elementOfLayer);
 		} else {
 			elementOfLayer = calques.get(layer);
 		}
 		elementOfLayer.add(vue);
 		if (vue.getParent() == null) {
 			vue.setParent(this);
 		}
 	}
 
 	public void retirerVue(VueElem vue) {
 		boolean trouve = false;
 		for (Iterator<Integer> iterator = calques.keySet().iterator(); iterator
 				.hasNext() && !trouve;) {
 			LinkedList<VueElem> vues = calques.get(iterator.next());
 			for (int j = 0; j < vues.size() && !trouve; j++) {
 				if (vues.get(j).equals(vue)) {
 					vues.remove(j);
 					trouve = true;
 				}
 			}
 		}
 	}
 
 	public void updateView() {
 
 		// Add new bagages views
 		ArrayList<Bagage> bagages = this.getHall().getBagagesList();
 		for (int i = 0; i < bagages.size(); i++) {
 			boolean found = false;
 			for (int j = 0; j < bagagesVues.size() && !found; j++) {
 				if(bagages.get(i).equals(bagagesVues.get(j).getBagage())) {
 					found = true;
 				}
 			}
 			if(!found) {
 				addBagage(bagages.get(i));
 				log.debug("Ajout d'une vue bagage");
 			}
 		}
 
 		// Remove deleted bagages views
 		for (int i = 0; i < bagagesVues.size(); i++) {
 			boolean found = false;
 			for (int j = 0; j < bagages.size() && !found; j++) {
				if(bagagesVues.get(i).getBagage().equals(bagages.get(j))) {
 					found = true;
 				}
 			}
 			if(!found) {
 				//Remove bagage vue
 				bagagesVues.get(i).destroy();
 				bagagesVues.remove(i);
 				log.debug("Suppression d'une vue bagage");
 			}
 		}
 
 		// Update all the views, ordered by the layers
 		for (Iterator<Integer> iterator = calques.keySet().iterator(); iterator
 				.hasNext();) {
 			LinkedList<VueElem> vues = calques.get(iterator.next());
 			for (int j = 0; j < vues.size(); j++) {
 				vues.get(j).updateView();
 			}
 		}
 	}
 	
 	public void addBagage(Bagage bagage) {
 		VueBagage vueBagage = new VueBagage(this);
 		vueBagage.setBagage(bagage);
 		this.ajouterVue(vueBagage, 4);
 		ViewSelector.getInstance().setKernelView(bagage, vueBagage);
 		bagagesVues.add(vueBagage);
 	}
 
 	public void draw() {
 		// Force a redraw event :
 		redraw();
 	}
 
 	public boolean isClicked(Point p) {
 		return true;
 	}
 
 	public Viewable getClickedView(int x, int y) {
 
 		Point p = new Point(x, y);
 		for (Iterator<Integer> iterator = calques.descendingKeySet().iterator(); iterator
 				.hasNext();) {
 			LinkedList<VueElem> vues = calques.get(iterator.next());
 			for (Iterator<VueElem> itVueElem = vues.descendingIterator(); itVueElem
 					.hasNext();) {
 				VueElem v = itVueElem.next();
 				if (v.isClicked(p))
 					return v;
 			}
 		}
 
 		return this;
 	}
 
 }
