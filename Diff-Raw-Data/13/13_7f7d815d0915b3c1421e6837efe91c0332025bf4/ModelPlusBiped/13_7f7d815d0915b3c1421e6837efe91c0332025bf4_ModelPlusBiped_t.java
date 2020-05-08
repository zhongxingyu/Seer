 package info.aoisensi.skinplus.client.model;
 
 import org.lwjgl.opengl.GL11;
 
 import net.minecraft.client.model.ModelBiped;
 import net.minecraft.client.model.ModelRenderer;
 import net.minecraft.entity.Entity;
 import net.minecraft.util.MathHelper;
 
 public class ModelPlusBiped extends ModelBiped {
 
 	public ModelRenderer plusTest;
 
 	public ModelPlusBiped()
 	{
 		this(0.0F);
 	}
 
 	public ModelPlusBiped(float par1)
 	{
 		this(par1, 0.0F, 64, 32);
 	}
 
 	public ModelPlusBiped(float par1, float par2, int par3, int par4)
 	{
 		super(par1, par2, par3, par4);
 
 		//texture offset
 		this.plusTest = new ModelRenderer(this, 0, 0);
 		//origin size
 		this.plusTest.addBox(-1.0F, -8.0F, -1.0F, 2, 30, 2, par1);
 		//roll
 		this.plusTest.setRotationPoint(5.0F, 0.0F + par2, 0.0F);
 
 		this.bipedHead.addChild(this.plusTest);
 	}
 
 	public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
 	{
 		super.render(par1Entity, par2, par3, par4, par5, par6, par7);
 		//this.plusTest.render(par7);
 	}
 
 	float f = 0.0F;
 
 	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity)
 	{
		f += 3.14F;
 		super.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity);
		plusTest.rotateAngleX = bipedHead.rotateAngleX + f;
 	}
 }
