 /*
  *  Copyright (C) 2013 caryoscelus
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Additional permission under GNU GPL version 3 section 7:
  *  If you modify this Program, or any covered work, by linking or combining
  *  it with Clojure (or a modified version of that library), containing parts
  *  covered by the terms of EPL 1.0, the licensors of this Program grant you
  *  additional permission to convey the resulting work. {Corresponding Source
  *  for a non-source form of such a combination shall include the source code
  *  for the parts of Clojure used as well as that of the covered work.}
  */
 
 package gg;
 
 import com.badlogic.gdx.*;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g2d.*;
 import com.badlogic.gdx.scenes.scene2d.*;
 import com.badlogic.gdx.scenes.scene2d.ui.*;
 import com.badlogic.gdx.maps.tiled.*;
 import com.badlogic.gdx.maps.tiled.renderers.*;
 import com.badlogic.gdx.math.*;
 import com.badlogic.gdx.utils.*;
 import com.badlogic.gdx.Input.*;
 
 import java.lang.Math;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * Main game screen: map screen
  */
 public class MapScreen implements Screen, StoryScreen {
     // renderers
     OrthogonalTiledMapRenderer renderer;
     OrthographicCamera camera;
     
     final float TILES_NX = 25;
     final float TILES_NY = TILES_NX*3/4f;
     
     final float TILE_SIZE = 16;
     
     // data
     TiledMap map;
     
     Texture personSprite;
     
     // gameplay
     Person person;
     
     
     boolean inited = false;
     
     // input
     Map<Integer,Boolean> pressed = new HashMap();
     
     
     // UI
     StoryStage storyStage;
     
     public void init () {
         initRenderer();
        initUi();
         initGame();
     }
     
     /**
      * Init map renderer; TODO: move to map renderer class
      */
     public void initRenderer () {
         renderer = new OrthogonalTiledMapRenderer(map, 1 / TILE_SIZE);
         
         camera = new OrthographicCamera();
         camera.setToOrtho(false, TILES_NX, TILES_NY);
         camera.update();
         
         MapDrawableFactory.instance().init();
     }
     
     /**
      * Init ui
      */
     public void initUi () {
         // UI
         UiManager.instance().init();
         
         Story.instance().screen = this;
         Story.instance().addObject("self", person);
         storyStage = new StoryStage();
     }
     
     /**
      * Init gameplay and story; move to gameplay management
      */
     public void initGame () {
         person = new Person();
         person.moveTo("data/maps/map.tmx", new Vector2(50, 50));
         Story.instance().init();
     }
     
     @Override
     public void show () {
         if (!inited) {
             init();
         }
         
         updateStoryStage();
     }
     
     @Override
     public void hide () {
         
     }
     
     public void update(float dt) {
         if (map != person.onMap) {
             map = person.onMap;
             renderer.setMap(map);
         }
         
         // check movement
         float mdx = 0, mdy = 0;
         if (Gdx.input.isKeyPressed(Keys.LEFT)) {
             mdx += -1*dt;
         }
         if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
             mdx += 1*dt;
         }
         if (Gdx.input.isKeyPressed(Keys.UP)) {
             mdy += 1*dt;
         }
         if (Gdx.input.isKeyPressed(Keys.DOWN)) {
             mdy += -1*dt;
         }
         
         person.move.x = mdx;
         person.move.y = mdy;
         person.update(dt);
         
         
         // check feature
         if (Gdx.input.isKeyPressed(Keys.SPACE)) {
             if (!pressed.get(Keys.SPACE)) {
                 pressed.put(Keys.SPACE, true);
                 person.clicked();
             }
         } else {
             pressed.put(Keys.SPACE, false);
         }
         
         
         //check help
         if (Gdx.input.isKeyPressed(Keys.F1)) {
             Story.instance().trigger("help");
         }
         
         // check sound
         // TODO: make proper handler
         if (Gdx.input.isKeyPressed(Keys.F2)) {
             Streamer st = Streamer.instance();
             if (st.isEnabled()) {
                 st.disable();
             } else {
                 st.enable();
             }
         }
     }
     
     @Override
     public void render (float dt) {
         update(dt);
         
         Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
         
         camera.position.x = person.position.x + 1;
         camera.position.y = person.position.y - 1;
         camera.update();
         
         renderer.setView(camera);
         renderer.render();
         
         person.render(renderer.getSpriteBatch());
         
         if (storyStage.show) {
             storyStage.act(dt);
             storyStage.draw();
         }
     }
     
     @Override
     public void resize (int width, int height) {
     }
     
     @Override
     public void pause () {
     }
     
     @Override
     public void resume () {
     }
     
     @Override
     public void dispose () {
         map.dispose();
     }
     
     
     // UI
     /**
      * Setup inputprocessor to storyStage if present
      */
     void updateStoryStage () {
         if (storyStage.show) {
             Gdx.input.setInputProcessor(storyStage);
         }
     }
     
     @Override
     public void showStory (StoryDialog dialogue) {
         if (storyStage.show) {
             hideStory();
         }
         
         storyStage.show = true;
         
         storyStage.setupUi(dialogue);
         
         updateStoryStage();
     }
     
     @Override
     public void hideStory () {
         storyStage.show = false;
         updateStoryStage();
         storyStage.clear();
     }
 }
