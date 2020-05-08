 package at.ac.uibk.akari.common.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.TextMenuItem;
 import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import android.opengl.GLES20;
 import at.ac.uibk.akari.common.listener.MenuItemSeletedEvent;
 import at.ac.uibk.akari.common.listener.MenuListener;
 import at.ac.uibk.akari.utils.FontLoader;
 import at.ac.uibk.akari.utils.FontLoader.FontType;
 import at.ac.uibk.akari.utils.ListenerList;
 
 public abstract class AbstractMenuScene extends MenuScene implements IOnMenuItemClickListener {
 
 	protected ListenerList listeners;
 
 	private VertexBufferObjectManager vertexBufferObjectManager;
 
 	private List<MenuItem> itemTypes;
 
 	public AbstractMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager, final List<MenuItem> itemTypes) {
 		super(camera);
		this.itemTypes = itemTypes;
 		this.listeners = new ListenerList();
 		this.vertexBufferObjectManager = vertexBufferObjectManager;
		this.initGUI();
 	}
 
 	public AbstractMenuScene(final Camera camera, final VertexBufferObjectManager vertexBufferObjectManager) {
 		this(camera, vertexBufferObjectManager, null);
 	}
 
	private void initGUI() {
 
 		this.setSceneOptions();
 
		this.setItemTypes(this.getItemTypes());
 		this.setOnMenuItemClickListener(this);
 	}
 
 	public void setItemTypes(final List<MenuItem> itemTypes) {
 		if (!this.getItemTypes().equals(itemTypes)) {
 			this.itemTypes = itemTypes;
 			this.clearMenuItems();
 
 			// add items to menu
 			FontType fontType = this.getItemsFontType();
 			float sizePessed = 1.1f;
 			float sizeNormal = 1f;
 
 			List<IMenuItem> menuItems = new ArrayList<IMenuItem>();
 
 			for (MenuItem item : this.getItemTypes()) {
 				IMenuItem menuItem = new ScaleMenuItemDecorator(new TextMenuItem(item.ordinal(), FontLoader.getInstance().getFont(fontType), item.getText(), this.vertexBufferObjectManager), sizePessed, sizeNormal);
 				menuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 				menuItems.add(menuItem);
 			}
 
 			for (IMenuItem menuItem : menuItems) {
 				this.addMenuItem(menuItem);
 			}
 
 			this.buildAnimations();
 		}
 	}
 
 	protected abstract void setSceneOptions();
 
 	protected abstract FontType getItemsFontType();
 
 	public List<MenuItem> getItemTypes() {
 		if (this.itemTypes == null) {
 			this.itemTypes = new ArrayList<MenuItem>();
 		}
 		return this.itemTypes;
 	}
 
 	@Override
 	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
 		if (pMenuScene == this) {
 			for (MenuItem itemType : this.getItemTypes()) {
 				if (pMenuItem.getID() == itemType.ordinal()) {
 					this.fireMenuItemSelected(itemType);
 					break;
 				}
 			}
 		}
 		return true;
 	}
 
 	protected void fireMenuItemSelected(final MenuItem itemType) {
 		MenuItemSeletedEvent event = new MenuItemSeletedEvent(this, itemType);
 		for (MenuListener listener : this.listeners.getListeners(MenuListener.class)) {
 			listener.menuItemSelected(event);
 		}
 	}
 
 	public void addMenuListener(final MenuListener listener) {
 		this.listeners.addListener(MenuListener.class, listener);
 	}
 
 	public void removeMenuListener(final MenuListener listener) {
 		this.listeners.removeListener(MenuListener.class, listener);
 	}
 
 	public VertexBufferObjectManager getVertexBufferObjectManager() {
 		return this.vertexBufferObjectManager;
 	}
 }
