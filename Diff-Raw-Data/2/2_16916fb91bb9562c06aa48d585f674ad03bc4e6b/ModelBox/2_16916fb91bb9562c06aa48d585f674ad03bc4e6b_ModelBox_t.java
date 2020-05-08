 package ml.boxes.client;
 
 import net.minecraft.client.model.ModelBase;
 import net.minecraft.client.model.ModelRenderer;
 
 public class ModelBox extends ModelBase {
 
 	private int texX = 128;
 	private int texY = 128;
 	
 	public ModelRenderer bottom;
 	public ModelRenderer flap1; //X+
 	public ModelRenderer flap2; //X-
 	public ModelRenderer flap3; //Z-
 	public ModelRenderer flap4; //Z+
 	public ModelRenderer[] sides = new ModelRenderer[4];
 	
 	public ModelBox() {
 		bottom = new ModelRenderer(this, 0, 0).setTextureSize(texX, texY);
 		bottom.addBox(0F, 0F, 0F, 12, 1, 12, 0.0F);
 		bottom.rotationPointX = 2F;
 		bottom.rotationPointZ = 2F;
 
 		flap1 = new ModelRenderer(this, 0, 45).setTextureSize(texX, texY);
 		flap1.addBox(0F, -0.5F, 0F, 12, 1, 6);
 		flap1.rotationPointX = 2F;
 		flap1.rotationPointY = 13.5F;
 		flap1.rotationPointZ = 2F;
 
 		flap2 = new ModelRenderer(this, 38, 45).setTextureSize(texX, texY);
 		flap2.addBox(0F, -0.5F, -6F, 12, 1, 6);
 		flap2.rotationPointX = 2F;
 		flap2.rotationPointY = 13.5F;
 		flap2.rotationPointZ = 14F;
 
 		flap3 = new ModelRenderer(this, 0, 54).setTextureSize(texX, texY);
 		flap3.addBox(0F, -0.5F, 0F, 6, 1, 12);
 		flap3.rotationPointX = 2F;
 		flap3.rotationPointY = 13.4F;
 		flap3.rotationPointZ = 2F;
 
 		flap4 = new ModelRenderer(this, 38, 54).setTextureSize(texX, texY);
 		flap4.addBox(-6F, -0.5F, 0F, 6, 1, 12);
 		flap4.rotationPointX = 14F;
 		flap4.rotationPointY = 13.4F;
 		flap4.rotationPointZ = 2F;
 
 		for (int i=0; i<4; i++){
 			ModelRenderer side = new ModelRenderer(this, i*32, 15).setTextureSize(texX, texY);
 			side.addBox(-7F, 0F, -7F, 1, 14, 14);
 			side.rotationPointX = 8F;
 			side.rotationPointY = 0F;
			side.rotationPointZ = 8F;
 			side.rotateAngleY = (float)(i*(Math.PI/2));
 			sides[i] = side;
 		}
 		
 	}
 	
 	public void renderAll(){
 		bottom.render(0.0625F);
 		flap1.render(0.0625F);
 		flap2.render(0.0625F);
 		flap3.render(0.0625F);
 		flap4.render(0.0625F);
 		
 		for (ModelRenderer mr : sides){
 			mr.render(0.0625F);
 		}
 		
 	}
 }
