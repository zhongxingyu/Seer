 package scene;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import component.HUD;
 import component.Component;
 import component.ModalListener;
 import component.SceneLayer;
 import component.Tooltip;
 import component.Positionable.ReferencePoint;
 import component.Visible;
 import component.modal.Modal;
 
 /**
  * How the game displays information to the player.  Inherited by the containers which execute the game functionality
  */
 public abstract class Scene extends BasicGameState implements Visible, ModalListener {
 	protected static GameContainer container;
 	private static Tooltip tooltip;
 	
 	protected SceneLayer backgroundLayer;
 	protected SceneLayer mainLayer;
 	protected SceneLayer hudLayer;
 	protected SceneLayer modalLayer;
 	
 	private boolean active;
 	private boolean paused;
 	
 	@Override
 	public void init(GameContainer container, StateBasedGame game) throws SlickException {
 		Scene.container = container;
 		
 		backgroundLayer = new SceneLayer(container);
 		backgroundLayer.setVisibleParent(this);
 		mainLayer = new SceneLayer(container);
 		mainLayer.setVisibleParent(this);
 		hudLayer = new SceneLayer(container);
 		hudLayer.setVisibleParent(this);
 		modalLayer = new SceneLayer(container);
 		modalLayer.setVisibleParent(this);
 	}
 
 	@Override
 	public final void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
 		backgroundLayer.render(container, g);
 		mainLayer.render(container, g);
 		hudLayer.render(container, g);
 		modalLayer.render(container, g);
 		
 		if (tooltip != null) {
 			if (tooltip.getOwner().isMouseOver()) {
 				tooltip.render(container, g);
 			} else {
 				removeTooltip();
 			}
 		}
 	}
 
 	@Override
 	public abstract void update(GameContainer container, StateBasedGame game, int delta) throws SlickException;
 
 	private void pause() {
 		paused = true;
 	}
 	
 	private void resume() {
 		paused = false;
 	}
 	
 	public boolean isPaused() {
 		return paused;
 	}
 	
 	public SceneLayer[] getLayers() {
 		return new SceneLayer[] {
 				backgroundLayer,
 				mainLayer,
 				hudLayer,
				modalLayer
 		};
 	}
 	
 	/**
 	 * Activates a modal screen 
 	 * @param modal the screen to be displayed via modality
 	 */
 	public void showModal(Modal modal) {
 		pause();
 		removeTooltip();
 		
 		modalLayer.add(modal);
 		
 		backgroundLayer.setAcceptingInput(false);
 		mainLayer.setAcceptingInput(false);
 		hudLayer.setAcceptingInput(false);
 		
 		modalLayer.setAcceptingInput(true);
 		modalLayer.setVisible(true);
 	}
 	
 	@Override
 	public void dismissModal(Modal modal, boolean cancelled) {
 		resume();
 		removeTooltip();
 		
 		// Make Modal invisible and set accepting input to false before removing it!
 		modalLayer.setVisible(false);
 		modalLayer.setAcceptingInput(false);
 		modalLayer.remove(modal);
 		
 		backgroundLayer.setAcceptingInput(true);
 		mainLayer.setAcceptingInput(true);
 		hudLayer.setAcceptingInput(true);
 	}
 	
 	public void showHUD(HUD hud) {
 		hudLayer.add(hud);
 	}
 	
 	/**
 	 * Prepare for entry. Transition is about to start to this scene. 
 	 */
 	public void prepareToEnter() {
 		setActive(true);
 	}
 	
 	/**
 	 * Prepare for departure. Transition is about to end this scene. 
 	 */
 	public void prepareToLeave() {
 		return;
 	}
 	
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)  {
 		mainLayer.setAcceptingInput(true);
 		hudLayer.setAcceptingInput(true);
 		modalLayer.setAcceptingInput(true);
 	}
 	
 	@Override
 	public void leave(GameContainer container, StateBasedGame game)  {
 		mainLayer.setAcceptingInput(false);
 		hudLayer.setAcceptingInput(false);
 		modalLayer.setAcceptingInput(false);
 		setActive(false);
 		
 		removeTooltip();
 	}
 	
 	public void disable() {
 		mainLayer.setAcceptingInput(false);
 		hudLayer.setAcceptingInput(false);
 		modalLayer.setAcceptingInput(false);
 	}
 	
 	@Override
 	public abstract int getID();
 	
 	public static void showTooltip(int x, int y, Component owner, String message) {
 		if (tooltip != null) {
 			if (tooltip.getOwner() != owner && owner.isVisible()) {
 				tooltip = new Tooltip(container, owner, message);
 			}
 			
 			if (tooltip.getOwner().isVisible()) {
 				setTooltipPosition(x, y);
 			} else{
 				removeTooltip();
 			}
 		} else if (owner.isVisible()){
 			tooltip = new Tooltip(container, owner, message);
 			setTooltipPosition(x, y);
 		}
 	}
 	
 	public static void removeTooltip() {
 		tooltip = null;
 	}
 	
 	private static void setTooltipPosition(int x, int y) {
 		ReferencePoint referencePoint = ReferencePoint.TOPLEFT;
 		
 		final int BOTTOM_OFFSET = 20;
 		final int TOP_OFFSET = 10;
 		
 		if (x + tooltip.getWidth() >= container.getWidth()) {
 			if (y + BOTTOM_OFFSET + tooltip.getHeight() >= container.getHeight()) {
 				referencePoint = ReferencePoint.BOTTOMRIGHT;
 				y -= TOP_OFFSET;
 			} else {
 				referencePoint = ReferencePoint.TOPRIGHT;
 				y += BOTTOM_OFFSET;
 			}
 		} else {
 			if (y + BOTTOM_OFFSET + tooltip.getHeight() >= container.getHeight()) {
 				referencePoint = ReferencePoint.BOTTOMLEFT;
 				y -= TOP_OFFSET;
 			} else {
 				referencePoint = ReferencePoint.TOPLEFT;
 				y += BOTTOM_OFFSET;
 			}
 		}
 		
 		tooltip.setPosition(new Vector2f(x, y), referencePoint);
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	private void setActive(boolean active) {
 		this.active = active;
 	}
 	
 	public Input getInput() {
 		return container.getInput();
 	}
 	
 	public void setVisible(boolean visible) {
 		SceneLayer[] layers  = getLayers();
 		for (SceneLayer layer : layers) {
 			layer.setVisible(false);
 		}
 	}
 	
 	public boolean isVisible() {
 		return active;
 	}
 }
