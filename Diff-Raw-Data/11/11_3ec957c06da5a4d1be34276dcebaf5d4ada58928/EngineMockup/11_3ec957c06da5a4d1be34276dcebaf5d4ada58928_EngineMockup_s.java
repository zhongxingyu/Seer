 package engine;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import petrinet.Arc;
 import petrinet.INode;
 import petrinet.Petrinet;
 import petrinet.Place;
 import petrinet.Transition;
 import transformation.dependency.PetrinetAdapter;
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import edu.uci.ics.jung.algorithms.layout.StaticLayout;
 import edu.uci.ics.jung.graph.DirectedGraph;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import engine.attribute.ArcAttribute;
 import engine.attribute.PlaceAttribute;
 import engine.attribute.TransitionAttribute;
 import engine.data.JungData;
 import engine.handler.NodeTypeEnum;
 import engine.ihandler.IPetrinetManipulation;
 import exceptions.EngineException;
 
 public class EngineMockup implements IPetrinetManipulation {
 	JungData jung;
 	Petrinet petrinet;
 	
 	public EngineMockup() {
 		DirectedGraph<INode, Arc> graph = new DirectedSparseGraph<INode, Arc>();
 		
 		jung     = new JungData(graph, new StaticLayout<INode, Arc>(graph));		
 		petrinet = PetrinetAdapter.createPetrinet();
 	}
 	
 	
 	public void build() throws EngineException {		
 		DirectedGraph<INode, Arc> graph = new DirectedSparseGraph<INode, Arc>();
 		jung     = new JungData(graph, new StaticLayout<INode, Arc>(graph));
 		petrinet = PetrinetAdapter.createPetrinet();
 		
 		createPlace(1, new Point2D.Double(10, 10));
 		createPlace(1, new Point2D.Double(10, 100));
 		createPlace(1, new Point2D.Double(100, 10));
 		createPlace(1, new Point2D.Double(100, 100));
 
 		createTransition(1, new Point2D.Double(55, 10));
 		createTransition(1, new Point2D.Double(10, 55));
 		createTransition(1, new Point2D.Double(100, 55));
 		createTransition(1, new Point2D.Double(55, 100));
 		
 		List<Place> 	 places 	 = new ArrayList<Place>(petrinet.getAllPlaces());
 		List<Transition> transitions = new ArrayList<Transition>(petrinet.getAllTransitions());
 
 		createArc(1, places.get(0),      transitions.get(0));
 		createArc(1, transitions.get(0), places.get(1));
 		
 		createArc(1, places.get(1),      transitions.get(1));
 		createArc(1, transitions.get(1), places.get(2));
 		
 		createArc(1, places.get(2),      transitions.get(2));
 		createArc(1, transitions.get(2), places.get(3));
 		
 		createArc(1, places.get(3),      transitions.get(3));
 		createArc(1, transitions.get(3), places.get(0));
 	}
 	
 	@Override
 	public void createArc(int id, INode from, INode to) throws EngineException {
 		try {
 			if (from instanceof Place && to instanceof Transition) {
 				jung.createArc(
 					petrinet.createArc("undefined", from, to), 
 					(Place) from, 
 					(Transition) to
 				);
 			} else if (from instanceof Transition && to instanceof Place) {
 				jung.createArc(
 						petrinet.createArc("undefined", from, to), 
 						(Transition) from, 
 						(Place) to
 					);
 			} else {
 				throw new IllegalArgumentException();
 			}			
 		} catch (Exception e) {
 			throw new EngineException(e.getMessage());
 		}
 	}
 
 	@Override
 	public void createPlace(int id, Point2D coordinate) throws EngineException {
 		try {
 			jung.createPlace(
 				petrinet.createPlace("undefined"), 
 				coordinate
 			);		
 		} catch (Exception e) {
 			throw new EngineException(e.getMessage());
 		}		
 	}
 
 	@Override
 	public int createPetrinet() {
 		return 1;
 	}
 
 	@Override
 	public void createTransition(int id, Point2D coordinate)
 			throws EngineException {
 		try {
 			jung.createTransition(
 				petrinet.createTransition("undefined"), 
 				coordinate
 			);		
 		} catch (Exception e) {
 			throw new EngineException(e.getMessage());
 		}
 		
 	}
 
 	@Override
 	public void deleteArc(int id, Arc arc) throws EngineException {
 		throw new EngineException("unsupported");		
 	}
 
 	@Override
 	public void deletePlace(int id, INode place) throws EngineException {
 		throw new EngineException("unsupported");
 		
 	}
 
 	@Override
 	public void deleteTransition(int id, INode transition) throws EngineException {
 		throw new EngineException("unsupported");
 		
 	}
 
 	@Override
 	public ArcAttribute getArcAttribute(int id, Arc arc) {
 		return new ArcAttribute(arc.getMark());
 	}
 
 	@Override
 	public AbstractLayout<INode, Arc> getJungLayout(int id)
 			throws EngineException {
 		return jung.getJungLayout();
 	}
 
 	@Override
 	public PlaceAttribute getPlaceAttribute(int id, INode place)
 			throws EngineException {
 
 		return new PlaceAttribute(
 			((Place) place).getMark(), 
 			((Place) place).getName(), 
 			jung.getPlaceColor((Place) place)
 		);
 	}
 
 	@Override
 	public TransitionAttribute getTransitionAttribute(int id, INode transition)
 			throws EngineException {
 
 		return new TransitionAttribute(
 			((Transition) transition).getTlb(), 
 			((Transition) transition).getName(), 
 			((Transition) transition).getRnw(), 
 			((Transition) transition).isActivated()
 		);
 	}
 
 	@Override
 	public void moveGraph(int id, Point2D relativePosition)
 			throws EngineException {
 		
 		throw new EngineException("unsupported");		
 	}
 
 	@Override
 	public void moveNode(int id, INode node, Point2D relativePosition)
 			throws EngineException {
 		throw new EngineException("unsupported");	
 	}
 
 	@Override
 	public void save(int id, String path, String filename, String format) throws EngineException {
 		throw new EngineException("unsupported");	
 		
 	}
 	
 	@Override
	public int load(String path, String filename) throws EngineException {
		throw new EngineException("unsupported");	
 		return 1;
 	}
 
 	@Override
 	public void setMarking(int id, INode place, int marking)
 			throws EngineException {
 		try {
 			((Place) place).setMark(marking);
 		} catch (Exception e) {
 			throw new EngineException(e.getMessage());
 		}		
 		
 	}
 
 	@Override
 	public void setPname(int id, INode place, String pname)
 			throws EngineException {
 		try {
 			((Place) place).setName(pname);
 		} catch (Exception e) {
 			throw new EngineException(e.getMessage());
 		}
 		
 	}
 
 	@Override
 	public void setTlb(int id, INode transition, String tlb)
 			throws EngineException {	
 		try {
 			((Transition) transition).setTlb(tlb);
 		} catch (Exception e) {
 			throw new EngineException(e.getMessage());
 		}
 	}
 
 	@Override
 	public void setTname(int id, INode transition, String tname)
 			throws EngineException {
 		try {
 			((Transition) transition).setName(tname);
 		} catch (Exception e) {
 			throw new EngineException(e.getMessage());
 		}
 	}
 
 	@Override
 	public void setWeight(int id, Arc arc, int weight) throws EngineException {
 		try {
 			arc.setMark(weight);
 		} catch (Exception e) {
 			throw new EngineException(e.getMessage());
 		}
 		
 	}
 
 	@Override
 	public NodeTypeEnum getNodeType(INode node) throws EngineException {
 		if (node instanceof Place) {
 			return NodeTypeEnum.Place;
 		} else if (node instanceof Transition) {
 			return NodeTypeEnum.Transition;
 		} else {
 			throw new EngineException("");
 		}
 	}
 }
