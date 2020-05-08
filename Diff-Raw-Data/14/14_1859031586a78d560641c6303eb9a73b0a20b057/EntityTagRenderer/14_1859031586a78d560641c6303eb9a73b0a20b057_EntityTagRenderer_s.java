 package boq.cctags.client;
 
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.Render;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.entity.Entity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.client.event.TextureStitchEvent;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.event.ForgeSubscribe;
 
 import org.lwjgl.opengl.GL11;
 
 import boq.cctags.tag.*;
 import boq.cctags.tag.TagIcons.IconData;
 import boq.utils.render.*;
 
 import com.google.common.base.Strings;
 
 public class EntityTagRenderer extends Render {
 
     private static class Key {
         public final IconData icon;
         public final TagType type;
 
         private Key(IconData icon, TagType type) {
             this.icon = icon;
             this.type = type;
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             return prime * (prime + icon.hashCode()) + type.hashCode();
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
 
             if (obj instanceof Key) {
                 Key other = (Key)obj;
                 return other.type == type && icon.equals(other.icon);
             }
             return false;
         }
 
         @Override
         public String toString() {
             return "Key [" + icon + ", type=" + type + "]";
         }
     }
 
     private static final double Z_FIGHTER = 0.0005;
 
     private final static ParameterModel<Key> iconModels = new ParameterModel<Key>() {
 
         @Override
         public void compile(Key param) {
             final TagIcons icons = TagIcons.instance;
             GL11.glDisable(GL11.GL_CULL_FACE);
 
             Tessellator tes = new Tessellator();
             tes.setColorOpaque(255, 255, 255);
             tes.startDrawingQuads();
            tes.setNormal(0.0F, 0.0F, -1.0F);
             tes.setTranslation(0, 0, 2 * Z_FIGHTER);
             final double size = param.type == TagType.BIG ? 0.5 : 0.25;
             final Icon background = param.type == TagType.GLASS ? icons.iconBackgroundGlass : icons.iconBackgroundPaper;
             if (param.type == TagType.GLASS) {
                 GL11.glEnable(GL11.GL_BLEND);
                 GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
             }
 
             RenderUtils.drawRectangle(tes, -size, -size, size, size, icons.iconMarker);
             tes.draw();
 
             GL11.glColor3d(1, 1, 1);
 
             tes.startDrawingQuads();
            tes.setNormal(0.0F, 0.0F, -1.0F);
             tes.setTranslation(0, 0, 2 * Z_FIGHTER);

             RenderUtils.drawRectangle(tes, -size, -size, size, size, background);
             tes.draw();
 
             final IconData icon = param.icon;
             icon.render(tes, -size, -size, size, size, 3 * Z_FIGHTER);
 
             GL11.glEnable(GL11.GL_CULL_FACE);
         }
     };
 
     public void doRender(EntityTag entity, double x, double y, double z, float yaw, float partialTickTime) {
         GL11.glPushMatrix();
         GL11.glTranslated(x, y, z);
         GL11.glColor4f(1, 1, 1, 1);
 
         if (RenderManager.field_85095_o) { // bounding box
             GL11.glDisable(GL11.GL_TEXTURE_2D);
             renderOffsetAABB(entity.boundingBox, -entity.posX, -entity.posY, -entity.posZ);
             GL11.glEnable(GL11.GL_TEXTURE_2D);
         }
 
         final TagData data = entity.data;
         final ForgeDirection side = data.side;
 
         CenterRotationHelper.setupSide(side);
         CenterRotationHelper.setupRotation(data.rotation);
         GL11.glTranslated(0, 0, 0.5);
 
         BoxHelper.setColor(data.color);
 
         IconData render = TagIcons.instance.getIconData(Strings.nullToEmpty(data.icon));
         Key key = new Key(render, data.tagType);
         func_110777_b(entity); // set texture
         iconModels.render(key);
         GL11.glPopMatrix();
     }
 
     @Override
     public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTickTime) {
         doRender((EntityTag)entity, x, y, z, yaw, partialTickTime);
     }
 
     @Override
     protected ResourceLocation func_110775_a(Entity entity) {
         return renderManager.renderEngine.func_130087_a(1); // item texture
     }
 
     @ForgeSubscribe
     public void onTextureChange(TextureStitchEvent.Post evt) {
         iconModels.flush();
     }
 }
