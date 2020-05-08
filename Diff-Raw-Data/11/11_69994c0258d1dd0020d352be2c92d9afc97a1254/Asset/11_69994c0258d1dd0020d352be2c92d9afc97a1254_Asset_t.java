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
 
 package com.nebula2d.assets;
 
import com.badlogic.gdx.utils.Disposable;

 import java.io.File;
 
 /**
  * Asset is an abstract base class for a game resource,
  * such as audio files, image files, script fiels, etc...
  *
  * @author Jon Bonazza <jonbonazza@gmail.com>
  */
public abstract class Asset<T> implements Disposable {
 
     protected String filename;
     protected String path;
     protected boolean loaded;
 
     protected T data;
 
     protected Asset(String path, T data) {
         this.path = path;
         this.filename = path.substring(path.lastIndexOf(File.separator));
         this.loaded = false;
         this.data = data;
     }
 
     public T getData() {
         return data;
     }
 
     public String getFilename() {
         return filename;
     }
 
     public String getPath() {
         return path;
     }
 
     public boolean isLoaded() {
         return loaded;
     }
 
     public void setLoaded(boolean loaded) {
         this.loaded = loaded;
     }

    public void dispose() {
        if (data != null && data instanceof Disposable)
            ((Disposable) data).dispose();
    }
 }
