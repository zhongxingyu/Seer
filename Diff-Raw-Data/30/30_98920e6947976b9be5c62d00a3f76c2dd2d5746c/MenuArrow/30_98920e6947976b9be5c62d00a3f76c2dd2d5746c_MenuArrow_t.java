 package net.cscott.sdr.anim;
 
 import com.jme.image.Texture;
 import com.jme.scene.state.TextureState;
 import com.jme.system.DisplaySystem;
 import com.jme.util.TextureManager;
 
 /**
  * {@link MenuArrow} is a {@link GradientTriangle} which is sized and textured
  * appropriately for use in the {@link MenuState}.
  * @author C. Scott Ananian
 * @version $Id: MenuArrow.java,v 1.2 2006-11-13 04:27:09 cananian Exp $
  */
 public class MenuArrow extends GradientTriangle {
     final TextureState textureState;
    public MenuArrow(String name, BaseState st, boolean isLeft) {
        super(name, st.x(0), st.y(-22), st.x(isLeft?-24:24), st.y(0), st.x(0), st.y(22));
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
