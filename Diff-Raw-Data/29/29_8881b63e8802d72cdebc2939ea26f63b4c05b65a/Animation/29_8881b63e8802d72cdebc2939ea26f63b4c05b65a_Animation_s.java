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
 
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.nebula2d.editor.common.ISerializable;
 import com.nebula2d.editor.framework.GameObject;
 import com.nebula2d.editor.framework.assets.Sprite;
 import com.nebula2d.editor.util.FullBufferedWriter;
 
 import java.io.IOException;
 
 public abstract class Animation implements ISerializable {
 
     public static enum AnimationType {
         KEY_FRAME
     }
     protected String name;
     protected AnimationType animationType;
 
     public Animation(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public abstract Sprite getSprite();
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public void save(FullBufferedWriter fw) throws IOException {
         fw.writeLine(name);
         fw.writeLine(animationType.name());
     }
 
     @Override
     public String toString() {
         return name;
     }
 
     public abstract void showAnimationEditDialog();
 
     public abstract void renderStill(SpriteBatch batch, GameObject gameObject, Camera cam);
 
     public abstract void renderAnimated(SpriteBatch batch, Camera cam, int canvasW, int canvasH);
 
     public abstract void init();
 }
