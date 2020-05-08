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
 
 package com.nebula2d.editor.ui;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Rectangle;
 import com.nebula2d.editor.framework.GameObject;
 import com.nebula2d.editor.framework.Project;
 
 
 public class RenderAdapter implements ApplicationListener {
 
     private GameObject selectedObject;
     private boolean enabled;
     private OrthographicCamera camera;
    private SpriteBatch batcher;
 
     public RenderAdapter() {
 
     }
 
     public void initCamera(int w, int h) {
         camera = new OrthographicCamera(w, h);
     }
 
     public OrthographicCamera getCamera() {
         return camera;
     }
 
     public GameObject getSelectedObject() {
         return selectedObject;
     }
 
     public void setSelectedObject(GameObject selectedObject) {
         this.selectedObject = selectedObject;
     }
 
     @Override
     public void create() {
        batcher = new SpriteBatch();
     }
 
     @Override
     public void resize(int width, int height) {
         camera.viewportWidth = width;
         camera.viewportHeight = height;
         camera.update();
     }
 
     @Override
     public void render() {
         Gdx.graphics.getGL20().glClearColor(.17f, .17f, .17f, 1.0f);
         Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
         if (!enabled)
             return;
 
         if (camera == null)
             return;
 
         batcher.setProjectionMatrix(camera.projection);
         batcher.begin();
         Project p = MainFrame.getProject();
         if (p != null && p.getCurrentScene() != null) {
             p.getCurrentScene().render(selectedObject, batcher, camera);
         }
         batcher.end();
 
         if (selectedObject != null && selectedObject.getRenderer() != null &&
                 selectedObject.getRenderer().isReady()) {
             Rectangle boundingBox = selectedObject.getRenderer().getBoundingBox(camera);
 
             if (boundingBox != null) {
                 Gdx.gl.glEnable(GL20.GL_BLEND);
 //                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                 ShapeRenderer shape = new ShapeRenderer();
                 shape.setColor(new Color(0.0f, 1.0f, 0.0f, 0.5f));
                 shape.begin(ShapeRenderer.ShapeType.Filled);
 
                 float x = boundingBox.getX();
                 float y = boundingBox.getY();
                 shape.rect(x, y, boundingBox.getWidth(), boundingBox.getHeight());
 
                 shape.end();
                 Gdx.gl.glDisable(GL20.GL_BLEND);
             }
         }
     }
 
     @Override
     public void pause() {
 
     }
 
     @Override
     public void resume() {
     }
 
     @Override
     public void dispose() {
        batcher.dispose();
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 }
