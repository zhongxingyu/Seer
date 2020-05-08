 package net.cscott.sdr.anim;
 
 import com.jme.image.Texture;
 import com.jme.scene.state.TextureState;
 import com.jme.system.DisplaySystem;
 import com.jme.util.TextureManager;
 
 /**
  * {@link MenuArrow} is a {@link GradientTriangle} which is sized and textured
  * appropriately for use in the {@link MenuState}.
  * @author C. Scott Ananian
 * @version $Id: MenuArrow.java,v 1.1 2006-11-12 21:57:39 cananian Exp $
  */
 public class MenuArrow extends GradientTriangle {
     final TextureState textureState;
    public MenuArrow(String name, boolean isLeft) {
        super(name, 0, -22, isLeft?-24:24, 0, 0, 22);
         textureState = DisplaySystem.getDisplaySystem()
             .getRenderer().createTextureState();
         textureState.setEnabled(true);
         this.setRenderState(textureState);
         this.setRenderState(BaseState.mkAlpha());
         setSelected(false);
     }
     public void setSelected(boolean isSelected) {
         textureState.setTexture(getTexture(isSelected));
         this.updateRenderState();
     }
     
     private static Texture[] texture = new Texture[2];
     private static Texture getTexture(boolean isSelected) {
         int which = isSelected ? 1 : 0;
         if (texture[which]==null) {
             texture[which] = TextureManager.loadTexture(
                     MenuArrow.class.getClassLoader().getResource(
                     "net/cscott/sdr/anim/menu-arrow"+
                     (isSelected?"-sel":"")+
                     ".png"),
                     Texture.MM_NONE,
                     Texture.FM_LINEAR);
         }
         return texture[which];
     }
 }
