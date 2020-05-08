 package com.mobi.badvibes.view.graphics;
 
import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.mobi.badvibes.util.GameUtil;
 import com.mobi.badvibes.view.GameDimension;
 
 public class BVTextureRegion extends TextureRegion
 {
     private Rectangle source;
     private Vector2   scale;
     private Vector2   scaledDimensions;
 
     public BVTextureRegion(TextureRegion r){
         super(r);
         setSourceRect(new Rectangle(getRegionX(), getRegionY(), 150, 38));
     }
     
     public BVTextureRegion(Texture t, Rectangle source)
     {
         super(t);
         setSourceRect(source);
     }
 
     public BVTextureRegion(TextureRegion textureRegion, int i, int j)
     {
         super(textureRegion);
 //        setSourceRect(new Rectangle(i * 150, j * 38, 150, 38));
         setSourceRect(new Rectangle(getRegionX(), getRegionY(), getRegionWidth(), getRegionHeight()));
     }
 
     public int getWidth()
     {
         return (int) scaledDimensions.x;
     }
 
     public int getHeight()
     {
         return (int) scaledDimensions.y;
     }
 
     public void setSourceRect(Rectangle source){
         source = GameUtil.scaleRectangle(source);
         
         setRegion((int) source.x,
                   (int) source.y,
                   (int) source.width,
                   (int) source.height);
 
         this.source             = source;
        this.scale              = new Vector2(1,1);//GameDimension.Scale();
         this.scaledDimensions   = new Vector2(source.width, source.height).mul(scale.x, scale.y);
     }
     
     public Rectangle getSourceRect()
     {
         return this.source;
     }
     
     public void draw(SpriteBatch spriteBatch, Vector2 pos)
     {
         spriteBatch.draw(this, pos.x, pos.y + scaledDimensions.y, scaledDimensions.x, -scaledDimensions.y);
     }
 
     public Vector2 centerAt(Vector2 pos)
     {
         return pos.cpy().div(2).sub(scaledDimensions.cpy().div(2));
     }
 }
