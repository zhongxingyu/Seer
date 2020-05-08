 package projectrts.view;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import projectrts.controller.InGameState;
 import projectrts.model.IGame;
 import projectrts.model.entities.IEntity;
 import projectrts.view.controls.MoveControl;
 import projectrts.view.spatials.AbstractSpatial;
 import projectrts.view.spatials.SelectSpatial;
 import projectrts.view.spatials.SpatialFactory;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 // TODO Markus: ADD JAVADOC!
 class EntityHandler implements PropertyChangeListener {
	private SimpleApplication app;
	private IGame game;
 	// The node for all entities
 	private final Node entities = new Node("entities");
 	// The node for the selected graphics
 	private final Node selected = new Node("selected");
 	// The modifier value for converting lengths between model and world.
 	// TODO Markus: PMD: Variables that are final and static should be in all caps.
 	private final static float mod = InGameState.MODEL_TO_WORLD;
 
 	public EntityHandler(SimpleApplication app, IGame game) {
 		this.app = app;
 		this.game = game;
 		game.getEntityManager().addListener(this);
 	}
 
 	public void initialize() {
 		integrateNewEntities(game.getEntityManager().getAllEntities());
 
 		// Attach the entities node to the root node, connecting it to the
 		// world.
 		this.app.getRootNode().attachChild(entities);
 		this.app.getRootNode().attachChild(selected);
 	}
 
 	private void integrateNewEntities(List<IEntity> newEntities) {
 		for (int i = 0; i < newEntities.size(); i++) {
 			integrateNewEntity(newEntities.get(i));
 		}
 	}
 
 	private void integrateNewEntity(IEntity newEntity) {
 		// Create shape.
 		// The location of the entity is initialized to (0, 0, 0) but is then
 		// instantly translated to the correct place by moveControl.
 		// Gets the size from the model and converts it to world size.
 		Box shape = new Box(new Vector3f(0, 0, 0),
 				(newEntity.getSize() * mod) / 2,
 				(newEntity.getSize() * mod) / 2, 0);
 		// Create spatial.
 		AbstractSpatial entitySpatial = SpatialFactory.createEntitySpatial(
 				newEntity.getClass().getSimpleName() + "Spatial", newEntity
 						.getClass().getSimpleName(), shape, newEntity);
 		// Attach spatial to the entities node.
 		entities.attachChild(entitySpatial);
 	}
 
 	private void removeDeadEntity(IEntity entity) {
 		for (Spatial spatial : entities.getChildren()) {
 			if (spatial.getControl(MoveControl.class).getEntity()
 					.equals(entity)) {
 				spatial.removeFromParent();
 			}
 		}
 	}
 
 	/**
 	 * Draws the selected graphics for all entities in the passed list.
 	 * 
 	 * @param selectedEntities
 	 *            A list of selected entities.
 	 * @param controlList
 	 *            A list of controls for the select-spatials, one for each
 	 *            spatial.
 	 */
 	public void drawSelected() {
 		List<IEntity> selectedEntities = game.getEntityManager()
 				.getSelectedEntities();
 
 		// Remove all previously selected graphics
 		selected.detachAllChildren();
 
 		for (int i = 0; i < selectedEntities.size(); i++) {
 			// Sets the location of the spatial to (0, 0, -1) to make sure it's
 			// behind the entities that use (x, y, 0).
 			// The control will instantly translate it to the correct location.
 			Box circle = new Box(new Vector3f(0, 0, -1), (selectedEntities.get(
 					i).getSize() + 0.3f)
 					/ 2 * mod, (selectedEntities.get(i).getSize() + 0.3f) / 2
 					* mod, 0);
 			AbstractSpatial circleSpatial = SpatialFactory.createEntitySpatial(
 					SelectSpatial.class.getSimpleName(), selectedEntities
 							.get(i).getName(), circle, selectedEntities.get(i));
 			// Attach spatial to the selected node, connecting it to the world.
 			selected.attachChild(circleSpatial);
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (evt.getPropertyName().equals("entityCreated")) {
 			if (evt.getNewValue() instanceof IEntity) {
 				integrateNewEntity((IEntity) evt.getNewValue());
 			}
 		} else if (evt.getPropertyName().equals("entityRemoved")
 				&& evt.getOldValue() instanceof IEntity) {
 			removeDeadEntity((IEntity) evt.getOldValue());
 			drawSelected();
 		} else if (evt.getPropertyName().equals("entitySelected")) {
 			drawSelected();
 		}
 	}
 
 }
