 package se2.e.simulator;
 
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.pnml.tools.epnk.pnmlcoremodel.PetriNetDoc;
 
 import se2.e.engine3d.Engine3D;
 import se2.e.engine3d.Engine3DFactory;
 import se2.e.engine3d.Engine3DListener;
 import se2.e.geometry.Geometry;
 import se2.e.simulator.runtime.petrinet.RuntimePetriNet;
 import se2.e.simulator.runtime.petrinet.RuntimePetriNetFactory;
 import se2.e.simulator.runtime.petrinet.RuntimeToken;
 import se2.e.simulator.runtime.petrinet.TokenMovement;
 import appearance.AppearanceModel;
 
 /**
  * The Class Simulator.
  * 
  * @author Ruxandra, Marius
  */
 public class Simulator implements Engine3DListener {
 
 	/** The selected petri. */
 	private PetriNetDoc selectedPetri;
 
 	/** The geometry. */
 	private Geometry geometry;
 
 	/** The appearance. */
 	AppearanceModel appearance;
 
 	/** The rpn. */
 	private RuntimePetriNet rpn;
 
 	/** The engine. */
 	private Engine3D engine;
 	
 	/** The track width. */
 	private double trackWidth;
 	
 	/** The initial movements. */
 	List<TokenMovement> initialMovements;
 
 	/** The logger. */
 	private static Logger logger = Logger.getAnonymousLogger();
 	
 	
 	/**
 	 * Instantiates a new simulator.
 	 *
 	 * @param geometry the geometry
 	 * @param selectedPetri the selected petri
 	 * @param appearance the appearance
 	 * @param trackWidth the track width
 	 */
 	public Simulator(Geometry geometry, PetriNetDoc selectedPetri, AppearanceModel appearance, double trackWidth) {
 		RuntimePetriNetFactory rpnf = new RuntimePetriNetFactory();
 		rpn = rpnf.getRuntimePetriNet();
 		this.selectedPetri = selectedPetri;
 		this.geometry = geometry;
 		this.appearance = appearance;
 		this.trackWidth = trackWidth;
 	}
 
 	public void initializePetriNet(){
 		initialMovements = rpn.init(selectedPetri);
 	}
 	
 	/**
 	 * Start simulation.
 	 */
 	public void startSimulation() {
 		this.initializePetriNet();
 		engine = Engine3DFactory.getEngine();
 		Set<String> inputPlaces = this.rpn.getInputPlaces();
 		engine.init(this.geometry, this.appearance, inputPlaces, trackWidth);
 		engine.setEngine3DListener(this);
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.Engine3DListener#onAnimationFinished(se2.e.simulator.runtime.petrinet.RuntimeToken)
 	 */
 	@Override
 	public void onAnimationFinished(RuntimeToken token) {
 		rpn.markToken(token);
 		List<TokenMovement> tokenMovements = rpn.fireTransitions();
 		for (TokenMovement tokenMovement : tokenMovements) {
 			if(tokenMovement.isDestroyed())
 				engine.destroyRepresentation(tokenMovement.getToken());
 			else 
 				engine.startAnimation(tokenMovement.getToken(), tokenMovement.getAnimation(), tokenMovement.getGeoLabel());
 
 		}
 
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.Engine3DListener#onStartSimulation()
 	 */
 	@Override
 	public void onStartSimulation() {
 		
 		for (TokenMovement tokenMovement : initialMovements) {
 			if(tokenMovement.isDestroyed())
 				engine.destroyRepresentation(tokenMovement.getToken());
 			else 
 				engine.startAnimation(tokenMovement.getToken(), tokenMovement.getAnimation(), tokenMovement.getGeoLabel());
 		}
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see se2.e.engine3d.Engine3DListener#onUserInteraction(java.lang.String)
 	 */
 	@Override
 	public void onUserInteraction(String label) {
 		// drop token on the place with the label label
 		rpn.dropTokenOnPlace(label);
 		
 		List<TokenMovement> tokenMovements = rpn.fireTransitions();
 		logger.info("Fired transitions after dropping the token. Next tokens will be moved: " + tokenMovements);
 		for (TokenMovement tokenMovement : tokenMovements) {
 			if(tokenMovement.isDestroyed())
 				engine.destroyRepresentation(tokenMovement.getToken());
 			else 
 				engine.startAnimation(tokenMovement.getToken(), tokenMovement.getAnimation(), tokenMovement.getGeoLabel());
 		}
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see se2.e.engine3d.Engine3DListener#onStopSimulation()
 	 */
 	@Override
 	public void onStopSimulation() {
 		rpn = null;
 		this.initializePetriNet();
 		logger.info("Stopped Animation in the Simulator.");
 	}
 }
