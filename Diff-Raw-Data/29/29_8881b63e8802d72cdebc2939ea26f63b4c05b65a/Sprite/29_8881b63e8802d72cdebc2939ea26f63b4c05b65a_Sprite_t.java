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
 
 package com.nebula2d.editor.framework.assets;
 
 import com.badlogic.gdx.Gdx;
 import com.nebula2d.editor.util.FullBufferedReader;
 import com.nebula2d.editor.util.FullBufferedWriter;
 
 import java.io.IOException;
 
 public class Sprite extends Asset {
 
     protected com.badlogic.gdx.graphics.Texture texture;
     protected boolean spriteSheet;
 
     public Sprite(final String path) {
         super(path);
         Gdx.app.postRunnable(new Runnable() {
             @Override
             public void run() {
                 texture = new com.badlogic.gdx.graphics.Texture(path);
             }
         });

         spriteSheet = false;
     }
 
     public int getWidth() {
         return this.texture.getWidth();
     }
 
     public int getHeight() {
         return this.texture.getHeight();
     }
 
     public boolean isSpriteSheet() {
         return spriteSheet;
     }
 
     public void setSpriteSheet(boolean spriteSheet) {
         this.spriteSheet = spriteSheet;
     }
 
     public com.badlogic.gdx.graphics.Texture getTexture() {
         return texture;
     }
 
     @Override
     public void save(FullBufferedWriter fw) throws IOException {
         super.save(fw);
         String type = isSpriteSheet() ? "SpriteSheet" : "Sprite";
         fw.writeLine(type);
     }
 
     @Override
     public void load(FullBufferedReader fr) throws IOException {
         String type = fr.readLine();
         if (type.equalsIgnoreCase("Spritesheet"))
             setSpriteSheet(true);
     }
 }
