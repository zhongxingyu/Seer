 /*
  * Nebula2D is a cross-platform, 2D game engine for PC, Mac, & Linux
  * Copyright (c) 2014 Jon Bonazza
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.nebula2d.editor.framework.components;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.nebula2d.editor.framework.GameObject;
 import com.nebula2d.editor.framework.assets.Sprite;
 import com.nebula2d.editor.ui.KeyFrameAnimationEditDialog;
 import com.nebula2d.editor.util.FullBufferedReader;
 import com.nebula2d.editor.util.FullBufferedWriter;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 public class KeyFrameAnimation extends Animation {
 
     protected TextureRegion[] frames;
     protected int frameWidth;
     protected int frameHeight;
     protected int startFrame;
     protected int endFrame;
     protected float speed;
     protected Sprite sprite;
     protected com.badlogic.gdx.graphics.g2d.Animation animation;
     protected float stateTime;
     protected boolean wrap;
 
     /**
      * Ctor used for loading animation form disk
      * @param sprite the {@link com.nebula2d.editor.framework.assets.Sprite} to associate with the animation
      */
     public KeyFrameAnimation(String name, Sprite sprite) {
         super(name);
         this.sprite = sprite;
         this.sprite.setSpriteSheet(true);
         wrap = false;
         animationType = AnimationType.KEY_FRAME;
     }
 
     public int getFrameWidth() { return frameWidth; }
 
     public int getFrameHeight() {
         return frameHeight;
     }
 
     public float getSpeed() {
         return speed;
     }
 
     public int getStartFrameIndex() { return startFrame; }
 
     public int getEndFrameIndex() { return endFrame; }
 
    public TextureRegion getStartFrame() {
        if (frames == null)
            init();

        return frames[startFrame];
    }
 
     public boolean wrap() { return wrap; }
 
     public void setFrameWidth(int frameWidth) {
         this.frameWidth = frameWidth;
     }
 
     public void setFrameHeight(int frameHeight) { this.frameHeight = frameHeight; }
 
     public void setSpeed(float speed) {
         this.speed = speed;
     }
 
     public void setStartFrameIndex(int startFrame) { this.startFrame = startFrame; }
 
     public void setEndFrameIndex(int endFrame) { this.endFrame = endFrame; }
 
     public void setStateTime(float stateTime) { this.stateTime = stateTime; }
 
     public void setWrap(boolean wrap) { this.wrap = wrap; }
 
     @Override
     public void load(FullBufferedReader fr) throws IOException {
         this.frameWidth = fr.readIntLine();
         this.frameHeight = fr.readIntLine();
         this.speed = fr.readFloatLine();
         this.startFrame = fr.readIntLine();
         this.endFrame = fr.readIntLine();
     }
 
     @Override
     public void save(FullBufferedWriter fw) throws IOException {
         super.save(fw);
         fw.writeIntLine(this.frameWidth);
         fw.writeIntLine(this.frameHeight);
         fw.writeFloatLine(this.speed);
         fw.writeIntLine(this.startFrame);
         fw.writeIntLine(this.endFrame);
     }
 
     @Override
     public void showAnimationEditDialog() {
         new KeyFrameAnimationEditDialog(this);
     }
     
     @Override
     public void renderStill(SpriteBatch batch, GameObject gameObject, Camera cam) {
         TextureRegion frame = getStartFrame();
         float halfw = sprite.getWidth() / 2.0f;
         float halfh = sprite.getHeight() / 2.0f;
         batch.draw(frame,
                 gameObject.getPosition().x - halfw - cam.position.x,
                 gameObject.getPosition().y - halfh - cam.position.y,
                 gameObject.getPosition().x - halfw - cam.position.x,
                 gameObject.getPosition().y - halfh - cam.position.y,
                 frame.getRegionWidth(),
                 frame.getRegionHeight(),
                 gameObject.getScale().x,
                 gameObject.getScale().y,
                 gameObject.getRotation());
     }
 
     @Override
     public void renderAnimated(SpriteBatch batch, Camera cam, int canvasW, int canvasH) {
 
         if (frames != null) {
 
             stateTime += Gdx.graphics.getDeltaTime();
             TextureRegion currentFrame = animation.getKeyFrame(stateTime, wrap);
             float imgW = currentFrame.getRegionWidth();
             float imgH = currentFrame.getRegionHeight();
 
             if (imgW > imgH) {
                 imgW *= canvasH / imgH;
                 imgH = canvasH;
             } else if (imgH > imgW) {
                 imgH *= canvasW / imgW;
                 imgW = canvasW;
             } else {
                 imgW = canvasW;
                 imgH = canvasH;
             }
 
             batch.draw(currentFrame, canvasW/2.0f - imgW / 2.0f, canvasH/2.0f - imgH/2.0f, imgW, imgH);
         }
     }
 
     @Override
     public Sprite getSprite() { return sprite; }
 
     @Override
     public void init() {
         if (frameWidth > 0 && frameHeight > 0 && startFrame >= 0 && endFrame >= 0 &&
                 endFrame >= startFrame && speed > 0) {
             stateTime = 0;
             com.badlogic.gdx.graphics.Texture tmpTexture = sprite.getTexture();
             TextureRegion[][] tmpRegions = TextureRegion.split(tmpTexture, frameWidth, frameHeight);
 
             int numRows = tmpRegions.length;
             int numCols = tmpRegions[0].length;
             frames = new TextureRegion[numRows * numCols];
 
             //FIXME: This can be optimized, but I can't be assed with it at the moment.
             int index = 0;
             for (TextureRegion[] tmpRegion : tmpRegions) {
                 for (int j = 0; j < numCols; ++j) {
                     frames[index++] = tmpRegion[j];
                 }
             }
 
             frames = Arrays.copyOfRange(frames, startFrame, endFrame + 1);
             animation = new com.badlogic.gdx.graphics.g2d.Animation(speed, frames);
         }
     }

    @Override
    public boolean isRenderable() {
        boolean framesValid = startFrame > 0 ? endFrame > startFrame : endFrame > 0;
        return frameWidth > 0 && frameHeight > 0 && framesValid;
    }
 }
