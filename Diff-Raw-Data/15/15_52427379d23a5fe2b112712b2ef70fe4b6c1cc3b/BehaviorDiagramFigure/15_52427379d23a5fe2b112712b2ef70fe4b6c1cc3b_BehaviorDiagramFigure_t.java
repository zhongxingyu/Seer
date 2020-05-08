 package fiuba.mda.ui.figures;
 
 import fiuba.mda.model.BehaviorRelation;
 import org.eclipse.draw2d.FreeformLayer;
 import org.eclipse.draw2d.FreeformLayout;
 
 import fiuba.mda.model.BehaviorDiagram;
 import fiuba.mda.model.BehaviorState;
 import fiuba.mda.model.Representation;
 import fiuba.mda.utilities.SimpleEvent.Observer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Figure which displays a behavior diagram
  */
 public class BehaviorDiagramFigure extends FreeformLayer {
 	private Observer<BehaviorDiagram> onStatesChanged = new Observer<BehaviorDiagram>() {
 		@Override
 		public void notify(BehaviorDiagram observable) {
 			rebindChildFigures();
 		}
 	};
 
     private Observer<BehaviorDiagram> onRelationChanged = new Observer<BehaviorDiagram>() {
         @Override
         public void notify(BehaviorDiagram observable) {
             rebindChildFigures();
         }
     };
 
 	private final BehaviorDiagram component;
 
    private List<BehaviorStateFigure> behaviorStateFigures;
 	/**
 	 * Creates a new @{link BehaviorDiagramFigure} instance
 	 * 
 	 * @param component
 	 *            the {@link BehaviorDiagram} instance to display
 	 */
 	public BehaviorDiagramFigure(final BehaviorDiagram component) {
 		this.component = component;
 		component.statesChangedEvent().observe(onStatesChanged);
         component.relationChangedEvent().observe(onRelationChanged);
 		setLayoutManager(new FreeformLayout());
 		rebindChildFigures();
         behaviorStateFigures = new ArrayList<>();
     }
 
 	private void rebindChildFigures() {
 		removeAll();
 		for (Representation<BehaviorState> state : component.getStates()) {
             BehaviorStateFigure figure = new BehaviorStateFigure(state);
             add(figure);
            getBehaviorStateFigures().add(figure);
 		}
         for (Representation<BehaviorRelation> relation : component.getRelations()) {
            add(new BehaviorRelationFigure(relation,getBehaviorStateFigures()));
         }
 	}
 
    public List<BehaviorStateFigure> getBehaviorStateFigures() {
        if (behaviorStateFigures == null){
            behaviorStateFigures = new ArrayList<>();
        }
        return behaviorStateFigures;
    }
 }
