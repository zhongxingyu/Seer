 package steamcraft.render;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ResourceLocation;
 
 import org.lwjgl.opengl.GL11;
 
 import steamcraft.HandlerRegistry;
 import steamcraft.TileEntityLamp;
 
 public class TileEntityLampRenderer extends TileEntitySpecialRenderer {
 	public static final ResourceLocation lampOn = new ResourceLocation("steamcraft", "item/lampon.png");
 	public static final ResourceLocation lampOff = new ResourceLocation("steamcraft", "item/lampoff.png");
 	private ModelLampUp lampModelUp;
 	private ModelLampSide lampModelSide;
 
 	public TileEntityLampRenderer() {
 		lampModelUp = new ModelLampUp();
 		lampModelSide = new ModelLampSide();
 	}
 
 	@Override
 	public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f) {
 		renderTileEntityLampAt((TileEntityLamp) tileentity, d, d1, d2, f);
 	}
 
 	public void renderTileEntityLampAt(TileEntityLamp tileentitylamp, double d, double d1, double d2, float f) {
 		Block block = tileentitylamp.getBlockType();
 		GL11.glPushMatrix();
 		float f1 = 0.6666667F;
 		int i = tileentitylamp.getBlockMetadata();
 		float f3 = 0.0F;
 		float f2 = 1.0F;
 		if (i == 2) {
 			f3 = 180F;
 		}
 		if (i == 4) {
 			f3 = 90F;
 		}
 		if (i == 3) {
 			f3 = -90F;
 		}
 		if (i == 6) {
 			f2 = 180F;
 		}
 		GL11.glTranslatef((float) d + 0.5F, (float) d1 + 0.75F * f1, (float) d2 + 0.5F);
 		GL11.glRotatef(f3, 0.0F, 1.0F, 0.0F);
 		if (i == 6) {
 			GL11.glRotatef(f2, 0.0F, 0.0F, 1.0F);
 		}
 		GL11.glTranslatef(0.0F, -0.3125F, -0.4375F);
 		if (block == getTorchElectricActive() || block == getTorchElectricIdle()) {
 			lampModelUp.BracketWide.showModel = false;
 			lampModelUp.CrossbarLeft.showModel = false;
 			lampModelUp.CrossbarRight.showModel = false;
 			lampModelSide.CrossbarLeft.showModel = false;
 			lampModelSide.CrossbarRight.showModel = false;
 		} else if (block == getWirelessLampActive() || block == getWirelessLampIdle()) {
 			lampModelUp.BracketWide.showModel = true;
 			lampModelUp.CrossbarLeft.showModel = true;
 			lampModelUp.CrossbarRight.showModel = true;
 			lampModelSide.CrossbarLeft.showModel = true;
 			lampModelSide.CrossbarRight.showModel = true;
 		}
 		if (block == getTorchElectricActive() || block == getWirelessLampActive()) {
             bindTexture(lampOn);
 		} else if (block == getTorchElectricIdle() || block == getWirelessLampIdle()) {
             bindTexture(lampOff);
 		}
 		GL11.glPushMatrix();
 		GL11.glScalef(f1, -f1, -f1);
 		if (i == 5 || i == 6) {
 			lampModelUp.renderSign();
 		} else {
 			lampModelSide.renderSign();
 		}
 		GL11.glPopMatrix();
 		float f4 = 0.01666667F * f1;
 		GL11.glTranslatef(0.0F, 0.5F * f1, 0.07F * f1);
 		GL11.glScalef(f4, -f4, f4);
 		GL11.glNormal3f(0.0F, 0.0F, -1F * f4);
 		GL11.glDepthMask(false);
 		GL11.glDepthMask(true);
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		GL11.glPopMatrix();
 	}
 
 	private static Block getTorchElectricActive() {
 		return HandlerRegistry.getBlock("steamcraft:electricLampOn").get();
 	}
 
 	private static Block getTorchElectricIdle() {
		return HandlerRegistry.getBlock("steamcraft:electricLamp").get();
 	}
 
 	private static Block getWirelessLampActive() {
 		return HandlerRegistry.getBlock("steamcraft:wirelessLampOn").get();
 	}
 
 	private static Block getWirelessLampIdle() {
		return HandlerRegistry.getBlock("steamcraft:wirelessLamp").get();
 	}
 }
