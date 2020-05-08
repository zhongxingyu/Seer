 package petrinetze.impl;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import petrinetze.ActionType;
 import petrinetze.IArc;
 import petrinetze.IGraphElement;
 import petrinetze.INode;
 import petrinetze.IPetrinet;
 import petrinetze.IPetrinetListener;
 import petrinetze.IPlace;
 import petrinetze.IPost;
 import petrinetze.IPre;
 import petrinetze.ITransition;
 
 public class Petrinet implements IPetrinet {
 	
 	private int id;
 	private final Set<IPetrinetListener> listeners = new HashSet<IPetrinetListener>();
 	private Set<IPlace> places;
 	private Set<ITransition> transitions;
 	private Set<IArc> arcs;
 	private IGraphElement graphElements;
 	
 	
 	public Petrinet(int id) {
 		this.id = id;
 		places = new HashSet<IPlace>();
 		transitions = new HashSet<ITransition>();
 		arcs = new HashSet<IArc>();
 		graphElements = new GraphElement();
 	}
 
 	
 	
 	public Petrinet() {
		new Petrinet(UUID.getnID());
 	}
 
 
 
 	@Override
 	public IPlace createPlace(String name) {
 		final Place p = new Place(UUID.getpID());
 		p.setName(name);
 		places.add(p);
 		return p;
 	}
 
 	@Override
 	public void deletePlaceById(int id) {
 		IPlace toBeDelete = null;
 		for (IPlace p : places) {
 			if (p.getId() == id) {
 				toBeDelete = p;
 				break;
 			}
 		}
 		if (toBeDelete != null) {
 			places.remove(toBeDelete);
 		}
 	}
 
 	@Override
 	public ITransition createTransition(String name) {
 		final Transition t = new Transition(UUID.getpID());
 		t.setName(name);
 		transitions.add(t);
 		return t;
 
 	}
 
 	@Override
 	public void deleteTransitionByID(int id) {
 		ITransition toBeDelete = null;
 		for (ITransition t : transitions) {
 			if (t.getId() == id) {
 				toBeDelete = t;
 				break;
 			}
 		}
 		if (toBeDelete != null) {
 			transitions.remove(toBeDelete);
 		}
 		
 	}
 
 	@Override
 	public IArc createArc(String name) {
 		final IArc arc = new Arc(UUID.getaID());
 		arc.setName(name);
 		fireChanged(arc);
 		return arc;
 	}
 
 	@Override
 	public void deleteArcByID(int id) {
 		IArc toBeDelete = null;
 		for (IArc a : arcs) {
 			if (a.getId() == id) {
 				toBeDelete = a;
 				break;
 			}
 		}
 		if (toBeDelete != null) {
 			arcs.remove(toBeDelete);
 		}
 	}
 
 	@Override
 	public Set<ITransition> getActivatedTransitions() {
 		return null;
 	}
 
 	@Override
 	public Set<INode> fire(int id) {
 		return null;
 	}
 
 	@Override
 	public Set<INode> fire() {
 		return null;
 	}
 
 	@Override
 	public IPre getPre() {
 		return null;
 	}
 
 	@Override
 	public IPost getPost() {
 		return null;
 	}
 
 	@Override
 	public int getId() {
 		return 0;
 	}
 
 	private void fireChanged(INode element) {
 		final List<IPetrinetListener> listeners;
 		
 		synchronized (this.listeners){
 			listeners = new ArrayList<IPetrinetListener>(this.listeners);
 		}
 		
 		for (IPetrinetListener l : listeners)  {
 			l.changed(this, element, ActionType.changed);
 		}
 	}
 
 	@Override
 	public Set<IPlace> getAllPlaces() {
 		return places;
 	}
 
 	@Override
 	public Set<ITransition> getAllTransitions() {
 		return transitions;
 	}
 
 	@Override
 	public Set<IArc> getAllArcs() {
 		return arcs;
 	}
 
 	@Override
 	public IGraphElement getAllGraphElement() {
 		final Set<INode> nodes = new HashSet<INode>();
 		final Set<IArc> arcs = new HashSet<IArc>();
 		for (INode node : places) {
 			nodes.add(node);
 		}
 		for (INode node : transitions) {
 			nodes.add(node);
 		}
 		for (IArc arc : arcs) {
 			nodes.add(arc);
 		}
 		((GraphElement) graphElements).setNodes(nodes);
 		((GraphElement) graphElements).setArcs(arcs);
 		
 		return graphElements;
 	}
 
 	public void addPetrinetListener(IPetrinetListener l) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void removePetrinetListener(IPetrinetListener l) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Petrinet [id=" + id + ", places=" + places + ", transitions="
 				+ transitions + ", arcs=" + arcs + "]";
 	}
 
 
 	
 }
