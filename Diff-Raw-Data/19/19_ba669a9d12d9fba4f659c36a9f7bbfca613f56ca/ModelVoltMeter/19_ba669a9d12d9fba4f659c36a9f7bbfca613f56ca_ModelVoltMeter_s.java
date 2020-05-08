 package ip.industrialProcessing.power.meters;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.client.model.ModelRenderer;
 import ip.industrialProcessing.client.render.ModelAnimatedMachine;
 
 public class ModelVoltMeter extends ModelAnimatedMachine {
 
 	// fields
 	ModelRenderer Support1;
 	ModelRenderer Support3;
 	ModelRenderer Back;
 	ModelRenderer Body;
 	ModelRenderer Viewer;
 	ModelRenderer ConnectorPower;
 	ModelRenderer Needle;
 	ModelRenderer Support2;
 	ModelRenderer Support4;
 	ModelRenderer ConnectorPowerTop;
 	private boolean ampMeter;
 
 	public ModelVoltMeter(boolean ampMeter) {
 		textureWidth = 64;
 		textureHeight = 64;
 		this.ampMeter = ampMeter;
 		Support1 = new ModelRenderer(this, 28, 30);
 		Support1.addBox(0F, 0F, 0F, 16, 2, 2);
 		Support1.setRotationPoint(-8F, 22F, 6F);
 		Support1.setTextureSize(64, 64);
 		Support1.mirror = true;
 		setRotation(Support1, 0F, 0F, 0F);
 		Support3 = new ModelRenderer(this, 29, 31);
 		Support3.addBox(0F, 0F, 0F, 12, 1, 1);
 		Support3.setRotationPoint(6F, 23F, 6F);
 		Support3.setTextureSize(64, 64);
 		Support3.mirror = true;
 		setRotation(Support3, 0F, 1.570796F, 0F);
 		Back = new ModelRenderer(this, 0, 6);
 		Back.addBox(0F, 0F, 0F, 1, 12, 14);
 		Back.setRotationPoint(-7F, 10F, 7F);
 		Back.setTextureSize(64, 64);
 		Back.mirror = true;
 		setRotation(Back, 0F, 1.570796F, 0F);
 		Body = new ModelRenderer(this, 6, 34);
 		Body.addBox(0F, 0F, 0F, 12, 14, 16);
 		Body.setRotationPoint(-8F, 9F, 6F);
 		Body.setTextureSize(64, 64);
 		Body.mirror = true;
 		setRotation(Body, 0F, 1.570796F, 0F);
 		Viewer = new ModelRenderer(this, 0, 6);
 		Viewer.addBox(0F, 0F, 0F, 1, 12, 14);
 		Viewer.setRotationPoint(-7F, 10F, -5.5F);
 		Viewer.setTextureSize(64, 64);
 		Viewer.mirror = true;
 		setRotation(Viewer, 0F, 1.570796F, 0F);
 		ConnectorPower = new ModelRenderer(this, 0, 13);
 		ConnectorPower.addBox(0F, 0F, 0F, 6, 6, 1);
 		ConnectorPower.setRotationPoint(-3F, 13F, 7F);
 		ConnectorPower.setTextureSize(64, 64);
 		ConnectorPower.mirror = true;
 		setRotation(ConnectorPower, 0F, 0F, 0F);
 		Needle = new ModelRenderer(this, 16, 9);
 		Needle.addBox(-0.5F, -10F, -0.5F, 1, 10, 1);
 		Needle.setRotationPoint(0F, 21F, -5.6F);
 		Needle.setTextureSize(64, 64);
 		Needle.mirror = true;
 		setRotation(Needle, 0F, 0.7853982F, 0F);
 		Support2 = new ModelRenderer(this, 28, 30);
 		Support2.addBox(0F, 0F, 0F, 16, 2, 2);
 		Support2.setRotationPoint(-8F, 22F, -8F);
 		Support2.setTextureSize(64, 64);
 		Support2.mirror = true;
 		setRotation(Support2, 0F, 0F, 0F);
 		Support4 = new ModelRenderer(this, 29, 31);
 		Support4.addBox(0F, 0F, 0F, 12, 1, 1);
 		Support4.setRotationPoint(-7F, 23F, 6F);
 		Support4.setTextureSize(64, 64);
 		Support4.mirror = true;
 		setRotation(Support4, 0F, 1.570796F, 0F);
 		ConnectorPowerTop = new ModelRenderer(this, 0, 13);
 		ConnectorPowerTop.addBox(0F, 0F, 0F, 6, 6, 1);
 		ConnectorPowerTop.setRotationPoint(-3F, 8F, 3F);
 		ConnectorPowerTop.setTextureSize(64, 64);
 		ConnectorPowerTop.mirror = true;
 		setRotation(ConnectorPowerTop, -1.570796F, 0F, 0F);
 
 	}
 
 	@Override
 	public void renderModelAnimated(float f5, float[] progress) {
		
 		Support1.render(f5);
 		Support3.render(f5);
 		Back.render(f5);
 		Body.render(f5);
 		Viewer.render(f5);
 		ConnectorPower.render(f5);
 		Support2.render(f5);
 		Support4.render(f5);
 		if (ampMeter)
 			ConnectorPowerTop.render(f5);
 
 		float center = 1.2f;
 		GL11.glPushMatrix();
 		GL11.glTranslatef(0, center, 0);
 		GL11.glRotatef(progress[0] * 90 - 45, 0, 0, 1);
 		GL11.glTranslatef(0, -center, 0);
 		Needle.render(f5);
 		GL11.glPopMatrix();
 	}
 
 	@Override
 	public void renderModel(float f5) {
 		renderModelAnimated(f5, new float[] { 0 });
 	}
 
 }
