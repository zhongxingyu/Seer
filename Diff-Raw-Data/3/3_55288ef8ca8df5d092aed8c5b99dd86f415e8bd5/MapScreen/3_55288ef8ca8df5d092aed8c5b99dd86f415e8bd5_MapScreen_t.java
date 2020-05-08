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
 
 package shabby;
 
 import shabby.person.*;
 
 import chlorophytum.*;
 import chlorophytum.story.*;
 import chlorophytum.story.view.*;
 import chlorophytum.map.*;
 import chlorophytum.map.view.*;
 
 import com.badlogic.gdx.*;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g2d.*;
 import com.badlogic.gdx.scenes.scene2d.*;
 import com.badlogic.gdx.scenes.scene2d.ui.*;
 import com.badlogic.gdx.maps.tiled.*;
 import com.badlogic.gdx.math.*;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 import java.lang.Math;
 
 /**
  * Main game screen: map screen
  */
 public class MapScreen implements Screen, StoryScreen {
     protected final float TILES_NX = 25;
     protected final float TILES_NY = TILES_NX*3/4f;
     
     protected final float TILE_SIZE = 16;
     
     // data
     protected ChloroMap map;
     
     // gameplay
     public Person person;
     
     protected boolean inited = false;
     
     // input
     protected InputMultiplexer inputMultiplexer;
     
     public KeyboardHandler keyboardHandler;
     
     // UI
     protected ChloroMapStage mapStage;
     protected StoryStage storyStage;
     protected Stage stage;
     
     public void init () {
         initRenderer();
         initUi();
         initGame();
     }
     
     /**
      * Init map stage
      */
     protected void initRenderer () {
         mapStage = new ChloroMapStage();
         mapStage.init(TILE_SIZE, TILES_NX, TILES_NY);
         
         MapObjectViewFactory.init();
     }
     
     /**
      * Init ui
      */
     protected void initUi () {
         // UI
         Story.instance().screen = this;
         storyStage = new StoryStage();
         
         stage = new Stage();
         
         keyboardHandler = new KeyboardHandler();
         keyboardHandler.init();
         
         // Input
         inputMultiplexer = new InputMultiplexer();
         inputMultiplexer.addProcessor(storyStage);
         inputMultiplexer.addProcessor(stage);
         stage.addListener(new ClickListener () {
             @Override
             public void clicked(InputEvent event, float x, float y) {
                 processClick(x, y);
             }
         });
         Gdx.input.setInputProcessor(inputMultiplexer);
     }
     
     /**
      * Init gameplay and story; move to gameplay management
      */
     protected void initGame () {
         person = new Person();
         person.moveTo("data/maps/map.tmx", new Vector2(50, 50));
         Story.instance().init();
        Story.instance().addObject("self", person);
     }
     
     @Override
     public void show () {
         if (!inited) {
             init();
         }
     }
     
     @Override
     public void hide () {
         
     }
     
     /**
      * Logic update
      * @param dt float delta time in seconds
      */
     void update(float dt) {
         if (map != person.onMap) {
             map = person.onMap;
             mapStage.setMap(map);
         }
         
         keyboardHandler.update(dt);
         
         // check movement
         float mdx = 0, mdy = 0;
         if (Gdx.input.isKeyPressed(Keys.LEFT)) {
             mdx += -1;
         }
         if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
             mdx += 1;
         }
         if (Gdx.input.isKeyPressed(Keys.UP)) {
             mdy += 1;
         }
         if (Gdx.input.isKeyPressed(Keys.DOWN)) {
             mdy += -1;
         }
         
         person.move.x = mdx;
         person.move.y = mdy;
         person.update(dt);
     }
     
     /**
      * Process click happend on map
      */
     public void processClick (float x, float y) {
         Vector2 t = screenToMap(new Vector2(x, y)).sub(person.position);
         person.clicked(t.x, t.y);
     }
     
     protected Vector2 getCamPosition() {
         return mapStage.getCamPosition();
     }
     
     /**
      * Convert screen coordinates to map ones
      * @param screenXY Vector2 screen coordinates; value range (-1.0;1.0)
      * @return Vector2 map coordinates
      */
     public Vector2 screenToMap (Vector2 screenXY) {
         return new Vector2(screenXY.x*TILES_NX/2, screenXY.y*TILES_NY/2).add(getCamPosition());
     }
     
     /**
      * Convert map coordinates to screen ones
      * @param screenXY Vector2 map coordinates
      * @return Vector2 screen coordinates
      */
     public Vector2 mapToScreen (Vector2 mapXY) {
         return mapXY.scl(TILE_SIZE).sub(getCamPosition());
     }
     
     @Override
     public void render (float dt) {
         update(dt);
         
         Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
         
         mapStage.setPosition(new Vector2(person.position.x + 1, person.position.y - 1));
         mapStage.act(dt);
         mapStage.draw();
         
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
     
     
     @Override
     public void showStory (StoryContext context) {
         storyStage.setContext(context);
     }
 }
