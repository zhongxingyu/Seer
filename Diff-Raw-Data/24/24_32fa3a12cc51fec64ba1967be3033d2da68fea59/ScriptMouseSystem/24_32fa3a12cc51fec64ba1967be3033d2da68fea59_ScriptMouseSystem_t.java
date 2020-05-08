 package entitySystems;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import math.Vector2;
 
 import org.lwjgl.input.Cursor;
 import org.lwjgl.input.Mouse;
 
 import com.google.common.collect.ImmutableSet;
 
 import components.BehaviourComp;
 import components.SpatialComp;
 import components.TransformationComp;
 
 import scripting.Behavior;
 import utils.ButtonState;
 import utils.Camera2D;
 import utils.Circle;
 import utils.MouseButton;
 import utils.MouseState;
 import utils.Rectangle;
 import entityFramework.ComponentMapper;
 import entityFramework.EntityProcessingSystem;
 import entityFramework.EntitySingleProcessingSystem;
 import entityFramework.IComponent;
 import entityFramework.IEntity;
 import entityFramework.IEntityWorld;
 
 public class ScriptMouseSystem extends EntityProcessingSystem {
 
 	private Camera2D camera;
 	
 	public ScriptMouseSystem(IEntityWorld world, Camera2D camera) {
 		super(world, TransformationComp.class, SpatialComp.class, BehaviourComp.class);
 		this.camera = camera;
 		
 	}
 	
 	private ComponentMapper<TransformationComp> transCM;
 	private ComponentMapper<SpatialComp> spatCM;
 	private ComponentMapper<BehaviourComp> scriptCM;
 
 	private MouseState lastMouseState;
 	
 	
 	private enum CollisionState {
 		None,
 		Over,
 		MouseDrag,
 		MouseDragOver
 	}
 	
 	Map<IEntity, CollisionState> collisionStates = new HashMap<>();
 	
 	@Override
 	public void entityAdded(IEntity entity) {
 		collisionStates.put(entity, CollisionState.None);
 	}
 	
 	@Override
 	public void entityRemoved(IEntity entity) {
 		CollisionState state = collisionStates.remove(entity);
 		if(state == CollisionState.Over) {
 			Behavior behaviour = this.scriptCM.getComponent(entity).getBehavior();
 			behaviour.onMouseExit(this.lastMouseState);
 		}
 	}
 	
 	
 
 	@Override
 	public void initialize() {
 		transCM 	= ComponentMapper.create(this.getDatabase(), TransformationComp.class);
 		spatCM 		= ComponentMapper.create(this.getDatabase(), SpatialComp.class);
 		scriptCM 	= ComponentMapper.create(this.getDatabase(), BehaviourComp.class);
 		
 		lastMouseState = new MouseState(Vector2.Zero, Vector2.Zero,Vector2.Zero, Vector2.Zero,
 										0, ButtonState.Depressed, ButtonState.Depressed, ButtonState.Depressed);
 	}
 	
 	protected void processEntity(IEntity entity, MouseState ms) {
 		TransformationComp transC 	 =  transCM.getComponent(entity);
 		SpatialComp spatC			 =  spatCM.getComponent(entity);
 		BehaviourComp  scriptC			 =  scriptCM.getComponent(entity);	
 		CollisionState state = this.collisionStates.get(entity);
 		Behavior behaviour = scriptC.getBehavior();
 		
 		
 		Circle circle = new Circle(Vector2.Zero, spatC.getBounds().getRadius() * transC.getScale().X);
 		boolean collision = Circle.intersects(circle, transC.getPosition(), ms.WorldCoords);

		System.out.println(transC.getPosition());
		System.out.println(ms.WorldCoords);
 		if(collision) {
 			fixCollisionMouseBehaviour(entity, ms, state, behaviour);	
 		} else {
 			fixNonCollisionBehaviour(entity, ms, state, behaviour);	
 		}	
 	}
 
 	private void fixCollisionMouseBehaviour(IEntity entity, MouseState ms,
 			CollisionState state, Behavior behaviour) {
 		if(state == CollisionState.None || state == CollisionState.MouseDrag) {
 			behaviour.onMouseEnter(ms);
 			if(state == CollisionState.None) {
 				this.setState(entity, CollisionState.Over);
 			} else {
 				this.setState(entity, CollisionState.MouseDragOver);
 			}
 		} else if(state == CollisionState.Over || 
 				 state == CollisionState.MouseDragOver){
 			behaviour.onMouseOver(ms);
 		}
 		if(state == CollisionState.MouseDrag || state == CollisionState.MouseDragOver) {
 			behaviour.onMouseDrag(ms);
 		}
 		
 		fixMouseButtonEventsForCollision(entity, ms, state, behaviour, MouseButton.Left);
 		fixMouseButtonEventsForCollision(entity, ms, state, behaviour, MouseButton.Right);
 		fixMouseButtonEventsForCollision(entity, ms, state, behaviour, MouseButton.Middle);
 	}
 	
 	private void fixNonCollisionBehaviour(IEntity entity, MouseState ms,
 			CollisionState state, Behavior behaviour) {
 		if(state == CollisionState.Over || state == CollisionState.MouseDragOver) {
 			behaviour.onMouseExit(ms);
 			if(state == CollisionState.MouseDragOver) {
 				this.setState(entity, CollisionState.MouseDrag);
 			} else {
 				this.setState(entity, CollisionState.None);
 			}
 		}
 		
 		
 		if(state == CollisionState.MouseDrag) {
 			behaviour.onMouseDrag(ms);
 			fixMouseButtonEventsForNonCollision(entity, ms, state, behaviour, MouseButton.Left);
 			fixMouseButtonEventsForNonCollision(entity, ms, state, behaviour, MouseButton.Right);
 			fixMouseButtonEventsForNonCollision(entity, ms, state, behaviour, MouseButton.Middle);
 		}
 	}
 
 	private void fixMouseButtonEventsForCollision(IEntity entity, MouseState ms,
 			CollisionState state, Behavior behaviour, MouseButton button) {
 		if(mouseGotPressed(ms, button)) {
 			behaviour.onMouseDown(ms, button);
 			this.setState(entity, CollisionState.MouseDragOver);
 		} else if(mouseGotReleased(ms, button)){
 			if(state == CollisionState.MouseDragOver) {
 				behaviour.onMouseUp(ms, button);
 				behaviour.onMouseUpAsButton(ms, button);
 				if(noButtonsDown(ms)) {
 					this.setState(entity, CollisionState.Over);
 				}
 			} else {
 				behaviour.onMouseUp(ms, button);
 			}
 		}
 	}
 	
 	private void fixMouseButtonEventsForNonCollision(IEntity entity, MouseState ms,
 			CollisionState state, Behavior behaviour, MouseButton button) {
 		if(mouseGotReleased(ms, button)) {
 			behaviour.onMouseUp(ms, button);
 			this.setState(entity, CollisionState.None);
 		}
 	}
 	
 
 	private void setState(IEntity entity, CollisionState state) {
 		this.collisionStates.put(entity, state);
 	}
 	
 	private boolean mouseGotReleased(MouseState ms, MouseButton button) {
 		switch(button) {
 			case Left:
 				return lastMouseState.LeftButtonState == ButtonState.Pressed &&
 				   ms.LeftButtonState == ButtonState.Depressed;
 			case Right:
 				return lastMouseState.RightButtonState == ButtonState.Pressed &&
 				   ms.RightButtonState == ButtonState.Depressed;
 			case Middle:
 				return lastMouseState.MiddleButtonState == ButtonState.Pressed &&
 				ms.MiddleButtonState == ButtonState.Depressed;
 			default:
 				throw new UnsupportedOperationException("Button not supported " + button);				
 			}
 	}
 
 	private boolean mouseGotPressed(MouseState ms, MouseButton button) {
 		switch(button) {
 			case Left:
 				return lastMouseState.LeftButtonState == ButtonState.Depressed &&
 				   ms.LeftButtonState == ButtonState.Pressed;
 			case Right:
 				return lastMouseState.RightButtonState == ButtonState.Depressed &&
 				   ms.RightButtonState == ButtonState.Pressed;
 			case Middle:
 				return lastMouseState.MiddleButtonState == ButtonState.Depressed &&
 				ms.MiddleButtonState == ButtonState.Pressed;
 			default:
 				throw new UnsupportedOperationException("Button not supported " + button);			
 		}
 	}
 	
 	private boolean noButtonsDown(MouseState ms) {
 		return ms.LeftButtonState == ButtonState.Depressed &&
 			   ms.RightButtonState == ButtonState.Depressed &&
 			   ms.MiddleButtonState == ButtonState.Depressed;			
 	}
 
 	@Override
 	protected void processEntities(ImmutableSet<IEntity> entities) {
 		MouseState ms = generateMouseState();	
 		System.out.println("Processing");
 		
 		for (IEntity entity : entities) {	
 			this.processEntity(entity, ms);
 		}
 		this.lastMouseState = ms;
 	}
 
 	private MouseState generateMouseState() {
 		
 		Rectangle viewport = camera.getViewport();
 		
 		Vector2 viewPos = new Vector2(Mouse.getX(), viewport.Height -Mouse.getY());
 		Vector2 worldPos = camera.getViewToWorldCoords(viewPos);
 		
 		Vector2 viewDelta = Vector2.subtract(viewPos, lastMouseState.ViewCoords);
 		Vector2 worldDelta = Vector2.subtract(worldPos, lastMouseState.WorldCoords);
 		
 		int scrollDeltha = Mouse.getDWheel();
 		
 		ButtonState leftButton = Mouse.isButtonDown(0) ? ButtonState.Pressed : ButtonState.Depressed;
 		ButtonState rightButton = Mouse.isButtonDown(1) ? ButtonState.Pressed : ButtonState.Depressed;
 		ButtonState middleButton = Mouse.isButtonDown(2) ? ButtonState.Pressed : ButtonState.Depressed;
 		
 		return new MouseState(worldPos,
 					   		  viewPos, 
 							  worldDelta,
 							  viewDelta,
 							  scrollDeltha, 
 							  leftButton,
 							  rightButton, 
 							  middleButton);
 	}
 
 }
