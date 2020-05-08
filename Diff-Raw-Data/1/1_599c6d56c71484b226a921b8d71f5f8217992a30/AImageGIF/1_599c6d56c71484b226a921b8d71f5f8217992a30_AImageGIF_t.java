 package fr.ravenfeld.livewallpaper.library.objects.simple;
 
 import rajawali.materials.Material;
 import rajawali.materials.textures.ASingleTexture;
 import rajawali.materials.textures.ATexture;
 import rajawali.materials.textures.AnimatedGIFTexture;
 import rajawali.materials.textures.Texture;
 import rajawali.primitives.Plane;
 
 public abstract class AImageGIF  extends  AElement{
     protected AnimatedGIFTexture mTexture;
 
     public AnimatedGIFTexture getTexture(){
         return mTexture;
     }
 
     public void setLoop(boolean loop) {
         mTexture.setLoop(loop);
     }
 
     public void rewind() {
         mTexture.rewind();
     }
 
     public void animate() {
         mTexture.animate();
     }
 
     public void stopAnimation() {
         mTexture.stopAnimation();
     }
 
     public void update() throws ATexture.TextureException {
         mTexture.update();
     }
 
     @Override
     public int getWidth() {
         return mTexture.getWidth();
     }
 
     @Override
     public int getHeight() {
         return mTexture.getHeight();
     }
 
     @Override
     public void surfaceDestroyed() throws ATexture.TextureException {
         mMaterial.removeTexture(mTexture);
         mTexture.reset();
        mTexture.remove();
     }
 }
