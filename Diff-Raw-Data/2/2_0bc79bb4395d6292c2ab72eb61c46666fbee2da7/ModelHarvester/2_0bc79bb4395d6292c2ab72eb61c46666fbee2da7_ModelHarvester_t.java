 package mods.ltp.models;
 
 import net.minecraft.client.model.ModelBase;
 import net.minecraft.client.model.ModelRenderer;
 import net.minecraft.entity.Entity;
 
 public class ModelHarvester extends ModelBase
 {
   //fields
     ModelRenderer Shape1;
     ModelRenderer Shape2;
     ModelRenderer Shape3;
     ModelRenderer Shape4;
     ModelRenderer Shape5;
     ModelRenderer Shape6;
   
   public ModelHarvester()
   {
     textureWidth = 64;
     textureHeight = 64;
     
       Shape1 = new ModelRenderer(this, 0, 0);
       Shape1.addBox(0F, 0F, 0F, 15, 16, 1);
       Shape1.setRotationPoint(-8F, 8F, -8F);
       Shape1.setTextureSize(64, 64);
       Shape1.mirror = true;
       setRotation(Shape1, 0F, 0F, 0F);
       Shape2 = new ModelRenderer(this, 0, 0);
       Shape2.addBox(0F, 0F, 0F, 15, 16, 1);
       Shape2.setRotationPoint(-7F, 8F, 7F);
       Shape2.setTextureSize(64, 64);
       Shape2.mirror = true;
       setRotation(Shape2, 0F, 0F, 0F);
       Shape3 = new ModelRenderer(this, 0, 17);
       Shape3.addBox(0F, 0F, 0F, 1, 16, 15);
       Shape3.setRotationPoint(7F, 8F, -8F);
       Shape3.setTextureSize(64, 64);
       Shape3.mirror = true;
       setRotation(Shape3, 0F, 0F, 0F);
       Shape4 = new ModelRenderer(this, 0, 17);
       Shape4.addBox(0F, 0F, 0F, 1, 16, 15);
       Shape4.setRotationPoint(-8F, 8F, -7F);
       Shape4.setTextureSize(64, 64);
       Shape4.mirror = true;
       setRotation(Shape4, 0F, 0F, 0F);
       Shape5 = new ModelRenderer(this, -14, 48);
       Shape5.addBox(0F, 0F, 0F, 14, 1, 14);
       Shape5.setRotationPoint(-7F, 23F, -7F);
       Shape5.setTextureSize(64, 64);
       Shape5.mirror = true;
       setRotation(Shape5, 0F, 0F, 0F);
       Shape6 = new ModelRenderer(this, 36, 48);
       Shape6.addBox(0F, 0F, 0F, 14, 1, 14);
       Shape6.setRotationPoint(-7F, 10F, -7F);
       Shape6.setTextureSize(64, 64);
       Shape6.mirror = true;
       setRotation(Shape6, 0F, 0F, 0F);
   }
   
   public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {    
     super.render(entity, f, f1, f2, f3, f4, f5);
     setRotationAngles(f, f1, f2, f3, f4, f5, entity);
     Shape1.render(f5);
     Shape2.render(f5);
     Shape3.render(f5);
     Shape4.render(f5);
     Shape5.render(f5);
     Shape6.render(f5);
   }
   
   private void setRotation(ModelRenderer model, float x, float y, float z)
   {
     model.rotateAngleX = x;
     model.rotateAngleY = y;
     model.rotateAngleZ = z;
   }
   
   public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
   {
     super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
   }
 
 }
