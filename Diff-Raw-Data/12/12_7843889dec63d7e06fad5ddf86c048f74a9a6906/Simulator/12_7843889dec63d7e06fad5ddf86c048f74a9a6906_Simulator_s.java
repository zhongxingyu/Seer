 package se2.e.simulator;
 
import geometry.Geometry;
 
 import java.util.List;
 
import petrinet.PetriNet;
 import se2.e.engine3d.Engine3D;
 import se2.e.engine3d.Engine3DFactory;
 import se2.e.engine3d.Engine3DListener;
 import se2.e.simulator.runtime.petrinet.RuntimePetriNet;
 import se2.e.simulator.runtime.petrinet.RuntimeToken;
 import se2.e.simulator.runtime.petrinet.TokenMovement;
 
 /**
  * The Class Simulator.
  * @author Ruxandra, Marius
  */
 public class Simulator implements Engine3DListener {
 
 	/** The selected petri. */
	private PetriNet selectedPetri;
 	
 	/** The geometry. */
 	private Geometry geometry;
 	
 	/** The rpn. */
 	private RuntimePetriNet rpn;
 	
 	/** The engine. */
 	private Engine3D engine;
 
 	/**
 	 * Instantiates a new simulator.
 	 *
 	 * @param geometry the geometry
 	 * @param selectedPetri the selected petri
 	 */
	public Simulator(Geometry geometry, PetriNet selectedPetri) {
 		rpn = new RuntimePetriNet(selectedPetri);
 		this.selectedPetri = selectedPetri;
 		this.geometry = geometry;
 		System.out.println("Constructor Simulator");
 	}
 
 	/**
 	 * Start simulation.
 	 */
 	public void startSimulation() {
 		engine = Engine3DFactory.getEngine();
 		engine.init(this.geometry, null);
 		engine.setEngine3DListener(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see se2.e.engine3d.Engine3DListener#onAnimationFinished(se2.e.simulator.runtime.petrinet.RuntimeToken)
 	 */
 	@Override
 	public void onAnimationFinished(RuntimeToken token) {		
 		rpn.markToken( token);
 		List<TokenMovement> tokenMovements = rpn.fireTransitions();
 		for(TokenMovement tokenMovement : tokenMovements) {
 			engine.startAnimation(tokenMovement.getToken(), tokenMovement.getPlace(), tokenMovement.getAnimation());
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see se2.e.engine3d.Engine3DListener#onStartSimulation()
 	 */
 	@Override
 	public void onStartSimulation() {
 		// fire transitions
 		List<TokenMovement> tokenMovements = rpn.fireTransitions();
 		for(TokenMovement tokenMovement : tokenMovements) {
 			engine.startAnimation(tokenMovement.getToken(), tokenMovement.getPlace(), tokenMovement.getAnimation());
 		}
 	}
 
 	public void onUserInteraction(String label) {
 		//drop token on the place with the label label
 		rpn.dropTokenOnPlace(label);
 		List<TokenMovement> tokenMovements = rpn.fireTransitions();
 		for(TokenMovement tokenMovement : tokenMovements) {
 			engine.startAnimation(tokenMovement.getToken(), tokenMovement.getPlace(), tokenMovement.getAnimation());
 		}
 	}
 
 
 	public void onStopSimulation() {
 		
 	}
 }
