 package org.rubenrr.walkeitor.menu;
 
 import org.andengine.engine.Engine;
 import org.andengine.entity.Entity;
 import org.andengine.entity.scene.ITouchArea;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.rubenrr.walkeitor.config.FontConfig;
 import org.rubenrr.walkeitor.manager.FontLoadManager;
 import org.rubenrr.walkeitor.manager.GameManager;
 import org.rubenrr.walkeitor.manager.SceneManager;
 import org.rubenrr.walkeitor.util.TileLocatable;
 
 import java.util.ArrayList;
 
 /**
  * User: Ruben Rubio Rey
  * Date: 7/04/13
  * Time: 6:30 PM
  */
 public abstract class MenuBase implements MenuStrategy {
 
     // Should this class know about how is it displayed ??
     // this shows the position of the MenuOption
     private int height = 0;
 
     // the sprite that we are generating the menu from
     private MenuExtendable sprite = null;
 
     // list of all elements are generated as menu
     private ArrayList<Entity> menuEntities = new ArrayList<Entity>();
 
     public void setSprite(MenuExtendable sprite) {
         this.sprite = sprite;
     }
 
     public Sprite getSprite() {
         return (Sprite)this.sprite;
     }
 
     public TileLocatable getSpriteLocatable() {
         return (TileLocatable)this.sprite;
     }
 
     protected void addMenuOption(final MenuOption menuOption, final MenuAction menuAction) {
 
         String optionText = menuOption.getLabel();
 
         final Text option1 = new Text(this.getSprite().getWidth() + 10, this.height, FontLoadManager.getInstance().get(FontConfig.MENU_STANDARD),
                 optionText, optionText.length(), SceneManager.getInstance().getVertexBufferObjectManager()) {
             @Override
             public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                 if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
                     menuAction.execute(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
                     MenuBase.this.clear();
                     // unSelect units, as when the a menu option is selected the unit should be unselected
                     GameManager.getInstance().unselectUnits();
                 }
                 return true;
             }
 
             @Override
             public String toString() {
                 return "MenuOptionText: " + menuOption.toString() + super.toString();    //To change body of overridden methods use File | Settings | File Templates.
             }
 
             @Override
             public boolean equals(Object o) {
                 if (this == o) return true;
                 if (!(o instanceof Text)) return false;
 
                 Text that = (Text) o;
 
                 if (mText != null ? !mText.equals(that.getText()) : that.getText() != null) return false;
 
                 return true;
             }
         };
 
         this.addEntity(option1);
 
         this.height = this.height + 20;
 
     }
 
     protected void addEntity(final Entity entity) {
         if (this.sprite == null) {
             throw new IllegalArgumentException("Sprite has not been initialized in MenuBanse");
         }
 
         // we make sure that the menu is only added once
         if (!this.menuEntities.contains(entity)) {
             this.sprite.attachChild(entity);
            menuEntities.add(entity);
             SceneManager.getInstance().getScene().registerTouchArea((ITouchArea)entity);
         }
     }
 
     @Override
     public void clear() {
         final Engine.EngineLock engineLock = SceneManager.getInstance().getEngineLock();
         engineLock.lock();
        for (final Entity menuEntity: menuEntities) {
             this.sprite.detachChild(menuEntity);
         }
        menuEntities.clear();
         engineLock.unlock();
         this.height = 0;
     }
 
 }
